# PROJECT DARK 개발 지휘 기준

Revision THREE-20260910 · 사용자 요청: 3개 예약으로 단순화하고 실제 구현을 우선한다.

이 개정은 과거 4/5개 역할, 회차마다 새 브랜치/PR 생성, 디렉터의 검증 전용 제한을 대체한다. 최신 사용자 확정 gameplay/visual canon은 그대로 따른다. 현재 기준은 `design/PLAYTEST_CANON_20260910_2149.md`이며 19:38의 1.60 scale과 맵 확장 지시는 그 문서가 대체한다. 더 최신 사용자 확정이 생기면 디렉터가 기준 문서를 갱신한다.

## 공동 목표와 역할

첫 30초가 실제 게임처럼 보이고 움직이는 Android APK를 먼저 완성한다. 한 화면은 먼저 완성할 제작 범위이며 기존 이동 가능 영역을 강제로 축소하라는 뜻이 아니다.

| 활성 예약 | 책임 | 지금 끝낼 결과 |
|---|---|---|
| DARK 플레이어블 월드 | world/map/tile/collision/path/camera와 World 이동 API | 기존 타일 이동 작업을 완료하여 넘긴 뒤, 등각 길·건물·소품의 한 화면 마을 |
| DARK 캐릭터 완성 | CharacterRenderer/atlas/방향/모션/리소스 안전성 | 최신 승인 무도가 24×32, scale 1.50의 4방향 IDLE/WALK, 이어서 맨손 ATTACK |
| DARK 통합·검증 | GameView/MainActivity/RuntimeState 연결, HUD/input, CI/APK, 현재 루프의 Combat/RPG/NPC/save 연결 및 회귀 수정 | World 이동과 Character facing을 실제 입력·루프에 연결하고 APK로 검증·전달 |

디렉터는 직접 코드를 수정하는 통합 책임자다. 중단된 Combat/RPG/UX 예약의 재개를 기다리지 않는다. 기존 모듈 경계와 중앙 mutation API는 보존하며 GameView에 내부 로직을 복제하지 않는다. 현재 단계와 무관한 기능 확장은 하지 않는다. World/Character 내부를 디렉터가 긴급 수정해야 하면 최신 head를 확인하고 변경 SHA/영향 파일을 handoff에 남겨 동시 작업과 조정한다.

## 실행 원칙

1. 최신 main SHA와 사용자 canon, 이 지침/backlog, 담당 handoff와 활성 PR을 읽는다. 최초에는 constitution/data contract/source registry/Master manifest/reconciliation을 읽고 이후 변경분·담당 테이블·의존 데이터만 확인한다. 매번 모든 history/92 CSV를 재감사하지 않는다.
2. 담당 결과 하나를 선택해 코드 → 실제 호출 연결 → 관련 검증까지 닫는다. 연구·문서 비중은 대략 20% 이내를 목표로 하되 정확성이나 접근 제한을 우회하지 않는다. P0 연결이 남았는데 cleanup/프레임워크/미세 표현만 반복하지 않는다.
3. 정상 코드를 보존하고 기존 미완료 작업을 이어서 처리한다. 역할별 활성 미병합 작업선/PR 하나를 재사용한다. 새 회차라는 이유로 run-id 브랜치나 stacked PR을 만들지 않는다. 완료/폐기 후 새 작업선이 필요하면 최신 main에서 시작한다.
4. World/Character는 자기 코드와 관련 검증, 짧은 handoff를 commit/push한다. GameView 연결 위치/API를 정확히 남기고 통합 담당에게 넘긴다. main 반영은 디렉터만 한다. force push 금지. 시각이 다르다고 실행이 직렬이라고 가정하지 않는다.
5. 디렉터는 연결 접점·회귀를 직접 수정하고 검증한 head만 통합한다. 안전하고 독립적인 부분 변경은 다른 작업 전체 완료를 기다리지 않는다. 최신 main이 바뀌면 영향/충돌을 다시 확인한다.
6. compile + 관련 audit/회귀 검증 후 assembleDebug 및 APK를 확인하고 가능한 Android emulator/device에서 실행한다. main 반영 후 동일 main SHA CI 결과를 확인한다. UI/자산 변경은 실제 런타임 시각 검수가 필요하다. mockup/atlas preview는 runtime 증거가 아니다.
7. 사용자 테스트 가치가 있는 visible delta가 있으면 해당 회차 APK를 다운로드해 제공한다. 빌드 한국시간(Asia/Seoul), SHA, 변화와 검증 범위를 표시한다. Android 실행 환경이 없으면 BUILD VERIFIED까지만 보고하고 미실행 이유/구체적인 테스트 행동을 밝힌다. 변경 없으면 억지 APK·버전을 만들지 않는다.
8. 보고는 바뀐 플레이 경험 / commit·PR / 실행 검증·미실행 gate / 다음 결과 각 1~2줄이다. backlog는 역할별 다음 결과 하나와 acceptance/의존성/PR·SHA만 유지한다. 같은 미연결 문제가 두 회차 반복되면 직접 연결 수정 또는 구체적 접근·환경 blocker를 밝혀야 한다.

