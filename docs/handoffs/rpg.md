# RPG Agent Handoff

Run: `20260910-1822`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 33 — typed action learning + persistence/quick-slot promotion

Direction: replace the low-level boolean learned-action mutation as the integration contract with an evidence-safe typed acquisition service.

### Implemented

1. Added `RpgActionLearningService`.

Typed requirement proof:
- `PENDING`
- `SATISFIED`

Typed learning outcomes:
- `LEARNED`
- `ALREADY_LEARNED`
- `UNKNOWN_ACTION`
- `RUNTIME_PROTOTYPE`
- `WRONG_JOB`
- `REQUIREMENT_PENDING`
- `MUTATION_FAILED`

2. Learning rules are fail-closed:
- unknown action IDs do not mutate state;
- prototype CAST/SKILL/KICK bindings are not learnable canonical actions;
- other-job actions are rejected;
- current-job actions with unresolved acquisition requirements return `REQUIREMENT_PENDING` unless the owning quest/NPC/progression flow supplies `RequirementProof.SATISFIED`;
- no level/stat/item/Gold cost is fabricated from missing Master fields.

3. Successful `LEARNED` delegates to the existing persisted learned-action set.
- learned state is therefore included in schema-v1 save/restore;
- after learning an active `quickSlotEligible` action, `RpgActionMetadataCatalog.quickSlotCandidates(rpg)` immediately includes it;
- a second request returns `ALREADY_LEARNED` without duplicate mutation.

4. `LearnOutcome` exposes integration metadata:
- actionId / actionName
- action job / player job
- mutated flag
- message
- runtime evidence
- original `sourceEvidence` provenance

5. Added `RpgActionLearningAudit` covering:
- select ROGUE at Lv10 through the existing evidence-safe job gate;
- WARRIOR skill on ROGUE → `WRONG_JOB`;
- ROGUE skill with pending proof → `REQUIREMENT_PENDING` and no mutation;
- satisfied proof → `LEARNED`;
- learned active skill becomes a quick-slot candidate;
- repeated learn → `ALREADY_LEARNED`;
- learned state survives save → restore;
- unknown action remains non-mutating.

### Commits this pass
- `a298ce397af1e3b6bf92367cfe31cbd0f58294c5` — typed evidence-safe action learning service
- `bbc20d0811f1aae0426633516d134ac3d75b7143` — action learning / persistence / quick-slot audit

### Integration contract
Owning NPC/quest/progression flow should call:
`RpgActionLearningService.learn(state.rpg(), actionId, proof)`

Rules:
- do not call `setLearnedAction()` directly from UI integration;
- do not synthesize `SATISFIED` from catalog presence, level alone, or a displayed button;
- only provide `SATISFIED` after the relevant acquisition flow has actually resolved the requirement;
- after `LEARNED`, refresh `RpgVisibleProgressionPresentation.snapshot(state.rpg())`;
- render learned state from action metadata and active slots from `snapshot.quickSlotCandidates`.

### Existing progression retained
- full Lv1→99 cumulative EXP model / Lv99 cap / HUD ratio;
- evidence-safe Commoner → five basic-job selection;
- five-job circle-1 skill book;
- direct-inventory reward outcome feed and duplicate suppression;
- schema-v1 save/restore for job, EXP, inventory/equipment and learned action IDs.

### Evidence boundary
`Skill_Requirements.csv` still leaves most exact acquisition costs/requirements unresolved. This service intentionally requires an external proof token rather than inventing missing rules.

### Ownership preserved
No `GameView.java`, combat effect execution, MonsterAI, map/world, renderer, NPC/dialog rendering, workflow, APK packaging, or main merge changes.

## Next RPG P0
1. Resolve individual starter-skill acquisition cases where stronger O/V evidence is available and replace external proof with internal deterministic checks only for those cases.
2. Project selected-job 2-circle metadata for the playable vertical slice, not the entire catalog blindly.
3. Add typed action-forget/replacement handling only where canonical upgrade chains require replacement (e.g. verified skill supersession), preserving save migration.
4. Integrator should wire the learning outcome + quick-slot refresh into the live NPC/skill UI and run compile/APK/runtime validation.
