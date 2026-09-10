package com.projectdark.mobile;

/**
 * Read-only provenance for character visuals confirmed on Nexon's official Legend of Darkness site.
 * This is evidence metadata only: arbitrary screenshots are never promoted to sprite-sheet truth.
 */
public final class CharacterVisualSourceManifest {
  private CharacterVisualSourceManifest(){}

  public static final String EVIDENCE="O";
  public static final String SOURCE_STATUS="SOURCE_FOUND";
  public static final String CROP_STATUS="PENDING_CROP";
  public static final String CHARACTER_CREATION_GUIDE_URL="https://lod.nexon.com/info/guide/82283";
  public static final String CHARACTER_CREATION_SCREENSHOT_URL=
      "https://storage.nexon.com/dsk03/13/NX_FILE/Board/65536/05/1/000/00/00/5557536463815442599.png";

  /** Directly visible in the official guide screenshot; exact sprite pixels/palette remain unresolved. */
  public static final int VISIBLE_GENDER_CHOICES=2;
  public static final int VISIBLE_POPULATED_HAIR_CHOICES=18;
  public static final int VISIBLE_POPULATED_HAIR_COLOR_CHOICES=14;
  public static final boolean MALE_BASE_AVATAR_VISIBLE=true;
  public static final boolean FEMALE_BASE_AVATAR_VISIBLE_IN_CAPTURE=false;

  /** Official character-introduction art. These are class-key art, not sprite-frame sources. */
  public static final String MARTIAL_ARTIST_ART_URL="https://lwi.nexon.com/lod/renewal/brand/sub/gi_c1.jpg";
  public static final String CLERIC_ART_URL="https://lwi.nexon.com/lod/renewal/brand/sub/gi_c2.jpg";
  public static final String WARRIOR_ART_URL="https://lwi.nexon.com/lod/renewal/brand/sub/gi_c3.jpg";
  public static final String ROGUE_ART_URL="https://lwi.nexon.com/lod/renewal/brand/sub/gi_c4.jpg";
  public static final String WIZARD_ART_URL="https://lwi.nexon.com/lod/renewal/brand/sub/gi_c5.jpg";

  public static boolean hasOfficialCreationEvidence(){return true;}
  public static boolean hasAuthenticatedSpriteCrop(){return false;}
}
