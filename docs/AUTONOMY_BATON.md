# PROJECT DARK Autonomy Baton

This file carries only the immediate handoff between scheduled autonomous developers. Long-lived project truth belongs in `docs/PROJECT_STATE.yaml`; development rules belong in `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.

## Current baton

AGENT: D

HEAD / WORK LINE:
- `director/milles-grass-first` / PR #112
- START_HEAD: `36d53becdb771b45595d7b5166764a2e1f4b3c26`
- RUNTIME_HEAD: `7ea7b1ec160607137825765f38e8b48d7e816820`
- exact-head CI: run #379 PASS

TASK / WORK PACKAGE:
- `WORLD_MILLES_001`
- one runtime delta: south-east continuous water body + wet bank transition + stable crossing.

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
- restored CI coverage for integration-branch pushes; removed the obsolete Android setup action path and use the hosted runner SDK manager directly;
- preserved the existing continuous grass/plaza/road foundation;
- added an explicitly `[ADAPTED]` south-east water body using stable absolute world coordinates;
- separated wet-bank and water polygons so the shoreline is a continuous terrain transition instead of a hard tile/cell boundary;
- continued the waterside route with a stable visual crossing drawn over the water edge;
- crossing remains presentation-only until the later collision/interaction phase; no unsupported original geometry is claimed.

WHY VS REFERENCE / CANON:
- Milles direction requires build order ground -> routes -> water/banks/crossing -> landmarks/buildings;
- `Asset_Map_Mapping.csv` confirms an old Milles source exists but exact image identity/geometry is unresolved;
- `Map_Instance_Master.csv` confirms Milles town identity but contains no recoverable water geometry;
- `Spawn_Master.csv` has no Milles town geometry; therefore this water/crossing placement is coherent `[ADAPTED]`, not claimed original.

TESTS:
- CI infrastructure exact-head gate restored: PASS on `36d53bec...` before new runtime work;
- first runtime CI found one deterministic Java syntax error in the new water list; fixed immediately on the same integration line;
- exact runtime HEAD `7ea7b1ec160607137825765f38e8b48d7e816820`, Actions run #379: Android SDK prepare PASS, Master validation PASS, Java compile PASS, debug APK build PASS, artifact upload PASS.

DONE:
- continuous world-space grass floor;
- central civic square + connected road hierarchy;
- south-east water body + wet bank terrain transition + crossing;
- exact-head CI/build path operational for integration-line pushes.

ACTIVE:
- `WORLD_MILLES_001` remains open; floor hierarchy is materially stronger but major landmark footprints and district boundaries/vertical assets are not complete.

NEXT:
- preserve this BUILD_VERIFIED floor candidate;
- next eligible Milles delta: major landmark footprints/district boundaries using stable coordinates, then audited REUSE/REWORK/REMAKE/NEW building reintroduction;
- do not retune this water geometry as if reference-derived without new source/device evidence.

LATER:
- vegetation/fences/props;
- collision/entrances/NPC anchors and full traversal verification;
- fresh APK device/visual acceptance.

BLOCKED:
- exact original Milles full geometry and exact old-town reference image identity remain unresolved.

KNOWN RISK:
- water shape, bank profile, crossing position and colors are `[ADAPTED]`; build correctness is verified but actual visual quality is not device-verified.

ACCEPTANCE STATE:
- IMPLEMENTED: YES
- INTEGRATED: YES on PR #112 shared line
- BUILD_VERIFIED: YES for runtime HEAD `7ea7b1ec160607137825765f38e8b48d7e816820`
- DEVICE_VERIFIED: NO for this exact build
- VISUAL_ACCEPTED: NO
