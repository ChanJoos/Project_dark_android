# PROJECT DARK — DESIGN CONSTITUTION

Status: CANONICAL / Source of Truth gate

Current amendments: `design/PLAYTEST_CANON_20260910_2149.md` supersedes conflicting older scale/movement/world-expansion requirements below. `design/LOD_SYSTEM_EVIDENCE_20260911.md` records the 2026-09-09 NamuWiki snapshot as `[FAN]` evidence and must not be silently promoted to official canon. Operational ownership and execution priorities follow `docs/DIRECTOR_GUIDE.md`. One coherent village screen remains the first production milestone; system discovery does not widen the current implementation sprint.
Updated: 2026-09-11

## 1. Product canon

Core principle: **세계는 원작 그대로. 성장은 원작 그대로. 캐릭터는 원작 그대로. 조작과 UX만 모바일에 맞게 조정한다.**

Development must preserve original proper nouns, job roles, acquisition conditions, currency meaning, quest structure, UI information architecture, and pixel-art proportions when supported by evidence. Mobile changes must be explicitly tagged `[ADAPTED]`.

PROJECT DARK must preserve the original game's identity as more than a combat loop: party role differentiation, open-world social contact, diary/history, quests, PvE/PvP, gathering/social spaces and region identity are design inventory items. Discovery of these systems does not mean all are in the current runtime milestone.

## 2. Runtime scope — immutable unless user explicitly changes it

`캐릭터 생성 → 평민 Lv1 → 밀레스/초반 성장 → 5개 기본직업(전사/도적/마법사/성직자/무도가) → Lv99 지존 → 전직 OR 순수직업 → 다시 Lv99 → 1차 승급`

Out of current runtime scope: 2차/3차 승급 and later progression. They remain long-term source inventory when evidenced, but must not silently expand the first playable milestone.

Character creation does not select a job. Preserve gender/hair/hair-color/name creation flow; job is selected after starting as 평민.

## 3. Evidence policy

Every original-game claim/value should retain evidence status:
- `[O]`: official/original evidence
- `[V]`: verified old official-hosted/community experiment or directly verified observation as applicable
- `[U]`: user-confirmed recollection
- `[B]`: balanced reconstruction/prototype value, NOT an original fact
- `[ADAPTED]`: deliberate mobile adaptation
- `PENDING` / `PENDING_CROP`: insufficient evidence or asset identification
- `[FAN]`: fan/community evidence; never silently promote to official

If exact original HP, damage, timing, drop rate, formula, sprite frame count, map geometry, etc. is unknown, do not invent it as canon. Keep it null/PENDING or isolate a prototype `[B]` value.

The NamuWiki snapshot dated 2026-09-09 is a broad discovery/index source, not an authority for exact formulas. It is especially valuable for identifying linked evidence domains that require follow-up: 직업, 기술마법, 사냥터, 용어, 공성전, 반혼의 결서, 문제점, historical interfaces and community guides.

## 4. Visual canon

Do not create a new '어둠의전설풍' interpretation and call it original. Official/original gameplay imagery is the visual reference. AI-generated or procedural substitute art may exist only as clearly identified prototype material.

Do not use a whole gameplay screenshot as the final map texture. Final world architecture must support `Tile Map + Object + Collision + NPC + Monster Spawn + Portal` layers.

Original asset redistribution for shipping is licensing-gated. Keep reference/prototype assets distinct from distributable shipping assets.

Historical interface evidence shows that visual identity and information density matter: modernization must not become a generic mobile-RPG skin. Preserve readable HP/MP/EXP, character status, inventory/skill access and the world-first presentation while adapting touch targets and layout for mobile.

### User-confirmed playtest presentation requirements — `[ADAPTED]`

- Character/NPC/monster presentation must not dominate the mobile viewport. Logical world coordinates and collision dimensions remain independent from renderer scale.
- **The player character must never be presented as a front-facing avatar while idle/moving in normal field play.** PROJECT DARK uses the original isometric/diagonal presentation. Character appearance is rendered from `NW / NE / SW / SE`.
- Character animation/rendering must support distinct directional visual frames or image sets. Mirroring may be used only where visually valid and evidence-compatible.
- BODY / HAIR / EQUIPMENT / WEAPON / EFFECT layers share one directional anchor/facing contract.
- Player movement uses following camera/world scroll while preserving world↔screen correctness, map clamp, collision, portal and touch targeting.
- ATTACK / SKILL / MAGIC expose visibly distinguishable presentation states/effect hooks.
- Combat feedback must be legible and game-like; exact original styling remains evidence-gated.
- Current procedural/line-art buildings and props are fallback/debug material only. Production acceptance requires coherent sprite-based 2.5D art.

