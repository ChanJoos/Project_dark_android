# V138 whole catalog execution audit

User rejects the V137 early-magic-only closure and requests every technique and spell. Director is the single editor. Approved V136 HUD, source artwork, movement, damage anchors and save identities remain the governing baseline.

All 221 stable IDs have an individual record in `master/changes/V138_ALL221_SKILL_AUDIT.json`: class, job, route, formula evidence, fixed/all-MP cost, separate reuse/cast/cadence, tile shape, reach, motion, FX source and device status. This is a full catalog audit, not whole-original-game certification.

## Corrections

- All 138 catalog Spell entries share 1/3s admission cadence. Ordinary spells have zero individual reuse instead of the V83 circle-based invented values. Supplied wiki PDF explicitly lists Sleep 1s, Immortal/Reflect 5s; these are adopted source-version exceptions, not official current-server claims. Dara retains its existing 12s ADAPTED exception and 3s release.
- Every active spell now uses the actual accepted, equipped-staff-adjusted cast clock for input duration and release pose. A zero-cast upper-circle/healing spell does not inherit the fixed .65s or old rule-contact pose lock. Cast time is still separate from reuse. The source-defined CHARGE_CAST (Dara) retains its existing .35s raised-arm release/recovery after the effective contact clock; ordinary/instant spells do not inherit this tail.
- Target-cross magic starts on visible live inter-tile targets. Ranged target-cross and screen loadouts remain at range while waiting for spell admission/reuse/resources; they do not fall through to melee. Four-axis finite technique masks remain unchanged.
- Utility entry validates alive/control/MP before charging. Separate-player Push/ArmorDisarm/HideOther and currently unwired leap recipient are unavailable instead of applying an enemy effect to self or displaying success without a recipient.
- All83 catalog techniques retain separate individual cooldowns. Their exact historical seconds remain ADAPTED unless independently sourced; they are not globally zeroed.

## Source boundaries and unresolved work

The user PDF is a supplied wiki snapshot, not official numeric patch canon. Its general technique/spell distinction, specific cooldown exceptions and explicit cast examples are retained in the accepted change. Official HolyShock3/HolyBlow4 already supported by earlier source records are unchanged. Elementary1s remains ADAPTED. Other unknown base cast times retain the existing contact value and remain identified as ADAPTED in every row; no circle-based original cast table is fabricated. The historical Pana -1/heal0/curse1 profile remains unchanged, with the newer -2 guide conflict preserved.

Jangpung has a classification conflict: the supplied wiki calls it magic while the retained 2020 Naver exact-ID source projection calls it technique. Its projection is preserved pending a version-resolved decision; the whole138 Spell count refers to the current source catalog. Muyeong also remains the source-defined technique. Current utility INSPECT/services execute immediately via their existing route; the known Adles5s description is not a claim that a utility cast queue exists. Unavailable original party/coma, travel destination, crafting, durability, summons and unknown effects remain separate gaps.

Routes after rejecting missing recipient services:173 Resolver actions,8 linked/passive,18 utilities,1 equipped basic attack,21 unavailable (20SERVICE plus separate-recipient transfusion). Native fixtures inject learned truth but do not enable skill-test mode or waive resource/conditional checks. Normal GameView actual slot/AUTO tests additionally cover upper-circle ranged use and shared healing cadence. These fixtures do not prove all acquisition UIs, original damage values, effect pixels, multiplayer or physical-device acceptance.

## Validation

Deterministic generation and per-ID joins verify all221 records. `AllSkillRuntimeV138Test` attempts every supported runtime route, rejects unavailable routes without MP mutation, verifies full Spell reuse exceptions, actual moving target-cross slot input, ranged AUTO, cross-spell cadence and utility dead/control/resource gates. Existing source, finite-shape, HUD, equipment, combat/quest/save/forest regressions remain required. Native/full CI, exact source/run/APK and phone states are recorded separately when complete.
