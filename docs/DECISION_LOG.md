## 2026-10-02 — V84 four town interiors implementation; verification pending

User requested reagent/equipment/bank/church interiors in tile → atlas → individual props → dressed base-body NPC → catalog → buy/sell UI order, following supplied five shop screenshots. Previous reagent failure is reproduced: screen-fixed single plate, camera/map identity mismatch, remote NPC interaction and illegal exit offset. New TownInteriorDef tile graphs and independent props use WorldRuntimeAdapter/collision/interpolation/depth; four existing doors lead to scenes (general-shop doorway is now bank). NPCs use the approved source BODY with registered garments. Reference wood interface has buy/sell or bank/service tabs, category, paging, details, quantity, total, explicit confirmation and durable result. Domain TownCommerce owns catalog/transactions; bank data persists in the same checkpoint as gold/inventory; save failure restores every changed channel. Church offers paid HP/MP recovery. Interior map and position restore at restart; exit chooses a legal exterior tile outside portal bounds.

Evidence distinctions: prop/tile art is PROJECT_RECONSTRUCTION from provided references; bank/church layout and NPC costume approximations have no supplied exact original evidence. Recall1000 is shown in reference; curanum50 retained; other prices are declared project balance. Reagent item semantics beyond existing use/learning contracts are unchanged. ImageGen source hash and screenshot hashes in verification/TOWN_INTERIORS_V84_SOURCES.json and packaged atlas.json. BUILD/NATIVE/PHONE/VISUAL: PENDING until exact-SHA CI and scene review. Active PR176 candidate; main unchanged. New tests cover four world doors, walk-to-NPC/confirmation input, collisions, legal exits, rollback, storage conservation and restart. No source BODY modification.

## 2026-10-02 — V83 abilities BUILD/NATIVE VERIFIED; phone pending

Recovered the interrupted V83 implementation after V82. CI36947486811 failed compiling ClassicSkillReferenceTest because it called nonexistent restoreBaseHpMp; use restoreBaseResources and applyDerivedGrowth for coherent fixture caps. Runtime formulas were retained.

IMPLEMENTED / BUILD VERIFIED / NATIVE RUNTIME VERIFIED: source54780b98a276ca0c741c67e9b94aff8be4458a8f, checkout552e884864126f44946763336b3664fddcd7e3eb; identical tree6f95b70bef02b24f5bb13339a81a4157b42f0769. Actions36954125312/job110673189388 SUCCESS, including all configured source regeneration, skill/UI/BODY/world/combat/growth/restart checks and assembleDebug. APK83/0.83-skill-abilities, artifact11204983666, SHA256 f396af5f278dfea89d82e36388532378af22da020f21d8a97ae9c2b808858760,29158837 bytes; build completed2026-10-02T11:13:21.911592+09:00 Asia/Seoul. All663 locally tracked packaged assets byte-match after ZIP filename decoding; original BODY source unchanged.

V83 tests exercise77 offensive actions through Resolver, distinct/stat-linked formulas, preserved classic finisher equations,16 heals, statuses/control/poison/expiry/save, defense type distinctions, once-only area resource snapshots, Dara release/cancellation and four-direction immediate Spin quickslots without selection or facing. Native artifact11205053316 contains681 PNGs; four new V83 full-scene Spin images visually inspected and show damage on four adjacent axes while diagonal/far fixtures remain unhit. V82 kick/Dara regression renders/tests remain enabled. Ragnarok and Complete Defense user acceptance is retained.

LIMITS:104 status policies and8 linked/passive entries are explicit policies, not104 independently certified original mechanics;16 SERVICE entries still reject because required party/travel/crafting/trade/summon/durability or original semantic systems are absent. Ordinary coefficients are user-authorized project balance; source anchors and adapted upgrades remain distinguished. PHONE and user acceptance of V82 kicks/Dara/V83 balance PENDING. PR176 is unmerged; main bfd668d4 unchanged. Next: install candidate ZIP, test damage/status/resource scenarios on phone, then resolve16 service mechanics through domain systems. Proof: verification/SKILL_ABILITIES_V83_BUILD.json.

## 2026-10-02 — V82 BUILD/NATIVE VERIFIED; kicks/Dara phone pending

Source `4bf789dc952403163fbc0aab6bc805186f75a28c`, tree `8177ecda01df08c33a49baa715449382a068fa1b`. Actions36942749572 /job110637808191 SUCCESS at that exact checkout. APK artifact11201490465, SHA256 `48612dec0648f4bdcee106af80a0cb34d84b6d29752a8e31356b7b7f4101bc1c`, 29124483 bytes, build completed2026-10-02T08:53:41+09:00 Asia/Seoul. Binary version82/0.82-martial-kick-release confirmed. All659 locally available packaged assets match bytes; all268 original BODY frames unchanged. Artifact/APK/native CRC and published archive digests verified.

Native artifact11201495497:679 PNGs, including272 V82 actor/live frames. All24 equipped kick contact actors (three kicks/two bodies/four actual facings),8 spin turn actors and8 Dara waiting/release/lower/end sequences reviewed. Physical front/side kicks use actual original d-group lifted-leg frames; spin traverses rear/front orientations. Dara pixels remain equipped standing through2.99sec and raises f1/f3 only at real3sec damage/recipient release; lowers and recovers. Configured source/catalog/skill/world/combat/growth/restart checks all pass. Earlier run36941351678 failed a test-only generic reflection varargs cast; run36941896830 passed. Final follow-up strengthened actual self-area starting-facing assertions; it changed only tests and final APK manifest/runtime/catalog bytes match that prior passing build.

V81 Ragnarok and Complete Defense are USER VISUAL ACCEPTED and retained unchanged. Original project sequencing, missing garment groups, other source gaps remain explicit. V82 kicks/Dara physical-phone and user visual acceptance PENDING; this is an unmerged PR176 task candidate, main bfd668d4 unchanged. Delivery includes exact APK, build proof and native-review contact/Dara mosaics inside ZIP. Full proof: verification/SKILL_FX_V82_BUILD.json. Next: phone revalidation of these four skills; preserve approvals and unresolved facts.

## 2026-10-02 — V82 user-reported kick BODY and Dara release repair

V81 Ragnarok and Complete Defense are USER VISUAL ACCEPTED per current phone report; exact installed hash was not supplied. V81 Dankak/Bungkak/Spin and early Dara raised arms are DEVICE_FAILED (user-reported). Root cause reproduced in the source: V80 selected c14–23 grounded warrior lunge/thrust frames for kicks, while original group d contains the actual lifted-knee/extended-foot martial sprites. Previous test merely confirmed the erroneous c selection; that assertion is superseded.

Accepted mappings: Dankak front kick d0/1/2 rear, d3/4/5 front; Bungkak side kick d10/11/12/13 rear, d14/15/16/17 front. Spin cycles these source side-kick orientations through a turn. Source frames are byte-identical; selected project timing still reaches contact at1/3. Dara keeps fully equipped male IDLE (female source IDLE) throughout the existing3sec preparation, raises f1/f3 only at actual release/contact, then lowers during recovery. Damage/contact/resources/reach unchanged. No source redraw. Related upgraded kick profiles inherit their corrected base family.

Consumed inputs: user report; original mm001/wm001 group c/d/f frames; labelled four classic GIFs and definitions; Asset_Animation_Frame_Master/Asset_Animation_Semantics; SKILL_ACTION_DECISIONS; generator/catalog; SkillBodyRenderer/GameView and current contracts. Governing Master correction: changes/USER-MARTIAL-KICK-RELEASE-V82.json. New tests use actual GameView input/clock for both bodies/four facings with equipped actor/whole-scene captures, standing-pixel equality during Dara wait, real damage/recipient effect at release, and lower/recovery frames. Exact-SHA CI/native/render/APK verification pending. PR176 remains unmerged task candidate; main bfd668d4 unchanged. Next: review these exact live images and deliver candidate ZIP only after checks, with physical-phone revalidation pending.

## 2026-10-02 — V81 Ragnarok BUILD/NATIVE VERIFIED; phone pending

Source `b35a69b09f163da91140a466adba5b971e0bb8e4`, tree `44ed3f78857e0c5b0c3f973316f4170998b6d160`. Actions36914678590 /job110545625507 SUCCESS; synthetic checkout `041606d21d771ebd4ea6eddb67a7382dce337757` has the identical intended source tree. APK artifact11188683570, SHA256 `52087b80cd86baae551d3312207fd500117bdf96e9ecf3553dcf4779ffadfd3c`, 29124235 bytes, built 2026-10-02T04:34:23+09:00 Asia/Seoul. Binary manifest versionCode81/versionName0.81-ragnarok-source confirmed; all662 locally available packaged assets byte-match source; artifact/APK/native ZIP CRC and artifact digests pass. Native artifact11188543667 contains404 PNGs; Ragnarok14 isolated phases and8 live frames reviewed. Three actual recipients receive visible source rings at real damage contact, six-point rune sigil/white flash appear centred, and pulses expire at810ms. Full configured skill/world/combat/growth/restart gates pass.

