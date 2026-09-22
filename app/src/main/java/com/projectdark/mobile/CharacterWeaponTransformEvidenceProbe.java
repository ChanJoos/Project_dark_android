package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Director gate: mw001 is registered atomically to the BODY dominant-hand anchor per facing. */
public final class CharacterWeaponTransformEvidenceProbe {
  public static final class Sample {
    public final CharacterRenderer.Direction direction;
    public final boolean bodyMirror,weaponMirror,weaponBehindBody;
    public final float handX,handY,attackAngle;
    Sample(CharacterRenderer.Direction d){
      direction=d;
      bodyMirror=CharacterRenderer.bodyMirrorX(d);
      weaponMirror=CharacterRenderer.weaponMirrorX(d);
      weaponBehindBody=CharacterRenderer.weaponBehindBody(d);
      handX=CharacterRenderer.weaponAttackOffsetX(d);
      handY=-CharacterRenderer.weaponAttackOffsetY(d);
      attackAngle=CharacterRenderer.weaponAttackAngle(d,.55f);
    }
    public boolean sane(){
      boolean expectedBodyMirror=false;
      boolean expectedWeaponMirror=false;
      boolean expectedBehind=direction==CharacterRenderer.Direction.NW||direction==CharacterRenderer.Direction.NE;
      return bodyMirror==expectedBodyMirror&&weaponMirror==expectedWeaponMirror&&weaponBehindBody==expectedBehind&&
          handX==0f&&handY==0f&&!Float.isNaN(attackAngle)&&!CharacterRenderer.weaponAttackUsesIndependentSwing();
    }
    @Override public String toString(){return direction+" bodyMirror="+bodyMirror+" weaponMirror="+weaponMirror+
        " behind="+weaponBehindBody+" hand=("+handX+","+handY+") angle="+attackAngle;}
  }

  private CharacterWeaponTransformEvidenceProbe(){}

  public static List<Sample> samples(){
    List<Sample> out=new ArrayList<>();
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())out.add(new Sample(d));
    return Collections.unmodifiableList(out);
  }

  public static boolean passes(){
    List<Sample> s=samples();if(s.size()!=4)return false;
    for(Sample sample:s)if(!sample.sane())return false;
    return true;
  }

  public static String report(){
    StringBuilder b=new StringBuilder("mw001 atomic BODY-hand registration rows=4\n");
    for(Sample s:samples())b.append(s).append('\n');
    return b.toString();
  }
}
