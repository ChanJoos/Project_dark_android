# Pote generated monster runtime test assets v1

This production asset directory contains resized copies of the current generated candidate images used by the 2026-09-29 test APK request. These are adapted test art, not extracted original-game sprites or canonical monster definitions.

- Runtime mappings: red/green/purple/silver Pamfet and Lycanthrope only.
- 60 individual 48x48 RGBA frames: idle/walk/attack × NW/NE/SW/SE per mapped species.
- Each frame keeps a shared bottom ground anchor; alpha silhouette height is at most 30px. The app draws at the 48px frame size.
- The frames are single poses. They do not provide multi-frame looping animation. Runtime AI state/facing selects the matching image.
- Trant has only 9 candidates and no complete four-direction attack set, so it is not registered. Other species and strong variants are not registered.
- Source candidate files and per-file SHA-256 evidence: `assets/pote/review/monster_rebuild_v1/sprites/` and `runtime_asset_manifest.csv`.
- The old `monster_test_v04` production copy is removed from the packaged APK. Historical review material is outside the Android production asset source set.
- Current verification: Actions run `36534954426` passed at source SHA `1def7a92aeef68ce144ebaad8b94f02dee011a12`. APK artifact `11017989115`, versionCode 49, APK SHA-256 `9b8cb7e86c7b9c89b34667199711b1738f72aa9267b6edbb79743c934b18c799`; APK inventory confirmed 60 generated poses and no `monster_test_v04/` paths. Automated renderer/placement tests passed. Device interaction and visual acceptance remain pending. The source candidate files are preserved at the art review commit `1011bfbf8171c1510cc47870adb86cf50be870e3`; runtime downsized files and their source checksums are listed in `runtime_asset_manifest.csv`.


## Runtime issue found after v49 user test

Version 49 did not meet the user's on-device expectations: only Purple appeared near the entry, walk/attack behavior was not visible, and facing looked wrong. The follow-up patch clusters all five test candidates near the entrance, adds a walk gait and short attack lunge around the single-pose art, renders the shared-combat attack image, and releases attack facing after its visual window. The gameplay direction contract is four diagonals. Follow-up source SHA `3cd69850c16a61629e4c1c80a9683cb6f4fc46e3`, versionCode 50, passed Actions run `36549350583`; Pote runtime/presentation and spatial tests plus `assembleDebug` passed. APK artifact `11024127131`, APK SHA-256 `807437bf4b5270a2701c2380e854eeb032018117952336f1b0e1f3bb516961d4`. APK inventory confirms 60 new runtime poses and no `monster_test_v04/` assets. User-device interaction and visual acceptance remain pending.
