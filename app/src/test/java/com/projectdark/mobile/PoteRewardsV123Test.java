package com.projectdark.mobile;

import static org.junit.Assert.*;
import java.util.*;
import android.content.Context;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public class PoteRewardsV123Test {
  private Context context;
  @Before public void reset(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
  static RpgProgressionState reportReady(String id,String job)throws Exception{
    RpgProgressionState r=new RpgProgressionState();r.grantAdaptedReward(22800,0);assertTrue(r.chooseInitialJob(job));
    CampaignProgress.Def target=CampaignProgress.find(id);r.restoreProgression(target.level,0L);
    JSONArray completed=new JSONArray();for(CampaignProgress.Def d:r.campaign().route(job)){if(d.id.equals(id))break;completed.put(d.id);}
    JSONObject j=r.campaign().snapshot().put("complete",completed).put("active",id);
    JSONObject counts=new JSONObject();for(String species:target.targets.split(","))counts.put(species,target.goal);j.put("counts",counts);
    assertTrue(r.campaign().restore(j));assertEquals(CampaignProgress.Status.REPORT,r.campaign().status(target,r));return r;
  }
  @Test public void allJobRewardIdsResolveAndEachSlotHasReachableAcquisition(){
    for(String job:CampaignProgress.JOBS){RpgProgressionState r=new RpgProgressionState();r.grantAdaptedReward(22800,0);assertTrue(r.chooseInitialJob(job));Set<String> slots=new HashSet<>();
      for(CampaignProgress.Def d:r.campaign().route(job))for(String id:AdaptedPoteQuestRewards.items(d,r).keySet()){
        RpgProgressionState.ItemDefinition item=r.itemDefinitions().get(id);assertNotNull(job+" "+d.id+" "+id,item);if(item.equippable())slots.add(item.equipSlot);
      }
      assertTrue(slots.containsAll(Arrays.asList("갑옷","장갑","각반","신발","목걸이","벨트","반지")));
      assertTrue(AdaptedPoteQuestRewards.items(CampaignProgress.find("B06"),r).containsKey("IT_B_CAMPAIGN_TOOL_"+job+"_26"));
      assertFalse(AdaptedPoteQuestRewards.items(CampaignProgress.find("B06"),r).containsKey("IT_B_CAMPAIGN_"+job+"_11"));
    }
  }
  @Test public void departureGearBecomesUsableAtElevenWithoutReplacingCurrentOutfit()throws Exception{
    for(String job:CampaignProgress.JOBS){RpgProgressionState r=reportReady("M06",job);Map<String,String> equipped=new LinkedHashMap<>(r.equipment());
      assertTrue(r.campaign().claim("M06",r));assertEquals(Integer.valueOf(11),r.normalLevel());assertEquals(equipped,r.equipment());
      for(String id:new String[]{"IT_B_CAMPAIGN_"+job+"_11","IT_B_CAMPAIGN_TOOL_"+job+"_11"}){
        assertEquals(Integer.valueOf(1),r.inventory().get(id));assertEquals(RpgProgressionState.RequirementResult.MET,r.currentRequirements(id));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(id));
      }
    }
  }
  @Test public void bossRingGoldAndClaimStateSurviveDurableRestoreWithoutDuplicate()throws Exception{
    RpgProgressionState r=reportReady("D03","WARRIOR");long before=r.gold();
    assertTrue(F5mSaveStore.transactActive(r,null,()->r.campaign().claim("D03",r)));
    assertEquals(before+10000,r.gold().longValue());assertEquals(Integer.valueOf(1),r.inventory().get("IT_RING_THREELINEGOLD"));assertEquals(Integer.valueOf(41),r.normalLevel());
    F5mSaveStore.install(context);RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);
    assertTrue(F5mSaveStore.writable());assertEquals(r.inventory(),restored.inventory());assertEquals(r.gold(),restored.gold());assertFalse(restored.campaign().claim("D03",restored));
    assertEquals(RpgProgressionState.EquipResult.EQUIPPED,restored.equip("IT_RING_THREELINEGOLD"));assertEquals(Integer.valueOf(300),restored.recomputeStats().equipment.get("HP"));
  }
  @Test public void fullLaterItemAndGoldOverflowLeaveEntireClaimUnchanged()throws Exception{
    for(boolean overflow:new boolean[]{false,true}){RpgProgressionState r=reportReady("D03","WARRIOR");
      if(overflow)r.restoreGold(Long.MAX_VALUE-9999);else assertEquals(RpgProgressionState.AutoLootResult.LOOTED,r.autoLootResolvedItem("IT_RING_THREELINEGOLD",999999));
      Map<String,Integer> inv=new LinkedHashMap<>(r.inventory());long gold=r.gold(),exp=CampaignProgress.earnedExp(r);String quest=r.campaign().snapshot().toString();
      assertFalse(r.campaign().claim("D03",r));assertEquals(inv,r.inventory());assertEquals(gold,r.gold().longValue());assertEquals(exp,CampaignProgress.earnedExp(r));assertEquals(quest,r.campaign().snapshot().toString());
    }
  }
  @Test public void elementRewardsActuallyEquipAndFallbackShopsSellAllFourPairs()throws Exception{
    RpgProgressionState r=reportReady("C04","MAGE");assertTrue(r.campaign().claim("C04",r));
    assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip("IT_NECK_FIRE_PEARL"));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip("IT_BELT_FIRE_LEATHER"));
    assertEquals("화염",r.itemDefinitions().get(r.equipment().get("목걸이")).attackElement);assertEquals("화염",r.itemDefinitions().get(r.equipment().get("벨트")).defenseElement);
    for(String element:new String[]{"WATER","WIND","EARTH","FIRE"})for(String id:new String[]{"IT_NECK_"+element+"_PEARL","IT_BELT_"+element+"_LEATHER"})assertNotNull(TownCommerce.find(com.projectdark.mobile.world.TownInteriorDef.Kind.EQUIPMENT,id));
  }
  @Test public void journalShowsExactRingAndNamedEquipmentRatherThanOnlyExpGold()throws Exception{
    RpgProgressionState r=reportReady("D03","WARRIOR");QuestJournalModel model=new QuestJournalModel(context);
    boolean found=false;for(QuestJournalModel.Row row:model.rows(F5mAdaptedPrologueQuest.openingFixture(),new GrowthQuest2(),true,r))if(row.id.equals("CAMPAIGN_D03")){found=true;assertTrue(row.reward.contains("세줄금반지"));assertTrue(row.reward.contains("10,000"));}
    assertTrue(found);assertTrue(AdaptedPoteQuestRewards.equipmentText(CampaignProgress.find("B05"),r).contains("이름 미확인 신발"));
  }
  @Test public void spiritMobileRewardNoLongerDominatesSameHpPeersAndSourceStaysIntact(){
    AdaptedCampaignRewardCatalog c=new AdaptedCampaignRewardCatalog();assertTrue(c.find("POTE_SPIRIT").exp<=c.find("POTE_WOLFRIDER").exp*2);
    assertEquals(Integer.valueOf(308950),new CanonicalMonsterRewardCatalog().find("POTE_SPIRIT").exp);assertTrue(new CanonicalMonsterRewardCatalog().hasNoInventedDropEmission());
  }
  @Test @GraphicsMode(GraphicsMode.Mode.NATIVE) public void renderNamedDepartureTrainingAndBossRewards()throws Exception{
    java.io.File dir=new java.io.File("build/reports/pote-rewards-v123");assertTrue(dir.mkdirs()||dir.isDirectory());
    for(String id:new String[]{"M06","B06","D03"}){
      RpgProgressionState r=reportReady(id,"WARRIOR");QuestJournalModel model=new QuestJournalModel(context);
      List<QuestJournalModel.Row> rows=model.rows(F5mAdaptedPrologueQuest.openingFixture(),new GrowthQuest2(),true,r);
      QuestJournalModel.Row chosen=null;for(QuestJournalModel.Row row:rows)if(row.id.equals("CAMPAIGN_"+id))chosen=row;
      assertNotNull(chosen);QuestJournalWindow window=new QuestJournalWindow();window.open(rows,chosen);
      android.graphics.Bitmap b=android.graphics.Bitmap.createBitmap(960,540,android.graphics.Bitmap.Config.ARGB_8888);
      window.draw(new android.graphics.Canvas(b),rows,0);
      try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(dir,id+".png"))){assertTrue(b.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out));}b.recycle();
    }
  }
}