Four original user screenshots retained unchanged with hashes. GIF enlarges extracted frames3x; runtime true-alpha projection2x with filtered NORMAL blend. Capture order/crossfades/timing, ring tracing, central symmetry repair and violet field reconstruction remain explicit project processing; no recovered original-cadence or exact occluded-pixel claim. Other three fifth-circle original FX remain unresolved; source gaps32/Mage21. BUILD VERIFIED / NATIVE RUNTIME VERIFIED; PHONE and USER VISUAL ACCEPTANCE PENDING. PR176 unmerged candidate; main unchanged `bfd668d4e178fa82625d634b5a54be0e27ce30a3`. Deliver RAGNAROK_original_capture.gif and PROJECT_DARK_v81_RAGNAROK.zip (APK, GIF, transparent enhanced stills, source provenance, build proof). Exact evidence: verification/SKILL_FX_V81_BUILD.json. Next: install this exact candidate and review Ragnarok on phone; preserve remaining source gaps.

## 2026-10-02 — V81 Ragnarok user-source extraction (verification pending)

User labelled four retained screenshots as Ragnarok. Preserve original JPG bytes and SHA256 in master/source/skill_fx/user_ragnarok_20261002/provenance.json. Replace only SK_마법사_037 generic five-point star with capture-derived six-point hexagram, rune annuli, gold rings, white flash and purple field. Each phase combines clean views of the same screenshot; UI/actor/overlapping-spell masks, symmetry repair of the inner core, radial tracing of the ring-only phase and violet fill reconstruction are explicit approximations. Outer rune text is not generated. Four stills provide no original cadence; capture order, crossfades and 810ms lifetime are project-authored. GIF enlarged 3x; runtime true-alpha atlas 2x with filtered NORMAL blend and recipient visual-center registration. Other fifth-circle effects retain their V80 reconstruction.

103 classic skill routes /144 channels; all-source channels182; remaining independent particle gaps32 (Mage21). Damage/reach/cost/contact, BODY/basic swing/movement unchanged. New native regression checks source hashes, no green HP bars, transparent corners, visible onset/flash, real input damage contact, every hit recipient and expiry. IMPLEMENTED; exact-SHA BUILD/NATIVE and PHONE/VISUAL PENDING. Task PR176 remains unmerged; main bfd668d4e178fa82625d634b5a54be0e27ce30a3 unchanged. Continue exact tree CI/render review and deliver GIF plus APK ZIP.

## 2026-10-02 — V80 BUILD/NATIVE VERIFIED, phone pending

Source `d10e729f87253867efdb1d123122fb710ebe235a`, tree `d3b97f27a50ad415b0b03240fe836ae21c3f2df6`. Actions36905998670 / job110516571548 SUCCESS; CI synthetic checkout `315f70d95b8f07c6e0318e70e60fd945b991162b` has the same source tree. APK artifact11183672728, native artifact11183587827 (382 PNGs). APK SHA256 `f6f52fa0d23ba2629062feb30936901b74435677b85bfa89c1775b4cd4dd7d52`, 28681206 bytes, built 2026-10-02T03:25:22+09:00 Asia/Seoul. All661 locally available packaged assets match bytes; versionCode80/versionName0.80-martial-defense-star confirmed. CRC checked for artifact/native/delivery ZIPs.

Native gates pass: all34 martial selections ×2 source bodies ×4 directions ×5 phases; real kick input/body clock before/contact/recovery; full source ellipse contains head/arms/feet with Defense project scale.31 and foot offset-35; all four fifth-circle spells emit immediately visible star on actual damage recipients, including screen Ragnarok multiple targets. Final Defense8 views, kick6 frames and finisher4 contacts inspected. Existing source, skill, world, combat, growth and persistence gates pass. Earlier run36903942008 failed new fixture camera/channel assumptions; run36904644965 passed but was superseded after enlarged review showed insufficient Defense hat clearance. Final source above includes clearance and stronger ellipse checks.

V79 DEVICE_FAILED (user-reported) remains; exact installed SHA unknown. Original source review24 martial entries,6 original caster motions not demonstrated,3 source-occluded and1 unresolved Pachungak cast remain open. Stars are user-confirmed motif/PROJECT_RECONSTRUCTION; original pixels/color/cadence and33 original particle gaps remain unverified. BUILD VERIFIED / NATIVE RUNTIME VERIFIED; PHONE and user VISUAL_ACCEPTED PENDING. PR176 unmerged task candidate; main unchanged `bfd668d4e178fa82625d634b5a54be0e27ce30a3`. Delivery PROJECT_DARK_v80_download.zip contains APK, BUILD_INFO and proof. Full exact evidence: verification/SKILL_FX_V80_BUILD.json. Next: install this exact candidate on phone and review reported scenes; do not certify unknown originals or merge without acceptance.

## 2026-10-02 — V80 martial motion, Defense envelope and fifth-circle stars

V79 DEVICE_FAILED (user-reported): Complete Defense head outside sphere, martial skill motion mismatches, Mage fifth-circle missing original star motif. Exact installed APK SHA unconfirmed. Reproduced metadata defects: Defense was centered at feet minus23, leaving only55px above feet; kicks selected their partial preparation frame at damage contact and full extension later; finishers034–037 used generic12-ray burst. V80 retains Defense66 frames, uses project scale .31 (source .27 retained in provenance) and explicit foot offset -35. Kick profiles reach full source extension at normalized contact1/3. Martial34 entries reviewed against labelled sources; Changpung uses punch, Guyang uses raised arms, Heupjeong keeps standing, Dara holds charge then releases, Triple Punch cycles three source punches. Unlabelled/occluded caster motions and exact original cadence remain open in verification/MARTIAL_MOTION_V80_AUDIT.json.

Mage Semellia/Armageddon/Summa Stella/Ragnarok now use a visible-at-contact five-point star on every actual hit recipient. Star motif is user-confirmed; geometry/color/cadence are PROJECT_RECONSTRUCTION, not recovered original pixels. Original finisher particle source gaps remain open. Damage/reach/cost rules, basic BODY-only swing, original body/source bytes and approved movement unchanged. New native tests exercise live kick input/clock/contact, both source bodies/four directions for all34 entries, Defense envelope and four real finisher contact events. BUILD/NATIVE/PHONE/VISUAL PENDING until exact-SHA evidence. PR176 task candidate; main unchanged; delivery ZIP with APK.

# PROJECT DARK 결정 및 제안 기록

## 2026-10-02 — V79 verified contact-FX candidate

IMPLEMENTED / BUILD_VERIFIED / NATIVE_INPUT_VERIFIED. Source `9aa7dbeedeebd9f93c92ed88fb2dc9bf22322edf`, tree `b687575e3f4b9e642dec11bbf343e72f16994fb7`; actual CI checkout `637899226031486b01a111b0a21f36efe8a15499` has identical tree. Actions 36887739073 / job 110455250429 SUCCESS, build 2026-10-02 00:58:42 KST; APK artifact 11175715710, SHA256 `8b2a32837e4b56bf61a5925920f3d0f74ee7bb11b003de16f9b325f491baad31`, 28680090 bytes. Native artifact 11175845458 contains 295 PNGs. The 52 contact/crown/midpoint regression images match the reviewed same-runtime run byte-for-byte. All 661 locally available packaged asset files match tracked source bytes (ZIP Unicode filenames normalized for comparison).

Dara recipient contact, Holy Dragon immediate visible onset including lethal contact, four shared full-skull hue variants, both Crasher midpoint projections, BODY-only basic attack pass the production-input/native regressions. Full configured world/combat/growth/restart gates pass. Two initial contact-test assumptions used catalog basic timing and arbitrary captured phase; corrected to equipped action contact and actual alpha-energy peak. Forest test formerly required a basic impact; updated to user-approved no-particle rule while retaining real damage/hit-tint checks. Runtime and assets did not change after those test corrections.

V78 DEVICE_FAILED (user-reported) remains historical evidence; exact installed SHA unconfirmed. V79 physical-device and user VISUAL_ACCEPTED remain PENDING. PR176 unmerged, main unchanged `bfd668d4e178fa82625d634b5a54be0e27ce30a3`; this APK is a task-branch candidate. Delivery ZIP contains PROJECT_DARK_v79.apk and BUILD_INFO.txt. Next: install this exact APK and confirm the five reported scenes on phone; original particle evidence gaps33 remain open. Full proof: verification/SKILL_FX_V79_BUILD.json.

## 2026-10-02 — V79 contact FX correction (implementation; verification pending)

