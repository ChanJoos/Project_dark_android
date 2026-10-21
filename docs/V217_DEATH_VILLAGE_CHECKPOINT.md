## 2026-10-21 V217 구현·빌드·호스트 실행 검증 완료 — 실기기/원작 미확인 부분 대기

Runtime037da0145022dade21701e635894c4de893c44f1, focused39613760032/job119269177599 SUCCESS: 신규7tests/0failures-errors-skips.10씬/24방향gate의 실제 분기 왕복,arrival bounce,ground tap/held joystick,source4종48pose·hit/curse alpha,combat reward exactly-once,10씬 startup restore,12trip캐시 통과. 직전17056366/run39612638119의 NPC/town20쌍/ground/chase/save 관련13PASS는 영향받는 production Java와build.gradle이 최종 소스와 byte-identical이므로 재사용. 최초6e96409 compile 괄호 오류,17056366 loop fixture1실패는 별도 보존; 최종7PASS로 수정 검증. 보호 latch를 제거하거나 기대값을 완화하지 않고 실제 arrival 칸에서 걸어 나왔다가 되돌아오도록 fixture 수정.

APK PROJECT_DARK_V217_TEST.apk / com.projectdark.mobile.v217test,2026-10-21 23:08:17.589KST,101,661,704bytes,SHA25661a58b7d0feb729cd605a87c3444de14db98df9adfe16f0376b753ca19720689. Artifact12352390155/ZIP118392992bytes/SHA256c60e0d0843f0cc4406b8973f8f8a8e112c62cd2a73ab17bbfe5780229d197e16. ZIP/APK CRC, embedded exact source/run/package,cryptographic V2signature+1MiBcontentdigest,824source-decodedassets+1ADAPTEDstairs 및 retained3154existingassets actual bytes 확인.22native captures와3door/3house/5underground/C↔house3stair/source-direction 검토. 독립 설치/저장으로 기존 앱 보존, 자동 save이전 없음.

최종12map-switch first-frame host p50=13.9221125ms,p95=23.1573608ms,p99=24.120654560000002ms,max24.361478ms; host통계이며 phoneFPS 아님. Current-map4variant atlas8,847,360bytes,32floorchunks/8,652,800bytebound 통과·scene출발zero. PRscope39613770995 SUCCESS, full compile 의도적SKIPPED. 기존PR197미병합/main41fe6def유지. Source/receipt verification/DEATH_V217_BUILD.json. 이 종료 기록은metadata-only이며 새APK재빌드 금지.

확인된 범위: 대기실→야외1개/건물3개→집안3개→A–E5개. 건물1↔A↔B↔C/D↔E↔건물2 및 건물3↔C. 던전미추가. 205939영상 확인;205405영상은첨부전달실패로미검토. 야외/대기실 source선택과연결배치,미확인한국명 및 원작수치/드랍은 ADAPTED/PENDING이며 원작완전복원 주장금지. 보라슬라임원본105를실제영상모양과대조해선택(녹색176제외);좀비외3종의원작한국명미확정. 다음:exactV217휴대폰 이동/사냥/계단 사용자 확인,205405재업로드후원작배치대조. PHONE/USER_VISUAL PENDING.

## 2026-10-21 V217 죽음의마을 구현 후보 — 관련 CI 대기

Director 단일 편집. 기존 PR197/codex/rucesion-coast-v157, base828406914d494a2354462c35ed858c0ce6c1b8e8, main41fe6def 유지. 사용자 GitHub 명시 승인. 대기실·야외1개·건물3채/집안3개·지하A–E 총10씬. 건물1↔A↔B↔C/D↔E↔건물2, 건물3↔C 양방향. 던전 제외. 기존 World/Resolver/직접보상/저장 사용, 정적 맵/32chunkLRU/진입prewarm/출발release/좌표HUD/실제문·계단 latch. 공유 신전·장비·이동 cadence는 유지.

원본 Korean map archive 및 official741 HPF/MPF를 byte-preserved pack으로 보관. 205939 영상은 실제 존재/검토, 205405 영상은 첨부 전달 실패로 미검토. 야외/대기실 원본선택, 5개 인접맵의 연결 좌표, 미확인 몬스터 원래 한국명은 명시적 ADAPTED/PENDING. Editor text floor→인접 source 재질, 계단은 원본 돌재질로 만든 ADAPTED 독립오브젝트. 좀비 외3종은 원래 한국명으로 임의 재명명하지 않음. 수치/드랍/현재 원작 완전일치 주장 금지. 원본 main 던전 및 머미 미추가.

집중 검증:10씬/24방향 gate의 실제 이동·역방향·arrival bounce/held input, town카드20쌍·NPC, 원본4종/48pose와hit/curse alpha, shared combat exact-once reward,10씬 startup restore,12trip캐시. 공유 의존 때문에 NPC appearance·ground path·chase·save matrix 포함; 성장·스킬 전체 회귀는 이번 후보 범위 아님. 정확한 runtime SHA/CI/APK/hash는 성공 후 별도 기록. BUILD/HOST_RUNTIME/PHONE/USER_VISUAL PENDING. 계약 master/changes/V217_DEATH_VILLAGE.json, continuation docs/V217_DEATH_VILLAGE_CHECKPOINT.md.

Next: run exact-source scoped CI; fix actual failures; inspect native scene/door/stair/source frames; verify exact APK source/hash/signature. Missing205405 reference can revise source selection after reupload; no main merge or phone acceptance claim.

First runtime6e96409c03ae7d183802ff12912c699221ba1131 / run39612162569 FAILED at NPC registry closing parenthesis; no APK. Syntax fixed; source violet slime corrected176→105 from video silhouette. Java parser checks469files PASS. Current runtime17056366fc76b0a5a548517f380f4f5bc3a48afd / scoped39612638119 job119265241998 IN_PROGRESS; PR scope39612648588 passes exact reviewed bytes (full compile skipped). Local branch object aligned to remote exact UTC+0900 commit.

Pending source-only refinement: independent adapted stone stairs now have separated ascending treads instead of a raised stack. tools/build_death_v217.py and exact scope profile are local modifications, not yet published/built. Wait current focused evidence, fix actual failures, then publish final stairs and inspect exact native output/APK. Do not call first/current APK ready until tests and byte/signature/download checks succeed.

Second scoped39612638119 compiled successfully and ran20 tests:19 PASS,1 failure in loop fixture immediately reversing E→D while still standing on the deliberately latched arrival gate. All-map graph/population,held input,ten-scene restart,reward,48native pose/alpha,12trip cache and directly affected NPC/town/ground/chase/save tests passed. Fixture now actually leaves arrival beyond60px and re-approaches before expecting reverse entry, preserving bounce guard. Original assertion for arrival-no-bounce retained. First native artifact12351322270/SHA25637cedc662196d270d25379c9bc3158bc56261adda64bf94f4a3e51c35d54a11a. No APK because Gradle assembles only after tests pass.
