# PROJECT DARK — DEV HISTORY PASS 22 · WORLD / CHARACTER

Date: 2026-09-10
Role: World · Character

## Source-of-Truth gate executed

Read from GitHub `main` before coding in the required order:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md` and latest World PASS (`docs/DEV_HISTORY_PASS_20_WORLD_CHARACTER.md` / current PASS lineage)

Current user canon was preserved: Nexon-first visual reconstruction, adapted fill only for genuinely missing coverage, and direct-inventory monster rewards with no ground pickup path.

## Web evidence research

Re-checked the Nexon-hosted old-town-map post:
- `https://lod.nexon.com/Community/screenshot/136184?Category2=2`
- title explicitly includes Milles and other old towns;
- page exposes nine image attachments;
- all nine attachment links resolve to Nexon FileDownloader `oidFile` endpoints, but direct binary retrieval in the current environment still returns cache miss.

Additional Milles-labeled source pages were located:
- `https://lod.nexon.com/Community/screenshot/121256?Category2=2` — 2004-05-27, 길레노아/로즈마리 spatial relation text.
- `https://lod.nexon.com/Community/screenshot/125266?Category2=2` — 2005-11-26, title `밀레스의 노숙자들`, useful Milles scene reference.

Because the actual pixels of the nine old-town attachments were not retrievable in this environment, no attachment was falsely promoted to `PIXEL_VERIFIED` and no calibration coordinate was invented.

## Implementation completed

### 1. Added `MillesVisualEvidenceRegistry`

Created:
`app/src/main/java/com/projectdark/mobile/MillesVisualEvidenceRegistry.java`

The registry separates:
- `SOURCE_LOCATED`
- `PIXEL_FETCH_PENDING`
- `PIXEL_VERIFIED`

and geometry-use classes:
- `LEGACY_CALIBRATION_CANDIDATE`
- `SPATIAL_RELATION_REFERENCE`
- `REFERENCE_ONLY`

Current registered sources include:
- 2021 old-town map attachment set;
- 2004 길레노아/로즈마리 source;
- 2005 Milles scene source;
- 2024 Milles event-map reference.

No source currently reports `PIXEL_VERIFIED`.

Commit:
- `3ce2c18d799ba8c51bafee89550905ff9d5ec97d` — `Add Milles visual evidence candidate registry`

### 2. Bound `MillesTraceContract` to the evidence registry

Updated:
`app/src/main/java/com/projectdark/mobile/MillesTraceContract.java`

Changes:
- trace contract now owns a `MillesVisualEvidenceRegistry`;
- `hasCalibrationEvidence()` first requires at least one pixel-verified calibration source;
- even then it still requires two distinct verified legacy calibration anchors;
- `passesAudit()` verifies the evidence gate remains closed while source candidates are present but pixel verification is absent.

This prevents source discovery alone from silently enabling a tile-to-screen transform.

Commit:
- `e0dd92e43f492b3ed44593db757f30581e9b0acf` — `Bind Milles trace gate to visual evidence registry`

## Validation

GitHub Actions Run #176 (ID `34438169353`) for commit `e0dd92e4...` completed SUCCESS:
- Validate Master DB: PASS
- Compile debug sources: PASS
- Build debug APK: PASS
- Upload debug APK: PASS
- Complete job: PASS

## DESIGN_CONFLICT / PENDING

No canonical map/location/NPC/item/monster/skill/progression ID or value was changed.
No 2021 attachment was identified as Milles without seeing its pixels.
No Master coordinate was projected into screenshot space.
No screenshot was promoted to final map texture.

PENDING:
- retrieve/inspect the nine 2021 old-town attachment binaries in an environment that can access the Nexon FileDownloader payload;
- positively identify the Milles attachment(s);
- mark visible landmark coordinates in the verified image;
- create at least two version-compatible `CalibrationAnchor` records before transform activation;
- begin TILE -> OBJECT -> COLLISION trace only after calibration is evidence-backed;
- continue collecting source-covered Milles visuals before authoring `[ADAPTED]` fill.

## Result

Milles source collection is now represented as executable runtime evidence metadata rather than informal notes. The project can accumulate source candidates immediately, while tile projection remains mechanically impossible until pixel verification and anchor requirements are satisfied.
