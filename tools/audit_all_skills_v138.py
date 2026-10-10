"""Exact per-ID whole catalog audit; source evidence gaps remain explicit."""
import csv,json,collections,hashlib
from pathlib import Path
root=Path(__file__).resolve().parents[1]
cat=json.loads((root/'app/src/main/assets/skills/catalog.json').read_text())
a={r['id']:r for r in json.loads((root/'app/src/main/assets/skills/abilities.json').read_text())}
r={r['id']:r for r in csv.DictReader((root/'docs/SKILL_ACTION_DECISIONS.csv').open())}
v={r['id']:r for r in json.loads((root/'app/src/main/assets/skill-presentation/catalog.json').read_text())['skills']}
assert len(cat)==len(a)==len(r)==len(v)==221
exceptions={'이모탈':5,'리플렉토':5,'나르콜리':1,'다라밀공':12}
rows=[]
for e in cat:
 b,s,p=a[e['id']],r[e['id']],v[e['id']]
 spell=e['kind']=='마법'
 if spell:assert b['cooldown']==exceptions.get(e['name'],0),(e['id'],b['cooldown'])
 base=0 if e['name']=='쿠로토' or e['id']=='SK_무도가_016' else 1 if e['id'] in ['SK_마법사_001','SK_마법사_003','SK_마법사_004','SK_마법사_005'] else 3 if e['name']=='홀리쇼크' else 4 if e['name']=='홀리블로우' else 5 if e['id'] in ['SK_성직자_055','SK_공통_004'] else float(s['contact'])
 route='UNAVAILABLE_SERVICE' if b['kind']=='SERVICE' or b['status']=='TRANSFER' else 'LINKED_PASSIVE' if s['mode']=='LINKED' or s['pattern']=='PASSIVE' else 'UTILITY' if s['mode']=='UTILITY' else 'RESOLVER'
 rows.append(dict(id=e['id'],name=e['name'],job=e['job'],kind=e['kind'],route=route,mechanic=b['kind'],formula=b['formula'],formulaEvidence=b['evidence'],mp=b['mpCost'],allMp=b['allMp'],reuse=b['cooldown'],reuseEvidence='PDF_EXCEPTION' if spell and e['name'] in ['이모탈','리플렉토','나르콜리'] else 'RETAINED_ADAPTED_EXCEPTION' if e['name']=='다라밀공' else 'SPELL_NO_INVENTED_REUSE' if spell else 'RETAINED_ADAPTED_PHYSICAL',castBase=base,castEvidence='OFFICIAL_RETAINED' if e['name'] in ['홀리쇼크','홀리블로우'] else 'SUPPLIED_PDF' if e['id'] in ['SK_무도가_016','SK_성직자_055','SK_공통_004'] else 'ADAPTED_OR_RETAINED_PENDING_ORIGINAL',sharedInterval=1/3 if spell else 0,pattern=s['pattern'],reach=s['reach'],minReach=s['minReach'],spatialEvidence=s['spatialEvidence'],motion=p['motion'],fxEvidence=p['effectEvidence'],originalFxVerified=p['originalEffectVerified'],phone='PENDING'))
report=dict(catalogCount=len(rows),kindCounts=dict(collections.Counter(x['kind'] for x in rows)),routeCounts=dict(collections.Counter(x['route'] for x in rows)),rows=rows,scope='STATIC_FULL_221; native execution separately verified by CI. Not all original mechanics or phone acceptance.',sourceHashes={x:hashlib.sha256((root/x).read_bytes()).hexdigest() for x in ['app/src/main/assets/skills/catalog.json','app/src/main/assets/skills/abilities.json','docs/SKILL_ACTION_DECISIONS.csv']})
out=root/'master/changes/V138_ALL221_SKILL_AUDIT.json';out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(report['kindCounts'],report['routeCounts'])
