# UX / NPC / Quest handoff

## 2026-09-10 18:25 KST — direct-inventory reward feedback pass

Branch: `agent/ux/manual-20260910-1825`
Base: latest main `ef211e54ff71027477020f9347d2acf96ceb9561`.

### Canonical/design check
- Re-read `design/DESIGN_CONSTITUTION.md`, `design/DATA_CONTRACT.md`, and `design/SOURCE_OF_TRUTH.md` before coding.
- Tap-to-move remains USER CANON and is already present on main `GameView` v0.69; it was not reimplemented.
- Monster rewards remain `MONSTER_DEFEATED -> reward resolution -> direct inventory mutation`; ground drop/pickup remains retired.
- UX owns observability only and must not invent or mutate unresolved rewards.

### User-visible delta completed
- `GameView` v0.70 now polls the existing read-only `RpgInventoryPresentation.latestRewardNotice(...)` projection after runtime state/ledger advancement.
- A newly observed `combatSequence` produces a non-blocking ~2.4s reward banner without opening inventory.
- Resolved item rewards show `자동루팅 · <canonical/runtime item name> x<quantity>` and summarize additional item kinds when present.
- Resolved EXP-only rewards show `보상 처리 · EXP +<value>`.
- Pending/unresolved reward resolution shows `보상 확인 필요`; UX does not fabricate an item, quantity, EXP, Gold, or probability.
- The existing detailed reward view inside inventory remains intact.

### Continuity / blockers
- Stable player action presentation DTO for ATTACK/SKILL/MAGIC/AUTO is still not available on the inspected main surface.
- `MonsterAutoCombatBridge` is combat-owned MonsterAI AUTO intent routing, not a player AUTO-button API; UX must not bind the player AUTO control to it.
- Next UX pass should first check for a new player action/AUTO presentation-orchestration contract; otherwise continue mobile quick-slot press/disabled/selected presentation using only existing stable combat readiness surfaces.

### QA focus
1. Defeat event produces at most one visible reward banner per reward `combatSequence`.
2. RESOLVED item reward name/quantity matches inventory mutation.
3. PENDING reward displays no invented item/value.
4. Reward banner is non-blocking and does not intercept world/HUD touch input.
5. Existing tap-to-move, NPC priority, camera follow and direct-input cancellation do not regress.

### Boundaries preserved
- No World/path/collision/portal implementation changed.
- No CharacterRenderer internals changed.
- No CombatResolver/MonsterAI/damage implementation changed.
- No RPG inventory/reward/EXP/job/save mutation logic changed.
- No canonical values changed.
- No ground-drop/pickup UX introduced.
