package com.projectdark.mobile;

import android.graphics.RectF;
import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

/**
 * PROJECT DARK prototype runtime state.
 * [B] Coordinates/collision/combat values are reconstruction fixtures.
 * [ADAPTED] Screen-space geometry validates mobile controls before verified tile collision exists.
 */
public final class RuntimeState {
  public enum BootMode { MILLES, POTE_01_PROTOTYPE }

  public static final float WORLD_MIN_X=WorldDef.MIN_X,WORLD_MAX_X=WorldDef.MAX_X,WORLD_MIN_Y=WorldDef.MIN_Y,WORLD_MAX_Y=WorldDef.MAX_Y;
  public static final float PLAYER_RADIUS=10f,MONSTER_RADIUS=12f,NPC_RADIUS=10f;
  private static final float ACTOR_CLEARANCE=5f;

  public static final class Player {
    public float spawnX,spawnY;
    public float x,y;
    public int hp=100,maxHp=100,mp=90,maxMp=100;
    public boolean alive=true;
    public float hitFlash=0f;
    Player(float spawnX,float spawnY){this.spawnX=spawnX;this.spawnY=spawnY;this.x=spawnX;this.y=spawnY;}
  }

  public static final class Npc {
    public final String id,name,dialogue,assetStatus;public final float x,y;
    Npc(String id,String name,float x,float y,String dialogue,String assetStatus){this.id=id;this.name="town_keeper".equals(id)||id.startsWith("campaign_altar_")?name:NpcIdentity.forId(id).label();this.x=x;this.y=y;this.dialogue=NpcIdentity.text(dialogue);this.assetStatus=assetStatus;}
  }

  public static final class Monster {
    public enum State { SPAWN,IDLE,WANDER,DETECT,CHASE,ATTACK,DEAD,RESPAWN }
    public final String id,name,assetStatus;public final float spawnX,spawnY;public float x,y;
    /** Selected only by the mobile 2-circle field adapter; null keeps unknown prototypes fail-closed. */
    public String campaignRewardProfileId;
    public float moveStartX,moveStartY,moveTargetX,moveTargetY,moveElapsed,moveDuration;
    public boolean isMoving;
    public int hp;public final int maxHp;public boolean alive=true;
    public State state=State.SPAWN;
    public float attackCooldown=0f,attackWindup=0f,attackVisualRemaining=0f,respawnClock=0f,hitFlash=0f,damagePopupClock=0f;
    public boolean attackPrimed=false;
    public final CanonicalActorFacing visualFacing=new CanonicalActorFacing(CharacterRenderer.Direction.SE);
    public int lastDamage=0;
    public int detourSign=1;
    public float detourClock=0f;
    /** Per-actor animation phase used to animate single-pose candidate art. */
    public float animationClock=0f;
    public float respawnSeconds=4f;
    Monster(String id,String name,float x,float y,int hp,String assetStatus){this.id=id;this.name=name;this.assetStatus=assetStatus;spawnX=x;spawnY=y;this.x=x;this.y=y;this.hp=hp;maxHp=hp;state=State.IDLE;}
  }

  private final BootMode bootMode;
  private String currentMapId;
  private float currentMinX=WorldDef.MIN_X,currentMaxX=WorldDef.MAX_X,currentMinY=WorldDef.MIN_Y,currentMaxY=WorldDef.MAX_Y;
  private final WorldDef world=new WorldDef();
  private final Player player;
  private final List<RectF> obstacles=new ArrayList<>();
  private com.projectdark.mobile.world.TerrainObstacleIndex terrainIndex;
  private final List<Npc> npcs=new ArrayList<>();
  private final List<Monster> monsters=new ArrayList<>();
  private final List<Monster> suspendedMillesMonsters=new ArrayList<>();
  private final List<Npc> suspendedMillesNpcs=new ArrayList<>();
  private final List<RectF> suspendedMillesObstacles=new ArrayList<>();
  private final CombatLedger ledger=new CombatLedger();
  private final SkillEffectState skillEffects=new SkillEffectState();
  public SkillEffectState skillEffects(){return skillEffects;}
  private final RuntimeMetrics metrics=new RuntimeMetrics();
  private final RpgProgressionState rpg=new RpgProgressionState();

  public RuntimeState(){this(BootMode.MILLES,false);}
  public RuntimeState(BootMode bootMode){this(bootMode,false);}

