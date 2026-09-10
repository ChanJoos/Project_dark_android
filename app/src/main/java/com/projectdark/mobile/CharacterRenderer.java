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
 * [ADAPTED] User-approved hard-pixel player renderer.
 *
 * Device playtest 2026-09-10 supersedes the former 1.50 presentation baseline. V5 uses a
 * 1.60 render scale and a revised source atlas whose head band was reduced one logical pixel
 * (target visual head/body ratio 0.29) without changing the logical foot anchor/collision point.
 * IDLE/WALK and ATTACK are image-atlas backed. Remaining action states preserve direction and
 * reuse the same cohesive atlas body until dedicated approved frames replace them.
 */
public final class CharacterRenderer {
  public static final String EVIDENCE="USER_DEVICE_PLAYTEST+USER_APPROVED_GENERATED_ATLAS+ADAPTED";
  public static final String ASSET_STATUS="ADAPTED_ATLAS_V5_ACTIVE_ORIGINAL_PENDING_CROP";
  public static final String PRESENTATION_PROFILE="USER_PLAYTEST_ATLAS_20260910_V5";
  public static final float PLAYER_RENDER_SCALE=1.60f;
  public static final float SHADOW_RENDER_SCALE=0.72f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;
  public static final float BASE_HEIGHT=32f;
  public static final float HEAD_TO_BODY_RATIO=0.29f;
  public static final float HEAD_WIDTH=9f;
  public static final float SHOULDER_WIDTH=8f;
  public static final boolean HARD_PIXEL_GRID=true;
  public static final boolean DEFAULT_ATLAS_ENABLED=true;
  public static final boolean ATTACK_ATLAS_ENABLED=true;
  public static final int ATLAS_FRAME_WIDTH=24;
  public static final int ATLAS_FRAME_HEIGHT=32;
  public static final int ATLAS_COLUMNS=5;
  public static final int ATLAS_ROWS=4;
  public static final int ATTACK_ATLAS_COLUMNS=4;

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

