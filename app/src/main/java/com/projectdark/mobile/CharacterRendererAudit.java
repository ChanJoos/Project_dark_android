package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Static contract audit for the crash-safe four-diagonal starter-peasant renderer. */
public final class CharacterRendererAudit {
  public static final int EXPECTED_DIRECTION_COUNT=4;
  public static final int EXPECTED_STATE_COUNT=7;
  public static final int EXPECTED_LAYER_COUNT=5;
  public static final int EXPECTED_MATRIX_CASES=28;
  public static final int EXPECTED_PAPER_DOLL_LAYER_COUNT=10;
  public static final float USER_APPROVED_PLAYER_RENDER_SCALE=1.50f;
  public static final float USER_APPROVED_SHADOW_RENDER_SCALE=0.72f;
  public static final float EXPECTED_WALK_FRAMES_PER_SECOND=5.0f;

  public static final class Case {
    public final CharacterRenderer.Direction direction;
    public final CharacterRenderer.State state;
    public final CharacterRenderer.EffectFamily effectFamily;
    Case(CharacterRenderer.Direction direction,CharacterRenderer.State state,CharacterRenderer.EffectFamily effectFamily){this.direction=direction;this.state=state;this.effectFamily=effectFamily;}
  }

  private CharacterRendererAudit(){}

  public static List<Case> matrix(){
    List<Case> cases=new ArrayList<>();
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())
      for(CharacterRenderer.State s:CharacterRenderer.State.values())
        cases.add(new Case(d,s,defaultEffect(s)));
    return Collections.unmodifiableList(cases);
  }

  public static boolean passes(){
    if(CharacterRenderer.Direction.values().length!=EXPECTED_DIRECTION_COUNT)return false;
    if(CharacterRenderer.State.values().length!=EXPECTED_STATE_COUNT)return false;
    if(CharacterRenderer.DRAW_ORDER.size()!=EXPECTED_LAYER_COUNT||matrix().size()!=EXPECTED_MATRIX_CASES)return false;
    if(!"PEASANT_BASE_20260911_R1".equals(CharacterRenderer.PRESENTATION_PROFILE))return false;
    if(!"PEASANT".equals(CharacterRenderer.STARTER_ARCHETYPE)||!"PRE_CLASS".equals(CharacterRenderer.STARTER_CLASS_STATE))return false;
    if(!CharacterRenderer.HARD_PIXEL_GRID||!CharacterRenderer.STARTER_WEAPONLESS||!CharacterRenderer.STARTER_OFFHAND_EMPTY)return false;
    if(CharacterRenderer.LEGACY_MARTIAL_ASSET_ALLOWED)return false;
    if(CharacterRenderer.PAPER_DOLL_ORDER.size()!=EXPECTED_PAPER_DOLL_LAYER_COUNT)return false;
    if(!"BODY_BASE".equals(CharacterRenderer.PAPER_DOLL_ORDER.get(0)))return false;
    if(!"OFFHAND".equals(CharacterRenderer.PAPER_DOLL_ORDER.get(CharacterRenderer.PAPER_DOLL_ORDER.size()-1)))return false;
    if(CharacterRenderer.ATLAS_FRAME_WIDTH!=24||CharacterRenderer.ATLAS_FRAME_HEIGHT!=32)return false;
    if(CharacterRenderer.IDLE_WALK_COLUMNS!=5||CharacterRenderer.ATTACK_COLUMNS!=4||CharacterRenderer.ATLAS_ROWS!=4)return false;
    if(CharacterRenderer.IDLE_WALK_WIDTH!=120||CharacterRenderer.ACTION_WIDTH!=96||CharacterRenderer.ATLAS_HEIGHT!=128)return false;
    if(Math.abs(CharacterRenderer.PLAYER_RENDER_SCALE-USER_APPROVED_PLAYER_RENDER_SCALE)>.0001f)return false;
    if(Math.abs(CharacterRenderer.SHADOW_RENDER_SCALE-USER_APPROVED_SHADOW_RENDER_SCALE)>.0001f)return false;
    if(Math.abs(CharacterRenderer.WALK_FRAMES_PER_SECOND-EXPECTED_WALK_FRAMES_PER_SECOND)>.0001f)return false;
    if(CharacterRenderer.LOGICAL_FOOT_ANCHOR_Y!=0f)return false;

    if(CharacterRenderer.atlasRow(CharacterRenderer.Direction.NW)!=0)return false;
    if(CharacterRenderer.atlasRow(CharacterRenderer.Direction.NE)!=1)return false;
    if(CharacterRenderer.atlasRow(CharacterRenderer.Direction.SW)!=2)return false;
    if(CharacterRenderer.atlasRow(CharacterRenderer.Direction.SE)!=3)return false;
    for(int row=0;row<4;row++){
      CharacterRenderer.Direction d=CharacterRenderer.visualFacingForRow(row);
      if(d==null||CharacterRenderer.atlasRow(d)!=row)return false;
    }

    CharacterRenderer.DirectionalVisualSet unresolved=new CharacterRenderer.DirectionalVisualSet(null,null,null,null);
    if(!unresolved.unresolved())return false;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())if(unresolved.forDirection(d)!=null)return false;

    for(Case c:matrix()){
      if(c.direction==null||c.state==null||c.effectFamily==null)return false;
      if(c.state==CharacterRenderer.State.CAST&&c.effectFamily!=CharacterRenderer.EffectFamily.CAST)return false;
      if(c.state==CharacterRenderer.State.HIT&&c.effectFamily!=CharacterRenderer.EffectFamily.HIT)return false;
    }
    return true;
  }

  public static String summary(){
    return "profile="+CharacterRenderer.PRESENTATION_PROFILE+
        ",starter="+CharacterRenderer.STARTER_ARCHETYPE+
        ",frame="+CharacterRenderer.ATLAS_FRAME_WIDTH+"x"+CharacterRenderer.ATLAS_FRAME_HEIGHT+
        ",idleWalk="+CharacterRenderer.IDLE_WALK_WIDTH+"x"+CharacterRenderer.ATLAS_HEIGHT+
        ",rows=NW,NE,SW,SE,scale="+CharacterRenderer.PLAYER_RENDER_SCALE+
        ",walkFps="+CharacterRenderer.WALK_FRAMES_PER_SECOND+
        ",paperDollLayers="+CharacterRenderer.PAPER_DOLL_ORDER.size()+
        ",legacyMartialAllowed="+CharacterRenderer.LEGACY_MARTIAL_ASSET_ALLOWED+
        ",anchorY="+CharacterRenderer.LOGICAL_FOOT_ANCHOR_Y;
  }

  private static CharacterRenderer.EffectFamily defaultEffect(CharacterRenderer.State state){
    switch(state){
      case CAST:return CharacterRenderer.EffectFamily.CAST;
      case HIT:return CharacterRenderer.EffectFamily.HIT;
      default:return CharacterRenderer.EffectFamily.NONE;
    }
  }
}
