# Character Visual Failure Ledger

Permanent regression gate for PROJECT DARK character visuals. CI/build success never upgrades DEVICE_FAIL or VISUAL_FAIL to accepted. Only fresh device evidence can.

## Non-negotiable rules
- Never render an attacking/swinging weapon over an IDLE paper-doll BODY fallback. If source BODY action is unavailable, BODY, garment, and weapon remain non-attacking together.
- mm001 group-02 is SINGLE_POSE_PLACEHOLDER / temporal semantics UNRESOLVED. Do not reinterpret four directional source poses as fabricated temporal animation frames.
- A garment helper/wrapper is not evidence of shared registration. Preserve source-authored registration and independently verify the composite.
- Previously device-accepted mu0000058 robe IDLE/WALK registration is a regression baseline; do not replace it with a new heuristic merely to fit another garment.
- mu0000001 shirt source metadata is authoritative for recovered pixels, but runtime placement remains VISUAL_PENDING until device accepted.
- Any path marked DEVICE_FAIL/VISUAL_FAIL stays failed until a later exact APK is explicitly device accepted.

## Failure history
| Evidence / commit | Attempt | Result | Lesson | Status |
| --- | --- | --- | --- | --- |
| PR #103 / 7fe3403, eac6b79 | Restore source attack BODY; forbid weapon-only fallback; repair robe registration | Later regressed | Correct invariant existed but was not permanent | REGRESSION BASELINE |
| PR #119 / 472a718 | CharacterSemanticRig hand/mirror repair | Device acceptance not established | Rig refinement is not visual proof | HISTORICAL PENDING |
| 90e6be0 / dac6f3f | Shirt visibility/resource substitution | Rejected | Wrong/substituted source cannot be promoted | FAIL |
| 097f792 / f9a0aa1 | Recover exact mu0000001 atlas + metadata | Source recovered | Runtime registration still unproven | SOURCE_VERIFIED |
| c422395 | Shirt source-anchor registration | Later device visual still wrong | Source metadata alone does not prove BODY composite | DEVICE_VISUAL_FAIL lineage |
| 600af566 | Shared drawGarmentAttached wrapper | Device video 2026-09-21 failed | Function unification != coordinate unification | DEVICE_VISUAL_FAIL |
| 4da5154 | Remove weapon-only ATTACK fallback | Pending | Re-establishes #103 invariant | IMPLEMENTED_PENDING |

## Required acceptance sequence
SOURCE_VERIFIED -> STATIC_COMPOSITE_VERIFIED -> CI/BUILD_VERIFIED -> exact-SHA APK -> DEVICE_VERIFIED -> VISUAL_ACCEPTED.

Do not skip or infer a later state from an earlier state.
