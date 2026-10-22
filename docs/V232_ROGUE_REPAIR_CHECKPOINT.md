## 2026-10-22 V232 도적 수정 후보 — 빌드·관련 실행 검증 완료

APK 소스 `2d8aed12f3432cba1566ad537681165106b40f66`. 최종 run39763441450/job119772990677 SUCCESS, 공통 범위 run39763449606 SUCCESS. 첫 run39761907202의 54 PASS와 한정 후속 4 PASS를 exact-delta guard로 연결하여 관련 고유 56건 PASS. 첫 실패 중 도적 기본 THRUST 연결 및 retired031 테스트 호출을 수정하여 후속 통과. 전체 회귀 통과 아님: 기존 Abel 5직업 Lv70 장비 여정 1건은 gear(s,41)의 alias/요구조건 충돌로 미해결이며 별도 통합 전 해결 필요. 원본 테스트/장비 조건을 완화하지 않음.

도적 c8/c13 한쪽 무릎 접촉 자세 + 원본 IDLE 준비/회복을 두 BODY·4방향 native로 직접 확인. 함정찾기1→2 별칭, Slash만 기존 갈색 적용, 설치형트랩031 목록/습득/시전/저장 슬롯에서 제거. 하이드 전체 배우80/255alpha 및 진행/신규 물리·마법 공격 차단. 센스몬스터 원본 갈색 프레임/Close/스크롤 사용, 바라보는 전방1~2칸만 허용, 자동 이동/회전 없음, AUTO0.3초 닫힘. 무도가 에셋16개 및 요청 외 도적 아이콘24개는 V231 APK와 동일. 정상 입력 native 캡처, 결정적 생성, 범위5검사, 동시성 감사, 컴파일/조립 PASS.

PROJECT_DARK_V232_TEST.apk / com.projectdark.mobile.v232test / PROJECT DARK V232 TEST. 빌드2026-10-22 19:32:14 KST,102028166bytes,SHA256 `5ff7bba1cb6afc3790076c4eb81aef40d195be4539a0291ff93ba77ae1eb121d`. artifact12418877755/ZIPsha `260bcdd3fa5aad750ae420105bd35880ab212b87c2fd82815ece48e56f2b076d`. ZIP/APK CRC·hash·내장 소스/run·binary package/label·변경 asset 일치 확인. CI V2 signature PASS, 인증서SHA256118503aa3a09e65c7e1f1c52911c36e404bbada1a1cd1b2acf91f83d966ce615. 전달 보관ID libfile_d764ca7ad6248191ae4cf44a08527557. 상세 verification/ROGUE_REPAIR_V232_BUILD.json.

IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_NATIVE VERIFIED. 무도가 USER_VISUAL_ACCEPTED 유지. 도적 PHONE / USER_VISUAL PENDING. 별도 설치 후보이며 이전 앱/저장은 유지, 자동 저장 이동 없음. PR197 unmerged/main41fe6def 유지. NEXT: 이 APK의 도적 휴대폰 피드백; 동일 소스 재빌드 금지. 이 마감은 문서만 변경하며 APK소스는 위 SHA 고정.

## V232 원본 자세 준비/회복 직접 검토

첫 native4phase strip을원문GIFidle/contact/contact/idle과비교:contact c8/c13은맞지만준비/회복c4overhead는원문과다름. 명시profile idleEdges만추가,준비<.15/회복>=.70은기존원본IDLEsource재사용. 도적만해당하며다른profile/png/frame/skill수치불변. 이시각변경은RogueRepairpose+RogueReviewbothBODY4facing/14normalinput추가2case로검증. Tail exactdelta guard expands only this flag/frameKey optional lookup and Rogue basicpredicate.56distinct relevantcasesexpected;oldAbelgearfixturefailureseparate.

## V232 첫 네이티브 결과와 한정 후속 검증

