# Mine doorway arrival candidate — 2026-10-24

User reports appearing among monsters on entry and asks to arrive immediately in front of the mine doorway. Base main bd060ac85f39ebc2b2589a775f54c9930b206f71 (after merged V266).

Changes: source grid arrival (12,35), adjacent to retained exit (13,35); arrival is not the gate trigger. Deliberate one-tile return is armed outside the 14px trigger without requiring a 64px detour. Remove the old ten-species welcome ring. Preserve all 100 actors and ten species, placing initial/respawn homes at least 360 world pixels from the doorway. Ordinary pursuit, damage, rewards, source map pixels, collision grid, renderer/cache, save schema and other maps remain unchanged. This is a user-requested mobile arrival policy, not an original-server fact.

Changed behavior scope: MineHuntGeometry, MineHuntMonsters, the mine-only GameView gate branch. Test new entry/idle/respawn/exit/reentry paths and existing mine navigation/save/combat/render-cache tests. Update V209's superseded entrance-showcase expectation; source monster identity remains checked. No full-regression pass is claimed; existing main automatic failures are outside this focused candidate.

Local source-cell check: entry and exit walkable and one step apart, 9468-cell connected component retained, 9218 cells outside spawn exclusion. Terrain composite inspected at actual doorway source cells; not an Android runtime screenshot. `git diff --check` passed. Native/Android build verification pending CI. No main merge or handset acceptance.
