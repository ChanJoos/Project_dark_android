# DEV HISTORY — PASS 37 WORLD MAP EXPANSION

Date: 2026-09-10
Owner: World / Map Engine
Branch: `agent/world/20260910-1847`
Draft PR: #61
Code commit: `de9ea41994c0bdf5a81789f0481a4c1bcf36c649`

## Canonical change response

The latest Design Constitution now explicitly states that the current expanded prototype remains too
small and must continue growing. PASS 37 prioritizes physical world expansion over the previously
planned decoration-only pass.

## Implemented

- Expanded world bounds from 1600×1120 to 2240×1552 logical units.
- Added east market road/square, east outer lane, south commons and a new outer south gate.
- Relocated the target-pending south portal to the new boundary without inventing its destination.
- Added market and commons NPC/exploration anchors.
- Expanded collision-aligned structures from 11 to 20.
- Grew generated isometric tiles from 1,691 to 3,312.
- Added four-edge transition masks; 710 tiles now expose surface boundaries.
- Added 26 non-collision decoration placements: trees, fences, signs, well, benches and lamps.
- Extended the existing Canvas map renderer to draw transition edges and decorations.

## Evidence

All added geometry and fallback visuals are `ADAPTED/B`. Asset refs remain `PENDING_CROP`.
No original Milles geometry or redistributable Nexon art is asserted.

## Verification

- Isolated Java compilation: PASS via JDK compiler module with Android graphics/runtime stubs.
- `MapExpansionAudit PASS`.
- Generated totals:
  - 3,312 tiles
  - 2,123 GROUND
  - 640 ROAD
  - 527 PLAZA
  - 22 GATE
  - 710 transition tiles
  - 20 structures
  - 26 decorations
- Full Gradle/APK/device runtime visual verification: pending Director integration.

## Boundaries

No GameView, CharacterRenderer, Combat, RPG/progression/save, HUD, dialogue/quest or APK packaging
files were changed.
