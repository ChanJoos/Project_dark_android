package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.json.*;
import java.io.*;
import java.util.*;
/** Separate original tile/object/actor layers with bounded floor LRU and map-entry prewarm. */
public final class MineHuntRenderer {
 private static final float SCALE=8f/7f;
 private final Context context;private final Paint p=new Paint();private final Rect clip=new Rect(),src=new Rect();private final RectF dst=new RectF();
 private final Map<Integer,Bitmap> floors=new HashMap<>(),objects=new HashMap<>();
 private final LinkedHashMap<Long,Bitmap> chunks=new LinkedHashMap<>(32,.75f,true);
 private final List<Piece> foreground=new ArrayList<>();private final List<PoteFieldRenderer.ActorDraw> actors=new ArrayList<>();
 private final Bitmap[][] sheets=new Bitmap[4][MineHuntMonsters.COUNT];private final int[] widths=new int[MineHuntMonsters.COUNT],heights=new int[MineHuntMonsters.COUNT],ax=new int[MineHuntMonsters.COUNT],ay=new int[MineHuntMonsters.COUNT],idle=new int[MineHuntMonsters.COUNT],walk=new int[MineHuntMonsters.COUNT],attack=new int[MineHuntMonsters.COUNT];
 private Bitmap portal;private boolean ready;private int builds,fadedPieces;private float playerX,playerY;
 private static final class Piece {final Bitmap image;final float x,y,depth;Piece(Bitmap b,float x,float y,float d){image=b;this.x=x;this.y=y;depth=d;}}
 public MineHuntRenderer(Context c){context=c;p.setFilterBitmap(false);}
 private Bitmap load(String n){try(InputStream in=context.getAssets().open("mine-hunt-v208/"+n+".png")){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;Bitmap b=BitmapFactory.decodeStream(in,null,o);if(b==null)throw new IOException(n);b.prepareToDraw();return b;}catch(Exception e){throw new IllegalStateException("Missing original Mine asset "+n,e);}}
 public void prepare(){if(ready)return;try(InputStream in=context.getAssets().open("mine-hunt-v208/manifest.json")){JSONObject j=new JSONObject(new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));JSONArray ms=j.getJSONArray("monsters");for(int i=0;i<MineHuntMonsters.COUNT;i++){JSONObject m=ms.getJSONObject(i);sheets[0][i]=load("monster_"+i);for(int v=1;v<4;v++){sheets[v][i]=MonsterCursePalette.variant(sheets[0][i],v>=2,(v&1)!=0);sheets[v][i].prepareToDraw();}widths[i]=m.getInt("width");heights[i]=m.getInt("height");ax[i]=m.getInt("anchorX");ay[i]=m.getInt("anchorY");idle[i]=m.getInt("idle");walk[i]=m.getInt("walk");attack[i]=m.getInt("attack");}}catch(Exception e){throw new IllegalStateException(e);}
  HorrorCastleOriginalData.Grid g=MineHuntGeometry.grid();for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++){int bg=g.value(u,v,0);if(bg>0&&!floors.containsKey(bg))floors.put(bg,load("bg"+bg));for(int side=1;side<=2;side++){int id=g.value(u,v,side);if(id<=12)continue;Bitmap b=objects.get(id);if(b==null){b=load(String.format(Locale.ROOT,"stc%05d",id));objects.put(id,b);}float foot=MineHuntGeometry.y(u,v)+16;foreground.add(new Piece(b,MineHuntGeometry.x(u,v)-32+(side-1)*32,foot-b.getHeight()*SCALE,foot));}}
  foreground.sort(Comparator.comparingDouble(a->a.depth));try(InputStream in=context.getAssets().open("assets/world/portal/portal_reagent_shop.webp")){portal=BitmapFactory.decodeStream(in);}catch(Exception e){throw new IllegalStateException(e);}ready=true;
 }
 public void release(){chunks.clear();floors.clear();objects.clear();foreground.clear();actors.clear();for(Bitmap[] state:sheets)Arrays.fill(state,null);portal=null;ready=false;}
 public int fadedPieces(){return fadedPieces;}public int floorBuilds(){return builds;}public int floorEntries(){return chunks.size();}public long floorBytes(){long n=0;for(Bitmap b:chunks.values())n+=b.getAllocationByteCount();return n;}public long atlasBytes(){long n=0;for(Bitmap[] state:sheets)for(Bitmap b:state)if(b!=null)n+=b.getAllocationByteCount();return n;}
 private Bitmap chunk(int cx,int cy){long key=((long)cx<<32)^(cy&0xffffffffL);Bitmap cached=chunks.get(key);if(cached!=null)return cached;Bitmap b=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);c.drawColor(Color.BLACK);float left=cx*256f,top=cy*256f;HorrorCastleOriginalData.Grid g=MineHuntGeometry.grid();
  for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++){float x=MineHuntGeometry.x(u,v)-32,y=MineHuntGeometry.y(u,v)-16;if(x+64<left||x>left+256||y+32<top||y>top+256)continue;Bitmap tile=floors.get(g.value(u,v,0));if(tile!=null){dst.set(x-left,y-top,x-left+64,y-top+27*SCALE);c.drawBitmap(tile,null,dst,p);}}
  b.prepareToDraw();chunks.put(key,b);builds++;if(chunks.size()>32){Iterator<Long> it=chunks.keySet().iterator();it.next();it.remove();}return b;
 }
 private void piece(Canvas c,Piece a,float ox,float oy){float w=a.image.getWidth()*SCALE,h=a.image.getHeight()*SCALE;if(a.x+w<ox+clip.left||a.x>ox+clip.right||a.y+h<oy+clip.top||a.y>oy+clip.bottom)return;dst.set(a.x-ox,a.y-oy,a.x-ox+w,a.y-oy+h);
  // Original entrance footage shows the player through the near wall. Fade only occluding tall foreground.
  boolean occludes=a.image.getHeight()>=48&&a.depth>playerY+2&&a.depth-playerY<150&&playerX>=a.x-12&&playerX<=a.x+w+12&&playerY-40>a.y;
  p.setAlpha(occludes?108:255);if(occludes)fadedPieces++;c.drawBitmap(a.image,null,dst,p);p.setAlpha(255);}
 public void draw(Canvas c,WorldRuntimeAdapter world,List<PoteFieldRenderer.ActorDraw> input){prepare();fadedPieces=0;playerX=world.presentationPlayerX();playerY=world.presentationPlayerY();float ox=world.camera().cameraX(),oy=world.camera().cameraY();c.getClipBounds(clip);p.setColorFilter(null);p.setAlpha(255);for(int cy=(int)Math.floor((oy+clip.top)/256f);cy<=Math.floor((oy+clip.bottom)/256f);cy++)for(int cx=(int)Math.floor((ox+clip.left)/256f);cx<=Math.floor((ox+clip.right)/256f);cx++)c.drawBitmap(chunk(cx,cy),cx*256f-ox,cy*256f-oy,p);
  WorldMoveTargetController.TileCenter e=MineHuntGeometry.exit();dst.set(e.x-24-ox,e.y-15-oy,e.x+24-ox,e.y+9-oy);c.drawBitmap(portal,null,dst,p);
  actors.clear();actors.addAll(input);actors.sort(Comparator.comparingDouble(a->a.y));int i=0;for(PoteFieldRenderer.ActorDraw actor:actors){while(i<foreground.size()&&foreground.get(i).depth<=actor.y)piece(c,foreground.get(i++),ox,oy);actor.draw.run();}while(i<foreground.size())piece(c,foreground.get(i++),ox,oy);actors.clear();
 }
 public float bodyHeight(String id){int t=MineHuntMonsters.type(id);return t<0?50:ay[t]*SCALE;}
 public void monster(Canvas c,RuntimeState.Monster m,SkillEffectState effects){prepare();int t=MineHuntMonsters.type(m.id);if(t<0)return;CharacterRenderer.Direction d=m.visualFacing.presentation();boolean flip=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW;int col=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE?0:1,row=(int)(m.animationClock/.15f)%idle[t];if(m.attackPrimed||m.attackVisualRemaining>0)row=idle[t]+walk[t]+(m.attackPrimed?0:Math.min(attack[t]-1,(int)((1-Math.min(1,m.attackVisualRemaining/.6f))*attack[t])));else if(m.isMoving)row=idle[t]+(int)(m.animationClock/.12f)%walk[t];src.set(col*widths[t],row*heights[t],(col+1)*widths[t],(row+1)*heights[t]);dst.set(m.x-ax[t]*SCALE,m.y-ay[t]*SCALE,m.x+(widths[t]-ax[t])*SCALE,m.y+(heights[t]-ay[t])*SCALE);p.setColorFilter(null);int variant=(MonsterCursePalette.active(effects,m.id)?2:0)+(m.hitFlash>0?1:0);if(flip){c.save();c.scale(-1,1,m.x,m.y);}c.drawBitmap(sheets[variant][t],src,dst,p);if(flip)c.restore();
 }
}
