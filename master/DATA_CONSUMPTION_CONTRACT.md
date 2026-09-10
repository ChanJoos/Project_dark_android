CURRENT AUDIT M001: 기존 tar.gz는 손상되어 사용 금지. 최신 입력은 master/data의 CSV 92개와 master/source의 첨부 ZIP입니다. master/MASTER_MANIFEST.md 및 master/RECONCILIATION.md를 먼저 읽으세요. 아래는 이전 기록입니다.

# Master Data Consumption Contract

- Read design rules from `master/SOURCE_OF_TRUTH.md` and `master/PROJECT_DARK_V0.6_PLAN_KO.md`.
- Treat `master/PROJECT_DARK_MASTER_DB.tar.gz` as the preserved v4.4 database snapshot.
- Before using or extracting the archive, run `python master/verify_master_archive.py`.
- The verified archive must contain exactly 92 CSV sheet files beneath `master/data/`.
- Do not overwrite SOURCE / SOURCE+BALANCED / BALANCED or other evidence semantics with guessed values.
- Visual implementation must honor the visual manifest and audit sheets contained in the archive.
