# V99 equipment and stat presentation repair

User-rejected V98 labels/alignment supersede that visual candidate; successful tests never count as user visual acceptance. Original stat labels are STR, INT, WIS, CON, DEX. Empty equipment slots show centered part silhouettes (earring, necklace, armor, helmet, wing, weapon, shield, glove, belt, leggings, boot), never textual part names. Labels remain in contextual item information. Source item/character bytes stay untouched. Equal rails, centered head/actor/lower pair, clean readouts and continuous detail pane use one draw/input geometry. Remove redundant equipment ticks/zero bonuses.

ModernUiTest uses actual production GameView occupied, all-empty, stats and wide captures; empty-gear fixture restores the exact equipment map after prerequisites resolve before any stat screenshot. ItemWindowReferenceTest drives every real slot, equip/unequip and restart. Physical phone and user visual acceptance remain pending. V98 signing upgrade limitation remains open; this task does not claim to fix migration.

V99 first CI Actions37169633101/source48e58bee: UI/item gates passed; 78-test combined skill regression failed only with Java OutOfMemoryError constructing GameView in SkillQuickslotApproachTest. Test worker previously used default heap and accumulated native/source atlases across classes. Repair is test configuration only:1536MiB heap, one worker, fresh process per test class. No skipped/relaxed assertion, no gameplay change. Exact rerun pending.

Final source4666202a/Actions37177996173 PASS; all14 final native PNGs match reviewed local. Anonymous APK HTTP200/hash equals CI, source assets unchanged. Full proof EQUIPMENT_UI_V99_BUILD.json. Signing upgrade/physical phone/user acceptance pending.
