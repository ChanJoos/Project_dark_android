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
