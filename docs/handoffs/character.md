# Character / Animation Handoff — CURRENT

Updated: 2026-09-11 01:08 KST
Branch: `agent/character/20260910-1942`

## Authority / supersession

Latest explicit user direction is authoritative. Obsolete V2/V3/V4/V5/R3 redraw history is not a visual source. Current Character canon is: source sprite 24x32, runtime scale 1.50, exact diagonal facing NW / NE / SW / SE, martial artist weaponless except shield, and user-provided martial-artist sprite imagery used directly rather than re-authored interpretation.

## R4 visible delta — direct source-cropped IDLE / WALK

Commit `223565b98bda7e561dfea9dee2f7a6b0fa993ae7` replaces `app/src/main/res/drawable-nodpi/player_martial_idle_walk.png` using pixels cropped directly from the latest user-provided 4x5 martial-artist sprite sheet instead of the previous hand-redrawn R3 atlas.

- source image rows are treated explicitly as `SW / SE / NW / NE` visual source rows and normalized into the runtime physical row contract `NW=0 / NE=1 / SW=2 / SE=3` before atlas assembly;
- each source sprite is background-isolated, proportion-preserved, normalized into a 24x32 transparent cell and foot-anchored at the common frame bottom;
- output remains `120x128` = 5 columns x 4 rows;
- no runtime mirroring is used;
- character identity now preserves the supplied white hair/head wrap, red band, facial proportions, bare torso shading, navy pants, red shoe accents and glove/shield silhouette from the supplied source instead of a simplified redraw;
- runtime renderer scale remains exactly 1.50 and nearest-neighbor draw remains unchanged;
- provenance remains `[ADAPTED]` because this is user-supplied/generated/reference imagery, not authenticated original Nexon asset extraction.

## Existing motion line

`player_martial_attack.png` remains the previous unarmed 4-direction action atlas for now. Per the user-ordered acceptance sequence, ATTACK will be replaced from the same direct-source lineage only after the new source-cropped IDLE/WALK direction read is accepted; SKILL/HIT/DIE must not advance ahead of that gate.

## Direction / crash-safe contract

- exact runtime rows: `NW=0 / NE=1 / SW=2 / SE=3`;
- `atlasRow(Direction)` and `visualFacingForRow(row)` remain reciprocal audit points;
- frame 24x32, IDLE/WALK atlas 120x128, scale 1.50, foot anchor 0;
- no giant inline/base64 runtime atlas;
- Android drawable resource loading uses crash-safe fallback; missing/decode-invalid/shape-invalid resources do not throw from `CharacterRenderer()`.

## Acceptance status / next gate

The IDLE/WALK visual asset is now direct-source-derived rather than manually redrawn. Director-owned device test must verify SW/SE/NW/NE visual facing, especially true SE lower-right and non-corrupt left-facing travel. If accepted, next visual delta is direct-source unarmed ATTACK replacement on this same branch. No new draft PR, main merge, or APK packaging from Character agent.
