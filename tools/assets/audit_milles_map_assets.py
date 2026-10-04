#!/usr/bin/env python3
"""Validate the reusable Milles map layout and the runtime asset contract."""
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PRODUCTION = ROOT / "assets/milles/production"
LAYOUT = PRODUCTION / "maps/milles_garden.json"
RENDERER = ROOT / "app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapRenderer.java"
TILES = ROOT / "app/src/main/java/com/projectdark/mobile/world/AdaptedMillesIsometricTileLayer.java"
COLLISION = ROOT / "app/src/main/java/com/projectdark/mobile/world/MillesProductionCollision.java"
DISTRICT_FENCES = ROOT / "app/src/main/java/com/projectdark/mobile/world/MillesDistrictFenceFootprints.java"


def main() -> None:
    authored_bytes = LAYOUT.read_bytes()
    layout = json.loads(authored_bytes.decode("utf-8"))
    generated = subprocess.run(["python3", str(ROOT / "tools/assets/build_milles_village_scene.py")], capture_output=True, text=True, check=True)
    assert generated.returncode == 0
    assert LAYOUT.read_bytes() == authored_bytes, "generated map differs from authored source; commit base and output together"
    assert layout["ground"]["tile_size"] == [64, 32]
    assert layout["ground"]["path_width_tiles"] <= 1
    items = layout["objects"]
    ids = [item["id"] for item in items]
    assert len(ids) == len(set(ids)), "duplicate map object ID"
    assert len(items) >= 200, "village districts lost their authored placements"
    assert len({item["district"] for item in items}) >= 8
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
        "video_reference/objects/bench_video_cutout_01.png",
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
    ):
        assert (PRODUCTION / rel).is_file(), f"missing reusable family: {rel}"
    renderer = RENDERER.read_text(encoding="utf-8")
    assert "maps/milles_garden.json" in renderer
    assert "garden_route_scene" not in renderer
    assert "drawConnectedSoil(canvas,world)" in renderer
    assert 'SOIL_SURFACE="video_reference/terrain/dirt_path_fill_texture.png"' in renderer
    tile_source = TILES.read_text(encoding="utf-8")
    assert "traceRoadCells" in tile_source and "connectRoadCells" in tile_source
    assert "private static final float[][][] PATHS" in tile_source
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
    for building in ("west_armorer", "south_flower_shop", "south_library", "east_guild", "north_healer"):
        assert f'"{building}",Kind.BUILDING,' in COLLISION.read_text(encoding="utf-8")
        assert building in ids
    # V100: visible solids and ground layers share the committed placement authority.
    scenery = (ROOT / "app/src/main/java/com/projectdark/mobile/world/MillesSceneryFootprints.java").read_text()
    solids = [item for item in items if "vegetation/trees/" in item["asset"] or "/bench_" in item["asset"] or "fountain" in item["asset"] or "OBJ_well" in item["asset"]]
    for item in solids:
        assert f'Footprint("{item["id"]}",' in scenery, f"visible solid lacks ground collision: {item['id']}"
    by_id = {item["id"]: item for item in items}
    for item in items:
        if item.get("draw") == "door":
            host = by_id[item["id"].split("_portal")[0]]
            assert item["depth_y"] > host["y"], "door marker must remain above its facade"
    assert by_id["garden_ring"]["draw"] == "ground", "tree bed must not paint over its trunk"
    assert by_id["waterside_pond"]["draw"] == "ground"
    assert by_id["waterside_bridge"]["ground_z"] > by_id["waterside_pond"].get("ground_z", 0)
    generator = (ROOT / "tools/assets/build_milles_village_scene.py").read_text()
    assert 'TILE_JAVA.read_text()' in generator, "road margins must follow runtime roads including shop-door branches"
    # Scene generation is deterministic; a repeated run may not silently reflow a district.
    before = LAYOUT.read_bytes()
    subprocess.run(["python3", str(ROOT / "tools/assets/build_milles_village_scene.py")], check=True, capture_output=True)
    assert LAYOUT.read_bytes() == before, "village scene generation is unstable"
    print(f"Milles asset layout PASS: {len(items)} independent placements, reusable terrain and props")


if __name__ == "__main__":
    main()
