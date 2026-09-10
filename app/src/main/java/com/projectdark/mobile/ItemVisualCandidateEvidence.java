package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Character-owned staging area for visual references found during item-image research.
 * A candidate is never render-authoritative until its item identity and image surface are verified.
 */
public final class ItemVisualCandidateEvidence {
  public enum SurfaceHint { INVENTORY_ICON, EQUIPPED_WORLD, MIXED_SCREENSHOT, SECTION_IMAGE, UNKNOWN }
  public enum IdentityStatus { UNMAPPED, FAMILY_ONLY, SECTION_MATCH, ITEM_CONFIRMED }

  public static final class Candidate {
    public final String candidateId;
    public final String sourceUrl;
    public final String sourceHost;
    public final String provenance;
    public final SurfaceHint surfaceHint;
    public final IdentityStatus identityStatus;
    public final String proposedItemId;
    public final String note;

    Candidate(String candidateId,String sourceUrl,String sourceHost,String provenance,
        SurfaceHint surfaceHint,IdentityStatus identityStatus,String proposedItemId,String note){
      this.candidateId=candidateId;this.sourceUrl=sourceUrl;this.sourceHost=sourceHost;
      this.provenance=provenance;this.surfaceHint=surfaceHint;this.identityStatus=identityStatus;
      this.proposedItemId=proposedItemId;this.note=note;
    }

    public boolean canPromoteToRender(){
      return identityStatus==IdentityStatus.ITEM_CONFIRMED && proposedItemId!=null &&
          (surfaceHint==SurfaceHint.EQUIPPED_WORLD||surfaceHint==SurfaceHint.INVENTORY_ICON);
    }
  }

  private static final List<Candidate> SHIELD_CANDIDATES;
  private static final List<Candidate> FAN_ARCHIVE_CANDIDATES;
  static {
    List<Candidate> shields=new ArrayList<>();
    shields.add(new Candidate(
        "CAND_SHIELD_KNIGHT_DC_2017",
        "https://gall.dcinside.com/mgallery/board/view/?id=darkages&no=3326",
        "gall.dcinside.com","FAN",
        SurfaceHint.MIXED_SCREENSHOT,IdentityStatus.FAMILY_ONLY,null,
        "Screenshot visibly contains a shield icon/equipped character, but the post is about 기사단 방패, which is not one of the seven canonical shield IDs in the current Item_Master batch. Do not map it by visual similarity."));
    SHIELD_CANDIDATES=Collections.unmodifiableList(shields);

    List<Candidate> fan=new ArrayList<>();
    fan.add(new Candidate(
        "CAND_MINIMOB_GLOVE_IMG_01",
        "https://t1.daumcdn.net/cfile/tistory/1409EC385046382A0D",
        "t1.daumcdn.net","FAN_ARCHIVE",
        SurfaceHint.SECTION_IMAGE,IdentityStatus.SECTION_MATCH,null,
        "Image is embedded in the glove section of minimob.tistory.com/99. Exact item identity is not yet confirmed because the CDN image cannot currently be fetched automatically (HTTP 403)."));
    fan.add(new Candidate(
        "CAND_MINIMOB_GLOVE_IMG_02",
        "https://t1.daumcdn.net/cfile/tistory/140CA4385046382B08",
        "t1.daumcdn.net","FAN_ARCHIVE",
        SurfaceHint.SECTION_IMAGE,IdentityStatus.SECTION_MATCH,null,
        "Second image embedded in the glove section of minimob.tistory.com/99; preserve for manual/browser extraction and later item-label matching."));
    fan.add(new Candidate(
        "CAND_MINIMOB_LEGGING_IMG_01",
        "https://t1.daumcdn.net/cfile/tistory/1243DD3C5055B3501F",
        "t1.daumcdn.net","FAN_ARCHIVE",
        SurfaceHint.SECTION_IMAGE,IdentityStatus.SECTION_MATCH,null,
        "Image embedded in the legging section of minimob.tistory.com/99; exact item identity pending visual extraction."));
    fan.add(new Candidate(
        "CAND_MINIMOB_ARMOR_IMG_01",
        "https://t1.daumcdn.net/cfile/tistory/2065923C505599F426",
        "t1.daumcdn.net","FAN_ARCHIVE",
        SurfaceHint.SECTION_IMAGE,IdentityStatus.SECTION_MATCH,null,
        "Image embedded in the armor/clothing section of minimob.tistory.com/99; exact armor identity pending visual extraction."));
    FAN_ARCHIVE_CANDIDATES=Collections.unmodifiableList(fan);
  }

  private ItemVisualCandidateEvidence(){}
  public static List<Candidate> shieldCandidates(){return SHIELD_CANDIDATES;}
  public static List<Candidate> fanArchiveCandidates(){return FAN_ARCHIVE_CANDIDATES;}
  public static int promotableShieldCandidateCount(){
    int n=0;for(Candidate c:SHIELD_CANDIDATES)if(c.canPromoteToRender())n++;return n;
  }
  public static int promotableFanArchiveCandidateCount(){
    int n=0;for(Candidate c:FAN_ARCHIVE_CANDIDATES)if(c.canPromoteToRender())n++;return n;
  }
}
