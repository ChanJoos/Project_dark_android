# PROJECT DARK — DEV HISTORY PASS 11 · RPG/PROGRESSION

Date: 2026-09-10
Role: RPG · Progression

## Source-of-Truth gate executed

Read before implementation:
- `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
- `master/PROJECT_DARK_V0.6_PLAN_KO.md`
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- `docs/DEV_HISTORY.md`
- latest available passes including PASS 08 integration audit, concurrent World PASS 09, and Combat PASS 10.

The derived `data/design/PROJECT_DARK_CANONICAL_SEED.md` was not used to override either Master.

## Highest incomplete RPG chain addressed

PASS 08 identified the highest integration gap as:
`MONSTER_DEFEATED → canonical reward result → world drop → pickup → inventory → equipment → stat recomputation`.

Implemented `RpgProgressionState.java` as the RPG/Progression-owned runtime boundary and wired it into `RuntimeState.tick()` after the shared `CombatLedger` snapshot is created.

### Implemented contracts

1. Combat events are consumed exactly once by monotonic `CombatLedger.Event.sequence`.
2. Monster defeat now produces an explicit `RewardResolution` instead of being ignored by progression.
3. Reward resolution supports `PENDING_NO_CANONICAL_MONSTER_REWARD`; PENDING resolutions mutate no EXP, Gold, inventory, or world drops.
4. Ground drops are explicit world entities (`WorldDrop`) with stable runtime drop IDs, source monster IDs, item IDs, quantity, position and evidence metadata.
5. Pickup validates existence, item identity, distance and bounded inventory capacity before inventory mutation.
6. Inventory and equipment are separate state concepts.
7. Equipment uses canonical item identity/slot/requirement fields and never mutates base stats.
8. `recomputeStats()` separately projects base stats, equipment modifiers and totals.
9. Normal level and normal EXP are explicit separate fields and remain `null` until canonical progression initialization is wired; no plausible default level/EXP was invented.

## Canonical data used

The first catalog fixture is copied from Master `Item_Master`:
- `IT_GLOVE_LEATHER`
- `가죽장갑`
- equipment slot: `장갑`
- required level: `11`
- source: `SRC_GEAR_GUIDE`
- Master confidence/status: `SOURCE`

No equipment stat bonus was invented because the Master row used in this pass does not provide one.

## DESIGN_CONFLICT / PENDING

### PENDING: COMBAT_DUMMY_REWARD_MAPPING
Current runtime monster `combat_dummy_01` / `훈련용 몬스터 [B]` is an explicit prototype fixture and is not a canonical Master monster ID. Therefore this pass intentionally does **not** assign it canonical EXP, Gold or `Monster_Drop` rows.

Resolution:
- `MONSTER_DEFEATED` is consumed by RPG state.
- result is `PENDING_NO_CANONICAL_MONSTER_REWARD`;
- EXP remains unchanged/null;
- Gold remains unchanged/not introduced;
- no world drop is spawned.

This follows the Constitution/Data Contract rule that unknown original values remain PENDING rather than becoming convenient hard-coded values.

### PENDING: PLAYER_PROGRESSION_INITIALIZATION
Current runtime `Player` has prototype combat HP/MP but no canonical commoner normal-level/EXP/stat initialization wired into runtime. `RpgProgressionState.normalLevel` and `normalExp` therefore remain nullable until `Progression_Master` is mapped under the latest immutable progression graph.

### PENDING: ITEM_STAT_MODIFIERS
Item identity/equip slot/level requirement can be admitted when present in Master, but equipment stat modifiers remain absent until corresponding canonical fields/evidence are mapped. `recomputeStats()` is ready for those modifiers without changing base stats.

## Validation

Commits:
- `6648108a57cd0c8f37eac46f8e206c4ae074fdcb` — add evidence-safe RPG progression state pipeline.
- `10b36b022d1e46c21c82d1c9b7b4bb8fbd09bd0a` — wire CombatLedger results into `RuntimeState` RPG state.

GitHub Actions run #65 for `10b36b0...` completed successfully.
- `Compile debug sources only`: **SUCCESS**
- overall compile job: **SUCCESS**
- no APK assembly/package step introduced.

Static design verification:
- no second/third advancement node introduced;
- no job selection added to character creation;
- no arbitrary EXP/drop/Gold value assigned to the prototype monster;
- inventory/equipment/base stats remain separate concepts;
- ground loot is not teleported directly into inventory;
- equipment recomputation does not mutate permanent base stats.

## Next RPG bottleneck

1. Map the first canonical Master monster ID actually present in the playable world and connect its `Monster_Drop`/EXP evidence to `RewardResolution`.
2. Introduce a canonical data adapter/repository so `Item_Master` and `Monster_Drop` rows are not manually duplicated across Java definitions.
3. Wire visible/touch pickup interaction to existing `WorldDrop` APIs, then expose inventory/equipment UI plumbing.
4. Map `Progression_Master`: COMMONER → BASIC_JOB → LV99_MASTER → (JOB_CHANGE | PURE_JOB) → LV99 → FIRST_ADVANCEMENT; keep all later progression inactive.
5. Add Gold ledger/economy only from Master-backed acquisition/spend rows; preserve `MASTER_ECONOMY_SPEEDBOOSTER_STALE` no-go.
