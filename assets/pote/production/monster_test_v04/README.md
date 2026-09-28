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

## Renderer scale feedback (2026-09-28)

A user review of a delivered APK reported that the monsters looked much larger than the player, attack poses looked unnatural, the monsters seemed to attack before the player approached, and the Strong Gnoll looked broken/pixelated. The tested APK SHA was not supplied, so do not attribute the screenshot to a specific build.

The test renderer now removes transparent cell margins at runtime and draws each silhouette at 44 logical pixels high with its aspect ratio preserved. It does not add a synthetic lunge or rotation to the supplied attack still. A regression test bounds every roster/state/facing silhouette to 48 logical pixels high. This passed Android CI run #36393396473 at source `8ca5fda1c2483589b6f16fc9800bb9f87597ec23`; exact APK SHA and artifact are in `docs/PROJECT_STATE.md`. These values are adapted test-scene presentation only, not a canonical species scale or combat animation specification. The source images remain unchanged.

The v0.4 source pack contains no separate Strong Gnoll concept; that test identity continues to share the GNOLL concept. The new scale should reduce magnification, but pixel-art clarity and overall device appearance are not accepted until the new exact-SHA APK is reviewed on-device. The chase radius remains the existing prototype value (180); the shared melee route still requires authored adjacent ground tiles. If the new screen still reads as an early attack, record monster/player tile positions and windup/hit timing before proposing a radius change.

## Pose path

`monster_test_v04/sprites/<species>/<state>_<direction>.png`

The code mapping from the 16 runtime identities to the 14 supplied concept forms is in `PoteForestMonsterShowcase.java`.
