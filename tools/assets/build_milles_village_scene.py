#!/usr/bin/env python3
"""Build a reproducible village layout from authored districts and path margins.

The base JSON owns landmarks and gameplay anchors. This script expands vegetation and
small props into *independent* editable placements; it does not bake a screenshot.
"""
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MAPS = ROOT / "assets/milles/production/maps"
BASE = MAPS / "milles_garden_base.json"
OUTPUT = MAPS / "milles_garden.json"
ASSET_ANCHORS = json.loads(BASE.read_text(encoding="utf-8")).get("asset_anchors", {})
FENCE_JAVA = ROOT / "app/src/main/java/com/projectdark/mobile/world/MillesDistrictFenceFootprints.java"

# These are the same authored centerlines as AdaptedMillesIsometricTileLayer.PATHS.
# Keep the two in sync via the asset audit, not an independent random road generator.
# One road authority: parse the exact runtime centerlines, including the door branches.
import re
TILE_JAVA = ROOT / "app/src/main/java/com/projectdark/mobile/world/AdaptedMillesIsometricTileLayer.java"
road_source = TILE_JAVA.read_text().split("private static final float[][][] PATHS={", 1)[1].split("\n  };", 1)[0]
PATHS = [[(float(x), float(y)) for x, y in re.findall(r"\{([-\d.]+),([-\d.]+)\}", line)]
         for line in road_source.splitlines() if re.search(r"\{[-\d.]+,[-\d.]+\}", line)]

RIVER_JAVA = ROOT / "app/src/main/java/com/projectdark/mobile/world/MillesRiverGeometry.java"
river_source = RIVER_JAVA.read_text().split("CENTER={", 1)[1].split(";", 1)[0]
RIVER = [(float(x),float(y)) for x,y in re.findall(r"\{([-\d.]+),([-\d.]+)\}",river_source)]


BUILDINGS = [
    ("west_armorer", "buildings/BLD_004_armor_shop.png", 60, 700, .86, "west_crafts"),
    ("south_flower_shop", "buildings/BLD_007_flower_shop.png", 400, 1088, .92, "south_residences"),
    ("south_library", "buildings/BLD_008_library.png", 1120, 1050, .92, "south_residences"),
    ("east_guild", "buildings/BLD_009_guild.png", 1530, 1170, .92, "east_residences"),
    ("north_healer", "buildings/BLD_010_healer.png", 2024, 320, .89, "east_quiet"),
]

# A pair of small gardens reuses the ten-piece open-gate footprint of the
# original center garden. Their offset is authored; rails vary, gates stay open.
FENCE_LOTS = [
    ("orchard", -720, 168, "west_crafts"),
    ("south", -150, 690, "south_residences"),
]

