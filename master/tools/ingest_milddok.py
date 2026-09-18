#!/usr/bin/env python3
"""Merge a normalized Milddok snapshot into PROJECT DARK master without overwriting evidence.
Input CSV columns: source_key,name,item_type,slot,jobs,required_level,min_atk,max_atk,ac,hit,dam,hp,mp,str,int,wis,con,dex,magic_defense,attack_element,defense_element,weight,durability,source_url,era
"""
import csv,sys,re,unicodedata
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; DATA=ROOT/"data"
def n(s): return re.sub(r"\s+","",unicodedata.normalize("NFKC",(s or "")).strip()).lower()
def rows(p):
 with open(p,encoding="utf-8-sig",newline="") as f:return list(csv.DictReader(f))
def main(src):
 items=rows(DATA/"Item_Master.csv"); byname={}
 for r in items:
  if r.get("Item_ID") and r.get("아이템명"):byname.setdefault(n(r["아이템명"]),[]).append(r)
 source=rows(src); matched=[]; ambiguous=[]; missing=[]
 for r in source:
  if not r.get("name"):continue
  cand=byname.get(n(r["name"]),[])
  if len(cand)==1:matched.append((r,cand[0]))
  elif len(cand)>1:ambiguous.append(r)
  else:missing.append(r)
 out=ROOT/"canonical/generated";out.mkdir(parents=True,exist_ok=True)
 with open(out/"Milddok_Match_Audit.csv","w",encoding="utf-8-sig",newline="") as f:
  w=csv.writer(f);w.writerow(["source_key","name","status","Item_ID"])
  for r,i in matched:w.writerow([r.get("source_key"),r.get("name"),"MATCHED",i["Item_ID"]])
  for r in ambiguous:w.writerow([r.get("source_key"),r.get("name"),"AMBIGUOUS",""])
  for r in missing:w.writerow([r.get("source_key"),r.get("name"),"NEW_CANDIDATE",""])
 print(f"source={len(source)} matched={len(matched)} ambiguous={len(ambiguous)} new={len(missing)}")
 return 0
if __name__=="__main__":raise SystemExit(main(Path(sys.argv[1])))
