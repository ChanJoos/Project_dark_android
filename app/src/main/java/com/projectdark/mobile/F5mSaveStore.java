package com.projectdark.mobile;
import android.content.Context;
import android.content.SharedPreferences;
/** F5M-024/027/028 durable save boundary for the playable adapted opening slice. */
public final class F5mSaveStore{
 private static final String PREF="project_dark_f5m_v1",K_STATE="quest_state",K_COUNT="quest_count",K_TOKEN="training_token_qty",K_REWARD_CLAIMED="quest_reward_claimed";
 private static F5mSaveStore active;private final SharedPreferences prefs;
 private F5mSaveStore(Context c){prefs=c.getApplicationContext().getSharedPreferences(PREF,Context.MODE_PRIVATE);} public static void install(Context c){active=new F5mSaveStore(c);} public static boolean installed(){return active!=null;}
 public static void restoreQuestActive(F5mAdaptedPrologueQuest q){if(active!=null)active.restoreQuest(q);} public static void saveQuestActive(F5mAdaptedPrologueQuest q){if(active!=null)active.saveQuest(q);}
 public static void restoreRewardsActive(RpgProgressionState r){if(active!=null)active.restoreRewards(r);} public static void saveRewardsActive(RpgProgressionState r){if(active!=null)active.saveRewards(r);}
 public static boolean rewardClaimedActive(){return active!=null&&active.prefs.getBoolean(K_REWARD_CLAIMED,false);}
 /** Persist reward claim + quantity + COMPLETED together. Call only after runtime inventory accepted the reward. */
 public static boolean commitTurnInActive(F5mAdaptedPrologueQuest q,RpgProgressionState r){if(active==null||q==null||r==null)return false;if(q.state()!=F5mAdaptedPrologueQuest.State.RETURN_READY)return q.state()==F5mAdaptedPrologueQuest.State.COMPLETED&&rewardClaimedActive();int qty=qty(r);boolean ok=active.prefs.edit().putBoolean(K_REWARD_CLAIMED,true).putInt(K_TOKEN,qty).putString(K_STATE,F5mAdaptedPrologueQuest.State.COMPLETED.name()).putInt(K_COUNT,q.currentCount()).commit();if(ok)q.restore(F5mAdaptedPrologueQuest.State.COMPLETED,q.currentCount());return ok;}
 private void restoreQuest(F5mAdaptedPrologueQuest q){String raw=prefs.getString(K_STATE,null);if(raw==null)return;try{q.restore(F5mAdaptedPrologueQuest.State.valueOf(raw),prefs.getInt(K_COUNT,0));}catch(IllegalArgumentException ignored){q.restore(F5mAdaptedPrologueQuest.State.AVAILABLE,0);}}
 private void saveQuest(F5mAdaptedPrologueQuest q){prefs.edit().putString(K_STATE,q.state().name()).putInt(K_COUNT,q.currentCount()).apply();}
 private void restoreRewards(RpgProgressionState r){int desired=Math.max(0,prefs.getInt(K_TOKEN,0)),current=qty(r);if(desired>current)r.autoLootResolvedItem(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID,desired-current);}
 private void saveRewards(RpgProgressionState r){prefs.edit().putInt(K_TOKEN,qty(r)).apply();}
 private static int qty(RpgProgressionState r){Integer q=r.inventory().get(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID);return q==null?0:q;}
}
