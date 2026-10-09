# V126 original inventory presentation contract

User report: V125 inventory had blue rounded sockets, mixed baked brown icon squares, and broken backgrounds. The user requests the original client's brown palette and inventory form, matching equipment, skill, and stat windows, original item illustrations, and separate item/background layers.

## Source and implementation

Five user screenshots are preserved under master/source/ui/classic_20261009. inventory_weapon.jpg supplies exact empty texture and border crops; classic-ui/manifest.json records boxes and hashes. The two reported V125 screenshots preserve the failure evidence.

ClassicUiSkin loads source crops once and repeats empty interior texture; only empty frame strips stretch. UiTheme provides the same brown skin to inventory, equipment, skill, and stats. Inventory uses a contiguous square grid with thin lines and first-row position numbers. Mobile capacity remains 50 cells per page, 236 owned item kinds, five pages. Existing hit targets, equipment calculation, sandbox requirements policy and saved state remain authoritative.

157 captured item illustrations retain every source RGB pixel. Separate, reviewable alpha masks remove baked socket/frame pixels, with explicit hand-traced JPEG silhouettes and holes. Raw source decodes and archive remain unchanged. Master original equipment art remains unchanged. tools/build_accessory_catalog.py regenerates both originals and separate projections deterministically; tools/verify_equipment_icons.py checks raw pixels, mask and packaged hashes. No generated or redrawn item artwork.

## Verification and limits

Local source/hash and Master checks pass. ClassicUiV126Test renders all five actual inventory pages plus equipment, skill and stats windows through native Android Canvas and tests 157 icons on two different backgrounds. Focused CI also runs existing real touch/equip/stat/skill/save tests. Exact APK verification checks source SHA, run, version 126, all UI/item assets and preserved monster assets. CI results and rendered visual review are recorded after execution. Physical phone and user visual acceptance remain pending.

Combat feel is already accepted and excluded from this visual task. No movement, combat, monster frame, save migration or item balance changes are authorized here.
