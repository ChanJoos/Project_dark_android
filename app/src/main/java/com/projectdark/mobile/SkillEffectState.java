package com.projectdark.mobile;
import java.util.*;
import org.json.*;
/** Combat-owned timed effects. Reapplication refreshes a family; it never stacks copies. */
public final class SkillEffectState {
 public static final class Effect {public final String source,kind;public final int power;public float remaining,tick;Effect(String source,String kind,int p,float t){this.source=source;this.kind=kind;power=p;remaining=t;}}
 private final Map<String,LinkedHashMap<String,Effect>> effects=new HashMap<>();
 public void put(String actor,String source,String kind,int power,float seconds){effects.computeIfAbsent(actor,k->new LinkedHashMap<>()).put(kind,new Effect(source,kind,power,seconds));}
 public boolean has(String actor,String kind){return get(actor,kind)!=null;}
 public Effect get(String actor,String kind){Map<String,Effect> e=effects.get(actor);return e==null?null:e.get(kind);}
 public int power(String actor,String kind){Effect e=get(actor,kind);return e==null?0:e.power;}
 public void remove(String actor,String kind){Map<String,Effect> e=effects.get(actor);if(e!=null)e.remove(kind);}
 public void clear(String actor){effects.remove(actor);}
 public boolean rooted(String actor){return has(actor,"ROOT")||disabled(actor);}
 public boolean disabled(String actor){return has(actor,"FREEZE")||has(actor,"SLEEP");}
 public boolean hidden(){return has("player","STEALTH");}
 public float speed(){return has("player","SPEED")?1.2f:1f;}
 public List<Effect> playerEffects(){Map<String,Effect> e=effects.get("player");return e==null?Collections.emptyList():Collections.unmodifiableList(new ArrayList<>(e.values()));}
 public void tick(RuntimeState state,float dt){
  if(dt<=0)return;
  for(String actor:new ArrayList<>(effects.keySet())){
   RuntimeState.Monster m=monster(state,actor);boolean player=actor.equals("player");
   if(player?!state.player().alive:m==null||!m.alive){clear(actor);continue;}
   Map<String,Effect> map=effects.get(actor);if(map==null)continue;
   for(Effect e:new ArrayList<>(map.values())){
    float active=Math.min(dt,e.remaining);e.remaining-=dt;e.tick+=active;
    while(e.tick>=1f){e.tick-=1f;
     if(e.kind.equals("POISON")||e.kind.equals("DEATH_POISON")){int n=e.kind.equals("DEATH_POISON")?Math.max(1,(int)Math.round((player?state.player().maxHp:m.maxHp)*.05)):e.power;if(player)state.damagePlayer(n);else state.damage(m,n);}
     else if(player&&e.kind.equals("REGEN"))state.player().hp=Math.min(state.player().maxHp,state.player().hp+e.power);
     else if(player&&(e.kind.equals("DRAGON")||e.kind.equals("PHOENIX")))state.player().hp=Math.max(1,state.player().hp-Math.max(1,state.player().maxHp/100));
    }
    if(e.remaining<=0)map.remove(e.kind);
   }
   if(map.isEmpty())effects.remove(actor);
  }
 }
 public JSONObject snapshot(){try{JSONArray list=new JSONArray();for(Effect e:playerEffects())list.put(new JSONObject().put("source",e.source).put("kind",e.kind).put("power",e.power).put("remaining",e.remaining).put("tick",e.tick));return new JSONObject().put("version",1).put("player",list);}catch(JSONException e){throw new IllegalStateException(e);}}
 public boolean restore(JSONObject json){try{if(json.getInt("version")!=1)return false;JSONArray rows=json.getJSONArray("player");List<Effect> next=new ArrayList<>();for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(r.getString("source"));float seconds=(float)r.getDouble("remaining"),clock=(float)r.getDouble("tick");int power=r.getInt("power");String kind=r.getString("kind");if(a==null||!validKind(kind)||!Float.isFinite(seconds)||seconds<=0||seconds>120||!Float.isFinite(clock)||clock<0||clock>=1||power<0||power>1000000)return false;Effect e=new Effect(a.id,kind,power,seconds);e.tick=clock;next.add(e);}clear("player");for(Effect e:next){put("player",e.source,e.kind,e.power,e.remaining);get("player",e.kind).tick=e.tick;}return true;}catch(Exception e){return false;}}
 private static boolean validKind(String k){return Arrays.asList("ROOT","FREEZE","SLEEP","BLIND","DRAGON","PHOENIX","PHYSICAL_GUARD","TECH_MAGIC_GUARD","FOCUS","STEALTH","EVASION","ELEMENT_BOOST","ARMOR","HIT","DAM","REGEN","SHIELD","PROTECT","INVINCIBLE","REFLECT_MAGIC","REFLECT_TECH","MAGIC_GUARD","SPEED","BASIC_POWER","CURSE","POISON","DEATH_POISON","ARMOR_BREAK","CHANGE_ELEMENT","DISARM","AGGRO_RESET","TAUNT").contains(k);}
 static RuntimeState.Monster monster(RuntimeState state,String id){for(RuntimeState.Monster m:state.monsters())if(m.id.equals(id))return m;return null;}
}
