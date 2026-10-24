package com.projectdark.mobile;
/** Pure HUD projection for post-prologue progression. */
public final class PostF5mHudPresentation{
 private PostF5mHudPresentation(){}
 public static boolean showNextPurpose(F5mAdaptedPrologueQuest.State state){return F5mCompletionPresentation.showNextPurpose(state);}
 public static String purposeTitle(F5mAdaptedPrologueQuest.State state){return F5mCompletionPresentation.nextPurposeTitle(state);}
 public static String purposeBody(F5mAdaptedPrologueQuest.State state){return F5mCompletionPresentation.nextPurposeBody(state);}
 public static String expLabel(RpgProgressionState rpg){Integer lv=rpg.normalLevel();Long exp=rpg.normalExp();if(lv==null||exp==null)return "EXP · 확인 필요";Long req=LevelExpCurve.requiredForNext(lv);return req==null?"EXP · "+exp+" / "+TempleGrowthPolicy.EXP_CAP+(exp>=TempleGrowthPolicy.EXP_CAP?" · 상한":""):"EXP · "+exp+" / "+req;}
 public static Float expRatio(RpgProgressionState rpg){Integer lv=rpg.normalLevel();Long exp=rpg.normalExp();if(lv==null||exp==null)return null;if(lv>=99)return Math.min(1f,exp/(float)TempleGrowthPolicy.EXP_CAP);return LevelExpCurve.ratio(lv,exp);}
}