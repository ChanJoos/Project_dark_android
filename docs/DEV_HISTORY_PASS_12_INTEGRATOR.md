# PROJECT DARK — DEV HISTORY PASS 12 · INTEGRATOR / UX / QA

Date: 2026-09-10
Role: Integrator · UX · QA / final design compliance gate

## Source-of-Truth gate

Read before integration:
- `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
- `master/PROJECT_DARK_V0.6_PLAN_KO.md`
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- `data/design/PROJECT_DARK_CANONICAL_SEED.md`
- `docs/DEV_HISTORY.md`
- latest pass history including PASS 08, World PASS 09, Combat PASS 10, RPG PASS 11

During this run a newer verified repository Master became available: `master/PROJECT_DARK_MASTER_DB.tar.gz`, converted from the user-provided `PROJECT_DARK_Master_DB_v4.4_VISUAL_MANIFEST.xlsx` and preserving 92 worksheet CSVs. `master/MASTER_DB_INFO.txt` records the archive SHA-256 and `master/verify_master_archive.py` enforces the checksum and 92-sheet count.

## Source-of-Truth correction

Updated `design/SOURCE_OF_TRUTH.md` so the verified v4.4 repository archive is the highest database authority below the latest explicit user canon / Design Constitution. The earlier `LOD_Mobile_Final_Progression_World_DB_v1.0.md` remains a useful readable source but cannot override v4.4 when domains overlap.

This closes a governance defect from PASS 08: the prior registry treated the 19-tab v1.0 representation as the highest available database Master even after the 92-sheet v4.4 original-derived snapshot was preserved in Git.

## RPG integration audit

PASS 11 correctly added `RpgProgressionState` and wired `CombatLedger.MONSTER_DEFEATED` into it. The state model now supports:
- evidence-safe reward resolution,
- explicit world-drop entities,
- pickup validation,
- inventory state,
- equipment state,
- separate base/equipment/total stat recomputation.

However, the current runtime monster remains `combat_dummy_01 [B]`, which is not a canonical Master monster. Therefore no canonical EXP/drop/Gold was attached. This is correct; assigning Master rewards to the dummy would create an invented monster→reward relationship.

## Implementation completed

Added `RpgInteractionController.java` as an Integrator-owned orchestration boundary between the renderer/input layer and RPG state.

It provides:
1. nearest ground-drop selection without mutation,
2. pickup through the existing `RpgProgressionState.pickup()` validation path,
3. canonical item-ID inventory selection,
4. equip through the RPG-owned requirement/slot validation path,
5. read-only stat snapshot access,
6. QA invariant checking that equipped item IDs still refer to positively owned inventory entries.

The controller contains no EXP, item stat, drop-rate, level, monster, Gold, or original-game balance constants. AUTO/manual callers can share the same pickup contract; no direct inventory teleport path was introduced.

Commit: `5cc409abca97562a73d35b6be0d61e021b779b1b`.

## Validation

GitHub Actions run #68 for the new controller executed `:app:compileDebugJavaWithJavac`.
- `Compile debug sources only`: SUCCESS
- no Java compilation error
- APK packaging remains disabled in the autonomous validation workflow

The v4.4 Master archive preservation commit also passed the Android compile workflow; data preservation introduced no source regression.

## DESIGN_CONFLICT / PENDING

### PENDING: COMBAT_DUMMY_REWARD_MAPPING
`combat_dummy_01` remains a prototype fixture and must not receive canonical EXP/drop/Gold. Runtime reward result remains `PENDING_NO_CANONICAL_MONSTER_REWARD` until a Master-backed monster identity is placed in the playable world.

### PENDING: PLAYER_PROGRESSION_INITIALIZATION
Runtime normal level/EXP/base stats are not yet initialized from the authoritative progression data. Do not invent Lv/EXP/stat defaults merely to make equip tests pass.

### PENDING: ITEM_STAT_MODIFIERS
The currently admitted `IT_GLOVE_LEATHER` identity/slot/requirement is Master-backed, but stat modifiers were not present in the mapped row. Equip may be validated as a state transition once level is valid, but a fabricated stat delta is forbidden.

### DESIGN_CONFLICT: MASTER PRECEDENCE MIGRATION
Any agent still treating `LOD_Mobile_Final_Progression_World_DB_v1.0.md` or `PROJECT_DARK_CANONICAL_SEED.md` as higher authority than the preserved v4.4 archive is out of date. `design/SOURCE_OF_TRUTH.md` now resolves this explicitly.

## Playable-slice status

Current chain:
`movement → target → approach → attack/magic/skill → monster counterattack → death → MONSTER_DEFEATED event → reward resolution`

Implemented but not yet visible/fully wired in `GameView`:
`world drop → pickup → inventory → equip → stat recomputation`

The data and interaction contracts for this second segment now exist without inventing canonical rewards. The remaining blocker is not controller architecture; it is choosing/placing the first Master-backed monster in the playable world and mapping its authoritative reward/progression rows from the verified v4.4 archive.

## Next integration bottleneck

1. Validate/extract the v4.4 archive and identify the first canonical monster + region + reward/EXP mapping suitable for the current vertical slice.
2. Replace `combat_dummy_01` only when the canonical monster/world relationship is supported; keep visual asset `PENDING_CROP` until verified.
3. Wire `RpgInteractionController` to visible ground-drop rendering/touch pickup and compact inventory/equipment UI without moving RPG rules into `GameView`.
4. Initialize Commoner progression from Master-backed data, then validate equip requirement handling and stat recomputation without invented modifiers.
5. After the chain is runtime-visible, add an end-to-end regression check for defeat→reward→drop→pickup→inventory→equip→stat snapshot.