  private static final String DEFAULT_ATLAS_BASE64="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";
  private static final String ATTACK_ATLAS_BASE64="iVBORw0KGgoAAAANSUhEUgAAAGAAAACACAMAAADDApyIAAADAFBMVEUAAAD7+vny6ub658785bPz18L2yqLazdPqtaLIsLXvnny6oKLZkHHhdVOYipSFc4XVW0OrWUxtYHSJTEGnMS+rFBdqKStuEBRBRmQyNlEuLUYYKEw4FBpPBQkSGDEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABWSJkNAAAAAXRSTlMAQObYZgAADdJJREFUeNrtWtuyrCyvFbU4CKgoINpiv/9b/kHtbg7Oqr2/6zUv1qoGSUKAMEZCVf37+z//cfib/upk0PnUjtnDIMsZY6UIglBNyKP4iRPoGwoVM4b2QhhnCCHCt7S1B/nQjOonBVcnZTht3q92RhINM6tDa53N7PwUEVvbhwmcI2BI5qaBXB0EJa6o789dMi1CWM1Go01TyB844zBGCbL06bzAPaieZWOieTtyiRLUxB+PVJN6X4wxbaHAKT0T4r0xSzKDnjWKMAtjlmjpLFk0QUFSokAbAwpww/1QKJiNVjV7T01f2azdQ/uisY9nIA0laKe11okbHGFIvpt9L5fAUQXOmN8qX3rpad3b97zv8SJIRQia9+Jr2IlKITE/7iJWk042/vpVf+x1PSaq9dymE+thZRBFNHeEQnMn5P6kYOgJ6bhql1HCr2P4bMCt51xgNeaSetTI3FTbKWLVvGg9jzLdLZUaZ0XmtluaLpj1Oj7dSqjGUpwaNXbgiRnn273DjZytwaRpqKCvWLVsmrbT2mM8jqesW/4OYzqrllS+aLumG4XKFNi2xQ3GbQf/a7PHCnyDuqaRbed9ZmrbtLLTWKb2jx1p4N/Mbx7LDjVdo7VW3mdy2pEQ3ObbotItCKKtFFHbZho9NnJsfaZgpGYUQspygU3TjmMHFhUxZMRSjrjzXRIS5n3spFE6FaXazuAWd2Mhf+8aqjvayaJL4BbGGLlHolBfrZWhtBmzU+Nbs2uJzcMWtVLZDoux2L9WKQ3ug/P6C5vkOCct5by5JKzU4Vg79yDfVdsfPQTk2tHYaosif1+BiqNa328S3wf8CEYcx4OY9/DHHbe+w24FKeuRXjhr+Oc1veObpV8HOD6gYi2jxGvYwqY9HmbQRxK/f6/X69SbXjfT1f5aX0/GQuPxKnTvQWvZvoKgdS2+Ds1BwYOz16D19VrzORwrCD9e6dWr9QqefuUKPAg4xz8oWLftOHVnkmDJttMjyc7aye2nVIj8BYHsvGobiYz9s7BLYeKiAU3K+nPFkmOpHL/CtKTZSZicvE6w0TIOIG5g6lRoE5O26VraxXSxOdPwgxJ0SeWj+7f3KurpUV+bT4yJFa/D4O5TGJtDFn/7fncs3l6OLZdqt71nGzljY/YrNz3/56lbt13Nw2/RHEIfBRNLltmhbbpvxznBOSu5fh1um0ixM7aVMR6fUPeNBVv28fad0DRlm/c79gnQDpP7h/b/wx9gED78zR0eutgTDwgfs6fAf0Fx9NC1BUBek3wUDyOKZh4+JqwwZ8PoT4LAL8Bf80z+BdMZL+QHqL/l0R2dHcqjYtd9aIBFydY+iQyIShXc36LCd6SGGQfMbXIFE/AAgI7ajCYGfNPMKIVZAReImpGhQtY1YFmSES4jfeAAflkKBVqamnizGB8r6IEYaFFzDZojBfCdH2u2yBwCGUD1zd4ivucKnDHAM/aZDEkM7jWgijfhVpsocqI3b/s3InPGPyD8WUJqsaPdlgRHgRuwVz+gca6mf8M1wnjjaYSj67edvUCs9iQnAhxcDbgVP5BAzgihVIhqP219X56yHvWCsZ4P8ZBZNxz8wGkhZ5sHIlounrAX50RgLkY6nqziXj4rkGB8yiRhSdqaz2UEBJQpZj8vchajXTMMN5NZsQWA935hkPumDbRB5pgP47m2qphA12IEKw/wvcFjyg+gqxnBeiCbe4yDdsDLmgKiTTQAfxlnQQHtpwpaRIFntBIDAtbJDGTTUoIbWqBfCZwBxDdtAgekoGBlYBT5xEaKwFI1jin/BOQtBJBrsRTwegTuAxShSVR3gIgJ4O5MgQHaAx4aS3xtW1AtxxK+V9J0HR1HQ7t4FNZt4DemHTMGtYApnfaFGHCPlh0tFXStAXaDgSzHxnpsMQW9vqUxMdnhYPvxwf7KeegaUyPv1bTAHDHgHE0jBbqrwBzhw1GPb47Glhf+DY3WgFyPB+58VN4APaiSo0A+gMDFUJ28T9lP/OAC3A89fVAcsOzw7l0k6XMPTEd81ti7P15HVWJ0QCvTSR1KXD+416W4n9aY4pw84AU3ZIrHjhOkr9sjP3gA5GEQQPojb1/Bn9v28PVJEI51Ox7w+6m6HBI4y+u1FZ+D2twVOphzefx40Hw8EZDRX5xlez3ylfRr/cPCWZCCC+K5ZxZ33E1noKoPC7PJVlQX0ZBMZKGxX84TZr018Rnse0bNnRDSY3Kg5uuQi+jzfqr1L9eTiO+nK0BXdtYp6rZ3RsPq5Hj2crluMtIfv81b7/6a0GaXGCJUaz9fBAGmTZPDsPKvC6wjHzjTD9bs9606RBhnq8nluNVNfZKLcj2/efWQ8oMDCPR9Qff8C/4dqunLXccn5Rlu/VKHNeOq9+81Tyd8OtzL/Ya8hune6Nv2D+z/l78A9+1z3pQx+v8Q81wlUBdBKDVMfX2i6CeyR0gOo6cTdJO+tPLG3SRffsfRzQ8egH3oSLgPJ7eYQsPdkaX9T+pz1wNUzn62e0jMrfndhvJyCTEdJaEyokwWjnRNgeJwo3O8PLG7qhF5jy8NCWJIPbMcvgN+B9pgczGV1ronFoiDLnB9B3ypBurCv0gahpNQ7yEqRxDzMgMnmmepC3iyzLS2+4iLGw0YCK5rwukv0vY+FDM447OZCwQNGJpTofKlnCE2I0v3QvO2M7sHaTHFGewAMBBIb0EzJwX0lmL6sL96Rmwzom+C5buos7entUNc8IHPAdTPDwdKDb0QqsSO4zyzGXCcuI4PW795EUw56Yc0AMPVQUD89pDhbZUA3lVixwbQ6ay1aK5LEnw7fBGnor1KjQXAqgAJFg61GAekLAvqMzaiwV0rtR71F/t8Bi0jDelllbCJdgRCUaTBZUMplghoTr5LOymaBosSFlc7bjuAzKKJaOAsgOc1DUV5sWZsA9BH1OQZfmAxYKN8ypt7HFQ3Y0xB9lBRAF+gTM7eUC2F6OSIdVrawSYk9+VTlPVCt1iO2nQ/DUtnaOAZJJuBRkAngD5pk/KVpWt3DTxDP8bfk7TA0YQZfuQA0G9HAf/gdBEWqd/aCOkrqeNEKAn5Sb883hIzXjQmDZi6j5/bYm8NxJZu9LJNpuDI9n4f+36cKczo7jh/HH8k+JaAugKj4+z9iZn7r/6ScPnj2I/jKiCs6xSRAHeRgweCMH1j6PH+QA735dD7qOcE66P3K6TNX8ea1EaI6090+wCuiXt/NP9MGk6wHKA6eSf3JgECdVRPif8tAPTDufWpxnJUBTVx61U/cOwBdT9VKC7Jq3sqXVykJXWfC3TlVX69PWTrQz3AXeMfyFXQfVSFsNOSnB8Asv1snS0JCUZ995rxeXC8kxbpDPSHdKVaZ2nsnX+Ir+Rpnu/QYeq8jkbeV1HAV/GJsp4GMq9IkvUfHPttWqKjDHfzScv4N/JJiGcz9ct1dmOq4dxFmvVikjLBhyBU2+HNd3878im9vHa10AQX1d9dvqsjwlCEn3x+t0sfZWCHmt6SDgdA4Vvwnmp0k2y3DiyB/APh58K7lRH+69hc2C1uAuRSJVTmh/6HKYoVzn1/uIwgRB3l9h2mYfiH9f/LHyAm/ITR/3pcBDygKBTsDAA9/0P+9ZanKuH7+V6ouM9OIpAVCi48/ExA2A3fyXN7wx/KE2eloKg0PAiJCULeVz8I+pUnUN3HhPtDMsraWlWT20VDqgFds244T2ojXFEWqgQkLhSw0QEzCIShLpfBcAKiANgbnVUuLsDPTEJw+kAoasYU1T9ZHDAGBTGKmJIzLkPYFEAzs6ck2ATWyLTOAL/RysL3Jn5A0e9+UT0YKU2Jc9xEwK984Hl5Rxo1IJD0dR16hzqoF7tSpCdDXIOkvt9tTXrO+8c6GuxgPuQnQXKuEGD04YdljoNwAMtYENYn9iwtagVwnudI5KC9j/1wrapnbEBAK77gazqOdROt8K0ias4yXkurGqIegdreyYGrSAF6Hb3CeNSWDUpFKzOcWTiNhbBFLQirGalOLqX88Xyd03S/50h8n4gBPwhNVVcyit2KpcNtK7L036gxxW2poaMIU9S2P6AcAv0i8Ii7DhB/W2gQgNSDSWn5oD0hfVO+8+kkCGpk2ya7cTezaSUKT5gKBbprGymbLnb47il4QnaorAaMsgMdwEJKQQGj06Z8vwYWibFpYlqnQIGAz8tqlm67lspO01KBpIGCNGV9SmNMddvEc9Ye+AqWBuZmMxhF7SgpHXFT7ABvBHCxpi1mbbX2S9d0S3Tnv3ej9WLBHp+v8W0ueaB7dgcW2oyPeBP4bc9+cB9oxnG61abPQUN4PQKi1KJ4LPg6c4/CCFRk5Y8zCfGrFKzT+2YZdnonEbs/tjP9flTlE9ubmfCjz7Pv52sbV0UvmNw7QFVA5Ov6zjKwn8ICL17ouPNtUzW9plIBnJWhGrKcf2jtC7h0nDN4APbu6viWMGMFh8sZy3HN4IFNXKIfFFzUoHwAtvtzmV/5djjzx6WYG/6/igqC/kyw0jkND88Nt2wAFUA7j+3IxVQLPXNgfjHZMVjU/a3NDog7a9uWJw+P+PUidlfaZEyv59cTJrRkx2C4cnVWy5nH5Q53AWat/M+kbRvOapzVIlPQr9Zbdxf4eMLQf1NVFkU7Yxv4RQQ683sIEhrPxXpbOpEEvjMWDNp6VLsU15ObXB8bjYlA4KXb+U6DuShveu3xvgeSkUGj9SqhueJh0IcHuGN64MqOsIf71/H+K+Z/THruqYNFskEAAAAASUVORK5CYII=";