# Each landmark is surrounded by a small, coherent set of props. The world and the
# user's route remain clear; these coordinates are scene composition, not a random scatter.
STORY_PROPS = [
    ("craft_hay", "materials/OBJ_hay.png", 114, 508, .42, "west_crafts"),
    ("craft_logs", "materials/OBJ_log.png", -46, 780, .37, "west_crafts"),
    ("craft_sack", "storage/OBJ_sack.png", 158, 484, .30, "west_crafts"),
    ("craft_barrels", "storage/OBJ_barrel.png", 114, 462, .27, "west_crafts"),
    ("craft_rock", "rocks/OBJ_rock_02.png", -120, 626, .41, "west_crafts"),
    ("craft_notice", "street/OBJ_signpost.png", 168, 806, .45, "west_crafts"),
    ("church_bench", "video_reference/objects/bench_video_cutout_02.png", 1760, 584, .42, "east_quiet"),
    ("church_flower_a", "vegetation/flowers/OBJ_flower_01.png", 1680, 570, .28, "east_quiet"),
    ("church_flower_b", "vegetation/flowers/OBJ_flower_03.png", 1848, 430, .30, "east_quiet"),
    ("inn_cart", "market/OBJ_cart.png", 2256, 832, .25, "waterside"),
    ("inn_crates", "storage/OBJ_crate.png", 2240, 768, .25, "waterside"),
    ("inn_sacks", "storage/OBJ_sack.png", 2272, 720, .30, "waterside"),
    ("inn_bench", "video_reference/objects/bench_video_cutout_03.png", 2130, 830, .43, "waterside"),
    ("inn_lantern", "street/OBJ_lamp_milles_rope.png", 2176, 864, .76, "waterside"),
    ("south_well", "street/OBJ_well.png", 336, 1210, .53, "south_residences"),
    ("south_hay", "materials/OBJ_hay.png", 560, 1100, .32, "south_residences"),
    ("south_logs", "materials/OBJ_log.png", 560, 1040, .32, "south_residences"),
    ("south_crates", "storage/OBJ_crate.png", 984, 1050, .29, "south_residences"),
    ("south_flowerbed_a", "vegetation/flowers/OBJ_flower_01.png", 568, 1130, .30, "south_residences"),
    ("south_flowerbed_b", "vegetation/flowers/OBJ_flower_02.png", 1240, 1144, .29, "south_residences"),
    ("orchard_tree", "vegetation/trees/OBJ_tree_milles_willow.png", 360, 708, .88, "west_crafts"),
    ("orchard_ring", "street/OBJ_tree_ring_milles_reference.png", 360, 708, .70, "west_crafts"),
    ("south_garden_tree", "vegetation/trees/OBJ_tree_milles_willow.png", 920, 1230, .78, "south_residences"),
    ("south_garden_ring", "street/OBJ_tree_ring_milles_reference.png", 920, 1230, .70, "south_residences"),
    ("south_garden_bench", "video_reference/objects/bench_video_cutout_03.png", 1082, 1320, .42, "south_residences"),
    ("east_guild_notice", "street/OBJ_noticeboard.png", 1680, 1264, .31, "east_residences"),
    ("east_guild_crate", "storage/OBJ_crate.png", 1680, 1220, .28, "east_residences"),
    ("east_guild_log", "materials/OBJ_log.png", 1730, 1190, .26, "east_residences"),
    ("garden_ring", "street/OBJ_tree_ring_milles_reference.png", 1080, 536, .70, "garden"),
    ("park_bench_nw", "video_reference/objects/bench_video_cutout_02.png", 636, 452, .43, "civic_square"),
    ("park_bench_ne", "video_reference/objects/bench_video_cutout_03.png", 844, 404, .43, "civic_square"),
    ("park_bench_sw", "video_reference/objects/bench_video_cutout_01.png", 706, 654, .43, "civic_square"),
    ("park_bench_se", "video_reference/objects/bench_video_cutout_04.png", 930, 572, .43, "civic_square"),
    ("park_rope_light_nw", "street/OBJ_lamp_milles_rope.png", 580, 444, .70, "civic_square"),
    ("park_rope_light_ne", "street/OBJ_lamp_milles_rope.png", 918, 418, .70, "civic_square"),
    ("park_rope_light_south", "street/OBJ_lamp_milles_rope.png", 980, 660, .70, "civic_square"),

]

# Clumps form readable groves, not a uniform noise carpet. Extra trees frame roads
# without covering the fountain, doors or active route.
GROVES = [
    ("north_wood", [(120, 190), (232, 182), (330, 214), (410, 154), (526, 172), (1040, 180)]),
    ("west_wood", [(-270, 340), (-132, 278), (-310, 528), (-108, 1020), (98, 1040), (248, 1160)]),
    ("civic_park", [(556, 356), (644, 382), (872, 344), (970, 298), (500, 706), (594, 794), (744, 450), (1010, 734)]),
    ("east_garden", [(1176, 260), (1304, 230), (1730, 324), (1250, 600), (1390, 606), (1650, 620), (1245, 640)]),
    ("south_grove", [(246, 1350), (462, 1360), (1000, 1300), (1160, 1430), (1350, 1320), (1550, 1470)]),
    ("waterside", [(1730, 960), (2150, 1008), (2150, 1320), (1880, 1430), (2270, 1220)]),
    ("west_edge", [(-340, 688), (-256, 760), (-300, 896), (-168, 1160), (-48, 1280), (112, 1460), (256, 1496)]),
    ("south_woodland", [(352, 1392), (528, 1480), (640, 1384), (1216, 1256), (1336, 1456), (1472, 1344), (1640, 1496)]),
    ("east_edge", [(2160, 416), (2232, 512), (2280, 944), (2216, 1440), (2024, 1496)]),
]
TREE_ART = ["vegetation/trees/OBJ_tree_milles_willow.png", "vegetation/trees/OBJ_tree_01.png", "vegetation/trees/OBJ_tree_04.png", "vegetation/trees/OBJ_tree_05.png"]
BUSH_ART = ["vegetation/bushes/OBJ_bush_01.png", "vegetation/bushes/OBJ_bush_02.png", "vegetation/bushes/OBJ_bush_03.png"]


