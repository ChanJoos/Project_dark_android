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
    if(CharacterVisualBinding.garmentCoverageForAppearance(CharacterVisualBinding.RESOLVED_ROBE_APPEARANCE_ID)!=CharacterVisualBinding.GarmentCoverage.FULL_BODY)return false;
    if(CharacterVisualBinding.visualSlotForAppearance(CharacterVisualBinding.RESOLVED_ROBE_APPEARANCE_ID)!=CharacterVisualBinding.VisualSlot.FULL_BODY)return false;
    if(CharacterVisualBinding.visualSlotForAppearance(CharacterVisualBinding.RESOLVED_WEAPON_APPEARANCE_ID)!=CharacterVisualBinding.VisualSlot.WEAPON)return false;

    if(Math.abs(CharacterRenderer.PLAYER_RENDER_SCALE-USER_APPROVED_PLAYER_RENDER_SCALE)>.0001f)return false;
    if(Math.abs(CharacterRenderer.SOURCE_PRESENTATION_SCALE-(1.70f/1.50f))>.0001f)return false;
    if(Math.abs(CharacterRenderer.SHADOW_RENDER_SCALE-USER_APPROVED_SHADOW_RENDER_SCALE)>.0001f)return false;
    if(Math.abs(CharacterRenderer.WALK_CYCLE_SECONDS-EXPECTED_WALK_CYCLE_SECONDS)>.0001f||Math.abs(CharacterRenderer.WALK_FRAMES_PER_SECOND-EXPECTED_WALK_FRAMES_PER_SECOND)>.0001f)return false;
    if(CharacterRenderer.LOGICAL_FOOT_ANCHOR_Y!=0f||CharacterRenderer.ATTACK_TRAIL_GHOSTS!=0)return false;
    if(!CharacterRenderer.ACTION_TEMPORAL_EVIDENCE.contains("UNRESOLVED")||!CharacterRenderer.ACTION_TEMPORAL_EVIDENCE.contains("no fabricated temporal sequence"))return false;
    if(!CharacterRenderer.ATTACK_GEOMETRY_EVIDENCE.contains("alpha bounds")||!CharacterRenderer.ATTACK_GEOMETRY_EVIDENCE.contains("height<=1px")||!CharacterRenderer.ATTACK_GEOMETRY_EVIDENCE.contains("foot<=1px")||!CharacterRenderer.ATTACK_GEOMETRY_EVIDENCE.contains("center<=2px"))return false;
    if(!CharacterRenderer.ROBE_ATTACK_EVIDENCE.contains("FULL_BODY")||!CharacterRenderer.HIT_POSE_EVIDENCE.contains("disabled")||!CharacterRenderer.WEAPON_TRANSFORM_EVIDENCE.contains("exactly one"))return false;
    if(CharacterRenderer.hitCharacterPoseEnabled()||!CharacterRenderer.weaponTransformUsesSingleMirror())return false;
    if(!CharacterRenderer.alphaGeometryWithinTolerance(40,41,1,2))return false;
    if(CharacterRenderer.alphaGeometryWithinTolerance(40,42,0,0))return false;
    if(CharacterRenderer.alphaGeometryWithinTolerance(40,40,2,0))return false;
    if(CharacterRenderer.alphaGeometryWithinTolerance(40,40,0,3))return false;

    if(CharacterRenderer.atlasRow(CharacterRenderer.Direction.NW)!=0||CharacterRenderer.atlasRow(CharacterRenderer.Direction.NE)!=1||CharacterRenderer.atlasRow(CharacterRenderer.Direction.SW)!=2||CharacterRenderer.atlasRow(CharacterRenderer.Direction.SE)!=3)return false;
    if(CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.NE)!=0||CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.SE)!=1||CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.NW)!=2||CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.SW)!=3)return false;

    java.util.HashSet<String> signatures=new java.util.HashSet<>();
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      CharacterRenderer.AttackVisualComposition c=CharacterRenderer.attackVisualComposition(d,"mu0000058","mw001");
      if(c.direction!=d||c.presentationScale!=1.70f||!c.robeVisible||!c.weaponVisible||!signatures.add(c.signature()))return false;
      boolean left=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW;
      boolean north=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE;
      if(c.mirrorBody!=left||CharacterRenderer.bodyMirrorX(d)!=left||CharacterRenderer.weaponMirrorX(d)!=left||c.weaponBehindBody!=north)return false;
      if(Math.abs(CharacterRenderer.normalizedActionScale(c.sourceIndex)-CharacterRenderer.SOURCE_PRESENTATION_SCALE)>.0001f)return false;
    }
    if(signatures.size()!=4)return false;

    CharacterRenderer.AttackVisualComposition bare=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,null,null);
    CharacterRenderer.AttackVisualComposition robe=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,"mu0000058",null);
    CharacterRenderer.AttackVisualComposition weapon=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,null,"mw001");
    CharacterRenderer.AttackVisualComposition both=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,"mu0000058","mw001");
    if(bare.robeVisible||bare.weaponVisible||!robe.robeVisible||robe.weaponVisible||weapon.robeVisible||!weapon.weaponVisible||!both.robeVisible||!both.weaponVisible)return false;
    if(CharacterRenderer.sourceActionLayersReadyContract("mu0000058",null,true,false,true))return false;
    if(CharacterRenderer.sourceActionLayersReadyContract(null,"mw001",true,true,false))return false;
    if(!CharacterRenderer.sourceActionLayersReadyContract("mu0000058","mw001",true,true,true))return false;

    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      boolean north=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE;if(CharacterRenderer.weaponBehindBody(d)!=north)return false;
      float start=CharacterRenderer.weaponAttackAngle(d,0f),peak=CharacterRenderer.weaponAttackAngle(d,.55f),end=CharacterRenderer.weaponAttackAngle(d,1f);
      if(Float.isNaN(start)||Float.isNaN(peak)||Float.isNaN(end)||Math.abs(peak-start)<20f||Math.abs(end-start)>.001f)return false;
      for(int col=0;col<CharacterRenderer.IDLE_WALK_COLUMNS;col++){
        float x=CharacterRenderer.weaponCarryOffsetX(d,col),y=CharacterRenderer.weaponCarryOffsetY(d,col),angle=CharacterRenderer.weaponCarryAngle(d,col);
        if(Float.isNaN(x)||Float.isNaN(y)||Float.isNaN(angle)||y<16f||y>27f||Math.abs(x)>11f)return false;
      }
    }

    if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.IDLE,.30f)!=0||CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.ATTACK,.30f)!=0||CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.HIT,.30f)!=0)return false;
    if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,0f)!=1||CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,.12f)!=2||CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,.24f)!=3||CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,.36f)!=4)return false;
    if(CharacterRenderer.walkFrameIndex(0f)!=0||CharacterRenderer.walkFrameIndex(.119f)!=0||CharacterRenderer.walkFrameIndex(.120f)!=1||CharacterRenderer.walkFrameIndex(.240f)!=2||CharacterRenderer.walkFrameIndex(.360f)!=3||CharacterRenderer.walkFrameIndex(.480f)!=0)return false;
    if(!CharacterRenderer.usesSourceActionPose(CharacterRenderer.State.ATTACK,AnimationAction.SWING)||CharacterRenderer.usesSourceActionPose(CharacterRenderer.State.IDLE,AnimationAction.SWING)||CharacterRenderer.legacyProceduralAttackReachable())return false;
    return true;
  }

  public static String summary(){return "directions=4,scale="+CharacterRenderer.PLAYER_RENDER_SCALE+",walkCycle="+CharacterRenderer.WALK_CYCLE_SECONDS+",coverage=FULL_BODY|UPPER|LOWER,attackAlphaTolerance=1/1/2,hitPose=disabled,weaponMirror=single,paperDollLayers="+CharacterRenderer.PAPER_DOLL_ORDER.size();}
  private static CharacterRenderer.EffectFamily defaultEffect(CharacterRenderer.State state){switch(state){case CAST:return CharacterRenderer.EffectFamily.CAST;default:return CharacterRenderer.EffectFamily.NONE;}}
  public static void main(String[] args){if(!passes())throw new AssertionError("CharacterRendererAudit failed: "+summary());System.out.println("CharacterRendererAudit PASS: "+summary());}
}
