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
