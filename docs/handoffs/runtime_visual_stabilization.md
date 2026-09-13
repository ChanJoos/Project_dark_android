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
- Body mirror and mw001 weapon mirror are explicit independent transforms. The weapon receives exactly one local mirror, after world-space attack rotation; no body transform is inherited by the weapon.

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

BUILD and DEVICE verification remain separate gates.
