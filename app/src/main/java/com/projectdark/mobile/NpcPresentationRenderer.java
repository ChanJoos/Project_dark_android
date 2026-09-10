package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * [B] Presentation-only NPC renderer contract.
 * Authenticated original NPC sprites remain PENDING_CROP.
 * Logical/world coordinates are supplied by the caller and are never mutated here.
 */
public final class NpcPresentationRenderer {
  public static final String EVIDENCE="B";
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final float NPC_RENDER_SCALE=0.88f; // [ADAPTED]
  public static final float SHADOW_RENDER_SCALE=0.56f; // [ADAPTED]
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;

  public enum Direction { NW, NE, SW, SE }
  public enum State { IDLE, WALK, CAST, HIT, DEAD }

  public static final class NpcPose {
    public final float x,y,stateClock,stateDuration;
    public final String name,bodyVisualRef,hairVisualRef,equipmentVisualRef,effectVisualRef;
    public final Direction direction;
    public final State state;
    public final boolean selected,hitFlash;

    public NpcPose(float x,float y,String name,Direction direction,State state,float stateClock,
        float stateDuration,boolean selected,boolean hitFlash,String bodyVisualRef,
        String hairVisualRef,String equipmentVisualRef,String effectVisualRef){
      this.x=x;this.y=y;this.name=name==null?"":name;
      this.direction=direction==null?Direction.SW:direction;
      this.state=state==null?State.IDLE:state;
      this.stateClock=stateClock;this.stateDuration=stateDuration;
      this.selected=selected;this.hitFlash=hitFlash;
      this.bodyVisualRef=bodyVisualRef;this.hairVisualRef=hairVisualRef;
      this.equipmentVisualRef=equipmentVisualRef;this.effectVisualRef=effectVisualRef;
    }
  }

  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);

  public void draw(Canvas c,NpcPose pose){
    if(c==null||pose==null)return;
    float shadowHalfWidth=10f*SHADOW_RENDER_SCALE;
    float shadowHalfHeight=3.5f*SHADOW_RENDER_SCALE;
    p.setColor(0x66000000);
    c.drawOval(new RectF(pose.x-shadowHalfWidth,pose.y-shadowHalfHeight,
        pose.x+shadowHalfWidth,pose.y+shadowHalfHeight),p);

    c.save();
    c.translate(pose.x,pose.y-35f*NPC_RENDER_SCALE);
    c.scale(NPC_RENDER_SCALE,NPC_RENDER_SCALE);
    c.translate(-7f,0f);
    drawPrototypeNpc(c,pose);
    c.restore();

    if(pose.selected){
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xfff0d17a);
      c.drawCircle(pose.x,pose.y-20f,14f,p);p.setStyle(Paint.Style.FILL);
    }
    if(!pose.name.isEmpty()){
      p.setTextSize(8f);p.setColor(0xfff3e1ae);
      float tw=p.measureText(pose.name);c.drawText(pose.name,pose.x-tw/2f,pose.y-42f,p);
    }
  }

  private void drawPrototypeNpc(Canvas c,NpcPose pose){
    int skin=pose.hitFlash?0xffffd8cf:0xffcab58b;
    p.setColor(skin);c.drawCircle(7f,4f,6f,p);
    p.setColor(0xff6c5946);c.drawRect(1f,10f,13f,28f,p);
    p.setColor(0xffb58f58);c.drawRect(2f,27f,6f,35f,p);c.drawRect(8f,27f,12f,35f,p);
    if(pose.state==State.CAST){
      float q=phase(pose);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaa78b9ff);
      c.drawCircle(7f,-4f,5f+6f*q,p);p.setStyle(Paint.Style.FILL);
    }
    if(pose.state==State.DEAD){p.setColor(0x66000000);c.drawRect(0f,8f,14f,34f,p);}
  }

  private float phase(NpcPose pose){return pose.stateDuration<=0f?0f:Math.max(0f,Math.min(1f,pose.stateClock/pose.stateDuration));}
}
