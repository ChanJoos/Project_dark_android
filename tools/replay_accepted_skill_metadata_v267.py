"""Replay explicit user-approved metadata after legacy generators; no gameplay rules."""
import csv,json
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets'
def apply():
 change=json.loads((R/'master/changes/V267_GENERATOR_REPLAY.json').read_text());assert change['status']=='ACCEPTED_TOOLING_REPAIR'
 f=A/'skills/abilities.json';rows=json.loads(f.read_text())
 for row in rows:row.update(change['abilities'].get(row['id'],{}))
 f.write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
 with (R/'docs/SKILL_ABILITY_FORMULAS.csv').open('w',newline='') as out:
  w=csv.DictWriter(out,fieldnames=list(rows[0]),lineterminator='\n');w.writeheader();w.writerows(rows)
 keys=['id','name','job','circle','kind','formula','coefficient','status','mpCost','cooldown','duration','allMp','evidence','description','cooldownEvidence','cooldownSource']
 lines=['package com.projectdark.mobile;','/** Generated per-ID abilities with accepted source overlays. */','final class SkillAbilityData {',' static final String[][] ROWS={']
 for r in rows:lines.append('  {'+','.join(json.dumps(str(r[k]).lower() if isinstance(r[k],bool) else str(r[k]),ensure_ascii=False) for k in keys)+'},')
 lines+=[' };','}'];(R/'app/src/main/java/com/projectdark/mobile/SkillAbilityData.java').write_text('\n'.join(lines)+'\n')
 f=A/'skill-presentation/catalog.json';d=json.loads(f.read_text())
 for row in d['skills']:row.update(change['presentation'].get(row['id'],{}))
 f.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
 with (R/'docs/SKILL_PRESENTATION_MAPPING.csv').open('w',newline='') as out:
  w=csv.DictWriter(out,fieldnames=list(d['skills'][0]),lineterminator='\n');w.writeheader();w.writerows(d['skills'])
 print('V267 approved descriptions/notes replayed; formulas/status/routes untouched')
if __name__=='__main__':apply()
