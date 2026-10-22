# V240 사용자 영상 대응 정정

User-confirmed: 포테000206 = 마레노001/테라미코003/아듀로004/플라모005. 이전225347 영상 = 4써클 전체030..033 이펙트이며 3써클 단일013..016 공용. V238의026..029 연결은 오판. V239는 승급5종에만 집중해 기본4를 미반영했음.

IMPLEMENTED 후보: basic4 연속20fps 접촉 atlas와 source/plate/frame provenance. 촬영 배우/글자/지형 배제, 가림픽셀은 복원하지 않음(ADAPTED matte). 013..016에 기존 V238 atlas bytes 그대로 재연결,030..033 동일 Channel 공유. 026..029 기존 classic 별도 소스로 복귀. 046각인·V239승급5·포테11원본/바닥 불변. 소모/피해/대상/학습/저장 규칙 불변.

검증범위: source hash/crop/frame/메모리; 새 basic4 실제 quickslot 단일대상/MP1회/수신자FX 및 mapping 2native; 기존 V2385native(이제3circle 단일/4circle전체); V235 원문UI/12원소원자료/전체시전/승급14 접촉; V239승급5 anchor+decoded budget. 렌더러 초기화 공유에 따라 인접승급 포함; 맵/몬스터/저장전체 재검사 없음. 피해공용5audit는 기존V238 native에서 실행. 최종main통합 full gate 유지.

BUILD/NATIVE/PHONE/USER_VISUAL PENDING. 동일 branch/PR197 유지, main41fe6def. 다음: exact source CI→native 실제 화면 검수→APK source/run/hash/signature 확인→후보 전달. V239 source511의 성공을 새V240 검증으로 재라벨하지 않음.
