"""Artifact provenance / untouched domain guards for the scoped video candidate."""
import hashlib,json
from pathlib import Path
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets/skill-presentation'
m=json.loads((A/'mage-v238/manifest.json').read_text());r=json.loads((R/'master/source/mage_video_v238/receipt.json').read_text())
assert m['sourceVideoSha256']==r['videoSha256'];assert not r['rawVideoCommitted']
assert set(m['skills'])=={'SK_마법사_'+str(i).zfill(3) for i in [13,14,15,16,46]}
for sid,data in m['skills'].items():
 q=data['channels']['RECIPIENT_CONTACT'];assert q['anchor']=='RECIPIENT';p=A/q['path'];assert hashlib.sha256(p.read_bytes()).hexdigest()==q['atlasSha256']
 image=Image.open(p);assert image.mode=='RGBA'
 assert image.width==q['width']*q['columns'];assert image.height>=q['height']*((len(q['durationsMs'])+q['columns']-1)//q['columns'])
 assert len(q['durationsMs'])==len(q['sourceFrames'])>=5
 hashes=[]
 for i,source in enumerate(q['sourceFrames']):
  assert source['videoSha256']==r['videoSha256'];assert len(source['crop'])==4
  assert 0<=source['timestampSeconds']<19.896833
  frame=image.crop((i%q['columns']*q['width'],i//q['columns']*q['height'],(i%q['columns']+1)*q['width'],(i//q['columns']+1)*q['height']))
  hashes.append(hashlib.sha256(frame.tobytes()).hexdigest())
 assert len(set(hashes))>=4
print('V2385 source hashes/crops/timestamps/real-changing frames PASS; no raw video admitted')
