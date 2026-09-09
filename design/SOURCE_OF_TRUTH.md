# PROJECT DARK — SOURCE OF TRUTH REGISTRY

Updated: 2026-09-10

## Precedence

When implementation decisions conflict, use this order:
1. Explicit latest user-approved PROJECT DARK canon / `design/DESIGN_CONSTITUTION.md`
2. `master/PROJECT_DARK_MASTER_DB.tar.gz` + `master/MASTER_DB_INFO.txt` — verified repository snapshot converted from user-provided `PROJECT_DARK_Master_DB_v4.4_VISUAL_MANIFEST.xlsx` (92 sheets)
3. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md` — earlier repository-readable integrated progression/world master; useful for readable lookup but subordinate to verified v4.4 snapshot when the two differ
4. `master/PROJECT_DARK_V0.6_PLAN_KO.md` plus explicit later canonical deltas
5. Official Nexon/original evidence linked by the design data
6. Verified observation / user-confirmed recollection according to evidence tag
7. `[B]` prototype/balance values only where canon is unknown
8. `data/design/PROJECT_DARK_CANONICAL_SEED.md` as a convenience projection/fallback only
9. Runtime implementation

**Runtime code is never allowed to override higher-level design truth merely because it already exists. The canonical seed never overrides any Master source.**

## Canonical repository files

- `master/PROJECT_DARK_MASTER_DB.tar.gz` — canonical preserved v4.4 Master DB snapshot; 92 CSV worksheet exports plus conversion metadata inside the archive
- `master/MASTER_DB_INFO.txt` — archive version, sheet count and SHA-256 identity
- `master/verify_master_archive.py` — required checksum/sheet-count validator before consuming the archive
- `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md` — earlier readable integrated Item/Economy/World/Progression Master DB; not allowed to override v4.4
- `master/PROJECT_DARK_V0.6_PLAN_KO.md` — currently available full planning-document master copy
- `design/DESIGN_CONSTITUTION.md` — latest user-approved non-negotiable product/scope/visual/gameplay rules; takes precedence over conflicting older rows
- `design/DATA_CONTRACT.md` — module/data boundaries
- `design/SOURCE_OF_TRUTH.md` — this registry
- `data/design/PROJECT_DARK_CANONICAL_SEED.md` — derived working projection only; not a master
- `docs/DEV_HISTORY.md` and pass files — implementation history; NOT higher authority than design

## Binary provenance

The repository now contains a verified compressed canonical snapshot created from the user-provided `PROJECT_DARK_Master_DB_v4.4_VISUAL_MANIFEST.xlsx`. `master/PROJECT_DARK_MASTER_DB.tar.gz` preserves 92 worksheet CSVs and conversion metadata. Its expected SHA-256 and sheet count are recorded in `master/MASTER_DB_INFO.txt` and enforced by `master/verify_master_archive.py`.

`master/LOD_Mobile_Final_Progression_World_DB_v1.0.md` remains available as an earlier readable master representation, but it is no longer the highest database authority where v4.4 contains the same domain.

`PROJECT_DARK_V0.6_PLAN_KO.md` is the repository-readable canonical copy of the currently available planning original. Historical project records mention later planning revisions, so v0.6 must not be misrepresented as proof that no later plan ever existed.

## Known DESIGN_CONFLICT / precedence notes

1. `Economy_Loop` in an earlier Master representation contains `속도부스터`, while the same workbook README and latest Constitution/no-go canon exclude it. **Do not implement speed booster.** Preserve stale source rows for provenance and treat as `DESIGN_CONFLICT: MASTER_ECONOMY_SPEEDBOOSTER_STALE` unless v4.4 explicitly resolves the row.
2. Acquisition references to locked towns such as 수오미/루어스 do not authorize opening those towns. Initial runtime follows the active-world/redistribution rules from the highest applicable Master source.
3. Character creation must start as 평민 without job selection. Any progression row mentioning `직업 선택` is not permission to move job selection into character creation.
4. Latest runtime scope ends at 1차 승급. Any older/general design mention of 2차/3차 progression is data-model/background context only and must not be activated.
5. Advancement-path detail that conflicts with a later explicit user no-go remains `PENDING/DESIGN_CONFLICT` until reconciled; do not expand endgame path content merely because a lower-precedence row mentions it.
6. Runtime `combat_dummy_01` is a `[B]` fixture and has no canonical reward mapping. Do not attach canonical EXP/drop/Gold to it. Use `PENDING_NO_CANONICAL_MONSTER_REWARD` until a Master-backed monster mapping exists.

## Design conflict protocol

If two sources conflict:
1. Do not choose the convenient value.
2. Apply the precedence above.
3. Record `DESIGN_CONFLICT` in DEV_HISTORY with source names/fields.
4. Keep runtime on the highest-precedence verified rule or PENDING-safe behavior.
5. Never promote `[B]`, FAN, prototype screenshots or generated art into `[O]`.

## Agent read gate

Every scheduled development run must read these exact repository authorities before modifying gameplay:
- `master/MASTER_DB_INFO.txt`
- `master/PROJECT_DARK_MASTER_DB.tar.gz` provenance/verification contract via `master/README.md` and `master/verify_master_archive.py`; when archive contents are needed, validate/extract the 92 CSVs rather than reconstructing them from memory
- `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md` as readable earlier reference
- `master/PROJECT_DARK_V0.6_PLAN_KO.md`
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- `data/design/PROJECT_DARK_CANONICAL_SEED.md` only as a derived convenience projection
- `docs/DEV_HISTORY.md` and the newest relevant pass file

The Integrator must reject or correct changes that bypass this gate.
