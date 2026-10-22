"""Read-only reward/source reconciliation. Never imports audit rates into runtime."""
from pathlib import Path
import re,json,csv,hashlib,html,subprocess,collections
ROOT=Path(__file__).resolve().parents[1]
OUT=Path('/workspace/scratch/9d5365bbd70c/reward_audit_v224')
JAVA=ROOT/'app/src/main/java/com/projectdark/mobile'
def read(n):return (JAVA/n).read_text()
def strings(s):return re.findall(r'"([^"\n]*)"',s)
def arr(s,n):return strings(re.search(r'\b'+n+r'=\{(.*?)\}',s,re.S)[1])
sources=[]
def source(i,title,url,era,evidence,summary):
 sources.append(dict(id=i,title=title,url=url,era=era,evidence=evidence,summary=summary))
source('S01','2~4써클 보스 센스 관측','https://lod.nexon.com/community/game/3472?SearchBoard=1','2008-01-21','V 커뮤니티','정령308950/광폭자606252/폭군780575는 당시 센스·파티/레벨 조건이 있는 관측치. 정령 반지·연필·금전, 광폭자 실버아쿠아링 관계. 수치 확률 없음.')
source('S02','팜팻의 변화','https://lod.nexon.com/community/game/4060?SearchBoard=1','2009-01-02','V 커뮤니티','브라운팜팻 정수10; 퍼플/실버 정수 각3; 울프/앤트 각5마리는 처치 목표; 정령 뿔 수집. 옛 완료 보상 경험치45만/엑스쿠라눔3. 정수/뿔은 다른 캐릭터로 조달 가능 기록.')
source('S03','킹아크퍼스 이벤트','https://lod.nexon.com/community/game/3452?SearchBoard=1','2008-01-16','V 커뮤니티','퐁퐁이조각/터틀껍질/게껍질 각2를 NPC에 전달해 대지의인장. 가짜 보스 문어다리. 진짜 보스 후 NPC 세줄황금반지. 인장·펜던트를 직접 보스 드랍으로 넣지 않음.')
source('S04','에리얼 이벤트','https://lod.nexon.com/community/game/740?SearchBoard=1','2006-12-04','V 커뮤니티','블루하콘 무기/레드하콘 다리/카카포·포이즌카카포 껍질 각2. 킹아크 완료자는 재료 면제 기록. 보스 완료 파파야방패. 수치 확률 없음.')
source('S05','맨티스 완료 보상','https://lod.nexon.com/community/game/24?SearchBoard=1','2005-08-17','V 커뮤니티','맨티스 처치 후 여자 NPC 대화: 세줄금반지와200만G. 희귀 랜덤 바닥드랍이 아닌 완료 보상.')
source('S06','맨티스 재료','https://lod.nexon.com/board/1493172227/769','옛 공략','V 커뮤니티','트랜트의뿌리8, 앤트자이언트 날개, 은빛늑대 갈기털5의 사용처. 갈기털 교환과 몬스터 직접 보상 구분.')
source('S07','해안 장비 획득 지역','https://lod.nexon.com/board/1493172227/6222','2010-08-27','V 커뮤니티','아쿠아/산호 장비 아벨해안 획득 관계. 뤼케 장비 추천이 곧 특정 몬스터 드랍 증거는 아님. 몬스터별 배분과 확률 미확인.')
source('S08','강력몹 정수 보고','https://lod.nexon.com/Community/free/119111?SearchBoard=1','2025-05-25','V 유저 신고','강력한트렌트/놀/울프라이더의 포테의정수 획득 보고. 처치마다 보장되지 않는다는 보고; 서버 확률 측정 표본 없음.')
source('S09','정수 확률 상향 공식','https://lod.nexon.com/News/update/5089?Page=5','2025-05-29','O 공식','포테 일부 몬스터의 포테의정수 확률 상향. 은빛늑대/강력몹 수 증가. 상향된 정확한 수치와 전체 몬스터별 표는 공지에 없음. 장비 삭제 지원 목록을 드랍표로 사용하지 않음.')
source('S10','팜팻 정수/알 공식','https://lod.nexon.com/News/update/5067?Page=2','2024-01-04','O 공식 검색 본문 확인','팜팻/퍼플/실버 정수 확률 상향, 수치 미공개. 마라메부인의부탁 팜팻의알 등장100%는 별도 1서클 퀘스트이며 현재 포테 몬스터에 전용하지 않음.')
source('S11','죽마 집 사냥 관측','https://lod.nexon.com/board/1493172227/7264','2020-01-26','V 커뮤니티','집/지하 한 번 쓸기 관측에서 엑스쿠라눔1~2와 소량 골드. 몬스터 종류·처치 수·반복 횟수 미기록이므로 개체별 확률 산출 불가.')
source('S12','구광 사냥 보고','https://lod.nexon.com/Community/free/144067?Page=8&SearchBoard=1','2026-03-25','V 유저 관측','구광 사냥 중 헬옷/투구·왕의문장·3갱 퀸홀 경험 보고. 짧은 관측에서 도적/도가 미획득을 드랍0%로 해석하지 않음.')
source('S13','구광 막층 아이템','https://lod.nexon.com/board/1509949442/392159','2024-10-31','V 커뮤니티','전사 헬셋/곡단도/더브릴/왕의문장/남녀 법사 헬옷 획득 보고. 퀸홀 희귀 표현만 있고 정확한 수치 없음.')
source('S14','광산 무기 외형·기원','https://lod.nexon.com/community/game/3484?SearchBoard=3','옛 회고','V 커뮤니티 회고','헬블루나이트-하이브레이질배틀액스, 그림록퀸-더브릴, 헬씨프-곡단도 외형/획득 계통 기록. 외형 유사만으로 모든 옷 드랍처를 확정하지 않음.')
source('S15','드라코 갱도별 드랍','https://lod.nexon.com/Community/game/384714?Category2=1&SearchBoard=2','2021-08-21','V 검색 본문 확인','1갱:쇠조각·드라코발톱;2갱:쇠조각·리베륨;3갱:목도리염료b·드래곤크로어. 같은 몬스터라도 맵조건 필요, 원문 직접열람 실패로 보강 필요.')
source('S16','드래곤크로어 획득처','https://lod.nexon.com/board/1509949442/374717','2019-07-25','V 재인용','3갱 드라코 획득처 안내. 답변 자체가 검색 재인용이어서 단독 확정 근거로 부족.')
source('S17','용의발톱 퀘스트','https://lod.nexon.com/board/1493172227/88','옛 공략','V 커뮤니티','드라코의비늘5개를 상인에게 전달하는 퀘스트. 발톱/비늘/완성무기 용의발톱은 서로 다른 품목, 정확 갱도/확률 미확인.')
source('S18','헬 장비 이름','https://lod.nexon.com/board/1493172227/2131','2006-07-09','V 커뮤니티','법사 남녀/직자 남녀 로브·투구의 별도 이름 확인. 같은 그림을 이유로 이름·직업·성별을 통합하면 안 됨. 드랍처나 확률은 이 자료로 확정 못함.')
source('S19','오묘한보석 획득','https://lod.nexon.com/Community/game/393776?Category2=1&SearchBoard=2','2025-06-20','V 유저 관측','아벨 터틀(1존포함),바크,일에서 오묘한보석 관측. 다른 몬스터 미획득은0% 증거가 아님. 현재 바크/일 정체 매칭 미확정.')
source('S20','포스파늄 획득','https://lod.nexon.com/board/1811939343/119362','2024-04-04','V 커뮤니티','뤼케 대기실 주기 생성 자원. 광물 채집·식물 채집도 별개. 모두 몬스터 랜덤 드랍으로 합치지 않음.')
source('S21','포스파늄 공식 변경','https://lod.nexon.com/news/update/147822','2026-08-27','O 공식','뤼케 대기실 포스파늄 주기와 개수 변경. 옛10~15분/3개를 현행 확정값으로 쓰지 않음.')
source('S22','포테 혼/캅셀 관측','https://lod.nexon.com/Community/game/705206?Category2=1&SearchBoard=4','2017-11-15','V 직접획득 보고','레드팜팻·앤트라이온 혼ㅎ1, 실버팜팻 혼ㅍ1, 강력울프 혼ㅅ2/캅셀2, 정령 혼ㅎ2/캅셀1·3, 트렌트 포장된달인의책+2, 놀 +3. 축약명 완전 식별 전 활성화 금지.')
source('S23','사냥터 경험치 공식 변경','https://lod.nexon.com/news/update/5095','2025-05-22','O 공식','포테/아벨/뤼케 난이도·경험치 변경 및 호러/죽마 경험치 증가. 2008관측값을 현재 기본치로 단순 복사 불가.')
source('S24','호러 드랍 공식 제거','https://lod.nexon.com/news/update/147557','2026-08-20','O 공식','호러 몬스터 드롭 아이템 제거 및 경험치 증가. 현재버전 일반 아이템 드랍은0으로 정의 가능; 옛 영상 복원판에 자동 소급하지 않음. 정제된정수 제조도 변경.')
source('S25','정령 골드/퀘스트 변경','https://lod.nexon.com/News/update/5020?Page=2','2023-11-30','O 공식','정령/강력정령 골드 감소·HP 감소·젠 증가. 메인퀘스트 경험치물약 도입. 옛 완료EXP·골드와 현재값을 섞지 않음.')
source('S26','은빛늑대 갈기털 교환','https://lod.nexon.com/board/1509949442/392568','2025-01-12 표시','V 커뮤니티','갈기털5개로 가죽 교환 기록. 소비여부는 게시물 자체 충돌하므로 조건 미확정.')
source('S27','독거미알의 지역 차이','https://lod.nexon.com/Community/free/87014?SearchBoard=1','옛 공략','V 커뮤니티','독거미알은 노비스 독거미 획득 기록. 죽마 독거미와 같은 이름만으로 공유 드랍 연결 금지.')

