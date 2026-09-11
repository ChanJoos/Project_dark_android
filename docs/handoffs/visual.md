# Visual Designer handoff

## 2026-09-11 — STAGE 1 modular BODY_BASE / 4-direction IDLE

Base main: `018eac36cfdef1cd95187164bdc1d4033fd926e8`
Evidence: `USER_PROVIDED_REFERENCE / [ADAPTED]`.

Completed stage:
- STAGE 0 reference lock is active: the user's three martial-artist reference boards are the character proportion/direction/style reference; prior brown-hair/gray-gi baked sprite is superseded and must not be consumed.
- STAGE 1 BODY_BASE IDLE foundation delivered as a modular body-only set with no hair, head gear, equipment, weapon or shield baked into the body.

Assets:
- `design/visual/modular_character/body/player_body_base_idle_4dir_24x32.png` — 4-direction IDLE atlas, direction order `SW / SE / NW / NE`, each frame 24×32 RGBA.
- `design/visual/modular_character/body/player_body_base_idle_sw_24x32.png`
- `design/visual/modular_character/body/player_body_base_idle_se_24x32.png`
- `design/visual/modular_character/body/player_body_base_idle_nw_24x32.png`
- `design/visual/modular_character/body/player_body_base_idle_ne_24x32.png`
- `design/visual/modular_character/previews/player_body_base_idle_4dir_preview_8x.png` — nearest-neighbor inspection preview.
- `design/visual/modular_character/player_body_base_idle_4dir_manifest.json` — hashes and body-layer contract.

Contract:
- frame box `24×32`, runtime scale `1.50`, nearest-neighbor.
- foot anchor `[12,30]` at the ground-contact row.
- BODY includes only skin/body plus a neutral modesty underlayer; `HAIR / HEAD / TOP / BOTTOM / GLOVES / SHOES / WEAPON_MAIN / OFFHAND` are explicitly not baked.
- Directions remain independent: `SE=우하`, `SW=좌하`, `NE=우상`, `NW=좌상`; do not substitute unsafe mirroring.

Next visual task: **STAGE 1 BODY_BASE WALK** — build four-direction WALK frames with the exact same frame box, anchor and body-volume contract. Do not start hair/equipment before the BODY walk poses are stable.
