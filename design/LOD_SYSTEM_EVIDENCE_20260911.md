# PROJECT DARK — LOD SYSTEM EVIDENCE INDEX — 2026-09-11

Status: RESEARCH INDEX / `[FAN]` evidence, not official canon
Source: user-supplied PDF snapshot of NamuWiki `어둠의 전설`, recent edit timestamp shown as 2026-09-09 13:18:33.

Purpose: prevent PROJECT DARK from reducing the original game to movement/combat alone. This document inventories source-supported systems and marks follow-up evidence needs. User-confirmed PROJECT DARK rules and official/original evidence outrank this file.

## 1. Identity and play pattern
- MMORPG / long-running Nexon classic RPG.
- Pixel-art visual identity remains a defining trait across eras.
- Party hunting and differentiated party roles are described as foundational.
- Non-leveling content inventory includes story quests, PvP battle spaces, amusement/social spaces, museum/lore, gathering and fishing.
- Community/social interaction is described as a major retention/play pattern.

Design consequence: preserve party/social/world identity in long-term architecture; do not make the product a disconnected combat-room grinder.

## 2. Movement and field presentation
- `[FAN]` live game is described as four-directional with character fronts on the four screen diagonals.
- Historical 8-direction material is described as test-only/early material in the interface-history section.

PROJECT DARK resolution: user-confirmed runtime remains exactly `NW / NE / SW / SE`. No 8-direction expansion.

## 3. Core stats
Inventory:
- STR: basic attack/carry-weight association.
- INT: magic attack association.
- WIS: healing, MP recovery, level-up MP growth association.
- CON: defense, HP recovery, level-up HP growth association.
- DEX: basic-attack evasion and critical association.

Progression inventory:
- Level 1–99: 2 stat points per level is stated in this fan source.
- Skill/magic learning can require stat thresholds.
- Post-99: EXP can purchase additional points.
- Job-specific purchasable-stat maxima exist; `올포` terminology is documented.

Evidence gate: exact formulas, thresholds, purchase costs and caps require official/original or stronger corroboration before numeric canon.

## 4. Element/attribute system
Nine named categories are described:
`무 / 수(바다) / 토(대지) / 풍(바람) / 화(화염) / 목(숲) / 금(강철) / 어둠(암흑) / 빛(생명)`.

Important structural evidence:
- attack and defense attributes interact;
- same monster type can have variable/random attribute in the described system;
- thief `센스` can reveal monster attribute;
- party communication therefore has mechanical value;
- water/earth/wind/fire relationship and later wood/metal/dark/light behavior are described, but exact multipliers are not promoted here.

Design consequence: `MonsterDefinition` cannot assume one immutable elemental defense solely from monster species if we choose this era/rule; encounter/spawn state may need an attribute field. Combat needs data-driven attribute resolution.

## 5. Jobs
Five base jobs are explicitly central and strongly differentiated:
`전사 / 도적 / 마법사 / 성직자 / 무도가`.

The source describes customization through job-change progression and references pure-job development. PROJECT DARK current scope remains through first advancement only; later advancement remains research inventory.

Design consequence: retain job-specific role, learning requirements, targeting and equipment/action identity. Do not normalize every job into one generic skill system.

## 6. Techniques vs magic and targeting
Source distinguishes them operationally:
- techniques commonly activate according to their own range, often in front of facing character;
- targeted magic commonly requires choosing a target unless self/global-type;
- desktop game later added key mapping, pre-targeting and macro facilities to mitigate control burden.

Design consequence for mobile:
- keep `TECHNIQUE` and `MAGIC` distinct in data/UI;
- action targeting must be explicit per action (`SELF`, `FACING_AREA`, `SELECTED_TARGET`, `GLOBAL`, etc.);
- mobile quick slots/target lock can be `[ADAPTED]` replacements for keyboard/mouse burden;
- do not reproduce PC key macros as a required mobile mechanic.

## 7. Diary / character history
The source describes an `일기장` that records quests/events experienced by the character, using in-fiction `세오` year/season notation rather than real-world time.

Design consequence: reserve a persistent character-history/event-log domain separate from transient quest UI. Exact calendar rules and record text remain PENDING.

## 8. PvE inventory
Current/modern source index lists:
- 퀵던전
- 차원의균열
- 베크나탑
- 물의신전
- 혼돈의탑
- 보스레이드

These are discovery inventory only. Current PROJECT DARK P0 does not implement them. Era fit must be decided before inclusion in target progression.

## 9. PvP inventory
- 길드대전
- 배틀로얄

Historical/retired inventory:
- 칭호대항전
- 루딘의서 서버대항전
- 천하제일무한대전
- 공성전
- 반혼의 결서

