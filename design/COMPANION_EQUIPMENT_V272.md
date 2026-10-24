# V272 companion equipment windows

User directive 2026-10-24 KST: create companion equipment windows and apply the companion's profession, level and attributes to equipment and appearance. No level99 equipment-kit grant is authorized or implemented. Existing immediate99/all-own-job-skills test entry remains independent of gear conditions.

## Producer and consumer contract
CompanionEquipment owns staged equip/unequip transactions. CompanionEquipmentWindow owns an independent paper doll, five companion tabs, live stat view, slot selection and an owned spare-equipment picker. GameView connects actual touch commands, existing original-art renderers and the atomic F5mSaveStore checkpoint. CompanionParty.Member.growth remains each actor's profession/level/base stats/equipment authority; FinalStats is shared by the paper doll, AI and Resolver. Player equipment/progression/quests are not edited by companion equipment.

Open companion management -> 동료 장비. Select a profession tab, select a paper-doll slot, choose a spare item and press 동료에게 장착. Select a worn slot and use 해제 to return it. The source stat button shows this companion's live profession/level/STR/INT/WIS/CON/DEX/HP/MP/AC/HIT/DAM/weapon range/magic defense/elements. Inactive companions can be inspected/equipped without recruiting them or changing the active companion. Menus consume world touches; active player/companion action and read-only saves block mutations.

## Eligibility and ownership
- Use the companion's own profession and actual level. Neither player sandbox access nor the player's temporary starter-necklace exception bypasses companion requirements. Known source job/level conditions are enforced. Inventory-only/unknown job conditions remain pending; do not manufacture missing source requirements.
- The current item model exposes source job/level conditions and stat modifiers; it does not contain verified item STR/INT/WIS/CON/DEX minimum-requirement fields. Do not invent such minima. Attributes and equipment bonuses are actually applied through FinalStats and the shared combat formulas.
- Use spare owned stock from the existing shared player bag. A worn player copy cannot be borrowed. Transfer one physical item into the selected companion's allocation; return replaced pieces, two-hand-displaced shields and coverage-displaced garments to the bag. Explicit right/left ring/glove/ornament slots remain distinct. Existing slot, two-hand learning and full-body/upper/lower coverage rules are reused.
- A companion's inventory retains only its actual worn allocation, never its RpgProgressionState constructor's hidden sandbox catalog. This change adds no gear kit, shop purchase or equipment reward.
- Validate the staged owner/slot maps before live mutation. One checkpoint writes player stock plus companion gear/resources. Checkpoint failure restores both owners and resources while preserving live order, position and cooldowns.

## Stats, actions and appearance
All equipment modifiers, max resources and elements feed the companion's own FinalStats and actual Resolver damage/defense. Resync maximum HP/MP while clamping current pools; changing gear neither refills HP/MP nor revives a dead companion. Equipped staff cast/MP effects use the companion's weapon, not the player's. Basic attack semantic and two-hand contact/cycle use its equipment. Existing regeneration policy applies to active companion gear without adding a second player regen route. Source art, skill coefficients and player-only equipment behavior remain unchanged.

Both the field actor and paper doll project the same CharacterVisualBinding, including original head/clothing/weapon/shield/back layers and source body identity for male/female garments. Existing source frame/motion renderers are retained; missing source facts are not replaced with invented art.

## Save migration
Party snapshot3 includes each member's equipment. Item allocations are reconstructed exactly from worn-slot counts, validated against item definitions/slots and known requirements before replacing the live roster. Snapshots1/2 remain readable. The original constructor's precise starter slot/ID pairs are grandfathered on restore because accepted legacy shoes/shield have pending conditions and the legacy hat can predate its current minimum level; this does not permit new equip commands to bypass conditions. Never regrant equipment on rejoin/test toggle/restart. Unknown/corrupt equipment rejects the staged optional party save rather than partially overwriting it.

## Verification scope
Production compile and45 local domain tests PASS. Native: actual management entry, five job tabs,960/2340 touch geometry, stat view, original-art paper-doll/field pixel changes, gear ownership, inactive-member editing and full GameView restart. Adjacent existing companions, shared spatial/timing, inventory/HUD checks retained. Full current32gate regression is required because party save schema and actor-equipment combat projection changed. V268 retired checks stay retired. Final APK must prove exact source/run/hash/signature/asset bytes; handset/user visual acceptance remains separate.
