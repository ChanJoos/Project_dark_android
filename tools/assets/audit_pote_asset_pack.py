#!/usr/bin/env python3
import csv,json,struct
from pathlib import Path
R=Path("assets/pote/production")
atlas=R/"POTE_FOREST_ATLAS_V1.png"; meta=R/"POTE_FOREST_ATLAS_V1.json"; cat=R/"pote_asset_catalog_v1.csv"
raw=atlas.read_bytes()
assert raw[:8]==b"\x89PNG\r\n\x1a\n","atlas is not PNG"
w,h=struct.unpack(">II",raw[16:24]); assert (w,h)==(384,288),(w,h)
m=json.loads(meta.read_text()); assets=m["assets"]; assert len(assets)==44,len(assets)
rows=list(csv.DictReader(cat.open())); assert len(rows)==44,len(rows)
ids=[r["asset_id"] for r in rows]; assert len(ids)==len(set(ids))==44
assert set(ids)==set(assets)
required={"terrain","tree","bush","groundcover","forest_detail","rock","water"}
assert required<=set(r["class"] for r in rows)
assert sum(r["class"]=="tree" for r in rows)==12
assert sum(r["class"]=="terrain" for r in rows)==7
assert sum(r["class"]=="water" for r in rows)==8
assert all(r["status"]=="REFERENCE_DERIVED_ADAPTED_V1" for r in rows)
assert all(r["anchor_rule"] and r["collision_footprint"] and r["draw_rule"] for r in rows)
rects={(v["x"],v["y"],v["w"],v["h"]) for v in assets.values()}; assert len(rects)==44
print("POTE_ASSET_PACK_V1_PASS assets=44 atlas=384x288 trees=12 terrain=7 water=8")
