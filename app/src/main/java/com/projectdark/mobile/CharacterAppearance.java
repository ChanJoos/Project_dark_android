package com.projectdark.mobile;

/**
 * Character-creation appearance contract grounded in Nexon's official character creation guide.
 * The official screenshot visibly exposes two gender choices, eighteen populated hair choices and
 * fourteen populated hair-colour swatches. Exact sprite crops/palette RGB values are not embedded
 * here; procedural rendering remains [O+B]/[ADAPTED] until authenticated sprite extraction exists.
 */
public final class CharacterAppearance {
  public static final String EVIDENCE="O+B";
  public static final String SOURCE_URL=CharacterVisualSourceManifest.CHARACTER_CREATION_SCREENSHOT_URL;
  public static final int VISIBLE_HAIR_STYLE_COUNT=18;
  public static final int VISIBLE_HAIR_COLOR_COUNT=14;

  public enum Gender { MALE, FEMALE }

  /**
   * [ADAPTED_FROM_O] RGB approximations sampled visually from the official guide UI swatches.
   * These are presentation placeholders, not claimed canonical game palette values.
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
  public int previewHairColor(){return HAIR_COLOR_PREVIEW[hairColorIndex];}
  public boolean isMaleSourceBacked(){return gender==Gender.MALE;}
  public boolean exactSpriteCropResolved(){return false;}

  private static int clamp(int value,int min,int max){return Math.max(min,Math.min(max,value));}
}
