# PROJECT DARK 개발 지휘 기준

Revision LOOP-4-V1 · 사용자 승인: 4개 역할 유지, 상시 전원 가동보다 한 플레이 루프 완결 우선.
이 지침은 THREE-20260910 운영과 “마을 전체 PASS 전 게임 시스템 대기” 조건을 대체한다. 원작 재현·데이터 근거·시각 품질 기준을 면제하지 않는다.

## 공동 목표

밀레스 이동 → NPC 대화 → 몬스터 1마리 전투 → MONSTER_DEFEATED → 인벤토리 직접 보상 → 성장 반영 → 저장 → 프로세스 종료·재실행 후 복원.

이 루프 하나를 공동 목표로 삼고 역할마다 의존성이 적은 작업 하나를 맡는다. 마을 시각 QA 대기는 전투/보상/저장의 전역 선행조건이 아니다. 기존 정상 코드와 승인 자산을 유지하고, 크래시·통행 불가·데이터 손실·중복 보상은 즉시 해결한다. 새 지역/대량 콘텐츠/추가 직업/완벽한 걷기 튜닝은 이후다.

## 네 역할

| 역할 | 소유 | 첫 작업 |
|---|---|---|
| Visual Runtime | asset/renderer/frame/anchor/appearance layers | VISUAL-01 행동 시 승인 캐릭터가 도형 fallback으로 바뀌는 문제 |
| World Runtime | map/grid/movement/collision/camera/depth/portal | WORLD-01 spawn→NPC→훈련 몬스터→복귀 통행 검수 및 실제 장애 수정 |
| Game Systems | NPC/combat/reward/inventory/equipment/progression/quest/save 규칙 | GAME-01 기존 Resolver와 실제 행동 경로를 연결할 최소 API/adapter |
| Director/Integration | GameView/MainActivity wiring, 통합, CI/실행 검증, APK, 일정·backlog | DIRECTOR-01 GAME-01 결과를 실제 입력·루프에 연결하고 중복 damage 경로 제거 |

RuntimeState 규칙은 Game 소유다. GameView/MainActivity 연결은 Director 소유이며 Game 담당은 정확한 호출 위치/교체 계약을 제공한다. renderer metadata는 Visual, 공간 depth 알고리즘은 World 소유다. 같은 파일 교차 변경은 명시적으로 단일 편집자를 배정한다. Director는 통합 코드를 실제 수정할 책임이 있고 검토만으로 끝내지 않는다. 도메인 내부 전면 rewrite는 하지 않는다.

## 실행 및 통합

1. 최신 main SHA, 사용자 canon, 본 guide/backlog, 담당 handoff/활성 PR의 delta를 확인한다. 최초 전체 기준 읽기 이후 매번 모든 Master/history를 재감사하지 않는다.
2. 작업은 수락 조건이 있는 하나를 선택하여 구현→실제 호출 연결→관련 검증한다. 필요한 계약은 Producer→Contract→Consumer/파일/호출 위치로 남긴다. 이미 구현된 작업을 다시 시작하지 않는다.
3. 역할별 활성 브랜치/PR 하나를 재사용한다. worker는 main을 직접 변경하지 않는다. Director가 수정·통합 직전 최신 main을 확인하고 순차 통합한다. force push 금지. 실행 시간 차이는 직렬 실행 보장이 아니다.
4. 동시 작업은 같은 루프의 독립 부분만 허용한다. 다른 역할의 pixel-perfect 통과를 무관한 기능의 선행조건으로 만들지 않는다.
5. 같은 blocker가 두 회차 지속되면 Director가 API 축소, 작업 분리, owner 재배정, 직접 통합 수정 또는 구체적 환경 장애 처리를 선택한다. 시도와 새 해결책 없이 반복 보고하지 않는다.
6. 작업 없으면 IDLE과 다음 재개 조건을 남긴다. 억지 코드/문서/새 그림을 만들지 않는다. 준비된 결과를 검토하는 Director handoff가 다음 단계다.

## 예약 운영

기존 네 예약을 유지한다: 비주얼 매시 00분, 월드 15분, 게임 30분, 디렉터 45분(Asia/Seoul). 이번 개정에서 월드는 WORLD-01 때문에 재개한다.
Director는 매 실행 가능한 도구로 활성 상태·마지막 실행·실제 결과를 확인한다. enabled=true는 실행 성공 증거가 아니고 next_run_time=null은 다음 실행 보장이 아니다. 중단 원인을 모르면 UNKNOWN이다.
일감이 끝난 worker는 이유/마지막 SHA/인수인계/재개 조건/후속 점검 담당(Director)을 backlog에 기록한 후 기존 예약을 쉬게 할 수 있다. 이 운영 방식으로 쉰 worker는 담당 장애나 승인된 다음 작업이 생기면 재개하고 설정을 다시 확인한다. 사용자 직접 중단은 임의로 덮어쓰지 않는다. Director 자신의 예약은 자동 중단하지 않는다. 도구가 없거나 실패하면 상태를 기록하며 설정 변경을 완료했다고 주장하지 않는다.
알림·예약 증가나 실행 횟수 자체를 진척도로 삼지 않는다. 판정된 플레이 경험, 검증된 SHA와 다음 담당이 결과다.

