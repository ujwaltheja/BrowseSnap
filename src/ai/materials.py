"""Materials database for jewelry CAD."""

from __future__ import annotations

from typing import Any, Dict, List, Optional


MATERIALS_DB: List[Dict[str, Any]] = [
    {
        "id": "gold_18k_yellow",
        "name": "18K Yellow Gold",
        "category": "gold",
        "purity": "75%",
        "density_g_cm3": 15.6,
        "melting_point_c": 1064,
        "hardness_vickers": 170,
        "color_hex": "#FFD700",
        "suitable_for": ["rings", "pendants", "earrings", "bracelets"],
    },
    {
        "id": "gold_14k_white",
        "name": "14K White Gold",
        "category": "gold",
        "purity": "58.3%",
        "density_g_cm3": 14.0,
        "melting_point_c": 1064,
        "hardness_vickers": 155,
        "color_hex": "#E8E8E8",
        "suitable_for": ["rings", "pendants", "earrings"],
    },
    {
        "id": "platinum_950",
        "name": "950 Platinum",
        "category": "platinum",
        "purity": "95%",
        "density_g_cm3": 21.4,
        "melting_point_c": 1768,
        "hardness_vickers": 110,
        "color_hex": "#E5E4E2",
        "suitable_for": ["rings", "pendants"],
    },
    {
        "id": "silver_925",
        "name": "925 Sterling Silver",
        "category": "silver",
        "purity": "92.5%",
        "density_g_cm3": 10.4,
        "melting_point_c": 961,
        "hardness_vickers": 80,
        "color_hex": "#C0C0C0",
        "suitable_for": ["rings", "pendants", "earrings", "bracelets"],
    },
    {
        "id": "gold_18k_rose",
        "name": "18K Rose Gold",
        "category": "gold",
        "purity": "75%",
        "density_g_cm3": 15.1,
        "melting_point_c": 1064,
        "hardness_vickers": 160,
        "color_hex": "#B76E79",
        "suitable_for": ["rings", "pendants", "earrings"],
    },
    {
        "id": "titanium_grade5",
        "name": "Grade 5 Titanium (Ti-6Al-4V)",
        "category": "titanium",
        "purity": "90%",
        "density_g_cm3": 4.43,
        "melting_point_c": 1660,
        "hardness_vickers": 349,
        "color_hex": "#878681",
        "suitable_for": ["rings", "bracelets"],
    },
]


def list_materials(category: Optional[str] = None) -> List[Dict[str, Any]]:
    """Return materials, optionally filtered by *category*."""
    if category:
        cat = category.lower()
        return [m for m in MATERIALS_DB if m["category"] == cat]
    return list(MATERIALS_DB)


def get_material(material_id: str) -> Optional[Dict[str, Any]]:
    """Look up a single material by its ID."""
    for m in MATERIALS_DB:
        if m["id"] == material_id:
            return m
    return None
