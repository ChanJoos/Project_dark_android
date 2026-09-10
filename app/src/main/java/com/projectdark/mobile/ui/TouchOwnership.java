package com.projectdark.mobile.ui;

/** Pointer identity survives index changes; UI fingers never acquire the movement stick on MOVE. */
public final class TouchOwnership {
  private int movementPointer = -1;
  public boolean acquireMovement(int pointerId) {
    if (movementPointer != -1) return false;
    movementPointer = pointerId;
    return true;
  }
  public boolean ownsMovement(int pointerId) { return movementPointer == pointerId; }
  public int movementPointer() { return movementPointer; }
  public boolean release(int pointerId) {
    if (!ownsMovement(pointerId)) return false;
    movementPointer = -1;
    return true;
  }
  public void cancel() { movementPointer = -1; }
}
