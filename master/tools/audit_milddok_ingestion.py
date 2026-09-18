#!/usr/bin/env python3
import csv,subprocess,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
src=ROOT/"tests/fixtures/milddok_normalized_sample.csv"
subprocess.run([sys.executable,str(ROOT/"tools/ingest_milddok.py"),str(src)],check=True)
p=ROOT/"data/generated/Milddok_Match_Audit.csv"
rows=list(csv.DictReader(open(p,encoding="utf-8-sig")))
assert len(rows)==3
m={r["name"]:r for r in rows}
assert m["바다의진주목걸이"]["status"]=="MATCHED" and m["바다의진주목걸이"]["Item_ID"]=="IT_NECK_WATER_PEARL"
assert m["가죽방패"]["status"]=="MATCHED" and m["가죽방패"]["Item_ID"]=="IT_SHIELD_LEATHER"
assert m["TEST_ONLY_MISSING"]["status"]=="NEW_CANDIDATE"
print("MILDDOK_INGESTION=PASS")
