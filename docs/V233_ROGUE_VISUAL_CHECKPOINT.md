## 2026-10-22 V233 후보 전달 완료 — 도적 관련 36 native PASS

APK 소스 `bd9bf2156c00bd4dd90f71613c00bacdf8afe6f5`, run39768250080/job119788997279 SUCCESS.9suite36native PASS/0failure/error/skipped;compile/assemble PASS,source 재생성 동일. 찌르기 정상퀵슬롯→원본 대상 이펙트/임시caster베기없음,두BODY4방향동일단검키,모든15도적공격정상입력,원본flare왼쪽복원/구중심픽셀/Hide양surface,하이드진행·신규공격차단/센스원본창·방향1~2칸/AUTO.3초/표창무particle,공유contact/window/approach 및무도가6active를검증. Native 전체FX strip/암살격5단계/Hide2surface/찌르기정상장면직접검토. 기존무도가16file+기존도적25icon=41file는V232APK와바이트동일. 새Hideoverride만추가.

복원영역확장중원본옷색잔여물이관측되어최종색조건과실제원본배우영역의확정whiteglow만보존하도록정리. 원본이실제배우로가린픽셀은창작하지않음. 암살020/027.scale.52(.416대비25%확대),원본warm/white구를지우던큰actorpolygon/top46제거. 찌르기001 BODY설정은기존부터동일했고빠진FX채널만007원본연결;모션자체를별도로바꾸지않음.

PROJECT_DARK_V233_TEST.apk / com.projectdark.mobile.v233test / PROJECT DARK V233 TEST. 빌드2026-10-22 20:12:19KST,102330834bytes,SHA256 `9164089119925cf97a8b42ff8cce21f4534fd7586eabf6e4bbd92734e73348e2`. artifact12420938311/ZIPsha `44fd3818f424187720075627c76d82250951210d00a119a06b06f80c0ec58f5b`. ZIP/APK CRC/hash·내장source/run·binarypackage/label·전체변경asset일치·CI APK V2 signature확인,인증서SHA2561e397fa39206176c62e5694cd9e0172b69869997377102dc0633bdd7e6e5fa37. 전달IDlibfile_717f5066b1d48191a1fc4830325f2c3c;상세 verification/ROGUE_VISUAL_V233_BUILD.json.

IMPLEMENTED / BUILD VERIFIED / SCOPED HOST_NATIVE VERIFIED. 무도가 USER_VISUAL_ACCEPTED 유지,도적 PHONE / USER_VISUAL PENDING. 전체회귀통과아님:기존Abel5직업장비여정1건미해결. 공통39768257316은workflow예상hash갱신누락으로fullfallback실행중이며결과미주장. 이마감에서정확한reviewedworkflowhash와문서hash만갱신;local FOCUSED판정+scope5PASS. 게임바이트재빌드불필요. PR197미병합/main41fe6def. NEXT:해당V233휴대폰피드백;full통합은별도gate유지. 별도설치로기존앱/저장유지,자동저장이전없음. 이마감HEAD를APK소스로재라벨하지않음.

## 2026-10-22 V233 도적 이펙트 잘림·찌르기·하이드 재검증 중

DEVICE_FAILED_USER_REPORTED V232: 찌르기 이펙트/모션 다름, 하이드 하단 흰 여백, 도적 이펙트 일부 잘림, 암살격 작아 보임. 도적 자세는 어느 정도 괜찮고 대부분 이펙트 종류/색은 수용;무도가 전체 승인 유지. 진단:001 ROGUE_STAB 키는007과 동일하지만 원본 채널 미등록으로 임시 caster SLASH/target CUT fallback.001에007원본채널 연결. 원본 도적 flare의 좌측 x77..79 제한이 광선 절반을 제거. 원본 GIF full envelope+chroma/temporal emission으로 실제 보이는 광선만 복원. 암살020/027은scale.416유지였으나 source actor polygon/top46이 구를 구멍내고 있었음;warm/white 실제 방출픽셀을 보존하고scale.52로25%확대. 하이드 atlas88의40x40 중 하단5px가 흰 캡처 여백;상단35pxglyph만40px로정규화,기존갈색palette적용. 타아이콘/무도가/피해/ID/save불변.

Affected: rogue/captured display assets+manifest, 재현 generator, V233 별도설치 label, 원본채널 lookup은기존runtime재사용. 검사:V233찌르기 정상입력/동일body키·원본채널/광선좌측복원·구중심알파/Hide두surface;전체도적15공격정상퀵슬롯·두BODY4방향;V232하이드차단/센스방향·원본창;V231표창무particle/기존승인FX;공유presentation/contact/window/approach 및 승인무도가6active. 관련native/compile/assemble 및정확APK검증 대기. 기존Abel5직업장비여정실패는별도미해결,전체회귀주장금지. PR197미병합/main41fe6def. source inputs:master/changes/V233_ROGUE_VISUAL.json,retainedrogue_bindings/media,naver_401229twoGIF,source_iconsatlas+audit,V232sourcepose와사용자피드백. NEXT:exactCI/nativecaptures/APKprovenance.

