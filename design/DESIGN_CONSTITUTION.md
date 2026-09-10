# PROJECT DARK — DESIGN CONSTITUTION

Status: CANONICAL / Source of Truth gate

Current amendments: `design/PLAYTEST_CANON_20260910_2149.md` supersedes conflicting older scale/movement/world-expansion requirements below. Operational ownership and execution priorities follow `docs/DIRECTOR_GUIDE.md` (THREE-20260910, three active roles). One coherent village screen is the first production milestone; do not expand bounds before its acceptance.
Updated: 2026-09-10

## 1. Product canon

Core principle: **세계는 원작 그대로. 성장은 원작 그대로. 캐릭터는 원작 그대로. 조작과 UX만 모바일에 맞게 조정한다.**

Development must preserve original proper nouns, job roles, acquisition conditions, currency meaning, quest structure, UI information architecture, and pixel-art proportions when supported by evidence. Mobile changes must be explicitly tagged `[ADAPTED]`.

## 2. Runtime scope — immutable unless user explicitly changes it

`캐릭터 생성 → 평민 Lv1 → 밀레스/초반 성장 → 5개 기본직업(전사/도적/마법사/성직자/무도가) → Lv99 지존 → 전직 OR 순수직업 → 다시 Lv99 → 1차 승급`

Out of scope: 2차 승급, 2차/3차 직업 and later progression. Do not silently expand scope.

Character creation does not select a job. Preserve gender/hair/hair-color/name creation flow; job is selected after starting as 평민.

## 3. Evidence policy

Every original-game claim/value should retain evidence status:
- `[O]`: official/original evidence
- `[V]`: verified old official-hosted/community experiment or directly verified observation as applicable
- `[U]`: user-confirmed recollection
- `[B]`: balanced reconstruction/prototype value, NOT an original fact
- `[ADAPTED]`: deliberate mobile adaptation
- `PENDING` / `PENDING_CROP`: insufficient evidence or asset identification
- `FAN`: fan evidence; never silently promote to official

If exact original HP, damage, timing, drop rate, formula, sprite frame count, map geometry, etc. is unknown, do not invent it as canon. Keep it null/PENDING or isolate a prototype `[B]` value.

## 4. Visual canon

Do not create a new '어둠의전설풍' interpretation and call it original. Official/original gameplay imagery is the visual reference. AI-generated or procedural substitute art may exist only as clearly identified prototype material.

Do not use a whole gameplay screenshot as the final map texture. Final world architecture must support `Tile Map + Object + Collision + NPC + Monster Spawn + Portal` layers.

Original asset redistribution for shipping is licensing-gated. Keep reference/prototype assets distinct from distributable shipping assets.

### User-confirmed playtest presentation requirements — `[ADAPTED]`

- Character/NPC/monster presentation must not dominate the mobile viewport. Logical world coordinates and collision dimensions must remain independent from renderer scale. The current reduced character presentation is user-approved and must not regress to the earlier oversized prototype without explicit user direction.
- **The player character must never be presented as a front-facing avatar while idle/moving in normal field play.** PROJECT DARK uses the original isometric/diagonal presentation. Character appearance must be rendered from side-diagonal viewpoints aligned to the four screen-diagonal directions `NW / NE / SW / SE`.
- Character animation/rendering must support distinct directional visual frames or image sets for those diagonal directions. Mirroring may be used only where visually valid and evidence-compatible; do not collapse all directions into one frontal sprite. `IDLE / WALK / CAST / ATTACK / SKILL / HIT / DEAD` presentation must preserve directional facing where applicable.
- BODY / HAIR / EQUIPMENT / WEAPON / EFFECT layers must share the same directional anchor/facing contract so equipped visuals do not drift or face the wrong way. Unverified source frames remain `PENDING_CROP`; do not fabricate original-looking directional frames and call them canonical.
- Player movement must be presented with a following camera/world scroll rather than a permanently fixed one-screen map. Camera behavior must preserve world↔screen coordinate correctness, map clamp, collision, portal and touch targeting.
- The starting vertical slice must provide enough connected spatial extent to feel like **one explorable village**, rather than a tiny fixed test room. The current expanded prototype is still considered too small for the target experience and must continue expanding. Original geometry must not be invented when unverified; source-backed reconstruction takes priority and authored gaps remain explicitly `[ADAPTED]`.
- ATTACK / SKILL / MAGIC must expose visibly distinguishable presentation states/effect hooks so animation and motion can be judged on-device. Unverified original frames/timing remain `[B]`, `[ADAPTED]`, or `PENDING_CROP` rather than fabricated original facts.
- Combat feedback, including damage presentation, must be legible and game-like rather than crude placeholder text/boxes; exact original styling remains evidence-gated.

