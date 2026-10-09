package com.projectdark.mobile;

import java.util.*;

/** Mobile acquisition/economy policy, not original drop rates or original quest reconstruction. */
public final class AdaptedPoteQuestRewards {
  public static final String POLICY_ID="ADAPTED_POTE_EQUIPMENT_V123";
  private AdaptedPoteQuestRewards(){}
  public static int gearTier(String id){
    return "M06".equals(id)||id.startsWith("J02_")?11:
        "B06".equals(id)||id.startsWith("J03_")?26:0;
  }
  public static Map<String,Integer> items(CampaignProgress.Def d,RpgProgressionState r){
    Map<String,Integer> out=new LinkedHashMap<>();
    if(!d.id.startsWith("T")){out.put(RpgProgressionState.B_SMALL_POTION_ITEM_ID,3);out.put("IT_B_MP_POTION",3);}
    int tier=gearTier(d.id);
    if(tier>0&& !"COMMONER".equals(r.currentJobCode())){
      out.put("IT_B_CAMPAIGN_"+r.currentJobCode()+"_"+tier,1);
      out.put("IT_B_CAMPAIGN_TOOL_"+r.currentJobCode()+"_"+tier,1);
    }
    switch(d.id){
      case "A02":out.put("IT_LEGGING_LEATHER",1);break;
      case "A03":out.put("IT_GLOVE_LEATHER",1);break;
      case "A05":out.put("IT_NECK_WATER_PEARL",1);break;
      case "B01":out.put("IT_BELT_WATER_LEATHER",1);break;
      case "B04":element(out,"WIND");break;
      case "B05":out.put("IT_B_POTE_SHOES",1);break;
      case "C01":element(out,"EARTH");break;
      case "C04":element(out,"FIRE");break;
      case "C06":case "M15":out.put("IT_B_PURIFIED_ESSENCE",3);break;
      case "D03":out.put("IT_RING_THREELINEGOLD",1);break;
      default:break;
    }
    return Collections.unmodifiableMap(out);
  }
  private static void element(Map<String,Integer> out,String element){out.put("IT_NECK_"+element+"_PEARL",1);out.put("IT_BELT_"+element+"_LEATHER",1);}
  public static long gold(CampaignProgress.Def d){
    if("D03".equals(d.id))return 10000;
    if(d.id.matches("A\\d\\d"))return 150;
    if(d.id.matches("B\\d\\d"))return 250;
    if(d.id.matches("C\\d\\d"))return 350;
    if(d.id.matches("D\\d\\d"))return 500;
    return d.gold;
  }
  public static String equipmentText(CampaignProgress.Def d,RpgProgressionState r){
    StringBuilder out=new StringBuilder();
    for(Map.Entry<String,Integer> e:items(d,r).entrySet()){
      RpgProgressionState.ItemDefinition item=r.itemDefinitions().get(e.getKey());
      if(item==null||!item.equippable())continue;
      if(out.length()>0)out.append(" · ");out.append(item.name);
    }
    return out.toString();
  }
  public static String summary(CampaignProgress.Def d,RpgProgressionState r,int exp){
    String gear=equipmentText(d,r);
    return "EXP +"+String.format(Locale.ROOT,"%,d",exp)+" · Gold +"+String.format(Locale.ROOT,"%,d",gold(d))+(gear.isEmpty()?"":" · "+gear+" (가방)");
  }
}
