"""Read-only current skill material audit, including UI/meteor overrides absent from old CSVs."""
from pathlib import Path
import json,csv,re,hashlib,collections,html,subprocess
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'app/src/main/assets'
def read(p):return json.loads(p.read_text())
cat=read(A/'skills/catalog.json');abilities={r['id']:r for r in read(A/'skills/abilities.json')}
presentation={r['id']:r for r in read(A/'skill-presentation/catalog.json')['skills']}
rules={r['id']:r for r in csv.DictReader((ROOT/'docs/SKILL_ACTION_DECISIONS.csv').open())}
icons={};missing_files=[]
source=read(A/'skills/source_icons.json');atlas=A/'skills/source_icons.png';assert atlas.is_file()
for sid,index in source['icons'].items():icons[sid]=dict(path=str(atlas.relative_to(ROOT)),cell=index)
directories=['rogue','classic','warrior','shared','captured'];manifests={}
for d in directories:
 manifests[d]=read(A/f'skill-presentation/{d}/manifest.json')['skills']
 if d!='captured':
  for r in read(A/f'skill-presentation/{d}/references.json')['rows']:
   if not r.get('id'):continue
   path=A/f'skill-presentation/{d}'/r['iconAssetPath']
   if not path.is_file():missing_files.append(str(path))
   else:icons[r['id']]=dict(path=str(path.relative_to(ROOT)))
for sid,r in read(A/'skill-window/manifest.json')['crops'].items():
 if sid.startswith('SK_'):
  path=A/r['path'];assert path.is_file();icons[sid]=dict(path=str(path.relative_to(ROOT)),route='UI_OVERRIDE')
meteor=A/'skill-presentation/screen-magic-v204/meteor-icon.png';assert meteor.is_file()
icons['SK_마법사_043']=dict(path=str(meteor.relative_to(ROOT)),route='METEOR_V204')
assert not missing_files,missing_files
skull=set(re.findall(r'case "(SK_[^"]+)"',(ROOT/'app/src/main/java/com/projectdark/mobile/CurseSkullFx.java').read_text()))
old_gaps={r['id']:r for r in csv.DictReader((ROOT/'docs/REMAINING_ORIGINAL_SKILL_FX.csv').open())}
java=[]
for line in (ROOT/'app/src/main/java/com/projectdark/mobile/SkillAbilityData.java').read_text().splitlines():
 line=line.strip().rstrip(',')
 if line.startswith('{"SK_'):java.append(json.loads('['+line[1:-1]+']'))
assert len(java)==221
for r in java:assert (r[4],r[7])==(abilities[r[0]]['kind'],abilities[r[0]]['status'])
rows=[]
for e in cat:
 sid=e['id'];ability=abilities[sid];p=presentation[sid];rule=rules[sid]
 iconid='SK_무도가_006' if sid=='SK_무도가_001' else sid
 selected=next((d for d in directories if sid in manifests[d]),'NO_PARTICLE' if p['caster']=='NONE' and p['target']=='NONE' else 'ADAPTED')
 channels=[]
 if selected in manifests:
  item=manifests[selected][sid]
  for key,v in item.get('channels',{'RECIPIENT_CONTACT':item}).items():
   path=A/'skill-presentation'/v['path'];assert path.is_file(),path
   channels.append(dict(phase=key,path=str(path.relative_to(ROOT)),peakPixels=max(v.get('opaquePixelCounts',[0])),frames=len(v.get('durationsMs',[]))))
 if sid=='SK_마법사_043':selected='METEOR_SOURCE_V204'
 elif sid in skull:selected='CURSE_SKULL_V204_SOURCE_SHAPE_PROJECT_TIMING'
 service=ability['kind']=='SERVICE' or ability['status']=='TRANSFER'
 rows.append(dict(id=sid,name=e['name'],job=e['job'],kind=e['kind'],stage=e['stage'],icon='CONNECTED' if iconid in icons else 'MISSING_BINDING',iconId=iconid,iconSource=icons.get(iconid),fxRoute=selected,channels=channels,missingLabelledOriginalFx=sid in old_gaps,fxReason=old_gaps.get(sid,{}).get('reasonCode'),standalonePresentation=rule['mode'] not in ['LINKED','UTILITY'] and rule['pattern'] not in ['PASSIVE','UTILITY','UNRESOLVED'],bodyProfile=p['motion'],mechanicKind=ability['kind'],mechanicStatus=ability['status'],mechanicUnavailable=service,formulaEvidence=ability['evidence'],description=e['effect'],needsUser=['스킬명과 아이콘이 함께 보이는 원작 스킬창 스크린샷'] if iconid not in icons else [],needsImplementation=service,device='USER_REPORTED_MISSING_SKILLS_NOT_IDENTIFIED_PER_ID'))
 forneed=rows[-1]['needsUser']
 if sid in old_gaps:forneed.append('스킬명 확인 가능한 원작 시전→명중/상태 적용→종료 영상. 별도 이펙트가 없다면 동작만 확인')
 if service:forneed.append('원작 대상/선택 메뉴/HP·MP·상태 변화 참고 자료; 서비스 구현은 개발 작업')
