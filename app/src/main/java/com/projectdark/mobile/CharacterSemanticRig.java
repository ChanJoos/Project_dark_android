package com.projectdark.mobile;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * Source-derived semantic attachment rig for paper-doll layers.
 * Heuristic by design for detached action bitmaps. WALK BODY/robe atlases already share
 * one authored 36x48 cell coordinate system, so authored registration wins over
 * alpha-derived re-centering. The live idle/walk weapon grip also uses the audited
 * per-direction/per-frame authored carry-anchor table instead of an alpha-edge guess.
 */
public final class CharacterSemanticRig {
  public static final String EVIDENCE="ADAPTED_SOURCE_DERIVED_SEMANTIC_HEURISTIC";
  public static final String IDLE_WALK_HAND_EVIDENCE="ADAPTED_AUTHORED_IDLE_WALK_HAND_TABLE_NOT_ALPHA_EDGE";

  public static final class Point {
    public final float x,y;
    public Point(float x,float y){this.x=x;this.y=y;}
  }
  public static final class Anchors {
    public final Point foot,pelvis,shoulder,dominantHand;
    /** Authored placement of this source bitmap inside the shared action source coordinate system. */
    public final float authoredOffsetX,authoredOffsetY;
    Anchors(Point foot,Point pelvis,Point shoulder,Point dominantHand,float offsetX,float offsetY){
      this.foot=foot;this.pelvis=pelvis;this.shoulder=shoulder;this.dominantHand=dominantHand;
      authoredOffsetX=offsetX;authoredOffsetY=offsetY;
    }
    public float globalPelvisX(){return authoredOffsetX+pelvis.x;}
    public float globalPelvisY(){return authoredOffsetY+pelvis.y;}
    public float globalFootY(){return authoredOffsetY+foot.y;}
  }
  public static final class Translation {
    public final float x,y;
    Translation(float x,float y){this.x=x;this.y=y;}
  }

  private CharacterSemanticRig(){}

  public static Anchors derive(Bitmap bitmap,Rect source,CharacterRenderer.Direction direction){
    float[] authored=authoredActionOffset(bitmap);
    if(bitmap==null||source==null||source.width()<=0||source.height()<=0)return fallback(source,authored[0],authored[1]);
    int l=source.width(),t=source.height(),r=-1,b=-1;
    for(int y=0;y<source.height();y++)for(int x=0;x<source.width();x++)if(opaque(bitmap,source.left+x,source.top+y)){
      if(x<l)l=x;if(x>r)r=x;if(y<t)t=y;if(y>b)b=y;
    }
    if(r<l||b<t)return fallback(source,authored[0],authored[1]);
    int h=b-t+1;
    Point foot=bandCenter(bitmap,source,l,r,Math.max(t,b-2),b,true);
    Point pelvis=bandCenter(bitmap,source,l,r,t+Math.round(h*.58f),t+Math.round(h*.76f),false);
    Point shoulder=bandCenter(bitmap,source,l,r,t+Math.round(h*.24f),t+Math.round(h*.43f),false);
    Point hand=idleWalkAuthoredHand(bitmap,source,direction);
    CharacterRenderer.Direction sourceHandDirection=sourceHandDirection(bitmap,direction);
    if(hand==null)hand=outerCluster(bitmap,source,l,r,t+Math.round(h*.30f),t+Math.round(h*.72f),sourceHandDirection,shoulder);
    return new Anchors(foot,pelvis,shoulder,hand,authored[0],authored[1]);
  }

  private static CharacterRenderer.Direction sourceHandDirection(Bitmap bitmap,CharacterRenderer.Direction direction){
    if(bitmap==null||direction==null)return direction;
    float[] authored=authoredActionOffset(bitmap);
    boolean detachedAction=authored[0]!=0f||authored[1]!=0f;
    if(!detachedAction||!CharacterRenderer.bodyMirrorX(direction))return direction;
    return direction==CharacterRenderer.Direction.NW?CharacterRenderer.Direction.NE:CharacterRenderer.Direction.SE;
  }

  private static Point idleWalkAuthoredHand(Bitmap bitmap,Rect source,CharacterRenderer.Direction direction){
    if(bitmap==null||source==null||direction==null)return null;
    if(bitmap.getWidth()!=CharacterRenderer.SOURCE_IDLE_WALK_WIDTH||bitmap.getHeight()!=CharacterRenderer.SOURCE_ATLAS_HEIGHT)return null;
    if(source.width()!=CharacterRenderer.SOURCE_FRAME_WIDTH||source.height()!=CharacterRenderer.SOURCE_FRAME_HEIGHT)return null;
    if(source.left<0||source.left%CharacterRenderer.SOURCE_FRAME_WIDTH!=0)return null;
    int column=source.left/CharacterRenderer.SOURCE_FRAME_WIDTH;
    if(column<0||column>=CharacterRenderer.IDLE_WALK_COLUMNS)return null;
    float x=CharacterRenderer.SOURCE_FOOT_ANCHOR_X+CharacterRenderer.weaponCarryOffsetX(direction,column);
    float y=CharacterRenderer.weaponCarryOffsetY(direction,column);
    return new Point(x,y);
  }

