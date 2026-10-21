# V213 temple route and Milles presentation repair

User report: DEVICE_FAILED (user-reported), entrance differs from recording; portals and NPC missing. Installed APK identity and physical-device root cause UNKNOWN. User approves existing interior room appearance; keep original three room cells/atlas intact. Milles visual revision authorized; preserve other towns, services, combat, progression and save semantics.

Reproduced by code/native baseline inspection: hub explicitly omits NPC drawing/input and reaches Seo only at an unmarked exact coordinate. Original entrance white recoloring/large blur washes source art; nearby trees overlap. Milles replaces all tree silhouettes with one generated willow; river renderer performs thousands of segment-distance checks every draw. These are code findings, not phone FPS measurements.

Candidate: distinct visible/tappable forward and return portals using existing source portal sprite; source guide NPC visible in hub at a separate walkable-adjacent position. Lobby→hub→Seo→hub→lobby→Milles, NPC HP/MP menu and exact user prices preserved. Portal coordinates/labels and hub guide placement are explicit mobile adaptations, not recovered current-server topology. Other deity room sources are not available and are not fabricated.

Exterior retains source circular platform/three arches at X1952,Y1104 (128px west of V211; source arches now remain inside world bounds), removes white beam recoloring and excessive blur, aligns arch collision with displayed scale, moves two interfering tree contacts with their visuals. Source RGB retained for light matte; extracted alpha/pulse remains adapted. Exterior surrounding current village is not an exact source-video reconstruction; partially obscured source details remain unresolved.

Milles uses recorded grass material, broader grass/soil edge transition, distinct retained tree silhouettes without repeated generated-willow conversion; building/door/NPC positions and existing cabin sources retained. Static river ripple geometry is built once, shaders retained between frames; movement remains 0.60s. No art regeneration or other-town change. Original source files preserved.

IMPLEMENTED; local temple source/grid hash audit and 368-object layout audit PASS. Exact-SHA Android build, production touch routes, HP/MP/save, all-district native review and physical handset pending. Existing PR197 branch; main41fe6def unchanged.
