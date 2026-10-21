## 2026-10-22 V219 대기실 입구/야외 동선 및 시작 퀵슬롯 수정 — 후보 검증 중

DEVICE_FAILED_USER_REPORTED: 대기실에서 진행 방향을 찾지 못하고 야외가 보이지 않음. 외부 소스2049/건물3채는 존재했으나 대기실 portal27,10은 spawn12,12에서 화면 밖이고, 야외 arrival12,85는 첫 건물28,3에서 멀리 떨어져 있었다. 입구 이름은 foreground 이전에 그려져 가려질 수 있었다. Director 단일 편집, 기존 PR197/main41fe6def 유지. V218 전체 렉 신고/휴대폰 수용은 별도 PENDING이며 이번 수정으로 해결 주장하지 않는다.

V219: 대기실부터 항상 보이는 야외 입장 버튼, 야외의 대기실/건물1·2·3 버튼 및 각 실내 지하 목적지 버튼. 기존 collision-aware walking + 실제 gate trigger 사용; 자동 순간이동 아님. 의도적 버튼 선택만 arrival latch 해제, 자동 bounce 보호 유지. ADAPTED 야외 arrival/return portal을28,10으로 이동하여 영상에 확인된 첫 목조건물28,3이 입장 화면에 보이게 함. 원본 grid/건물 PNG/몬스터/지하24방향 topology 불변. 원본 reference.json은 변경하지 않고 이 모바일 진입 좌표 override를 명시. 문/계단 이름은 foreground 이후 그려짐. 각 맵 gate 리스트 정적 캐시.

시험 모드 첫 접속 기본 quickslots=프라베라(SK_마법사_050),속성강화(SK_마법사_044),메테오(SK_마법사_043),홀리드래곤(SK_성직자_040),나머지6칸 비움. 기존 시험 slots는 preset_revision219로 한 번 교체; 이후 사용자 변경/재실행 보존. 정상 모드 습득/직업/저장 분리 유지.

변경 범위: GameView의 지역 HUD/입력과 test preset, DeathVillageGuide/Renderer/Travel/Geometry/Data, build identity. 관련 검사: 신규 실제 spawn→입구/야외→건물1 full update와72live collision 건물3개/wideHUD/실제MainActivity legacy preset migration+restart, V217 10씬24방향/계단/보상/저장/cache, SkillTestModeTest(정상 book 격리·저장), GroundTapPathTest(기존 길찾기), MobileHudV135Test(입력 겹침). 공유 이동·전투·보상·save schema는 변경 없음. 직전 동일 기반 full 검사 재사용; 최종 main 통합은 full 필수. 변경 파일 exact SHA256만 V219 focused 인정, 알 수 없는 runtime byte는 full fallback. IMPLEMENTED; BUILD/HOST_RUNTIME/PHONE/USER_VISUAL PENDING. 후보 com.projectdark.mobile.v219test.

