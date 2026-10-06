# V109 device quest and Rescue repair

Rejected V108 source: `c48cd39d33aae4c608d122b1257518cbaf3ec828` (user device failures). This supersedes prior CI-only acceptance. IMPLEMENTED / BUILD_VERIFIED / NATIVE_RUNTIME_VERIFIED_ROBOLECTRIC. Focused Actions37408187292: all82 tests passed, zero failures/errors/skips; rejected source reproduced four assertion failures. Actual release hash/identity/CRC, original47 equipment files and all16 mouse frames verified. Six native images inspected. Full pipeline result: docs/verification/DEVICE_QUEST_V109_BUILD.json. PHONE and USER_VISUAL pending.

## Root causes and acceptance

- Opening/growth quickquest used legacy navigation which disabled AUTO. Arm AUTO for the active kill objective before routing and after inn entry. Verify town tap → inn entry → natural kill and saved growth quest → three natural kills without manually tapping AUTO.
- MainActivity starts with skill preview ON, while campaign ignored accepted skill/heal events in preview. Count accepted matched ACTION_STARTED in both modes, retain sequence deduplication and positive applied healing. Verify visible Shortblade quickslot three times, including save/restart after first use, and REPORT at three.
- Rescue TAUNT was generic status that AI did not consume. Now it removes AGGRO_RESET and forces pursuit despite stealth/reset for its existing duration; no damage/projectile. The supplied PDF establishes aggro purpose, not range. Adjacent FRONT reach1 is ADAPTED_USER_V109_NEAR_TAUNT per user's near-attack complaint, not an original range claim. Verify far input approaches before application, unchanged target HP and actual monster attack against invisible caster.
- All 16 mouse poses retain their source bytes; draw scale64→40 (37.5% smaller), common ground baseline59. Keep idle/walk/attack routing. Native pixel tests and whole-scene screenshots verify scale and animation.

## Evidence gate

`DeviceQuestRegressionTest` launches MainActivity with its real startup defaults. The workflow runs these four tests against rejected V108 and requires four assertion failures, then runs the fixed scenarios and retained campaign/UI/render regressions. APK embeds source commit/run/version109, startup toast displays version and commit. Verify all16 mouse frames, skill catalog, original47 equipment hashes and APK CRC. One workflow publishes a unique commit/run-tagged candidate; full Android workflow only uploads artifacts. Build/native verification cannot establish physical phone acceptance.

Inputs: AGENTS and director/state/backlog/decision contracts; Master manifest/source/consumption contracts; existing quest/monster/skill contracts; current user report; supplied skill PDF SHA256 ed00844bb3c4c2de58c2dd36bbce2712bf4a40d553c8f3007ba8dc04de9d5c51. Original Master/source CSVs unchanged.
