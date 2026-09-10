package com.projectdark.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Character-owned evidence catalog for the first ItemVisualManifest acquisition batch: shields.
 * Text existence evidence and actual image evidence are intentionally separated.
 */
public final class ShieldVisualEvidenceCatalog {
  private ShieldVisualEvidenceCatalog(){}

  public enum EvidenceKind { OFFICIAL_TEXT, NEXON_HOSTED_COMMUNITY_TEXT, VERIFIED_IMAGE, NONE }

  public static final class Evidence {
    public final String itemId;
    public final String displayName;
    public final String textEvidenceUrl;
    public final EvidenceKind textEvidenceKind;
    public final String imageSourceUrl;
    public final String imageProvenance;

    Evidence(String itemId,String displayName,String textEvidenceUrl,EvidenceKind textEvidenceKind,
        String imageSourceUrl,String imageProvenance){
      this.itemId=itemId;this.displayName=displayName;this.textEvidenceUrl=textEvidenceUrl;
      this.textEvidenceKind=textEvidenceKind;this.imageSourceUrl=imageSourceUrl;
      this.imageProvenance=imageProvenance;
    }

    public boolean hasVerifiedImage(){return imageSourceUrl!=null&&imageSourceUrl.length()>0;}
  }

  /** Official Nexon probability page explicitly names 가죽방패. */
  public static final String OFFICIAL_PROBABILITY_URL="https://lod.nexon.com/cashshop/probability";

  /** Nexon-hosted game-board evidence enumerating the legacy/common shield family. [V] until independently official-verified. */
  public static final String SHIELD_LIST_BOARD_URL="https://lod.nexon.com/community/game/7009?SearchBoard=1";

  /** Nexon-hosted Papaya quest guide proving the item/reward name, but not an isolated shield sprite. */
  public static final String PAPAYA_GUIDE_URL="https://lod.nexon.com/community/game/791?SearchBoard=1";

  private static final Map<String,Evidence> BY_ID;
  static {
    Map<String,Evidence> m=new LinkedHashMap<>();
    m.put("IT_SHIELD_LEATHER",new Evidence("IT_SHIELD_LEATHER","가죽방패",OFFICIAL_PROBABILITY_URL,EvidenceKind.OFFICIAL_TEXT,null,null));
    m.put("IT_SHIELD_COPPER",new Evidence("IT_SHIELD_COPPER","구리방패",SHIELD_LIST_BOARD_URL,EvidenceKind.NEXON_HOSTED_COMMUNITY_TEXT,null,null));
    m.put("IT_SHIELD_IRON",new Evidence("IT_SHIELD_IRON","철방패",SHIELD_LIST_BOARD_URL,EvidenceKind.NEXON_HOSTED_COMMUNITY_TEXT,null,null));
    m.put("IT_SHIELD_SILVER",new Evidence("IT_SHIELD_SILVER","은제방패",SHIELD_LIST_BOARD_URL,EvidenceKind.NEXON_HOSTED_COMMUNITY_TEXT,null,null));
    m.put("IT_SHIELD_GOLD",new Evidence("IT_SHIELD_GOLD","금제방패",SHIELD_LIST_BOARD_URL,EvidenceKind.NEXON_HOSTED_COMMUNITY_TEXT,null,null));
    m.put("IT_SHIELD_PLANUM",new Evidence("IT_SHIELD_PLANUM","플라늄방패",SHIELD_LIST_BOARD_URL,EvidenceKind.NEXON_HOSTED_COMMUNITY_TEXT,null,null));
    m.put("IT_SHIELD_PAPAYA",new Evidence("IT_SHIELD_PAPAYA","파파야방패",PAPAYA_GUIDE_URL,EvidenceKind.NEXON_HOSTED_COMMUNITY_TEXT,null,null));
    BY_ID=Collections.unmodifiableMap(m);
  }

  public static Evidence get(String itemId){return BY_ID.get(itemId);}
  public static Map<String,Evidence> all(){return BY_ID;}
  public static int batchSize(){return BY_ID.size();}
  public static int verifiedImageCount(){
    int n=0;for(Evidence e:BY_ID.values())if(e.hasVerifiedImage())n++;return n;
  }
}
