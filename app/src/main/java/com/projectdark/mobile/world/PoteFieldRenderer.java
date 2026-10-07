package com.projectdark.mobile.world;

import com.projectdark.mobile.CharacterRenderer;
import com.projectdark.mobile.PoteForestMonsterShowcase;
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
  private static final String SOIL_TEXTURE="reference_v113/forest_floor.png";
  private static final String TRAIL_TEXTURE="video_reference/terrain/pote_dirt_path_fill_texture.png";
  private final Paint pixel=new Paint();
  private final Paint soilPaint=new Paint();
  private final Path groundCells=new Path();
  private final AssetManager assets=findAssets();
  private final Map<String,Bitmap> cache=new LinkedHashMap<>();
  private final Map<String,Bitmap> hitCache=new LinkedHashMap<>();
  private static final List<Placement> AUTHORED_PLACEMENTS=buildPlacements();
  private static final Map<String,List<Placement>> MAP_PLACEMENTS=new LinkedHashMap<>();

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

  public int placementCount(){return AUTHORED_PLACEMENTS.size();}

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

  /** Paint the registered generated candidate pose selected by the live test actor state and facing. */
  public void drawMonsterTestPose(Canvas c,String monsterId,String state,CharacterRenderer.Direction direction,
      float actionProgress,float idleClock,float x,float y){
    drawMonsterTestPose(c,monsterId,state,direction,actionProgress,idleClock,x,y,false);
  }

  /** Resolved damage uses the existing actor hit timer, retaining pose, alpha and foot anchor. */
  public void drawMonsterTestPose(Canvas c,String monsterId,String state,CharacterRenderer.Direction direction,
      float actionProgress,float idleClock,float x,float y,boolean hitFlash){
    if(c==null)return;
    String pose=("walk".equals(state)||"attack".equals(state))?state:"idle";
    String name=PoteForestMonsterShowcase.assetPath(monsterId,pose,direction);
    if(name==null)return;
    Bitmap b=bitmap(name);if(b==null)return;
    // Pamfets keep their small authored scale; the Lycan uses the player-sized frame.
    String species=PoteForestMonsterShowcase.species(monsterId);float h=PoteForestMonsterShowcase.bodyHeight(monsterId),w=h*b.getWidth()/Math.max(1f,b.getHeight());
    float cx=x,cy=y;
    if("idle".equals(pose)){
      cy-=(float)Math.sin(idleClock*4.5f)*.55f;
    }else if("walk".equals(pose)){
      // The art supplies one walk pose per facing. Add a visible two-beat gait while preserving
      // its direction-specific source image and the shared ground anchor.
      float gait=(float)Math.sin(idleClock*(2f*(float)Math.PI/CharacterRenderer.WALK_CYCLE_SECONDS));
      cy-=.5f+Math.abs(gait)*1.5f;
    }else{
      // Keep the four authored diagonal attack facings; animate a short forward strike/recoil
      // without rotating into unsupported cardinal/eight-way directions.
      float impulse=(float)Math.sin(Math.max(0f,Math.min(1f,actionProgress))*(float)Math.PI);
      cx+=facingX(direction)*1.6f*impulse;
      cy+=facingY(direction)*1.6f*impulse;
    }
    pixel.setColor(0xffffffff);pixel.setAlpha(255);pixel.setFilterBitmap(false);
    c.drawBitmap(hitFlash?hitBitmap(name,b):b,null,new RectF(cx-w*.5f,cy-h+3f,cx+w*.5f,cy+3f),pixel);
    pixel.setAlpha(255);
  }

  /** Cache a color-only variant: GPU color filters can bleed into transparent scaled edges. */
  private Bitmap hitBitmap(String name,Bitmap source){
    Bitmap hit=hitCache.get(name);if(hit!=null)return hit;
    int width=source.getWidth(),height=source.getHeight();int[] colors=new int[width*height];
    source.getPixels(colors,0,width,0,0,width,height);
    for(int i=0;i<colors.length;i++){
      int color=colors[i];if((color>>>24)==0)continue;
      int r=Math.min(255,((color>>>16)&255)+96),g=Math.min(255,((color>>>8)&255)+64),b=Math.min(255,(color&255)+64);
      colors[i]=(color&0xff000000)|(r<<16)|(g<<8)|b;
    }
    hit=source.copy(Bitmap.Config.ARGB_8888,true);hit.setPixels(colors,0,width,0,0,width,height);hitCache.put(name,hit);return hit;
  }

  private static float facingX(CharacterRenderer.Direction d){
    return d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW?-0.8944f:0.8944f;
  }
  private static float facingY(CharacterRenderer.Direction d){
    return d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE?-0.4472f:0.4472f;
  }

  /** Ground-contact collision footprints are derived from the same visible object anchors. */
  public static List<RectF> blockingFootprints(){
    return blockingFootprints(PoteFieldDef.MAP_ID);
  }
  public static List<RectF> blockingFootprints(String mapId){
    List<RectF> out=new ArrayList<>();
    for(Placement p:placementsForMap(mapId)){
      if("canopy".equals(p.role))out.add(new RectF(p.x-14f,p.y-10f,p.x+14f,p.y+10f));
      else if("rock".equals(p.role))out.add(new RectF(p.x-19f,p.y-15f,p.x+19f,p.y+15f));
      else if("stump".equals(p.role))out.add(new RectF(p.x-15f,p.y-10f,p.x+15f,p.y+10f));
    }
    return Collections.unmodifiableList(out);
  }

  public void draw(Canvas c,WorldRuntimeAdapter w){
    if(c==null||w==null)return;drawBelow(c,w,Float.POSITIVE_INFINITY);
  }

  public void drawPortalWorld(Canvas c,float x,float y){Bitmap b=bitmap("assets/world/portal/portal_reagent_shop.webp");if(b!=null){pixel.setAlpha(255);pixel.setColor(0xffffffff);c.drawBitmap(b,null,new RectF(x-32,y-22,x+32,y+10),pixel);}}
  /** Actor bounds/callbacks use world feet. Each tree fades only if its foreground pixels hide an actor. */
  public static final class ActorDraw {
    public final float x,y,height,width; public final Runnable draw;
    public ActorDraw(float x,float y,float height,float width,Runnable draw){this.x=x;this.y=y;this.height=height;this.width=width;this.draw=draw;}
  }
  private static final class DepthItem {
    final float y;final Runnable draw;
    DepthItem(float y,Runnable draw){this.y=y;this.draw=draw;}
  }
  private List<ActorDraw> visibleActors=Collections.emptyList();
  private float waterClock;
  public void tick(float dt){waterClock=(waterClock+Math.max(0,dt))%1024f;}
  private boolean campClearing(WorldRuntimeAdapter w,Placement p){return CampaignWorld.PIET.equals(w.runtime().currentMapId())&&!"water".equals(p.role)&&p.x>=540&&p.x<=1080&&p.y>=380&&p.y<=760;}
  public void drawScene(Canvas c,WorldRuntimeAdapter w,List<ActorDraw> actors){
    if(c==null||w==null)return;visibleActors=actors;
    drawFloor(c,w);drawCreekBed(c,w);
    List<DepthItem> depth=new ArrayList<>();
    for(Placement p:placementsForMap(w.runtime().currentMapId())){
      if(campClearing(w,p)||"water".equals(p.role))continue;
      if("groundcover".equals(p.role)||"bank".equals(p.role)||"bridge".equals(p.role))drawPlacement(c,w,p);
      else depth.add(new DepthItem(p.y,()->drawPlacement(c,w,p)));
    }
    for(ActorDraw actor:actors)depth.add(new DepthItem(actor.y,actor.draw));
    depth.sort(Comparator.comparingDouble(i->i.y));
    for(DepthItem item:depth)item.draw.run();visibleActors=Collections.emptyList();
  }
  public void drawBelow(Canvas c,WorldRuntimeAdapter w,float actorY){
    if(c==null||w==null)return;drawFloor(c,w);drawCreekBed(c,w);
    for(Placement p:placementsForMap(w.runtime().currentMapId()))if(!campClearing(w,p)&&!"water".equals(p.role)&&p.y<=actorY)drawPlacement(c,w,p);
    drawBridge(c,w);
  }
  public void drawAbove(Canvas c,WorldRuntimeAdapter w,float actorY){
    if(c==null||w==null)return;
    visibleActors=Collections.singletonList(new ActorDraw(w.presentationPlayerX(),actorY,52,30,()->{}));
    for(Placement p:placementsForMap(w.runtime().currentMapId()))if(!campClearing(w,p)&&!"bridge".equals(p.role)&&!"water".equals(p.role)&&p.y>actorY)drawPlacement(c,w,p);
    visibleActors=Collections.emptyList();
  }
  private void drawFloor(Canvas c,WorldRuntimeAdapter w){
    pixel.setStyle(Paint.Style.FILL);pixel.setColor(0xff533a29);c.drawRect(0,0,c.getWidth(),c.getHeight(),pixel);
    drawGroundTiles(c,w);drawTrail(c,w);
  }

  /** Feathered source earth patches overlap at irregular widths; no painted road border. */
  private void drawTrail(Canvas c,WorldRuntimeAdapter w){
    Bitmap texture=bitmap("reference_v113/trail_earth.png");if(texture==null)return;
    BitmapShader shader=new BitmapShader(texture,Shader.TileMode.MIRROR,Shader.TileMode.MIRROR);
    Matrix phase=new Matrix();phase.setTranslate(-w.camera().cameraX(),-w.camera().cameraY());shader.setLocalMatrix(phase);
    pixel.setShader(shader);pixel.setStyle(Paint.Style.STROKE);pixel.setStrokeCap(Paint.Cap.ROUND);pixel.setStrokeJoin(Paint.Join.ROUND);
    float[][] trail=PoteForestGeometry.trailCenterline(w.runtime().currentMapId());
    for(int i=1;i<trail.length;i++){
      float len=(float)Math.hypot(trail[i][0]-trail[i-1][0],trail[i][1]-trail[i-1][1]);int n=Math.max(1,(int)(len/32));
      for(int j=0;j<n;j++){
        float t=j/(float)n,t2=(j+1)/(float)n;
        WorldCameraTransform.Point p=w.worldToScreen(trail[i-1][0]+(trail[i][0]-trail[i-1][0])*t,trail[i-1][1]+(trail[i][1]-trail[i-1][1])*t);
        WorldCameraTransform.Point q=w.worldToScreen(trail[i-1][0]+(trail[i][0]-trail[i-1][0])*t2,trail[i-1][1]+(trail[i][1]-trail[i-1][1])*t2);
        float width=86+18*(float)Math.sin((i*n+j)*.37);
        // Many low-opacity shoulders soften into the same soil; original dirt colour, no gold stripe.
        for(int layer=5;layer>=0;layer--){pixel.setAlpha(layer==0?32:8);pixel.setStrokeWidth(width+layer*13);c.drawLine(p.x,p.y,q.x,q.y,pixel);}
      }
    }
    pixel.setShader(null);pixel.setStyle(Paint.Style.FILL);pixel.setColor(0xffffffff);pixel.setAlpha(255);
  }
  /** The visible river and the bank collision consume identical sampled centre/width data. */
  private void drawCreekBed(Canvas c,WorldRuntimeAdapter w){
    float[][] nodes=PoteForestGeometry.channel(w.runtime().currentMapId());if(nodes.length<2)return;
    Bitmap water=bitmap("reference_v113/water_current.png");if(water==null)return;
    BitmapShader shader=new BitmapShader(water,Shader.TileMode.MIRROR,Shader.TileMode.MIRROR);
    Matrix phase=new Matrix();phase.setTranslate(-w.camera().cameraX()+waterClock*2,-w.camera().cameraY()+waterClock*.6f);shader.setLocalMatrix(phase);
    Path surface=new Path();
    for(int side=1;side>=-1;side-=2)for(int k=0;k<nodes.length;k++){
      int i=side==1?k:nodes.length-1-k;
      float[] prev=nodes[Math.max(0,i-1)],next=nodes[Math.min(nodes.length-1,i+1)],a=nodes[i];
      float dx=next[0]-prev[0],dy=next[1]-prev[1],len=Math.max(1,(float)Math.hypot(dx,dy));
      WorldCameraTransform.Point q=w.worldToScreen(a[0]-dy/len*a[2]*side,a[1]+dx/len*a[2]*side);
      if(k==0&&side==1)surface.moveTo(q.x,q.y);else surface.lineTo(q.x,q.y);
    }
    Bitmap shore=bitmap("reference_v113/shore_material.png");
    if(shore!=null){BitmapShader bank=new BitmapShader(shore,Shader.TileMode.MIRROR,Shader.TileMode.MIRROR);Matrix bp=new Matrix();bp.setTranslate(-w.camera().cameraX(),-w.camera().cameraY());bank.setLocalMatrix(bp);pixel.setShader(bank);pixel.setColor(0xffffffff);pixel.setAlpha(105);pixel.setStyle(Paint.Style.STROKE);pixel.setStrokeWidth(11);pixel.setStrokeJoin(Paint.Join.ROUND);c.drawPath(surface,pixel);pixel.setShader(null);}
    surface.close();pixel.setColor(0xffffffff);pixel.setAlpha(255);pixel.setShader(shader);pixel.setStyle(Paint.Style.FILL);c.drawPath(surface,pixel);pixel.setShader(null);
    // Independent bank clusters: no mirrored pairs or evenly spaced stones.
    Bitmap rock=bitmap("reference_v113/shore_rock.png");if(rock!=null)for(int i=1;i<nodes.length-1;i++){
      float[] a=nodes[i],before=nodes[i-1],after=nodes[i+1];float dx=after[0]-before[0],dy=after[1]-before[1],len=Math.max(1,(float)Math.hypot(dx,dy));
      for(int side=-1;side<=1;side+=2){int seed=(i*1103515245+side*12345)&0x7fffffff;if(seed%13>3)continue;
        float offset=a[2]+2+(seed%7),jitter=((seed/13)%17)-8;WorldCameraTransform.Point q=w.worldToScreen(a[0]-dy/len*offset*side+dx/len*jitter,a[1]+dx/len*offset*side+dy/len*jitter);float scale=.62f+((seed/31)%9)*.08f;
        c.save();c.rotate(((seed/7)%19)-9,q.x,q.y);if((seed&1)==0){c.translate(q.x*2,0);c.scale(-1,1);}c.drawBitmap(rock,null,new RectF(q.x-24*scale,q.y-13*scale,q.x+24*scale,q.y+14*scale),pixel);c.restore();
      }
    }
    pixel.setColor(0xffffffff);pixel.setAlpha(255);
  }
  /** Each navigation-ground diamond samples one shared, source-video texture in world space. */
  private void drawGroundTiles(Canvas c,WorldRuntimeAdapter w){
    Bitmap soil=bitmap(SOIL_TEXTURE);if(soil==null)return;
    BitmapShader shader=new BitmapShader(soil,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
    Matrix phase=new Matrix();phase.setTranslate(-w.camera().cameraX(),-w.camera().cameraY());shader.setLocalMatrix(phase);
    soilPaint.setShader(shader);soilPaint.setColor(0xffffffff);soilPaint.setStyle(Paint.Style.FILL);
    groundCells.reset();
    for(WorldMoveTargetController.TileCenter tile:PoteCampaignMapDef.forId(w.runtime().currentMapId()).groundTiles()){
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
    for(Placement p:placementsForMap(w.runtime().currentMapId()))if("bridge".equals(p.role))drawPlacement(c,w,p);
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
      }else if("bridge".equals(p.role)){
        c.save();c.rotate(trailAngle(w.runtime().currentMapId(),p.x,p.y)-20f,q.x,q.y);c.drawBitmap(b,null,dst,pixel);c.restore();
      }else{
        pixel.setAlpha(occludesActor(p,b)?64:255);c.drawBitmap(b,null,dst,pixel);pixel.setAlpha(255);
      }
    }
  }

  private boolean occludesActor(Placement p,Bitmap b){
    if(!"canopy".equals(p.role)&&!"secondary_canopy".equals(p.role))return false;
    float ww=b.getWidth()*p.scale,hh=b.getHeight()*p.scale;
    for(ActorDraw a:visibleActors){
      if(a.y>=p.y||Math.abs(a.x-p.x)>ww*.5f+a.width*.5f||a.y<p.y-hh||a.y-a.height>p.y)continue;
      for(float yy=a.y-a.height;yy<a.y;yy+=5)for(float xx=a.x-a.width*.4f;xx<=a.x+a.width*.4f;xx+=5){
        int bx=(int)((xx-(p.x-ww*.5f))/p.scale),by=(int)((yy-(p.y-hh))/p.scale);
        if(bx>=0&&bx<b.getWidth()&&by>=0&&by<b.getHeight()&&(b.getPixel(bx,by)>>>24)>64)return true;
      }
    }
    return false;
  }
  /** Align each source creek tile with the closest authored creek segment. */
  private float creekSegmentAngle(WorldRuntimeAdapter w,float x,float y){
    float[][] line=PoteForestGeometry.creekCenterline(w.runtime().currentMapId());float best=Float.MAX_VALUE,angle=0f;
    if(line.length<2)return 0f;
    for(int i=1;i<line.length;i++){
      float ax=line[i-1][0],ay=line[i-1][1],dx=line[i][0]-ax,dy=line[i][1]-ay;
      float q=dx*dx+dy*dy,t=q==0?0:Math.max(0,Math.min(1,((x-ax)*dx+(y-ay)*dy)/q));
      float d=(float)Math.hypot(x-(ax+t*dx),y-(ay+t*dy));
      if(d<best){best=d;WorldCameraTransform.Point a=w.worldToScreen(ax,ay),b=w.worldToScreen(line[i][0],line[i][1]);angle=(float)Math.toDegrees(Math.atan2(b.y-a.y,b.x-a.x));}
    }
    return angle;
  }

  private static float trailAngle(String map,float x,float y){float[][] pts=PoteForestGeometry.trailCenterline(map);float best=Float.MAX_VALUE,angle=20;for(int i=1;i<pts.length;i++){float d=(float)Math.hypot(x-pts[i][0],y-pts[i][1]);if(d<best){best=d;angle=(float)Math.toDegrees(Math.atan2(pts[i][1]-pts[i-1][1],pts[i][0]-pts[i-1][0]));}}return angle;}
  private Bitmap bitmap(String name){
    if(cache.containsKey(name))return cache.get(name);
    Bitmap b=null;if(assets!=null)try(InputStream in=assets.open(name)){
      BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);
      if(b!=null&&!SOIL_TEXTURE.equals(name)&&!TRAIL_TEXTURE.equals(name)&&!name.startsWith("assets/world/portal/")&&!name.startsWith("pote/monsters/pamfet_")&&!name.startsWith(PoteForestMonsterShowcase.ASSET_ROOT)&&!name.startsWith("POTE_WATER_")&&!name.startsWith("reference_v113/"))
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
    return buildPlacements(PoteFieldDef.MAP_ID);
  }
  private static List<Placement> placementsForMap(String mapId){
    String key=mapId==null?PoteFieldDef.MAP_ID:mapId;
    synchronized(MAP_PLACEMENTS){List<Placement> cached=MAP_PLACEMENTS.get(key);if(cached!=null)return cached;List<Placement> made=buildPlacements(key);MAP_PLACEMENTS.put(key,made);return made;}
  }
  private static List<Placement> buildPlacements(String mapId){
    List<Placement> p=new ArrayList<>();PoteCampaignMapDef d=PoteCampaignMapDef.forId(mapId);
    float[][] trail=PoteForestGeometry.trailCenterline(mapId),creek=PoteForestGeometry.creekCenterline(mapId);
    // Authored macro patches vary by map. Jitter only distributes source sprites inside each patch.
    float[][] patches="MAP_POTE_02".equals(mapId)?new float[][]{{.12f,.16f},{.36f,.23f},{.63f,.13f},{.87f,.32f},{.2f,.64f},{.46f,.71f},{.72f,.81f},{.91f,.66f}}:
      "MAP_POTE_03".equals(mapId)?new float[][]{{.1f,.17f},{.31f,.12f},{.6f,.2f},{.88f,.17f},{.16f,.48f},{.37f,.65f},{.75f,.5f},{.93f,.73f},{.21f,.87f},{.62f,.86f}}:
      new float[][]{{.13f,.14f},{.36f,.12f},{.7f,.12f},{.91f,.29f},{.12f,.51f},{.38f,.57f},{.73f,.48f},{.9f,.74f},{.21f,.85f},{.58f,.88f}};
    java.util.List<float[]> macro=new ArrayList<>(java.util.Arrays.asList(patches));
    for(int i=0;i<patches.length;i++){float[] a=patches[i];macro.add(new float[]{Math.max(.06f,Math.min(.94f,a[0]+((i%2==0)?.12f:-.13f))),Math.max(.08f,Math.min(.92f,a[1]+((i%3==0)?.18f:-.12f)))});}
    if("MAP_POTE_01".equals(mapId)||CampaignWorld.PIET.equals(mapId)||"MAP_POTE_04".equals(mapId)){
      macro.add(new float[]{.105f,.28f});macro.add(new float[]{.215f,.12f});macro.add(new float[]{.30f,.35f});
    }
    int seed=0;
    for(float[] patch:macro){float x=d.minX+(d.maxX-d.minX)*patch[0],y=d.minY+(d.maxY-d.minY)*patch[1];
      // Sparse and dense groves alternate, leaving large irregular encounter clearings.
      for(int k=0;k<5+seed%4;k++){float angle=k*2.39996f+seed,radius=38+28*(k%4);float xx=x+(float)Math.cos(angle)*radius*1.65f,yy=y+(float)Math.sin(angle)*radius;
        if(distanceToPolyline(xx,yy,creek)<150||distanceToPolyline(xx,yy,trail)<105)continue;
        tree(p,xx,yy,k+seed,.88f+(k%3)*.11f);if((k&1)==0)bush(p,xx-53,yy+25,1+k%8);ground(p,xx+51,yy+21,1+k%6);
      }seed++;
    }
    // Landmark groves beside the arrival/route turns keep extended hunting areas woodland,
    // while the clear lane itself stays walkable. Alternating shoulders avoid two canopy rows.
    if(!CampaignWorld.BOSS_D.equals(mapId))for(int i=1;i<trail.length;i++){
      if(i%3==0)continue;float[] a=trail[i-1],b=trail[i];float dx=b[0]-a[0],dy=b[1]-a[1],len=Math.max(1,(float)Math.hypot(dx,dy)),side=(i%2==0?1:-1);
      for(int k=0;k<3;k++){float xx=b[0]-dy/len*(215+k*24)*side+(k-1)*58,yy=b[1]+dx/len*(215+k*24)*side+(k-1)*27;
        if(xx<d.minX+95||xx>d.maxX-95||yy<d.minY+100||yy>d.maxY-90||distanceToPolyline(xx,yy,creek)<150||distanceToPolyline(xx,yy,trail)<105)continue;
        tree(p,xx,yy,i+k,.9f+(k%2)*.13f);
      }
    }
    // Ground vegetation is irregular throughout the floor, not two repeated rows along a painted road.
    for(float y=d.minY+140;y<d.maxY-100;y+=116)for(float x=d.minX+130;x<d.maxX-100;x+=164){
      int h=Math.floorMod((int)x*13+(int)y*31,997);float xx=x+(h%97)-48,yy=y+((h/7)%59)-29;
      if(distanceToPolyline(xx,yy,creek)<150)continue;
      if(distanceToPolyline(xx,yy,trail)<50&&h%3!=0)continue;
      ground(p,xx,yy,1+h%6);if(h%7==0)bush(p,xx+35,yy-25,1+h%8);
      if(h%29==0)rock(p,xx-25,yy+20,1+h%5);if(h%43==0)stump(p,xx+27,yy+18,1+h%4);
    }
    if(creek.length>1){stream(p,creek);p.add(new Placement("POTE_BR_01.png",PoteForestGeometry.bridgeX(mapId),PoteForestGeometry.bridgeY(mapId),.12f,"bridge",false));}
    for(int i=p.size()-1;i>=0;i--){Placement a=p.get(i);if(!"water".equals(a.role)&&!"bridge".equals(a.role)&&distanceToPolyline(a.x,a.y,creek)<150)p.remove(i);}
    p.sort(Comparator.comparingDouble((Placement a)->a.y).thenComparing(a->a.asset));return Collections.unmodifiableList(p);
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
  private static void tree(List<Placement> p,float x,float y,int n,float s){p.add(new Placement("reference_v113/tree_"+((n&1)==0?"willow":"oak")+".png",x,y,s,"canopy",false));}
  private static void oak(List<Placement> p,float x,float y,float scale){p.add(new Placement("POTE_TR_08.png",x,y,scale,"canopy",false));}
  private static void small(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_TS_%02d.png",n),x,y,.96f,"secondary_canopy",false));}
  private static void bush(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_BS_%02d.png",n),x,y,.72f+(n%4)*.05f,"understory",false));}
  private static void ground(List<Placement> p,float x,float y,int n){p.add(new Placement("reference_v113/"+(n%3==0?"flowers":"fern")+".png",x,y,.88f,"groundcover",false));}
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
