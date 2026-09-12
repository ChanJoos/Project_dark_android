# PROJECT DARK — Milles Visual Constitution

## Source of truth

1. User-supplied original Milles screenshots are the primary visual reference.
2. The user-approved standalone weapon-shop image is `MILLES_MASTER_01` and is the production visual master once its exact binary is committed.
3. Never replace an approved/master asset by regeneration. New work must advance the registry to the next unfinished asset ID.

## Building grammar

- Classic Milles 2.5D/isometric pixel-art silhouette.
- Heavy log/timber wall language, compact broad footprint, substantial gabled roof.
- Roofs share one material family but MUST vary naturally by building: gray-purple, blue-gray, dark gray, brown-gray, red-brown, weathered gray-purple; vary tile size, ridge, age, chimney/dormer placement and roof height.
- Do not drift into generic European high-fantasy, Mir-like, chibi, or modern concept-art architecture.
- Functional identity should come from restrained signs, entrance treatment and exterior props, not a wholesale style change.

## Asset production rules

- Produce one independent object asset at a time; no multi-object generation sheet is a production source.
- No grid slicing of catalog/concept sheets.
- No mirrored/copied assets used to inflate asset count.
- Final production files must be real RGBA PNG with transparent background and no colored/black halo.
- Remove labels, numbers, UI and text contamination.
- Trim transparent bounds and store an anchor appropriate to category (building entrance/footprint, tree trunk base, lamp post base, etc.).
- Candidate assets never overwrite approved assets.

## QA gate

Before an asset can become `APPROVED`, verify: valid PNG decode; RGBA alpha; transparent exterior; no edge clipping; no unintended neighboring object; no text/UI contamination; normalized bounds/anchor; SHA-256 recorded; perceptual duplicate check against all approved assets; visual review against `MILLES_MASTER_01` and original Milles references.

A final pack may contain only QA-passed/approved assets. A failed asset must not be counted toward the target.
