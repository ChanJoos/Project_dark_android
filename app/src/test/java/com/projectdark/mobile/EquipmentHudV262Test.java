package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import java.util.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class EquipmentHudV262Test {
 final HudItemsV254Test f=new HudItemsV254Test();
 @Before public void setup(){f.setup();}
 @Test public void combatButtonsSitBesideSkillsAndDispatchTouchesAtBothWidths()throws Exception{
  for(int[] size:new int[][]{{960,540},{2340,1080}}){GameView v=f.view(size[0],size[1]);float scale=size[1]/540f,d=size[0]/scale-960;
   RectF last=f.h.slot(v,5);float x=last.right+45,y=f.h.<Float>field(v,"ATK_Y"),radius=f.h.<Float>field(v,"ATK_R");
   float ay=f.h.<Float>field(v,"AUTO_Y"),ar=f.h.<Float>field(v,"AUTO_R");
   assertEquals(31*1.2f,radius,.001f);assertTrue(x+radius<=960+d-7.99f);assertTrue(y+radius<540);
   assertTrue("both buttons below their previous positions",y>418&&ay>391);assertTrue("auto above attack with separate touch circles",y-ay>radius+4+ar+3);
   if(d>100){assertEquals("wide layout retains approved skill anchor",659+d*.5f,f.h.slot(v,0).left,.001f);assertTrue(x<854+d);}
   for(int i=0;i<12;i++){RectF r=f.h.slot(v,i);
    for(float[] circle:new float[][]{{x,y,radius+4},{x,ay,ar+3}}){float nx=Math.max(r.left,Math.min(circle[0],r.right)),ny=Math.max(r.top,Math.min(circle[1],r.bottom));assertTrue("button touch circle does not cover slot "+i,Math.hypot(circle[0]-nx,circle[1]-ny)>circle[2]);}
    tap(v,r.centerX()*scale,r.centerY()*scale);assertEquals("SLOT"+i,f.h.field(v,"pressedControl"));}
   assertTrue(GameView.blocksWorldTapForHud(x,y,false,d));assertTrue(GameView.blocksWorldTapForHud(x,ay,false,d));
   tap(v,x*scale,y*scale);assertEquals("ATK",f.h.field(v,"pressedControl"));
   boolean enabled=f.h.field(v,"autoAttackEnabled");tap(v,x*scale,ay*scale);assertEquals("AUTO",f.h.field(v,"pressedControl"));assertEquals(!enabled,f.h.field(v,"autoAttackEnabled"));
   tap(v,x*scale,ay*scale);assertEquals(enabled,f.h.field(v,"autoAttackEnabled"));
   render(v,"v265-hud-"+size[0],size[0],size[1]);
  }
 }
 @Test public void bagButtonOpensLeftEquipmentRightBagAndBothCloseThroughActualInput()throws Exception{
  for(int[] size:new int[][]{{960,540},{2340,1080}}){GameView v=f.view(size[0],size[1]);float scale=size[1]/540f,d=size[0]/scale-960;
   tap(v,(742+d)*scale,28*scale);assertEquals(true,f.h.field(v,"inventoryOpen"));assertEquals(true,f.h.field(v,"equipmentOpen"));
   render(v,"pair-"+size[0],size[0],size[1]);
   float x=InventoryPairV262.INV_X+916*InventoryPairV262.INV_SCALE+d/2,y=InventoryPairV262.INV_Y+66*InventoryPairV262.INV_SCALE;tap(v,x*scale,y*scale);assertEquals(false,f.h.field(v,"inventoryOpen"));assertEquals(false,f.h.field(v,"equipmentOpen"));
  }
 }
 @Test public void wearingRemovesOnlyEquippedCopiesAndFooterReturnsThemWithoutChangingOwnership()throws Exception{
  GameView v=f.view(960,540);RuntimeState s=f.h.field(v,"state");String id=f.necklaces(s).get(0);ItemWindow w=f.h.field(v,"itemWindow");int original=s.rpg().inventory().get(id);Map<String,Integer> owned=new LinkedHashMap<>(s.rpg().inventory());
  assertEquals(original,quantity(w,s,id));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,s.rpg().equipToSlot(id,"목걸이"));assertEquals(original-1,quantity(w,s,id));assertEquals(owned,s.rpg().inventory());
  tap(v,742,28);RectF slot=ItemWindow.slot(2);tap(v,InventoryPairV262.EQUIP_X+slot.centerX()*.9f,InventoryPairV262.EQUIP_Y+slot.centerY()*.9f);assertEquals("목걸이",w.selectedSlot);
  RectF footer=ItemWindow.pairedUnequip();tap(v,InventoryPairV262.EQUIP_X+footer.centerX()*.9f,InventoryPairV262.EQUIP_Y+footer.centerY()*.9f);assertNull(s.rpg().equipment().get("목걸이"));assertEquals(original,quantity(w,s,id));assertEquals(owned,s.rpg().inventory());
  assertEquals(RpgProgressionState.AutoLootResult.LOOTED,s.rpg().autoLootResolvedItem(id,1));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,s.rpg().equipToSlot(id,"목걸이"));assertEquals(original,quantity(w,s,id));
  v.pause();F5mSaveStore.install(f.h.c);GameView next=f.view(960,540);RuntimeState copy=f.h.field(next,"state");assertEquals(id,copy.rpg().equipment().get("목걸이"));assertEquals(original,quantity(f.h.field(next,"itemWindow"),copy,id));
 }
 @Test public void necklaceSwapUsesTappedSlotIncludingDuplicateBindingsAndSurvivesRestart()throws Exception{
  GameView v=f.view(960,540);RuntimeState s=f.h.field(v,"state");List<String> ids=f.necklaces(s);String a=ids.get(0),b=ids.get(1);QuickItemBarV254 q=f.h.field(v,"quickItems");assertEquals(RpgProgressionState.EquipResult.EQUIPPED,s.rpg().equipToSlot(a,"목걸이"));assertTrue(q.assign(0,b,s.rpg()));assertTrue(q.assign(3,b,s.rpg()));Map<String,Integer> owned=new LinkedHashMap<>(s.rpg().inventory());
  f.gesture(v,MobileHudLayout.quickItem(3,0),1,30,android.view.MotionEvent.ACTION_UP);assertEquals(b,s.rpg().equipment().get("목걸이"));assertEquals(a,q.slot(3));assertEquals("other duplicate binding unchanged",b,q.slot(0));
  f.gesture(v,MobileHudLayout.quickItem(3,0),1,30,android.view.MotionEvent.ACTION_UP);assertEquals(a,s.rpg().equipment().get("목걸이"));assertEquals(b,q.slot(3));assertEquals(owned,s.rpg().inventory());
  v.pause();F5mSaveStore.install(f.h.c);GameView next=f.view(960,540);assertEquals(b,((QuickItemBarV254)f.h.field(next,"quickItems")).slot(3));assertEquals(a,((RuntimeState)f.h.field(next,"state")).rpg().equipment().get("목걸이"));
 }
 static int quantity(ItemWindow w,RuntimeState s,String id){for(RpgInventoryPresentation.ItemRow row:w.rows(s.rpg()))if(row.itemId.equals(id))return row.quantity;return 0;}
 static void tap(GameView v,float x,float y){MartialHudAutoRegressionTest.tap(v,x,y);}
 static void render(GameView v,String name,int width,int height)throws Exception{Bitmap b=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File p=new File("build/reports/horror-v179/v262/"+name+".png");p.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(p)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();}
}
