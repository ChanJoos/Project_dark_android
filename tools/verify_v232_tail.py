"""Reuse first native evidence only under an exact single-predicate runtime delta."""
import subprocess,json
from pathlib import Path
BASE='4e9ca5253f6d78e7a733df584672bf2e178d3f14';f='app/src/main/java/com/projectdark/mobile/WeaponCombatProfile.java'
old=subprocess.check_output(['git','show',BASE+':'+f],text=True)
needle=' public static String motion(RpgProgressionState r,String requested){\n'
expected=old.replace(needle,needle+'  if("THRUST".equals(requested)&&"ROGUE".equals(r.currentJobCode()))return "ROGUE_STAB";\n')
assert expected!=old and Path(f).read_text()==expected
catalog='app/src/main/assets/skill-presentation/catalog.json'
oldCatalog=json.loads(subprocess.check_output(['git','show',BASE+':'+catalog],text=True));oldCatalog['profiles']['ROGUE_STAB']['idleEdges']=True
assert oldCatalog==json.loads(Path(catalog).read_text())
frame='app/src/main/java/com/projectdark/mobile/SkillPresentationCatalog.java';oldFrame=subprocess.check_output(['git','show',BASE+':'+frame],text=True)
needleFrame='float q=Math.max(0,Math.min(.999f,phase));'
assert Path(frame).read_text()==oldFrame.replace(needleFrame,needleFrame+'if(p.optBoolean("idleEdges")&&(q<.15f||q>=.70f))return frameKey(body,"IDLE",d,0);')
paths=subprocess.check_output(['git','diff','--name-only',BASE,'HEAD'],text=True).splitlines()
allowed={f,catalog,frame,'master/changes/V232_ROGUE_REPAIR.json','app/src/test/java/com/projectdark/mobile/SkillAbilityRuntimeTest.java','.github/workflows/rucesion-town-v156.yml','tools/ci_scope_v216.json','tools/verify_v232_tail.py'}
assert all(p in allowed or p.startswith(('docs/','verification/')) for p in paths),paths
# The broad five-job equipment journey fails before combat: legacy campaign gear aliases toggle
# the same item / lack verified source requirements. No requirement/commerce/item bytes changed.
for same in ['app/src/main/java/com/projectdark/mobile/RpgProgressionState.java','app/src/main/java/com/projectdark/mobile/TownCommerce.java','app/src/test/java/com/projectdark/mobile/AbelCombatV153Test.java']:
 assert subprocess.check_output(['git','rev-parse',BASE+':'+same],text=True)==subprocess.check_output(['git','rev-parse','3c2abf110f599eef535d82cc709e6108deb2141c:'+same],text=True)
print('Exact Rogue THRUST predicate and profile-only IDLE edges;all other runtime and all pixels unchanged. Reuse54 native PASS from39761907202;repair2tests then assemble. Excluded unrelated old5job equipment journey remainsFAILED/blocked;not full regression.')
