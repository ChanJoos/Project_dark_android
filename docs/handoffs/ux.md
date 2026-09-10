# UX / NPC / Quest handoff

## 2026-09-10 18:16 KST — direct-inventory reward discoverability pass

Branch: `agent/ux/manual-20260910-1816`
Base: latest main `87f5f9de7c060a84aad12a0eebdcc3a5a7b5b7af`.

### Canonical delta check
- Re-read `design/DESIGN_CONSTITUTION.md`, `design/DATA_CONTRACT.md`, and `design/SOURCE_OF_TRUTH.md` before coding.
- Latest canon still requires empty-map tap-to-move, direct inventory reward delivery, four diagonal character presentation, and mobile-only UX adaptation.
- `GameView.java` on latest main is already v0.69 and contains empty-map tap movement end-to-end, so UX did not reimplement it.
- The constitution still describes empty-map tap movement as a current playtest gap; code is ahead of that prose and the implementation is already present on main.

### Repository changes since previous UX pass
- Director integrated shared combat orchestration work (`CombatActionOrchestrator`, actor-scoped resolver seams) and `MonsterAutoCombatBridge`.
- `MonsterAutoCombatBridge` is a combat-owned adapter for MonsterAI AUTO attack intent. It is NOT a player AUTO-button presentation/runtime contract, so UX did not bind the player AUTO button to it.
- No stable player action presentation DTO with `actionId/iconKey/resourceCost/cooldown/learned/enabled/disabledReason/selected` was found on main.

### User-visible delta completed
- Startup onboarding now also states `처치 보상: 인벤토리 자동 지급`.
- This aligns first-launch guidance with the canonical retired-ground-loot model and directs playtesters to validate reward delivery through inventory rather than looking for floor drops.

### Existing live reward surface on main
- `RpgInventoryPresentation.latestRewardNotice(...)` is already consumed by `GameView.drawInventory(...)`.
- Inventory UI can show whether the latest reward is resolved/PENDING and whether auto-looted item entries exist.
- The next UX pass should improve post-defeat immediacy by surfacing newly changed reward notices outside the inventory panel, but must not mutate RPG reward state or invent unresolved rewards.

### Open contract request
RPG/Combat should expose a stable read-only player action presentation contract for ATTACK / SKILL / MAGIC / AUTO containing at least:
- `actionId`
- `label`
- `iconKey/visualRef`
- `resourceType/resourceCost`
- `cooldownRemaining/cooldownTotal`
- `learned/unlocked`
- `enabled/disabledReason`
- `selected/active`

Do not use `MonsterAutoCombatBridge` as the player AUTO contract.

### Next UX work
1. Re-check canonical docs and main first.
2. If player action DTO exists, bind mobile quick-slot press/cooldown/disabled/selected states immediately.
3. Otherwise, continue direct-inventory reward visibility: detect a newly published `RewardNotice` by combat sequence and show a short non-blocking reward banner/feedback outside the inventory panel, using only `RpgInventoryPresentation` read-only data.
4. Keep tap-to-move regression checks but do not duplicate its implementation.

### Boundaries preserved
- No World pathfinding/collision/portal code changed.
- No CharacterRenderer internals changed.
- No CombatResolver/MonsterAI/damage code changed.
- No RPG inventory/reward/EXP/job/save internals changed.
- No canonical values changed.
- No ground-drop/pickup UX introduced.
