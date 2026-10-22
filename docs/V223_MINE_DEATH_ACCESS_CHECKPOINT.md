## 2026-10-22 V223 마인 NPC 죽음의마을 입장 — 구현, CI 대기

사용자 요청 U: 집 안 신규 몬스터 추가 제외. 죽음의마을 대기실 목적지는 마인 마을 기존 이동 NPC 아서(mine_town_travel) 메뉴에서만 제공. 밀레스 및 다른 마을의 직접 입장 카드 제거; 기존 마을간 이동/로비 귀환 유지. ID·저장 스키마·맵·몬스터·성능 코드 변경 없음. 베이스16b8d319/마지막 검증소스ae0c7ff6/run39698748694, main41fe6def 미병합 PR197 유지.

검증 범위: DeathAccessV223Test, MineTownV203Test, DeathVillageV217Test, DeathNavigationV221Test, DeathMapV220Test. 실제 NPC 접근/입력·대기실 도착/재입장·저장복원·보상무변경 및 메뉴/문연결 회귀. BUILD/HOST_NATIVE/PHONE PENDING. 다음: CI 결과·정확 APK 증거 기록. 원본 미확인 추가 몬스터는 구현하지 않음.

