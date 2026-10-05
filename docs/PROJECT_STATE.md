## 2026-10-05 — V108 quest runtime fixes and audit (publication pending)

User-reported V106 device failures: kill-quest quickquest did not enable auto-attack; Warrior “use Shortblade 3 times” did not advance. V108 fixes both in production GameView/CampaignProgress event paths. Quickquest now enables auto-attack immediately for active KILL/PAIR goals, targets only quest-relevant monsters and respects pair caps. Successful Shortblade skill-use events count even when damage rounds to zero; healing goals still require actual healing. Added GameView integration coverage for quickquest and real Shortblade use plus duplicate/wrong-skill/zero-damage rules.

Quest audit: all five class campaign routes (23 entries per class) are exercised through progression/reward tests; KILL/PAIR combat events, SKILL resolver events, VISIT map entry, SUPPLY shop/sale/healing/equipment, ALTAR distinct altar/essence, LEVEL normal progression, prologue/growth handoff and idempotent claim/save are connected to runtime rules. Quickquest/journal UI, latency and HUD touch tests cover route and display behavior. None of these results claim physical-phone verification; user-reported failures remain acknowledged and phone acceptance of V108 is pending.

Focused Android/Robolectric suites passed after fixes: CampaignQuickQuestIntegrationTest, CampaignProgressTest, QuestJournalTest, QuickQuestLatencyTest, HudTouchAcceptanceTest. Exact run/build and artifact details will be recorded after GitHub Actions completes. Version 108.

## 2026-10-05 — V106 presentation repair verified candidate

V105 remains DEVICE_FAILED_USER_REPORTED / USER_VISUAL_REJECTED. V106 IMPLEMENTED / BUILD_VERIFIED / NATIVE_RUNTIME_VERIFIED_ROBOLECTRIC; PHONE and USER_VISUAL remain PENDING. Exact source7cefb0a1ad707b07bb0a5436a73a8189d5f1825c, Actions37263309751 SUCCESS, APK artifact11324339557, native artifact11325382653; built2026-10-05T13:26:05.523420+09:00. APK SHA2566cdb3e6724fd22cb231519744f334a0e14cbd33f46ddccf9a84c7a2265fb517b. Final25 affected tests passed, zero failures; baseline249/251 passed on byte-identical runtime/assets, with the two stale assertions corrected and rerun. Actual APK47 original-file hashes, CRC, v2 signature/content digest and manifest106 verified. Final HUD/inventory/equipment1536x709 images visually reviewed: original equipment/item art, restored equally spaced utility icons, Korean NPC names and fixed SW idle pose. NPC renderer pixel invariance tested across direction/walk/clock inputs. Source preservation and missing original poses retained, not fabricated. Current gameplay/campaign/save/map preserved. Unmerged active branch candidate. Debug signer differs from V105; no in-place upgrade claim, no uninstall instruction. Evidence: docs/verification/DEVICE_PRESENTATION_V106_BUILD.json and verification/V106_RESTORED_EQUIPMENT.json. Earlier pending run entries below are historical.

## 2026-10-05 — V105 DEVICE_FAILED; presentation repair V106 in progress

User supplied 1000058533.jpg (inventory placeholder glyphs) and 1000058531.jpg (corrupt player gear, English NPCs, changed utility icons/gaps). V105 is DEVICE_FAILED_USER_REPORTED / USER_VISUAL_REJECTED. Reproduced packaging root cause: campaign-review.yml omitted the Android general workflow source-recovery step; 47 V104 equipment atlas/registration assets were absent from the delivered APK. They are now restored byte-for-byte from exact V104 artifact11304510089, with per-file hashes in verification/V106_RESTORED_EQUIPMENT.json. No user raw media is published. NPC fixed SW idle/Korean transcription replaces previous face-player behavior. Utility icon painter restored from V97 source25871a27; five centers use one50px step, shared by input and rendering. Modern window layout, current gameplay/campaign/save/monster/world remain preserved. Native tests and actual final APK packaging hash gate added. First repair source262c77bc / Actions37261568293 pending; restore of three alias resources and final build evidence follow. PHONE and USER_VISUAL remain pending. Inputs: current user screenshots/direction; AGENTS/director/state/decision/backlog; development constitution; design DATA_CONTRACT/SOURCE_OF_TRUTH; Master manifest/data consumption/source catalog/visual rules; item-window/UI99/NPC104 contracts; exact V104 APK and V97 GameView.

## 2026-10-04 — V105 executable Lv40 candidate (active continuation)

User approved continuing implementation and publication. This supersedes the earlier approval/build-environment block below. The full common20+class15 campaign, 4forest instances/Piet safe hub, class practice/gear/shop, MP/HP supply/real essence altars and schema4 transactions are implemented. Five normal Resolver/resource simulations completed23quests each and reachedLv40; no forcedEXP/infiniteMP/testskills. Placement fixtures are not phone/timing acceptance. All NPC/species/gate paths and native choice/map/restart tests are in the candidate gate. Details and explicit remaining design proposals: [implementation scope](verification/LV40_IMPLEMENTATION_SCOPE.md). Final exact source/CI/artifact evidence is to be appended after build verification. PHONE/USER_VISUAL pending; main remains unchanged.

## 2026-10-04 KST — Lv1–40 implementation PARTIAL; BUILD NOT VERIFIED

Latest local source: `5f777ba99ca038c42395a150092088b9f2c9e4d5` on `codex/town-interiors-mobile-v86`. Campaign reward provenance now has its own `ADAPTED_CAMPAIGN` source; isolated training-token prototype rewards retain `ADAPTED_TEST`. Regression expectation updated. This is a source classification correction; EXP/Gold values and runtime behavior are unchanged. `git diff --check` and Master CSV integrity validation pass. Android tests/build, APK, device, and visual checks remain NOT RUN. The authorized implementation remains partial: four Pote areas, Piet, executable quest chains, later gear/skills/economy, and normal-mode Lv40 completion are outstanding. Source has not been pushed because the prior public push was rejected by automatic approval review; no alternate publication route was attempted.

Publication status update (review retry): user-authorized push to the same existing review branch was attempted; automatic review again rejected publishing to the public GitHub remote as unverified and explicitly barred workaround/indirect execution. No retry through another channel. Docs commit `e1be1b4989e312c97fdba063b526b68929600071` records this. Local working source remains the code commit above; main unchanged.

User authorized implementation from the plan. Current local source HEAD `db231e3dfc50d1450d9ad7daae08fe91464a171e` on `codex/town-interiors-mobile-v86`; base V104 remains source `48f8a037872c8ddfab8abacd5808671de9dd9f8f`, previous remote branch head `4782fae09995949891503f2b32b2f47a85211421`, main `bfd668d4e178fa82625d634b5a54be0e27ce30a3`.

Implemented candidate slice: new Michael job-counselor NPC and journal row; first basic job selection after the existing Growth Quest and Lv3, class-specific ADAPTED equipment, supported starter skill/slot, saved job code; separate 15-entry ADAPTED monster reward catalog, of which four Pamfet field actors plus the accepted inn mouse are currently connected by their runtime actor profile. Canonical POTE_SPIRIT stays separate. Added reward/job/restart/journal/NPC tests and CI step. Starter-skill award is disabled while skill-test mode is active. `git diff --check` and `python3 tools/validate_master.py` pass; Android tests/build remain unrun.

NOT COMPLETE: all other common/class quests, 4-zone Pote spawns/transitions, Piet hub and NPC quest givers, full progression gear/skills/supply/economy and Lv40 balance remain unimplemented. No Gradle wrapper/installation is available in the local workspace; tests/build/native evidence have not run and no APK exists for this work. Push of source `954b13a` to the public GitHub branch was rejected by automatic approval review because implementation authorization did not authorize external publication. It was not retried through another route. Two implementation commits remain local. Current branch therefore has not been CI-verified or uploaded. Preserve this incomplete status.

## 2026-10-04 KST — Lv1–40 complete progression planning (PROPOSED)

User requests planning before implementation: missing first-job acquisition and Pote EXP break the Lv40/second-circle journey. Plan docs/LEVEL1_40_PROGRESSION_PLAN.md defines free first job at proposedLv3, 5mentor/player-style NPCs, common20+job branches15 definitions (23per character), Pote4areas/Piet minimalhub, class gear/skills/MP supply, economy, save migration, and actual normal-mode5job1→40 acceptance. Current Master curve summed9,000,000EXP: proposed once quests3,600,000 + hunting5,400,000, representativeceil300kills, target150min; monetary targets34,000income/25,450mandatorycost/8,550surplus. These are arithmetic-checked design targets, not playable simulation or phone results.

Reference source5434d9cb880ff2966fc97d8a95be3a6f88653938; V104runtime48f8a037872c8ddfab8abacd5808671de9dd9f8f unchanged. First-joblevel, EXP, economy, mandatory-free skills and class gates are B/ADAPTED PROPOSALS. Prior mobile no-job/no-level learning policy remains active until explicit implementation adoption; do not erase existing learned skills. OriginalLv48POTE_SPIRIT remains afterLv40, tomb quest-band conflicts retained as open. Current general Pote canonical-reward fail-closed test is preserved; proposed mobile rewards need explicit separate policy and idempotent tests. No Master/runtime/APK changes, no main merge, no new agent/automation. Next development starts with policy/data reconciliation then rewards, first job, maps, executable quests, all-class gear/actions and normal-mode full progression. BUILD/RUNTIME/PHONE for this new content NOT_IMPLEMENTED/NOT_RUN, not COMPLETE.

