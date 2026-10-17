package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import java.util.*;
/** Existing Master HP/EXP anchors; placement/count/attack tuning are explicitly ADAPTED. */
public final class HorrorCastleMonsters {
 public static final String[] NAMES={"남자전사","여자전사","남자도적","여자도적","남자마법사","여자마법사","남자성직자","여자성직자","남자무도가","여자무도가"};
 public static final int[] HP={120000,125000,85000,90000,100000,105000,110000,115000,80000,140000},EXP={183000,187000,154000,158000,167000,171000,175000,179000,150000,200000};
 public static boolean actor(String id){return id!=null&&id.startsWith("HORROR_");}
 public static int type(String id){if(!actor(id))return -1;try{return Integer.parseInt(id.substring(7,9));}catch(Exception e){return -1;}}
 private static final AbelCombatProfile[][] PROFILES=new AbelCombatProfile[10][4];
 public static AbelCombatProfile profile(String id){int t=type(id);if(t<0||t>9)return null;int element=0;try{element=Integer.parseInt(id.substring(10,11));}catch(Exception ignored){}if(PROFILES[t][element%4]!=null)return PROFILES[t][element%4];return PROFILES[t][element%4]=AbelCombatProfile.regional(id,99,HP[t],48+t*3,-25,15,new String[]{"FIRE","WATER","WIND","EARTH"}[element%4],"",EXP[t],0,1.8f);}
 public static List<RuntimeState.Monster> spawn(String map,long seed){List<RuntimeState.Monster> out=new ArrayList<>();int room=HorrorCastleGeometry.room(map);if(room==0)return out;Random random=new Random(seed^((long)room*0x9E3779B97F4A7C15L));List<WorldMoveTargetController.TileCenter> clear=new ArrayList<>(CampaignWorld.connectedTiles(map));Collections.shuffle(clear,random);WorldMoveTargetController.TileCenter entry=HorrorCastleGeometry.entry(map);
  for(int i=0;i<15;i++){WorldMoveTargetController.TileCenter pick=null;for(WorldMoveTargetController.TileCenter at:clear){if(Math.hypot(at.x-entry.x,at.y-entry.y)<130)continue;boolean close=false;for(RuntimeState.Monster m:out)if(Math.hypot(at.x-m.x,at.y-m.y)<64){close=true;break;}if(!close){pick=at;break;}}if(pick==null)throw new IllegalStateException("Horror spawn clearance "+map);int t=random.nextInt(10),e=random.nextInt(4);String species=String.format(Locale.ROOT,"HORROR_%02d_%d",t,e);RuntimeState.Monster m=new RuntimeState.Monster(species+"#"+room+"_"+i,NAMES[t],pick.x,pick.y,HP[t],"MASTER_U+B / ADAPTED_SOURCE_WARDROBE_CANDIDATE");m.level=99;m.campaignRewardProfileId=species;m.respawnSeconds=Float.POSITIVE_INFINITY;out.add(m);}
  return out;
 }
 private HorrorCastleMonsters(){}
}
