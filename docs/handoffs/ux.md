# UX / NPC / Quest handoff

## 2026-09-10 13:51 KST — manual UX pass

Branch: `agent/ux/manual-20260910-1349`
Draft PR: #7 `UX: improve mobile combat HUD action states`

### User-visible delta completed
- `GameView.java` v0.68 now presents explicit `ATTACK`, `MAGIC`, `SKILL`, `KICK`, and `MODE` combat controls instead of ambiguous abbreviated slot labels.
- `ATTACK` is visually primary.
- Combat buttons expose enabled/disabled presentation from existing public combat metadata: target availability, cooldown readiness, and MP availability where applicable.
- Touch-down creates a pressed visual state; release/cancel clears it.
- Secondary combat touch radii were increased slightly while remaining inside the HUD action area.
- `AUTO` is visibly disabled because this agent has no stable auto-combat runtime contract to call.

### World contract request — P0 blocker for empty-map tap-to-move
Owner requested: World / map / movement domain.

Current public runtime surface observed by UX:
- `RuntimeState.tryMove(float dx, float dy)` supports incremental collision-aware movement.
- NPC/combat approach controllers can move by repeatedly consuming that low-level movement primitive.
- No stable public camera-aware map tap → world coordinate → move-target/path request contract is currently exposed to `GameView`.

UX must **not** implement pathfinding/collision/portal algorithms inside `GameView`.

Please expose a stable API/DTO with semantics equivalent to:
1. `WorldPoint screenToWorld(float screenX, float screenY, CameraSnapshot camera)` or a camera-owned conversion API consumable by `GameView`.
2. `MoveTargetResult requestPlayerMoveTarget(float worldX, float worldY)` that follows canonical collision/pathfinding/portal rules and replaces the prior move target.
3. `cancelPlayerMoveTarget(MoveCancelReason reason)` for manual joystick input, combat intent, NPC selection/approach, death, dialogue/modal transitions, etc.
4. Read-only movement presentation state sufficient for UX to know whether a target is active/reached/blocked, without reproducing world logic.

Required behavior once the contract exists:
- blank-world tap requests WALK movement, never teleport;
- a newer map tap replaces the previous target;
- NPC tap-to-approach has priority over generic map movement;
- HUD/button/modal taps never become world-move commands;
- joystick/combat/NPC input cancels or supersedes target movement according to the world contract;
- UX will draw a short-lived visible tap marker only after the move-target request is accepted.

### Other blocked presentation contracts
- Quest HUD still needs a stable read-only quest presentation/state DTO before the hard-coded prototype quest line can be replaced safely.
- AUTO remains presentation-only/disabled until an owning domain exposes an explicit stable auto-combat API/event contract.

### Conflict note
Main advanced while this pass was running. PR #7 is intentionally left as a draft and is not merged here; Integrator/Director should rebase or selectively integrate after resolving concurrent `GameView.java` ownership changes.
