#!/usr/bin/env python3
"""Validate the reusable Milles map layout and the runtime asset contract."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PRODUCTION = ROOT / "assets/milles/production"
LAYOUT = PRODUCTION / "maps/milles_garden.json"
RENDERER = ROOT / "app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapRenderer.java"
TILES = ROOT / "app/src/main/java/com/projectdark/mobile/world/AdaptedMillesIsometricTileLayer.java"
COLLISION = ROOT / "app/src/main/java/com/projectdark/mobile/world/MillesProductionCollision.java"


def main() -> None:
    layout = json.loads(LAYOUT.read_text(encoding="utf-8"))
    assert layout["ground"]["tile_size"] == [64, 32]
    assert layout["ground"]["path_width_tiles"] <= 1
    items = layout["objects"]
    ids = [item["id"] for item in items]
    assert len(ids) == len(set(ids)), "duplicate map object ID"
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
    print(f"Milles asset layout PASS: {len(items)} independent placements, reusable terrain and props")


if __name__ == "__main__":
    main()
