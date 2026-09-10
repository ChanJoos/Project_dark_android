package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Render-ready [ADAPTED]/[B] OBJECT layer for the current Milles prototype.
 * Structure footprints stay aligned with WorldDef collision while visual asset refs remain PENDING_CROP.
 */
public final class AdaptedMillesObjectLayer {
  public static final class ObjectInstance {
    public final String id;
    public final AdaptedMillesMapLayer.StructureKind kind;
    public final float footX,footY;
    public final float collisionLeft,collisionTop,collisionRight,collisionBottom;
    public final String assetRef,evidence,status;

    ObjectInstance(AdaptedMillesMapLayer.Structure s){
      id=s.id;kind=s.kind;
      collisionLeft=s.left;collisionTop=s.top;collisionRight=s.right;collisionBottom=s.bottom;
      footX=(s.left+s.right)*.5f;
      footY=s.bottom;
      assetRef=s.assetRef;
      evidence=s.evidence;
      status=s.status;
    }

    /** Painter key shared with characters/NPCs: higher world foot-Y is rendered later. */
    public long depthKey(){
      long y=Math.round(footY*100f);
      long x=Math.round(footX*10f)&0xfffffL;
      return (y<<20)|x;
    }
  }

  private static final List<ObjectInstance> OBJECTS=build();

  private AdaptedMillesObjectLayer(){}
  public static List<ObjectInstance> objects(){return OBJECTS;}

  private static List<ObjectInstance> build(){
    List<ObjectInstance> result=new ArrayList<>();
    for(AdaptedMillesMapLayer.Structure s:AdaptedMillesMapLayer.structures())result.add(new ObjectInstance(s));
    result.sort(Comparator.comparingLong(ObjectInstance::depthKey));
    return Collections.unmodifiableList(result);
  }
}
