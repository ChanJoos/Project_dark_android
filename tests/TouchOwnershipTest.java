import com.projectdark.mobile.ui.TouchOwnership;

public final class TouchOwnershipTest {
  private static void check(boolean value) { if (!value) throw new AssertionError(); }
  public static void main(String[] args) {
    TouchOwnership p = new TouchOwnership();
    check(p.acquireMovement(17));
    check(!p.acquireMovement(4));
    check(!p.release(4));
    check(p.ownsMovement(17));
    check(p.release(17));
    check(p.acquireMovement(31));
    p.cancel();
    check(p.movementPointer() == -1);
    check(p.acquireMovement(4));
    System.out.println("TouchOwnership pointer identity, independent release, cancel: PASS");
  }
}