V78 DEVICE_FAILED (user-reported): Dara recipient location, Holy Dragon delayed onset after lethal damage, cropped curse skulls, Crasher midpoint placement, unwanted basic-attack particles. Installed APK SHA remains unconfirmed. Diagnosis reproduced in source metadata/render path: Dara was caster-anchored; Holy Dragon included a 1000ms blank recording lead-in; curse crop/masks discarded the crown; Crasher used recipient registration; generic impact included basic attacks. Prior peak-only review did not validate effect onset.

User-approved presentation corrections: Dara recipient visual center with full source burst; Holy Dragon first visible source frame begins at resolved damage contact, recording lead-in omitted with original frame indices preserved; four curses share the full labelled Depreco skull capture with explicit project hue variants per user-confirmed same-shape family (individual labelled sources retained as evidence); both Crashers use peak-energy pivot at caster/recipient visual-center midpoint; basic remains equipped BODY swing with no particle channel. Damage, contact rules, costs, source bytes, BODY and movement unchanged. Curse still-capture cadence remains project hold/fade and source occlusion remains unresolved. Remaining independent particle gaps33; basic is deliberately BODY-only.

Active PR176/branch codex/original-skill-contract-v65, base d9893818898dcb6d8b2954edf2ddb23f9219a56c; main bfd668d4e178fa82625d634b5a54be0e27ce30a3 unchanged. New native regression advances production input from before contact through 1100ms, includes lethal Holy Dragon, no basic particles, both Crasher centroids in four directions and full curse crowns. Exact-SHA CI/native screenshot review/APK pending; physical device and user visual acceptance PENDING. Deliver ZIP containing APK because phone UI did not activate APK-only links.

## 2026-10-01 — V75 full source FX audit / verified candidate

IMPLEMENTED / BUILD_VERIFIED / NATIVE_INPUT_VERIFIED. User's previous visual DEVICE_FAILED report remains historical failure evidence; physical-device and user VISUAL_ACCEPTED remain PENDING. Exact source `d3c94924ed4ceab0f5ad0685eb905c174f899730`, tree `9f6a0614f52e08ed8f3aa759aec601e1b4bc935d`; CI36822168528 / job110239897761 SUCCESS. Actual PR checkout `10ad42ef237683b20f09e33f1bdd40f22cd5e7a6` has the identical tree. Build2026-10-01 15:00:30 KST, version75 /0.75-source-fx-audit. APK artifact11144092490, SHA256 `854a622b2d4bec91aa3565af4c09210484d2063110ea6c9e53b944fab87cc58a`,25,602,800 bytes; native artifact11143848607 with206 PNGs. PR176 stays unmerged; main stays `bfd668d4e178fa82625d634b5a54be0e27ce30a3`. This is a task-branch candidate APK.

All221 routes audited:95 select retained original source records, of which89 contain116 particle channels/81 unique channel source files and6 rogue demonstrations have no separately recoverable channel.86 use project fallback (85 standalone-presentable),40 declare no particle. The earlier shorthand “95 source effects” must not imply95 particle animations. All95 selected sources/projections were visually compared; eleven tiny original Cleric caster particles were confirmed and retained. Three Dara/Guyang/Dalma alternate source variants remain shadowed by classic sources, with selection unresolved. Full CSV/JSON and SKILL_FX_AUDIT_CONTRACT distinguish each case.

Mad Soul/Jin now snapshot actual caster→recipient contact direction and orient the single-facing capture toward it. Crasher and other captured finishers scale .44→.32 to match source actor/BODY registration; original occluded holes are not painted. Complete Defense preserves original RGB, NORMAL blend and bright rim, excluding only explicit actor-marker circles; its static hold/fade remains project cadence. Dankak/Bungkak retain original cyan/bright contact particles and remove false split caster starts. Audit also found Balgyung's single-actor blue crystal wrongly split toward a monster; it is now source-self anchored, with cold source particles retained. Costs/damage/gameplay domains remain unchanged.

Verification:221 IDs×4 directions renderer-route/anchor/dedup native mosaics (synthetic events, not all-skills mechanics acceptance); six reviewed skills×4 actual production quickslot inputs with24 peak-visible native frames; exact Defense rim/NORMAL pixel regression and Mad Soul forward-energy checks. All configured source-regeneration, original-input, source window, BODY, world, VFX, combat, growth, runtime/restart gates passed. Final24 frame bytes match the already inspected same-runtime run. Failed earlier gates were stale source-coverage output and the old test expecting a false Dankak start; corrected expected source contracts, no runtime restoration of bogus particles.

APK829 assets:four projected PNGs andthree manifests changed, three obsolete caster atlases removed;822 remaining assets unchanged against verifiedV74. All576 locally available assets match final tracked bytes,253 recovered assets match verifiedV74 unchanged. Original GIF/JPEG bytes, approved BODY, skill/all-job tabs, inventory/equipment skins and world remain unchanged. Full evidence:verification/SKILL_FX_V75_BUILD.json. Remaining work is physical installation and visual acceptance of exactV75,86 fallback original coverage, source actor/UI mask losses and occlusion, alternate versions, one-facing evidence, previous61 icon/9 inference/133 service gaps. Audit is complete; all-original pixel-identical reconstruction is not certified.


이 문서는 승인된 결정을 아이디어와 구분합니다. 상세 규칙은 인용된 원본 문서가 권위 문서입니다.

## 확정된 프로젝트/개발 결정

| 결정 | 범위와 의미 | 근거 |
|---|---|---|
| 개발은 설계→데이터/출처→런타임→플레이 장면→APK→기기 증거→수락을 구분한다. | 클래스 구현이나 컴파일만으로 기능 완료라 하지 않는다. | `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md` |
| 구현, 빌드 검증, 기기 검증, 시각 수락은 별도 상태다. | 증거가 없는 단계는 미확정으로 둔다. | `AGENTS.md`, `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md` |
| 승인된 외형/동작과 모듈 소유권을 보존하고 기존 계약을 먼저 조사한다. | 관련 도메인 계약과 검증을 확인한다. | `AGENTS.md` |
| 원작 근거가 없는 사실은 `UNRESOLVED`로 두며, 대체안은 그 사실을 명시한다. | 제안이나 임시 fixture를 원작 정사로 쓰지 않는다. | `AGENTS.md`, `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`, `master/MASTER_LAW.md` |
| 포테 몬스터 v0.4 묶음은 검토용이다. | 파일 저장만으로 APK 적용이나 시각 수락을 뜻하지 않는다. | `0506929`, `assets/pote/review/monster_concepts_v0.4/README.md` |
| APK는 활성 작업이 포함된 SHA에서 빌드한다. | main과 활성 task branch를 비교하고, 미병합 브랜치 빌드는 후보라고 표시한다. | 2026-09-28: main `0506929` 산출물은 Pote navigation 결함이 있는 구버전이었고, 수정은 `codex/pote-ground-tile-foundation`에 있음 |
| Artifact/SHA 일치는 기기 수락을 대신하지 않는다. | 사용자가 요청한 화면/조작 시나리오를 기기에서 별도로 검증한다. | `AGENTS.md`, `docs/CHAT_HANDOFF.md`; 2026-09-28 사용자 실패 보고 |

## 아이디어·기획 목표 (승인/구현 여부 별도 확인)

다음은 기획 목표 또는 검토 항목이며, 현재 기능이라는 뜻이 아닙니다.

- 원작 세계와 규칙을 근거 기반으로 모바일에서 구현하고 설치·저장 복구·성능·원작 비교를 검수하는 방향.
- 밀레스 이동→NPC 대화→전투→보상→성장→저장→재시작 복원 플레이 루프.
- 직업·성장·전투·퀘스트·UI·경제·온라인 기능을 단계적으로 구현하는 항목.

세부 제안과 원작 근거는 `master/PROJECT_DARK_V0.6_PLAN_KO.md` 및 관련 출처/SSOT 문서에서 항목별로 확인합니다. 미병합 Pote branch의 통합 여부는 기기 검증 및 변경 검토 전까지 미정입니다.

## 이력

새 결정/변경만 날짜와 근거를 붙입니다. 구현 상태는 `docs/PROJECT_STATE.md`, 작업 순서는 `docs/DIRECTOR_BACKLOG.md`에서 관리합니다.

## 2026-09-29 — Pote auto-target / walk presentation follow-up

