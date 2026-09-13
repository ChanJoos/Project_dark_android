package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Static contract audit for the crash-safe four-diagonal starter-peasant renderer. */
public final class CharacterRendererAudit {
  public static final int EXPECTED_DIRECTION_COUNT=4;
  public static final int EXPECTED_STATE_COUNT=7;
  public static final int EXPECTED_LAYER_COUNT=5;
  public static final int EXPECTED_MATRIX_CASES=28;
  public static final int EXPECTED_PAPER_DOLL_LAYER_COUNT=10;
  public static final float USER_APPROVED_PLAYER_RENDER_SCALE=1.70f;
  public static final float USER_APPROVED_SHADOW_RENDER_SCALE=0.72f;
  public static final float EXPECTED_WALK_FRAMES_PER_SECOND=8.333333f;
  public static final float EXPECTED_WALK_CYCLE_SECONDS=.48f;

  public static final class Case {
    public final CharacterRenderer.Direction direction;
    public final CharacterRenderer.State state;
    public final CharacterRenderer.EffectFamily effectFamily;
    Case(CharacterRenderer.Direction direction,CharacterRenderer.State state,CharacterRenderer.EffectFamily effectFamily){this.direction=direction;this.state=state;this.effectFamily=effectFamily;}
  }

  private CharacterRendererAudit(){}

