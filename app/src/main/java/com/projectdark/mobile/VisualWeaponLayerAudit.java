package com.projectdark.mobile;

/** Android-free audit for BODY -> ROBE -> WEAPON projection and semantic attack input. */
public final class VisualWeaponLayerAudit {
  private VisualWeaponLayerAudit(){}

  public static boolean verify(){
    RpgProgressionState rpg=new RpgProgressionState();
    if(rpg.equip(RpgProgressionState.PLAYTEST_ROBE_ITEM_ID)!=RpgProgressionState.EquipResult.EQUIPPED)return false;
    if(rpg.equip(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID)!=RpgProgressionState.EquipResult.EQUIPPED)return false;
    CharacterVisualBinding both=CharacterVisualBinding.from(rpg);
    if(!CharacterVisualBinding.RESOLVED_SHIRT_APPEARANCE_ID.equals(both.equipmentVisualRef()))return false;
    if(!CharacterVisualBinding.RESOLVED_WEAPON_APPEARANCE_ID.equals(both.weaponVisualRef()))return false;
    if(!both.hasResolvedSpriteAssets()||!both.isDefinitionConsistent())return false;

    CharacterRenderer.Pose swing=new CharacterRenderer.Pose(0,0,CharacterRenderer.Direction.SE,
        CharacterRenderer.State.ATTACK,0,.1f,.4f,false,both.equipmentVisualRef(),both.weaponVisualRef(),
        CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE,AnimationAction.SWING);
    if(swing.animationAction!=AnimationAction.SWING)return false;

    if(rpg.equip(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID)!=RpgProgressionState.EquipResult.UNEQUIPPED)return false;
    CharacterVisualBinding robeOnly=CharacterVisualBinding.from(rpg);
    if(!CharacterVisualBinding.RESOLVED_SHIRT_APPEARANCE_ID.equals(robeOnly.equipmentVisualRef()))return false;
    if(!CharacterVisualBinding.ASSET_STATUS.equals(robeOnly.weaponVisualRef()))return false;
    return robeOnly.hasResolvedSpriteAssets();
  }

  public static void main(String[] args){
    if(!verify())throw new AssertionError("VisualWeaponLayerAudit failed");
    System.out.println("VisualWeaponLayerAudit PASS");
  }
}
