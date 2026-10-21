## V221 선택 포탈별 도착 보호 보강

Initial published source be4bf25c/run39692798621 remains under focused verification. Code review caught that selecting a different entrance while standing at the arrival gate must preserve the return latch; only explicit selection of that return destination releases it. Added both assertions to the existing new grouped-pin test. This production change requires a new exact-source focused run; it is not a duplicate no-change rebuild.

## 2026-10-22 V221 재개 — 몬스터 배치/영상 HP/750000EXP/지도 포탈 선택 구현, 검증 대기

기존 PR197/codex/rucesion-coast-v157, main41fe6def 유지. 마지막 성공 runtime45936b9b/V220 run39685771102; 문서HEAD884a3599에서 재개. 이전 프롬프트 이후 미커밋 작업은 현재 checkout에 없으므로 저장된 V220부터 수정. 이번 세션 첨부 경로는 실제 비어 있었음; 이전 실행 중단 전체원인은 UNKNOWN. 승인된 GitHub 수정 계속.

V221: 지상/집안 니크르+독거미, 지하A–E 니크르+웨어렛+독거미;72/15/36개체수·원본MPF/그리드 유지. 영상205939를 직접 복구/decode하여 센스 독거미192707/164044, 니크르161239/164926·Lv99·EXP237000·개체별속성 확인. 실제처치로그733772. 최고관측 HP 니크르164926/독거미192707을 runtime calibration으로 적용, 미피격최대HP확정아님. 웨어렛HP220000 및 MP240 임시값 유지/명시; 두영상에서웨어렛센스/마력 미확보. 사용자요청으로활성3종EXP750000,원본센스EXP와구분. 좀비는이번지역배치제외,기존안정ID/원본에셋보존. contract master/changes/V221_DEATH_NAVIGATION.json.

전체지도입구⊕터치→지도닫기→기존requestGroundWorld→live충돌/일반문·계단전환. grouped4cell portal은표시는중앙/목표는실제한칸,사용자의명시선택만arrival latch해제;자동bounce보호유지. 일반공백터치는읽기전용,지도bitmap2MiB·현재맵캐시/카메라복원유지. 거리가먼목적지UI/순간이동/야외건물이동버튼추가없음. 나머지마을실제서비스문핀동일입력지원.

검증범위:신규실제6town선택/문도착·lobby→field→house1 fullupdate/live몬스터·10씬27gate합법목표/blank/no-rebake·3종750000EXP중복방지/restart; 기존V220/V217/V219quickslot/GroundTap/HUD/RestartMatrix. shared저장schema/전역보상알고리즘변경없음. IMPLEMENTED; BUILD/HOST_NATIVE/PHONE/USER_VISUAL PENDING. 다음: exactSHA focused Android CI와APK증거확인. 별도후보 com.projectdark.mobile.v221test.

