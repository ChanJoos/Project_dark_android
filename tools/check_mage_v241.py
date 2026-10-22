"""Independent original EPF -> packaged RGBA pixel restoration and scope guard."""
from pathlib import Path
import json,hashlib,zipfile,struct
import numpy as np
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets/skill-presentation';S=R/'master/source/mage_v241'
def sha(b):return hashlib.sha256(b).hexdigest()
m=json.loads((A/'mage-v241/manifest.json').read_text());ref=json.loads((S/'receipt.json').read_text());total=0
assert set(m['skills'])=={f'SK_마법사_{n:03}' for n in (39,40,41,42,45,46,51,52)}
with zipfile.ZipFile(S/'original_effects.zip') as z:
 for n,h in ref['sourceFiles'].items():assert sha(z.read(n))==h
 pal=np.frombuffer(z.read('eff000.pal'),np.uint8).reshape(256,3)
 for rec in ref['sequences']:
  q=m['skills'][rec['id']]['channels']['RECIPIENT_CONTACT'];p=A/q['path'];assert sha(p.read_bytes())==q['atlasSha256'];im=Image.open(p);total+=im.width*im.height*4
  original=z.read(rec['source']);toc=struct.unpack_from('<i',original,8)[0]
  assert q['blend']=='NORMAL' and q['scale']==1 and q['footOffsetY']==0
  assert len(q['durationsMs'])==len(rec['frames'])
  for j,f in enumerate(rec['frames']):
   t,l,b,r,start,end=struct.unpack_from('<4h2i',original,12+toc+16*f['frame']);idx=np.frombuffer(original[12+start:12+start+(b-t)*(r-l)],np.uint8).reshape(b-t,r-l)
   expected=np.dstack((pal[idx],np.where(idx!=0,255,0).astype(np.uint8)));x,y=f['placement'];actual=np.array(im.crop((j%q['columns']*q['width']+x,j//q['columns']*q['height']+y,j%q['columns']*q['width']+x+r-l,j//q['columns']*q['height']+y+b-t)))
   assert np.array_equal(actual,expected),(rec['id'],j);assert sha(expected.tobytes())==f['rgbaSha256']
   cell=np.array(im.crop((j%q['columns']*q['width'],j//q['columns']*q['height'],(j%q['columns']+1)*q['width'],(j//q['columns']+1)*q['height'])))
   assert not np.any(cell[:2,:,3]) and not np.any(cell[-2:,:,3]) and not np.any(cell[:,:2,3]) and not np.any(cell[:,-2:,3])
assert total==ref['decodedBytes']<11000000
# Bindings/basic4/cost/learning/map are unchanged from the exact V240 delivered tree.
import subprocess
for path in ['app/src/main/assets/skill-presentation/mage-v240','app/src/main/assets/skills','app/src/main/java/com/projectdark/mobile/RuntimeState.java','app/src/main/java/com/projectdark/mobile/RuntimeCombatSession.java','app/src/main/java/com/projectdark/mobile/GameView.java','app/src/main/java/com/projectdark/mobile/SkillPolicyV227.java','assets/pote']:
 assert not subprocess.check_output(['git','diff','79527cd2cdb1c25f151ee6cb718c17711a010bcc','--',path]),path
print('V241 original eight exact RGBA/padding/foot/whole-frame/scope PASS; decoded',total)
