package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/** Single source of truth: tile transition -> facing -> action lock -> presentation phase. */
public final class UnifiedRuntimeContract {
  public enum ActionPhase { ANTICIPATION, CONTACT, RECOVERY }
  public static final float ANTICIPATION_END=.18f;
  public static final float CONTACT_END=.78f;
  private UnifiedRuntimeContract(){}

  public static CharacterRenderer.Direction facing(WorldMoveTargetController.Direction d){
    if(d==null)return null;
    switch(d){case NW:return CharacterRenderer.Direction.NW;case NE:return CharacterRenderer.Direction.NE;case SW:return CharacterRenderer.Direction.SW;default:return CharacterRenderer.Direction.SE;}
  }
  public static WorldMoveTargetController.Direction adjacentDirection(float ax,float ay,float bx,float by){return WorldMoveTargetController.Direction.between(ax,ay,bx,by);}
  public static boolean meleeReachable(float ax,float ay,float bx,float by){return adjacentDirection(ax,ay,bx,by)!=null;}
  public static ActionPhase actionPhase(float normalized){float q=Math.max(0f,Math.min(1f,normalized));if(q<ANTICIPATION_END)return ActionPhase.ANTICIPATION;if(q<CONTACT_END)return ActionPhase.CONTACT;return ActionPhase.RECOVERY;}
  public static boolean contactVisible(float normalized){return actionPhase(normalized)==ActionPhase.CONTACT;}
}
