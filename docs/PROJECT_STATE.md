# PROJECT DARK 현재 상태

> 확인된 기준과 미완료 작업만 적습니다. 시작할 때 최신 `main`과 활성 작업 브랜치를 다시 조회합니다.

## 포테 몬스터 원작 참조 리빌드 (2026-09-29)

- 이번 작업 시작 시점의 원격 `main`과 작업 브랜치 base: `fdc9f5e28599501a530070a97d4c0670f3c390e4`.
- 사용자가 이전 몬스터 v0.4의 168개 생성물을 새 그림의 참고로 쓰지 말라고 명시했습니다. 해당 묶음은 계속 검토용/폐기 대상으로 취급하며 새 리빌드의 시각 참조에서 제외합니다.
- 사용자 확정 요구: 강력한 변형은 빼고, 포테의 숲 일반 몬스터/색상형을 대상으로 각 12개의 개별 이미지(대기 NW/NE/SW/SE, 걷기 NW/NE/SW/SE, 공격 NW/NE/SW/SE)를 만듭니다.
- 확인된 roster는 14개 일반 종/색상형입니다. `monster_concepts_v0.4/README.md`의 14개 목록에서 강력한 변형 3개는 포함되지 않습니다.
- 브랜치 [`codex/lycanthrope-reference-grounded-assets`](https://github.com/ChanJoos/Project_dark_android/tree/codex/lycanthrope-reference-grounded-assets), 후보 HEAD `b52696913225a0b5736b8eaeeaf0640d023d6b97`. 라이칸스로프 후보 12개를 `assets/pote/review/monster_rebuild_v1/`에 추가했습니다. 사용자 제공 원작 스크린샷 `1000057388.jpg`, `1000057384.jpg`를 시각 참조로 사용했습니다. 이전 168개는 사용하지 않았습니다.
- 각 파일은 투명 배경 64×64 PNG 한 장에 몬스터 한 마리/포즈 하나를 담습니다. 개수·크기·alpha 채널 검사는 통과했고, 전신 잘림은 contact QA에서 관찰되지 않았습니다. 대기/걷기/공격 자세 구분도 확인했습니다.
- 상태: **CANDIDATE / 사용자 시각 수락 대기**. 네 방향 중 원본 캡처에서 확인되지 않는 방향과 걷기·공격은 생성 포즈 후보이며 원작 sprite 추출물이 아닙니다. 게임 배율·발 앵커·원작 방향 정합성은 미검증이고 APK에는 적용하지 않았습니다.
- 남은 작업: 나머지 13개 종/색상형의 시각 근거를 종별로 확인한 뒤 같은 12개 개별 이미지 기준으로 제작·검수합니다. 원본 근거를 확보하지 못한 외형은 원작 정답처럼 단정하지 않습니다.

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

`docs/DIRECTOR_BACKLOG.md`의 `DELIVERY-01`을 진행합니다. 위 후보 APK의 정확한 SHA를 기준으로 세 시나리오를 기기 검증하고 결과를 기록합니다. 검증 전에는 포테 런타임을 기기 수락 완료로 부르지 않습니다. 브랜치 통합은 별도 변경 검토 후 결정합니다.
