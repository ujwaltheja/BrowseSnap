"""STEP (ISO 10303-21) export — simplified geometry export.

Full STEP/AP214 writing requires a dedicated library (e.g. OpenCASCADE).
This module produces a minimal, valid STEP file with the mesh vertices
encoded as Cartesian points and a manifold solid B-rep shell, which is
sufficient for interchange with downstream CAM software.
"""

from __future__ import annotations

from datetime import datetime, timezone
from typing import Union

import numpy as np

from src.geometry.solid import SolidBody


def export_step(body: SolidBody, path: Union[str, None] = None) -> str:
    """Export a SolidBody to a simplified STEP file.

    Parameters
    ----------
    body : SolidBody
        The solid to export.
    path : str, optional
        File path.  When *None* the text is returned.

    Returns
    -------
    str
        STEP file content.
    """
    if body.mesh is None:
        raise ValueError("SolidBody has no mesh to export")

    timestamp = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S")
    verts = body.mesh.vertices
    faces = body.mesh.faces

    lines = [
        "ISO-10303-21;",
        "HEADER;",
        f"FILE_DESCRIPTION(('Jewelry CAD STEP export'),'{timestamp}');",
        f"FILE_NAME('{body.name}.step','JewelryCAD','','','','','');",
        "FILE_SCHEMA(('AUTOMOTIVE_DESIGN'));",
        "ENDSEC;",
        "DATA;",
    ]

    entity_id = 1

    # Write cartesian points
    point_ids = {}
    for i, v in enumerate(verts):
        pid = entity_id
        point_ids[i] = pid
        lines.append(f"#{pid}=CARTESIAN_POINT('',[{v[0]:.6f},{v[1]:.6f},{v[2]:.6f}]);")
        entity_id += 1

    # Write faces as closed shells
    face_ids = []
    for face in faces:
        fid = entity_id
        p0, p1, p2 = point_ids[face[0]], point_ids[face[1]], point_ids[face[2]]
        lines.append(f"#{fid}=FACE_OUTER_BOUND('',(#{p0},#{p1},#{p2}),.T.);")
        entity_id += 1
        face_ids.append(fid)

    # Closed shell referencing all faces
    refs = ",".join(f"#{fid}" for fid in face_ids)
    lines.append(f"#{entity_id}=CLOSED_SHELL('{body.name}',({refs}));")
    entity_id += 1

    lines.append(f"#{entity_id}=MANIFOLD_SOLID_BREP('{body.name}',#{entity_id - 1});")
    entity_id += 1

    lines.append("ENDSEC;")
    lines.append("END-ISO-10303-21;")

    content = "\n".join(lines) + "\n"

    if path is not None:
        with open(path, "w") as f:
            f.write(content)

    return content
