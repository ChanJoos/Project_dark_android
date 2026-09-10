# RPG Agent Handoff

Run: `20260910-1836`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 34 — complete canonical skill-data ingestion + current official starter rules + icon-source audit

Direction: stop hand-copying a small subset of skills into Java. Package the canonical Master CSVs into the Android app, expose all runtime-included skill definitions directly, apply current official starter acquisition changes, and make the remaining icon gap explicit instead of pretending `PENDING_CROP` is a finished asset.

### Canonical audit result
- `Skill_Master.csv` already contains rich textual definitions: ID/name/job/stage/type/resource/resource amount/required level/stats/materials+Gold/source/evidence/circle/action class/target/range/element/effect/activation limits/mastery/prerequisite/runtime status/effect evidence/detail source/verification/runtime inclusion.
- It does **not** contain an icon/image column.
- Repository tree contains no individual skill-icon files and no `Asset_Skill_Mapping.csv`.
- Existing `Asset_Master.csv` covers map/UI references, not skill icons.
- Therefore the previous assumption that individual skill pictures were already in the CSV/repository was incorrect.

### Implemented
1. `app/build.gradle`
   - adds `../master/data` to Android `main` assets.
   - canonical CSVs are now packaged with the APK instead of requiring hand-copied Java constants.

2. `RpgCanonicalSkillRepository`
   - loads `Skill_Master.csv`, `Skill_Runtime_Overrides.csv`, and `Skill_Visual_Manifest.csv` from Android assets.
   - preserves every Skill_Master field.
   - exposes all canonical rows, job filters, circle filters, runtime inclusion, acquisition override metadata, and official visual source records.

3. `RpgCanonicalSkillPresentation`
   - exposes every `Runtime_Inclusion != EXCLUDE` row as a complete skill-book/detail DTO.
   - exposes current-job/full-job skill collections without truncating to the older static executable-action registry.
   - preserves unresolved values rather than coercing them to zero.
   - icon key is explicitly `OFFICIAL_SHEET_PENDING_CROP:<Skill_ID>` until a source-backed individual mapping exists.

4. Current official starter acquisition override
   - added `Skill_Runtime_Overrides.csv` from the 2026 official update removing acquisition requirements for:
     - 숏블레이드 (`SK_전사_001`)
     - 레스큐 (`SK_전사_012`)
     - 찌르기 (`SK_도적_001`)
     - 마레노 (`SK_마법사_001`)
     - 쿠로 (`SK_성직자_001`)
     - 디렌토 (`SK_성직자_003`)
     - 정권 (`SK_무도가_001`)
   - the same official notice also names `자물쇠열기`; there is no matching canonical Skill_ID in the current Skill_Master, so no ID was invented.
   - `RpgActionLearningService` now lets only these known official no-requirement starters learn with `RequirementProof.PENDING`; other unresolved skills remain fail-closed.

5. Corrected first-job ordering
   - Commoner Lv10 can choose WARRIOR/ROGUE/MAGE/CLERIC/MARTIAL_ARTIST.
   - starter-skill acquisition occurs after job selection through `RpgActionLearningService`.
   - no skill is silently auto-granted during job selection.
   - `RpgProgressionPresentation.basicJobSelectionGate()` now returns READY at Commoner Lv10.

6. Skill icon source manifest
   - added `Skill_Visual_Manifest.csv`.
   - records current 2026 official renewal confirmation.
   - records 11 official Nexon icon-sheet source OIDs from the official skill-icon notice.
   - direct source sheets are known, but this environment/repository does not contain source-backed per-skill crop coordinates or local icon files. Individual icon mapping therefore remains explicitly pending rather than guessed.

7. Completeness audit
   - added `Skill_Runtime_Completeness_Audit.csv` covering definitions, effects, acquisition overrides, job ordering, current icon policy, source-sheet registration, per-skill icon gap, and executable-binding boundary.
   - PR patch scan confirms removed legacy gate identifiers (`SKILL_REQUIREMENT_PENDING`, `BASIC_SKILL_PROOF_REQUIRED`, `skillRequirementResolved`) are no longer referenced in changed code.

### Key commits
- `19f6dd61e6e0b9c07f67b652f84ae62f6897113b` official starter acquisition overrides
- `f67415d675a38d5de843bd551e20ed7485d22984` initial official skill visual manifest
- `9e02d3aa5c39ceacfbf6fe17793ace59df3ffc92` package canonical Master CSVs as Android assets
- `22cfcb4e3c7803d82a189d42b00819ba41a8a788` full CSV-backed canonical skill repository
- `87a31e6c40457b05e19481df459e00d4f6a6953c` current official starter learning policy
- `5d83598f48373780c3637df614bf3a8abcf2947c` correct job-selection-before-starter ordering
- `72eee13e6b3c93a850f91fa922ec3c860668ae1d` updated job-selection audit
- `293f79420b2b5f466c71aa530e96f7c4644ed2c0` Lv10 READY presentation
- `5dc789452bdf81e2972f1d7f32ce9de043f339c5` updated progression presentation audit
- `638e3e89d10e2df1a5daf8234873a31682d18073` updated learning audit
- `51f1d32e771c8b61812be556b4ddce85fe571811` complete canonical skill-book presentation
- `a4a635f3286880527cdb61cc1963d889882c2902` 2026 icon-renewal confirmation
- `6d32a197527136e679e16265f2d82a5f8616daa2` completeness audit table

### Integrator contract
At app/runtime initialization:
`RpgCanonicalSkillRepository skills = RpgCanonicalSkillRepository.load(context);`

For complete skill-book/detail UI:
`RpgCanonicalSkillPresentation.snapshot(state.rpg(), skills)`

Use the older `RpgActionMetadataCatalog` only for currently executable/quick-slot-bound actions until Combat/Integrator binds additional canonical Skill_IDs to effect execution.

Starter flow:
1. Commoner reaches Lv10.
2. `BasicJobSelectionService.select(state.rpg(), jobCode)`.
3. Show selected job skills from the canonical repository.
4. For official no-requirement starters, `RpgActionLearningService.learn(..., RequirementProof.PENDING)` may succeed.
5. Other skills remain `REQUIREMENT_PENDING` unless their requirements are resolved.

### Honest remaining visual boundary
**Textual/canonical skill definitions are now fully ingestible from Skill_Master. Individual skill icon files are not yet complete.** Official icon renewal and official source sheets are identified, but per-skill crop mapping/pixels are absent from the repository and could not be recovered reliably in this run. Do not fabricate icons or claim individual mapping is complete.

### Validation state
- source/patch consistency audit performed;
- regression audit code updated for corrected gate/acquisition semantics;
- local clone/build unavailable in this execution environment (network DNS blocked);
- repository Actions workflow only triggers on `main` push or manual dispatch, so this PR branch has not received a compile/APK CI run here.

### Ownership preserved
No `GameView.java`, combat effect execution, MonsterAI, map/world, renderer, NPC/dialog rendering, workflow trigger, APK packaging, or main merge changes.
