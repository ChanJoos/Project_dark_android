# 디렉터 작업 지시

Revision LOOP-4-V1 · 사용자 승인 운영 개정.
공동 목표: 밀레스 이동→NPC 대화→몬스터 1마리 전투→직접 보상→성장→저장→프로세스 재시작 복원.
아래 네 역할 할당은 이전 개정의 기록이며, 최신 live 예약 상태·정확 SHA에서의 기능 상태를 각각 확인해야 한다. 2026-09-28 사용자 APK 실패 보고는 APK 전달/수락 전 최우선 Director blocker다. 이전 행은 완료나 취소로 간주하지 않는다.

| Task | Owner | 다음 결과 하나 | 수락 조건 | 의존성/상태 | 반복 blocker |
|---|---|---|---|---|---|
| WORLD-01 | World | 기존 spawn→NPC→훈련 몬스터→복귀 경로 검수 및 실제 통행 장애 수정 | 인접 tile/왕복 drift=0, 주요 footprint/shoreline 차단, 대화·공격 거리 접근, touch/camera 일치 | ACTIVE; 2dbcd9c 지형 정리 보존. Android 미실행은 Director QA handoff | 새 할당, 0 |
| VISUAL-01 | Visual | ATTACK/CAST 시 기본 캐릭터 외형 연속성 유지 | 승인 body/크기/방향/발 anchor 유지, resource 오류 crash 없음, HP mutation 없음 | ACTIVE; 원본 행동 frame 미확보는 명시하고 기존 BODY 보존. 실제 action contract는 Game/Director와 합의 | 새 할당, 0 |
| GAME-01 | Game | `RuntimeCombatSession` 단일 submit/tick/drain API와 Director 교체 handoff | action 구분·control/learned/resource/range/LOS/cooldown 검증·effect 1회·생명당 defeat 1회·훈련 증표 직접 지급 1회 audit PASS | IMPLEMENTED; Director가 GameView 직접 damage/legacy cooldown과 MonsterAI legacy route를 원자적으로 교체해야 함 | 없음, 0 |
| DIRECTOR-01 | Director | 실제 전투 입력/루프를 GAME-01과 연결·검증 | legacy direct damage 병행 제거, 관련 audit 실제 실행, 동일 SHA APK·실행 범위 명시 | ACTIVE; Game과 최소 API 합의. 결과 대기 중 다른 통합/검증 가능 | 새 할당, 0 |
| POTE-TEST-01 | Director/Integration | 크기·공격 표시 수정 APK를 기기에서 검증해 시각 피드백 종료 | 16 actor 배치/보상격리/방향별 이미지 유지, 투명 여백 정리 후 실루엣 높이 48 논리 픽셀 이하, 공격 표시 임의 돌진·회전 제거, 실제 인접 타일 공격 확인, exact-SHA CI + 같은 기기 재검증 | 사용자 2026-09-28 피드백: 설치 APK SHA 미제공 상태에서 과대/공격 포즈/선공 인상/강력한 놀 그래픽 문제 보고. 수정 source `8ca5fda1c2483589b6f16fc9800bb9f87597ec23`, Actions #36393396473 **성공**, APK artifact `10957720462`, APK SHA-256 `263a502075f438048feb0191550cf59cee8f9a424b41ffdf02605146d337c359`; 168 packaged PNG. 새 높이 및 비인접 추적 회귀 테스트 통과. Strong 별도 artwork 없음(기본 GNOLL concept 공유). 추적 반경 180과 인접 melee 판정 유지. **DEVICE_PENDING / VISUAL_PENDING**. | BUILD_VERIFIED; 기기 확인 대기, 0 |


## 즉시 인수인계 우선순위 (2026-09-28 확인)