## 2026-10-04 KST — V104 player-style NPC wardrobe delivered

All 11 current human NPCs now use the actual player CharacterRenderer body, four directions, scale and registered gear. World and interior renderers delegate to NpcActorRenderer; old procedural bodies and outfit recoloring removed. New exact-source wearable atlases: 11 clothes, 11 hair/hat, 3 boots, 3 swords/staves. Each present NPC has a distinct real outfit and name from SSA US1926–2025 top100; ADAPTED presentation names preserve original Master canon and stable domain IDs. New NPC IDs get a deterministic top100 name and varied source outfit; explicit profiles extend NpcIdentity plus npc/profiles.json. Dialogue/journal/target names match. Player, stats, quests, saves, monsters, restored cathedral, full bench supports and rivers retained.

IMPLEMENTED_CANDIDATE / BUILD_VERIFIED / NATIVE_RUNTIME_VERIFIED (Robolectric Android graphics), not physical-phone or user visual acceptance. Exact APK source48f8a037872c8ddfab8abacd5808671de9dd9f8f, focused Actions37203753211 successful, 32 focused tests, 27 complete decoded native PNGs, all7 NPC captures locally reviewed. All44 directional NPC renders byte-identical to player wearing the same loadout. APK version104, 43,827,754 bytes, SHA256 0defe4e08fdfb711d3816fb881a2c7b8802b6eae54d52d20fdf0374d35161689; built2026-10-04T21:57:46.848378+09:00. APK ZIP digest/CRC, all28 gear assets, binary manifest, v2 RSA signature and content digest independently verified. Signer501bf24d4052d33fc124d8b16b7c7f1ce0148249ad366fd6f61e3a8207c4937f differs from V103: preserve existing app/save, no claimed in-place upgrade.

General Actions37203753054 had one obsolete test expecting Mary instead of Benjamin; test-only follow-upd6770a2cb2085c4c0b8ff54d904c441d35efbbb7 corrects it, runtime unchanged. General run37204025653 is ongoing; do not claim full-suite success. Relevant story/interior/name/quest gates pass. Existing branchcodex/town-interiors-mobile-v86 remains unmerged; mainbfd668d4e178fa82625d634b5a54be0e27ce30a3 unchanged. Four final deliverables saved successfully with individual receipts/metadata: NPC roster, actual Milles, actual church, V104 APK. Evidence docs/verification/NPC_IDENTITY_STYLE_V104_BUILD.json and contract docs/NPC_IDENTITY_STYLE_V104_CONTRACT.md. Next: observe general CI; physical phone NPC interaction/facing/name readability and user visual acceptance remain pending. No new branch, merge, delegation or scheduling changes.

## 2026-10-04 KST — V103 church/bench repair native verified and delivered

V102 user report: missing church identity and repeated bench leg defect, DEVICE_FAILED_USER_REPORTED / USER_VISUAL_REJECTED. Reproduced visual root causes: V102 substituted single-tower timber chapel for established multi-spire stone church; original bench cutouts have short indistinct support geometry, so alpha-restoration counts were inadequate acceptance. V103 restores retained church asset, existing location/door/interior; replaces bench appearance with one independently edited full-support sprite reused in mirrored facing. All11 bench placements now register actual feet at90,172. Obsolete short-support/grass-key path bypassed; old source bytes untouched. River/navigation/player/UI/save unchanged.

Runtime source `1a7809354681219a2bdbd1457ebc30d550d93ccb`; focused Actions37200737195/job111431719038 SUCCESS: deterministic369-placement audit, 20 native tests (style5 including actual renderer long-leg/ground/anchor and church-spire regressions, reconstruction4, interiors8, UI3), assembleDebug. All20 native PNGs independently decoded and reviewed: actual church district, whole map, both bench facings at native scale and enlarged, every district/depth/bridge. BUILD_VERIFIED and NATIVE_RUNTIME_VERIFIED; physical PHONE and USER_VISUAL_ACCEPTANCE remain PENDING.

APK artifact11302579693/native11302749088. Built2026-10-04T21:04:31.520506+09:00, version103/1.03-church-bench-supports, 43707859bytes, APK SHA256 `119219b4e9d22fde2e6f1e43a662c574d6a51abfdd5e059c383607a06a3276e4`. Artifact ZIP hashes/CRC, APK v2 signature AND content digest, binary manifest independently PASS. All4 APK/native-image files saved with successful receipts. New debug signer b700180e differs from V10252f7ea00; preserve existing app/save, no in-place upgrade. General full run37200737185 remains IN_PROGRESS (22 passed steps), not claimed passed. Main unchanged; unmerged existing branch candidate. Evidence `docs/verification/MILLES_CHURCH_BENCH_V103_BUILD.json`. Next: exact phone church appearance, all bench ground contact feedback and general-suite terminal status. User visual acceptance cannot be inferred from native gates.

## 2026-10-04 KST — V102 north-map source, scenery and bridge candidate delivered

Runtime source `2ffd629c96086b3554311004628c9d0b4eef3040`, focused Actions37197488523/job111422247871 SUCCESS. BUILD VERIFIED and NATIVE RUNTIME VERIFIED: source/scene audit, 18 tests (style3, reconstruction4, interiors8, UI3), assembleDebug; all18 exact-source native PNGs decoded and visually reviewed. Build2026-10-04T20:06:46.478572+09:00. APK SHA256 `d9b3ac430af3fd27541b6e17b14bb69a3e10b0856144f5383bc79403f43d4701`, artifact11300574743; native11301705178. Independently checked ZIP SHA/CRC, v2 signature/content digest and binary manifest102. All3 deliverables saved successfully.

User's new 구밀레스북마을 whole-map image supersedes the speculative internal river: north exterior water, garden circuit and outer residential circuit now implemented. Muted varied roofs, log cabins, drooping willow, quiet grass; bench source metal feet retained and halos removed. Buildings/tree reuse is explicit ADAPTED imagery, not recovered original assets or an exact service-coordinate transcription. River art and collision share one geometry; bridge deck and both landings reachable. Native movement failure at northeast2240,304 was reproduced and resolved by matching north-bank cabin collision to its visible bottom315; assertions retained. Earlier V102 internal-river native pass was visually rejected and never delivered.

Physical phone and USER VISUAL ACCEPTANCE remain PENDING; V101 user visual rejection remains recorded. Main unchanged; this unmerged branch is a candidate. Debug signer differs from V101, so in-place upgrade is false; preserve existing app/save. General full Actions37197488544 is still IN_PROGRESS (23 successful steps at closure), not claimed as passed. Next: obtain user/device appearance feedback and full-suite terminal result; do not weaken native movement tests. Evidence: `docs/verification/MILLES_RIVER_STYLE_V102_BUILD.json`.

## 2026-10-04 KST — V101 final source-tone/bench candidate delivered

Runtime source `87e8915ab785734bdfc7d594e571d13e37f75c69`; focused Actions37194828641/job111414413206 SUCCESS: source asset/layout audit, 17 native tests (source-style2, Milles4, interiors8, UI3), assembleDebug. All18 native PNGs independently decoded and visually reviewed, including every district and both bench orientations at .43 and enlarged. Bench iron RGB/feet restored; muted source material palettes, foliage luminance bands and larger clean source grass crop correct earlier leaf fading/repeated tiny-swatch motifs. Source raster bytes and world positions retained. This is ADAPTED material treatment, not recovered original-client architecture or pixel-identical original expression.

IMPLEMENTED / BUILD_VERIFIED / NATIVE_RUNTIME_VERIFIED; PHONE and USER_VISUAL PENDING. V100 remains USER_VISUAL_REJECTED. Delivered APK SHA256 c203e1f58211bb8efb72ae79931f5b7e722713fd3c54b73a437440470f8e52d2, 35145681 bytes, version101/1.01-milles-source-style, built Seoul2026-10-04T19:17:56.145381+09:00. APK artifact11300441516; native11300516191. ZIP CRC, binary manifest, APK v2 signature AND content digest independently PASS. Signer b0839898 differs from V1009115ec38; no in-place upgrade, preserve existing app/save. APK and actual overview saved successfully, receipts libfile_d7aa6030d3188191bb63ae2fc774f468 and libfile_dd09ec4caf948191b7867a2750c992a4. General final Actions37194828669 still IN_PROGRESS at handoff; previous full baseline59fea43a/run37193474493 SUCCESS. No assertion disabled. Candidate branch only; main unchanged. Next: actual phone bench contact and original visual acceptance; stable signing/migration OPEN. Exact evidence docs/verification/MILLES_SOURCE_STYLE_V101_BUILD.json. Earlier pending entries below are historical and superseded by this closure.

## 2026-10-04 KST — V101 USER_VISUAL_REJECTED V100; source-tone and bench repair
User rejects V100 bench feet and original fidelity. Explicit correction: target is tone, color, shading, pixel expression; placement is not the requested fix. No village positions, services, movement, body or domain changes.
Source diagnosis: cutout01/02 retain leg RGB with alpha zero; 03/04 additionally clip/discard lower support RGB. Presentation restores traced iron-support masks from retained source RGB and routes incomplete orientations to complete original01/02 counterparts with corrected source foot anchors. All original raster bytes retained. Full sprite image shifted only by the actual foot registration, never a fake ground overlay or fabricated feet.
Scenery-only cached conversion uses 24 captured colors per wood/roof/foliage/stone family, reduced fine smooth detail and nearest sampling; original capture/actor/UI paths are excluded. Continuous captured grass material replaces repeated crop diamonds; soil and grass use the same .43 captured-to-native scale. This is ADAPTED art treatment, not original-client sprite recovery or identical architecture. Color palettes do not establish source-identical expression or user acceptance.
IMPLEMENTED; map generation/asset audit PASS. Exact SHA CI/bench leg pixel regression/all-district native review/APK PENDING. Phone/user visual acceptance PENDING. V100 remains USER_VISUAL_REJECTED.

