#!/usr/bin/env python3
import csv,re,unicodedata
from pathlib import Path
R=Path(__file__).resolve().parents[1]
def n(s): return re.sub(r"[^0-9a-z가-힣]","",unicodedata.normalize("NFKC",s or "").lower())
master=list(csv.DictReader(open(R/"data/Item_Master.csv",encoding="utf-8-sig")))
inv=list(csv.DictReader(open(R/"canonical/Existing_Equipment_Asset_Inventory.csv",encoding="utf-8-sig")))
by={}
for a in inv:
 if a["AssetName"]!="UNNAMED": by.setdefault(n(a["AssetName"]),[]).append(a)
out=[]
for i in master:
 if i.get("대분류")!="장비": continue
 hits=by.get(n(i.get("아이템명")),[])
 if len(hits)==1:
  a=hits[0];out.append([i["Item_ID"],i["아이템명"],"EXACT_NAME",a["Asset_Group"],a["SourceAssetCode"],a["LocalPath"],a["GitBlobSHA"],a["Bytes"]])
 elif len(hits)>1:
  for a in hits: out.append([i["Item_ID"],i["아이템명"],"AMBIGUOUS_NAME",a["Asset_Group"],a["SourceAssetCode"],a["LocalPath"],a["GitBlobSHA"],a["Bytes"]])
 else: out.append([i["Item_ID"],i["아이템명"],"MISSING","","","","",""])
p=R/"canonical/Item_Existing_Asset_Map.csv"
with open(p,"w",newline="",encoding="utf-8") as f:
 w=csv.writer(f);w.writerow(["Item_ID","ItemName","Match_Status","Asset_Group","SourceAssetCode","LocalPath","GitBlobSHA","Bytes"]);w.writerows(out)
print("equipment",len({r[0] for r in out}),"exact",len({r[0] for r in out if r[2]=="EXACT_NAME"}),"ambiguous",len({r[0] for r in out if r[2]=="AMBIGUOUS_NAME"}),"missing",len({r[0] for r in out if r[2]=="MISSING"}))
