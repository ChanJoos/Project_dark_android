"""Recover exact atlases/registrations for the admitted identity catalog, with source receipts."""
from pathlib import Path
import concurrent.futures,hashlib,json,urllib.request,zipfile,time
ROOT=Path(__file__).resolve().parents[1];BASE='https://lod-dressup-2.web.app/'
SRC=ROOT/'master/source/equipment/identity_v128';ASSETS=ROOT/'app/src/main/assets';j=json.loads((SRC/'catalog.json').read_text()); archive=SRC/'original_wearables.zip'
ids={a for a in j['icons'] if not a.startswith('mw_chungryong')}
tasks=[]
for a in sorted(ids):
 kind='weapon' if a.startswith('mw') else 'armor' if a.startswith('mu') else 'hair' if a.startswith('mh') else 'shoes' if a.startswith('ml') else 'shield'
 for rel in [f'data/type/{kind}/{a}.json',f'atlas/{kind}/{a}.webp']:tasks.append((a,rel))
existing={}
if archive.exists():
 with zipfile.ZipFile(archive) as z:existing={n:z.read(n) for n in z.namelist()}
def fetch(task):
 a,rel=task
 for attempt in range(3):
  try:
   data=existing.get(rel)
   if data is None:
    with urllib.request.urlopen(BASE+rel,timeout=25) as resp:data=resp.read()
   if rel.endswith('.json'):assert isinstance(json.loads(data).get('sprites'),dict)
   else:assert data[:4]==b'RIFF' and data[8:12]==b'WEBP'
   return a,rel,data
  except Exception as e:
   if attempt==2:return a,rel,None,str(e)
 return None
rows=[];success={};fail=[]
with concurrent.futures.ThreadPoolExecutor(max_workers=8) as ex:
 for n,result in enumerate(ex.map(fetch,tasks)):
  if len(result)==4:fail.append(dict(appearanceId=result[0],url=BASE+result[1],error=result[3]));continue
  a,rel,data=result;success[rel]=data;dest=ASSETS/('source-registration/'+a+'.json' if rel.endswith('.json') else 'wearable-atlases/'+a+'.webp');dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(data)
  rows.append(dict(appearanceId=a,url=BASE+rel,archiveMember=rel,assetPath=str(dest.relative_to(ASSETS)),sha256=hashlib.sha256(data).hexdigest(),bytes=len(data)))
  if (n+1)%80==0:print('RECOVERED',n+1,'/',len(tasks),flush=True)
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
 for rel,data in sorted(success.items()):z.writestr(rel,data)
(SRC/'retrieval.json').write_text(json.dumps(dict(sources=rows,failures=fail),ensure_ascii=False,indent=2)+'\n')
print('COMPLETE',len(success),'FAILURES',len(fail));assert not fail,fail
