"""NURBS curves and surfaces for parametric jewelry modeling."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import List, Sequence

import numpy as np


@dataclass
class NURBSCurve:
    """Non-Uniform Rational B-Spline curve.

    Parameters
    ----------
    control_points : array-like of shape (n, 3)
        3-D control points.
    weights : array-like of shape (n,), optional
        Rational weights (default all 1.0).
    degree : int
        Polynomial degree (default 3).
    knots : array-like, optional
        Knot vector.  Auto-generated (clamped uniform) when *None*.
    """

    control_points: np.ndarray
    weights: np.ndarray = None  # type: ignore[assignment]
    degree: int = 3
    knots: np.ndarray = field(default=None)  # type: ignore[assignment]

    def __post_init__(self) -> None:
        self.control_points = np.asarray(self.control_points, dtype=np.float64)
        if self.control_points.ndim != 2 or self.control_points.shape[1] != 3:
            raise ValueError("control_points must have shape (n, 3)")

        n = len(self.control_points)
        if self.weights is None:
            self.weights = np.ones(n, dtype=np.float64)
        else:
            self.weights = np.asarray(self.weights, dtype=np.float64)
        if len(self.weights) != n:
            raise ValueError("weights length must match number of control points")

        if self.knots is None:
            self.knots = _clamped_knot_vector(n, self.degree)
        else:
            self.knots = np.asarray(self.knots, dtype=np.float64)

    # ------------------------------------------------------------------
    # Evaluation
    # ------------------------------------------------------------------

    def evaluate(self, t: float) -> np.ndarray:
        """Evaluate the curve at parameter *t* ∈ [0, 1].

        Returns a 3-D point as ``np.ndarray`` of shape ``(3,)``.
        """
        t = float(np.clip(t, 0.0, 1.0))
        # Map t from [0,1] to the knot span
        t_mapped = self.knots[self.degree] + t * (
            self.knots[len(self.control_points)] - self.knots[self.degree]
        )
        return self._deboor(t_mapped)

    def sample(self, num_points: int = 50) -> np.ndarray:
        """Return *num_points* evenly-spaced samples along the curve.

        Returns array of shape ``(num_points, 3)``.
        """
        ts = np.linspace(0.0, 1.0, num_points)
        return np.array([self.evaluate(t) for t in ts])

    # ------------------------------------------------------------------
    # De Boor evaluation
    # ------------------------------------------------------------------

    def _deboor(self, t: float) -> np.ndarray:
        """De Boor's algorithm for rational B-spline evaluation."""
        p = self.degree
        knots = self.knots
        n = len(self.control_points)

        # Find knot span
        span = p
        for i in range(p, n):
            if t < knots[i + 1]:
                span = i
                break
        else:
            span = n - 1

        # Homogeneous control points  (x*w, y*w, z*w, w)
        pw = np.column_stack(
            [self.control_points * self.weights[:, None], self.weights]
        )

        d = [pw[span - p + j].copy() for j in range(p + 1)]

        for r in range(1, p + 1):
            for j in range(p, r - 1, -1):
                left = span - p + j
                denom = knots[left + p - r + 1] - knots[left]
                if abs(denom) < 1e-14:
                    alpha = 0.0
                else:
                    alpha = (t - knots[left]) / denom
                d[j] = (1.0 - alpha) * d[j - 1] + alpha * d[j]

        w = d[p][3]
        if abs(w) < 1e-14:
            return d[p][:3]
        return d[p][:3] / w


@dataclass
class NURBSSurface:
    """Tensor-product NURBS surface.

    Parameters
    ----------
    control_net : array-like of shape (nu, nv, 3)
    weights : array-like of shape (nu, nv), optional
    degree_u, degree_v : int
    knots_u, knots_v : array-like, optional
    """

    control_net: np.ndarray
    weights: np.ndarray = None  # type: ignore[assignment]
    degree_u: int = 3
    degree_v: int = 3
    knots_u: np.ndarray = field(default=None)  # type: ignore[assignment]
    knots_v: np.ndarray = field(default=None)  # type: ignore[assignment]

    def __post_init__(self) -> None:
        self.control_net = np.asarray(self.control_net, dtype=np.float64)
        if self.control_net.ndim != 3 or self.control_net.shape[2] != 3:
            raise ValueError("control_net must have shape (nu, nv, 3)")
        nu, nv, _ = self.control_net.shape
        if self.weights is None:
            self.weights = np.ones((nu, nv), dtype=np.float64)
        else:
            self.weights = np.asarray(self.weights, dtype=np.float64)
        if self.knots_u is None:
            self.knots_u = _clamped_knot_vector(nu, self.degree_u)
        if self.knots_v is None:
            self.knots_v = _clamped_knot_vector(nv, self.degree_v)

    def evaluate(self, u: float, v: float) -> np.ndarray:
        """Evaluate the surface at parameters *(u, v)* ∈ [0, 1]²."""
        nu, nv, _ = self.control_net.shape
        # Evaluate iso-curves in u-direction first
        curves_v: List[np.ndarray] = []
        weights_v: List[float] = []
        for j in range(nv):
            curve = NURBSCurve(
                self.control_net[:, j, :],
                self.weights[:, j],
                self.degree_u,
                self.knots_u.copy(),
            )
            # Evaluate returns 3-D point; we need the weighted point too
            pt = curve.evaluate(u)
            curves_v.append(pt)
            weights_v.append(1.0)

        curve_v = NURBSCurve(
            np.array(curves_v),
            np.array(weights_v),
            min(self.degree_v, nv - 1),
        )
        return curve_v.evaluate(v)


# ------------------------------------------------------------------
# Helpers
# ------------------------------------------------------------------

def _clamped_knot_vector(n: int, degree: int) -> np.ndarray:
    """Generate a clamped uniform knot vector of length *n + degree + 1*."""
    p = degree
    m = n + p + 1
    knots = np.zeros(m, dtype=np.float64)
    for i in range(m):
        if i <= p:
            knots[i] = 0.0
        elif i >= m - p - 1:
            knots[i] = 1.0
        else:
            knots[i] = (i - p) / (n - p)
    return knots
