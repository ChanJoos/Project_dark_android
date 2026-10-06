# Pote monster runtime assets

All art in this directory is adapted/generated candidate art, not recovered original-game pixels or canonical combat data. Pose files are single-frame images selected by runtime state and facing. The renderer adds a small gait/strike displacement; there is no multi-frame looping animation.

- Five prototype species use 60 48x48 RGBA poses: idle/walk/attack × NW/NE/SW/SE. Sources and runtime checksums are recorded in `runtime_asset_manifest.csv`.
- Six ordinary campaign species use 72 384x384 candidate poses in `campaign_v1/`; provenance and source/runtime SHA-256 values are recorded in `campaign_v1/provenance.json`.
- Giant Mantis uses 12 384x384 generated poses in `campaign_v2/giant_mantis/`. The generated transparent 4×3 source atlas and prompt are preserved under `assets/pote/review/pote_mantis_generated_v1/`; pose-level hashes are in `runtime_asset_manifest_v2.csv` and `campaign_v2/provenance.json`.
- The game selects the direction-specific image for each state. The new boss art and the campaign_v1 sets remain adapted candidates pending device visual acceptance.
- Strong/elite monster variants are not part of the A/B/C campaign pools. Historical review art remains outside the Android production source set unless explicitly mapped.
- V111 build/test/APK verification and physical-device acceptance are pending CI and user-device review.
