## V247 최종 입력·화면 검수 후속

최초source825f112f/run39899353646/job120237062108에서20tests중18PASS,2실패. 모든19서비스 actualscene/9상점 BUY/SELL/rollback/restart PASS. 실패2는expectedsourcebitmap Paint기본filter로인한half-pixelalpha127비교 및coastfixture enterPoteField생략. expected에도nearest명시,실제필드진입fixture로수정;assertion완화없음. 실제캡처에서영문Weight/Feature왼쪽잔상발견하여localizedFrame표면치환범위수정,가격262sourceX로정렬·긴이름clip,검은tooltip,최초판매분류탭활성. Original source18assets unchanged.

최종8검사: TownServices3 전체source9NPC/19씬/9shop/2coast 재검수 + TownInterior실제5실내window1 + Mine실제door/service1 + Abel실제purchase/bank/church1 + Rucesionequipment3inventory거래1 + Onboarding실제quickguide자동도착·선택품구입1. 최초16관련PASS(NpcAppearance3,CampaignProgress6,TownInterior나머지7)는runtime도메인·testbytes guard로재사용. 기본UI 변경에 맞춰 역사적shopbutton좌표4test파일갱신; runtime가격/거래규칙불변. final CI/APK/source/hash/PHONE pending.

## V247 — 해안 강조원 제거·공통 원본 NPC/상점 (검증 진행)

사용자 사진1000060733/731/735 기준. retained741클라이언트 Hades22frame0여관·30frame5mirror대장장이 확인, Setoa원본449x292Shop/640x108대화창/132x118portrait·버튼·Legend한글2350음절+51자모/ASCII bitmap font 직접복원. 사진JPEG를 확대·AI재작성하지 않음. 모든현재19서비스씬 중 여관4/장비5공통원본NPC,시약·장비9상점 및campaignsupplier 공통원본UI. 새TownInteriorDef서비스kind등록으로신규마을동일자산사용. 사진에는없는무게값은“-”로표시,프로젝트가격/품목유지. 원본영문baked label만같은bitmapfont한글localization; 원작한국서버전체pixel동일성주장아님.

밀레스5mentor제거;J01/J02같은퀘스트ID/진행을기존마이클NPC로통합,피에트J03mentor유지. 경비/이동/마이클/시장4명은실제퀘스트·서비스연결로유지. 두해안ENTRY의추가노란oval만제거,기존portal/입장조건유지. 저장schema/가격/보상/전투/스킬/이동/지형변경없음.

검증범위: sourcepixel전체18자산audit,9NPC정확pixel및futurekind,모든19서비스실제씬/NPC입력·9상점구매/판매/rollback/restart/quantity/top/close,두대기실실제draw;TownInterior8(은행/성당직접회귀),NpcAppearance3(기존paperdoll서비스예외는원본pixel검사대체),CampaignProgress6(다섯직업실행/마이그레이션). 로컬production/newtestcompilePASS,CI/화면검수/APK pending. SamePR197/branch,main41fe6defunmerged;PHONE/USER_VISUAL PENDING.

