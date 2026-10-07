# V113 Pote reference and occlusion repair

V112 (`280e5bb9738de7e97935e4c292c1de73865854da`, local APK only) is USER_VISUAL_REJECTED: river/path/map feel and hidden actors. User explicitly approves GitHub publication/CI on 2026-10-07. The prior local source workspace is no longer present; this repair is based on the current V111 main `18ef615b54833e2155b103cffbabdac304821a23`. Retained V112 APK supplies only the 47 byte-verified recovered equipment source files for local build; none of its terrain screenshot is used as a map.

## Consumed references and limits

- Supplied `1000056919.mp4`: 0/5/9/15/22/27 seconds; original 3-L/2-L trees, soil, flowers, encounter clearings and broad earth lanes.
- Supplied `1000056952.mp4`: 0/8/16/25/32/44 seconds; original 2-C/2-L woodland and a visible partially transparent tree while the actor is behind it.
- Earlier supplied `03-Screen_Recording_20260915_003701_YouTube.mp4`, 20 seconds: original-client blue water and rocky banks at Milles. This is a material reference, NOT evidence that Pote has the same river geometry.
- Tistory https://minimob.tistory.com/127 and https://rockerse.tistory.com/5 support field hunting and dense 2-C hunting context. Naver 성천직자 public page was robots-blocked in this run; no claim of full cafe access.
- `docs/visual/POTE_FOREST_DESIGN_V1.md`, governing data/source contracts and Master manifest, contributor/director/monster manuals, current renderer/geometry/runtime/tests and user rejection.

The source videos are not published. Derived assets carry original-video hashes, capture times, inspection-projection/crop/mask coordinates and per-output hashes in `assets/pote/production/reference_v113/provenance.json`. Trees preserve exposed original pixels; holes/captured soil are masked, occluded/unrecoverable pixels are not invented. Floor is small clean source patches combined as a wrap texture, never a monolithic screenshot. Exact native map layout, original server population and respawn values remain UNRESOLVED; these are explicit mobile ADAPTED values.

## Runtime contract

- A/B/C bounds are 4096x2048 / 4352x2176 / 4864x2432 maxima from64. D3072x1536. Four diagonal32x16 steps,0.60s, approved BODY/equipment/current movement retained. Ground64x32 diamonds join over shared source texture; navigation and river render consume one smooth sampled channel/width profile.
- Broad source-earth patches feather into leaf-litter soil; no gold road stripe. Variable-width blue source current uses world-anchored texture drift. Continuous source stone bank material and sparse source rock cutouts replace pale flat water and repeated outlined water sprites. Bridge and established portal bitmap are retained. Macro forest islands differ by map; grass/flowers/source trees are independently layered.
- Terrain/props/player/monsters/NPCs sort together by each object's own foot Y. A foreground tree checks its actual alpha pixels against each live actor's body bounds; overlapping trees render at64/255 opacity. Other trees stay opaque. No player-only depth partition or blanket fade. Actor names/health remain part of the actor draw.
- Manual AUTO acquires reachable living monsters across the active whole map. Quickquest activates a filter for the active KILL/PAIR ID; manual AUTO clears it; objective completion stops that hunt and routes to report. Shared multi-goal BFS chooses legal diagonal melee neighbour using full actor/terrain edge rules. No per-monster per-frame A* loop; no-target retry0.5s.
- A72/B96/C108 actors distributed over the connected map, same ordinary species/profile IDs and90px initial spacing. ADAPTED respawn6/7/8seconds through actual RuntimeState timers. D stays one Mantis with infinite respawn and existing Lv41/reward save transaction. No ground drops or new reward formulas.

## Verification and remaining acceptance

`PoteReferenceV113Test` exercises real quickquest/manual AUTO, far filtered target, live death/respawn and populations,20 native production views (4spots x4maps +player/monster/NPC behind trees +actor-free control), and an actual foreground pixel transmission check. Existing campaign navigation/save/progression, quickquest/Shortblade, forest movement/monster runtime and source-asset tests are required. Native Robolectric is NOT a physical phone. Implementation/build/native evidence is recorded separately; PHONE and USER_VISUAL remain PENDING. Original-perfect map topology/art animation is not claimed.


CI follow-up: first candidate `ed587523` passed all42 focused tests, but exact APK gate caught the established portal stored under AssetManager `assets/world/portal/` (ZIP `assets/assets/world/portal/`), not `world/portal/`. Renderer and APK verifier now consume the real retained path/bytes; a native portal-pixel assertion guards blank portals. Full run37625957529 caught an obsolete HudTouchAcceptanceTest radius limit; it is changed to assert remote reachable acquisition and remote unreachable rejection per current user request. Central foreground actor fixtures now explicitly assert opaque foliage overlap and include an actor-free control frame; publication/build evidence pending this follow-up.
