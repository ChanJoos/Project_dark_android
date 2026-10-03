package com.projectdark.mobile.world;

import android.content.Context;
import android.graphics.*;
import java.io.InputStream;
import java.util.*;

/** Independent furniture/material crops from the supplied inn recording. */
public final class InnSourceRenderer {
  private final Map<String,Bitmap> images=new HashMap<>();
  private final Paint paint=new Paint();
  private final BitmapShader floor;
  public InnSourceRenderer(Context c){
    for(String name:new String[]{"inn_table","inn_table_food","inn_hearth","inn_counter","inn_wall","inn_door","inn_planter","inn_floor"}){
      try(InputStream in=c.getAssets().open("interiors/v91/"+name+".png")){
        Bitmap b=BitmapFactory.decodeStream(in);if(b==null)throw new IllegalStateException(name);
        images.put(name,b);
      }catch(Exception e){throw new IllegalStateException("Inn source crop "+name,e);}
    }
    paint.setFilterBitmap(false);paint.setAntiAlias(false);
    floor=new BitmapShader(images.get("inn_floor"),Shader.TileMode.MIRROR,Shader.TileMode.MIRROR);
  }
  public boolean has(String name){return images.containsKey(name);}
  public boolean prop(Canvas c,WorldRuntimeAdapter w,TownInteriorDef.Prop o){
    Bitmap b=images.get(o.asset);if(b==null)return false;
    WorldCameraTransform.Point q=w.worldToScreen(o.x(),o.y());
    float h=o.width*b.getHeight()/b.getWidth();paint.setColor(Color.WHITE);
    c.drawBitmap(b,null,new RectF(q.x-o.width/2,q.y-h-o.lift,q.x+o.width/2,q.y-o.lift),paint);return true;
  }
  public void ground(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d){
    c.drawColor(0xff100c09);
    Matrix m=new Matrix();m.setTranslate(-w.camera().cameraX(),-w.camera().cameraY());floor.setLocalMatrix(m);
    paint.setShader(floor);paint.setColor(Color.WHITE);
    for(int u=0;u<d.columns;u++)for(int v=0;v<d.rows;v++){
      WorldCameraTransform.Point q=w.worldToScreen(TownInteriorDef.x(u,v),TownInteriorDef.y(u,v));
      Path diamond=new Path();diamond.moveTo(q.x,q.y-16);diamond.lineTo(q.x+32,q.y);diamond.lineTo(q.x,q.y+16);diamond.lineTo(q.x-32,q.y);diamond.close();c.drawPath(diamond,paint);
    }
    paint.setShader(null);
    wall(c,w,d,true);wall(c,w,d,false);
    WorldCameraTransform.Point e=w.worldToScreen(d.exitX(),d.exitY());
    paint.setColor(0xffccb184);paint.setStrokeWidth(1);paint.setStyle(Paint.Style.STROKE);
    c.drawOval(new RectF(e.x-17,e.y-6,e.x+17,e.y+6),paint);paint.setStyle(Paint.Style.FILL);
  }
  private void wall(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d,boolean left){
    int count=(left?d.rows:d.columns)-1;
    for(int i=0;i<count;i+=2){
      boolean door=left&&i==2;
      Bitmap b=images.get(door?"inn_door":"inn_wall");
      WorldCameraTransform.Point q=w.worldToScreen(TownInteriorDef.x(left?0:i,left?i:0),TownInteriorDef.y(left?0:i,left?i:0));
      float width=Math.min(2,count-i)*32,sign=left?-1:1,height=72;
      // Map the extracted wall's own plane into each structural bay. This is
      // material projection, not a rotated/mirrored NPC or monster direction.
      float[] src=door?new float[]{0,37,70,0,70,55,0,95}:new float[]{0,35,73,0,73,51,0,86};
      float[] dst={q.x+sign*width,q.y+width*.5f-height,q.x,q.y-height,q.x,q.y,q.x+sign*width,q.y+width*.5f};
      Matrix map=new Matrix();map.setPolyToPoly(src,0,dst,0,4);paint.setColor(Color.WHITE);c.drawBitmap(b,map,paint);
    }
  }
}
