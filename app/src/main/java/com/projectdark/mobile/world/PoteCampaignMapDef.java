package com.projectdark.mobile.world;

import android.graphics.RectF;
import java.util.*;

/** Map-local playable geometry for the four campaign regions. A retains the original authored field. */
public final class PoteCampaignMapDef {
  private static final Map<String,PoteCampaignMapDef> CACHE=new HashMap<>();
  public final String id,title;
  public final int zone;
  public final float minX,maxX,minY,maxY,entryX,entryY,backX,backY,nextX,nextY,exitRadius;
  private List<WorldMoveTargetController.TileCenter> navigation,ground;
  private List<RectF> obstacles;
  private Map<Long,WorldMoveTargetController.TileCenter> centerLookup;
  public WorldMoveTargetController.TileCenter exactCenter(float x,float y){if(Math.abs(x-Math.round(x))>=.01f||Math.abs(y-Math.round(y))>=.01f)return null;if(centerLookup==null){centerLookup=new HashMap<>();for(WorldMoveTargetController.TileCenter t:navigationTiles())centerLookup.put(((long)Math.round(t.x)<<32)^(Math.round(t.y)&0xffffffffL),t);}return centerLookup.get(((long)Math.round(x)<<32)^(Math.round(y)&0xffffffffL));}
  public boolean isNavigationCenter(float x,float y){return exactCenter(x,y)!=null;}
  private TerrainObstacleIndex index;
  public boolean terrainBlocked(float x,float y,float radius){if(CountessCastleGeometry.contains(id)&&!CountessCastleGeometry.inside(id,x,y))return true;if(DeathVillageGeometry.contains(id)&&!DeathVillageGeometry.inside(id,x,y))return true;if(MineHuntGeometry.contains(id)&&!MineHuntGeometry.inside(x,y))return true;if(HorrorCastleGeometry.contains(id)&&!HorrorCastleGeometry.inside(id,x,y,radius))return true;if(index==null)index=new TerrainObstacleIndex(obstacles());return index.blocked(x,y,radius);}

  private PoteCampaignMapDef(String id,int zone,String title,float minX,float maxX,float minY,float maxY,
      float entryX,float entryY,float backX,float backY,float nextX,float nextY,float exitRadius){
    this.id=id;this.zone=zone;this.title=title;this.minX=minX;this.maxX=maxX;this.minY=minY;this.maxY=maxY;
    this.entryX=entryX;this.entryY=entryY;this.backX=backX;this.backY=backY;this.nextX=nextX;this.nextY=nextY;this.exitRadius=exitRadius;
  }

