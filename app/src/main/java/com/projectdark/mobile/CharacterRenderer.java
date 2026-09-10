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
  public static final float PLAYER_RENDER_SCALE=1.35f;
  public static final float SHADOW_RENDER_SCALE=0.72f;

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

    // Shadow and body scale are presentation-only. pose.x/pose.y remain world/screen anchor coordinates.
    float shadowHalfWidth=13f*SHADOW_RENDER_SCALE;
    float shadowHalfHeight=4f*SHADOW_RENDER_SCALE;
    p.setColor(pose.hitFlash?0x99ff7766:0x66000000);
    c.drawOval(new RectF(pose.x-shadowHalfWidth,pose.y-shadowHalfHeight,
        pose.x+shadowHalfWidth,pose.y+shadowHalfHeight),p);

    c.save();
    c.translate(pose.x,pose.y-(28f*PLAYER_RENDER_SCALE)+bob*PLAYER_RENDER_SCALE);
    c.scale(PLAYER_RENDER_SCALE,PLAYER_RENDER_SCALE);
    c.translate(-8,0);
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
    int step=frame==1?1:frame==3?-1:0;
    int kick=pose.effectFamily==EffectFamily.KICK&&phase(pose)>.22f&&phase(pose)<.78f?5:0; // [B] presentation only
    rect(c,outline,4+step-kick,19,7+step,26);rect(c,outline,10-step,19,13-step+kick,26);
    rect(c,pants,5+step-kick,19,7+step,24);rect(c,pants,10-step,19,12-step+kick,24);
    rect(c,shoe,4+step-kick,24,7+step,27);rect(c,shoe,10-step,24,13-step+kick,27);
    int armLift=pose.state==State.CAST?-7:0;
    rect(c,outline,1,11+armLift,4,19);rect(c,skin,2,12+armLift,3,18);
    rect(c,outline,13,11+armLift,16,19);rect(c,skin,14,12+armLift,15,18);
    rect(c,outline,3,2,14,12);rect(c,skin,4,3,13,11);
    if(pose.state==State.DEAD){p.setColor(0x66000000);c.drawRect(1,11,16,24,p);}
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
    p.setColor(0xffd7d2c5);p.setStrokeWidth(2);
    c.drawLine(left?2:15,14,left?-7:23,7,p); // [B] generic weapon placeholder
  }

  /** Owns player-local prototype action effects. Combat semantics stay outside this renderer. */
  private void drawEffect(Canvas c,Pose pose){
    EffectFamily family=pose.hitFlash?EffectFamily.HIT:pose.effectFamily;
    if(family==EffectFamily.NONE)return;
    float q=phase(pose);
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    float sx=left?-1f:1f;
    float sy=(pose.direction==Direction.SW||pose.direction==Direction.SE)?1f:-1f;
    switch(family){
      case CAST:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaa78b9ff);
        c.drawCircle(8,-7,5+9*q,p);c.drawCircle(8,-7,12-4*q,p);p.setStyle(Paint.Style.FILL);break;
      case MAGIC:
        // [ADAPTED] Distinct presentation hook so MAGIC can be visually distinguished from generic CAST/SKILL.
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);p.setColor(0xaa9d7cff);
        c.drawCircle(8,-7,4+7*q,p);
        c.drawLine(8,-16-(5*q),8,-1+(3*q),p);
        c.drawLine(-1+(4*q),-7,17-(4*q),-7,p);
        p.setStyle(Paint.Style.FILL);break;
      case THROW:
        p.setColor(0xffffd76b);c.drawCircle(8+sx*27.5f*q,13+sy*17.5f*q,2,p);break;
      case PUNCH:
      case KICK:
      case SKILL:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(family==EffectFamily.SKILL?2.5f:1.5f);
        p.setColor(family==EffectFamily.SKILL?0xaa7fffa8:0xaaffefb0);
        c.drawArc(new RectF(8+sx*9-9,3,8+sx*9+9,21),20,130,false,p);p.setStyle(Paint.Style.FILL);break;
      case HIT:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaaff7755);
        c.drawCircle(8,8,5+7*q,p);p.setStyle(Paint.Style.FILL);break;
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
