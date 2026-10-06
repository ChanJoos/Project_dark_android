package com.projectdark.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONObject;

/** Versioned, single-editor checkpoints over the existing app-private save slot. */
public final class F5mSaveStore {
  private static final String PREF="project_dark_f5m_v1";
  private static final int SCHEMA=4;
  private static F5mSaveStore active;
  private final SharedPreferences prefs;
  private RuntimeState runtime;
  private SkillBook skillBook;
  private F5mAdaptedPrologueQuest quest;
  private GrowthQuest2 quest2;
  private boolean inFrame,writable=true;

  private F5mSaveStore(Context c){prefs=c.getApplicationContext().getSharedPreferences(PREF,Context.MODE_PRIVATE);writable=prefs.getInt("save_schema",1)<=SCHEMA;}
  public static void install(Context c){active=new F5mSaveStore(c);}
  public static boolean installed(){return active!=null;}
  public static boolean writable(){return active==null||active.writable;}
  public static boolean rewardClaimedActive(){return active!=null&&active.prefs.getBoolean("quest_reward_claimed",false);}
  /** Only maps with a complete runtime adapter may be restored at startup. */
  public static String savedMapIdActive(){
    if(active==null||!active.writable)return WorldDef.ID;
    String map=active.prefs.getString("map_id",WorldDef.ID);
    if(com.projectdark.mobile.world.TownInteriorDef.forMap(map)!=null||WorldDef.ID.equals(map)||com.projectdark.mobile.world.CampaignWorld.contains(map))return map;
    active.writable=false;
    return WorldDef.ID;
  }
  public static float[] savedFieldReturnPointActive(){
    if(active==null||!active.writable)return new float[]{WorldDef.PLAYER_SPAWN_X,WorldDef.PLAYER_SPAWN_Y};
    float x=active.prefs.getFloat("field_return_x",WorldDef.PLAYER_SPAWN_X);
    float y=active.prefs.getFloat("field_return_y",WorldDef.PLAYER_SPAWN_Y);
    if(!Float.isFinite(x)||!Float.isFinite(y)||x<WorldDef.MIN_X||x>WorldDef.MAX_X||y<WorldDef.MIN_Y||y>WorldDef.MAX_Y)
      return new float[]{WorldDef.PLAYER_SPAWN_X,WorldDef.PLAYER_SPAWN_Y};
    return new float[]{x,y};
  }
  public static void saveFieldReturnPointActive(float x,float y){
    if(active==null||!active.writable)return;
    active.prefs.edit().putFloat("field_return_x",x).putFloat("field_return_y",y).commit();
  }
  public static void restoreQuestActive(F5mAdaptedPrologueQuest q){if(active!=null&&active.writable)active.restoreQuest(q);}
  public static void restoreQuest2Active(GrowthQuest2 q){if(active!=null&&active.writable)active.restoreQuest2(q);}
  public static void restoreRewardsActive(RpgProgressionState r){if(active!=null&&active.writable)active.restoreRpg(r);}
  public static void saveQuestActive(F5mAdaptedPrologueQuest q){
    if(active==null||!active.writable||active.inFrame)return;
    if(active.runtime!=null){active.checkpoint();return;}
    active.writeQuest(active.prefs.edit(),q).commit();
  }
  public static void saveQuest2Active(GrowthQuest2 q){
    if(active==null||!active.writable||active.inFrame)return;
    if(active.runtime!=null){active.checkpoint();return;}
    active.writeQuest2(active.prefs.edit(),q).commit();
  }
  public static void saveRewardsActive(RpgProgressionState r){
    if(active==null||!active.writable||active.inFrame)return;
    if(active.runtime!=null){active.checkpoint();return;}
    active.writeRpg(active.prefs.edit(),r).commit();
  }

  /** Bind only after restore; frame consumers finish before any defeat checkpoint is written. */
  public static void bindRuntime(RuntimeState r,F5mAdaptedPrologueQuest q,GrowthQuest2 q2){if(active!=null){active.runtime=r;active.quest=q;active.quest2=q2;}}
  public static void restoreAndBindSkillsActive(SkillBook book){
    if(active==null)return;
    active.skillBook=book;
    if(!active.writable||!active.prefs.contains("skill_book_v1"))return;
    try{if(!book.restore(new JSONObject(active.prefs.getString("skill_book_v1",""))))active.writable=false;}
    catch(Exception e){active.writable=false;}
  }
  public static void beginFrame(){if(active!=null)active.inFrame=true;}
  public static void endFrame(){if(active!=null)active.inFrame=false;}
  public static boolean checkpointActive(){return active==null||active.checkpoint();}

