# Master Data Consumption Contract

- Read design rules from `master/SOURCE_OF_TRUTH.md` and `master/PROJECT_DARK_V0.6_PLAN_KO.md`.
- Treat `master/PROJECT_DARK_MASTER_DB.tar.gz` as the preserved v4.4 database snapshot.
- Before using or extracting the archive, run `python master/verify_master_archive.py`.
- The verified archive must contain exactly 92 CSV sheet files beneath `master/data/`.
- Do not overwrite SOURCE / SOURCE+BALANCED / BALANCED or other evidence semantics with guessed values.
- Visual implementation must honor the visual manifest and audit sheets contained in the archive.
