package com.projectdark.mobile;
/** F5M completion presentation policy kept separate from rendering. */
public final class F5mCompletionPresentation{
 private F5mCompletionPresentation(){}
 public static boolean showQuickQuest(F5mAdaptedPrologueQuest.State state){return state!=F5mAdaptedPrologueQuest.State.COMPLETED;}
 public static String dialogue(F5mAdaptedPrologueQuest.State state){
  if(state==F5mAdaptedPrologueQuest.State.RETURN_READY)return "잘 해냈군. 완료를 눌러 보상을 받게.";
  if(state==F5mAdaptedPrologueQuest.State.COMPLETED)return "첫 훈련은 끝났네. 훈련 증표와 장비를 BAG에서 확인하고, 한스에게 포테의 숲길에서 들은 팜팻 변화 의뢰를 물어보게.";
  return null;
 }
 public static String nextPurposeTitle(F5mAdaptedPrologueQuest.State state){return state==F5mAdaptedPrologueQuest.State.COMPLETED?"다음 목표":"";}
 public static String nextPurposeBody(F5mAdaptedPrologueQuest.State state){return state==F5mAdaptedPrologueQuest.State.COMPLETED?"한스에게 포테의 숲길을 묻고 식당 주인의 팜팻 변화 의뢰를 확인하세요. EXP 성장 → BAG에서 장비 확인":"";}
 public static boolean showNextPurpose(F5mAdaptedPrologueQuest.State state){return state==F5mAdaptedPrologueQuest.State.COMPLETED;}
}
