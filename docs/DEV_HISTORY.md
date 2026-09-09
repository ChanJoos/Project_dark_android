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

## 2026-09-10 — Autonomous pass 02

### Previous validation result
- Compile-only GitHub Actions run #15 on `2ca224ae...` completed **successfully**.
- No APK packaging/upload step is present in the autonomous validation workflow.

### Runtime state layer introduced
- Added `RuntimeState.java` to separate player/world/entity data from `GameView`.
- **[B]** Player world bounds and four rectangular screen-space collision blockers were added solely to exercise collision against the current screenshot-backed world.
- **[ADAPTED]** movement collision uses axis-separated resolution so the player can slide along blockers rather than stopping both axes.
- These blockers are not claimed as original map collision data.
- Commit: `f63344301c6fb71c09b1541621e032cb1bbfbbdd`.

### NPC approach/dialogue loop
- Added one prototype NPC entity with `PENDING_CROP` visual status.
- NPC tap behavior:
  1. if already within interaction distance, open dialogue;
  2. otherwise automatically approach using the same four screen-diagonal movement vocabulary;
  3. stop and open dialogue when within interaction range;
  4. manual joystick input cancels the approach.
- Added dialogue panel overlay integrated into the existing runtime.
- NPC placement/name/dialogue are **[B]** interaction fixtures, not original content claims.
- Compile-only CI run #17 for the first collision/NPC integration completed **successfully**.
- Commit: `40119a49f33dfd1512197e74512952194c1705cb`.

### Design contradiction found and fixed
- Existing bottom-right labels were `HP / MP / P / S`, but touch coordinates actually mapped the MP slot to attack-mode cycling.
- Corrected input mapping so UI label and runtime action agree:
  - `MP` -> CAST
  - `P` -> weapon attack mode cycle
  - later expanded `SK` -> generic skill and `K` -> martial kick
  - `ATK` -> selected weapon attack
- Commit: `96aafbbf73cd38e3e6ba13f421618dfc2e601a5a`.

### Minimal monster/combat loop
- Added `RuntimeState.Monster` and one generic `훈련용 몬스터 [B]` fixture.
- Monster visual remains **PENDING_CROP** and is intentionally generic; it is not labeled as any original LOD monster.
- **[B]** prototype state includes HP, alive/dead, chase radius, attack interval, incoming damage and player HP loss.
- Monster tap selects target and drives the top-center target HP panel.
- Target ring and per-monster HP bar were added.
- Monster approaches the player inside a prototype aggro radius and applies prototype damage in melee range.
- `RuntimeState` combat primitive commit: `6180e28932457a04fc154dd06f414f83f6d7f7b8`.

### Skill/magic/resource prototype
- Expanded action states to include generic `SKILL` in addition to `CAST`, four weapon attacks and `KICK`.
- **[B]** prototype resource/cooldown rules were added strictly as runtime scaffolding:
  - CAST: MP cost 8, cooldown 1.2 s, prototype target damage 12 within 150 screen units.
  - normal weapon attack: cooldown 0.38 s, damage 8; THROW has longer prototype reach.
  - generic SKILL: MP cost 5, cooldown 2.0 s, damage 16 within 90 units.
  - martial KICK: cooldown 1.0 s, damage 14 within 82 units.
- These values are not claimed to match original server data and must remain `[B]` until verified.
- Bottom-right runtime slots are now `HP / MP / P / SK / K`, plus `ATK` and `AUTO`.
- Commit: `a1398cfff1db6ebf49cf25510e825b7d136333ba`.

### Evidence anchors retained
- **[O] Basic UI:** https://lod.nexon.com/info/guide/82285
- **[O] Unified slots:** https://lod.nexon.com/info/guide/82284
- **[O] Character creation:** https://lod.nexon.com/info/guide/82283
- **[O] Five base jobs:** https://lod.nexon.com/info/intro
- **[O] Official-hosted screenshot background:** https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png

### Validation status at history write
- RuntimeState monster/combat primitive compile run #19 completed **successfully**.
- Full `GameView` v0.56 combat/skill integration compile run #20 was queued when this history entry was written; the next pass must inspect its final result before extending code.
- APK was not built or packaged.

### Remaining problems / next priority
1. Verify run #20; if it fails, fix compiler errors before any new feature work.
2. Monster chase currently ignores world blockers; route monster motion through world walkability/collision rules.
3. NPC auto-approach is direct four-diagonal pursuit only; add a tiny obstacle-aware detour/path recalculation instead of cancelling on first blocker.
4. Move skill definitions out of `GameView` into a data model (`SkillDef`/`SkillRuntime`) with evidence grade, cost, cooldown, range and effect type.
5. Add visual cooldown masks/remaining time to the bottom-right slots.
6. Add player death/monster death state feedback and a minimal reset/revive prototype.
7. Current screenshot-backed world must eventually be replaced by a proper tile/object/collision representation when official assets can be positively identified; until then keep map geometry `[B]` and asset sprites `PENDING_CROP`.
