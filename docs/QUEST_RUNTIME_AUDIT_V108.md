# Quest runtime audit V108

## Original V108 implementation (now superseded by device failure report)

- The original candidate enabled auto-attack only after routing reached the objective map. Its existing M04 test started on the same map, so it missed the travel case reported below. Follow-up moves arming to the moment the active kill/pair quickquest is tapped.
- The original Shortblade integration test invoked a private method directly, bypassing the on-screen skill slot. The quest text also said “유효 타격” instead of “사용”. Follow-up exercises the visible slot three times and updates the objective text to match the requested use count.
- The original inn/field follow-up only enlarged the source capture; that source is a blurred gray silhouette and remains visually ambiguous at runtime. Follow-up replaces its runtime rendering with four project-authored ADAPTED mouse stills while retaining the source capture as history and keeping monster IDs/rewards stable.

## 2026-10-06 — current V108 device report and repair

The user reports that the delivered V108 experience changed only the field mice and their movement; quest auto-attack, Shortblade counting, and mouse appearance still failed. Record the delivered candidate as `DEVICE_FAILED_USER_REPORTED`; the current source/device root cause is not considered closed until the follow-up APK is tested on the phone.

The delivered follow-up source `ad374ec52898cfb4f49a08cf2557258512ccc3eb` already armed AUTO before map travel and tested the visible slot, yet the user still reports the full device behavior failed. Therefore the device root cause remains UNKNOWN. The new M07 regression extends coverage from a button tap to travel-guide approach, forest entry, objective selection, monster approach, automatic combat, and one campaign-counted defeat. Shortblade now counts the accepted `ACTION_STARTED` for an actual non-healing technique; its input test confirms three separate resolved quickslot uses, immediate count, REPORT and claim. Healing tasks still require positive applied healing.

Generated mouse assets are `PROJECT_ADAPTED_GENERATED_ART`: four facings × idle/walk A/walk B/attack frames. Every projection tile has identical 64×64 frame geometry and ground anchor. `InnMouseRenderer` chooses walk frames from `isMoving`/`animationClock` and attack pose from `attackPrimed`/`attackVisualRemaining`; the inn and early field share this live path. V91 captures remain unchanged historical evidence.

The all-class route suite remains automated progression coverage, not proof that every quest has been physically played on the user's phone. Follow-up build, Robolectric run, exact APK, phone behavior, and visual acceptance remain separate gates.

## Coverage and runtime connections

Five campaign class routes contain 23 quest entries each and are exercised by CampaignProgressTest through normal resolver actions, progression and transactional reward claims. Runtime connections audited: combat KILL/PAIR events consume campaign progress; accepted GameView ACTION_STARTED events feed non-healing SKILL use objectives; healing objectives require positive EFFECT_APPLIED HP restoration; map entry feeds VISIT; actual shop purchase/sale, healing and equipment actions feed SUPPLY; unique altar activations consume essence for ALTAR; normal level progression feeds LEVEL; prologue/growth systems provide the M01/M02 handoff; claim/save paths are idempotent and covered. Campaign journal, quickquest interaction, HUD hit testing and response latency have dedicated tests.

The prior candidate's Robolectric suites passed, but their route-specific input coverage was insufficient: the kill quickquest started on its objective map, and Shortblade use bypassed the visible skill slot. These runs establish only the cases exercised under Robolectric. The broad five-class campaign simulation is not a claim that every quest was tested on the user's physical device.

## Acceptance status

The delivered V108 follow-up source `ad374ec52898cfb4f49a08cf2557258512ccc3eb` (Actions `37371178428`) is `DEVICE_FAILED_USER_REPORTED`; its device root cause remains UNKNOWN. The current local follow-up adds full M07 route-to-defeat coverage, accepted Shortblade ACTION_STARTED use accounting and 16 adapted mouse animation frames. Build/tests, exact APK, phone, and visual acceptance remain separate pending gates.
