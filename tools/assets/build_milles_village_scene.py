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
FENCE_JAVA = ROOT / "app/src/main/java/com/projectdark/mobile/world/MillesDistrictFenceFootprints.java"

# These are the same authored centerlines as AdaptedMillesIsometricTileLayer.PATHS.
# Keep the two in sync via the asset audit, not an independent random road generator.
PATHS = [
    [(768, 592), (704, 576), (640, 544), (576, 512), (512, 480), (448, 464), (384, 448), (320, 448)],
    [(768, 592), (768, 544), (752, 496), (752, 448), (736, 400), (736, 368)],
    [(768, 592), (832, 560), (896, 528), (960, 480), (1024, 432), (1088, 400), (1152, 400), (1216, 416), (1280, 432), (1344, 448), (1408, 448), (1472, 448), (1536, 448)],
    [(768, 592), (832, 640), (896, 672), (960, 704), (1056, 720), (1184, 736), (1312, 752), (1440, 768), (1568, 784), (1696, 784), (1824, 752), (1920, 704), (1984, 656)],
    [(768, 592), (736, 656), (720, 720), (704, 784), (704, 848), (720, 912), (736, 976), (752, 1040), (768, 1104), (784, 1168), (800, 1232), (800, 1328), (800, 1456), (800, 1568)],
]

BUILDINGS = [
    ("west_armorer", "buildings/BLD_004_armor_shop.png", 60, 700, .86, "west_crafts"),
    ("south_flower_shop", "buildings/BLD_007_flower_shop.png", 400, 1040, .92, "south_residences"),
    ("south_library", "buildings/BLD_008_library.png", 1120, 1050, .92, "south_residences"),
    ("east_guild", "buildings/BLD_009_guild.png", 1530, 1170, .92, "east_residences"),
    ("north_healer", "buildings/BLD_010_healer.png", 1880, 320, .89, "east_quiet"),
]

# A pair of small gardens reuses the ten-piece open-gate footprint of the
# original center garden. Their offset is authored; rails vary, gates stay open.
FENCE_LOTS = [
    ("orchard", -720, 240, "west_crafts"),
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
    ("craft_cart", "market/OBJ_cart.png", 512, 356, .29, "west_crafts"),
    ("craft_notice", "street/OBJ_signpost.png", 168, 806, .45, "west_crafts"),
    ("market_stall", "market/OBJ_stall_01.png", 1240, 620, .54, "east_market"),
    ("market_tent", "market/OBJ_tent_01.png", 1480, 630, .55, "east_market"),
    ("market_crates", "storage/OBJ_crate.png", 1310, 660, .29, "east_market"),
    ("market_sacks", "storage/OBJ_sack.png", 1210, 668, .29, "east_market"),
    ("market_barrel", "storage/OBJ_barrel.png", 1410, 672, .25, "east_market"),
    ("market_hay", "materials/OBJ_hay.png", 1540, 675, .28, "east_market"),
    ("church_statue", "street/OBJ_statue.png", 1694, 528, .47, "east_quiet"),
    ("church_arch", "structures/arches_gates/OBJ_arch.png", 1780, 472, .40, "east_quiet"),
    ("church_bench", "video_reference/objects/bench_video_cutout_02.png", 1760, 584, .42, "east_quiet"),
    ("church_flower_a", "vegetation/flowers/OBJ_flower_01.png", 1680, 570, .28, "east_quiet"),
    ("church_flower_b", "vegetation/flowers/OBJ_flower_03.png", 1848, 430, .30, "east_quiet"),
    ("inn_cart", "market/OBJ_cart.png", 2160, 704, .25, "waterside"),
    ("inn_crates", "storage/OBJ_crate.png", 2130, 660, .25, "waterside"),
    ("inn_sacks", "storage/OBJ_sack.png", 2180, 626, .30, "waterside"),
    ("inn_bench", "video_reference/objects/bench_video_cutout_03.png", 2130, 830, .43, "waterside"),
    ("inn_lantern", "street/OBJ_lamp_milles_rope.png", 2084, 800, .76, "waterside"),
    ("pond_rocks", "rocks/OBJ_rock_01.png", 1700, 1090, .31, "waterside"),
    ("pond_reeds", "vegetation/grass/OBJ_grass_milles_dense.png", 1760, 1090, .70, "waterside"),
    ("pond_bench", "video_reference/objects/bench_video_cutout_04.png", 2050, 1120, .45, "waterside"),
    ("south_well", "street/OBJ_well.png", 336, 1210, .53, "south_residences"),
    ("south_hay", "materials/OBJ_hay.png", 560, 1100, .32, "south_residences"),
    ("south_logs", "materials/OBJ_log.png", 560, 1040, .32, "south_residences"),
    ("south_crates", "storage/OBJ_crate.png", 984, 1050, .29, "south_residences"),
    ("south_flowerbed_a", "vegetation/flowers/OBJ_flower_01.png", 568, 1130, .30, "south_residences"),
    ("south_flowerbed_b", "vegetation/flowers/OBJ_flower_02.png", 1240, 1144, .29, "south_residences"),
    ("orchard_tree", "vegetation/trees/OBJ_tree_milles_willow.png", 360, 780, .78, "west_crafts"),
    ("orchard_ring", "street/OBJ_tree_ring_milles_reference.png", 360, 780, .70, "west_crafts"),
    ("orchard_flower", "vegetation/flowers/OBJ_flower_01.png", 400, 810, .25, "west_crafts"),
    ("orchard_barrel", "storage/OBJ_barrel.png", 484, 806, .22, "west_crafts"),
    ("orchard_lantern", "street/OBJ_lamp_milles_rope.png", 470, 724, .68, "west_crafts"),
    ("south_garden_tree", "vegetation/trees/OBJ_tree_milles_willow.png", 920, 1230, .78, "south_residences"),
    ("south_garden_ring", "street/OBJ_tree_ring_milles_reference.png", 920, 1230, .70, "south_residences"),
    ("south_garden_flower", "vegetation/flowers/OBJ_flower_02.png", 972, 1250, .26, "south_residences"),
    ("south_garden_bench", "video_reference/objects/bench_video_cutout_03.png", 1082, 1320, .42, "south_residences"),
    ("south_garden_lantern", "street/OBJ_lamp_milles_rope.png", 1032, 1178, .67, "south_residences"),
    ("east_guild_notice", "street/OBJ_noticeboard.png", 1710, 1120, .31, "east_residences"),
    ("east_guild_crate", "storage/OBJ_crate.png", 1680, 1220, .28, "east_residences"),
    ("east_guild_log", "materials/OBJ_log.png", 1730, 1190, .26, "east_residences"),
    ("civic_bench_north", "video_reference/objects/bench_video_cutout_02.png", 688, 424, .46, "civic_square"),
    ("civic_bench_south", "video_reference/objects/bench_video_cutout_01.png", 882, 824, .42, "civic_square"),
    ("civic_rope_light_west", "street/OBJ_lamp_milles_rope.png", 580, 478, .72, "civic_square"),
    ("civic_rope_light_east", "street/OBJ_lamp_milles_rope.png", 922, 458, .75, "civic_square"),
    ("civic_rope_light_south", "street/OBJ_lamp_milles_rope.png", 985, 620, .70, "civic_square"),
    ("garden_ring", "street/OBJ_tree_ring_milles_reference.png", 1080, 536, .70, "garden"),
]

