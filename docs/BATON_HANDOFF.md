# PROJECT DARK — Serial Baton Handoff

This file is the rolling handoff for the interchangeable Full-stack Developer + Acting Director baton. Read it after `docs/REFERENCE_GROUND_TRUTH.md`, the two frame-analysis documents, and `docs/CURRENT_STRUCTURAL_CANON.md`. Newer committed facts override older entries here.

## Latest baton

- START_HEAD: `647ab0ed92a421c57c1573bb3dc6d2849b91cbb0`
- END_HEAD: `b06b6e76f3ab7a748796ccae1c8c4b9a3b612372` (implementation + acceptance-gate head before this handoff commit)
- WHAT CHANGED: The adapted ATTACK presentation no longer translates the entire BODY+robe+weapon paper doll forward/back. `drawAdaptedAttack()` now keeps BODY and robe foot-locked at the canonical logical position and delegates phase motion only to the existing mw001 weapon rotation around the same source-derived dominant-hand anchor. Presentation/evidence strings were updated to `PEASANT_MM001_BASE_20260915_R8_STATIONARY_POSE_WEAPON_ATTACK` / `ADAPTED_STATIONARY_POSE_WEAPON_3_PHASE`. Runtime acceptance now fails if the live adapted-attack block contains `translate(` or motion logic and separately requires startup/contact/recovery weapon phases.
- WHY VS REFERENCE: The combat reference reads as a short pose/weapon-driven strike, while the previous runtime visibly slid the entire paper doll during startup→strike→recovery. The repository still has no verified temporal BODY+robe attack sequence; group-02 remains a directional single-pose placeholder. Removing whole-character translation is therefore a smaller, evidence-aligned correction than fabricating BODY animation frames.
- TESTS: Static runtime acceptance was updated to enforce stationary BODY+robe attack geometry and preserved three-phase weapon timing. Exact-head GitHub Actions must still pass before BUILD VERIFIED can become YES.
- KNOWN RISK: The BODY silhouette is still the stable idle pose during attack because a true temporal BODY+robe sequence is unresolved. Weapon dominant-hand anchor is source-derived heuristically and still requires fresh device comparison for grip/arc quality. Monster presentation remains procedural/adapted.

## Rolling roadmap

### DONE
- Canonical user reference videos captured as repository ground truth and frame-analysis documents.
- Exact 64×32 four-adjacent melee spatial contract and shared combat-facing semantics integrated in the corrective branch.
- Major robe SW/SE detachment regression structurally reduced via shared-atlas zero registration.
- Milles V5 landmark hierarchy: outer buildings, central fountain/plaza, clustered fenced vegetation, water-side landmark.
- Milles V6 surface-role split: natural-source ground variant for ROAD; stone restricted to PLAZA/GATE.
- Whole-paper-doll attack lunge removed from the live adapted attack path; BODY+robe remain foot-locked while weapon carries startup/contact/recovery motion.

### ACTIVE
- **Combat/attack/monster reference fidelity.** Validate stationary attack on device; then reduce remaining mismatch in weapon grip/arc, target reaction/effect timing, and monster presentation without fabricating source temporal BODY frames.

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

## Release taxonomy

- IMPLEMENTED: YES — stationary BODY+robe / weapon-driven adapted attack presentation.
- INTEGRATED: YES — committed on shared PR #104 integration branch.
- BUILD VERIFIED: NO until exact-head CI passes after this baton.
- DEVICE VERIFIED: NO.
- VISUAL ACCEPTED: NO.
