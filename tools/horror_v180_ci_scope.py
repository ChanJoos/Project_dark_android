"""User-requested Horror-only verification; any unlisted change resumes common CI.
The baseline is the previously verified V179 source plus its rejection record.
Opt-in applies only to commits explicitly marked [horror-scoped].
"""
import subprocess,os,hashlib
from pathlib import Path
BASE='af190162322029e3c6785a1362f74d89d7f3101d'
allowed={'.github/workflows/android.yml','.github/workflows/rucesion-town-v156.yml','app/build.gradle','app/src/main/java/com/projectdark/mobile/HorrorCastleRenderer.java','app/src/main/java/com/projectdark/mobile/HorrorCastleLegacyRenderer.java','app/src/main/java/com/projectdark/mobile/HorrorCastleMonsters.java','app/src/main/java/com/projectdark/mobile/world/HorrorCastleGeometry.java','app/src/main/java/com/projectdark/mobile/world/HorrorCastleOriginalData.java','app/src/main/java/com/projectdark/mobile/world/PoteCampaignMapDef.java','app/src/test/java/com/projectdark/mobile/HorrorCastleV179Test.java','tools/build_horror_v180.py','tools/horror_original_formats.py','tools/horror_v180_ci_scope.py','app/src/main/java/com/projectdark/mobile/HorrorCastleTravel.java'}
SCOPED_SHARED_BASE='94605a053fabb9f699bf82e8eda20e9e9f799fb0'
SCOPED_SHARED_DIFFS={'app/src/main/java/com/projectdark/mobile/GameView.java': '1ba30d9ec5dbd80954c7105ce648a700bb1028c3a192d32b13a435ffa661e1e7', 'app/src/main/java/com/projectdark/mobile/RuntimeState.java': 'f7575721e5c5520d2b259cd0fc4c389d4478bf51764319fa80d06afdf040e15b'}
files=subprocess.check_output(['git','diff','--name-only',BASE,'HEAD'],text=True).splitlines()
other=[p for p in files if p not in allowed and not p.startswith(('master/source/horror_v180/','master/changes/V180_','docs/'))]
remaining=[]
for p in other:
 if p not in SCOPED_SHARED_DIFFS:remaining.append(p);continue
 subprocess.run(['git','fetch','--depth=1','origin',SCOPED_SHARED_BASE],check=True)
 patch=subprocess.check_output(['git','diff',SCOPED_SHARED_BASE,'HEAD','--',p])
 if hashlib.sha256(patch).hexdigest()!=SCOPED_SHARED_DIFFS[p]:remaining.append(p)
other=remaining
full=bool(other)
print('HORROR_SCOPED_ONLY' if not full else 'SHARED_CHANGE_REQUIRES_COMMON',other)
with Path(os.environ['GITHUB_OUTPUT']).open('a') as out:out.write('full='+str(full).lower()+'\n')
