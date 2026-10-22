package com.projectdark.mobile;

import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public class WornEquipmentV225Test {
  @Before public void reset(){android.content.Context c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  private Set<String> ids(String species){Set<String> out=new HashSet<>();for(WornEquipmentDropsV225.Drop d:WornEquipmentDropsV225.forSpecies(species))out.add(d.itemId);return out;}
  @Test public void identifiedKitsNeverMixCasterGenderOrPriestRobes(){
    assertEquals(Collections.singleton("USER_ITEM_20261018_R01487"),ids("MINE_07"));
    assertEquals(new HashSet<>(Arrays.asList("USER_ITEM_20261018_R01489","USER_ITEM_20261018_R01488")),ids("MINE_09"));
    assertTrue(ids("MINE_05").contains("USER_ITEM_20261018_R01485"));assertTrue(ids("MINE_06").contains("USER_ITEM_20261018_R01493"));
    assertEquals(Collections.singleton("USER_ITEM_20261018_R05210"),ids("MINE_03"));
    assertFalse(ids("HORROR_05_0").contains("USER_ITEM_20261018_R01489"));
    for(int e=0;e<4;e++)assertEquals(ids("HORROR_04_0"),ids("HORROR_04_"+e));
    assertTrue(ids("HORROR_01_0").isEmpty()); // Unknown female armor is never replaced with male armor.
    for(String id:Arrays.asList("DEATH_WERERAT","POTE_GNOLL","ABEL_TURTLE","MINE_04","HORROR_04_9","MINE_99"))assertTrue(id,ids(id).isEmpty());
  }
  @Test public void dropsUseExistingCanonicalItemsAndTemporaryRatesWithoutInventedStats(){
    RpgProgressionState r=new RpgProgressionState();int n=0;
    for(Map.Entry<String,List<WornEquipmentDropsV225.Drop>> kit:WornEquipmentDropsV225.entries().entrySet()){
      Set<String> slots=new HashSet<>();for(WornEquipmentDropsV225.Drop d:kit.getValue()){
        assertNotNull(d.itemId,r.itemDefinitions().get(d.itemId));assertEquals(d.slot,r.itemDefinitions().get(d.itemId).equipSlot);
        assertTrue(kit.getKey()+" duplicate slot",slots.add(d.slot));assertNull(d.originalProbability);assertTrue(d.rate==200||d.rate==350);n++;
      }
    }
    assertEquals(23,n);
  }
  @Test public void realDefeatsAwardWholeIdentifiedKitsOnceAndPersist(){
    RuntimeState s=new RuntimeState();s.enterCampaignMap(com.projectdark.mobile.world.MineHuntGeometry.ID,false);
    Set<String> expected=new HashSet<>();for(int t:new int[]{3,5,6,7,8,9})expected.addAll(ids(MineHuntMonsters.species(t)));
    Set<String> received=new HashSet<>();long gold=s.rpg().gold();long countGold=0;
    for(RuntimeState.Monster m:s.monsters()){
      if(!expected.containsAll(ids(m.campaignRewardProfileId))||ids(m.campaignRewardProfileId).isEmpty())continue;
      for(int k=0;k<1800&&!received.containsAll(ids(m.campaignRewardProfileId));k++){
        m.alive=true;m.hp=m.maxHp;s.damage(m,m.maxHp);s.rpg().consumeCombat(s.ledger().snapshot(),s);
        countGold+=new AdaptedCampaignRewardCatalog().find(m.campaignRewardProfileId).gold;
        RpgProgressionState.RewardResolution receipt=s.rpg().rewardHistory().get(s.rpg().rewardHistory().size()-1);
        assertEquals(WornEquipmentDropsV225.POLICY,receipt.policyId);assertTrue(ids(m.campaignRewardProfileId).containsAll(receipt.autoLootedItems.keySet()));received.addAll(receipt.autoLootedItems.keySet());
      }
    }
    assertEquals(expected,received);assertEquals(gold+countGold,s.rpg().gold().longValue());
    Map<String,Integer> owned=new LinkedHashMap<>(s.rpg().inventory());long earned=CampaignProgress.earnedExp(s.rpg());
    s.rpg().consumeCombat(s.ledger().snapshot(),s);assertEquals(owned,s.rpg().inventory());assertEquals(gold+countGold,s.rpg().gold().longValue());
    F5mSaveStore.saveRewardsActive(s.rpg());F5mSaveStore.install(RuntimeEnvironment.getApplication());RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);
    assertEquals(owned,restored.inventory());assertEquals(earned,CampaignProgress.earnedExp(restored));assertEquals(s.rpg().gold(),restored.gold());
  }
  @Test public void horrorElementsUseAdmittedActorsAndOtherRegionsKeepTheirRewards(){
    RuntimeState s=new RuntimeState();s.enterCampaignMap("MAP_HORROR_ROOM_01",false);Set<String> checked=new HashSet<>();
    for(RuntimeState.Monster m:s.monsters()){
      if(ids(m.campaignRewardProfileId).isEmpty())continue;
      for(int k=0;k<1200;k++){
        m.alive=true;m.hp=m.maxHp;s.damage(m,m.maxHp);s.rpg().consumeCombat(s.ledger().snapshot(),s);
        RpgProgressionState.RewardResolution receipt=s.rpg().rewardHistory().get(s.rpg().rewardHistory().size()-1);
        assertTrue(ids(m.campaignRewardProfileId).containsAll(receipt.autoLootedItems.keySet()));
        checked.addAll(receipt.autoLootedItems.keySet());
        if(checked.containsAll(ids(m.campaignRewardProfileId)))break;
      }
    }
    assertFalse("real Horror actor drops must be reachable",checked.isEmpty());
    CombatLedger forged=new CombatLedger();forged.add(CombatLedger.Type.MONSTER_DEFEATED,"player","MINE_06#forged",0);RpgProgressionState r=new RpgProgressionState();Map<String,Integer> before=new LinkedHashMap<>(r.inventory());r.consumeCombat(forged.snapshot(),s);assertEquals(before,r.inventory());
  }
}
