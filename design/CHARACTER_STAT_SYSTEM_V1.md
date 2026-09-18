# PROJECT DARK — CHARACTER STAT SYSTEM v1

Status: CANONICAL SSOT
Date: 2026-09-18
Scope: Commoner Lv1 → five basic jobs → Lv99 → job-change/pure-job → Lv99 → first advancement.

Evidence: [O] official Nexon; [V] Nexon-hosted historical/community evidence; [B] PROJECT DARK balance fallback; PENDING = unknown formula. Original evidence wins. Equipment modifiers never permanently mutate base stats.

## 1. Primary stats / Lv1–99
Primary stats: STR / INT / WIS / CON / DEX.

- Basic-job Lv1 baseline = 3/3/3/3/3. Official 2021 reset restores all five to 3 and returns level-up points [O]; historical Lv1 tables corroborate [V].
- Commoner pre-job uses 3 each as [ADAPTED] until stronger evidence appears.
- Lv1→99: 2 points per level, 98 level-ups = 196 points [V]. One all-in stat can reach 199 (=3+196).
- Level-up MUST NOT automatically increase any primary stat.
- Previous Growth v2 +5 points/level is RETIRED.

STR: physical/basic attack and several Warrior/Rogue/Martial Artist techniques [O/V]; historical carry weight +1/STR [V]. Universal damage coefficient PENDING.
INT: Mage magic strength [O/V]; historical magic accuracy relation [V], formula PENDING. INT does not directly grant MaxMP.
WIS: Cleric heal strength, Lv1–99 MP level-up growth, MP recovery [O]. Exact growth formula PENDING.
CON: Warrior/Martial Artist technique relevance, Lv1–99 HP level-up growth, HP recovery [O/V]. Exact growth formula PENDING.
DEX: Rogue technique damage and critical behavior [O/V]; historical evasion relation [V]. Exact critical/evasion formula PENDING.

## 2. HP / MP
MaxHP and MaxMP are durable accumulated progression resources, not pure functions of current stats.

On each Lv1–99 level-up:
MaxHP += hpGrowth(level, job, CON_at_levelup)
MaxMP += mpGrowth(level, job, WIS_at_levelup)

Exact functions are PENDING. Historical evidence shows CON/WIS allocation timing matters [V]. Later stat changes therefore do not retroactively recompute prior HP/MP. Equipment HP/MP is a temporary additive modifier.

Previous hard-coded formulas level*12+CON*8 and level*6+INT*3+WIS*5 are RETIRED [B]. Until original formulas are recovered, use a replaceable data-driven [B] growth table preserving timing semantics.

## 3. Physical defense — AC
- AC is defense; LOWER is stronger [O].
- Equipment commonly grants negative AC [O/V].
- AC Ignore is a distinct offensive detailed stat [O].

[V-CANDIDATE] Nexon-hosted player testing:
- effective AC >= -98: received physical multiplier ≈ (100 + AC)/100.
- below -98: -98 gives ≈2%; each additional -1 AC reduces that remainder by ~1% relatively (-99≈1.98%, -100≈1.96%).
- positive AC may amplify received damage.
Keep this behind a replaceable defense formula policy until reproduced by tests. Exact AC Ignore ordering/caps are PENDING.

## 4. Magic Defense
Magic Defense Rate is separate from AC [O]. Official material says higher is stronger but does not publish the formula.
[V-CANDIDATE] 2025 Nexon-hosted research describes item Magic Defense primarily as a probability to resist applicable magic, with item-based effective cap around 70%, subject to debuffs/modern systems.
Do NOT model Magic Defense as AC or silently as percentage magic-damage reduction. Exact applicability, cap and roll rules remain PENDING.

## 5. HIT / evasion / critical
HIT independently raises basic-attack hit rate [O], and equipment can grant HIT [O]. Exact HIT-vs-evasion formula PENDING.
DEX is historically related to evasion and critical behavior [V], but exact formulas are unknown. Do not invent per-DEX percentages or a critical multiplier. Keep derived Evasion/Critical hidden until accepted evidence/formulas exist.

