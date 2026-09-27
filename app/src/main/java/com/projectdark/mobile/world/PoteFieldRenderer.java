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
    // Forest floor is continuous. A worn S-corridor is drawn as overlapping short segments,
    // never as giant isolated ovals; damp ground follows the stream axis on the east.
    pixel.setStyle(Paint.Style.FILL);pixel.setStrokeCap(Paint.Cap.ROUND);pixel.setStrokeJoin(Paint.Join.ROUND);
    pixel.setColor(0xff3f4b27);c.drawRect(0,0,c.getWidth(),c.getHeight(),pixel);
    terrainStroke(c,w,new float[][]{{190,810},{300,770},{390,700},{470,610},{575,520},{690,445},{805,390},{930,420}},102f,0xff50503d);
    terrainStroke(c,w,new float[][]{{1045,190},{1070,300},{1055,410},{1080,520},{1100,635},{1110,790}},132f,0xff465337);
    // Small clearing pads merge into the corridor rather than reading as separate painted islands.
    terrainStroke(c,w,new float[][]{{500,520},{560,520},{620,520}},132f,0xff5b543f);
    terrainStroke(c,w,new float[][]{{700,500},{760,500},{820,500}},128f,0xff625642);
    terrainStroke(c,w,new float[][]{{305,735},{360,735},{415,735}},126f,0xff625642);
  }
  private void terrainStroke(Canvas c,WorldRuntimeAdapter w,float[][] pts,float width,int color){
    if(pts.length<2)return;pixel.setColor(color);pixel.setStrokeWidth(width);pixel.setStyle(Paint.Style.STROKE);
    for(int i=1;i<pts.length;i++){WorldCameraTransform.Point a=w.worldToScreen(pts[i-1][0],pts[i-1][1]),b=w.worldToScreen(pts[i][0],pts[i][1]);c.drawLine(a.x,a.y,b.x,b.y,pixel);}
    pixel.setStyle(Paint.Style.FILL);
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
    return Bitmap.createBitmap(src,5,4,src.getWidth()-10,src.getHeight()-8);
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
    Bitmap out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);out.setPixels(px,0,w,0,0,w,h);return out;
  }

  private static List<Placement> buildPlacements(){
    List<Placement> p=new ArrayList<>();
    // V2 is authored as masses, not independent props: each canopy parent receives understory,
    // groundcover and detail children. Open centres are intentionally preserved for navigation.
    cluster(p,130,185,1,1);cluster(p,245,155,2,3);cluster(p,365,145,3,5);cluster(p,490,140,4,7);cluster(p,615,145,6,2);
    cluster(p,745,145,5,2);cluster(p,875,150,6,4);cluster(p,1000,170,7,6);cluster(p,1120,185,3,1);
    cluster(p,1250,220,2,8);cluster(p,1285,365,5,1);cluster(p,1305,500,3,3);cluster(p,1310,660,6,5);cluster(p,1280,820,1,7);
    cluster(p,105,320,4,2);cluster(p,100,475,7,4);cluster(p,120,640,2,6);cluster(p,135,790,3,8);cluster(p,220,455,5,7);cluster(p,205,590,1,2);

    // West grove: one continuous mass, with a deliberate east-facing opening.
    cluster(p,285,315,5,3);cluster(p,385,335,1,5);small(p,330,395,1);small(p,445,300,2);
    bush(p,250,375,6);ground(p,305,430,2);ground(p,420,415,5);ground(p,365,445,3);

    // Old-growth central grove / landmark mass.
    cluster(p,435,610,6,6);cluster(p,715,655,3,1);small(p,475,690,3);stump(p,455,615,3);stump(p,690,655,4);
    bush(p,555,705,5);ground(p,490,725,4);rock(p,705,715,3);

    // North-east mass frames the path before the stream.
    cluster(p,770,245,7,7);cluster(p,925,275,2,2);small(p,750,345,2);
    bush(p,845,355,4);rock(p,955,390,4);rock(p,980,250,2);

    // South-east mass closes the encounter pocket.
    cluster(p,895,700,4,4);cluster(p,1045,745,1,6);small(p,865,770,1);
    bush(p,1000,800,2);stump(p,835,730,1);rock(p,880,820,5);

    // Readable S-curve: entrance -> central clearing -> north-east -> water.
    pathEdge(p,270,780,1);pathEdge(p,390,700,2);pathEdge(p,470,610,3);
    pathEdge(p,575,500,4);pathEdge(p,700,430,5);pathEdge(p,820,385,6);pathEdge(p,930,420,7);

    // Connected stream corridor. Water pieces overlap; banks/rocks/low vegetation flank the axis.
    streamNode(p,1100,220,1);streamNode(p,1125,270,2);streamNode(p,1105,320,3);streamNode(p,1130,370,4);
    streamNode(p,1110,420,5);streamNode(p,1140,470,6);streamNode(p,1120,520,1);streamNode(p,1150,570,2);
    streamNode(p,1135,620,3);streamNode(p,1165,670,4);streamNode(p,1145,720,5);streamNode(p,1175,770,6);rock(p,1030,785,5);stump(p,1260,790,4);

    // Three readable encounter clearings: detail only on their perimeter.
    clearingEdge(p,560,520,1);clearingEdge(p,760,500,4);clearingEdge(p,360,735,6);
    // Entrance landmark, kept open around ENTRY_X/ENTRY_Y.
    stump(p,250,830,2);rock(p,365,825,1);

    p.sort(Comparator.comparingDouble((Placement a)->a.y).thenComparing(a->a.asset));
    return p;
  }

  private static void cluster(List<Placement> p,float x,float y,int treeSeed,int underSeed){
    tree(p,x,y,treeSeed,.86f+(underSeed%3)*.05f);
    bush(p,x-62-(underSeed%3)*9,y+18+(underSeed%2)*7,1+underSeed%8);bush(p,x+58+(underSeed%4)*8,y+25-(underSeed%2)*6,1+(underSeed+2)%8);
    ground(p,x-88-(underSeed%2)*12,y+48+(underSeed%3)*6,1+underSeed%6);ground(p,x+76+(underSeed%3)*11,y+54-(underSeed%2)*5,1+(underSeed+3)%6);
    if(underSeed%4==0)stump(p,x+24,y+68,1+underSeed%4);
  }
  private static void pathEdge(List<Placement> p,float x,float y,int seed){
    ground(p,x-118,y+28,1+seed%6);ground(p,x+118,y-24,1+(seed+2)%6);
    bush(p,x-148,y+12,1+seed%8);bush(p,x+148,y-10,1+(seed+3)%8);
    if(seed%4==0)rock(p,x-110,y+50,1+seed%5);
  }
  private static void streamNode(List<Placement> p,float x,float y,int seed){
    float wobble=((seed%3)-1)*18f; water(p,x+wobble,y,1+seed%6);
    bank(p,x-104+wobble,y+16,1+seed%5);bank(p,x+108+wobble,y-12,1+(seed+2)%5);
    if(seed%3==0)rock(p,x-112+wobble,y+28,1+seed%5);
    else if(seed%3==1)bush(p,x+118+wobble,y+20,1+seed%8);
    if((seed&1)==0)ground(p,x-148+wobble,y+50,1+seed%6);
  }
  private static void clearingEdge(List<Placement> p,float x,float y,int seed){
    ground(p,x-118-seed*2,y+68,1+seed%6);ground(p,x+104+seed*3,y+58,1+(seed+3)%6);
    bush(p,x-142,y+12+seed,1+seed%8);bush(p,x+132,y-8-seed,1+(seed+2)%8);
    if((seed&1)==0)rock(p,x+148,y+44,1+seed%5);
  }
  private static void encounterEdge(List<Placement> p,float x,float y,int seed){
    bush(p,x-95,y-20,1+seed%8);bush(p,x+95,y+10,1+(seed+2)%8);
    ground(p,x-70,y+65,1+seed%6);ground(p,x+75,y+70,1+(seed+3)%6);
    rock(p,x+120,y-45,1+seed%5);
  }
  private static void bushRing(List<Placement> p,float[][] xy,int seed){int i=0;for(float[] q:xy)bush(p,q[0],q[1],1+(seed+i++)%8);}
  private static void tree(List<Placement> p,float x,float y,int n,float s){p.add(new Placement(String.format("POTE_TR_%02d.png",n),x,y,s,"canopy",false));}
  private static void small(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_TS_%02d.png",n),x,y,.88f,"secondary_canopy",false));}
  private static void bush(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_BS_%02d.png",n),x,y,.84f,"understory",false));}
  private static void ground(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_GF_%02d.png",n),x,y,.88f,"groundcover",false));}
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
