package com.projectdark.mobile;

/**
 * Character-owned provenance seed for item visual acquisition from the official Legend of Darkness site.
 * This does not claim that text-only pages expose renderable sprites; image identity must still be verified.
 */
public final class OfficialItemHomepageSourceCatalog {
  private OfficialItemHomepageSourceCatalog(){}

  public static final String HOME_URL="https://lod.nexon.com/home";
  public static final String SITEMAP_URL="https://lod.nexon.com/sitemap";
  public static final String GUIDE_ROOT_URL="https://lod.nexon.com/info/guide/82286";
  public static final String PROBABILITY_URL="https://lod.nexon.com/cashshop/probability";

  /** Official structured item-name source with large manufacturing/equipment result coverage. */
  public static final String PROBABILITY_SOURCE_STATUS="OFFICIAL_TEXT_SOURCE";

  /**
   * The public sitemap currently exposes Game Info/Guide rather than a separately indexed normal-item DB route.
   * Acquisition should therefore crawl official guide/news/cashshop pages and Nexon-hosted image attachments,
   * then bind only images whose visible item identity is positively established.
   */
  public static final String PUBLIC_ITEM_DB_ROUTE_STATUS="NOT_EXPOSED_IN_PUBLIC_SITEMAP";

  public static boolean isOfficialNexonLodUrl(String url){
    return url!=null&&(url.startsWith("https://lod.nexon.com/")
        ||url.startsWith("https://storage.nexon.com/")
        ||url.startsWith("https://lwi.nexon.com/lod/"));
  }

  public static boolean textEvidenceCanAuthorizeRender(){return false;}
}
