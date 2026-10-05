## 2026-10-05 — V105 final CI / public APK closure

User-authorized implementation and candidate publication completed. APK source commit `5ebf1fc2c7fd07bc004b4863f86dd9204edfdadd`; Actions run 37254439040 / job 111588416336 completed SUCCESS. The full configured 248-test Gradle suite and APK assembly passed. Build completion from Actions log: 2026-10-05 11:30:40 KST.

Public candidate: [PROJECT_DARK_V105.apk](https://github.com/ChanJoos/Project_dark_android/releases/download/candidate-v105-5ebf1fc2/PROJECT_DARK_V105.apk) (52869317 bytes). GitHub release metadata SHA-256: `32c95a7dcf5f397f9f829776a48f25ddf7cf9b7cb3475ce10ece571c27cad2a8`. Release tag `candidate-v105-5ebf1fc2`. This is a server-reported digest; final binary/signature inspection and chat attachment saving could not run because the execution environment disconnected.

Five normal class simulations each reached Lv40, completed 23 quests and performed 9 valid skill practice actions. Functional route, rewards, gear, NPCs, supplies, altars and schema4 save transactions are implemented. Phone installation and user visual acceptance remain PENDING. Original unique terrain/full Piet town/new indoor counselor building/timed trial/unique strengthened monster art remain proposals. Main remains `bfd668d4e178fa82625d634b5a54be0e27ce30a3`, unmerged. This closure commit changes documentation only; APK source remains the commit above.

Detailed evidence: [verification/PROJECT_DARK_V105_VERIFICATION.md](verification/PROJECT_DARK_V105_VERIFICATION.md), [verification/LV40_CAMPAIGN_V105_BUILD.json](verification/LV40_CAMPAIGN_V105_BUILD.json).

# Lv40 campaign implementation candidate — V105

User explicitly approved continuing all implementation and publication in the active 2026-10-04 turn. The earlier public-push approval block is superseded; no force push/main merge is authorized by this record. Base remote branch: `065a8202c30661e5853f88061b0a3c9f039f3796`; main remains `bfd668d4e178fa82625d634b5a54be0e27ce30a3`.

## Implemented

- `CampaignProgress`: 20 common + 15 job definitions, 23 per character. Actual accepts, shared-ledger instance defeats, capped colour-pair objectives, successful skill-effect actions, fresh purchase/sale/consumable/equipment supply checks, regional visits, three single-use essence altar transactions, elite fight and final Lv40 report.
- Original first two quests and level curve retained. Once-only common quest EXP totals 3,600,000. Level 1→40 costs 9,000,000. `normalExp` is EXP **inside the current level**, never cumulative. Resolver simulation computes cumulative earned EXP from the unchanged curve.
- Five Lv3 basic jobs, free registered source starter gear/first skill, class mentors, free core skills and armour at practice milestones. Optional ADAPTED Lv11/26 tools cost400/1200G; armour600/1800G; martial tools are gloves, not swords. Existing source atlases/IDs are used without recolouring. Learned records preserved; matching normal jobs use their skills; explicit test mode remains separate.
- Four distinct forest actor instances and a safe Piet camp use the existing Pote terrain shell, World movement/navigation/camera/collision and shared combat. Connected tiles, four open neighbours and spacing prevent unreachable creek-bank spawns. Both route gates and every NPC/species have real path tests. This is a **reused adapted map shell**, not four original reconstructed maps or a full original Piet town.
- Six retained monster concept families imported byte-for-byte,72 poses with SHA256 provenance; existing five current families unchanged. Strengthened variants share their family images intentionally; no unique original variant-art claim. Candidate pose/body/ground-anchor behaviour uses the existing renderer. Original Lv48 Spirit remains outside this chain and its canonical reward is unchanged.
- HP/MP potion inventory/HUD use, 20/25G supply, free NPC recovery, Piet equipment purchase/sale UI, tier stats via FinalStats, simultaneous EXP/Gold/item feedback.
- Schema4 full campaign checkpoints. Atomic job/quest/gear/skills/supply saves; failed transactions restore job including COMMONER, inventory, equipment, EXP/Gold, campaign, skills and HP/MP. Full reward stacks keep the quest pending; duplicate report/altar events cannot grant again. Existing/future malformed saves remain fail-closed.

## Verification and scope

`CampaignProgressTest` starts each job with actual prologue/training Resolver kills and turn-ins, then levels through actual combat/quest EXP. No forced EXP, infinite MP or test skill access. Each class completes23quests,9valid practice actions and reachesLv40 with nonnegative Gold after core tool purchases. Combat placement/rest calls are fixtures: this is a normal-resource **domain simulation**, not a phone input run or measured 150-minute clear.

`CampaignNavigationTest` proves every campaign NPC/species/gate path, initial/reverse arrival, safe-camp zero enemies, town mentors/counselor accessibility. `CampaignUiTest` uses actual choice buttons, supplier dialog button/native shop, map render/cold restart. Existing rendering/combat/inventory/save/quest tests are rerun, separately from physical phone and user visual acceptance.

Build/artifact exact commit/run/hash evidence is recorded only after CI succeeds. Before that: IMPLEMENTED_CANDIDATE, focused runtime verified; build delivery pending. **PHONE and USER_VISUAL remain PENDING.**

## Remaining design proposals

The outdoor counselor and spaced mentors implement acquisition; a new indoor counselor building and optional timed job trial are not implemented. No historical CON/WIS growth is recomputed; existing growth and spent stats are preserved. No measured phone completion time, full Piet town, new original monster animation frames or user visual approval is claimed. These presentation expansions are separate from the executable Lv40 route.