Source4e9ca525/run39761907202:57cases54PASS/3FAIL. Source pose key/native strip, Sense directional1..2/livewall+visibility/originalframe/auto.3/manual, Hide normal80alpha/queued+newphysicalmagic+taunt/expiry,trap1alias+031save retirement all passed except sourcepose case's final Rogue basicTHRUST assertion. Runtime missing one early THRUST→ROGUE_STAB predicate (learned skill routes alreadycorrect);add predicate only. allSupportedStatuses test wrongly dispatches retired031;filter supported status IDs,keeping original unsupported catalog/ID for migration. Rerun those2cases and unexecuted APK/concurrency under exact-main-delta guard;reuse54PASS after XML verification.

Third failure AbelCombatV153 five-job Lv70journey at gear(s,41),before anycombat:WARRIOR/MARTIAL aliases equip toggles UNEQUIPPED,ROGUE/MAGE/CLERICREQUIREMENT_PENDING. Test/RpgProgressionState/TownCommerce anditemassets unchanged from3c2abf11;this is the retained legacy fixture's conflict with canonical item requirements/aliases,not caused by rogue motion/sense/hide. Do not bypasssource requirements,changeequipment balance,orclaimfull regression. Leave this unrelated broaderjourney FAILED/blocked for separate work/finalintegration;the otherAbelcombatcasesPASS. Applicable V232nativeexpected56distinctPASS aftertail;phonepending.

## 2026-10-22 V232 도적 실기기 정정 — 구현·검증 대기

USER_VISUAL_ACCEPTED: V231 무도가 모든 스킬 통과. 해당 BODY/FX/icon/숫자 파일 그대로 유지. DEVICE_FAILED_USER_REPORTED: 도적 e8..15는 활 모션;기존 c14..23은 양발 벌림. 첨부1000060674.mp4(3.294s)와 원문245450 보관GIF21의4frames를 직접 비교해 손을 낮추고 한쪽 무릎을 굽히는 c8(back)/c13(front) contact로 연결, jumping c9/c12 제외. 준비/회복 및 시간은project phase selection;새BODY 생성 없음. 도적 기본 단검THRUST만 같은pose로연결,전사/타직업기본불변. 원본 근거 master/source/rogue_v232/reference.json.

Icon scope: 함정찾기1(012)→2(017) screenshot icon alias on book/quickslot,Slash022만 기존 두번찌르기 갈색palette;나머지PNG해시동일/링크새아이콘취득안함. 설치형트랩031은HolyBlow와같은retirement경로로목록/습득/시전/기존저장퀵슬롯에서제외,원본ID보존. Hide정상시전→전체착용배우80/255alpha;기존duration/피해시해제유지;새/진행중물리·마법incoming및taunt우회차단. 전용facing Sense policy현재전방1..2칸nearest몬스터만,자동접근/회전없음/접촉livevisibility+LOS재검사. 자동공격유지/.3s창닫힘·수동Close. 죽음의마을205939영상3.5s 원작갈색frame/header/scrollbar/Close원본crop을사용하며이전임의어두운프레임폐기;동적필드글꼴은runtime.

Affected files/shareddependencies: GameViewactor/input,RuntimeCombatPortAdapterhidden/inspect gates,RuntimeCombatSessionfacingentry,MonsterAIhiddenpriority,SkillAbilityExecutorstealthactivation,SkillBookretirementpolicy;sourceicon/motion/frameonly. No movement/save schema/damage formula change. Focused tests:7new V232sourcepose/facing+livegate/icon+retiredmigration/frame/hidevisual+queuednewphysicalmagic;V2315normalinputtests;V2294motion/UI/IDs;V227retire/save;direct ability/presentation/window/approach/Abelmonstercombat/F5mrestart,one acceptedmartialnormal6activecapture;concurrency audit,compile thenassemble. Shared paths included for incoming combat gate/old-save retirement;no unrelated maps/fullregression. Source deterministic and onlySlashiconchanged guardsPASS. IMPLEMENTED;BUILD/HOST_NATIVE/PHONEPENDING. SamePR197main41fe6defunmerged. NEXT:exactCI/nativecaptures/APKprovenance,thenhandoff.

