package com.projectdark.mobile.world;
import android.content.Context;import android.graphics.*;import org.json.*;import java.io.*;import java.util.*;
public final class TownInteriorRenderer {
 private final InnFurnitureRenderer inn;private final TownInteriorDetails details=new TownInteriorDetails();private final Map<String,Bitmap> storage=new HashMap<>();private final Bitmap props,tiles;private Bitmap foliage;private final BitmapShader[] materials=new BitmapShader[4];private TownInteriorDef.Kind activeKind;private final Paint p=new Paint();private final Map<String,Rect> regions=new HashMap<>();
 public TownInteriorRenderer(Context c){inn=new InnFurnitureRenderer(c);Bitmap a=null,b=null;try(InputStream in=c.getAssets().open("interiors/v84/props.png")){a=BitmapFactory.decodeStream(in);}catch(IOException e){}try(InputStream in=c.getAssets().open("interiors/v84/tiles.png")){b=BitmapFactory.decodeStream(in);}catch(IOException e){}props=a;tiles=b;p.setFilterBitmap(false);try(InputStream in=c.getAssets().open("vegetation/bushes/OBJ_bush_02.png")){foliage=BitmapFactory.decodeStream(in);}catch(IOException e){throw new IllegalStateException("Interior foliage",e);}for(String n:new String[]{"barrel","crate","sack"})try(InputStream in=c.getAssets().open("storage/OBJ_"+n+".png")){storage.put("storage_"+n,BitmapFactory.decodeStream(in));}catch(IOException e){throw new IllegalStateException("Storage prop "+n,e);}try(InputStream in=c.getAssets().open("interiors/v85/materials.webp")){Bitmap atlas=BitmapFactory.decodeStream(in);int mw=atlas.getWidth()/2,mh=atlas.getHeight()/2;for(int i=0;i<4;i++)materials[i]=new BitmapShader(Bitmap.createBitmap(atlas,i%2*mw,i/2*mh,mw,mh),Shader.TileMode.MIRROR,Shader.TileMode.MIRROR);}catch(IOException e){throw new IllegalStateException("Interior materials",e);}try(InputStream in=c.getAssets().open("interiors/v84/atlas.json")){JSONObject o=new JSONObject(readText(in)).getJSONObject("props");for(Iterator<String> it=o.keys();it.hasNext();){String k=it.next();JSONObject r=o.getJSONObject(k);regions.put(k,new Rect(r.getInt("x"),r.getInt("y"),r.getInt("x")+r.getInt("width"),r.getInt("y")+r.getInt("height")));}Rect s=regions.get("church_wall_sw");regions.put("church_wall_se",s);}catch(Exception e){throw new IllegalStateException("Interior atlas",e);}}
 private static String readText(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];for(int n;(n=in.read(b))!=-1;)out.write(b,0,n);return out.toString("UTF-8");}
 public boolean ready(){return props!=null&&tiles!=null&&regions.size()>=16;}
 public void ground(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d){
  c.drawColor(0xff0c0907);activeKind=d.kind;
  for(int i=0;i<4;i++){Matrix m=new Matrix();if(i==0||i==1||i==3){float scale=i==0?128f/627:i==1?96f/627:112f/627;m.setValues(new float[]{scale,-scale,480-w.camera().cameraX(),scale*.5f,scale*.5f,132-w.camera().cameraY(),0,0,1});}else m.setScale(.22f,.22f);materials[i].setLocalMatrix(m);}
  for(int u=0;u<d.columns;u++)for(int v=0;v<d.rows;v++){WorldCameraTransform.Point q=w.worldToScreen(TownInteriorDef.x(u,v),TownInteriorDef.y(u,v));int type=d.floor(u,v),texture=type==0?0:type==2?3:1;p.setShader(materials[texture]);polygon(c,0xffffffff,q.x,q.y-16,q.x+32,q.y,q.x,q.y+16,q.x-32,q.y);p.setShader(null);if(type==3){line(c,0xffb79a68,1,q.x-32,q.y,q.x,q.y+16);line(c,0xffb79a68,1,q.x,q.y-16,q.x+32,q.y);}}
  floorDetails(c,w,d);wall(c,w,d,true);wall(c,w,d,false);
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
  boolean stone=d.kind==TownInteriorDef.Kind.CHURCH,boards=d.kind==TownInteriorDef.Kind.EQUIPMENT||d.kind==TownInteriorDef.Kind.INN;
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
  }else if(d.kind==TownInteriorDef.Kind.INN){
   for(int z=11;z<height;z+=10)timber(c,x,y,sign,0,z,length,z+8,left);
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
   // Wall-mounted lamps occupy structural bays, not floor/navigation tiles.
   if(i%4==2){float lamp=t+15,z=stone?57:52;
    face(c,0xff3e3526,x,y,sign,lamp-3,z-8,lamp+3,z+13);
    edge(c,0xffa78d57,1,x,y,sign,lamp-2,z-7,lamp-2,z+12);
    float lx=x+sign*lamp-sign*4,ly=y+lamp*.5f-z;
    line(c,0xff453322,3,x+sign*lamp,ly+5,lx,ly+9);
    polygon(c,0xffa8894c,lx-5,ly+7,lx+5,ly+7,lx+3,ly+10,lx-3,ly+10);
    polygon(c,0xffd1bf8d,lx-2,ly-7,lx+2,ly-7,lx+2,ly+7,lx-2,ly+7);
    polygon(c,0xffd18a37,lx,ly-14,lx+2,ly-10,lx,ly-7,lx-2,ly-10);
    line(c,0xfff7df99,1,lx,ly-12,lx,ly-9);
   }
   // Set between structural posts, never on the corner or the open room ends.
   if(d.kind!=TownInteriorDef.Kind.INN&&i%3==1&&i<count-1)window(c,x,y,sign,t+4,stone,left);
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
 private void counter(Canvas c,WorldRuntimeAdapter w,TownInteriorDef.Prop o){
  WorldCameraTransform.Point q=w.worldToScreen(o.x(),o.y());Rect r=regions.get("joined_counter_sw".equals(o.asset)?"counter_sw":"counter_se");float h=64f*r.height()/r.width();
  p.setColor(0x300b0805);c.drawOval(new RectF(q.x-28,q.y-4,q.x+25,q.y+6),p);p.setColor(Color.WHITE);
  // Reuse detailed oak sprite pixels rather than an untextured polygon front.
  c.drawBitmap(props,r,new RectF(q.x-32,q.y-h+2,q.x+32,q.y+2),p);
 }
 private int activeCounterStart(){return activeKind==TownInteriorDef.Kind.BANK?4:3;}private int activeCounterEnd(){return activeKind==TownInteriorDef.Kind.EQUIPMENT?11:activeKind==TownInteriorDef.Kind.BANK?8:10;}
 private void floorDetails(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d){
  if(d.kind==TownInteriorDef.Kind.INN){
   for(TownInteriorDef.Prop o:d.props)if("inn_world_table".equals(o.asset)){
    // Rug is a walkable floor surface, drawn before every actor and solid prop.
    WorldCameraTransform.Point q=w.worldToScreen(o.x(),o.y());
    polygon(c,0xffa48369,q.x-92,q.y-8,q.x+4,q.y-56,q.x+116,q.y,q.x+20,q.y+48);
    polygon(c,0xff53362d,q.x-86,q.y-8,q.x+4,q.y-53,q.x+110,q.y,q.x+20,q.y+45);
    polygon(c,0xff775148,q.x-78,q.y-8,q.x+4,q.y-49,q.x+102,q.y,q.x+20,q.y+41);
    for(int i=0;i<8;i++){float xx=q.x-60+i*20,yy=q.y-4+(i%2)*4;polygon(c,0xff9b7560,xx-4,yy,xx,yy-2,xx+4,yy,xx,yy+2);}
   }
  }
  // Bordered entrance/customer mats, with central paths left open.
  if(d.kind==TownInteriorDef.Kind.EQUIPMENT||d.kind==TownInteriorDef.Kind.BANK){
   int u=d.kind==TownInteriorDef.Kind.BANK?4:5,v=d.rows-4;WorldCameraTransform.Point q=w.worldToScreen(TownInteriorDef.x(u,v),TownInteriorDef.y(u,v));
   polygon(c,0xff62533a,q.x-45,q.y,q.x,q.y-23,q.x+45,q.y,q.x,q.y+23);
   polygon(c,0xffa28b54,q.x-40,q.y,q.x,q.y-20,q.x+40,q.y,q.x,q.y+20);
   polygon(c,0xff4b4f36,q.x-35,q.y,q.x,q.y-17,q.x+35,q.y,q.x,q.y+17);
   for(int i=-3;i<=3;i++)polygon(c,0xff8a7450,q.x+i*8-3,q.y,q.x+i*8,q.y-3,q.x+i*8+3,q.y,q.x+i*8,q.y+3);
  }
  if(d.kind==TownInteriorDef.Kind.REAGENT||d.kind==TownInteriorDef.Kind.CHURCH){
   for(int u=0;u<d.columns;u++)for(int v=0;v<d.rows;v++)if(d.floor(u,v)==1||d.floor(u,v)==3){
    WorldCameraTransform.Point q=w.worldToScreen(TownInteriorDef.x(u,v),TownInteriorDef.y(u,v));
    int ink=d.kind==TownInteriorDef.Kind.REAGENT?0x447e7055:0x66938a5b;
    line(c,ink,1,q.x-18,q.y,q.x,q.y-9);line(c,ink,1,q.x,q.y-9,q.x+18,q.y);line(c,ink,1,q.x+18,q.y,q.x,q.y+9);line(c,ink,1,q.x,q.y+9,q.x-18,q.y);
    polygon(c,ink,q.x-3,q.y,q.x,q.y-2,q.x+3,q.y,q.x,q.y+2);
   }
  }
 }
 public boolean hasProp(String name){return InnFurnitureRenderer.supports(name)||"joined_counter_sw".equals(name)||"herb_planter".equals(name)||"holy_font".equals(name)||"joined_counter".equals(name)||regions.containsKey(name)||storage.containsKey(name)||TownInteriorDetails.supports(name);}
 public void prop(Canvas c,WorldRuntimeAdapter w,TownInteriorDef.Prop o){
  if(InnFurnitureRenderer.supports(o.asset)){inn.draw(c,w,o);return;}
  if("joined_counter".equals(o.asset)||"joined_counter_sw".equals(o.asset)){counter(c,w,o);return;}
  WorldCameraTransform.Point q=w.worldToScreen(o.x(),o.y());float y=q.y-o.lift;
  if(o.blocking){p.setColor(0x480b0805);c.drawOval(new RectF(q.x-o.width*.35f,q.y-4,q.x+o.width*.35f,q.y+7),p);}
  if("herb_planter".equals(o.asset)){
   // Layer unchanged transparent foliage and detailed barrel pixels into a planter.
   Bitmap pot=storage.get("storage_barrel");float pw=o.width*.56f,ph=pw*pot.getHeight()/pot.getWidth();p.setColor(Color.WHITE);
   c.drawBitmap(pot,null,new RectF(q.x-pw/2,y-ph,q.x+pw/2,y),p);
   float fh=o.width*foliage.getHeight()/foliage.getWidth();
   c.drawBitmap(foliage,null,new RectF(q.x-o.width/2,y-ph*.7f-fh,q.x+o.width/2,y-ph*.7f),p);return;
  }
  if("holy_font".equals(o.asset)){
   // Detailed stone plinth from the altar sprite, topped by an inset water basin.
   Rect a=regions.get("altar"),base=new Rect(a.left,a.top+a.height()/3,a.right,a.bottom);
   float h=o.width*base.height()/base.width();p.setColor(Color.WHITE);
   c.drawBitmap(props,base,new RectF(q.x-o.width/2,y-h,q.x+o.width/2,y),p);
   float top=y-h+3;p.setColor(0xff393c35);c.drawOval(new RectF(q.x-14,top-4,q.x+14,top+7),p);
   p.setColor(0xffa89e86);c.drawOval(new RectF(q.x-14,top-6,q.x+14,top+4),p);
   p.setColor(0xff3a504c);c.drawOval(new RectF(q.x-11,top-4,q.x+11,top+2),p);
   line(c,0xffa6b8a4,1,q.x-6,top-2,q.x+5,top-2);return;
  }
  if(details.draw(c,o.asset,q.x,y,o.width))return;
  Bitmap extra=storage.get(o.asset);if(extra!=null){float h=o.width*extra.getHeight()/extra.getWidth();p.setColor(Color.WHITE);c.drawBitmap(extra,null,new RectF(q.x-o.width/2,y-h,q.x+o.width/2,y),p);return;}
  Rect src=regions.get(o.asset);if(src==null||props==null)return;float h=o.width*src.height()/src.width();p.setColor(Color.WHITE);
  if("church_wall_se".equals(o.asset)){c.save();c.scale(-1,1,q.x,y);c.drawBitmap(props,src,new RectF(q.x-o.width/2,y-h,q.x+o.width/2,y),p);c.restore();}else c.drawBitmap(props,src,new RectF(q.x-o.width/2,y-h,q.x+o.width/2,y),p);
 }
}
