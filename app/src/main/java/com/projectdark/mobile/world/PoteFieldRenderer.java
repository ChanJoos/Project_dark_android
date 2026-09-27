package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Rect;
import android.graphics.Shader;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
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
  public static final String STATUS="POTE_FOREST_REFERENCE_GROUND_V9";
  private static final float TILE_W=64f,TILE_H=32f;
  private static final String SOIL_TEXTURE="video_reference/terrain/pote_forest_soil_v2.png";
  private final Paint pixel=new Paint();
  private final Paint soilPaint=new Paint();
  private final Path groundCells=new Path();
  private final AssetManager assets=findAssets();
  private final Map<String,Bitmap> cache=new LinkedHashMap<>();
  private static final List<Placement> AUTHORED_PLACEMENTS=buildPlacements();
  private final List<Placement> placements=AUTHORED_PLACEMENTS;

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

  /** Ground-contact collision footprints are derived from the same visible object anchors. */
  public static List<RectF> blockingFootprints(){
    List<RectF> out=new ArrayList<>();
    for(Placement p:AUTHORED_PLACEMENTS){
      if("canopy".equals(p.role))out.add(new RectF(p.x-14f,p.y-10f,p.x+14f,p.y+10f));
      else if("rock".equals(p.role))out.add(new RectF(p.x-19f,p.y-15f,p.x+19f,p.y+15f));
      else if("stump".equals(p.role))out.add(new RectF(p.x-15f,p.y-10f,p.x+15f,p.y+10f));
    }
    return Collections.unmodifiableList(out);
  }

  public void draw(Canvas c,WorldRuntimeAdapter w){
    if(c==null||w==null)return;drawBelow(c,w,Float.POSITIVE_INFINITY);
  }

  /** Draw terrain and objects whose ground anchors are behind the supplied actor depth. */
  public void drawBelow(Canvas c,WorldRuntimeAdapter w,float actorY){
    if(c==null||w==null)return;
    c.drawColor(0xff241d15);drawFloor(c,w);
    for(Placement p:placements)if(!"bridge".equals(p.role)&&p.y<=actorY)drawPlacement(c,w,p);
    drawBridge(c,w);
  }

  /** Draw foreground canopies/props after the actor so Y-depth remains spatially believable. */
  public void drawAbove(Canvas c,WorldRuntimeAdapter w,float actorY){
    if(c==null||w==null)return;
    for(Placement p:placements)if(!"bridge".equals(p.role)&&p.y>actorY)drawPlacement(c,w,p);
  }

  private void drawFloor(Canvas c,WorldRuntimeAdapter w){
    pixel.setStyle(Paint.Style.FILL);pixel.setColor(0xff533a29);c.drawRect(0,0,c.getWidth(),c.getHeight(),pixel);
    drawGroundTiles(c,w);
  }

  /** Each navigation-ground diamond samples one shared, source-video texture in world space. */
  private void drawGroundTiles(Canvas c,WorldRuntimeAdapter w){
    Bitmap soil=bitmap(SOIL_TEXTURE);if(soil==null)return;
    BitmapShader shader=new BitmapShader(soil,Shader.TileMode.MIRROR,Shader.TileMode.MIRROR);
    Matrix phase=new Matrix();phase.setTranslate(-w.camera().cameraX(),-w.camera().cameraY());shader.setLocalMatrix(phase);
    soilPaint.setShader(shader);soilPaint.setColor(0xffffffff);soilPaint.setStyle(Paint.Style.FILL);
    groundCells.reset();
    for(WorldMoveTargetController.TileCenter tile:PoteFieldDef.groundTiles()){
      WorldCameraTransform.Point point=w.worldToScreen(tile.x,tile.y);
      if(point.x<-40||point.x>c.getWidth()+40||point.y<-24||point.y>c.getHeight()+24)continue;
      groundCells.moveTo(point.x,point.y-16f);groundCells.lineTo(point.x+32f,point.y);
      groundCells.lineTo(point.x,point.y+16f);groundCells.lineTo(point.x-32f,point.y);groundCells.close();
    }
    c.save();c.clipPath(groundCells);c.drawRect(0,0,c.getWidth(),c.getHeight(),soilPaint);c.restore();
    soilPaint.setShader(null);
  }
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

  /** Load the authored PNG bridge after stream water and before the actor. */
  private void drawBridge(Canvas c,WorldRuntimeAdapter w){
    for(Placement p:placements)if("bridge".equals(p.role))drawPlacement(c,w,p);
  }

  private void drawPlacement(Canvas c,WorldRuntimeAdapter w,Placement p){
    Bitmap b=bitmap(p.asset);if(b==null)return;
    WorldCameraTransform.Point q=w.worldToScreen(p.x,p.y);
    if(p.tile){
      RectF dst=new RectF(q.x-TILE_W*.5f,q.y-TILE_H*.5f,q.x+TILE_W*.5f,q.y+TILE_H*.5f);
      c.drawBitmap(b,null,dst,pixel);return;
    }
    float ww=b.getWidth()*p.scale,hh=b.getHeight()*p.scale;
    boolean center="water".equals(p.role)||"bridge".equals(p.role);
    RectF dst=center?new RectF(Math.round(q.x-ww*.5f),Math.round(q.y-hh*.5f),Math.round(q.x+ww*.5f),Math.round(q.y+hh*.5f)):
        new RectF(Math.round(q.x-ww*.5f),Math.round(q.y-hh),Math.round(q.x+ww*.5f),Math.round(q.y));
    if(dst.right>=0&&dst.left<=c.getWidth()&&dst.bottom>=0&&dst.top<=c.getHeight())c.drawBitmap(b,null,dst,pixel);
  }

  private Bitmap bitmap(String name){
    if(cache.containsKey(name))return cache.get(name);
    Bitmap b=null;if(assets!=null)try(InputStream in=assets.open(name)){
      BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);
      if(b!=null&&!SOIL_TEXTURE.equals(name))
        b=name.equals("POTE_BR_01.png")||name.equals("POTE_TR_08.png")
            ?trimSourceEdge(b):trimSourceEdge(stripEdgeMatte(b));
      if(b!=null&&name.startsWith("POTE_GD_"))b=softenGroundTileRim(b);
    }catch(Throwable ignored){}
    cache.put(name,b);return b;
  }

  private static Bitmap trimSourceEdge(Bitmap src){
    if(src==null||src.getWidth()<=8||src.getHeight()<=8)return src;
    // Source extraction occasionally leaves a 1-2 px crop/separator rule on the outer edge.
    // Trim two pixels symmetrically; anchor remains effectively unchanged at gameplay scale.
    return Bitmap.createBitmap(src,5,4,src.getWidth()-10,src.getHeight()-8);
  }

  /** Replaces the extracted black beveled edge with shared earth color so adjacent diamonds read as one floor. */
  private static Bitmap softenGroundTileRim(Bitmap src){
    int w=src.getWidth(),h=src.getHeight(),minX=w,minY=h,maxX=-1,maxY=-1;
    int[] px=new int[w*h];src.getPixels(px,0,w,0,0,w,h);
    for(int y=0;y<h;y++)for(int x=0;x<w;x++)if((px[y*w+x]>>>24)>12){minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);}
    if(maxX<=minX||maxY<=minY)return src;
    float cx=(minX+maxX)*.5f,cy=(minY+maxY)*.5f,rx=(maxX-minX)*.5f,ry=(maxY-minY)*.5f;
    for(int y=minY;y<=maxY;y++)for(int x=minX;x<=maxX;x++){
      int i=y*w+x,c=px[i],a=c>>>24;if(a==0)continue;
      float edge=Math.abs(x-cx)/rx+Math.abs(y-cy)/ry;
      if(edge<.90f)continue;
      // Soften only the narrow extracted bevel; preserve the tile's soil and moss details.
      float mix=Math.min(.98f,(edge-.90f)*10f);
      int r=(c>>>16)&255,g=(c>>>8)&255,b=c&255;
      r=Math.round(r*(1-mix)+105*mix);g=Math.round(g*(1-mix)+70*mix);b=Math.round(b*(1-mix)+46*mix);
      px[i]=(a<<24)|(r<<16)|(g<<8)|b;
    }
    Bitmap out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);out.setPixels(px,0,w,0,0,w,h);return out;
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
    // Build an enclosing canopy first, then fill the flanks around one broad irregular trail.
    // Positions are staggered and jittered so the forest reads as connected growth, not rows.
    int n=0;
    for(int x=34;x<=1740;x+=104){
      float wobble=((n*37)%31)-15;
      tree(p,x+wobble,154+((n*19)%35),1+(n%7),.91f+(n%3)*.07f);
      if((n&1)==0)tree(p,x+47-wobble*.35f,223+((n*23)%37),1+((n+3)%7),.78f+(n%4)*.06f);
      if(n%3!=1)small(p,x-27,248+((n*13)%34),1+n%3);
      n++;
    }
    n=0;for(int y=260;y<=850;y+=92){
      float wobble=((n*29)%35)-17;
      if(y<748)tree(p,102+wobble,y,1+(n%7),.9f+(n%3)*.06f);
      tree(p,1700-wobble,y+31,1+((n+4)%7),.89f+(n%4)*.05f);
      if((n&1)==0){small(p,178+wobble,y+48,1+n%3);small(p,1630-wobble,y+66,1+(n+1)%3);}
      n++;
    }
    n=0;for(int x=220;x<=1690;x+=132){
      if(x>=380)tree(p,x+((n*17)%29)-14,876-((n*11)%28),1+(n%7),.8f+(n%3)*.07f);n++;
    }

    // Interior canopy islands wrap around the trail. Each mass combines overlapping crowns,
    // small trees and understory; clear centers remain large enough for movement and combat.
    grove(p,178,365,2);grove(p,365,262,5);grove(p,650,205,1);grove(p,1198,294,4);
    grove(p,185,520,6);grove(p,438,812,3);grove(p,765,804,5);grove(p,1080,835,2);
    grove(p,1350,700,7);grove(p,240,430,4);grove(p,1320,810,1);grove(p,170,370,3);
    grove(p,520,280,6);grove(p,1120,380,2);grove(p,1660,680,5);
    // Video-reviewed broad-branch oaks add the separated rounded crowns seen in the reference.
    oak(p,365,320,.30f);oak(p,1080,330,.30f);oak(p,1640,700,.30f);

    // One diagonal woodland creek, assembled from the production water/bank sprites.
    stream(p,new float[][]{{820,700},{862,674},{904,648},{946,622},{988,596},{1030,570},{1072,544},{1114,518},{1156,492},{1198,466},{1240,440},{1282,414},{1324,388},{1366,362},{1408,336},{1450,310},{1492,284},{1534,258},{1576,232},{1618,206},{1660,180},{1702,154}});

    // One clear, playable footbridge crosses the creek and connects the eastern trail.
    p.add(new Placement("POTE_BR_01.png",1324,388,.145f,"bridge",false));
    ground(p,1490,500,3);ground(p,1580,540,6);bush(p,1455,565,4);rock(p,1648,558,2);

    // Landmarks sit at decision spaces, never in the centre of the walking corridor.
    stump(p,350,780,2);rock(p,448,745,4);small(p,334,770,2);
    stump(p,400,430,3);rock(p,821,566,2);small(p,723,601,3);
    rock(p,1028,376,5);small(p,1070,346,1);stump(p,1225,706,4);

    p.sort(Comparator.comparingDouble((Placement a)->a.y).thenComparingInt(a->"bridge".equals(a.role)?1:0).thenComparing(a->a.asset));
    return p;
  }

  /** A compact asymmetric grove: canopy anchors grouped around understory and ground details. */
  private static void grove(List<Placement> p,float x,float y,int seed){
    tree(p,x-78,y+18,1+Math.floorMod(seed,7),.82f);
    tree(p,x-21,y-25,1+Math.floorMod(seed+2,7),.94f);
    tree(p,x+52,y+8,1+Math.floorMod(seed+4,7),1.0f);
    tree(p,x+111,y+44,1+Math.floorMod(seed+5,7),.78f);
    small(p,x-102,y-29,1+Math.floorMod(seed,3));small(p,x+5,y-86,1+Math.floorMod(seed+1,3));
    bush(p,x-99,y+69,1+seed%8);bush(p,x-8,y+61,1+(seed+3)%8);bush(p,x+92,y+84,1+(seed+5)%8);
    ground(p,x-126,y+93,1+seed%6);ground(p,x+132,y+83,1+(seed+2)%6);
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
    int i=0;for(float[] q:pts){
      p.add(new Placement("POTE_WT_01.png",q[0],q[1],.95f,"water",false));
      if(i%2==0){bank(p,q[0]-22,q[1]+38,1+(i%5));bank(p,q[0]+27,q[1]-39,1+((i+2)%5));}
      if(i%4==0)rock(p,q[0]-56,q[1]+50,1+(i%5));
      if(i%4==2)bush(p,q[0]+60,q[1]-57,1+(i%8));
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
  private static void oak(List<Placement> p,float x,float y,float scale){p.add(new Placement("POTE_TR_08.png",x,y,scale,"canopy",false));}
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