monsters=[]
def add(mid,name,region,exp,gold,active='출현',note='모바일 임시 설정; 원작 수치 미검증'):
 monsters.append(dict(id=mid,name=name,region=region,currentExp=exp,currentGold=gold,spawnStatus=active,originalBaseExp=None,originalGold=None,expReview=note,originalExpObservation=None,sourceIds=[],rewardRuleIds=[]))
roster={m:n for m,n in re.findall(r'add\(entries,"([^"]+)","([^"]+)"',read('PoteMonsterRoster.java'))}
for mid,zone,exp,gold in re.findall(r'add\(m,"([^"]+)","([^"]+)",(\d+),(\d+)\)',read('AdaptedCampaignRewardCatalog.java')):
 region='밀레스' if mid=='milles_mouse_proto' else '포테의숲'
 name='여관 생쥐' if region=='밀레스' else roster.get(mid,'캠페인 정예 놀(프로젝트 등록명)')
 active='보상만 등록; 일반 풀 미배치' if 'STRONG' in mid or 'ELITE' in mid else '출현'
 add(mid,name,region,int(exp),int(gold),active)
for i in range(1,4):add(f'combat_dummy_0{i}',f'훈련 몬스터 {"ABC"[i-1]}','밀레스',2500,25,'훈련용','원작대상 아님; 프로젝트 튜토리얼')
names=dict(re.findall(r'case "([^"]+)":return "([^"]+)"',read('AbelCoastMonsters.java')))
for mid,lv,hp,dmg,ac,md,el,spell,exp,gold,cd in re.findall(r'add\("([^"]+)",(\d+),(\d+),(\d+),(-?\d+),(\d+),"([^"]*)","([^"]*)",(\d+),(\d+),([\d.]+)f\)',read('AbelCombatProfile.java')):
 add(mid,names[mid],'아벨해안',int(exp),int(gold))
