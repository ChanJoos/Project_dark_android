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

## 2026-09-29 — Pote auto-target / walk presentation follow-up

| 결정 | 범위와 의미 | 근거 |
|---|---|---|
| 몬스터 walk 방향은 AI의 원래 추적 의도가 아니라 충돌·detour를 거쳐 실제 적용된 canonical 타일 벡터로 선택한다. | 진행 중인 walk pose는 stale attack-facing을 무시하며, 네 방향 Pamfet 자산의 실제 시선은 기기 확인 전까지 수락하지 않는다. | 사용자 보고; `PoteForestMonsterShowcase`, `MonsterDiagonalLocomotion`, `RuntimeState`; `docs/MONSTER_CREATION_MANUAL.md` |
| 자동공격 타깃은 도달 가능한 합법 인접 타일까지의 최단 경로 수로 고른다. | 같은 경로 수에서는 직선거리를 tie-break로 사용하고, 선택된 유효 생존 타깃은 쓰러지거나 경로가 끊길 때까지 유지한다. 이동 중 타깃은 예약 도착 타일을 계획점으로 사용한다. | 사용자 보고의 빙글돎/비효율 타깃; `WorldMoveTargetController`, `WorldRuntimeAdapter`, `GameView` |
| 사용자 캡처에서 전사한 구클라이언트 요구 조건은 원본 92-sheet hash 검사 대상과 구분한다. | `Skill_Evidence.csv`의 SE10은 구조 검증 canonical override, 상세 표는 `Skill_Legacy_Requirements.csv` canonical addition으로 추적하며 원본 XLSX fidelity를 주장하지 않는다. | Main `c2e60d6`의 validator failure `36586971014`; `tools/validate_master.py`; 사용자가 제공한 캡처 전사 설명 |
| 최신 skill 캡처 반영으로 갱신된 `Skill_Master`, `Skill_Requirements`, `Skill_Research_Audit`도 원본 XLSX hash 검증과 구분하고, canonical override로 schema/header와 행 폭을 검사한다. | `Skill_Evidence`의 구형 6-column 증거 행은 27-column schema로 확장하고 기존 필드는 그대로 유지한다. | Main `50c65cc`와 validator integrity audit; 원본 XLSX 미확보 |
| 몬스터 장애물 추적은 매 걸음 greedy 방향을 다시 시도하지 않고, 합법 타일 그래프의 최단 melee 접근 경로를 따른다. | 장애물 detour 중에도 다음 타일을 재계산할 때 안정된 최단 경로를 고른다. | 사용자 보고; `MonsterDiagonalLocomotion.select` source audit; `MonsterChasePathfinder` |

## 2026-09-28 — Pote forest monster visual test scope

| 결정 | 범위와 의미 | 근거 |
|---|---|---|
| 사용자는 현재 Pote 숲에 정의된 모든 몬스터를 배치해 외형/상태별 포즈를 테스트하도록 요청했다. | Master roster의 16 identity를 검토용 v0.4 14-form pose pack에 연결하는 것은 B/ADAPTED test deployment다. 위치/개수/HP/미확보 개별형 외형은 원작 사실로 승격하지 않는다. | 사용자 요청(2026-09-28); `PoteMonsterRoster.java`; `assets/pote/review/monster_concepts_v0.4/README.md` 및 `manifest.csv` |
| 시각 테스트용 `POTE_SPIRIT_TEST_B`는 canonical spirit 보상과 분리한다. | 40 HP 테스트 개체가 canonical reward를 발생시키지 않도록 isolated runtime ID를 쓴다. canonical Master 행/보상 데이터는 변경하지 않는다. | `PoteForestMonsterShowcase.java`; `master/data/Monster_Master.csv`의 `POTE_SPIRIT` row |
| 단일 walk/attack pose를 루프 애니메이션으로 기록하지 않는다. | 방향/state 이미지 스왑과 실제 AI 이동, 공격 상태 표시를 테스트하며 여러 프레임 순환은 별도 자산 확보 전 미구현이다. | v0.4 review README/manifest; 구현 범위 |

구현/CI/APK/device 상태는 `docs/PROJECT_STATE.md`의 작업 기록으로 추적한다. Strong 변종 외형 공유는 테스트 편의를 위한 비정사 mapping이며, 원작 외형 확정이 아니다.

## 2026-09-28 — 사용자 화면 피드백에 따른 시각 수정안 (기기 미수락)

