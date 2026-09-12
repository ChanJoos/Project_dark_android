# MASTER RECONCILIATION — 2026-09-12

## Adoption
This package is designed to be merged into the existing repository root.
Do **not** delete the existing historical `master/data` CSVs simply because new tables are added.

## New canonical additions
- `master/data/Asset_Master.csv` — 4142 audited visual catalog rows
- `master/data/Asset_Animation_Frame_Master.csv` — 566 audited full-sprite frames
- `master/data/Asset_Animation_Semantics.csv`
- `master/data/Source_Fact_Master.csv`
- `master/data/Item_Enhancement_Rule_Master.csv`
- `master/data/External_Source_Index.csv`
- `master/assets/items/` — WebP item/appearance crops
- `master/assets/animation_frames/` — WebP animation frames

## Current verification state
- Source-named: 2070
- Source-unresolved: 2072
- Candidate names available but not promoted: 383
- Image QA failures: 0
- Animation QA failures: 0

## Deliberately excluded
- duplicate PNG assets
- duplicate JSON copy of the 4,142-row CSV
- raw HAR / raw homepage capture
- intermediate pixel/alpha/structural working tables
- duplicate unresolved lists derivable from `Asset_Master.csv`
- duplicate GitHub Item_Master extracts already present in the repository

The raw HAR was used as evidence but is intentionally excluded from the clean GitHub package.
