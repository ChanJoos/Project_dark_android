# 디렉터 작업 지시

Revision THREE-20260910 · 기존 D002 작업순서/역할을 대체한다.
기준: 최신 사용자 요청(예약 3개, 세부 작업보다 구현 우선), PLAYTEST_CANON_20260910_2149.md, DIRECTOR_GUIDE.md.

이번 갱신은 운영 지침 변경이다. 코드 전체 감사나 새 APK 실행을 수행한 것으로 간주하지 않는다. 아래 상태는 확인한 main/handoff/PR 기준이며 매 실행 실제 최신 코드와 대조한다.

| 담당 | 다음 결과 1개 | 수락 조건 | 현재 근거/의존성 |
|---|---|---|---|
| 월드 | 최신 타일 이동 작업을 완료해 넘긴다 | tap/joystick 공용 tile-step API, 4방향 인접성·왕복·10-step drift=0·camera 일관성 검증 | 열린 PR #81, head b17c6c6d215f632530ea13f7b6b1dc3b0a8d2883의 구현 주장. BUILD/RUNTIME 미검증. 더 최신 head 여부 재확인 |
| 캐릭터 | 승인 무도가 4방향 IDLE/WALK를 실제 적용 가능한 자산으로 끝낸다 | 24×32/1.50, SE 우하/좌측 깨짐 없음, foot anchor 고정, missing/decode/shape fallback | main handoff의 V5/1.60 지시는 21:49 canon이 대체. 최신 활성 작업선 확인 후 계속 |
| 통합 | World 타일 이동을 GameView joystick/tap 및 Character facing에 연결한다 | free-pixel 우회 경로 없음, 두 입력 동일 이동, 방향 일치, compile/관련 회귀/assembleDebug, 가능한 Android 실행 | PR #81 본문이 GameView joystick의 state.tryMove 우회를 미연결로 명시. 이미 수정됐으면 재구현하지 말고 검증 |

## 이후 진행

1. M1 통과 후 월드는 길+등각 건물 1개를 먼저 전달하고 한 화면 3–5개 건물/소품/출입구/충돌로 완성한다. 캐릭터는 맨손 ATTACK을 전달한다. 디렉터는 작은 정상 변경을 즉시 통합한다.
2. M2 통과 후 디렉터가 기존 NPC/전투/보상 직접 지급/EXP·Gold/저장·재시작의 첫 끊긴 호출을 직접 수정한다. 중단한 Combat/RPG/UX 예약에 작업을 떠넘기지 않는다.
3. 크래시/데이터 손실/중복 보상은 언제나 우선. 그 외 미세 개선은 현재 acceptance가 통과하면 뒤로 넘긴다.

## 매 회차 디렉터 갱신

위 3행의 다음 결과·owner·수락 조건·blocker·PR/SHA를 최신 결과로 교체한다. 전체 설계/과거 이력을 반복 복제하지 않는다. 오래된 stacked/draft PR은 최신 canon/코드와 대조하여 활성/보류/superseded를 기록하며 일괄 병합·삭제하지 않는다. 사용자에게 가치 있는 변화가 통합되면 해당 SHA APK와 한국시간 빌드 시각·검증 범위를 제공한다.
