# PROJECT DARK V105 검증 기록

Lv40 성장 경로·직업·퀘스트·장비·NPC·회복·정수 제단·저장 복원을 구현하고 후보 APK를 공개했습니다.

- APK 소스: `5ebf1fc2c7fd07bc004b4863f86dd9204edfdadd`
- Actions: https://github.com/ChanJoos/Project_dark_android/actions/runs/37254439040 — 성공
- 전체 설정 검사: 248개 구성의 Gradle 회귀 검사 통과. 최종 XML을 별도로 내려받아 재집계하지 않았습니다.
- 빌드 완료: 2026-10-05 11:30:40 KST (Actions 로그 기준)
- 설치 파일: [PROJECT_DARK_V105.apk](https://github.com/ChanJoos/Project_dark_android/releases/download/candidate-v105-5ebf1fc2/PROJECT_DARK_V105.apk)
- 파일 크기: 52869317 bytes
- SHA-256: `32c95a7dcf5f397f9f829776a48f25ddf7cf9b7cb3475ce10ece571c27cad2a8` (GitHub 릴리스 서버 메타데이터)
- 후보 릴리스: [candidate-v105-5ebf1fc2](https://github.com/ChanJoos/Project_dark_android/releases/tag/candidate-v105-5ebf1fc2)

## 구현 및 검사 범위

| 직업 | 도달 레벨 | 완료 퀘스트 | 유효 기술 연습 | 누적 획득 경험치 |
|---|---:|---:|---:|---:|
| WAR | 40 | 23 | 9 | 9,003,000 |
| ROG | 40 | 23 | 9 | 9,003,000 |
| MAGE | 40 | 23 | 9 | 9,003,000 |
| CLERIC | 40 | 23 | 9 | 9,003,000 |
| MONK | 40 | 23 | 9 | 9,003,000 |

공통 20개와 직업별 총 15개 정의, 캐릭터별 23개 퀘스트 경로를 구현했습니다. 실제 Resolver와 자원 소비를 사용한 도메인 시뮬레이션입니다. 강제 경험치·무한 MP·시험용 기술을 사용하지 않았습니다. 배치·휴식 픽스처를 사용했으므로 실기기 종단 플레이나 실제 소요시간 측정으로 간주하지 않습니다.

4개 숲 인스턴스와 Piet 안전 캠프는 기존 Pote 지형을 재사용합니다. Lv3 무료 직업 선택, 직업 지도·기술 연습, Lv11/26 장비, 실제 경험치·골드·아이템 보상, HP/MP 물약·상점·판매·무료 NPC 회복, 세 정수 제단, Lv40 최종 보고, schema4 저장 및 거래 복원을 포함합니다.

승인된 BODY·이동·소스 바이트를 보존했습니다. 기존 몬스터 72 PNG 해시를 확인했고, 누락된 구형 초안 8 PNG는 원래 Git blob 바이트로 복원했습니다. 현재 NPC 20명, 방향별 80개 렌더는 원본 종이인형 소스 비교 범위에 포함했습니다.

## 완료 조건과 남은 확인

- 전체 설정 회귀 검사 및 APK 빌드: PASS
- 공개 후보 APK와 소스 커밋·서버 해시 연결: PASS
- 실기기 설치 및 사용자 화면 승인: PENDING
- 최종 APK 독립 다운로드·바이너리·서명 검사: 환경 연결 중단으로 미실행
- 대화창 파일 첨부 저장: 환경 연결 중단으로 미실행; 위 GitHub 링크에서 다운로드 가능
- 고유 원본 지형, Piet 전체 마을, 신규 상담사 실내 건물, 시간제 직업 시험, 강화 몬스터 고유 그림: 제안 단계

main `bfd668d4e178fa82625d634b5a54be0e27ce30a3`는 변경하지 않았습니다. 후보 브랜치 문서 갱신 커밋은 APK 소스와 구분합니다. 이전 다른 소스 APK의 독립 서명 검사는 이번 APK 검증으로 전용하지 않았습니다.
