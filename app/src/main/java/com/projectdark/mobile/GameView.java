package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import com.projectdark.mobile.world.AdaptedMillesMapRenderer;
import com.projectdark.mobile.world.WorldCameraTransform;
import com.projectdark.mobile.world.WorldMoveTargetController;
import com.projectdark.mobile.world.WorldRuntimeAdapter;
import java.util.List;
import java.util.Map;

/** PROJECT DARK v0.74 - original-inspired mobile HUD adaptation + precise world tap routing. */
public final class GameView extends View {
  private static final float W=960f,H=540f;
  private static final float JOY_X=92f,JOY_Y=454f,JOY_R=58f;
  private static final float SLOT=42f,SLOT_GAP=4f,SLOT_X0=696f,SLOT_Y0=374f;
  private static final float ATK_X=914f,ATK_Y=493f,ATK_R=36f;
  private static final float MODE_X=810f,MODE_Y=495f,MODE_R=22f;
  private static final float AUTO_X=862f,AUTO_Y=495f,AUTO_R=24f;
  private static final float UTILITY_X0=684f,UTILITY_Y0=30f,UTILITY_STEP=42f,UTILITY_R=16f;

  private enum Action { IDLE,WALK,CAST,SWING,THRUST,THROW,PUNCH,SKILL,KICK }
  private enum FeedbackTone { INFO,WARN,REWARD }

  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RuntimeState state=new RuntimeState();
  private final WorldRuntimeAdapter worldAdapter=new WorldRuntimeAdapter(state,W,H);
  private final WorldCameraTransform camera=worldAdapter.camera();
  private final WorldMoveTargetController moveTarget=worldAdapter.movement();
  private final AdaptedMillesMapRenderer mapRenderer=new AdaptedMillesMapRenderer();
  private final CombatController combat=new CombatController();
  private final MonsterAIController monsterAi=new MonsterAIController();
  private final InteractionController interaction=new InteractionController();
  private final CharacterRenderer characterRenderer=new CharacterRenderer();
  private final RpgInventoryPresentation rpgPresentation=new RpgInventoryPresentation();
  private final RpgInteractionController rpgInteraction=new RpgInteractionController();
  private final ClassicHudIconAtlas hudIcons=new ClassicHudIconAtlas();

  private float scale=1,ox,oy,vx,vy;
  private float knobX=JOY_X,knobY=JOY_Y;
  private boolean joy,running,inventoryOpen;
  private long last,lastLedgerSequence=0,lastMoveRequestId=0,lastRewardSequence=0;
  private int dir=0;
  private float walkClock=0,actionClock=0,tapMarkerClock=0,tapMarkerX=0,tapMarkerY=0,directStepClock=0;
  private Action action=Action.IDLE;
  private WorldMoveTargetController.Status lastMoveStatus=WorldMoveTargetController.Status.IDLE;
  private String feedback="",pressedControl="",rewardBanner="";
  private float feedbackClock=0,rewardClock=0;
  private FeedbackTone feedbackTone=FeedbackTone.INFO;

  private final Runnable loop=new Runnable(){@Override public void run(){if(!running)return;long n=SystemClock.uptimeMillis();float dt=Math.min(.05f,(n-last)/1000f);last=n;update(dt);camera.follow(worldAdapter.presentationPlayerX(),worldAdapter.presentationPlayerY());invalidate();postDelayed(this,16);}};

