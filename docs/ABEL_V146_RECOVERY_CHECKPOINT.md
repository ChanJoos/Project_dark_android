# V146 Abel repair checkpoint

User explicitly approved GitHub publication 2026-10-12 KST. Continue existing PR195/codex/abel-coast-v139.

V144 and V145 visuals USER_REJECTED: floor/road/water tiling and bench legs. Apply Milles V102/V103 ground-anchor/full-support/whole-scene lessons. Temporary workspace reset to remote ddcba705; unpublished V144/V145 source commits absent. V145 APK recovered, SHA256 aa1fabd058c3f66dcf549302afe93b433e4935e5ef1b9ede06268a3a2109fab1;22 town PNGs and manifest restored. Reconstruct integration from APK plus retained source records. Do not claim exact earlier source recovery. No V146 build/native/visual acceptance yet.

Next: restore Geometry/Renderer/integration, fix continuous source-scale materials/water rim/whole bench supports, native full map/district/feet/door/route/save/cache checks, exact APK receipt. IN PROGRESS; no fresh collection/repeating historical audits.

## Current checkpoint: source integration restored, V146 repair implemented (verification pending)

AbelTownGeometry recovered from delivered APK; compiler lambda reconstructed explicitly. Renderer recreated from APK and retained source records; this is not the original lost commit. GameView/RuntimeState/CampaignWorld/map definition/interior identities restored against current V143 source; player/skills/equipment/HUD/forest/combat unchanged. Original recovered town PNG bytes retained. Four bench views now render a full adapted wood/iron structure in Canvas, with four support legs, feet and transparent ground gutter. No damaged cutout/grass color-key used. Ground foot registration is frontmost support, not seat or image centre. Roads are world-phased pebbles; stone paving uses coherent128x64 projection; water uses quiet world-phased ripples and7px coping; no repeated paving-derived water bitmap. This is source-referenced ADAPTED art, not a recovered original tile/sprite claim.

Focused native suite includes actual four-view supports/ground gutter;20 districts and full native scenery;5 door input/interior exits/restart;12 coast round trips;save/progress/cache. Existing normal Noah input now enters town; general navigation/punch checks retained. Local build is running; CI workflow V146 includes new tests. BUILD/NATIVE/PHONE/VISUAL remain PENDING until evidence. Continue from published source, do not repeat recovery.

## Native visual iteration

12 focused tests/3 suites passed on app source corresponding to9a588f4. First native review rejected the new procedural oval-dot road and overly plain complete benches; no candidate was delivered. Remove oval road grid: retain source pebble material with reduced contrast. Large slab tint is translucent over retained original stone grain; grout is subdued. Front two bench facings explicitly reuse MillesSourceStyle.completeBench V103 (same accepted support/alpha/anchor path, mirrored same asset); rear two use adapted complete supports/rounded arm frames. These are2 source families/4 views, not4 original recovered sprites. Revised whole/district/feet render verification running.

## Latest verified native scope

Final repaired town suite4 tests PASS (SDK34 NATIVE);20 districts,5 door/interior captures and4 bench renders plus whole scenery reviewed. Prior12 focused checks passed; retained navigation/punch8 tests are unchanged outside renderer. Native first oval-road/vector-front candidate was rejected locally and never delivered. Final town pass uses source pebble/stone grain and Milles V103 frontal bench pipeline; no isolated grain scatter/oval-dot roads. Exact published-source APK stamp/package/signature/content comparison pending final assembly; PHONE and USER_VISUAL remain PENDING.

## 2026-10-12 — V146 continued from saved source; exact CI candidate verified

Continue same draft PR195/codex/abel-coast-v139. Runtime/test source2c60e33efe8c12671e08f26af59f2fe3d9857f05; focused Actions38234421107/job114761473028 SUCCESS. Independently downloaded APK/native/preview ZIPs, checked CRC/hashes and113 unique native tests in23 suites, zero failures/errors/skips. Local18 Abel checks plus63 retained regressions also PASS;35 native town captures are byte-identical to CI. Source/output split-line error in focused workflow fixed; stale preview test now checks Noah→Abel town→coast without granting level/EXP/D04 completion. Runtime artwork/source bytes unchanged from75ef65d; no regenerated forest/player assets.

Exact delivered candidate: com.projectdark.mobile.v146test / version146; built2026-10-12T19:16:18.936651+09:00;68284217bytes;SHA256c7512beb13e57245a2447b364087144b69aa843683c1e1c4fb7b7d3baa96f34f;APKartifact11740328179/native11740630264/preview11740052865. Embedded source/run/package identity/signature and1027 retained town/skill/Pote files plus47 restored equipment files verified. Receipt master/changes/V146_BUILD_RUNTIME_RECEIPT.json. Attachment23-file paths recovered and source hashes matched. Whole map/20 districts/4 bench-support views/5 service doors,interior exits and restart/12 town-coast round trips reviewed/tested.

IMPLEMENTED / BUILD VERIFIED / NATIVE SCOPE VERIFIED. Broader Android38234421182/38234425340 still IN_PROGRESS_UNCERTIFIED at receipt. Draft branch remains unmerged, main deed7e52 unchanged. PHONE and USER_VISUAL_ACCEPTANCE PENDING; original whole-town coordinates/building identities/interior correspondence and original fidelity remain OPEN. Rear benches and quiet water are adapted; do not call them original recovered sprites. Existing test package V146 is a separate profile. Next: physical-phone/user visual review of this exact candidate, then address concrete remaining visual issues without redoing recovery or accepted player/forest work.
