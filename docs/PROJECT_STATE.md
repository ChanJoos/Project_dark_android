# PROJECT DARK 현재 상태

> 이 문서는 확인된 현재 스냅샷입니다. 과거 작업의 누적 일지가 아닙니다. 상태를 바꿀 때는 이 파일의 기존 내용을 확인하고 근거를 대조합니다.

## 기준 저장소

- 저장소: [ChanJoos/Project_dark_android](https://github.com/ChanJoos/Project_dark_android)
- 기준 브랜치: `main`
- 최신 `main` 확인 시점: 2026-09-28 (KST)
- 확인 당시 `main` HEAD: [`2eca004e2f2ddf9c17a76ac32c7b533c97f7861a`](https://github.com/ChanJoos/Project_dark_android/commit/2eca004e2f2ddf9c17a76ac32c7b533c97f7861a) (인수인계 문서 커밋)
- 마지막 코드/에셋 변경 SHA: [`0506929143996c31e38ea07918bea83377ff50a3`](https://github.com/ChanJoos/Project_dark_android/commit/0506929143996c31e38ea07918bea83377ff50a3)
- 저장소 README는 현재 실행물을 “v0.45 movement/rendering vertical slice”라 부릅니다. 기획안 버전과 실행물 버전은 별개입니다.

## 마지막으로 확인된 코드/에셋 변경

아래 SHA의 커밋 메시지는 `assets: replace Pamfet draft with Pote Forest monster set v0.4`입니다.

- `assets/pote/review/monster_concepts_v0.4/`에 포테 숲 몬스터 콘셉트 검토 자료가 추가되었습니다.
- 해당 README는 14종, 종별 대기/이동/공격 4방향 포즈, 개별 PNG 168장 및 아틀라스 14장을 기록합니다.
- README에 따르면 이 에셋은 원작에서 직접 추출한 스프라이트가 아니며, 완전한 반복 애니메이션이 아니고 APK에 적용되지 않았습니다.
- 이 커밋은 이전 경로의 팜팻 제작 초안을 삭제했습니다. 삭제 내용을 런타임에 통합된 에셋의 제거로 해석하지 않습니다.
- 코드/에셋 SHA의 Android workflow [#36387002427](https://github.com/ChanJoos/Project_dark_android/actions/runs/36387002427)는 `success`로 완료됐습니다. 문서 HEAD `2eca004`의 workflow [#36387737568](https://github.com/ChanJoos/Project_dark_android/actions/runs/36387737568)도 `success`입니다. 두 결과 모두 빌드/자동 검사이며 기기 작동 증거가 아닙니다.

## 프로젝트 상태 판정

- **검토용 에셋 추가:** IMPLEMENTED at `0506929`; BUILD_VERIFIED by workflow `36387002427`.
- **에셋의 APK 적용:** 해당 변경에서 수행되지 않았다고 커밋/README가 명시합니다.
- **기기 검증 및 시각 수락:** 위 에셋 변경에 대해서는 확인된 증거 없음.
- **다음 게임 개발 작업:** 미확정. 현재 백로그와 과거 handoff/status 사이에 날짜 및 작업 상태 충돌이 있으므로, 최신 코드·CI·사용자의 이번 지시를 대조하기 전에 어느 과거 티켓도 현재 작업으로 간주하지 않습니다.

## 최신 정보의 권위 순서

1. 사용자의 현재 지시와 승인
2. 현재 `main`의 해당 SHA 코드/데이터 및 그 SHA의 CI·기기 증거
3. `AGENTS.md`, `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`, `design/SOURCE_OF_TRUTH.md`, `design/DATA_CONTRACT.md` 등 승인된 규칙
4. 이 상태 문서와 현재 백로그
5. 날짜가 지난 실행 기록·자동 인수인계 문서
6. 채팅 요약·아이디어

충돌이 있으면 상위 근거로 해소하고, 해소할 수 없으면 충돌 상태를 그대로 기록합니다. 과거 기록은 삭제하지 않습니다.

## 현재 채팅에서 이어갈 것

새 채팅 시작 시 사용자가 원하는 다음 개발 결과를 확인합니다. 해당 요청을 기존 백로그의 구체 티켓과 연결할 수 있으면 근거를 확인해 백로그를 갱신하고, 연결할 수 없으면 새 작업을 임의로 만들어 확정하지 않습니다.

## 최근 APK 전달 및 사용자 기기 보고 (2026-09-28 KST)

- 전달 파일: `PROJECT_DARK-debug-0506929143996c31e38ea07918bea83377ff50a3.apk`
- 실제 전달 파일 SHA-256: `e10e94495a8cd5dba2f86753f28d617835d42ecc2f4f0baf331c9fe671b62ef2`
- 파일 바이트는 Actions run [#36387002427](https://github.com/ChanJoos/Project_dark_android/actions/runs/36387002427)의 artifact `PROJECT_DARK-debug-0506929143996c31e38ea07918bea83377ff50a3` (artifact ID `10955136379`)의 `app-debug.apk`와 일치함을 확인했습니다. 빌드 소스 SHA는 `0506929`입니다.
- 해당 SHA의 소스에는 `PoteFieldDef`, `PoteFieldRenderer` 및 밀레스 남쪽 출구를 통한 필드 진입 코드가 있습니다. v0.4 몬스터 콘셉트 이미지들은 review 경로이며 런타임 적용 자료가 아닙니다. 소스 존재만으로 실제 게임에서 숲에 진입할 수 있거나 이동이 작동한다고 증명되지 않습니다.
- **DEVICE_FAILED (사용자 보고):** 사용자는 전달된 APK가 기대한 버전이 아니며 포테 숲이 구현되지 않은 것처럼 보이고 캐릭터 이동도 되지 않는다고 보고했습니다. 이 보고를 수락 실패로 기록합니다. 동일 APK로 이 환경에서 재현하지 못했으며, 원인은 **UNKNOWN**입니다. SHA 확인은 산출물 출처를 확인한 것이지 사용자가 지적한 기능을 반박하는 근거가 아닙니다.
- **다음 작업 / 우선순위:** 현재 `main` APK와 정확한 기기 시나리오에서 (1) 시작 후 이동, (2) 남쪽 출구를 통한 포테 숲 진입, (3) 숲 내부 이동을 재현·진단합니다. 실패 경로와 원인을 소스·상태 저장·터치 라우팅·지도 연결 근거로 좁힌 뒤 수정하고, 새 SHA의 APK에서 같은 시나리오를 재검증합니다. 이 결과 전까지 해당 APK를 POTE playable/runtime accepted로 표시하지 않습니다.
