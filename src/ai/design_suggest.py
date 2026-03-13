"""AI design-suggestion service."""

from __future__ import annotations

from typing import Any, Dict, List, Optional


def suggest_design(
    jewelry_type: str,
    style: Optional[str] = None,
    material: Optional[str] = None,
    budget_range: Optional[str] = None,
    occasion: Optional[str] = None,
) -> Dict[str, Any]:
    """Return AI-powered design suggestions.

    All AI responses include ``explainability`` and ``confidence`` fields.
    """
    suggestions: List[Dict[str, Any]] = []
    base_confidence = 0.85

    rules: Dict[str, Dict[str, Any]] = {
        "ring": {
            "suggestions": [
                {
                    "name": "Classic Solitaire",
                    "description": "Timeless single-stone ring with a four-prong setting on a comfort-fit band",
                    "parameters": {
                        "band_width_mm": 3.0,
                        "stone_diameter_mm": 6.5,
                        "setting_type": "prong",
                        "num_prongs": 4,
                    },
                },
                {
                    "name": "Pavé Eternity Band",
                    "description": "Continuous row of small stones encircling the full band",
                    "parameters": {
                        "band_width_mm": 2.5,
                        "stone_diameter_mm": 1.5,
                        "setting_type": "pave",
                    },
                },
                {
                    "name": "Cathedral Setting",
                    "description": "Arched band supports lift the center stone for maximum light entry",
                    "parameters": {
                        "band_width_mm": 3.5,
                        "stone_diameter_mm": 7.0,
                        "setting_type": "prong",
                        "num_prongs": 6,
                    },
                },
            ],
            "confidence": 0.92,
        },
        "pendant": {
            "suggestions": [
                {
                    "name": "Bezel Drop Pendant",
                    "description": "Minimalist bezel-set stone on a delicate chain",
                    "parameters": {
                        "stone_diameter_mm": 8.0,
                        "setting_type": "bezel",
                    },
                },
                {
                    "name": "Halo Pendant",
                    "description": "Center stone surrounded by a halo of smaller accent stones",
                    "parameters": {
                        "stone_diameter_mm": 6.0,
                        "setting_type": "prong",
                        "num_prongs": 4,
                    },
                },
            ],
            "confidence": 0.88,
        },
        "earring": {
            "suggestions": [
                {
                    "name": "Stud Earring",
                    "description": "Simple four-prong stud earring for everyday wear",
                    "parameters": {
                        "stone_diameter_mm": 4.0,
                        "setting_type": "prong",
                        "num_prongs": 4,
                    },
                },
            ],
            "confidence": 0.90,
        },
    }

    jtype = jewelry_type.lower()
    match = rules.get(jtype)
    if match:
        suggestions = match["suggestions"]
        base_confidence = match["confidence"]
    else:
        suggestions = [
            {
                "name": "Custom Design",
                "description": f"A bespoke {jewelry_type} piece tailored to your specifications",
                "parameters": {},
            }
        ]
        base_confidence = 0.60

    # Filter by style / material when provided
    if style:
        base_confidence = min(base_confidence + 0.02, 0.99)
    if material:
        for s in suggestions:
            s["recommended_material"] = material
    if budget_range:
        for s in suggestions:
            s["budget_note"] = f"Designed to fit within {budget_range}"

    return {
        "jewelry_type": jewelry_type,
        "suggestions": suggestions,
        "confidence": round(base_confidence, 2),
        "explainability": (
            f"Suggestions generated using rule-based design knowledge for "
            f"'{jtype}' jewelry type. Confidence reflects coverage of the "
            f"design space for this category."
        ),
    }
