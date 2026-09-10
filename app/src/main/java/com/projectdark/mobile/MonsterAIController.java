package com.projectdark.mobile;

/**
 * Combat-owned deterministic monster decision state machine.
 *
 * Movement is delegated to World and damage/effect timing is delegated to the shared
 * CombatResolver. AI only decides when to detect, chase, telegraph and submit an AUTO action.
 */
public final class MonsterAIController {
  public enum State { IDLE, DETECT, CHASE, WINDUP, ATTACK, RECOVER, CANCELLED, DEAD }
  public enum CancelReason { NONE, TARGET_LOST, OUT_OF_CANCEL_RANGE, LOS_BLOCKED, MOVE_BLOCKED, MONSTER_DEAD, TARGET_DEAD }
  public enum EventType { STATE_CHANGED, ATTACK_REQUESTED, ATTACK_REJECTED, CANCELLED, DEAD }

  public static final class Config {
    public final float detectRange,attackRange,cancelRange,moveSpeed,windupSeconds,attackPoseSeconds,recoverySeconds,blockedTimeout;
    public Config(float detectRange,float attackRange,float cancelRange,float moveSpeed,float windupSeconds,
        float attackPoseSeconds,float recoverySeconds,float blockedTimeout){
      if(detectRange<=0f||attackRange<=0f||cancelRange<detectRange||moveSpeed<=0f||windupSeconds<0f||
          attackPoseSeconds<0f||recoverySeconds<0f||blockedTimeout<=0f)throw new IllegalArgumentException("invalid AI config");
      this.detectRange=detectRange;this.attackRange=attackRange;this.cancelRange=cancelRange;this.moveSpeed=moveSpeed;
      this.windupSeconds=windupSeconds;this.attackPoseSeconds=attackPoseSeconds;
      this.recoverySeconds=recoverySeconds;this.blockedTimeout=blockedTimeout;
    }
  }

  public static final class AttackRequest {
    public final String monsterId,targetId,actionId;
    public final CombatResolver.InputMode inputMode=CombatResolver.InputMode.AUTO;
    private AttackRequest(String monsterId,String targetId,String actionId){
      this.monsterId=monsterId;this.targetId=targetId;this.actionId=actionId;
    }
  }

  public static final class Snapshot {
    public final long sequence;
    public final String monsterId,targetId,actionId;
    public final State state;
    public final CancelReason cancelReason;
    public final EventType lastEvent;
    public final float stateTime,blockedTime;
    private Snapshot(long sequence,String monsterId,String targetId,String actionId,State state,
        CancelReason cancelReason,EventType lastEvent,float stateTime,float blockedTime){
      this.sequence=sequence;this.monsterId=monsterId;this.targetId=targetId;this.actionId=actionId;
      this.state=state;this.cancelReason=cancelReason;this.lastEvent=lastEvent;
      this.stateTime=stateTime;this.blockedTime=blockedTime;
    }
  }

  public interface Port {
    boolean monsterAlive(String monsterId);
    boolean targetAlive(String targetId);
    float distance(String monsterId,String targetId);
    boolean hasLineOfSight(String monsterId,String targetId);
    /** World-authoritative collision move. AI never writes coordinates directly. */
    boolean tryMoveToward(String monsterId,String targetId,float maxDistance);
    /** Adapter must submit this through the same CombatResolver used by manual input. */
    boolean submitAttack(AttackRequest request);
  }

  private final String monsterId,actionId;
  private final Config config;
  private final Port port;
  private String targetId;
  private State state=State.IDLE;
  private CancelReason cancelReason=CancelReason.NONE;
  private EventType lastEvent=EventType.STATE_CHANGED;
  private long sequence;
  private float stateTime,blockedTime;

  public MonsterAIController(String monsterId,String actionId,Config config,Port port){
    if(blank(monsterId)||blank(actionId)||config==null||port==null)throw new IllegalArgumentException("AI dependencies required");
    this.monsterId=monsterId;this.actionId=actionId;this.config=config;this.port=port;
  }