  RuntimeState(BootMode bootMode,boolean skipPoteRuntimeE2EAudit){
    this.bootMode=bootMode==null?BootMode.MILLES:bootMode;
    currentMapId=this.bootMode==BootMode.POTE_01_PROTOTYPE?PotePrototypeWorldDef.MAP_ID:WorldDef.ID;

    // IMPORTANT: regression/content audits are CI/development checks, not runtime startup gates.
    // A stale audit must never make a build that otherwise renders and plays crash before GameView appears.
    // Keep the audit classes independently runnable from CI/tools, but construct the playable runtime fail-safe.

    if(this.bootMode==BootMode.POTE_01_PROTOTYPE){
      player=new Player(PotePrototypeWorldDef.PLAYER_X_B,PotePrototypeWorldDef.PLAYER_Y_B);
      monsters.addAll(PoteForestMonsterShowcase.instantiate(
          new PoteMonsterRoster(),com.projectdark.mobile.world.PoteFieldDef.navigationTiles()));
    }else{
      player=new Player(WorldDef.PLAYER_SPAWN_X,WorldDef.PLAYER_SPAWN_Y);
      for(RectF r:world.blockers())obstacles.add(new RectF(r));
      for(WorldDef.NpcSpawn n:world.npcSpawns())npcs.add(new Npc(n.id,n.name,n.x,n.y,n.dialogue,n.assetStatus));
      for(WorldDef.MonsterSpawn m:world.monsterSpawns()){
        com.projectdark.mobile.world.WorldMoveTargetController.TileCenter center=MonsterTileCenterLocomotion.nearestAuthoredCenter(m.x,m.y);
        monsters.add(new Monster(m.id,m.name,center==null?m.x:center.x,center==null?m.y:center.y,m.hp,m.assetStatus));
      }
    }
  }

