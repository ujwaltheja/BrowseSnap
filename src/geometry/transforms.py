"""Geometric transforms for solid bodies."""

from __future__ import annotations

import numpy as np

from src.geometry.solid import Mesh, SolidBody


def _apply_to_mesh(mesh: Mesh, matrix: np.ndarray) -> Mesh:
    """Apply a 4×4 homogeneous transform matrix to a mesh."""
    ones = np.ones((mesh.num_vertices, 1))
    homo = np.hstack([mesh.vertices, ones])  # (V, 4)
    transformed = (matrix @ homo.T).T[:, :3]
    return Mesh(transformed, mesh.faces.copy())


def translate(body: SolidBody, offset: tuple[float, float, float]) -> SolidBody:
    """Return a translated copy of *body*."""
    mat = np.eye(4)
    mat[:3, 3] = offset
    new_mesh = _apply_to_mesh(body.mesh, mat) if body.mesh else None
    return SolidBody(name=body.name, mesh=new_mesh, parameters={**body.parameters, "translate": list(offset)})


def rotate(body: SolidBody, axis: tuple[float, float, float], angle_deg: float) -> SolidBody:
    """Return a copy of *body* rotated by *angle_deg* around *axis*."""
    ax = np.asarray(axis, dtype=np.float64)
    ax = ax / (np.linalg.norm(ax) + 1e-30)
    rad = np.radians(angle_deg)
    c, s = np.cos(rad), np.sin(rad)
    x, y, z = ax
    rot = np.array([
        [c + x * x * (1 - c), x * y * (1 - c) - z * s, x * z * (1 - c) + y * s],
        [y * x * (1 - c) + z * s, c + y * y * (1 - c), y * z * (1 - c) - x * s],
        [z * x * (1 - c) - y * s, z * y * (1 - c) + x * s, c + z * z * (1 - c)],
    ])
    mat = np.eye(4)
    mat[:3, :3] = rot
    new_mesh = _apply_to_mesh(body.mesh, mat) if body.mesh else None
    return SolidBody(name=body.name, mesh=new_mesh, parameters={**body.parameters, "rotate_axis": list(axis), "rotate_angle": angle_deg})


def scale(body: SolidBody, factors: tuple[float, float, float]) -> SolidBody:
    """Return a scaled copy of *body*."""
    mat = np.diag([factors[0], factors[1], factors[2], 1.0])
    new_mesh = _apply_to_mesh(body.mesh, mat) if body.mesh else None
    return SolidBody(name=body.name, mesh=new_mesh, parameters={**body.parameters, "scale": list(factors)})


def mirror(body: SolidBody, plane_normal: tuple[float, float, float] = (1, 0, 0)) -> SolidBody:
    """Return a mirrored copy of *body* across the plane defined by *plane_normal*."""
    n = np.asarray(plane_normal, dtype=np.float64)
    n = n / (np.linalg.norm(n) + 1e-30)
    # Householder reflection matrix
    ref = np.eye(3) - 2.0 * np.outer(n, n)
    mat = np.eye(4)
    mat[:3, :3] = ref
    new_mesh = _apply_to_mesh(body.mesh, mat) if body.mesh else None
    return SolidBody(name=body.name, mesh=new_mesh, parameters={**body.parameters, "mirror_normal": list(plane_normal)})
