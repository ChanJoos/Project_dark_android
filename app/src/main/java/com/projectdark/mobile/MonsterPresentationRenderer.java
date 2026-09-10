package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * [B] Presentation-only monster renderer contract.
 * Authenticated original monster sprites remain PENDING_CROP.
 * Runtime coordinates and combat semantics stay outside this renderer.
 */
public final class MonsterPresentationRenderer {
  public static final String EVIDENCE="B";
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final float MONSTER_RENDER_SCALE=0.86f; // [ADAPTED]
  public static final float SHADOW_RENDER_SCALE=0.60f; // [ADAPTED]
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;

  public enum Direction { NW, NE, SW, SE }
  public enum State { IDLE, WALK, ATTACK, HIT, DEAD }

  public static final class MonsterPose {
    public final float x,y,stateClock,stateDuration,hpRatio,attackTelegraphPhase;
    public final int lastDamage;
    public final Direction direction;
    public final State state;
    public final boolean selected,hitFlash,showDamage;
    public final String bodyVisualRef,effectVisualRef;

    public MonsterPose(float x,float y,Direction direction,State state,float stateClock,float stateDuration,
        float hpRatio,float attackTelegraphPhase,boolean selected,boolean hitFlash,boolean showDamage,
        int lastDamage,String bodyVisualRef,String effectVisualRef){
      this.x=x;this.y=y;
      this.direction=direction==null?Direction.SW:direction;
      this.state=state==null?State.IDLE:state;
      this.stateClock=stateClock;this.stateDuration=stateDuration;
      this.hpRatio=Math.max(0f,Math.min(1f,hpRatio));
      this.attackTelegraphPhase=Math.max(0f,Math.min(1f,attackTelegraphPhase));
      this.selected=selected;this.hitFlash=hitFlash;this.showDamage=showDamage;
      this.lastDamage=lastDamage;this.bodyVisualRef=bodyVisualRef;this.effectVisualRef=effectVisualRef;
    }
  }

  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);

  public void draw(Canvas c,MonsterPose pose){
    if(c==null||pose==null)return;
    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;
    float shadowHalfWidth=15f*SHADOW_RENDER_SCALE;
    float shadowHalfHeight=4.5f*SHADOW_RENDER_SCALE;
    p.setColor(0x66000000);
    c.drawOval(new RectF(pose.x-shadowHalfWidth,anchorY-shadowHalfHeight,
        pose.x+shadowHalfWidth,anchorY+shadowHalfHeight),p);

    c.save();
    c.translate(pose.x,anchorY-31f*MONSTER_RENDER_SCALE);
    c.scale(MONSTER_RENDER_SCALE,MONSTER_RENDER_SCALE);
    drawPrototypeMonster(c,pose);
    c.restore();

    drawSelection(c,pose,anchorY);
    drawAttackTelegraph(c,pose,anchorY);
    drawHp(c,pose,anchorY);
    drawDamage(c,pose,anchorY);
  }

  private void drawPrototypeMonster(Canvas c,MonsterPose pose){
    if(pose.state==State.DEAD)return;
    p.setColor(pose.hitFlash?0xffd9e8d7:0xff6a7c69);
    c.drawOval(new RectF(-13f,0f,13f,26f),p);
    p.setColor(0xffd8c27b);c.drawCircle(-5f,11f,2f,p);c.drawCircle(5f,11f,2f,p);
    if(pose.state==State.ATTACK){
      float q=phase(pose);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f+q);
      p.setColor(0xaaff8866);c.drawArc(new RectF(-18f,-3f,18f,30f),15f,145f,false,p);
      p.setStyle(Paint.Style.FILL);
    }
  }

  private void drawSelection(Canvas c,MonsterPose pose,float anchorY){
    if(!pose.selected)return;
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.6f);p.setColor(0xffffd86b);
    c.drawOval(new RectF(pose.x-16f,anchorY-7f,pose.x+16f,anchorY+7f),p);p.setStyle(Paint.Style.FILL);
  }

  private void drawAttackTelegraph(Canvas c,MonsterPose pose,float anchorY){
    if(pose.attackTelegraphPhase<=0f||pose.state==State.DEAD)return;
    float q=pose.attackTelegraphPhase;
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f+2f*q);p.setColor(0xaaff7755);
    c.drawCircle(pose.x,anchorY-17f,13f+7f*q,p);p.setStyle(Paint.Style.FILL);
  }

  private void drawHp(Canvas c,MonsterPose pose,float anchorY){
    if(pose.state==State.DEAD)return;
    float l=pose.x-15f,r=pose.x+15f,t=anchorY-36f,b=anchorY-32f;
    p.setColor(0xb51a1714);c.drawRoundRect(new RectF(l,t,r,b),3f,3f,p);
    p.setColor(0xffd63442);c.drawRoundRect(new RectF(l,t,l+(r-l)*pose.hpRatio,b),3f,3f,p);
  }

  private void drawDamage(Canvas c,MonsterPose pose,float anchorY){
    if(!pose.showDamage||pose.lastDamage<=0)return;
    float q=phase(pose);String text="-"+pose.lastDamage;
    p.setTextSize(10f);p.setColor(0xffffdc72);float tw=p.measureText(text);
    c.drawText(text,pose.x-tw/2f,anchorY-40f-(12f*q),p);
  }

  private float phase(MonsterPose pose){return pose.stateDuration<=0f?0f:Math.max(0f,Math.min(1f,pose.stateClock/pose.stateDuration));}
}
