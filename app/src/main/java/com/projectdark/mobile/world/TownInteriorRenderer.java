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
 /** Wall-local horizontal distance follows the 2:1 isometric edge; z is height. */
 private void face(Canvas c,int color,float x,float y,float sign,float t0,float z0,float t1,float z1){
  polygon(c,color,x+sign*t0,y+t0*.5f-z0,x+sign*t1,y+t1*.5f-z0,
   x+sign*t1,y+t1*.5f-z1,x+sign*t0,y+t0*.5f-z1);
 }
 private void edge(Canvas c,int color,float width,float x,float y,float sign,float t0,float z0,float t1,float z1){
  line(c,color,width,x+sign*t0,y+t0*.5f-z0,x+sign*t1,y+t1*.5f-z1);
 }
 private void timber(Canvas c,float x,float y,float sign,float t0,float z0,float t1,float z1,boolean left){
  // Raised interior-facing edge and shaded underside make each beam a solid object.
  face(c,left?0xff4c3021:0xff623e29,x,y,sign,t0,z0,t1,z1);
  edge(c,0xff9a7049,1.5f,x,y,sign,t0,z1-1,t1,z1-1);
  edge(c,0xff2d2119,2,x,y,sign,t0,z0,t1,z0);
  edge(c,left?0xff795234:0xffa47a50,1,x,y,sign,t0+1,z0+2,t0+1,z1-2);
  if(t1-t0>12){edge(c,0x407d5639,1,x,y,sign,t0+3,z0+3,t1-3,z0+3);}
 }
 private void wall(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d,boolean left){
  int count=(left?d.rows:d.columns)-1;
  boolean stone=d.kind==TownInteriorDef.Kind.CHURCH,boards=d.kind==TownInteriorDef.Kind.EQUIPMENT;
  float height=stone?112:96,sign=left?-1:1;
  WorldCameraTransform.Point origin=w.worldToScreen(TownInteriorDef.x(0,0),TownInteriorDef.y(0,0));
  float x=origin.x,y=origin.y,length=count*32;
  int base=stone?(left?0xff8c897c:0xffa8a392):boards?(left?0xff65432e:0xff805738):(left?0xffa89a7e:0xffc3b499);
  face(c,base,x,y,sign,0,0,length,height);
  BitmapShader surface=materials[boards?0:stone?3:2];
  // The old matrix had two collinear axes (determinant 0), collapsing the texture.
  // Texture x follows the wall edge; texture y remains independently vertical.
  Matrix wm=new Matrix();wm.setValues(new float[]{sign*.18f,0,x,.09f,.18f,y-height,0,0,1});
  surface.setLocalMatrix(wm);p.setShader(surface);face(c,0x70ffffff,x,y,sign,0,0,length,height);p.setShader(null);
  face(c,left?0x40392a20:0x18392a20,x,y,sign,0,0,length,height);
  // Ground contact shadow extends into the room, with no change to navigation.
  float nx=-sign*7;
  polygon(c,0x480a0806,x,y,x+sign*length,y+length*.5f,x+sign*length+nx,y+length*.5f+4,x+nx,y+4);
  if(stone){
   for(int row=0;row<7;row++){
    float z=12+row*14;
    edge(c,0xff716f65,1,x,y,sign,0,z,length,z);
    edge(c,0x509d9a8c,1,x,y,sign,0,z+1,length,z+1);
    for(float t=(row%2)*16;t<length;t+=32){edge(c,0xff777368,1,x,y,sign,t,z,t,z+14);}
   }
  }else if(boards){
   for(int t=0;t<length;t+=8){
    edge(c,0xff453122,1,x,y,sign,t,12,t,height-6);
    edge(c,0x408f6946,1,x,y,sign,t+2,15,t+2,height-8);
   }
  }
  // Continuous cap, skirting and chair rail, with visible top/side thickness.
  timber(c,x,y,sign,0,0,length,10,left);
  timber(c,x,y,sign,0,height-7,length,height,left);
  polygon(c,stone?0xffbbb7a6:0xff956b45,x,y-height,x+sign*length,y+length*.5f-height,
   x+sign*length+sign*5,y+length*.5f-height-3,x+sign*5,y-height-3);
  if(!stone)timber(c,x,y,sign,0,22,length,26,left);
  for(int i=0;i<count;i++){
   float t=i*32;
   if(!stone&&i%3==0){
    timber(c,x,y,sign,t,8,t+5,height-5,left);
    if(!boards&&i+2<count){
     // Braces frame the plaster bays instead of drawing a blank white panel.
     edge(c,0xff513824,5,x,y,sign,t+6,27,t+30,height-9);
     edge(c,0xff96714c,1,x,y,sign,t+7,29,t+31,height-10);
    }
   }
   // Set between structural posts, never on the corner or the open room ends.
   if(i%3==1&&i<count-1)window(c,x,y,sign,t+4,stone,left);
  }
  // Thick end post and corner terminate the exposed wall rather than a paper edge.
  if(stone){face(c,left?0xff807c70:0xff969182,x,y,sign,0,0,5,height);face(c,0xff817c6f,x,y,sign,length-5,0,length,height);}
  else{timber(c,x,y,sign,0,0,5,height,left);timber(c,x,y,sign,length-5,0,length,height,left);}
  float ex=x+sign*length,ey=y+length*.5f;
  polygon(c,stone?0xff605e56:0xff31251b,ex,ey,ex+sign*5,ey-3,ex+sign*5,ey-height-3,ex,ey-height);
 }
 private void window(Canvas c,float x,float y,float sign,float t,boolean stone,boolean left){
  float bottom=stone?28:35,top=stone?96:77,width=23;
  // Dark aperture. Glass is smaller, leaving deep top and side reveals.
  face(c,0xff282c28,x,y,sign,t,bottom,t+width,top);
  if(stone){
   // A pointed stained-glass opening is built in wall coordinates.
   polygon(c,0xff29292b,x+sign*t,y+t*.5f-top+10,x+sign*(t+width/2),y+(t+width/2)*.5f-top-5,
    x+sign*(t+width),y+(t+width)*.5f-top+10,x+sign*(t+width),y+(t+width)*.5f-bottom,x+sign*t,y+t*.5f-bottom);
   face(c,left?0xff514c59:0xff68606b,x,y,sign,t+4,bottom+5,t+width-4,top-12);
   for(int row=0;row<4;row++)for(int col=0;col<2;col++){
    int color=(row+col)%3==0?0xff82544f:(row+col)%3==1?0xff8b7850:0xff4f6872;
    face(c,color,x,y,sign,t+5+col*7,bottom+7+row*10,t+11+col*7,bottom+15+row*10);
   }
   edge(c,0xffb7ae95,3,x,y,sign,t-1,bottom-2,t-1,top-10);
   edge(c,0xff696659,3,x,y,sign,t+width+1,bottom-2,t+width+1,top-10);
   edge(c,0xffb7ae95,3,x,y,sign,t-1,top-10,t+width/2,top+5);
   edge(c,0xff696659,3,x,y,sign,t+width/2,top+5,t+width+1,top-10);
  }else{
   face(c,left?0xff667e7a:0xff7a9188,x,y,sign,t+4,bottom+5,t+width-4,top-5);
   face(c,left?0xffafc3af:0xffced4b5,x,y,sign,t+6,bottom+7,t+11,top-7);
   face(c,left?0xff90ac9e:0xffbbcbb3,x,y,sign,t+13,bottom+7,t+width-5,top-7);
   // Glazing bars inherit the same wall slope as the opening and the masonry.
   edge(c,0xff373d33,2,x,y,sign,t+width/2,bottom+4,t+width/2,top-4);
   edge(c,0xff414438,2,x,y,sign,t+4,(bottom+top)/2,t+width-4,(bottom+top)/2);
   timber(c,x,y,sign,t-2,bottom-2,t+2,top+2,left);
   timber(c,x,y,sign,t+width-2,bottom-2,t+width+2,top+2,left);
   timber(c,x,y,sign,t-2,top-2,t+width+2,top+2,left);
  }
  // Sill projects toward the room, with a lit top and dark underside.
  float a=t-3,b=t+width+3,nx=-sign*5,z=bottom-1;
  polygon(c,stone?0xffb0a78f:0xffa78152,x+sign*a,y+a*.5f-z,x+sign*b,y+b*.5f-z,
   x+sign*b+nx,y+b*.5f-z+3,x+sign*a+nx,y+a*.5f-z+3);
  polygon(c,stone?0xff716e61:0xff4b3423,x+sign*a+nx,y+a*.5f-z+3,x+sign*b+nx,y+b*.5f-z+3,
   x+sign*b+nx,y+b*.5f-z+6,x+sign*a+nx,y+a*.5f-z+6);
  edge(c,0x440b0805,3,x,y,sign,t-3,bottom-7,t+width+3,bottom-7);
 }
 private void counter(Canvas c,WorldRuntimeAdapter w,TownInteriorDef.Prop o){WorldCameraTransform.Point q=w.worldToScreen(o.x(),o.y());float x=q.x,y=q.y;
  polygon(c,0xff664126,x-32,y-14,x,y+2,x,y-22,x-32,y-38);
  if(o.u==activeCounterEnd())polygon(c,0xff48301f,x,y+2,x+32,y-14,x+32,y-38,x,y-22);
  p.setShader(materials[0]);polygon(c,0xbfffffff,x-32,y-14,x,y+2,x,y-22,x-32,y-38);polygon(c,0x7fffffff,x,y+2,x+32,y-14,x+32,y-38,x,y-22);p.setShader(null);
  // Continuous oak countertop: no bright untextured linen slab or tile seams.
  polygon(c,0xffa07543,x-32,y-38,x,y-54,x+32,y-38,x,y-22);
  p.setShader(materials[0]);polygon(c,0xafffffff,x-32,y-38,x,y-54,x+32,y-38,x,y-22);p.setShader(null);
  line(c,0xffcfad70,2,x-32,y-38,x,y-22);
  // Front panel inset and projecting apron share the counter's isometric plane.
  polygon(c,0xff382719,x-28,y-16,x-4,y-4,x-4,y-23,x-28,y-35);
  polygon(c,0xff765034,x-26,y-17,x-6,y-7,x-6,y-22,x-26,y-32);
  line(c,0xffb18a57,1,x-26,y-32,x-6,y-22);
  line(c,0xffb58c57,2,x-32,y-34,x,y-18);
  line(c,0xff352518,2,x-32,y-14,x,y+2);
  if(o.u==activeCounterEnd())line(c,0xffd0ad6a,1,x,y-22,x+32,y-38);
 }
 private int activeCounterStart(){return activeKind==TownInteriorDef.Kind.BANK?4:3;}private int activeCounterEnd(){return activeKind==TownInteriorDef.Kind.EQUIPMENT?11:activeKind==TownInteriorDef.Kind.BANK?8:10;}
 public void prop(Canvas c,WorldRuntimeAdapter w,TownInteriorDef.Prop o){if("joined_counter".equals(o.asset)){counter(c,w,o);return;}Rect src=regions.get(o.asset);if(src==null||props==null)return;WorldCameraTransform.Point q=w.worldToScreen(o.x(),o.y());float h=o.width*src.height()/src.width();p.setColor(0x480b0805);c.drawOval(new RectF(q.x-o.width*.35f,q.y-4,q.x+o.width*.35f,q.y+7),p);p.setColor(Color.WHITE);if("church_wall_se".equals(o.asset)){c.save();c.scale(-1,1,q.x,q.y);c.drawBitmap(props,src,new RectF(q.x-o.width/2,q.y-h,q.x+o.width/2,q.y),p);c.restore();}else c.drawBitmap(props,src,new RectF(q.x-o.width/2,q.y-h,q.x+o.width/2,q.y),p);}
}
