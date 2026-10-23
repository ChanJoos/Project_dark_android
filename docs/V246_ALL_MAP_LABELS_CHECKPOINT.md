## V246 — 모든 맵 입구 이름 통합 및 전체 맵 검수 (진행)

사용자 재지시: 밀레스만 수정하는 것은 범위 부족이며 모든 맵을 검수해 같은 규칙 적용. V245는 밀레스만6검사/빌드검증 후보이며 최종전달하지 않음. V246은 WorldMapWindow 공통 grouping으로 밀레스·각마을·일반실내·신전·포테·아벨/뤼케시온해안·광산·호러캐슬·죽음의마을 전체를 처리. raw pin에 실제 destination identity와 threshold 보존; same-destination 72worldpx 연결component만1이름/중심marker로통합. 원거리/다른도착지 분리. Death 기존 전역destination묶음 제거, Horror 문tile별 중복 개선. legacy town/coast alias canonical mapping도 검수. 입구·충돌·이동·몬스터·리콜/시작점·save schema는 불변.

검증: AllMapLabelsV246Test가 활성/복원지원 전체맵 목록과3legacyalias, 모든rawgate의1회coverage/실제target/connected component경계, 실제모든맵HUD열기/고유pin수/원본overview/nativefullcapture/전pin실제input→live길찾기→원본threshold접근/camera/cache를 점검. synthetic chain/원거리/다른dest검사. 기존Milles3·Deathgroup1 직접회귀. 새3+기존4=7native예정. BUILD/SCOPED_HOST_NATIVE/PHONE PENDING; 화면 원본 모든맵 캡처를확인한뒤검수범위보고. 전체게임회귀는주장하지않음.

Base26845b478574159b81855b44534a69c8473246f6/V245runtime, samePR197/branch/main41fe6defunmerged. Next exactsourceCI→allmapinventory/count/path실패수정→전체mapnative캡처검수→exactAPKsource/run/hash/signature→delivery. Localcode/testcompilePASS; pendingCI를완료로표기하지않음.

