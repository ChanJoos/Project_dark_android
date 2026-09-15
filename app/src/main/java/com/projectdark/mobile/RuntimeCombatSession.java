package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Runtime combat session shared by player/manual/auto and monster resolver routes. */
public final class RuntimeCombatSession {
  public static final String MONSTER_BASIC_ACTION_ID="monster_basic_attack";
  private final CombatResolver resolver;
  private long lastLedgerSequence;

  public RuntimeCombatSession(RuntimeState state){
    this(state,new CombatResolver(state,definitions()));
  }

  RuntimeCombatSession(RuntimeState state,CombatResolver resolver){
    if(state==null)throw new IllegalArgumentException("state");
    if(resolver==null)throw new IllegalArgumentException("resolver");
    this.resolver=resolver;
  }

  public CombatResolver resolver(){return resolver;}

  public CombatResolver.Result submit(String actorId,String targetId,String actionId){
    return resolver.submit(actorId,targetId,actionId);
  }

  public void update(float dt){resolver.update(dt);}

  public void syncLedger(CombatLedger ledger){
    if(ledger==null)return;
    for(CombatLedger.Event event:ledger.events()){
      if(event.sequence<=lastLedgerSequence)continue;
      lastLedgerSequence=event.sequence;
      if(event.type==CombatLedger.Type.MONSTER_RESPAWNED)resolver.onTargetRespawned(event.targetId);
    }
  }

  private static List<CombatResolver.Definition> definitions(){
    List<CombatResolver.Definition> out=new ArrayList<>();
    for(int i=0;i<AttackDef.PROTOTYPES.length;i++)out.add(CombatResolver.attackPrototype(AttackDef.at(i),i));
    out.add(CombatResolver.magicPrototype());
    out.add(CombatResolver.skillPrototype());
    out.add(CombatResolver.kickPrototype());
    // Melee legality is owned by CanonicalMeleeTileContract at submission/contact time.
    // The resolver range is therefore only a non-authoritative broad-phase envelope and
    // must not resurrect the removed MonsterAIController pixel-radius contract.
    out.add(new CombatResolver.Definition(
        MONSTER_BASIC_ACTION_ID,CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,
        CombatResolver.EffectType.PHYSICAL_HIT,false,0,MonsterAIController.ATTACK_COOLDOWN_B,
        48f,.24f,MonsterAIController.ATTACK_DAMAGE_B));
    Map<String,CombatResolver.Definition> unique=new LinkedHashMap<>();
    for(CombatResolver.Definition definition:out){
      if(unique.put(definition.actionId,definition)!=null)throw new IllegalStateException("duplicate combat action id");
    }
    return Collections.unmodifiableList(out);
  }
}