#!/usr/bin/env python3
import csv,re,unicodedata
from pathlib import Path
R=Path(__file__).resolve().parents[1]
GROUPS={"armor","weapon","shield","shoes","cape","back"}
def norm(s): return re.sub(r"[^0-9a-z가-힣]","",unicodedata.normalize("NFKC",s or "").lower())
def read(p):
 with open(p,encoding="utf-8-sig",newline="") as f:return list(csv.DictReader(f))
base=read(R/"data/Item_Master.csv"); adds=read(R/"canonical/Equipment_Catalog_Additions.csv")
known={}
for x in base+adds:
 name=x.get("아이템명","")
 if name: known.setdefault(norm(name),x)
assets={}
for p in sorted((R/"assets/items").rglob("*.webp")):
 group=p.parts[p.parts.index("items")+1]
 if group not in GROUPS: continue
 stem=p.stem
 if "_" not in stem: continue
 code,name=stem.split("_",1)
 if not name or name=="UNNAMED": continue
 assets.setdefault(norm(name),{"name":name,"group":group,"codes":[],"paths":[]})
 assets[norm(name)]["codes"].append(code);assets[norm(name)]["paths"].append(p.relative_to(R).as_posix())
fields=list(base[0].keys())
out=[]
used={x["Item_ID"] for x in base+adds}
for k,a in assets.items():
 if k in known: continue
 code=a["codes"][0]
 raw="IT_ASSET_"+re.sub(r"[^A-Za-z0-9]+","_",code).upper()
 iid=raw;n=2
 while iid in used:iid=f"{raw}_{n}";n+=1
 used.add(iid)
 slot={"weapon":"무기","armor":"갑옷","shield":"방패","shoes":"신발","cape":"망토","back":"등"}.get(a["group"],a["group"])
 row={x:"" for x in fields};row.update({"Item_ID":iid,"아이템명":a["name"],"대분류":"장비","세부분류":slot,"슬롯":slot,"Source_ID":"SRC_EXISTING_ASSET_LIBRARY","신뢰도":"V","비고":"Recovered from existing source-derived asset library; stats/requirements pending evidence"})
 out.append(row)
p=R/"canonical/Equipment_Asset_Recovered_Catalog.csv"
with open(p,"w",encoding="utf-8-sig",newline="") as f:
 w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerows(out)
print(f"ASSET_RECOVERED_CATALOG=PASS recovered={len(out)} named_asset_identities={len(assets)}")
