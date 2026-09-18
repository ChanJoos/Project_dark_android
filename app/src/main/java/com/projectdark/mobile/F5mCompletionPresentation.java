package com.projectdark.mobile;
/** F5M completion presentation policy kept separate from rendering. */
public final class F5mCompletionPresentation{
 private F5mCompletionPresentation(){}
 public static boolean showQuickQuest(F5mAdaptedPrologueQuest.State state){return state!=F5mAdaptedPrologueQuest.State.COMPLETED;}
 public static String dialogue(F5mAdaptedPrologueQuest.State state){
  if(state==F5mAdaptedPrologueQuest.State.RETURN_READY)return "잘 해냈군. 완료를 눌러 보상을 받게.";
  if(state==F5mAdaptedPrologueQuest.State.COMPLETED)return "첫 훈련은 끝났네. 이제 사냥으로 경험을 쌓고, 얻은 장비를 확인해 보게.";
  return null;
 }
 public static String nextPurposeTitle(F5mAdaptedPrologueQuest.State state){return state==F5mAdaptedPrologueQuest.State.COMPLETED?"다음 목표":"";}
 public static String nextPurposeBody(F5mAdaptedPrologueQuest.State state){return state==F5mAdaptedPrologueQuest.State.COMPLETED?"몬스터 사냥 → EXP 성장 → BAG에서 장비 확인":"";}
 public static boolean showNextPurpose(F5mAdaptedPrologueQuest.State state){return state==F5mAdaptedPrologueQuest.State.COMPLETED;}
}