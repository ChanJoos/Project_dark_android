package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.*;
import android.graphics.*;
import java.util.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class InventoryFeedbackV199Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 void save(Bitmap b,String n)throws Exception{File f=new File("build/reports/horror-v179/v199/"+n+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
 @Test public void allNewClothingIconsUseExactFrontSourcePixels()throws Exception{
  SourceItemIconRegistry icons=new SourceItemIconRegistry(c);EquipmentVisualRegistry gear=new EquipmentVisualRegistry(c);RpgProgressionState r=new RpgProgressionState();int count=0;
  Bitmap sheet=Bitmap.createBitmap(700,((155+9)/10)*76,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);canvas.drawColor(0xff303030);
  for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values()){
   org.json.JSONObject receipt=icons.receipt(d.itemId);if(receipt==null||!receipt.optString("projection").contains("IDLE"))continue;String id=receipt.optString("appearanceId");if(!(id.startsWith("mu")||id.startsWith("wu")))continue;
   org.json.JSONObject meta=new org.json.JSONObject(new String(PresentationAssetBytes.read(c,"source-registration/"+id+".json"),java.nio.charset.StandardCharsets.UTF_8));org.json.JSONObject f=SourceEquipmentRegistration.sourceFrame(meta.getJSONObject("sprites").getJSONArray("01"),1);Bitmap atlas=BitmapFactory.decodeStream(c.getAssets().open("wearable-atlases/"+id+".webp"));Bitmap expected=Bitmap.createBitmap(atlas,f.getInt("sx"),f.getInt("sy"),f.getInt("w"),f.getInt("h"));Bitmap actual=icons.get(d.itemId);assertTrue(d.itemId,actual.sameAs(expected));
   SourceItemIconRegistry.draw(canvas,actual,new RectF((count%10)*70,(count/10)*76,(count%10)*70+68,(count/10)*76+70),new Paint());count++;
  }assertTrue("all 155 linked garments",count>=155);save(sheet,"front-clothing-icons");
 }
 @Test public void backgroundMattePreservesForegroundAndEnclosedPixels(){
  Bitmap original=Bitmap.createBitmap(40,40,Bitmap.Config.ARGB_8888);original.eraseColor(0xff302820);
  for(int y=10;y<30;y++)for(int x=10;x<30;x++)original.setPixel(x,y,Color.WHITE);original.setPixel(20,20,0xff302820);
  Bitmap clean=InventoryArtworkV199.removeTileBackground(original);assertEquals(0,Color.alpha(clean.getPixel(0,0)));assertEquals(Color.WHITE,clean.getPixel(10,10));assertEquals(0xff302820,clean.getPixel(20,20));assertEquals(255,Color.alpha(original.getPixel(0,0)));
 }
 @Test public void actualScreenshotAccessoriesHaveTransparentOuterBackground()throws Exception{
  SourceItemIconRegistry icons=new SourceItemIconRegistry(c);RpgProgressionState r=new RpgProgressionState();int changed=0;
  Bitmap sheet=Bitmap.createBitmap(720,600,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(sheet);out.drawColor(0xff246050);
  for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values()){
   org.json.JSONObject receipt=icons.receipt(d.itemId);if(receipt==null||!receipt.optString("projection").contains("BACKGROUND_PRESERVED"))continue;
   Bitmap clean=icons.get(d.itemId);if(clean==null)continue;boolean transparent=false;for(int x=0;x<clean.getWidth();x++)if(Color.alpha(clean.getPixel(x,0))==0)transparent=true;
   if(transparent)changed++;if(changed<140)out.drawBitmap(clean,(changed%18)*40,(changed/18)*50,null);
  }assertTrue("real source screenshot backgrounds removed",changed>=20);save(sheet,"clean-accessory-icons");
 }
 RpgProgressionState.ItemDefinition ring(String id,int hp){return new RpgProgressionState.ItemDefinition(id,"검증 반지","반지",1,Collections.<String>emptySet(),true,null,null,Collections.singletonMap("HP",hp),RpgProgressionState.Evidence.U);}
 @Test public void exactAliasesStackButDifferentStatsStaySeparateAndPairedEquipKeepsIds(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);r.registerSourceAccessory(ring("ALIAS_A",100));r.registerSourceAccessory(ring("ALIAS_B",100));r.registerSourceAccessory(ring("VARIANT_C",200));Map<String,Integer> owned=new LinkedHashMap<>();owned.put("ALIAS_A",1);owned.put("ALIAS_B",2);owned.put("VARIANT_C",1);assertTrue(r.restoreOwnedItems(owned,Collections.emptyMap()));
  ItemWindow window=new ItemWindow(c);List<RpgInventoryPresentation.ItemRow> rows=window.rows(r);assertEquals(2,rows.size());assertEquals(3,rows.get(0).quantity);assertFalse(InventoryGroupsV199.equivalent(r,"ALIAS_A","VARIANT_C"));assertEquals(owned,r.inventory());
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot("ALIAS_A","반지"));String right=RpgProgressionState.RIGHT_RING_SLOT;assertEquals("ALIAS_B",InventoryGroupsV199.actionId(r,"ALIAS_A",right));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(InventoryGroupsV199.actionId(r,"ALIAS_A",right),right));assertEquals("ALIAS_B",InventoryGroupsV199.actionId(r,"ALIAS_A",right));assertEquals(owned,r.inventory());assertEquals(2,window.rows(r).size());
 }
 int difference(Bitmap a,Bitmap b){int n=0;for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++)if(a.getPixel(x,y)!=b.getPixel(x,y))n++;return n;}
 @Test public void knownRapidTiersDrawBothBodiesFourDirectionsIdleWalkAndContact()throws Exception{
  SkillBodyRenderer renderer=new SkillBodyRenderer(c,new SkillPresentationCatalog(c));String[] items={"USER_ITEM_20261018_R05141","USER_ITEM_20261018_R05145","USER_ITEM_20261018_R05143","USER_ITEM_20261018_R05142","USER_ITEM_20261018_R01339"};int cells=0;
  Bitmap sheet=Bitmap.createBitmap(800,1040,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(sheet);out.drawColor(0xff303030);
  for(String item:items)for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values())for(String motion:new String[]{"IDLE","WALK","SWING"}){
   String rapid=RapidHairclipV199.appearance(item,"장식");Bitmap before=Bitmap.createBitmap(140,120,Bitmap.Config.ARGB_8888),after=Bitmap.createBitmap(140,120,Bitmap.Config.ARGB_8888);CharacterRenderer.Pose a=new CharacterRenderer.Pose(70,102,direction,CharacterRenderer.State.IDLE,0,.45f,1,false,null,null,null,CharacterRenderer.EffectFamily.NONE),b=new CharacterRenderer.Pose(70,102,direction,CharacterRenderer.State.IDLE,0,.45f,1,false,rapid,null,null,CharacterRenderer.EffectFamily.NONE);
   assertTrue(body+motion,renderer.draw(new Canvas(before),a,body,motion,.45f));assertTrue(renderer.draw(new Canvas(after),b,body,motion,.45f));assertTrue(item+body+direction+motion,difference(before,after)>0);if(motion.equals("IDLE")){out.drawBitmap(after,(cells%5)*160,(cells/5)*130,null);cells++;}
  }assertEquals(0,RapidHairclipV199.tier("USER_ITEM_20261018_R05144"));save(sheet,"rapid-known-tiers");
 }
 @Test public void actualInventoryAndEquipmentShowRapidBindingAndRetainItAfterRestart()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(v,"state");RpgProgressionState r=state.rpg();r.enableEquipmentSandbox(true);String id="USER_ITEM_20261018_R05141";assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(id,"장식"));assertTrue(CharacterVisualBinding.from(r).equipmentVisualRef().contains("mg146"));
  java.util.Map<String,java.util.List<RpgInventoryPresentation.ItemRow>> names=new java.util.LinkedHashMap<>();for(RpgInventoryPresentation.ItemRow row:new RpgInventoryPresentation().inventoryRows(r))names.computeIfAbsent(row.name,k->new java.util.ArrayList<>()).add(row);
  org.json.JSONArray duplicates=new org.json.JSONArray();for(java.util.Map.Entry<String,java.util.List<RpgInventoryPresentation.ItemRow>> e:names.entrySet())if(e.getValue().size()>1){org.json.JSONArray aliases=new org.json.JSONArray();for(RpgInventoryPresentation.ItemRow row:e.getValue()){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(row.itemId);aliases.put(new org.json.JSONObject().put("id",d.itemId).put("appearance",d.appearanceId).put("level",d.requiredLevel).put("jobResolved",d.jobRestrictionResolved).put("stats",new org.json.JSONObject(d.statModifiers)));}duplicates.put(new org.json.JSONObject().put("name",e.getKey()).put("items",aliases));}
  File audit=new File("build/reports/horror-v179/v199/duplicate-audit.json");audit.getParentFile().mkdirs();java.nio.file.Files.writeString(audit.toPath(),new org.json.JSONObject().put("rawRows",new RpgInventoryPresentation().inventoryRows(r).size()).put("groupedRows",new ItemWindow(c).rows(r).size()).put("sameNames",duplicates).toString(2));
  ItemWindowReferenceTest.tap(v,742,28);Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(image));save(image,"actual-inventory");ItemWindowReferenceTest.tap(v,916,66);ItemWindowReferenceTest.tap(v,830,28);v.draw(new Canvas(image));save(image,"actual-equipped-rapid");v.pause();F5mSaveStore.install(c);GameView restored=new GameView(c);RuntimeState s=ItemWindowReferenceTest.field(restored,"state");assertEquals(id,s.rpg().equipment().get("장식"));assertTrue(CharacterVisualBinding.from(s.rpg()).equipmentVisualRef().contains("mg146"));
 }
}
