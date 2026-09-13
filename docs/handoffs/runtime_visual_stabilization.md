# Runtime + Visual stabilization handoff

Base: `c1ea17068164bcdbc068bef34143dbc540582a79`

Scope is intentionally limited to the seven stabilization findings supplied after device QA. World-owned monster locomotion remains outside this branch and continues in PR #100.

## Runtime
- Inventory is an overlay: opening, closing, selecting, equipping and unequipping no longer cancel an existing World move request.
- Existing update loop remains live while inventory is visible: combat tick, RuntimeState tick, monster AI, navigation interpolation and camera follow continue.
- Only inventory-panel touches are consumed by inventory; touches outside the panel continue through normal HUD/world routing while the panel stays open.
- Player basic attack snapshots target delta exactly once through the canonical NW/NE/SW/SE quantizer. `attackFacing` remains locked until action completion.
- Monster attacks continue to use the same CanonicalActorFacing snapshot contract in RuntimeState; no 8-way renderer direction exists.

## Visual
- `mu0000058` is explicitly `FULL_BODY`; coverage vocabulary adds FULL_BODY/UPPER/LOWER and extension slots HEAD/HANDS/FEET/WEAPON/SHIELD.
- BODY and FULL_BODY robe keep the same direction, atlas column, scale and source foot-anchor contract while walking; accepted bare-body walk cadence remains unchanged.
- Equipped attack layers are atomic. Missing robe/weapon/action geometry rejects the partial action and falls back to the complete equipped source-IDLE paper doll.
- Attack body geometry is measured from actual bitmap alpha bounds against the matching direction's IDLE alpha bounds. Action rendering is accepted only within height <=1 source px, foot <=1 px and center <=2 px after translation normalization. There is no action-only scale multiplier; common presentation scale remains 1.70.
- Player HIT/HURT/FLINCH character pose/effect routing is disabled. Damage feedback remains in HP/ledger/banner paths without swapping the paper doll.
- Body mirror and mw001 weapon mirror are explicit independent transforms. The weapon receives exactly one local mirror after rotation; no body transform is inherited by the weapon.

## Decoded source-pixel evidence — mandatory Director gate
`CharacterVisualEvidenceProbe.collect(Resources)` reads the exact runtime drawables instead of accepting constants as proof. It must be run on the same APK/SHA that is proposed for merge/release.

Required evidence:
1. **ROBE WALK 4×5** — all 20 NW/NE/SW/SE × IDLE/WALK atlas cells for `mm001` BODY and `mu0000058` FULL_BODY robe are non-empty. For each facing, robe-vs-body alpha-foot delta may vary by at most 1 source px across the five columns and centerline delta by at most 2 source px. Both layers use the same row, column, 36×48 cell, `(18,46)` source foot anchor and 1.70 presentation contract.
2. **ATTACK COMPOSITE** — for all four facings, measure the actual alpha union of `BODY + FULL_BODY robe` after the same BODY normalization offset used by the renderer. Compare with that direction's equipped IDLE composite. Required errors: visible height <=1 source px, foot <=1 px, center <=2 px. If this evidence fails, source ATTACK must not be accepted; complete equipped source-IDLE fallback is the only safe presentation.
3. **MW001 HAND/TIP** — scan the actual `mw001` alpha pixels, use the renderer hand pivot `(2,4)`, derive the farthest opaque pixel as blade tip, then record hand→tip vectors for all four facings × five carry columns. Vector length must remain invariant, hand/tip finite, and renderer must report single weapon mirror. Director/device QA must visually confirm the recorded direction-specific vectors place the blade tip on the intended side of the hand with correct front/back depth.
4. **ATTACK WEAPON** — visually confirm NW/NE/SW/SE at start/peak/end of SWING. No double mirror, no flipped tip, no stale weapon, no ghost/trail.

A PR being open/mergeable is not evidence. Missing probe output, missing one direction/frame, or any failed composite measurement means **DO NOT MERGE**.

## Preserved
- bare BODY walk cadence and atlas
- player target-facing behavior
- common presentation scale 1.70
- source-only mm001 / mu0000058 / mw001
- `[ADAPTED PLAYTEST] / UNRESOLVED` action semantics
- collision/camera/world/reward rules
- no startup fail-fast audit

## Validation
- `CanonicalActorFacingAudit`
- `CharacterRendererAudit`
- `RuntimeVisualStabilizationAudit`
- `CharacterVisualEvidenceProbe` decoded-resource evidence on the exact candidate APK/SHA

BUILD and DEVICE verification remain separate gates.
