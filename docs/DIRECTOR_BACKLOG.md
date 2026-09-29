# 디렉터 작업 지시

Revision LOOP-4-V1 · 사용자 승인 운영 개정.
공동 목표: 밀레스 이동→NPC 대화→몬스터 1마리 전투→직접 보상→성장→저장→프로세스 재시작 복원.
아래 네 역할 할당은 이전 개정의 기록이며, 최신 live 예약 상태·정확 SHA에서의 기능 상태를 각각 확인해야 한다. 2026-09-28 사용자 APK 실패 보고는 APK 전달/수락 전 최우선 Director blocker다. 이전 행은 완료나 취소로 간주하지 않는다.

| Task | Owner | 다음 결과 하나 | 수락 조건 | 의존성/상태 | 반복 blocker |
|---|---|---|---|---|---|
| WORLD-01 | World | 기존 spawn→NPC→훈련 몬스터→복귀 경로 검수 및 실제 통행 장애 수정 | 인접 tile/왕복 drift=0, 주요 footprint/shoreline 차단, 대화·공격 거리 접근, touch/camera 일치 | ACTIVE; 2dbcd9c 지형 정리 보존. Android 미실행은 Director QA handoff | 새 할당, 0 |
| VISUAL-01 | Visual | ATTACK/CAST 시 기본 캐릭터 외형 연속성 유지 | 승인 body/크기/방향/발 anchor 유지, resource 오류 crash 없음, HP mutation 없음 | ACTIVE; 원본 행동 frame 미확보는 명시하고 기존 BODY 보존. 실제 action contract는 Game/Director와 합의 | 새 할당, 0 |
| GAME-01 | Game | `RuntimeCombatSession` 단일 submit/tick/drain API와 Director 교체 handoff | action 구분·control/learned/resource/range/LOS/cooldown 검증·effect 1회·생명당 defeat 1회·훈련 증표 직접 지급 1회 audit PASS | IMPLEMENTED; Director가 GameView 직접 damage/legacy cooldown과 MonsterAI legacy route를 원자적으로 교체해야 함 | 없음, 0 |
| DIRECTOR-01 | Director | 실제 전투 입력/루프를 GAME-01과 연결·검증 | legacy direct damage 병행 제거, 관련 audit 실제 실행, 동일 SHA APK·실행 범위 명시 | ACTIVE; Game과 최소 API 합의. 결과 대기 중 다른 통합/검증 가능 | 새 할당, 0 |
| POTE-TEST-01 | Director/Integration | 크기·공격 표시 수정 APK를 기기에서 검증해 시각 피드백 종료 | 16 actor 배치/보상격리/방향별 이미지 유지, 투명 여백 정리 후 실루엣 높이 48 논리 픽셀 이하, 공격 표시 임의 돌진·회전 제거, 실제 인접 타일 공격 확인, exact-SHA CI + 같은 기기 재검증 | 사용자 2026-09-28 피드백: 설치 APK SHA 미제공 상태에서 과대/공격 포즈/선공 인상/강력한 놀 그래픽 문제 보고. 수정 source `8ca5fda1c2483589b6f16fc9800bb9f87597ec23`, Actions #36393396473 **성공**, APK artifact `10957720462`, APK SHA-256 `263a502075f438048feb0191550cf59cee8f9a424b41ffdf02605146d337c359`; 168 packaged PNG. 새 높이 및 비인접 추적 회귀 테스트 통과. Strong 별도 artwork 없음(기본 GNOLL concept 공유). 추적 반경 180과 인접 melee 판정 유지. **DEVICE_PENDING / VISUAL_PENDING**. | BUILD_VERIFIED; 기기 확인 대기, 0 |


## 즉시 인수인계 우선순위 (2026-09-28 확인)

| Task | Owner | 다음 결과 하나 | 수락 조건 | 상태 |
|---|---|---|---|---|
| DELIVERY-01 | Director + World | versionCode 48 Pote 후보를 실제 기기에서 검증하고 통합 여부를 기록 | 정확한 APK/SHA에서 시작 이동·안내인 숲 진입·숲 내부 이동을 확인. 실패하면 원인을 고치고 새 SHA에서 같은 시나리오 재검증 | BUILD_VERIFIED; DEVICE_PENDING; 브랜치 미병합 |

근거: 기존 전달 APK는 main `0506929`, SHA-256 `e10e94495a8cd5dba2f86753f28d617835d42ecc2f4f0baf331c9fe671b62ef2`이며 포테 격자 간격 결함을 포함했습니다. 활성 브랜치 `codex/pote-ground-tile-foundation`의 source candidate `16c6d9a`는 CI #36388750145 성공 및 Pote 경로/렌더 검사를 통과했습니다. APK artifact ID `10955795355`, APK SHA-256 `03f4ccae5fe0804fa78bbeaef974696c0064b3d2941bb190b8fc68d3e64e3e4f`. 실제 기기 검증은 아직 안 됐습니다. 기존 네 역할 할당을 완료/취소로 간주하지 않습니다.

## 다음 작업 대기열

