package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.InputStream;
import java.util.*;
/** Raw original tiles and MPFs. Floor chunks, foreground depth and actors remain separate. */
public final class HorrorCastleRenderer {
 private static final float SCALE=8f/7f;
 private final Context context;private final Paint paint=new Paint();private final Rect clip=new Rect(),src=new Rect();private final RectF dst=new RectF();
 private Bitmap portal;
 private final Bitmap[] monsters=new Bitmap[10];private final int[] attacks={2,2,2,2,3,3,2,2,4,4};
 private final Map<Integer,Bitmap> floors=new HashMap<>(),objects=new HashMap<>();
 private final LinkedHashMap<Long,Bitmap> chunks=new LinkedHashMap<>(32,.75f,true);
 private final List<TileDraw> foreground=new ArrayList<>();private final List<PoteFieldRenderer.ActorDraw> actors=new ArrayList<>();
 private String map;private int builds;private HorrorCastleOriginalData.Grid grid;private HorrorCastleLegacyRenderer legacy;
 private static final class TileDraw {final Bitmap image;final float x,y,depth;final boolean flip;TileDraw(Bitmap b,float xx,float yy,float z){this(b,xx,yy,z,false);}TileDraw(Bitmap b,float xx,float yy,float z,boolean f){image=b;x=xx;y=yy;depth=z;flip=f;}}
 public HorrorCastleRenderer(Context c){context=c;paint.setFilterBitmap(false);for(int i=0;i<10;i++)monsters[i]=load("monster_"+i);try(InputStream in=c.getAssets().open("assets/world/portal/portal_reagent_shop.webp")){portal=BitmapFactory.decodeStream(in);if(portal==null)throw new IllegalStateException("shared portal");portal.prepareToDraw();}catch(Exception e){throw new IllegalStateException("Missing shared portal",e);}}
 private Bitmap load(String name){try(InputStream in=context.getAssets().open("horror-v180/"+name+".png")){BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;Bitmap b=BitmapFactory.decodeStream(in,null,options);if(b==null)throw new IllegalStateException(name);b.prepareToDraw();return b;}catch(Exception e){throw new IllegalStateException("Original Horror source "+name,e);}}
 public int backgroundBuilds(){return builds;}
 public int backgroundBytes(){int n=0;for(Bitmap b:chunks.values())n+=b.getAllocationByteCount();return n+(grid==null&&legacy!=null?legacy.backgroundBytes():0);}
 public void releaseFloor(){chunks.clear();floors.clear();objects.clear();foreground.clear();actors.clear();grid=null;map=null;if(legacy!=null)legacy.releaseFloor();}
 public void prepare(String id){if(id.equals(map))return;releaseFloor();map=id;builds++;grid=HorrorCastleGeometry.original(id);if(grid==null){if(legacy==null)legacy=new HorrorCastleLegacyRenderer(context);legacy.prepare(id);return;}
  for(int v=0;v<grid.height;v++)for(int u=0;u<grid.width;u++){
   int bg=grid.value(u,v,0);if(bg>0&&!floors.containsKey(bg))floors.put(bg,load("bg"+bg));
   for(int side=1;side<=2;side++){int index=grid.value(u,v,side);if(!rendered(index))continue;Bitmap b=objects.get(index);if(b==null){b=load(String.format(Locale.ROOT,"stc%05d",index));objects.put(index,b);}float x=HorrorCastleGeometry.x(u,v)-32+(side-1)*32,y=HorrorCastleGeometry.y(u,v)+16;foreground.add(new TileDraw(b,x,y-b.getHeight()*SCALE,y));}
  }
  for(HorrorCastleReferenceRooms.Piece p:HorrorCastleReferenceRooms.pieces(id)){
   Bitmap b=objects.get(p.sprite);if(b==null){b=load(String.format(Locale.ROOT,"stc%05d",p.sprite));objects.put(p.sprite,b);}
   float dx=HorrorCastleGeometry.x(p.u,p.v)-HorrorCastleGeometry.x(p.sourceU,p.sourceV)-32+(p.side-1)*32;
   float dy=HorrorCastleGeometry.y(p.u,p.v)-HorrorCastleGeometry.y(p.sourceU,p.sourceV)+16;
   float x=HorrorCastleGeometry.x(p.anchorU,p.anchorV)+(p.mirror?-dx-b.getWidth()*SCALE:dx),foot=HorrorCastleGeometry.y(p.anchorU,p.anchorV)+dy;
   foreground.add(new TileDraw(b,x,foot-b.getHeight()*SCALE,foot,p.mirror));
  }
  foreground.sort(Comparator.comparingDouble(a->a.depth));
 }
 private static boolean rendered(int i){return i>10012||i%10000>12;}
 private Bitmap chunk(int cx,int cy){long key=((long)cx<<32)^(cy&0xffffffffL);Bitmap b=chunks.get(key);if(b!=null)return b;b=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(b);out.drawColor(Color.BLACK);float left=cx*256f,top=cy*256f;
  for(int v=0;v<grid.height;v++)for(int u=0;u<grid.width;u++){float x=HorrorCastleGeometry.x(u,v)-32,y=HorrorCastleGeometry.y(u,v)-16;if(x+64<left||x>left+256||y+32<top||y>top+256)continue;Bitmap tile=floors.get(grid.value(u,v,0));if(tile!=null){dst.set(x-left,y-top,x-left+64,y-top+27*SCALE);out.drawBitmap(tile,null,dst,paint);}}
  b.prepareToDraw();chunks.put(key,b);if(chunks.size()>32){Iterator<Map.Entry<Long,Bitmap>> it=chunks.entrySet().iterator();it.next();it.remove();}return b;
 }
 private void tile(Canvas c,TileDraw t,float ox,float oy){if(t.x+t.image.getWidth()*SCALE<ox+clip.left||t.x>ox+clip.right||t.y+t.image.getHeight()*SCALE<oy+clip.top||t.y>oy+clip.bottom)return;dst.set(t.x-ox,t.y-oy,t.x-ox+t.image.getWidth()*SCALE,t.y-oy+t.image.getHeight()*SCALE);if(t.flip){c.save();c.scale(-1,1,dst.centerX(),dst.centerY());c.drawBitmap(t.image,null,dst,paint);c.restore();}else c.drawBitmap(t.image,null,dst,paint);}
 public void draw(Canvas c,WorldRuntimeAdapter world,List<PoteFieldRenderer.ActorDraw> input){String id=world.runtime().currentMapId();prepare(id);if(grid==null){legacy.draw(c,world,input);drawCounter(c,world);return;}float ox=world.camera().cameraX(),oy=world.camera().cameraY();c.getClipBounds(clip);paint.setColor(Color.WHITE);paint.setAlpha(255);
  int minCx=(int)Math.floor((ox+clip.left)/256f),maxCx=(int)Math.floor((ox+clip.right)/256f),minCy=(int)Math.floor((oy+clip.top)/256f),maxCy=(int)Math.floor((oy+clip.bottom)/256f);
  for(int cy=minCy;cy<=maxCy;cy++)for(int cx=minCx;cx<=maxCx;cx++)c.drawBitmap(chunk(cx,cy),cx*256f-ox,cy*256f-oy,paint);

  actors.clear();actors.addAll(input);actors.sort(Comparator.comparingDouble(a->a.y));int i=0;for(PoteFieldRenderer.ActorDraw actor:actors){while(i<foreground.size()&&foreground.get(i).depth<=actor.y)tile(c,foreground.get(i++),ox,oy);actor.draw.run();}while(i<foreground.size())tile(c,foreground.get(i++),ox,oy);actors.clear();
  drawCounter(c,world);
 }
 private void drawCounter(Canvas c,WorldRuntimeAdapter world){if(HorrorCastleGeometry.room(world.runtime().currentMapId())<=0)return;c.getClipBounds(clip);int alive=0;for(RuntimeState.Monster m:world.runtime().monsters())if(m.alive)alive++;UiTheme.center(c,"남은 몬스터 "+alive+" / 15",clip.exactCenterX(),82,11,0xffe9d5ad,true);}

 public void monster(Canvas c,RuntimeState.Monster m){int t=HorrorCastleMonsters.type(m.id);if(t<0||t>=10)return;CharacterRenderer.Direction d=m.visualFacing.presentation();int col=d==CharacterRenderer.Direction.NW?0:d==CharacterRenderer.Direction.NE?1:d==CharacterRenderer.Direction.SW?2:3;
  int row=0;if(m.attackPrimed)row=6;else if(m.attackVisualRemaining>0)row=6+Math.min(attacks[t]-1,(int)((1-Math.min(1,m.attackVisualRemaining/.36f))*attacks[t]));else if(m.isMoving)row=1+(int)(m.animationClock/.12f)%5;
  src.set(col*160,row*128,col*160+160,row*128+128);dst.set(m.x-80*SCALE,m.y-88*SCALE,m.x+80*SCALE,m.y+40*SCALE);paint.setColor(Color.WHITE);paint.setAlpha(255);c.drawBitmap(monsters[t],src,dst,paint);
 }
}
