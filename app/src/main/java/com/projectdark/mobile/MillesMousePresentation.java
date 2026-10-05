package com.projectdark.mobile;

/** ADAPTED early-game field actors presented as mice while keeping combat IDs stable. */
public final class MillesMousePresentation {
  private MillesMousePresentation() {}

  public static boolean isMouse(String id) {
    return F5mAdaptedPrologueQuest.OPENING_MONSTER_ID.equals(id) || isEarlyFieldMouse(id);
  }

  public static boolean isEarlyFieldMouse(String id) {
    return "combat_dummy_01".equals(id) || "combat_dummy_02".equals(id) || "combat_dummy_03".equals(id);
  }

  public static String fieldName(String id) {
    if ("combat_dummy_01".equals(id)) return "들쥐 A [ADAPTED]";
    if ("combat_dummy_02".equals(id)) return "들쥐 B [ADAPTED]";
    if ("combat_dummy_03".equals(id)) return "들쥐 C [ADAPTED]";
    return null;
  }
}
