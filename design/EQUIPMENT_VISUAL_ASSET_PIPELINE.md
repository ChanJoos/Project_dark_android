# Equipment visual asset evidence pipeline

## Rule
Item identity, stats, inventory icon, equipped sprite/layer, and world/drop sprite are independent evidence dimensions. Never label a generated/redrawn asset as original.

## Asset types
- ICON: inventory/shop/tooltip icon.
- EQUIPPED: character-worn sprite or compositing layer.
- WORLD: ground/drop representation.
- REFERENCE: screenshot/table image that proves identity/appearance but is not production-ready.

## Mapping
All assets map through canonical Item_ID. Multiple source/era/variant assets may coexist for one Item_ID. Store source page URL separately from direct asset URL and preserve SHA-256 after bytes are actually acquired.

## Evidence
O official Nexon; V Nexon-hosted historical/community; FAN external fan source; B project fallback. Evidence is per asset, not inherited from item stats.

## Acceptance
An asset is MAPPED only when item identity is unambiguous. It is PRODUCTION_READY only when dimensions/transparency/crop and era are suitable for runtime. Screenshots remain REFERENCE until clean extraction is verified.

## Collection order
1. Existing canonical equipment, especially Lv1-99.
2. Milddok item dictionary and dress-up resources.
3. Official Nexon guides/update tables containing item images.
4. Nexon-hosted historical/community pages.
5. Other fan archives.
6. User-provided captures when web extraction is impossible.

## Copyright/provenance
Repository ingestion must retain provenance. Do not bulk-copy third-party art into runtime merely because it is publicly reachable; distinguish research/reference snapshots from assets approved for production use.
