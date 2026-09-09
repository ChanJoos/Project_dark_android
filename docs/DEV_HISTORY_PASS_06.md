# PROJECT DARK Autonomous Development History — Pass 06

Date: 2026-09-10

Evidence grades used in this pass:
- **[O]** official Nexon / official-hosted source
- **[V]** verified legacy official-hosted community or experiment source
- **[B]** balanced prototype reconstruction where original exact data is not verified
- **[ADAPTED]** deliberate mobile/runtime adaptation
- **PENDING_CROP** visual asset not yet sufficiently identified/licensed for direct mapping

## Repository state read
- Continued from `GameView` v0.59 and `docs/DEV_HISTORY_PASS_05.md`.
- Pass 05 left monster blocker deadlocks, result/progression projection, CAST validation order, target-facing, and `CombatIntent` extraction as next priorities.
- Existing CI remains compile-only (`:app:compileDebugJavaWithJavac`); no APK assembly/package step was introduced.

## Runtime collision and pursuit stabilization
- Expanded runtime collision from world blockers/player-vs-monster only to include:
  - player vs NPC fixture collision **[B]**,
  - monster vs NPC fixture collision **[B]**,
  - monster vs monster separation **[B]**.
- Added `NPC_RADIUS=10f` as an explicit prototype-only collision radius **[B]**.
- Refactored player and monster occupancy checks into `playerCanOccupy` / `monsterCanOccupy` so the same collision policy is reused consistently.
- Added bounded local monster detour steering inside `RuntimeState.tryMoveMonster()`:
  1. attempt requested pursuit vector,
  2. if fully blocked, try one perpendicular direction,
  3. try the opposite perpendicular direction,
  4. periodically flip detour preference after a bounded stall window.
- This steering is **[B]** and explicitly not claimed as original Legend of Darkness pathfinding.
- Because existing `GameView.updateMonsters()` already calls `tryMoveMonster()`, this change immediately applies to the current playable pursuit loop without changing the renderer/controller API.
- Commit: `975b937ad0854e7acef1cee17b25cac8e50001e5`.

## Evidence-safe combat result projection
- Added `RuntimeMetrics.java` as a neutral projection over `CombatLedger`.
- It records only prototype counters:
  - monster hit count,
  - monster defeat count,
  - player hit count,
  - player defeat count,
  - player revive count,
  - total prototype damage dealt/taken.
- It intentionally grants **no XP, gold, drops, quest credit, stat growth, or progression rewards** until those rules have verified evidence or are explicitly introduced as `[B]`.
- `RuntimeState` owns `RuntimeMetrics`, exposes it via `metrics()`, and consumes new ledger events at the end of each runtime tick.
- Commits: `11a8c4edf074dd79c65ed24dcb4af974a6459132`, `f24a7fe2b3dcde8e91957f72847cfe70726872c2`.

## Design contradictions found and resolved
1. **NPCs were interactable fixtures but non-solid during movement** — prototype bodies could be walked through while still being tap targets. Resolved with explicit `[B]` NPC occupancy radii.
2. **Multiple monsters could converge onto the same screen-space point** — resolved with monster-to-monster separation in the prototype collision layer.
3. **Monster pursuit stalled permanently on simple blocker arrangements** — added bounded perpendicular detour fallback rather than inventing a full original pathfinding algorithm.
4. **CombatLedger had presentation consumption but no gameplay-neutral result projection** — added `RuntimeMetrics` while deliberately withholding unverified progression rewards.

## Evidence / asset status
- **[O] Basic UI:** https://lod.nexon.com/info/guide/82285
- **[O] Unified slots:** https://lod.nexon.com/info/guide/82284
- **[O] Character creation:** https://lod.nexon.com/info/guide/82283
- **[O] Five base jobs:** https://lod.nexon.com/info/intro
- **[O] Current official-hosted screenshot visual source:** https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png
- World collision rectangles, collision radii, entity spawn coordinates, combat timings/damage/ranges, detour steering, and respawn remain **[B]** unless separately verified.
- Procedural player/NPC/monster bodies remain prototype visualization / **PENDING_CROP**; they are not claimed to be original LOD sprites.
- No new AI-generated visual asset was added or promoted as original art.

## Validation status
- Compile-only validation is triggered for every pushed commit.
- Code head for this pass: `f24a7fe2b3dcde8e91957f72847cfe70726872c2`.
- At the moment this history entry was written, compile-only run #40 had started and had not yet reported a final conclusion. The next pass must read the final run result first; if failed, inspect the compiler log and repair before feature work.
- No APK packaging/distribution was performed.

## Remaining issues / next autonomous priority
1. Read compile-only run #40 result and repair immediately if needed.
2. Fix CAST order so target/range validity is decided before MP/cooldown consumption. Prefer explicit range failure for magic unless auto-approach is deliberately selected as `[ADAPTED]`.
3. Update facing toward selected target immediately before CAST/ATTACK/SKILL/KICK.
4. Extract `CombatIntent`/approach state from `GameView` into a gameplay controller/policy class before the view accumulates more combat rules.
5. Replace GameView-local defeat/hit counters with `RuntimeMetrics` once controller extraction makes the change low-risk.
6. Continue refining monster pursuit only as bounded `[B]` steering; do not claim original AI/pathfinding behavior.
7. Continue official-hosted sprite/effect identification; keep visual bodies/effects `PENDING_CROP` until positively identified and legally usable.
8. Keep compile-only validation and do not assemble an APK during autonomous night passes.