  public BootMode bootMode(){return bootMode;}
  public String currentMapId(){return currentMapId;}
  public com.projectdark.mobile.world.WorldMoveTargetController.TileCenter nearestMonsterTileCenter(float x,float y){
    com.projectdark.mobile.world.TownInteriorDef d=com.projectdark.mobile.world.TownInteriorDef.forMap(currentMapId);
    if(d!=null){com.projectdark.mobile.world.WorldMoveTargetController.TileCenter best=null;float score=Float.MAX_VALUE;for(com.projectdark.mobile.world.WorldMoveTargetController.TileCenter t:d.navigationTiles()){float distance=distanceSquared(x,y,t.x,t.y);if(distance<score){best=t;score=distance;}}return best;}
    if(com.projectdark.mobile.world.CampaignWorld.contains(currentMapId))
      return com.projectdark.mobile.world.PoteCampaignMapDef.forId(currentMapId).nearest(x,y);
    return MonsterTileCenterLocomotion.nearestAuthoredCenter(x,y);
  }
  public boolean isMonsterTileCenter(float x,float y){
    com.projectdark.mobile.world.TownInteriorDef d=com.projectdark.mobile.world.TownInteriorDef.forMap(currentMapId);
    if(d!=null){for(com.projectdark.mobile.world.WorldMoveTargetController.TileCenter t:d.navigationTiles())if(Math.abs(t.x-x)<.01f&&Math.abs(t.y-y)<.01f)return true;return false;}
    if(com.projectdark.mobile.world.CampaignWorld.contains(currentMapId))
      return com.projectdark.mobile.world.PoteCampaignMapDef.forId(currentMapId).isNavigationCenter(x,y);
    return MonsterTileCenterLocomotion.isAuthoredCenter(x,y);
  }
  public List<com.projectdark.mobile.world.WorldMoveTargetController.TileCenter> monsterNavigationTiles(){
    com.projectdark.mobile.world.TownInteriorDef d=com.projectdark.mobile.world.TownInteriorDef.forMap(currentMapId);if(d!=null)return d.navigationTiles();
    if(com.projectdark.mobile.world.CampaignWorld.contains(currentMapId))
      return com.projectdark.mobile.world.PoteCampaignMapDef.forId(currentMapId).navigationTiles();
    return MonsterTileCenterLocomotion.authoredCenters();
  }
  private String chaseMapId;
  private MonsterChasePathfinder.IndexedMap chaseMap;
  public WorldMoveTargetController.Direction nextMonsterChaseStep(Monster monster,float targetX,float targetY){
    if(monster==null||!monster.alive)return null;
    if(chaseMap==null||!currentMapId.equals(chaseMapId)){chaseMap=new MonsterChasePathfinder.IndexedMap(monsterNavigationTiles());chaseMapId=currentMapId;}
    return chaseMap.nextStep(monster.x,monster.y,targetX,targetY,
        (from,to)->monsterCanTraverse(monster,from.x,from.y,to.x,to.y));
  }
  private boolean monsterCanTraverse(Monster self,float fromX,float fromY,float toX,float toY){
    int samples=Math.max(2,(int)Math.ceil(distance(fromX,fromY,toX,toY)/4f));
    for(int i=1;i<=samples;i++){
      float t=i/(float)samples,x=fromX+(toX-fromX)*t,y=fromY+(toY-fromY)*t;
      if(x<currentMinX||x>currentMaxX||y<currentMinY||y>currentMaxY
          ||!monsterCanOccupy(self,x,y))return false;
    }
    return true;
  }
  public WorldDef world(){return world;}
  public Player player(){return player;}
  public CombatLedger ledger(){return ledger;}
  public RuntimeMetrics metrics(){return metrics;}
  public RpgProgressionState rpg(){return rpg;}
  public void applyDerivedGrowth(){int oldMaxHp=player.maxHp,oldMaxMp=player.maxMp;int newMaxHp=rpg.maxHpGrowth(),newMaxMp=rpg.maxMpGrowth();player.maxHp=newMaxHp;player.maxMp=newMaxMp;if(newMaxHp>oldMaxHp)player.hp=Math.min(newMaxHp,player.hp+(newMaxHp-oldMaxHp));else player.hp=Math.min(player.hp,newMaxHp);if(newMaxMp>oldMaxMp)player.mp=Math.min(newMaxMp,player.mp+(newMaxMp-oldMaxMp));else player.mp=Math.min(player.mp,newMaxMp);}
  @Deprecated public void syncPlayerGrowth(){applyDerivedGrowth();}
  public List<RectF> obstacles(){return Collections.unmodifiableList(obstacles);}
  public List<Npc> npcs(){return Collections.unmodifiableList(npcs);}
  public List<Monster> monsters(){return Collections.unmodifiableList(monsters);}
  public String campaignRewardProfileFor(String monsterId){for(Monster m:monsters)if(monsterId!=null&&monsterId.equals(m.id))return m.campaignRewardProfileId;return null;}

  /** Spawn the explicitly adapted opening-quest mouse only after the player accepts that story. */
  public Monster ensureAdaptedMillesMouse(){
    if(!"milles_interior_inn".equals(currentMapId))return null;
    for(Monster m:monsters)if("milles_mouse_proto".equals(m.id))return m;
    float x=com.projectdark.mobile.world.TownInteriorDef.x(10,11),y=com.projectdark.mobile.world.TownInteriorDef.y(10,11);
    com.projectdark.mobile.world.WorldMoveTargetController.TileCenter center=nearestMonsterTileCenter(x,y);
    Monster mouse=new Monster("milles_mouse_proto","생쥐",center==null?x:center.x,center==null?y:center.y,24,WorldDef.ASSET_STATUS);
    mouse.campaignRewardProfileId=mouse.id;
    monsters.add(mouse);return mouse;
  }

