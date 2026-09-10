# DARK 통합·검증 handoff

## 2026-09-11 — M1 tile movement runtime wiring

- Latest main before integration: `a58e351`.
- Integrated World result: PR #81 follow-up branch `agent/world/20260911-0117`, head `464f315`.
- Local integration commit: `7a7e234` (World commit cherry-picked onto the newer main so the THREE-role guide update is preserved).

### Implemented and reviewed

- `GameView` joystick no longer calls free-pixel `RuntimeState.tryMove`; each pulse calls `WorldRuntimeAdapter.step(NW/NE/SW/SE)`.
- Eligible map taps, NPC approach and monster approach use the same authored-tile graph.
- World `Snapshot.lastStepDirection` drives Character SW/SE/NW/NE facing.
- Logical destinations are authored 64×32 tile centers; no arbitrary final pixel leg remains.
- Camera follow remains in the actual GameView frame loop after every update, including direct joystick steps.

### Verification state

- `git diff --check`: PASS.
- Static integration review: PASS for removal of player free-pixel bypass and direct World-to-Character facing mapping.
- Focused Java audits: not executed locally because this runner has no JDK (`javac` unavailable).
- Gradle/assembleDebug: not executed locally because this runner has no `gradle` command or wrapper.
- Android runtime: not available in this runner. Do not label RUNTIME VERIFIED until a device/emulator demonstrates the acceptance actions below.

### Device acceptance

1. Hold joystick in NW/NE/SW/SE and confirm each repeat lands on exactly one adjacent diamond center while the camera stays aligned.
2. Tap at least one off-center point on both left and right sides; confirm the marker snaps to a tile center and every path segment is adjacent.
3. Move NW→SE and NE→SW repeatedly; confirm exact return with no accumulated drift.
4. Confirm Character facing follows each step; SE must read lower-right and both left directions must render without corruption.
5. Launch/relaunch and confirm no startup crash. Current Character remains the safe fallback until the new 24×32/1.50 atlas passes its own gate.

### Next integration result

After CI/build evidence, merge only the next independently verified Character 24×32/1.50 four-direction result. In parallel, accept World M2 in small visible pieces beginning with one properly projected building connected to the road.
