package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * [B]/[ADAPTED] Renderer-owned NPC/monster presentation seam.
 * Verified original directional sprites remain PENDING_CROP. Presentation scale/anchor stays
 * independent from RuntimeState logical positions and collision radii.
 */
public final class WorldEntityPresentationRenderer {
  public static final String EVIDENCE="B+ADAPTED";
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final float NPC_RENDER_SCALE=0.84f;
  public static final float MONSTER_RENDER_SCALE=0.88f;
  public static final float NPC_SHADOW_SCALE=0.54f;
  public static final float MONSTER_SHADOW_SCALE=0.62f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;

  public enum Kind { NPC, MONSTER }

  /** Immutable renderer input; gameplay semantics stay outside this layer. */
  public static final class Pose {
    public final Kind kind;
    public final float x,y,walkClock,stateClock,stateDuration;
    public final CharacterRenderer.Direction direction;
    public final CharacterRenderer.State state;
    public final CharacterRenderer.EffectFamily effectFamily;
    public final boolean hitFlash,selected;
    public final String visualRef,effectVisualRef;
    public final DirectionalVisualBinding visualBinding;

    public Pose(Kind kind,float x,float y,CharacterRenderer.Direction direction,
        CharacterRenderer.State state,float walkClock,float stateClock,float stateDuration,
        CharacterRenderer.EffectFamily effectFamily,boolean hitFlash,boolean selected,
        String visualRef,String effectVisualRef){
      this(kind,x,y,direction,state,walkClock,stateClock,stateDuration,effectFamily,hitFlash,selected,
          visualRef,effectVisualRef,null);
    }

