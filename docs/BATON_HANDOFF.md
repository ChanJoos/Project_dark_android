# PROJECT DARK — Serial Baton Handoff

This file is the rolling handoff for the interchangeable Full-stack Developer + Acting Director baton. Read it after `docs/REFERENCE_GROUND_TRUTH.md`, the two frame-analysis documents, and `docs/CURRENT_STRUCTURAL_CANON.md`. Newer committed facts override older entries here.

## Latest baton

- START_HEAD: `8c75a16a4acb11d294b9e8250040211b00fe7e71`
- END_HEAD: `f47095fdb12175aa3d5459f0549c52e2588f0c3c` (implementation + acceptance-gate head before this handoff commit)
- WHAT CHANGED: Milles terrain presentation advanced from V5 to `MILLES_VIDEO_REFERENCE_V6_NATURAL_PATH_SPLIT`. `ROAD` no longer shares the stone tile used by PLAZA/GATE; it now consumes existing source-derived `terrain/OBJ_ground_02.png`, while PLAZA/GATE retain `terrain/OBJ_stone_01.png`. Runtime acceptance was updated to enforce the split and the truthful source-status label.
- WHY VS REFERENCE: The canonical Milles video reads as grass-first terrain with branching natural/brown paths and localized stone landmarks. V5 painted ROAD/PLAZA/GATE all as stone, producing a hard checker/stone-road read. The repository currently exposes only four source terrain assets (`OBJ_ground_01`, `OBJ_ground_02`, `OBJ_stone_01`, `OBJ_stone_02`) and no dedicated dirt/path source. V6 therefore removes the known stone-road mismatch using the second source ground variant, but explicitly labels it `[ADAPTED]` rather than claiming it is the original dirt road.
- TESTS: Source-level runtime acceptance gate updated for V6 status, truthful `ROAD_SURFACE_STATUS`, ROAD→ground_02 and PLAZA/GATE→stone_01 mapping. Exact-head GitHub Actions run was not yet visible immediately after the commit; therefore BUILD VERIFIED remains NO for this baton until a later exact-head run passes.
- KNOWN RISK: `OBJ_ground_02` is source-derived but not proven to be the true brown dirt-path texture from the reference. Continuous river/bridge and stepped water-side shrine are still not reconstructed. Device visual comparison is required before accepting this surface split.

## Rolling roadmap

### DONE
- Canonical user reference videos captured as repository ground truth and frame-analysis documents.
- Exact 64×32 four-adjacent melee spatial contract and shared combat-facing semantics integrated in the corrective branch.
- Major robe SW/SE detachment regression structurally reduced via shared-atlas zero registration.
- Milles V5 landmark hierarchy: outer buildings, central fountain/plaza, clustered fenced vegetation, water-side landmark.
- Milles V6 surface-role split: natural-source ground variant for ROAD; stone restricted to PLAZA/GATE.

### ACTIVE
- **Milles reference fidelity.** Finish the remaining largest reference-visible gaps without inventing source authenticity: validate V6 road appearance on device; search for/derive a defensible true dirt-path source if available; reconstruct continuous water edge/river + bridge/shrine only from source-backed or clearly `[ADAPTED]` evidence.

### NEXT
- **Combat/attack/monster reference fidelity.** Replace the whole-paper-doll lunge read with the short pose/weapon-driven startup→contact→recovery presentation shown by the combat reference. Do not fabricate temporal source frames; group-02 remains `SINGLE_POSE_PLACEHOLDER` / `UNRESOLVED_SOURCE_SEQUENCE` unless real temporal frames are found.

### LATER
- Replace procedural/placeholder monster presentation with source-backed or clearly adapted sprite content.
- Playable UX/NPC/reward/inventory/equipment loop.
- Progression/save.
- Broader content expansion.

### BLOCKED
- VISUAL ACCEPTED for Milles/attack requires a fresh device APK recording from the user.
- True source dirt-path asset is currently unresolved in the checked Milles production terrain set.
- True temporal BODY+robe attack sequence is unresolved; do not relabel single directional poses as animation frames.

## Release taxonomy

- IMPLEMENTED: YES — V6 natural-path surface-role split.
- INTEGRATED: YES — committed on shared PR #104 integration branch.
- BUILD VERIFIED: NO for current head until exact-head CI passes.
- DEVICE VERIFIED: NO.
- VISUAL ACCEPTED: NO.
