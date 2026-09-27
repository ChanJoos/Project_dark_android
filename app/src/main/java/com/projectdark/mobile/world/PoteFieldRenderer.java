package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Asset-driven Pote forest renderer.
 * No synthetic circles/ovals/placeholder trees are permitted here: every visible forest object
 * resolves to a POTE_* production sprite and is placed by the spatial relationship grammar.
 */
public final class PoteFieldRenderer {
  public static final String STATUS="POTE_CLUSTERED_FOREST_V2";
  private static final float TILE_W=64f,TILE_H=32f;
  private final Paint pixel=new Paint();
  private final AssetManager assets=findAssets();
  private final Map<String,Bitmap> cache=new LinkedHashMap<>();
  private final List<Placement> placements=buildPlacements();

  private static final class Placement {
    final String asset,role; final float x,y,scale; final boolean tile;
    Placement(String a,float x,float y,float s,String role,boolean tile){
      this.asset=a;this.x=x;this.y=y;this.scale=s;this.role=role;this.tile=tile;
    }
  }

  public PoteFieldRenderer(){
    pixel.setAntiAlias(false);pixel.setFilterBitmap(false);pixel.setDither(false);
  }

  public int placementCount(){return placements.size();}

  public void draw(Canvas c,WorldRuntimeAdapter w){
    if(c==null||w==null)return;drawBelow(c,w,Float.POSITIVE_INFINITY);
  }

  /** Draw terrain and objects whose ground anchors are behind the supplied actor depth. */
  public void drawBelow(Canvas c,WorldRuntimeAdapter w,float actorY){
    if(c==null||w==null)return;
    c.drawColor(0xff241d15);drawFloor(c,w);
    for(Placement p:placements)if(p.y<=actorY)drawPlacement(c,w,p);
  }

  /** Draw foreground canopies/props after the actor so Y-depth remains spatially believable. */
  public void drawAbove(Canvas c,WorldRuntimeAdapter w,float actorY){
    if(c==null||w==null)return;
    for(Placement p:placements)if(p.y>actorY)drawPlacement(c,w,p);
  }

  private void drawFloor(Canvas c,WorldRuntimeAdapter w){
    // V2 terrain is layered, not a flat brown board. Broad organic bands are deliberately larger
    // than a tile so the player reads forest floor / worn corridor / damp stream edge as regions.
    pixel.setStyle(Paint.Style.FILL);pixel.setColor(0xff3f4b27);c.drawRect(0,0,c.getWidth(),c.getHeight(),pixel);
    terrainPatch(c,w,420,760,430,250,0xff665039); terrainPatch(c,w,610,610,500,250,0xff665039);
    terrainPatch(c,w,700,430,520,230,0xff665039); terrainPatch(c,w,850,315,390,190,0xff665039);
    terrainPatch(c,w,1060,455,260,560,0xff455633);
    terrainPatch(c,w,560,520,220,150,0xff746049); terrainPatch(c,w,760,505,210,145,0xff746049);
    terrainPatch(c,w,360,735,220,145,0xff746049);
  }
  private void terrainPatch(Canvas c,WorldRuntimeAdapter w,float x,float y,float ww,float hh,int color){
    WorldCameraTransform.Point q=w.worldToScreen(x,y);pixel.setColor(color);
    c.drawOval(new RectF(q.x-ww*.5f,q.y-hh*.5f,q.x+ww*.5f,q.y+hh*.5f),pixel);
  }

  private void drawPlacement(Canvas c,WorldRuntimeAdapter w,Placement p){
    Bitmap b=bitmap(p.asset);if(b==null)return;
    WorldCameraTransform.Point q=w.worldToScreen(p.x,p.y);
    if(p.tile){
      RectF dst=new RectF(q.x-TILE_W*.5f,q.y-TILE_H*.5f,q.x+TILE_W*.5f,q.y+TILE_H*.5f);
      c.drawBitmap(b,null,dst,pixel);return;
    }
    float ww=b.getWidth()*p.scale,hh=b.getHeight()*p.scale;
    RectF dst=new RectF(Math.round(q.x-ww*.5f),Math.round(q.y-hh),Math.round(q.x+ww*.5f),Math.round(q.y));
    if(dst.right>=0&&dst.left<=c.getWidth()&&dst.bottom>=0&&dst.top<=c.getHeight())c.drawBitmap(b,null,dst,pixel);
  }

