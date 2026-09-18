#!/usr/bin/env python3
import csv,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
MASTER=ROOT/"data/Item_Master.csv"
ASSETS=ROOT/"canonical/Equipment_Asset_Evidence.csv"
def main():
 items=list(csv.DictReader(open(MASTER,encoding="utf-8-sig")))
 eq=[r for r in items if r.get("대분류")=="장비" and r.get("Item_ID")]
 assets=list(csv.DictReader(open(ASSETS,encoding="utf-8-sig")))
 mapped={r.get("Item_ID") for r in assets if r.get("Item_ID")}
 unknown=sorted(mapped-{r["Item_ID"] for r in eq})
 if unknown: raise SystemExit("unknown asset Item_ID: "+",".join(unknown))
 print(f"EQUIPMENT_ASSET_REGISTRY=PASS equipment={len(eq)} mapped={len(mapped)} missing={len(eq)-len(mapped)}")
if __name__=="__main__":main()