| Task | Owner | 다음 결과 하나 | 수락 조건 | 상태 |
|---|---|---|---|---|
| DELIVERY-01 | Director + World | versionCode 48 Pote 후보를 실제 기기에서 검증하고 통합 여부를 기록 | 정확한 APK/SHA에서 시작 이동·안내인 숲 진입·숲 내부 이동을 확인. 실패하면 원인을 고치고 새 SHA에서 같은 시나리오 재검증 | BUILD_VERIFIED; DEVICE_PENDING; 브랜치 미병합 |

근거: 기존 전달 APK는 main `0506929`, SHA-256 `e10e94495a8cd5dba2f86753f28d617835d42ecc2f4f0baf331c9fe671b62ef2`이며 포테 격자 간격 결함을 포함했습니다. 활성 브랜치 `codex/pote-ground-tile-foundation`의 source candidate `16c6d9a`는 CI #36388750145 성공 및 Pote 경로/렌더 검사를 통과했습니다. APK artifact ID `10955795355`, APK SHA-256 `03f4ccae5fe0804fa78bbeaef974696c0064b3d2941bb190b8fc68d3e64e3e4f`. 실제 기기 검증은 아직 안 됐습니다. 기존 네 역할 할당을 완료/취소로 간주하지 않습니다.

## 다음 작업 대기열

Game/Director: GAME-01 뒤 실제 보상·EXP/Gold/level mutation 및 최소 save/restore에서 가장 큰 끊김 하나. 저장은 대규모 콘텐츠 이후로 미루지 않는다. GAME-01에 막히면 독립적인 저장 계약/구현을 작업 하나로 명시 전환할 수 있다.
World: WORLD-01 통행 검수 후 READY_FOR_RUNTIME_QA handoff; 실제 장애 없으면 IDLE, 추가 맵/지형 튜닝 금지.
Visual: VISUAL-01 뒤 현재 루프에서 실제 잘못 표시되는 부분이 없으면 IDLE; 새 장비 대량 제작 금지.
Director: POTE-TEST-01 CI is complete; perform its Android/device check before closing the task, then continue DELIVERY-01 device scenarios. 결과→통합→판정→다음 할당을 매 회차 닫는다. 동일 blocker 두 회차면 해결 방법/배정 변경.

## 초기 근거와 한계

감사 baseline 2dbcd9c945ee043ff36cb08f4f740ca1204ccad0.
- 제작 마을·성당·호수·평민 IDLE/WALK·타일 이동/충돌 수정은 main에 존재. 2dbcd9c CI compile/APK 성공 및 artifact 확인.
- GameView 공격은 state.damage 직접 경로, 훈련 증표 직접 보상은 연결, 퀘스트 HUD는 고정 문구, AUTO는 준비 중. 저장/프로세스 재시작 복원과 실제 성장은 미완성으로 감사됨.
- 이전 예약 지시의 기기 PASS는 과거 검수 기록이며 최신 SHA 전체 실행 증거로 승격하지 않는다.
- 운영 개정 자체로 게임 동작 또는 Android 검증이 완료된 것은 아니다.

## 예약 상태 및 대기 관리

기존 예약 ID:
- Visual: 6aa57a764bac8191bb090fe300fb7b7a
- World: 6aa57a8952648191bb96a2abcb510670
- Game: 6aa57a9c1c648191a1999dbbec5809c4
- Director: 6aa57ab0feac819196778cc4e3d2c7d1

이 개정은 World 재개와 네 prompt 교체를 승인한다. 실제 설정 결과는 automation readback으로 확인한다.
Director는 매 실행 live 상태와 마지막 결과를 확인한다. IDLE로 쉴 때는 이유/마지막 SHA/재개 조건/후속 점검 담당을 기록한다. next_run_time=null이면 다음 실행 보장이라고 쓰지 않는다. 원인 불명 중단은 UNKNOWN으로 남긴다.

각 task 완료 시 status/검증 범위/SHA·PR/다음 결과로 해당 행을 교체한다. 역사 전체를 복제하지 않는다.


