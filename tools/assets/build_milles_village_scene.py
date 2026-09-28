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
    # The waterside is a destination: the inn road reaches the pond-side rest area.
    [(1824, 752), (1860, 824), (1900, 904), (1960, 992), (1980, 1060)],
    # South residents can walk from the main lane to their enclosed garden.
    [(800, 1328), (900, 1345), (1060, 1335)],
]

BUILDINGS = [
    # Canonical non-enterable homes. One visual family, placed as coherent residential frontage.
    ("house_west", "buildings/BLD_001_house.png", 60, 700, 2.00, "west_residential"),
    ("house_southwest", "buildings/BLD_001_house.png", 400, 1040, 2.00, "south_residences"),
    ("house_south", "buildings/BLD_001_house.png", 1120, 1050, 2.00, "south_residences"),
    ("house_east", "buildings/BLD_001_house.png", 1530, 1170, 2.00, "east_residences"),
    ("house_northeast", "buildings/BLD_001_house.png", 1880, 320, 2.00, "east_quiet"),
]

# A pair of small gardens reuses the ten-piece open-gate footprint of the
# original center garden. Their offset is authored; rails vary, gates stay open.
FENCE_LOTS = [
    ("orchard", -540, 240, "west_crafts"),
    ("south", -150, 690, "south_residences"),
]

# Each landmark is surrounded by a small, coherent set of props. The world and the
# user's route remain clear; these coordinates are scene composition, not a random scatter.
STORY_PROPS = [
    ("craft_hay", "materials/OBJ_hay.png", 184, 760, .42, "west_crafts"),
    ("craft_logs", "materials/OBJ_log.png", 124, 768, .37, "west_crafts"),
    ("craft_sack", "storage/OBJ_sack.png", 220, 712, .30, "west_crafts"),
    ("craft_barrels", "storage/OBJ_barrel.png", 208, 675, .27, "west_crafts"),
    ("craft_rock", "rocks/OBJ_rock_02.png", -120, 626, .41, "west_crafts"),
    ("craft_cart", "market/OBJ_cart.png", 452, 388, .29, "west_crafts"),
    ("craft_notice", "street/OBJ_signpost.png", 168, 806, .45, "west_crafts"),
    ("market_stall", "market/OBJ_stall_01.png", 1240, 496, .54, "east_market"),
    ("market_tent", "market/OBJ_tent_01.png", 1320, 490, .55, "east_market"),
    ("market_crates", "storage/OBJ_crate.png", 1300, 520, .29, "east_market"),
    ("market_sacks", "storage/OBJ_sack.png", 1200, 526, .29, "east_market"),
    ("market_barrel", "storage/OBJ_barrel.png", 1330, 558, .25, "east_market"),
    ("market_hay", "materials/OBJ_hay.png", 1250, 570, .28, "east_market"),
    ("church_statue", "street/OBJ_statue.png", 1694, 528, .47, "east_quiet"),
    ("church_arch", "structures/arches_gates/OBJ_arch.png", 1720, 530, .40, "east_quiet"),
    ("church_bench", "street/OBJ_bench_forged_v2.png", 1530, 548, .060, "east_quiet"),
    ("church_flower_a", "vegetation/flowers/OBJ_flower_01.png", 1680, 570, .28, "east_quiet"),
    ("church_flower_b", "vegetation/flowers/OBJ_flower_03.png", 1650, 550, .30, "east_quiet"),
    ("church_tree_west", "vegetation/trees/OBJ_tree_01.png", 1435, 590, .46, "east_quiet"),
    ("church_tree_east", "vegetation/trees/OBJ_tree_03.png", 1630, 585, .46, "east_quiet"),
    ("church_bench_east", "street/OBJ_bench_forged_v2.png", 1575, 550, .060, "east_quiet"),
    ("inn_cart", "market/OBJ_cart.png", 2100, 748, .25, "waterside"),
    ("inn_crates", "storage/OBJ_crate.png", 2090, 692, .25, "waterside"),
    ("inn_sacks", "storage/OBJ_sack.png", 2090, 638, .30, "waterside"),
    ("inn_bench", "street/OBJ_bench_forged_v2.png", 1960, 770, .060, "waterside"),
    ("inn_lantern", "street/OBJ_lamp_milles_rope.png", 2040, 750, .76, "waterside"),
    ("pond_rocks", "rocks/OBJ_rock_01.png", 1700, 1090, .31, "waterside"),
    ("pond_reeds", "vegetation/grass/OBJ_grass_milles_dense.png", 1760, 1090, .70, "waterside"),
    ("pond_bench", "street/OBJ_bench_forged_v2.png", 1900, 976, .060, "waterside"),
    ("south_well", "street/OBJ_well.png", 400, 1160, .53, "south_residences"),
    ("south_hay", "materials/OBJ_hay.png", 514, 1118, .32, "south_residences"),
    ("south_logs", "materials/OBJ_log.png", 544, 1090, .32, "south_residences"),
    ("south_crates", "storage/OBJ_crate.png", 1170, 1112, .29, "south_residences"),
    ("south_flowerbed_a", "vegetation/flowers/OBJ_flower_01.png", 452, 1200, .30, "south_residences"),
    ("south_flowerbed_b", "vegetation/flowers/OBJ_flower_02.png", 1240, 1144, .29, "south_residences"),
    ("orchard_tree", "vegetation/trees/OBJ_tree_milles_willow.png", 540, 780, .78, "west_crafts"),
    ("orchard_ring", "street/OBJ_tree_ring_milles_reference.png", 540, 780, .70, "west_crafts"),
    ("orchard_flower", "vegetation/flowers/OBJ_flower_01.png", 580, 810, .25, "west_crafts"),
    ("orchard_barrel", "storage/OBJ_barrel.png", 664, 806, .22, "west_crafts"),
    ("orchard_lantern", "street/OBJ_lamp_milles_rope.png", 650, 724, .68, "west_crafts"),
    ("south_garden_tree", "vegetation/trees/OBJ_tree_milles_willow.png", 920, 1230, .78, "south_residences"),
    ("south_garden_ring", "street/OBJ_tree_ring_milles_reference.png", 920, 1230, .70, "south_residences"),
    ("south_garden_flower", "vegetation/flowers/OBJ_flower_02.png", 972, 1250, .26, "south_residences"),
    ("south_garden_bench", "street/OBJ_bench_forged_v2.png", 1060, 1308, .060, "south_residences"),
    ("south_garden_lantern", "street/OBJ_lamp_milles_rope.png", 1032, 1178, .67, "south_residences"),
    ("east_guild_notice", "street/OBJ_noticeboard.png", 1710, 1120, .31, "east_residences"),
    ("east_guild_crate", "storage/OBJ_crate.png", 1680, 1220, .28, "east_residences"),
    ("east_guild_log", "materials/OBJ_log.png", 1690, 1190, .26, "east_residences"),
    ("garden_ring", "street/OBJ_tree_ring_milles_reference.png", 1080, 536, .70, "garden"),
]

