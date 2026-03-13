"""Unit tests for STEP/STL export."""

import struct

import numpy as np
import pytest

from src.geometry.primitives import create_cylinder, create_torus
from src.jewelry.ring import RingBuilder
from src.export.stl_export import export_stl
from src.export.step_export import export_step


class TestSTLExport:
    def test_binary_stl_header(self):
        cyl = create_cylinder()
        data = export_stl(cyl, binary=True)
        assert isinstance(data, bytes)
        assert len(data) > 84  # 80-byte header + 4-byte count

    def test_binary_stl_face_count(self):
        cyl = create_cylinder(segments=8)
        data = export_stl(cyl, binary=True)
        num_faces = struct.unpack_from("<I", data, 80)[0]
        assert num_faces == cyl.mesh.num_faces

    def test_ascii_stl_format(self):
        cyl = create_cylinder(segments=8)
        text = export_stl(cyl, binary=False)
        assert isinstance(text, str)
        assert text.startswith("solid cylinder")
        assert text.strip().endswith("endsolid cylinder")

    def test_export_ring_to_stl(self):
        ring = RingBuilder().build()
        data = export_stl(ring, binary=True)
        assert len(data) > 0

    def test_export_to_file(self, tmp_path):
        cyl = create_cylinder()
        path = str(tmp_path / "test.stl")
        export_stl(cyl, path=path)
        with open(path, "rb") as f:
            header = f.read(80)
        assert len(header) == 80


class TestSTEPExport:
    def test_step_contains_header(self):
        cyl = create_cylinder()
        text = export_step(cyl)
        assert "ISO-10303-21;" in text
        assert "END-ISO-10303-21;" in text

    def test_step_contains_cartesian_points(self):
        cyl = create_cylinder(segments=8)
        text = export_step(cyl)
        assert "CARTESIAN_POINT" in text

    def test_step_contains_solid_brep(self):
        cyl = create_cylinder()
        text = export_step(cyl)
        assert "MANIFOLD_SOLID_BREP" in text

    def test_export_ring_to_step(self):
        ring = RingBuilder().build()
        text = export_step(ring)
        assert "CLOSED_SHELL" in text

    def test_export_to_file(self, tmp_path):
        cyl = create_cylinder()
        path = str(tmp_path / "test.step")
        export_step(cyl, path=path)
        with open(path) as f:
            content = f.read()
        assert "ISO-10303-21;" in content
