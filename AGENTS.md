# PROJECT DARK contributor gate

Operational revision: LOOP-4-V1, approved by the user after the project/automation audit.
Four roles: Visual Runtime, World Runtime, Game Systems, Director/Integration.
Read docs/DIRECTOR_GUIDE.md and docs/DIRECTOR_BACKLOG.md plus current user canon and the relevant handoff/PR delta. Initially read constitution/data contract/source registry/Master manifest; do not repeat a full historical audit every run. For monster creation, asset replacement, or monster runtime behavior, also read docs/MONSTER_CREATION_MANUAL.md and update its acceptance record when the contract changes.

The shared goal is one playable loop: Milles movement → NPC dialogue → one monster combat → direct inventory reward → progression → save → process restart and restore. Work on independent parts of this loop may proceed while village visual QA remains pending. The former THREE-20260910 role assignment and blanket M1/M2-before-gameplay rule are superseded.

Visual owns assets/renderers/frame/anchor. World owns map/grid/movement/collision/camera/depth/portal. Game owns NPC/combat/reward/inventory/equipment/progression/quest/save rules. Director owns GameView/MainActivity orchestration, integration, exact-SHA verification/APK, scheduling health and backlog. RuntimeState rule changes belong to Game; Director coordinates any cross-cutting change with a single assigned editor. Preserve modules; do not duplicate domain logic in GameView.

One accepted task per role, one active branch/PR per role. Continue valid unfinished work after checking latest main; do not proliferate branches or redo completed work. Only Director advances main after applicable checks. No force push. Staggered schedules do not prevent overlap.

Director resolves repeated blockers after two runs, records handoff and pause/resume conditions, and checks actual scheduling state. Idle workers need no artificial commits. Existing worker reservations may rest after a documented handoff and be resumed for a concrete task; do not override a user's explicit pause. Director remains responsible for monitoring.

Preserve approved peasant appearance and current movement unless evidence requires a fix. Official Nexon evidence and living Master updates remain Director responsibility. Preserve source bytes and provenance. No ground-drop/pickup; unresolved original facts remain unresolved or explicit B/ADAPTED fixtures.

Report IMPLEMENTED / BUILD VERIFIED / RUNTIME VERIFIED / VISUAL ACCEPTED separately. APK delivery includes exact SHA, build time in Asia/Seoul, and tested scope. Operational guidance updates are not game implementation or runtime verification.

## Cross-chat handoff maintenance
- Start from the current `main` and read `docs/CHAT_HANDOFF.md`, `docs/PROJECT_STATE.md`, `docs/DECISION_LOG.md`, `docs/DIRECTOR_BACKLOG.md`, then the applicable design/source contract and handoff.
- Before editing any existing file, fetch and read its current contents on the latest base; never fill unknowns by inference.
- At the end of each completed task, update the project state, the affected backlog row and any changed decision or implementation contract with exact evidence (SHA, CI run, device scope). Keep approved decisions, implementation facts, proposals, and unresolved conflicts distinct.
- Treat dated execution reports and automation batons as historical unless the current SHA and live evidence confirm them. Documentation maintenance alone is not runtime implementation or verification.

- Before delivering an APK, prove it came from the exact intended source SHA and identify the exact Actions run/artifact and APK SHA-256. A successful CI build proves build status only, not that the requested feature is reachable or works on device.
- When a user reports a delivered APK failure, record the report as DEVICE_FAILED (user-reported), keep root cause UNKNOWN until reproduced, and make diagnosis/reproduction the immediate handoff task. Do not send a newly built APK as a fix unless the runtime change and requested device scenario have been verified; if device verification is unavailable, label it pending.

- APK source selection follows the active task branch. Compare it with main before building; label an unmerged/diverged branch artifact as a candidate, never as the current main APK.

## Required hunting-map performance baseline
Before creating or changing any hunting map, read `docs/HUNT_MAP_PERFORMANCE_LESSONS.md` and retain its static-cache/live-collision/prewarm/vsync/save and memory regression gates. User reported forest speed resolved on 2026-10-09; do not regress the accepted experience.

## Change-scoped verification — user decision 2026-10-21
- Before running checks, state the changed behavior, affected files/shared dependencies, the smallest meaningful test set, and why each adjacent subsystem is included. A large file such as GameView changing does not by itself require every suite.
- Default to requested-feature tests plus directly affected integration/regression paths. EXP changes include reward accounting and applicable save migration; equipment rendering includes the changed idle/action layers. They do not automatically require every growth quest, skill, job or map scenario.
- Run full regression for final integration into main, broad shared-foundation changes (save schema, shared combat/progression rules, global movement/render loop), or an observed failure showing wider impact. Record the concrete trigger; use a bounded affected suite first for diagnosis and iteration.
- Deliver an explicitly scoped candidate APK after its applicable build/runtime/artifact checks pass; unrelated full regression is not a blanket prerequisite. Integration remains gated by its required checks. Do not call focused success full-suite success or claim phone/visual acceptance from host tests.
- Reuse valid evidence for the same source and relevant scope. Do not intentionally schedule duplicate full push and PR checks for the same source/environment. Repeat or broaden checks only for new changes, failures or unresolved concerns; never disable meaningful assertions to save time.
- Documentation-only changes need content/link/diff checks, not game regression or a new APK. Preserve exact source/run/artifact/hash proof for APK delivery.
- CI configuration and this policy are separate: never claim a documentation edit optimized CI. V216 implements and remotely verifies one exact reviewed-blob scope profile and removes common feature-push/PR full duplication (scope run39604868913; focused39604859782). Unreviewed runtime bytes fall back to full CI. Select/review a suitable profile for each future task; the V216 profile is not blanket permission to skip other affected tests. Final main integration still requires manual full verification or the full-regression PR label.
- This section narrows historical blanket verification language in handoffs/guides; prior test reports remain historical evidence, not a requirement to repeat their entire scope.

## Required magic FX lessons
Before creating or replacing magic effects, read `docs/MAGIC_FX_LESSONS.md`. Preserve complete original frames, per-frame anchors and blending; verify source pixels, multi-tile bounds and actual single/screen casts. User explicitly requires these V241/V242 lessons in later spell work.

## User-retired obsolete verification — 2026-10-24
The user confirmed the13previously reported failed groups had already been reviewed against the current baseline and explicitly ordered them discarded. `verification/V268_OBSOLETE_CHECKS_RETIRED.json` is the closed retirement record. Do not reopen those groups/cases as outstanding debt, regenerate their old assumptions, or restore them without a new user request. Their historical failures are not relabelled PASS. Keep current valid checks; new requested changes receive current-contract verification. Approved V266 runtime/assets and main integration remain the baseline.