| 결정 | 범위와 의미 | 근거 |
|---|---|---|
| 몬스터 walk 방향은 AI의 원래 추적 의도가 아니라 충돌·detour를 거쳐 실제 적용된 canonical 타일 벡터로 선택한다. | 진행 중인 walk pose는 stale attack-facing을 무시하며, 네 방향 Pamfet 자산의 실제 시선은 기기 확인 전까지 수락하지 않는다. | 사용자 보고; `PoteForestMonsterShowcase`, `MonsterDiagonalLocomotion`, `RuntimeState`; `docs/MONSTER_CREATION_MANUAL.md` |
| 자동공격 타깃은 도달 가능한 합법 인접 타일까지의 최단 경로 수로 고른다. | 같은 경로 수에서는 직선거리를 tie-break로 사용하고, 선택된 유효 생존 타깃은 쓰러지거나 경로가 끊길 때까지 유지한다. 이동 중 타깃은 예약 도착 타일을 계획점으로 사용한다. | 사용자 보고의 빙글돎/비효율 타깃; `WorldMoveTargetController`, `WorldRuntimeAdapter`, `GameView` |
| 사용자 캡처에서 전사한 구클라이언트 요구 조건은 원본 92-sheet hash 검사 대상과 구분한다. | `Skill_Evidence.csv`의 SE10은 구조 검증 canonical override, 상세 표는 `Skill_Legacy_Requirements.csv` canonical addition으로 추적하며 원본 XLSX fidelity를 주장하지 않는다. | Main `c2e60d6`의 validator failure `36586971014`; `tools/validate_master.py`; 사용자가 제공한 캡처 전사 설명 |
| 최신 skill 캡처 반영으로 갱신된 `Skill_Master`, `Skill_Requirements`, `Skill_Research_Audit`도 원본 XLSX hash 검증과 구분하고, canonical override로 schema/header와 행 폭을 검사한다. | `Skill_Evidence`의 구형 6-column 증거 행은 27-column schema로 확장하고 기존 필드는 그대로 유지한다. | Main `50c65cc`와 validator integrity audit; 원본 XLSX 미확보 |
| 몬스터 장애물 추적은 매 걸음 greedy 방향을 다시 시도하지 않고, 합법 타일 그래프의 최단 melee 접근 경로를 따른다. | 장애물 detour 중에도 다음 타일을 재계산할 때 안정된 최단 경로를 고른다. | 사용자 보고; `MonsterDiagonalLocomotion.select` source audit; `MonsterChasePathfinder` |

## 2026-09-28 — Pote forest monster visual test scope

| 결정 | 범위와 의미 | 근거 |
|---|---|---|
| 사용자는 현재 Pote 숲에 정의된 모든 몬스터를 배치해 외형/상태별 포즈를 테스트하도록 요청했다. | Master roster의 16 identity를 검토용 v0.4 14-form pose pack에 연결하는 것은 B/ADAPTED test deployment다. 위치/개수/HP/미확보 개별형 외형은 원작 사실로 승격하지 않는다. | 사용자 요청(2026-09-28); `PoteMonsterRoster.java`; `assets/pote/review/monster_concepts_v0.4/README.md` 및 `manifest.csv` |
| 시각 테스트용 `POTE_SPIRIT_TEST_B`는 canonical spirit 보상과 분리한다. | 40 HP 테스트 개체가 canonical reward를 발생시키지 않도록 isolated runtime ID를 쓴다. canonical Master 행/보상 데이터는 변경하지 않는다. | `PoteForestMonsterShowcase.java`; `master/data/Monster_Master.csv`의 `POTE_SPIRIT` row |
| 단일 walk/attack pose를 루프 애니메이션으로 기록하지 않는다. | 방향/state 이미지 스왑과 실제 AI 이동, 공격 상태 표시를 테스트하며 여러 프레임 순환은 별도 자산 확보 전 미구현이다. | v0.4 review README/manifest; 구현 범위 |

구현/CI/APK/device 상태는 `docs/PROJECT_STATE.md`의 작업 기록으로 추적한다. Strong 변종 외형 공유는 테스트 편의를 위한 비정사 mapping이며, 원작 외형 확정이 아니다.

## 2026-09-28 — 사용자 화면 피드백에 따른 시각 수정안 (기기 미수락)

| 구분 | 내용 | 근거 / 상태 |
|---|---|---|
| 사용자 요구 | 몬스터 크기를 플레이어에 맞게 줄이고, 어색한 공격 모션과 접근 전에 공격하는 듯한 인상을 바로잡으며, 강력한 놀의 깨져 보이는 그래픽을 확인한다. | 2026-09-28 사용자 화면 피드백. 보고된 설치 APK SHA는 미확인. |
| 테스트 렌더링 수정 | 포즈 PNG의 투명 여백을 기준으로 실루엣 높이 44 논리 픽셀, 비율 유지; 공격 포즈에 인공 돌진/회전은 추가하지 않고 제공된 단일 대표 포즈를 표시한다. | 구현 commit `8ca5fda1c2483589b6f16fc9800bb9f87597ec23`; Actions #36393396473 성공, APK artifact `10957720462`. 기기 결과 대기. 이는 테스트 장면 렌더링 값이며 정사 몬스터 스케일/공격 애니메이션 결정이 아니다. |
| 미확정 | 강력한 놀의 별도 그림 필요 여부와 추적 반경 변경 여부. | 현재 pack에는 별도 strong-GNOLL artwork가 없어 기본 GNOLL concept를 공유한다. 새 APK에서 크기/선명도를 보고한 뒤 결정한다. |


## 2026-09-29 — 몬스터 이미지 개별 파일 형식

| 구분 | 내용 | 근거 / 상태 |
|---|---|---|
| 확정된 출력 형식 | 몬스터 시트 대신 몬스터/포즈별 개별 이미지로 만든다. 전체 요구는 비강력형 13종 × 정지/공격/이동 각 4방향 = 종당 12개 이미지다. | 사용자 명시(2026-09-29), 앞서 확정한 12컷 규격. |
| 이번 생성 | 팜팻 구르기 공격 1장, 기본 직립 늑대인간 대기 1장, 갑옷·칼 늑대인간 대기 1장. 모두 컨셉 초안이며 종 ID 배정·시각 수락·런타임 적용은 미정/미실시. | 생성 입력은 Library: 팜팻 승인 외형 libfile_d213c79680108191b4f13b8bc89b68de, 늑대 이미지 libfile_1c5a4161d2b08191be701bf0f89b463f 및 libfile_f65c74247704819180f7e8bfa212fee3. |
| 참조 제외 | 이전 168장 콘셉트 PNG와 사용자 반려 생성 시트는 새 이미지 생성에 사용하지 않는다. | 사용자 명시. |


| 생성물 QA 상태 | Pamfet의 원형 구르기·잎은 표현됐으나 보라색 후광이 제거되지 않아 미수락. 무장 늑대 재생성은 칼 길이를 줄였지만 여전히 컨셉 후보. | 최신 파일 이름과 SHA-256은 `docs/PROJECT_STATE.md`의 “이미지 자체 검수 추가”; 사용자 시각 수락 전. |


## 2026-09-29 — Pote 생성 후보의 테스트 APK 등록 승인

| 구분 | 확정 내용 | 근거/범위 |
|---|---|---|
| 이번 테스트 APK | 기존 16 identity의 이전 `monster_test_v04` runtime 이미지 연결을 해제하고, 현재 완성된 5개 생성 후보 세트만 idle/walk/attack 4방향 렌더 경로에 등록한다. | 사용자 요청(2026-09-29); 이번 테스트 APK 범위에 한한 승인. 신규 아트의 원작 정사/시각 수락 승인과 구분. |
| 등록 종 | 퍼플/레드/그린/실버 팜팻 및 라이칸스로프, 각 12개 대표 포즈 = 60 PNG. | 현재 저장소의 각 파일 실재 확인. `monster_artwork_manifest.csv` runtime registration columns. |
| 미등록 종 | 트랜트(9/12), 나머지 미완성 종 및 강력한 변형은 등록하지 않는다. 없는 방향·상태를 복사/추정하지 않는다. | 현재 소스 파일 확인; 사용자 이전 결정에서 강력한 변형 제외. |
| 아트 상태 | 프레임 PNG는 생성 후보의 단일 still pose다. 상태/방향에 따라 교체하며 다중 프레임 루프는 구현하지 않았다. | 생성 원본 및 `PoteFieldRenderer` 구현 계약. |
| 구버전 보존 | 기존 v0.4 자료는 review 이력으로 남지만, production source set과 APK에는 포함하지 않는다. | Android `app/build.gradle` production asset roots 및 삭제한 `assets/pote/production/monster_test_v04/`. |


| 테스트 APK 검증 결과 | GitHub Actions run `36534954426` / source `1def7a92aeef68ce144ebaad8b94f02dee011a12` 성공. versionCode 49 artifact `11017989115`; APK SHA-256 `9b8cb7e86c7b9c89b34667199711b1738f72aa9267b6edbb79743c934b18c799`. APK 안에서 신규 PNG 60개, 이전 v0.4 PNG 경로 0개를 확인. | CI artifact inventory 및 Pote pose/placement tests; 실제 기기 시각 QA는 별도 미완료. |

