package com.projectdark.mobile;
import android.content.*;import android.graphics.*;import java.util.*;import java.io.*;import java.lang.reflect.*;import org.junit.*;import static org.junit.Assert.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class InventoryRepairV212Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 @Test public void recoveredNamedEquipmentHasRealRegisteredSourceFramesAndEquips(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);r.bindTwoHandLearning(()->true);EquipmentVisualRegistry registry=new EquipmentVisualRegistry(c);int count=0;
  for(String id:RecoveredWearablesV212.IDS){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);assertNotNull(id,d);assertNotNull(id,d.appearanceId);assertFalse(id,r.inventoryOnlyItem(id));EquipmentVisualRegistry.Visual v=registry.get(d.appearanceId);assertNotNull(id,v);assertNotNull(id,v.registration);
   for(CharacterRenderer.Direction dir:new CharacterRenderer.Direction[]{CharacterRenderer.Direction.NE,CharacterRenderer.Direction.NW,CharacterRenderer.Direction.SE,CharacterRenderer.Direction.SW}){SourceEquipmentRegistration.Frame f=v.registration.idle(dir,0);assertNotNull(id+dir,f);assertTrue(id,f.src.width()>0&&f.src.height()>0);assertTrue(id+dir+" source bounds "+f.src+" atlas "+v.atlas.getWidth()+"x"+v.atlas.getHeight(),f.src.left>=0&&f.src.top>=0&&f.src.right<=v.atlas.getWidth()&&f.src.bottom<=v.atlas.getHeight());boolean visible=false;for(int y=f.src.top;y<f.src.bottom&&!visible;y++)for(int x=f.src.left;x<f.src.right;x++)if(Color.alpha(v.atlas.getPixel(x,y))>0){visible=true;break;}assertTrue(id+dir,visible);}
   RpgProgressionState.EquipResult result=r.equip(id);assertTrue(id+result,result==RpgProgressionState.EquipResult.EQUIPPED||result==RpgProgressionState.EquipResult.UNEQUIPPED);count++;
  }assertTrue("recover the majority, not just a sample",count>1000);
 }
 @Test public void repeatedDetailsUseOneProjectionAndMutationsRefreshImmediately(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);ItemWindow window=new ItemWindow(c);String id=RpgProgressionState.canonicalItemId("IT_TEST_WEAPON_MW002");window.rows(r);int builds=window.rowBuildCount();long[] times=new long[120];
  for(int i=0;i<times.length;i++){long t=System.nanoTime();for(int n=0;n<15;n++)assertNotNull(window.find(r,id));window.rows(r);times[i]=System.nanoTime()-t;}
  assertEquals(builds,window.rowBuildCount());Arrays.sort(times);System.out.println("V212_DETAIL_15_LOOKUPS_P50_P95_P99_NS "+times[60]+" "+times[114]+" "+times[118]);
  int q=r.inventory().get(id);assertEquals(RpgProgressionState.AutoLootResult.LOOTED,r.autoLootResolvedItem(id,1));assertEquals(q+1,window.find(r,id).quantity);assertEquals(builds+1,window.rowBuildCount());r.equip(id);assertTrue(window.find(r,id).equipped);assertEquals(builds+2,window.rowBuildCount());
 }
 @Test public void stationaryAutosavesReuseOwnershipJsonAndChangedItemsSurviveRestart()throws Exception{
  GameView v=new GameView(c);RuntimeState s=TownInteriorTest.field(v,"state");assertTrue(F5mSaveStore.checkpointActive());Field field=F5mSaveStore.class.getDeclaredField("active");field.setAccessible(true);F5mSaveStore store=(F5mSaveStore)field.get(null);int count=store.ownershipSerializationCount();
  for(int i=0;i<60;i++)assertTrue(F5mSaveStore.autosaveActive());assertEquals(count,store.ownershipSerializationCount());String id=RpgProgressionState.canonicalItemId("IT_TEST_WEAPON_MW002");int q=s.rpg().inventory().get(id);s.rpg().autoLootResolvedItem(id,1);assertTrue(F5mSaveStore.checkpointActive());assertEquals(count+1,store.ownershipSerializationCount());F5mSaveStore.install(c);GameView next=new GameView(c);RuntimeState reopened=TownInteriorTest.field(next,"state");assertEquals(Integer.valueOf(q+1),reopened.rpg().inventory().get(id));
 }
 @Test public void repairedRapidAndFoodsHaveTransparentSocketBorderAndSharedWindowPixels(){
  SourceItemIconRegistry icons=new SourceItemIconRegistry(c);RpgProgressionState r=new RpgProgressionState();String[] ids={"USER_ITEM_20261018_R05141","IT_NA_FOOD_293294_00","IT_NA_FOOD_293294_24","IT_NA_FOOD_293294_37"};
  for(String id:ids){Bitmap b=icons.get(r.itemDefinitions().get(id));assertNotNull(id,b);assertSame(b,icons.get(id));for(int x=0;x<b.getWidth();x++){assertEquals(id,0,Color.alpha(b.getPixel(x,0)));assertEquals(id,0,Color.alpha(b.getPixel(x,b.getHeight()-1)));}assertFalse(id,b.sameAs(Bitmap.createBitmap(b.getWidth(),b.getHeight(),Bitmap.Config.ARGB_8888)));}
 }
 @Test public void actualRecoveredEquipmentAndFoodInventoryRender()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=TownInteriorTest.field(v,"state");s.rpg().enableEquipmentSandbox(true);Set<String> slots=new HashSet<>();
  for(String id:RecoveredWearablesV212.IDS){RpgProgressionState.ItemDefinition d=s.rpg().itemDefinitions().get(id);if(slots.add(d.equipSlot))assertEquals(id,RpgProgressionState.EquipResult.EQUIPPED,s.rpg().equip(id));}
  Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File out=new File("build/reports/horror-v179/v212/recovered-equipment.png");out.getParentFile().mkdirs();try(FileOutputStream f=new FileOutputStream(out)){b.compress(Bitmap.CompressFormat.PNG,100,f);}b.recycle();
 }
}
