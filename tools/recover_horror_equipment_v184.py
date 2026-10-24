from pathlib import Path
import csv,json,zipfile,urllib.request,concurrent.futures,hashlib,io
from PIL import Image
root=Path(__file__).resolve().parents[1];out=root/'master/source/equipment/horror_v184';old=zipfile.ZipFile(root/'master/source/equipment/identity_v128/original_wearables.zip')
retained={}
if (out/'original_wearables.zip').exists():
 with zipfile.ZipFile(out/'original_wearables.zip') as z:retained={n:z.read(n) for n in z.namelist()}
ids=[2,3,4,5,10,11,12,13,14,15,16,17,18,19,20,21,22,23,34,35,36,37,38,39,40,41,42,43,44,47,48,52,53,54,55,56,69,76,88,92,95,97,98,99,100,133,137,170,186,203,284,288,290,295]
assets=list(csv.DictReader(open(root/'master/data/Asset_Master.csv',encoding='utf-8-sig')));by={a['id'].lower():a for a in assets};names=[f'mw{i:03}' for i in ids]+['ww096','ww141','ww145','ww289']+['mu0000036','mu0000037','mu0000038','mu0000040','wu0000038']
def get(a):
 row=by[a];kind=row['type'];paths=[f'data/type/{kind}/{a}.json',f'atlas/{kind}/{a}.webp'];values={}
 for p in paths:
  if p in retained:b=retained[p]
  elif p in old.namelist():b=old.read(p)
  else:
   with urllib.request.urlopen('https://lod-dressup-2.web.app/'+p,timeout=45) as response:b=response.read()
  values[p]=b
 j=json.loads(values[paths[0]]);im=Image.open(io.BytesIO(values[paths[1]]));count=0
 for group,frames in j['sprites'].items():
  for f in frames:
   assert f['sx']>=0 and f['sy']>=0 and f['sx']+f['w']<=im.width and f['sy']+f['h']<=im.height,(a,f,im.size)
   count+=1
 return a,values,{'appearanceId':a,'name':row['name'],'type':kind,'size':im.size,'frameCount':count,'files':{p:hashlib.sha256(b).hexdigest() for p,b in values.items()}}
results=[];fail=[]
with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
 futures={pool.submit(get,a):a for a in names}
 for f in concurrent.futures.as_completed(futures):
  a=futures[f]
  try:results.append(f.result());print('RECOVERED',a,flush=True)
  except Exception as e:fail.append({'id':a,'error':str(e)});print('FAILED',a,str(e),flush=True)
with zipfile.ZipFile(out/'original_wearables.zip','w',zipfile.ZIP_DEFLATED) as z:
 for a,values,row in sorted(results):
  for p,b in values.items():z.writestr(p,b)
(out/'receipt.json').write_text(json.dumps({'sourceBase':'https://lod-dressup-2.web.app/','status':'SOURCE_RECOVERY_NOT_FULL_RUNTIME_COMPLETION','assets':[r for a,v,r in sorted(results)],'failures':fail},ensure_ascii=False,indent=2)+'\n')
print('TOTAL',len(results),'FAILED',len(fail))