# Clumps form readable groves, not a uniform noise carpet. Extra trees frame roads
# without covering the fountain, doors or active route.
GROVES = [
    ("north_wood", [(120, 190), (232, 182), (330, 214), (410, 154), (526, 172), (1040, 180)]),
    ("west_wood", [(-270, 340), (-132, 278), (-310, 528), (-108, 1020), (98, 1040), (248, 1160)]),
    ("civic_park", [(530, 340), (620, 352), (870, 344), (970, 298), (500, 646), (594, 794), (500, 368), (585, 690), (1010, 630)]),
    ("east_garden", [(1176, 260), (1304, 230), (1730, 324), (1250, 600), (1390, 606), (1650, 620), (1245, 640)]),
    ("south_grove", [(246, 1350), (462, 1360), (1000, 1300), (1160, 1430), (1350, 1320), (1550, 1470)]),
    ("waterside", [(1730, 960), (2080, 1020), (2150, 1320), (1880, 1430), (2270, 1220)]),
]
TREE_ART = ["vegetation/trees/OBJ_tree_milles_willow.png", "vegetation/trees/OBJ_tree_01.png", "vegetation/trees/OBJ_tree_03.png", "vegetation/trees/OBJ_tree_05.png"]
BUSH_ART = ["vegetation/bushes/OBJ_bush_01.png", "vegetation/bushes/OBJ_bush_02.png", "vegetation/bushes/OBJ_bush_03.png"]


def distance_segment(x, y, ax, ay, bx, by):
    dx, dy = bx - ax, by - ay
    t = max(0., min(1., ((x - ax) * dx + (y - ay) * dy) / (dx * dx + dy * dy))) if dx or dy else 0.
    return math.hypot(x - ax - t * dx, y - ay - t * dy)


def distance_road(x, y):
    return min(distance_segment(x, y, *a, *b) for path in PATHS for a, b in zip(path, path[1:]))


def within_building(x, y, margin=0):
    bases = [(320, 420), (760, 300), (1120, 360), (1540, 455), (1980, 650)]
    bases += [(x, y) for _, _, x, y, _, _ in BUILDINGS]
    return any(abs(x - bx) < 134 + margin and by - 188 - margin < y < by + 48 + margin for bx, by in bases)


def add(objects, id, asset, x, y, scale, district):
    objects.append(dict(id=id, asset=asset, x=round(x), y=round(y), scale=round(scale, 2), district=district))


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
                    if math.hypot(x - 768, y - 592) < 142 or within_building(x, y, 12):
                        continue
                    if not (-475 < x < 2260 and 90 < y < 1570):
                        continue
                    if distance_road(x, y) < 32:
                        continue
                    art = "vegetation/grass/OBJ_grass_milles_dense.png" if key % 3 == 0 else "vegetation/grass/OBJ_grass_edge_milles_reference.png"
                    district = ["west_crafts", "north_services", "east_market", "waterside", "south_gate"][arm]
                    add(objects, f"shoulder_{arm}_{int(d)}_{'l' if side < 0 else 'r'}", art, x, y, .28 + key % 5 * .038, district)
            walked += length


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
            if within_building(x, y, 8) or distance_road(x, y) < 66:
                continue
            tree = TREE_ART[(i + len(zone)) % len(TREE_ART)]
            add(objects, f"{zone}_tree_{i}", tree, x, y, .45 if "willow" not in tree else .83, zone)
            for k, (dx, dy) in enumerate(((-45, 28), (54, 23))):
                bx, by = x + dx, y + dy
                if within_building(bx, by) or distance_road(bx, by) < 43:
                    continue
                add(objects, f"{zone}_understory_{i}_{k}", BUSH_ART[(i + k) % 3], bx, by, .27 + (i % 3) * .03, zone)
    path_margin(objects)
    ids = [o["id"] for o in objects]
    assert len(ids) == len(set(ids)), "duplicate village placement"
    OUTPUT.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {len(objects)} independent Milles objects ({len(objects) - len(json.loads(BASE.read_text())['objects'])} district objects)")


if __name__ == "__main__":
    main()
