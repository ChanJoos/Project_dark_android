"""Recover only exact named wearable sources; pin all original bytes before runtime admission."""
from pathlib import Path
import gzip,json,hashlib,io,urllib.request,concurrent.futures,sys,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'app/build/generated/wearables-v212'
CACHE=ROOT/'app/build/source-cache-v212'
CONTRACT=ROOT/'master/source/items/wearables_v212.json.gz'
KINDS={'무기':'weapon','갑옷':'armor','모자':'hair','신발':'shoes','방패':'shield'}
def digest(b):return hashlib.sha256(b).hexdigest()
def recover(spec):
 ident=spec['appearanceId'];kind=spec['kind'];result=dict(spec);data={}
 try:
  for tag,path in [('registration',f'data/type/{kind}/{ident}.json'),('atlas',f'atlas/{kind}/{ident}.webp')]:
   url='https://lod-dressup-2.web.app/'+path;local=CACHE/path;local.parent.mkdir(parents=True,exist_ok=True)
   if local.exists():raw=local.read_bytes()
   else:
    with urllib.request.urlopen(url,timeout=25) as r:raw=r.read()
    local.write_bytes(raw)
   if tag in spec:assert digest(raw)==spec[tag]['sha256'],f'{ident} {tag} original source changed'
   result[tag]={'url':url,'sha256':digest(raw),'bytes':len(raw)};data[tag]=raw
  j=json.loads(data['registration']);im=Image.open(io.BytesIO(data['atlas']));assert im.format=='WEBP';assert 'sprites' in j
  frames=j['sprites'];idle={f['f']for f in frames.get('01',[])}
  assert {0,1}<=idle and idle&{2,3,4,5} and idle&{6,7,8,9},f'incomplete original idle/walk: {sorted(idle)}'
  for f in frames['01']:
   if f['f'] in [0,1]:assert im.convert('RGBA').crop((f['sx'],f['sy'],f['sx']+f['w'],f['sy']+f['h'])).getchannel('A').getbbox(), 'empty original idle pose'
  for group,fs in frames.items():
   assert len({f['f']for f in fs})==len(fs)
   for f in fs:assert 0<=f['sx'] and 0<=f['sy'] and f['sx']+f['w']<=im.width and f['sy']+f['h']<=im.height
  result.update(status='EXACT_IDLE_WALK_SOURCE',atlasSize=list(im.size),missingIdleFrames=sorted(set(range(10))-idle),missingContactFrames=sorted(set(range(4))-{f['f']for f in frames.get('02',[])}))
  return result,data
 except Exception as e:return dict(spec,status='PENDING',reason=str(e)),None

def main():
 pin='--pin' in sys.argv
 if pin:
  records=json.loads((ROOT/'master/source/items/catalog_v210/catalog.json').read_text())['records'];specs=[]
  for r in records:
   if not r['inventoryOnly'] or r['slot'] not in KINDS:continue
   a=Path(r['previewPath']).name.split('_')[0]
   specs.append(dict(itemId=r['itemId'],name=r['name'],appearanceId=a,kind=KINDS[r['slot']]))
 else:specs=json.loads(gzip.decompress(CONTRACT.read_bytes()))['items']
 accepted=[];pending=[];OUT.mkdir(parents=True,exist_ok=True)
 with concurrent.futures.ThreadPoolExecutor(max_workers=12) as pool:
  for n,(result,data) in enumerate(pool.map(recover,specs),1):
   if data:
    accepted.append(result)
    for directory,ext,tag in [('wearables-v212/registrations','.json','registration'),('wearables-v212/atlases','.webp','atlas')]:
     p=OUT/directory/(result['appearanceId']+ext);p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data[tag])
   else:pending.append(result)
   if n%100==0:print('RECOVERED',n,'/',len(specs),'accepted',len(accepted),'pending',len(pending),flush=True)
 if pin:
  CONTRACT.parent.mkdir(parents=True,exist_ok=True);CONTRACT.write_bytes(gzip.compress(json.dumps(dict(revision='V212_EXACT_NAMED_WEARABLE_RECOVERY',items=accepted,pending=pending),ensure_ascii=False,separators=(',',':')).encode(),mtime=0))
 else:
  failures=[x for x in pending if any(t in x['reason'] for t in ['urlopen','HTTP','timed out','source changed'])]
  assert not failures,failures[:3]
  assert len(accepted)>1000,('incomplete wearable recovery',len(accepted),pending[:3])
 java=ROOT/'app/build/generated/wearables-java-v212/com/projectdark/mobile/RecoveredWearablesV212.java';java.parent.mkdir(parents=True,exist_ok=True)
 lines=['package com.projectdark.mobile;','final class RecoveredWearablesV212 {',' static void install(RpgProgressionState r){']
 for i in range((len(accepted)+99)//100):lines.append('  part%d(r);'%i)
 lines.append(' }')
 for i in range((len(accepted)+99)//100):
  lines.append(' static void part%d(RpgProgressionState r){'%i)
  for a in accepted[i*100:(i+1)*100]:lines.append('  r.bindRecoveredWearable('+json.dumps(a['itemId'])+','+json.dumps(a['appearanceId'])+');')
  lines.append(' }')
 lines.append(' static final String[] IDS={'+','.join(json.dumps(a['itemId']) for a in accepted)+'};')
 lines.append('}');java.write_text('\n'.join(lines)+'\n')
 receipt=ROOT/'app/build/reports/wearables-v212';receipt.mkdir(parents=True,exist_ok=True);(receipt/'sources.json').write_text(json.dumps(dict(items=accepted,pending=pending),ensure_ascii=False,indent=2)+'\n')
 with zipfile.ZipFile(receipt/'original_sources.zip','w',zipfile.ZIP_DEFLATED) as z:
  for a in accepted:
   for tag in ['registration','atlas']:
    path=a[tag]['url'].split('web.app/')[1];z.writestr(path,(CACHE/path).read_bytes())
 print('V212_WEARABLES',len(accepted),'pending',len(pending),'bytes',sum(x['atlas']['bytes']for x in accepted),flush=True)
if __name__=='__main__':main()
