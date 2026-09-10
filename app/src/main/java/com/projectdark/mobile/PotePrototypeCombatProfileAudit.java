package com.projectdark.mobile;

/** Regression guard ensuring prototype combat tuning cannot masquerade as canonical Pote data. */
public final class PotePrototypeCombatProfileAudit {
  private PotePrototypeCombatProfileAudit(){}

  public static boolean verify(){
    if(!PotePrototypeCombatProfile.isPrototypeOnly())return false;
    if(PotePrototypeCombatProfile.hasPlacement())return false;
    if(!"MAP_POTE_01".equals(PotePrototypeCombatProfile.MAP_ID))return false;
    if(!"POTE_PURPLE".equals(PotePrototypeCombatProfile.MONSTER_ID))return false;

    PoteMonsterRoster roster=new PoteMonsterRoster();
    PoteMonsterRoster.Entry entry=roster.find(PotePrototypeCombatProfile.MONSTER_ID);
    if(entry==null||!entry.hasStableIdentity())return false;
    if(!"포테의숲".equals(entry.region))return false;
    // Canonical common-monster stats must stay unresolved even though a [B] runtime tuning profile exists.
    if(entry.level!=null||entry.hp!=null||entry.exp!=null)return false;

    boolean relationFound=false;
    for(PoteSpawnManifest.Relation r:new PoteSpawnManifest().forMap(PotePrototypeCombatProfile.MAP_ID)){
      if(PotePrototypeCombatProfile.MONSTER_ID.equals(r.monsterId))relationFound=true;
    }
    if(!relationFound)return false;

    // Reuse the already-established dummy baseline exactly; this pass introduces no new balance numbers.
    if(PotePrototypeCombatProfile.RUNTIME_HP_B!=60)return false;
    if(PotePrototypeCombatProfile.CHASE_RADIUS_B!=180f)return false;
    if(PotePrototypeCombatProfile.ATTACK_BEGIN_RANGE_B!=42f)return false;
    if(PotePrototypeCombatProfile.ATTACK_CANCEL_RANGE_B!=48f)return false;
    if(PotePrototypeCombatProfile.CHASE_SPEED_B!=28f)return false;
    if(PotePrototypeCombatProfile.ATTACK_DAMAGE_B!=4)return false;
    return PotePrototypeCombatProfile.ATTACK_COOLDOWN_B==1.2f;
  }
}