## 6. DAM
DAM independently increases basic attack and some skill damage [O]. Equipment grants DAM separately from STR [O]. DAM is not STR and not weapon base damage. Exact per-point effect/order is PENDING.

## 7. Elements
IDs: NONE, WATER(水), EARTH(土), WIND(風), FIRE(火), METAL(金), WOOD(木), HOLY/SAINT(聖), DARK(暗) [O current labels].

Natural cycle [V]: WATER < EARTH < WIND < FIRE < WATER.
Thus FIRE defense is weakest to WATER attack; WATER to EARTH; EARTH to WIND; WIND to FIRE.
Historical equipment semantics [V]: necklace = attack element; belt = defense element.

[V-CANDIDATE] historical natural matrix: weakness ≈100% reference, next relation ≈60%, next ≈38%, same-element ('반속') ≈33%.
NONE/HOLY/DARK/WOOD/METAL exact matrix is PENDING and era-sensitive. WOOD/METAL are enhanced-family relations in player evidence, but coefficients remain PENDING. Do not import later live-service balance changes silently.

## 8. Final character model
PlayerProgression:
level/exp/job, base STR/INT/WIS/CON/DEX, unspent points, durableBaseMaxHP/MP.

EquipmentModifiers:
STR/INT/WIS/CON/DEX, HP/MP, AC, MagicDefense, HIT, DAM, AttackElement, DefenseElement, DamageHealIncreasePct, DamageReductionPct, AdditionalDamage, FlatMitigation, ACIgnore, HP/MPRecoveryIncreasePct, MoveSpeed, special effects.

FinalStats is a read-only projection. Equip/unequip must be reversible and must never mutate permanent progression.

## 9. Defense pipeline contract
Physical candidate:
action/weapon/skill base → element → hit/evasion → DAM/damage modifiers → effective AC → percent damage reduction → flat mitigation → HP.

Magic candidate:
spell formula → magic hit/resist applicability → element → damage/heal modifiers → Magic Defense/Resist → percent reduction where applicable → flat mitigation → HP.

Where original ordering is not evidenced, implementation must be tagged [B] and replaceable.

## 10. Stat screen v1
Primary: STR, INT, WIS, CON, DEX shown as base (+equipment), POINT.
Resources: HP current/finalMax, MP current/finalMax.
Detailed: AC, Magic Defense, HIT, DAM, Attack Element, Defense Element, Damage/Heal Increase, Damage Reduction, Additional Damage, Flat Mitigation, AC Ignore, HP Recovery Increase, MP Recovery Increase, Move Speed.
Critical/Evasion/Magic Accuracy stay hidden while PENDING.

## 11. Mandatory migration gates
1. Change Growth v2 level points +5 → +2 for Lv1–99.
2. Base basic-job stats 3 each; never auto-increase INT or another primary.
3. Replace recomputed HP/MP with durable level-up accumulation and a replaceable [B] growth table until exact formulas are recovered.
4. Separate AC/MagicDefense/HIT/DAM/elements in FinalStats/equipment.
5. Add regression: 98 level-ups=196; all-in=199; no automatic primary mutation; CON/WIS timing affects future growth only; equip/unequip round-trip; AC monotonic mitigation; element matrix.
6. Existing runtime behavior conflicting with this document is NON-CANON and must migrate before further RPG expansion.

## 12. Evidence anchors
Official Nexon: character/detailed-stat guide (guide 82294); 2021 update 4962 (reset to all 3); official item/update records showing independent AC/HIT/DAM/HP/MP/Magic Defense.
Nexon-hosted historical/community: 2019 pure-Rogue guide (2 points/level, 196 total, 199 all-in, CON/WIS timing); 2007 stat guide; 2025 AC and Magic Defense research; historical element guides.
Community numeric formulas remain [V-CANDIDATE], never [O].
