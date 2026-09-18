package com.projectdark.mobile;

/** Regression audit for independent robe/weapon slots and the Game -> Visual semantic handoff. */
public final class EquipmentActionResolverAudit {
  private EquipmentActionResolverAudit(){}

  public static boolean verify(){
    RpgProgressionState rpg=new RpgProgressionState();
    EquipmentActionResolver resolver=new EquipmentActionResolver();
    RpgProgressionState.ItemDefinition weapon=rpg.itemDefinitions().get(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID);
    if(weapon==null||!RpgProgressionState.WEAPON_SLOT.equals(weapon.equipSlot))return false;
    if(!RpgProgressionState.PLAYTEST_WEAPON_APPEARANCE_ID.equals(weapon.appearanceId))return false;
    if(weapon.basicAttackAction!=AnimationAction.SWING||weapon.evidence!=RpgProgressionState.Evidence.ADAPTED)return false;
    if(!Integer.valueOf(1).equals(rpg.inventory().get(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID)))return false;

    EquipmentActionResolver.Result unarmed=resolver.resolveBasicAttack(rpg);
    if(unarmed.animationAction!=AnimationAction.PUNCH||unarmed.weaponAppearanceId!=null)return false;
    if(rpg.equip(RpgProgressionState.STARTER_SHIRT_ITEM_ID)!=RpgProgressionState.EquipResult.EQUIPPED)return false;
    if(rpg.equip(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID)!=RpgProgressionState.EquipResult.EQUIPPED)return false;
    if(!RpgProgressionState.STARTER_SHIRT_ITEM_ID.equals(rpg.equipment().get(RpgProgressionState.ARMOR_SLOT)))return false;
    if(!RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID.equals(rpg.equipment().get(RpgProgressionState.WEAPON_SLOT)))return false;

    EquipmentActionResolver.Result armed=resolver.resolveBasicAttack(rpg);
    if(armed.animationAction!=AnimationAction.SWING)return false;
    if(!RpgProgressionState.PLAYTEST_WEAPON_APPEARANCE_ID.equals(armed.weaponAppearanceId))return false;
    if(!EquipmentActionResolver.SOURCE_NAMED_APPEARANCE.equals(armed.appearanceEvidence))return false;
    if(!EquipmentActionResolver.ADAPTED_PLAYTEST_ACTION.equals(armed.actionEvidence))return false;
    EquipmentActionResolver.Result cast=resolver.resolve(rpg,AnimationAction.CAST);
    if(cast.animationAction!=AnimationAction.CAST||!armed.weaponAppearanceId.equals(cast.weaponAppearanceId))return false;
    if(!EquipmentActionResolver.EXPLICIT_GAME_ACTION.equals(cast.actionEvidence))return false;

    if(rpg.equip(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID)!=RpgProgressionState.EquipResult.UNEQUIPPED)return false;
    if(!RpgProgressionState.STARTER_SHIRT_ITEM_ID.equals(rpg.equipment().get(RpgProgressionState.ARMOR_SLOT)))return false;
    return resolver.resolveBasicAttack(rpg).animationAction==AnimationAction.PUNCH;
  }

  public static void main(String[] args){
    if(!verify())throw new AssertionError("EquipmentActionResolverAudit failed");
    System.out.println("EquipmentActionResolverAudit PASS");
  }
}
