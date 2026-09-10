CURRENT AUDIT M001: 기존 tar.gz는 손상되어 사용 금지. 최신 입력은 master/data의 CSV 92개와 master/source의 첨부 ZIP입니다. master/MASTER_MANIFEST.md 및 master/RECONCILIATION.md를 먼저 읽으세요. 아래는 이전 기록입니다.

# Recovery Procedure

If individual generated data files are missing, validate `PROJECT_DARK_MASTER_DB.tar.gz`, extract it, and regenerate runtime data from the extracted `master/data/*.csv` files. Do not reconstruct missing rows from memory. If archive validation fails, stop canonical-data migration until a verified source snapshot is restored.