## 완료 순서

- M1: 실제 맵에서 tap/joystick이 동일한 4방향 인접 타일 이동을 사용하고 camera와 캐릭터 facing이 일치한다. 안전한 기존 renderer로 이동부터 검증 가능하다. 최신 캐릭터는 24×32/1.50, IDLE/WALK의 SE 및 좌측 깨짐이 없어야 한다.
- M2: 길·광장, 투영이 맞는 건물 3–5개, 출입구·나무·울타리·소품·충돌이 하나의 마을 화면으로 읽힌다. 길+건물 1개의 작은 변경부터 통합한다. 무작정 bounds를 늘리지 않는다.
- M3: 기존 NPC/대화 → 전투/구분되는 ATTACK·SKILL·MAGIC → 사망 이벤트 → 인벤토리 직접 지급 → EXP/Gold → 저장/앱 종료·재시작 복원을 연결한다. 테스트 직업/행동은 사용 조건을 보존하며 평민에게 임의 마법을 열지 않는다.
- 앞 단계 수락 후 미세 개선은 뒤로 넘기고 다음 단계로 간다. 시작 크래시·저장 손실·중복 보상은 단계와 관계없이 우선 수정한다.

## 고정 기준과 근거 갱신

- 원작 사실은 필요 시 넥슨 어둠의전설 공식 홈페이지/가이드/공지에서 검증한다. Master는 완성본이 아니며 디렉터가 관련 근거/변경분을 지속 갱신한다. 수집 자체가 구현을 대체하면 안 된다.
- 출처·날짜·O/V/U/B/ADAPTED/FAN/PENDING_CROP를 보존한다. 커뮤니티의 넥슨 호스팅만으로 O라고 하지 않는다.
- 최신 승인 무도가 reference를 사용한다. 승인된 제작 이미지는 ADAPTED이며 원작 asset 증거가 아니다. 새 AI 컨셉으로 교체하지 않는다. 설명판의 라벨도 실제 방향 픽셀과 대조한다.
- NW/NE/SW/SE, 한 step=인접 tile 1칸. 64×32 projection의 delta는 NW(-32,-16), NE(+32,-16), SW(-32,+16), SE(+32,+16). 논리 tile 중심/화면 보간 분리.
- 캐릭터 24×32/1.50, 동일 foot anchor, nearest-neighbor, 무기 없음/방패 허용, 맨손 공격. missing/decode/shape 실패가 startup crash를 일으키지 않아야 한다.
- 전체 스크린샷을 live map texture로 쓰지 않는다. 타일/오브젝트/충돌로 구성한다.
- 보상은 MONSTER_DEFEATED → reward resolution → inventory mutation. 바닥 드랍/pickup은 폐기되었다. 원작 미확정 보상은 PENDING, 사용자 승인 TEST REWARD는 B/ADAPTED로 구분한다.

## Master 개정 규칙

- `master/source`와 첨부에서 가져온 `master/data`는 원본 파생 스냅샷이다. 원본 손실을 감추려고 삭제/정리/요약하지 않는다.
- 개선은 `master/changes/<change-id>.json` 제안으로 작성한다. source sheet/cell/ID, before/after, 이유, evidence 원문·버전·날짜, 영향 받는 코드/저장/검증, status(PROPOSED/ACCEPTED/REJECTED)를 포함한다.
- director가 근거·참조·테스트·현재 사용자 확정을 검토하고 ACCEPTED로 기록한다. accepted 변경만 runtime adapter에 반영하고 Master manifest revision과 generator 입력에 연결한다. 원본 자료 자체의 변경은 새 source snapshot으로 추가한다.
- 명시적인 사용자 확정과 확인된 오류 수정은 불필요하게 재질문하지 않는다. unknown은 unknown이며, [B]는 원작 확정치로 승격하지 않는다. 개별 출처 필드에 SOURCE를 무조건 O로 매핑하지 않는다.
- 문서의 READY/PASS는 과거 작성자의 주장이다. 코드 구현·실행 검증과 별도다. source sheet 92개와 row 1786은 현재 첨부 측정값이며 추후 버전에서 바뀔 수 있다.


## 상태 구분

PLANNED = 아직 미구현. IMPLEMENTED = 코드/commit 존재. BUILD VERIFIED = 동일 SHA compile·관련 검증·assembleDebug 성공 및 APK 존재. RUNTIME VERIFIED = 해당 SHA/APK를 Android에서 실행한 시나리오/로그·스크린샷 근거 존재. 실행한 범위만 판정한다.

현재 작업 지시를 정리한 것이며 이번 가이드 개정을 게임 구현·빌드·런타임 검증 완료로 보고하지 않는다.
