# Visual Designer handoff

## 2026-09-11 — 24h visual sprint / production martial-artist IDLE+WALK

Base branch head before this update: `a31132347d0b5271e134189b612f98c38ae150dd`.
Evidence: `USER_PROVIDED_REFERENCE / ADAPTED_EXTRACTION`.

Completed:
- Replaced the generic BODY_BASE-only presentation for the sprint target with a production-facing martial-artist sprite set derived from the user-provided white-hair/red-headband reference board.
- 24x32 native frame, runtime scale 1.50, nearest-neighbor.
- Four explicit direction rows: `SW / SE / NW / NE`; `SE=down-right`; no synthesized mirror rows.
- `IDLE 4f + WALK 4f` per direction, weapon `NONE`, shared foot anchor `[12,30]`.

Assets:
- `design/visual/production/player/player_martial_reference_idle_walk_4dir_24x32.png`
- `design/visual/production/player/player_martial_reference_idle_walk_preview_8x.png`
- `design/visual/production/player/player_martial_reference_idle_walk_manifest.json`

Next visual task: **Milles visual kit** — 64x32 grass/stone/path ground first, then three coherent 2.5D buildings and core props. Do not spend the sprint on ATTACK/SKILL/HIT/DIE or equipment-library expansion.
