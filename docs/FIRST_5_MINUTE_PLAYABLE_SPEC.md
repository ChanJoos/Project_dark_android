# PROJECT DARK — FIRST 5 MINUTE PLAYABLE SPEC

Status: ACTIVE PRODUCT SPEC
Purpose: Single source of truth for the first complete playable RPG loop in Milles.

## 0. Product Goal
A new PRE_CLASS / PEASANT character must experience one coherent RPG loop without debug intervention:

Milles spawn → understand HUD → discover NPC → approach/dialogue → accept quest → Quick Quest → navigate toward objective → find/target monster → combat → damage/HP feedback → monster defeat → monster EXP + direct auto-loot → EXP bar changes → level-up when threshold is crossed → stats/derived combat power change → inventory/equipment/status inspection → return navigation → NPC turn-in → quest reward EXP/currency/item → completed/history state → save → process restart → exact state restored without duplicate rewards.

Attack presentation defects already classified KNOWN_VISUAL_FAIL remain frozen unless they block this loop.

## 1. Governance
This file owns product behavior, dependencies, acceptance criteria and feature ownership. Agents must not replace source-backed original facts with invented values.

Provenance: SOURCE_BACKED | USER_CONFIRMED | ADAPTED | ADAPTED_BALANCE | UNRESOLVED
Status: UNRESOLVED | READY | IN_PROGRESS | IMPLEMENTED | INTEGRATED | VERIFIED | BLOCKED
QA severity: BLOCKER | MAJOR | POLISH

A feature is not VERIFIED merely because it compiles. Integrator must verify its acceptance path. DEVICE_VERIFIED is reserved for explicit device evidence.

## 2. Agent Ownership
DIRECTOR: source/canon audit, story/content/balance decisions, dependency ordering, acceptance gates.
GAMEPLAY: quest/progression/stats/combat/reward/inventory/equipment/save domain state and contracts.
WORLD_UX: NPC/world interaction, dialogue, HUD, Quick Quest/navigation, menus/status/equipment/inventory/quest presentation.
INTEGRATOR: coherent integration, regression, acceptance-path verification, severity classification. No overnight APK packaging.

Features can have one Owner and multiple Collaborators. Feature ownership is authoritative over file ownership.

## 3. Content Sheet — must be resolved before pretending values are canonical
Director must resolve from MASTER/design/source evidence where possible:
- opening NPC: NPC_ID, exact name, role, source status, sprite/animation asset IDs, Milles coordinate/facing.
- opening monster: Monster_ID, exact name, sprite, level, HP, attack/defense, EXP, spawn/hunt area, reward table.
- opening quest: Quest_ID/title/story beats/start/end NPC/dialogue/objective/reward/next-purpose/journal record.
- progression: initial level/stats/HP/MP, required EXP table, monster EXP, quest EXP, level-up stat rules.
- combat: authoritative or adapted derived-stat/damage rules.
- reward item: Item_ID/name/category/equipment slot/stats/appearance asset if source exists.
- UI layout: safe-screen placement for Lv/HP/MP/EXP, Quick Quest, target HP, action buttons and menu entry points.

Q_MIL_01 or any source quest whose level/prerequisites do not match a new character must not be silently converted into an opening quest. If no valid source-backed opening exists, create an explicitly ADAPTED prologue fixture and keep source facts separate.

## 4. Feature Graph / Work Packages

### F5M-001 Canon + Opening Narrative
Owner: DIRECTOR. Collaborators: GAMEPLAY, WORLD_UX. QA: INTEGRATOR.
Define the actual first-five-minute story, NPC, monster, quest and reward using the Content Sheet. Story must explain enough context to give the player a purpose, not dump lore. Define dialogue nodes for AVAILABLE, OFFER, DECLINED/REOPEN, ACTIVE, RETURN_READY and COMPLETED. Define the next-purpose hook after completion.
Acceptance: every proper noun/value has provenance; no inappropriate source quest is repurposed; narrative/objective/world target/reward form one coherent loop.

### F5M-002 Player Start + HUD Baseline
Owner: WORLD_UX. Collaborator: GAMEPLAY.
Start as PRE_CLASS/PEASANT on a valid Milles traversable tile. Camera and existing movement remain intact. HUD must expose player Level and HP; MP if current canonical runtime supports it; EXP bar must be persistently readable without opening a menu. EXP bar has current/required EXP detail. Existing ATTACK/SKILL/MAGIC controls must not regress.
Acceptance: fresh start needs no debug action; player can see level/health/EXP and reach first NPC; no HUD overlap with core controls.

