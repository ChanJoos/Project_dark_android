# Source-reviewed skill spatial contract — v65

v64 is DEVICE_FAILED / VISUAL_REJECTED (user-reported). Code audit reproduced missing skill tile rules, preview bypass, wrong passive attack and generic finisher effects. Original-equivalence and physical device acceptance remain pending.

`SKILL_ACTION_DECISIONS.csv` contains all219 stable IDs, each with source/status, mode, tile pattern, minimum/maximum reach, body motion, caster effect, recipient effect, mechanics support and unresolved notes. There is no name substring inference in the runtime or generator. `master/changes/SKILL-SPATIAL-V65.json` records the accepted projection corrections; the original Master snapshot is preserved.

## Applied rules

| Class | Rules connected to Resolver |
|---|---|
| Warrior | front1/3, cross1, screen-wide roar, passive two-hand permission, basic-linked2/3/4 hits; distinct green/white/red blades and blue/red crasher stems |
| Rogue | front1, front/back1, shuriken max4 and non-adjacent, Amnesia non-adjacent, passive linked basic hits; distinct reconstructed assassination/ambush/backstab impacts |
| Martial | different extended front/side kick source frames, front3 Baekbo/Muyeong, cross kicks/Guyang, surrounding8 Bungshinseon, ranged single Dara3-second cast |
| Mage | single, caster-cross Supe, target-cross Ex, screen-wide elemental/ultimate spells, target-cross4 Summa, screen-wide upgraded curses |
| Cleric | single light attacks, dark screen-wide HolyDragon, ally/self heals, group heal/dispel recipients restricted to actual allies |
| Common | existing equipped basic attack, self-heal; interaction skills do not emit fake monster hits |

72 offensive spell/technique definitions and16 heal definitions plus equipped basic attack are real damage/healing paths with explicitly adapted numeric balance. Status/buff presentation tests use the same start/contact legality and recipient masks with zero damage, but persistent buffs, poison/sleep, recognition state/reset and summons are not thereby implemented. Linked basic hits use the highest applicable learned tier plus installed-trap passive, one Resolver charge/cooldown and one aggregate ledger hit; exact original server sub-hit roll behavior remains unresolved.

Every accepted action validates job/learned state, cooldown, MP, actor/target, tile reach, visibility and LOS before charge. At contact, start legality is checked again, area recipients are enumerated from current positions, duplicates removed, and living/visible/LOS-legal recipients receive exactly one effect event. Caster emission is keyed by action; target emission is keyed by action and recipient. No archived test bypass exists. Test mode still grants virtual skills/refunds MP without touching normal progression; normal lists/job gates remain intact.

Tile geometry is the accepted four-axis64x32 isometric grid. Front rays exclude diagonals/rear; cross masks exclude diagonal tiles;8-surround masks include diagonals but exclude caster. Target-cross includes the selected tile. Screen-wide spells use the actual current viewport and same-map runtime entities. Maximum single spell distance is unresolved: visible-target limit is a project adaptation, replacing arbitrary150px. Shuriken's tile metric and Amnesia max4 are explicitly adapted. Warrior finisher reach1 remains adapted until direct range evidence is found. Group services currently have only the player entity; no enemy is healed or dispelled as an ally.

## Source and visual limits

Packaged `skill-presentation/research.json` records sources, versions, failed retrievals and video sampling. User PDF sections cover every catalog job. Tistory warrior/rogue/mage/martial posts and Nexon-hosted fan discussions were checked; they are V/FAN, not official server rules. 2023 official ki-skill changes were read only to avoid replacing classic Bungak1 with the newer Ki-Bungak3. Naver was blocked by robots; Gamezone origin/GIF retrieval returned502, Tistory screenshot CDN403 and discovered YouTube page was throttled. Supplied YouTube recordings were visually sampled and show mostly travel, not individually identified skill impacts.

Original effect pixel match count remains0. Source-described blade and crasher color/form are newly reconstructed in an alpha-preserved48-frame generated sprite atlas and Canvas; other finisher silhouettes and existing elemental/status atlases are ADAPTED. Exact original frames/timing for individual finishers are unresolved. Male/female bodies are unchanged source bytes; extended kick frame choices are project selections from visibly inspected frames. Gear with no matching motion retains the previously documented static registration fallback. No original visual acceptance is claimed.

Dash, leap-over, teleport, summon, player-specific interactions and four unclear entries are gated instead of playing wrong standstill attack effects. Warrior dash's confirmed front5 rule and martial leap's adjacent rule are retained in decisions, with movement/landing service unresolved. Pacheongak exact mask, two Climens IDs, Dinosense, unknown maximum reaches, per-skill original FX and status mechanics are outstanding. This release improves all-class source-informed contracts; it is not a claim of complete219 original mechanics.

## Verification

Finisher atlas: `app/src/main/assets/skill-presentation/finisher-v65.png`, built-in imagegen,6columns×8rows, original pixel matches0. Prompt summary and SHA256 are in packaged research.json. Pixel assets remain adaptations even when their color/form is source described.

CI must regenerate presentation and `SkillActionData`, run existing skill/window/save/world regressions and new `SkillSpatialContractTest`: fixed original tile oracles in four directions, all219 rule coverage, real six-class/action recipient sets, out-of-range/wall/viewport/moving-target exclusion, no passive/utility fake impacts, linked basic tiers, per-recipient VFX dedup and production GameView finisher screenshots. Source SHA, actual PR checkout, CI/artifact and APK digest are added after build. Physical Android/device and user visual acceptance remain pending.


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
