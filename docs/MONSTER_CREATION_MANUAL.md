## V273 Countess candidate acceptance record — Android pending

14 native MPFs383/385/341/342/387/388–391/319/400/401/397/410 retain every indexed frame and pivot. Runtime stores each native source cell once; west directions use source-compatible canvas mirroring. MPF387 contains one static furniture frame; no fabricated attack frame. All14 source species are admitted to scene rosters including dormant second boss and summonable bomb. Specific Korean boss identity joins and original server tuning remain PENDING/ADAPTED. Shared Resolver/AI, actual player+companion sleep/dispel, per-map lifetime snapshot, source-alpha variants, navigation/current-map prewarm and12trip cache checks added. Local compile/domain8PASS; Android/native/artifact/phone acceptance pending. Do not promote this to original full parity solely from compilation. See design/COUNTESS_CASTLE_V273.md.

## V271 companion stealth acceptance evidence

Actor-specific targetHidden/Resolver begin-and-contact guard is host/native verified at b6e9961b, Actions40076725337 (61 current feature/adjacent tests including real monster retaliation). Shared full32gate run40076146616 on e7b18af0 is reused through exact three-line HUD-only final delta guard. Source monster art, movement/facing/attack cadence/population unchanged. User phone acceptance PENDING. Receipt verification/COMPANION_AGENTS_V271.json.

## V271 companion stealth targeting contract

New rogue companion stealth uses its own actor ID in the existing monster targetHidden gate. Only attacks targeting that companion are cancelled by its hide; attacks on player/other actors retain their prior state. No monster source art, movement/facing/attack cadence/population changes. Companion and actual monster-retaliation regressions plus current full gates required. Phone acceptance pending. Contract design/COMPANION_AGENTS_V271.md.

## V264 — 공격 그림의 프레임 선택·반전 교정 후보 (2026-10-24 KST)

V263 DEVICE_FAILED_USER_REPORTED: Screen_Recording_20261024_081540.mp4(29.390678s,2340x1080,libfile_1556c6c0b3748191bac77952c1d935a4). 전체1fps와10~14/19~23초6fps크롭확인. 설치SHA영상내UNKNOWN/최근eca4d745. 기존 방향값/소스회귀 검사는 실제 몸그림의 방향을 놓쳤다.

실제 SkillBodyRenderer의 west 출력이 원본 contact 프레임을 반전한 독립 픽셀 oracle과 다른 실패를 V263 코드에서 재현했다. source02의 프레임을 수평방향별로 고르고 west반전을 제외한 두 렌더 분기가 원인이다. 접촉자세를 back1/front3으로 선택하고 west에서 장비까지 합성 전체를 반전한다. source앞모습02/2 머리픽셀은 IDLE앞모습과100%,02/3과99.17%일치(비반전). 자세 단계 해석은 소스 기반 추론이며 원본 엔진 전체 애니메이션 타이밍의 확정 근거는 아니다. GameView/AI/방향상태/피해/타수/접촉시각/에셋 변경 없음. V263의 추가 방향보정 삭제 및 독립 HUD/장비 요구는 유지.

소스 `b8ec4fdacabe38776dd1589eacd264b1fdf8e411`, Actions40039445543/job120706791702/artifact12539270759 SUCCESS. **27 scoped native checks PASS**: 기존 실제 렌더 실패 선재현, 최종 west 원본접촉 픽셀 정확일치, 남녀/4방향/장비합성, 실제 수동/AUTO 접촉·피해·방향, 기본1/2/3·숫자·HUD/가방/목걸이·저장재시작·추격/성능. 기존V167의4방향source프레임가정만 교정. scope40039451310 SUCCESS/full compile의도적SKIP; full/main통합 별도.

APK PROJECT_DARK_V264_TEST.apk / com.projectdark.mobile.v264test / built2026-10-24 08:31:20 KST /106539710bytes. SHA256 `ea6075777ef1c3bdf77fba084b443c00ff8852cd8a1efa45bced0063a100306f`, ZIP `249e97c5fe82cdf0d7a8c254caf965a3b479f7cfb75696ba8d543da94828a7da`. CRC/hash/embedded source+run/CIv2서명/manifest/1518 mainasset bytes PASS. old/final west와실제GameView manual-SW캡처 검토. 설치파일 저장완료 libfile_8a54bb477bac81919b7080094cef9886. IMPLEMENTED / BUILD VERIFIED / HOST_RENDER_PIXEL VERIFIED; PHONE/USER_VISUAL_ACCEPTANCE PENDING. 원인 두 렌더 분기의 검증 후보이며 전체 잔존문제 해결로 표기하지 않는다. main41fe6def/PR197미병합. 문서HEAD를APK소스로재라벨금지.

[검증영수증](../verification/CONTACT_POSE_V264_BUILD.json), [계약](../design/CONTACT_POSE_V264.md), [Master](../master/changes/V264_CONTACT_POSE.json). 다음: exactV264에서 사용자 실기기의 밀집전투 외형 확인. 추가 방향정책을 임의로 도입하지 않는다.

## V263 — 회귀 요청 준수: 추가 방향 보정 제거 (2026-10-24 KST)

V262는 DEVICE_FAILED_USER_REPORTED. 사용자 영상 Screen_Recording_20261024_075444.mp4(26.378456s,libfile_c07b8f5f1a3c81919c53069a820f5e41,SHA256 7a86278ce9ecd77a2fd995d0934f46b19afd0e3ea585cb8a5168df12a6f0c823) 전체1fps및12~16초6fps/10·14초원본크롭확인. 설치SHA영상내UNKNOWN/최근배포65330bb0. V262의두합성재현과36hostPASS는이영상상황의재현/해결검증이아니다. ROOT CAUSE UNKNOWN,PHONE/USER_VISUAL_ACCEPTANCE PENDING. 회귀요청뒤두보정정책을재추가한것을철회한다.

게임런타임수정은2파일뿐: GameView의기본HIT_FEEDBACK방향덮어쓰기5줄삭제,MonsterAIController를위치피해도입전255a104c원본으로복원. 대체알고리즘없음. AI·CanonicalActorFacing·CombatController·근접계약·CharacterRenderer·SkillBodyRenderer·Pote표시방향·RuntimeCombatSession·CombatResolver등9전체파일과GameView전체전투eventconsumer/9방향·공격메서드,RuntimeState공격/이동및port방향잠금의기준일치확인. 독립적으로요청한기본1/2/3·팝업·HUD·장비/인벤·퀵목걸이교환은보존. V262두보정정책테스트는철회하고밀집선택유지검사는유지;설계/Master에도방향정책WITHDRAWN명시.

소스 `eca4d74515781d01e1758545f739567ef78211c3`, Actions40037138309/job120699289145/artifact12538041249 SUCCESS. 도입전소스일치guard + **34 scoped native checks PASS**(기본1/2/3,기존방향잠금·대기,밀집24개체선택유지,수동/AUTO,추격,저장재시작,기존HUD/장비/목걸이기능). 36→34는거절된V262정책검사2개제거때문이며실패를PASS로재분류하지않았다. scope40037144174 SUCCESS/full compile의도적SKIP. Full/main통합별도.

APK PROJECT_DARK_V263_ROLLBACK.apk / com.projectdark.mobile.v263test / built2026-10-24 08:05:54 KST /106539706bytes. SHA256 `1b3ebd66a99d41bac43cda5acd575de7bf2cbb61d846dadf69c7244ea717dfba`, ZIP `57b29075a8288db965d92abe7b971126106caa244ff903b0d94b92a37eb82b3c`. CRC/hash/embedded source+run/CIv2서명/manifest label/1518 mainasset bytes PASS. Native NW밀집장면sanity검토만했으며사용자기기재현성공또는문제해결로표기하지말것. IMPLEMENTED/BUILD VERIFIED/HOST_SCOPED VERIFIED; 회귀후보이고main41fe6def/PR197미병합. 후속문서HEAD를APK소스로재라벨금지. [영수증](../verification/FACING_ROLLBACK_V263_BUILD.json), [회귀계약](../design/FACING_ROLLBACK_V263.md), [Master](../master/changes/V263_FACING_ROLLBACK.json).

다음: exactV263에서사용자영상과동일한실기기상황확인. 확인전추가방향패치금지;기존전체성장/catalog차단해결주장없음. 통과한동일소스재빌드불필요.

## V262 — 한 칸 HUD·장비/가방 통합·접촉 방향 검증 후보 (2026-10-24 KST)

사용자 최종수정 반영: 퀵스킬6x2 오른쪽 **한 칸39px**, 공격 크기20% 확대(반경31→37.2)·960px안에서겹침회피. 장비 단독버튼 제거/아이콘간격정리. 가방을 열면 좌측 장비+우측 인벤을 함께 열고 양쪽닫기연동. 착용복사본은 가방 loose quantity에서제외/해제시복귀,중복여분유지. 소유권/schema/원본자산은변경하지않는다. 목걸이는 실제 누른퀵슬롯에기존목걸이를교환하고 센스자동선택도같은경로. 읽기전용/소유검사/거절/재시작보존.

V261 회귀실패 DEVICE_FAILED_USER_REPORTED: 몬스터가많으면옆보는모션으로앞을공격. 설치SHA UNKNOWN/최근배포57b8d85a. old adaed06에서 두 실제 경로를 먼저 FAIL로재현: 기본공격접촉전상대가SE→NW로이동했는데HP는맞고모션은SE, 원거리몬스터가인접전용facing검사때문에보행NW를SE공격에유지. 수정은 기본 HIT_FEEDBACK의 실제recipient방향과 몬스터의 accepted공격방향잠금뿐. 새선택/주변다른몬스터로회전하지않고 대기추적/위치별피해재도입없음. 물리기기동일성및사용자외형수용은PENDING.

IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_RUNTIME VERIFIED: 소스 `65330bb0fb2c0135e483a8fee47c0ded3360f53a`, Actions40032342978/job120683396314/artifact12534704137 SUCCESS, **36 scoped native checks PASS**, old두경로재현/수정후방향·밀집24개타깃·기본1/2/3·자동대상유지·추격·실제HUD터치·장비해제·목걸이중복바인딩정확슬롯교환·읽기전용/소모품·저장재시작. first8da219e/run40031956717의35PASS를최종소스로재라벨하거나재사용하지않고 최신소스로36건전부실행. scope40032349575 SUCCESS/full compile의도적SKIP. Full/main통합은별도이며과거Lv70/catalog전구간차단해결주장없음.

APK PROJECT_DARK_V262_TEST.apk / com.projectdark.mobile.v262test / built2026-10-24 07:20:11 KST /106539866bytes. SHA256 `53aef7091533cc7f3d546853a014f7d90842326e49886c5699c8aa03db870106`. ZIP SHA256 `594f140bf324d4ceb5f73c58c0ccf0f3db749431b5904070950eb93a636cfdef`. CRC·해시·embedded source/run·CIv2서명·1518 mainassets동일PASS. 960/2340실제렌더의좌우장비/가방·HUD원형비겹침·밀집NW공격화면검토. manifest184/1.84유지/V262testpackage+label. PR197/main41fe6def미병합후보; 문서HEAD를APK소스로재라벨금지. [검증영수증](../verification/EQUIPMENT_HUD_V262_BUILD.json), [설계](../design/EQUIPMENT_HUD_V262.md), [Master변경](../master/changes/V262_EQUIPMENT_HUD.json).

다음: exactV262기기에서밀집사냥방향·장비인벤이동·목걸이교환확인. 휴대폰성공/원작외형수용을host성공으로대체하지않는다. 동일한통과소스재빌드불필요.

## V261 — 위치별 피해 회귀·총1/2/3타·영상 숫자 궤적 (2026-10-24 KST)

사용자 V260 기기 실패 보고: 전투 방향이 깨지고 트리플 기본공격이4타, 숫자의 발생·이동·소멸이 원작과 다름. 설치SHA UNKNOWN, 최근후보df3c0e56. 사용자 명시로 정면/측면/후면 피해 기능을 폐기한다. BasicPositionDamageV259/MonsterCombatFacingV260 파일과 실제 호출 제거, MonsterAIController는 도입 전V258/255a104c와 바이트 동일. 부가 대기추적/원거리방향보정 모두 회귀했고 센스·목걸이는 유지한다.

기본 총1, 더블 총2, 트리플 총3, 겹쳐 배워도3. LINKED 쿼드4타를3으로 제한하고 설치형트랩의 불필요한+1 경로 제거. 기본1타에트리플3타를더하지 않는다. 기존 접촉 시점에 실제HP/장부1·2·3회를 처리하여 합산 감소, 독립 숫자2/3개를같은원점에서발생시킨다. 공격모션/주기 변경없음,처치후추가타/중복보상금지.

영상 Screen_Recording_20261021_205939_YouTube.mp4/5.750~6.350초1433·1444·1421 템플릿 추적. 기존26px상승/새숫자가이전숫자를12px밀어올리기 폐기, 오른쪽포물선/숫자별상하속도/층간확산/끝페이드. 초기숨겨진원점·월드크기·15fps13스텝은ADAPTED,영상탐색/반복프레임으로원작상수동일성미확정. 실제GameView combo09와원본폰트팬그리드검토, 실기기/사용자외형수용PENDING.

IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_RUNTIME VERIFIED: 소스 `57b8d85ac62cdfa9dcedb2d493ec8e8c864214da`, Actions40028630344/job120671021334/artifact12534246645 SUCCESS. first601e0ba/run40027979318의49PASS를정확runtime/assets/Master/design및해시/XMLguard로유지하고양손무기fixture3건교정후3PASS추가=**52 scoped checks PASS**. 교정은단일접촉전사거리취소,양손접촉3타동시/지연0타·주기유지,기존canonicalItemId소유조회이며게임착용조건우회없음. 정상/미리보기·추가연결·수동/AUTO·장부HP·4방향피해동일·AI원본·추격·센스목걸이·저장재시작검증. Full/main통합별도,기존전구간성장/catalog재생성차단해결주장없음.

APK PROJECT_DARK_V261_TEST.apk / com.projectdark.mobile.v261test / built2026-10-24 06:45:43 KST / 106538406bytes. SHA256 `3f9a1daa340a7dd19acd2bfdf14f4dfa22103cd451fdb80e906c45848fdcba07`. ZIP `2954a1db63ece86cb035cc3ff5f619a4d39d1a22d66df7477448f115fd8f2f3d`. APK/ZIP CRC·해시·embedded source/run·CIv2서명·1518 main asset bytes PASS. 기존manifest184/1.84유지, V261은testpackage/label. PR197/main41fe6def미병합후보. [검증 영수증](../verification/BASIC_ROLLBACK_V261_BUILD.json), [사용자 회귀 계약](../design/COMBAT_ROLLBACK_V261.md), [Master 개정](../master/changes/V261_BASIC_ROLLBACK.json). 이후문서커밋으로APK소스를재라벨하지말것.

다음: 정확V261기기에서기본1/더블2/트리플3·기존전투방향·새숫자팬확인. 사용자요청없이위치별피해/자동방향보정재도입금지. 이미통과한동일런타임재빌드불필요.

## V260 — 방향·타격별 숫자 후보 검증 완료 (2026-10-24 KST)

소스 `df3c0e5675e8b6617d2a83ac1e35813fc1b67a15`, Actions40023813177/job120654810808/artifact12531379208 SUCCESS. 기존 AI ce02feb8에서 쿨다운 대기 중 정면100 기대가 후면200으로 나오는 결함을 먼저 재현했다. 복원한 실제 런타임은 대기 중 기존 상대를 바라보며 이동·공격 방향 잠금·수면/마비·은신·어그로 해제를 보존한다. 위치 피해 계산은 방향을 바꾸지 않는다. 기본공격 정면100/측면150/후면200 규칙은 다른 공격자 위치를 위해 유지한다.

더블2타/트리플3타, 동시 습득 시 트리플만 적용. 실제 양수 타격마다 독립 숫자를 만들고 저프레임·처치 타격도 보존한다. 기존 원본 font-en glyph 사용, 관측 영상의 상승·중첩·페이드는 ADAPTED(.80초/26px/12px), 원작 상수의 완전 동일성 주장이 아니다. 실제 GameView combo03/06/09/12와 lethal/digits 화면 검토 완료; 물리 휴대폰/사용자 외형 수용 PENDING. 최종 combo09에서 실제5 숫자3개를 확인했다.

최종 원본 숫자 atlas의 !기준 셀 오프셋과 스트림 수명을 교정했고, 상대 방향 추적을 근접 도달 범위로 한정해 추격 경로 방향을 유지한다. 기존 unchanged38건을 엄격한 delta/hash/XML 검증으로 재사용하고, 연속공격·추격·폰트픽셀·실제화면·인접AI·원본/생성형보행17건을 재실행해 **55 scoped host checks PASS**. 옛 AI facing 결함 및 기존 검사2개의 실패를 complete old source ce02feb8에서 각각 재현했다. 옛 V153 5직업 Lv70 유료장비 시나리오는 V210 canonical alias의 착용 토글/미확정 원본 착용조건과 충돌하여 여전히 OPEN이다. 게임 착용조건을 우회하거나 전체 성장 검증 성공으로 처리하지 않았다. 공통40023457499는 Master/V12420검사 PASS 후 기존 skill catalog 재생성 dirty diff에서 중단; 전체회귀/main통합 미완료. 최신 scope40023819383 SUCCESS이며 full compile은 의도적으로 SKIP됐다.

APK PROJECT_DARK_V260_TEST.apk / com.projectdark.mobile.v260test, built2026-10-24 06:08:37 KST,106538494bytes,SHA256 `8c2fc38b596f9cb363aa27ea4789f03c3ca286cd2d8fbb7d9a154aeb8d710d84`. ZIP SHA256 `8df2cb964ccdf5394b14d85a503608572c4a26a70a5073abdfc6daf807dfc858`. CRC/hash/embedded source+run/CI apksigner v2 PASS;1518 tracked main assets byte exact. 기존 manifest versionCode184/versionName1.84는 유지, V260은 test package/label 식별자다. IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_RUNTIME VERIFIED; PHONE/USER_ACCEPTANCE PENDING. 기존 branch/PR197/main41fe6def 미병합 후보. 정확 기록 [검증 영수증](../verification/FACING_NUMBERS_V260_BUILD.json). 이후 문서 커밋을 APK 소스로 재라벨하지 말 것.

다음: 정확 V260을 설치해 서로 마주보기·수동/AUTO 더블2/트리플3·처치 숫자 확인. 이미 통과한 동일 런타임을 재빌드하지 않는다. 별도 full통합 시 기존 Lv70장비 fixture와 skill catalog 재생성 차단을 해결한다.

## V259 — 기본 공격 위치 배율·센스 목걸이 검증 후보 (2026-10-24 KST)

사용자 확정: [설계](../design/COMBAT_NECKLACE_V259.md), [변경 계약](../master/changes/V259_SENSE_NECKLACE.json), [목걸이 원본 ID·출처](../master/canonical/Necklace_Elements_V259.json). 플레이어 기본 공격만 정면100%/측면150%/후면200%, 접촉 시 대상 표시 방향 기준으로 각 타격에1회 적용. 전사·도적·무도가 실제 더블2타/트리플3타 유지, 배수 중복 없음. 기술/마법/몬스터가 주는 공격에는 적용하지 않음.

센스몬스터 성공 알림→대상의 실제 방어 속성(속성변환 포함)→퀵아이템4칸의 소유·착용가능·속성확인 목걸이를 V257/V258 표로 비교→기존 durable equip 경로. 현재 장비보다 유리할 때만 교체, 동점 유지; 수방어 토→풍→화→수, 별도 예시 하드코딩 없음. 빈칸/미소유/미확정/착용불가/죽은대상/읽기전용/센스실패는 교체 없음. 수동·AUTO 같은 경로.

신규 시작 수크리스탈 착용, 화·토·풍·빈칸 등록; 기존 저장 장비/슬롯 선택 보존. 네 시작 크리스탈만 사용자 요청에 따라 시작 레벨 착용 허용 예외, 원본 Lv11/스탯/제한 근거 보존. source139 explicit ID mappings와 alias 속성 보존으로 실제 신규 GameView 인벤토리 목걸이44종 확인, 쥬얼십자가목걸이FX1종은 원문에 공격속성이 없어 UNKNOWN/자동교체 제외. 숫자열/이름으로 미확정 속성 추정하지 않음. V258 일반 공방 독립 자연4속 리젠/생존중유지·광산60DARK예외·원본92CSV·schema4 유지.

