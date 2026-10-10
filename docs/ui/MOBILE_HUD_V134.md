# V134 mobile landscape HUD

The user requested the Dokkaebi mobile layout for an upcoming introduction video while retaining skill art. Existing SkillIconCatalog sources/IDs and combat behavior are preserved. Native Canvas vectors supply new frames and utility icons.

| Surface | Logical 960×540 placement | Wide viewport |
| --- | --- | --- |
| Minimap | Circle center68,73 radius51; title above | Left anchor |
| Quest | 720,76–944,139; transparent right gradient | Right offset |
| Utility |742,786,830,874,918 at y28 | Right offset |
| Skills | Ten circles44diameter;5×2; first center672,429 | Right offset |
| HP/MP/EXP | Thin resource bars384–576 at466/482/490 | Center offset |
| HP/MP potions |36diameter centers456/504,y515 | Center offset |
| Chat |14,493–266,536; expansion top346 | Left anchor |
| Movement |Existing112,434 radius58 input | Left anchor |
| Attack/Auto/Mode |926,449 /842,518 /926,507 | Right offset |

Ten skills are actual SkillBook slots, not eight skills plus two potions. Registration uses five columns in the existing skill window; shared Resolver/cooldown/resource/save gates remain authoritative. Legacy eight-slot version1 saves are atomically restored into ten slots with the new real slots empty. Test profile slots are separate and allow ten preview skills. Central potions call the same transactional quick-consumable action and cannot consume when not needed.

The minimap projects actual authored static obstacle footprints into a single96×96 cached bitmap per map, and draws live NPC/player markers and nearby monster markers. It is an occupancy overview; it does not claim a complete painted world map. No navigation or collision caches are modified. Empty old HUD coordinates return to the world. Chat expansion alone consumes its actual expanded bounds.

References and retrieval hashes: master/source/ui/mobile_hud_v134/sources.json. Four official HUD guide originals, six Android store originals and user mobile screenshots are archived separately from the APK. Store artwork includes PC labels; mobile geometry comes from the user mobile references. The official YouTube page timed out; video extraction is not claimed. Native review fixtures suppress transient setup notices; delivered runtime notices still expire normally. Physical phone and user visual acceptance remain pending independently of native runtime/build verification.

Verification closure: native96 tests PASS, focused Actions38051917485, full push38051917537 and PR38051920451 SUCCESS. APK source f8eec3fd6c831dae33f699c6f27315d41473156e; PR189 merge abe94195002802c3a3e7bb5f58ccc73dc91c3ba4 has the exact tested tree. APK com.projectdark.mobile.v134test/134/1.34-mobile-hud,2026-10-10 21:30:49 KST, SHA256 b55ceceb7b10fb37f0d56edfbc44616ab9908ca2f6089a68c649cb4dedc995a8. Evidence and separately saved reusable SVG/PNG exports: master/changes/V134_BUILD_RUNTIME_RECEIPT.json. Phone/user visual acceptance remain PENDING.
