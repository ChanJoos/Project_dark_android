package com.projectdark.mobile;
/** [B] Replaceable Lv1-99 growth policy. Preserves canonical CON/WIS-at-level-up timing semantics. */
public interface LevelGrowthPolicy {
  int hpGain(int newLevel,String job,int conAtLevelUp);
  int mpGain(int newLevel,String job,int wisAtLevelUp);
  final class BalancedV1 implements LevelGrowthPolicy {
    public int hpGain(int newLevel,String job,int con){return Math.max(1,8+Math.max(0,con));}
    public int mpGain(int newLevel,String job,int wis){return Math.max(1,4+Math.max(0,wis));}
  }
}