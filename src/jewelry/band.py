"""Band-profile tools for jewelry CAD."""

from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from typing import List

import numpy as np

from src.geometry.nurbs import NURBSCurve


class ProfileShape(str, Enum):
    FLAT = "flat"
    DOMED = "domed"
    COMFORT_FIT = "comfort_fit"
    KNIFE_EDGE = "knife_edge"


@dataclass
class BandProfile:
    """Cross-section profile for a ring band.

    Parameters
    ----------
    width_mm : float
        Full width of the band.
    thickness_mm : float
        Maximum thickness of the band.
    shape : ProfileShape
        Type of profile.
    """

    width_mm: float = 4.0
    thickness_mm: float = 1.5
    shape: ProfileShape = ProfileShape.DOMED

    def generate_curve(self) -> NURBSCurve:
        """Return a NURBS cross-section curve for the band profile."""
        hw = self.width_mm / 2.0
        t = self.thickness_mm

        if self.shape == ProfileShape.FLAT:
            pts = [[-hw, 0, 0], [-hw, t, 0], [hw, t, 0], [hw, 0, 0]]
        elif self.shape == ProfileShape.DOMED:
            pts = [
                [-hw, 0, 0],
                [-hw, t * 0.5, 0],
                [-hw * 0.5, t, 0],
                [0, t * 1.1, 0],
                [hw * 0.5, t, 0],
                [hw, t * 0.5, 0],
                [hw, 0, 0],
            ]
        elif self.shape == ProfileShape.COMFORT_FIT:
            pts = [
                [-hw, 0.2, 0],
                [-hw, t * 0.6, 0],
                [-hw * 0.4, t, 0],
                [0, t * 1.05, 0],
                [hw * 0.4, t, 0],
                [hw, t * 0.6, 0],
                [hw, 0.2, 0],
            ]
        else:  # KNIFE_EDGE
            pts = [
                [-hw, 0, 0],
                [-hw * 0.3, t * 0.8, 0],
                [0, t, 0],
                [hw * 0.3, t * 0.8, 0],
                [hw, 0, 0],
            ]

        degree = min(3, len(pts) - 1)
        return NURBSCurve(control_points=np.array(pts), degree=degree)