counts=dict(total=len(rows),iconsConnected=sum(r['icon']=='CONNECTED' for r in rows),iconsMissing=sum(r['icon']=='MISSING_BINDING' for r in rows),originalFxUnconfirmed=sum(r['missingLabelledOriginalFx'] for r in rows),unavailableMechanics=sum(r['mechanicUnavailable'] for r in rows),fxRoutes=dict(collections.Counter(r['fxRoute'] for r in rows)))
assert (counts['total'],counts['iconsMissing'],counts['originalFxUnconfirmed'],counts['unavailableMechanics'])==(221,42,32,20)
report=dict(revision='V226_SKILL_MATERIAL_AUDIT',baseCommit='93f7651818fd46552ca077ca667edeb2f96bd0b1',asOfKST='2026-10-22',scope='Static current loader/manifest/Java route audit; no new runtime/phone verification',counts=counts,findings=['기존 SKILL_SOURCE_COVERAGE 174 icons / 47 gaps omits skin overrides and Meteor; actual connected179 / missing42.','32 missing original FX use adapted presentation; not 32 entirely effectless skills.','41 NO_PARTICLE routes include linked/passive/utility/body-only. Never request 41 invented particles.','19 SERVICE plus transfusion TRANSFER are unavailable regardless of pictures; implementation and some original mechanics remain missing.','12 classic caster channels have very low retained pixel counts (21–26); presence does not prove visually adequate output.','Original BODY pose per skill, 4 facings, staff/weapon contact, cast/release/recovery and many original formulas remain unverified.','Source GIF17-8 is Meteor and already superseded by V204; do not re-request. GIF16-7 remains unlabelled.','Supplied 28-page skill PDF has only two embedded image objects (site graphics), no individual icon sheets. Supplied9icon screenshots already used; do not re-request whole existing set.'],rows=rows,unlabelledGif=dict(file='다운로드파일-7.gif',path='master/source/skill_fx/user_20260930/16-7.gif',needs='스킬명 또는 원문 링크; 외형만으로 홀리 계열 등 추정 연결하지 않음'),sourceHashes={n:hashlib.sha256((ROOT/n).read_bytes()).hexdigest() for n in ['app/src/main/java/com/projectdark/mobile/SkillIconCatalog.java','app/src/main/java/com/projectdark/mobile/SkillWindowSkin.java','app/src/main/java/com/projectdark/mobile/SkillVfxRenderer.java','app/src/main/java/com/projectdark/mobile/SkillAbilityExecutor.java','app/src/main/java/com/projectdark/mobile/SkillAbilityData.java']})
out=ROOT/'docs/research/SKILL_GAPS_V226.json';out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
esc=lambda x:html.escape(str(x))
parts=['<!doctype html><html lang="ko"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>스킬 자료 요청 목록 V226</title><style>body{max-width:1300px;margin:30px auto;padding:0 18px;font:16px/1.65 system-ui;color:#203046;background:#f6f8fb}h1,h2{line-height:1.4}table{border-collapse:collapse;width:100%;background:white;margin-bottom:24px}td,th{padding:10px;border:1px solid #dbe2ec;vertical-align:top;text-align:left}th{background:#e7edf6}small{color:#58687b}li{margin:7px 0}a{color:#1251a1}</style><h1>원작 스킬 자료 요청 목록</h1><p>2026-10-22 · 현재 등록221개 전수 확인 · 아이콘 누락42 · 원작 이펙트 미확인32 · 기능 미지원20. 정적 코드/파일 감사이며 모든 스킬의 휴대폰 작동을 확인한 결과는 아닙니다.</p><p><b>찾아주실 자료:</b> 아이콘은 스킬명과 그림이 함께 있는 원작 스킬창. 이펙트는 이름·시전 시작·명중/버프 적용·종료가 보이는 원본 영상/GIF. 서버/연도, 시전자와 대상, 방향, HP·MP 변화가 보이면 좋습니다. 이펙트 없는 기술은 그 사실과 동작을 확인하면 됩니다.</p>']
for title,field,value in [('아이콘 연결이 없는42개','icon','MISSING_BINDING'),('원작 이펙트가 확인되지 않은32개','missingLabelledOriginalFx',True),('그림만으로 해결되지 않는 기능 미지원20개','mechanicUnavailable',True)]:
 parts.append('<h2>'+title+'</h2><table><tr><th>직업</th><th>스킬명</th></tr>')
 for job in dict.fromkeys(r['job'] for r in rows):
  names=[r['name'] for r in rows if r['job']==job and r[field]==value]
  if names:parts.append('<tr><td>'+esc(job)+'</td><td>'+esc(' · '.join(names))+'</td></tr>')
 parts.append('</table>')
parts.append('<h2>자료 보유·품질·개발 작업의 구분</h2><ul>'+''.join('<li>'+esc(s.replace('旧 ','기존 '))+'</li>' for s in report['findings'])+'</ul><h2>221개 개별 상태</h2><table><tr><th>직업/이름</th><th>아이콘</th><th>현재 효과 경로</th><th>기능</th><th>추가 자료</th></tr>')
for r in rows:parts.append('<tr><td>'+esc(r['job']+' / '+r['name'])+'<br><small>'+esc(r['id'])+'</small></td><td>'+('추가 필요' if r['icon']=='MISSING_BINDING' else '연결됨')+'</td><td>'+esc(r['fxRoute'])+'<br><small>'+esc(r['bodyProfile'])+'</small></td><td>'+('미지원' if r['mechanicUnavailable'] else '코드 경로 있음; 원작 수치/실기 검증 별도')+'</td><td>'+esc(' / '.join(r['needsUser']) or '보유 자료 적용 및 품질 확인은 개발 작업')+'</td></tr>')
parts.append('</table><p>기존 GIF 다운로드파일-7.gif(16-7)의 스킬명은 미확인입니다. 메테오 GIF(17-8)는 이미 반영되어 추가 요청 대상에서 제외했습니다.</p></html>')
delivery=ROOT.parent/'skill_audit_v226';delivery.mkdir(exist_ok=True);(delivery/'PROJECT_DARK_SKILL_MATERIALS_V226.html').write_text(''.join(parts))
print(json.dumps(counts,ensure_ascii=False))
