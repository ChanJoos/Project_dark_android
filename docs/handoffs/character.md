# Character / Animation Handoff — CURRENT

Updated: 2026-09-11 01:01 KST
Branch: `agent/character/20260910-1942`

## Authority / supersession

This handoff intentionally collapses obsolete V2/V3/V4/V5 draft history into one current implementation line. Current authority order is: latest explicit user direction -> latest playtest canon where not superseded -> DESIGN_CONSTITUTION / DATA_CONTRACT / SOURCE_OF_TRUTH -> older history.

Latest explicit Character canon: logical sprite **24x32**, runtime scale **1.50**, exact diagonal facing **NW / NE / SW / SE**. Martial artist is weaponless: no sword, axe, staff or other weapon drawing. Shield is allowed.

## R1 baseline — IDLE / WALK

- `app/src/main/res/drawable-nodpi/player_martial_idle_walk.png`
- atlas size **120x128**, frame **24x32**, columns `0=IDLE`, `1..4=WALK`
- physical rows are explicit and non-mirrored: `0=NW`, `1=NE`, `2=SW`, `3=SE`
- all four rows are independently authored; runtime does not infer facing by mirroring
- source-shaped martial-artist silhouette: white hair/head wrap, red forehead accent, bare upper body, dark navy pants/shoes, attached shield
- head/body target `0.28`, scale `1.50`, logical foot anchor `0`
- nearest-neighbor rendering: anti-alias, dither and bitmap filtering disabled

The reciprocal `atlasRow(Direction)` / `visualFacingForRow(row)` contract specifically protects the device-reported wrong-SE and broken-left presentation defects.

## R2 — weaponless directional ATTACK

- `app/src/main/res/drawable-nodpi/player_martial_attack.png`
- atlas size **96x128** = 4 ATTACK frames x 4 directional rows
- frame size remains **24x32**
- attack rows use the exact same mapping: `NW / NE / SW / SE`
- ATTACK advances by `stateClock/stateDuration` across four complete body frames
- attack is bare-hand martial-art motion; there is no sword/axe/staff render path
- shield remains attached to the off-hand silhouette
- body and feet keep the same draw destination and logical foot anchor used by IDLE/WALK
- missing/corrupt/mismatched attack atlas falls back safely instead of throwing at startup

## R3 visible delta — SE + left-facing reinforcement

Commit `51bfeeeaceaf7d76759cc6f7a596b8ed332cfb34` replaces both active martial-artist atlases in-place. No new renderer architecture or feature was added; this is a direct runtime visual correction on the one active Character line.

Changes:
- **SE row is independently redrawn** so visible face, eye/nose bias, torso diagonal, near arm, near foot and shield placement all bias toward lower-right instead of reading as a generic front/mirrored frame.
- **SW and NW rows are independently redrawn** with left-biased head/torso/near-limb overlap to address the device-reported broken leftward presentation.
- NE remains an independent right/up row and is not reused for SE.
- WALK uses the same row identity as IDLE; only near/far leg overlap changes per frame. The foot anchor remains at the same 24x32 frame bottom for all four frames, preventing visual vertical hopping.
- ATTACK was regenerated from the same body proportions so shoulder -> arm -> fist remains connected and direction is preserved through all four punch frames.
- Martial artist remains **weaponless**. No sword/axe/staff pixels were introduced; only the allowed off-hand shield remains.
- Source style remains `[ADAPTED]`; verified original Nexon frame extraction is still `PENDING_CROP`.

## Crash-proof resource gate

- no giant inline/base64 atlas
- Android drawable resources only
- `player_martial_idle_walk`: expected `120x128`
- `player_martial_attack`: expected `96x128`
- resource decode uses `inScaled=false`
- missing resource, decode failure, shape mismatch or invalid direction never throws from `CharacterRenderer()`
- fallback remains weaponless and preserves `24x32`, `1.50`, four-direction facing and foot anchor

## Static verification

Current `CharacterRendererAudit` still gates the active runtime contract:
- frame `24x32`
- IDLE/WALK atlas `120x128`
- ATTACK atlas `96x128`
- scale `1.50`
- `MARTIAL_ARTIST_WEAPONLESS=true`
- exact reciprocal rows `NW=0 / NE=1 / SW=2 / SE=3`
- logical foot anchor `0`

The replacement PNGs were locally dimension-checked before commit: IDLE/WALK=`120x128 RGBA`, ATTACK=`96x128 RGBA`.

## Acceptance status / next gate

The current line now contains a stronger static visual correction for the exact two device failures: wrong-SE and broken left-facing travel. This still is not claimed as device-runtime accepted until the Director-owned APK is tested.

Do **not** start SKILL/HIT/DIE until the refreshed IDLE/WALK/ATTACK resources are accepted on device. If accepted, continue this same branch with SKILL under the same 24x32, 1.50, weaponless, four-direction, crash-safe resource contract. No new draft PR should be opened.
