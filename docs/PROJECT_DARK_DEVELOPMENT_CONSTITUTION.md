# PROJECT DARK Development Constitution

## Mission
Turn existing PROJECT DARK Design, Master Data, source material, assets, references, and code into a coherent playable Android game.

**Design -> Master Data -> Source / Asset -> Runtime -> Playable Scene -> APK -> Device Evidence -> Acceptance**

## Core rules
1. Use existing authored material first. Inspect relevant design, master, source/audit, assets, reference evidence and runtime before invention.
2. Do not privilege a subsystem by default. Priority comes from current project evidence.
3. Master data is an implementation input, not archival documentation.
4. Playable scenes are the integration target; isolated classes/CSVs/renderers are not completion.
5. Reference evidence governs observable fidelity.
6. Use `[ADAPTED]` only after a real source gap is verified. Unsupported original facts remain `UNRESOLVED`.
7. Fresh device evidence overrides stale completion claims.
8. Keep `IMPLEMENTED`, `INTEGRATED`, `BUILD_VERIFIED`, `DEVICE_VERIFIED`, `VISUAL_ACCEPTED` distinct.
9. Each normal run should produce one meaningful end-to-end runtime delta whenever technically possible.
10. Avoid speculative micro-tuning without new evidence.
11. Respect work-package claims/leases in `docs/PROJECT_STATE.yaml`.
12. `PROJECT_STATE.yaml` is current project truth; `AUTONOMY_BATON.md` is immediate execution context only.
13. Do not hard-code permanent PR/branch/SHA/temporary bugs into agent behavior.
14. Repair or roll back bad prior work instead of stacking on it.
15. Optimize for game completion, not agent activity.

## Mandatory implementation-input gate
This gate applies to every autonomous agent and every work package.

1. After claiming a work package, read its `required_sources` in `docs/PROJECT_STATE.yaml` before implementation.
2. Every concrete required path must actually be loaded. Descriptive or wildcard entries must be resolved to the exact files used.
3. If a new canonical direction, design, reference, or master document materially governs the work, register it in the work package `required_sources` before coding.
4. A file merely mentioned in `AUTONOMY_BATON.md`, a PR body, commit message, or chat is **not** considered consumed.
5. The agent must record the exact governing inputs it consumed in the baton/state update.
6. If a mandatory governing source cannot be loaded, implementation must stop or be marked BLOCKED rather than silently improvising around it.
7. A runtime delta that contradicts an unread/unregistered governing source is invalid and must not be promoted to completion.

This prevents canonical project direction from becoming passive documentation. The control plane must route it into implementation.

## Work selection order
Use `docs/PROJECT_STATE.yaml` as the queue. Prefer:
1. runtime/build blocker;
2. fresh explicit user/device FAIL not awaiting re-validation;
3. existing Design/Master/Asset core content not wired;
4. missing link for a playable scene/core loop;
5. material source/reference fidelity gap;
6. content expansion;
7. polish.

## Required source discipline
Load relevant sources deeply, not the whole repository indiscriminately. Canonical indexes include `master/MASTER_MANIFEST.md`, relevant `master/data/`, design constitutions/contracts, playtest canon, source/audit tables, relevant assets/runtime, reference ground truth/frame analyses, and latest device evidence in `PROJECT_STATE.yaml`.

## Work package rule
A work package must be large enough for a visible/functional game delta and small enough for one autonomous run or short baton chain. Investigation/docs/audits alone are supporting work.

## Acceptance taxonomy
- `IMPLEMENTED`: code/data integration exists.
- `INTEGRATED`: present on current shared integration line.
- `BUILD_VERIFIED`: exact HEAD passed required build/CI.
- `DEVICE_VERIFIED`: exact build exercised on a real Android device.
- `VISUAL_ACCEPTED`: observable result meets current reference/visual acceptance.
Never infer a later state from an earlier one.

## Agent collaboration
A-E are peer Senior Game Developer + Acting Director agents, not permanent subsystem owners.

Each run must:
1. Sync repository/current integration line.
2. Read this Constitution, `PROJECT_STATE.yaml`, and `AUTONOMY_BATON.md`.
3. Verify previous result.
4. Claim one eligible READY package.
5. **Pass the Mandatory implementation-input gate and load the package's governing required sources.**
6. Implement one end-to-end runtime delta.
7. Test/build and commit appropriately.
8. Update affected work package/scene/evidence/claim state.
9. Write a minimal baton including the exact sources actually consumed.

Repository + PROJECT_STATE + BATON carry project memory, but PROJECT_STATE `required_sources` determines mandatory package inputs.