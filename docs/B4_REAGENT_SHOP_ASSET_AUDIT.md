# Milles Reagent Shop — Asset-First Implementation Audit

Base: 4ffd0ec47e36a828f405e00e75112d187a883223
Status: PRE-INTEGRATION GATE

## Confirmed repo assets/data
- Exterior potion-shop building: assets/milles/production/buildings/BLD_002_potion_shop.png
- Merlin canonical NPC: NPC_MERLIN_MILLES in master/data/NPC_Master.csv
- Milles reagent-shop service: SHOP_MILLES_POTION in master/data/Town_Services.csv
- Player BODY: master/assets/items/body/mm001_남자.webp
- Wizard robe family exists, including master/assets/items/armor/mu0000118_밀레스위저드로브.webp
- Wizard-like headgear candidates exist in master/assets/items/hair (e.g. magic-family hats/hoods). Candidate must be visually reviewed before Merlin mapping.

## Consumable asset finding
The current master/assets/items tree contains wearable/equipment families only (armor/back/body/cape/faceskin/hair/shield/shoes/weapon).
No canonical consumable-icon asset family for 코마디움/디베노뭄/쿠라눔/엑스쿠라눔/마을리콜 is present under master/assets/items.
Text DB references are NOT visual assets.

## External evidence
Nexon 2009 remote-shop documentation explicitly lists reagent families:
쿠룸, 코마디움, 디베노뭄, 엑스쿠라눔 and town-recall items.
Older Nexon records independently discuss 코마디움/디베노뭄/엑스쿠라눔 and recall as inventory items.

## Implementation gates
1. NO Canvas-drawn fake room.
2. Interior must be a World-owned tile/object map, using the same movement/grid/collision conventions as Milles.
3. Reuse repo terrain/object art where visually compatible; otherwise create/import reviewed pixel assets before integration.
4. Merlin must reuse the character BODY/paper-doll rendering pipeline. No geometric placeholder NPC.
5. Merlin clothing/headgear must be selected from reviewed existing wizard assets or newly authored assets.
6. Shop product rows must render actual item icons. No colored ovals or placeholder bottle geometry.
7. Shop UI must derive from the current inventory presentation language: slots, borders, item icon scale, typography and interaction affordances.
8. Direct tap Merlin -> shop. No proximity gate, dialogue or keyword input.
9. Exit portal -> Milles exterior.
10. Render/device screenshot review is mandatory before APK acceptance.

## Required shop catalog for first slice
- 코마디움
- 디베노뭄
- 쿠라눔
- 리콜 (town-recall presentation; exact variant to be bound from canonical data)

Historical spelling/naming must be normalized only after identity evidence is recorded.


## 2026-09-24 portal reference lock
- User-supplied original screenshots establish the portal as a small pink concentric double-ring floor effect on one isometric tile.
- Runtime asset: `master/assets/world/portal/portal_reagent_shop.webp` (64x32 transparent WEBP).
- Exterior and interior exit must render this same asset, and the visible center must equal the transition trigger center.
