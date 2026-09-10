package com.projectdark.mobile;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PROJECT DARK prototype runtime state.
 * [B] Coordinates/collision/combat values are reconstruction fixtures.
 * [ADAPTED] Screen-space geometry validates mobile controls before verified tile collision exists.
 */
public final class RuntimeState {
  public static final float WORLD_MIN_X=WorldDef.MIN_X,WORLD_MAX_X=WorldDef.MAX_X,WORLD_MIN_Y=WorldDef.MIN_Y,WORLD_MAX_Y=WorldDef.MAX_Y;
  public static final float PLAYER_RADIUS=9f,MONSTER_RADIUS=11f,NPC_RADIUS=10f;

  public static final class Player {
    public final float spawnX=WorldDef.PLAYER_SPAWN_X,spawnY=WorldDef.PLAYER_SPAWN_Y;
    public float x=spawnX,y=spawnY;
    public int hp=100,maxHp=100,mp=90,maxMp=100;
    public boolean alive=true;
    public float hitFlash=0f;
  }

  public static final class Npc {
    public final String id,name,dialogue,assetStatus;public final float x,y;
    Npc(String id,String name,float x,float y,String dialogue,String assetStatus){this.id=id;this.name=name;this.x=x;this.y=y;this.dialogue=dialogue;this.assetStatus=assetStatus;}
  }

  public static final class Monster {
    /**
     * Runtime-only lifecycle states. They do not assert original server AI timings/behavior.
     * WANDER/DETECT are reserved contract states and remain unused until evidence-safe policies exist.
     */
    public enum State { SPAWN,IDLE,WANDER,DETECT,CHASE,ATTACK,DEAD,RESPAWN }

    public final String id,name,assetStatus;public final float spawnX,spawnY;public float x,y;
    public int hp;public final int maxHp;public boolean alive=true;
    public State state=State.SPAWN;
    public float attackCooldown=0f,attackWindup=0f,respawnClock=0f,hitFlash=0f,damagePopupClock=0f;
    public boolean attackPrimed=false;
    public int lastDamage=0;
    /** [B] Small bounded steering memory used only to avoid obstacle deadlocks in the prototype. */
    public int detourSign=1;
    public float detourClock=0f;
    Monster(String id,String name,float x,float y,int hp,String assetStatus){this.id=id;this.name=name;this.assetStatus=assetStatus;spawnX=x;spawnY=y;this.x=x;this.y=y;this.hp=hp;maxHp=hp;state=State.IDLE;}
  }

  private final WorldDef world=new WorldDef();
  private final Player player=new Player();
  private final List<RectF> obstacles=new ArrayList<>();
  private final List<Npc> npcs=new ArrayList<>();
  private final List<Monster> monsters=new ArrayList<>();
  private final CombatLedger ledger=new CombatLedger();
  private final RuntimeMetrics metrics=new RuntimeMetrics();
  private final RpgProgressionState rpg=new RpgProgressionState();

  public RuntimeState(){
    if(!RewardPipelineAudit.verify())throw new IllegalStateException("Direct auto-loot contract audit failed");
    if(!MonsterDefeatIdempotencyAudit.verify())throw new IllegalStateException("Monster defeat reward idempotency audit failed");
    if(!MonsterSpawnAdmissionAudit.verify(world))throw new IllegalStateException("Monster spawn admission audit failed");
    if(!RpgEquipmentInteractionAudit.verify())throw new IllegalStateException("RPG equipment interaction audit failed");
    for(RectF r:world.blockers())obstacles.add(new RectF(r));
    for(WorldDef.NpcSpawn n:world.npcSpawns())npcs.add(new Npc(n.id,n.name,n.x,n.y,n.dialogue,n.assetStatus));
    for(WorldDef.MonsterSpawn m:world.monsterSpawns())monsters.add(new Monster(m.id,m.name,m.x,m.y,m.hp,m.assetStatus));
  }

  public WorldDef world(){return world;}
  public Player player(){return player;}
  public CombatLedger ledger(){return ledger;}
  public RuntimeMetrics metrics(){return metrics;}
  public RpgProgressionState rpg(){return rpg;}
  public List<RectF> obstacles(){return Collections.unmodifiableList(obstacles);}
  public List<Npc> npcs(){return Collections.unmodifiableList(npcs);}
  public List<Monster> monsters(){return Collections.unmodifiableList(monsters);}