## 2026-10-04 KST — V100 build, native runtime gates and public APK VERIFIED

Exact source7f71aa38be9ae31027c3204d65a6bb8b4a5df66b/tree1aea13deb6a925b3a88c280ebc353411b2268c77. Actions37181394473/job111374533183 SUCCESS: all configured source/world/skill/combat/growth/save gates, V100 native depth pixels, all five services and district/bridge navigation passed. Native review artifact11294979007 (16 expected V100 captures); local reviewed16 complete PNGs and focused25 tests. CI PNG vs local byte comparison remains PENDING: artifact downloaded, but terminal stopped responding before extraction. APK artifact11294764162; release402872541/candidate-v100-7f71aa38. Public full anonymous HTTP200 attachment35139795bytes, SHA25621460a617ef12c0b2d0a1209e043f956e1c1ba3f30b15b0242e8f6214faa8458 equals BUILD_INFO and release asset digest; version100/1.00-milles-reconstruction, built2026-10-04 15:10:36.581611KST, apksigner v2 verification PASS. All954 non-map prior assets unchanged; two map JSONs changed, one independent generated bridge added, none removed. Direct https://github.com/ChanJoos/Project_dark_android/releases/download/candidate-v100-7f71aa38/PROJECT_DARK_V100.apk. Exact evidence docs/verification/MILLES_V100_BUILD.json.

IMPLEMENTED/BUILD/NATIVE_RUNTIME VERIFIED; PHONE and USER_VISUAL PENDING. Compact original-inspired composition is ADAPTED, not complete original town geometry or pixel-identical extraction. Existing NPC/monster appearance remains. V100 signer9115ec38 differs from V99a6f79441: no in-place upgrade; user warned to retain existing app/save. Stable signing/migration remains OPEN. Final Library batch invoked once, but terminal no-response means saving outcome UNKNOWN; not retried. Local git alignment blocked; closure uses sequential GitHub API writes with skip-ci, runtime source remains exact7f71. Candidate branch only, main unchanged. Next: inspect exact V100 park/benches/gardens/inn/bridge and front/behind motion on phone; resolve saving/native-byte comparison and signing without uninstalling user data. Earlier candidate-pending reports and clean-CI failures are historical; no checks skipped.

## V100 clean CI asset generation diagnosis

Actions37181212482/sourceeadbecbe and37181290557/source6f65ecce failed at the Milles regeneration gate before native/build execution. New generator imported Pillow without a clean-CI dependency. Replaced it with measured immutable source asset_anchors in authoritative base JSON; no new CI dependency required. Standard-library-only python3 -S generation and byte-reproducibility audit PASS; runtime positions/image bytes unchanged. Error stderr now surfaces when regeneration fails. Exact rerun follows; no checks skipped.

## 2026-10-04 KST — V100 old Milles reconstruction candidate

User requests old Milles reference research and complete village overlap/ground/depth repair. IMPLEMENTED on existing task branch; mainbfd668d4 unchanged. Supplied20.8sec footage plus visually inspected Nexon-hosted community in-game maps136184 support U/ADAPTED composition, not exact original geometry. Central grass island/six benches/rope lamps, perimeter dirt paths, ground tree beds and independently generated transparent wooden bridge; cleaned all districts, cohesive deciduous groves, moved orchard/flower house/inn lamp away from obstructing artwork. Scenery/individual NPC/monster/interpolated player now merge by foot-Y; standing scenery pre-sorted once to avoid per-frame object closure/sort churn. Same runtime PATHS drives road shoulders and door branches. Generated visible trunk/bench/fountain/well contacts plus water/bridge corridor consumed by WorldDef; existing five service doors/interiors and domain rules retained. All old raster asset bytes unchanged. Local focused25 native tests PASS (Milles4,district1,town8,journal9,UI3);16 final native V100 frames including full map overview reviewed, initial plinth/bench/fence/inn-lamp overlap corrected before final capture. Stronger bridge endpoints and final12 Milles/town tests PASS. Two truncated review PNGs detected by independent decode; fixed native byte-buffer/IEND/atomic write guard and exact4-test rerun passed, all16 final PNGs independently decoded. Initial local native fetch failure was missing worker proxy; fixed local-only init configuration. Local assemble was stopped after packaging hang/cache-lock contention; not a verified APK. Exact source/full CI/public APK PENDING. PHONE/USER_VISUAL pending. Contract docs/MILLES_RECONSTRUCTION_V100_CONTRACT.md; sources docs/verification/MILLES_V100_SOURCES.json.

## 2026-10-04 KST — V99 equipment/stat repair CI/NATIVE/DOWNLOAD VERIFIED candidate

Exact source4666202ab1eb26768d52da9553ab5bc296decdcd, Actions37177996173/job111364560018 SUCCESS; full configured gates passed including formerly OOM 78-test combined skill suite after fresh worker per class/1536MiB heap. No assertions skipped or weakened. STR/INT/WIS/CON/DEX restored; all11 empty equipment cells use centered part icons; balanced rails/head/actor/lower pair; aligned stat readouts/one continuous details pane. Native artifact11294275944:14 final PNGs all byte-identical to reviewed local occupied/empty/stat/wide frames. APK artifact11294340852/release402847732 version99,34079805bytes,SHA256b2edfa18835e85ece33b24e3584bb2972d302977549de26314051e8cacf6c2e9; BUILD_INFO13:58:56KST. Anonymous complete public downloadHTTP200/attachment/hash equals CI. Direct https://github.com/ChanJoos/Project_dark_android/releases/download/candidate-v99-4666202a/PROJECT_DARK_V99.apk. All956 V98 packaged assets unchanged. V99 signer differs from V98; not in-place update compatible; user warned to retain existing app/save. Signing/migration remains OPEN. V98 USER_VISUAL_REJECTED preserved; V99 PHONE/USER_VISUAL pending. Candidate unmerged, main unchanged. Proof docs/verification/EQUIPMENT_UI_V99_BUILD.json. Earlier interaction stopped before first failure was examined; resumed on user's status request, diagnosed/repaired OOM and completed delivery verification.

## V99 CI memory failure diagnosed and repaired

V99 first CI Actions37169633101/source48e58bee: UI/item gates passed; 78-test combined skill regression failed only with Java OutOfMemoryError constructing GameView in SkillQuickslotApproachTest. Test worker previously used default heap and accumulated native/source atlases across classes. Repair is test configuration only:1536MiB heap, one worker, fresh process per test class. No skipped/relaxed assertion, no gameplay change. Exact rerun pending.

## 2026-10-04 KST — V99 user-rejected V98 equipment/stat presentation repair

V98 USER_VISUAL_REJECTED: user points out unauthorized STR/INT/WIS/CON/DEX translation and left-aligned textual empty gear slots in screenshot1000058482.png. Reproduced directly in V98 source: Korean stat labels and left-aligned fit(slotName), uneven paper-doll coordinates. V99 restores original abbreviations, uses centered distinct vector silhouettes for all11 empty sockets (no slot text), equal left/right rails and centered head/actor/lower pair, removes redundant equipment checkmarks and zero bonus clutter, right-aligns stat numbers, and combines summary/details into one information pane with aligned HP/MP/AC/HIT/DAM. Original gear/character pixels and stat/equipment authority retained. Actual existing slot hitboxes derive from revised geometry. Production native occupied/empty/wide screenshots and all-slot touch/unequip/restart regression verification; phone/user acceptance remains pending. Existing active branch/main unchanged. Public push/release remain authorized by previous explicit user approval; do not ask again. Exact build/CI proof will follow successful execution.

## 2026-10-04 KST — V98 CI / native / anonymous APK download VERIFIED candidate

User explicitly approved V98 source push and public APK release. Published consolidated source72de0f03216625d9105db06f76589adb92893f1f/treeb62bff92507c6fea28c968ab276b7bae916c441c is byte-identical to locally reviewed95262bf (runtime98e12db); connected GitHub API used after generic git credential absence. Actions37168089905/job111335187724 SUCCESS, all configured gates passed. APK version98/0.98-modern-ui, built10:37:27KST,34078441bytes,SHA256c52280f8fe6d9027023d57635b9a6a6941cb1335b5eb5047d8375ade24b86bdb. Exact native review artifact11290023734:13 PNGs, ZIP digest/CRC verified, all13 byte-identical to reviewed local output. Prior952 V97 packaged assets unchanged; only4 Pretendard font/license/manifest assets added. Release402773125/candidate-v98-72de0f03, anonymous direct complete download HTTP200/attachment/hash equals CI artifact11290567773. Direct https://github.com/ChanJoos/Project_dark_android/releases/download/candidate-v98-72de0f03/PROJECT_DARK_V98.apk. CI debug signing certificate differs from V97, so in-place update NOT COMPATIBLE; user explicitly warned not to delete existing app/save to preserve data. Stable signing/migration follow-up remains open. No uninstall/migration performed. PHONE_PENDING / USER_VISUAL_PENDING; existing branch unmerged, main unchanged. Proof docs/verification/MODERN_UI_V98_BUILD.json. Initial publication approval block is resolved; never ask again for this authorized V98 push/release.