### F5M-003 NPC Character Asset Package
Owner: WORLD_UX. Approver: DIRECTOR.
Resolve source NPC sprite before production use. Register sprite/frame semantics, world scale, foot anchor, facing/idle behavior, hitbox and nameplate. Do not recolor player art and call it canonical NPC art. Missing source art remains PENDING/ADAPTED prototype with provenance.
Acceptance: NPC identity and visual asset mapping are explicit; rendering aligns to world ground plane; name is readable.

### F5M-004 NPC World Entity + Quest Marker
Owner: WORLD_UX. Collaborator: GAMEPLAY.
NPC world state includes stable ID, position, facing, collision/interaction radius, dialogue binding and quest-provider binding. Marker policy: ! = available quest, none = active/no turn-in, ? = return-ready, normal nameplate after completion unless next quest requires another marker.
Acceptance: marker derives from actual quest state, not duplicated UI state.

### F5M-005 NPC Tap / Approach / Facing
Owner: WORLD_UX.
Tap NPC → select → path to legal adjacent interaction tile if distant → stop → face NPC → open dialogue. Never teleport. Manual movement cancels pending auto-approach. Dialogue cannot open through invalid collision/path.
Acceptance: near/far tap paths work; blocked path fails safely; player retains control after cancellation/dialogue end.

### F5M-006 Dialogue Runtime + Mobile Dialogue UI
Owner: WORLD_UX. Collaborator: GAMEPLAY.
State-driven dialogue nodes. Mobile panel includes NPC name, text, progression affordance and explicit quest accept/decline when appropriate. Outside tap must not accidentally accept/complete. Back/close behavior is defined. Movement/combat input is suppressed or safely routed while modal dialogue is active.
Acceptance: AVAILABLE/ACTIVE/RETURN_READY/COMPLETED produce correct dialogue and no duplicate acceptance/turn-in.

### F5M-007 Quest Data + State Machine
Owner: GAMEPLAY. Approver: DIRECTOR.
Quest record includes provenance, prerequisite, start/end NPC, objective type/target/count, hunt/location target, dialogue IDs, monster/turn-in rewards, next purpose and journal metadata. Lifecycle at minimum: LOCKED/AVAILABLE → ACTIVE → OBJECTIVE_COMPLETE/RETURN_READY → COMPLETED. Decline must leave it available where applicable.
Acceptance: lifecycle transitions are deterministic/idempotent and queryable by UI.

### F5M-008 Quest Offer / Accept Transaction
Owner: GAMEPLAY. Collaborator: WORLD_UX.
Dialogue offer displays title, concise story/objective and reward preview. Accept atomically activates quest and creates tracker state. Decline policy and re-offer behavior defined. Tutorial-required quest abandonment policy explicitly defined.
Acceptance: UI and runtime agree immediately after accept/decline; re-entry does not duplicate quest state.

### F5M-009 Quick Quest Tracker
Owner: WORLD_UX. Collaborator: GAMEPLAY.
Compact HUD tracker shows quest title, current objective and count. Body tap opens Quest Detail. Separate navigation affordance (>) routes toward objective. On objective completion tracker changes to return-NPC instruction. Multiple-quest priority policy must exist even if slice has one quest.
Acceptance: accept instantly shows tracker; progress is live; RETURN_READY changes text/action; completion removes/advances tracker.

### F5M-010 Quest Detail + Quest Menu
Owner: WORLD_UX. Collaborators: GAMEPLAY, DIRECTOR.
Menu provides at least Active and Completed; Journal/history integration is represented without inventing unsupported original chronology. Detail includes narrative summary, current objective/progress, rewards and destination action. Abandon action only if policy allows it.
Acceptance: detail reads runtime state, not hard-coded copies; completed quest moves out of Active.

### F5M-011 Objective Navigation
Owner: WORLD_UX. Collaborator: GAMEPLAY.
Navigation service handles NPC, location/portal and hunting-area targets. Quest navigation may move to the hunt area but must not auto-select/auto-kill the target unless future design explicitly enables auto combat. Manual world input cancels navigation. Off-screen objective indication is defined when useful.
Acceptance: Quick Quest/detail navigation reaches a legal objective vicinity and cancellation works.

