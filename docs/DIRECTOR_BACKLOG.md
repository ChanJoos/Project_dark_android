# 디렉터 작업 지시

Revision D001 · 2026-09-10 · 기반 main c895bf0

현재 전체 게임 상태: IMPLEMENTED prototype / full vertical slice PLANNED. 기존 CI는 compile만 수행했다. MASTER_READY 같은 과거 표시는 runtime 완료 증거가 아니다.

| 우선순위/담당 | 다음 작업 | 의존성 | 수락 조건 |
|---|---|---|---|
| P0 director | 첨부 92 CSV 복구·검증·Master manifest 및 4배치 지침 반영 | 없음 | 92 해시/행/열/수식 검증, main 반영 재조회 |
| P0 director | compile-only CI를 APK 및 Android 실행 gate까지 확대 | source 복구 | 동일 SHA assembleDebug/APK, instrumentation 로그·실제 screenshot |
| P0 rpg | CSV→runtime adapter, evidence 원문 보존, 기존 하드코딩 장갑 교체 | source 복구 | unknown 비용/조건 미확정 보존, source sheet/row/hash 추적, parser 검증 |
| P0 combat | 전투 공통 검증 및 actionId 한 번 효과, 사망 후 시전 차단 | 기존 controller 보존 | LOS/자원/쿨다운 실패 무변경, 반복 resolve 무피해, manual/AUTO 동일 resolver |
| P0 world | 전체 screenshot texture 경로 제거 및 레이어/충돌 정합 | VR01~06 | 실제 runtime 캡처, PENDING_CROP 유지, 원본 없는 sprite 발명 금지 |
| P1 rpg | 평민 생성 데이터와 versioned save/restore | adapter/공통 계약 | 직업 선택 없는 생성, 재시작 후 identity/성장/가방/퀘스트 보존 |
| P1 director | UI pointer 소유권, popup/back/cancel, 가방·ground pickup 연결 | RPG contracts | 멀티터치 UI world 관통 없음, 소비된 drop 재획득 없음 |
| P1 world+rpg | Lv1 밀레스 퀘스트/몬스터/보상 근거 보완 제안 | RECONCILIATION R04 | Lv40 퀘스트를 Lv1로 몰래 변경하지 않음, ACCEPTED change 후 활성화 |
| P2 director | 전체 vertical slice 검사 후 직업/마을 확장 지시 | P0/P1 통과 | 저장/프로세스 재시작을 포함한 플레이 경로 전체 통과 |

한 번에 담당별 가장 앞의 미완성 작업을 닫는다. 디렉터는 매 회차 결과에 따라 이 표를 갱신하며 새 지시는 revision을 올린다. 미해결 코드/자료 문제를 사용자에게 반복 전가하지 말고 실제 진행 불가 blocker만 보고한다.
