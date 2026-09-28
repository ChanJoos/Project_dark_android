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

먼저 `POTE-TEST-01`의 새 APK를 기기에서 실행해 16종 타깃 선택 및 방향별 걷기/공격 포즈를 검증합니다. 이 결과 후 `DELIVERY-01`의 시작 이동·안내인 진입·숲 내부 이동 시나리오와 브랜치 통합 검토를 이어갑니다. 기기 검증 전에는 수락 완료로 부르지 않습니다.

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
