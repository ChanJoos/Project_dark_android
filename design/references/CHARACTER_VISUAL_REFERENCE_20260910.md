# PROJECT DARK Character Visual Reference — 2026-09-10

Status: USER_APPROVED_VISUAL_DIRECTION / ADAPTED_REFERENCE

This document records the exact visual direction shown in the user-approved character concept board created in the project chat on 2026-09-10. It is a design reference, not authenticated original Legend of Darkness source art. Original-game assets still require provenance and remain PENDING_CROP until verified.

## Non-negotiable user decisions

- Player render scale is fixed at **1.50**. Do not reduce it without a newer explicit user instruction.
- The previous procedural character became too ugly: arms appeared detached from the torso and the silhouette felt fragmented. This must be corrected.
- Characters must read as one coherent body: head, torso, shoulders, arms, hands, legs, equipment and weapon need continuous anatomical attachment and intentional overlap.
- The desired look is compact, attractive, readable pixel-art inspired by the original Legend of Darkness visual language, not generic geometric primitives.
- Movement/presentation is isometric/diagonal, with **NW / NE / SW / SE** directional variants.
- The character should appear side-oblique rather than front-facing while moving.
- Directional consistency must hold across BODY / HAIR / EQUIPMENT / WEAPON / EFFECT layers.

## Reference-board visual target

The approved board depicts a dark-background sprite-sheet concept with compact chibi/pixel characters. Core traits:

1. **Proportions**
   - roughly 2.5–3 heads tall;
   - slightly oversized head relative to body;
   - narrow shoulders but arms visibly attached at shoulder sockets;
   - short legs with clear feet/shoes;
   - no floating limbs or visible gaps between arm and torso except deliberate animation separation of one or two pixels.

2. **Silhouette**
   - readable at mobile scale;
   - diagonal body axis;
   - near/far arm and near/far leg overlap according to facing;
   - weapon hand aligned with wrist/forearm;
   - hair wraps the skull rather than looking like a rectangular cap;
   - face uses minimal but expressive pixels.

3. **Directional sheet**
   - SW, SE, NW, NE variants for IDLE, WALK, ATTACK, SKILL, HIT, DEAD;
   - WALK uses alternating near/far leg movement while preserving torso cohesion;
   - ATTACK extends from shoulder → arm → hand → weapon as one connected chain;
   - SKILL effects arc from the actor and do not visually detach the body;
   - HIT is primarily recoil/flash, not body disassembly;
   - DEAD collapses/rotates the full body as one silhouette.

4. **Class/equipment feeling**
   - warrior: dark/navy outfit, brown hair, sword/shield, compact armored silhouette;
   - thief: darker agile silhouette;
   - mage: robe/hood and staff/orb style silhouette;
   - priest: light robe/staff silhouette;
   - equipment should be layered over a stable body rig, not replace limbs with disconnected rectangles.

5. **Mobile presentation**
   - preserve crisp pixel readability;
   - avoid overly thin one-pixel limb gaps at runtime scale;
   - selection ring/shadow must stay subordinate to body;
   - logical foot anchor/collision position must remain independent from render scale.

## Implementation rules

- Prefer verified original Nexon/LOD assets whenever exact source frames can be authenticated.
- Do not claim generated/procedural art as original. Use `ADAPTED` / `B` / `PENDING_CROP` provenance correctly.
- Until verified source sprites are available, procedural fallback must follow the visual target above and prioritize cohesive anatomy over abstract rectangles.
- Maintain a reusable directional binding architecture so verified original sprites can replace fallback art without changing gameplay coordinates or state contracts.
- Any change to `CharacterRenderer.PLAYER_RENDER_SCALE` must preserve **1.50** unless superseded by an explicit newer user decision.

## Acceptance gate for Character agent

A character visual pass is not complete merely because it compiles. Before handoff, inspect all four directions at IDLE and WALK and at least one ATTACK frame and verify:

- both arms visually attach to the torso/shoulders;
- hands connect to weapon/equipment;
- legs originate from the pelvis/torso with intentional overlap;
- head/hair/face align to one diagonal pose;
- no body part appears to float;
- silhouette is attractive/readable at scale 1.50;
- NW/NE/SW/SE are visibly distinct;
- no unverified procedural sprite is labeled as original.

If any of these fail, continue the same visual task before starting unrelated Character work.
