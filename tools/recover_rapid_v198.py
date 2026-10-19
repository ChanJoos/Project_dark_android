"""Recover immutable Rapid source pairs; mappings are reviewed separately."""
import hashlib,json,urllib.request,zipfile
from pathlib import Path
OUT=Path("rapid-v198");OUT.mkdir(exist_ok=True)
BASE="https://lod-dressup-2.web.app"
records=[]
with zipfile.ZipFile(OUT/"original_sources.zip","w",zipfile.ZIP_DEFLATED) as pack:
 for prefix,folder in [("mg","back"),("wg","back"),("mc","cape"),("wc","cape")]:
  for number in range(146,150):
   identity=f"{prefix}{number}"
   for rel in [f"data/type/{folder}/{identity}.json",f"atlas/{folder}/{identity}.webp"]:
    url=BASE+"/"+rel
    with urllib.request.urlopen(url,timeout=45) as response:
     raw=response.read();content_type=response.headers.get("Content-Type")
    if rel.endswith(".json"):
     parsed=json.loads(raw);assert isinstance(parsed.get("sprites"),dict),rel
    else:assert raw[:4]==b"RIFF" and raw[8:12]==b"WEBP",rel
    pack.writestr(rel,raw)
    records.append(dict(identity=identity,path=rel,url=url,bytes=len(raw),sha256=hashlib.sha256(raw).hexdigest(),contentType=content_type))
 (OUT/"receipt.json").write_text(json.dumps(dict(status="SOURCE_RECOVERED_NOT_RUNTIME_MAPPED",records=records),ensure_ascii=False,indent=2)+"\n")
 pack.writestr("receipt.json",(OUT/"receipt.json").read_bytes())
with zipfile.ZipFile(OUT/"original_sources.zip") as pack:assert pack.testzip() is None
print("Retained",len(records),"original source files; no runtime mapping or visual acceptance claim")
