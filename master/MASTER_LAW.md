# PROJECT DARK MASTER LAW v1.0

Status: GITHUB_ADOPTION_CANDIDATE
Date: 2026-09-12

## Canonical authority
After adoption, the repository `master/` directory is the single PROJECT DARK canonical data authority.
The historical 92 CSV files remain preserved as baseline history, but "92" is not a schema or table-count constraint.

## Evidence precedence
1. Direct first-party/source metadata
2. User-confirmed project decisions
3. Traceable high-confidence reconstruction
4. External client/reference databases
5. Implementation defaults

Lower-confidence evidence must never silently overwrite higher-confidence evidence.

## Unknown values
Unknown original values remain `UNRESOLVED` / `PENDING`.
Names, stats, acquisition rules, animation semantics, and item identities must not be fabricated for completeness.

## Identity model
Gameplay `Item_ID`, visual asset ID, display name, appearance ID, skin/design identity, and sprite-frame identity
are separate concepts. Equal names or equal pixels are evidence of relationship, not automatic proof of identical gameplay identity.

## Versioned facts
Historical rules and stats remain versioned facts with their sources.
A later value supersedes an earlier value only when exact identity and scope are established.

## Asset policy
`master/data/Asset_Master.csv` is the unified visual-asset verification table.
WebP is the repository asset format in this package; duplicate PNG copies are intentionally omitted.
Missing source atlases remain missing rather than being synthetically replaced.

## Animation policy
Animation semantics are promoted only when directly supported by captured source/runtime evidence.
Unknown groups remain unresolved.

## Audit policy
Conflicts and ambiguous identities are preserved in `master/audit/`.
Candidate relationships are not silently promoted into canonical exact mappings.

## Validation gate
A promoted canonical row must remain source-traceable and pass referential, duplicate-identity,
image/frame, and schema checks.
