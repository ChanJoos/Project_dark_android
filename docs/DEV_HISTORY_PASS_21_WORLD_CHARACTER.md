# PROJECT DARK — DEV HISTORY PASS 21 · WORLD / CHARACTER

Date: 2026-09-10
Role: World · Character

## Source-of-Truth gate executed

Read current GitHub `main` before implementation in the required order:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md` (M001 / D003)
6. `docs/DEV_HISTORY.md` and latest World PASS (`docs/DEV_HISTORY_PASS_20_WORLD_CHARACTER.md`)

Auto-loot user canon remains unchanged; this pass introduced no ground-drop/pickup behavior.

## Nexon source mining completed

Expanded the Milles source pool beyond the unresolved 2021 nine-image old-town set.

New positively labelled sources recorded:
- 2005 `밀레스의 노숙자들` — historical Milles scene `[V]`.
- 2004 `헉...길레노아 실종사건` — historical prose places Rosemary in front of/in the yard of Gillenoa's shop; cross-check candidates `NPC_GILLENOA_MILLES`, `NPC_ROSEMARY`, `LOC_MILLES_ROSEMARY (81,43)`. This is relative landmark evidence, not an exact transform anchor.
- 2022 `[세오200년] 밀레스 조각상과 함께 기원` listing — labelled Milles statue candidate `[V]`.
- 2024 Milles village and Milles Park screenshots — modern/event visual references `[V]`.
- 2024 official Nexon 30th-anniversary event page `[O]` — explicitly states a `밀레스마을(61,119)` portal leads to `밀레스공원` and publishes images with that instruction.

The official `(61,119)` fact is version-scoped modern evidence and is NOT mixed into the legacy old-map transform without compatibility evidence.

## Implementation completed

### 1. Expanded `NEXON_VISUAL_SOURCE_MANIFEST_MILLES.md`

Added the new source registry and a calibration-candidate table separating:
- Master legacy location coordinates;
- historical relative landmark evidence;
- modern official coordinate evidence;
- event-map references.

No candidate is currently marked `CALIBRATION_READY`.

Commits:
- `9756ff0d8158e4c6d614d86897b0d4fbf7a6cd99` — initial expanded registry.
- `5c23aef561dacd5228db8977bce513d8debfb895` — cleaned the attachment registry before runtime integration.

### 2. Hardened `MillesTraceContract`

Updated `app/src/main/java/com/projectdark/mobile/MillesTraceContract.java`.

Added:
- `SourceEra { LEGACY_OLD_MAP, MODERN_MAP, EVENT_MAP, UNKNOWN }`;
- `AnchorStatus { PENDING_PIXEL_IDENTIFICATION, VERIFIED }`;
- provenance-carrying `CalibrationAnchor`;
- `hasCalibrationEvidence()` gate.

Calibration now requires at least two verified `LEGACY_OLD_MAP` correspondences with distinct tile and visual positions. Modern/event anchors are deliberately excluded until compatibility is proven.

Current runtime calibration list is empty, so `transformStatus()` remains `PENDING_ANCHORS`, `canProject()` remains false, and `projectMasterToScreen()` still refuses projection. This prevents the newly discovered official 2024 coordinate from being incorrectly combined with legacy Master coordinates.

Commit:
- `059b7406d7cfd8518b7ffd878c483f2ff2049cea` — `Gate Milles calibration by evidence version`.

## Validation

GitHub Actions Run #168 (ID `34437485588`) for commit `059b7406...` completed SUCCESS.

Verified steps:
- Validate Master DB: PASS
- Compile debug sources: PASS
- Build debug APK: PASS
- Upload debug APK: PASS
- Complete job: PASS

## DESIGN_CONFLICT / PENDING

No canonical Map/NPC/Location/Monster/Item/Skill/Progression ID or value was changed.
No modern coordinate was promoted into legacy map geometry.
No screenshot was used as a final map texture.
No attachment content/order was guessed.

PENDING:
- binary inspection of the nine 2021 old-town attachments;
- exact post/image resolution for the 2022 Milles statue source;
- pixel inspection of the 2004 Gillenoa/Rosemary and 2005 Milles scenes;
- positive Milles attachment identification;
- at least two version-compatible verified legacy landmark correspondences;
- actual transform implementation only after those gates pass;
- TILE → OBJECT → COLLISION trace after calibration evidence exists.

## Result / next bottleneck

The source pool is materially broader and the runtime trace gate now understands evidence era/version instead of treating every Milles coordinate as interchangeable.

Next priority:
1. resolve more exact legacy Milles post/image URLs and binaries;
2. identify the 2021 Milles attachment using labelled historical landmarks;
3. convert only positively matched legacy landmarks into `CalibrationAnchor` entries;
4. then implement and audit the map projection before tracing TILE/OBJECT/COLLISION.
