"""Verify source provenance, bounded atlases and the user-corrected binding."""
from pathlib import Path
from PIL import Image
import json,hashlib
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets/skill-presentation'
m=json.loads((A/'mage-v240/manifest.json').read_text());total=0
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
print('V240 Pote basic4 source/frames/anchors/hash PASS; decoded',total)

# Scope locked by user: no other-circle/description/domain/forest changes.
UNCHANGED={'app/src/main/java/com/projectdark/mobile/SkillPolicyV227.java': '83a82e9e2abf2a4814e1d83feca6538a18233116cd29910b0c0ab8b510335608', 'app/src/main/assets/skill-presentation/mage-v238/manifest.json': '48bb9791f905766ea9e88c56dd2580cd1c199ff49d6ec3de78c4160f4760ff18', 'app/src/main/assets/skills/catalog.json': 'bab8a892ac23cc760124e8ccf30d4c3c1fb8279ff387785f9227a6aa45264c7f', 'app/src/main/assets/skills/mobile_learning.json': 'd9a2200cdb59e927937623c0eb7ae89028b044e6af516fdc2d8da22c1b13316e', 'app/src/main/assets/skill-presentation/mage-v235/references.json': '7522128cd53908c11a2453ed045ea0e741c8a73092fe290abce166a5b72c3881', 'app/src/main/assets/skill-presentation/mage-v239/manifest.json': '033fcbd00f08617a6a80521724d2c07555b11487c4a2ff87a07ed01fad714999', 'app/src/main/java/com/projectdark/mobile/RuntimeState.java': 'b236ff3e119bdb2487672addc95d48b6f78f04308a355a48ebb4e2d8b93753a5', 'app/src/main/java/com/projectdark/mobile/RuntimeCombatSession.java': '2956a0c26cfad87057038df0511162cadbbd091a7ae9e4828f83a45f6f398a47', 'app/src/main/java/com/projectdark/mobile/SkillActionContract.java': '07b7f1f39fb0cd6d5cfcf9283c3f4ea3f1cc07e8c9294fc63325d3c498a21553', 'app/src/main/java/com/projectdark/mobile/SkillAbilityCatalog.java': '16fa2dbd3d9e8135daa8dea8aa9283dc37a10b4989bf936b3375dae0126547b5', 'app/src/main/java/com/projectdark/mobile/GameView.java': 'b44b3217755883ad52baed34ec03c5e622d6a3dabb7e24caa2b0db7aa143f378', 'app/src/main/java/com/projectdark/mobile/PoteForestMonsterShowcase.java': 'c2f690f3c3d9e7996b27fe636e8cd58d7c705af5be2a8dec8c26ede221afb8b1'}
for path,digest in UNCHANGED.items():assert hashlib.sha256((R/path).read_bytes()).hexdigest()==digest,path
print("Other-circle/description/combat/map scope guard PASS")
