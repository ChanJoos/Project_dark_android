## 2026-10-02 — V82 BUILD/NATIVE VERIFIED; kicks/Dara phone pending

Source `4bf789dc952403163fbc0aab6bc805186f75a28c`, tree `8177ecda01df08c33a49baa715449382a068fa1b`. Actions36942749572 /job110637808191 SUCCESS at that exact checkout. APK artifact11201490465, SHA256 `48612dec0648f4bdcee106af80a0cb34d84b6d29752a8e31356b7b7f4101bc1c`, 29124483 bytes, build completed2026-10-02T08:53:41+09:00 Asia/Seoul. Binary version82/0.82-martial-kick-release confirmed. All659 locally available packaged assets match bytes; all268 original BODY frames unchanged. Artifact/APK/native CRC and published archive digests verified.

Native artifact11201495497:679 PNGs, including272 V82 actor/live frames. All24 equipped kick contact actors (three kicks/two bodies/four actual facings),8 spin turn actors and8 Dara waiting/release/lower/end sequences reviewed. Physical front/side kicks use actual original d-group lifted-leg frames; spin traverses rear/front orientations. Dara pixels remain equipped standing through2.99sec and raises f1/f3 only at real3sec damage/recipient release; lowers and recovers. Configured source/catalog/skill/world/combat/growth/restart checks all pass. Earlier run36941351678 failed a test-only generic reflection varargs cast; run36941896830 passed. Final follow-up strengthened actual self-area starting-facing assertions; it changed only tests and final APK manifest/runtime/catalog bytes match that prior passing build.

V81 Ragnarok and Complete Defense are USER VISUAL ACCEPTED and retained unchanged. Original project sequencing, missing garment groups, other source gaps remain explicit. V82 kicks/Dara physical-phone and user visual acceptance PENDING; this is an unmerged PR176 task candidate, main bfd668d4 unchanged. Delivery includes exact APK, build proof and native-review contact/Dara mosaics inside ZIP. Full proof: verification/SKILL_FX_V82_BUILD.json. Next: phone revalidation of these four skills; preserve approvals and unresolved facts.

## 2026-10-02 — V82 user-reported kick BODY and Dara release repair

V81 Ragnarok and Complete Defense are USER VISUAL ACCEPTED per current phone report; exact installed hash was not supplied. V81 Dankak/Bungkak/Spin and early Dara raised arms are DEVICE_FAILED (user-reported). Root cause reproduced in the source: V80 selected c14–23 grounded warrior lunge/thrust frames for kicks, while original group d contains the actual lifted-knee/extended-foot martial sprites. Previous test merely confirmed the erroneous c selection; that assertion is superseded.

Accepted mappings: Dankak front kick d0/1/2 rear, d3/4/5 front; Bungkak side kick d10/11/12/13 rear, d14/15/16/17 front. Spin cycles these source side-kick orientations through a turn. Source frames are byte-identical; selected project timing still reaches contact at1/3. Dara keeps fully equipped male IDLE (female source IDLE) throughout the existing3sec preparation, raises f1/f3 only at actual release/contact, then lowers during recovery. Damage/contact/resources/reach unchanged. No source redraw. Related upgraded kick profiles inherit their corrected base family.

Consumed inputs: user report; original mm001/wm001 group c/d/f frames; labelled four classic GIFs and definitions; Asset_Animation_Frame_Master/Asset_Animation_Semantics; SKILL_ACTION_DECISIONS; generator/catalog; SkillBodyRenderer/GameView and current contracts. Governing Master correction: changes/USER-MARTIAL-KICK-RELEASE-V82.json. New tests use actual GameView input/clock for both bodies/four facings with equipped actor/whole-scene captures, standing-pixel equality during Dara wait, real damage/recipient effect at release, and lower/recovery frames. Exact-SHA CI/native/render/APK verification pending. PR176 remains unmerged task candidate; main bfd668d4 unchanged. Next: review these exact live images and deliver candidate ZIP only after checks, with physical-phone revalidation pending.

# PROJECT DARK Autonomy Baton

