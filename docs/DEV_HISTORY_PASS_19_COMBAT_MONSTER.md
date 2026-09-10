# PROJECT DARK — DEV HISTORY PASS 19 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster / defeat-reward idempotency

## Source-of-Truth / current-main audit

Started from current main after PASS 18 and re-checked the unresolved Milles monster relation before implementation.

Result remains evidence-constrained:
- `MAP_MILLES_D10` exposes display names 해골지네/암흑소환석, but current repository evidence still does not provide a safe stable Monster_ID + playable spawn + authoritative reward chain.
- `POTE_SPIRIT` remains a verified reward-catalog monster from another region and was not moved into Milles.
- `combat_dummy_01` remains rewardless `[B]`.
- Ground loot/pickup remains retired.

## Playable-slice bottleneck selected

PASS 18's next-step list required an idempotency regression proving one defeat sequence cannot grant more than once. This can be implemented without inventing Milles data, drop rates, quantities, or regions.

## Implementation

Added `MonsterDefeatIdempotencyAudit.java`.

The isolated audit verifies:
1. a canonical `POTE_SPIRIT` `MONSTER_DEFEATED` ledger event resolves once;
2. its verified EXP payload remains 308950;
3. its unresolved major-drop probability/quantity emits no item;
4. re-consuming the same ledger snapshot does not append a second reward resolution;
5. a later distinct defeat sequence resolves once as a distinct event;
6. inventory remains unchanged while authoritative drop emission is unresolved.

This uses `POTE_SPIRIT` only as an isolated reward-boundary probe. It does not spawn or relocate that monster into Milles.

Commits:
- `d9bbfb26a2aa5bde8cea14ba826fd0c951953d6b` — `Add monster defeat idempotency audit`
- `b5b620bb0e8fd9f0b0983f49e13d6f295c800271` — `Enforce defeat reward idempotency audit`

`RuntimeState` now fails closed at initialization if either the direct-auto-loot audit or defeat idempotency audit fails.

## Concurrent work handling

Concurrent World/RPG commits landed after the Combat implementation. No files owned by those commits were overwritten. Current main ancestry includes the Combat commits plus later presentation/inventory work.

## Validation

GitHub Actions Run #117 (`34435774698`) for `b5b620bb0e8fd9f0b0983f49e13d6f295c800271` completed `success`.

Verified:
- Android/Gradle setup: SUCCESS
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS
- workflow conclusion: SUCCESS

## DESIGN_CONFLICT / PENDING

### PENDING: CANONICAL_MILLES_MONSTER_RUNTIME_LINK
No evidence-safe stable ID/spawn/reward chain exists yet for the current Milles combat fixture. Do not invent one.

### PENDING: DROP_RATE_AND_QUANTITY
Verified major-drop identities still have unresolved probability/quantity. Auto-loot must not make them deterministic.

### PENDING: REAL_MONSTER_AUTO_LOOT_E2E
Exactly-once reward consumption is now guarded, but a real playable canonical monster cannot yet demonstrate item auto-loot until both playable spawn identity and authoritative drop emission are supported.

### RETIRED: GROUND_LOOT / PICKUP
Do not restore ground item entities, pickup range/input, loot navigation, or AUTO pickup movement.

## Result

The combat-to-RPG boundary is now protected against duplicate processing of the same `MONSTER_DEFEATED` sequence while preserving evidence-safe unresolved drops. One combat ledger defeat sequence can resolve at most once in RPG reward history.

## Next Combat · Monster bottleneck

1. Continue resolving a Master-backed starting-region stable Monster_ID + spawn relation.
2. If authoritative drop probability/quantity becomes available, project it without approximation and connect it to direct inventory auto-loot.
3. Once a playable canonical monster exists, add a true runtime defeat -> reward -> inventory end-to-end regression using that monster.
4. Keep prototype AI values `[B]` and ground-loot behavior retired unless evidence explicitly changes those contracts.
