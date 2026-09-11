#!/usr/bin/env python3
"""Audit PROJECT DARK starter peasant sprite atlas before runtime promotion.

Usage:
  python3 tools/audit_peasant_sprite.py path/to/player_peasant_idle_walk.png

Hard gates use Python stdlib only. If Pillow is available, deeper alpha/bbox/anchor
checks are added, but Pillow is not required to run the structural audit.
"""
from __future__ import annotations

import argparse
import struct
import sys
from pathlib import Path

EXPECTED_W = 120
EXPECTED_H = 128
CELL_W = 24
CELL_H = 32
ROWS = ("NW", "NE", "SW", "SE")
COLS = ("IDLE", "WALK_1", "WALK_2", "WALK_3", "WALK_4")
ANCHOR_X = 12
ANCHOR_Y = 30
PNG_SIG = b"\x89PNG\r\n\x1a\n"


def read_ihdr(path: Path):
    data = path.read_bytes()
    if not data.startswith(PNG_SIG):
        raise ValueError("not a PNG file")
    if len(data) < 33 or data[12:16] != b"IHDR":
        raise ValueError("missing PNG IHDR")
    w, h, bit_depth, color_type, compression, filtering, interlace = struct.unpack(
        ">IIBBBBB", data[16:29]
    )
    return {
        "width": w,
        "height": h,
        "bit_depth": bit_depth,
        "color_type": color_type,
        "compression": compression,
        "filter": filtering,
        "interlace": interlace,
    }


def deep_audit_with_pillow(path: Path):
    try:
        from PIL import Image
    except Exception:
        return ["Pillow unavailable: skipped deep per-cell alpha/bbox/anchor audit"], []

    warnings, failures = [], []
    im = Image.open(path).convert("RGBA")
    alpha = im.getchannel("A")
    if alpha.getextrema() == (255, 255):
        failures.append("atlas has no transparent pixels; production atlas must use alpha background")

    row_anchor_occupancy = []
    for r, row_name in enumerate(ROWS):
        for c, col_name in enumerate(COLS):
            box = (c * CELL_W, r * CELL_H, (c + 1) * CELL_W, (r + 1) * CELL_H)
            cell = im.crop(box)
            a = cell.getchannel("A")
            bbox = a.getbbox()
            label = f"{row_name}/{col_name}"
            if bbox is None:
                failures.append(f"{label}: empty cell")
                continue
            left, top, right, bottom = bbox
            if right - left < 4 or bottom - top < 8:
                failures.append(f"{label}: occupied bbox too small {bbox}")
            if bottom > 31:
                failures.append(f"{label}: opaque pixels extend to row 31; canonical foot anchor is y=30")
            # Check whether there is at least one opaque-ish pixel on the canonical foot line.
            px = cell.load()
            on_anchor_line = any(px[x, ANCHOR_Y][3] >= 64 for x in range(CELL_W))
            if not on_anchor_line:
                warnings.append(f"{label}: no visible pixel on canonical foot line y={ANCHOR_Y}")
            row_anchor_occupancy.append((label, bbox, on_anchor_line))

    # IDLE silhouettes should not differ wildly in occupied height across facings.
    idle_heights = []
    for r, row_name in enumerate(ROWS):
        cell = im.crop((0, r * CELL_H, CELL_W, (r + 1) * CELL_H))
        bbox = cell.getchannel("A").getbbox()
        if bbox:
            idle_heights.append((row_name, bbox[3] - bbox[1]))
    if idle_heights:
        heights = [h for _, h in idle_heights]
        if max(heights) - min(heights) > 5:
            warnings.append(f"IDLE occupied heights vary by >5 px: {idle_heights}")

    return warnings, failures


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("png", type=Path)
    args = parser.parse_args()
    path = args.png

    failures = []
    warnings = []
    if not path.is_file():
        print(f"FAIL: file not found: {path}")
        return 2

    try:
        ihdr = read_ihdr(path)
    except Exception as exc:
        print(f"FAIL: {exc}")
        return 2

    if (ihdr["width"], ihdr["height"]) != (EXPECTED_W, EXPECTED_H):
        failures.append(
            f"atlas must be {EXPECTED_W}x{EXPECTED_H}, got {ihdr['width']}x{ihdr['height']}"
        )
    if ihdr["bit_depth"] != 8:
        warnings.append(f"expected 8-bit PNG channels, got bit depth {ihdr['bit_depth']}")
    if ihdr["color_type"] not in (4, 6):
        warnings.append(
            f"PNG color type {ihdr['color_type']} does not guarantee per-pixel alpha; prefer RGBA/color type 6"
        )
    if ihdr["interlace"] != 0:
        warnings.append("interlaced PNG is unnecessary for sprite atlas; prefer non-interlaced")

    deep_warnings, deep_failures = deep_audit_with_pillow(path)
    warnings.extend(deep_warnings)
    failures.extend(deep_failures)

    print("PROJECT DARK PEASANT SPRITE AUDIT")
    print(f"file={path}")
    print(f"atlas={ihdr['width']}x{ihdr['height']} cell={CELL_W}x{CELL_H} rows={','.join(ROWS)}")
    print(f"columns={','.join(COLS)} canonical_anchor=({ANCHOR_X},{ANCHOR_Y})")
    for w in warnings:
        print(f"WARN: {w}")
    for f in failures:
        print(f"FAIL: {f}")

    if failures:
        print("RESULT=REJECT")
        return 1
    print("RESULT=STRUCTURAL_PASS_VISUAL_GATE_STILL_REQUIRED")
    return 0


if __name__ == "__main__":
    sys.exit(main())
