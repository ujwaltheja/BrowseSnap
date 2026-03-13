"""Simple renderer producing a wireframe/depth-buffer representation."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Dict, List, Optional

import numpy as np

from src.geometry.solid import SolidBody


@dataclass
class RenderSettings:
    """Settings for rendering a scene."""

    width: int = 800
    height: int = 600
    background_color: tuple[int, int, int] = (30, 30, 30)
    camera_position: tuple[float, float, float] = (0.0, -30.0, 15.0)
    camera_target: tuple[float, float, float] = (0.0, 0.0, 0.0)
    light_direction: tuple[float, float, float] = (1.0, -1.0, 2.0)


@dataclass
class Scene:
    """A collection of solid bodies to render."""

    bodies: List[SolidBody] = field(default_factory=list)
    settings: RenderSettings = field(default_factory=RenderSettings)

    def add(self, body: SolidBody) -> None:
        self.bodies.append(body)


def render_scene(scene: Scene) -> Dict:
    """Render the scene and return metadata (vertex/face counts, bounding box).

    A production renderer would rasterize to an image buffer; this
    implementation returns a scene description suitable for a WebGL
    front-end.
    """
    all_verts = 0
    all_faces = 0
    bb_min: Optional[np.ndarray] = None
    bb_max: Optional[np.ndarray] = None

    objects = []
    for body in scene.bodies:
        if body.mesh is None:
            continue
        all_verts += body.mesh.num_vertices
        all_faces += body.mesh.num_faces
        lo, hi = body.mesh.bounding_box()
        if bb_min is None:
            bb_min, bb_max = lo.copy(), hi.copy()
        else:
            bb_min = np.minimum(bb_min, lo)
            bb_max = np.maximum(bb_max, hi)
        objects.append({
            "id": body.id,
            "name": body.name,
            "vertices": body.mesh.num_vertices,
            "faces": body.mesh.num_faces,
        })

    return {
        "total_vertices": all_verts,
        "total_faces": all_faces,
        "bounding_box": {
            "min": bb_min.tolist() if bb_min is not None else None,
            "max": bb_max.tolist() if bb_max is not None else None,
        },
        "objects": objects,
        "settings": {
            "width": scene.settings.width,
            "height": scene.settings.height,
            "camera_position": list(scene.settings.camera_position),
        },
    }
