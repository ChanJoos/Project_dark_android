package com.projectdark.mobile;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Crash-safe renderer for the source-derived old-Dark-Ages peasant base-body atlas. */
public final class CharacterRenderer {
  public static final String EVIDENCE="LOD_DRESSUP_HAR_MM001_MALE_BASE_BODY+ADAPTED";
  public static final String ASSET_STATUS="PRODUCTION_WEBP_IDLE_WALK_ACTIVE";
  public static final String PRESENTATION_PROFILE="PEASANT_MM001_BASE_20260916_R10_ATTACK_COMPOSITE";
  public static final String STARTER_ARCHETYPE="PEASANT";
  public static final String STARTER_CLASS_STATE="PRE_CLASS";
  public static final String IDLE_WALK_ASSET_ID="player.peasant.mm001.idle_walk.production-2026-09-12";
  public static final String IDLE_WALK_RESOURCE="player_peasant_idle_walk";
    public static final String STARTER_SHIRT_RESOURCE="player_shirt_mu0000001_source"; // exact mu0000001 wearable source atlas
  public static final String LUERS_ROBE_RESOURCE="player_armor_mu0000058_idle_walk";
  public static final String MOKDO_RESOURCE="player_weapon_mw001";
  public static final String SHOES_ML228_RESOURCE="player_shoes_ml228_source";
  public static final String HAT_MH172_RESOURCE="player_hat_mh172_source";
  public static final String SHIELD_MS001_RESOURCE="player_shield_ms001_source";
  public static final String ACTION_SOURCE_EVIDENCE="mm001 group 02 source pixels; ADAPTED PLAYTEST ACTION GROUP";
  public static final String ACTION_TEMPORAL_STATUS="SINGLE_POSE_PLACEHOLDER";
  public static final String ACTION_TEMPORAL_EVIDENCE="SINGLE_POSE_PLACEHOLDER remains UNRESOLVED; runtime uses one directional source action pose as contact while wind-up and recovery use the standing equipped paper doll without fabricated intermediate frames";
  public static final String WEAPON_SOURCE_EVIDENCE="mw001 Î™©ÎèÑ HAR/source pixels; source-derived BODY dominantHand attachment";
  public static final String ATTACK_PRESENTATION_EVIDENCE="SOURCE_DIRECTIONAL_ACTION_CONTACT: startup/recovery use equipped idle paper doll; CONTACT uses one normalized BODY+garment+mw001 composite transform with four-way authored registration";
  public static final String ATTACK_DIRECTION_EVIDENCE="runtime Pose.direction selects one locked 4-way attack direction; BODY+robe+weapon consume it unchanged";
  public static final String PAPER_DOLL_ATTACK_EVIDENCE="ATTACK consumes current equipmentVisualRef and weaponVisualRef; no baked equipment";
  public static final String ATTACK_FALLBACK_EVIDENCE="stable equipped source paper doll remains fallback when action resources/geometry are unavailable; weapon-only attack animation is unreachable";
  public static final String ATTACK_GEOMETRY_EVIDENCE="CONTACT uses 1.70 presentation scale and shares one final foot/center transform across BODY+garment+weapon; no destructive action crop";
  public static final String ROBE_ATTACK_EVIDENCE="equipped FULL_BODY mu0000058 is composited atomically with BODY during contact; UPPER mu0000001 uses its exact recovered source atlas and source registration";
  public static final String ROBE_WALK_EVIDENCE="NW/NE retain accepted registration exactly; SW/SE packaged alpha pixels remain untouched and use shared-atlas zero translation without runtime alpha re-centering";
  public static final String WEAPON_TRANSFORM_EVIDENCE="mw001 local handle (2,4) attaches directly to action BODY dominantHand through the same normalized CONTACT transform; four-way grip correction and fixed wrist-aligned angle only, no time-based independent swing";
  public static final String HIT_POSE_EVIDENCE="HIT/HURT/FLINCH character pose is disabled; source paper doll remains visually stable while damage feedback is external";

  public static final float SOURCE_BAKED_SCALE=1.50f;
  public static final float PLAYER_RENDER_SCALE=1.70f;
  public static final float SOURCE_PRESENTATION_SCALE=PLAYER_RENDER_SCALE/SOURCE_BAKED_SCALE;
  public static final float SHADOW_RENDER_SCALE=0.72f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;
  public static final float BASE_HEIGHT=32f;
  public static final float HEAD_TO_BODY_RATIO=0.28f;
  public static final float WALK_FRAME_SECONDS=.12f;
  public static final float WALK_CYCLE_SECONDS=WALK_FRAME_SECONDS*4f;
  public static final float WALK_FRAMES_PER_SECOND=1f/WALK_FRAME_SECONDS;
  public static final float ATTACK_ACTION_BEGIN=.18f;
  public static final float ATTACK_ACTION_END=.62f;
  public static final float ATTACK_RECOVERY_BEGIN=.62f;
  public static final boolean HARD_PIXEL_GRID=true;
  public static final boolean STARTER_WEAPONLESS=true;
  public static final boolean STARTER_OFFHAND_EMPTY=true;
  public static final boolean LEGACY_MARTIAL_ASSET_ALLOWED=false;
  public static final int MAX_ALPHA_HEIGHT_ERROR_PX=1;
  public static final int MAX_ALPHA_FOOT_ERROR_PX=1;
  public static final int MAX_ALPHA_CENTER_ERROR_PX=2;
  public static final float WEAPON_HANDLE_X=2f;
  public static final float WEAPON_HANDLE_Y=4f;

