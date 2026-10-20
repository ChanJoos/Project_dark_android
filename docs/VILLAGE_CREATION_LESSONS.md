# Village creation lessons — living baseline

Apply before adding or rebuilding a village. V203 extends the accepted Milles/Abel/Rucesion lessons; it does not replace the approved character or movement pipeline.

1. Inspect the user's source footage first. Decompose source pixels into materials and independent objects with crop/alpha/pivot provenance. A screenshot is not a map. Reconstruct cut silhouettes from multiple observed camera positions before inventing missing art; keep inferred topology and unseen joins explicitly ADAPTED.
2. Reuse RuntimeState, WorldRuntimeAdapter, 64×32 four diagonal movement, the accepted0.60s tile step, camera and paper-doll actors. Never build a separate mini-runtime or draw placeholder buildings in GameView.
3. Register the town in CampaignWorld, PoteCampaignMapDef, travel manager network, service definitions/identities, save validation and GameView scene orchestration. Missing registration must not silently select the default forest. New towns must not grant levels, quests, Gold or items through travel.
4. Keep native facade scale and source doorway pivots. Enter at the door image's threshold; return outside the same door without immediate re-entry. Verify full route, portal visibility, walls, building/tree feet, bridges and NPC/customer clearance.
5. Use independent sprites plus shared ground depth sorting. A long rock wall cannot use one far-end depth for its entire face: sort connected material strips by their local ground depth so actors in front remain visible.
6. Continuous world-anchored materials prevent alternating diamond-tone ground grids. Directional water ripples should not use MIRROR in both axes: the reflected pattern creates conspicuous symmetric diamonds. Preserve source ripple direction and inspect full-size native screenshots.
7. Ground navigation/collision indices are static per map. Actors/reserved destinations remain live. Prewarm scene textures; bound floor chunks to32/8652800CPU pixel bytes and clear on scene departure/interior entry. Check warm frames and12roundtrips. Host statistics are not handsetFPS.
8. New travel entries must remain inside the modal and have unique reachable hit targets; test all existing destinations, including forest/Horror. Service scope follows the user: V203 Mine has only inn and reagent shop, plus a return travel manager.
9. Inn/shop services use existing TownCommerce, actual inventory/resource/Gold changes and transactional save paths. Verify real NPC clicks, confirmation, customer walking, exits, town/interior save and restart; a displayed label is not a working service.
10. Separate IMPLEMENTED, BUILD VERIFIED, HOST NATIVE RUNTIME VERIFIED and HANDSET/USER VISUAL ACCEPTED. Review actual standard/wide landmark and interior captures; CI success alone is not visual acceptance. Exact APK source/run/artifact/size/SHA must be recorded separately from later documentation commits.
11. Do not upload raw phone/YouTube recordings or reference frames containing notifications. Preserve their private source hash and extraction recipe; publish only the user-authorized, isolated game assets without unrelated overlays.

V203 evidence and limitations: verification/MINE_TOWN_V203_BUILD.json and master/source/mine_v203/assets.json. The compact two-bank layout and service assignments are adapted from observed mining-town motifs; original full map/warp coordinates and unobserved Mine interiors are not recovered facts.

12. Adding a town also changes the human NPC registry. Extend the roster regression with explicit stable travel/service IDs, uniqueness, labels and source registrations; do not leave a historical total (V203:40→43) that blocks common CI after focused town tests pass.
