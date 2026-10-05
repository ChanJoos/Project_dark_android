# Pamfet runtime draft

The purple Pamfet's eight PNG frames are integrated from `Pamfet_8_Direction_Draft_v3` as a visual prototype:

- four diagonal idle poses are used for both IDLE and WALK (no walk cycle was authored in this draft)
- four matching diagonal roll poses are used for ATTACK
- all frames are 128×128 RGBA with transparent background
- runtime display is nearest-neighbour scaled and anchored at the monster's logical ground point

These are generated/adapted review art, not original game pixels or canonical art. The runtime currently binds only `POTE_PURPLE`; no Silver recolor or other Pamfet variant is implied. Monster identity/map membership comes from the existing master data. HP, combat tuning, and placement remain the pre-existing explicitly B/adapted prototype values.