## 5. Movement canon

Original presentation uses four screen-diagonal directions `↖ ↗ ↙ ↘`. Logical movement can remain four-neighbor grid movement because isometric projection maps logical axes to those screen diagonals. Do not 'fix' this into 8-neighbor movement without evidence.

The rendered character must face and animate in the matching side-diagonal direction while moving; movement direction and visible facing may not contradict each other. A persistent front-facing field pose is non-canonical.

Mobile joystick/tap movement must issue the same logical movement command. NPC tap may pathfind and approach before interaction.

### Tap-to-move — USER CANON / `[ADAPTED]`

In addition to joystick/directional control, tapping an eligible point on the world must set a movement target. The screen tap is converted using the current camera/world transform and the character **walks** toward the target; tap-to-move must never teleport the player or bypass collision, pathfinding, map bounds, or portal rules.

Input semantics:
- a new eligible world tap replaces the previous movement target;
- direct joystick/directional input cancels or overrides the active tap target;
- combat/action input may cancel or suspend tap movement as required by the shared action state;
- tapping an NPC prioritizes NPC selection/approach/dialogue semantics over generic ground movement;
- **tapping empty eligible map/world space must also move the player to that target; NPC tap-to-approach alone does not satisfy this requirement;**
- HUD, quick-slot, dialogue, utility and other UI touches must never leak through as world movement commands;
- blocked/unreachable targets must terminate or report failure rather than causing infinite movement/path loops;
- reaching the target uses an explicit tolerance appropriate to the movement model;
- successful world taps should receive brief visible target feedback/marker so the player can confirm the command.

World owns movement/path/collision semantics and exposes a stable move-target API. UX/input owns touch hit-testing, screen→world conversion/wiring and UI-vs-world input priority. These responsibilities must not be duplicated in `GameView.java`.

**Current playtest gap (2026-09-10):** world-side move-target/camera contracts exist, but empty-map tap movement is not yet wired end-to-end in the mobile UX runtime. This is a P0 implementation gap, not an optional backlog item.

## 6. Character action states

Minimum common states: `IDLE / WALK / CAST / ATTACK / SKILL / HIT / DEAD`.

All field-capable states should retain the current `NW / NE / SW / SE` facing context where visually applicable. Directional sprite/image binding must be part of the renderer contract, not a one-frame frontal placeholder.

CAST uses a generic raise-hand/hands casting body action unless verified original material says otherwise; spell identity should primarily come from effect/data.

Weapon attack families: `SWING / THRUST / SHOT(or THROW) / PUNCH`. Martial artist requires a dedicated `KICK` action. Prototype timings are `[B]` until measured.

## 7. Combat rules

Separate action data, animation, input, and effect resolution but connect them with a stable `actionId`.

Action validation order should preserve: alive/state control → target type → distance → line of sight → resource → acquisition/usage condition → cooldown. Apply cost/cooldown once and resolve damage/heal/status at the defined action event.

Skills and magic are distinct. Do not apply one universal MP-cost rule to all skills. AC is not a generic 'higher is better' stat. Unknown official probability/defense/element formulas remain PENDING/B.

## 8. RPG/data canon

STR/CON/INT/DEX/WIS, base stats, equipment modifiers, normal EXP/level, ability/progression state, item/equipment identity, quest state and rewards must be separate data concepts. Do not collapse them for implementation convenience.

Item/monster/skill/NPC/map IDs and relationships already present in Master DB or canonical extracted data take precedence over hard-coded Java values.

### Monster reward delivery — USER CANON / `[ADAPTED]`

Canonical runtime flow:
`MONSTER_DEFEATED → reward resolution → direct inventory grant → EXP/Gold/quest progression`.

Resolved monster item rewards are granted directly to inventory exactly once. **There is no ground-drop entity, loot-on-floor state, pickup-distance validation, manual pickup interaction, pickup animation, or AUTO pickup behavior in the target PROJECT DARK runtime.**

The former design `monster death → ground item/drop entity → pickup → inventory` is **RETIRED / SUPERSEDED** as of 2026-09-10. It is no longer an alternative mode, backlog item, future feature, regression expectation, or implementation requirement. Historical references to that flow are archival only and have zero design authority.

Unknown drop probability, quantity, item identity, monster→reward relation, or inventory-capacity policy remains PENDING. Removing pickup does not permit fabricated rewards. Inventory mutation and reward claim must be idempotent.

