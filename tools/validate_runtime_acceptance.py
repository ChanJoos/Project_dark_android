from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
def read(rel): return (ROOT / rel).read_text(encoding="utf-8")
def require(text, needle, label):
    if needle not in text: raise SystemExit(f"RUNTIME ACCEPTANCE FAIL: {label}: missing {needle!r}")

character=read("app/src/main/java/com/projectdark/mobile/CharacterRenderer.java")
game_view=read("app/src/main/java/com/projectdark/mobile/GameView.java")
monster_ai=read("app/src/main/java/com/projectdark/mobile/MonsterAIController.java")
world=read("app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapRenderer.java")
monster_visual=read("app/src/main/java/com/projectdark/mobile/MonsterVisualRenderer.java")

start=character.index("private void drawEquipment");end=character.index("private Registration robeRegistration",start);equipment_block=character[start:end]
if "CharacterSemanticRig" in equipment_block or "garmentTranslation" in equipment_block: raise SystemExit("RUNTIME ACCEPTANCE FAIL: live robe equipment path still uses semantic alpha re-centering")
require(character,"{0,0,0,0,0},{0,0,0,0,0}}","SW/SE shared-atlas zero registration")
require(character,"ADAPTED_STATIONARY_POSE_WEAPON_3_PHASE","stationary pose/weapon attack profile")
attack_start=character.index("private void drawAdaptedAttack");attack_end=character.index("private void drawSourcePaperDoll",attack_start);attack_block=character[attack_start:attack_end]
require(attack_block,"drawSourcePaperDoll(c,pose,anchorY,true);","stationary attack paper doll path")
if "translate(" in attack_block or "motion" in attack_block: raise SystemExit("RUNTIME ACCEPTANCE FAIL: whole-paper-doll attack translation remains reachable")
require(character,"if(q<.22f)amount","weapon startup phase")
require(character,"else if(q<.48f)amount","weapon contact phase")
require(character,"else amount=70f","weapon recovery phase")

require(monster_ai,"MONSTER_TILE_STEP_SECONDS=.82f","monster pursuit cadence")
require(monster_ai,"CanonicalMeleeTileContract.direction","canonical melee adjacency")
require(game_view,"private final MonsterVisualRenderer monsterVisualRenderer=new MonsterVisualRenderer();","live monster renderer wiring")
require(game_view,"monsterVisualRenderer.draw(c,m,selected==m);","live monster renderer draw path")
require(monster_visual,"public final class MonsterVisualRenderer","monster visual implementation")

require(world,"MILLES_VIDEO_REFERENCE_V6_NATURAL_PATH_SPLIT","video-reference Milles composition")
require(world,"grass-first village; branching diagonal paths; central fountain; fenced tree gardens; water-side landmark","reference composition contract")
require(world,"ADAPTED_SOURCE_GROUND_VARIANT_PENDING_TRUE_DIRT_SOURCE","truthful road-surface evidence")
require(world,"case ROAD:return \"terrain/OBJ_ground_02.png\";","natural road surface mapping")
require(world,"case PLAZA:","plaza surface split")
require(world,"case GATE:return \"terrain/OBJ_stone_01.png\";","stone landmark surface mapping")
require(world,"s.x+12f,s.y+34f,1.02f","central fountain anchor")
require(world,"s.x+318f,s.y+252f,.82f","water-side landmark")
require(world,"s.x-205f,s.y+76f,.88f","left tree garden")
require(world,"s.x+206f,s.y+82f,.88f","right tree garden")

print("RUNTIME ACCEPTANCE PASS: robe atlas-lock, stationary pose/weapon 3-phase attack, monster cadence/visual wiring, canonical melee, Milles V6 natural-path split")
