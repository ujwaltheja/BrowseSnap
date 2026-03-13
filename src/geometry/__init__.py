"""Geometry Kernel — parametric NURBS curves/surfaces and solid modeling."""

from src.geometry.nurbs import NURBSCurve, NURBSSurface
from src.geometry.solid import SolidBody, boolean_union, boolean_subtract, boolean_intersect
from src.geometry.primitives import (
    create_cylinder,
    create_torus,
    create_sphere,
    create_cone,
)
from src.geometry.transforms import translate, rotate, scale, mirror

__all__ = [
    "NURBSCurve",
    "NURBSSurface",
    "SolidBody",
    "boolean_union",
    "boolean_subtract",
    "boolean_intersect",
    "create_cylinder",
    "create_torus",
    "create_sphere",
    "create_cone",
    "translate",
    "rotate",
    "scale",
    "mirror",
]
