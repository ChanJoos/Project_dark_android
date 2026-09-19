package com.projectdark.mobile;

/** Four-direction geometry contract. Garment pixels are only accepted when their exact source frame family exists. */
public final class PlayerFourWayVisualContractAudit {
  private PlayerFourWayVisualContractAudit(){}
  public static boolean verify(){
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      if(CharacterRenderer.atlasRow(d)<0)return false;
      // IDLE + all four WALK frames must resolve the same body/shirt column and bounded registrations.
      if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.IDLE,0f)!=0)return false;
      for(int frame=0;frame<4;frame++){
        float clock=frame*CharacterRenderer.WALK_FRAME_SECONDS;
        int col=CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,clock);
        if(col!=frame+1)return false;
        if(Float.isNaN(CharacterRenderer.robeFrameRegistrationX(d,col))||Float.isNaN(CharacterRenderer.robeFrameRegistrationY(d,col)))return false;
        if(Float.isNaN(CharacterRenderer.weaponCarryOffsetX(d,col))||Float.isNaN(CharacterRenderer.weaponCarryOffsetY(d,col))||Float.isNaN(CharacterRenderer.weaponCarryAngle(d,col)))return false;
        if(CharacterRenderer.weaponCarryOffsetY(d,col)<16f||CharacterRenderer.weaponCarryOffsetY(d,col)>27f)return false;
      }
      // ATTACK is exactly: standing paper doll -> authored BODY+shirt+mw001 contact -> standing recovery.
      CharacterRenderer.AttackVisualComposition attack=CharacterRenderer.attackVisualComposition(d,CharacterVisualBinding.RESOLVED_SHIRT_APPEARANCE_ID,"mw001");
      if(!attack.robeVisible||!attack.weaponVisible||attack.sourceIndex!=CharacterRenderer.atlasRow(d))return false;
      if(CharacterRenderer.attackThreePose(0f)!=0||CharacterRenderer.attackThreePose(.4f)!=1||CharacterRenderer.attackThreePose(.9f)!=2)return false;
      if(CharacterRenderer.attackFallbackWeaponSwingReachable())return false;
    }
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      if(CharacterRenderer.weaponCarryMaxJumpX(d)>20f||CharacterRenderer.weaponCarryMaxJumpY(d)>8f||CharacterRenderer.weaponCarryMaxJumpAngle(d)>20f)return false;
    }
    return CharacterRenderer.robeRegistrationContinuityWithin(2f);
  }
  public static void main(String[] args){if(!verify())throw new AssertionError("PlayerFourWayVisualContractAudit failed");System.out.println("PLAYER_4WAY_VISUAL_CONTRACT=PASS");}
}
