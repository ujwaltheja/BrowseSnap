"""AI manufacturability-check service."""

from __future__ import annotations

from typing import Any, Dict, List


def check_manufacturability(design: Dict[str, Any]) -> Dict[str, Any]:
    """Analyse a design dict for manufacturing feasibility.

    Returns warnings, pass/fail status, and AI explainability/confidence.
    """
    warnings: List[str] = []
    issues: List[Dict[str, str]] = []
    confidence = 0.90

    band_width = design.get("band_width_mm", 0)
    band_thickness = design.get("band_thickness_mm", 0)
    stone_diameter = design.get("stone_diameter_mm", 0)
    setting_type = design.get("setting_type", "")
    material = design.get("material", "")
    finger_diameter = design.get("finger_diameter_mm", 0)

    # Rule: minimum band thickness for casting
    if band_thickness and band_thickness < 0.8:
        issues.append({
            "field": "band_thickness_mm",
            "severity": "error",
            "message": f"Band thickness {band_thickness} mm is below the 0.8 mm casting minimum.",
        })

    # Rule: minimum band width
    if band_width and band_width < 1.5:
        warnings.append(f"Band width {band_width} mm is very narrow; may be fragile for daily wear.")

    # Rule: stone-to-band ratio
    if stone_diameter and band_width:
        ratio = stone_diameter / band_width
        if ratio > 2.5:
            issues.append({
                "field": "stone_diameter_mm",
                "severity": "warning",
                "message": (
                    f"Stone diameter ({stone_diameter} mm) is {ratio:.1f}× the band width "
                    f"({band_width} mm). The setting may be top-heavy."
                ),
            })

    # Rule: prong count
    num_prongs = design.get("num_prongs", 0)
    if setting_type == "prong" and num_prongs and num_prongs < 3:
        issues.append({
            "field": "num_prongs",
            "severity": "error",
            "message": "At least 3 prongs are required to securely hold a stone.",
        })

    # Rule: material-specific checks
    if material:
        mat_lower = material.lower()
        if mat_lower == "silver" and band_thickness and band_thickness < 1.0:
            warnings.append("Silver is softer; consider at least 1.0 mm thickness.")
        if mat_lower == "platinum" and band_width and band_width > 8.0:
            warnings.append("Wide platinum bands can be very heavy; consider comfort-fit profile.")

    # Rule: finger-size sanity
    if finger_diameter:
        if finger_diameter < 12.0 or finger_diameter > 25.0:
            issues.append({
                "field": "finger_diameter_mm",
                "severity": "warning",
                "message": f"Finger diameter {finger_diameter} mm is outside typical range (12–25 mm).",
            })

    passed = all(i["severity"] != "error" for i in issues)
    if not issues and not warnings:
        confidence = 0.95

    return {
        "passed": passed,
        "issues": issues,
        "warnings": warnings,
        "confidence": round(confidence, 2),
        "explainability": (
            "Manufacturability evaluated using bench-jeweler heuristics covering "
            "minimum wall thickness, stone/band ratio, prong count, and material "
            "suitability. Rules sourced from industry casting and setting guidelines."
        ),
    }
