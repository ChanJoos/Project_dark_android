# Recovery Procedure

If individual generated data files are missing, validate `PROJECT_DARK_MASTER_DB.tar.gz`, extract it, and regenerate runtime data from the extracted `master/data/*.csv` files. Do not reconstruct missing rows from memory. If archive validation fails, stop canonical-data migration until a verified source snapshot is restored.
