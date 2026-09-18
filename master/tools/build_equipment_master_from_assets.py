#!/usr/bin/env python3
import csv,re,hashlib,unicodedata
from pathlib import Path
R=Path(__file__).resolve().parents[1]
ASSET_ROOT=R/"assets/items"
EQUIP_GROUPS={"armor","weapon","shield","shoes","cape","back"}
def norm(s): return re.sub(r"[^0-9a-z가-힣]","",unicodedata.normalize("NFKC",s or "").lower())
def read(p):
 return list(csv.DictReader(open(p,encoding="utf-8-sig"))) if p.exists() else []
base=read(R/"data/Item_Master.csv")
adds=read(R/"canonical/Equipment_Catalog_Additions.csv")
stats=read(R/"canonical/Equipment_Stats.csv")
base_by_name={}
for x in base+adds:
 name=x.get("아이템명") or x.get("ItemName") or x.get("Name") or ""
 if name: base_by_name.setdefault(norm(name),[]).append(x)
stat_by_id={}
for s in stats: stat_by_id.setdefault(s.get("Item_ID",""),[]).append(s)
assets=[]
for p in sorted(ASSET_ROOT.rglob("*.webp")):
 rel=p.relative_to(R).as_posix(); group=p.parts[p.parts.index("items")+1]
 if group not in EQUIP_GROUPS: continue
 stem=p.stem; code,name=(stem.split("_",1)+[""])[:2] if "_" in stem else (stem,"")
 if not name or name=="UNNAMED": continue
 assets.append((group,code,name,rel,p.stat().st_size))
by_name={}
for a in assets: by_name.setdefault(norm(a[2]),[]).append(a)
rows=[]
used_ids=set()
for key,arr in sorted(by_name.items(),key=lambda z:z[1][0][2]):
 name=arr[0][2]; known=base_by_name.get(key,[])
 if known:
  item_id=known[0].get("Item_ID",""); origin="EXISTING_MASTER" if known[0] in base else "CATALOG_ADDITION"
 else:
  raw="IT_ASSET_"+re.sub(r"[^A-Za-z0-9]+","_",arr[0][1]).upper()
  item_id=raw; n=2
  while item_id in used_ids: item_id=f"{raw}_{n}";n+=1
  origin="ASSET_RECOVERED"
 used_ids.add(item_id)
 groups="|".join(sorted({a[0] for a in arr})); codes="|".join(a[1] for a in arr); paths="|".join(a[3] for a in arr)
 st=stat_by_id.get(item_id,[])
 stat_status="RESOLVED" if any((x.get("Value") or "").strip() for x in st) else "PENDING"
 rows.append([item_id,name,origin,groups,codes,len(arr),paths,stat_status])
out=R/"canonical/Equipment_Master_Expanded.csv"
with open(out,"w",newline="",encoding="utf-8") as f:
 w=csv.writer(f);w.writerow(["Item_ID","ItemName","IdentityOrigin","AssetGroups","SourceAssetCodes","AssetCount","LocalAssetPaths","StatsStatus"]);w.writerows(rows)
miss=R/"canonical/Equipment_Stats_Missing.csv"
with open(miss,"w",newline="",encoding="utf-8") as f:
 w=csv.writer(f);w.writerow(["Item_ID","ItemName","Reason"]);w.writerows([[r[0],r[1],"NO_RESOLVED_STATS"] for r in rows if r[-1]=="PENDING"])
print(f"expanded_equipment={len(rows)} assets_used={len(assets)} existing_identity={sum(r[2]!='ASSET_RECOVERED' for r in rows)} recovered_identity={sum(r[2]=='ASSET_RECOVERED' for r in rows)} stats_resolved={sum(r[-1]=='RESOLVED' for r in rows)} stats_pending={sum(r[-1]=='PENDING' for r in rows)}")
