package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data-only spatial semantics for an explorable map. Rendering may use these areas for road/plaza/gate
 * presentation while navigation/UX may use anchors without hard-coding coordinates in GameView.
 */
public final class WorldSpatialLayout {
  public enum AreaKind { ROAD, PLAZA, GATE, OPEN_SPACE }

  public static final class Area {
    public final String id;
    public final AreaKind kind;
    public final float left,top,right,bottom;
    public final String evidence,status;
    public Area(String id,AreaKind kind,float left,float top,float right,float bottom,String evidence,String status){
      if(id==null||id.isEmpty()||kind==null)throw new IllegalArgumentException("id/kind required");
      if(right<=left||bottom<=top)throw new IllegalArgumentException("invalid bounds");
      this.id=id;this.kind=kind;this.left=left;this.top=top;this.right=right;this.bottom=bottom;
      this.evidence=evidence;this.status=status;
    }
    public boolean contains(float x,float y){return x>=left&&x<=right&&y>=top&&y<=bottom;}
  }

  public static final class Anchor {
    public final String id;
    public final float x,y;
    public final String purpose,evidence,status;
    public Anchor(String id,float x,float y,String purpose,String evidence,String status){
      if(id==null||id.isEmpty())throw new IllegalArgumentException("id required");
      this.id=id;this.x=x;this.y=y;this.purpose=purpose;this.evidence=evidence;this.status=status;
    }
  }

  private final List<Area> areas;
  private final List<Anchor> anchors;

  public WorldSpatialLayout(List<Area> areas,List<Anchor> anchors){
    this.areas=Collections.unmodifiableList(new ArrayList<>(areas));
    this.anchors=Collections.unmodifiableList(new ArrayList<>(anchors));
  }

  public List<Area> areas(){return areas;}
  public List<Anchor> anchors(){return anchors;}

  public Area areaAt(float x,float y){
    for(Area a:areas)if(a.contains(x,y))return a;
    return null;
  }

  public Anchor anchor(String id){
    if(id==null)return null;
    for(Anchor a:anchors)if(id.equals(a.id))return a;
    return null;
  }
}
