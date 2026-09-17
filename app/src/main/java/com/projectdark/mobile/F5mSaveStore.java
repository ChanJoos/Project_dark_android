package com.projectdark.mobile;

import android.content.Context;
import android.content.SharedPreferences;

/** F5M-027/028 minimal durable save boundary for the currently playable adapted opening slice. */
public final class F5mSaveStore {
  private static final String PREF="project_dark_f5m_v1";
  private static final String K_STATE="quest_state",K_COUNT="quest_count",K_TOKEN="training_token_qty";
  private final SharedPreferences prefs;
  public F5mSaveStore(Context context){prefs=context.getSharedPreferences(PREF,Context.MODE_PRIVATE);}

  public void restore(F5mAdaptedPrologueQuest quest,RpgProgressionState rpg){
    String raw=prefs.getString(K_STATE,null);
    if(raw!=null){
      try{quest.restore(F5mAdaptedPrologueQuest.State.valueOf(raw),prefs.getInt(K_COUNT,0));}
      catch(IllegalArgumentException ignored){quest.restore(F5mAdaptedPrologueQuest.State.AVAILABLE,0);}
    }
    int desired=Math.max(0,prefs.getInt(K_TOKEN,0));
    int current=quantity(rpg,AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID);
    if(desired>current)rpg.autoLootResolvedItem(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID,desired-current);
  }

  public void save(F5mAdaptedPrologueQuest quest,RpgProgressionState rpg){
    prefs.edit().putString(K_STATE,quest.state().name()).putInt(K_COUNT,quest.currentCount())
        .putInt(K_TOKEN,quantity(rpg,AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID)).apply();
  }

  private static int quantity(RpgProgressionState rpg,String itemId){Integer q=rpg.inventory().get(itemId);return q==null?0:q;}
}
