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
public class EquipmentWindowV178Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String n:new String[]{"project_dark_f5m_v1","project_dark_skill_test_v1"})c.getSharedPreferences(n,0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 @Test public void everyCatalogEarringAndBothRingSlotsRenderAtEightyPercentOfV177(){
  RpgProgressionState r=new RpgProgressionState();SourceItemIconRegistry icons=new SourceItemIconRegistry(c);int earrings=0,rings=0;
  for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values()){
   if(!"귀걸이".equals(d.equipSlot)&&!"반지".equals(d.equipSlot))continue;Bitmap src=icons.get(d.itemId);if(src==null)continue;
   float previous="귀걸이".equals(d.equipSlot)?.85f:1f;float now=previous*.8f;RectF fit=new RectF(10+30*(1-now),10+30*(1-now),70-30*(1-now),70-30*(1-now));
   Bitmap expected=Bitmap.createBitmap(80,80,Bitmap.Config.ARGB_8888);SourceItemIconRegistry.drawEquipment(new Canvas(expected),src,fit,new Paint(),false);
   for(String slot:"반지".equals(d.equipSlot)?new String[]{"반지",RpgProgressionState.RIGHT_RING_SLOT}:new String[]{"귀걸이"}){
    Bitmap actual=Bitmap.createBitmap(80,80,Bitmap.Config.ARGB_8888);SourceItemIconRegistry.drawEquipmentForSlot(new Canvas(actual),src,new RectF(10,10,70,70),new Paint(),slot,d.itemId);assertPixels(d.itemId+slot,expected,actual);
   }
   if("귀걸이".equals(d.equipSlot))earrings++;else rings++;
  }
  assertTrue(earrings>1);assertTrue(rings>2);System.out.println("ALL_VARIANT_ACCESSORIES earrings="+earrings+" rings="+rings);
 }
 @Test public void everyGloveMatchesTheRightSlotAndEveryShoeReversesTheV177Projection(){
  RpgProgressionState r=new RpgProgressionState();SourceItemIconRegistry icons=new SourceItemIconRegistry(c);int gloves=0,shoes=0;
  for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values()){
   if(!"장갑".equals(d.equipSlot)&&!"신발".equals(d.equipSlot))continue;Bitmap src=icons.get(d.itemId);if(src==null)continue;RectF target=new RectF(10,10,70,70);
   Bitmap expected=Bitmap.createBitmap(80,80,Bitmap.Config.ARGB_8888);SourceItemIconRegistry.drawEquipment(new Canvas(expected),src,target,new Paint(),false);
   for(String slot:"장갑".equals(d.equipSlot)?new String[]{"장갑",RpgProgressionState.RIGHT_GLOVE_SLOT}:new String[]{"신발"}){
    Bitmap actual=Bitmap.createBitmap(80,80,Bitmap.Config.ARGB_8888);SourceItemIconRegistry.drawEquipmentForSlot(new Canvas(actual),src,target,new Paint(),slot,d.itemId);assertPixels(d.itemId+slot,expected,actual);
   }
   if("장갑".equals(d.equipSlot))gloves++;else shoes++;
  }
  assertTrue(gloves>2);assertTrue(shoes>=3);System.out.println("ALL_VARIANT_DIRECTIONS gloves="+gloves+" shoes="+shoes);
 }
 @Test public void actualGameViewSwapsVariantsThroughTheSameEquipmentSlots()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(v,"state");RpgProgressionState r=state.rpg();r.enableEquipmentSandbox(true);ItemWindowReferenceTest.tap(v,830,28);
  String glove="IT_SOURCE_170_KHANGOLDGLOVER_A65F8611";r.equipToSlot(glove,"장갑");r.equipToSlot(glove,RpgProgressionState.RIGHT_GLOVE_SLOT);r.equipToSlot("IT_RING_THREELINEGOLD","반지");r.equipToSlot("IT_RING_THREELINEGOLD",RpgProgressionState.RIGHT_RING_SLOT);r.equipToSlot("IT_TEST_SHOES_ML229","신발");
  for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values())if("귀걸이".equals(d.equipSlot)&&!"IT_EARRING_DOUBLE_SILVER".equals(d.itemId)&&new SourceItemIconRegistry(c).get(d.itemId)!=null){r.equipToSlot(d.itemId,"귀걸이");break;}
  render(v,"variants.png");r.equipToSlot("IT_EARRING_DOUBLE_SILVER","귀걸이");r.equipToSlot("IT_GLOVE_LEATHER","장갑");r.equipToSlot("IT_GLOVE_LEATHER",RpgProgressionState.RIGHT_GLOVE_SLOT);r.equipToSlot("IT_RING_REDJADE","반지");r.equipToSlot("IT_RING_REDJADE",RpgProgressionState.RIGHT_RING_SLOT);r.equipToSlot("IT_SHOES","신발");render(v,"starter.png");
 }
 void assertPixels(String id,Bitmap expected,Bitmap actual){int[] e=new int[6400],a=new int[6400];expected.getPixels(e,0,80,0,0,80,80);actual.getPixels(a,0,80,0,0,80,80);assertArrayEquals(id,e,a);}
 void render(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File d=new File("build/reports/equipment-v178");d.mkdirs();try(FileOutputStream o=new FileOutputStream(new File(d,name))){b.compress(Bitmap.CompressFormat.PNG,100,o);}b.recycle();}
}