### F5M-012 Monster Content + World Binding
Owner: GAMEPLAY for data/runtime; WORLD_UX for world presentation/placement; DIRECTOR approves provenance/balance.
Quest target ID must bind to the actual spawned combat target. Define level, HP, attack/defense, EXP, spawn/respawn/aggro and reward eligibility. Opening balance must be intentionally survivable and finishable within the five-minute slice.
Acceptance: objective target and killable world entity share authoritative identity; no fake quest-only kill counter.

### F5M-013 Combat Target HUD + Player HP Feedback
Owner: WORLD_UX. Collaborator: GAMEPLAY.
Selected monster exposes name and HP bar; level if supported/meaningful. Damage feedback reflects actual resolved damage. Player HUD HP changes from actual combat state. Do not reopen frozen attack-animation polish.
Acceptance: player can understand target health, own health and damage outcomes.

### F5M-014 Monster Defeat → Quest Objective
Owner: GAMEPLAY.
Reuse existing MONSTER_DEFEATED ledger/event. Active objective checks authoritative Monster_ID and increments once. Required count transition produces RETURN_READY. Duplicate/replayed defeat event must not increment twice.
Acceptance: only qualifying real kills progress objective; 0/1→1/1 is deterministic and idempotent.

### F5M-015 Monster EXP + EXP Model
Owner: GAMEPLAY. Collaborator: DIRECTOR for source/balance.
One progression service receives monster EXP and quest EXP. Define required EXP by level with provenance. Overflow supports multiple level-ups. EXP state is authoritative and observable by HUD/status UI.
Acceptance: kill changes EXP exactly once; current/required values remain valid at thresholds and overflow.

### F5M-016 Level Up Transaction
Owner: GAMEPLAY. Collaborator: WORLD_UX.
Threshold crossing performs Level+1, base-stat progression, max-resource update policy, derived-stat recomputation and event emission atomically. WORLD_UX updates Lv/EXP and presents a non-conflicting LEVEL UP feedback sequence.
Acceptance: level-up is persistent, idempotent and visible; large reward can cross multiple levels safely.

### F5M-017 Base Stats + Derived Stats
Owner: GAMEPLAY. Approver: DIRECTOR.
Use source-backed stat model/formulas when available; otherwise mark adapted balance. Separate base stats, equipment modifiers and derived stats. At minimum combat-relevant Attack/Defense and HP/resource derivations must have one source of truth.
Acceptance: changing level/base stat/equipment recomputes derived values; UI and combat read the same values.

### F5M-018 Damage Scaling Proof
Owner: GAMEPLAY. QA: INTEGRATOR.
Combat damage must consume derived combat stats rather than a disconnected display value. Define deterministic audit case: before level/equipment change vs after change against equivalent defense state.
Acceptance: legitimate stat increase changes resolved combat outcome according to formula; Character Sheet attack value is not decorative.

### F5M-019 Character / Status Screen
Owner: WORLD_UX. Collaborator: GAMEPLAY.
Expose character identity, Level/class-state, HP/MP where applicable, base stats, derived combat stats and EXP current/required. Entry/close/back behavior defined. Values are live snapshots from progression/combat state.
Acceptance: displayed level/EXP/stats match runtime and refresh after level/equipment changes.

### F5M-020 Inventory
Owner: GAMEPLAY for inventory domain; WORLD_UX for presentation.
Existing direct auto-loot remains canonical for the slice. Inventory displays item identity, quantity/category and details. Equipment-capable item exposes equip action if eligible. No ground-drop duplication.
Acceptance: monster/quest reward appears exactly once and survives reopen/restart.

### F5M-021 Equipment Screen + Equip Transaction
Owner: GAMEPLAY for equipment/state; WORLD_UX for screen/paper-doll presentation.
Use canonical equipment slots supported by data. Equip/unequip atomically moves authoritative equipment state and recalculates derived stats. If a source appearance asset exists, renderer consumes it; missing appearance is not fabricated. Show equipment details and stat effect.
Acceptance: inventory→equip→status change→combat change is coherent; equipped state persists.

### F5M-022 Auto Loot + Reward Feedback
Owner: GAMEPLAY. Collaborator: WORLD_UX.
Existing direct inventory reward pipeline remains single source. Kill produces concise loot/EXP feedback based on actually granted values. Reward event idempotency is mandatory.
Acceptance: one kill = one eligible grant; UI never claims an item that inventory did not receive.