## 2026-10-04 KST — V98 modern UI LOCAL BUILD / NATIVE VERIFIED; publication blocked

Runtime source 98e12db0c6f390684b6bb8928534af3f8d9e36a3. Local Gradle assembleDebug PASS, version98/0.98-modern-ui. Nine relevant native regression suites: 66 tests PASS (3 new ModernUiTest); 13 lossless native captures, normal and wide layouts. Manually inspected HUD, inventory comparison, equipment (weapon preview overlap corrected), stats, skills and quests including wide layouts. Master validation PASS. Source item/skill artwork retained; Pretendard 1.3.9 Regular/SemiBold with OFL bundled. Exact APK hash/build KST/capture hashes/suites in docs/verification/MODERN_UI_V98_BUILD.json. Local debug certificate differs from V97: local APK is NOT an in-place update and is not presented as a phone upgrade. Existing saves must not be removed for it. GitHub push was rejected by automatic approval review: explicit authorization to export project changes to the public remote was required. No retry/workaround; all V98 commits remain local on codex/town-interiors-mobile-v86, main unchanged. V98 Actions/public release NOT RUN. PHONE_PENDING / USER_VISUAL_PENDING. Next: obtain user approval for this concrete V98 diff + canonical public APK, then push existing branch, verify exact CI/captures/signing/download and deliver. Do not mark physical phone or visual acceptance on native test evidence.

## 2026-10-04 KST — V98 whole-game UI redesign in verification

User rejects the overall UI and explicitly supersedes prior screenshot-matched window shells. INTEGRATED: shared charcoal/slate/ivory surfaces, gold/cyan accents, packaged Pretendard Regular/SemiBold, consistent vector utility glyphs/buttons/slots. HUD resources and quick skills, inventory/comparison, equipment, skill grid/detail, quest list and stat sheet restyled. Source item/skill icons remain unchanged. Larger named skill cards use the existing catalog and original grouping. Wide viewport modal drawing and touch coordinates share the center offset. Existing economy/equipment/skill/quest/stat authority stays in its existing modules. Three focused native tests cover actual input/rendering at 960x540 and 2340x1080, modal close alignment, read-only UI state and Korean glyph/contrast. BUILD/NATIVE/PHONE/USER_VISUAL pending exact CI/render review. Active unmerged branch codex/town-interiors-mobile-v86; main unchanged. Design contract docs/MODERN_UI_V98_CONTRACT.md.

## 2026-10-04 KST — V97 quest journal BUILD/NATIVE/ANONYMOUS DOWNLOAD VERIFIED candidate

Exact source 25871a27bfe599ed8ad2b742d2e16c386be7778a / tree 41d775e6ce042e9f8b4aa8c6e10fce6c687170e3. Actions37163238922 / job111320859970 SUCCESS; assembled09:03:23 KST. APK artifact11287939968,31907428bytes,SHA25686c120b1f1ec47f7ea2d91734dc63edcf0722bc07cf6afb1936dc5c0b0bc48f8. Native review11287894957:20 PNGs, ZIP digest/CRC verified, all states/wide layout/quick NPC/inn target/Mary/next quest manually inspected;13 previous review frames identical,7 exact reward-text updates inspected. Nine focused native tests plus all configured gates PASS. All951 V96 packaged assets identical; only quests/journal.json added. Public candidate release402741839 / candidate-v97-25871a27: anonymous HTTP200 attachment download independently fetched entire APK; hash/bytes match Actions and release digest. Direct https://github.com/ChanJoos/Project_dark_android/releases/download/candidate-v97-25871a27/PROJECT_DARK_V97.apk; no sandbox link or login. Public BUILD_INFO source/run/hash/time verified. Quest icon beside Skills matches brown/gold rail; all12 rows, status filters/conditions/next steps, two-line first-touch quick navigation, actual inn entry/objective/report -> immediate growth unlock -> exit/James -> Hans continuation, persisted completed forest travel and manual destination cancellation verified. Nine region-locked Master quests are inspectable and cannot accept/travel/reward. Initial compile failure was new test boxed Long/JUnit ambiguity, corrected;8-test precursor and9-test final full CI succeeded. PHONE_PENDING / USER_VISUAL_PENDING, unmerged candidate; main bfd668d4e178fa82625d634b5a54be0e27ce30a3 unchanged. User DELIVERY_FAILED and quest UX DEVICE_FAILED reports preserved; final independent download succeeds, native reproduction repaired. Proof docs/verification/QUEST_JOURNAL_V97_BUILD.json.

## 2026-10-04 KST — V97 quest UX and direct APK delivery candidate

V96 APK DELIVERY_FAILED (user-reported): sandbox delivery not downloadable on phone. Quest UX DEVICE_FAILED (user-reported): poor continuity, visibility and convenience; prior single-current-quest journal/expand-first quick card reproduced in source. V97 adds a matching upper-right quest icon next to skills, all 12 catalog entries (2 actual domain quests + existing Hans travel + 9 Master planned/region-locked), all/available/active/completed filters, status badges, scroll/paging, objectives/prerequisites/rewards/next links, and context travel to actual NPC/objective. Quick tracker stays two lines and first touch navigates. Interior exit-to-next-NPC, forest-to-Milles and entry-to-indoor-objective use existing navigation; existing acceptance/reward domains unchanged. Forest visit is journal-only persisted state, no reward. Locked planned quests never become available or travel. Official Black Desert Mobile guide informed list/progress/goal travel structure; game colors/icons remain own. Eight focused native tests and updated former layout assertions wired. CI publishes an anonymous direct APK release after all configured checks. BUILD/NATIVE/PHONE/USER_VISUAL pending exact source CI; candidate unmerged, main unchanged. Contract docs/QUEST_JOURNAL_V97_CONTRACT.md.

## 2026-10-04 KST — V96 weapon and complete defense jump BUILD/NATIVE VERIFIED candidate

Exact source e76991d29bb0fe4e302b2114614ede934bb5fd2c / tree 7feabf98b8953b65f0ec08d0739ad10baf10796a. Actions37160669377 / job111313255835 SUCCESS; APK assembled2026-10-04 08:15:45 KST. APK artifact11287078468,31900040bytes,SHA25652a488ed8d5e4c865395e3609da3241249926a2d0d2e150ce3c4946a1bc81b1e. Native review11287123312:57 PNGs,digest/CRC verified. Eight focused native tests and all configured gates PASS. Manually inspected four-way idle/all actual walk columns,both-sex all four attack types including intermediate twohand frame,real warrior contacts,basic/inn,paper doll,and32 real-input jump start/contact/sphere/landing frames.24 earlier reviewed non-twohand motion rows byte-identical to final output. All949 other V95 packaged assets identical,including original268 BODY frames and source weapon atlas/icon; only presentation catalog/weapon registration manifest changed. Weapon source scale .50/carry ratio43/51, cropped BODY hand anchors before pivot/offset, reviewed preparations. Complete defense uses unchanged original guard BODY with project vertical jump; equipped blade/gear follow actor, shadow/world position and accepted sphere/guard/contact stay fixed. First CI stopped on stale generated RAISE coverage row; regenerated JUMP, runtime tests passed. Walk fixture corrected to drive actual global presentation clock. Original precise jump cadence/four-view weapon/occluded grip/cord remain ADAPTED/PENDING_REFERENCE. PHONE_PENDING / USER_VISUAL_PENDING; V95 user DEVICE_FAILED report preserved. Unmerged candidate; main bfd668d4e178fa82625d634b5a54be0e27ce30a3 unchanged. Full proof docs/verification/CHUNGRYONG_JUMP_V96_BUILD.json.

## 2026-10-04 KST — V96 weapon hand/scale and complete defense jump candidate

V95 DEVICE_FAILED (user-reported): 청룡의숨결 four idle directions/size and all attacks require review. Five supplied screenshots and original article490398 GIFs inspected. Reproduced carry/attack scale inconsistency (.60 carry vs .60*43/51 attack) and root-relative hand guesses below actual source hands. V96 uses source capture 2:1 BODY scale (.50), matched carry ratio43/51, cropped-BODY hand coordinates before pivot/offset registration, per-body dimension adaptation, and source-matched basic/horizontal/vertical preparation poses. No source pixels changed. User explicitly confirms 완전방어 is JUMP: original f4/f5 guard BODY plus project vertical takeoff/contact/landing, stationary world position/shadow and unchanged accepted sphere/combat. Exact original jump cadence/four-view weapon source remain ADAPTED. Native four-direction walk/all four attacks/both bodies, real-input jump/guard/landing and sphere envelope gates wired. BUILD/NATIVE/PHONE/USER_VISUAL pending exact CI; unmerged codex/town-interiors-mobile-v86, main unchanged. Previous Dara fix retained.

## 2026-10-04 KST — V95 Dara recipient fix BUILD/NATIVE VERIFIED candidate

