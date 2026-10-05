package com.projectdark.mobile;

import java.util.*;
import org.json.*;

/** User-authorized mobile campaign. All new quests/rewards are ADAPTED, not canonical claims. */
public final class CampaignProgress {
 public enum Status { LOCKED,AVAILABLE,ACTIVE,REPORT,COMPLETE }
 public static final String[] JOBS={"WARRIOR","ROGUE","MAGE","CLERIC","MARTIAL_ARTIST"};
 public static final class Def {
  public final String id,title,npc,map,kind,targets,job; public final int level,goal,exp; public final long gold;
  Def(String id,String title,String npc,String map,String kind,String targets,int level,int goal,int exp,long gold,String job){this.id=id;this.title=title;this.npc=npc;this.map=map;this.kind=kind;this.targets=targets;this.level=level;this.goal=goal;this.exp=exp;this.gold=gold;this.job=job;}
 }
 private static final List<Def> ALL=new ArrayList<>();
 static {
  common("M01","여관의 소란","interior_inn","milles_interior_inn","EXTERNAL","",1,1,7500,100);
  common("M02","성장 훈련","milles_guide_proto",WorldDef.ID,"EXTERNAL","",2,3,15000,250);
  common("M03","나의 첫 직업","milles_job_counselor",WorldDef.ID,"JOB","",3,1,1700,75);
  common("M04","마을 외곽의 안전","milles_market_proto",WorldDef.ID,"KILL","combat_dummy_01,combat_dummy_02,combat_dummy_03",5,10,40800,500);
  common("M05","모험가의 준비","milles_job_counselor",WorldDef.ID,"SUPPLY","",7,4,55000,75);
  common("M06","포테 숲길 답사","pote_trail_guide","MAP_POTE_01","VISIT","MAP_POTE_01",10,1,42760,500);
  common("M07","붉고 푸른 팜팻의 정수","pote_trail_guide","MAP_POTE_01","KILL","POTE_RED,POTE_GREEN",11,10,140000,1160);
  common("M08","두 색의 변화","pote_trail_guide","MAP_POTE_02","PAIR","POTE_PURPLE,POTE_SILVER",15,6,140000,650);
  common("M09","피에트에 보내는 서신","piet_investigator","MAP_PIET_CAMP","VISIT","MAP_PIET_CAMP",18,1,127480,1340);
  common("M10","숲의 서식지 조사","piet_investigator","MAP_PIET_CAMP","KILL","POTE_TREANT,POTE_ANTLION,POTE_GNOLL",20,12,240000,900);
  common("M11","숲길 안전 확보","piet_investigator","MAP_PIET_CAMP","PAIR","POTE_WOLFRIDER,POTE_ANTGIANT",23,10,249280,1520);
  common("M12","강력한 개체의 흔적","piet_investigator","MAP_PIET_CAMP","KILL","POTE_STRONG_GNOLL,POTE_STRONG_TREANT",26,8,225040,1000);
  common("M13","조사 거점의 보급","piet_supplier","MAP_PIET_CAMP","SUPPLY","",28,4,250000,2320);
  common("M14","포테 정수 수집","piet_purifier","MAP_PIET_CAMP","KILL","POTE_STRONG_GNOLL,POTE_STRONG_TREANT,POTE_STRONG_WOLFRIDER",30,5,420000,1300);
  common("M15","피에트 정수 정제","piet_purifier","MAP_PIET_CAMP","KILL","POTE_STRONG_GNOLL,POTE_STRONG_TREANT,POTE_STRONG_WOLFRIDER",33,10,418440,1700);
  common("M16","숲의 세 제단","piet_purifier","MAP_PIET_CAMP","ALTAR","",35,3,400000,1300);
  common("M17","결계 앞의 전투","piet_investigator","MAP_PIET_CAMP","KILL","POTE_STRONG_GNOLL,POTE_STRONG_TREANT,POTE_STRONG_WOLFRIDER",37,10,350000,1500);
  common("M18","정예 경계병","piet_investigator","MAP_PIET_CAMP","KILL","POTE_CAMPAIGN_ELITE_GNOLL",39,1,477000,1520);
  common("M19","두 번째 써클의 끝","piet_investigator","MAP_PIET_CAMP","LEVEL","",40,1,0,0);
  common("M20","다음 모험의 문턱","milles_guide_proto",WorldDef.ID,"VISIT",WorldDef.ID,40,1,0,0);
  for(String j:JOBS)for(int tier=1;tier<=3;tier++)ALL.add(new Def("J0"+tier+"_"+j,(tier==1?"첫 기술 실습":tier==2?"2써클 기술 실습":"주력 기술의 완성")+" · "+jobName(j),mentor(j),tier==3?"MAP_PIET_CAMP":WorldDef.ID,"SKILL","",tier==1?3:tier==2?11:26,3,0,0,j));
 }
 private static void common(String id,String title,String npc,String map,String kind,String targets,int lv,int n,int exp,long gold){ALL.add(new Def(id,title,npc,map,kind,targets,lv,n,exp,gold,null));}
 public static List<Def> definitions(){return Collections.unmodifiableList(ALL);}
 public static Def find(String id){for(Def d:ALL)if(d.id.equals(id))return d;return null;}
 public static String mentor(String job){return "WARRIOR".equals(job)?"mentor_warrior":"ROGUE".equals(job)?"mentor_rogue":"MAGE".equals(job)?"mentor_mage":"CLERIC".equals(job)?"mentor_cleric":"mentor_monk";}
 public static String jobName(String j){return "WARRIOR".equals(j)?"전사":"ROGUE".equals(j)?"도적":"MAGE".equals(j)?"마법사":"CLERIC".equals(j)?"성직자":"MARTIAL_ARTIST".equals(j)?"무도가":"평민";}
 public static String skill(String job,int tier){String prefix="SK_"+jobName(job)+"_";String tail="WARRIOR".equals(job)?(tier==1?"001":tier==2?"003":"006"):"ROGUE".equals(job)?(tier==1?"001":tier==2?"003":"007"):"MAGE".equals(job)?(tier==1?"001":tier==2?"004":"007"):"CLERIC".equals(job)?(tier==1?"013":tier==2?"052":"005"):(tier==1?"001":tier==2?"002":"007");return prefix+tail;}
 private final Set<String> completed=new LinkedHashSet<>(),visited=new LinkedHashSet<>(),altars=new LinkedHashSet<>();
 private final Map<String,Integer> counts=new LinkedHashMap<>();
 private String active; private long eventSequence,skillSequence; private boolean bought,healed,sold;
 public void syncOpening(boolean opening,boolean training){if(opening)completed.add("M01");if(training)completed.add("M02");}
 public List<Def> route(String job){List<Def> out=new ArrayList<>();for(Def d:ALL)if(d.job==null){out.add(d);if(!"COMMONER".equals(job)&&(d.id.equals("M03")||d.id.equals("M07")||d.id.equals("M11")))out.add(find("J0"+(d.id.equals("M03")?1:d.id.equals("M07")?2:3)+"_"+job));}return out;}
 public Def next(RpgProgressionState r){for(Def d:route(r.currentJobCode()))if(!completed.contains(d.id))return d;return null;}
 public Status status(Def d,RpgProgressionState r){if(completed.contains(d.id))return Status.COMPLETE;Def n=next(r);if(n==null||!n.id.equals(d.id)||d.kind.equals("EXTERNAL")||r.normalLevel()<d.level)return Status.LOCKED;if(d.id.equals(active))return count(d,r)>=d.goal?Status.REPORT:Status.ACTIVE;return Status.AVAILABLE;}
 public int count(Def d,RpgProgressionState r){switch(d.kind){case "JOB":return "COMMONER".equals(r.currentJobCode())?0:1;case "VISIT":return visited.contains(d.targets)?1:0;case "LEVEL":return r.normalLevel()>=d.level?1:0;case "SUPPLY":return (bought?1:0)+(sold?1:0)+(healed?1:0)+(r.equipment().containsKey(RpgProgressionState.ARMOR_SLOT)?1:0);case "ALTAR":return altars.size();case "PAIR":String[] pair=d.targets.split(",");int cap=d.goal/pair.length,total=0;for(String p:pair)total+=Math.min(cap,counts.getOrDefault(p,0));return total;default:int sum=0;for(int v:counts.values())sum+=v;return Math.min(d.goal,sum);}}
 public boolean accept(String id,RpgProgressionState r,SkillBook book){Def d=find(id);if(d==null||status(d,r)!=Status.AVAILABLE)return false;active=id;counts.clear();altars.clear();if(d.kind.equals("SUPPLY")){bought=false;healed=false;sold=false;}if(d.kind.equals("SKILL")&&book!=null&&!book.testAccess()){int tier=Integer.parseInt(id.substring(2,3));String skill=skill(r.currentJobCode(),tier);if(SkillRuntimeCatalog.get(skill)!=null){book.learn(skill,0);book.assign(0,skill);}}return true;}
 public void visit(String map){if(map!=null)visited.add(map);}
 public void bought(){bought=true;} public void sold(){sold=true;} public void healed(){healed=true;}
 public void consume(CombatLedger.Event e,RuntimeState runtime){if(e==null||e.sequence<=eventSequence)return;eventSequence=e.sequence;Def d=find(active);if(d==null||e.type!=CombatLedger.Type.MONSTER_DEFEATED||!(d.kind.equals("KILL")||d.kind.equals("PAIR")))return;String species=runtime==null?e.targetId:runtime.campaignRewardProfileFor(e.targetId);if(species==null)species=e.targetId;if(!Arrays.asList(d.targets.split(",")).contains(species))return;if(runtime==null||(!e.targetId.startsWith("combat_dummy_")&&runtime.campaignRewardProfileFor(e.targetId)==null))return;counts.put(species,Math.min(d.goal,counts.getOrDefault(species,0)+1));}
 public void validSkill(String action,long sequence,int amount,boolean testMode,RpgProgressionState r){
  if(testMode||sequence<=skillSequence)return;skillSequence=sequence;Def d=find(active);if(d==null||!d.kind.equals("SKILL"))return;
  String wanted=skill(r.currentJobCode(),Integer.parseInt(d.id.substring(2,3)));if(!wanted.equals(action))return;
  SkillAbilityCatalog.Ability ability=SkillAbilityCatalog.get(action);
  // An applied damage technique counts as a use even when mitigation rounds damage to zero;
  // a healing practice still requires actual HP restored.
  if(ability==null||(!ability.damage()&&(!ability.heal()||amount<=0)))return;
  counts.put("action",Math.min(d.goal,counts.getOrDefault("action",0)+1));
 }
 public boolean purify(String altar,RpgProgressionState r){
  Def d=find(active);if(d==null||!d.kind.equals("ALTAR")||!Arrays.asList("campaign_altar_0","campaign_altar_1","campaign_altar_2").contains(altar)||altars.contains(altar))return false;
  int quantity=r.inventory().getOrDefault("IT_B_PURIFIED_ESSENCE",0);if(quantity<1)return false;
  Map<String,Integer> inv=new LinkedHashMap<>(r.inventory());if(quantity==1)inv.remove("IT_B_PURIFIED_ESSENCE");else inv.put("IT_B_PURIFIED_ESSENCE",quantity-1);
  if(!r.restoreOwnedItems(inv,r.equipment()))return false;return altars.add(altar);
 }
 public boolean claim(String id,RpgProgressionState r){
  Def d=find(id);if(d==null||status(d,r)!=Status.REPORT)return false;
  Map<String,Integer> rewards=new LinkedHashMap<>();rewards.put(RpgProgressionState.B_SMALL_POTION_ITEM_ID,3);rewards.put("IT_B_MP_POTION",3);
  int tier=d.id.startsWith("J02_")?11:d.id.startsWith("J03_")?26:0;
  if(tier>0)rewards.put("IT_B_CAMPAIGN_"+r.currentJobCode()+"_"+tier,1);
  if(d.id.equals("M15"))rewards.put("IT_B_PURIFIED_ESSENCE",3);
  for(Map.Entry<String,Integer> e:rewards.entrySet())if(!r.itemDefinitions().containsKey(e.getKey())||r.inventory().getOrDefault(e.getKey(),0)>999999-e.getValue())return false;
  for(Map.Entry<String,Integer> e:rewards.entrySet())r.autoLootResolvedItem(e.getKey(),e.getValue());
  if(tier>0)r.equip("IT_B_CAMPAIGN_"+r.currentJobCode()+"_"+tier);
  r.grantAdaptedReward(d.exp,d.gold);completed.add(id);active=null;counts.clear();altars.clear();return true;
 }

