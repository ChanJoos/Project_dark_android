# PROJECT DARK — Classic Milles Reagent Shop Contract

Status: IMPLEMENTATION SSOT
Target era: classic/legacy Legend of Darkness presentation and interaction semantics.

## Player-approved gameplay contract
1. The Milles reagent-shop front door is a portal trigger.
2. Entering the portal changes to a separate reagent-shop interior map.
3. The interior contains a shop NPC.
4. The NPC is activated by direct click/tap. No proximity auto-open and no dialogue step.
5. The shop inventory for this vertical slice is exactly: 코마디움, 디베노움, 쿠라눔.
6. The interior exit portal returns the player to Milles in front of the reagent shop.
7. Novice keyword/typing transport is explicitly out of scope.

## Evidence
- Nexon community records identify 멀린 as the reagent-shop NPC and explicitly state that Milles is one of the reagent shops where 멀린 sells items.
- Nexon records independently confirm Milles reagent shop as a real shop location.
- Exact classic interior tile layout, legacy shop-window pixels, and classic prices for the three requested items are not yet verified. They must not be fabricated as canonical values.

## Implementation constraints
- Interior/map/portal/collision belong to World, not GameView.
- NPC click dispatch and shop transaction belong to Game.
- GameView only orchestrates input and presentation.
- No auto-approach for the reagent-shop NPC: a tap on the NPC opens the shop immediately.
- No dialogue modal before the shop.
- Unknown historical values remain explicitly unresolved until evidence is found.
- Accepted gameplay freeze 4ffd0ec47e36a828f405e00e75112d187a883223 remains untouched.
