# Skill artwork and mobile learning policy — v60

136 ID mappings from eleven user-provided screenshots are registered in source_icons.json with original hashes, rectangles and mapping decisions (135 direct class/name matches plus the shared 쿠로토 entry). The original screenshot bytes are preserved; the atlas contains cropped presentation copies. Native SkillIconCatalog masks cyan capture-background corner pixels and clips the presentation to rounded icon bounds; unregistered IDs use a neutral placeholder.

Rebuild the source atlas with tools/extract_skill_reference_icons.py --source-dir /path/to/project_sources. No unknown name borrows another skill image. Screenshot evidence is V, not proof of official provenance.

catalog.json is the unchanged read-only 219-row Master projection. acquisition_captures.json remains source evidence, including historical prerequisites. Latest user direction supersedes those prerequisite/mastery/job/circle gates. tools/generate_mobile_skill_policy.py creates the separate mobile_learning.json: final stats plus explicit Gold/material costs, readable summaries and PROJECT_ADAPTED_V60 evidence. Captured stats are reused, unknown stats use project tiers. The Gold tier schedule and reagent recipes are project balance, not original-server data. See docs/SKILL_ACQUISITION_CONTRACT.md for atomic learning/save, eight slots and supported combat limits.
