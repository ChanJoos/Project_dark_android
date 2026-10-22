"""Accepted article review overlay. Source snapshots, stable IDs and numeric rules stay intact."""
import csv,json
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets';p=json.loads((R/'master/changes/V229_ROGUE_ARTICLE_REVIEW.json').read_text());assert p['status']=='ACCEPTED'
def save(f,d):f.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
cat=json.loads((A/'skills/catalog.json').read_text())
for r in cat:r.update(p['catalog'].get(r['id'],{}))
save(A/'skills/catalog.json',cat)
learn=json.loads((A/'skills/mobile_learning.json').read_text())
for id,summary in p['summaries'].items():learn[id]['summary']=summary
save(A/'skills/mobile_learning.json',learn)
f=R/'docs/SKILL_ACTION_DECISIONS.csv'
with f.open() as src:reader=csv.DictReader(src);fields=reader.fieldnames;rules=list(reader)
for r in rules:
 if r['id'] in p['motions']:r['motion']=p['motions'][r['id']];r['motionEvidence']='USER_REVIEW_SOURCE_BODY_FAMILY_V229_EXACT_CADENCE_PENDING'
with f.open('w',newline='') as out:w=csv.DictWriter(out,fieldnames=fields,lineterminator='\n');w.writeheader();w.writerows(rules)
d=json.loads((A/'skill-presentation/catalog.json').read_text());d['profiles'].update(p['profiles'])
for r in d['skills']:
 if r['id'] in p['motions']:r['motion']=p['motions'][r['id']];r['poseEvidence']='USER_REVIEW_SOURCE_BODY_FAMILY_V229_EXACT_CADENCE_PENDING'
save(A/'skill-presentation/catalog.json',d)
with (R/'docs/SKILL_PRESENTATION_MAPPING.csv').open('w',newline='') as out:w=csv.DictWriter(out,fieldnames=list(d['skills'][0]),lineterminator='\n');w.writeheader();w.writerows(d['skills'])
print('V229: separate15/23 IDs,25 original article icons,16 per-ID reviewed source BODY routes')
