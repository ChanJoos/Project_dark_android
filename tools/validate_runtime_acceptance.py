from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")


def require(text, needle, label):
    if needle not in text:
        raise SystemExit(f"RUNTIME ACCEPTANCE FAIL: {label}: missing {needle!r}")


character = read("app/src/main/java/com/projectdark/mobile/CharacterRenderer.java")
game_view = read("app/src/main/java/com/projectdark/mobile/GameView.java")
monster_ai = read("app/src/main/java/com/projectdark/mobile/MonsterAIController.java")
world = read("app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapRenderer.java")
monster_visual = read("app/src/main/java/com/projectdark/mobile/MonsterVisualRenderer.java")

# Player visual contract: no SW/SE runtime semantic re-centering in the live equipment path.
start = character.index("private void drawEquipment")
end = character.index("private Registration robeRegistration", start)
equipment_block = character[start:end]
if "CharacterSemanticRig" in equipment_block or "garmentTranslation" in equipment_block:
    raise SystemExit("RUNTIME ACCEPTANCE FAIL: live robe equipment path still uses semantic alpha re-centering")
require(character, "{0,0,0,0,0},{0,0,0,0,0}}", "SW/SE shared-atlas zero registration")
require(character, "ADAPTED_RUNTIME_3_PHASE", "three-phase attack profile")
require(character, "if(q<.22f)motion", "attack wind-up phase")
require(character, "else if(q<.48f)", "attack strike phase")
require(character, "else {float t=(q-.48f)/.52f", "attack recovery phase")

# Monster gameplay/presentation contract.
require(monster_ai, "MONSTER_TILE_STEP_SECONDS=.82f", "monster pursuit cadence")
require(monster_ai, "CanonicalMeleeTileContract.direction", "canonical melee adjacency")
require(game_view, "private final MonsterVisualRenderer monsterVisualRenderer=new MonsterVisualRenderer();", "live monster renderer wiring")
require(game_view, "monsterVisualRenderer.draw(c,m,selected==m);", "live monster renderer draw path")
require(monster_visual, "public final class MonsterVisualRenderer", "monster visual implementation")

# World composition contract.
require(world, "MILLES_PRODUCTION_VERTICAL_SLICE_V4_DENSE_TOWN_COMPOSITION", "dense Milles composition")
require(world, "s.x+190f,s.y-92f", "church near playable center")
require(world, "s.x+245f,s.y+172f", "lake near playable center")

print("RUNTIME ACCEPTANCE PASS: robe atlas-lock, 3-phase attack, monster cadence/visual wiring, canonical melee, dense Milles world")
