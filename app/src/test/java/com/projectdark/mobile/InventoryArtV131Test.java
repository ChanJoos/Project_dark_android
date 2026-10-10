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
public class InventoryArtV131Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 @Test public void labelledInventoryIconsReplaceHeldPreviewsWithoutChangingActorOrAttack()throws Exception{
  RpgProgressionState r=new RpgProgressionState();SourceItemIconRegistry icons=new SourceItemIconRegistry(c);int dedicated=0,fallback=0,weapons=0;
  for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values()){
   if(!"무기".equals(d.equipSlot))continue;weapons++;
   assertTrue(d.itemId,d.statModifiers.containsKey("MinATK"));
   org.json.JSONObject row=icons.receipt(d.itemId);
   if(row.optBoolean("inventoryArtwork")){dedicated++;assertEquals(d.appearanceId,row.getString("appearanceId"));assertEquals(32,icons.get(d).getWidth());assertEquals(32,icons.get(d).getHeight());assertTrue(row.getString("sourceMember").endsWith(".gif"));}
   else if(row.has("inventoryArtwork")){fallback++;assertTrue(row.getString("limitation").contains("unresolved"));}
  }
  assertEquals(56,weapons);assertEquals(32,dedicated);assertEquals(23,fallback);
  assertEquals("mw002",r.itemDefinitions().get("IT_TEST_WEAPON_MW002").appearanceId);
  assertEquals(20,(int)r.itemDefinitions().get("IT_TEST_WEAPON_MW002").statModifiers.get("MinATK"));
  assertEquals(30,(int)r.itemDefinitions().get("IT_TEST_WEAPON_MW002").statModifiers.get("MaxATK"));
  GameView v=new GameView(c);v.layout(0,0,960,540);ItemWindowReferenceTest.tap(v,742,28);save(v,"inventory-source-weapons");
 }
 @Test public void referenceMatchedRingReplacesRejectedCaptureWhileDetailsRemainReachable()throws Exception{
  SourceItemIconRegistry icons=new SourceItemIconRegistry(c);assertFalse(icons.pending("IT_RING_THREELINEGOLD"));Bitmap ring=icons.get("IT_RING_THREELINEGOLD");assertNotNull(ring);assertEquals(32,ring.getWidth());assertEquals(32,ring.getHeight());assertEquals("USER_REFERENCE_MATCHED_NATIVE_CAPTURE",icons.receipt("IT_RING_THREELINEGOLD").getString("identityMatch"));
  try{c.getAssets().open("equipment-icons/it_ring_threelinegold.png");fail("Rejected capture packaged");}catch(java.io.FileNotFoundException expected){}
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(v,"state");ItemWindow w=ItemWindowReferenceTest.field(v,"itemWindow");ItemWindowReferenceTest.tap(v,742,28);
  List<RpgInventoryPresentation.ItemRow> rows=new ArrayList<>(new RpgInventoryPresentation().inventoryRows(state.rpg()));int index=0;while(!"IT_RING_THREELINEGOLD".equals(rows.get(index).itemId))index++;
  w.page=index/50;int local=index%50;RectF cell=ItemWindow.cell(local);ItemWindowReferenceTest.tap(v,cell.centerX(),cell.centerY());assertEquals("IT_RING_THREELINEGOLD",w.hitItem);assertTrue(w.details);save(v,"ring-native-details");

 }
 void save(GameView view,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(b));File p=new File("build/reports/inventory-art-v131");p.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(p,name+".png"))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();}
}
