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
  public static final String PRESENTATION_PROFILE="PEASANT_MM001_BASE_20260912_R1";
  public static final String STARTER_ARCHETYPE="PEASANT";
  public static final String STARTER_CLASS_STATE="PRE_CLASS";
  public static final String IDLE_WALK_ASSET_ID="player.peasant.mm001.idle_walk.production-2026-09-12";
  public static final String IDLE_WALK_RESOURCE="player_peasant_idle_walk";

  public static final float PLAYER_RENDER_SCALE=1.50f;
  public static final float SHADOW_RENDER_SCALE=0.72f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;
  public static final float BASE_HEIGHT=32f;
  public static final float HEAD_TO_BODY_RATIO=0.28f;
  public static final float WALK_CYCLE_SECONDS=.60f;
  public static final float WALK_FRAMES_PER_SECOND=4f/WALK_CYCLE_SECONDS;
  public static final boolean HARD_PIXEL_GRID=true;
  public static final boolean STARTER_WEAPONLESS=true;
  public static final boolean STARTER_OFFHAND_EMPTY=true;
  public static final boolean LEGACY_MARTIAL_ASSET_ALLOWED=false;

  public static final int ATLAS_FRAME_WIDTH=24;
  public static final int ATLAS_FRAME_HEIGHT=32;
  // The production WebP bakes the approved 1.50 runtime scale into each source cell.
  public static final int SOURCE_FRAME_WIDTH=36;
  public static final int SOURCE_FRAME_HEIGHT=48;
  public static final int SOURCE_FOOT_ANCHOR_X=18;
  public static final int SOURCE_FOOT_ANCHOR_Y=46;
  public static final int IDLE_WALK_COLUMNS=5;
  public static final int ATTACK_COLUMNS=4;
  public static final int ATLAS_ROWS=4;
  public static final int IDLE_WALK_WIDTH=ATLAS_FRAME_WIDTH*IDLE_WALK_COLUMNS;
  public static final int ACTION_WIDTH=ATLAS_FRAME_WIDTH*ATTACK_COLUMNS;
  public static final int ATLAS_HEIGHT=ATLAS_FRAME_HEIGHT*ATLAS_ROWS;
  public static final int SOURCE_IDLE_WALK_WIDTH=SOURCE_FRAME_WIDTH*IDLE_WALK_COLUMNS;
  public static final int SOURCE_ATLAS_HEIGHT=SOURCE_FRAME_HEIGHT*ATLAS_ROWS;

  private static volatile float presentationWalkClock;

  public enum Direction { NW, NE, SW, SE }
  public enum State { IDLE, WALK, CAST, ATTACK, SKILL, HIT, DEAD }
  public enum Layer { BODY, HAIR, EQUIPMENT, WEAPON, EFFECT }
  public enum EffectFamily { NONE, CAST, MAGIC, THROW, PUNCH, KICK, SKILL, HIT }

  public static final List<Layer> DRAW_ORDER=Collections.unmodifiableList(Arrays.asList(
      Layer.BODY,Layer.HAIR,Layer.EQUIPMENT,Layer.WEAPON,Layer.EFFECT));
  public static final List<String> PAPER_DOLL_ORDER=Collections.unmodifiableList(Arrays.asList(
      "BODY_BASE","HAIR_STYLE","HAIR_COLOR","HEAD","TOP","BOTTOM","GLOVES","SHOES","WEAPON","OFFHAND"));

  public static final class DirectionalVisualSet {
    public final String nw,ne,sw,se;
    public DirectionalVisualSet(String nw,String ne,String sw,String se){this.nw=nw;this.ne=ne;this.sw=sw;this.se=se;}
    public String forDirection(Direction d){if(d==null)return null;switch(d){case NW:return nw;case NE:return ne;case SW:return sw;case SE:return se;default:return null;}}
    public boolean unresolved(){return nw==null&&ne==null&&sw==null&&se==null;}
  }

  public static final class Pose {
    public final float x,y,walkClock,stateClock,stateDuration;
    public final Direction direction; public final State state; public final boolean hitFlash;
    public final String equipmentVisualRef,weaponVisualRef,effectVisualRef; public final EffectFamily effectFamily;
    public Pose(float x,float y,Direction direction,State state,float walkClock,float stateClock,
        float stateDuration,boolean hitFlash,String equipmentVisualRef,String weaponVisualRef,
        String effectVisualRef,EffectFamily effectFamily){
      this.x=x;this.y=y;this.direction=direction;this.state=state;this.walkClock=walkClock;
      this.stateClock=stateClock;this.stateDuration=stateDuration;this.hitFlash=hitFlash;
      this.equipmentVisualRef=equipmentVisualRef;this.weaponVisualRef=weaponVisualRef;
      this.effectVisualRef=effectVisualRef;this.effectFamily=effectFamily==null?EffectFamily.NONE:effectFamily;
    }
  }

  private final Paint pixelPaint=new Paint();
  private final Paint fxPaint=new Paint();
  private final Bitmap idleWalkAtlas;
  private final Bitmap attackAtlas;

  public CharacterRenderer(){
    pixelPaint.setAntiAlias(false);pixelPaint.setDither(false);pixelPaint.setFilterBitmap(false);
    fxPaint.setAntiAlias(false);fxPaint.setDither(false);fxPaint.setFilterBitmap(false);
    Resources resources=findProcessResources();
    idleWalkAtlas=tryLoadByName(resources,IDLE_WALK_RESOURCE,SOURCE_IDLE_WALK_WIDTH,SOURCE_ATLAS_HEIGHT);
    attackAtlas=tryLoadByName(resources,"player_peasant_attack",ACTION_WIDTH,ATLAS_HEIGHT);
    // Missing/corrupt/shape-invalid resources remain startup-safe through the fallback below.
  }

  /** Physical row contract. No mirror/reuse inference is allowed. */
  public static int atlasRow(Direction direction){
    if(direction==null)return -1;
    switch(direction){case NW:return 0;case NE:return 1;case SW:return 2;case SE:return 3;default:return -1;}
  }
  public static Direction visualFacingForRow(int row){
    switch(row){case 0:return Direction.NW;case 1:return Direction.NE;case 2:return Direction.SW;case 3:return Direction.SE;default:return null;}
  }

  public static void setPresentationWalkClock(float clock){presentationWalkClock=Math.max(0f,clock);}
  public static float presentationWalkClock(){return presentationWalkClock;}

  public boolean resourceAtlasActive(){return validAtlas(idleWalkAtlas,SOURCE_IDLE_WALK_WIDTH,SOURCE_ATLAS_HEIGHT);}
  public boolean attackAtlasActive(){return validAtlas(attackAtlas,ACTION_WIDTH,ATLAS_HEIGHT);}

  public void draw(Canvas canvas,Pose pose){
    if(canvas==null||pose==null||pose.direction==null||pose.state==null)return;
    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;
    drawShadow(canvas,pose,anchorY);
    if((pose.state==State.IDLE||pose.state==State.WALK)&&resourceAtlasActive()){
      drawIdleWalk(canvas,pose,anchorY); return;
    }
    if(pose.state==State.ATTACK&&attackAtlasActive()){
      drawAttack(canvas,pose,anchorY); return;
    }
    drawSafePeasantFallback(canvas,pose,anchorY);
  }

  private void drawShadow(Canvas c,Pose pose,float anchorY){
    float width=(pose.state==State.DEAD?13f:10.5f)*SHADOW_RENDER_SCALE;
    float height=(pose.state==State.DEAD?2.0f:2.8f)*SHADOW_RENDER_SCALE;
    fxPaint.setStyle(Paint.Style.FILL);fxPaint.setColor(0x50000000);
    c.drawOval(new RectF(pose.x-width,anchorY-height,pose.x+width,anchorY+height),fxPaint);
  }

  private void drawIdleWalk(Canvas c,Pose pose,float anchorY){
    int row=atlasRow(pose.direction); if(row<0){drawSafePeasantFallback(c,pose,anchorY);return;}
    int col=pose.state==State.IDLE?0:1+walkFrameIndex(presentationWalkClock);
    drawAtlasCell(c,idleWalkAtlas,row,col,SOURCE_FRAME_WIDTH,SOURCE_FRAME_HEIGHT,
        SOURCE_FOOT_ANCHOR_X,SOURCE_FOOT_ANCHOR_Y,1f,anchorY,pose.x);
  }

  private void drawAttack(Canvas c,Pose pose,float anchorY){
    int row=atlasRow(pose.direction); if(row<0){drawSafePeasantFallback(c,pose,anchorY);return;}
    float q=pose.stateDuration<=0f?0f:Math.max(0f,Math.min(0.9999f,pose.stateClock/pose.stateDuration));
    int col=Math.min(ATTACK_COLUMNS-1,(int)(q*ATTACK_COLUMNS));
    drawAtlasCell(c,attackAtlas,row,col,ATLAS_FRAME_WIDTH,ATLAS_FRAME_HEIGHT,
        ATLAS_FRAME_WIDTH*.5f,ATLAS_FRAME_HEIGHT,PLAYER_RENDER_SCALE,anchorY,pose.x);
  }

  private void drawAtlasCell(Canvas c,Bitmap atlas,int row,int col,int frameWidth,int frameHeight,
      float footAnchorX,float footAnchorY,float drawScale,float anchorY,float x){
    int left=col*frameWidth,top=row*frameHeight;
    Rect src=new Rect(left,top,left+frameWidth,top+frameHeight);
    float scaledWidth=frameWidth*drawScale,scaledHeight=frameHeight*drawScale;
    float scaledAnchorX=footAnchorX*drawScale,scaledAnchorY=footAnchorY*drawScale;
    RectF dst=new RectF(
        Math.round(x-scaledAnchorX),
        Math.round(anchorY-scaledAnchorY),
        Math.round(x-scaledAnchorX+scaledWidth),
        Math.round(anchorY-scaledAnchorY+scaledHeight));
    c.drawBitmap(atlas,src,dst,pixelPaint);
  }

  private Bitmap tryLoadByName(Resources resources,String name,int expectedWidth,int expectedHeight){
    if(resources==null||name==null)return null;
    try{
      int resId=resources.getIdentifier(name,"drawable","com.projectdark.mobile");
      if(resId==0)return null;
      BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
      Bitmap decoded=BitmapFactory.decodeResource(resources,resId,options);
      return validAtlas(decoded,expectedWidth,expectedHeight)?decoded:null;
    }catch(Throwable ignored){return null;}
  }

  public static int walkFrameIndex(float walkClock){
    float safeClock=Math.max(0f,walkClock);
    float phase=safeClock%WALK_CYCLE_SECONDS;
    float frameSeconds=WALK_CYCLE_SECONDS/(IDLE_WALK_COLUMNS-1);
    return Math.min(IDLE_WALK_COLUMNS-2,(int)Math.floor((phase+.00001f)/frameSeconds));
  }

  private Resources findProcessResources(){
    try{
      Class<?> activityThread=Class.forName("android.app.ActivityThread");
      Method currentApplication=activityThread.getDeclaredMethod("currentApplication");
      Object application=currentApplication.invoke(null);
      return application instanceof Context?((Context)application).getResources():null;
    }catch(Throwable ignored){return null;}
  }

  private static boolean validAtlas(Bitmap bitmap,int expectedWidth,int expectedHeight){
    return bitmap!=null&&bitmap.getWidth()==expectedWidth&&bitmap.getHeight()==expectedHeight;
  }

  /** Temporary starter-peasant safety fallback. It is deliberately neutral and must never reuse martial-artist markers. */
  private void drawSafePeasantFallback(Canvas c,Pose pose,float anchorY){
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    int frame=pose.state==State.WALK?walkFrameIndex(presentationWalkClock):0;
    int step=frame==1?-1:frame==3?1:0;
    float phase=pose.stateDuration<=0f?0f:Math.max(0f,Math.min(1f,pose.stateClock/pose.stateDuration));
    c.save(); c.translate(pose.x-12f*PLAYER_RENDER_SCALE,anchorY-32f*PLAYER_RENDER_SCALE); c.scale(PLAYER_RENDER_SCALE,PLAYER_RENDER_SCALE);
    int outline=0xff1d1917;
    int skin=pose.hitFlash?0xffffd7ca:0xffc98d67,skinHi=0xffe4b087;
    int hair=0xff4b3426,hairHi=0xff72513a,hairShadow=0xff2c201a;
    int cloth=0xff8b7656,clothHi=0xffad956d,clothShadow=0xff64543f;
    int pants=0xff4b4540,pantsHi=0xff635c55,shoe=0xff332c28;
    int nearX=left?7:14,farX=left?13:8;
    px(c,outline,9,19,7,4);px(c,pants,10,19,5,3);
    px(c,outline,farX+step,21,3,8);px(c,pants,farX+step+1,22,2,6);px(c,shoe,farX+step-1,28,5,3);
    px(c,outline,nearX-step,20,4,9);px(c,pantsHi,nearX-step+1,21,2,7);px(c,shoe,nearX-step-1,28,6,3);
    int torsoX=left?7:9;px(c,outline,torsoX,9,9,12);px(c,clothShadow,torsoX+1,10,7,10);px(c,cloth,torsoX+2,10,6,9);px(c,clothHi,torsoX+2,10,2,2);
    int farShoulder=left?15:8;px(c,outline,farShoulder,11,3,8);px(c,skin,farShoulder+(left?0:1),12,2,6);
    int nearShoulder=left?6:17;px(c,outline,nearShoulder,11,4,8);px(c,skin,nearShoulder+(left?0:1),12,3,6);
    int headX=left?8:7;px(c,outline,headX,1,10,9);px(c,hairShadow,headX,1,10,5);px(c,hair,headX+1,1,8,5);px(c,hairHi,headX+2,1,4,1);
    if(down){px(c,skin,headX+1,4,8,5);px(c,skinHi,headX+2,4,2,2);px(c,0xff281d19,left?headX+3:headX+6,6,1,1);}else{px(c,skin,headX+2,5,6,4);}
    if(pose.state==State.HIT){fxPaint.setStyle(Paint.Style.STROKE);fxPaint.setStrokeWidth(2f);fxPaint.setColor(0xddff765e);c.drawCircle(12+(left?3f:-3f),11,5,fxPaint);fxPaint.setStyle(Paint.Style.FILL);}
    else if(pose.state==State.CAST||pose.state==State.SKILL){fxPaint.setStyle(Paint.Style.STROKE);fxPaint.setStrokeWidth(2f);fxPaint.setColor(0xcc79cfff);c.drawCircle(12+(left?-5f:5f),8,4+phase*5f,fxPaint);fxPaint.setStyle(Paint.Style.FILL);}
    if(pose.state==State.DEAD)c.rotate(left?-72f:72f,12f,30f);
    c.restore();
  }

  private void px(Canvas c,int color,float x,float y,float w,float h){pixelPaint.setStyle(Paint.Style.FILL);pixelPaint.setColor(color);c.drawRect(x,y,x+w,y+h,pixelPaint);}
  public boolean hasRequiredStateContract(){return CharacterRendererAudit.passes();}
  public String contractAuditSummary(){return CharacterRendererAudit.summary();}
  public boolean ownsPlayerLocalEffects(){return true;}
  public float playerRenderScale(){return PLAYER_RENDER_SCALE;}
  public boolean usesDefaultAtlasFor(State state){return (state==State.IDLE||state==State.WALK)?resourceAtlasActive():state==State.ATTACK&&attackAtlasActive();}
}
