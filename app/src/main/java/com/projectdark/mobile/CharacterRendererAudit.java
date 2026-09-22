package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Focused static contract audit; never used as an app-startup fail-fast gate. */
public final class CharacterRendererAudit {
  public static final int EXPECTED_DIRECTION_COUNT=4;
  public static final int EXPECTED_STATE_COUNT=7;
  public static final int EXPECTED_LAYER_COUNT=5;
  public static final int EXPECTED_MATRIX_CASES=28;
  public static final int EXPECTED_PAPER_DOLL_LAYER_COUNT=12;
  public static final float USER_APPROVED_PLAYER_RENDER_SCALE=1.70f;
  public static final float USER_APPROVED_SHADOW_RENDER_SCALE=0.72f;
  public static final float EXPECTED_WALK_FRAMES_PER_SECOND=8.333333f;
  public static final float EXPECTED_WALK_CYCLE_SECONDS=.48f;

  public static final class Case {
    public final CharacterRenderer.Direction direction;public final CharacterRenderer.State state;public final CharacterRenderer.EffectFamily effectFamily;
    Case(CharacterRenderer.Direction d,CharacterRenderer.State s,CharacterRenderer.EffectFamily e){direction=d;state=s;effectFamily=e;}
  }
  private CharacterRendererAudit(){}
  public static List<Case> matrix(){List<Case> out=new ArrayList<>();for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(CharacterRenderer.State s:CharacterRenderer.State.values())out.add(new Case(d,s,defaultEffect(s)));return Collections.unmodifiableList(out);}

