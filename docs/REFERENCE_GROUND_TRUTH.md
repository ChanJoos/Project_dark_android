# PROJECT DARK — Reference Ground Truth

Status: CANONICAL VISUAL / MOTION REFERENCE
Owner: Integration Director

## Canonical video references
The following two user-supplied videos are the highest-priority visual/motion truth for the current vertical slice:

1. `Screen_Recording_20260915_003420_YouTube.mp4` — Milles village reference.
2. `Screen_Recording_20260915_003701_YouTube.mp4` — combat / player-monster interaction reference.

These files live in the project workspace supplied by the user. When the runtime has direct access to them, inspect them frame-by-frame. When a worker cannot access the binary video, consume the derived frame-analysis documents in `docs/reference/` and do not invent missing source behavior.

## Precedence
For visible presentation and motion, precedence is:

1. User-supplied canonical reference videos.
2. Frame measurements derived from those videos in `docs/reference/*_FRAME_ANALYSIS.md`.
3. Current user-confirmed device feedback / latest playtest evidence.
4. Source-backed official / archival assets and documentation.
5. Project design docs.
6. Adapted implementation decisions explicitly labelled `[ADAPTED]`.

If any older prompt, handoff, heuristic offset, procedural visual, or previous implementation conflicts with the videos, the video reference wins for visible behavior.

## Required workflow for every visual/runtime change
1. REFERENCE: identify the exact video scene / frame range relevant to the change.
2. MEASURE: write down observable geometry, timing, scale, direction, pose, spacing, camera or composition facts.
3. COMPARE: compare the current runtime against the reference. Do not start from arbitrary tuning values.
4. IMPLEMENT: make the smallest coherent code / asset change that reduces the measured delta.
5. VERIFY: compare the resulting runtime against the same reference scene / measurements.
6. REPORT: classify results as IMPLEMENTED / INTEGRATED / BUILD VERIFIED / DEVICE VERIFIED / VISUAL ACCEPTED.

## Milles ground rules
- The village must read as broad grass terrain with clear diagonal path structure, central landmark/plaza, grouped vegetation/fences, water-side landmarks and readable outer buildings.
- Do not recreate Milles as a dense test board of repeated brown tiles with arbitrary props scattered around spawn.
- Player/world/building scale, camera framing, landmark spacing and path rhythm should be judged against the Milles video, not against prior procedural values.
- Unknown off-camera portions of the town are `UNRESOLVED`; do not call invented layout source-accurate.

## Combat ground rules
- Melee legality remains the canonical 64×32 adjacent-tile contract already established in runtime.
- Attack presentation must be judged against the combat video frame sequence: startup → contact → recovery, body silhouette change, weapon travel, target reaction/effect timing and relative spacing.
- Do not treat whole-character forward/back translation as source-accurate just because it creates three phases.
- If source temporal sprite frames are unavailable, procedural motion must remain `[ADAPTED]` and be compared to the video before acceptance.
- Player/monster relative scale, chase cadence, facing changes and attack interval should be measured from the reference whenever observable.

## Anti-forgetting rule
Every scheduled PROJECT DARK worker and the Director must read this file FIRST on every run, then the relevant `docs/reference/*_FRAME_ANALYSIS.md`, then `docs/CURRENT_STRUCTURAL_CANON.md` if present, before modifying code.

The repository, not chat memory, is the long-term project memory.