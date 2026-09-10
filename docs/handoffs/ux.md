# UX / NPC / Quest handoff

## 2026-09-10 19:02 KST — inventory modal input-safety continuation

Branch: `agent/ux/auto-20260910-1902`
Parent UX head: `d24f20842d9c3be3c7aa07e70b39d832cbdbfefb` (PR #63)
Latest main verified before work: `2b66df9780142e3d84606f4ac0250dcabfa2d2b7`

### Canonical read-first result
- Re-read `design/DESIGN_CONSTITUTION.md`, `design/DATA_CONTRACT.md`, `design/SOURCE_OF_TRUTH.md`, current UX handoff, and current DEV_HISTORY lineage before implementation.
- No newer canonical rule supersedes the mobile shell, tap-to-move, diagonal character, or direct-inventory reward contracts.
- Direct inventory auto-loot remains canonical. No ground-drop/pickup behavior was introduced.
- PR #63 remains open/draft/mergeable and is the immediate UX continuity baseline; this run continues from its head rather than rebuilding the HUD from main.

### User-visible delta completed
`GameView` v0.72 fixes the inventory modal so it behaves like a real mobile-game overlay instead of a visual panel over still-active gameplay controls:
- adds a visible circular `×` close affordance to the inventory header;
- tapping the close affordance closes inventory and consumes the touch;
- tapping outside the inventory card now closes inventory and consumes the touch;
- while inventory is open, the same touch can no longer fall through to joystick, combat buttons, NPC/monster selection, or generic map tap-to-move;
- existing row selection/equip interactions remain intact.

This closes a concrete P0 input-routing violation in the v0.71 redesign: modal UI touches must never leak into world movement/action commands.

### Runtime semantics preserved
- camera-correct blank-map tap-to-move unchanged;
- joystick/direct-input priority unchanged when no modal is open;
- NPC/monster hit-test priority unchanged;
- ATTACK/MAGIC/SKILL/KICK APIs unchanged;
- CharacterRenderer usage unchanged;
- direct-inventory reward banner/read-only RewardNotice consumption unchanged;
- no World, Combat, MonsterAI, RPG mutation, save, or canonical data internals changed.

### Remaining continuity
1. Device/integrator QA of the full v0.71/v0.72 HUD touch geometry and readability.
2. Stable player action/AUTO presentation/orchestration DTO remains the blocker for real player AUTO and canonical icon/learned/disabled-reason metadata.
3. If that contract is still absent next run, continue UX polish using only existing stable surfaces; do not invent AUTO behavior or canonical action metadata.

### QA focus
- Open BAG, tap world: inventory should close, player must not move from that same tap.
- Open BAG, tap joystick/ATK/SKILL/MAG: inventory should close, no action should fire from that same tap.
- Explicit `×` closes BAG without movement/action side effects.
- Item row selection and Equip remain interactive inside the modal.
- After closing, the next independent world/control tap behaves normally.

### Boundary note
UX/presentation/input-routing only. No gameplay values or non-UX domain internals changed.
