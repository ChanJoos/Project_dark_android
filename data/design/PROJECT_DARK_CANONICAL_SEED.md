# PROJECT DARK — CANONICAL DESIGN SEED

Purpose: machine-readable-by-agents design projection while exact latest Master DB binary is pending repository import.

## Scope
- Start: character creation → Commoner Lv1.
- Jobs: Warrior, Rogue, Mage, Cleric, Martial Artist.
- Runtime ceiling: Lv99 master → job-change OR pure-job path → Lv99 → first advancement.
- Exclude second advancement and 2nd/3rd jobs.

## Character creation
Preserve gender, hair, hair color and name. Do not select job at creation. Character begins as Commoner.

## Jobs
- Warrior: high HP / strong physical attack; HP-consuming finishing actions must model risk separately.
- Rogue: stealth, critical attack, monster element inspection, smoke/control are distinct mechanics.
- Mage: multi-target magic and MP management; do not collapse into melee warrior behavior.
- Cleric: healing, protection, cleanse and ally buffs have distinct target/effect semantics.
- Martial Artist: barehand, kick, leap and paralysis/control require distinct action support; KICK is a dedicated animation family.

## Stats/progression
Preserve STR/CON/INT/DEX/WIS as separate base stats and separate equipment modifiers. Normal level/EXP and advancement/ability progression are distinct fields. Do not finalize unknown HP/MP growth formulas without evidence. Equipment must not permanently mutate base stats.

## Combat/action model
Common action states: IDLE, WALK, CAST, ATTACK, SKILL, HIT, DEAD.
Attack animation families: SWING, THRUST, SHOT/THROW, PUNCH; Martial Artist additionally KICK.
Validate action request in order: alive/control state → target type → distance → line of sight → resource → acquisition/usage condition → cooldown. Costs/cooldowns are applied once. Effects resolve at a defined action event. Skill effects, animation and input remain separate but share actionId.
Skills are not universally MP-consuming. Magic uses MP where defined. Unknown damage/hit/defense/element formulas remain PENDING or B.

## Movement
Logical four-neighbor grid movement projects to screen-diagonal NW/NE/SW/SE. Mobile joystick and tap movement feed the same command system. Preserve collision and pathfinding. Rendering should interpolate movement rather than merely teleport logical tile coordinates.

## World/map
Target architecture: Tile Map + Object + Collision + NPC + Monster Spawn + Portal.
Whole screenshots are golden/reference evidence, not final map textures. First vertical slice should be original-based starting/Milles content with NPC/quest/hunting connectivity.

## Quest
Model explicit lifecycle states and prerequisites. Objectives can include kill/collect/visit/talk. Completion/reward must be idempotent. Save/load preserves branch, abandon/reset semantics and reward state. Unknown original dialogue/lore stays PENDING rather than invented.

## Items/equipment
Canonical item data takes priority over hard-coded placeholders. Preserve item identity, slot, job/level restriction, element, acquisition and evidence. Attack element via necklace and defense element via belt are core equipment semantics where supported. Prototype free element-cycle UI is not canon.

Available earlier Item Master evidence includes named equipment such as 뉴비세트, 로오의반지, 칸의목걸이, 가죽장갑/각반, elemental necklace families, elemental belts, class weapons and first-advancement rewards. Agents must not invent missing exact AC/DAM/HIT/drop-rate values.

## Death/revival
Death event executes once. Ghost transition, revival place/condition/cost and related original systems are separate concepts. Prototype 'lose 10% Gold and return to camp' is NOT original canon.

## UI/mobile
Approved shell: compact party and quest tracker; target HP top-center; minimap top-right; right utility rail; translucent/expandable chat; HP/MP/EXP bottom-center; circular joystick bottom-left; quick slots + attack + AUTO bottom-right. Do not restore giant placeholder HUD panels.

Original unified slot system identity should be preserved while adapting permanent screen footprint for mobile.

## AUTO
AUTO orchestrates existing systems: target search → move/approach → combat → skill/survival → world loot pickup. It uses the same combat resolver as manual play and gets no damage bonus. Manual input overrides AUTO; resume around 1.2 s is `[ADAPTED]`. Repeated blocked-path failures must recover/stop instead of infinite looping.

Party priority when applicable: Mage curse/debuff → Rogue sense/element → damage dealers → Cleric heal/protection. Dangerous HP/MP-consuming skills require opt-in. Loot priority design: Quest > Rare > Material > Consumable > Gold > Common.

## Visual rules
- No AI reinterpretation replacement for final original visuals.
- No whole screenshot as final map texture.
- Individual monster sprites remain PENDING_CROP until visually identified.
- Mobile adaptation changes input/UI layout, not world/character canon.
- Original asset redistribution is licensing-gated; prototype/reference and shipping assets remain separated.

## Evidence
Use O / V / U / B / ADAPTED / FAN / PENDING_CROP exactly as defined in DESIGN_CONSTITUTION. Never silently upgrade evidence.

## Immediate playable-slice integration target
`movement → target → approach → attack/magic/skill → monster counterattack → death → EXP → ground drop → pickup → inventory → equip → stat recomputation → next hunt`

Agents should close missing links in this chain before broad content expansion unless a dependency requires otherwise.