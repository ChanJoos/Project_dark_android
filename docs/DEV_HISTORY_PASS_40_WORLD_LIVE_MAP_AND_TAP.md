# DEV HISTORY — PASS 40 WORLD LIVE MAP AND TAP

## Trigger

The latest device canon reports two runtime failures: the APK still presents a stretched screenshot instead of the authored tile/object village, and eligible empty-map taps remain unreliable. Previous World work created the data and renderer but the single live integration surface remained only in Draft PR #71.

## Implemented

- Reintroduced `WorldLiveMapLayer` on current `main` as the stable screenshot-free renderer/navigation facade.
- Added procedural `[ADAPTED]/[B]` material detail to grass, road, plaza and gate tiles so the authored map reads as a connected village rather than flat colored diamonds.
- Replaced fixed 4,096-node A* budget with a bounded active-map cell budget.
- Reset the replan streak after every successful WALK step.
- Aligned adapter map-bound eligibility with player radius.
- Moved the stale east-outer anchor out of collision.
- Added a 14-target off-grid/camera/bounds/replacement/cancel/dynamic-blocker acceptance audit.
- Updated the earlier exploration audit to the current expanded village bounds and blockers.

No `GameView.java`, Character, Combat, RPG, HUD or quest-owned source was modified.

## Verification

- navigation sources compiled using the JDK compiler module;
- base move-target audit passed;
- expanded exploration audit passed;
- arbitrary tap acceptance passed for 14 targets;
- live renderer/facade compiled against minimal Android graphics API stubs;
- full Gradle/APK and device runtime remain Director-owned and are not claimed.

## GitHub

- branch: `agent/world/20260910-2014`
- Draft PR: #76
- code commit on GitHub: `5b8e2fab22217b9b6c9cbe9caf4e52b0bf73a1c1`