  public static boolean usesAuthoredIdleWalkHandAnchor(Bitmap bitmap,Rect source,CharacterRenderer.Direction direction){
    return idleWalkAuthoredHand(bitmap,source,direction)!=null;
  }

  /**
   * The renderer already places detached action BODY and robe using their independent authored
   * source-global offsets. Therefore this method must return only the residual LOCAL semantic
   * correction. Including authored offsets here as well double-counted them and could move the
   * CONTACT robe away from the BODY. Shared idle/walk atlases remain zero-translation.
   */
  public static Translation garmentTranslation(Anchors body,Anchors garment){
    if(body==null||garment==null)return new Translation(0f,0f);
    if(body.authoredOffsetX==0f&&body.authoredOffsetY==0f&&garment.authoredOffsetX==0f&&garment.authoredOffsetY==0f)
      return new Translation(0f,0f);
    float dx=body.pelvis.x-garment.pelvis.x;
    float footDy=body.foot.y-garment.foot.y;
    float pelvisDy=body.pelvis.y-garment.pelvis.y;
    return new Translation(Math.round(dx),Math.round((footDy+pelvisDy)*.5f));
  }

  /** Errors after renderer placement: authored offsets plus residual local translation. */
  public static float postTranslationPelvisXError(Anchors body,Anchors garment,Translation tr){
    return Math.abs(body.globalPelvisX()-(garment.globalPelvisX()+tr.x));
  }
  public static float postTranslationPelvisYError(Anchors body,Anchors garment,Translation tr){
    return Math.abs(body.globalPelvisY()-(garment.globalPelvisY()+tr.y));
  }
  public static float postTranslationFootYError(Anchors body,Anchors garment,Translation tr){
    return Math.abs(body.globalFootY()-(garment.globalFootY()+tr.y));
  }

  /** Exact authored group-02 offsets keyed by the packaged action bitmap dimensions. */
  private static float[] authoredActionOffset(Bitmap bitmap){
    if(bitmap==null)return new float[]{0f,0f};
    int w=bitmap.getWidth(),h=bitmap.getHeight();
    if(w==17&&h==51)return new float[]{8f,8f};
    if(w==18&&h==52)return new float[]{2f,8f};
    if(w==19&&h==51)return new float[]{6f,9f};
    if(w==21&&h==49)return new float[]{4f,3f};
    if(w==14&&h==33)return new float[]{8f,20f};
    if(w==19&&h==22)return new float[]{3f,27f};
    if(w==15&&h==30)return new float[]{6f,23f};
    if(w==20&&h==21)return new float[]{6f,22f};
    return new float[]{0f,0f};
  }

  private static Anchors fallback(Rect source,float offsetX,float offsetY){
    float w=source==null?36f:source.width(),h=source==null?48f:source.height();
    return new Anchors(new Point(w*.5f,h-2f),new Point(w*.5f,h*.66f),new Point(w*.5f,h*.34f),new Point(w*.72f,h*.52f),offsetX,offsetY);
  }
  private static Point bandCenter(Bitmap bitmap,Rect src,int l,int r,int y0,int y1,boolean bottomWeighted){
    int minY=Math.max(0,y0),maxY=Math.min(src.height()-1,y1);float sx=0f,sy=0f,weight=0f;
    for(int y=minY;y<=maxY;y++)for(int x=l;x<=r;x++)if(opaque(bitmap,src.left+x,src.top+y)){
      float w=bottomWeighted?1f+(y-minY):1f;sx+=x*w;sy+=y*w;weight+=w;
    }
    return weight<=0f?new Point((l+r)*.5f,(minY+maxY)*.5f):new Point(sx/weight,sy/weight);
  }
  private static Point outerCluster(Bitmap bitmap,Rect src,int l,int r,int y0,int y1,CharacterRenderer.Direction direction,Point fallback){
    boolean west=direction==CharacterRenderer.Direction.NW||direction==CharacterRenderer.Direction.SW;
    int minY=Math.max(0,y0),maxY=Math.min(src.height()-1,y1),extreme=west?src.width():-1;
    for(int y=minY;y<=maxY;y++)for(int x=l;x<=r;x++)if(opaque(bitmap,src.left+x,src.top+y))extreme=west?Math.min(extreme,x):Math.max(extreme,x);
    if((west&&extreme==src.width())||(!west&&extreme<0))return fallback;
    float sx=0f,sy=0f,n=0f;
    for(int y=minY;y<=maxY;y++)for(int x=l;x<=r;x++)if(opaque(bitmap,src.left+x,src.top+y)&&Math.abs(x-extreme)<=2){sx+=x;sy+=y;n++;}
    return n<=0f?fallback:new Point(sx/n,sy/n);
  }
  private static boolean opaque(Bitmap b,int x,int y){return x>=0&&y>=0&&x<b.getWidth()&&y<b.getHeight()&&((b.getPixel(x,y)>>>24)&0xff)!=0;}
}