# LOOP-4-V1 — 운영 구조 개정

사용자 요청: 프로젝트 방향/4개 예약의 효과와 월드 비활성 문제를 점검한 뒤 제안한 운영 변경 승인.
Baseline: 2dbcd9c945ee043ff36cb08f4f740ca1204ccad0.

변경: 네 역할 유지, 기존 World 예약 재개, 전체 마을 시각 gate가 무관한 gameplay 작업을 막던 전역 대기 제거. 현재 루프의 역할별 작업 1개와 수락 조건 지정. Director runtime wiring 책임 및 두 회차 blocker 해소, worker IDLE/pause/resume 관리 명시. 기존 시간/예약 ID 유지.
공통 목표: 이동/NPC/전투/직접 보상/성장/저장/프로세스 재시작 복원.
원작 근거/시각 QA/승인 자산/기존 code 보존. 저장/성장은 현재 루프에 포함되며 신규 콘텐츠 완료 이후로 지연하지 않음.
AGENTS, DIRECTOR_GUIDE, DIRECTOR_BACKLOG를 현재 운영에 맞추고 SOURCE_OF_TRUTH에 운영 precedence를 추가한다.

검증: 문서 scope와 diff/지시 일관성 확인. automation 설정 성공은 도구 readback으로 별도 확인.
게임 runtime 코드는 변경하지 않음. 이번 변경으로 새 플레이 기능/빌드/runtime 완료를 주장하지 않음.
