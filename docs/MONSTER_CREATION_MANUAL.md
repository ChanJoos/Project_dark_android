## 2026-10-08 — V118 monster quality / intermittent frame repair candidate

V117 DEVICE_FAILED_USER_REPORTED: tap lag improved, intermittent stutter remains; Pamfet art pixelated and old monster coverage questioned. Physical root UNKNOWN_NO_DEVICE_TRACE. Confirmed code hazards: 48px sprites decoded at 24px, first-state/first-hit decode in draw, synchronous two-second disk commit, fixed16ms delay after update. Candidate full192px sprite decode, map-entry all-pose/hit preparation, vsync scheduling and periodic apply(); reward/transaction/pause commit retained. Android orders apply/commit; no asynchronous durability claim.

User authorizes sheet generation/cropping/size matching.14 visual families×12 still poses=168 frames,192RGBA and baseline184. Pamfet and Lycan silhouettes maximum120px preserve prior30/45 logical body heights; remaining species max176px within same48/72/80 logical canvas. Source sheets and exact crop/hash provenance preserved. Pamfet reference is retained current flower-bud silhouette; other supplied tree/insect/wolf/deer sheets guide reimplementation. Generated candidates are not original frames; original match and four-facing visual acceptance remain pending. First ogre-shaped Pamfet generation and incomplete tree block rejected.12 poses means one still per state/direction, not12-frame continuous animation.

A/B/C/D populations remain72/96/108/1. C additionally includes canonical POTE_SPIRIT with alternating brown/black visual variants, existing canonical stat/reward identity unchanged; spawn HP/positions remain adapted. Strong/elite registry aliases remain available using corresponding family art but outside the previously selected ordinary campaign. Full14-art-family coverage does not assert every Master variant has a unique sprite.

Tests add168 native draw/decode checks, all-map prewarm cache stability/variant reachability, real periodic loop apply-vs-commit and pause newest-state; retained save/restart/rollback, pose-size, tap oracle and gameplay checks included. IMPLEMENTED; BUILD/NATIVE/PHYSICAL_PHONE/USER_VISUAL pending until exact CI. Same draft PR180, unmerged candidate vs main18ef615.

## 2026-10-07 — V113 exact CI candidate delivered; phone / visual acceptance pending