| 구분 | 내용 | 근거 / 상태 |
|---|---|---|
| 사용자 요구 | 몬스터 크기를 플레이어에 맞게 줄이고, 어색한 공격 모션과 접근 전에 공격하는 듯한 인상을 바로잡으며, 강력한 놀의 깨져 보이는 그래픽을 확인한다. | 2026-09-28 사용자 화면 피드백. 보고된 설치 APK SHA는 미확인. |
| 테스트 렌더링 수정 | 포즈 PNG의 투명 여백을 기준으로 실루엣 높이 44 논리 픽셀, 비율 유지; 공격 포즈에 인공 돌진/회전은 추가하지 않고 제공된 단일 대표 포즈를 표시한다. | 구현 commit `8ca5fda1c2483589b6f16fc9800bb9f87597ec23`; Actions #36393396473 성공, APK artifact `10957720462`. 기기 결과 대기. 이는 테스트 장면 렌더링 값이며 정사 몬스터 스케일/공격 애니메이션 결정이 아니다. |
| 미확정 | 강력한 놀의 별도 그림 필요 여부와 추적 반경 변경 여부. | 현재 pack에는 별도 strong-GNOLL artwork가 없어 기본 GNOLL concept를 공유한다. 새 APK에서 크기/선명도를 보고한 뒤 결정한다. |


## 2026-09-29 — 몬스터 이미지 개별 파일 형식

| 구분 | 내용 | 근거 / 상태 |
|---|---|---|
| 확정된 출력 형식 | 몬스터 시트 대신 몬스터/포즈별 개별 이미지로 만든다. 전체 요구는 비강력형 13종 × 정지/공격/이동 각 4방향 = 종당 12개 이미지다. | 사용자 명시(2026-09-29), 앞서 확정한 12컷 규격. |
| 이번 생성 | 팜팻 구르기 공격 1장, 기본 직립 늑대인간 대기 1장, 갑옷·칼 늑대인간 대기 1장. 모두 컨셉 초안이며 종 ID 배정·시각 수락·런타임 적용은 미정/미실시. | 생성 입력은 Library: 팜팻 승인 외형 libfile_d213c79680108191b4f13b8bc89b68de, 늑대 이미지 libfile_1c5a4161d2b08191be701bf0f89b463f 및 libfile_f65c74247704819180f7e8bfa212fee3. |
| 참조 제외 | 이전 168장 콘셉트 PNG와 사용자 반려 생성 시트는 새 이미지 생성에 사용하지 않는다. | 사용자 명시. |


| 생성물 QA 상태 | Pamfet의 원형 구르기·잎은 표현됐으나 보라색 후광이 제거되지 않아 미수락. 무장 늑대 재생성은 칼 길이를 줄였지만 여전히 컨셉 후보. | 최신 파일 이름과 SHA-256은 `docs/PROJECT_STATE.md`의 “이미지 자체 검수 추가”; 사용자 시각 수락 전. |


## 2026-09-29 — Pote 생성 후보의 테스트 APK 등록 승인

| 구분 | 확정 내용 | 근거/범위 |
|---|---|---|
| 이번 테스트 APK | 기존 16 identity의 이전 `monster_test_v04` runtime 이미지 연결을 해제하고, 현재 완성된 5개 생성 후보 세트만 idle/walk/attack 4방향 렌더 경로에 등록한다. | 사용자 요청(2026-09-29); 이번 테스트 APK 범위에 한한 승인. 신규 아트의 원작 정사/시각 수락 승인과 구분. |
| 등록 종 | 퍼플/레드/그린/실버 팜팻 및 라이칸스로프, 각 12개 대표 포즈 = 60 PNG. | 현재 저장소의 각 파일 실재 확인. `monster_artwork_manifest.csv` runtime registration columns. |
| 미등록 종 | 트랜트(9/12), 나머지 미완성 종 및 강력한 변형은 등록하지 않는다. 없는 방향·상태를 복사/추정하지 않는다. | 현재 소스 파일 확인; 사용자 이전 결정에서 강력한 변형 제외. |
| 아트 상태 | 프레임 PNG는 생성 후보의 단일 still pose다. 상태/방향에 따라 교체하며 다중 프레임 루프는 구현하지 않았다. | 생성 원본 및 `PoteFieldRenderer` 구현 계약. |
| 구버전 보존 | 기존 v0.4 자료는 review 이력으로 남지만, production source set과 APK에는 포함하지 않는다. | Android `app/build.gradle` production asset roots 및 삭제한 `assets/pote/production/monster_test_v04/`. |


| 테스트 APK 검증 결과 | GitHub Actions run `36534954426` / source `1def7a92aeef68ce144ebaad8b94f02dee011a12` 성공. versionCode 49 artifact `11017989115`; APK SHA-256 `9b8cb7e86c7b9c89b34667199711b1738f72aa9267b6edbb79743c934b18c799`. APK 안에서 신규 PNG 60개, 이전 v0.4 PNG 경로 0개를 확인. | CI artifact inventory 및 Pote pose/placement tests; 실제 기기 시각 QA는 별도 미완료. |
