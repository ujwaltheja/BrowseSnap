# Changelog

## v0.1.0 — Initial Release

### Added
- **Geometry Kernel**: NURBS curves and surfaces with De Boor evaluation, solid body representation with triangle meshes, Boolean operations (union, subtract, intersect), geometric primitives (cylinder, torus, sphere, cone), and affine transforms (translate, rotate, scale, mirror).
- **Jewelry Tools**: Parametric ring builder with configurable finger size, band width, and thickness; stone-setting generator supporting prong, bezel, channel, and pavé types; band cross-section profiles (flat, domed, comfort-fit, knife-edge).
- **Renderer**: Scene/render pipeline returning mesh metadata and bounding boxes for WebGL integration.
- **AI Services**: `POST /ai/design-suggest` for rule-based design suggestions with explainability and confidence; `POST /ai/check-manufacture` for manufacturability analysis using bench-jeweler heuristics; `GET /assets/materials` for materials database (gold, platinum, silver, titanium).
- **Export / CAM**: Binary and ASCII STL export; simplified STEP (ISO 10303-21) export with Cartesian points and manifold solid B-rep.
- **Tests**: Unit tests for geometry operations, jewelry tools, AI services, export modules; five bench-jeweler acceptance scenarios; FastAPI endpoint integration tests.
- **Documentation**: README with API examples, project structure overview, and quick-start guide.
