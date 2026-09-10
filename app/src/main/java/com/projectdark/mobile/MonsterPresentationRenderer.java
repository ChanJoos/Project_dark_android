package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * [B] Presentation-only monster renderer contract.
 * Authenticated original monster sprites remain PENDING_CROP.
 * Combat/AI semantics stay outside this renderer.
 */
public final class MonsterPresentationRenderer {
  public static final String EVIDENCE="B";
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final float MONSTER_RENDER_SCALE=0.84f; // [ADAPTED]
  public static final float SHADOW_RENDER_SCALE=0.60f; // [ADAPTED]
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;

  public enum Direction { NW, NE, SW, SE }
  public enum State { IDLE, WALK, ATTACK, HIT, DEAD }
  public enum FeedbackType { NONE, WINDUP, HIT, CRIT, MISS }

  public static final class MonsterPose {
    public final float x,y,stateClock,stateDuration,hpRatio;
    public final int lastDamage;
    public final String name,bodyVisualRef,effectVisualRef;
    public final Direction direction;
    public final State state;
    public final FeedbackType feedbackType;
    public final boolean selected;

    public MonsterPose(float x,float y,String name,Direction direction,State state,float stateClock,
        float stateDuration,float hpRatio,int lastDamage,FeedbackType feedbackType,boolean selected,
        String bodyVisualRef,String effectVisualRef){
      this.x=x;this.y=y;this.name=name==null?"":name;
      this.direction=direction==null?Direction.SW:direction;
      this.state=state==null?State.IDLE:state;
      this.stateClock=stateClock;this.stateDuration=stateDuration;
      this.hpRatio=Math.max(0f,Math.min(1f,hpRatio));this.lastDamage=lastDamage;
      this.feedbackType=feedbackType==null?FeedbackType.NONE:feedbackType;
      this.selected=selected;this.bodyVisualRef=bodyVisualRef;this.effectVisualRef=effectVisualRef;
    }
  }

  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);

  public void draw(Canvas c,MonsterPose pose){
    if(c==null||pose==null)return;
    float shadowHalfWidth=13f*SHADOW_RENDER_SCALE;
    float shadowHalfHeight=4f*SHADOW_RENDER_SCALE;
    p.setColor(0x66000000);
    c.drawOval(new RectF(pose.x-shadowHalfWidth,pose.y-shadowHalfHeight,
        pose.x+shadowHalfWidth,pose.y+shadowHalfHeight),p);

    c.save();
    c.translate(pose.x,pose.y-30f*MONSTER_RENDER_SCALE);
    c.scale(MONSTER_RENDER_SCALE,MONSTER_RENDER_SCALE);
    c.translate(-13f,0f);
    drawPrototypeMonster(c,pose);
    c.restore();

    drawHp(c,pose);
    drawFeedback(c,pose);
    if(pose.selected){
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xffffd86b);
      c.drawOval(new RectF(pose.x-17f,pose.y-7f,pose.x+17f,pose.y+7f),p);
      p.setStyle(Paint.Style.FILL);
    }
  }

  private void drawPrototypeMonster(Canvas c,MonsterPose pose){
    int body=pose.state==State.HIT?0xffd9e8d7:0xff6a7c69;
    p.setColor(body);c.drawOval(new RectF(0f,0f,26f,26f),p);
    p.setColor(0xffd8c27b);c.drawCircle(8f,11f,2f,p);c.drawCircle(18f,11f,2f,p);
    if(pose.state==State.DEAD){p.setColor(0x77000000);c.drawOval(new RectF(0f,0f,26f,26f),p);}
  }

  private void drawHp(Canvas c,MonsterPose pose){
    float l=pose.x-15f,r=pose.x+15f,t=pose.y-36f,b=pose.y-32f;
    p.setColor(0xb51a1714);c.drawRoundRect(new RectF(l,t,r,b),4f,4f,p);
    p.setColor(0xffd63442);c.drawRoundRect(new RectF(l,t,l+(r-l)*pose.hpRatio,b),4f,4f,p);
  }

  private void drawFeedback(Canvas c,MonsterPose pose){
    float q=phase(pose);
    switch(pose.feedbackType){
      case WINDUP:
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f+1.5f*q);p.setColor(0xaaff7755);
        c.drawCircle(pose.x,pose.y-18f,14f+7f*q,p);p.setStyle(Paint.Style.FILL);break;
      case HIT:
      case CRIT:
        if(pose.lastDamage>0){
          p.setTextSize(pose.feedbackType==FeedbackType.CRIT?13f:11f);
          p.setColor(pose.feedbackType==FeedbackType.CRIT?0xffff9f43:0xffffdc72);
          String d="-"+pose.lastDamage;float tw=p.measureText(d);
          c.drawText(d,pose.x-tw/2f,pose.y-43f-14f*q,p);
        }
        break;
      case MISS:
        p.setTextSize(10f);p.setColor(0xffd9e8ff);
        String miss="MISS";c.drawText(miss,pose.x-p.measureText(miss)/2f,pose.y-43f-10f*q,p);break;
      default:break;
    }
  }

  private float phase(MonsterPose pose){return pose.stateDuration<=0f?0f:Math.max(0f,Math.min(1f,pose.stateClock/pose.stateDuration));}
}
