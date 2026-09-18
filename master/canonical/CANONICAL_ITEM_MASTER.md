# PROJECT DARK Canonical Item Master Contract

Status: ACTIVE CANONICAL CONTRACT
Updated: 2026-09-18

## Canonical rule

`master/data/Item_Master.csv` is a **historical imported source sheet** containing 222 data rows. It is not the current equipment catalog size and MUST NOT be reported or consumed as the complete PROJECT DARK item/equipment master.

The active canonical item master is generated at:

`master/canonical/generated/Item_Master_Merged.csv`

Generation pipeline:

1. preserve historical imported `data/Item_Master.csv`;
2. add researched identities from `Equipment_Catalog_Additions.csv`;
3. recover named equipment identities from the existing 3,692-file item asset inventory into `Equipment_Asset_Recovered_Catalog.csv`;
4. merge all non-colliding identities;
5. attach image mappings/evidence and equipment-stat evidence independently.

## Reporting rule

Never state “the Master has 222 equipment/items” without explicitly calling 222 the **legacy/historical imported Item_Master row count**. Current catalog counts MUST come from the generated canonical master at the exact branch/head being discussed.

## Asset rule

The 3,692 existing item visual files are an asset inventory, not 3,692 unique equipment identities. Named equipment identities are promoted into the canonical catalog; UNNAMED and cosmetic/variant assets remain recoverable evidence until identity is resolved.

## Stat rule

Image identity does not imply verified stats. Missing stats remain PENDING and are enriched from official Nexon, Nexon-hosted historical evidence, registered fan databases, and other provenance-preserving sources. No fabricated numeric values.