  public GameView(Context c){super(c);setKeepScreenOn(true);worldAdapter.snapCameraToPlayer();}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(loop);}
  public void pause(){running=false;removeCallbacks(loop);}

  private void update(float dt){
    feedbackClock=Math.max(0,feedbackClock-dt);rewardClock=Math.max(0,rewardClock-dt);tapMarkerClock=Math.max(0,tapMarkerClock-dt);
    combat.tick(dt);state.tick(dt);consumeLedger();consumeRewardNotice();monsterAi.tick(state,dt);
    if(!state.player().alive){action=Action.IDLE;interaction.cancel();combat.cancelApproach();worldAdapter.cancel();joy=false;vx=vy=0;knobX=JOY_X;knobY=JOY_Y;return;}
    if(isActing()){actionClock+=dt;if(actionClock>=duration(action)){actionClock=0;action=(joy&&(vx!=0||vy!=0))?Action.WALK:Action.IDLE;}return;}
    WorldRuntimeAdapter.FrameSnapshot navigation=worldAdapter.tickNavigation(dt);
    if(joy&&(vx!=0||vy!=0)){
      directStepClock=Math.max(0f,directStepClock-dt);
      if(!worldAdapter.presentationMoving()&&directStepClock<=0f){WorldMoveTargetController.Snapshot step=worldAdapter.step(joystickDirection());directStepClock=WorldMoveTargetController.TILE_STEP_SECONDS;applyWorldFacing(step.lastStepDirection);consumeMoveOutcome(step);}
      if(worldAdapter.presentationMoving()){action=Action.WALK;walkClock+=dt;}else action=Action.IDLE;
      interaction.cancelApproach();combat.cancelApproach();return;
    }
    WorldMoveTargetController.Snapshot move=navigation.movement;
    if(move.status==WorldMoveTargetController.Status.MOVING||worldAdapter.presentationMoving()){
      if(move.lastStepDirection!=null)applyWorldFacing(move.lastStepDirection);
      action=Action.WALK;walkClock+=dt;return;
    }
    consumeMoveOutcome(move);action=Action.IDLE;
  }

  private void consumeMoveOutcome(WorldMoveTargetController.Snapshot move){
    if(move==null)return;
    if(move.requestId!=lastMoveRequestId){lastMoveRequestId=move.requestId;lastMoveStatus=WorldMoveTargetController.Status.IDLE;}
    if(move.status==lastMoveStatus)return;
    lastMoveStatus=move.status;
    if(move.status==WorldMoveTargetController.Status.REACHED){
      if(move.kind==WorldMoveTargetController.RequestKind.NPC_APPROACH){RuntimeState.Npc npc=findNpc(move.targetEntityId);if(npc!=null)interaction.request(state,npc);}
      else if(move.kind==WorldMoveTargetController.RequestKind.MONSTER_APPROACH)executeReadyCombatIntent();
      else if(move.kind==WorldMoveTargetController.RequestKind.GROUND)showFeedback("이동 완료",FeedbackTone.INFO);
    }else if(move.status==WorldMoveTargetController.Status.BLOCKED){
      if(move.kind==WorldMoveTargetController.RequestKind.MONSTER_APPROACH)combat.cancelApproach();
      showFeedback("이동할 수 없는 위치입니다",FeedbackTone.WARN);
    }
  }
  private void consumeLedger(){List<CombatLedger.Event> events=state.ledger().snapshot();for(CombatLedger.Event e:events){if(e.sequence<=lastLedgerSequence)continue;lastLedgerSequence=e.sequence;switch(e.type){case MONSTER_DEFEATED:showFeedback("몬스터 격파",FeedbackTone.INFO);break;case PLAYER_HIT:showFeedback("-"+e.amount+" HP",FeedbackTone.WARN);break;case PLAYER_DEFEATED:showFeedback("행동 불능",FeedbackTone.WARN);break;case PLAYER_REVIVED:showFeedback("부활",FeedbackTone.INFO);break;default:break;}}}
  private void consumeRewardNotice(){RpgInventoryPresentation.RewardNotice notice=rpgPresentation.latestRewardNotice(state.rpg());if(notice==null||notice.combatSequence<=lastRewardSequence)return;lastRewardSequence=notice.combatSequence;if(notice.status!=RpgProgressionState.RewardStatus.RESOLVED){showFeedback("보상 확인 필요",FeedbackTone.WARN);return;}if(!notice.autoLootedItems.isEmpty()){Map.Entry<String,Integer> first=null;for(Map.Entry<String,Integer> e:notice.autoLootedItems.entrySet()){first=e;break;}if(first!=null){int visibleQty=inventoryQuantity(first.getKey());if(visibleQty<first.getValue()){showFeedback("보상 지급 상태를 확인할 수 없습니다",FeedbackTone.WARN);return;}String name=itemDisplayName(first.getKey());int extra=Math.max(0,notice.autoLootedItems.size()-1);showReward("자동루팅 · "+name+" x"+first.getValue()+(extra>0?" 외 "+extra+"종":""));return;}}if(notice.exp!=null){showReward("보상 처리 · EXP +"+notice.exp);return;}showReward("보상 처리 완료");}
  private int inventoryQuantity(String itemId){for(RpgInventoryPresentation.ItemRow row:rpgPresentation.inventoryRows(state.rpg()))if(row.itemId.equals(itemId))return row.quantity;return 0;}
  private String itemDisplayName(String itemId){for(RpgInventoryPresentation.ItemRow row:rpgPresentation.inventoryRows(state.rpg()))if(row.itemId.equals(itemId))return row.name;return itemId;}
  private RuntimeState.Npc findNpc(String id){if(id==null)return null;for(RuntimeState.Npc npc:state.npcs())if(id.equals(npc.id))return npc;return null;}

  private void executeReadyCombatIntent(){CombatController.Intent ready=combat.consumeReadyIntent();action=Action.IDLE;if(ready==CombatController.Intent.ATTACK)attack();else if(ready==CombatController.Intent.CAST)cast();else if(ready==CombatController.Intent.SKILL)skill();else if(ready==CombatController.Intent.KICK)kick();}
  private void beginCombatApproach(CombatController.Intent intent){worldAdapter.cancelForAction();if(combat.beginApproach(intent)){RuntimeState.Monster target=combat.approachTarget();if(target!=null)worldAdapter.requestMonsterApproach(target.id,combat.intentRange());showFeedback("타깃으로 이동",FeedbackTone.INFO);}}

  private void setFacingDirection(float dx,float dy){float sx=dx>=0?1f:-1f,sy=dy>=0?1f:-1f;if(sx<0&&sy>0)dir=0;else if(sx>0&&sy>0)dir=1;else if(sx<0)dir=2;else dir=3;}
  private void applyWorldFacing(WorldMoveTargetController.Direction direction){if(direction==null)return;switch(direction){case SW:dir=0;break;case SE:dir=1;break;case NW:dir=2;break;case NE:dir=3;break;}}
  private WorldMoveTargetController.Direction joystickDirection(){if(vx>0f&&vy>0f)return WorldMoveTargetController.Direction.SE;if(vx<0f&&vy<0f)return WorldMoveTargetController.Direction.NW;if(vx>0f)return WorldMoveTargetController.Direction.NE;return WorldMoveTargetController.Direction.SW;}
  private void setAutoDirection(float dx,float dy){float sx=dx>=0?1f:-1f,sy=dy>=0?1f:-1f;vx=.707f*sx;vy=.707f*sy;}
  private void faceTarget(){RuntimeState.Monster t=combat.target();if(t!=null&&t.alive)setFacingDirection(t.x-state.player().x,t.y-state.player().y);}
  private boolean isActing(){return action!=Action.IDLE&&action!=Action.WALK;}
  private boolean attacking(){return action==Action.SWING||action==Action.THRUST||action==Action.THROW||action==Action.PUNCH;}
  private float duration(Action a){switch(a){case CAST:return .65f;case SWING:return .45f;case THRUST:return .38f;case THROW:return .52f;case PUNCH:return .32f;case SKILL:return .50f;case KICK:return .45f;default:return 0;}}
  private void trigger(Action a){if(isActing()||!state.player().alive)return;worldAdapter.cancelForAction();interaction.cancel();combat.cancelApproach();action=a;actionClock=0;}
  private void showFeedback(String s){showFeedback(s,FeedbackTone.INFO);}private void showFeedback(String s,FeedbackTone tone){feedback=s;feedbackTone=tone;feedbackClock=1.15f;}private void showReward(String s){rewardBanner=s;rewardClock=2.4f;}
  private boolean consume(SkillDef def){if(state.player().mp<def.mpCost){showFeedback("MP가 부족합니다",FeedbackTone.WARN);return false;}state.player().mp-=def.mpCost;return true;}
  private boolean requireTarget(){if(combat.hasUsableTarget())return true;showFeedback("먼저 몬스터를 선택하세요",FeedbackTone.WARN);return false;}
  private boolean inRange(float range){return combat.inRange(state,range);}
  private void applyToTarget(SkillDef def){RuntimeState.Monster t=combat.target();if(t!=null&&t.alive&&inRange(def.range))state.damage(t,def.damage);}
  private void cast(){worldAdapter.cancelForAction();if(isActing()||!combat.castReady()||!requireTarget())return;if(!inRange(SkillDef.CAST_PROTO.range)){beginCombatApproach(CombatController.Intent.CAST);return;}if(!consume(SkillDef.CAST_PROTO))return;faceTarget();combat.commitCast();trigger(Action.CAST);applyToTarget(SkillDef.CAST_PROTO);}
  private void skill(){worldAdapter.cancelForAction();if(isActing()||!combat.skillReady()||!requireTarget())return;if(!inRange(SkillDef.SKILL_PROTO.range)){beginCombatApproach(CombatController.Intent.SKILL);return;}if(!consume(SkillDef.SKILL_PROTO))return;faceTarget();combat.commitSkill();trigger(Action.SKILL);applyToTarget(SkillDef.SKILL_PROTO);}
  private void kick(){worldAdapter.cancelForAction();if(isActing()||!combat.kickReady()||!requireTarget())return;if(!inRange(SkillDef.KICK_PROTO.range)){beginCombatApproach(CombatController.Intent.KICK);return;}faceTarget();combat.commitKick();trigger(Action.KICK);applyToTarget(SkillDef.KICK_PROTO);}
  private Action actionFor(AttackDef.Kind k){switch(k){case THRUST:return Action.THRUST;case THROW:return Action.THROW;case PUNCH:return Action.PUNCH;default:return Action.SWING;}}
  private void attack(){worldAdapter.cancelForAction();if(isActing()||!combat.attackReady()||!state.player().alive||!requireTarget())return;AttackDef def=combat.attackDef();if(!inRange(def.range)){beginCombatApproach(CombatController.Intent.ATTACK);return;}faceTarget();combat.commitAttack();trigger(actionFor(def.kind));RuntimeState.Monster t=combat.target();if(t!=null&&t.alive)state.damage(t,def.damage);}

  protected void onSizeChanged(int w,int h,int ow,int oh){scale=Math.min(w/W,h/H);ox=(w-W*scale)/2;oy=(h-H*scale)/2;}
  protected void onDraw(Canvas c){c.drawColor(Color.BLACK);c.save();c.translate(ox,oy);c.scale(scale,scale);drawWorld(c);c.save();c.translate(-camera.cameraX(),-camera.cameraY());drawTapMarker(c);drawNpcs(c);drawMonsters(c);drawCharacter(c);c.restore();drawHud(c);drawFeedbackBanners(c);drawInventory(c);drawDialogue(c);drawDeath(c);c.restore();}

  private void drawWorld(Canvas c){p.setColor(0xff101511);c.drawRect(0,0,W,H,p);mapRenderer.draw(c,worldAdapter);}
  private void drawTapMarker(Canvas c){if(tapMarkerClock<=0)return;float q=Math.max(0f,Math.min(1f,tapMarkerClock/.7f));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(0xD8FFD86B);c.drawCircle(tapMarkerX,tapMarkerY,8f+8f*(1f-q),p);p.setStyle(Paint.Style.FILL);p.setColor(0x66FFD86B);c.drawCircle(tapMarkerX,tapMarkerY,3.5f,p);}
  private void drawNpcs(Canvas c){for(RuntimeState.Npc n:state.npcs()){p.setColor(0x66000000);c.drawOval(new RectF(n.x-11,n.y-3,n.x+11,n.y+4),p);p.setColor(0xffcab58b);c.drawCircle(n.x,n.y-39,7,p);p.setColor(0xff6c5946);c.drawRect(n.x-7,n.y-31,n.x+7,n.y-11,p);p.setColor(0xffb58f58);c.drawRect(n.x-5,n.y-12,n.x-1,n.y-3,p);c.drawRect(n.x+1,n.y-12,n.x+5,n.y-3,p);p.setTextSize(9);p.setColor(0xfff3e1ae);float tw=p.measureText(n.name);c.drawText(n.name,n.x-tw/2,n.y-52,p);if(interaction.approachNpc()==n){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xfff0d17a);c.drawCircle(n.x,n.y-24,19,p);p.setStyle(Paint.Style.FILL);}}}
  private void drawMonsters(Canvas c){RuntimeState.Monster selected=combat.target();for(RuntimeState.Monster m:state.monsters()){if(!m.alive)continue;p.setColor(0x66000000);c.drawOval(new RectF(m.x-15,m.y-4,m.x+15,m.y+5),p);p.setColor(m.hitFlash>0?0xffd9e8d7:0xff6a7c69);c.drawOval(new RectF(m.x-13,m.y-31,m.x+13,m.y-5),p);p.setColor(0xffd8c27b);c.drawCircle(m.x-5,m.y-20,2,p);c.drawCircle(m.x+5,m.y-20,2,p);if(m.attackPrimed){float q=1f-Math.min(1f,m.attackWindup/.24f);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2+2*q);p.setColor(0xaaff7755);c.drawCircle(m.x,m.y-19,18+8*q,p);p.setStyle(Paint.Style.FILL);}bar(c,m.x-18,m.y-42,m.x+18,m.y-37,0xffd63442,m.hp/(float)m.maxHp);if(m.damagePopupClock>0){p.setTextSize(12);p.setColor(0xffffdc72);String d="-"+m.lastDamage;float tw=p.measureText(d);c.drawText(d,m.x-tw/2,m.y-50-(.65f-m.damagePopupClock)*20,p);}if(selected==m){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.3f);p.setColor(0xffffd86b);c.drawOval(new RectF(m.x-21,m.y-9,m.x+21,m.y+9),p);p.setStyle(Paint.Style.FILL);}}}
  private CharacterRenderer.Direction characterDirection(){switch(dir){case 1:return CharacterRenderer.Direction.SE;case 2:return CharacterRenderer.Direction.NW;case 3:return CharacterRenderer.Direction.NE;default:return CharacterRenderer.Direction.SW;}}
  private CharacterRenderer.State characterState(){if(!state.player().alive)return CharacterRenderer.State.DEAD;if(state.player().hitFlash>0)return CharacterRenderer.State.HIT;switch(action){case WALK:return CharacterRenderer.State.WALK;case CAST:return CharacterRenderer.State.CAST;case SKILL:case KICK:return CharacterRenderer.State.SKILL;case SWING:case THRUST:case THROW:case PUNCH:return CharacterRenderer.State.ATTACK;default:return CharacterRenderer.State.IDLE;}}
  private CharacterRenderer.EffectFamily characterEffectFamily(){if(state.player().hitFlash>0)return CharacterRenderer.EffectFamily.HIT;switch(action){case CAST:return CharacterRenderer.EffectFamily.CAST;case THROW:return CharacterRenderer.EffectFamily.THROW;case PUNCH:return CharacterRenderer.EffectFamily.PUNCH;case KICK:return CharacterRenderer.EffectFamily.KICK;case SKILL:return CharacterRenderer.EffectFamily.SKILL;default:return CharacterRenderer.EffectFamily.NONE;}}
  private void drawCharacter(Canvas c){CharacterRenderer.State presentation=characterState();CharacterVisualBinding visuals=CharacterVisualBinding.from(state.rpg());float stateDuration=isActing()?duration(action):1f;characterRenderer.draw(c,new CharacterRenderer.Pose(worldAdapter.presentationPlayerX(),worldAdapter.presentationPlayerY(),characterDirection(),presentation,walkClock,actionClock,stateDuration,state.player().hitFlash>0,visuals.equipmentVisualRef(),visuals.weaponVisualRef(),CharacterRenderer.ASSET_STATUS,characterEffectFamily()));}

  private void panel(Canvas c,float l,float t,float r,float b){p.setStyle(Paint.Style.FILL);p.setColor(0x8A17120E);c.drawRoundRect(new RectF(l,t,r,b),7,7,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xA0A86F3B);c.drawRoundRect(new RectF(l+.5f,t+.5f,r-.5f,b-.5f),7,7,p);p.setStyle(Paint.Style.FILL);}
  private void modalPanel(Canvas c,float l,float t,float r,float b){p.setStyle(Paint.Style.FILL);p.setColor(0xED11151A);c.drawRoundRect(new RectF(l,t,r,b),12,12,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.4f);p.setColor(0xAACFB171);c.drawRoundRect(new RectF(l+.5f,t+.5f,r-.5f,b-.5f),12,12,p);p.setStyle(Paint.Style.FILL);}
  private void text(Canvas c,String s,float x,float y,float size){p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));p.setTextSize(size);p.setColor(0xfff4ead5);c.drawText(s,x,y,p);p.setTypeface(Typeface.DEFAULT);}
  private void mutedText(Canvas c,String s,float x,float y,float size){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);p.setColor(0xffb8aa91);c.drawText(s,x,y,p);}
  private void bar(Canvas c,float l,float t,float r,float b,int color,float ratio){ratio=Math.max(0,Math.min(1,ratio));p.setColor(0xCC171416);c.drawRoundRect(new RectF(l,t,r,b),(b-t)*.3f,(b-t)*.3f,p);p.setColor(color);c.drawRoundRect(new RectF(l,t,l+(r-l)*ratio,b),(b-t)*.3f,(b-t)*.3f,p);}

  private void drawHud(Canvas c){drawTopLeftStatus(c);drawQuest(c);drawTarget(c);drawMinimap(c);drawUtilityRail(c);drawChat(c);drawJoystick(c);drawExpStrip(c);drawCombatCluster(c);}
  private void drawTopLeftStatus(Canvas c){panel(c,14,12,202,108);Integer level=state.rpg().normalLevel();text(c,"Lv. "+(level==null?"?":level),25,36,14);mutedText(c,"평민",26,55,8);bar(c,70,22,190,37,0xffd73d31,state.player().hp/(float)state.player().maxHp);bar(c,70,43,190,58,0xff347fcf,state.player().mp/(float)state.player().maxMp);mutedText(c,state.player().hp+" / "+state.player().maxHp,106,34,7);mutedText(c,state.player().mp+" / "+state.player().maxMp,106,55,7);for(int i=0;i<8;i++){float x=24+i*20;p.setColor(0xCC25211D);c.drawRect(x,70,x+15,85,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xff8f6a43);c.drawRect(x,70,x+15,85,p);p.setStyle(Paint.Style.FILL);p.setColor(i%2==0?0xff6d9a58:0xff755dc0);c.drawCircle(x+7.5f,77.5f,4,p);}mutedText(c,"BUFF",24,100,7);}
  private void drawQuest(Canvas c){panel(c,14,124,214,282);text(c,"퀘스트",28,146,11);mutedText(c,"[메인] 밀레스의 첫걸음",28,171,9);mutedText(c,"NPC와 대화하기  0/1",38,190,8);mutedText(c,"[반복] 수련의 길",28,219,9);mutedText(c,"주변 몬스터 처치  0/5",38,238,8);mutedText(c,"탭하여 접기",130,270,7);}
  private void drawTarget(Canvas c){RuntimeState.Monster selected=combat.target();if(selected==null)return;panel(c,380,12,580,55);text(c,selected.name,397,31,9.5f);bar(c,397,39,563,48,0xffd63e49,selected.hp/(float)selected.maxHp);}
  private void drawMinimap(Canvas c){panel(c,810,12,946,112);text(c,"CH. 5-1",846,30,9);p.setColor(0xB7232B1F);c.drawRect(820,38,936,92,p);float nx=(worldAdapter.presentationPlayerX()-WorldDef.MIN_X)/Math.max(1f,WorldDef.MAX_X-WorldDef.MIN_X),ny=(worldAdapter.presentationPlayerY()-WorldDef.MIN_Y)/Math.max(1f,WorldDef.MAX_Y-WorldDef.MIN_Y);float px=823+Math.max(0,Math.min(1,nx))*110,py=41+Math.max(0,Math.min(1,ny))*48;p.setColor(0xffffd44f);c.drawCircle(px,py,3.5f,p);mutedText(c,"밀레스",861,105,8);}
  private void drawUtilityRail(Canvas c){String[] labels={"BAG","SHOP","EVENT","MAIL","≡","⚙"};for(int i=0;i<labels.length;i++){float cx=UTILITY_X0+(i%3)*UTILITY_STEP,cy=UTILITY_Y0+(i/3)*UTILITY_STEP;boolean active=i==0&&inventoryOpen;p.setColor(active?0xD75E3F24:0xA81C1815);c.drawCircle(cx,cy,UTILITY_R,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(active?2f:1f);p.setColor(active?0xffffd477:0x99bb8753);c.drawCircle(cx,cy,UTILITY_R,p);p.setStyle(Paint.Style.FILL);p.setTextSize(labels[i].length()>3?5.8f:7f);p.setColor(active?0xffffefc7:0xffe4d5bc);float tw=p.measureText(labels[i]);c.drawText(labels[i],cx-tw/2,cy+2.5f,p);}}
  private void drawChat(Canvas c){RuntimeMetrics metrics=state.metrics();p.setColor(0x76120f0e);c.drawRoundRect(new RectF(270,418,642,522),6,6,p);text(c,"전체    지역    파티    길드",286,438,8.5f);mutedText(c,"[지역] 밀레스에 오신 것을 환영합니다.",286,460,8);mutedText(c,"[전투] 격파 "+metrics.monsterDefeats()+" · 피격 "+metrics.playerHits()+" · DMG "+metrics.damageDealt(),286,479,8);mutedText(c,"[시스템] NPC · 전투 · 보상 알림",286,498,8);p.setColor(0x7F211B17);c.drawRoundRect(new RectF(282,505,630,518),5,5,p);mutedText(c,"메시지를 입력하세요.",292,516,7);}
  private void drawJoystick(Canvas c){p.setColor(joy?0x523D4652:0x2A2B3038);c.drawCircle(JOY_X,JOY_Y,JOY_R,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(joy?2.2f:1.2f);p.setColor(joy?0xC8DBC386:0x697F7562);c.drawCircle(JOY_X,JOY_Y,JOY_R,p);p.setStrokeWidth(1);p.setColor(0x447F7562);c.drawCircle(JOY_X,JOY_Y,36,p);p.setStyle(Paint.Style.FILL);p.setColor(joy?0xD7C7A86B:0x9A766951);c.drawCircle(knobX,knobY,joy?24:22,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xDCEAD9B0);c.drawCircle(knobX,knobY,joy?24:22,p);p.setStyle(Paint.Style.FILL);}
  private void drawExpStrip(Canvas c){p.setColor(0xB4101112);c.drawRect(0,531,W,540,p);p.setColor(0xff46ad45);c.drawRect(0,535,W*.35f,540,p);mutedText(c,"EXP",216,538,6.5f);}

  private RectF slotRect(int index){int col=index%5,row=index/5;float l=SLOT_X0+col*(SLOT+SLOT_GAP),t=SLOT_Y0+row*(SLOT+SLOT_GAP);return new RectF(l,t,l+SLOT,t+SLOT);}
  private void drawCombatCluster(Canvas c){
    boolean target=combat.hasUsableTarget();
    boolean skillEnabled=target&&combat.skillReady()&&state.player().alive&&state.player().mp>=SkillDef.SKILL_PROTO.mpCost;
    boolean magEnabled=target&&combat.castReady()&&state.player().alive&&state.player().mp>=SkillDef.CAST_PROTO.mpCost;
    boolean kickEnabled=target&&combat.kickReady()&&state.player().alive;
    ClassicHudIconAtlas.Icon[] icons={ClassicHudIconAtlas.Icon.SLASH,ClassicHudIconAtlas.Icon.WAVE,ClassicHudIconAtlas.Icon.CLAW,ClassicHudIconAtlas.Icon.BURST,ClassicHudIconAtlas.Icon.AURA,ClassicHudIconAtlas.Icon.FLAME,ClassicHudIconAtlas.Icon.PALM,ClassicHudIconAtlas.Icon.SPIRAL,ClassicHudIconAtlas.Icon.BODY,ClassicHudIconAtlas.Icon.COMET};
    for(int i=0;i<10;i++){boolean enabled=i==0?skillEnabled:i==1?magEnabled:i==2?kickEnabled:true;boolean selected=i==0&&action==Action.SKILL||i==1&&action==Action.CAST||i==2&&action==Action.KICK;boolean pressed=(i==0&&"SKILL".equals(pressedControl))||(i==1&&"MAG".equals(pressedControl))||(i==2&&"KICK".equals(pressedControl));hudIcons.draw(c,p,icons[i],slotRect(i),enabled,selected,pressed);}
    boolean atkEnabled=target&&combat.attackReady()&&state.player().alive;
    RectF atk=new RectF(ATK_X-ATK_R,ATK_Y-ATK_R,ATK_X+ATK_R,ATK_Y+ATK_R);hudIcons.draw(c,p,ClassicHudIconAtlas.Icon.ATTACK,atk,atkEnabled,attacking(),"ATK".equals(pressedControl));
    circleIcon(c,MODE_X,MODE_Y,MODE_R,ClassicHudIconAtlas.Icon.MODE,true,"MODE".equals(pressedControl));
    circleIcon(c,AUTO_X,AUTO_Y,AUTO_R,ClassicHudIconAtlas.Icon.AUTO,false,"AUTO".equals(pressedControl));
    mutedText(c,combat.attackDef().label,882,537,6.8f);
  }
  private void circleIcon(Canvas c,float cx,float cy,float r,ClassicHudIconAtlas.Icon icon,boolean enabled,boolean pressed){RectF b=new RectF(cx-r,cy-r,cx+r,cy+r);hudIcons.draw(c,p,icon,b,enabled,false,pressed);}
  private void cooldown(Canvas c,float x,float y,float r,float remaining,float total){if(remaining<=0||total<=0)return;float ratio=Math.min(1f,remaining/total);p.setColor(0xA9000000);c.drawArc(new RectF(x-r,y-r,x+r,y+r),-90,360*ratio,true,p);p.setTextSize(8);p.setColor(Color.WHITE);String s=String.format(java.util.Locale.US,"%.1f",remaining);float tw=p.measureText(s);c.drawText(s,x-tw/2,y+3,p);}

  private void drawFeedbackBanners(Canvas c){if(rewardClock>0){p.setColor(0xD02B2517);c.drawRoundRect(new RectF(346,74,614,106),16,16,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);p.setColor(0xBADBB768);c.drawRoundRect(new RectF(346,74,614,106),16,16,p);p.setStyle(Paint.Style.FILL);p.setTextSize(10);p.setColor(0xffffe5a3);float tw=p.measureText(rewardBanner);c.drawText(rewardBanner,480-tw/2,95,p);}if(feedbackClock>0){int fill=feedbackTone==FeedbackTone.WARN?0xD04A2226:feedbackTone==FeedbackTone.REWARD?0xD03C321E:0xCC161A20;p.setColor(fill);p.setTextSize(9.2f);float width=Math.min(340,Math.max(146,p.measureText(feedback)+38));c.drawRoundRect(new RectF(480-width/2,111,480+width/2,139),14,14,p);p.setColor(feedbackTone==FeedbackTone.WARN?0xffffb4ad:0xffeee5d3);float ft=p.measureText(feedback);c.drawText(feedback,480-ft/2,130,p);}}

  private void drawInventory(Canvas c){if(!inventoryOpen)return;p.setColor(0x62000000);c.drawRect(0,0,W,H,p);modalPanel(c,588,92,904,444);text(c,"INVENTORY",608,118,12.5f);Integer level=state.rpg().normalLevel();mutedText(c,"평민 · Lv "+(level==null?"?":level),608,135,8f);p.setColor(0xB024262A);c.drawCircle(879,113,15,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.1f);p.setColor(0x99C8A96A);c.drawCircle(879,113,15,p);p.setStyle(Paint.Style.FILL);text(c,"×",874,118,14);List<RpgInventoryPresentation.ItemRow> rows=rpgPresentation.inventoryRows(state.rpg());String selectedId=rpgInteraction.selectedInventoryItemId();if(rows.isEmpty())mutedText(c,"보유 아이템 없음",608,171,10);else{int limit=Math.min(7,rows.size());for(int i=0;i<limit;i++){RpgInventoryPresentation.ItemRow row=rows.get(i);float y=169+i*27;boolean selected=row.itemId.equals(selectedId);p.setColor(selected?0x665D4A28:0x5517191D);c.drawRoundRect(new RectF(602,y-16,890,y+6),7,7,p);if(selected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);p.setColor(0xBBD2AF67);c.drawRoundRect(new RectF(602,y-16,890,y+6),7,7,p);p.setStyle(Paint.Style.FILL);}text(c,row.name+"  x"+row.quantity+(row.equipped?"  [장착]":""),614,y-2,9);mutedText(c,row.itemId,790,y-2,7f);}if(rows.size()>limit)mutedText(c,"외 "+(rows.size()-limit)+"개",610,169+limit*27,8);}RpgInventoryPresentation.ItemRow selectedRow=null;if(selectedId!=null)for(RpgInventoryPresentation.ItemRow row:rows)if(selectedId.equals(row.itemId)){selectedRow=row;break;}p.setColor(0x5517191D);c.drawRoundRect(new RectF(602,356,890,426),8,8,p);if(selectedRow!=null){text(c,selectedRow.name,614,375,10);mutedText(c,rpgPresentation.requirementLabel(selectedRow),614,391,8);String element=rpgPresentation.elementLabel(selectedRow);if(!element.isEmpty())mutedText(c,element,614,406,7.5f);}else mutedText(c,"아이템을 선택하세요",614,386,9);p.setColor(0xD05B4727);c.drawRoundRect(new RectF(810,389,880,419),9,9,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.1f);p.setColor(0xCCD3B16D);c.drawRoundRect(new RectF(810,389,880,419),9,9,p);p.setStyle(Paint.Style.FILL);text(c,"장착",833,409,9.5f);RpgInventoryPresentation.RewardNotice notice=rpgPresentation.latestRewardNotice(state.rpg());if(notice!=null)mutedText(c,"최근 보상 · "+(notice.status==RpgProgressionState.RewardStatus.RESOLVED?"처리됨":"확인 필요"),608,438,7.3f);}
  private String equipResultLabel(RpgProgressionState.EquipResult result){switch(result){case EQUIPPED:return "장착 완료";case REQUIREMENT_NOT_MET:return "장착 불가 · 조건 미충족";case REQUIREMENT_PENDING:return "장착 보류 · 조건 확인 필요";case NOT_EQUIPPABLE:return "장착할 수 없는 아이템";case UNKNOWN_ITEM:return "알 수 없는 아이템";case ITEM_NOT_OWNED:default:return "보유하지 않은 아이템";}}
  private boolean handleInventoryTouch(float x,float y){if(!inventoryOpen)return false;if(dist(x,y,879,113)<=24){inventoryOpen=false;showFeedback("인벤토리 닫힘",FeedbackTone.INFO);return true;}List<RpgInventoryPresentation.ItemRow> rows=rpgPresentation.inventoryRows(state.rpg());int limit=Math.min(7,rows.size());if(x>=602&&x<=890){for(int i=0;i<limit;i++){float ry=169+i*27;if(y>=ry-18&&y<=ry+8){RpgInventoryPresentation.ItemRow row=rows.get(i);if(rpgInteraction.selectInventoryItem(state.rpg(),row.itemId))showFeedback(row.name+" 선택",FeedbackTone.INFO);return true;}}}if(x>=810&&x<=880&&y>=389&&y<=419){RpgProgressionState.EquipResult result=rpgInteraction.equipSelectedDetailed(state.rpg());showFeedback(equipResultLabel(result),result==RpgProgressionState.EquipResult.EQUIPPED?FeedbackTone.INFO:FeedbackTone.WARN);return true;}return inside(x,y,588,92,904,444);}

  private void drawDialogue(Canvas c){RuntimeState.Npc dialogNpc=interaction.dialogNpc();if(dialogNpc==null)return;p.setColor(0x74000000);c.drawRect(0,0,W,H,p);modalPanel(c,190,326,770,462);p.setColor(0xC95A472A);c.drawRoundRect(new RectF(210,342,338,371),8,8,p);text(c,dialogNpc.name,226,361,11.5f);drawWrappedText(c,dialogNpc.dialogue,218,394,510,11,18);mutedText(c,"화면을 터치하면 닫기",616,446,8);p.setColor(0xB024262A);c.drawCircle(744,347,14,p);text(c,"×",739,352,14);}
  private void drawDeath(Canvas c){if(state.player().alive)return;p.setColor(0xB6000000);c.drawRect(0,0,W,H,p);modalPanel(c,330,205,630,335);text(c,"행동 불능",428,242,18);mutedText(c,"프로토타입 부활",432,270,9.5f);p.setColor(0xD05B4727);c.drawRoundRect(new RectF(405,286,555,320),10,10,p);text(c,"화면 중앙 터치",433,308,10.5f);}
  private void drawWrappedText(Canvas c,String s,float x,float y,float maxWidth,float size,float lineHeight){p.setTextSize(size);p.setColor(0xffeee4cf);String[] words=s.split(" ");String line="";float yy=y;for(String word:words){String test=line.length()==0?word:line+" "+word;if(p.measureText(test)>maxWidth&&line.length()>0){c.drawText(line,x,yy,p);yy+=lineHeight;line=word;}else line=test;}if(line.length()>0)c.drawText(line,x,yy,p);}

  static boolean blocksWorldTapForHud(float x,float y,boolean targetVisible){
    if(inside(x,y,14,12,202,108)||inside(x,y,14,124,214,282)||inside(x,y,810,12,946,112)||inside(x,y,270,418,642,522))return true;
    if(targetVisible&&inside(x,y,380,12,580,55))return true;
    if(circleHit(x,y,JOY_X,JOY_Y,JOY_R))return true;
    for(int i=0;i<6;i++){float cx=UTILITY_X0+(i%3)*UTILITY_STEP,cy=UTILITY_Y0+(i/3)*UTILITY_STEP;if(circleHit(x,y,cx,cy,UTILITY_R+2))return true;}
    for(int i=0;i<10;i++)if(slotRectStatic(i).contains(x,y))return true;
    return circleHit(x,y,ATK_X,ATK_Y,ATK_R+4)||circleHit(x,y,MODE_X,MODE_Y,MODE_R+3)||circleHit(x,y,AUTO_X,AUTO_Y,AUTO_R+3);
  }
  private static RectF slotRectStatic(int index){int col=index%5,row=index/5;float l=SLOT_X0+col*(SLOT+SLOT_GAP),t=SLOT_Y0+row*(SLOT+SLOT_GAP);return new RectF(l,t,l+SLOT,t+SLOT);}
  private boolean isHudSurface(float x,float y){return blocksWorldTapForHud(x,y,combat.target()!=null);}
  private void requestGroundMove(float x,float y){WorldMoveTargetController.Snapshot move=worldAdapter.requestGroundScreenTap(x,y);lastMoveRequestId=move.requestId;lastMoveStatus=WorldMoveTargetController.Status.IDLE;if(move.status==WorldMoveTargetController.Status.MOVING||move.status==WorldMoveTargetController.Status.REACHED){tapMarkerX=move.targetX;tapMarkerY=move.targetY;tapMarkerClock=.7f;showFeedback(move.replacedRequestId>0?"이동 목표 변경":"이동 시작",FeedbackTone.INFO);}consumeMoveOutcome(move);}

  public boolean onTouchEvent(MotionEvent e){float x=(e.getX()-ox)/scale,y=(e.getY()-oy)/scale;switch(e.getActionMasked()){
    case MotionEvent.ACTION_DOWN:
      if(!state.player().alive){if(dist(x,y,480,270)<=180){state.revivePlayer();combat.clearTarget();interaction.cancel();worldAdapter.cancel();worldAdapter.snapCameraToPlayer();action=Action.IDLE;}return true;}
      if(interaction.dialogOpen()){interaction.dismissDialog();worldAdapter.cancelForAction();showFeedback("대화 종료",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0,UTILITY_Y0,UTILITY_R+4)){inventoryOpen=!inventoryOpen;worldAdapter.cancelForAction();showFeedback(inventoryOpen?"인벤토리 열림":"인벤토리 닫힘",FeedbackTone.INFO);return true;}
      if(handleInventoryTouch(x,y)){worldAdapter.cancelForAction();return true;}
      if(inventoryOpen){inventoryOpen=false;worldAdapter.cancelForAction();showFeedback("인벤토리 닫힘",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,JOY_X,JOY_Y,JOY_R)){joy=true;directStepClock=0f;pressedControl="JOY";worldAdapter.cancelForDirectInput();interaction.cancelApproach();combat.cancelApproach();stick(x,y);return true;}
      if(slotRect(0).contains(x,y)){pressedControl="SKILL";skill();return true;}
      if(slotRect(1).contains(x,y)){pressedControl="MAG";cast();return true;}
      if(slotRect(2).contains(x,y)){pressedControl="KICK";kick();return true;}
      for(int i=3;i<10;i++)if(slotRect(i).contains(x,y)){pressedControl="SLOT"+i;showFeedback("퀵슬롯 준비 중",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,MODE_X,MODE_Y,MODE_R+3)){pressedControl="MODE";worldAdapter.cancelForAction();combat.cycleAttackMode();showFeedback(combat.attackDef().label,FeedbackTone.INFO);return true;}
      if(circleHit(x,y,ATK_X,ATK_Y,ATK_R+4)){pressedControl="ATK";attack();return true;}
      if(circleHit(x,y,AUTO_X,AUTO_Y,AUTO_R+3)){pressedControl="AUTO";showFeedback("AUTO 준비 중",FeedbackTone.WARN);return true;}
      if(isHudSurface(x,y))return true;
      WorldCameraTransform.Point wp=worldAdapter.screenToWorld(x,y);RuntimeState.Npc npc=state.hitNpc(wp.x,wp.y,34f);if(npc!=null){combat.cancelApproach();interaction.cancelApproach();worldAdapter.requestNpcApproach(npc.id);showFeedback("NPC 접근 · "+npc.name,FeedbackTone.INFO);return true;}RuntimeState.Monster monster=state.hitMonster(wp.x,wp.y,34f);if(monster!=null){worldAdapter.cancelForAction();combat.selectTarget(monster);interaction.cancelApproach();showFeedback("타깃 선택 · "+monster.name,FeedbackTone.INFO);return true;}requestGroundMove(x,y);return true;
    case MotionEvent.ACTION_MOVE:if(joy)stick(x,y);return true;
    case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:joy=false;directStepClock=0f;pressedControl="";knobX=JOY_X;knobY=JOY_Y;vx=vy=0;return true;
  }return true;}
  private void stick(float x,float y){if(isActing()||!state.player().alive)return;float dx=x-JOY_X,dy=y-JOY_Y,len=(float)Math.sqrt(dx*dx+dy*dy);if(len>JOY_R){dx=dx/len*JOY_R;dy=dy/len*JOY_R;len=JOY_R;}knobX=JOY_X+dx;knobY=JOY_Y+dy;if(len<8){vx=vy=0;return;}float nx=dx/len,ny=dy/len;if(Math.abs(nx)>Math.abs(ny)){if(nx>0){vx=.707f;vy=.707f;dir=1;}else{vx=-.707f;vy=-.707f;dir=2;}}else{if(ny>0){vx=-.707f;vy=.707f;dir=0;}else{vx=.707f;vy=-.707f;dir=3;}}}
  private static boolean inside(float x,float y,float l,float t,float r,float b){return x>=l&&x<=r&&y>=t&&y<=b;}
  private static boolean circleHit(float x,float y,float cx,float cy,float r){float dx=x-cx,dy=y-cy;return dx*dx+dy*dy<=r*r;}
  private float dist(float a,float b,float c,float d){float x=a-c,y=b-d;return(float)Math.sqrt(x*x+y*y);}
}
