package com.projectdark.mobile;

/**
 * Combat-owned adapter from MonsterAI AUTO attack intent to the shared action orchestrator.
 * This class never applies damage itself and never creates a second combat formula.
 */
public final class MonsterAutoCombatBridge {
  public enum Outcome { ACCEPTED, REJECTED, ACTION_UNRESOLVED }

  public static final class Result {
    public final Outcome outcome;
    public final String monsterId,targetId,actionId;
    public final long actionSequence;
    public final CombatResolver.RejectReason rejectReason;
    Result(Outcome outcome,String monsterId,String targetId,String actionId,long actionSequence,
        CombatResolver.RejectReason rejectReason){
      this.outcome=outcome;this.monsterId=monsterId;this.targetId=targetId;this.actionId=actionId;
      this.actionSequence=actionSequence;this.rejectReason=rejectReason;
    }
  }

  private final CombatActionOrchestrator actions;
  private final String actionId;

  public MonsterAutoCombatBridge(CombatActionOrchestrator actions,String actionId){
    if(actions==null||actionId==null||actionId.trim().isEmpty())throw new IllegalArgumentException("actions/actionId");
    this.actions=actions;this.actionId=actionId;
  }

  public String actionId(){return actionId;}

  public Result submit(String monsterId,String targetId){
    CombatActionOrchestrator.Submission submission=actions.submitAuto(monsterId,targetId,actionId);
    Outcome outcome;
    switch(submission.outcome){
      case ACCEPTED: outcome=Outcome.ACCEPTED; break;
      case ACTION_UNRESOLVED: outcome=Outcome.ACTION_UNRESOLVED; break;
      default: outcome=Outcome.REJECTED; break;
    }
    return new Result(outcome,monsterId,targetId,actionId,submission.actionSequence,submission.rejectReason);
  }
}
