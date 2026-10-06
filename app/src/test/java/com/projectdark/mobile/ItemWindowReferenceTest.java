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
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=field(v,"state");ItemWindow w=field(v,"itemWindow");tap(v,608,28);render(v,"inventory-v74-grid.png");
  List<RpgInventoryPresentation.ItemRow> rows=w.rows(state.rpg());int selected=-1;for(int i=0;i<rows.size();i++)if(rows.get(i).itemId.equals(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID))selected=i;assertTrue(selected>=0);
  RectF b=ItemWindow.cell(selected);tap(v,b.centerX(),b.centerY());assertTrue(w.details);render(v,"inventory-v74-compare.png");int dam=state.rpg().finalStats().dam;tap(v,805,490);assertNull(state.rpg().equipment().get("무기"));assertEquals(dam-3,state.rpg().finalStats().dam);
  GameView restarted=new GameView(c);RuntimeState restored=field(restarted,"state");assertNull(restored.rpg().equipment().get("무기"));
  v=restarted;state=restored;v.layout(0,0,960,540);tap(v,608,28);tap(v,b.centerX(),b.centerY());tap(v,805,490);assertEquals(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID,state.rpg().equipment().get("무기"));v.pause();GameView again=new GameView(c);assertEquals(state.rpg().equipment(),((RuntimeState)field(again,"state")).rpg().equipment());
  float x=state.player().x,y=state.player().y;tap(v,100,180);assertEquals(x,state.player().x,0);assertEquals(y,state.player().y,0);tap(v,914,60);assertFalse((Boolean)field(v,"inventoryOpen"));
 }
 @Test public void equipmentShowsEverySupportedSlotAndClosesAtDrawnPosition()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=field(v,"state");ItemWindow w=field(v,"itemWindow");tap(v,716,28);assertTrue((Boolean)field(v,"equipmentOpen"));render(v,"equipment-v74-overview.png");
  Set<String> reached=new HashSet<>();for(int i=0;i<ItemWindow.SLOTS.length;i++){RectF r=ItemWindow.slot(i);tap(v,r.centerX(),r.centerY());assertEquals(ItemWindow.SLOTS[i],w.selectedSlot);reached.add(w.selectedSlot);}assertTrue(reached.contains("갑옷"));assertTrue(reached.containsAll(state.rpg().equipment().keySet()));
  int weapon=Arrays.asList(ItemWindow.SLOTS).indexOf("무기");RectF b=ItemWindow.slot(weapon);tap(v,b.centerX(),b.centerY());render(v,"equipment-v74-selected.png");tap(v,805,490);assertNull(state.rpg().equipment().get("무기"));render(v,"equipment-v74-unequipped.png");
  tap(v,492,61);assertFalse((Boolean)field(v,"equipmentOpen"));tap(v,716,28);tap(v,640,92);assertFalse((Boolean)field(v,"equipmentOpen"));assertTrue((Boolean)field(v,"statsOpen"));
 }
 @Test public void filtersAreReadOnlyAndAllOwnedItemsRemainReachable()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=field(v,"state");ItemWindow w=field(v,"itemWindow");Map<String,Integer> owned=new LinkedHashMap<>(s.rpg().inventory());Map<String,String> eq=new LinkedHashMap<>(s.rpg().equipment());tap(v,608,28);
  tap(v,438,93);assertEquals(1,w.filter);for(RpgInventoryPresentation.ItemRow row:w.rows(s.rpg()))assertFalse(row.equipSlot.isEmpty());tap(v,509,93);assertEquals(2,w.filter);for(RpgInventoryPresentation.ItemRow row:w.rows(s.rpg()))assertTrue(s.rpg().isConsumable(row.itemId));tap(v,366,93);assertEquals(0,w.filter);
  Set<String> seen=new HashSet<>();for(int i=0;i<w.rows(s.rpg()).size();i++){w.page=i/50;RectF b=ItemWindow.cell(i%50);tap(v,b.centerX(),b.centerY());seen.add(((RpgInteractionController)field(v,"rpgInteraction")).selectedInventoryItemId());tap(v,914,250);}assertEquals(owned.keySet(),seen);assertEquals(owned,s.rpg().inventory());assertEquals(eq,s.rpg().equipment());
 }
 static void tap(GameView v,float x,float y){MotionEvent e=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,x,y,0);v.onTouchEvent(e);e.recycle();}
 @SuppressWarnings("unchecked") static <T>T field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return(T)f.get(o);}
 static void render(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File dir=new File("build/reports/device-review");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name))){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
}