## 2026-09-30 — 승인된 스킬창 구현
사용자 “구현해”: 인벤토리 프레임 재사용, 기술/마법·전체/습득 목록, 상세·사용·퀵슬롯·저장. 평민 임의 습득 없음. 캡처 구 조건은 참고 표시, 습득 강제 적용 없음. 미구현 효과는 구현 예정. 4번째 utility 버튼을 스킬로 바꾸고 퀘스트는 기존 tracker를 통해 접근한다.

## 2026-09-30 — 캡처 습득 조건 적용 및 전투/슬롯 연결

사용자 선택 **스크린샷 조건 적용 (Recommended)**을 받았다. 이전의 ‘구 조건 참고 표시·습득 강제 적용 없음’은 이번 프로젝트 습득 규칙에 한해 변경된다. 직업·캡처 능력치·명확한 선행 숙련도를 검사하고, 불명확한 추가 조건은 추측하지 않는다. UI 흐름은 습득→퀵슬롯 등록→필드 사용이며 본문 탭 순환을 제거한다. 등록/해제와 저장은 배운 ID에 적용하고 미구현 효과는 명시한다. 29개 액션의 수치는 B/ADAPTED 시험 밸런스이며 원작 값/속성/특수효과 수락이 아니다. 기본공격 슬롯은 기존 무기 공격과 쿨타임을 공유한다. 성공 액션의 숙련 +1/100 cap은 이번 구현의 ADAPTED 훈련 규칙이며 원작 실패 확률은 미정이다. 출처/구현/검증은 `SKILL_ACQUISITION_CONTRACT.md`, PROJECT_STATE SKILL-WINDOW-03, source `30cb18baa2268c60067bd3f6d33e921b935154f8`를 따른다.


## 2026-09-30 — v60 모바일 스킬 습득 규칙 변경 (최신 사용자 지시)

사용자는 숙련도 조건을 제거하고 원작 습득 규칙 대신 스탯과 돈/필요 아이템으로 스킬창에서 직접 배우도록 확정했다. 기존 캡처 직업·승급·서클·레벨·선행 숙련 조건은 프로젝트 학습 제한으로 더 이상 적용하지 않는다. 직업은 목록 분류일 뿐이다. 스탯은 FinalStats, 재료는 현재 inventory, 돈은 Gold로 판정한다. 습득 버튼에서 한 번 검증·결제·학습·저장하며 NPC/추가 확인을 요구하지 않는다. 창 열기만으로 자동 결제/학습하지 않는다.

Gold 등급 가격, 6개 재료 recipe 및 미확정 스탯 tier fallback은 이번 구현의 PROJECT_ADAPTED_V60 밸런스 선택이며 사용자 확정한 원작 수치로 기록하지 않는다. 별도 mobile_learning.json으로 원본 Master/캡처를 보존한다. 명시적 설명/습득 조건 탭, 통일된 중앙 정렬, cyan source corner presentation mask가 현재 UI다. 모든 효과를 구현했다고 주장하지 않고 미구현 효과는 표시한다. 구현/검증은 source `e0f61bf1712e7b18ece4c9602e5dbf82da0bf6f3`, PR #171, SKILL_ACQUISITION_CONTRACT.md 및 PROJECT_STATE의 SKILL-WINDOW-04를 따른다.


## 2026-09-30 — v61 일괄 모바일 스킬창 품질 수리

사용자가 출시 품질 검토의 문제들을 한 번에 처리하도록 승인했다. 실제 효과 있는 30개를 기본 목록으로 제공하고 미구현은 명시적 자료 보기로 보존하되 결제/습득을 막는다. 기존 ID/배운 기록은 삭제하지 않는다. 스탯+돈/필요 아이템 학습과 직업/숙련 제한 제거는 유지한다. 원본 없는 아이콘은 project Canvas emblems로 명시적으로 표현하며 다른 스킬 아트를 빌리지 않는다. 큰 카드/상태 필터/분류 dropdown/창 안 결과와 오류 안내/슬롯 강조가 현재 UI다.

기존 재료 경로가 가격null/구매 입력부재로 막혀 있던 supported recipe를 위해 쿠라눔에 프로젝트 ADAPTED 50G 가격과 atomic purchase를 연결했다. 원작 가격 확정이나 다른 미확정 재료 가격 승인으로 해석하지 않는다. 훈련25G 보상 기준 첫 기술2회, 쿠라눔+학비8회 Gold 상당을 검사했으며 실제 플레이 세션 경제 수락은 남는다. 회복은 FinalStats WIS와 표시값을 맞춘다. source `89bad5a620324f9e5b0b3c8fbc7191fe59fb2a1f`, PR #172, SKILL_ACQUISITION_CONTRACT.md / PROJECT_STATE SKILL-WINDOW-05를 따른다.


## 2026-09-30 — SKILL-WINDOW-06 v62 preferred grid restoration

User prefers v60's 4×3 grid over v61's six-card layout. Restore centered icon/name cells, seven direct job tabs and compact description/requirements frame. Preserve v61's 30 supported actions, unsupported no-charge archive, stat/Gold/material atomic learning, Curanum purchase, FinalStats healing, slot checkpoint rollback and shared cooldown. Production-input tests follow the restored coordinates. Source af63d7d24e3734700cfd414ddac65f96b543ac2c; PR #173; Actions 36667530858 SUCCESS. Physical device and user visual acceptance pending. Previous v61 layout is user-rejected, not a new combat failure report.

Final verification: Actions 36667530858 / job109735322078 SUCCESS, source af63d7d24e3734700cfd414ddac65f96b543ac2c; actual synthetic merge checkout cf46ea11259232588035483637872f0a7d6501bd into main48037b694b5a034e5851c1a240b61cd05dc81682. All 16 SkillWindowTest cases and configured regressions/assembleDebug passed. APK artifact11076641254, versionCode62, built2026-09-30 13:10:54 KST, 13,069,271 bytes, SHA25626e5da74bc6e743fd3decfd9719cde101983f3ae2573ef0201dc32a362a49229. Packaged learning policy/icons/captures match retained source bytes. Render artifact11077080799 overview/shop-learned/cooldown reviewed: centered grid labels, direct tabs, completed costs, slot feedback and shared cooldown visible. Physical-device and user visual acceptance remain pending. Closure commit changes documentation only.


## 2026-09-30 — SKILL-PRESENTATION-01 v63

User-authorized source male/female pose decisions and separate caster/recipient VFX. 219 ID mappings/268 unchanged source frames, newly generated ADAPTED atlases. Current30 combat actions connected; archived189 do not become playable. Missing garment-group motions use existing standing layer with explicit adapted registration. No finished sex-selection UI. Exact original FX mapping count0; web search/reference gaps and source/status distinction in SKILL_PRESENTATION_CONTRACT.md. Build/runtime evidence follows; physical-device/user acceptance pending.


Verification: implementation source ee643167680d83de47cc57a073610df4bb137ed7; PR #174; Actions36682111983/job109779630434 SUCCESS. Actual PR checkout1b377c71cc18d3d2f4482b885dc15d8a7dc8d7bd merges that source into main8525060cee3fec3c4fa8ab3d60bb02d8d0f19b60. Catalog regeneration, 4 SkillPresentationTest cases, 16 SkillWindowTest cases and all configured regressions/assembleDebug passed. Body source268/268 byte equality verified. APK artifact11082039016 (uploaded2026-09-30 16:11:59 KST), versionCode63, 18,693,002 bytes, SHA25619ce62df1a6f4629ae7a99b4219c718f86a2910e078fd60d594e07797ccac542. All273 packaged presentation files match retained source bytes. Native render artifact11082541202 reviewed: both genders/four directions/seven pose families, monster fire/blunt impact and player heal anchors visible. Static garment registration and original source semantics remain ADAPTED/UNRESOLVED; no full visual acceptance claimed. Physical-device/user acceptance pending. Closure changes only documentation and an additional pre-impact caster capture fixture; app runtime/assets stay identical to the verified implementation source.


## 2026-09-30 — SKILL-TEST-02 v64

User requests beginner testing of every skill and questions cross-job availability. Supersedes the previous stats/Gold-only cross-job permission: normal learning/use and default available list now enforce current job plus common skills. Old learned entries remain in save but cannot bypass job. Activity starts test mode ON by default (saved toggle honored). 219 real entries are virtually learned/test-accessible; separate test quick slots persist outside the normal book, leaving actual learned/proficiency/slots untouched. Test combat30 uses existing Resolver with MP cost refunded; other189 are explicitly visual-only previews with no invented damage/status/teleport/summon mechanics. All magic uses original f1 rear/f3 front overhead two-arm charge/contact, f0/f2 recovery, both sexes. UI distinguishes test combat support from visual-only and has ON/OFF. No Master numeric original claim. CI/native/physical-device evidence pending.


