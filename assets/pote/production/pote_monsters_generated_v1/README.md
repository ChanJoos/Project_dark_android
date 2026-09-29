# Pote generated monster runtime test assets v1

This production asset directory contains resized copies of the current generated candidate images used by the 2026-09-29 test APK request. These are adapted test art, not extracted original-game sprites or canonical monster definitions.

- Runtime mappings: red/green/purple/silver Pamfet and Lycanthrope only.
- 60 individual 48x48 RGBA frames: idle/walk/attack × NW/NE/SW/SE per mapped species.
- Each frame keeps a shared bottom ground anchor; alpha silhouette height is at most 30px. The app draws at the 48px frame size.
- The frames are single poses. They do not provide multi-frame looping animation. Runtime AI state/facing selects the matching image.
- Trant has only 9 candidates and no complete four-direction attack set, so it is not registered. Other species and strong variants are not registered.
- Source candidate files and per-file SHA-256 evidence: `assets/pote/review/monster_rebuild_v1/sprites/` and `runtime_asset_manifest.csv`.
- The old `monster_test_v04` production copy is removed from the packaged APK. Historical review material is outside the Android production asset source set.
- Current verification state: CI pending; device interaction and visual acceptance pending.
