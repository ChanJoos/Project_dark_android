package com.projectdark.mobile;

import java.util.List;

/**
 * Android-runtime acceptance probe for F5M-007/008/014.
 *
 * This deliberately exercises the production RuntimeState instead of constructing a fake monster or
 * standalone ledger. It is callable only from an Android-capable runtime because RuntimeState owns
 * Android world geometry. The plain-JVM F5mAdaptedPrologueQuestAudit remains complementary domain coverage.
 */
public final class F5mRuntimeBindingAudit {
  private F5mRuntimeBindingAudit(){}

  public static Result run(){
    RuntimeState runtime = new RuntimeState(RuntimeState.BootMode.MILLES, true);
    RuntimeState.Monster target = null;
    for(RuntimeState.Monster monster : runtime.monsters()){
      if(F5mAdaptedPrologueQuest.OPENING_MONSTER_ID.equals(monster.id)){
        target = monster;
        break;
      }
    }
    if(target == null) return Result.fail("opening target is not spawned in Milles runtime");

    F5mAdaptedPrologueQuest quest = F5mAdaptedPrologueQuest.openingFixture();
    if(quest.accept() != F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED){
      return Result.fail("opening quest did not activate");
    }

    runtime.damage(target, target.maxHp);
    CombatLedger.Event defeat = latestDefeat(runtime.ledger().snapshot(), target.id);
    if(defeat == null) return Result.fail("production damage path emitted no matching MONSTER_DEFEATED");
    if(quest.consume(defeat) != F5mAdaptedPrologueQuest.DefeatResult.RETURN_READY){
      return Result.fail("matching production defeat did not transition 0/1 -> 1/1 RETURN_READY");
    }
    if(quest.consume(defeat) != F5mAdaptedPrologueQuest.DefeatResult.IGNORED){
      return Result.fail("replayed production defeat incremented objective twice");
    }
    if(quest.currentCount() != 1 || quest.state() != F5mAdaptedPrologueQuest.State.RETURN_READY){
      return Result.fail("quest state/count diverged after production defeat");
    }
    return Result.pass(defeat.sequence, target.id);
  }

  private static CombatLedger.Event latestDefeat(List<CombatLedger.Event> events, String monsterId){
    for(int i=events.size()-1;i>=0;i--){
      CombatLedger.Event event = events.get(i);
      if(event.type == CombatLedger.Type.MONSTER_DEFEATED && monsterId.equals(event.targetId)) return event;
    }
    return null;
  }

  public static final class Result {
    public final boolean passed;
    public final String detail;
    public final long defeatSequence;
    public final String monsterId;

    private Result(boolean passed,String detail,long defeatSequence,String monsterId){
      this.passed=passed;
      this.detail=detail;
      this.defeatSequence=defeatSequence;
      this.monsterId=monsterId;
    }

    static Result pass(long sequence,String monsterId){
      return new Result(true,"PASS",sequence,monsterId);
    }

    static Result fail(String detail){
      return new Result(false,detail,-1L,null);
    }
  }
}
