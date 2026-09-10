# PROJECT DARK — DEV HISTORY PASS 21 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster / canonical roster projection

## Source-of-Truth audit

Re-checked latest main and `master/data/Monster_Master.csv` after PASS 20.

Findings:
- No new evidence-safe stable Monster_ID + playable spawn relation was found for the current Milles prototype / `MAP_MILLES_D10` 해골지네·암흑소환석 references.
- Do not invent those IDs and do not relocate another region's canonical monsters into Milles.
- `Monster_Master.csv` does contain a substantial stable-ID roster for 포테의숲, including `POTE_RED`, `POTE_GREEN`, `POTE_PURPLE`, `POTE_SILVER`, `POTE_TREANT`, `POTE_ANTLION`, `POTE_GNOLL`, `POTE_WOLFRIDER`, `POTE_LYCAN`, `POTE_ANTGIANT`, `POTE_SILVERWOLF`, strong variants, `POTE_SPIRIT`, and `POTE_MANTIS`.
- Most Pote rows do not have authoritative Lv/HP/EXP values. Those values remain null/PENDING.
- `POTE_SPIRIT` remains the currently projected fully resolved stat row: Lv48 / HP29201 / EXP308950 / source evidence `V` / `STAT_VERIFIED`.

## Bottleneck selected

Before a real Pote world slice can replace the generic combat dummy, Combat needs an evidence-preserving stable-ID roster projection that can be consumed without fabricating missing combat numbers.

## Implementation

Added `PoteMonsterRoster.java` as a read-only Master projection for 16 포테의숲 monster rows.

The projection preserves:
- stable Monster_ID;
- monster name;
- region;
- zone/appearance text;
- nullable Lv/HP/EXP;
- raw source evidence text such as `V/FAN`, `O/V`, and `V`;
- validation status.

Mixed evidence is intentionally preserved verbatim rather than being flattened/promoted to a stronger single evidence enum.

Added `PoteMonsterRosterAudit.java`.

The audit verifies:
1. all 16 projected rows have stable identity and remain in 포테의숲;
2. source evidence and validation status are present;
3. duplicate names are rejected;
4. non-`POTE_SPIRIT` rows do not silently acquire a complete Lv/HP/EXP tuple;
5. `POTE_SPIRIT` stays exactly Lv48 / HP29201 / EXP308950 with `V` / `STAT_VERIFIED`;
6. `POTE_RED` keeps mixed `V/FAN` evidence and unresolved numeric stats.

Updated `MonsterSpawnAdmissionAudit.verify()` to also enforce the Pote roster projection audit at the Combat·Monster runtime boundary.

Commits:
- `3f26fb564da61cb385ff1602558f667408fb090d` — `Project canonical Pote monster roster`
- `0fd1c30770664023c998c0579f40691d6afe4f63` — `Add Pote roster projection audit`
- `8427ea77f75c95edb5fc1c30e0e5764f8015c37b` — `Validate canonical Pote roster at monster boundary`

## Concurrent work handling

Other RPG/World agents continued committing to main while this pass ran. The Combat commits were preserved in ancestry and no concurrent-owned file was overwritten except the Combat-owned `MonsterSpawnAdmissionAudit.java` integration point.

## Validation

GitHub Actions Run #133 (`34436427136`) for head `8427ea77f75c95edb5fc1c30e0e5764f8015c37b` completed successfully at the job level.

Verified successful steps:
- Android / Gradle setup
- Validate Master DB
- Compile debug sources
- Build debug APK
- Upload debug APK
- complete job: SUCCESS

## DESIGN_CONFLICT / PENDING

### PENDING: MILLES_DUNGEON_MONSTER_ID_MAPPING
`MAP_MILLES_D10` display-name references still lack a sufficiently authoritative stable Monster_ID + playable spawn mapping in the inspected data. Do not invent IDs.

### PENDING: POTE_PLAYABLE_WORLD_SPAWN
Pote stable monster identities are now projected, but current runtime world is still `milles_runtime_proto`. Do not place Pote monsters into Milles. A Master-backed Pote map/spawn relation is required before activation.

### PENDING: POTE_COMMON_MONSTER_STATS
Most Pote monsters have roster/behavior evidence but exact Lv/HP/EXP remain unresolved. Runtime combat HP/damage/reward numbers must not be fabricated from those rows.

### PENDING: CANONICAL_MONSTER_AI_PROFILE
Partial aggression/movement descriptions are evidence, but exact detect radius, chase speed, attack range, damage, windup, cooldown, and respawn timings remain unresolved unless separately supported.

### RETIRED: GROUND_LOOT / PICKUP
Monster item rewards remain direct-to-inventory after reward resolution. Do not restore world item drops or pickup navigation.

## Result

Combat now has a safe canonical starting-region roster substrate instead of only a single hand-bound `POTE_SPIRIT` definition. This materially reduces the work required to transition from `combat_dummy_01` to real monsters once a valid Pote world/spawn slice is available, without inventing missing combat values.

## Next Combat · Monster bottleneck

1. Inspect Master map/region/spawn tables for a positively supported 포테의숲 map instance and zone relation.
2. Build an evidence-safe Pote spawn manifest if those relations are sufficient.
3. Activate only monsters whose runtime HP/action profile can be supported; keep incomplete rows identity-only/inert rather than inventing stats.
4. Once a playable canonical monster exists, connect defeat -> canonical reward -> direct inventory end-to-end and add a runtime regression.
