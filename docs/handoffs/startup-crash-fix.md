# Startup crash fix handoff

Device evidence confirmed the immediate launch crash was `CharacterRenderer.requireAtlas` rejecting the default player atlas (`default player atlas decode/shape failed`).

Hotfix branch restores the last runtime-safe CharacterRenderer/CharacterRendererAudit pair while keeping newer World/RPG/UX code intact. MainActivity diagnostic fallback remains enabled so any subsequent startup exception is visible rather than a silent process exit.

The 1.60 V5 player atlas revision is temporarily rolled back and must be reintroduced later through a verified resource-loading path, with device runtime verification before release.