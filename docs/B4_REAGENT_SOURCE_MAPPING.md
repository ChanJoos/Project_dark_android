# Classic reagent source mapping

Source SSOT: user-provided original Legend of Darkness inventory screenshot (2026-09-24).

Confirmed by user:
- top row, rightmost two slots: 코마디움, 디베노뭄
- directly below 코마디움: 엑스쿠라눔
- recall row below; black scroll: 밀레스리콜

Runtime assets are lossless WEBP with tight, variable canvases, matching the repository's existing master-item convention (examples: mu0000001 11x17, mu0000002 16x16, mw001 16x8, mh223 13x20). No forced 32x32 frame.

Files:
- master/assets/items/consumable/it_reagent_komadium.webp
- master/assets/items/consumable/it_reagent_dibenomum.webp
- master/assets/items/consumable/it_reagent_excuranum.webp
- master/assets/items/consumable/it_recall_milles.webp

Rules:
- Do not use the previously generated 32x32 PNG set.
- 쿠라눔 is intentionally not mapped until its source slot is confirmed.
- Inventory/shop rendering must preserve nearest-neighbor filtering.
