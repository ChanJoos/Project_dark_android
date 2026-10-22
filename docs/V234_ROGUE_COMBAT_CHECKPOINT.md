## 2026-10-22 V234 도적 원본 색·습격·표창·피해 재검증 — 구현 완료/검증 대기

DEVICE_FAILED_USER_REPORTED V233:습격만상이,공격색동일,표창전방1~4최근접1대상,피해연결의심,암살격잔상불필요. 원문245450 cachedHTML/media 직접재검토(웹open불가);습격image23미연결→청백원본채널,공통damageimpact겹침→원본도적채널위중복제거,원본같은001/007/014magenta유지/021cyan/022violet/023gold/028white/029warmwhite/030white. 암살027warm-only가삭제하던bluephase복원. 원본캡처배우/은신윤곽은같은프레임반대편의실제FXsample을silhouette안에재배치하는명시ADAPTED복원,scale.52유지;raw원본불변. 아이콘전체·무도가불변.

표창003은현재방향직선1~4최근접단일alivevisible몬스터;선택된원거리대상우선안함/자동접근·회전·관통없음. GameView전용입력→RogueSkillPolicy→Session→Port.begin/contact/recipients재검증,LOS는공유Resolver유지. 규칙minReach2→1. 공통공간/접근의이전인접불가검사를새명시지시에맞게수정.

피해코드는기존연결확인:AbilitySnapshot→per-IDamount→defense/element→state.damage/lastDamage/ledger. 일반ROGUE계수는기존ADAPTED수치,원작확정주장금지. TestModeOFF정상퀵슬롯13공격에독립계산expected/실HP차감/표시damage/자원단일소모검사및JSONtrace추가. 표창4방향1..5칸·대각/뒤/동일칸·최근접선택/비관통·contact새전방대상/가시성/벽·정상입력무접근검사,sourcehue/bluephase/ghost없는native strip추가. 관련V233/V232/V231,전체15도적정상공격,AbilityRuntime/Spatial/Presentation/Contact/Window/Approach,무도가6active및동시성감사를실행예정. 큰GameView변경을이유로무관fullsuite강제안함. 기존Abel5직업장비여정은별도미해결. PR197미병합/main41fe6def. NEXT:exactSHA CI/native/trace검토/APK검증.
