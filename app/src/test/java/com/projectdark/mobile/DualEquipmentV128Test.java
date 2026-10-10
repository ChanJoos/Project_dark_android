package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DualEquipmentV128Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 @Test public void differentAndIdenticalPairsAddExactlyAndRemoveOnlyTheSelectedHand(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);int hp=r.finalStats().maxHp,mp=r.finalStats().maxMp,ac=r.finalStats().ac;
  String ring="IT_RING_THREELINEGOLD";
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(ring,"반지"));
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(ring,RpgProgressionState.RIGHT_RING_SLOT));
  assertEquals(hp+600,r.finalStats().maxHp);assertEquals(mp+300,r.finalStats().maxMp);assertEquals(ac-2,r.finalStats().ac);
  assertEquals(RpgProgressionState.EquipResult.UNEQUIPPED,r.unequip(RpgProgressionState.RIGHT_RING_SLOT));assertEquals(ring,r.equipment().get("반지"));assertEquals(hp+300,r.finalStats().maxHp);
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot("IT_RING_REDJADE",RpgProgressionState.RIGHT_RING_SLOT));assertEquals(hp+400,r.finalStats().maxHp);
  r.equipToSlot("IT_GLOVE_LEATHER","장갑");r.equipToSlot("IT_GLOVE_LEATHER",RpgProgressionState.RIGHT_GLOVE_SLOT);assertEquals(2,r.equippedCount("IT_GLOVE_LEATHER"));assertTrue(CharacterVisualBinding.from(r).isDefinitionConsistent());
  assertEquals(ac-3,r.finalStats().ac);r.unequip("장갑");assertEquals(ac-2,r.finalStats().ac);
 }
 @Test public void quantityCannotBeDuplicatedAndMalformedSaveIsRejectedAtomically(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(false);r.autoLootResolvedItem("IT_RING_REDJADE",1);
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot("IT_RING_REDJADE","반지"));
  assertEquals(RpgProgressionState.EquipResult.ITEM_NOT_OWNED,r.equipToSlot("IT_RING_REDJADE",RpgProgressionState.RIGHT_RING_SLOT));
  Map<String,String> invalid=new LinkedHashMap<>(r.equipment());invalid.put(RpgProgressionState.RIGHT_RING_SLOT,"IT_RING_REDJADE");Map<String,String> before=new LinkedHashMap<>(r.equipment());
  assertFalse(r.restoreOwnedItems(r.inventory(),invalid));assertEquals(before,r.equipment());
  assertEquals(RpgProgressionState.EquipResult.INVALID_SLOT,r.equipToSlot("IT_RING_REDJADE",RpgProgressionState.RIGHT_GLOVE_SLOT));
 }
 @Test public void legacyLeftSlotsAndFourEquippedInstancesSurviveRealSaveRestore(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);r.equipToSlot("IT_RING_REDJADE","반지");r.equipToSlot("IT_RING_THREELINEGOLD",RpgProgressionState.RIGHT_RING_SLOT);r.equipToSlot("IT_GLOVE_LEATHER","장갑");r.equipToSlot("IT_GLOVE_LEATHER",RpgProgressionState.RIGHT_GLOVE_SLOT);
  F5mSaveStore.saveRewardsActive(r);F5mSaveStore.install(c);RpgProgressionState saved=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(saved);
  assertEquals(r.inventory(),saved.inventory());assertEquals(r.equipment(),saved.equipment());assertEquals(r.finalStats().maxHp,saved.finalStats().maxHp);assertEquals(r.finalStats().ac,saved.finalStats().ac);assertTrue(F5mSaveStore.writable());
  // V127 keys remain the left hand. A single old ring never gets duplicated into the right hand.
  Map<String,String> legacy=new LinkedHashMap<>();legacy.put("반지","IT_RING_REDJADE");legacy.put("장갑","IT_GLOVE_LEATHER");assertTrue(saved.restoreOwnedItems(saved.inventory(),legacy));assertNull(saved.equipment().get(RpgProgressionState.RIGHT_RING_SLOT));
 }
 @Test public void smallSourceArtworkRendersAtOneSourcePixelPerLogicalPixel()throws Exception{
  SourceItemIconRegistry icons=new SourceItemIconRegistry(c);
  for(String id:new String[]{"IT_EARRING_DOUBLE_SILVER","IT_REAGENT_KOMADIUM",RpgProgressionState.STARTER_HAT_ITEM_ID}){
   Bitmap src=icons.get(id),out=Bitmap.createBitmap(80,80,Bitmap.Config.ARGB_8888);Paint paint=new Paint();SourceItemIconRegistry.draw(new Canvas(out),src,new RectF(10,10,70,70),paint);
   int x=Math.round(40-src.getWidth()/2f),y=Math.round(40-src.getHeight()/2f);
   assertFalse(paint.isFilterBitmap());for(int yy=0;yy<src.getHeight();yy++)for(int xx=0;xx<src.getWidth();xx++)if(Color.alpha(src.getPixel(xx,yy))==255)assertEquals(id,src.getPixel(xx,yy),out.getPixel(x+xx,y+yy));
  }
 }
 @Test public void everyWearableIdentityResolvesRealFramesAndRendersAnAuditContactSheet()throws Exception{
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);SourceItemIconRegistry icons=new SourceItemIconRegistry(c);EquipmentVisualRegistry wearables=new EquipmentVisualRegistry(c);CharacterRenderer renderer=new CharacterRenderer(c);
  List<RpgProgressionState.ItemDefinition> gear=new ArrayList<>();Set<String> appearance=new HashSet<>();for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values())if(d.appearanceId!=null&&!d.appearanceId.equals("mw_chungryong")&&appearance.add(d.appearanceId))gear.add(d);
  Bitmap sheet=Bitmap.createBitmap(1000,((gear.size()+7)/8)*120,Bitmap.Config.ARGB_8888);Canvas cv=new Canvas(sheet);cv.drawColor(UiTheme.BG);int i=0;
  for(RpgProgressionState.ItemDefinition d:gear){
   JSONObject receipt=icons.receipt(d.itemId);assertEquals(d.itemId,d.appearanceId,receipt.getString("appearanceId"));EquipmentVisualRegistry.Visual v=wearables.get(d.appearanceId);assertNotNull(d.itemId,v);assertNotNull(d.itemId,v.registration);
   for(CharacterRenderer.Direction dir:CharacterRenderer.Direction.values())for(int col=0;col<5;col++)assertNotNull(d.itemId+dir+col,v.registration.idle(dir,col));
   for(int action=0;action<4;action++)assertNotNull(d.itemId+action,v.registration.action(action));
   r.restoreOwnedItems(r.inventory(),Collections.emptyMap());assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(d.itemId,d.equipSlot));CharacterVisualBinding binding=CharacterVisualBinding.from(r);assertTrue(binding.isDefinitionConsistent());
   float x=i%8*125,y=i/8*120;SourceItemIconRegistry.draw(cv,icons.get(d),new RectF(x+2,y+20,x+45,y+66),new Paint());
   CharacterRenderer.Pose pose=new CharacterRenderer.Pose(x+83,y+79,CharacterRenderer.Direction.SE,CharacterRenderer.State.IDLE,0,0,0,false,binding.equipmentVisualRef(),binding.weaponVisualRef(),null,CharacterRenderer.EffectFamily.NONE);renderer.draw(cv,pose);UiTheme.fit(cv,RpgInventoryPresentation.displayName(d.name),x+3,y+110,120,9,UiTheme.TEXT,false);i++;
  }
  File dir=new File("build/reports/identity-v128");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,"all-wearable-name-icon-actor.png"))){sheet.compress(Bitmap.CompressFormat.PNG,100,out);}
  assertEquals("매직파나",r.itemDefinitions().get("IT_WARDROBE_MW062").name);assertEquals("mh172",icons.receipt(RpgProgressionState.STARTER_HAT_ITEM_ID).getString("appearanceId"));
 }
 @Test public void actualEquipmentWindowCanTargetEitherRingWithoutTouchingTheOther()throws Exception{
  GameView view=new GameView(c);view.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(view,"state");ItemWindow w=ItemWindowReferenceTest.field(view,"itemWindow");RpgProgressionState r=state.rpg();r.equipToSlot("IT_RING_REDJADE","반지");r.equipToSlot("IT_RING_THREELINEGOLD",RpgProgressionState.RIGHT_RING_SLOT);
  ItemWindowReferenceTest.tap(view,830,28);int right=Arrays.asList(ItemWindow.SLOTS).indexOf(RpgProgressionState.RIGHT_RING_SLOT);RectF b=ItemWindow.slot(right);ItemWindowReferenceTest.tap(view,b.centerX(),b.centerY());assertEquals(RpgProgressionState.RIGHT_RING_SLOT,w.selectedSlot);ItemWindowReferenceTest.tap(view,805,490);assertNull(r.equipment().get(RpgProgressionState.RIGHT_RING_SLOT));assertEquals("IT_RING_REDJADE",r.equipment().get("반지"));
  Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(image));File dir=new File("build/reports/identity-v128");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,"paired-slots.png"))){image.compress(Bitmap.CompressFormat.PNG,100,out);}
 }
}
