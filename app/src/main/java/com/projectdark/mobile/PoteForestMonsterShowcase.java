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
    // Six retained review families are explicitly registered below for the adapted Lv40 campaign; provenance remains candidate-only.
    art.put("POTE_PURPLE","purple_pamfet");
    art.put("POTE_RED","red_pamfet"); art.put("POTE_GREEN","green_pamfet");
    art.put("POTE_SILVER","silver_pamfet");
    art.put("POTE_LYCAN","lycanthrope");
    art.put("POTE_TREANT","campaign_v1/trant");art.put("POTE_ANTLION","campaign_v1/antlion");art.put("POTE_GNOLL","campaign_v1/gnoll");art.put("POTE_WOLFRIDER","campaign_v1/wolf_rider");art.put("POTE_ANTGIANT","campaign_v1/ant_giant");art.put("POTE_SILVERWOLF","campaign_v1/silver_wolf");
    art.put("POTE_STRONG_GNOLL","campaign_v1/gnoll");art.put("POTE_STRONG_WOLFRIDER","campaign_v1/wolf_rider");art.put("POTE_STRONG_TREANT","campaign_v1/trant");art.put("POTE_CAMPAIGN_ELITE_GNOLL","campaign_v1/gnoll");
    ART=Collections.unmodifiableMap(art);
    IDS=Collections.unmodifiableList(new ArrayList<>(java.util.Arrays.asList("POTE_PURPLE","POTE_RED","POTE_GREEN","POTE_SILVER","POTE_LYCAN")));
  }

  private PoteForestMonsterShowcase(){}
  public static List<String> monsterIds(){return IDS;}
  public static float bodyHeight(String id){String s=species(id);return s!=null&&s.contains("TREANT")?80f:s!=null&&(s.contains("LYCAN")||s.contains("GNOLL")||s.contains("WOLFRIDER"))?72f:48f;}
  public static String species(String id){return id==null?null:id.split("#",2)[0];}
  public static boolean containsMonster(String id){return ART.containsKey(species(id));}
  public static String artKey(String id){return ART.get(species(id));}
  public static String poseFor(RuntimeState.Monster monster){
    if(monster==null)return "idle";
    if(monster.isMoving)return "walk";
    if(monster.attackPrimed||monster.attackVisualRemaining>0f)return "attack";
    return "idle";
  }
  /** While walking, render from the committed tile-step vector, never a stale attack lock. */
  public static CharacterRenderer.Direction presentationFacing(RuntimeState.Monster monster){
    if(monster==null)return CharacterRenderer.Direction.SE;
    if(monster.isMoving){
      // The applied segment may be a collision detour from the AI's original intent. Require
      // its exact canonical vector here so a bad/stale segment cannot be rounded into a false
      // direction; RuntimeState commits facing and endpoints atomically.
      WorldMoveTargetController.Direction step=WorldMoveTargetController.Direction.between(
          monster.moveStartX,monster.moveStartY,monster.moveTargetX,monster.moveTargetY);
      return MonsterTileCenterLocomotion.facing(step);
    }
    return monster.visualFacing.presentation();
  }
  public static float attackProgress(RuntimeState.Monster monster){
    if(monster==null)return 0f;
    float progress=monster.attackPrimed
        ?0.5f*(0.24f-monster.attackWindup)/0.24f
        :0.5f+0.5f*(1f-monster.attackVisualRemaining/0.36f);
    return Math.max(0f,Math.min(1f,progress));
  }
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
    // The former farthest-point sampler scattered four candidates across the entire forest.
    // Keep the whole visual test roster near the entrance so all five are visible and can enter
    // the same playable encounter on arrival.
    List<WorldMoveTargetController.TileCenter> entryPocket=new ArrayList<>();
    for(WorldMoveTargetController.TileCenter tile:tiles){
      float d=(float)Math.hypot(tile.x-com.projectdark.mobile.world.PoteFieldDef.ENTRY_X,
          tile.y-com.projectdark.mobile.world.PoteFieldDef.ENTRY_Y);
      if(d>=72f&&d<=168f)entryPocket.add(tile);
    }
    while(chosen.size()<IDS.size()){
      WorldMoveTargetController.TileCenter best=null;float bestSpacing=-1f;
      for(WorldMoveTargetController.TileCenter candidate:entryPocket){
        if(contains(chosen,candidate))continue;
        float spacing=Float.MAX_VALUE;
        for(WorldMoveTargetController.TileCenter existing:chosen)
          spacing=Math.min(spacing,(float)Math.hypot(candidate.x-existing.x,candidate.y-existing.y));
        if(spacing>bestSpacing){best=candidate;bestSpacing=spacing;}
      }
      if(best==null)throw new IllegalStateException("Not enough clear entrance tiles for visible monster test roster");
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
