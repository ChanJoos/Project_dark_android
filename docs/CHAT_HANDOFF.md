# PROJECT DARK 채팅 간 인수인계

이 문서는 새 채팅에서 작업을 안전하게 이어가기 위한 사용 절차입니다. 프로젝트 사실의 원본이 아니라 탐색 경로와 갱신 규칙입니다.

## 새 채팅 시작

1. 저장소의 최신 `main`을 기준으로 이 문서, `AGENTS.md`, [현재 상태](PROJECT_STATE.md), [결정 기록](DECISION_LOG.md), [작업 백로그](DIRECTOR_BACKLOG.md)를 읽습니다.
2. 사용자의 이번 요청과 제공 자료를 먼저 기준으로 삼습니다. 과거 채팅 요약이나 오래된 baton은 현재 요청을 덮어쓰지 않습니다.
3. 관련 구현을 조사하기 전에 해당 주제의 기존 기획·데이터 계약·출처/감사 문서와 관련 handoff를 읽습니다. 기존 문서를 수정할 때는 반드시 최신 `main`의 파일 내용을 먼저 확인합니다. 몬스터 신규 추가·에셋 교체·AI/충돌 수정은 `docs/MONSTER_CREATION_MANUAL.md`의 등록 및 검증 절차를 함께 적용합니다.
4. 코드, 테스트, CI, 기기 녹화 등 필요한 근거를 직접 확인합니다. 문서에 적혔다는 이유만으로 구현 또는 검증 완료로 간주하지 않습니다.
5. 이 문서들끼리 상태가 충돌하거나 기준 SHA가 다르면 최신 `main`과 그 SHA의 증거를 대조합니다. 풀리지 않는 충돌은 임의로 선택하지 말고 `미정/충돌`로 기록합니다.

## 확정사항과 아이디어 구분

- **확정 결정**: 사용자가 승인했거나, 저장소의 승인된 운영 규칙/기준 계약에 명시되어 근거 경로를 적을 수 있는 결정.
- **구현 사실**: 특정 SHA의 소스·데이터·에셋에 실제 존재함을 확인한 상태. 기획 의도나 아이디어와 구분합니다.
- **검증 상태**: 빌드, 기기 실행, 시각 수락을 각각 증거와 범위에 따라 기록합니다. 한 단계를 다른 단계로 추정 승격하지 않습니다.
- **아이디어/제안**: 논의되었거나 기획 문서에 목표로 기재된 내용이지만, 승인 또는 구현이 확인되지 않은 상태. 결정·현재 기능처럼 쓰지 않습니다.
- **미정/충돌**: 근거가 없거나 자료끼리 충돌하는 내용. 추측으로 채우지 않고 확인 담당 또는 필요한 증거를 적습니다.

## 작업 종료 시 갱신

작업이 끝날 때마다 같은 PR/변경 묶음에서 관련 문서를 갱신합니다.

1. [현재 상태](PROJECT_STATE.md): 기준 SHA, 이번 변경, 실제 구현 상태, 정확한 검증 결과/범위, 남은 한계, 다음 미완료 작업.
2. [결정 기록](DECISION_LOG.md): 새로 승인되거나 변경된 결정만 추가. 제안은 제안 구역에 두고 결정으로 승격하지 않습니다.
3. `docs/DIRECTOR_BACKLOG.md`: 해당 작업 행의 상태·소유자·검증 기준·막힘·다음 결과를 최신화합니다. 작업 결과를 확인하지 못했다면 완료로 바꾸지 않습니다.
4. 관련 상세 기획/계약/handoff: 실제 결정이나 구현 계약이 바뀐 경우에만 해당 문서를 수정합니다. 과거 기록을 지우거나 무근거로 일괄 재작성하지 않습니다.
5. 코드/에셋 변경과 문서의 SHA·CI·산출물 참조를 서로 대조하고, 문서만 바뀐 경우 게임 구현 완료로 표현하지 않습니다.

## 상태 문구 규칙

- `IMPLEMENTED`: 소스/데이터에 구현됨을 확인.
- `BUILD_VERIFIED`: 기록한 정확한 SHA에서 지정 빌드·검사를 통과.
- `DEVICE_VERIFIED`: 기록한 APK를 실제 기기에서 실행해 기록된 시나리오를 확인.
- `VISUAL_ACCEPTED`: 기준 이미지/화면과 비교해 수락 기준을 통과.
- `PENDING`: 확인할 근거 또는 조건이 남음.
- `UNKNOWN`: 실행/작업의 결과나 중단 이유를 확인할 수 없음.

