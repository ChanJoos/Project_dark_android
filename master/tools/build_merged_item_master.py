#!/usr/bin/env python3
import csv
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
base=ROOT/"data/Item_Master.csv"; add=ROOT/"canonical/Equipment_Catalog_Additions.csv"; out=ROOT/"canonical/generated/Item_Master_Merged.csv"
out.parent.mkdir(parents=True,exist_ok=True)
def read(p):
 with open(p,encoding="utf-8-sig",newline="") as f:return list(csv.DictReader(f))
b,a=read(base),read(add); fields=list(b[0].keys())
seen={r["Item_ID"] for r in b}; dup=[r["Item_ID"] for r in a if r["Item_ID"] in seen]
if dup: raise SystemExit("addition collides with base: "+",".join(dup))
with open(out,"w",encoding="utf-8-sig",newline="") as f:
 w=csv.DictWriter(f,fieldnames=fields,extrasaction="ignore");w.writeheader();w.writerows(b);w.writerows(a)
print(f"ITEM_MASTER_MERGED=PASS base={len(b)} additions={len(a)} total={len(b)+len(a)}")
