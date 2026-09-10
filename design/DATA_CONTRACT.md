# PROJECT DARK — DATA CONTRACT

Status: CANONICAL CONTRACT

This document defines boundaries between design data and runtime code. Code consumes canonical data; code does not redefine canon.

## Common identity

All persistent/content entities require stable IDs. Display names are not IDs.

Core runtime concepts:
- `EntityId`
- `Position(tileX,tileY)`
- `Direction(NW,NE,SW,SE)`
- `ActionState(IDLE,WALK,CAST,ATTACK,SKILL,HIT,DEAD)`
- `TargetId`
- `Stats`
- `EvidenceTag(O,V,U,B,ADAPTED,PENDING,FAN)`

Unknown canonical fields must support `null/PENDING`; do not replace missing data with plausible-looking constants.

## MonsterDefinition

Required conceptual fields: `monsterId`, canonical/display name, evidence, region/spawn relation, visualAssetRef/PENDING_CROP, base stats when known, detection/AI profile, attack/action refs, EXP ref/value with evidence, dropTableId/reward relation, respawn policy.

Runtime monster instance state is separate: current HP, tile, state, target, timers. Never mutate MonsterDefinition to store instance state.

## ActionDefinition / Skill / Magic

Fields: `actionId`, name, kind, job/usage restrictions, acquisition conditions, target filter, range, resource type/cost, cast/prep, effect event, recovery, cooldown, animation family, effect refs, evidence.

`SKILL` and `MAGIC` must remain distinguishable. Unknown timings/formulas remain PENDING or prototype B values.

## ItemDefinition

Fields: `itemId`, canonical name, category, equip slot where applicable, job/level/circle restrictions, element, stat modifiers with evidence, acquisition source, monster/shop/quest reward relationships, visual ref, evidence.

Attack element via necklace and defense element via belt are first-class equipment semantics where supported by canonical design data. Do not implement them as free UI toggles and call that original behavior.

## PlayerState

Separate: identity/appearance, current job, normal level/EXP, progression/advancement state, base STR/CON/INT/DEX/WIS, equipment modifiers, derived combat stats, HP/MP, inventory, equipment, learned actions, quest states, Gold, map/position, save schema version.

Equipment changes modifiers/derived stats; it must not mutate permanent base stats.

## Progression

Allowed runtime progression graph:
`COMMONER → BASIC_JOB → LV99_MASTER → (JOB_CHANGE | PURE_JOB) → LV99 → FIRST_ADVANCEMENT`

Basic jobs: `WARRIOR / ROGUE / MAGE / CLERIC / MARTIAL_ARTIST`.

No node beyond first advancement may be added without explicit design approval.

## MapDefinition

Fields/layers: `mapId`, canonical name/status, tile/geometry data, object layer, collision layer, NPC placements, monster spawn placements, portal connections, visual refs/evidence.

Screenshot references are evidence/golden references, not final monolithic map textures.

## NPCDefinition

Fields: `npcId`, name/status, map placement, dialogue/interaction graph ref, shop/quest/service refs, visual ref, evidence.

## QuestDefinition

Fields: `questId`, title/status, prerequisites, start NPC, state graph, objectives (kill/collect/visit/talk), completion NPC/dialogue, rewards, abandon/reset policy, evidence.

Reward claim must be idempotent. Save/load must preserve branch and reward state.

## Monster rewards and inventory — CANONICAL

Combat emits `MONSTER_DEFEATED` exactly once for a valid monster death. RPG·Progression resolves only evidence-backed EXP/Gold/item relationships and applies resolved rewards through centralized mutation APIs.

Canonical item flow:
`MONSTER_DEFEATED → reward resolution → inventory mutation`.

There is **no world-drop/pickup stage**. Target runtime must not create a ground item entity for monster rewards and must not require proximity, pickup input, pickup pathfinding, or pickup animation before inventory mutation. AUTO and manual combat call the same reward-resolution and inventory APIs.

The previous contract `combat death → world drop entity → pickup validation → inventory mutation` is **RETIRED / SUPERSEDED (2026-09-10)**. It must not be implemented, tested as desired behavior, or used as a fallback. Existing code using that path is legacy code to remove/disconnect.

Full inventory, invalid item IDs, invalid quantity, unresolved probability/quantity, and unresolved monster→reward relationships must remain explicit outcomes. Direct inventory delivery does not permit deterministic or invented rewards. Defeat/reward processing must be idempotent so one monster death cannot grant the same reward twice.

## AUTO

AUTO is an orchestration layer over existing movement/target/combat/survival/reward APIs. It may not implement a second combat formula, separate loot formula, ground-loot navigation, or pickup subsystem. Manual commands have priority over AUTO.

## Save contract

Persist IDs and mutable state, not duplicated canonical definitions. Save data must be versioned and migration-aware. Invalid/missing content IDs must fail safely rather than silently map to another item/monster/action.

## Ownership boundaries

- World·Character owns map/entity presentation, character animation and world interaction plumbing; monster reward ground entities are not part of its target contract.
- Combat·Monster owns combat resolution/action execution and monster runtime AI, and emits one `MONSTER_DEFEATED` event.
- RPG·Progression owns definitions/state for items, inventory, equipment, stats, EXP, reward resolution and progression, including direct reward inventory mutation.
- Integrator·UX·QA owns mobile orchestration/UI, NPC/Quest/AUTO integration, cross-module contract enforcement and regression gates.

Cross-domain access should occur through stable contracts/data definitions, not by duplicating logic in GameView.