  public static PoteCampaignMapDef forId(String id){id=AbelCoastGeometry.canonical(AbelTownGeometry.canonical(id));String key=CountessCastleGeometry.contains(id)||DeathVillageGeometry.contains(id)||MineHuntGeometry.contains(id)||MineTownGeometry.contains(id)||HorrorCastleGeometry.contains(id)||RucesionCoastGeometry.contains(id)||RucesionTownGeometry.contains(id)||AbelTownGeometry.contains(id)||AbelCoastGeometry.contains(id)?id:CampaignWorld.PIET.equals(id)||CampaignWorld.BOSS_D.equals(id)||"MAP_POTE_04".equals(id)?id:id!=null&&id.startsWith("MAP_POTE_")&&id.length()==11?id:"MAP_POTE_01";synchronized(CACHE){PoteCampaignMapDef d=CACHE.get(key);if(d==null){d=create(key);CACHE.put(key,d);}return d;}}
  private static PoteCampaignMapDef create(String id){if(CountessCastleGeometry.contains(id)){WorldMoveTargetController.TileCenter e=CountessCastleGeometry.entry(id);return new PoteCampaignMapDef(id,120+CountessCastleGeometry.zone(id),CountessCastleGeometry.title(id),CountessCastleGeometry.minX(id),CountessCastleGeometry.maxX(id),0,CountessCastleGeometry.maxY(id),e.x,e.y,e.x,e.y,e.x,e.y,22);}
 if(DeathVillageGeometry.contains(id)){WorldMoveTargetController.TileCenter e=DeathVillageGeometry.entry(id);return new PoteCampaignMapDef(id,100,DeathVillageGeometry.title(id),DeathVillageGeometry.minX(id),DeathVillageGeometry.maxX(id),0,DeathVillageGeometry.maxY(id),e.x,e.y,e.x,e.y,e.x,e.y,22);}
    if(MineHuntGeometry.contains(id)){WorldMoveTargetController.TileCenter e=MineHuntGeometry.entry(),back=MineHuntGeometry.exit();return new PoteCampaignMapDef(id,90,"카스마늄제1·3갱도",96,8256,0,4300,e.x,e.y,back.x,back.y,e.x,e.y,12);}
    if(MineTownGeometry.contains(id))return new PoteCampaignMapDef(id,80,"마인 마을",0,4096,0,2176,MineTownGeometry.x(37,25),MineTownGeometry.y(37,25),MineTownGeometry.x(37,25),MineTownGeometry.y(37,25),MineTownGeometry.x(37,25),MineTownGeometry.y(37,25),22);
    if(HorrorCastleGeometry.contains(id)){WorldMoveTargetController.TileCenter e=HorrorCastleGeometry.entry(id);return new PoteCampaignMapDef(id,60+HorrorCastleGeometry.room(id),HorrorCastleGeometry.title(id),HorrorCastleGeometry.minX(id),HorrorCastleGeometry.maxX(id),0,HorrorCastleGeometry.maxY(id),e.x,e.y,e.x,e.y,e.x,e.y,22);}
    if(RucesionCoastGeometry.contains(id))return new PoteCampaignMapDef(id,40+RucesionCoastGeometry.index(id),RucesionCoastGeometry.title(id),64,RucesionCoastGeometry.width(id),64,RucesionCoastGeometry.height(id),CoastalHuntLayout.entryX(RucesionCoastGeometry.index(id)),CoastalHuntLayout.entryY(RucesionCoastGeometry.index(id)),CoastalHuntLayout.backX(RucesionCoastGeometry.index(id)),CoastalHuntLayout.backY(RucesionCoastGeometry.index(id)),CoastalHuntLayout.nextX(RucesionCoastGeometry.index(id)),CoastalHuntLayout.nextY(RucesionCoastGeometry.index(id)),40);
    if(RucesionTownGeometry.contains(id))return new PoteCampaignMapDef(id,30,"뤼케시온 마을",0,6656,0,3584,RucesionTownGeometry.x(41,41),RucesionTownGeometry.y(41,41),RucesionTownGeometry.x(41,41),RucesionTownGeometry.y(41,41),RucesionTownGeometry.x(41,41),RucesionTownGeometry.y(41,41),40);
    if(AbelTownGeometry.contains(id))return new PoteCampaignMapDef(AbelTownGeometry.ID,9,"아벨 마을",0,6144,0,3584,AbelTownGeometry.x(22,18),AbelTownGeometry.y(22,18),AbelTownGeometry.x(22,18),AbelTownGeometry.y(22,18),AbelTownGeometry.x(81,39),AbelTownGeometry.y(81,39),40);
    if(AbelCoastGeometry.contains(id))return new PoteCampaignMapDef(id,10+AbelCoastGeometry.index(id),AbelCoastGeometry.title(id),64,AbelCoastGeometry.width(id),64,AbelCoastGeometry.height(id),CoastalHuntLayout.entryX(AbelCoastGeometry.index(id)),CoastalHuntLayout.entryY(AbelCoastGeometry.index(id)),CoastalHuntLayout.backX(AbelCoastGeometry.index(id)),CoastalHuntLayout.backY(AbelCoastGeometry.index(id)),CoastalHuntLayout.nextX(AbelCoastGeometry.index(id)),CoastalHuntLayout.nextY(AbelCoastGeometry.index(id)),40);
    if("MAP_POTE_02".equals(id))return new PoteCampaignMapDef(id,2,"포테의 숲 B · 무리 숲",64,4352,64,2176,320,1984,128,2048,4224,160,40);
    if("MAP_POTE_03".equals(id))return new PoteCampaignMapDef(id,3,"포테의 숲 C · 깊은 숲",64,4864,64,2432,320,2240,128,2304,4736,144,40);
    if(CampaignWorld.BOSS_D.equals(id))return new PoteCampaignMapDef(id,4,"포테의 숲 D · 결계 너머",64,3072,64,1536,256,768,128,768,2944,768,44);
    if("MAP_POTE_04".equals(id))return new PoteCampaignMapDef(id,3,"포테 숲 구역 4 · 이전 경로",PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y,PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y,3968,1888,PoteFieldDef.EXIT_RADIUS);
    if(CampaignWorld.PIET.equals(id))return new PoteCampaignMapDef(id,0,"피에트 조사 거점",PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y,PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y,3968,1888,PoteFieldDef.EXIT_RADIUS);
    return new PoteCampaignMapDef("MAP_POTE_01",1,"포테의 숲 A · 입구 숲",PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y,PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y,3968,1888,PoteFieldDef.EXIT_RADIUS);
  }

