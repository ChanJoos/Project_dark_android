# UX / NPC / Quest handoff

## 2026-09-10 15:54 KST — manual UX pass

Branch: `agent/ux/manual-20260910-1554`

### Repository state
- Latest main observed: `3b58a8d08805cf9031466fe0c50d56e17aa1344d`.
- World move-target/camera contract now exists on `agent/world/20260910-1455`, not yet on main.
- World handoff exposes `WorldCameraTransform.screenToWorld(...)` and `WorldMoveTargetController.requestGroundMove(...)`, with WALK-only path execution, replacement semantics and cancel/status snapshots.
- Because those APIs are not yet merged to main, UX did not copy or reimplement them into GameView.

### User-visible delta completed
- Added a short startup onboarding toast in `MainActivity`: `조이스틱 이동 · NPC/몬스터를 터치해 상호작용`.
- This improves first-run discoverability of the currently available mobile controls without inventing unavailable map-tap behavior.

### Boundary preservation
- No world/camera/pathfinding/collision/portal algorithm was copied into UX-owned code.
- `GameView.java` was intentionally left unchanged because the stable World dependencies are not available on main yet.
- No CharacterRenderer, CombatResolver, MonsterAI, reward/progression/save internals were modified.
- No ground-drop/pickup UI was introduced.

### Next P0 wiring once World merges
1. Instantiate `WorldCameraTransform` using world bounds + gameplay viewport.
2. `snapTo(player)` at boot/respawn and `follow(player)` after logical movement.
3. Project world entities through `worldToScreen` while keeping HUD in screen-space.
4. Reject HUD/modal touches before generic map movement.
5. Preserve NPC tap priority, then convert blank-map taps via `screenToWorld` and call `requestGroundMove`.
6. Tick move-target controller to drive WALK only; never teleport.
7. Cancel move target on joystick, combat, NPC interaction, death/dialog transitions.
8. Draw a short-lived tap marker only for accepted move requests.

### Other blocked contracts
- Quest HUD still needs a stable read-only quest presentation DTO before replacing the hard-coded prototype quest line.
- AUTO stays disabled until an owning domain exposes a stable auto-combat action contract.
