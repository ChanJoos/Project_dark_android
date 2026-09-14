# PROJECT DARK — Serial Baton Handoff

This file is the rolling handoff for the interchangeable Full-stack Developer + Acting Director baton. Read it after `docs/REFERENCE_GROUND_TRUTH.md`, the two frame-analysis documents, and `docs/CURRENT_STRUCTURAL_CANON.md`. Newer committed facts override older entries here.

## Latest baton

- START_HEAD: `063f7876846bb22b3726da66a551c6c67f4ebb36`
- END_HEAD: `06153a9a2e264db1a40198f4f80c8bfc7a121df2` (implementation + acceptance-gate head before this handoff commit)
- WHAT CHANGED: Verified the prior foot-locked monster head `063f787...` was BUILD VERIFIED by GitHub Actions run #366. Then synchronized the prototype monster counterattack as one visible windup → contact → recovery event. `RuntimeState.Monster` now owns `attackRecoveryClock`; resolving an attack applies `PLAYER_HIT` exactly at the end of the existing 0.24 s windup but keeps the locked attack facing/state for a short 0.14 s `[ADAPTED]` recovery instead of snapping immediately to IDLE. `MonsterVisualRenderer` consumes both clocks: windup ramps the local foot-locked pose toward contact, recovery decays that same pose back to idle while preserving logical feet/shadow/HP anchor. The contact arc also persists through recovery with a distinct post-contact treatment. `tools/validate_runtime_acceptance.py` now gates the recovery clock, delayed attack-facing release, and renderer consumption.
- WHY VS REFERENCE: The combat video requires startup → contact → recovery and target/player feedback to read as one event. Before this baton the monster pose reached contact, `damagePlayer()` fired, then `attackPrimed` and attack facing were cleared immediately, so the attacking monster visually snapped to idle on the same frame the player damage feedback arrived. This change removes that discontinuity without inventing source monster frames or moving the monster off its canonical foot anchor.
- TESTS: Previous exact head `063f7876846bb22b3726da66a551c6c67f4ebb36` BUILD VERIFIED by run #366. Current implementation head `06153a9...` has GitHub Actions run #369 queued; exact-head BUILD VERIFIED remains NO until that run succeeds.
- KNOWN RISK: Player-initiated physical attack damage is still applied immediately in `GameView.attack()` while the mw001 visual reaches its contact apex later in the attack phase. Therefore monster `hitFlash`/damage popup/HP loss can still lead the weapon contact. This is now the highest non-device-dependent combat timing mismatch. Monster source sprite remains unresolved and procedural/adapted.

## Rolling roadmap

### DONE
- Canonical user reference videos captured as repository ground truth and frame-analysis documents.
- Exact 64×32 four-adjacent melee spatial contract and shared combat-facing semantics integrated in the corrective branch.
- Major robe SW/SE detachment regression structurally reduced via shared-atlas zero registration.
- Milles V5 landmark hierarchy: outer buildings, central fountain/plaza, clustered fenced vegetation, water-side landmark.
- Milles V6 surface-role split: natural-source ground variant for ROAD; stone restricted to PLAZA/GATE.
- Whole-paper-doll player attack lunge removed; BODY+robe remain foot-locked while weapon carries startup/contact/recovery motion.
- Live idle/walk/attack mw001 pivot uses authored per-direction/per-frame carry-anchor table instead of alpha-edge hand inference; BUILD VERIFIED.
- Procedural monster attack no longer translates the whole entity away from the logical foot anchor; source status remains `[ADAPTED] / SOURCE_SPRITE_PENDING`.
- Prototype monster counterattack now preserves locked pose/facing through a short recovery after contact, so `PLAYER_HIT` lands at windup completion without an immediate visual snap to idle.

### ACTIVE
- **Combat/attack/monster reference fidelity.** Monster counterattack windup/contact/recovery is structurally synchronized. Remaining highest-impact timing mismatch is player physical damage resolving before the visible weapon contact apex.

### NEXT
- **Player attack contact synchronization.** Defer `state.damage(target, damage)` for the physical ATTACK path from button-press time to the current mw001 contact phase, while preserving one-hit semantics, locked target/facing, cooldown behavior, death/reward idempotency, and cancellation safety if the player dies before contact. Gate it with an explicit contact-clock acceptance contract rather than an arbitrary cosmetic delay.

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

- IMPLEMENTED: YES — synchronized monster windup/contact/recovery + acceptance gate.
- INTEGRATED: YES — committed on shared PR #104 integration branch.
- BUILD VERIFIED: YES for prior head `063f787...`; NO for current `06153a9...` until exact-head run #369 succeeds.
- DEVICE VERIFIED: NO.
- VISUAL ACCEPTED: NO.