  /** Live map transition keeps RPG/combat ledger identity while replacing map-local actors/collision. */
  public void enterTownInterior(com.projectdark.mobile.world.TownInteriorDef d){
    if(d==null)throw new IllegalArgumentException("interior");
    if(WorldDef.ID.equals(currentMapId)){suspendedMillesMonsters.clear();suspendedMillesMonsters.addAll(monsters);suspendedMillesNpcs.clear();suspendedMillesNpcs.addAll(npcs);suspendedMillesObstacles.clear();suspendedMillesObstacles.addAll(obstacles);}
    currentMapId=d.mapId;monsters.clear();npcs.clear();obstacles.clear();terrainIndex=null;obstacles.addAll(d.obstacles());
    npcs.add(new Npc("town_keeper",d.npcName,d.npcX(),d.npcY(),"",WorldDef.ASSET_STATUS));
    currentMinX=d.MIN_X;currentMaxX=d.MAX_X;currentMinY=d.MIN_Y;currentMaxY=d.MAX_Y;
    player.spawnX=d.spawnX();player.spawnY=d.spawnY();player.x=player.spawnX;player.y=player.spawnY;
  }
  public void leaveTownInterior(float x,float y){monsters.clear();monsters.addAll(suspendedMillesMonsters);npcs.clear();npcs.addAll(suspendedMillesNpcs);obstacles.clear();terrainIndex=null;obstacles.addAll(suspendedMillesObstacles);currentMapId=WorldDef.ID;currentMinX=WorldDef.MIN_X;currentMaxX=WorldDef.MAX_X;currentMinY=WorldDef.MIN_Y;currentMaxY=WorldDef.MAX_Y;player.spawnX=WorldDef.PLAYER_SPAWN_X;player.spawnY=WorldDef.PLAYER_SPAWN_Y;player.x=x;player.y=y;}
  public void enterPoteField(){
    currentMapId=PotePrototypeWorldDef.MAP_ID;currentMinX=com.projectdark.mobile.world.PoteFieldDef.MIN_X;currentMaxX=com.projectdark.mobile.world.PoteFieldDef.MAX_X;currentMinY=com.projectdark.mobile.world.PoteFieldDef.MIN_Y;currentMaxY=com.projectdark.mobile.world.PoteFieldDef.MAX_Y;
    obstacles.clear();terrainIndex=null;for(RectF r:com.projectdark.mobile.world.PoteFieldDef.obstacles())obstacles.add(new RectF(r));
    npcs.clear();npcs.add(new Npc("pote_trail_guide","숲길 안내인",736f,496f,
        "북동쪽 흙길을 따라가면 숲 안쪽 공터와 물가로 이어집니다.","PENDING_CROP/pote/npc/trail_guide"));
    monsters.clear();monsters.addAll(PoteForestMonsterShowcase.instantiate(
        new PoteMonsterRoster(),com.projectdark.mobile.world.PoteFieldDef.navigationTiles()));
    AdaptedCampaignRewardCatalog campaignRewards=new AdaptedCampaignRewardCatalog();
    for(Monster monster:monsters)if(campaignRewards.find(monster.id)!=null)monster.campaignRewardProfileId=monster.id;
    player.spawnX=com.projectdark.mobile.world.PoteFieldDef.ENTRY_X;player.spawnY=com.projectdark.mobile.world.PoteFieldDef.ENTRY_Y;player.x=player.spawnX;player.y=player.spawnY;
  }
  public void enterCampaignMap(String id,boolean fromNext){
    if(!com.projectdark.mobile.world.CampaignWorld.contains(id))throw new IllegalArgumentException("campaign map");
    com.projectdark.mobile.world.PoteCampaignMapDef map=com.projectdark.mobile.world.PoteCampaignMapDef.forId(id);currentMapId=id;currentMinX=map.minX;currentMaxX=map.maxX;currentMinY=map.minY;currentMaxY=map.maxY;obstacles.clear();terrainIndex=null;for(RectF r:map.obstacles())obstacles.add(new RectF(r));npcs.clear();monsters.clear();
    int zone=map.zone;monsters.addAll(CampaignMonsters.spawnForMap(id));
    if(zone==0){npcs.add(new Npc("piet_investigator","이선",768,512,"피에트 조사와 서신을 담당합니다.","ADAPTED"));npcs.add(new Npc("piet_supplier","조슈아",864,560,"조사 장비와 보급품을 준비하세요.","ADAPTED"));npcs.add(new Npc("piet_purifier","새뮤얼",672,560,"숲의 정수를 정화합니다.","ADAPTED"));for(String job:CampaignProgress.JOBS)npcs.add(new Npc(CampaignProgress.mentor(job),"지도자",608+Arrays.asList(CampaignProgress.JOBS).indexOf(job)*64,656,"숙련 기술과 장비를 지도합니다.","ADAPTED"));}
    else {com.projectdark.mobile.world.WorldMoveTargetController.TileCenter guide=map.nearest(map.entryX+96,map.entryY+16);npcs.add(new Npc("pote_trail_guide","알렉산더",guide.x,guide.y,"숲길 안내와 조사 의뢰", "ADAPTED"));if(zone==3&&!"MAP_POTE_04".equals(id)){float[][] sites={{720,560},{1536,760},{2300,1040}};for(int i=0;i<3;i++){com.projectdark.mobile.world.WorldMoveTargetController.TileCenter altar=com.projectdark.mobile.world.CampaignWorld.nearestConnectedTile(id,sites[i][0],sites[i][1]);npcs.add(new Npc("campaign_altar_"+i,"정화 제단 "+(i+1),altar.x,altar.y,"제단 정화", "ADAPTED"));}}}
    com.projectdark.mobile.world.WorldMoveTargetController.TileCenter arrival=com.projectdark.mobile.world.CampaignWorld.arrival(id,fromNext);player.spawnX=arrival.x;player.spawnY=arrival.y;player.x=arrival.x;player.y=arrival.y;rpg.campaign().visit(id);
  }
  public void enterMillesFromField(float x,float y){
    currentMapId=WorldDef.ID;currentMinX=WorldDef.MIN_X;currentMaxX=WorldDef.MAX_X;currentMinY=WorldDef.MIN_Y;currentMaxY=WorldDef.MAX_Y;
    obstacles.clear();terrainIndex=null;for(RectF r:world.blockers())obstacles.add(new RectF(r));npcs.clear();monsters.clear();
    for(WorldDef.NpcSpawn n:world.npcSpawns())npcs.add(new Npc(n.id,n.name,n.x,n.y,n.dialogue,n.assetStatus));
    for(WorldDef.MonsterSpawn m:world.monsterSpawns()){com.projectdark.mobile.world.WorldMoveTargetController.TileCenter center=MonsterTileCenterLocomotion.nearestAuthoredCenter(m.x,m.y);monsters.add(new Monster(m.id,m.name,center==null?m.x:center.x,center==null?m.y:center.y,m.hp,m.assetStatus));}
    player.spawnX=WorldDef.PLAYER_SPAWN_X;player.spawnY=WorldDef.PLAYER_SPAWN_Y;player.x=x;player.y=y;
  }