날짜는 Asia/Seoul 기준으로 쓰고, SHA, workflow run, APK/artifact 링크가 확인되면 함께 남깁니다. 추정 날짜·가짜 성공 상태는 쓰지 않습니다.

## APK 전달 전 확인

- 확인한 사용자 요구가 해당 SHA의 소스에 실제 포함됐는지 확인합니다. 코드/에셋이 저장소에 존재하는 사실과 APK에서 접근·동작하는 사실을 구분합니다.
- 산출물 파일명만 보지 말고 GitHub Actions run의 `head_sha`, artifact ID/name, APK 내부 파일과 SHA-256을 대조합니다.
- 사용자가 요구한 실행 시나리오로 기기에서 확인한 범위를 적습니다. CI 성공은 빌드 검증입니다. 기기 확인 없이 “작동한다”고 하지 않습니다.
- 사용자가 실패를 보고하면 보고 문구, 전달 APK SHA/run, 재현 여부, 원인 상태를 기록합니다. 보고 자체는 실패 증거지만 원인을 증명하지는 않습니다.
- 실패 원인이 해결되고 같은 시나리오를 검증하기 전에는 이전 APK를 새 버전처럼 다시 전달하지 않습니다. 기기 검증을 할 수 없으면 그 한계를 분명히 적습니다.

## Active task branch and APK source

Before building, inspect main and the branch that contains the requested work. Compare their heads and relevant diffs. Build the SHA with the requested work; identify branch, SHA, app version, Actions run, artifact, and APK SHA-256. If that branch is unmerged/diverged, call the APK a candidate. Build success and renderer screenshots do not prove device interaction; record requested on-device scenarios separately.


## Pote monster runtime candidate handoff (2026-09-29)

- Source branch: `codex/pote-monster-sprites-runtime-20260929`, based on Pote runtime branch head `133f2388706b1948e63c7b931fb46c4a442c8ff2`; this is a candidate branch, not main.
- User explicitly requested a test APK using the generated artwork currently available. Register only the five complete sets in `PoteForestMonsterShowcase`: `POTE_PURPLE`, `POTE_RED`, `POTE_GREEN`, `POTE_SILVER`, `POTE_LYCAN` (12 individual still-pose PNGs each). Only the Lycanthrope set was previously visually accepted; the other four remain generated candidates.
- Remove the old `monster_test_v04` 16-identity runtime mappings and its production copy from packaged assets. Preserve its separate historical review source; do not load it in the app or use it as new-art reference.
- Do not register Trant's partial 9-pose set (only NW attack exists), other missing species, or strong variants. Never fill missing directions with guessed/mirrored art.
- PNGs are one representative image per state/direction, not looping animation sheets. Verify live AI state changes, four diagonal facings, all 60 asset loads, silhouette bounds, and APK contents. Record exact CI run, source SHA, artifact ID, APK SHA-256, device-pending status, and next task in `PROJECT_STATE.md`, `DIRECTOR_BACKLOG.md`, and the art README before ending.


- Runtime candidate APK delivered: versionCode 49; source SHA `1def7a92aeef68ce144ebaad8b94f02dee011a12`; Actions run `36534954426` succeeded; artifact `11017989115`; APK SHA-256 `9b8cb7e86c7b9c89b34667199711b1738f72aa9267b6edbb79743c934b18c799`. APK inventory: 60 generated pose PNGs, zero `monster_test_v04` paths. Automated device-review tests pass; physical/user-device visual QA remains pending.
- Next action: install this exact APK on a device and inspect scale, four facings, walk/attack state changes, and attack timing; then update these files with observed results. Do not call the build visually accepted before that check.


## 최신 사용자 검수: v49 런타임 미통과 (2026-09-29)

