# DARK UX · NPC · Quest handoff

## Manual pass — 2026-09-10 13:25 KST

Base main: `d21944e64eb2834838d9f7b79a392181b88929d0`
Branch: `agent/ux/manual-20260910-1325`

### Audit
- `GameView` already consumes `InteractionController` for NPC tap → approach → dialogue.
- HUD still exposes a hard-coded quest string (`밀레스의 첫걸음 0/4`) rather than a quest-state projection.
- Dialogue is currently tap-anywhere-to-dismiss only; no explicit continuation/quest affordance exists.
- Inventory presentation correctly describes monster reward items as auto-looted; no ground-drop/pickup UX should be reintroduced.
- Combat action controls remain prototype circular text controls. They are distinct inputs but need richer mobile states in a later pass.

### Manual pass decision
P0 for the next UX implementation delta is quest orchestration/presentation, not new ground-loot UI. The UX layer needs a stable quest presentation contract instead of inventing quest counters in `GameView`.

### Contract request — RPG/Progression owner
Expose a read-only quest presentation DTO/API with at minimum:
- `questId`
- `title`
- `state` (`AVAILABLE`, `ACTIVE`, `READY_TO_TURN_IN`, `COMPLETED` or equivalent canonical states)
- ordered objective rows: `label`, `current`, `required`, `complete`
- optional `sourceNpcId` / `turnInNpcId`
- optional presentation-safe next-step label

Quest mutation remains outside UX. UX will only consume this projection and route NPC/combat events through stable APIs.

### Next UX implementation
Once the quest projection exists, replace the hard-coded `0/4` HUD text with live quest state and add explicit NPC dialogue affordances for accept/continue/turn-in where the underlying state permits. Preserve tap-to-approach and do not duplicate progression logic in `GameView`.
