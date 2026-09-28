# PROJECT DARK contributor gate

Operational revision: LOOP-4-V1, approved by the user after the project/automation audit.
Four roles: Visual Runtime, World Runtime, Game Systems, Director/Integration.
Read docs/DIRECTOR_GUIDE.md and docs/DIRECTOR_BACKLOG.md plus current user canon and the relevant handoff/PR delta. Initially read constitution/data contract/source registry/Master manifest; do not repeat a full historical audit every run.

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