  private boolean checkpoint(){
    if(!writable||runtime==null)return false;
    SharedPreferences.Editor edit=writeRpg(prefs.edit(),runtime.rpg());
    writeQuest(edit,quest);writeQuest2(edit,quest2);writeRuntime(edit);
    if(skillBook!=null)edit.putString("skill_book_v1",skillBook.snapshot().toString());
    return edit.commit();
  }

  private void writeRuntime(SharedPreferences.Editor edit){
    RuntimeState.Player p=runtime.player();
    for(RuntimeState.Monster m:runtime.monsters())if("milles_mouse_proto".equals(m.id)){
      edit.putString("inn_mouse_map",runtime.currentMapId()).putInt("inn_mouse_hp",m.hp)
          .putFloat("inn_mouse_x",m.isMoving?m.moveTargetX:m.x).putFloat("inn_mouse_y",m.isMoving?m.moveTargetY:m.y);
    }
    edit.putString("map_id",runtime.currentMapId()).putFloat("player_x",p.x).putFloat("player_y",p.y)
        .putString("skill_effects_v1",runtime.skillEffects().snapshot().toString()).putInt("player_hp",p.alive?p.hp:0).putInt("player_mp",p.mp).putLong("ledger_sequence",runtime.ledger().sequence());
  }
  public static void restoreRuntimeActive(RuntimeState r){
    r.applyDerivedGrowth();
    if(active==null||!active.writable)return;
    SharedPreferences p=active.prefs;
    if(r.currentMapId().equals(p.getString("map_id",null))){
      float x=p.getFloat("player_x",r.player().x),y=p.getFloat("player_y",r.player().y);
      if(Float.isFinite(x)&&Float.isFinite(y)&&x>=RuntimeState.WORLD_MIN_X&&x<=RuntimeState.WORLD_MAX_X&&y>=RuntimeState.WORLD_MIN_Y&&y<=RuntimeState.WORLD_MAX_Y){r.player().x=x;r.player().y=y;}
    }
    if(r.currentMapId().equals(p.getString("inn_mouse_map",null)))for(RuntimeState.Monster m:r.monsters())if("milles_mouse_proto".equals(m.id)){
      int hp=p.getInt("inn_mouse_hp",m.hp);float x=p.getFloat("inn_mouse_x",m.x),y=p.getFloat("inn_mouse_y",m.y);
      if(hp>0){m.hp=Math.min(m.maxHp,hp);if(r.isMonsterTileCenter(x,y)&&!r.blocked(x,y)){m.x=x;m.y=y;}}
    }
    r.player().hp=Math.max(0,Math.min(r.player().maxHp,p.getInt("player_hp",r.player().hp)));
    r.player().mp=Math.max(0,Math.min(r.player().maxMp,p.getInt("player_mp",r.player().mp)));
    r.player().alive=r.player().hp>0;
    String effectJson=p.getString("skill_effects_v1",null);if(effectJson!=null)try{if(!r.skillEffects().restore(new org.json.JSONObject(effectJson)))active.writable=false;}catch(Exception ex){active.writable=false;}

    long watermark=Math.max(p.getLong("ledger_sequence",0),r.rpg().consumedCombatSequence());
    watermark=Math.max(watermark,Math.max(p.getLong("quest_sequence",0),p.getLong("quest2_sequence",0)));
    r.ledger().restoreSequence(watermark);
  }

  private SharedPreferences.Editor writeQuest(SharedPreferences.Editor edit,F5mAdaptedPrologueQuest q){
    return edit.putString("quest_state",q.state().name()).putInt("quest_count",q.currentCount()).putLong("quest_sequence",q.consumedSequence());
  }
  private SharedPreferences.Editor writeQuest2(SharedPreferences.Editor edit,GrowthQuest2 q){
    return edit.putString("quest2_state",q.state().name()).putInt("quest2_count",q.currentCount()).putLong("quest2_sequence",q.consumedSequence());
  }
  private void restoreQuest(F5mAdaptedPrologueQuest q){
    String raw=prefs.getString("quest_state",null);if(raw==null)return;
    try{q.restore(F5mAdaptedPrologueQuest.State.valueOf(raw),prefs.getInt("quest_count",0));q.restoreSequence(prefs.getLong("quest_sequence",0));}
    catch(IllegalArgumentException invalid){writable=false;}
  }
  private void restoreQuest2(GrowthQuest2 q){
    String raw=prefs.getString("quest2_state",null);if(raw==null)return;
    try{q.restore(GrowthQuest2.State.valueOf(raw),prefs.getInt("quest2_count",0));q.restoreSequence(prefs.getLong("quest2_sequence",0));}
    catch(IllegalArgumentException invalid){writable=false;}
  }

