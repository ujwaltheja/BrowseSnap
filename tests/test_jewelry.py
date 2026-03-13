"""Unit tests for jewelry tools."""

import numpy as np
import pytest

from src.jewelry.ring import RingBuilder
from src.jewelry.stone_setting import StoneSetting, SettingType
from src.jewelry.band import BandProfile, ProfileShape


class TestRingBuilder:
    def test_build_default_ring(self):
        ring = RingBuilder().build()
        assert ring.is_valid
        assert ring.mesh.num_faces > 0

    def test_ring_parameters_stored(self):
        builder = RingBuilder(finger_diameter_mm=18.0, band_width_mm=5.0)
        ring = builder.build()
        assert ring.parameters["finger_diameter_mm"] == 18.0
        assert ring.parameters["band_width_mm"] == 5.0

    def test_ring_with_channel(self):
        builder = RingBuilder(finger_diameter_mm=17.3, band_width_mm=4.0)
        ring = builder.with_channel(channel_width_mm=2.0)
        assert ring.name == "ring_with_channel"


class TestStoneSetting:
    def test_prong_setting(self):
        setting = StoneSetting(stone_diameter_mm=5.0, setting_type=SettingType.PRONG, num_prongs=4)
        body = setting.build()
        assert body.is_valid
        assert body.parameters["setting_type"] == "prong"

    def test_bezel_setting(self):
        setting = StoneSetting(setting_type=SettingType.BEZEL)
        body = setting.build()
        assert body.is_valid

    def test_channel_setting(self):
        setting = StoneSetting(setting_type=SettingType.CHANNEL)
        body = setting.build()
        assert body.is_valid

    def test_pave_setting(self):
        setting = StoneSetting(setting_type=SettingType.PAVE)
        body = setting.build()
        assert body.is_valid

    def test_prong_count_six(self):
        setting = StoneSetting(setting_type=SettingType.PRONG, num_prongs=6)
        body = setting.build()
        assert body.parameters["num_prongs"] == 6


class TestBandProfile:
    def test_flat_profile(self):
        profile = BandProfile(shape=ProfileShape.FLAT)
        curve = profile.generate_curve()
        samples = curve.sample(10)
        assert samples.shape == (10, 3)

    def test_domed_profile(self):
        profile = BandProfile(shape=ProfileShape.DOMED)
        curve = profile.generate_curve()
        assert len(curve.control_points) > 3

    def test_comfort_fit_profile(self):
        profile = BandProfile(shape=ProfileShape.COMFORT_FIT)
        curve = profile.generate_curve()
        assert curve.degree == 3

    def test_knife_edge_profile(self):
        profile = BandProfile(shape=ProfileShape.KNIFE_EDGE)
        curve = profile.generate_curve()
        assert curve.degree >= 2
