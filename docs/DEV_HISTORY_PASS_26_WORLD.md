# PROJECT DARK — DEV HISTORY PASS 26 · WORLD / MAP ENGINE

Date: 2026-09-10
Role: World · Map Engine
Branch: `agent/world/20260910-1455`

## Source-of-Truth gate

Read the latest main contributor gate, canonical design/data/source documents, Director backlog,
World PASS 24–25 history and current world handoff before implementation. Original Milles portal
destinations and arrival coordinates remain unverified.

## Implemented

Added `WorldPortalTransitionController`, a stable Android-free world API with:

- activation-radius validation in world coordinates;
- explicit `READY / TARGET_PENDING / DISABLED` readiness;
- fail-closed pending/disabled outcomes;
- exactly-once request emission while the player occupies a portal;
- explicit accepted, pending, completed, rejected and stale-completion results;
- target map plus target spawn identity in the request DTO;
- move-target cancellation only after runtime accepts the transition;
- leave-before-retry latch semantics.

`WorldPortalTransitionAudit` proves that the current pending Milles south exit emits no request,
accepted transitions emit once, repeated overlap cannot duplicate them, stale completion is ignored,
and runtime rejection does not cancel movement.

## Verification status

- PLANNED: complete.
- IMPLEMENTED: complete on the existing World draft branch.
- BUILD VERIFIED: partial. Both new Java sources compile and the deterministic audit passes in the
  worker Java runtime. Full Gradle/APK build is not claimed.
- RUNTIME VERIFIED: no. Director/UX integration and Android execution remain required.

## Evidence safety and blocker

The existing `milles_south_exit_proto` stays `TARGET_PENDING`; no original destination or arrival
coordinate was invented. The concrete blocker to an on-screen map change is a verified destination
map/spawn plus Director wiring. Until both exist, entering the south gate must return
`BLOCKED_TARGET_PENDING` and leave the player in the current map.
