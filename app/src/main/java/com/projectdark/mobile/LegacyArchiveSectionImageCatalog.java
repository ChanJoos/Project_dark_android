package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Character-owned index of surviving legacy Tistory/Daum image URLs from the minimob item archive.
 * These rows prove section-level visual presence only. HTTP 403 currently blocks binary recovery,
 * so no row is canonical-item-authoritative until the image can be inspected and paired to a label.
 */
public final class LegacyArchiveSectionImageCatalog {
  public enum Section { RING, GLOVE, LEGGING }
  public enum RecoveryStatus { URL_RECOVERED_BINARY_BLOCKED, BINARY_RECOVERED, ITEM_CONFIRMED }

  public static final class Row {
    public final Section section;
    public final int ordinal;
    public final String sourcePageUrl;
    public final String imageUrl;
    public final RecoveryStatus status;

    Row(Section section,int ordinal,String sourcePageUrl,String imageUrl,RecoveryStatus status){
      this.section=section;this.ordinal=ordinal;this.sourcePageUrl=sourcePageUrl;
      this.imageUrl=imageUrl;this.status=status;
    }

    public boolean exactItemConfirmed(){return status==RecoveryStatus.ITEM_CONFIRMED;}
  }

  public static final String SOURCE_PAGE="https://minimob.tistory.com/99";
  private static final List<Row> ROWS;

  static {
    List<Row> r=new ArrayList<>();
    // Ring section: source page links 29..36, table immediately below names including 골드아쿠아링.
    add(r,Section.RING,1,"https://t1.daumcdn.net/cfile/tistory/15043A4250406F891E");
    add(r,Section.RING,2,"https://t1.daumcdn.net/cfile/tistory/152E284250406F893D");
    add(r,Section.RING,3,"https://t1.daumcdn.net/cfile/tistory/1518B53D5055A4BF05");
    add(r,Section.RING,4,"https://t1.daumcdn.net/cfile/tistory/1203B03D5055A4BF21");
    add(r,Section.RING,5,"https://t1.daumcdn.net/cfile/tistory/20660D355046302D1D");
    add(r,Section.RING,6,"https://t1.daumcdn.net/cfile/tistory/17763F355046302D0F");
    add(r,Section.RING,7,"https://t1.daumcdn.net/cfile/tistory/03474C3C50817CFC34");
    add(r,Section.RING,8,"https://t1.daumcdn.net/cfile/tistory/025FCA3C50817CFD20");

    // Glove section: source page links 37..40.
    add(r,Section.GLOVE,1,"https://t1.daumcdn.net/cfile/tistory/1409EC385046382A0D");
    add(r,Section.GLOVE,2,"https://t1.daumcdn.net/cfile/tistory/140CA4385046382B08");
    add(r,Section.GLOVE,3,"https://t1.daumcdn.net/cfile/tistory/14758341504070E802");
    add(r,Section.GLOVE,4,"https://t1.daumcdn.net/cfile/tistory/174AC441504070E82F");

    // Legging section: source page links 41..46.
    add(r,Section.LEGGING,1,"https://t1.daumcdn.net/cfile/tistory/1243DD3C5055B3501F");
    add(r,Section.LEGGING,2,"https://t1.daumcdn.net/cfile/tistory/124EE23C5055B35011");
    add(r,Section.LEGGING,3,"https://t1.daumcdn.net/cfile/tistory/18133E4A50462D0931");
    add(r,Section.LEGGING,4,"https://t1.daumcdn.net/cfile/tistory/141C3B4A50462D0A2A");
    add(r,Section.LEGGING,5,"https://t1.daumcdn.net/cfile/tistory/1123C33D50406DFB34");
    add(r,Section.LEGGING,6,"https://t1.daumcdn.net/cfile/tistory/1350703D50406DFB13");
    ROWS=Collections.unmodifiableList(r);
  }

  private LegacyArchiveSectionImageCatalog(){}
  private static void add(List<Row> rows,Section section,int ordinal,String url){
    rows.add(new Row(section,ordinal,SOURCE_PAGE,url,RecoveryStatus.URL_RECOVERED_BINARY_BLOCKED));
  }

  public static List<Row> all(){return ROWS;}
  public static List<Row> bySection(Section section){
    List<Row> out=new ArrayList<>();
    for(Row row:ROWS)if(row.section==section)out.add(row);
    return Collections.unmodifiableList(out);
  }
  public static int recoveredUrlCount(){return ROWS.size();}
  public static int confirmedItemCount(){int n=0;for(Row row:ROWS)if(row.exactItemConfirmed())n++;return n;}
}
