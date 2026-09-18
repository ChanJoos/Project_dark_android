#!/usr/bin/env python3
import csv
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
base=ROOT/"data/Item_Master.csv"
sources=[ROOT/"canonical/Equipment_Catalog_Additions.csv",ROOT/"canonical/Equipment_Asset_Recovered_Catalog.csv"]
out=ROOT/"canonical/generated/Item_Master_Merged.csv";out.parent.mkdir(parents=True,exist_ok=True)
def read(p):
 with open(p,encoding="utf-8-sig",newline="") as f:return list(csv.DictReader(f))
b=read(base); fields=list(b[0].keys()); merged=list(b); seen={r["Item_ID"] for r in b}; counts=[]
for p in sources:
 if not p.exists(): counts.append((p.name,0));continue
 rows=read(p);counts.append((p.name,len(rows)))
 dup=[r["Item_ID"] for r in rows if r["Item_ID"] in seen]
 if dup: raise SystemExit("catalog collides with existing IDs: "+",".join(dup[:20]))
 merged.extend(rows);seen.update(r["Item_ID"] for r in rows)
with open(out,"w",encoding="utf-8-sig",newline="") as f:
 w=csv.DictWriter(f,fieldnames=fields,extrasaction="ignore");w.writeheader();w.writerows(merged)
equip=sum(1 for r in merged if r.get("대분류")=="장비")
print(f"ITEM_MASTER_MERGED=PASS historical_base={len(b)} canonical_total={len(merged)} equipment={equip} sources={counts}")
