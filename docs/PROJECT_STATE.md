# PROJECT DARK 현재 상태

## 최신 작업 기준 — v0.61

통합 PR #172의 runtime source는 `89bad5a620324f9e5b0b3c8fbc7191fe59fb2a1f`; 정확한 CI/APK 및 검증 범위는 최신 SKILL-WINDOW-05 기록을 따른다. v60의 모든 미구현 항목 결제는 폐기됐으며 기본 목록은 실제 효과 있는30개다. v59 캡처 숙련/직업/선행 조건은 최신 사용자 지시로 폐기됐다. 이후 섹션의 오래된 main SHA는 해당 날짜의 감사 이력이며 현재 main으로 해석하지 않는다.


## 2026-09-30 — SKILL-WINDOW-04 실제 게임 스킬창 / v60

- 사용자 보고: 이전 스킬창의 아이콘 아래 이름 정렬, 빈약한 설명, 애매한 조건과 파란 모서리를 거절했다. 이전 전달 v59 SHA-256 `4e47fc87e67321598dfe1b253f1b57b2597b0382fb765dbf78d23c9af5391bac`는 VISUAL_REJECTED / DEVICE_FAILED (user-reported); 실제 설치 APK identity는 독립 확인하지 않았다. 이전 CI 성공을 사용자 수락으로 유지하지 않는다.
- 최신 사용자 확정: 원작 습득 규칙에서 벗어나 스탯과 돈/필요 아이템만으로 창에서 직접 습득한다. 숙련도와 선행 스킬을 제거하며 직업/승급/써클/레벨도 습득 제한으로 쓰지 않는다. 명시적 습득 버튼 한 번으로 검증→비용 차감→습득→저장한다. 창을 여는 것만으로 자동 소비하지 않는다.
- 새 별도 project policy는 Master 219행/원본 캡처를 보존한다. Gold 등급 가격 50/150/500/1200/2500/5000, 기본공격·문열기 무료, 6개 재료 레시피는 PROJECT_ADAPTED_V60 설계다. 캡처 스탯을 재사용하며 미확정 수치는 프로젝트 tier fallback; 원작 가격/룰 확정이 아니다. 모든 학습 ID에 정확한 stats/Gold/items/요약이 있으며 CI 재생성 일치를 검사한다.
- 네 열×세 행 카드, 아이콘/이름 중앙 정렬과 두 줄 이름, 기술/마법·분류·배운 스킬 필터, 고정 설명/습득 조건 탭, 실제 MP/위력/회복량/재사용/대상, 요구/현재 스탯 및 필요/보유 Gold·아이템, 하단 8슬롯으로 재구성했다. 본문은 탭해도 바뀌지 않으며 단어 경계에서 줄바꿈한다. 135 exact-ID 원본은 그대로 보존하고 native cache에서 cyan 캡처 배경 모서리만 마스크/clip한다.
- 경제 및 저장: 전체 자원 확인 후 한 번 차감. 중복 습득 차감 없음. 학습 checkpoint 실패 시 Gold/items/equipment/book을 복원. 등록/해제 실패도 기존 슬롯을 복원하며 read-only 저장에서 비용/등록 변경을 거부한다. 기존 save 호환은 유지한다. 성공 액션의 숙련 값은 기존 save 호환 목적으로 남지만 조건/창에는 쓰지 않는다.
- Runtime source **e0f61bf1712e7b18ece4c9602e5dbf82da0bf6f3**, base **1927ef9204a288ff35cd4fd440cc36204a77f0c2**, PR [#171](https://github.com/ChanJoos/Project_dark_android/pull/171). Actions [36636635716](https://github.com/ChanJoos/Project_dark_android/actions/runs/36636635716) **SUCCESS**, job **109638772772**. Actual checkout is synthetic PR merge **a9a3bdf2a89bfe7be7d84ef69e20afc5b2b9a978** (head e0f61bf1712e7b18ece4c9602e5dbf82da0bf6f3 into base 1927ef9). First run 36636038805 failed at boxed Gold JUnit assertion compile ambiguity; corrected before the final passing run. Earlier passing 36636243096 predates final word wrapping/read-only tests and is not the delivered APK.
- All 13 SkillWindowTest cases and configured Master/catalog/policy reproducibility, movement/HUD/target/combat/growth/quest/save/restart/render regression checks and assembleDebug passed. Input evidence includes fresh COMMONER basic attack through real utility→common card→learn→slot→HUD and paid martial learning with injected stats/Gold but no profession injection. Self-heal/cooldown examples inject learned IDs for isolated timing review; not physical-device evidence.
- APK versionCode **60**, versionName **0.60-game-skill-book**. Build completed **2026-09-30 07:00:03 KST**. APK artifact **11064287506**, render artifact **11064462402**; names carry checkout a9a3bdf rather than head source. APK **13,067,379 bytes**, SHA-256 **8a11132af58c2615ef5e2b00e4ae9ea33115c5799d62ecbaa3570bec1d91f073**. Packaged policy/icon atlas/icon mapping/capture bytes match authored assets. Packaged catalog Git blob **0e3a04beaa7bbda6f51e9de23eb5a81663210547** matches the exact repository projection; local materialized copy had only one extra final newline, not a source difference. Catalog 219 / policy 222 / source mappings 135.
- Final native Canvas renders inspected: magic explanation, missing/ready requirements, learned slot assignment, duplicate-slot cooldown, empty state. Required/current values, prices/owned counts and labels fit their panels; card names align and no cyan corner triangles remain in inspected icons. Render tests explicitly check transparent corner pixels. These are native automated renders, not physical screenshots or user visual acceptance.
- Status **IMPLEMENTED / BUILD_VERIFIED / AUTOMATED_INPUT_VERIFIED / NATIVE_RENDER_REVIEWED / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING**. Pending: physical-device UI/save/slot/cooldown acceptance; unsupported special/AOE/utility/stealth/advanced effects; missing or unresolved source icon/name rows; four unknown descriptions; offline cooldown persistence. Existing 29 adapted attacks/self-heals + basic weapon attack are the combat scope. Job acquisition route remains a separate game task and no longer blocks skill learning.

## 2026-09-30 — SKILL-WINDOW-05 모바일 스킬 흐름 / v61

- 사용자 “한번에 처리”는 앞선 출시 품질 검토의 아이콘/가독성/상태/설명/반응/미구현 결제/경제 경로 문제를 한 작업으로 수리하도록 승인했다. v60의 출시 품질 부족은 이번 검토 판단이며, v60에 대한 새 physical-device 실패 보고나 설치 SHA 확인으로 기록하지 않는다. 이전 v59 사용자 거절 이력은 보존한다.
- 기본 목록은 실제 효과가 있는 real-ID 30개(29 adapted 공격/자기회복 + 무기 기본공격). 219개 Master/135 source icon/222 policy와 기존 learned/slot save는 보존한다. 전체 자료 보기로 미구현 항목을 볼 수 있지만 습득/결제하지 못한다. 내부 fixture는 기본 목록에 없다. 직업·숙련·선행·승급/써클/레벨은 여전히 습득 조건이 아니다.
- 여섯 큰 카드, 기술/마법, 전체/습득 가능/배운 스킬, 분류 dropdown으로 조작을 줄였다. 카드 이름 14px/제목22px/설명14px, 36–40 logical px 버튼/46px 슬롯. 카드에 조건 부족·습득 가능·완료·등록 슬롯, 창 안 습득/등록/오류 피드백과 등록 슬롯 강조, 필드 사용 안내를 표시한다. 모바일 dp/손가락 수락은 기기 확인 전이다.
- 원본 source bytes를 유지하고 작은 source art는 40 logical px 이하로 표시한다. 공통 프레임과 cyan-corner mask, 원본 없는 supported ID의 project Canvas sword/fist/heal/attack emblems를 쓴다. 다른 스킬 이미지를 빌리지 않으며 이 emblems는 원작 아트가 아니다. 캡처의 낮은 해상도 자체를 복원했다고 주장하지 않는다.
- 효과/조건 설명은 실제 MP·쿨타임·대상·기본 위력/회복량 및 공격력/방어/속성 또는 회복량=base+FinalStats WIS 식을 안내한다. Heal adapter도 FinalStats WIS를 사용하도록 맞췄다. 습득 완료 후에는 소비한 재료/Gold를 부족 조건처럼 표시하지 않고 추가 결제/아이템이 없음을 표시한다.
- 도메인 ReagentPurchase를 새로 연결: 기존 멀린 counter의 쿠라눔에 ADAPTED **50G** 가격. 구매 버튼→Gold/items→checkpoint; 실패 시 전부 복원. 기존 다른 reagent/recall 가격은 unresolved이며 구매하지 않는다. Gold/보유량/구매 확인도 상점 안에 표시한다. 첫 기술50G=기존 훈련 보상25G 두 번; supported 쿠라눔 recipe는 학비150G+재료50G=200G(훈련8회 Gold 상당). 실제 플레이 시간/전체 경제 수락이 아닌 source-economy 기준 검사다.
- Runtime source **89bad5a620324f9e5b0b3c8fbc7191fe59fb2a1f**, PR [#172](https://github.com/ChanJoos/Project_dark_android/pull/172), base **fae835dee6d5475efb9db67f07da45e90b666be3**. Actions [36644434203](https://github.com/ChanJoos/Project_dark_android/actions/runs/36644434203) **SUCCESS**, job **109663993916**. Actual synthetic checkout **e481a9010979967c1c404979cca69408ddf8feda** merges source89bad5a into basefae835d. Earlier passing runs precede final feedback/calculation/material/use proof and are not the delivered artifact.
- All **16 SkillWindowTest** cases and configured Master/catalog/policy equality, HUD/world/target/combat/growth/quest/save/restart/render checks plus assembleDebug passed. Paid flow uses an explicit shop-context/stat/Gold fixture, then actual counter button→book learn→register→field HUD use→MP50→32 once/repeated input blocked→heal5→87→checkpoint/restart Gold/material/skill/slot restoration. This does not claim independent physical walking to the shop; the existing door route was not rewritten. Fresh COMMONER basic attack UI and the two-training-reward technique benchmark also pass. Unsupported/archive no-charge, save failures and old-ID preservation pass.
- versionCode **61**, versionName **0.61-mobile-skill-flow**, build **2026-09-30 08:19:43 KST**. APK artifact **11067706867**, native review **11067821705**; artifact names carry synthetic checkout e481a90. APK **13,070,503 bytes**, SHA-256 **32155516e6213d8804e8f5751563ea7fa34e5f1c43dfcad061c30a02ea4e2571**. Packaged policy/source atlas/mapping/captures match authored bytes; catalog Git blob **0e3a04beaa7bbda6f51e9de23eb5a81663210547** matches authoritative source.
- Native final overview, ready requirements, completed/registered conditions, actual shop purchase confirmation, shared cooldown reviewed: names/fields fit panels, completed costs no longer falsely demand resources, slot feedback visible. No subjective user/device visual acceptance is inferred.
- Status **IMPLEMENTED / BUILD_VERIFIED / AUTOMATED_INPUT_VERIFIED / NATIVE_RENDER_REVIEWED / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING**. Remaining gates: real phone touch/text/readability and shop walk/restart acceptance, play-session economy tuning, higher-resolution/exact missing original art. Special/AOE/utility/stealth/advanced effects remain unimplemented and are safely archived, not sold. This is the improved skill-flow build, not certification that the entire game is launch-ready.

> 확인된 기준과 미완료 작업만 적습니다. 시작할 때 최신 `main`과 활성 작업 브랜치를 다시 조회합니다.

## 저장소와 코드 기준 (이전 감사 기록)

- 저장소: [ChanJoos/Project_dark_android](https://github.com/ChanJoos/Project_dark_android)
- 이번 검토의 main 코드/에셋 기준: [`0506929143996c31e38ea07918bea83377ff50a3`](https://github.com/ChanJoos/Project_dark_android/commit/0506929143996c31e38ea07918bea83377ff50a3). 이후 main의 `2eca004`, `edf6a62`는 인수인계 문서 변경입니다.
- `README.md`의 v0.45 표기와 Gradle 앱 버전 표기가 일치하지 않습니다. APK 버전은 `app/build.gradle`의 versionCode/versionName을 기준으로 확인합니다.
- main SHA `0506929`의 포테 몬스터 v0.4 파일은 `assets/pote/review/...` 아래 검토용입니다. README는 14종의 대기/걷기/공격 포즈 자료라고 설명하며, APK 런타임 적용 자료라고 하지 않습니다.

## 2026-09-29 — v0.55 이후 Pamfet 방향 및 자동공격 보고

- 최초 검토 기준은 Main `3772848` (Pote 종료/사망 후 지도·이동 복구)이었으나, 작업 도중 Main이 `c2e60d6`로 전진해 최신 SHA 위에 후보를 다시 적용했습니다. 사용자는 포테에서 재실행해도 포테에 남는 동작을 확인했습니다.
- 사용자는 팜팻 일부가 이동 방향과 다른 걷기 포즈를 보이고, 자동공격이 빙글돌거나 가까운 몬스터를 택해 효율적인 경로로 공격하지 않는다고 보고했습니다. 이 보고는 새 이슈의 기기 증거이며, 이번 수정 전후의 전체 원인은 기기 재현 전까지 UNKNOWN입니다.
- Main 소스 감사에서 두 위험을 확인했습니다: 이동 렌더의 방향 정수화는 AI 의도와 충돌 우회로가 달라질 때 허용되지 않은 벡터도 방향으로 반올림할 수 있었고, 자동타깃은 도달 가능한 경로 수를 비교하지 않고 유클리드 거리만 사용했습니다. 움직이는 몬스터의 보간 중간점도 인접 타일 목표가 아니므로 접근 계획이 불안정할 수 있었습니다.
- v0.56 후보는 렌더 방향을 정확한 적용 타일 벡터에서만 고르고, 자동타깃을 합법 인접 타일까지의 최단 경로 수 우선으로 선택하며, 이동 중 몬스터의 예약 목적지를 경로 계획 기준으로 사용하도록 변경합니다. 팜팻 네 방향/스테일 공격 방향 잠금, 접근 경로 우선순위, 비도달 타깃 제외 테스트를 추가했습니다.
- 첫 Actions run `36586971014`는 몬스터 테스트 이전의 Master 감사에서 실패했습니다. 원인은 latest Main의 `Skill_Legacy_Requirements.csv` 미등록과 `Skill_Evidence.csv` 사용자 제공 SE10 추가분을 historical workbook hash로 계속 검사한 것이었습니다. 검증기에서 새 파일을 canonical addition으로 등록하고 amended evidence sheet를 기존 canonical override로 분류했습니다. 로컬 `python3 tools/validate_master.py`는 `AVAILABLE_CSV_INTEGRITY_PASS`로 통과했으며, 원본 XLSX fidelity는 여전히 미검증으로 표시됩니다. 수정 검증기의 Actions 재실행은 대기 중입니다.
- 후보 상태: **IMPLEMENTED / MASTER_GATE_REPAIRED_LOCALLY / CI_PENDING / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING**. 이번 작업의 새 SHA/Actions/APK 기록은 CI 결과 확인 뒤 갱신합니다. 팜팻 원본 아트의 실제 시선이 파일 방향명과 맞는지 실기기 시각 확인도 별도 필요합니다.

## 2026-09-30 — v0.56 기기 실패 보고 및 추적 경로 수정

- v0.56은 PR #166으로 Main에 병합됐다. 코드 SHA `ddc39fde33f2d37e54df35d23d88c0757591ef80`, merge SHA `b6d1c4b87e730c16ec974797205b340a41eac593`; 최신 Main은 후속 Master 문서/데이터 커밋 `50c65cc457227f447356335ba89391489bab41b9`이다.
- 사용자가 전달된 v0.56 APK에서 팜팻 이동 방향/포즈 불일치와 자동공격 중 회전·비효율 타깃 선택이 남았다고 보고했다. 기록: **DEVICE_FAILED (user-reported)**. 전달 APK SHA-256 `2fc04baed7c4d53942c129a91cd61a3864243cceb10cc55749361e1d112a2ec9`; 최초 CI run #1508 (`36588894898`) 성공, artifact `11043600170`.
- v0.56의 타깃 비용 선택과 예약 endpoint는 소스에 구현돼 있었지만, 테스트는 selector 단위 비교에 그쳤다. 현재 소스 감사에서 몬스터 추적은 매 걸음마다 플레이어 방향으로 greedy하게 향하고, 막히면 `MonsterDiagonalLocomotion.select`가 좌/우 우회 방향을 임시 적용한 뒤 다음 걸음에 원래 목표 방향을 다시 계산하는 점을 확인했다. 또 player route planning은 actor point occupancy를 검사했지만 live commit은 actor crossing을 추가 거부했다. 두 차이가 반복 BLOCKED/replan을 만들 수 있는 source risk다.
- 새 후보 r4는 몬스터를 authored tile graph에서 합법 melee 인접 타일까지의 BFS 최단 경로로 이동시키고, 매 타일 재계산 시 같은 최단 우회로를 유지한다. player path planner와 live commit도 동일한 full-edge traversal predicate를 사용한다. 직진 경로 차단/반복 재계산 회귀 테스트를 추가했다. 방향 아트의 실제 시선 정합은 이전 테스트가 검사하지 않았으므로 기기 원인은 여전히 **UNKNOWN**이며 별도 시각 검증 대상이다.
- Latest Main에는 이후 skill master 업데이트로 canonical override 3개와 행 폭이 맞지 않는 evidence rows가 더해져 기존 Master gate가 다시 실패했다. 현재 후보는 `Skill_Master`, `Skill_Requirements`, `Skill_Research_Audit`를 schema/header 검증 override로 등록하고 `Skill_Evidence`의 빈 행을 제거하고 기존 6-field 값을 27-column 스키마에 빈 값으로 맞췄다. 값은 보존했다. 로컬 `python3 tools/validate_master.py`는 `AVAILABLE_CSV_INTEGRITY_PASS`; YAML parse와 diff whitespace 검사도 통과했다.
- 후보 상태: **IMPLEMENTED / MASTER_AUDIT_PASS / LOCAL_BUILD_UNAVAILABLE (repository has no Gradle wrapper; system Gradle unavailable) / CI_PENDING / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING**. 새 APK를 검증된 해결본으로 전달하지 않는다. 다음 기준은 새 CI의 정확한 SHA 통과와, 사용자가 해당 후보를 기기에서 확인한 결과다.

## 최신 확인 대상

v0.57 후보의 CI 통과 후 팜팻 네 방향 이동/충돌 우회, 몬스터 장애물 우회 최단 경로, 자동공격 대상 경로·회전 현상을 기기에서 확인합니다. v0.55 포테 재시작/사망 복귀 통과 결과와 보상 미확정 원칙은 유지합니다.

## 마지막 전달 APK와 확인된 결함

- 사용자에게 전달된 파일: `PROJECT_DARK-debug-0506929143996c31e38ea07918bea83377ff50a3.apk`
- APK SHA-256: `e10e94495a8cd5dba2f86753f28d617835d42ecc2f4f0baf331c9fe671b62ef2`
- Actions [#36387002427](https://github.com/ChanJoos/Project_dark_android/actions/runs/36387002427), artifact ID `10955136379`, build source SHA `0506929`; 전달 파일과 artifact 내부 APK 바이트가 일치함을 확인했습니다.
- 사용자는 이 APK에서 기대한 최신 숲을 볼 수 없었고, 캐릭터도 움직이지 않았다고 보고했습니다. 해당 전달물은 **DEVICE_FAILED (user-reported)** 입니다.
- 소스 결함 확인: main `0506929`의 `PoteFieldDef.navigationTiles()`는 X 간격 64/Y 간격 32로 좌표를 만들지만, `WorldMoveTargetController`의 인접 이동은 32×16입니다. 격자 사이에 합법적인 이동 간선이 생기지 않아 포테 숲 내부 경로 이동이 불가능합니다. 기기에서 사용자 보고 전체를 재현한 것은 아니므로 밀레스 시작 화면 조작 실패 원인은 별도 미확정입니다.

## 활성 Pote 작업 브랜치

- 브랜치: [`codex/pote-ground-tile-foundation`](https://github.com/ChanJoos/Project_dark_android/tree/codex/pote-ground-tile-foundation)
- 구현 스냅샷 `5ebc78312a7203508daba77f093762f8da9507ed`는 비교 당시 main 코드 SHA `0506929`보다 84 commits 앞서고 2 commits 뒤에 있어 **분기·미병합** 상태였습니다.
- 이 구현은 이동 격자를 32×16으로 고치고, 숲 입구·중앙 길·다리 너머 경로 연결 검사를 `PoteForestSpatialGrammarTest`에 추가했습니다. `WorldDef`에서는 숲 안내인을 시작점 가까이 배치하고, 도착점을 중앙 트레일로 옮겼습니다.
- `app/build.gradle`을 versionCode 48, versionName `0.48-pote-forest-candidate`로 바꾼 브랜치 HEAD는 [`16c6d9a92f4a01315c9cb46c1cb52cbe46185cfa`](https://github.com/ChanJoos/Project_dark_android/commit/16c6d9a92f4a01315c9cb46c1cb52cbe46185cfa)입니다.
- Actions [#36388750145](https://github.com/ChanJoos/Project_dark_android/actions/runs/36388750145)는 해당 SHA에서 성공했습니다. Android workflow는 `PoteVisualReviewTest`, `PoteForestSpatialGrammarTest`, `PoteMonsterPlayableRuntimeTest`, `PotePamfetPresentationTest`, `PoteCreekWaterAssetTest`를 실행하고 APK를 빌드했습니다.
- CI device-review artifact에는 포테 입구·중앙 공터·다리 화면이 있습니다. 이는 코드 렌더링 확인이며 실제 Android 기기 조작 확인이 아닙니다.
- APK 후보 artifact ID `10955795355`, 이름 `PROJECT_DARK-debug-16c6d9a92f4a01315c9cb46c1cb52cbe46185cfa`; 압축 artifact 안 `app-debug.apk`의 SHA-256은 `03f4ccae5fe0804fa78bbeaef974696c0064b3d2941bb190b8fc68d3e64e3e4f`입니다.
- 이 후보는 main에 병합되지 않았습니다. 포테 숲 코드의 CI/경로 검증은 통과했고, 실제 기기에서 시작 이동·안내인 진입·숲 내부 이동을 확인하는 작업이 남아 있습니다.

## 다음 작업

먼저 새 수정 후보 artifact `10957720462` (source `8ca5fda1c2483589b6f16fc9800bb9f87597ec23`, APK SHA-256 `263a502075f438048feb0191550cf59cee8f9a424b41ffdf02605146d337c359`)를 기기에서 실행해 크기·강력한 놀 외형·16종 조작 및 실제 접근/공격 시점을 검증합니다. 앞선 version 48 후보는 낡은 화면 피드백의 원 APK가 특정되지 않아 재사용하지 않습니다. 이후 `DELIVERY-01`의 시작 이동·안내인 숲 진입·숲 내부 이동 시나리오를 이어갑니다. 기기 검증 전에는 시각 수락 완료로 부르지 않습니다.

## Pote forest full roster pose test — current task

- User request (2026-09-28): place every monster defined for Pote Forest so its directional look, movement, and attack pose can be tested.
- Evidence baseline checked: main `fdc9f5e` review pack README/manifest documents 14 concept forms × 12 representative still poses = 168 PNGs; `master/data/Monster_Master.csv` via `PoteMonsterRoster.java` projects 16 Pote identities. This art is explicitly concept/review art, not original extracted animation.
- Active source branch remains `codex/pote-ground-tile-foundation`, based on candidate v0.48 SHA `16c6d9a`; this task is not on main. Current work adds a 16-actor adapted field showcase, selects pose image by runtime AI state/facing, and copies all 168 PNGs from the reviewed pack into the packaged test asset location. Three strong forms share base concept art; the canonical spirit uses `POTE_SPIRIT_TEST_B` to keep the test HP/reward isolated. These are prototype fixture mappings, not canonical spawn/art claims.
- 소스 HEAD `cd88c67657f2d249db490fc897b348ea940197f3`에서 Actions [#36391291190](https://github.com/ChanJoos/Project_dark_android/actions/runs/36391291190) **성공**. 이 run의 workflow는 앱 compile, Pote forest review/render tests, `PoteForestSpatialGrammarTest`, `PoteMonsterPlayableRuntimeTest`, 확장된 `PotePamfetPresentationTest`, `PoteCreekWaterAssetTest`, 나머지 repo gate를 모두 통과하고 APK를 생성했습니다. 앞선 두 CI 실패는 stale package 참조와 테스트의 `assertSame` import 누락으로 재현·수정되었습니다.
- 같은 SHA의 device-review artifact ID `10956695994`에는 포테 입구/중앙 공터/다리/동쪽 둔치 캡처가 있습니다. 캡처는 런타임 렌더 검토 자료이며 실제 Android 기기 실행이나 `VISUAL_ACCEPTED`는 아닙니다.
- APK candidate: versionCode 48 / `0.48-pote-forest-candidate`, artifact ID `10956323696`, artifact `PROJECT_DARK-debug-cd88c67657f2d249db490fc897b348ea940197f3`, APK SHA-256 `52d64a81a890e1ee3ec54ad0aee910c55e9788eee32f3ad25a1ba40294c7fb74`. ZIP 내부 APK에 `monster_test_v04/manifest.csv`와 168 PNG가 확인되었습니다. CI의 새 테스트는 16개 서로 다른 walkable/tappable placement와 각 test ID의 12 state/facing 리소스(총 192 renderer loads)를 확인합니다. 공유 AI/combat 동작의 실행 회귀는 기존 `PoteMonsterPlayableRuntimeTest`에서 퍼플팜팻의 chase/4방향 공격을 확인합니다. 다른 15개별 개체의 이동/공격 동작은 실제 플레이어 조작으로 각각 확인한 상태가 아닙니다.
- 상태: **IMPLEMENTED, BUILD_VERIFIED, DEVICE_PENDING, VISUAL_ACCEPTED_PENDING**. APK는 미병합 후보 브랜치에서 나온 명시적 테스트 후보이며 수락 완료/수정 완료 APK라고 부르지 않습니다. 실제 기기에서 종별 선택·이동·방향·공격 포즈를 확인하는 것이 다음 작업입니다.
- Previous delivery remains DEVICE_FAILED (user-reported) at APK SHA `e10e94495a8cd5dba2f86753f28d617835d42ecc2f4f0baf331c9fe671b62ef2`; it is distinct from this test candidate.

### Next result

Run the exact candidate APK on an Android device: select each of the 16 displayed identities and record idle/walk/attack pose, facing changes, successful input and crashes. Then run `DELIVERY-01` start movement, forest-guide entry and in-forest route checks. Record device model/Android version if available, scenario results, and whether any per-monster visual mappings need revision; keep acceptance pending until evidence exists.

## 2026-09-28 — 사용자 화면 피드백 / 크기·공격 포즈 수정

- 사용자는 전달 APK를 예전 버전이라고 지적했고, 첨부 화면에서 몬스터가 플레이어보다 너무 크며 공격 포즈가 어색하고 접근 전에 공격하는 것처럼 보인다고 보고했습니다. 강력한 놀 그림도 깨져 보인다고 했습니다.
- 해당 기기에 설치된 APK SHA는 사용자가 제공하지 않았으므로, 이 보고를 앞서 기록된 `cd88c676...` 후보 APK에 대한 확정 재현으로 연결하지 않습니다. 보고 자체는 사용자 화면의 시각 결함 증거로 보존합니다.
- 소스 확인: `PoteFieldRenderer.drawMonsterTestPose()`는 384×384 원본 셀 전체를 112×112 논리 픽셀로 확대/표시하고, 공격 포즈에 추가 돌진·상하 이동·회전을 적용했습니다. `MonsterAIController`의 추적 시작 범위는 180이며 실제 근접 공격은 `CanonicalMeleeTileContract.reachable()` 경로에서만 시작합니다. 첨부 화면의 시각적 거리와 실제 전투 판정이 같은 사건인지는 재현되지 않았습니다.
- 사용자 요청에 맞춰 수정한 코드: commit [`8ca5fda`](https://github.com/ChanJoos/Project_dark_android/commit/8ca5fda1c2483589b6f16fc9800bb9f87597ec23). 테스트용 PNG의 투명 여백을 제거하고 실루엣 높이를 44 논리 픽셀로 통일하며 비율을 유지합니다. 공격 표시에서 추가 돌진·회전을 제거하고 원본 대표 공격 포즈만 표시합니다. Pote 테스트 화면의 이름/HP 표시도 축소된 실루엣 위로 겹치지 않게 조정했습니다.
- 새 회귀 테스트는 192개 ID/상태/방향 렌더 결과의 불투명 실루엣 높이가 48 논리 픽셀 이하인지, 추적 한 걸음 중 공격 windup이 시작되지 않는지 확인합니다. 로컬 Gradle wrapper가 없어 테스트는 여기서 실행하지 못했습니다.
- 강력한 놀의 별도 원본 그림은 v0.4 pack에 없으며 현재 GNOLL 기본 concept 그림을 공유합니다. 이번 수정은 확대 표시를 줄였지만, 별도 강력한 놀 그림을 만들거나 정사 외형으로 취급하지 않았습니다. “깨짐” 개선과 전체 비율 수락은 새 APK의 기기 재검증 전까지 미확정입니다.
- 상태: 수정 소스 **BUILD_VERIFIED** at `8ca5fda1c2483589b6f16fc9800bb9f87597ec23`; Android Actions [#36393396473](https://github.com/ChanJoos/Project_dark_android/actions/runs/36393396473) 성공. APK artifact ID `10957720462`, 이름 `PROJECT_DARK-debug-8ca5fda1c2483589b6f16fc9800bb9f87597ec23`, 내부 APK SHA-256 `263a502075f438048feb0191550cf59cee8f9a424b41ffdf02605146d337c359`; packaged monster concept PNG 168개 확인. 새 regression: roster/state/facing 192 렌더 실루엣 높이 ≤48 logical px 및 추적 시작 tick에서 공격 windup 미시작. 작업 브랜치는 미병합 후보입니다. **DEVICE_PENDING / VISUAL_ACCEPTED_PENDING**이며, CI 렌더 캡처에는 전체 몬스터 장면이 없어 기기 화면 확인을 대체하지 않습니다.

### 다음 결과

1. 새 artifact `10957720462`를 전달하고, 같은 기기에서 크기·강력한 놀 선명도·네 방향 walk/attack·실제 접근/피격 시점을 다시 확인합니다.
2. 재검증 후에도 공격 시작이 너무 이른지 실제 거리/인접 tile을 분리해 기록합니다. 추적 반경 조정은 현재 제안 상태이며 새 수치가 확정된 것으로 기록하지 않습니다.
3. `DELIVERY-01`의 시작 이동·안내인 진입·숲 내부 경로를 검증하고 브랜치 통합 판정을 별도 기록합니다.


## 2026-09-29 — 개별 몬스터 이미지 초안

- 사용자는 몬스터 시트가 아니라 개별 이미지로 만들라고 명확히 했다. 개별 파일 기준은 몬스터당 정지 4방향 + 공격 4방향 + 이동 4방향, 총 12개이며 제작 대상은 강력한 3종을 제외한 13종이다.
- 이번에 독립 PNG 3장을 생성했다: 팜팻 구르기 공격(보라색 변형), 기본 직립 늑대인간 대기, 갑옷·칼을 든 직립 늑대인간 대기. 입력 참조는 사용자가 마지막에 유지하라고 한 팜팻 외형(Library libfile_d213c79680108191b4f13b8bc89b68de) 및 사용자가 늑대인간/원작 장면으로 제공한 이미지 2장(Library libfile_1c5a4161d2b08191be701bf0f89b463f, libfile_f65c74247704819180f7e8bfa212fee3)이다. 기존 168장 콘셉트나 반려 시트는 사용하지 않았다.
- 결과는 AI 생성 컨셉 초안일 뿐 원작 추출물/사용자 수락/게임 적용이 아니다. 팜팻 보라색은 이번 샘플 선택이며 색상 확정이 아니다. 늑대 이미지와 POTE 몬스터 ID 대응도 미정이다.
- 현재 상태: **3 INDIVIDUAL DRAFTS GENERATED / VISUAL_ACCEPTANCE_PENDING / APP_UNCHANGED / APK_NOT_BUILT**. 나머지 종·방향·동작 이미지 제작과 방향/크기/원작 분위기 검수는 미완료다.


### 이미지 자체 검수 추가

- 첫 생성 이후 팜팻 외곽의 보라색 광휘와 갑옷 늑대의 칼 과장을 발견해 각 이미지를 다시 생성/수정했다. 무장 늑대 수정본은 칼을 줄였지만 AI 생성안이며 사용자 수락은 미정이다.
- 최신 개별 검토 파일: Pamfet roll `exec-5195fb43-3077-4d9b-a10e-2863f40a446f.png` (SHA-256 `c0197daf5a7b7ddb8cb478a85284a20da547ce0e5cfd79ca28fc965a6d70b5b1`); basic werewolf idle `exec-06318ec5-970e-4f20-82c1-c9ad8744cbd9.png` (SHA-256 `6e0599e6b61dfcca8ed8253ffe0df20bd04990604ea35809aaced48562ffedc6`); armored sword werewolf idle `exec-b490a73d-8f3f-40b2-bc2f-c8cdde5d844c.png` (SHA-256 `13db8ab947647a49109bdde18e01aa837eeeac030d5a24aed89ad8ed15ce62ca`).
- Pamfet 이미지에는 지시 후에도 보라색 후광이 남아 있어 이 부분은 미통과/수락 대기다. 기본·무장 늑대도 원작과의 크기·팔레트·장식 차이를 사용자 검토 전 확정하지 않는다. 어떤 이미지도 앱 리소스에 반영하지 않았다.


## 2026-09-29 — 사용자 지적에 따른 제작 범위·확인 경로 정정

- 활성 브랜치의 `PoteMonsterRoster.java`를 재조회해 강력한 3종을 제외한 13개 identity를 확인했다: 레드/그린/퍼플/실버팜팻, 트렌트, 앤트라이온, 놀, 울프라이더, 라이칸스로프, 앤트자이언트, 은빛늑대, 포테의정령, 자이언트맨티스. 요구량은 13 × 12 = 156개 **개별 이미지**다. 앞서 세 장만 전달한 것은 전체 범위를 임의로 좁힌 오류이며, 완료가 아니다. v0.4 폴더의 14개 concept form/168 PNG 또는 앱 테스트의 16 identity 숫자와 새 그림의 13종을 혼동하지 않는다.
- 사용자 제공 `1000057373.png`만 참고해 팜팻 SE 대기 초안을 4색 각각 생성했다. 레드 `exec-a763baa2-fbcf-4d35-bf72-b6f69ab099e2.png`, 그린 `exec-93ef1015-2202-46f0-a95f-af60cb23d003.png`, 퍼플 `exec-733c2a16-f1ca-4a53-b5ea-46d1820e0be1.png`, 실버 `exec-c54d888a-24f3-4088-a286-e6f4db4b2c67.png`. 실버 후광 제거 재시도 `exec-c0352775-1235-4fec-923d-8dfa439e2dda.png`도 흐린 후광이 남아 **시각 QA 반려**. 레드·그린·퍼플은 개별 화면 표시/투명 PNG 기본 확인만 했으며 원작 유사성·사용자 수락·게임 적용은 미확정이다.
- 앞서 만든 보라 팜팻 구르기도 후광으로 QA 반려. 늑대인간 2장은 사용자 스크린샷의 기본형/갑옷·칼 대비를 보려는 초안이며 정확한 `POTE_GNOLL/WOLFRIDER/LYCAN` 배정은 근거가 없어 미정이다. 검수되지 않은 그림을 종별 완료 수에 넣지 않는다.
- GitHub 활성 브랜치의 `assets/pote/reference`에는 `PAMFET_8DIRECTION_DRAFT_V3.jpg`만 확인됐다. `master/data/Monster_Master.csv`의 출처 URL과 과거 검토용 168 PNG는 있으나, 이전에 언급된 `assets/pote/reference/original_web_2026-09-28/` 및 `docs/POTE_MONSTER_ORIGINAL_REFERENCES.md`는 이 브랜치에서 확인되지 않았다. 별도 원작 스크린샷·라벨 자료가 GitHub에 모두 보관됐다고 간주하지 않는다.
- 이번 결과는 이 대화에서 이미지가 직접 표시됐으며 각 파일은 클릭 가능한 개별 PNG로 전달한다. **13종 전체 완료 0 / 156장 제작·검수 완료 0 / 앱 변경 없음 / 새 APK 없음**. 다음에는 원작 스크린샷의 종별 대응과 자료 보관 위치를 확보한 뒤, 각 identity의 네 방향 정지→이동→공격을 개별 PNG로 만들고 방향/팔레트/전신/투명 가장자리를 검사한다. 사용자 수락 전 `VISUAL_ACCEPTED`로 표기하지 않는다.


## 2026-09-29 — 신규 생성 몬스터 후보 런타임 등록 (현재 작업)

- **사용자 결정:** 기존 16 identity에 연결한 이전 `monster_test_v04` 이미지를 게임 런타임에서 해제하고, 현재 생성된 이미지로 idle/walk/attack 방향 포즈를 APK에 반영합니다. 이는 테스트 후보 적용 승인입니다. 신규 그림 전부의 시각 수락/원작 정사 승인은 아닙니다.
- **확인한 기반:** 현재 Pote 숲 구현은 원격 `codex/pote-ground-tile-foundation` HEAD `133f2388706b1948e63c7b931fb46c4a442c8ff2`에 있습니다. 문서 전용 main 스냅샷은 APK 소스로 사용하지 않습니다. 새 작업 브랜치 `codex/pote-monster-sprites-runtime-20260929`에서 구현합니다.
- **구현 대상:** 기존 생성 후보 중 완전한 12장 세트가 존재하는 퍼플/레드/그린/실버 팜팻과 라이칸스로프, 5종 × 12 = 60장입니다. 개별 48×48 RGBA 프레임으로 변환했고, 실루엣 높이는 최대 30px, 바닥 접점은 공통 하단 앵커로 정렬했습니다. AI가 idle/chase-walk/attack 상태와 NW/NE/SW/SE 방향을 골라 대표 포즈를 바꿉니다. 각 상태는 단일 still PNG로서 다중 프레임 루프는 아닙니다.
- **미등록:** 트랜트는 공격 NW 한 장만 있는 9장 부분 세트라 제외했습니다. 나머지 미완성 종/방향과 강력한 변형도 제외하고 누락 방향을 추측해 만들지 않습니다. 기존 16 test mapping은 5종 완전 세트로 대체됩니다.
- **기존 파일 처리:** 이전 v0.4 이미지는 Android production assets의 `monster_test_v04` 경로에서 제거합니다. 기존 review 자료는 보존하되 Android production asset source set에서 로드하지 않습니다. 신규 원본 후보 69장과 출처 해시는 `assets/pote/review/monster_rebuild_v1/`에 저장합니다.
- **문서 상태:** 작업 소스 구현은 있으나 아직 commit/CI 전입니다. `versionCode 49`, `0.49-pote-monster-art-candidate`를 사용합니다. 다음은 60방향/상태 렌더 QA, APK 내부 기존 경로 제거 및 신규 PNG 수 확인, 정확한 SHA의 GitHub Actions 빌드입니다. 이후 APK SHA와 artifact를 기록합니다.
- **수락 상태:** `IMPLEMENTED` 후보 코드 기준; `BUILD_VERIFIED=PENDING`, `DEVICE_VERIFIED=PENDING`, 신규 팜팻 `VISUAL_ACCEPTED=PENDING`. 라이칸스로프만 앞서 사용자가 시각 수락했고 이번 게임 내 크기/앵커 확인은 남았습니다.


### 완료 결과 — 런타임 후보 APK (2026-09-29)

- GitHub Actions run `36534954426` 성공, source SHA `1def7a92aeef68ce144ebaad8b94f02dee011a12`; `PotePamfetPresentationTest` 포함 전체 workflow 통과.
- APK versionCode 49 / `0.49-pote-monster-art-candidate`, artifact `11017989115` (`PROJECT_DARK-debug-1def7a92aeef68ce144ebaad8b94f02dee011a12`), APK SHA-256 `9b8cb7e86c7b9c89b34667199711b1738f72aa9267b6edbb79743c934b18c799`. 대화에 전달한 다운로드 파일은 이 APK다.
- APK 내부 검사: 새 경로 포즈 PNG 60개 포함, `monster_test_v04` 경로 0개. CI에서 60개 포즈 렌더/불투명 픽셀/표시 높이, 5종 타일 배치·탭 테스트 통과.
- 사용자 기기에서 직접 실행/시각 검수는 아직 안 됨. 테스트 빌드에는 다중 프레임 루프가 없고, 5종의 12개 방향/상태 still만 AI 상태 선택으로 전환한다. 13종 전체 이미지 완료는 아님.
- 다음: APK를 기기에 설치해 크기·방향·이동·접근 후 공격 시점을 확인하고 결과를 기록한다. 눈으로 확인하지 않은 결과는 device-verified로 표기하지 않는다.


## 2026-09-29 — 사용자 런타임 검수 피드백과 수정 중

- 사용자가 version 49 APK에서 퍼플팜팻 한 마리만 보이고 걷기/공격 포즈가 나타나지 않으며, 이동 중 바라보는 방향이 맞지 않고 공격이 8방향처럼 보인다고 보고했다. 직전 APK는 전달 후 실사용 기준 **미통과**다.
- 소스 재검토에서 5개 세트의 등록은 되어 있었지만 배치 알고리즘이 입구 근처 퍼플 외 4마리를 지도 전체로 분산해 초기 화면/180px 추적권 밖에 두었다. 걷기 이미지는 단일 still이라 반복 움직임이 없었고, shared combat hit 경로는 `attackPrimed`를 해제한 뒤 `GameView`가 공격 포즈 대신 idle을 선택했다. 공격 facing lock도 종료시점이 없어 이동 facing에 남을 수 있었다.
- 수정 중: 다섯 후보를 입구 주변 72~168px ring에 배치, actor별 walk gait/공격 짧은 lunge 및 animation phase 추가, shared-hit 공격 포즈 표시/4방향 facing lock 해제. 캐릭터 방향 enum과 맵 이동/근접 계약은 네 대각 방향(NW/NE/SW/SE)으로 한정한다.
- 새 테스트는 다섯 종 각각의 이동 실제 step과 facing 일치, idle/walk/attack 12 리소스, shared-hit 공격 포즈, 공격 lock 종료, 다섯 몬스터 입구 가시영역 배치를 검사한다. 다음은 CI 실행과 APK 패키지 검사이며, 기기 시각 검수 전 성공 처리하지 않는다.


## 2026-09-29 — v50 Pote monster runtime follow-up

User-reported v49 failures (only Purple visible, walk/attack absent, facing mismatch, attack appearing eight-directional) were addressed on the candidate branch. Exact source SHA `3cd69850c16a61629e4c1c80a9683cb6f4fc46e3`, versionCode 50 / `0.50-pote-monster-runtime-fix`; branch compare says 103 ahead and 5 behind `main`, so it remains an unmerged candidate.

Actions run [#36549350583](https://github.com/ChanJoos/Project_dark_android/actions/runs/36549350583) succeeded at the source SHA: Pote spatial grammar, playable monster runtime, 60-pose presentation, creek assets, and `assembleDebug` passed. APK artifact [11024127131](https://github.com/ChanJoos/Project_dark_android/actions/runs/36549350583) contains `app-debug.apk`, 12,631,372 bytes, SHA-256 `807437bf4b5270a2701c2380e854eeb032018117952336f1b0e1f3bb516961d4`. Independent inventory found 60 new runtime PNGs and zero `monster_test_v04/` files. Build time was 2026-09-29 18:29:57 KST.

Runtime code now positions the five complete candidates in the entrance pocket, couples the walk pose/facing to applied movement, shows walk and attack motion using the authored four diagonal directions, and keeps/ends attack facing through the visual hit window. This is **IMPLEMENTED and BUILD_VERIFIED**. The reported fixes have not been checked on the user's Android device: **DEVICE_PENDING / VISUAL_ACCEPTED_PENDING**. Next action is to install this exact APK and check five-monster visibility, direction/facing, movement, attack timing/range, and stability. The generated poses remain single stills per state/direction, not multi-frame animation.

## 2026-09-29 — POTE actor overlap and movement-facing follow-up

The user reported that the character and monsters, and monsters with each other, can overlap in the delivered v0.52 APK; the walk pose also occasionally faces away from its movement direction. This is recorded as **DEVICE_FAILED (user-reported)** for v0.52 (source d7627d7abe7f8574f0557da37afe81a30225e2ed, APK SHA-256 6a35937f55f5fe84603593f808e7954e404ca9d14ccac756c50a9c0aa9c2ce63). The report has not been independently reproduced on the user's handset; final root cause remains **UNKNOWN**.

Source audit found two concrete risks: actor collision checks considered other monsters' current positions but not reserved in-flight destinations or swept movement paths, and Pote rendering read the facing presentation lock instead of deriving walk direction from the committed movement vector. VersionCode 53 addresses both: species-aware actor clearance plus destination/swept-path checks; Pote walk rendering now uses the actual start-to-target vector and keeps WALK priority while moving.

Exact implementation source SHA: 7f6944e42888a1bd4c5a43e1b4e6261b0f9c2320. GitHub Actions run #36565408459 passed, including Pote spatial/runtime/presentation checks and APK build. Artifact 11030999456 contains app-debug.apk; extracted APK SHA-256: a1cccce1d52b172172355063ca0f653be648251f1210a6b0e20d758fce2a27bb. Build time: 2026-09-29 21:03 KST.

Status: **IMPLEMENTED / BUILD_VERIFIED / DEVICE_PENDING / VISUAL_ACCEPTED_PENDING**. The added regressions check concurrent destination reservation, crossing-path clearance, attempted entry onto the player's occupied tile, and walk facing despite a stale attack-facing lock. CI rendering is not physical-device verification. Next acceptance step: install this exact APK and verify actor spacing, movement-facing, and attack approach on the user's device.


## 2026-09-29 — v0.53 자동전투 접근 중 겹침 후속

사용자는 v0.53에서 자동전투로 몬스터를 공격할 때 플레이어와 몬스터가 가끔 겹친다고 보고했다. 기기에서 독립 재현은 아직 없어 최종 기기 원인은 계속 **UNKNOWN**이며, 이 보고는 **DEVICE_FAILED (user-reported)** 로 기록한다.

소스 감사에서 자동 접근 계획 시점과 실제 한 타일 이동 시작 시점의 충돌 검사 차이를 확인했다. 실제 이동 시작 검사가 몬스터의 현재 좌표만 보고 이동 중 예약 목적지와 전체 이동 경로를 확인하지 않아, 몬스터가 접근 도중 플레이어의 예정 위치로 들어올 수 있었다. v0.54는 World 어댑터의 점/경로 검사를 RuntimeState의 종별 충돌 규칙으로 통일하고 이동 시작 시 예약 목적지 및 스윕 경로를 다시 검사한다. 회귀 테스트 automaticApproachCannotEnterAMonsterReservedTile에서 예약 타일 진입이 차단되는 것을 검증한다.

구현 소스 SHA 5905e963de7898acec4b84644e78fcb98cec395e; GitHub Actions [#36570339135](https://github.com/ChanJoos/Project_dark_android/actions/runs/36570339135) 성공. APK artifact 11033662723, APK SHA-256 97b892b9afd8afb82c888db1ed13305f565e3072300900e26ffbf5ec848f2fb4. 몬스터 생성/등록/크기/방향/충돌/이동·공격 템포/검증 절차는 [docs/MONSTER_CREATION_MANUAL.md](MONSTER_CREATION_MANUAL.md)에 문서화했고 AGENTS.md와 docs/CHAT_HANDOFF.md에서 필독하도록 연결했다.

상태: **IMPLEMENTED / BUILD_VERIFIED / DEVICE_PENDING / VISUAL_ACCEPTED_PENDING**. 자동 접근 예약 목적지 회귀 및 전체 CI는 통과했지만 사용자의 기기에서 이 APK로 자동전투를 직접 재검증하기 전까지 기기 해결 완료로 간주하지 않는다.


## 2026-09-29 — 사용자 기기 통과 보고 및 다음 세로 루프 작업

사용자가 앞서 제안한 세 항목을 통과했다고 확인했다: (1) v0.54 자동전투 중 플레이어/몬스터 간격, (2) 전투 판정·이동 연계, (3) 플레이어 BODY+로브+무기 공격 표현. 이는 **사용자 보고 DEVICE PASS**로 기록한다. 이 보고에서 기기 모델·별도 녹화와 2·3번의 APK SHA는 제공되지 않았다. 무기 손 결합과 피격 가독성은 사용자가 후순위로 명시했다. 몬스터 아트 전체의 `VISUAL_ACCEPTED`를 뜻하지 않는다.

다음은 First RPG Loop의 cold-restart 복원 경로다. Main의 `F5mSaveStore`는 map id/좌표, HP/MP, 퀘스트, 인벤토리·장비, EXP/Gold·레벨·스탯·보상 sequence 저장/복원 코드를 갖고 있고 Robolectric persistence matrix가 일부 RPG/퀘스트 데이터를 검사한다. 그러나 `GameView`는 매번 Milles `RuntimeState`로 시작하며 저장된 `map_id`에 맞춰 활성 맵과 World adapter를 재구성하지 않는다. 따라서 Pote 진입 중 강제 종료 후 같은 맵·위치로 돌아오는 cold-start 경로는 현재 테스트에서 검증되지 않았다. 이는 소스 감사에서 확인한 통합 공백이며 기기 재현 보고는 아니다.

다음 패키지 제안: `F5M_RESTART_RESTORE_001` — 저장된 Milles/Pote map id에 맞는 world/runtime/adapter를 초기화한 뒤 안전한 이동 타일 위치, HP/MP, 퀘스트/목표, 소지품·장비, 진행도와 reward sequence를 복원한다. Milles와 Pote에서 프로세스를 종료·재실행하는 테스트, 손상/구버전 저장 fallback, 중복 처치/퀘스트 보상 방지를 검증하고, 정확한 APK에서 실제 재실행까지 확인한다.

### 사용자 보고: 포테 재시작·사망 및 보상 저장 확인 어려움 (2026-09-29)

사용자는 포테에서 앱을 종료하고 다시 열면 밀레스로 돌아오며, 포테에서 사망 후 중앙에서 부활해도 움직이지 않고, 포테 몬스터가 EXP나 장비를 주지 않아 저장 여부를 확인하기 어렵다고 보고했다. 이는 기기 보고이며 v0.55 수정이 해당 기기에서 확인된 것은 아니다.

Main 소스 감사에서 재시작 때 저장 `map_id`와 무관하게 Milles 런타임만 초기화하던 점, Pote 사망·부활 처리에서 Milles 어댑터에만 카메라/제어 정리를 하던 점을 찾았다. 수정은 저장된 Pote 맵으로 런타임·어댑터를 구성한 다음 안전한 위치를 복원하고, Pote 입장 전 Milles 복귀 위치를 저장한다. 사망/부활 때 현재 맵 어댑터, 카메라, 자동공격·조이스틱 상태를 초기화한다. Robolectric 테스트에서 Pote 콜드 재시작, 알 수 없는 맵 저장 보존, 부활 뒤 조이스틱 이동을 확인했다. 첫 Actions 실행에서 두 신규 assertion이 실패해 위치 캡처 및 이동 가능한 인접 타일 선택을 바로잡았고, 수정 SHA `0bbb6ee354af2e67e4513db7e5bb7e643c310052`의 전체 Actions run `36579231684` (#1490)가 모든 단계와 APK 빌드를 통과했다. APK artifact 11038421617의 내부 APK SHA-256은 `b79e1c58742f006cecc167a6e66f16b6ba9501d8d4a70d506a790d149aeb603a`다. 이는 BUILD VERIFIED이며 사용자 기기 확인은 아직 pending이다.

## 2026-09-30 — 스킬창 구현

- Base `cd3dd871`: Master 투영(219 INCLUDE + 기존 B 시험 동작 3), 기술/마법·습득 필터, 16칸 페이지, 설명/조건/구 자료 상세, 슬롯 1~8 등록·해제. 원작 아이콘 미확보는 기술/마법 텍스트 타일로 표시.
- GameView utility 4번째 버튼은 스킬창. 기존 퀘스트 카드 접근 유지. 모달은 입력만 소비하며 world tick을 정지하지 않는다.
- 새 SkillBook의 습득/숙련도/슬롯은 schema 3 체크포인트에 저장. 평민은 미습득 상태; 임의 지급 없음. 기존 3 B 동작만 shared Resolver로 실행 가능; 원작 219개 효과는 구현 예정.
- 자동 테스트: catalog identity, 미습득 차단, checkpoint/restart, invalid ID 원본 보존, shared combat repeat-input, modal/render. CI_PENDING / DEVICE_PENDING / VISUAL_PENDING.


## 2026-09-30 — SKILL-WINDOW-01 build verification

- PR #168 runtime source: `74c3833e0010e7f0c78e01a70b0c8cafc0ade614`; base `cd3dd871fc81dcc88053ffff5305fd6fdeddc065`, ahead 4 / behind 0 at verification.
- Actions run [36622542956](https://github.com/ChanJoos/Project_dark_android/actions/runs/36622542956), job 109591297347: SUCCESS. Checkout actually built synthetic PR merge `5560ccc69e990d08b44172a9aa538815e64a29f3` (head 74c3833e into base cd3dd871). Do not identify this as a head-only checkout.
- Passed: Master validation, regenerated catalog equality, SkillWindowTest (catalog / unlearned gates / skill checkpoint restore / invalid snapshot protection / shared Resolver repeated input / world tick behind modal), restart persistence matrix, RuntimeCheckpointTest legacy migration and all configured regression steps, assembleDebug.
- APK artifact `11059261609`, 12,666,152 APK bytes, APK SHA-256 `aa1eac489ac840e293a73619e821491288dc53d8e2a4b482cbbe2b0925400d92`. Build completed 2026-09-30 04:56:45 KST. ZIP digest is distinct from APK hash.
- Inspected packaged `assets/skills/catalog.json`: 219 Master records. SkillBook adds three B fixtures at runtime. Inspected `skill-window.png` from render artifact `11059746241`: frame, tabs, grid, details and disabled unlearned controls render within the viewport. This is automated native rendering, not physical-device acceptance.
- Status: IMPLEMENTED / BUILD_VERIFIED / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING. Starting commoner receives no implicit skills; acquisition NPC/service, original combat effects and source icons remain pending. Consequently normal fresh saves cannot yet use/register skills; those paths are exercised with explicitly learned B fixtures in automated tests.
- Next: user/device acceptance for opening the fourth utility button, technique/magic and learned/all filters, pages/details, old-save preservation and restart; then the separately authorized acquisition/effects/artwork implementation.


## 2026-09-30 — SKILL-WINDOW-02 source art and presentation repair

User rejected PR #168 APK (`aa1eac489ac840e293a73619e821491288dc53d8e2a4b482cbbe2b0925400d92`) as mockup-like and visually poor. Record VISUAL_REJECTED (user-reported); prior build success is not visual acceptance. Functional/device root cause is not claimed from this report.

Candidate `codex/skill-window-source-art-20260930`, based on main `1d8b2118a357483803f8b98668f5000d53aee0d5`, v0.58. Replaces Chinese type tiles with 75 exact job/name icon crops from the five user-provided screenshots; crop coordinates, original SHA-256 and unmatched names are recorded in `skills/source_icons.json`. Unmatched and duplicate source rows are not assigned by resemblance. Source images are V reference artwork, not verified official originals. No other skill borrows a pictured icon.

Reworks the frame, typography and opaque panels; adds six job filters and separate effect/requirements/reference tabs, paged long descriptions, selected highlight and a visible eight-slot rail. Keeps original acquisition/learned checks and shared Resolver; fresh commoners still cannot acquire or use original skills because acquisition/effects are outside this visual repair. Do not call this a playable original-skills release.

Verification pending: exact-SHA Actions, direct fourth-utility reachability, source-ID rendering, job-filter/modal/slot interactions, save/restart regressions, native screenshots for overview/rogue/cleric/empty states. Physical device and user visual acceptance remain pending.


### SKILL-WINDOW-02 final build evidence

- Runtime head `9fb4e3d77f144ef30a4fd342e544b955a369a8c1`; Actions run [36626946163](https://github.com/ChanJoos/Project_dark_android/actions/runs/36626946163) SUCCESS, job `109606255393`. Actual checkout is PR merge `29bf3cee1fba673aee0be77225ce16089b14fde7` into base `1d8b2118a357483803f8b98668f5000d53aee0d5`.
- v0.58 / versionCode 58, APK artifact `11060023417`, build finished 2026-09-30 05:33:12 KST. Extracted APK 12,880,982 bytes; SHA-256 `a7e2e666db282f34218a86f34a72faf606799544d4df5ccae000e676e6c072c2`. Packaged catalog is 219 records, source icon mappings are 75; packaged atlas bytes match authored atlas exactly.
- Passed all configured CI checks, including direct utility reachability, exact source icon loading, job selection, explicit detail tabs, learned/empty state, shared Resolver quick-slot use/repeated-input gates, world tick behind modal, checkpoint restore, malformed-save protection and legacy migration; assembleDebug passed.
- Render artifact `11060203281`: inspected overview, rogue, cleric and empty-state renders. Final source removes repeated missing-art placeholders, renders missing-art entries as name/job cards, uses a full-width missing-icon detail title, and removes dummy grids under empty-state text. No text/panel overlaps observed in inspected 960x540 native renders. Physical Android device interaction and user visual acceptance remain PENDING.
- Status: IMPLEMENTED / BUILD_VERIFIED / NATIVE_RENDER_REVIEWED / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING. Previous PR #168 remains visually rejected by the user; do not erase that report. This repairs the window presentation only; original acquisition NPC/service and combat effects remain unimplemented, and most original icons still require source material. No original skills are silently granted to commoners.

## 2026-09-30 — SKILL-WINDOW-03 습득·퀵슬롯·쿨타임

- 사용자 검수: v58에서 습득 경로/원작 효과/퀵슬롯 연결이 빠졌고 설명 본문을 누르면 바뀐다는 지적. 전달된 v58 검수의 기능 미수락으로 기록한다. 실제 설치된 APK SHA는 독립 확인하지 않았으므로 기기 원인/판정 범위를 추정하지 않는다. 소스 감사에서는 Master 219행의 runtime=null, 습득 호출 부재, runtime 있는 B fixture만 등록 허용, 본문 탭의 detailOffset 증가를 확인했다.
- 사용자 선택 **스크린샷 조건 적용**에 따라 직업·능력치·선행 숙련도를 확인하는 SkillAcquisition을 연결했다. 새 캡처 무도가 조건 12행, 전사 드래곤모드의 두 선행 90/90, 공통 유즈스태프 INT6/WIS3 전사 오류를 source hash와 함께 별도 acquisition_captures.json에 보존했다. Master의 219행/원본 출처는 그대로 유지한다. 조건 미확정/승급/5서클 재료·이벤트는 차단한다.
- 11개 사용자 캡처에서 정확히 매칭한 아이콘 135개 (이전 75개). 10개 캡처 행은 명칭 불일치로 미등록이며 유사 아이콘을 빌리지 않는다. 모든 원본 hash와 crop bounds를 검사했다.
- 실제 흐름: 공통 필터→스킬 선택→습득→8개 퀵슬롯 등록/동일 항목 해제→전투 HUD 사용→체크포인트/복원. 창 안 슬롯 선택은 설명 선택만 한다. 등록할 때 스킬이 실행되지 않는다. 설명 본문 tap으로 페이지/내용이 바뀌지 않는다; 명시적 효과/조건 탭 및 bounded up/down 버튼만 반응한다.
- starting COMMONER의 기본공격은 캡처 3/3/3/3/3 조건으로 직접 습득 가능하며, 슬롯에서 기존 장착 무기 기본공격 경로를 호출한다. 기존 공격 버튼과 action-ID 쿨타임을 공유해 별도 피해/재사용 우회를 만들지 않는다. 실제 utility/common-filter/cell/learn/register/HUD touch 경로를 automated test로 검증했다.
- 29개 단일 공격/자기회복 액션을 real ID 아래 명시적인 B/ADAPTED 시험 수치로 shared Resolver에 연결했다. 기술 MP0, 마법 MP차감, 자기회복/maxHP cap, 액션 ID별 쿨타임, 중복 슬롯 재사용 차단/만료, 거부 입력 MP 무차감. 성공한 플레이어 액션마다 숙련 1 증가/100 cap은 ADAPTED. 원작 속성/특수/광역/그룹/은신/승급 효과와 모든 219개 효과를 구현했다고 하지 않는다.
- Runtime source **30cb18baa2268c60067bd3f6d33e921b935154f8**, based on main **7329840e7f0458cd315409f2e43b0a8bec375a27**. PR [#170](https://github.com/ChanJoos/Project_dark_android/pull/170). Actions [36631412453](https://github.com/ChanJoos/Project_dark_android/actions/runs/36631412453) SUCCESS; job 109621294136; checkout is PR merge **8f8491fb1e5387dd3f4484849f42861df90601a2**. All configured checks and assembleDebug passed.
- v0.59 / versionCode59, APK artifact **11061669352**, render artifact **11061664240**. Build completed **2026-09-30 06:11:53 KST**. APK 13,057,259 bytes, SHA-256 **4e47fc87e67321598dfe1b253f1b57b2597b0382fb765dbf78d23c9af5391bac**. Packaged atlas/icon/acquisition bytes exactly match authored files; packaged catalog Git blob **0e3a04beaa7bbda6f51e9de23eb5a81663210547** matches current Master projection, 219 rows; 135 icon mappings.
- Native 960x540 commoner-learned and cooldown frames inspected: seven filters/common route, learned/register controls, icon rail, visible total/remaining timer, no text/panel overlaps observed. This is automated native rendering, not physical-device acceptance. Martial-artist/class acquisition tests inject job state; a fresh normal save cannot yet enter those jobs because the profession route is incomplete.
- Status: **IMPLEMENTED / BUILD_VERIFIED / AUTOMATED_INPUT_VERIFIED / NATIVE_RENDER_REVIEWED / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING**. Remaining: real profession route; missing material/event/circle conditions and source-name resolutions; unsupported effects and original balance; persisted offline cooldown policy; physical-device/UI acceptance. Do not label this APK as the completed original-skills game.


## 2026-09-30 — SKILL-WINDOW-06 v62 preferred grid restoration

User prefers v60's 4×3 grid over v61's six-card layout. Restore centered icon/name cells, seven direct job tabs and compact description/requirements frame. Preserve v61's 30 supported actions, unsupported no-charge archive, stat/Gold/material atomic learning, Curanum purchase, FinalStats healing, slot checkpoint rollback and shared cooldown. Production-input tests follow the restored coordinates. Source af63d7d24e3734700cfd414ddac65f96b543ac2c; PR #173; Actions 36667530858 SUCCESS. Physical device and user visual acceptance pending. Previous v61 layout is user-rejected, not a new combat failure report.

Final verification: Actions 36667530858 / job109735322078 SUCCESS, source af63d7d24e3734700cfd414ddac65f96b543ac2c; actual synthetic merge checkout cf46ea11259232588035483637872f0a7d6501bd into main48037b694b5a034e5851c1a240b61cd05dc81682. All 16 SkillWindowTest cases and configured regressions/assembleDebug passed. APK artifact11076641254, versionCode62, built2026-09-30 13:10:54 KST, 13,069,271 bytes, SHA25626e5da74bc6e743fd3decfd9719cde101983f3ae2573ef0201dc32a362a49229. Packaged learning policy/icons/captures match retained source bytes. Render artifact11077080799 overview/shop-learned/cooldown reviewed: centered grid labels, direct tabs, completed costs, slot feedback and shared cooldown visible. Physical-device and user visual acceptance remain pending. Closure commit changes documentation only.


## 2026-09-30 — SKILL-PRESENTATION-01 v63

User-authorized source male/female pose decisions and separate caster/recipient VFX. 219 ID mappings/268 unchanged source frames, newly generated ADAPTED atlases. Current30 combat actions connected; archived189 do not become playable. Missing garment-group motions use existing standing layer with explicit adapted registration. No finished sex-selection UI. Exact original FX mapping count0; web search/reference gaps and source/status distinction in SKILL_PRESENTATION_CONTRACT.md. Build/runtime evidence follows; physical-device/user acceptance pending.


Verification: implementation source ee643167680d83de47cc57a073610df4bb137ed7; PR #174; Actions36682111983/job109779630434 SUCCESS. Actual PR checkout1b377c71cc18d3d2f4482b885dc15d8a7dc8d7bd merges that source into main8525060cee3fec3c4fa8ab3d60bb02d8d0f19b60. Catalog regeneration, 4 SkillPresentationTest cases, 16 SkillWindowTest cases and all configured regressions/assembleDebug passed. Body source268/268 byte equality verified. APK artifact11082039016 (uploaded2026-09-30 16:11:59 KST), versionCode63, 18,693,002 bytes, SHA25619ce62df1a6f4629ae7a99b4219c718f86a2910e078fd60d594e07797ccac542. All273 packaged presentation files match retained source bytes. Native render artifact11082541202 reviewed: both genders/four directions/seven pose families, monster fire/blunt impact and player heal anchors visible. Static garment registration and original source semantics remain ADAPTED/UNRESOLVED; no full visual acceptance claimed. Physical-device/user acceptance pending. Closure changes only documentation and an additional pre-impact caster capture fixture; app runtime/assets stay identical to the verified implementation source.


## 2026-09-30 — SKILL-TEST-02 v64

User requests beginner testing of every skill and questions cross-job availability. Supersedes the previous stats/Gold-only cross-job permission: normal learning/use and default available list now enforce current job plus common skills. Old learned entries remain in save but cannot bypass job. Activity starts test mode ON by default (saved toggle honored). 219 real entries are virtually learned/test-accessible; separate test quick slots persist outside the normal book, leaving actual learned/proficiency/slots untouched. Test combat30 uses existing Resolver with MP cost refunded; other189 are explicitly visual-only previews with no invented damage/status/teleport/summon mechanics. All magic uses original f1 rear/f3 front overhead two-arm charge/contact, f0/f2 recovery, both sexes. UI distinguishes test combat support from visual-only and has ON/OFF. No Master numeric original claim. CI/native/physical-device evidence pending.


Verified implementation76422fa364a6936234b7c6260714a07e64d27988; PR175; Actions36691264703/job109808785633 SUCCESS. PR checkout6d1d51aab131248c2a2c9cef4948075b6d28b18b merges implementation into main89c49b39ab44ca4106a4a91b6723b45681b4b748. Four new SkillTestModeTest cases, four SkillPresentationTest cases, 16 SkillWindowTest cases and all configured regressions/assembleDebug passed. Tests cover real Activity default ON and restart OFF, all219 virtual learned/usable entries, normal job rejection, save isolation, MP0 supported magic, every unsupported mage/cleric spell and real slot edit/restart. Native artifact11085513953 reviewed: cleric59 entries/test label, poison caster/recipient anchors and shared overhead source pose. APK artifact11085469099 uploaded2026-09-30 17:43:37 KST; versionCode64; 18,695,978 bytes; SHA2567043d8bb59416b62afdf8a7c8cbe5f225d575bb45d9762bf78727bb33f11c052. All273 packaged presentation assets match verified source bytes. Implemented/build/native-input runtime verified; physical-device and user visual acceptance pending. All219 are test accessible, not219 fully implemented mechanics: existing30 real combat; remaining189 explicitly preview-only. Closure documentation changes no runtime/APK bytes.


## 2026-09-30 — SKILL-SPATIAL-03 v65 candidate

User rejects v64 all-class motion/effect/range behavior: DEVICE_FAILED / VISUAL_REJECTED (user-reported). Code inspection reproduced absent per-skill tile rules, preview bypass and passive/finisher alias errors; source exact original visuals remain unverified. Current task is all-class original research and actual-path correction, branch `codex/original-skill-contract-v65`, base bfd668d4e178fa82625d634b5a54be0e27ce30a3. See SKILL_SPATIAL_CONTRACT.md and219 reviewed SKILL_ACTION_DECISIONS.csv. Shared Resolver now owns test presentation legality and area recipients; expanded72 offense/16 heal plus equipped basic paths, reconstructed finisher shapes, source kick extensions and passive/basic-link distinctions. Persistent status, travel/landing and exact original effect/finisher-pose evidence remain incomplete. Candidate implementation; exact-SHA CI/native verification pending. Do not describe219 original skills as fully implemented or source-authenticated.


## 2026-09-30 — SKILL-SPATIAL-03 v65 verification resumed

- IMPLEMENTED: source `fd3e33176859956780c4fd752d8d1381c18280a7`, branch `codex/original-skill-contract-v65`, PR #176, base main `bfd668d4e178fa82625d634b5a54be0e27ce30a3`. This remains an unmerged candidate. Compared the active source with current main; the 30-file delta contains this task. All219 reviewed IDs have per-ID spatial/presentation decisions;72 damage and16 heal definitions use adapted numeric balance.131 entries have presentation-only mechanics, including gated interactions/passives/unresolved entries, and are not fully implemented abilities.
- BUILD_VERIFIED: push Actions run [36702916604](https://github.com/ChanJoos/Project_dark_android/actions/runs/36702916604), job109846298749, checkout/source `fd3e33176859956780c4fd752d8d1381c18280a7` succeeded. Regeneration, SkillWindowTest, SkillPresentationTest, SkillTestModeTest, SkillSpatialContractTest, configured combat/world/save/monster regressions and assembleDebug passed. The PR check run36702922238 also passed; the delivered artifact below is from the exact-source push run, not the synthetic PR merge.
- NATIVE_RUNTIME_VERIFIED (automated scope): native GameView input/Resolver tests cover all-class recipient masks, range/LOS/viewport checks at start/contact, moved-target cancellation, passive/utility gates, linked basic tiers and per-recipient VFX deduplication. Downloaded rendering artifact11090284176 and inspected male/female four-direction pose sheets plus Warrior Crasher/DevilCrasher, Rogue assassination, Martial Dara, Mage Meteor and Cleric HolyDragon production screenshots. Impact screenshots prove these fixture frames only, not every animation frame or physical-device behavior.
- APK: artifact11090074775, versionCode65 / versionName `0.65-source-skill-contract`; built2026-09-30 19:33:56 KST (APK artifact file timestamp; upload19:33:57 KST),20523070 bytes, SHA256 `3e2b1c7bd81a09333696c8658351bafe5f73d95be00e4a925cf7a8c72d5ee5ca`. All274 packaged presentation files match verified source bytes. All268 packaged male/female BODY frames match `master/assets/animation_frames/body` byte-for-byte;219 catalog decisions present.
- DEVICE_PENDING / VISUAL_ACCEPTED_PENDING: original pixel-effect matches remain0. Source-described/reconstructed effects, adapted balance and selected source poses are not authenticated original per-skill animation. Persistent buffs/poison/sleep/recognition reset, travel/landing, summons, exact finisher poses/reaches, Pacheongak/Climens/Dinosense and gender equipment motion gaps remain open. Further targeted web searches did not supply usable exact finisher reach/motion or Pacheongak mask evidence; no original fact was promoted from a search snippet.
- Next work: reproduce the reported motion/range scenarios on this exact candidate APK; resolve individual original finisher pose/range and remaining unknown masks from identifiable combat video/source frames; then implement persistent status/travel/summon services through existing domain modules. Do not mark all219 mechanics complete or physical-device/original visual acceptance passed. This closure changes documentation only and does not rebuild or relabel old runtime bytes as a new version.


## 2026-09-30 — v66 quick-slot approach follow-up (implementation pending CI)

User reports v65 quick slots reject out-of-range use and rejects adapted VFX as unlike the original (DEVICE_FAILED / VISUAL_REJECTED, user-reported; exact installed SHA unconfirmed). Continue PR176 branch; main remains bfd668d4e178fa82625d634b5a54be0e27ce30a3.

- Added one-shot SkillApproachController intent wired to quick slots in Milles/Pote. World BFS finds shortest reachable legal range/LOS/viewport tile, including retreat for minimum-range skills; tracks moving target reserved tile. MP/cooldown is charged only by Resolver on submission, once. Direct input, target change/death, player death, map change, pause or test-toggle cancels.
- Added SkillQuickslotApproachTest for native quick-slot input, one-shot charge/hit, minimum-range retreat, cancellation, blocked route/four directions and moving-target replanning. Generator and Master validation pass locally. Android tests/build/device verification remain PENDING until exact CI evidence.
- Complete defense now uses a thin blue shell reconstructed from supplied combat video. Crasher/DevilCrasher use shared blue/red procedural vortex instead of v65 generic generated columns. These are still ADAPTED, not extracted/authenticated original frames; all219 original VFX request remains OPEN. Existing elemental/status atlases and other generated finishers have NOT been promoted to original.
- Owner: Director/Integration. Next: run exact-source CI, diagnose failures, review native runtime captures; obtain identifiable original skill effect frames and replace remaining adaptations. No full original visual completion or physical-device success claimed.


## 2026-09-30 — v67 original combat capture source (CI pending)

User supplied Naver cafe13434008/article401229 and six GIFs; replaces prior insufficient-search conclusions. Original blog https://m.blog.naver.com/180921/221777831873 publicly provides nine labelled GIFs. Downloaded all9 through the page-observed w800 CDN URLs, retaining bytes and hashes in master/source/skill_fx/naver_401229/provenance.json. This is FAN_GAME_CAPTURE, not an authenticated native game archive or current server numeric canon.

CapturedSkillFx routes nine exact skill IDs: Crasher, MadSoul, DevilCrasher, MadSoulJin, Assassination, AssassinationJin, Dara, Guyang and Dalma. 112 real capture frames preserve nonuniform GIF frame times (one cycle, excluding idle recording waits). Extractor removes floor and captured actor/text; SCREEN alpha and pivot/scale are explicit matting adaptations. Occluded/clipped effect pixels stay missing, not generated. Old procedural/generated recipient effects and generic caster pulses are disabled for these9. Other210 IDs do not become original effects by this change. Male/female selected BODY bytes and actual combat rules stay unchanged.

Validation pending: CapturedSkillFxTest checks9 ID mappings, source/atlas SHA hashes, nonuniform timing, transparency, actual GameView submission/contact/recipient-only emission/expiry and9 production screenshots. Existing quick-slot full-frame and combat/save/world regressions remain enabled. Exact-source CI/APK/native visual review required; physical device and all219 original visual acceptance remain pending.

Capture anchoring: source GIFs show the effect around the caster, so captured contact pulses follow the player foot anchor and deduplicate per action (including multi-recipient hits). Transparent actor masks align to that foot; the source does not supply effect pixels hidden by the actor. Runtime hit recipients and damage remain separate.


## 2026-09-30 — v68 classic martial/cleric source candidate

Continue PR176/codex/original-skill-contract-v65 from308bc751. Fully read61 labelled classic rows (27 martial/34 cleric); retained132 source media/56 GIFs. Adds missing martial IDs033/034; catalog221, Benusti stays excluded.55 captured effect IDs /85 caster/contact channels,60 source icon bindings, corrected source descriptions/stats/kinds and ally/group/self rules. Original BODY268 bytes preserved. See CLASSIC_MARTIAL_CLERIC_V68.md and source provenance. IMPLEMENTED_CANDIDATE; exact-source build/native review/device/visual acceptance PENDING. Persistent status, travel, summon, party and finisher formulas remain unimplemented; this is not61 complete mechanics. v67 prior run36715127755 SUCCESS but its legacy-graphics captured screenshots were black; corrected native-render test now required.
# 2026-09-30 — User Naver skill sources / shared Kuroto icon follow-up

- User provided eleven Naver Cafe articles covering Warrior circles/promotion (191105, 191110, 191114, 191241, 191445, 191777), finisher effects (401229), Martial Artist (415456), Cleric magic (416044), Rogue techniques (245450) and Rogue magic (245456). This session could not retrieve the Cafe bodies: CUA reported no attached browser target, and the web open/search tools returned an inaccessible/robots-blocked result. Treat article details as NOT_READ; do not infer skill rules from titles.
- Implemented an additional skill-window source icon for `SK_공통_010` 쿠로토 from screenshot `11-Screenshot_20260929_233743_NAVER.jpg`, crop `[21,991,61,1030]`, source SHA-256 `df05de86e6d048a95fa5cf493cab7bcd83c8cd77e83314028e44152a5094416e`. Screenshot rows under 전사 and 도적 both show the same exact name, description (“자신체력 회복”) and visually matching icon; they resolve to the existing common catalog skill. The audit records both rows, one registered and one duplicate, with explicit mapping basis. No Master catalog fact or unrelated icon was inferred.
- Updated the screenshot icon extractor, source-icon metadata/atlas, skill-art README and `SkillWindowTest` to cover the shared ID. Local Python verification passed: 136 IDs point into the 221-entry catalog, screenshot hash/crop reproduces atlas cell 135, and shared-row status is consistent. Exact source commit `1c3deb056021b88d4d81a439f1b52241793786a1` failed Actions run [36732130899](https://github.com/ChanJoos/Project_dark_android/actions/runs/36732130899) at `:app:testDebugUnitTest`: 40 tests completed, 29 failed; `assembleDebug` was skipped. Diagnostic run [36733585836](https://github.com/ChanJoos/Project_dark_android/actions/runs/36733585836) exposed the main cascade: Robolectric could not open tracked Korean-named capture/icon assets (`ShadowArscAssetManager10.nativeOpenAsset`). The screenshot icon change was not the cause. The same report exposed a utility-mode gate mismatch and one stale classic-presentation expectation; fixes are included with the deterministic ASCII asset-path migration. Local importer verification passes twice with identical classic tree SHA-256 `c0fc3d0d864f495defc4437df34c1af710a53cc028f2f359c74d20dc587b5d0f`, 55 captured IDs/85 channels and 145 existing ASCII paths. Exact-source Actions verification remains pending. No APK has been produced. This checkout has no Gradle wrapper and no system Gradle.
- The source article review, remaining unregistered icon rows, missing icon art, and requested skill descriptions/motions/effects remain OPEN until article data can be read. Recounting source_icons and classic references covers 140/221 catalog IDs; 81 IDs still have no source icon. Current branch `codex/original-skill-contract-v65`, PR #176 is unmerged; physical-device and visual acceptance remain pending.


## 2026-10-01 — Offline Cafe collection checkpoint (supersedes prior access status)

User explicitly permits Cafe collection and future revisits as needed; thorough offline preservation is the objective. Nine of eleven requested article bodies are now retained under master/source/skill_fx/naver_cafe_archive_20261001 (all Warrior circles/promotion, finisher, Martial and Cleric). All225 observed media URLs are retained as213 unique byte files, including65 GIFs;86 new binaries, remaining references reuse tracked identical source bytes. Hashes, source URLs, image indices, frame counts/durations and observed CDN resolution policy are recorded. Offline evidence index contains95 labelled sections/table rows and2 explicit source conflicts; no unresolved historical/current formula was silently made canonical.

IMPLEMENTED / SOURCE_RETAINED: archive, offline media verifier and evidence index generators. Runtime candidate keeps existing classic/icon/capture implementation; this archive does not accept new Master mechanics. Original icon coverage140/221,81 gaps; BODY, persistent mechanics and full original VFX remain open.

BUILD_VERIFIED: complete Actions run36776004466 SUCCESS on exact runtime/test source4f8bec32c2d8d8c54d87c919c2a884317c39c0b5. Corrections align caster assertions with frame event drain and Kurus with CLASSIC_CAPTURE. DEVICE_PENDING / VISUAL_ACCEPTED_PENDING remain. Source checkpoint makes no new APK delivery claim.

BLOCKED: Rogue245450/245456 redirect to nid.naver.com login. Automatic approval review denied authentication-origin access despite authorized Cafe collection. Do not route around the denial. Next: request explicit Naver login procedure authorization, use secure authentication handoff, retain both bodies/media, then close remaining source icon mappings and accepted Master/BODY/VFX/UI changes through project generators and exact-source verification. PR176 remains unmerged on codex/original-skill-contract-v65; main unchanged.


## 2026-10-01 — Cafe archive collection complete / source analysis resumed

Continuation from PR176 head b3e7f5dd0795b1fb53e1c068596c70c772553803 on codex/original-skill-contract-v65; compared against main bfd668d4e178fa82625d634b5a54be0e27ce30a3. Existing authenticated browser session now exposes both Rogue245450/245456; prior authentication blocker is resolved without login-origin interaction or bypass.

SOURCE_RETAINED: all11 requested bodies,350 image references/288 distinct URLs/276 unique exact-byte files/96 GIFs. OFFLINE_MEDIA_INTEGRITY_PASS288/288; source index133 sections/table rows and8 conflicts. Added a sanitized local-only11-article HTML reader, original Rogue text/HTML snapshots, DOM-ordered GIF candidates, and six proposed Rogue magic-kind corrections. Raw commenter/account/profile data are not retained; all comment pages are not claimed complete. Offline collection/index/viewer and Master integrity checks pass.

This batch does not modify runtime/assets, accept the proposed kind changes, replace the81 missing icons or implement persistent status/travel/summon/user services. Previous runtime CI36776004466 at4f8bec32 still governs the prior implementation; it does not verify new source tooling. No new APK delivery/device/original visual acceptance claim.

NEXT: accept/test six source-backed Rogue magic classifications through catalog/presentation generators; visually disambiguate the31 GIF candidates (especially 습격 vs 기습 and combined basic/double/triple demonstration); bind verified caster/recipient channels and source icons; then implement persistent mechanics through existing domain modules. Requesting another Cafe login is no longer the immediate task. Evidence: master/source/skill_fx/naver_cafe_archive_20261001/{manifest.json,rogue_evidence.json,ROGUE_ANALYSIS.md,index.html}; master/changes/ROGUE-2015-SOURCE-REVIEW.json(PROPOSED).


## 2026-10-01 — Rogue source integration V69 (verification pending)

Accepted six source-backed Rogue magic kinds through supplemental change and catalog generator; selected original overhead BODY frames for both sexes/four facings. Retained 22 labelled icon crops, nine separately masked recipient/self channels and six explicit actor/utility-only no-particle entries. Generic particles are suppressed for those reviewed demonstrations. Existing labelled finisher sources remain in use. Source masks, pivots and timing are explicit project choices; no persistent mechanics or original visual acceptance are inferred. Corrected Hider description to another-user stealth and clarified unavailable test utility labels.

Verify RogueSkillReferenceTest alongside existing skill UI, learning/save, spatial/quickslot and native rendering regressions. Current implementation needs CI/APK evidence before delivery. Remaining: uncertain/repeated icons, combined or occluded GIFs, persistent status/stealth/trap/group/travel/summon/user services, and on-device visual acceptance. Source review boundaries are in rogue_bindings.json; do not call every one of 221 skills implemented.

Rogue follow-up: recovered three additional source channels (아무네지아, 연막탄터뜨리기, 백슬래쉬), total12;22 cropped icons;147/221 catalog icons. Player-only 밀기/적갑옷해체/하이더 are gated pending a user-target service. Smoke test targets visible screen enemies only; its persistent blind status is pending. Per-ID evidence/mechanic gaps are reproducible with tools/audit_skill_source_coverage.py.
