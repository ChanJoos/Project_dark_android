# 디렉터 작업 지시

Revision D002 · 2026-09-10 · 사용자 실기기 APK 피드백 반영

현재 전체 게임 상태: BUILD VERIFIED / device movement VERIFIED by user / full vertical slice PLANNED. 최신 사용자 검수에서 이동 자체는 양호하나 world camera, entity scale, action animation regression, combat test HUD가 핵심 blocker로 확인됐다. 기능 수평 확장보다 아래 P0 visual-playability pass를 먼저 닫는다.

## 이번 회차 사용자 수락 기준
1. 플레이어·NPC·몬스터 표시 크기를 현재보다 축소하여 원작 화면 밀도에 가깝게 조정한다. 임의 숫자를 원작 공식값으로 표기하지 말고 runtime visual tuning으로 기록한다.
2. 플레이어 이동 시 플레이어를 화면의 안정적인 anchor/dead-zone에 두고 WORLD LAYERS가 반대 방향으로 scroll하는 camera-follow를 구현한다. HUD/joystick/action buttons는 screen-space 고정이다. NPC/몬스터/portal/drop은 world-space라 map과 함께 움직여야 한다.
3. 기존에 동작했던 전투 모션 regression을 복구한다. 최소 IDLE/WALK/ATTACK/CAST/SKILL_KICK 상태를 실제 버튼 입력으로 관찰 가능하게 하고 action 후 WALK/IDLE로 복귀한다. attack effect/damage는 actionId당 한 번만 발생한다.
4. 우측 하단을 실제 전투 QA가 가능한 HUD로 개편한다. 최소 ATTACK + SKILL + MAGIC + POTION을 동시에 직접 누를 수 있어야 한다. 버튼은 임시 사각형 텍스트 UI가 아니라 원작 어둠의전설 HUD/통합슬롯의 비례·테두리·아이콘 밀도를 공식/검증된 reference와 Visual Manifest를 기준으로 재구성한다. 원작 asset 근거가 없으면 AI/generic fantasy icon을 canon으로 넣지 말고 evidence-safe placeholder로 표시한다.
5. SKILL/MAGIC/POTION 각각의 눌림 상태와 실제 runtime 반응을 눈으로 구분할 수 있어야 한다. MAGIC은 CAST, SKILL은 해당 skill action, POTION은 소비/회복 feedback을 연결한다. 미확정 원작 수치/skill을 발명하지 않는다.
6. 실제 Android runtime screenshot을 artifact로 남긴다. 정적 mockup을 runtime 증거로 사용하지 않는다.

| 우선순위/담당 | 다음 작업 | 의존성 | 수락 조건 |
|---|---|---|---|
| P0 world | entity visual scale 축소 + camera-follow/world-space projection 구현 | WorldLayerContract | 이동 시 map/NPC/monster/drop/portal 동시 scroll, HUD 고정, collision logical coordinate 불변 |
| P0 combat | 전투 animation regression 원인 추적 및 ATTACK/CAST/SKILL_KICK 복구 | action state contract | 각 action 버튼으로 상태 진입 확인, target facing, 종료 후 WALK/IDLE, 중복 damage 없음 |
| P0 director | 우측 하단 combat QA HUD: ATTACK/SKILL/MAGIC/POTION 및 원작형 visual shell | combat+rpg contracts | 4 action 모두 한 화면에서 터치 가능, pressed feedback, UI touch world 관통 없음 |
| P0 rpg | POTION 소비/회복과 skill/magic test projection을 evidence-safe data에 연결 | CSV adapter | inventory 차감/효과 1회, unknown 원작 수치 발명 금지, source trace 유지 |
| P0 director | 위 변경 통합 후 assembleDebug + APK artifact + Android runtime screenshot gate | worker PRs | 동일 SHA build success, APK artifact, 실제 runtime screenshot 및 4버튼 QA 기록 |
| P1 director | Master integrity 21,292 vs 16,222 차이 원인 확정 | source XLSX/manifest | 차이를 셀 유형별 설명하고 integrity 상태 갱신 |
| P1 rpg | 평민 생성 데이터와 versioned save/restore | adapter/공통 계약 | 직업 선택 없는 생성, 재시작 후 identity/성장/가방/퀘스트 보존 |
| P1 director | popup/back/cancel 및 전체 pointer ownership 강화 | UI contracts | 멀티터치 UI world 관통 없음 |
| P1 world+rpg | Lv1 밀레스 퀘스트/몬스터/보상 근거 보완 제안 | RECONCILIATION R04 | Lv40 퀘스트를 Lv1로 몰래 변경하지 않음, ACCEPTED change 후 활성화 |
| P2 director | 전체 vertical slice 검사 후 직업/마을 확장 지시 | P0/P1 통과 | 생성→밀레스→NPC→퀘스트→사냥→전투→loot→inventory→EXP→save/restart 전체 통과 |

## Visual 구현 원칙
- VR01: AI reinterpretation으로 원작 캐릭터/몬스터/UI를 대체하지 않는다.
- VR02: 전체 gameplay screenshot을 final map texture로 사용하지 않는다.
- 원작 HUD는 Nexon 공식 guide/screenshot 및 master Visual_Manifest/Gameplay_Screen_Master/Screen_To_Runtime_Mapping을 먼저 조사한다.
- 화면상 크기/간격은 reference screenshot 대비 측정 가능한 비율로 조정하고 변경 근거를 handoff에 남긴다.
- World coordinate와 Screen coordinate를 분리한다. camera offset은 rendering projection에만 적용하며 logical collision/AI coordinate를 camera에 종속시키지 않는다.

한 번에 담당별 가장 앞의 미완성 작업을 닫는다. 디렉터는 매 회차 결과에 따라 revision을 올린다. 사용자 실기기 피드백은 최상위 acceptance evidence이며, 이번 D002가 이전 D001의 visual/runtime 관련 우선순위를 대체한다.
