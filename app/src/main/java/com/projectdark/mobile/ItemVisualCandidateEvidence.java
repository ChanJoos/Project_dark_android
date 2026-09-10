package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Character-owned staging area for visual references found during item-image research.
 * A candidate is never render-authoritative until its item identity and image surface are verified.
 */
public final class ItemVisualCandidateEvidence {
  public enum SurfaceHint { INVENTORY_ICON, EQUIPPED_WORLD, MIXED_SCREENSHOT, UNKNOWN }
  public enum IdentityStatus { UNMAPPED, FAMILY_ONLY, ITEM_CONFIRMED }

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
  static {
    List<Candidate> rows=new ArrayList<>();
    rows.add(new Candidate(
        "CAND_SHIELD_KNIGHT_DC_2017",
        "https://gall.dcinside.com/mgallery/board/view/?id=darkages&no=3326",
        "gall.dcinside.com","FAN",
        SurfaceHint.MIXED_SCREENSHOT,IdentityStatus.FAMILY_ONLY,null,
        "Screenshot visibly contains a shield icon/equipped character, but the post is about 기사단 방패, which is not one of the seven canonical shield IDs in the current Item_Master batch. Do not map it by visual similarity."));
    SHIELD_CANDIDATES=Collections.unmodifiableList(rows);
  }

  private ItemVisualCandidateEvidence(){}
  public static List<Candidate> shieldCandidates(){return SHIELD_CANDIDATES;}
  public static int promotableShieldCandidateCount(){
    int n=0;for(Candidate c:SHIELD_CANDIDATES)if(c.canPromoteToRender())n++;return n;
  }
}
