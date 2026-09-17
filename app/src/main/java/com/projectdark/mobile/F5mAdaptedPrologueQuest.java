package com.projectdark.mobile;

/**
 * [ADAPTED] First-five-minute prologue quest domain contract.
 *
 * This fixture is deliberately isolated from SOURCE_BACKED Q_MIL_01/NPC_AN/NPC_BANE.
 * The Director-approved opening target is the already spawned runtime fixture combat_dummy_01;
 * it remains ADAPTED/ADAPTED_BALANCE and is not promoted into canonical Monster_Master.
 */
public final class F5mAdaptedPrologueQuest {
  public static final String QUEST_ID = "Q_ADAPTED_F5M_PROLOGUE_01";
  public static final String OPENING_MONSTER_ID = "combat_dummy_01";
  public static final String PROVENANCE = "ADAPTED";
  public static final String BALANCE_PROVENANCE = "ADAPTED_BALANCE";

  public enum State { AVAILABLE, ACTIVE, RETURN_READY, COMPLETED }
  public enum AcceptResult { ACTIVATED, ALREADY_ACTIVE, ALREADY_RETURN_READY, ALREADY_COMPLETED, BLOCKED_TARGET_UNRESOLVED }
  public enum DeclineResult { LEFT_AVAILABLE, NO_CHANGE }
  public enum DefeatResult { IGNORED, PROGRESSED, RETURN_READY }

  private final String objectiveMonsterId;
  private State state = State.AVAILABLE;
  private int defeated = 0;
  private long lastCombatSequence = 0L;

  /** Production F5M fixture: binds only to the Director-approved real spawned runtime target. */
  public static F5mAdaptedPrologueQuest openingFixture(){
    return new F5mAdaptedPrologueQuest(OPENING_MONSTER_ID);
  }

  /** Explicit constructor retained for deterministic contract tests and future content injection. */
  public F5mAdaptedPrologueQuest(String objectiveMonsterId) {
    this.objectiveMonsterId = normalize(objectiveMonsterId);
  }

  public String questId(){ return QUEST_ID; }
  public String provenance(){ return PROVENANCE; }
  public String balanceProvenance(){ return BALANCE_PROVENANCE; }
  public String objectiveMonsterId(){ return objectiveMonsterId; }
  public State state(){ return state; }
  public int currentCount(){ return defeated; }
  public int requiredCount(){ return 1; }
  public boolean targetResolved(){ return objectiveMonsterId != null; }

  public AcceptResult accept(){
    if(state == State.COMPLETED) return AcceptResult.ALREADY_COMPLETED;
    if(state == State.RETURN_READY) return AcceptResult.ALREADY_RETURN_READY;
    if(state == State.ACTIVE) return AcceptResult.ALREADY_ACTIVE;
    if(!targetResolved()) return AcceptResult.BLOCKED_TARGET_UNRESOLVED;
    state = State.ACTIVE;
    return AcceptResult.ACTIVATED;
  }

  public DeclineResult decline(){
    return state == State.AVAILABLE ? DeclineResult.LEFT_AVAILABLE : DeclineResult.NO_CHANGE;
  }

  /** Consumes authoritative MONSTER_DEFEATED identity once; replayed sequences cannot progress twice. */
  public DefeatResult consume(CombatLedger.Event event){
    if(event == null || event.sequence <= lastCombatSequence) return DefeatResult.IGNORED;
    lastCombatSequence = event.sequence;
    if(state != State.ACTIVE || event.type != CombatLedger.Type.MONSTER_DEFEATED) return DefeatResult.IGNORED;
    if(!objectiveMonsterId.equals(event.targetId)) return DefeatResult.IGNORED;
    defeated = 1;
    state = State.RETURN_READY;
    return DefeatResult.RETURN_READY;
  }

  /** F5M-024 owns the atomic one-time reward/progression/inventory transaction. */
  void markCompletedFromTurnInTransaction(){
    if(state == State.RETURN_READY) state = State.COMPLETED;
  }

  private static String normalize(String value){
    if(value == null) return null;
    String v = value.trim();
    return v.isEmpty() ? null : v;
  }
}
