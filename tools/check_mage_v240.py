"""Verify source provenance, bounded atlases and the user-corrected binding."""
from pathlib import Path
from PIL import Image
import json,hashlib
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets/skill-presentation'
m=json.loads((A/'mage-v240/manifest.json').read_text());old=json.loads((A/'mage-v238/manifest.json').read_text());total=0
assert set(m['skills'])=={f'SK_마법사_{n:03}' for n in (1,3,4,5)}
for sid,data in m['skills'].items():
 q=data['channels']['RECIPIENT_CONTACT'];im=Image.open(A/q['path']);assert hashlib.sha256((A/q['path']).read_bytes()).hexdigest()==q['atlasSha256'];assert q['anchor']=='RECIPIENT' and q['footOffsetY']==0
 assert len(q['durationsMs'])==len(q['sourceFrames'])>=10;assert im.size==(q['columns']*q['width'],((len(q['durationsMs'])+q['columns']-1)//q['columns'])*q['height']);total+=im.width*im.height*4
 hashes=set()
 for i,f in enumerate(q['sourceFrames']):
  assert f['videoSha256']==m['sourceSha256'];assert len(f['crop'])==4
  hashes.add(hashlib.sha256(im.crop((i%8*q['width'],i//8*q['height'],(i%8+1)*q['width'],(i//8+1)*q['height'])).tobytes()).hexdigest())
 assert len(hashes)>=7
assert total<4_000_000
assert set(old['skills'])=={f'SK_마법사_{n:03}' for n in (13,14,15,16,46)}
assert old['screenAliases']=={f'SK_마법사_{30+i:03}':f'SK_마법사_{13+i:03}' for i in range(4)}
print('V240 basic4 source/frames/anchors/hash PASS; circle3/screen4 mapping PASS; decoded',total)
