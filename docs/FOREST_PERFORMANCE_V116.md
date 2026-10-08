# V116 forest performance diagnosis

V115 phone lag remains DEVICE_FAILED_USER_REPORTED, root cause UNKNOWN without a device trace. Exact V116 attempt 77e6bcc5 / Actions37707251907 passed73 tests but was **inconclusive for overall performance**: MAP_POTE_01 render median8.772→10.394ms; MAP_POTE_03 19.259→18.254ms. Immutable pixel caching is valid (5000 exact legacy occlusion queries;67794 native scalar reads→2 bulk readbacks), but cannot alone explain the phone complaint. Floor/trail isolated draw consumed about5.7–6.4ms in native CPU evidence. These are JVM/native CPU observations, not phone FPS.

The first profiler moved a presentation interpolator from the previous map-entry location when it repositioned the logical player. Its movedPixels measurement included that initial snap; do not use its numbers as a verified continuous near-tree movement scenario. The revised harness explicitly snaps the adapter after fixture placement, asserts at least75/100 measured frames really move, and records measured travel. Identical harness runs on exact V115 fe8dc5d2 and final candidate.

Next repair: cache static soil/trail at world-aligned256px chunks with2px gutters, retain at most32chunks (8652800 CPU pixel bytes), clear on map change, bypass caching for extreme viewports. Draw only logical viewport chunks. Water and all actors remain live, all authored trail geometry/width/alpha/source/colour and collision/simulation stay unchanged. Do not recycle evicted bitmaps while hardware display lists may refer to them. An independent frozen V115 floor algorithm checks world texture/seams across four maps and fractional/integer camera positions; repeat frames must not rebuild chunks; map traversal memory bounded. Cold entry and physical phone graphics/memory remain pending.

IMPLEMENTED locally; final BUILD/NATIVE/PERFORMANCE/PHONE acceptance pending. Do not deliver the initial 77e6bcc5 release as a performance fix.

## Full-floor cache rejected, 06497242 / Actions37708099908

75 tests:74pass,1floor raster failure. The corrected profiler proves100/100 measured moving frames in both maps, identical95.406px measured travel. Native render median MAP_POTE_01 13.087→12.498ms,MAP_POTE_03 25.595→8.225ms;floor2.327→1.068ms and16.060→1.058ms. However fractional camera (.25) full-floor raster meanRGB difference1.947 exceeded the1.0 guard. No APK built/published for this source;not a deliverable.

Repair keeps the **ground shader live** for original subpixel texture phase and caches only static translucent trail paint. Same bounded world tiles/gutters/LRU;water unchanged. Frozen-raster guard is retained (not weakened). BUILD/NATIVE/PERFORMANCE acceptance pending again.
