# PROJECT DARK — DESIGN CONSTITUTION

Status: CANONICAL / Source of Truth gate
Updated: 2026-09-10

## 1. Product canon

Core principle: **세계는 원작 그대로. 성장은 원작 그대로. 캐릭터는 원작 그대로. 조작과 UX만 모바일에 맞게 조정한다.**

Development must preserve original proper nouns, job roles, acquisition conditions, currency meaning, quest structure, UI information architecture, and pixel-art proportions when supported by evidence. Mobile changes must be explicitly tagged `[ADAPTED]`.

## 2. Runtime scope — immutable unless user explicitly changes it

`캐릭터 생성 → 평민 Lv1 → 밀레스/초반 성장 → 5개 기본직업(전사/도적/마법사/성직자/무도가) → Lv99 지존 → 전직 OR 순수직업 → 다시 Lv99 → 1차 승급`

Out of scope: 2차 승급, 2차/3차 직업 and later progression. Do not silently expand scope.

Character creation does not select a job. Preserve gender/hair/hair-color/name creation flow; job is selected after starting as 평민.

## 3. Evidence policy

Every original-game claim/value should retain evidence status:
- `[O]`: official/original evidence
- `[V]`: verified old official-hosted/community experiment or directly verified observation as applicable
- `[U]`: user-confirmed recollection
- `[B]`: balanced reconstruction/prototype value, NOT an original fact
- `[ADAPTED]`: deliberate mobile adaptation
- `PENDING` / `PENDING_CROP`: insufficient evidence or asset identification
- `FAN`: fan evidence; never silently promote to official

If exact original HP, damage, timing, drop rate, formula, sprite frame count, map geometry, etc. is unknown, do not invent it as canon. Keep it null/PENDING or isolate a prototype `[B]` value.

## 4. Visual canon

Do not create a new '어둠의전설풍' interpretation and call it original. Official/original gameplay imagery is the visual reference. AI-generated or procedural substitute art may exist only as clearly identified prototype material.

Do not use a whole gameplay screenshot as the final map texture. Final world architecture must support `Tile Map + Object + Collision + NPC + Monster Spawn + Portal` layers.

Original asset redistribution for shipping is licensing-gated. Keep reference/prototype assets distinct from distributable shipping assets.

## 5. Movement canon

Original presentation uses four screen-diagonal directions `↖ ↗ ↙ ↘`. Logical movement can remain four-neighbor grid movement because isometric projection maps logical axes to those screen diagonals. Do not 'fix' this into 8-neighbor movement without evidence.

Mobile joystick/tap movement must issue the same logical movement command. NPC tap may pathfind and approach before interaction.

## 6. Character action states

Minimum common states: `IDLE / WALK / CAST / ATTACK / SKILL / HIT / DEAD`.

CAST uses a generic raise-hand/hands casting body action unless verified original material says otherwise; spell identity should primarily come from effect/data.

Weapon attack families: `SWING / THRUST / SHOT(or THROW) / PUNCH`. Martial artist requires a dedicated `KICK` action. Prototype timings are `[B]` until measured.

## 7. Combat rules

Separate action data, animation, input, and effect resolution but connect them with a stable `actionId`.

Action validation order should preserve: alive/state control → target type → distance → line of sight → resource → acquisition/usage condition → cooldown. Apply cost/cooldown once and resolve damage/heal/status at the defined action event.

Skills and magic are distinct. Do not apply one universal MP-cost rule to all skills. AC is not a generic 'higher is better' stat. Unknown official probability/defense/element formulas remain PENDING/B.

## 8. RPG/data canon

STR/CON/INT/DEX/WIS, base stats, equipment modifiers, normal EXP/level, ability/progression state, item/equipment identity, quest state and rewards must be separate data concepts. Do not collapse them for implementation convenience.

Item/monster/skill/NPC/map IDs and relationships already present in Master DB or canonical extracted data take precedence over hard-coded Java values.

## 9. Quest/world canon

Preserve original start conditions, NPC dialogue sequence, objective conditions, completion dialogue and rewards when evidenced. Quest state must distinguish undiscovered/available/in-progress/completable/rewarded/abandoned or equivalent explicit states. Do not invent lore, NPC relationships or town facts to fill gaps.

First content objective is a coherent original-based starting-region/Milles vertical slice with connected NPC, quest and hunting loop, then expansion.

## 10. Mobile UX canon

World remains visually central. Approved mobile shell: compact party/quest information, target HP top-center, minimap top-right, utility rail, translucent/expandable chat, HP/MP/EXP bottom-center, circular joystick bottom-left, frequent quick slots + attack + AUTO bottom-right. Do not restore giant placeholder HUD boxes or a permanently expanded PC-sized slot grid.

## 11. AUTO canon

AUTO uses the same combat math as manual play; no AUTO damage bonus. Manual input overrides AUTO and may resume after ~1.2s `[ADAPTED]`. Repeated path failure must terminate/recover rather than loop forever. Loot is a ground-world interaction, not direct inventory teleportation.

## 12. Agent governance

Before coding, every development agent must read this file, `design/DATA_CONTRACT.md`, `design/SOURCE_OF_TRUTH.md`, canonical data under `data/design/`, Visual Manifest material, and `docs/DEV_HISTORY*`.

Existing canonical IDs/values/relationships must not be rewritten for convenience. If source documents disagree or required data is absent, record `DESIGN_CONFLICT` or `PENDING`; do not silently decide canon.

The Integrator is the final Design Compliance Gate. A compiling implementation that violates this constitution is a failed integration.