#!/usr/bin/env bash
# Run from conversation workspace. Raw footage/frames remain private.
set -euo pipefail
out=project_dark/app/src/main/assets/mine-v206
mkdir -p "$out"
ffmpeg -loglevel error -y -i mine205/frames/065.png -vf crop=68:30:226:54 "$out/meadow0.png"
ffmpeg -loglevel error -y -i mine205/frames/038.png -vf crop=62:36:378:236 "$out/meadow1.png"
ffmpeg -loglevel error -y -i mine205/frames/065.png -vf crop=64:32:328:246 "$out/meadow2.png"
ffmpeg -loglevel error -y -i mine205/frames/038.png -vf crop=30:60:410:282 "$out/flowers.png"
