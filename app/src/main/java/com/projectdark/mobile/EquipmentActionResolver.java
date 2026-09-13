package com.projectdark.mobile;

/** Resolves equipment state into the stable appearance + semantic-action handoff consumed by Visual. */
public final class EquipmentActionResolver {
  public static final String SOURCE_NAMED_APPEARANCE="SOURCE_NAMED:Asset_Master";
  public static final String ADAPTED_PLAYTEST_ACTION="ADAPTED PLAYTEST FIXTURE";
  public static final String EXPLICIT_GAME_ACTION="EXPLICIT GAME ACTION";
  public static final String UNARMED_RUNTIME="RUNTIME UNARMED";

  public static final class Result {
    public final String weaponItemId;
    public final String weaponAppearanceId;
    public final AnimationAction animationAction;
    public final String appearanceEvidence;
    public final String actionEvidence;

    Result(String weaponItemId,String weaponAppearanceId,AnimationAction animationAction,
        String appearanceEvidence,String actionEvidence){
      this.weaponItemId=weaponItemId;
      this.weaponAppearanceId=weaponAppearanceId;
      this.animationAction=animationAction;
      this.appearanceEvidence=appearanceEvidence;
      this.actionEvidence=actionEvidence;
    }
  }

  /** Explicit skill/spell semantics override equipment; otherwise weapon family wins over PUNCH. */
  public Result resolve(RpgProgressionState rpg,AnimationAction explicitOverride){
    if(rpg==null)return new Result(null,null,explicitOverride==null?AnimationAction.PUNCH:explicitOverride,
        null,explicitOverride==null?UNARMED_RUNTIME:EXPLICIT_GAME_ACTION);
    RpgProgressionState.ItemDefinition weapon=rpg.equippedDefinition(RpgProgressionState.WEAPON_SLOT);
    String itemId=weapon==null?null:weapon.itemId;
    String appearanceId=weapon==null?null:weapon.appearanceId;
    if(explicitOverride!=null)
      return new Result(itemId,appearanceId,explicitOverride,
          appearanceId==null?null:SOURCE_NAMED_APPEARANCE,EXPLICIT_GAME_ACTION);
    if(weapon!=null&&weapon.basicAttackAction!=null)
      return new Result(itemId,appearanceId,weapon.basicAttackAction,
          appearanceId==null?null:SOURCE_NAMED_APPEARANCE,ADAPTED_PLAYTEST_ACTION);
    return new Result(itemId,appearanceId,AnimationAction.PUNCH,
        appearanceId==null?null:SOURCE_NAMED_APPEARANCE,UNARMED_RUNTIME);
  }

  public Result resolveBasicAttack(RpgProgressionState rpg){return resolve(rpg,null);}
}
