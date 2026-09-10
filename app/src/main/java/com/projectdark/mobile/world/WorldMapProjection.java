package com.projectdark.mobile.world;

import android.graphics.RectF;
import com.projectdark.mobile.WorldDef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Read-only projection of the active world map for renderer/input/runtime wiring. */
public final class WorldMapProjection {
  public static final class Bounds { public final float minX,maxX,minY,maxY; Bounds(float a,float b,float c,float d){minX=a;maxX=b;minY=c;maxY=d;} }
  public static final class Rect { public final float left,top,right,bottom; Rect(float l,float t,float r,float b){left=l;top=t;right=r;bottom=b;} public boolean contains(float x,float y){return x>=left&&x<=right&&y>=top&&y<=bottom;} }
  public static final class Spawn { public final float x,y; Spawn(float x,float y){this.x=x;this.y=y;} }
  public static final class Npc { public final String id,name,assetStatus; public final float x,y; Npc(WorldDef.NpcSpawn n){id=n.id;name=n.name;assetStatus=n.assetStatus;x=n.x;y=n.y;} }
  public static final class Monster { public final String id,name,assetStatus; public final float x,y; Monster(WorldDef.MonsterSpawn m){id=m.id;name=m.name;assetStatus=m.assetStatus;x=m.x;y=m.y;} }
  public static final class Portal { public final String id,targetMapId,evidence,status; public final float x,y,radius; Portal(WorldDef.PortalSpawn p){id=p.portalId;targetMapId=p.targetMapId;evidence=p.evidence;status=p.status;x=p.x;y=p.y;radius=p.radius;} public boolean contains(float wx,float wy){float dx=wx-x,dy=wy-y;return dx*dx+dy*dy<=radius*radius;} }
  public static final class ObjectMarker { public final String id,assetRef,evidence,status; public final float x,y; ObjectMarker(WorldDef.WorldObject o){id=o.objectId;assetRef=o.visualAssetRef;evidence=o.evidence;status=o.status;x=o.x;y=o.y;} }

  private final String mapId,evidence,status;
  private final Bounds bounds;
  private final Spawn spawn;
  private final List<Rect> collision;
  private final List<Npc> npcs;
  private final List<Monster> monsters;
  private final List<Portal> portals;
  private final List<ObjectMarker> objects;
  private final List<WorldExplorationContract.Anchor> anchors;
  private final List<AdaptedMillesMapLayer.Surface> surfaces;
  private final List<AdaptedMillesMapLayer.Structure> structures;
  private final List<AdaptedMillesIsometricTileLayer.Tile> tiles;
  private final List<AdaptedMillesObjectLayer.ObjectInstance> renderObjects;

  private WorldMapProjection(WorldDef world){
    mapId=WorldDef.ID;evidence=WorldDef.EVIDENCE_GEOMETRY;status=WorldDef.GEOMETRY_STATUS;
    bounds=new Bounds(WorldDef.MIN_X,WorldDef.MAX_X,WorldDef.MIN_Y,WorldDef.MAX_Y);
    spawn=new Spawn(WorldDef.PLAYER_SPAWN_X,WorldDef.PLAYER_SPAWN_Y);
    List<Rect> c=new ArrayList<>();for(RectF r:world.blockers())c.add(new Rect(r.left,r.top,r.right,r.bottom));collision=Collections.unmodifiableList(c);
    List<Npc> n=new ArrayList<>();for(WorldDef.NpcSpawn v:world.npcSpawns())n.add(new Npc(v));npcs=Collections.unmodifiableList(n);
    List<Monster> m=new ArrayList<>();for(WorldDef.MonsterSpawn v:world.monsterSpawns())m.add(new Monster(v));monsters=Collections.unmodifiableList(m);
    List<Portal> p=new ArrayList<>();for(WorldDef.PortalSpawn v:world.portalSpawns())p.add(new Portal(v));portals=Collections.unmodifiableList(p);
    List<ObjectMarker> o=new ArrayList<>();for(WorldDef.WorldObject v:world.objects())o.add(new ObjectMarker(v));objects=Collections.unmodifiableList(o);
    anchors=WorldExplorationContract.millesPrototype();
    surfaces=AdaptedMillesMapLayer.surfaces();
    structures=AdaptedMillesMapLayer.structures();
    tiles=AdaptedMillesIsometricTileLayer.tiles();
    renderObjects=AdaptedMillesObjectLayer.objects();
  }

  public static WorldMapProjection from(WorldDef world){if(world==null)throw new IllegalArgumentException("world required");return new WorldMapProjection(world);}
  public String mapId(){return mapId;} public String evidence(){return evidence;} public String status(){return status;}
  public Bounds bounds(){return bounds;} public Spawn spawn(){return spawn;} public List<Rect> collision(){return collision;}
  public List<Npc> npcs(){return npcs;} public List<Monster> monsters(){return monsters;} public List<Portal> portals(){return portals;}
  public List<ObjectMarker> objects(){return objects;} public List<WorldExplorationContract.Anchor> anchors(){return anchors;}
  public List<AdaptedMillesMapLayer.Surface> surfaces(){return surfaces;} public List<AdaptedMillesMapLayer.Structure> structures(){return structures;}
  public List<AdaptedMillesIsometricTileLayer.Tile> tiles(){return tiles;} public List<AdaptedMillesObjectLayer.ObjectInstance> renderObjects(){return renderObjects;}
  public WorldCameraTransform newCamera(float viewportWidth,float viewportHeight){return new WorldCameraTransform(bounds.minX,bounds.maxX,bounds.minY,bounds.maxY,viewportWidth,viewportHeight);}
}
