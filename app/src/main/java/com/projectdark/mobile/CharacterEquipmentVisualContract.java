package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * [O+B] Presentation-only equipment silhouette contract grounded in Nexon's official
 * character-info guide. The screenshot visibly surrounds the avatar with distinct wearable slots,
 * but exact per-item sprite crops are not extracted here.
 */
public final class CharacterEquipmentVisualContract {
  public static final String EVIDENCE="O+B";
  public static final String SOURCE_URL=CharacterVisualSourceManifest.CHARACTER_INFO_EQUIPMENT_SCREENSHOT_URL;
  public static final String SPRITE_STATUS="PENDING_CROP";

  public enum VisualRegion {
    HEAD,
    FACE_OR_NECK,
    TORSO,
    HANDS,
    MAIN_HAND,
    OFF_HAND,
    LOWER_BODY,
    FEET,
    ACCESSORY_LEFT,
    ACCESSORY_RIGHT
  }

  /**
   * Regions are deliberately semantic, not canonical RPG slot names. Integrator/RPG may map
   * stable item-slot IDs to these presentation regions without changing renderer ownership.
   */
  public static final List<VisualRegion> VISIBLE_REGIONS=Collections.unmodifiableList(Arrays.asList(
      VisualRegion.HEAD,
      VisualRegion.FACE_OR_NECK,
      VisualRegion.TORSO,
      VisualRegion.HANDS,
      VisualRegion.MAIN_HAND,
      VisualRegion.OFF_HAND,
      VisualRegion.LOWER_BODY,
      VisualRegion.FEET,
      VisualRegion.ACCESSORY_LEFT,
      VisualRegion.ACCESSORY_RIGHT));

  private CharacterEquipmentVisualContract(){}

  public static boolean hasOfficialLayoutEvidence(){return CharacterVisualSourceManifest.EQUIPMENT_SLOT_LAYOUT_VISIBLE;}
  public static boolean hasResolvedItemSprites(){return false;}
}
