"""Stone-setting tools for jewelry CAD."""

from __future__ import annotations

from dataclasses import dataclass
from enum import Enum

from src.geometry.primitives import create_cylinder, create_cone
from src.geometry.solid import SolidBody, boolean_union
from src.geometry.transforms import translate


class SettingType(str, Enum):
    PRONG = "prong"
    BEZEL = "bezel"
    CHANNEL = "channel"
    PAVE = "pave"


@dataclass
class StoneSetting:
    """Create a stone-setting geometry.

    Parameters
    ----------
    stone_diameter_mm : float
        Diameter of the stone.
    stone_depth_mm : float
        Depth (height) of the stone seat.
    setting_type : SettingType
        The type of setting to generate.
    num_prongs : int
        Number of prongs (only used for PRONG setting).
    """

    stone_diameter_mm: float = 5.0
    stone_depth_mm: float = 3.0
    setting_type: SettingType = SettingType.PRONG
    num_prongs: int = 4

    def build(self) -> SolidBody:
        """Build the setting geometry based on the configured type."""
        if self.setting_type == SettingType.PRONG:
            return self._build_prong()
        elif self.setting_type == SettingType.BEZEL:
            return self._build_bezel()
        elif self.setting_type == SettingType.CHANNEL:
            return self._build_channel()
        else:
            return self._build_pave()

    def _build_prong(self) -> SolidBody:
        """Generate a prong setting with individual prong posts."""
        import numpy as np

        r = self.stone_diameter_mm / 2.0
        prong_r = 0.3  # prong wire radius
        prong_h = self.stone_depth_mm + 1.0

        result = None
        for i in range(self.num_prongs):
            angle = 2 * np.pi * i / self.num_prongs
            x = r * np.cos(angle)
            y = r * np.sin(angle)
            prong = create_cylinder(radius=prong_r, height=prong_h, segments=8, name=f"prong_{i}")
            prong = translate(prong, (x, y, prong_h / 2))
            if result is None:
                result = prong
            else:
                result = boolean_union(result, prong)

        result.name = "prong_setting"
        result.parameters.update({
            "stone_diameter_mm": self.stone_diameter_mm,
            "setting_type": self.setting_type.value,
            "num_prongs": self.num_prongs,
        })
        return result

    def _build_bezel(self) -> SolidBody:
        """Generate a bezel (tube) setting."""
        outer_r = self.stone_diameter_mm / 2.0 + 0.5
        bezel = create_cylinder(radius=outer_r, height=self.stone_depth_mm, name="bezel_setting")
        bezel.parameters.update({
            "stone_diameter_mm": self.stone_diameter_mm,
            "setting_type": self.setting_type.value,
        })
        return bezel

    def _build_channel(self) -> SolidBody:
        """Generate a channel (rail) setting."""
        outer_r = self.stone_diameter_mm / 2.0 + 0.8
        channel = create_cylinder(radius=outer_r, height=self.stone_depth_mm, segments=4, name="channel_setting")
        channel.parameters.update({
            "stone_diameter_mm": self.stone_diameter_mm,
            "setting_type": self.setting_type.value,
        })
        return channel

    def _build_pave(self) -> SolidBody:
        """Generate a pavé bead setting."""
        bead_r = 0.25
        bead = create_cylinder(radius=bead_r, height=self.stone_depth_mm * 0.8, segments=8, name="pave_setting")
        bead.parameters.update({
            "stone_diameter_mm": self.stone_diameter_mm,
            "setting_type": self.setting_type.value,
        })
        return bead
