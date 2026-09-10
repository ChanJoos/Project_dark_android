package com.projectdark.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Routes MANUAL and MonsterAI AUTO submissions into one CombatResolver without inventing action data. */
public final class CombatActionOrchestrator {
  public enum Outcome { ACCEPTED, ACTION_UNRESOLVED, REJECTED }
  public static final class Submission {
    public final Outcome outcome; public final String actorId,targetId,actionId; public final CombatResolver.InputMode inputMode;
    public final long actionSequence; public final CombatResolver.RejectReason rejectReason;
    Submission(Outcome o,String a,String t,String id,CombatResolver.InputMode m,long s,CombatResolver.RejectReason r){outcome=o;actorId=a;targetId=t;actionId=id;inputMode=m;actionSequence=s;rejectReason=r;}
    public boolean accepted(){return outcome==Outcome.ACCEPTED;}
  }
  private final CombatResolver resolver; private final Map<String,CombatResolver.Definition> definitions;
  public CombatActionOrchestrator(CombatResolver resolver,List<CombatResolver.Definition> defs){
    if(resolver==null||defs==null)throw new IllegalArgumentException("resolver/definitions"); this.resolver=resolver;
    Map<String,CombatResolver.Definition> index=new LinkedHashMap<>();
    for(CombatResolver.Definition d:defs){if(d==null||index.put(d.actionId,d)!=null)throw new IllegalArgumentException("duplicate/null action definition");}
    definitions=Collections.unmodifiableMap(index);
  }
  public Submission submitManual(String actorId,String targetId,String actionId){return submit(actorId,targetId,actionId,CombatResolver.InputMode.MANUAL);}
  public Submission submitAuto(String actorId,String targetId,String actionId){return submit(actorId,targetId,actionId,CombatResolver.InputMode.AUTO);}
  public Submission submit(String actorId,String targetId,String actionId,CombatResolver.InputMode mode){
    CombatResolver.InputMode actual=mode==null?CombatResolver.InputMode.MANUAL:mode; CombatResolver.Definition d=definitions.get(actionId);
    if(d==null)return new Submission(Outcome.ACTION_UNRESOLVED,actorId,targetId,actionId,actual,0,null);
    CombatResolver.BeginResult r=resolver.begin(d,actorId,targetId,actual);
    return new Submission(r.accepted?Outcome.ACCEPTED:Outcome.REJECTED,actorId,targetId,actionId,actual,r.actionSequence,r.rejectReason);
  }
  public void tick(float dt){resolver.tick(dt);} public List<CombatResolver.Event> drainEvents(){return resolver.drainEvents();}
  public List<CombatResolver.ActionSnapshot> actionSnapshots(){return resolver.actionSnapshots();} public int activeActionCount(){return resolver.activeActionCount();}
  public Map<String,CombatResolver.Definition> definitions(){return definitions;}
}
