"""AI services for design suggestion, manufacturability checking, and materials."""

from src.ai.design_suggest import suggest_design
from src.ai.manufacture_check import check_manufacturability
from src.ai.materials import MATERIALS_DB, get_material, list_materials

__all__ = [
    "suggest_design",
    "check_manufacturability",
    "MATERIALS_DB",
    "get_material",
    "list_materials",
]
