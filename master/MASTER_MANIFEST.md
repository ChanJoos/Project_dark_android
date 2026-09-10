# PROJECT DARK Master manifest

Revision M001 / D001 · 2026-09-10

이 Master는 지속 개정하는 기준 데이터다. 현재 첨부에서 확보한 92 CSV 전체를 원문 그대로 보존했다. XLSX 원본 자체는 미확보이며 이를 lossless XLSX 검증 완료라고 부르지 않는다.

## 검증 결과

- CSV 92개: 첨부 manifest의 모든 SHA-256, 행 수, 열 수, 수식 수 일치.
- 전체 행 1786개는 헤더 포함. sheet당 헤더 1행 제외 데이터 행 1694개.
- CSV 비어 있지 않은 필드 16222개. 원본 populated cell 주장 21292개. 차이 5070개는 미해결.
- 수식 문자열 47개. CSV 빈 문자열, 한글, 후행 빈 열, 행 순서 그대로 보존.
- UTF-8 strict decode 통과. 값→JSON→값 roundtrip은 runtime generator gate에서 검사.
- 명시 ID 참조 413개 검사 통과. 반복 Source_ID/Asset_ID는 외래키 반복이며 primary-key 중복으로 오판하지 않음.
- XLSX null/empty/type/style/merged-cell/formula-cache 정보는 원본 없으므로 미검증.
- master/PROJECT_DARK_MASTER_DB.tar.gz는 손상된 이전 업로드이며 사용 금지. RECONCILIATION R01 참조.

## 원본과 변환 추적

| 파일 | SHA-256 | 사용 |
|---|---|---|
| source/PROJECT_DARK_MASTER_CONVERTED.zip | 4f02ad4b0be3312ca4c87c953c9e02f9c2f980e134f1d8ad4d97971f311e72d3 | 첨부 ZIP 또는 실제 확보 DOCX 원본 |
| source/PROJECT_DARK_V0.6_PLAN_KO.docx | 28ae6bf5f5cd35044656d4cb3d2fa4b388e77f3ee14dd8de1e3644e992fc8a83 | 첨부 ZIP 또는 실제 확보 DOCX 원본 |

원본 XLSX 명칭/해시 주장은 CONVERSION_MANIFEST.json에 그대로 보존한다. 실측 원본 검증값이 아니다.

`source/ZIP → CONVERSION_MANIFEST.json → data/<sheet>.csv → tools/validate_master.py → runtime adapter/build-time projection → runtime caller`

현재 Java는 CSV를 직접 읽지 않는다. adapter 연결 전까지 runtime 소비는 PLANNED. 추후 소비 경로와 적용 revision을 여기에 갱신한다.

