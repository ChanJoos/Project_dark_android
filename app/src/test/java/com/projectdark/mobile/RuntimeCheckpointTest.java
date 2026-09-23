package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
public class RuntimeCheckpointTest {
  private Context context;
  private SharedPreferences prefs;
  @Before public void reset(){context=RuntimeEnvironment.getApplication();prefs=context.getSharedPreferences("project_dark_f5m_v1",0);prefs.edit().clear().commit();F5mSaveStore.install(context);}
  private RuntimeState stateOf(GameView view)throws Exception{Field f=GameView.class.getDeclaredField("state");f.setAccessible(true);return (RuntimeState)f.get(view);}
  private RpgProgressionState restartRpg(){F5mSaveStore.install(context);RpgProgressionState r=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(r);return r;}

  @Test public void allItemsAndExplicitUnequipSurviveRestart(){
    RpgProgressionState r=new RpgProgressionState();r.autoLootResolvedItem("IT_GLOVE_LEATHER",4);
    for(String id:new ArrayList<>(r.equipment().values()))r.equip(id);
    assertTrue(r.equipment().isEmpty());F5mSaveStore.saveRewardsActive(r);
    RpgProgressionState restored=restartRpg();assertEquals(r.inventory(),restored.inventory());assertTrue(restored.equipment().isEmpty());
  }
  @Test public void emptyInventoryDoesNotRecreateStarterItems(){
    RpgProgressionState r=new RpgProgressionState();assertTrue(r.restoreOwnedItems(Collections.emptyMap(),Collections.emptyMap()));
    F5mSaveStore.saveRewardsActive(r);assertTrue(restartRpg().inventory().isEmpty());
  }
  @Test public void legacySaveMigratesWithoutReequippingEmptySlot(){
    prefs.edit().putInt("training_token_qty",7).putString("equipment_ids","").putLong("gold",45).commit();
    RpgProgressionState r=restartRpg();assertEquals(Integer.valueOf(7),r.inventory().get(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID));assertTrue(r.equipment().isEmpty());assertEquals(Long.valueOf(45),r.gold());
    F5mSaveStore.saveRewardsActive(r);assertEquals(3,prefs.getInt("save_schema",0));assertEquals(r.inventory(),restartRpg().inventory());
  }
  @Test public void unsupportedOrUnknownItemsCannotOverwriteSave(){
    prefs.edit().putInt("save_schema",99).putLong("gold",987).commit();restartRpg();F5mSaveStore.saveRewardsActive(new RpgProgressionState());assertEquals(99,prefs.getInt("save_schema",0));assertEquals(987,prefs.getLong("gold",0));
    prefs.edit().putInt("save_schema",2).putString("inventory_v2","{\"REMOVED_ITEM\":2}").putString("equipment_v2","{}").commit();restartRpg();assertFalse(F5mSaveStore.writable());F5mSaveStore.saveRewardsActive(new RpgProgressionState());assertTrue(prefs.getString("inventory_v2","").contains("REMOVED_ITEM"));
  }
  @Test public void liveGameViewPauseRestoresPositionResourcesAndOwnership()throws Exception{
    GameView first=new GameView(context);RuntimeState r=stateOf(first);
    // Use an actual traversable authored tile distinct from spawn.
    com.projectdark.mobile.world.WorldRuntimeAdapter world=new com.projectdark.mobile.world.WorldRuntimeAdapter(r,960,540);
    for(com.projectdark.mobile.world.WorldMoveTargetController.TileCenter tile:world.navigationTiles())if(world.canPlayerOccupy(tile.x,tile.y)&&Math.abs(tile.x-r.player().x)>64){r.player().x=tile.x;r.player().y=tile.y;break;}
    float x=r.player().x,y=r.player().y;r.player().hp=31;r.player().mp=17;r.rpg().autoLootResolvedItem("IT_GLOVE_LEATHER",3);
    for(String id:new ArrayList<>(r.rpg().equipment().values()))r.rpg().equip(id);
    first.pause();F5mSaveStore.install(context);RuntimeState restored=stateOf(new GameView(context));
    assertEquals(x,restored.player().x,0.001f);assertEquals(y,restored.player().y,0.001f);assertEquals(31,restored.player().hp);assertEquals(17,restored.player().mp);assertEquals(r.rpg().inventory(),restored.rpg().inventory());assertTrue(restored.rpg().equipment().isEmpty());
  }
  @Test public void deadPlayerRestartsDeadUntilExplicitRevive()throws Exception{
    GameView view=new GameView(context);RuntimeState r=stateOf(view);r.damagePlayer(99999);view.pause();F5mSaveStore.install(context);RuntimeState restored=stateOf(new GameView(context));assertFalse(restored.player().alive);assertEquals(0,restored.player().hp);restored.revivePlayer();assertTrue(restored.player().alive);
  }
  @Test public void defeatImmediatelyBeforePauseIsRewardedOnceAndNextLifeWorks()throws Exception{
    GameView view=new GameView(context);RuntimeState r=stateOf(view);RuntimeState.Monster m=r.monsters().get(0);r.damage(m,m.maxHp);CombatLedger.Event old=r.ledger().latest();view.pause();
    long exp=r.rpg().normalExp(),gold=r.rpg().gold();assertTrue(gold>0);
    F5mSaveStore.install(context);RuntimeState restored=stateOf(new GameView(context));
    assertEquals(Long.valueOf(exp),restored.rpg().normalExp());assertEquals(Long.valueOf(gold),restored.rpg().gold());
    restored.rpg().consumeCombat(Collections.singletonList(old),restored);assertEquals(Long.valueOf(gold),restored.rpg().gold());
    RuntimeState.Monster next=restored.monsters().get(0);restored.damage(next,next.maxHp);assertTrue(restored.ledger().latest().sequence>old.sequence);restored.tick(0);assertTrue(restored.rpg().gold()>gold);
  }
  @Test public void quest2CompletionAndAllGrowthCommitTogether(){
    RpgProgressionState r=new RpgProgressionState();r.restoreStats(5,4,8,9,3,6);GrowthQuest2 q=new GrowthQuest2();q.restore(GrowthQuest2.State.RETURN_READY,3);
    assertTrue(q.turnIn(r));RpgProgressionState restored=restartRpg();GrowthQuest2 rq=new GrowthQuest2();F5mSaveStore.restoreQuest2Active(rq);
    assertEquals(GrowthQuest2.State.COMPLETED,rq.state());assertEquals(r.gold(),restored.gold());assertEquals(r.normalLevel(),restored.normalLevel());assertEquals(r.statPoints(),restored.statPoints());assertEquals(r.baseMaxHp(),restored.baseMaxHp());assertEquals(r.baseMaxMp(),restored.baseMaxMp());assertFalse(rq.turnIn(restored));
  }
  @Test public void prologueTurnInDoesNotReloadAndDiscardLiveStats(){
    RpgProgressionState r=new RpgProgressionState();F5mSaveStore.saveRewardsActive(r);r.restoreStats(7,5,9,11,4,8);r.autoLootResolvedItem("IT_GLOVE_LEATHER",2);
    F5mAdaptedPrologueQuest q=new F5mAdaptedPrologueQuest(F5mAdaptedPrologueQuest.OPENING_MONSTER_ID);q.restore(F5mAdaptedPrologueQuest.State.RETURN_READY,1);
    assertEquals(F5mTurnInCoordinator.Result.COMPLETED,F5mTurnInCoordinator.complete(q,r));RpgProgressionState restored=restartRpg();assertEquals(11,restored.con());assertEquals(r.statPoints(),restored.statPoints());assertEquals(r.baseMaxHp(),restored.baseMaxHp());assertEquals(r.inventory(),restored.inventory());
  }
  @Test public void rejectedQuestCommitDoesNotMutateLiveReward(){
    prefs.edit().putInt("save_schema",99).commit();F5mSaveStore.install(context);
    GrowthQuest2 q=new GrowthQuest2();q.restore(GrowthQuest2.State.RETURN_READY,3);RpgProgressionState r=new RpgProgressionState();assertFalse(q.turnIn(r));assertEquals(Long.valueOf(0),r.gold());assertEquals(GrowthQuest2.State.RETURN_READY,q.state());
  }
  @Test public void failedDiskCommitCanRetryWithoutDuplicatingReward(){
    final boolean[] fail={true};
    SharedPreferences failing=(SharedPreferences)java.lang.reflect.Proxy.newProxyInstance(SharedPreferences.class.getClassLoader(),new Class<?>[]{SharedPreferences.class},(proxy,method,args)->{
      if(!method.getName().equals("edit"))return method.invoke(prefs,args);
      SharedPreferences.Editor delegate=prefs.edit();
      return java.lang.reflect.Proxy.newProxyInstance(SharedPreferences.Editor.class.getClassLoader(),new Class<?>[]{SharedPreferences.Editor.class},(editor,m,a)->{
        if(m.getName().equals("commit")&&fail[0]){fail[0]=false;return false;}
        Object result=m.invoke(delegate,a);return result==delegate?editor:result;
      });
    });
    Context injected=new android.content.ContextWrapper(context){
      @Override public Context getApplicationContext(){return this;}
      @Override public SharedPreferences getSharedPreferences(String name,int mode){return failing;}
    };
    F5mSaveStore.install(injected);GrowthQuest2 q=new GrowthQuest2();q.restore(GrowthQuest2.State.RETURN_READY,3);RpgProgressionState r=new RpgProgressionState();
    assertFalse(q.turnIn(r));assertEquals(Long.valueOf(0),r.gold());assertEquals(GrowthQuest2.State.RETURN_READY,q.state());
    assertTrue(q.turnIn(r));assertEquals(Long.valueOf(250),r.gold());assertFalse(q.turnIn(r));assertEquals(Long.valueOf(250),restartRpg().gold());
  }
  @Test public void quest2WatermarkRejectsOldDefeatAfterRestart(){
    GrowthQuest2 q=new GrowthQuest2();q.restore(GrowthQuest2.State.ACTIVE,0);CombatLedger ledger=new CombatLedger();ledger.restoreSequence(40);ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player","combat_dummy_01",0);CombatLedger.Event old=ledger.latest();assertTrue(q.consume(old));
    F5mSaveStore.install(context);GrowthQuest2 restored=new GrowthQuest2();F5mSaveStore.restoreQuest2Active(restored);assertFalse(restored.consume(old));assertEquals(1,restored.currentCount());ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player","combat_dummy_01",0);assertTrue(restored.consume(ledger.latest()));assertEquals(2,restored.currentCount());
  }

}
