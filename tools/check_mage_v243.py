"""Independent compressed-source, scoped-data and generator idempotence checks."""
from pathlib import Path
import json,hashlib,struct,zlib,zipfile,subprocess,csv
import numpy as np
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets';S=R/'master/source/mage_v243';BASE='68dfb93ec466e0caeaa240d0033043a095db2890'
sha=lambda b:hashlib.sha256(b).hexdigest()
def old(path):return json.loads(subprocess.check_output(['git','show',BASE+':'+str(path)],cwd=R))
p=json.loads((R/'master/changes/V243_CIRCLE3_CROSS.json').read_text());links={r['id']:r['single'] for r in p['rows']};assert len(links)==8 and p['status']=='ACCEPTED'
m=json.loads((A/'skill-presentation/mage-v243/manifest.json').read_text());ref=json.loads((S/'receipt.json').read_text());total=0
assert set(m['skills'])=={f'SK_마법사_{i:03}' for i in range(13,17)}
with zipfile.ZipFile(S/'original_effects.zip') as z:
 for rec in ref['sequences']:
  raw=z.read(rec['source']);assert sha(raw)==rec['sourceSha256'];_,count,ms,blend=struct.unpack_from('<iiiB',raw);assert blend in (1,2)
  q=m['skills'][rec['id']]['channels']['RECIPIENT_CONTACT'];path=A/'skill-presentation'/q['path'];assert sha(path.read_bytes())==q['atlasSha256'];atlas=Image.open(path);total+=atlas.width*atlas.height*4
  assert q['durationsMs']==[ms]*count and q['blend']==('ADDITIVE' if blend==1 else 'SCREEN') and q['footOffsetY']==0 and q['scale']==1
  for j,f in enumerate(rec['frames']):
   v=struct.unpack_from('<10i2hi6hi',raw,64+64*j);data=zlib.decompress(raw[64+64*count+v[1]:64+64*count+v[1]+v[2]]);assert len(data)==v[3]
   fw,fh=f['sourceSize'];words=np.frombuffer(data[:v[8]],'<u2').reshape(fh,v[6]//2)[:,:fw].astype(np.uint32)
   rgb=np.stack(((words//2048)*255//31,((words//32)%64)*255//63,(words%32)*255//31),axis=-1).astype(np.uint8);rgba=np.dstack((rgb,np.where(words!=0,255,0).astype(np.uint8)))
   assert sha(rgba.tobytes())==f['rgbaSha256'] and [v[10],v[11]]==f['sourcePivot'];x,y=f['placement'];ox=j%q['columns']*q['width'];oy=j//q['columns']*q['height'];cell=np.array(atlas.crop((ox,oy,ox+q['width'],oy+q['height'])))
   assert np.array_equal(cell[y:y+fh,x:x+fw],rgba) and x+f['sourcePivot'][0]==q['pivotX'] and y+f['sourcePivot'][1]==q['pivotY']
   assert not np.any(cell[:2,:,3]) and not np.any(cell[-2:,:,3]) and not np.any(cell[:,:2,3]) and not np.any(cell[:,-2:,3])
assert total==ref['decodedBytes']<8000000
abilities=json.loads((A/'skills/abilities.json').read_text());byid={r['id']:r for r in abilities};original=old('app/src/main/assets/skills/abilities.json')
for before,after in zip(original,abilities):
 assert before['id']==after['id'];sid=after['id']
 if sid in links:
  assert after['formula']=='MAGIC' and after['coefficient']==byid[links[sid]]['coefficient']
  assert {k:v for k,v in after.items() if k not in {'coefficient','description','evidence','source'}}=={k:v for k,v in before.items() if k not in {'coefficient','description','evidence','source'}}
 else:assert before==after,sid
for path,field in [('app/src/main/assets/skills/catalog.json','effect'),('app/src/main/assets/skills/mobile_learning.json','summary'),('app/src/main/assets/skill-presentation/mage-v235/references.json','description')]:
 before=old(path);after=json.loads((R/path).read_text())
 if path.endswith('references.json'):before=before['rows'];after=after['rows']
 if isinstance(after,list):before={r['id']:r for r in before};after={r['id']:r for r in after}
 for sid in after:
  assert {k:v for k,v in after[sid].items() if not(sid in links and k==field)}=={k:v for k,v in before[sid].items() if not(sid in links and k==field)},(path,sid)
for path in ['app/src/main/assets/skill-presentation/mage-v240','app/src/main/assets/skill-presentation/mage-v241','app/src/main/assets/skill-presentation/mage-v242','app/src/main/java/com/projectdark/mobile/GameView.java','app/src/main/java/com/projectdark/mobile/RuntimeCombatPortAdapter.java','app/src/main/java/com/projectdark/mobile/RuntimeState.java','app/src/main/java/com/projectdark/mobile/SkillAbilityCatalog.java','app/src/main/java/com/projectdark/mobile/SkillActionContract.java','assets/pote']:
 assert not subprocess.check_output(['git','diff',BASE,'--',path],cwd=R),path
paths=['app/src/main/assets/skills/abilities.json','app/src/main/assets/skills/catalog.json','app/src/main/assets/skills/mobile_learning.json','app/src/main/assets/skill-presentation/mage-v235/references.json','app/src/main/java/com/projectdark/mobile/SkillActionData.java','app/src/main/java/com/projectdark/mobile/SkillAbilityData.java','docs/SKILL_ABILITY_FORMULAS.csv','docs/SKILL_ACTION_DECISIONS.csv']
prior={p:sha((R/p).read_bytes()) for p in paths};subprocess.run(['python3','tools/apply_mage_v243.py'],cwd=R,check=True);assert prior=={p:sha((R/p).read_bytes()) for p in paths}
print('V243 original36frames/sourceRGB565/pitch/interval/blend/foot/padding PASS; eight paired powers/MP/acquisition/unrelated data guard/overlay idempotence PASS; decoded',total)
