#!/usr/bin/env python3
import csv
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def read(p):
 with open(p,encoding="utf-8-sig",newline="") as f:return list(csv.DictReader(f))
base=read(ROOT/"data/Item_Master.csv")
add=read(ROOT/"canonical/Equipment_Catalog_Additions.csv")
assets=read(ROOT/"canonical/Equipment_Asset_Evidence.csv")
ids={r.get("Item_ID") for r in base+add if r.get("Item_ID")}
eq={r.get("Item_ID") for r in base+add if r.get("Item_ID") and r.get("대분류")=="장비"}
mapped={r.get("Item_ID") for r in assets if r.get("Item_ID")}
unknown=sorted(mapped-ids)
if unknown: raise SystemExit("unknown asset Item_ID: "+",".join(unknown))
dup=sorted({x for x in ids if sum(1 for r in base+add if r.get("Item_ID")==x)>1})
if dup: raise SystemExit("duplicate canonical Item_ID: "+",".join(dup))
official=[r for r in assets if r.get("Evidence")=="O" and r.get("MappingStatus")=="MAPPED"]
print(f"EQUIPMENT_ASSET_REGISTRY=PASS equipment={len(eq)} mapped_items={len(mapped)} official_mapped_assets={len(official)}")
