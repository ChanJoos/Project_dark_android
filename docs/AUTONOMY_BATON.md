# PROJECT DARK Autonomy Baton

This file carries only the immediate handoff between scheduled autonomous developers. Long-lived project truth belongs in `docs/PROJECT_STATE.yaml`; development rules belong in `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.

## Current baton

AGENT: D

HEAD / WORK LINE:
- `director/milles-grass-first` / PR #112
- START_HEAD: `9c69f67bb8f9ecdc7a7b8fbb7b666c0f6f9a4f08`
- RUNTIME_HEAD: `0e5deb2073b5196a46e022b2fd75163e9833e6ff`
- exact-head CI: run #383 PASS

TASK / WORK PACKAGE:
- `WORLD_MILLES_001`
- one runtime delta: stable major landmark footprints and district ground boundaries before vertical asset reintroduction.

SOURCES USED / RESOLVED:
- `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`
- `docs/PROJECT_STATE.yaml`
- `docs/AUTONOMY_BATON.md`
- `docs/MILLES_WORLD_DIRECTION.md`
- `master/MASTER_MANIFEST.md`
- `master/data/Asset_Map_Mapping.csv`
- `master/data/Map_Instance_Master.csv`
- `master/data/Spawn_Master.csv`
- `app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapLayer.java`
- `app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapRenderer.java`
- `app/src/main/java/com/projectdark/mobile/WorldDef.java`
- `docs/REFERENCE_GROUND_TRUTH.md` and `docs/reference/MILLES_FRAME_ANALYSIS.md` remain absent/UNRESOLVED; no contents were inferred.

WHAT CHANGED / RUNTIME DELTA:
- preserved the BUILD_VERIFIED continuous grass/road/plaza/water/crossing foundation;
- added four stable irregular district-ground polygons independent of navigation cells: north service/residential green, west craft yard, east church quiet forecourt, south market commons;
- added an explicit east-church LANDMARK footprint candidate so the major landmark has reserved breathing room before a sprite is chosen;
- renderer now draws these district pads below roads/water/vertical entities and reports `MILLES_FLOOR_FOUNDATION_V4_DISTRICT_LANDMARK_FOOTPRINTS`;
- all new footprint geometry and colors remain explicitly `[ADAPTED]`; no unsupported original coordinates are claimed.

WHY VS REFERENCE / CANON:
- active world direction requires build order water/terrain -> major landmark footprints -> buildings;
- `Map_Instance_Master.csv` confirms Milles town identity but exposes no landmark geometry;
- `Asset_Map_Mapping.csv` says the old-town source exists but exact Milles image identification is unresolved;
- therefore stable district footprints are a coherent `[ADAPTED]` prerequisite, not source-derived original layout.

TESTS:
- prior runtime `7ea7b1ec...` independently preserved as BUILD_VERIFIED; intervening commits before this run were docs-only;
- exact runtime HEAD `0e5deb2073b5196a46e022b2fd75163e9833e6ff`, Actions run #383: Android SDK prepare PASS, Master validation PASS, Java compile PASS, debug APK build PASS, artifact upload PASS.

DONE:
- continuous world-space grass floor;
- central civic square + connected road hierarchy;
- south-east water body + wet bank terrain transition + crossing;
- major district ground hierarchy + east church landmark footprint;
- exact-head CI/build path operational.

ACTIVE:
- `WORLD_MILLES_001` remains open; the floor hierarchy is structurally ready for audited vertical asset reintroduction, but buildings/props and final collision/entrance contracts are not complete.

NEXT:
- audit the candidate Milles production object set and assign REUSE/REWORK/REMAKE/NEW dispositions for the church and first district buildings;
- reintroduce only approved vertical assets at stable world-space landmark footprints;
- preserve the current floor candidate unless new source/device evidence reveals a deterministic defect.

LATER:
- vegetation/fences/props;
- collision/entrances/NPC anchors and full traversal verification;
- fresh APK device/visual acceptance.

BLOCKED:
- exact original Milles full geometry and exact old-town reference image identity remain unresolved.

KNOWN RISK:
- district pad shapes, landmark footprint and colors are `[ADAPTED]`; build correctness is verified but visual quality remains unverified on device.

ACCEPTANCE STATE:
- IMPLEMENTED: YES
- INTEGRATED: YES on PR #112 shared line
- BUILD_VERIFIED: YES for runtime HEAD `0e5deb2073b5196a46e022b2fd75163e9833e6ff`
- DEVICE_VERIFIED: NO for this exact build
- VISUAL_ACCEPTED: NO
