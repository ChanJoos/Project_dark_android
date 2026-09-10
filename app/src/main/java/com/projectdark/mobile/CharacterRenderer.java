package com.projectdark.mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Base64;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * [ADAPTED] Default player renderer using the user-approved generated sprite atlas for
 * directional IDLE/WALK presentation. The atlas is not claimed as original Nexon art.
 * Non-IDLE/WALK action states retain a hard-pixel connected fallback until approved
 * per-state atlas frames are cut and bound.
 */
public final class CharacterRenderer {
  public static final String EVIDENCE="USER_APPROVED_GENERATED_ATLAS+ADAPTED";
  public static final String ASSET_STATUS="ADAPTED_ATLAS_ACTIVE_ORIGINAL_PENDING_CROP";
  public static final String PRESENTATION_PROFILE="USER_APPROVED_ATLAS_20260910_V4";
  public static final float PLAYER_RENDER_SCALE=1.50f;
  public static final float SHADOW_RENDER_SCALE=0.72f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;
  public static final float BASE_HEIGHT=32f;
  public static final float HEAD_TO_BODY_RATIO=0.31f;
  public static final float HEAD_WIDTH=10f;
  public static final float SHOULDER_WIDTH=8f;
  public static final boolean HARD_PIXEL_GRID=true;
  public static final boolean DEFAULT_ATLAS_ENABLED=true;
  public static final int ATLAS_FRAME_WIDTH=24;
  public static final int ATLAS_FRAME_HEIGHT=32;
  public static final int ATLAS_COLUMNS=5;
  public static final int ATLAS_ROWS=4;

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

  private static final String DEFAULT_ATLAS_BASE64="iVBORw0KGgoAAAANSUhEUgAAAHgAAACACAMAAAD3e12UAAAAwFBMVEVmIyCbJR5nVl7YZ0+qmqDUWDPgopOgXFbplGfj0tanTDMhL1fXIyH13LAvIy2Zd4gyRWZYNkxsb4iliXqVNkattchzhJ3VssFiRTnQNULKeYj9x3YrR4W9yccAAAD7+vkOFy4QCRHw5+gWKErXxszq19P5584UExb617ArCAr3t432pnTNtbX6xZUtFRQoNlPymGuvlpX2yKrqeE9NCAuPdnVPFhLIqK7xhVkVIzgbM1b989UIDCariY/RusWKaW02fFpZAAAAQHRSTlP///////////////////////////////////////8A////////////////////////////////////////////4VbhigAAFiVJREFUeNrlm2dz67iShgGCBINEyfYJE+4uSVAExSgxKMf//6+2KckyGvJ8nlu1qppTUwj9IhHA0w2T4F/6kX9dmMOv/bZIC79vktc/THP9TbpxblvjOzsVEngIt2aVhG63NeZ6cXNdufJi6qYWppkkid/q5YVZxbG7NV/b0yZu7LZrTTgJLyH8Lq3eByNJhgxziudkEVdDclitsbKwb+nxqx3zZqfiaywcbuObIXnWKsA43CxdcB+M8PGr1lqD7smuOcENMuJPgQkSJpRCTuImCVaWISFQAwakelfnNyTvNztxYgolfZ6cJ0Oq60pjgdZJmN96nMiLgYR52I1ZeNm6lykSDmmcjv+KXVOiaTNszlkcJ1uZmC1eQIxtw2SbyBbNjWn/zbgbb+VFmgtVeDz8SDzl8d9YeDVkMLfaukiYQqqdJK3pcjTW2zEfM560W5djYcrGY+4m/BL/kavCK0pXeSCvxKKaMKFktQptRh1VGFLhF59XnF6R8FA6j23SjRdYGCpQ6ZKQMtRjM04rQuR5Qz0sDFOTchK2sAhU4YkZVnkecpv+pfZsUZGwWuTx1KIMLff1wk6MReLadEznqnB+5cHCkWdu/0DC3A5CsqDh1LHVL3keiC53SMw7YqjC84Xgwnbiitv6jrAxNr5MXMJNtLiOPLV95p5bbQvNbScsuBdyru0gzbGu7fh8veLPZm4dacbj8xkt9uF7csI6laZ51r7jJvXr2nPtib4RkTSrOxrb79pGUVt1aUvjusDl56wuUla99DdY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";

  private static final boolean CONTRACT_VALID=CharacterRendererAudit.passes();
  private final Paint pixelPaint=new Paint();
  private final Paint fxPaint=new Paint();
  private final Bitmap defaultAtlas;

  public CharacterRenderer(){
    pixelPaint.setAntiAlias(false);pixelPaint.setDither(false);pixelPaint.setFilterBitmap(false);
    fxPaint.setAntiAlias(false);fxPaint.setDither(false);fxPaint.setFilterBitmap(false);
    byte[] bytes=Base64.decode(DEFAULT_ATLAS_BASE64,Base64.DEFAULT);
    defaultAtlas=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    if(!CONTRACT_VALID)throw new IllegalStateException("CharacterRenderer contract audit failed: "+CharacterRendererAudit.summary());
    if(defaultAtlas==null||defaultAtlas.getWidth()!=ATLAS_FRAME_WIDTH*ATLAS_COLUMNS||defaultAtlas.getHeight()!=ATLAS_FRAME_HEIGHT*ATLAS_ROWS)
      throw new IllegalStateException("Default player atlas decode/shape failed");
  }