  public List<RectF> obstacles(){if(obstacles==null){if(CountessCastleGeometry.contains(id))obstacles=CountessCastleGeometry.obstacles(id);else if(DeathVillageGeometry.contains(id))obstacles=DeathVillageGeometry.obstacles(id);else if(MineHuntGeometry.contains(id))obstacles=MineHuntGeometry.obstacles();else if(MineTownGeometry.contains(id))obstacles=MineTownGeometry.obstacles();else if(HorrorCastleGeometry.contains(id))obstacles=HorrorCastleGeometry.obstacles(id);else if(RucesionCoastGeometry.contains(id))obstacles=RucesionCoastGeometry.obstacles(id);else if(RucesionTownGeometry.contains(id))obstacles=RucesionTownGeometry.obstacles(id);else if(AbelTownGeometry.contains(id))obstacles=AbelTownGeometry.obstacles(id);else if(AbelCoastGeometry.contains(id))obstacles=AbelCoastGeometry.obstacles(id);else if(zone==1||zone==0||"MAP_POTE_04".equals(id))obstacles=PoteFieldDef.obstacles();else obstacles=buildObstacles();}return obstacles;}
  public List<WorldMoveTargetController.TileCenter> navigationTiles(){
    if(navigation!=null)return navigation;if(CountessCastleGeometry.contains(id))return navigation=CountessCastleGeometry.tiles(id,true);if(DeathVillageGeometry.contains(id))return navigation=DeathVillageGeometry.tiles(id,true);if(MineHuntGeometry.contains(id))return navigation=MineHuntGeometry.tiles(true);if(MineTownGeometry.contains(id)){navigation=MineTownGeometry.navigation();return navigation;}if(HorrorCastleGeometry.contains(id)){navigation=HorrorCastleGeometry.tiles(id,true);return navigation;}
    if(RucesionTownGeometry.contains(id)){navigation=RucesionTownGeometry.navigation(id);return navigation;}
    if(AbelTownGeometry.contains(id)){navigation=AbelTownGeometry.navigation(id);return navigation;}
    if(zone==1||zone==0||"MAP_POTE_04".equals(id)){navigation=PoteFieldDef.navigationTiles();return navigation;}
    List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();
    for(float y=minY;y<=maxY;y+=16f)for(float x=minX;x<=maxX;x+=32f){int parity=(int)(x/32f+y/16f);if((parity&1)==0&&!blocked(x,y))out.add(new WorldMoveTargetController.TileCenter(x,y));}
    navigation=Collections.unmodifiableList(out);return navigation;
  }
  public List<WorldMoveTargetController.TileCenter> groundTiles(){
    if(ground!=null)return ground;if(CountessCastleGeometry.contains(id))return ground=CountessCastleGeometry.tiles(id,false);if(DeathVillageGeometry.contains(id))return ground=DeathVillageGeometry.tiles(id,false);if(MineHuntGeometry.contains(id))return ground=MineHuntGeometry.tiles(false);if(HorrorCastleGeometry.contains(id)){ground=HorrorCastleGeometry.tiles(id,false);return ground;}if(zone==1||zone==0||"MAP_POTE_04".equals(id)){ground=PoteFieldDef.groundTiles();return ground;}
    List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();int row=0;
    for(float y=minY;y<=maxY;y+=16f,row++){float first=minX+((row&1)==0?0f:32f);for(float x=first;x<=maxX;x+=64f)out.add(new WorldMoveTargetController.TileCenter(x,y));}
    ground=Collections.unmodifiableList(out);return ground;
  }
  public WorldMoveTargetController.TileCenter nearest(float x,float y){WorldMoveTargetController.TileCenter exact=exactCenter(x,y);if(exact!=null)return exact;WorldMoveTargetController.TileCenter best=null;float bd=Float.MAX_VALUE;for(WorldMoveTargetController.TileCenter t:navigationTiles()){float dx=t.x-x,dy=t.y-y,d=dx*dx+dy*dy;if(d<bd){bd=d;best=t;}}return best;}
  public float[][] trail(){return PoteForestGeometry.trailCenterline(id);}
  public float[][] creek(){return PoteForestGeometry.creekCenterline(id);}
  public float bridgeX(){return PoteForestGeometry.bridgeX(id);}
  public float bridgeY(){return PoteForestGeometry.bridgeY(id);}
  public boolean atExit(float x,float y){float dx=x-backX,dy=y-backY;return dx*dx+dy*dy<=exitRadius*exitRadius;}

  private List<RectF> buildObstacles(){
    List<RectF> out=new ArrayList<>(PoteFieldRenderer.blockingFootprints(id));
    out.addAll(PoteForestGeometry.waterObstacles(id));
    return Collections.unmodifiableList(out);
  }
  private boolean blocked(float x,float y){float r=14;if(x-r<minX||x+r>maxX||y-r<minY||y+r>maxY)return true;return terrainBlocked(x,y,r);}
}

