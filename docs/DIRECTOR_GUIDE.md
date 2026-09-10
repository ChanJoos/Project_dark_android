# PROJECT DARK 개발 지휘 기준

2026-09-10 사용자 지시: 기존 4개 시간제 배치를 유지하고 역할을 재정의한다. Master는 완성된 정답지가 아니라 근거와 함께 지속 개정하는 공통 기준이다. 이 문서는 현재 사용자 지시를 실행 규칙으로 옮긴다.

## 매 실행 필수 절차

1. 최신 main SHA, `AGENTS.md`, 이 문서, `docs/DIRECTOR_BACKLOG.md`, `master/MASTER_MANIFEST.md`, `master/RECONCILIATION.md`, `design/DESIGN_CONSTITUTION.md`, `design/DATA_CONTRACT.md`, `design/SOURCE_OF_TRUTH.md`, `docs/DEV_HISTORY*`의 최신 기록과 해당 역할 handoff를 읽는다.
2. `python tools/validate_master.py` 실행 후 담당 테이블의 전체 행과 의존 테이블을 읽는다. 92개 목록 확인을 전체 내용 이해로 보고하지 않는다. 최초 감사는 모든 테이블을 파싱하고, 반복 실행은 변경된 테이블과 의존 범위를 검토한다.
3. backlog의 의존성이 풀린 가장 높은 우선순위 하나를 선택한다. 최신 main에서 `agent/<role>/<run-id>` 브랜치를 만든다. 기존 미완료 브랜치가 있으면 먼저 검토하고 이어서 작업한다.
4. 조사 → 구현 → 관련 규칙 검증 → compile → assembleDebug를 수행한다. 가능한 Android emulator/device 실행 검증을 수행한다. 도구가 없으면 실행하지 못한 gate와 원인을 기록한다.
5. 세 작업자는 main에 직접 push/merge하지 않는다. 자기 브랜치에 코드·테스트·자기 handoff를 함께 commit/push하고 draft PR을 만든다. 디렉터가 최신 main에 통합하고 재검증한다. force push 금지. 충돌 시 다른 역할 코드를 지우지 말고 조정한다.
6. 역할별 `docs/handoffs/<role>.md`에 기반 SHA, Master revision, 변경 파일, 근거, 정확한 실행 명령/결과, PR, 남은 blocker와 다음 작업을 적는다. 각자 다른 파일을 써서 동시 변경 충돌을 줄인다.

## 4개 역할과 쓰기 경계

| 역할 | 소유 영역 | 첫 번째 목표 | 다른 역할에 전달할 계약 |
|---|---|---|---|
| world | WorldDef, world/, character/, 지도·충돌·포털·방향별 동작·원본 evidence | 스크린샷 전체 배경 제거, 타일/오브젝트/충돌 레이어와 원본 tracing 분리 | mapId, spawnId, position, direction, collision/LOS API |
| combat | CombatController, AttackDef, SkillDef, combat/, monster AI | 생존→제어→타입→거리→LOS→자원→습득→쿨다운, actionId 1회 효과 | 승인 action/result event와 안정된 monster instance ID |
| rpg | RpgProgressionState, rpg/, data adapters | Master adapter, 평민 초기화, 저장·복구, ground drop→inventory→EXP | item/quest/progression/save schema와 idempotent reward |
| director | GameView, MainActivity, RuntimeState의 통합 접점, ui/, 빌드/CI, 공통 계약, backlog | 세 역할 통합, 실제 APK와 runtime 검수, 다음 우선순위 갱신 | 전체 기능 계약과 검증된 main |

기존 파일이 다른 시스템을 함께 담고 있으면 담당 모듈을 추출하고 공용 파일 연결 변경은 PR patch로 전달한다. 구조 개편 자체를 목표로 전면 재작성하지 않는다. 막힌 작업을 임시 상수로 메우기보다 동일 목표의 독립 하위 작업을 진행한다.

## 지속적인 디렉팅

director는 매 실행 시작에 열린 agent PR/CI/handoff와 main을 확인한다. 실패한 main·데이터 손실·중복 보상을 가장 먼저 복구한다. 다음으로 vertical slice의 첫 미완성 연결을 찾고 각 역할의 다음 작업·의존성·수락 조건을 `DIRECTOR_BACKLOG.md`에 갱신한다. 기존 제약과 모순 없는 세부 구현/모바일 [ADAPTED]/명시적 [B] 검증 fixture는 스스로 판단한다. 원작값·새 세계관·scope 확장은 발명하지 않는다.

시간 간격은 동시 실행 락이 아니다. 05/20/35/50분 실행이 겹쳐도 역할별 브랜치와 단일 integrator로 main 경쟁을 막는다. 자동화 한 번이 끝난 뒤 계속 메모리에서 일한다고 표현하지 않는다. 지속성은 Git 문서·커밋·다음 예약 실행으로 확보한다.

## Master 개정 규칙

- `master/source`와 첨부에서 가져온 `master/data`는 원본 파생 스냅샷이다. 원본 손실을 감추려고 삭제/정리/요약하지 않는다.
- 개선은 `master/changes/<change-id>.json` 제안으로 작성한다. source sheet/cell/ID, before/after, 이유, evidence 원문·버전·날짜, 영향 받는 코드/저장/검증, status(PROPOSED/ACCEPTED/REJECTED)를 포함한다.
- director가 근거·참조·테스트·현재 사용자 확정을 검토하고 ACCEPTED로 기록한다. accepted 변경만 runtime adapter에 반영하고 Master manifest revision과 generator 입력에 연결한다. 원본 자료 자체의 변경은 새 source snapshot으로 추가한다.
- 명시적인 사용자 확정과 확인된 오류 수정은 불필요하게 재질문하지 않는다. unknown은 unknown이며, [B]는 원작 확정치로 승격하지 않는다. 개별 출처 필드에 SOURCE를 무조건 O로 매핑하지 않는다.
- 문서의 READY/PASS는 과거 작성자의 주장이다. 코드 구현·실행 검증과 별도다. source sheet 92개와 row 1786은 현재 첨부 측정값이며 추후 버전에서 바뀔 수 있다.

## 완료 판정

- PLANNED: 의존성·수락 조건을 정했지만 미구현.
- IMPLEMENTED: 코드/데이터와 commit 존재. 빌드 성공을 암시하지 않는다.
- BUILD VERIFIED: 동일 SHA에서 compile·관련 규칙 검사·assembleDebug 성공과 APK 존재 확인.
- RUNTIME VERIFIED: 동일 SHA/APK의 실제 Android emulator/device 시나리오, 로그·스크린샷 증거 존재. 실행한 범위만 판정.

전체 목표: 생성→평민→밀레스→NPC/대화→초반 퀘스트→필드→전투→사망→바닥드롭→줍기→가방→EXP/레벨→저장→프로세스 재시작→복원. 전체 통과 전에는 다음 콘텐츠로 수평 확장하지 않는다. 원작 sprite 미확보와 기하 placeholder는 별도로 명시한다.
