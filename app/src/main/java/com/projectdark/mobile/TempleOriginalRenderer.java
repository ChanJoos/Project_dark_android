package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.json.*;
import java.io.*;
import java.util.*;

/** Original tile layers. Floor is built once per room; foreground shares actor depth sorting. */
public final class TempleOriginalRenderer {
 private static final float SCALE=8f/7f;
 private final Bitmap atlas;private Bitmap floor;private final Paint paint=new Paint();private final Map<String,Rect> regions=new HashMap<>();private final Map<String,float[]> anchors=new HashMap<>();private String map;private float left;private int builds;
 public TempleOriginalRenderer(Context c){try(InputStream a=c.getAssets().open("temple-v211/atlas.png");InputStream j=c.getAssets().open("temple-v211/atlas.json")){atlas=BitmapFactory.decodeStream(a);JSONObject rs=new JSONObject(new String(read(j),"UTF-8")).getJSONObject("regions");for(Iterator<String> it=rs.keys();it.hasNext();){String k=it.next();JSONObject r=rs.getJSONObject(k);regions.put(k,new Rect(r.getInt("x"),r.getInt("y"),r.getInt("x")+r.getInt("width"),r.getInt("y")+r.getInt("height")));if(!r.isNull("anchor")){JSONArray v=r.getJSONArray("anchor");anchors.put(k,new float[]{(float)v.getDouble(0),(float)v.getDouble(1)});}}}catch(Exception e){throw new IllegalStateException("Original temple atlas",e);}paint.setFilterBitmap(false);}
 private static byte[] read(InputStream in)throws IOException{ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] a=new byte[8192];for(int n;(n=in.read(a))!=-1;)b.write(a,0,n);return b.toByteArray();}
 public int floorBuilds(){return builds;}public int floorBytes(){return floor==null?0:floor.getAllocationByteCount();}
 public void release(){if(floor!=null)floor.recycle();floor=null;map=null;}
 public void prepare(TownInteriorDef d){if(d.mapId.equals(map))return;release();map=d.mapId;builds++;TempleOriginalData.Grid g=d.original();left=d.minX();int width=(int)(d.maxX()-left),height=132+(g.width+g.height-2)*16+16;floor=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);if(floor.getAllocationByteCount()>8652800)throw new IllegalStateException("Temple floor memory bound");Canvas c=new Canvas(floor);for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++){int i=g.value(u,v,0);if(i>0)tile(c,"bg"+i,TownInteriorDef.x(u,v)-32-left,TownInteriorDef.y(u,v)-16);}}
 private void tile(Canvas c,String name,float x,float y){Rect r=regions.get(name);if(r==null)throw new IllegalStateException("Missing temple source "+name);c.drawBitmap(atlas,r,new RectF(x,y,x+r.width()*SCALE,y+r.height()*SCALE),paint);}
 public void ground(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d){if(!d.mapId.equals(map))throw new IllegalStateException("Temple not prewarmed");c.drawColor(Color.BLACK);c.drawBitmap(floor,left-w.camera().cameraX(),-w.camera().cameraY(),paint);}
 public void foreground(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d,List<Runnable> draws,List<Float> depths){TempleOriginalData.Grid g=d.original();for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++)for(int side=1;side<=2;side++){int id=g.value(u,v,side);if(id<=1||id==10000)continue;String key="stc"+id;Rect r=regions.get(key);float x=TownInteriorDef.x(u,v)-32+(side-1)*32,y=TownInteriorDef.y(u,v)+16;draws.add(()->{float px=w.presentationPlayerX(),py=w.presentationPlayerY();boolean covers=r.height()>56&&y>py&&px>x-18&&px<x+r.width()*SCALE+18&&py>y-r.height()*SCALE-5;paint.setAlpha(covers?110:255);tile(c,key,x-w.camera().cameraX(),y-r.height()*SCALE-w.camera().cameraY());paint.setAlpha(255);});depths.add(y);}}
 public void npc(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d){String key="npc"+d.original().npc;float[] a=anchors.get(key);WorldCameraTransform.Point q=w.worldToScreen(d.npcX(),d.npcY());tile(c,key,q.x-a[0]*SCALE,q.y-a[1]*SCALE);}
}
