package com.projectdark.mobile.world;
import android.content.Context;import android.graphics.*;
import java.io.*;import java.util.*;
/** Independent tiles and registered source sprites with a bounded floor cache. */
public final class AbelTownRenderer {
 private static final int CHUNK=256,GUTTER=2,LIMIT=32;
 private final Context context;private final Paint p=new Paint();private final RectF dst=new RectF();private final Map<String,Bitmap> assets=new HashMap<>();private final LinkedHashMap<Long,Bitmap> chunks=new LinkedHashMap<>(32,.75f,true);private String map="";private long floorBuilds;public long floorBuildCount(){return floorBuilds;}
 private static final String[] TILE={"tile_white","tile_brick_rosette","tile_blue_water","tile_rock_paving","tile_sea_water","tile_grass"};
 public AbelTownRenderer(Context c){context=c;p.setFilterBitmap(false);p.setAntiAlias(false);}
 private Bitmap image(String name){Bitmap b=assets.get(name);if(b==null)try(InputStream in=context.getAssets().open("abel-video-v147/"+name+".png")){b=BitmapFactory.decodeStream(in);if(b==null)throw new IOException(name);b.prepareToDraw();assets.put(name,b);}catch(IOException e){throw new IllegalStateException("Missing registered Abel asset "+name,e);}return b;}
 public void prepare(){for(String t:TILE)image(t);for(AbelTownGeometry.Prop a:AbelTownGeometry.PROPS)image(a.asset);for(AbelTownGeometry.Prop a:AbelTownGeometry.EAST_PROPS)image(a.asset);}
 public int floorCacheEntries(){return chunks.size();}public long floorCachePixelBytes(){long n=0;for(Bitmap b:chunks.values())n+=b.getAllocationByteCount();return n;}
 private void select(String id){if(id.equals(map))return;chunks.clear();map=id;}
 public void draw(Canvas c,WorldRuntimeAdapter w,List<PoteFieldRenderer.ActorDraw> actors){select(w.runtime().currentMapId());float ox=w.camera().cameraX(),oy=w.camera().cameraY(),vw=w.camera().viewportWidth(),vh=w.camera().viewportHeight();c.drawColor(0xffc6c7ce);
  int countX=(int)Math.floor((ox+vw)/CHUNK)-(int)Math.floor(ox/CHUNK)+1,countY=(int)Math.floor((oy+vh)/CHUNK)-(int)Math.floor(oy/CHUNK)+1;
  if(countX*countY>LIMIT)floor(c,ox,oy,vw,vh);else for(int cy=(int)Math.floor(oy/CHUNK);cy<=(int)Math.floor((oy+vh)/CHUNK);cy++)for(int cx=(int)Math.floor(ox/CHUNK);cx<=(int)Math.floor((ox+vw)/CHUNK);cx++){
   long key=((long)cx<<32)^(cy&0xffffffffL);Bitmap b=chunks.get(key);if(b==null){b=Bitmap.createBitmap(CHUNK+GUTTER*2,CHUNK+GUTTER*2,Bitmap.Config.ARGB_8888);floor(new Canvas(b),cx*CHUNK-GUTTER,cy*CHUNK-GUTTER,CHUNK+GUTTER*2,CHUNK+GUTTER*2);floorBuilds++;chunks.put(key,b);if(chunks.size()>LIMIT){Iterator<Map.Entry<Long,Bitmap>> it=chunks.entrySet().iterator();it.next();it.remove();}}
   dst.set(cx*CHUNK-GUTTER-ox,cy*CHUNK-GUTTER-oy,(cx+1)*CHUNK+GUTTER-ox,(cy+1)*CHUNK+GUTTER-oy);c.drawBitmap(b,null,dst,p);
  }
  List<PoteFieldRenderer.ActorDraw> order=new ArrayList<>(actors);for(AbelTownGeometry.Prop a:AbelTownGeometry.props(map)){float left=a.x-ox-a.anchorX,top=a.y-oy-a.anchorY;if(left>vw||left+a.width<0||top>vh||top+a.height<0)continue;order.add(new PoteFieldRenderer.ActorDraw(a.x,a.depthY,a.height,a.width,()->sprite(c,a.asset,left,top,a.width,a.height)));}
  order.sort(Comparator.comparingDouble(a->a.y));for(PoteFieldRenderer.ActorDraw a:order)a.draw.run();
 }
 private void floor(Canvas c,float ox,float oy,float vw,float vh){c.drawColor(0xffc6c7ce);float l=ox-64,r=ox+vw+64,t=oy-40,b=oy+vh+40;int u0=Math.max(0,(int)Math.floor((l-2560)/64+(t-64)/32)),u1=Math.min(AbelTownGeometry.SIZE-1,(int)Math.ceil((r-2560)/64+(b-64)/32)),v0=Math.max(0,(int)Math.floor((t-64)/32-(r-2560)/64)),v1=Math.min(AbelTownGeometry.SIZE-1,(int)Math.ceil((b-64)/32-(l-2560)/64));for(int u=u0;u<=u1;u++)for(int v=v0;v<=v1;v++){float x=AbelTownGeometry.x(u,v)-ox,y=AbelTownGeometry.y(u,v)-oy;if(x< -65||x>vw+65||y< -65||y>vh+65)continue;sprite(c,TILE[AbelTownGeometry.tile(map,u,v)],x-32,y-16,64,32);}
  for(int u=Math.max(1,u0);u<=Math.min(AbelTownGeometry.SIZE-2,u1);u++)for(int v=Math.max(1,v0);v<=Math.min(AbelTownGeometry.SIZE-2,v1);v++){int k=AbelTownGeometry.tile(map,u,v);if(k!=2&&k!=4&&k!=5)continue;float x=AbelTownGeometry.x(u,v)-ox,y=AbelTownGeometry.y(u,v)-oy;if(x< -65||x>vw+65||y< -65||y>vh+65)continue;String a=k==5?"garden_curb_down_edge":"coping_up_edge",b=k==5?"garden_curb_up_edge":"coping_down_edge";
   if(AbelTownGeometry.tile(map,u-1,v)!=k)edge(c,b,x-32,y);if(AbelTownGeometry.tile(map,u,v-1)!=k)edge(c,a,x,y-16);if(AbelTownGeometry.tile(map,u+1,v)!=k)edge(c,b,x,y+16);if(AbelTownGeometry.tile(map,u,v+1)!=k)edge(c,a,x-32,y);
  }
 }
 private void edge(Canvas c,String n,float x,float y){Bitmap b=image(n);float ay=n.equals("coping_down_edge")||n.equals("garden_curb_up_edge")?14*AbelVideoLayout.SCALE:0;sprite(c,n,x,y-ay,b.getWidth()*AbelVideoLayout.SCALE,b.getHeight()*AbelVideoLayout.SCALE);}
 private void sprite(Canvas c,String name,float x,float y,float width,float height){dst.set(x,y,x+width,y+height);p.setColor(Color.WHITE);p.setAlpha(255);c.drawBitmap(image(name),null,dst,p);}
}
