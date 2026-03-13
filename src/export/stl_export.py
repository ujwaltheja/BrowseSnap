"""STL (stereolithography) export for 3-D printing and CNC."""

from __future__ import annotations

import struct
from typing import Union

import numpy as np

from src.geometry.solid import SolidBody


def export_stl(body: SolidBody, path: Union[str, None] = None, binary: bool = True) -> Union[bytes, str]:
    """Export a SolidBody to STL format.

    Parameters
    ----------
    body : SolidBody
        The solid to export.
    path : str, optional
        File path to write to.  When *None* the raw content is returned.
    binary : bool
        If *True* (default) produce binary STL; otherwise ASCII STL.

    Returns
    -------
    bytes or str
        The STL content when *path* is None.
    """
    if body.mesh is None:
        raise ValueError("SolidBody has no mesh to export")

    verts = body.mesh.vertices
    faces = body.mesh.faces

    if binary:
        content = _binary_stl(verts, faces)
    else:
        content = _ascii_stl(verts, faces, body.name)

    if path is not None:
        mode = "wb" if binary else "w"
        with open(path, mode) as f:
            f.write(content)

    return content


def _compute_normal(v0: np.ndarray, v1: np.ndarray, v2: np.ndarray) -> np.ndarray:
    n = np.cross(v1 - v0, v2 - v0)
    norm = np.linalg.norm(n)
    if norm < 1e-30:
        return np.zeros(3)
    return n / norm


def _binary_stl(verts: np.ndarray, faces: np.ndarray) -> bytes:
    header = b"\x00" * 80
    num_faces = len(faces)
    buf = bytearray(header)
    buf += struct.pack("<I", num_faces)
    for face in faces:
        v0, v1, v2 = verts[face[0]], verts[face[1]], verts[face[2]]
        normal = _compute_normal(v0, v1, v2)
        buf += struct.pack("<fff", *normal)
        buf += struct.pack("<fff", *v0)
        buf += struct.pack("<fff", *v1)
        buf += struct.pack("<fff", *v2)
        buf += struct.pack("<H", 0)  # attribute byte count
    return bytes(buf)


def _ascii_stl(verts: np.ndarray, faces: np.ndarray, name: str) -> str:
    lines = [f"solid {name}"]
    for face in faces:
        v0, v1, v2 = verts[face[0]], verts[face[1]], verts[face[2]]
        normal = _compute_normal(v0, v1, v2)
        lines.append(f"  facet normal {normal[0]:.6e} {normal[1]:.6e} {normal[2]:.6e}")
        lines.append("    outer loop")
        for v in (v0, v1, v2):
            lines.append(f"      vertex {v[0]:.6e} {v[1]:.6e} {v[2]:.6e}")
        lines.append("    endloop")
        lines.append("  endfacet")
    lines.append(f"endsolid {name}")
    return "\n".join(lines) + "\n"
