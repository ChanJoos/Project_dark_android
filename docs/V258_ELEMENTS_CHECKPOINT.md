## V258 — 속성표 실제 대미지·모든 몬스터 공방 속성 적용 (2026-10-24 KST)

[설계](../design/ELEMENT_SYSTEM_V258.md), [대미지 투영](../master/canonical/Element_Runtime_V258.json), [몬스터 정책](../master/canonical/Monster_Element_Policy_V258.json). V257원시표81조합/18300을 기본 유리상성 기준으로 정규화(U표+ADAPTED기존raw연결),한번반올림/양수최소1/포화. 기존무속1.0·생암1.75·목금중립 임시값 폐기. 원시13000 우선으로55%불일치 처리,원본사진/243셀전사보존. 고정피해/회복/상태/명시속성마법 계약유지. 수동/AUTO 같은CombatStatPipeline.

모든개체 생성/성공리젠에 자연4속 공격·방어 독립균등추첨;살아있는동안유지. 광산MINE_04드라코+05..09헬5종 공방DARK(100중60);나머지/다른현존맵 자연4속,특별LIFE임의추가없음. 드라코이전임시LIFE수정. Source-backed드라코두채널,헬DARK관측+두채널U정책분리. 개체필드→공유전투/센스,프로필/이름의가짜센스표시제거. 명시몬스터불/물/뿌리마법행동속성유지. schema4선택키monster_elements_v258:동일맵/ID/유효자연쌍복원,구버전/오류안전기본값. 새난수는생명주기만;cache/collision/population/art/vsync/loot원자성불변.

APK정확소스 `255a104c69cb09bfc4c760d9c0c98210502bae05`;Actions39977747900/job120496693189/artifact12512317256 SUCCESS. 최종AUTO1+strict runtime/assets guard로7ebca27c/run39976958819의38PASS유지=39; b2cac9f1/run39978518320의테스트전용무속장비교정1PASS추가=40scoped checks.37huntmaps/1522actors/60mineDARK,81sourcecells,16independentpairs,리젠/막힘,실제necklace/belt/manualAUTO/magic/sense/save/oldsave및기존자동순서·방향·시전·장비·저장 검증. actualSensePNG검토. APK106532826bytes,2026-10-24 00:26:43KST,SHA256 `d28930e9b74b5385d66cb9865b57deb66f8069e807a847bc0c1571309c6b1210`;ZIP/APKCRC+hash+embeddedsource/run+CIv2signaturePASS. IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_RUNTIME VERIFIED; PHONE/USER_ACCEPTANCE PENDING. 테스트전용b2c나이후문서HEAD를APK소스로재라벨금지.

Full39978531235:원본Sources.csv정확복원후Master보존검사+V12420체크PASS;기존approved설명vs스킬catalog생성dirtydiff에서중단. 전체회귀/main통합미완료. V257에추가했던Sources원본행은원본blob으로복원하고 `master/canonical/Sources_Elements_V257.csv`와별도provenance에보존;새근거유실없음. main41fe6def/PR197미병합후보. 기존V256HUD/AUTO·NarcoliUSER_ACCEPTED유지. 상세verification/ELEMENTS_V258_BUILD.json. 다음은정확APK의실기기속성사냥확인 또는catalog재생성통합차단해결;재검증/재빌드불필요.


Saved APK libfile_fdb4474182a4819191038e4bea8b7540; preview libfile_1281236edfc88191b0345c9bf1ba8cf1; local identity metadata applied. Device scenario: sense current pair, switch necklace, compare normal/same/advantage damage; swap belt and compare incoming; allow normal regen and restart; verify mineDraco/hellDARK vs othernatural. No claim original uniform spawn probabilities or action raw formula independently recovered.
