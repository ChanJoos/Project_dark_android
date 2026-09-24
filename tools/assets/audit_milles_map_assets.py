#!/usr/bin/env python3
"""Validate the reusable Milles map layout and the runtime asset contract."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PRODUCTION = ROOT / "assets/milles/production"
LAYOUT = PRODUCTION / "maps/milles_garden.json"
RENDERER = ROOT / "app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapRenderer.java"
TILES = ROOT / "app/src/main/java/com/projectdark/mobile/world/AdaptedMillesIsometricTileLayer.java"


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
        "video_reference/objects/bench_video_cutout_01.png",
        "structures/fences/OBJ_fence_01.png",
        "vegetation/trees/OBJ_tree_01.png",
    ):
        assert (PRODUCTION / rel).is_file(), f"missing reusable family: {rel}"
    renderer = RENDERER.read_text(encoding="utf-8")
    assert "maps/milles_garden.json" in renderer
    assert "garden_route_scene" not in renderer
    assert "drawPath(" not in renderer and "BitmapShader" not in renderer
    tile_source = TILES.read_text(encoding="utf-8")
    assert "traceRoadCells" in tile_source and "connectRoadCells" in tile_source
    assert "private static final float[][][] PATHS" in tile_source
    print(f"Milles asset layout PASS: {len(items)} independent placements, reusable terrain and props")


if __name__ == "__main__":
    main()
