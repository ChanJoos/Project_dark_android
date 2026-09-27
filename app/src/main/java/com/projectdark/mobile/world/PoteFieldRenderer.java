package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.BitmapShader;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
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
  public static final String STATUS="POTE_FOREST_MASS_V3";
  private static final float TILE_W=64f,TILE_H=32f;
  private static final String SOIL_TEXTURE="video_reference/terrain/dirt_path_fill_texture.png";
  private static final int[] FOREST_GROUND_TILES={1,2,3,4,9,10};
  private static final int[] CLEARING_GROUND_TILES={5,6,7,8};
  private final Paint pixel=new Paint();
  private final Paint soilPaint=new Paint();
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
    soilPaint.setAntiAlias(false);soilPaint.setFilterBitmap(false);soilPaint.setDither(false);
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
    // Continuous soil underlay plus a tightly packed field of source-derived forest-floor
    // diamonds. The former sparse scatter left most of the map looking like empty brown void.
    pixel.setStyle(Paint.Style.FILL);pixel.setColor(0xff40502d);c.drawRect(0,0,c.getWidth(),c.getHeight(),pixel);
    Bitmap soil=bitmap(SOIL_TEXTURE);
    if(soil!=null){soilPaint.setShader(new BitmapShader(soil,Shader.TileMode.MIRROR,Shader.TileMode.MIRROR));c.drawRect(0,0,c.getWidth(),c.getHeight(),soilPaint);soilPaint.setShader(null);}
    drawGroundTiles(c,w);
    // A shallow, asset-textured stream hugs the eastern boundary and leaves a direct trail
    // from the south-west entry to the northern encounter clearing.
    terrainStroke(c,w,new float[][]{{1230,145},{1260,225},{1225,305},{1268,385},{1232,465},{1272,545},{1237,625},{1278,705},{1244,785},{1280,850}},104f,0xff416f6d);
    terrainStroke(c,w,new float[][]{{1234,150},{1253,230},{1232,310},{1260,390},{1240,470},{1264,550},{1245,630},{1270,710},{1250,790},{1275,850}},26f,0xff60918c);
  }

  /** Source-derived forest-floor cutouts add uneven leaf litter over the seamless soil substrate. */
  private void drawGroundTiles(Canvas c,WorldRuntimeAdapter w){
    float stepX=58f,stepY=34f,cx=w.camera().cameraX(),cy=w.camera().cameraY();
    int firstRow=(int)Math.floor((cy-64f)/stepY),lastRow=(int)Math.ceil((cy+c.getHeight()+64f)/stepY);
    for(int row=firstRow;row<=lastRow;row++){
      float y=row*stepY;float rowOffset=(Math.floorMod(row,2)==0?0f:stepX*.5f);
      int firstCol=(int)Math.floor((cx-64f-rowOffset)/stepX),lastCol=(int)Math.ceil((cx+c.getWidth()+64f-rowOffset)/stepX);
      for(int col=firstCol;col<=lastCol;col++){
        int seed=tileSeed(col,row);
        float jitterX=(unit(seed)-.5f)*5f,jitterY=(unit(seed^0x45d9f3b)-.5f)*4f;
        float wx=col*stepX+rowOffset+jitterX,wy=y+jitterY;
        boolean path=Math.abs(wy-(790f-(wx-190f)*.43f))<78f&&wx>150f&&wx<1120f;
        int[] palette=path?CLEARING_GROUND_TILES:FOREST_GROUND_TILES;
        int asset=palette[Math.floorMod(seed>>>4,palette.length)];
        float scale=.49f+unit(seed^0x27d4eb2d)*.055f;
        drawGroundPatch(c,w,String.format("POTE_GD_%02d.png",asset),wx,wy,scale);
      }
    }
  }
  private static int tileSeed(int col,int row){return col*73856093^row*19349663;}
  private static float unit(int n){int h=n^(n>>>16);h*=0x7feb352d;h^=h>>>15;h*=0x846ca68b;h^=h>>>16;return (h&0x7fffffff)/(float)Integer.MAX_VALUE;}
  private void drawGroundPatch(Canvas c,WorldRuntimeAdapter w,String name,float wx,float wy,float scale){
    Bitmap b=bitmap(name);if(b==null)return;WorldCameraTransform.Point p=w.worldToScreen(wx,wy);
    float width=b.getWidth()*scale,height=b.getHeight()*scale;
    RectF dst=new RectF(Math.round(p.x-width*.5f),Math.round(p.y-height*.5f),Math.round(p.x+width*.5f),Math.round(p.y+height*.5f));
    if(dst.right>=0&&dst.left<=c.getWidth()&&dst.bottom>=0&&dst.top<=c.getHeight())c.drawBitmap(b,null,dst,pixel);
  }
  private void terrainStroke(Canvas c,WorldRuntimeAdapter w,float[][] pts,float width,int color){
    if(pts.length<2)return;pixel.setColor(color);pixel.setStrokeWidth(width);pixel.setStrokeCap(Paint.Cap.ROUND);pixel.setStrokeJoin(Paint.Join.ROUND);pixel.setStyle(Paint.Style.STROKE);
    for(int i=1;i<pts.length;i++){WorldCameraTransform.Point a=w.worldToScreen(pts[i-1][0],pts[i-1][1]),b=w.worldToScreen(pts[i][0],pts[i][1]);c.drawLine(a.x,a.y,b.x,b.y,pixel);}pixel.setStyle(Paint.Style.FILL);
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
      if(b!=null)b=trimSourceEdge(stripEdgeMatte(b));
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
    // Rebuilt from a readable route outward: continuous perimeter canopy, distinct groves,
    // and an open south-west to north-east travel lane. Avoid the old evenly repeated masses.
    int n=0;
    for(int x=40;x<=1390;x+=118){tree(p,x,138+(n%3)*13,1+(n%7),.86f+(n%3)*.08f);if(n%2==0)small(p,x+34,177+(n%2)*12,1+(n%3));n++;}
    n=0;for(int y=260;y<=820;y+=112){tree(p,128+(n%2)*24,y,1+(n%7),.88f+(n%3)*.07f);tree(p,1350-(n%2)*20,y+24,1+((n+3)%7),.90f+(n%2)*.08f);n++;}
    for(int x=255;x<=1240;x+=165)tree(p,x,876+(x%2)*6,1+(x/165)%7,.84f+(x%3)*.05f);

    // Separate wooded pockets flank the playable lane instead of spilling across it.
    grove(p,320,390,2);grove(p,430,650,4);grove(p,640,300,1);
    grove(p,770,640,5);grove(p,930,430,3);grove(p,1085,690,6);
    grove(p,1040,230,4);
    shoulder(p,300,740,1);shoulder(p,570,505,3);shoulder(p,890,545,2);

    // Connected eastern-edge stream with bank hierarchy. Keep the entry-to-clearing trail open.
    stream(p,new float[][]{{1230,205},{1260,270},{1225,335},{1268,400},{1232,465},{1270,530},{1238,595},{1275,660},{1245,725},{1280,790}});

    // Landmarks sit at decision spaces, never in the centre of the walking corridor.
    stump(p,240,815,2);rock(p,430,780,4);small(p,340,835,2);
    stump(p,500,590,3);rock(p,820,725,2);small(p,695,680,3);
    rock(p,970,360,5);small(p,1000,325,1);stump(p,1185,775,4);

    p.sort(Comparator.comparingDouble((Placement a)->a.y).thenComparing(a->a.asset));
    return p;
  }

  /** A compact asymmetric grove: canopy anchors grouped around understory and ground details. */
  private static void grove(List<Placement> p,float x,float y,int seed){
    tree(p,x-48,y+8,1+Math.floorMod(seed,7),.88f);
    tree(p,x+14,y-34,1+Math.floorMod(seed+2,7),1.02f);
    tree(p,x+74,y+2,1+Math.floorMod(seed+4,7),.91f);
    small(p,x-8,y-66,1+Math.floorMod(seed,3));
    bush(p,x-62,y+55,1+seed%6);bush(p,x+60,y+61,1+(seed+3)%6);
    ground(p,x-92,y+76,1+seed%6);ground(p,x+102,y+82,1+(seed+2)%6);
  }

  private static void mass(List<Placement> p,float[][] pts,int seed){
    int i=0; for(float[] q:pts){
      int n=1+(seed+i*2)%7; float x=q[0],y=q[1];
      tree(p,x,y,n,1.10f+((seed+i)%3)*.07f);
      tree(p,x-44+((seed+i)%3)*20,y-38,1+(seed+i+3)%7,1.02f+((seed+i)%2)*.10f);
      if(i%3==0)small(p,x+64-((seed+i)%3)*22,y+30,1+(seed+i)%3);
      bush(p,x-48-(i%2)*16,y+48,1+(seed+i)%8);
      bush(p,x+54+(i%3)*10,y+52,1+(seed+i+3)%8);
      ground(p,x-72,y+64,1+(seed+i)%6);
      if(i%2==0)ground(p,x+74,y+68,1+(seed+i+2)%6);
      i++;
    }
  }
  private static void shoulder(List<Placement> p,float x,float y,int seed){
    bush(p,x-122,y+18,1+seed%8); ground(p,x-88,y+48,1+seed%6); ground(p,x+82,y+52,1+(seed+3)%6);
    if((seed&1)==0)rock(p,x+110,y+24,1+seed%5); else stump(p,x+112,y+30,1+seed%4);
  }
  private static void stream(List<Placement> p,float[][] pts){
    // Water V2: sparse overlapping water surfaces, continuous banks. Avoid the old vertical PNG chain.
    int i=0; for(float[] q:pts){
      float x=q[0]+((i%5)-2f)*15f,y=q[1];
      if((i&1)==0){
        int sprite=1+(i/2)%3;
        p.add(new Placement(String.format("POTE_WT_%02d.png",sprite),x,y,1.22f+(i%3)*.08f,"water",false));
      }
      bank(p,x-86,y+18,1+i%5); bank(p,x+90,y-14,1+(i+2)%5);
      if(i%3==0)rock(p,x-108,y+30,1+i%5);
      if(i%3==1)ground(p,x+112,y+38,1+i%6);
      i++;
    }
  }

  private static void cluster(List<Placement> p,float x,float y,int treeSeed,int underSeed){
    tree(p,x,y,treeSeed,.86f+(underSeed%3)*.05f);
    bush(p,x-78-(underSeed%3)*10,y+30+(underSeed%2)*8,1+underSeed%8);bush(p,x+76+(underSeed%4)*9,y+34-(underSeed%2)*6,1+(underSeed+2)%8);
    ground(p,x-88-(underSeed%2)*12,y+48+(underSeed%3)*6,1+underSeed%6);ground(p,x+76+(underSeed%3)*11,y+54-(underSeed%2)*5,1+(underSeed+3)%6);
    if(underSeed%4==0)stump(p,x+24,y+68,1+underSeed%4);
  }
  private static void pathEdge(List<Placement> p,float x,float y,int seed){
    ground(p,x-118,y+28,1+seed%6);ground(p,x+118,y-24,1+(seed+2)%6);
    bush(p,x-148,y+12,1+seed%8);bush(p,x+148,y-10,1+(seed+3)%8);
    if(seed%4==0)rock(p,x-110,y+50,1+seed%5);
  }
  private static void streamNode(List<Placement> p,float x,float y,int seed){
    float wobble=((seed%4)-1.5f)*26f; p.add(new Placement(String.format("POTE_WT_%02d.png",1+seed%6),x+wobble,y,.78f+(seed%3)*.09f,"water",false));
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
  private static void small(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_TS_%02d.png",n),x,y,.96f,"secondary_canopy",false));}
  private static void bush(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_BS_%02d.png",n),x,y,.72f+(n%4)*.05f,"understory",false));}
  private static void ground(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_GF_%02d.png",n),x,y,.88f,"groundcover",false));}
  private static void stump(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_ST_%02d.png",n),x,y,1f,"stump",false));}
  private static void rock(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_RK_%02d.png",n),x,y,.76f+(n%3)*.07f,"rock",false));}
  private static void water(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_WT_%02d.png",n),x,y,1.05f,"stream",false));}
  private static void bank(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_BW_%02d.png",n),x,y,1f,"bank",false));}
  private static void detail(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_OT_%02d.png",n),x,y,1f,"detail",false));}

  private static AssetManager findAssets(){
    try{Class<?> t=Class.forName("android.app.ActivityThread");Method m=t.getDeclaredMethod("currentApplication");
      Object app=m.invoke(null);return app instanceof Context?((Context)app).getAssets():null;
    }catch(Throwable ignored){return null;}
  }
}