APK 정확 소스 `aab7b4dab1d9644fa0676beb8ad63e33a4c1caa4`, Actions39983972749/job120518223036/artifact12514976446 SUCCESS. 최종 실제 인벤토리 검사1PASS + 엄격한 runtime/assets/master/design와 test-only guard로 first8ef0a774/run39981751669의52PASS 및 ee5e4b48/run39983186247의3PASS 유지 = **56 scoped host checks PASS**.4방향/위치·3직업 더블/트리플·수동/AUTO·센스 성공/실패/사거리·임시방어·선택 우선순위/빈칸/미소유/미확정/읽기전용·시작키트/재시작·기존 자동순서/장비/마법/저장 검증. 최초 인벤토리 검사는 bare RuntimeState를 실제 새게임 인벤토리로 착각한 fixture였으며 actual GameView 검사로 교정; 임의 최소개수 가정 제거, 각 목걸이 속성/미확정1종의 완전성 검사는 유지. 기존 HudItems fixture는 신규 시작에서만 슬롯을 비우고 restart 상태는 유지하도록 교정. Pote 화염/FIRE 비교는 normalize 의미 비교로 수정. 게임 런타임 변경 추가 없음.

APK PROJECT_DARK_V259_TEST.apk / com.projectdark.mobile.v259test / 2026-10-24T01:09:16.949898+09:00, 106536378bytes, SHA256 `03993cfecf4ec2d2b63345a84dceb5923fb1b5dd61f39bfa91e2f3f32b762b3e`. ZIP SHA256 `b189b34952a55b58b820ed8b8d4b28fb46d226a7d09115857786a6e3ef6b61e5`. ZIP/APK CRC·embedded source/run·APK hash·CI apksignerV2 서명 PASS. IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_RUNTIME VERIFIED; PHONE/USER_ACCEPTANCE PENDING. 상세 [검증 기록](../verification/SENSE_NECKLACE_V259_BUILD.json). 후속 문서 HEAD를 APK 소스로 재라벨 금지.

공통 전체회귀39981760803: Master 보존검사 PASS, V12420개 중19PASS·Pote 속성 표기 문자열1FAIL. 해당1개는 ee5e4b48의 semantic assertion 교정으로 PASS; 그 이후 full 단계는 실행되지 않았으며 전체회귀 완료 주장이 아님. 후속 scope39983982161 SUCCESS/full compile 의도적SKIP(정확히 검토된 blob). 과거 skill catalog 생성 차단 해결 주장 없음. main41fe6def/PR197 OPEN·UNMERGED 후보 유지. 다음: exact V259 실기기 사냥/센스/목걸이 피드백 또는 main 통합의 남은 전체회귀. 검증 완료한 불변 소스 재빌드/재검사 불필요.

## V258 — 속성표 실제 대미지·모든 몬스터 공방 속성 적용 (2026-10-24 KST)

[설계](../design/ELEMENT_SYSTEM_V258.md), [대미지 투영](../master/canonical/Element_Runtime_V258.json), [몬스터 정책](../master/canonical/Monster_Element_Policy_V258.json). V257원시표81조합/18300을 기본 유리상성 기준으로 정규화(U표+ADAPTED기존raw연결),한번반올림/양수최소1/포화. 기존무속1.0·생암1.75·목금중립 임시값 폐기. 원시13000 우선으로55%불일치 처리,원본사진/243셀전사보존. 고정피해/회복/상태/명시속성마법 계약유지. 수동/AUTO 같은CombatStatPipeline.

모든개체 생성/성공리젠에 자연4속 공격·방어 독립균등추첨;살아있는동안유지. 광산MINE_04드라코+05..09헬5종 공방DARK(100중60);나머지/다른현존맵 자연4속,특별LIFE임의추가없음. 드라코이전임시LIFE수정. Source-backed드라코두채널,헬DARK관측+두채널U정책분리. 개체필드→공유전투/센스,프로필/이름의가짜센스표시제거. 명시몬스터불/물/뿌리마법행동속성유지. schema4선택키monster_elements_v258:동일맵/ID/유효자연쌍복원,구버전/오류안전기본값. 새난수는생명주기만;cache/collision/population/art/vsync/loot원자성불변.

APK정확소스 `255a104c69cb09bfc4c760d9c0c98210502bae05`;Actions39977747900/job120496693189/artifact12512317256 SUCCESS. 최종AUTO1+strict runtime/assets guard로7ebca27c/run39976958819의38PASS유지=39; b2cac9f1/run39978518320의테스트전용무속장비교정1PASS추가=40scoped checks.37huntmaps/1522actors/60mineDARK,81sourcecells,16independentpairs,리젠/막힘,실제necklace/belt/manualAUTO/magic/sense/save/oldsave및기존자동순서·방향·시전·장비·저장 검증. actualSensePNG검토. APK106532826bytes,2026-10-24 00:26:43KST,SHA256 `d28930e9b74b5385d66cb9865b57deb66f8069e807a847bc0c1571309c6b1210`;ZIP/APKCRC+hash+embeddedsource/run+CIv2signaturePASS. IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_RUNTIME VERIFIED; PHONE/USER_ACCEPTANCE PENDING. 테스트전용b2c나이후문서HEAD를APK소스로재라벨금지.

Full39978531235:원본Sources.csv정확복원후Master보존검사+V12420체크PASS;기존approved설명vs스킬catalog생성dirtydiff에서중단. 전체회귀/main통합미완료. V257에추가했던Sources원본행은원본blob으로복원하고 `master/canonical/Sources_Elements_V257.csv`와별도provenance에보존;새근거유실없음. main41fe6def/PR197미병합후보. 기존V256HUD/AUTO·NarcoliUSER_ACCEPTED유지. 상세verification/ELEMENTS_V258_BUILD.json. 다음은정확APK의실기기속성사냥확인 또는catalog재생성통합차단해결;재검증/재빌드불필요.

## V239 전달 완료 — 원본 포테11종·숲 바닥·마법5종 검증 후보

Runtime `511688e412fc05b1925b307d4770f1a966f67d6f`, focused Actions39857975662/job120101026413 SUCCESS, artifact12461495013. 최종3native PASS + 엄격한변경가드로 first f75761af/run39854786767의 unchanged31PASS 재사용 = 고유34PASS/0미해결실패. CI selector6PASS, common scope39857984377 SUCCESS/full compile 의도적SKIP. 컴파일/assemble/관련호스트native검증이며 전체회귀/main/휴대폰수용은 아님.

11MPF원본/132idle-walk-attack pose samples, capturedsoil, 아이스블러스트·플레쉬스톰·플레어·델리스펠라스·포트리스 연속접촉영상을 적용. 촬영무기별잔상/옆대상조각 제거, 방어막foot260등록 및 native몸통~지면envelope검사. 원본별이름/HP를 paddedcanvas와분리. 12맵왕복 현재4state monster<=35,389,440bytes/각<=60textures/floor<=32chunk. 새5FX blankmargin만trim:49,299,200→10,631,328bytes, 모든frame 원등록에restore한RGBA동일성PASS. 전체게임메모리/FPS78%개선 주장이 아님. 새shader캐시/livecollision/AI/개체수/보상/save/마레노/기존V238/승인정령·맨티스 유지.

APK PROJECT_DARK_V239_TEST.apk / com.projectdark.mobile.v239test, **2026-10-23 06:35:05 KST** 빌드,104560713bytes,SHA256 `b2cfb71373b2dceab82e50df9b58bdb385589999f88fa43f0491e4aac7f9f231`. ZIP SHA2569e9b15b7eaa8f8c1ced9fd14f81b05130a6849e7d7ede170a329fdd2c24713f2. Embedded source/run, binarymanifest package/label/version,830packaged file bytes, ZIPCRC, 독립APK V2 RSA서명+102chunk contentdigest PASS. APK·실제A화면·132pose비교그리드 저장완료(metadata applied). 상세 `verification/FOREST_MAGE_V239_BUILD.json`.

IMPLEMENTED / BUILD_VERIFIED / SCOPED_HOST_NATIVE_VERIFIED. PHONE/USER_VISUAL_ACCEPTANCE PENDING. 남은것: 미적용일반마법/디버프연속영상, 원본전체monster다중frame재생, Koreanname-index/3독립참조, 가려진FX픽셀. topology는기존adapted이며원작전체맵복구주장없음. 최초common39854794744의 source-guide catalog재생성 불일치는 최종main통합에남음; 새scope수정이그문제를수정한것으로주장하지않음. PR197 OPEN/UNMERGED, main41fe6def 그대로. 이후문서HEAD를APK소스로재라벨금지. 다음: exactV239 휴대폰 포테·마법 외형/동작 피드백, 또는 남은 명시일감부터 재개; 불변소스 재생성/재빌드 금지.

## V239 — 추가 마법 영상 / 포테 원본 외형 개선 후보 (2026-10-23)

Director 단일 편집. 기존 branch `codex/rucesion-coast-v157` / PR197 유지, main `41fe6def5120b6c33b58f609e52fbaf2ead700ee`. 마지막 성공 V238 runtime `dc6f1426b61a1584bd819ceed312e82b42002514`, run39806023052; 문서HEAD ba495f2에서 재개. V238 재빌드/검사 반복 없음.

USER: 추가 영상 마법 반영, 1번000206의 포테 맵/몬스터 개선. IMPLEMENTED 후보: 원본741installer hash 검증/REWise 추출, 선택11MPF+palette 보존 및132 original idle/walk/attack pose samples. 기존 generated source/정령/맨티스 보존. 원본별2views+client mirror, native anchor48,76×2와logical96. 구generated 방향예외/mesh 변형은 원본에 적용하지 않음. Korean name table 미확보: 영상에 보이는 팜팻색/앤트자이언트/울프라이더 직접 대조, 나머지 indexed joins 시각후보. 3독립라벨참조 PENDING_REFERENCE. 전체 원작 다중프레임 재생으로 주장하지 않음.

포테 흙3조각을35초 격자없는 바닥에서 quilt; 전체스크린샷/캐릭터/UI 제외. 기존 지형/topology/props/충돌/72·96·108개체수/통계/보상/save/32floorLRU/prewarm/vsync 유지. source terrain receipt 보존. 승급 아이스블러스트/플레쉬스톰/플레어/델리스펠라스/포트리스 5contact 연속영상 matting. 가려진 영역은 비우며 숨겨진 원작픽셀 복원 주장 없음. 피해숫자·대상 겹침이 큰 퀘이크 새 추출은 자체검수 거절, 기존 V237 유지. 다른 디버프/하위써클 영상은 evidence inventory이며 이5개 외 runtime 적용 완료로 주장하지 않음. 마레노/V238각인·4circle/whole4·학습비용/효과수치/무도 불변.

검증범위: source/hash132poses/5FX; new3native(5sequence+14normalpromotion,132pose presentation,12actualforest trips/population/captures), V2385+contact3, PoteMonsterPlayable, ForestFloorCache2(currentmaterial만갱신/algorithm불변), ForestPixelCache, GroundTap, chase performance, restart. 비주얼 source/render/공유cache 영향에 집중. main최종통합full별도. BUILD/NATIVE/PHONE/USER_VISUAL PENDING. 다음: exact commit CI 확인→실제GameView·effect/monstergrid 원본대조→관측실패수정→APK identity/run/hash/signature확인. 재개 시 이행동부터 진행.

## 2026-10-22 V225 인간형 착용 장비 드롭 — 구현, 검증 대기

U: 인간형의 실제 착용 무기/갑옷/모자를 드롭, 그 외 보상은 원작 근거. 단일 `WornEquipmentDropsV225` 14유형/23관계에서 기존 ID를 사용. 광산 캐스터 남녀 로브를 분리하고 무관한 성직자 로브 동시 드롭 제거; 그림록퀸 더브릴·헬씨프/나이트 투구·법사 후드 및 식별된 호러 장비 연결. 호러4속성은 외형별 공유, 직업/성별 임의 통합 금지. 원작2026호러드롭제거보다 사용자 착용장비 규칙을 이 관계에 한해 우선한다.

확률은 기존 B 임시 무기2%/갑옷3.5% 유지, 모자3.5%는 구현 임시 선택; 원작 또는 사용자 지정 수치 아님. 각 품목 독립1개 추첨→기존 인벤 직접 지급, 처치 중복방지/저장 불변. 미식별 인간형 장비와 V224 기타 원작 퀘스트/잡템 확률은 PENDING; 신규 임의 보상 없음. 상세 `master/changes/V225_WORN_EQUIPMENT.json`. 6개 관련 suite와 assembleDebug 예정; 전체회귀/폰/시각 수용 주장 없음. main41fe6def/기존 PR197 유지.

## 2026-10-22 V222 검증 완료 — 원본 입구·지하문·웨어렛 HP·밀집 충돌 비용 수정 후보

사용자 실기기 결함 신고 DEVICE_FAILED_USER_REPORTED/설치 SHA UNKNOWN 이력 유지. 새 후보 source `ae0c7ff69c130479c013ad831a6ff696efcb50ea`, focused run39698748694/job119562479699 SUCCESS; 관련13 suites **47 PASS/0 failure/0 error/0 skipped**, scope selector5 PASS. common39698755769 SUCCESS(범위검사; full compile 의도적skip). main41fe6def/기존PR197미병합; 최종main전체회귀는별도. IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_NATIVE VERIFIED; PHONE·USER_VISUAL ACCEPTANCE PENDING.

웨어렛HP180000(U/ADAPTED 사용자요청),니크르164926/독거미192707·3종EXP750000유지. 로비→야외원본spawn12,85복구;원본지하문6633/6639총10개에문발시각핀+합법인접문턱1개씩join,양방향arrival수정. 27gate합법/원본12,85실제entry·지도선택도착·충돌navigation/13문·계단native캡처검증. B14,19분기와C/E25,10귀환은명시적adapted계단. 원본그리드·충돌·825PNG·해상도불변;새원작사실로간주하지않음.

밀집 충돌: ID고정반경1회계산+비정규식species+보수적live AABB선별. 500독립point/segment/reservation oracle PASS. 72/36/100마리·AI빈도·충돌·이동·저장유지. 같은6000query의CPU 야외382.90→14.92ms,지하36.53→2.97ms,광산94.06→3.50ms;임시할당291703656→507744/114390680→272048/300384048→266928bytes. 이는호스트hotspot측정(약92–96%CPU/99%이상할당감소)이며FPS주장아님. 최종각90실제AI/update/drawframes+맵/개체수검사PASS. 전체프레임cold outlier는남음(광산update891.79ms,야외draw262.62ms);폰렉완전해결미확인. 직전31cfe95b/run39697696611도동일production47PASS/전체before+after자료보존;실행이8분이상으로보여비교범위를좁혔으나구실행은이미성공종료하여취소되지않았음. 추가동일소스재검증금지.

APK `PROJECT DARK V222 TEST` / `com.projectdark.mobile.v222test`(독립설치후보),legacy184/1.84-mobile-horror. 빌드 **2026-10-22T09:24:50.082134+09:00**,101630340bytes,SHA256 `f54951327dd08f24b67d8961cdeeb1d69231b41ad086fc76918051d4cc28eef0`. artifact12392152261/ZIP26d2c4faf2adbd304cc84bec0b21a59bc203f109c9d6ac749a9ee3ce1db33882. ZIP CRC/embeddedsource+run/manifest/독립APK V2 RSA+cert+contentdigest PASS. 13문캡처직접검토및최종픽셀동일확인. 상세 `verification/DEATH_REPAIR_V222_BUILD.json`; 이후문서HEAD를APK소스로재라벨하지않음. 다음은정확V222휴대폰입구/문이동/밀집사냥피드백.

## 2026-10-22 V222 사용자 실기기 결함 신고 — 입구/지하문/밀집 렉 수정, 검증 대기

DEVICE_FAILED_USER_REPORTED: 야외 시작점이상·지하문과분리된포탈·몬스터밀집렉. 사용자설치APK정확SHA는UNKNOWN;직전전달V221호스트27PASS를휴대폰수용으로간주하지않음. main41fe6def/기존PR197/codex/rucesion-coast-v157,마지막성공소스3bf65b96/run39693321115에서재개,문서HEADc91f1359. 이전V219의임의FIELD28,10을원본source spawn12,85로복구. 웨어렛HP180000은사용자조정U/ADAPTED,원작확정아님;3종EXP750000유지.

원본지하문stc6633/6639총10개모두문발위치시각핀과합법인접문턱을join. 기존11,3/3,4/46,12등임의gate삭제,원본그리드/문아트/충돌불변. B는원본문2개에3연결필요하므로추가D분기는14,19명시적계단, C/E건물귀환계단은25,10의빈walkable바닥. 모든반대방향arrival동시수정,house1-A-B-C/D-E-house2 및house3-C/던전제외유지. 10원본문좌표·27gate실제왕복·native문턱캡처검증예정.

밀집비용검토: actor충돌의매검사마다species정규식split과동일반경계산반복. ID고정반경을monster생성시1회계산,공유species는indexOf/substring,상세endpoint/예약/교차segment검사전conservative live AABB로먼개체제외. 움직임/예약정보프레임간캐시없음.72/36/100마리·해상도·AIcadence·충돌·보상/save유지. 이전소스2클래스만임시적용한before와final after를같은fixture로호스트paired측정하고최종production복구git diff guard 후APK생성.500독립collision oracle와기존공간/chase/이동회귀포함;무관한성장/스킬전체회귀를자동추가하지않음. 새공유이동규칙/save schema변경없음.

IMPLEMENTED; BUILD/HOST_NATIVE/PHONE/USER_VISUAL PENDING. contract master/changes/V222_ENTRY_DOORS_DENSITY.json. 다음: exactSHA baseline/after비교와관련Android검증,실제entry/10doors native리뷰,APK서명·소스·해시확인후전달. 휴대폰렉완전해결은아직주장하지않음.

## 2026-10-22 V221 검증 완료 — 지도 포탈 선택·배치·HP·750000EXP, 휴대폰 대기

Runtime `3bf65b964c85696420c7a56b38825231f5ec795a`, focused Actions39693321115/job119544557697 SUCCESS. 7 suites **27 PASS/0 failure/error/skipped**, scope selector5 PASS. 실제7town/field views의 지도핀선택→기존길찾기/문전환, lobby→field→house1 fullupdate와live72몬스터, 다른목적지선택/명시적귀환의분리된arrival latch,10씬27gate·12왕복·48poses,3종각750000EXP 1회지급/재시작,HUD/ground/4slot/restart검사. 초기be4bf25c/run39692798621는27중1실패(house1선택시lobby로복귀), 선택포탈별latch수정으로같은검사27개통과. 기대값완화/몬스터삭제없음.

APK **PROJECT DARK V221 TEST**, `com.projectdark.mobile.v221test` 별도후보. source3bf65b96/run39693321115; **2026-10-22 08:30:07 KST**,101629572bytes,SHA256 `17652ec90bcbb6492c1c1f91f28262de25ac7c214a024cf50bec15a244f0b2cc`. Artifact12389098422 ZIP SHA256 `6280e941779b3ccd446c13354b7fa9049529c1eb851e7cc0cc291164dc35fa11`. ZIP/APK CRC,embedded source/run,binarymanifest package/label,독립V2 RSA서명+99chunk contentdigest+certificate확인.825deathPNG포함,sourcepack/generator/grid는V220과byte동일;기존원본bytecompare근거재사용. 실제house1도착/선택가능로비지도native캡처직접검토. APK저장완료 libfile_4b63368b0f648191b0d12e1cd59f025d.

최종배치 지상/집안 니크르+독거미,지하A–E 니크르+웨어렛+독거미,72/15/36유지. 니크르HP164926/독거미192707은영상최고관측HP기준calibration,원작미피격최대확정아님. 센스EXP237000/실제로그733772와사용자설정750000을구분. 웨어렛HP220000/MP240및기타미확인전투값은기존B/ADAPTED;영상센스에MP항목없음. 기존recall의웨어렛172875/니크르141008은현재영상에서미검증이므로반영안함. 상세계약 master/changes/V221_DEATH_NAVIGATION.json.

