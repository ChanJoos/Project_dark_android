# PROJECT DARK 현재 상태

> 확인된 기준과 미완료 작업만 적습니다. 시작할 때 최신 `main`과 활성 작업 브랜치를 다시 조회합니다.

## 저장소와 코드 기준

- 저장소: [ChanJoos/Project_dark_android](https://github.com/ChanJoos/Project_dark_android)
- 이번 검토의 main 코드/에셋 기준: [`0506929143996c31e38ea07918bea83377ff50a3`](https://github.com/ChanJoos/Project_dark_android/commit/0506929143996c31e38ea07918bea83377ff50a3). 이후 main의 `2eca004`, `edf6a62`는 인수인계 문서 변경입니다.
- `README.md`의 v0.45 표기와 Gradle 앱 버전 표기가 일치하지 않습니다. APK 버전은 `app/build.gradle`의 versionCode/versionName을 기준으로 확인합니다.
- main SHA `0506929`의 포테 몬스터 v0.4 파일은 `assets/pote/review/...` 아래 검토용입니다. README는 14종의 대기/걷기/공격 포즈 자료라고 설명하며, APK 런타임 적용 자료라고 하지 않습니다.

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
