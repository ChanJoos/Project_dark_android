# DEV HISTORY — PASS 33 WORLD

## Scope
World-owned tap navigation robustness only. `GameView.java`, CharacterRenderer, Combat, RPG, HUD and quest files were not modified.

## Defect found
`WorldMoveTargetController` used one cumulative replan counter for an entire movement request. On a long route, three unrelated transient runtime WALK rejections (for example moving NPC/monster occupancy) could consume the lifetime budget and force a valid route into `BLOCKED` even after successful progress between those events.

## Fix
- Renamed the internal budget semantics to `MAX_CONSECUTIVE_REPLANS`.
- A rejected WALK increments the consecutive replan count and rebuilds the path from the current position.
- Any successful WALK resets the count to zero because forward progress has resumed.
- Three truly consecutive failed WALK attempts still fail closed as `BLOCKED`, preventing infinite loops.
- Public DTO/API, ground vs NPC request separation, replacement/cancel semantics and collision authority are unchanged.

## Regression audit
Added `WorldMoveTargetDynamicReplanAudit`:
- injects three non-consecutive transient WALK rejections across one long ground move and requires `REACHED`;
- injects a persistent WALK rejection and requires `BLOCKED` after three attempts;
- rechecks target replacement and direct-input cancellation semantics.

## Verification state
- GitHub writes: PASS.
- Isolated javac/runtime audit: NOT EXECUTED because the execution container could not resolve `github.com` while cloning the branch (`Could not resolve host: github.com`).
- Gradle/APK/device runtime: Director-owned and not verified in this pass.

## Canon safety
No original Milles geometry/value claims were added. All existing `[ADAPTED]/[B]` geometry and `PENDING_TARGET_MAP` status remain unchanged.
