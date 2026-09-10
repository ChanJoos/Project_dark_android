package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Character-owned acquisition policy for unresolved legacy item imagery.
 * Keeps exhausted recovery lanes from consuming repeated passes and pivots to the next evidence pool.
 */
public final class ItemVisualAcquisitionDecision {
  public enum LaneStatus { ACTIVE, EXHAUSTED_FOR_NOW, READY_TO_PROMOTE }

  public static final class Lane {
    public final String laneId;
    public final LaneStatus status;
    public final String reason;
    public final List<String> targets;

    Lane(String laneId,LaneStatus status,String reason,List<String> targets){
      this.laneId=laneId;this.status=status;this.reason=reason;
      this.targets=Collections.unmodifiableList(targets);
    }
  }

  public static final Lane LEGACY_TISTORY_HASH_RECOVERY=new Lane(
      "LEGACY_TISTORY_HASH_RECOVERY",
      LaneStatus.EXHAUSTED_FOR_NOW,
      "Exact legacy CDN URLs and hashes were recovered, but direct binary fetch returns HTTP 403 and exact-hash web searches did not recover mirrors. Do not spend further Character passes on the same hashes unless a new mirror/cache source appears.",
      Arrays.asList(
          "1409EC385046382A0D",
          "140CA4385046382B08",
          "1243DD3C5055B3501F",
          "2065923C505599F426",
          "13637C4250EA87772F"));

  public static final Lane NEXT_SURVIVING_ARCHIVE_PASS=new Lane(
      "NEXT_SURVIVING_ARCHIVE_PASS",
      LaneStatus.ACTIVE,
      "Pivot from blocked CDN recovery to surviving pages whose text and image positions can still be correlated, plus independently reposted screenshots and fan archives.",
      Arrays.asList(
          "IT_RING_GOLDAQUA",
          "IT_BOOTS_MAGMA",
          "IT_GLOVE_AQUA",
          "IT_SHIELD_SILVER",
          "IT_LEGGING_AQUA"));

  private ItemVisualAcquisitionDecision(){}

  public static boolean shouldRetryLegacyHashes(){
    return LEGACY_TISTORY_HASH_RECOVERY.status==LaneStatus.ACTIVE;
  }

  public static List<String> nextCanonicalTargets(){
    return NEXT_SURVIVING_ARCHIVE_PASS.targets;
  }
}