This file carries only the immediate handoff between scheduled autonomous developers. Long-lived project truth belongs in `docs/PROJECT_STATE.yaml`; development rules belong in `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.

## Current baton

AGENT: DARK Dev 1 -> DARK Verify 1 / DARK Verify 2

HEAD / WORK LINE:
- shared `main`
- DEVICE_TESTED_HEAD: `60721da4e002c21ad20e0aa82687e1f25c80e251`
- PR #118 is merged and exact-main Android CI run #428 passed.
- `ATTACK_PRESENTATION_002` is therefore BUILD_VERIFIED but DEVICE/VISUAL FAILED on fresh user recording `Screen_Recording_20260916_151528.mp4`.

TASK / WORK PACKAGE:
- `ATTACK_PRESENTATION_003_DEVICE_REPAIR`
- lifecycle: `READY -> IN_DEVELOPMENT`
- This is a focused repair of the already merged attack vertical slice. Do not expand equipment, add temporal frames, redesign combat, or invent source art.

DEVICE EVIDENCE — 2026-09-16 USER ACCEPTANCE TEST:
User explicitly identified these failures in the exact-main #118 APK:
1. Mokdo position is slightly offset from the hand.
2. A single attack pose / three-stage attack presentation is acceptable, but during the inserted attack pose the character becomes larger and the robe is absent; the attack pose is not composed with the equipped robe and mokdo correctly.
3. Attack pose visual scale is larger than the normal character and creates a visible discontinuity.
4. Do NOT treat the single inserted pose / three-stage timing itself as a defect. Preserve the current startup -> contact pose -> recovery structure; do not fabricate temporal frames.
5. Attack animation facing can differ from the monster/target direction. This is a functional visual defect.

REQUIRED ROOT-CAUSE INVESTIGATION:
- Trace final screen-space geometry for normal equipped IDLE versus source-action CONTACT. Compare BODY destination rect, logical foot anchor, source baked scale, SOURCE_PRESENTATION_SCALE, robe destination rect/registration, and weapon destination/handle anchor. Do not accept finite/loadability-only probes as proof.
- Confirm whether group-02 robe source is actually selected/drawn in the CONTACT path for all four directions and whether BODY/robe use the same final source-action transform. If robe source is unavailable for any direction, keep that state explicit rather than silently dropping the robe.
- Trace attack-facing at the combat submission/target-lock boundary through runtime pose creation into CharacterRenderer. ATTACK facing must come from the canonical adjacent target tile delta at accepted attack time, not stale movement facing or previous actor facing.
- Trace mokdo handle attachment against the action BODY `dominantHand` anchor in final rendered coordinates, including any action-source offset, pivot, foot-anchor normalization, and presentation scale.

REQUIRED MINIMAL RUNTIME DELTA:
- Preserve the accepted three-stage presentation: equipped IDLE/startup -> ONE source-backed directional CONTACT pose -> equipped IDLE/recovery.
- At CONTACT render one coherent paper-doll composite: ATTACK BODY + equipped robe + equipped mokdo. BODY/robe/weapon must share the same facing and compatible action transform.
- Normalize CONTACT to the same gameplay presentation scale and logical foot anchor as normal character rendering. Source bitmap dimensions must not directly cause a larger on-screen actor.
- Correct mokdo handle-to-action-hand registration in final screen coordinates; avoid arbitrary screen-space offsets unless the source evidence requires an explicit per-direction authored transform and that transform is documented/tested.
- Lock BODY/robe/weapon attack facing to the attacked monster's canonical adjacent tile delta (NW/NE/SW/SE) for the contact presentation.
- Do not change canonical melee legality, reward flow, monster AI, or the accepted single-pose temporal policy.

MANDATORY REGRESSION / EVIDENCE GATE:
For all four attack directions, evidence must prove:
- target canonical tile delta -> expected ATTACK facing exactly;
- CONTACT BODY facing == robe facing == weapon facing;
- equipped robe remains visible/composited during CONTACT;
- idle/startup and CONTACT logical foot positions remain aligned within a small explicit pixel tolerance;
- CONTACT actor visual scale/height is consistent with equipped IDLE within an explicit tolerance justified by source pose silhouette, not raw bitmap dimensions;
- mokdo handle aligns with action BODY dominantHand anchor within an explicit pixel tolerance;
- recovery returns to the same equipped IDLE composition;
- no new fabricated temporal frames or generated replacement art.

VERIFICATION POLICY:
- Dev must implement one coherent runtime delta and run local/static evidence where available.
- Verify 1/2 must resolve the exact PR HEAD SHA and inspect GitHub Actions for that exact SHA, including push-triggered runs. If normal PR/check lookup is empty, query repository Actions by head SHA and inspect jobs/steps directly.
- BUILD_VERIFIED requires exact-head Android validation/build success.
- After merge, exact-main CI must pass before APK release.
- DEVICE_VERIFIED and VISUAL_ACCEPTED remain false until a fresh exact APK recording confirms all five user findings are resolved. CI or evidence probes alone cannot promote visual acceptance.

ACCEPTANCE STATE:
- PR #118 / `60721da4...`: BUILD_VERIFIED = YES
- PR #118 DEVICE_VERIFIED = FAIL
- PR #118 VISUAL_ACCEPTED = FAIL
- `ATTACK_PRESENTATION_003_DEVICE_REPAIR`: READY / IN_DEVELOPMENT

NEXT ACTION:
- DARK Dev 1: inspect current main and implement the focused five-finding repair above, then open/update one clean PR with exact evidence.
- DARK Verify 1 / 2: independently validate actual runtime wiring, exact-head CI and the five regression criteria; selectively merge only if coherent. After merge, build exact-main APK for fresh user device acceptance.
- Once this gate passes, freeze further equipment/attack animation expansion and return to the approved first-five-minute gameplay loop.