### F5M-023 Return Ready UX
Owner: WORLD_UX. Collaborator: GAMEPLAY.
On objective completion: objective-complete feedback, Quick Quest changes to return instruction, NPC marker changes to ?, navigation target changes to end NPC. Arrival does not silently turn in; player performs NPC interaction.
Acceptance: all three surfaces derive from RETURN_READY and stay consistent.

### F5M-024 Quest Turn-In + Completion Reward
Owner: GAMEPLAY. Collaborators: WORLD_UX, DIRECTOR.
NPC interaction in RETURN_READY enters completion dialogue. Confirmed turn-in atomically grants completion EXP/currency/item through existing reward/progression/inventory services, marks COMPLETED, records claimed reward identity and emits completion presentation event. Quest EXP may trigger level-up through the same progression service.
Acceptance: reward exactly once; repeated NPC interaction cannot re-grant; completion state and rewards agree.

### F5M-025 Completion Presentation + Next Purpose
Owner: WORLD_UX. Collaborator: DIRECTOR.
Show quest-complete result with actual EXP/currency/items. Resolve ordering with simultaneous LEVEL UP so modals do not overlap or swallow input. Remove/advance tracker, update NPC marker/dialogue, expose next destination/quest hook.
Acceptance: player understands what was earned and what to do next.

### F5M-026 Completed Quest + Journal/History
Owner: GAMEPLAY for state; WORLD_UX for presentation; DIRECTOR for source framing.
Completed quest moves to completed/history. Preserve original diary/journal concepts only to the degree supported by source; unsupported dates/lore remain unresolved/adapted.
Acceptance: completed state remains inspectable and is not shown as active.

### F5M-027 Save Model
Owner: GAMEPLAY.
Persist at minimum player position where appropriate, Level, EXP, base progression state, HP/MP policy, inventory, equipment, quest lifecycle/objective progress, completed quests and claimed one-time reward IDs. Derived stats should preferably recompute from authoritative base/equipment state rather than become a competing source of truth.
Acceptance: schema/version/error policy exists; save operation cannot create reward duplication.

### F5M-028 Restart / Restore Matrix
Owner: INTEGRATOR with GAMEPLAY support.
Verify process restart after: fresh start; ACTIVE 0/1; RETURN_READY 1/1; monster reward granted; equipment equipped; level-up; COMPLETED. Confirm no duplicated EXP/items/rewards and renderer/UI reflect restored state.
Acceptance: each checkpoint restores coherent runtime + UI state.

### F5M-029 Five-Minute End-to-End Regression
Owner: INTEGRATOR. All agents support blockers.
Scripted path: spawn → inspect Lv/HP/EXP → NPC ! → approach/dialogue → offer/reward preview → accept → tracker → navigation → target monster/HP → fight → player/monster damage → kill → monster EXP + loot → EXP bar → optional level-up → inventory/equipment/status inspection → prove stat-driven combat delta where achievable → RETURN_READY tracker + NPC ? → navigate/return → completion dialogue → quest reward → level-up if threshold → completed/history → save/restart/restore.
Acceptance: no debug intervention, no BLOCKER, no duplicate grants, no disconnected UI values. Compile/CI evidence is not device evidence.

## 5. Dependency Rules
F5M-001 gates content identity. F5M-007/008 gate quest-facing UI. F5M-012 gates real objective combat. F5M-015/017 gate level/stat presentation. F5M-020/021 gate equipment proof. F5M-024 gates completion/history. F5M-027 gates restore. Independent READY work may run in parallel, but agents must not bypass unresolved upstream identities/contracts.

## 6. Overnight Operating Rule
Each cycle: DIRECTOR reads this spec + execution log + latest main/PR evidence and assigns the smallest dependency-ready player-visible increment. GAMEPLAY and WORLD_UX implement their owned portion and append handoff evidence. INTEGRATOR verifies the current acceptance path and classifies failures. BLOCKER returns to next cycle; MAJOR/POLISH is logged and does not stop the five-minute loop unless it becomes blocking. VERIFIED work is not reopened without regression evidence.

No agent packages/releases an APK overnight. Final APK is built only after the user returns and the integrated state is reviewed.
