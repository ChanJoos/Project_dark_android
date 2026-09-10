# PROJECT DARK — DEV HISTORY PASS 24 · RPG / INVENTORY DETAIL + POTE REWARD BOUNDARY

Date: 2026-09-10
Role: RPG · Progression

## Source-of-Truth gate executed

Re-read current GitHub main before implementation:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest RPG PASS 23.

Direct monster reward delivery remains authoritative:
`MONSTER_DEFEATED -> reward resolution -> inventory mutation`.
No ground-drop/pickup behavior was restored.

## Concurrent Pote state re-audit

Combat PASS 25 added an isolated `PotePrototypeWorldDef` that can instantiate a real canonical Monster_ID `POTE_PURPLE` using explicitly `[B]/[ADAPTED]` prototype placement and the existing `[B]` combat profile.

Important boundary retained:
- canonical map/monster fact: `MAP_POTE_01 -> POTE_PURPLE`;
- prototype placement: player `(480,320)`, monster `(480,220)`, count 1;
- prototype runtime HP: 60;
- canonical POTE_PURPLE HP/EXP remain unresolved;
- current normal `RuntimeState`/`GameView` still boots Milles, so Pote is not mixed into Milles.

## Implementation completed

### 1. Visible inventory detail panel

Updated `GameView` to v0.67 and wired the PASS 23 RPG presentation fields into the actual inventory panel.

Selected items now display:
- canonical item name and equipment slot;
- centralized current requirement label (`장착 가능`, level/job failure, or PENDING);
- resolved allowed-job set / common / PENDING;
- attack or defense element identity when present;
- equipment result feedback without duplicating requirement truth in GameView.

The HUD level label now reads `state.rpg().normalLevel()` rather than a literal `Lv 1` string.

Commit:
- `1d5cfda12bd81a465e12970015eaf30ed03c90c8` — `Show RPG requirement and element details in inventory`.

### 2. Pote prototype defeat -> RPG reward boundary regression

Added `PotePrototypeRewardBoundaryAudit`.

The audit constructs the isolated prototype spawn, emits one `MONSTER_DEFEATED` event for canonical ID `POTE_PURPLE`, and runs it through `RpgProgressionState.consumeCombat()`.

Expected and enforced result with current evidence:
- exactly one reward-resolution record;
- status `PENDING_NO_CANONICAL_MONSTER_REWARD`;
- EXP remains null;
- auto-looted item map remains empty;
- inventory remains empty;
- normal EXP remains null.

This proves a canonical monster identity exercised through prototype combat cannot silently fabricate reward data.

Commit:
- `a8430b7f6fd3409f67b4fec168eed05b6c9c42ae` — `Add Pote prototype reward boundary audit`.

### 3. Runtime fail-fast enforcement

`RuntimeState` now executes `PotePrototypeRewardBoundaryAudit.verify()` during initialization, alongside existing reward/idempotency/spawn/equipment/readiness audits.

Commit:
- `c3c97a673b11a01a63140439d7fc236139f9b194` — `Enforce Pote prototype reward boundary`.

## Validation

### Inventory UI commit
GitHub Actions run #182 (`34438456223`) for `1d5cfda12bd81a465e12970015eaf30ed03c90c8` completed successfully.

Successful substantive gates:
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS

### Reward-boundary enforcement
The direct run #184 was queued while another main run executed. A direct descendant commit `ad83bd5930680a234ee5f64415b6343b390240b1` has parent `c3c97a673b11a01a63140439d7fc236139f9b194`; its GitHub Actions run #185 (`34438620885`) validated the reward-boundary code in ancestry.

Observed successful substantive gates on run #185:
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS

## DESIGN_CONFLICT / PENDING

### RESOLVED: INVENTORY_DETAIL_PRESENTATION_LINK
RPG-owned requirement/job/element metadata is now visible in the runtime inventory panel without reimplementing canonical equip rules in the renderer.

### RESOLVED: POTE_PROTOTYPE_REWARD_FALSE_PROMOTION_GUARD
A prototype POTE_PURPLE defeat can traverse the RPG resolution boundary while remaining explicitly reward-PENDING and granting no fabricated EXP/item.

### PENDING: POTE_RUNTIME_WORLD_SELECTION
Current normal runtime still boots Milles. A deliberate runtime-world selection abstraction is required before the isolated Pote prototype can become interactively playable without mixing maps.

### PENDING: POTE_PURPLE_CANONICAL_STATS_AND_REWARD
Canonical POTE_PURPLE HP/EXP/reward facts remain unresolved. The `[B]` prototype combat profile is not canon.

### PENDING: POTE_CANONICAL_PLACEMENT
The isolated Pote placement/count are `[B]/[ADAPTED]`, not original tile coordinates/population.

### PENDING: DROP_RATE_AND_QUANTITY
POTE_SPIRIT has a major-drop identity relation to `IT_RING_THREELINEGOLD`, but probability and quantity remain unresolved. No deterministic item emission is allowed.

### PENDING: NORMAL_EXP_MUTATION
Normal EXP start/threshold/stat-point idempotency remains unresolved; resolved monster EXP facts are not yet applied to live progression.

### PENDING: SHIELD_ALLOWED_JOB_SET
The exact job-code set behind `방패 가능 직업` remains unresolved.

### PENDING: ITEM_STAT_MODIFIERS
Unsupported equipment stat values remain absent.

## Next RPG bottleneck

1. Work with the new runtime-world abstraction when available so Milles and the isolated Pote prototype can be selected without mixing world data.
2. Once Pote prototype runtime activation exists, run a true playable `POTE_PURPLE -> MONSTER_DEFEATED -> reward PENDING` end-to-end path and expose the result in the inventory/reward UI.
3. Continue to refuse POTE_PURPLE EXP/items until canonical reward data is recovered.
4. If authoritative probability/quantity becomes available for a mapped reward, route it only through the existing direct-inventory auto-loot endpoint.
5. Resolve normal EXP thresholds/start representation before applying resolved monster EXP to the player.