  private static final boolean CONTRACT_VALID=CharacterRendererAudit.passes();
  private final Paint pixelPaint=new Paint();
  private final Paint fxPaint=new Paint();
  private final Bitmap defaultAtlas;
  private final Bitmap attackAtlas;

  public CharacterRenderer(){
    pixelPaint.setAntiAlias(false); pixelPaint.setDither(false); pixelPaint.setFilterBitmap(false);
    fxPaint.setAntiAlias(false); fxPaint.setDither(false); fxPaint.setFilterBitmap(false);
    defaultAtlas=decode(DEFAULT_ATLAS_BASE64);
    attackAtlas=decode(ATTACK_ATLAS_BASE64);
    if(!CONTRACT_VALID)throw new IllegalStateException("CharacterRenderer contract audit failed: "+CharacterRendererAudit.summary());
    requireAtlas(defaultAtlas,ATLAS_COLUMNS,ATLAS_ROWS,"default");
    requireAtlas(attackAtlas,ATTACK_ATLAS_COLUMNS,ATLAS_ROWS,"attack");
  }

  private static Bitmap decode(String value){byte[] bytes=Base64.decode(value,Base64.DEFAULT);return BitmapFactory.decodeByteArray(bytes,0,bytes.length);}
  private static void requireAtlas(Bitmap b,int cols,int rows,String name){
    if(b==null||b.getWidth()!=ATLAS_FRAME_WIDTH*cols||b.getHeight()!=ATLAS_FRAME_HEIGHT*rows)
      throw new IllegalStateException(name+" player atlas decode/shape failed");
  }

