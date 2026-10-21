package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

/** Real update + draw, including cold samples. Host CPU measurements are not handset FPS. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class HudInventoryPerformanceV218Test {
 Context c;
 @Before public void reset(){c=RuntimeEnvironment.getApplication();for(String n:new String[]{"project_dark_f5m_v1","project_dark_journal_v1","project_dark_skill_test_v1"})c.getSharedPreferences(n,0).edit().clear().commit();F5mSaveStore.install(c);}
 static Object call(GameView v,String name,Class<?>[] types,Object... args)throws Exception{Method m=GameView.class.getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(v,args);}
 static int quantity(GameView v,String id)throws Exception{return (Integer)call(v,"inventoryQuantity",new Class<?>[]{String.class},id);}
 static int previous(RpgProgressionState r,String id){for(RpgInventoryPresentation.ItemRow row:new RpgInventoryPresentation().inventoryRows(r))if(row.itemId.equals(id))return row.quantity;return 0;}
 static JSONObject stats(long[] values)throws Exception{long[] x=values.clone();Arrays.sort(x);return new JSONObject().put("p50ms",x[x.length/2]/1e6).put("p95ms",x[Math.min(x.length-1,(int)(x.length*.95))]/1e6).put("p99ms",x[Math.min(x.length-1,(int)(x.length*.99))]/1e6).put("maxms",x[x.length-1]/1e6);}
 static void report(String name,JSONObject data)throws Exception{File f=new File("build/reports/forest-performance/v218/"+name+".json");f.getParentFile().mkdirs();try(FileWriter w=new FileWriter(f)){w.write(data.toString(2));}System.out.println("V218_"+name+" "+data);}
 @Test public void livePotionCountsMatchOldProjectionThroughUseLootBuyAndRestore()throws Exception{
  GameView v=new GameView(c);RuntimeState s=TownInteriorTest.field(v,"state");RpgProgressionState r=s.rpg();String hp=RpgProgressionState.B_SMALL_POTION_ITEM_ID,mp="IT_B_MP_POTION";
  for(String id:new String[]{hp,mp,"NOT_OWNED"})assertEquals(previous(r,id),quantity(v,id));
  int before=quantity(v,hp);assertEquals(RpgProgressionState.AutoLootResult.LOOTED,r.autoLootResolvedItem(hp,3));assertEquals(before+3,quantity(v,hp));s.player().hp=1;assertEquals(RpgProgressionState.UseResult.USED,r.useQuickConsumable(hp,s));assertEquals(before+2,quantity(v,hp));
  r.restoreGold(10000);int m=quantity(v,mp);assertTrue(r.buyItem(mp,20));assertEquals(m+1,quantity(v,mp));s.player().mp=0;assertEquals(RpgProgressionState.UseResult.USED,r.useQuickConsumable(mp,s));assertEquals(m,quantity(v,mp));
  for(String id:new String[]{hp,mp})assertEquals(previous(r,id),quantity(v,id));assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(c);GameView restored=new GameView(c);assertEquals(quantity(v,hp),quantity(restored,hp));assertEquals(quantity(v,mp),quantity(restored,mp));
 }
 @Test public void actualHudNeverBuildsInventoryRowsAndPairedLookupProfilesKeepSameItems()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,1536,864);RuntimeState s=TownInteriorTest.field(v,"state");RpgProgressionState r=s.rpg();r.enableEquipmentSandbox(true);assertTrue(r.inventory().size()>1500);
  Field f=GameView.class.getDeclaredField("rpgPresentation");f.setAccessible(true);f.set(v,new RpgInventoryPresentation(){@Override public List<ItemRow> inventoryRows(RpgProgressionState ignored){throw new AssertionError("HUD rebuilt full inventory projection");}});
  Bitmap b=Bitmap.createBitmap(1536,864,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));b.recycle();
  String[] ids={r.quickHpPotionId(),"IT_B_MP_POTION"};long[] old=new long[80],now=new long[80];int checksum=0;
  for(int i=0;i<90;i++){long a=System.nanoTime();int x=previous(r,ids[0])+previous(r,ids[1]);long z=System.nanoTime();int y=quantity(v,ids[0])+quantity(v,ids[1]);long end=System.nanoTime();assertEquals(x,y);checksum+=y;if(i>=10){old[i-10]=z-a;now[i-10]=end-z;}}
  report("paired-quantity",new JSONObject().put("environment","same host, same ownership; not phone FPS").put("items",r.inventory().size()).put("checksum",checksum).put("previousTwoFullProjections",stats(old)).put("directTwoLiveQuantities",stats(now)));
 }
 @Test public void fullLiveFramesKeepAllActorsAndProfileMineCoastMillesAndTwelveTrips()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,1536,864);RuntimeState s=TownInteriorTest.field(v,"state");s.rpg().enableEquipmentSandbox(true);s.rpg().restoreProgression(99,0);
  Bitmap image=Bitmap.createBitmap(1536,864,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);JSONArray scenes=new JSONArray();
  for(String id:new String[]{"MILLES","MAP_RUCESION_COAST_1C",MineHuntGeometry.ID}){
   if(!id.equals("MILLES")){if(!(Boolean)TownInteriorTest.field(v,"inPoteField"))call(v,"enterPoteField",new Class<?>[]{});call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},id,false);}
   WorldRuntimeAdapter w=TownInteriorTest.field(v,id.equals("MILLES")?"worldAdapter":"poteFieldAdapter");int population=s.monsters().size();float originX=s.player().x,originY=s.player().y;long[] update=new long[240],draw=new long[240],input=new long[8],save=new long[4];double travel=0;
   for(int i=0;i<240;i++){
    if(i%30==0){long t=System.nanoTime();w.requestGroundWorld(originX+(i/30%2==0?160:-64),originY+(i/30%2==0?80:-32));input[i/30]=System.nanoTime()-t;}
    s.player().alive=true;s.player().hp=s.player().maxHp;float x=w.presentationPlayerX(),y=w.presentationPlayerY();long a=System.nanoTime();F5mSaveStore.beginFrame();try{call(v,"update",new Class<?>[]{float.class},.016f);}finally{F5mSaveStore.endFrame();}w.camera().follow(w.presentationPlayerX(),w.presentationPlayerY());long z=System.nanoTime();v.draw(canvas);long end=System.nanoTime();update[i]=z-a;draw[i]=end-z;travel+=Math.hypot(w.presentationPlayerX()-x,w.presentationPlayerY()-y);
    if(i%60==0){long t=System.nanoTime();assertTrue(F5mSaveStore.autosaveActive());save[i/60]=System.nanoTime()-t;}
   }
   assertEquals(population,s.monsters().size());assertTrue(id+" actual travel "+travel,travel>20);if(id.equals(MineHuntGeometry.ID))assertEquals(100,population);
   JSONObject row=new JSONObject().put("map",id).put("population",population).put("inventory",s.rpg().inventory().size()).put("travel",travel).put("updateIncludingCold",stats(update)).put("drawIncludingCold",stats(draw)).put("input",stats(input)).put("autosave",stats(save));scenes.put(row);
   File shot=new File("build/reports/forest-performance/v218/"+id+".png");shot.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(shot)){assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));}
  }
  long[] trips=new long[12];for(int i=0;i<12;i++){String id=(i&1)==0?"MAP_RUCESION_COAST_1C":MineHuntGeometry.ID;long a=System.nanoTime();call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},id,false);v.draw(canvas);trips[i]=System.nanoTime()-a;if((i&1)!=0){MineHuntRenderer r=TownInteriorTest.field(v,"mineHuntRenderer");assertTrue(r.floorEntries()<=32);assertTrue(r.floorBytes()<=8652800);assertEquals(100,s.monsters().size());}else{MineHuntRenderer r=TownInteriorTest.field(v,"mineHuntRenderer");assertEquals(0,r.floorEntries());assertEquals(0,r.atlasBytes());}}
  report("live-frames",new JSONObject().put("source",System.getenv("PROJECT_DARK_SOURCE_SHA")).put("environment","Robolectric native CPU, real update+draw+live actors; not handset FPS").put("scenes",scenes).put("twelveTripsEntryAndFirstDraw",stats(trips)));
  image.recycle();
 }
}
