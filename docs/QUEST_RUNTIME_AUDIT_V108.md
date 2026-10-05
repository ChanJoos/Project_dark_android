# Quest runtime audit V108

## Fixed device failures

- Selecting an active monster-kill/pair quickquest now enables auto-attack before searching for an eligible target. If no target is currently available, auto-attack remains armed. Target selection is restricted to the active objective and pair objectives respect remaining wanted count.
- Warrior Shortblade “use 3 times” listens to successful skill effect events. A successful action counts even if computed damage is zero after rounding. Failed, duplicate-sequence, wrong-ability, and test-mode events do not count. Healing objectives continue to require positive applied healing.
- Follow-up for the user’s inn/field report: inn chase is asserted by actual mouse coordinate displacement through the GameView update loop; the three early Milles field actors now use the captured mouse sprites at a readable scale. Their combat IDs remain unchanged so M04 quickquest progression and rewards keep their existing route. Their visible names and quickquest objective now say “들쥐”.

## Coverage and runtime connections

Five campaign class routes contain 23 quest entries each and are exercised by CampaignProgressTest through normal resolver actions, progression and transactional reward claims. Runtime connections audited: combat KILL/PAIR events consume campaign progress; GameView successful EFFECT_APPLIED events feed SKILL objectives; map entry feeds VISIT; actual shop purchase/sale, healing and equipment actions feed SUPPLY; unique altar activations consume essence for ALTAR; normal level progression feeds LEVEL; prologue/growth systems provide the M01/M02 handoff; claim/save paths are idempotent and covered. Campaign journal, quickquest interaction, HUD hit testing and response latency have dedicated tests.

Focused Robolectric test classes passed after the change: CampaignQuickQuestIntegrationTest, CampaignProgressTest, QuestJournalTest, QuickQuestLatencyTest, HudTouchAcceptanceTest. These establish Android runtime behavior under Robolectric, not physical-device acceptance. No quest was classified as decorative solely from its label; reviewed objectives have production event sources and progression coverage.

## Acceptance status

The earlier V108 candidate (source 4e1cb570c3b352cef2251799b072d2ee5c33694f) passed campaign Actions 37309497449 and full regression Actions 37309497455. The user subsequently reported that the inn mouse did not visibly move and did not read as a mouse, and requested mice in the early field. Follow-up code/tests now cover live GameView coordinate movement and readable source-sprite rendering in the field; exact follow-up CI/APK verification is pending. Physical phone and user visual acceptance are not claimed.
