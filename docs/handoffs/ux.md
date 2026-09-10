# UX / NPC / Quest handoff

## 2026-09-10 18:01 KST — post-integration tap-move continuity pass

Branch: `agent/ux/auto-20260910-1801`
Draft PR: #51
Base main observed at run start: `35a32e61ea7408a3653a086487fedc009d65cfcb`

### Canonical / current-code reconciliation
- Latest `design/DESIGN_CONSTITUTION.md` still records empty-map tap movement as a current playtest gap, but latest main code has advanced beyond that statement.
- `GameView` on main is now v0.69 and already contains the end-to-end generic ground tap path: current-camera `screenToWorld`, `WorldMoveTargetController.requestGroundMove`, WALK ticking through `RuntimeState.tryMove`, retarget replacement, direct-input/action cancellation, HUD leak prevention, NPC/monster priority, world-anchored tap marker, and REACHED/BLOCKED feedback.
- Therefore this pass treats the implementation gap as code-complete on main and does not duplicate the World or UX movement implementation.
- Canonical movement semantics remain authoritative; this reconciliation only records that the implementation state has caught up with them.

### User-visible delta completed this pass
- Startup onboarding now teaches not only blank-map tap movement but also the canonical control transitions:
  - tap empty world space to move;
  - tap a new world position to replace the active move target;
  - use directional/joystick input to cancel/override tap movement;
  - monster selection remains the entry to ATK / SKILL / MAGIC.
- Landscape runtime behavior is preserved.

### Existing P0 behavior verified in latest main
- UI/HUD/quick-slot surfaces are rejected before generic world movement.
- NPC taps outrank generic map taps and retain existing tap-to-approach/dialogue behavior.
- Monster taps select combat targets instead of issuing ground movement.
- Joystick/direct input cancels the active world move target.
- Combat action input cancels/supersedes the active move target.
- New eligible ground taps replace the previous target.
- Accepted targets show a world-anchored marker and movement result feedback.
- Character-facing mapping remains `NW / NE / SW / SE` through the existing `CharacterRenderer` consumption path.
- Inventory presentation already exposes the RPG `latestRewardNotice(...)` surface and direct auto-loot item count when the RPG reward state supplies it; no ground-drop/pickup UI exists.

### Contract request still open
RPG/Combat should expose a stable read-only action presentation DTO for ATTACK / SKILL / MAGIC / AUTO containing at least:
- `actionId`
- `label`
- `iconKey` / visual ref
- resource type/cost
- cooldown remaining/total
- learned/unlocked state
- enabled/disabled reason
- selected/active state where applicable

Until this reaches main, UX should not fabricate canonical action metadata merely to replace prototype slot labels.

### Next UX continuation priority
1. Verify whether the stable action metadata/action API contract has reached main; if yes, bind it to mobile RPG quick-slot pressed/cooldown/disabled/selected presentation.
2. If still blocked, improve observable direct-inventory reward feedback using the existing RPG presentation/event surface, without creating reward logic in UX.
3. Continue device regression around map-edge camera clamp, rapid retarget, HUD boundary taps, joystick override, NPC/combat priority and marker anchoring.
4. Do not reimplement tap-to-move unless a regression is actually found in current main.

### Boundaries preserved
- No MapDefinition/camera/collision/pathfinding/portal algorithm modified or copied.
- No CharacterRenderer internals modified.
- No CombatResolver/MonsterAI/damage calculation modified.
- No RPG inventory/reward/EXP/job/save internals modified.
- No canonical values or original-game claims changed.
- No ground-drop entity, pickup input, pickup animation or AUTO pickup behavior introduced.
