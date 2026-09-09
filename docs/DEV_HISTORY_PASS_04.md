# PROJECT DARK Autonomous Development History — Pass 04

Date: 2026-09-10

Evidence grades used in this pass:
- **[O]** official Nexon / official-hosted source
- **[V]** verified legacy official-hosted community or experiment source
- **[B]** balanced prototype reconstruction where original exact data is not verified
- **[ADAPTED]** deliberate mobile/runtime adaptation
- **PENDING_CROP** visual asset not yet sufficiently identified/licensed for direct mapping

## Repository state read
- Continued from `GameView` v0.58 and the pass-03 `RuntimeState`, `SkillDef`, and `AttackDef` integration.
- Prior documented compile-only validation was successful.
- The next documented priorities were world manifest separation, attack telegraph/player damage feedback, target approach expansion, and combat result bookkeeping.

## World manifest separation
- Added `WorldDef.java`.
- Moved current runtime world bounds, player spawn, prototype collision rectangles, NPC fixture spawn and monster fixture spawn into one explicit manifest.
- `WorldDef.VISUAL_SOURCE_URL` is tagged **[O]** because it points to the official-hosted Nexon screenshot already used by the runtime.
- Collision rectangles, spawn coordinates and fixture data remain explicitly **[B]** and are not represented as extracted original map geometry.
- Character/NPC/monster/tile/object visual mapping remains **PENDING_CROP**.
- Commit: `cbb086dd07796eb0c306e5943ae76ffbc259fcc6`.

## Combat result bookkeeping
- Added `CombatLedger.java` to decouple gameplay outcomes from Canvas/UI rendering.
- Event types now cover prototype player hit, monster hit, monster defeated, player defeated, monster respawn and player revive.
- The ledger stores only bounded runtime events and deliberately contains no claimed original XP/drop rewards.
- This creates an attachment point for later `[O]/[V]` progression/drop data or `[B]` prototypes without putting result logic into `GameView`.
- Commit: `0c53e3f609c2b05ed9d265a2b8ec24c6463f197c`.

## RuntimeState integration
- `RuntimeState` now constructs collision geometry and entity fixtures from `WorldDef` instead of owning duplicated world constants/data.
- NPC and monster runtime entities carry `assetStatus`, currently `PENDING_CROP`.
- Player and monster damage, defeat, respawn and revive now emit `CombatLedger` events.
- Added `Player.hitFlash` state and a short monster `attackWindup` state as **[B]** presentation/runtime scaffolding. The windup API is not yet invoked by the current `GameView` monster attack loop, so no false claim is made that attack telegraph integration is complete.
- Commit: `6d617780bd5050c5da15ad6408bdf8406f5951ae`.

## Validation
- GitHub Actions compile-only run #33 validated commit `6d617780...`.
- `Compile debug sources only` completed **successfully**.
- No `assembleDebug`, APK packaging or artifact upload was performed.

## Design contradictions / remaining coupling found
1. `GameView` still duplicates the official screenshot URL instead of consuming `WorldDef.VISUAL_SOURCE_URL`. This is harmless at compile time but should be removed to make `WorldDef` the single source of world identity.
2. The monster loop currently applies damage immediately when cooldown permits. `RuntimeState.attackWindup` exists but is not yet consumed by `GameView`; the visible telegraph therefore remains unfinished.
3. `CombatLedger` is integrated into state mutations but no progression/result consumer exists yet. XP and drops must not be invented as original values.
4. Generic SKILL and KICK still report out-of-range rather than sharing the target-approach behavior already used by ATK.
5. The screenshot-backed world remains a visual prototype. Collision rectangles are `[B]`; no authenticated original tile/object collision map has been mapped.
6. Procedural character/NPC/monster bodies remain prototype drawings / `PENDING_CROP`, not original art.

## Evidence anchors retained
- **[O] Basic UI:** https://lod.nexon.com/info/guide/82285
- **[O] Unified slots:** https://lod.nexon.com/info/guide/82284
- **[O] Character creation:** https://lod.nexon.com/info/guide/82283
- **[O] Five base jobs:** https://lod.nexon.com/info/intro
- **[O] Official-hosted screenshot visual source:** https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png

## Next autonomous priority
1. Make `GameView` consume `WorldDef.VISUAL_SOURCE_URL` and remove duplicated world identity data.
2. Integrate the existing `[B]` monster attack windup into the visible runtime, with a neutral telegraph that is not represented as an original animation.
3. Add visible player hit flash using `RuntimeState.Player.hitFlash`.
4. Generalize combat approach intent so ATK / generic SKILL / martial KICK can approach their own definition range while preserving manual joystick override.
5. Add a small result consumer over `CombatLedger`; connect only prototype counters first, with XP/drop rewards remaining `[B]` or absent until evidence exists.
6. Reduce remaining development-only HUD copy and keep the compact right-bottom action surface aligned with the approved mobile composition.
7. Continue official-hosted asset identification before replacing any `PENDING_CROP` body/effect with a claimed original sprite.

> Continuity note: this pass is a companion continuation of `docs/DEV_HISTORY.md`. A later consolidation pass should merge this section back into the primary history file once an append-capable repository edit path is available.