Common39693329803/scope119544587306 SUCCESS,full compile의도적SKIPPED. 본27검사는관련호스트native검증이며최종main전체회귀/실기FPS/사용자시각수용주장아님. 12왕복host최대35.841756ms,atlas8,847,360bytes/32floorLRU bound PASS. IMPLEMENTED/BUILD VERIFIED/SCOPED HOST_NATIVE VERIFIED. **PHONE/USER_VISUAL PENDING**. PR197미병합/main41fe6def유지;별도앱/저장,자동이전없음. receipt verification/DEATH_NAVIGATION_V221_BUILD.json. 이후문서HEAD를APK소스로재라벨하지않으며무관한재빌드금지. 다음은exactV221휴대폰지도포탈선택/이동/사냥피드백.

## 2026-10-22 V221 재개 — 몬스터 배치/영상 HP/750000EXP/지도 포탈 선택 구현, 검증 대기

기존 PR197/codex/rucesion-coast-v157, main41fe6def 유지. 마지막 성공 runtime45936b9b/V220 run39685771102; 문서HEAD884a3599에서 재개. 이전 프롬프트 이후 미커밋 작업은 현재 checkout에 없으므로 저장된 V220부터 수정. 이번 세션 첨부 경로는 실제 비어 있었음; 이전 실행 중단 전체원인은 UNKNOWN. 승인된 GitHub 수정 계속.

V221: 지상/집안 니크르+독거미, 지하A–E 니크르+웨어렛+독거미;72/15/36개체수·원본MPF/그리드 유지. 영상205939를 직접 복구/decode하여 센스 독거미192707/164044, 니크르161239/164926·Lv99·EXP237000·개체별속성 확인. 실제처치로그733772. 최고관측 HP 니크르164926/독거미192707을 runtime calibration으로 적용, 미피격최대HP확정아님. 웨어렛HP220000 및 MP240 임시값 유지/명시; 두영상에서웨어렛센스/마력 미확보. 사용자요청으로활성3종EXP750000,원본센스EXP와구분. 좀비는이번지역배치제외,기존안정ID/원본에셋보존. contract master/changes/V221_DEATH_NAVIGATION.json.

전체지도입구⊕터치→지도닫기→기존requestGroundWorld→live충돌/일반문·계단전환. grouped4cell portal은표시는중앙/목표는실제한칸,사용자의명시선택만arrival latch해제;자동bounce보호유지. 일반공백터치는읽기전용,지도bitmap2MiB·현재맵캐시/카메라복원유지. 거리가먼목적지UI/순간이동/야외건물이동버튼추가없음. 나머지마을실제서비스문핀동일입력지원.

검증범위:신규실제6town선택/문도착·lobby→field→house1 fullupdate/live몬스터·10씬27gate합법목표/blank/no-rebake·3종750000EXP중복방지/restart; 기존V220/V217/V219quickslot/GroundTap/HUD/RestartMatrix. shared저장schema/전역보상알고리즘변경없음. IMPLEMENTED; BUILD/HOST_NATIVE/PHONE/USER_VISUAL PENDING. 다음: exactSHA focused Android CI와APK증거확인. 별도후보 com.projectdark.mobile.v221test.

## 2026-10-22 V220 검증 완료 — 지도/포탈/몬스터 교정 후보, 휴대폰 수용 대기

V219 USER_VISUAL_REJECTED 기록을 유지한다. V220 runtime `45936b9b65be6614e27d08a237e0450e3e718233`, focused run39685771102/job119519304224 SUCCESS. 8 suites **27 PASS/0 failure/0 skipped**, scope selector5 PASS. 실제6town overview render/camera restore/readonly/cache, 4칸 ground portal 진입/group latch,10씬/27gates/12trip/48monsterposes,4quickslot restart,HUD/ground/movement/Milles검사. 공통39685778854 scope검사SUCCESS, full compile의도적SKIPPED;최종main 전체회귀 또는휴대폰FPS를주장하지않음.

APK **PROJECT DARK V220 TEST**, `com.projectdark.mobile.v220test`, legacy184/1.84-mobile-horror; 빌드시각 **2026-10-22T07:25:44.664592+09:00**,101628448bytes,SHA256 `2fb460a5caf2cf8e31e2c1189043700e38fe3164ced53859e8b42cc5a613161f`. ZIP CRC,embedded source/run,binarymanifest,APK V2 RSA 서명/contentdigest/certificate,825원본PNG bytecompare PASS. Artifact12387036337 ZIP SHA256cb8522a34cf2d5fb8fb13c126afa7b312eb2f78d7b5d9acc8ad68765ea3e19dc. Receipt `verification/DEATH_MAP_MONSTERS_V220_BUILD.json`.

신규네이티브캡처6지도+3몬스터 직접검토. 영상205405 2s실제전체지도/23s초록옷좀비/29s니크르·초록거미/43s단검든쥐인간과원본166/178/176/175를대조. 이전회색석상/후드기사/보라슬라임가명교체. 원본10map데이터정의는V219와동일. 이동버튼삭제,로비8..11,0접촉포탈,모든canonical마을미니맵전체지도/현재위치/입구pins,지도터치는읽기전용. 영상외영역은근처원본map사용한adapted영역,던전미구현. 한국어웨어렛/독거미이름은형태·목록join이며클라이언트이름표확정아님. 기존수치/보상프로필유지,원작통계확정주장금지.

IMPLEMENTED/BUILD VERIFIED/SCOPED HOST_NATIVE VERIFIED. PHONE/USER_VISUAL ACCEPTANCE **PENDING**. 기존PR197미병합/main41fe6def유지. 앱설치와실기속도·사용자시각수용은별도단계. 아래PENDING기록은빌드전이력이며이머리가현재상태다.

## 2026-10-22 V220 사용자 V219 거부 — 포탈/전체지도/몬스터 수정, 검증 대기

DEVICE_FAILED_USER_REPORTED/USER_VISUAL_REJECTED: 대기실·건물 이동 버튼 금지; 대기실8,0~11,0 포탈; 모든 마을 미니맵 터치 후 영상처럼 전체지도/현재위치/포탈을 보고 직접 이동; 회색좀비/집안의망자/슬라임 등 원본과 불일치한 외형·가명 거부. Director 단일 편집,기존PR197/main41fe6def 유지. 앞선V219호스트21PASS는 사용자 시각 수용이 아니었으며 전체완성 근거로 재사용 금지.

이동 버튼 및DeathVillageGuide삭제. 로비4gate8..11,0,귀환9,0;4칸을 묶은 arrival latch. 기존그리드4칸모두원본walkable. 일반ground/joystick접촉만 진입. 전체지도는실제맵원본렌더러로1024x512(2MiB) 한장,current map만 cache;static배경+건물/현재위치/실제입구pins. 미니맵/닫기만입력,지도터치자동이동없음. Milles/Abel/Rucesion/Mine/Deathlobby+field/Deathinteriors지원. 임시 overviewcamera는finally에서 exact기존camera객체복원;플레이어좌표/이동/충돌geometry미변경. Millesclip을Canvas실제clipbounds로 판정하여축소지도에전체건물이표시됨. 원본32chunkLRU를확대/소모하지않으며이동frame에지도재생성없음.

205405(2)영상을2초전체지도/23초야외/29초집안/43초지하15로 다시 decode하고 공식741설치본(208513633bytes/SHA256dbbd937d4dd1eed310b7b1de4c5c3d98e829ed77e5493b0857063528a47e1093)의953MPF 실제 원본 index를 재검토. 137회색석상/171후드기사/105보라슬라임 잘못된선택을166초록옷좀비/178붉은몸초록모자니크르/176초록거미로 교체,175단검든쥐인간은지하에만배치. 집안니크르+거미,지하웨어렛.좀비/니크르 이름은기존Master·Nexon4509/4515,웨어렛/독거미는Nexon호스팅목록721717와형태대조에근거한join(클라이언트한국어이름테이블 미확보,확정원작통계 주장금지). 새raw원본pack master/source/death_v220에보존,기존source는삭제안함. 4프로필ID/모바일수치/EXP·Gold/status정책 및72/15/36마리수 유지. 기존집/지하가명은사용안함.

관련검사: 신규4tests(네portal 실제접촉·whole-strip latch·6town originaloverview/camera/readonly/cache·MPF조인/지역분리/저장),기존V2177(10씬/변경27gate·12trips·원본4방향/공격/alpha),V219정상4slot재접속1,GroundTap/HUD,카메라helper/clip영향ContinuousMove/MillesSourceStyle/MillesGroundMaterial. 성장/전체스킬/장비 전체회귀는무관;원작숫자/드랍확정또는휴대폰수용주장없음. IMPLEMENTED;BUILD/HOST_NATIVE/PHONE/USER_VISUAL PENDING. V220별도후보.

## 2026-10-21 V217 구현·빌드·호스트 실행 검증 완료 — 실기기/원작 미확인 부분 대기

Runtime037da0145022dade21701e635894c4de893c44f1, focused39613760032/job119269177599 SUCCESS: 신규7tests/0failures-errors-skips.10씬/24방향gate의 실제 분기 왕복,arrival bounce,ground tap/held joystick,source4종48pose·hit/curse alpha,combat reward exactly-once,10씬 startup restore,12trip캐시 통과. 직전17056366/run39612638119의 NPC/town20쌍/ground/chase/save 관련13PASS는 영향받는 production Java와build.gradle이 최종 소스와 byte-identical이므로 재사용. 최초6e96409 compile 괄호 오류,17056366 loop fixture1실패는 별도 보존; 최종7PASS로 수정 검증. 보호 latch를 제거하거나 기대값을 완화하지 않고 실제 arrival 칸에서 걸어 나왔다가 되돌아오도록 fixture 수정.

APK PROJECT_DARK_V217_TEST.apk / com.projectdark.mobile.v217test,2026-10-21 23:08:17.589KST,101,661,704bytes,SHA25661a58b7d0feb729cd605a87c3444de14db98df9adfe16f0376b753ca19720689. Artifact12352390155/ZIP118392992bytes/SHA256c60e0d0843f0cc4406b8973f8f8a8e112c62cd2a73ab17bbfe5780229d197e16. ZIP/APK CRC, embedded exact source/run/package,cryptographic V2signature+1MiBcontentdigest,824source-decodedassets+1ADAPTEDstairs 및 retained3154existingassets actual bytes 확인.22native captures와3door/3house/5underground/C↔house3stair/source-direction 검토. 독립 설치/저장으로 기존 앱 보존, 자동 save이전 없음.

최종12map-switch first-frame host p50=13.9221125ms,p95=23.1573608ms,p99=24.120654560000002ms,max24.361478ms; host통계이며 phoneFPS 아님. Current-map4variant atlas8,847,360bytes,32floorchunks/8,652,800bytebound 통과·scene출발zero. PRscope39613770995 SUCCESS, full compile 의도적SKIPPED. 기존PR197미병합/main41fe6def유지. Source/receipt verification/DEATH_V217_BUILD.json. 이 종료 기록은metadata-only이며 새APK재빌드 금지.

확인된 범위: 대기실→야외1개/건물3개→집안3개→A–E5개. 건물1↔A↔B↔C/D↔E↔건물2 및 건물3↔C. 던전미추가. 205939영상 확인;205405영상은첨부전달실패로미검토. 야외/대기실 source선택과연결배치,미확인한국명 및 원작수치/드랍은 ADAPTED/PENDING이며 원작완전복원 주장금지. 보라슬라임원본105를실제영상모양과대조해선택(녹색176제외);좀비외3종의원작한국명미확정. 다음:exactV217휴대폰 이동/사냥/계단 사용자 확인,205405재업로드후원작배치대조. PHONE/USER_VISUAL PENDING.

## 2026-10-21 V217 죽음의마을 구현 후보 — 관련 CI 대기

Director 단일 편집. 기존 PR197/codex/rucesion-coast-v157, base828406914d494a2354462c35ed858c0ce6c1b8e8, main41fe6def 유지. 사용자 GitHub 명시 승인. 대기실·야외1개·건물3채/집안3개·지하A–E 총10씬. 건물1↔A↔B↔C/D↔E↔건물2, 건물3↔C 양방향. 던전 제외. 기존 World/Resolver/직접보상/저장 사용, 정적 맵/32chunkLRU/진입prewarm/출발release/좌표HUD/실제문·계단 latch. 공유 신전·장비·이동 cadence는 유지.

원본 Korean map archive 및 official741 HPF/MPF를 byte-preserved pack으로 보관. 205939 영상은 실제 존재/검토, 205405 영상은 첨부 전달 실패로 미검토. 야외/대기실 원본선택, 5개 인접맵의 연결 좌표, 미확인 몬스터 원래 한국명은 명시적 ADAPTED/PENDING. Editor text floor→인접 source 재질, 계단은 원본 돌재질로 만든 ADAPTED 독립오브젝트. 좀비 외3종은 원래 한국명으로 임의 재명명하지 않음. 수치/드랍/현재 원작 완전일치 주장 금지. 원본 main 던전 및 머미 미추가.

집중 검증:10씬/24방향 gate의 실제 이동·역방향·arrival bounce/held input, town카드20쌍·NPC, 원본4종/48pose와hit/curse alpha, shared combat exact-once reward,10씬 startup restore,12trip캐시. 공유 의존 때문에 NPC appearance·ground path·chase·save matrix 포함; 성장·스킬 전체 회귀는 이번 후보 범위 아님. 정확한 runtime SHA/CI/APK/hash는 성공 후 별도 기록. BUILD/HOST_RUNTIME/PHONE/USER_VISUAL PENDING. 계약 master/changes/V217_DEATH_VILLAGE.json, continuation docs/V217_DEATH_VILLAGE_CHECKPOINT.md.

## 2026-10-21 V209 mine / weapon display repair — build and host native verified

User rejected V208: installed APK SHA unknown; prior host PASS did not establish user visual acceptance. Missing labelled original Hell Sorcerer mns068 now replaces unsighted153. White/orange thief155 and red martial monk238 corrected; mage68/158 both drop mage robes. Priest robe sharing between those two casters is explicit mobile policy, not verified original priest ownership. Ten active appearances total, not ten guaranteed original Korean identities; first three soldier names remain descriptive. All server stats/rates remain unresolved mobile balance.

Runtime **90845f57470ca994abce73db1ced38eb053d22a0**, focused Actions **39516434711 SUCCESS**, artifact **12308520348**. Independently checked archive SHA/CRC, APK CRC/size/SHA/embedded source/run, all244packaged generated native PNG hashes and supplemental wearable bytes. **10suites/44tests,0failures/errors/skips**. Tests exercise actual mage entry/defeat/mage-armor inventory/checkpoint; source roles;26native tour coordinates; original96px minimap; player-obscuring wall fade; all available weapon axes and actual inventory/equipment callback pixel equality; prior NPC roundtrip,combat,loot,save,cache,Horror andmagic regressions. Actual entry, wall-overlap and entire weapon sheet visually reviewed. Shared32px NEdiagonal projection includes staffs; original indexed sources and held character art stay immutable.

Original150x102cell map remains unchanged. Entry corrected to12,34 and return13,35 based on retained recording; no invented square reshaping. All31decodable four-second source regions across uploaded127.974sec tour matched raw-map foreground/background. Actual build-host X102/Y76 screenshot versus120sec video:87RANSAC inliers,60cart-region,median0.635px. Full supplied YouTube is932sec; downloading both resolutions/storyboards returned195byteerror and browser remains black0:00 after play. Full932sec NOT viewed/verified. No full current-server topology/warp/spawn proof.

APK **PROJECT_DARK_V209_TEST.apk**, built **2026-10-21T08:43:03.257889+09:00**, **88070346bytes**, SHA256 **fce8bb394cdd037132af2ad843784912180439772c33d01ae59f890500cd7220**. Internal184/1.84labels legacy; embedded source/run identify this build. Prepared save failedHTTP403beforeupload; verified localAPK plus immutableActions artifact retained. Full common39516434627/39516440166 running at receipt; no fullcommonPASSclaim. Physical phone/user visual acceptance pending. PR197unmerged; main41fe6def5120b6c33b58f609e52fbaf2ead700eeunchanged. Receipt verification/MINE_HUNT_V209_BUILD.json; source contract master/source/mine_hunt_v209/repair_contract.json. Resume only outstanding commonCI/phone/source uncertainties, do not repeat completed source recovery or native tests without new evidence.

## V208 Mine acceptance amendment — 2026-10-21

Ten native source appearances683/684/685/11/151/155/156/158/238/153; distinct mobile profiles,100source-grid actors, shared resolver/AI/direct-inventory defeat ledger. Source stand/walk/attack frames and anchors, four mirrored directions and alpha-identical hit/curse variants. Original identities/server stats/rates not inferred from client art; first3labels descriptive and153associated candidate remain explicit. Runtime 5f4044d9bcd30fcbbb3d4f17d53209610c25d583/run39512080778SUCCESS:28focusedtests,0failures; full6suite evidence verification/MINE_HUNT_V208_BUILD.json. Original raw map2102/150×102, observed video landmarks checked; full current-server parity/phone/user acceptance pending.

## V207 Horror loop acceptance amendment — 2026-10-21

User requires rooms01..15in sequence, last kill15→fresh01 indefinitely, manual room-door→lobby with living actors allowed. Existing15random source humanoids/HP/EXP/combat/AI/no within-room respawn remain. New lap records last room then clears encounter HP/mask and increments serial before spawning01, preventing stale-dead room skipping; old13room saves remain accepted. Screenshot-guided native tile rooms12..15are ADAPTED, not original binary map recovery. Required gates: two full live GameView laps, exact12HUDportal cells and former-cell rejection, all15native door routes, partial second-lap save/restart and12cache roundtrips. V207b75b4175/run39445126813SUCCESS:27tests/0failures,all647nativePNGbytesmatched,fiveactualcapturesreviewed. No phone/original visual acceptance claim.

## 2026-10-20 V201 whole-screen magic and damage aggro repair

DEVICE_FAILED_USER_REPORTED: Mage/Cleric screen attacks (Ragnarok/Meteor/HolyDragon examples) skip visible blocked victims, and damaged victims fail to aggro. User did not explicitly name APK SHA; latest delivered6d21a7c. Current source reproduces two LOS filters (recipient selection and Resolver contact), AUTO requires a reachable/LOS target, and AI ignores damaged distant actors beyond180px.

Accepted fix: action-aware LOS policy only for player SCREEN magic (all11 registered:7damage+4screen curses); current viewport and living-target membership are checked again at contact. Ordinary single/physical and non-SCREEN shapes retain LOS; no offscreen/map-wide expansion. AUTO screen loadouts acquire visible targets without needing a path through their blocking furniture. Real positive damage provokes surviving actors for the current actor life, through shared RuntimeState.damage; both prototype and coast/Horror AI keep pursuing past passive detection range. Live terrain/actor collision, four directions and normal path cache remain authoritative. Confusion/aggro-reset, death/respawn and player death clear provocation; stealth/root/freeze gates remain effective. Provocation is ephemeral, no save schema change or original-server claim.

IMPLEMENTED_NATIVE_VERIFIED_HANDSET_PENDING. New tests enumerate all11screen spells with blocked rays, moving/hidden/dead victims, live contact visibility, real resource snapshot/charging, normal learned quickslots against original Horror furniture, AUTO and real far-target pursuit in Pote/coast/Horror. Existing221-ID/141spell, skill contact/spatial/resource, AI movement/collision/performance and Horror regressions selected. Assets/populations/rewards/cast timing unchanged. Same PR197/main41fe6def, candidate only; phone pending.


