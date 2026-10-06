#!/usr/bin/env python3
"""Project the tracked V108 mouse animation sheet into normalized runtime frames."""
from pathlib import Path
import hashlib
import json
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "assets/milles/review/interiors/v108/field_mouse_sheet.png"
OUTPUT = ROOT / "assets/milles/production/interiors/v108"
DIRECTIONS = ("nw", "ne", "sw", "se")
ACTIVITIES = ("idle", "walk_a", "walk_b", "attack")


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> None:
    source = Image.open(SOURCE).convert("RGBA")
    if source.size != (1254, 1254):
        raise SystemExit(f"unexpected source sheet size: {source.size}")
    OUTPUT.mkdir(parents=True, exist_ok=True)
    frames = []
    for row, direction in enumerate(DIRECTIONS):
        for column, activity in enumerate(ACTIVITIES):
            cell = source.crop((
                round(column * source.width / 4),
                round(row * source.height / 4),
                round((column + 1) * source.width / 4),
                round((row + 1) * source.height / 4),
            ))
            alpha = cell.getchannel("A")
            bounds = alpha.point(lambda value: 255 if value >= 128 else 0).getbbox()
            if bounds is None:
                raise SystemExit(f"empty generated frame: {direction}/{activity}")
            content = cell.crop(bounds).resize((52, 36), Image.Resampling.LANCZOS)
            tile = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
            tile.alpha_composite(content, (6, 23))
            output = OUTPUT / f"field_mouse_{direction}_{activity}.png"
            tile.save(output, optimize=True)
            frames.append({
                "file": output.name,
                "direction": direction,
                "activity": activity,
                "width": 64,
                "height": 64,
                "opaque_bounds": list(tile.getchannel("A").getbbox()),
                "sha256": digest(output),
            })
    manifest = {
        "assetSet": "V108_ADAPTED_FIELD_MOUSE_ANIMATED",
        "classification": "PROJECT_ADAPTED_GENERATED_ART",
        "sourceImage": "../../../review/interiors/v108/field_mouse_sheet.png",
        "sourceSha256": digest(SOURCE),
        "sourceDimensions": list(source.size),
        "rowDirectionOrder": list(DIRECTIONS),
        "columnActivityOrder": list(ACTIVITIES),
        "normalization": {
            "contentRect": [6, 23, 58, 59],
            "groundBaselineY": 59,
            "alphaCropThreshold": 128,
            "resampling": "Lanczos; fixed projection frame 52x36",
        },
        "assets": frames,
        "runtime": "idle when still; walk_a/walk_b alternate from animationClock; attack while attackPrimed or attackVisualRemaining > 0",
    }
    (OUTPUT / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


if __name__ == "__main__":
    main()
