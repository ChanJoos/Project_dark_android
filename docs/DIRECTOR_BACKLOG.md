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


## 즉시 인수인계 우선순위 (2026-09-28 확인)

| Task | Owner | 다음 결과 하나 | 수락 조건 | 상태 |
|---|---|---|---|---|
| DELIVERY-01 | Director + World | versionCode 48 Pote 후보를 실제 기기에서 검증하고 통합 여부를 기록 | 정확한 APK/SHA에서 시작 이동·안내인 숲 진입·숲 내부 이동을 확인. 실패하면 원인을 고치고 새 SHA에서 같은 시나리오 재검증 | BUILD_VERIFIED; DEVICE_PENDING; 브랜치 미병합 |
| MONSTER-ART-01 | Visual + Director | 원작 근거표와 함께 14종/색상형의 12개 개별 PNG 제작·시각 검수 | 총 168 PNG(대기/걷기/공격 × NW/NE/SW/SE); 강력한 변형 제외; 각 파일 존재·투명 배경·전신·포즈/방향 구분 QA; 종별 source/근거 등급 기록; 사용자 최종 시각 승인 전 APK 미적용 | IN_PROGRESS; 새 리빌드에 라이칸스로프 사용자 시각 수락 12장과 팜팻 빨강/보라/초록/실버 생성 후보 48장이 추적됨. 팜팻 후보는 1254×1254 원본이며 64×64 변환·앵커·사용자 승인 전이라 완료 산출물로 계산하지 않음. 남은 9종/색상형의 108개 개별 PNG는 미생성. `monster_artwork_manifest.csv`에서 상태/근거 추적. 별도 브랜치 prototype 및 폐기된 v0.4는 새 입력·수량에서 제외. Naver 나무형·보라 토끼는 이름표 미확인 후보이며 PDF/영상은 종별 프레임 근거를 추가하지 않음. |

근거: 기존 전달 APK는 main `0506929`, SHA-256 `e10e94495a8cd5dba2f86753f28d617835d42ecc2f4f0baf331c9fe671b62ef2`이며 포테 격자 간격 결함을 포함했습니다. 활성 브랜치 `codex/pote-ground-tile-foundation`의 source candidate `16c6d9a`는 CI #36388750145 성공 및 Pote 경로/렌더 검사를 통과했습니다. APK artifact ID `10955795355`, APK SHA-256 `03f4ccae5fe0804fa78bbeaef974696c0064b3d2941bb190b8fc68d3e64e3e4f`. 실제 기기 검증은 아직 안 됐습니다. 기존 네 역할 할당을 완료/취소로 간주하지 않습니다.

## 다음 작업 대기열

Game/Director: GAME-01 뒤 실제 보상·EXP/Gold/level mutation 및 최소 save/restore에서 가장 큰 끊김 하나. 저장은 대규모 콘텐츠 이후로 미루지 않는다. GAME-01에 막히면 독립적인 저장 계약/구현을 작업 하나로 명시 전환할 수 있다.
World: WORLD-01 통행 검수 후 READY_FOR_RUNTIME_QA handoff; 실제 장애 없으면 IDLE, 추가 맵/지형 튜닝 금지.
Visual: VISUAL-01 뒤 현재 루프에서 실제 잘못 표시되는 부분이 없으면 IDLE; 새 장비 대량 제작 금지.
Director: 결과→통합→판정→다음 할당을 매 회차 닫는다. 동일 blocker 두 회차면 해결 방법/배정 변경.

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
