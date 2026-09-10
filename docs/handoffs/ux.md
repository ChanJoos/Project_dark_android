# DARK UX · NPC · Quest handoff

## Manual pass — 2026-09-10 13:42 KST

Base main: `c8fb07a1cb4358b20a222a8da4013fe6a990b328`
Branch: `agent/ux/manual-20260910-1342`

### User-visible delta
- NPC dialogue now renders with a dimmed modal backdrop and explicit `닫기` action.
- While dialogue is open, unrelated movement/combat/inventory touches are consumed instead of accidentally closing the dialogue.
- Closing dialogue emits `대화 종료` feedback.
- The affordance is labeled `[ADAPTED]`; it is a mobile usability adaptation and is not claimed as original-game behavior.

### Quest status
- Stable quest presentation DTO/API is still absent on main.
- Therefore the hard-coded quest HUD string is intentionally not replaced with invented progression logic in this pass.
- Required RPG contract remains: quest id/title/state/objectives/source NPC/turn-in NPC/presentation-safe next step.

### Loot rule
- Ground drop/pickup UX remains prohibited.
- Monster reward presentation continues to assume direct inventory grant from the RPG domain.

### Next
- When quest projection lands, wire live quest HUD + NPC accept/continue/turn-in affordances without duplicating progression state in GameView.
