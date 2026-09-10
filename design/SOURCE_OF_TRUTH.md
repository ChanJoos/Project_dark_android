# PROJECT DARK Source of Truth

Revision M001 / D003 · 2026-09-10

현재 최신 사용자 지시와 실제 감사에 따라 이전 archive-verified 주장을 정정한다. `master/MASTER_MANIFEST.md`와 `master/RECONCILIATION.md`가 현재 확보/검증 상태다.

## 적용 우선순위

명시적 최신 사용자 결정은 적용 범위와 모바일 적응을 정한다. 원작 사실은 실제 원본/검증된 official evidence > Master 원문 > 확정 constitution/data contract > canonical seed > runtime 임시값 순으로 판단한다. 서로 충돌하면 자동 덮어쓰기하지 말고 최신 확정 여부와 버전을 RECONCILIATION/changes에 기록한다. O/V/U/B/ADAPTED/FAN/PENDING_CROP 및 SOURCE/SOURCE+BALANCED/BALANCED를 보존한다.

- 현재 DB baseline: `master/data/*.csv` 92개, 첨부 원본 ZIP `master/source/PROJECT_DARK_MASTER_CONVERTED.zip`.
- 현재 직접 읽은 기획 원본: `master/source/PROJECT_DARK_V0.6_PLAN_KO.docx`; 전체 추출은 `master/design/PROJECT_DARK_V0.6_PLAN_KO.fulltext.txt`.
- 첨부 Markdown: `master/design/PROJECT_DARK_V0.6_PLAN_KO.imported.md`. 기존 Markdown과 19-tab v1.0은 역사적 비교 자료로 보존.
- v0.7 원본은 미확보. v0.6이 역사상 마지막 기획서라는 주장은 금지.
- 손상된 `master/PROJECT_DARK_MASTER_DB.tar.gz`는 현재 데이터 입력으로 사용하지 않는다. compile 성공이 archive 보존 성공을 의미하지 않는다.
- XLSX 원본은 미확보. 92 CSV 전체 해시는 검증했지만 XLSX null/type/empty/cache 무손실은 미검증.

## 살아 있는 Master

원본 스냅샷은 보존하며 개선은 `master/changes/<id>.json`으로 제안/검토/수락한다. source sheet/cell/ID, before/after, 근거와 버전, 영향 코드, 테스트, status를 함께 기록한다. accepted 변경만 runtime projection에 연결한다. detailed procedure와 단일 integrator 권한은 `docs/DIRECTOR_GUIDE.md`를 따른다.

## 실행 범위와 주요 충돌

- 생성은 성별/머리/색/이름 → 평민 Lv1. 직업 선택은 이후.
- 평민→5기본직업→Lv99→전직 OR 순수→Lv99→1차 승급. 이후 활성화 금지.
- **최신 사용자 결정: 몬스터 아이템 보상은 자동 루팅이다. `MONSTER_DEFEATED → reward resolution → inventory mutation`을 사용하며 ground item entity / pickup / pickup pathfinding 경로는 폐기한다. AUTO와 수동 전투는 동일 보상 경로를 사용한다.**
- 자동루팅 전환은 미확정 drop probability/quantity/item relation을 임의 확정할 권한을 주지 않는다. 값이 없으면 PENDING으로 유지한다.
- P00의 Lv1 지하묘지 vs 실제 quest row Lv40~60 충돌은 미해결. 임의로 레벨을 바꾸지 않는다.
- 속도부스터 제외, 잠긴 마을은 획득처에 쓰였다는 이유로 열지 않는다.
- prototype dummy 보상과 원작 monster reward는 구분한다.
- 원작 미확인 sprite는 PENDING_CROP. 전체 screenshot 최종 map texture 금지.

## 시각 자료 수집·복원 정책 — USER CANON / 2026-09-10

- 넥슨 어둠의전설 공식 홈페이지(`lod.nexon.com`)와 넥슨 파일 호스트에서 확보 가능한 과거/현재 게임 이미지는 적극적으로 수집하여 world/character reconstruction의 1차 시각 근거 풀로 사용한다.
- 단, `lod.nexon.com` 커뮤니티에 사용자가 올린 스크린샷은 **Nexon-hosted provenance**는 확정되지만 자동으로 `[O]` official art가 되지 않는다. 별도 공식성 검증 전에는 `[V]`로 유지한다.
- 확보 가능한 원본 시각 정보는 최대한 trace/reconstruct한다. 자료가 존재하는 영역을 임의 창작물로 대체하지 않는다.
- 공식/검증 이미지로 덮이지 않는 genuinely missing 영역은 PROJECT DARK에서 직접 제작할 수 있다. 이때 제작 자산은 `[ADAPTED]` 또는 prototype 단계 `[B]`로 분리하고 원작 자산이라고 표기하지 않는다.
- 신규 제작 부분은 인접한 검증 자료의 픽셀 비례, 팔레트, 타일 크기, 등각 투영 규칙, object silhouette vocabulary를 따라 연결하되, 확인되지 않은 원작 세부 구조나 랜드마크를 창작해 canon으로 승격하지 않는다.
- source pixel / traced reconstruction / adapted fill은 provenance를 분리해 추적 가능해야 한다.
- 전체 스크린샷을 최종 map texture로 사용하는 것은 계속 금지한다. 최종 월드는 `TILE / OBJECT / COLLISION / NPC / MONSTER_SPAWN / PORTAL` 레이어로 분해한다.
- Milles 관련 Nexon-hosted source registry는 `data/design/NEXON_VISUAL_SOURCE_MANIFEST_MILLES.md`를 따른다. 구 마을맵 게시물의 9개 attachment 중 Milles 대응 이미지는 실제 시각 식별 전까지 `PENDING_VISUAL_IDENTIFICATION`; 좌표 transform calibration을 추측으로 확정하지 않는다.

매 실행 `AGENTS.md`, DIRECTOR_GUIDE/BACKLOG, MASTER_MANIFEST/RECONCILIATION, constitution, DATA_CONTRACT, 이 registry, DEV_HISTORY와 담당 전체 테이블/의존 범위를 확인한다.
