package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ClassicPolishV132Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 GameView view(int width,int height)throws Exception{GameView v=new GameView(c);v.layout(0,0,width,height);RpgProgressionState r=rpg(v);r.enableEquipmentSandbox(true);assertTrue(r.restoreOwnedItems(r.inventory(),Collections.emptyMap()));return v;}
 RpgProgressionState rpg(GameView v)throws Exception{return ((RuntimeState)ItemWindowReferenceTest.field(v,"state")).rpg();}
 ItemWindow window(GameView v)throws Exception{return ItemWindowReferenceTest.field(v,"itemWindow");}
 void tap(GameView v,float x,float y)throws Exception{float scale=v.getHeight()/540f,offset=GameView.rightHudOffsetForView(v.getWidth(),v.getHeight())/2;ItemWindowReferenceTest.tap(v,(x+offset)*scale,y*scale);}
 void open(GameView v)throws Exception{float scale=v.getHeight()/540f,right=GameView.rightHudOffsetForView(v.getWidth(),v.getHeight());ItemWindowReferenceTest.tap(v,(608+right)*scale,28*scale);}
 void select(GameView v,String id)throws Exception{ItemWindow w=window(v);List<RpgInventoryPresentation.ItemRow> rows=w.rows(rpg(v));int n=0;while(!rows.get(n).itemId.equals(id))n++;w.page=n/50;RectF b=ItemWindow.cell(n%50);tap(v,b.centerX(),b.centerY());assertEquals(id,w.detailItemId);assertNull(w.pickedTargetSlot);}
 @Test public void oneActionFillsBothVacantRingAndGloveSlotsWithoutChangingOwnership()throws Exception{
  GameView v=view(960,540);RpgProgressionState r=rpg(v);Map<String,Integer> owned=new LinkedHashMap<>(r.inventory());int hp=r.finalStats().maxHp;open(v);
  for(String id:new String[]{"IT_RING_THREELINEGOLD","IT_GLOVE_LEATHER"}){select(v,id);RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);assertEquals(d.equipSlot,window(v).targetSlot(r,d));assertEquals("장착",window(v).actionLabel(r,d,false));save(v,"auto-empty-"+d.equipSlot);tap(v,805,490);assertEquals(id,r.equipment().get(d.equipSlot));tap(v,805,490);assertEquals(id,r.equipment().get(RpgProgressionState.secondSlot(d.equipSlot)));tap(v,805,490);assertEquals(2,r.equippedCount(id));tap(v,914,250);}
  assertEquals(owned,r.inventory());assertEquals(hp+600,r.finalStats().maxHp);
 }
 @Test public void fullPairsRequireCardSelectionAndReplaceOnlyThatCardThenRestore()throws Exception{
  GameView v=view(960,540);RpgProgressionState r=rpg(v);r.equipToSlot("IT_RING_REDJADE","반지");r.equipToSlot("IT_RING_THREELINEGOLD",RpgProgressionState.RIGHT_RING_SLOT);Map<String,Integer> owned=new LinkedHashMap<>(r.inventory());open(v);select(v,"IT_RING_REDJADE");Map<String,String> before=new LinkedHashMap<>(r.equipment());tap(v,805,490);assertEquals(before,r.equipment());assertEquals("장비 선택",window(v).actionLabel(r,r.itemDefinitions().get("IT_RING_REDJADE"),true));save(v,"choose-replacement");
  RectF b=ItemWindow.compareCell(1,2);tap(v,b.centerX(),b.centerY());assertEquals(RpgProgressionState.RIGHT_RING_SLOT,window(v).pickedTargetSlot);assertEquals("교체",window(v).actionLabel(r,r.itemDefinitions().get("IT_RING_REDJADE"),true));save(v,"replacement-selected");tap(v,805,490);assertEquals("IT_RING_REDJADE",r.equipment().get("반지"));assertEquals("IT_RING_REDJADE",r.equipment().get(RpgProgressionState.RIGHT_RING_SLOT));assertEquals(owned,r.inventory());v.pause();GameView restored=new GameView(c);assertEquals(r.equipment(),rpg(restored).equipment());assertEquals(r.finalStats().maxHp,rpg(restored).finalStats().maxHp);
 }
 @Test public void aSingleOwnedCopyCannotFillTwoSlotsAndCardRemovalKeepsOtherSlot()throws Exception{
  GameView v=view(960,540);RpgProgressionState r=rpg(v);Map<String,Integer> owned=new LinkedHashMap<>(r.inventory());owned.put("IT_RING_REDJADE",1);assertTrue(r.restoreOwnedItems(owned,Collections.emptyMap()));open(v);select(v,"IT_RING_REDJADE");tap(v,805,490);tap(v,805,490);assertEquals(1,r.equippedCount("IT_RING_REDJADE"));assertNull(r.equipment().get(RpgProgressionState.RIGHT_RING_SLOT));
  r.equipToSlot("IT_RING_THREELINEGOLD",RpgProgressionState.RIGHT_RING_SLOT);RectF b=ItemWindow.compareCell(0,2);tap(v,b.centerX(),b.centerY());assertEquals("해제",window(v).actionLabel(r,r.itemDefinitions().get("IT_RING_REDJADE"),true));tap(v,805,490);assertNull(r.equipment().get("반지"));assertEquals("IT_RING_THREELINEGOLD",r.equipment().get(RpgProgressionState.RIGHT_RING_SLOT));
 }
 @Test public void wideCompactCardTargetsMatchDrawnPositionsAndSelectionResets()throws Exception{
  GameView v=view(2340,1080);RpgProgressionState r=rpg(v);r.equipToSlot("IT_GLOVE_LEATHER","장갑");r.equipToSlot("IT_GLOVE_LEATHER",RpgProgressionState.RIGHT_GLOVE_SLOT);open(v);select(v,"IT_GLOVE_LEATHER");RectF b=ItemWindow.compareCell(1,2);tap(v,b.centerX(),b.centerY());save(v,"wide-paired-card");tap(v,805,490);assertNull(r.equipment().get(RpgProgressionState.RIGHT_GLOVE_SLOT));assertEquals("IT_GLOVE_LEATHER",r.equipment().get("장갑"));tap(v,914,250);select(v,"IT_RING_THREELINEGOLD");assertEquals("반지",window(v).targetSlot(r,r.itemDefinitions().get("IT_RING_THREELINEGOLD")));save(v,"wide-auto-ring");
 }
 void save(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File f=new File("build/reports/classic-polish-v132/"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream o=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,o));}b.recycle();}
}
