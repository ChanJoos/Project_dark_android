# PROJECT DARK — Art Direction

Status: WORKING VISUAL CONTRACT / Visual Designer owned
Updated: 2026-09-11

## Field projection
- World grid: 64×32 px, 2:1 isometric diamond.
- Ground, road, building footprint, prop footprint and shadows share the same projection.
- Objects use a bottom/foot depth anchor; renderer depth-sorts by projected foot Y.
- Final production visuals are transparent raster sprites, not Canvas primitive drawings.

## Character
- Native frame: 24×32 px; runtime scale 1.50; nearest-neighbor only.
- Field directions: NW / NE / SW / SE, each visually authored/validated; unsafe mirroring is prohibited.
- Foot baseline for the current character family: y=29 within the 32 px frame, with transparent padding below.
- Head/neck/torso must form one connected silhouette in every frame. No floating part assembly.
- Martial artist is unarmed; shield may be shown where the accepted reference supports it.

## Pixel language
- Use clustered pixels rather than single-pixel noise.
- Dark warm-neutral outline; restrained blue-gray cloth family; warm skin; brown leather/wood accents.
- One consistent upper-left key-light assumption for new ADAPTED material unless source evidence requires otherwise.
- Avoid filtered scaling and subpixel blur. Runtime sprites retain hard alpha edges.

## Production gate
Every handoff is reviewed for: silhouette/readability, anatomy/proportion, perspective/projection, scale consistency, palette harmony, lighting/material, pixel density/clustering, alpha/crop cleanliness, anchor/depth correctness, animation/frame continuity. Average must be >=8.5/10 and every category >=7/10. A failed study may remain in `design/visual/studies/` but may not be handed to Character/World as a runtime asset.
