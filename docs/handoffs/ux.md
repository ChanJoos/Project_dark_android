# DARK UX · NPC · Quest handoff

## Manual pass — 2026-09-10 13:30 KST

Base main: `52b05bc263d4995a6e20075fb868245114a1c041`
Branch: `agent/ux/manual-20260910-1330`

### Implemented
- Improved NPC tap-to-approach completion feedback in `InteractionController`.
- When auto-approach reaches interaction range and opens the dialogue, UX now emits `대화 시작 · <NPC>` so the player receives explicit transition feedback.
- When pathing is blocked past the prototype timeout, feedback now names the NPC and instructs the player to move manually and tap again instead of showing the opaque `접근 경로 없음` message.
- No ground-drop or pickup UX was introduced.

### Quest status
- Main still has no stable quest presentation DTO/API. `GameView` therefore still contains a hard-coded `밀레스의 첫걸음 0/4` label.
- Do not duplicate quest progression logic in `GameView` to bypass this contract.

### Contract request — RPG/Progression owner
Expose read-only quest presentation data with `questId`, `title`, canonical `state`, ordered objective rows (`label/current/required/complete`), optional source/turn-in NPC IDs, and a presentation-safe next-step label.

### Next UX pass
- Consume the quest projection when available and replace hard-coded quest HUD state.
- Add explicit dialogue affordances for continue / accept / turn-in only where the underlying quest state permits.
- Continue converting prototype text controls into stateful mobile RPG controls without taking ownership of combat/progression rules.