  public boolean tryMove(float dx,float dy){if(!player.alive||skillEffects.rooted("player"))return false;float bx=player.x,by=player.y;float nx=clamp(player.x+dx,currentMinX,currentMaxX),ny=clamp(player.y+dy,currentMinY,currentMaxY);if(playerCanOccupy(nx,player.y))player.x=nx;if(playerCanOccupy(player.x,ny))player.y=ny;return player.x!=bx||player.y!=by;}

  public boolean tryMoveMonster(Monster m,float desiredDx,float desiredDy,float distance){
    if(m==null||!m.alive)return false;
    MonsterDiagonalLocomotion.Step applied=MonsterDiagonalLocomotion.select(
        m.x,m.y,desiredDx,desiredDy,distance,m.visualFacing.locomotion(),m.detourSign,
        new MonsterDiagonalLocomotion.Occupancy(){
          public boolean canOccupy(float x,float y){
            return x>=currentMinX&&x<=currentMaxX&&y>=currentMinY&&y<=currentMaxY
                &&monsterCanOccupy(m,x,y);
          }
        });
    if(applied==null){
      m.detourClock+=.05f;
      if(m.detourClock>.75f){m.detourSign=-m.detourSign;m.detourClock=0f;}
      return false;
    }
    if(m.state!=Monster.State.ATTACK)m.state=Monster.State.CHASE;
    m.moveStartX=m.x;m.moveStartY=m.y;m.moveTargetX=m.x+applied.dx;m.moveTargetY=m.y+applied.dy;m.moveElapsed=0f;m.moveDuration=com.projectdark.mobile.world.WorldMoveTargetController.TILE_STEP_SECONDS;m.isMoving=true;m.visualFacing.setLocomotion(applied.facing);
    m.detourClock=Math.max(0f,m.detourClock-.05f);
    return true;
  }
  private boolean playerCanOccupy(float x,float y){return !blocked(x,y,PLAYER_RADIUS)&&!monsterOccupied(null,x,y,PLAYER_RADIUS)&&!npcOccupied(x,y,PLAYER_RADIUS);}
  private boolean monsterCanOccupy(Monster self,float x,float y){float radius=monsterCollisionRadius(self);return !blocked(x,y,radius)&&!playerOccupied(x,y,radius)&&!monsterOccupied(self,x,y,radius)&&!npcOccupied(x,y,radius);}
  public boolean blocked(float x,float y){return blocked(x,y,PLAYER_RADIUS);}
  private boolean blocked(float x,float y,float radius){if(terrainIndex==null)terrainIndex=new com.projectdark.mobile.world.TerrainObstacleIndex(obstacles);return terrainIndex.blocked(x,y,radius);}
  private boolean playerOccupied(float x,float y,float radius){
    if(!player.alive)return false;
    float min=radius+PLAYER_RADIUS+ACTOR_CLEARANCE;
    return distance(x,y,player.x,player.y)<min;
  }
  /** Shared World/Runtime actor rule for a player tile endpoint, including moving reservations. */
  public boolean canPlayerOccupyActors(float x,float y){
    return !monsterOccupied(null,x,y,PLAYER_RADIUS)&&!npcOccupied(x,y,PLAYER_RADIUS);
  }

