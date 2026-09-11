# Character / Animation Handoff — CURRENT

Updated: 2026-09-11 KST
Canonical base: current `main`

## Current canon

Latest explicit user direction is authoritative: the starter character is **평민 / PEASANT (pre-class)**, not 무도가.

Core runtime contract remains:
- source frame 24x32;
- runtime scale 1.50;
- exact diagonal facings NW / NE / SW / SE;
- one logical world step remains 0.80 sec;
- WALK animation uses 4 frames at 5 fps so one full walk cycle is 0.80 sec;
- no direction may be synthesized only by mirroring another direction;
- common frame-bottom foot anchor is mandatory.

## Starter character architecture

The starter renderer must never silently reuse `player_martial_*` assets. Martial artist is a later class/equipment appearance, not the base avatar.

Paper-doll composition order is fixed as:
`BODY_BASE -> HAIR_STYLE -> HAIR_COLOR -> HEAD -> TOP -> BOTTOM -> GLOVES -> SHOES -> WEAPON -> OFFHAND`.

The base must remain class-neutral enough that later class/equipment layers can replace appearance without changing movement geometry, frame size, direction mapping, or foot anchor.

## Runtime resource policy

`CharacterRenderer` now dynamically looks for:
- `player_peasant_idle_walk` (120x128; 5 columns x 4 rows)
- `player_peasant_attack` (96x128; optional/future)

The lookup is dynamic so `main` compiles before the production peasant atlas lands. Missing/corrupt/shape-invalid peasant resources must not crash startup.

Legacy martial resources may remain in the repository for history/reference, but the starter renderer is forbidden from resolving or displaying them.

## Temporary safe fallback

Until the reference-accurate peasant atlas passes the visual gate, runtime uses a neutral peasant safety fallback. It intentionally removes the previous martial-artist markers:
- no silver/white martial hair contract;
- no red headband;
- no martial dark-navy costume contract;
- no shield;
- no martial weaponless-profile flag.

This fallback is **not production art**. It exists only so the APK stays playable while the P0/P1-based peasant sprite is being reconstructed.

## Visual acceptance gate for the real peasant atlas

Do not promote a generated/reference reconstruction just because it is technically 24x32. It must pass all of these:
1. reads as old `어둠의전설` before generic retro MMORPG;
2. reads as 평민/pre-class rather than 무도가 or another class;
3. four rows are genuinely NW/NE/SW/SE 3/4 views;
4. body silhouette stays connected in every IDLE/WALK frame;
5. feet stay on the common anchor across all 20 cells;
6. no modern chibi proportions;
7. no Mir2/other-game visual vocabulary;
8. at gameplay scale, the player remains small relative to the world as in the P0 screenshots.

## Direction mapping

Runtime physical atlas rows stay:
- row 0 = NW
- row 1 = NE
- row 2 = SW
- row 3 = SE

`visualFacingForRow(row)` must remain reciprocal to `atlasRow(Direction)`.

## Current implementation status

- PEASANT/pre-class is now the canonical starter profile.
- Martial assets are explicitly disabled as starter fallback.
- Paper-doll order is encoded in runtime contract.
- WALK frame cadence is synchronized to the 0.80 sec tile step.
- Crash-safe dynamic peasant resource lookup is implemented.
- Production peasant PNG atlas: **NOT YET ACCEPTED**.
- ATTACK/SKILL/HIT/DIE replacement: **DEFERRED** until IDLE/WALK visual acceptance.
- Runtime device acceptance of the future real peasant atlas: **NOT CLAIMED**.

## Next gate

Produce and validate one reference-accurate `player_peasant_idle_walk.png` at exactly 120x128 (24x32 x 5 columns x 4 rows). Only after side-by-side acceptance against P0/P1 Dark Ages references should it be wired as production art. Do not spend the next iteration on combat animations or class-specific equipment.
