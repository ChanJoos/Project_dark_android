## 2026-10-22 V221 검증 완료 — 지도 포탈 선택·배치·HP·750000EXP, 휴대폰 대기

Runtime `3bf65b964c85696420c7a56b38825231f5ec795a`, focused Actions39693321115/job119544557697 SUCCESS. 7 suites **27 PASS/0 failure/error/skipped**, scope selector5 PASS. 실제7town/field views의 지도핀선택→기존길찾기/문전환, lobby→field→house1 fullupdate와live72몬스터, 다른목적지선택/명시적귀환의분리된arrival latch,10씬27gate·12왕복·48poses,3종각750000EXP 1회지급/재시작,HUD/ground/4slot/restart검사. 초기be4bf25c/run39692798621는27중1실패(house1선택시lobby로복귀), 선택포탈별latch수정으로같은검사27개통과. 기대값완화/몬스터삭제없음.

APK **PROJECT DARK V221 TEST**, `com.projectdark.mobile.v221test` 별도후보. source3bf65b96/run39693321115; **2026-10-22 08:30:07 KST**,101629572bytes,SHA256 `17652ec90bcbb6492c1c1f91f28262de25ac7c214a024cf50bec15a244f0b2cc`. Artifact12389098422 ZIP SHA256 `6280e941779b3ccd446c13354b7fa9049529c1eb851e7cc0cc291164dc35fa11`. ZIP/APK CRC,embedded source/run,binarymanifest package/label,독립V2 RSA서명+99chunk contentdigest+certificate확인.825deathPNG포함,sourcepack/generator/grid는V220과byte동일;기존원본bytecompare근거재사용. 실제house1도착/선택가능로비지도native캡처직접검토. APK저장완료 libfile_4b63368b0f648191b0d12e1cd59f025d.

최종배치 지상/집안 니크르+독거미,지하A–E 니크르+웨어렛+독거미,72/15/36유지. 니크르HP164926/독거미192707은영상최고관측HP기준calibration,원작미피격최대확정아님. 센스EXP237000/실제로그733772와사용자설정750000을구분. 웨어렛HP220000/MP240및기타미확인전투값은기존B/ADAPTED;영상센스에MP항목없음. 기존recall의웨어렛172875/니크르141008은현재영상에서미검증이므로반영안함. 상세계약 master/changes/V221_DEATH_NAVIGATION.json.

Common39693329803/scope119544587306 SUCCESS,full compile의도적SKIPPED. 본27검사는관련호스트native검증이며최종main전체회귀/실기FPS/사용자시각수용주장아님. 12왕복host최대35.841756ms,atlas8,847,360bytes/32floorLRU bound PASS. IMPLEMENTED/BUILD VERIFIED/SCOPED HOST_NATIVE VERIFIED. **PHONE/USER_VISUAL PENDING**. PR197미병합/main41fe6def유지;별도앱/저장,자동이전없음. receipt verification/DEATH_NAVIGATION_V221_BUILD.json. 이후문서HEAD를APK소스로재라벨하지않으며무관한재빌드금지. 다음은exactV221휴대폰지도포탈선택/이동/사냥피드백.

## V221 선택 포탈별 도착 보호 보강

Initial published source be4bf25c/run39692798621 remains under focused verification. Code review caught that selecting a different entrance while standing at the arrival gate must preserve the return latch; only explicit selection of that return destination releases it. Added both assertions to the existing new grouped-pin test. This production change requires a new exact-source focused run; it is not a duplicate no-change rebuild.

## 2026-10-22 V221 재개 — 몬스터 배치/영상 HP/750000EXP/지도 포탈 선택 구현, 검증 대기

기존 PR197/codex/rucesion-coast-v157, main41fe6def 유지. 마지막 성공 runtime45936b9b/V220 run39685771102; 문서HEAD884a3599에서 재개. 이전 프롬프트 이후 미커밋 작업은 현재 checkout에 없으므로 저장된 V220부터 수정. 이번 세션 첨부 경로는 실제 비어 있었음; 이전 실행 중단 전체원인은 UNKNOWN. 승인된 GitHub 수정 계속.

V221: 지상/집안 니크르+독거미, 지하A–E 니크르+웨어렛+독거미;72/15/36개체수·원본MPF/그리드 유지. 영상205939를 직접 복구/decode하여 센스 독거미192707/164044, 니크르161239/164926·Lv99·EXP237000·개체별속성 확인. 실제처치로그733772. 최고관측 HP 니크르164926/독거미192707을 runtime calibration으로 적용, 미피격최대HP확정아님. 웨어렛HP220000 및 MP240 임시값 유지/명시; 두영상에서웨어렛센스/마력 미확보. 사용자요청으로활성3종EXP750000,원본센스EXP와구분. 좀비는이번지역배치제외,기존안정ID/원본에셋보존. contract master/changes/V221_DEATH_NAVIGATION.json.

전체지도입구⊕터치→지도닫기→기존requestGroundWorld→live충돌/일반문·계단전환. grouped4cell portal은표시는중앙/목표는실제한칸,사용자의명시선택만arrival latch해제;자동bounce보호유지. 일반공백터치는읽기전용,지도bitmap2MiB·현재맵캐시/카메라복원유지. 거리가먼목적지UI/순간이동/야외건물이동버튼추가없음. 나머지마을실제서비스문핀동일입력지원.

검증범위:신규실제6town선택/문도착·lobby→field→house1 fullupdate/live몬스터·10씬27gate합법목표/blank/no-rebake·3종750000EXP중복방지/restart; 기존V220/V217/V219quickslot/GroundTap/HUD/RestartMatrix. shared저장schema/전역보상알고리즘변경없음. IMPLEMENTED; BUILD/HOST_NATIVE/PHONE/USER_VISUAL PENDING. 다음: exactSHA focused Android CI와APK증거확인. 별도후보 com.projectdark.mobile.v221test.

