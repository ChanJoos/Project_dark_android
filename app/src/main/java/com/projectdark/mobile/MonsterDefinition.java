package com.projectdark.mobile;

/** Evidence-safe monster definition projected from Master-backed data. */
public final class MonsterDefinition {
  public enum Evidence { O,V,U,B,ADAPTED,PENDING,FAN }
  public enum Status { CANONICAL, PROTOTYPE_PENDING, UNRESOLVED }

  public final String monsterId;
  public final String name;
  public final String region;
  public final Integer level;
  public final Integer hp;
  public final Integer exp;
  public final String majorDropText;
  public final Evidence evidence;
  public final Status status;

  public MonsterDefinition(String monsterId,String name,String region,Integer level,Integer hp,Integer exp,String majorDropText,Evidence evidence,Status status){
    this.monsterId=monsterId;
    this.name=name;
    this.region=region;
    this.level=level;
    this.hp=hp;
    this.exp=exp;
    this.majorDropText=majorDropText;
    this.evidence=evidence;
    this.status=status;
  }

  public boolean hasCanonicalReward(){return status==Status.CANONICAL&&exp!=null;}
}
