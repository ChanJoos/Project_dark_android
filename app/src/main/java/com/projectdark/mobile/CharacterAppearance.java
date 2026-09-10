package com.projectdark.mobile;

/**
 * Character-creation appearance contract grounded in Nexon's official Legend of Darkness guides.
 * Creation UI proves two gender choices, eighteen populated hair choices and fourteen colour swatches.
 * Character-info UI separately proves a rendered female in-game avatar. Exact sprite crops and exact
 * palette values remain unresolved; procedural rendering is [O+B]/[ADAPTED_FROM_O].
 */
public final class CharacterAppearance {
  public static final String EVIDENCE="O+B";
  public static final String CREATION_SOURCE_URL=CharacterVisualSourceManifest.CHARACTER_CREATION_SCREENSHOT_URL;
  public static final String FEMALE_SOURCE_URL=CharacterVisualSourceManifest.CHARACTER_INFO_EQUIPMENT_SCREENSHOT_URL;
  public static final int VISIBLE_HAIR_STYLE_COUNT=18;
  public static final int VISIBLE_HAIR_COLOR_COUNT=14;

  public enum Gender { MALE, FEMALE }

  /**
   * [ADAPTED_FROM_O] Visual approximations of the official creation-screen swatches.
   * They are intentionally not claimed as canonical client RGB values.
   */
  private static final int[] HAIR_COLOR_PREVIEW={
      0xff2c8f89,0xff16911e,0xffa4a946,0xffd1b20b,0xffb06b19,0xffb43b59,0xff813c9d,
      0xff6b20a0,0xff34448d,0xff4850a7,0xff8f929a,0xffa74c07,0xff764520,0xff55595a
  };

  public final Gender gender;
  public final int hairStyleIndex;
  public final int hairColorIndex;

  public CharacterAppearance(Gender gender,int hairStyleIndex,int hairColorIndex){
    this.gender=gender==null?Gender.MALE:gender;
    this.hairStyleIndex=clamp(hairStyleIndex,0,VISIBLE_HAIR_STYLE_COUNT-1);
    this.hairColorIndex=clamp(hairColorIndex,0,VISIBLE_HAIR_COLOR_COUNT-1);
  }

  public static CharacterAppearance defaultGuideMale(){return new CharacterAppearance(Gender.MALE,0,7);}

  /**
   * Official character-info screenshot visibly shows a green-haired female avatar.
   * Hair slot identity is not recoverable from that capture, so style index 5 remains adapted.
   */
  public static CharacterAppearance officialInfoFemalePreview(){return new CharacterAppearance(Gender.FEMALE,5,1);}

  public int previewHairColor(){return HAIR_COLOR_PREVIEW[hairColorIndex];}
  public boolean hasOfficialGenderSilhouetteEvidence(){
    return gender==Gender.MALE?CharacterVisualSourceManifest.MALE_BASE_AVATAR_VISIBLE:
        CharacterVisualSourceManifest.FEMALE_AVATAR_VISIBLE_IN_CHARACTER_INFO;
  }
  public boolean exactSpriteCropResolved(){return false;}

  private static int clamp(int value,int min,int max){return Math.max(min,Math.min(max,value));}
}
