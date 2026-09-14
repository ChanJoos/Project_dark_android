# PROJECT DARK — Serial Baton Handoff

This file is the rolling handoff for the interchangeable Full-stack Developer + Acting Director baton. Read it after `docs/REFERENCE_GROUND_TRUTH.md`, the two frame-analysis documents, and `docs/CURRENT_STRUCTURAL_CANON.md`. Newer committed facts override older entries here.

## Latest baton

- START_HEAD: `ed7daecbda4889526619077723626d664f746de9`
- END_HEAD: `52c028265bd3ed94acfea7a3bf81cdd379a3cac1` (implementation + acceptance-gate head before this handoff commit)
- WHAT CHANGED: The live mw001 grip path no longer accepts the idle/walk BODY atlas `outerCluster()` alpha edge as its primary dominant-hand truth. `CharacterSemanticRig.derive()` now detects the authored 36×48 idle/walk atlas cell, derives its exact atlas column, and reuses the existing per-direction/per-frame `CharacterRenderer.weaponCarryOffsetX/Y` table as the weapon hand pivot. Alpha-edge inference remains only as a fallback for detached/non-authored bitmaps. `tools/validate_runtime_acceptance.py` now gates this contract and fails if the authored idle/walk hand-anchor wiring disappears.
- WHY VS REFERENCE: The combat reference requires the weapon to read as hand-driven, while the previous device evidence showed mw001 visually near the belly/right-lower body. The prior live runtime asked an alpha-extreme heuristic to decide `dominantHand`, even though an explicit per-direction/per-frame carry-anchor table already existed. Reusing that authored table removes a structurally arbitrary attachment source without fabricating new temporal BODY frames. The table remains `[ADAPTED]` until source metadata explicitly names an original hand pivot.
- TESTS: Previous exact head `ed7daecbda4889526619077723626d664f746de9` is BUILD VERIFIED by GitHub Actions run #360. Current exact-head validation must pass after this baton before the new hand-anchor change becomes BUILD VERIFIED.
- KNOWN RISK: Fresh device evidence is still required to confirm actual hand/handle overlap and swing arc. BODY silhouette remains the stable idle pose during attack because a true temporal BODY+robe sequence is unresolved. Monster presentation is still procedural/adapted.

## Rolling roadmap

### DONE
- Canonical user reference videos captured as repository ground truth and frame-analysis documents.
- Exact 64×32 four-adjacent melee spatial contract and shared combat-facing semantics integrated in the corrective branch.
- Major robe SW/SE detachment regression structurally reduced via shared-atlas zero registration.
- Milles V5 landmark hierarchy: outer buildings, central fountain/plaza, clustered fenced vegetation, water-side landmark.
- Milles V6 surface-role split: natural-source ground variant for ROAD; stone restricted to PLAZA/GATE.
- Whole-paper-doll attack lunge removed from the live adapted attack path; BODY+robe remain foot-locked while weapon carries startup/contact/recovery motion.
- Live idle/walk/attack mw001 pivot now uses the authored per-direction/per-frame carry-anchor table instead of alpha-edge hand inference for the shared BODY atlas.

### ACTIVE
- **Combat/attack/monster reference fidelity.** Await device confirmation of weapon grip/arc while continuing to reduce non-device-dependent mismatch in monster presentation and hit/contact feedback.

### NEXT
- **Source-backed/non-placeholder monster visual.** Search existing repository/source material first. If no defensible source sprite exists, keep current monster explicitly `[ADAPTED]` and improve only with reference-supported silhouette/scale/facing evidence.

### LATER
- Return to Milles only after fresh device evidence identifies a concrete remaining visual delta; true dirt path and continuous river/bridge/shrine remain unresolved/source-limited.
- Playable UX/NPC/reward/inventory/equipment loop.
- Progression/save.
- Broader content expansion.

### BLOCKED
- VISUAL ACCEPTED for Milles/attack requires a fresh device APK recording from the user.
- True source dirt-path asset is unresolved in the checked Milles production terrain set.
- True temporal BODY+robe attack sequence is unresolved; do not relabel directional group-02 poses as animation frames.
- Exact original hand-pivot metadata for mw001 is unresolved; the authored carry-anchor table is `[ADAPTED]` until source metadata is found.

## Release taxonomy

- IMPLEMENTED: YES — authored-table mw001 hand pivot for shared idle/walk BODY atlas.
- INTEGRATED: YES — committed on shared PR #104 integration branch.
- BUILD VERIFIED: NO for the current hand-anchor head until exact-head CI passes; prior head `ed7daec...` was BUILD VERIFIED YES.
- DEVICE VERIFIED: NO.
- VISUAL ACCEPTED: NO.
