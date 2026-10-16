package com.projectdark.mobile;
/** Source-envelope calibration of visible alpha, not 192px canvas dimensions. */
public final class AbelSpriteCalibration {
 private AbelSpriteCalibration(){}
 public static float width(String id){switch(id){
 case "ABEL_TURTLE":return 53.45f;
 case "ABEL_ARKPUS":return 51.78f;
 case "ABEL_PONG":return 47.37f;
 case "ABEL_CATERPILLAR":return 55.32f;
 case "ABEL_GRAB":return 54.18f;
 case "ABEL_OBS_SLATE":return 59.64f;
 case "ABEL_OCTOPUS":return 68.27f;
 case "ABEL_OBS_PURPLE_EGG":return 60f;
 case "ABEL_GAS":return 55.14f;
 case "ABEL_OBS_PURPLE_MAW":return 57.32f;
 case "ABEL_BERSERKER":return 68.07f;
 case "ABEL_KINGARK_FAKE":return 210f;
 case "ABEL_KINGARK":return 210f;
 default:return -1f;}}
 public static float height(String id){switch(id){
 case "ABEL_TURTLE":return 50.65f;
 case "ABEL_ARKPUS":return 62.99f;
 case "ABEL_PONG":return 49.95f;
 case "ABEL_CATERPILLAR":return 62.6f;
 case "ABEL_GRAB":return 54.25f;
 case "ABEL_OBS_SLATE":return 53.16f;
 case "ABEL_OCTOPUS":return 66.99f;
 case "ABEL_OBS_PURPLE_EGG":return 60f;
 case "ABEL_GAS":return 56.73f;
 case "ABEL_OBS_PURPLE_MAW":return 61.42f;
 case "ABEL_BERSERKER":return 62.01f;
 case "ABEL_KINGARK_FAKE":return 210f;
 case "ABEL_KINGARK":return 210f;
 default:return -1f;}}
}
