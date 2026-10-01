#!/usr/bin/env python3
"""Keep only actual missing labelled particle evidence in the residual queue."""
import csv, hashlib, json
from pathlib import Path
R=Path(__file__).resolve().parents[1]
read=lambda p:json.loads((R/p).read_text())
audit=read('docs/verification/SKILL_FX_V79_AUDIT.json')
previous=read('docs/verification/SKILL_FX_V77_AUDIT.json')
before={r['id']:r for r in previous['rows'] if r['runtimeBranch']=='ADAPTED'}
current={r['id']:r for r in audit['rows']}
resolved=[];remaining=[]
reasons={
 'SK_도적_015':('LABEL_CONFLICT','Article 245450 places the GIF under 습격 but its text calls it 기습; cannot certify 습격-specific pixels.'),
 'SK_도적_001':('NO_LABELLED_DEMONSTRATION','No labelled basic 찌르기 particle demonstration in the retained articles or attachments.'),
 'SK_성직자_051':('NEWER_VARIANT_NOT_LABELLED','Older buff captures do not identify 블레스; cannot borrow blessing pixels by resemblance.'),
 'SK_성직자_052':('NEWER_VARIANT_NOT_LABELLED','Holy Bolt/Blow/Dragon are different names; no labelled 홀리쇼크 demonstration.'),
 'SK_공통_001':('BODY_NOT_INDEPENDENT_SOURCE_FX','Approved ATTACK BODY is connected; an independent original basic-hit particle is not identified.')}
for sid,old in before.items():
 row=current[sid]
 if row['runtimeBranch']!='ADAPTED':
  resolved.append(dict(id=sid,name=row['name'],route=row['runtimeBranch'],
     status='SOURCE_CHANNEL_CONNECTED' if row['channels'] else 'USER_APPROVED_BODY_ONLY_NO_PARTICLE' if row['runtimeBranch']=='NO_PARTICLE' else 'DEMONSTRATION_LINKED_NO_SEPARATELY_RECOVERABLE_PARTICLE',
     source=[c['source'] for c in row['channels']] or ([row['reviewedSourceWithoutChannel']['source']] if row['reviewedSourceWithoutChannel'] else []),
     warnings=row['warnings']))
 else:
  code,reason=reasons.get(sid,('NO_LABELLED_EFFECT_FRAMES','Supplied name/requirements/description evidence is not labelled effect frames; no exact-name recoverable demonstration in the retained source corpus.'))
  remaining.append(dict(id=sid,name=row['name'],job=row['job'],reasonCode=code,reason=reason,
     runtime='Existing project fallback; NOT original',needed='Labelled original use/impact footage or sprite frames'))
sourceChecks=[]
for path in ['master/source/skill_fx/naver_cafe_archive_20261001/media.json',
             'master/source/skill_fx/naver_magic_recovered_20261001/media.json']:
 for row in read(path)['media']:
  p=R/row['path'];assert hashlib.sha256(p.read_bytes()).hexdigest()==row['sha256'],str(p)
  sourceChecks.append(row['path'])
attachment=read('master/source/skill_fx/user_20260930/provenance.json')
unlabelled=[]
for row in attachment['files']:
 p=R/'master/source/skill_fx/user_20260930'/row['file']
 assert hashlib.sha256(p.read_bytes()).hexdigest()==row['sha256']
 if row['classification'].startswith('UNLABELLED'):
  unlabelled.append(dict(file=row['file'],sha256=row['sha256'],reason='No skill label. Motif resemblance to dragon/fire does not identify a skill or original era.'))
outside=[dict(name=r['name'],source=r['article'],reason='No current catalog ID; no automatic later-job content expansion.')
         for r in read('master/source/skill_fx/naver_magic_recovered_20261001/definitions.json') if not r['id']]
out=dict(revision='PROVIDED_SOURCE_RECHECK_V79',base='d57e480c1342f8f9ad7936b3c3f25d4630d9345c',
 catalogCount=len(current),initialFallbackCount=len(before),resolved=resolved,
 remainingOriginalEffectCount=len(remaining),remaining=remaining,
 verifiedRetainedMediaReferences=len(sourceChecks),unlabelledUserGIFs=unlabelled,
 outsideCurrentCatalog=outside,
 sharedRouteBoundary='All four shared routes are project reuse, not per-job original footage proof.',
 device='PENDING',visualAcceptance='PENDING')
(R/'docs/verification/PROVIDED_SOURCE_RECHECK_V79.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n')
with (R/'docs/REMAINING_ORIGINAL_SKILL_FX.csv').open('w',newline='') as f:
 w=csv.DictWriter(f,fieldnames=list(remaining[0]),lineterminator='\n');w.writeheader();w.writerows(remaining)
print('Rechecked',len(before),'fallback IDs;',len(resolved),'resolved routes;',len(remaining),'actual particle gaps;',len(unlabelled),'unlabelled GIFs;',len(outside),'outside catalog')
