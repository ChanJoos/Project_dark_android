package com.projectdark.mobile;

import com.projectdark.mobile.world.*;
import java.lang.reflect.*;
import java.util.*;
import org.json.*;

/** Explicit capture-only staging. Not compiled into debug/release. No combat rule overrides. */
public final class CaptureMineEncounter {
  public static final int COUNT=15, ORIGIN_U=90, ORIGIN_V=42;
  public static final String LABEL="촬영 전용 테스트 · 기존 몬스터 15마리";
  static Object field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
  static RuntimeState state(GameView v)throws Exception{return (RuntimeState)field(v,"state");}
  public static WorldMoveTargetController.TileCenter player(){return MineHuntGeometry.at(ORIGIN_U+6,ORIGIN_V+4);}
  public static WorldMoveTargetController.TileCenter position(int i){int row=i/5,col=i%5-2;return MineHuntGeometry.at(ORIGIN_U+2*row+col,ORIGIN_V+row-col);}
  @SuppressWarnings("unchecked") public static JSONObject stage(GameView view)throws Exception{
    RuntimeState s=state(view);RpgProgressionState r=s.rpg();
    if(!"MAGE".equals(r.currentJobCode())||r.normalLevel()!=99||!r.firstAdvanced()||r.str()!=255||r.intel()!=255||r.wis()!=255||r.con()!=255||r.dex()!=255||s.player().hp!=1000000||s.player().mp!=1000000)
      throw new IllegalStateException("Restore the authorized dedicated MAGE 99/promoted/255/1M profile first");
    if(!(Boolean)field(view,"inPoteField"))throw new IllegalStateException("Dedicated campaign profile required");
    Method change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class);change.setAccessible(true);change.invoke(view,MineHuntGeometry.ID,false);
    List<RuntimeState.Monster> ordinary=new ArrayList<>(s.monsters());
    List<RuntimeState.Monster> staged=(List<RuntimeState.Monster>)field(s,"monsters");staged.clear();
    WorldMoveTargetController.TileCenter p=player();s.player().x=p.x;s.player().y=p.y;
    JSONArray actors=new JSONArray();
    for(int i=0;i<COUNT;i++){
      RuntimeState.Monster old=ordinary.get(i);WorldMoveTargetController.TileCenter at=position(i);
      if(!MineHuntGeometry.inside(at.x,at.y)||s.blocked(at.x,at.y))throw new IllegalStateException("Blocked staged cell "+i);
      RuntimeState.Monster m=new RuntimeState.Monster(old.id,old.name,at.x,at.y,old.maxHp,old.assetStatus);
      m.level=old.level;m.campaignRewardProfileId=old.campaignRewardProfileId;m.respawnSeconds=old.respawnSeconds;staged.add(m);
      actors.put(new JSONObject().put("id",m.id).put("x",m.x).put("y",m.y).put("hp",m.hp).put("maxHp",m.maxHp).put("profile",m.campaignRewardProfileId));
    }
    WorldRuntimeAdapter world=(WorldRuntimeAdapter)field(view,"poteFieldAdapter");world.cancel();world.snapCameraToPlayer();view.invalidate();
    return new JSONObject().put("label",LABEL).put("staged",true).put("count",COUNT).put("map",MineHuntGeometry.ID).put("playerX",p.x).put("playerY",p.y).put("monsters",actors).put("activation","Explicit capture-only activity; Start button resumes ordinary GameView simulation").put("rules","Existing species profiles, AI, collision, HP, status, damage, skills and respawn unchanged; only initial encounter list/positions staged. Not ordinary travel footage.");
  }
  private CaptureMineEncounter(){}
}
