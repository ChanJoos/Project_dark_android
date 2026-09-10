# Character / Animation Handoff — CURRENT

Updated: 2026-09-10 23:34 KST
Branch: `agent/character/20260910-1942`

## Authority / supersession

This handoff intentionally collapses obsolete V2/V3/V4/V5 draft history into one current implementation line. Current authority order is: latest explicit user direction -> latest playtest canon where not superseded -> DESIGN_CONSTITUTION / DATA_CONTRACT / SOURCE_OF_TRUTH -> older history.

Latest explicit Character canon: logical sprite **24x32**, runtime scale **1.50**, exact diagonal facing **NW / NE / SW / SE**. Martial artist is weaponless: no sword, axe, staff or other weapon drawing. Shield is allowed.

## R1 accepted implementation baseline — IDLE / WALK

- `app/src/main/res/drawable-nodpi/player_martial_idle_walk.png`
- atlas size **120x128**, frame **24x32**, columns `0=IDLE`, `1..4=WALK`
- physical rows are explicit and non-mirrored: `0=NW`, `1=NE`, `2=SW`, `3=SE`
- all four rows are independently authored; runtime does not infer facing by mirroring
- source-shaped martial-artist silhouette: white hair/head wrap, red forehead accent, bare upper body, dark navy pants/shoes, attached shield
- head/body target `0.28`, scale `1.50`, logical foot anchor `0`
- nearest-neighbor rendering: anti-alias, dither and bitmap filtering disabled

The reciprocal `atlasRow(Direction)` / `visualFacingForRow(row)` contract specifically protects the device-reported wrong-SE and broken-left presentation defects.

## R2 visible delta — weaponless directional ATTACK

Commit `1d7a695f1fa6f78ce5ea455a61f64a459db30834` adds and binds `app/src/main/res/drawable-nodpi/player_martial_attack.png`.

- atlas size **96x128** = 4 ATTACK frames x 4 directional rows
- frame size remains **24x32**
- attack rows use the exact same mapping: `NW / NE / SW / SE`
- ATTACK advances by `stateClock/stateDuration` across four complete body frames
- attack is bare-hand martial-art motion; there is no sword/axe/staff render path
- shield remains attached to the off-hand silhouette
- body and feet keep the same draw destination and logical foot anchor used by IDLE/WALK, preventing foot-anchor jumps between states
- missing/corrupt/mismatched attack atlas falls back safely instead of throwing at startup

## Crash-proof resource gate

- no giant inline/base64 atlas
- Android drawable resources only
- `player_martial_idle_walk`: expected `120x128`
- `player_martial_attack`: expected `96x128`
- resource decode uses `inScaled=false`
- missing resource, decode failure, shape mismatch or invalid direction never throws from `CharacterRenderer()`
- fallback remains weaponless and preserves `24x32`, `1.50`, four-direction facing and foot anchor

## Static audit correction in this run

Commit `91a70507bdee53f9d81dba0f075428c0ada0559c` updates `CharacterRendererAudit` to the active R2 contract. The prior audit still referenced R1 and removed constants (`ATLAS_COLUMNS`, `ATLAS_WIDTH`), which could break compile/static verification even though the renderer itself had already advanced to R2.

The corrected audit now gates:
- profile `MARTIAL_ARTIST_SOURCE_SHAPED_20260910_R2`
- frame `24x32`
- IDLE/WALK atlas `120x128`
- ATTACK atlas `96x128`
- scale `1.50`
- `MARTIAL_ARTIST_WEAPONLESS=true`
- exact reciprocal rows `NW=0 / NE=1 / SW=2 / SE=3`
- logical foot anchor `0`

## Acceptance status

Static implementation now contains explicit 4-direction IDLE/WALK plus explicit 4-direction unarmed ATTACK under one anchor contract. This is not yet claimed as device-runtime verified; Director-owned APK/device testing must still prove the actual resource path and visual facing on target hardware.

Do **not** start SKILL/HIT/DIE until the IDLE/WALK/ATTACK line is accepted on device. If accepted, next Character visual delta is SKILL under the same 24x32, 1.50, weaponless, four-direction, crash-safe resource contract. No new draft PR should be opened; continue this branch/work line until Director integration.
