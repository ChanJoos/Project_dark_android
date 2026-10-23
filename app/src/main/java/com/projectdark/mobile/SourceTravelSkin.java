package com.projectdark.mobile;
import android.content.Context;import android.graphics.*;import java.util.*;
/** Nine-slice borders retain the supplied modern-client screenshot pixels. */
final class SourceTravelSkin {
 private final Map<String,Bitmap> images=new HashMap<>();private final Paint p=new Paint();private final NpcActorRenderer npc;
 SourceTravelSkin(Context c){npc=new NpcActorRenderer(c);p.setFilterBitmap(false);for(String n:new String[]{"choice-selected","choice-idle","portrait"})try(java.io.InputStream in=c.getAssets().open("town-v248/"+n+".png")){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;images.put(n,BitmapFactory.decodeStream(in,null,o));}catch(java.io.IOException e){throw new IllegalStateException(e);}}
 void frame(Canvas c,String name,RectF r){Bitmap b=images.get(name);int cap=name.equals("portrait")?12:9;float[] xs={r.left,r.left+cap,r.right-cap,r.right},ys={r.top,r.top+cap,r.bottom-cap,r.bottom};int[] sx={0,cap,b.getWidth()-cap,b.getWidth()},sy={0,cap,b.getHeight()-cap,b.getHeight()};p.setColor(0xff080807);c.drawRect(r,p);p.setColor(Color.WHITE);for(int y=0;y<3;y++)for(int x=0;x<3;x++){if(x==1&&y==1)continue;c.drawBitmap(b,new Rect(sx[x],sy[y],sx[x+1],sy[y+1]),new RectF(xs[x],ys[y],xs[x+1],ys[y+1]),p);}}
 void portrait(Canvas c){RectF r=new RectF(64,390,146,492);frame(c,"portrait",r);c.save();c.translate(105,477);c.scale(1.55f,1.55f);npc.draw(c,"pote_travel_guide",0,0,CharacterRenderer.Direction.SW,CharacterRenderer.State.IDLE,0);c.restore();}
}
