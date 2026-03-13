"""Asset-related API endpoints (materials, etc.)."""

from __future__ import annotations

from typing import Any, Dict, List, Optional

from fastapi import APIRouter, HTTPException, Query

from src.ai.materials import get_material, list_materials

router = APIRouter(prefix="/assets", tags=["Assets"])


@router.get("/materials")
async def materials(
    category: Optional[str] = Query(None, description="Filter by category (gold, silver, platinum, …)"),
) -> Dict[str, Any]:
    """Return available materials list."""
    mats = list_materials(category=category)
    return {
        "materials": mats,
        "count": len(mats),
    }


@router.get("/materials/{material_id}")
async def material_detail(material_id: str) -> Dict[str, Any]:
    """Return details for a single material."""
    mat = get_material(material_id)
    if mat is None:
        raise HTTPException(status_code=404, detail=f"Material '{material_id}' not found")
    return mat
