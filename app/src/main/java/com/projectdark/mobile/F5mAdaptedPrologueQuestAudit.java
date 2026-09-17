package com.projectdark.mobile;

import java.util.List;

/** Executable development/CI audit for the [ADAPTED] F5M-007/008 quest transaction contract. */
public final class F5mAdaptedPrologueQuestAudit {
  private F5mAdaptedPrologueQuestAudit(){}

  public static void main(String[] args){
    assertCase("approvedOpeningTargetIsBound", approvedOpeningTargetIsBound());
    assertCase("unresolvedTargetBlocksActivation", unresolvedTargetBlocksActivation());
    assertCase("declineReopensWithoutMutation", declineReopensWithoutMutation());
    assertCase("acceptIsIdempotent", acceptIsIdempotent());
    assertCase("authoritativeDefeatIdentityAndReplayAreIdempotent", authoritativeDefeatIdentityAndReplayAreIdempotent());
    assertCase("sourceQuestNamespaceIsNotReused", sourceQuestNamespaceIsNotReused());
    System.out.println("F5M_ADAPTED_PROLOGUE_QUEST_AUDIT=PASS");
  }

  private static void assertCase(String name, boolean passed){
    if(!passed) throw new AssertionError("F5M adapted prologue quest contract failed: "+name);
  }

  public static boolean passes(){
    return approvedOpeningTargetIsBound()
        && unresolvedTargetBlocksActivation()
        && declineReopensWithoutMutation()
        && acceptIsIdempotent()
        && authoritativeDefeatIdentityAndReplayAreIdempotent()
        && sourceQuestNamespaceIsNotReused();
  }

  static boolean approvedOpeningTargetIsBound(){
    F5mAdaptedPrologueQuest q = F5mAdaptedPrologueQuest.openingFixture();
    return "combat_dummy_01".equals(q.objectiveMonsterId())
        && F5mAdaptedPrologueQuest.OPENING_MONSTER_ID.equals(q.objectiveMonsterId())
        && q.targetResolved();
  }

  static boolean unresolvedTargetBlocksActivation(){
    F5mAdaptedPrologueQuest q = new F5mAdaptedPrologueQuest(null);
    return q.accept() == F5mAdaptedPrologueQuest.AcceptResult.BLOCKED_TARGET_UNRESOLVED
        && q.state() == F5mAdaptedPrologueQuest.State.AVAILABLE;
  }

  static boolean declineReopensWithoutMutation(){
    F5mAdaptedPrologueQuest q = F5mAdaptedPrologueQuest.openingFixture();
    return q.decline() == F5mAdaptedPrologueQuest.DeclineResult.LEFT_AVAILABLE
        && q.decline() == F5mAdaptedPrologueQuest.DeclineResult.LEFT_AVAILABLE
        && q.state() == F5mAdaptedPrologueQuest.State.AVAILABLE;
  }

  static boolean acceptIsIdempotent(){
    F5mAdaptedPrologueQuest q = F5mAdaptedPrologueQuest.openingFixture();
    return q.accept() == F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED
        && q.accept() == F5mAdaptedPrologueQuest.AcceptResult.ALREADY_ACTIVE
        && q.state() == F5mAdaptedPrologueQuest.State.ACTIVE;
  }

  static boolean authoritativeDefeatIdentityAndReplayAreIdempotent(){
    // This audit intentionally stays pure JVM. RuntimeState is Android-bound (graphics stubs cannot be
    // executed from a plain java process in CI), while CombatLedger is the authoritative event contract
    // consumed by F5M-014. RuntimeState publisher wiring remains compile-checked by the Android build.
    CombatLedger ledger = new CombatLedger();
    ledger.add(CombatLedger.Type.MONSTER_DEFEATED, "player", F5mAdaptedPrologueQuest.OPENING_MONSTER_ID, 1);
    List<CombatLedger.Event> events = ledger.snapshot();
    CombatLedger.Event defeat = events.get(events.size()-1);

    F5mAdaptedPrologueQuest q = F5mAdaptedPrologueQuest.openingFixture();
    if(q.accept() != F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED) return false;
    if(q.consume(defeat) != F5mAdaptedPrologueQuest.DefeatResult.RETURN_READY) return false;
    return q.consume(defeat) == F5mAdaptedPrologueQuest.DefeatResult.IGNORED
        && q.currentCount() == 1
        && q.state() == F5mAdaptedPrologueQuest.State.RETURN_READY;
  }

  static boolean sourceQuestNamespaceIsNotReused(){
    return !"Q_MIL_01".equals(F5mAdaptedPrologueQuest.QUEST_ID)
        && "ADAPTED".equals(F5mAdaptedPrologueQuest.PROVENANCE)
        && "ADAPTED_BALANCE".equals(F5mAdaptedPrologueQuest.BALANCE_PROVENANCE);
  }
}
