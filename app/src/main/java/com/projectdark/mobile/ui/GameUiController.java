package com.projectdark.mobile.ui;

import com.projectdark.mobile.RpgInteractionController;
import com.projectdark.mobile.RpgProgressionState;
import com.projectdark.mobile.RuntimeState;

/** UI state and RPG orchestration only. No canonical reward, stat or item values live here. */
public final class GameUiController {
  public enum Window { NONE, INVENTORY }
  private Window window = Window.NONE;
  private final RpgInteractionController rpg = new RpgInteractionController();
  public Window window() { return window; }
  public boolean modal() { return window != Window.NONE; }
  public void openInventory() { window = Window.INVENTORY; }
  public boolean back() {
    if (!modal()) return false;
    window = Window.NONE;
    return true;
  }
  public String pickup(RuntimeState state) {
    if (!state.player().alive) return "행동할 수 없습니다";
    if (!rpg.selectNearestDrop(state.rpg(), state.player().x, state.player().y, 34f))
      return "가까운 전리품이 없습니다";
    switch (rpg.pickupSelected(state.rpg(), state.player().x, state.player().y)) {
      case PICKED_UP: return "전리품을 주웠습니다";
      case INVENTORY_FULL: return "가방이 가득 찼습니다";
      case DROP_TOO_FAR: return "더 가까이 이동하세요";
      default: return "주울 수 없습니다";
    }
  }
  public String equipRow(RuntimeState state, int row) {
    if (!state.player().alive) return "행동할 수 없습니다";
    if (row < 0 || row >= state.rpg().inventory().size()) return "";
    String id = state.rpg().inventory().keySet().toArray(new String[0])[row];
    if (!rpg.selectInventoryItem(state.rpg(), id)) return "아이템이 없습니다";
    return rpg.equipSelected(state.rpg()) == RpgInteractionController.InteractionResult.EQUIPPED
        ? "장착했습니다" : "장착 조건을 충족하지 못했습니다";
  }
  /** Hit blocks match drawn HUD bounds; controls are tested before any world entity. */
  public static boolean hudBlocksWorld(float x, float y) {
    return (y <= 124 && ((x >= 12 && x <= 320) || (x >= 372 && x <= 604) || x >= 714))
        || (x >= 912 && y <= 308)
        || (x >= 12 && x <= 272 && y >= 304 && y <= 382)
        || (y >= 436 && x >= 337);
  }
}