lyk=read('RucesionCoastMonsters.java');lykids=strings(re.search(r'Arrays.asList\((.*?)\)',lyk)[1]);lyknames=arr(lyk,'NAMES');lykn=dict(zip(lykids,lyknames))
for mid,lv,hp,dmg,el,spell,exp,gold in re.findall(r'add\("([^"]+)",(\d+),(\d+),(\d+),"([^"]*)","([^"]*)",(\d+),(\d+)\)',read('RucesionCombatProfiles.java')):
 add(mid,lykn[mid],'뤼케시온해안',int(exp),int(gold),'시험 보스' if mid=='LYK_ARIEL' else '출현')
h=read('HorrorCastleMonsters.java');hexp=list(map(int,re.search(r'EXP=\{([^}]+)',h)[1].split(',')))
for i,name in enumerate(arr(h,'NAMES')):add(f'HORROR_{i:02d}_*',name,'호러캐슬',hexp[i],0,'출현/4속성 동일보상','원작 개체별 EXP 미검증; 2026 드랍제거 공지 별도')
for i,name in enumerate(arr(read('MineHuntMonsters.java'),'NAMES')):add(f'MINE_{i:02d}',name,'카스마늄광산',1400000+i*120000,800+i*90)
for i,name in enumerate(arr(read('DeathVillageMonsters.java'),'NAMES')):add(f'DEATH_0{i}',name,'죽음의마을',300000 if i==0 else 750000,450+i*180,'보상만 등록; 미배치' if i==0 else '출현','사용자 영상근사75만/요청값; 원작 순수 기본EXP 확정 아님' if i else '미출현 등록값')
byid={m['id']:m for m in monsters}
assert len(monsters)==68 and len(byid)==68,(len(monsters),len(byid))
for mid,exp in [('POTE_SPIRIT',308950),('ABEL_BERSERKER',606252),('LYK_TYRANT',780575)]:
 byid[mid]['originalExpObservation']=exp;byid[mid]['sourceIds']=['S01','S23','S25'];byid[mid]['expReview']='현재값과2008 센스관측 불일치. 파티/레벨/가호·시대 조건 미확인, 관측값을 기본EXP로 덮어쓰기 금지'

rules=[]
def rule(target,item,kind,src,condition='',rate=None,rate_status='원작 수치 미공개/미확인',quantity=None,current='미지급',note='',era='자료시대 기준; 현행 검증 별도'):
 rid=f'R{len(rules)+1:03d}'
 row=dict(id=rid,targets=target if isinstance(target,list) else [target],item=item,kind=kind,sourceIds=src.split(','),condition=condition,originalProbability=rate,probabilityStatus=rate_status,quantity=quantity,current=current,review=note,era=era,proposedProbability=None)
 rules.append(row)
 for mid in row['targets']:
  if mid in byid:byid[mid]['rewardRuleIds'].append(rid)
 return rid
