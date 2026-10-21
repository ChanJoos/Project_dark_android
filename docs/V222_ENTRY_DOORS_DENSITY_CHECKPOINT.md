## 2026-10-22 V222 사용자 실기기 결함 신고 — 입구/지하문/밀집 렉 수정, 검증 대기

DEVICE_FAILED_USER_REPORTED: 야외 시작점이상·지하문과분리된포탈·몬스터밀집렉. 사용자설치APK정확SHA는UNKNOWN;직전전달V221호스트27PASS를휴대폰수용으로간주하지않음. main41fe6def/기존PR197/codex/rucesion-coast-v157,마지막성공소스3bf65b96/run39693321115에서재개,문서HEADc91f1359. 이전V219의임의FIELD28,10을원본source spawn12,85로복구. 웨어렛HP180000은사용자조정U/ADAPTED,원작확정아님;3종EXP750000유지.

원본지하문stc6633/6639총10개모두문발위치시각핀과합법인접문턱을join. 기존11,3/3,4/46,12등임의gate삭제,원본그리드/문아트/충돌불변. B는원본문2개에3연결필요하므로추가D분기는14,19명시적계단, C/E건물귀환계단은25,10의빈walkable바닥. 모든반대방향arrival동시수정,house1-A-B-C/D-E-house2 및house3-C/던전제외유지. 10원본문좌표·27gate실제왕복·native문턱캡처검증예정.

밀집비용검토: actor충돌의매검사마다species정규식split과동일반경계산반복. ID고정반경을monster생성시1회계산,공유species는indexOf/substring,상세endpoint/예약/교차segment검사전conservative live AABB로먼개체제외. 움직임/예약정보프레임간캐시없음.72/36/100마리·해상도·AIcadence·충돌·보상/save유지. 이전소스2클래스만임시적용한before와final after를같은fixture로호스트paired측정하고최종production복구git diff guard 후APK생성.500독립collision oracle와기존공간/chase/이동회귀포함;무관한성장/스킬전체회귀를자동추가하지않음. 새공유이동규칙/save schema변경없음.

IMPLEMENTED; BUILD/HOST_NATIVE/PHONE/USER_VISUAL PENDING. contract master/changes/V222_ENTRY_DOORS_DENSITY.json. 다음: exactSHA baseline/after비교와관련Android검증,실제entry/10doors native리뷰,APK서명·소스·해시확인후전달. 휴대폰렉완전해결은아직주장하지않음.

