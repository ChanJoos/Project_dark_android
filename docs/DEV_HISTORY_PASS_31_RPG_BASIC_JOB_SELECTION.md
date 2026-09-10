# PASS 31 — RPG basic job selection

- Added `BasicJobSelectionService` for Commoner -> basic-job transition.
- Supported jobs: WARRIOR / ROGUE / MAGE / CLERIC / MARTIAL_ARTIST.
- P00/G01 evidence boundary preserved: Lv10 alone is not sufficient; caller must supply explicit `BasicSkillProof.SATISFIED`.
- Starter 1-circle milestone IDs are surfaced as metadata only and are not auto-learned because `Skill_Requirements.csv` still leaves most acquisition semantics unresolved.
- Successful selection mutates `progressionNode` to BASIC_JOB and persists the selected job through the existing validated save snapshot.
- Added regression audit for level gating, proof gating, persistence, no fabricated learned skills, and single-selection behavior.
- No GameView/combat/world/renderer/NPC rendering/APK/main merge changes.
