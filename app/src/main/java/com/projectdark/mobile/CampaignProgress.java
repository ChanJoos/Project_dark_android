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
  common("M04","자동 사냥 익히기","milles_market_proto",WorldDef.ID,"KILL","combat_dummy_01,combat_dummy_02,combat_dummy_03",5,3,40800,500);
  common("M05","모험가의 준비","milles_job_counselor",WorldDef.ID,"SUPPLY","",7,4,55000,75);
  common("M06","포테 숲길 출정 준비","milles_job_counselor",WorldDef.ID,"LEVEL","",10,1,42760,500);
  common("M07","붉고 푸른 팜팻의 정수","pote_trail_guide","MAP_POTE_01","KILL","POTE_RED,POTE_GREEN",11,10,140000,1160);
  common("M08","두 색의 변화","pote_trail_guide","MAP_POTE_02","PAIR","POTE_PURPLE,POTE_SILVER",15,6,140000,650);
  common("M09","피에트에 보내는 서신","piet_investigator","MAP_PIET_CAMP","VISIT","MAP_PIET_CAMP",18,1,127480,1340);
  common("M10","숲의 서식지 조사","piet_investigator","MAP_PIET_CAMP","KILL","POTE_TREANT,POTE_ANTLION,POTE_GNOLL",20,12,240000,900);
  common("M11","숲길 안전 확보","piet_investigator","MAP_PIET_CAMP","PAIR","POTE_GNOLL,POTE_LYCAN",23,10,249280,1520);
  common("M12","깊은 숲의 추적자","piet_investigator","MAP_PIET_CAMP","KILL","POTE_TREANT,POTE_LYCAN",26,8,225040,1000);
  common("M13","조사 거점의 보급","piet_supplier","MAP_PIET_CAMP","SUPPLY","",28,4,250000,2320);
  common("M14","은빛 늑대의 흔적","piet_purifier","MAP_PIET_CAMP","KILL","POTE_SILVERWOLF",30,5,420000,1300);
  common("M15","은빛 늑대의 갈기","piet_purifier","MAP_PIET_CAMP","KILL","POTE_SILVERWOLF",33,10,418440,1700);
  common("M16","숲의 세 제단","piet_purifier","MAP_PIET_CAMP","ALTAR","",35,3,400000,1300);
  common("M17","결계 앞의 은빛 늑대","piet_investigator","MAP_PIET_CAMP","KILL","POTE_SILVERWOLF",37,10,350000,1500);
  common("M18","은빛 늑대 정찰","piet_investigator","MAP_PIET_CAMP","KILL","POTE_SILVERWOLF",39,1,477000,1520);
  common("M19","두 번째 써클의 끝","piet_investigator","MAP_PIET_CAMP","LEVEL","",40,1,0,0);
  common("M20","다음 모험의 문턱","milles_guide_proto",WorldDef.ID,"VISIT",WorldDef.ID,40,1,0,0);
  // New, map-local forest route. These objectives use events already persisted by the
  // campaign runtime (defeat, map visit, level, and accepted skill use).
  common("A01","입구 숲 조사 등록","pote_trail_guide","MAP_POTE_01","VISIT","MAP_POTE_01",11,1,0,0);
  common("A02","붉은 팜팻 조사","pote_trail_guide","MAP_POTE_01","KILL","POTE_RED",11,5,0,0);
  common("A03","초록 팜팻 조사","pote_trail_guide","MAP_POTE_01","KILL","POTE_GREEN",12,5,0,0);
  common("A04","보랏빛 팜팻의 흔적","pote_trail_guide","MAP_POTE_01","KILL","POTE_PURPLE",14,5,0,0);
  common("A05","은빛 팜팻의 흔적","pote_trail_guide","MAP_POTE_01","KILL","POTE_SILVER",16,5,0,0);
  common("A06","개울 너머 서식지","pote_trail_guide","MAP_POTE_01","PAIR","POTE_RED,POTE_GREEN",17,6,0,0);
  common("A07","숲 입구 마무리 조사","pote_trail_guide","MAP_POTE_01","PAIR","POTE_PURPLE,POTE_SILVER",18,6,0,0);
  common("A08","무리 숲으로","pote_trail_guide","MAP_POTE_02","VISIT","MAP_POTE_02",19,1,0,0);
  common("B01","뿌리 숲의 흔적","pote_trail_guide","MAP_POTE_02","KILL","POTE_TREANT",20,4,0,0);
  common("B02","놀 무리 추적","pote_trail_guide","MAP_POTE_02","KILL","POTE_GNOLL",21,6,0,0);
  common("B03","라이칸스로프 추적","pote_trail_guide","MAP_POTE_02","KILL","POTE_LYCAN",22,6,0,0);
  common("B04","뿌리와 모래의 흔적","pote_trail_guide","MAP_POTE_02","PAIR","POTE_TREANT,POTE_ANTLION",24,8,0,0);
  common("B05","숲의 무리 조사","pote_trail_guide","MAP_POTE_02","PAIR","POTE_GNOLL,POTE_LYCAN",25,10,0,0);
  common("B06","2써클 기술 현장 실습","pote_trail_guide","MAP_POTE_02","SKILL","",26,3,0,0);
  common("B07","앤트라이온 흔적","pote_trail_guide","MAP_POTE_02","KILL","POTE_ANTLION",27,4,0,0);
  common("B08","깊은 숲 전진","pote_trail_guide","MAP_POTE_03","VISIT","MAP_POTE_03",29,1,0,0);
  common("C01","울프라이더 전초 정찰","pote_trail_guide","MAP_POTE_03","KILL","POTE_WOLFRIDER",30,4,0,0);
  common("C02","울프라이더 정찰","pote_trail_guide","MAP_POTE_03","KILL","POTE_WOLFRIDER",31,5,0,0);
  common("C03","앤트자이언트 정찰","pote_trail_guide","MAP_POTE_03","KILL","POTE_ANTGIANT",33,5,0,0);
  common("C04","은빛늑대 영역 조사","pote_trail_guide","MAP_POTE_03","KILL","POTE_SILVERWOLF",35,6,0,0);
  common("C05","깊은 숲의 세 종","pote_trail_guide","MAP_POTE_03","PAIR","POTE_WOLFRIDER,POTE_ANTGIANT",36,8,0,0);
  common("C06","은빛늑대 무리 추적","pote_trail_guide","MAP_POTE_03","KILL","POTE_SILVERWOLF",37,8,0,0);
  common("C07","결계 재료 조사","pote_trail_guide","MAP_POTE_03","PAIR","POTE_ANTGIANT,POTE_SILVERWOLF",38,8,0,0);
  common("C08","결계 앞 최종 보고","piet_investigator","MAP_PIET_CAMP","LEVEL","",40,1,0,0);
  common("D01","결계 너머 진입","pote_trail_guide",com.projectdark.mobile.world.CampaignWorld.BOSS_D,"VISIT",com.projectdark.mobile.world.CampaignWorld.BOSS_D,40,1,0,0);
  common("D02","결계 전투 준비","piet_purifier","MAP_PIET_CAMP","ALTAR","",40,3,0,0);
  common("D03","자이언트맨티스 처치","pote_trail_guide",com.projectdark.mobile.world.CampaignWorld.BOSS_D,"KILL","POTE_MANTIS",40,1,0,0);
  common("D04","숲의 조사 결과 보고","piet_investigator","MAP_PIET_CAMP","VISIT","MAP_PIET_CAMP",41,1,0,0);
  common("T01","나에게 맞는 능력치","milles_job_counselor",WorldDef.ID,"STAT","",3,1,9300,50);
  common("T02","첫 기술 배우기","milles_job_counselor",WorldDef.ID,"LEARN","",3,1,8700,75);
  common("T03","기술을 손끝에","milles_job_counselor",WorldDef.ID,"SLOT","",3,1,12500,50);
  common("T04","첫 장비 구매","milles_job_counselor",WorldDef.ID,"BUY","IT_TEST_SHOES_ML229",3,1,9000,150);
  common("T05","가죽 신발 장착하기","milles_job_counselor",WorldDef.ID,"EQUIP","IT_TEST_SHOES_ML229",3,1,17200,50);
  common("T06","회복 물약 구매","milles_job_counselor",WorldDef.ID,"BUY",RpgProgressionState.B_SMALL_POTION_ITEM_ID,3,1,17800,75);
  common("T07","다친 몸 돌보기","milles_job_counselor",WorldDef.ID,"USE",RpgProgressionState.B_SMALL_POTION_ITEM_ID,3,1,22200,75);
  common("T08","마력 물약 구매","milles_job_counselor",WorldDef.ID,"BUY","IT_B_MP_POTION",3,1,22800,75);
  common("T09","퀵슬롯으로 회복","milles_job_counselor",WorldDef.ID,"QUICK_USE","",3,1,27200,75);
  common("T10","남는 물품 판매","milles_job_counselor",WorldDef.ID,"SELL","IT_B_MP_POTION",3,1,30600,75);
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
 private boolean legacy,forestRoute=true; private String active; private long eventSequence,skillSequence; private boolean bought,healed,sold;
 public void syncOpening(boolean opening,boolean training){if(opening)completed.add("M01");if(training)completed.add("M02");}
 public List<Def> route(String job){List<Def> out=new ArrayList<>();if(forestRoute&&!legacy){for(String id:new String[]{"M01","M02","M03","T01","T02","T03"})out.add(find(id));if(!"COMMONER".equals(job))out.add(find("J01_"+job));for(String id:new String[]{"T04","T05","T06","T07","T08","T09","T10","M04","M06"})out.add(find(id));for(String id:new String[]{"A01","A02","A03","A04","A05","A06","A07","A08","B01","B02","B03","B04","B05","B06","B07","B08","C01","C02","C03","C04","C05","C06","C07","C08","D01","D02","D03","D04"})out.add(find(id));return out;}if(!legacy){for(String id:new String[]{"M01","M02","M03","T01","T02","T03"})out.add(find(id));if(!"COMMONER".equals(job))out.add(find("J01_"+job));for(String id:new String[]{"T04","T05","T06","T07","T08","T09","T10","M04","M06"})out.add(find(id));}for(Def d:ALL)if(d.job==null&&!d.id.startsWith("T")&&(legacy||Integer.parseInt(d.id.substring(1))>=7)&&!d.id.matches("[ABCD]\\d\\d")){out.add(definition(d.id));if(!"COMMONER".equals(job)&&(legacy&&d.id.equals("M03")||d.id.equals("M07")||d.id.equals("M11")))out.add(find("J0"+(d.id.equals("M03")?1:d.id.equals("M07")?2:3)+"_"+job));}return out;}
 public boolean legacy(){return legacy;}
 private Def definition(String id){Def d=find(id);if(!legacy&&d!=null&&d.id.equals("M13"))return new Def(d.id,"조사대의 회복약",d.npc,d.map,"BUY",RpgProgressionState.B_SMALL_POTION_ITEM_ID,d.level,1,d.exp,d.gold,d.job);if(legacy&&d!=null&&d.id.equals("M04"))return new Def(d.id,"마을 외곽의 안전",d.npc,d.map,d.kind,d.targets,d.level,10,d.exp,d.gold,d.job);if((legacy||!forestRoute)&&d!=null&&d.id.equals("M06"))return new Def(d.id,"포테 숲길 답사","pote_trail_guide","MAP_POTE_01","VISIT","MAP_POTE_01",10,1,d.exp,d.gold,d.job);return d;}
 public static long earnedExp(RpgProgressionState r){long exp=r.normalExp();for(int lv=1;lv<r.normalLevel();lv++)exp+=LevelExpCurve.requiredForNext(lv);return exp;}
 public int rewardExp(Def d,RpgProgressionState r){if(forestRoute&&!legacy&&d.id.matches("[ABCD]\\d\\d")){int lv=routeRewardLevel(d.id);long total=0;for(int i=1;i<lv;i++)total+=LevelExpCurve.requiredForNext(i);return (int)Math.max(0,Math.min(Integer.MAX_VALUE,total-earnedExp(r)));}int target=milestone(d);return !legacy&&target>0?(int)Math.max(0,target-earnedExp(r)):d.exp;}
 private static int routeRewardLevel(String id){switch(id){case "A01":return 11;case "A02":return 12;case "A03":return 13;case "A04":return 15;case "A05":return 17;case "A06":return 18;case "A07":return 19;case "A08":case "B01":return 20;case "B02":return 22;case "B03":return 23;case "B04":return 25;case "B05":return 26;case "B06":return 27;case "B07":return 28;case "B08":return 30;case "C01":return 31;case "C02":return 33;case "C03":return 35;case "C04":return 36;case "C05":return 37;case "C06":return 38;case "C07":return 39;case "C08":return 40;case "D01":case "D02":return 40;case "D03":case "D04":return 41;default:return 11;}}
 private static int milestone(Def d){switch(d.id){case "M03":return 30000;case "T01":return 39300;case "T02":return 48000;case "T03":return 60500;case "T04":return 87800;case "T05":return 105000;case "T06":return 122800;case "T07":return 145000;case "T08":return 167800;case "T09":return 195000;case "T10":return 225600;case "M04":return 300000;case "M06":return 378000;default:return d.id.startsWith("J01_")?78800:0;}}
 public String practiceSkill(Def d,RpgProgressionState r){if("B06".equals(d.id))return skill(r.currentJobCode(),2);return !legacy&&d.id.startsWith("J01_CLERIC")?"SK_성직자_001":skill(r.currentJobCode(),Integer.parseInt(d.id.substring(2,3)));}
 public static String beginnerSkill(String job){return "CLERIC".equals(job)?"SK_성직자_001":skill(job,1);}
 public void observeBasics(RpgProgressionState r,SkillBook b){Def d=definition(active);if(d==null||b==null)return;String id=beginnerSkill(r.currentJobCode());if(d.kind.equals("EQUIP")&&r.equipment().containsValue(d.targets))record("EQUIP",d.targets);if(d.kind.equals("LEARN")&&b.owned(id))record("LEARN","");if(d.kind.equals("SLOT")&&b.owned(id)&&!b.testAccess())for(int i=0;i<8;i++)if(id.equals(b.slot(i)))record("SLOT","");}
 public void record(String kind,String target){Def d=definition(active);if(d!=null&&kind.equals(d.kind)&&(d.targets.isEmpty()||d.targets.equals(target)))counts.put("action",1);}
 public Def next(RpgProgressionState r){for(Def d:route(r.currentJobCode()))if(!completed.contains(d.id))return d;return null;}
 public Status status(Def d,RpgProgressionState r){d=definition(d.id);if(completed.contains(d.id))return Status.COMPLETE;Def n=next(r);if(n==null||!n.id.equals(d.id)||d.kind.equals("EXTERNAL")||r.normalLevel()<d.level)return Status.LOCKED;if(d.id.equals(active))return count(d,r)>=d.goal?Status.REPORT:Status.ACTIVE;return Status.AVAILABLE;}
 public int count(Def d,RpgProgressionState r){d=definition(d.id);switch(d.kind){case "JOB":return "COMMONER".equals(r.currentJobCode())?0:1;case "VISIT":return visited.contains(d.targets)?1:0;case "LEVEL":return r.normalLevel()>=d.level?1:0;case "SUPPLY":return (bought?1:0)+(sold?1:0)+(healed?1:0)+(r.equipment().containsKey(RpgProgressionState.ARMOR_SLOT)?1:0);case "ALTAR":return altars.size();case "PAIR":String[] pair=d.targets.split(",");int cap=d.goal/pair.length,total=0;for(String p:pair)total+=Math.min(cap,counts.getOrDefault(p,0));return total;default:int sum=0;for(int v:counts.values())sum+=v;return Math.min(d.goal,sum);}}
 public boolean accept(String id,RpgProgressionState r,SkillBook book){Def d=definition(id);if(d==null||status(d,r)!=Status.AVAILABLE)return false;active=id;counts.clear();altars.clear();if(d.kind.equals("SUPPLY")){bought=false;healed=false;sold=false;}if(d.kind.equals("SKILL")&&(legacy||id.equals("B06")||!id.startsWith("J01_"))&&book!=null&&!book.testAccess()){int tier=id.equals("B06")?2:Integer.parseInt(id.substring(2,3));String skill=skill(r.currentJobCode(),tier);if(SkillRuntimeCatalog.get(skill)!=null){book.learn(skill,0);book.assign(0,skill);}}observeBasics(r,book);return true;}
 public void quickUse(String id,RpgProgressionState r){String expected=(r.currentJobCode().equals("MAGE")||r.currentJobCode().equals("CLERIC"))?"IT_B_MP_POTION":RpgProgressionState.B_SMALL_POTION_ITEM_ID;if(expected.equals(id))record("QUICK_USE","");}
 public void visit(String map){if(map!=null)visited.add(map);}
 public void bought(){bought=true;} public void sold(){sold=true;} public void healed(){healed=true;}
 public void bought(String id){bought();record("BUY",id);} public void sold(String id){sold();record("SELL",id);} public void healed(String id){healed();record("USE",id);}
 public void consume(CombatLedger.Event e,RuntimeState runtime){if(e==null||e.sequence<=eventSequence)return;eventSequence=e.sequence;Def d=definition(active);if(d==null||e.type!=CombatLedger.Type.MONSTER_DEFEATED||!(d.kind.equals("KILL")||d.kind.equals("PAIR")))return;String species=runtime==null?e.targetId:runtime.campaignRewardProfileFor(e.targetId);if(species==null)species=e.targetId;if(!Arrays.asList(d.targets.split(",")).contains(species))return;if(runtime==null||(!e.targetId.startsWith("combat_dummy_")&&runtime.campaignRewardProfileFor(e.targetId)==null))return;counts.put(species,Math.min(d.goal,counts.getOrDefault(species,0)+1));}
 /** Accepted matching uses count in both production and the app's default skill-preview mode. Rejections and damage amount never manufacture a use. */
 public void acceptedSkillUse(String action,long sequence,boolean testMode,RpgProgressionState r){
  if(sequence<=skillSequence)return;Def d=definition(active);if(d==null||!d.kind.equals("SKILL"))return;
  String wanted=practiceSkill(d,r);if(!wanted.equals(action))return;
  SkillAbilityCatalog.Ability ability=SkillAbilityCatalog.get(action);if(ability==null||ability.heal())return;
  skillSequence=sequence;counts.put("action",Math.min(d.goal,counts.getOrDefault("action",0)+1));
 }
 /** Healing objectives deliberately require a resolver-confirmed positive HP restoration. */
 public void appliedHealingUse(String action,long sequence,int restoredHp,boolean testMode,RpgProgressionState r){
  if(sequence<=skillSequence||restoredHp<=0)return;Def d=definition(active);if(d==null||!d.kind.equals("SKILL"))return;
  String wanted=practiceSkill(d,r);SkillAbilityCatalog.Ability ability=SkillAbilityCatalog.get(action);
  if(!wanted.equals(action)||ability==null||!ability.heal())return;skillSequence=sequence;counts.put("action",Math.min(d.goal,counts.getOrDefault("action",0)+1));
 }
 public boolean purify(String altar,RpgProgressionState r){
  Def d=definition(active);if(d==null||!d.kind.equals("ALTAR")||!Arrays.asList("campaign_altar_0","campaign_altar_1","campaign_altar_2").contains(altar)||altars.contains(altar))return false;
  int quantity=r.inventory().getOrDefault("IT_B_PURIFIED_ESSENCE",0);if(quantity<1)return false;
  Map<String,Integer> inv=new LinkedHashMap<>(r.inventory());if(quantity==1)inv.remove("IT_B_PURIFIED_ESSENCE");else inv.put("IT_B_PURIFIED_ESSENCE",quantity-1);
  if(!r.restoreOwnedItems(inv,r.equipment()))return false;return altars.add(altar);
 }
 public boolean claim(String id,RpgProgressionState r){
  Def d=definition(id);if(d==null||status(d,r)!=Status.REPORT)return false;
  Map<String,Integer> rewards=new LinkedHashMap<>();if(!d.id.startsWith("T")){rewards.put(RpgProgressionState.B_SMALL_POTION_ITEM_ID,3);rewards.put("IT_B_MP_POTION",3);}
  int tier=d.id.equals("B06")||d.id.startsWith("J02_")?11:d.id.startsWith("J03_")?26:0;
  if(tier>0)rewards.put("IT_B_CAMPAIGN_"+r.currentJobCode()+"_"+tier,1);
  if(d.id.equals("M15")||d.id.equals("C06"))rewards.put("IT_B_PURIFIED_ESSENCE",3);
  // Adapt the recorded Mantis ring drop into a first-clear, direct-inventory quest reward.
  if(d.id.equals("D03"))rewards.put("IT_RING_THREELINEGOLD",1);
  for(Map.Entry<String,Integer> e:rewards.entrySet())if(!r.itemDefinitions().containsKey(e.getKey())||r.inventory().getOrDefault(e.getKey(),0)>999999-e.getValue())return false;
  for(Map.Entry<String,Integer> e:rewards.entrySet())r.autoLootResolvedItem(e.getKey(),e.getValue());
  if(tier>0)r.equip("IT_B_CAMPAIGN_"+r.currentJobCode()+"_"+tier);
  r.grantAdaptedReward(rewardExp(d,r),d.gold);completed.add(id);active=null;counts.clear();altars.clear();return true;
 }

 public boolean wanted(Def d,String species){if(d==null||!Arrays.asList(d.targets.split(",")).contains(species))return false;return !d.kind.equals("PAIR")||counts.getOrDefault(species,0)<d.goal/d.targets.split(",").length;}
 public String objective(Def d){d=definition(d.id);if(d.id.startsWith("T")||d.kind.equals("BUY"))return OnboardingGuide.objective(d);if(d.id.equals("B06"))return "현재 직업의 2써클 기술 3회 사용";if(d.kind.equals("SKILL")){SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(!legacy&&d.id.equals("J01_CLERIC")?"SK_성직자_001":skill(d.job==null?"WARRIOR":d.job,Integer.parseInt(d.id.substring(2,3))));return a.name+(a.heal()?"로 실제 HP 회복":" 사용")+" 3회";}if(d.kind.equals("KILL")||d.kind.equals("PAIR")){StringBuilder out=new StringBuilder();PoteMonsterRoster roster=new PoteMonsterRoster();for(String id:d.targets.split(",")){if(out.length()>0)out.append(" · ");PoteMonsterRoster.Entry e=roster.find(id);out.append(MillesMousePresentation.isEarlyFieldMouse(id)?"들쥐":e==null?id.startsWith("combat_dummy_")?"훈련 몬스터":id.equals("POTE_MANTIS")?"자이언트맨티스":"대상 몬스터":e.name);if(d.kind.equals("PAIR"))out.append(" ").append(Math.min(d.goal/2,counts.getOrDefault(id,0))).append("/").append(d.goal/2);}return out.toString();}return d.kind.equals("ALTAR")?"정화된 정수 3개로 제단 3곳 정화":d.kind.equals("SUPPLY")?"상점 구매 · 판매 · 회복약 사용 · 의상 착용":d.kind.equals("LEVEL")?"Lv"+(d.id.equals("D04")?41:d.level)+" 도달":"목표 지역 방문";}
 public boolean altarDone(String id){return altars.contains(id);}
 public String activeId(){return active;} public long sequence(){return eventSequence;} public boolean finished(){return forestRoute?completed.contains("D04"):completed.contains("M20");}
 public JSONObject snapshot(){try{return new JSONObject().put("version",4).put("legacy",legacy).put("forestRoute",forestRoute).put("complete",new JSONArray(completed)).put("visited",new JSONArray(visited)).put("altars",new JSONArray(altars)).put("counts",new JSONObject(counts)).put("active",active==null?JSONObject.NULL:active).put("sequence",eventSequence).put("skillSequence",skillSequence).put("bought",bought).put("healed",healed).put("sold",sold);}catch(JSONException e){throw new IllegalStateException(e);}}
 public boolean restore(JSONObject j){try{int version=j.getInt("version");if(version<1||version>4)return false;Set<String> c=decode(j.getJSONArray("complete")),v=decode(j.getJSONArray("visited")),a=decode(j.getJSONArray("altars"));for(String id:c)if(find(id)==null)return false;String active=j.isNull("active")?null:j.getString("active");if(active!=null&&(find(active)==null||c.contains(active)))return false;Map<String,Integer> n=new LinkedHashMap<>();JSONObject ns=j.getJSONObject("counts");for(Iterator<String> it=ns.keys();it.hasNext();){String k=it.next();int x=ns.getInt(k);if(x<0||x>1000)return false;n.put(k,x);}boolean bought=j.getBoolean("bought"),healed=j.getBoolean("healed"),sold=j.optBoolean("sold",false);long seq=j.getLong("sequence"),sk=j.getLong("skillSequence");if(seq<0||sk<0||a.size()>3)return false;for(String map:v)if(!WorldDef.ID.equals(map)&&!com.projectdark.mobile.world.CampaignWorld.contains(map)&&com.projectdark.mobile.world.TownInteriorDef.forMap(map)==null)return false;for(String altar:a)if(!Arrays.asList("campaign_altar_0","campaign_altar_1","campaign_altar_2").contains(altar))return false;boolean old=version==1&&(active!=null&&!Arrays.asList("M01","M02","M03").contains(active)||c.contains("M03"));legacy=version>=2?j.optBoolean("legacy",false):old;boolean migrate=version<4&&!legacy&&c.contains("M03");forestRoute=version>=4?j.optBoolean("forestRoute",false):migrate;completed.clear();completed.addAll(c);if(migrate)migrateForestCompletions(c,completed);visited.clear();visited.addAll(v);altars.clear();altars.addAll(a);counts.clear();counts.putAll(n);if(version<3&&Arrays.asList("M11","M12","M14","M15","M17","M18").contains(active)){active=null;counts.clear();altars.clear();}if(migrate&&active!=null&&active.matches("M(0[6-9]|1[0-9])")){active=null;counts.clear();altars.clear();}this.active=active;eventSequence=seq;skillSequence=0;this.bought=bought;this.healed=healed;this.sold=sold;return true;}catch(Exception e){return false;}}
 private static void migrateForestCompletions(Set<String> old,Set<String> out){for(int n=6;n<=20;n++)if(old.contains(String.format(Locale.US,"M%02d",n))){out.add("M06");out.add("A01");break;}if(old.contains("M07")){out.add("A02");out.add("A03");}if(old.contains("M08")){out.add("A04");out.add("A05");}if(old.contains("M09"))out.add("A08");if(old.contains("M11"))out.add("B05");if(old.contains("M13")||old.contains("M14")||old.contains("M15")||old.contains("M16")||old.contains("M17")||old.contains("M18")){for(String id:new String[]{"A01","A02","A03","A04","A05","A06","A07","A08","B01","B02","B03","B04","B05","B07","B08"})out.add(id);}if(old.contains("M15")||old.contains("M17")||old.contains("M18")){out.add("C04");out.add("C06");}if(old.contains("M16"))out.add("D02");if(old.contains("M19")){for(String id:new String[]{"A01","A02","A03","A04","A05","A06","A07","A08","B01","B02","B03","B04","B05","B06","B07","B08","C01","C02","C03","C04","C05","C06","C07","C08"})out.add(id);}}
 private static Set<String> decode(JSONArray a)throws JSONException{Set<String>s=new LinkedHashSet<>();for(int i=0;i<a.length();i++)s.add(a.getString(i));return s;}
}
