## 2026-10-21 V217 죽음의마을 구현 후보 — 관련 CI 대기

Director 단일 편집. 기존 PR197/codex/rucesion-coast-v157, base828406914d494a2354462c35ed858c0ce6c1b8e8, main41fe6def 유지. 사용자 GitHub 명시 승인. 대기실·야외1개·건물3채/집안3개·지하A–E 총10씬. 건물1↔A↔B↔C/D↔E↔건물2, 건물3↔C 양방향. 던전 제외. 기존 World/Resolver/직접보상/저장 사용, 정적 맵/32chunkLRU/진입prewarm/출발release/좌표HUD/실제문·계단 latch. 공유 신전·장비·이동 cadence는 유지.

원본 Korean map archive 및 official741 HPF/MPF를 byte-preserved pack으로 보관. 205939 영상은 실제 존재/검토, 205405 영상은 첨부 전달 실패로 미검토. 야외/대기실 원본선택, 5개 인접맵의 연결 좌표, 미확인 몬스터 원래 한국명은 명시적 ADAPTED/PENDING. Editor text floor→인접 source 재질, 계단은 원본 돌재질로 만든 ADAPTED 독립오브젝트. 좀비 외3종은 원래 한국명으로 임의 재명명하지 않음. 수치/드랍/현재 원작 완전일치 주장 금지. 원본 main 던전 및 머미 미추가.

집중 검증:10씬/24방향 gate의 실제 이동·역방향·arrival bounce/held input, town카드20쌍·NPC, 원본4종/48pose와hit/curse alpha, shared combat exact-once reward,10씬 startup restore,12trip캐시. 공유 의존 때문에 NPC appearance·ground path·chase·save matrix 포함; 성장·스킬 전체 회귀는 이번 후보 범위 아님. 정확한 runtime SHA/CI/APK/hash는 성공 후 별도 기록. BUILD/HOST_RUNTIME/PHONE/USER_VISUAL PENDING. 계약 master/changes/V217_DEATH_VILLAGE.json, continuation docs/V217_DEATH_VILLAGE_CHECKPOINT.md.

Next: run exact-source scoped CI; fix actual failures; inspect native scene/door/stair/source frames; verify exact APK source/hash/signature. Missing205405 reference can revise source selection after reupload; no main merge or phone acceptance claim.

First runtime6e96409c03ae7d183802ff12912c699221ba1131 / run39612162569 FAILED at NPC registry closing parenthesis; no APK. Syntax fixed; source violet slime corrected176→105 from video silhouette. Java parser checks469files PASS. Current runtime17056366fc76b0a5a548517f380f4f5bc3a48afd / scoped39612638119 job119265241998 IN_PROGRESS; PR scope39612648588 passes exact reviewed bytes (full compile skipped). Local branch object aligned to remote exact UTC+0900 commit.

Pending source-only refinement: independent adapted stone stairs now have separated ascending treads instead of a raised stack. tools/build_death_v217.py and exact scope profile are local modifications, not yet published/built. Wait current focused evidence, fix actual failures, then publish final stairs and inspect exact native output/APK. Do not call first/current APK ready until tests and byte/signature/download checks succeed.

Second scoped39612638119 compiled successfully and ran20 tests:19 PASS,1 failure in loop fixture immediately reversing E→D while still standing on the deliberately latched arrival gate. All-map graph/population,held input,ten-scene restart,reward,48native pose/alpha,12trip cache and directly affected NPC/town/ground/chase/save tests passed. Fixture now actually leaves arrival beyond60px and re-approaches before expecting reverse entry, preserving bounce guard. Original assertion for arrival-no-bounce retained. First native artifact12351322270/SHA25637cedc662196d270d25379c9bc3158bc56261adda64bf94f4a3e51c35d54a11a. No APK because Gradle assembles only after tests pass.
