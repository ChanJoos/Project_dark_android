package com.projectdark.mobile;

/** User-approved mobile rules: one Seo, no mastery or advancement dungeon gate. */
public final class TempleGrowthPolicy {
  public static final long EXP_CAP=4_200_000_000L,ADVANCEMENT_EXP=250_000_000L;
  public enum Resource { HP, MP }
  public enum Result { OK, LEVEL_REQUIRED, JOB_REQUIRED, INSUFFICIENT_EXP, STATS_REQUIRED, ALREADY_ADVANCED, INVALID, SAVE_FAILED }
  private static final double CALIBRATION;
  static { double sum=0;for(int hp=4000;hp<10000;hp+=50)sum+=(double)hp*hp;CALIBRATION=100_000_000d/sum; }
  private TempleGrowthPolicy(){}
  public static int unit(Resource resource){return resource==Resource.HP?50:25;}
  /** Calibrated to ~100m for HP4000->10000. An explicit mobile curve, not a recovered server formula. */
  public static long singleCost(int pureAmount){
    if(pureAmount<1)return Long.MAX_VALUE;
    double cost=CALIBRATION*pureAmount*pureAmount;
    if(pureAmount>10000)cost*=Math.pow(pureAmount/10000d,2);
    return cost>EXP_CAP?EXP_CAP+1:Math.max(1,Math.round(cost));
  }
  public static long quote(RpgProgressionState r,Resource resource,int count){
    if(r==null||resource==null||count<1||count>1000)return -1;
    long amount=resource==Resource.HP?r.baseMaxHp():r.baseMaxMp(),total=0;
    for(int i=0;i<count;i++){
      if(amount>Integer.MAX_VALUE-unit(resource))return -1;
      long cost=singleCost((int)amount);if(total>EXP_CAP-cost)return -1;
      total+=cost;amount+=unit(resource);
    }
    return total;
  }
  public static int hpRequired(String job){switch(job){case "WARRIOR":return 17000;case "ROGUE":case "CLERIC":return 13000;case "MAGE":return 12000;case "MARTIAL_ARTIST":return 16000;default:return 0;}}
  public static int mpRequired(String job){switch(job){case "WARRIOR":return 5000;case "ROGUE":case "MARTIAL_ARTIST":return 7000;case "MAGE":return 13000;case "CLERIC":return 14000;default:return 0;}}
  public static Result eligibility(RpgProgressionState r){
    if(r.firstAdvanced())return Result.ALREADY_ADVANCED;
    if(r.normalLevel()<99)return Result.LEVEL_REQUIRED;
    if(hpRequired(r.currentJobCode())==0)return Result.JOB_REQUIRED;
    if(r.baseMaxHp()<hpRequired(r.currentJobCode())||r.baseMaxMp()<mpRequired(r.currentJobCode()))return Result.STATS_REQUIRED;
    return r.normalExp()<ADVANCEMENT_EXP?Result.INSUFFICIENT_EXP:Result.OK;
  }
  public static String message(Result result){switch(result){case OK:return "은총을 받았습니다";case LEVEL_REQUIRED:return "99레벨부터 이용할 수 있습니다";case JOB_REQUIRED:return "기본 직업을 먼저 선택하세요";case INSUFFICIENT_EXP:return "보유 경험치가 부족합니다";case STATS_REQUIRED:return "순수 체력과 마력이 승급 조건에 부족합니다";case ALREADY_ADVANCED:return "이미 1차 승급을 마쳤습니다";case SAVE_FAILED:return "저장하지 못했습니다 · 거래가 취소되었습니다";default:return "구매 수량과 경험치를 확인하세요";}}
}
