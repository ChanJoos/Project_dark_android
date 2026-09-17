# PROJECT DARK — F5M ACTIVE ROUTING

Updated: 2026-09-17 17:00 KST — DIRECTOR

ACTIVE_OWNER=GAMEPLAY
ACTIVE_F5M=F5M-024,F5M-025,F5M-009
BASE_SHA=18c2b7302321eb03138cec0a018f1fe4c1a18a8e
REQUIRED_DELTA=Complete the adapted prologue after RETURN_READY through an explicit milles_guide_proto turn-in transaction: grant completion reward exactly once through the existing authoritative reward/progression/inventory pipeline, transition Q_ADAPTED_F5M_PROLOGUE_01 RETURN_READY->COMPLETED, expose completion event/state so WORLD_UX removes/advances Quick Quest instead of leaving the completed quest visible, and prevent repeated NPC interaction from re-granting rewards.
ACCEPTANCE=Focused executable regression proves RETURN_READY turn-in -> one atomic reward grant -> COMPLETED; repeated turn-in grants nothing; authoritative quest state is queryable by UI. Then WORLD_UX binds COMPLETED to completion presentation/tracker removal and Integrator verifies on exact lineage. User device evidence already proves dialogue/accept -> real combat_dummy_01 kill -> RETURN_READY -> NPC return works, but tracker remains after apparent completion; this is DEVICE evidence of the missing completion path, not full DEVICE_VERIFIED.
BLOCKED_BY=none
NEXT_OWNER=WORLD_UX

## Director evidence
- Exact latest main before this routing decision: 18c2b7302321eb03138cec0a018f1fe4c1a18a8e.
- PR #121 is merged and its production quest contract intentionally leaves markCompletedFromTurnInTransaction() package-private for F5M-024 ownership; no production caller was found on current main.
- Current GameView owns a view-local F5mAdaptedPrologueQuest and renders COMPLETED as `첫 훈련 · 완료`; the F5M spec instead requires completion to remove/advance the tracker.
- User device path on the integrated World UX APK: quest accepted, real target killed, returned to NPC successfully; after apparent completion the quest remained visible. Treat this as acceptance-blocking evidence for F5M-024/025/009, not as polish.
- Main CI #499 on de2d5bf880ba57e4cbaa02e00676fbc5fa8aff71 passed Master DB, compile, F5M domain audit, production runtime binding, APK build/upload. This does not cover turn-in/completion and is not DEVICE_VERIFIED.
- Source-backed Q_MIL_01 / NPC_AN / NPC_BANE remain untouched. Adapted tutorial provenance remains ADAPTED / ADAPTED_BALANCE.
- Frozen attack presentation remains out of scope.

## Worker guard
GAMEPLAY: active owner. Implement the smallest coherent F5M-024 completion transaction on current main, with exact-once reward/idempotency regression. Do not duplicate quest state in UI and do not invent source rewards; use existing adapted reward/progression/inventory services and ADAPTED_BALANCE values/contracts already present where possible.
WORLD_UX: NO_ACTION_NOT_ACTIVE_OWNER until GAMEPLAY exposes the authoritative completion transaction/event. Then implement F5M-025/F5M-009 presentation: confirmed COMPLETED removes/advances Quick Quest, completion feedback reflects actual granted values, subsequent NPC dialogue is completed-state dialogue.
INTEGRATOR: wait for coherent GAMEPLAY candidate; verify exact BASE/HEAD, executable turn-in idempotency, reward single-grant and no regression of accept->kill->RETURN_READY. After WORLD_UX follow-up, verify tracker removal/completion presentation.
DIRECTOR: recalculate after GAMEPLAY evidence. If GAMEPLAY produces no coherent artifact across its due run, emit DIRECTOR_FALLBACK_REQUIRED for F5mAdaptedPrologueQuest + existing reward/progression/inventory transaction wiring and the milles_guide_proto RETURN_READY interaction path.