  public static final int ATLAS_FRAME_WIDTH=24,ATLAS_FRAME_HEIGHT=32,SOURCE_FRAME_WIDTH=36,SOURCE_FRAME_HEIGHT=48,SOURCE_FOOT_ANCHOR_X=18,SOURCE_FOOT_ANCHOR_Y=46,IDLE_WALK_COLUMNS=5,ATTACK_COLUMNS=4,ATLAS_ROWS=4;
  public static final int IDLE_WALK_WIDTH=ATLAS_FRAME_WIDTH*IDLE_WALK_COLUMNS,ACTION_WIDTH=ATLAS_FRAME_WIDTH*ATTACK_COLUMNS,ATLAS_HEIGHT=ATLAS_FRAME_HEIGHT*ATLAS_ROWS,SOURCE_IDLE_WALK_WIDTH=SOURCE_FRAME_WIDTH*IDLE_WALK_COLUMNS,SOURCE_ATLAS_HEIGHT=SOURCE_FRAME_HEIGHT*ATLAS_ROWS,SOURCE_ACTION_COUNT=4,ATTACK_TRAIL_GHOSTS=0;
  private static final int[] BODY_ACTION_WIDTH={17,18,19,21},BODY_ACTION_HEIGHT={51,52,51,49},BODY_ACTION_OFFSET_X={8,2,6,4},BODY_ACTION_OFFSET_Y={8,8,9,3};
  private static final float[] BODY_ACTION_PIVOT_X={8.5f,9f,9.5f,10.5f},BODY_ACTION_FOOT_Y={50f,51f,50f,48f};
  private static final int[] ROBE_ACTION_WIDTH={14,19,15,20},ROBE_ACTION_HEIGHT={33,22,30,21},ROBE_ACTION_OFFSET_X={8,3,6,6},ROBE_ACTION_OFFSET_Y={20,27,23,22};
  private static final int[] SHIRT_ACTION_WIDTH={9,13,10,13},SHIRT_ACTION_HEIGHT={18,16,16,16};
  private static final float[][] CARRY_FRAME_X={{-6,-10,10,-6,-1},{6,10,-10,6,1},{-7,-10,10,-7,9},{7,8,8,7,7}},CARRY_FRAME_Y={{21,21,23,18,26},{21,21,23,18,26},{19,19,24,17,21},{19,20,21,20,19}},CARRY_FRAME_ANGLE={{56,62,48,59,52},{-64,-58,-72,-61,-68},{72,78,62,75,68},{-62,-64,-66,-64,-62}},ROBE_FRAME_X={{-1,-2,-1,0,1},{2,2,2,0,1},{0,0,0,0,0},{0,0,0,0,0}},ROBE_FRAME_Y={{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};
  private static volatile float presentationWalkClock;

  public enum Direction { NW, NE, SW, SE }
  public enum State { IDLE, WALK, CAST, ATTACK, SKILL, HIT, DEAD }
  public enum Layer { BODY, HAIR, EQUIPMENT, WEAPON, EFFECT }
  public enum EffectFamily { NONE, CAST, MAGIC, THROW, PUNCH, KICK, SKILL, HIT }
  public static final List<Layer> DRAW_ORDER=Collections.unmodifiableList(Arrays.asList(Layer.BODY,Layer.HAIR,Layer.EQUIPMENT,Layer.WEAPON,Layer.EFFECT));
  public static final List<String> PAPER_DOLL_ORDER=Collections.unmodifiableList(Arrays.asList("BODY_BASE","HAIR_STYLE","HAIR_COLOR","HEAD","FULL_BODY","UPPER","LOWER","HANDS","FEET","WEAPON","SHIELD","OFFHAND"));

  public static final class DirectionalVisualSet { public final String nw,ne,sw,se; public DirectionalVisualSet(String nw,String ne,String sw,String se){this.nw=nw;this.ne=ne;this.sw=sw;this.se=se;} public String forDirection(Direction d){if(d==null)return null;switch(d){case NW:return nw;case NE:return ne;case SW:return sw;case SE:return se;default:return null;}} public boolean unresolved(){return nw==null&&ne==null&&sw==null&&se==null;} }
  public static final class Pose { public final float x,y,walkClock,stateClock,stateDuration; public final Direction direction; public final State state; public final boolean hitFlash; public final String equipmentVisualRef,weaponVisualRef,effectVisualRef; public final EffectFamily effectFamily; public final AnimationAction animationAction; public Pose(float x,float y,Direction direction,State state,float walkClock,float stateClock,float stateDuration,boolean hitFlash,String equipmentVisualRef,String weaponVisualRef,String effectVisualRef,EffectFamily effectFamily){this(x,y,direction,state,walkClock,stateClock,stateDuration,hitFlash,equipmentVisualRef,weaponVisualRef,effectVisualRef,effectFamily,inferAnimationAction(state,effectFamily,weaponVisualRef));} public Pose(float x,float y,Direction direction,State state,float walkClock,float stateClock,float stateDuration,boolean hitFlash,String equipmentVisualRef,String weaponVisualRef,String effectVisualRef,EffectFamily effectFamily,AnimationAction animationAction){this.x=x;this.y=y;this.direction=direction;this.state=state;this.walkClock=walkClock;this.stateClock=stateClock;this.stateDuration=stateDuration;this.hitFlash=hitFlash;this.equipmentVisualRef=equipmentVisualRef;this.weaponVisualRef=weaponVisualRef;this.effectVisualRef=effectVisualRef;this.effectFamily=effectFamily==null?EffectFamily.NONE:effectFamily;this.animationAction=animationAction;} }
  public static final class AttackVisualComposition { public final Direction direction;public final int sourceIndex;public final boolean mirrorBody,weaponBehindBody,robeVisible,weaponVisible;public final float presentationScale; AttackVisualComposition(Direction direction,int sourceIndex,boolean mirrorBody,boolean weaponBehindBody,boolean robeVisible,boolean weaponVisible,float presentationScale){this.direction=direction;this.sourceIndex=sourceIndex;this.mirrorBody=mirrorBody;this.weaponBehindBody=weaponBehindBody;this.robeVisible=robeVisible;this.weaponVisible=weaponVisible;this.presentationScale=presentationScale;} public String signature(){return direction+":"+sourceIndex+":"+mirrorBody+":"+weaponBehindBody+":"+robeVisible+":"+weaponVisible+":"+weaponAttackOffsetX(direction)+":"+weaponAttackOffsetY(direction)+":"+weaponAttackAngle(direction,.55f);} }
  private static final class AlphaBounds {final int left,top,right,bottom;AlphaBounds(int left,int top,int right,int bottom){this.left=left;this.top=top;this.right=right;this.bottom=bottom;}boolean empty(){return right<left||bottom<top;}int width(){return empty()?0:right-left+1;}int height(){return empty()?0:bottom-top+1;}float centerX(){return empty()?0f:(left+right)*.5f;}}
  private static final class Registration {final float x,y;Registration(float x,float y){this.x=x;this.y=y;}}
  private static final class ActionGeometry {final int offsetX,offsetY,cropTop,cropBottomExclusive,heightError,footError,centerError;final boolean compatible;ActionGeometry(int x,int y,int cropTop,int cropBottomExclusive,int h,int f,int c,boolean ok){offsetX=x;offsetY=y;this.cropTop=cropTop;this.cropBottomExclusive=cropBottomExclusive;heightError=h;footError=f;centerError=c;compatible=ok;}}
  private final Paint pixelPaint=new Paint(),fxPaint=new Paint();
  private final Bitmap idleWalkAtlas,starterShirtSource,luersRobeAtlas,mokdoSprite,shoesMl228,hatMh172,shieldMs001; private final SourceEquipmentRegistration shoesRegistration,hatRegistration,shieldRegistration; private final EquipmentVisualRegistry equipmentRegistry; private final Bitmap[] bodyActionFrames=new Bitmap[SOURCE_ACTION_COUNT],robeActionFrames=new Bitmap[SOURCE_ACTION_COUNT],shirtActionFrames=new Bitmap[SOURCE_ACTION_COUNT];

  public CharacterRenderer(){this(findProcessContext());}
  public CharacterRenderer(Context context){pixelPaint.setAntiAlias(false);pixelPaint.setDither(false);pixelPaint.setFilterBitmap(false);fxPaint.setAntiAlias(false);fxPaint.setDither(false);fxPaint.setFilterBitmap(false);Resources resources=context==null?null:context.getResources();idleWalkAtlas=tryLoadByName(resources,IDLE_WALK_RESOURCE,SOURCE_IDLE_WALK_WIDTH,SOURCE_ATLAS_HEIGHT);starterShirtSource=tryLoadByName(resources,STARTER_SHIRT_RESOURCE,298,19);luersRobeAtlas=tryLoadByName(resources,LUERS_ROBE_RESOURCE,SOURCE_IDLE_WALK_WIDTH,SOURCE_ATLAS_HEIGHT);mokdoSprite=tryLoadByName(resources,MOKDO_RESOURCE,16,8);shoesMl228=tryLoadByNameAny(resources,SHOES_ML228_RESOURCE);hatMh172=tryLoadByNameAny(resources,HAT_MH172_RESOURCE);shieldMs001=tryLoadByNameAny(resources,SHIELD_MS001_RESOURCE);shoesRegistration=SourceEquipmentRegistration.load(context,"ml228");hatRegistration=SourceEquipmentRegistration.load(context,"mh172");shieldRegistration=SourceEquipmentRegistration.load(context,"ms001");equipmentRegistry=new EquipmentVisualRegistry(context);for(int i=0;i<SOURCE_ACTION_COUNT;i++){bodyActionFrames[i]=tryLoadByName(resources,"player_body_mm001_action02_"+i,BODY_ACTION_WIDTH[i],BODY_ACTION_HEIGHT[i]);robeActionFrames[i]=tryLoadByName(resources,"player_robe_mu0000058_action02_"+i,ROBE_ACTION_WIDTH[i],ROBE_ACTION_HEIGHT[i]);shirtActionFrames[i]=ShirtSourceRegistration.actionFrame(starterShirtSource,i);}}
  CharacterRenderer(Bitmap idle,Bitmap shirt,Bitmap robe,Bitmap weapon,Bitmap[] bodies,Bitmap[] robes){
    idleWalkAtlas=idle;starterShirtSource=shirt;luersRobeAtlas=robe;mokdoSprite=weapon;shoesMl228=null;hatMh172=null;shieldMs001=null;shoesRegistration=null;hatRegistration=null;shieldRegistration=null;equipmentRegistry=null;
    pixelPaint.setFilterBitmap(false);
    for(int i=0;i<SOURCE_ACTION_COUNT;i++){bodyActionFrames[i]=bodies[i];robeActionFrames[i]=robes[i];shirtActionFrames[i]=ShirtSourceRegistration.actionFrame(shirt,i);}
  }
  public static int atlasRow(Direction direction){if(direction==null)return -1;switch(direction){case NW:return 0;case NE:return 1;case SW:return 2;case SE:return 3;default:return -1;}}
  public static Direction visualFacingForRow(int row){switch(row){case 0:return Direction.NW;case 1:return Direction.NE;case 2:return Direction.SW;case 3:return Direction.SE;default:return null;}}
  public static void setPresentationWalkClock(float clock){presentationWalkClock=Math.max(0f,clock);} public static float presentationWalkClock(){return presentationWalkClock;}
  public boolean resourceAtlasActive(){return validAtlas(idleWalkAtlas,SOURCE_IDLE_WALK_WIDTH,SOURCE_ATLAS_HEIGHT);} public boolean attackAtlasActive(){return sourceActionActive();} public boolean equipmentAtlasActive(){return starterShirtSource!=null||validAtlas(luersRobeAtlas,SOURCE_IDLE_WALK_WIDTH,SOURCE_ATLAS_HEIGHT);} public boolean weaponSourceActive(){return validAtlas(mokdoSprite,16,8);}
  public boolean sourceActionActive(){for(int i=0;i<SOURCE_ACTION_COUNT;i++)if(!validAtlas(bodyActionFrames[i],BODY_ACTION_WIDTH[i],BODY_ACTION_HEIGHT[i]))return false;return true;}
  public static boolean usesSourceActionPose(State state,AnimationAction action){return state==State.ATTACK&&action==AnimationAction.SWING;} public static boolean attackUsesActionPose(float phase){float q=Math.max(0f,Math.min(1f,phase));return q>=ATTACK_ACTION_BEGIN&&q<ATTACK_ACTION_END;}
  public static int attackThreePose(float phase){float q=Math.max(0f,Math.min(1f,phase));return q<ATTACK_ACTION_BEGIN?0:q<ATTACK_RECOVERY_BEGIN?1:2;} public static boolean attackUsesDestructiveCrop(){return false;} public static boolean attackTemporalSourceResolved(){return false;} public static boolean attackUsesSinglePosePlaceholder(){return ACTION_TEMPORAL_STATUS.equals("SINGLE_POSE_PLACEHOLDER");}
  public static int paperDollAtlasColumn(State state,float walkClock){return state==State.WALK?1+walkFrameIndex(walkClock):0;}
  public static boolean sourceActionLayersReadyContract(String equipmentVisualRef,String weaponVisualRef,boolean bodyReady,boolean robeReady,boolean weaponReady){if(!bodyReady)return false;if(containsVisualRef(equipmentVisualRef,CharacterVisualBinding.RESOLVED_LUERS_ROBE_APPEARANCE_ID)&&!robeReady)return false;if(containsVisualRef(weaponVisualRef,CharacterVisualBinding.RESOLVED_WEAPON_APPEARANCE_ID)&&!weaponReady)return false;return true;}
  public static boolean alphaGeometryWithinTolerance(int idleHeight,int actionHeight,int footError,int centerError){return idleHeight>0&&actionHeight>0&&Math.abs(idleHeight-actionHeight)<=MAX_ALPHA_HEIGHT_ERROR_PX&&Math.abs(footError)<=MAX_ALPHA_FOOT_ERROR_PX&&Math.abs(centerError)<=MAX_ALPHA_CENTER_ERROR_PX;}
  private boolean sourceActionReadyFor(Pose pose){if(pose==null)return false;int source=actionSourceIndex(pose.direction);boolean bodyReady=source>=0&&source<SOURCE_ACTION_COUNT&&validAtlas(bodyActionFrames[source],BODY_ACTION_WIDTH[source],BODY_ACTION_HEIGHT[source]);if(!bodyReady)return false;return actionGeometry(pose.direction,source)!=null;}
  private boolean sourcePaperDollReadyFor(Pose pose){return pose!=null&&resourceAtlasActive();}

  public void draw(Canvas canvas,Pose pose){if(canvas==null||pose==null||pose.direction==null||pose.state==null)return;float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;drawShadow(canvas,pose,anchorY);if((pose.state==State.IDLE||pose.state==State.WALK||pose.state==State.HIT)&&sourcePaperDollReadyFor(pose)){drawSourcePaperDoll(canvas,pose,anchorY,false);return;}if(usesSourceActionPose(pose.state,pose.animationAction)&&sourceActionReadyFor(pose)){if(attackUsesActionPose(attackPhase(pose)))drawSourceAction(canvas,pose,anchorY);else if(sourcePaperDollReadyFor(pose))drawSourcePaperDoll(canvas,pose,anchorY,false);return;}if(pose.state==State.ATTACK){if(sourcePaperDollReadyFor(pose))drawSourcePaperDoll(canvas,pose,anchorY,false);return;}if(sourcePaperDollReadyFor(pose)){drawSourcePaperDoll(canvas,pose,anchorY,false);return;}drawSafePeasantFallback(canvas,pose,anchorY);}
  private void drawSourcePaperDoll(Canvas c,Pose pose,float anchorY,boolean attackingWeapon){boolean weaponBehind=weaponBehindBody(pose.direction),shieldBehind=shieldBehindBody(pose.direction);if(shieldBehind)drawShield(c,pose,anchorY);if(weaponBehind)drawWeapon(c,pose,anchorY,attackingWeapon);drawIdleWalk(c,pose,anchorY);drawEquipment(c,pose,anchorY);drawAccessoryEquipment(c,pose,anchorY);if(!weaponBehind)drawWeapon(c,pose,anchorY,attackingWeapon);if(!shieldBehind)drawShield(c,pose,anchorY);}
  private void drawAccessoryEquipment(Canvas c,Pose pose,float anchorY){
    int row=atlasRow(pose.direction),col=paperDollAtlasColumn(pose.state,presentationWalkClock);if(row<0)return;
    AlphaBounds bounds=alphaBoundsCell(idleWalkAtlas,row,col,SOURCE_FRAME_WIDTH,SOURCE_FRAME_HEIGHT);if(bounds.empty())return;
    float left=Math.round(pose.x-SOURCE_FOOT_ANCHOR_X*SOURCE_PRESENTATION_SCALE)+bounds.left*SOURCE_PRESENTATION_SCALE;
    float top=Math.round(anchorY-SOURCE_FOOT_ANCHOR_Y*SOURCE_PRESENTATION_SCALE)+bounds.top*SOURCE_PRESENTATION_SCALE;
    int bodyFrame=ShirtSourceRegistration.idleFrame(pose.direction,col);float scaleX=bounds.width()*SOURCE_PRESENTATION_SCALE/ShirtSourceRegistration.BODY_WIDTH[bodyFrame],scaleY=bounds.height()*SOURCE_PRESENTATION_SCALE/ShirtSourceRegistration.BODY_HEIGHT[bodyFrame];boolean mirror=pose.direction==Direction.NW||pose.direction==Direction.SW;
    for(String appearance:visualTokens(pose.equipmentVisualRef)){String a=appearance.toLowerCase();if(!(a.startsWith("mh")||a.startsWith("wh")||a.startsWith("ml")||a.startsWith("wl")))continue;EquipmentVisualRegistry.Visual v=equipmentRegistry==null?null:equipmentRegistry.get(a);if(v!=null){if(v.registration!=null)drawRegisteredIdle(c,v.atlas,v.registration,pose.direction,col,left,top,scaleX,scaleY,mirror,bounds);else drawTightMasterGear(c,v.atlas,pose.x,anchorY,a.startsWith("mh")||a.startsWith("wh"));}}
  }
  private static String[] visualTokens(String ref){return ref==null?new String[0]:ref.split(",");}
  private void drawTightMasterGear(Canvas c,Bitmap b,float footX,float footY,boolean head){
    if(b==null)return;float targetH=head?30f:56f,sc=targetH/Math.max(1f,b.getHeight()),w=b.getWidth()*sc,h=b.getHeight()*sc;
    float top=head?footY-72f:footY-h;RectF dst=new RectF(Math.round(footX-w*.5f),Math.round(top),Math.round(footX+w*.5f),Math.round(top+h));c.drawBitmap(b,null,dst,pixelPaint);
  }
  private void drawRegisteredIdle(Canvas c,Bitmap atlas,SourceEquipmentRegistration registration,Direction direction,int col,float left,float top,float scaleX,float scaleY,boolean mirror,AlphaBounds bounds){
    if(atlas==null||registration==null)return;SourceEquipmentRegistration.Frame f=registration.idle(direction,col);if(f==null)return;
    float x=left+f.dx*scaleX,y=top+f.dy*scaleY;RectF dst=new RectF(Math.round(x),Math.round(y),Math.round(x+f.src.width()*scaleX),Math.round(y+f.src.height()*scaleY));
    c.save();if(mirror)c.scale(-1f,1f,left+bounds.width()*SOURCE_PRESENTATION_SCALE*.5f,top+bounds.height()*SOURCE_PRESENTATION_SCALE);c.drawBitmap(atlas,f.src,dst,pixelPaint);c.restore();
  }
  private static boolean shieldBehindBody(Direction d){return d==Direction.NW||d==Direction.NE;}
  private void drawShield(Canvas c,Pose pose,float anchorY){
    String appearance=null;for(String x:visualTokens(pose.equipmentVisualRef)){String a=x.toLowerCase();if(a.startsWith("ms")||a.startsWith("ws"))appearance=a;}if(appearance==null)return;EquipmentVisualRegistry.Visual v=equipmentRegistry==null?null:equipmentRegistry.get(appearance);if(v==null)return;
    int row=atlasRow(pose.direction),col=paperDollAtlasColumn(pose.state,presentationWalkClock);if(row<0)return;AlphaBounds bounds=alphaBoundsCell(idleWalkAtlas,row,col,SOURCE_FRAME_WIDTH,SOURCE_FRAME_HEIGHT);if(bounds.empty())return;float left=Math.round(pose.x-SOURCE_FOOT_ANCHOR_X*SOURCE_PRESENTATION_SCALE)+bounds.left*SOURCE_PRESENTATION_SCALE,top=Math.round(anchorY-SOURCE_FOOT_ANCHOR_Y*SOURCE_PRESENTATION_SCALE)+bounds.top*SOURCE_PRESENTATION_SCALE;int bodyFrame=ShirtSourceRegistration.idleFrame(pose.direction,col);float scaleX=bounds.width()*SOURCE_PRESENTATION_SCALE/ShirtSourceRegistration.BODY_WIDTH[bodyFrame],scaleY=bounds.height()*SOURCE_PRESENTATION_SCALE/ShirtSourceRegistration.BODY_HEIGHT[bodyFrame];drawRegisteredIdle(c,v.atlas,v.registration,pose.direction,col,left,top,scaleX,scaleY,pose.direction==Direction.NW||pose.direction==Direction.SW,bounds);
  }
  private void drawShadow(Canvas c,Pose pose,float anchorY){float width=(pose.state==State.DEAD?13f:10.5f)*SHADOW_RENDER_SCALE,height=(pose.state==State.DEAD?2f:2.8f)*SHADOW_RENDER_SCALE;fxPaint.setStyle(Paint.Style.FILL);fxPaint.setColor(0x50000000);c.drawOval(new RectF(pose.x-width,anchorY-height,pose.x+width,anchorY+height),fxPaint);}
  private void drawIdleWalk(Canvas c,Pose pose,float anchorY){int row=atlasRow(pose.direction);if(row<0){drawSafePeasantFallback(c,pose,anchorY);return;}int col=paperDollAtlasColumn(pose.state,presentationWalkClock);drawAtlasCell(c,idleWalkAtlas,row,col,SOURCE_FRAME_WIDTH,SOURCE_FRAME_HEIGHT,SOURCE_FOOT_ANCHOR_X,SOURCE_FOOT_ANCHOR_Y,SOURCE_PRESENTATION_SCALE,anchorY,pose.x);}
  private void drawEquipment(Canvas c,Pose pose,float anchorY){
    int row=atlasRow(pose.direction);if(row<0)return;
    int col=paperDollAtlasColumn(pose.state,presentationWalkClock);
    if(containsVisualRef(pose.equipmentVisualRef,CharacterVisualBinding.RESOLVED_LUERS_ROBE_APPEARANCE_ID)&&luersRobeAtlas!=null){
      drawAtlasCell(c,luersRobeAtlas,row,col,SOURCE_FRAME_WIDTH,SOURCE_FRAME_HEIGHT,SOURCE_FOOT_ANCHOR_X,SOURCE_FOOT_ANCHOR_Y,SOURCE_PRESENTATION_SCALE,anchorY+ROBE_FRAME_Y[row][col],pose.x+ROBE_FRAME_X[row][col]);
    }else{
      String garment=garmentAppearance(pose.equipmentVisualRef);
      EquipmentVisualRegistry.Visual gv=garment==null||equipmentRegistry==null?null:equipmentRegistry.get(garment);
      if(gv!=null&&gv.registration!=null){
        AlphaBounds bounds=alphaBoundsCell(idleWalkAtlas,row,col,SOURCE_FRAME_WIDTH,SOURCE_FRAME_HEIGHT);
        if(!bounds.empty()){
          float left=Math.round(pose.x-SOURCE_FOOT_ANCHOR_X*SOURCE_PRESENTATION_SCALE)+bounds.left*SOURCE_PRESENTATION_SCALE;
          float top=Math.round(anchorY-SOURCE_FOOT_ANCHOR_Y*SOURCE_PRESENTATION_SCALE)+bounds.top*SOURCE_PRESENTATION_SCALE;
          int frame=ShirtSourceRegistration.idleFrame(pose.direction,col);
          float sx=bounds.width()*SOURCE_PRESENTATION_SCALE/ShirtSourceRegistration.BODY_WIDTH[frame];
          float sy=bounds.height()*SOURCE_PRESENTATION_SCALE/ShirtSourceRegistration.BODY_HEIGHT[frame];
          drawRegisteredIdle(c,gv.atlas,gv.registration,pose.direction,col,left,top,sx,sy,pose.direction==Direction.NW||pose.direction==Direction.SW,bounds);
        }
      }else if(gv!=null&&gv.atlas!=null){
        drawTightMasterGear(c,gv.atlas,pose.x,anchorY,false);
      }else if(containsVisualRef(pose.equipmentVisualRef,CharacterVisualBinding.RESOLVED_SHIRT_APPEARANCE_ID)&&starterShirtSource!=null){
        int frame=ShirtSourceRegistration.idleFrame(pose.direction,col);
        AlphaBounds bounds=alphaBoundsCell(idleWalkAtlas,row,col,SOURCE_FRAME_WIDTH,SOURCE_FRAME_HEIGHT);
        float left=Math.round(pose.x-SOURCE_FOOT_ANCHOR_X*SOURCE_PRESENTATION_SCALE);
        float top=Math.round(anchorY-SOURCE_FOOT_ANCHOR_Y*SOURCE_PRESENTATION_SCALE);
        float scaleX=bounds.width()*SOURCE_PRESENTATION_SCALE/ShirtSourceRegistration.BODY_WIDTH[frame];
        float scaleY=bounds.height()*SOURCE_PRESENTATION_SCALE/ShirtSourceRegistration.BODY_HEIGHT[frame];
        left+=bounds.left*SOURCE_PRESENTATION_SCALE;top+=bounds.top*SOURCE_PRESENTATION_SCALE;
        c.save();
        if(pose.direction==Direction.NW||pose.direction==Direction.SW)c.scale(-1,1,left+bounds.width()*SOURCE_PRESENTATION_SCALE*.5f,anchorY);
        Rect src=ShirtSourceRegistration.idleRect(frame);
        float x=left+ShirtSourceRegistration.idleX(frame)*scaleX,y=top+ShirtSourceRegistration.idleY(frame)*scaleY;
        c.drawBitmap(starterShirtSource,src,new RectF(Math.round(x),Math.round(y),Math.round(x+src.width()*scaleX),Math.round(y+src.height()*scaleY)),pixelPaint);
        c.restore();
      }
    }
  }
  /** Shared garment attachment primitive. Coverage (UPPER/FULL_BODY) controls equip conflicts, not placement math. */
  private void drawGarmentAttached(Canvas c,Bitmap bitmap,Rect src,float bodyAnchorX,float bodyAnchorY,float garmentAnchorX,float garmentAnchorY,float scale,boolean mirror){if(c==null||bitmap==null||src==null)return;float left=Math.round(bodyAnchorX-garmentAnchorX*scale),top=Math.round(bodyAnchorY-garmentAnchorY*scale);RectF dst=new RectF(left,top,Math.round(left+src.width()*scale),Math.round(top+src.height()*scale));c.save();if(mirror)c.scale(-1f,1f,bodyAnchorX,bodyAnchorY);c.drawBitmap(bitmap,src,dst,pixelPaint);c.restore();}
  private void drawGarmentAttached(Canvas c,Bitmap bitmap,float bodyAnchorX,float bodyAnchorY,float garmentAnchorX,float garmentAnchorY,float scale){if(bitmap==null)return;drawGarmentAttached(c,bitmap,new Rect(0,0,bitmap.getWidth(),bitmap.getHeight()),bodyAnchorX,bodyAnchorY,garmentAnchorX,garmentAnchorY,scale,false);}
  private Registration robeRegistration(Direction direction,int atlasColumn){int row=atlasRow(direction),col=Math.max(0,Math.min(IDLE_WALK_COLUMNS-1,atlasColumn));if(row<0)return new Registration(0f,0f);return new Registration(ROBE_FRAME_X[row][col],ROBE_FRAME_Y[row][col]);}
  public static float robeFrameRegistrationX(Direction direction,int atlasColumn){int row=atlasRow(direction),column=Math.max(0,Math.min(IDLE_WALK_COLUMNS-1,atlasColumn));return row<0?0f:ROBE_FRAME_X[row][column];} public static float robeFrameRegistrationY(Direction direction,int atlasColumn){int row=atlasRow(direction),column=Math.max(0,Math.min(IDLE_WALK_COLUMNS-1,atlasColumn));return row<0?0f:ROBE_FRAME_Y[row][column];} public static boolean robeUsesRuntimeXYRegistration(Direction direction){return direction==Direction.SW||direction==Direction.SE;}
  public static boolean robeRegistrationContinuityWithin(float maxDelta){if(maxDelta<0f)return false;for(Direction d:Direction.values())for(int col=1;col<IDLE_WALK_COLUMNS;col++){if(Math.abs(robeFrameRegistrationX(d,col)-robeFrameRegistrationX(d,col-1))>maxDelta)return false;if(Math.abs(robeFrameRegistrationY(d,col)-robeFrameRegistrationY(d,col-1))>maxDelta)return false;}return true;}
  private static boolean containsVisualRef(String refs,String expected){if(refs==null||expected==null)return false;for(String ref:refs.split(","))if(expected.equals(ref.trim()))return true;return false;} private static String garmentAppearance(String refs){if(refs==null)return null;for(String ref:refs.split(",")){String a=ref.trim().toLowerCase();if(a.startsWith("mu")||a.startsWith("wu"))return a;}return null;} private static boolean hasWeaponAppearance(String ref){if(ref==null)return false;String a=ref.trim().toLowerCase();return a.startsWith("mw")||a.startsWith("ww");}

  private void drawSourceAction(Canvas c,Pose pose,float anchorY){
    AttackVisualComposition composition=attackVisualComposition(pose.direction,pose.equipmentVisualRef,pose.weaponVisualRef);int source=composition.sourceIndex,row=atlasRow(pose.direction);if(source<0||row<0)return;
    AlphaBounds idle=alphaBoundsCell(idleWalkAtlas,row,0,SOURCE_FRAME_WIDTH,SOURCE_FRAME_HEIGHT),action=alphaBounds(bodyActionFrames[source]);
    if(idle.empty()||action.empty()){drawSourcePaperDoll(c,pose,anchorY,false);return;}
    AttackCompositeTransform.Result transform=AttackCompositeTransform.normalize(pose.x,anchorY,SOURCE_PRESENTATION_SCALE,SOURCE_FOOT_ANCHOR_Y,SOURCE_FOOT_ANCHOR_X,idle.top,idle.bottom,idle.centerX(),action.top,action.bottom,action.centerX());
    CharacterSemanticRig.Anchors body=CharacterSemanticRig.derive(bodyActionFrames[source],new Rect(0,0,BODY_ACTION_WIDTH[source],BODY_ACTION_HEIGHT[source]),pose.direction);
    if(shieldBehindBody(pose.direction))drawActionShield(c,pose,transform,body);if(composition.weaponVisible&&composition.weaponBehindBody)drawActionWeapon(c,pose,source,transform,body);
    c.save();if(composition.mirrorBody)c.scale(-1f,1f,transform.mirrorPivotX,anchorY);
    drawRawSourceScaled(c,bodyActionFrames[source],transform.bodyLeft,transform.bodyTop,transform.scale);
    String garment=garmentAppearance(pose.equipmentVisualRef);EquipmentVisualRegistry.Visual gv=garment==null||equipmentRegistry==null?null:equipmentRegistry.get(garment);
    if(gv!=null&&gv.registration!=null&&gv.registration.action(source)!=null){drawRegisteredAction(c,gv.atlas,gv.registration,source,transform,composition.mirrorBody);}else if(containsVisualRef(pose.equipmentVisualRef,CharacterVisualBinding.RESOLVED_SHIRT_APPEARANCE_ID)&&validAtlas(shirtActionFrames[source],SHIRT_ACTION_WIDTH[source],SHIRT_ACTION_HEIGHT[source])){drawRawSourceScaled(c,shirtActionFrames[source],transform.bodyLeft+ShirtSourceRegistration.actionX(source)*transform.scale,transform.bodyTop+ShirtSourceRegistration.actionY(source)*transform.scale,transform.scale);}else if(composition.robeVisible&&validAtlas(robeActionFrames[source],ROBE_ACTION_WIDTH[source],ROBE_ACTION_HEIGHT[source])){float robeLeft=transform.layerLeft(ROBE_ACTION_OFFSET_X[source],BODY_ACTION_OFFSET_X[source],0f),robeTop=transform.layerTop(ROBE_ACTION_OFFSET_Y[source],BODY_ACTION_OFFSET_Y[source],0f);float robeAnchorX=(pose.x-robeLeft)/transform.scale,robeAnchorY=(anchorY-robeTop)/transform.scale;drawGarmentAttached(c,robeActionFrames[source],pose.x,anchorY,robeAnchorX,robeAnchorY,transform.scale);}
    c.restore();drawActionAccessories(c,pose,source,transform,body);if(composition.weaponVisible&&!composition.weaponBehindBody)drawActionWeapon(c,pose,source,transform,body);if(!uﬂû≠¢Gß≤⁄Óù∆≠y–“‘ó÷KSX]úõ›[ô
Ÿ[X[ùX—€ÿò[õ€›JŸJJKŸ[ù\ë\úèSX]úõ›[ô
X]òXú ”’Tê—W—ì”’–Sê“‘ó÷JŸ[X[ùX—€ÿò[]õ›
Ÿ
JJK\úèSX]òXú YKöZY⁄

KXX›[€ãöZY⁄

JN‹ô]\õàô]»X›[€ëŸ[€Y]ûJKì—W–P’S”ó“RQ“‹€›\òŸWK\úãõ€›\úãŸ[ù\ë\úãùYJNﬂBàö]ò]H›]X»[Põ›[ô»[Põ›[ô ö]X\ö]X\
^⁄Yäö]X\O[ù[
\ô]\õàô]»[Põ›[ô LKLJN⁄[ùXö]X\ôŸ]⁄Y

KXö]X\ôŸ]ZY⁄

KèKLKèKLNŸõ‹ä[ùOLﬁOö]X\ôŸ]ZY⁄

NﬁJ  Yõ‹ä[ùLﬁö]X\ôŸ]⁄Y

Nﬁ
  ZYä

ö]X\ôŸ]^[
JOèèåç
IåôäHOL
^⁄Yä
[^⁄Yäúä\è^⁄YäO
]^N⁄YäOòäXè^Nﬂ\ô]\õàô]»[Põ›[ô ãäNﬂHö]ò]H›]X»[Põ›[ô»[Põ›[ô–Ÿ[
ö]X\]\À[ùõ›À[ù€€[ùÀ[ù
^⁄Yä]\œO[ù[
\ô]\õàô]»[Põ›[ô LKLJN⁄[ùﬁX€€
ùÀﬁO\õ› ö]ÀZèKLKèKLNŸõ‹ä[ùOLﬁOﬁJ  Yõ‹ä[ùLﬁŒﬁ
  ZYä

]\ÀôŸ]^[
ﬁ
ﬁﬁJﬁJOèèåç
IåôäHOL
^⁄Yä
[^⁄Yäúä\è^⁄YäO
]^N⁄YäOòäXè^Nﬂ\ô]\õàô]»[Põ›[ô ãäNﬂBàXõX»›]X»õÿ]õ‹õX[^ôYX›[€îÿÿ[J[ù€›\òŸJ^‹ô]\õà”’Tê—W‘ëT—SïUS”ó‘––SNﬂHXõX»›]X»[ùX›[€êÿ[õ€öXÿ[ö\⁄XõRZY⁄^[ 
^‹ô]\õà”’Tê—W—îêSQW“RQ“ﬂHXõX»›]X»[ùX›[€êÿ[õ€öXÿ[õ€›[ò⁄‹î^[ 
^‹ô]\õà”’Tê—W—ì”’–Sê“‘ó÷NﬂHXõX»›]X»[ùX›[€ïö\⁄XõRZY⁄^[ [ù€›\òŸJ^⁄Yä€›\òŸO€›\òŸOèT”’Tê—W–P’S”ó–”’Sï
\ô]\õà‹ô]\õàì—W–P’S”ó“RQ“‹€›\òŸWNﬂHXõX»›]X»õÿ]X›[€îŸ[X[ùX‘]õ›
[ù€›\òŸJ^‹ô]\õà€›\òŸO€›\òŸOèT”’Tê—W–P’S”ó–”’Sï—õÿ]ìòSéêì—W–P’S”ó”—ëî—U÷‹€›\òŸWJ–ì—W–P’S”ó‘Uì’÷‹€›\òŸWNﬂHXõX»›]X»õÿ]X›[€îŸ[X[ùX—õ€›J[ù€›\òŸJ^‹ô]\õà€›\òŸO€›\òŸOèT”’Tê—W–P’S”ó–”’Sï—õÿ]ìòSéêì—W–P’S”ó”—ëî—U÷V‹€›\òŸWJ–ì—W–P’S”ó—ì”’÷V‹€›\òŸWNﬂHXõX»›]X»õ€€X[àõŸSZ\úõ‹ñ
\ôX›[€à
^‹ô]\õàOQ\ôX›[€ãìïﬂOQ\ôX›[€ãî’ŒﬂHXõX»›]X»õ€€X[àŸX\€ìZ\úõ‹ñ
\ôX›[€à
^‹ô]\õàOQ\ôX›[€ãìïﬂOQ\ôX›[€ãî’ŒﬂBàXõX»›]X»]X⁄’ö\›X[€€\‹⁄][€à]X⁄’ö\›X[€€\‹⁄][€ä\ôX›[€à\ôX›[€ã›ö[ô»\]Z\Y[ùö\›X[ôYã›ö[ô»ŸX\€ïö\›X[ôYä^‹ô]\õàô]»]X⁄’ö\›X[€€\‹⁄][€ä\ôX›[€ãX›[€î€›\òŸR[ô^
\ôX›[€äKõŸSZ\úõ‹ñ
\ôX›[€äKŸX\€êôZ[ôõŸJ\ôX›[€äKÿ\õY[ù\X\ò[òŸJ\]Z\Y[ùö\›X[ôYäHO[ù[\’ŸX\€ê\X\ò[òŸJŸX\€ïö\›X[ôYäKVQTó‘ëSëTó‘––SJNﬂHXõX»›]X»õ€€X[àŸX\€êôZ[ôõŸJ\ôX›[€à\ôX›[€ä^‹ô]\õà\ôX›[€èOQ\ôX›[€ãìïﬂ\ôX›[€èOQ\ôX›[€ãìëNﬂBàö]ò]H⁄[ô‹û[€ô’ŸX\€îô[ô\ô\à⁄[ô‹û[€ô’ŸX\€é¬àö]ò]Hõ⁄Yò]’ŸX\€äÿ[ùò\»À‹ŸH‹ŸKõÿ][ò⁄‹ñKõ€€X[à]X⁄⁄[ô ^¬àYä⁄[ô‹û[€ô’ŸX\€îô[ô\ô\ãô\]Z\Y
‹ŸKùŸX\€ïö\›X[ôYäJ^¬àYä⁄[ô‹û[€ô’ŸX\€èO[ù[
^–€€ù^€€ù^Yö[ôõÿŸ\‹–€€ù^

N⁄Yä€€ù^O[ù[
X⁄[ô‹û[€ô’ŸX\€è[ô]»⁄[ô‹û[€ô’ŸX\€îô[ô\ô\ä€€ù^
NﬂBàYä⁄[ô‹û[€ô’ŸX\€àO[ù[
^⁄[ùõ›œX]\‘õ› ‹ŸKô\ôX›[€äK€€[[è\\\ë€]\–€€[[ä‹ŸKú›]Kô\Ÿ[ù][€ïÿ[–€ÿ⁄ NŸõÿ]ÿÿ[OT”’Tê—W‘ëT—SïUS”ó‘––SNŸõÿ]SX]úõ›[ô
‹ŸKûT”’Tê—W—ì”’–Sê“‘ó÷
úÿÿ[JJ–⁄[ô‹û[€ô’ŸX\€îô[ô\ô\ãòÿ\úûR[ô
‹ŸKô\ôX›[€ã€€[[äJúÿÿ[KOSX]úõ›[ô
[ò⁄‹ñKT”’Tê—W—ì”’–Sê“‘ó÷Júÿÿ[JJ–⁄[ô‹û[€ô’ŸX\€îô[ô\ô\ãòÿ\úûR[ôJ‹ŸKô\ôX›[€ã€€[[äJúÿÿ[Nÿ⁄[ô‹û[€ô’ŸX\€ãòÿ\úûJÀ‹ŸKKÿÿ[JNﬂ\ô]\õé¬àBî›ö[ô»\X\ò[òŸO\‹ŸKùŸX\€ïö\›X[ôYèO[ù[€ù[ú‹ŸKùŸX\€ïö\›X[ôYãù”›Ÿ\êÿ\ŸJ
N—\]Z\Y[ùö\›X[ôY⁄\›ûKïö\›X[èY\]Z\Y[ùôY⁄\›ûOO[ù[€ù[ô\]Z\Y[ùôY⁄\›ûKôŸ]
\X\ò[òŸJN⁄YäàO[ù[	âùãúôY⁄\›ò][€àO[ù[
^⁄[ùõ›œX]\‘õ› ‹ŸKô\ôX›[€äK€€\\\ë€]\–€€[[ä‹ŸKú›]Kô\Ÿ[ù][€ïÿ[–€ÿ⁄ N⁄Yäõ›œ
\ô]\õé–[Põ›[ô»õ›[ôœX[Põ›[ô–Ÿ[
YUÿ[–]\Àõ›À€€”’Tê—W—îêSQW’“Q”’Tê—W—îêSQW“RQ“
N⁄Yäõ›[ôÀô[\J
J\ô]\õéŸõÿ]YùSX]úõ›[ô
‹ŸKûT”’Tê—W—ì”’–Sê“‘ó÷
î”’Tê—W‘ëT—SïUS”ó‘––SJJÿõ›[ôÀõYù
î”’Tê—W‘ëT—SïUS”ó‘––SK‹SX]úõ›[ô
[ò⁄‹ñKT”’Tê—W—ì”’–Sê“‘ó÷Jî”’Tê—W‘ëT—SïUS”ó‘––SJJÿõ›[ôÀù‹
î”’Tê—W‘ëT—SïUS”ó‘––SN⁄[ùõŸQúò[YOT⁄\ù€›\òŸTôY⁄\›ò][€ãöYQúò[YJ‹ŸKô\ôX›[€ã€€
NŸõÿ]ﬁXõ›[ôÀù⁄Y

Jî”’Tê—W‘ëT—SïUS”ó‘––SK‘⁄\ù€›\òŸTôY⁄\›ò][€ãêì—W’“QÿõŸQúò[YWKﬁOXõ›[ôÀöZY⁄

Jî”’Tê—W‘ëT—SïUS”ó‘––SK‘⁄\ù€›\òŸTôY⁄\›ò][€ãêì—W“RQ“ÿõŸQúò[YWNŸò]‘ôY⁄\›\ôYYJÀãò]\ÀãúôY⁄\›ò][€ã‹ŸKô\ôX›[€ã€€Yù‹ﬁﬁK‹ŸKô\ôX›[€èOQ\ôX›[€ãìïﬂ‹ŸKô\ôX›[€èOQ\ôX›[€ãî’Àõ›[ô N‹ô]\õéﬂZYäX€€ùZ[ú’ö\›X[ôYä‹ŸKùŸX\€ïö\›X[ôYã⁄\òX›\ïö\›X[ö[ô[ôÀîëT””ëQ’—PT”ó–TPTêSê—W“Q
_]ŸX\€î€›\òŸPX›]ôJ
J\ô]\õéŸõÿ]ÿÿ[OT”’Tê—W‘ëT—SïUS”ó‘––SN⁄[ùúò[YO\\\ë€]\–€€[[ä‹ŸKú›]Kô\Ÿ[ù][€ïÿ[–€ÿ⁄ Kõ›œX]\‘õ› ‹ŸKô\ôX›[€äN⁄Yäõ›œ
\ô]\õé⁄[ù€›\òŸP€€[[èX]X⁄⁄[ôœÃôúò[YN‘ôX›Ÿ[X]\–Ÿ[ôX›
õ›À€›\òŸP€€[[äNŸõÿ]õŸSYùSX]úõ›[ô
‹ŸKûT”’Tê—W—ì”’–Sê“‘ó÷
úÿÿ[JKõŸU‹SX]úõ›[ô
[ò⁄‹ñKT”’Tê—W—ì”’–Sê“‘ó÷Júÿÿ[JN–⁄\òX›\îŸ[X[ùX‘öYÀê[ò⁄‹ú»õŸOP⁄\òX›\îŸ[X[ùX‘öYÀô\ö]ôJYUÿ[–]\ÀŸ[‹ŸKô\ôX›[€äNŸõÿ][ôVXõŸSYù
ÿõŸKô€Z[ò[ù[ôû
úÿÿ[K[ôVOXõŸU‹
ÿõŸKô€Z[ò[ù[ôûJúÿÿ[K[ô€OX]X⁄⁄[ôœ›ŸX\€ê]X⁄–[ô€J‹ŸKô\ôX›[€ã]X⁄‘\ŸJ‹ŸJJNùŸX\€êÿ\úûP[ô€J‹ŸKô\ôX›[€ãúò[YJNÿÀúÿ]ôJ
NÿÀúõ›]J[ô€K[ôV[ôVJN⁄YäŸX\€ìZ\úõ‹ñ
‹ŸKô\ôX›[€äJXÀúÿÿ[JLYãYã[ôV[ôVJNŸò]‘ò]‘€›\òŸTÿÿ[Y
À[⁄Ÿ‘‹ö]K[ôVU—PT”ó“SëW÷
úÿÿ[K[ôVKU—PT”ó“SëW÷Júÿÿ[Kÿÿ[JNÿÀúô\›‹ôJ
NﬂBàö]ò]H›]X»ôX›]\–Ÿ[ôX›
[ùõ›À[ù€€
^⁄[ùYùX€€
î”’Tê—W—îêSQW’“Q‹\õ› î”’Tê—W—îêSQW“RQ“‹ô]\õàô]»ôX›
Yù‹Yù
‘”’Tê—W—îêSQW’“Q‹
‘”’Tê—W—îêSQW“RQ“
NﬂHö]ò]H›]X»õÿ]]X⁄‘\ŸJ‹ŸH‹ŸJ^⁄Yä‹ŸOO[ù[‹ŸKú›]Q\ò][€èLä\ô]\õàé‹ô]\õàX]õX^
ãX]õZ[äYã‹ŸKú›]P€ÿ⁄À‹‹ŸKú›]Q\ò][€äJNﬂBàXõX»›]X»õÿ]ŸX\€êÿ\úûP[ô€J\ôX›[€à\ôX›[€ä^⁄Yä\ôX›[€èO[ù[
\ô]\õàé‹›⁄]⁄
\ôX›[€ä^ÿÿ\ŸHïŒúô]\õàMôéÿÿ\ŸHëNúô]\õàMçéÿÿ\ŸH’Œúô]\õàÃôéÿÿ\ŸH—Núô]\õàMåôéŸYò][úô]\õàéﬂ_HXõX»›]X»õÿ]ŸX\€êÿ\úûP[ô€J\ôX›[€à\ôX›[€ã[ù]\–€€[[ä^⁄[ùõ›œX]\‘õ› \ôX›[€äK€€[[èSX]õX^
X]õZ[äQW’–S◊–””SSîÀLK]\–€€[[äJN⁄Yäõ›œ
\ô]\õàé‹ô]\õà–TîñW—îêSQW–Së”V‹õ›◊Vÿ€€[[óNﬂHXõX»›]X»õÿ]ŸX\€êÿ\úûSŸôúŸ]
\ôX›[€à\ôX›[€ä^⁄Yä\ôX›[€èO[ù[
\ô]\õàé‹›⁄]⁄
\ôX›[€ä^ÿÿ\ŸHïŒúô]\õàMŸéÿÿ\ŸHëNúô]\õàôéÿÿ\ŸH’Œúô]\õàNYéÿÿ\ŸH—Núô]\õàéŸYò][úô]\õàéﬂ_HXõX»›]X»õÿ]ŸX\€êÿ\úûSŸôúŸ]
\ôX›[€à\ôX›[€ã[ù]\–€€[[ä^‹ô]\õàÿ\úûQúò[YP[ò⁄‹ä\ôX›[€ã]\–€€[[ãùYJNﬂHXõX»›]X»õÿ]ŸX\€êÿ\úûSŸôúŸ]J\ôX›[€à\ôX›[€ä^⁄Yä\ôX›[€èO[ù[
\ô]\õàåôé‹›⁄]⁄
\ôX›[€ä^ÿÿ\ŸHïŒúô]\õàçôéÿÿ\ŸHëNúô]\õàçéÿÿ\ŸH’Œúô]\õàåYéÿÿ\ŸH—Núô]\õàåŸéŸYò][úô]\õàåôéﬂ_HXõX»›]X»õÿ]ŸX\€êÿ\úûSŸôúŸ]J\ôX›[€à\ôX›[€ã[ù]\–€€[[ä^‹ô]\õàÿ\úûQúò[YP[ò⁄‹ä\ôX›[€ã]\–€€[[ãò[ŸJNﬂHö]ò]H›]X»õÿ]ÿ\úûQúò[YP[ò⁄‹ä\ôX›[€à\ôX›[€ã[ù]\–€€[[ãõ€€X[à
^⁄[ùõ›œX]\‘õ› \ôX›[€äK€€[[èSX]õX^
X]õZ[äQW’–S◊–””SSîÀLK]\–€€[[äJN⁄Yäõ›œ
\ô]\õàé‹ô]\õà––TîñW—îêSQW÷‹õ›◊Vÿ€€[[óNê–TîñW—îêSQW÷V‹õ›◊Vÿ€€[[óNﬂBàXõX»›]X»õÿ]ŸX\€êÿ\úûSX^ù[\
\ôX›[€à\ôX›[€ä^‹ô]\õàX^ÿ\úûRù[\
\ôX›[€ã
NﬂHXõX»›]X»õÿ]ŸX\€êÿ\úûSX^ù[\J\ôX›[€à\ôX›[€ä^‹ô]\õàX^ÿ\úûRù[\
\ôX›[€ãJNﬂHXõX»›]X»õÿ]ŸX\€êÿ\úûSX^ù[\[ô€J\ôX›[€à\ôX›[€ä^‹ô]\õàX^ÿ\úûRù[\
\ôX›[€ãäNﬂHö]ò]H›]X»õÿ]X^ÿ\úûRù[\
\ôX›[€à\ôX›[€ã[ù€€\€ô[ù
^⁄[ùõ›œX]\‘õ› \ôX›[€äN⁄Yäõ›œ
\ô]\õàõÿ]î‘“UUëW“SëíSíUNŸõÿ]X^LéŸõ‹ä[ù€€LNÿ€€QW’–S◊–””SSîŒÿ€€
  ^Ÿõÿ]OX€€\€ô[ùOL––TîñW—îêSQW÷‹õ›◊Vÿ€€LWNò€€\€ô[ùOLO––TîñW—îêSQW÷V‹õ›◊Vÿ€€LWNê–TîñW—îêSQW–Së”V‹õ›◊Vÿ€€LWKèX€€\€ô[ùOL––TîñW—îêSQW÷‹õ›◊Vÿ€€Nò€€\€ô[ùOLO––TîñW—îêSQW÷V‹õ›◊Vÿ€€Nê–TîñW—îêSQW–Së”V‹õ›◊Vÿ€€N€X^SX]õX^
X^X]òXú ãXJJNﬂ\ô]\õàX^ﬂBàXõX»›]X»õÿ]X›[€íZY⁄ò][ [ù€›\òŸJ^⁄Yä€›\òŸO€›\òŸOèT”’Tê—W–P’S”ó–”’Sï
\ô]\õàYé‹ô]\õàX›[€ïö\⁄XõRZY⁄^[ €›\òŸJK õÿ]
T”’Tê—W—îêSQW“RQ“ﬂHXõX»›]X»õ€€X[àYÿXﬁTõÿŸY\ò[]X⁄‘ôXX⁄XõJ
^‹ô]\õàò[ŸNﬂHXõX»›]X»õ€€X[à]⁄\òX›\î‹ŸQ[òXõY

^‹ô]\õàò[ŸNﬂHXõX»›]X»õ€€X[àŸX\€ïò[úŸõ‹õU\Ÿ\‘⁄[ô€SZ\úõ‹ä
^‹ô]\õàùYNﬂHXõX»›]X»õ€€X[à]X⁄—ò[òX⁄’ŸX\€î›⁄[ô‘ôXX⁄XõJ
^‹ô]\õàò[ŸNﬂBàö]ò]H›]X»ö[ò[õÿ]◊HP’S”ó’—PT”ó“Së÷^ÕçYãLÀçYãKåãMKåüKP’S”ó’—PT”ó“Së÷O^ÃçÀåãçÀåãéåãçÀåüN»XõX»›]X»õÿ]X›[€ïŸX\€í[ô
\ôX›[€à⁄\òX›\îŸ[X[ùX‘öYÀê[ò⁄‹ú»ò[òX⁄ ^⁄[ùOXX›[€î€›\òŸR[ô^

N‹ô]\õàOèL	âöOP’S”ó’—PT”ó“Së÷õ[ô›–P’S”ó’—PT”ó“Së÷⁄WNäò[òX⁄œO[ù[Ãéôò[òX⁄Àô€Z[ò[ù[ôû
NﬂHXõX»›]X»õÿ]X›[€ïŸX\€í[ôJ\ôX›[€à⁄\òX›\îŸ[X[ùX‘öYÀê[ò⁄‹ú»ò[òX⁄ ^⁄[ùOXX›[€î€›\òŸR[ô^

N‹ô]\õàOèL	âöOP’S”ó’—PT”ó“Së÷Kõ[ô›–P’S”ó’—PT”ó“Së÷V⁄WNäò[òX⁄œO[ù[Ãéôò[òX⁄Àô€Z[ò[ù[ôûJNﬂHXõX»›]X»õÿ]ŸX\€ê]X⁄”ŸôúŸ]
\ôX›[€à\ôX›[€ä^‹ô]\õàéﬂHXõX»›]X»õÿ]ŸX\€ê]X⁄”ŸôúŸ]J\ôX›[€à\ôX›[€ä^‹ô]\õàéﬂHXõX»›]X»õÿ]ŸX\€ê]X⁄—‹ö\
\ôX›[€à\ôX›[€ä^⁄Yä\ôX›[€èO[ù[
\ô]\õàé‹›⁄]⁄
\ôX›[€ä^ÿÿ\ŸHïŒúô]\õàLçYéÿÿ\ŸHëNúô]\õàçYéÿÿ\ŸH’Œúô]\õàLçYéÿÿ\ŸH—Núô]\õàçYéŸYò][úô]\õàéﬂ_HXõX»›]X»õÿ]ŸX\€ê]X⁄—‹ö\J\ôX›[€à\ôX›[€ä^⁄Yä\ôX›[€èO[ù[
\ô]\õàé‹›⁄]⁄
\ôX›[€ä^ÿÿ\ŸHïŒòÿ\ŸHëNúô]\õàçYéÿÿ\ŸH’Œòÿ\ŸH—Núô]\õàLçYéŸYò][úô]\õàéﬂ_Bà äà]X⁄»ŸX\€à\»õ»[ô\[ô[ù[\‹ò[›⁄[ôŒ»‹öY[ù][€à\»ö^Y\àì—H€€ùX›‹ŸKà
ã¬àXõX»›]X»õÿ]ŸX\€ê]X⁄–[ô€J\ôX›[€à\ôX›[€ãõÿ]\ŸJ^⁄Yä\ôX›[€èO[ù[
\ô]\õàé‹›⁄]⁄
\ôX›[€ä^ÿÿ\ŸHïŒúô]\õàÃôéÿÿ\ŸH’Œúô]\õàôéÿÿ\ŸHëNúô]\õàLÃôéÿÿ\ŸH—Núô]\õàMôéŸYò][úô]\õàéﬂ_BàXõX»›]X»õ€€X[àŸX\€ê]X⁄’\Ÿ\“[ô\[ô[ù›⁄[ô 
^‹ô]\õàò[ŸNﬂBàö]ò]Hõ⁄Yò]‘ò]‘€›\òŸTÿÿ[Y
ÿ[ùò\»Àö]X\ö]X\õÿ]Yùõÿ]‹õÿ]ÿÿ[J^⁄Yäö]X\O[ù[
\ô]\õéÿÀôò]–ö]X\
ö]X\ù[ô]»ôX›äX]úõ›[ô
Yù
KX]úõ›[ô
‹
KX]úõ›[ô
Yù
ÿö]X\ôŸ]⁄Y

Júÿÿ[JKX]úõ›[ô
‹
ÿö]X\ôŸ]ZY⁄

Júÿÿ[JJK^[Z[ù
NﬂHö]ò]Hõ⁄Yò]‘ò]‘€›\òŸP‹õ‹Yÿÿ[Y
ÿ[ùò\»Àö]X\ö]X\[ù‹ò”Yù[ù‹ò’‹[ù‹ò‘öY⁄[ù‹ò–õ›€Kõÿ]Yùõÿ]‹õÿ]ÿÿ[J^⁄Yäö]X\O[ù[‹ò‘öY⁄\‹ò”Yù‹ò–õ›€O\‹ò’‹
\ô]\õé‘ôX›‹òœ[ô]»ôX›
‹ò”Yù‹ò’‹‹ò‘öY⁄‹ò–õ›€JN‘ôX›à›[ô]»ôX›äX]úõ›[ô
Yù
KX]úõ›[ô
‹
KX]úõ›[ô
Yù
 ‹ò‘öY⁄\‹ò”Yù
Júÿÿ[JKX]úõ›[ô
‹
 ‹ò–õ›€K\‹ò’‹
Júÿÿ[JJNÿÀôò]–ö]X\
ö]X\‹òÀ›^[Z[ù
NﬂHö]ò]Hõ⁄Yò]–]\–Ÿ[
ÿ[ùò\»Àö]X\]\À[ùõ›À[ù€€[ùúò[YU⁄Y[ùúò[YRZY⁄õÿ]õ€›[ò⁄‹ñõÿ]õ€›[ò⁄‹ñKõÿ]ò]‘ÿÿ[Kõÿ][ò⁄‹ñKõÿ]
^⁄[ùYùX€€
ôúò[YU⁄Y‹\õ› ôúò[YRZY⁄‘ôX›‹òœ[ô]»ôX›
Yù‹Yù
Ÿúò[YU⁄Y‹
Ÿúò[YRZY⁄
NŸõÿ]ÿÿ[Y⁄YYúò[YU⁄Y
ôò]‘ÿÿ[Kÿÿ[YZY⁄Yúò[YRZY⁄
ôò]‘ÿÿ[Kÿÿ[Y[ò⁄‹ñYõ€›[ò⁄‹ñ
ôò]‘ÿÿ[Kÿÿ[Y[ò⁄‹ñOYõ€›[ò⁄‹ñJôò]‘ÿÿ[N‘ôX›à›[ô]»ôX›äX]úõ›[ô
\ÿÿ[Y[ò⁄‹ñ
KX]úõ›[ô
[ò⁄‹ñK\ÿÿ[Y[ò⁄‹ñJKX]úõ›[ô
\ÿÿ[Y[ò⁄‹ñ
‹ÿÿ[Y⁄Y
KX]úõ›[ô
[ò⁄‹ñK\ÿÿ[Y[ò⁄‹ñJ‹ÿÿ[YZY⁄
JNÿÀôò]–ö]X\
]\À‹òÀ›^[Z[ù
NﬂBàö]ò]Hö]X\ûSÿYûSò[YP[ûJô\€›\òŸ\»ô\€›\òŸ\À›ö[ô»ò[YJ^⁄Yäô\€›\òŸ\œO[ù[ò[YOO[ù[
\ô]\õàù[›û^⁄[ùô\“Y\ô\€›\òŸ\ÀôŸ]Y[ùYöY\äò[YKôò]ÿXõHãò€€Kúõ⁄ôX›\öÀõ[ÿö[HäN⁄Yäô\“YOL
\ô]\õàù[–ö]X\òX›‹ûKì‹[€ú»‹[€úœ[ô]»ö]X\òX›‹ûKì‹[€ú 
N€‹[€úÀö[îÿÿ[YYò[ŸN‹ô]\õàö]X\òX›‹ûKôX€ŸTô\€›\òŸJô\€›\òŸ\Àô\“Y‹[€ú NﬂXÿ]⁄
õ›ÿXõHY€õ‹ôY
^‹ô]\õàù[ﬂ_Bàö]ò]Hö]X\ûSÿYûSò[YJô\€›\òŸ\»ô\€›\òŸ\À›ö[ô»ò[YK[ù^X›Y⁄Y[ù^X›YZY⁄
^⁄Yäô\€›\òŸ\œO[ù[ò[YOO[ù[
\ô]\õàù[›û^⁄[ùô\“Y\ô\€›\òŸ\ÀôŸ]Y[ùYöY\äò[YKôò]ÿXõHãò€€Kúõ⁄ôX›\öÀõ[ÿö[HäN⁄Yäô\“YOL
\ô]\õàù[–ö]X\òX›‹ûKì‹[€ú»‹[€úœ[ô]»ö]X\òX›‹ûKì‹[€ú 
N€‹[€úÀö[îÿÿ[YYò[ŸN–ö]X\X€ŸYPö]X\òX›‹ûKôX€ŸTô\€›\òŸJô\€›\òŸ\Àô\“Y‹[€ú N‹ô]\õàò[Y]\ X€ŸY^X›Y⁄Y^X›YZY⁄
OŸX€ŸYõù[ﬂXÿ]⁄
õ›ÿXõHY€õ‹ôY
^‹ô]\õàù[ﬂ_BàXõX»›]X»[ùÿ[—úò[YR[ô^
õÿ]ÿ[–€ÿ⁄ ^Ÿõÿ]ÿYôP€ÿ⁄œSX]õX^
ãÿ[–€ÿ⁄ K\ŸO\ÿYôP€ÿ⁄…U–S◊–÷P”W‘—P””ëŒ‹ô]\õàX]õZ[äQW’–S◊–””SSîÀLã
[ù
SX]ôõ€‹ä
\ŸJÀåYäK’–S◊—îêSQW‘—P””ë JNﬂHö]ò]H›]X»€€ù^ö[ôõÿŸ\‹–€€ù^

^›û^–€\‹œœàX›]ö]UôXYP€\‹Àôõ‹ìò[YJò[ôõ⁄Yò\êX›]ö]UôXYäN”Y]Ÿ›\úô[ù\Xÿ][€èXX›]ö]UôXYôŸ]X€\ôYY]Ÿ
ò›\úô[ù\Xÿ][€àäN”ÿöôX›\Xÿ][€èX›\úô[ù\Xÿ][€ãö[ùõ⁄ŸJù[
N‹ô]\õà\Xÿ][€à[ú›[òŸ[Ÿà€€ù^ €€ù^
X\Xÿ][€éõù[ﬂXÿ]⁄
õ›ÿXõHY€õ‹ôY
^‹ô]\õàù[ﬂ_Hö]ò]Hô\€›\òŸ\»ö[ôõÿŸ\‹‘ô\€›\òŸ\ 
^–€€ù^œYö[ôõÿŸ\‹–€€ù^

N‹ô]\õàœO[ù[€ù[òÀôŸ]ô\€›\òŸ\ 
NﬂHö]ò]H›]X»õ€€X[àò[Y]\ ö]X\ö]X\[ù^X›Y⁄Y[ù^X›YZY⁄
^‹ô]\õàö]X\O[ù[	âòö]X\ôŸ]⁄Y

OOY^X›Y⁄Y	âòö]X\ôŸ]ZY⁄

OOY^X›YZY⁄ﬂH äàì—HX›[€åà€›\òŸH‹ô\à\»ö\›X[Hô\öYöYYYÿZ[ú›Hõ›\à^òX›YõŸHúò[Y\ŒàïœLëOLK’œLã—OLÀàŸX\€àY]Y]H\»õ›Hì—H€›\òŸK[X\]]‹ö]Kà
ã»XõX»›]X»[ùX›[€î€›\òŸR[ô^
\ôX›[€à\ôX›[€ä^‹ô]\õà]\‘õ› \ôX›[€äNﬂHö]ò]H›]X»[ö[X][€êX›[€à[ôô\ê[ö[X][€êX›[€ä›]H›]KYôôX›ò[Z[HYôôX››ö[ô»ŸX\€ïö\›X[ôYä^⁄Yä›]OOT›]KêUP“…âö\’ŸX\€ê\X\ò[òŸJŸX\€ïö\›X[ôYäJ\ô]\õà[ö[X][€êX›[€ãî’“SëŒ⁄YäYôôX›OQYôôX›ò[Z[Kïì’ \ô]\õà[ö[X][€êX›[€ãïì’Œ⁄YäYôôX›OQYôôX›ò[Z[Kí“P“ \ô]\õà[ö[X][€êX›[€ãí“P“Œ⁄YäYôôX›OQYôôX›ò[Z[Kê–T’YôôX›OQYôôX›ò[Z[KìPQ“P \ô]\õà[ö[X][€êX›[€ãê–T’⁄YäYôôX›OQYôôX›ò[Z[Kî““S
\ô]\õà[ö[X][€êX›[€ãî““S‹ô]\õà›]OOT›]KêUP“œ–[ö[X][€êX›[€ãîSê“õù[ﬂBàö]ò]Hõ⁄Yò]‘ÿYôTX\ÿ[ùò[òX⁄ ÿ[ùò\»À‹ŸH‹ŸKõÿ][ò⁄‹ñJ^ÿõ€€X[àYù\‹ŸKô\ôX›[€èOQ\ôX›[€ãìïﬂ‹ŸKô\ôX›[€èOQ\ôX›[€ãî’À›€è\‹ŸKô\ôX›[€èOQ\ôX›[€ãî’ﬂ‹ŸKô\ôX›[€èOQ\ôX›[€ãî—N⁄[ùúò[YO\‹ŸKú›]OOT›]Kï–Sœ›ÿ[—úò[YR[ô^
ô\Ÿ[ù][€ïÿ[–€ÿ⁄ Nå›\Yúò[YOOLOÀLNôúò[YOOLœÃNåŸõÿ]\ŸO\‹ŸKú›]Q\ò][€èLèÃéìX]õX^
ãX]õZ[äYã‹ŸKú›]P€ÿ⁄À‹‹ŸKú›]Q\ò][€äJNÿÀúÿ]ôJ
NÿÀùò[ú€]J‹ŸKûLLôäîVQTó‘ëSëTó‘––SK[ò⁄‹ñKLÃôäîVQTó‘ëSëTó‘––SJNÿÀúÿÿ[JVQTó‘ëSëTó‘––SKVQTó‘ëSëTó‘––SJN⁄[ù›][ôOLôåYNLMÀ⁄⁄[è\‹ŸKö]õ\⁄ÃôôôôÿÿNåôòŒNçÀ⁄⁄[íOLôôMåÀZ\èLôçåÕçãZ\íOLôçÃçLLÿKZ\î⁄Y›œLôåòÃåXK€›LôéçÕçMã€›OLôòYMMô€›⁄Y›œLôççMŸã[ùœLôççM[ù“OLôçåÕXÕMK⁄ŸOLôåÃÃòÃé⁄[ùôX\ñ[YùÕŒåMò\ñ[YùÃLŒé‹
À›][ôKKNKÀ
N‹
À[ùÀLNKK N‹
À›][ôKò\ñ
‹›\åKÀ
N‹
À[ùÀò\ñ
‹›\
ÃKåããäN‹
À⁄ŸKò\ñ
‹›\LKéK N‹
À›][ôKôX\ñ\›\åJN‹
À[ù“KôX\ñ\›\
ÃKåKã N‹
À⁄ŸKôX\ñ\›\LKéã N⁄[ù‹ú€÷[YùÕŒéN‹
À›][ôK‹ú€÷KKLäN‹
À€›⁄Y›À‹ú€÷
ÃKLÀL
N‹
À€›‹ú€÷
ÃãLãJN‹
À€›K‹ú€÷
ÃãLãäN⁄[ùò\î⁄›[\è[YùÃMNé‹
À›][ôKò\î⁄›[\ãLKÀ
N‹
À⁄⁄[ãò\î⁄›[\ä YùÃåJKLããäN⁄[ùôX\î⁄›[\è[YùÕéåMŒ‹
À›][ôKôX\î⁄›[\ãLK
N‹
À⁄⁄[ãôX\î⁄›[\ä YùÃåJKLãÀäN⁄[ùXY[YùŒçŒ‹
À›][ôKXYKLJN‹
ÀZ\î⁄Y›ÀXYKLJN‹
ÀZ\ãXY
ÃKKJN‹
ÀZ\íKXY
ÃãKJN⁄Yä›€ä^‹
À⁄⁄[ãXY
ÃKJN‹
À⁄⁄[íKXY
ÃããäN‹
ÀôåéYNKYù⁄XY
ÃŒöXY
ÕããKJNﬂY[ŸH
À⁄⁄[ãXY
ÃãKã
N⁄Yä‹ŸKú›]OOT›]Kê–T’‹ŸKú›]OOT›]Kî““S
^ŸûZ[ùúŸ]›[JZ[ùî›[Kî’ì“—JNŸûZ[ùúŸ]›õ⁄ŸU⁄Y
ôäNŸûZ[ùúŸ]€€‹äÿÕŒXŸôôäNÿÀôò]–⁄\ò€JLä YùÀMYéçYäK
‹\ŸJçYãûZ[ù
NŸûZ[ùúŸ]›[JZ[ùî›[KëíS
NﬂZYä‹ŸKú›]OOT›]KëPQ
XÀúõ›]JYùÀMÃôéçÃôãLôãÃäNÿÀúô\›‹ôJ
NﬂBàö]ò]Hõ⁄Y
ÿ[ùò\»À[ù€€‹ãõÿ]õÿ]Kõÿ]Àõÿ]
^‹^[Z[ùúŸ]›[JZ[ùî›[KëíS
N‹^[Z[ùúŸ]€€‹ä€€‹äNÿÀôò]‘ôX›
K
›ÀJ⁄^[Z[ù
NﬂHXõX»õ€€X[à\‘ô\]Z\ôY›]P€€ùòX›

^‹ô]\õà⁄\òX›\îô[ô\ô\ê]Y]ú\‹Ÿ\ 
NﬂHXõX»›ö[ô»€€ùòX›]Y]›[[X\ûJ
^‹ô]\õà⁄\òX›\îô[ô\ô\ê]Y]ú›[[X\ûJ
NﬂHXõX»õ€€X[à›€ú‘^Y\ìÿÿ[YôôX› 
^‹ô]\õàùYNﬂHXõX»õÿ]^Y\îô[ô\îÿÿ[J
^‹ô]\õàVQTó‘ëSëTó‘––SNﬂHXõX»õ€€X[à\Ÿ\—Yò][]\—õ‹ä›]H›]J^‹ô]\õà›]OOT›]KíQ_›]OOT›]Kï–Sﬂ›]OOT›]KíU‹ô\€›\òŸP]\–X›]ôJ
Nú›]OOT›]KêUP“…âú€›\òŸPX›[€êX›]ôJ
NﬂBü