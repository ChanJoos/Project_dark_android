package com.projectdark.mobile;

/**
 * World/NPC interaction orchestration extracted from renderer-owned policy.
 *
 * <p>[B] Interaction distance, approach speed, timeout, and detour policy are prototype
 * reconstruction values only. They are deliberately isolated here so verified original values can
 * replace them without rewriting GameView or RuntimeState.</p>
 *
 * <p>[ADAPTED] Tap-to-approach is a mobile interaction convenience. It still moves through
 * RuntimeState.tryMove(), so collision/occupancy rules are not bypassed.</p>
 */
public final class InteractionController {
  public enum TickResult { IDLE, WALKING, DIALOG_OPENED, BLOCKED }

  private static final float INTERACTION_RANGE = 56f; // [B]
  private static final float APPROACH_SPEED = 92f;    // [B]
  private static final float BLOCK_TIMEOUT = 2f;      // [B]
  private static final float DIAGONAL = .70710677f;

  private RuntimeState.Npc approachNpc;
  private RuntimeState.Npc dialogNpc;
  private float blockedClock;
  private float moveX;
  private float moveY;
  private String feedback;

  public RuntimeState.Npc approachNpc(){ return approachNpc; }
  public RuntimeState.Npc dialogNpc(){ return dialogNpc; }
  public boolean approaching(){ return approachNpc != null; }
  public boolean dialogOpen(){ return dialogNpc != null; }
  public float moveX(){ return moveX; }
  public float moveY(){ return moveY; }

  /** Selects an NPC. Nearby NPCs open immediately; distant NPCs become an approach target. */
  public TickResult request(RuntimeState state, RuntimeState.Npc npc){
    if(state == null || npc == null || !state.player().alive) return TickResult.IDLE;
    feedback = null;
    blockedClock = 0f;
    if(state.distanceTo(npc) <= INTERACTION_RANGE){
      dialogNpc = npc;
      approachNpc = null;
      moveX = moveY = 0f;
      feedback = "대화 시작 · " + dialogNpc.name;
      return TickResult.DIALOG_OPENED;
    }
    dialogNpc = null;
    approachNpc = npc;
    updateDirection(state);
    return TickResult.WALKING;
  }

  /**
   * Advances tap-to-approach using the canonical four screen-diagonal direction vocabulary.
   * The controller never directly mutates player coordinates; RuntimeState owns collision checks.
   */
  public TickResult tick(RuntimeState state, float dt){
    if(state == null || !state.player().alive){ cancel(); return TickResult.IDLE; }
    if(approachNpc == null) return TickResult.IDLE;
    if(state.distanceTo(approachNpc) <= INTERACTION_RANGE){
      dialogNpc = approachNpc;
      approachNpc = null;
      blockedClock = 0f;
      moveX = moveY = 0f;
      feedback = "대화 시작 · " + dialogNpc.name;
      return TickResult.DIALOG_OPENED;
    }

    updateDirection(state);
    float dx = moveX * APPROACH_SPEED * dt;
    float dy = moveY * APPROACH_SPEED * dt;
    if(state.tryMove(dx, dy)){
      blockedClock = 0f;
      return TickResult.WALKING;
    }

    blockedClock += dt;
    // [B] deterministic perpendicular detours, preserving four-direction diagonal movement.
    boolean moved = state.tryMove(dx, -dy);
    if(!moved) moved = state.tryMove(-dx, dy);
    if(moved){
      blockedClock = Math.max(0f, blockedClock - .12f);
      return TickResult.WALKING;
    }

    if(blockedClock > BLOCK_TIMEOUT){
      RuntimeState.Npc blockedNpc = approachNpc;
      approachNpc = null;
      blockedClock = 0f;
      moveX = moveY = 0f;
      feedback = blockedNpc == null
          ? "NPC 접근 실패 [B]"
          : blockedNpc.name + " 접근 실패 · 직접 이동 후 다시 탭 [B]";
      return TickResult.BLOCKED;
    }
    return TickResult.WALKING;
  }

  public void dismissDialog(){ dialogNpc = null; }

  /** Manual movement/combat input has priority over mobile auto-approach. */
  public void cancelApproach(){
    approachNpc = null;
    blockedClock = 0f;
    moveX = moveY = 0f;
  }

  public void cancel(){
    cancelApproach();
    dialogNpc = null;
    feedback = null;
  }

  public String consumeFeedback(){
    String out = feedback;
    feedback = null;
    return out;
  }

  private void updateDirection(RuntimeState state){
    float dx = approachNpc.x - state.player().x;
    float dy = approachNpc.y - state.player().y;
    moveX = (dx >= 0f ? DIAGONAL : -DIAGONAL);
    moveY = (dy >= 0f ? DIAGONAL : -DIAGONAL);
  }
}
