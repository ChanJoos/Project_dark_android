Run from the conversation workspace with the private source in upload/ and repo in project_dark/.
Use ffmpeg fps=1 and fps=5, crop=1920:1080:210:0, scale=640:480; number frames from1 into mine205/frames/%03d.png and mine205/dense/%04d.png respectively.
Then run align.py, bridge.py, cave.py, extract.py, water.py in this order.
Intermediate mosaics include UI and private phone overlays: NEVER publish them. The extraction polygons isolate only game objects; all fully transparent RGB bytes are zeroed before publication. Registration hashes and isolated PNGs are the committed output.
Feature alignment across camera jumps is not an authoritative world map. Only the local continuous bridge/cave sections are consumed. Full layout, canal inlets and cliff continuation remain adaptations.
