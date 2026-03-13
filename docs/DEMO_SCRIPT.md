# Demo Script — v0.1.0

## Prerequisites
```bash
pip install -e ".[dev]"
```

## 1. Start the API Server
```bash
uvicorn src.main:app --reload --port 8000
```

## 2. Health Check
```bash
curl http://localhost:8000/health
# → {"status":"ok","version":"0.1.0"}
```

## 3. Browse Materials
```bash
curl http://localhost:8000/assets/materials | python -m json.tool
# Shows 6 materials: 18K Yellow Gold, 14K White Gold, 950 Platinum, …

curl "http://localhost:8000/assets/materials?category=gold" | python -m json.tool
# Filters to gold materials only
```

## 4. Get AI Design Suggestions
```bash
curl -X POST http://localhost:8000/ai/design-suggest \
  -H "Content-Type: application/json" \
  -d '{"jewelry_type":"ring","material":"platinum","budget_range":"$1000-$3000"}' \
  | python -m json.tool
# Returns 3 ring suggestions with confidence and explainability
```

## 5. Check Manufacturability
```bash
curl -X POST http://localhost:8000/ai/check-manufacture \
  -H "Content-Type: application/json" \
  -d '{"band_width_mm":4.0,"band_thickness_mm":1.5,"stone_diameter_mm":6.5,"setting_type":"prong","num_prongs":4}' \
  | python -m json.tool
# → {"passed": true, "confidence": 0.95, "explainability": "..."}
```

## 6. Trigger a Failure
```bash
curl -X POST http://localhost:8000/ai/check-manufacture \
  -H "Content-Type: application/json" \
  -d '{"band_thickness_mm":0.3}' \
  | python -m json.tool
# → {"passed": false, "issues": [...], ...}
```

## 7. Export Ring as STL
```bash
curl -X POST http://localhost:8000/export/generate \
  -H "Content-Type: application/json" \
  -d '{"format":"stl","jewelry_type":"ring","finger_diameter_mm":17.3}' \
  --output ring.stl
ls -la ring.stl
# Binary STL file ready for 3D printing
```

## 8. Export Ring as STEP
```bash
curl -X POST http://localhost:8000/export/generate \
  -H "Content-Type: application/json" \
  -d '{"format":"step","jewelry_type":"ring"}' \
  --output ring.step
head ring.step
# ISO-10303-21 STEP file for CAM
```

## 9. Run Tests
```bash
pytest -v
# All tests should pass: geometry, jewelry, AI, export, acceptance, API
```

## 10. Run Acceptance Scenarios
```bash
pytest tests/test_acceptance.py -v
# 5 bench-jeweler acceptance scenarios:
#   1. Engagement ring design → export
#   2. Wedding band with channel → render → STEP export
#   3. AI-assisted pendant design → manufacturability check
#   4. Multi-stone eternity band → STL export
#   5. Material selection → cost estimation
```
