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

## 7. Current acceptance status

The 2026-09-29 versionCode 53 spacing/facing update passed GitHub Actions but was not physically verified on the user's handset. The user later reported that overlap still occurs during automatic combat. Treat the new report as DEVICE_FAILED (user-reported), keep the complete device root cause UNKNOWN until reproduced, and do not mark this manual's spacing rules DEVICE_VERIFIED merely because unit tests pass. See docs/PROJECT_STATE.md and docs/DIRECTOR_BACKLOG.md for the active follow-up. The versionCode 54 follow-up now delegates World point and whole-step actor collision to RuntimeState and blocks entry into a moving monster's reserved destination/path. Exact source HEAD 5905e963de7898acec4b84644e78fcb98cec395e passed Actions run 36570339135; APK artifact 11033662723 has SHA-256 97b892b9afd8afb82c888db1ed13305f565e3072300900e26ffbf5ec848f2fb4. This is BUILD_VERIFIED only; device verification remains pending.

### 2026-09-29 follow-up status

The user subsequently confirmed the v0.55 Pote restart/map persistence behavior. They then reported occasional Pamfet movement-art/facing mismatch and auto-attack circling or choosing a geometrically close but inefficient target. The v0.56 candidate changes are recorded in the current project state/backlog. Until its exact Actions artifact is installed and checked, do not call the changes device-verified or visually accepted.
