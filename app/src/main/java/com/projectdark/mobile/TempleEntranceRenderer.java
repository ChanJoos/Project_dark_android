package com.projectdark.mobile;
import android.content.Context;import android.graphics.*;import java.io.*;import java.util.*;import com.projectdark.mobile.world.AdaptedMillesMapRenderer;
/** Independently cut source stone platform, three arches and a translucent source light matte. */
public final class TempleEntranceRenderer {
 public static final float X=1952,Y=1104;
 private static final float S=8f/7f;
 private final Map<String,Bitmap> images=new HashMap<>();private final Paint p=new Paint();private float clock,ox,oy;
 public TempleEntranceRenderer(Context c){for(String n:new String[]{"entrance_platform","entrance_arch_back","entrance_arch_front","entrance_arch_left","entrance_light"})try(InputStream in=c.getAssets().open("temple-v211/"+n+".png")){Bitmap b=BitmapFactory.decodeStream(in);if(b==null)throw new IOException(n);b.prepareToDraw();images.put(n,b);}catch(IOException e){throw new IllegalStateException("Temple entrance source "+n,e);}p.setFilterBitmap(false);}
 private void draw(Canvas c,String key,float dx,float dy,int alpha){Bitmap b=images.get(key);p.setAlpha(alpha);float x=X+dx*S-ox,y=Y+dy*S-oy;c.drawBitmap(b,null,new RectF(x,y,x+b.getWidth()*S,y+b.getHeight()*S),p);p.setAlpha(255);}
 public static java.util.List<float[]> archFeet(){java.util.List<float[]> feet=new java.util.ArrayList<>();for(float[] q:new float[][]{{-101,-89},{-67,-97},{-31,-103},{190,54},{204,82},{220,70},{-192,103},{-165,116},{-132,116}})feet.add(new float[]{X+q[0]*S-7,Y+q[1]*S-5,X+q[0]*S+7,Y+q[1]*S+5});return java.util.Collections.unmodifiableList(feet);}
 public static boolean coversGround(float x,float y){return Math.pow((x-X)/156f,2)+Math.pow((y-Y)/81f,2)<1;}
 public void tick(float dt){clock=(clock+dt)%100;}
 public void add(Canvas c,List<AdaptedMillesMapRenderer.DepthDraw> actors,float ox,float oy){this.ox=ox;this.oy=oy;
  actors.add(new AdaptedMillesMapRenderer.DepthDraw(Float.NEGATIVE_INFINITY,()->draw(c,"entrance_platform",-135,-69,255)));
  actors.add(new AdaptedMillesMapRenderer.DepthDraw(Y-86*S,()->draw(c,"entrance_arch_back",-131,-159,255)));
  actors.add(new AdaptedMillesMapRenderer.DepthDraw(Y+84*S,()->draw(c,"entrance_arch_front",184,-24,255)));
  actors.add(new AdaptedMillesMapRenderer.DepthDraw(Y+127*S,()->draw(c,"entrance_arch_left",-207,36,255)));
  actors.add(new AdaptedMillesMapRenderer.DepthDraw(Y+7,()->draw(c,"entrance_light",-47,-215,220+(int)(12*Math.sin(clock*1.5)))));
 }
}
