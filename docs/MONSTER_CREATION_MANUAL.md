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
