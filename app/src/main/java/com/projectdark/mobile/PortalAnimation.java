package com.projectdark.mobile;
import android.content.Context;import android.graphics.*;import java.io.*;
/** One prewarmed original32-frame additive portal sheet; no floor-cache animation or per-frame decode. */
public final class PortalAnimation {
 private static Bitmap atlas;private static final Paint P=new Paint();private static final Rect SRC=new Rect();private static final RectF DST=new RectF();
 private static float clock;static int mapDraws,lastPins;static String lastMap;
 public static final int FRAMES=32,FRAME_MS=125;public static final float SCALE=8f/7f;
 public static void install(Context c){if(atlas!=null)return;try(InputStream in=c.getAssets().open("town-v248/portal.png")){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;atlas=BitmapFactory.decodeStream(in,null,o);if(atlas==null)throw new IOException("portal sheet");atlas.prepareToDraw();}catch(IOException e){throw new IllegalStateException(e);}P.setAntiAlias(false);P.setFilterBitmap(false);P.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));}
 public static void tick(float dt){clock=(clock+Math.max(0,dt))%4f;}static void time(float t){clock=((t%4f)+4f)%4f;}
 public static int frame(){return Math.min(31,(int)(clock*8));}public static long bytes(){return atlas==null?0:atlas.getAllocationByteCount();}
 public static void draw(Canvas c,float x,float y){if(atlas==null)return;int f=frame();SRC.set(f%8*38,f/8*120,(f%8+1)*38,(f/8+1)*120);DST.set(x-19*SCALE,y-110*SCALE,x+19*SCALE,y+10*SCALE);c.drawBitmap(atlas,SRC,DST,P);}
 /** Cached destination-aware physical entrance groups are shared with the minimap. */
 public static void drawMap(Canvas c,String map,float ox,float oy){mapDraws++;lastMap=map;lastPins=0;for(WorldMapWindow.Pin pin:WorldMapWindow.entrances(map)){draw(c,pin.x-ox,pin.y-oy);lastPins++;}}
 private PortalAnimation(){}
}
