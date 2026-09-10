# Startup crash root-cause fix

Date: 2026-09-10

Device diagnostic APK showed:
- `java.lang.IllegalStateException: default player atlas decode/shape failed`
- `CharacterRenderer.requireAtlas(CharacterRenderer.java:94)`
- constructor path `CharacterRenderer -> GameView -> MainActivity`

Root cause is therefore confirmed in the V5 embedded default player atlas runtime decode/shape path, not RuntimeState audits.

Hotfix strategy:
- restore the last runtime-safe `CharacterRenderer` + `CharacterRendererAudit` pair from main `2b66df9780142e3d84606f4ac0250dcabfa2d2b7`;
- preserve all newer World/RPG/UX integrations;
- keep MainActivity startup crash diagnostic screen as a regression guard;
- temporarily revert the V5 1.60/head-ratio atlas revision until it is reintroduced using a runtime-safe resource-loading path.

Status after code change: IMPLEMENTED; device runtime verification still required.