def distance_segment(x, y, ax, ay, bx, by):
    dx, dy = bx - ax, by - ay
    t = max(0., min(1., ((x - ax) * dx + (y - ay) * dy) / (dx * dx + dy * dy))) if dx or dy else 0.
    return math.hypot(x - ax - t * dx, y - ay - t * dy)


def distance_road(x, y):
    return min(distance_segment(x, y, *a, *b) for path in PATHS for a, b in zip(path, path[1:]))


def beside_river(x,y):
    return min(distance_segment(x,y,*a,*b) for a,b in zip(RIVER,RIVER[1:])) < 90


def within_building(x, y, margin=0):
    bases = [(320, 420), (760, 300), (1120, 360), (1540, 455), (2035.2, 766.4)]
    bases += [(x, y) for _, _, x, y, _, _ in BUILDINGS]
    return any(abs(x - bx) < 134 + margin and by - 188 - margin < y < by + 48 + margin for bx, by in bases)


def add(objects, id, asset, x, y, scale, district):
    item = dict(id=id, asset=asset, x=round(x), y=round(y), scale=round(scale, 2), district=district)
    # Measured immutable source anchors are authored data, so clean CI needs no image library.
    if asset in ASSET_ANCHORS:
        item.update(ASSET_ANCHORS[asset])
    if "_ring" in id:
        item["draw"] = "ground"
    objects.append(item)


def gardens(objects):
    original = [item for item in objects if item["id"].startswith("garden_fence_")]
    assert len(original) == 10
    added = []
    for name, dx, dy, district in FENCE_LOTS:
        for piece in original:
            copy = dict(piece)
            copy["id"] = piece["id"].replace("garden_fence_", f"{name}_fence_")
            copy["x"], copy["y"] = piece["x"] + dx, piece["y"] + dy
            copy["district"] = district
            copy.pop("source", None)
            if piece["id"].endswith(("nw_1", "se_2")):
                copy["asset"] = "structures/fences/OBJ_palisade_milles_rail.png"
            added.append(copy)
    objects.extend(added)
    rows = [f'    out.add(new MillesProductionCollision.Footprint("{p["id"]}", MillesProductionCollision.Kind.FENCE, {p["x"] - 19}f, {p["y"] - 8}f, {p["x"] + 19}f, {p["y"] + 8}f));' for p in added]
    source = """package com.projectdark.mobile.world;

import java.util.List;

/** Generated by build_milles_village_scene.py from independently anchored garden segments. */
final class MillesDistrictFenceFootprints {
  private MillesDistrictFenceFootprints(){}
  static void addTo(List<MillesProductionCollision.Footprint> out){
""" + "\n".join(rows) + "\n  }\n}\n"
    FENCE_JAVA.write_text(source, encoding="utf-8")


