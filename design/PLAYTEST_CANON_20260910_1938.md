# PROJECT DARK — DEVICE PLAYTEST CANON 2026-09-10 19:38 KST

Status: USER-CONFIRMED / CANONICAL AMENDMENT
Authority: latest user device playtest. This file supersedes older claims of runtime completion for the items below.

## Tested baseline
- main APK lineage after Director integration through `9302f302999401903e74197026831ed765e071c6`.
- BUILD success does not imply RUNTIME VERIFIED for the items below.

## P0-1 Character scale and proportion
- Current player character is still slightly too small on device.
- Increase overall player presentation size above the current 1.50 baseline for the next device test. Target **1.60 [ADAPTED]** unless a later user instruction supersedes it.
- At the same time, reduce the face/head visual proportion slightly relative to the body. Target head/body visual ratio around **0.28–0.30** for the next adapted atlas/rig iteration rather than enlarging the current head with the overall scale.
- Preserve NW/NE/SW/SE diagonal facing, hard-pixel look, contiguous shoulders/arms/hands/weapon and pelvis/legs.
- Character logical world coordinates/collision remain independent from render scale.

## P0-2 HUD visual quality
- Current redesigned HUD is functionally richer but still visually awkward/unfinished.
- Improve hierarchy, spacing, alignment, typography, panel opacity, icon/button consistency and negative space so it reads as one polished mobile MMORPG HUD rather than assembled debug widgets.
- Keep world view dominant and touch targets usable.
- Do not enlarge invisible HUD interception rectangles merely to match visual groups; touch ownership should follow the actual visible interactive controls/panels.

## P0-3 / P0-6 Empty-map tap movement — runtime failed
- User confirms empty-map tap-to-move is still unreliable / effectively not working in the tested APK.
- This remains a P0 runtime defect even though `requestGroundMove` and camera transform code exists.
- Director inspection found the current `GameView.isHudSurface(...)` rejects several very large rectangular screen regions, so visible empty world inside those rectangles can be consumed as HUD and never reaches world tap routing.
- Replace broad HUD blocking rectangles with precise actual-control/modal hit regions. Empty visible world must reach `screenToWorld -> WorldRuntimeAdapter/WorldMoveTargetController -> WALK`.
- Device acceptance: tap at least 10 representative empty-world points across center/left/right/top/bottom safe world areas after camera movement; accepted taps must show marker and cause WALK unless genuinely blocked/unreachable.
- NPC/monster/modal/control priority must remain intact.

## P0-4 Map/world presentation — runtime failed
- The current result does NOT yet read as a larger coherent village. It appears mainly as a larger canvas while retaining the captured screenshot look and large black/empty regions.
- Root integration defect: current `GameView.drawWorld()` still stretches `WorldDef.VISUAL_SOURCE_URL` screenshot across the expanded world bounds. This violates the existing rule that a whole gameplay screenshot is not the final map texture.
- Stop using the screenshot as the live world texture. It may remain reference-only.
- Wire the current World-owned `WorldRuntimeAdapter + AdaptedMillesMapRenderer` (or its superseding renderer) into the actual GameView world layer before dynamic entities.
- Render a coherent Tile Map + Object + Collision-aligned building/decoration presentation across the traversable village. No unexplained black gaps inside reachable map space.
- Expansion means additional authored/source-backed streets, plazas, buildings, vegetation/props, entrances and traversal continuity, not merely larger numeric bounds.
- All unverified reconstruction stays `[ADAPTED]/[B]`; source-backed original reconstruction remains the target.

## P0-5 Monster defeat reward -> inventory — runtime failed
- User sees reward feedback, but opening inventory shows no item.
- This means presentation exists while actual inventory mutation for the live test monster is absent.
- Current live Milles monster is `combat_dummy_01 [B]`; canonical reward catalog has no entry for it, therefore the reward flow can produce no actual item grant.
- For vertical-slice runtime verification, allow an explicitly labeled **`[B]/[ADAPTED] TEST REWARD`** for `combat_dummy_01` so every defeat deterministically grants one registered inventory item exactly once. This is a QA fixture only and must never be presented as original monster drop data.
- Preferred test item: an already registered item such as `IT_GLOVE_LEATHER`, quantity 1, until a source-backed Milles monster/reward relation replaces the dummy.
- Device acceptance: kill dummy -> exactly one reward event -> inventory quantity increases by exactly one -> reopen inventory and see the item -> repeated UI consumption does not duplicate grant.

## Director completion gate
Do not call these RUNTIME VERIFIED until a new device APK confirms:
1. player overall size improved while face proportion is slightly smaller;
2. HUD visibly polished and world-dominant;
3. 10-point empty-map tap test passes except legitimate blockers;
4. live map is renderer-built village content, not a stretched screenshot/black canvas;
5. combat dummy defeat creates a real visible inventory item exactly once.