  public void draw(Canvas c,Pose pose){
    if(c==null||pose==null||pose.direction==null||pose.state==null)throw new IllegalArgumentException("Character pose requires direction and state");
    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;
    float sw=10.5f*SHADOW_RENDER_SCALE,sh=2.8f*SHADOW_RENDER_SCALE;
    fxPaint.setStyle(Paint.Style.FILL);fxPaint.setColor(0x50000000);
    c.drawOval(new RectF(pose.x-sw,anchorY-sh,pose.x+sw,anchorY+sh),fxPaint);
    if(pose.state==State.IDLE||pose.state==State.WALK){drawDefaultAtlas(c,pose,anchorY);return;}
    drawActionFallback(c,pose,anchorY);
  }

  private void drawDefaultAtlas(Canvas c,Pose pose,float anchorY){
    int row=atlasRow(pose.direction);
    int col=pose.state==State.IDLE?0:1+(((int)(pose.walkClock*7f))&3);
    int left=col*ATLAS_FRAME_WIDTH,top=row*ATLAS_FRAME_HEIGHT;
    Rect src=new Rect(left,top,left+ATLAS_FRAME_WIDTH,top+ATLAS_FRAME_HEIGHT);
    float w=ATLAS_FRAME_WIDTH*PLAYER_RENDER_SCALE,h=ATLAS_FRAME_HEIGHT*PLAYER_RENDER_SCALE;
    RectF dst=new RectF(Math.round(pose.x-w*.5f),Math.round(anchorY-h),Math.round(pose.x+w*.5f),Math.round(anchorY));
    c.drawBitmap(defaultAtlas,src,dst,pixelPaint);
  }

  private int atlasRow(Direction d){switch(d){case SW:return 0;case SE:return 1;case NW:return 2;case NE:return 3;default:return 0;}}

  private void drawActionFallback(Canvas c,Pose pose,float anchorY){
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    int s=left?-1:1,v=down?1:-1;
    float phase=pose.stateDuration<=0?0f:Math.max(0f,Math.min(1f,pose.stateClock/pose.stateDuration));
    int reach=(pose.state==State.ATTACK||pose.state==State.SKILL)?Math.round(4f*(float)Math.sin(Math.PI*phase)):0;
    float ox=pose.x-12f*PLAYER_RENDER_SCALE,oy=anchorY-32f*PLAYER_RENDER_SCALE;
    c.save();c.translate(ox,oy);c.scale(PLAYER_RENDER_SCALE,PLAYER_RENDER_SCALE);
    int outline=0xff17130f,skin=pose.hitFlash?0xffffd5c7:0xffe59b69,dark=0xff19253b,mid=0xff273d5d,red=0xffc8242d,white=0xfff3eee8;
    px(c,outline,7,10,10,12);px(c,skin,8,11,8,10);px(c,dark,8,19,8,4);
    px(c,outline,8,21,4,9);px(c,outline,13,21,4,9);px(c,mid,9,22,2,6);px(c,mid,14,22,2,6);
    px(c,outline,5,12,4,9);px(c,skin,6,13,2,7);px(c,outline,16,12,4+Math.abs(reach),9);px(c,skin,17,13,2+Math.abs(reach),7);
    px(c,outline,6+s*2,0,12,11);px(c,white,7+s*2,1,10,5);px(c,red,left?7+s*2:14+s*2,4,3,4);px(c,skin,8+s*2,5,8,5);
    int handX=left?5-reach:20+reach,handY=down?18:15;
    if(pose.state==State.ATTACK||pose.state==State.SKILL){
      px(c,0xff6c4528,handX-s,handY-v,3,2);
      for(int i=1;i<=8+reach;i++)px(c,i==8+reach?0xffffffff:0xffdbe1e4,handX+s*i,handY+v*i,2,2);
    }
    if(pose.state==State.CAST){fxPaint.setStyle(Paint.Style.STROKE);fxPaint.setStrokeWidth(2f);fxPaint.setColor(0xcc76caff);c.drawCircle(12+s*6,8+v*4,4+phase*7,fxPaint);fxPaint.setStyle(Paint.Style.FILL);}
    if(pose.state==State.SKILL){fxPaint.setStyle(Paint.Style.STROKE);fxPaint.setStrokeWidth(3f);fxPaint.setColor(0xdd78ceff);c.drawArc(new RectF(12+s*8-13,15+v*3-11,12+s*8+13,15+v*3+11),left?25:195,150,false,fxPaint);fxPaint.setStyle(Paint.Style.FILL);}
    if(pose.state==State.HIT){fxPaint.setStyle(Paint.Style.STROKE);fxPaint.setStrokeWidth(2f);fxPaint.setColor(0xddff765e);c.drawCircle(12-s*3,11-v*2,5,fxPaint);fxPaint.setStyle(Paint.Style.FILL);}
    if(pose.state==State.DEAD){c.rotate(left?-76f:76f,12,29);}
    c.restore();
  }

  private void px(Canvas c,int color,float x,float y,float w,float h){pixelPaint.setStyle(Paint.Style.FILL);pixelPaint.setColor(color);c.drawRect(x,y,x+w,y+h,pixelPaint);}
  public boolean hasRequiredStateContract(){return CharacterRendererAudit.passes();}
  public String contractAuditSummary(){return CharacterRendererAudit.summary();}
  public boolean ownsPlayerLocalEffects(){return true;}
  public float playerRenderScale(){return PLAYER_RENDER_SCALE;}
  public boolean usesDefaultAtlasFor(State state){return DEFAULT_ATLAS_ENABLED&&(state==State.IDLE||state==State.WALK);}
}
