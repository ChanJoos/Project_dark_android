# Horror Castle V207 — implementation checkpoint

Active branch codex/rucesion-coast-v157 / PR197, main41fe6def remains unmerged.

User HUDportal coordinates are applied to markers and activation cells: (0,21)..(0,25)→Milles, (9,21)..(9,25)→west corridor, (16,45)→east corridor, (18,17)→upper corridor. Native lobby cell(u,v)=(HUDy+1,19-HUDx); all12targets already source-walkable.

Rooms01..11retain raw maps. Rooms12..15use original source tile pixels with screenshot-guided ADAPTED wall/furniture layout, no legacy fallback. All-new-room original binary geometry remains unknown. Room15's clipped bed is approximated with retained upholstery/bust pixels; no exact recovered bed or full-map fidelity claim. Private screenshots retained only by hashes in master/source/horror_v207/reference.json.

15random humanoids per room; all-dead01→...→15→fresh01forever. Room doorway→Horror lobby even with living monsters. New lap records last room before clearing run HP/mask and incrementing serial, so prior deaths cannot skip the next lap. Existing13room snapshots remain accepted; bounds extended to15rooms/225HPentries/32767mask. Shared combat/rewards/movement/cache remain.

Initialde5252fe focused39444358343:26of27tests passed; the unchanged native route assertion caught the first fireplace blocking room12's native door. Correctionb75b4175 moves the native doorway to(u9,v0), between fireplaces, closes old(u6,v0), retains the route assertion. 00b6a838also repaired source carpet/wall joins before final native review.

Latest runtime candidate b75b417598201b5c94588626bc02ab9b753555b3, focused39445126813/job118704885673; SUCCESS:6JUnit suites/27tests,0failures/errors/skips; independently parsed XML, ZIPCRC/SHA, APKCRC/SHA/embeddedsource/run, all647nativePNGbytes matched. Five actual captures(room12..15/lobby)reviewed. APK PROJECT_DARK_V207_TEST.apk,86633757bytes,built2026-10-21T00:08:29.322262+09:00,SHA256310d6f03c9fc6556b3c2ecae9bffc0cb1fc0c6c5ce5c0ee4f27c115e7b0a877e; artifact12277035459. Receipt verification/HORROR_LOOP_V207_BUILD.json. Prepared saving failed403beforeupload;localAPKandGitHubartifactavailable. Failed predecessors are superseded, not delivered. Full common CI and physical handset/user visual acceptance remain separate.
