# PROJECT DARK 결정 및 제안 기록

이 문서는 승인된 결정을 아이디어와 구분합니다. 상세 규칙은 인용된 원본 문서가 권위 문서입니다.

## 확정된 프로젝트/개발 결정

| 결정 | 범위와 의미 | 근거 |
|---|---|---|
| 개발은 설계→데이터/출처→런타임→플레이 장면→APK→기기 증거→수락을 구분한다. | 클래스 구현이나 컴파일만으로 기능 완료라 하지 않는다. | `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md` |
| 구현, 빌드 검증, 기기 검증, 시각 수락은 별도 상태다. | 증거가 없는 단계는 미확정으로 둔다. | `AGENTS.md`, `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md` |
| 승인된 외형/동작과 모듈 소유권을 보존하고 기존 계약을 먼저 조사한다. | 관련 도메인 계약과 검증을 확인한다. | `AGENTS.md` |
| 원작 근거가 없는 사실은 `UNRESOLVED`로 두며, 대체안은 그 사실을 명시한다. | 제안이나 임시 fixture를 원작 정사로 쓰지 않는다. | `AGENTS.md`, `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`, `master/MASTER_LAW.md` |
| 포테 몬스터 v0.4 묶음은 검토용이다. | 파일 저장만으로 APK 적용이나 시각 수락을 뜻하지 않는다. | `0506929`, `assets/pote/review/monster_concepts_v0.4/README.md` |
| APK는 활성 작업이 포함된 SHA에서 빌드한다. | main과 활성 task branch를 비교하고, 미병합 브랜치 빌드는 후보라고 표시한다. | 2026-09-28: main `0506929` 산출물은 Pote navigation 결함이 있는 구버전이었고, 수정은 `codex/pote-ground-tile-foundation`에 있음 |
| Artifact/SHA 일치는 기기 수락을 대신하지 않는다. | 사용자가 요청한 화면/조작 시나리오를 기기에서 별도로 검증한다. | `AGENTS.md`, `docs/CHAT_HANDOFF.md`; 2026-09-28 사용자 실패 보고 |

## 아이디어·기획 목표 (승인/구현 여부 별도 확인)

다음은 기획 목표 또는 검토 항목이며, 현재 기능이라는 뜻이 아닙니다.

- 원작 세계와 규칙을 근거 기반으로 모바일에서 구현하고 설치·저장 복구·성능·원작 비교를 검수하는 방향.
- 밀레스 이동→NPC 대화→전투→보상→성장→저장→재시작 복원 플레이 루프.
- 직업·성장·전투·퀘스트·UI·경제·온라인 기능을 단계적으로 구현하는 항목.

세부 제안과 원작 근거는 `master/PROJECT_DARK_V0.6_PLAN_KO.md` 및 관련 출처/SSOT 문서에서 항목별로 확인합니다. 미병합 Pote branch의 통합 여부는 기기 검증 및 변경 검토 전까지 미정입니다.

## 이력

새 결정/변경만 날짜와 근거를 붙입니다. 구현 상태는 `docs/PROJECT_STATE.md`, 작업 순서는 `docs/DIRECTOR_BACKLOG.md`에서 관리합니다.
