package com.projectdark.mobile.world;
import android.content.Context;import android.graphics.*;import org.json.*;import java.io.*;import java.util.*;
public final class TownInteriorRenderer {
 private final Bitmap props,tiles;private final BitmapShader[] materials=new BitmapShader[4];private TownInteriorDef.Kind activeKind;private final Paint p=new Paint();private final Map<String,Rect> regions=new HashMap<>();
 public TownInteriorRenderer(Context c){Bitmap a=null,b=null;try(InputStream in=c.getAssets().open("interiors/v84/props.png")){a=BitmapFactory.decodeStream(in);}catch(IOException e){}try(InputStream in=c.getAssets().open("interiors/v84/tiles.png")){b=BitmapFactory.decodeStream(in);}catch(IOException e){}props=a;tiles=b;p.setFilterBitmap(false);try(InputStream in=c.getAssets().open("interiors/v85/materials.webp")){Bitmap atlas=BitmapFactory.decodeStream(in);int mw=atlas.getWidth()/2,mh=atlas.getHeight()/2;for(int i=0;i<4;i++)materials[i]=new BitmapShader(Bitmap.createBitmap(atlas,i%2*mw,i/2*mh,mw,mh),Shader.TileMode.MIRROR,Shader.TileMode.MIRROR);}catch(IOException e){throw new IllegalStateException("Interior materials",e);}try(InputStream in=c.getAssets().open("interiors/v84/atlas.json")){JSONObject o=new JSONObject(readText(in)).getJSONObject("props");for(Iterator<String> it=o.keys();it.hasNext();){String k=it.next();JSONObject r=o.getJSONObject(k);regions.put(k,new Rect(r.getInt("x"),r.getInt("y"),r.getInt("x")+r.getInt("width"),r.getInt("y")+r.getInt("height")));}Rect s=regions.get("church_wall_sw");regions.put("church_wall_se",s);}catch(Exception e){throw new IllegalStateException("Interior atlas",e);}}
 private static String readText(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];for(int n;(n=in.read(b))!=-1;)out.write(b,0,n);return out.toString("UTF-8");}
 public boolean ready(){return props!=null&&tiles!=null&&regions.size()>=16;}
 public void ground(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d){
  c.drawColor(0xff0c0907);activeKind=d.kind;
  for(int i=0;i<4;i++){Matrix m=new Matrix();if(i==0||i==1||i==3)m.setValues(new float[]{192f/627,-192f/627,480-w.camera().cameraX(),96f/627,96f/627,132-w.camera().cameraY(),0,0,1});else m.setScale(.22f,.22f);materials[i].setLocalMatrix(m);}
  for(int u=0;u<d.columns;u++)for(int v=0;v<d.rows;v++){WorldCameraTransform.Point q=w.worldToScreen(TownInteriorDef.x(u,v),TownInteriorDef.y(u,v));int type=d.floor(u,v),texture=type==0?0:type==2?3:1;p.setShader(materials[texture]);polygon(c,0xffffffff,q.x,q.y-16,q.x+32,q.y,q.x,q.y+16,q.x-32,q.y);p.setShader(null);if(type==3){line(c,0xffb79a68,1,q.x-32,q.y,q.x,q.y+16);line(c,0xffb79a68,1,q.x,q.y-16,q.x+32,q.y);}}
  wall(c,w,d,true);wall(c,w,d,false);
  WorldCameraTransform.Point e=w.worldToScreen(d.exitX(),d.exitY());p.setColor(0xffd9c392);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);c.drawOval(new RectF(e.x-18,e.y-7,e.x+18,e.y+7),p);p.setStyle(Paint.Style.FILL);
 }
 private void polygon(Canvas c,int color,float... xy){Path a=new Path();a.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)a.lineTo(xy[i],xy[i+1]);a.close();p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawPath(a,p);}
 private void line(Canvas c,int color,float width,float x,float y,float xx,float yy){p.setColor(color);p.setStrokeWidth(width);c.drawLine(x,y,xx,yy,p);}
 private void floorTile(Canvas c,float x,float y,int type,int u,int v){
  int base=type==0?0xff69412a:type==1?0xff67504d:type==2?0xff9d9988:0xff74433d;
  polygon(c,base,x,y-16,x+32,y,x,y+16,x-32,y);
  if(type==0){for(int k=0;k<4;k++){float a=k*8;polygon(c,((u*13+v*7+k)%3==0)?0xff795039:0xff70472e,x-32+a,y-a*.5f,x-24+a,y-4-a*.5f,x+8+a,y+12-a*.5f,x+a,y+16-a*.5f);line(c,0xff492f21,1,x-32+a,y-a*.5f,x+a,y+16-a*.5f);line(c,0x557e6243,1,x-28+a,y-a*.5f,x-4+a,y+12-a*.5f);}}
  else if(type==1){for(int k=0;k<8;k++){float dx=((u*19+v*31+k*23)%60)-30,dy=((u*11+v*17+k*13)%26)-13;if(Math.abs(dx)/32+Math.abs(dy)/16<.9){p.setColor(k%2==0?0xff725a55:0xff5b4643);c.drawPoint(x+dx,y+dy,p);}}}
  else if(type==2){line(c,0xff706f65,1,x,y-16,x+32,y);line(c,0xff706f65,1,x-32,y,x,y+16);line(c,0xffb8b4a1,1,x+1,y-14,x+30,y);}
  else{line(c,0xffc4a571,1,x-29,y,x,y+14);line(c,0xffa58559,1,x,y-14,x+29,y);}
 }
 private void wall(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d,boolean left){
  int count=left?d.rows:d.columns;float height=d.kind==TownInteriorDef.Kind.CHURCH?112:96;
  for(int i=0;i<count-1;i++){
   WorldCameraTransform.Point a=w.worldToScreen(TownInteriorDef.x(left?0:i,left?i:0),TownInteriorDef.y(left?0:i,left?i:0));
   WorldCameraTransform.Point b=w.worldToScreen(TownInteriorDef.x(left?0:i+1,left?i+1:0),TownInteriorDef.y(left?0:i+1,left?i+1:0));
   int wall=d.kind==TownInteriorDef.Kind.EQUIPMENT?(left?0xff6b402b:0xff795039):d.kind==TownInteriorDef.Kind.CHURCH?(left?0xffa6a18e:0xffbbb5a2):(left?0xffa69b82:0xffbdb198);
   polygon(c,wall,a.x,a.y,b.x,b.y,b.x,b.y-height,a.x,a.y-height);
   BitmapShader surface=materials[d.kind==TownInteriorDef.Kind.EQUIPMENT?0:d.kind==TownInteriorDef.Kind.CHURCH?3:2];Matrix wm=new Matrix();wm.setScale(.2f,.2f);wm.postTranslate(-w.camera().cameraX(),-w.camera().cameraY());surface.setLocalMatrix(wm);p.setShader(surface);polygon(c,0x8fffffff,a.x,a.y,b.x,b.y,b.x,b.y-height,a.x,a.y-height);p.setShader(null);
   line(c,0xff504638,4,a.x,a.y-5,b.x,b.y-5);line(c,0xffd3c7a9,2,a.x,a.y-12,b.x,b.y-12);
   line(c,0xff413629,5,a.x,a.y-height,b.x,b.y-height);
   if(i%3==0)line(c,d.kind==TownInteriorDef.Kind.CHURCH?0xff817d70:0xff593a26,4,a.x,a.y,a.x,a.y-height);
   if(d.kind==TownInteriorDef.Kind.EQUIPMENT){for(int k=1;k<4;k++)line(c,0xff533724,1,a.x,a.y-k*22,b.x,b.y-k*22);}
   if(i==3||i==8){float midX=(a.x+b.x)/2,midY=(a.y+b.y)/2;float dx=left?-23:23,dy=11.5f;
    polygon(c,0xff4a4f45,midX-dx/2,midY-75-dy/2,midX+dx/2,midY-75+dy/2,midX+dx/2,midY-37+dy/2,midX-dx/2,midY-37-dy/2);
    polygon(c,d.kind==TownInteriorDef.Kind.CHURCH?0xff8d6680:0xffd7e0d5,midX-dx/2+3,midY-72-dy/2,midX+dx/2-3,midY-72+dy/2,midX+dx/2-3,midY-40+dy/2,midX-dx/2+3,midY-40-dy/2);
    line(c,0xff51574e,3,midX,midY-74,midX,midY-38);line(c,0xff51574e,3,midX-dx/2,midY-56-dy/2,midX+dx/2,midY-56+dy/2);
   }
  }
 }
 private void counter(Canvas c,WorldRuntimeAdapter w,TownInteriorDef.Prop o){WorldCameraTransform.Point q=w.worldToScreen(o.x(),o.y());float x=q.x,y=q.y;
  polygon(c,0xff664126,x-32,y-14,x,y+2,x,y-22,x-32,y-38);
  polygon(c,0xff48301f,x,y+2,x+32,y-14,x+32,y-38,x,y-22);
  p.setShader(materials[0]);polygon(c,0xbfffffff,x-32,y-14,x,y+2,x,y-22,x-32,y-38);polygon(c,0x7fffffff,x,y+2,x+32,y-14,x+32,y-38,x,y-22);p.setShader(null);
  polygon(c,activeKind==TownInteriorDef.Kind.REAGENT?0xffc9c6ba:0xffa07543,x-32,y-38,x,y-54,x+32,y-38,x,y-22);
  if(activeKind==TownInteriorDef.Kind.REAGENT){polygon(c,0xffa6a399,x-32,y-38,x,y-22,x,y-18,x-32,y-34);line(c,0xffede8da,1,x-32,y-38,x,y-22);}else{p.setShader(materials[0]);polygon(c,0xafffffff,x-32,y-38,x,y-54,x+32,y-38,x,y-22);p.setShader(null);}
  line(c,0xffcfad70,2,x-32,y-38,x,y-22);line(c,0xffd0ad6a,1,x,y-22,x+32,y-38);
  line(c,0xff9b7549,2,x-27,y-12,x-27,y-31);line(c,0xff332316,1,x-5,y-2,x-5,y-23);
 }
 public void prop(Canvas c,WorldRuntimeAdapter w,TownInteriorDef.Prop o){if("joined_counter".equals(o.asset)){counter(c,w,o);return;}Rect src=regions.get(o.asset);if(src==null||props==null)return;WorldCameraTransform.Point q=w.worldToScreen(o.x(),o.y());float h=o.width*src.height()/src.width();if("church_wall_se".equals(o.asset)){c.save();c.scale(-1,1,q.x,q.y);c.drawBitmap(props,src,new RectF(q.x-o.width/2,q.y-h,q.x+o.width/2,q.y),p);c.restore();}else c.drawBitmap(props,src,new RectF(q.x-o.width/2,q.y-h,q.x+o.width/2,q.y),p);}
}
