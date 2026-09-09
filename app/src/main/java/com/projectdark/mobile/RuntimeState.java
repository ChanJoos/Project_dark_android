package com.projectdark.mobile;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PROJECT DARK prototype runtime state.
 * [B] Coordinates/collision/combat values are reconstruction fixtures.
 * [ADAPTED] Screen-space geometry validates mobile controls before verified tile collision exists.
 */
public final class RuntimeState {
  public static final float WORLD_MIN_X=180f,WORLD_MAX_X=760f,WORLD_MIN_Y=120f,WORLD_MAX_Y=455f;
  public static final float PLAYER_RADIUS=9f,MONSTER_RADIUS=11f;

  public static final class Player {
    public final float spawnX=480f,spawnY=300f;
    public float x=spawnX,y=spawnY;
    public int hp=100,maxHp=100,mp=90,maxMp=100;
    public boolean alive=true;
  }

  public static final class Npc {
    public final String id,name,dialogue;public final float x,y;
    Npc(String id,String name,float x,float y,String dialogue){this.id=id;this.name=name;this.x=x;this.y=y;this.dialogue=dialogue;}
  }

  public static final class Monster {
    public final String id,name;public final float spawnX,spawnY;public float x,y;
    public int hp;public final int maxHp;public boolean alive=true;
    public float attackCooldown=0f,respawnClock=0f,hitFlash=0f,damagePopupClock=0f;
    public int lastDamage=0;
    Monster(String id,String name,float x,float y,int hp){this.id=id;this.name=name;spawnX=x;spawnY=y;this.x=x;this.y=y;this.hp=hp;maxHp=hp;}
  }

  private final Player player=new Player();
  private final List<RectF> obstacles=new ArrayList<>();
  private final List<Npc> npcs=new ArrayList<>();
  private final List<Monster> monsters=new ArrayList<>();

  public RuntimeState(){
    obstacles.add(new RectF(272f,170f,342f,238f));obstacles.add(new RectF(600f,150f,684f,218f));
    obstacles.add(new RectF(312f,350f,374f,430f));obstacles.add(new RectF(620f,332f,700f,420f));
    npcs.add(new Npc("milles_guide_proto","밀레스 안내인 [B]",555f,248f,"밀레스에 온 것을 환영합니다. 이 대화는 모바일 접근/대화 루프 검증용 프로토타입입니다."));
    monsters.add(new Monster("combat_dummy_01","훈련용 몬스터 [B]",430f,205f,60));
  }

  public Player player(){return player;}
  public List<RectF> obstacles(){return Collections.unmodifiableList(obstacles);}
  public List<Npc> npcs(){return Collections.unmodifiableList(npcs);}
  public List<Monster> monsters(){return Collections.unmodifiableList(monsters);}

  public boolean tryMove(float dx,float dy){
    if(!player.alive)return false;float bx=player.x,by=player.y;
    float nx=clamp(player.x+dx,WORLD_MIN_X,WORLD_MAX_X),ny=clamp(player.y+dy,WORLD_MIN_Y,WORLD_MAX_Y);
    if(!blocked(nx,player.y,PLAYER_RADIUS)&&!monsterOccupied(nx,player.y,PLAYER_RADIUS))player.x=nx;
    if(!blocked(player.x,ny,PLAYER_RADIUS)&&!monsterOccupied(player.x,ny,PLAYER_RADIUS))player.y=ny;
    return player.x!=bx||player.y!=by;
  }

  public boolean tryMoveMonster(Monster m,float dx,float dy){
    if(m==null||!m.alive)return false;float bx=m.x,by=m.y;
    float nx=clamp(m.x+dx,WORLD_MIN_X,WORLD_MAX_X),ny=clamp(m.y+dy,WORLD_MIN_Y,WORLD_MAX_Y);
    if(!blocked(nx,m.y,MONSTER_RADIUS)&&!playerOccupied(nx,m.y,MONSTER_RADIUS))m.x=nx;
    if(!blocked(m.x,ny,MONSTER_RADIUS)&&!playerOccupied(m.x,ny,MONSTER_RADIUS))m.y=ny;
    return m.x!=bx||m.y!=by;
  }

  public boolean blocked(float x,float y){return blocked(x,y,PLAYER_RADIUS);}
  private boolean blocked(float x,float y,float radius){for(RectF r:obstacles)if(x+radius>r.left&&x-radius<r.right&&y+radius>r.top&&y-radius<r.bottom)return true;return false;}
  private boolean playerOccupied(float x,float y,float radius){if(!player.alive)return false;float min=radius+PLAYER_RADIUS+3f;return distance(x,y,player.x,player.y)<min;}
  private boolean monsterOccupied(float x,float y,float radius){for(Monster m:monsters){if(!m.alive)continue;float min=radius+MONSTER_RADIUS+3f;if(distance(x,y,m.x,m.y)<min)return true;}return false;}

  public Npc hitNpc(float x,float y,float radius){for(Npc n:npcs){float dx=x-n.x,dy=y-n.y;if(dx*dx+dy*dy<=radius*radius)return n;}return null;}
  public Monster hitMonster(float x,float y,float radius){for(Monster m:monsters){if(!m.alive)continue;float dx=x-m.x,dy=y-m.y;if(dx*dx+dy*dy<=radius*radius)return m;}return null;}
  public float distanceTo(Npc n){return distance(player.x,player.y,n.x,n.y);}
  public float distanceTo(Monster m){return distance(player.x,player.y,m.x,m.y);}

  public void damage(Monster m,int amount){
    if(m==null||!m.alive||amount<=0)return;m.hp=Math.max(0,m.hp-amount);m.hitFlash=.14f;m.damagePopupClock=.65f;m.lastDamage=amount;
    if(m.hp==0){m.alive=false;m.attackCooldown=0f;m.respawnClock=4f;}
  }
  public void damagePlayer(int amount){if(amount<=0||!player.alive)return;player.hp=Math.max(0,player.hp-amount);if(player.hp==0)player.alive=false;}
  public void revivePlayer(){player.x=player.spawnX;player.y=player.spawnY;player.hp=player.maxHp;player.mp=player.maxMp;player.alive=true;}

  public void tick(float dt){
    for(Monster m:monsters){
      m.hitFlash=Math.max(0,m.hitFlash-dt);m.damagePopupClock=Math.max(0,m.damagePopupClock-dt);
      if(m.alive)continue;m.respawnClock=Math.max(0f,m.respawnClock-dt);
      if(m.respawnClock<=0f){m.x=m.spawnX;m.y=m.spawnY;m.hp=m.maxHp;m.attackCooldown=0f;m.alive=true;m.lastDamage=0;}
    }
  }

  private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);}
  private static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}
}
