"""Solid-body representation and Boolean operations."""

from __future__ import annotations

import uuid
from dataclasses import dataclass, field
from typing import List, Optional

import numpy as np


@dataclass
class Mesh:
    """Triangle mesh used as the boundary representation of a solid."""

    vertices: np.ndarray  # (V, 3)
    faces: np.ndarray  # (F, 3) — indices into vertices

    def __post_init__(self) -> None:
        self.vertices = np.asarray(self.vertices, dtype=np.float64)
        self.faces = np.asarray(self.faces, dtype=np.int64)

    @property
    def num_vertices(self) -> int:
        return len(self.vertices)

    @property
    def num_faces(self) -> int:
        return len(self.faces)

    def bounding_box(self) -> tuple[np.ndarray, np.ndarray]:
        """Return ``(min_corner, max_corner)`` axis-aligned bounding box."""
        return self.vertices.min(axis=0), self.vertices.max(axis=0)

    def volume(self) -> float:
        """Compute signed volume using the divergence theorem."""
        total = 0.0
        for face in self.faces:
            v0, v1, v2 = self.vertices[face]
            total += np.dot(v0, np.cross(v1, v2))
        return abs(total) / 6.0

    def translate(self, offset: np.ndarray) -> "Mesh":
        return Mesh(self.vertices + np.asarray(offset), self.faces.copy())


@dataclass
class SolidBody:
    """Parametric solid body composed of a mesh and metadata."""

    id: str = field(default_factory=lambda: uuid.uuid4().hex[:12])
    name: str = "solid"
    mesh: Optional[Mesh] = None
    parameters: dict = field(default_factory=dict)

    @property
    def is_valid(self) -> bool:
        return self.mesh is not None and self.mesh.num_faces > 0

    def bounding_box(self) -> Optional[tuple[np.ndarray, np.ndarray]]:
        if self.mesh is None:
            return None
        return self.mesh.bounding_box()

    def volume(self) -> float:
        if self.mesh is None:
            return 0.0
        return self.mesh.volume()


# ------------------------------------------------------------------
# Boolean operations (simplified mesh union/subtract/intersect)
# ------------------------------------------------------------------

def boolean_union(a: SolidBody, b: SolidBody) -> SolidBody:
    """Combine two solids into one (mesh concatenation approximation)."""
    if a.mesh is None or b.mesh is None:
        raise ValueError("Both solids must have valid meshes")
    combined_verts = np.vstack([a.mesh.vertices, b.mesh.vertices])
    offset = a.mesh.num_vertices
    combined_faces = np.vstack([a.mesh.faces, b.mesh.faces + offset])
    return SolidBody(
        name=f"union({a.name},{b.name})",
        mesh=Mesh(combined_verts, combined_faces),
        parameters={"operation": "union", "operands": [a.id, b.id]},
    )


def boolean_subtract(a: SolidBody, b: SolidBody) -> SolidBody:
    """Subtract solid *b* from *a*.

    This simplified implementation keeps only faces of *a* whose centroids
    fall outside the bounding box of *b*.
    """
    if a.mesh is None or b.mesh is None:
        raise ValueError("Both solids must have valid meshes")
    bb_min, bb_max = b.mesh.bounding_box()
    keep: List[int] = []
    for i, face in enumerate(a.mesh.faces):
        centroid = a.mesh.vertices[face].mean(axis=0)
        inside = np.all(centroid >= bb_min) and np.all(centroid <= bb_max)
        if not inside:
            keep.append(i)
    if not keep:
        return SolidBody(
            name=f"subtract({a.name},{b.name})",
            mesh=Mesh(a.mesh.vertices, np.empty((0, 3), dtype=np.int64)),
            parameters={"operation": "subtract", "operands": [a.id, b.id]},
        )
    new_faces = a.mesh.faces[keep]
    return SolidBody(
        name=f"subtract({a.name},{b.name})",
        mesh=Mesh(a.mesh.vertices.copy(), new_faces),
        parameters={"operation": "subtract", "operands": [a.id, b.id]},
    )


def boolean_intersect(a: SolidBody, b: SolidBody) -> SolidBody:
    """Intersect two solids.

    Simplified: keep faces of *a* whose centroids fall inside *b*'s AABB.
    """
    if a.mesh is None or b.mesh is None:
        raise ValueError("Both solids must have valid meshes")
    bb_min, bb_max = b.mesh.bounding_box()
    keep: List[int] = []
    for i, face in enumerate(a.mesh.faces):
        centroid = a.mesh.vertices[face].mean(axis=0)
        inside = np.all(centroid >= bb_min) and np.all(centroid <= bb_max)
        if inside:
            keep.append(i)
    if not keep:
        return SolidBody(
            name=f"intersect({a.name},{b.name})",
            mesh=Mesh(a.mesh.vertices, np.empty((0, 3), dtype=np.int64)),
            parameters={"operation": "intersect", "operands": [a.id, b.id]},
        )
    new_faces = a.mesh.faces[keep]
    return SolidBody(
        name=f"intersect({a.name},{b.name})",
        mesh=Mesh(a.mesh.vertices.copy(), new_faces),
        parameters={"operation": "intersect", "operands": [a.id, b.id]},
    )
