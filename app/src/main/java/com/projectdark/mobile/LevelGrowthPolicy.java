package com.projectdark.mobile;
/** ADAPTED_USER_CALIBRATION_V168: user's approximate 3/60 examples, not an original numeric formula. */
public interface LevelGrowthPolicy {
 int hpGain(int newLevel,String job,int conAtLevelUp);
 int mpGain(int newLevel,String job,int wisAtLevelUp);
 static int center(int stat){return 50+(int)Math.round((Math.max(3,Math.min(10000,stat))-3)*90d/57d);}
 static int spread(int stat){return 5+(int)Math.round((Math.max(3,Math.min(10000,stat))-3)*5d/57d);}
 static int minimum(int stat){return center(stat)-spread(stat);}
 static int maximum(int stat){return center(stat)+spread(stat);}
 final class BalancedV1 implements LevelGrowthPolicy {
  public int hpGain(int newLevel,String job,int con){return center(con);}
  public int mpGain(int newLevel,String job,int wis){return center(wis);}
 }
}