  public static List<Case> matrix(){
    List<Case> cases=new ArrayList<>();
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())
      for(CharacterRenderer.State s:CharacterRenderer.State.values())
        cases.add(new Case(d,s,defaultEffect(s)));
    return Collections.unmodifiableList(cases);
  }

  public static boolean passes(){
    if(!CanonicalActorFacingAudit.passes())return false;
    if(CharacterRenderer.Direction.values().length!=EXPECTED_DIRECTION_COUNT)return false;
    if(CharacterRenderer.State.values().length!=EXPECTED_STATE_COUNT)return false;
    if(CharacterRenderer.DRAW_ORDER.size()!=EXPECTED_LAYER_COUNT||matrix().size()!=EXPECTED_MATRIX_CASES)return false;
    if(!"PEASANT_MM001_BASE_20260912_R1".equals(CharacterRenderer.PRESENTATION_PROFILE))return false;
    if(!"PEASANT".equals(CharacterRenderer.STARTER_ARCHETYPE)||!"PRE_CLASS".equals(CharacterRenderer.STARTER_CLASS_STATE))return false;
    if(!"player.peasant.mm001.idle_walk.production-2026-09-12".equals(CharacterRenderer.IDLE_WALK_ASSET_ID))return false;
    if(!CharacterRenderer.HARD_PIXEL_GRID||!CharacterRenderer.STARTER_WEAPONLESS||!CharacterRenderer.STARTER_OFFHAND_EMPTY)return false;
    if(CharacterRenderer.LEGACY_MARTIAL_ASSET_ALLOWED)return false;
    if(CharacterRenderer.PAPER_DOLL_ORDER.size()!=EXPECTED_PAPER_DOLL_LAYER_COUNT)return false;
    if(!"BODY_BASE".equals(CharacterRenderer.PAPER_DOLL_ORDER.get(0)))return false;
    if(!"OFFHAND".equals(CharacterRenderer.PAPER_DOLL_ORDER.get(CharacterRenderer.PAPER_DOLL_ORDER.size()-1)))return false;
    if(CharacterRenderer.ATLAS_FRAME_WIDTH!=24||CharacterRenderer.ATLAS_FRAME_HEIGHT!=32)return false;
    if(CharacterRenderer.SOURCE_FRAME_WIDTH!=36||CharacterRenderer.SOURCE_FRAME_HEIGHT!=48)return false;
    if(CharacterRenderer.SOURCE_FOOT_ANCHOR_X!=18||CharacterRenderer.SOURCE_FOOT_ANCHOR_Y!=46)return false;
    if(CharacterRenderer.IDLE_WALK_COLUMNS!=5||CharacterRenderer.ATTACK_COLUMNS!=4||CharacterRenderer.ATLAS_ROWS!=4)return false;
    if(CharacterRenderer.IDLE_WALK_WIDTH!=120||CharacterRenderer.ACTION_WIDTH!=96||CharacterRenderer.ATLAS_HEIGHT!=128)return false;
    if(CharacterRenderer.SOURCE_IDLE_WALK_WIDTH!=180||CharacterRenderer.SOURCE_ATLAS_HEIGHT!=192)return false;
    if(CharacterRenderer.SOURCE_ACTION_COUNT!=4||CharacterRenderer.ATTACK_TRAIL_GHOSTS!=0)return false;
    if(!CharacterRenderer.ACTION_SOURCE_EVIDENCE.contains("ADAPTED PLAYTEST ACTION GROUP"))return false;
    if(!CharacterRenderer.ACTION_TEMPORAL_EVIDENCE.contains("UNRESOLVED")||!CharacterRenderer.ACTION_TEMPORAL_EVIDENCE.contains("no fabricated temporal sequence"))return false;
    if(!CharacterRenderer.WEAPON_SOURCE_EVIDENCE.contains("mw001"))return false;
    if(!CharacterRenderer.ATTACK_PRESENTATION_EVIDENCE.contains("no afterimage/trail"))return false;
    if(!CharacterRenderer.ATTACK_DIRECTION_EVIDENCE.contains("runtime Pose.direction")||!CharacterRenderer.ATTACK_DIRECTION_EVIDENCE.contains("UNRESOLVED"))return false;
    if(!CharacterRenderer.PAPER_DOLL_ATTACK_EVIDENCE.contains("current equipmentVisualRef")||!CharacterRenderer.PAPER_DOLL_ATTACK_EVIDENCE.contains("no baked equipment"))return false;
    if(!CharacterRenderer.ATTACK_FALLBACK_EVIDENCE.contains("source IDLE paper doll")||!CharacterRenderer.ATTACK_FALLBACK_EVIDENCE.contains("canonical weapon depth"))return false;
    if(!CharacterRenderer.ATTACK_GEOMETRY_EVIDENCE.contains("36x48")||!CharacterRenderer.ATTACK_GEOMETRY_EVIDENCE.contains("foot anchor")||!CharacterRenderer.ATTACK_GEOMETRY_EVIDENCE.contains("1.70"))return false;
    if(!CharacterRenderer.ROBE_ATTACK_EVIDENCE.contains("mu0000058")||!CharacterRenderer.ROBE_ATTACK_EVIDENCE.contains("whole attack falls back"))return false;
    if(Math.abs(CharacterRenderer.PLAYER_RENDER_SCALE-USER_APPROVED_PLAYER_RENDER_SCALE)>.0001f)return false;
    if(Math.abs(CharacterRenderer.SOURCE_PRESENTATION_SCALE-(1.70f/1.50f))>.0001f)return false;
    if(Math.abs(CharacterRenderer.SHADOW_RENDER_SCALE-USER_APPROVED_SHADOW_RENDER_SCALE)>.0001f)return false;
    if(Math.abs(CharacterRenderer.WALK_CYCLE_SECONDS-EXPECTED_WALK_CYCLE_SECONDS)>.0001f)return false;
    if(Math.abs(CharacterRenderer.WALK_FRAMES_PER_SECOND-EXPECTED_WALK_FRAMES_PER_SECOND)>.0001f)return false;
    if(CharacterRenderer.LOGICAL_FOOT_ANCHOR_Y!=0f)return false;

    if(CharacterRenderer.atlasRow(CharacterRenderer.Direction.NW)!=0)return false;
    if(CharacterRenderer.atlasRow(CharacterRenderer.Direction.NE)!=1)return false;
    if(CharacterRenderer.atlasRow(CharacterRenderer.Direction.SW)!=2)return false;
    if(CharacterRenderer.atlasRow(CharacterRenderer.Direction.SE)!=3)return false;
    for(int row=0;row<4;row++){
      CharacterRenderer.Direction d=CharacterRenderer.visualFacingForRow(row);
      if(d==null||CharacterRenderer.atlasRow(d)!=row)return false;
    }

    if(CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.NE)!=0)return false;
    if(CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.SE)!=1)return false;
    if(CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.NW)!=2)return false;
    if(CharacterRenderer.actionSourceIndex(CharacterRenderer.Direction.SW)!=3)return false;

    java.util.HashSet<String> facingSignatures=new java.util.HashSet<>();
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      CharacterRenderer.AttackVisualComposition composition=CharacterRenderer.attackVisualComposition(d,"mu0000058","mw001");
      if(composition.direction!=d||composition.presentationScale!=1.70f)return false;
      if(!composition.robeVisible||!composition.weaponVisible)return false;
      if(!facingSignatures.add(composition.signature()))return false;
      boolean left=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW;
      if(composition.mirrorBody!=left)return false;
      boolean north=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE;
      if(composition.weaponBehindBody!=north)return false;
      if(Math.abs(CharacterRenderer.normalizedActionScale(composition.sourceIndex)-CharacterRenderer.SOURCE_PRESENTATION_SCALE)>.0001f)return false;
      if(CharacterRenderer.actionVisibleHeightPixels(composition.sourceIndex)!=CharacterRenderer.SOURCE_FRAME_HEIGHT)return false;
      if(Math.abs(CharacterRenderer.actionHeightRatio(composition.sourceIndex)-1f)>.0001f)return false;
    }
    if(facingSignatures.size()!=EXPECTED_DIRECTION_COUNT)return false;
    if(CharacterRenderer.actionCanonicalVisibleHeightPixels()!=CharacterRenderer.SOURCE_FRAME_HEIGHT)return false;
    if(CharacterRenderer.actionCanonicalFootAnchorPixels()!=CharacterRenderer.SOURCE_FOOT_ANCHOR_Y)return false;

    CharacterRenderer.AttackVisualComposition bare=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,null,null);
    CharacterRenderer.AttackVisualComposition robeOnly=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,"mu0000058",null);
    CharacterRenderer.AttackVisualComposition weaponOnly=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,null,"mw001");
    CharacterRenderer.AttackVisualComposition equipped=CharacterRenderer.attackVisualComposition(CharacterRenderer.Direction.SE,"mu0000058","mw001");
    if(bare.robeVisible||bare.weaponVisible)return false;
    if(!robeOnly.robeVisible||robeOnly.weaponVisible)return false;
    if(weaponOnly.robeVisible||!weaponOnly.weaponVisible)return false;
    if(!equipped.robeVisible||!equipped.weaponVisible)return false;

    // Equipped attack layers are atomic: a missing equipped layer rejects source-action rendering.
    if(!CharacterRenderer.sourceActionLayersReadyContract(null,null,true,false,false))return false;
    if(CharacterRenderer.sourceActionLayersReadyContract("mu0000058",null,true,false,true))return false;
    if(!CharacterRenderer.sourceActionLayersReadyContract("mu0000058",null,true,true,true))return false;
    if(CharacterRenderer.sourceActionLayersReadyContract(null,"mw001",true,true,false))return false;
    if(!CharacterRenderer.sourceActionLayersReadyContract(null,"mw001",true,true,true))return false;
    if(CharacterRenderer.sourceActionLayersReadyContract("mu0000058","mw001",true,false,true))return false;
    if(CharacterRenderer.sourceActionLayersReadyContract("mu0000058","mw001",true,true,false))return false;
    if(!CharacterRenderer.sourceActionLayersReadyContract("mu0000058","mw001",true,true,true))return false;

    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      float carry=CharacterRenderer.weaponCarryAngle(d);
      if(Math.abs(carry)<45f||Math.abs(carry)>80f)return false;
      float start=CharacterRenderer.weaponAttackAngle(d,0f);
      float peak=CharacterRenderer.weaponAttackAngle(d,.55f);
      float end=CharacterRenderer.weaponAttackAngle(d,1f);
      if(Float.isNaN(start)||Float.isNaN(peak)||Float.isNaN(end))return false;
      if(Math.abs(peak-start)<20f)return false;
      if(Math.abs(end-start)>.001f)return false;
    }
    if(CharacterRenderer.weaponCarryOffsetX(CharacterRenderer.Direction.NW)>=0f)return false;
    if(CharacterRenderer.weaponCarryOffsetX(CharacterRenderer.Direction.SW)>=0f)return false;
    if(CharacterRenderer.weaponCarryOffsetX(CharacterRenderer.Direction.NE)<=0f)return false;
    if(CharacterRenderer.weaponCarryOffsetX(CharacterRenderer.Direction.SE)<=0f)return false;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      java.util.HashSet<String> anchors=new java.util.HashSet<>();
      for(int col=0;col<CharacterRenderer.IDLE_WALK_COLUMNS;col++){
        float x=CharacterRenderer.weaponCarryOffsetX(d,col),y=CharacterRenderer.weaponCarryOffsetY(d,col);
        float angle=CharacterRenderer.weaponCarryAngle(d,col);
        if(Float.isNaN(x)||Float.isNaN(y)||Float.isNaN(angle))return false;
        if(y<16f||y>27f||Math.abs(x)>11f||Math.abs(angle)<45f||Math.abs(angle)>80f)return false;
        anchors.add(x+":"+y+":"+angle);
      }
      if(anchors.size()<3)return false;
    }

    // BODY/ROBE/WEAPON share one direction/frame/anchor contract in IDLE/WALK/fallback.
    if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.IDLE,.30f)!=0)return false;
    if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.ATTACK,.30f)!=0)return false;
    if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.CAST,.30f)!=0)return false;
    if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,0f)!=1)return false;
    if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,.12f)!=2)return false;
    if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,.24f)!=3)return false;
    if(CharacterRenderer.paperDollAtlasColumn(CharacterRenderer.State.WALK,.36f)!=4)return false;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      boolean north=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE;
      if(CharacterRenderer.weaponBehindBody(d)!=north)return false;
    }

    if(!CharacterRenderer.usesSourceActionPose(CharacterRenderer.State.ATTACK,AnimationAction.SWING))return false;
    if(CharacterRenderer.usesSourceActionPose(CharacterRenderer.State.IDLE,AnimationAction.SWING))return false;
    if(CharacterRenderer.usesSourceActionPose(CharacterRenderer.State.WALK,AnimationAction.SWING))return false;
    if(CharacterRenderer.usesSourceActionPose(CharacterRenderer.State.ATTACK,AnimationAction.PUNCH))return false;
    if(CharacterRenderer.legacyProceduralAttackReachable())return false;

    if(CharacterRenderer.walkFrameIndex(0f)!=0||CharacterRenderer.walkFrameIndex(.119f)!=0)return false;
    if(CharacterRenderer.walkFrameIndex(.120f)!=1||CharacterRenderer.walkFrameIndex(.239f)!=1)return false;
    if(CharacterRenderer.walkFrameIndex(.240f)!=2||CharacterRenderer.walkFrameIndex(.359f)!=2)return false;
    if(CharacterRenderer.walkFrameIndex(.360f)!=3||CharacterRenderer.walkFrameIndex(.479f)!=3)return false;
    if(CharacterRenderer.walkFrameIndex(.480f)!=0)return false;

    CharacterRenderer.DirectionalVisualSet unresolved=new CharacterRenderer.DirectionalVisualSet(null,null,null,null);
    if(!unresolved.unresolved())return false;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())if(unresolved.forDirection(d)!=null)return false;

    for(Case c:matrix()){
      if(c.direction==null||c.state==null||c.effectFamily==null)return false;
      if(c.state==CharacterRenderer.State.CAST&&c.effectFamily!=CharacterRenderer.EffectFamily.CAST)return false;
      if(c.state==CharacterRenderer.State.HIT&&c.effectFamily!=CharacterRenderer.EffectFamily.HIT)return false;
    }
    return true;
  }

  public static String summary(){
    return "profile="+CharacterRenderer.PRESENTATION_PROFILE+
        ",starter="+CharacterRenderer.STARTER_ARCHETYPE+
        ",frame="+CharacterRenderer.ATLAS_FRAME_WIDTH+"x"+CharacterRenderer.ATLAS_FRAME_HEIGHT+
        ",idleWalk="+CharacterRenderer.IDLE_WALK_WIDTH+"x"+CharacterRenderer.ATLAS_FRAME_HEIGHT*CharacterRenderer.ATLAS_ROWS+
        ",sourceAtlas="+CharacterRenderer.SOURCE_IDLE_WALK_WIDTH+"x"+CharacterRenderer.SOURCE_ATLAS_HEIGHT+
        ",rows=NW,NE,SW,SE,scale="+CharacterRenderer.PLAYER_RENDER_SCALE+
        ",sourcePresentationScale="+CharacterRenderer.SOURCE_PRESENTATION_SCALE+
        ",attackTrailGhosts="+CharacterRenderer.ATTACK_TRAIL_GHOSTS+
        ",temporalAction=UNRESOLVED"+
        ",runtimeFacingCompositions=4"+
        ",attackPaperDoll=current-equipment-atomic"+
        ",attackViewport="+CharacterRenderer.actionCanonicalVisibleHeightPixels()+"px@foot"+CharacterRenderer.actionCanonicalFootAnchorPixels()+
        ",attackFallback=shared-source-paperdoll"+
        ",cleanReset=ATTACK_SWING_ONLY"+
        ",walkFps="+CharacterRenderer.WALK_FRAMES_PER_SECOND+
        ",walkCycleSeconds="+CharacterRenderer.WALK_CYCLE_SECONDS+
        ",paperDollLayers="+CharacterRenderer.PAPER_DOLL_ORDER.size()+
        ",legacyMartialAllowed="+CharacterRenderer.LEGACY_MARTIAL_ASSET_ALLOWED+
        ",anchorY="+CharacterRenderer.LOGICAL_FOOT_ANCHOR_Y;
  }

  private static CharacterRenderer.EffectFamily defaultEffect(CharacterRenderer.State state){
    switch(state){
      case CAST:return CharacterRenderer.EffectFamily.CAST;
      case HIT:return CharacterRenderer.EffectFamily.HIT;
      default:return CharacterRenderer.EffectFamily.NONE;
    }
  }

  public static void main(String[] args){
    if(!passes())throw new AssertionError("CharacterRendererAudit failed: "+summary());
    System.out.println("CharacterRendererAudit PASS: "+summary());
  }
}
