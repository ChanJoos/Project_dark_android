package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * [B] Layered procedural character renderer scaffold.
 * Authenticated original character frames remain PENDING_CROP.
 * This class defines the presentation contract without claiming prototype pixels/timings as original LOD assets.
 */
public final class CharacterRenderer {
  public static final String EVIDENCE="B";
  public static final String ASSET_STATUS="PENDING_CROP";

  /** [ADAPTED] Presentation-only scale. Logical/world coordinates are intentionally unchanged. */
  public static final float PLAYER_RENDER_SCALE=0.92f;
  public static final float SHADOW_RENDER_SCALE=0.58f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;

  public enum Direction { NW, NE, SW, SE }
  public enum State { IDLE, WALK, CAST, ATTACK, SKILL, HIT, DEAD }
  public enum Layer { BODY, HAIR, EQUIPMENT, WEAPON, EFFECT }
  public enum EffectFamily { NONE, CAST, MAGIC, THROW, PUNCH, KICK, SKILL, HIT }

  public static final List<Layer> DRAW_ORDER=Collections.unmodifiableList(Arrays.asList(
      Layer.BODY,Layer.HAIR,Layer.EQUIPMENT,Layer.WEAPON,Layer.EFFECT));

  /** Immutable presentation input. Runtime/gameplay state stays outside the renderer. */
  public static final class Pose {
    public final float x,y,walkClock,stateClock,stateDuration;
    public final Direction direction;
    public final State state;
    public final boolean hitFlash;
    public final String equipmentVisualRef,weaponVisualRef,effectVisualRef;
    public final EffectFamily effectFamily;

    public Pose(float x,float y,Direction direction,State state,float walkClock,float stateClock,
        float stateDuration,boolean hitFlash,String equipmentVisualRef,String weaponVisualRef,
        String effectVisualRef,EffectFamily effectFamily){
      this.x=x;this.y=y;this.direction=direction;this.state=state;this.walkClock=walkClock;
      this.stateClock=stateClock;this.stateDuration=stateDuration;this.hitFlash=hitFlash;
      this.equipmentVisualRef=equipmentVisualRef;this.weaponVisualRef=weaponVisualRef;
      this.effectVisualRef=effectVisualRef;
      this.effectFamily=effectFamily==null?EffectFamily.NONE:effectFamily;
    }
  }

