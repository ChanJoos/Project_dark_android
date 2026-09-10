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

/** [ADAPTED] Crash-safe 24x32 martial-artist presentation using explicit four-direction resources. */
public final class CharacterRenderer {
  public static final String EVIDENCE="USER_MARTIAL_ARTIST_REFERENCE+ADAPTED";
  public static final String ASSET_STATUS="ADAPTED_RESOURCE_ATLAS_OR_SAFE_FALLBACK_ORIGINAL_PENDING_CROP";
  public static final String PRESENTATION_PROFILE="MARTIAL_ARTIST_SOURCE_SHAPED_20260910_R2";

  public static final float PLAYER_RENDER_SCALE=1.50f;
  public static final float SHADOW_RENDER_SCALE=0.72f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;
  public static final float BASE_HEIGHT=32f;
  public static final float HEAD_TO_BODY_RATIO=0.28f;
  public static final boolean HARD_PIXEL_GRID=true;
  public static final boolean MARTIAL_ARTIST_WEAPONLESS=true;
  public static final boolean SHIELD_ALLOWED=true;

  public static final int ATLAS_FRAME_WIDTH=24;
  public static final int ATLAS_FRAME_HEIGHT=32;
  public static final int IDLE_WALK_COLUMNS=5;
  public static final int ATTACK_COLUMNS=4;
  public static final int ATLAS_ROWS=4;
  public static final int IDLE_WALK_WIDTH=ATLAS_FRAME_WIDTH*IDLE_WALK_COLUMNS;
  public static final int ACTION_WIDTH=ATLAS_FRAME_WIDTH*ATTACK_COLUMNS;
  public static final int ATLAS_HEIGHT=ATLAS_FRAME_HEIGHT*ATLAS_ROWS;

  public enum Direction { NW, NE, SW, SE }
  public enum State { IDLE, WALK, CAST, ATTACK, SKILL, HIT, DEAD }
  public enum Layer { BODY, HAIR, EQUIPMENT, WEAPON, EFFECT }
  public enum EffectFamily { NONE, CAST, MAGIC, THROW, PUNCH, KICK, SKILL, HIT }