Verified APK source **8de565c4c954e53f93b45ef46cfe71cb300002d1**, focused Actions **39385004700 SUCCESS**, artifact **12246814750**. Built **2026-10-20T16:26:11.409340+09:00**, **85464049 bytes**, SHA256 **aa3ab97a5cec73e1f1213b5f6e7b00beb21be5d66a769cabc829acf6baf7fcb7**. Independently downloaded ZIP/APK CRC, embedded source/run identity and SHA verified; JUnit **9 suites/62 tests,0failure/0error/0skip**, including9V201 tests. Reviewed all7normal spell captures and1AUTO capture. Bounded resolver event capacity4096 preserves108 simultaneous victims'216 effect/feedback records. Fresh damage clears AGGRO_RESET and reprovokes. Existing221-ID/141magic suite passed. Common CI39385010450/39385004685 still running at receipt; do not infer PASS. Host native sdk34 evidence only; physical phone pending. Internal184/1.84 legacy version labels retained; build_evidence.scope contains legacy workflow text; verification/SCREEN_MAGIC_V201_BUILD.json is actual V201 scope. APK durable upload helper failed HTTP403; verified local file and GitHub artifact remain. No main merge.

## 2026-10-18 — V180 known-source runtime verified;12/13 STILL UNRESOLVED

Runtime/source4e1fb08d3dd6fc6c44a6ca0e73564be1f09e80a4; final cache-test/CI headc9b40628e36373cc0f81f58d33d31f3bd3f45888. Focused Actions39095897134/job117582142762 SUCCESS:7tests/1suite,0failures/errors/skips; actual native draws exercise warmed floor cache over12trips. Runtime code unchanged from4e1; later changes onlyCI and the stronger cache fixture. First native39095555903/job117581147935 independently downloaded artifact12124388526,ZIP SHAffe0d140a56f8ae99e0ffd533b830ff576d3b9e77c6a80f8032dd68f35f02434/CRC/XML7/0/0/0checked; actual lobby/corridor/room01/08 images reviewed against source recording. Final nativeartifact12124422686/SHA238b12245b9f8f3ca3b1702772bfbe88086f1ecb80d927e718adcd6c56df3ea9. Original-source graphics do not certify source-obscured Korean palette/timing or phone FPS.

Wrong common runs39095559473/39095556051 started because rg was absent in the runner and the shell condition fell through; both CANCELED. Guard now uses shell built-in, scoped source allowlist passes, final common39095899856/39095897132 compileSKIPPED. No unrelated suite should be repeated for this recovery.

User-facing HORROR_V180_SOURCE_COMPARISON.png saved (libfile_bc194dffdbf08191b1c225906373b5a2). **This is NOT a completed original13-room remake.** Twelve/thirteen still use isolated rejectedV179legacy geometry/rendering; exact source is missing. No new APK was built/delivered and no user/phone visual acceptance is claimed. Preserve user13-stage/15actors/norespawn/save rule. Resume ONLY missing12/13source/parity; keep recovered14maps/10MPFs and passed checks. Do not re-download source archives, redo unrelated Milles/Pote tests, or rebuild a candidate merely after interruption. PR197 remains open/unmerged;main41fe6defunchanged.

## 2026-10-18 — V180 original Horror recovery IN PROGRESS, not an accepted replacement

Recovered official DarkAges741 hades.dat MPFs363–372; visually matched all10humanoid appearances in supplied Korean recording. Master order maps363/368warriors,365/370rogues,364/369mages,367/372clerics,366/371monks. Replaced substitute wardrobes with original indexed pixels, original anchors,5walk frames and2–4attack frames in four directions. HP/EXP/combat rules unchanged.

Recovered Korean20210608maps archive and retained exact original map bytes:6002lobby50x20,6003corridor10x35,6801second-floor corridor12x30,6401–6411rooms01–11. Original447foreground HPFs,190floor tiles and palette/SOTP bytes are retained offline. Original tile/object/depth/collision layers replace authored geometry for these14maps; shared32x16movement grid uses uniform8/7source scale. Floor cache32x256x256RGBA≤8,388,608bytes; no manual recycle.

Video labels independently read:49s=02,53s=08,65s=11,73s=03. The gray/red and violet grids at73/113s are TAB navigation overlays, not original flooring. Do not repeat the V179 overlay-as-floor mistake. Video113s2Fhall matches6801carpet/sofas/white ornaments;6003has5actualround doors. Second-floor hall has its observed end arch rather than invented round doors.

**12/13original map files and their exact layouts are still MISSING.** They retain isolated rejectedV179legacy code during recovery; they are NOT source-faithful and MUST NOT be delivered as a completed13-room remake. No new APK is authorized by the visual gate until the remaining source/parity is resolved. User sequential13/15actors/norespawn rule stays intact; this is a WIP branch, not an accepted final build. All other exact-original monster timing, phone FPS and source-obscured material remain unverified.

Native compilation/rendering/Horror-only navigation/combat/clear/save checks are pending. Functional checks cannot certify13-room original parity. Scoped commit opt-in `[horror-scoped]` suppresses common CI ONLY if changes since previously verifiedaf19016are confined to the explicit Horror allowlist/docs; unmarked or unlisted changes resume common CI. The focused workflow runs Horror tests only, uploads evidence, and deliberately builds/delivers no APK.

Retained sources/generator:master/source/horror_v180/{original_monsters.zip,original_maps.zip,map_sources.json};tools/build_horror_v180.py;tools/horror_original_formats.py. Latest main41fe6defunchanged; existingPR197/branch retained. Do not redownload/reidentify these sources after interruption; resume from retained bytes.

## V179 Horror acceptance record — 2026-10-18

Milles manager→lobby→1F/2F corridors with multiple reachable doors→13compact rooms;15random actors once per room,no respawn,all-dead sequential01-through13→lobby. Existing Resolver/AI/direct reward/save used; seed/dead and living HP restored. Original source furniture/material pixels and human body/wardrobe resources retained; unseen layouts and exact Horror monster costumes/tuning remain ADAPTED/PENDING. Master U+B HP/EXP and final Lv99/100Gold design retained; free entry is prototype review policy. User cafe13434008/article312424 gateway returned401; article contents were not read.

10gender/job source-composited humanoids×4directions×6state cells have nonempty registered native frames. This is source human wardrobe reuse,not original Horror monster costume/action certification. MasterHP80k–140k/EXP150k–200k U+B anchors and40element profiles remain explicitly separated from original-server balance. Source furniture/masks/provenance are preserved in master/source/horror_v179;generator tools/build_horror_v179.py.

Terrain/live collision/shared AI/Resolver/reward/save use existing adapters. Single-map background7127040bytes≤8652800;12trip bounded-cache and predecoded atlas gates pass;GPU-visible background references are released without manual recycle. Physical phoneFPS/memory and fullHP five-job balance are unverified. Actual lobby/corridors/13rooms and fresh partial encounter restart14alive were reviewed. Runtime/APK source `4e5d88579fd9499a73c5a16cb76b9f846117aedf`; focused39075519734/job117521748733 SUCCESS:88tests/18suites,0failures/errors/skips. Downloaded XML,APK source/run/version/hash/size,ZIP digests/CRC and22Horror/retained-equipment-window bytes independently checked. Full common39075519707/job117521748864 SUCCESS;decoded logs confirm17successful Gradle executions plus machine-checked reuse of15actually successful prefix stages fromd7aaf0b/run39074826700. Other duplicate CI need not delay this verified delivery.

IMPLEMENTED / BUILD VERIFIED / HOST ANDROID NATIVE VERIFIED / COMMON VERIFIED. PHONE AND USER VISUAL ACCEPTANCE PENDING;main41fe6def/PR197 remains unmerged. Next: exact V179 phone feedback. Do not redo assets,88passing cases,build or saved upload after interruption. Exact live evidence/remaining fidelity limits:master/changes/V179_BUILD_RUNTIME_RECEIPT.json;resume docs/HORROR_CASTLE_V179_CHECKPOINT.md.

## V173 control acceptance update

User20261016: martial freeze8s blocks movement and attacks; root8s blocks movement while adjacent attacks remain legal. Runtime movement and both coast/prototype AI consult the same effects authority. Native verification pending; no monster artwork, stats or map population change. See master/changes/V173_MARTIAL_EFFECTS.json.

## 2026-10-15 — V162 original Pong palettes and live curse colors verified; delivery ready

IMPLEMENTED / BUILD VERIFIED / NATIVE RUNTIME VERIFIED. APK source `6f3ac7b900299330b7e15c1d374f3d56048c86db`; focused Actions38664473610/job116193095024 SUCCESS:128tests/19suites,0failures/errors/skips. Actual GameView inn scene plus standalone mouse,23coast species×4directions×3poses, hit overlap,10catalog curses, stronger/weak dispel, expiry/respawn/fresh-map lifetime and independent normal/curse material samples pass. Common PR38664479830/job116193114075 and push38664473609/job116193094457 SUCCESS; exact source decoded logs checked,26successful Gradle invocations each.

Pong normal violet91/37/121 and gold193/139/91 follow raw9.5s video samples; its cursed blue77.5/60.5/153 and warm212/157/96 follow raw16s samples. Both unchanged crops, rectangles and lossy-video evidence limits: master/source/abel_v162/reference.json. Existing timed CURSE is the sole authority; no duplicate visual flag. Normal restores on expiry/correct dispel/respawn; new campaign lives clear old reused species#slot effects while preserving player effects. Indoor and outdoor mouse paths both pass live combat effects. Other species use modest generic fading; their exact original curse palettes are unobserved/ADAPTED.

Scaled GPU filter was rejected after native edge alpha0→3 and63→0. Exact-alpha assertions were retained. Normal/hit/curse/curse-hit are prewarmed per map, each≤60textures:35,389,440CPU pixel bytes total, plus786,432mouse variant bytes. Floor32LRU/8,652,800bytes remains.12trip host renderer p95=3.46571,p99=5.07871,max=5.210656ms is not handsetFPS. Existing V16184 raw frames/alpha/pose/occupied-body size calibration and accepted Rucesion10 normal appearance retained; AI/movement/collision/rewards/save preserved. Phone memory/FPS/color and USER_VISUAL acceptance PENDING.

APK162/com.projectdark.mobile.v162test built2026-10-15T10:22:44.068597+09:00,80462485bytes,SHA256874cef5f2b2046fc915a90c9577c1228258dfeebc023d8836189d788388bc8a0. Original ZIP digests and2067packaged-source byte comparisons independently verified on a delivery runner, including all84 deterministic V161 frames; runtime diff is empty. Delivery Actions38666940549/job116200934445 SUCCESS, artifact11943270122 `PROJECT_DARK_V162_COMPLETE_DELIVERY`,ZIP80076319bytes,SHA256404edb8cc76fdf2da59a6ee2b11d70465b0e3bd7d1e5dcc563f6415e9991c1c7. Download: https://github.com/ChanJoos/Project_dark_android/actions/runs/38666940549/artifacts/11943270122. Bundle contains exact APK, Pong original/normal/curse/dispel comparison,23species comparison, actual inn comparison and V162_VERIFICATION.json.

Local exec_command/view_image stopped responding across read-only retries; no Library upload or local final-file verification is claimed. Same exact tested artifacts were rehydrated and checked on CI rather than regenerating art/rebuilding gameplay. If the file workspace returns, save this existing bundle; no art or runtime rebuild is needed. Source/runtime6f3ac7b9 differs from delivery-only a4d7d256 and documentation closure; never relabel the APK as documentation HEAD. Same PR197 open/unmerged, main41fe6def unchanged. Original indexed palette/curve/atlas and source-obscured V161 ratios remain unresolved. Receipt master/changes/V162_BUILD_RUNTIME_RECEIPT.json; review docs/ABEL_CURSE_V162_REVIEW.md. Next gameplay action is handset feedback, not another request to resume.

## 2026-10-15 — V162 normal Pong / live curse color correction in progress

User corrected V161 Pong reference: blue-faded monster was cursed, not its normal palette. Supplied 20261011 recording9.5s vivid violet actor and16s blue-faded actor recropped without altering pixels; reference/provenance master/source/abel_v162. Normal palette transferred through existing code-native material adapter; V16184 source poses, alpha, calibration and accepted Rucesion art retained. Live combat-owned CURSE (all catalog stages and screen variants) selects prewarmed RGB-only bitmap variants with exact source alpha. Native scaled-filter testing exposed altered transparent edges; variant caches replace that rejected approach. Current-map60source/60hit/60curse/60curse-hit max35,389,440bytes; clear on map entry, no per-draw raster editing; expiry/dispel/respawn returns normal. Status authority/stacking/combat math unchanged. Root sole editor on existing PR197. Implementation/tests prepared; BUILD/NATIVE/phone acceptance PENDING, no V162 APK yet.

## 2026-10-15 — V161 exact candidate verified and delivered

IMPLEMENTED / BUILD VERIFIED / NATIVE RUNTIME VERIFIED. Source `b7d1f0a52214e88a0dbb6eb46466e8dba29aaf76`; focused Actions38636656452/job116101434216 SUCCESS:120tests/18suites,0failures/errors/skips. Common PR38636665925/job116101468060 and push38636656320/job116101434881 both SUCCESS; exact source and decoded build logs checked.

All13 Abel coast IDs audited;7families84 newly reconstructed poses;10same-frame source/player envelope calibrations. New actual idle/walk/attack four-direction images and original/V160/V161 comparison reviewed. Both ZIP SHA/CRC and1130 packaged source files independently match. APK161/com.projectdark.mobile.v161test built2026-10-15T06:05:26.068905+09:00,80459941bytes,SHA256c715fc1c47d8125d81bfc994a048385c7e0f0487e4610221ff1bc3685eb73b9f. APK and2comparison files saved for delivery. Receipt `master/changes/V161_BUILD_RUNTIME_RECEIPT.json`.

Accepted Rucesion10 retained. Source atlas/cadence unavailable; manual ratios remain approximate. KingArk fake/real and purple exact source ratios unresolved due occlusion. Phone/user visual acceptance PENDING. Original canonical Crab/dungeon region joins remain open, not invented new spawns. Cache60source-hit pairs/32floorLRU and12trips retained; hostCPU p95=2.117406,p99=2.779085,max=3.201713ms is not phoneFPS.

Token interruption/scratch pruning recovered from exact GitHub source and Actions blobs; no asset regeneration. Implementation/required checks/file delivery complete; next concrete action is handset feedback. Same PR197, main41fe6def unchanged. Documentation closure does not relabel the APK source.

## 2026-10-15 — V159 full common Android verified; task closed

Exact APK/runtime source615bbd278c93168b7e5f11d8145e94c9ca4f7d6a full Android PR Actions38613964684/job116024149521 COMPLETED/SUCCESS. All configured retained skills/HUD/forest/campaign/growth/reward/checkpoint/render/native/build/APK verification steps completed; decoded logs confirm the exact source and successful builds. This supersedes the earlier common-PR IN_PROGRESS note; focused110 remains separately counted. APK/five native comparison images delivered; PR197 ready for review, main unchanged. Original unseen layout/motion/stat and Plant-eye visibility limits remain recorded; new phone/user visual acceptance PENDING. Receipt master/changes/V159_BUILD_RUNTIME_RECEIPT.json tracks same-source push38613954756 separately. Runtime implementation, appropriate checks, APK delivery and GitHub publication complete; next action is concrete phone feedback, not another request to resume. Subsequent documentation-only commits do not relabel APK source.

## 2026-10-15 — V159 both coasts and monster color pairs verified/delivered

V158 USER_VISUAL_REJECTED: cloned layouts, cheap repetitive sea, different paired anatomy/pose. V159 IMPLEMENTED / BUILD VERIFIED / NATIVE RUNTIME VERIFIED; new whole-coast/other-monster phone/user visual acceptance PENDING. Tyrant and Ariel USER_VISUAL_ACCEPTED by user, their pixels/size preserved. Arkpus=Plant84; Koatl uses Elect exact alpha/pose with copper ribs only; PoisonKakapo uses Kakapo exact alpha/pose with green shell only; RedHacon uses BlueHacon exact alpha/pose with red material, same gait.4directions idle/walk/contact attack compared; native alpha equality and retained navy/neutral details verified. Plant eyes remain source-foliage-occluded/UNRESOLVED; not declared eyeless or invented. Abel13 and Rucesion10 actual same-pose comparisons delivered.

Both coasts each have compact768 arrival, three2560square hunts and2304boss with distinct source-local rock/vegetation assemblages, coves/west inlets, curved shared collision shore. New1254water texture/blended sea depth/wet sand/broken thin foam removes repeated purple tiles/hard bands. All10 full maps and24local/2actual first-camera portal frames inspected. Source motifs observed, unseen complete topology/motion/stats ADAPTED. Original Ariel5job party event remains NOT_IMPLEMENTED_PREVIEW_ONLY0EXP/Gold. Full populations/legacy1B/cache/AI/sharedResolver/checkpoint retained.

Exact source615bbd278c93168b7e5f11d8145e94c9ca4f7d6a, focused Actions38613954729/job116024115724 SUCCESS:110tests/17suites/0failures/errors/skips. APK159/com.projectdark.mobile.v159test/PROJECT DARK V159 TEST/1.59-coast-source-pairs built2026-10-15T03:25:32.315872+09:00,78039577bytes,SHA256275ed1e0ed78a5309ff428af04e0cf5230097579f5b8ba980055d20ba0e949c7. Artifact11919228668/native11919540476 archiveSHA/CRC and2561 packaged file bytes independently verified.32floorLRU/60source-hit pairs/12trips/0warm floor rebuilds retained. Host renderer p95=2.866282,p99=4.480944,max=5.231788ms is not handsetFPS. Whole common Android runs38613964684/38613954756 IN_PROGRESS at APK delivery; final receipt supersedes this dated status after completion. Receipt master/changes/V159_BUILD_RUNTIME_RECEIPT.json, contract COAST_V159_SOURCE_PAIRS.json, review docs/COAST_V159_REVIEW.md. Same PR197/codex/rucesion-coast-v157, main41fe6def unchanged. APK/five actual native images saved and delivered; no merge claimed. Documentation-only closure must not relabel the tested APK source.

Interrupted scratch pruning removed local files, but GitHub code and detached color/water blobs were recovered byte-for-byte; no replacement generation/lost-source fidelity claim. Source174e7bb smoothing empty-inlet index[-1] produced29 failures, fixed in0e0539af; earlier angular/striped depth failed own visual QA and was repaired in85dbf820. Final615bbd changes launcher label and verifier only from that reviewed runtime, then reruns all110. Failures and visual rejection are preserved in receipt. Builtin imagegen prompts/reference edits: master/source/rucesion_v159; production canonical-frame material mapping uses CoastSpritePalette.java. Next concrete action is user phone feedback, with common-CI completion recorded separately; do not recreate assets or ask permission to resume.

## 2026-10-14 — V159 user visual repair in progress

V158 user rejected cloned coast layouts and inconsistent color-pair anatomy/pose comparison. User explicitly accepts Tyrant/Ariel appearance; preserve those pixels and size. V159 sole root editor on same PR197/codex/rucesion-coast-v157: Arkpus matches Plant84; Koatl uses exact ElectKoatl pose/alpha with copper-rib material only; PoisonKakapo uses exact Kakapo pose/alpha with green-shell material only; RedHacon uses BlueHacon geometry with warm/red material. Native palette variants happen during prewarm, preserve foot/alpha/cache pair bound; no repeated per-frame editing. Four-direction idle/walk/actual-contact attack comparisons include Rucesion10 and Abel13. Plant eye visibility remains foliage-occluded/UNRESOLVED, not asserted eyeless. Source3color-reference edits + builtin prompts preserved under master/source/rucesion_v159. No ordinary monster or original event/stat renaming.

Both Abel and Rucesion footage re-inspected: Abel2C overlapping rock columns, Abel3C dense mixed fan/palm/banana vegetation, Abel4C large uneven reef groups; Rucesion1C palm corridor and reef edge,2C fan/banana clearing,3B layered rock masses. New individually authored10contours/assemblages use winding coves, distinct western inlets and wet sand surf, no same arena plan/regular rock rows/palm stripes. Compact arrival/gold portal,1/2/3/boss labels/square bounds, full populations/legacy1B/shared collision/cache/AI/reward/save retained. Unseen full topology ADAPTED. Native/API build remains PENDING until exact final receipt; current delivered V158 APK is user-visual-rejected, do not re-deliver as repaired. Contract master/changes/COAST_V159_SOURCE_PAIRS.json. V159 separate test package, original Ariel party event remains preview-only. main41fe6def unchanged.

## 2026-10-14 — V158 final common Android regression passed

