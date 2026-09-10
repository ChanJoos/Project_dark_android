# Character / Animation Handoff

## 2026-09-10 16:05 KST — agent/character/20260910-1605

### Source state
- Continued from draft PR #15 / `agent/character/20260910-1548`; previous Character work remains unmerged.
- Re-verified Nexon's official character-creation guide `https://lod.nexon.com/info/guide/82283` and its official storage image `https://storage.nexon.com/dsk03/13/NX_FILE/Board/65536/05/1/000/00/00/5557536463815442599.png`.
- The screenshot directly shows a male base avatar, two gender buttons, eighteen populated hair cells and fourteen populated hair-colour cells. Exact underlying sprite sheets / palette RGB remain unavailable and are not invented.

### Priority
Character visual identity remains ahead of micro-animation polish.

### Completed this run
- Added `CharacterAppearance` as a stable renderer-owned creation appearance contract: `Gender`, `hairStyleIndex`, `hairColorIndex`.
- Encoded the visible creation UI bounds as 18 hair-style choices and 14 hair-colour choices; indices clamp safely to those visible options.
- Added an `[ADAPTED_FROM_O]` preview hair-colour palette approximated from the official guide swatches. These RGB values are explicitly not claimed canonical game palette data.
- Extended `CharacterRenderer` with an appearance-aware overload `draw(Canvas, Pose, CharacterAppearance)` while preserving the existing `draw(Canvas, Pose)` API. Existing `GameView.java` therefore compiles against the same signature and automatically receives `defaultGuideMale()`.
- Implemented eighteen distinct procedural hair silhouettes keyed by the official creation UI hair index so the renderer now has a real replaceable hair-style contract rather than one hard-coded placeholder.
- Existing BODY/HAIR/EQUIPMENT/WEAPON/EFFECT layer ordering, four-direction presentation, scale/anchor contract and action states remain intact.
- Gender selection is represented in the API, but the official capture only visibly proves the selected male base avatar. Female-specific body pixels remain `PENDING_CROP`; the renderer does not invent a female anatomy/sprite.

### User-visible delta
The currently wired player now uses a guide-backed default male appearance through the unchanged renderer entry point. Hair is no longer one fixed violet placeholder: the renderer supports the eighteen creation-screen hair slots and fourteen visible colour choices through a stable appearance contract, ready for character-creation UI wiring.

### Evidence discipline
- Official guide page + Nexon-hosted screenshot: `[O]` source evidence.
- Base proportions / procedural hair geometry: `[O+B]`.
- Preview RGB palette: `[ADAPTED_FROM_O]`.
- Exact game sprite crops, directional frames, exact palette values and female base-avatar pixels: `PENDING_CROP`.
- Class-introduction key art remains reference-only and is not treated as runtime sprite evidence.

### Integration request
- `GameView.java` remains untouched.
- When character-creation state is exposed by its owning layer, Integrator can call `CharacterRenderer.draw(canvas, pose, appearance)` directly; no renderer refactor should be required.
- NPC/monster direct drawing still needs Integrator delegation to `NpcPresentationRenderer` / `MonsterPresentationRenderer` as previously handed off.

### Validation
- Character-owned changes only: `CharacterAppearance.java`, `CharacterRenderer.java`, `CharacterVisualSourceManifest.java`, this handoff.
- Existing `CharacterRendererAudit` structural contract remains compatible: 4 directions, 7 states, 5 layers and `PENDING_CROP` asset status are unchanged.
- No APK packaging or Director integration performed.

### Ownership boundary preserved
No changes to `GameView.java`, world/map/camera/collision/pathfinding/portal, CombatResolver/MonsterAI/damage semantics, inventory/reward/EXP/save/progression, HUD/input, NPC dialogue or quest state.

### Next Character priority
1. Find a Nexon-hosted capture where the female base avatar is actually selected, then implement female body geometry from evidence rather than inference.
2. Search official Nexon-hosted gameplay/guide assets for real equipment/weapon sprites and map them to known item IDs.
3. If direct sprite-sheet/crop evidence is found, replace procedural hair/body geometry incrementally while preserving the `CharacterAppearance` contract.