  public static final List<Layer> DRAW_ORDER=Collections.unmodifiableList(Arrays.asList(
      Layer.BODY,Layer.HAIR,Layer.EQUIPMENT,Layer.WEAPON,Layer.EFFECT));

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
    idleWalkAtlas=tryLoad(resources,R.drawable.player_martial_idle_walk,IDLE_WALK_WIDTH,ATLAS_HEIGHT);
    attackAtlas=tryLoad(resources,R.drawable.player_martial_attack,ACTION_WIDTH,ATLAS_HEIGHT);
    // Never throw for visual resources. Missing/corrupt/mismatched atlases must not block app startup.
  }

  /** Physical row contract. No mirror/reuse inference is allowed. */
  public static int atlasRow(Direction direction){
    if(direction==null)return -1;
    switch(direction){case NW:return 0;case NE:return 1;case SW:return 2;case SE:return 3;default:return -1;}
  }
  public static Direction visualFacingForRow(int row){
    switch(row){case 0:return Direction.NW;case 1:return Direction.NE;case 2:return Direction.SW;case 3:return Direction.SE;default:return null;}
  }

  public boolean resourceAtlasActive(){return validAtlas(idleWalkAtlas,IDLE_WALK_WIDTH,ATLAS_HEIGHT);}
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
    drawSafeFallback(canvas,pose,anchorY);
  }

  private void drawShadow(Canvas c,Pose pose,float anchorY){
    float width=(pose.state==State.DEAD?13f:10.5f)*SHADOW_RENDER_SCALE;
    float height=(pose.state==State.DEAD?2.0f:2.8f)*SHADOW_RENDER_SCALE;
    fxPaint.setStyle(Paint.Style.FILL);fxPaint.setColor(0x50000000);
    c.drawOval(new RectF(pose.x-width,anchorY-height,pose.x+width,anchorY+height),fxPaint);
  }

  private void drawIdleWalk(Canvas c,Pose pose,float anchorY){
    int row=atlasRow(pose.direction); if(row<0){drawSafeFallback(c,pose,anchorY);return;}
    int col=pose.state==State.IDLE?0:1+(((int)(Math.max(0f,pose.walkClock)*7f))&3);
    drawAtlasCell(c,idleWalkAtlas,row,col,anchorY,pose.x);
  }

  private void drawAttack(Canvas c,Pose pose,float anchorY){
    int row=atlasRow(pose.direction); if(row<0){drawSafeFallback(c,pose,anchorY);return;}
    float q=pose.stateDuration<=0f?0f:Math.max(0f,Math.min(0.9999f,pose.stateClock/pose.stateDuration));
    int col=Math.min(ATTACK_COLUMNS-1,(int)(q*ATTACK_COLUMNS));
    drawAtlasCell(c,attackAtlas,row,col,anchorY,pose.x);
  }

  private void drawAtlasCell(Canvas c,Bitmap atlas,int row,int col,float anchorY,float x){
    int left=col*ATLAS_FRAME_WIDTH,top=row*ATLAS_FRAME_HEIGHT;
    Rect src=new Rect(left,top,left+ATLAS_FRAME_WIDTH,top+ATLAS_FRAME_HEIGHT);
    float w=ATLAS_FRAME_WIDTH*PLAYER_RENDER_SCALE,h=ATLAS_FRAME_HEIGHT*PLAYER_RENDER_SCALE;
    RectF dst=new RectF(Math.round(x-w*.5f),Math.round(anchorY-h),Math.round(x+w*.5f),Math.round(anchorY));
    c.drawBitmap(atlas,src,dst,pixelPaint);
  }

  private Bitmap tryLoad(Resources resources,int resId,int expectedWidth,int expectedHeight){
    if(resources==null)return null;
    try{
      BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
      Bitmap decoded=BitmapFactory.decodeResource(resources,resId,options);
      return validAtlas(decoded,expectedWidth,expectedHeight)?decoded:null;
    }catch(Throwable ignored){return null;}
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

  /** Crash-safe, weaponless fallback only. It preserves facing and the fixed foot anchor. */
  private void drawSafeFallback(Canvas c,Pose pose,float anchorY){
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    int frame=pose.state==State.WALK?(((int)(Math.max(0f,pose.walkClock)*7f))&3):0;
    int step=frame==1?-1:frame==3?1:0;
    float phase=pose.stateDuration<=0f?0f:Math.max(0f,Math.min(1f,pose.stateClock/pose.stateDuration));
    int reach=pose.state==State.ATTACK?Math.round(4f*(float)Math.sin(Math.PI*phase)):0;
    c.save(); c.translate(pose.x-12f*PLAYER_RENDER_SCALE,anchorY-32f*PLAYER_RENDER_SCALE); c.scale(PLAYER_RENDER_SCALE,PLAYER_RENDER_SCALE);
    int outline=0xff181619,skin=pose.hitFlash?0xffffd7ca:0xffca8458,skinHi=0xffe8ab75,pants=0xff212b43,pantsHi=0xff38445f,shoe=0xff292e42,red=0xffbd292c;
    int hair=0xffcbc8cb,hairHi=0xfff1eee7,hairShadow=0xff85818a,shield=0xff293246,shieldHi=0xff505b70;
    int nearX=left?7:14,farX=left?13:8;
    px(c,outline,9,19,7,4);px(c,pants,10,19,5,3);
    px(c,outline,farX+step,21,3,8);px(c,pants,farX+step+1,22,2,6);px(c,shoe,farX+step-1,28,5,3);px(c,red,farX+step,28,2,1);
    px(c,outline,nearX-step,20,4,9);px(c,pantsHi,nearX-step+1,21,2,7);px(c,shoe,nearX-step-1,28,6,3);px(c,red,nearX-step,28,2,1);
    int torsoX=left?7:9;px(c,outline,torsoX,9,9,12);px(c,skin,torsoX+1,10,7,9);px(c,skinHi,torsoX+2,10,2,2);
    int farShoulder=left?15:8;px(c,outline,farShoulder,11,3,8);px(c,skin,farShoulder+(left?0:1),12,2,6);
    int nearShoulder=left?6:17;
    if(left){px(c,outline,nearShoulder-reach,11,4+reach,8);px(c,skin,nearShoulder-reach,12,3+reach,6);}else{px(c,outline,nearShoulder,11,4+reach,8);px(c,skin,nearShoulder+1,12,3+reach,6);}
    int shieldX=left?16:4;px(c,shield,shieldX,13,5,9);px(c,shieldHi,shieldX+2,15,1,5);
    int headX=left?8:7;px(c,outline,headX,0,10,9);px(c,hairShadow,headX,1,10,7);px(c,hair,headX+1,0,8,7);px(c,hairHi,headX+2,0,6,2);
    if(down){px(c,skin,headX+1,3,8,5);px(c,red,headX+1,3,8,2);px(c,0xff221a18,left?headX+3:headX+6,6,1,1);}else px(c,red,left?headX:headX+8,4,2,2);
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