- 사용자 관찰: 초기 장면에서 퍼플팜팻만 보임; walk/attack 동작 미표시; 이동 방향과 sprite facing 불일치; 공격이 8방향처럼 보임. v49 APK는 미통과.
- 코드 원인: 다른 4종이 기존 farthest-point 배치로 입구/aggro 밖에 산개; walk는 단일 pose still; shared combat route에서 `attackPrimed=false`가 된 이후 렌더가 idle을 골랐음; 공격 방향 lock이 walk로 복귀할 때 풀리지 않을 수 있었음.
- 수정: 입구 72~168px 범위로 전원 배치, 반복 walk gait와 공격 lunge, hit 이후 공격 포즈 유지/방향 lock 종료. 네 대각 방향만 사용.
- 새 다섯 종 × 4 facing의 이동 방향/공격 방향과 배치·렌더 테스트를 추가했다. 새 CI/APK 검증 결과는 아래 PROJECT_STATE와 backlog에 기록되기 전까진 미완료다.


## Latest candidate result — version 50 (2026-09-29, Asia/Seoul)

- Source branch `codex/pote-monster-sprites-runtime-20260929`, code SHA `3cd69850c16a61629e4c1c80a9683cb6f4fc46e3` (versionCode 50, `0.50-pote-monster-runtime-fix`). The branch is not merged; comparison with `main` reports 103 commits ahead and 5 behind.
- Actions run [#36549350583](https://github.com/ChanJoos/Project_dark_android/actions/runs/36549350583) succeeded at that exact SHA. Pote test batch passed: `PoteForestSpatialGrammarTest`, `PoteMonsterPlayableRuntimeTest`, `PotePamfetPresentationTest`, and `PoteCreekWaterAssetTest`; `assembleDebug` succeeded. Build time: 2026-09-29 18:29:57 KST.
- Artifact [11024127131](https://github.com/ChanJoos/Project_dark_android/actions/runs/36549350583) contains `app-debug.apk` (12,631,372 bytes), SHA-256 `807437bf4b5270a2701c2380e854eeb032018117952336f1b0e1f3bb516961d4`. APK inventory: 60 `pote_monsters_generated_v1/sprites/` PNGs and zero `monster_test_v04/` files.
- Source patch clusters the five complete art candidates near the entrance, exposes walk/attack state changes with a four-diagonal facing contract, and tests applied movement/facing and pose rendering. User-reported v49 failures have not yet been verified on a physical device with v50. Status: **IMPLEMENTED / BUILD_VERIFIED / DEVICE_PENDING / VISUAL_ACCEPTED_PENDING**.
- Next: install this exact APK and verify all five candidates are visible, movement-facing matches actual step, attacks use only NW/NE/SW/SE, attack occurs at valid range, and the app remains stable. Do not mark visual acceptance until this check is recorded.


## 2026-09-29 — Pamfet movement facing and auto-attack follow-up

- User confirmed the Pote map remains active after exit/restart with v0.55. New report: some Pamfets occasionally use a movement pose/facing that disagrees with travel direction; auto-attack sometimes circles or selects a geometrically nearby but non-optimal target.
- Active candidate branch: `codex/pamfet-facing-auto-target-r3-20260929`, based on latest Main `c2e60d6` (the base advanced while the change was being prepared).
- Candidate changes: require exact canonical applied movement vector for walk pose; remove lateral sprite drift from one-frame walk art; target the moving monster's reserved next tile; choose reachable auto targets by shortest legal approach path then distance; keep target stable while alive/reachable; ignore unreachable targets.
- Added regressions cover all four Pamfet pose directions despite stale attack lock, shortest reachable path preference, inaccessible targets, moving-target reservations, and legal adjacent approach path cost.
- First run `36586971014` at source `83db88d...` stopped before compile in `Validate Master DB`, due the latest Main not registering its new `Skill_Legacy_Requirements.csv` addition and continuing to hash-check the amended `Skill_Evidence.csv` as unchanged workbook data. The audit registry was corrected: the former is an explicit canonical addition; the latter is a canonical override with schema-header validation. Local `python3 tools/validate_master.py` now passes and reports XLSX fidelity as unverified. The source fix must be committed to this PR and the exact-SHA workflow rerun.
- Device facing and auto-attack behavior remain pending; generated art direction labels need visual check on device. Update `PROJECT_STATE.md`, YAML, backlog, this handoff and manual with final measured run/artifact. Keep `DEVICE_PENDING` until user/device confirms the exact APK.

## 2026-09-30 — user reports v0.56 still fails; r4 underway

- User clarified the active issue is the Pote Pamfet movement-facing mismatch and automatic combat path behavior; the later skill/PDF attachments were unrelated to this task.
- Delivered v0.56 APK SHA-256 `2fc04baed7c4d53942c129a91cd61a3864243cceb10cc55749361e1d112a2ec9` is **DEVICE_FAILED (user-reported)**. Do not represent r3 CI success as proof the bug is fixed.
- Latest Main at start of r4: `50c65cc457227f447356335ba89391489bab41b9`. r4 code replaces greedy monster chase detours with BFS over legal authored tile steps toward melee adjacency; regression test covers a blocked direct route and repeated replanning.
- Current branch: `codex/pote-facing-autotarget-r4-20260930`; candidate app version 0.57. Local Gradle is unavailable in this checkout (no wrapper and no system `gradle`); run exact-SHA GitHub Actions. Device art-gaze QA and automatic target behavior still require user/device evidence.
- Do not send a new APK as a verified fix until the requested device scenario passes. Report build/runtime/visual states separately.


## 2026-09-30 — SKILL-WINDOW-01 build verification

- PR #168 runtime source: `74c3833e0010e7f0c78e01a70b0c8cafc0ade614`; base `cd3dd871fc81dcc88053ffff5305fd6fdeddc065`, ahead 4 / behind 0 at verification.
- Actions run [36622542956](https://github.com/ChanJoos/Project_dark_android/actions/runs/36622542956), job 109591297347: SUCCESS. Checkout actually built synthetic PR merge `5560ccc69e990d08b44172a9aa538815e64a29f3` (head 74c3833e into base cd3dd871). Do not identify this as a head-only checkout.
- Passed: Master validation, regenerated catalog equality, SkillWindowTest (catalog / unlearned gates / skill checkpoint restore / invalid snapshot protection / shared Resolver repeated input / world tick behind modal), restart persistence matrix, RuntimeCheckpointTest legacy migration and all configured regression steps, assembleDebug.
- APK artifact `11059261609`, 12,666,152 APK bytes, APK SHA-256 `aa1eac489ac840e293a73619e821491288dc53d8e2a4b482cbbe2b0925400d92`. Build completed 2026-09-30 04:56:45 KST. ZIP digest is distinct from APK hash.
- Inspected packaged `assets/skills/catalog.json`: 219 Master records. SkillBook adds three B fixtures at runtime. Inspected `skill-window.png` from render artifact `11059746241`: frame, tabs, grid, details and disabled unlearned controls render within the viewport. This is automated native rendering, not physical-device acceptance.
- Status: IMPLEMENTED / BUILD_VERIFIED / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING. Starting commoner receives no implicit skills; acquisition NPC/service, original combat effects and source icons remain pending. Consequently normal fresh saves cannot yet use/register skills; those paths are exercised with explicitly learned B fixtures in automated tests.
- Next: user/device acceptance for opening the fourth utility button, technique/magic and learned/all filters, pages/details, old-save preservation and restart; then the separately authorized acquisition/effects/artwork implementation.


## 2026-09-30 — SKILL-WINDOW-02 source art and presentation repair

User rejected PR #168 APK (`aa1eac489ac840e293a73619e821491288dc53d8e2a4b482cbbe2b0925400d92`) as mockup-like and visually poor. Record VISUAL_REJECTED (user-reported); prior build success is not visual acceptance. Functional/device root cause is not claimed from this report.

Candidate `codex/skill-window-source-art-20260930`, based on main `1d8b2118a357483803f8b98668f5000d53aee0d5`, v0.58. Replaces Chinese type tiles with 75 exact job/name icon crops from the five user-provided screenshots; crop coordinates, original SHA-256 and unmatched names are recorded in `skills/source_icons.json`. Unmatched and duplicate source rows are not assigned by resemblance. Source images are V reference artwork, not verified official originals. No other skill borrows a pictured icon.

Reworks the frame, typography and opaque panels; adds six job filters and separate effect/requirements/reference tabs, paged long descriptions, selected highlight and a visible eight-slot rail. Keeps original acquisition/learned checks and shared Resolver; fresh commoners still cannot acquire or use original skills because acquisition/effects are outside this visual repair. Do not call this a playable original-skills release.

Verification pending: exact-SHA Actions, direct fourth-utility reachability, source-ID rendering, job-filter/modal/slot interactions, save/restart regressions, native screenshots for overview/rogue/cleric/empty states. Physical device and user visual acceptance remain pending.
