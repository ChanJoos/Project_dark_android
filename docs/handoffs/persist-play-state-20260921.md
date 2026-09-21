# Play-state persistence continuation — 2026-09-21 KST

Base: `d7746e414566a464346b4c578291cf41e0400e24` (main).
Branch: `director/persist-play-state`. Owner: Director/integration, single editor.
The interrupted Sep13 draft is preserved in the local git stash and is superseded by this implementation on current main.

## Runtime changes

- F5mSaveStore retains the existing preference slot and migrates legacy saves to schema 2. It stores all owned item IDs/quantities and the exact equipment map, including explicitly empty inventory/equipment. Unknown saved content/newer schemas disable writing and preserve source data.
- GameView binds its RuntimeState and both quests to one checkpoint. All defeat consumers finish before the checkpoint; periodic position/resource saves occur every two simulated seconds, with immediate ledger-change, equipment, revive and pause checkpoints. Pause drains pending defeat events before saving.
- Position restores through the existing WorldRuntimeAdapter traversal/tile-snap validation. Current HP/MP and dead state restore after derived maximum resources are applied. Map identity guards coordinate restoration.
- CombatLedger and reward/quest consumption watermarks persist together. A restarted runtime clears its event queue but allocates future events above the persisted watermark. Monsters start fresh; transient combat animations/cooldowns and individual monster state are not persisted.
- Both quest turn-ins stage rewards in a separate RPG state, commit completion plus full RPG progression/ownership in one preference editor, then update live state after success. The prologue coordinator no longer reloads an old save over live stats before granting rewards.
- Save failure feedback is visible; retries are throttled to the periodic checkpoint instead of repeating every frame after failure.

## Verification completed locally

- Java 17 compilation of all production Java sources against Robolectric's Android 14 framework: PASS (only existing deprecation notes).
- JUnit + Robolectric 4.14.1 / SDK 34: **17 tests PASS**:
  - RuntimeCheckpointTest: 12 (full/empty ownership, explicit unequip, legacy migration, unsupported/unknown preservation, GameView pause/recreation position/resources, death, immediate defeat/pause and replay/new-life reward, atomic quest2 growth, prologue live-stat preservation, rejected commit, injected failed disk commit + retry, quest watermark).
  - F5mRestartPersistenceMatrixTest: 4.
  - F5mRuntimeBindingTest: 1.
- `python tools/validate_master.py`: PASS, errors empty; 101 pre-existing preserved findings remain.
- `git diff --check`: PASS.
- F5mProductionQuickQuestRouteAudit: FAIL both before and after this change using the same Android framework classpath. Baseline was independently compiled from exact main d7746e4. This is not a new persistence regression; do not claim the movement gate passes.

Robolectric tests recreate the GameView/save-store/runtime, not an actual device OS process. No physical-device restart, visual acceptance, Gradle APK assembly or exact-head GitHub CI has been verified in this turn.

## Remote blocker and next action

Auto-review rejected pushing this branch to `https://github.com/ChanJoos/Project_dark_android.git`, including a retry after connector verification of the authenticated account, repository ownership and write permissions. Its stated remaining requirement is explicit user approval for pushing these source/workflow changes to that destination. No alternate upload route was used. Changes and tests are committed locally.

After explicit approval: check latest main and reconcile any concurrent delta; push this existing branch; open a PR; run the existing Android CI with the added RuntimeCheckpointTest step; investigate any failed mandatory gate before main integration; deliver the exact-SHA APK with KST build time. Do not merge on the basis of local javac/Robolectric alone.

## Device acceptance after APK is available

1. Install over a compatible existing save; verify previous EXP/Gold/quest state is retained.
2. Unequip shirt and weapon, move to a different tile, lose HP/use MP, background and restart. Verify ownership, unequipped state, position and HP/MP.
3. Kill the training target and immediately background/terminate; restart, verify one reward, then kill a new life and verify one additional reward.
4. Complete quests 1 and 2; restart and check completion, EXP/Gold, stat points and HP/MP maxima. Repeated confirmation must grant nothing.
5. Die, restart, verify dead-state UI remains until explicit revive; check movement/NPC/quick quest separately against its known baseline issue.
