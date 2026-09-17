package com.projectdark.mobile;
/** Pure HUD projection for post-prologue progression. */
public final class PostF5mHudPresentation{
 private PostF5mHudPresentation(){}
 public static boolean showNextPurpose(F5mAdaptedPrologueQuest.State state){return F5mCompletionPresentation.showNextPurpose(state);}
 public static String purposeTitle(F5mAdaptedPrologueQuest.State state){return F5mCompletionPresentation.nextPurposeTitle(state);}
 public static String purposeBody(F5mAdaptedPrologueQuest.State state){return F5mCompletionPresentation.nextPurposeBody(state);}
 public static String expLabel(RpgProgressionState rpg){Integer lv=rpg.normalLevel();Long exp=rpg.normalExp();if(lv==null||exp==null)return "EXP · 확인 필요";Long req=LevelExpCurve.requiredForNext(lv);return req==null?"EXP · "+exp:"EXP · "+exp+" / "+req;}
 public static Float expRatio(RpgProgressionState rpg){Integer lv=rpg.normalLevel();Long exp=rpg.normalExp();if(lv==null||exp==null||LevelExpCurve.requiredForNext(lv)==null)return null;return LevelExpCurve.ratio(lv,exp);}
}