rule('OUT_OF_SCOPE_NOVICE_BROWN_PAMPAT','팜팻의정수','퀘스트 재료','S02,S10','팜팻의변화 단계; 수집10개는 드랍수량/확률이 아님',note='레드/그린팜팻에 매핑 금지')
rule('POTE_PURPLE','퍼플팜팻의정수','퀘스트 재료','S02,S10','3개 수집; 타캐릭 조달 가능 기록; 선행 필요여부 별도 검증')
rule('POTE_SILVER','실버팜팻의정수','퀘스트 재료','S02,S10','3개 수집; 2024확률 상향 수치 미공개')
rule('POTE_TREANT','트랜트의뿌리','퀘스트/잡템','S06','8개 이야기 재료; 1회처치당 수량 미확인')
rule('POTE_ANTGIANT','앤트자이언트의날개','퀘스트 재료','S06','맨티스 함정 관련; 아이템명 앤트/엔트 별칭 정규화 필요')
rule('POTE_SILVERWOLF','은빛늑대의갈기털','퀘스트/교환 재료','S06,S26','5개→가죽 교환. 필요갯수는 획득률 아님')
rule('POTE_SPIRIT','포테정령의뿔','퀘스트 재료','S02','뿔 제출단계; 다른 캐릭터 조달 가능 기록')
for item in ['세줄금반지','강력한연필','금전더미']:rule('POTE_SPIRIT',item,'장비' if item!='금전더미' else '골드 획득','S01,S25','관측자료상 가끔/금전. 금전더미의 존재가 고정82G 증거 아님')
rule('ABEL_BERSERKER','실버아쿠아링','장비','S01','2008개체 보고; 확률/수량 미공개')
rule('POTE_MANTIS','세줄금반지','완료/NPC 보상','S05,S06','맨티스 처치 후 보상방 여자NPC 완료',1,'공략상 조건부 확정지급(정책 표현; 서버확률 실측 아님)',1,current='D03 완료로 반지1개 지급',note='랜덤드랍으로 전환 금지')
rule('POTE_MANTIS','금전 2,000,000G','완료/NPC 보상','S05','동일 완료거래',1,'공략상 조건부 확정지급',2000000,current='D03는10,000G',note='2005보상과 프로젝트조정200배 차이; 최신 적용값은 미확정')
rule('POTE_MANTIS','Lv41 도달','프로젝트 완료 보상','S05','D03 완료',None,'원작 근거 없음',current='현재 자동 레벨 이정표',note='원작 보스 보상으로 표시 금지')
rule(['POTE_STRONG_GNOLL','POTE_STRONG_WOLFRIDER','POTE_STRONG_TREANT'],'포테의정수','퀘스트 재료','S08,S09','강력3종 유저보고/공식 지역확률상향; 현재 강력3종 미배치',note='현재 획득불가. 일반몹전체에 일괄 연결 금지')
for mid,item in [('POTE_TREANT','포장된 달인의책+2'),('POTE_GNOLL','포장된 달인의책+3')]:rule(mid,item,'성장 소비/잡템','S22','2017 직접획득 보고',note='후대 패치 아이템; 목표시대 매칭 필요')
for target,item in [(['POTE_RED','POTE_ANTLION'],'혼 ㅎ1'),('POTE_SILVER','혼 ㅍ1'),('POTE_PURPLE','캅셀3'),('POTE_STRONG_WOLFRIDER','혼 ㅅ2 / 캅셀2'),('POTE_SPIRIT','혼 ㅎ2 / 캅셀1 / 캅셀3')]:rule(target,item,'축약명 미해결 관측','S22','2017기록 원문 축약 유지',note='정식 itemId/등급/속성 식별 전 드랍 활성화 금지')
for mid,item in [('ABEL_TURTLE','터틀껍질'),('ABEL_PONG','퐁퐁이조각'),('OUT_OF_SCOPE_ABEL_CRAB','게껍질')]:rule(mid,item,'퀘스트 재료','S03','각2개 수집→대지의인장; 개별몬스터 귀속은 이름/기존마스터와 교차, 수량 미확인')
rule('ABEL_TURTLE','오묘한보석','퀘스트 재료','S19','2025터틀1존 포함 획득 관측; 다른 몬스터0%로 일반화 금지')
for item in ['아쿠아글러브','산호각반']:rule('REGION_ABEL_UNASSIGNED',item,'장비','S07','아벨해안 지역 획득; 몬스터별 미배분',note='모든 일반몹 동일확률 적용 근거 없음')
rule('REGION_ABEL_UNASSIGNED','실버아쿠아링','장비','S01,S07','광폭자와의 확인 관계 외 일반몹 전원에게 분배 금지')
rule('ABEL_KINGARK_FAKE','문어다리','이벤트 진행 재료','S03','가짜 보스 후 파티원이 각각 조달',None,'공략상 드랍 관계; 개인100% 수량은 미확인',note='한마리당5개/개인별100% 추정 금지')
rule('ABEL_KINGARK','세줄황금반지','완료/NPC 보상','S03','진짜보스 처치 후 소녀NPC 완료',1,'공략상 조건부 확정지급',1,note='세줄금반지와 별도 원작명; 같은 옵션/그림을 이유로 동일 ID 단정 금지')
for mid,item in [('LYK_BLUE_HACON','블루하콘의무기'),('LYK_RED_HACON','레드하콘의다리'),('LYK_KAKAPO','카카포껍질'),('LYK_POISON_KAKAPO','포이즌카카포껍질')]:rule(mid,item,'퀘스트 재료','S04','각2개→에리얼 진행; 킹아크 완료자 재료면제')
rule('LYK_ARIEL','파파야방패','보스/완료 보상','S04','이벤트 보스 완료',1,'공략상 조건부 확정지급',1,current='현재 시험보스0EXP/0G; 방패 미지급',note='일반무한젠 랜덤 드랍으로 전환 금지; 반복/파티원 조건 보강')
for item in ['아쿠아레더글러브','아쿠아각반','골드아쿠아링']:rule('REGION_RUCESION_UNASSIGNED',item,'장비 후보','S07','기존마스터 지역 관계/장비가이드 추천, 개체별원문 부족',note='추천/착용레벨은 드랍처 증거 아님; 몬스터와 확률 보류')
rule('RUCESION_LOBBY_RESOURCE','포스파늄','맵 주기 생성','S20,S21','대기실 자원 생성;2026주기/개수 변경',None,'처치확률 적용대상 아님',note='몬스터 드랍에 넣지 않음; 현행 생성량/주기 미공개')
rule('POTE_ROUTE_NPC','투르크의팬던트','조건부 위치 획득','S05','투르크의사면 완료 후 지정위치',1,'공략상 조건부 위치획득',1,note='맨티스 사망 드랍 아님')
rule('ABEL_ROUTE_NPC','대지의인장','NPC 교환','S03','터틀껍질/퐁퐁이조각/게껍질 각2 제출',1,'공략상 조건부 교환지급',1,note='킹아크 드랍 아님')
rule('ABEL_ROUTE_NPC','해저의펜던트','NPC 진행 보상','S03','포스파늄→붉은진주→브라토 교환',1,'공략상 조건부 교환지급',1,note='아크퍼스 랜덤 드랍 아님')
rule('POTE_ROUTE_NPC','은빛늑대의가죽','NPC 교환','S06,S26','갈기털5개 제출',1,'공략상 조건부 교환지급',1,note='소모성/반복성은 자료충돌 보류')
for mid,item,cur in [('MINE_05','곡단도','2%/1개'),('MINE_06','하이브레이질배틀액스','2%/1개'),('MINE_03','더브릴','미지급')]:rule(mid,item,'무기','S13,S14','구광층/3갱/시대 구분 필요',current=cur,note='외형·역사자료로 관계 보강; 개체/층 서버테이블·숫자확률 미확인')
rule('REGION_MINE_UNASSIGNED','그림록퀸홀','투구','S12,S13','구광/3갱 획득지역보고; 정확 개체 매핑 미확인',current='미지급',note='이름에 그림록퀸이 있어도 그 몬스터 전용드랍으로 즉시 확정하지 않음')
rule('REGION_MINE_UNASSIGNED','왕의문장','재료/잡템','S12,S13','구광 획득 기록; 현재 사용처·개별 몬스터 미확인')
for mid,item,cond in [('MINE_04','쇠조각','1갱/2갱'),('MINE_04','드라코발톱','1갱'),('MINE_04','리베륨','2갱'),('MINE_04','목도리염료b','3갱'),('MINE_04','드래곤크로어','3갱')]:rule(mid,item,'재료/소비' if item!='드래곤크로어' else '무기','S15,S16',cond,note='현재 광산 단일 맵에 모든 갱도의 드랍을 혼합하지 않음; 직접원문 실패/재인용 근거 보강')
rule('MINE_04','드라코의비늘','퀘스트 재료','S17','5개 NPC 제출;정확 갱도 미확인')
for mid,item in [('MINE_05','헬씨프아머'),('MINE_06','헬나이트아머'),('MINE_08','헬몽크아머')]:rule(mid,item,'방어구','S12,S18','현재 외형기반 매핑; 원작 개체·갱도표 추가확인',current='3.5%/1개',note='기존3.5%는프로젝트임시.원작확률확정금지')
for mid in ['MINE_07','MINE_09']:
 for item in ['헬소서러로브','헬프리스트로브']:rule(mid,item,'방어구','S13,S18','현재 두 몬스터가 법사남/직자여 로브 공통지급',current='각각3.5%/1개',note='개체-직업매칭 미검증; 남녀법사·남녀직자 별도종류 누락 감사 필요')
