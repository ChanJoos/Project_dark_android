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
  public static final String STATUS="POTE_ASSET_DRIVEN_FOREST_V1";
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
    // Continuous authored-tone earth. Never stamp rectangular GD source patches into the live map:
    // Milles QA proved visible terrain seams are worse than restrained base terrain.
    pixel.setColor(0xff563b24);pixel.setStyle(Paint.Style.FILL);c.drawRect(0,0,c.getWidth(),c.getHeight(),pixel);
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
      if(b!=null&&!name.startsWith("POTE_GD_"))b=stripEdgeMatte(b);
    }catch(Throwable ignored){}
    cache.put(name,b);return b;
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
    // EAST STREAM: overlapping, gently meandering sequence. The earlier isolated water pockets
    // read as unrelated props; keep the water visually continuous and wrap it with bank/rock/moisture
    // vegetation so every water asset has a spatial reason to be there.
    water(p,1110,225,2); water(p,1125,305,4); water(p,1100,385,6); water(p,1115,465,3);
    water(p,1140,545,5); water(p,1160,625,1); water(p,1170,705,4);
    rock(p,1025,245,1);rock(p,1200,315,2);rock(p,1015,430,3);rock(p,1215,585,4);rock(p,1110,745,5);
    bank(p,1010,270,1);bank(p,1205,350,2);bank(p,1000,485,3);bank(p,1220,645,4);bank(p,1125,760,5);
    ground(p,990,305,2);ground(p,1230,400,5);ground(p,1030,555,4);ground(p,1225,720,6);
    bush(p,970,345,4);bush(p,1240,500,6);bush(p,1035,640,2);bush(p,1240,700,8);
    detail(p,1020,575,1);detail(p,1200,760,5);

    // NORTH/WEST FOREST WALL: large canopy -> small tree -> bush -> groundcover.
    tree(p,135,180,1,.78f);tree(p,245,150,2,.78f);tree(p,365,145,3,.78f);
    tree(p,500,130,4,.78f);tree(p,650,125,5,.78f);tree(p,820,125,6,.78f);
    tree(p,980,145,7,.78f);tree(p,1280,210,2,.78f);tree(p,1320,350,5,.78f);
    tree(p,1310,520,3,.78f);tree(p,1325,690,6,.78f);tree(p,1300,825,1,.78f);
    tree(p,110,330,4,.78f);tree(p,125,500,7,.78f);tree(p,145,650,2,.78f);
    bushRing(p,new float[][]{{190,215},{300,205},{430,200},{575,190},{735,185},{900,195},{1240,250},{1240,430},{1240,590},{1240,770}},1);

    // INTERNAL GROVE A: blocks direct sight, but leaves a south-east escape corridor.
    tree(p,300,300,5,.78f);tree(p,380,330,1,.78f);small(p,335,385,1);small(p,415,285,2);small(p,245,395,3);
    bushRing(p,new float[][]{{265,350},{330,420},{420,390},{455,335}},3);
    ground(p,285,410,1);ground(p,440,405,3);ground(p,315,455,5);detail(p,365,430,2);detail(p,455,455,4);

    // INTERNAL GROVE B: old-growth landmark around the twisted trunk.
    tree(p,540,560,6,.78f);tree(p,625,610,3,.78f);small(p,500,645,3);
    stump(p,470,590,3);stump(p,680,650,4);
    bushRing(p,new float[][]{{500,520},{590,520},{680,570},{620,690},{520,700}},5);
    ground(p,455,675,4);ground(p,700,610,5);bush(p,445,635,6);bush(p,715,665,4);detail(p,585,735,3);

    // NORTH-EAST GROVE, framing the approach to the stream.
    tree(p,785,245,7,.78f);tree(p,885,275,2,.78f);small(p,745,325,2);
    bushRing(p,new float[][]{{735,225},{835,205},{930,250},{950,335},{820,365}},7);
    ground(p,710,340,2);ground(p,940,370,6);ground(p,985,315,4);bush(p,990,255,5);rock(p,920,205,2);

    // SOUTH-EAST GROVE: dense wall behind the final encounter pocket.
    tree(p,930,670,4,.78f);tree(p,1020,720,1,.78f);small(p,885,760,1);
    bushRing(p,new float[][]{{875,650},{970,610},{1060,650},{1080,760},{950,805}},2);
    stump(p,850,705,1);ground(p,860,805,3);detail(p,1040,815,5);

    // ENCOUNTER POCKETS: deliberately open centres; peripheral detail gives visual enclosure.
    encounterEdge(p,580,385,1);encounterEdge(p,760,505,4);encounterEdge(p,360,735,6);
    // Entrance readability: sparse vegetation, landmark stump and flowers, no canopy over the spawn.
    stump(p,250,790,2);ground(p,285,820,6);detail(p,330,805,4);
    bush(p,165,735,8);bush(p,410,815,7);

    // Forest-floor variation and corridor-edge clusters. Keep the actual walking spine open,
    // but avoid the "objects floating on a brown board" look by chaining low vegetation along it.
    detail(p,520,330,1);detail(p,690,300,3);detail(p,830,445,2);detail(p,650,760,4);
    ground(p,470,460,2);ground(p,620,445,1);ground(p,845,555,5);ground(p,730,675,6);
    ground(p,560,470,3);ground(p,760,405,4);ground(p,790,620,2);ground(p,430,760,5);
    bush(p,505,485,2);bush(p,705,420,5);bush(p,805,590,3);bush(p,455,720,1);
    detail(p,600,475,5);detail(p,755,455,2);detail(p,785,665,4);detail(p,500,745,1);
    rock(p,545,280,1);rock(p,720,575,5);stump(p,800,585,1);

    p.sort(Comparator.comparingDouble((Placement a)->a.y).thenComparing(a->a.asset));
    return p;
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
