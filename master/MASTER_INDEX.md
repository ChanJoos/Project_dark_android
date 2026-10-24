## V258 — 속성표 실제 대미지·모든 몬스터 공방 속성 적용 (2026-10-24 KST)

[설계](../design/ELEMENT_SYSTEM_V258.md), [대미지 투영](../master/canonical/Element_Runtime_V258.json), [몬스터 정책](../master/canonical/Monster_Element_Policy_V258.json). V257원시표81조합/18300을 기본 유리상성 기준으로 정규화(U표+ADAPTED기존raw연결),한번반올림/양수최소1/포화. 기존무속1.0·생암1.75·목금중립 임시값 폐기. 원시13000 우선으로55%불일치 처리,원본사진/243셀전사보존. 고정피해/회복/상태/명시속성마법 계약유지. 수동/AUTO 같은CombatStatPipeline.

모든개체 생성/성공리젠에 자연4속 공격·방어 독립균등추첨;살아있는동안유지. 광산MINE_04드라코+05..09헬5종 공방DARK(100중60);나머지/다른현존맵 자연4속,특별LIFE임의추가없음. 드라코이전임시LIFE수정. Source-backed드라코두채널,헬DARK관측+두채널U정책분리. 개체필드→공유전투/센스,프로필/이름의가짜센스표시제거. 명시몬스터불/물/뿌리마법행동속성유지. schema4선택키monster_elements_v258:동일맵/ID/유효자연쌍복원,구버전/오류안전기본값. 새난수는생명주기만;cache/collision/population/art/vsync/loot원자성불변.

APK정확소스 `255a104c69cb09bfc4c760d9c0c98210502bae05`;Actions39977747900/job120496693189/artifact12512317256 SUCCESS. 최종AUTO1+strict runtime/assets guard로7ebca27c/run39976958819의38PASS유지=39; b2cac9f1/run39978518320의테스트전용무속장비교정1PASS추가=40scoped checks.37huntmaps/1522actors/60mineDARK,81sourcecells,16independentpairs,리젠/막힘,실제necklace/belt/manualAUTO/magic/sense/save/oldsave및기존자동순서·방향·시전·장비·저장 검증. actualSensePNG검토. APK106532826bytes,2026-10-24 00:26:43KST,SHA256 `d28930e9b74b5385d66cb9865b57deb66f8069e807a847bc0c1571309c6b1210`;ZIP/APKCRC+hash+embeddedsource/run+CIv2signaturePASS. IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_RUNTIME VERIFIED; PHONE/USER_ACCEPTANCE PENDING. 테스트전용b2c나이후문서HEAD를APK소스로재라벨금지.

Full39978531235:원본Sources.csv정확복원후Master보존검사+V12420체크PASS;기존approved설명vs스킬catalog생성dirtydiff에서중단. 전체회귀/main통합미완료. V257에추가했던Sources원본행은원본blob으로복원하고 `master/canonical/Sources_Elements_V257.csv`와별도provenance에보존;새근거유실없음. main41fe6def/PR197미병합후보. 기존V256HUD/AUTO·NarcoliUSER_ACCEPTED유지. 상세verification/ELEMENTS_V258_BUILD.json. 다음은정확APK의실기기속성사냥확인 또는catalog재생성통합차단해결;재검증/재빌드불필요.

## V257 — 원작 속성표 기획·마스터 등록 (2026-10-24 KST)

사용자 승인 사진1000060687.jpg(성천직자의어둠의전설/속성정리표)을 원작 기획 기준으로 등록. [기획](../design/ELEMENT_SYSTEM_V257.md), [정규JSON](../master/canonical/Element_Reference_V257.json), [CSV](../master/canonical/Element_Reference_V257.csv): 방어행×공격열9속성, 원문대미지/무속방어100%/기본4속상성100% 세 표243셀. U+FAN 출처 보존;공식O승격 없음. 기존BALANCED Element_Matrix는 구기획 참고값으로 분리. 금방어×목공격의 표2=55%와 원문대미지 환산≈53% 불일치는 원문그대로보존. 기준대미지24500/18300은 표1과의 수치대응 해석이며 보편계산식은 미확정. **등록완료, 런타임·APK변경 없음**. 후속 구현은 기준/반올림/불일치/목금ID및장비·몬스터경로 결정과 공통계산 검증이 필요하다. 이전V256AUTO/HUD검증과 Narcoli승인 상태 유지.

CURRENT AUDIT M001: 기존 tar.gz는 손상되어 사용 금지. 최신 입력은 master/data의 CSV 92개와 master/source의 첨부 ZIP입니다. master/MASTER_MANIFEST.md 및 master/RECONCILIATION.md를 먼저 읽으세요. 아래는 이전 기록입니다.

# Canonical Master Index

- `PROJECT_DARK_MASTER_DB.tar.gz`: canonical v4.4 database conversion archive
- `PROJECT_DARK_V0.6_PLAN_KO.md`: readable planning baseline
- `SOURCE_OF_TRUTH.md`: precedence and consumption rules
- `verify_master_archive.py`: checksum/sheet-count validator
- `extract_master.sh`: extraction helper
- `MASTER_DB_INFO.txt`: snapshot identity
