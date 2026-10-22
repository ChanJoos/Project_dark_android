# V226 스킬 자료 부족 목록

## 2026-10-22 V226 스킬 자료 부족 전수 감사 — 구현 전 자료 요청

기준 HEAD93f7651818fd46552ca077ca667edeb2f96bd0b1 / 마지막 APK2fc3f37a. 전체221개 현재 loader/manifest/Java 대조: 아이콘 연결179/누락42(과거CSV174/47은 skin+Meteor override 미포함), 원작 이펙트 미확인32(ADAPTED 경로, 효과 완전 부재와 다름), 기능 미지원19SERVICE+수혈TRANSFER=20. NO_PARTICLE41에는 passive/linked/utility/body-only가 포함되므로 전체 이펙트 누락으로 보고하지 않음. 12개 caster 채널 저픽셀21~26, original BODY/4방향·무기/지팡이·타이밍·원작 수치 미확정도 별도.

첨부 스킬 PDF28p 텍스트와9개 icon screenshot 직접 검토. 기존 GIF17-8은 V204 Meteor로 이미 연결;16-7은 스킬명 미확인. 사용자에게 정확 스킬별 아이콘/이름 포함 원작 영상만 요청; 기존 메테오/라그나로크/완전방어 자료 재요청 금지. 등록아이콘이 앱에서 비면 코드/표시 결함이며 자료를 다시 요구하지 않음. 원작fx 미확인32도 실제 무이펙트 기술일 수 있어 시연으로 확인하고 새 FX를 임의 창작하지 않음.

생성기 tools/audit_skills_v226.py; 결과 docs/research/SKILL_GAPS_V226.json. 221 ID 유일성/Java와JSON kind·status동일/아이콘·채널 파일존재/정확누락수/HTML전체목록 검사PASS. 저장된 사용자 전체 목록 PROJECT_DARK_SKILL_MATERIALS_V226.html(libfile_2f93e69507488191b4139d0c2c1a57ea). AUDIT ONLY, 게임코드/원본스냅샷 변경·Android 재빌드 없음. 다음: 성직자 홀리블로우/홀리쇼크/블레스/수혈, 무도가 연천단각/붕신선각/무영신공/자기보호의 명명 원작 아이콘·시연 우선 확보; 미지원 기능은 별도 구현 과제. 현재 사용자 실제 누락 스킬명/폰 증거 미확인.
