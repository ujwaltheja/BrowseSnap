"""Unit tests for the Geometry Kernel."""

import math

import numpy as np
import pytest

from src.geometry.nurbs import NURBSCurve, NURBSSurface
from src.geometry.solid import Mesh, SolidBody, boolean_union, boolean_subtract, boolean_intersect
from src.geometry.primitives import create_cylinder, create_torus, create_sphere, create_cone
from src.geometry.transforms import translate, rotate, scale, mirror


# ======================================================================
# NURBS Curve tests
# ======================================================================

class TestNURBSCurve:
    def test_create_linear_curve(self):
        pts = np.array([[0, 0, 0], [1, 0, 0]])
        curve = NURBSCurve(control_points=pts, degree=1)
        assert len(curve.control_points) == 2
        assert curve.degree == 1

    def test_evaluate_endpoints(self):
        pts = np.array([[0, 0, 0], [5, 0, 0], [10, 0, 0], [10, 5, 0]])
        curve = NURBSCurve(control_points=pts, degree=3)
        start = curve.evaluate(0.0)
        end = curve.evaluate(1.0)
        np.testing.assert_allclose(start, [0, 0, 0], atol=1e-10)
        np.testing.assert_allclose(end, [10, 5, 0], atol=1e-10)

    def test_evaluate_midpoint_is_between(self):
        pts = np.array([[0, 0, 0], [5, 5, 0], [10, 0, 0]])
        curve = NURBSCurve(control_points=pts, degree=2)
        mid = curve.evaluate(0.5)
        assert 0 < mid[0] < 10
        assert mid[1] > 0  # above baseline

    def test_sample_returns_correct_count(self):
        pts = np.array([[0, 0, 0], [1, 1, 0], [2, 0, 0], [3, 1, 0]])
        curve = NURBSCurve(control_points=pts, degree=3)
        samples = curve.sample(num_points=20)
        assert samples.shape == (20, 3)

    def test_weighted_curve_differs_from_unweighted(self):
        pts = np.array([[0, 0, 0], [5, 5, 0], [10, 0, 0]])
        unweighted = NURBSCurve(control_points=pts, degree=2)
        weighted = NURBSCurve(control_points=pts, weights=np.array([1.0, 5.0, 1.0]), degree=2)
        mid_u = unweighted.evaluate(0.5)
        mid_w = weighted.evaluate(0.5)
        # The weighted midpoint should be pulled towards the high-weight control point
        assert mid_w[1] > mid_u[1]

    def test_invalid_control_points_raises(self):
        with pytest.raises(ValueError):
            NURBSCurve(control_points=np.array([[1, 2]]), degree=1)  # shape (1,2) != (n,3)


# ======================================================================
# NURBS Surface tests
# ======================================================================

class TestNURBSSurface:
    def test_create_surface(self):
        net = np.zeros((4, 4, 3))
        for i in range(4):
            for j in range(4):
                net[i, j] = [i, j, 0]
        surf = NURBSSurface(control_net=net, degree_u=3, degree_v=3)
        assert surf.control_net.shape == (4, 4, 3)

    def test_evaluate_corner(self):
        net = np.zeros((4, 4, 3))
        for i in range(4):
            for j in range(4):
                net[i, j] = [i, j, 0]
        surf = NURBSSurface(control_net=net, degree_u=3, degree_v=3)
        corner = surf.evaluate(0.0, 0.0)
        np.testing.assert_allclose(corner, [0, 0, 0], atol=1e-8)


# ======================================================================
# Mesh / SolidBody tests
# ======================================================================

