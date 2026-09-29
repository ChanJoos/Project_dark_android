# Skill reference icon atlas

101 exact job/name mappings cropped from nine user-provided Naver screenshots. This is referenced artwork (V), not proof of official provenance. `source_icons.json` records each original SHA-256, bounding crop rectangle, exact source name, matching Master ID, duplicate and unmapped decisions. The original screenshots remain unchanged in the supplied project sources.

`source_icons.png` is an opaque 10-column atlas of 40px crop derivatives; these original icon backgrounds are part of the source icon, not a captured table background. Rectangles contain only icon pixels. `SkillIconCatalog` maps only registered IDs and never borrows another skill's artwork for missing entries.

Rebuild with `python tools/extract_skill_reference_icons.py --source-dir /path/to/project_sources` after generating `catalog.json`. Requires Pillow and NumPy. Unknown name variants remain unregistered. No Master IDs, combat effects, conditions or acquisition defaults are changed.


Acquisition: `acquisition_captures.json` records twelve additional martial-artist condition rows from screenshots 12/13. The user explicitly chose capture-based stat and prerequisite rules on 2026-09-30. Runtime acquisition reads these additions and the preserved legacy table; it does not mutate the Master projection. Unknown names remain unmapped.
