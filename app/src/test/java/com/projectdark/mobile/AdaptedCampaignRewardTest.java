package com.projectdark.mobile;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public final class AdaptedCampaignRewardTest {
  @Test public void admittedFieldActorsReceiveSpeciesSpecificAdaptedExpAndGold(){
    RuntimeState state=new RuntimeState();state.enterPoteField();
    RuntimeState.Monster purple=find(state,"POTE_PURPLE");
    long before=state.rpg().normalExp();long gold=state.rpg().gold();
    state.ledger().add(CombatLedger.Type.MONSTER_DEFEATED,"player",purple.id,0);
    state.rpg().consumeCombat(state.ledger().snapshot(),state);
    RpgProgressionState.RewardResolution result=state.rpg().rewardHistory().get(0);
    assertEquals(Integer.valueOf(9000),result.exp);assertEquals(gold+20,state.rpg().gold().longValue());
    assertEquals(before+9000,state.rpg().normalExp().longValue());
    assertEquals(AdaptedCampaignRewardCatalog.POLICY_ID,result.policyId);
    assertEquals(RpgProgressionState.RewardSource.ADAPTED_CAMPAIGN,result.source);
  }
  @Test public void unprofiledCanonicalPrototypeStillFailsClosed(){
    RpgProgressionState r=new RpgProgressionState();long before=r.normalExp();java.util.Map<String,Integer> inventoryBefore=new java.util.LinkedHashMap<>(r.inventory());
    CombatLedger ledger=new CombatLedger();ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player","POTE_PURPLE",0);
    r.consumeCombat(ledger.snapshot(),null);
    RpgProgressionState.RewardResolution result=r.rewardHistory().get(0);
    assertEquals(RpgProgressionState.RewardStatus.PENDING_NO_CANONICAL_MONSTER_REWARD,result.status);
    assertNull(result.exp);assertTrue(result.autoLootedItems.isEmpty());
    assertEquals(before,r.normalExp().longValue());assertEquals(inventoryBefore,r.inventory());
  }
  @Test public void campaignHasPerSpeciesProfilesAndDoesNotReplaceSpiritCanon(){
    AdaptedCampaignRewardCatalog c=new AdaptedCampaignRewardCatalog();
    assertEquals(18,c.entries().size());assertEquals(40000,c.find("POTE_SPIRIT").exp);assertEquals(Integer.valueOf(308950),new CanonicalMonsterRewardCatalog().find("POTE_SPIRIT").exp);
    assertNotEquals(c.find("POTE_RED").exp,c.find("POTE_SILVER").exp);
  }
  @Test public void admittedSpiritInstancesResolveAdaptedExpExactlyOnceWithoutInventedDrops(){
    RuntimeState state=new RuntimeState();state.enterCampaignMap("MAP_POTE_03",false);
    RuntimeState.Monster spirit=null;for(RuntimeState.Monster m:state.monsters())if(m.id.startsWith("POTE_SPIRIT#")){spirit=m;break;}
    assertNotNull(spirit);long before=state.rpg().normalExp();long gold=state.rpg().gold();
    java.util.Map<String,Integer> inventory=new java.util.LinkedHashMap<>(state.rpg().inventory());
    state.ledger().add(CombatLedger.Type.MONSTER_DEFEATED,"player",spirit.id,0);
    state.rpg().consumeCombat(state.ledger().snapshot(),state);
    RpgProgressionState.RewardResolution result=state.rpg().rewardHistory().get(0);
    assertEquals(RpgProgressionState.RewardSource.ADAPTED_CAMPAIGN,result.source);
    assertEquals(Integer.valueOf(40000),result.exp);assertEquals(before+40000,state.rpg().normalExp().longValue());
    assertEquals(gold+82,state.rpg().gold().longValue());assertEquals(inventory,state.rpg().inventory());
    state.rpg().consumeCombat(state.ledger().snapshot(),state);assertEquals(1,state.rpg().rewardHistory().size());
    RpgProgressionState unbound=new RpgProgressionState();unbound.consumeCombat(state.ledger().snapshot(),null);
    assertEquals(RpgProgressionState.RewardStatus.PENDING_NO_CANONICAL_MONSTER_REWARD,unbound.rewardHistory().get(0).status);
  }
  @Test public void firstJobChoiceIsLevelGatedOneTimeAndGrantsRoleGear(){
    RpgProgressionState r=new RpgProgressionState();
    assertFalse(r.chooseInitialJob("WARRIOR"));
    r.grantAdaptedReward(22800,0);
    assertEquals(Integer.valueOf(3),r.normalLevel());
    assertTrue(r.chooseInitialJob("MARTIAL_ARTIST"));
    assertEquals("MARTIAL_ARTIST",r.currentJobCode());
    assertEquals("IT_B_JOB_MONK_GI",r.equipment().get(RpgProgressionState.ARMOR_SLOT));
    assertFalse(r.chooseInitialJob("MAGE"));
  }
  @Test public void firstJobChoiceSurvivesRestartWithoutRegranting(){
    android.content.Context context=org.robolectric.RuntimeEnvironment.getApplication();
    context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);
    RpgProgressionState r=new RpgProgressionState();r.grantAdaptedReward(22800,0);assertTrue(r.chooseInitialJob("CLERIC"));
    F5mSaveStore.saveRewardsActive(r);F5mSaveStore.install(context);
    RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);
    assertTrue(F5mSaveStore.writable());assertEquals("CLERIC",restored.currentJobCode());
    assertEquals(r.equipment(),restored.equipment());assertFalse(restored.chooseInitialJob("WARRIOR"));
  }
  @Test public void jobCounselorIsAReachableNewTownNpc(){
    RuntimeState state=new RuntimeState();RuntimeState.Npc counselor=null;
    for(RuntimeState.Npc npc:state.npcs())if("milles_job_counselor".equals(npc.id))counselor=npc;
    assertNotNull(counselor);assertEquals("마이클",NpcIdentity.forId(counselor.id).name);
    assertFalse(state.blocked(counselor.x,counselor.y));
    assertNotNull(state.nearestMonsterTileCenter(counselor.x,counselor.y));
  }
  @Test public void eachInitialJobGetsItsOwnWearableAndUsableWeaponPolicy(){
    String[][] expected={{"WARRIOR","IT_B_JOB_WARRIOR_TUNIC","IT_B_JOB_WARRIOR_WEAPON"},{"ROGUE","IT_B_JOB_ROGUE_GARMENT","IT_B_JOB_ROGUE_WEAPON"},{"MAGE","IT_B_JOB_MAGE_ROBE","IT_B_JOB_MAGE_WAND"},{"CLERIC","IT_B_JOB_CLERIC_ROBE","IT_B_JOB_CLERIC_WAND"},{"MARTIAL_ARTIST","IT_B_JOB_MONK_GI",null}};
    for(String[] row:expected){RpgProgressionState r=new RpgProgressionState();r.grantAdaptedReward(22800,0);assertTrue(r.chooseInitialJob(row[0]));String armor=r.equipment().get(RpgProgressionState.ARMOR_SLOT);assertEquals(row[1],armor);assertTrue(r.itemDefinitions().get(armor).allowedJobCodes.contains(row[0]));if(row[2]==null)assertFalse(r.equipment().containsKey(RpgProgressionState.WEAPON_SLOT));else{assertEquals(row[2],r.equipment().get(RpgProgressionState.WEAPON_SLOT));assertTrue(r.itemDefinitions().get(row[2]).allowedJobCodes.contains(row[0]));}}
  }
  private RuntimeState.Monster find(RuntimeState r,String id){for(RuntimeState.Monster m:r.monsters())if(id.equals(m.id))return m;throw new AssertionError(id);}
}