  public Snapshot acquireTarget(String targetId){
    if(blank(targetId))return cancel(CancelReason.TARGET_LOST);
    this.targetId=targetId;cancelReason=CancelReason.NONE;blockedTime=0f;
    if(!port.monsterAlive(monsterId))return transition(State.DEAD,EventType.DEAD);
    if(!port.targetAlive(targetId))return cancel(CancelReason.TARGET_DEAD);
    if(port.distance(monsterId,targetId)<=config.detectRange&&port.hasLineOfSight(monsterId,targetId))
      return transition(State.DETECT,EventType.STATE_CHANGED);
    return transition(State.IDLE,EventType.STATE_CHANGED);
  }

  public Snapshot tick(float deltaSeconds){
    if(deltaSeconds<=0f)return snapshot();
    if(!port.monsterAlive(monsterId))return cancelDead();
    if(blank(targetId))return state==State.CANCELLED?snapshot():cancel(CancelReason.TARGET_LOST);
    if(!port.targetAlive(targetId))return cancel(CancelReason.TARGET_DEAD);
    float distance=port.distance(monsterId,targetId);
    if(distance>config.cancelRange)return cancel(CancelReason.OUT_OF_CANCEL_RANGE);
    stateTime+=deltaSeconds;
    switch(state){
      case IDLE:
        if(distance<=config.detectRange&&port.hasLineOfSight(monsterId,targetId))return transition(State.DETECT,EventType.STATE_CHANGED);
        return snapshot();
      case DETECT:
        return transition(distance<=config.attackRange?State.WINDUP:State.CHASE,EventType.STATE_CHANGED);
      case CHASE:
        if(distance<=config.attackRange){
          if(!port.hasLineOfSight(monsterId,targetId))return cancel(CancelReason.LOS_BLOCKED);
          return transition(State.WINDUP,EventType.STATE_CHANGED);
        }
        if(!port.tryMoveToward(monsterId,targetId,config.moveSpeed*deltaSeconds)){
          blockedTime+=deltaSeconds;
          if(blockedTime>=config.blockedTimeout)return cancel(CancelReason.MOVE_BLOCKED);
        }else blockedTime=0f;
        return snapshot();
      case WINDUP:
        if(distance>config.attackRange)return transition(State.CHASE,EventType.STATE_CHANGED);
        if(!port.hasLineOfSight(monsterId,targetId))return cancel(CancelReason.LOS_BLOCKED);
        if(stateTime<config.windupSeconds)return snapshot();
        AttackRequest request=new AttackRequest(monsterId,targetId,actionId);
        if(!port.submitAttack(request))return transition(State.RECOVER,EventType.ATTACK_REJECTED);
        return transition(State.ATTACK,EventType.ATTACK_REQUESTED);
      case ATTACK:
        return stateTime>=config.attackPoseSeconds?transition(State.RECOVER,EventType.STATE_CHANGED):snapshot();
      case RECOVER:
        return stateTime>=config.recoverySeconds?transition(State.IDLE,EventType.STATE_CHANGED):snapshot();
      case CANCELLED:
      case DEAD:
      default:
        return snapshot();
    }
  }

  public Snapshot clearTarget(){targetId=null;return cancel(CancelReason.TARGET_LOST);}
  public Snapshot onRespawn(){targetId=null;cancelReason=CancelReason.NONE;blockedTime=0f;return transition(State.IDLE,EventType.STATE_CHANGED);}
  public Snapshot snapshot(){return new Snapshot(sequence,monsterId,targetId,actionId,state,cancelReason,lastEvent,stateTime,blockedTime);}

  private Snapshot cancelDead(){
    targetId=null;cancelReason=CancelReason.MONSTER_DEAD;blockedTime=0f;
    return transition(State.DEAD,EventType.DEAD);
  }
  private Snapshot cancel(CancelReason reason){
    cancelReason=reason;blockedTime=0f;
    return transition(State.CANCELLED,EventType.CANCELLED);
  }
  private Snapshot transition(State next,EventType event){
    if(state!=next||lastEvent!=event){state=next;lastEvent=event;stateTime=0f;sequence++;}
    return snapshot();
  }
  private static boolean blank(String value){return value==null||value.trim().isEmpty();}
}