# Clumps form readable groves, not a uniform noise carpet. Extra trees frame roads
# without covering the fountain, doors or active route.
GROVES = [
    ("north_wood", [(120, 190), (232, 182), (330, 214), (410, 154), (526, 172)]),
    ("west_wood", [(-270, 340), (-132, 278), (-310, 528), (-108, 1020), (98, 1040), (248, 1160)]),
    ("civic_park", [(506, 626), (568, 672), (944, 310), (1000, 326), (1030, 684)]),
    ("east_garden", [(1304, 230), (1730, 324), (1250, 600), (1390, 606), (1650, 620)]),
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


def add(objects, id, asset, x, y, scale, district, group=None):
    entry=dict(id=id, asset=asset, x=round(x), y=round(y), scale=round(scale, 3), district=district)
    entry['group'] = group or district
    objects.append(entry)


def relate(objects):
    """Name the physical neighbour and the use that justifies every placement.

    References establish *patterns*, while coordinates are authored for this new village.
    A relationship may point to an object, a road arm, or a bounded district grove.
    """
    ids = {o['id'] for o in objects}
    buildings = {o['id'] for o in objects if o['asset'].startswith(('buildings/', 'landmarks/'))}
    for o in objects:
        name, art = o['id'], o['asset']
        if name.startswith('shoulder_'):
            anchor, role = 'road_' + name.split('_')[1], 'irregular_grass_verge'
        elif name == 'civic_fountain':
            anchor, role = 'road_0', 'village_centre'
        elif '_understory_' in name:
            anchor, role = name.split('_understory_')[0] + '_tree_' + name.split('_understory_')[1].split('_')[0], 'tree_understory'
        elif '_tree_' in name and any(name.startswith(zone + '_') for zone, _ in GROVES):
            anchor, role = 'grove_' + next(zone for zone, _ in GROVES if name.startswith(zone + '_')), 'woodland_edge'
        elif name == 'garden_bench':
            anchor, role = 'garden_tree_1', 'shaded_rest_area'
        elif name == 'garden_flowerbed_2':
            anchor, role = 'garden_bench', 'benchside_planting'
        elif name.startswith(('garden_fence_', 'garden_flowerbed_', 'garden_ring')):
            anchor, role = 'garden_tree_1', 'enclosed_garden_boundary' if 'fence' in name else 'garden_planting'
        elif name.startswith('orchard_') and name != 'orchard_tree':
            anchor, role = 'orchard_tree', 'enclosed_orchard_boundary' if 'fence' in name else 'orchard_use'
        elif name.startswith('south_fence_') or name.startswith('south_garden_') and name != 'south_garden_tree':
            anchor, role = 'south_garden_tree', 'residential_garden_boundary' if 'fence' in name else 'garden_rest'
        elif name.startswith(('church_',)):
            anchor, role = 'church', 'courtyard_use'
        elif name.startswith('inn_'):
            anchor, role = 'inn', 'inn_frontage'
        elif name.startswith('pond_') or name in ('waterside_bush', 'waterside_tree'):
            anchor, role = 'waterside_pond', 'shoreline_use'
        elif name.startswith('east_guild_'):
            anchor, role = 'house_east', 'residential_frontage'
        elif name.startswith(('south_',)) and name not in buildings:
            anchor, role = ('road_4' if name in ('south_gate_lamp', 'south_garden_tree') else 'house_southwest' if o['x'] < 800 else 'house_south'), 'residential_frontage'
        elif name.startswith('craft_'):
            anchor, role = ('potion_shop' if name in ('craft_cart',) else 'house_west'), 'residential_frontage'
        elif name.startswith('market_'):
            anchor, role = ('potion_shop' if name in ('market_barrel_1', 'market_crate_1') else 'bank' if name in ('market_cart', 'market_stall') else 'market_stall'), 'shop_frontage'
        elif name in ('square_tree_west', 'square_shrub_west'):
            anchor, role = 'square_bench_west', 'shaded_rest_area'
        elif name in ('square_shrub_east', 'square_grass_2'):
            anchor, role = 'garden_tree_1', 'garden_edge_planting'
        elif name == 'square_grass_1':
            anchor, role = 'road_0', 'roadside_grass'
        elif name.startswith(('square_', 'civic_', 'village_noticeboard')):
            anchor, role = 'civic_fountain', 'plaza_rest_or_wayfinding'
        elif name == 'potion_shop_portal':
            anchor, role = 'potion_shop', 'building_entrance'
        elif name in buildings or name in ('civic_fountain', 'orchard_tree', 'south_garden_tree', 'garden_tree_1', 'garden_tree_2', 'waterside_pond'):
            nearest = min(range(len(PATHS)), key=lambda i: min(distance_segment(o['x'], o['y'], *a, *b) for a, b in zip(PATHS[i], PATHS[i][1:])))
            anchor, role = f'road_{nearest}', 'destination_or_park'
        else:
            anchor, role = 'road_' + str(min(range(len(PATHS)), key=lambda i: min(distance_segment(o['x'], o['y'], *a, *b) for a, b in zip(PATHS[i], PATHS[i][1:])))), 'roadside_or_district_edge'
        o['anchor'], o['role'] = anchor, role
        # Spatial contract: metadata is not enough; each role declares the physical rule CI must enforce.
        if role in ('shaded_rest_area', 'garden_rest'):
            o['constraint'] = 'near_shade_and_path'
        elif role in ('building_entrance', 'shop_frontage', 'inn_frontage', 'residential_frontage', 'destination_or_park'):
            o['constraint'] = 'connected_to_walk_network'
        elif role == 'courtyard_use':
            o['constraint'] = 'inside_landmark_forecourt'
        elif role in ('enclosed_orchard_boundary', 'enclosed_garden_boundary', 'residential_garden_boundary'):
            o['constraint'] = 'clustered_boundary'
        elif role in ('woodland_edge', 'tree_understory'):
            o['constraint'] = 'clustered_greenery'
        else:
            o['constraint'] = 'district_context'
        o['basis'] = 'observed_pattern' if role in ('enclosed_orchard_boundary', 'enclosed_garden_boundary', 'residential_garden_boundary', 'plaza_rest_or_wayfinding', 'tree_understory', 'woodland_edge', 'shop_frontage', 'irregular_grass_verge') else 'adapted_village_plan'
        if not (anchor in ids or anchor.startswith('road_') and anchor[5:].isdigit() and int(anchor[5:]) < len(PATHS) or anchor.startswith('grove_') and anchor[6:] in dict(GROVES)):
            raise ValueError(f'{name}: unknown spatial anchor {anchor}')


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
            copy["group"] = f"{name}_enclosure"
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
    """Place grass in uneven verge clusters; never make a uniform double border."""
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
                if (int(d / 155) + arm) % 3 == 1:
                    continue
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
                    district = ["west_crafts", "north_services", "east_market", "waterside", "south_gate", "waterside", "south_residences"][arm]
                    add(objects, f"shoulder_{arm}_{int(d)}_{'l' if side < 0 else 'r'}", art, x, y, .28 + key % 5 * .038, district, f"road_verge_{arm}")
            walked += length


def main():
    data = json.loads(BASE.read_text(encoding="utf-8"))
    objects = data["objects"]
    # Service canon: reagent shop, equipment shop, bank, church and inn.
    for item in objects:
        if item["id"] in ("potion_shop", "weapon_shop", "bank", "church", "inn"):
            item["enterable"] = True
            item["function"] = {
                "potion_shop": "reagent_shop",
                "weapon_shop": "equipment_shop",
                "bank": "bank",
                "church": "church",
                "inn": "inn",
            }[item["id"]]
    for item in objects:
        name = item["id"]
        if name.startswith(("garden_fence_", "garden_tree_")):
            item["group"] = "enclosed_garden"
        elif name.startswith(("square_", "civic_", "village_noticeboard")):
            item["group"] = "fountain_rest_area"
        elif name.startswith(("market_", "potion_shop", "weapon_shop", "bank")):
            item["group"] = "shop_fronts"
        elif name.startswith("waterside_"):
            item["group"] = "pond_rest_area"
        elif name == "inn":
            item["group"] = "inn_frontage"
        else:
            item["group"] = item["district"]
    for entry in BUILDINGS:
        add(objects, *entry)
    for item in objects:
        if item["id"].startswith("house_"):
            item["enterable"] = False
            item["function"] = "residence"
    for entry in STORY_PROPS:
        add(objects, *entry)
    group_prefixes = {"civic_": "fountain_rest_area", "church_": "church_courtyard",
                      "inn_": "inn_frontage", "orchard_": "orchard_enclosure",
                      "south_garden_": "south_garden_enclosure", "pond_": "pond_rest_area",
                      "craft_": "shop_fronts", "market_": "shop_fronts"}
    for item in objects:
        for prefix, group in group_prefixes.items():
            if item["id"].startswith(prefix):
                item["group"] = group
                break
    gardens(objects)
    for zone, locations in GROVES:
        for i, (x, y) in enumerate(locations):
            if within_building(x, y, 8) or distance_road(x, y) < 66:
                continue
            tree = TREE_ART[(i + len(zone)) % len(TREE_ART)]
            add(objects, f"{zone}_tree_{i}", tree, x, y, .45 if "willow" not in tree else .83, zone, f"{zone}_grove")
            for k, (dx, dy) in enumerate(((-45, 28), (54, 23))):
                bx, by = x + dx, y + dy
                if within_building(bx, by) or distance_road(bx, by) < 43:
                    continue
                add(objects, f"{zone}_understory_{i}_{k}", BUSH_ART[(i + k) % 3], bx, by, .27 + (i % 3) * .03, zone, f"{zone}_grove")
    path_margin(objects)
    relate(objects)
    ids = [o["id"] for o in objects]
    assert len(ids) == len(set(ids)), "duplicate village placement"
    OUTPUT.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {len(objects)} independent Milles objects ({len(objects) - len(json.loads(BASE.read_text())['objects'])} district objects)")


if __name__ == "__main__":
    main()
