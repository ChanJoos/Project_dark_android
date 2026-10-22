"""Focused source/asset contract, before Android compile and native execution."""
from pathlib import Path
import json,hashlib,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 source=ROOT/'master/source/pote_v239';ref=json.loads((source/'reference.json').read_text())
 with zipfile.ZipFile(source/'original_monsters.zip') as z:
  for n,h in ref['sourceFiles'].items():assert hashlib.sha256(z.read(n)).hexdigest()==h,n
 poses=json.loads((source/'poses.json').read_text())['records'];assert len(poses)==132
 for row in poses:
  p=ROOT/row['path'];assert sha(p)==row['sha256'];im=Image.open(p);assert im.size==(192,192) and im.mode=='RGBA';assert im.getbbox()
 terrain=json.loads((source/'terrain.json').read_text());assert sha(ROOT/'assets/pote/production/reference_v239/forest_floor.png')==terrain['sha256']
 magic=ROOT/'app/src/main/assets/skill-presentation/mage-v239';skills=json.loads((magic/'manifest.json').read_text())['skills']
 assert set(skills)=={f'SK_마법사_{n:03}' for n in (39,41,42,51,52)}
 for sid,row in skills.items():
  q=row['channels']['RECIPIENT_CONTACT'];assert q['anchor']=='RECIPIENT';assert len(q['durationsMs'])>=10
  p=ROOT/'app/src/main/assets/skill-presentation'/q['path'];assert sha(p)==q['atlasSha256'];im=Image.open(p);assert im.getbbox()
  frames=[im.crop((i%q['columns']*q['width'],i//q['columns']*q['height'],(i%q['columns']+1)*q['width'],(i//q['columns']+1)*q['height'])).tobytes() for i in range(len(q['durationsMs']))]
  assert len(set(frames))>=4,sid
 print('V239 retained MPF hashes,132 original pose samples, soil source and5 changing FX atlas contracts PASS')
if __name__=='__main__':main()
