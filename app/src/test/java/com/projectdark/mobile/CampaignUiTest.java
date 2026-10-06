package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.lang.reflect.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CampaignUiTest {
 Context context;
 @Before public void reset(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
 @Test public void choiceButtonsGrantEachJobAndFreeSkillsAndPersistWithoutTestMode()throws Exception{
  for(int index=0;index<5;index++){
   reset();GameView v=new GameView(context);v.layout(0,0,960,540);RuntimeState s=TownInteriorTest.field(v,"state");s.rpg().grantAdaptedReward(22800,0);
   F5mAdaptedPrologueQuest q=TownInteriorTest.field(v,"f5mQuest");GrowthQuest2 g=TownInteriorTest.field(v,"quest2");q.restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);g.restore(GrowthQuest2.State.COMPLETED,3);s.rpg().campaign().syncOpening(true,true);
   RuntimeState.Npc n=null;for(RuntimeState.Npc candidate:s.npcs())if(candidate.id.equals("milles_job_counselor"))n=candidate;assertNotNull(n);s.player().x=n.x-32;s.player().y=n.y-16;((WorldRuntimeAdapter)TownInteriorTest.field(v,"worldAdapter")).snapCameraToPlayer();((InteractionController)TownInteriorTest.field(v,"interaction")).request(s,n);
   capture(v,"choice-"+index);float x=index<3?218+index*173+77:304+(index-3)*173+77;float y=index<3?411:443;QuestJournalTest.gesture(v,x,y);
   assertEquals(CampaignProgress.JOBS[index],s.rpg().currentJobCode());SkillBook b=TownInteriorTest.field(v,"skillBook");assertFalse(b.testAccess());assertFalse("new job choice must not bypass learning",b.owned(CampaignProgress.beginnerSkill(s.rpg().currentJobCode())));assertTrue(s.rpg().campaign().status(CampaignProgress.find("M03"),s.rpg())==CampaignProgress.Status.REPORT);
   capture(v,"job-"+CampaignProgress.JOBS[index]);v.pause();F5mSaveStore.install(context);RuntimeState restored=TownInteriorTest.field(new GameView(context),"state");assertEquals(s.rpg().currentJobCode(),restored.rpg().currentJobCode());assertEquals(s.rpg().equipment(),restored.rpg().equipment());
  }
 }
 @Test public void pietSupplierOpensTheActualEquipmentShopThroughDialogButton()throws Exception{
  GameView v=new GameView(context);v.layout(0,0,960,540);RuntimeState s=TownInteriorTest.field(v,"state");s.rpg().grantAdaptedReward(22800,0);assertTrue(s.rpg().chooseInitialJob("MAGE"));TownInteriorTest.call(v,"enterPoteField");Method change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class);change.setAccessible(true);change.invoke(v,CampaignWorld.PIET,false);
  RuntimeState.Npc supplier=null;for(RuntimeState.Npc n:s.npcs())if(n.id.equals("piet_supplier"))supplier=n;assertNotNull(supplier);WorldRuntimeAdapter w=TownInteriorTest.field(v,"poteFieldAdapter");assertTrue("supplier path",CampaignNavigationTest.walk(w,w.requestNpcApproach(supplier.id)));assertEquals(InteractionController.TickResult.DIALOG_OPENED,((InteractionController)TownInteriorTest.field(v,"interaction")).request(s,supplier));QuestJournalTest.gesture(v,490,430);assertTrue(((TownShopWindow)TownInteriorTest.field(v,"townWindow")).isOpen());capture(v,"piet-equipment-shop");
 }
 @Test public void everyMapAndAltarRendersAndColdRestartRestoresActualMap()throws Exception{
  GameView v=new GameView(context);v.layout(0,0,960,540);RuntimeState s=TownInteriorTest.field(v,"state");TownInteriorTest.call(v,"enterPoteField");
  Method change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class);change.setAccessible(true);
  for(String map:CampaignWorld.ROUTE){change.invoke(v,map,false);assertEquals(map,s.currentMapId());s.player().x=800;s.player().y=592;((WorldRuntimeAdapter)TownInteriorTest.field(v,"poteFieldAdapter")).snapCameraToPlayer();capture(v,map);v.pause();F5mSaveStore.install(context);GameView restored=new GameView(context);RuntimeState sr=TownInteriorTest.field(restored,"state");assertEquals(map,sr.currentMapId());assertEquals(s.player().x,sr.player().x,.01f);F5mSaveStore.bindRuntime(s,TownInteriorTest.field(v,"f5mQuest"),TownInteriorTest.field(v,"quest2"));}
 }
 static void capture(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File f=new File("build/reports/device-review/v105-campaign-"+name+".png");f.getParentFile().mkdirs();try(OutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();}
}
