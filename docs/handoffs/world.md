# World handoff — PASS 33

- Branch: `agent/world/20260910-1812`
- Base lineage: stacked on PASS 32 PR #49 head `05da88dbbb673126a09b74954d30c09e67c0492e`; main was still `6785efb6f7504e070ee0c0aa6924d1281e444469` when this run began.
- Geometry: current Milles expansion remains `[ADAPTED]/[B]`; original geometry still unverified.

## P0 navigation fixes now in lineage
1. PASS 32: off-grid ground taps no longer false-BLOCK because the 16-unit search lattice can enter the exact target leg from half-cell diagonal tolerance when the exact point is occupiable.
2. PASS 33: dynamic replan budget is now consecutive-only. Successful WALK progress resets the budget, so unrelated transient NPC/monster collisions across a long route do not accumulate into a false terminal `BLOCKED`.
3. Persistent rejection still fails closed after three consecutive WALK failures; no infinite loop or teleport fallback was added.

## New regression gate
`WorldMoveTargetDynamicReplanAudit` verifies:
- three non-consecutive transient WALK rejections during one long ground move still end in `REACHED`;
- persistent WALK rejection ends in `BLOCKED` after three attempts;
- replacement and direct-input cancellation semantics remain intact.

## Stable integration contract
- Empty eligible world tap: UI ownership reject → `screenToWorld` → `requestGroundMove`.
- NPC hit retains priority and uses `requestNpcApproach` separately.
- `tick()` emits only incremental `Walker.tryWalkStep` movement. Runtime collision remains authoritative.
- A failed runtime step triggers world-side replanning from the current world coordinate.
- Director/UX must not duplicate this path/replan logic in `GameView.java`.

## Verification
- GitHub source commits: PASS.
- Isolated javac/audit execution: blocked by container DNS while cloning GitHub (`Could not resolve host: github.com`), not claimed as PASS.
- Gradle/APK/device runtime: pending Director integration.

## Remaining P0
- End-to-end empty-map tap and camera/world projection still require Director/UX runtime wiring in the APK.
- South portal remains `PENDING_TARGET_MAP` and must fail closed.
- Original Milles geometry remains evidence-blocked.

No CharacterRenderer, Combat, RPG, HUD, quest or `GameView.java` file was modified.
