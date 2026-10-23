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
