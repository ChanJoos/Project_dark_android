package com.projectdark.mobile;

import java.util.List;

/**
 * Evidence-safe runtime result projection over CombatLedger.
 *
 * This intentionally grants no XP, currency, loot, stat growth, or quest credit.
 * Until original reward rules are verified, it only exposes neutral prototype counters [B].
 */
public final class RuntimeMetrics {
  private long lastSequence=0;
  private int monsterHits=0,monsterDefeats=0,playerHits=0,playerDefeats=0,playerRevives=0;
  private int damageDealt=0,damageTaken=0;

  public void consume(List<CombatLedger.Event> events){
    if(events==null)return;
    for(CombatLedger.Event e:events){
      if(e==null||e.sequence<=lastSequence)continue;
      lastSequence=e.sequence;
      switch(e.type){
        case MONSTER_HIT:monsterHits++;damageDealt+=Math.max(0,e.amount);break;
        case MONSTER_DEFEATED:monsterDefeats++;break;
        case PLAYER_HIT:playerHits++;damageTaken+=Math.max(0,e.amount);break;
        case PLAYER_DEFEATED:playerDefeats++;break;
        case PLAYER_REVIVED:playerRevives++;break;
        default:break;
      }
    }
  }

  public long lastSequence(){return lastSequence;}
  public int monsterHits(){return monsterHits;}
  public int monsterDefeats(){return monsterDefeats;}
  public int playerHits(){return playerHits;}
  public int playerDefeats(){return playerDefeats;}
  public int playerRevives(){return playerRevives;}
  public int damageDealt(){return damageDealt;}
  public int damageTaken(){return damageTaken;}
}
