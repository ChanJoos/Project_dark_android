CURRENT AUDIT M001: 기존 tar.gz는 손상되어 사용 금지. 변환 manifest는 역사적 source CSV 92개를 기록하며 `master/data`에는 canonical additions 6개와 canonical overrides 2개가 반영되어 있습니다. `Skill_Evidence.csv`에는 사용자 캡처 추가 사실이 포함되고 상세 전사는 `Skill_Legacy_Requirements.csv`에 보존됩니다. 최신 검증은 `python tools/validate_master.py`; master/MASTER_MANIFEST.md 및 master/RECONCILIATION.md를 먼저 읽으세요. 아래는 이전 기록입니다.

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
