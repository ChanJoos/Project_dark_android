# PROJECT DARK Autonomous Development History

Evidence grades used throughout this project:
- **[O]** official Nexon / official-hosted source
- **[V]** verified legacy official-hosted community or experiment source
- **[B]** balanced prototype reconstruction where the original exact value/behavior is not verified
- **[ADAPTED]** deliberate mobile/runtime adaptation
- **PENDING_CROP** visual asset not yet sufficiently identified/licensed for direct mapping

## 2026-09-10 — Autonomous pass 01

### Repository state read
- Latest gameplay renderer: `app/src/main/java/com/projectdark/mobile/GameView.java`, v0.54 prototype.
- Existing implemented runtime concepts before this pass: official-hosted world screenshot background, mobile HUD, 4-direction diagonal movement, procedural prototype character, CAST, weapon attack variants, and martial-artist KICK.
- The previous CI failed at Java compilation in the frame loop.

### Failure found and fixed
- **Failure:** `GameView cannot be converted to Runnable` at `postDelayed(this,16)` inside a lambda assigned to `Runnable loop`.
- **Cause:** inside a lambda, `this` refers to the enclosing `GameView`, not to the lambda/Runnable instance.
- **Fix:** replaced the lambda loop with an anonymous `Runnable` implementation so `this` is the actual Runnable when passed to `postDelayed`.
- Commit: `5193ba8d9f7235bab9f1d6a051c9e7b3fcacc7c4`.

### CI policy corrected for autonomous development
- The overnight development instruction explicitly requires compile/static verification but **no APK packaging**.
- Changed `.github/workflows/android.yml` from `:app:assembleDebug` + artifact upload to compile-only `:app:compileDebugJavaWithJavac`.
- This prevents later autonomous passes from packaging APKs while still validating Android Java sources.
- Commit: `db965ebc2faf04b0f59bda33e584213ffc4f6844`.

### Runtime behavior currently integrated
- **[ADAPTED]** joystick input maps to the original-style four screen-diagonal directions: SW/SE/NW/NE.
- `IDLE` / `WALK` states are mutually exclusive with action states.
- **[B]** action state set: `CAST`, `SWING`, `THRUST`, `THROW`, `PUNCH`, `KICK`.
- **[B]** action durations currently use prototype values, not claimed original timings.
- Movement pauses while an action animation is active, then returns to WALK when movement input remains held, otherwise IDLE.
- Bottom-right prototype controls:
  - `MP` → CAST
  - `P` → cycle SWING / THRUST / THROW / PUNCH
  - `S` → martial-artist KICK
  - `ATK` → execute selected weapon attack type
- Character rendering remains a **prototype procedural sprite**, not authenticated original pixel art.

### Official evidence anchors
- **[O] Character creation:** https://lod.nexon.com/info/guide/82283 — character creation includes gender, hair, hair color, and name; no job selection at creation.
- **[O] Basic UI:** https://lod.nexon.com/info/guide/82285
- **[O] Unified slots:** https://lod.nexon.com/info/guide/82284
- **[O] Game intro / five base jobs:** https://lod.nexon.com/info/intro
- **[O] Official-hosted world screenshot currently used as background:** https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png

### Asset/evidence constraints retained
- Do not label the procedural character as original art.
- Do not fabricate exact original animation frame counts/timings when not verified.
- Direct character/NPC/monster/skill sprite mapping remains **PENDING_CROP** until a specific official-hosted visual can be confidently identified and licensing/redistribution status is clear enough for the prototype use case.
- The current world is still screenshot-backed rather than a real tile/collision map. It is a visual prototype only.

### Remaining design/runtime problems
1. World renderer has only rectangular movement bounds; no map-specific walkability/collision geometry yet.
2. Character has four facing directions but uses procedural limb drawing rather than verified original sprites.
3. Attack and spell effects are minimal placeholders; no cooldown/MP/resource model yet.
4. No NPC interaction loop yet.
5. No monster target/HP/chase/combat loop yet.
6. HUD contains prototype text/labels that should be progressively replaced with a more faithful mobile arrangement.

### Next autonomous priority
1. Wait for and verify compile-only CI on `db965ebc...`; fix any compilation errors immediately.
2. Introduce a small runtime data/state layer instead of keeping all gameplay state in `GameView`.
3. Add walkability/collision primitives and obstacle-aware movement bounds while preserving four-direction logical movement.
4. Then add NPC approach/dialogue prototype before monster combat.
