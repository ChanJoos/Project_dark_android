# 2026-09-22 device recording repair

Input: `Screen_Recording_20260922_104456.mp4`, 20.119 s, 2340×1080.
Baseline: main `a5527c77bd0a91d355c1035274b25690ba227ed6`.

## Observed and traced

- Clothing flickers and floats away in walking/combat. Packed shirt rectangles ignored atlas padding and nonsequential packing. The renderer also scaled raw shirt pixels independently of the already baked BODY, and interpreted source bottom offsets as top offsets.
- Weapon-equipped contact remains an idle body. Shipped legacy shirt action bitmaps did not match the dimensions demanded by the loader, so the renderer silently rejected the entire action composite.
- Combat damage occurred on touch, before contact. GameView bypassed the existing shared resolver and selected arbitrary attack modes independently of equipment.
- Quest acceptance leaves the guide dialogue open; inventory exposes internal asset-evidence labels and overlays raw IDs on item names.
- The attack control center was x=958 in a 960-wide viewport, clipping most of the button.
- Inspection of movement found an inverted obstacle-bottom comparison, and monster approach could stop at a nearby but illegal melee tile. Initial monster fixture positions were not tile-centered until the first AI tick.

## Implementation

- Use exact recovered mu0000001 rectangles. Register clothing relative to its matching mm001 BODY frame, with the accepted BODY scale, foot and facing. Crop action layers directly from the existing verified atlas. Preserve all original image files and the accepted body silhouette. Restore full-body robe rendering in idle/walk. Keep source paper doll for unresolved cast/skill/death rather than switching to a procedural character.
- Submit player actions through RuntimeCombatSession. The equipment semantic chooses the basic attack; damage is deferred to contact and spatial legality is checked again at contact. Starting commoners cannot use unlearned prototype skills. Monster legacy routing is unchanged in this repair.
- Restrict melee to four diagonal neighboring tiles; align approach endpoints with this contract. Center Milles monster fixtures at spawn/respawn. Correct obstacle overlap; add obstacle LOS to player combat.
- Close and checkpoint successful quest acceptance. Clean presentation names without deleting provenance. Bound item names and separate quantity/equipped labels. Move the full attack touch region inside the viewport. Preserve movement while inventory is open.
- Make the obsolete branch-mutating Finalize Rewards workflow manual; Android validation remains automatic on pushes.

## Evidence and limits

`DeviceRecordingRegressionTest` exercises actual GameView input/submission, delayed damage, exactly-once hit, contact range revalidation, dialogue dismissal, collision, route and button bounds. `ShirtRenderingRegressionTest` draws the shipped bitmaps through native Android Canvas in all four directions, idle, four walk frames, contact, recovery and cast fallback; checks garment visibility/location and that contact differs from idle; exports `character-matrix.png` for inspection. Existing quest/restart regression gates remain enabled.

IMPLEMENTED: fixes above. BUILD VERIFIED / automated RUNTIME VERIFIED: recorded by the exact commit CI run and local test output. PHYSICAL DEVICE VERIFIED / VISUAL ACCEPTED: pending installation of the new APK. The group-02 source is still one directional contact pose, not a recovered full temporal animation. NPC/monster placeholder art and original asset-source gaps are not claimed resolved by this code repair.
