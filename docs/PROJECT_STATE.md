# PROJECT DARK 현재 상태

> 이 문서는 확인된 현재 스냅샷입니다. 과거 작업의 누적 일지가 아닙니다. 상태를 바꿀 때는 이 파일의 기존 내용을 확인하고 근거를 대조합니다.

## 기준 저장소

- 저장소: [ChanJoos/Project_dark_android](https://github.com/ChanJoos/Project_dark_android)
- 기준 브랜치: `main`
- 기준 HEAD 확인 시점: 2026-09-28 (KST)
- 기준 SHA: [`0506929143996c31e38ea07918bea83377ff50a3`](https://github.com/ChanJoos/Project_dark_android/commit/0506929143996c31e38ea07918bea83377ff50a3)
- 저장소 README는 현재 실행물을 “v0.45 movement/rendering vertical slice”라 부릅니다. 기획안 버전과 실행물 버전은 별개입니다.

## 마지막으로 확인된 변경

위 SHA의 커밋 메시지는 `assets: replace Pamfet draft with Pote Forest monster set v0.4`입니다.

- `assets/pote/review/monster_concepts_v0.4/`에 포테 숲 몬스터 콘셉트 검토 자료가 추가되었습니다.
- 해당 README는 14종, 종별 대기/이동/공격 4방향 포즈, 개별 PNG 168장 및 아틀라스 14장을 기록합니다.
- README에 따르면 이 에셋은 원작에서 직접 추출한 스프라이트가 아니며, 완전한 반복 애니메이션이 아니고 APK에 적용되지 않았습니다.
- 이 커밋은 이전 경로의 팜팻 제작 초안을 삭제했습니다. 삭제 내용을 런타임에 통합된 에셋의 제거로 해석하지 않습니다.
- 이 SHA의 Android workflow [#36387002427](https://github.com/ChanJoos/Project_dark_android/actions/runs/36387002427)는 `success`로 완료됐습니다. 이는 해당 workflow에 기록된 빌드/자동 검사 결과입니다. 실제 기기 실행이나 시각 수락의 증거는 아닙니다.

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
