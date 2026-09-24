# PROJECT DARK mobile HUD assets

Baseline: `9e9ef5d41261586e3dbabd38d2c3b282e7c23dcc`
Canvas: landscape `960 × 540` virtual coordinates, aspect-fit to the device viewport.

Each raster component is a separate `drawable-nodpi` PNG so it can be replaced without recropping an atlas.

| Resource | Purpose | Runtime use |
| --- | --- | --- |
| `hud_attack_button.png` | Large basic attack control | Active when a target is selected and attack is ready |
| `hud_auto_button.png` | Toggle control for automatic basic attacks | Uses only the selected target and existing basic-attack resolver |
| `hud_empty_slot.png` | Empty square skill/quick slot | Ten reserved slots; eight Skills and two Potions remain visibly grouped and unbound |
| `hud_quest_icon.png` | Quest navigation | Routes to the currently tracked quest target |
| `hud_inventory_icon.png` | Inventory navigation | Opens the existing inventory panel |
| `hud_status_icon.png` | Character status navigation | Opens the existing status panel |
| `hud_equipment_icon.png` | Equipment navigation | Opens the existing equipment panel |
| `hud_portrait_frame.png` | Legacy player status portrait frame | Kept as an unused asset; no portrait is shown in the current status layout |
| `hud_hp_bar.png`, `hud_mp_bar.png` | Legacy HP/MP bar frames | Current gauges are drawn from runtime values with shaded fills and inset metal frames |
| `hud_exp_bar.png` | Legacy experience bar frame | Current single EXP gauge is drawn under the top-left status bars |

Potion slots remain empty and visibly grouped in the last two quick-slot cells. Skill and potion artwork, effects, and item bindings are intentionally omitted until their gameplay definitions are confirmed. Buff slots are not displayed pending the user's later layout direction. The top-left status card shows the level column, beveled runtime HP/MP gauges and one EXP gauge; the original ability-level bar is omitted for mobile. The tracked quest card presents the live objective and progress and can be folded; tapping its expanded body keeps the existing quick-navigation behavior. The utility rail and minimap are aligned to the right-side HUD zone. The quick-slot deck sits left of the basic attack button, which is right-anchored within the safe canvas margin. Chat/joystick contrast and action-deck grouping are strengthened while gameplay bindings remain unchanged.

All image assets were cropped from the approved dark bronze HUD concept sheet into transparent PNGs; UI layout and labels remain code-drawn at runtime. Source image concepts are visual references only and are not shipped as a combined background.