for item in ['헬나이트투구','헬씨프투구','헬소서러후드','헬소서리스로브','매직베일','헬클레릭로브','헬클레릭후드','홀리베일']:rule('REGION_MINE_UNASSIGNED',item,'장비/누락후보','S12,S13,S18','존재/지역·계열 관계와 실제 개체드랍은 별도',note='itemId는 성별·직업·옵션으로 구별; 그림중복제거 금지')
rule('REGION_DEATH_HOUSE_UNDER_UNASSIGNED','엑스쿠라눔','소비/판매 재화','S11','한번쓸기1~2개 관측; 몬스터수N/총반복수 미기록',note='1~2개를 각개체100% 또는 일정%로 변환 금지')
rule('REGION_DEATH_HOUSE_UNDER_UNASSIGNED','소량 골드','골드 획득','S11','지역관측이며 현재630/810/990G의 직접근거 아님',note='현재고정금화값은U/B/ADAPTED 유지')
rule('OUT_OF_SCOPE_NOVICE_SPIDER','독거미알','퀘스트 재료','S27','노비스 독거미 기록',note='죽음의마을 독거미에 재사용 금지')
for mid in [m['id'] for m in monsters if m['region']=='호러캐슬']:
 rule(mid,'일반 드롭 아이템 전체','드랍 제거','S24','2026-08-20 정식판 기준',0,'공식 드랍제거 →0%',0,current='아이템 미지급',note='최신판일 때 일치.옛판기준은별도미확정;골드까지0이라고추정금지',era='2026-08-20 이후 공식')
for i in range(1,4):rule(f'combat_dummy_0{i}','훈련 증표','프로젝트 시험 보상','PROJECT','튜토리얼',None,'원작대상 아님',1,current='100%/1개',note='원작 드랍표에서 격리')

