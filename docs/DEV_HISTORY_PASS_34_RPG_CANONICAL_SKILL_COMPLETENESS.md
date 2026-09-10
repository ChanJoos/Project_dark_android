# PASS 34 — RPG canonical skill completeness

Date: 2026-09-10
Branch: `agent/rpg/20260910-1338`

## Goal
Close the gap between the rich canonical `Skill_Master.csv` and the partial hand-written runtime catalog. Audit skill visuals and current official acquisition policy without inventing missing per-skill image mappings.

## Delivered
- Canonical `master/data` CSVs are packaged into Android assets.
- Added `RpgCanonicalSkillRepository` to load the complete Skill Master and preserve all source fields/evidence/runtime inclusion.
- Added `RpgCanonicalSkillPresentation` to expose the full runtime-included skill book/detail model.
- Added `Skill_Runtime_Overrides.csv` for current official no-requirement starter acquisition rules.
- Corrected Commoner Lv10 flow to job selection first, starter acquisition second.
- Updated `RpgActionLearningService`, job-selection and progression audits for current semantics.
- Added `Skill_Visual_Manifest.csv` with current 2026 renewal confirmation and official Nexon icon-sheet source OIDs.
- Added `Skill_Runtime_Completeness_Audit.csv`.

## Visual finding
`Skill_Master.csv` has no icon field, the repository has no individual skill icon files, and no per-skill asset mapping CSV exists. Official icon source sheets are identified, but individual crop coordinates/pixels remain unresolved. Runtime presentation marks this honestly with `OFFICIAL_SHEET_PENDING_CROP:<Skill_ID>` rather than fabricating a binding.

## Validation
PR patch references to superseded gate identifiers were checked and removed. Full Android compile/APK was not executed in this run because the available Actions workflow is main/manual only and the local execution environment could not resolve GitHub for a clone.
