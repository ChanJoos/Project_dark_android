# PROJECT DARK — F5M ACTIVE ROUTING

Updated: 2026-09-17 16:00 KST — DIRECTOR

ACTIVE_OWNER=WORLD_UX
ACTIVE_F5M=F5M-006,F5M-008,F5M-009
BASE_SHA=f1e7621cdde76501864441f2ed51b6a2b73f59e1
REQUIRED_DELTA=milles_guide_proto interaction opens authoritative Q_ADAPTED_F5M_PROLOGUE_01 offer dialogue; explicit accept/decline; accept immediately exposes Quick Quest "훈련용 몬스터 0/1" from authoritative quest state; real combat_dummy_01 production defeat updates tracker to 1/1 / RETURN_READY and changes it to return-NPC instruction.
ACCEPTANCE=production UI/runtime wiring uses authoritative quest state with no duplicate hard-coded quest state; accept/decline are idempotent; actual combat_dummy_01 RuntimeState.damage defeat drives 0/1->1/1; focused audit/test and exact-head compile pass; coherent commit/PR handed to Integrator. CI/compile is not DEVICE_VERIFIED.
BLOCKED_BY=none
NEXT_OWNER=INTEGRATOR

## Director evidence
- PR #121 is MERGED. Replayed exact PR head was 91574358377628bc2b5bf66d826618b281149dc0; main integration commit is c6ee624f62ba3c632e6b147637d69cf5af6f7101.
- Latest main at routing decision is f1e7621cdde76501864441f2ed51b6a2b73f59e1.
- PR #121 carried exact-head CI run 35183170132 / job 105079520381 PASS for Master DB, production compile, F5M domain audit and production runtime-binding audit. This is CI evidence only.
- Source-backed Q_MIL_01 / NPC_AN / NPC_BANE remain untouched. combat_dummy_01 and tutorial provider remain ADAPTED / ADAPTED_BALANCE where applicable.
- Frozen attack presentation remains out of scope absent new blocking evidence.

## Worker guard
GAMEPLAY: NO_ACTION_NOT_ACTIVE_OWNER unless WORLD_UX exposes a concrete gameplay-contract blocker.
WORLD_UX: implement the REQUIRED_DELTA now; player-visible code/commit/PR is required unless a concrete code-level blocker is demonstrated.
INTEGRATOR: wait for the coherent WORLD_UX candidate, then independently verify exact lineage, runtime wiring, focused tests and compile; integrate only if no BLOCKER.
DIRECTOR: on next cycle, re-read latest main, this routing file, execution log, PR/CI and Integrator evidence; recalculate ownership rather than repeating this assignment blindly.
