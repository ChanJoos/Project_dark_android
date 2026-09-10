# PROJECT DARK — Item Visual Manifest

Status: ACTIVE / character presentation source registry

## Goal
Build image coverage for every canonical item in `master/data/Item_Master.csv` before large-scale equipped-character rendering. Item identity, equipability, stats and progression remain RPG-owned; this registry owns only visual evidence and renderer mapping.

## Per-item visual contract
Each canonical Item ID must be traceable through:

`Item_ID -> inventory icon -> equipped sprite (if visible) -> equip slot -> render layer -> direction/gender coverage -> provenance -> crop/readiness status`

Image surfaces are tracked separately because an inventory icon does not prove an equipped-world sprite.

## Status vocabulary
- `PENDING_SOURCE`: no positively identified image source yet.
- `SOURCE_FOUND`: a candidate image source is positively identified but item/crop work remains.
- `PENDING_IDENTIFICATION`: source image exists but the exact item inside it is not yet positively identified.
- `PENDING_CROP`: item is identified but final crop/sprite extraction is not ready.
- `READY_FOR_RENDER`: image/crop and anchor/scale metadata are sufficient for renderer binding.
- `NO_SOURCE_FOUND`: search completed for the current evidence pool without usable visual evidence; do not invent original art.

## Character-visible candidate mapping
Presentation-only mapping; this does not redefine RPG slots.

- `무기` -> `MAIN_HAND`
- `방패` -> `OFF_HAND`
- `장갑`, `각반`, `신발`, `갑옷`, `의복` -> `EQUIPMENT`
- `귀걸이`, `목걸이`, `반지`, `벨트` -> `ACCESSORY` candidate; actual on-character visibility must be proven before drawing.
- unknown/non-equipment slot -> `NONE`

`ItemVisualManifest.from(RpgProgressionState)` enumerates **all runtime canonical item definitions** so new Item_Master rows cannot silently disappear from the visual backlog.

## Official evidence pool seeded this pass
1. Nexon LOD guide index / right-menu UI guide: `https://lod.nexon.com/info/guide/82295`
   - Official `[O]` proof that inventory can filter equipment and that the character-info panel displays equipped items.
   - Contains inventory/character-info screenshots, but individual canonical Item IDs are not yet positively mapped to image pixels.
2. Nexon probability/manufacturing page: `https://lod.nexon.com/cashshop/probability`
   - Official `[O]` text existence evidence for named items including `가죽방패`; this is **not** image evidence.
3. Nexon-hosted community/game-board pages may contain historical item names and screenshots. Hosting provenance alone is not official-art provenance; keep such visual evidence `[V]` until independently verified.

## First collection batches
Work by slot family so visual comparisons can be rendered consistently:

1. Shields: `가죽방패 -> 구리방패 -> 철방패 -> 은제방패 -> 금제방패 -> 플라늄방패 -> 파파야방패`
2. Common gloves/leggings/shoes: leather/copper/silver/aqua/coral/boots families.
3. Weapons by canonical job family.
4. Armor/clothing.
5. Accessories; first determine whether each class is world-visible or inventory-icon-only.

For each family, collect official/Nexon-hosted images first, identify exact items, crop without resampling where possible, record gender/direction coverage, then promote to `READY_FOR_RENDER` one item at a time.

## Evidence discipline
- Do not infer an equipped sprite from an inventory icon.
- Do not infer exact RGB, anchor, scale or directional frames from item names.
- Do not promote user/community screenshots to `[O]`; Nexon hosting can establish source location while visual truth remains `[V]`.
- Missing original art stays unresolved or becomes `[ADAPTED]` only when the project explicitly chooses to create replacement art.
