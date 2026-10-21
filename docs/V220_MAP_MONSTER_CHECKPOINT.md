## 2026-10-22 V220 사용자 V219 거부 — 포탈/전체지도/몬스터 수정, 검증 대기

DEVICE_FAILED_USER_REPORTED/USER_VISUAL_REJECTED: 대기실·건물 이동 버튼 금지; 대기실8,0~11,0 포탈; 모든 마을 미니맵 터치 후 영상처럼 전체지도/현재위치/포탈을 보고 직접 이동; 회색좀비/집안의망자/슬라임 등 원본과 불일치한 외형·가명 거부. Director 단일 편집,기존PR197/main41fe6def 유지. 앞선V219호스트21PASS는 사용자 시각 수용이 아니었으며 전체완성 근거로 재사용 금지.

이동 버튼 및DeathVillageGuide삭제. 로비4gate8..11,0,귀환9,0;4칸을 묶은 arrival latch. 기존그리드4칸모두원본walkable. 일반ground/joystick접촉만 진입. 전체지도는실제맵원본렌더러로1024x512(2MiB) 한장,current map만 cache;static배경+건물/현재위치/실제입구pins. 미니맵/닫기만입력,지도터치자동이동없음. Milles/Abel/Rucesion/Mine/Deathlobby+field/Deathinteriors지원. 임시 overviewcamera는finally에서 exact기존camera객체복원;플레이어좌표/이동/충돌geometry미변경. Millesclip을Canvas실제clipbounds로 판정하여축소지도에전체건물이표시됨. 원본32chunkLRU를확대/소모하지않으며이동frame에지도재생성없음.

205405(2)영상을2초전체지도/23초야외/29초집안/43초지하15로 다시 decode하고 공식741설치본(208513633bytes/SHA256dbbd937d4dd1eed310b7b1de4c5c3d98e829ed77e5493b0857063528a47e1093)의953MPF 실제 원본 index를 재검토. 137회색석상/171후드기사/105보라슬라임 잘못된선택을166초록옷좀비/178붉은몸초록모자니크르/176초록거미로 교체,175단검든쥐인간은지하에만배치. 집안니크르+거미,지하웨어렛.좀비/니크르 이름은기존Master·Nexon4509/4515,웨어렛/독거미는Nexon호스팅목록721717와형태대조에근거한join(클라이언트한국어이름테이블 미확보,확정원작통계 주장금지). 새raw원본pack master/source/death_v220에보존,기존source는삭제안함. 4프로필ID/모바일수치/EXP·Gold/status정책 및72/15/36마리수 유지. 기존집/지하가명은사용안함.

관련검사: 신규4tests(네portal 실제접촉·whole-strip latch·6town originaloverview/camera/readonly/cache·MPF조인/지역분리/저장),기존V2177(10씬/변경27gate·12trips·원본4방향/공격/alpha),V219정상4slot재접속1,GroundTap/HUD,카메라helper/clip영향ContinuousMove/MillesSourceStyle/MillesGroundMaterial. 성장/전체스킬/장비 전체회귀는무관;원작숫자/드랍확정또는휴대폰수용주장없음. IMPLEMENTED;BUILD/HOST_NATIVE/PHONE/USER_VISUAL PENDING. V220별도후보.

