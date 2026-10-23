## V245 — 밀레스 지도 입구 이름 중복 수정 (검증 대기)

사용자 V244 휴대폰 보고: 밀레스 같은 입구 이름이 두 번씩 표시됨 (DEVICE_FAILED_USER_REPORTED). 원인확인: WorldMapWindow가 같은 건물의 primary/두번째 인접 portal threshold에 각각 Pin을 만듦. 같은 targetMapId·인접거리72worldpx 이내의 연결된 portal cluster를 하나로 묶고 중심에는 이름/Pin1개만 표시. 경로 목표는 centroid가 아닌 실제 threshold 중 중심에 가장 가까운1개를 유지. 동일 도착지도 멀리 떨어지면 독립; 다른 도착지는 인접해도 별도. 원본12portal/trigger/collision/movement/save/도착점/마법 불변, 표시Pin만7개.

영향 runtime WorldMapWindow.java1개, 테스트 MapLabelsV245Test3개, V245별도설치label/package 및 focused workflow. 검증범위: 실제12threshold→7label/실제target, 같은target 인접chain·원거리·다른target 분리,7개 정상HUD pin입력→기존길찾기→portal진입·overviewcache, V244forest/interior 및 mine/horror/coast/templemap2개, 기존Death grouping/cache1개. 미검증 표시 기능의 source/input/render에 집중; save/skills/growth 전체회귀 불필요. Android BUILD/HOST_NATIVE/USER_VISUAL PENDING.

Base e3fd85fdd8b0a7dbee2d77ba5f8c104e896c7ade, same PR197/branch codex/rucesion-coast-v157, main41fe6def unchanged. Next exactsource focused6tests→native Milles 지도 확인→APK SHA/run/signature/hash→delivery. V244 26검사는 역사적 유지; 새 pass로 재표기하지 않음.

