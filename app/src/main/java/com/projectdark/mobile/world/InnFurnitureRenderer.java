package com.projectdark.mobile.world;

import android.content.Context;
import android.graphics.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** Independent antique sprites; footprints and depth remain owned by World. */
public final class InnFurnitureRenderer {
  private final Paint p=new Paint();
  private final Map<String,Bitmap> sprites=new HashMap<>();
  public InnFurnitureRenderer(Context context){
    p.setFilterBitmap(false);p.setAntiAlias(false);
    for(String name:new String[]{"table","chair_ne","chair_nw","chair_se","chair_sw","hearth"}){
      try(InputStream in=context.getAssets().open("interiors/v93/"+name+".png")){
        Bitmap b=BitmapFactory.decodeStream(in);if(b==null)throw new IllegalStateException(name);sprites.put(name,b);
      }catch(Exception e){throw new IllegalStateException("Antique inn sprite "+name,e);}
    }
  }
  public static boolean supports(String name){return "inn_world_table".equals(name)||"inn_world_hearth".equals(name)||name.startsWith("inn_world_chair_");}
  public void draw(Canvas c,WorldRuntimeAdapter w,TownInteriorDef.Prop o){
    WorldCameraTransform.Point q=w.worldToScreen(o.x(),o.y());
    String key=o.asset.substring("inn_world_".length());Bitmap b=sprites.get(key);
    if(b==null)throw new IllegalArgumentException(o.asset);
    float width=o.width,height=width*b.getHeight()/b.getWidth();
    boolean chair=key.startsWith("chair_");float left=chair?-width/2:-32,bottom=chair?6:26;
    // Contact shadow stays on the occupied floor, never baked room pixels.
    p.setColor(0x480d0906);c.drawOval(new RectF(q.x+left+3,q.y-5,q.x+left+width-3,q.y+bottom),p);p.setColor(Color.WHITE);
    c.save();if("hearth".equals(key)&&o.cellsV==2)c.scale(-1,1,q.x,q.y);
    c.drawBitmap(b,null,new RectF(q.x+left,q.y+bottom-height,q.x+left+width,q.y+bottom),p);c.restore();
  }
}