  public static boolean isDefaultAtlasState(State state){return DEFAULT_ATLAS_ENABLED&&(state==State.IDLE||state==State.WALK);}
  public static boolean isAttackAtlasState(State state){return ATTACK_ATLAS_ENABLED&&state==State.ATTACK;}
  public static boolean isAtlasBackedState(State state){return isDefaultAtlasState(state)||isAttackAtlasState(state);}

  public void draw(Canvas c,Pose pose){
    if(c==null||pose==null||pose.direction==null||pose.state==null)throw new IllegalArgumentException("Character pose requires direction and state");
    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;
    drawShadow(c,pose,anchorY);
    if(isDefaultAtlasState(pose.state)){drawDefaultAtlas(c,pose,anchorY);return;}
    if(isAttackAtlasState(pose.state)){drawAttackAtlas(c,pose,anchorY);return;}
    drawDirectionalAction(c,pose,anchorY);
  }

  private void drawShadow(Canvas c,Pose pose,float anchorY){
    float k=pose.state==State.DEAD?1.25f:1f;
    float sw=10.5f*SHADOW_RENDER_SCALE*k,sh=2.8f*SHADOW_RENDER_SCALE;
    fxPaint.setStyle(Paint.Style.FILL);fxPaint.setColor(0x50000000);
    c.drawOval(new RectF(pose.x-sw,anchorY-sh,pose.x+sw,anchorY+sh),fxPaint);
  }

  private void drawDefaultAtlas(Canvas c,Pose pose,float anchorY){
    int col=pose.state==State.IDLE?0:1+(((int)(pose.walkClock*7f))&3);
    drawFrame(c,defaultAtlas,col,atlasRow(pose.direction),pose.x,anchorY,PLAYER_RENDER_SCALE,0f);
  }

  private void drawAttackAtlas(Canvas c,Pose pose,float anchorY){
    float q=phase(pose);
    int col=Math.min(ATTACK_ATLAS_COLUMNS-1,Math.max(0,(int)(q*ATTACK_ATLAS_COLUMNS)));
    drawFrame(c,attackAtlas,col,atlasRow(pose.direction),pose.x,anchorY,PLAYER_RENDER_SCALE,0f);
  }

