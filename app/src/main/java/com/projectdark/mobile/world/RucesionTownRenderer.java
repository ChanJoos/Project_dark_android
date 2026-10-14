package com.projectdark.mobile.world;
import android.content.Context;import android.graphics.*;
import java.io.*;import java.util.*;
/** Independent tiles and registered source sprites with a bounded floor cache. */
public final class RucesionTownRenderer {
 private static final int CHUNK=256,GUTTER=2,LIMIT=32;
 private final Context context;private final Paint p=new Paint();private final Paint labels=new Paint(Paint.ANTI_ALIAS_FLAG);private final RectF dst=new RectF();private final Map<String,Bitmap> assets=new HashMap<>();private final LinkedHashMap<Long,Bitmap> chunks=new LinkedHashMap<>(32,.75f,true);private String map="";private long floorBuilds;public long floorBuildCount(){return floorBuilds;}
 private static final String[] TILE={"tile_white","tile_brick_rosette","tile_blue_water","tile_rock_paving","tile_sea_water","tile_grass"};
 private Bitmap doorPortal;
 public RucesionTownRenderer(Context c){context=c;labels.setTextSize(12);labels.setTextAlign(Paint.Align.CENTER);p.setFilterBitmap(false);p.setAntiAlias(false);}
 private Bitmap image(String name){Bitmap b=assets.get(name);if(b==null)try(InputStream in=context.getAssets().open("rucesion-v154/"+name+".png")){b=BitmapFactory.decodeStream(in);if(b==null)throw new IOException(name);b.prepareToDraw();assets.put(name,b);}catch(IOException e){throw new IllegalStateException("Missing registered Rucesion asset "+name,e);}return b;}
 private Bitmap portal(){if(doorPortal==null)try(InputStream in=context.getAssets().open("assets/world/portal/portal_reagent_shop.webp")){doorPortal=BitmapFactory.decodeStream(in);if(doorPortal==null)throw new IOException("door portal");doorPortal.prepareToDraw();}catch(IOException e){throw new IllegalStateException("Missing shared Milles entrance portal",e);}return doorPortal;}
 public void prepare(){portal();for(String t:TILE)image(t);for(RucesionTownGeometry.Prop a:RucesionTownGeometry.PROPS)image(a.asset);for(RucesionTownGeometry.Prop a:RucesionTownGeometry.EAST_PROPS)image(a.asset);}
 public int floorCacheEntries(){return chunks.size();}public long floorCachePixelBytes(){long n=0;for(Bitmap b:chunks.values())n+=b.getAllocationByteCount();return n;}
 private void select(String id){if(id.equals(map))return;chunks.clear();map=id;}
 public void draw(Canvas c,WorldRuntimeAdapter w,List<PoteFieldRenderer.ActorDraw> actors){select(w.runtime().currentMapId());float ox=w.camera().cameraX(),oy=w.camera().cameraY(),vw=w.camera().viewportWidth(),vh=w.camera().viewportHeight();c.drawColor(0xffc6c7ce);
  int countX=(int)Math.floor((ox+vw)/CHUNK)-(int)Math.floor(ox/CHUNK)+1,countY=(int)Math.floor((oy+vh)/CHUNK)-(int)Math.floor(oy/CHUNK)+1;
  if(countX*countY>LIMIT)floor(c,ox,oy,vw,vh);else for(int cy=(int)Math.floor(oy/CHUNK);cy<=(int)Math.floor((oy+vh)/CHUNK);cy++)for(int cx=(int)Math.floor(ox/CHUNK);cx<=(int)Math.floor((ox+vw)/CHUNK);cx++){
   long key=((long)cx<<32)^(cy&0xffffffffL);Bitmap b=chunks.get(key);if(b==null){b=Bitmap.createBitmap(CHUNK+GUTTER*2,CHUNK+GUTTER*2,Bitmap.Config.ARGB_8888);floor(new Canvas(b),cx*CHUNK-GUTTER,cy*CHUNK-GUTTER,CHUNK+GUTTER*2,CHUNK+GUTTER*2);floorBuilds++;chunks.put(key,b);if(chunks.size()>LIMIT){Iterator<Map.Entry<Long,Bitmap>> it=chunks.entrySet().iterator();it.next();it.remove();}}
   dst.set(cx*CHUNK-GUTTER-ox,cy*CHUNK-GUTTER-oy,(cx+1)*CHUNK+GUTTER-ox,(cy+1)*CHUNK+GUTTER-oy);c.drawBitmap(b,null,dst,p);
  }
  List<PoteFieldRenderer.ActorDraw> order=new ArrayList<>(actors);for(RucesionTownGeometry.Prop a:RucesionTownGeometry.props(map)){float left=a.x-ox-a.anchorX,top=a.y-oy-a.anchorY;if(left>vw||left+a.width<0||top>vh||top+a.height<0)continue;order.add(new PoteFieldRenderer.ActorDraw(a.x,a.depthY,a.height,a.width,()->sprite(c,a.asset,left,top,a.width,a.height)));}
  // Match Milles' 40x16 doorway marker and threshold layer. The facade must
  // precede its marker so the foundation cannot hide the entrance indication.
  for(RucesionTownGeometry.Building b:RucesionTownGeometry.BUILDINGS){float x=b.doorX()-ox,y=b.doorY()-oy;if(x+20<0||x-20>vw||y+8<0||y-8>vh)continue;order.add(new PoteFieldRenderer.ActorDraw(b.doorX(),Math.max(b.doorY(),b.prop.depthY)+.01f,16,40,()->{dst.set(x-20,y-8,x+20,y+8);p.setColor(Color.WHITE);p.setAlpha(255);c.drawBitmap(portal(),null,dst,p);}));}
  order.sort(Comparator.comparingDouble(a->a.y));for(PoteFieldRenderer.ActorDraw a:order)a.draw.run();
  for(RucesionTownGeometry.Building b:RucesionTownGeometry.BUILDINGS){float x=b.doorX()-ox,y=b.doorY()-oy+24;if(x<0||x>vw||y<0||y>vh)continue;String name=b.title.replace("뤼케시온 ","");float half=labels.measureText(name)/2+7;p.setColor(0xd9292721);c.drawRoundRect(x-half,y-13,x+half,y+4,4,4,p);labels.setColor(0xffffe4a0);c.drawText(name,x,y,labels);}
 }
 private void floor(Canvas c,float ox,float oy,float vw,float vh){c.drawColor(0xffc6c7ce);float l=ox-64,r=ox+vw+64,t=oy-40,bottom=oy+vh+40;int u0=Math.max(0,(int)Math.floor((l-3328)/64+(t-64)/32)),u1=Math.min(RucesionTownGeometry.SIZE-1,(int)Math.ceil((r-3328)/64+(bottom-64)/32)),v0=Math.max(0,(int)Math.floor((t-64)/32-(r-3328)/64)),v1=Math.min(RucesionTownGeometry.SIZE-1,(int)Math.ceil((bottom-64)/32-(l-3328)/64));for(int u=u0;u<=u1;u++)for(int v=v0;v<=v1;v++){float x=RucesionTownGeometry.x(u,v)-ox,y=RucesionTownGeometry.y(u,v)-oy;if(x< -65||x>vw+65||y< -65||y>vh+65)continue;sprite(c,TILE[RucesionTownGeometry.tile(map,u,v)],x-32,y-16,64,32);}
  for(int u=Math.max(1,u0);u<=Math.min(RucesionTownGeometry.SIZE-2,u1);u++)for(int v=Math.max(1,v0);v<=Math.min(RucesionTownGeometry.SIZE-2,v1);v++){int k=RucesionTownGeometry.tile(map,u,v);if(k!=2&&k!=4&&k!=5)continue;float x=RucesionTownGeometry.x(u,v)-ox,y=RucesionTownGeometry.y(u,v)-oy;if(x< -65||x>vw+65||y< -65||y>vh+65)continue;String a=k==5?"garden_curb_down_edge":"coping_up_edge",b=k==5?"garden_curb_up_edge":"coping_down_edge";
   if(RucesionTownGeometry.tile(map,u-1,v)!=k)edge(c,b,x-32,y);if(RucesionTownGeometry.tile(map,u,v-1)!=k)edge(c,a,x,y-16);if(RucesionTownGeometry.tile(map,u+1,v)!=k)edge(c,b,x,y+16);if(RucesionTownGeometry.tile(map,u,v+1)!=k)edge(c,a,x-32,y);
  }
 }
 private void edge(Canvas c,String n,float x,float y){Bitmap b=image(n);float ay=n.equals("coping_down_edge")||n.equals("garden_curb_up_edge")?14*AbelVideoLayout.SCALE:0;sprite(c,n,x,y-ay,b.getWidth()*AbelVideoLayout.SCALE,b.getHeight()*AbelVideoLayout.SCALE);}
 private void sprite(Canvas c,String name,float x,float y,float width,float height){dst.set(x,y,x+width,y+height);p.setColor(Color.WHITE);p.setAlpha(255);c.drawBitmap(image(name),null,dst,p);}
}
