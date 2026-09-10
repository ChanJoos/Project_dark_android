# World handoff — PLAYABLE VILLAGE M3 2.5D PROPS

- Persistent branch: `agent/world/playable-village`
- Base main: `77132528a29d073080e5966844d033dcefb62fef`
- M2 main integration: `77132528a29d073080e5966844d033dcefb62fef` (`Director: integrate coherent opening-screen isometric village`)
- Current M3 code head before this handoff: `2e55002958e7383afbaf56b925fdbf74dd64dd30`
- Canon: `design/PLAYTEST_CANON_20260910_2149.md` plus latest user direction that village props must read as 2.5D, not flat 2D.
- Evidence: procedural geometry is `ADAPTED/B`; verified original source art remains `PENDING_CROP`.

## Already integrated — do not reopen

- Tile-locked NW/NE/SW/SE movement is on main.
- Opening screen has three coherent 2:1 isometric buildings on main: `west_house`, `plaza_landmark_a`, `plaza_landmark_b`.

## M3 visible result

`AdaptedMillesDecorationLayer` now gives directional props an explicit isometric axis (`NW_SE` / `NE_SW`) and adds opening-screen tree/fence/shop-sign/bench grouping around the three coherent buildings.

`AdaptedMillesMapRenderer.drawDecorations(...)` no longer renders village props as flat screen-space primitives. Every prop family now has a 2.5D renderer:

- TREE: projected ground shadow, two-face trunk prism, four layered faceted canopy masses.
- FENCE: endpoints derived in world coordinates and passed through `worldToScreen`; rails follow the 2:1 world slope and posts are two-face prisms with caps.
- SIGN: isometric post plus axis-aligned projected sign board.
- WELL: raised diamond stone ring with two visible vertical faces and inset opening.
- BENCH: axis-projected seat slab, thickness, supports, and back rail.
- LAMP: projected base, vertical post, diamond lantern and cap.
- BUSH: projected shadow plus layered faceted crown masses.
- GATEPOST: two-face stone prism with projected cap/roof.

The live call path remains `GameView -> AdaptedMillesMapRenderer.draw(...) -> drawDecorations(...)`; no new GameView wiring is required.

## Regression audit

`AdaptedMillesDecoration25DAudit` enforces:

- depth-sorted decoration data;
- `ADAPTED/B` + `ISOMETRIC_25D` evidence/status;
- positive dimensions;
- FENCE/SIGN/BENCH cannot use `Axis.NONE`;
- opening-screen tree/fence/shop-sign/shop-bench groups must remain present.

## Verification status

- IMPLEMENTED: yes.
- Static code/diff inspection: complete.
- Full Android BUILD VERIFIED: not claimed; no Actions run is attached yet.
- RUNTIME VERIFIED: not claimed; requires Director APK/device visual check.

## Next one World result

Do not add more systems or expand map bounds. After Director build/device inspection, tune only the opening-screen composition if occlusion/scale is visibly wrong; otherwise continue replacing remaining flat fallback buildings with the coherent isometric building path.
