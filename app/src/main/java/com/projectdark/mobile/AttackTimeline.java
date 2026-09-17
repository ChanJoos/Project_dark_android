package com.projectdark.mobile;

/** One normalized temporal contract consumed by BODY, robe, weapon and melee contact. */
public final class AttackTimeline {
  public enum Phase { ANTICIPATION, CONTACT, RECOVERY }
  public static final float CONTACT_BEGIN=.24f;
  public static final float CONTACT_END=.64f;
  public static final float HIT_POINT=.46f;
  private AttackTimeline(){}
  public static float normalized(float clock,float duration){return duration<=0f?0f:Math.max(0f,Math.min(1f,clock/duration));}
  public static Phase phase(float q){q=Math.max(0f,Math.min(1f,q));return q<CONTACT_BEGIN?Phase.ANTICIPATION:q<CONTACT_END?Phase.CONTACT:Phase.RECOVERY;}
  public static boolean actionPose(float q){return phase(q)==Phase.CONTACT;}
  public static boolean hitCrossed(float before,float after){return before<HIT_POINT&&after>=HIT_POINT;}
  /** Procedural temporal motion around the authored contact pose; degrees added to the directional weapon base. */
  public static float weaponSweep(float q){q=Math.max(0f,Math.min(1f,q));if(q<CONTACT_BEGIN)return -24f*(q/CONTACT_BEGIN);if(q<CONTACT_END)return -24f+104f*((q-CONTACT_BEGIN)/(CONTACT_END-CONTACT_BEGIN));return 80f*(1f-(q-CONTACT_END)/(1f-CONTACT_END));}
}
