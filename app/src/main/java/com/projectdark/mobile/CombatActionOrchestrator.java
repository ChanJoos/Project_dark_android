package com.projectdark.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Combat-owned submission boundary joining manual commands and MonsterAI AUTO requests to one
 * resolver. Definitions are injected by stable actionId; this class invents no combat data.
 */
public final class CombatActionOrchestrator {
  public enum Outcome { ACCEPTED, ACTION_UNRESOLVED, REJECTED }

  public static final class Submission {
    public final Outcome outcome;
    public final String actorId,targetId,actionId;
    public final CombatResolver.InputMode inputMode;
    public final long actionSequence;
    public final CombatResolver.RejectReason rejectReason;
    private Submission(Outcome outcome,String actorId,String targetId,String actionId,
        CombatResolver.InputMode inputMode,long actionSequence,CombatResolver.RejectReason rejectReason){
      this.outcome=outcome;this.actorId=actorId;this.targetId=targetId;this.actionId=actionId;
      this.inputMode=inputMode;this.actionSequence=actionSequence;this.rejectReason=rejectReason;
    }
    public boolean accepted(){return outcome==Outcome.ACCEPTED;}
  }

  private final CombatResolver resolver;
  private final Map<String,CombatResolver.Definition> definitions;

  public CombatActionOrchestrator(CombatResolver resolver,List<CombatResolver.Definition> definitions){
    if(resolver==null||definitions==null)throw new IllegalArgumentException("resolver and definitions are required");
    this.resolver=resolver;
    Map<String,CombatResolver.Definition> indexed=new LinkedHashMap<>();
    for(CombatResolver.Definition definition:definitions){
      if(definition==null)throw new IllegalArgumentException("null definition");
      if(indexed.put(definition.actionId,definition)!=null)throw new IllegalArgumentException("duplicate actionId: "+definition.actionId);
    }
    this.definitions=Collections.unmodifiableMap(indexed);
  }

  public Submission submitManual(String actorId,String targetId,String actionId){
    return submit(actorId,targetId,actionId,CombatResolver.InputMode.MANUAL);
  }

  /** Suitable for MonsterAIController.Port.submitAttack(request). */
  public boolean submitMonsterAttack(MonsterAIController.AttackRequest request){
    if(request==null)throw new IllegalArgumentException("request");
    return submit(request.monsterId,request.targetId,request.actionId,CombatResolver.InputMode.AUTO).accepted();
  }

  public Submission submit(String actorId,String targetId,String actionId,CombatResolver.InputMode inputMode){
    CombatResolver.InputMode mode=inputMode==null?CombatResolver.InputMode.MANUAL:inputMode;
    CombatResolver.Definition definition=definitions.get(actionId);
    if(definition==null)return new Submission(Outcome.ACTION_UNRESOLVED,actorId,targetId,actionId,mode,0,null);
    CombatResolver.BeginResult result=resolver.begin(definition,actorId,targetId,mode);
    return new Submission(result.accepted?Outcome.ACCEPTED:Outcome.REJECTED,actorId,targetId,actionId,
        mode,result.actionSequence,result.rejectReason);
  }

  public void tick(float deltaSeconds){resolver.tick(deltaSeconds);}
  public List<CombatResolver.Event> drainEvents(){return resolver.drainEvents();}
  public boolean actionActive(String actorId){return resolver.actionActive(actorId);}
  public int activeActionCount(){return resolver.activeActionCount();}
  public Map<String,CombatResolver.Definition> definitions(){return definitions;}
}
