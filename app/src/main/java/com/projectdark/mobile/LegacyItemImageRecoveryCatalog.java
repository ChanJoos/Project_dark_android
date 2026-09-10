package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recovery registry for legacy fan-archive images whose original CDN binaries are still blocked.
 * A page/section can prove that item-labelled imagery existed without claiming the binary itself was recovered.
 */
public final class LegacyItemImageRecoveryCatalog {
  public enum RecoveryStatus { PAGE_IMAGE_CONFIRMED, CDN_URL_KNOWN, BINARY_BLOCKED, BINARY_RECOVERED }

  public static final class Row {
    public final String itemId,itemName,pageUrl,section;
    public final List<String> legacyImageUrls;
    public final RecoveryStatus status;
    public final String provenance,note;

    Row(String itemId,String itemName,String pageUrl,String section,List<String> legacyImageUrls,
        RecoveryStatus status,String provenance,String note){
      this.itemId=itemId;this.itemName=itemName;this.pageUrl=pageUrl;this.section=section;
      this.legacyImageUrls=Collections.unmodifiableList(new ArrayList<>(legacyImageUrls));
      this.status=status;this.provenance=provenance;this.note=note;
    }

    public boolean binaryUsable(){return status==RecoveryStatus.BINARY_RECOVERED;}
  }

  public static final String WARRIOR_SETUP="https://minimob.tistory.com/99";
  private static final Map<String,Row> BY_ID;

  static {
    Map<String,Row> m=new LinkedHashMap<>();
    // The surviving page explicitly places these images inside the glove section immediately above
    // a table naming 흑요석워리어장갑 / 체력의고무장갑. Exact per-image identity is not asserted.
    add(m,"IT_GLOVE_LEATHER","가죽장갑","장갑",Collections.<String>emptyList(),
        RecoveryStatus.PAGE_IMAGE_CONFIRMED,"V","Canonical low-level glove is independently validated by Nexon historical guides; the high-value fan archive confirms glove imagery exists, but this article section does not individually label 가죽장갑 pixels.");
    add(m,"IT_GLOVE_COPPER","동장갑","장갑",Collections.<String>emptyList(),
        RecoveryStatus.PAGE_IMAGE_CONFIRMED,"V","Canonical glove lineage independently confirmed by Nexon; exact fan-archive image not yet isolated.");
    add(m,"IT_GLOVE_AQUA","아쿠아글러브","장갑",Collections.<String>emptyList(),
        RecoveryStatus.PAGE_IMAGE_CONFIRMED,"V","Canonical glove lineage independently confirmed by Nexon; exact fan-archive image not yet isolated.");
    add(m,"IT_GLOVE_SILVER","은장갑","장갑",Collections.<String>emptyList(),
        RecoveryStatus.PAGE_IMAGE_CONFIRMED,"V","Canonical glove lineage independently confirmed by Nexon; exact fan-archive image not yet isolated.");
    add(m,"IT_GLOVE_AQUALEATHER","아쿠아레더글러브","장갑",Collections.<String>emptyList(),
        RecoveryStatus.PAGE_IMAGE_CONFIRMED,"V","Canonical glove lineage independently confirmed by Nexon; exact fan-archive image not yet isolated.");
    add(m,"IT_RING_GOLDAQUA","골드아쿠아링","반지",Collections.<String>emptyList(),
        RecoveryStatus.PAGE_IMAGE_CONFIRMED,"V","The surviving fan article explicitly names 골드아쿠아링 in the ring comparison table and contains ring images in the same section; exact image-to-row identity still needs binary inspection.");
    BY_ID=Collections.unmodifiableMap(m);
  }

  private LegacyItemImageRecoveryCatalog(){}
  private static void add(Map<String,Row> m,String id,String name,String section,List<String> urls,
      RecoveryStatus status,String provenance,String note){
    m.put(id,new Row(id,name,WARRIOR_SETUP,section,urls,status,provenance,note));
  }
  public static Row get(String itemId){return BY_ID.get(itemId);}
  public static Map<String,Row> all(){return BY_ID;}
  public static List<Row> blocked(){
    List<Row> out=new ArrayList<>();
    for(Row r:BY_ID.values())if(!r.binaryUsable())out.add(r);
    return Collections.unmodifiableList(out);
  }
}
