package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * [ADAPTED] Four-direction player renderer. V3 abandons smooth vector mannequin shapes and uses
 * hard pixel cells measured against the user-supplied live screenshot of the adjacent original
 * character. It is a reconstruction target, not extracted Nexon source pixels; originals remain
 * PENDING_CROP until a clean directional source sprite is authenticated.
 */
public final class CharacterRenderer {
  public static final String EVIDENCE="USER_SCREENSHOT_MEASURED+ADAPTED";
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final String PRESENTATION_PROFILE="USER_SCREENSHOT_20260910_PIXEL_RIG_V3";
  public static final float PLAYER_RENDER_SCALE=1.50f;
  public static final float SHADOW_RENDER_SCALE=0.72f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;
  public static final float BASE_HEIGHT=30f;
  public static final float HEAD_TO_BODY_RATIO=0.36f;
  public static final float HEAD_WIDTH=12f;
  public static final float SHOULDER_WIDTH=9f;
  public static final boolean HARD_PIXEL_GRID=true;

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

  private static final boolean CONTRACT_VALID=CharacterRendererAudit.passes();
  private final Paint p=new Paint();

  public CharacterRenderer(){
    p.setAntiAlias(false);p.setDither(false);p.setFilterBitmap(false);
    if(!CONTRACT_VALID)throw new IllegalStateException("CharacterRenderer contract audit failed: "+CharacterRendererAudit.summary());
  }

