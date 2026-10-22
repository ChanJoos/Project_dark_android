## 2026-10-22 V222 검증 완료 — 원본 입구·지하문·웨어렛 HP·밀집 충돌 비용 수정 후보

사용자 실기기 결함 신고 DEVICE_FAILED_USER_REPORTED/설치 SHA UNKNOWN 이력 유지. 새 후보 source `ae0c7ff69c130479c013ad831a6ff696efcb50ea`, focused run39698748694/job119562479699 SUCCESS; 관련13 suites **47 PASS/0 failure/0 error/0 skipped**, scope selector5 PASS. common39698755769 SUCCESS(범위검사; full compile 의도적skip). main41fe6def/기존PR197미병합; 최종main전체회귀는별도. IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_NATIVE VERIFIED; PHONE·USER_VISUAL ACCEPTANCE PENDING.

웨어렛HP180000(U/ADAPTED 사용자요청),니크르164926/독거미192707·3종EXP750000유지. 로비→야외원본spawn12,85복구;원본지하문6633/6639총10개에문발시각핀+합법인접문턱1개씩join,양방향arrival수정. 27gate합법/원본12,85실제entry·지도선택도착·충돌navigation/13문·계단native캡처검증. B14,19분기와C/E25,10귀환은명시적adapted계단. 원본그리드·충돌·825PNG·해상도불변;새원작사실로간주하지않음.

밀집 충돌: ID고정반경1회계산+비정규식species+보수적live AABB선별. 500독립point/segment/reservation oracle PASS. 72/36/100마리·AI빈도·충돌·이동·저장유지. 같은6000query의CPU 야외382.90→14.92ms,지하36.53→2.97ms,광산94.06→3.50ms;임시할당291703656→507744/114390680→272048/300384048→266928bytes. 이는호스트hotspot측정(약92–96%CPU/99%이상할당감소)이며FPS주장아님. 최종각90실제AI/update/drawframes+맵/개체수검사PASS. 전체프레임cold outlier는남음(광산update891.79ms,야외draw262.62ms);폰렉완전해결미확인. 직전31cfe95b/run39697696611도동일production47PASS/전체before+after자료보존;실행이8분이상으로보여비교범위를좁혔으나구실행은이미성공종료하여취소되지않았음. 추가동일소스재검증금지.

APK `PROJECT DARK V222 TEST` / `com.projectdark.mobile.v222test`(독립설치후보),legacy184/1.84-mobile-horror. 빌드 **2026-10-22T09:24:50.082134+09:00**,101630340bytes,SHA256 `f54951327dd08f24b67d8961cdeeb1d69231b41ad086fc76918051d4cc28eef0`. artifact12392152261/ZIP26d2c4faf2adbd304cc84bec0b21a59bc203f109c9d6ac749a9ee3ce1db33882. ZIP CRC/embeddedsource+run/manifest/독립APK V2 RSA+cert+contentdigest PASS. 13문캡처직접검토및최종픽셀동일확인. 상세 `verification/DEATH_REPAIR_V222_BUILD.json`; 이후문서HEAD를APK소스로재라벨하지않음. 다음은정확V222휴대폰입구/문이동/밀집사냥피드백.

## V222 bounded comparison methodology

31cfe95b/run39697696611 continued over8minutes in the combined before+after step, with no live log availability. Bound old-code comparison to identical6000 actor queries/population/point-and-segment oracle; retain all90 real dense AI/update/draw frames and map/population assertions for final production. This compares hotspot CPU/allocation only; do NOT claim before/after whole-game FPS or frame delta. Superseded run cancellation is guarded by exact oldSHA and only performed if still running; retain any completed artifact as historical evidence. No production bytes or requested-feature assertions changed.

## V222 dense fixture actual entrance repair

61d21e9a/run39697181667: host geometry/allocation APIs now compile/run; 500 independent collision oracle test PASS. Dense fixture failed72→0 because it used a direct map setup with no inbound Gate, leaving the initial return gate unlatched; first full update returned to lobby. Set up Death scenes through their actual inbound Gate and assert exact map on every measured frame, keeping72/36/100 population assertions. No production change or assertion relaxation; re-run paired measurements with real entrance semantics.

## V222 host-only measurement API compile repair

834cfd59/run39696857596 reached Java test compilation; Android test compile bootclasspath excludes java.desktop/java.management compile symbols. Production compiled. Test-only independent JDK Line2D oracle and ThreadMXBean allocation probe now use reflection against the actual host interfaces; no production behavior or assertions removed. Repeat exact-source baseline/after runtime verification next.

## V222 CI baseline checkout repair

Published f98cb3df/run39696577345 stopped before Java compilation: checkout was shallow, so the exact V221 source used by git show was absent. Added explicit depth1 fetch of approved baseline3bf65b96 before the temporary before-phase. Trap restored final production; no runtime test or APK existed from that failed attempt. Common scope39696584605 succeeded. Production bytes unchanged; next exact-source focused run required.

## 2026-10-22 V222 사용자 실기기 결함 신고 — 입구/지하문/밀집 렉 수정, 검증 대기

DEVICE_FAILED_USER_REPORTED: 야외 시작점이상·지하문과분리된포탈·몬스터밀집렉. 사용자설치APK정확SHA는UNKNOWN;직전전달V221호스트27PASS를휴대폰수용으로간주하지않음. main41fe6def/기존PR197/codex/rucesion-coast-v157,마지막성공소스3bf65b96/run39693321115에서재개,문서HEADc91f1359. 이전V219의임의FIELD28,10을원본source spawn12,85로복구. 웨어렛HP180000은사용자조정U/ADAPTED,원작확정아님;3종EXP750000유지.

원본지하문stc6633/6639총10개모두문발위치시각핀과합법인접문턱을join. 기존11,3/3,4/46,12등임의gate삭제,원본그리드/문아트/충돌불변. B는원본문2개에3연결필요하므로추가D분기는14,19명시적계단, C/E건물귀환계단은25,10의빈walkable바닥. 모든반대방향arrival동시수정,house1-A-B-C/D-E-house2 및house3-C/던전제외유지. 10원본문좌표·27gate실제왕복·native문턱캡처검증예정.

밀집비용검토: actor충돌의매검사마다species정규식split과동일반경계산반복. ID고정반경을monster생성시1회계산,공유species는indexOf/substring,상세endpoint/예약/교차segment검사전conservative live AABB로먼개체제외. 움직임/예약정보프레임간캐시없음.72/36/100마리·해상도·AIcadence·충돌·보상/save유지. 이전소스2클래스만임시적용한before와final after를같은fixture로호스트paired측정하고최종production복구git diff guard 후APK생성.500독립collision oracle와기존공간/chase/이동회귀포함;무관한성장/스킬전체회귀를자동추가하지않음. 새공유이동규칙/save schema변경없음.

IMPLEMENTED; BUILD/HOST_NATIVE/PHONE/USER_VISUAL PENDING. contract master/changes/V222_ENTRY_DOORS_DENSITY.json. 다음: exactSHA baseline/after비교와관련Android검증,실제entry/10doors native리뷰,APK서명·소스·해시확인후전달. 휴대폰렉완전해결은아직주장하지않음.

