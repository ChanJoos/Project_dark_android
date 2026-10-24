package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;import java.io.*;import java.util.*;import org.json.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class InventoryPolishV266Test {
 final HudItemsV254Test f=new HudItemsV254Test();
 @Before public void setup(){f.setup();}
 @Test public void consumptionTabIncludesAllReagentsFoodAndRecallsWithoutAddingOwnership()throws Exception{
  GameView v=f.view(2340,1080);RuntimeState s=f.h.field(v,"state");RpgProgressionState r=s.rpg();Map<String,Integer> inventory=new LinkedHashMap<>(r.inventory());
  for(String id:LinkedFoodCatalogV187.IDS)inventory.put(id,1);
  for(String id:r.itemDefinitions().keySet())if(id.startsWith("IT_REAGENT_")||id.startsWith("IT_RECALL_"))inventory.put(id,1);
  assertTrue(r.restoreOwnedItems(inventory,Collections.emptyMap()));Map<String,Integer> owned=new LinkedHashMap<>(r.inventory());
  EquipmentHudV262Test.tap(v,(742+210)*2,56);ItemWindow w=f.h.field(v,"itemWindow");RectF tab=ItemWindow.filterTab(10);
  float x=InventoryPairV262.INV_X+tab.centerX()*InventoryPairV262.INV_SCALE+105,y=InventoryPairV262.INV_Y+tab.centerY()*InventoryPairV262.INV_SCALE;
  EquipmentHudV262Test.tap(v,x*2,y*2);assertEquals(10,w.filter);assertEquals("소비",ItemWindow.FILTERS[10]);
  Set<String> actual=new HashSet<>();for(RpgInventoryPresentation.ItemRow row:w.rows(r)){assertFalse(r.itemDefinitions().get(row.itemId).equippable());actual.add(row.itemId);}
  for(String id:LinkedFoodCatalogV187.IDS)assertTrue(id,actual.contains(RpgProgressionState.canonicalItemId(id)));
  for(String id:owned.keySet())if(id.startsWith("IT_REAGENT_")||id.startsWith("IT_RECALL_"))assertTrue(id,actual.contains(id));
  assertFalse(actual.contains(RpgProgressionState.STARTER_SHIRT_ITEM_ID));assertEquals(owned,r.inventory());
  EquipmentHudV262Test.render(v,"v266-consumption-tab",2340,1080);
 }
 @Test public void allTenRecallsLoseOuterBlackRectangleAndKeepForegroundPixelsAndCachedIdentity()throws Exception{
  SourceItemIconRegistry icons=new SourceItemIconRegistry(f.h.c);JSONArray rows=new JSONObject(new String(PresentationAssetBytes.read(f.h.c,"recalls-v251/manifest.json"),java.nio.charset.StandardCharsets.UTF_8)).getJSONArray("recalls");
  Bitmap sheet=Bitmap.createBitmap(700,160,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(sheet);c.drawColor(0xff314044);
  for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);String id=row.getString("id");Bitmap raw=BitmapFactory.decodeStream(f.h.c.getAssets().open(row.getString("path"))),clean=icons.get(id);assertSame(clean,icons.get(id));assertEquals(raw.getWidth(),clean.getWidth());int cleared=0,retained=0;
   for(int y=0;y<raw.getHeight();y++)for(int x=0;x<raw.getWidth();x++){int a=raw.getPixel(x,y),b=clean.getPixel(x,y);if(Color.alpha(b)==0)cleared++;else{assertEquals(id,a,b);retained++;}if(Math.max(Color.red(a),Math.max(Color.green(a),Color.blue(a)))>24)assertEquals(id,a,b);}
   assertTrue(id+" outer background removed "+cleared,cleared>500);assertTrue(id+" foreground retained",retained>100);assertEquals(0,Color.alpha(clean.getPixel(0,0)));
   SourceItemIconRegistry.draw(c,raw,new RectF(i*70,0,i*70+60,65),new Paint());SourceItemIconRegistry.draw(c,clean,new RectF(i*70,80,i*70+60,145),new Paint());
  }save(sheet,"recall-before-after");
 }
 @Test public void everyNecklaceVisibleBoundsAreCenteredOnBothAxesAndActualEquipmentUsesSameProjection()throws Exception{
  GameView v=f.view(960,540);RuntimeState s=f.h.field(v,"state");RpgProgressionState r=s.rpg();SourceItemIconRegistry icons=new SourceItemIconRegistry(f.h.c);Set<String> seen=new HashSet<>();List<RpgProgressionState.ItemDefinition> necklaces=new ArrayList<>();
  for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values())if("목걸이".equals(d.equipSlot)&&seen.add(RpgProgressionState.canonicalItemId(d.itemId))&&!icons.pending(d.itemId))necklaces.add(d);
  assertTrue(necklaces.size()>10);Bitmap sheet=Bitmap.createBitmap(800,((necklaces.size()+7)/8)*100,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);canvas.drawColor(0xff303030);Paint p=new Paint();int n=0;
  for(RpgProgressionState.ItemDefinition d:necklaces){Bitmap src=icons.get(d);assertNotNull(d.itemId,src);Bitmap tile=Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);SourceItemIconRegistry.drawEquipmentForSlot(new Canvas(tile),src,new RectF(23,23,77,77),p,"목걸이",d.itemId);Rect bounds=SourceItemIconRegistry.visibleEquipmentBounds(tile);assertFalse(d.itemId,bounds.isEmpty());assertEquals(d.itemId,50,bounds.exactCenterX(),.8);assertEquals(d.itemId,50,bounds.exactCenterY(),.8);assertTrue(d.itemId,bounds.left>=23&&bounds.top>=23&&bounds.right<=77&&bounds.bottom<=77);canvas.drawBitmap(tile,n%8*100,n/8*100,null);n++;}
  save(sheet,"all-necklaces-centered");
  r.enableEquipmentSandbox(true);String id=necklaces.get(0).itemId;assertEquals(RpgProgressionState.AutoLootResult.LOOTED,r.autoLootResolvedItem(id,1));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(id,"목걸이"));EquipmentHudV262Test.tap(v,742,28);EquipmentHudV262Test.render(v,"v266-equipped-necklace",960,540);
 }
 static void save(Bitmap b,String name)throws Exception{File p=new File("build/reports/horror-v179/v266/"+name+".png");p.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(p)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
}
