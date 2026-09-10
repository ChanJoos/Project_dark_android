package com.projectdark.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Read-only identity/roster projection of the current Master-backed 포테의숲 monster rows.
 *
 * This class intentionally does NOT invent missing HP/EXP/AI/drop-rate values. Mixed source evidence
 * such as V/FAN and O/V is preserved verbatim instead of being flattened into a stronger evidence class.
 * Source: master/data/Monster_Master.csv.
 */
public final class PoteMonsterRoster {
  public static final class Entry {
    public final String monsterId;
    public final String name;
    public final String region;
    public final String zone;
    public final Integer level;
    public final Integer hp;
    public final Integer exp;
    public final String sourceEvidence;
    public final String validationStatus;

    Entry(String monsterId,String name,String region,String zone,Integer level,Integer hp,Integer exp,
          String sourceEvidence,String validationStatus){
      this.monsterId=monsterId;
      this.name=name;
      this.region=region;
      this.zone=zone;
      this.level=level;
      this.hp=hp;
      this.exp=exp;
      this.sourceEvidence=sourceEvidence;
      this.validationStatus=validationStatus;
    }

    /** Identity is usable without claiming unresolved combat stats are known. */
    public boolean hasStableIdentity(){return monsterId!=null&&!monsterId.isEmpty()&&name!=null&&!name.isEmpty()&&region!=null&&!region.isEmpty();}
    public boolean hasResolvedCombatStats(){return level!=null&&hp!=null&&exp!=null;}
  }

  private final Map<String,Entry> byId;

  public PoteMonsterRoster(){
    Map<String,Entry> entries=new LinkedHashMap<>();
    add(entries,"POTE_RED","레드팜팻","1~2존",null,null,null,"V/FAN","BEHAVIOR_PARTIAL");
    add(entries,"POTE_GREEN","그린팜팻","1~2존",null,null,null,"V/FAN","BEHAVIOR_PARTIAL");
    add(entries,"POTE_PURPLE","퍼플팜팻","1~2존",null,null,null,"V/FAN","BEHAVIOR_PARTIAL");
    add(entries,"POTE_SILVER","실버팜팻","1~2존",null,null,null,"V/FAN","BEHAVIOR_PARTIAL");
    add(entries,"POTE_TREANT","트렌트","일반존",null,null,null,"V/FAN","BEHAVIOR_PARTIAL");
    add(entries,"POTE_ANTLION","앤트라이온","일반존",null,null,null,"V/FAN","BEHAVIOR_PARTIAL");
    add(entries,"POTE_GNOLL","놀","일반존",null,null,null,"V/FAN","BEHAVIOR_PARTIAL");
    add(entries,"POTE_WOLFRIDER","울프라이더","3~4존",null,null,null,"V/FAN","BEHAVIOR_PARTIAL");
    add(entries,"POTE_LYCAN","라이칸스로프","일반존",null,null,null,"V/FAN","BEHAVIOR_PARTIAL");
    add(entries,"POTE_ANTGIANT","앤트자이언트","3~4존",null,null,null,"V/FAN","BEHAVIOR_PARTIAL");
    add(entries,"POTE_SILVERWOLF","은빛늑대","일반존",null,null,null,"O/V","BEHAVIOR_PARTIAL");
    add(entries,"POTE_STRONG_GNOLL","강력한놀","일반존",null,null,null,"O/V","ROSTER_VERIFIED");
    add(entries,"POTE_STRONG_WOLFRIDER","강력한울프라이더","일반존",null,null,null,"O/V","ROSTER_VERIFIED");
    add(entries,"POTE_STRONG_TREANT","강력한트렌트","일반존",null,null,null,"O/V","ROSTER_VERIFIED");
    add(entries,"POTE_SPIRIT","포테의정령","마지막 존",48,29201,308950,"V","STAT_VERIFIED");
    add(entries,"POTE_MANTIS","자이언트맨티스","오솔길/결계 이후 보스",null,null,null,"V/FAN","BEHAVIOR_VERIFIED");
    byId=Collections.unmodifiableMap(entries);
  }

  private static void add(Map<String,Entry> entries,String id,String name,String zone,Integer level,Integer hp,Integer exp,
                          String evidence,String validationStatus){
    entries.put(id,new Entry(id,name,"포테의숲",zone,level,hp,exp,evidence,validationStatus));
  }

  public Entry find(String monsterId){return byId.get(monsterId);}
  public Map<String,Entry> entries(){return byId;}
}
