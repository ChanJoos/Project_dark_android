# UX / NPC / Quest handoff

## 2026-09-10 13:58 KST — manual UX pass

Branch: `agent/ux/manual-20260910-1357`
Draft PR: #9 `UX: acknowledge NPC tap-to-approach immediately`

### Repository state read
- Latest main observed: `3b58a8d08805cf9031466fe0c50d56e17aa1344d` (`Canonize user-confirmed mobile playtest requirements`).
- `GameView.java` on main remains v0.67.
- `RuntimeState` still exposes incremental collision-aware `tryMove(dx,dy)` but no stable camera-aware move-target/path request contract was found.
- Previous UX draft PR #7 remains unmerged; this pass branches from current main to avoid assuming integration.

### User-visible delta completed
- `InteractionController.request(...)` now emits `NPC 접근 · <name>` immediately when a distant NPC becomes the tap-to-approach target.
- Existing `GameView.updateNpcInteraction()` consumes the message on the next runtime tick and displays it through the existing feedback rail, so NPC taps no longer appear unacknowledged while walking begins.
- Nearby NPCs continue to open dialogue immediately and retain `대화 시작 · <name>` feedback.
- Blocked approach retains the existing actionable failure message.

### Boundary preservation
- NPC movement still routes only through `RuntimeState.tryMove(...)`; no collision/pathfinding/portal bypass was introduced.
- No `GameView.java` changes were needed in this pass.
- No combat resolver, MonsterAI, inventory/reward/EXP/job/save internals were modified.
- No ground-drop/pickup UI was introduced.

### P0 world contract request — still blocked
Owner requested: World / map / movement domain.

UX still needs a stable API/DTO with semantics equivalent to:
1. camera-owned `screenToWorld(...)` conversion consumable by `GameView`;
2. `requestPlayerMoveTarget(worldX, worldY)` that replaces prior movement target and follows canonical collision/pathfinding/portal rules;
3. `cancelPlayerMoveTarget(reason)` for joystick, combat, NPC selection/approach, death, dialogue/modal transitions;
4. read-only movement presentation state (`ACTIVE/REACHED/BLOCKED/...`) so UX can draw status without reproducing world logic.

Once exposed, UX will wire:
- blank-map tap -> accepted move target -> WALK, never teleport;
- newer blank-map tap replaces old target;
- NPC tap has priority over generic map movement;
- HUD/modal touch never becomes movement;
- joystick/combat/NPC inputs cancel/supersede map movement;
- accepted map target gets a short-lived visible tap marker.

### Other blocked contracts
- Quest HUD requires a stable read-only quest presentation/state DTO before replacing the hard-coded prototype quest line.
- AUTO remains disabled/presentation-only until a stable auto-combat contract is exposed.

### Next UX pass
1. Re-check latest main for the World move-target/path contract first.
2. If available, implement the full user-confirmed blank-map tap-to-move P0 in `GameView.java` without duplicating world algorithms.
3. If still blocked, continue a separate user-visible mobile-shell/NPC/dialogue delta while preserving the blocker request.
