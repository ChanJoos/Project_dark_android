# Device feedback — eac6b792 — 2026-09-14

Authoritative source: fresh Android device recording from exact main `eac6b792b404b2ebc27ca7304c107e2709482739` after successful CI/APK build.

## Release verdict
- BUILD VERIFIED: PASS
- DEVICE VERIFIED: FAIL
- VISUAL ACCEPTED: FAIL

## World — DEVICE FAIL
The 8x4 quarter-stride segment lock is mathematically canonical but visually unacceptable. Direct north/south pursuit reads as short alternating diagonal zig-zag/jitter. Do not defend the current implementation using the 4096-step audit; the device gate supersedes that acceptance assumption.

Required correction:
- Keep every applied movement vector strictly NW/NE/SW/SE with equal speed and atomic collision.
- Replace the visible short-period 8px left/right oscillation with a trajectory policy that reads as deliberate diagonal locomotion rather than jitter or synthetic vertical glide.
- Do not reintroduce frame-by-frame NE/NW alternation.
- Do not use cardinal/free-angle correction.
- Add an executable trajectory gate that measures direction-switch frequency / minimum committed visual run, not only lateral envelope and per-step canonicality.
- Device acceptance is authoritative.

## Visual — DEVICE FAIL
Attack no longer reads as only a sword swing, but the actor still does not read as a coherent source animation. BODY/robe/weapon transition abruptly and the current vertical-crop normalization visibly breaks continuity.

Required correction:
- Remove destructive vertical cropping as the acceptance mechanism for group-02 BODY.
- Preserve source pixels at the common 1.70 presentation scale; align by semantic foot/pivot registration rather than deleting visible alpha.
- Treat ATTACK as a temporal contract: source-backed IDLE/wind-up -> source-backed action pose + robe + mw001 -> source-backed recovery/IDLE. BODY, robe and weapon must change phase together. A static action BODY for the entire attack state is not accepted.
- Never permit weapon-only attack.
- Keep target-facing four-direction attack snapshot/lock.

## Robe walk — DEVICE FAIL SW/SE
NW/NE remain the accepted baseline and must not change. SW/SE still show frame-to-frame BODY/robe registration jumps.

Required correction:
- Stop treating raw alpha center/bottom changes as a sufficient semantic pivot; that can over-correct animation frames.
- Establish stable per-frame semantic foot/pivot registration for SW/SE against BODY, then gate frame-to-frame displacement continuity.
- Preserve direction/frame/walk-clock lock and 0.12s/frame cadence.
- Add a continuity audit that rejects registration deltas that create visible frame jumps even when alpha centers numerically align.

## Non-goals
No new features, CAST expansion, map work, combat reward changes, inventory changes, or unrelated refactors until these device failures are corrected.

## Merge gate
A corrective PR may be IMPLEMENTED and BUILD VERIFIED, but it must not be called DEVICE VERIFIED or VISUAL ACCEPTED until a fresh APK is recorded and passes the three device checks above.

## 2026-09-14 corrective integration status
- P1 World integrated from PR #105: monster locomotion now consumes the same authored 64x32 tile-center contract as player movement, using ±32/±16 adjacent endpoints and no 8x4/16x8 acceptance tuning.
- P2 Game Systems integrated surgically from PR #106: `CanonicalActorFacing` and its audit now quantize attack direction in the World tile basis and enforce reciprocal NW<->SE / NE<->SW snapshots. An unrelated stale Visual probe revert from the worker branch was intentionally excluded.
- P3/P4/P5 Visual integrated from PR #107: explicit SW/SE robe registration, semantic attack BODY foot/pivot registration at common 1.70 with no destructive crop, and continuous SE mw001 carry hand path.
- Current integration branch remains PR #104 and must stay unmerged to main until exact-head CI passes and a fresh device recording is accepted.