  /** Revalidate the whole player tile segment against stationary actors and moving monster paths. */
  public boolean canPlayerTraverseActors(float fromX,float fromY,float toX,float toY){
    if(!canPlayerOccupyActors(toX,toY))return false;
    float playerDx=toX-fromX,playerDy=toY-fromY;
    for(Monster other:monsters){
      if(!other.alive)continue;
      float min=PLAYER_RADIUS+monsterCollisionRadius(other)+ACTOR_CLEARANCE;
      float minSquared=min*min;
      if(other.isMoving){
        if(segmentDistanceSquared(fromX,fromY,toX,toY,other.x,other.y,
            other.moveTargetX,other.moveTargetY)<minSquared){
          float rx=fromX-other.x,ry=fromY-other.y;
          float monsterDx=other.moveTargetX-other.x,monsterDy=other.moveTargetY-other.y;
          boolean exitingExistingOverlap=distanceSquared(fromX,fromY,other.x,other.y)<minSquared
              &&rx*(playerDx-monsterDx)+ry*(playerDy-monsterDy)>=0f;
          if(!exitingExistingOverlap)return false;
        }
      }else if(pointSegmentDistanceSquared(other.x,other.y,fromX,fromY,toX,toY)<minSquared){
        float rx=fromX-other.x,ry=fromY-other.y;
        boolean exitingExistingOverlap=distanceSquared(fromX,fromY,other.x,other.y)<minSquared
            &&rx*playerDx+ry*playerDy>=0f;
        if(!exitingExistingOverlap)return false;
      }
    }
    for(Npc npc:npcs){
      float min=PLAYER_RADIUS+NPC_RADIUS+ACTOR_CLEARANCE;
      if(pointSegmentDistanceSquared(npc.x,npc.y,fromX,fromY,toX,toY)<min*min){
        float rx=fromX-npc.x,ry=fromY-npc.y;
        boolean exitingExistingOverlap=distanceSquared(fromX,fromY,npc.x,npc.y)<min*min
            &&rx*playerDx+ry*playerDy>=0f;
        if(!exitingExistingOverlap)return false;
      }
    }
    return true;
  }

