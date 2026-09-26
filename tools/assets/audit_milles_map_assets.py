#!/usr/bin/env python3
"""Validate the reusable Milles map layout and the runtime asset contract."""
import json
import math
import re
import subprocess
from pathlib import Path
from build_milles_village_scene import distance_road

ROOT = Path(__file__).resolve().parents[2]
PRODUCTION = ROOT / "assets/milles/production"
LAYOUT = PRODUCTION / "maps/milles_garden.json"
RENDERER = ROOT / "app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapRenderer.java"
TILES = ROOT / "app/src/main/java/com/projectdark/mobile/world/AdaptedMillesIsometricTileLayer.java"
COLLISION = ROOT / "app/src/main/java/com/projectdark/mobile/world/MillesProductionCollision.java"
DISTRICT_FENCES = ROOT / "app/src/main/java/com/projectdark/mobile/world/MillesDistrictFenceFootprints.java"


def main() -> None:
    generated = subprocess.run(["python3", str(ROOT / "tools/assets/build_milles_village_scene.py")], text=True, check=True)
    assert generated.returncode == 0
    layout = json.loads(LAYOUT.read_text(encoding="utf-8"))
    assert layout["ground"]["tile_size"] == [64, 32]
    assert layout["ground"]["path_width_tiles"] <= 1
    items = layout["objects"]
    ids = [item["id"] for item in items]
    assert len(ids) == len(set(ids)), "duplicate map object ID"
    assert len(items) >= 200, "village districts lost their authored placements"
    assert len({item["district"] for item in items}) >= 8
    assert all(item.get("group") for item in items), "each object needs a spatial reason"
    by_id = {item['id']: item for item in items}
    for item in items:
        anchor = item.get('anchor', '')
        assert anchor and item.get('role') and item.get('constraint') and item.get('basis') in ('observed_pattern', 'adapted_village_plan'), item['id']
        assert anchor in by_id or re.fullmatch(r'road_[0-6]', anchor) or anchor.startswith('grove_'), item['id']
        if anchor in by_id:
            other = by_id[anchor]
            assert item['id'] != anchor, f"self-anchored prop: {item['id']}"
            assert math.dist((item['x'], item['y']), (other['x'], other['y'])) <= 200, (
                f"orphaned prop {item['id']}: {anchor} too far away")
    assert all(math.dist((by_id['orchard_tree']['x'], by_id['orchard_tree']['y']),
                         (item['x'], item['y'])) < 115 for item in items if item['id'].startswith('orchard_fence_'))
    assert all(math.dist((by_id['south_garden_tree']['x'], by_id['south_garden_tree']['y']),
                         (item['x'], item['y'])) < 115 for item in items if item['id'].startswith('south_fence_'))
    benches = [item for item in items if "bench" in item["id"]]
    assert benches and all(item["asset"] == "street/OBJ_bench_forged_v2.png" for item in benches), "broken video bench cutouts returned"
    assert all(20 <= distance_road(item["x"], item["y"]) <= 115 for item in benches), "bench must serve an accessible path"
    trees = [item for item in items if "/trees/" in item["asset"]]
    for bench_id in ("square_bench_west", "garden_bench"):
        bench = by_id[bench_id]
        assert min(math.dist((bench["x"], bench["y"]), (tree["x"], tree["y"])) for tree in trees) <= 125, f"{bench_id} floats outside shade cluster"
    church_props = [item for item in items if item["id"].startswith("church_") and item["id"] != "church"]
    assert church_props and all(math.dist((item["x"], item["y"]), (by_id["church"]["x"], by_id["church"]["y"])) <= 205 for item in church_props), "church forecourt props escaped landmark cluster"
    assert {"church_tree_west", "church_tree_east", "church_bench", "church_bench_east"}.issubset(by_id), "church forecourt lacks shade/rest framing"
    assert {item["id"] for item in benches if item["group"] == "fountain_rest_area"} == {
        "square_bench_west"
    }, "plaza bench must serve the tree-side rest area without duplication"
    assert len({item["group"] for item in items}) >= 12, "district objects lost their relationships"
    for item in items:
        assert item["scale"] > 0, item
        path = PRODUCTION / item["asset"]
        if not path.is_file():
            path = ROOT / "app/src/main/assets" / item["asset"]
        assert path.is_file(), f"missing map asset: {item['asset']}"
    for rel in (
        "video_reference/terrain/grass_tile_01.png",
        "video_reference/terrain/dirt_path_tile_01.png",
        "video_reference/terrain/dirt_path_fill_texture.png",
        "street/OBJ_bench_forged_v2.png",
        "street/OBJ_fountain_milles_reference.png",
        "structures/fences/OBJ_palisade_milles_reference.png",
        "structures/fences/OBJ_palisade_diagonal_down.png",
        "structures/fences/OBJ_palisade_diagonal_up.png",
        "structures/fences/OBJ_fence_01.png",
        "vegetation/trees/OBJ_tree_01.png",
        "vegetation/grass/OBJ_grass_edge_milles_reference.png",
        "vegetation/grass/OBJ_grass_milles_dense.png",
        "vegetation/trees/OBJ_tree_milles_willow.png",
        "street/OBJ_lamp_milles_rope.png",
        "street/OBJ_tree_ring_milles_reference.png",
        "structures/fences/OBJ_palisade_milles_rail.png",
        "buildings/BLD_001_house.png",
    ):
        assert (PRODUCTION / rel).is_file(), f"missing reusable family: {rel}"
    renderer = RENDERER.read_text(encoding="utf-8")
    assert "maps/milles_garden.json" in renderer
    assert "garden_route_scene" not in renderer
    assert "drawConnectedSoil(canvas,world)" in renderer
    assert 'buildRoadContours(11.5f,AdaptedMillesIsometricTileLayer.approachPaths())' in renderer
    assert 'SOIL_SURFACE="video_reference/terrain/dirt_path_fill_texture.png"' in renderer
    tile_source = TILES.read_text(encoding="utf-8")
    assert "traceRoadCells" in tile_source and "connectRoadCells" in tile_source
    assert "private static final float[][][] PATHS" in tile_source
    assert "private static final float[][][] APPROACH_PATHS" in tile_source
    assert tile_source.count('// west residential frontage') == 1 and '// east homes from south gardens' in tile_source
    assert '{{1536,448},{1600,420},{1680,400},{1750,392},{1820,390}}' in tile_source, 'northeast residential lane must branch from church road'
    garden_fences = [item for item in items if item["id"].startswith("garden_fence_")]
    assert len(garden_fences) >= 10, "garden must form a real enclosure, not loose fence props"
    assert {item["asset"] for item in garden_fences} == {
        "structures/fences/OBJ_palisade_diagonal_up.png",
        "structures/fences/OBJ_palisade_diagonal_down.png",
        "structures/fences/OBJ_palisade_milles_rail.png",
    }
    fence_contacts = {
        identifier: (float(x), float(y))
        for identifier, x, y in re.findall(
            r'add\(b,"(garden_fence_[^"]+)",Kind\.FENCE,([\d.]+)f,([\d.]+)f,',
            COLLISION.read_text(encoding="utf-8"),
        )
    }
    assert fence_contacts == {
        item["id"]: (float(item["x"]), float(item["y"])) for item in garden_fences
    }, "fence art and blocking footprints must share anchors"
    district_fences = [item for item in items if item["id"].startswith(("orchard_fence_", "south_fence_"))]
    assert len(district_fences) == 20
    district_contacts = {
        identifier: ((float(left) + float(right)) / 2, (float(top) + float(bottom)) / 2)
        for identifier, left, top, right, bottom in re.findall(
            r'Footprint\("((?:orchard|south)_fence_[^"]+)", MillesProductionCollision.Kind.FENCE, ([-\d.]+)f, ([-\d.]+)f, ([-\d.]+)f, ([-\d.]+)f\)',
            DISTRICT_FENCES.read_text(encoding="utf-8"),
        )
    }
    assert district_contacts == {
        item["id"]: (float(item["x"]), float(item["y"])) for item in district_fences
    }, "the two complete garden enclosures must share art and collision anchors"
    service_ids = {"potion_shop", "weapon_shop", "bank", "church", "inn"}
    services = {item["id"] for item in items if item.get("enterable")}
    assert services == service_ids, f"unexpected enterable buildings: {services}"
    houses = [item for item in items if item["id"].startswith("house_")]
    assert len(houses) == 5, "Milles needs five authored non-enterable house placements"
    assert all(item["asset"] == "buildings/BLD_001_house.png" for item in houses)
    assert all(item.get("function") == "residence" and item.get("enterable") is False for item in houses)
    forbidden_ids = {"west_armorer", "south_flower_shop", "south_library", "east_guild", "north_healer", "waterside_house"}
    assert not forbidden_ids.intersection(ids), f"legacy special buildings returned: {forbidden_ids.intersection(ids)}"
    forbidden_assets = {
        "buildings/BLD_004_armor_shop.png", "buildings/BLD_007_flower_shop.png",
        "buildings/BLD_008_library.png", "buildings/BLD_009_guild.png", "buildings/BLD_010_healer.png",
    }
    assert not any(item["asset"] in forbidden_assets for item in items), "removed special-building art returned"
    collision_source = COLLISION.read_text(encoding="utf-8")
    for building in (*sorted(service_ids), *(item["id"] for item in houses)):
        assert f'"{building}",Kind.' in collision_source, f"missing collision footprint: {building}"
    # Scene generation is deterministic; a repeated run may not silently reflow a district.
    before = LAYOUT.read_bytes()
    subprocess.run(["python3", str(ROOT / "tools/assets/build_milles_village_scene.py")], check=True, capture_output=True)
    assert LAYOUT.read_bytes() == before, "village scene generation is unstable"
    print(f"Milles asset layout PASS: {len(items)} independent placements, reusable terrain and props")


if __name__ == "__main__":
    main()
