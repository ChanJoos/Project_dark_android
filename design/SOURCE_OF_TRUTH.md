# PROJECT DARK — SOURCE OF TRUTH REGISTRY

Updated: 2026-09-10

## Precedence

When implementation decisions conflict, use this order:
1. Explicit latest user-approved PROJECT DARK canon / DESIGN_CONSTITUTION
2. Latest canonical Master DB and its versioned extracts
3. Latest backbone/design specification plus explicit later canonical deltas
4. Official Nexon/original evidence linked by the design data
5. Verified observation / user-confirmed recollection according to evidence tag
6. `[B]` prototype/balance values only where canon is unknown
7. Runtime implementation

**Runtime code is never allowed to override higher-level design truth merely because it already exists.**

## Canonical repository files

- `design/DESIGN_CONSTITUTION.md` — non-negotiable product/scope/visual/gameplay rules
- `design/DATA_CONTRACT.md` — module/data boundaries
- `design/SOURCE_OF_TRUTH.md` — this registry
- `data/design/PROJECT_DARK_CANONICAL_SEED.md` — repository-readable canonical seed extracted from available design materials and latest approved deltas
- `docs/DEV_HISTORY.md` and pass files — implementation history; NOT higher authority than design

## External master artifacts

The project history identifies later master artifacts such as `PROJECT_DARK_Master_DB_v4.4_VISUAL_MANIFEST.xlsx`, `PROJECT_DARK_Master_DB_v4.3_GAMEPLAY_SCREEN_MAPPED.xlsx`, and `PROJECT_DARK_V0.7_BACKBONE_KO.docx`. At the time this registry was created, those exact binary files were not available through the connected GitHub repository/file-library retrieval path, so this repository MUST NOT pretend that their binaries have been uploaded.

Until their exact binaries can be committed, `PROJECT_DARK_CANONICAL_SEED.md` plus the Constitution/Contract are the enforceable repository-readable projection of the latest approved canon. If an exact later Master DB becomes available, it supersedes conflicting seed rows after explicit reconciliation.

## Design conflict protocol

If two sources conflict:
1. Do not choose the convenient value.
2. Record `DESIGN_CONFLICT` in DEV_HISTORY with source names/fields.
3. Keep runtime on the highest-precedence verified rule or PENDING-safe behavior.
4. Never promote `[B]`, FAN, prototype screenshots or generated art into `[O]`.

## Agent read gate

Every scheduled development run must read these exact paths before modifying gameplay:
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- `data/design/PROJECT_DARK_CANONICAL_SEED.md`
- `docs/DEV_HISTORY.md` and the newest relevant pass file

The Integrator must reject or correct changes that bypass this gate.