package com.projectdark.mobile;

/** [B] Prototype weapon attack definitions; not original server values. */
public final class AttackDef {
  public enum Kind { SWING, THRUST, THROW, PUNCH }
  public enum Evidence { O, V, B, ADAPTED }

  public final Kind kind;
  public final String label;
  public final float cooldown;
  public final float range;
  public final int damage;
  public final Evidence evidence;

  public AttackDef(Kind kind,String label,float cooldown,float range,int damage,Evidence evidence){
    this.kind=kind;this.label=label;this.cooldown=cooldown;this.range=range;this.damage=damage;this.evidence=evidence;
  }

  public static final AttackDef[] PROTOTYPES={
      new AttackDef(Kind.SWING,"SWING",.38f,78f,8,Evidence.B),
      new AttackDef(Kind.THRUST,"THRUST",.36f,86f,8,Evidence.B),
      new AttackDef(Kind.THROW,"THROW",.52f,130f,8,Evidence.B),
      new AttackDef(Kind.PUNCH,"PUNCH",.30f,70f,7,Evidence.B)
  };

  public static AttackDef at(int index){return PROTOTYPES[Math.floorMod(index,PROTOTYPES.length)];}
}
