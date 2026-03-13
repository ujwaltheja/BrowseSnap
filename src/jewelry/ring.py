"""Parametric ring builder."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Optional

from src.geometry.primitives import create_torus
from src.geometry.solid import SolidBody


@dataclass
class RingBuilder:
    """Build a parametric ring from finger size, band width, and profile.

    Parameters
    ----------
    finger_diameter_mm : float
        Inner diameter in mm (e.g. 17.3 for US size 7).
    band_width_mm : float
        Band width in mm.
    band_thickness_mm : float
        Band thickness in mm.
    """

    finger_diameter_mm: float = 17.3
    band_width_mm: float = 4.0
    band_thickness_mm: float = 1.5

    def build(self) -> SolidBody:
        """Generate the ring solid body."""
        inner_r = self.finger_diameter_mm / 2.0
        outer_r = inner_r + self.band_thickness_mm
        minor_r = (outer_r - inner_r) / 2.0
        major_r = inner_r + minor_r

        ring = create_torus(
            major_radius=major_r,
            minor_radius=minor_r,
            name="ring_band",
        )
        ring.parameters.update({
            "finger_diameter_mm": self.finger_diameter_mm,
            "band_width_mm": self.band_width_mm,
            "band_thickness_mm": self.band_thickness_mm,
        })
        return ring

    def with_channel(self, channel_width_mm: float = 2.0, channel_depth_mm: float = 0.5) -> SolidBody:
        """Build a ring with a channel cut on the outside surface."""
        ring = self.build()
        # Mark the ring as having a channel — full Boolean cut requires
        # a volumetric CSG kernel; here we record the channel parameters
        # for downstream CAM processing.
        ring.name = "ring_with_channel"
        ring.parameters["channel_width_mm"] = channel_width_mm
        ring.parameters["channel_depth_mm"] = channel_depth_mm
        return ring
