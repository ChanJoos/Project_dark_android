package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.lang.reflect.*;
import java.io.File;
import java.util.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class PoteReferenceV113Test {
 @Before public void clear(){for(String n:new String[]{"project_dark_f5m_v1","project_dark_journal_v1","project_dark_skill_test_v1"})RuntimeEnvironment.getApplication().getSharedPreferences(n,0).edit().clear().commit();F5mSaveStore.install(RuntimeEnvironment.getApplication());}
 @Test public void mapsHaveDistributedPopulationAndRealShortRespawn(){
  RuntimeState state=new RuntimeState();
  for(int z=1;z<=3;z++){
   state.enterCampaignMap("MAP_POTE_0"+z,false);List<RuntimeState.Monster> ms=state.monsters();assertEquals(new int[]{72,96,108}[z-1],ms.size());
   float minX=Float.MAX_VALUE,maxX=0,minY=Float.MAX_VALUE,maxY=0;
   for(RuntimeState.Monster m:ms){minX=Math.min(minX,m.x);maxX=Math.max(maxX,m.x);minY=Math.min(minY,m.y);maxY=Math.max(maxY,m.y);assertTrue(m.respawnSeconds>=6&&m.respawnSeconds<=8);}
   assertTrue(maxX-minX>2800);assertTrue(maxY-minY>1000);
   RuntimeState.Monster m=ms.get(0);state.damage(m,m.hp);state.tick(5.9f);assertFalse(m.alive);state.tick(.11f);assertTrue(m.alive);assertEquals(m.maxHp,m.hp);
  }
  state.enterCampaignMap(CampaignWorld.BOSS_D,false);assertEquals(1,state.monsters().size());assertTrue(Float.isInfinite(state.monsters().get(0).respawnSeconds));
 }
 @Test public void quickQuestFiltersWholeMapAndManualAutoClearsFilter()throws Exception{
  GameView v=new GameView(RuntimeEnvironment.getApplication());v.layout(0,0,960,540);invoke(v,"enterPoteField");RuntimeState state=TownInteriorTest.field(v,"state");
  state.rpg().grantAdaptedReward(5_000_000,0);assertTrue(state.rpg().chooseInitialJob("WARRIOR"));CampaignProgress cp=state.rpg().campaign();
  org.json.JSONArray completed=new org.json.JSONArray();for(CampaignProgress.Def d:CampaignProgress.definitions())if(d.id.startsWith("M")||d.id.startsWith("T")||d.id.equals("J01_WARRIOR")||d.id.equals("A01"))completed.put(d.id);
  assertTrue(cp.restore(cp.snapshot().put("version",4).put("complete",completed).put("active","A02")));
  for(RuntimeState.Monster m:state.monsters())m.alive=false;
  WorldRuntimeAdapter world=TownInteriorTest.field(v,"poteFieldAdapter");WorldMoveTargetController.TileCenter far=PoteCampaignMapDef.forId(state.currentMapId()).nearest(3000,1400),near=PoteCampaignMapDef.forId(state.currentMapId()).nearest(state.player().x+96,state.player().y+48);
  RuntimeState.Monster red=state.monsters().get(0),green=state.monsters().get(1);red.alive=green.alive=true;red.x=far.x;red.y=far.y;green.x=near.x;green.y=near.y;
  assertEquals("A02",cp.next(state.rpg()).id);TownInteriorTest.tap(v,790,104);CombatController combat=TownInteriorTest.field(v,"combat");assertSame("A02 red hunt acquires the remote red, ignoring near green",red,combat.target());assertTrue(Math.hypot(red.x-state.player().x,red.y-state.player().y)>1000);
  assertSame(red,world.selectAutoTarget(m->m.campaignRewardProfileId.equals("POTE_RED")));
  // Visible manual AUTO off/on must discard the quickquest species filter.
  TownInteriorTest.tap(v,842,518);TownInteriorTest.tap(v,842,518);assertNull(TownInteriorTest.field(v,"autoHuntQuestId"));assertSame(green,combat.target());
 }
 @Test public void nativeSceneShowsAllMapsAndOccludedPlayerMonsterNpc()throws Exception{
  GameView v=new GameView(RuntimeEnvironment.getApplication());v.layout(0,0,1536,864);invoke(v,"enterPoteField");RuntimeState state=TownInteriorTest.field(v,"state");
  String[] maps={"MAP_POTE_01","MAP_POTE_02","MAP_POTE_03",CampaignWorld.BOSS_D};
  for(String map:maps){Method change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class);change.setAccessible(true);change.invoke(v,map,false);PoteCampaignMapDef d=PoteCampaignMapDef.forId(map);WorldRuntimeAdapter w=TownInteriorTest.field(v,"poteFieldAdapter");
   float[][] points={{d.entryX,d.entryY},{d.bridgeX(),d.bridgeY()},{(d.minX+d.maxX)/2,(d.minY+d.maxY)/2},{d.nextX,d.nextY}};
   for(int i=0;i<points.length;i++){w.camera().snapTo(points[i][0],points[i][1]);capture(v,map+"-"+i);}
  }
  invoke(v,"enterPoteField");state=TownInteriorTest.field(v,"state");WorldRuntimeAdapter w=TownInteriorTest.field(v,"poteFieldAdapter");
  Method pm=PoteFieldRenderer.class.getDeclaredMethod("placementsForMap",String.class);pm.setAccessible(true);List<?> props=(List<?>)pm.invoke(null,"MAP_POTE_01");Object chosen=null;float best=Float.MAX_VALUE;
  for(Object p:props){Field role=p.getClass().getDeclaredField("role");role.setAccessible(true);if(role.get(p).equals("canopy")){Field px=p.getClass().getDeclaredField("x"),py=p.getClass().getDeclaredField("y");px.setAccessible(true);py.setAccessible(true);float score=(float)Math.hypot(px.getFloat(p)-2000,py.getFloat(p)-1100);if(score<best){best=score;chosen=p;}}}assertNotNull(chosen);
  Field xf=chosen.getClass().getDeclaredField("x"),yf=chosen.getClass().getDeclaredField("y");xf.setAccessible(true);yf.setAccessible(true);float x=xf.getFloat(chosen),y=yf.getFloat(chosen);
  // Isolate a central foreground tree; the fixture is a visual pose, not a navigation request.
  PoteFieldRenderer checkRenderer=TownInteriorTest.field(v,"poteFieldRenderer");Field visible=PoteFieldRenderer.class.getDeclaredField("visibleActors");visible.setAccessible(true);visible.set(checkRenderer,Collections.singletonList(new PoteFieldRenderer.ActorDraw(x,y-80,52,32,()->{})));Method bitmap=PoteFieldRenderer.class.getDeclaredMethod("bitmap",String.class);bitmap.setAccessible(true);Field asset=chosen.getClass().getDeclaredField("asset");asset.setAccessible(true);Bitmap tree=(Bitmap)bitmap.invoke(checkRenderer,asset.get(chosen));Method overlaps=PoteFieldRenderer.class.getDeclaredMethod("occludesActor",chosen.getClass(),Bitmap.class);overlaps.setAccessible(true);assertTrue("fixture overlaps actual opaque foreground foliage",(Boolean)overlaps.invoke(checkRenderer,chosen,tree));visible.set(checkRenderer,Collections.emptyList());
  state.player().x=x;state.player().y=y-80;w.cancel();w.camera().snapTo(x,y-80);capture(v,"occluded-player");
  Bitmap control=Bitmap.createBitmap(1536,864,Bitmap.Config.ARGB_8888);Canvas cc=new Canvas(control);cc.scale(1.6f,1.6f);checkRenderer.drawScene(cc,w,Collections.emptyList());saveBitmap(control,"occlusion-control");
  RuntimeState.Monster mob=state.monsters().get(0);mob.x=x;mob.y=y-80;state.player().x=x-180;state.player().y=y+20;w.cancel();w.camera().snapTo(x,y-80);capture(v,"occluded-monster");
  mob.alive=false;RuntimeState.Npc npc=state.npcs().get(0);Field nx=RuntimeState.Npc.class.getDeclaredField("x"),ny=RuntimeState.Npc.class.getDeclaredField("y");nx.setAccessible(true);ny.setAccessible(true);nx.setFloat(npc,x);ny.setFloat(npc,y-80);capture(v,"occluded-npc");
  // Pixel evidence: actual foreground tree must transmit actor pixels and remain opaque elsewhere.
  PoteFieldRenderer renderer=TownInteriorTest.field(v,"poteFieldRenderer");
  Bitmap portal=Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);renderer.drawPortalWorld(new Canvas(portal),50,50);int portalPixels=0;for(int yy=0;yy<100;yy++)for(int xx=0;xx<100;xx++)if(Color.alpha(portal.getPixel(xx,yy))>64)portalPixels++;assertTrue("established portal resolves its real packaged AssetManager path",portalPixels>100);
  Bitmap empty=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888),marked=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);Canvas ec=new Canvas(empty),mc=new Canvas(marked);
  renderer.drawScene(ec,w,Collections.emptyList());WorldCameraTransform.Point q=w.worldToScreen(x,y-80);Paint ink=new Paint();ink.setColor(Color.MAGENTA);
  renderer.drawScene(mc,w,Collections.singletonList(new PoteFieldRenderer.ActorDraw(x,y-80,48,24,()->mc.drawRect(q.x-10,q.y-48,q.x+10,q.y,ink))));
  int transmitted=0;for(int yy=Math.max(0,(int)q.y-48);yy<Math.min(540,(int)q.y);yy++)for(int xx=Math.max(0,(int)q.x-10);xx<Math.min(960,(int)q.x+10);xx++){int a=marked.getPixel(xx,yy);if(Color.red(a)>Color.red(empty.getPixel(xx,yy))+50&&Color.blue(a)>100)transmitted++;}
  assertTrue("foreground foliage transmits the actor colour through its alpha footprint",transmitted>80);
 }
 static void invoke(Object o,String name)throws Exception{Method m=o.getClass().getDeclaredMethod(name);m.setAccessible(true);m.invoke(o);}
 static void capture(GameView view,String name)throws Exception{
  Bitmap b=Bitmap.createBitmap(1536,864,Bitmap.Config.ARGB_8888);view.draw(new Canvas(b));saveBitmap(b,name);
 }
 static void saveBitmap(Bitmap b,String name)throws Exception{
  int[] px=new int[1536*864];b.getPixels(px,0,1536,0,0,1536,864);
  Class<?> type=Class.forName("java.awt.image.BufferedImage");Object image=type.getConstructor(int.class,int.class,int.class).newInstance(1536,864,2);type.getMethod("setRGB",int.class,int.class,int.class,int.class,int[].class,int.class,int.class).invoke(image,0,0,1536,864,px,0,1536);
  File out=new File("build/reports/pote-v113/"+name+".png");out.getParentFile().mkdirs();Class.forName("javax.imageio.ImageIO").getMethod("write",Class.forName("java.awt.image.RenderedImage"),String.class,File.class).invoke(null,image,"png",out);b.recycle();
 }
}
