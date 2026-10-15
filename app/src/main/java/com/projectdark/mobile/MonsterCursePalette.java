package com.projectdark.mobile;

import android.graphics.ColorMatrixColorFilter;

/** User-confirmed curse fading. RGB only: alpha, source pixels and combat authority stay intact. */
public final class MonsterCursePalette {
  // A modest desaturation and cool lift, matching the normal/faded pair in the supplied recording.
  // Construct once; never allocate status textures or filters while drawing a crowded hunt.
  public static final ColorMatrixColorFilter FADED=new ColorMatrixColorFilter(new float[]{
      .615f,.300f,.085f,0,9,
      .165f,.750f,.085f,0,12,
      .165f,.300f,.535f,0,22,
      0,0,0,1,0});
  public static boolean active(SkillEffectState effects,String actor){
    if(effects==null)return false;
    SkillEffectState.Effect curse=effects.get(actor,"CURSE");
    return curse!=null&&curse.remaining>0f;
  }
  private MonsterCursePalette(){}
}
