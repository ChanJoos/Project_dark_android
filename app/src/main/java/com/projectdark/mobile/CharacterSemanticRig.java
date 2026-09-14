package com.projectdark.mobile;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * Source-derived semantic attachment rig for paper-doll layers.
 * Heuristic by design: anchors are derived from the packaged alpha pixels and are not
 * represented as canonical source metadata.
 */
public final class CharacterSemanticRig {
  public static final String EVIDENCE="ADAPTED_SOURCE_DERIVED_SEMANTIC_HEURISTIC";

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
    Point hand=outerCluster(bitmap,source,l,r,t+Math.round(h*.30f),t+Math.round(h*.72f),direction,shoulder);
    return new Anchors(foot,pelvis,shoulder,hand,authored[0],authored[1]);
  }

  /**
   * Align garment to BODY in the shared source coordinate system.
   * WALK atlases have authoredOffset=(0,0); group-02 action bitmaps carry their authored
   * BODY_ACTION_OFFSET / ROBE_ACTION_OFFSET here so local bitmap anchors are never compared
   * as though both sprites started at the same source origin.
   */
  public static Translation garmentTranslation(Anchors body,Anchors garment){
    if(body==null||garment==null)return new Translation(0f,0f);
    float dx=body.globalPelvisX()-garment.globalPelvisX();
    float footDy=body.globalFootY()-garment.globalFootY();
    float pelvisDy=body.globalPelvisY()-garment.globalPelvisY();
    return new Translation(Math.round(dx),Math.round((footDy+pelvisDy)*.5f));
  }

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
    if(w==17&&h==51)return new float[]{8f,8f};   // BODY source 0
    if(w==18&&h==52)return new float[]{2f,8f};   // BODY source 1
    if(w==19&&h==51)return new float[]{6f,9f};   // BODY source 2
    if(w==21&&h==49)return new float[]{4f,3f};   // BODY source 3
    if(w==14&&h==33)return new float[]{8f,20f};  // ROBE source 0
    if(w==19&&h==22)return new float[]{3f,27f};  // ROBE source 1
    if(w==15&&h==30)return new float[]{6f,23f};  // ROBE source 2
    if(w==20&&h==21)return new float[]{6f,22f};  // ROBE source 3
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
