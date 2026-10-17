package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.InputStream;
import java.util.*;
/** Source-pixel materials/furniture, authored castle geometry and bounded single-map background cache. */
public final class HorrorCastleRenderer {
 private final Map<String,Bitmap> images=new HashMap<>();private final Paint p=new Paint();private final Path path=new Path();
 private Bitmap background;private String map;private int builds;private final List<PoteFieldRenderer.ActorDraw> sorted=new ArrayList<>();
 public HorrorCastleRenderer(Context c){for(String n:new String[]{"carpet","wood","wall","tiger","chair","sofa","table","door"})load(c,n);for(int i=0;i<10;i++)load(c,"human_"+i);p.setFilterBitmap(false);}
 private void load(Context c,String n){try(InputStream in=c.getAssets().open("horror-v179/"+n+".png")){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;Bitmap b=BitmapFactory.decodeStream(in,null,o);if(b==null)throw new IllegalStateException(n);b.prepareToDraw();images.put(n,b);}catch(Exception e){throw new IllegalStateException("Horror source "+n,e);}}
 public int backgroundBuilds(){return builds;}public int backgroundBytes(){return background==null?0:background.getAllocationByteCount();}
 public void releaseFloor(){if(background!=null)background.recycle();background=null;map=null;}
 private void polygon(Canvas c,int color,float... xy){p.setColor(color);path.reset();path.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);path.close();c.drawPath(path,p);}
 private void line(Canvas c,int color,float width,float x,float y,float xx,float yy){p.setShader(null);p.setColor(color);p.setStrokeWidth(width);c.drawLine(x,y,xx,yy,p);}
 private void textured(Canvas c,String name,float scale,int color,float... points){BitmapShader shader=new BitmapShader(images.get(name),Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);Matrix m=new Matrix();m.setScale(scale,scale);shader.setLocalMatrix(m);p.setShader(shader);polygon(c,color,points);p.setShader(null);}
 public void prepare(String id){if(id.equals(map)&&background!=null)return;releaseFloor();map=id;builds++;background=Bitmap.createBitmap(1856,960,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(background);c.drawColor(0xff080708);int cols=HorrorCastleGeometry.columns(id),rows=HorrorCastleGeometry.rows(id),theme=(HorrorCastleGeometry.room(id)-1)%5;
  for(int u=0;u<cols;u++)for(int v=0;v<rows;v++){float x=HorrorCastleGeometry.x(u,v),y=HorrorCastleGeometry.y(u,v);boolean border=u<2||v<2||u>=cols-2||v>=rows-2;int color=border?((u+v)%2==0?0xff646763:0xff777a75):theme==1?((u+v)%2==0?0xff343747:0xff9b673c):theme==4?((u+v)%2==0?0xff8b7d91:0xffb2a4ad):0xffa55d33;
   if(border||theme==1||theme==4)polygon(c,color,x,y-16,x+32,y,x,y+16,x-32,y);else textured(c,"carpet",.7f,0xffffffff,x,y-16,x+32,y,x,y+16,x-32,y);
   if(border||theme==1||theme==4){line(c,0x7046413c,1,x,y-16,x+32,y);line(c,0x808c8882,1,x-32,y,x,y+16);}}
  wall(c,id,true);wall(c,id,false);
  if(HorrorCastleGeometry.LOBBY.equals(id))stairs(c,HorrorCastleGeometry.x(11,3),HorrorCastleGeometry.y(11,3));
  for(HorrorCastleGeometry.Door d:HorrorCastleGeometry.doors(id)){
   if(!"MILLES".equals(d.destination)&&!"로비로".equals(d.label)){bitmap(c,"door",d.x+32,d.y-16,55);}
   p.setColor(0xffa28c67);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);c.drawOval(d.x-16,d.y-6,d.x+16,d.y+6,p);p.setStyle(Paint.Style.FILL);
  }
 }
 private void wall(Canvas c,String id,boolean left){int count=(left?HorrorCastleGeometry.rows(id):HorrorCastleGeometry.columns(id))-1;float sign=left?-1:1,x=800,y=160,length=count*32;
  textured(c,"wall",1f,left?0xffb4a898:0xffc9b9a4,x,y,x+sign*length,y+length/2,x+sign*length,y+length/2-110,x,y-110);
  polygon(c,0xff422d23,x,y,x+sign*length,y+length/2,x+sign*length,y+length/2-20,x,y-20);
  for(int t=0;t<length;t+=10){float a=x+sign*t,b=y+t/2;line(c,0xff7b5941,1,a,b-15,a+sign*3,b-13);}
  line(c,0xff231d1a,5,x,y-110,x+sign*length,y+length/2-110);line(c,0xffd2c3aa,1,x,y-104,x+sign*length,y+length/2-104);
  for(int i=3;i<count;i+=5){float a=x+sign*i*32,b=y+i*16;polygon(c,0xff503928,a-13,b-83,a+13,b-83,a+13,b-48,a-13,b-48);polygon(c,0xffac8756,a-11,b-81,a+11,b-81,a+11,b-50,a-11,b-50);Bitmap h=images.get("human_"+(i%10));p.setColor(0xff6b5147);c.drawRect(a-8,b-77,a+8,b-54,p);p.setColor(Color.WHITE);c.drawBitmap(h,new Rect(192,0,256,64),new RectF(a-11,b-78,a+11,b-56),p);}
 }
 private void stairs(Canvas c,float x,float y){for(int i=10;i>=0;i--){float a=x-i*10,b=y-i*8;polygon(c,0xff3a302c,a-130,b-5,a+120,b-130,a+120,b-115,a-130,b+10);textured(c,"wood",.8f,0xffffffff,a-130,b-5,a+120,b-130,a+108,b-136,a-142,b-11);line(c,0xffb77b48,1,a-142,b-11,a+108,b-136);if(i%2==0){line(c,0xff18191a,3,a-139,b-13,a-139,b-42);line(c,0xff796047,2,a-144,b-45,a-134,b-40);}}}
 private void bitmap(Canvas c,String name,float x,float y,float width){Bitmap b=images.get(name);if(b==null)return;float h=b.getHeight()*width/b.getWidth();p.setColor(Color.WHITE);c.drawBitmap(b,null,new RectF(x-width/2,y-h,x+width/2,y),p);}
 private void pillar(Canvas c,float x,float y,float width){float w=width/2;polygon(c,0xff3e4039,x-w,y-4,x,y+7,x+w,y-4,x,y-14);polygon(c,0xffb0ac96,x-w+4,y-10,x-4,y-4,x-4,y-91,x-w+4,y-98);polygon(c,0xff706f62,x-4,y-4,x+w-4,y-13,x+w-4,y-101,x-4,y-91);polygon(c,0xffbcb9a4,x-w,y-97,x,y-87,x+w,y-97,x,y-108);line(c,0xffdfd6bb,1,x-w+6,y-16,x-w+6,y-94);}
 private void bookshelf(Canvas c,float x,float y,float width){float w=width/2;polygon(c,0xff35251f,x-w,y,x+w,y-28,x+w,y-120,x-w,y-92);for(int shelf=0;shelf<4;shelf++){float yy=y-8-shelf*22;line(c,0xffb58b54,3,x-w,yy,x+w,yy-28);for(int i=0;i<13;i++){float a=x-w+4+i*(width-7)/13,b=yy-(a-x+w)*28/width;int color=new int[]{0xff74583e,0xff525944,0xff704747,0xff56596c,0xff9d8058}[(i+shelf*3)%5];polygon(c,color,a,b-3,a+4,b-5,a+4,b-19,a,b-17);}}}
 public void draw(Canvas c,WorldRuntimeAdapter world,List<PoteFieldRenderer.ActorDraw> actors){String id=world.runtime().currentMapId();prepare(id);float ox=world.camera().cameraX(),oy=world.camera().cameraY();p.setColor(Color.WHITE);c.drawBitmap(background,-ox,-oy,p);sorted.clear();sorted.addAll(actors);for(HorrorCastleGeometry.Prop prop:HorrorCastleGeometry.props(id)){if(prop.art.equals("stairs"))continue;sorted.add(new PoteFieldRenderer.ActorDraw(prop.x(),prop.y(),110,prop.width,()->{c.save();c.translate(-ox,-oy);if(prop.art.equals("pillar"))pillar(c,prop.x(),prop.y(),prop.width);else if(prop.art.equals("bookcase"))bookshelf(c,prop.x(),prop.y(),prop.width);else bitmap(c,prop.art,prop.x(),prop.y(),prop.width);c.restore();}));}sorted.sort(Comparator.comparingDouble(a->a.y));for(PoteFieldRenderer.ActorDraw a:sorted)a.draw.run();sorted.clear();
  c.save();c.translate(-ox,-oy);for(HorrorCastleGeometry.Door d:HorrorCastleGeometry.doors(id)){UiTheme.center(c,d.label,d.x,d.y+20,9,0xffebdbb8,true);}c.restore();if(HorrorCastleGeometry.room(id)>0){int alive=0;for(RuntimeState.Monster m:world.runtime().monsters())if(m.alive)alive++;UiTheme.center(c,"남은 몬스터 "+alive+" / 15",480,82,11,0xffe9d5ad,true);}
 }
 public void monster(Canvas c,RuntimeState.Monster m){int t=HorrorCastleMonsters.type(m.id);if(t<0||t>9)return;CharacterRenderer.Direction d=m.visualFacing.presentation();int col=d==CharacterRenderer.Direction.NW?0:d==CharacterRenderer.Direction.NE?1:d==CharacterRenderer.Direction.SW?2:3;int row=m.attackPrimed||m.attackVisualRemaining>0?5:m.isMoving?1+((int)(m.animationClock/.12f)%4):0;p.setColor(0x55000000);c.drawOval(m.x-10,m.y-3,m.x+10,m.y+3,p);p.setColor(Color.WHITE);c.drawBitmap(images.get("human_"+t),new Rect(col*64,row*64,col*64+64,row*64+64),new RectF(m.x-36,m.y-67.5f,m.x+36,m.y+4.5f),p);}
}
