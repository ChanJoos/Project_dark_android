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

  private PoteCampaignMapDef(String id,int zone,String title,float minX,float maxX,float minY,float maxY,
      float entryX,float entryY,float backX,float backY,float nextX,float nextY,float exitRadius){
    this.id=id;this.zone=zone;this.title=title;this.minX=minX;this.maxX=maxX;this.minY=minY;this.maxY=maxY;
    this.entryX=entryX;this.entryY=entryY;this.backX=backX;this.backY=backY;this.nextX=nextX;this.nextY=nextY;this.exitRadius=exitRadius;
  }

  public static PoteCampaignMapDef forId(String id){String key=CampaignWorld.PIET.equals(id)||CampaignWorld.BOSS_D.equals(id)||"MAP_POTE_04".equals(id)?id:id!=null&&id.startsWith("MAP_POTE_")&&id.length()==11?id:"MAP_POTE_01";synchronized(CACHE){PoteCampaignMapDef d=CACHE.get(key);if(d==null){d=create(key);CACHE.put(key,d);}return d;}}
  private static PoteCampaignMapDef create(String id){
    if("MAP_POTE_02".equals(id))return new PoteCampaignMapDef(id,2,"포테의 숲 B · 무리 숲",64,2304,64,1216,320,928,128,1088,2208,160,40);
    if("MAP_POTE_03".equals(id))return new PoteCampaignMapDef(id,3,"포테의 숲 C · 깊은 숲",64,2816,64,1472,320,1312,128,1344,2704,144,40);
    if(CampaignWorld.BOSS_D.equals(id))return new PoteCampaignMapDef(id,4,"포테의 숲 D · 결계 너머",64,1664,64,960,256,512,128,512,1536,512,44);
    if("MAP_POTE_04".equals(id))return new PoteCampaignMapDef(id,3,"포테 숲 구역 4 · 이전 경로",PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y,PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y,1056,240,PoteFieldDef.EXIT_RADIUS);
    if(CampaignWorld.PIET.equals(id))return new PoteCampaignMapDef(id,0,"피에트 조사 거점",PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y,PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y,1056,240,PoteFieldDef.EXIT_RADIUS);
    return new PoteCampaignMapDef("MAP_POTE_01",1,"포테의 숲 A · 입구 숲",PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y,PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y,1056,240,PoteFieldDef.EXIT_RADIUS);
  }

  public List<RectF> obstacles(){if(obstacles==null){if(zone==1||zone==0||"MAP_POTE_04".equals(id))obstacles=PoteFieldDef.obstacles();else obstacles=buildObstacles();}return obstacles;}
  public List<WorldMoveTargetController.TileCenter> navigationTiles(){
    if(navigation!=null)return navigation;
    if(zone==1||zone==0||"MAP_POTE_04".equals(id)){navigation=PoteFieldDef.navigationTiles();return navigation;}
    List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();
    for(float y=minY;y<=maxY;y+=16f)for(float x=minX;x<=maxX;x+=32f){int parity=(int)(x/32f+y/16f);if((parity&1)==0&&!blocked(x,y))out.add(new WorldMoveTargetController.TileCenter(x,y));}
    navigation=Collections.unmodifiableList(out);return navigation;
  }
  public List<WorldMoveTargetController.TileCenter> groundTiles(){
    if(ground!=null)return ground;if(zone==1||zone==0||"MAP_POTE_04".equals(id)){ground=PoteFieldDef.groundTiles();return ground;}
    List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();int row=0;
    for(float y=minY;y<=maxY;y+=16f,row++){float first=minX+((row&1)==0?0f:32f);for(float x=first;x<=maxX;x+=64f)out.add(new WorldMoveTargetController.TileCenter(x,y));}
    ground=Collections.unmodifiableList(out);return ground;
  }
  public WorldMoveTargetController.TileCenter nearest(float x,float y){WorldMoveTargetController.TileCenter best=null;float bd=Float.MAX_VALUE;for(WorldMoveTargetController.TileCenter t:navigationTiles()){float dx=t.x-x,dy=t.y-y,d=dx*dx+dy*dy;if(d<bd){bd=d;best=t;}}return best;}
  public float[][] trail(){return PoteForestGeometry.trailCenterline(id);}
  public float[][] creek(){return PoteForestGeometry.creekCenterline(id);}
  public float bridgeX(){return PoteForestGeometry.bridgeX(id);}
  public float bridgeY(){return PoteForestGeometry.bridgeY(id);}
  public boolean atExit(float x,float y){float dx=x-backX,dy=y-backY;return dx*dx+dy*dy<=exitRadius*exitRadius;}

  private List<RectF> buildObstacles(){
    List<RectF> out=new ArrayList<>(PoteFieldRenderer.blockingFootprints(id));float[][] water=creek();float bx=bridgeX(),by=bridgeY();
    for(int i=1;i<water.length;i++){float x0=water[i-1][0],y0=water[i-1][1],x1=water[i][0],y1=water[i][1],len=(float)Math.hypot(x1-x0,y1-y0);int n=Math.max(1,(int)Math.ceil(len/18f));for(int j=0;j<=n;j++){float t=j/(float)n,x=x0+(x1-x0)*t,y=y0+(y1-y0)*t;if(Math.hypot(x-bx,y-by)<=47f)continue;out.add(new RectF(x-19,y-19,x+19,y+19));}}
    return Collections.unmodifiableList(out);
  }
  private boolean blocked(float x,float y){float r=14;if(x-r<minX||x+r>maxX||y-r<minY||y+r>maxY)return true;for(RectF o:obstacles())if(x+r>o.left&&x-r<o.right&&y+r>o.top&&y-r<o.bottom)return true;return false;}
}
