# Combat Frame Analysis

Canonical source: `Screen_Recording_20260915_003701_YouTube.mp4`
Status: working measurement sheet; refine with additional frame extraction as implementation proceeds.

## Confirmed reference characteristics
- Combat readability comes from a short, explicit attack sequence rather than large whole-character translation.
- The attack must be evaluated as startup → contact → recovery, with body silhouette, weapon travel and target reaction considered together.
- Player and target maintain a clear close-combat spatial relationship; PROJECT DARK canonical melee legality remains exact 64×32 adjacent-tile logic.
- Weapon/contact feedback and target reaction/effect timing contribute materially to perceived impact.
- Relative player/monster scale, facing, chase cadence and attack interval must be judged against the video when observable.
- A single directional pose is not evidence of a temporal animation sequence.

## Required comparison for every combat-visual change
1. startup duration,
2. contact timing,
3. recovery duration,
4. BODY silhouette delta from idle,
5. weapon handle/hand attachment and weapon travel,
6. target reaction/effect/damage timing,
7. player-target separation at attack,
8. facing consistency across BODY/robe/weapon,
9. monster pursuit speed relative to player,
10. absence of noncanonical straight/radius melee attacks.

## Current project delta to eliminate
- Earlier group-02 treatment incorrectly behaved like temporal animation although source evidence supports a direction pose, not a sequence.
- Later adapted 3-phase runtime improved stability but still reads as whole-character lunge/recoil rather than source-like body/weapon action.
- Future changes must reduce this measured presentation delta instead of merely tuning translation magnitude.

## Source-truth labels
- Source-observed video behavior: `[V]`.
- Official/source-backed data or assets: `[O]` where applicable.
- Prototype inference: `[B]`.
- Deliberate non-source adaptation: `[ADAPTED]`.
- Unknown timing/pose/asset semantics: `UNRESOLVED`.

## Measurement discipline
When binary-video access is available, add timestamp/frame measurements before/with implementation. Record the observable fact separately from the implementation decision. Do not convert an estimate into an original-game fact without evidence.