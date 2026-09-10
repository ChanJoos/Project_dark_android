package com.projectdark.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RPG runtime projection of master/data/Level_EXP_Curve.csv.
 * All 98 rows are Evidence.B balance values, not claimed original-game constants.
 */
public final class LevelExpCurveCatalog {
  public static final String SOURCE_PATH="master/data/Level_EXP_Curve.csv";
  public static final int MIN_LEVEL=1,MAX_PROJECTED_LEVEL=99;
  public static final long LEVEL_99_CUMULATIVE_EXP=150000000L;

  public static final class Entry {
    public final int currentLevel,nextLevel;
    public final String segment,primaryHunting;
    public final long requiredExp,cumulativeExp;
    public final RpgProgressionState.Evidence evidence;
    Entry(int currentLevel,int nextLevel,String segment,long requiredExp,long cumulativeExp,String primaryHunting,
        RpgProgressionState.Evidence evidence){
      this.currentLevel=currentLevel;this.nextLevel=nextLevel;this.segment=segment;
      this.requiredExp=requiredExp;this.cumulativeExp=cumulativeExp;this.primaryHunting=primaryHunting;this.evidence=evidence;
    }
  }

  private static final Map<Integer,Entry> BY_CURRENT_LEVEL=new LinkedHashMap<>();
  static {
    put(new Entry(1,2,"S1",10000L,10000L,"밀레스/초반",RpgProgressionState.Evidence.B));
    put(new Entry(2,3,"S1",12800L,22800L,"밀레스/초반",RpgProgressionState.Evidence.B));
    put(new Entry(3,4,"S1",16500L,39300L,"밀레스/초반",RpgProgressionState.Evidence.B));
    put(new Entry(4,5,"S1",21200L,60500L,"밀레스/초반",RpgProgressionState.Evidence.B));
    put(new Entry(5,6,"S1",27300L,87800L,"밀레스/초반",RpgProgressionState.Evidence.B));
    put(new Entry(6,7,"S1",35000L,122800L,"밀레스/초반",RpgProgressionState.Evidence.B));
    put(new Entry(7,8,"S1",45000L,167800L,"밀레스/초반",RpgProgressionState.Evidence.B));
    put(new Entry(8,9,"S1",57800L,225600L,"밀레스/초반",RpgProgressionState.Evidence.B));
    put(new Entry(9,10,"S1",74400L,300000L,"밀레스/초반",RpgProgressionState.Evidence.B));
    put(new Entry(10,11,"S2",78000L,378000L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(11,12,"S2",84200L,462200L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(12,13,"S2",90900L,553100L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(13,14,"S2",98000L,651100L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(14,15,"S2",105800L,756900L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(15,16,"S2",114200L,871100L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(16,17,"S2",123200L,994300L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(17,18,"S2",133000L,1127300L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(18,19,"S2",143500L,1270800L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(19,20,"S2",154800L,1425600L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(20,21,"S2",167100L,1592700L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(21,22,"S2",180300L,1773000L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(22,23,"S2",194600L,1967600L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(23,24,"S2",210000L,2177600L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(24,25,"S2",226600L,2404200L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(25,26,"S2",244600L,2648800L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(26,27,"S2",263900L,2912700L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(27,28,"S2",284800L,3197500L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(28,29,"S2",307300L,3504800L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(29,30,"S2",331600L,3836400L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(30,31,"S2",357900L,4194300L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(31,32,"S2",386200L,4580500L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(32,33,"S2",416800L,4997300L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(33,34,"S2",449800L,5447100L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(34,35,"S2",485400L,5932500L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(35,36,"S2",523800L,6456300L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(36,37,"S2",565200L,7021500L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(37,38,"S2",610000L,7631500L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(38,39,"S2",658200L,8289700L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(39,40,"S2",710300L,9000000L,"포테의숲",RpgProgressionState.Evidence.B));
    put(new Entry(40,41,"S3",745800L,9745800L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(41,42,"S3",757900L,10503700L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(42,43,"S3",770100L,11273800L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(43,44,"S3",782600L,12056400L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(44,45,"S3",795200L,12851600L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(45,46,"S3",808000L,13659600L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(46,47,"S3",821100L,14480700L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(47,48,"S3",834400L,15315100L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(48,49,"S3",847800L,16162900L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(49,50,"S3",861500L,17024400L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(50,51,"S3",875500L,17899900L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(51,52,"S3",889600L,18789500L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(52,53,"S3",904000L,19693500L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(53,54,"S3",918600L,20612100L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(54,55,"S3",933400L,21545500L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(55,56,"S3",948500L,22494000L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(56,57,"S3",963800L,23457800L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(57,58,"S3",979400L,24437200L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(58,59,"S3",995200L,25432400L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(59,60,"S3",1011300L,26443700L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(60,61,"S3",1027600L,27471300L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(61,62,"S3",1044200L,28515500L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(62,63,"S3",1061100L,29576600L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(63,64,"S3",1078200L,30654800L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(64,65,"S3",1095600L,31750400L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(65,66,"S3",1113300L,32863700L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(66,67,"S3",1131300L,33995000L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(67,68,"S3",1149600L,35144600L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(68,69,"S3",1168200L,36312800L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(69,70,"S3",1187200L,37500000L,"아벨해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(70,71,"S4",1246400L,38746400L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(71,72,"S4",1319000L,40065400L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(72,73,"S4",1395900L,41461300L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(73,74,"S4",1477200L,42938500L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(74,75,"S4",1563200L,44501700L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(75,76,"S4",1654300L,46156000L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(76,77,"S4",1750700L,47906700L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(77,78,"S4",1852700L,49759400L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(78,79,"S4",1960600L,51720000L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(79,80,"S4",2074800L,53794800L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(80,81,"S4",2195700L,55990500L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(81,82,"S4",2323600L,58314100L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(82,83,"S4",2459000L,60773100L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(83,84,"S4",2602200L,63375300L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(84,85,"S4",2753800L,66129100L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(85,86,"S4",2914300L,69043400L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(86,87,"S4",3084000L,72127400L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(87,88,"S4",3263700L,75391100L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(88,89,"S4",3453800L,78844900L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(89,90,"S4",3655100L,82500000L,"뤼케시온해안던전",RpgProgressionState.Evidence.B));
    put(new Entry(90,91,"S5",3837800L,86337800L,"뤼케시온해안 후반/99 준비",RpgProgressionState.Evidence.B));
    put(new Entry(91,92,"S5",4455400L,90793200L,"뤼케시온해안 후반/99 준비",RpgProgressionState.Evidence.B));
    put(new Entry(92,93,"S5",5172400L,95965600L,"뤼케시온해안 후반/99 준비",RpgProgressionState.Evidence.B));
    put(new Entry(93,94,"S5",6004800L,101970400L,"뤼케시온해안 후반/99 준비",RpgProgressionState.Evidence.B));
    put(new Entry(94,95,"S5",6971100L,108941500L,"뤼케시온해안 후반/99 준비",RpgProgressionState.Evidence.B));
    put(new Entry(95,96,"S5",8093000L,117034500L,"뤼케시온해안 후반/99 준비",RpgProgressionState.Evidence.B));
    put(new Entry(96,97,"S5",9395400L,126429900L,"뤼케시온해안 후반/99 준비",RpgProgressionState.Evidence.B));
    put(new Entry(97,98,"S5",10907400L,137337300L,"뤼케시온해안 후반/99 준비",RpgProgressionState.Evidence.B));
    put(new Entry(98,99,"S5",12662700L,150000000L,"뤼케시온해안 후반/99 준비",RpgProgressionState.Evidence.B));
    if(!audit())throw new IllegalStateException("Level EXP curve projection audit failed");
  }
  private LevelExpCurveCatalog(){}
  private static void put(Entry entry){BY_CURRENT_LEVEL.put(entry.currentLevel,entry);}

  public static Entry entryForCurrentLevel(int level){return BY_CURRENT_LEVEL.get(level);}
  public static Map<Integer,Entry> entries(){return Collections.unmodifiableMap(BY_CURRENT_LEVEL);}
  public static long cumulativeRequiredForLevel(int level){
    if(level==MIN_LEVEL)return 0L;
    Entry previous=BY_CURRENT_LEVEL.get(level-1);
    if(previous==null)throw new IllegalArgumentException("level");
    return previous.cumulativeExp;
  }
  public static Long requiredToNextLevel(int level){
    Entry entry=BY_CURRENT_LEVEL.get(level);return entry==null?null:entry.requiredExp;
  }
  public static int levelForCumulativeExp(long cumulativeExp){
    if(cumulativeExp<0L)throw new IllegalArgumentException("cumulativeExp");
    long clamped=Math.min(LEVEL_99_CUMULATIVE_EXP,cumulativeExp);
    int level=MIN_LEVEL;
    while(level<MAX_PROJECTED_LEVEL){
      Entry entry=BY_CURRENT_LEVEL.get(level);
      if(entry==null||clamped<entry.cumulativeExp)break;
      level=entry.nextLevel;
    }
    return level;
  }
  public static boolean audit(){
    if(BY_CURRENT_LEVEL.size()!=98)return false;
    long previous=0L;
    for(int level=1;level<99;level++){
      Entry entry=BY_CURRENT_LEVEL.get(level);
      if(entry==null||entry.currentLevel!=level||entry.nextLevel!=level+1||entry.requiredExp<=0L||
          entry.cumulativeExp!=previous+entry.requiredExp||entry.evidence!=RpgProgressionState.Evidence.B)return false;
      previous=entry.cumulativeExp;
    }
    return previous==LEVEL_99_CUMULATIVE_EXP;
  }
}
