package com.projectdark.mobile;

/** Structural audit for renderer-owned NPC/monster visual presentation contracts. */
public final class WorldEntityPresentationAudit {
  private WorldEntityPresentationAudit(){}

  public static boolean passes(){
    if(WorldEntityPresentationRenderer.Kind.values().length!=2)return false;
    if(!"PENDING_CROP".equals(WorldEntityPresentationRenderer.ASSET_STATUS))return false;
    if(WorldEntityPresentationRenderer.NPC_RENDER_SCALE<=0f||WorldEntityPresentationRenderer.NPC_RENDER_SCALE>=1f)return false;
    if(WorldEntityPresentationRenderer.MONSTER_RENDER_SCALE<=0f||WorldEntityPresentationRenderer.MONSTER_RENDER_SCALE>=1f)return false;
    if(WorldEntityPresentationRenderer.NPC_SHADOW_SCALE>=WorldEntityPresentationRenderer.NPC_RENDER_SCALE)return false;
    if(WorldEntityPresentationRenderer.MONSTER_SHADOW_SCALE>=WorldEntityPresentationRenderer.MONSTER_RENDER_SCALE)return false;
    if(WorldEntityPresentationRenderer.LOGICAL_FOOT_ANCHOR_Y!=0f)return false;

    CharacterRenderer.Direction[] dirs=CharacterRenderer.Direction.values();
    if(dirs.length!=4)return false;
    for(CharacterRenderer.Direction d:dirs){
      if(WorldEntityPresentationRenderer.directionToward(0f,0f,
          d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW?-1f:1f,
          d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE?-1f:1f,
          null)!=d)return false;
    }

    RuntimeState.Monster m=new RuntimeState.Monster("audit","audit",0f,0f,1,"PENDING_CROP");
    m.state=RuntimeState.Monster.State.CHASE;
    if(WorldEntityPresentationRenderer.presentationState(m)!=CharacterRenderer.State.WALK)return false;
    m.state=RuntimeState.Monster.State.ATTACK;
    if(WorldEntityPresentationRenderer.presentationState(m)!=CharacterRenderer.State.ATTACK)return false;
    m.hitFlash=.1f;
    if(WorldEntityPresentationRenderer.presentationState(m)!=CharacterRenderer.State.HIT)return false;
    m.hitFlash=0f;m.alive=false;m.state=RuntimeState.Monster.State.DEAD;
    if(WorldEntityPresentationRenderer.presentationState(m)!=CharacterRenderer.State.DEAD)return false;
    return true;
  }

  public static String summary(){
    return "kinds="+WorldEntityPresentationRenderer.Kind.values().length+
        ",directions="+CharacterRenderer.Direction.values().length+
        ",npcScale="+WorldEntityPresentationRenderer.NPC_RENDER_SCALE+
        ",monsterScale="+WorldEntityPresentationRenderer.MONSTER_RENDER_SCALE+
        ",anchorY="+WorldEntityPresentationRenderer.LOGICAL_FOOT_ANCHOR_Y+
        ",assets="+WorldEntityPresentationRenderer.ASSET_STATUS;
  }
}
