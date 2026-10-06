# V107 reference equipment and quick quest repair

User reports V106 carry presentation incorrect and quick-quest touches freeze for approximately3s: DEVICE_FAILED_USER_REPORTED. Phone reproduction is unavailable; expensive planner work is reproduced locally.

Consumed inputs: AGENTS.md; docs/DIRECTOR_GUIDE.md; docs/DIRECTOR_BACKLOG.md; docs/CHAT_HANDOFF.md; docs/PROJECT_STATE.md; docs/DECISION_LOG.md; docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md; master/REPOSITORY_MASTER_CONTRACT.md; master/DATA_CONSUMPTION_CONTRACT.md; master/MASTER_MANIFEST.md; master/source/PROVENANCE.md; master/data/Asset_Master.csv rows mu0000180/mh168; retained master/source/weapons/chungryong_490398/basic.gif; app/src/main/assets/weapons/chungryong/manifest.json; app/src/main/assets/source-registration/mm001.json; new public full-sprite manifests and atlases in master/source/reference_warrior_v107. The newly supplied1000058541 screenshot is the visual standing reference; raw private screenshot is not committed.

- Preserve accepted standing/walk BODY01 and its scale, camera, movement and source bytes. The basic source blade is registered on the actual per-frame hand; never switch to the opposite arm at walking column2 or SW column4. Remove back-view recovery's artificial5-source-pixel shove. Dedicated20-frame table avoids changing accepted other weapons.
- Add reference-matching 레오파드(mu0000180) and 헬름(mh168) as inventory-only test wearables. Level1/all-job availability and zero extra stats are ADAPTED for the user's explicit early visual testing, not historical gameplay stats. Preserve original full sprites,01/c source registration and full-body coverage.
- New characters own one each. Valid existing saves receive missing ones exactly once; preserve existing equipment/quantities/currency/progression. Invalid saves remain unchanged and do not set the grant marker. Later removal does not regrant.
- Entity approaches use one multi-goal A*; choose the reachable tile with the fewest legal diagonal steps, then entity distance, then authored order. Use a consistent diagonal lower bound. Occupancy cache exists only during a single request; actual movement revalidates terrain and actors. Monster cost queries use the same planner and retain the adjacency contract.

Local independent navigation check:100 seeded obstacle maps match exhaustive BFS goal and step counts. An unreachable80x80 authored-plane NPC request drops occupancy calls238857→3371 (local JVM103.7ms→5.63ms; synthetic comparison, not phone latency). Full Java source compiled with Android35 android.jar and temporary resource-ID stub; Gradle/native tests and APK are pending at creation of this record.

Public GitHub push is BLOCKED_BY_AUTO_REVIEW. Two ordinary non-force pushes were rejected because the reviewer requires current trusted user-authored approval for this code publication, and does not accept retrieved previous approval as authorization. No connector or indirect publication attempted after that decision. Continue local verification/build; request scoped publication approval only after reviewable results exist.

Physical phone and user visual acceptance remain PENDING. Build/native evidence belongs in verification/V107_LOCAL_BUILD.json after it actually succeeds.

### V107 local verification completed — 2026-10-05 Asia/Seoul

IMPLEMENTED / BUILD VERIFIED / NATIVE RUNTIME VERIFIED (Robolectric SDK34): exact source c99345776c5633843bf80fa80e1e6eabfee7c226; local Gradle8.11.1/Android35/JDK17, no Actions run/artifact. Related27 tests pass: Chungryong9, quest journal10, reference equipment3, path3, latency1, production route1. Native20-frame carry and20-frame reference outfit sheets inspected. Real GameView DOWN+UP callback timing45/6/3/3/3/4ms is local JVM evidence only.

APK built2026-10-05T20:13:53.399912+09:00, version107, SHA25669c8c90d55138991f2eda88dd9c011d0188cd140a999b8dca2d45cb97cb5bd8d. Preserved47 recovered source files and4 new reference sprite/manifest entries match source bytes in actual APK; v2 signature verifies. Local candidate signature differs from V106, so in-place update is NOT supported; do not instruct removal of the existing app or risk its save. Update-compatible CI delivery requires authorized GitHub publication.

Publication remains BLOCKED_BY_AUTO_REVIEW; no bypass. Physical phone and user VISUAL ACCEPTED remain PENDING. Earlier pending local build notes are superseded by verification/V107_LOCAL_BUILD.json. Resume: obtain scoped current push approval, publish this tested branch without force, build with existing CI signing, verify exact resulting artifact and then request device feedback.
