package com.projectdark.mobile;
import com.projectdark.mobile.world.*;import java.util.*;
/** Raw original MPFs. Korean name joins except zombie and original server balance are still pending. */
public final class DeathVillageMonsters {
 public static final String POLICY="DEATH_V217_MOBILE_BALANCE";
 public static final String[] NAMES={"좀비","니크르","웨어렛","독거미"};
 private static final int[] HP={30000,200000,220000,200000};private static final Map<String,AbelCombatProfile> PROFILES=new LinkedHashMap<>();
 static{for(int i=0;i<4;i++)PROFILES.put(species(i),AbelCombatProfile.regional(species(i),99,HP[i],40+i*22,-15-i*8,8+i*10,new String[]{"EARTH","WIND","WATER","FIRE"}[i],new String[]{"","MONSTER_CURSE","MONSTER_FREEZE","MONSTER_VENOM"}[i],300000+i*150000,450+i*180,1.8f));}
 public static String species(int t){return "DEATH_0"+t;}public static int type(String id){if(id==null||!id.startsWith("DEATH_0")||id.length()<8)return -1;int n=id.charAt(7)-'0';return n>=0&&n<4?n:-1;}
 public static boolean actor(String id){return type(id)>=0;}public static AbelCombatProfile profile(String id){int t=type(id);return t<0?null:PROFILES.get(species(t));}public static Collection<AbelCombatProfile> all(){return Collections.unmodifiableCollection(PROFILES.values());}
 public static List<RuntimeState.Monster> spawn(String id){if(DeathVillageGeometry.LOBBY.equals(id))return Collections.emptyList();int count=DeathVillageGeometry.FIELD.equals(id)?72:DeathVillageGeometry.house(id)?15:36;List<WorldMoveTargetController.TileCenter> cells=new ArrayList<>(DeathVillageGeometry.tiles(id,true));Collections.shuffle(cells,new Random(id.hashCode()));List<RuntimeState.Monster> out=new ArrayList<>();List<WorldMoveTargetController.TileCenter> safe=new ArrayList<>();safe.add(DeathVillageGeometry.entry(id));for(DeathVillageData.Gate g:DeathVillageGeometry.gates(id))safe.add(DeathVillageGeometry.at(g.u,g.v));
  for(WorldMoveTargetController.TileCenter p:cells){boolean bad=false;for(WorldMoveTargetController.TileCenter t:safe)if(Math.hypot(p.x-t.x,p.y-t.y)<100){bad=true;break;}for(RuntimeState.Monster m:out)if(Math.hypot(p.x-m.x,p.y-m.y)<58){bad=true;break;}if(bad)continue;int t=DeathVillageGeometry.FIELD.equals(id)?0:DeathVillageGeometry.house(id)?(out.size()%3==2?3:1):2;RuntimeState.Monster m=new RuntimeState.Monster(species(t)+"#"+out.size(),NAMES[t],p.x,p.y,HP[t],"ORIGINAL_MPF / "+POLICY);m.level=99;m.campaignRewardProfileId=species(t);m.respawnSeconds=18;out.add(m);if(out.size()==count)break;}if(out.size()!=count)throw new IllegalStateException("Death population clearance "+id+" "+out.size());return out;
 }
 private DeathVillageMonsters(){}
}
