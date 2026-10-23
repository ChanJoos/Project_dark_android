package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.app.*;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.shadows.ShadowAlertDialog;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class HudItemsV254Test {
 final MobileHudV134Test h=new MobileHudV134Test();Activity activity;
 @Before public void setup(){h.setup();h.c.getSharedPreferences(QuickItemBarV254.PREFS,0).edit().clear().commit();activity=Robolectric.buildActivity(Activity.class).setup().get();}
 GameView view(int width,int height){GameView v=new GameView(activity);v.layout(0,0,width,height);return v;}
 void gesture(GameView v,RectF r,float scale,long duration,int finish){long start=100;for(int action:new int[]{MotionEvent.ACTION_DOWN,finish}){MotionEvent e=MotionEvent.obtain(start,action==MotionEvent.ACTION_DOWN?start:start+duration,action,r.centerX()*scale,r.centerY()*scale,0);v.onTouchEvent(e);e.recycle();}}
 @Test public void sixByTwoAndSixJoinedItemCellsHaveExactTouchBoundsWithoutAnyControlOverlap()throws Exception{
  for(int[] size:new int[][]{{960,540},{2340,1080}}){GameView v=view(size[0],size[1]);float scale=size[1]/540f,d=size[0]/scale-960;List<RectF> rects=new ArrayList<>();
   for(int i=0;i<12;i++){RectF r=h.slot(v,i);assertEquals(44,r.width(),0);assertEquals(44,r.height(),0);if(i>0&&i%6!=0)assertEquals(h.slot(v,i-1).top,r.top,0);if(i>=6){assertEquals(h.slot(v,i-6).left,r.left,0);assertTrue(r.top>h.slot(v,i-6).bottom);}rects.add(r);assertTrue(GameView.blocksWorldTapForHud(r.left+1,r.top+1,false,d));}
   for(int i=0;i<6;i++){RectF r=MobileHudLayout.itemBar(i,d);assertEquals(36,r.width(),0);assertEquals(r.width(),r.height(),0);assertTrue(r.bottom<MobileHudLayout.status(d).top);if(i>0)assertEquals(MobileHudLayout.itemBar(i-1,d).right,r.left,0);for(RectF prior:rects)assertFalse("no skill/item intersection",RectF.intersects(prior,r));rects.add(r);assertTrue(GameView.blocksWorldTapForHud(r.right-1,r.bottom-1,false,d));}
   RectF atk=new RectF(869+d,418,931+d,480),auto=new RectF(838+d,375,870+d,407);for(RectF r:rects){assertFalse(RectF.intersects(r,atk));assertFalse(RectF.intersects(r,auto));}
  }
 }
 List<String> necklaces(RuntimeState s){s.rpg().enableEquipmentSandbox(true);List<String> ids=new ArrayList<>();for(String alias:new String[]{"IT_NECK_WATER_PEARL","IT_NECK_EARTH_PEARL"}){String id=RpgProgressionState.canonicalItemId(alias);assertNotNull(id,s.rpg().itemDefinitions().get(id));assertEquals("목걸이",s.rpg().itemDefinitions().get(id).equipSlot);assertEquals(id,RpgProgressionState.RequirementResult.MET,s.rpg().currentRequirements(id));assertEquals(RpgProgressionState.AutoLootResult.LOOTED,s.rpg().autoLootResolvedItem(id,1));ids.add(id);}assertEquals(2,new HashSet<>(ids).size());return ids;}
 void choose(GameView v,int slot,String id)throws Exception{QuickItemBarV254 b=h.field(v,"quickItems");RuntimeState s=h.field(v,"state");AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();assertNotNull(dialog);int n=id==null?0:b.choices(s.rpg()).indexOf(id)+1;assertTrue(id==null||n>0);dialog.getListView().performItemClick(null,n,n);assertEquals(id,b.slot(slot));}
 @Test public void emptyTapRegistersOwnedNecklacesAndSingleTapSwapsActualEquipmentWithoutTogglingOffThenRestarts()throws Exception{
  GameView v=view(960,540);RuntimeState s=h.field(v,"state");List<String> ids=necklaces(s);QuickItemBarV254 b=h.field(v,"quickItems");
  for(int i=0;i<2;i++){RectF r=MobileHudLayout.quickItem(i,0);gesture(v,r,1,40,MotionEvent.ACTION_UP);choose(v,i,ids.get(i));assertFalse("registration does not equip",s.rpg().equipment().containsValue(ids.get(i)));}
  Map<String,Integer> owned=new LinkedHashMap<>(s.rpg().inventory());
  for(int i:new int[]{0,1,1}){gesture(v,MobileHudLayout.quickItem(i,0),1,40,MotionEvent.ACTION_UP);assertEquals(ids.get(i),s.rpg().equipment().get("목걸이"));assertEquals(owned,s.rpg().inventory());}
  v.pause();F5mSaveStore.install(h.c);GameView next=view(960,540);QuickItemBarV254 restored=h.field(next,"quickItems");RuntimeState saved=h.field(next,"state");assertEquals(ids.get(0),restored.slot(0));assertEquals(ids.get(1),restored.slot(1));assertEquals(ids.get(1),saved.rpg().equipment().get("목걸이"));
 }
 @Test public void longPressReassignsAndClearsCancelDoesNotUseAndUnownedOrReadonlyCannotMutate()throws Exception{
  GameView v=view(960,540);RuntimeState s=h.field(v,"state");List<String> ids=necklaces(s);QuickItemBarV254 b=h.field(v,"quickItems");assertTrue(b.assign(3,ids.get(0),s.rpg()));RectF r=MobileHudLayout.quickItem(3,0);
  gesture(v,r,1,650,MotionEvent.ACTION_UP);choose(v,3,ids.get(1));assertFalse(s.rpg().equipment().containsValue(ids.get(1)));
  gesture(v,r,1,20,MotionEvent.ACTION_CANCEL);assertFalse(s.rpg().equipment().containsValue(ids.get(1)));
  gesture(v,r,1,650,MotionEvent.ACTION_UP);choose(v,3,null);assertFalse(b.assign(3,"UNKNOWN",s.rpg()));
  assertTrue(b.assign(3,ids.get(0),s.rpg()));h.c.getSharedPreferences("project_dark_f5m_v1",0).edit().putInt("save_schema",999).commit();F5mSaveStore.install(h.c);assertFalse(F5mSaveStore.writable());assertFalse(b.assign(3,ids.get(1),s.rpg()));gesture(v,r,1,20,MotionEvent.ACTION_UP);assertEquals(ids.get(0),b.slot(3));assertFalse(s.rpg().equipment().containsValue(ids.get(0)));
 }
 @Test public void fourthSlotConsumableUsesOnceRestoresOnNoEffectAndRetainsBindingAfterDepletion()throws Exception{
  GameView v=view(2340,1080);RuntimeState s=h.field(v,"state");QuickItemBarV254 b=h.field(v,"quickItems");String id="IT_B_MP_POTION";Map<String,Integer> owned=new LinkedHashMap<>(s.rpg().inventory());owned.put(id,1);assertTrue(s.rpg().restoreOwnedItems(owned,s.rpg().equipment()));int before=1;assertTrue(b.assign(3,id,s.rpg()));s.player().mp=s.player().maxMp-1;
  RectF r=MobileHudLayout.quickItem(3,210);gesture(v,r,2,30,MotionEvent.ACTION_UP);assertEquals(s.player().maxMp,s.player().mp);assertEquals(before-1,s.rpg().inventory().getOrDefault(id,0).intValue());gesture(v,r,2,30,MotionEvent.ACTION_UP);assertEquals(before-1,s.rpg().inventory().getOrDefault(id,0).intValue());assertEquals(id,b.slot(3));v.pause();F5mSaveStore.install(h.c);assertEquals(id,((QuickItemBarV254)h.field(view(960,540),"quickItems")).slot(3));
 }
 @Test public void actualTownForestAndInteriorRenderBothWidthsWithAllTwelveSkillsAndFourItems()throws Exception{
  for(int[] size:new int[][]{{960,540},{2340,1080}}){h.reset();h.c.getSharedPreferences(QuickItemBarV254.PREFS,0).edit().clear().commit();GameView v=view(size[0],size[1]);v.setSkillTestMode(true);SkillBook book=h.field(v,"skillBook");int n=0;for(SkillBook.Entry e:book.entries())if(book.previewable(e.id)&&n<12)book.assign(n++,e.id);RuntimeState s=h.field(v,"state");List<String> ids=necklaces(s);QuickItemBarV254 bar=h.field(v,"quickItems");assertTrue(bar.assign(0,ids.get(0),s.rpg()));assertTrue(bar.assign(1,ids.get(1),s.rpg()));s.rpg().grantB5SmallHpPotion(2);s.rpg().autoLootResolvedItem("IT_B_MP_POTION",2);assertTrue(bar.assign(2,s.rpg().quickHpPotionId(),s.rpg()));assertTrue(bar.assign(3,"IT_B_MP_POTION",s.rpg()));
   render(v,"town-"+size[0],size[0],size[1]);Method enter=GameView.class.getDeclaredMethod("enterPoteField");enter.setAccessible(true);enter.invoke(v);render(v,"forest-"+size[0],size[0],size[1]);Method shop=GameView.class.getDeclaredMethod("enterReagentShop");shop.setAccessible(true);shop.invoke(v);render(v,"interior-"+size[0],size[0],size[1]);
  }
 }
 void render(GameView v,String name,int width,int height)throws Exception{Bitmap image=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);v.draw(new Canvas(image));File f=new File("build/reports/horror-v179/v254/"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));}image.recycle();}
}
