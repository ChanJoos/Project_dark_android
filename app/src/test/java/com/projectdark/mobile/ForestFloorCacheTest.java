package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ForestFloorCacheTest {
 private WorldRuntimeAdapter world(String map){RuntimeState state=new RuntimeState();state.enterCampaignMap(map,false);PoteCampaignMapDef d=PoteCampaignMapDef.forId(map);return new WorldRuntimeAdapter(state,320,256,d.minX,d.maxX,d.minY,d.maxY,d.navigationTiles(),d.obstacles());}
 private void cached(PoteFieldRenderer r,Canvas c,WorldRuntimeAdapter w)throws Exception{Method draw=PoteFieldRenderer.class.getDeclaredMethod("drawFloor",Canvas.class,WorldRuntimeAdapter.class);draw.setAccessible(true);draw.invoke(r,c,w);}
 @Test public void cachedFloorKeepsLegacyWorldTextureAndChunkSeamsAcrossAllMaps()throws Exception{
  double worstMean=0;int maximum=0;
  for(String map:new String[]{"MAP_POTE_01","MAP_POTE_02","MAP_POTE_03",CampaignWorld.BOSS_D}){
   WorldRuntimeAdapter w=world(map);PoteFieldRenderer r=new PoteFieldRenderer();LegacyFloor old=new LegacyFloor();float[][] trail=PoteForestGeometry.trailCenterline(map);
   for(int spot:new int[]{1,trail.length/2,trail.length-1})for(float fraction:new float[]{0,.25f,.75f}){
    float cx=(float)Math.floor(trail[spot][0]-160)+fraction,cy=(float)Math.floor(trail[spot][1]-128)+fraction;
    w.camera().snapTo(cx+w.camera().anchorX(),cy+w.camera().anchorY());
    Bitmap a=Bitmap.createBitmap(320,256,Bitmap.Config.ARGB_8888),b=Bitmap.createBitmap(320,256,Bitmap.Config.ARGB_8888);old.drawFloor(new Canvas(a),w);cached(r,new Canvas(b),w);
    int[] expected=new int[320*256],actual=new int[expected.length];a.getPixels(expected,0,320,0,0,320,256);b.getPixels(actual,0,320,0,0,320,256);long error=0;int localMax=0;
    for(int i=0;i<expected.length;i++){assertEquals(255,actual[i]>>>24);for(int shift:new int[]{0,8,16}){int delta=Math.abs(((expected[i]>>shift)&255)-((actual[i]>>shift)&255));error+=delta;localMax=Math.max(localMax,delta);}}
    double mean=error/(double)(expected.length*3);worstMean=Math.max(worstMean,mean);maximum=Math.max(maximum,localMax);
    System.out.println("FLOOR_RASTER_CASE="+map+"; FRACTION="+fraction+"; MEAN_RGB_ERROR="+mean+"; MAX_CHANNEL_ERROR="+localMax);
    if(mean>=worstMean){java.io.File folder=new java.io.File("build/reports/forest-performance");folder.mkdirs();try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(folder,"floor-legacy-worst.png"))){a.compress(Bitmap.CompressFormat.PNG,100,out);}try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(folder,"floor-cached-worst.png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
    a.recycle();b.recycle();
   }
  }
  System.out.println("FLOOR_LEGACY_RASTER_WORST_MEAN_RGB_ERROR="+worstMean+"; MAX_CHANNEL_ERROR="+maximum);
  assertTrue("world-aligned floor, no cache-grid line; worst mean RGB error="+worstMean,worstMean<1.0);
 }
 @Test public void stationaryFloorReusesChunksAndMapTravelHasBoundedMemory()throws Exception{
  PoteFieldRenderer r=new PoteFieldRenderer();WorldRuntimeAdapter w=world("MAP_POTE_01");Bitmap image=Bitmap.createBitmap(320,256,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);
  w.camera().snapTo(1600,900);cached(r,canvas,w);int builds=((Number)ForestPixelCacheTest.field(r,"floorChunkBuilds")).intValue();for(int i=0;i<100;i++)cached(r,canvas,w);assertEquals(builds,((Number)ForestPixelCacheTest.field(r,"floorChunkBuilds")).intValue());
  for(int i=0;i<40;i++){w.camera().snapTo(100+i*90,200+i*37);cached(r,canvas,w);assertTrue(((Map<?,?>)ForestPixelCacheTest.field(r,"floorChunks")).size()<=32);}
  w=world("MAP_POTE_03");w.camera().snapTo(1500,800);cached(r,canvas,w);assertEquals("MAP_POTE_03",ForestPixelCacheTest.field(r,"floorMap"));assertTrue(((Map<?,?>)ForestPixelCacheTest.field(r,"floorChunks")).size()<=6);
  System.out.println("FLOOR_RETAINED_PIXEL_BYTES_MAX="+(32*260*260*4)+"; STATIC_REPEAT_FRAMES=100; REBUILDS=0");
 }
 // Frozen V115 fe8dc5d2 ground renderer, independent of the chunk implementation.
 private static class LegacyFloor {
  final Paint pixel=new Paint(),soilPaint=new Paint();final Matrix texturePhase=new Matrix();final PoteFieldRenderer sources=new PoteFieldRenderer();final Map<String,BitmapShader> shaders=new HashMap<>();final String SOIL_TEXTURE="reference_v113/forest_floor.png";
  private Bitmap bitmap(String name){try{Method load=PoteFieldRenderer.class.getDeclaredMethod("bitmap",String.class);load.setAccessible(true);return (Bitmap)load.invoke(sources,name);}catch(Exception ex){throw new AssertionError(ex);}}
  private BitmapShader shader(String name,Bitmap b,Shader.TileMode mode){return shaders.computeIfAbsent(name,n->new BitmapShader(b,mode,mode));}
  private void drawFloor(Canvas c,WorldRuntimeAdapter w){
    pixel.setStyle(Paint.Style.FILL);pixel.setColor(0xff533a29);c.drawRect(0,0,c.getWidth(),c.getHeight(),pixel);
    drawGroundTiles(c,w);drawTrail(c,w);
  }
  private void drawGroundTiles(Canvas c,WorldRuntimeAdapter w){
    Bitmap soil=bitmap(SOIL_TEXTURE);if(soil==null)return;
    BitmapShader shader=shader("soil",soil,Shader.TileMode.REPEAT);
    texturePhase.setTranslate(-w.camera().cameraX(),-w.camera().cameraY());shader.setLocalMatrix(texturePhase);
    soilPaint.setShader(shader);soilPaint.setColor(0xffffffff);soilPaint.setStyle(Paint.Style.FILL);
    PoteCampaignMapDef d=PoteCampaignMapDef.forId(w.runtime().currentMapId());
    c.drawRect(d.minX-32-w.camera().cameraX(),d.minY-16-w.camera().cameraY(),d.maxX+32-w.camera().cameraX(),d.maxY+16-w.camera().cameraY(),soilPaint);
    soilPaint.setShader(null);
  }
  private void drawTrail(Canvas c,WorldRuntimeAdapter w){
    Bitmap texture=bitmap("reference_v113/trail_earth.png");if(texture==null)return;
    BitmapShader shader=shader("trail",texture,Shader.TileMode.MIRROR);
    Matrix phase=texturePhase;phase.setTranslate(-w.camera().cameraX(),-w.camera().cameraY());shader.setLocalMatrix(phase);
    pixel.setShader(shader);pixel.setStyle(Paint.Style.STROKE);pixel.setStrokeCap(Paint.Cap.ROUND);pixel.setStrokeJoin(Paint.Join.ROUND);
    float[][] trail=PoteForestGeometry.trailCenterline(w.runtime().currentMapId());
    for(int i=1;i<trail.length;i++){
      float len=(float)Math.hypot(trail[i][0]-trail[i-1][0],trail[i][1]-trail[i-1][1]);int n=Math.max(1,(int)(len/32));
      for(int j=0;j<n;j++){
        float t=j/(float)n,t2=(j+1)/(float)n;
        WorldCameraTransform.Point p=w.worldToScreen(trail[i-1][0]+(trail[i][0]-trail[i-1][0])*t,trail[i-1][1]+(trail[i][1]-trail[i-1][1])*t);
        WorldCameraTransform.Point q=w.worldToScreen(trail[i-1][0]+(trail[i][0]-trail[i-1][0])*t2,trail[i-1][1]+(trail[i][1]-trail[i-1][1])*t2);
        if(Math.max(p.x,q.x)<-200||Math.min(p.x,q.x)>c.getWidth()+200||Math.max(p.y,q.y)<-200||Math.min(p.y,q.y)>c.getHeight()+200)continue;
        float width=86+18*(float)Math.sin((i*n+j)*.37);
        // Many low-opacity shoulders soften into the same soil; original dirt colour, no gold stripe.
        for(int layer=5;layer>=0;layer--){pixel.setAlpha(layer==0?32:8);pixel.setStrokeWidth(width+layer*13);c.drawLine(p.x,p.y,q.x,q.y,pixel);}
      }
    }
    pixel.setShader(null);pixel.setStyle(Paint.Style.FILL);pixel.setColor(0xffffffff);pixel.setAlpha(255);
  }
 }
}