| POTE-ART-IMG-01 | Visual + Director | 원작 자료와 13종 대응을 확인하고 독립 PNG 156장을 제작·시각 검수 | 13종 × 정지/공격/이동 × NW/NE/SW/SE; 각 파일 직접 확인 가능, 전신·방향·팔레트·투명 가장자리 검사; 168 콘셉트/반려 시트 참조 금지 | 앞선 3장만의 전달은 범위 누락. 이번 팜팻 SE 대기 4색 초안 중 실버는 후광 QA 반려, 나머지 3색도 사용자 수락 전. 늑대 ID 대응 미정. 활성 GitHub 브랜치에서 원본 종별 스크린샷 묶음 경로 미확인. 앱 미적용/새 APK 없음. | 0/156 VISUAL_ACCEPTED; IN_PROGRESS; SOURCE_GAP |


최신 팜팻 SE 대기 이미지 파일명·QA 범위는 PROJECT_STATE의 2026-09-29 범위 정정에 기록. 앞선 팜팻 구르기와 실버 대기는 후광 때문에 반려; 레드·그린·퍼플 대기 초안도 사용자 수락 전이다. 13종의 나머지 방향·동작은 미완료.


## Pote 몬스터 런타임 후보 교체 (2026-09-29)

| Task | Owner | 결과 | 검증/상태 | 다음 작업 |
|---|---|---|---|---|
| POTE-MONSTER-RUNTIME-01 | Visual + Director | v49 user-reported display/motion/facing failures addressed in versionCode 50 candidate | `codex/pote-monster-sprites-runtime-20260929`, source SHA `3cd69850c16a61629e4c1c80a9683cb6f4fc46e3`; Actions #36549350583 succeeded; Pote spatial/runtime/presentation/water tests and assembleDebug passed. Artifact 11024127131; APK SHA-256 `807437bf4b5270a2701c2380e854eeb032018117952336f1b0e1f3bb516961d4`; inventory 60 new pose PNGs, 0 old `monster_test_v04` paths. Branch is 103 ahead / 5 behind main. **BUILD_VERIFIED; DEVICE_PENDING; VISUAL_ACCEPTED_PENDING**. | Install exact v50 APK and verify five candidates visible, movement-facing alignment, only four diagonal attacks, valid attack distance/timing, and stability. |

현재 5종은 기존 생성 후보만 사용합니다. 트랜트 9장 부분 세트 및 그 밖의 미생성 종/강력형은 새 artwork가 완성될 때까지 미등록입니다. 12장은 방향/상태별 대표 still이며 multi-frame loop 미구현입니다.


## POTE monster spacing and direction follow-up (2026-09-29)

| Task | Owner | Result | Verification / state | Next action |
|---|---|---|---|---|
| POTE-MONSTER-SPACING-02 | Director (cross-cutting RuntimeState + GameView integration) | VersionCode 53 adds species-aware actor clearance, reserves other monsters' in-flight destinations and swept paths, and renders Pote walk facing from the committed tile-step vector | Source 7f6944e42888a1bd4c5a43e1b4e6261b0f9c2320; Actions #36565408459 succeeded; APK artifact 11030999456; extracted APK SHA-256 a1cccce1d52b172172355063ca0f653be648251f1210a6b0e20d758fce2a27bb. Unit/render checks cover simultaneous approaches, crossing paths, player-tile exclusion, and stale attack lock. **IMPLEMENTED / BUILD_VERIFIED / DEVICE_PENDING / VISUAL_ACCEPTED_PENDING**. | Install the exact v0.53 APK on the user's device; verify no player/monster overlap and correct facing through movement and attack recovery. Keep root cause UNKNOWN until reproduced or disproven on device. |

The v0.52 issue is user-reported DEVICE_FAILED: character/monster overlap, monster/monster overlap, and occasional walk-facing mismatch. The fix has not yet been accepted on-device; CI checks do not close this work item.


## POTE automatic-combat spacing follow-up (2026-09-29)