## 검증 및 완료

- 구현/호출 연결, compile, 실제 실행한 관련 rule/integration audits, assembleDebug와 APK 존재, Android 실행, 시각 acceptance를 각각 기록한다.
- audit 클래스 컴파일과 audit 실행은 다르다. 기존 CI가 컴파일만 하면 실행 공백을 드러내고 필요한 관련 검증을 연결한다.
- 최신 build SHA의 이동/NPC/타깃/공격/사망 정확히 1회/보상/성장/프로세스 재시작 복원으로 공동 루프를 검증한다.
- 마을 지형·충돌·캐릭터 외형도 실제 Android 증거로 판정한다. 환경 미확보는 해당 runtime gate 미검증이며 독립 구현을 멈출 사유는 아니다.
- 보상 snapshot만으로 process restart 완료라고 하지 않는다. 앱 종료 후 동일 상태·중복 보상 방지를 확인해야 한다.
- 사용자에게 가치 있는 결과가 있으면 exact-SHA APK와 Asia/Seoul 빌드 시각, 변경/검증/미검증을 전달한다. mockup·atlas preview는 runtime screenshot이 아니다.
- IMPLEMENTED / BUILD VERIFIED / RUNTIME VERIFIED / VISUAL ACCEPTED를 구분한다. 이번 운영 문서 개정 자체는 게임 구현 완료가 아니다.

## canon과 Master

시작은 평민이며 직업은 이후 선택한다. 현재 승인 BODY/IDLE/WALK와 크기·방향·카메라를 불필요하게 다시 설계하지 않는다. 2dbcd9c에서 관측된 이동 간격은 0.60초이고 active source atlas는 36×48 cell을 1배로 그린다. 이것은 현재 코드 기준의 기록이며 원작 수치 선언이나 새 디자인 변경이 아니다. 역사적 0.80초/무도가 시작 규칙으로 되돌리지 말고 최신 사용자 승인과 실제 소스를 함께 확인한다.
행동 frame이 없으면 승인 BODY 유지와 명시적 최소 presentation을 사용하고 완성된 원작 공격 sprite라고 주장하지 않는다. 신규 AI/도형 그림으로 승인 자산을 대체하지 않는다.
원작 사실/수치는 필요 시 넥슨 어둠의전설 공식 홈페이지·가이드·공지로 검증한다. Master는 지속 갱신되는 기준이며 Director 책임이다. O/V/U/B/ADAPTED/FAN/PENDING_CROP와 출처·날짜를 유지한다. Nexon-hosted community 자료를 자동으로 O로 승격하지 않는다.
보상은 MONSTER_DEFEATED→resolution→inventory 직접 지급. ground-drop/pickup 없음. 기존 훈련 증표는 B/ADAPTED fixture이며 원작 드랍이 아니다. 성장 수치 미확정은 명시적 시험 설정으로 격리하고 평민에게 미습득 마법을 임의 개방하지 않는다.

## Master 개정 규칙

- `master/source`와 첨부에서 가져온 `master/data`는 원본 파생 스냅샷이다. 원본 손실을 감추려고 삭제/정리/요약하지 않는다.
- 개선은 `master/changes/<change-id>.json` 제안으로 작성한다. source sheet/cell/ID, before/after, 이유, evidence 원문·버전·날짜, 영향 받는 코드/저장/검증, status(PROPOSED/ACCEPTED/REJECTED)를 포함한다.
- director가 근거·참조·테스트·현재 사용자 확정을 검토하고 ACCEPTED로 기록한다. accepted 변경만 runtime adapter에 반영하고 Master manifest revision과 generator 입력에 연결한다. 원본 자료 자체의 변경은 새 source snapshot으로 추가한다.
- 명시적인 사용자 확정과 확인된 오류 수정은 불필요하게 재질문하지 않는다. unknown은 unknown이며, [B]는 원작 확정치로 승격하지 않는다. 개별 출처 필드에 SOURCE를 무조건 O로 매핑하지 않는다.
- 문서의 READY/PASS는 과거 작성자의 주장이다. 코드 구현·실행 검증과 별도다. source sheet 92개와 row 1786은 현재 첨부 측정값이며 추후 버전에서 바뀔 수 있다.