## 5. Movement and targeting canon

Original live-game presentation is four screen-diagonal directions `NW / NE / SW / SE`; historical 8-direction material is test/historical evidence and must not cause an 8-direction runtime regression. Logical four-neighbor movement maps to those screen diagonals.

The rendered character must face and animate in the matching diagonal direction. Mobile joystick/tap movement must issue the same logical movement command.

### Tap-to-move — USER CANON / `[ADAPTED]`

Tapping an eligible world point resolves to a traversable tile/node and the character **walks** there. It must never teleport or bypass collision/pathfinding/map bounds/portal rules. A new world tap replaces the previous target; direct movement overrides it; UI touches never leak into world movement; blocked targets terminate cleanly; successful taps receive brief target feedback.

NPC tap prioritizes selection/approach/dialogue. Empty eligible world space must also support tap-to-move.

### Targeting model

Source evidence distinguishes techniques from magic: techniques commonly resolve by facing/range, while targeted magic may require an explicit target. PROJECT DARK therefore keeps `action targeting mode` as data (`SELF / FACING_AREA / SELECTED_TARGET / GLOBAL` or equivalent) instead of forcing every action through one target model. Desktop pre-target/key-macro ergonomics are evidence of the original control burden, **not** a requirement to reproduce PC key macros on mobile. Mobile quick slots and target lock may adapt the interaction while preserving action semantics.

## 6. Character action states

Minimum common states: `IDLE / WALK / CAST / ATTACK / SKILL / HIT / DEAD`.

All field-capable states retain `NW / NE / SW / SE` facing where applicable. CAST uses a generic raise-hand/hands action unless stronger evidence exists. Weapon attack families and martial-artist unarmed/kick actions remain evidence-gated where exact animation is unknown.

## 7. Core stats, attributes and combat rules

Source inventory now explicitly includes five base stats: `STR / INT / WIS / CON / DEX`. Preserve them as separate concepts. `[FAN]` evidence associates STR with basic attack/carry weight, INT with magic attack, WIS with healing/MP recovery and MP growth, CON with defense/HP recovery and HP growth, and DEX with evasion/basic-attack critical chance. Exact formulas and thresholds require stronger evidence before becoming numeric canon.

Level 1–99 stat allocation, skill/magic stat prerequisites, post-99 EXP-based stat purchase and per-job purchase caps are source-backed design inventory `[FAN]`; exact costs/caps remain PENDING until corroborated.

Attributes/elements are a first-class system, not a cosmetic damage type. Source inventory identifies nine: neutral, water, earth, wind, fire, wood/forest, metal/steel, dark and light/life. Monster attribute can vary even among the same monster in the described system, and thief `센스` is evidence of party information asymmetry. Exact multipliers/compatibility rules remain evidence-gated.

Separate action data, animation, input, targeting and effect resolution with a stable `actionId`. Skills and magic are distinct. Do not apply one universal MP-cost/target/cooldown rule. Unknown official probability/defense/element formulas remain PENDING/B.

## 8. Jobs and party identity

The five base jobs are not interchangeable combat skins. Preserve strong role differentiation and party interdependence as a core identity. Job advancement, pure-job paths and later advancement must be represented as progression relationships, not flattened into one linear class ladder.

The source also documents the historical downside of extreme party dependence. PROJECT DARK should preserve role synergy without deliberately reproducing hours-long party assembly or making solo onboarding impossible. Any accessibility mitigation is `[ADAPTED]` and must not erase class identity.

## 9. RPG/data canon

STR/CON/INT/DEX/WIS, equipment modifiers, normal EXP/level, HP/MP growth, ability/progression state, elements, item/equipment identity, quest state and rewards are separate data concepts.

Item/monster/skill/NPC/map IDs and relationships already present in Master DB or canonical extracted data take precedence over hard-coded Java values.

### Monster reward delivery — USER CANON / `[ADAPTED]`

Canonical runtime flow:
`MONSTER_DEFEATED → reward resolution → direct inventory grant → EXP/Gold/quest progression`.

Resolved monster item rewards are granted directly to inventory exactly once. There is no ground-drop/pickup subsystem in target PROJECT DARK runtime. Unknown reward probabilities/quantities/relationships remain PENDING.

## 10. Progression and long-term growth

Experience is not only a conventional level bar in the long-term design inventory. Source evidence describes post-99 EXP expenditure for stat purchase and HP/MP growth at specific temples, with strength heavily tied to accumulated HP/MP. These relationships must be modeled explicitly rather than hard-coded as ordinary level-up-only growth. Exact costs/formulas remain PENDING.

