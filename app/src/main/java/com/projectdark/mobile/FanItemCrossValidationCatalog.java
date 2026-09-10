package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cross-validates canonical item names against historical Nexon-board text and surviving fan archives.
 * Text confirmation never implies image confirmation; image promotion still requires exact visual identity.
 */
public final class FanItemCrossValidationCatalog {
  public enum Confidence { SINGLE_SOURCE, CROSS_VALIDATED_TEXT, IMAGE_SECTION_MATCH, IMAGE_ITEM_CONFIRMED }

  public static final class Row {
    public final String itemId,name,family;
    public final String primaryTextUrl,secondaryTextUrl,visualArchiveUrl;
    public final Confidence confidence;

    Row(String itemId,String name,String family,String primaryTextUrl,String secondaryTextUrl,
        String visualArchiveUrl,Confidence confidence){
      this.itemId=itemId;this.name=name;this.family=family;
      this.primaryTextUrl=primaryTextUrl;this.secondaryTextUrl=secondaryTextUrl;
      this.visualArchiveUrl=visualArchiveUrl;this.confidence=confidence;
    }

    public boolean exactImageConfirmed(){return confidence==Confidence.IMAGE_ITEM_CONFIRMED;}
  }

  public static final String NEXON_LOW_LEVEL_GEAR="https://lod.nexon.com/Community/game/6222?Category2=1&SearchBoard=1";
  public static final String NEXON_GLOVE_STATS="https://lod.nexon.com/community/game/1702?SearchBoard=1";
  public static final String NEXON_GLOVE_TIP="https://lod.nexon.com/Community/game/1643?Category2=1&SearchBoard=1";
  public static final String FAN_LEVELING_ARCHIVE="https://minimob.tistory.com/125";
  public static final String FAN_ITEM_ARCHIVE="https://minimob.tistory.com/99";
  public static final String HYUNJA_MIRROR="https://lod-ontime.tistory.com/";

  private static final Map<String,Row> BY_ID;
  static {
    Map<String,Row> m=new LinkedHashMap<>();
    add(m,"IT_GLOVE_LEATHER","가죽장갑","장갑",NEXON_LOW_LEVEL_GEAR,NEXON_GLOVE_STATS,FAN_ITEM_ARCHIVE,Confidence.CROSS_VALIDATED_TEXT);
    add(m,"IT_GLOVE_COPPER","동장갑","장갑",NEXON_GLOVE_STATS,NEXON_GLOVE_TIP,FAN_ITEM_ARCHIVE,Confidence.CROSS_VALIDATED_TEXT);
    add(m,"IT_GLOVE_AQUA","아쿠아글러브","장갑",NEXON_GLOVE_STATS,NEXON_GLOVE_TIP,FAN_ITEM_ARCHIVE,Confidence.CROSS_VALIDATED_TEXT);
    add(m,"IT_GLOVE_SILVER","은장갑","장갑",NEXON_GLOVE_STATS,NEXON_GLOVE_TIP,FAN_ITEM_ARCHIVE,Confidence.CROSS_VALIDATED_TEXT);
    add(m,"IT_GLOVE_AQUALEATHER","아쿠아레더글러브","장갑",NEXON_GLOVE_STATS,NEXON_GLOVE_TIP,FAN_ITEM_ARCHIVE,Confidence.CROSS_VALIDATED_TEXT);
    add(m,"IT_LEGGING_LEATHER","가죽각반","각반",NEXON_LOW_LEVEL_GEAR,"https://lod-ontime.tistory.com/15",FAN_ITEM_ARCHIVE,Confidence.CROSS_VALIDATED_TEXT);
    add(m,"IT_SHOES","신발","신발",NEXON_LOW_LEVEL_GEAR,FAN_LEVELING_ARCHIVE,FAN_ITEM_ARCHIVE,Confidence.CROSS_VALIDATED_TEXT);
    add(m,"IT_SHOES_GRAY","회색신발","신발",NEXON_LOW_LEVEL_GEAR,FAN_LEVELING_ARCHIVE,FAN_ITEM_ARCHIVE,Confidence.CROSS_VALIDATED_TEXT);
    add(m,"IT_SHIELD_LEATHER","가죽방패","방패",NEXON_LOW_LEVEL_GEAR,"https://lod-ontime.tistory.com/",FAN_ITEM_ARCHIVE,Confidence.CROSS_VALIDATED_TEXT);
    add(m,"IT_SHIELD_COPPER","구리방패","방패",NEXON_LOW_LEVEL_GEAR,FAN_ITEM_ARCHIVE,FAN_ITEM_ARCHIVE,Confidence.CROSS_VALIDATED_TEXT);
    BY_ID=Collections.unmodifiableMap(m);
  }

  private FanItemCrossValidationCatalog(){}
  private static void add(Map<String,Row> m,String id,String name,String family,String a,String b,String visual,Confidence c){
    m.put(id,new Row(id,name,family,a,b,visual,c));
  }
  public static Row get(String itemId){return BY_ID.get(itemId);}
  public static Map<String,Row> all(){return BY_ID;}
  public static List<Row> byFamily(String family){
    List<Row> out=new ArrayList<>();
    for(Row row:BY_ID.values())if(family.equals(row.family))out.add(row);
    return Collections.unmodifiableList(out);
  }
  public static int exactImageConfirmedCount(){int n=0;for(Row r:BY_ID.values())if(r.exactImageConfirmed())n++;return n;}
}
