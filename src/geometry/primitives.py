"""Geometric primitives used in jewelry modeling."""

from __future__ import annotations

import numpy as np

from src.geometry.solid import Mesh, SolidBody


def create_cylinder(
    radius: float = 1.0,
    height: float = 2.0,
    segments: int = 32,
    name: str = "cylinder",
) -> SolidBody:
    """Generate a cylinder centered at the origin, aligned along the Z axis."""
    angles = np.linspace(0, 2 * np.pi, segments, endpoint=False)
    cos_a = np.cos(angles)
    sin_a = np.sin(angles)

    # Bottom and top circle vertices
    bottom = np.column_stack([radius * cos_a, radius * sin_a, np.full(segments, -height / 2)])
    top = np.column_stack([radius * cos_a, radius * sin_a, np.full(segments, height / 2)])
    center_bot = np.array([[0, 0, -height / 2]])
    center_top = np.array([[0, 0, height / 2]])

    vertices = np.vstack([bottom, top, center_bot, center_top])
    cb_idx = 2 * segments
    ct_idx = 2 * segments + 1

    faces = []
    for i in range(segments):
        j = (i + 1) % segments
        # Side quad as two triangles
        faces.append([i, j, segments + j])
        faces.append([i, segments + j, segments + i])
        # Bottom cap
        faces.append([cb_idx, j, i])
        # Top cap
        faces.append([ct_idx, segments + i, segments + j])

    return SolidBody(
        name=name,
        mesh=Mesh(vertices, np.array(faces)),
        parameters={"radius": radius, "height": height, "segments": segments},
    )


def create_torus(
    major_radius: float = 5.0,
    minor_radius: float = 1.0,
    major_segments: int = 32,
    minor_segments: int = 16,
    name: str = "torus",
) -> SolidBody:
    """Generate a torus lying in the XY plane."""
    vertices = []
    for i in range(major_segments):
        theta = 2 * np.pi * i / major_segments
        for j in range(minor_segments):
            phi = 2 * np.pi * j / minor_segments
            x = (major_radius + minor_radius * np.cos(phi)) * np.cos(theta)
            y = (major_radius + minor_radius * np.cos(phi)) * np.sin(theta)
            z = minor_radius * np.sin(phi)
            vertices.append([x, y, z])
    vertices = np.array(vertices)

    faces = []
    for i in range(major_segments):
        ni = (i + 1) % major_segments
        for j in range(minor_segments):
            nj = (j + 1) % minor_segments
            a = i * minor_segments + j
            b = i * minor_segments + nj
            c = ni * minor_segments + nj
            d = ni * minor_segments + j
            faces.append([a, b, c])
            faces.append([a, c, d])

    return SolidBody(
        name=name,
        mesh=Mesh(vertices, np.array(faces)),
        parameters={
            "major_radius": major_radius,
            "minor_radius": minor_radius,
            "major_segments": major_segments,
            "minor_segments": minor_segments,
        },
    )


def create_sphere(
    radius: float = 1.0,
    segments: int = 16,
    rings: int = 12,
    name: str = "sphere",
) -> SolidBody:
    """Generate a UV sphere centered at the origin."""
    vertices = [[0, 0, radius]]  # north pole
    for i in range(1, rings):
        phi = np.pi * i / rings
        for j in range(segments):
            theta = 2 * np.pi * j / segments
            x = radius * np.sin(phi) * np.cos(theta)
            y = radius * np.sin(phi) * np.sin(theta)
            z = radius * np.cos(phi)
            vertices.append([x, y, z])
    vertices.append([0, 0, -radius])  # south pole
    vertices = np.array(vertices)

    faces = []
    # Top cap
    for j in range(segments):
        nj = (j + 1) % segments
        faces.append([0, 1 + j, 1 + nj])
    # Middle bands
    for i in range(rings - 2):
        for j in range(segments):
            nj = (j + 1) % segments
            a = 1 + i * segments + j
            b = 1 + i * segments + nj
            c = 1 + (i + 1) * segments + nj
            d = 1 + (i + 1) * segments + j
            faces.append([a, b, c])
            faces.append([a, c, d])
    # Bottom cap
    south = len(vertices) - 1
    base = 1 + (rings - 2) * segments
    for j in range(segments):
        nj = (j + 1) % segments
        faces.append([south, base + nj, base + j])

    return SolidBody(
        name=name,
        mesh=Mesh(vertices, np.array(faces)),
        parameters={"radius": radius, "segments": segments, "rings": rings},
    )


def create_cone(
    radius: float = 1.0,
    height: float = 2.0,
    segments: int = 32,
    name: str = "cone",
) -> SolidBody:
    """Generate a cone with apex at top, base at bottom, along Z axis."""
    angles = np.linspace(0, 2 * np.pi, segments, endpoint=False)
    base = np.column_stack([radius * np.cos(angles), radius * np.sin(angles), np.full(segments, -height / 2)])
    apex = np.array([[0, 0, height / 2]])
    center = np.array([[0, 0, -height / 2]])
    vertices = np.vstack([base, apex, center])
    apex_idx = segments
    center_idx = segments + 1

    faces = []
    for i in range(segments):
        j = (i + 1) % segments
        faces.append([i, j, apex_idx])  # Side
        faces.append([center_idx, j, i])  # Base

    return SolidBody(
        name=name,
        mesh=Mesh(vertices, np.array(faces)),
        parameters={"radius": radius, "height": height, "segments": segments},
    )