Design consequence: model `content lifecycle/era` in research, not as an undifferentiated feature list.

## 10. World/regions and hunting grounds
The source points to a dedicated hunting-ground document and names many regions through BGM associations. Examples include:
- 밀레스 성 / 밀레스 마을 / 밀레스 지하묘지
- 우드랜드
- 루어스 대평원 / 루어스 성 / 루어스 마을
- 피에트 / 아벨 / 수오미 / 호엔 / 로톤 / 운디네 / 오렌 / 노엠 / 아슬론 / 아만 / 화론 / 타고르 / 뤼케시온 / 죽음의 마을
- 카스마늄광산, 적룡굴, 물의 신전, 해저 던전, 메카닉 던전, 호러캐슬, 백작부인의 별장 and others.

World-history section describes eastern continent `마이소시아` and western continent `메데니아` as a broader progression/world arc.

Design consequence: create a future `Region → Map → Portal → HuntingGround/Dungeon` evidence graph. Do not infer map geometry from names/BGM table.

## 11. BGM / audio metadata
The PDF includes a long BGM-to-region association table. This is useful as metadata/evidence for regional identity, not permission to redistribute audio. Audio licensing/source remains separate.

Immediate useful association: `Legend of darkness (b-edit M)` is listed with 밀레스 성; later table entries include 밀레스 마을 and 밀레스 지하묘지 associations.

## 12. UI history and lessons
Historical screenshots in the supplied PDF show:
- world-first quarter/isometric presentation;
- persistent HP/MP meters and dense bottom information/action area in older clients;
- later unified skill/magic/item windows and quest list;
- 2019 quick-dungeon affordance;
- 2020/2022 engine/UI changes.

The source criticism of the 2022 renewal is design-relevant: modernization can improve appearance yet harm information visibility, control efficiency and identity. PROJECT DARK should modernize interaction while preserving the information hierarchy players need.

## 13. Party dependence — preserve identity, not historical pain
The source describes very high party dependence and explicit role requirements, including healer importance. It also documents long party-formation times and difficulty for solo/new players.

Design consequence:
- preserve role synergy and meaningful cooperation;
- do not require historical waiting/friction as authenticity;
- onboarding/solo fallback/matchmaking conveniences may be `[ADAPTED]` if they do not erase class roles.

## 14. Death and durability — version before implementation
Historical source describes extremely harsh death loss and item destruction, later mitigated. Durability historically could destroy items at zero, later improved.

Design consequence: never implement one of these historical states accidentally. Add an explicit era/rule decision before production implementation. Exact current/target policy PENDING.

## 15. Economy / monetization lessons
The source documents later inflation/deflation, cash-shop dependence, paid progression pressure, expensive high-tier items and enhancement. These are historical/evaluation evidence, not target monetization requirements.

Design consequence: PROJECT DARK does not inherit exploitative friction merely for fidelity. Economy/monetization requires its own explicit product decision.

## 16. Open field vs instance lesson
The source records controversy where quick-dungeon efficiency marginalized open-field hunting and reduced spontaneous social interaction; later open-field content also produced conflict/interference.

Design consequence: future content design should preserve shared-world social presence while providing bounded ways to avoid blocking/griefing. Do not blindly choose 100% instanced or 100% contested open-field structure.

## 17. Community evidence sources to investigate
The PDF identifies:
- 성천직자의 어둠의전설 카페 — historically large information archive, though some information was lost/removed;
- user-made `어둠의전설 위키 뉴비 기본가이드` — entry-level guide aggregation/search;
- community galleries and historical sites.

Use community material as `[FAN]`; cross-check important numbers and original-era claims.

## 18. Follow-up evidence backlog
Priority A — directly affects current/near runtime:
1. `어둠의 전설/직업`
2. `어둠의 전설/기술마법`
3. `어둠의 전설/사냥터`
4. original/official Milles screenshots/maps/NPC placements
5. original character/equipment sprites and directional animation evidence

Priority B — progression/data:
6. exact Lv1–99 stat rules and job learning requirements
7. HP/MP growth and post-99 EXP purchase rules
8. exact attribute matrix/multipliers and `센스`
9. item/equipment/economy/repair rules by target era
10. diary/calendar rules

Priority C — later content:
11. PvE modes
12. PvP/guild systems
13. gathering/fishing/social spaces
14. later advancement/Ability/Ether/Nageling/Metenia content

## 19. Current sprint firewall
This research **must not derail the 24-hour visual/player slice**. Current P0 remains:
- visible correct martial-artist character;
- 4 diagonal adjacent-tile movement at latest user-approved timing;
- one production-quality sprite-based Milles village screen;
- integrated APK.

Newly discovered systems enter the design/data backlog only until P0 visual acceptance is achieved.