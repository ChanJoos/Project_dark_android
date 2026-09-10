# Visual Designer handoff

## 2026-09-11 — P0 character rescue study 2

Base main: `018eac36cfdef1cd95187164bdc1d4033fd926e8`
Evidence: `[ADAPTED]`; not claimed as original Nexon art.

Delivered:
- `design/visual/approved/player_martial_idle_se_v2.png` — transparent 24×32 SE/down-right martial-artist IDLE frame; unarmed, shield allowed.
- `design/visual/previews/player_martial_idle_se_v2_preview_8x.png` — 8× nearest-neighbor inspection preview.
- `design/visual/asset_manifest.json` — runtime scale 1.50, foot anchor `[12,29]`, alpha bbox `[3,1,21,30]`, evidence and review.

Visual result: full head→neck→torso silhouette is connected, limbs remain inside one 24×32 frame, SE reads down-right, no weapon is present. Art review **8.7/10 PASS**; `runtimeEligible:true`. This supersedes rejected v1 for Character consumption.

Character consumer contract: preserve native 24×32 frame, render at 1.50× with nearest-neighbor and foot anchor `[12,29]`; do not mirror this SE frame into other directions. Next visual task: build NW/NE/SW IDLE from the accepted identity, then a coherent four-direction WALK cycle before ATTACK.