| Task | Owner | Result | Verification / state | Next action |
|---|---|---|---|---|
| POTE-MONSTER-SPACING-03 | Director (World adapter + RuntimeState) | v0.54 rechecks species-aware occupancy and the entire player step against monsters' moving reserved destinations/swept paths when automatic combat advances | Source 5905e963de7898acec4b84644e78fcb98cec395e; Actions #36570339135 passed; APK artifact 11033662723; SHA-256 97b892b9afd8afb82c888db1ed13305f565e3072300900e26ffbf5ec848f2fb4. User reports the device spacing scenario passed; device/build details were not repeated. **USER-REPORTED DEVICE PASS; visual acceptance remains separate**. | Keep the result as user-reported pass. Next: verify cold-process map/position restore in the First RPG Loop. |

The v0.53 overlap report remains user-reported and has not been independently reproduced on-device. Source risk was found and covered by v0.54, but root cause on the handset remains UNKNOWN until the exact APK is tried there. Future monster work must follow [MONSTER_CREATION_MANUAL.md](MONSTER_CREATION_MANUAL.md).


## Next integration task after v0.54 combat acceptance (2026-09-29)

| Task | Owner | Next result | Acceptance |
|---|---|---|---|
| F5M-RESTART-RESTORE-01 | Game + Director | Restore the correct active map and safe player position after process death/relaunch | Milles and Pote cold-restart tests; preserve HP/MP, quest/objectives, inventory/equipment, EXP/Gold/level/stats and reward watermark; invalid/old save uses safe fallback without destructive overwrite; no duplicate combat or quest reward; exact APK process-restart check. |

Weapon hand attachment and hit readability are explicitly deferred by the user. Existing progression/save components should be reused; do not reimplement them. Close the map restore gap first, then run the full NPC → hunt → reward → progression → restart/restore loop on device.

## Pote restart, respawn and reward verification (2026-09-29)

| Task | Owner | Result | Verification / state | Next action |
|---|---|---|---|---|
| F5M-RESTART-RESTORE-01 | Game + Director | v0.55 reconstructs the saved Pote runtime/adapter before applying its checkpoint, preserves the Milles return point, and revives through the active map adapter while clearing stale touch/joystick state. | Source SHA `0bbb6ee354af2e67e4513db7e5bb7e643c310052`; Actions #1490 (`36579231684`) passed all checks and APK build; artifact 11038421617, APK SHA-256 `b79e1c58742f006cecc167a6e66f16b6ba9501d8d4a70d506a790d149aeb603a`. **BUILD VERIFIED / DEVICE PENDING**. | Install exact v0.55 APK, cold restart in Pote, die/revive/walk, and verify saved quest reward, inventory, equipment and progression. |

Pote showcase monsters have no source-backed EXP or drop rates/quantities in the available master data. `POTE_SPIRIT` has verified EXP 308950, but no resolvable gear/item odds or quantities; it is not one of the current five showcase actors. Keep these monsters' reward state pending. Persistence can be tested with the existing explicit quest reward and equipped gear; do not fabricate monster rewards to make the test convenient.


## Pote Pamfet facing and auto-attack path follow-up (2026-09-29)

| Task | Owner | Candidate result | Verification / state | Next action |
|---|---|---|---|---|
| POTE-AUTO-FACING-01 | Director (renderer + world path + GameView integration) | v0.56 was merged, but user reports movement-facing and auto-attack path defects remain on the delivered APK. r4 replaces greedy monster detours with stable shortest legal tile paths to melee adjacency and adds a replan-around-blocked-route regression test. | v0.56 Main merge `b6d1c4b`; delivered APK SHA-256 `2fc04baed7c4d53942c129a91cd61a3864243cceb10cc55749361e1d112a2ec9`; user report is **DEVICE_FAILED**. Root device cause remains UNKNOWN. Candidate `codex/pote-facing-autotarget-r4-20260930`, versionCode 57; local Gradle unavailable; exact-SHA Actions pending. **IMPLEMENTED / CI_PENDING / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING**. | Wait for exact-SHA Actions; do not call or deliver an unverified APK as the fix. Device check: all Pamfet directions and gaze, blocked-tile detours, target route efficiency and no circling. |

