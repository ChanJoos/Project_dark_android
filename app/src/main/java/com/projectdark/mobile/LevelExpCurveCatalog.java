package com.projectdark.mobile;

/**
 * RPG-owned runtime projection of the canonical project balance curve in master/data/Level_EXP_Curve.csv.
 *
 * PASS 29 intentionally activates only the early vertical slice (Lv1 -> Lv10). Every projected row is
 * Evidence.B in the Master. These values are therefore project balance-model facts, not claimed original
 * game constants. Later levels remain outside this runtime projection until the next expansion pass.
 */
public final class LevelExpCurveCatalog {
  public static final int MIN_LEVEL=1;
  public static final int MAX_PROJECTED_LEVEL=10;

  /** Cumulative EXP required to have reached each level index. Level 1 starts at cumulative 0 [B]. */
  private static final long[] CUMULATIVE={
      0L,
      0L,      // Lv1
      10000L,  // Lv2
      22800L,  // Lv3
      39300L,  // Lv4
      60500L,  // Lv5
      87800L,  // Lv6
      122800L, // Lv7
      167800L, // Lv8
      225600L, // Lv9
      300000L  // Lv10
  };

  private LevelExpCurveCatalog(){}

  public static long cumulativeRequiredForLevel(int level){
    if(level<MIN_LEVEL||level>MAX_PROJECTED_LEVEL)throw new IllegalArgumentException("level");
    return CUMULATIVE[level];
  }

  public static Long requiredToNextLevel(int level){
    if(level<MIN_LEVEL||level>MAX_PROJECTED_LEVEL)throw new IllegalArgumentException("level");
    if(level==MAX_PROJECTED_LEVEL)return null;
    return CUMULATIVE[level+1]-CUMULATIVE[level];
  }

  /** Returns the highest projected level supported by cumulativeExp. Never guesses beyond Lv10. */
  public static int levelForCumulativeExp(long cumulativeExp){
    if(cumulativeExp<0)throw new IllegalArgumentException("cumulativeExp");
    int level=MIN_LEVEL;
    for(int candidate=2;candidate<=MAX_PROJECTED_LEVEL;candidate++){
      if(cumulativeExp<CUMULATIVE[candidate])break;
      level=candidate;
    }
    return level;
  }

  public static boolean audit(){
    long previous=-1;
    for(int level=MIN_LEVEL;level<=MAX_PROJECTED_LEVEL;level++){
      long current=CUMULATIVE[level];
      if(current<0||current<=previous&&level>MIN_LEVEL)return false;
      previous=current;
    }
    return CUMULATIVE[2]==10000L&&CUMULATIVE[10]==300000L;
  }
}
