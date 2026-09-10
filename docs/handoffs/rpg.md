# RPG Agent Handoff

Run: `20260910-1753`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 32 — five-job circle-1 skill book + post-selection visibility

Direction: make the first-job choice immediately change the visible skill-book surface for all five basic jobs without fabricating unresolved acquisition rules.

### Source-of-Truth gate
Re-read:
- `master/data/Skill_Master.csv`
- `master/data/Skill_Milestones.csv`
- `master/data/Skill_Requirements.csv`

Confirmed all current canonical circle-1 rows:
- WARRIOR: `SK_전사_001` 숏블레이드, `SK_전사_012` 레스큐
- ROGUE: `SK_도적_001` 찌르기, `SK_도적_002` 센스몬스터
- MAGE: `SK_마법사_001` 마레노
- CLERIC: `SK_성직자_001` 쿠로, `SK_성직자_002` 수혈, `SK_성직자_003` 디렌토
- MARTIAL_ARTIST: `SK_무도가_001` 정권

`Skill_Requirements.csv` still leaves most exact acquisition requirements unresolved. Catalog presence therefore remains separate from learned state.

### Implemented

1. Expanded `RpgActionMetadataCatalog` beyond Warrior with the remaining basic-job circle-1 canonical actions.

2. Preserved unresolved runtime values:
- `resourceCost = null` where Master does not resolve a number;
- `cooldown = null`;
- `iconKey = PENDING_CROP:<actionId>`.

3. Added `sourceEvidence` to `ActionMetadata` so composite canonical provenance such as `O/V` is retained instead of being collapsed into the single runtime `Evidence` enum.

4. Added `forJobAndCircle(rpg, jobCode, circle)` for stable skill-book filtering.

5. Expanded `RpgVisibleProgressionPresentation.Snapshot` with:
- `basicJobOptions`
- `currentCircleOneActions`

Before first-job selection, `currentCircleOneActions` is empty. After a successful `BasicJobSelectionService.select(...)`, refreshing the snapshot immediately returns the selected job's canonical circle-1 rows.

6. Acquisition remains evidence-safe:
- newly visible current-job actions are `CURRENT_JOB_LOCKED`;
- job selection never auto-learns them;
- locked actions do not enter `quickSlotCandidates`;
- after explicit successful `setLearnedAction(actionId, true)`, the action becomes `LEARNED` and an eligible active action can enter the quick-slot candidate list.

7. Added `RpgBasicJobCircleOneAudit` covering:
- circle-1 counts `WARRIOR=2 / ROGUE=2 / MAGE=1 / CLERIC=3 / MARTIAL_ARTIST=1`;
- composite evidence preservation;
- unresolved martial resource label preservation;
- Commoner Lv10 → ROGUE selection;
- post-selection circle-1 rows become current-job locked;
- no implicit quick-slot unlock;
- explicit learning promotes `SK_도적_001` to learned quick-slot candidate.

### Commits this pass
- `b8435cdf717f35ed000d4c7a2029d7fda1b49040` — five-job circle-1 canonical action projection
- `76080b028454ecff29d681b57a90d1d7c35c0df8` — circle-1/post-selection regression audit
- `afe49da15053df3a38ca73d65ae68761ff1ff89b` — expose job options and current circle-1 book in single visible snapshot
- `38f45d120c6908eef017f7bd11ae78ea0de2ac8c` — PASS 32 development history

### Integration contract
Integrator/UX should consume one object:
`new RpgVisibleProgressionPresentation().snapshot(state.rpg())`

Job UI:
- five choices: `snapshot.basicJobOptions`
- selected job: `snapshot.player.jobCode`
- current starter/circle-1 panel: `snapshot.currentCircleOneActions`

Skill rows:
- name/icon: `action.name / action.iconKey`
- class/target/range/effect: typed metadata fields
- acquired state: `action.visibilityState` or `learnedLabel(action)`
- unresolved cost/cooldown: render PENDING, never zero
- provenance/debug: `action.sourceEvidence`

Quick slots:
- consume `snapshot.quickSlotCandidates` only;
- never place `CURRENT_JOB_LOCKED` actions into an active slot.

### Existing progression retained
- full Lv1→99 cumulative EXP curve with `[B]` provenance;
- live EXP mutation and normalized HUD ratio;
- Lv99 cap;
- Commoner → five basic-job selection service with explicit external gate proof;
- direct-inventory reward outcomes/feed;
- reward-sequence idempotency;
- schema-v1 save/restore including selected job and learned action IDs.

### Evidence boundary
Circle/name/action-class/target/range/effect are projected from the canonical Skill Master. Exact acquisition requirements, numeric costs and cooldowns remain unresolved where the Master does not resolve them. No automatic starter-skill grant is permitted yet.

### Ownership preserved
No `GameView.java`, combat effect execution, MonsterAI, map/world, character renderer, NPC/dialog rendering, workflow, APK packaging, or main merge changes.

## Next RPG P0
1. Introduce a typed `learnAction` outcome contract (`LEARNED / ALREADY_LEARNED / WRONG_JOB / REQUIREMENT_PENDING / UNKNOWN_ACTION`) rather than relying on the low-level boolean setter.
2. Resolve evidence-safe starter acquisition cases individually where official/verified rules support them; leave the rest pending.
3. Expand 2-circle metadata for the selected-job vertical slice, prioritizing currently playable progression rather than projecting the entire catalog blindly.
4. Integrator should wire `basicJobOptions` + `currentCircleOneActions` into the live NPC/job/skill UI and build the integrated APK.
