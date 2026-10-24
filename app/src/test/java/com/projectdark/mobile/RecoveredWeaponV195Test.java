package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.util.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RecoveredWeaponV195Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 @Test public void registrationPreservesEverySourceFrameOmission()throws Exception{
  RpgProgressionState r=new RpgProgressionState();EquipmentVisualRegistry registry=new EquipmentVisualRegistry(c);SourceItemIconRegistry icons=new SourceItemIconRegistry(c);assertEquals(6,RecoveredWeaponCatalogV195.IDS.length);
  for(String id:RecoveredWeaponCatalogV195.IDS){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);assertNotNull(id,icons.get(id));EquipmentVisualRegistry.Visual v=registry.get(d.appearanceId);assertNotNull(id,v);assertNotNull(id,v.registration);org.json.JSONObject source=new org.json.JSONObject(read("source-registration/"+d.appearanceId+".json"));for(int f=0;f<10;f++)assertEquals(id+" idle "+f,SourceEquipmentRegistration.sourceFrame(source.getJSONObject("sprites").optJSONArray("01"),f)!=null,v.registration.idleByIndex(f)!=null);for(int f=0;f<4;f++)assertEquals(id+" action "+f,SourceEquipmentRegistration.sourceFrame(source.getJSONObject("sprites").optJSONArray("02"),f)!=null,v.registration.action(f)!=null);}
 }
 @Test public void omittedActionFrameKeepsBodyWithoutPaintingAnEntireAtlas(){
  RpgProgressionState r=new RpgProgressionState();EquipmentVisualRegistry registry=new EquipmentVisualRegistry(c);CharacterRenderer renderer=new CharacterRenderer(c);int checks=0;
  for(String id:RecoveredWeaponCatalogV195.IDS){String appearance=r.itemDefinitions().get(id).appearanceId;EquipmentVisualRegistry.Visual v=registry.get(appearance);for(CharacterRenderer.Direction dir:CharacterRenderer.Direction.values())if(v.registration.action(CharacterRenderer.actionSourceIndex(dir))==null){Bitmap withGear=Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888),bodyOnly=Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888);renderer.draw(new Canvas(withGear),new CharacterRenderer.Pose(100,150,dir,CharacterRenderer.State.ATTACK,0,.4f,1,false,"",appearance,null,CharacterRenderer.EffectFamily.NONE,AnimationAction.SWING));renderer.draw(new Canvas(bodyOnly),new CharacterRenderer.Pose(100,150,dir,CharacterRenderer.State.ATTACK,0,.4f,1,false,"",null,null,CharacterRenderer.EffectFamily.NONE,AnimationAction.SWING));int[] actual=new int[40000],expected=new int[40000];withGear.getPixels(actual,0,200,0,0,200,200);bodyOnly.getPixels(expected,0,200,0,0,200,200);assertArrayEquals(id+dir+" omitted gear layer must preserve the exact body image",expected,actual);int visible=0;for(int pixel:actual)if((pixel>>>24)>0)visible++;assertTrue("body remains visible",visible>50);withGear.recycle();bodyOnly.recycle();checks++;}}
  assertEquals(5,checks);
 }
 @Test public void realGameViewEquipsAndPersistsEachWeapon()throws Exception{
  GameView view=new GameView(c);view.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(view,"state");RpgProgressionState r=state.rpg();r.enableEquipmentSandbox(true);r.bindTwoHandLearning(()->true);ItemWindowReferenceTest.tap(view,830,28);File out=new File("build/reports/horror-v179/recovered-weapons-v195");out.mkdirs();for(String id:RecoveredWeaponCatalogV195.IDS){assertEquals(id,RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(id,"무기"));assertEquals(r.itemDefinitions().get(id).appearanceId,CharacterVisualBinding.from(r).weaponVisualRef());Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(image));try(FileOutputStream stream=new FileOutputStream(new File(out,id+".png"))){image.compress(Bitmap.CompressFormat.PNG,100,stream);}image.recycle();}F5mSaveStore.saveRewardsActive(r);GameView restart=new GameView(c);RuntimeState restored=ItemWindowReferenceTest.field(restart,"state");assertEquals(r.equipment(),restored.rpg().equipment());
 }
 @Test public void migrationSkipsBankAndDoesNotRestockRemovedWeapons(){
  android.content.SharedPreferences p=c.getSharedPreferences("project_dark_f5m_v1",0);RpgProgressionState old=new RpgProgressionState();String banked=RecoveredWeaponCatalogV195.IDS[0];assertTrue(old.restoreOwnedItems(Collections.emptyMap(),Collections.emptyMap()));assertTrue(old.restoreBank(0,Collections.singletonMap(banked,1)));F5mSaveStore.saveRewardsActive(old);p.edit().remove("recovered_weapons_granted_v195").commit();RpgProgressionState current=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(current);assertTrue(F5mSaveStore.writable());assertFalse(current.inventory().containsKey(banked));for(String id:RecoveredWeaponCatalogV195.IDS)if(!id.equals(banked))assertEquals(Integer.valueOf(1),current.inventory().get(id));assertTrue(current.restoreOwnedItems(Collections.emptyMap(),Collections.emptyMap()));F5mSaveStore.saveRewardsActive(current);RpgProgressionState restart=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restart);assertTrue(restart.inventory().isEmpty());assertEquals(Integer.valueOf(1),restart.bankInventory().get(banked));
 }
 private String read(String path)throws Exception{try(InputStream in=c.getAssets().open(path);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);return new String(out.toByteArray(),java.nio.charset.StandardCharsets.UTF_8);}}
}
