"""User-requested Horror-only verification; any unlisted change resumes common CI.
The baseline is the previously verified V179 source plus its rejection record.
Opt-in applies only to commits explicitly marked [horror-scoped].
"""
import subprocess,os,hashlib
from pathlib import Path
BASE='af190162322029e3c6785a1362f74d89d7f3101d'
allowed={'.github/workflows/android.yml','.github/workflows/rucesion-town-v156.yml','app/build.gradle','app/src/main/java/com/projectdark/mobile/HorrorCastleRenderer.java','app/src/main/java/com/projectdark/mobile/HorrorCastleLegacyRenderer.java','app/src/main/java/com/projectdark/mobile/HorrorCastleMonsters.java','app/src/main/java/com/projectdark/mobile/world/HorrorCastleGeometry.java','app/src/main/java/com/projectdark/mobile/world/HorrorCastleOriginalData.java','app/src/main/java/com/projectdark/mobile/world/PoteCampaignMapDef.java','app/src/test/java/com/projectdark/mobile/HorrorCastleV179Test.java','tools/build_horror_v180.py','tools/horror_original_formats.py','tools/horror_v180_ci_scope.py','app/src/main/java/com/projectdark/mobile/HorrorCastleTravel.java'}
SCOPED_SHARED_BASE='94605a053fabb9f699bf82e8eda20e9e9f799fb0'
SCOPED_SHARED_BLOBS={'app/src/main/java/com/projectdark/mobile/GameView.java': '7511565591e0d92296edc25ecef91af8ac8fc640', 'app/src/main/java/com/projectdark/mobile/RuntimeState.java': '60691a1597f75fec02b95c29ae6c29e5ea5ecd82'}
files=subprocess.check_output(['git','diff','--name-only',BASE,'HEAD'],text=True).splitlines()
other=[p for p in files if p not in allowed and not p.startswith(('master/source/horror_v180/','master/changes/V180_','docs/'))]
remaining=[]
for p in other:
 if p not in SCOPED_SHARED_BLOBS:remaining.append(p);continue
 blob=subprocess.check_output(['git','rev-parse','HEAD:'+p],text=True).strip()
 if blob!=SCOPED_SHARED_BLOBS[p]:remaining.append(p)
other=remaining
full=bool(other)
print('HORROR_SCOPED_ONLY' if not full else 'SHARED_CHANGE_REQUIRES_COMMON',other)
with Path(os.environ['GITHUB_OUTPUT']).open('a') as out:out.write('full='+str(full).lower()+'\n')
