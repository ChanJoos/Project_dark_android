package com.projectdark.mobile;
import java.util.*;
/** ADAPTED presentation identities. Stable domain IDs, quests and saves remain authoritative. */
public final class NpcIdentity {
 public static final String NAME_SOURCE="SSA_US_1926_2025_TOP100";
 public static final class Profile {
  public final String id,name,role,outfit,weapon; public final int rank;
  Profile(String id,String name,int rank,String role,String outfit,String weapon){this.id=id;this.name=korean(name);this.rank=rank;this.role=role;this.outfit=outfit;this.weapon=weapon;}
  public String label(){return name+" · "+role;}
 }
 private static final String[] NAMES={"James","Michael","John","Robert","David","William","Richard","Joseph","Thomas","Christopher","Charles","Daniel","Matthew","Anthony","Mark","Steven","Andrew","Donald","Joshua","Paul","Kevin","Kenneth","Brian","Timothy","Ronald","Jason","George","Edward","Jeffrey","Jacob","Ryan","Nicholas","Gary","Eric","Jonathan","Stephen","Larry","Justin","Benjamin","Scott","Brandon","Samuel","Alexander","Gregory","Patrick","Jack","Frank","Raymond","Dennis","Aaron","Tyler","Jerry","Jose","Nathan","Adam","Henry","Zachary","Douglas","Peter","Noah","Ethan","Kyle","Christian","Jeremy","Austin","Keith","Sean","Terry","Roger","Dylan","Walter","Gerald","Jordan","Gabriel","Carl","Bryan","Jesse","Logan","Lawrence","Elijah","Arthur","Bruce","Harold","Billy","Liam","Alan","Juan","Joe","Mason","Lucas","Randy","Willie","Wayne","Vincent","Caleb","Albert","Luke","Isaac","Bradley","Cameron"};
 private static final String[] KOREAN_NAMES={"제임스","마이클","존","로버트","데이비드","윌리엄","리처드","조셉","토머스","크리스토퍼","찰스","대니얼","매슈","앤서니","마크","스티븐","앤드루","도널드","조슈아","폴","케빈","케네스","브라이언","티머시","로널드","제이슨","조지","에드워드","제프리","제이컵","라이언","니컬러스","게리","에릭","조너선","스테픈","래리","저스틴","벤저민","스콧","브랜던","새뮤얼","알렉산더","그레고리","패트릭","잭","프랭크","레이먼드","데니스","에런","타일러","제리","호세","네이선","애덤","헨리","재커리","더글러스","피터","노아","이선","카일","크리스천","제러미","오스틴","키스","숀","테리","로저","딜런","월터","제럴드","조던","게이브리얼","칼","브라이언","제시","로건","로런스","일라이자","아서","브루스","해럴드","빌리","리엄","앨런","후안","조","메이슨","루커스","랜디","윌리","웨인","빈센트","케일럽","앨버트","루크","아이작","브래들리","캐머런"};
 public static String korean(String name){for(int i=0;i<NAMES.length;i++)if(NAMES[i].equals(name))return KOREAN_NAMES[i];return name;}
 public static final List<Profile> ALL=Collections.unmodifiableList(Arrays.asList(
  new Profile("milles_guide_proto","James",1,"마을 경비","mu0000059,mh124,ml255","mw015"),
  new Profile("milles_gate_proto","William",6,"숲길 경비","mu0000057,mh108,ml243","mw015"),
  new Profile("pote_travel_guide","Noah",60,"숲 여행 안내","mu0000102,mh007,ml240",null),
  new Profile("milles_west_proto","David",5,"서부 주민","mu0000030,mh010,ml243",null),
  new Profile("milles_market_proto","Matthew",13,"시장 상인","mu0000237,mh232,ml240",null),
  new Profile("milles_job_counselor","Michael",2,"직업 상담관","mu0000007,mh132,ml255","mw015"),
  new Profile("pote_trail_guide","Alexander",43,"숲길 안내","mu0000365,mh009,ml255",null),
  new Profile("interior_reagent","Henry",56,"시약상","mu0000117,mh132,ml240","mw024"),
  new Profile("interior_equipment","Logan",78,"장비상","mu0000055,mh005,ml255",null),
  new Profile("interior_bank","Charles",11,"은행원","mu0000202,mh006,ml243",null),
  new Profile("interior_church","Joseph",8,"사제","mu0000210,mh008,ml240","mw026"),
  new Profile("mentor_warrior","Robert",4,"전사 지도자","mu0000059,mh005,ml255","mw015"),
  new Profile("mentor_rogue","John",3,"도적 지도자","mu0000055,mh010,ml243","mw024"),
  new Profile("mentor_mage","Daniel",12,"마법사 지도자","mu0000117,mh006,ml240","mw024"),
  new Profile("mentor_cleric","Thomas",9,"성직자 지도자","mu0000210,mh011,ml243","mw026"),
  new Profile("mentor_monk","Jacob",48,"무도가 지도자","mu0000057,mh007,ml255",null),
  new Profile("piet_investigator","Ethan",61,"피에트 조사 담당","mu0000030,mh009,ml243",null),
  new Profile("piet_supplier","Joshua",19,"피에트 보급 담당","mu0000237,mh124,ml240",null),
  new Profile("piet_purifier","Samuel",42,"정화 담당","mu0000202,mh008,ml243","mw026"),
  new Profile("interior_inn","Benjamin",39,"여관 주인","mu0000025,mh011,ml243",null)));
 public static Profile forId(String id){
  if(id==null||id.isEmpty())throw new IllegalArgumentException("NPC stable ID required");
  for(Profile p:ALL)if(p.id.equals(id))return p;
  int code=id.hashCode()&0x7fffffff,index=code%NAMES.length;
  Profile garment=ALL.get(code%ALL.size()),head=ALL.get((code/ALL.size())%ALL.size());
  String[] clothes=garment.outfit.split(","),accessory=head.outfit.split(",");
  return new Profile(id,NAMES[index],index+1,"주민",clothes[0]+","+accessory[1]+","+clothes[2],null);
 }
 public static String interiorKey(String kind){return "interior_"+kind.toLowerCase(Locale.ROOT);}
 public static String text(String original){if(original==null)return null;String value=original.replace("한스","윌리엄").replace("메리","벤저민").replace("멀린","헨리");for(int i=0;i<NAMES.length;i++)value=value.replace(NAMES[i],KOREAN_NAMES[i]);return value;}
 private NpcIdentity(){}
}
