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
  public static final float MIN_HEAD_TO_BODY_RATIO=.30f;
  public static final float MAX_HEAD_TO_BODY_RATIO=.38f;
  public static final float MIN_HEAD_WIDTH=10.5f;
  public static final float MAX_SHOULDER_WIDTH=9.2f;

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
    if(CharacterRenderer.DRAW_ORDER.get(0)!=CharacterRenderer.Layer.BODY||CharacterRenderer.DRAW_ORDER.get(1)!=CharacterRenderer.Layer.HAIR||CharacterRenderer.DRAW_ORDER.get(2)!=CharacterRenderer.Layer.EQUIPMENT||CharacterRenderer.DRAW_ORDER.get(3)!=CharacterRenderer.Layer.WEAPON||CharacterRenderer.DRAW_ORDER.get(4)!=CharacterRenderer.Layer.EFFECT)return false;
    if(!"PENDING_CROP".equals(CharacterRenderer.ASSET_STATUS))return false;
    if(!"USER_CONCEPT_20260910_CHIBI_DIAGONAL_V2".equals(CharacterRenderer.PRESENTATION_PROFILE))return false;
    if(Math.abs(CharacterRenderer.PLAYER_RENDER_SCALE-USER_APPROVED_PLAYER_RENDER_SCALE)>.0001f)return false;
    if(Math.abs(CharacterRenderer.SHADOW_RENDER_SCALE-USER_APPROVED_SHADOW_RENDER_SCALE)>.0001f)return false;
    if(CharacterRenderer.HEAD_TO_BODY_RATIO<MIN_HEAD_TO_BODY_RATIO||CharacterRenderer.HEAD_TO_BODY_RATIO>MAX_HEAD_TO_BODY_RATIO)return false;
    if(CharacterRenderer.HEAD_WIDTH<MIN_HEAD_WIDTH)return false;
    if(CharacterRenderer.SHOULDER_WIDTH>MAX_SHOULDER_WIDTH)return false;
    if(CharacterRenderer.HEAD_WIDTH<=CharacterRenderer.SHOULDER_WIDTH)return false;
    if(CharacterRenderer.LOGICAL_FOOT_ANCHOR_Y!=0f)return false;
    CharacterRenderer.DirectionalVisualSet unresolved=new CharacterRenderer.DirectionalVisualSet(null,null,null,null);if(!unresolved.unresolved())return false;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())if(unresolved.forDirection(d)!=null)return false;
    for(Case c:matrix()){
      if(c.direction==null||c.state==null||c.effectFamily==null)return false;
      if(c.state==CharacterRenderer.State.CAST&&c.effectFamily!=CharacterRenderer.EffectFamily.CAST)return false;
      if(c.state==CharacterRenderer.State.HIT&&c.effectFamily!=CharacterRenderer.EffectFamily.HIT)return false;
      if((c.state==CharacterRenderer.State.IDLE||c.state==CharacterRenderer.State.WALK||c.state==CharacterRenderer.State.DEAD)&&c.effectFamily!=CharacterRenderer.EffectFamily.NONE)return false;
    }
    return true;
  }
  public static String summary(){return "profile="+CharacterRenderer.PRESENTATION_PROFILE+",directions="+CharacterRenderer.Direction.values().length+",states="+CharacterRenderer.State.values().length+",layers="+CharacterRenderer.DRAW_ORDER.size()+",matrix="+matrix().size()+",scale="+CharacterRenderer.PLAYER_RENDER_SCALE+",headRatio="+CharacterRenderer.HEAD_TO_BODY_RATIO+",headWidth="+CharacterRenderer.HEAD_WIDTH+",shoulderWidth="+CharacterRenderer.SHOULDER_WIDTH+",anchorY="+CharacterRenderer.LOGICAL_FOOT_ANCHOR_Y+",assets="+CharacterRenderer.ASSET_STATUS;}
  private static CharacterRenderer.EffectFamily defaultEffect(CharacterRenderer.State state){switch(state){case CAST:return CharacterRenderer.EffectFamily.CAST;case HIT:return CharacterRenderer.EffectFamily.HIT;default:return CharacterRenderer.EffectFamily.NONE;}}
}