    public Pose(Kind kind,float x,float y,CharacterRenderer.Direction direction,
        CharacterRenderer.State state,float walkClock,float stateClock,float stateDuration,
        CharacterRenderer.EffectFamily effectFamily,boolean hitFlash,boolean selected,
        String visualRef,String effectVisualRef,DirectionalVisualBinding visualBinding){
      this.kind=kind;this.x=x;this.y=y;this.direction=direction;this.state=state;
      this.walkClock=walkClock;this.stateClock=stateClock;this.stateDuration=stateDuration;
      this.effectFamily=effectFamily==null?CharacterRenderer.EffectFamily.NONE:effectFamily;
      this.hitFlash=hitFlash;this.selected=selected;this.visualRef=visualRef;
      this.effectVisualRef=effectVisualRef;this.visualBinding=visualBinding;
    }
  }

  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);

  public WorldEntityPresentationRenderer(){
    if(!WorldEntityPresentationAudit.passes())throw new IllegalStateException(
        "World entity presentation contract audit failed: "+WorldEntityPresentationAudit.summary());
  }

  public void draw(Canvas c,Pose pose){
    if(c==null||pose==null||pose.kind==null||pose.direction==null||pose.state==null)
      throw new IllegalArgumentException("Entity pose requires kind, direction and state");

    float actorScale=pose.kind==Kind.NPC?NPC_RENDER_SCALE:MONSTER_RENDER_SCALE;
    float shadowScale=pose.kind==Kind.NPC?NPC_SHADOW_SCALE:MONSTER_SHADOW_SCALE;
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);
    float sx=left?-1f:1f,sy=down?1f:-1f;
    int frame=pose.state==CharacterRenderer.State.WALK?((int)(pose.walkClock*7f)%4):0; // [B]
    float walkBob=(frame==1||frame==3)?-1.2f:0f;
    float idleWave=pose.state==CharacterRenderer.State.IDLE?(float)Math.sin(pose.stateClock*4.2f):0f; // [B]
    float idleBob=idleWave*(pose.kind==Kind.NPC?.55f:.8f);
    float idleSway=idleWave*(pose.kind==Kind.NPC?.45f:.7f)*sx;
    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;
    float recoilX=pose.state==CharacterRenderer.State.HIT?-sx*3f:0f;
    float recoilY=pose.state==CharacterRenderer.State.HIT?-sy*1.2f:0f;

    float shadowW=(pose.kind==Kind.NPC?12f:15f)*shadowScale;
    float shadowH=(pose.kind==Kind.NPC?3.6f:4.4f)*shadowScale;
    if(pose.state==CharacterRenderer.State.DEAD){shadowW*=1.28f;shadowH*=.72f;}
    if(pose.state==CharacterRenderer.State.IDLE){shadowW*=1f+.025f*idleWave;shadowH*=1f-.018f*idleWave;}
    p.setStyle(Paint.Style.FILL);
    p.setColor(pose.hitFlash?0x88ff7755:0x55000000);
    c.drawOval(new RectF(pose.x-shadowW,anchorY-shadowH,pose.x+shadowW,anchorY+shadowH),p);

    if(pose.selected){
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaaffdd66);
      c.drawOval(new RectF(pose.x-shadowW-3f,anchorY-shadowH-2f,pose.x+shadowW+3f,anchorY+shadowH+2f),p);
      p.setStyle(Paint.Style.FILL);
    }

    c.save();
    c.translate(pose.x+recoilX+idleSway,
        anchorY-(pose.kind==Kind.NPC?28f:25f)*actorScale+(walkBob+idleBob)*actorScale+recoilY);
    c.scale(actorScale,actorScale);
    c.translate(-8f,0f);
    if(pose.state==CharacterRenderer.State.DEAD){
      c.rotate(left?-68f:68f,8f,pose.kind==Kind.NPC?27f:24f);
      c.scale(1f,.84f,8f,pose.kind==Kind.NPC?27f:24f);
    }
    if(pose.kind==Kind.NPC)drawNpc(c,pose,frame,idleWave);else drawMonster(c,pose,frame,idleWave);
    drawEffect(c,pose);
    c.restore();
  }

  private void drawNpc(Canvas c,Pose pose,int frame,float idleWave){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);
    float side=left?-1f:1f;
    int step=pose.state==CharacterRenderer.State.WALK?(frame==1?1:frame==3?-1:0):0;
    int depth=down?step:-step;
    float breathe=pose.state==CharacterRenderer.State.IDLE?idleWave*.35f:0f;
    float shift=side*1.35f;
    int outline=0xff201c1a,skin=pose.hitFlash?0xffffddd0:0xffffc99c,cloth=0xff8b7763,dark=0xff4f443a;

    rect(c,outline,left?10f:5f,19-depth,left?12f:7f,28-depth);
    rect(c,dark,left?10.4f:5.4f,20-depth,left?11.6f:6.6f,27-depth);
    rect(c,outline,left?5f:10f,19+depth,left?7.4f:12.4f,28+depth);
    rect(c,dark,left?5.4f:10.4f,20+depth,left?7f:12f,27+depth);

    rect(c,outline,4.6f+shift,9f-breathe,12.4f+shift,21f);
    rect(c,cloth,5.5f+shift,10f-breathe,11.5f+shift,20f);
    float headShift=side*1.8f;
    p.setColor(outline);c.drawOval(new RectF(4.2f+headShift,-breathe,13f+headShift,10f-breathe),p);
    p.setColor(skin);c.drawOval(new RectF(5f+headShift,1f-breathe,12.2f+headShift,9f-breathe),p);
    p.setColor(0xff3c2a21);c.drawRect(3.5f+headShift,-breathe,13.4f+headShift,4.5f-breathe,p);
    if(down){float eyeX=left?6f:11f;rect(c,0xff27211e,eyeX+headShift,6f-breathe,eyeX+1f+headShift,7f-breathe);}
  }

  private void drawMonster(Canvas c,Pose pose,int frame,float idleWave){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);
    float side=left?-1f:1f;
    int step=pose.state==CharacterRenderer.State.WALK?(frame==1?1:frame==3?-1:0):0;
    int depth=down?step:-step;
    float q=phase(pose);
    float attackLunge=pose.state==CharacterRenderer.State.ATTACK?(q<.5f?q*2f:(1f-q)*2f)*3.5f:0f;
    float breathe=pose.state==CharacterRenderer.State.IDLE?idleWave*.65f:0f;
    int outline=0xff1c2020,body=pose.hitFlash?0xffd8aaa0:0xff789077,belly=0xffa7b59b;

    // [B] low, hunched placeholder silhouette; not claimed as LOD monster art.
    float bodyShift=side*(1.4f+attackLunge);
    p.setColor(outline);c.drawOval(new RectF(2.5f+bodyShift,7f-breathe,14.5f+bodyShift,21f),p);
    p.setColor(body);c.drawOval(new RectF(3.5f+bodyShift,8f-breathe,13.5f+bodyShift,20f),p);
    p.setColor(belly);c.drawOval(new RectF(5.3f+bodyShift,12f-breathe*.5f,11.7f+bodyShift,19.5f),p);

    float headX=left?2f:10f;
    p.setColor(outline);c.drawOval(new RectF(headX+bodyShift,3f-breathe,headX+7f+bodyShift,11f-breathe),p);
    p.setColor(body);c.drawOval(new RectF(headX+.8f+bodyShift,4f-breathe,headX+6.2f+bodyShift,10f-breathe),p);
    if(down){float eyeX=left?headX+1.5f:headX+4.5f;rect(c,0xffffd86a,eyeX+bodyShift,6f-breathe,eyeX+1.2f+bodyShift,7.2f-breathe);}

    float farX=left?11f:5f,nearX=left?5f:11f;
    rect(c,outline,farX-step,19-depth,farX+2f-step,26-depth);
    rect(c,outline,nearX+step,19+depth,nearX+2.4f+step,27+depth);
  }

  private void drawEffect(Canvas c,Pose pose){
    CharacterRenderer.EffectFamily family=pose.hitFlash?CharacterRenderer.EffectFamily.HIT:pose.effectFamily;
    if(family==CharacterRenderer.EffectFamily.NONE)return;
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);
    float sx=left?-1f:1f,sy=down?1f:-1f,q=phase(pose);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);
    switch(family){
      case CAST:
      case MAGIC:
        p.setColor(0xaa9c86ff);c.drawCircle(8+sx*4f,6+sy*5f,5f+8f*q,p);break;
      case SKILL:
        p.setColor(0xaa83f6a2);c.drawArc(new RectF(8+sx*8-10,11+sy*5-8,8+sx*8+10,11+sy*5+8),left?35:195,125,false,p);break;
      case HIT:
        p.setColor(0xaaff7655);c.drawCircle(8-sx*3f,9-sy*2f,4f+6f*q,p);break;
      default:
        p.setColor(0xaaffd273);c.drawArc(new RectF(8+sx*7-8,12+sy*4-7,8+sx*7+8,12+sy*4+7),left?45:205,100,false,p);break;
    }
    p.setStyle(Paint.Style.FILL);
  }

  /** Resolved directional source ref for future bitmap/sprite draw; null keeps procedural fallback. */
  public static String resolvedVisualRef(Pose pose){
    if(pose==null)return null;
    if(pose.visualBinding!=null){
      String directional=pose.visualBinding.resolve(pose.state,pose.direction);
      if(directional!=null)return directional;
    }
    if(pose.visualRef==null||pose.visualRef.trim().isEmpty()||ASSET_STATUS.equals(pose.visualRef))return null;
    return pose.visualRef;
  }

  /** Presentation helper only: chooses the closest screen diagonal without moving any entity. */
  public static CharacterRenderer.Direction directionToward(float fromX,float fromY,float toX,float toY,
      CharacterRenderer.Direction fallback){
    float dx=toX-fromX,dy=toY-fromY;
    if(Math.abs(dx)+Math.abs(dy)<0.001f)return fallback==null?CharacterRenderer.Direction.SE:fallback;
    if(dx<0f)return dy<0f?CharacterRenderer.Direction.NW:CharacterRenderer.Direction.SW;
    return dy<0f?CharacterRenderer.Direction.NE:CharacterRenderer.Direction.SE;
  }

  /** Maps existing monster runtime state into the common renderer state without changing combat semantics. */
  public static CharacterRenderer.State presentationState(RuntimeState.Monster monster){
    if(monster==null)return CharacterRenderer.State.IDLE;
    if(!monster.alive||monster.state==RuntimeState.Monster.State.DEAD||monster.state==RuntimeState.Monster.State.RESPAWN)
      return CharacterRenderer.State.DEAD;
    if(monster.hitFlash>0f)return CharacterRenderer.State.HIT;
    if(monster.state==RuntimeState.Monster.State.ATTACK)return CharacterRenderer.State.ATTACK;
    if(monster.state==RuntimeState.Monster.State.WANDER||monster.state==RuntimeState.Monster.State.CHASE)
      return CharacterRenderer.State.WALK;
    return CharacterRenderer.State.IDLE;
  }

  private static boolean isLeft(CharacterRenderer.Direction d){return d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW;}
  private static boolean isDown(CharacterRenderer.Direction d){return d==CharacterRenderer.Direction.SW||d==CharacterRenderer.Direction.SE;}
  private static float phase(Pose pose){return pose.stateDuration<=0f?0f:Math.max(0f,Math.min(1f,pose.stateClock/pose.stateDuration));}
  private void rect(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}
}
