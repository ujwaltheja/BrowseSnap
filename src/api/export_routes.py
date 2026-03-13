"""Export API endpoints — generate STL/STEP from parametric descriptions."""

from __future__ import annotations

from typing import Any, Dict, Optional

from fastapi import APIRouter, HTTPException
from fastapi.responses import Response
from pydantic import BaseModel, Field

from src.geometry.primitives import create_cylinder, create_torus, create_sphere
from src.jewelry.ring import RingBuilder
from src.export.stl_export import export_stl
from src.export.step_export import export_step

router = APIRouter(prefix="/export", tags=["Export / CAM"])


class ExportRequest(BaseModel):
    format: str = Field("stl", description="Export format: stl or step")
    jewelry_type: str = Field("ring", description="Jewelry type to export")
    finger_diameter_mm: Optional[float] = Field(17.3, description="Finger diameter in mm")
    band_width_mm: Optional[float] = Field(4.0, description="Band width in mm")
    band_thickness_mm: Optional[float] = Field(1.5, description="Band thickness in mm")


@router.post("/generate")
async def generate_export(req: ExportRequest) -> Response:
    """Generate and return a STEP or STL file for the specified design."""
    if req.jewelry_type.lower() != "ring":
        raise HTTPException(status_code=400, detail="Currently only 'ring' export is supported")

    builder = RingBuilder(
        finger_diameter_mm=req.finger_diameter_mm or 17.3,
        band_width_mm=req.band_width_mm or 4.0,
        band_thickness_mm=req.band_thickness_mm or 1.5,
    )
    body = builder.build()

    fmt = req.format.lower()
    if fmt == "stl":
        content = export_stl(body, binary=True)
        return Response(content=content, media_type="application/octet-stream",
                        headers={"Content-Disposition": "attachment; filename=ring.stl"})
    elif fmt == "step":
        content = export_step(body)
        return Response(content=content, media_type="text/plain",
                        headers={"Content-Disposition": "attachment; filename=ring.step"})
    else:
        raise HTTPException(status_code=400, detail=f"Unsupported format '{fmt}'. Use 'stl' or 'step'.")
