package com.projectdark.mobile;
/** F5M completion presentation policy kept separate from rendering. */
public final class F5mCompletionPresentation{
 private F5mCompletionPresentation(){}
 public static boolean showQuickQuest(F5mAdaptedPrologueQuest.State state){return state!=F5mAdaptedPrologueQuest.State.COMPLETED;}
 public static String dialogue(F5mAdaptedPrologueQuest.State state){if(state==F5mAdaptedPrologueQuest.State.RETURN_READY)return "잘 해냈군. 완료를 눌러 보상을 받게.";if(state==F5mAdaptedPrologueQuest.State.COMPLETED)return "첫 훈련은 완료되었네. 훈련 증표는 잘 챙겨두게.";return null;}
}