Exact source0e760fd8ccf5481ed7bed01fce78a02f915bed99 full Android PR run38575347594/job115890851329 COMPLETED/SUCCESS at2026-10-14T14:11:12Z (23:11KST). All configured native/rule/progression/skill/forest/checkpoint/render/build/APK verification steps completed; decoded logs inspected for exact SHA and successful builds. This supersedes the earlier PR IN_PROGRESS note. Focused100-test count remains separate. Same-source duplicate push38575337978 still running at closure, not claimed successful; required common regression is satisfied by exact-source PR success. No additional runtime change or replacement APK required. V158 files delivered and PR197 READY for review, main unchanged. Phone/user visual PENDING. Receipt master/changes/V158_BUILD_RUNTIME_RECEIPT.json. Task implementation/build/native/publication complete; no request to resume is needed. Next action is specific phone feedback; do not recreate assets or relabel the APK source as documentation head.

## 2026-10-14 — V158 coast device-feedback repair verified and delivered

V157 user phone report: DEVICE_FAILED_USER_REPORTED_VISUAL_UX for oversized empty arrivals, repetitive long maps and monster appearance/scale. V158 IMPLEMENTED / BUILD VERIFIED / NATIVE RUNTIME VERIFIED; new physical phone and USER_VISUAL acceptance PENDING. Two768-square arrivals with first-camera gold hunting portal; coast1/2/3 each2560-square, boss2304-square; authored large reef/grove layouts with shared live collision and connected portals. Legacy Abel1B save/quest aliases to1A, full populations retained. Latest user BlueHacon clarification changes blue starfish palette; orange Kakapo stays source-bound. Brown Koatl and wide low silver-grey ElectKoatl are separately named and bound. Species-specific player-relative silhouettes audited with original recording and actual equipped player, not uniform enlargement. Original unseen geometry/motion/stats remain ADAPTED; original Ariel five-job ritual NOT_IMPLEMENTED, preview-only0EXP/Gold.

Exact runtime/APK source0e760fd8ccf5481ed7bed01fce78a02f915bed99; Actions38575338140/job115890818157 SUCCESS,100 tests/16suites/0 failures/errors/skips. Actual camp entry/return and ten whole-map/native comparison scenes inspected. APK158/com.projectdark.mobile.v158test/1.58-coast-refinement built2026-10-14T22:55:48.374558+09:00,74,773,688bytes,SHA256229e94e2091f5d3c2433a9bf705b83f1b47a93f640ccb4e29b7de20265b35945. Artifact11900290487/native11900082431 archiveSHA/CRC and1125 packaged source files independently verified.32floorLRU/60source-hit pairs/12trips/0warm rebuilds retained; p95=1.815103ms,p99=2.17242ms,max=2.254383ms host renderer CPU, not handsetFPS. Broader push38575337978 and PR38575347594 still IN_PROGRESS at APK delivery; do not claim full common CI passed. Five-job live balance test passed at3905c649; not re-counted as final-source coverage. Receipt master/changes/V158_BUILD_RUNTIME_RECEIPT.json, contract COAST_V158_DEVICE_REFINEMENT.json, review docs/COAST_V158_REVIEW.md. Same PR197/codex/rucesion-coast-v157; main41fe6def unchanged, candidate not merged. Subsequent documentation-only closure must not relabel APK source.

Delivered APK and actual ten-monster/full-map images. Navigation: skill window test ON → MillesNoah Abel preview → AbelKevin Rucesion → RucesionLuke beach; AbelKenneth coast. Gold arrival portal enters hunting1. Normal level gates unchanged; test mode grants no progression. Resume only on concrete device feedback or pending broader-CI result; never repeat base64 asset creation for this completed candidate.

## 2026-10-14 — V157 coast exact Android candidate verified

Source c422ddd3b41874c05be6af872296cea99d47c6f1, focused Actions38561700387/job115844096162 SUCCESS.89 native tests/14suites,0 failures/errors/skips. Five maps,10species/120poses, actual town NPC entry/return, legal full populations/connected gates, restart and exactly-once shared resolver reward fixtures passed. Final15scene cameras and120pose grid match inspected pixels. Tyrant reward precedence now uses the admitted video-derived runtime profile while historical canonical source stays untouched. FullHP balance and original five-job Ariel party event are not verified/implemented. Ariel stays preview-only with0EXP/Gold.

APK157/com.projectdark.mobile.v157test, built2026-10-14T21:15:41.223546+09:00,74,810,740bytes,SHA2566e62ef162e290bab44dd234e2f90e28273d0ced5a26845d09cbb6180737b5c28. APK artifact11892337481/native11892237516 downloaded and archive digest/CRC verified; embedded source/run/version and1125 asset/metadata bytes independently match. Renderer12trips/32floorLRU/60source-hit pairs and0warm floor rebuilds; p95=3.153097ms/p99=3.734638ms/max=3.887294ms are host rendererCPU, not handsetFPS. Receipt master/changes/V157_BUILD_RUNTIME_RECEIPT.json. Physical phone/user visual acceptance PENDING. Normal entryLv71; existing skill-test preview gives no progression grants. Unseen geometry, cut joins, unsensed stats and motion remain ADAPTED. PR197 published; no main merge or complete original-map fidelity claim. Wider Android runs38561700451/38561708090 tracked separately in receipt. Subsequent documentation changes do not relabel the APK source.

## 2026-10-14 — V157 recovered coast implementation candidate

Continue PR197/codex/rucesion-coast-v157 on main41fe6def after scratch maintenance removed unpublished prior code/assets. Recovered original81.905292s recording SHA16562bf1f9f4113e5dba453ec9975a259a76807dd1f2acb332f40b2a13329e12 and userphoto; previous HTML reference used incorrect bird/crab stand-ins and is not Android implementation. Latest user explicitly confirms video shell boss is 아리엘. User-labelled nine species plus sensed 일렉코아틀 =10 IDs,120 newly regenerated independently registered poses; old lost sheets are not claimed byte-identical. Source sheets checkpointed to GitHub before integration; source/master CSV unchanged. Sense82/20163/196250,98/22345/248570,tyrant98/87801/1280575 retained with timestamp/element/version conflicts. SenseHP is not certified undamaged maximum; unsensed canonical fields null and gameplay profiles ADAPTED.

Playable candidate safe hub plus observed1-C/2-C/3-B and user-confirmed shellboss preview. Source-style independent tropical props/clear sand texture, shared terrain/collision/depth/lattice/.60s/path/AI/Resolver/checkpoint, LRU32floor/60source-hit pairs/prewarm retained. Source cut transitions, unseen whole extent, counts/placements/cadence/unknown stats and raw observedHP provisional maxima are ADAPTED. Normal beachNPC entryLv71; explicit existing skill-test preview grants no progression. Original five-job Ariel party ritual/reward unimplemented: Ariel room preview-only, zero prototype EXP/Gold, no fake original solo access. No monolithic screenshot map or generic bird/crab substitutes. Existing approved source pixels and player/HUD retained.

IMPLEMENTED_CANDIDATE. Static Master/source/alpha/120unique pose/padding checks pass. Android/native/build/APK/phone/user visual PENDING until exact Actions evidence. RucesionCoastV157Test covers NPCdialogue/gate/no grants/actual town-return, legal full populations/connected portals/save restart,120native poses,15actual native scene cameras/12 trips/warm CPU+cache,10species sharedResolver final-contact fixture/idempotent rewards/checkpoint. This is not fullHP/five-job balance proof or physical handset verification. Resume from exact branch, preserve V156 town/Abel/forest regression tests and all source history. APK is v157test side-by-side candidate after checks, never labeled currentmain until merged.

## 2026-10-13 — V153 exact-source third-circle candidate verified

IMPLEMENTED / BUILD VERIFIED / ANDROID NATIVE RUNTIME VERIFIED; PHONE / USER VISUAL PENDING. Same draft PR195 and branch, main unchanged. Source `6c0bdd21afcf00f32dab744d663d8740ae1c9aaf` / Actions38364329715 (primary115186846057) SUCCESS:132 unique primary tests/24 suites plus5 independent job executions =137 passing executions,133 distinct class/method pairs,0 failures/errors/skips. All five normal-job live navigation/AI/Resolver/purchased-gear/learned-skill/paid-potion E01–E18 simulations reachLv70 with positive Gold and117+ actual defeat receipts. Original source catalog remains separate from13 ADAPTED_MOBILE_SOLO_3C profiles. Ordinary EXP and quest milestone EXP are both included; this is not pure-grind pacing.

Final fixes prevent respawning onto an occupied actor tile and route ranged casting goals inside actual clamped follow-camera/dead-zone bounds. Actual regressions and5-job evidence pass. Retained five Milles40x16 portal pixel/touch-entry checks, common .60s movement, whole facades/props/live collision, caches, skills/HUD/save regressions. Eight downloaded artifacts SHA256/CRC validated; embedded identity plus87 packaged source/portal/NPC files independently matched; all five entrance renders and actual monster-cast scene inspected. Native floor warm rebuilds0/p95=2.753212ms is host CPU, not handset FPS.

APK153/com.projectdark.mobile.v153test, built2026-10-13T16:52:27.052166+09:00, 70174809bytes, SHA2565a9d040c8a1ff5d3bf0038bfec13641b5f60a4977159bb8cae2cfa5bedc4185b, artifact11796691195. Preview11797006485/native11796254423. Exact receipt master/changes/V153_BUILD_RUNTIME_RECEIPT.json. Balance maps are entered directly per quest and merchant transactions occur between waves; continuous shop/report journeys are outside the hunting simulation and independently tested UI/portal paths do not claim a handset walkthrough. Mage uses normally learned element-aware manual spell selection; AUTO weakness optimization unverified. Separate test profile; no save migration/main merge claim. Continue physical-phone/user review from this exact receipt without restarting art/map/combat work.

## 2026-10-13 — V153 third-circle combat and progression candidate

User explicitly requests all reviewed gaps implemented. Root sole editor, same PR195/branch.13 ADAPTED level/HP/MP/AC/MDEF/element/reward profiles now drive actual shared Resolver physical/spell attacks, resource/cooldown/LOS gates, saved timed effects, per-map AI state, Berserker rage and bounded KingArk retreat/recovery.18 E quests reachLv70; three paid/quest gear tiers41/56/65 reuse existing approved appearance. Original sourceLv88/HP61982/EXP606252 and source media retained. Native tests include actual GameView casting plus full-population5-job continuous live-navigation/AI/normal learned-skill/finite Gold/potion fights without per-kill rest. Build/native/phone/user acceptance PENDING until exact receipt; implementation alone is not balance verification. Contract master/changes/ABEL_3C_V153.json. Preserve V152 entrance portals, single town and hunting performance caches.

## 2026-10-12 — V143 exact candidate build and native input/render verified

Source601fc35a57d259eb24a8c44bc7846f52b0dca1be / focusedActions38180326177/job114591314579 SUCCESS. Downloaded3 ZIPs and independently verified digests/CRC;109 unique tests/22 suites,0 failures/errors/skips.46 core including4 new V143 tests plus63 retained regressions. Real normal basic attack button input contacts actual target, no particle pulse, retained group-e punch poses differ for both genders/all4 directions. Native8 preparation/contact comparisons inspected. Eligible normalLv41 player walks toNoah/dialogue/touch and entersAbel with no test flag or EXP/Gold/item grant. Lv1 player sees disabled Abel option and explicit conditions; gate remains intact. NativeNoah dialogue and corrected monster screens inspected.12round trips retainmax60source-hit pairs/floorLRU32/8652800pixel bytes.330 Pote/coast files and entire skill-presentation bytes match APK; no new art or combat balance. KingArk canvas3.5x normal; original shape/event fidelity and normalKraken join remain OPEN.

APK com.projectdark.mobile.v143test/143/67603932bytes,built2026-10-12T07:29:49.253127+09:00,SHA2561473db586d96e86cd3186a2b83dfe53bfce8821f0736762821f31a560a5018ab;APKartifact11718295007/native11717646068/preview11717500639. Earlysource dbb140f294295dec13d9b73ad2b169080c65f92c failed solely native screenshot test's ambiguous android.graphics.Path/java.nio.file.Path import; explicit import repaired, runtime Java/assets unchanged. IMPLEMENTED / BUILD VERIFIED / NATIVE SCOPE VERIFIED; DEVICE_REPORT remains user-reported historical failure, new physical-phone/user visual approval PENDING. Same draft/unmergedPR195. FullAndroid38180326232/38180329198 IN_PROGRESS_UNCERTIFIED at receipt. Separate candidate profile, no deletion/save-transfer instruction. Receipt master/changes/V143_BUILD_RUNTIME_RECEIPT.json. Source name corrections in master/source/abel_v143/coverage.json supersede wrong V141/V142 joins while preserving original source snapshots. Slate unknown; user-assigned 보라/튜터 not promoted to independently verified original names.

## 2026-10-12 — V143 user identity corrections, basic punch and Noah travel repair

Director single editor continues PR195/codex/abel-coast-v139 from2e77b4a/main deed7e5. DEVICE_FAILED_USER_REPORTED: martial basic punch not visibly animating, and Abel NPC connection undiscoverable. Root source diagnosis: unlearned PUNCH has no presentation entry and CharacterRenderer's non-SWING attack fallback draws idle; Noah overlapped620/560 spawn and Abel button was invisible beforeLv41/D04. Repair reuses retained male/female group-e PUNCH source and the existing basic contact clock/Resolver; no combat values, weapon semantics or source image changes. Noah moves to the already verified walkable512/544 tile, role becomes 아벨·포테 이동, and Abel option plus eligibility always displays. Normal eligibility remainsLv41/forestFinished, test preview retains no progression grants.

User-confirmed bindings: green=애벌레; orange floater=가스; armored crab=그래브; slate blob explicitly NOT Grab, retain 회청색 몬스터 until original name found; purple egg=보라, mouth/stalk=튜터 (USER_ASSIGNED names), teal boss=광폭자. Retire runtime duplicateCRAB/orange IDs, keep source/history snapshots and reward receipts;13 runtime IDs/12 art families, same72/96/108 population, <=60source-hit pairs. E05 targetsGrab+slate and E07 remainsOctopus+Tutor. KingArk separate from normalKraken, projected scale3.5x60; original art/event fidelity and normalKraken appearance join remain OPEN. Current source/coverage master/source/abel_v143/coverage.json supersedes old identity joins without rewriting historical captures.

IMPLEMENTED_CANDIDATE, exact native/build/APK pending. New native test covers normal unlearned punch input/contact/no particles, both bodies/all four directions, source pose visual differences; normal eligible NPC walk/dialogue/touch without test mode/reward grant; visible locked conditions; name/art/no-duplicates/KingArk scale. Retain105 prior coast/campaign/cache/HUD/skills/save checks. Snapshot media/source bytes unchanged. Required source consumers: AGENTS.md; design/DATA_CONTRACT.md; docs/HUNT_MAP_PERFORMANCE_LESSONS.md; docs/MONSTER_CREATION_MANUAL.md; docs/SKILL_PRESENTATION_CONTRACT.md; docs/SKILL_ABILITY_CONTRACT.md; app/src/main/assets/skill-presentation/catalog.json profilesPUNCH+bodye frames; master/assets/animation_frames/body/mm001/e andwm001/e; V142 coverage/source; currentWorldDef/NpcIdentity/GameView/EquipmentActionResolver/CombatController/sharedResolver. Physical phone verification pending after exact native/APK receipt.

## 2026-10-12 — V142 exact-source candidate verified after reward-policy repair

IMPLEMENTED / BUILD VERIFIED / NATIVE SCOPE VERIFIED; PHONE / USER VISUAL ACCEPTANCE PENDING; original fidelity and exhaustive roster closure remain OPEN. Exact runtime source 2cb7a9c837427ff5c96e409aacd45ec4d64d432a, app tree e10e238958058b5c1d823535e366c6d8eaf984d9 matches the reviewed local index. Focused Actions38177352950/job114582428401 SUCCESS. Downloaded all3 ZIPs; independent SHA256/CRC and105 unique tests/21 suites,0 failures/errors/skips.42 coast/campaign/input plus63 retained forest/HUD/skills/save tests. All13 live candidate profiles resolve exactly once and save/restore; five jobs complete E01–E08 toLv55. Native156 identity/pose draws and actual six-map GameView screens inspected.12round trips keep60source-hit pairs/floorLRU32/8652800pixel bytes;48 real DOWN/UP callbacks p95=10.843099ms/max18.259203ms are Robolectric CPU, NOT handset FPS.

First source9752b858 failed2 reward assertions: canonical Berserker606252EXP leaked into adapted3000HP/95000EXP actor. Repair admits only explicitly matching live coast profiles before canonical fallback; original catalog606252 and unrelated source fixture rewards retained. No assertion removed. Source failure receipt V142_FIRST_CI_FAILURE.json.330 retained Pote/coast files and all skill-presentation files independently match APK; old rejected art remains absent. APK com.projectdark.mobile.v142test/142/67603808bytes,built2026-10-12T06:47:46.581765+09:00,SHA256d017c6060e99ece396f320f049fa395831bacf9783d4c7674f26f28a05f5329f,artifact11716179308. Native11716836195/preview11716139306. Same draft/unmerged PR195; broader Android38177352986/38177356934 still IN_PROGRESS at receipt, uncertified. Receipt master/changes/V142_BUILD_RUNTIME_RECEIPT.json.

13 candidate identities/12 art families include all seven newly observed video forms plus existing four families and two adapted KingArk identities sharing art. Do not claim original event reconstruction, final names, original directional cadence, complete interval review or user acceptance. Unknown names, Crab/Berserker visual joins, black-pile monster/scenery classification and Slug/Escomoid coast association stay open. Pending note below is superseded only for exact tested native/build scope. Resume remaining original-source/fidelity/event work without resetting this branch or regenerating accepted forest art. No previous app removal or save transfer required: this separate test package uses a fresh profile.

## 2026-10-12 — V142 supplied second recording: whole visual roster candidate implementation

Director single editor continues PR195/codex/abel-coast-v139 on464a379/main deed7e5. User requires ALL Abel monsters and supplied60.8s second recording. Fully decoded1846 unique frames; manually reviewed61 one-second samples,32 early quarter-second target samples and enlarged selected crops. Original full YouTube interval completeness is not certified. Source receipt/hash and2-C→3-C→4-C transitions: master/source/abel_v142/research.json. Visible names establish 그래브 and 문어. Seven new observed visual forms: slate-blue Grab, silver-blue armored creature, round red octopus, spotted purple hovering egg, spotted orange hovering lobed body, purple toothed looping-stalk body, teal octopus boss. Unknown names are retained; Crab/Berserker visual joins remain PROVISIONAL, not canonical proof.

IMPLEMENTED_CANDIDATE:13 runtime identities/12 art families across existing6 maps,8 new generated12-pose sheets=96 additional192RGBA frames/ground184. Fake/real KingArk are adapted solo encounters sharing one partially obscured historical purple-boss reconstruction; original party triggers/event sequence and fake/real art difference are NOT implemented/verified. They are not falsely labelled original event restoration. Per-zone roster limits each map to5 distinct art families/60 source-hit pairs, retaining72/96/108 population, live collision/shared AI/Resolver/direct reward/save and unchanged approved player/HUD/forest. E05 now hunts Grab/armored; E07 red octopus/purple maw, stable quest IDs and claimed rewards retained. No old rejected crab/blue-octopus/red-octopus art reused. Current native tests exercise all13 identities with real Resolver, exactly-once receipts and save restoration,156 identity/pose draws and12 round trips. Source/master/96-pose static checks PASS; Android build/native/device/visual PENDING until exact CI.

All known coastline/event IDs now have candidate bindings, but whole ORIGINAL restoration remains OPEN: original name/version joins, black spiky pile entity/scenery classification, Slug/Escomoid region joins, original event rules, directional animation/fidelity and user visual/phone acceptance. Coverage gate distinguishes candidate visual coverage from original completion. Do not describe13 candidates as an accepted exhaustive original roster. New generator/provenance and coverage snapshot are additive; earlier original source snapshots retained unchanged.

## 2026-10-11 — V141 resumed candidate verified; whole Abel restoration BLOCKED

