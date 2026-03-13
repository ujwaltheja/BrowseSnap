# Jewelry CAD

A parametric jewelry CAD product inspired by **3DESIGN**, built with Python and FastAPI.

## Features

| Module | Description |
|--------|-------------|
| **Geometry Kernel** | NURBS curves/surfaces, solid modeling, Boolean operations, primitives, transforms |
| **Jewelry Tools** | Parametric ring builder, stone-setting tools (prong/bezel/channel/pavé), band profiles |
| **Renderer** | Scene description and metadata for WebGL front-ends |
| **AI Services** | Design suggestion, manufacturability checking, materials database |
| **Export / CAM** | STL (binary & ASCII) and STEP (ISO 10303-21) export |

## Quick Start

```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate  # or venv\Scripts\activate on Windows

# Install dependencies
pip install -e ".[dev]"

# Run tests
pytest

# Start the API server
uvicorn src.main:app --reload
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/ai/design-suggest` | AI-powered design suggestions |
| `POST` | `/ai/check-manufacture` | Manufacturability analysis |
| `GET`  | `/assets/materials` | List available materials |
| `GET`  | `/assets/materials/{id}` | Get material details |
| `POST` | `/export/generate` | Generate STL/STEP export |
| `GET`  | `/health` | Health check |

### Example: Design Suggestion

```bash
curl -X POST http://localhost:8000/ai/design-suggest \
  -H "Content-Type: application/json" \
  -d '{"jewelry_type": "ring", "material": "platinum"}'
```

All AI responses include `explainability` and `confidence` fields.

### Example: Manufacturability Check

```bash
curl -X POST http://localhost:8000/ai/check-manufacture \
  -H "Content-Type: application/json" \
  -d '{
    "band_width_mm": 4.0,
    "band_thickness_mm": 1.5,
    "stone_diameter_mm": 6.5,
    "setting_type": "prong",
    "num_prongs": 4
  }'
```

### Example: Export Ring as STL

```bash
curl -X POST http://localhost:8000/export/generate \
  -H "Content-Type: application/json" \
  -d '{"format": "stl", "jewelry_type": "ring"}' \
  --output ring.stl
```

## Project Structure

```
src/
├── main.py                  # FastAPI entry point
├── geometry/                # Geometry Kernel
│   ├── nurbs.py             # NURBS curves & surfaces
│   ├── solid.py             # Solid bodies & Boolean ops
│   ├── primitives.py        # Cylinder, torus, sphere, cone
│   └── transforms.py        # Translate, rotate, scale, mirror
├── jewelry/                 # Jewelry Tools
│   ├── ring.py              # Parametric ring builder
│   ├── stone_setting.py     # Stone settings (prong/bezel/channel/pavé)
│   └── band.py              # Band cross-section profiles
├── renderer/                # Renderer
│   └── renderer.py          # Scene & render pipeline
├── ai/                      # AI Services
│   ├── design_suggest.py    # Design suggestions
│   ├── manufacture_check.py # Manufacturability checks
│   └── materials.py         # Materials database
├── export/                  # Export / CAM
│   ├── stl_export.py        # STL export (binary & ASCII)
│   └── step_export.py       # STEP (ISO 10303-21) export
└── api/                     # API Routes
    ├── ai_routes.py         # /ai/* endpoints
    ├── assets_routes.py     # /assets/* endpoints
    └── export_routes.py     # /export/* endpoints

tests/
├── test_geometry.py         # Geometry kernel unit tests
├── test_jewelry.py          # Jewelry tools tests
├── test_ai.py               # AI services tests
├── test_export.py           # Export tests
├── test_acceptance.py       # 5 bench-jeweler acceptance scenarios
└── test_api.py              # FastAPI endpoint tests
```

## Running Tests

```bash
# All tests
pytest

# Specific module
pytest tests/test_geometry.py

# Acceptance scenarios only
pytest tests/test_acceptance.py -v
```

## Technology Stack

- **Python 3.10+**
- **FastAPI** — REST API framework
- **NumPy** — Geometry computations
- **Pydantic** — Request/response validation
- **pytest** — Testing

## License

MIT
