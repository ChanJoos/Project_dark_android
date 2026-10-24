package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.util.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class EquipmentWindowV175Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 @Test public void referenceHasExactlyFifteenNonOverlappingReachableSlotsAndOrnamentsBelowEarring()throws Exception{
  assertEquals(15,ItemWindow.SLOTS.length);assertEquals(15,new HashSet<>(Arrays.asList(ItemWindow.SLOTS)).size());
  GameView v=new GameView(c);v.layout(0,0,960,540);ItemWindowReferenceTest.tap(v,830,28);ItemWindow w=ItemWindowReferenceTest.field(v,"itemWindow");
  for(int i=0;i<15;i++){RectF b=ItemWindow.slot(i);assertTrue(b.width()>=48);for(int j=0;j<i;j++)assertFalse(RectF.intersects(b,ItemWindow.slot(j)));ItemWindowReferenceTest.tap(v,b.centerX(),b.centerY());assertEquals(ItemWindow.SLOTS[i],w.selectedSlot);}
  RectF ear=ItemWindow.slot(1),orn=ItemWindow.slot(13);assertEquals(ear.left,orn.left,0);assertTrue(orn.top>ear.bottom);
  w.selectedSlot=null;render(v,"overview.png");
  for(String name:new String[]{"panel","slot"}){assertNotNull(w.equipmentImages.get(name));}
  RectF stat=ItemWindow.equipmentStats();ItemWindowReferenceTest.tap(v,stat.centerX(),stat.centerY());assertTrue((Boolean)ItemWindowReferenceTest.field(v,"statsOpen"));
 }
 @Test public void realEquipUnequipChangesLiveStatRasterAndRestoresAfterRestart()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(v,"state");RpgProgressionState r=state.rpg();ItemWindowReferenceTest.tap(v,830,28);
  Bitmap before=render(v,"before.png");int oldStr=r.finalStats().str;
  r.equipToSlot("IT_EARRING_DOUBLE_SILVER","귀걸이");assertEquals(oldStr+1,r.finalStats().str);Bitmap after=render(v,"earring-equipped.png");
  int changes=0;for(int y=442;y<507;y++)for(int x=224;x<668;x++)if(before.getPixel(x,y)!=after.getPixel(x,y))changes++;assertTrue("Live footer numbers must change",changes>5);
  F5mSaveStore.saveRewardsActive(r);GameView restart=new GameView(c);RuntimeState rs=ItemWindowReferenceTest.field(restart,"state");assertEquals(r.finalStats().str,rs.rpg().finalStats().str);assertEquals(r.equipment(),rs.rpg().equipment());
  int i=Arrays.asList(ItemWindow.SLOTS).indexOf("귀걸이");RectF b=ItemWindow.slot(i);ItemWindowReferenceTest.tap(v,b.centerX(),b.centerY());ItemWindowReferenceTest.tap(v,805,490);assertEquals(oldStr,r.finalStats().str);render(v,"earring-unequipped.png");
 }
 @Test public void ornamentsAreIndependentCountedOnceAndSaveWithoutInventingProductionItems(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);register(r);int ac=r.finalStats().ac,str=r.finalStats().str;
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot("TEST_ORNAMENT","장식"));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot("TEST_ORNAMENT",RpgProgressionState.SECOND_ORNAMENT_SLOT));assertEquals(ac-4,r.finalStats().ac);assertEquals(str+6,r.finalStats().str);
  RpgProgressionState restored=new RpgProgressionState();register(restored);assertTrue(restored.restoreOwnedItems(r.inventory(),r.equipment()));assertEquals(r.equipment(),restored.equipment());assertEquals(r.finalStats().str,restored.finalStats().str);
  r.unequip(RpgProgressionState.SECOND_ORNAMENT_SLOT);assertEquals("TEST_ORNAMENT",r.equipment().get("장식"));assertEquals(str+3,r.finalStats().str);assertEquals(RpgProgressionState.EquipResult.INVALID_SLOT,r.equipToSlot("TEST_ORNAMENT","반지"));
 }
 void register(RpgProgressionState r){Map<String,Integer> stats=new HashMap<>();stats.put("STR",3);stats.put("AC",-2);r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("TEST_ORNAMENT","검증용 장식","장식",1,Collections.emptySet(),false,null,null,stats,RpgProgressionState.Evidence.B));r.autoLootResolvedItem("TEST_ORNAMENT",2);}
 Bitmap render(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File d=new File("build/reports/equipment-v175");d.mkdirs();try(FileOutputStream o=new FileOutputStream(new File(d,name))){b.compress(Bitmap.CompressFormat.PNG,100,o);}return b;}
}
