# PROJECT DARK — DEV HISTORY PASS 10

Date: 2026-09-10
Role: Combat · Monster

## Source-of-Truth gate executed

Read before coding:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest `docs/DEV_HISTORY_PASS_09.md` plus PASS 08 integration audit

`data/design/PROJECT_DARK_CANONICAL_SEED.md` remains a derived convenience projection and was not allowed to override either Master.

## PLAYABLE SLICE bottleneck selected

The current combat loop already performed chase, attack windup, hit, death and respawn, but monster lifecycle was represented by scattered booleans/timers (`alive`, `attackPrimed`, cooldowns) rather than an explicit runtime state. This made the required SPAWN/IDLE/WANDER/DETECT/CHASE/ATTACK/DEAD/RESPAWN contract impossible to audit and made later AI extraction risky.

PASS 08 also identified monster AI orchestration as a remaining coupling target after reward-chain contracts. This pass therefore establishes the runtime state seam without inventing canonical AI distances, rates, monster IDs, drop values or original timings.

## Implementation completed

Updated `RuntimeState.Monster` with explicit runtime-only state:

`SPAWN / IDLE / WANDER / DETECT / CHASE / ATTACK / DEAD / RESPAWN`

Implemented active transitions already supported by the existing playable prototype:
- construction/spawn completion -> `IDLE`
- `tryMoveMonster()` pursuit -> `CHASE`
- `beginMonsterAttack()` -> `ATTACK`
- resolved/cancelled attack -> `IDLE`
- HP reaches zero -> `DEAD`
- dead tick awaiting prototype timer -> `RESPAWN`
- respawn reset -> `SPAWN -> IDLE`
- player defeat cancels primed monster attacks and returns living monsters to `IDLE`

`monsterAttackReady()` now also requires `ATTACK` state, preventing an attack-resolution call from succeeding solely because timers happen to be ready.

## Evidence / fidelity constraints

No canonical content value was added or changed.

Preserved existing prototype values as `[B]` only:
- attack windup `.24f`
- prototype monster contact damage `4`
- prototype attack cooldown `1.2f`
- chase/engagement geometry already owned by the existing runtime
- prototype respawn timer `4f`

`WANDER` and `DETECT` are declared only to satisfy the runtime contract vocabulary. They are intentionally **not activated** because the Master/design sources do not currently provide sufficiently verified original policies/timings for those transitions.

Monster IDs, stats, EXP, drop tables, spawn relations and original AI values were not fabricated or rewritten.

## Validation

Gameplay commit:
- `d5ee7699969455e786fb8f8bfe557452d46fb7bf` — `Add explicit monster runtime state transitions`

GitHub Actions run #62 (`Validate PROJECT DARK Android`):
- checkout/setup: PASS
- Android SDK/build tools setup: PASS
- `Compile debug sources only`: **PASS**
- overall compile job: **PASS**

No APK packaging was introduced.

## DESIGN_CONFLICT / PENDING

No new `DESIGN_CONFLICT` was introduced.

PENDING items retained:
- exact original monster detection radius and detection transition
- wandering rules, cadence and boundary policy
- attack windup/effect frame timing
- canonical monster damage/cooldown values
- respawn policy/timing
- canonical monster sprites (`PENDING_CROP` where not authenticated)

The existing `[B]` training fixture remains a prototype and is not promoted to an original monster definition.

## Result

The playable combat loop now has an auditable monster lifecycle seam instead of relying only on renderer-side conditional logic. Existing chase/attack/death/respawn behavior still compiles and runs through the same RuntimeState APIs, while invalid attack resolution outside `ATTACK` is explicitly rejected.

## Next Combat · Monster bottleneck

Highest priority after the RPG reward contract is available:
1. extract GameView-owned monster detect/chase/attack orchestration into a dedicated `MonsterAIController` that drives these explicit states;
2. connect `MONSTER_DEFEATED` to the canonical reward-result contract without inventing EXP/drop values;
3. consume Master-backed Monster/Drop IDs when repository canonical data exposes them through a runtime loader;
4. keep WANDER/DETECT behavior PENDING until evidence-safe policy exists rather than hard-coding plausible original rules.
