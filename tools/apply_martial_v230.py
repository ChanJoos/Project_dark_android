"""Accepted V230 user corrections after V228/V229; leave source snapshots intact."""
import csv,json
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets';p=json.loads((R/'master/changes/V230_MARTIAL_REPAIR.json').read_text());assert p['status']=='ACCEPTED'
f=R/'docs/SKILL_ACTION_DECISIONS.csv'
with f.open() as src:reader=csv.DictReader(src);fields=reader.fieldnames;rules=list(reader)
for r in rules:r.update(p['rules'].get(r['id'],{}))
with f.open('w',newline='') as out:w=csv.DictWriter(out,fieldnames=fields,lineterminator='\n');w.writeheader();w.writerows(rules)
keys=['id','name','mode','pattern','reach','minReach','mechanic','hits','contact','spatialEvidence','note','kind'];lines=['package com.projectdark.mobile;','','/** Generated from the reviewed per-ID decisions; do not infer rules from skill names. */','final class SkillActionData {','  static final String[][] ROWS={']
for r in rules:lines.append('    {'+', '.join(json.dumps(str(r[k]),ensure_ascii=False) for k in keys)+'},')
lines+=[' };','}'];(R/'app/src/main/java/com/projectdark/mobile/SkillActionData.java').write_text('\n'.join(lines)+'\n')
d=json.loads((A/'skill-presentation/catalog.json').read_text())
for r in d['skills']:r.update(p['presentation'].get(r['id'],{}))
(A/'skill-presentation/catalog.json').write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
with (R/'docs/SKILL_PRESENTATION_MAPPING.csv').open('w',newline='') as out:w=csv.DictWriter(out,fieldnames=list(d['skills'][0]),lineterminator='\n');w.writeheader();w.writerows(d['skills'])
print('V230 martial user corrections applied')
