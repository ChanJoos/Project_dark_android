# Legend of Darkness — Hunting Ground Evidence Map

Source class: `[FAN]`
Source: NamuWiki `어둠의 전설/사냥터`, revision 2026-08-26.

> Evidence policy: this document records source-derived structure and gameplay implications. It is not authoritative truth. The source explicitly describes multiple historical eras and states that Quick Dungeon changed the practical progression meta. Preserve era/context instead of flattening everything into one ruleset.

## 1. Progression topology

### Legacy 1–4 circle route
- 1C: Novice village dungeon, Woodland 1–2, Milles/Roton/Suomi underground dungeon 1–3F.
- 2C: Pote Forest.
- 3C: Abel Coast.
- 4C: Rucesion Coast.
- Other areas such as Piet dungeon, Abel dungeon, underwater dungeon, wrecked ship, ski resort and Mountain Merry are described mainly as quest/farming destinations.

### 5 circle / pre-ascension
- Horror Castle.
- Beqna Tower.
- Countess Mansion.
- Kasmanium Mine 3rd shaft.
- Great Plains dungeon.
- This tier historically has longer residence because players raise HP/MP for ascension.

### Ascended / high-level
- Death Village houses/dungeon.
- Countess Mansion and Countess Mansion Urid.
- Ophion caves: Bronze / Blue / Red / Black.
- Mechanic Dungeon.
- Murekan Counterattack Dungeon.

### Ability progression areas
- Meadow field.
- Endor Tower.
- Hiyat Dungeon.
- Volcano.
- Catacomb.

## 2. Era rule

The source explicitly says this topology mostly describes the pre-Quick-Dungeon era. Since the 2019 Quick Dungeon introduction, legacy fields largely lost their role as optimal EXP progression and many became quest/item-farming/nostalgia locations.

PROJECT DARK design consequence:
- Do not erase legacy world progression merely because later live-service optimization displaced it.
- Treat `LEGACY_WORLD_PROGRESSION` and `LATE_QUICK_DUNGEON_META` as separate eras/rulesets.
- Initial PROJECT DARK should prefer explorable world/dungeon progression because it gives maps, NPC travel and party composition meaningful gameplay.
- Quick Dungeon, if implemented later, is a separate instanced progression feature, not a replacement for canonical world content in data.

## 3. Dungeon design grammar extracted from source

The source demonstrates that hunting grounds are not merely level-gated monster rooms. Required reusable primitives include:
- field vs instanced dungeon
- floor/zone sequence
- entrance NPC and entrance fee
- minimum/maximum party size
- HP/MP/ability entrance gates
- re-entry cooldown
- random maze generation
- room-clear gate
- key-monster gate
- objective objects (e.g. four pawns)
- boss spawn condition
- boss room and reward room
- floor-specific monster pools
- floor-specific loot pools
- high-density spawn rooms
- party-positioning/chokepoint gameplay
- monster dispel/status/target-recognition behavior
- clear reward vs monster drop reward
- dungeon completion qualification/unlock

These should become data, not hard-coded per map.

Suggested future data contract:
`HuntingGroundDefinition { id, era, progressionTier, worldRegion, mapSequence, entryRule, partyRule, respawnRule, monsterPools, objectiveRules, bossRules, lootTables, clearRewards, travelLinks, recommendedRoles }`

## 4. Representative encounter specifications

### Horror Castle
- Major legacy beginner 5-circle hunting ground.
- Entry through an NPC in Death Village waiting room; source records a 100,000 gold fee.
- Historically supports six-person party compositions and positional/chokepoint play.
- Source describes monster behavior changing at player HP/MP thresholds.
- Design value: first strong reference for classic party-role combat and fixed-position formation play.

### Countess Mansion
- Floor-based field dungeon with low/mid/high hunting bands.
- Historical floor-specific Obsidian equipment categories and named high-floor targets.
- Separate ascended/non-ascended paths are described.
- Design value: floor progression + loot stratification + elite/named monster template.

