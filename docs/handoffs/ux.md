# UX / NPC / Quest handoff

## 2026-09-10 19:55 KST — device playtest canon response

Branch: `agent/ux/playtest-20260910-1955`
Base main after canonical amendment: `200edd0aa1a3fcbd3eacbe32c8056bc8357b2137`
Canonical amendment: `design/PLAYTEST_CANON_20260910_1938.md`

### Canonical delta handled first
The 19:38 device playtest supersedes earlier runtime-complete claims. UX-owned P0 findings handled in this pass:
- HUD still reads as assembled/debug-like rather than one polished mobile MMORPG shell.
- empty-map tap movement is unreliable because broad invisible `isHudSurface(...)` rectangles intercept visible world taps.
- live world still uses a stretched screenshot despite the World-owned renderer contract being available.

### Code delta — GameView v0.73
- Replaced duplicated camera/move-target construction with the stable World-owned `WorldRuntimeAdapter` surface.
- Wired `AdaptedMillesMapRenderer.draw(canvas, worldRuntime)` into the actual live world layer before dynamic entities; the screenshot URL is no longer used as the live world texture by GameView.
- Generic map taps now call `WorldRuntimeAdapter.requestGroundScreenTap(...)`; NPC/monster/modal/control precedence remains above generic ground movement.
- Replaced the broad HUD interception rectangles with precise visible panel/control hit regions. Utility buttons and combat controls use circular hit regions; empty visible world outside actual HUD surfaces reaches map movement.
- Tightened HUD spacing/opacity and removed several unnecessary debug-like labels so the world remains visually dominant.
- Inventory and dialogue remain modal and continue consuming their own input.
- Direct-inventory reward banner remains presentation-only and reads existing RPG `RewardNotice`.

### Cross-domain blockers / requests
- CHARACTER: 19:38 user canon requests player presentation scale 1.60 [ADAPTED] and smaller head/body ratio ~0.28–0.30. CharacterRenderer internals were not modified by UX; Character domain must expose/apply the updated presentation.
- RPG: `combat_dummy_01 [B]` still needs the canonical-amendment QA fixture reward (`IT_GLOVE_LEATHER` x1 preferred) through RPG-owned reward mutation so device verification can confirm real inventory growth exactly once. UX does not fabricate or mutate this reward.

### Device acceptance focus
1. Tap 10 representative empty visible-world points across center/left/right/top/bottom after camera movement; accepted taps must show marker + WALK unless legitimately blocked.
2. Tap every visible HUD/control region; none may leak to ground movement.
3. NPC/monster selection must still beat generic world taps.
4. Live village must render from the World renderer, not the stretched reference screenshot.
5. Verify HUD hierarchy/spacing on device and confirm central world visibility is improved.

### Boundary note
UX/presentation/input-routing/top-level World adapter wiring only. No World algorithms, CharacterRenderer internals, CombatResolver/MonsterAI/damage, RPG reward mutation, save internals, or canonical numeric data were modified.

---

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
