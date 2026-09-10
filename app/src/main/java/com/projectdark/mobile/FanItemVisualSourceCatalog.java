package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Character-owned index of fan/community item pages that contain actual item imagery.
 * These sources are visual candidates only and never override canonical Item_Master identity.
 */
public final class FanItemVisualSourceCatalog {
  public enum TrustTier { NEXON_BOARD, ESTABLISHED_FAN_ARCHIVE, MIRROR_BLOG, UNKNOWN }
  public enum Coverage { WEAPON, ARMOR, RING, GLOVE, LEGGING, NECKLACE, BELT, MIXED }

  public static final class Source {
    public final String sourceId,title,pageUrl,host;
    public final TrustTier trustTier;
    public final Coverage coverage;
    public final int embeddedImageCount;
    public final boolean itemNamesAdjacentToImages;
    public final String note;

    Source(String sourceId,String title,String pageUrl,String host,TrustTier trustTier,
        Coverage coverage,int embeddedImageCount,boolean adjacent,String note){
      this.sourceId=sourceId;this.title=title;this.pageUrl=pageUrl;this.host=host;
      this.trustTier=trustTier;this.coverage=coverage;this.embeddedImageCount=embeddedImageCount;
      this.itemNamesAdjacentToImages=adjacent;this.note=note;
    }
  }

  private static final List<Source> SOURCES;
  static {
    List<Source> rows=new ArrayList<>();
    rows.add(new Source(
        "FAN_MINIMOB_WARRIOR_2012",
        "비승급 전사의 아이템 셋팅",
        "https://minimob.tistory.com/99",
        "minimob.tistory.com",
        TrustTier.ESTABLISHED_FAN_ARCHIVE,
        Coverage.MIXED,
        50,
        true,
        "Large surviving visual archive. Page contains many Daum/Tistory-hosted item images grouped by weapon, armor, ring, glove, legging, necklace and belt sections. CDN images currently return HTTP 403 to automated fetch, so URLs are preserved as acquisition candidates rather than treated as extracted assets."));
    rows.add(new Source(
        "FAN_LOD_ONTIME_LEVEL_REWARD",
        "어둠의전설 ON타임 / 현자의마을 앨시 자료 미러",
        "https://lod-ontime.tistory.com/",
        "lod-ontime.tistory.com",
        TrustTier.MIRROR_BLOG,
        Coverage.MIXED,
        0,
        false,
        "Useful mirror/index for canonical item-name relationships and attribution to 현자의마을 앨시. Use to locate original visual posts, not as automatic image truth."));
    rows.add(new Source(
        "NEXON_RELIC_REWARD_2019",
        "퀵던전 유물함 보상",
        "https://lod.nexon.com/community/game/7009?SearchBoard=1",
        "lod.nexon.com",
        TrustTier.NEXON_BOARD,
        Coverage.MIXED,
        0,
        false,
        "Strong name/family cross-reference for gloves, leggings, shoes, shields, earrings and rings."));
    rows.add(new Source(
        "NEXON_SHIELD_AC_2005",
        "방패별 AC",
        "https://lod.nexon.com/community/game/1487?SearchBoard=1",
        "lod.nexon.com",
        TrustTier.NEXON_BOARD,
        Coverage.MIXED,
        0,
        false,
        "Legacy shield lineage/name validation including leather/copper/iron/silver/gold/planum/papaya."));
    SOURCES=Collections.unmodifiableList(rows);
  }

  private FanItemVisualSourceCatalog(){}
  public static List<Source> all(){return SOURCES;}
  public static List<Source> visualSources(){
    List<Source> out=new ArrayList<>();
    for(Source s:SOURCES)if(s.embeddedImageCount>0)out.add(s);
    return Collections.unmodifiableList(out);
  }
  public static int knownEmbeddedImageCount(){int n=0;for(Source s:SOURCES)n+=s.embeddedImageCount;return n;}
}
