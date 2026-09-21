package com.projectdark.mobile;

/** [ADAPTED] First-five-minute prologue quest domain contract. */
public final class F5mAdaptedPrologueQuest {
  public static final String QUEST_ID="Q_ADAPTED_F5M_PROLOGUE_01",OPENING_MONSTER_ID="combat_dummy_01",PROVENANCE="ADAPTED",BALANCE_PROVENANCE="ADAPTED_BALANCE";
  public enum State{AVAILABLE,ACTIVE,RETURN_READY,COMPLETED} public enum AcceptResult{ACTIVATED,ALREADY_ACTIVE,ALREADY_RETURN_READY,ALREADY_COMPLETED,BLOCKED_TARGET_UNRESOLVED}
  public enum DeclineResult{LEFT_AVAILABLE,NO_CHANGE} public enum DefeatResult{IGNORED,PROGRESSED,RETURN_READY} public enum TurnInResult{COMPLETED,NOT_READY,ALREADY_COMPLETED}
  private static F5mAdaptedPrologueQuest activeOpening;
  private final String objectiveMonsterId; private State state=State.AVAILABLE; private int defeated=0; private long lastCombatSequence=0L;

  public static F5mAdaptedPrologueQuest openingFixture(){F5mAdaptedPrologueQuest q=new F5mAdaptedPrologueQuest(OPENING_MONSTER_ID);if(F5mSaveStore.installed()){activeOpening=q;F5mSaveStore.restoreQuestActive(q);}return q;}
  public static F5mAdaptedPrologueQuest activeOpening(){return activeOpening;}
  public F5mAdaptedPrologueQuest(String objectiveMonsterId){this.objectiveMonsterId=normalize(objectiveMonsterId);}
  public String questId(){return QUEST_ID;} public String provenance(){return PROVENANCE;} public String balanceProvenance(){return BALANCE_PROVENANCE;} public String objectiveMonsterId(){return objectiveMonsterId;}
  public State state(){return state;} public int currentCount(){return defeated;} public int requiredCount(){return 1;} public boolean targetResolved(){return objectiveMonsterId!=null;}
  public AcceptResult accept(){if(state==State.COMPLETED)return AcceptResult.ALREADY_COMPLETED;if(state==State.RETURN_READY)return AcceptResult.ALREADY_RETURN_READY;if(state==State.ACTIVE)return AcceptResult.ALREADY_ACTIVE;if(!targetResolved())return AcceptResult.BLOCKED_TARGET_UNRESOLVED;state=State.ACTIVE;persist();return AcceptResult.ACTIVATED;}
  public DeclineResult decline(){return state==State.AVAILABLE?DeclineResult.LEFT_AVAILABLE:DeclineResult.NO_CHANGE;}
  public DefeatResult consume(CombatLedger.Event event){if(event==null||event.sequence<=lastCombatSequence)return DefeatResult.IGNORED;lastCombatSequence=event.sequence;if(state!=State.ACTIVE||event.type!=CombatLedger.Type.MONSTER_DEFEATED)return DefeatResult.IGNORED;if(!objectiveMonsterId.equals(event.targetId))return DefeatResult.IGNORED;defeated=1;state=State.RETURN_READY;persist();return DefeatResult.RETURN_READY;}
  public TurnInResult turnIn(){if(state==State.COMPLETED)return TurnInResult.ALREADY_COMPLETED;if(state!=State.RETURN_READY)return TurnInResult.NOT_READY;state=State.COMPLETED;persist();return TurnInResult.COMPLETED;}
  public void restore(State s,int count){if(s==null||s==State.AVAILABLE){state=State.AVAILABLE;defeated=0;}else if(s==State.ACTIVE){state=State.ACTIVE;defeated=0;}else{state=s;defeated=1;}}
  public long consumedSequence(){return lastCombatSequence;}
  public void restoreSequence(long value){lastCombatSequence=Math.max(0L,value);}
  void markCompletedFromTurnInTransaction(){turnIn();} private void persist(){F5mSaveStore.saveQuestActive(this);}
  private static String normalize(String value){if(value==null)return null;String v=value.trim();return v.isEmpty()?null:v;}
}
