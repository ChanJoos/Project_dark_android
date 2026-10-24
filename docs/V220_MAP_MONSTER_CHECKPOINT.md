## 2026-10-22 V220 검증 완료 — 지도/포탈/몬스터 교정 후보, 휴대폰 수용 대기

V219 USER_VISUAL_REJECTED 기록을 유지한다. V220 runtime `45936b9b65be6614e27d08a237e0450e3e718233`, focused run39685771102/job119519304224 SUCCESS. 8 suites **27 PASS/0 failure/0 skipped**, scope selector5 PASS. 실제6town overview render/camera restore/readonly/cache, 4칸 ground portal 진입/group latch,10씬/27gates/12trip/48monsterposes,4quickslot restart,HUD/ground/movement/Milles검사. 공통39685778854 scope검사SUCCESS, full compile의도적SKIPPED;최종main 전체회귀 또는휴대폰FPS를주장하지않음.

APK **PROJECT DARK V220 TEST**, `com.projectdark.mobile.v220test`, legacy184/1.84-mobile-horror; 빌드시각 **2026-10-22T07:25:44.664592+09:00**,101628448bytes,SHA256 `2fb460a5caf2cf8e31e2c1189043700e38fe3164ced53859e8b42cc5a613161f`. ZIP CRC,embedded source/run,binarymanifest,APK V2 RSA 서명/contentdigest/certificate,825원본PNG bytecompare PASS. Artifact12387036337 ZIP SHA256cb8522a34cf2d5fb8fb13c126afa7b312eb2f78d7b5d9acc8ad68765ea3e19dc. Receipt `verification/DEATH_MAP_MONSTERS_V220_BUILD.json`.

신규네이티브캡처6지도+3몬스터 직접검토. 영상205405 2s실제전체지도/23s초록옷좀비/29s니크르·초록거미/43s단검든쥐인간과원본166/178/176/175를대조. 이전회색석상/후드기사/보라슬라임가명교체. 원본10map데이터정의는V219와동일. 이동버튼삭제,로비8..11,0접촉포탈,모든canonical마을미니맵전체지도/현재위치/입구pins,지도터치는읽기전용. 영상외영역은근처원본map사용한adapted영역,던전미구현. 한국어웨어렛/독거미이름은형태·목록join이며클라이언트이름표확정아님. 기존수치/보상프로필유지,원작통계확정주장금지.

IMPLEMENTED/BUILD VERIFIED/SCOPED HOST_NATIVE VERIFIED. PHONE/USER_VISUAL ACCEPTANCE **PENDING**. 기존PR197미병합/main41fe6def유지. 앱설치와실기속도·사용자시각수용은별도단계. 아래PENDING기록은빌드전이력이며이머리가현재상태다.

## 2026-10-22 V220 사용자 V219 거부 — 포탈/전체지도/몬스터 수정, 검증 대기

DEVICE_FAILED_USER_REPORTED/USER_VISUAL_REJECTED: 대기실·건물 이동 버튼 금지; 대기실8,0~11,0 포탈; 모든 마을 미니맵 터치 후 영상처럼 전체지도/현재위치/포탈을 보고 직접 이동; 회색좀비/집안의망자/슬라임 등 원본과 불일치한 외형·가명 거부. Director 단일 편집,기존PR197/main41fe6def 유지. 앞선V219호스트21PASS는 사용자 시각 수용이 아니었으며 전체완성 근거로 재사용 금지.

이동 버튼 및DeathVillageGuide삭제. 로비4gate8..11,0,귀환9,0;4칸을 묶은 arrival latch. 기존그리드4칸모두원본walkable. 일반ground/joystick접촉만 진입. 전체지도는실제맵원본렌더러로1024x512(2MiB) 한장,current map만 cache;static배경+건물/현재위치/실제입구pins. 미니맵/닫기만입력,지도터치자동이동없음. Milles/Abel/Rucesion/Mine/Deathlobby+field/Deathinteriors지원. 임시 overviewcamera는finally에서 exact기존camera객체복원;플레이어좌표/이동/충돌geometry미변경. Millesclip을Canvas실제clipbounds로 판정하여축소지도에전체건물이표시됨. 원본32chunkLRU를확대/소모하지않으며이동frame에지도재생성없음.

205405(2)영상을2초전체지도/23초야외/29초집안/43초지하15로 다시 decode하고 공식741설치본(208513633bytes/SHA256dbbd937d4dd1eed310b7b1de4c5c3d98e829ed77e5493b0857063528a47e1093)의953MPF 실제 원본 index를 재검토. 137회색석상/171후드기사/105보라슬라임 잘못된선택을166초록옷좀비/178붉은몸초록모자니크르/176초록거미로 교체,175단검든쥐인간은지하에만배치. 집안니크르+거미,지하웨어렛.좀비/니크르 이름은기존Master·Nexon4509/4515,웨어렛/독거미는Nexon호스팅목록721717와형태대조에근거한join(클라이언트한국어이름테이블 미확보,확정원작통계 주장금지). 새raw원본pack master/source/death_v220에보존,기존source는삭제안함. 4프로필ID/모바일수치/EXP·Gold/status정책 및72/15/36마리수 유지. 기존집/지하가명은사용안함.

관련검사: 신규4tests(네portal 실제접촉·whole-strip latch·6town originaloverview/camera/readonly/cache·MPF조인/지역분리/저장),기존V2177(10씬/변경27gate·12trips·원본4방향/공격/alpha),V219정상4slot재접속1,GroundTap/HUD,카메라helper/clip영향ContinuousMove/MillesSourceStyle/MillesGroundMaterial. 성장/전체스킬/장비 전체회귀는무관;원작숫자/드랍확정또는휴대폰수용주장없음. IMPLEMENTED;BUILD/HOST_NATIVE/PHONE/USER_VISUAL PENDING. V220별도후보.


### 전달 파일 영속 저장 확인

- PROJECT_DARK_V220_TEST.apk: libfile_a6fb4da66df48191a43c210e18d9a22b (saved; local metadata applied)
- DEATH_MAP_V220.png: libfile_a9214fcd43e88191a74b9982e19de966 (saved; local metadata applied)
- DEATH_MONSTERS_V220.png: libfile_e3d60c06e7088191a78379076759e4fb (saved; local metadata applied)
- V220_VERIFICATION.json: libfile_794fb97c15d08191803d29f5d0aae8ae (saved; local metadata applied)
