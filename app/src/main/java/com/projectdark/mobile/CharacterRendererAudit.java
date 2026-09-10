package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exhaustive contract audit for World/Character presentation.
 *
 * This is intentionally asset-agnostic: authenticated original sprite frames remain PENDING_CROP.
 * The audit protects the structural contract only: four directions, seven common states,
 * five ordered visual layers, safe effect families, unresolved visual refs and presentation-only scale.
 */
public final class CharacterRendererAudit {
  public static final int EXPECTED_DIRECTION_COUNT=4;
  public static final int EXPECTED_STATE_COUNT=7;
  public static final int EXPECTED_LAYER_COUNT=5;
  public static final int EXPECTED_MATRIX_CASES=28;

  public static final class Case {
    public final CharacterRenderer.Direction direction;
    public final CharacterRenderer.State state;
    public final CharacterRenderer.EffectFamily effectFamily;

    Case(CharacterRenderer.Direction direction,CharacterRenderer.State state,
        CharacterRenderer.EffectFamily effectFamily){
      this.direction=direction;
      this.state=state;
      this.effectFamily=effectFamily;
    }
  }

  private CharacterRendererAudit(){}

  public static List<Case> matrix(){
    List<Case> cases=new ArrayList<>();
    for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
      for(CharacterRenderer.State state:CharacterRenderer.State.values()){
        cases.add(new Case(direction,state,defaultEffect(state)));
      }
    }
    return Collections.unmodifiableList(cases);
  }

  public static boolean passes(){
    if(CharacterRenderer.Direction.values().length!=EXPECTED_DIRECTION_COUNT)return false;
    if(CharacterRenderer.State.values().length!=EXPECTED_STATE_COUNT)return false;
    if(CharacterRenderer.DRAW_ORDER.size()!=EXPECTED_LAYER_COUNT)return false;
    if(matrix().size()!=EXPECTED_MATRIX_CASES)return false;
    if(CharacterRenderer.DRAW_ORDER.get(0)!=CharacterRenderer.Layer.BODY)return false;
    if(CharacterRenderer.DRAW_ORDER.get(1)!=CharacterRenderer.Layer.HAIR)return false;
    if(CharacterRenderer.DRAW_ORDER.get(2)!=CharacterRenderer.Layer.EQUIPMENT)return false;
    if(CharacterRenderer.DRAW_ORDER.get(3)!=CharacterRenderer.Layer.WEAPON)return false;
    if(CharacterRenderer.DRAW_ORDER.get(4)!=CharacterRenderer.Layer.EFFECT)return false;
    if(!CharacterRenderer.ASSET_STATUS.equals("PENDING_CROP"))return false;

    // Presentation-scale guard: world coordinates stay untouched while the visual body remains below 1:1 prototype scale.
    if(CharacterRenderer.PLAYER_RENDER_SCALE<=0f||CharacterRenderer.PLAYER_RENDER_SCALE>=1f)return false;
    if(CharacterRenderer.SHADOW_RENDER_SCALE<=0f||CharacterRenderer.SHADOW_RENDER_SCALE>CharacterRenderer.PLAYER_RENDER_SCALE)return false;
    if(CharacterRenderer.LOGICAL_FOOT_ANCHOR_Y!=0f)return false;

    for(Case c:matrix()){
      if(c.direction==null||c.state==null||c.effectFamily==null)return false;
      if(c.state==CharacterRenderer.State.CAST&&c.effectFamily!=CharacterRenderer.EffectFamily.CAST)return false;
      if(c.state==CharacterRenderer.State.HIT&&c.effectFamily!=CharacterRenderer.EffectFamily.HIT)return false;
      if(c.state==CharacterRenderer.State.IDLE&&c.effectFamily!=CharacterRenderer.EffectFamily.NONE)return false;
      if(c.state==CharacterRenderer.State.WALK&&c.effectFamily!=CharacterRenderer.EffectFamily.NONE)return false;
      if(c.state==CharacterRenderer.State.DEAD&&c.effectFamily!=CharacterRenderer.EffectFamily.NONE)return false;
    }
    return true;
  }

  public static String summary(){
    return "directions="+CharacterRenderer.Direction.values().length+
        ",states="+CharacterRenderer.State.values().length+
        ",layers="+CharacterRenderer.DRAW_ORDER.size()+
        ",matrix="+matrix().size()+
        ",playerScale="+CharacterRenderer.PLAYER_RENDER_SCALE+
        ",shadowScale="+CharacterRenderer.SHADOW_RENDER_SCALE+
        ",assets="+CharacterRenderer.ASSET_STATUS;
  }

  private static CharacterRenderer.EffectFamily defaultEffect(CharacterRenderer.State state){
    switch(state){
      case CAST:return CharacterRenderer.EffectFamily.CAST;
      case HIT:return CharacterRenderer.EffectFamily.HIT;
      default:return CharacterRenderer.EffectFamily.NONE;
    }
  }
}
