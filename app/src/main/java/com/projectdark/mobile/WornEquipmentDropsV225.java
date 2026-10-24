package com.projectdark.mobile;

import java.util.*;

/** U: visually identified worn equipment may drop. Rates are temporary B values, never original facts. */
public final class WornEquipmentDropsV225 {
  public static final String POLICY="USER_WORN_EQUIPMENT_V225";
  public static final int ROLL_SIZE=10000, WEAPON_RATE=200, ARMOR_RATE=350, HAT_RATE=350;
  public static final class Drop {
    public final String itemId,slot;
    public final int rate;
    public final Float originalProbability=null;
    private Drop(String suffix,String slot,int rate){this.itemId="USER_ITEM_20261018_"+suffix;this.slot=slot;this.rate=rate;}
    public boolean awarded(String actorId,long sequence){return MineHuntMonsters.dropRoll(actorId+":"+itemId,sequence)<rate;}
  }
  private static final Map<String,List<Drop>> TABLE;
  static {
    Map<String,List<Drop>> m=new LinkedHashMap<>();
    put(m,"MINE_03",weapon("R05210")); // Grimlock Queen: retained native short sword / Deuvril.
    put(m,"MINE_05",armor("R01486"),hat("R01485"),weapon("R05502"));
    put(m,"MINE_06",armor("R01494"),hat("R01493"),weapon("R01533"));
    put(m,"MINE_07",armor("R01487")); // Female sorceress; never also priest/male robes.
    put(m,"MINE_08",armor("R01490"));
    put(m,"MINE_09",armor("R01489"),hat("R01488"));
    // Original MPF comparisons: male Hell armor36/37/38/39/40, female mage wu38;
    // pink blade mw014, purple star staff mw026, orange staff mw060.
    // Other female armor IDs, warrior blade and unlabelled hats are unresolved, not substitutes.
    put(m,"HORROR_00",armor("R01494"));
    put(m,"HORROR_02",armor("R01486"),weapon("R01591"));
    put(m,"HORROR_03",weapon("R01591"));
    put(m,"HORROR_04",armor("R01489"),weapon("R04330"));
    put(m,"HORROR_05",armor("R01487"),weapon("R04330"));
    put(m,"HORROR_06",armor("R01482"),weapon("R02939"));
    put(m,"HORROR_07",weapon("R02939"));
    put(m,"HORROR_08",armor("R01490"));
    TABLE=Collections.unmodifiableMap(m);
  }
  private static Drop weapon(String id){return new Drop(id,RpgProgressionState.WEAPON_SLOT,WEAPON_RATE);}
  private static Drop armor(String id){return new Drop(id,RpgProgressionState.ARMOR_SLOT,ARMOR_RATE);}
  private static Drop hat(String id){return new Drop(id,"모자",HAT_RATE);}
  private static void put(Map<String,List<Drop>> m,String species,Drop... rows){m.put(species,Collections.unmodifiableList(Arrays.asList(rows)));}
  public static List<Drop> forSpecies(String species){
    if(species==null)return Collections.emptyList();
    // Element variants share the same worn appearance, but never cross job or gender.
    String key=species.matches("HORROR_\\d{2}_[0-3]")?species.substring(0,9):species;
    List<Drop> result=TABLE.get(key);return result==null?Collections.emptyList():result;
  }
  public static Map<String,List<Drop>> entries(){return TABLE;}
  private WornEquipmentDropsV225(){}
}