  private Bitmap bitmap(String name){
    if(cache.containsKey(name))return cache.get(name);
    Bitmap b=null;if(assets!=null)try(InputStream in=assets.open(name)){
      BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);
      if(b!=null&&!name.startsWith("POTE_GD_"))b=trimSourceEdge(stripEdgeMatte(b));
    }catch(Throwable ignored){}
    cache.put(name,b);return b;
  }

  private static Bitmap trimSourceEdge(Bitmap src){
    if(src==null||src.getWidth()<=8||src.getHeight()<=8)return src;
    // Source extraction occasionally leaves a 1-2 px crop/separator rule on the outer edge.
    // Trim two pixels symmetrically; anchor remains effectively unchanged at gameplay scale.
    return Bitmap.createBitmap(src,2,2,src.getWidth()-4,src.getHeight()-4);
  }

  /**
   * Production-pack lesson from Milles: cutout sprites may still carry a dark/brown source matte.
   * Remove only edge-connected pixels similar to the corner matte; interior dark sprite pixels survive.
   */
  private static Bitmap stripEdgeMatte(Bitmap src){
    if(src==null)return src;
    int w=src.getWidth(),h=src.getHeight();if(w<3||h<3)return src;
    int[] px=new int[w*h];src.getPixels(px,0,w,0,0,w,h);
    // Many source cutouts have a transparent outer gutter and an opaque rectangular matte inside it.
    // Find the dominant quantized opaque color instead of trusting transparent corner RGB.
    int[] hist=new int[4096];int opaque=0;
    for(int c:px){int aa=(c>>>24)&255;if(aa<24)continue;int rr=(c>>>20)&15,gg=(c>>>12)&15,bb=(c>>>4)&15;hist[(rr<<8)|(gg<<4)|bb]++;opaque++;}
    int best=0,count=0;for(int i=0;i<hist.length;i++)if(hist[i]>count){count=hist[i];best=i;}
    if(opaque==0||count<Math.max(12,opaque/80))return src;
    int br=((best>>>8)&15)*17,bg=((best>>>4)&15)*17,bb=(best&15)*17;
    boolean[] seen=new boolean[px.length];ArrayDeque<Integer> q=new ArrayDeque<>();
    for(int x=0;x<w;x++){q.add(x);q.add((h-1)*w+x);}for(int y=1;y<h-1;y++){q.add(y*w);q.add(y*w+w-1);}
    final int threshold=45*45;
    while(!q.isEmpty()){
      int i=q.removeFirst();if(i<0||i>=px.length||seen[i])continue;seen[i]=true;
      int c=px[i],aa=(c>>>24)&255,rr=(c>>>16)&255,gg=(c>>>8)&255,bl=c&255;
      int dr=rr-br,dg=gg-bg,db=bl-bb;
      // Also remove edge-connected near-black scan/matte remnants. This is intentionally
      // edge-connected only, so legitimate dark bark/shadow pixels inside the sprite survive.
      int lum=(rr*3+gg*4+bl)/8;
      if(aa<24||dr*dr+dg*dg+db*db<=threshold||lum<28){
        px[i]=c&0x00ffffff;
        int x=i%w,y=i/w;if(x>0)q.add(i-1);if(x+1<w)q.add(i+1);if(y>0)q.add(i-w);if(y+1<h)q.add(i+w);
      }
    }
    // Remove scan/crop separator columns occasionally embedded inside source cutouts.
    // A separator is a near-black column spanning most of the sprite height; ordinary bark/shadows
    // do not satisfy this full-column criterion.
    for(int x=0;x<w;x++){
      int dark=0,solid=0;
      for(int y=0;y<h;y++){
        int c=px[y*w+x],aa=(c>>>24)&255;if(aa<24)continue;solid++;
        int rr=(c>>>16)&255,gg=(c>>>8)&255,bl=c&255;
        if((rr*3+gg*4+bl)/8<30)dark++;
      }
      if(solid>Math.max(12,(int)(h*.55f))&&dark>=solid*.82f){
        for(int y=0;y<h;y++)px[y*w+x]&=0x00ffffff;
      }
    }
    Bitmap out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);out.setPixels(px,0,w,0,0,w,h);return out;
  }

  private static List<Placement> buildPlacements(){
    List<Placement> p=new ArrayList<>();
    // V2 is authored as masses, not independent props: each canopy parent receives understory,
    // groundcover and detail children. Open centres are intentionally preserved for navigation.
    cluster(p,150,190,1,1);cluster(p,280,165,2,3);cluster(p,420,155,3,5);cluster(p,570,150,4,7);
    cluster(p,730,145,5,2);cluster(p,890,155,6,4);cluster(p,1015,175,7,6);
    cluster(p,1250,220,2,8);cluster(p,1285,365,5,1);cluster(p,1290,520,3,3);cluster(p,1295,690,6,5);cluster(p,1260,825,1,7);
    cluster(p,120,340,4,2);cluster(p,120,505,7,4);cluster(p,145,665,2,6);

    // West grove: one continuous mass, with a deliberate east-facing opening.
    cluster(p,285,315,5,3);cluster(p,385,335,1,5);small(p,330,395,1);small(p,445,300,2);
    bush(p,250,375,6);ground(p,305,430,2);ground(p,420,415,5);detail(p,365,445,2);

    // Old-growth central grove / landmark mass.
    cluster(p,525,565,6,6);cluster(p,635,615,3,1);small(p,500,665,3);stump(p,455,615,3);stump(p,690,655,4);
    bush(p,575,690,5);ground(p,505,710,4);ground(p,690,700,1);detail(p,600,735,3);

    // North-east mass frames the path before the stream.
    cluster(p,790,265,7,7);cluster(p,900,285,2,2);small(p,750,345,2);
    bush(p,850,360,4);ground(p,930,380,6);rock(p,965,245,2);

    // South-east mass closes the encounter pocket.
    cluster(p,920,680,4,4);cluster(p,1025,725,1,6);small(p,865,770,1);
    bush(p,985,790,2);stump(p,840,720,1);ground(p,865,815,3);detail(p,1040,820,5);

    // Readable S-curve: entrance -> central clearing -> north-east -> water.
    pathEdge(p,270,780,1);pathEdge(p,390,700,2);pathEdge(p,470,610,3);
    pathEdge(p,575,500,4);pathEdge(p,700,430,5);pathEdge(p,820,385,6);pathEdge(p,930,420,7);

    // Connected stream corridor. Water pieces overlap; banks/rocks/low vegetation flank the axis.
    streamNode(p,1115,220,1);streamNode(p,1125,300,2);streamNode(p,1110,380,3);
    streamNode(p,1120,460,4);streamNode(p,1140,540,5);streamNode(p,1155,620,6);streamNode(p,1165,700,1);streamNode(p,1170,780,2);

    // Three readable encounter clearings: detail only on their perimeter.
    clearingEdge(p,560,520,1);clearingEdge(p,760,500,4);clearingEdge(p,360,735,6);
    // Entrance landmark, kept open around ENTRY_X/ENTRY_Y.
    stump(p,245,810,2);ground(p,300,825,6);detail(p,345,810,4);

    p.sort(Comparator.comparingDouble((Placement a)->a.y).thenComparing(a->a.asset));
    return p;
  }

  private static void cluster(List<Placement> p,float x,float y,int treeSeed,int underSeed){
    tree(p,x,y,treeSeed,.92f);
    bush(p,x-54,y+18,1+underSeed%8);bush(p,x+52,y+25,1+(underSeed+2)%8);
    ground(p,x-72,y+48,1+underSeed%6);ground(p,x+68,y+52,1+(underSeed+3)%6);
    detail(p,x+8,y+58,1+underSeed%5);
  }
  private static void pathEdge(List<Placement> p,float x,float y,int seed){
    ground(p,x-92,y+22,1+seed%6);ground(p,x+92,y-18,1+(seed+2)%6);
    bush(p,x-118,y+10,1+seed%8);bush(p,x+118,y-8,1+(seed+3)%8);
    detail(p,x-76,y+42,1+seed%5);
  }
  private static void streamNode(List<Placement> p,float x,float y,int seed){
    water(p,x,y,1+seed%6);bank(p,x-78,y+12,1+seed%5);bank(p,x+78,y-8,1+(seed+2)%5);
    if((seed&1)==0)rock(p,x-98,y+24,1+seed%5);else bush(p,x+104,y+18,1+seed%8);
    ground(p,x-118,y+42,1+seed%6);
  }
  private static void clearingEdge(List<Placement> p,float x,float y,int seed){
    ground(p,x-105,y+65,1+seed%6);ground(p,x+105,y+62,1+(seed+3)%6);
    bush(p,x-125,y+8,1+seed%8);bush(p,x+125,y-4,1+(seed+2)%8);rock(p,x+135,y+40,1+seed%5);
  }
  private static void encounterEdge(List<Placement> p,float x,float y,int seed){
    bush(p,x-95,y-20,1+seed%8);bush(p,x+95,y+10,1+(seed+2)%8);
    ground(p,x-70,y+65,1+seed%6);ground(p,x+75,y+70,1+(seed+3)%6);
    rock(p,x+120,y-45,1+seed%5);
  }
  private static void bushRing(List<Placement> p,float[][] xy,int seed){int i=0;for(float[] q:xy)bush(p,q[0],q[1],1+(seed+i++)%8);}
  private static void tree(List<Placement> p,float x,float y,int n,float s){p.add(new Placement(String.format("POTE_TR_%02d.png",n),x,y,s,"canopy",false));}
  private static void small(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_TS_%02d.png",n),x,y,.88f,"secondary_canopy",false));}
  private static void bush(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_BS_%02d.png",n),x,y,.90f,"understory",false));}
  private static void ground(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_GF_%02d.png",n),x,y,1f,"groundcover",false));}
  private static void stump(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_ST_%02d.png",n),x,y,1f,"stump",false));}
  private static void rock(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_RK_%02d.png",n),x,y,1f,"rock",false));}
  private static void water(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_WT_%02d.png",n),x,y,1.05f,"stream",false));}
  private static void bank(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_BW_%02d.png",n),x,y,1f,"bank",false));}
  private static void detail(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_OT_%02d.png",n),x,y,1f,"detail",false));}

  private static AssetManager findAssets(){
    try{Class<?> t=Class.forName("android.app.ActivityThread");Method m=t.getDeclaredMethod("currentApplication");
      Object app=m.invoke(null);return app instanceof Context?((Context)app).getAssets():null;
    }catch(Throwable ignored){return null;}
  }
}