Game/Director: GAME-01 뒤 실제 보상·EXP/Gold/level mutation 및 최소 save/restore에서 가장 큰 끊김 하나. 저장은 대규모 콘텐츠 이후로 미루지 않는다. GAME-01에 막히면 독립적인 저장 계약/구현을 작업 하나로 명시 전환할 수 있다.
World: WORLD-01 통행 검수 후 READY_FOR_RUNTIME_QA handoff; 실제 장애 없으면 IDLE, 추가 맵/지형 튜닝 금지.
Visual: VISUAL-01 뒤 현재 루프에서 실제 잘못 표시되는 부분이 없으면 IDLE; 새 장비 대량 제작 금지.
Director: POTE-TEST-01 CI is complete; perform its Android/device check before closing the task, then continue DELIVERY-01 device scenarios. 결과→통합→판정→다음 할당을 매 회차 닫는다. 동일 blocker 두 회차면 해결 방법/배정 변경.

## 초기 근거와 한계

감사 baseline 2dbcd9c945ee043ff36cb08f4f740ca1204ccad0.
- 제작 마을·성당·호수·평민 IDLE/WALK·타일 이동/충돌 수정은 main에 존재. 2dbcd9c CI compile/APK 성공 및 artifact 확인.
- GameView 공격은 state.damage 직접 경로, 훈련 증표 직접 보상은 연결, 퀘스트 HUD는 고정 문구, AUTO는 준비 중. 저장/프로세스 재시작 복원과 실제 성장은 미완성으로 감사됨.
- 이전 예약 지시의 기기 PASS는 과거 검수 기록이며 최신 SHA 전체 실행 증거로 승격하지 않는다.
- 운영 개정 자체로 게임 동작 또는 Android 검증이 완료된 것은 아니다.

## 예약 상태 및 대기 관리

기존 예약 ID:
- Visual: 6aa57a764bac8191bb090fe300fb7b7a
- World: 6aa57a8952648191bb96a2abcb510670
- Game: 6aa57a9c1c648191a1999dbbec5809c4
- Director: 6aa57ab0feac819196778cc4e3d2c7d1

이 개정은 World 재개와 네 prompt 교체를 승인한다. 실제 설정 결과는 automation readback으로 확인한다.
Director는 매 실행 live 상태와 마지막 결과를 확인한다. IDLE로 쉴 때는 이유/마지막 SHA/재개 조건/후속 점검 담당을 기록한다. next_run_time=null이면 다음 실행 보장이라고 쓰지 않는다. 원인 불명 중단은 UNKNOWN으로 남긴다.

각 task 완료 시 status/검증 범위/SHA·PR/다음 결과로 해당 행을 교체한다. 역사 전체를 복제하지 않는다.


| POTE-ART-IMG-01 | Visual + Director | 원작 자료와 13종 대응을 확인하고 독립 PNG 156장을 제작·시각 검수 | 13종 × 정지/공격/이동 × NW/NE/SW/SE; 각 파일 직접 확인 가능, 전신·방향·팔레트·투명 가장자리 검사; 168 콘셉트/반려 시트 참조 금지 | 앞선 3장만의 전달은 범위 누락. 이번 팜팻 SE 대기 4색 초안 중 실버는 후광 QA 반려, 나머지 3색도 사용자 수락 전. 늑대 ID 대응 미정. 활성 GitHub 브랜치에서 원본 종별 스크린샷 묶음 경로 미확인. 앱 미적용/새 APK 없음. | 0/156 VISUAL_ACCEPTED; IN_PROGRESS; SOURCE_GAP |


최신 팜팻 SE 대기 이미지 파일명·QA 범위는 PROJECT_STATE의 2026-09-29 범위 정정에 기록. 앞선 팜팻 구르기와 실버 대기는 후광 때문에 반려; 레드·그린·퍼플 대기 초안도 사용자 수락 전이다. 13종의 나머지 방향·동작은 미완료.


## Pote 몬스터 런타임 후보 교체 (2026-09-29)

| Task | Owner | 결과 | 검증/상태 | 다음 작업 |
|---|---|---|---|---|
| POTE-MONSTER-RUNTIME-01 | Visual + Director | 이전 16 identity의 `monster_test_v04` art runtime mapping 해제; 현재 5개 완전 세트(60 pose PNG) 등록 | Source edited on candidate branch `codex/pote-monster-sprites-runtime-20260929`; 48×48 transparent frame, visible height cap 30px, old production copy removed. Actions run `36534954426` passed at source SHA `1def7a92aeef68ce144ebaad8b94f02dee011a12`; artifact `11017989115`, versionCode 49, APK SHA-256 `9b8cb7e86c7b9c89b34667199711b1738f72aa9267b6edbb79743c934b18c799`. APK inventory: 60 new pose PNGs, zero old `monster_test_v04/` paths; automated pose and placement tests passed. Device QA remains pending. | 사용자가 v49를 미통과로 보고함: 입구에서 퍼플만 표시, walk/attack 부재, facing 불일치. 코드 수정 중: 전원 입구 근처 배치, walk gait/attack lunge, shared-hit에서 attack pose 노출, 공격 lock 해제. 새 CI에서 5종×4 대각 facing 이동/공격과 표시 영역을 검증하고 새 APK 만든 후 실제 기기 확인. |

현재 5종은 기존 생성 후보만 사용합니다. 트랜트 9장 부분 세트 및 그 밖의 미생성 종/강력형은 새 artwork가 완성될 때까지 미등록입니다. 12장은 방향/상태별 대표 still이며 multi-frame loop 미구현입니다.
