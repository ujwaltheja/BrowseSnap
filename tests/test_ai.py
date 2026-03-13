"""Unit tests for AI services."""

import pytest

from src.ai.design_suggest import suggest_design
from src.ai.manufacture_check import check_manufacturability
from src.ai.materials import list_materials, get_material, MATERIALS_DB


class TestDesignSuggest:
    def test_ring_suggestions(self):
        result = suggest_design(jewelry_type="ring")
        assert "suggestions" in result
        assert len(result["suggestions"]) > 0
        assert "confidence" in result
        assert "explainability" in result
        assert result["confidence"] > 0.5

    def test_pendant_suggestions(self):
        result = suggest_design(jewelry_type="pendant")
        assert len(result["suggestions"]) > 0
        assert result["confidence"] > 0.5

    def test_unknown_type_has_low_confidence(self):
        result = suggest_design(jewelry_type="tiara")
        assert result["confidence"] <= 0.65

    def test_material_preference_added(self):
        result = suggest_design(jewelry_type="ring", material="platinum")
        for s in result["suggestions"]:
            assert s.get("recommended_material") == "platinum"

    def test_budget_note_added(self):
        result = suggest_design(jewelry_type="ring", budget_range="$500-$1000")
        for s in result["suggestions"]:
            assert "budget_note" in s


class TestManufactureCheck:
    def test_valid_design_passes(self):
        design = {"band_width_mm": 4.0, "band_thickness_mm": 1.5}
        result = check_manufacturability(design)
        assert result["passed"] is True
        assert "confidence" in result
        assert "explainability" in result

    def test_thin_band_fails(self):
        design = {"band_thickness_mm": 0.5}
        result = check_manufacturability(design)
        assert result["passed"] is False
        assert any(i["field"] == "band_thickness_mm" for i in result["issues"])

    def test_low_prong_count_fails(self):
        design = {"setting_type": "prong", "num_prongs": 2}
        result = check_manufacturability(design)
        assert result["passed"] is False

    def test_top_heavy_warning(self):
        design = {"stone_diameter_mm": 10, "band_width_mm": 3}
        result = check_manufacturability(design)
        assert any("top-heavy" in i["message"] for i in result["issues"])

    def test_finger_diameter_out_of_range(self):
        design = {"finger_diameter_mm": 30}
        result = check_manufacturability(design)
        assert any(i["field"] == "finger_diameter_mm" for i in result["issues"])


class TestMaterials:
    def test_list_all(self):
        mats = list_materials()
        assert len(mats) == len(MATERIALS_DB)

    def test_filter_by_category(self):
        golds = list_materials(category="gold")
        assert all(m["category"] == "gold" for m in golds)
        assert len(golds) >= 2

    def test_get_existing_material(self):
        mat = get_material("platinum_950")
        assert mat is not None
        assert mat["name"] == "950 Platinum"

    def test_get_missing_material(self):
        assert get_material("unobtanium") is None