Partial candidate only: 4 species (Turtle/Arkpus/Pong/Gas), NOT all Abel monsters. Exact APK source 4ba719f088ecdddbb3957d15013572da8b1d8687; focused Actions38144172737/job114483952099 SUCCESS. Downloaded preview/native/APK ZIP digest+CRC independently verified; 104 unique tests/21 suites, zero failures/errors/skips; 234 retained Pote/coast sprite bytes independently matched. Actual GameView map/48-pose screens reviewed. APK com.projectdark.mobile.v141test/141,64362280bytes,SHA256 e079018dd4ab46562be3706fbca492e520c44171ff5ddd044ca061743e9f4c2d,built2026-10-11T22:48:48.086385+09:00,artifact11703644426. Preview11703843439/native11703589672. Real48-touch callback p95=11.345031ms/max19.260724ms; Robolectric CPU, not handset FPS. Phone/user visual approval remains PENDING. Full Android38144175384 still running at focused receipt; not certified. Same draft/unmerged PR195.

Candidate runtime is unchanged versus prior af2d9d4; this resume repaired the stale23-vs22 reward test and added full-roster coverage/identity checks. Current coverage-only head ec035776a011e53a08ed06753b406130a8201f3c has identical app/asset bundles to built source. Full restoration gate remains false: missing Grab/Crab/Berserker/fake+real KingArk, three observed unlabelled silhouettes, provisional roster not closed, second video9:31–11:50 readyState0/download canceled. Old-dungeon Slug/Escomoid region joins unresolved. No additional original species/art admitted; no imagined replacement. User needs the requested interval as a recording to unblock video-specific evidence. Resume with frame/identity extraction and every in-scope missing-family/event implementation, then native/phone/visual verification. Evidence master/changes/V141_RESUME_BUILD_RUNTIME_RECEIPT.json and master/source/abel_v141/coverage.json.

## 2026-10-11 — V141 resume: whole-roster scope retained, candidate CI repair

Resume on remote af2d9d4/main deed7e5, same PR195/coast branch, Director single editor. User explicitly requires ALL Abel monsters; four candidates are NOT completion. Added master/source/abel_v141/coverage.json and tools/audit_abel_coverage.py: known coastline/event inventory includes Turtle/Arkpus/Pong/Gas/Grab/Crab/Berserker/fake KingArk/real KingArk; Slug/Escomoid region joins pending. Inventory is provisional, not exhaustive. Original art acceptance false; second video9:31–11:50 still blocked (browser readyState0/videoWidth0; media download canceled). No frame-review claim.

Diagnosed Actions38142597842/job114479340076 failure: AdaptedCampaignRewardTest expected23 profiles after rejected families removed; actual22. Correct count plus explicit admitted/rejected coast identities, preserving source/Spirit assertions and exactly-once checks. Static coverage/12-pose/art/reward binding PASS; --require-complete correctly REJECTS partial roster. Native/build/APK PENDING rerun. Existing candidate runtime unchanged; no new species invented or admitted. Rejected blue octopus/crab art stays absent. Hunt-map performance baseline unchanged. Immediate unresolved task: secure requested video interval and labelled missing-family references, implement every identified family and event, then native/phone/visual verification. Do not mark V141 as all-monster restoration.

## 2026-10-11 — V141 supplied Abel video reconstruction, implementation candidate

Director single editor on existing PR195/codex/abel-coast-v139; base remote2b0de3c/main deed7e5. User rejects invented blue octopus/crab and supplies two videos plus34.8s recording. Actual recording decoded1018frames/970unique; inspected2fps samples/contact sheet and Arkpus/Pong/Gas crops. Not individually manually reviewed all1018frames. Second video9:31–11:50 playback/download/export unavailable; no claim of new observed species. Reference details and limits: master/source/abel_v141/research.json.

IMPLEMENTED_CANDIDATE: remove rejected species/art from spawns/registry/rewards and synchronized APK pack; replace rounded red octopus/cartoon pong with36reference-reconstructed generated Arkpus/Pong/Gas192RGBA poses, ground184. Retain turtle12, population72/96/108, current player/UI and shared navigation/collision/AI/Resolver/save. Six maps now have connected tall/low reef masses with surrounding palms/banana/ferns and inland debris. Map topology, pose directions, combat numbers/economy are ADAPTED; original animation/full-map match/all-monster coverage/second-video analysis OPEN. E03/E05/E07 now targetArkpus, quest IDs retained. Build/native/APK/phone/user-visual pending. Hunt performance gates retained (prewarm,static navigation,live collision,LRU32/8,652,800;source+hit<=60;vsync;apply/commit).

## 2026-10-09 — V122 exact combined repair verified

IMPLEMENTED / BUILD_VERIFIED / NATIVE_SCOPE_VERIFIED. Runtime source363ddca72c0c210568f844c714c41859c6358bea;focused Actions37871336909/job113629894711 SUCCESS. Downloaded ZIP digests match GitHub;32 XML suites independently counted125tests,0failures/errors/skips. APK59,949,763bytes,SHA2564ee239646e4c0b003fd56364ff8d320ed0c3269f363bf3d1ba3a78bd20832c1d,built2026-10-09T10:54:00.835364+09:00. APK artifact11589928876/native11589908910. Embedded source/run/version122/package and168unchanged sprite bytes verified. Separate com.projectdark.mobile.v122test,fresh profile;draftPR180/unmerged/main18ef615. Release https://github.com/ChanJoos/Project_dark_android/releases/tag/candidate-v122-363ddca7-37871336909-1 . Evidence verification/MONSTER_VISUAL_V122_BUILD.json.

Death collapse/fade/fields removed; dead actors immediately excluded and zero corpse/pop-up pixels verified, kill/reward exact-once/respawn/save unchanged. Mantis72→180visual canvas (2.5x), same192px source and184px ground anchor, matching actor/culling/tap bounds, collisionradius18 unchanged. Spirit lower mesh pinned and screen anchor snapped with reusable storage: both colors×4facings×16phases/fractional offsets have identical hoof rasters, no invisible feet. Native four-facing tree/giant-ant/antlion idle/walk grid and head-centroid orientation checks pass;back idle shares correct art AND mesh rasterization with walking. All final native grids/boss scene/foot comparison manually reviewed. Mapping corrections are ADAPTED; no regenerated assets or original-animation proof.

First source9cd4fd28/run37870740856 had125tests/1failure: tree NW alias matched texture but idle bitmap vs walk mesh changed pixels. Fixed actual renderer and preserved exact comparison; final125zero-failure verification supersedes that failed candidate. Paired360native frames versus V120: A render p9513.000→14.391,p9914.994→15.932,max177.773→189.457ms;C p956.290→6.913,p997.205→9.336,max90.159→82.936ms. Some CPU tails rose. Memory/map-switch/navigation/live-collision/save gates pass;native CPU is not phone FPS or proof of retained handset speed. V122 physical phone/user visual acceptance PENDING. Broader Android runs37871336861/37871342167 IN_PROGRESS at closure.

User's earlier options3/4 remain UNKNOWN_SCOPE: exact prior list is missing and two conversation-context searches errored;directly specified four monster repairs are complete in this candidate. Ask only for option names, then process3/4together. Do not infer their definitions or claim them completed. Preserve V120 art/user speed baseline;exact reported handset position for spirit hoof issue was not provided,so test covers phase/fractional positions and user field verification remains pending. Closure is docs/evidence only;delivered runtime remains363ddca7.

## 2026-10-09 — V122 combined visible monster repair

User repeats removal of death presentation and requests giant mantis2–3x scale, spirit hoof graphics audit, and ant/Ent NW walk→idle consistency. Director handles one combined candidate on PR180. Mantis presentation canvas72→180 (2.5x), source192RGBA unchanged, proportional184px foot anchor retained, large actor occlusion/culling/tap selection follows visual body; collision radius18/damage/rewards unchanged. Spirit lower mesh vertices stay rigid while upper body moves; snap its screen anchor to integer device pixels with reusable Matrix/buffer to prevent subpixel hoof loss. Native gait/fractional-position tests compare actual hoof pixels. Trant/giant-ant back idle reuse the matching walk silhouette; antlion back-direction sources were swapped, now NW/NE resolve opposite labels and back attacks reuse correct idle instead of a frontal attack. All168 sprite files and source provenance remain byte-unchanged; mapping is ADAPTED visual correction, not new original artwork.

Death code is removed, actors vanish immediately; hit/walk/contact improvements and exact-once rewards/respawn/save/performance contracts retained. Updated native tests plus V122 feet/facing/enlarged-boss/live-map tests; BUILD/NATIVE/APK pending. Prior options3/4 remain UNKNOWN_SCOPE: original numbered list absent, two personal-context searches returned conversation errors. Do not claim those unidentified options are complete. Confirm only their names while proceeding with these explicit repairs.

## 2026-10-09 — user removes death presentation; options3/4 awaiting identification

Explicit user instruction supersedes V121 corpse acceptance: remove all death collapse/rotation/scale/fade and presentation timers/facing fields. Dead monsters disappear immediately from GameView and renderer. Preserve walking/contact/recovery/hit improvements, accepted168sprite bytes, frozen dead movement, exact-once defeat/rewards, existing respawn and save contracts. Native tests now require zero dead-actor pixels and identical actual GameView after death versus dead actor outside the scene; no tick/camera/other-actor changes in that comparison.

IMPLEMENTED_LOCAL, BUILD/NATIVE pending. User also requests prior options3 and4 together. Their definitions are absent from recovered current conversation/project handoff; personal-context retrieval errored. Do not invent their scope. Ask only for option names, then combine them in one reviewed candidate. Same branch/PR180; no main promotion or combined completion claim.

## 2026-10-09 — V121 exact candidate verified

IMPLEMENTED / BUILD_VERIFIED / NATIVE_SCOPE_VERIFIED. Runtime source `794823078ec3a5ee2b93fcb8803a939273721938`, focused Actions `37866922982`/job `113615737171` SUCCESS. Downloaded APK/native ZIPs exactly match GitHub digests;31 XML suites contain121 tests,0 failures/errors/skips. Five new native tests cover14families×4facings continuous changing renders, legacy contact/recovery/facing/exact-once hit, frozen untargetable corpse/infinite respawn clock/exact-once defeat, cache-stable hit/death and actual GameView corpse integration. Existing campaign/quest/reward/save/navigation/live-collision/joystick and12map-switch source+hit/floor memory tests pass. Native motion/contact/hit/death/corpse scenes manually reviewed. Approved168PNG bytes remain identical to accepted V120; generated interpolation is not a new authored-frame atlas or original-game motion proof.

APK59,949,443bytes SHA256 `7ac0ddacd843ef1f58fae671b1cae733dbe6fee552e90a879890b1adbcec924a`;built `2026-10-09T09:59:48.033294+09:00`;APK artifact `11589150496`, native `11588472821`. Embedded source/run/version121/package and168asset bytes independently verified. Release https://github.com/ChanJoos/Project_dark_android/releases/tag/candidate-v121-79482307-37866922982-1 . Evidence `verification/MONSTER_MOTION_V121_BUILD.json`.

Paired360-frame native CPU comparison against accepted V120a7de0466: A render median7.708→7.673,p9513.866→13.476,p9914.913→15.053,max179.616→175.832ms;C median5.857→5.976,p956.452→6.739,p9910.027→10.762,max84.676→80.116ms. C tails rise slightly; cold spikes remain. These independent timing distributions do not prove phone FPS or sustained device frame recovery. No population/resolution reduction, no new bitmap cache, no chase/navigation/save tuning. V120 broad runs37864101065/37864095323 nowSUCCESS. V121 broader Android push/PR runs37866923090/37866928207 IN_PROGRESS at closure. Candidate remains unmerged draftPR180/main18ef615. Separate com.projectdark.mobile.v121test,new profile;PHONE/USER_VISUAL PENDING. V120 user acceptance remains the approved baseline.

Next: exact V121 phone movement/attack/hit/death/first-kill review; compare perceived deformation and sustained performance. Preserve baseline rather than regenerate accepted artwork. Closure changes only docs/evidence; delivered runtime source remains79482307.

## 2026-10-09 — V121 continuous motion: user-approved V120 baseline

User confirms option1 (V120 baseline) and requests option2 (monster animation/combat feel). Record V120 USER_ACCEPTED_BASELINE from that message; no independently captured physical trace. Preserve all168 accepted192RGBA sprites, their provenance/anchors/body sizes, player art, map geometry/populations, collision, damage/cooldowns/rewards/save contracts and performance lessons.

V121 task: asset-preserving procedural 35-vertex articulation for four-facing walk, attack anticipation/contact/recovery, actual-hit recoil,0.60s transient corpse collapse/fade and readable damage numbers. This is ADAPTED_PRESENTATION_INTERPOLATION, not newly authored multi-frame sprite atlases or verified original animation. Death becomes non-selectable immediately; rewards/quest credit remain exact-once at actual defeat; corpse cannot slide; respawn schedule unchanged. Legacy attack recovery now starts at resolved contact rather than being spent during preparation. Existing0.24s windup/0.36s recovery and damage/cooldown values retained. Source/hit prewarm caches remain bounded; one reusable35-vertex buffer, no per-frame bitmap creation. Same active branch/PR180; main unchanged. Exact CI/runtime/APK and V121 user visual/device acceptance PENDING. Contract: docs/MONSTER_MOTION_V121_CONTRACT.md.

## 2026-10-09 — V120 resumed: exact candidate verified

IMPLEMENTED / BUILD_VERIFIED / NATIVE_SCOPE_VERIFIED: source `a7de04660d317d64f2b6b07cd44e2d57c5da4462`, focused Actions `37864095275` SUCCESS; APK artifact `11587763303`, native `11587881579`. Independently downloaded both ZIPs and verified GitHub digests, APK CRC/embedded exact source/run/version/package and all168 runtime PNG bytes.30 XML suites contain116 tests, zero failures/errors/skips, including actual JoystickV119Test. APK59,947,419bytes, SHA256 `46d782c787258c0814bdce3e6f8e3cfc344efe8dbd7b1930c0ec59614cf926fb`, built `2026-10-09T09:26:25.042284+09:00`. Evidence: `verification/MONSTER_STYLE_V120_BUILD.json`.

Resumption fixed two CI-only faults: android.yml still invoked V119 APK version verifier against version120; focused workflow selected nonexistent JoystickV120Test and silently omitted the existing joystick test. Corrected both and verified actual joystick XML in successful exact-source artifact. Runtime Java/assets/Master are byte-identical to f4a37475; no new performance tuning. V120 crop generator reproducibility, all168 frame hashes/192RGBA/ground184 and three retained original screenshot hashes pass. Manually reviewed48 replacement poses against unchanged spirit at runtime scale and native B/C scenes.

User speed resolution remains USER_REPORTED_RESOLVED without identifying an exact handset APK. Mandatory hunting-map performance lessons and AGENTS.md link retained. Paired native CPU timings remain environment-specific and do not prove phone FPS or sustained maximum-frame recovery. Separate `com.projectdark.mobile.v120test` preserves existing installed apps/saves; it uses a fresh separate profile. New assets remain ADAPTED_GENERATED and PHONE/USER_VISUAL PENDING. Draft PR180/unmerged versus main18ef615; no main promotion. Broad Android runs37864101065/37864095323 still IN_PROGRESS at closure; prior broad failure37857894010 was obsolete-version verification after preceding gameplay steps succeeded.

Next: inspect exact V120 phone movement/first attack and four-family style; preserve accepted performance baseline. Continue missing original monster/animation evidence only if requested. Do not restart asset generation or substitute old V118/V119 APK.

## 2026-10-09 — V120 spirit-style monster replacement

User reports forest speed resolved; preserve V119 runtime02026e91 and performance regressions. Exact handset APK was not named, so record USER_REPORTED_RESOLVED rather than independent device trace. Mandatory new-map baseline: docs/HUNT_MAP_PERFORMANCE_LESSONS.md, linked from AGENTS.md.

User-provided catalog1000057394/7396/7398.jpg preserved byte-for-byte under master/source/monsters/user_style_20261009 with hashes/U_REFERENCE_ONLY. Newly generated gray lycan, olive-brown gnoll with axe, armored gnoll wolf-rider with saddle/spear, silver wolf replace48 existing campaign_v3 poses. Style follows unchanged Pote spirit palette/broad-shading/outline.12 still poses per family=idle/walk/attack×NW/NE/SW/SE; not continuous animation. All source sheets/prompts/crop/hash provenance retained. Frames192RGBA, ground184; original family-wide scale and runtime/collision/AI/rewards/caches retained. Generated interpretation is ADAPTED, not original artwork or user visual acceptance.

IMPLEMENTED candidate on existing branch codex/pote-forest-v112 / PR180, main18ef615 unchanged. Local Master integrity PASS; all168 pose hashes/resolution/transparent edges/ground184 PASS; other120 sprites byte-unchanged. Lycan projected heights40.875–48px preserve acceptance bounds. Exact-source CI/native/APK pending. Separate com.projectdark.mobile.v120test preserves prior installed profile; no automatic save transfer claim. Next: native all168 pose rendering/cache/navigation/save regressions plus exact package verification, then user phone/style review.

## 2026-10-09 — V118 exact candidate build/native verified

Runtime source9f0d09e3df6f5beaabb8456d552cf7071b122aae;focused Actions37840619005/job113528745268 SUCCESS. Downloaded native archive SHA256aecf4f5b6680436e00ea85398a50306de651178f941cabcb75ddc81bd0285e8f matches GitHub digest;26 XML suites independently counted109 tests,0failures/errors/skips. Includes168 full-resolution native pose draws,all-map prewarm cache/variant checks,periodic apply/pause commit,spirit instance canonical EXP308950 exact-once/unbound guard,retained save/restart,4-direction bounds and32 actual touches/16 routes. Paired V116 comparison preserves target/status/shortest lengths;CPU tap medians A88.339→2.864ms,B112.033→3.155ms,C142.773→2.167ms,D1.522→0.296ms. These are JVM/native CPU timings,NOT phone timings.

APK60,062,907bytes,SHA256a68ddfc84fa8a3a49b18e0a764f02a166b87558a77e6a0ebb1f4917ec8241023,built2026-10-09 05:41:54KST;APK artifact11577816876/native11578160902. Downloaded release APK digest/CRC/exact source/run/version and all168 PNG original bytes verified. Manually reviewed all168 source pose contact sheet and native A/C scenes. Evidence verification/MONSTER_QUALITY_V118_BUILD.json;release https://github.com/ChanJoos/Project_dark_android/releases/tag/candidate-v118-9f0d09e3-37840619005-1.

IMPLEMENTED/BUILD_VERIFIED/NATIVE_SCOPE_VERIFIED;PHYSICAL_PHONE/USER_VISUAL PENDING. Separate com.projectdark.mobile.v118test,new profile;existing apps/saves retained,no transfer claim. DraftPR180/unmerged vs main18ef615;broader CI37840622523/37840619014 still in_progress at closure. Source868ffc90 FAILED_CI;V117 USER_REPORTED_INTERMITTENT_STUTTER remains history,physical root UNKNOWN_NO_DEVICE_TRACE. Next:exact V118 phone extended forest movement/tapping/first attack and visual review;trace if stutter persists. Closure changes docs/evidence only;delivered runtime stays9f0d09e3.

## 2026-10-09 — V118 failed CI recovery

Remote source868ffc90989333287e5675e8089fedcf21b895f0 failed focused Actions37773204198 (101 tests,2 failures) and broad PR37773209749; no V118 APK was published. Lycan crouched attacks projected38.25px, below retained40px minimum. Shared family scale now128/192 (idle maximum48px, attacks40.875px); all12 poses keep relative geometry,192canvas,184ground anchor. Pamfet and other156 poses unchanged; crop provenance regenerated. Navigation reward assertion now accepts canonical as well as adapted registered profiles. Source review additionally found spirit instance IDs bypassed canonical rewards: admitted matching live species profiles now resolve existing canonicalEXP308950 without invented gold/drop and preserve event target/duplicate guard. Added real C-map spirit defeat/exact-once/unbound fail-closed test. No canonical Master values changed.

Same PR180/branch codex/pote-forest-v112;main18ef615 unchanged. Master integrity and diff checks pass. Exact-source CI/native/APK pending;phone stutter and visual acceptance pending. This supersedes prior45px Lycan maximum with48px maximum;logical72px canvas/collision remain unchanged.

