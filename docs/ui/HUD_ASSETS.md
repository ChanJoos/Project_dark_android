# PROJECT DARK mobile HUD assets

Baseline: `9e9ef5d41261586e3dbabd38d2c3b282e7c23dcc`
Canvas: landscape `960 × 540` virtual coordinates, aspect-fit to the device viewport.

Each raster component is a separate `drawable-nodpi` PNG so it can be replaced without recropping an atlas.

| Resource | Purpose | Runtime use |
| --- | --- | --- |
| `hud_attack_button.png` | Large basic attack control | Active when a target is selected and attack is ready |
| `hud_auto_button.png` | Toggle control for automatic basic attacks | Uses only the selected target and existing basic-attack resolver |
| `hud_empty_slot.png` | Empty square skill/quick slot | Ten reserved slots; first eight are marked Skill |
| `hud_quest_icon.png` | Quest navigation | Routes to the currently tracked quest target |
| `hud_inventory_icon.png` | Inventory navigation | Opens the existing inventory panel |
| `hud_status_icon.png` | Character status navigation | Opens the existing status panel |
| `hud_equipment_icon.png` | Equipment navigation | Opens the existing equipment panel |
| `hud_portrait_frame.png` | Player status portrait frame | Frame only; no placeholder character portrait is baked in |
| `hud_hp_bar.png`, `hud_mp_bar.png` | Red/blue resource bar frames | Filled from current runtime HP/MP |
| `hud_exp_bar.png` | Green experience bar frame | Filled from current progression EXP ratio |

Potion slots remain empty and visibly grouped in the last two quick-slot cells. Skill and potion artwork, effects, and item bindings are intentionally omitted until their gameplay definitions are confirmed. Buff cells are also empty frames. The minimap, tracked quest panel, target bar, chat, joystick, attack, and EXP strip remain part of the live HUD. Inventory, status, and equipment panels retain their existing data and touch behavior.

All image assets were cropped from the approved dark bronze HUD concept sheet into transparent PNGs; UI layout and labels remain code-drawn at runtime. Source image concepts are visual references only and are not shipped as a combined background.
