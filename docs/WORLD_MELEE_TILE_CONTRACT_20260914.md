# World melee tile contract — 2026-09-14

Device feedback supersedes radius-based melee acceptance.

## Canonical spatial contract
- Authored logical tile: 64x32.
- Adjacent melee endpoints only: NW (-32,-16), NE (+32,-16), SW (-32,+16), SE (+32,+16).
- SWING / THRUST / PUNCH use exact adjacent-tile reach.
- THROW and non-melee skill/magic retain their explicit separate range contracts.
- Melee reachability and melee facing are derived from the same endpoint delta.

## Runtime rules
- Monster attack start requires exact adjacent-tile reach; Euclidean `d <= 42` is not an eligibility rule.
- Primed monster attack cancels if adjacency is lost before resolution.
- Legacy and shared-resolver monster routes both fail closed on adjacency before damage submission.
- Player melee `CombatController.inRange/attackInRange` uses the same canonical adjacency contract.
- Melee auto-approach targets one of the four exact adjacent authored centers, not an arbitrary center inside a radius.

## Regression gates
- Four accepted adjacent deltas map 1:1 to NW/NE/SW/SE facing.
- Cardinal authored-center deltas such as (0,+32), (0,-32), (+32,0), (-32,0) are rejected for melee even though a scalar radius could accept them.
- Existing player/monster tile-center movement, atomic collision, zero-drift and attack-facing lock gates remain required.

BUILD VERIFIED and DEVICE VERIFIED remain separate release gates.