### Beqna Tower
- Random maze generated on entry.
- Rooms include pass-through rooms, kill-all rooms and key rooms.
- Four pawn objectives must be destroyed before boss spawn; boss clear leads to reward room.
- Some monsters use Comadia/DiNarcoli-like support/dispel behavior.
- Design value: excellent reusable dungeon-objective template for PROJECT DARK.

### Countess Mansion Urid
- Instance entered through Pinato NPC.
- Source records ability >=10, party 4–7, and 30-minute re-entry restriction.
- 12 floors; 12F is a boss room with multiple boss-class enemies.
- Clear reward and boss-drop reward are distinct.
- Design value: canonical instance template with party gate, cooldown, floor escalation and final boss.

### Ophion cave family
- Bronze/Blue/Red/Black variants with different progression and farming roles.
- Specific floors became community-defined hunting spots because of spawn density, party size, EXP and materials.
- Design value: one environment family can support several difficulty/loot/ecology variants without pretending they are identical maps.

### Mechanic Dungeon
- A–F areas; fragments are collected to progress and completion grants qualification to equip Mechanic weapons.
- Monster recognition interaction with Hide is specifically important.
- Design value: dungeon completion can unlock equipment eligibility, not merely reward an item.

### Murekan Counterattack Dungeon
- High-end historical EXP dungeon with ability and HP/MP entry gates.
- Design value: multi-dimensional progression gates and high-stat encounter tier.

## 5. Ability-area encounter identity

### Meadow
- Entry ability hunting field; monster gathering and burst clearing strongly shape party roles.

### Endor Tower
- Multiple ability thresholds/areas and monsters with disruptive mechanics: status effects, coma, mana depletion, item interference, etc.
- Party support roles became specialized around healing/dispel/recovery.

### Hiyat Dungeon
- Explicitly designed to favor physical attackers after support-heavy metas.
- Some monsters require magic-defense bypass while others reject magic, creating class-composition pressure.
- Source says full-job parties later received bonus ability EXP.

### Volcano
- Described as solo-friendly; grouping changes monster spell/dispel behavior.
- Strong evidence that party state itself can be an encounter-rule input.

### Catacomb
- Primarily item-farming context in the source and described as incompletely implemented historically.

## 6. System implications for PROJECT DARK

This source materially strengthens the case for these engine systems:
1. `WorldGraph`: towns and dungeons must be connected by actual travel/entrance relationships.
2. `DungeonInstance`: supports field and instance modes.
3. `EntryRuleEvaluator`: level/circle/ability/HP/MP/party/cooldown/cost checks.
4. `EncounterObjective`: kill-all, key kill, object destruction, boss trigger, clear.
5. `SpawnDirector`: floor/room-specific pools and density.
6. `MonsterBehaviorProfile`: recognition, Hide interaction, dispel, status, heal, escape and magic/physical defense behavior.
7. `PartyRole`: tank/gatherer/melee/ranged/debuffer/healer/dispel/burst roles must emerge from mechanics rather than arbitrary labels.
8. `LootTable`: floor and named-monster-specific rewards.
9. `DungeonProgress`: floor state, objectives, qualification unlocks, re-entry cooldown.
10. `EraTag`: historical/live-service rule changes remain distinguishable.

## 7. Initial PROJECT DARK progression recommendation

Do not implement every hunting ground now. Preserve the full topology in design/data, but use a vertical slice:

`Milles -> Milles underground dungeon -> Pote Forest`

This is the most useful first progression spine because it validates:
- town-to-dungeon travel
- portal/entrance logic
- early monster spawning
- combat reward/progression
- return-to-town loop
- map identity across town/dungeon/forest

After that, Beqna Tower is a high-value systems milestone because its random rooms + four-objective + boss/reward-room structure exercises the reusable dungeon framework.

## 8. Current sprint boundary

This evidence does NOT expand the immediate visual/playability P0. Current priority remains:
- visible 24x32 martial artist character
- NW/NE/SW/SE movement
- 0.60 sec per adjacent 64x32 isometric tile
- polished sprite-based Milles opening screen
- device-testable APK

Hunting-ground implementation begins after that slice is visually accepted. The evidence map exists now so subsequent development follows source-derived progression rather than invented generic RPG zones.
