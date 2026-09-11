# PROJECT DARK — Item Database Evidence Source

Status: RESEARCH SOURCE / `[FAN]`
Registered: 2026-09-11

## Source

- Site: `https://milddok.cc/items/`
- Parent site: `https://milddok.cc/`
- Source type: unofficial Legend of Darkness fan utility/database.
- Site search/index description observed 2026-09-11: item dictionary containing **9,484 items**, searchable by name and filterable by equipment part, job and level, with item stats available.
- The user identified this source specifically as the comprehensive item-information reference for PROJECT DARK.

## Evidence policy

This source is `[FAN]`, not official Nexon evidence. Item names/descriptions/stats/requirements should be preserved as source evidence but must not be silently promoted to `[O]` official canon. Where a value conflicts with official/original evidence or a user-confirmed PROJECT DARK rule, stronger evidence wins. Record era/version when the database exposes enough context; current/live-service values must not silently overwrite historical values.

The item page is client-driven and was not fully retrievable through the current crawler at registration time. Therefore this commit registers the database/source contract and known index metadata; it does **not** pretend that all 9,484 item records have already been extracted into the repository.

## Canonical ingestion target

PROJECT DARK should ingest the item dictionary into structured data rather than hand-copying descriptions into Java. Recommended record:

```text
ItemEvidenceRecord {
  sourceId,
  sourceUrl,
  sourceEvidence = FAN,
  sourceObservedAt,
  sourceItemKey,
  name,
  description,
  itemType,
  equipmentSlot,
  allowedJobs[],
  requiredLevel,
  requiredStats,
  genderRestriction,
  weight,
  durability,
  price,
  attack,
  defense,
  hit,
  damage,
  ac,
  mr,
  hpModifier,
  mpModifier,
  strModifier,
  intModifier,
  wisModifier,
  conModifier,
  dexModifier,
  elementAttack,
  elementDefense,
  specialEffects[],
  acquisitionNotes,
  era,
  rawSourceFields,
  evidenceNotes
}
```

Fields absent from the source remain `null/PENDING`; never infer missing values.

## Game-data mapping

Evidence records and runtime definitions must be separate:

`milddok item evidence -> evidence normalization -> canonical ItemDefinition -> inventory/equipment/shop/drop/quest runtime`

This allows:
- source updates without rewriting game code;
- historical/current values to coexist;
- official evidence to override fan values field-by-field;
- item art to be added independently from textual/stat evidence;
- automated completeness and conflict audits.

## Visual asset rule

The user notes that this database does not provide the required item images. Therefore:
- textual/stat evidence from milddok does **not** satisfy item-art acceptance;
- `iconAssetId`, equipped sprite/layer and world/drop visuals remain separate evidence/assets;
- never generate an arbitrary icon and label it original;
- item visuals should later be matched against official/original screenshots, archives, fan dress-up resources or user-supplied evidence and retain their own evidence status.

## Equipment/rendering implications

The database is especially valuable for constructing the equipment catalog before art is complete. Equipment records should map to explicit slots and restrictions so CharacterRenderer can eventually resolve layered appearance independently from item stats.

For the current martial-artist player canon, weapon/equipment compatibility must follow user-confirmed and stronger evidence; database records are supporting evidence, not permission to violate the current character rule.

## Scope firewall

Registering ~9,484 items does not mean implementing 9,484 item assets now. Current P0 remains:
1. visible correct 24x32 martial-artist character;
2. NW/NE/SW/SE movement at 0.60 sec per adjacent 64x32 tile;
3. production-quality sprite-based Milles opening screen;
4. stable APK.

Item-database ingestion belongs to the data/content pipeline and may proceed without delaying visual P0. Runtime item/equipment expansion starts after the first playable visual slice is accepted.

## Next extraction task

When direct access/export/API/client data becomes available, perform a bulk snapshot and create:
- raw source snapshot (machine-readable),
- normalized item evidence table,
- duplicate/name collision report,
- slot/job/level taxonomy report,
- missing-field report,
- conflict report against existing PROJECT DARK Master item data,
- image-missing manifest keyed by canonical item ID.

Until that extraction is actually completed, report the source as **REGISTERED**, not **FULLY INGESTED**.