Runtime source `d5472f7245ef430601c5bdd0d7f97f8e6ad8775b`; focused Actions [37628115881](https://github.com/ChanJoos/Project_dark_android/actions/runs/37628115881), job112815058869 SUCCESS. **47 tests, zero failures/errors/skips**; 20 native SDK34 captures including each map entry/bridge/centre/exit and same foreground tree with opaque control/player/monster/NPC. Actual opaque foliage overlap, pixel transmission and established portal pixels asserted. All production scenes reviewed; shared movement/collision, navigation, quickquest filter/manual reset, campaign/save/rewards, population and real respawn pass. Original-video asset regeneration matches all9 recorded output hashes. Final river revision softens the grey rim and breaks symmetric stone rows; water/collision geometry unchanged.

Downloaded released version113 /1.13-pote-reference-visibility APK: **55,408,708 bytes**, SHA256 `9e11aec3b06b862aaee847bf535683cd189124fcc93c625187d9df20cf457201`, built **2026-10-07 22:26:05 Asia/Seoul**. Embedded exact source/run/version, ZIP CRC, all47 source equipment files, all9 capture-derived assets/provenance, retained portal,12 Mantis poses,16 mouse frames and skill catalog match; APK v2 signature verified. APK artifact11484753928; native11484847139/archiveSHA abd298c740775de7ee36a9d607fa76b18846ea0a07541ec3a1b15e9ac3634756. [Exact verification](verification/POTE_REFERENCE_V113_BUILD.json). [Candidate APK](https://github.com/ChanJoos/Project_dark_android/releases/download/candidate-v113-d5472f72-37628115881-1/PROJECT_DARK_V113.apk).

IMPLEMENTED / BUILD_VERIFIED / NATIVE_RUNTIME_VERIFIED; PHONE / USER_VISUAL PENDING. Full push37628115950 and PR37628124882 were still IN_PROGRESS at this checkpoint; do not claim full-CI completion or main merge. PR180 remains draft and main18ef615 unchanged. Original map topology and population/respawn canon remain unresolved/adapted; Naver cafe access blocked. The source material is supplied original videos, not full original tiles/maps. Native Robolectric scenes are not physical phone evidence. Existing V112 user visual rejection remains in history. Closure is documentation-only; delivered APK source stays d5472f72.

Next: inspect exact V113 on phone (forest material/river/path, foreground tree fade for player/monster/NPC, whole-map AUTO/quest filter and respawn), finish full CI before any integration, and continue source-backed geometry refinement if visually rejected. No blanket original-perfect claim.

## 2026-10-07 — V113 original-video forest/occlusion repair candidate

V112 local280e5bb is USER_VISUAL_REJECTED by current user report (river/path/map quality and actors hidden behind trees). User explicitly authorizes GitHub publication/CI. Current repair starts from latest main18ef615; old local V112 source workspace absent, source-derived visuals and whole-map AUTO/density changes are rebuilt on current main. V113 implements original-video willow/oak, earth/flowers/current/shore material, smooth shared river collision, expanded A/B/C/D, per-actor foot-depth with actual foreground canopy alpha64, map-wide reachable AUTO and quickquest-only species filter, A72/B96/C108 populations and6–8sec adapted respawn. Approved BODY/diagonal movement/campaign/save/rewards retained. [Contract](POTE_REFERENCE_V113_CONTRACT.md). BUILD/NATIVE final evidence pending at this checkpoint; phone/user visual acceptance PENDING. Naver cafe access was blocked; supplied original videos are the primary visual evidence.


CI follow-up: first candidate `ed587523` passed all42 focused tests, but exact APK gate caught the established portal stored under AssetManager `assets/world/portal/` (ZIP `assets/assets/world/portal/`), not `world/portal/`. Renderer and APK verifier now consume the real retained path/bytes; a native portal-pixel assertion guards blank portals. Full run37625957529 caught an obsolete HudTouchAcceptanceTest radius limit; it is changed to assert remote reachable acquisition and remote unreachable rejection per current user request. Central foreground actor fixtures now explicitly assert opaque foliage overlap and include an actor-free control frame; publication/build evidence pending this follow-up.

## 2026-10-06 — V109 acceptance revision (native verified; phone pending)

V108 c48cd39d is DEVICE_FAILED_USER_REPORTED. Preserve all16 V108 animation PNG bytes and existing collision/AI timings; scale the shared inn/field display64→40px and anchor source baseline59 to actor ground. Body footprint approximately32px, smaller than player48px. Native tests must inspect production idle/walkA/walkB/attack in all4 facings and whole GameView scenes. Rescue now explicitly drives live TAUNT pursuit despite stealth/reset; its non-damaging adjacent range is ADAPTED_USER, not an original monster fact. Actual MainActivity-default regressions verify opening/growth hunts, skill quest and Rescue attacks. Exact source44d19c1e, focused Actions37408187292 SUCCESS/82 tests, including all16 native pose/geometry checks and actual taunted-monster attack. Exact release hash/identity/CRC and all16 source PNG bytes verified. Six native review images inspected; phone/visual acceptance pending. Evidence: docs/verification/DEVICE_QUEST_V109_BUILD.json. Contract: docs/DEVICE_QUEST_V109.md.

## V105 adapted campaign registration — candidate acceptance record

Six retained review families (trant,antlion,gnoll,wolf_rider,ant_giant,silver_wolf) are now registered byte-for-byte under `assets/pote/production/pote_monsters_generated_v1/sprites/campaign_v1`;72 source/runtime pairs and SHA256s are in provenance.json. Earlier historical-only exclusion below is superseded **only for these explicit candidate registrations**. Five prior runtime art families and original Master bytes remain unchanged. Strong gnoll/treant/wolfrider and adapted elite share the corresponding family art, not invented original variant art.

Unique actorID=species#spawn with admitted species reward profile; separate ADAPTED policy,240/450/800/1200zoneHP,2200eliteHP,24second respawn. SharedAI/Resolver/World lattice, no alternate damage path. Spawn selection uses connected terrain and four open neighbours,≥90spacing; gates/NPC/species paths tested. Body frames48px,gnoll/wolfrider/lycan72px,treant80px; originallycan16px collision retained and instance-aware; other current actor collision12px. Candidate idle/walk/attack timings follow existing renderer. PHONE and USER_VISUAL PENDING; this does not establish original monster art/four-facing animation acceptance. Exact source/CI evidence to follow successful build.


### 2026-10-04 Lv1–40 adapted reward profile boundary — implementation unverified

`AdaptedCampaignRewardCatalog` defines 15 noncanonical profiles, separately from `CanonicalMonsterRewardCatalog` and the three training-token test actors. `RuntimeState.enterPoteField()` attaches profiles only to the four currently spawned Pamfet IDs; the accepted `milles_mouse_proto` attaches its own profile only after the adapted inn quest creates that actor. `RpgProgressionState` first resolves canonical entries, otherwise requires a matching actor-level profile. Unprofiled `POTE_PURPLE` and unknown IDs remain pending; `POTE_SPIRIT` remains canonical only. All profile values are project balance, not source EXP/Gold claims.

New reward-boundary and restart tests are wired in CI but have NOT RUN. The local environment has no Gradle executable or wrapper; current source `db231e3dfc50d1450d9ad7daae08fe91464a171e` is local-only after public branch push was rejected by automatic review. Only four of the fourteen Pote profiles are presently reachable from the field; a catalog entry is not a playable spawn.
## 2026-10-03 V93 resolved recipient impact contract (BUILD/NATIVE VERIFIED)
Shared SkillVfxRenderer now consumes actual non-miss damaging monster HIT_FEEDBACK as recipient-only damageImpact on the live target center. It never emits an unlearned monster skill/caster effect or changes chase, wind-up, cooldown, damage, sprite bytes or BODY. Indoor mouse remains a source-still ADAPTED/B actor. New native test uses the actual mouse AI/resolver attack to verify player damage and visible onDraw effect pixels. Exact source42d20ece, Actions37122438002 SUCCESS, actual indoor mouse→player damage/visible onDraw impact native test PASS; no phone acceptance claim.

# Monster Creation and Runtime Integration Manual

This is the required checklist for adding or replacing any monster in PROJECT DARK. It records the current Pote runtime contract and separates confirmed implementation values from prototype-only values. Read it before changing monster art, roster entries, scale, collision, AI, or combat.

## 2026-10-06 Pote Lv11–41 candidate acceptance record — build pending

The local candidate distributes ordinary hunt actors A=`RED/GREEN/PURPLE/SILVER`, B=`TREANT/ANTLION/GNOLL/LYCAN`, C=`WOLFRIDER/ANTGIANT/SILVERWOLF`. Strong/elite actors and spirits are not in this route. D has one `POTE_MANTIS#0` actor using the shared adapted AI, 3200 adapted HP, infinite respawn delay, and zero direct EXP/Gold; completing D03 awards the remaining EXP and one `IT_RING_THREELINEGOLD` directly through the campaign save transaction. The ring is an adapted first-clear quest reward based on the recorded Mantis drop; it is not a ground drop and is not claimed as original mobile payout behavior. These combat values are project test values, not original-game facts. `POTE_MANTIS` has no registered production directional art and currently renders via generic placeholder presentation. The supplied 2026-09-29 screenshots (05–13) are stat/equipment/skill tables; GIFs (14–22) show player/skill effects. Neither set contains Mantis or other directional monster sprites. Do not mark its appearance accepted or claim original artwork. `MAP_POTE_04` remains an isolated old-save compatibility map. Static CSV/diff checks pass; Android tests, package inspection, device play and visual acceptance are NOT RUN because no Gradle executable/wrapper/launcher is available. Current source is the uncommitted candidate based on V110 merge `00b094c76add19d7c5fc1292365cf208882bf3a5`.

## 1. Source of truth and current asset locations

The current Pote runtime candidates are adapted generated test art, not original-game sprites or canonical monster definitions.

| Purpose | Repository path |
|---|---|
| Packaged runtime images | assets/pote/production/pote_monsters_generated_v1/sprites/<art-key>/ |
| Per-image source/hash manifest | assets/pote/production/pote_monsters_generated_v1/runtime_asset_manifest.csv |
| Review/source candidates | assets/pote/review/monster_rebuild_v1/sprites/<art-key>/ |
| Runtime identity and art registry | app/src/main/java/com/projectdark/mobile/PoteForestMonsterShowcase.java |
| Master-backed Pote roster projection | app/src/main/java/com/projectdark/mobile/PoteMonsterRoster.java |
| Sprite rendering and per-species height | app/src/main/java/com/projectdark/mobile/world/PoteFieldRenderer.java |
| Movement, occupancy and actor clearances | app/src/main/java/com/projectdark/mobile/RuntimeState.java |
| Auto-approach path and collision revalidation | app/src/main/java/com/projectdark/mobile/world/WorldRuntimeAdapter.java and WorldMoveTargetController.java |
| Monster AI and attack timing | app/src/main/java/com/projectdark/mobile/MonsterAIController.java |
| Focused regression tests | app/src/test/java/com/projectdark/mobile/PoteMonsterPlayableRuntimeTest.java |

Gradle packages assets/pote/production as the runtime asset root, so the runtime key omits assets/pote/production/. Do not copy historical review-only art into the production source set by accident.

Current runtime mappings:

| Runtime ID | Art key | Draw height | Collision radius |
|---|---|---:|---:|
| POTE_PURPLE | purple_pamfet | 48 px | 12 px |
| POTE_RED | red_pamfet | 48 px | 12 px |
| POTE_GREEN | green_pamfet | 48 px | 12 px |
| POTE_SILVER | silver_pamfet | 48 px | 12 px |
| POTE_LYCAN | lycanthrope | 72 px | 16 px |

Pamfets intentionally retain their small scale. The Lycan is intentionally drawn at player-like height. Do not enlarge every new monster to Lycan size; select scale and collision radius from its approved silhouette and anchor, then test it.

## 2. Required sprite set and file names

A playable directional monster needs exactly 12 registered stills: idle, walk, and attack, each in NW, NE, SW, and SE.

| State | Required files |
|---|---|
| Idle | idle_nw.png, idle_ne.png, idle_sw.png, idle_se.png |
| Walk | walk_nw.png, walk_ne.png, walk_sw.png, walk_se.png |
| Attack | attack_nw.png, attack_ne.png, attack_sw.png, attack_se.png |

Store all 12 beneath the same art-key directory. Keep a transparent background, pixel-preserving nearest-neighbor resizing, a shared bottom-center ground contact, and consistent scale across all directions/states. The present runtime frames are 48×48 RGBA; current candidate silhouettes are no taller than 30 alpha pixels in that frame. Keep the source artwork and hashes in the review directory and manifest.

Do not mirror a direction or silently substitute a missing pose. Do not register a species until the whole 12-pose set is present and its identity/art mapping has been reviewed. These files are one still per state/direction; they do not constitute multi-frame animation.

## 3. Registration sequence

1. Confirm the stable monster ID and name in the Master/source records. Preserve evidence labels and unresolved values; do not invent canonical stats, spawn facts, drops, or attack rules.
2. Confirm the complete image set, anchor, silhouette, direction labels, and scale. Record each source/runtime path and SHA-256 in the manifest.
3. Add the ID-to-art-key entry in PoteForestMonsterShowcase.ART. Its ID list drives the showcase placement, so verify the placement count and that each roster lookup succeeds.
4. Confirm the actor's draw height in PoteFieldRenderer and collision radius in RuntimeState.monsterCollisionRadius. Larger art needs a correspondingly reviewed collision radius; visual scaling alone does not change collision.
5. Wire only through the existing RuntimeState, MonsterAIController, RuntimeCombatSession/MonsterAutoCombatBridge, WorldRuntimeAdapter, and renderer paths. Do not add a parallel GameView damage or movement path.
6. Add focused tests before producing an APK. Keep the new monster tagged as adapted/test-only until its source and visual acceptance are established.

## 4. Movement, facing, spacing, and collision

- World movement uses the existing 64×32 isometric tile centers and four legal diagonal directions. One logical step is NW (−32,−16), NE (+32,−16), SW (−32,+16), or SE (+32,+16). Do not introduce cardinal or eight-way attacks.
- The walk image direction must come from the exact canonical committed movement start-to-target vector after collision/detour selection. Rendering must not use an older attack-facing lock or only the original AI intent. While interpolating, show WALK and keep the sprite facing the actual step. Check all four directions for each registered species; code wiring alone does not prove the generated art itself faces the labeled direction.
- Actor spacing is measured between world ground anchors. Current prototype radii are player 10 px, Pamfet 12 px, Lycan 16 px, with 5 px clearance. This yields minimum center spacing of 27 px for player/Pamfet, 31 px for player/Lycan, 29 px for Pamfet/Pamfet, and 33 px for Lycan/Pamfet.
- Check current positions and reserved in-flight destinations. Check the movement segment against other moving actors as well; an endpoint-only check is insufficient.
- Pote and Milles world adapters that contain actors must use actorsBlockMovement=true. WorldRuntimeAdapter must delegate point and whole-step collision to RuntimeState so movement planning, live step revalidation, and monster AI share the same clearance rules. Do not maintain a separate stale player/monster radius check in a renderer or UI class.
- Automatic combat may stop at a legal adjacent tile and then attack. Never move the player onto the target monster's tile to make melee range succeed. Keep approach range, canonical melee adjacency, and attack-facing derived from the same tile-center contract.
- Auto-targeting compares the shortest reachable path to a legal adjacent tile first, then straight-line distance for ties. Ignore unreachable targets and use a moving monster's reserved next tile for planning. Keep a selected living target stable while its path remains valid to prevent target/path oscillation.
- Approach path planning and each live movement step must use the same full-edge rule, including terrain sweep and actor crossing/reservations. Point occupancy alone is insufficient: a planner must not repeatedly choose a shortest edge that runtime will reject.
- Test overlapping spawn positions, two monsters reserving the same destination, crossing in-flight steps, the player entering a moving monster's destination, and revalidation when the target moves during automatic approach.

## 5. Attack pace and presentation

Current values are prototype/adapted defaults, not original server values:

| Behavior | Current default |
|---|---:|
| Monster tile step | 0.60 s per tile |
| Monster wind-up before hit | 0.24 s |
| Visible recovery after hit | 0.36 s |
| Monster attack cooldown | 1.80 s |
| Attack pose total from wind-up through recovery | about 0.60 s |

The player prototype attack cooldowns are 0.30–0.38 s for melee and 0.52 s for throw. Keep ordinary monster attacks distinctly slower than the player's: retain the 1.80 s monster cooldown unless a new, evidence-backed design is explicitly approved. Do not make monsters repeatedly strike at player speed.

Damage must still pass through the shared combat resolver at the hit frame. The attack art uses one direction-specific image and a small forward/recoil displacement; do not restart the lunge at contact, shake the sprite, snap its direction, or rotate into unsupported directions. Keep attack facing locked only for the attack presentation and never let that lock misdirect a walk pose.

## 6. Required verification before merging or APK delivery

Run the repository workflow and add or extend tests for the new ID. At minimum verify:

- Exactly 12 packaged PNGs resolve for the monster; no stale or review-only asset path is used.
- Idle, walk, and attack images load for all four directions, with shared ground anchoring and the intended per-species size.
- A real AI step moves smoothly, and the walk renderer faces the applied step even if a deliberately stale attack-facing lock is present.
- All four Pamfet direction assets are selected from each exact applied NW/NE/SW/SE step; inspect the art's actual gaze as well as the file name.
- Auto-targeting picks the reachable target with the fewest legal approach steps, ignores blocked targets, uses moving target reservations, and does not switch a still-valid target every frame.
- Player/monster and monster/monster minimum clearances hold at spawn, at destination, and during interpolation.
- The player cannot enter a moving monster's reserved destination or cross its active movement path during automatic approach.
- Monster attacks use only four diagonal facings, shared resolver damage, the wind-up/recovery timings, and the slower cooldown.
- Existing Pote spatial, water, monster presentation, Milles movement, combat, and save tests still pass.
- The APK artifact's source SHA matches the exact tested commit. Report build verification separately from physical-device verification and visual acceptance. A CI-rendered screenshot is not a substitute for device testing.

## 7. Adapted Milles mouse fixture — candidate story slice

`milles_mouse_proto` is a project-authored story fixture, not a canonical monster identity. The current Master has no mouse row or original mouse sprite. It is tagged `B`/`PROTOTYPE_PENDING`, has no canonical EXP or drops, and uses three observed mouse silhouettes cropped from the supplied 20260924_163501 recording. Original direction/action animation remains unresolved; SW reuses the observed west pose without mirroring. See assets/milles/production/interiors/v91/sources.json for hashes, masks and source coordinates. Do not describe fixture stats or behavior as original-game data.

V91 confines this fixture to milles_interior_inn after quest acceptance, uses InnMouseRenderer at 24 px width and RuntimeState collision radius 7, and routes selection, AUTO, damage, quest reward and checkpoint through the existing shared controllers. Outdoor actors are suspended/restored at the doorway. Native InnSourceRepairTest covers the actual indoor target, pursuit, defeat, Mary approach, one reward, HP checkpoint and completed restart. Physical verification and visual acceptance remain pending.

The fixture is included in the same `MonsterAIController.tickPrototypeMonster()` branch as `combat_dummy_01`. Its chase step, attack wind-up and shared-resolver submission are compared against that control in `MillesStoryQuestTest`. The fixture remains project-story-only until exact CI tests and device observation pass; the comparison cannot establish original mouse behavior.

## 8. V108 adapted mouse appearance follow-up

The user explicitly rejected the prior captured gray mouse as not recognizable. Keep the V91 recording crops and hashes unchanged as historical evidence. Production inn and early-field runtime use `assets/milles/production/interiors/v108/field_mouse_{nw,ne,sw,se}_{idle,walk_a,walk_b,attack}.png`, generated for this project and classified `PROJECT_ADAPTED_GENERATED_ART` in the sibling manifest. Each fixed 64×64 transparent frame uses the same 52×36 content projection and baseline; source sheet: `assets/milles/review/interiors/v108/field_mouse_sheet.png`. This is not an original-game sprite claim.

The renderer selects walk A/B from live `isMoving` plus `animationClock`, attack while `attackPrimed` or `attackVisualRemaining` is active, and direction from `visualFacing`. Both the inn story mouse and early field actors share this renderer. Stable actor IDs, campaign targeting/rewards, collision, and combat remain the existing adapted fixture. `MillesMousePresentationTest` checks rendered frame geometry/poses in all four directions and the actual GameView actor path; `InnSourceRepairTest` retains coordinate displacement/chase and quest turn-in coverage. Exact build, device, and user visual acceptance remain separate; the delivered V108 was reported as failed.

## 9. Current acceptance status

The 2026-09-29 versionCode 53 spacing/facing update passed GitHub Actions but was not physically verified on the user's handset. The user later reported that overlap still occurs during automatic combat. Treat the new report as DEVICE_FAILED (user-reported), keep the complete device root cause UNKNOWN until reproduced, and do not mark this manual's spacing rules DEVICE_VERIFIED merely because unit tests pass. See docs/PROJECT_STATE.md and docs/DIRECTOR_BACKLOG.md for the active follow-up. The versionCode 54 follow-up now delegates World point and whole-step actor collision to RuntimeState and blocks entry into a moving monster's reserved destination/path. Exact source HEAD 5905e963de7898acec4b84644e78fcb98cec395e passed Actions run 36570339135; APK artifact 11033662723 has SHA-256 97b892b9afd8afb82c888db1ed13305f565e3072300900e26ffbf5ec848f2fb4. This is BUILD_VERIFIED only; device verification remains pending.

### 2026-09-29 follow-up status

The user subsequently confirmed the v0.55 Pote restart/map persistence behavior. They then reported occasional Pamfet movement-art/facing mismatch and auto-attack circling or choosing a geometrically close but inefficient target. The v0.56 candidate changes are recorded in the current project state/backlog. Until its exact Actions artifact is installed and checked, do not call the changes device-verified or visually accepted.

### 2026-09-30 user-reported v0.56 failure and r4 correction

- The user reports the delivered v0.56 APK still shows occasional Pamfet movement pose/facing mismatch and auto-attack circling/poor route choice. Record this APK as `DEVICE_FAILED (user-reported)`; the user report does not independently identify the root cause.
- Source audit found the monster AI still chose a greedy direction toward the player every tile. Collision handling could substitute a side step; the next AI decision then pointed toward the player again. Replace this with a shortest legal tile route to any melee-adjacent tile, so obstacle detours are planned and stable across replanning.
- The player path planner previously sampled point occupancy along an edge, while live movement additionally rejected actor-crossing paths. Share the full `canPlayerTraverse` contract between route search and movement commit to prevent selecting a route the runtime cannot execute.
- Existing facing tests assert enum/file-name agreement, not that the generated image's gaze actually matches its direction label. Visual gaze acceptance remains pending and must not be inferred from green CI.
- r4 test requirement: construct a route where the direct tile is blocked, recalculate after each step, and prove the actor continues along a stable shortest detour without alternating directions. Retain four-direction pose tests and the real-device gaze/auto-target acceptance checks.
- Do not label a new APK as the fix until the exact-source workflow succeeds and the requested device scenario is checked. Current r4 status and exact evidence live in `docs/PROJECT_STATE.md` and `docs/DIRECTOR_BACKLOG.md`.


### 2026-10-01 V71 recipient hit feedback contract (verification pending)

GameView forwards the shared RuntimeState.Monster.hitFlash timer to the Pote sprite renderer. A cached RGB-only bitmap variant changes only occupied sprite colors on a resolved positive-damage hit; it preserves source alpha, pose/facing, dimensions and ground anchor, and never recolors a later actor or terrain draw. All five species and idle/walk/attack poses follow the same rule. The existing0.14-second timer remains authoritative; MISS, HEAL and zero-damage presentation do not start it. Existing lethal removal is unchanged; no original hurt/corpse animation is claimed. Nonlethal native HP fixtures test rendering without changing production monster stats. Exact-source CI, packaged source pixel identity and native review are required; device/visual acceptance remains pending.


### 2026-10-01 V71 automated acceptance record

Runtime623451c814a89e96df9cc486bb96da8931f689ea passed Actions36803805174/job110183675335. Exact-alpha and normal-pixel restoration checks cover all60 poses;30 real GameView job/species routes cover damage/contact/impact/tint/expiry, with heal/zero-damage protection. Native review contains3 pose grids/10 production hit scenes and confirms popup spacing. All790 packaged asset files match V70 bytes. Final cached RGB-only variants replace color filters that bled on the Lycan fractional scale; source PNGs remain unchanged. APK artifact11136812203 SHA2566f56537b66d56241b2a1e37f1e82b0d616a2edcd59502af01cdfacf5449bee32, built2026-10-01 11:05:13 KST. This verifies the added automated damage-presentation scope; device gaze/spacing and original visual acceptance remain PENDING. Exact record: docs/verification/POTE_DAMAGE_V71_BUILD.json.
## 2026-10-06 — D map Giant Mantis encounter (adapted candidate; visual pending)

`POTE_MANTIS` is now instantiated once on the new `MAP_POTE_D_BOSS` route endpoint and receives the shared prototype AI/resolver path. The fixture uses 3200 HP and zero direct combat EXP/Gold; both are ADAPTED tuning values. The first D03 turn-in grants the missing EXP up to Lv41 and one three-line gold ring directly to inventory, then D04 reports back at Piet. No strong/elite variant is used. The old saved map ID `MAP_POTE_04` remains a separate compatibility map and is not the boss arena.

The user supplied mantis row remains review/source material only. Its directional cells, runtime pose extraction, transparency and foot anchor have not passed the artwork checklist below. Until those are accepted, GameView falls back to the shared generic actor presentation and visual acceptance is pending. This candidate has not been built or tested on Android in this environment; map, quest, boss reward and legacy-map assertions were added but remain unexecuted. `git diff --check` and `tools/validate_master.py` are source checks only.
