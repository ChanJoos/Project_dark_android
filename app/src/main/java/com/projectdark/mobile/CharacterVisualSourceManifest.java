package com.projectdark.mobile;

/**
 * Read-only provenance for character visuals confirmed on Nexon's official Legend of Darkness site.
 * This is evidence metadata only: it does not grant permission to treat arbitrary screenshots as sprite sheets.
 */
public final class CharacterVisualSourceManifest {
  private CharacterVisualSourceManifest(){}

  public static final String EVIDENCE="O";
  public static final String SOURCE_STATUS="SOURCE_FOUND";
  public static final String CROP_STATUS="PENDING_CROP";

  /** Official Nexon guide page documenting character creation. */
  public static final String CHARACTER_CREATION_GUIDE_URL="https://lod.nexon.com/info/guide/82283";

  /**
   * Official Nexon-hosted screenshot used only as proportion / silhouette / hair evidence.
   * It visibly contains the in-game male base avatar, hair choices and hair-colour choices.
   */
  public static final String CHARACTER_CREATION_SCREENSHOT_URL=
      "https://storage.nexon.com/dsk03/13/NX_FILE/Board/65536/05/1/000/00/00/5557536463815442599.png";

  /** Official character-introduction art. These are class-key art, not sprite-frame sources. */
  public static final String MARTIAL_ARTIST_ART_URL="https://lwi.nexon.com/lod/renewal/brand/sub/gi_c1.jpg";
  public static final String CLERIC_ART_URL="https://lwi.nexon.com/lod/renewal/brand/sub/gi_c2.jpg";
  public static final String WARRIOR_ART_URL="https://lwi.nexon.com/lod/renewal/brand/sub/gi_c3.jpg";
  public static final String ROGUE_ART_URL="https://lwi.nexon.com/lod/renewal/brand/sub/gi_c4.jpg";
  public static final String WIZARD_ART_URL="https://lwi.nexon.com/lod/renewal/brand/sub/gi_c5.jpg";

  public static boolean hasOfficialCreationEvidence(){return true;}
  public static boolean hasAuthenticatedSpriteCrop(){return false;}
}