  private SharedPreferences.Editor writeRpg(SharedPreferences.Editor edit,RpgProgressionState r){
    return edit.putInt("save_schema",SCHEMA)
        // A save authored by V94 already includes the one-time grant, even if the player removed it.
        .putBoolean("chungryong_granted_v94",true).putBoolean("reference_warrior_granted_v107",true)
        .putString("inventory_v2",new JSONObject(r.inventory()).toString())
        .putString("equipment_v2",new JSONObject(r.equipment()).toString())
        .putLong("bank_gold_v84",r.bankGold()).putString("bank_inventory_v84",new JSONObject(r.bankInventory()).toString())
        .putInt("training_token_qty",quantity(r)).putInt("normal_level",r.normalLevel()).putLong("normal_exp",r.normalExp()).putLong("gold",r.gold())
        .putInt("str",r.str()).putInt("int",r.intel()).putInt("wis",r.wis()).putInt("con",r.con()).putInt("dex",r.dex()).putInt("stat_points",r.statPoints())
        .putInt("base_max_hp_v3",r.baseMaxHp()).putInt("base_max_mp_v3",r.baseMaxMp()).putLong("reward_sequence",r.consumedCombatSequence())
        .putString("job_code_v4",r.currentJobCode()).putString("campaign_v1",r.campaign().snapshot().toString());
  }
  private void restoreRpg(RpgProgressionState r){
    // Decode ownership first. Unknown identities preserve the source save instead of overwriting it.
    Map<String,Integer> owned=new LinkedHashMap<>(r.inventory());
    Map<String,String> equipped=new LinkedHashMap<>(r.equipment());
    try{
      if(prefs.contains("inventory_v2")){
        owned.clear();JSONObject inv=new JSONObject(prefs.getString("inventory_v2","{}"));
        for(java.util.Iterator<String> it=inv.keys();it.hasNext();){String id=it.next();owned.put(id,inv.getInt(id));}
        equipped.clear();JSONObject eq=new JSONObject(prefs.getString("equipment_v2","{}"));
        for(java.util.Iterator<String> it=eq.keys();it.hasNext();){String slot=it.next();equipped.put(slot,eq.getString(slot));}
      }else{
        int token=Math.max(0,prefs.getInt("training_token_qty",0));
        if(token>0)owned.put(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID,token);
        if(prefs.contains("equipment_ids")){
          equipped.clear();String raw=prefs.getString("equipment_ids","");
          for(String id:raw.split("\\|")){if(id.isEmpty())continue;RpgProgressionState.ItemDefinition def=r.itemDefinitions().get(id);if(def==null)throw new IllegalArgumentException("Unknown saved item");equipped.put(def.equipSlot,id);}
        }
      }
      RpgProgressionState staged=new RpgProgressionState();
      if(!staged.restoreOwnedItems(owned,equipped))throw new IllegalArgumentException("Invalid saved ownership");
      // User-requested visual test item: seed old saves once, preserve all equipment and other ownership.
      if(!prefs.getBoolean("chungryong_granted_v94",false)&&!owned.containsKey(RpgProgressionState.CHUNGRYONG_ITEM_ID)){
        owned.put(RpgProgressionState.CHUNGRYONG_ITEM_ID,1);
        if(!staged.restoreOwnedItems(owned,equipped))throw new IllegalArgumentException("Invalid V94 ownership migration");
      }
      if(!prefs.getBoolean("reference_warrior_granted_v107",false)){
        owned.putIfAbsent(RpgProgressionState.REFERENCE_LEOPARD_ITEM_ID,1);
        owned.putIfAbsent(RpgProgressionState.REFERENCE_HELM_ITEM_ID,1);
        if(!staged.restoreOwnedItems(owned,equipped))throw new IllegalArgumentException("Invalid reference gear migration");
      }
      Map<String,Integer> bank=new LinkedHashMap<>();JSONObject bankJson=new JSONObject(prefs.getString("bank_inventory_v84","{}"));
      for(java.util.Iterator<String> it=bankJson.keys();it.hasNext();){String id=it.next();bank.put(id,bankJson.getInt(id));}
      if(!staged.restoreBank(prefs.getLong("bank_gold_v84",0),bank))throw new IllegalArgumentException("Invalid bank");
      staged.restoreProgression(prefs.getInt("normal_level",1),prefs.getLong("normal_exp",0L));
      if(!staged.restoreJobCode(prefs.getString("job_code_v4","COMMONER")))throw new IllegalArgumentException("Invalid saved job");
      staged.restoreGold(prefs.getLong("gold",0L));
      staged.restoreStats(prefs.getInt("str",3),prefs.getInt("int",3),prefs.getInt("wis",3),prefs.getInt("con",3),prefs.getInt("dex",3),prefs.getInt("stat_points",0));
      staged.restoreBaseResources(prefs.getInt("base_max_hp_v3",staged.baseMaxHp()),prefs.getInt("base_max_mp_v3",staged.baseMaxMp()));
      if(prefs.contains("campaign_v1")&&!staged.campaign().restore(new JSONObject(prefs.getString("campaign_v1",""))))throw new IllegalArgumentException("Invalid campaign");
      staged.restoreCombatSequence(prefs.getLong("reward_sequence",0));copyRpg(staged,r);
      if(!prefs.getBoolean("chungryong_granted_v94",false)||!prefs.getBoolean("reference_warrior_granted_v107",false))prefs.edit().putString("inventory_v2",new JSONObject(r.inventory()).toString()).putString("equipment_v2",new JSONObject(r.equipment()).toString()).putBoolean("chungryong_granted_v94",true).putBoolean("reference_warrior_granted_v107",true).commit();
    }catch(Exception invalid){writable=false;}
  }

