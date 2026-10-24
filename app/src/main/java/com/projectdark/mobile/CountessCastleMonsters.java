package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import java.util.*;
/** Video roster/source pixels; unobserved server stats and counts are ADAPTED test fixtures. */
public final class CountessCastleMonsters {
 public static final String POLICY="V273_VIDEO_ROSTER_ADAPTED_SERVER_STATS";
 public static final String[] NAMES={"유령","인형","별장의 푸른 기사","별장의 금빛 기사","움직이는 가구","검은 큐브","붉은 큐브","푸른 큐브","금빛 큐브","엘리자베스","블러드조나단백작","블러드조나단백작","백작부인다이애나","폭탄"};
 public static String species(int t){return String.format(Locale.ROOT,"COUNTESS_%02d",t);}
 public static int type(String id){if(id==null||!id.startsWith("COUNTESS_")||id.length()<11)return -1;try{int t=Integer.parseInt(id.substring(9,11));return t>=0&&t<NAMES.length?t:-1;}catch(Exception e){return -1;}}
 public static boolean actor(String id){return type(id)>=0;}
 private static final Map<String,AbelCombatProfile> PROFILES=new LinkedHashMap<>();
 static{for(int t=0;t<NAMES.length;t++){int hp=t>=10?500000:t>=9?220000:120000+t*10000;String spell=t==0?"":t==1||t==9||t==10?"MONSTER_NARCOLI":t==11?"MONSTER_DISPEL":t==12?"MONSTER_FIRE":t==13?"MONSTER_FIRE":t==4?"MONSTER_ROOT":"MONSTER_VENOM";PROFILES.put(species(t),AbelCombatProfile.regional(species(t),99,hp,50+t*4,-25,12,"DARK",spell,t==10?0:t>=11?1000000:500000,0,1.8f));}}
 public static Collection<AbelCombatProfile> all(){return Collections.unmodifiableCollection(PROFILES.values());}public static AbelCombatProfile profile(String id){int t=type(id);return t<0?null:PROFILES.get(species(t));}
 public static int[] roster(String id){int z=CountessCastleGeometry.zone(id);if(CountessCastleGeometry.BOSS.equals(id)||z==12)return new int[]{10,11,12,13,0,1,9};if(z<=0)return new int[0];if(z<=2)return new int[]{0};if(z<=4)return new int[]{1};if(z<=5)return new int[]{2,3};if(z<=7)return new int[]{2,3,4};if(z<=9)return new int[]{5,6,7,8};return new int[]{9,5,7,8};}
 public static List<RuntimeState.Monster> spawn(String id){int[] types=roster(id);List<RuntimeState.Monster> out=new ArrayList<>();if(types.length==0)return out;boolean boss=CountessCastleGeometry.BOSS.equals(id)||CountessCastleGeometry.zone(id)==12;List<WorldMoveTargetController.TileCenter> cells=new ArrayList<>(CountessCastleGeometry.tiles(id,true));Collections.shuffle(cells,new Random(id.hashCode()));int count=boss?22:28;
  for(WorldMoveTargetController.TileCenter p:cells){if(Math.hypot(p.x-CountessCastleGeometry.entry(id).x,p.y-CountessCastleGeometry.entry(id).y)<130)continue;boolean blocked=false;for(CountessCastleGeometry.Gate g:CountessCastleGeometry.gates(id))if(Math.hypot(p.x-g.x,p.y-g.y)<90)blocked=true;for(RuntimeState.Monster m:out)if(Math.hypot(p.x-m.x,p.y-m.y)<58)blocked=true;if(blocked)continue;int t=types[out.size()%types.length];if(boss&&out.size()>=4)t=types[4+(out.size()-4)%3];AbelCombatProfile profile=PROFILES.get(species(t));RuntimeState.Monster m=new RuntimeState.Monster(species(t)+"#"+id+"_"+out.size(),NAMES[t],p.x,p.y,profile.hp,"ORIGINAL_MPF / "+POLICY);m.level=99;m.mp=m.maxMp=profile.mp;m.campaignRewardProfileId=species(t);m.respawnSeconds=Float.POSITIVE_INFINITY;if(t==11||t==13){m.alive=false;m.hp=0;m.respawnClock=Float.POSITIVE_INFINITY;}out.add(m);if(out.size()==count)break;}
  if(out.size()!=count)throw new IllegalStateException("Countess spawn clearance "+id+" "+out.size());return out;
 }
 private CountessCastleMonsters(){}
}