**Current playtest gap (2026-09-10):** the tested main APK does not yet visibly grant monster item rewards into inventory after defeat. This is a P0 runtime-integration defect against this canon. Combat must emit the defeat event exactly once; RPG/Data must resolve an evidence-backed reward and mutate inventory exactly once; UX must make the resulting inventory change observable. A compiling reward pipeline that is not connected to the live play loop does not satisfy this requirement.

## 9. Quest/world canon

Preserve original start conditions, NPC dialogue sequence, objective conditions, completion dialogue and rewards when evidenced. Quest state must distinguish undiscovered/available/in-progress/completable/rewarded/abandoned or equivalent explicit states. Do not invent lore, NPC relationships or town facts to fill gaps.

First content objective is a coherent original-based starting-region/Milles vertical slice with connected NPC, quest and hunting loop, then expansion.

### World expansion — USER CANON / `[ADAPTED]`

The current camera-follow/world-scroll behavior is user-verified and accepted as the movement presentation baseline. However, the current prototype world extent is still insufficient. Continue expanding the connected Milles/start-region play area so traversal feels like a real village/field slice rather than a camera moving across a small test rectangle.

Expansion must preserve collision continuity, reachable paths, NPC/monster visibility, portal semantics, camera clamp correctness and screen↔world touch conversion. Source-backed geometry is preferred; unverified connective geometry must remain explicitly `[ADAPTED]/[B]` and replaceable.

## 10. Mobile UX canon

World remains visually central. Approved mobile shell: compact party/quest information, target HP top-center, minimap top-right, utility rail, translucent/expandable chat, HP/MP/EXP bottom-center, circular joystick bottom-left, frequent quick slots + attack + AUTO bottom-right. Do not restore giant placeholder HUD boxes or a permanently expanded PC-sized slot grid.

User-approved NPC conversation flow from the current playtest is the baseline and must not regress without a concrete reason. Placeholder rectangular/text-heavy controls should continue moving toward a mobile RPG HUD with adequate touch targets and clear pressed/cooldown/disabled/selected feedback.

Empty-map tap-to-move is a required peer input path to joystick movement. NPC tap-to-approach is a specialized interaction layered above it, not a replacement for generic world tapping.

## 11. AUTO canon

AUTO uses the same combat math and reward path as manual play; no AUTO damage bonus. Manual input overrides AUTO and may resume after ~1.2s `[ADAPTED]`. Repeated path failure must terminate/recover rather than loop forever.

Monster rewards use the §8 direct-inventory contract. AUTO contains no loot navigation or pickup subsystem.

## 12. Agent governance

Before coding, every development agent must read this file, `design/DATA_CONTRACT.md`, `design/SOURCE_OF_TRUTH.md`, canonical data under `data/design/`, Visual Manifest material, and `docs/DEV_HISTORY*`.

Existing canonical IDs/values/relationships must not be rewritten for convenience. If source documents disagree or required data is absent, record `DESIGN_CONFLICT` or `PENDING`; do not silently decide canon.

**User-canon persistence rule:** when the user explicitly approves, rejects, replaces, or adds a gameplay/UX rule, the Director must persist that decision in canonical design documentation before relying on transient chat or agent prompts. Scheduled agents must treat the latest canonical design as authoritative on subsequent runs.

**Supersession rule:** if any older plan, Master prose, backlog, DEV_HISTORY, handoff, test, code comment, or implementation conflicts with §8 monster reward delivery, §5 Tap-to-move user canon, §4 directional character presentation, or §9 world-expansion user canon, the newer user-canon sections win. Agents must not restore retired ground-drop/pickup behavior, remove empty-map tap-to-move, collapse character facing into a frontal sprite, or shrink the verified camera-follow world back into a one-screen test room merely because an older document lacks the newer rule.

### Current Director P0 integration gates

Until explicitly verified in a device playtest, the following are P0 integration gates:
1. player/NPC/monster remain visible after world/camera changes;
2. character field presentation faces `NW / NE / SW / SE` side-diagonal directions rather than front-facing;
3. empty eligible map taps walk the character to the tapped world target with camera/collision correctness;
4. monster defeat results in an observable, exactly-once direct inventory grant when an evidence-backed reward is resolved;
5. Milles/start-region world extent continues expanding without breaking traversal, targeting, NPC interaction or combat.

The Integrator is the final Design Compliance Gate. A compiling implementation that violates this constitution is a failed integration.