Verified implementation76422fa364a6936234b7c6260714a07e64d27988; PR175; Actions36691264703/job109808785633 SUCCESS. PR checkout6d1d51aab131248c2a2c9cef4948075b6d28b18b merges implementation into main89c49b39ab44ca4106a4a91b6723b45681b4b748. Four new SkillTestModeTest cases, four SkillPresentationTest cases, 16 SkillWindowTest cases and all configured regressions/assembleDebug passed. Tests cover real Activity default ON and restart OFF, all219 virtual learned/usable entries, normal job rejection, save isolation, MP0 supported magic, every unsupported mage/cleric spell and real slot edit/restart. Native artifact11085513953 reviewed: cleric59 entries/test label, poison caster/recipient anchors and shared overhead source pose. APK artifact11085469099 uploaded2026-09-30 17:43:37 KST; versionCode64; 18,695,978 bytes; SHA2567043d8bb59416b62afdf8a7c8cbe5f225d575bb45d9762bf78727bb33f11c052. All273 packaged presentation assets match verified source bytes. Implemented/build/native-input runtime verified; physical-device and user visual acceptance pending. All219 are test accessible, not219 fully implemented mechanics: existing30 real combat; remaining189 explicitly preview-only. Closure documentation changes no runtime/APK bytes.


## 2026-09-30 — SKILL-SPATIAL-03 v65 candidate

User rejects v64 all-class motion/effect/range behavior: DEVICE_FAILED / VISUAL_REJECTED (user-reported). Code inspection reproduced absent per-skill tile rules, preview bypass and passive/finisher alias errors; source exact original visuals remain unverified. Current task is all-class original research and actual-path correction, branch `codex/original-skill-contract-v65`, base bfd668d4e178fa82625d634b5a54be0e27ce30a3. See SKILL_SPATIAL_CONTRACT.md and219 reviewed SKILL_ACTION_DECISIONS.csv. Shared Resolver now owns test presentation legality and area recipients; expanded72 offense/16 heal plus equipped basic paths, reconstructed finisher shapes, source kick extensions and passive/basic-link distinctions. Persistent status, travel/landing and exact original effect/finisher-pose evidence remain incomplete. Candidate implementation; exact-SHA CI/native verification pending. Do not describe219 original skills as fully implemented or source-authenticated.


## 2026-09-30 — SKILL-SPATIAL-03 v65 verification resumed

