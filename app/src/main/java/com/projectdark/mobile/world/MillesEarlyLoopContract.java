package com.projectdark.mobile.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Bundle-B early Milles loop contract.
 *
 * This does not claim verified original Milles geometry. Runtime coordinates reuse the current
 * [ADAPTED]/[B] production fixture while semantic roles are backed by the repository Master data:
 * C01 = NPC -> move -> basic combat -> return; early economy = hunt -> sell -> potion/basic gear.
 */
public final class MillesEarlyLoopContract {
  public enum Role { SPAWN, QUEST_NPC, HUNTING, SUPPLY, RETURN_NPC }

  public static final class Stop {
    public final String id,actorId,roleLabel,evidence,status;
    public final Role role;
    public final float x,y;
    Stop(String id,String actorId,Role role,String roleLabel,float x,float y,String evidence,String status){
      this.id=id;this.actorId=actorId;this.role=role;this.roleLabel=roleLabel;this.x=x;this.y=y;
      this.evidence=evidence;this.status=status;
    }
  }

  private static final List<Stop> STOPS=Collections.unmodifiableList(Arrays.asList(
      new Stop("spawn",null,Role.SPAWN,"밀레스 시작",620f,560f,"B+O / ADAPTED_GEOMETRY","READY"),
      new Stop("quest_npc","milles_guide_proto",Role.QUEST_NPC,"초반 퀘스트 NPC",665f,615f,"B / ADAPTED_GEOMETRY","READY"),
      new Stop("hunting","combat_dummy_01",Role.HUNTING,"초반 사냥 지점",1315f,715f,"B / ADAPTED_GEOMETRY","READY"),
      new Stop("supply","milles_market_proto",Role.SUPPLY,"상점·보급 지점",1900f,820f,"B / ADAPTED_GEOMETRY","READY"),
      new Stop("return_npc","milles_guide_proto",Role.RETURN_NPC,"퀘스트 복귀",665f,615f,"B / ADAPTED_GEOMETRY","READY")));

  private MillesEarlyLoopContract(){}
  public static List<Stop> stops(){return STOPS;}
  public static Stop byRole(Role role){for(Stop s:STOPS)if(s.role==role)return s;return null;}

  public static boolean verify(RuntimeStateView world){
    if(world==null||STOPS.size()!=5)return false;
    for(Stop s:STOPS){
      if(s.actorId==null)continue;
      if(s.role==Role.HUNTING){if(!world.hasMonster(s.actorId))return false;}
      else if(!world.hasNpc(s.actorId))return false;
    }
    return byRole(Role.QUEST_NPC).actorId.equals(byRole(Role.RETURN_NPC).actorId);
  }

  /** Tiny read-only seam so the contract can be audited without owning RuntimeState. */
  public interface RuntimeStateView {
    boolean hasNpc(String id);
    boolean hasMonster(String id);
  }
}
