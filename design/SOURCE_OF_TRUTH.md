# PROJECT DARK — SOURCE OF TRUTH REGISTRY

Updated: 2026-09-10

## Precedence

When implementation decisions conflict, use this order:
1. Explicit latest user-approved PROJECT DARK canon / `design/DESIGN_CONSTITUTION.md`
2. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md` — full repository-readable conversion of the latest retrievable integrated Master DB
3. `master/PROJECT_DARK_V0.6_PLAN_KO.md` plus explicit later canonical deltas
4. Official Nexon/original evidence linked by the design data
5. Verified observation / user-confirmed recollection according to evidence tag
6. `[B]` prototype/balance values only where canon is unknown
7. `data/design/PROJECT_DARK_CANONICAL_SEED.md` as a convenience projection/fallback only
8. Runtime implementation

**Runtime code is never allowed to override higher-level design truth merely because it already exists. The canonical seed never overrides either master file.**

## Canonical repository files

- `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md` — canonical integrated Item/Economy/World/Progression Master DB; 19 source workbook tabs reproduced as text
- `master/PROJECT_DARK_V0.6_PLAN_KO.md` — canonical full design-plan master copy
- `design/DESIGN_CONSTITUTION.md` — latest user-approved non-negotiable product/scope/visual/gameplay rules; takes precedence over conflicting older master rows
- `design/DATA_CONTRACT.md` — module/data boundaries
- `design/SOURCE_OF_TRUTH.md` — this registry
- `data/design/PROJECT_DARK_CANONICAL_SEED.md` — derived working projection only; not a master
- `docs/DEV_HISTORY.md` and pass files — implementation history; NOT higher authority than design

## Binary provenance

The Master DB markdown is a loss-averse repository-readable conversion of the retrievable Library workbook `LOD_Mobile_Final_Progression_World_DB_v1.0.xlsx`; worksheet names, rows, IDs, values, evidence/status strings and source URLs were retained rather than summarized. The raw XLSX currently lacks an authorized raw-byte materialization path, so the markdown master is the usable GitHub source representation.

`PROJECT_DARK_V0.6_PLAN_KO.md` is the repository-readable canonical master copy of the corresponding planning document.

Historical filenames mentioned in development history (for example later `PROJECT_DARK_Master_DB_v4.4_VISUAL_MANIFEST.xlsx` / v0.7 backbone artifacts) must not be treated as available canonical files unless their exact content is actually present and reconciled in the repository.

## Known DESIGN_CONFLICT / precedence notes

1. `Economy_Loop` in the Master DB still contains `속도부스터`, while the same workbook README explicitly says speed booster was removed and the latest Constitution/no-go canon excludes it. **Do not implement speed booster.** Preserve the stale row for source fidelity and treat it as `DESIGN_CONFLICT: MASTER_ECONOMY_SPEEDBOOSTER_STALE`.
2. The Master DB contains acquisition references to locked towns such as 수오미/루어스. Initial runtime keeps those towns LOCKED and applies `Shop_Redistribution`; item identity remains canonical but locked-town opening must not be inferred.
3. Character creation must start as 평민 without job selection. `Progression_Master` P00's `직업 선택` is an exit/progression event around the early-game gate, not character-creation job selection.
4. Latest runtime scope ends at 1차 승급. Any older/general design mention of 2차/3차 progression is data-model/background context only and must not be activated.
5. Advancement-path detail that conflicts with a later explicit user no-go remains `PENDING/DESIGN_CONFLICT` until reconciled; do not expand endgame path content merely because a lower-precedence master row mentions it.

## Design conflict protocol

If two sources conflict:
1. Do not choose the convenient value.
2. Apply the precedence above.
3. Record `DESIGN_CONFLICT` in DEV_HISTORY with source names/fields.
4. Keep runtime on the highest-precedence verified rule or PENDING-safe behavior.
5. Never promote `[B]`, FAN, prototype screenshots or generated art into `[O]`.

## Agent read gate

Every scheduled development run must read these exact paths before modifying gameplay:
- `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
- `master/PROJECT_DARK_V0.6_PLAN_KO.md`
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- `data/design/PROJECT_DARK_CANONICAL_SEED.md` only as a derived convenience projection
- `docs/DEV_HISTORY.md` and the newest relevant pass file

The Integrator must reject or correct changes that bypass this gate.