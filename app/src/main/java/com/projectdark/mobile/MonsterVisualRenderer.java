package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * [ADAPTED] Live monster presentation used until verified source monster sprites are available.
 * Logical position, facing, attack state, HP and selection remain runtime-owned; this class is
 * presentation-only and can be replaced by source sprite rendering without touching combat AI.
 */
public final class MonsterVisualRenderer {
  public static final String EVIDENCE="ADAPTED_FOOT_LOCKED_MONSTER_ATTACK_POSE";
  public static final String SOURCE_STATUS="SOURCE_SPRITE_PENDING";
  private final Paint p=new Paint();

  public MonsterVisualRenderer(){p.setAntiAlias(false);p.setDither(false);}

  public void draw(Canvas c,RuntimeState.Monster m,boolean selected){
    if(c==null||m==null||!m.alive)return;
    CharacterRenderer.Direction facing=m.visualFacing.presentation();
    boolean west=facing==CharacterRenderer.Direction.NW||facing==CharacterRenderer.Direction.SW;
    boolean north=facing==CharacterRenderer.Direction.NW||facing==CharacterRenderer.Direction.NE;
    float sx=west?-1f:1f;
    float attack=m.attackPrimed?1f-Math.min(1f,m.attackWindup/.24f):0f;

    // Combat reference: keep the logical foot/ground contact fixed. Attack readability must come
    // from silhouette/limb motion and contact feedback, not whole-creature world translation.
    float x=m.x,y=m.y;
    float bodyLean=attack*1.25f*sx;

    // Ground contact and target footprint are deliberately isometric, not circular body blobs.
    p.setStyle(Paint.Style.FILL);p.setColor(0x66000000);
    c.drawOval(new RectF(x-16f,y-4f,x+16f,y+4f),p);
    if(selected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);p.setColor(0xffffd86b);c.drawOval(new RectF(x-21f,y-8f,x+21f,y+8f),p);p.setStyle(Paint.Style.FILL);}

    // Feet remain foot-locked through attack. Direction is expressed by depth ordering only.
    int outline=0xff243126,deep=0xff354b37,body=m.hitFlash>0f?0xffd7ead6:0xff58745b,light=m.hitFlash>0f?0xffeff8ed:0xff78977a;
    int skin=0xffb8a36e,eye=0xfff0d47b,dark=0xff182019;
    float nearLegX=x+(west?-6f:6f),farLegX=x+(west?5f:-5f);
    p.setColor(outline);c.drawRect(farLegX-4,y-13,farLegX+3,y-3,p);c.drawRect(nearLegX-4,y-14,nearLegX+4,y-2,p);
    p.setColor(deep);c.drawRect(farLegX-2,y-12,farLegX+2,y-4,p);
    p.setColor(body);c.drawRect(nearLegX-2,y-13,nearLegX+3,y-3,p);

    // Torso gets only a subtle local lean; the world-space foot anchor does not move.
    float torsoX=x+bodyLean;
    p.setColor(outline);c.drawOval(new RectF(torsoX-13,y-34,torsoX+13,y-10),p);
    p.setColor(body);c.drawOval(new RectF(torsoX-11,y-33,torsoX+11,y-12),p);
    p.setColor(light);c.drawOval(new RectF(torsoX+(west?-7:-2),y-31,torsoX+(west?2:7),y-17),p);
    p.setColor(deep);c.drawRect(torsoX+(west?4:-9),y-28,torsoX+(west?9:-4),y-14,p);

    // Near arm supplies most of the attack pose delta while feet/torso remain spatially stable.
    float armForward=attack*6f;
    float nearArmX=torsoX+(west?-12f:12f)+sx*armForward;
    float farArmX=torsoX+(west?10f:-10f);
    p.setColor(outline);c.drawOval(new RectF(farArmX-4,y-29,farArmX+4,y-15),p);c.drawOval(new RectF(nearArmX-5,y-30,nearArmX+5,y-14),p);
    p.setColor(body);c.drawOval(new RectF(farArmX-2,y-27,farArmX+3,y-17),p);p.setColor(light);c.drawOval(new RectF(nearArmX-3,y-28,nearArmX+3,y-16),p);

    // Head follows only the local silhouette lean, never the world position.
    float headX=torsoX+bodyLean*.25f,headY=y-38f;
    p.setColor(outline);c.drawOval(new RectF(headX-10,headY-9,headX+10,headY+8),p);
    p.setColor(body);c.drawOval(new RectF(headX-8,headY-8,headX+8,headY+6),p);
    p.setColor(deep);
    if(west){c.drawRect(headX-13,headY-5,headX-8,headY,p);c.drawRect(headX+7,headY-3,headX+11,headY+1,p);}else{c.drawRect(headX+8,headY-5,headX+13,headY,p);c.drawRect(headX-11,headY-3,headX-7,headY+1,p);}
    p.setColor(light);c.drawRect(headX+(west?-6:1),headY-6,headX+(west?1:6),headY-2,p);

    // Directional face. NW/NE sit higher; SW/SE lower and therefore visibly face the player plane.
    float eyeY=headY+(north?-2f:1f),eyeX=headX+(west?-2.5f:2.5f);
    p.setColor(dark);c.drawRect(eyeX-4,eyeY-1,eyeX-1,eyeY+2,p);c.drawRect(eyeX+2,eyeY-1,eyeX+5,eyeY+2,p);
    p.setColor(eye);c.drawRect(eyeX-3,eyeY,eyeX-2,eyeY+1,p);c.drawRect(eyeX+3,eyeY,eyeX+4,eyeY+1,p);
    p.setColor(skin);c.drawRect(eyeX-1,eyeY+4,eyeX+2,eyeY+6,p);

    // Attack telegraph stays attached to the pose; no detached movement or world-space shove.
    if(m.attackPrimed){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f+attack*1.5f);p.setColor(0xc8ff7458);c.drawArc(new RectF(torsoX-17,y-42,torsoX+17,y-8),200,140,false,p);p.setStyle(Paint.Style.FILL);}

    // HP and damage feedback remain locked to the logical entity anchor.
    drawBar(c,x-20,y-55,x+20,y-50,m.hp/(float)m.maxHp);
    if(m.damagePopupClock>0){p.setTextSize(12);p.setColor(0xffffdc72);String d="-"+m.lastDamage;float tw=p.measureText(d);c.drawText(d,x-tw/2,y-61-(.65f-m.damagePopupClock)*20,p);}
  }

  private void drawBar(Canvas c,float l,float t,float r,float b,float ratio){ratio=Math.max(0f,Math.min(1f,ratio));p.setStyle(Paint.Style.FILL);p.setColor(0xcc171416);c.drawRect(l,t,r,b,p);p.setColor(0xffd63442);c.drawRect(l,t,l+(r-l)*ratio,b,p);}
}
