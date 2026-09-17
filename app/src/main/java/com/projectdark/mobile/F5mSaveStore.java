package com.projectdark.mobile;

import android.content.Context;
import android.content.SharedPreferences;

/** F5M-027/028 durable save boundary for the playable adapted opening slice. */
public final class F5mSaveStore {
  private static final String PREF="project_dark_f5m_v1";
  private static final String K_STATE="quest_state",K_COUNT="quest_count",K_TOKEN="training_token_qty";
  private static F5mSaveStore active;
  private final SharedPreferences prefs;
  private F5mSaveStore(Context context){prefs=context.getApplicationContext().getSharedPreferences(PREF,Context.MODE_PRIVATE);}
  public static void install(Context context){active=new F5mSaveStore(context);}
  public static boolean installed(){return active!=null;}
  public static void restoreActive(F5mAdaptedPrologueQuest quest,RpgProgressionState rpg){if(active!=null)active.restore(quest,rpg);}
  public static void saveActive(F5mAdaptedPrologueQuest quest,RpgProgressionState rpg){if(active!=null)active.save(quest,rpg);}
  private void restore(F5mAdaptedPrologueQuest quest,RpgProgressionState rpg){
    String raw=prefs.getString(K_STATE,null);
    if(raw!=null){try{quest.restore(F5mAdaptedPrologueQuest.State.valueOf(raw),prefs.getInt(K_COUNT,0));}catch(IllegalArgumentException ignored){quest.restore(F5mAdaptedPrologueQuest.State.AVAILABLE,0);}}
    int desired=Math.max(0,prefs.getInt(K_TOKEN,0)),current=quantity(rpg,AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID);
    if(desired>current)rpg.autoLootResolvedItem(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID,desired-current);
  }
  private void save(F5mAdaptedPrologueQuest quest,RpgProgressionState rpg){prefs.edit().putString(K_STATE,quest.state().name()).putInt(K_COUNT,quest.currentCount()).putInt(K_TOKEN,quantity(rpg,AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID)).apply();}
  private static int quantity(RpgProgressionState rpg,String itemId){Integer q=rpg.inventory().get(itemId);return q==null?0:q;}
}
