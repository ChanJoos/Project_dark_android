# UX / NPC / Quest handoff

## 2026-09-10 — UX autonomous pass

Branch: `agent/ux/auto-20260910-1358`
Draft PR: #10 `UX: harden NPC dialogue touch flow`

### Canonical state read
- Latest canonical design explicitly makes mobile tap-to-move USER CANON / `[ADAPTED]`.
- UX owns touch hit-testing and wiring; World owns movement/path/collision semantics and must expose a stable move-target API.
- Player movement must ultimately use a following camera/world scroll with correct world↔screen transforms.
- NPC conversation flow must not regress, and placeholder-heavy mobile controls should continue toward clear mobile RPG interaction states.

### User-visible delta completed
- `GameView.java` v0.68 acknowledges NPC taps immediately.
- A distant NPC tap shows `NPC 접근 · <name>` immediately while existing `InteractionController` performs collision-aware approach.
- A nearby NPC tap surfaces the controller's existing dialogue-start feedback immediately.
- Dialogue now renders as an explicit modal with a dimmed backdrop and dedicated `닫기` action.
- While dialogue is open, unrelated touches are consumed; joystick, combat, inventory, monster targeting, and future world-tap routing cannot leak through the modal.
- Closing dialogue emits short completion feedback.

### Ownership audit
- This pass changed only `GameView.java` before creating this handoff.
- No `InteractionController`, RuntimeState/world, CharacterRenderer internals, combat/MonsterAI, RPG/reward/save implementation, or canonical values were changed.
- No ground-drop/pickup UX exists in this change.

### P0 World contract request — still blocked
Search of current `main` found no stable camera-aware move-target/path API suitable for GameView consumption.

Current public runtime still exposes low-level collision-aware incremental player movement (`RuntimeState.tryMove(dx,dy)`), but UX must not build its own pathfinding/portal policy on top of that in `GameView`.

World owner: please expose stable contracts equivalent in semantics to:
1. screen/current-camera → world coordinate conversion consumable by UX;
2. replaceable `requestPlayerMoveTarget(worldX, worldY)` that obeys path/collision/map/portal rules;
3. explicit move-target cancellation for joystick, combat, NPC/dialogue, death/modal transitions;
4. read-only active/reached/blocked movement presentation state.

Once available, UX will wire canonical priorities:
- NPC tap before generic world tap;
- HUD/modal touches never leak to world movement;
- new map tap replaces old target;
- joystick/combat/NPC input cancels or supersedes target movement;
- accepted target draws a short-lived user-visible tap marker;
- player walks rather than teleports.

### Other pending contracts
- Quest HUD remains hard-coded because no stable read-only quest presentation/state DTO was found on main. Do not duplicate quest/progression internals in GameView.
- AUTO remains dependent on an owning runtime contract; do not invent auto-combat logic in the view.

### Next UX priority
1. Consume the World move-target/camera contract immediately when it lands and implement canonical empty-map tap-to-move.
2. Replace hard-coded quest HUD as soon as a quest presentation DTO exists.
3. Continue mobile HUD action-state work without duplicating combat/action internals.
