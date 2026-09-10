package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * [O+B] Layered character presentation scaffold.
 * Base-avatar silhouette/proportion is now guided by Nexon's official character-creation screenshot,
 * while authenticated original per-frame sprite crops remain PENDING_CROP.
 */
public final class CharacterRenderer {
  public static final String EVIDENCE="O+B";
  public static final String ASSET_STATUS="PENDING_CROP";

  /** [ADAPTED] Presentation-only scale. Logical/world coordinates are intentionally unchanged. */
  public static final float PLAYER_RENDER_SCALE=0.92f;
  public static final float SHADOW_RENDER_SCALE=0.58f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;

  /** Official-guide-backed base-avatar proportion contract; not an extracted sprite frame. */
  public static final String BASE_AVATAR_PROPORTION_SOURCE=CharacterVisualSourceManifest.CHARACTER_CREATION_SCREENSHOT_URL;

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
    float recoilX=pose.state==State.HIT?-facingX*3.5f:0f; // [ADAPTED] presentation-only recoil.
    float recoilY=pose.state==State.HIT?-facingY*1.5f:0f;

    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;
    float shadowHalfWidth=13f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?1.35f:1f);
    float shadowHalfHeight=4f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?0.72f:1f);
    p.setColor(pose.hitFlash?0x99ff7766:0x66000000);
    c.drawOval(new RectF(pose.x-shadowHalfWidth,anchorY-shadowHalfHeight,
        pose.x+shadowHalfWidth,anchorY+shadowHalfHeight),p);

    c.save();
    c.translate(pose.x+recoilX,anchorY-(30f*PLAYER_RENDER_SCALE)+bob*PLAYER_RENDER_SCALE+recoilY);
    c.scale(PLAYER_RENDER_SCALE,PLAYER_RENDER_SCALE);
    c.translate(-8,0);
    if(pose.state==State.DEAD){
      c.rotate(left?-76f:76f,8f,29f); // [ADAPTED]
      c.scale(1f,0.86f,8f,29f);
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
    // [O+B] Proportion follows the official creation-screen avatar: larger head, narrow torso,
    // slim limbs and short base lower garment. Pixel geometry remains adapted until crop extraction.
    int outline=0xff2a1b17;
    int skin=pose.hitFlash?0xffffe0d0:0xffefb58b;
    int baseGarment=0xff5f4439;
    int foot=0xff7f5b44;
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    int step=frame==1?1:frame==3?-1:0;
    int depthStep=down?step:-step;
    int kick=pose.effectFamily==EffectFamily.KICK&&phase(pose)>.22f&&phase(pose)<.78f?5:0;

    // Legs: thinner than the old blocky placeholder.
    rect(c,outline,5+step-kick,20+depthStep,7+step,29+depthStep);
    rect(c,outline,10-step,20-depthStep,12-step+kick,29-depthStep);
    rect(c,skin,5.5f+step-kick,20+depthStep,6.5f+step,27+depthStep);
    rect(c,skin,10.5f-step,20-depthStep,11.5f-step+kick,27-depthStep);
    rect(c,foot,4.5f+step-kick,27+depthStep,7.5f+step,30+depthStep);
    rect(c,foot,9.5f-step,27-depthStep,12.5f-step+kick,30-depthStep);

    // Base commoner/creation-avatar lower garment. Equipment layer can replace/cover this later.
    rect(c,outline,4,15,13,22);
    rect(c,baseGarment,5,16,12,21);

    int walkSwing=pose.state==State.WALK?step*2:0;
    int armLift=pose.state==State.CAST?-7:pose.state==State.SKILL?-3:0;
    int nearSwing=left?walkSwing:-walkSwing;
    int farSwing=-nearSwing;
    int verticalBias=down?1:-1;

    // Narrow bare torso and slim arms match the official creation-screen silhouette more closely.
    rect(c,outline,5,8,12,17);
    rect(c,skin,6,9,11,16);
    rect(c,outline,2,10+armLift+farSwing*verticalBias,4,20+farSwing*verticalBias);
    rect(c,skin,2.5f,11+armLift+farSwing*verticalBias,3.5f,19+farSwing*verticalBias);
    rect(c,outline,13,10+armLift+nearSwing*verticalBias,15,20+nearSwing*verticalBias);
    rect(c,skin,13.5f,11+armLift+nearSwing*verticalBias,14.5f,19+nearSwing*verticalBias);

    // Larger head-to-body ratio observed in the official in-game creation avatar.
    p.setColor(outline);c.drawOval(new RectF(3.5f,0f,13.5f,10f),p);
    p.setColor(skin);c.drawOval(new RectF(4.5f,1f,12.5f,9f),p);

    if(pose.state==State.DEAD){p.setColor(0x44000000);c.drawRect(2,8,15,26,p);}
  }

  private void drawHair(Canvas c,Pose pose){
    // [ADAPTED] Shape vocabulary is based on the official creation UI hair silhouettes.
    // Colour is a prototype sample only; the guide proves hair-colour selection exists but not a canonical default.
    int hair=0xff5b2a72;
    rect(c,hair,3,0,14,4);
    rect(c,hair,2,2,5,8);
    rect(c,hair,12,2,15,7);
    rect(c,hair,5,-1,12,2);
    rect(c,hair,4,4,6,7);
    boolean back=pose.direction==Direction.NW||pose.direction==Direction.NE;
    if(!back){
      int eye=0xff2a2020;boolean left=pose.direction==Direction.SW;
      rect(c,eye,left?5:7,6,left?6:8,7);
      rect(c,eye,left?9:11,6,left?10:12,7);
    }
  }

  private void drawEquipment(Canvas c,Pose pose){
    // The official creation screen shows a bare/base avatar before equipment presentation.
    // Do not paint the old invented shirt when no equipment item is actually bound.
    if(pose.equipmentVisualRef==null||ASSET_STATUS.equals(pose.equipmentVisualRef))return;

    // [ADAPTED] Equipped item IDs are known, authenticated item sprite crops are not.
    int shirt=pose.hitFlash?0xffffd0ca:0xffd8c7aa;
    rect(c,0xff2a1b17,4,8,13,18);
    rect(c,shirt,5,9,12,16);
    rect(c,0xff4e657c,5,14,12,18);
  }

  private void drawWeapon(Canvas c,Pose pose){
    if(pose.state!=State.ATTACK||pose.effectFamily==EffectFamily.PUNCH)return;
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    float sx=left?-1f:1f,sy=down?1f:-1f;
    p.setColor(0xffd7d2c5);p.setStrokeWidth(2);
    float handX=left?2f:15f;
    float handY=down?15f:12f;
    c.drawLine(handX,handY,handX+sx*9f,handY+sy*7f,p);
  }

  /** Owns player-local prototype action effects. Combat semantics stay outside this renderer. */
  private void drawEffect(Canvas c,Pose pose){
    EffectFamily family=pose.hitFlash?EffectFamily.HIT:pose.effectFamily;
    float q=phase(pose);
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    float sx=left?-1f:1f;
    float sy=(pose.direction==Direction.SW||pose.direction==Direction.SE)?1f:-1f;

    if(family==EffectFamily.NONE&&pose.state==State.ATTACK){
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setColor(0xaad7d2c5);
      float leadX=8+sx*(10+5*q);
      float leadY=12+sy*(4+3*q);
      float start=left?(sy>0?35f:205f):(sy>0?25f:215f);
      c.drawArc(new RectF(leadX-12,leadY-11,leadX+12,leadY+11),start,115,false,p);
      p.setStyle(Paint.Style.FILL);
      return;
    }
    if(family==EffectFamily.NONE)return;

    switch(family){
      case CAST:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaa78b9ff);
        c.drawCircle(8,-7,5+9*q,p);c.drawCircle(8,-7,12-4*q,p);p.setStyle(Paint.Style.FILL);break;
      case MAGIC:
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
        float effectCenterX=8+sx*9f;
        float effectCenterY=12+sy*3f;
        c.drawArc(new RectF(effectCenterX-9,effectCenterY-9,effectCenterX+9,effectCenterY+9),
            left?(sy>0?25f:205f):(sy>0?15f:215f),130,false,p);
        p.setStyle(Paint.Style.FILL);break;
      case HIT:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaaff7755);
        c.drawCircle(8,8,5+7*q,p);
        c.drawLine(8-sx*2f,8-sy*2f,8+sx*8f,8+sy*8f,p);
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
