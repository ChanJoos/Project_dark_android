package com.projectdark.mobile.world;
import android.content.Context;import android.graphics.*;import java.io.*;import java.util.*;
/** Independent video-derived materials/objects, shared 64x32 world and bounded floor cache. */
public final class MineTownRenderer {
 private final Context context;private final Paint paint=new Paint();private final RectF dst=new RectF();private final Map<String,Bitmap> art=new HashMap<>();
 private static final int CHUNK=256,GUTTER=2,LIMIT=32;private final LinkedHashMap<Long,Bitmap> floor=new LinkedHashMap<>(40,.75f,true);private int builds;private Bitmap portal,contactShadow;private BitmapShader waterMaterial,grassMaterial,cliffMaterial;private final Matrix materialMatrix=new Matrix();private final Path shape=new Path();
 public MineTownRenderer(Context c){context=c;paint.setFilterBitmap(false);}
 private Bitmap image(String n){Bitmap b=art.get(n);if(b==null)try(InputStream s=context.getAssets().open((n.startsWith("meadow")||n.equals("flowers")?"mine-v206/":n.startsWith("bridge_")||n.startsWith("paving")||n.startsWith("shore")||n.equals("cave")||n.equals("tree")||n.startsWith("riverrock")||n.equals("water_flow")?"mine-v205/":"mine-v203/")+n+".png")){b=BitmapFactory.decodeStream(s);if(b==null)throw new IOException(n);b=MineSourceMatte.prepare(n,b);b.prepareToDraw();art.put(n,b);}catch(IOException e){throw new IllegalStateException("Missing Mine source asset "+n,e);}return b;}
 public void prepare(){prepareShadow();for(String n:new String[]{"grass","grass2","grass3","meadow0","meadow1","meadow2","water","planks","water_flow","cliff_material","grass_material","bridge_deck","bridge_back","bridge_front"})image(n);for(MineTownGeometry.Prop a:MineTownGeometry.PROPS)image(a.asset);if(portal==null)try(InputStream s=context.getAssets().open("assets/world/portal/portal_reagent_shop.webp")){portal=BitmapFactory.decodeStream(s);portal.prepareToDraw();}catch(IOException e){throw new IllegalStateException(e);}}
 private void prepareMaterials(){if(waterMaterial==null){waterMaterial=new BitmapShader(image("water_flow"),Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);grassMaterial=new BitmapShader(image("grass_material"),Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);cliffMaterial=new BitmapShader(image("cliff_material"),Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);}}
 public void releaseFloor(){floor.clear();}public int floorCacheEntries(){return floor.size();}public long floorCachePixelBytes(){long n=0;for(Bitmap b:floor.values())n+=b.getAllocationByteCount();return n;}public int floorBuildCount(){return builds;}
 public void draw(Canvas c,WorldRuntimeAdapter w,List<PoteFieldRenderer.ActorDraw> actors){float ox=w.camera().cameraX(),oy=w.camera().cameraY(),vw=w.camera().viewportWidth(),vh=w.camera().viewportHeight();c.drawColor(Color.BLACK);paint.setColor(Color.WHITE);paint.setAlpha(255);
  int nx=(int)Math.floor((ox+vw)/CHUNK)-(int)Math.floor(ox/CHUNK)+1,ny=(int)Math.floor((oy+vh)/CHUNK)-(int)Math.floor(oy/CHUNK)+1;
  if(nx*ny>LIMIT)ground(c,ox,oy,vw,vh);else for(int cy=(int)Math.floor(oy/CHUNK);cy<=(int)Math.floor((oy+vh)/CHUNK);cy++)for(int cx=(int)Math.floor(ox/CHUNK);cx<=(int)Math.floor((ox+vw)/CHUNK);cx++){
   long key=((long)cx<<32)^(cy&0xffffffffL);Bitmap b=floor.get(key);if(b==null){b=Bitmap.createBitmap(CHUNK+GUTTER*2,CHUNK+GUTTER*2,Bitmap.Config.ARGB_8888);ground(new Canvas(b),cx*CHUNK-GUTTER,cy*CHUNK-GUTTER,CHUNK+GUTTER*2,CHUNK+GUTTER*2);builds++;floor.put(key,b);if(floor.size()>LIMIT){Iterator<Map.Entry<Long,Bitmap>> it=floor.entrySet().iterator();it.next();it.remove();}}
   dst.set(cx*CHUNK-GUTTER-ox,cy*CHUNK-GUTTER-oy,(cx+1)*CHUNK+GUTTER-ox,(cy+1)*CHUNK+GUTTER-oy);c.drawBitmap(b,null,dst,paint);
  }
  List<PoteFieldRenderer.ActorDraw> order=new ArrayList<>(actors);
  for(int u:new int[]{19,43})for(int v=27;v<=37;v++){final int bu=u,bv=v;order.add(new PoteFieldRenderer.ActorDraw(MineTownGeometry.x(u+4,v),MineTownGeometry.y(u+4,v)+4,50,90,()->bridgeFront(c,ox,oy,bu,bv)));}
  for(MineTownGeometry.Prop a:MineTownGeometry.PROPS){if(a.asset.equals("cliff")||a.asset.startsWith("paving")||a.asset.startsWith("riverrock"))continue;float x=a.x-ox-a.anchorX,y=a.y-oy-a.anchorY;if(x-(a.asset.equals("cave")?135:0)>vw||x+a.width<0||y-(a.asset.equals("cave")?64:0)>vh||y+a.height<0)continue;if(a.asset.equals("cave")){order.add(new PoteFieldRenderer.ActorDraw(a.x,a.y-64,a.height,160,()->ridgeCap(c,a,x,y,ox,oy)));for(int strip=0;strip<13;strip++){final int k=strip;float foot=a.y+(32-k*16)*MineTownGeometry.SCALE;order.add(new PoteFieldRenderer.ActorDraw(a.x,foot,a.height,36,()->{c.save();c.clipRect(x+k*32*MineTownGeometry.SCALE,y,x+Math.min(398,(k+1)*32)*MineTownGeometry.SCALE,y+a.height);sprite(c,a.asset,x,y,a.width,a.height);c.restore();}));}}else order.add(new PoteFieldRenderer.ActorDraw(a.x,a.depthY,a.height,a.width,()->sprite(c,a.asset,x,y,a.width,a.height)));}
  order.sort(Comparator.comparingDouble(a->a.y));for(PoteFieldRenderer.ActorDraw a:order)a.draw.run();
 }
 private void ground(Canvas c,float ox,float oy,float vw,float vh){prepareMaterials();materialMatrix.setValues(new float[]{MineTownGeometry.SCALE,0,-ox,0,MineTownGeometry.SCALE,-oy,0,0,1});waterMaterial.setLocalMatrix(materialMatrix);grassMaterial.setLocalMatrix(materialMatrix);c.drawColor(Color.BLACK);
  for(int u=0;u<MineTownGeometry.SIZE;u++)for(int v=0;v<MineTownGeometry.SIZE;v++){float x=MineTownGeometry.x(u,v)-ox,y=MineTownGeometry.y(u,v)-oy;if(x< -33||x>vw+33||y< -17||y>vh+17)continue;int k=MineTownGeometry.tile(u,v);shape.reset();shape.moveTo(x,y-16.25f);shape.lineTo(x+32.25f,y);shape.lineTo(x,y+16.25f);shape.lineTo(x-32.25f,y);shape.close();paint.setShader(k==1?grassMaterial:waterMaterial);c.drawPath(shape,paint);paint.setShader(null);}
  // Source lawn patches overlap with feathered edges and irregular phase; no diamond colour grid.
  for(int u=9;u<55;u++)for(int v=9;v<55;v++){
   if(MineTownGeometry.tile(u,v)!=1||MineTownGeometry.bridge(u,v)||MineTownGeometry.tile(u-1,v)!=1||MineTownGeometry.tile(u+1,v)!=1||MineTownGeometry.tile(u,v-1)!=1||MineTownGeometry.tile(u,v+1)!=1)continue;
   int seed=(u*73856093)^(v*19349663);int n=Math.floorMod(seed,3);Bitmap b=image("meadow"+n);
   float x=MineTownGeometry.x(u,v)-ox+Math.floorMod(seed,13)-6,y=MineTownGeometry.y(u,v)-oy+Math.floorMod(seed/13,9)-4;
   float w=b.getWidth()*MineTownGeometry.SCALE*1.15f,h=b.getHeight()*MineTownGeometry.SCALE*1.15f;
   if(x+w/2<0||x-w/2>vw||y+h/2<0||y-h/2>vh)continue;dst.set(x-w/2,y-h/2,x+w/2,y+h/2);c.drawBitmap(b,null,dst,paint);
  }
  // Ground contact belongs below the source objects and actors, not above their feet.
  prepareShadow();for(MineTownGeometry.Prop a:MineTownGeometry.PROPS){float rx,ry;
   if(a.asset.equals("tree")){rx=16;ry=6;}else if(a.asset.equals("lamp")){rx=12;ry=4;}else if(a.asset.equals("flowers")){rx=11;ry=4;}else if(a.asset.equals("bench")){rx=23;ry=6;}else if(a.asset.startsWith("shore")||a.asset.startsWith("rock")){rx=24;ry=7;}else continue;
   dst.set(a.x-ox-rx,a.y-oy-ry,a.x-ox+rx,a.y-oy+ry);c.drawBitmap(contactShadow,null,dst,paint);
  }
  for(MineTownGeometry.Prop a:MineTownGeometry.PROPS)if(a.asset.startsWith("paving")||a.asset.startsWith("riverrock"))sprite(c,a.asset,a.x-ox-a.anchorX,a.y-oy-a.anchorY,a.width,a.height);
  for(int u:new int[]{19,43}){Matrix m=bridgeMatrix(ox,oy,u);c.drawBitmap(image("bridge_deck"),m,paint);c.drawBitmap(image("bridge_back"),m,paint);}
 }
 private Matrix bridgeMatrix(float ox,float oy,int u){Matrix m=new Matrix();float a=u-.5f,b=u+4.5f;float[] src={17,223,361,61,510,137},to={2048+(a-37)*32-ox,64+(a+37)*16-oy,2048+(a-27)*32-ox,64+(a+27)*16-oy,2048+(b-27)*32-ox,64+(b+27)*16-oy};m.setPolyToPoly(src,0,to,0,3);return m;}
 private void bridgeFront(Canvas c,float ox,float oy,int u,int v){c.save();c.concat(bridgeMatrix(ox,oy,u));float left=171+(37-v)*34; c.clipRect(left,0,v==27?550:left+34.1f,310);c.drawBitmap(image("bridge_front"),0,0,paint);c.restore();}
 /** Source material continuation closes the crop's unseen side; explicit adapted terrain join. */
 private void ridgeCap(Canvas c,MineTownGeometry.Prop a,float x,float y,float ox,float oy){prepareMaterials();float scale=MineTownGeometry.SCALE;
  materialMatrix.setValues(new float[]{scale,0,-ox,0,scale,-oy,0,0,1});grassMaterial.setLocalMatrix(materialMatrix);cliffMaterial.setLocalMatrix(materialMatrix);
  // A short grass plateau joins the source ridge crest instead of leaving a vertical cutout in water.
  shape.reset();shape.moveTo(x,y+29*scale);shape.lineTo(x+56*scale,y+8*scale);shape.lineTo(x+133*scale,y+8*scale);shape.lineTo(x+143*scale,y);shape.lineTo(x+252*scale,y);shape.lineTo(x+252*scale-128,y-64);shape.lineTo(x+143*scale-128,y-64);shape.lineTo(x-128,y+29*scale-64);shape.close();paint.setShader(grassMaterial);c.drawPath(shape,paint);paint.setShader(null);
  shape.reset();shape.moveTo(x-128,y+29*scale-64);shape.lineTo(x,y+29*scale);shape.lineTo(x,y+317*scale);shape.lineTo(x-128,y+317*scale-64);shape.lineTo(x-135,y+230*scale-64);shape.lineTo(x-124,y+140*scale-64);shape.close();paint.setShader(cliffMaterial);c.drawPath(shape,paint);paint.setShader(null);
 }
 private void prepareShadow(){if(contactShadow!=null)return;contactShadow=Bitmap.createBitmap(64,32,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(contactShadow);Paint p=new Paint();p.setShader(new RadialGradient(32,16,31,new int[]{0x650b1606,0x220b1606,0},new float[]{0,.42f,1},Shader.TileMode.CLAMP));c.save();c.scale(1,.5f,32,16);c.drawCircle(32,16,31,p);c.restore();contactShadow.prepareToDraw();}
 private void sprite(Canvas c,String n,float x,float y,float w,float h){dst.set(x,y,x+w,y+h);c.drawBitmap(image(n),null,dst,paint);}
}
