# Visual Designer handoff

## 2026-09-11 — P0 character rescue study 1

Base main: `018eac36cfdef1cd95187164bdc1d4033fd926e8`
Evidence: `[ADAPTED]`; this study is not claimed to be an original Nexon frame.

Created:
- `design/visual/studies/player_martial_idle_se_v1.png` — transparent 24×32 SE IDLE study.
- `design/visual/previews/player_martial_idle_se_v1_preview_8x.png` — nearest-neighbor visual inspection preview.
- `design/visual/asset_manifest.json` — dimensions, anchor, evidence and scored review.
- `design/ART_DIRECTION.md` — first visual production contract.

Inspection: alpha bbox x=2..20 / y=3..29, foot baseline y=29, connected head→neck→torso silhouette, no filtered scaling.

Art review: **8.0/10 average — FAIL / NOT FOR CHARACTER CONSUMPTION.** It clears the prior floating-neck structural defect, but diagonal anatomy/material articulation and animation-continuity readiness are below the >=8.5 production gate. Character must therefore **not** wire this study into runtime. The existing crash-safe fallback remains until a PASS asset is delivered.

Next visual task: rework the SE frame until the diagonal martial-artist pose, face/shoulders and cloth/material clusters clear the gate; only then produce NW/NE/SW IDLE from that accepted identity.
