# PROJECT DARK contributor gate

Active scheduled roles: World, Character, Director/Integrator (THREE-20260910).
Read docs/DIRECTOR_GUIDE.md and docs/DIRECTOR_BACKLOG.md, latest user playtest canon and relevant handoff/active PR before changes. Initially read constitution/data contract/source registry/Master manifest/reconciliation; subsequent runs read changes and affected data. Do not repeat a full history/Master audit every run.

The latest user request reduces coordination to three roles and prioritizes visible, wired Android progress. The current presentation/movement amendment is design/PLAYTEST_CANON_20260910_2149.md; later explicit user decisions supersede it. Older 4/5-worker assignments and stale scale/ground-pickup instructions are historical.

World owns world/movement. Character owns sprite/render/motion. Director owns runtime wiring, current-loop UX/Combat/RPG/save integration fixes, CI/APK and next assignments. Director must implement integration fixes, not only review. Preserve domain APIs and working code; do not duplicate internals in GameView.

Reuse one active unmerged branch/PR per role; no per-run branch proliferation or stacked PR chains. Only Director advances main after applicable verification. No force push. Runs may overlap.

One visible result per role per run: code → runtime wiring → focused verification. Missing dependencies receive exact API/caller handoff; do not repeatedly report an unconnected implementation as complete. Official Nexon evidence and living Master updates remain Director responsibility with provenance and source-byte preservation.

Report PLANNED, IMPLEMENTED, BUILD VERIFIED, RUNTIME VERIFIED separately. Compile is not APK/runtime proof. Only actual Android screenshots/logs support runtime claims. APK delivery includes build time in Asia/Seoul and source SHA.
