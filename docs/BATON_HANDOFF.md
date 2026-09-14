# PROJECT DARK — Serial Baton Handoff

This file is the rolling handoff for the interchangeable Full-stack Developer + Acting Director baton. Read it after `docs/REFERENCE_GROUND_TRUTH.md`, the two frame-analysis documents, and `docs/CURRENT_STRUCTURAL_CANON.md`. Newer committed facts override older entries here.

## Latest baton

- START_HEAD: `ca169d525e5264794507a379459484e65e7b1d40`
- END_HEAD: `eb292bf6c02855da7010b7a67185058e509f1e1c` (implementation + acceptance-gate head before this handoff commit)
- WHAT CHANGED: Verified the prior mw001 authored-table hand-anchor head `ca169d5...` with exact-head GitHub Actions run #363 = success, so that change is now BUILD VERIFIED. Then audited repository source material for a defensible Milles monster sprite: the live runtime still uses prototype `combat_dummy_01 [B]`; the checked Android `assets/visual` tree contains only player/robe/weapon manifests and no source monster sprite. Because replacing the dummy with a region-bound canonical monster would invent a spawn relation, the procedural monster remains explicitly `[ADAPTED]`. Its live attack presentation was changed from whole-entity forward displacement to a foot-locked pose: `x/y` stay at the logical monster anchor, feet/shadow/HP remain fixed, while a small torso lean + near-arm extension carry the attack silhouette. `tools/validate_runtime_acceptance.py` now fails if whole-monster lunge translation reappears.
- WHY VS REFERENCE: The combat reference says impact should read from a short pose/weapon/contact sequence, not large whole-character translation. The player attack was already foot-locked; the procedural monster still violated that principle with a 3.5 px whole-entity shove. This baton removes that non-source motion without pretending a source monster sprite exists.
- TESTS: Prior exact head `ca169d525e5264794507a379459484e65e7b1d40` BUILD VERIFIED by run #363. New exact-head CI is required after this handoff commit before the monster foot-lock change is BUILD VERIFIED.
- KNOWN RISK: Monster remains procedural/adapted because no defensible source monster sprite is present in the checked runtime asset set and `combat_dummy_01` is intentionally a prototype fixture. Fresh device evidence is required to judge silhouette/scale/facing. True temporal BODY+robe attack sequence and original mw001 hand-pivot metadata remain unresolved.

## Rolling roadmap

### DONE
- Canonical user reference videos captured as repository ground truth and frame-analysis documents.
- Exact 64×32 four-adjacent melee spatial contract and shared combat-facing semantics integrated in the corrective branch.
- Major robe SW/SE detachment regression structurally reduced via shared-atlas zero registration.
- Milles V5 landmark hierarchy: outer buildings, central fountain/plaza, clustered fenced vegetation, water-side landmark.
- Milles V6 surface-role split: natural-source ground variant for ROAD; stone restricted to PLAZA/GATE.
- Whole-paper-doll player attack lunge removed; BODY+robe remain foot-locked while weapon carries startup/contact/recovery motion.
- Live idle/walk/attack mw001 pivot uses authored per-direction/per-frame carry-anchor table instead of alpha-edge hand inference; exact head `ca169d5...` BUILD VERIFIED.
- Procedural monster attack no longer translates the whole entity away from the logical foot anchor; attack pose is local silhouette/arm motion only. Source status remains `[ADAPTED] / SOURCE_SPRITE_PENDING`.

### ACTIVE
- **Combat/attack/monster reference fidelity.** Current non-device-dependent lunge mismatch is structurally removed. Remaining work should focus on contact/hit reaction timing and any source-backed monster evidence that can be defended without inventing Milles spawn identity.

### NEXT
- **Hit/contact feedback synchronization.** Audit player attack contact frame, monster `hitFlash`/damage-popup timing, and monster counterattack feedback so startup → contact → recovery reads as one event rather than independent effects. Use the combat reference and existing runtime event clocks; avoid arbitrary cosmetic additions.

### LATER
- Source-backed/non-placeholder monster visual if a defensible source asset + identity relation becomes available.
- Return to Milles only after fresh device evidence identifies a concrete remaining visual delta; true dirt path and continuous river/bridge/shrine remain unresolved/source-limited.
- Playable UX/NPC/reward/inventory/equipment loop.
- Progression/save.
- Broader content expansion.

### BLOCKED
- VISUAL ACCEPTED for Milles/attack/monster requires a fresh device APK recording from the user.
- True source dirt-path asset is unresolved in the checked Milles production terrain set.
- True temporal BODY+robe attack sequence is unresolved; do not relabel directional group-02 poses as animation frames.
- Exact original hand-pivot metadata for mw001 is unresolved; the authored carry-anchor table is `[ADAPTED]` until source metadata is found.
- Canonical Milles monster identity/source sprite is unresolved; `combat_dummy_01` remains a prototype fixture and region-bound monsters must not be injected merely to replace a placeholder.

## Release taxonomy

- IMPLEMENTED: YES — foot-locked procedural monster attack pose + regression gate.
- INTEGRATED: YES — committed on shared PR #104 integration branch.
- BUILD VERIFIED: YES for prior hand-anchor head `ca169d5...`; NO for the current monster foot-lock head until exact-head CI succeeds.
- DEVICE VERIFIED: NO.
- VISUAL ACCEPTED: NO.
