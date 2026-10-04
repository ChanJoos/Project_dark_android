package com.projectdark.mobile;
/** F5M completion presentation policy kept separate from rendering. */
public final class F5mCompletionPresentation{
 private F5mCompletionPresentation(){}
 public static boolean showQuickQuest(F5mAdaptedPrologueQuest.State state){return state!=F5mAdaptedPrologueQuest.State.COMPLETED;}
 public static String dialogue(F5mAdaptedPrologueQuest.State state){
  if(state==F5mAdaptedPrologueQuest.State.RETURN_READY)return "잘 해냈군. 완료를 눌러 보상을 받게.";
  if(state==F5mAdaptedPrologueQuest.State.COMPLETED)return "첫 의뢰는 끝났네. 경험치와 훈련 증표를 받았으니 BAG에서 장비를 확인하고 다음 성장 훈련을 준비하게.";
  return null;
 }
 public static String nextPurposeTitle(F5mAdaptedPrologueQuest.State state){return state==F5mAdaptedPrologueQuest.State.COMPLETED?"다음 목표":"";}
 public static String nextPurposeBody(F5mAdaptedPrologueQuest.State state){return state==F5mAdaptedPrologueQuest.State.COMPLETED?"William에게 포테의 숲길 안내를 받으세요. EXP 성장과 BAG 장비·물약을 확인한 뒤 숲으로 이동하세요.":"";}
 public static boolean showNextPurpose(F5mAdaptedPrologueQuest.State state){return state==F5mAdaptedPrologueQuest.State.COMPLETED;}
}
