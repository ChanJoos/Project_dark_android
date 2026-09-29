package com.projectdark.mobile;

import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapted test placements for currently available generated 12-pose candidate sets. */
public final class PoteForestMonsterShowcase {
  public static final String ASSET_ROOT="pote_monsters_generated_v1/sprites/";
  private static final Map<String,String> ART;
  private static final List<String> IDS;
  static {
    Map<String,String> art=new LinkedHashMap<>();
    // Only species with all 12 current generated candidates (idle/walk/attack × NW/NE/SW/SE).
    // Historical monster_test_v04 mappings are intentionally not part of the runtime registry.
    art.put("POTE_PURPLE","purple_pamfet");
    art.put("POTE_RED","red_pamfet"); art.put("POTE_GREEN","green_pamfet");
    art.put("POTE_SILVER","silver_pamfet");
    art.put("POTE_LYCAN","lycanthrope");
    ART=Collections.unmodifiableMap(art);
    IDS=Collections.unmodifiableList(new ArrayList<>(art.keySet()));
  }

  private PoteForestMonsterShowcase(){}
  public static List<String> monsterIds(){return IDS;}
  public static boolean containsMonster(String id){return ART.containsKey(id);}
  public static String artKey(String id){return ART.get(id);}
  public static String assetPath(String monsterId,String state,CharacterRenderer.Direction direction){
    String species=artKey(monsterId);
    if(species==null||direction==null)return null;
    String pose="walk".equals(state)||"attack".equals(state)?state:"idle";
    String dir;
    switch(direction){
      case NW:dir="nw";break;
      case NE:dir="ne";break;
      case SW:dir="sw";break;
      case SE:dir="se";break;
      default:return null;
    }
    return ASSET_ROOT+species+"/"+pose+"_"+dir+".png";
  }

  /** Spread only art-registered test actors across clear tiles; this does not declare canon spawns. */
  public static List<RuntimeState.Monster> instantiate(PoteMonsterRoster roster,List<WorldMoveTargetController.TileCenter> tiles){
    if(roster==null||tiles==null)throw new IllegalArgumentException("roster/tiles");
    List<WorldMoveTargetController.TileCenter> chosen=new ArrayList<>();
    WorldMoveTargetController.TileCenter purple=nearest(tiles,704f,480f);
    if(purple!=null)chosen.add(purple);
    while(chosen.size()<IDS.size()){
      WorldMoveTargetController.TileCenter best=null;float bestSpacing=-1f;
      for(WorldMoveTargetController.TileCenter candidate:tiles){
        if(contains(chosen,candidate))continue;
        float spacing=Float.MAX_VALUE;
        for(WorldMoveTargetController.TileCenter existing:chosen)
          spacing=Math.min(spacing,(float)Math.hypot(candidate.x-existing.x,candidate.y-existing.y));
        if(spacing>bestSpacing){best=candidate;bestSpacing=spacing;}
      }
      if(best==null)throw new IllegalStateException("Not enough clear forest tiles for test roster");
      chosen.add(best);
    }
    List<RuntimeState.Monster> result=new ArrayList<>();
    for(int i=0;i<IDS.size();i++){
      String id=IDS.get(i);PoteMonsterRoster.Entry entry=roster.find(id);
      if(entry==null)throw new IllegalStateException("Roster is missing "+id);
      WorldMoveTargetController.TileCenter tile=chosen.get(i);
      result.add(new RuntimeState.Monster(id,entry.name+" [테스트 배치]",tile.x,tile.y,40,"B/ADAPTED_GENERATED_CANDIDATE"));
    }
    return Collections.unmodifiableList(result);
  }

  private static WorldMoveTargetController.TileCenter nearest(List<WorldMoveTargetController.TileCenter> tiles,float x,float y){
    WorldMoveTargetController.TileCenter best=null;float d=Float.MAX_VALUE;
    for(WorldMoveTargetController.TileCenter t:tiles){float q=(t.x-x)*(t.x-x)+(t.y-y)*(t.y-y);if(q<d){d=q;best=t;}}
    return best;
  }
  private static boolean contains(List<WorldMoveTargetController.TileCenter> tiles,WorldMoveTargetController.TileCenter c){
    for(WorldMoveTargetController.TileCenter t:tiles)if(t.x==c.x&&t.y==c.y)return true;return false;
  }
}