findings=[
 dict(id='F13',severity='높음',title='퀘스트 재료 수집과 실제 인벤토리의 연결 부족',detail='현재 캠페인 처치 처리는 KILL/PAIR 카운트를 올리고, C06/M15 완료 시 시험용 정제된 정수를 3개 지급한다. 실제 원작 정수 드랍→인벤 수집→NPC 제출 과정이 아니다. 원작 재료 획득률을 정의해도 이 경로를 연결하지 않으면 퀘스트 재료가 사냥 보상으로 작동하지 않는다.'),
 dict(id='F01',severity='높음',title='보상표를 원작 설계로 승인할 수 없음',detail='현재 68종 중 대부분의 EXP/Gold가 모바일 임시 프로필이다. 원작 확인 상태와 실제 사용값을 별도 저장해야 한다.'),
 dict(id='F02',severity='높음',title='관측 경험치와 기본 경험치 혼동',detail='2008년 센스 글은 레벨·파티원 수에 따른 경험치 변동을 명시한다. 현재 정령 40,000 / 광폭자 95,000 / 폭군 1,280,575를 당시 관측 308,950 / 606,252 / 780,575로 곧바로 교체하는 것도 오류다. 버전·솔로/파티·가호 조건 확인이 필요하다.'),
 dict(id='F03',severity='높음',title='광산 획득률은 원작 근거 없는 임시값',detail='방어구 350/10,000=3.5%, 무기 200/10,000=2%다. 기대 처치 약 28.57/50회는 임시 수치의 수학적 의미이며 원작 사냥 속도의 증거가 아니다. 두 로브의 동시 판정과 공유 드랍은 개체·갱도 근거가 부족하다.'),
 dict(id='F04',severity='높음',title='재료·잡템·장비·퀘스트 완료 보상을 한 테이블로 합치면 안 됨',detail='정수·뿔·뿌리·날개·갈기털·오묘한보석·쇠조각·왕의문장·비늘 등이 누락되어 있다. 맨티스 반지와 NPC 200만G, 킹아크 반지, 파파야방패는 완료 조건을 별도로 관리한다.'),
 dict(id='F05',severity='높음',title='원작 시대가 혼재됨',detail='2005년 완료 보상, 2008년 센스, 2017년 혼·캅셀, 2023년 골드, 2024년 정수, 2025년 경험치, 2026년 호러 드랍 제거를 한 버전의 수치로 합치지 않는다. 기존 영상의 복원 범위와 최신 공식 자료를 따로 대조한다.'),
 dict(id='F06',severity='높음',title='같은 이름/그림이어도 드랍표는 지역·개체별',detail='노비스 독거미알을 죽마 독거미에 연결하지 않는다. 드라코에는 갱도별 드랍 차이 기록이 있다. 헬 로브는 남녀·직업별 항목을 같은 그림이라는 이유로 합치지 않는다.'),
 dict(id='F07',severity='중간',title='현재 출현하지 않는 보상 프로필',detail='강력한놀·강력한울프라이더·강력한트렌트·캠페인정예놀·좀비는 보상만 등록되어 있거나 일반 출현 풀에 없다. 보상 등록만으로 획득 가능하다고 보고하지 않는다.'),
 dict(id='F08',severity='높음',title='사냥 보상 일부 실패 처리',detail='자동 지급이 한도 초과로 실패하면 사냥 보상은 실패 품목을 이력에만 남기고 EXP/Gold는 지급한다. 보관함·재수령이 없어 당첨품이 누락될 수 있다. 현재 검사 대상은 전체 슬롯 수가 아니라 종류별 스택 한도다.'),
 dict(id='F09',severity='중간',title='획득량/희귀성 수식 검증 범위',detail='현재 추첨은 사망 순번과 개체 ID의 해시를 사용한다. 10,000구간과 비교한 값은 설정률이며 원작 실측률이 아니다. 결과 고정과 재접속 재추첨 방지를 지급·저장과 함께 검증해야 한다.'),
 dict(id='F10',severity='중간',title='처치목표와 아이템 수집을 구분',detail='울프라이더 5마리·앤트자이언트 5마리는 처치 목표이며 드랍 5개가 아니다. 수집 필요 개수는 1회당 수량이 아니다. 포스파늄·팬던트는 맵 생성·위치 획득을 분리한다.'),
 dict(id='F11',severity='중간',title='게임 경제와 성장 경로 평가 유보',detail='현재 HP·스킬·출현 수도 조정되어 있어 EXP/HP의 단순 비교로 원작 정상성을 판정할 수 없다. 획득·판매 골드, 물약 소모, 시간당 처치 수, 성장 경험치와 가호 조건을 함께 검증해야 한다.'),
 dict(id='F12',severity='중간',title='기존 마스터의 서술도 확률 근거 아님',detail='기존 Monster_Drop의 높음·낮음은 프로젝트 밸런스 표시이며 원작 수치 공개가 아니다. 은빛늑대 일반 잡템은 이름도 없다. 과거 바닥 소유방식은 폐기됐으며 승인된 인벤 자동 지급이 우선한다.'),
]
policy={
 'status':'AUDIT_COMPLETE_WITH_UNRESOLVED_ORIGINAL_FACTS; NOT_RUNTIME_CONFIG',
 'scope':'현재68종/보상프로필(호러40속성조합을10외형으로집계), 출현여부포함. 자료만있는미구현몬스터는별도보류.',
 'versionPolicy':'사용자영상/기존복원시대와현행공식분리.최신공지자동소급없음.확정목표시대는원본영상메타/원작버전으로추가바인딩.',
 'rateDefinition':'1회유효처치·한아이템·명시된조건에서추첨되는확률.완료/NPC/위치/채집은처치확률과별도분모.',
 'probabilityUnit':'0..1; null=원작미공개/미확인/적용대상아님, 절대로0%로해석금지',
 'allowedNumericRates':'공식명시수치,조건부확정지급의명시적정책표현,실제k/n관측+표본조건만허용.이번원작일반드랍중새로확정한0<p<1수치없음.',
 'unknownHandling':'확률임의생성금지.원작드랍가능품목의미확인수치로0%판정금지.현재기능정지나기존임시률덮어쓰기는이감사에포함하지않음.',
 'rollTiming':'조건판정→조건별드랍풀→그룹/개인분모확정→1회사망추첨→결과고정→인벤/미수령보관→영속화.수령실패/재접속재추첨금지.',
 'questRules':'선행수락/단계/소지상한/완료후드랍/거래/파티공유는아이템별근거필요.일괄퀘스트중전용또는완료후삭제규칙추정금지.',
 'quantities':'수집필요개수와1회드랍수량분리.미확인수량은null.중복장비직업·성별·옵션유지.',
 'experienceRules':'기본EXP·관측EXP·가호·레벨차·룹배율·분배·시대분리.원작공식이없는배율을새로만들지않음.현재Lv99누적상한규칙은보존.',
 'goldRules':'고정금화/랜덤금전더미/아이템판매가/NPC완료금화를분리.상점매입가격은원작버전근거없으면미확정.',
 'autoLoot':'기존승인대로인벤자동지급;원작바닥줍기복원안함.저장중복방지·미수령처리는보상계약으로검증.',
 'rateMeasurement':'관측률은 조건에 맞는 처치 N회 중 해당 품목을 한 번 이상 얻은 처치 K회의 K/N. 여러개 드랍의 총개수/N(기대수량)와 구분한다. 버전·지역·퀘스트단계·파티·가호·관측기간·표본수를 보존하고 95% 신뢰구간을 붙인다. 0회 획득은 서버확률0% 증거가 아니다. 이번 자료에는 필요한 분모가 없어 수치추정을 하지 않았다.',
 'outOfScope':'새몬스터/던전/승급길/퀵던전추가없음.이번은설계감사와정의이며런타임수치변경·APK생성없음.'
}
exclusions=[dict(id='ABEL_CRAB',reason='마스터의게/게껍질관계는있지만현재그래브와동일몬스터라고확정못함'),dict(id='DEATH_UNDER_MUMMY',reason='죽던자료,현재요청범위던전제외'),dict(id='DEATH_UNDER_GENERIC',reason='그룹범주라개별종드랍표로사용불가'),dict(id='NOVICE_BROWN_PAMPAT',reason='현재포테RED/GREEN과별개;정수/알범위확인용'),dict(id='MINE_ORE_AND_WOODLAND_HERBS',reason='노다지광물/식물채집은몬스터드랍아님'),dict(id='PROMOTION_AND_QUICK_DUNGEON',reason='이동석/문열쇠/본드라곤재물/퀵던전드랍을현맵으로유입하지않음')]
for m in monsters:
 if m['region']=='호러캐슬':m['sourceIds']=['S23','S24']
 if m['region']=='죽음의마을':m['sourceIds']=['S11','S23'];m['expReview']+='; 지역엑쿠보고는개별종확정아님'
 if not m['rewardRuleIds']:m['itemReview']='개별드랍원문미확인.아이템없음이나0%로결론내리지않음'
 else:m['itemReview']='연결된획득규칙의근거·시대·미공개확률을각각확인'
 m['regionalRuleIds']=[r['id'] for r in rules if any(t.startswith('REGION_') and ((m['region']=='아벨해안' and 'ABEL' in t) or (m['region']=='뤼케시온해안' and 'RUCESION' in t) or(m['region']=='카스마늄광산' and 'MINE' in t) or(m['region']=='죽음의마을' and 'DEATH' in t)) for t in r['targets'])]
