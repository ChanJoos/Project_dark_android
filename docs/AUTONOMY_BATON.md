# PROJECT DARK Autonomy Baton

This file carries only the immediate handoff between scheduled autonomous developers. Long-lived project truth belongs in `docs/PROJECT_STATE.yaml`; development rules belong in `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.

## Current baton

AGENT: DIRECTOR

HEAD / WORK LINE:
- `director/milles-grass-first`
- current head includes the Milles floor-foundation reset and durable Milles world-design direction.

TASK / WORK PACKAGE:
- `WORLD_MILLES_001`
- rebuild Milles from a coherent village plan rather than preserving the prior spawn-offset asset cluster.

SOURCES USED:
- latest device evidence registered in `docs/PROJECT_STATE.yaml`
- `docs/MILLES_WORLD_DIRECTION.md`
- `WorldDef` world bounds/camera contract
- existing Milles references, map master/mapping data and production candidate assets

RUNTIME DELTA:
- removed all building/lake/tree/fence/street-prop drawing from the Milles map renderer for the foundation pass;
- removed per-cell 64x32 visible floor stamping;
- replaced the screen-fixed backdrop with a bounded world-space ground slab tied to `WorldDef.MIN/MAX`;
- roads/plaza/gate are continuous world-space surfaces and scroll with the camera;
- outside-map background is intentionally distinct so camera/bounds defects cannot be hidden.

DESIGN DIRECTION NOW DURABLE:
- `docs/MILLES_WORLD_DIRECTION.md` is the governing world-direction document for this work package.
- Milles is to be built as one believable quiet medieval village with a central civic square, connected districts, readable road hierarchy, water-side space and green/open areas.
- missing original coverage must be filled with coherent `[ADAPTED]` design, not empty space or arbitrary clutter.
- `assets/milles/production` is a candidate library, not a completed final asset set.
- every object must be classified REUSE / REWORK / REMAKE / NEW; missing or inadequate objects are to be recreated as required by the village design.
- major objects use stable world-space coordinates; `spawn + arbitrary offset` is not an acceptable production layout model.

TESTS:
- source-level contract inspected; exact-head CI still required after runtime iterations.

ACCEPTANCE STATE:
- IMPLEMENTED: floor foundation YES on branch
- INTEGRATED: NO
- BUILD_VERIFIED: NO
- DEVICE_VERIFIED: NO
- VISUAL_ACCEPTED: NO

STATE UPDATED:
- no completion claim; `WORLD_MILLES_001` remains open.
- durable world-design and asset-production direction has been added to the repository so later agents must not revert to asset-cluster/prototype-map behavior.

NEXT BEST ACTION:
- turn `docs/MILLES_WORLD_DIRECTION.md` into an actual world-space layout: define the central square and primary road hierarchy first, then water/terrain transitions and district footprints. Only after those are stable should major buildings and object families be reintroduced or remade.

BLOCKERS:
- verified original Milles full geometry remains incomplete; unknown areas must stay `[ADAPTED]`, not fabricated as canonical.

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