  private static final boolean CONTRACT_VALID=CharacterRendererAudit.passes();
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);

  public CharacterRenderer(){
    if(!CONTRACT_VALID)throw new IllegalStateException("CharacterRenderer contract audit failed: "+CharacterRendererAudit.summary());
  }

  public void draw(Canvas c,Pose pose){
    if(pose==null||pose.direction==null||pose.state==null)throw new IllegalArgumentException("Character pose requires direction and state");
    int frame=pose.state==State.WALK?((int)(pose.walkClock*8f)%4):0; // [B] prototype cadence
    float bob=(frame==1||frame==3)?-2f:0f;
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    float facingX=left?-1f:1f;
    float facingY=down?1f:-1f;
    float recoilX=pose.state==State.HIT?-facingX*3.5f:0f;
    float recoilY=pose.state==State.HIT?-facingY*1.5f:0f;
    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;

    // Shadow and body scale are presentation-only. pose.x/pose.y remain world/screen anchor coordinates.
    float shadowHalfWidth=13f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?1.35f:1f);
    float shadowHalfHeight=4f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?0.72f:1f);
    p.setColor(pose.hitFlash?0x99ff7766:0x66000000);
    c.drawOval(new RectF(pose.x-shadowHalfWidth,anchorY-shadowHalfHeight,
        pose.x+shadowHalfWidth,anchorY+shadowHalfHeight),p);

    c.save();
    c.translate(pose.x+recoilX,anchorY-(28f*PLAYER_RENDER_SCALE)+bob*PLAYER_RENDER_SCALE+recoilY);
    c.scale(PLAYER_RENDER_SCALE,PLAYER_RENDER_SCALE);
    c.translate(-8,0);
    if(pose.state==State.DEAD){
      c.rotate(left?-76f:76f,8f,27f);
      c.scale(1f,0.86f,8f,27f);
    }
    for(Layer layer:DRAW_ORDER)drawLayer(c,pose,layer,frame);
    c.restore();
  }

  private void drawLayer(Canvas c,Pose pose,Layer layer,int frame){
    switch(layer){
      case BODY:drawBody(c,pose,frame);break;
      case HAIR:drawHair(c,pose);break;
      case EQUIPMENT:drawEquipment(c,pose);break;
      case WEAPON:drawWeapon(c,pose);break;
      case EFFECT:drawEffect(c,pose);break;
    }
  }

  private void drawBody(Canvas c,Pose pose,int frame){
    int outline=0xff171313,skin=pose.hitFlash?0xffffe0d0:0xffffc68f,pants=0xff393a3a,shoe=0xff6a4526;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    int step=frame==1?1:frame==3?-1:0;
    int depthStep=down?step:-step;
    int kick=pose.effectFamily==EffectFamily.KICK&&phase(pose)>.22f&&phase(pose)<.78f?5:0; // [B] presentation only
    rect(c,outline,4+step-kick,19+depthStep,7+step,26+depthStep);rect(c,outline,10-step,19-depthStep,13-step+kick,26-depthStep);
    rect(c,pants,5+step-kick,19+depthStep,7+step,24+depthStep);rect(c,pants,10-step,19-depthStep,12-step+kick,24-depthStep);
    rect(c,shoe,4+step-kick,24+depthStep,7+step,27+depthStep);rect(c,shoe,10-step,24-depthStep,13-step+kick,27-depthStep);
    int armLift=pose.state==State.CAST?-7:pose.state==State.SKILL?-3:0;
    int swing=pose.state==State.WALK?step*2:0;
    rect(c,outline,1,11+armLift+swing,4,19+swing);rect(c,skin,2,12+armLift+swing,3,18+swing);
    rect(c,outline,13,11+armLift-swing,16,19-swing);rect(c,skin,14,12+armLift-swing,15,18-swing);
    rect(c,outline,3,2,14,12);rect(c,skin,4,3,13,11);
    if(pose.state==State.DEAD){p.setColor(0x44000000);c.drawRect(1,11,16,24,p);}
  }

  private void drawHair(Canvas c,Pose pose){
    int hair=0xff3b251b;
    rect(c,hair,3,1,14,6);rect(c,hair,2,3,5,9);rect(c,hair,12,3,15,8);rect(c,hair,5,0,12,3);
    boolean back=pose.direction==Direction.NW||pose.direction==Direction.NE;
    if(!back){int eye=0xff252020;boolean left=pose.direction==Direction.SW;rect(c,eye,left?5:7,7,left?6:8,8);rect(c,eye,left?9:11,7,left?10:12,8);}
  }

  private void drawEquipment(Canvas c,Pose pose){
    // [B] procedural placeholder. visualRef is intentionally not resolved until authenticated crops exist.
    int shirt=pose.hitFlash?0xffffd0ca:0xffeee5d3;
    rect(c,0xff171313,3,10,14,21);rect(c,shirt,4,11,13,19);rect(c,0xff3c6382,4,16,13,20);rect(c,0xffc9b070,7,11,9,20);
  }

  private void drawWeapon(Canvas c,Pose pose){
    if(pose.state!=State.ATTACK||pose.effectFamily==EffectFamily.PUNCH)return;
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    float sx=left?-1f:1f,sy=down?1f:-1f;
    float q=phase(pose);
    float reach=7f+5f*(float)Math.sin(Math.PI*q);
    p.setColor(0xffd7d2c5);p.setStrokeWidth(2);
    float handX=left?2f:15f,handY=down?15f:12f;
    c.drawLine(handX,handY,handX+sx*reach,handY+sy*(5f+3f*q),p); // [B] directional generic weapon placeholder
  }

  /** Owns player-local prototype action effects. Combat semantics stay outside this renderer. */
  private void drawEffect(Canvas c,Pose pose){
    EffectFamily family=pose.hitFlash?EffectFamily.HIT:pose.effectFamily;
    if(family==EffectFamily.NONE)return;
    float q=phase(pose);
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    float sx=left?-1f:1f;
    float sy=down?1f:-1f;
    switch(family){
      case CAST:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaa78b9ff);
        c.drawCircle(8,-7,5+9*q,p);c.drawCircle(8,-7,12-4*q,p);p.setStyle(Paint.Style.FILL);break;
      case MAGIC:
        // [ADAPTED] MAGIC uses a focused cross/rune burst rather than CAST's concentric charge rings.
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);p.setColor(0xaa9d7cff);
        c.drawCircle(8,-7,4+7*q,p);
        c.drawLine(8,-16-(5*q),8,-1+(3*q),p);
        c.drawLine(-1+(4*q),-7,17-(4*q),-7,p);
        p.setStyle(Paint.Style.FILL);break;
      case THROW:
        p.setColor(0xffffd76b);c.drawCircle(8+sx*27.5f*q,13+sy*17.5f*q,2,p);break;
      case PUNCH:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaaffefb0);
        c.drawCircle(8+sx*(7f+8f*q),11+sy*(2f+3f*q),3f+3f*q,p);p.setStyle(Paint.Style.FILL);break;
      case KICK:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setColor(0xaaffefb0);
        c.drawArc(new RectF(8+sx*10-10,8,8+sx*10+10,28),15,145,false,p);p.setStyle(Paint.Style.FILL);break;
      case SKILL:
        // [ADAPTED] SKILL gets a broader directional crescent so ATTACK/SKILL/MAGIC read differently at a glance.
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.8f);p.setColor(0xaa7fffa8);
        float cx=8+sx*(8f+4f*q),cy=10+sy*4f;
        c.drawArc(new RectF(cx-12f,cy-10f,cx+12f,cy+10f),left?210f:-30f,150f,false,p);
        c.drawLine(8,11,cx+sx*9f,cy+sy*5f,p);
        p.setStyle(Paint.Style.FILL);break;
      case HIT:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaaff7755);
        c.drawCircle(8,8,5+7*q,p);
        c.drawLine(3,3,13,13,p);c.drawLine(13,3,3,13,p);
        p.setStyle(Paint.Style.FILL);break;
      default:break;
    }
  }

  private float phase(Pose pose){return pose.stateDuration<=0?0:Math.max(0,Math.min(1,pose.stateClock/pose.stateDuration));}
  private void rect(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}

  public boolean hasRequiredStateContract(){return CharacterRendererAudit.passes();}
  public String contractAuditSummary(){return CharacterRendererAudit.summary();}
  public boolean ownsPlayerLocalEffects(){return true;}
  public float playerRenderScale(){return PLAYER_RENDER_SCALE;}
}
