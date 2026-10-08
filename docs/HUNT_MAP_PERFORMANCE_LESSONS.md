# 사냥맵 성능 필수 기준 — 포테 V116–V119

2026-10-09 사용자 피드백: “속도문제해결!” USER_REPORTED_RESOLVED. 사용자 메시지는 APK 버전을 명시하지 않았다. 최신 확인 후보는 V119 runtime `02026e910cc068fa54236ce334ae5ea48f188674`, 브랜치 `codex/pote-forest-v112`. 이 피드백을 특정 APK 독립 실측 결과로 바꾸지 않는다. 이전 V118 실패는 과거 기록으로 보존한다.

**앞으로 모든 사냥맵 구현/수정 전에 반드시 이 문서를 읽고 아래 기준을 적용한다.**

1. 맵의 타일/인접 그래프만 맵 생명주기 동안 보관한다. `MonsterChasePathfinder.IndexedMap`을 재사용하고 맵 전환 때 교체한다. 몬스터 추격마다 맵 전체를 문자열로 재인덱싱하지 않는다.
2. 이동 경로는 실제 64×32 아이소메트릭 4대각 방향이다. A-star 휴리스틱은 `max(abs(dx)/32, abs(dy)/16)`이다. 이미 닫힌/개선 안 되는 이웃은 비싼 live 충돌 검사 전에 제외한다. 공격 인접칸이 모두 막혔으면 전체 맵 탐색을 피한다.
3. 클릭 시 가까운 지형부터 검사하며 기존 같은 거리 순서를 유지한다. 충돌 결과 memo는 단일 요청에서만 재사용한다. 움직이는 액터/예약 목적지/교차 segment는 캐시하지 않고 실제 이동 직전에 재검사한다.
4. 바닥 bitmap chunk는 최대32개, 현재 기준8,652,800bytes. 몬스터 source+hit cache는 현재 맵만 유지하며 최대60쌍×192×192×4×2=17,694,720bytes. 이것은 전체 프로세스/GPU 메모리 한도가 아니다.
5. 맵 진입 때 idle/walk/attack×4방향 및 hit 이미지를 미리 decode/prepare 한다. 첫 공격/방향전환 draw에서 decode하거나48px 아트를24px로 줄이지 않는다.
6. 프레임은 vsync에 맞춘다. update/render 후 고정16ms sleep을 추가하지 않는다. 주기 저장은 apply, 보상/전환/일시정지 등 확정 저장은 기존 commit/transaction 계약을 보존한다.
7. 다수 몬스터와 나무/장애물 근처에서 반복 클릭, 연속 조이스틱 이동, 첫 공격, 경계 chunk 진입,12회 맵 왕복을 함께 검증한다. median뿐 아니라p95/p99/max 및 캐시 증가량도 기록한다. CPU/native 통계는 휴대폰FPS가 아니다.
8. 몬스터 수 감소/충돌 생략/이미지 해상도 저하로 회피하지 않는다. 기존 이동·보상·저장·맵 전환 계약을 함께 확인한다.

검증 재사용: ForestFloorCacheTest, ForestPixelCacheTest, ForestTapProfileTest, GroundTapPathTest, MonsterChasePathfinderTest, MonsterChasePerformanceTest, ContinuousMoveProfileTest, MonsterQualityV118Test. 기존116개 native 검증과 exact APK 근거: `verification/MOVEMENT_CACHE_V119_BUILD.json`. V119 p99는 개선되었으나 렌더 최대 지연은 개선되지 않았다는 기록을 유지한다. 신규 맵에서 별도 사용자/기기 확인이 필요하다.
