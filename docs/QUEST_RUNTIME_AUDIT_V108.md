# Quest runtime audit V108

## Original V108 implementation (now superseded by device failure report)

- The original candidate enabled auto-attack only after routing reached the objective map. Its existing M04 test started on the same map, so it missed the travel case reported below. Follow-up moves arming to the moment the active kill/pair quickquest is tapped.
- The original Shortblade integration test invoked a private method directly, bypassing the on-screen skill slot. The quest text also said “유효 타격” instead of “사용”. Follow-up exercises the visible slot three times and updates the objective text to match the requested use count.
- The original inn/field follow-up only enlarged the source capture; that source is a blurred gray silhouette and remains visually ambiguous at runtime. Follow-up replaces its runtime rendering with four project-authored ADAPTED mouse stills while retaining the source capture as history and keeping monster IDs/rewards stable.

## 2026-10-06 — current V108 device report and repair

The user reports that the delivered V108 experience changed only the field mice and their movement; quest auto-attack, Shortblade counting, and mouse appearance still failed. Record the delivered candidate as `DEVICE_FAILED_USER_REPORTED`; the current source/device root cause is not considered closed until the follow-up APK is tested on the phone.

Reproduced source-level gap: `autoNavigateCampaign()` cleared AUTO at entry and armed it only after confirming the current map, while the passing test tapped M04 already on that map. The follow-up regression taps an active M07 kill quest from Milles and asserts AUTO is armed before travel begins. Shortblade coverage now taps the visible quickslot three times, checks the quest reaches REPORT, and claims it through CampaignProgress. New art is explicitly `PROJECT_ADAPTED_GENERATED_ART`, not an original sprite claim; the prior V91 captures remain untouched.

The all-class route suite remains automated progression coverage, not proof that every quest has been physically played on the user's phone. Follow-up build, Robolectric run, exact APK, phone behavior, and visual acceptance remain separate gates.

## Coverage and runtime connections

Five campaign class routes contain 23 quest entries each and are exercised by CampaignProgressTest through normal resolver actions, progression and transactional reward claims. Runtime connections audited: combat KILL/PAIR events consume campaign progress; GameView successful EFFECT_APPLIED events feed SKILL objectives; map entry feeds VISIT; actual shop purchase/sale, healing and equipment actions feed SUPPLY; unique altar activations consume essence for ALTAR; normal level progression feeds LEVEL; prologue/growth systems provide the M01/M02 handoff; claim/save paths are idempotent and covered. Campaign journal, quickquest interaction, HUD hit testing and response latency have dedicated tests.

The prior candidate's Robolectric suites passed, but their route-specific input coverage was insufficient: the kill quickquest started on its objective map, and Shortblade use bypassed the visible skill slot. These runs establish only the cases exercised under Robolectric. The broad five-class campaign simulation is not a claim that every quest was tested on the user's physical device.

## Acceptance status

The earlier V108 candidate (source `4e1cb570c3b352cef2251799b072d2ee5c33694f`, Actions `37309497449` and `37309497455`) is `DEVICE_FAILED_USER_REPORTED` based on the user's follow-up test. The current local follow-up has not yet been built or run. It includes immediate AUTO arming before travel, actual visible quickslot input for the three Shortblade uses, corrected use-count wording, and four new ADAPTED mouse stills. CI/build, native runtime, exact APK, phone, and visual acceptance are pending independently.
