package com.projectdark.mobile;
public final class GrowthCoreV3Audit {
 private GrowthCoreV3Audit(){}
 public static boolean verify(){
  RpgProgressionState r=new RpgProgressionState();if(r.str()!=3||r.intel()!=3||r.wis()!=3||r.con()!=3||r.dex()!=3)return false;
  int hp=r.baseMaxHp(),mp=r.baseMaxMp();int gained=r.grantAdaptedReward(1000000000L,0);if(gained!=98||r.normalLevel()!=99||r.statPoints()!=196)return false;
  if(r.baseMaxHp()<=hp||r.baseMaxMp()<=mp)return false;if(r.str()!=3||r.intel()!=3||r.wis()!=3||r.con()!=3||r.dex()!=3)return false;
  for(int n=0;n<196;n++)if(!r.spendStat("STR"))return false;if(r.str()!=199||r.statPoints()!=0)return false;
  RpgProgressionState a=new RpgProgressionState(),b=new RpgProgressionState();a.restoreStats(3,3,3,20,20,0);b.restoreStats(3,3,20,3,3,0);a.grantAdaptedReward(1000000,0);b.grantAdaptedReward(1000000,0);if(a.baseMaxHp()<=b.baseMaxHp()||b.baseMaxMp()<=a.baseMaxMp())return false;
  FinalStats fa=a.finalStats();if(fa.maxHp!=a.baseMaxHp()||fa.ac!=0||fa.magicDefense!=0)return false;
  FinalStats def=a.finalStats();int d0=CombatDefenseFormula.physical(100,null,def);if(d0!=100)return false;
  return true;
 }
 public static void main(String[] z){if(!verify())throw new AssertionError("GrowthCoreV3Audit failed");System.out.println("GROWTH_CORE_V3=PASS");}
}