class TestMeshAndSolid:
    def _simple_cube_mesh(self):
        """2-triangle quad on each of 6 faces → 12 triangles."""
        v = np.array([
            [0, 0, 0], [1, 0, 0], [1, 1, 0], [0, 1, 0],
            [0, 0, 1], [1, 0, 1], [1, 1, 1], [0, 1, 1],
        ], dtype=float)
        f = np.array([
            [0, 1, 2], [0, 2, 3],  # bottom
            [4, 6, 5], [4, 7, 6],  # top
            [0, 4, 5], [0, 5, 1],  # front
            [2, 6, 7], [2, 7, 3],  # back
            [0, 3, 7], [0, 7, 4],  # left
            [1, 5, 6], [1, 6, 2],  # right
        ])
        return Mesh(v, f)

    def test_mesh_bounding_box(self):
        mesh = self._simple_cube_mesh()
        lo, hi = mesh.bounding_box()
        np.testing.assert_array_equal(lo, [0, 0, 0])
        np.testing.assert_array_equal(hi, [1, 1, 1])

    def test_mesh_volume(self):
        mesh = self._simple_cube_mesh()
        vol = mesh.volume()
        assert abs(vol - 1.0) < 0.01

    def test_solid_body_valid(self):
        body = SolidBody(mesh=self._simple_cube_mesh())
        assert body.is_valid

    def test_solid_body_invalid_without_mesh(self):
        body = SolidBody()
        assert not body.is_valid

    def test_boolean_union_vertex_count(self):
        m1 = self._simple_cube_mesh()
        m2 = m1.translate(np.array([2, 0, 0]))
        a = SolidBody(name="a", mesh=m1)
        b = SolidBody(name="b", mesh=m2)
        result = boolean_union(a, b)
        assert result.mesh.num_vertices == 16
        assert result.mesh.num_faces == 24

    def test_boolean_subtract(self):
        m1 = self._simple_cube_mesh()
        m2 = m1.translate(np.array([0.5, 0.5, 0.5]))
        a = SolidBody(name="a", mesh=m1)
        b = SolidBody(name="b", mesh=m2)
        result = boolean_subtract(a, b)
        # Some faces should be removed
        assert result.mesh.num_faces <= m1.num_faces

    def test_boolean_intersect(self):
        m1 = self._simple_cube_mesh()
        m2 = m1.translate(np.array([0.5, 0.5, 0.5]))
        a = SolidBody(name="a", mesh=m1)
        b = SolidBody(name="b", mesh=m2)
        result = boolean_intersect(a, b)
        assert result.mesh is not None


# ======================================================================
# Primitives
# ======================================================================

class TestPrimitives:
    def test_cylinder_has_faces(self):
        cyl = create_cylinder(radius=2, height=5)
        assert cyl.is_valid
        assert cyl.mesh.num_faces > 0

    def test_torus_has_faces(self):
        torus = create_torus(major_radius=5, minor_radius=1)
        assert torus.is_valid
        assert torus.mesh.num_faces > 0

    def test_sphere_has_faces(self):
        sphere = create_sphere(radius=3)
        assert sphere.is_valid
        assert sphere.mesh.num_faces > 0

    def test_cone_has_faces(self):
        cone = create_cone(radius=2, height=4)
        assert cone.is_valid
        assert cone.mesh.num_faces > 0

    def test_torus_bounding_box(self):
        torus = create_torus(major_radius=5, minor_radius=1)
        lo, hi = torus.bounding_box()
        assert lo[0] < -5 and hi[0] > 5  # spans beyond major radius


# ======================================================================
# Transforms
# ======================================================================

class TestTransforms:
    def test_translate(self):
        cyl = create_cylinder(radius=1, height=2)
        moved = translate(cyl, (10, 0, 0))
        lo, _ = moved.bounding_box()
        assert lo[0] > 8  # shifted right

    def test_rotate_90_deg_z(self):
        cyl = create_cylinder(radius=1, height=2)
        rotated = rotate(cyl, (0, 0, 1), 90)
        # After 90° rotation around Z, bounding box should be roughly the same size
        lo, hi = rotated.bounding_box()
        assert hi[0] - lo[0] > 1  # still has extent

    def test_scale(self):
        cyl = create_cylinder(radius=1, height=2)
        scaled = scale(cyl, (2, 2, 2))
        lo, hi = scaled.bounding_box()
        extent = hi - lo
        # Scaled by 2 in each axis
        assert extent[2] > 3.5  # original height=2, scaled=4

    def test_mirror_x(self):
        cyl = create_cylinder(radius=1, height=2)
        moved = translate(cyl, (5, 0, 0))
        mirrored = mirror(moved, (1, 0, 0))
        lo, _ = mirrored.bounding_box()
        assert lo[0] < -3  # mirrored to negative x
