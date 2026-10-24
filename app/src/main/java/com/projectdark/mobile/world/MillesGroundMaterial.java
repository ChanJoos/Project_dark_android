package com.projectdark.mobile.world;
import android.content.res.AssetManager;import android.graphics.*;import java.io.*;import java.util.*;
/** Original retained Mine lawn materials reused as a mobile Milles surface, no new artwork. */
final class MillesGroundMaterial {
 private static final int CHUNK=256,GUTTER=2,LIMIT=32;
 private final Paint p=new Paint();private final RectF dst=new RectF();private final Bitmap[] patches=new Bitmap[3];private final BitmapShader grass;
 private final LinkedHashMap<Long,Bitmap> cache=new LinkedHashMap<>(40,.75f,true);private int builds;
 MillesGroundMaterial(AssetManager a){Bitmap base=read(a,"mine-v203/grass_material.png");grass=new BitmapShader(base,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);Matrix m=new Matrix();m.setScale(8f/7f,8f/7f);grass.setLocalMatrix(m);for(int i=0;i<3;i++){patches[i]=MineSourceMatte.prepare("meadow"+i,read(a,"mine-v206/meadow"+i+".png"));patches[i].prepareToDraw();}p.setFilterBitmap(false);}
 private static Bitmap read(AssetManager a,String path){try(InputStream in=a.open(path)){Bitmap b=BitmapFactory.decodeStream(in);if(b==null)throw new IOException(path);return b;}catch(IOException e){throw new IllegalStateException("Milles original material "+path,e);}}
 int entries(){return cache.size();}long bytes(){long n=0;for(Bitmap b:cache.values())n+=b.getAllocationByteCount();return n;}int builds(){return builds;}void release(){cache.clear();}
 private void fill(Canvas c,float ox,float oy,float w,float h){c.save();c.translate(-ox,-oy);p.setShader(grass);c.drawRect(ox,oy,ox+w,oy+h,p);p.setShader(null);
  // World-seeded overlapping native patches break a mirrored swatch's circular grid.
  for(int y=(int)Math.floor((oy-64)/32)*32;y<oy+h+64;y+=32)for(int x=(int)Math.floor((ox-96)/64)*64;x<ox+w+96;x+=64){int seed=(x*73856093)^(y*19349663);Bitmap b=patches[Math.floorMod(seed,3)];float px=x+Math.floorMod(seed,21)-10,py=y+Math.floorMod(seed/17,13)-6,bw=b.getWidth()*1.32f,bh=b.getHeight()*1.32f;dst.set(px-bw/2,py-bh/2,px+bw/2,py+bh/2);c.drawBitmap(b,null,dst,p);}
  c.restore();}
 void draw(Canvas c,WorldRuntimeAdapter w){float ox=w.camera().cameraX(),oy=w.camera().cameraY(),vw=w.camera().viewportWidth(),vh=w.camera().viewportHeight();int x0=(int)Math.floor(ox/CHUNK),x1=(int)Math.floor((ox+vw)/CHUNK),y0=(int)Math.floor(oy/CHUNK),y1=(int)Math.floor((oy+vh)/CHUNK);if((x1-x0+1)*(y1-y0+1)>LIMIT){fill(c,ox,oy,vw,vh);return;}
  for(int y=y0;y<=y1;y++)for(int x=x0;x<=x1;x++){long key=((long)x<<32)^(y&0xffffffffL);Bitmap b=cache.get(key);if(b==null){b=Bitmap.createBitmap(CHUNK+GUTTER*2,CHUNK+GUTTER*2,Bitmap.Config.ARGB_8888);fill(new Canvas(b),x*CHUNK-GUTTER,y*CHUNK-GUTTER,b.getWidth(),b.getHeight());b.prepareToDraw();cache.put(key,b);builds++;if(cache.size()>LIMIT){Iterator<Long> it=cache.keySet().iterator();it.next();it.remove();}}dst.set(x*CHUNK-GUTTER-ox,y*CHUNK-GUTTER-oy,(x+1)*CHUNK+GUTTER-ox,(y+1)*CHUNK+GUTTER-oy);c.drawBitmap(b,null,dst,p);}
 }
}
