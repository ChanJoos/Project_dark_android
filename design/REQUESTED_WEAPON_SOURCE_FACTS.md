# Requested Mine episode weapons

The user requested 신월도 + 기사단방패 for the rogue and 로오의은총 (or 이아의은총) for the mage. Runtime recovery, not the pre-recovery V210 catalog, determines wearability. The pinned V212 contract already includes `mw103` and `mw078`, and its generated installer binds both. Knight shield `ms008` remains unchanged.

The one production correction is 신월도 stats. The preserved user master row 5500 has display name 곡단도2 and unique name 신월도. Its level is 99, small attack 165–170, large attack 16–17, HP and other mapped modifiers zero. The earlier display-name-only importer left the named 신월도 identity PENDING with empty stats. `RequestedWeaponSourceFacts` admits this explicit unique-name match as U evidence without renaming or duplicating the existing item ID. Existing wearable recovery and equip rules remain in place. No unsupported job restriction is added.

Changed behavior: 신월도 now contributes the user's source attack values instead of empty modifiers. Affected path: constructor catalog installation → existing wearable recovery → equipment stats and appearance. Scoped verification: `RequestedWeaponLoadoutTest` checks exact stats and preserves 로오의은총 HP −500, actual runtime equip, and visible four-direction registered source pixels for both weapons and the knight shield. Existing adjacent equipment test may run at integration. No need to resurrect retired broad verification groups.

Status: IMPLEMENTED locally; BUILD/RUNTIME/VISUAL acceptance pending combined candidate. Do not infer handset appearance from catalog metadata or source assertions.
