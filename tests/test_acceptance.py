"""Five bench-jeweler acceptance scenarios.

Each scenario simulates a realistic end-to-end workflow that a bench
jeweler would perform using the Jewelry CAD system.
"""

import numpy as np
import pytest

from src.jewelry.ring import RingBuilder
from src.jewelry.stone_setting import StoneSetting, SettingType
from src.jewelry.band import BandProfile, ProfileShape
from src.geometry.transforms import translate, rotate
from src.geometry.solid import boolean_union
from src.renderer.renderer import Scene, RenderSettings, render_scene
from src.export.stl_export import export_stl
from src.export.step_export import export_step
from src.ai.design_suggest import suggest_design
from src.ai.manufacture_check import check_manufacturability


class TestScenario1_EngagementRingDesignToExport:
    """Scenario 1: A jeweler designs a custom engagement ring and exports for casting.

    Steps:
    1. Build a comfort-fit ring for US size 7 (17.3 mm diameter)
    2. Add a 4-prong solitaire setting for a 6.5 mm round diamond
    3. Run manufacturability check
    4. Export as STL for 3D printing the wax
    """

    def test_full_workflow(self):
        # 1. Build ring
        ring = RingBuilder(
            finger_diameter_mm=17.3,
            band_width_mm=3.0,
            band_thickness_mm=1.5,
        ).build()
        assert ring.is_valid

        # 2. Add setting
        setting = StoneSetting(
            stone_diameter_mm=6.5,
            setting_type=SettingType.PRONG,
            num_prongs=4,
        ).build()
        assert setting.is_valid

        # 3. Check manufacturability
        result = check_manufacturability({
            "band_width_mm": 3.0,
            "band_thickness_mm": 1.5,
            "stone_diameter_mm": 6.5,
            "setting_type": "prong",
            "num_prongs": 4,
            "material": "gold",
        })
        assert result["passed"] is True
        assert result["confidence"] >= 0.85

        # 4. Export
        stl_data = export_stl(ring, binary=True)
        assert len(stl_data) > 0


class TestScenario2_WeddingBandWithChannelAndRender:
    """Scenario 2: A jeweler creates a wedding band with a channel, renders it, and exports STEP.

    Steps:
    1. Build a flat-profile wedding band
    2. Cut a decorative channel
    3. Render the scene
    4. Export as STEP for CNC milling
    """

    def test_full_workflow(self):
        # 1. Build band
        builder = RingBuilder(
            finger_diameter_mm=19.0,
            band_width_mm=6.0,
            band_thickness_mm=2.0,
        )

        # 2. Channel cut
        ring = builder.with_channel(channel_width_mm=2.0, channel_depth_mm=0.5)
        assert ring.name == "ring_with_channel"

        # 3. Render scene
        scene = Scene()
        scene.add(ring)
        render_data = render_scene(scene)
        assert render_data["total_faces"] > 0
        assert render_data["bounding_box"]["min"] is not None

        # 4. Export STEP
        step_text = export_step(ring)
        assert "ISO-10303-21;" in step_text
        assert "MANIFOLD_SOLID_BREP" in step_text


class TestScenario3_AIAssistedDesignSuggestion:
    """Scenario 3: A jeweler uses AI to get design suggestions and validates them.

    Steps:
    1. Request AI suggestions for a pendant
    2. Select a suggestion and build its geometry
    3. Check manufacturability of the AI suggestion
    4. Verify explainability and confidence are present
    """

    def test_full_workflow(self):
        # 1. AI suggestion
        suggestions = suggest_design(
            jewelry_type="pendant",
            material="platinum",
            budget_range="$1000-$3000",
        )
        assert len(suggestions["suggestions"]) > 0
        assert "explainability" in suggestions
        assert "confidence" in suggestions
        assert suggestions["confidence"] > 0.5

        # 2. Pick a suggestion and build setting
        first = suggestions["suggestions"][0]
        params = first.get("parameters", {})
        setting = StoneSetting(
            stone_diameter_mm=params.get("stone_diameter_mm", 5.0),
            setting_type=SettingType(params.get("setting_type", "bezel")),
        )
        body = setting.build()
        assert body.is_valid

        # 3. Manufacture check
        check = check_manufacturability({
            "stone_diameter_mm": params.get("stone_diameter_mm", 5.0),
            "setting_type": params.get("setting_type", "bezel"),
            "material": "platinum",
        })
        assert "explainability" in check
        assert "confidence" in check


class TestScenario4_MultiStoneEternityBand:
    """Scenario 4: Design an eternity band with multiple pavé stones.

    Steps:
    1. Build ring body
    2. Create multiple pavé settings around the band
    3. Combine all settings with union
    4. Export as binary STL
    """

    def test_full_workflow(self):
        # 1. Build ring
        ring = RingBuilder(
            finger_diameter_mm=17.3,
            band_width_mm=2.5,
            band_thickness_mm=1.2,
        ).build()
        assert ring.is_valid

        # 2 & 3. Create and union multiple settings
        combined = ring
        num_stones = 4
        for i in range(num_stones):
            setting = StoneSetting(
                stone_diameter_mm=1.5,
                setting_type=SettingType.PAVE,
            ).build()
            angle = 360 * i / num_stones
            setting = rotate(setting, (0, 0, 1), angle)
            r = ring.parameters.get("major_radius", 9.0)
            setting = translate(setting, (r, 0, 0))
            setting = rotate(setting, (0, 0, 1), angle)
            combined = boolean_union(combined, setting)

        assert combined.is_valid
        assert combined.mesh.num_faces > ring.mesh.num_faces

        # 4. Export STL
        stl_data = export_stl(combined, binary=True)
        assert len(stl_data) > 100


class TestScenario5_MaterialSelectionAndCostEstimate:
    """Scenario 5: A jeweler selects materials, checks compatibility, and validates the design.

    Steps:
    1. Browse available materials
    2. Filter by gold category
    3. Design a ring in 18K rose gold
    4. Run manufacture check with material
    5. Verify volume calculation for weight estimation
    """

    def test_full_workflow(self):
        from src.ai.materials import list_materials, get_material

        # 1. Browse all materials
        all_mats = list_materials()
        assert len(all_mats) >= 5

        # 2. Filter gold
        golds = list_materials(category="gold")
        assert len(golds) >= 2

        # 3. Select 18K rose gold
        rose = get_material("gold_18k_rose")
        assert rose is not None
        assert rose["density_g_cm3"] > 0

        # 4. Build ring and check
        ring = RingBuilder(
            finger_diameter_mm=16.5,
            band_width_mm=3.5,
            band_thickness_mm=1.5,
        ).build()

        check = check_manufacturability({
            "band_width_mm": 3.5,
            "band_thickness_mm": 1.5,
            "material": "gold",
            "finger_diameter_mm": 16.5,
        })
        assert check["passed"] is True

        # 5. Volume for weight estimation
        vol_mm3 = ring.volume()
        assert vol_mm3 > 0
        # Weight estimate: volume (mm³) → cm³ × density
        vol_cm3 = vol_mm3 / 1000.0
        weight_g = vol_cm3 * rose["density_g_cm3"]
        assert weight_g >= 0  # non-negative
