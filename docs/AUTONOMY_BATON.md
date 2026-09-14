# PROJECT DARK Autonomy Baton

This file carries only the immediate handoff between scheduled autonomous developers. Long-lived project truth belongs in `docs/PROJECT_STATE.yaml`; development rules belong in `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.

## Current baton

AGENT: D

HEAD / WORK LINE:
- `director/milles-grass-first` / PR #112
- START_HEAD: `3eb355fbb797554191559de69a9f8c5e19a8773f`
- RUNTIME_HEAD: `ce13533679aa5099dc1096a74c390c46e8b039e4`
- STATE_HEAD before this baton: `25189037955c68bcc6dd4bf5c7e67fcae3d63d43`

TASK / WORK PACKAGE:
- `WORLD_MILLES_001`
- one runtime delta: central civic square + connected primary/secondary road hierarchy.

SOURCES USED / RESOLVED:
- `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`
- `docs/PROJECT_STATE.yaml`
- `docs/MILLES_WORLD_DIRECTION.md`
- `master/MASTER_MANIFEST.md`
- `master/data/Asset_Map_Mapping.csv`
- `master/data/Map_Instance_Master.csv`
- `master/data/Spawn_Master.csv`
- `app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapLayer.java`
- `app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapRenderer.java`
- `app/src/main/java/com/projectdark/mobile/WorldDef.java`
- old state pointed at `docs/REFERENCE_GROUND_TRUTH.md` and `docs/reference/MILLES_FRAME_ANALYSIS.md`, but both paths are absent from the repository; this is now explicitly UNRESOLVED in PROJECT_STATE rather than falsely counted as consumed evidence.

WHAT CHANGED / RUNTIME DELTA:
- replaced visible rectangular ROAD strips with absolute world-space route polylines;
- enlarged the central civic square as the route hub;
- added connected north service/residential, west craft/equipment, east church, south market/gate and narrow south-east waterside routes;
- retained logical ROAD rectangles only for existing navigation/tile classification, so navigation semantics no longer dictate visible road geometry;
- all new route geometry is explicitly `[ADAPTED]`, not claimed original.

WHY VS REFERENCE / CANON:
- latest device evidence failed the brown/checker test-board world;
- durable Milles direction requires ground -> plaza/roads -> water/terrain -> buildings, and prohibits spawn-offset asset clustering;
- Map_Instance_Master confirms MAP_MILLES is a Town/original-coordinate hub but provides no recoverable road geometry;
- Asset_Map_Mapping says old-town source exists but exact Milles image identity still needs visual resolution, therefore unsupported geometry remains UNRESOLVED and this coherent village layout is `[ADAPTED]`.

TESTS:
- source-level/API compatibility inspection: PASS;
- PR #112 remains mergeable after runtime commits;
- no GitHub Actions run is currently attached to this branch/exact head, so BUILD_VERIFIED is NOT claimed.

DONE:
- continuous world-space grass floor foundation;
- central square + first coherent road hierarchy integrated on shared line.

ACTIVE:
- `WORLD_MILLES_001` remains READY/open; build validation and remaining floor layers are incomplete.

NEXT:
- exact-head Android build validation;
- if build passes, implement water body/bank/bridge + wet/grass terrain transitions, still before vertical buildings/props.

LATER:
- district footprints and audited REUSE/REWORK/REMAKE/NEW vertical asset reintroduction;
- vegetation/props/collision/entrances/device acceptance.

BLOCKED:
- exact original Milles full geometry is unavailable;
- repository reference pointer files named above are missing; do not fabricate their contents.

KNOWN RISK:
- road geometry is coherent but `[ADAPTED]`; without fresh device footage it is not DEVICE_VERIFIED or VISUAL_ACCEPTED.

ACCEPTANCE STATE:
- IMPLEMENTED: YES
- INTEGRATED: YES on PR #112 shared line
- BUILD_VERIFIED: NO
- DEVICE_VERIFIED: NO for this exact build
- VISUAL_ACCEPTED: NO