def path_margin(objects):
    """Repeat source-inspired grass along *both* shoulders, with deterministic gaps."""
    for arm, path in enumerate(PATHS):
        walked = 0
        last = -100
        for a, b in zip(path, path[1:]):
            length = math.dist(a, b)
            steps = max(1, math.ceil(length / 13))
            for step in range(steps):
                d = walked + length * step / steps
                if d - last < 48 + (arm * 13 + int(d / 80) * 17) % 18:
                    continue
                last = d
                t = step / steps
                px, py = a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t
                nx, ny = -(b[1] - a[1]) / length, (b[0] - a[0]) / length
                for side in (-1, 1):
                    key = int(d / 10) + arm * 29 + (side + 1) * 5
                    shoulder = 43 + key % 15
                    x, y = px + nx * shoulder * side, py + ny * shoulder * side
                    if math.hypot((x - 768)/1.4, y - 540) < 145 or within_building(x, y, 12):
                        continue
                    if not (-475 < x < 2260 and 90 < y < 1570):
                        continue
                    if beside_river(x,y) or distance_road(x, y) < 38 or any(math.hypot(x-o["x"], (y-o["y"])*1.5) < 48 for o in objects if not o["id"].startswith("shoulder_")):
                        continue
                    art = "vegetation/grass/OBJ_grass_milles_dense.png" if key % 3 == 0 else "vegetation/grass/OBJ_grass_edge_milles_reference.png"
                    district = ["west_crafts", "north_services", "east_market", "waterside", "south_gate"][arm]
                    add(objects, f"shoulder_{arm}_{int(d)}_{'l' if side < 0 else 'r'}", art, x, y, .28 + key % 5 * .038, district)
            walked += length


def scenery_contacts(objects):
    """Generate ground contacts from the same placements; never block the full tree canopy."""
    contacts = []
    for item in objects:
        asset = item["asset"]
        x, y = item["x"], item["y"]
        if "vegetation/trees/" in asset:
            kind, w, h = "TREE", 16, 10
        elif "/bench_" in asset:
            kind, w, h = "BENCH", 50, 16
            y -= 5
        elif "fountain" in asset:
            kind, w, h = "FOUNTAIN", 112, 38
            y -= 20
        elif "OBJ_well" in asset:
            kind, w, h = "WELL", 38, 22
            y -= 8
        else:
            continue
        contacts.append((item["id"], kind, x-w/2, y-h/2, x+w/2, y+h/2))
    rows = [f'    out.add(new MillesProductionCollision.Footprint("{id}", MillesProductionCollision.Kind.{kind}, {l:.1f}f, {t:.1f}f, {rr:.1f}f, {b:.1f}f));' for id,kind,l,t,rr,b in contacts]
    source = """package com.projectdark.mobile.world;
import java.util.List;
/** Generated from milles_garden placements; contacts only, never visual canopy bounds. */
final class MillesSceneryFootprints {
  static void addTo(List<MillesProductionCollision.Footprint> out){
"""+"\n".join(rows)+"\n  }\n  private MillesSceneryFootprints(){}\n}\n"
    (ROOT / "app/src/main/java/com/projectdark/mobile/world/MillesSceneryFootprints.java").write_text(source)


def main():
    data = json.loads(BASE.read_text(encoding="utf-8"))
    objects = data["objects"]
    for entry in BUILDINGS:
        add(objects, *entry)
    for entry in STORY_PROPS:
        add(objects, *entry)
    gardens(objects)
    for zone, locations in GROVES:
        for i, (x, y) in enumerate(locations):
            if within_building(x, y, 8) or distance_road(x, y) < 66 or beside_river(x,y):
                continue
            tree = TREE_ART[(i + len(zone)) % len(TREE_ART)]
            add(objects, f"{zone}_tree_{i}", tree, x, y, .90 if "willow" in tree else (.55 if "tree_01" in tree else .88), zone)
            for k, (dx, dy) in enumerate(((-45, 28), (54, 23))):
                bx, by = x + dx, y + dy
                if within_building(bx, by) or beside_river(bx,by) or distance_road(bx, by) < 48 or any(math.hypot(bx-o["x"], (by-o["y"])*1.5) < 38 for o in objects):
                    continue
                add(objects, f"{zone}_understory_{i}_{k}", BUSH_ART[(i + k) % 3], bx, by, .27 + (i % 3) * .03, zone)
    path_margin(objects)
    scenery_contacts(objects)
    ids = [o["id"] for o in objects]
    assert len(ids) == len(set(ids)), "duplicate village placement"
    OUTPUT.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {len(objects)} independent Milles objects ({len(objects) - len(json.loads(BASE.read_text())['objects'])} district objects)")


if __name__ == "__main__":
    main()
