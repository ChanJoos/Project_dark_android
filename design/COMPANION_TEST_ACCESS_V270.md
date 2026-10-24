# V270 temporary immediate companion testing

User directive: "퀘스트는 퀘스트고 난 테스트해야하니까 처음부터 임시적으로 레벨 99 모든 기술을 배운 동료를 쓸 수 있게 만들어".

The side-by-side APK exposes an explicit COMPANION_TEST_ACCESS build flag. MainActivity passes it to GameView; the ordinary GameView constructors and production build retain normal progression. This flag is independent of the player's skill-test setting and quest learning actions.

After save restoration, both implemented companions are promoted to99 and know every non-retired skill of their own profession, including advanced-circle knowledge. Existing unsupported services remain fail-closed at execution; priest AI still does not use transfusion or curse. Warrior AI selects learned upper soul/crash variants and learned triple passive applies to companion basics. Priest can select learned upper HIT/DAM buffs.

A fresh test profile immediately starts with the warrior selected. Priest selection, dismissal and recovery do not require a job, quest, level or town while this temporary flag is active. Death/current action/threat checks remain. Player level, job, experience, skill book and quests are not granted or completed by this feature. Reapplying on restart preserves HP/MP/death/cooldowns rather than refilling them. Existing99 members are not rescaled on restart.

Main package: normal flag=false. Test APK: com.projectdark.mobile.v270test / PROJECT DARK V270 TEST. The test flag can be removed when returning to normal progression; no original skill coefficients or source assets change.

Validation scope: six new Android tests cover the real MainActivity cold start, all class skills, no-quest HUD selection, restart and player-test toggle independence, field access triple passive and companion-level healing while the player stays Lv1; retain the previous37 companion/adjacent checks. No retired V268 tests are restored.
