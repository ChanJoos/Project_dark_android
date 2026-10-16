package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class EquipmentWindowV177Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String n:new String[]{"project_dark_f5m_v1","project_dark_skill_test_v1"})c.getSharedPreferences(n,0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 @Test public void slotDirectionsFollowCurrentUserPolicyWithV178GloveAndShoeOverride(){
  Bitmap src=Bitmap.createBitmap(20,20,Bitmap.Config.ARGB_8888);Canvas sc=new Canvas(src);Paint p=new Paint();p.setColor(Color.RED);sc.drawRect(2,2,10,18,p);p.setColor(Color.BLUE);sc.drawRect(10,2,18,18,p);
  for(String slot:ItemWindow.SLOTS){
   Bitmap out=Bitmap.createBitmap(80,80,Bitmap.Config.ARGB_8888);SourceItemIconRegistry.drawEquipmentForSlot(new Canvas(out),src,new RectF(10,10,70,70),p,slot,"fixture");
   // V178 supersedes the V177 left-glove and shoe direction after phone feedback.
   boolean expected="모자".equals(slot)||"갑옷".equals(slot);
   assertEquals(slot,expected?Color.BLUE:Color.RED,out.getPixel(25,40));assertEquals(slot,expected?Color.RED:Color.BLUE,out.getPixel(55,40));
  }
 }
 @Test public void realArtworkHasReducedHatEarringShirtAndCenteredNecklaceArmorWithoutClipping(){
  SourceItemIconRegistry icons=new SourceItemIconRegistry(c);String[][] cases={{"모자",RpgProgressionState.STARTER_HAT_ITEM_ID},{"귀걸이","IT_EARRING_DOUBLE_SILVER"},{"갑옷",RpgProgressionState.STARTER_SHIRT_ITEM_ID},{"목걸이","IT_NECK_FIRE_PEARL"},{"갑옷","IT_SHOP_ARMOR_JIPON"}};
  for(String[] entry:cases){Bitmap src=icons.get(entry[1]);assertNotNull(entry[1],src);Bitmap out=Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);SourceItemIconRegistry.drawEquipmentForSlot(new Canvas(out),src,new RectF(20,20,80,80),new Paint(),entry[0],entry[1]);Rect bounds=SourceItemIconRegistry.visibleEquipmentBounds(out);
   assertTrue(entry[1],bounds.left>=20&&bounds.top>=20&&bounds.right<=80&&bounds.bottom<=80);
   if(entry[1].equals(RpgProgressionState.STARTER_HAT_ITEM_ID)||entry[1].equals("IT_EARRING_DOUBLE_SILVER")||entry[1].equals(RpgProgressionState.STARTER_SHIRT_ITEM_ID))assertTrue(entry[1]+bounds,Math.max(bounds.width(),bounds.height())<=52);
   if("목걸이".equals(entry[0])||"갑옷".equals(entry[0])){PointF center=SourceItemIconRegistry.equipmentOpticalCenter(out);System.out.println("OPTICAL_CENTER "+entry[1]+" "+center);assertEquals(entry[1],50,center.x,.8);assertEquals(entry[1],50,center.y,.8);}
  }
 }
 @Test public void actualEquipmentFrameShowsStarterAndBothGlovesNecklaceAndEarring()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(v,"state");RpgProgressionState r=state.rpg();ItemWindowReferenceTest.tap(v,830,28);render(v,"starter.png");
  r.enableEquipmentSandbox(true);r.equipToSlot("IT_EARRING_DOUBLE_SILVER","귀걸이");r.equipToSlot("IT_GLOVE_LEATHER","장갑");r.equipToSlot("IT_GLOVE_LEATHER",RpgProgressionState.RIGHT_GLOVE_SLOT);r.equipToSlot("IT_NECK_FIRE_PEARL","목걸이");r.equipToSlot("IT_RING_REDJADE","반지");r.equipToSlot("IT_RING_REDJADE",RpgProgressionState.RIGHT_RING_SLOT);
  for(String slot:new String[]{"벨트","각반"})for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values())if(slot.equals(d.equipSlot)){r.equipToSlot(d.itemId,slot);break;}
  render(v,"filled.png");r.equipToSlot("IT_SHOP_ARMOR_JIPON","갑옷");render(v,"armor.png");
 }
 void render(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File d=new File("build/reports/equipment-v177");d.mkdirs();try(FileOutputStream o=new FileOutputStream(new File(d,name))){b.compress(Bitmap.CompressFormat.PNG,100,o);}b.recycle();}
}
