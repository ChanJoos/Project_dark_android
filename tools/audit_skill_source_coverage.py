#!/usr/bin/env python3
"""Per-ID evidence gaps. Presence of source pixels never implies mechanic or visual acceptance."""
import csv,json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];ASSETS=ROOT/'app/src/main/assets'
catalog=json.loads((ASSETS/'skills/catalog.json').read_text());decisions={r['id']:r for r in csv.DictReader((ROOT/'docs/SKILL_ACTION_DECISIONS.csv').open())}
icons=set(json.loads((ASSETS/'skills/source_icons.json').read_text())['icons']);effects={};noFx=set()
for directory in ['classic','rogue']:
 icons.update(r['id'] for r in json.loads((ASSETS/f'skill-presentation/{directory}/references.json').read_text())['rows'] if r.get('id'))
 for sid,row in json.loads((ASSETS/f'skill-presentation/{directory}/manifest.json').read_text())['skills'].items():
  if row['channels']:effects[sid]=directory+':'+','.join(row['channels'])
  else:noFx.add(sid)
# Finisher manifest uses a separately retained capture importer.
finisher=json.loads((ASSETS/'skill-presentation/captured/manifest.json').read_text()) if (ASSETS/'skill-presentation/captured/manifest.json').exists() else {}
for sid in finisher.get('skills',{}):effects.setdefault(sid,'captured:RECIPIENT_CONTACT_CASTER_REGISTERED')
rows=[]
for entry in catalog:
 sid=entry['id'];d=decisions[sid];mechanic=d['mechanic'];eligible=d['mode']=='ACTIVE' and mechanic!='PRESENTATION'
 rows.append(dict(id=sid,name=entry['name'],job=entry['job'],kind=entry['kind'],sourceIcon='RETAINED' if sid in icons else 'MISSING_VERIFIED_BINDING',body='SOURCE_PIXELS_PROJECT_SELECTION:'+d['motion'],sourceChannel=effects.get(sid,'REVIEWED_NO_PARTICLE' if sid in noFx else 'OTHER_CAPTURE_OR_ADAPTED_UNRESOLVED'),mechanic='ADAPTED_DAMAGE_HEAL' if eligible else 'PRESENTATION_OR_SERVICE_PENDING',device='PENDING',originalVisualAcceptance='PENDING'))
p=ROOT/'docs/SKILL_SOURCE_COVERAGE.csv'
with p.open('w',newline='') as f:w=csv.DictWriter(f,fieldnames=list(rows[0]),lineterminator='\n');w.writeheader();w.writerows(rows)
print('Catalog:',len(rows),'source icons:',sum(r['sourceIcon']=='RETAINED' for r in rows),'adapted damage/heal:',sum(r['mechanic']=='ADAPTED_DAMAGE_HEAL' for r in rows),'remaining service/presentation:',sum(r['mechanic']=='PRESENTATION_OR_SERVICE_PENDING' for r in rows))
