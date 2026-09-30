# Naver Cafe offline source archive (partial)

The user requested one collection of all eleven linked articles, their original media, and reusable skill/master/motion/effect mappings so later work can run offline.

Five observed article bodies are retained without rewriting: 191105, 191110, 191114, 191241 and 191777. JSON contains rendered body text and observed media URLs; four snapshots also contain the body HTML. Source SHA-256 is recorded in manifest.json. These are historical fan sources, not current official numeric canon.

## Remaining work

- Collect the six remaining article bodies (191445, 401229, 415456, 416044, 245450, 245456) in this execution and compare with older retained sources.
- Download each observed original media binary and bind it to the exact labelled skill/table row. Retaining a remote URL alone is not offline media preservation.
- Preserve the distinction between historical/current variants, original mechanics and adapted project balance; apply accepted changes through Master change records and generators.
- Analyze caster motion, caster/recipient effects, timing and anchors, then verify source icons, skill window/quick-slot and production combat.

Collection stopped after automatic browser approval review interpreted “avoid future revisits” as “stop browsing now.” No route around that rejection was attempted. The archive remains explicitly incomplete. Existing downloaded sources are under the three paths listed in manifest.json.

No runtime behavior changed in this archive-only checkpoint. CI on runtime source ec67c73650dc161374dc614d3d13373baeeb18c5, run36735102944, still has two failing Android assertions; APK build was skipped.
