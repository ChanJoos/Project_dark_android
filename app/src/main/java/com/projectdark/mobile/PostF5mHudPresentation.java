package com.projectdark.mobile;

/** Pure HUD projection for post-prologue progression. No fabricated level thresholds. */
public final class PostF5mHudPresentation {
  private PostF5mHudPresentation(){}
  public static boolean showNextPurpose(F5mAdaptedPrologueQuest.State state){return F5mCompletionPresentation.showNextPurpose(state);}
  public static String purposeTitle(F5mAdaptedPrologueQuest.State state){return F5mCompletionPresentation.nextPurposeTitle(state);}
  public static String purposeBody(F5mAdaptedPrologueQuest.State state){return F5mCompletionPresentation.nextPurposeBody(state);}
  public static String expLabel(RpgProgressionState rpg){Long exp=rpg.normalExp();return exp==null?"EXP · 확인 필요":"EXP · "+exp;}
  /** Returns null until a canonical next-level threshold is available. */
  public static Float expRatio(RpgProgressionState rpg){return null;}
}