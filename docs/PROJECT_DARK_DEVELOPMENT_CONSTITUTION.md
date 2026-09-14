# PROJECT DARK Development Constitution

## Mission

Turn the existing PROJECT DARK design, master data, source material, assets, references, and code into a coherent playable Android game. The goal is not to produce more planning documents; it is to close the chain:

**Design -> Master Data -> Source / Asset -> Runtime -> Playable Scene -> APK -> Device Evidence -> Acceptance**

## Core rules

1. **Use existing authored material first.** Before inventing mechanics, data, animation, art, layout, or placeholders, inspect the relevant design, master data, source/audit material, assets, reference evidence, and current runtime.
2. **Do not privilege any subsystem by default.** World, character, combat, monsters, items, equipment, skills, magic, NPCs, quests, progression, save, and UX are all implementation domains. Priority comes from current project evidence, not from a permanent subsystem preference.
3. **Master data is an implementation input, not archival documentation.** Where practical, runtime definitions should be driven by or faithfully projected from existing master data instead of duplicated as unrelated prototype constants.
4. **Playable scenes are the integration target.** A system is not meaningfully complete merely because a class, CSV, renderer, or controller exists. Connect systems and content into actual player-visible gameplay loops and scenes.
5. **Reference evidence governs observable fidelity.** Original/source-backed images, videos, frame analyses, and user-provided device evidence are used to judge appearance, motion, timing, composition, and UX. Do not replace supported reference facts with invention.
6. **Use `[ADAPTED]` only after a real source gap is verified.** Unsupported original facts remain `UNRESOLVED`. Never present adapted/procedural work as source-authentic.
7. **Device evidence overrides stale completion claims for observable results.** A fresh device failure can demote a previously marked DONE item back to FAIL/READY.
8. **Build success is not visual or device acceptance.** Keep these states distinct: `IMPLEMENTED`, `INTEGRATED`, `BUILD_VERIFIED`, `DEVICE_VERIFIED`, `VISUAL_ACCEPTED`.
9. **Each run should produce one meaningful end-to-end runtime delta whenever technically possible.** Investigation, documentation, audits, CI runs, refactors, and commits are supporting work, not progress by themselves.
10. **Avoid speculative micro-tuning loops.** Do not repeatedly change anchors, offsets, timing, or pixels without new evidence. Work that is `PENDING_DEVICE` must not be repeatedly modified without new device/user evidence unless a clear deterministic defect is discovered.
11. **Do not duplicate active work.** Autonomous agents must claim a READY work package in `docs/PROJECT_STATE.yaml`. Respect an unexpired claim held by another agent. Expired claims may be recovered.
12. **Keep the control plane small.** Current project truth is recorded in `docs/PROJECT_STATE.yaml`; immediate execution context is recorded in `docs/AUTONOMY_BATON.md`. Do not create competing status documents unless required for evidence.
13. **Do not hard-code a permanent PR, branch, SHA, temporary bug, or current screenshot into agent instructions.** Discover the current integration line and repository state on every run.
14. **Repair bad prior work instead of stacking on it.** Verify the previous baton and current diff. Correct or roll back regressions before extending them.
15. **Optimize for game completion, not agent activity.** Choose work by its contribution to a coherent playable experience and by the strongest current evidence.

## Work selection order

Use `docs/PROJECT_STATE.yaml` as the queue. Prefer the highest-value unclaimed READY work package using this order as guidance:

1. Runtime/build blocker that prevents meaningful play or integration.
2. Fresh explicit user/device FAIL that is not already waiting for device re-validation.
3. Core Design/Master/Asset content that exists but is not wired into runtime.
4. Missing link required to complete a playable scene or core gameplay loop.
5. Source/reference fidelity gap that materially changes the player experience.
6. Content expansion.
7. Polish and optimization.

This order is guidance, not a permanent roadmap. Use dependencies and current evidence.

## Required source discipline

For the selected work package, load only the relevant sources deeply. Prefer existing canonical indexes before broad searching:

- `master/MASTER_MANIFEST.md`
- relevant files under `master/data/`
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- latest relevant `design/PLAYTEST_CANON_*`
- relevant source/audit/research tables
- relevant assets and runtime code
- `docs/REFERENCE_GROUND_TRUTH.md` and related frame analysis where applicable
- latest user/device evidence recorded in `docs/PROJECT_STATE.yaml`

Do not reread the entire repository on every run if `PROJECT_STATE` already points to the relevant sources. Expand the search only when the state is incomplete or contradictory.

## Work package rule

A good work package is large enough to create a visible/functional game delta but small enough for one autonomous run or a short baton chain.

Bad: `research monsters`.

Good: `wire an already-authored monster definition/asset into the relevant playable field scene through spawn -> movement -> combat -> death, without inventing unsupported stats`.

Bad: `analyze attack animation`.

Good: `connect the existing authored/source-backed attack action semantics to body/equipment/weapon runtime and verify the attack loop builds correctly`.

The examples are illustrative only; they do not make those subsystems permanent priorities.

## Acceptance taxonomy

- `IMPLEMENTED`: code/data integration exists.
- `INTEGRATED`: present on the current shared integration line.
- `BUILD_VERIFIED`: exact HEAD passed the required build/CI.
- `DEVICE_VERIFIED`: exact build was exercised on a real Android device.
- `VISUAL_ACCEPTED`: observable result meets current visual/reference acceptance.

Never infer a later state from an earlier one.

## Agent collaboration

All scheduled A-E agents are peers with the same role: **Senior Game Developer + Acting Director**. They are serial baton developers, not permanent subsystem owners.

Each run must:

1. Sync repository and current integration line.
2. Read this Constitution, `PROJECT_STATE.yaml`, and `AUTONOMY_BATON.md`.
3. Verify the previous result.
4. Claim one eligible READY work package using the claim/lease fields.
5. Load the referenced Design/Master/Source/Asset/Reference inputs.
6. Implement an end-to-end runtime delta.
7. Test/build as appropriate and commit to the current integration line.
8. Update the affected work package, scene/evidence state, and claim in `PROJECT_STATE.yaml`.
9. Write a minimal baton for the next agent.

The repository, state file, and baton—not the schedule prompt—carry project memory.