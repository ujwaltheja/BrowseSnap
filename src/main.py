"""Jewelry CAD — FastAPI application entry-point."""

from __future__ import annotations

import logging
import os

from fastapi import FastAPI

from src.api.ai_routes import router as ai_router
from src.api.assets_routes import router as assets_router
from src.api.export_routes import router as export_router

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
logger = logging.getLogger("jewelry_cad")

app = FastAPI(
    title="Jewelry CAD API",
    description=(
        "Parametric jewelry CAD product inspired by 3DESIGN.  "
        "Provides geometry kernel, jewelry-specific tools, AI design services, "
        "and STEP/STL export."
    ),
    version="0.1.0",
)

app.include_router(ai_router)
app.include_router(assets_router)
app.include_router(export_router)


@app.get("/health")
async def health() -> dict:
    return {"status": "ok", "version": app.version}
