package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Combat-owned frame facade over the RuntimeState adapter, shared resolver, and feedback projection.
 * RuntimeState.tick() remains Director-owned and must run before advance() so respawn ledger events
 * are visible before new actions for the monster's next life are admitted.
 */
public final class CombatRuntimeSession {
  public enum FrameStatus { APPLIED, STALE_FRAME_IGNORED }

  public static final class ActionRequest {
    public final String actorId,targetId,actionId;
    public final CombatResolver.InputMode inputMode;

    public ActionRequest(String actorId,String targetId,String actionId,CombatResolver.InputMode inputMode){
      if(actorId==null||targetId==null||actionId==null)throw new IllegalArgumentException("action request identity");
      this.actorId=actorId;
      this.targetId=targetId;
      this.actionId=actionId;
      this.inputMode=inputMode==null?CombatResolver.InputMode.MANUAL:inputMode;
    }

    public static ActionRequest manual(String actorId,String targetId,String actionId){
      return new ActionRequest(actorId,targetId,actionId,CombatResolver.InputMode.MANUAL);
    }

    public static ActionRequest auto(String actorId,String targetId,String actionId){
      return new ActionRequest(actorId,targetId,actionId,CombatResolver.InputMode.AUTO);
    }
  }

  public static final class FrameSnapshot {
    public final long frameId;
    public final FrameStatus status;
    public final List<CombatActionOrchestrator.Submission> submissions;
    public final List<CombatResolver.Event> resolverEvents;
    public final List<CombatFeedbackStream.Event> feedbackEvents;
    public final List<DamageNumberModel.Snapshot> damageNumbers;
    public final List<CombatResolver.ActionSnapshot> activeActions;
    public final List<String> respawnedTargetIds;

    FrameSnapshot(long frameId,FrameStatus status,
        List<CombatActionOrchestrator.Submission> submissions,
        List<CombatResolver.Event> resolverEvents,
        List<CombatFeedbackStream.Event> feedbackEvents,
        List<DamageNumberModel.Snapshot> damageNumbers,
        List<CombatResolver.ActionSnapshot> activeActions,
        List<String> respawnedTargetIds){
      this.frameId=frameId;
      this.status=status;
      this.submissions=immutable(submissions);
      this.resolverEvents=immutable(resolverEvents);
      this.feedbackEvents=immutable(feedbackEvents);
      this.damageNumbers=immutable(damageNumbers);
      this.activeActions=immutable(activeActions);
      this.respawnedTargetIds=immutable(respawnedTargetIds);
    }

    public boolean applied(){return status==FrameStatus.APPLIED;}

    private static <T> List<T> immutable(List<T> source){
      return Collections.unmodifiableList(new ArrayList<>(source));
    }
  }

  private final RuntimeState state;
  private final RuntimeCombatPortAdapter adapter;
  private final CombatResolver resolver;
  private final CombatActionOrchestrator actions;
  private final CombatFeedbackStream feedback=new CombatFeedbackStream();
  private long lastFrameId=Long.MIN_VALUE;
  private long lastLedgerSequence;

  public CombatRuntimeSession(RuntimeState state,
      RuntimeCombatPortAdapter.LineOfSightPort lineOfSight,
      RuntimeCombatPortAdapter.LearnedActionPort learnedActions,
      List<CombatResolver.Definition> definitions){
    if(state==null)throw new IllegalArgumentException("state");
    this.state=state;
    adapter=new RuntimeCombatPortAdapter(state,lineOfSight,learnedActions);
    resolver=new CombatResolver(adapter);
    actions=new CombatActionOrchestrator(resolver,definitions);
  }

  /**
   * Advances combat exactly once for a monotonically increasing Director frame ID.
   * Requests are admitted after cooldown/respawn synchronization and before the shared hit-frame tick.
   */
  public FrameSnapshot advance(long frameId,float dt,List<ActionRequest> requests,
      CombatFeedbackStream.AnchorPort anchors){
    if(dt<0f||Float.isNaN(dt)||Float.isInfinite(dt))throw new IllegalArgumentException("dt");
    if(frameId<=lastFrameId)return ignored(frameId);
    lastFrameId=frameId;

    adapter.tick(dt);
    List<String> respawned=synchronizeRespawns();
    List<CombatActionOrchestrator.Submission> submissions=new ArrayList<>();
    if(requests!=null){
      for(ActionRequest request:requests){
        if(request==null)continue;
        submissions.add(actions.submit(request.actorId,request.targetId,request.actionId,request.inputMode));
      }
    }

    actions.tick(dt);
    List<CombatResolver.Event> resolverEvents=actions.drainEvents();
    feedback.consume(resolverEvents,anchors);
    feedback.tick(dt);
    List<CombatFeedbackStream.Event> feedbackEvents=feedback.drainEvents();
    return new FrameSnapshot(frameId,FrameStatus.APPLIED,submissions,resolverEvents,feedbackEvents,
        feedback.damageNumbers(),resolver.actionSnapshots(),respawned);
  }

  public float cooldownRemaining(String actorId,String actionId){return adapter.cooldownRemaining(actorId,actionId);}
  public int activeActionCount(){return actions.activeActionCount();}

  private List<String> synchronizeRespawns(){
    Set<String> reset=new LinkedHashSet<>();
    for(CombatLedger.Event event:state.ledger().snapshot()){
      if(event.sequence<=lastLedgerSequence)continue;
      lastLedgerSequence=Math.max(lastLedgerSequence,event.sequence);
      if(event.type==CombatLedger.Type.MONSTER_RESPAWNED&&event.targetId!=null&&reset.add(event.targetId))
        resolver.onTargetRespawned(event.targetId);
    }
    return new ArrayList<>(reset);
  }

  private FrameSnapshot ignored(long frameId){
    return new FrameSnapshot(frameId,FrameStatus.STALE_FRAME_IGNORED,
        Collections.<CombatActionOrchestrator.Submission>emptyList(),
        Collections.<CombatResolver.Event>emptyList(),
        Collections.<CombatFeedbackStream.Event>emptyList(),feedback.damageNumbers(),
        resolver.actionSnapshots(),Collections.<String>emptyList());
  }
}