  private boolean monsterOccupied(Monster self,float x,float y,float radius){
    for(Monster other:monsters){
      if(other==self||!other.alive)continue;
      float min=radius+monsterCollisionRadius(other)+ACTOR_CLEARANCE;
      float minSquared=min*min;
      if(distanceSquared(x,y,other.x,other.y)<minSquared)return true;
      // An in-flight destination is reserved, so two AI updates cannot choose the same tile.
      if(other.isMoving&&distanceSquared(x,y,other.moveTargetX,other.moveTargetY)<minSquared)return true;
      // Reject crossing trajectories as well as colliding endpoints during simultaneous movement.
      if(self!=null&&other.isMoving&&segmentDistanceSquared(self.x,self.y,x,y,
          other.x,other.y,other.moveTargetX,other.moveTargetY)<minSquared)return true;
    }
    return false;
  }
  private float monsterCollisionRadius(Monster monster){
    return "milles_mouse_proto".equals(monster.id)?7f:"POTE_MANTIS".equals(PoteForestMonsterShowcase.species(monster.id))?18f:"POTE_LYCAN".equals(PoteForestMonsterShowcase.species(monster.id))?16f:MONSTER_RADIUS;
  }
  private boolean npcOccupied(float x,float y,float radius){
    for(Npc n:npcs){float min=radius+NPC_RADIUS+ACTOR_CLEARANCE;if(distance(x,y,n.x,n.y)<min)return true;}
    return false;
  }
  private static float distanceSquared(float ax,float ay,float bx,float by){
    float dx=ax-bx,dy=ay-by;return dx*dx+dy*dy;
  }
  private static float segmentDistanceSquared(float ax,float ay,float bx,float by,
      float cx,float cy,float dx,float dy){
    if(segmentsIntersect(ax,ay,bx,by,cx,cy,dx,dy))return 0f;
    return Math.min(Math.min(pointSegmentDistanceSquared(ax,ay,cx,cy,dx,dy),
        pointSegmentDistanceSquared(bx,by,cx,cy,dx,dy)),
        Math.min(pointSegmentDistanceSquared(cx,cy,ax,ay,bx,by),
            pointSegmentDistanceSquared(dx,dy,ax,ay,bx,by)));
  }
  private static float pointSegmentDistanceSquared(float px,float py,float ax,float ay,float bx,float by){
    float vx=bx-ax,vy=by-ay,length=vx*vx+vy*vy;
    if(length<=.0001f)return distanceSquared(px,py,ax,ay);
    float t=Math.max(0f,Math.min(1f,((px-ax)*vx+(py-ay)*vy)/length));
    return distanceSquared(px,py,ax+t*vx,ay+t*vy);
  }
  private static boolean segmentsIntersect(float ax,float ay,float bx,float by,
      float cx,float cy,float dx,float dy){
    float abC=cross(bx-ax,by-ay,cx-ax,cy-ay);
    float abD=cross(bx-ax,by-ay,dx-ax,dy-ay);
    float cdA=cross(dx-cx,dy-cy,ax-cx,ay-cy);
    float cdB=cross(dx-cx,dy-cy,bx-cx,by-cy);
    if(((abC<0f&&abD>0f)||(abC>0f&&abD<0f))
        &&((cdA<0f&&cdB>0f)||(cdA>0f&&cdB<0f)))return true;
    return (Math.abs(abC)<.001f&&onSegment(ax,ay,bx,by,cx,cy))
        ||(Math.abs(abD)<.001f&&onSegment(ax,ay,bx,by,dx,dy))
        ||(Math.abs(cdA)<.001f&&onSegment(cx,cy,dx,dy,ax,ay))
        ||(Math.abs(cdB)<.001f&&onSegment(cx,cy,dx,dy,bx,by));
  }
  private static boolean onSegment(float ax,float ay,float bx,float by,float px,float py){
    return px>=Math.min(ax,bx)-.001f&&px<=Math.max(ax,bx)+.001f
        &&py>=Math.min(ay,by)-.001f&&py<=Math.max(ay,by)+.001f;
  }
  private static float cross(float ax,float ay,float bx,float by){return ax*by-ay*bx;}

  public Npc hitNpc(float x,float y,float radius){for(Npc n:npcs){float dx=x-n.x,dy=y-n.y;if(dx*dx+dy*dy<=radius*radius)return n;}return null;}
  public Monster hitMonster(float x,float y,float radius){for(Monster m:monsters){if(!m.alive)continue;float dx=x-m.x,dy=y-m.y;if(dx*dx+dy*dy<=radius*radius)return m;if(com.projectdark.mobile.world.CampaignWorld.contains(currentMapId)&&PoteForestMonsterShowcase.containsMonster(m.id)&&Math.abs(dx)<=42f&&dy>=-42f&&dy<=8f)return m;}return null;}
  public float distanceTo(Npc n){return distance(player.x,player.y,n.x,n.y);}
  public float distanceTo(Monster m){return distance(player.x,player.y,m.x,m.y);}

  public void beginMonsterAttack(Monster m){if(m!=null&&m.alive&&!m.attackPrimed&&m.attackCooldown<=0f){m.visualFacing.beginAttack(player.x-m.x,player.y-m.y);m.state=Monster.State.ATTACK;m.attackPrimed=true;m.attackWindup=.24f;m.attackVisualRemaining=.36f;}}
  public boolean monsterAttackReady(Monster m){return m!=null&&m.alive&&m.state==Monster.State.ATTACK&&m.attackPrimed&&m.attackWindup<=0f&&m.attackCooldown<=0f;}
  public void resolveMonsterAttack(Monster m,int damage,float cooldown){if(!monsterAttackReady(m))return;m.attackPrimed=false;m.attackWindup=0f;m.attackCooldown=Math.max(0f,cooldown);damagePlayer(damage);m.visualFacing.endAttack();if(m.alive)m.state=Monster.State.IDLE;}
  public void cancelMonsterAttack(Monster m){if(m!=null){m.attackPrimed=false;m.attackWindup=0f;m.attackVisualRemaining=0f;m.visualFacing.endAttack();if(m.alive)m.state=Monster.State.IDLE;}}