  public void draw(Canvas c,Pose pose){
    if(c==null||pose==null||pose.direction==null||pose.state==null)throw new IllegalArgumentException("Character pose requires direction and state");
    int frame=pose.state==State.WALK?((int)(pose.walkClock*6f)&3):0;
    boolean left=isLeft(pose.direction);float sx=left?-1f:1f,q=phase(pose);
    float bob=pose.state==State.WALK&&((frame&1)==1)?-1f:0f;
    float recoil=pose.state==State.HIT?-2f*sx:0f;
    float action=pose.state==State.ATTACK||pose.state==State.SKILL?(float)Math.sin(Math.PI*q)*sx:0f;
    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;
    float sw=10.5f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?1.35f:1f);
    float sh=2.8f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?.65f:1f);
    p.setColor(0x50000000);c.drawOval(new RectF(pose.x-sw,anchorY-sh,pose.x+sw,anchorY+sh),p);

    c.save();c.translate(pose.x+recoil+action,anchorY-BASE_HEIGHT*PLAYER_RENDER_SCALE+bob*PLAYER_RENDER_SCALE);
    c.scale(PLAYER_RENDER_SCALE,PLAYER_RENDER_SCALE);c.translate(-9f,0f);
    if(pose.state==State.DEAD){c.rotate(left?-74f:74f,9f,27f);c.scale(1.05f,.78f,9f,27f);}
    for(Layer layer:DRAW_ORDER)drawLayer(c,pose,layer,frame);
    c.restore();
  }

  private void drawLayer(Canvas c,Pose pose,Layer layer,int frame){switch(layer){
    case BODY:drawBody(c,pose,frame);break;case HAIR:drawHair(c,pose);break;
    case EQUIPMENT:drawEquipment(c,pose);break;case WEAPON:drawWeapon(c,pose);break;
    case EFFECT:drawEffect(c,pose);break;}}

  private void drawBody(Canvas c,Pose pose,int frame){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);int s=left?-1:1;
    int outline=0xff17130f,skin=pose.hitFlash?0xffffd5c7:0xffd99b6b,skinHi=0xffefb883;
    int dark=0xff172538,mid=0xff263f5e,boot=0xff4a3024;
    int step=pose.state==State.WALK?(frame==1?1:frame==3?-1:0):0;
    int tx=s;

    // pelvis + legs: all integer cells, no floating gaps.
    px(c,outline,6+tx,19,7,4);px(c,dark,7+tx,20,5,3);
    int farX=left?11:5,nearX=left?5:11;
    px(c,outline,farX-step,22,3,6);px(c,dark,farX-step+1,22,2,5);px(c,boot,farX-step,27,4,3);
    px(c,outline,nearX+step,21,3,7);px(c,mid,nearX+step+1,22,2,5);px(c,boot,nearX+step-1,27,5,3);

    // compact torso measured from adjacent original: narrow waist, broad one-pixel shoulder cap.
    px(c,outline,5+tx,10,9,11);px(c,dark,6+tx,11,7,9);px(c,mid,7+tx,12,5,7);
    px(c,0xffb27b35,6+tx,18,7,2); // belt
    px(c,outline,7+tx,8,4,4);px(c,skin,8+tx,8,3,4); // neck bridge

    // near/far arm blocks are attached directly to shoulder cells.
    int farShoulder=left?13+tx:4+tx,nearShoulder=left?4+tx:13+tx;
    int swing=pose.state==State.WALK?step:0;
    armPixels(c,farShoulder,12, -s, 1, -s, 3+swing, outline,skin,false);
    int reach=(pose.state==State.ATTACK||pose.state==State.SKILL)?Math.round(3f*(float)Math.sin(Math.PI*phase(pose))):0;
    int lift=pose.state==State.CAST?-4:pose.state==State.SKILL?-1:0;
    armPixels(c,nearShoulder,12, s, 1+lift, s*(2+reach), 3+lift-swing, outline,skin,true);

    // hard-edged head silhouette; forward diagonals show nose/eyes, rear diagonals show cheek-free back silhouette.
    int hx=3+s*2;
    px(c,outline,hx,0,12,10);px(c,skin,hx+1,2,10,7);px(c,skinHi,hx+2,2,6,2);
    if(down){
      int eye1=left?hx+3:hx+8,eye2=left?hx+7:hx+4;
      px(c,0xff201b19,eye1,5,1,2);px(c,0xff4a3228,eye2,5,1,1);
      px(c,0xffaa624d,left?hx+4:hx+7,8,2,1);
    }
  }

  private void drawHair(Canvas c,Pose pose){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);int s=left?-1:1,hx=3+s*2;
    int d=0xff2a1a13,m=0xff53331f,l=0xff875333;
    // stepped skull mass and fringe intentionally match old sprite vocabulary instead of smooth ellipses.
    px(c,d,hx+1,-1,10,3);px(c,d,hx,1,12,3);px(c,m,hx+2,0,8,2);px(c,l,hx+3,0,4,1);
    px(c,d,left?hx+9:hx,3,3,6);
    if(down){px(c,m,left?hx+1:hx+8,3,3,4);px(c,d,left?hx:hx+10,5,2,3);}
    else{px(c,d,left?hx+7:hx+2,3,4,6);px(c,m,left?hx+2:hx+7,2,3,3);}
  }

  private void drawEquipment(Canvas c,Pose pose){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);int s=left?-1:1,tx=s;
    int navy=pose.hitFlash?0xff9c5b55:0xff1d2f48,blue=0xff31527a,edge=0xff8d9ba4,gold=0xffb77e32;
    px(c,navy,6+tx,11,7,9);px(c,blue,7+tx,12,5,6);px(c,gold,6+tx,18,7,2);
    px(c,edge,left?5+tx:11+tx,11,3,3); // pauldron
    // shield anchored tight to far forearm rather than floating beside body.
    int sx=left?12:2; if(!down)sx+=s;
    px(c,0xff17212c,sx,14,5,8);px(c,0xff2b4968,sx+1,15,3,6);px(c,gold,sx+2,16,1,4);
  }

  private void drawWeapon(Canvas c,Pose pose){
    if(pose.effectFamily==EffectFamily.PUNCH||pose.effectFamily==EffectFamily.KICK)return;
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);int s=left?-1:1,v=down?1:-1;
    float q=phase(pose);int extra=(pose.state==State.ATTACK||pose.state==State.SKILL)?Math.round(5f*(float)Math.sin(Math.PI*q)):0;
    int hx=left?3:15,hy=down?17:14;
    int len=8+extra;
    // pixel staircase blade starts exactly at hand/grip cell.
    px(c,0xff6a4327,hx-s,hy-v,3,2);px(c,0xffc5ccd0,hx+s,hy+v,2,2);
    for(int i=1;i<=len;i++)px(c,i==len?0xffffffff:0xffd9dfe2,hx+s*i,hy+v*i,2,2);
    px(c,0xffa16f31,hx-s*2,hy-v*2,2,2);
  }

  private void drawEffect(Canvas c,Pose pose){
    EffectFamily f=pose.hitFlash?EffectFamily.HIT:pose.effectFamily;if(f==EffectFamily.NONE)return;
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float sx=left?-1f:1f,sy=down?1f:-1f,q=phase(pose);
    p.setStyle(Paint.Style.STROKE);p.setAntiAlias(false);
    switch(f){
      case SKILL:
        p.setStrokeWidth(4f);p.setColor(0xd06ecbff);c.drawArc(new RectF(9+sx*6-15,14+sy*4-12,9+sx*6+15,14+sy*4+12),left?28:198,150,false,p);
        p.setStrokeWidth(2f);p.setColor(0xe7d9f7ff);c.drawArc(new RectF(9+sx*6-18,14+sy*4-14,9+sx*6+18,14+sy*4+14),left?25:195,154,false,p);break;
      case CAST:case MAGIC:
        p.setStrokeWidth(2f);p.setColor(0xcc75c9ff);c.drawCircle(9+sx*4,5+sy*4,4+8*q,p);break;
      case HIT:
        p.setStrokeWidth(2f);p.setColor(0xd9ff765e);c.drawCircle(9-sx*3,10-sy*2,4+5*q,p);break;
      case THROW:
        p.setStyle(Paint.Style.FILL);p.setColor(0xffffd36a);c.drawRect(9+sx*24*q,14+sy*16*q,11+sx*24*q,16+sy*16*q,p);break;
      default:break;
    }
    p.setStyle(Paint.Style.FILL);
  }

  private void armPixels(Canvas c,int sx,int sy,int dx1,int dy1,int dx2,int dy2,int outline,int skin,boolean near){
    int thick=near?3:2;px(c,outline,sx-1,sy-1,thick,thick);px(c,outline,sx+dx1-1,sy+dy1,thick,3);px(c,outline,sx+dx2-1,sy+dy2,thick,3);
    px(c,skin,sx+dx1,sy+dy1+1,1,2);px(c,skin,sx+dx2,sy+dy2+1,2,2);
  }

  private void px(Canvas c,int color,float x,float y,float w,float h){p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRect(x,y,x+w,y+h,p);}
  private boolean isLeft(Direction d){return d==Direction.NW||d==Direction.SW;}
  private boolean isDown(Direction d){return d==Direction.SW||d==Direction.SE;}
  private float phase(Pose pose){return pose.stateDuration<=0?0:Math.max(0,Math.min(1,pose.stateClock/pose.stateDuration));}
  public boolean hasRequiredStateContract(){return CharacterRendererAudit.passes();}
  public String contractAuditSummary(){return CharacterRendererAudit.summary();}
  public boolean ownsPlayerLocalEffects(){return true;}
  public float playerRenderScale(){return PLAYER_RENDER_SCALE;}
}
