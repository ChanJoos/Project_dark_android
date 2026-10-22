## 2026-10-22 V236 도적 아무네지아·피해 — 구현 / 검증 대기

DEVICE_FAILED_USER_REPORTED: 도적 피해가 너무 약하고 기술별 동일. V234의 통과는 산술 연결만 확인했으며 밸런스 적합성 검증이 아니었음을 정정. 첨부214818 영상8.457초 검토:LV26,STR12/DEX17,HP8333인 캐릭터가 기존호러8만~14만HP를 공격;남은HP 기반 암살 위력감소도 관측. 원인:기술별역할대신circle/acquisition부담계수로마구/슬래쉬/기습2.852동일. 정확원작일반계수 미확정.

USER확정:아무네지아 현재방향직선1~4칸최근접1마리(표창과동일),일시비선공/그동안받는피해2배. hit시AGGRO_RESET즉시삭제버그수정;10초는기존표시project지속값(사용자정확초미지정). 방향/가시성/LOS/nearest를begin/contact재검사,접근/회전없음. firstdamage후에도기간유지,만료후공격복귀. 별도AMNESIA효과로마법사콘푸지오기존동작보존. damage권위RuntimeState에서2배적용→HP/popup/ledger/Resolver실피해동일;타깃1마리에만적용.

일반10도적기의임시계수를역할별분리·강화;원작확정값아닌PROJECT_ADAPTED_V236. 습격진은습격+10%,기습>습격2배.표창basic/암살현재HP계수·자원·강타/타직업/몬스터HP/장비/저장불변. 계약master/changes/V236_ROGUE_AMNESIA_DAMAGE.json. 새7tests:4방향1..4최근접/contact,정상quickslot,noapproach,timed비선공/2배/만료AI,실HP/ledger/피드백/사망1회,콘푸지오불변,LV26독립예상피해표. 직접영향sharedAbility/Contact/Spatial/AI/defeat/derivedresource/save검증;main최종통합full회귀별도. Scope7new+기존직접regressions,공유피해경로5audit. PhonePENDING/rootcauseconfirmedcode,handsetfixpending. PR197main41fe6def유지.

