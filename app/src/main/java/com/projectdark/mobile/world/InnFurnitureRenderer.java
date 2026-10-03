package com.projectdark.mobile.world;

import android.content.Context;
import android.graphics.*;
import java.io.InputStream;

/** Tile-sized furniture geometry. No room, dining-group or counter screenshots. */
public final class InnFurnitureRenderer {
  private final Paint p=new Paint();
  private final BitmapShader wood;
  public InnFurnitureRenderer(Context context){
    try(InputStream in=context.getAssets().open("interiors/v85/materials.webp")){
      Bitmap atlas=BitmapFactory.decodeStream(in);
      wood=new BitmapShader(Bitmap.createBitmap(atlas,0,0,atlas.getWidth()/2,atlas.getHeight()/2),Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
    }catch(Exception e){throw new IllegalStateException("Shared shop wood material",e);}
    p.setAntiAlias(false);p.setFilterBitmap(false);
  }
  public static boolean supports(String name){return "inn_world_table".equals(name)||"inn_world_hearth".equals(name)||name.startsWith("inn_world_chair_");}
  private void poly(Canvas c,int color,float... xy){p.setColor(color);Path a=new Path();a.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)a.lineTo(xy[i],xy[i+1]);a.close();c.drawPath(a,p);}
  private void line(Canvas c,int color,float width,float x,float y,float xx,float yy){p.setColor(color);p.setStrokeWidth(width);c.drawLine(x,y,xx,yy,p);}
  private void beam(Canvas c,float x,float y,float xx,float yy,float width){line(c,0xff322116,width+2,x,y,xx,yy);line(c,0xff855b35,width,x,y,xx,yy);line(c,0xffbd915a,1,x-1,y-1,xx-1,yy-1);}
  private void grain(Canvas c,float x,float y,float... xy){
    Matrix m=new Matrix();m.setValues(new float[]{.12f,-.12f,x,.06f,.06f,y,0,0,1});wood.setLocalMatrix(m);
    p.setShader(wood);poly(c,Color.WHITE,xy);p.setShader(null);
  }
  public void draw(Canvas c,WorldRuntimeAdapter w,TownInteriorDef.Prop o){
    WorldCameraTransform.Point q=w.worldToScreen(o.x(),o.y());float x=q.x,y=q.y;
    if("inn_world_table".equals(o.asset)){
      // Two occupied cells along u: the visual footprint and blocked cells agree.
      poly(c,0x500b0805,x-29,y,x,y-14,x+61,y+16,x+32,y+30);
      for(float[] leg:new float[][]{{-23,-1},{1,-10},{53,15},{31,25}})beam(c,x+leg[0],y+leg[1]-29,x+leg[0],y+leg[1],4);
      poly(c,0xff513521,x-28,y-31,x+32,y-1,x+32,y-6,x-28,y-36);
      poly(c,0xff684629,x+32,y-1,x+60,y-15,x+60,y-20,x+32,y-6);
      grain(c,x,y-38,x-28,y-36,x,y-50,x+60,y-20,x+32,y-6);
      for(int i=0;i<4;i++){float t=i*7;line(c,0xff4d321f,1,x-28+t,y-36-t*.5f,x+32+t,y-6-t*.5f);line(c,0xffbd8e56,1,x-27+t,y-35-t*.5f,x+31+t,y-6-t*.5f);}
      line(c,0xffd3a66a,1,x-28,y-36,x+32,y-6);line(c,0xffd3a66a,1,x+32,y-6,x+60,y-20);
    }else if(o.asset.startsWith("inn_world_chair_")){
      String facing=o.asset.substring("inn_world_chair_".length());
      float ax=facing.endsWith("e")?-16:0,ay=facing.endsWith("e")?0:-8;
      float bx=facing.endsWith("e")?0:16,by=facing.endsWith("e")?-8:0;
      if(facing.startsWith("n")){ax=-ax;ay=-ay;bx=-bx;by=-by;}
      // A separate chair has its own feet, back, facing, collision and depth.
      if(facing.startsWith("s"))back(c,x,y,ax,ay,bx,by);
      for(float[] leg:new float[][]{{-13,0},{0,-6},{13,0},{0,6}})beam(c,x+leg[0],y+leg[1]-16,x+leg[0],y+leg[1],3);
      poly(c,0xff563720,x-16,y-16,x,y-8,x+16,y-16,x+16,y-20,x,y-12,x-16,y-20);
      grain(c,x,y-28,x-16,y-20,x,y-28,x+16,y-20,x,y-12);
      line(c,0xffc39258,1,x-16,y-20,x,y-12);line(c,0xffc39258,1,x,y-12,x+16,y-20);
      if(facing.startsWith("n"))back(c,x,y,ax,ay,bx,by);
    }else{
      c.save();if(o.cellsV==2)c.scale(-1,1,x,y);
      // Brick hearth built from front/side planes and individual brick courses.
      poly(c,0xff62362c,x-28,y,x+32,y+30,x+60,y+16,x,y-14);
      poly(c,0xff884334,x-28,y,x+32,y+30,x+32,y-11,x-28,y-41);
      poly(c,0xff5e3028,x+32,y+30,x+60,y+16,x+60,y-25,x+32,y-11);
      for(int z=0;z<5;z++){float yy=y-z*8;line(c,0xff3f2923,1,x-28,yy,x+32,yy+30);line(c,0xff3f2923,1,x+32,yy+30,x+60,yy+16);for(int i=0;i<4;i++){float dx=-26+i*16+(z%2)*8;line(c,0xff4e2b23,1,x+dx,yy+(dx+28)*.5f,x+dx,yy+(dx+28)*.5f-8);}}
      poly(c,0xff261b15,x-15,y-13,x+21,y+5,x+21,y-12,x-15,y-30);
      for(int i=0;i<5;i++){float fx=x-10+i*6,fy=y-11+i*3;poly(c,i%2==0?0xffdc762a:0xffe5a846,fx-3,fy,fx,fy-15+(i%3)*3,fx+4,fy+2);line(c,0xffffd98a,2,fx,fy-8,fx+1,fy);}
      poly(c,0xff9c6150,x-31,y-43,x,y-58,x+63,y-27,x+32,y-11);
      poly(c,0xff784233,x+5,y-52,x+23,y-43,x+23,y-82,x+5,y-91);
      poly(c,0xff542b24,x+23,y-43,x+34,y-49,x+34,y-88,x+23,y-82);
      for(int i=0;i<5;i++)line(c,0xff462b24,1,x+5,y-53-i*8,x+23,y-44-i*8);
      poly(c,0xffb27762,x+3,y-93,x+16,y-100,x+36,y-90,x+23,y-83);
      c.restore();
    }
  }
  private void back(Canvas c,float x,float y,float ax,float ay,float bx,float by){
    beam(c,x+ax,y+ay-4,x+ax,y+ay-43,3);beam(c,x+bx,y+by-4,x+bx,y+by-43,3);
    for(int z:new int[]{27,35,42})beam(c,x+ax,y+ay-z,x+bx,y+by-z,3);
  }
}
