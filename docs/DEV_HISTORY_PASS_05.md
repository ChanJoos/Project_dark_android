# PROJECT DARK Autonomous Development History — Pass 05

Date: 2026-09-10

Evidence grades used in this pass:
- **[O]** official Nexon / official-hosted source
- **[V]** verified legacy official-hosted community or experiment source
- **[B]** balanced prototype reconstruction where original exact data is not verified
- **[ADAPTED]** deliberate mobile/runtime adaptation
- **PENDING_CROP** visual asset not yet sufficiently identified/licensed for direct mapping

## Repository state read
- Continued from `GameView` v0.58 and `docs/DEV_HISTORY_PASS_04.md`.
- Pass 04 identified five immediate issues: duplicated world visual URL, unfinished monster telegraph, unused player hit flash, ATK-only target approach, and unconsumed `CombatLedger` events.
- Existing compile-only workflow was verified to run `:app:compileDebugJavaWithJavac`; no APK assembly or artifact packaging is configured.

## RuntimeState attack lifecycle repair
- Added `Monster.attackPrimed` and explicit neutral attack lifecycle helpers: `beginMonsterAttack`, `monsterAttackReady`, `resolveMonsterAttack`, and `cancelMonsterAttack`.
- `[B]` windup remains 0.24 s and prototype monster contact damage/cooldown remain 4 HP / 1.2 s. These are explicitly not original values.
- Moved monster cooldown decrement into `RuntimeState.tick()` so cooldown state is no longer decremented independently by `GameView`.
- Defeat and respawn now clear `attackPrimed` as well as windup/cooldown state.
- Commit: `566d003fbf834196a8e9fe1f997e5651ddca3fd9`.
- Compile-only run #35 completed successfully.

## GameView v0.59 integration
- Removed the duplicated `WORLD_URL`; runtime loading now consumes the official-hosted visual source from `WorldDef`.
- Added `CombatIntent { NONE, ATTACK, SKILL, KICK }` and generalized target approach behavior:
  - ATK approaches using the selected `AttackDef` range.
  - generic SKILL approaches using `SkillDef.SKILL_PROTO.range`.
  - martial KICK approaches using `SkillDef.KICK_PROTO.range`.
  - manual joystick input cancels approach immediately `[ADAPTED]`.
- Monster combat now uses the full `[B]` telegraph lifecycle instead of immediate contact damage:
  1. player enters contact range,
  2. monster attack is primed,
  3. a neutral expanding warning ring is rendered,
  4. damage resolves after windup if the player remains in range,
  5. leaving range cancels the primed attack.
- Player `hitFlash` is now visibly consumed by the renderer through a short warm flash on the prototype character/shadow.
- Added a bounded `CombatLedger` consumer in `GameView` using event sequence numbers.
  - prototype counters currently expose monster defeats and received-hit count.
  - ledger feedback is used for player hit/defeat/revive messages.
  - no XP, currency or drop rewards were invented.
- Reduced development-only chat text and kept the lower-right `MP / P / SK / K / ATK` interaction surface intact.
- Commit: `f7732c0fa5dc2105e2d91112cd11ad4057893a85`.

## Validation
- GitHub Actions run #36 validated commit `f7732c0f...`.
- `Compile debug sources only` completed **successfully**.
- Workflow remains compile-only:
  `gradle :app:compileDebugJavaWithJavac --stacktrace`
- No `assembleDebug`, APK packaging, APK artifact upload, signing or distribution was performed.

## Design contradictions found and resolved
1. **World visual identity duplication** — resolved by consuming `WorldDef` from `GameView`.
2. **Attack telegraph state without renderer/resolve consumer** — resolved with explicit primed lifecycle and neutral `[B]` warning ring.
3. **Player hit flash state unused** — resolved by the character renderer.
4. **ATK auto-approach inconsistent with SKILL/KICK** — generalized through `CombatIntent`.
5. **CombatLedger collected events but did not affect runtime presentation** — added sequence-aware consumer without inventing progression rewards.
6. **Monster cooldown decremented by view code while combat state belonged to RuntimeState** — consolidated into `RuntimeState.tick()`.

## Evidence / asset status
- **[O] Basic UI:** https://lod.nexon.com/info/guide/82285
- **[O] Unified slots:** https://lod.nexon.com/info/guide/82284
- **[O] Character creation:** https://lod.nexon.com/info/guide/82283
- **[O] Five base jobs:** https://lod.nexon.com/info/intro
- **[O] Current official-hosted screenshot visual source:** https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png
- Current screen-space collision geometry and fixture coordinates remain **[B]**.
- Procedural player/NPC/monster bodies remain prototype visualization / **PENDING_CROP**, not claimed original LOD sprites.
- The monster warning ring and player flash are **[B]/[ADAPTED]** feedback only, not claimed original effects.

## Remaining issues / next autonomous priority
1. Generalize CAST range handling so ranged magic does not consume MP/cooldown before a valid target/range outcome; decide approach-vs-explicit range failure as `[ADAPTED]` rather than claiming original targeting behavior.
2. Separate `CombatIntent`/approach routing out of `GameView` if action logic continues to grow; avoid Canvas view becoming the gameplay controller.
3. Add a small evidence-aware progression/result interface over `CombatLedger`, initially with no rewards or clearly `[B]` counters only.
4. Improve monster pursuit around blockers with bounded detour/path retry; current sign-based pursuit can stall against `[B]` obstacles.
5. Add target-facing update before CAST/ATTACK/SKILL/KICK so action direction consistently follows the selected target.
6. Continue reducing placeholder HUD copy while preserving the approved compact mobile composition.
7. Continue official-hosted sprite/effect identification. Do not replace `PENDING_CROP` bodies until a source is clearly identified and redistribution/use status is understood.
8. Keep compile-only validation; do not package an APK during autonomous night passes.
