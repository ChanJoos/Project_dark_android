# V214 device performance regression

V213 DEVICE_FAILED_USER_REPORTED: game too slow to play. User reviewed exterior; no new exterior changes requested. Phone root cause remains UNKNOWN until candidate feedback. Priority: continuous whole-game performance; named shield rendering secondary.

Baseline source627efe6 / branch codex/rucesion-coast-v157 / main41fe6def. Reproduced code issue: every drawHud creates the complete 79+ quest journal twice, including locale number formatting of all future rewards, item reward maps, full story/instruction strings and repeated status projections. V214 computes one compact HUD projection and keeps full journal construction on explicit journal/input paths. HUD title/objective/status/progress/badge semantics must match full journal after live changes. No TTL/stale cache. Map density/art/resolution, movement, combat, reward and save rules preserved. Source/read/build/native/phone gates separate.

Shield diagnosis: fourteen named IT_SHIELD_* definitions intentionally have appearanceId=null (SourceEquipmentPerformance); earlier tests explicitly assert this source gap. Stats/icons equip, but no worn sprite route. Three ms001-003 have registered sprites. V212 recovery handles only inventoryOnly catalog rows and contains no named shields; it did not repair these14. Exact-name source identification remains pending; do not attach an arbitrary ms sprite. User's equipped identity unknown.

Pending: exact-source focused+common CI, paired full-journal/HUD CPU measurement, whole moving-frame tail metrics, inspect candidate capture/APK, phone comparison. Existing V213 APK retains previous proof and failure report; no claim of handset fix yet.