scope_files=['AdaptedCampaignRewardCatalog.java','AdaptedPrototypeRewardCatalog.java','CanonicalMonsterRewardCatalog.java','CampaignMonsters.java','PoteMonsterRoster.java','AbelCombatProfile.java','AbelCoastMonsters.java','RucesionCombatProfiles.java','RucesionCoastMonsters.java','HorrorCastleMonsters.java','MineHuntMonsters.java','DeathVillageMonsters.java','RpgProgressionState.java','AdaptedPoteQuestRewards.java','CampaignProgress.java']
data=dict(revision='V224_REWARD_ORIGINAL_AUDIT',asOfKST='2026-10-22',sourceCommit='111df324af03aa4111942a417983dbaed9b6ed42',runtimeSource='f79f354459007d6fe2cc214319db7dba278ebbd7',policy=policy,findings=findings,monsters=monsters,rewardRules=rules,sources=sources,excluded=exclusions,codeHashes={n:hashlib.sha256((JAVA/n).read_bytes()).hexdigest() for n in scope_files})
source_ids={s['id'] for s in sources}|{'PROJECT'}
assert all(set(r['sourceIds'])<=source_ids for r in rules)
assert all(r['originalProbability'] is None or 0<=r['originalProbability']<=1 for r in rules)
assert all(r['proposedProbability'] is None for r in rules)
assert all(r['originalProbability'] is None for r in rules if r['kind'] in ['장비','무기','방어구','장비 후보','장비/누락후보'])
counts=dict(collections.Counter(m['region'] for m in monsters));data['coverage']=dict(monsterProfiles=len(monsters),byRegion=counts,rewardRelations=len(rules),sources=len(sources),findings=len(findings),unresolvedFractionalOriginalRates=True,allRuntimeRewardProfilesIncluded=True)
OUT.mkdir(exist_ok=True)
for dest in [OUT/'PROJECT_DARK_REWARD_AUDIT_V224.json',ROOT/'docs/research/REWARD_ORIGINAL_V224.json']:
 dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
