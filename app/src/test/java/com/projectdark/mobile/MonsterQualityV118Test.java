package com.projectdark.mobile;
import android.graphics.*;
import android.content.SharedPreferences;
import android.os.SystemClock;
import com.projectdark.mobile.world.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MonsterQualityV118Test {
 static Field field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f;}
 static Object get(Object o,String name)throws Exception{return field(o,name).get(o);}
 @Test public void allFourteenFamiliesLoadFullResolutionAndKeepTransparentGroundAnchor()throws Exception{
  PoteFieldRenderer renderer=new PoteFieldRenderer();Method load=PoteFieldRenderer.class.getDeclaredMethod("bitmap",String.class);load.setAccessible(true);
  Set<String> keys=new HashSet<>();int count=0;
  for(String id:new String[]{"POTE_RED","POTE_GREEN","POTE_PURPLE","POTE_SILVER","POTE_LYCAN","POTE_TREANT","POTE_ANTLION","POTE_GNOLL","POTE_WOLFRIDER","POTE_ANTGIANT","POTE_SILVERWOLF","POTE_MANTIS","POTE_SPIRIT#0","POTE_SPIRIT#1"}){
   assertTrue(keys.add(PoteForestMonsterShowcase.artKey(id)));
   for(String pose:new String[]{"idle","walk","attack"})for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
    Bitmap source=(Bitmap)load.invoke(renderer,PoteForestMonsterShowcase.assetPath(id,pose,direction));assertNotNull(source);assertEquals(192,source.getWidth());assertEquals(192,source.getHeight());
    assertEquals(0,source.getPixel(0,0)>>>24);assertEquals(0,source.getPixel(191,191)>>>24);
    Bitmap frame=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);renderer.drawMonsterTestPose(new Canvas(frame),id,pose,direction,.5f,0,64,112);
    int pixels=0;for(int y=0;y<128;y++)for(int x=0;x<128;x++)if((frame.getPixel(x,y)>>>24)>64)pixels++;
    assertTrue(id+" "+pose+" "+direction,pixels>100);count++;
   }
  }
  assertEquals(168,count);System.out.println("V118_FULL_RESOLUTION_POSES="+count);
 }
 @Test public void mapEntryPreparesEveryPoseAndHitWithoutCacheGrowthDuringTurns()throws Exception{
  PoteFieldRenderer renderer=new PoteFieldRenderer();RuntimeState runtime=new RuntimeState();
  Set<String> spiritVariants=new HashSet<>();
  for(String map:new String[]{"MAP_POTE_01","MAP_POTE_02","MAP_POTE_03",CampaignWorld.BOSS_D}){
   runtime.enterCampaignMap(map,false);renderer.prepareMonsters(runtime);
   int sourceCount=((Map<?,?>)get(renderer,"cache")).size(),hitCount=((Map<?,?>)get(renderer,"hitCache")).size();assertTrue(hitCount>=12);
   for(RuntimeState.Monster m:runtime.monsters()){
    if("POTE_SPIRIT".equals(PoteForestMonsterShowcase.species(m.id)))spiritVariants.add(PoteForestMonsterShowcase.artKey(m.id));
    for(String pose:new String[]{"idle","walk","attack"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())renderer.drawMonsterTestPose(new Canvas(Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888)),m.id,pose,d,.5f,0,64,112,true);
   }
   assertEquals(sourceCount,((Map<?,?>)get(renderer,"cache")).size());assertEquals(hitCount,((Map<?,?>)get(renderer,"hitCache")).size());
  }
  assertEquals(new HashSet<>(Arrays.asList("campaign_v3/brown_pote_spirit","campaign_v3/black_pote_spirit")),spiritVariants);
 }
 @Test public void livePeriodicFrameQueuesSaveAndPauseCommitsNewestSnapshot()throws Exception{
  android.content.Context context=RuntimeEnvironment.getApplication();SharedPreferences actual=context.getSharedPreferences("project_dark_f5m_v1",0);actual.edit().clear().commit();F5mSaveStore.install(context);
  GameView view=new GameView(context);RuntimeState runtime=(RuntimeState)get(view,"state");Object store=F5mSaveStore.class.getDeclaredField("active");Field active=F5mSaveStore.class.getDeclaredField("active");active.setAccessible(true);store=active.get(null);
  int[] calls=new int[2];
  SharedPreferences tracked=(SharedPreferences)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{SharedPreferences.class},(proxy,m,args)->{
   if(m.getName().equals("edit")){SharedPreferences.Editor delegate=actual.edit();return Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{SharedPreferences.Editor.class},(ep,em,ea)->{
    if(em.getName().equals("apply"))calls[0]++;if(em.getName().equals("commit"))calls[1]++;
    Object result=em.invoke(delegate,ea);return result instanceof SharedPreferences.Editor?ep:result;
   });}return m.invoke(actual,args);
  });
  field(store,"prefs").set(store,tracked);field(view,"running").setBoolean(view,true);field(view,"last").setLong(view,SystemClock.uptimeMillis());field(view,"checkpointClock").setFloat(view,2f);field(view,"savedLedgerSequence").setLong(view,runtime.ledger().sequence());
  ((Runnable)get(view,"loop")).run();assertEquals(1,calls[0]);assertEquals("periodic frame must not wait for disk",0,calls[1]);
  runtime.player().x+=32;view.pause();assertEquals(1,calls[1]);assertEquals(runtime.player().x,actual.getFloat("player_x",0),0);
  System.out.println("V118_PERIODIC_APPLY=1; PERIODIC_COMMIT=0; PAUSE_COMMIT=1");
 }
}
