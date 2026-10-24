package com.projectdark.mobile;

import android.content.SharedPreferences;
import android.util.Xml;
import com.projectdark.mobile.world.MineTownGeometry;
import java.io.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.xmlpull.v1.*;
import static org.junit.Assert.*;

/** Explicitly authorized synthetic filming profiles. Never installed in a user save or production code. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FilmingProfileExportTest {
  final MineTownV203Test f=new MineTownV203Test();
  static final String[] PREFS={"project_dark_f5m_v1","project_dark_skill_test_v1","project_dark_quick_items_v254","project_dark_visual_v1","project_dark_skill_book_v1"};
  static final String[] JOBS={"WARRIOR","ROGUE","MAGE","CLERIC","MARTIAL_ARTIST"};
  static final int MILLION=1_000_000;
  void clean(){f.reset();for(String p:PREFS)assertTrue(f.c.getSharedPreferences(p,0).edit().clear().commit());F5mSaveStore.install(f.c);}
  String[] skills(String job){return "ROGUE".equals(job)?new String[]{"SK_도적_002","SK_도적_005","SK_도적_023","SK_도적_019"}:"MAGE".equals(job)?new String[]{"SK_마법사_050","SK_마법사_017","SK_마법사_043","SK_마법사_044"}:new String[0];}
  GameView setup(String job)throws Exception{
    GameView v=f.town();RuntimeState s=f.field(v,"state");RpgProgressionState r=s.rpg();
    r.restoreGrowthRandomState(20261024L);
    while(r.normalLevel()<3)r.grantAdaptedReward(LevelExpCurve.requiredForNext(r.normalLevel()),0);
    assertTrue(r.chooseInitialJob(job));
    while(r.normalLevel()<99)r.grantAdaptedReward(LevelExpCurve.requiredForNext(r.normalLevel()),0);
    if(job.equals("ROGUE")||job.equals("MAGE")){
      for(String slot:new ArrayList<>(r.equipment().keySet()))r.unequip(slot);
      String[] gear=job.equals("ROGUE")?new String[]{"USER_ITEM_20261018_R01486","USER_ITEM_20261018_R01485",RequestedWeaponSourceFacts.SHINWOLDO,"IT_SHIELD_KNIGHT"}:new String[]{"USER_ITEM_20261018_R01714","USER_ITEM_20261018_R01712",RequestedWeaponSourceFacts.ROO_GRACE};
      for(String id:gear)assertEquals(id,RpgProgressionState.EquipResult.EQUIPPED,r.equip(id));
    }
    // Explicit synthetic test resources, not claimed natural growth. Compensate exact current gear modifiers.
    int hpMod=r.finalStats().maxHp-r.baseMaxHp(),mpMod=r.finalStats().maxMp-r.baseMaxMp();
    r.restoreBaseResources(MILLION-hpMod,MILLION-mpMod);
    r.grantAdaptedReward(TempleGrowthPolicy.ADVANCEMENT_EXP,0);
    assertEquals(TempleGrowthPolicy.Result.OK,r.advanceAtTemple());
    r.restoreGold(500);s.applyDerivedGrowth();s.player().hp=s.player().maxHp;s.player().mp=s.player().maxMp;
    v.setSkillTestMode(true);SkillBook book=f.field(v,"skillBook");
    for(int i=0;i<SkillBook.SLOT_COUNT;i++)book.clearSlot(i);
    String[] slots=skills(job);for(int i=0;i<slots.length;i++){assertTrue(book.get(slots[i]).jobMatches(job));assertTrue(book.assign(i,slots[i]));}
    assertTrue((Boolean)f.call(v,"saveSkillSlots",new Class<?>[]{}));
    QuickItemBarV254 bar=f.field(v,"quickItems");
    String[] necklaces={StarterNecklacesV259.FIRE,StarterNecklacesV259.EARTH,StarterNecklacesV259.WIND,StarterNecklacesV259.WATER};
    for(int i=0;i<4;i++)assertTrue(bar.assign(i,StarterNecklacesV259.id(necklaces[i]),r));
    assertTrue("real LIFE necklace remains available in sandbox",r.inventory().getOrDefault("USER_ITEM_20261018_R03547",0)>0);
    assertFalse((Boolean)f.field(v,"autoAttackEnabled"));assertTrue(F5mSaveStore.checkpointActive());return v;
  }
  void verify(GameView v,String job,Map<String,String> gear)throws Exception{
    RuntimeState s=f.field(v,"state");RpgProgressionState r=s.rpg();
    assertEquals(job,r.currentJobCode());assertEquals(99,r.normalLevel().intValue());assertTrue(r.firstAdvanced());assertEquals(RpgProgressionState.ProgressionNode.FIRST_ADVANCEMENT,r.progressionNode());
    assertEquals(MILLION,r.finalStats().maxHp);assertEquals(MILLION,r.finalStats().maxMp);
    assertEquals(MILLION,s.player().maxHp);assertEquals(MILLION,s.player().maxMp);assertEquals(MILLION,s.player().hp);assertEquals(MILLION,s.player().mp);
    assertEquals(gear,r.equipment());assertEquals(MineTownGeometry.ID,s.currentMapId());assertEquals(500,r.gold().longValue());
    assertFalse((Boolean)f.field(v,"autoAttackEnabled"));SkillBook b=f.field(v,"skillBook");assertTrue(b.testAccess());
    String[] expected=skills(job);for(int i=0;i<SkillBook.SLOT_COUNT;i++)assertEquals(i<expected.length?expected[i]:null,b.slot(i));
  }
  // Export the typed values produced by canonical checkpoint, using Android's standard prefs XML schema.
  void exportPrefs(File file,Map<String,?> values)throws Exception{
    assertTrue(file.getParentFile().isDirectory()||file.getParentFile().mkdirs());
    try(FileOutputStream out=new FileOutputStream(file)){
      XmlSerializer x=Xml.newSerializer();x.setOutput(out,"UTF-8");x.startDocument("UTF-8",true);x.startTag(null,"map");
      for(String key:new TreeSet<>(values.keySet())){
        Object v=values.get(key);String type=v instanceof String?"string":v instanceof Integer?"int":v instanceof Long?"long":v instanceof Float?"float":v instanceof Boolean?"boolean":null;
        assertNotNull("supported pref type "+key,type);x.startTag(null,type);x.attribute(null,"name",key);
        if(v instanceof String)x.text((String)v);else x.attribute(null,"value",v.toString());x.endTag(null,type);
      }x.endTag(null,"map");x.endDocument();
    }
  }
  void importPrefs(File file,SharedPreferences prefs)throws Exception{
    SharedPreferences.Editor e=prefs.edit().clear();try(FileInputStream in=new FileInputStream(file)){
      XmlPullParser p=Xml.newPullParser();p.setInput(in,"UTF-8");
      for(int event=p.next();event!=XmlPullParser.END_DOCUMENT;event=p.next())if(event==XmlPullParser.START_TAG&&!p.getName().equals("map")){
        String type=p.getName(),key=p.getAttributeValue(null,"name"),value=p.getAttributeValue(null,"value");
        switch(type){case "string":e.putString(key,p.nextText());break;case "int":e.putInt(key,Integer.parseInt(value));break;case "long":e.putLong(key,Long.parseLong(value));break;case "float":e.putFloat(key,Float.parseFloat(value));break;case "boolean":e.putBoolean(key,Boolean.parseBoolean(value));break;default:fail(type);}
      }
    }assertTrue(e.commit());
  }
  @Test public void fiveSyntheticPromotedMillionResourceProfilesRoundTripCanonicalSaveAndExport()throws Exception{
    for(String job:JOBS){clean();GameView v=setup(job);RuntimeState s=f.field(v,"state");Map<String,String> gear=new LinkedHashMap<>(s.rpg().equipment());verify(v,job,gear);
      File root=new File("build/reports/horror-v179/filming-profiles/"+job);Map<String,Map<String,?>> snapshots=new LinkedHashMap<>();
      for(String pref:PREFS){Map<String,?> values=new LinkedHashMap<>(f.c.getSharedPreferences(pref,0).getAll());snapshots.put(pref,values);exportPrefs(new File(root,"shared_prefs/"+pref+".xml"),values);}
      clean();for(String pref:PREFS){SharedPreferences p=f.c.getSharedPreferences(pref,0);importPrefs(new File(root,"shared_prefs/"+pref+".xml"),p);assertEquals(snapshots.get(pref),p.getAll());}
      F5mSaveStore.install(f.c);GameView restored=new GameView(f.c,true);restored.layout(0,0,960,540);restored.setSkillTestMode(true);verify(restored,job,gear);assertTrue(F5mSaveStore.writable());assertTrue(F5mSaveStore.checkpointActive());
      JSONObject manifest=new JSONObject().put("job",job).put("level",99).put("firstAdvanced",true).put("currentHp",MILLION).put("maxHp",MILLION).put("currentMp",MILLION).put("maxMp",MILLION).put("baseHp",s.rpg().baseMaxHp()).put("baseMp",s.rpg().baseMaxMp()).put("equipment",new JSONObject(gear)).put("gold",500).put("startMap",MineTownGeometry.ID).put("auto",false).put("skillTestMode",true).put("freeSkillMp",true).put("unallocatedStatPoints",s.rpg().statPoints()).put("skillSlots",new JSONArray(Arrays.asList(skills(job)))).put("provenance","USER AUTHORIZED SYNTHETIC FILMING PROFILE: 99 promoted, postgear HP/MP each1000000. Not naturally earned progression. Test-only exporter, no global balance/production endpoint/user-save changes. Canonical checkpoint then typed XML export/import and fresh GameView reload verified. Android installation verification separate.");
      try(FileWriter w=new FileWriter(new File(root,"provenance.json"))){w.write(manifest.toString(2));}
      f.shot(restored,"filming-"+job);
    }
  }
}
