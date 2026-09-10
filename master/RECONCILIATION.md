# Master reconciliation

Audit 2026-09-10, source commit 5cc409a, refreshed c895bf0. Latest user authorizes ongoing Master refinement and director coordination of four existing workers.

| ID | 충돌 / 증거 | 처리 |
|---|---|---|
| R01 | 기존 tar.gz claimed SHA 4e478fea91ab5a5a8a86f0a0b31c886c3923797713e744b39c9f0a2b1652ee0b; actual 3362f1b4b96d8e25fb0898a85d2f8803ede37fecb27a4d0358d7aa8d8f3534c4, 7514 bytes, EOFError | 기존 파일은 손상 증거로 보존. 사용 금지. 첨부 ZIP/개별 CSV를 사용. PASS 12의 verified archive 주장은 반증됨 |
| R02 | 첨부 CSV hash/row/column/formula는 manifest와 일치. XLSX populated cells 주장 21292 vs CSV nonempty 16222 | 5070 차이를 보존. 빈 문자열/빈 셀 집계 차이 가능성이 있지만 원본 없이는 확정 불가. XLSX lossless 검증은 미완료 |
| R03 | 기존 Git v0.6 Markdown 18372 bytes, 첨부 Markdown 34445 bytes, 실제 로컬 DOCX 존재 | 둘 다 보존. source DOCX 전체 OOXML 텍스트를 추출. 기존 Markdown을 full original이라고 단정하지 않음. v0.7 원본 미확보 |
| R04 | Progression_Master P00 Lv1~10은 지하묘지 3연퀘를 지시하나 Quest_Runtime_Master Q_MIL_01은 40~58, Q_MIL_03은 45~60 | Lv1 퀘스트로 자동 활성화 금지. 원작 시작 구간 근거 보완 또는 명시 ADAPTED 튜토리얼 제안 필요 |
| R05 | Runtime WorldDef combat_dummy_01 vs Monster_Master 실제 42종 | dummy에 원작 몬스터 보상 붙이지 않음. 현재 사냥의 EXP/drop 연결은 미구현. 시스템 검증 fixture와 canon 구분 |
| R06 | Item SOURCE/SOURCE+BALANCED는 출처/복원 구분인데 기존 장갑 코드가 Evidence.O로 일괄 표기 | adapter는 source confidence와 Source_ID/URL 원문 보존. 공식성은 URL 호스팅만으로 확정하지 않음 |
| R07 | WorldDef.EVIDENCE_VISUAL=O + GameView 전체 screenshot texture | 현재 VR02 위반. 레이어 교체 우선순위 P0. 원작 sprite 미확보를 procedural sprite 완료로 주장 금지 |
| R08 | 문서/DB READY·PASS·YES vs 실행 코드 | 설계자료 작성 시점의 상태 문자열로 보존. 검증 판정은 새 테스트 증거로만 기록 |
| R09 | v0.6 장기 2차/3차와 현재 사용자 확정 범위 | 현재 runtime은 1차 승급까지만. 원본 문구는 삭제하지 않음 |
| R10 | 기존 19-tab v1.0/seed vs 92 CSV | 92 CSV 원문이 현재 사용 가능한 최신 DB baseline. 기존 파일은 historical reference. 공식 증거와 사용자 확정에 따른 개정은 traceable changes로 관리 |
| R11 | Economy_Loop 속도부스터 vs README 제외, locked-town 상점 참조 | 기존 no-go 유지. 원문 행 삭제 금지. Shop_Redistribution/Locked_Towns 함께 적용 |

Master 개정은 `docs/DIRECTOR_GUIDE.md`의 before/after/evidence/impact/validation 절차를 따른다. 원본 XLSX는 제공 시 source SHA와 함께 직접 감사하고 empty cell/type/formula/cache 보존 검증을 마무리한다.
