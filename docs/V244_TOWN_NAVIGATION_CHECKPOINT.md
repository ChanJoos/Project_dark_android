## V244 local verification / publication blocked

전체 app/src/main Java와 신규 TownNavigationV244Test 및 종속 fixture compile PASS (API35 android-all, 임시 R/BuildConfig 선언, 원본 pinned manifest의1073개 generated wearable binding 사용). Android Gradle/패키징 성공 주장은 아님. 실제 Milles tile layer의736,704→HUD46,39 및 등록된 리콜6종 destination resolution Java probe PASS. CI scope classifier6tests PASS. 확대지도/정상HUD입력/path/포탈/저장복원 host-native 실행은 아직 PENDING; standalone probe는 Android Paint.nInit native runtime 부재로 중단. 변수 scope 충돌을 실제 compile 전에 수정.

최초 로컬커밋1a73b67의 git push가 자동 승인 검토에서 “대상 GitHub로 소스/payload를 내보내는 명시적 승인 미확인”으로 거절됨. 해당 push 재시도·다른 API 우회 없음. 로컬 후속 수정/검증만 완료. GitHub source/CI/APK 미갱신. Next: ChanJoos/Project_dark_android 기존 codex/rucesion-coast-v157 브랜치의 변경 소스 업로드 명시 승인 → 최종 로컬 commit push → scoped CI 실실행/실패수정 → exactAPK 검증/전달. 상세 verification/TOWN_NAVIGATION_V244_LOCAL.json.

## V244 — 밀레스 (46,39) 시작·귀환 및 전 맵 미니맵 이동 (검증 대기)

사용자 2026-10-23 지시: 신규 시작·밀레스리콜·다른 마을 NPC 이동의 밀레스 도착점을 HUD (46,39), 현재 lattice world (736,704)로 통일. 기존 저장 위치는 그대로 복원. 다른 마을 리콜은 NPC 도착점 CampaignWorld.arrival을 공유. 기존 밀레스 외 5개 마을 리콜을 명시적 ADAPTED item ID로 등록하고 시약점 리콜류에 기존 1000G 프로젝트 가격으로 연결. 살아 있을 때 1개만 소모하며 저장 실패 시 기존 transaction rollback 사용. 신전 깊은 방에서 귀환 시 중간 실내 레이어를 모두 종료.

미니맵 열기 마을 제한 해제, 모든 현재 맵 가족(포테·아벨/뤼케시온해안·광산·호러캐슬·죽음의마을·각 마을·일반실내·신전)에 실제 geometry 출입구 pin 연결. 확대 지도에서 기존 live 길찾기로 입구까지 걷고 기존 portal/level/latch 조건을 유지. 실내/광산/호러는 해당 renderer 사용. 1024x512 overview 1개와 기존 96x96 HUD terrain을 재사용; portal 표시는 terrain rebuild 때만 그린다. 이동/몬스터/전투·마법/보상·save schema 불변.

영향: WorldDef spawn, TownTravel arrival/recall mapping, RpgProgressionState·TownCommerce·ItemEffects recall, WorldMapWindow pins, GameView HUD/map/recall integration, MobileHudSkin cached markers, candidate package V244. 관련 검증: 새 TownNavigationV244Test 6건(실제 시작/마을왕복/리콜6종/실내귀환/전맵pin/정상HUD입력·path·exit·cache·restore), GroundTapPathTest, RuntimeCheckpointTest, ForestFloorCacheTest. GameView 대형파일 자체는 전체회귀 이유가 아님; 최종 main 통합은 별도 full gate.

Base d6a1d34642b4cf0a9541efd5f01c688aa941ccf8, same PR197/branch codex/rucesion-coast-v157, main41fe6def unchanged. IMPLEMENTED_CANDIDATE / BUILD_PENDING / HOST_NATIVE_PENDING / PHONE_PENDING. Next exact-source focused CI → failures resolve → APK source/run/hash/signature verification → deliver. V243 verified work not rebuilt separately.

