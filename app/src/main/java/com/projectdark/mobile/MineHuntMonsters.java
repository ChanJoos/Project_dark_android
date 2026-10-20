package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import java.util.*;
/** Original MPF appearances; numeric profiles are explicit mobile balance, not server statistics. */
public final class MineHuntMonsters {
 public static final String POLICY="MINE_V208_MOBILE_BALANCE";
 public static final String[] NAMES={"광산 적발 병사","광산 적발 검병","광산 적발 근위병","그림록퀸","드라코","헬블씨프","헬블루나이트","헬소서러스","헬몽크","헬소서러"};
 public static final int[] HP={150000,170000,190000,240000,360000,260000,320000,210000,280000,310000};
 private static final String[] SPELLS={"","MONSTER_CURSE","MONSTER_VENOM","MONSTER_ROOT","MONSTER_FIRE","MONSTER_BLIND","","MONSTER_FREEZE","","MONSTER_FIRE"};
 private static final String[] ELEMENTS={"EARTH","WATER","WIND","EARTH","LIFE","WIND","FIRE","WATER","FIRE","FIRE"};
 public static final int COUNT=NAMES.length;
 private static final Map<String,AbelCombatProfile> PROFILES=new LinkedHashMap<>();
 static {for(int t=0;t<COUNT;t++){String s=species(t);PROFILES.put(s,AbelCombatProfile.regional(s,99,HP[t],75+t*9,-30-t*3,18+t*2,ELEMENTS[t],SPELLS[t],1400000+t*120000,800+t*90,1.65f+t*.065f));}}
 public static String species(int t){return String.format(Locale.ROOT,"MINE_%02d",t);}
 public static int type(String id){if(id==null||!id.startsWith("MINE_"))return -1;try{int t=Integer.parseInt(id.substring(5,7));return t>=0&&t<COUNT?t:-1;}catch(Exception e){return -1;}}
 public static boolean actor(String id){return type(id)>=0;}
 public static AbelCombatProfile profile(String id){int t=type(id);return t<0?null:PROFILES.get(species(t));}
 public static Collection<AbelCombatProfile> all(){return Collections.unmodifiableCollection(PROFILES.values());}
 /** Source-confirmed/visually compatible worn-armor relations; probabilities are mobile-only. */
 public static String armor(String id){switch(type(id)){case 8:return "USER_ITEM_20261018_R01490";case 6:return "USER_ITEM_20261018_R01494";case 7:case 9:return "USER_ITEM_20261018_R01489";case 5:return "USER_ITEM_20261018_R01486";default:return null;}}
 public static List<String> armors(String id){String armor=armor(id);if(armor==null)return Collections.emptyList();return type(id)==7||type(id)==9?Arrays.asList(armor,"USER_ITEM_20261018_R01482"):Collections.singletonList(armor);}
 public static String weapon(String id){return type(id)==6?"USER_ITEM_20261018_R01533":type(id)==5?"USER_ITEM_20261018_R05502":null;}
 public static int dropRoll(String actorId,long defeatSequence){long h=defeatSequence*0x9E3779B97F4A7C15L+actorId.hashCode();h^=h>>>33;h*=0xff51afd7ed558ccdL;h^=h>>>33;return (int)Math.floorMod(h,10000);}
 public static List<RuntimeState.Monster> spawn(){List<RuntimeState.Monster> out=new ArrayList<>();List<WorldMoveTargetController.TileCenter> cells=new ArrayList<>(CampaignWorld.connectedTiles(MineHuntGeometry.ID));Collections.shuffle(cells,new Random(208));
  List<WorldMoveTargetController.TileCenter> welcome=new ArrayList<>(cells);WorldMoveTargetController.TileCenter entry=MineHuntGeometry.entry();welcome.removeIf(at->Math.abs(at.x-entry.x)>420||Math.abs(at.y-entry.y)>170||Math.hypot(at.x-entry.x,at.y-entry.y)<180);welcome.sort(Comparator.comparingDouble(at->Math.hypot(at.x-entry.x,at.y-entry.y)));
  for(int i=0;i<100;i++){WorldMoveTargetController.TileCenter pick=null;for(WorldMoveTargetController.TileCenter at:i<COUNT?welcome:cells){if(Math.hypot(at.x-entry.x,at.y-entry.y)<180)continue;boolean occupied=false;for(RuntimeState.Monster m:out)if(Math.hypot(m.x-at.x,m.y-at.y)<90){occupied=true;break;}if(!occupied){pick=at;break;}}if(pick==null)throw new IllegalStateException("Mine spawn clearance");int t=i%COUNT;RuntimeState.Monster m=new RuntimeState.Monster(species(t)+"#"+i,NAMES[t],pick.x,pick.y,HP[t],"ORIGINAL_MPF / "+POLICY);m.level=99;m.campaignRewardProfileId=species(t);m.respawnSeconds=18+t;out.add(m);}return out;
 }
 private MineHuntMonsters(){}
}
