package com.projectdark.mobile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Lightweight combat-result ledger.
 * [B] Prototype bookkeeping only; no XP/drop values here are claimed as original LOD data.
 * This decouples gameplay outcomes from Canvas rendering so verified progression/drop systems can attach later.
 */
public final class CombatLedger {
  public enum Type { PLAYER_HIT, MONSTER_HIT, MONSTER_DEFEATED, PLAYER_DEFEATED, MONSTER_RESPAWNED, PLAYER_REVIVED }

  public static final class Event {
    public final Type type;
    public final String sourceId,targetId;
    public final int amount;
    public final long sequence;
    Event(Type type,String sourceId,String targetId,int amount,long sequence){this.type=type;this.sourceId=sourceId;this.targetId=targetId;this.amount=amount;this.sequence=sequence;}
  }

  private static final int MAX_EVENTS=48;
  private final Deque<Event> events=new ArrayDeque<>();
  private long sequence=0;

  public void add(Type type,String sourceId,String targetId,int amount){
    events.addLast(new Event(type,sourceId,targetId,amount,++sequence));
    while(events.size()>MAX_EVENTS)events.removeFirst();
  }

  public List<Event> snapshot(){return Collections.unmodifiableList(new ArrayList<>(events));}
  public Event latest(){return events.peekLast();}
  public void clear(){events.clear();}
}
