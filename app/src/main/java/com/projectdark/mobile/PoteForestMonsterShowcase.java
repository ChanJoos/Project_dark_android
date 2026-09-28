package com.projectdark.mobile;

import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapted test placements for every identity in the current Pote forest Monster_Master projection. */
public final class PoteForestMonsterShowcase {
  private static final Map<String,String> ART;
  private static final List<String> IDS;
  static {
    Map<String,String> art=new LinkedHashMap<>();
    art.put("POTE_RED","red_pamfet"); art.put("POTE_GREEN","green_pamfet");
    art.put("POTE_PURPLE","purple_pamfet"); art.put("POTE_SILVER","silver_pamfet");
    art.put("POTE_TREANT","trant"); art.put("POTE_ANTLION","antlion");
    art.put("POTE_GNOLL","gnoll"); art.put("POTE_WOLFRIDER","wolf_rider");
    art.put("POTE_LYCAN","lycanthrope"); art.put("POTE_ANTGIANT","ant_giant");
    art.put("POTE_SILVERWOLF","silver_wolf"); art.put("POTE_SPIRIT","brown_pote_spirit");
    art.put("POTE_MANTIS","giant_mantis");
    // No separate strong-variant concepts are present in the v0.4 art pack.
    art.put("POTE_STRONG_GNOLL","gnoll"); art.put("POTE_STRONG_WOLFRIDER","wolf_rider");
    art.put("POTE_STRONG_TREANT","trant");
    ART=Collections.unmodifiableMap(art);
    IDS=Collections.unmodifiableList(new ArrayList<>(art.keySet()));
  }

  private PoteForestMonsterShowcase(){}
  public static List<String> monsterIds(){return IDS;}
  public static boolean containsMonster(String id){return ART.containsKey(id)||"POTE_SPIRIT_TEST_B".equals(id);}
  public static String artKey(String id){return "POTE_SPIRIT_TEST_B".equals(id)?ART.get("POTE_SPIRIT"):ART.get(id);}
  public static String runtimeId(String rosterId){
    // The canonically rewarded spirit must use an isolated fixture identity in the test field.
    return "POTE_SPIRIT".equals(rosterId)?"POTE_SPIRIT_TEST_B":rosterId;
  }

  /** Keep the original adapted purple placement and spread remaining test actors across clear tiles. */
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
      result.add(new RuntimeState.Monster(runtimeId(id),entry.name+" [테스트 배치]",tile.x,tile.y,40,"B/ADAPTED_TEST_CONCEPT"));
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
