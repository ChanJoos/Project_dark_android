# Milddok equipment ingestion contract

Status: FOUNDATION READY / BULK SOURCE EXTRACTION BLOCKED BY CLIENT-DRIVEN SOURCE

## Goal
Use the existing `master/data/Item_Master.csv` as canonical identity. Milddok is an external [FAN] catalogue, never the canonical ID authority.

Pipeline:
1. Snapshot Milddok source records.
2. Filter equipment records.
3. Normalize name/slot/job/level without discarding raw fields.
4. Match to Item_Master by exact name first, then normalized name + slot/job/level.
5. Preserve existing Item_ID on matches.
6. Mint a new canonical Item_ID only for genuinely missing equipment.
7. Store source key mapping in Item_Source_Map.csv.
8. Store every sourced field in Item_Evidence.csv.
9. Promote resolved stats into Equipment_Stats.csv field-by-field according to evidence priority.

## Null rule
Blank means unknown/PENDING. Zero means the source explicitly establishes zero. Never convert missing values to zero.

## Evidence priority
O > V > FAN > B. Conflicts are preserved in Item_Evidence and must not be overwritten silently.

## Runtime activation gate
An equipment record may exist in Item_Master with incomplete stats. Runtime activation requires enough resolved fields for its intended gameplay role and no unresolved high-priority conflict.

## Current source state
Milddok publicly advertises 9,484 items and filtering by equipment part/job/level with visible stats, but its item dictionary is client-driven and the current crawler does not expose the bulk payload/API. Therefore the repository now has the canonical ingestion schema and mapping contract, but must not claim the 9,484 records are ingested until an actual snapshot is obtained.

## Next extraction acceptance
A valid bulk extraction must produce a machine-readable raw snapshot containing stable source keys where available, names, category/slot, restrictions/level and all exposed stat fields. It must then emit counts for matched existing, new equipment, ambiguous, and rejected/non-equipment records.
