# Master Source of Truth

Development agents must treat the repository Master archive as the canonical database snapshot for PROJECT DARK.

## Required inputs
1. `master/PROJECT_DARK_MASTER_DB.tar.gz` — converted snapshot of the v4.4 Master DB (92 workbook sheets preserved as CSV plus manifest).
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md` — currently available original planning document converted to Markdown.
3. `master/MASTER_DB_INFO.txt` — archive identity and checksum.

## Precedence
Verified source data > user-confirmed data > balanced reconstruction > implementation defaults.
Unknown original values must remain unknown/pending rather than being fabricated.

The v0.6 plan is the currently available original planning document; historical project records indicate later planning revisions existed, so do not interpret the `v0.6` filename as proof that no later planning revision ever existed.