Current implementation scope still ends at first advancement unless user expands it. Later systems (2nd/3rd advancement, Ability, Ether/enhancement and high-tier regions) are recorded as future evidence inventory, not current acceptance gates.

## 11. Quest, diary and world canon

Preserve original quest start conditions, NPC dialogue sequence, objectives, completion dialogue and rewards when evidenced. Do not invent lore/NPC relationships/town facts to fill gaps.

The original-style **일기장** is now a required design-domain concept: completed quests/events can leave character-history records using the world's in-fiction chronology (e.g. seasonal `세오` dating) when exact evidence is available. It is not merely a generic mobile achievement log.

World architecture must support regions, towns, hunting grounds/dungeons, social spaces and transitions. Source inventory explicitly includes eastern continent `마이소시아` and western continent `메데니아`; later regional progression must not be detached from world/lore relationships.

First content objective remains a coherent original-based Milles/start-region vertical slice. Source-backed geometry is preferred; unverified connective geometry is `[ADAPTED]/[B]` and replaceable.

## 12. Content taxonomy

Maintain explicit data/content categories even when not yet implemented:
- PvE: hunting grounds plus later quick dungeon, dimensional rift, Bekna Tower, Water Temple, Chaos Tower, boss raids.
- PvP: guild battle and battle royale; historical/retired modes remain archival evidence unless deliberately restored.
- Social/non-combat: amusement park/social spaces, gathering/fishing, museum/lore-style content where corroborated.
- World/BGM association: region-specific BGM identity is part of content metadata, but audio files and exact rights are separately gated.

Do not implement every listed modern/retired system merely because it appears in a fan index. Each needs era/scope/evidence classification.

## 13. Death, durability and economy

Historical death penalties and durability are evidence domains, not defaults to copy blindly. Source material records very punitive historical item loss/destruction and durability destruction that were later improved. PROJECT DARK must version these rules by era/evidence before selecting a mobile rule. Do not accidentally ship the harsh historical version from a stray reference.

Likewise, later cash-shop inflation, paid growth accelerators and extreme economic barriers are historical lessons, not target product requirements. Monetization is not inferred from the original merely because it existed.

## 14. Mobile UX canon

World remains visually central. Approved mobile shell: compact party/quest information, target HP, minimap, utility access, translucent/expandable chat, HP/MP/EXP, joystick/tap movement and quick action slots.

Preserve original information semantics while adapting interaction: techniques vs magic remain distinguishable; explicit target selection remains available where an action requires it; quick slots replace keyboard-number dependency; mobile target-lock/pre-target may reduce repeated taps. Do not reproduce PC-only keymap/macro complexity as mandatory gameplay.

The 2022-style lesson in the supplied source is explicit: a more modern UI can still fail if it loses information visibility, control efficiency or design identity. PROJECT DARK modernization must therefore be usability-driven, not generic-RPG imitation.

## 15. AUTO canon

AUTO uses the same combat math and reward path as manual play; no AUTO damage bonus. Manual input overrides AUTO. Monster rewards use the direct-inventory contract; AUTO contains no loot-navigation/pickup subsystem.

The source's history of automation/macros is not design authorization for unattended exploit-like behavior. PROJECT DARK AUTO is a first-party bounded convenience system with normal combat rules.

## 16. Production priority and governance

**Knowledge scope and implementation scope are different.** The source inventory can be broad while the active sprint remains narrow.

Current P0 remains:
1. player character visibly correct on-device (`24×32`, scale `1.50`, `NW/NE/SW/SE`, martial artist reference);
2. exact adjacent isometric movement at the latest user-approved timing;
3. one coherent sprite-based Milles village screen with production-quality ground/buildings/props;
4. stable startup/build/install path.

Do not pause P0 to implement quick dungeons, PvP, macros, later advancement, economy, fishing, gathering or other newly catalogued systems.

Before coding, agents read this file, `design/PLAYTEST_CANON_20260910_2149.md`, `design/LOD_SYSTEM_EVIDENCE_20260911.md`, `design/DATA_CONTRACT.md`, `design/SOURCE_OF_TRUTH.md`, canonical data and relevant DEV_HISTORY/handoffs.

If source documents disagree or required data is absent, record `DESIGN_CONFLICT` or `PENDING`; do not silently decide canon. User-approved/rejected rules supersede fan evidence. The Integrator is the final Design Compliance Gate; compiling code that violates this constitution is failed integration.