  private static void copyRpg(RpgProgressionState source,RpgProgressionState dest){
    dest.restoreProgression(source.normalLevel(),source.normalExp());dest.restoreGold(source.gold());
    if(!dest.restoreJobCode(source.currentJobCode()))throw new IllegalArgumentException("Invalid job");
    dest.restoreStats(source.str(),source.intel(),source.wis(),source.con(),source.dex(),source.statPoints());
    dest.restoreBaseResources(source.baseMaxHp(),source.baseMaxMp());
    if(!dest.restoreOwnedItems(source.inventory(),source.equipment()))throw new IllegalArgumentException("Invalid ownership");
    dest.restoreBank(source.bankGold(),source.bankInventory());
    dest.restoreCombatSequence(source.consumedCombatSequence());
    if(!dest.campaign().restore(source.campaign().snapshot()))throw new IllegalArgumentException("Invalid campaign state");
  }
  /** One durable edit covers job/quest/gear/skills; failure restores the live snapshot. */
  public static boolean transactActive(RpgProgressionState r,SkillBook book,java.util.function.BooleanSupplier mutation){
    if(!writable())return false;
    RpgProgressionState before=new RpgProgressionState();copyRpg(r,before);
    JSONObject skills=book==null?null:book.snapshot();
    int hp=active!=null&&active.runtime!=null?active.runtime.player().hp:0,mp=active!=null&&active.runtime!=null?active.runtime.player().mp:0;
    boolean saved=false;
    try {
      if(mutation.getAsBoolean()){
        if(active==null)saved=true;
        else if(active.runtime!=null)saved=active.checkpoint();
        else {SharedPreferences.Editor edit=active.writeRpg(active.prefs.edit(),r);if(book!=null)edit.putString("skill_book_v1",book.snapshot().toString());saved=edit.commit();}
      }
    }catch(RuntimeException failure){saved=false;}
    if(saved)return true;copyRpg(before,r);if(book!=null)book.restore(skills);if(active!=null&&active.runtime!=null){active.runtime.applyDerivedGrowth();active.runtime.player().hp=hp;active.runtime.player().mp=mp;}return false;
  }
  private static int quantity(RpgProgressionState r){Integer q=r.inventory().get(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID);return q==null?0:q;}

  public static boolean commitTurnInActive(F5mAdaptedPrologueQuest q,RpgProgressionState r){
    if(active==null||!active.writable||q==null||r==null)return false;
    if(q.state()!=F5mAdaptedPrologueQuest.State.RETURN_READY)return q.state()==F5mAdaptedPrologueQuest.State.COMPLETED&&rewardClaimedActive();
    RpgProgressionState staged=new RpgProgressionState();copyRpg(r,staged);
    if(staged.autoLootResolvedItem(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID,1)!=RpgProgressionState.AutoLootResult.LOOTED)return false;
    staged.grantAdaptedReward(AdaptedPrototypeRewardCatalog.TRAINING_QUEST_EXP,AdaptedPrototypeRewardCatalog.TRAINING_QUEST_GOLD);
    SharedPreferences.Editor edit=active.writeRpg(active.prefs.edit(),staged);
    active.writeQuest(edit,q).putString("quest_state",F5mAdaptedPrologueQuest.State.COMPLETED.name()).putBoolean("quest_reward_claimed",true);
    if(!edit.commit())return false;
    copyRpg(staged,r);q.restore(F5mAdaptedPrologueQuest.State.COMPLETED,q.currentCount());return true;
  }
  public static boolean commitQuest2TurnInActive(GrowthQuest2 q,RpgProgressionState r){
    if(active==null||!active.writable||q==null||r==null||q.state()!=GrowthQuest2.State.RETURN_READY)return false;
    RpgProgressionState staged=new RpgProgressionState();copyRpg(r,staged);staged.grantAdaptedReward(15000,250);
    SharedPreferences.Editor edit=active.writeRpg(active.prefs.edit(),staged);
    active.writeQuest2(edit,q).putString("quest2_state",GrowthQuest2.State.COMPLETED.name());
    if(!edit.commit())return false;
    copyRpg(staged,r);q.restore(GrowthQuest2.State.COMPLETED,q.currentCount());return true;
  }
}