  public static boolean passes(){
    if(!CanonicalActorFacingAudit.passes())return false;
    if(CharacterRenderer.Direction.values().length!=EXPECTED_DIRECTION_COUNT||CharacterRenderer.State.values().length!=EXPECTED_STATE_COUNT)return false;
    if(CharacterRenderer.DRAW_ORDER.size()!=EXPECTED_LAYER_COUNT||matrix().size()!=EXPECTED_MATRIX_CASES)return false;
    if(CharacterRenderer.PAPER_DOLL_ORDER.size()!=EXPECTED_PAPER_DOLL_LAYER_COUNT)return false;
    if(!CharacterRenderer.PAPER_DOLL_ORDER.contains("FULL_BODY")||!CharacterRenderer.PAPER_DOLL_ORDER.contains("UPPER")||!CharacterRenderer.PAPER_DOLL_ORDER.contains("LOWER"))return false;
    if(!CharacterRenderer.PAPER_DOLL_ORDER.contains("HEAD")||!CharacterRenderer.PAPER_DOLL_ORDER.contains("HANDS")||!CharacterRenderer.PAPER_DOLL_ORDER.contains("FEET")||!CharacterRenderer.PAPER_DOLL_ORDER.contains("WEAPON")||!CharacterRenderer.PAPER_DOLL_ORDER.contains("SHIELD"))return false;
    if(CharacterVisualBinding.garmentCoverageForAppearance(CharacterVisualBinding.RESOLVED_SHIRT_APPEARANCE_ID)!=CharacterVisualBinding.GarmentCoverage.UPPER)return false;
    if(CharacterVisualBinding.visualSlotForAppearance(CharacterVisualBinding.RESOLVED_SHIRT_APPEARANCE_ID)!=CharacterVisualBinding.VisualSlot.UPPER)return false;
    if(CharacterVisualBinding.visualSlotForAppearance(CharacterVisualBinding.RESOLVED_WEAPON_APPEARANCE_ID)!=CharacterVisualBinding.VisualSlot.WEAPON)return false;

    if(Math.abs(CharacterRenderer.PLAYER_RENDER_SCALE-USER_APPROVED_PLAYER_RENDER_SCALE)>.0001f)return false;
    if(Math.abs(CharacterRenderer.SOURCE_PRESENTATION_SCALE-(1.70f/1.50f))>.0001f)return false;
    if(Math.abs(CharacterRenderer.SHADOW_RENDER_SCALE-USER_APPROVED_SHADOW_RENDER_SCALE)>.0001f)return false;
    if(Math.abs(CharacterRenderer.WALK_CYCLE_SECONDS-EXPECTED_WALK_CYCLE_SECONDS)>.0001f||Math.abs(CharacterRenderer.WALK_FRAMES_PER_SECOND-EXPECTED_WALK_FRAMES_PER_SECOND)>.0001f)return false;
    if(CharacterRenderer.LOGICAL_FOOT_ANCHOR_Y!=0f||CharacterRenderer.ATTACK_TRAIL_GHOSTS!=0)return false;

    if(!CharacterRenderer.ACTION_TEMPORAL_EVIDENCE.contains("UNRESOLVED")||!CharacterRenderer.ACTION_TEMPORAL_EVIDENCE.contains("wind-up")||!CharacterRenderer.ACTION_TEMPORAL_EVIDENCE.contains("recovery")||!CharacterRenderer.ACTION_TEMPORAL_EVIDENCE.contains("without fabricated intermediate frames"))return false;
    if(!CharacterRenderer.attackUsesSinglePosePlaceholder()||CharacterRenderer.attackTemporalSourceResolved())return false;
    if(!CharacterRenderer.ATTACK_GEOMETRY_EVIDENCE.contains("no destructive action crop")||!CharacterRenderer.ATTACK_GEOMETRY_EVIDENCE.contains("1.70"))return false;
    if(CharacterRenderer.attackUsesDestructiveCrop())return false;
    if(CharacterRenderer.attackUsesActionPose(0f)||CharacterRenderer.attackUsesActionPose(.17f)||!CharacterRenderer.attackUsesActionPose(.18f)||!CharacterRenderer.attackUsesActionPose(.50f)||CharacterRenderer.attackUsesActionPose(.62f)||CharacterRenderer.attackUsesActionPose(1f))return false;
    if(CharacterRenderer.attackThreePose(0f)!=0||CharacterRenderer.attackThreePose(.17f)!=0||CharacterRenderer.attackThreePose(.18f)!=1||CharacterRenderer.attackThreePose(.61f)!=1||CharacterRenderer.attackThreePose(.62f)!=2||CharacterRenderer.attackThreePose(1f)!=2)return false;
    if(!CharacterRenderer.ATTACK_FALLBACK_EVIDENCE.contains("weapon-only attack animation is unreachable"))return false;
    if(!CharacterRenderer.ROBE_ATTACK_EVIDENCE.contains("FULL_BODY")||!CharacterRenderer.ROBE_ATTACK_EVIDENCE.contains("atomically"))return false;
    if(!CharacterRenderer.ROBE_WALK_EVIDENCE.contains("SW/SE")||!CharacterRenderer.ROBE_WALK_EVIDENCE.contains("packaged alpha pixels"))return false;
    if(!CharacterRenderer.robeRegistrationContinuityWithin(2f))return false;
    if(!CharacterRenderer.HIT_POSE_EVIDENCE.contains("disabled")||!CharacterRenderer.WEAPON_TRANSFORM_EVIDENCE.contains("dominantHand")||!CharacterRenderer.WEAPON_TRANSFORM_EVIDENCE.contains("no independent"))return false;
    if(CharacterRenderer.hitCharacterPoseEnabled()||CharacterRenderer.attackFallbackWeaponSwingReachable())return false;

    if(CharacterRenderer.atlasRow(CharacterRenderer.Direction.NW)!=0||CharacterRenderer.atlasRow(CharacterRenderer.Direction.NE)!=1||CharacterRenderer.atlasRow(CharacterRenderer.Direction.SW)!=2||CharacterRenderer.atlasRow(CharacterRenderer.Direction.SE)!=3)return false;
    if(CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.NW)!=0||CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.NE)!=1||CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.SW)!=2||CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.SE)!=3)return false;
    if(CharacterRenderer.robeUsesRuntimeXYRegistration(CharacterRenderer.Direction.NW)||CharacterRenderer.robeUsesRuntimeXYRegistration(CharacterRenderer.Direction.NE))return false;
    if(!CharacterRenderer.robeUsesRuntimeXYRegistration(CharacterRenderer.Direction.SW)||!CharacterRenderer.robeUsesRuntimeXYRegistration(CharacterRenderer.Direction.SE))return false;

    // P3: NW/NE frozen compatibility registration remains unchanged; SW/SE runtime path is semantic.
    float[][] expectedNorthX={{-1,-2,-1,0,1},{2,2,2,0,1}};
    CharacterRenderer.Direction[] northDirs={CharacterRenderer.Direction.NW,CharacterRenderer.Direction.NE};
    for(int r=0;r<2;r++)for(int col=0;col<CharacterRenderer.IDLE_WALK_COLUMNS;col++){
      if(CharacterRenderer.robeFrameRegistrationX(northDirs[r],col)!=expectedNorthX[r][col]||CharacterRenderer.robeFrameRegistrationY(northDirs[r],col)!=0f)return false;
    }

    java.util.HashSet<String> signatures=new java.util.HashSet<>();
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      CharacterRenderer.AttackVisualComposition c=CharacterRenderer.attackVisualComposition(d,CharacterVisualBinding.RESOLVED_LUERS_ROBE_APPEARANCE_ID,"mw001");
      if(c.direction!=d||c.presentationScale!=1.70f||!c.robeVisible||!c.weaponVisible||!signatures.add(c.signature()))return false;
      boolean expectedBodyMirror=false;
      boolean expectedWeaponMirror=false;
      boolean north=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE;
      if(c.mirrorBody!=CharacterRenderer.bodyMirrorX(d)||CharacterRenderer.bodyMirrorX(d)!=expectedBodyMirror)return false;
      if(CharacterRenderer.weaponMirrorX(d)!=expectedWeaponMirror||c.weaponBehindBody!=north)return false;
      if(Math.abs(CharacterRenderer.normalizedActionScale(c.sourceIndex)-(1.70f/1.50f))>.0001f)return false;
      if(Float.isNaN(CharacterRenderer.actionSemanticPivotX(c.sourceIndex))||Float.isNaN(CharacterRenderer.actionSemanticFootY(c.sourceIndex)))return false;
    }
    if(signatures.size()!=4)return false;

    // P4: every action source is translated to the shared semantic pivot/foot contract without crop/scale changes.
    for(int source=0;source<CharacterRenderer.SOURCE_ACTION_COUNT;source++){
      float pivot=CharacterRenderer.actionSemanticPivotX(source),foot=CharacterRenderer.actionSemanticFootY(source);
      if(pivot<10f||pivot>18f||foot<46f||foot>60f)return false;
      if(CharacterRenderer.normalizedActionScale(source)!=(1.70f/1.50f))return false;
    }

    CharacterRenderer.AttackVisualComposition bare=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,null,null);
    CharacterRenderer.AttackVisualComposition robe=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,CharacterVisualBinding.RESOLVED_LUERS_ROBE_APPEARANCE_ID,null);
    CharacterRenderer.AttackVisualComposition weapon=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,null,"mw001");
    CharacterRenderer.AttackVisualComposition both=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,CharacterVisualBinding.RESOLVED_LUERS_ROBE_APPEARANCE_ID,"mw001");
    if(bare.robeVisible||bare.weaponVisible||!robe.robeVisible||robe.weaponVisible||weapon.robeVisible||!weapon.weaponVisible||!both.robeVisible||!both.weaponVisible)return false;
    if(CharacterRenderer.sourceActionLayersReadyContract(CharacterVisualBinding.RESOLVED_LUERS_ROBE_APPEARANCE_ID,null,true,false,true))return false;
    if(CharacterRenderer.sourceActionLayersReadyContract(null,"mw001",true,true,false))return false;
    if(!CharacterRenderer.sourceActionLayersReadyContract(CharacterVisualBinding.RESOLVED_LUERS_ROBE_APPEARANCE_ID,"mw001",true,true,true))return false;

    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      boolean north=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE;if(CharacterRenderer.weaponBehindBody(d)!=north)return false;
      float start=CharacterRenderer.weaponAttackAngle(d,0f),peak=CharacterRenderer.weaponAttackAngle(d,.55f),end=CharacterRenderer.weaponAttackAngle(d,1f);
      if(Float.isNaN(start)||Float.isNaN(peak)||Float.isNaN(end)||Math.abs(peak-start)>.001f||Math.abs(end-start)>.001f||CharacterRenderer.weaponAttackUsesIndependentSwing())return false;
      if(Math.abs(CharacterRenderer.weaponAttackGripX(d))>.5f||Math.abs(CharacterRenderer.weaponAttackGripY(d))>.5f)return false; CharacterSemanticRig.Anchors fb=null; float hx=CharacterRenderer.actionWeaponHandX(d,fb),hy=CharacterRenderer.actionWeaponHandY(d,fb); if(hx<4f||hx>16f||hy<26f||hy>29f)return false;
      for(int col=0;col<CharacterRenderer.IDLE_WALK_COLUMNS;col++){
        float x=CharacterRenderer.weaponCarryOffsetX(d,col),y=CharacterRenderer.weaponCarryOffsetY(d,col),angle=CharacterRenderer.weaponCarryAngle(d,col),robeX=CharacterRenderer.robeFrameRegistrationX(d,col),robeY=CharacterRenderer.robeFrameRegistrationY(d,col);
        if(Float.isNaN(x)||Float.isNaN(y)||Float.isNaN(angle)||Float.isNaN(robeX)||Float.isNaN(robeY)||y<16f||y>27f||Math.abs(x)>11f||Math.abs(robeX)>3f||Math.abs(robeY)>2f)return false;
      }
    }

    // Legacy tables remain bounded compatibility evidence; runtime weapon position is semantic hand attachment.
    if(CharacterRenderer.weaponCarryMaxJumpX(CharacterRenderer.Direction.SE)>1f)return false;
    if(CharacterRenderer.weaponCarryMaxJumpY(CharacterRenderer.Direction.SE)>1f)return false;
    if(CharacterRenderer.weaponCarryMaxJumpAngle(CharacterRenderer.Direction.SE)>2f)return false;
    if(CharacterRenderer.weaponCarryAngle(CharacterRenderer.Direction.NW,0)<=0f||CharacterRenderer.weaponCarryAngle(CharacterRenderer.Direction.SW,0)<=0f)return false;
    if(CharacterRenderer.weaponCarryAngle(CharacterRenderer.Direction.NE,0)>=0f||CharacterRenderer.weaponCarryAngle(CharacterRenderer.Direction.SE,0)>=0f)return false;
    if(CharacterRenderer.weaponAttackAngle(CharacterRenderer.Direction.NW,.55f)<=0f)return false;

    if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.IDLE,.30f)!=0||CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.ATTACK,.30f)!=0||CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.HIT,.30f)!=0)return false;
    if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,0f)!=1||CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,.12f)!=2||CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,.24f)!=3||CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,.36f)!=4)return false;
    if(CharacterRenderer.walkFrameIndex(0f)!=0||CharacterRenderer.walkFrameIndex(.119f)!=0||CharacterRenderer.walkFrameIndex(.120f)!=1||CharacterRenderer.walkFrameIndex(.240f)!=2||CharacterRenderer.walkFrameIndex(.360f)!=3||CharacterRenderer.walkFrameIndex(.480f)!=0)return false;
    if(!CharacterRenderer.usesSourceActionPose(CharacterRenderer.State.ATTACK,AnimationAction.SWING)||CharacterRenderer.usesSourceActionPose(CharacterRenderer.State.IDLE,AnimationAction.SWING)||CharacterRenderer.legacyProceduralAttackReachable())return false;
    return true;
  }

  public static String summary(){return "directions=4,scale="+CharacterRenderer.PLAYER_RENDER_SCALE+",walkCycle="+CharacterRenderer.WALK_CYCLE_SECONDS+",P3=SWSE-semantic-rig,P4=attack-single-pose-placeholder-no-crop,P5=mw001-body-hand-attachment,paperDollLayers="+CharacterRenderer.PAPER_DOLL_ORDER.size();}
  private static CharacterRenderer.EffectFamily defaultEffect(CharacterRenderer.State state){switch(state){case CAST:return CharacterRenderer.EffectFamily.CAST;default:return CharacterRenderer.EffectFamily.NONE;}}
  public static void main(String[] args){if(!passes())throw new AssertionError("CharacterRendererAudit failed: "+summary());System.out.println("CharacterRendererAudit PASS: "+summary());}
}
