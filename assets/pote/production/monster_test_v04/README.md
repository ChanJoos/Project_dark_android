# Pote forest monster pose test pack v0.4

## Confirmed source facts

- The source pack is `assets/pote/review/monster_concepts_v0.4/` from main at the implementation base `fdc9f5e`.
- Its README and `manifest.csv` list 14 concept species/color forms × 12 transparent PNGs: idle/walk/attack × NW/NE/SW/SE (168 files, 384 × 384 px each).
- The source README explicitly says the walk/attack images are representative single poses, not complete looping animation frames, and are not original extracted sprites.
- The Master-backed runtime roster currently contains 16 Pote identities (`PoteMonsterRoster`, projected from `master/data/Monster_Master.csv`).

## Runtime test use (B/ADAPTED)

The 168 PNGs are copied without image edits from the review pack to `sprites/` and packaged so each roster test actor can present the selected direction and state. The existing forest runtime places all 16 roster identities at distinct clear navigation tile centers spread across the adapted field. Their position, count, 40 HP test baseline, concept-to-identity mapping, and deployment are test fixtures; they do not change Master facts, canonical combat stats, or canonical spawn rules.

The three `POTE_STRONG_*` identities reuse their base concept form because the source pack contains no separate strong-variant art. The one canonical `POTE_SPIRIT` test actor runs under isolated runtime ID `POTE_SPIRIT_TEST_B`, using the brown spirit concept. This keeps its confirmed canonical reward from being granted by a 40-HP visual test fixture. Neither mapping claims the art is canonical. The black spirit concept remains in the test pack but has no separate Monster_Master identity to place.

On the field, tap a visible monster to select it; move within reach and use the existing attack controls to see its directional walk and attack pose. Move around it to check all four facings. The walk and attack images change with action state; because each is one source pose, they do not cycle through a multi-frame loop. Device interaction and visual acceptance remain pending until verified on the exact built APK.

## Pose path

`monster_test_v04/sprites/<species>/<state>_<direction>.png`

The code mapping from the 16 runtime identities to the 14 supplied concept forms is in `PoteForestMonsterShowcase.java`.
