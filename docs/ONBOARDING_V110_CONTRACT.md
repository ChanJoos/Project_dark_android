# V110 guided Lv1–11 curriculum

User explicitly accepted V109 device behavior, then requested separate, taught essentials, spread EXP to Lv11, and a usable quest journal. V109 combat/AUTO/Shortblade/Rescue/mouse acceptance is DEVICE_VERIFIED_USER_REPORTED for the reported scenarios. V110 remains IMPLEMENTED_PENDING_VERIFICATION until exact CI/native/APK evidence is appended.

All added curriculum, rewards, stories and controls are PROJECT_ADAPTED_V110. Original Master CSVs, skill requirements, EXP thresholds, sprites and save ownership are preserved.

## Fresh-character order

|Step|Quest|Actual completion|Cumulative EXP milestone|
|---|---|---|---|
|1|M01 여관의 소란|Existing real mouse defeat and Benjamin report|10000 including kill|
|2|M02 성장 훈련|Existing three field defeats and James report|25300 including kills|
|3|M03 나의 첫 직업|Michael job choice and report|30000|
|4|T01 나에게 맞는 능력치|Successful point allocation|39300 / Lv4|
|5|T02 첫 기술 배우기|Actual learned record, not preview availability|48000|
|6|T03 기술을 손끝에|Owned beginner skill in a real quickslot|60500 / Lv5|
|7|J01 직업별 첫 기술 실습|Three accepted matching actions; actual positive HP restoration for Cleric|78800|
|8|T04 첫 장비 구매|Successful purchase of IT_SHOES|87800 / Lv6|
|9|T05 신발 장착하기|Successful explicit shoe equip|105000|
|10|T06 회복 물약 구매|Successful purchase of small HP potion|122800 / Lv7|
|11|T07 다친 몸 돌보기|Actual HP restoration by this potion|145000|
|12|T08 마력 물약 구매|Successful purchase of MP potion|167800 / Lv8|
|13|T09 퀵슬롯으로 회복|Successful field quickslot potion restoration: MP for Mage/Cleric, HP for physical jobs|195000|
|14|T10 남는 물품 판매|Successful MP potion sale|225600 / Lv9|
|15|M04 자동 사냥 익히기|Three real target field-mouse defeats|300000 / Lv10|
|16|M06 포테 숲길 답사|Actual forest map visit and report|378000 / Lv11|

Turn-in grants max(0, milestone − total normal EXP already earned); it never sets a level, removes EXP, or lowers an overlevelled character. Display/reward toast quote the actual current amount. Extra early combat reduces the catch-up reward; levels already beyond the milestone remain. Original LevelExpCurve is unchanged. No filler hunting gate before Lv11. Every T quest has one action, not a composite boolean supply objective. T rewards do not flood inventory with HP/MP potion stacks. Gold budgets retain affordably paid first learning (50G), shoes (100G), HP20/MP25 and sale proceeds.

## Manual learning and teaching

Fresh job choice gives equipment/supply, not automatic learned/slot ownership. T02 opens matching-job beginner skill, conditions page, and actual ownership mode. Preview mode is not a learned record. T03 opens matching skill registration. First Cleric practice is actual basic heal SK_성직자_001, whose original mobile policy is attainable at initial stats; former high-stat HolyBolt013 is retained in imported V109 practice. No stats or acquisition prices are bypassed to learn a new starter. Later J02/J03 existing mentor skill awards remain adapted campaign rewards; not claimed paid learning.

Guide covers STR/INT/DEX and CON/WIS growth timing, available points and skill conditions; conditions/Gold; explicit 습득; 등록 then eight slots; closing the book and actual field use; product/quantity/amount confirmation; purchase vs equip; HP/MP full rejection; equipped sale rejection. Physical starter techniques consume zero MP, so their T09 uses HP instead of imposing impossible MP spending. Inventory use does not fake a quickslot-use event. Accepted V109 skill counting stays compatible with default preview in imported campaigns.

T tutorials accept and explicitly claim through their own current journal/quickquest action, with durable transactions; no repeated cross-town NPC reporting for ten UI lessons. Existing story/combat quests retain NPC acceptance/report. Purchase/sale guide uses actual walking through the door and counter approach, then selects the right product/page/tab; no teleport or automatic purchase. Wrong-item/no-effect/failed-save actions do not count. Skill slot save restores campaign state if persistence fails.

## Journal and compatibility

Only the player's ordered playable route appears; no foreign class quests, duplicate forest/job cards or unimplemented source placeholders in the runtime list. Original catalogs remain available as source data. Clear chapters, specific previous-quest lock reason, actual current objective, reward and next title replace generic text. Current row is selected and scrolled into view. Separate scrollable 방법 보기 describes exact controls. Larger 290×62 quickquest shows title/state/action/count. Journal action dispatches the selected current row; it cannot silently navigate a different row. Modal touches/swipes block world movement; wide centering remains.

Campaign snapshots advance to v2 and preserve all v1 IDs/counts/completion/resources. Existing v1 progress past M03 imports the original route (legacy flag persists), avoiding inserted compulsory lessons, duplicate rewards, changed progress or restarting a user's Lv11 character. Early v1 saves before job completion use the new curriculum. Imported M05's original composite objective remains only on that old route; fresh route replaces it entirely. Existing class skill/slot ownership stays intact. Completion is not fabricated to award new tutorial rewards.

## Verification gates

CampaignProgressTest: normal real Resolver/resources/paid starter learning for all five jobs, 16 essentials reach exactly Lv11/378000 without filler grind, retained 32-quest normal Lv40 route, checkpoint restores/idempotent rewards. OnboardingUiTest: real stat/learn/register controls in all five jobs; physical shoe-shop walk/product/confirmation/payment/count/restart; wrong item and failed purchase/no-effect HP rejection; V109 Lv11 active count/equipment/EXP preservation; own-route/focus/method-scroll journal. V109 native actual default-mode AUTO/Shortblade/Rescue/mouse tests remain. New task phone and user visual acceptance remain pending until user tests the exact new candidate.