Exact source65d01e51c5a345654dbdb092b5f03a73702101a9/tree1cc7e34bf8669f9b82ea13e0bde2025ceff7d96b. Actions37136447554/job111241849304 SUCCESS, assembled01:28:34KST. APK artifact11278649727 SHA2560183dfb4aa92a01d76bcdb11f08d1e47de0598c10009b329bfdbfad5cac5aa76,31898784bytes,version95. All configured checks pass, including3 new Dara tests. Review11278913858: five native frames, ZIP digest/CRC verified, final inn viewed; all five are byte-identical to manually inspected first-run output. Actual recipient source burst now appears after3sec at HP100/MP90 in explicit test access; damage remains0, HP/MP unchanged, no damage-impact. Four outdoor directions and inn mouse recipient pixel/anchor assertions, expiry, actual death cancellation and normal-MISS suppression pass. First cancellation fixture failed because direct alive=false allowed instant respawn; test corrected to actual RuntimeState.damage, no extra runtime changes. All951 V94 packaged assets byte-identical. V94 user-reported Dara DEVICE_FAILED preserved; this cause fixed and native-reproduced. PHONE_PENDING / USER_VISUAL_PENDING; unmerged candidate, main unchanged. Proof docs/verification/DARA_RECIPIENT_V95.json.

## 2026-10-04 KST — V95 Dara recipient FX repair candidate

V94 DEVICE_FAILED (user-reported): 다라밀공 effect missing on monster. Code diagnosis: default test access skips MP1440 prerequisite while preserving max(0,HP+MP-1440) damage; HP100/MP90 resolves zero/MISS and SkillVfxRenderer drops its source recipient channel. Fix is restricted to explicit test access and resolved Dara zero-damage HIT_FEEDBACK. Show existing classic source burst at the actual recipient after 3sec release; no early caster pulse, no damage-impact pulse, no balance/resource changes, no effect for cancellation or normal MISS. Existing positive-damage path retained. Three native tests cover all four outdoor facings, inn mouse anchor, delay/pixels/expiry, cancellation and normal-mode suppression. BUILD/NATIVE/PHONE/VISUAL pending exact CI. Existing V94 asset bytes unchanged. Active unmerged candidate branch codex/town-interiors-mobile-v86; main unchanged.

## 2026-10-03 KST — V94 청룡의숨결 BUILD/NATIVE VERIFIED candidate
Exact source e586a908a3ce493bc5d12fcc48bee5daa2b079e5/tree fd1a56dfc801baab81bac066322fac0bc3bedca0; Actions 37125545957/job 111209999300 SUCCESS, completed 2026-10-03T22:22:14+09:00. APK artifact 11275595579, 31898712 bytes, SHA256 210a20df1892f1e3261af75e8f08b40eaaf95e7da398c298641a9044fbfe2270. Focused review 11274611522: 24 PNGs, published digest/CRC verified; inventory/equipped paper doll, basic outdoor contact, inn horizontal, both-sex four-way motion and live warrior facings manually reviewed. All configured gates PASS including six weapon tests, prior inn/quest/FX suites and runtime empty-inventory checkpoint regression. All 948 prior packaged assets identical to V93, including 268 original BODY frames; all three additive weapon assets byte-match source. Article490398 icon/four labelled GIFs become 26 transparent independent blade/trail frames, not captured actor/floor. One item is granted to new/old saves without auto equip or replacing ownership; migration and every current-version ownership save record the grant so disposal stays disposed. Invalid saved ownership and unsupported save schemas remain untouched. Only equipped mw_chungryong overrides basic and existing warrior SWING presentation: basic, twohand, horizontal, vertical; source clock maps to existing resolver contact (basic .18sec, applicable warrior .14sec), facing, recovery and shared FX. 투핸드어택 stays passive. Existing clothing/BODY mapping and hand registration are ADAPTED, occluded grip/cord pixels and independent original four views unavailable; one-article reference shortage PENDING_REFERENCE. No original weapon damage/durability/event rules invented. V93 antique/quest/indoor FX fixes retained. DEVICE_PENDING / USER_VISUAL_PENDING; unmerged candidate, main bfd668d4e178fa82625d634b5a54be0e27ce30a3 unchanged. Full proof docs/verification/CHUNGRYONG_WEAPON_V94_BUILD.json; extraction/provenance master/changes/CHUNGRYONG-WARRIOR-V94.json and assets/weapons/chungryong/manifest.json.

## 2026-10-03 KST — V94 청룡의숨결 / 전사 무기 모션 validation candidate
User requests Naver article490398 weapon and warrior motion integration with actual inventory/equip/attack verification. Public browser article and five image/GIF sources inspected and preserved at master/source/weapons/chungryong_490398. Labelled icon plus26 weapon frames extracted into independent transparent layers; c/e original both-sex BODY frames retained unchanged. Basic / twohand / horizontal / vertical profiles apply only when custom weapon equipped; existing warrior skill IDs choose profiles, combat/FX clock/damage/learned/target rules unchanged; 투핸드어택 remains passive. Fresh inventory and one-time old-save migration add one weapon without replacing equipment/progress; corrupt save stays unwritable and untouched. No captured actor/floor or whole screenshot is loaded. Body/hand registration and color matting are ADAPTED; one independent article reference shortage, missing garment groups/occluded blade pixels/original four views remain explicit. Candidate version94; local pixel extraction checked. BUILD/NATIVE/DEVICE/USER_VISUAL PENDING until exact-SHA CI and native capture review. Main bfd668d4 unchanged. Acceptance/provenance master/changes/CHUNGRYONG-WARRIOR-V94.json and weapons/chungryong/manifest.json; six native regression tests wired into Actions.

