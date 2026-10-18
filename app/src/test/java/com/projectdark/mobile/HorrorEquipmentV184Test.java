package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class HorrorEquipmentV184Test {
 @Test public void modifiersMatchEveryRetainedUserSourceCell() throws Exception {
  org.json.JSONArray rows=new org.json.JSONObject(java.nio.file.Files.readString(java.nio.file.Path.of("../master/canonical/User_Item_Master_20261018.json"))).getJSONArray("records");
  RpgProgressionState r=new RpgProgressionState();Map<String,org.json.JSONObject> source=new HashMap<>();for(int i=0;i<rows.length();i++)source.put(rows.getJSONObject(i).getString("itemId"),rows.getJSONObject(i));
  String[] fields={"AC","HIT","DAM","HP","MP","STR","DEX","INT","WIS","CON"};
  for(String id:HorrorEquipmentCatalog.IDS){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);org.json.JSONArray cells=source.get(id).getJSONArray("cells");assertEquals(cells.getString(0),d.name);for(int n=0;n<fields.length;n++)assertEquals(id+fields[n],cells.getInt(9+n),(int)d.statModifiers.get(fields[n]));assertEquals(cells.getInt(21),(int)d.requiredLevel);
   if("무기".equals(d.equipSlot)){String[] small=cells.getString(24).split("m"),large=cells.getString(25).split("m");assertEquals(Integer.valueOf(small[0]),d.statModifiers.get("MinATK"));assertEquals(Integer.valueOf(small[1]),d.statModifiers.get("MaxATK"));assertEquals(Integer.valueOf(large[0]),d.statModifiers.get("LMinATK"));assertEquals(Integer.valueOf(large[1]),d.statModifiers.get("LMaxATK"));}
  }
 }
 @Test public void existingProfilesReceiveKitOnceAndEveryItemEquipsWithItsSourceRange() {
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(false);r.bindTwoHandLearning(()->true);
  Map<String,Integer> first=new LinkedHashMap<>(r.inventory());r.enableEquipmentSandbox(false);assertEquals(first,r.inventory());
  for(String id:HorrorEquipmentCatalog.IDS){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);assertEquals(RpgProgressionState.Evidence.U,d.evidence);assertTrue(r.inventory().get(id)>=1);r.unequip(d.equipSlot);assertEquals(id,RpgProgressionState.EquipResult.EQUIPPED,r.equip(id));assertEquals(d.appearanceId,"무기".equals(d.equipSlot)?CharacterVisualBinding.from(r).weaponVisualRef():r.equippedDefinition(d.equipSlot).appearanceId);
   if("무기".equals(d.equipSlot)){assertEquals(id,(int)d.statModifiers.get("MinATK"),r.finalStats().weaponMinAttack);assertEquals(id,(int)d.statModifiers.get("MaxATK"),r.finalStats().weaponMaxAttack);}
  }
  RpgProgressionState copy=new RpgProgressionState();copy.bindTwoHandLearning(()->true);assertTrue(copy.restoreOwnedItems(r.inventory(),r.equipment()));assertEquals(r.inventory(),copy.inventory());assertEquals(r.equipment(),copy.equipment());
 }
 @Test public void allAdmittedItemsHaveExactIconsAndEveryDirectionalIdleWalkAttackFrame() {
  Context c=RuntimeEnvironment.getApplication();EquipmentVisualRegistry registry=new EquipmentVisualRegistry(c);SourceItemIconRegistry icons=new SourceItemIconRegistry(c);RpgProgressionState r=new RpgProgressionState();
  for(String id:HorrorEquipmentCatalog.IDS){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);EquipmentVisualRegistry.Visual v=registry.get(d.appearanceId);assertNotNull(id,v);assertNotNull(id,v.registration);assertNotNull(id,icons.get(id));assertTrue(id,v.atlas.getWidth()>icons.get(id).getWidth());
   for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values())for(int n=0;n<5;n++)assertNotNull(id+direction+n,v.registration.idle(direction,n));
   for(int n=0;n<4;n++){SourceEquipmentRegistration.Frame f=v.registration.action(n);assertNotNull(id+n,f);assertTrue(id,f.src.right<=v.atlas.getWidth()&&f.src.bottom<=v.atlas.getHeight());}
  }
 }
}
