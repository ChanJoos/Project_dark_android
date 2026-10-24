# V266 requested inventory and HUD polish

User: attack slightly right; rename reagent tab to consumption and include reagents/food/recalls; recall background wrong; center all equipped necklaces horizontally and vertically.

Only attack anchor moves10 logicalpx on wide2340x1080 (20physicalpx). Preserve AUTO/grid positions. At960x540 right safe bound2px limits movement6px. Shared draw/two input/world blocker use attackHudX. No direction/combat behavior change.

ItemWindow tab10 label 소비; reagent/food/recall identifiers and known usable recovery items map here. Existing ownership/grants/use/save unaffected. All54 source foods included even effect unresolved; classification does not enable unknown effects.

RecallAssetsV251 caches a display projection: flood near-black RGBmax<=24 from screenshot perimeter. Surviving foreground pixels remain exact RGB, enclosed dark regions remain; all10immutable source PNG/crops and provenance retained. This supersedes V251 background-preservation rendering requirement only.

SourceItemIconRegistry equipment-only necklace projection uses visible alpha bounds center on both axes, replacing earlier optical mass center for necklaces only. No rotation/recolor/mirror/pixel rewrite. Armor optical center remains.

Focused native verification: actual two-aspect HUD touches and clipping; actual consumption tab input/all54food/allregisteredreagent/recall classification with no ownership mutation; all10recall foreground/cached-matte; allavailable necklace bounding centers/native montage/actual equipped window. Adjacent existing gear pair/quick swap/save, earlier equipment direction/size, recall original source bytes/use/migration, food use. Compile/assemble exact candidate. Phone acceptance pending; main integration/full regression separate.
