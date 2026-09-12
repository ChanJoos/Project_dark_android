package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;

/** RuntimeState-backed regression audit for frame ordering, duplicate suppression, and respawn reset. */
public final class CombatRuntimeSessionAudit {
  private CombatRuntimeSessionAudit(){}

  public static boolean verify(){
    RuntimeState state=new RuntimeState(RuntimeState.BootMode.MILLES,true);
    if(state.monsters().isEmpty())return false;
    RuntimeState.Monster monster=state.monsters().get(0);
    state.player().x=monster.x;
    state.player().y=monster.y;

    CombatResolver.Definition strike=new CombatResolver.Definition(
        "session_strike",CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,
        CombatResolver.EffectType.PHYSICAL_HIT,false,0,0f,100f,.20f,monster.maxHp);
    CombatRuntimeSession session=new CombatRuntimeSession(state,(a,t)->true,(a,id)->true,
        Collections.singletonList(strike));
    CombatFeedbackStream.AnchorPort anchors=new CombatFeedbackStream.AnchorPort(){
      public float anchorX(String id){return monster.x;}
      public float anchorY(String id){return monster.y;}
    };

    CombatRuntimeSession.FrameSnapshot first=session.advance(1,.20f,
        Collections.singletonList(CombatRuntimeSession.ActionRequest.manual("player",monster.id,"session_strike")),anchors);
    if(!first.applied()||monster.alive||first.damageNumbers.size()!=1)return false;
    if(count(state,CombatLedger.Type.MONSTER_DEFEATED)!=1)return false;

    // Replaying the Director frame must not re-submit, re-tick, re-drain, or mutate runtime state.
    int hpAfterFirst=monster.hp;
    CombatRuntimeSession.FrameSnapshot duplicate=session.advance(1,.20f,
        Collections.singletonList(CombatRuntimeSession.ActionRequest.manual("player",monster.id,"session_strike")),anchors);
    if(duplicate.status!=CombatRuntimeSession.FrameStatus.STALE_FRAME_IGNORED)return false;
    if(!duplicate.submissions.isEmpty()||!duplicate.resolverEvents.isEmpty()||monster.hp!=hpAfterFirst)return false;
    if(count(state,CombatLedger.Type.MONSTER_DEFEATED)!=1)return false;

    // Runtime owns respawn timing. Session consumes its ledger event once before admitting the next-life action.
    state.tick(4f);
    if(!monster.alive||count(state,CombatLedger.Type.MONSTER_RESPAWNED)!=1)return false;
    CombatRuntimeSession.FrameSnapshot second=session.advance(2,.20f,
        Collections.singletonList(CombatRuntimeSession.ActionRequest.manual("player",monster.id,"session_strike")),anchors);
    if(!second.applied()||!second.respawnedTargetIds.equals(Arrays.asList(monster.id)))return false;
    if(monster.alive||count(state,CombatLedger.Type.MONSTER_DEFEATED)!=2)return false;

    // The already-consumed respawn ledger entry must never reset again on a later frame.
    CombatRuntimeSession.FrameSnapshot third=session.advance(3,0f,Collections.<CombatRuntimeSession.ActionRequest>emptyList(),anchors);
    return third.respawnedTargetIds.isEmpty()&&count(state,CombatLedger.Type.MONSTER_RESPAWNED)==1;
  }

  private static int count(RuntimeState state,CombatLedger.Type type){
    int count=0;
    for(CombatLedger.Event event:state.ledger().snapshot())if(event.type==type)count++;
    return count;
  }

  public static void main(String[] args){
    if(!verify())throw new AssertionError("CombatRuntimeSessionAudit failed");
    System.out.println("CombatRuntimeSessionAudit PASS");
  }
}

