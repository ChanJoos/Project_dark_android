# V139 — 아벨해안 첫 맵 구현

기준 main55988d8ab99fdf046d419d54cfa5bd67d073d77c. 사용자 요청: 원작/유튜브 참고, 포테 lessons learned 유지, 아벨부터 게임 확장. Director single editor; codex/abel-coast-v139.

IMPLEMENTED, CI/native/phone/user visual acceptance PENDING until exact evidence is recorded.

## 실제 범위

기존 포테/피에트 뒤 조사 거점 MAP_ABEL_COAST와 1-A/1-B/2-A/3-B/4-C의 여섯 맵을 연결한다. 대표 구역 이름은 원작 자료와 연결하되 크기/전체 연결/지형/개체 배치와 8개 E 조사 의뢰는 ADAPTED다. 원작 전체 방 목록이나 정확 좌표를 복제했다는 주장이 아니다. D04를 완료한 정상 Lv41 캐릭터에서 의뢰 E01이 이어지고 E08까지 Lv55 이상으로 정산한다. 정상 성장 엔진의 Lv99 상한은 유지하며 자유 사냥은 이후에도 가능하다.

노아 여행 대화에 정상 조건을 만족하면 아벨 출정, 기존 스킬 시험 모드에서는 아벨 미리보기를 제공한다. 미리보기는 레벨/직업/과거 퀘스트 완료나 보상을 주입하지 않는다. 정상 해안 진입은 Lv41와 D04 완료를 확인한다. 기존 포테 퀵퀘스트·NPC 상호작용·중앙 B HUD 유지. 아벨 조사/보급 NPC의 이름과 서비스는 ADAPTED.

첫 사냥 지형에 터틀 및 이름 매핑 미확정 붉은 문어 후보가 실제 등장한다. 각각12독립 포즈(idle/walk/attack×NW/NE/SW/SE),192RGBA, bottom184 foot registration. 붉은 문어를 그래브/아크퍼스 등 원작 이름으로 임의 등록하지 않는다. 모든 신규 HP/EXP/Gold/재등장 수치는 ADAPTED. 1-A72/1-B96/후속108개체; 기존 포테 개체/그림/스킬/장비를 축소·교체하지 않는다. 보상은 기존 Resolver→ledger→종별 프로필→직접 인벤토리/EXP/Gold 단일 경로에서 처리한다. 원작 임의 드롭 확률이나 킹아크 반지 보상을 만들지 않는다.

## 출처와 남은 몬스터

master/source/abel_v139/research.json은 Master6종과 붉은 문어/아크퍼스 후보, 구 던전 몬스터, 해저 구분을 모두 기록한다. 원작 게임 화면5장의 원본 bytes/hash를 reference_screenshots.zip에 보존한다. 실제 참고 화면에서 누런 모래/푸른 물결/짙은 야자수/회색 암초/터틀/빨간 문어/보라색 킹아크를 확인했다. built-in ImageGen 생산 후보의 prompt 및 원본/hash/기계적 cell extraction은 master/assets/abel_v139에 기록한다. 원작 클라이언트 애니메이션을 추출한 자산이 아니다.

유튜브 딩군의 VxmS9AhpMwQ/15:17 영상을 식별했지만 영상과 storyboard CDN은 Site Unavailable HTML로 막혔다. 영상 재생/프레임 감상 완료로 취급하지 않는다. 원작 전투 모션/타이밍 비교 OPEN.

그래브: 2026-01-15 공식 업데이트가 아벨해안2 출현을 명시; 외형/수치 OPEN. 퐁퐁이: 원작 재료/외형 서술 자료, 정밀 포즈 OPEN. 크랩: 크래커 별칭 충돌 보존, 게껍질 자료, 정확 외형 OPEN. 광폭자: Blue Arkpus likeness/구수치 자료 있으나 현행/자산 OPEN. 킹아크: false→octopus-leg→Milles altar→grudge essence→real boss와 무도가/그룹 위치/뤼케시온 포스파늄/3-B비밀 방/4-C암초가 필요한 실제 이벤트 조사 완료; 이번 조사 의뢰와 혼동하지 않으며 boss 구현 미완료. 그동안의 2재료→단일 보스 요약을 원작 완성으로 표시하지 않는다.

## 포테에서 배운 규칙

기존 WorldRuntimeAdapter/4diagonal64×32 lattice/WorldMoveTargetController/RuntimeState live occupancy, reservation and movement segments/MonsterChasePathfinder를 공유한다. 지형 navigation/connected component/adjacency는 맵 생명주기 캐시, 동적 충돌은 재검사. 신규 바닷물과 암초·야자수 발판은 같은 AbelCoastGeometry 데이터로 표시/충돌을 구성한다. 장식/actor는 기존 foot-Y depth merge, 야자수 가림은 source alpha mask.

기존 world-aligned256+2gutter floor chunk LRU32(8,652,800 pixel bytes) 경로에 해안 모래·바다만 추가한다. 새 map-wide framebuffer/second loop 없음. 현재 맵 idle/walk/attack4directions+hit을 entry에 prewarm, 교체시 이전 몬스터 캐시를 제거한다. 최대60source/hit pairs 계약 유지. vsync 및 periodicapply/criticalcommit transaction 변경 없음.

검증: AbelCoastV139Test 정상5직업 D04fixture→실제Resolver사냥→E8원자적 보상/저장, 이전 기록 보존/진입 조건, 모든 맵/문/NPC/개체의 실제 live-collision 길, 게임 대화 preview 무EXP주입, 모든 맵 process-style restore,24포즈/native6카메라/12round trips cache한도. 기존 숲 floor/pixel/tap/chase/movement/HUD/magic/save/quality 회귀. Robolectric CPU 수치를 handset FPS로 부르지 않는다.

휴대폰 이동/첫 공격/다수 추격/바닷가 충돌/맵 왕복/앱 종료재개 및 새 그림 승인 PENDING.