## 2026-10-03 KST — V93 antique/quest/indoor FX BUILD/NATIVE VERIFIED candidate
Exact source42d20ece678b0a4bc5aa111da468f3dcec31c95b/tree d147d67741f33b41165751e48b419b0d04c18605. Actions37122438002/job111201056317 SUCCESS, completed21:27:24 KST. APK93 artifact11273852663,31768106 bytes,SHA256421302d612e43de3cbfe2e9afc8e1aade7b2d9b6124a6dc2473e0512df87f40d. Focused native review11274261676 downloaded/CRC/digest verified,18 PNGs; actual entry/two depths/growth NPC/Hans/magic/self heal/mouse hit manually inspected. All configured checks PASS,20 inn tests including7 new regressions; binary manifest and six new sprite bytes verified,all942 prior packaged asset bytes identical toV92. Six independent transparent antique sprites replace primitives; chair/table directions, footprints,115 connected tiles,depth and patterned ground rugs verified; neighboring atlas fragments isolated. Shared BODY/FX/camera/utility windows and accepted skill clocks run inside World interiors. Real caster/recipient/monster contact pixels, reward→immediate quest2 unlock→journal→exit→NPC accept→restart and Hans→forest travel verified. Request-ID completion is scoped per active World adapter. Source reference furniture form/material manually compared; antique art and3-table common hall are authored adaptation, not original pixels/full4-table topology. Mary identity/full mouse animation/full original rooms and existing FX-source gaps remain unresolved. V92 DEVICE_FAILED user report retained, installed hash not independently supplied. V93 DEVICE_PENDING / USER_VISUAL_PENDING; candidate unmerged/main unchanged. Proof docs/verification/INN�;��$z{-���jם1183672728; sha256 f6f52fa0d23ba2629062feb30936901b74435677b85bfa89c1775b4cd4dd7d52"
    native_verified: "382 PNGs; full ellipse envelope; live contact clocks; four finisher contacts"
    delivery_evidence: docs/verification/SKILL_FX_V80_BUILD.json
  SKILL_FX_SOURCE_RECHECK_078:
    title: "Recheck all supplied originals and connect omitted labelled source routes"
    system: combat_presentation
    priority: P1
    queue_state: DEVICE_VERIFICATION_PENDING
    claim: Director/Integration
    source_branch: codex/original-skill-contract-v65
    required_sources: [docs/SKILL_FX_AUDIT_CONTRACT.md, docs/verification/SKILL_FX_V77_AUDIT.json, master/source/skill_fx/naver_cafe_archive_20261001/warrior_bindings.json, master/source/skill_fx/naver_cafe_archive_20261001/rogue_bindings.json, master/source/skill_fx/naver_magic_recovered_20261001/definitions.json, master/source/skill_fx/naver_magic_recovered_20261001/media.json, master/source/skill_fx/user_20260930/provenance.json, docs/verification/PROVIDED_SOURCE_RECHECK_V78_ATTACHMENTS.json, app/src/main/java/com/projectdark/mobile/ClassicSkillReference.java, app/src/main/java/com/projectdark/mobile/SkillVfxRenderer.java]
    acceptance: ["All 40 previous fallback IDs have explicit evidence outcomes", "Four omitted original channels reachable through production input", "Same-name reuse remains explicitly adapted registration", "34 missing originals and 2 unlabelled GIFs remain honest gaps", "Exact SHA native render and APK checks"]
    remaining: "Mage22 original particle sources; total34 particle gaps; phone and original visual acceptance"
    build_verified: "4a887579; Actions36880039516 SUCCESS; APK11170802690"
    native_verified: "221 route audit, production input, peak-frame renderer review; native artifact11171027453"
    delivery_evidence: docs/verification/V78_DELIVERY.json
  CLASSIC_MARTIAL_CLERIC_068:
    title: "Import labelled classic martial and cleric references into source and presentation"
    system: combat_presentation
    scenes: [SCENE_MILLES_VILLAGE, SCENE_FIELD_COMBAT]
    priority: P1
    queue_state: VERIFICATION_PENDING
    claim: Director/Integration
    required_sources: [docs/CLASSIC_MARTIAL_CLERIC_V68.md, master/source/skill_fx/naver_classic_2020/provenance.json, master/source/skill_fx/naver_classic_2020/definitions.json, docs/SKILL_SPATIAL_CONTRACT.md, docs/SKILL_ACTION_DECISIONS.csv, master/changes/CLASSIC-MARTIAL-CLERIC-V68.json, app/src/main/java/com/projectdark/mobile/ClassicSkillReference.java, app/src/main/java/com/projectdark/mobile/SkillVfxRenderer.java]
    acceptance: ["61 source rows and byte hashes retained", "55 labelled effect IDs emit separate caster/contact channels", "Source descriptions/stats/icons visible", "Shared legality and no-enemy-heal regressions pass", "Native composited captures reviewed; device gate separately pending"]
    remaining: "Persistent status, travel, party, summon and finisher mechanics; original alpha/occlusion and every-facing pose fidelity"

  SKILL-WINDOW-02:
    owner: Director/Visual
    status: BUILD_VERIFIED
    source_branch: codex/skill-window-source-art-20260930
    runtime_source_sha: 9fb4e3d77f144ef30a4fd342e544b955a369a8c1
    build_checkout_sha: 29bf3cee1fba673aee0be77225ce16089b14fde7
    workflow_run: 36626946163
    apk_artifact: 11060023417
    apk_sha256: a7e2e666db282f34218a86f34a72faf606799544d4df5ccae000e676e6c072c2
    prior_user_verdict: VISUAL_REJECTED_PR168
    source_icons_registered: 75
    verification: "BUILD_VERIFIED / NATIVE_RENDER_REVIEWED / DEVICE_PENDING / VISUAL_PENDING"
    remaining: "Original skill acquisition, combat effects and missing source artwork"
  SKILL-WINDOW-01:
    owner: Director/Game
    status: BUILD_VERIFIED
    requested_by: "User 2026-09-30: 구현해"
    source_branch: codex/skill-window-20260930
    required_sources:
      - design/DATA_CONTRACT.md
      - design/SOURCE_OF_TRUTH.md
      - master/MASTER_MANIFEST.md
      - master/data/Skill_Master.csv
      - master/data/Skill_Requirements.csv
      - master/data/Skill_Legacy_Requirements.csv
      - app/src/main/java/com/projectdark/mobile/SkillDef.java
      - app/src/main/java/com/projectdark/mobile/RuntimeCombatSession.java
      - app/src/main/java/com/projectdark/mobile/F5mSaveStore.java
    verification: "BUILD_VERIFIED / DEVICE_PENDING / VISUAL_PENDING"
    runtime_source_sha: 74c3833e0010e7f0c78e01a70b0c8cafc0ade614
    build_checkout_sha: 5560ccc69e990d08b44172a9aa538815e64a29f3
    workflow_run: 36622542956
    apk_artifact: 11059261609
    apk_sha256: aa1eac489ac840e293a73619e821491288dc53d8e2a4b482cbbe2b0925400d92
  COMBAT_PIPELINE_003:
    title: "Unify movement, target, reciprocal tile adjacency, attack snapshot and hit resolution"
    system: combat_cross_runtime
    scenes: [SCENE_FIELD_COMBAT]
    priority: P0
    queue_state: READY
    claim: null
    evidence: [D214834_PLAYER_TO_MONSTER_ATTACK, D214834_MONSTER_TO_PLAYER_ATTACK, D214834_MONSTER_MOVEMENT, D214834_MOVE_COMBAT_LINK]
    required_sources: [design/DESIGN_CONSTITUTION.md, design/DATA_CONTRACT.md, master/MASTER_MANIFEST.md, app/src/main/java/com/projectdark/mobile/]
    gap: "The device build proves a non-reciprocal combat relationship. Treat this as an upstream pipeline defect, not only an attack-button symptom."
    acceptance:
      - "Player and monster positions used by combat come from the same canonical logical tile-center representation used by movement."
      - "All four legal 64x32 diagonal adjacent relations are reciprocal: if A can melee B, B has the reciprocal relation to A under the same snapshot."
      - "No radius/Euclidean/8-direction bypass can grant monster attack where player canonical adjacency denies the corresponding relationship."
      - "Target selection, legality, facing, attack snapshot, animation direction and hit resolution consume the same immutable relationship for an attack."
      - "Automated audit covers four diagonal relations, reciprocal directions, illegal non-adjacent relations and movement->attack transition."
      - "Exact-head build verification is not device acceptance; fresh APK evidence is required before DEVICE_VERIFIED."

  ATTACK_PRESENTATION_002:
    title: "Restore authored BODY+robe+weapon attack without scale/anchor regression"
    system: character_visual
    scenes: [SCENE_FIELD_COMBAT]
    priority: P0
    queue_state: READY
    claim: null
    evidence: [D214834_ATTACK_BODY, D214834_ATTACK_WEAPON, D214834_ATTACK_SCALE]
    required_sources:
      - master/data/Asset_Animation_Frame_Master.csv
      - master/data/Asset_Animation_Semantics.csv
      - master/data/Asset_Character_Mapping.csv
      - app/src/main/java/com/projectdark/mobile/CharacterRenderer.java
      - app/src/main/java/com/projectdark/mobile/CharacterSemanticRig.java
      - app/src/main/java/com/projectdark/mobile/CharacterSemanticRigEvidenceProbe.java
      - app/src/main/java/com/projectdark/mobile/CharacterVisualEvidenceProbe.java
    failure_history:
      - "Attempt 1: BODY attack presentation was introduced but apparent attack-image scale/anchor did not match the normal character, producing an unnatural multi-pose transition."
      - "Attempt 2: while correcting that regression, meaningful BODY attack was removed/suppressed and weapon-only motion remained."
      - "Current device result: character remains effectively static while mokdo attacks/floats independently. This is not progress past the root problem."
    investigation_gate:
      - "Before renderer edits, enumerate and audit every authored attack BODY/robe/weapon resource and Master mapping actually present."
      - "Prove which files are direction variants and which are genuine temporal frames. Never count action02_0..3 as temporal merely because four resources exist."
      - "For each consumed pose/frame record source canvas dimensions, opaque bounds, foot anchor, torso/dominant-hand semantic anchors and runtime scale."
      - "Compare idle/walk and attack apparent body dimensions/foot placement before choosing scale/translation."
      - "If genuine source temporal sequence is incomplete, record UNRESOLVED_SOURCE_SEQUENCE instead of fabricating frames or hiding BODY attack."
    acceptance:
      - "Attack visibly changes player BODY pose; weapon-only attack is automatic FAIL."
      - "If genuine authored temporal BODY frames exist, at least two distinct temporal poses must be observed in runtime; direction variants do not count."
      - "Idle/walk -> attack -> idle preserves apparent body scale and world foot anchor with no size pop or position jump."
      - "Robe consumes the same attack state and BODY anchor contract."
      - "Mokdo handle remains attached to the dominant-hand semantic anchor throughout startup/contact/recovery."
      - "Suppressing/removing BODY attack cannot satisfy acceptance."
      - "Evidence probes must validate semantic/scale/anchor/runtime-state continuity, not only bitmap loadability."

  WEAPON_RIG_001:
    title: "Finish weapon-to-hand attachment across walk and attack"
    system: character_visual
    scenes: [SCENE_FIELD_COMBAT]
    priority: P1
    queue_state: READY
    claim: null
    evidence: [D214834_ATTACK_WEAPON, D214834_WALK_WEAPON]
    required_sources: [master/data/Asset_Animation_Semantics.csv, master/data/Asset_Animation_Frame_Master.csv, app/src/main/java/com/projectdark/mobile/CharacterSemanticRig.java, app/src/main/java/com/projectdark/mobile/CharacterRenderer.java]
    acceptance:
      - "No visible floating weapon during attack."
      - "Walk direction/frame changes preserve handle-to-hand attachment without twisting/pop beyond source-authored motion."
      - "Weapon rig consumes BODY semantic anchor rather than independent screen-space magic offsets as primary truth."

  HIT_READABILITY_001:
    title: "Make attack contact and target reaction readable"
    system: combat_presentation
    scenes: [SCENE_FIELD_COMBAT]
    priority: P1
    queue_state: READY
    claim: null
    evidence: [D214834_HIT_PRESENTATION, D214834_COMBAT_READABILITY]
    required_sources: [design/DESIGN_CONSTITUTION.md, master/data/Asset_Animation_Semantics.csv, app/src/main/java/com/projectdark/mobile/]
    acceptance:
      - "Damage/HP mutation occurs at the same contact phase communicated by attack presentation."
      - "Target reaction/contact feedback makes attacker, target and contact timing readable without relying only on HP numbers."
      - "Do not fabricate unsupported source animation; use existing authored events/assets first and mark adapted presentation when necessary."

  WORLD_MILLES_001:
    title: "Advance Milles from correct terrain semantics to coherent lived-in village composition"
    system: world_map
    scenes: [SCENE_MILLES_VILLAGE]
    priority: P1
    queue_state: READY
    claim: null
    evidence: [D214834_MILLES_GROUND, D214834_MILLES_PATHS, D214834_MILLES_COMPOSITION]
    required_sources: [docs/MILLES_WORLD_DIRECTION.md, master/data/Asset_Map_Mapping.csv, master/data/Map_Instance_Master.csv, assets/milles/production/terrain/, assets/milles/production/buildings/, assets/milles/production/landmarks/]
    preserve:
      - "Current grass/dirt semantic correction is PARTIAL PASS and must not regress."
    acceptance:
      - "Paths read as intentional village circulation rather than repeated broad diagonal bands."
      - "Plaza, residential, commerce/equipment, church/quiet, waterside and gate/outskirts form one coherent spatial story with breathing room."
      - "Large empty flower-grass expanses are reduced through purposeful district composition, vegetation/props or intentional open space, not random clutter."
      - "World work must stop at a practical acceptance gate and advance to other game systems rather than endless Milles polish."

  MONSTER_RUNTIME_001:
    title: "Replace temporary monster presentation through authored monster path"
    system: monster
    scenes: [SCENE_FIELD_COMBAT]
    priority: P1
    queue_state: READY
    claim: null
    evidence: [D214834_MONSTER_VISUAL]
    required_sources: [master/data/Monster_Master.csv, master/data/Monster_Sources.csv, master/data/Monster_Research_Audit.csv, master/data/Asset_Monster_Mapping.csv, master/data/Spawn_Master.csv]
    acceptance:
      - "Audit existing authored/master monster mappings before creating substitute visuals."
      - "Accepted monster presentation must no longer read as the temporary simple placeholder seen in DEVICE_20260915_214834."
      - "Walk-facing asset direction matches the committed tile-step vector throughout interpolation, regardless of a stale attack-facing snapshot."
      - "Player/monster and monster/monster occupancy checks include configured actor clearance, moving destinations, and crossing in-flight paths."
    current_followup:
      reported_apk_version: "0.53"
      report_status: "DEVICE_FAILED (initial user report); v0.54 user-reported device pass"
      report: "Automatic combat sometimes overlapped the player and a monster; user now reports the v0.54 spacing scenario passed."
      root_cause: "Source-level omission was fixed in v0.54. User reports pass; no device model/video was supplied in the status message."
      fix_version: "0.54"
      latest_user_report: "User reports the delivered v0.56 APK still has occasional Pamfet movement art/facing mismatch and automatic combat circling/inefficient target routing."
      latest_report_state: "DEVICE_FAILED (user-reported for APK SHA-256 2fc04baed7c4d53942c129a91cd61a3864243cceb10cc55749361e1d112a2ec9)"
      root_cause_status: "UNKNOWN on device. Source audit found monster AI uses greedy pursuit with collision-substituted side steps, which can repeat instead of following a planned detour. Generated walk-art gaze was not visually accepted."
      fix_candidate_version: "0.57-pote-chase-pathfinding"
      fix_candidate_scope: "Route monster pursuit over legal authored tiles to melee adjacency using BFS; keep replanned obstacle detours stable; retain exact movement-vector facing and shortest legal player auto-approach target selection."
      fix_candidate_state: "IMPLEMENTED / CI_PENDING / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING"
      ci_blocker: "Local Master integrity audit now passes. Exact-SHA Actions pending; local checkout has no Gradle wrapper and system gradle is unavailable."
      failed_delivery_sha256: "2fc04baed7c4d53942c129a91cd61a3864243cceb10cc55749361e1d112a2ec9"
      failed_delivery_actions_run: "36588894898"
      failed_delivery_artifact_id: "11043600170"
      candidate_branch: "codex/pote-facing-autotarget-r4-20260930"
      candidate_source_sha: "90e539660ce850217b00775c17536d5381e3e09d"
      manual: "docs/MONSTER_CREATION_MANUAL.md"
      next_check: "Run exact-SHA GitHub Actions; then verify Pamfet image gaze for four movement directions, blocked-tile detours, shortest-path auto-target selection, no circling, and the previously passed Pote restart behavior on-device. Do not deliver an unverified APK as a fix."
      implementation_source_sha: "5905e963de7898acec4b84644e78fcb98cec395e"
      implementation_state: "BUILD_VERIFIED"
      observable_state: "DEVICE_VERIFIED (user-reported)"
      device_report_scope: "Spacing/approach, combat relation and BODY+robe+weapon attack presentation passed per user; weapon hand attachment and hit readability explicitly deferred."
      actions_run: "36570339135"
      apk_artifact: "11033662723"
      apk_sha256: "97b892b9afd8afb82c888db1ed13305f565e3072300900e26ffbf5ec848f2fb4"
      manual: "docs/MONSTER_CREATION_MANUAL.md"
      next_check: "Complete First RPG Loop cold-restart restoration for Milles/Pote; test persisted map, valid position and duplicate-reward watermark."

  F5M_RESTART_RESTORE_001:
    title: "Restore the active map and complete First RPG Loop state after process restart"
    system: persistence
    scenes: [SCENE_MILLES_VILLAGE, SCENE_FIELD_COMBAT]
    priority: P1
    queue_state: DEVICE_PENDING
    claim: null
    required_sources:
      - app/src/main/java/com/projectdark/mobile/F5mSaveStore.java
      - app/src/main/java/com/projectdark/mobile/GameView.java
      - app/src/main/java/com/projectdark/mobile/MainActivity.java
      - app/src/main/java/com/projectdark/mobile/RuntimeState.java
      - app/src/main/java/com/projectdark/mobile/world/WorldRuntimeAdapter.java
      - app/src/main/java/com/projectdark/mobile/world/PoteFieldDef.java
      - app/src/test/java/com/projectdark/mobile/F5mRestartPersistenceMatrixTest.java
      - app/src/test/java/com/projectdark/mobile/RuntimeCheckpointTest.java
      - docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md
    gap: "Device report: quitting in Pote restarts in Milles; dying in Pote respawns at the center but controls do not move. Source audit: startup ignored saved map_id, and Pote death/revive paths reset only the Milles adapter/input state. Pote showcase monsters also lack canonical EXP and drop quantities/rates, so they cannot safely provide invented reward fixtures."
    acceptance:
      - "Cold restart restores the saved Milles or Pote map and a valid walkable player tile before camera/adapter binding."
      - "HP/MP/alive, quest state and objective progress, inventory/equipment, level/EXP/Gold/stats, and combat reward watermark round-trip."
      - "A process kill after a monster defeat or quest turn-in does not duplicate rewards/objective counts."
      - "Invalid or older save data falls back safely and is not destructively overwritten."
      - "Automated process-boundary tests cover Milles and Pote; exact APK is cold-restarted on device through the First RPG Loop."
      - "Pote death and revive clear controls and reset the active Pote adapter/camera; joystick movement works after revive."
      - "Reward tests use only verified/explicitly adapted rewards; unresolved Pote monster drops are not silently granted."
    current_followup:
      implementation_state: "BUILD_VERIFIED"
      observable_state: "DEVICE_PENDING"
      device_report: "User reports quitting from Pote returns to Milles; Pote death returns to center but movement fails. Fix is not yet device-verified."
      reward_boundary: "POTE_PURPLE/RED/GREEN/SILVER/LYCAN have no verified EXP/drop quantity/rates. POTE_SPIRIT EXP 308950 is verified, but its drop odds/quantities are unresolved. Test persistence using existing quest rewards/equipped gear; do not invent monster loot."
      implementation_source_sha: "0bbb6ee354af2e67e4513db7e5bb7e643c310052"
      actions_run: "36579231684 (run 1490; initial run 1489 failed two new assertions, corrected tests passed)"
      apk_artifact: "11038421617 (PROJECT_DARK-debug-0bbb6ee354af2e67e4513db7e5bb7e643c310052)"
      apk_sha256: "b79e1c58742f006cecc167a6e66f16b6ba9501d8d4a70d506a790d149aeb603a"
      artifact_zip_sha256: "046864636300cf00931b31843326eafae7bf59bdada89d4f09394d9b5eec8353"
      verification_scope: "All workflow steps passed: compile, F5M restart persistence, runtime checkpoint/migration, regression suites, and debug APK build. No physical-device verification yet."
      next_check: "Install exact v0.55 APK; cold-restart while in Pote, revive after Pote death and walk; use the existing quest reward and carried gear to validate inventory/equipment/progression save restoration."

  HUD_WORLD_BALANCE_001:
    title: "Reduce HUD competition with world while preserving controls"
    system: ux
    scenes: [SCENE_MILLES_VILLAGE, SCENE_FIELD_COMBAT]
    priority: P2
    queue_state: READY
    claim: null
    evidence: [D214834_UI_WORLD_BALANCE]
    required_sources: [design/DESIGN_CONSTITUTION.md, master/data/Asset_UI_Mapping.csv, app/src/main/java/com/projectdark/mobile/GameView.java]
    acceptance:
      - "Quest/chat/skill/control presentation remains usable but no longer visually dominates the playable world."
      - "Touch routing and existing functional controls must not regress while visual hierarchy/occupied area is improved."

  MILLES_STORY_QUEST_UI_MOUSE_AI_001:
    title: "Implement adapted Milles story quest journal and verify mouse AI parity"
    system: [quest, ux, monster]
    scenes: [SCENE_MILLES_VILLAGE, SCENE_FIELD_COMBAT]
    priority: P1
    queue_state: BUILD_VERIFIED
    claim: null
    required_sources:
      - AGENTS.md
      - docs/DIRECTOR_GUIDE.md
      - docs/DIRECTOR_BACKLOG.md
      - docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md
      - docs/PROJECT_STATE.yaml
      - docs/CHAT_HANDOFF.md
      - docs/DECISION_LOG.md
      - docs/MONSTER_CREATION_MANUAL.md
      - design/DATA_CONTRACT.md
      - design/SOURCE_OF_TRUTH.md
      - master/MASTER_MANIFEST.md
      - master/RECONCILIATION.md
      - master/DATA_CONSUMPTION_CONTRACT.md
      - master/data/Quest_Runtime_Master.csv
      - master/data/NPC_Master.csv
      - master/data/NPC_Runtime_Master.csv
      - master/data/Monster_Master.csv
      - app/src/main/java/com/projectdark/mobile/GameView.java
      - app/src/main/java/com/projectdark/mobile/RuntimeState.java
      - app/src/main/java/com/projectdark/mobile/WorldDef.java
      - app/src/main/java/com/projectdark/mobile/WorldEntityPresentationRenderer.java
      - app/src/main/java/com/projectdark/mobile/TownNpcRenderer.java
      - app/src/main/java/com/projectdark/mobile/F5mCompletionPresentation.java
      - app/src/main/java/com/projectdark/mobile/world/MillesDoorAnchors.java
      - app/src/main/java/com/projectdark/mobile/world/TownInteriorDef.java
      - app/src/main/java/com/projectdark/mobile/world/TownInteriorDetails.java
      - app/src/main/java/com/projectdark/mobile/world/TownInteriorRenderer.java
      - app/src/main/java/com/projectdark/mobile/world/MillesProductionCollision.java
      - app/src/main/java/com/projectdark/mobile/world/MillesProductionCollisionAudit.java
      - app/src/main/java/com/projectdark/mobile/F5mAdaptedPrologueQuest.java
      - app/src/main/java/com/projectdark/mobile/F5mAdaptedPrologueQuestAudit.java
      - app/src/main/java/com/projectdark/mobile/F5mRuntimeBindingAudit.java
      - app/src/main/java/com/projectdark/mobile/F5mWorldUxQuestPresentationAudit.java
      - app/src/main/java/com/projectdark/mobile/GrowthQuest2.java
      - app/src/main/java/com/projectdark/mobile/F5mSaveStore.java
      - app/src/main/java/com/projectdark/mobile/F5mQuestUiFlow.java
      - app/src/main/java/com/projectdark/mobile/ActiveQuestTracker.java
      - app/src/main/java/com/projectdark/mobile/MonsterAIController.java
      - app/src/main/java/com/projectdark/mobile/MonsterDefinitionRegistry.java
      - app/src/test/java/com/projectdark/mobile/HudTouchAcceptanceTest.java
      - app/src/test/java/com/projectdark/mobile/MonsterChasePathfinderTest.java
      - app/src/test/java/com/projectdark/mobile/PoteMonsterPlayableRuntimeTest.java
      - app/src/test/java/com/projectdark/mobile/TownInteriorTest.java
      - app/src/test/java/com/projectdark/mobile/FieldTransitionE2ETest.java
      - app/src/test/java/com/projectdark/mobile/MillesStoryQuestTest.java
      - assets/milles/production/maps/milles_garden.json
      - assets/milles/production/maps/milles_garden_base.json
    gap: "Current prologue is a generic guide-to-training-dummy loop; no canonical mouse identity or mouse sprite exists in the Master/assets. User requests an adapted story, named NPCs, a useful quest journal, and a verified shared-AI mouse comparison."
    acceptance:
      - "Project-authored Milles NPCs have stable IDs, distinct role-specific names/appearance, and connected dialogue/quest progression."
      - "Quest journal exposes state, issuer, objective, progress, reward and next destination without breaking touch routing."
      - "Mouse remains explicitly ADAPTED/B and uses the same MonsterAIController chase, path, attack and combat resolver behavior as the control monster."
      - "Focused unit/UI tests pass; exact build and device verification are reported separately."
      - "Canonical Q_POT_01 and unknown original mouse facts remain unchanged/unclaimed."
    current_followup:
      implementation_state: "BUILD_NATIVE_VERIFIED_V93"
      observable_state: "DEVICE_PENDING (V93); prior V92 DEVICE_FAILED user-reported"
      evidence: "Exact V93 source42d20ece678b0a4bc5aa111da468f3dcec31c95b passed Actions37122438002/job111201056317 at21:27:24 KST. APK11273852663 SHA256421302d612e43de3cbfe2e9afc8e1aade7b2d9b6124a6dc2473e0512df87f40d; native20 inn tests/7 new regressions and18 PNGs verified. Source/body942 asset bytes retained,6 new sprites match APK. Full proof docs/verification/INN_DETAIL_V93_BUILD.json; historical V90/V91/V92 device reports preserved in handoff/decision/verification records."
      next_check: "Install exact V93 source42d20ece APK: Actions37122438002 SUCCESS; native20 inn tests/7 new regressions,18 focused screenshots reviewed; manifest/new6 sprite bytes and prior942 asset bytes verified. APK11273852663 SHA256421302d612e43de3cbfe2e9afc8e1aade7b2d9b6124a6dc2473e0512df87f40d; proof INN_DETAIL_V93_BUILD.json. Handset/user visual acceptance pending; V92 user-reported primitive furniture/quest/FX DEVICE_FAILED retained."

  CHUNGRYONG_WEAPON_WARRIOR_001:
    title: Source-backed 청룡의숨결 item and warrior weapon presentation
    system:
    - item
    - inventory
    - equipment
    - animation
    - combat
    scenes:
    - SCENE_MILLES_VILLAGE
    - SCENE_FIELD_COMBAT
    priority: P1
    queue_state: BUILD_VERIFIED
    claim: null
    required_sources:
    - master/source/weapons/chungryong_490398
    - master/changes/CHUNGRYONG-WARRIOR-V94.json
    - docs/SKILL_PRESENTATION_CONTRACT.md
    - app/src/main/java/com/projectdark/mobile/ChungryongWeaponRenderer.java
    - app/src/test/java/com/projectdark/mobile/ChungryongWeaponTest.java
    acceptance:
    - Actual inventory input equips/unequips/restarts without replacing old ownership.
    - Source blade and four motions remain visible on both original BODY identities
      and four facings.
    - Real quickslot/basic contact, indoor recipient FX and existing combat rules pass.
    - Physical phone and user visual acceptance are recorded separately.
    current_followup:
      implementation_state: BUILD_NATIVE_VERIFIED_V94
      observable_state: DEVICE_PENDING / USER_VISUAL_PENDING
      implementation_source_sha: e586a908a3ce493bc5d12fcc48bee5daa2b079e5
      actions_run: 37125545957
      apk_artifact: 11275595579
      apk_sha256: 210a20df1892f1e3261af75e8f08b40eaaf95e7da398c298641a9044fbfe2270
      evidence: docs/verification/CHUNGRYONG_WEAPON_V94_BUILD.json; six weapon tests
        and 24 native PNGs; prior 948 assets identical.
      limitations: ADAPTED BODY/grip/clothing mapping; occluded weapon pixels; mirrored
        four views; one independent reference; no original stats/durability claim.
      next_check: Install exact V94 candidate, equip 청룡의숨결 in inventory, try basic attack
        and warrior 숏블레이드/메가블레이드/크래셔 in field and inn, restart. Preserve V93 furniture/quest/FX
        fixes.

agent_selection_policy:
  - "Every scheduled Dev/Verify run MUST load current PROJECT_STATE and DEVICE_20260915_214834 before selecting or verifying work."
  - "Every finding in DEVICE_20260915_214834 is mapped to a work package or explicit preserve constraint; no finding may silently disappear because another P0 is being worked."
  - "Select by severity and dependency. COMBAT_PIPELINE_003 is upstream P0; ATTACK_PRESENTATION_002 is a parallel P0 presentation blocker. Neither may be treated as the sole project task."
  - "ATTACK_PRESENTATION_002 investigation_gate is mandatory before renderer modification to prevent repeating the two-day scale->BODY-removal failure loop."
  - "After a package reaches its acceptance gate, update its status/evidence and advance to the next highest-value dependency-valid READY package. Do not micro-polish an accepted item."
  - "Verifier must cross-check the package against its referenced device finding IDs; a code/test pass that does not satisfy the finding is FAILED/VERIFICATION_PENDING, not ACCEPTED."
  - "Build pass != device pass != visual acceptance. Fresh exact-APK device evidence is required to close device-visible failures."
  - "User-reported v0.54 device acceptance on 2026-09-29: automatic-combat spacing, combat relation and BODY+robe+weapon attack presentation passed; weapon hand attachment and hit readability remain deferred. Exact user device/build evidence was not included in the report."

status_summary:
  visual_acceptance: FAIL
  latest_device_evidence: DEVICE_20260915_214834
  unresolved_device_findings: [D214834_PLAYER_TO_MONSTER_ATTACK, D214834_MONSTER_TO_PLAYER_ATTACK, D214834_MONSTER_MOVEMENT, D214834_ATTACK_BODY, D214834_ATTACK_WEAPON, D214834_WALK_WEAPON, D214834_ATTACK_SCALE, D214834_HIT_PRESENTATION, D214834_COMBAT_READABILITY, D214834_MOVE_COMBAT_LINK, D214834_MILLES_PATHS, D214834_MILLES_COMPOSITION, D214834_UI_WORLD_BALANCE, D214834_MONSTER_VISUAL]
  preserve_findings: [D214834_MILLES_GROUND]
  next_global_rule: "Treat the full 21:48 device matrix as current project truth. Repair upstream combat reciprocity/pipeline and authored attack presentation under separate gates, then consume remaining P1/P2 findings without allowing any one defect to monopolize development. Preserve the corrected Milles grass/dirt semantics."
