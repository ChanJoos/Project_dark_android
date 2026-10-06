# V98 modern mobile presentation

The user's explicit whole-game redesign supersedes V72/V74 screenshot crop shells and V89 gold utility artwork, not original icon provenance or gameplay. This is authored modern presentation, not an original-client reproduction.

## Visual system

Pretendard 1.3.9 Regular and SemiBold are bundled offline, including SIL OFL license. Source https://github.com/orioncactus/pretendard/releases/tag/v1.3.9. UiTheme owns fonts, colors, rounded layered surfaces, 1.5px line glyphs, tabs, buttons, slots and text. Charcoal/slate panels, ivory primary text, muted secondary labels, champagne emphasis and cyan selection. Body colors have >=4.5:1 contrast on solid panel surfaces. Tiny HUD labels remain compact and physical phone readability is pending.

Reference: official Black Desert Mobile remastered UI note https://www.world.blackdesertm.com/Ocean/News/Detail?boardNo=4642 — clearer menu grouping, uncluttered HUD and direct access. Interaction principles only; no third-party UI artwork is copied.

## Scope and authority

- HUD: consistent resource labels/values, level block, lightweight bars, compact quest, vector menu glyphs, single quickslot frame, numbered skills, potion glyph placeholders, attack/auto/mode controls, chat/joystick palette.
- ItemWindow: new panel shell, consistent item cells, legible filters and comparison cards, established equipment slots and approved actor preview.
- SkillWindow: larger 61px cards on 69px pitch with skill names, existing 7 job tabs/groups/learning/8 slots, continuous right pane, modern scroll indicator. Source square icons are untouched.
- QuestJournalWindow: matching dark panels/status/selection/filter hierarchy, same objectives/locked states/contextual actions.
- Stats: Korean labels, aligned values and separate allocation controls, same FinalStats data and spendStat path.
- Inventory/equipment/skills/stats modals now use the same horizontal centering offset as quests at wide aspect ratios; input subtracts that offset. Their local hit rectangles are otherwise unchanged.

## Verification

ModernUiTest renders every requested pane and both aspect ratios through the production GameView, exercises actual close routing, checks no movement/economy side effects, bundled Korean glyphs and text contrast. Existing inventory/equip/restart, all-skill catalog/learning/source pixels, quest travel/persistence, HUD/world/combat/FX tests remain enabled. Previous exact 49px skill-card assertion is superseded by the user-authorized larger-card contract; it now checks minimum touch-card width. Focused v98 PNGs and exact-source APK are archived by CI. BUILD_VERIFIED/NATIVE_RUNTIME_VERIFIED require actual successful run; phone/user visual acceptance remains separate.

## Exact local verification

Source 98e12db0c6f390684b6bb8928534af3f8d9e36a3: local assembleDebug and 66 tests in nine relevant suites PASS; 13 native PNGs captured. Equipment preview overlap fixed and wide pane touch alignment reviewed. Public CI/push blocked by automatic approval review pending explicit user authorization. Phone/user visual acceptance pending. Local APK signer differs from V97; it is not offered as an update. Proof: docs/verification/MODERN_UI_V98_BUILD.json.

## Published exact verification

User approval received. Public source72de0f03/treeb62bff9 exactly matches reviewed local bytes. Actions37168089905 SUCCESS/all configured checks; all13 final native PNGs match reviewed local output. V97 prior952 assets unchanged. Release candidate-v98-72de0f03 anonymous full APK HTTP200/hash matches CI. Version98 built10:37:27KST. CI signing differs from V97, so not an in-place upgrade; user warned to retain existing app/save. Phone/user visual acceptance pending. Full evidence MODERN_UI_V98_BUILD.json.