  public void damage(Monster m,int amount){if(m==null||!m.alive||amount<=0)return;m.hp=Math.max(0,m.hp-amount);m.hitFlash=.14f;m.damagePopupClock=.65f;m.lastDamage=amount;ledger.add(CombatLedger.Type.MONSTER_HIT,"player",m.id,amount);if(m.hp==0){m.alive=false;m.state=Monster.State.DEAD;m.attackCooldown=0f;m.attackWindup=0f;m.attackPrimed=false;m.visualFacing.endAttack();m.detourClock=0f;m.respawnClock=m.respawnSeconds;ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player",m.id,0);}}
  public void damagePlayer(int amount){if(amount<=0||!player.alive)return;skillEffects.stopRest();player.hp=Math.max(0,player.hp-amount);player.hitFlash=.18f;ledger.add(CombatLedger.Type.PLAYER_HIT,"monster","player",amount);if(player.hp==0){player.alive=false;ledger.add(CombatLedger.Type.PLAYER_DEFEATED,"monster","player",0);for(Monster m:monsters)if(m.alive){cancelMonsterAttack(m);m.state=Monster.State.IDLE;}}}
  public void revivePlayer(){skillEffects.clear("player");player.x=player.spawnX;player.y=player.spawnY;player.hp=player.maxHp;player.mp=player.maxMp;player.alive=true;player.hitFlash=0f;ledger.add(CombatLedger.Type.PLAYER_REVIVED,"runtime","player",0);}

  public void tick(float dt){skillEffects.tick(this,Math.max(0f,dt));player.hitFlash=Math.max(0f,player.hitFlash-dt);for(Monster m:monsters){tickMonsterMovement(m,dt);m.attackCooldown=Math.max(0f,m.attackCooldown-dt);m.hitFlash=Math.max(0,m.hitFlash-dt);m.damagePopupClock=Math.max(0,m.damagePopupClock-dt);m.attackWindup=Math.max(0,m.attackWindup-dt);float priorAttackVisual=m.attackVisualRemaining;m.attackVisualRemaining=Math.max(0f,m.attackVisualRemaining-dt);if(priorAttackVisual>0f&&m.attackVisualRemaining<=0f&&!m.attackPrimed)m.visualFacing.endAttack();m.detourClock=Math.max(0f,m.detourClock-dt*.25f);if(m.alive)m.animationClock+=Math.max(0f,dt);if(m.alive)continue;if(m.state==Monster.State.DEAD)m.state=Monster.State.RESPAWN;m.respawnClock=Math.max(0f,m.respawnClock-dt);if(m.respawnClock<=0f){m.state=Monster.State.SPAWN;m.x=m.spawnX;m.y=m.spawnY;m.hp=m.maxHp;m.attackCooldown=0f;m.attackWindup=0f;m.attackVisualRemaining=0f;m.attackPrimed=false;m.detourClock=0f;m.detourSign=1;m.alive=true;m.lastDamage=0;m.state=Monster.State.IDLE;ledger.add(CombatLedger.Type.MONSTER_RESPAWNED,"runtime",m.id,0);}}List<CombatLedger.Event> events=ledger.snapshot();metrics.consume(events);rpg.consumeCombat(events,this);}

  private static void tickMonsterMovement(Monster m,float dt){if(!m.isMoving)return;m.moveElapsed=Math.min(m.moveDuration,m.moveElapsed+Math.max(0f,dt));float t=m.moveDuration<=0f?1f:m.moveElapsed/m.moveDuration;float eased=t*t*(3f-2f*t);m.x=m.moveStartX+(m.moveTargetX-m.moveStartX)*eased;m.y=m.moveStartY+(m.moveTargetY-m.moveStartY)*eased;if(t>=1f){m.x=m.moveTargetX;m.y=m.moveTargetY;m.isMoving=false;}}
  private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);}
  private static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}
}
