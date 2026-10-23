## V248 — 최종 후보 검증·APK 전달 완료

APK source `e38aeba15e34a7d9c797f16af7d8c8ed2922bb87`; [Actions39906861205](https://github.com/ChanJoos/Project_dark_android/actions/runs/39906861205) / job120260704047 SUCCESS, artifact12481077445. Final PortalServices3 + TempleMilles5 =8 PASS/0 failures/errors/skips. Exact-delta guarded retained32 PASS from eb7c55be/run39904714042 and 2df7cdf4/run39905956948:40 unique scoped checks, not a full-suite claim. Final71map/130portal groups,20source NPC roles, death-knight shop/recall checked. Prior21paid town routes/coast locking/cancel/poor/rollback/restart and adjacent navigation/save/cache/input evidence retained. Source pixel audit PASS. All71 final portal scenes plus temple capture inspected; old baked temple white beam removed, original32frame portal is common live overlay. Screenshot choice idle border cleaned with preserved original pixels.

`PROJECT_DARK_V248_TEST.apk` / com.projectdark.mobile.v248test; built2026-10-23 14:42:51 KST;106187803bytes; SHA256 `342b13004d95525c2d19b890196f03a46a7d3b2abcb36efeec6df4082fdb952d`. Downloaded artifact ZIP158454943bytes/SHA2565dcf5be84364304ae91164fa2d90a6869eb6f7a49592bcb93cf3aa1ff897d8b4; ZIP/APK CRC, embedded exact source/run, binary package/label, independent APKv2 RSA-SHA256 signature/certificate key/full content digest PASS.748packaged source files byte-identical (721skill+26town+NPC profiles). Saved APK successfully. Evidence: `verification/TOWN_PORTALS_V248_BUILD.json`.

IMPLEMENTED / BUILD_VERIFIED / SCOPED_HOST_NATIVE_VERIFIED. PHONE / USER_VISUAL PENDING. Existing PR197/branch codex/rucesion-coast-v157 unmerged; main41fe6def unchanged. Documentation closure does not change delivered APK source. Next: exactV248 handset feedback; no unchanged rebuild. Final main integration requires separate full gate. Prior CI_PENDING entries below are historical.

## V248 신전 최종 캡처 보정

2df7cdf4/run39905956948/job12025786302014nativePASS이나실제캡처에서흰빛이남아있음확인. 별도entrance_light는삭제됐으나originalentrance_platformcrop자체에흰빛이합성됨;기존포탈받침그림도제외하고공통EFA만사용,3stonearch/altar/충돌/입구그대로. Whitebeam밖픽셀반례검사를PortalServices에추가.26기존+직전NPC/travel6=32불변PASS재사용,portal3+temple5=8재실행/총40고유검사. 기존2dfAPK는전달하지않으며수정후exactAPK로대체. Finalsource/CI/APK/PHONE pending.

## V248 최종 화면·NPC 검사 보정

최초eb7c55be/run39904714042/job120253889351:35검사34PASS,1실패는NpcAppearance의예전마인이동role문구기대값. 요구된공통백원만/이동으로기대값정정. 모든71맵/130그룹·20원본NPC·21유료마을경로·rollback·무료리콜·재시작등통과. Native96PNG/71portal씬9contactpage검수. Idle선택창right9px에촬영원본backgroundHUD혼입확인:깨끗한left원본pixel을right에대칭하여정리/independentaudit확장. 밀레스신전기존흰빛effect도commonportal로통일;stoneplatform/arches보존.

26불변PASS재사용은CI변경경로/신전의단일effect삭제exactguard로고정. 최종feature/NPC9+직접영향Temple5=14검사재실행,최종unique40예정. CI결과/최종APK/PHONE pending. 불필요common39904721524는rootCHAT_HANDOFF분류누락으로시작되어exactrun/SHAguard취소39905022210;수정scope39905030371SUCCESS/compileSKIPPED. Runtime재검증우회없음.

## V248 — 원본 동적 포탈·시약/은행/이동 NPC (CI 진행)

사용자 첨부5사진+영상1000060741 기준. Hades mns035(시약),028(죽음상점기사),056(은행),019(백원만) 원본MPF/PAL 복원. Roh efct362.efa 전체32프레임38x120·125ms·pivot19,110·ADD 보존. 스크린샷 오른쪽선택창/portrait 프레임 RGB 그대로 추출, 내용부만투명화. master/source/town_v248/receipt.json 및rawzip 보관; tools/check_town_v248.py 전체pixel PASS.

활성/복원지원71맵의기존130입구그룹에공통원본애니메이션;지형캐시밖공통liveoverlay로1회draw,583680byte공유atlas,매프레임decode없음. 현재/신규마을시약·은행kind공통원본NPC. 실제모든기존이동NPC(6마을manager+2해안keeper) 백원만·우측선택창통일. 죽음대기실기사상점시약/리콜실제구매. 이동NPC100골드단일차감+원자save,실패시원래골드·살아있는actor/scene복원. 취소/잠금/잔액부족무과금,기존리콜/포탈무료·해금정책/밀레스46,39보존. legacyHorrorkeeper호환만유지,현재미배치NPC새로추가하지않음.

검증예정: 신규6native(전체71맵·130그룹·20역할pixel·21마을경로·해안잠금·취소·부족·rollback·죽음상점/리콜/재시작), 직접영향AllMapLabels/NpcAppearance/TownInterior/TownNavigation/F5mRestartPersistenceMatrix/ForestFloorCache/GroundTapPath. 로컬production·신규testcompile/sourceaudit PASS. CI·native화면검수·APK pending; PHONE/USER_VISUAL PENDING. SamePR197/branch,main41fe6def unmerged. 최종main통합은별도fullgate.

