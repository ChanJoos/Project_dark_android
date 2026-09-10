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
    if(!CharacterVisualSourceRegistry.preservesEvidenceGate())return false;

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

    DirectionalVisualBinding binding=new DirectionalVisualBinding();
    if(!binding.stateUnresolved(CharacterRenderer.State.IDLE))return false;
    binding.bind(CharacterRenderer.State.IDLE,new CharacterRenderer.DirectionalVisualSet(
        "npc_idle_nw",DirectionalVisualBinding.PENDING_CROP,null,"npc_idle_se"));
    if(binding.resolvedDirectionCount(CharacterRenderer.State.IDLE)!=2)return false;
    if(!"npc_idle_nw".equals(binding.resolve(CharacterRenderer.State.IDLE,CharacterRenderer.Direction.NW)))return false;
    if(binding.resolve(CharacterRenderer.State.IDLE,CharacterRenderer.Direction.NE)!=null)return false;

    LayeredDirectionalVisualBinding layered=new LayeredDirectionalVisualBinding();
    if(!layered.unresolved())return false;
    DirectionalVisualBinding body=new DirectionalVisualBinding().bind(CharacterRenderer.State.IDLE,
        new CharacterRenderer.DirectionalVisualSet("body_nw",null,null,null));
    DirectionalVisualBinding hair=new DirectionalVisualBinding().bind(CharacterRenderer.State.IDLE,
        new CharacterRenderer.DirectionalVisualSet("hair_nw",DirectionalVisualBinding.PENDING_CROP,null,null));
    layered.bind(CharacterRenderer.Layer.BODY,body).bind(CharacterRenderer.Layer.HAIR,hair);
    if(layered.unresolved())return false;
    if(layered.resolvedLayerCount(CharacterRenderer.State.IDLE,CharacterRenderer.Direction.NW)!=2)return false;
    if(layered.resolvedLayerCount(CharacterRenderer.State.IDLE,CharacterRenderer.Direction.NE)!=0)return false;

    WorldEntityPresentationRenderer.Pose pose=new WorldEntityPresentationRenderer.Pose(
        WorldEntityPresentationRenderer.Kind.NPC,0f,0f,CharacterRenderer.Direction.NW,
        CharacterRenderer.State.IDLE,0f,0f,1f,CharacterRenderer.EffectFamily.NONE,false,false,
        "PENDING_CROP",null,binding,layered);
    if(!"npc_idle_nw".equals(WorldEntityPresentationRenderer.resolvedVisualRef(pose)))return false;
    if(!"body_nw".equals(WorldEntityPresentationRenderer.resolvedLayerVisualRef(pose,CharacterRenderer.Layer.BODY)))return false;
    if(!"hair_nw".equals(WorldEntityPresentationRenderer.resolvedLayerVisualRef(pose,CharacterRenderer.Layer.HAIR)))return false;
    if(WorldEntityPresentationRenderer.resolvedLayerVisualRef(pose,CharacterRenderer.Layer.WEAPON)!=null)return false;
    return true;
  }

  public static String summary(){
    return "kinds="+WorldEntityPresentationRenderer.Kind.values().length+
        ",directions="+CharacterRenderer.Direction.values().length+
        ",npcScale="+WorldEntityPresentationRenderer.NPC_RENDER_SCALE+
        ",monsterScale="+WorldEntityPresentationRenderer.MONSTER_RENDER_SCALE+
        ",anchorY="+WorldEntityPresentationRenderer.LOGICAL_FOOT_ANCHOR_Y+
        ",assets="+WorldEntityPresentationRenderer.ASSET_STATUS+
        ",directionalBinding=true,layeredBinding=true,evidenceGate=true";
  }
}
