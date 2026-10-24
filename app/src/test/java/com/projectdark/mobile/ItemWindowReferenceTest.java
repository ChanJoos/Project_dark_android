package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.*;
import android.graphics.*;
import android.view.MotionEvent;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ItemWindowReferenceTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 @Test public void inventoryGridCompareEquipUnequipAndRestartUseActualInput()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=field(v,"state");ItemWindow w=field(v,"itemWindow");String id=state.rpg().equipment().get("무기");assertNotNull(id);int dam=state.rpg().finalStats().dam,weaponDam=state.rpg().equippedDefinition("무기").statModifiers.getOrDefault("DAM",0);
  tap(v,742,28);int index=Arrays.asList(ItemWindow.SLOTS).indexOf("무기");RectF gear=ItemWindow.slot(index);equipmentTap(v,gear.centerX(),gear.centerY());RectF footer=ItemWindow.pairedUnequip();equipmentTap(v,footer.centerX(),footer.centerY());assertNull(state.rpg().equipment().get("무기"));assertEquals(dam-weaponDam,state.rpg().finalStats().dam);v.pause();F5mSaveStore.install(c);
  GameView next=new GameView(c);next.layout(0,0,960,540);RuntimeState restored=field(next,"state");assertNull(restored.rpg().equipment().get("무기"));tap(next,742,28);ItemWindow nw=field(next,"itemWindow");List<RpgInventoryPresentation.ItemRow> rows=nw.rows(restored.rpg());int selected=-1;for(int i=0;i<rows.size();i++)if(rows.get(i).itemId.equals(id))selected=i;assertTrue(selected>=0);for(int i=0;i<selected/50;i++)inventoryTap(next,894,480);RectF cell=ItemWindow.cell(selected%50);inventoryTap(next,cell.centerX(),cell.centerY());assertTrue(nw.details);render(next,"inventory-v74-compare.png");inventoryTap(next,805,490);assertEquals(id,restored.rpg().equipment().get("무기"));next.pause();F5mSaveStore.install(c);GameView again=new GameView(c);assertEquals(restored.rpg().equipment(),((RuntimeState)field(again,"state")).rpg().equipment());
  float x=restored.player().x,y=restored.player().y;tap(next,100,180);assertEquals(x,restored.player().x,0);assertEquals(y,restored.player().y,0);inventoryTap(next,916,66);assertFalse((Boolean)field(next,"inventoryOpen"));
 }
 @Test public void equipmentShowsEverySupportedSlotAndClosesAtDrawnPosition()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=field(v,"state");ItemWindow w=field(v,"itemWindow");tap(v,742,28);assertTrue((Boolean)field(v,"equipmentOpen"));render(v,"equipment-v74-overview.png");Set<String> reached=new HashSet<>();for(int i=0;i<ItemWindow.SLOTS.length;i++){RectF r=ItemWindow.slot(i);equipmentTap(v,r.centerX(),r.centerY());assertEquals(ItemWindow.SLOTS[i],w.selectedSlot);reached.add(w.selectedSlot);}assertTrue(reached.contains("갑옷"));assertTrue(reached.containsAll(state.rpg().equipment().keySet()));
  RectF b=ItemWindow.slot(Arrays.asList(ItemWindow.SLOTS).indexOf("무기"));equipmentTap(v,b.centerX(),b.centerY());RectF footer=ItemWindow.pairedUnequip();equipmentTap(v,footer.centerX(),footer.centerY());assertNull(state.rpg().equipment().get("무기"));render(v,"equipment-v74-unequipped.png");equipmentTap(v,ItemWindow.equipmentClose().centerX(),ItemWindow.equipmentClose().centerY());assertFalse((Boolean)field(v,"equipmentOpen"));tap(v,742,28);equipmentTap(v,ItemWindow.equipmentStats().centerX(),ItemWindow.equipmentStats().centerY());assertFalse((Boolean)field(v,"equipmentOpen"));assertTrue((Boolean)field(v,"statsOpen"));
 }
 @Test public void filtersAreReadOnlyAndAllOwnedItemsRemainReachable()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=field(v,"state");ItemWindow w=field(v,"itemWindow");Map<String,Integer> owned=new LinkedHashMap<>(s.rpg().inventory());Map<String,String> eq=new LinkedHashMap<>(s.rpg().equipment());tap(v,742,28);
  Set<String> classified=new HashSet<>();for(int i=1;i<ItemWindow.FILTERS.length;i++){RectF tab=ItemWindow.filterTab(i);inventoryTap(v,tab.centerX(),tab.centerY());assertEquals(i,w.filter);for(RpgInventoryPresentation.ItemRow row:w.rows(s.rpg())){assertEquals(i,ItemWindow.category(row,s.rpg()));assertTrue("disjoint categories",classified.add(row.itemId));}}RectF all=ItemWindow.filterTab(0);inventoryTap(v,all.centerX(),all.centerY());assertEquals(w.rows(s.rpg()).size(),classified.size());
  Set<String> seen=new HashSet<>();for(int i=0;i<w.rows(s.rpg()).size();i++){w.page=i/50;RectF b=ItemWindow.cell(i%50);inventoryTap(v,b.centerX(),b.centerY());seen.add(((RpgInteractionController)field(v,"rpgInteraction")).selectedInventoryItemId());inventoryTap(v,914,250);}for(String id:owned.keySet()){boolean reachable=seen.contains(id)||eq.containsValue(id);for(String selected:seen)if(InventoryGroupsV199.equivalent(s.rpg(),id,selected))reachable=true;assertTrue("owned item remains represented: "+id,reachable);}long rawQuantity=0,displayQuantity=eq.size();for(int quantity:owned.values())rawQuantity+=quantity;for(RpgInventoryPresentation.ItemRow row:w.rows(s.rpg()))displayQuantity+=row.quantity;assertEquals(rawQuantity,displayQuantity);assertEquals(w.rows(s.rpg()).size(),seen.size());assertEquals(owned,s.rpg().inventory());assertEquals(eq,s.rpg().equipment());
 }
 static void inventoryTap(GameView v,float x,float y){tap(v,InventoryPairV262.INV_X+x*InventoryPairV262.INV_SCALE,InventoryPairV262.INV_Y+y*InventoryPairV262.INV_SCALE);}
 static void equipmentTap(GameView v,float x,float y){tap(v,InventoryPairV262.EQUIP_X+x*InventoryPairV262.EQUIP_SCALE,InventoryPairV262.EQUIP_Y+y*InventoryPairV262.EQUIP_SCALE);}
 static void tap(GameView v,float x,float y){MotionEvent e=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,x,y,0);v.onTouchEvent(e);e.recycle();}
 @SuppressWarnings("unchecked") static <T>T field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return(T)f.get(o);}
 static void render(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File dir=new File("build/reports/device-review");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name))){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
}
