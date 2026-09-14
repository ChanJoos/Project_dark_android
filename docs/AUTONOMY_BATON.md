# PROJECT DARK Autonomy Baton

This file carries only the immediate handoff between scheduled autonomous developers. Long-lived project truth belongs in `docs/PROJECT_STATE.yaml`; development rules belong in `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.

## Current baton

AGENT: DIRECTOR

HEAD / WORK LINE:
- `director/milles-grass-first`
- current head includes the Milles floor-foundation reset after the initial checkerboard-only patch proved insufficient.

TASK / WORK PACKAGE:
- `WORLD_MILLES_001`
- rebuild from the floor upward instead of preserving the prior asset-cluster prototype.

SOURCES USED:
- latest device evidence registered in `docs/PROJECT_STATE.yaml`
- `WorldDef` world bounds/camera contract
- `AdaptedMillesMapLayer` authored surface regions
- current Milles renderer/runtime call path

RUNTIME DELTA:
- removed all building/lake/tree/fence/street-prop drawing from the Milles map renderer for this foundation pass;
- removed all per-cell 64x32 ground/stone stamping from the visible floor;
- replaced the screen-fixed grass backdrop with a bounded world-space ground slab tied to `WorldDef.MIN/MAX`;
- roads/plaza/gate are now continuous world-space surfaces and scroll with the camera;
- outside-map background is intentionally distinct so camera/bounds defects cannot be hidden.

TESTS:
- source-level contract inspected; exact-head CI pending.

ACCEPTANCE STATE:
- IMPLEMENTED: YES on branch
- INTEGRATED: NO
- BUILD_VERIFIED: NO
- DEVICE_VERIFIED: NO
- VISUAL_ACCEPTED: NO

STATE UPDATED:
- no completion claim; `WORLD_MILLES_001` remains open until a verified Milles layout is rebuilt above this floor foundation and device-tested.

NEXT BEST ACTION:
- verify exact-head build, then replace the old spawn-offset/content-cluster layout with a reference-grounded world-space Milles layout zone by zone. Reintroduce vertical assets only after floor/camera coverage is stable.

BLOCKERS:
- verified original Milles full geometry remains incomplete; do not fabricate it as canonical.

## Baton write format for future agents

Keep future updates short and replace the `Current baton` section with:

- `AGENT`
- `HEAD / WORK LINE`
- `TASK / WORK PACKAGE`
- `SOURCES USED`
- `RUNTIME DELTA`
- `TESTS`
- `ACCEPTANCE STATE`
- `STATE UPDATED`
- `NEXT BEST ACTION`
- `BLOCKERS`

Do not copy the full project state into this file.