  private void drawDirectionalAction(Canvas c,Pose pose,float anchorY){
    // Dedicated action atlases are still pending for CAST/SKILL/HIT/DEAD. Keep the exact V5 body,
    // direction and anchor instead of falling back to detached procedural limbs.
    float q=phase(pose),dx=0f,dy=0f,rotation=0f;
    if(pose.state==State.HIT)dx=(isLeft(pose.direction)?3f:-3f)*q;
    if(pose.state==State.DEAD){rotation=isLeft(pose.direction)?-74f:74f;dy=2f;}
    drawFrame(c,defaultAtlas,0,atlasRow(pose.direction),pose.x+dx,anchorY+dy,PLAYER_RENDER_SCALE,rotation);
    if(pose.state==State.CAST)drawCastFx(c,pose,q);
    if(pose.state==State.SKILL)drawSkillFx(c,pose,q);
    if(pose.state==State.HIT||pose.hitFlash)drawHitFx(c,pose,q);
  }

  private void drawFrame(Canvas c,Bitmap atlas,int col,int row,float x,float anchorY,float scale,float rotation){
    int left=col*ATLAS_FRAME_WIDTH,top=row*ATLAS_FRAME_HEIGHT;
    Rect src=new Rect(left,top,left+ATLAS_FRAME_WIDTH,top+ATLAS_FRAME_HEIGHT);
    float w=ATLAS_FRAME_WIDTH*scale,h=ATLAS_FRAME_HEIGHT*scale;
    RectF dst=new RectF(Math.round(x-w*.5f),Math.round(anchorY-h),Math.round(x+w*.5f),Math.round(anchorY));
    if(rotation==0f){c.drawBitmap(atlas,src,dst,pixelPaint);return;}
    c.save();c.rotate(rotation,x,anchorY-2f*scale);c.drawBitmap(atlas,src,dst,pixelPaint);c.restore();
  }

  private void drawCastFx(Canvas c,Pose pose,float q){
    float sx=isLeft(pose.direction)?-1f:1f,sy=isDown(pose.direction)?1f:-1f;
    fxPaint.setStyle(Paint.Style.STROKE);fxPaint.setStrokeWidth(2f);fxPaint.setColor(0xcc76caff);
    c.drawCircle(pose.x+sx*10f,pose.y-30f+sy*4f,4f+8f*q,fxPaint);fxPaint.setStyle(Paint.Style.FILL);
  }
  private void drawSkillFx(Canvas c,Pose pose,float q){
    float sx=isLeft(pose.direction)?-1f:1f,sy=isDown(pose.direction)?1f:-1f;
    float cx=pose.x+sx*(14f+5f*q),cy=pose.y-22f+sy*3f;
    fxPaint.setStyle(Paint.Style.STROKE);fxPaint.setStrokeWidth(4f);fxPaint.setColor(0xdd78ceff);
    c.drawArc(new RectF(cx-22f,cy-20f,cx+22f,cy+20f),isLeft(pose.direction)?25:195,150,false,fxPaint);
    fxPaint.setStrokeWidth(2f);fxPaint.setColor(0xeeedf9ff);
    c.drawArc(new RectF(cx-18f,cy-17f,cx+18f,cy+17f),isLeft(pose.direction)?28:198,145,false,fxPaint);
    fxPaint.setStyle(Paint.Style.FILL);
  }
  private void drawHitFx(Canvas c,Pose pose,float q){
    float sx=isLeft(pose.direction)?-1f:1f;
    fxPaint.setStyle(Paint.Style.STROKE);fxPaint.setStrokeWidth(2f);fxPaint.setColor(0xddff765e);
    c.drawCircle(pose.x-sx*6f,pose.y-28f,4f+5f*q,fxPaint);fxPaint.setStyle(Paint.Style.FILL);
  }

  private int atlasRow(Direction d){switch(d){case SW:return 0;case SE:return 1;case NW:return 2;case NE:return 3;default:return 0;}}
  private boolean isLeft(Direction d){return d==Direction.NW||d==Direction.SW;}
  private boolean isDown(Direction d){return d==Direction.SW||d==Direction.SE;}
  private float phase(Pose pose){return pose.stateDuration<=0f?0f:Math.max(0f,Math.min(1f,pose.stateClock/pose.stateDuration));}

  public boolean hasRequiredStateContract(){return CharacterRendererAudit.passes();}
  public String contractAuditSummary(){return CharacterRendererAudit.summary();}
  public boolean ownsPlayerLocalEffects(){return true;}
  public float playerRenderScale(){return PLAYER_RENDER_SCALE;}
  public boolean usesDefaultAtlasFor(State state){return isDefaultAtlasState(state);}
  public boolean usesAttackAtlasFor(State state){return isAttackAtlasState(state);}
}