The user reported the v0.55 Pote restart map behavior now passes. The new facing/auto-target report is user-reported; device root cause remains UNKNOWN until the exact updated APK is run. Follow [MONSTER_CREATION_MANUAL.md](MONSTER_CREATION_MANUAL.md). Do not mark generated Pamfet gaze as visually accepted from file-name/unit-test checks alone.

| SKILL-WINDOW-01 | Director/Game | 기술·마법 창/상세/퀵슬롯/저장 | shared Resolver·미습득 차단·재시작 복원·world tick 유지 | IMPLEMENTED; BUILD_VERIFIED; DEVICE_PENDING | 원작 효과/아이콘은 후속 |


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


### SKILL-WINDOW-02 final build evidence

- Runtime head `9fb4e3d77f144ef30a4fd342e544b955a369a8c1`; Actions run [36626946163](https://github.com/ChanJoos/Project_dark_android/actions/runs/36626946163) SUCCESS, job `109606255393`. Actual checkout is PR merge `29bf3cee1fba673aee0be77225ce16089b14fde7` into base `1d8b2118a357483803f8b98668f5000d53aee0d5`.
- v0.58 / versionCode 58, APK artifact `11060023417`, build finished 2026-09-30 05:33:12 KST. Extracted APK 12,880,982 bytes; SHA-256 `a7e2e666db282f34218a86f34a72faf606799544d4df5ccae000e676e6c072c2`. Packaged catalog is 219 records, source icon mappings are 75; packaged atlas bytes match authored atlas exactly.
- Passed all configured CI checks, including direct utility reachability, exact source icon loading, job selection, explicit detail tabs, learned/empty state, shared Resolver quick-slot use/repeated-input gates, world tick behind modal, checkpoint restore, malformed-save protection and legacy migration; assembleDebug passed.
- Render artifact `11060203281`: inspected overview, rogue, cleric and empty-state renders. Final source removes repeated missing-art placeholders, renders missing-art entries as name/job cards, uses a full-width missing-icon detail title, and removes dummy grids under empty-state text. No text/panel overlaps observed in inspected 960x540 native renders. Physical Android device interaction and user visual acceptance remain PENDING.
- Status: IMPLEMENTED / BUILD_VERIFIED / NATIVE_RENDER_REVIEWED / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING. Previous PR #168 remains visually rejected by the user; do not erase that report. This repairs the window presentation only; original acquisition NPC/service and combat effects remain unimplemented, and most original icons still require source material. No original skills are silently granted to commoners.

## 2026-09-30 — SKILL-WINDOW-03 습득·퀵슬롯·쿨타임

- 사용자 검수: v58에서 습득 경로/원작 효과/퀵슬롯 연결이 빠졌고 설명 본문을 누르면 바뀐다는 지적. 전달된 v58 검수의 기능 미수락으로 기록한다. 실제 설치된 APK SHA는 독립 확인하지 않았으므로 기기 원인/판정 범위를 추정하지 않는다. 소스 감사에서는 Master 219행의 runtime=null, 습득 호출 부재, runtime 있는 B fixture만 등록 허용, 본문 탭의 detailOffset 증가를 확인했다.
- 사용자 선택 **스크린샷 조건 적용**에 따라 직업·능력치·선행 숙련도를 확인하는 SkillAcquisition을 연결했다. 새 캡처 무도가 조건 12행, 전사 드래곤모드의 두 선행 90/90, 공통 유즈스태프 INT6/WIS3 전사 오류를 source hash와 함께 별도 acquisition_captures.json에 보존했다. Master의 219행/원본 출처는 그대로 유지한다. 조건 미확정/승급/5서클 재료·이벤트는 차단한다.
- 11개 사용자 캡처에서 정확히 매칭한 아이콘 135개 (이전 75개). 10개 캡처 행은 명칭 불일치로 미등록이며 유사 아이콘을 빌리지 않는다. 모든 원본 hash와 crop bounds를 검사했다.
- 실제 흐름: 공통 필터→스킬 선택→습득→8개 퀵슬롯 등록/동일 항목 해제→전투 HUD 사용→체크포인트/복원. 창 안 슬롯 선택은 설명 선택만 한다. 등록할 때 스킬이 실행되지 않는다. 설명 본문 tap으로 페이지/내용이 바뀌지 않는다; 명시적 효과/조건 탭 및 bounded up/down 버튼만 반응한다.
- starting COMMONER의 기본공격은 캡처 3/3/3/3/3 조건으로 직접 습득 가능하며, 슬롯에서 기존 장착 무기 기본공격 경로를 호출한다. 기존 공격 버튼과 action-ID 쿨타임을 공유해 별도 피해/재사용 우회를 만들지 않는다. 실제 utility/common-filter/cell/learn/register/HUD touch 경로를 automated test로 검증했다.
- 29개 단일 공격/자기회복 액션을 real ID 아래 명시적인 B/ADAPTED 시험 수치로 shared Resolver에 연결했다. 기술 MP0, 마법 MP차감, 자기회복/maxHP cap, 액션 ID별 쿨타임, 중복 슬롯 재사용 차단/만료, 거부 입력 MP 무차감. 성공한 플레이어 액션마다 숙련 1 증가/100 cap은 ADAPTED. 원작 속성/특수/광역/그룹/은신/승급 효과와 모든 219개 효과를 구현했다고 하지 않는다.
- Runtime source **30cb18baa2268c60067bd3f6d33e921b935154f8**, based on main **7329840e7f0458cd315409f2e43b0a8bec375a27**. PR [#170](https://github.com/ChanJoos/Project_dark_android/pull/170). Actions [36631412453](https://github.com/ChanJoos/Project_dark_android/actions/runs/36631412453) SUCCESS; job 109621294136; checkout is PR merge **8f8491fb1e5387dd3f4484849f42861df90601a2**. All configured checks and assembleDebug passed.
- v0.59 / versionCode59, APK artifact **11061669352**, render artifact **11061664240**. Build completed **2026-09-30 06:11:53 KST**. APK 13,057,259 bytes, SHA-256 **4e47fc87e67321598dfe1b253f1b57b2597b0382fb765dbf78d23c9af5391bac**. Packaged atlas/icon/acquisition bytes exactly match authored files; packaged catalog Git blob **0e3a04beaa7bbda6f51e9de23eb5a81663210547** matches current Master projection, 219 rows; 135 icon mappings.
- Native 960x540 commoner-learned and cooldown frames inspected: seven filters/common route, learned/register controls, icon rail, visible total/remaining timer, no text/panel overlaps observed. This is automated native rendering, not physical-device acceptance. Martial-artist/class acquisition tests inject job state; a fresh normal save cannot yet enter those jobs because the profession route is incomplete.
- Status: **IMPLEMENTED / BUILD_VERIFIED / AUTOMATED_INPUT_VERIFIED / NATIVE_RENDER_REVIEWED / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING**. Remaining: real profession route; missing material/event/circle conditions and source-name resolutions; unsupported effects and original balance; persisted offline cooldown policy; physical-device/UI acceptance. Do not label this APK as the completed original-skills game.

| Task | Owner | 상태/근거 | 다음 결과 | 수락 기준 |
|---|---|---|---|---|
| SKILL-WINDOW-03 | Director/Integration | IMPLEMENTED / BUILD_VERIFIED; source 30cb18b, run 36631412453 | v59 기기 검수 및 실제 전직→직업 스킬 습득 경로 연결 | 조건 실패/성공, 등록/해제/재시작, MP/쿨타임 차단과 만료, 설명 안정성; 특수효과 미구현은 별도 완료 기준 |
