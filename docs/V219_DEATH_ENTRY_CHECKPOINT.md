Delivery recovery: PROJECT_DARK_V219_TEST.apk = libfile_8db4f0d8fed881919dab5ce21d662d94 / file_00000000fe4081fb8bf40459a5e5ff63; DEATH_VILLAGE_OUTDOOR_V219.png = libfile_59bc2ef9ed788191a5abad6767a93720; V219_VERIFICATION.json = libfile_a7257cc45a188191bb52c7726b08e628. All three saves succeeded with local identity applied.

## 2026-10-22 V219 야외 입장/건물 동선·시작 4스킬 — 빌드/호스트21검사 통과, 휴대폰 대기

Runtime b439c3b5ed6174c480ab8594063e12c53745a130; Actions39679695547/job119498638236 SUCCESS. DeathVillageV219Test3 + V2177 + SkillTestMode4 + GroundTapPath3 + MobileHud4 =21tests/0failures/errors/skips. 대기실 실제 도착12,12에서 버튼→기존 길찾기→portal27,10→야외28,10→실제문28,3→건물1 full update/입력으로 통과(중간 순간이동/몬스터 삭제 없음). 야외72live actor 충돌을 유지한 3개 건물 버튼/wideHUD 경로는 navigation tick+실제 gate 진입으로 검증; 2·3번 전체 AI full-update 이동을 검증했다고 확대하지 않음. 기존10씬/24방향·지하C↔건물3/저장·보상·arrival latch·12trip cache 유지. MainActivity 실제 실행에서 기존10칸 설정을 요청한 프라베라/속성강화/메테오/홀리드래곤4개+빈6칸으로1회교체,재실행 및 이후 사용자 수정 저장/정상 book 격리 통과. 7신규 native captures 확인. 원본10grid encoded blob과825generatedPNG actual bytes 비교 통과. 원작 몬스터 이름/외형 join 및 현재 원작 전체 일치 수용은 여전히 미확정.

APK PROJECT_DARK_V219_TEST.apk/com.projectdark.mobile.v219test,2026-10-22 06:37:46.231381KST,101663264bytes,SHA25624b8dcf2478296e9935601da13271a316affdf19934421330b5234c0fabc4058. artifact12382999598/ZIP123711053bytes/SHA25647dd0219607784d7c287a07b2c716928c22c9cf1c77ed1eeb57a3ab93c1a839a. APK embedded exact source/run,manifest/label/package,ZIP CRC,cryptographicV2 RSA signature+99chunk contentdigest 및825PNG actual bytes 확인. 별도 앱/저장,자동이전 없음. Common PRscope39679702893/119498661349 SUCCESS,unrelated full compile 의도적SKIPPED. V218 동일 기반 full 검사 재사용,main 통합 시 full 필수. PR197/main41fe6def 미병합. receipt verification/DEATH_ENTRY_V219_BUILD.json. 이 종료 기록은metadata-only/새APK재빌드 불필요.

205405(2)영상97.384062s/SHA256223c845d4c5287975701e45a76d4c4af93fc8552378fb1ea8be7ea7692dcc0e2 복구 사본을 다시 열어23초 목조건물 외부/50초 지하15와북쪽던전1안내 대조. 던전 미추가. 사용자 “205405 본 거 맞나”에 실제 본 내용과 대기실 동선 검증 누락을 구분해 답변. IMPLEMENTED/BUILD_VERIFIED/HOST_NATIVE_VERIFIED; PHONE/USER_VISUAL PENDING. V218 전역 렉 휴대폰 수용도 여전히 대기이며 이번 수정으로 전체 성능 해결 주장 금지. 다음은 exactV219 휴대폰 입장/야외/건물/4퀵슬롯 확인이며 같은 완성물을 무관한 검사로 재빌드하지 않음.

## 2026-10-22 V219 대기실 입구/야외 동선 및 시작 퀵슬롯 수정 — 후보 검증 중

DEVICE_FAILED_USER_REPORTED: 대기실에서 진행 방향을 찾지 못하고 야외가 보이지 않음. 외부 소스2049/건물3채는 존재했으나 대기실 portal27,10은 spawn12,12에서 화면 밖이고, 야외 arrival12,85는 첫 건물28,3에서 멀리 떨어져 있었다. 입구 이름은 foreground 이전에 그려져 가려질 수 있었다. Director 단일 편집, 기존 PR197/main41fe6def 유지. V218 전체 렉 신고/휴대폰 수용은 별도 PENDING이며 이번 수정으로 해결 주장하지 않는다.

V219: 대기실부터 항상 보이는 야외 입장 버튼, 야외의 대기실/건물1·2·3 버튼 및 각 실내 지하 목적지 버튼. 기존 collision-aware walking + 실제 gate trigger 사용; 자동 순간이동 아님. 의도적 버튼 선택만 arrival latch 해제, 자동 bounce 보호 유지. ADAPTED 야외 arrival/return portal을28,10으로 이동하여 영상에 확인된 첫 목조건물28,3이 입장 화면에 보이게 함. 원본 grid/건물 PNG/몬스터/지하24방향 topology 불변. 원본 reference.json은 변경하지 않고 이 모바일 진입 좌표 override를 명시. 문/계단 이름은 foreground 이후 그려짐. 각 맵 gate 리스트 정적 캐시.

시험 모드 첫 접속 기본 quickslots=프라베라(SK_마법사_050),속성강화(SK_마법사_044),메테오(SK_마법사_043),홀리드래곤(SK_성직자_040),나머지6칸 비움. 기존 시험 slots는 preset_revision219로 한 번 교체; 이후 사용자 변경/재실행 보존. 정상 모드 습득/직업/저장 분리 유지.

변경 범위: GameView의 지역 HUD/입력과 test preset, DeathVillageGuide/Renderer/Travel/Geometry/Data, build identity. 관련 검사: 신규 실제 spawn→입구/야외→건물1 full update와72live collision 건물3개/wideHUD/실제MainActivity legacy preset migration+restart, V217 10씬24방향/계단/보상/저장/cache, SkillTestModeTest(정상 book 격리·저장), GroundTapPathTest(기존 길찾기), MobileHudV135Test(입력 겹침). 공유 이동·전투·보상·save schema는 변경 없음. 직전 동일 기반 full 검사 재사용; 최종 main 통합은 full 필수. 변경 파일 exact SHA256만 V219 focused 인정, 알 수 없는 runtime byte는 full fallback. IMPLEMENTED; BUILD/HOST_RUNTIME/PHONE/USER_VISUAL PENDING. 후보 com.projectdark.mobile.v219test.

