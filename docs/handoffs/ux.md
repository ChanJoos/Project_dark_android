# UX / NPC / Quest handoff

## 2026-09-10 15:01 KST — autonomous UX pass

Branch: `agent/ux/auto-20260910-1457`
Draft PR: #18 `UX: acknowledge monster target selection`

### Canonical state read
- `design/DESIGN_CONSTITUTION.md`: tap-to-move is USER CANON / `[ADAPTED]`; World owns movement/path/collision semantics and UX owns touch routing/wiring.
- `design/DATA_CONTRACT.md`: cross-domain logic must stay behind stable contracts; no retired ground-drop/pickup flow may be reintroduced.
- `design/SOURCE_OF_TRUTH.md`: latest user decisions and evidence hierarchy remain authoritative; auto-loot/direct inventory delivery supersedes old pickup flows.
- Latest main observed: `3b58a8d08805cf9031466fe0c50d56e17aa1344d`.

### User-visible delta completed
- `GameView.java` v0.68 now gives immediate target-acquisition feedback when the user taps a live monster: `타깃 선택 · <monster name>`.
- Existing target selection still routes through `CombatController.selectTarget(...)` and existing target ring/top-center HP presentation remain the source of persistent target state.
- This change only closes the short input-feedback gap; it does not alter targeting/combat semantics.

### Boundary preservation
- Only `GameView.java` was modified for runtime behavior.
- No World/camera/collision/pathfinding/portal algorithm was added.
- No CharacterRenderer, CombatResolver, MonsterAI, RPG/reward/EXP/job/save internals were modified.
- No ground-drop/pickup UI or state was introduced.

### P0 blocker — blank-world tap-to-move still waiting on World contract
Current main still exposes low-level collision-aware movement but no stable camera-aware API for the UX-owned wiring required by canon.

World owner still needs to expose a stable surface equivalent to:
1. current-camera screen -> world conversion;
2. replaceable player move-target/path request;
3. explicit cancellation semantics for joystick/combat/NPC/dialogue/death transitions;
4. read-only active/reached/blocked movement state.

When available, UX should immediately wire blank-world tap -> move-target request -> WALK and draw a brief accepted-target marker, with NPC/HUD/modal/action precedence preserved.

### Other pending contracts
- Quest HUD remains blocked on a stable read-only quest presentation DTO.
- AUTO remains dependent on a stable owning runtime contract; do not invent auto-combat logic inside `GameView`.

### Integration note
Previous UX draft PRs remain separate and unmerged; this branch is based directly on the latest observed main and contains only this pass's target-selection feedback plus this handoff.
