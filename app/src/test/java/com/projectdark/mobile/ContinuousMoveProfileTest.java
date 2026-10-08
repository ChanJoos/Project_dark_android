package com.projectdark.mobile;
import android.graphics.*;
import android.view.MotionEvent;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
/** Paired V118/candidate live updates near real actors; include cold chunk boundaries and tails. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ContinuousMoveProfileTest {
 @Test public void longMovementProfilesMillesAndForestWithLiveChase()throws Exception{
  for(String n:new String[]{"project_dark_f5m_v1","project_dark_journal_v1","project_dark_skill_test_v1"})RuntimeEnvironment.getApplication().getSharedPreferences(n,0).edit().clear().commit();
  F5mSaveStore.install(RuntimeEnvironment.getApplication());GameView view=new GameView(RuntimeEnvironment.getApplication());view.layout(0,0,1536,864);
  Method change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class),enter=GameView.class.getDeclaredMethod("enterPoteField"),update=GameView.class.getDeclaredMethod("update",float.class);
  for(Method m:new Method[]{change,enter,update})m.setAccessible(true);JSONArray scenes=new JSONArray();
  for(String scene:new String[]{"MILLES","MAP_POTE_01","MAP_POTE_03"}){
   if(!scene.equals("MILLES")){if(scene.equals("MAP_POTE_01"))enter.invoke(view);else change.invoke(view,scene,false);}
   RuntimeState state=TownInteriorTest.field(view,"state");WorldRuntimeAdapter world=TownInteriorTest.field(view,scene.equals("MILLES")?"worldAdapter":"poteFieldAdapter");
   RuntimeState.Monster near=state.monsters().get(0);state.player().x=near.x-96;state.player().y=near.y-48;world.snapCameraToPlayer();
   Bitmap frame=Bitmap.createBitmap(1536,864,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(frame);long[] updates=new long[360],draws=new long[360];double travel=0;int moving=0;
   float originX=state.player().x,originY=state.player().y;
   for(int i=0;i<360;i++){
    if(i%90==0){float offset=(i/90)%2==0?384:-192;world.requestGroundWorld(originX+offset,originY+offset*.5f);}
    state.player().hp=state.player().maxHp;state.player().alive=true;
    float x=world.presentationPlayerX(),y=world.presentationPlayerY();long a=System.nanoTime();update.invoke(view,.016f);long b=System.nanoTime();view.draw(canvas);long c=System.nanoTime();updates[i]=b-a;draws[i]=c-b;
    double delta=Math.hypot(world.presentationPlayerX()-x,world.presentationPlayerY()-y);travel+=delta;if(delta>.001)moving++;
   }
   assertTrue(scene+" actual interpolated travel",travel>20&&moving>30);Arrays.sort(updates);Arrays.sort(draws);
   JSONObject row=new JSONObject().put("map",scene).put("monsters",state.monsters().size()).put("tiles",world.navigationTiles().size()).put("travel",travel).put("movingFrames",moving);
   for(int n=0;n<2;n++){long[] a=n==0?updates:draws;String s=n==0?"update":"render";row.put(s+"MedianMs",a[180]/1e6).put(s+"P95Ms",a[342]/1e6).put(s+"P99Ms",a[356]/1e6).put(s+"MaxMs",a[359]/1e6);}
   scenes.put(row);File image=new File("build/reports/forest-performance/continuous-"+scene+".png");image.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(image)){frame.compress(Bitmap.CompressFormat.PNG,100,out);}frame.recycle();
  }
  JSONObject result=new JSONObject().put("source",System.getenv("PROJECT_DARK_SOURCE_SHA")).put("environment","Robolectric native CPU; no physical phone FPS claim").put("framesPerScene",360).put("scenes",scenes);
  File file=new File("build/reports/forest-performance/CONTINUOUS.json");file.getParentFile().mkdirs();try(FileWriter out=new FileWriter(file)){out.write(result.toString(2));}System.out.println(result);
 }
}
