# PROJECT DARK — DEV HISTORY PASS 18 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster / direct auto-loot regression

## Source-of-Truth gate executed

Re-read current main design/runtime evidence before implementation, including:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest Combat history `docs/DEV_HISTORY_PASS_17_COMBAT_MONSTER.md` and current concurrent World/RPG history.

Current canonical monster reward path remains:
`MONSTER_DEFEATED -> reward resolution -> direct inventory mutation`.
Ground item entities and pickup behavior remain retired.

## Playable-world canonical monster audit

Re-checked Milles linkage before replacing the prototype monster.

Result:
- `MAP_MILLES` still has no supported canonical monster spawn relation.
- `MAP_MILLES_D10` names 해골지네/암흑소환석, but current evidence still does not positively join those names to a stable `Monster_Master` ID + runtime spawn + authoritative reward row.
- `POTE_SPIRIT` remains region-bound to 포테의숲 and was not substituted into Milles.
- `combat_dummy_01` therefore remains a rewardless `[B]` fixture.

No region or monster identity was invented.

## Canonical reward item projection

Concurrent RPG work landed during this pass and already expanded `RpgProgressionState` with Item_Master-backed definitions needed by the current canonical reward catalog:
- `IT_RING_THREELINEGOLD` / 세줄금반지 / 반지 / 요구 Lv11.
- `IT_RING_SILVERAQUA` / 실버아쿠아링 / 반지 / 요구 Lv51.

These identities/requirements match current Item_Master. Unknown stat modifiers remain empty rather than invented.

## Implementation completed

Added `app/src/main/java/com/projectdark/mobile/RewardPipelineAudit.java`.

The audit uses an isolated `RpgProgressionState` and verifies:
1. every item ID referenced by current `CanonicalMonsterRewardCatalog` has a registered ItemDefinition;
2. a valid resolved canonical item passed to `autoLootResolvedItem` increments inventory directly;
3. zero quantity fails as `INVALID_QUANTITY` without mutation;
4. unknown item ID fails as `INVALID_ITEM` without mutation;
5. current unresolved reward hints have not silently become deterministic (`hasNoInventedDropEmission`).

This isolated quantity=1 probe is a runtime contract test only. It does **not** assert a canonical monster drop quantity or rate.

Commit:
- `47bfc5db5cea80b212f4bd009c47d75fc4825e4f` — `Add direct auto-loot runtime audit`.

Integrated the audit into `RuntimeState` construction so a broken direct-auto-loot contract fails closed during runtime initialization instead of silently restoring a legacy ground-loot path.

Commit:
- `4e27a8a7186e9d9c16d2d610505e22935c89ff66` — `Enforce auto-loot runtime contract audit`.

## Validation failure and concurrent correction

GitHub Actions Run #110 for `4e27a8a...` passed `Validate Master DB` but initially failed Java compilation.

The compile error was unrelated to auto-loot logic: concurrent World/Character work had extended `CharacterRenderer.Pose` with an `EffectFamily` argument while the then-checked-out `GameView` still called the previous constructor signature.

Observed compiler failure:
- `GameView.java:144`
- `CharacterRenderer.Pose` required an additional `EffectFamily` argument.

Concurrent World work then integrated the intended action-effect routing in commit:
- `684f000e84eb1936880d6387b66e89dd46de5a3d` — `Route player effects through CharacterRenderer`.

The current GameView now maps CAST / THROW / PUNCH / KICK / SKILL / HIT to `CharacterRenderer.EffectFamily` and supplies the new Pose parameter. The Combat auto-loot audit commits remain in ancestry.

## Final validation

GitHub Actions Run #112 (`34435510337`) for `684f000e84eb1936880d6387b66e89dd46de5a3d` completed `success`.

Verified gates:
- Android/Gradle setup: SUCCESS
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS
- workflow conclusion: SUCCESS

Thus the direct-auto-loot audit is compile/build compatible with the latest concurrent World/Character integration.

## DESIGN_CONFLICT / PENDING

### PENDING: CANONICAL_MILLES_MONSTER_RUNTIME_LINK
No evidence-safe stable ID/spawn/reward chain is yet available for the Milles playable combat fixture. Do not move a monster from another region merely to exercise rewards.

### PENDING: DROP_RATE_AND_QUANTITY
Current major-drop identities in the reward catalog still have null probability/quantity. Direct auto-loot does not convert those hints into guaranteed rewards.

### PENDING: REAL_MONSTER_AUTO_LOOT_E2E
The direct inventory endpoint and runtime audit are functional, but a real playable canonical monster defeat cannot yet demonstrate an evidence-backed item grant end-to-end until both playable spawn identity and reward quantity/probability are resolved.

### RETIRED: GROUND_LOOT / PICKUP
No ground entity, pickup range, pickup input, loot navigation, or AUTO pickup path should be restored.

## Result

The new architecture is now guarded at runtime: canonical reward item identities must be registered, resolved rewards can enter inventory directly, invalid inputs fail closed, and unresolved drop data cannot silently become deterministic. The current Milles prototype remains rewardless rather than fabricating canon.

## Next Combat · Monster bottleneck

1. Resolve an evidence-backed Milles/starting-region monster stable ID + spawn relation through Master/accepted changes.
2. Resolve authoritative item probability/quantity where source evidence exists.
3. Once both are supported, exercise `MONSTER_DEFEATED -> reward resolution -> autoLootResolvedItem -> inventory` with a real runtime monster and add an idempotency regression proving one defeat grants at most once.
4. Keep all ground-loot/pickup code retired throughout that integration.
