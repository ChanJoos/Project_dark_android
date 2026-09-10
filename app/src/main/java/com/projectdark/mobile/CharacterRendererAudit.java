package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Structural audit for the user-approved four-direction character presentation contract. */
public final class CharacterRendererAudit {
  public static final int EXPECTED_DIRECTION_COUNT=4;
  public static final int EXPECTED_STATE_COUNT=7;
  public static final int EXPECTED_LAYER_COUNT=5;
  public static final int EXPECTED_MATRIX_CASES=28;
  public static final float USER_APPROVED_PLAYER_RENDER_SCALE=1.50f;
  public static final float USER_APPROVED_SHADOW_RENDER_SCALE=0.72f;

  public static final class Case {
    public final CharacterRenderer.Direction direction;
    public final CharacterRenderer.State state;
    public final CharacterRenderer.EffectFamily effectFamily;
    Case(CharacterRenderer.Direction direction,CharacterRenderer.State state,CharacterRenderer.EffectFamily effectFamily){this.direction=direction;this.state=state;this.effectFamily=effectFamily;}
  }
  private CharacterRendererAudit(){}
  public static List<Case> matrix(){List<Case> cases=new ArrayList<>();for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values())for(CharacterRenderer.State state:CharacterRenderer.State.values())cases.add(new Case(direction,state,defaultEffect(state)));return Collections.unmodifiableList(cases);}
  public static boolean passes(){
    if(CharacterRenderer.Direction.values().length!=EXPECTED_DIRECTION_COUNT)return false;
    if(CharacterRenderer.State.values().length!=EXPECTED_STATE_COUNT)return false;
    if(CharacterRenderer.DRAW_ORDER.size()!=EXPECTED_LAYER_COUNT||matrix().size()!=EXPECTED_MATRIX_CASES)return false;
    if(!"USER_APPROVED_ATLAS_20260910_V4".equals(CharacterRenderer.PRESENTATION_PROFILE))return false;
    if(!CharacterRenderer.HARD_PIXEL_GRID||!CharacterRenderer.DEFAULT_ATLAS_ENABLED)return false;
    if(CharacterRenderer.ATLAS_FRAME_WIDTH!=24||CharacterRenderer.ATLAS_FRAME_HEIGHT!=32||CharacterRenderer.ATLAS_COLUMNS!=5||CharacterRenderer.ATLAS_ROWS!=4)return false;
    if(Math.abs(CharacterRenderer.PLAYER_RENDER_SCALE-USER_APPROVED_PLAYER_RENDER_SCALE)>.0001f)return false;
    if(Math.abs(CharacterRenderer.SHADOW_RENDER_SCALE-USER_APPROVED_SHADOW_RENDER_SCALE)>.0001f)return false;
    if(CharacterRenderer.LOGICAL_FOOT_ANCHOR_Y!=0f)return false;
    if(CharacterRenderer.HEAD_WIDTH<=CharacterRenderer.SHOULDER_WIDTH)return false;
    if(!CharacterRenderer.isDefaultAtlasState(CharacterRenderer.State.IDLE))return false;
    if(!CharacterRenderer.isDefaultAtlasState(CharacterRenderer.State.WALK))return false;
    if(CharacterRenderer.isDefaultAtlasState(CharacterRenderer.State.ATTACK))return false;
    CharacterRenderer.DirectionalVisualSet unresolved=new CharacterRenderer.DirectionalVisualSet(null,null,null,null);if(!unresolved.unresolved())return false;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())if(unresolved.forDirection(d)!=null)return false;
    for(Case c:matrix()){
      if(c.direction==null||c.state==null||c.effectFamily==null)return false;
      if(c.state==CharacterRenderer.State.CAST&&c.effectFamily!=CharacterRenderer.EffectFamily.CAST)return false;
      if(c.state==CharacterRenderer.State.HIT&&c.effectFamily!=CharacterRenderer.EffectFamily.HIT)return false;
    }
    return true;
  }
  public static String summary(){return "profile="+CharacterRenderer.PRESENTATION_PROFILE+",atlas="+CharacterRenderer.DEFAULT_ATLAS_ENABLED+",frame="+CharacterRenderer.ATLAS_FRAME_WIDTH+"x"+CharacterRenderer.ATLAS_FRAME_HEIGHT+",directions="+CharacterRenderer.Direction.values().length+",states="+CharacterRenderer.State.values().length+",scale="+CharacterRenderer.PLAYER_RENDER_SCALE+",anchorY="+CharacterRenderer.LOGICAL_FOOT_ANCHOR_Y;}
  private static CharacterRenderer.EffectFamily defaultEffect(CharacterRenderer.State state){switch(state){case CAST:return CharacterRenderer.EffectFamily.CAST;case HIT:return CharacterRenderer.EffectFamily.HIT;default:return CharacterRenderer.EffectFamily.NONE;}}
}
