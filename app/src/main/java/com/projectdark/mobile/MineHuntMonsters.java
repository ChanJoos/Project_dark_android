package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import java.util.*;
/** Original MPF appearances; numeric profiles are explicit mobile balance, not server statistics. */
public final class MineHuntMonsters {
 public static final String POLICY="MINE_V208_MOBILE_BALANCE";
 public static final String[] NAMES={"광산 적발 병사","광산 적발 검병","광산 적발 근위병","그림록퀸","드라코","헬몽크","헬블루나이트","헬소서리스","헬소서러","헬블씨프"};
 public static final int[] HP={150000,170000,190000,240000,360000,260000,320000,210000,280000,230000};
 private static final String[] SPELLS={"","MONSTER_CURSE","MONSTER_VENOM","MONSTER_ROOT","MONSTER_FIRE","","","MONSTER_FREEZE","MONSTER_FIRE","MONSTER_BLIND"};
 private static final String[] ELEMENTS={"EARTH","WATER","WIND","EARTH","LIFE","FIRE","FIRE","WATER","FIRE","WIND"};
 private static final Map<String,AbelCombatProfile> PROFILES=new LinkedHashMap<>();
 static {for(int t=0;t<10;t++){String s=species(t);PROFILES.put(s,AbelCombatProfile.regional(s,99,HP[t],75+t*9,-30-t*3,18+t*2,ELEMENTS[t],SPELLS[t],1400000+t*120000,800+t*90,1.65f+t*.065f));}}
 public static String species(int t){return String.format(Locale.ROOT,"MINE_%02d",t);}
 public static int type(String id){if(id==null||!id.startsWith("MINE_"))return -1;try{int t=Integer.parseInt(id.substring(5,7));return t>=0&&t<10?t:-1;}catch(Exception e){return -1;}}
 public static boolean actor(String id){return type(id)>=0;}
 public static AbelCombatProfile profile(String id){int t=type(id);return t<0?null:PROFILES.get(species(t));}
 public static Collection<AbelCombatProfile> all(){return Collections.unmodifiableCollection(PROFILES.values());}
 /** Source-confirmed/visually compatible worn-armor relations; probabilities are mobile-only. */
 public static String armor(String id){switch(type(id)){case 5:return "USER_ITEM_20261018_R01490";case 6:return "USER_ITEM_20261018_R01494";case 7:return "USER_ITEM_20261018_R01489";case 8:return "USER_ITEM_20261018_R01482";case 9:return "USER_ITEM_20261018_R01486";default:return null;}}
 public static String weapon(String id){return type(id)==6?"USER_ITEM_20261018_R01533":type(id)==9?"USER_ITEM_20261018_R05502":null;}
 public static int dropRoll(String actorId,long defeatSequence){long h=defeatSequence*0x9E3779B97F4A7C15L+actorId.hashCode();h^=h>>>33;h*=0xff51afd7ed558ccdL;h^=h>>>33;return (int)Math.floorMod(h,10000);}
 public static List<RuntimeState.Monster> spawn(){List<RuntimeState.Monster> out=new ArrayList<>();List<WorldMoveTargetController.TileCenter> cells=new ArrayList<>(CampaignWorld.connectedTiles(MineHuntGeometry.ID));Collections.shuffle(cells,new Random(208));
  for(int i=0;i<100;i++){WorldMoveTargetController.TileCenter pick=null;for(WorldMoveTargetController.TileCenter at:cells){if(Math.hypot(at.x-MineHuntGeometry.entry().x,at.y-MineHuntGeometry.entry().y)<220)continue;boolean occupied=false;for(RuntimeState.Monster m:out)if(Math.hypot(m.x-at.x,m.y-at.y)<90){occupied=true;break;}if(!occupied){pick=at;break;}}if(pick==null)throw new IllegalStateException("Mine spawn clearance");int t=i%10;RuntimeState.Monster m=new RuntimeState.Monster(species(t)+"#"+i,NAMES[t],pick.x,pick.y,HP[t],"ORIGINAL_MPF / "+POLICY);m.level=99;m.campaignRewardProfileId=species(t);m.respawnSeconds=18+t;out.add(m);}return out;
 }
 private MineHuntMonsters(){}
}