DOCX 원문은 source에 보존했고 전체 332 paragraph(표 내부 포함)를 design/*.fulltext.txt로 추출했다. 첨부 Markdown과 이전 Git Markdown 모두 보존. v0.7 존재 흔적은 후속 확인 대상.

## 모든 sheet

| 순번 | source sheet / CSV | 행(헤더 포함) | 열 | 비어 있지 않은 필드 | 수식 |
|---|---|---:|---:|---:|---:|
| 001 | [Item_Master](data/Item_Master.csv) | 223 | 26 | 3113 | 0 |
| 002 | [Monster_Drop](data/Monster_Drop.csv) | 16 | 26 | 128 | 0 |
| 003 | [Economy_Loop](data/Economy_Loop.csv) | 8 | 26 | 48 | 0 |
| 004 | [Element_Matrix](data/Element_Matrix.csv) | 12 | 26 | 60 | 0 |
| 005 | [Element_Presets](data/Element_Presets.csv) | 7 | 26 | 35 | 0 |
| 006 | [Sources](data/Sources.csv) | 13 | 26 | 52 | 0 |
| 007 | [README](data/README.csv) | 22 | 2 | 43 | 0 |
| 008 | [World_Master](data/World_Master.csv) | 6 | 26 | 48 | 0 |
| 009 | [Location_Points](data/Location_Points.csv) | 24 | 26 | 203 | 0 |
| 010 | [NPC_Master](data/NPC_Master.csv) | 22 | 26 | 138 | 0 |
| 011 | [Town_Services](data/Town_Services.csv) | 14 | 26 | 98 | 0 |
| 012 | [Quest_Placement](data/Quest_Placement.csv) | 13 | 26 | 111 | 0 |
| 013 | [World_Flow](data/World_Flow.csv) | 6 | 26 | 42 | 0 |
| 014 | [World_Sources](data/World_Sources.csv) | 10 | 26 | 40 | 0 |
| 015 | [Locked_Towns](data/Locked_Towns.csv) | 8 | 26 | 40 | 0 |
| 016 | [Progression_Master](data/Progression_Master.csv) | 14 | 26 | 168 | 0 |
| 017 | [Skill_Milestones](data/Skill_Milestones.csv) | 7 | 26 | 84 | 0 |
| 018 | [Continuity_Audit](data/Continuity_Audit.csv) | 16 | 26 | 64 | 0 |
| 019 | [Shop_Redistribution](data/Shop_Redistribution.csv) | 6 | 26 | 24 | 0 |
| 020 | [MASTER_DB_README](data/MASTER_DB_README.csv) | 20 | 26 | 26 | 0 |
| 021 | [DB_Sources](data/DB_Sources.csv) | 16 | 26 | 64 | 0 |
| 022 | [Skill_Master](data/Skill_Master.csv) | 185 | 28 | 3870 | 0 |
| 023 | [Skill_Requirements](data/Skill_Requirements.csv) | 185 | 26 | 777 | 0 |
| 024 | [Spawn_Master](data/Spawn_Master.csv) | 6 | 26 | 41 | 0 |
| 025 | [AUTO_Rules](data/AUTO_Rules.csv) | 10 | 26 | 50 | 0 |
| 026 | [Coverage_Audit](data/Coverage_Audit.csv) | 8 | 26 | 40 | 0 |
| 027 | [Skill_Evidence](data/Skill_Evidence.csv) | 10 | 27 | 60 | 0 |
| 028 | [Skill_Research_Audit](data/Skill_Research_Audit.csv) | 11 | 27 | 33 | 0 |
| 029 | [Skill_58_Research_Pass](data/Skill_58_Research_Pass.csv) | 59 | 6 | 354 | 0 |
| 030 | [User_Confirm_Queue](data/User_Confirm_Queue.csv) | 7 | 6 | 20 | 0 |
| 031 | [User_Confirmed_Skill_Facts](data/User_Confirmed_Skill_Facts.csv) | 7 | 5 | 35 | 0 |
| 032 | [Monster_Master](data/Monster_Master.csv) | 43 | 20 | 642 | 0 |
| 033 | [Monster_Sources](data/Monster_Sources.csv) | 13 | 20 | 65 | 0 |
| 034 | [HuntingGround_Rules](data/HuntingGround_Rules.csv) | 7 | 20 | 41 | 0 |
| 035 | [Monster_Research_Audit](data/Monster_Research_Audit.csv) | 11 | 20 | 33 | 0 |
| 036 | [Monster_Open_Queue](data/Monster_Open_Queue.csv) | 7 | 20 | 30 | 0 |
| 037 | [Horror_Room_Master](data/Horror_Room_Master.csv) | 10 | 8 | 50 | 0 |
| 038 | [Death_Village_Structure](data/Death_Village_Structure.csv) | 4 | 8 | 31 | 0 |
| 039 | [Monster_User_Facts](data/Monster_User_Facts.csv) | 9 | 8 | 45 | 0 |
| 040 | [Progression_Parameters](data/Progression_Parameters.csv) | 23 | 10 | 92 | 0 |
| 041 | [Level_EXP_Curve](data/Level_EXP_Curve.csv) | 99 | 10 | 891 | 0 |
| 042 | [Hunting_EXP_Balance](data/Hunting_EXP_Balance.csv) | 10 | 14 | 94 | 0 |
| 043 | [Horror_EXP_Model](data/Horror_EXP_Model.csv) | 20 | 10 | 99 | 0 |
| 044 | [HP_MP_Purchase_Curve](data/HP_MP_Purchase_Curve.csv) | 50 | 10 | 369 | 0 |
| 045 | [Promotion_Growth_Model](data/Promotion_Growth_Model.csv) | 10 | 10 | 48 | 0 |
| 046 | [Progression_Math_Audit](data/Progression_Math_Audit.csv) | 11 | 10 | 33 | 0 |
| 047 | [Time_Balance_Targets](data/Time_Balance_Targets.csv) | 18 | 10 | 90 | 0 |
| 048 | [Level_Time_Model](data/Level_Time_Model.csv) | 7 | 10 | 67 | 16 |
| 049 | [Post99_Time_Model](data/Post99_Time_Model.csv) | 10 | 10 | 54 | 5 |
| 050 | [PostPromotion_Time_Model](data/PostPromotion_Time_Model.csv) | 10 | 10 | 56 | 4 |
| 051 | [Progression_Time_Audit](data/Progression_Time_Audit.csv) | 10 | 10 | 48 | 3 |
| 052 | [Class_99_Profile](data/Class_99_Profile.csv) | 6 | 20 | 66 | 0 |
| 053 | [Class_STAT_Caps](data/Class_STAT_Caps.csv) | 6 | 20 | 54 | 0 |
| 054 | [Class_Promotion_Path](data/Class_Promotion_Path.csv) | 6 | 20 | 78 | 10 |
| 055 | [Class_STAT_Time_Model](data/Class_STAT_Time_Model.csv) | 6 | 20 | 120 | 0 |
| 056 | [Ultimate_Resource_Scaling](data/Ultimate_Resource_Scaling.csv) | 6 | 20 | 48 | 0 |
| 057 | [Class_Balance_Audit](data/Class_Balance_Audit.csv) | 11 | 20 | 44 | 0 |
| 058 | [Combat_Parameters](data/Combat_Parameters.csv) | 21 | 12 | 114 | 0 |
| 059 | [Skill_Damage_Model](data/Skill_Damage_Model.csv) | 8 | 12 | 73 | 0 |
| 060 | [Combat_TTK_Model](data/Combat_TTK_Model.csv) | 4 | 12 | 47 | 9 |
| 061 | [Party_Hunting_Model](data/Party_Hunting_Model.csv) | 7 | 12 | 53 | 0 |
| 062 | [Combat_Balance_Audit](data/Combat_Balance_Audit.csv) | 13 | 12 | 52 | 0 |
| 063 | [Skill_Formula_Research](data/Skill_Formula_Research.csv) | 8 | 12 | 46 | 0 |
| 064 | [AUTO_Class_Policies](data/AUTO_Class_Policies.csv) | 6 | 12 | 54 | 0 |
| 065 | [Class_Rotation_Model](data/Class_Rotation_Model.csv) | 6 | 12 | 68 | 0 |
| 066 | [Party_Rotation_Timeline](data/Party_Rotation_Timeline.csv) | 11 | 12 | 77 | 0 |
| 067 | [Class_Parity_Model](data/Class_Parity_Model.csv) | 6 | 12 | 60 | 0 |
| 068 | [AUTO_Combat_Audit](data/AUTO_Combat_Audit.csv) | 11 | 12 | 44 | 0 |
| 069 | [Content_Flow_Master](data/Content_Flow_Master.csv) | 11 | 12 | 126 | 0 |
| 070 | [Map_Instance_Master](data/Map_Instance_Master.csv) | 12 | 12 | 120 | 0 |
| 071 | [Quest_Runtime_Master](data/Quest_Runtime_Master.csv) | 10 | 12 | 116 | 0 |
| 072 | [Encounter_Master](data/Encounter_Master.csv) | 7 | 12 | 77 | 0 |
| 073 | [NPC_Runtime_Master](data/NPC_Runtime_Master.csv) | 8 | 12 | 79 | 0 |
| 074 | [Content_Gates](data/Content_Gates.csv) | 7 | 12 | 49 | 0 |
| 075 | [Vertical_Slice_Playthrough](data/Vertical_Slice_Playthrough.csv) | 16 | 12 | 112 | 0 |
| 076 | [Content_Completion_Audit](data/Content_Completion_Audit.csv) | 9 | 12 | 63 | 0 |
| 077 | [Asset_Master](data/Asset_Master.csv) | 10 | 11 | 105 | 0 |
| 078 | [Asset_Character_Mapping](data/Asset_Character_Mapping.csv) | 6 | 11 | 48 | 0 |
| 079 | [Asset_Map_Mapping](data/Asset_Map_Mapping.csv) | 11 | 11 | 82 | 0 |
| 080 | [Asset_Monster_Mapping](data/Asset_Monster_Mapping.csv) | 43 | 11 | 332 | 0 |
| 081 | [Asset_UI_Mapping](data/Asset_UI_Mapping.csv) | 7 | 11 | 42 | 0 |
| 082 | [Asset_Source_Catalog](data/Asset_Source_Catalog.csv) | 8 | 11 | 56 | 0 |
| 083 | [Asset_Gap_Audit](data/Asset_Gap_Audit.csv) | 10 | 11 | 70 | 0 |
| 084 | [Gameplay_Screen_Master](data/Gameplay_Screen_Master.csv) | 10 | 10 | 100 | 0 |
| 085 | [Screen_To_Runtime_Mapping](data/Screen_To_Runtime_Mapping.csv) | 10 | 10 | 70 | 0 |
| 086 | [Legacy_Image_Endpoints](data/Legacy_Image_Endpoints.csv) | 13 | 10 | 91 | 0 |
| 087 | [Screen_Gap_Audit](data/Screen_Gap_Audit.csv) | 9 | 10 | 63 | 0 |
| 088 | [Visual_Manifest](data/Visual_Manifest.csv) | 9 | 8 | 72 | 0 |
| 089 | [Scene_Object_Mapping](data/Scene_Object_Mapping.csv) | 8 | 7 | 56 | 0 |
| 090 | [Monster_Visual_Evidence](data/Monster_Visual_Evidence.csv) | 42 | 8 | 324 | 0 |
| 091 | [Runtime_Visual_Rules](data/Runtime_Visual_Rules.csv) | 7 | 4 | 28 | 0 |
| 092 | [Visual_Implementation_Audit](data/Visual_Implementation_Audit.csv) | 8 | 7 | 56 | 0 |

## 개정과 검증

`python tools/validate_master.py --write-report`로 재현. 기계 판독 결과와 각 수식의 셀 주소는 VALIDATION_REPORT.json. 스키마의 빈 헤더도 위치를 그대로 보존한다. 개발 개선은 changes/에 before/after/evidence/impact/validation을 적고 director가 통합한다. SOURCE 파일과 baseline을 수정하여 과거 증거를 잃지 않는다.
