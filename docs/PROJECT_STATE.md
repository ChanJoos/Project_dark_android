# PROJECT DARK 현재 상태

> 확인된 기준과 미완료 작업만 적습니다. 시작할 때 최신 `main`과 활성 작업 브랜치를 다시 조회합니다.

## 포테 몬스터 원작 참조 리빌드 (2026-09-29)

- 이번 작업 시작 시점의 원격 `main`과 작업 브랜치 base: `fdc9f5e28599501a530070a97d4c0670f3c390e4`.
- 사용자가 이전 몬스터 v0.4의 168개 생성물을 새 그림의 참고로 쓰지 말라고 명시했습니다. 해당 묶음은 계속 검토용/폐기 대상으로 취급하며 새 리빌드의 시각 참조에서 제외합니다.
- 사용자 확정 요구: 강력한 변형은 빼고, 포테의 숲 일반 몬스터/색상형을 대상으로 각 12개의 개별 이미지(대기 NW/NE/SW/SE, 걷기 NW/NE/SW/SE, 공격 NW/NE/SW/SE)를 만듭니다.
- 확인된 roster는 14개 일반 종/색상형입니다. `monster_concepts_v0.4/README.md`의 14개 목록에서 강력한 변형 3개는 포함되지 않습니다.
- 원격 작업 브랜치 [`codex/lycanthrope-reference-grounded-assets`](https://github.com/ChanJoos/Project_dark_android/tree/codex/lycanthrope-reference-grounded-assets) HEAD는 점검 시 `72803f9`였습니다. 브랜치에는 폐기 대상으로 지정한 v0.4 168장과 별도로 라이칸스로프 새 후보 12장이 있습니다. 라이칸스로프 12장의 시각 결과는 사용자가 수락했지만, 발 앵커·실제 게임 배율·원작 4방향 정합성은 미검증입니다.
- 각 파일은 투명 배경 64×64 PNG 한 장에 몬스터 한 마리/포즈 하나를 담습니다. 개수·크기·alpha 채널 검사는 통과했고, 전신 잘림은 contact QA에서 관찰되지 않았습니다. 대기/걷기/공격 자세 구분도 확인했습니다.
- 상태: **사용자 시각 승인**. 사용자가 “좋았어. 이제 나머지도 다 해서 완성본 만들어”라고 응답했습니다. 단, 실제 게임 배율·발 앵커·원작 방향 정합성은 여전히 별도 기술 검수 대상입니다. 네 방향 중 원본 캡처에서 확인되지 않는 방향과 걷기·공격은 생성 포즈 후보이며 원작 sprite 추출물이 아닙니다. 게임 배율·발 앵커·원작 방향 정합성은 미검증이고 APK에는 적용하지 않았습니다.
- 재접속 후 확인: 현재 체크아웃에는 승인된 라이칸스로프 12개만 새 리빌드 산출물로 추적되어 있습니다. 직전 실행에서 보고한 120개는 현 저장소에 없어 산출물 수량에서 제외하고 12개로 정정했습니다. 남은 13종/색상형 × 12장 = 156개입니다. 새 168행 추적표는 [`monster_artwork_manifest.csv`](../assets/pote/review/monster_rebuild_v1/monster_artwork_manifest.csv)입니다.
- 2026-09-29 새로 제공된 `01-PDF`, `02-_-.PDF`, 두 화면 녹화 영상을 확인했습니다. PDF는 몬스터 스프라이트 참조를 추가하지 않았고, 영상 프레임에는 포테 환경은 있지만 종별로 식별 가능한 sprite/motion을 확인하지 못했습니다.
- GitHub 원격 관련 브랜치도 조회했습니다. `codex/lycanthrope-reference-grounded-assets`에는 제외 대상 v0.4 168장과 별도 라이칸스로프 12장이 있습니다. `codex/pote-latest-pamfet-npc-2026-09-27`에는 보라 팜팻 prototype idle 4/roll attack 4가 있으나 생성·수정 에셋이며 walk는 없습니다. 사용자의 168장 폐기 지시 때문에 해당 파일을 새 작업의 입력 또는 완료 수량에 쓰지 않습니다.
- 네이버 이미지 검색 캐시에서 `포테의숲2-C` 게임 화면을 확인하고 몬스터가 보이는 crop을 보존했습니다. 나무형 적과 보라색 토끼형 개체를 볼 수 있으나 이름표가 식별되지 않아 트랜트/보라 팜팻 후보로만 등록했습니다. ILBE 글의 개별 몬스터 GIF 주소는 현재 404/투명 GIF로 열려 sprite 바이너리를 확보하지 못했습니다. 출처별 범위는 `assets/pote/review/monster_rebuild_v1/source_audit.md`와 `master/data/Monster_Visual_Evidence.csv`에 남겼습니다.
- 다음 작업: 각 대상의 새 후보 12 PNG를 개별 파일로 생성하고 출처 근거와 같이 검수합니다. v0.4의 168장은 시각 참조로 사용하지 않습니다. 생성 후보의 검수·시각 수락·게임 배율/앵커 검증을 서로 다른 상태로 기록하고, 승인 전 APK에 넣지 않습니다.

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


## 2026-09-29 몬스터 아트 후보 진행 갱신

- 작업 브랜치 `codex/monster-handoff-sources-20260929`의 후속 커밋 `e3adc1e`까지 확인: 새 리빌드에는 사용자 시각 수락 라이칸스로프 12장과 생성 후보 팜팻 빨강/보라/초록/실버 48장이 존재합니다. 팜팻 후보는 1254×1254 원본 투명 PNG로, 64×64 게임 프레임 제작·앵커·시각 승인·런타임 적용은 미완료입니다.
- 빨강·보라·초록 후보의 방향/포즈를 화면에서 확인했습니다. 보라 및 초록 대기 방향은 뒤/앞 구분이 보이나 전체 세트 원작 유사성·게임 내 크기/방향 정합성 QA를 통과했다고 보지 않습니다. 빨강은 빠른 시각 확인만 했고 구르기 공격만 별도 포즈로 생성됐습니다.
- 이번 업데이트 이후 작업 우선: 신규 산출물을 기준 프레임 크기로 변환·검수하고, 남은 9개 종/색상형 108 PNG를 생성합니다. 확정 로스터 및 제외/참고 규칙은 변경하지 않았습니다.

- 추가 진행: 팜팻 실버형 12개 별도 PNG 후보를 생성했습니다. 이제 팜팻 네 색상형 48개 후보가 브랜치에 있습니다. 원본 추출이 아니고 1254×1254 생성 원본이므로 사용자 최종 승인 및 기술 적용 대상은 아닙니다. 나머지 9개 종/색상형 108개는 미생성입니다.
- 팜팻 실버형 12개 생성 후보를 추가했고 대기 네 방향에서 앞/뒤 시각 차이를 확인했습니다. 네 색상형 48장은 모두 1254×1254 원본 이미지이며 기존 64×64 게임 프레임에 맞춘 기술검수·사용자 승인·APK 적용은 이뤄지지 않았습니다.
