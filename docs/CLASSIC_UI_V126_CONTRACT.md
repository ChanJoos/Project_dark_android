# V126 original inventory presentation contract

User report: V125 inventory had blue rounded sockets, mixed baked brown icon squares, and broken backgrounds. The user requests the original client's brown palette and inventory form, matching equipment, skill, and stat windows, original item illustrations, and separate item/background layers.

## Source and implementation

Five user screenshots are preserved under master/source/ui/classic_20261009. inventory_weapon.jpg supplies exact empty texture and border crops; classic-ui/manifest.json records boxes and hashes. The two reported V125 screenshots preserve the failure evidence.

ClassicUiSkin loads source crops once and repeats empty interior texture; only empty frame strips stretch. UiTheme provides the same brown skin to inventory, equipment, skill, and stats. Inventory uses a contiguous square grid with thin lines and first-row position numbers. Mobile capacity remains 50 cells per page, 236 owned item kinds, five pages. Existing hit targets, equipment calculation, sandbox requirements policy and saved state remain authoritative.

157 captured item illustrations retain every source RGB pixel. Separate, reviewable alpha masks remove baked socket/frame pixels, with explicit hand-traced JPEG silhouettes and holes. Raw source decodes and archive remain unchanged. Master original equipment art remains unchanged. tools/build_accessory_catalog.py regenerates both originals and separate projections deterministically; tools/verify_equipment_icons.py checks raw pixels, mask and packaged hashes. No generated or redrawn item artwork.

## Verification and limits

Local source/hash and Master checks pass. ClassicUiV126Test renders all five actual inventory pages plus equipment, skill and stats windows through native Android Canvas and tests 157 icons on two different backgrounds. Focused CI also runs existing real touch/equip/stat/skill/save tests. Exact APK verification checks source SHA, run, version 126, all UI/item assets and preserved monster assets. CI results and rendered visual review are recorded after execution. Physical phone and user visual acceptance remain pending.

Combat feel is already accepted and excluded from this visual task. No movement, combat, monster frame, save migration or item balance changes are authorized here.

Initial native review (2914d7b8, run 37919194635) passed all five pages and equipment/skills/stats. Review identified a two-pixel JPEG capture frame at the right edge of minimob_7 and header-strip repetition over the large equipment detail surface. Both are corrected: the capture edge is removed by alpha only; large surfaces use the same original empty-cell texture, while short header/button strips retain the source header texture. Final source and CI receipts follow after rerun.

## 2026-10-09 — V126 original brown UI integrated

IMPLEMENTED / BUILD_VERIFIED / RUNTIME_VERIFIED_NATIVE / AGENT_VISUAL_REVIEW_PASS; USER_VISUAL_ACCEPTANCE and PHYSICAL_PHONE_ACCEPTANCE remain PENDING. User-reported V125 mixed/embedded icon sockets recorded as DEVICE_FAILED_USER_REPORTED. Root cause reproduced from preserved source pixels and reported screenshots: captured brown item sockets were composited intact over blue rounded UI cells. V126 uses exact original screenshot texture/frame in inventory, equipment, skills and stats, with item foregrounds separated by 157 audited alpha masks and source RGB/raw archives preserved. Mobile inventory remains 236 item kinds, 50 contiguous square cells/page, 5 pages. Accepted combat feel remains DONE_EXCLUDED.

Exact runtime source 1e3df0a9605e44442e36d66a910a50344c4fd10a. Focused Actions37919855872/job113784901600 SUCCESS:8 suites42 tests,0 failures/errors/skips; actual native renders of all5 inventory pages plus equipment/skills/stats reviewed. Full Android Actions37919855921 and37919861915 SUCCESS, including wide modal input/font/contrast, equip/stats/skill/save and existing world/combat regressions. PR181 merged to main 9a0e4916970f52e9cf5d86bd81d5c21e3ebdf1ec; merge tree1dd95b86f41b45ab1626c2c4779ac41150ec3acd exactly equals the tested source tree. Native artifact11611995361 digest f3f3b349115d7196523a151f16deb54ea1b447f50687459daf3d3694b282d500; APK artifact11611780501 digest f116e8525c3b0b5daa310e9a8c8bd83993e9116e3eb5cdd8a3cc330c8de4a454. Exact separate V126 TEST APK built19:51:39 Asia/Seoul,60335063 bytes,SHA256 b9c4ae6911333c437f334b5eacd35243b738c5e1e81c69a5504b8334c52efadc,applicationId com.projectdark.mobile.v126test. Release candidate-v126-1e3df0a9-37919855872-1. Contract docs/CLASSIC_UI_V126_CONTRACT.md; receipt docs/verification/CLASSIC_UI_V126_DELIVERY.json. This completion supersedes earlier pending-CI/unmerged V126 records. No runtime changes in this documentation closure.

