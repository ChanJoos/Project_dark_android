# UX / NPC / Quest handoff

## 2026-09-10 18:38 KST — full mobile MMORPG HUD/UX redesign

Branch: `agent/ux/manual-20260910-1838`
Base main: `40d945d724c906189a630cbfc9d43342ffc6639c`

### Canonical read-first result
- Re-read `DESIGN_CONSTITUTION`, `DATA_CONTRACT`, `SOURCE_OF_TRUTH`, latest UX handoff, and current main runtime before coding.
- No newer canonical rule supersedes the current movement/reward/UX contract.
- Tap-to-move remains implemented and is preserved.
- Monster rewards remain direct inventory grant; no ground-drop/pickup UX exists or was added.

### User-visible delta completed
`GameView` v0.71 replaces the prototype-looking shell with a cohesive mobile MMORPG HUD while preserving runtime semantics:
- compact party card and quest tracker;
- compact top-center target frame with HP bar;
- framed minimap with player marker and de-emphasized coordinates;
- circular utility rail with inventory active state;
- translucent compact chat/activity panel;
- two-layer floating joystick with active/pressed presentation;
- consolidated player Lv/job + HP/MP/EXP status module;
- radial combat cluster with large primary ATK plus SKILL/MAG/KICK/MODE/AUTO secondary controls;
- pressed/selected/cooldown/disabled visual states using existing readiness surfaces;
- player AUTO remains visibly locked because no stable player AUTO orchestration contract exists yet;
- non-blocking centered feedback pills for movement/target/NPC/resource errors;
- separate reward banner consuming `RpgInventoryPresentation.RewardNotice` read-only data;
- inventory restyled as a dimmed modal/card list with selection and equipment action emphasis;
- NPC dialogue restyled as a dimmed game dialogue modal with named header and close affordance;
- death overlay restyled without changing revive semantics.

### Runtime semantics preserved
- blank map tap still routes camera-correct screen→world→World move target;
- joystick direct input still cancels tap movement;
- NPC/monster hit testing still wins over generic ground movement;
- HUD/modal touches remain consumed and do not leak into world movement;
- ATTACK/MAGIC/SKILL/KICK still call the same existing combat methods;
- no World pathfinding/collision/portal code copied or changed;
- CharacterRenderer contract usage unchanged;
- no CombatResolver/MonsterAI/damage changes;
- no RPG inventory/reward mutation changes.

### Remaining contract blocker
- Stable player action/AUTO presentation/orchestration DTO is still needed for canonical icon refs, learned/unlocked state, disabled reasons, selected state, and player AUTO execution. Current v0.71 deliberately derives only safe presentation state from existing combat readiness/cooldown/MP/target surfaces and leaves AUTO locked.

### Integrator QA focus
1. Confirm all combat touch circles align with the new rendered cluster on 960×540 scaling.
2. Confirm HUD/modal touches never issue ground-move commands.
3. Verify joystick drag, map-tap movement, NPC approach, target selection, and combat input priority remain unchanged.
4. Verify reward banners appear once per `RewardNotice.combatSequence` and do not mutate reward state.
5. Verify inventory list selection/equip touch areas match restyled rows/buttons.
6. Verify target/quest/minimap/party panels remain readable without obscuring central world play.
7. Device test cooldown/pressed/disabled visual legibility.

### Boundary note
This is a UX/presentation pass only. No canonical numeric/original-game data was changed.
