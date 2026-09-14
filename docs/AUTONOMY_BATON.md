# PROJECT DARK Autonomy Baton

This file carries only the immediate handoff between scheduled autonomous developers. Long-lived project truth belongs in `docs/PROJECT_STATE.yaml`; development rules belong in `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.

## Current baton

AGENT: D

HEAD / WORK LINE:
- `director/milles-grass-first` / PR #112
- START_HEAD: `6bbc54704dacc2585294ff8ce6e7966cfffefed7`
- CURRENT_RUNTIME_HEAD: `79cc1d4e7c6681715b2a823c7268e864b0316051`
- exact-head CI: run #387 PASS

TASK / WORK PACKAGE:
- `WORLD_MILLES_001`
- runtime delta: restore authored Milles terrain art and audited production buildings into the coherent world-space rebuild without returning to spawn-relative clustering.

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
- `app/build.gradle`
- `assets/milles/PROJECT_DARK_ASSET_REGISTRY.json`
- `assets/milles/QA_REPORT.txt`
- `assets/milles/QA_ADDENDUM_CHURCH_LAKE.json`
- `assets/milles/production/buildings/`
- `assets/milles/production/landmarks/`
- `assets/milles/production/terrain/` consumed by runtime via the assets sourceSet.
- `docs/REFERENCE_GROUND_TRUTH.md` and `docs/reference/MILLES_FRAME_ANALYSIS.md` remain absent/UNRESOLVED; no contents were inferred.

WHAT CHANGED / RUNTIME DELTA:
- reintroduced six existing production vertical sprites (inn, potion shop, church, weapon shop, armor shop, general shop) at stable absolute world coordinates; no spawn-relative placement;
- the newest serial-baton commit preserved those vertical placements and restored authored production terrain pixels (`OBJ_ground_01/02`, `OBJ_stone_01/02`) across the visible world;
- terrain now uses a staggered 64x32 lattice with a 1px seam guard and nearest-neighbour rendering, replacing the giant flat-color grass/road/district presentation;
- road/plaza intent is projected into authored stone-tile classification while exact original Milles geometry remains `[ADAPTED]`;
- water/crossing remain stable world-space features.

WHY VS REFERENCE / CANON:
- user/device feedback rejected the flat/debug-like map presentation and specifically required the collaboratively produced terrain art to remain in use;
- production object QA reports 51/51 isolated sprites PASS; church also has explicit visual-isolation QA PASS;
- `Map_Instance_Master.csv` confirms Milles as an original tile/coordinate town hub, while `Asset_Map_Mapping.csv` still leaves exact old-Milles image identity unresolved;
- therefore source-backed project art is reused, but tile arrangement, building coordinates and scales remain `[ADAPTED]` pending source/device calibration.

TESTS:
- runtime commit `9aee16c72bc1c57bfb6c2b6e278e30b880d40089` exact-head Actions run #386 PASS: Master validation, Java compile, debug APK build, artifact upload;
- current runtime HEAD `79cc1d4e7c6681715b2a823c7268e864b0316051` exact-head Actions run #387 PASS: Android SDK prepare, Master validation, Java compile, debug APK build, artifact upload.

DONE:
- authored terrain asset path restored to live Milles renderer;
- first audited building/landmark set placed with stable world-space anchors;
- spawn-relative production-object clustering remains removed;
- exact current runtime HEAD build verified.

ACTIVE:
- `WORLD_MILLES_001` is structurally build-verified but now requires exact-APK device evidence before further visual tuning.

NEXT:
- move this visual slice to device validation: inspect tile seam/checker cadence, road readability, building scale/occlusion, church landmark read and camera traversal on the exact #387 APK;
- only after fresh device evidence, repair deterministic visual defects or continue vegetation/fences/props and collision/entrance integration.

LATER:
- vegetation/fences/props;
- collision/entrances/NPC anchors and full traversal verification;
- subsequent combat/content packages once WORLD is awaiting evidence or accepted.

BLOCKED:
- exact original Milles full geometry and exact old-town reference image identity remain unresolved.

KNOWN RISK:
- the seam guard prevents literal cracks but cannot prove the repeated 64x32 authored-tile cadence reads naturally on device;
- building scales/occlusion and world-to-actor depth ordering are not device-verified;
- terrain arrangement and object placement are `[ADAPTED]`, not claimed original Milles geometry.

ACCEPTANCE STATE:
- IMPLEMENTED: YES
- INTEGRATED: YES on the current shared line
- BUILD_VERIFIED: YES for runtime HEAD `79cc1d4e7c6681715b2a823c7268e864b0316051` via run #387
- DEVICE_VERIFIED: NO
- VISUAL_ACCEPTED: NO
