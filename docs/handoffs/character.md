# PROJECT DARK — Character / Animation Handoff

Date: 2026-09-10
Branch: `agent-character-20260910-1328`
Draft PR: #3

## Implemented
- Reduced procedural player presentation from fixed `2.0x` scaling to explicit `[ADAPTED]` `PLAYER_RENDER_SCALE=1.35f`.
- Kept `Pose.x/y` anchor coordinates unchanged; render size and shadow size are presentation-only constants so logical/world coordinates are not rescaled.
- Adjusted vertical render anchoring from a fixed 56 px assumption to a scale-derived offset.
- Added `EffectFamily.MAGIC` and a distinct prototype `[ADAPTED]` magic visual hook, separate from generic CAST and SKILL effects.
- No `GameView.java`, combat, RPG, map/camera, HUD, or quest internals were modified.

## User-visible delta
- Once this PR is integrated, the existing procedural player rendered through `CharacterRenderer` should appear materially smaller than the prior 2.0x version while keeping the same logical position.
- MAGIC now has a renderer-level presentation family available for UX/combat wiring; it will become visible when the UX owner supplies `EffectFamily.MAGIC` in the pose/event adapter.

## Contract requests
- UX owner: continue to use `CharacterRenderer` as the only player presentation path and wire MAGIC input/event to `EffectFamily.MAGIC`; do not duplicate rendering in `GameView`.
- World owner: no action required; world/camera coordinates are unchanged.
- Combat owner: no semantic changes required; continue to expose action/effect identity via contract rather than drawing directly.

## Evidence / provenance
- Current procedural drawing remains `[B]` prototype material.
- Render-scale reduction is `[ADAPTED]`; it is not claimed as an exact original-game pixel scale.
- Authenticated original sprite/frame/timing remains `PENDING_CROP`.

## Validation status
- Source diff is isolated to CharacterRenderer-owned code plus this handoff.
- CI/build status must be checked by Director before integration.
- Runtime device validation is still required after integration; this worker does not package APKs.

## Integration note
Main advanced after this branch was created. Director should rebase/refresh PR #3 against the latest main and resolve any contract-only conflict before merge.