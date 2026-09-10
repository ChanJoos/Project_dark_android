CURRENT AUDIT M001: 기존 tar.gz는 손상되어 사용 금지. 최신 입력은 master/data의 CSV 92개와 master/source의 첨부 ZIP입니다. master/MASTER_MANIFEST.md 및 master/RECONCILIATION.md를 먼저 읽으세요. 아래는 이전 기록입니다.

# PROJECT DARK Master

This directory stores the repository-level Source of Truth for game design and data.

`PROJECT_DARK_MASTER_DB.tar.gz` is a compressed, loss-preserving repository snapshot converted from `PROJECT_DARK_Master_DB_v4.4_VISUAL_MANIFEST.xlsx`. It contains the workbook's 92 sheets as CSV files under `master/data/`, together with the conversion manifest and planning Markdown.

Validate with:

```bash
python master/verify_master_archive.py
```

Extract with:

```bash
sh master/extract_master.sh
```

The archive SHA-256 is recorded in `MASTER_DB_INFO.txt` and `MASTER_DB_ARCHIVE.md`.
