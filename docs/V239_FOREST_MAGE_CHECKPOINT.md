## V239 — 추가 마법 영상 / 포테 원본 외형 개선 후보 (2026-10-23)

Director 단일 편집. 기존 branch `codex/rucesion-coast-v157` / PR197 유지, main `41fe6def5120b6c33b58f609e52fbaf2ead700ee`. 마지막 성공 V238 runtime `dc6f1426b61a1584bd819ceed312e82b42002514`, run39806023052; 문서HEAD ba495f2에서 재개. V238 재빌드/검사 반복 없음.

USER: 추가 영상 마법 반영, 1번000206의 포테 맵/몬스터 개선. IMPLEMENTED 후보: 원본741installer hash 검증/REWise 추출, 선택11MPF+palette 보존 및132 original idle/walk/attack pose samples. 기존 generated source/정령/맨티스 보존. 원본별2views+client mirror, native anchor48,76×2와logical96. 구generated 방향예외/mesh 변형은 원본에 적용하지 않음. Korean name table 미확보: 영상에 보이는 팜팻색/앤트자이언트/울프라이더 직접 대조, 나머지 indexed joins 시각후보. 3독립라벨참조 PENDING_REFERENCE. 전체 원작 다중프레임 재생으로 주장하지 않음.

포테 흙3조각을35초 격자없는 바닥에서 quilt; 전체스크린샷/캐릭터/UI 제외. 기존 지형/topology/props/충돌/72·96·108개체수/통계/보상/save/32floorLRU/prewarm/vsync 유지. source terrain receipt 보존. 승급 아이스블러스트/플레쉬스톰/플레어/델리스펠라스/포트리스 5contact 연속영상 matting. 가려진 영역은 비우며 숨겨진 원작픽셀 복원 주장 없음. 피해숫자·대상 겹침이 큰 퀘이크 새 추출은 자체검수 거절, 기존 V237 유지. 다른 디버프/하위써클 영상은 evidence inventory이며 이5개 외 runtime 적용 완료로 주장하지 않음. 마레노/V238각인·4circle/whole4·학습비용/효과수치/무도 불변.

검증범위: source/hash132poses/5FX; new3native(5sequence+14normalpromotion,132pose presentation,12actualforest trips/population/captures), V2385+contact3, PoteMonsterPlayable, ForestFloorCache2(currentmaterial만갱신/algorithm불변), ForestPixelCache, GroundTap, chase performance, restart. 비주얼 source/render/공유cache 영향에 집중. main최종통합full별도. BUILD/NATIVE/PHONE/USER_VISUAL PENDING. 다음: exact commit CI 확인→실제GameView·effect/monstergrid 원본대조→관측실패수정→APK identity/run/hash/signature확인. 재개 시 이행동부터 진행.

