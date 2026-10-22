## 2026-10-22 V233 도적 이펙트 잘림·찌르기·하이드 재검증 중

DEVICE_FAILED_USER_REPORTED V232: 찌르기 이펙트/모션 다름, 하이드 하단 흰 여백, 도적 이펙트 일부 잘림, 암살격 작아 보임. 도적 자세는 어느 정도 괜찮고 대부분 이펙트 종류/색은 수용;무도가 전체 승인 유지. 진단:001 ROGUE_STAB 키는007과 동일하지만 원본 채널 미등록으로 임시 caster SLASH/target CUT fallback.001에007원본채널 연결. 원본 도적 flare의 좌측 x77..79 제한이 광선 절반을 제거. 원본 GIF full envelope+chroma/temporal emission으로 실제 보이는 광선만 복원. 암살020/027은scale.416유지였으나 source actor polygon/top46이 구를 구멍내고 있었음;warm/white 실제 방출픽셀을 보존하고scale.52로25%확대. 하이드 atlas88의40x40 중 하단5px가 흰 캡처 여백;상단35pxglyph만40px로정규화,기존갈색palette적용. 타아이콘/무도가/피해/ID/save불변.

Affected: rogue/captured display assets+manifest, 재현 generator, V233 별도설치 label, 원본채널 lookup은기존runtime재사용. 검사:V233찌르기 정상입력/동일body키·원본채널/광선좌측복원·구중심알파/Hide두surface;전체도적15공격정상퀵슬롯·두BODY4방향;V232하이드차단/센스방향·원본창;V231표창무particle/기존승인FX;공유presentation/contact/window/approach 및 승인무도가6active. 관련native/compile/assemble 및정확APK검증 대기. 기존Abel5직업장비여정실패는별도미해결,전체회귀주장금지. PR197미병합/main41fe6def. source inputs:master/changes/V233_ROGUE_VISUAL.json,retainedrogue_bindings/media,naver_401229twoGIF,source_iconsatlas+audit,V232sourcepose와사용자피드백. NEXT:exactCI/nativecaptures/APKprovenance.

