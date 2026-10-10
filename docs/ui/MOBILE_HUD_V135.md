# V135 HUD correction

Latest user direction supersedes V134's simplified status bars, line glyphs and single transparent quest card. Reference layout is an adaptation; no claim of third-party artwork identity or video extraction.

- Exact V133 LV tile, red HP value/bar, blue MP value/bar and gold experience percentage/bar drawing restored by translation to status bounds355,424–605,536 (+half wide offset).
- HP/MP potions centers456/504,398 (+half wide offset), bounds36×40 entirely above status. Shared transactional quick-use and persisted resources retained.
- Joystick center84,398 radius58, previously112,434. Same movement command/facing/tile path; visible bounds and reset match.
- Top-right utilities keep current centers742/786/830/874/918,28, with filled silver relief bag, stat crest, armor, spellbook, scroll. Dark engraving/specular edges and active gold state. Attack is a silver blade with gold guard and metal bezel; AUTO uses readable stacked AU/TO lettering, two unobtrusive perimeter arcs and active-state color.
- Quest tracker720,76–944,247 (+wide offset): up to three real noncompleted journal rows, current navigable rows first then locked next rows.57px each; notched gold/green title ribbon, diamond marker, objective, numerical progress/status, contextual instruction and right medallion. Current quest routes; other rows open the same journal selecting that exact quest. No fake achievements/rewards or accepting locked quests.
- All ten existing skill pictures/IDs/proficiency/save rules untouched. Terrain/actor/collision/cache untouched.

Comparison: official guide fullHUD/menu/preset originals plus user mobile screenshot, provenance master/source/ui/mobile_hud_v134/sources.json. Explicit title categories and live content mapped to PROJECT DARK domains. Silver emblems are native vector UI adaptations. UI vectors are not replacement world/actor production sprites. Native actual town/forest/available-active-report tracker and both aspect ratios must be reviewed before delivery. BUILD VERIFIED, NATIVE RUNTIME VERIFIED, INTEGRATED, phone and USER VISUAL are separate gates.

Closure: exact source f637fad784575ed7dfe4bc6e0cc71493d607ffc1, focused38055833678101 native tests PASS; full push/PR CI SUCCESS; PR190 tested-tree merge f78182099ff26192950562b3fb308e0718e67d58. Delivered APK SHA256 f73ff5b25f754b1c507deeebfbc4f70f4adfeaaea723843f4497de6e1e5f65f1, built2026-10-10T22:35:32.523246+09:00. Phone and user visual acceptance PENDING. Full receipt master/changes/V135_BUILD_RUNTIME_RECEIPT.json.
