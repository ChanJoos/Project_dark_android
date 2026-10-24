package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class TempleEquipmentV216Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String n:new String[]{"project_dark_f5m_v1","project_dark_skill_test_v1"})c.getSharedPreferences(n,0).edit().clear().commit();F5mSaveStore.install(c);}
 void capture(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File f=new File("build/reports/horror-v179/v216/"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();}
 @Test public void displayed21Comma25IsReachableByGroundTapAndHeldJoystick()throws Exception{
  for(boolean joystick:new boolean[]{false,true}){
   setup();GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(false);TownInteriorDef hub=TownInteriorDef.forMap("milles_interior_temple_hub");TownInteriorTest.enter(v,hub);RuntimeState s=TownInteriorTest.field(v,"state");WorldRuntimeAdapter a=TownInteriorTest.field(v,"reagentShopAdapter");
   TempleRoutes.Gate door=TempleRoutes.forRoom(hub).stream().filter(g->!g.exit).findFirst().get();assertEquals(TownInteriorDef.x(21,25),door.x,0);assertEquals(TownInteriorDef.y(21,25),door.y,0);assertFalse(hub.blocked(21,25));
   s.player().x=TownInteriorDef.x(22,25);s.player().y=TownInteriorDef.y(22,25);a.snapCameraToPlayer();capture(v,joystick?"hub-before-held-step":"hub-no-npc");
   if(joystick){MotionEvent down=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,110,374,0);v.onTouchEvent(down);down.recycle();MotionEvent move=MotionEvent.obtain(0,16,MotionEvent.ACTION_MOVE,70,354,0);v.onTouchEvent(move);move.recycle();}else{WorldCameraTransform.Point p=a.worldToScreen(door.x,door.y);TownInteriorTest.tap(v,p.x,p.y);}
   TownInteriorTest.tick(v,80);assertEquals("milles_interior_temple_seo",s.currentMapId());capture(v,joystick?"seo-after-held-step":"seo-after-ground-tap");
  }
 }
 @Test public void hubNpcHasNeitherCollisionNorTapInteraction()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);TownInteriorDef hub=TownInteriorDef.forMap("milles_interior_temple_hub");TownInteriorTest.enter(v,hub);assertFalse(hub.hasNpc());assertFalse(hub.blocked(hub.npcU,hub.npcV));assertTrue(TownInteriorDef.forMap("milles_interior_temple_lobby").hasNpc());assertTrue(TownInteriorDef.forMap("milles_interior_temple_seo").hasNpc());WorldRuntimeAdapter a=TownInteriorTest.field(v,"reagentShopAdapter");WorldCameraTransform.Point p=a.worldToScreen(hub.npcX(),hub.npcY());TownInteriorTest.tap(v,p.x,p.y-24);assertFalse((Boolean)TownInteriorTest.field(v,"merchantApproach"));assertFalse(((TempleGrowthWindow)TownInteriorTest.field(v,"templeWindow")).isOpen());
 }
 @Test public void categoryTabsSeparateShieldEarringAndEveryGloveBraceletAndKeepRapidReachable()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=TownInteriorTest.field(v,"state");RpgProgressionState r=s.rpg();r.enableEquipmentSandbox(true);ItemWindow w=TownInteriorTest.field(v,"itemWindow");ItemWindowReferenceTest.tap(v,742,28);int bracelets=0;
  for(RpgProgressionState.ItemDefinition d:r.canonicalItemDefinitions().values())if(d.name.contains("팔찌")){assertEquals(d.name,"장갑",d.equipSlot);assertEquals(13,ItemWindow.category(w.find(r,d.itemId),r));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(d.itemId,"장갑"));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(d.itemId,RpgProgressionState.RIGHT_GLOVE_SLOT));bracelets++;}assertTrue(bracelets>0);
  assertEquals(11,ItemWindow.category(w.find(r,"IT_SHIELD_WOOD"),r));assertEquals(12,ItemWindow.category(w.find(r,"IT_EARRING_DOUBLE_SILVER"),r));assertEquals(13,ItemWindow.category(w.find(r,"IT_GLOVE_LEATHER"),r));
  for(int i=0;i<ItemWindow.FILTERS.length;i++){RectF tab=ItemWindow.filterTab(i);ItemWindowReferenceTest.tap(v,tab.centerX(),tab.centerY());assertEquals(i,w.filter);if(i>=11)capture(v,"inventory-"+ItemWindow.FILTERS[i]);}
  for(String id:new String[]{"USER_ITEM_20261018_R05141","USER_ITEM_20261018_R05145","USER_ITEM_20261018_R05144","USER_ITEM_20261018_R05143","USER_ITEM_20261018_R05142","USER_ITEM_20261018_R01339"}){assertTrue(r.inventory().get(id)>0);assertTrue(w.rows(r).stream().anyMatch(row->row.itemId.equals(id)));assertEquals(14,ItemWindow.category(w.find(r,id),r));}
  String rapid="USER_ITEM_20261018_R05143";List<RpgInventoryPresentation.ItemRow> rows=w.rows(r);int i=0;while(!rows.get(i).itemId.equals(rapid))i++;w.page=i/50;RectF cell=ItemWindow.cell(i%50);ItemWindowReferenceTest.tap(v,cell.centerX(),cell.centerY());assertEquals(rapid,w.hitItem);capture(v,"rapid-in-other");
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(rapid,"장식"));assertTrue(CharacterVisualBinding.from(r).equipmentVisualRef().contains("mg148"));F5mSaveStore.saveRewardsActive(r);F5mSaveStore.install(c);RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);assertEquals(r.equipment(),restored.equipment());assertEquals(r.inventory(),restored.inventory());
 }
 @Test public void wingSocketIsImmediatelyRightOfShieldWithoutCaption()throws Exception{
  RectF shield=ItemWindow.slot(12),wing=ItemWindow.slot(15);assertEquals(shield.top,wing.top,0);assertEquals(11*.85f,wing.left-shield.right,.001f);GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=TownInteriorTest.field(v,"state");s.rpg().enableEquipmentSandbox(true);s.rpg().equip("USER_ITEM_20261018_R00689");s.rpg().equip("IT_SHIELD_DEATHKNIGHT");s.rpg().equipToSlot("USER_ITEM_20261018_R05143","장식");ItemWindowReferenceTest.tap(v,830,28);capture(v,"equipment-wing-right-of-shield");ItemWindowReferenceTest.tap(v,wing.centerX(),wing.centerY());ItemWindow w=TownInteriorTest.field(v,"itemWindow");assertEquals("날개",w.selectedSlot);
 }
}
