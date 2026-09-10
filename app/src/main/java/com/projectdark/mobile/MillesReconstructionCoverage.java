package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Evidence-safe coverage gate for MAP_MILLES reconstruction.
 *
 * PROJECT DARK may author [ADAPTED] fill only after the Nexon/source sweep for a scope is explicitly
 * complete and the scope is positively classified as uncovered. Source discovery alone is never enough.
 */
public final class MillesReconstructionCoverage {
  public enum Scope {
    GROUND_TILE,
    BUILDING_OBJECT,
    WATER_EDGE,
    LANDMARK_OBJECT,
    COLLISION_WALKABILITY,
    NPC_PLACEMENT,
    MONSTER_SPAWN,
    PORTAL
  }

  public enum SweepStatus { IN_PROGRESS, SOURCE_SWEEP_COMPLETE }
  public enum CoverageStatus { UNKNOWN, SOURCE_COVERED, PARTIAL, UNCOVERED }

  public static final class CoverageRecord {
    public final Scope scope;
    public final SweepStatus sweepStatus;
    public final CoverageStatus coverageStatus;
    public final String evidence;
    public final String note;

    CoverageRecord(Scope scope,SweepStatus sweepStatus,CoverageStatus coverageStatus,String evidence,String note){
      this.scope=scope;
      this.sweepStatus=sweepStatus;
      this.coverageStatus=coverageStatus;
      this.evidence=evidence;
      this.note=note;
    }

    /** Newly authored visual/geometry fill is legal only for a completed, explicitly uncovered scope. */
    public boolean adaptedFillAllowed(){
      return sweepStatus==SweepStatus.SOURCE_SWEEP_COMPLETE
          &&coverageStatus==CoverageStatus.UNCOVERED;
    }
  }

  private static final List<CoverageRecord> RECORDS=Collections.unmodifiableList(Arrays.asList(
      pending(Scope.GROUND_TILE,"2021 old-town attachment pixels still pending"),
      pending(Scope.BUILDING_OBJECT,"legacy Milles buildings not exhaustively pixel-verified"),
      pending(Scope.WATER_EDGE,"legacy coastline/water-edge coverage not exhaustively pixel-verified"),
      pending(Scope.LANDMARK_OBJECT,"Milles landmark candidates exist but source sweep is incomplete"),
      pending(Scope.COLLISION_WALKABILITY,"must derive from verified map trace, not screenshot guessing"),
      pending(Scope.NPC_PLACEMENT,"Master identities/locations exist; visual placement transform pending"),
      pending(Scope.MONSTER_SPAWN,"canonical spawn geometry not yet source-complete"),
      pending(Scope.PORTAL,"portal graph/placement remains version-sensitive and incomplete")
  ));

  private static CoverageRecord pending(Scope scope,String note){
    return new CoverageRecord(scope,SweepStatus.IN_PROGRESS,CoverageStatus.UNKNOWN,"PENDING",note);
  }

  public List<CoverageRecord> records(){return RECORDS;}

  public CoverageRecord recordFor(Scope scope){
    for(CoverageRecord record:RECORDS)if(record.scope==scope)return record;
    throw new IllegalArgumentException("Unknown Milles reconstruction scope: "+scope);
  }

  public boolean canAuthorAdaptedFill(Scope scope){return recordFor(scope).adaptedFillAllowed();}

  public int adaptedFillAllowedCount(){
    int count=0;
    for(CoverageRecord record:RECORDS)if(record.adaptedFillAllowed())count++;
    return count;
  }

  /** PASS 23 invariant: no scope is currently proven source-exhausted, so authored fill stays blocked. */
  public boolean preservesNexonFirstGate(){
    return RECORDS.size()==Scope.values().length&&adaptedFillAllowedCount()==0;
  }
}
