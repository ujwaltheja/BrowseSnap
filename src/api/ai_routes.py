"""AI-related API endpoints."""

from __future__ import annotations

from typing import Any, Dict, Optional

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from src.ai.design_suggest import suggest_design
from src.ai.manufacture_check import check_manufacturability

router = APIRouter(prefix="/ai", tags=["AI Services"])


# ----- Request / Response schemas -----

class DesignSuggestRequest(BaseModel):
    jewelry_type: str = Field(..., description="Type of jewelry (ring, pendant, earring, …)")
    style: Optional[str] = Field(None, description="Style preference")
    material: Optional[str] = Field(None, description="Preferred material")
    budget_range: Optional[str] = Field(None, description="Budget range, e.g. '$500-$1000'")
    occasion: Optional[str] = Field(None, description="Occasion for the piece")


class ManufactureCheckRequest(BaseModel):
    band_width_mm: Optional[float] = None
    band_thickness_mm: Optional[float] = None
    stone_diameter_mm: Optional[float] = None
    setting_type: Optional[str] = None
    num_prongs: Optional[int] = None
    material: Optional[str] = None
    finger_diameter_mm: Optional[float] = None


# ----- Endpoints -----

@router.post("/design-suggest")
async def design_suggest(req: DesignSuggestRequest) -> Dict[str, Any]:
    """Return AI design suggestions with explainability and confidence."""
    return suggest_design(
        jewelry_type=req.jewelry_type,
        style=req.style,
        material=req.material,
        budget_range=req.budget_range,
        occasion=req.occasion,
    )


@router.post("/check-manufacture")
async def manufacture_check(req: ManufactureCheckRequest) -> Dict[str, Any]:
    """Check design manufacturability with explainability and confidence."""
    design = req.model_dump(exclude_none=True)
    if not design:
        raise HTTPException(status_code=422, detail="At least one design parameter is required")
    return check_manufacturability(design)