## 2026-10-08 — V118 monster quality / intermittent frame repair candidate

V117 DEVICE_FAILED_USER_REPORTED: tap lag improved, intermittent stutter remains; Pamfet art pixelated and old monster coverage questioned. Physical root UNKNOWN_NO_DEVICE_TRACE. Confirmed code hazards: 48px sprites decoded at 24px, first-state/first-hit decode in draw, synchronous two-second disk commit, fixed16ms delay after update. Candidate full192px sprite decode, map-entry all-pose/hit preparation, vsync scheduling and periodic apply(); reward/transaction/pause commit retained. Android orders apply/commit; no asynchronous durability claim.

User authorizes sheet generation/cropping/size matching.14 visual families×12 still poses=168 frames,192RGBA and baseline184. Pamfet and Lycan silhouettes maximum120px preserve prior30/45 logical body heights; remaining species max176px within same48/72/80 logical canvas. Source sheets and exact crop/hash provenance preserved. Pamfet reference is retained current flower-bud silhouette; other supplied tree/insect/wolf/deer sheets guide reimplementation. Generated candidates are not original frames; original match and four-facing visual acceptance remain pending. First ogre-shaped Pamfet generation and incomplete tree block rejected.12 poses means one still per state/direction, not12-frame continuous animation.

A/B/C/D populations remain72/96/108/1. C additionally includes canonical POTE_SPIRIT with alternating brown/black visual variants, existing canonical stat/reward identity unchanged; spawn HP/positions remain adapted. Strong/elite registry aliases remain available using corresponding family art but outside the previously selected ordinary campaign. Full14-art-family coverage does not assert every Master variant has a unique sprite.

Tests add168 native draw/decode checks, all-map prewarm cache stability/variant reachability, real periodic loop apply-vs-commit and pause newest-state; retained save/restart/rollback, pose-size, tap oracle and gameplay checks included. IMPLEMENTED; BUILD/NATIVE/PHYSICAL_PHONE/USER_VISUAL pending until exact CI. Same draft PR180, unmerged candidate vs main18ef615.

## 2026-10-07 — V113 exact CI candidate delivered; phone / visual acceptance pending

