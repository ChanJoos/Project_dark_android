package com.projectdark.mobile;
import java.util.*;
/** ADAPTED presentation identities. Stable domain IDs, quests and saves remain authoritative. */
public final class NpcIdentity {
 public static final String NAME_SOURCE="SSA_US_1926_2025_TOP100";
 public static final class Profile {
  public final String id,name,role,outfit,weapon; public final int rank;
  Profile(String id,String name,int rank,String role,String outfit,String weapon){this.id=id;this.name=name;this.rank=rank;this.role=role;this.outfit=outfit;this.weapon=weapon;}
  public String label(){return name+" · "+role;}
 }
 private static final String[] NAMES={"James","Michael","John","Robert","David","William","Richard","Joseph","Thomas","Christopher","Charles","Daniel","Matthew","Anthony","Mark","Steven","Andrew","Donald","Joshua","Paul","Kevin","Kenneth","Brian","Timothy","Ronald","Jason","George","Edward","Jeffrey","Jacob","Ryan","Nicholas","Gary","Eric","Jonathan","Stephen","Larry","Justin","Benjamin","Scott","Brandon","Samuel","Alexander","Gregory","Patrick","Jack","Frank","Raymond","Dennis","Aaron","Tyler","Jerry","Jose","Nathan","Adam","Henry","Zachary","Douglas","Peter","Noah","Ethan","Kyle","Christian","Jeremy","Austin","Keith","Sean","Terry","Roger","Dylan","Walter","Gerald","Jordan","Gabriel","Carl","Bryan","Jesse","Logan","Lawrence","Elijah","Arthur","Bruce","Harold","Billy","Liam","Alan","Juan","Joe","Mason","Lucas","Randy","Willie","Wayne","Vincent","Caleb","Albert","Luke","Isaac","Bradley","Cameron"};
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
 public static String text(String original){if(original==null)return null;return original.replace("제임스","James").replace("한스","William").replace("메리","Benjamin").replace("멀린","Henry").replace("로건","Logan");}
 private NpcIdentity(){}
}