 public boolean wanted(Def d,String species){if(d==null||!Arrays.asList(d.targets.split(",")).contains(species))return false;return !d.kind.equals("PAIR")||counts.getOrDefault(species,0)<d.goal/d.targets.split(",").length;}
 public String objective(Def d){if(d.kind.equals("SKILL")){SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(skill(d.job,Integer.parseInt(d.id.substring(2,3))));return a.name+(a.heal()?"로 실제 HP 회복":"로 유효 타격")+" 3회";}if(d.kind.equals("KILL")||d.kind.equals("PAIR")){StringBuilder out=new StringBuilder();PoteMonsterRoster roster=new PoteMonsterRoster();for(String id:d.targets.split(",")){if(out.length()>0)out.append(" · ");PoteMonsterRoster.Entry e=roster.find(id);out.append(MillesMousePresentation.isEarlyFieldMouse(id)?"들쥐":e==null?id.startsWith("combat_dummy_")?"훈련 몬스터":"정예 강력한 놀":e.name);if(d.kind.equals("PAIR"))out.append(" ").append(Math.min(d.goal/2,counts.getOrDefault(id,0))).append("/").append(d.goal/2);}return out.toString();}return d.kind.equals("ALTAR")?"정화된 정수 3개로 제단 3곳 정화":d.kind.equals("SUPPLY")?"상점 구매 · 판매 · 회복약 사용 · 의상 착용":d.kind.equals("LEVEL")?"Lv40 도달":"목표 지역 방문";}
 public boolean altarDone(String id){return altars.contains(id);}
 public String activeId(){return active;} public long sequence(){return eventSequence;} public boolean finished(){return completed.contains("M20");}
 public JSONObject snapshot(){try{return new JSONObject().put("version",1).put("complete",new JSONArray(completed)).put("visited",new JSONArray(visited)).put("altars",new JSONArray(altars)).put("counts",new JSONObject(counts)).put("active",active==null?JSONObject.NULL:active).put("sequence",eventSequence).put("skillSequence",skillSequence).put("bought",bought).put("healed",healed).put("sold",sold);}catch(JSONException e){throw new IllegalStateException(e);}}
 public boolean restore(JSONObject j){try{if(j.getInt("version")!=1)return false;Set<String> c=decode(j.getJSONArray("complete")),v=decode(j.getJSONArray("visited")),a=decode(j.getJSONArray("altars"));for(String id:c)if(find(id)==null)return false;String active=j.isNull("active")?null:j.getString("active");if(active!=null&&(find(active)==null||c.contains(active)))return false;Map<String,Integer> n=new LinkedHashMap<>();JSONObject ns=j.getJSONObject("counts");for(Iterator<String> it=ns.keys();it.hasNext();){String k=it.next();int x=ns.getInt(k);if(x<0||x>1000)return false;n.put(k,x);}boolean bought=j.getBoolean("bought"),healed=j.getBoolean("healed"),sold=j.optBoolean("sold",false);long seq=j.getLong("sequence"),sk=j.getLong("skillSequence");if(seq<0||sk<0||a.size()>3)return false;for(String map:v)if(!WorldDef.ID.equals(map)&&!com.projectdark.mobile.world.CampaignWorld.contains(map)&&com.projectdark.mobile.world.TownInteriorDef.forMap(map)==null)return false;for(String altar:a)if(!Arrays.asList("campaign_altar_0","campaign_altar_1","campaign_altar_2").contains(altar))return false;completed.clear();completed.addAll(c);visited.clear();visited.addAll(v);altars.clear();altars.addAll(a);counts.clear();counts.putAll(n);this.active=active;eventSequence=seq;skillSequence=0;this.bought=bought;this.healed=healed;this.sold=sold;return true;}catch(Exception e){return false;}}
 private static Set<String> decode(JSONArray a)throws JSONException{Set<String>s=new LinkedHashSet<>();for(int i=0;i<a.length();i++)s.add(a.getString(i));return s;}
}
