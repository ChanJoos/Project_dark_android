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
| SKILL-WINDOW-03 | Director/Integration | IMPLEMENTED / BUILD_VERIFIED; source 30cb18b, run 36631412453 | SUPERSEDED by SKILL-WINDOW-04: 직업·선행 숙련 습득 제한 폐기 | 조건 실패/성공, 등록/해제/재시작, MP/쿨타임 차단과 만료, 설명 안정성; 특수효과 미구현은 별도 완료 기준 |


## 2026-09-30 — SKILL-WINDOW-04 실제 게임 스킬창 / v60

- 사용자 보고: 이전 스킬창의 아이콘 아래 이름 정렬, 빈약한 설명, 애매한 조건과 파란 모서리를 거절했다. 이전 전달 v59 SHA-256 `4e47fc87e67321598dfe1b253f1b57b2597b0382fb765dbf78d23c9af5391bac`는 VISUAL_REJECTED / DEVICE_FAILED (user-reported); 실제 설치 APK identity는 독립 확인하지 않았다. 이전 CI 성공을 사용자 수락으로 유지하지 않는다.
- 최신 사용자 확정: 원작 습득 규칙에서 벗어나 스탯과 돈/필요 아이템만으로 창에서 직접 습득한다. 숙련도와 선행 스킬을 제거하며 직업/승급/써클/레벨도 습득 제한으로 쓰지 않는다. 명시적 습득 버튼 한 번으로 검증→비용 차감→습득→저장한다. 창을 여는 것만으로 자동 소비하지 않는다.
- 새 별도 project policy는 Master 219행/원본 캡처를 보존한다. Gold 등급 가격 50/150/500/1200/2500/5000, 기본공격·문열기 무료, 6개 재료 레시피는 PROJECT_ADAPTED_V60 설계다. 캡처 스탯을 재사용하며 미확정 수치는 프로젝트 tier fallback; 원작 가격/룰 확정이 아니다. 모든 학습 ID에 정확한 stats/Gold/items/요약이 있으며 CI 재생성 일치를 검사한다.
- 네 열×세 행 카드, 아이콘/이름 중앙 정렬과 두 줄 이름, 기술/마법·분류·배운 스킬 필터, 고정 설명/습득 조건 탭, 실제 MP/위력/회복량/재사용/대상, 요구/현재 스탯 및 필요/보유 Gold·아이템, 하단 8슬롯으로 재구성했다. 본문은 탭해도 바뀌지 않으며 단어 경계에서 줄바꿈한다. 135 exact-ID 원본은 그대로 보존하고 native cache에서 cyan 캡처 배경 모서리만 마스크/clip한다.
- 경제 및 저장: 전체 자원 확인 후 한 번 차감. 중복 습득 차감 없음. 학습 checkpoint 실패 시 Gold/items/equipment/book을 복원. 등록/해제 실패도 기존 슬롯을 복원하며 read-only 저장에서 비용/등록 변경을 거부한다. 기존 save 호환은 유지한다. 성공 액션의 숙련 값은 기존 save 호환 목적으로 남지만 조건/창에는 쓰지 않는다.
- Runtime source **e0f61bf1712e7b18ece4c9602e5dbf82da0bf6f3**, base **1927ef9204a288ff35cd4fd440cc36204a77f0c2**, PR [#171](https://github.com/ChanJoos/Project_dark_android/pull/171). Actions [36636635716](https://github.com/ChanJoos/Project_dark_android/actions/runs/36636635716) **SUCCESS**, job **109638772772**. Actual checkout is synthetic PR merge **a9a3bdf2a89bfe7be7d84ef69e20afc5b2b9a978** (head e0f61bf1712e7b18ece4c9602e5dbf82da0bf6f3 into base 1927ef9). First run 36636038805 failed at boxed Gold JUnit assertion compile ambiguity; corrected before the final passing run. Earlier passing 36636243096 predates final word wrapping/read-only tests and is not the delivered APK.
- All 13 SkillWindowTest cases and configured Master/catalog/policy reproducibility, movement/HUD/target/combat/growth/quest/save/restart/render regression checks and assembleDebug passed. Input evidence includes fresh COMMONER basic attack through real utility→common card→learn→slot→HUD and paid martial learning with injected stats/Gold but no profession injection. Self-heal/cooldown examples inject learned IDs for isolated timing review; not physical-device evidence.
- APK versionCode **60**, versionName **0.60-game-skill-book**. Build completed **2026-09-30 07:00:03 KST**. APK artifact **11064287506**, render artifact **11064462402**; names carry checkout a9a3bdf rather than head source. APK **13,067,379 bytes**, SHA-256 **8a11132af58c2615ef5e2b00e4ae9ea33115c5799d62ecbaa3570bec1d91f073**. Packaged policy/icon atlas/icon mapping/capture bytes match authored assets. Packaged catalog Git blob **0e3a04beaa7bbda6f51e9de23eb5a81663210547** matches the exact repository projection; local materialized copy had only one extra final newline, not a source difference. Catalog 219 / policy 222 / source mappings 135.
- Final native Canvas renders inspected: magic explanation, missing/ready requirements, learned slot assignment, duplicate-slot cooldown, empty state. Required/current values, prices/owned counts and labels fit their panels; card names align and no cyan corner triangles remain in inspected icons. Render tests explicitly check transparent corner pixels. These are native automated renders, not physical screenshots or user visual acceptance.
- Status **IMPLEMENTED / BUILD_VERIFIED / AUTOMATED_INPUT_VERIFIED / NATIVE_RENDER_REVIEWED / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING**. Pending: physical-device UI/save/slot/cooldown acceptance; unsupported special/AOE/utility/stealth/advanced effects; missing or unresolved source icon/name rows; four unknown descriptions; offline cooldown persistence. Existing 29 adapted attacks/self-heals + basic weapon attack are the combat scope. Job acquisition route remains a separate game task and no longer blocks skill learning.

| Task | Owner | 상태/근거 | 다음 결과 | 수락 기준 |
|---|---|---|---|---|
| SKILL-WINDOW-04 | Director/Integration | IMPLEMENTED / BUILD_VERIFIED / AUTOMATED_INPUT_VERIFIED / NATIVE_RENDER_REVIEWED; source e0f61bf, run 36636635716 | v60 사용자 기기/UI 검수; 특수효과는 별도 게임 작업 | 스탯·Gold·재료의 명확한 판정, 한 번 차감/저장 실패 복구, 등록/해제/재시작, 쿨타임 차단/만료, 이름 정렬/파란 모서리 제거 |


## 2026-09-30 — SKILL-WINDOW-05 모바일 스킬 흐름 / v61

- 사용자 “한번에 처리”는 앞선 출시 품질 검토의 아이콘/가독성/상태/설명/반응/미구현 결제/경제 경로 문제를 한 작업으로 수리하도록 승인했다. v60의 출시 품질 부족은 이번 검토 판단이며, v60에 대한 새 physical-device 실패 보고나 설치 SHA 확인으로 기록하지 않는다. 이전 v59 사용자 거절 이력은 보존한다.
- 기본 목록은 실제 효과가 있는 real-ID 30개(29 adapted 공격/자기회복 + 무기 기본공격). 219개 Master/135 source icon/222 policy와 기존 learned/slot save는 보존한다. 전체 자료 보기로 미구현 항목을 볼 수 있지만 습득/결제하지 못한다. 내부 fixture는 기본 목록에 없다. 직업·숙련·선행·승급/써클/레벨은 여전히 습득 조건이 아니다.
- 여섯 큰 카드, 기술/마법, 전체/습득 가능/배운 스킬, 분류 dropdown으로 조작을 줄였다. 카드 이름 14px/제목22px/설명14px, 36–40 logical px 버튼/46px 슬롯. 카드에 조건 부족·습득 가능·완료·등록 슬롯, 창 안 습득/등록/오류 피드백과 등록 슬롯 강조, 필드 사용 안내를 표시한다. 모바일 dp/손가락 수락은 기기 확인 전이다.
- 원본 source bytes를 유지하고 작은 source art는 40 logical px 이하로 표시한다. 공통 프레임과 cyan-corner mask, 원본 없는 supported ID의 project Canvas sword/fist/heal/attack emblems를 쓴다. 다른 스킬 이미지를 빌리지 않으며 이 emblems는 원작 아트가 아니다. 캡처의 낮은 해상도 자체를 복원했다고 주장하지 않는다.
- 효과/조건 설명은 실제 MP·쿨타임·대상·기본 위력/회복량 및 공격력/방어/속성 또는 회복량=base+FinalStats WIS 식을 안내한다. Heal adapter도 FinalStats WIS를 사용하도록 맞췄다. 습득 완료 후에는 소비한 재료/Gold를 부족 조건처럼 표시하지 않고 추가 결제/아이템이 없음을 표시한다.
- 도메인 ReagentPurchase를 새로 연결: 기존 멀린 counter의 쿠라눔에 ADAPTED **50G** 가격. 구매 버튼→Gold/items→checkpoint; 실패 시 전부 복원. 기존 다른 reagent/recall 가격은 unresolved이며 구매하지 않는다. Gold/보유량/구매 확인도 상점 안에 표시한다. 첫 기술50G=기존 훈련 보상25G 두 번; supported 쿠라눔 recipe는 학비150G+재료50G=200G(훈련8회 Gold 상당). 실제 플레이 시간/전체 경제 수락이 아닌 source-economy 기준 검사다.
- Runtime source **89bad5a620324f9e5b0b3c8fbc7191fe59fb2a1f**, PR [#172](https://github.com/ChanJoos/Project_dark_android/pull/172), base **fae835dee6d5475efb9db67f07da45e90b666be3**. Actions [36644434203](https://github.com/ChanJoos/Project_dark_android/actions/runs/36644434203) **SUCCESS**, job **109663993916**. Actual synthetic checkout **e481a9010979967c1c404979cca69408ddf8feda** merges source89bad5a into basefae835d. Earlier passing runs precede final feedback/calculation/material/use proof and are not the delivered artifact.
- All **16 SkillWindowTest** cases and configured Master/catalog/policy equality, HUD/world/target/combat/growth/quest/save/restart/render checks plus assembleDebug passed. Paid flow uses an explicit shop-context/stat/Gold fixture, then actual counter button→book learn→register→field HUD use→MP50→32 once/repeated input blocked→heal5→87→checkpoint/restart Gold/material/skill/slot restoration. This does not claim independent physical walking to the shop; the existing door route was not rewritten. Fresh COMMONER basic attack UI and the two-training-reward technique benchmark also pass. Unsupported/archive no-charge, save failures and old-ID preservation pass.
- versionCode **61**, versionName **0.61-mobile-skill-flow**, build **2026-09-30 08:19:43 KST**. APK artifact **11067706867**, native review **11067821705**; artifact names carry synthetic checkout e481a90. APK **13,070,503 bytes**, SHA-256 **32155516e6213d8804e8f5751563ea7fa34e5f1c43dfcad061c30a02ea4e2571**. Packaged policy/source atlas/mapping/captures match authored bytes; catalog Git blob **0e3a04beaa7bbda6f51e9de23eb5a81663210547** matches authoritative source.
- Native final overview, ready requirements, completed/registered conditions, actual shop purchase confirmation, shared cooldown reviewed: names/fields fit panels, completed costs no longer falsely demand resources, slot feedback visible. No subjective user/device visual acceptance is inferred.
- Status **IMPLEMENTED / BUILD_VERIFIED / AUTOMATED_INPUT_VERIFIED / NATIVE_RENDER_REVIEWED / DEVICE_PENDING / VISUAL_ACCEPTANCE_PENDING**. Remaining gates: real phone touch/text/readability and shop walk/restart acceptance, play-session economy tuning, higher-resolution/exact missing original art. Special/AOE/utility/stealth/advanced effects remain unimplemented and are safely archived, not sold. This is the improved skill-flow build, not certification that the entire game is launch-ready.

| Task | Owner | 상태/근거 | 다음 결과 | 수락 기준 |
|---|---|---|---|---|
| SKILL-WINDOW-05 | Director/Integration | IMPLEMENTED / BUILD_VERIFIED / AUTOMATED_INPUT_VERIFIED / NATIVE_RENDER_REVIEWED; source89bad5a, run36644434203 | v61 실제 기기 사용/경제 세션 검수 | 목록에서 판정→재료 구매/학습→등록→필드 사용/쿨타임→재시작, 텍스트/터치/완료비용 의미; 미구현 결제 없음 |


## 2026-09-30 — SKILL-WINDOW-06 v62 preferred grid restoration

User prefers v60's 4×3 grid over v61's six-card layout. Restore centered icon/name cells, seven direct job tabs and compact description/requirements frame. Preserve v61's 30 supported actions, unsupported no-charge archive, stat/Gold/material atomic learning, Curanum purchase, FinalStats healing, slot checkpoint rollback and shared cooldown. Production-input tests follow the restored coordinates. Source af63d7d24e3734700cfd414ddac65f96b543ac2c; PR #173; Actions 36667530858 SUCCESS. Physical device and user visual acceptance pending. Previous v61 layout is user-rejected, not a new combat failure report.

Final verification: Actions 36667530858 / job109735322078 SUCCESS, source af63d7d24e3734700cfd414ddac65f96b543ac2c; actual synthetic merge checkout cf46ea11259232588035483637872f0a7d6501bd into main48037b694b5a034e5851c1a240b61cd05dc81682. All 16 SkillWindowTest cases and configured regressions/assembleDebug passed. APK artifact11076641254, versionCode62, built2026-09-30 13:10:54 KST, 13,069,271 bytes, SHA25626e5da74bc6e743fd3decfd9719cde101983f3ae2573ef0201dc32a362a49229. Packaged learning policy/icons/captures match retained source bytes. Render artifact11077080799 overview/shop-learned/cooldown reviewed: centered grid labels, direct tabs, completed costs, slot feedback and shared cooldown visible. Physical-device and user visual acceptance remain pending. Closure commit changes documentation only.


## 2026-09-30 — SKILL-PRESENTATION-01 v63

User-authorized source male/female pose decisions and separate caster/recipient VFX. 219 ID mappings/268 unchanged source frames, newly generated ADAPTED atlases. Current30 combat actions connected; archived189 do not become playable. Missing garment-group motions use existing standing layer with explicit adapted registration. No finished sex-selection UI. Exact original FX mapping count0; web search/reference gaps and source/status distinction in SKILL_PRESENTATION_CONTRACT.md. Build/runtime evidence follows; physical-device/user acceptance pending.


Verification: implementation source ee643167680d83de47cc57a073610df4bb137ed7; PR #174; Actions36682111983/job109779630434 SUCCESS. Actual PR checkout1b377c71cc18d3d2f4482b885dc15d8a7dc8d7bd merges that source into main8525060cee3fec3c4fa8ab3d60bb02d8d0f19b60. Catalog regeneration, 4 SkillPresentationTest cases, 16 SkillWindowTest cases and all configured regressions/assembleDebug passed. Body source268/268 byte equality verified. APK artifact11082039016 (uploaded2026-09-30 16:11:59 KST), versionCode63, 18,693,002 bytes, SHA25619ce62df1a6f4629ae7a99b4219c718f86a2910e078fd60d594e07797ccac542. All273 packaged presentation files match retained source bytes. Native render artifact11082541202 reviewed: both genders/four directions/seven pose families, monster fire/blunt impact and player heal anchors visible. Static garment registration and original source semantics remain ADAPTED/UNRESOLVED; no full visual acceptance claimed. Physical-device/user acceptance pending. Closure changes only documentation and an additional pre-impact caster capture fixture; app runtime/assets stay identical to the verified implementation source.


## 2026-09-30 — SKILL-TEST-02 v64

User requests beginner testing of every skill and questions cross-job availability. Supersedes the previous stats/Gold-only cross-job permission: normal learning/use and default available list now enforce current job plus common skills. Old learned entries remain in save but cannot bypass job. Activity starts test mode ON by default (saved toggle honored). 219 real entries are virtually learned/test-accessible; separate test quick slots persist outside the normal book, leaving actual learned/proficiency/slots untouched. Test combat30 uses existing Resolver with MP cost refunded; other189 are explicitly visual-only previews with no invented damage/status/teleport/summon mechanics. All magic uses original f1 rear/f3 front overhead two-arm charge/contact, f0/f2 recovery, both sexes. UI distinguishes test combat support from visual-only and has ON/OFF. No Master numeric original claim. CI/native/physical-device evidence pending.


Verified implementation76422fa364a6936234b7c6260714a07e64d27988; PR175; Actions36691264703/job109808785633 SUCCESS. PR checkout6d1d51aab131248c2a2c9cef4948075b6d28b18b merges implementation into main89c49b39ab44ca4106a4a91b6723b45681b4b748. Four new SkillTestModeTest cases, four SkillPresentationTest cases, 16 SkillWindowTest cases and all configured regressions/assembleDebug passed. Tests cover real Activity default ON and restart OFF, all219 virtual learned/usable entries, normal job rejection, save isolation, MP0 supported magic, every unsupported mage/cleric spell and real slot edit/restart. Native artifact11085513953 reviewed: cleric59 entries/test label, poison caster/recipient anchors and shared overhead source pose. APK artifact11085469099 uploaded2026-09-30 17:43:37 KST; versionCode64; 18,695,978 bytes; SHA2567043d8bb59416b62afdf8a7c8cbe5f225d575bb45d9762bf78727bb33f11c052. All273 packaged presentation assets match verified source bytes. Implemented/build/native-input runtime verified; physical-device and user visual acceptance pending. All219 are test accessible, not219 fully implemented mechanics: existing30 real combat; remaining189 explicitly preview-only. Closure documentation changes no runtime/APK bytes.


## 2026-09-30 — SKILL-SPATIAL-03 v65 candidate

User rejects v64 all-class motion/effect/range behavior: DEVICE_FAILED / VISUAL_REJECTED (user-reported). Code inspection reproduced absent per-skill tile rules, preview bypass and passive/finisher alias errors; source exact original visuals remain unverified. Current task is all-class original research and actual-path correction, branch `codex/original-skill-contract-v65`, base bfd668d4e178fa82625d634b5a54be0e27ce30a3. See SKILL_SPATIAL_CONTRACT.md and219 reviewed SKILL_ACTION_DECISIONS.csv. Shared Resolver now owns test presentation legality and area recipients; expanded72 offense/16 heal plus equipped basic paths, reconstructed finisher shapes, source kick extensions and passive/basic-link distinctions. Persistent status, travel/landing and exact original effect/finisher-pose evidence remain incomplete. Candidate implementation; exact-SHA CI/native verification pending. Do not describe219 original skills as fully implemented or source-authenticated.


## 2026-09-30 — SKILL-SPATIAL-03 v65 verification resumed

- IMPLEMENTED: source `fd3e33176859956780c4fd752d8d1381c18280a7`, branch `codex/original-skill-contract-v65`, PR #176, base main `bfd668d4e178fa82625d634b5a54be0e27ce30a3`. This remains an unmerged candidate. Compared the active source with current main; the 30-file delta contains this task. All219 reviewed IDs have per-ID spatial/presentation decisions;72 damage and16 heal definitions use adapted numeric balance.131 entries have presentation-only mechanics, including gated interactions/passives/unresolved entries, and are not fully implemented abilities.
- BUILD_VERIFIED: push Actions run [36702916604](https://github.com/ChanJoos/Project_dark_android/actions/runs/36702916604), job109846298749, checkout/source `fd3e33176859956780c4fd752d8d1381c18280a7` succeeded. Regeneration, SkillWindowTest, SkillPresentationTest, SkillTestModeTest, SkillSpatialContractTest, configured combat/world/save/monster regressions and assembleDebug passed. The PR check run36702922238 also passed; the delivered artifact below is from the exact-source push run, not the synthetic PR merge.
- NATIVE_RUNTIME_VERIFIED (automated scope): native GameView input/Resolver tests cover all-class recipient masks, range/LOS/viewport checks at start/contact, moved-target cancellation, passive/utility gates, linked basic tiers and per-recipient VFX deduplication. Downloaded rendering artifact11090284176 and inspected male/female four-direction pose sheets plus Warrior Crasher/DevilCrasher, Rogue assassination, Martial Dara, Mage Meteor and Cleric HolyDragon production screenshots. Impact screenshots prove these fixture frames only, not every animation frame or physical-device behavior.
- APK: artifact11090074775, versionCode65 / versionName `0.65-source-skill-contract`; built2026-09-30 19:33:56 KST (APK artifact file timestamp; upload19:33:57 KST),20523070 bytes, SHA256 `3e2b1c7bd81a09333696c8658351bafe5f73d95be00e4a925cf7a8c72d5ee5ca`. All274 packaged presentation files match verified source bytes. All268 packaged male/female BODY frames match `master/assets/animation_frames/body` byte-for-byte;219 catalog decisions present.
- DEVICE_PENDING / VISUAL_ACCEPTED_PENDING: original pixel-effect matches remain0. Source-described/reconstructed effects, adapted balance and selected source poses are not authenticated original per-skill animation. Persistent buffs/poison/sleep/recognition reset, travel/landing, summons, exact finisher poses/reaches, Pacheongak/Climens/Dinosense and gender equipment motion gaps remain open. Further targeted web searches did not supply usable exact finisher reach/motion or Pacheongak mask evidence; no original fact was promoted from a search snippet.
- Next work: reproduce the reported motion/range scenarios on this exact candidate APK; resolve individual original finisher pose/range and remaining unknown masks from identifiable combat video/source frames; then implement persistent status/travel/summon services through existing domain modules. Do not mark all219 mechanics complete or physical-device/original visual acceptance passed. This closure changes documentation only and does not rebuild or relabel old runtime bytes as a new version.


## 2026-09-30 — v66 quick-slot approach follow-up (implementation pending CI)

User reports v65 quick slots reject out-of-range use and rejects adapted VFX as unlike the original (DEVICE_FAILED / VISUAL_REJECTED, user-reported; exact installed SHA unconfirmed). Continue PR176 branch; main remains bfd668d4e178fa82625d634b5a54be0e27ce30a3.

- Added one-shot SkillApproachController intent wired to quick slots in Milles/Pote. World BFS finds shortest reachable legal range/LOS/viewport tile, including retreat for minimum-range skills; tracks moving target reserved tile. MP/cooldown is charged only by Resolver on submission, once. Direct input, target change/death, player death, map change, pause or test-toggle cancels.
- Added SkillQuickslotApproachTest for native quick-slot input, one-shot charge/hit, minimum-range retreat, cancellation, blocked route/four directions and moving-target replanning. Generator and Master validation pass locally. Android tests/build/device verification remain PENDING until exact CI evidence.
- Complete defense now uses a thin blue shell reconstructed from supplied combat video. Crasher/DevilCrasher use shared blue/red procedural vortex instead of v65 generic generated columns. These are still ADAPTED, not extracted/authenticated original frames; all219 original VFX request remains OPEN. Existing elemental/status atlases and other generated finishers have NOT been promoted to original.
- Owner: Director/Integration. Next: run exact-source CI, diagnose failures, review native runtime captures; obtain identifiable original skill effect frames and replace remaining adaptations. No full original visual completion or physical-device success claimed.


## 2026-09-30 — v67 original combat capture source (CI pending)

User supplied Naver cafe13434008/article401229 and six GIFs; replaces prior insufficient-search conclusions. Original blog https://m.blog.naver.com/180921/221777831873 publicly provides nine labelled GIFs. Downloaded all9 through the page-observed w800 CDN URLs, retaining bytes and hashes in master/source/skill_fx/naver_401229/provenance.json. This is FAN_GAME_CAPTURE, not an authenticated native game archive or current server numeric canon.

CapturedSkillFx routes nine exact skill IDs: Crasher, MadSoul, DevilCrasher, MadSoulJin, Assassination, AssassinationJin, Dara, Guyang and Dalma. 112 real capture frames preserve nonuniform GIF frame times (one cycle, excluding idle recording waits). Extractor removes floor and captured actor/text; SCREEN alpha and pivot/scale are explicit matting adaptations. Occluded/clipped effect pixels stay missing, not generated. Old procedural/generated recipient effects and generic caster pulses are disabled for these9. Other210 IDs do not become original effects by this change. Male/female selected BODY bytes and actual combat rules stay unchanged.

Validation pending: CapturedSkillFxTest checks9 ID mappings, source/atlas SHA hashes, nonuniform timing, transparency, actual GameView submission/contact/recipient-only emission/expiry and9 production screenshots. Existing quick-slot full-frame and combat/save/world regressions remain enabled. Exact-source CI/APK/native visual review required; physical device and all219 original visual acceptance remain pending.

Capture anchoring: source GIFs show the effect around the caster, so captured contact pulses follow the player foot anchor and deduplicate per action (including multi-recipient hits). Transparent actor masks align to that foot; the source does not supply effect pixels hidden by the actor. Runtime hit recipients and damage remain separate.


## 2026-09-30 — v68 classic martial/cleric source candidate

Continue PR176/codex/original-skill-contract-v65 from308bc751. Fully read61 labelled classic rows (27 martial/34 cleric); retained132 source media/56 GIFs. Adds missing martial IDs033/034; catalog221, Benusti stays excluded.55 captured effect IDs /85 caster/contact channels,60 source icon bindings, corrected source descriptions/stats/kinds and ally/group/self rules. Original BODY268 bytes preserved. See CLASSIC_MARTIAL_CLERIC_V68.md and source provenance. IMPLEMENTED_CANDIDATE; exact-source build/native review/device/visual acceptance PENDING. Persistent status, travel, summon, party and finisher formulas remain unimplemented; this is not61 complete mechanics. v67 prior run36715127755 SUCCESS but its legacy-graphics captured screenshots were black; corrected native-render test now required.
# 2026-09-30 — USER-SKILL-SOURCE-01 Naver class sources and icon completion

| Task | Owner | Status/evidence | Next result | Acceptance |
|---|---|---|---|---|
| USER-SKILL-SOURCE-01 | Director/Integration | PARTIAL / FIX_CANDIDATE: shared `SK_공통_010` icon added from exact screenshot rows; source hash/crop verified; 136 screenshot mappings. Articles NOT_READ because no browser target was attached and web access was blocked. Diagnostic run36733585836 identified Robolectric Korean asset-path lookup failures, a utility-mode contract mismatch and stale presentation expectation; ASCII asset-path migration and focused fixes are in the active branch; exact-SHA CI PENDING. | Verify the current fix with exact-source CI; then reopen the 11 supplied articles from an authenticated browser and verify exact skill rows, art/motion/effect mappings and remaining icons. | Exact-SHA build/tests pass; each claimed fact has article/image provenance; no guessed icons; device/visual status separate. |


## 2026-10-01 — Offline Cafe collection checkpoint (supersedes prior access status)

User explicitly permits Cafe collection and future revisits as needed; thorough offline preservation is the objective. Nine of eleven requested article bodies are now retained under master/source/skill_fx/naver_cafe_archive_20261001 (all Warrior circles/promotion, finisher, Martial and Cleric). All225 observed media URLs are retained as213 unique byte files, including65 GIFs;86 new binaries, remaining references reuse tracked identical source bytes. Hashes, source URLs, image indices, frame counts/durations and observed CDN resolution policy are recorded. Offline evidence index contains95 labelled sections/table rows and2 explicit source conflicts; no unresolved historical/current formula was silently made canonical.

IMPLEMENTED / SOURCE_RETAINED: archive, offline media verifier and evidence index generators. Runtime candidate keeps existing classic/icon/capture implementation; this archive does not accept new Master mechanics. Original icon coverage140/221,81 gaps; BODY, persistent mechanics and full original VFX remain open.

BUILD_VERIFIED: complete Actions run36776004466 SUCCESS on exact runtime/test source4f8bec32c2d8d8c54d87c919c2a884317c39c0b5. Corrections align caster assertions with frame event drain and Kurus with CLASSIC_CAPTURE. DEVICE_PENDING / VISUAL_ACCEPTED_PENDING remain. Source checkpoint makes no new APK delivery claim.

BLOCKED: Rogue245450/245456 redirect to nid.naver.com login. Automatic approval review denied authentication-origin access despite authorized Cafe collection. Do not route around the denial. Next: request explicit Naver login procedure authorization, use secure authentication handoff, retain both bodies/media, then close remaining source icon mappings and accepted Master/BODY/VFX/UI changes through project generators and exact-source verification. PR176 remains unmerged on codex/original-skill-contract-v65; main unchanged.


## 2026-10-01 — Cafe archive collection complete / source analysis resumed

Continuation from PR176 head b3e7f5dd0795b1fb53e1c068596c70c772553803 on codex/original-skill-contract-v65; compared against main bfd668d4e178fa82625d634b5a54be0e27ce30a3. Existing authenticated browser session now exposes both Rogue245450/245456; prior authentication blocker is resolved without login-origin interaction or bypass.

SOURCE_RETAINED: all11 requested bodies,350 image references/288 distinct URLs/276 unique exact-byte files/96 GIFs. OFFLINE_MEDIA_INTEGRITY_PASS288/288; source index133 sections/table rows and8 conflicts. Added a sanitized local-only11-article HTML reader, original Rogue text/HTML snapshots, DOM-ordered GIF candidates, and six proposed Rogue magic-kind corrections. Raw commenter/account/profile data are not retained; all comment pages are not claimed complete. Offline collection/index/viewer and Master integrity checks pass.

This batch does not modify runtime/assets, accept the proposed kind changes, replace the81 missing icons or implement persistent status/travel/summon/user services. Previous runtime CI36776004466 at4f8bec32 still governs the prior implementation; it does not verify new source tooling. No new APK delivery/device/original visual acceptance claim.

NEXT: accept/test six source-backed Rogue magic classifications through catalog/presentation generators; visually disambiguate the31 GIF candidates (especially 습격 vs 기습 and combined basic/double/triple demonstration); bind verified caster/recipient channels and source icons; then implement persistent mechanics through existing domain modules. Requesting another Cafe login is no longer the immediate task. Evidence: master/source/skill_fx/naver_cafe_archive_20261001/{manifest.json,rogue_evidence.json,ROGUE_ANALYSIS.md,index.html}; master/changes/ROGUE-2015-SOURCE-REVIEW.json(PROPOSED).
