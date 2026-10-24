## 2026-10-22 V223 검증 완료 — 마인 마을 NPC를 통한 죽음의마을 입장

소스 `f79f354459007d6fe2cc214319db7dba278ebbd7`, focused run39702647619/job119575266975 SUCCESS, 5 suites **23 PASS/0 failure/error/skipped**. 마인 이동 NPC 아서(mine_town_travel) 대화→대기실 도착·귀환·재입장·저장소 재설치 후 복원, 보상 불변, 밀레스/다른마을 메뉴 제거, 기존 마을/죽음의마을 문·지도 이동 회귀 PASS. native 마인 메뉴/대기실 도착 캡처 직접 검토. common39702654080 범위검사 SUCCESS; 전체회귀 아님.

집 안 추가 몬스터는 사용자 요청으로 제외; 기존 맵·배치·전투·성능·save schema 불변. 마인마인 NPC는 기존 마인 마을 이동 NPC로 해석해 적용했으며 신규 NPC/원작 사실 주장 없음. main41fe6def/PR197 미병합 후보 유지. IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_NATIVE VERIFIED; PHONE 및 사용자 시각 수용 PENDING.

APK PROJECT DARK V223 TEST, `com.projectdark.mobile.v222test`(V222 테스트 앱 업데이트), 빌드 **2026-10-22 10:00:37 KST**,101630380bytes,SHA256 `9d90c357c819ce565e9d98fb69c564a78fe9a06da4f04064dc93c26d43d05f62`. artifact12394245326. ZIP 해시·CRC/내장source+run/manifest·V223 label/CI APK V2서명 PASS. 상세 verification/DEATH_ACCESS_V223_BUILD.json. Library 저장 libfile_b9f902348fd88191ade855a105ef1644. 이후 문서 커밋을 APK 소스로 재라벨하지 않음. 다음: 정확 후보 휴대폰 이동 피드백. 새 콘텐츠 제안은 드랍→인벤→착용 흐름 정리이며 아직 구현 승인·작업하지 않음.

## 2026-10-22 V223 마인 NPC 죽음의마을 입장 — 구현, CI 대기

사용자 요청 U: 집 안 신규 몬스터 추가 제외. 죽음의마을 대기실 목적지는 마인 마을 기존 이동 NPC 아서(mine_town_travel) 메뉴에서만 제공. 밀레스 및 다른 마을의 직접 입장 카드 제거; 기존 마을간 이동/로비 귀환 유지. ID·저장 스키마·맵·몬스터·성능 코드 변경 없음. 베이스16b8d319/마지막 검증소스ae0c7ff6/run39698748694, main41fe6def 미병합 PR197 유지.

검증 범위: DeathAccessV223Test, MineTownV203Test, DeathVillageV217Test, DeathNavigationV221Test, DeathMapV220Test. 실제 NPC 접근/입력·대기실 도착/재입장·저장복원·보상무변경 및 메뉴/문연결 회귀. BUILD/HOST_NATIVE/PHONE PENDING. 다음: CI 결과·정확 APK 증거 기록. 원본 미확인 추가 몬스터는 구현하지 않음.