  public boolean tryMove(float dx,float dy){
    if(!player.alive)return false;float bx=player.x,by=player.y;
    float nx=clamp(player.x+dx,WORLD_MIN_X,WORLD_MAX_X),ny=clamp(player.y+dy,WORLD_MIN_Y,WORLD_MAX_Y);
    if(playerCanOccupy(nx,player.y))player.x=nx;
    if(playerCanOccupy(player.x,ny))player.y=ny;
    return player.x!=bx||player.y!=by;
  }

  /**
   * [B] Bounded local steering for prototype monster pursuit.
   * The caller still chooses the pursuit vector; this method only prevents simple blocker/NPC/monster deadlocks.
   * It is not claimed to reproduce original server pathfinding.
   */
  public boolean tryMoveMonster(Monster m,float dx,float dy){
    if(m==null||!m.alive)return false;
    if(m.state!=Monster.State.ATTACK)m.state=Monster.State.CHASE;
    float bx=m.x,by=m.y;
    moveMonsterAxes(m,dx,dy);
    if(m.x!=bx||m.y!=by){m.detourClock=Math.max(0f,m.detourClock-.05f);return true;}

    m.detourClock+=.05f;
    float px=-dy*m.detourSign,py=dx*m.detourSign;
    moveMonsterAxes(m,px,py);
    if(m.x!=bx||m.y!=by)return true;

    m.detourSign=-m.detourSign;
    px=-dy*m.detourSign;py=dx*m.detourSign;
    moveMonsterAxes(m,px,py);
    if(m.x!=bx||m.y!=by)return true;

    if(m.detourClock>.75f){m.detourSign=-m.detourSign;m.detourClock=0f;}
    return false;
  }

  private void moveMonsterAxes(Monster m,float dx,float dy){
    float nx=clamp(m.x+dx,WORLD_MIN_X,WORLD_MAX_X),ny=clamp(m.y+dy,WORLD_MIN_Y,WORLD_MAX_Y);
    if(monsterCanOccupy(m,nx,m.y))m.x=nx;
    if(monsterCanOccupy(m,m.x,ny))m.y=ny;
  }

  private boolean playerCanOccupy(float x,float y){
    return !blocked(x,y,PLAYER_RADIUS)&&!monsterOccupied(null,x,y,PLAYER_RADIUS)&&!npcOccupied(x,y,PLAYER_RADIUS);
  }

  private boolean monsterCanOccupy(Monster self,float x,float y){
    return !blocked(x,y,MONSTER_RADIUS)&&!playerOccupied(x,y,MONSTER_RADIUS)&&!monsterOccupied(self,x,y,MONSTER_RADIUS)&&!npcOccupied(x,y,MONSTER_RADIUS);
  }

  public boolean blocked(float x,float y){return blocked(x,y,PLAYER_RADIUS);}
  private boolean blocked(float x,float y,float radius){for(RectF r:obstacles)if(x+radius>r.left&&x-radius<r.right&&y+radius>r.top&&y-radius<r.bottom)return true;return false;}
  private boolean playerOccupied(float x,float y,float radius){if(!player.alive)return false;float min=radius+PLAYER_RADIUS+3f;return distance(x,y,player.x,player.y)<min;}
  private boolean monsterOccupied(Monster self,float x,float y,float radius){for(Monster m:monsters){if(m==self||!m.alive)continue;float min=radius+MONSTER_RADIUS+3f;if(distance(x,y,m.x,m.y)<min)return true;}return false;}
  private boolean npcOccupied(float x,float y,float radius){for(Npc n:npcs){float min=radius+NPC_RADIUS+2f;if(distance(x,y,n.x,n.y)<min)return true;}return false;}

