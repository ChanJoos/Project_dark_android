# PROJECT DARK — DEV HISTORY PASS 16 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster

## Source-of-Truth gate executed

Re-read before implementation:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest available PASS history including PASS 14 RPG and PASS 15 Combat/Monster.

Current Source of Truth places `master/data/*.csv` above historical convenience projections. No Master row was modified.

## Master audit result

Inspected `master/data/Monster_Master.csv`, `Monster_Sources.csv`, `Map_Instance_Master.csv`, `Progression_Master.csv`, current `WorldDef`, and concurrent RPG reward projection.

Findings:
- `MAP_MILLES` is a town and has no canonical monster spawn relation.
- `MAP_MILLES_D10` names 해골지네/암흑소환석 in its spawn/room description, but those names do not currently have a positively matched stable Monster_Master ID + HP/EXP/drop row suitable for runtime binding.
- `POTE_SPIRIT` is positively identified in Monster_Master as 포테의정령 / 포테의숲 / Lv48 / HP 29201 / EXP 308950 / Evidence V.
- Region mismatch is preserved. `POTE_SPIRIT` was not substituted into Milles.
- `combat_dummy_01` remains a [B] runtime fixture with no canonical reward.

## Implementation completed

Added `MonsterDefinition.java` as the Combat·Monster-owned evidence-safe monster definition contract. Unknown fields are nullable and definition status distinguishes CANONICAL / PROTOTYPE_PENDING / UNRESOLVED.

Added `MonsterDefinitionRegistry.java` as a runtime projection boundary:
- registers `POTE_SPIRIT` using the exact verified Master identity/region/Lv/HP/EXP values;
- registers `combat_dummy_01` explicitly as `PROTOTYPE_PENDING` with no canonical Lv/HP/EXP/drop fields;
- unresolved IDs return an explicit PENDING definition rather than receiving plausible defaults.

Updated `MonsterAIController` to resolve each runtime monster through the registry before AI execution. Existing [B] chase/attack behavior now executes only for explicit `PROTOTYPE_PENDING` fixtures. Canonical or unresolved monsters do not silently inherit the dummy AI profile; they remain inert until an evidenced AI/action profile is projected.

This preserves current playable dummy behavior while preventing future Master-backed monsters from accidentally receiving prototype AI assumptions.

Gameplay commits:
- `4a9f0877e982716c3c846eb879cbed32770959d1` — add MonsterDefinition contract.
- `068d3d71faf62c510acf3b6ed77391542d873064` — add Master-backed MonsterDefinitionRegistry.
- concurrent RPG commit `cfa8e97788c418b68250a3c9f9150dcb7dc87f7c` added `CanonicalMonsterRewardCatalog` and was preserved in main ancestry.
- `574f090adc3451283491718b14c6f63d30ab40d9` — wire MonsterAIController through definition registry.

The Combat definition registry and RPG reward catalog remain separate ownership concerns: Combat owns monster identity/AI projection; RPG owns reward projection.

## Validation

GitHub Actions run #91 for `574f090adc3451283491718b14c6f63d30ab40d9` reached all substantive gates successfully:
- Android SDK/build-tools setup: SUCCESS
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS

No compile correction was required.

## DESIGN_CONFLICT / PENDING

### PENDING: MILLES_DUNGEON_MONSTER_ID_MAPPING
`MAP_MILLES_D10` contains display names 해골지네/암흑소환석, but current inspected Master data does not expose a sufficiently authoritative stable Monster_ID + combat/reward mapping for them. Do not invent IDs or copy another region's monsters.

### PENDING: CANONICAL_MONSTER_AI_PROFILE
Master-backed monsters must not inherit the current `[B]` dummy chase radius, ranges, speed, damage or cooldown. Per-monster detect/chase/attack/action data remains to be projected from supported evidence.

### PENDING: DROP_RATE_AND_QUANTITY
Major-drop text is not a deterministic drop table. Concurrent RPG catalog correctly leaves probability and quantity unresolved. Combat does not emit world drops from textual hints.

### PENDING: WANDER_DETECT
WANDER/DETECT remain declared contract states only until evidence-safe behavior is available.

## Result

The combat runtime now has an evidence-aware boundary between prototype monsters and canonical Master monsters. The current dummy remains playable, but a future canonical monster cannot silently receive prototype AI or fabricated reward data.

## Next Combat · Monster bottleneck

1. Expand the MonsterDefinition projection from selected hand-bound rows toward a generated/read-only projection of relevant `Monster_Master` rows without altering Master.
2. Establish a positively supported Milles dungeon Monster_ID mapping for 해골지네/암흑소환석 if the source tables/evidence can support it.
3. When a canonical runtime spawn exists, bind its evidenced AI/action profile and only then activate canonical reward resolution through the RPG catalog.
4. Keep unknown detect range, attack timing, damage formula, respawn timing and drop rates PENDING/B until evidence exists.
