## V248 — 원본 동적 포탈·시약/은행/이동 NPC (CI 진행)

사용자 첨부5사진+영상1000060741 기준. Hades mns035(시약),028(죽음상점기사),056(은행),019(백원만) 원본MPF/PAL 복원. Roh efct362.efa 전체32프레임38x120·125ms·pivot19,110·ADD 보존. 스크린샷 오른쪽선택창/portrait 프레임 RGB 그대로 추출, 내용부만투명화. master/source/town_v248/receipt.json 및rawzip 보관; tools/check_town_v248.py 전체pixel PASS.

활성/복원지원71맵의기존130입구그룹에공통원본애니메이션;지형캐시밖공통liveoverlay로1회draw,583680byte공유atlas,매프레임decode없음. 현재/신규마을시약·은행kind공통원본NPC. 실제모든기존이동NPC(6마을manager+2해안keeper) 백원만·우측선택창통일. 죽음대기실기사상점시약/리콜실제구매. 이동NPC100골드단일차감+원자save,실패시원래골드·살아있는actor/scene복원. 취소/잠금/잔액부족무과금,기존리콜/포탈무료·해금정책/밀레스46,39보존. legacyHorrorkeeper호환만유지,현재미배치NPC새로추가하지않음.

검증예정: 신규6native(전체71맵·130그룹·20역할pixel·21마을경로·해안잠금·취소·부족·rollback·죽음상점/리콜/재시작), 직접영향AllMapLabels/NpcAppearance/TownInterior/TownNavigation/F5mRestartPersistenceMatrix/ForestFloorCache/GroundTapPath. 로컬production·신규testcompile/sourceaudit PASS. CI·native화면검수·APK pending; PHONE/USER_VISUAL PENDING. SamePR197/branch,main41fe6def unmerged. 최종main통합은별도fullgate.