esc=lambda s:html.escape(str(s))
fmt=lambda v:'미확인' if v is None else f'{v:,}'
def table(headers,rows):return '<div class="scroll"><table><thead><tr>'+''.join('<th>'+esc(h)+'</th>' for h in headers)+'</tr></thead><tbody>'+''.join('<tr>'+''.join('<td>'+str(c)+'</td>' for c in row)+'</tr>' for row in rows)+'</tbody></table></div>'
def refs(ids):return ' '.join('<a href="#'+i+'">'+i+'</a>' for i in ids if i!='PROJECT') or '프로젝트 코드'
parts=['''<!doctype html><html lang="ko"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>PROJECT DARK 사냥 보상 원작 검토 V224</title><style>body{font:15px/1.65 system-ui,sans-serif;background:#f4f5f8;color:#182234;margin:0}main{max-width:1500px;margin:auto;padding:30px}h1{font-size:30px}h2{margin-top:42px;color:#183b59}p{max-width:1050px}.lead{background:#16364c;color:white;padding:25px;border-radius:14px}.grid{display:flex;gap:12px;flex-wrap:wrap}.card{background:white;border-radius:10px;padding:14px 22px;box-shadow:0 1px 5px #ddd}.card b{font-size:26px;display:block}.scroll{overflow:auto;background:white;border-radius:10px;margin:15px 0}table{border-collapse:collapse;width:100%;font-size:13px}th{background:#203e56;color:white;position:sticky;top:0;text-align:left}td,th{padding:10px 12px;border-bottom:1px solid #dce2e9;vertical-align:top;min-width:85px}td:nth-child(2){min-width:160px}a{color:#176691}input{padding:12px;width:min(450px,90%);border:1px solid #9eafbf;border-radius:8px}details{background:white;padding:12px 18px;margin:10px 0;border-radius:8px}summary{font-weight:bold;cursor:pointer}.note{background:#fff0cf;padding:16px;border-radius:9px}.muted{color:#586878}nav a{margin-right:15px}@media print{body{background:white}main{padding:0}th{position:static}.scroll{overflow:visible}input{display:none}}</style><main>''']
parts+=['<div class="lead"><h1>사냥 보상, 원작 기준으로 다시 점검</h1><p>V224 · 2026-10-22 · 전체 몬스터 프로필 전수 대조 / 런타임 변경 없음</p><p>판정: 현재 설계는 원작 보상표로 승인할 수 없습니다. 확인한 보상 관계와 공개되지 않은 확률을 구분했으며, 모르는 수치를 임의로 채우지 않았습니다.</p></div>']
parts+=['<div class="grid">'+''.join(f'<div class="card"><b>{v}</b>{esc(k)}</div>' for k,v in [('몬스터 프로필',68),('획득 관계',len(rules)),('근거 자료',len(sources)),('개선 항목',len(findings))])+'</div>']
parts+=['<nav><a href="#findings">검토 결과</a><a href="#monsters">전체 몬스터</a><a href="#rules">아이템·획득률</a><a href="#policy">정의</a><a href="#sources">출처</a></nav><p class="note">null / 미확인은 0%가 아닙니다. 확률을 공개하지 않은 원작 일반드랍을 이번 조사에서 수치로 확정하지 못했습니다. 100% 표시는 공략상 조건부 완료·교환을 표현한 정책이며 개인별 처치 드랍률 실측이 아닙니다. 호러 0%는 2026-08-20 이후 공식판에만 해당합니다.</p>']
parts+=['<h2 id="findings">검토 결과</h2>']
for f in findings:parts+=[f'<details open><summary>{esc(f["id"])} · {esc(f["severity"])} · {esc(f["title"])}</summary><p>{esc(f["detail"])}</p></details>']
parts+=['<h2 id="monsters">전체 몬스터: 현재 수치와 원작 근거</h2><p>호러 4속성은 같은 보상을 쓰므로 10종으로 묶었습니다. 출현하지 않는 등록값, 시험용 개체도 누락 없이 표시했습니다. 지역 단위 획득 기록은 해당 지역 전체 몬스터의 공통 드랍으로 확정하지 않았습니다.</p><input id="filter" placeholder="몬스터·지역·아이템 검색">']
mr=[]
for m in monsters:
 rel=m['rewardRuleIds'];items=' / '.join(next(r['item'] for r in rules if r['id']==rid) for rid in rel) or '개체별 미확인'
 mr.append([esc(m['region']),esc(m['name'])+'<br><span class="muted">'+esc(m['id'])+'</span>',esc(m['spawnStatus']),fmt(m['currentExp']),fmt(m['currentGold']),fmt(m['originalExpObservation']),esc(m['expReview']),esc(items),refs(m['sourceIds'])])
parts+=[table(['지역','몬스터/ID','출현 상태','현EXP','현Gold','과거 관측EXP','검토','아이템/관계','근거'],mr)]
parts+=['<h2 id="rules">아이템·재료·잡템과 획득률 정의</h2><p>장비만 나열하지 않고 퀘스트 재료·소비품·금전·완료 보상·맵 자원·교환을 포함했습니다. 개체가 미확정인 지역 획득품은 REGION 행으로 분리했습니다. 임시획득률을 원작수치로 올리지 않았습니다.</p>']
rr=[]
for r in rules:
 rate='적용대상 아님 / 미확인' if r['originalProbability'] is None else f'{r["originalProbability"]*100:g}%'
 target=' / '.join(byid[t]['name'] if t in byid else t for t in r['targets'])
 rr.append([esc(r['id']),esc(target),esc(r['item']),esc(r['kind']),rate+'<br>'+esc(r['probabilityStatus']),fmt(r['quantity']),esc(r['condition']),esc(r['current']),esc(r['review']),refs(r['sourceIds'])])
parts+=[table(['규칙','대상','품목','유형','원작 획득률/상태','1회 수량','조건','현재 구현','검토','근거'],rr)]
parts+=['<h2 id="policy">보상 처리 정의</h2>']
parts+=[table(['항목','정의'],[(esc(k),esc(v)) for k,v in policy.items()])]
parts+=['<h2>범위 밖 자료와 추가 확인</h2>',table(['항목','제외·보류 이유'],[(esc(x['id']),esc(x['reason'])) for x in exclusions])]
parts+=['<h2 id="sources">출처와 시대</h2><p>O는 공식 공지, V는 넥슨 사이트에 실린 유저 글·관측입니다. 넥슨에 게시되어 있다고 유저 글이 공식 서버 확률표가 되는 것은 아닙니다. 검색본문만 확인된 자료와 재인용도 낮은 근거로 표시했습니다. 아래는 짧은 요약이며 원문은 링크에서 확인합니다.</p>']
for s in sources:parts+=[f'<article id="{s["id"]}"><b>{esc(s["id"])} · <a href="{esc(s["url"])}">{esc(s["title"])}</a></b><p>{esc(s["era"])} · {esc(s["evidence"])}<br>{esc(s["summary"])}</p></article>']
parts+=['<h2>프로젝트 기준</h2><p>소스 '+esc(data['sourceCommit'])+' / 실제 런타임 '+esc(data['runtimeSource'])+'<br>첨부 나무위키 PDF는 게임 일반 설명 중심이고 아이템 링크 PDF는 카페 아이템별 링크 모음입니다. 이를 서버 드랍 확률표로 취급하지 않았습니다. 기존 마스터 CSV 및 현재 보상·사망처리·퀘스트 코드와 대조했습니다.</p><script>document.getElementById("filter").addEventListener("input",e=>{let q=e.target.value.toLowerCase();document.querySelectorAll("tbody tr").forEach(r=>r.hidden=q&&!r.textContent.toLowerCase().includes(q))});</script></main></html>']
(OUT/'PROJECT_DARK_REWARD_AUDIT_V224.html').write_text('\n'.join(parts))
print(json.dumps(data['coverage'],ensure_ascii=False));print('All roster IDs unique; all source joins valid; no invented fractional rates; HTML/JSON generated')