  public Npc hitNpc(float x,float y,float radius){for(Npc n:npcs){float dx=x-n.x,dy=y-n.y;if(dx*dx+dy*dy<=radius*radius)return n;}return null;}
  public Monster hitMonster(float x,float y,float radius){for(Monster m:monsters()){if(!m.alive)continue;float dx=x-m.x,dy=y-m.y;if(dx*dx+dy*dy<=radius*radius)return m;}return null;}
  public float distanceTo(Npc n){return distance(player.x,player.y,n.x,n.y);}
  public float distanceTo(Monster m){return distance(player.x,player.y,m.x,m.y);}

  /** [B] Neutral combat telegraph scaffold; not an original monster animation/timing claim. */
  public void beginMonsterAttack(Monster m){if(m!=null&&m.alive&&!m.attackPrimed&&m.attackCooldown<=0f){m.state=Monster.State.ATTACK;m.attackPrimed=true;m.attackWindup=.24f;}}
  public boolean monsterAttackReady(Monster m){return m!=null&&m.alive&&m.state==Monster.State.ATTACK&&m.attackPrimed&&m.attackWindup<=0f&&m.attackCooldown<=0f;}
  public void resolveMonsterAttack(Monster m,int damage,float cooldown){
    if(!monsterAttackReady(m))return;
    m.attackPrimed=false;m.attackWindup=0f;m.attackCooldown=Math.max(0f,cooldown);damagePlayer(damage);
    if(m.alive)m.state=Monster.State.IDLE;
  }
  public void cancelMonsterAttack(Monster m){if(m!=null){m.attackPrimed=false;m.attackWindup=0f;if(m.alive)m.state=Monster.State.IDLE;}}

  public void damage(Monster m,int amount){
    if(m==null||!m.alive||amount<=0)return;
    m.hp=Math.max(0,m.hp-amount);m.hitFlash=.14f;m.damagePopupClock=.65f;m.lastDamage=amount;
    ledger.add(CombatLedger.Type.MONSTER_HIT,"player",m.id,amount);
    if(m.hp==0){m.alive=false;m.state=Monster.State.DEAD;m.attackCooldown=0f;m.attackWindup=0f;m.attackPrimed=false;m.detourClock=0f;m.respawnClock=4f;ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player",m.id,0);}
  }

  public void damagePlayer(int amount){
    if(amount<=0||!player.alive)return;
    player.hp=Math.max(0,player.hp-amount);player.hitFlash=.18f;ledger.add(CombatLedger.Type.PLAYER_HIT,"monster","player",amount);
    if(player.hp==0){player.alive=false;ledger.add(CombatLedger.Type.PLAYER_DEFEATED,"monster","player",0);for(Monster m:monsters)if(m.alive){cancelMonsterAttack(m);m.state=Monster.State.IDLE;}}
  }

  public void revivePlayer(){player.x=player.spawnX;player.y=player.spawnY;player.hp=player.maxHp;player.mp=player.maxMp;player.alive=true;player.hitFlash=0f;ledger.add(CombatLedger.Type.PLAYER_REVIVED,"runtime","player",0);}

  public void tick(float dt){
    player.hitFlash=Math.max(0f,player.hitFlash-dt);
    for(Monster m:monsters){
      m.attackCooldown=Math.max(0f,m.attackCooldown-dt);m.hitFlash=Math.max(0,m.hitFlash-dt);m.damagePopupClock=Math.max(0,m.damagePopupClock-dt);m.attackWindup=Math.max(0,m.attackWindup-dt);m.detourClock=Math.max(0f,m.detourClock-dt*.25f);
      if(m.alive)continue;
      if(m.state==Monster.State.DEAD)m.state=Monster.State.RESPAWN;
      m.respawnClock=Math.max(0f,m.respawnClock-dt);
      if(m.respawnClock<=0f){m.state=Monster.State.SPAWN;m.x=m.spawnX;m.y=m.spawnY;m.hp=m.maxHp;m.attackCooldown=0f;m.attackWindup=0f;m.attackPrimed=false;m.detourClock=0f;m.detourSign=1;m.alive=true;m.lastDamage=0;m.state=Monster.State.IDLE;ledger.add(CombatLedger.Type.MONSTER_RESPAWNED,"runtime",m.id,0);}
    }
    List<CombatLedger.Event> events=ledger.snapshot();
    metrics.consume(events);
    rpg.consumeCombat(events,this);
  }

  private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);}
  private static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}
}
