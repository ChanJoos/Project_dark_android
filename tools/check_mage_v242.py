"""Independent compressed original RGB565 -> full packaged RGBA verification."""
from pathlib import Path
import json,hashlib,zipfile,zlib,struct,subprocess
import numpy as np
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets/skill-presentation';S=R/'master/source/mage_v242'
def sha(b):return hashlib.sha256(b).hexdigest()
m=json.loads((A/'mage-v242/manifest.json').read_text());ref=json.loads((S/'receipt.json').read_text());total=0
assert set(m['skills'])=={f'SK_마법사_{i:03}' for i in range(26,30)}
with zipfile.ZipFile(S/'original_effects.zip') as z:
 for n,h in ref['sourceFiles'].items():assert sha(z.read(n))==h
 for rec in ref['sequences']:
  raw=z.read(rec['source']);_,cnt,ms,blend=struct.unpack_from('<iiiB',raw);assert blend==2
  q=m['skills'][rec['id']]['channels']['RECIPIENT_CONTACT'];p=A/q['path'];assert sha(p.read_bytes())==q['atlasSha256'];im=Image.open(p);total+=im.width*im.height*4
  assert q['blend']=='SCREEN' and q['scale']==1 and q['footOffsetY']==0 and q['durationsMs']==[ms]*cnt
  assert len(rec['frames'])==cnt
  for j,f in enumerate(rec['frames']):
   v=struct.unpack_from('<10i2hi6hi',raw,64+64*j);data=zlib.decompress(raw[64+64*cnt+v[1]:64+64*cnt+v[1]+v[2]]);assert len(data)==v[3]
   fw,fh=f['sourceShape'];pixels=np.frombuffer(data[:v[8]],'<u2').reshape(fh,v[6]//2)[:,:fw].astype(np.uint32)
   rgb=np.stack(((pixels//2048)*255//31,((pixels//32)%64)*255//63,(pixels%32)*255//31),axis=-1).astype(np.uint8)
   expected=np.dstack((rgb,np.where(pixels!=0,255,0).astype(np.uint8)));assert sha(rgb.tobytes())==f['rgbSha256']
   assert sha(expected.tobytes())==f['sourceRgbaSha256']
   if f['displayCorrections']:
    assert rec['source']=='efct252.efa' and j==1 and len(f['displayCorrections'])==1
    assert np.all(pixels[0]==0x18e3) and not np.any(pixels[1:20])
    assert f['displayCorrections'][0]['rectangle']==[0,0,240,1];expected[0]=0
   x,y=f['placement'];ox=j%q['columns']*q['width'];oy=j//q['columns']*q['height'];actual=np.array(im.crop((ox+x,oy+y,ox+x+fw,oy+y+fh)))
   assert np.array_equal(actual,expected),(rec['id'],j);assert sha(expected.tobytes())==f['rgbaSha256'];assert [v[10],v[11]]==f['sourcePivot']
   cell=np.array(im.crop((ox,oy,ox+q['width'],oy+q['height'])))
   assert not np.any(cell[:2,:,3]) and not np.any(cell[-2:,:,3]) and not np.any(cell[:,:2,3]) and not np.any(cell[:,-2:,3])
assert total==ref['decodedBytes']<10000000
for path in ['app/src/main/assets/skill-presentation/mage-v240','app/src/main/assets/skill-presentation/mage-v241','app/src/main/assets/skills','app/src/main/java/com/projectdark/mobile/RuntimeState.java','app/src/main/java/com/projectdark/mobile/RuntimeCombatSession.java','app/src/main/java/com/projectdark/mobile/SkillAbilityCatalog.java','app/src/main/java/com/projectdark/mobile/SkillActionContract.java','app/src/main/java/com/projectdark/mobile/GameView.java','app/src/main/java/com/projectdark/mobile/SkillPolicyV227.java','assets/pote']:
 assert not subprocess.check_output(['git','diff','e0b38a12684b4086fb80297f1a5906f54dce97c3','--',path]),path
print('V2424circle nativeRGB565/all frames/headerinterval/pivots/padding/cost-domain guard PASS; decoded',total)
