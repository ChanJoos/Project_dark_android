# Mine doorway arrival candidate — 2026-10-24

User reports appearing among monsters on entry and asks to arrive immediately in front of the mine doorway. Base main bd060ac85f39ebc2b2589a775f54c9930b206f71 (after merged V266).

Changes: source grid arrival (12,35), adjacent to retained exit (13,35); arrival is not the gate trigger. Deliberate one-tile return is armed outside the 14px trigger without requiring a 64px detour. Remove the old ten-species welcome ring. Preserve all 100 actors and ten species, placing initial/respawn homes at least 360 world pixels from the doorway. Ordinary pursuit, damage, rewards, source map pixels, collision grid, renderer/cache, save schema and other maps remain unchanged. This is a user-requested mobile arrival policy, not an original-server fact.

Changed behavior scope: MineHuntGeometry, MineHuntMonsters, the mine-only GameView gate branch. Test new entry/idle/respawn/exit/reentry paths and existing mine navigation/save/combat/render-cache tests. Update V209's superseded entrance-showcase expectation; source monster identity remains checked. No full-regression pass is claimed; existing main automatic failures are outside this focused candidate.

Local source-cell check: entry and exit walkable and one step apart, 9468-cell connected component retained, 9218 cells outside spawn exclusion. Terrain composite inspected at actual doorway source cells; not an Android runtime screenshot. `git diff --check` passed. Native/Android build verification pending CI. No main merge or handset acceptance.

## Exact candidate verification

Source `8be211a3ccffca6d0bb73071c0994ec3218d015b`, [Actions40061780890](https://github.com/ChanJoos/Project_dark_android/actions/runs/40061780890), 15 native checks passed with no failures/errors/skips. Exact embedded source/run, ZIP/APK CRC, SHA256 and APK v2 signature verified; three actual GameView doorway screenshots inspected. First run failed eight test fixtures; no failed result is reclassified. Receipt: `verification/MINE_ENTRY_20261024_BUILD.json`. Candidate retains `com.projectdark.mobile.v266test`; source/hash distinguish it from accepted V266. Physical-device acceptance and main merge remain pending. Next: recording worker install exact candidate and verify real mine entry before capture.
