# 디렉터 작업 지시

Revision THREE-20260910 · 기존 D002 작업순서/역할을 대체한다.
기준: 최신 사용자 요청(예약 3개, 세부 작업보다 구현 우선), PLAYTEST_CANON_20260910_2149.md, DIRECTOR_GUIDE.md.

이번 갱신은 운영 지침 변경이다. 코드 전체 감사나 새 APK 실행을 수행한 것으로 간주하지 않는다. 아래 상태는 확인한 main/handoff/PR 기준이며 매 실행 실제 최신 코드와 대조한다.

| 담당 | 다음 결과 1개 | 수락 조건 | 현재 근거/의존성 |
|---|---|---|---|
| 월드 | M2의 첫 조각으로 기존 투영 위에 길과 연결된 등각 건물 1개를 완성한다 | 지면·건물·출입구·그림자·collision이 같은 iso 투영을 사용하고 flat-front box가 아니며 실제 GameView에 표시 | PR #81의 후속 head `464f315`에서 M1 tile 이동 전달 완료. main 통합/CI 결과를 확인한 뒤 맵 bounds 확장 없이 진행 |
| 캐릭터 | 승인 무도가 4방향 IDLE/WALK를 실제 적용 가능한 자산으로 끝낸다 | 24×32/1.50, SE 우하/좌측 깨짐 없음, foot anchor 고정, missing/decode/shape fallback | 새 최신 canon 대응 Character commit 없음. V5/1.60 및 startup-crash 계보는 superseded/보류 |
| 통합 | 통합된 M1 이동 APK를 기기에서 검증하고 Character 결과가 오면 facing/anchor를 닫는다 | joystick/tap 4방향 한 칸, 왕복 drift=0, camera 일치, SE·좌측 방향, startup 안전성을 실제 Android에서 확인 | World `464f315`를 최신 main 위에 통합한 main commit `b8f36e6`; 로컬 runner에는 Gradle/JDK/Android runtime이 없어 GitHub CI와 기기 gate 필요 |

## 이후 진행

1. M1 통과 후 월드는 길+등각 건물 1개를 먼저 전달하고 한 화면 3–5개 건물/소품/출입구/충돌로 완성한다. 캐릭터는 맨손 ATTACK을 전달한다. 디렉터는 작은 정상 변경을 즉시 통합한다.
2. M2 통과 후 디렉터가 기존 NPC/전투/보상 직접 지급/EXP·Gold/저장·재시작의 첫 끊긴 호출을 직접 수정한다. 중단한 Combat/RPG/UX 예약에 작업을 떠넘기지 않는다.
3. 크래시/데이터 손실/중복 보상은 언제나 우선. 그 외 미세 개선은 현재 acceptance가 통과하면 뒤로 넘긴다.

## 매 회차 디렉터 갱신

위 3행의 다음 결과·owner·수락 조건·blocker·PR/SHA를 최신 결과로 교체한다. 전체 설계/과거 이력을 반복 복제하지 않는다. 오래된 stacked/draft PR은 최신 canon/코드와 대조하여 활성/보류/superseded를 기록하며 일괄 병합·삭제하지 않는다. 사용자에게 가치 있는 변화가 통합되면 해당 SHA APK와 한국시간 빌드 시각·검증 범위를 제공한다.