Runtime source `d5472f7245ef430601c5bdd0d7f97f8e6ad8775b`; focused Actions [37628115881](https://github.com/ChanJoos/Project_dark_android/actions/runs/37628115881), job112815058869 SUCCESS. **47 tests, zero failures/errors/skips**; 20 native SDK34 captures including each map entry/bridge/centre/exit and same foreground tree with opaque control/player/monster/NPC. Actual opaque foliage overlap, pixel transmission and established portal pixels asserted. All production scenes reviewed; shared movement/collision, navigation, quickquest filter/manual reset, campaign/save/rewards, population and real respawn pass. Original-video asset regeneration matches all9 recorded output hashes. Final river revision softens the grey rim and breaks symmetric stone rows; water/collision geometry unchanged.

Downloaded released version113 /1.13-pote-reference-visibility APK: **55,408,708 bytes**, SHA256 `9e11aec3b06b862aaee847bf535683cd189124fcc93c625187d9df20cf457201`, built **2026-10-07 22:26:05 Asia/Seoul**. Embedded exact source/run/version, ZIP CRC, all47 source equipment files, all9 capture-derived assets/provenance, retained portal,12 Mantis poses,16 mouse frames and skill catalog match; APK v2 signature verified. APK artifact11484753928; native11484847139/archiveSHA abd298c740775de7ee36a9d607fa76b18846ea0a07541ec3a1b15e9ac3634756. [Exact verification](verification/POTE_REFERENCE_V113_BUILD.json). [Candidate APK](https://github.com/ChanJoos/Project_dark_android/releases/download/candidate-v113-d5472f72-37628115881-1/PROJECT_DARK_V113.apk).

IMPLEMENTED / BUILD_VERIFIED / NATIVE_RUNTIME_VERIFIED; PHONE / USER_VISUAL PENDING. Full push37628115950 and PR37628124882 were still IN_PROGRESS at this checkpoint; do not claim full-CI completion or main merge. PR180 remains draft and main18ef615 unchanged. Original map topology and population/respawn canon remain unresolved/adapted; Naver cafe access blocked. The source material is supplied original videos, not full original tiles/maps. Native Robolectric scenes are not physical phone evidence. Existing V112 user visual rejection remains in history. Closure is documentation-only; delivered APK source stays d5472f72.

Next: inspect exact V113 on phone (forest material/river/path, foreground tree fade for player/monster/NPC, whole-map AUTO/quest filter and respawn), finish full CI before any integration, and continue source-backed geometry refinement if visually rejected. No blanket original-perfect claim.

## 2026-10-07 — V113 original-video forest/occlusion repair candidate

V112 local280e5bb is USER_VISUAL_REJECTED by current user report (river/path/map quality and actors hidden behind trees). User explicitly authorizes GitHub publication/CI. Current repair starts from latest main18ef615; old local V112 source workspace absent, source-derived visuals and whole-map AUTO/density changes are rebuilt on current main. V113 implements original-video willow/oak, earth/flowers/current/shore material, smooth shared river collision, expanded A/B/C/D, per-actor foot-depth with actual foreground canopy alpha64, map-wide reachable AUTO and quickquest-only species filter, A72/B96/C108 populations and6–8sec adapted respawn. Approved BODY/diagonal movement/campaign/save/rewards retained. [Contract](POTE_REFERENCE_V113_CONTRACT.md). BUILD/NATIVE final evidence pending at this checkpoint; phone/user visual acceptance PENDING. Naver cafe access was blocked; supplied original videos are the primary visual evidence.


CI follow-up: first candidate `ed587523` passed all42 focused tests, but exact APK gate caught the established portal stored under AssetManager `assets/world/portal/` (ZIP `assets/assets/world/portal/`), not `world/portal/`. Renderer and APK verifier now consume the real retained path/bytes; a native portal-pixel assertion guards blank portals. Full run37625957529 caught an obsolete HudTouchAcceptanceTest radius limit; it is changed to assert remote reachable acquisition and remote unreachable rejection per current user request. Central foreground actor fixtures now explicitly assert opaque foliage overlap and include an actor-free control frame; publication/build evidence pending this follow-up.

## 2026-10-06 — V109 acceptance revision (native verified; phone pending)

V108 c48cd39d is DEVICE_FAILED_USER_REPORTED. Preserve all16 V108 animation PNG bytes and existing collision/AI timings; scale the shared inn/field display64→40px and anchor source baseline59 to actor ground. Body footprint approximately32px, smaller than player48px. Native tests must inspect production idle/walkA/walkB/attack in all4 facings and whole GameView scenes. Rescue now explicitly drives live TAUNT pursuit despite stealth/reset; its non-damaging adjacent range is ADAPTED_USER, not an original monster fact. Actual MainActivity-default regressions verify opening/growth hunts, skill quest and Rescue attacks. Exact source44d19c1e, focused Actions37408187292 SUCCESS/82 tests, including all16 native pose/geometry checks and actual taunted-monster attack. Exact release hash/identity/CRC and all16 source PNG bytes verified. Six native review images inspected; phone/visual acceptance pending. Evidence: docs/verification/DEVICE_QUEST_V109_BUILD.json. Contract: docs/DEVICE_QUEST_V109.md.

## V105 adapted campaign registration — candidate acceptance record

Six retained review families (trant,antlion,gnoll,wolf_rider,ant_giant,silver_wolf) are now registered byte-for-byte under `assets/pote/production/pote_monsters_generated_v1/sprites/campaign_v1`;72 source/runtime pairs and SHA256s are in provenance.json. Earlier historical-only exclusion below is superseded **only for these explicit candidate registrations**. Five prior runtime art families and original Master bytes remain unchanged. Strong gnoll/treant/wolfrider and adapted elite share the corresponding family art, not invented original variant art.

Unique actorID=species#spawn with admitted species reward profile; separate ADAPTED policy,240/450/800/1200zoneHP,2200eliteHP,24second respawn. SharedAI/Resolver/World lattice, no alternate damage path. Spawn selection uses connected terrain and four open neighbours,≥90spacing; gates/NPC/species paths tested. Body frames48px,gnoll/wolfrider/lycan72px,treant80px; originallycan16px collision retained and instance-aware; other current actor collision12px. Candidate idle/walk/attack timings follow existing renderer. PHONE and USER_VISUAL PENDING; this does not establish original monster art/four-facing animation acceptance. Exact source/CI evidence to follow successful build.


### 2026-10-04 Lv1–40 adapted reward profile boundary — implementation unverified

`AdaptedCampaignRewardCatalog` defines 15 noncanonical profiles, separately from `CanonicalMonsterRewardCatalog` and the three training-token test actors. `RuntimeState.enterPoteField()` attaches profiles only to the four currently spawned Pamfet IDs; the accepted `milles_mouse_proto` attaches its own profile only after the adapted inn quest creates that actor. `RpgProgressionState` first resolves canonical entries, otherwise requires a matching actor-level profile. Unprofiled `POTE_PURPLE` and unknown IDs remain pending; `POTE_SPIRIT` remains canonical only. All profile values are project balance, not source EXP/Gold claims.

New reward-boundary and restart tests are wired in CI but have NOT RUN. The local environment has no Gradle executable or wrapper; current source `db231e3dfc50d1450d9ad7daae08fe91464a171e` is local-only after public branch push was rejected by automatic review. Only four of the fourteen Pote profiles are presently reachable from the field; a catalog entry is not a playable spawn.
## 2026-10-03 V93 resolved recipient impact contract (BUILD/NATIVE VERIFIED)
Shared SkillVfxRenderer now consumes actual non-miss damaging monster HIT_FEEDBACK as recipient-only damageImpact on the live target center. It never emits an unlearned monster skill/caster effect or changes chase, wind-up, cooldown, damage, sprite bytes or BODY. Indoor mouse remains a source-still ADAPTED/B actor. New native test uses the actual mouse AI/resolver attack to verify player damage and visible onDraw effect pixels. Exact source42d20ece, Actions37122438002 SUCCESS, actual indoor mouse→player damage/visible onDraw impact native test PASS; no phone acceptance claim.

# Monster Creation and Runtime Integration Manual

This is the required checklist for adding or replacing any monster in PROJECT DARK. It records the current Pote runtime contract and separates confirmed implementation values from prototype-only values. Read it before changing monster art, roster entries, scale, collision, AI, or combat.

## 2026-10-06 Pote Lv11–41 candidate acceptance record — build pending

The local candidate distributes ordinary hunt actors A=`RED/GREEN/PURPLE/SILVER`, B=`TREANT/ANTLION/GNOLL/LYCAN`, C=`WOLFRIDER/ANTGIANT/SILVERWOLF`. Strong/elite actors and spirits are not in this route. D has one `POTE_MANTIS#0` actor using the shared adapted AI, 3200 adapted HP, infinite respawn delay, and zero direct EXP/Gold; completing D03 awards the remaining EXP and one `IT_RING_THREELINEGOLD` directly through the campaign save transaction. The ring is an adapted first-clear quest reward based on the recorded Mantis drop; it is not a ground drop and is not claimed as original mobile payout behavior. These combat values are project test values, not original-game facts. `POTE_MANTIS` has no registered production directional art and currently renders via generic placeholder presentation. The supplied 2026-09-29 screenshots (05–13) are stat/equipment/skill tables; GIFs (14–22) show player/skill effects. Neither set contains Mantis or other directional monster sprites. Do not mark its appearance accepted or claim original artwork. `MAP_POTE_04` remains an isolated old-save compatibility map. Static CSV/diff checks pass; Android tests, package inspection, device play and visual acceptance are NOT RUN because no Gradle executable/wrapper/launcher is available. Current source is the uncommitted candidate based on V110 merge `00b094c76add19d7c5fc1292365cf208882bf3a5`.

## 1. Source of truth and current asset locations

The current Pote runtime candidates are adapted generated test art, not original-game sprites or canonical monster definitions.

| Purpose | Repository path |
|---|---|
| Packaged runtime images | assets/pote/production/pote_monsters_generated_v1/sprites/<art-key>/ |
| Per-image source/hash manifest | assets/pote/production/pote_monsters_generated_v1/runtime_asset_manifest.csv |
| Review/source candidates | assets/pote/review/monster_rebuild_v1/sprites/<art-key>/ |
| Runtime identity and art registry | app/src/main/java/com/projectdark/mobile/PoteForestMonsterShowcase.java |
| Master-backed Pote roster projection | app/src/main/java/com/projectdark/mobile/PoteMonsterRoster.java |
| Sprite rendering and per-species height | app/src/main/java/com/projectdark/mobile/world/PoteFieldRenderer.java |
| Movement, occupancy and actor clearances | app/src/main/java/com/projectdark/mobile/RuntimeState.java |
| Auto-approach path and collision revalidation | app/src/main/java/com/projectdark/mobile/world/WorldRuntimeAdapter.java and WorldMoveTargetController.java |
| Monster AI and attack timing | app/src/main/java/com/projectdark/mobile/MonsterAIController.java |
| Focused regression tests | app/src/test/java/com/projectdark/mobile/PoteMonsterPlayableRuntimeTest.java |

Gradle packages assets/pote/production as the runtime asset root, so the runtime key omits assets/pote/production/. Do not copy historical review-only art into the production source set by accident.

Current runtime mappings:

| Runtime ID | Art key | Draw height | Collision radius |
|---|---|---:|---:|
| POTE_PURPLE | purple_pamfet | 48 px | 12 px |
| POTE_RED | red_pamfet | 48 px | 12 px |
| POTE_GREEN | green_pamfet | 48 px | 12 px |
| POTE_SILVER | silver_pamfet | 48 px | 12 px |
| POTE_LYCAN | lycanthrope | 72 px | 16 px |

Pamfets intentionally retain their small scale. The Lycan is intentionally drawn at player-like height. Do not enlarge every new monster to Lycan size; select scale and collision radius from its approved silhouette and anchor, then test it.

## 2. Required sprite set and file names

A playable directional monster needs exactly 12 registered stills: idle, walk, and attack, each in NW, NE, SW, and SE.

| State | Required files |
|---|---|
| Idle | idle_nw.png, idle_ne.png, idle_sw.png, idle_se.png |
| Walk | walk_nw.png, walk_ne.png, walk_sw.png, walk_se.png |
| Attack | attack_nw.png, attack_ne.png, attack_sw.png, attack_se.png |

Store all 12 beneath the same art-key directory. Keep a transparent background, pixel-preserving nearest-neighbor resizing, a shared bottom-center ground contact, and consistent scale across all directions/states. The present runtime frames are 48×48 RGBA; current candidate silhouettes are no taller than 30 alpha pixels in that frame. Keep the source artwork and hashes in the review directory and manifest.

Do not mirror a direction or silently substitute a missing pose. Do not register a species until the whole 12-pose set is present and its identity/art mapping has been reviewed. These files are one still per state/direction; they do not constitute multi-frame animation.

## 3. Registration sequence

1. Confirm the stable monster ID and name in the Master/source records. Preserve evidence labels and unresolved values; do not invent canonical stats, spawn facts, drops, or attack rules.
2. Confirm the complete image set, anchor, silhouette, direction labels, and scale. Record each source/runtime path and SHA-256 in the manifest.
3. Add the ID-to-art-key entry in PoteForestMonsterShowcase.ART. Its ID list drives the showcase placement, so verify the placement count and that each roster lookup succeeds.
4. Confirm the actor's draw height in PoteFieldRenderer and collision radius in RuntimeState.monsterCollisionRadius. Larger art needs a correspondingly reviewed collision radius; visual scaling alone does not change collision.
5. Wire only through the existing RuntimeState, MonsterAIController, RuntimeCombatSession/MonsterAutoCombatBridge, WorldRuntimeAdapter, and renderer paths. Do not add a parallel GameView damage or movement path.
6. Add focused tests before producing an APK. Keep the new monster tagged as adapted/test-only until its source and visual acceptance are established.

## 4. Movement, facing, spacing, and collision

- World movement uses the existing 64×32 isometric tile centers and four legal diagonal directions. One logical step is NW (−32,−16), NE (+32,−16), SW (−32,+16), or SE (+32,+16). Do not introduce cardinal or eight-way attacks.
- The walk image direction must come from the exact canonical committed movement start-to-target vector after collision/detour selection. Rendering must not use an older attack-facing lock or only the original AI intent. While interpolating, show WALK and keep the sprite facing the actual step. Check all four directions for each registered species; code wiring alone does not prove the generated art itself faces the labeled direction.
- Actor spacing is measured between world ground anchors. Current prototype radii are player 10 px, Pamfet 12 px, Lycan 16 px, with 5 px clearance. This yields minimum center spacing of 27 px for player/Pamfet, 31 px for player/Lycan, 29 px for Pamfet/Pamfet, and 33 px for Lycan/Pamfet.
- Check current positions and reserved in-flight destinations. Check the movement segment against other moving actors as well; an endpoint-only check is insufficient.
- Pote and Milles world adapters that contain actors must use actorsBlockMovement=true. WorldRuntimeAdapter must delegate point and whole-step collision to RuntimeState so movement planning, live step revalidation, and monster AI share the same clearance rules. Do not maintain a separate stale player/monster radius check in a renderer or UI class.
- Automatic combat may stop at a legal adjacent tile and then attack. Never move the player onto the target monster's tile to make melee range succeed. Keep approach range, canonical melee adjacency, and attack-facing derived from the same tile-center contract.
- Auto-targeting compares the shortest reachable path to a legal adjacent tile first, then straight-line distance for ties. Ignore unreachable targets and use a moving monster's reserved next tile for planning. Keep a selected living target stable while its path remains valid to prevent target/path oscillation.
- Approach path planning and each live movement step must use the same full-edge rule, including terrain sweep and actor crossing/reservations. Point occupancy alone is insufficient: a planner must not repeatedly choose a shortest edge that runtime will reject.
- Test overlapping spawn positions, two monsters reserving the same destination, crossing in-flight steps, the player entering a moving monster's destination, and revalidation when the target moves during automatic approach.

## 5. Attack pace and presentation

Current values are prototype/adapted defaults, not original server values:

| Behavior | Current default |
|---|---:|
| Monster tile step | 0.60 s per tile |
| Monster wind-up before hit | 0.24 s |
| Visible recovery after hit | 0.36 s |
| Monster attack cooldown | 1.80 s |
| Attack pose total from wind-up through recovery | about 0.60 s |

The player prototype attack cooldowns are 0.30–0.38 s for melee and 0.52 s for throw. Keep ordinary monster attacks distinctly slower than the player's: retain the 1.80 s monster cooldown unless a new, evidence-backed design is explicitly approved. Do not make monsters repeatedly strike at player speed.

Damage must still pass through the shared combat resolver at the hit frame. The attack art uses one direction-specific image and a small forward/recoil displacement; do not restart the lunge at contact, shake the sprite, snap its direction, or rotate into unsupported directions. Keep attack facing locked only for the attack presentation and never let that lock misdirect a walk pose.

## 6. Required verification before merging or APK delivery

Run the repository workflow and add or extend tests for the new ID. At minimum verify:

- Exactly 12 packaged PNGs resolve for the monster; no stale or review-only asset path is used.
- Idle, walk, and attack images load for all four directions, with shared ground anchoring and the intended per-species size.
- A real AI step moves smoothly, and the walk renderer faces the applied step even if a deliberately stale attack-facing lock is present.
- All four Pamfet direction assets are selected from each exact applied NW/NE/SW/SE step; inspect the art's actual gaze as well as the file name.
- Auto-targeting picks the reachable target with the fewest legal approach steps, ignores blocked targets, uses moving target reservations, and does not switch a still-valid target every frame.
- Player/monster and monster/monster minimum clearances hold at spawn, at destination, and during interpolation.
- The player cannot enter a moving monster's reserved destination or cross its active movement path during automatic approach.
- Monster attacks use only four diagonal facings, shared resolver damage, the wind-up/recovery timings, and the slower cooldown.
- Existing Pote spatial, water, monster presentation, Milles movement, combat, and save tests still pass.
- The APK artifact's source SHA matches the exact tested commit. Report build verification separately from physical-device verification and visual acceptance. A CI-rendered screenshot is not a substitute for device testing.

## 7. Adapted Milles mouse fixture — candidate story slice

`milles_mouse_proto` is a project-authored story fixture, not a canonical monster identity. The current Master has no mouse row or original mouse sprite. It is tagged `B`/`PROTOTYPE_PENDING`, has no canonical EXP or drops, and uses three observed mouse silhouettes cropped from the supplied 20260924_163501 recording. Original direction/action animation remains unresolved; SW reuses the observed west pose without mirroring. See assets/milles/production/interiors/v91/sources.json for hashes, masks and source coordinates. Do not describe fixture stats or behavior as original-game data.

V91 confines this fixture to milles_interior_inn after quest acceptance, uses InnMouseRenderer at 24 px width and RuntimeState collision radius 7, and routes selection, AUTO, damage, quest reward and checkpoint through the existing shared controllers. Outdoor actors are suspended/restored at the doorway. Native InnSourceRepairTest covers the actual indoor target, pursuit, defeat, Mary approach, one reward, HP checkpoint and completed restart. Physical verification and visual acceptance remain pending.

The fixture is included in the same `MonsterAIController.tickPrototypeMonster()` branch as `combat_dummy_01`. Its chase step, attack wind-up and shared-resolver submission are compared against that control in `MillesStoryQuestTest`. The fixture remains project-story-only until exact CI tests and device observation pass; the comparison cannot establish original mouse behavior.

## 8. V108 adapted mouse appearance follow-up

The user explicitly rejected the prior captured gray mouse as not recognizable. Keep the V91 recording crops and hashes unchanged as historical evidence. Production inn and early-field runtime use `assets/milles/production/interiors/v108/field_mouse_{nw,ne,sw,se}_{idle,walk_a,walk_b,attack}.png`, generated for this project and classified `PROJECT_ADAPTED_GENERATED_ART` in the sibling manifest. Each fixed 64×64 transparent frame uses the same 52×36 content projection and baseline; source sheet: `assets/milles/review/interiors/v108/field_mouse_sheet.png`. This is not an original-game sprite claim.

The renderer selects walk A/B from live `isMoving` plus `animationClock`, attack while `attackPrimed` or `attackVisualRemaining` is active, and direction from `visualFacing`. Both the inn story mouse and early field actors share this renderer. Stable actor IDs, campaign targeting/rewards, collision, and combat remain the existing adapted fixture. `MillesMousePresentationTest` checks rendered frame geometry/poses in all four directions and the actual GameView actor path; `InnSourceRepairTest` retains coordinate displacement/chase and quest turn-in coverage. Exact build, device, and user visual acceptance remain separate; the delivered V108 was reported as failed.

## 9. Current acceptance status

The 2026-09-29 versionCode 53 spacing/facing update passed GitHub Actions but was not physically verified on the user's handset. The user later reported that overlap still occurs during automatic combat. Treat the new report as DEVICE_FAILED (user-reported), keep the complete device root cause UNKNOWN until reproduced, and do not mark this manual's spacing rules DEVICE_VERIFIED merely because unit tests pass. See docs/PROJECT_STATE.md and docs/DIRECTOR_BACKLOG.md for the active follow-up. The versionCode 54 follow-up now delegates World point and whole-step actor collision to RuntimeState and blocks entry into a moving monster's reserved destination/path. Exact source HEAD 5905e963de7898acec4b84644e78fcb98cec395e passed Actions run 36570339135; APK artifact 11033662723 has SHA-256 97b892b9afd8afb82c888db1ed13305f565e3072300900e26ffbf5ec848f2fb4. This is BUILD_VERIFIED only; device verification remains pending.

### 2026-09-29 follow-up status

The user subsequently confirmed the v0.55 Pote restart/map persistence behavior. They then reported occasional Pamfet movement-art/facing mismatch and auto-attack circling or choosing a geometrically close but inefficient target. The v0.56 candidate changes are recorded in the current project state/backlog. Until its exact Actions artifact is installed and checked, do not call the changes device-verified or visually accepted.

### 2026-09-30 user-reported v0.56 failure and r4 correction

- The user reports the delivered v0.56 APK still shows occasional Pamfet movement pose/facing mismatch and auto-attack circling/poor route choice. Record this APK as `DEVICE_FAILED (user-reported)`; the user report does not independently identify the root cause.
- Source audit found the monster AI still chose a greedy direction toward the player every tile. Collision handling could substitute a side step; the next AI decision then pointed toward the player again. Replace this with a shortest legal tile route to any melee-adjacent tile, so obstacle detours are planned and stable across replanning.
- The player path planner previously sampled point occupancy along an edge, while live movement additionally rejected actor-crossing paths. Share the full `canPlayerTraverse` contract between route search and movement commit to prevent selecting a route the runtime cannot execute.
- Existing facing tests assert enum/file-name agreement, not that the generated image's gaze actually matches its direction label. Visual gaze acceptance remains pending and must not be inferred from green CI.
- r4 test requirement: construct a route where the direct tile is blocked, recalculate after each step, and prove the actor continues along a stable shortest detour without alternating directions. Retain four-direction pose tests and the real-device gaze/auto-target acceptance checks.
- Do not label a new APK as the fix until the exact-source workflow succeeds and the requested device scenario is checked. Current r4 status and exact evidence live in `docs/PROJECT_STATE.md` and `docs/DIRECTOR_BACKLOG.md`.


### 2026-10-01 V71 recipient hit feedback contract (verification pending)

GameView forwards the shared RuntimeState.Monster.hitFlash timer to the Pote sprite renderer. A cached RGB-only bitmap variant changes only occupied sprite colors on a resolved positive-damage hit; it preserves source alpha, pose/facing, dimensions and ground anchor, and never recolors a later actor or terrain draw. All five species and idle/walk/attack poses follow the same rule. The existing0.14-second timer remains authoritative; MISS, HEAL and zero-damage presentation do not start it. Existing lethal removal is unchanged; no original hurt/corpse animation is claimed. Nonlethal native HP fixtures test rendering without changing production monster stats. Exact-source CI, packaged source pixel identity and native review are required; device/visual acceptance remains pending.


### 2026-10-01 V71 automated acceptance record

Runtime623451c814a89e96df9cc486bb96da8931f689ea passed Actions36803805174/job110183675335. Exact-alpha and normal-pixel restoration checks cover all60 poses;30 real GameView job/species routes cover damage/contact/impact/tint/expiry, with heal/zero-damage protection. Native review contains3 pose grids/10 production hit scenes and confirms popup spacing. All790 packaged asset files match V70 bytes. Final cached RGB-only variants replace color filters that bled on the Lycan fractional scale; source PNGs remain unchanged. APK artifact11136812203 SHA2566f56537b66d56241b2a1e37f1e82b0d616a2edcd59502af01cdfacf5449bee32, built2026-10-01 11:05:13 KST. This verifies the added automated damage-presentation scope; device gaze/spacing and original visual acceptance remain PENDING. Exact record: docs/verification/POTE_DAMAGE_V71_BUILD.json.
## 2026-10-06 — D map Giant Mantis encounter (adapted candidate; visual pending)

`POTE_MANTIS` is now instantiated once on the new `MAP_POTE_D_BOSS` route endpoint and receives the shared prototype AI/resolver path. The fixture uses 3200 HP and zero direct combat EXP/Gold; both are ADAPTED tuning values. The first D03 turn-in grants the missing EXP up to Lv41 and one three-line gold ring directly to inventory, then D04 reports back at Piet. No strong/elite variant is used. The old saved map ID `MAP_POTE_04` remains a separate compatibility map and is not the boss arena.

The user supplied mantis row remains review/source material only. Its directional cells, runtime pose extraction, transparency and foot anchor have not passed the artwork checklist below. Until those are accepted, GameView falls back to the shared generic actor presentation and visual acceptance is pending. This candidate has not been built or tested on Android in this environment; map, quest, boss reward and legacy-map assertions were added but remain unexecuted. `git diff --check` and `tools/validate_master.py` are source checks only.

## 2026-10-09 — V123 exact reward progression verified

IMPLEMENTED / BUILD_VERIFIED / NATIVE_SCOPE_VERIFIED. Source7ea2dcd0048598d61f4bb514a8dfbb0fd995cdd8; Actions37875659441/job113643547795 SUCCESS. Independently checked both ZIP digests/CRC,35XML suites144tests,0failures/errors/skips; new reward8 tests plus all-five-job Resolver campaign, equipment/commerce/save, forest/cache/V122 regressions. Three named reward frames inspected and final hashes match reviewed frames. APK SHA256aeada750ab92a51744c961f6bbdd6585c477f80edd450a9ee19beb9b7fc41e13,59,951,091bytes,built2026-10-09T11:49:30.811449+09:00;artifact11592860835,native11592726353. Embedded source/run/version123 verified; packagecom.projectdark.mobile.v123test, separate fresh profile. All168 packaged Pote sprites match unchanged source.

New actual acquisition: M06Lv11 armor+tool; A02leggings/A03gloves/A05waterneck/B01waterbelt/B04windpair/B05forestshoes; B06Lv26 armor+tool; C01earth/C04firepairs; D03 one-time ring+10000Gold/Lv41 milestone. A/B/C questGold150/250/350; otherD500. Spirit admitted mobile800HP reward40000EXP/82Gold;308950 sourceEXP retained. Journal/NPC/receipt name equipment; shop fallback offers fourpairs/shoes; full item/Gold overflow claim refuses atomically. No new random drop rates, ground items, art or world/population/movement changes.

First run37874806749 failed two test-fixture assumptions(raw remaining EXP vs awarded total; restoring job atLv1). Production normalization/job gates were correct; assertions now exercise total earned EXP and realLv3 job selection, no runtime workaround/test removal. Evidence verification/POTE_REWARDS_V123_FIRST_FAILURE.json. Final exact144 test run passes. Main18ef615 unchanged; PR180 remains draft candidate. Broader workflows37875659453/37875663659 stillIN_PROGRESS at closure; not certified. Physical phone/process termination and user acceptance PENDING. Contract POTE_REWARDS_V123_CONTRACT.md, evidence verification/POTE_REWARDS_V123_BUILD.json. Next: exact V123 phone review; retain original rate/stat/source gaps and older visual acceptance boundaries.

## 2026-10-11 — V139 coast candidates (native verified; phone/visual pending)

Two independently source-referenced adapted families: ABEL_TURTLE and unnamed ADAPTED_COAST_RED_OCTOPUS,12 poses each,192RGBA/foot184. No Pote source pixels changed, no mirroring/reused missing directions. Scarlet octopus is not relabeled Grab/Arkpus without evidence. Original Master six-species research and exact OPEN boundaries recorded in master/source/abel_v139/research.json; generated admission does not certify original animation/stat/AI/drop fidelity. Normal shared live collision, resolver, cached RGB hit and entry prewarm retained. Current-map24 coast pairs≤60; LRU32 floor retained. Native admission verified at b57ed082e8ecd4084c39c33bfadabc4f39cc9114, focused38131552722/job114446754482:102 unique tests/21 suites,0 failures/errors/skips; all24 independent pose frames/six cameras inspected, real shared Resolver kills for five jobs, live reachable spawn tiles, source/hit max58≤60/floorLRU32/12round trips. Full38131552729 and38131554871 SUCCESS; PR194 merge 15b1790108b8aaf03d50c3844d713122ae132bb7. Phone and user visual acceptance remain PENDING. Exact APK/source/digest/build receipt master/changes/V139_BUILD_RUNTIME_RECEIPT.json; docs/ABEL_COAST_V139.md.

## 2026-10-11 — V140 reference revision acceptance record

V139 visually rejected. Crab/Pong/Berserker each12 independently generated poses,192RGBA/bottom184; source identity fidelity PENDING_REFERENCE (fewer than3 labelled refs). Crab52/Pong48/Berserker72 logical canvas; turtle48/red-octopus60 restores small original-relative silhouette. Shared adapted reward ledger,5 families maximum60 source/hit pairs, unchanged logical monster radius/AI/combat/respawn72/96/108. Source-style12props plus2capture-derived textures use shared depth/live footprint and floor LRU32. Graph/entry/gate/actor/quest/save/cache regression and native composite must pass before delivery; original stats/drop/animation and phone/user visual acceptance remain pending.

## 2026-10-11 — V140 review candidate build/native verified

IMPLEMENTED / BUILD VERIFIED / NATIVE SCOPE VERIFIED; USER VISUAL ACCEPTANCE and physical phone remain PENDING. Exact runtime source 824853992a1b96d6d30973cf93cbf59efcdb5e0c, focused Actions38139162377/job114469242568 SUCCESS. Preview11701648663 plus native11702725064 independently counted104 unique tests/21 suites,0 failures/errors/skips; first41 coast/campaign/input tests and second63 retained forest/HUD/skill/save tests. ZIP digests/CRC and APK identity/source pixels independently checked. APK artifact11702615318,64,160,460bytes,SHA256 a96be2cf580a9f4eefe5195625db7de2f9d7b34efca88fb6dfc7672ca22d3739,built2026-10-11T21:32:06.876704+09:00. Separate com.projectdark.mobile.v140test profile; Noah's actual “아벨 미리보기” dialogue opens the coast without prior-level/reward grants. Draft PR195 remains UNMERGED; broader Android38139162416/38139165608 still IN_PROGRESS at this receipt, not certified.

Five12-pose candidate families,60 source/hit pairs maximum; original population72/96/108 and floorLRU32/8,652,800pixel-byte cap survive12 round trips. Capture-derived floor/water plus12 reference-style generated prop types. Palm/banana zone12groves/5reefs; final zone4groves/12reefs; monster grid jitter and per-row species mixing remove columns; coast-only canopy fade160 retains scenery visibility. Berserker appears only as3 final-zone adapted3000HP/30s encounters with95000EXP/190Gold; original canonical numbers remain retained. Live route to every actor/NPC/gate, five-job Resolver/quest/restart,48 GameView touch callbacks verified. Robolectric touch p95=10.99168ms/max17.152433ms, not handset FPS.28 older coast+50 new bundle files,168 Pote sprite PNGs and592 unchanged skill-presentation files match actual APK bytes.

Actual GameView entry/palm/reef/deep-coast screenshots and original-reference comparison inspected and candidate files saved for user review. Three new generated species still lack three independently labelled original references; exact visual identity/poses/zone/stat/drop fidelity OPEN. Grab is not relabelled from the unnamed red octopus; KingArk original party event NOT IMPLEMENTED. Resume with candidate phone/visual feedback and original identity/source recovery. Receipt master/changes/V140_BUILD_RUNTIME_RECEIPT.json. This supersedes the earlier V140 build-pending note only for the tested scope.



## 2026-10-18 — V182 resume: user corrections and full source item master

Base6565a0f6, same branch/PR197, main41fe6def unchanged. User rejects V181 lobby exit extent/threshold alignment and monster role labels. Restrict Milles exits to u22–26,v18; west portals move to u24–26,v10; upper to19,2; east marker moves to47,2 while legal activation remains46,2 because original47,2 is blocked (full activation shift remains unresolved). Minimap title centered at68. Original MPF mapping corrects male/female Warrior↔Monk; Rogue/Mage/Cleric keep source correspondence. Raw source pixels preserved.

Recovered both supplied item xlsx sources; each contains5722 populated item rows×27columns. Only differing row4993 has display name 라→루딘의모자 in file02; later source selected, both snapshots retained. Lossless source CSV/all-cell canonical JSON verified; duplicate unique names retained via stable row IDs. This is full Master admission, NOT5722 runtime-ready/equippable/rendered items. Unknown column semantics, item identity/slot/art mappings, Horror worn equipment and article310542 source remain PENDING. Never invent stats/art or merge duplicate rows by name. Importer tools/import_user_item_master_v182.py. Receipt master/changes/V182_HORROR_AND_USER_ITEMS.json. Focused native/build and common CI pending; handset acceptance pending.


## 2026-10-18 — V183 mobile Horror policy and source-recovery work
User asks actual completion of1Horror worn equipment/stat/art/inventory,2all article310542weapon assets/motions and3mobile flow/direction/name defects.1and2NOTCOMPLETE;V182onlyadmitted5722Masterrows. Browser now successfully opened exact310542(2017-05-17앨시,[아이템]전사무기),revealed3original600pxinfoPNGand8animatedGIF;14mediareferencesincludingduplicatedheadingpreserved under master/source/items/naver_310542_20261018 with observed URLs/hashes. These are captured sources,not wearable completion. Do not repeat earlier web-tool-inaccessible claim as current browserstatus.

Implement3:all15dead automatically01→13→lobby;remove roomNPCs;roomdoor thresholds becomeMILLESreturns moved one legal adjacentcell,prefer+u,then+v/-v/-uwhen sourceblocked;no killgate for return. ChangeCampaignMap cancels old Resolver actions,clearsVFX/actionclock and stale facinglock,setsSWarrival. Remove HIT_FEEDBACK-time player reorientation;VFXdirections locked byaccepted actionsequence and primarytarget ratherthan laterrecipients. Gender/joblabels spaced andcentered;priorMPFrolecorrection retained. Exact reported unidentifiedmonstername mismatch is not claimedfullyresolved beforephonefeedback. V182common39125922172gamechecks passed but stale179APKidentity verifierfailed;updated current183identity without removing source/stat/asset checks. Newfocusedtests exercise13lastkills,alive-roomreturns/noNPC,facingreset,multiple-recipientdirection. Currentcompile/native/buildpending,notdeliveredyet;phonepending. Existinghuntingmapcache/movement/reward/save contracts retained.


## 2026-10-18 — V183 tested candidate saved; requests1/2 remain incomplete

Exact sourceeb0c979f8d30d0272df52fc20cb065a5054b1cb6,focused39132348710/job117689907697 SUCCESS. Downloaded XML independently parsed41tests/7suites/0failures/errors/skips. Includes all13automatic last-kill transitions, everyroom noNPC/live-monsterMillesreturn, transitionfacingreset, AREA multi-recipientdirection, retained SINGLE lethal moving-recipienttracking, fullHorror save/cache/sourcepose and skill/Martial regression. First fc17 and5419 both failed exactly1of41: original SINGLE release retarget was incorrectly removed; eb0c restored it with actionsequence/primarytarget/one-release guards without weakening the existing regression. Primary source/report tests are evidence of their scopes, not phone approval or fullitem readiness.

Saved PROJECT_DARK_V183_TEST.apk/com.projectdark.mobile.v183test;built2026-10-18 22:46:31KST,83634542bytes,SHA256da4df42f9048eaab97188a06c9b5b40c0e5441afdb49a43f476042c83bc4193a. Artifact12138184924/ZIP92786070bytes,SHA256588aa5b7c51762d7405b9d1dfc4f3372e783503be81d683d180fa4682e78bade. Download initially truncated82837504bytes;resumed only the missing tail via validated206/Content-Range,then fullhash/CRC/source/run/version/all10monster hashes verified. APKsignaturev2PASS. Actualroom01/03screens reviewed: noexitNPC,sourceprops,onegroundreturnportal and centeredlabels. Library save succeeded with metadata applied. Receipt master/changes/V183_BUILD_RUNTIME_RECEIPT.json;policy master/changes/V183_MOBILE_HORROR_POLICY.json. Full common39132352529 still running; no fullPASS claimed. Phone/useracceptance PENDING;PR197/main41fe6def unmerged.

Requests1and2are NOTCOMPLETE. Original310542 source info3PNG+8GIF(369frames),14mediarefs including headings preserved. Added paired originalmw069 full868x50atlas+27frame/anchor registration, allboundsPASS and existing38x13inventorycrop equals exact idle0pixels. ExistingMasterweaponWebPs are often singleidle images,NOTfullwearableatlases; do not fake missing motion by using the static crop. Fiveweapon source-row/art checks recorded in source/items/naver_310542_20261018/mapping_checks.json. Other article weapon/attribute/enhancement identities, matching original fullatlases/registrations,10Horror exact separate armor/weapon/shield kits, fullruntime/inventory/stat/equip tests remain unfinished. Color-histogram lookalikes are exploratory only and NOTidentity proof. Resume these concrete source gaps, or current fullCI result. Do not recreate/reupload the testedV183APK or redo41passing tests merely because the chat was interrupted.
