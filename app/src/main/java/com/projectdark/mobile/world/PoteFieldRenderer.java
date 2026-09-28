package com.projectdark.mobile.world;

import com.projectdark.mobile.CharacterRenderer;
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
  public static final String STATUS="POTE_FOREST_REFERENCE_GROUND_V10";
  private static final float TILE_W=64f,TILE_H=32f;
  private static final String SOIL_TEXTURE="video_reference/terrain/pote_forest_soil_v2.png";
  private static final String TRAIL_TEXTURE="video_reference/terrain/pote_dirt_path_fill_texture.png";
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

  /** Creek channel contains water and its authored crossing only. */
  public static int nonWaterCreekAnchorCount(){
    int count=0;float[][] line=PoteForestGeometry.creekCenterline();
    for(Placement p:AUTHORED_PLACEMENTS){
      if("water".equals(p.role)||"bridge".equals(p.role))continue;
      // Keep the complete visual footprint of plants and rocks outside the creek corridor.
      if(distanceToPolyline(p.x,p.y,line)<150f)count++;
    }
    return count;
  }
  private static float distanceToPolyline(float x,float y,float[][] line){
    float best=Float.MAX_VALUE;
    for(int i=1;i<line.length;i++){
      float ax=line[i-1][0],ay=line[i-1][1],dx=line[i][0]-ax,dy=line[i][1]-ay;
      float q=dx*dx+dy*dy,t=q==0?0:Math.max(0,Math.min(1,((x-ax)*dx+(y-ay)*dy)/q));
      best=Math.min(best,(float)Math.hypot(x-(ax+t*dx),y-(ay+t*dy)));
    }
    return best;
  }

  /** Paint the source-derived directional Pamfet idle/roll frames using the monster's live facing. */
  public void drawPamfet(Canvas c,CharacterRenderer.Direction direction,boolean attacking,
      float actionProgress,float idleClock,float x,float y){
    if(c==null)return;
    String dir=direction==CharacterRenderer.Direction.NW?"nw":
        direction==CharacterRenderer.Direction.NE?"ne":
        direction==CharacterRenderer.Direction.SW?"sw":"se";
    String name="pote/monsters/pamfet_"+dir+(attacking?"_roll.png":"_idle.png");
    Bitmap b=bitmap(name);if(b==null)return;
    float phase=attacking?(float)Math.sin(Math.PI*Math.max(0f,Math.min(1f,actionProgress))):0f;
    float dx=0f,dy=0f;
    switch(direction){
      case NW:dx=-.894f;dy=-.447f;break;
      case NE:dx=.894f;dy=-.447f;break;
      case SW:dx=-.894f;dy=.447f;break;
      case SE:dx=.894f;dy=.447f;break;
    }
    float bob=attacking?0f:(float)Math.sin(idleClock*5f)*1.2f;
    float cx=x+dx*phase*7f,cy=y+dy*phase*4f-bob;
    float w=64f,h=64f;
    pixel.setColor(0xffffffff);pixel.setAlpha(255);pixel.setFilterBitmap(false);
    c.save();if(attacking)c.rotate((direction==CharacterRenderer.Direction.NW||direction==CharacterRenderer.Direction.SE?-1f:1f)*phase*7f,cx,cy-20f);
    c.drawBitmap(b,null,new RectF(cx-w*.5f,cy-h+3f,cx+w*.5f,cy+3f),pixel);c.restore();
    pixel.setAlpha(255);
  }

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
    c.drawColor(0xff241d15);drawFloor(c,w);drawCreekBed(c,w);
    for(Placement p:placements)if(!"bridge".equals(p.role)&&("water".equals(p.role)||p.y<=actorY))drawPlacement(c,w,p);
    drawBridge(c,w);
  }

  /** Draw foreground canopies/props after the actor so Y-depth remains spatially believable. */
  public void drawAbove(Canvas c,WorldRuntimeAdapter w,float actorY){
    if(c==null||w==null)return;
    for(Placement p:placements)if(!"bridge".equals(p.role)&&!"water".equals(p.role)&&p.y>actorY)drawPlacement(c,w,p);
  }

  private void drawFloor(Canvas c,WorldRuntimeAdapter w){
    pixel.setStyle(Paint.Style.FILL);pixel.setColor(0xff533a29);c.drawRect(0,0,c.getWidth(),c.getHeight(),pixel);
    drawGroundTiles(c,w);
  }

  /** A broad, gently bending bare-earth lane leads from the arrival point to the footbridge. */
  private void drawTrail(Canvas c,WorldRuntimeAdapter w){
    Bitmap texture=bitmap(TRAIL_TEXTURE);if(texture==null)return;
    BitmapShader shader=new BitmapShader(texture,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
    Matrix phase=new Matrix();phase.setTranslate(-w.camera().cameraX(),-w.camera().cameraY());shader.setLocalMatrix(phase);
    pixel.setShader(shader);pixel.setStyle(Paint.Style.STROKE);pixel.setStrokeWidth(72f);
    pixel.setStrokeCap(Paint.Cap.ROUND);pixel.setStrokeJoin(Paint.Join.ROUND);
    Path trail=new Path();float[][] points=PoteForestGeometry.trailCenterline();
    WorldCameraTransform.Point[] screen=new WorldCameraTransform.Point[points.length];
    for(int i=0;i<points.length;i++)screen[i]=w.worldToScreen(points[i][0],points[i][1]);
    trail.moveTo(screen[0].x,screen[0].y);
    for(int i=0;i<screen.length-1;i++){
      WorldCameraTransform.Point p0=screen[Math.max(0,i-1)],p1=screen[i],p2=screen[i+1],p3=screen[Math.min(screen.length-1,i+2)];
      float c1x=p1.x+(p2.x-p0.x)/6f,c1y=p1.y+(p2.y-p0.y)/6f;
      float c2x=p2.x-(p3.x-p1.x)/6f,c2y=p2.y-(p3.y-p1.y)/6f;
      trail.cubicTo(c1x,c1y,c2x,c2y,p2.x,p2.y);
    }
    c.drawPath(trail,pixel);pixel.setShader(null);pixel.setStyle(Paint.Style.FILL);
  }

  /** Continuous narrow creek bed fills the seams between the authored water sprites. */
  private void drawCreekBed(Canvas c,WorldRuntimeAdapter w){
    float[][] points=PoteForestGeometry.creekCenterline();
    Path creek=new Path();WorldCameraTransform.Point[] screen=new WorldCameraTransform.Point[points.length];
    for(int i=0;i<points.length;i++)screen[i]=w.worldToScreen(points[i][0],points[i][1]);
    creek.moveTo(screen[0].x,screen[0].y);
    for(int i=0;i<screen.length-1;i++){
      WorldCameraTransform.Point p0=screen[Math.max(0,i-1)],p1=screen[i],p2=screen[i+1],p3=screen[Math.min(screen.length-1,i+2)];
      float c1x=p1.x+(p2.x-p0.x)/6f,c1y=p1.y+(p2.y-p0.y)/6f;
      float c2x=p2.x-(p3.x-p1.x)/6f,c2y=p2.y-(p3.y-p1.y)/6f;
      creek.cubicTo(c1x,c1y,c2x,c2y,p2.x,p2.y);
    }
    pixel.setShader(null);pixel.setStyle(Paint.Style.STROKE);pixel.setStrokeCap(Paint.Cap.ROUND);pixel.setStrokeJoin(Paint.Join.ROUND);
    pixel.setColor(0xff55442e);pixel.setStrokeWidth(46f);c.drawPath(creek,pixel);
    pixel.setColor(0xff72b5c4);pixel.setStrokeWidth(32f);c.drawPath(creek,pixel);
    pixel.setColor(0x997de0ea);pixel.setStrokeWidth(2f);c.drawPath(creek,pixel);
    pixel.setStyle(Paint.Style.FILL);
  }

  /** Each navigation-ground diamond samples one shared, source-video texture in world space. */
  private void drawGroundTiles(Canvas c,WorldRuntimeAdapter w){
    Bitmap soil=bitmap(SOIL_TEXTURE);if(soil==null)return;
    BitmapShader shader=new BitmapShader(soil,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
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
    if(dst.right>=0&&dst.left<=c.getWidth()&&dst.bottom>=0&&dst.top<=c.getHeight()){
      if("water".equals(p.role)){
        float angle=creekSegmentAngle(w,p.x,p.y);
        c.save();c.rotate(angle,q.x,q.y);c.drawBitmap(b,null,dst,pixel);c.restore();
      }else c.drawBitmap(b,null,dst,pixel);
    }
  }

  /** Align each source creek tile with the closest authored creek segment. */
  private float creekSegmentAngle(WorldRuntimeAdapter w,float x,float y){
    float[][] line=PoteForestGeometry.creekCenterline();float best=Float.MAX_VALUE,angle=0f;
    for(int i=1;i<line.length;i++){
      float ax=line[i-1][0],ay=line[i-1][1],dx=line[i][0]-ax,dy=line[i][1]-ay;
      float q=dx*dx+dy*dy,t=q==0?0:Math.max(0,Math.min(1,((x-ax)*dx+(y-ay)*dy)/q));
      float d=(float)Math.hypot(x-(ax+t*dx),y-(ay+t*dy));
      if(d<best){best=d;WorldCameraTransform.Point a=w.worldToScreen(ax,ay),b=w.worldToScreen(line[i][0],line[i][1]);angle=(float)Math.toDegrees(Math.atan2(b.y-a.y,b.x-a.x));}
    }
    return angle;
  }

  private Bitmap bitmap(String name){
    if(cache.containsKey(name))return cache.get(name);
    Bitmap b=null;if(assets!=null)try(InputStream in=assets.open(name)){
      BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);
      if(b!=null&&!SOIL_TEXTURE.equals(name)&&!TRAIL_TEXTURE.equals(name)&&!name.startsWith("pote/monsters/pamfet_")&&!name.startsWith("POTE_WATER_"))
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
    // Distinct patches keep some forest edges dense while the middle opens into a broad route.
    grove(p,190,185,2);grove(p,415,290,5);grove(p,250,620,6);grove(p,430,770,3);
    grove(p,700,190,1);grove(p,550,790,5);grove(p,1010,190,4);grove(p,850,590,2);
    grove(p,1700,130,7);grove(p,1605,665,4);grove(p,1730,390,1);

    // Low plants and groundcover mark both shoulders without closing the walking lane.
    float[][] trail=PoteForestGeometry.trailCenterline(),creek=PoteForestGeometry.creekCenterline();
    for(int i=1;i<trail.length-1;i++){
      float x=trail[i][0],y=trail[i][1];
      if(distanceToPolyline(x-8,y+112,creek)>=48f)ground(p,x-8,y+112,1+(i%6));
      if(distanceToPolyline(x+10,y-112,creek)>=48f)ground(p,x+10,y-112,1+((i+2)%6));
      if((i&1)==0){
        if(distanceToPolyline(x-32,y+145,creek)>=48f)bush(p,x-32,y+145,1+(i%8));
        if(distanceToPolyline(x+24,y-145,creek)>=48f)bush(p,x+24,y-145,1+((i+3)%8));
      }
    }

    // Water segments form the creek; the authored bridge is its only placed crossing.
    stream(p,PoteForestGeometry.creekCenterline());
    p.add(new Placement("POTE_BR_01.png",PoteForestGeometry.BRIDGE_X,PoteForestGeometry.BRIDGE_Y,.085f,"bridge",false));
    grove(p,1650,300,3);grove(p,1370,565,6);

    // Sparse landmarks vary by patch; the arrival clearing and bridge approach stay readable.
    stump(p,365,535,2);rock(p,510,590,4);small(p,305,505,2);
    stump(p,550,365,3);rock(p,910,670,2);small(p,870,690,3);
    rock(p,1570,300,5);small(p,1660,320,1);stump(p,1690,760,4);

    // Suppress all non-crossing props near the creek, including oversized canopy footprints.
    // Water PNGs are the only creek sprites; the authored bridge is the only crossing object.
    for(int i=p.size()-1;i>=0;i--){
      Placement item=p.get(i);
      if(!"water".equals(item.role)&&!"bridge".equals(item.role)
          &&distanceToPolyline(item.x,item.y,PoteForestGeometry.creekCenterline())<150f)p.remove(i);
    }
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
    int i=0;for(int segment=1;segment<pts.length;segment++){
      float[] a=pts[segment-1],b=pts[segment];float length=(float)Math.hypot(b[0]-a[0],b[1]-a[1]);
      int steps=Math.max(1,(int)Math.ceil(length/32f));
      for(int step=0;step<steps;step++,i++){
        float t=step/(float)steps,x=a[0]+(b[0]-a[0])*t,y=a[1]+(b[1]-a[1])*t;
        int waterId=1+(i%6);
        p.add(new Placement(String.format("POTE_WATER_%02d.png",waterId),x,y,.76f+(i%3)*.03f,"water",false));
      }
    }
    float[] end=pts[pts.length-1];p.add(new Placement("POTE_WATER_03.png",end[0],end[1],.78f,"water",false));
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