- IMPLEMENTED: source `fd3e33176859956780c4fd752d8d1381c18280a7`, branch `codex/original-skill-contract-v65`, PR #176, base main `bfd668d4e178fa82625d634b5a54be0e27ce30a3`. This remains an unmerged candidate. Compared the active source with current main; the 30-file delta contains this task. All219 reviewed IDs have per-ID spatial/presentation decisions;72 damage and16 heal definitions use adapted numeric balance.131 entries have presentation-only mechanics, including gated interactions/passives/unresolved entries, and are not fully implemented abilities.
- BUILD_VERIFIED: push Actions run [36702916604](https://github.com/ChanJoos/Project_dark_android/actions/runs/36702916604), job109846298749, checkout/source `fd3e33176859956780c4fd752d8d1381c18280a7` succeeded. Regeneration, SkillWindowTest, SkillPresentationTest, SkillTestModeTest, SkillSpatialContractTest, configured combat/world/save/monster regressions and assembleDebug passed. The PR check run36702922238 also passed; the delivered artifact below is from the exact-source push run, not the synthetic PR merge.
- NATIVE_RUNTIME_VERIFIED (automated scope): native GameView input/Resolver tests cover all-class recipient masks, range/LOS/viewport checks at start/contact, moved-target cancellation, passive/utility gates, linked basic tiers and per-recipient VFX deduplication. Downloaded rendering artifact11090284176 and inspected male/female four-direction pose sheets plus Warrior Crasher/DevilCrasher, Rogue assassination, Martial Dara, Mage Meteor and Cleric HolyDragon production screenshots. Impact screenshots prove these fixture frames only, not every animation frame or physical-device behavior.
- APK: artifact11090074775, versionCode65 / versionName `0.65-source-skill-contract`; built2026-09-30 19:33:56 KST (APK artifact file timestamp; upload19:33:57 KST),20523070 bytes, SHA256 `3e2b1c7bd81a09333696c8658351bafe5f73d95be00e4a925cf7a8c72d5ee5ca`. All274 packaged presentation files match verified source bytes. All268 packaged male/female BODY frames match `master/assets/animation_frames/body` byte-for-byte;219 catalog decisions present.
- DEVICE_PENDING / VISUAL_ACCEPTED_PENDING: original pixel-effect matches remain0. Source-described/reconstructed effects, adapted balance and selected source poses are not authenticated original per-skill animation. Persistent buffs/poison/sleep/recognition reset, travel/landing, summons, exact finisher poses/reaches, Pacheongak/Climens/Dinosense and gender equipment motion gaps remain open. Further targeted web searches did not supply usable exact finisher reach/motion or Pacheongak mask evidence; no original fact was promoted from a search snippet.
- Next work: reproduce the reported motion/range scenarios on this exact candidate APK; resolve individual original finisher pose/range and remaining unknown masks from identifiable combat video/source frames; then implement persistent status/travel/summon services through existing domain modules. Do not mark all219 mechanics complete or physical-device/original visual acceptance passed. This closure changes documentation only and does not rebuild or relabel old runtime bytes as a new version.


## 2026-09-30 — v66 quick-slot approach follow-up (implementation pending CI)

User reports v65 quick slots reject out-of-range use and rejects adapted VFX as unlike the original (DEVICE_FAILED / VISUAL_REJECTED, user-reported; exact installed SHA unconfirmed). Continue PR176 branch; main remains bfd668d4e178fa82625d634b5a54be0e27ce30a3.

- Added one-shot SkillApproachController intent wired to quick slots in Milles/Pote. World BFS finds shortest reachable legal range/LOS/viewport tile, including retreat for minimum-range skills; tracks moving target reserved tile. MP/cooldown is charged only by Resolver on submission, once. Direct input, target change/death, player death, map change, pause or test-toggle cancels.
- Added SkillQuickslotApproachTest for native quick-slot input, one-shot charge/hit, minimum-range retreat, cancellation, blocked route/four directions and moving-target replanning. Generator and Master validation pass locally. Android tests/build/device verification remain PENDING until exact CI evidence.
- Complete defense now uses a thin blue shell reconstructed from supplied combat video. Crasher/DevilCrasher use shared blue/red procedural vortex instead of v65 generic generated columns. These are still ADAPTED, not extracted/authenticated original frames; all219 original VFX request remains OPEN. Existing elemental/status atlases and other generated finishers have NOT been promoted to original.
- Owner: Director/Integration. Next: run exact-source CI, diagnose failures, review native runtime captures; obtain identifiable original skill effect frames and replace remaining adaptations. No full original visual completion or physical-device success claimed.


## 2026-09-30 — v67 original combat capture source (CI pending)

User supplied Naver cafe13434008/article401229 and six GIFs; replaces prior insufficient-search conclusions. Original blog https://m.blog.naver.com/180921/221777831873 publicly provides nine labelled GIFs. Downloaded all9 through the page-observed w800 CDN URLs, retaining bytes and hashes in master/source/skill_fx/naver_401229/provenance.json. This is FAN_GAME_CAPTURE, not an authenticated native game archive or current server numeric canon.

CapturedSkillFx routes nine exact skill IDs: Crasher, MadSoul, DevilCrasher, MadSoulJin, Assassination, AssassinationJin, Dara, Guyang and Dalma. 112 real capture frames preserve nonuniform GIF frame times (one cycle, excluding idle recording waits). Extractor removes floor and captured actor/text; SCREEN alpha and pivot/scale are explicit matting adaptations. Occluded/clipped effect pixels stay missing, not generated. Old procedural/generated recipient effects and generic caster pulses are disabled for these9. Other210 IDs do not become original effects by this change. Male/female selected BODY bytes and actual combat rules stay unchanged.

Validation pending: CapturedSkillFxTest checks9 ID mappings, source/atlas SHA hashes, nonuniform timing, transparency, actual GameView submission/contact/recipient-only emission/expiry and9 production screenshots. Existing quick-slot full-frame and combat/save/world regressions remain enabled. Exact-source CI/APK/native visual review required; physical device and all219 original visual acceptance remain pending.

Capture anchoring: source GIFs show the effect around the caster, so captured contact pulses follow the player foot anchor and deduplicate per action (including multi-recipient hits). Transparent actor masks align to that foot; the source does not supply effect pixels hidden by the actor. Runtime hit recipients and damage remain separate.


## 2026-09-30 — v68 classic martial/cleric source candidate

Continue PR176/codex/original-skill-contract-v65 from308bc751. Fully read61 labelled classic rows (27 martial/34 cleric); retained132 source media/56 GIFs. Adds missing martial IDs033/034; catalog221, Benusti stays excluded.55 captured effect IDs /85 caster/contact channels,60 source icon bindings, corrected source descriptions/stats/kinds and ally/group/self rules. Original BODY268 bytes preserved. See CLASSIC_MARTIAL_CLERIC_V68.md and source provenance. IMPLEMENTED_CANDIDATE; exact-source build/native review/device/visual acceptance PENDING. Persistent status, travel, summon, party and finisher formulas remain unimplemented; this is not61 complete mechanics. v67 prior run36715127755 SUCCESS but its legacy-graphics captured screenshots were black; corrected native-render test now required.


## 2026-10-01 — retained Rogue source constraints

ROGUE-2015-SOURCE-REVIEW accepted for six magic kinds, project-selected overhead BODY, labelled icon crops and reviewed source-pixel channels. Source245450 explicitly says 밀기/적갑옷해체 are player-only; source245456 says 하이더 targets another user. Until user-target services exist, these three are gated as utility actions instead of previewing on a monster/self. Smoke uses observed screen membership in test presentation; map-wide behavior remains unresolved and the circa-three-second blind status is not claimed implemented. Backslash uses one isolated source glint on each resolved front/rear recipient, not a fabricated four-direction original capture.

Coverage is per ID in SKILL_SOURCE_COVERAGE.csv:147/221 source icons; native BODY pixels are project-selected semantic poses; missing bindings/status services/device and original visual approval remain explicit.


## 2026-10-01 — all-job presentation and resolved damage channels

V70 extends retained-source bindings beyond Rogue to every labelled Warrior icon, reviewed Warrior shapes, existing Martial/Cleric sources and same-name common/Mage heals. Different-name spells never borrow another original ID merely to fill coverage. Static source shapes use explicitly authored hold/fade timing. The universal recipient impact is an adapted reuse of retained Charge burst pixels, emitted only by a resolved positive DAMAGE/CRIT contact; it is not a claimed original universal impact. Spell/caster channels and recipient damage feedback are independent. BODY uses original male/female front/back groups with the established horizontal transform for four directions.


## 2026-10-01 — restore continuation and complete Pote hit timer wiring

V70 is already retained and verified at PR176. Scratch restoration must reuse its remote checkpoint rather than reconstruct source assets. Pote's separate sprite renderer must consume the same resolved positive-damage timer as the existing Milles presentation, using a cached RGB-only variant that preserves alpha/pose/anchor and does not leak to other draws. This is ADAPTED presentation; no new original hurt sprites, timing facts or source mappings are inferred. UI catalog counts derive from current previewable entries and exclude the three internal validation fixtures.

V71 native edge QA exposed color-filter alpha bleed at the Lycan1.5x scale (transparent alpha0 became4). Retain exact source alpha in a cached color-only bitmap variant and draw both variants with the same paint. Maximum extra memory for all60 current48x48 poses is552,960 pixel bytes, with no second packaged sprite set. Keep the exact-alpha assertion rather than loosening it.

V71 native full-scene review also showed Pote damage numbers overlapping names. Place Pamfet and Lycan popup baselines above their respective name/HP regions (62/78px above the world foot at contact); retain the existing popup timer/rise and Milles layout. This is project UI spacing, not an original combat number or sprite change.


## 2026-10-01 — V72 reference skill window (IMPLEMENTED, verification pending)

Continue verified V71 on PR176. User requests the exact structure/art of the attached original-client book. Preserve attachment bytes at master/source/skill_window/user_reference_20261001.jpg. Source-crop skin keeps original list/detail headers, material, frame strips, square icon frame, selected gold frame, command box and scrollbar. Eight screenshot crops override modern Rogue icons in the square UI cache; seven are inferred from original circle-four/five ordering and the catalogue, selected trap is explicitly labelled. Historical art/source atlases stay untouched. Group list by recorded circles 1–5, advancement, pure advancement; eight columns, clipping/scrolling and three full-width horizontally paged job tabs. Existing 221 IDs remain reachable; no fabricated second-job data/rank system or original cooldown. 1/1 counters mean learned/not learned, not original progression ranks. Missing cast time remains —. Mobile controls for learning/conditions/8 quickslots remain below the detail panel and do not claim desktop chat/command entry implementation. New input/native tests cover all221 IDs, source trap pixels, clipping/scroll isolation, jobs and registration. Existing economy/save/combat/monster/world remain unchanged. BUILD/RUNTIME/DEVICE/VISUAL acceptance pending until actual evidence.


V72 native review follow-up: first successful run36810870615 proves all221 IDs/input/scroll/transaction and existing regressions. Manual native comparison found command-box captured text under the new hint and trap at column5. Remove only command interior pixels, keeping source border. Two further modern Rogue icon crops (Slash/Hyder) retain explicit visual-motif/name inference; total10 window crops,9 inferred/1directly labelled. Reorder Rogue promotion slots from retained source evidence so trap is column3; no rank/progression change. Show directly labelled trap source description/captured23sec cooldown/0sec cast as SOURCE_PREVIEW_ONLY when no runtime definition exists; explicitly label 전투 효과 준비 중. No original23sec gameplay timer is implemented or implied. Historical engine, source atlas and rank counts stay unchanged. Final follow-up verification pending.


## 2026-10-01 — V72 source-window candidate verified

BUILD_VERIFIED: Actions36812062766/job110209069294 SUCCESS. Runtime/test1b4c456abed36cef8a5dd262780d8c65b1dc23d5 and PR checkoutadb95976da8268d77061209cc038072c7fd4c75f share treedbadb0d564ee42583e1db1e7f4fbaa34e34d0ebd. Version72/0.72-reference-skill-window; APK artifact11140760052,25,528,159bytes, SHA2565b073725c2f8c18ef02f309ac4336d414d68e54abc7221b682e3ba517744c359, build2026-10-01 12:50:32KST. Native artifact11139851327 ZIPSHA85188f44d8a989cd1d676d766ef83c84c747778ab3e5c2bf770c195097ab84ae. Exact record: docs/verification/SKILL_WINDOW_V72_BUILD.json.

IMPLEMENTED / NATIVE_INPUT_VERIFIED: source two-panel skin and circle/advancement grid, seven jobs in three full-width paged tabs, real vertical scroll/clipped selection and221 accessible IDs, source selected square/gold frame, details, retained learning/condition/save/8-slot input.27 crop files;10 modern Rogue icons (1direct label/9explicit inferences),160 UI bindings,61 unresolved. Trap's source23sec/0sec/0MP and captured text are preview data with pending-effect label, not a new trap mechanic. Boolean1/1 counters are not original ranks. Main remainsbfd668d4 and PR176 remains unmerged.

REVIEW: real native Rogue frame inspected after fixing duplicate captured hint and moving trap to column3; final MP0 and source typography/frame/spacing read correctly. Scrolled/all-job frames retain clipping and ownership selection. All configured generator/source/Master, skill learning/MP/quickslot/economy/save, BODY/VFX, world/Pote/combat/growth/reward/restart checks and APK assembly pass. All790 V71 packaged assets are byte-identical, and every new packaged skin byte matches the tracked source projection. Closure is docs-only.

PENDING: photograph's2차직업 data/ranks and unlabelled icons are not fabricated;61 window-icon gaps,9 new name/motif inference acceptance,133 existing domain/presentation gaps, physical-device/user visual acceptance remain. The requested fully identical photograph is NOT fully met. Next: user inspect exact candidate APK/window, confirm inferred Rogue names, then obtain labelled modern second-job/icon data and close source gaps. No claim of all221 original skills complete or physical-device verification.


## 2026-10-01 — V73 all jobs visible / verified candidate

User report: only Mage/Rogue appeared visible. V72 retained every job but exposed only three horizontally paged tabs; this visibility design is superseded. IMPLEMENTED: one simultaneous row of 공통 / 전사 / 도적 / 무도가 / 마법사 / 성직자 / 전체, direct selection, no tab arrows or paging. Circle grid, source skin/details, learning and eight slots remain intact. No skill mechanics, source bindings, job data or gameplay balance change.

BUILD_VERIFIED: runtime/test8dc610e17abbf005073fbd0b33fc61d67d7c2593 and actual PR checkoutf90facca64233f7ac12fd61a587d950a1230b378 share treed52bf4ba6610925192497626120882a18f8c0e56. Actions36813536121/job110213560625 SUCCESS; version73/0.73-all-job-visible-tabs; build2026-10-01 13:10:36KST. APK artifact11140962290,25,528,023bytes, SHA256626907f1d71c562e3789fc522b12095f6d5224b65ba9b78861ac9f12c7c10f85. Native artifact11140653325. Exact evidence: docs/verification/SKILL_WINDOW_V73_BUILD.json.

NATIVE_INPUT_VERIFIED: direct taps on all seven visible tabs and all221 ID selection, source-pixel/scroll/clipping/world-isolation checks, existing learning/economy/quickslot/save and configured world/BODY/VFX/combat/growth/restart regressions pass. All818 V72 packaged assets are byte-identical. Actual all-tabs and selected Rogue native frames manually reviewed. VISUAL_ACCEPTED/PHYSICAL_DEVICE remains PENDING; this is an unmerged PR176 candidate, main remainsbfd668d4. Previous61 icon,9 inferred-name and133 service/presentation gaps remain open. Closure changes docs only. Next: inspect this exact APK on device and confirm job visibility; continue retained source/service backlog without restarting collection.


## 2026-10-01 — V74 inventory/equipment reference windows verified

User requested both supplied original-client screenshots applied to existing UI. IMPLEMENTED:13 lossless window/toolbar crops plus manifest, retained original JPEGs;10-column inventory with filters/paging/equipped counts, two current/chosen option cards; central approved paper-doll, eleven supported equipment slots including armor, slot selection/direct unequip, actual FinalStats and detailed-stats navigation. Existing domain/economy/save authority retained. Fix equipment close routing to the drawn position and consume modal taps instead of passing them to the world. No copied screenshot-owned items/stat values, original durability/capacity expansion/grade mechanics or independent second glove domain slot. See docs/ITEM_WINDOW_CONTRACT.md.

BUILD_VERIFIED: runtime/teste23a559286f16f5bea3058053981756e53199997 and PR checkout9cf6745a7dd1dfc313d41d2a14e3650f83d952b8 share treec215bf24b84d2a7c21878b8b1f8909f28e3283eb. Actions36817322833/job110225117388 SUCCESS; version74/0.74-reference-item-windows, built2026-10-01 13:59:46KST. APK artifact11142395977,25,588,331bytes, SHA256ae464a19ffb8a7c32705485e47e6600047a02e8fde1360fb516cd3f7bb111c47; native artifact11142346061. Exact record: docs/verification/ITEM_WINDOWS_V74_BUILD.json.

NATIVE_INPUT_VERIFIED: selection/filters/all owned IDs, slot/close/details routing, equip/unequip immediate stat change and save restore into fresh GameView, plus existing equipment stats and all configured skill/BODY/VFX/world/combat/growth/restart checks. First run36817236551 failed only the restart harness's old-view-after-rebind action; corrected to continue on the restored view, no runtime save workaround. Five native frames reviewed; all818 V73 assets unchanged and14 new skin files match tracked bytes.

VISUAL_ACCEPTED / PHYSICAL_DEVICE_PENDING. Unmerged PR176 candidate; main remainsbfd668d4. Mobile landscape geometry is adapted from the two photos, not pixel-identical original desktop UI. Previous61 skill icon/9 inference/133 service gaps remain open. Closure docs-only. Next: install exact V74 and inspect inventory/compare/equip/unequip/detail/close on device; preserve existing all-job/source/BODY/world work.


## 2026-10-01 — V75 source FX audit in progress / DEVICE_FAILED report

User reports Crasher visually ambiguous, Complete Defense unlike supplied source, Mad Soul sometimes points behind caster, Dankak/Bungkak unlike supplied effects while testing the delivered skill window. Record DEVICE_FAILED (user-reported visual); physical reproduction by agent unavailable. Local source/runtime audit reproduces three code defects: captured Mad Soul never transforms its fixed rightward source direction toward actual contact target; defense blue-only inverse matting removes pale/white ring rim; martial contact matting masks cyan overlapping body pixels and splits off tiny start fragments. Correct projections/registration without repainting missing source art. Source actor occlusion still cannot be recovered. All221 IDs require source/precedence/anchor audit; generic fallback and missing/per-facing/service evidence remain explicit gaps. No all-original-complete claim. Native/input/build/visual acceptance pending until exact final evidence is recorded.


## 2026-10-01 — User video / center registration
Latest user asks to follow the supplied Complete Defense video and start recipient particles at monster center. Supersedes static Defense hold/fade. Source trace fills occluded shell from the unobstructed blue radial samples; it is not pixel-exact recovery. Original highlight pixels retain video cadence. Public GitHub push was blocked by automatic approval review: publishing user-video frames/derived assets lacked explicit publication authorization. No workaround attempted; changes remain local until user authorizes that specific public destination. Magic original coverage remains unresolved for Mage52 and Cleric22 fallback IDs.


## 2026-10-01 — Defense visual rejection and source collection correction
User explicitly rejects the v76 Defense preview and says original magic already supplied. Record VISUAL_REJECTED; do not ship the radial trace as accepted. Collection-complete claim is scoped to11 listed archive articles, not all jobs/spell art. Source census proves zero Mage-demonstration article in that11 and only Martial/Cleric in61 classic definitions. The earlier all-original saved/applied statement was inaccurate. Icons, information and effect binaries must have separate coverage checks.


## 2026-10-01 — V76 recovered magic source, local candidate

IMPLEMENTED LOCALLY: restored five missing public cafe articles from the first-party gateway declared by the public mobile client. Preserved228 media references/24 animations and immutable article bytes in naver_magic_recovered_20261001. Article416054 adds16 exact-ID Cleric GIF routes and17 icon bindings; classic rows85, runtime IDs71, channels112. Two table typos are separately recorded against labelled GIFs; source bytes stay unchanged. No source GIF is rebound to a different job by visual similarity. Source icons174/221; adapted FX70. Mage52 adapted animations remain unresolved: recovered2014/2015 articles are still captures, not original animation cadence. Seven advanced table abilities are outside the current catalog; Holydragon large-scene pivot is pending.

Defense candidate replaces the rejected radial profile/erased centre with the observed2D surface, reflected visible arc and harmonic occlusion fill. This is reconstructed, not a native original atlas or user-accepted correction. See defense_v76_surface_candidate.gif for video/source comparison. Actor occlusion and extracted glint edges remain visual review items. Recipient-center code from the previous local work is retained.

VERIFIED LOCALLY: available CSV integrity and228 media/source hashes; all221 runtime route/atlas hashes; deterministic projection regeneration. Updated existing native integration expectations to71 effects/112 channels/85 reference rows. BUILD/ANDROID RUNTIME/DEVICE/VISUAL ACCEPTANCE: PENDING; no new APK delivered. Earlier automatic review rejected public egress of private-video derivatives; no push performed or bypass attempted. Existing legacy archive verifier hardcoded checksum fails against an unchanged archive; current CSV validator passes. Continue PR176; do not deliver as current main or all-original-complete.


## 2026-10-01 — V77 Defense quality and Mage source routes

User accepts the V76 Defense shape and asks for improved quality, Mage connection and an actual GitHub commit. Explicit approval includes publication of the supplied recording-derived assets; the prior egress blocker is superseded. Preserve the accepted two-dimensional shape, pivot, scale and 66-frame cadence. IMPLEMENTED: feather recovered bloom before alpha quantization, opt-in filtered bitmap scaling for Defense only, and an RGB MP4 comparison avoiding the old GIF palette limit. This is capture reconstruction, not newly recovered high-resolution source. Defense shape is USER_ACCEPTED; quality and physical-device acceptance remain PENDING.

Mage: 30 exact-name routes now use retained 2014/2015 source capture pixels, with source-frame and matting-plate hashes, actor/UI masking, visual-center registration and explicit project hold/fade or capture-order timing. Original animation cadence is not known from still captures. Classic runtime IDs101/channels142; source table rows85; all221 IDs audited. Mage57 =30 capture routes +1 shared route +4 no-particle +22 unresolved original effects. No similar-spell rebinding or icon-as-effect substitution. Existing Cleric16 recovered GIF routes and recipient-center/directional corrections are retained. Source CSV/Master rules, BODY, movement and gameplay balance are unchanged.

VERIFIED LOCALLY:228 retained media-reference hashes, all source-frame/plate/atlas hashes, CSV integrity and deterministic projection regeneration. Native Android tests and build are delegated to the exact GitHub Actions SHA; not yet certified. Continue PR176 without merging or force pushing. Evidence: verification/SKILL_FX_V77_AUDIT.json, MAGIC_SOURCE_V77_AUDIT.json and defense_v77_quality.mp4. Remaining22 Mage originals, occlusion, cadence and physical-device checks stay open.

## 2026-10-01 — V78 original-source residual queue

User directs exhaustive verification of unconfirmed effects against all supplied original references, connecting matches and retaining only genuine source gaps. Accepted: consume labelled omitted sources, keep exact-name cross-job reuse explicitly project-authored, register Push demonstration without inventing a particle, and leave ambiguous/unlabelled or absent original frames unresolved. No later-job catalog expansion or mechanic change is authorized by this source-mapping request. Remaining originals are individually recorded; build/device/visual acceptance require separate evidence.
