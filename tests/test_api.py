"""Tests for the FastAPI endpoints."""

import pytest
from fastapi.testclient import TestClient

from src.main import app

client = TestClient(app)


class TestHealthEndpoint:
    def test_health(self):
        resp = client.get("/health")
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "ok"


class TestAIDesignSuggest:
    def test_suggest_ring(self):
        resp = client.post("/ai/design-suggest", json={"jewelry_type": "ring"})
        assert resp.status_code == 200
        data = resp.json()
        assert "suggestions" in data
        assert "confidence" in data
        assert "explainability" in data

    def test_suggest_with_options(self):
        resp = client.post("/ai/design-suggest", json={
            "jewelry_type": "pendant",
            "material": "platinum",
            "budget_range": "$500-$1500",
        })
        assert resp.status_code == 200

    def test_suggest_missing_type(self):
        resp = client.post("/ai/design-suggest", json={})
        assert resp.status_code == 422


class TestAIManufactureCheck:
    def test_valid_design(self):
        resp = client.post("/ai/check-manufacture", json={
            "band_width_mm": 4.0,
            "band_thickness_mm": 1.5,
        })
        assert resp.status_code == 200
        data = resp.json()
        assert data["passed"] is True
        assert "confidence" in data
        assert "explainability" in data

    def test_invalid_thin_band(self):
        resp = client.post("/ai/check-manufacture", json={
            "band_thickness_mm": 0.3,
        })
        assert resp.status_code == 200
        data = resp.json()
        assert data["passed"] is False


class TestMaterialsEndpoint:
    def test_list_all_materials(self):
        resp = client.get("/assets/materials")
        assert resp.status_code == 200
        data = resp.json()
        assert data["count"] > 0

    def test_filter_by_category(self):
        resp = client.get("/assets/materials?category=gold")
        assert resp.status_code == 200
        data = resp.json()
        assert all(m["category"] == "gold" for m in data["materials"])

    def test_get_material_detail(self):
        resp = client.get("/assets/materials/platinum_950")
        assert resp.status_code == 200
        data = resp.json()
        assert data["name"] == "950 Platinum"

    def test_material_not_found(self):
        resp = client.get("/assets/materials/nonexistent")
        assert resp.status_code == 404


class TestExportEndpoint:
    def test_export_stl(self):
        resp = client.post("/export/generate", json={
            "format": "stl",
            "jewelry_type": "ring",
        })
        assert resp.status_code == 200
        assert resp.headers["content-type"] == "application/octet-stream"
        assert len(resp.content) > 84

    def test_export_step(self):
        resp = client.post("/export/generate", json={
            "format": "step",
            "jewelry_type": "ring",
        })
        assert resp.status_code == 200
        assert "ISO-10303-21" in resp.text

    def test_unsupported_format(self):
        resp = client.post("/export/generate", json={
            "format": "obj",
            "jewelry_type": "ring",
        })
        assert resp.status_code == 400

    def test_unsupported_jewelry_type(self):
        resp = client.post("/export/generate", json={
            "format": "stl",
            "jewelry_type": "bracelet",
        })
        assert resp.status_code == 400
