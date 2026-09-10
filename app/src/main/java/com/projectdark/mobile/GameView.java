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

/** PROJECT DARK v0.73 - playtest-driven mobile HUD + precise touch routing + World adapter integration. */
public final class GameView extends View {
  private static final float W=960f,H=540f;
  private static final float JOY_X=92f,JOY_Y=449f,JOY_R=58f;
  private static final float ATK_X=884f,ATK_Y=466f,ATK_R=40f;
  private static final float SKILL_X=824f,SKILL_Y=414f,SKILL_R=27f;
  private static final float MAG_X=810f,MAG_Y=478f,MAG_R=27f;
  private static final float KICK_X=762f,KICK_Y=432f,KICK_R=24f;
  private static final float MODE_X=752f,MODE_Y=494f,MODE_R=21f;
  private static final float AUTO_X=928f,AUTO_Y=514f,AUTO_R=20f;
  private static final float UTILITY_X=928f,UTILITY_R=17f,UTILITY_Y0=32f,UTILITY_STEP=42f;

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

  private float scale=1,ox,oy,vx,vy;
  private float knobX=JOY_X,knobY=JOY_Y;
  private boolean joy,running,inventoryOpen;
  private long last,lastLedgerSequence=0,lastMoveRequestId=0,lastRewardSequence=0;
  private int dir=0;
  private float walkClock=0,actionClock=0,tapMarkerClock=0,tapMarkerX=0,tapMarkerY=0;
  private Action action=Action.IDLE;
  private WorldMoveTargetController.Status lastMoveStatus=WorldMoveTargetController.Status.IDLE;
  private String feedback="",pressedControl="",rewardBanner="";
  private float feedbackClock=0,rewardClock=0;
  private FeedbackTone feedbackTone=FeedbackTone.INFO;

  private final Runnable loop=new Runnable(){@Override public void run(){if(!running)return;long n=SystemClock.uptimeMillis();float dt=Math.min(.05f,(n-last)/1000f);last=n;update(dt);camera.follow(state.player().x,state.player().y);invalidate();postDelayed(this,16);}};

  public GameView(Context c){super(c);setKeepScreenOn(true);worldAdapter.snapCameraToPlayer();}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(loop);}
  public void pause(){running=false;removeCallbacks(loop);}

  private void update(float dt){
    feedbackClock=Math.max(0,feedbackClock-dt);rewardClock=Math.max(0,rewardClock-dt);tapMarkerClock=Math.max(0,tapMarkerClock-dt);
    combat.tick(dt);state.tick(dt);consumeLedger();consumeRewardNotice();monsterAi.tick(state,dt);
    if(!state.player().alive){action=Action.IDLE;interaction.cancel();combat.cancelApproach();worldAdapter.cancel();joy=false;vx=vy=0;knobX=JOY_X;knobY=JOY_Y;return;}
    if(isActing()){actionClock+=dt;if(actionClock>=duration(action)){actionClock=0;action=(joy&&(vx!=0||vy!=0))?Action.WALK:Action.IDLE;}return;}
    if(joy&&(vx!=0||vy!=0)){action=Action.WALK;state.tryMove(vx*145*dt,vy*145*dt);walkClock+=dt;interaction.cancelApproach();combat.cancelApproach();return;}
    if(interaction.approaching()){updateNpcInteraction(dt);return;}
    if(combat.approaching()){updateCombatApproach(dt);return;}
    WorldMoveTargetController.Snapshot move=moveTarget.snapshot();
    if(move.status==WorldMoveTargetController.Status.MOVING){
      float bx=state.player().x,by=state.player().y;
      move=worldAdapter.tickNavigation(dt).movement;
      float dx=state.player().x-bx,dy=state.player().y-by;
      if(dx!=0||dy!=0){setFacingDirection(dx,dy);action=Action.WALK;walkClock+=dt;}else action=Action.IDLE;
      consumeMoveOutcome(move);return;
    }
    consumeMoveOutcome(move);action=Action.IDLE;
  }

  private void consumeMoveOutcome(WorldMoveTargetController.Snapshot move){if(move==null)return;if(move.requestId!=lastMoveRequestId){lastMoveRequestId=move.requestId;lastMoveStatus=WorldMoveTargetController.Status.IDLE;}if(move.status==lastMoveStatus)return;lastMoveStatus=move.status;if(move.status==WorldMoveTargetController.Status.REACHED)showFeedback("이동 완료",FeedbackTone.INFO);else if(move.status==WorldMoveTargetController.Status.BLOCKED)showFeedback("이동할 수 없는 위치입니다",FeedbackTone.WARN);}
  private void consumeLedger(){List<CombatLedger.Event> events=state.ledger().snapshot();for(CombatLedger.Event e:events){if(e.sequence<=lastLedgerSequence)continue;lastLedgerSequence=e.sequence;switch(e.type){case MONSTER_DEFEATED:showFeedback("몬스터 격파",FeedbackTone.INFO);break;case PLAYER_HIT:showFeedback("-"+e.amount+" HP",FeedbackTone.WARN);break;case PLAYER_DEFEATED:showFeedback("행동 불능",FeedbackTone.WARN);break;case PLAYER_REVIVED:showFeedback("부활",FeedbackTone.INFO);break;default:break;}}}
  private void consumeRewardNotice(){
    RpgInventoryPresentation.RewardNotice notice=rpgPresentation.latestRewardNotice(state.rpg());
    if(notice==null||notice.combatSequence<=lastRewardSequence)return;
    lastRewardSequence=notice.combatSequence;
    if(notice.status!=RpgProgressionState.RewardStatus.RESOLVED){showFeedback("보상 확인 필요",FeedbackTone.WARN);return;}
    if(!notice.autoLootedItems.isEmpty()){
      Map.Entry<String,Integer> first=null;for(Map.Entry<String,Integer> e:notice.autoLootedItems.entrySet()){first=e;break;}
      if(first!=null){int visibleQty=inventoryQuantity(first.getKey());if(visibleQty<first.getValue()){showFeedback("보상 지급 상태를 확인할 수 없습니다",FeedbackTone.WARN);return;}String name=itemDisplayName(first.getKey());int extra=Math.max(0,notice.autoLootedItems.size()-1);showReward("자동루팅 · "+name+" x"+first.getValue()+(extra>0?" 외 "+extra+"종":""));return;}
    }
    if(notice.exp!=null){showReward("보상 처리 · EXP +"+notice.exp);return;}showReward("보상 처리 완료");
  }
  private int inventoryQuantity(String itemId){for(RpgInventoryPresentation.ItemRow row:rpgPresentation.inventoryRows(state.rpg()))if(row.itemId.equals(itemId))return row.quantity;return 0;}
  private String itemDisplayName(String itemId){for(RpgInventoryPresentation.ItemRow row:rpgPresentation.inventoryRows(state.rpg()))if(row.itemId.equals(itemId))return row.name;return itemId;}

  private void updateNpcInteraction(float dt){InteractionController.TickResult result=interaction.tick(state,dt);vx=interaction.moveX();vy=interaction.moveY();if(vx!=0||vy!=0)setFacingDirection(vx,vy);if(result==InteractionController.TickResult.WALKING){action=Action.WALK;walkClock+=dt;return;}action=Action.IDLE;String message=interaction.consumeFeedback();if(message!=null)showFeedback(message,FeedbackTone.INFO);}
  private void updateCombatApproach(float dt){RuntimeState.Monster approach=combat.approachTarget();if(approach==null||!approach.alive){combat.cancelApproach();return;}float d=state.distanceTo(approach),range=combat.intentRange();if(d<=range){CombatController.Intent ready=combat.consumeReadyIntent();action=Action.IDLE;if(ready==CombatController.Intent.ATTACK)attack();else if(ready==CombatController.Intent.CAST)cast();else if(ready==CombatController.Intent.SKILL)skill();else if(ready==CombatController.Intent.KICK)kick();return;}float dx=approach.x-state.player().x,dy=approach.y-state.player().y;setAutoDirection(dx,dy);action=Action.WALK;boolean moved=state.tryMove(vx*105*dt,vy*105*dt);walkClock+=dt;if(!moved){combat.cancelApproach();action=Action.IDLE;showFeedback("사거리 접근 실패",FeedbackTone.WARN);}}
  private void beginCombatApproach(CombatController.Intent intent){worldAdapter.cancelForAction();if(combat.beginApproach(intent))showFeedback("타깃으로 이동",FeedbackTone.INFO);}

  private void setFacingDirection(float dx,float dy){float sx=dx>=0?1f:-1f,sy=dy>=0?1f:-1f;if(sx<0&&sy>0)dir=0;else if(sx>0&&sy>0)dir=1;else if(sx<0)dir=2;else dir=3;}
  private void setAutoDirection(float dx,float dy){float sx=dx>=0?1f:-1f,sy=dy>=0?1f:-1f;vx=.707f*sx;vy=.707f*sy;setFacingDirection(dx,dy);}
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
  protected void onDraw(Canvas c){
    c.drawColor(Color.BLACK);c.save();c.translate(ox,oy);c.scale(scale,scale);
    drawWorld(c);
    c.save();c.translate(-camera.cameraX(),-camera.cameraY());drawTapMarker(c);drawNpcs(c);drawMonsters(c);drawCharacter(c);c.restore();
    drawHud(c);drawFeedbackBanners(c);drawInventory(c);drawDialogue(c);drawDeath(c);c.restore();
  }

  /** Renderer-built Milles world. Legacy captured screenshot remains reference-only and is never stretched into runtime. */
  private void drawWorld(Canvas c){p.setColor(0xff101511);c.drawRect(0,0,W,H,p);mapRenderer.draw(c,worldAdapter);}
  private void drawTapMarker(Canvas c){if(tapMarkerClock<=0)return;float q=Math.max(0f,Math.min(1f,tapMarkerClock/.7f));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(0xD8FFD86B);c.drawCircle(tapMarkerX,tapMarkerY,8f+8f*(1f-q),p);p.setStyle(Paint.Style.FILL);p.setColor(0x66FFD86B);c.drawCircle(tapMarkerX,tapMarkerY,3.5f,p);}
  private void drawNpcs(Canvas c){for(RuntimeState.Npc n:state.npcs()){p.setColor(0x66000000);c.drawOval(new RectF(n.x-11,n.y-3,n.x+11,n.y+4),p);p.setColor(0xffcab58b);c.drawCircle(n.x,n.y-39,7,p);p.setColor(0xff6c5946);c.drawRect(n.x-7,n.y-31,n.x+7,n.y-11,p);p.setColor(0xffb58f58);c.drawRect(n.x-5,n.y-12,n.x-1,n.y-3,p);c.drawRect(n.x+1,n.y-12,n.x+5,n.y-3,p);p.setTextSize(9);p.setColor(0xfff3e1ae);float tw=p.measureText(n.name);c.drawText(n.name,n.x-tw/2,n.y-52,p);if(interaction.approachNpc()==n){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xfff0d17a);c.drawCircle(n.x,n.y-24,19,p);p.setStyle(Paint.Style.FILL);}}}
  private void drawMonsters(Canvas c){RuntimeState.Monster selected=combat.target();for(RuntimeState.Monster m:state.monsters()){if(!m.alive)continue;p.setColor(0x66000000);c.drawOval(new RectF(m.x-15,m.y-4,m.x+15,m.y+5),p);p.setColor(m.hitFlash>0?0xffd9e8d7:0xff6a7c69);c.drawOval(new RectF(m.x-13,m.y-31,m.x+13,m.y-5),p);p.setColor(0xffd8c27b);c.drawCircle(m.x-5,m.y-20,2,p);c.drawCircle(m.x+5,m.y-20,2,p);if(m.attackPrimed){float q=1f-Math.min(1f,m.attackWindup/.24f);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2+2*q);p.setColor(0xaaff7755);c.drawCircle(m.x,m.y-19,18+8*q,p);p.setStyle(Paint.Style.FILL);}bar(c,m.x-18,m.y-42,m.x+18,m.y-37,0xffd63442,m.hp/(float)m.maxHp);if(m.damagePopupClock>0){p.setTextSize(12);p.setColor(0xffffdc72);String d="-"+m.lastDamage;float tw=p.measureText(d);c.drawText(d,m.x-tw/2,m.y-50-(.65f-m.damagePopupClock)*20,p);}if(selected==m){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.3f);p.setColor(0xffffd86b);c.drawOval(new RectF(m.x-21,m.y-9,m.x+21,m.y+9),p);p.setStyle(Paint.Style.FILL);}}}
  private CharacterRenderer.Direction characterDirection(){switch(dir){case 1:return CharacterRenderer.Direction.SE;case 2:return CharacterRenderer.Direction.NW;case 3:return CharacterRenderer.Direction.NE;default:return CharacterRenderer.Direction.SW;}}
  private CharacterRenderer.State characterState(){if(!state.player().alive)return CharacterRenderer.State.DEAD;if(state.player().hitFlash>0)return CharacterRenderer.State.HIT;switch(action){case WALK:return CharacterRenderer.State.WALK;case CAST:return CharacterRenderer.State.CAST;case SKILL:case KICK:return CharacterRenderer.State.SKILL;case SWING:case THRUST:case THROW:case PUNCH:return CharacterRenderer.State.ATTACK;default:return CharacterRenderer.State.IDLE;}}
  private CharacterRenderer.EffectFamily characterEffectFamily(){if(state.player().hitFlash>0)return CharacterRenderer.EffectFamily.HIT;switch(action){case CAST:return CharacterRenderer.EffectFamily.CAST;case THROW:return CharacterRenderer.EffectFamily.THROW;case PUNCH:return CharacterRenderer.EffectFamily.PUNCH;case KICK:return CharacterRenderer.EffectFamily.KICK;case SKILL:return CharacterRenderer.EffectFamily.SKILL;default:return CharacterRenderer.EffectFamily.NONE;}}
  private void drawCharacter(Canvas c){CharacterRenderer.State presentation=characterState();CharacterVisualBinding visuals=CharacterVisualBinding.from(state.rpg());float stateDuration=isActing()?duration(action):1f;characterRenderer.draw(c,new CharacterRenderer.Pose(state.player().x,state.player().y,characterDirection(),presentation,walkClock,actionClock,stateDuration,state.player().hitFlash>0,visuals.equipmentVisualRef(),visuals.weaponVisualRef(),CharacterRenderer.ASSET_STATUS,characterEffectFamily()));}

  private void panel(Canvas c,float l,float t,float r,float b){p.setStyle(Paint.Style.FILL);p.setColor(0x8A11161C);c.drawRoundRect(new RectF(l,t,r,b),11,11,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1f);p.setColor(0x66D0B477);c.drawRoundRect(new RectF(l+.5f,t+.5f,r-.5f,b-.5f),11,11,p);p.setStyle(Paint.Style.FILL);}
  private void modalPanel(Canvas c,float l,float t,float r,float b){p.setStyle(Paint.Style.FILL);p.setColor(0xED11151A);c.drawRoundRect(new RectF(l,t,r,b),14,14,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.4f);p.setColor(0xAACFB171);c.drawRoundRect(new RectF(l+.5f,t+.5f,r-.5f,b-.5f),14,14,p);p.setStyle(Paint.Style.FILL);}
  private void text(Canvas c,String s,float x,float y,float size){p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));p.setTextSize(size);p.setColor(0xfff2ead8);c.drawText(s,x,y,p);p.setTypeface(Typeface.DEFAULT);}
  private void mutedText(Canvas c,String s,float x,float y,float size){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);p.setColor(0xffaaa496);c.drawText(s,x,y,p);}
  private void bar(Canvas c,float l,float t,float r,float b,int color,float ratio){ratio=Math.max(0,Math.min(1,ratio));p.setColor(0xA9181A1E);c.drawRoundRect(new RectF(l,t,r,b),(b-t)*.5f,(b-t)*.5f,p);p.setColor(color);c.drawRoundRect(new RectF(l,t,l+(r-l)*ratio,b),(b-t)*.5f,(b-t)*.5f,p);}

  private void drawHud(Canvas c){drawParty(c);drawQuest(c);drawTarget(c);drawMinimap(c);drawUtilityRail(c);drawChat(c);drawJoystick(c);drawPlayerStatus(c);drawCombatCluster(c);}
  private void drawParty(Canvas c){panel(c,16,14,142,92);mutedText(c,"PARTY",26,30,7.5f);String[] n={"조춘찬","수수료좀챙","이루","별빛영혼"};for(int i=0;i<4;i++){float y=45+i*11;text(c,n[i],26,y,8.2f);bar(c,88,y-6,132,y-3,0xffd94a57,1f);}}
  private void drawQuest(Canvas c){panel(c,158,14,338,58);mutedText(c,"QUEST",170,29,7.5f);text(c,"밀레스의 첫걸음",170,45,9.5f);mutedText(c,"0 / 4",302,45,8f);}
  private void drawTarget(Canvas c){RuntimeState.Monster selected=combat.target();if(selected==null){panel(c,394,14,566,46);mutedText(c,"TARGET  ·  타깃 없음",424,34,8.5f);return;}panel(c,372,14,588,58);mutedText(c,"TARGET",384,29,7.5f);text(c,selected.name,430,30,9.5f);bar(c,384,40,576,49,0xffda4151,selected.hp/(float)selected.maxHp);}
  private void drawMinimap(Canvas c){panel(c,760,14,894,104);mutedText(c,"MILLES",772,30,7.5f);p.setColor(0x8A090C0F);c.drawRoundRect(new RectF(770,37,884,88),7,7,p);float nx=(state.player().x-WorldDef.MIN_X)/Math.max(1f,WorldDef.MAX_X-WorldDef.MIN_X),ny=(state.player().y-WorldDef.MIN_Y)/Math.max(1f,WorldDef.MAX_Y-WorldDef.MIN_Y);float px=773+Math.max(0,Math.min(1,nx))*108,py=40+Math.max(0,Math.min(1,ny))*45;p.setColor(0xffffdf74);c.drawCircle(px,py,3f,p);mutedText(c,"X"+(int)state.player().x+"  Y"+(int)state.player().y,805,99,7f);}
  private void drawUtilityRail(Canvas c){String[] labels={"≡","BAG","Q","G","W","⚙"};for(int i=0;i<labels.length;i++){float cy=UTILITY_Y0+i*UTILITY_STEP;boolean active=i==1&&inventoryOpen;p.setColor(active?0xD45A492E:0xA815191E);c.drawCircle(UTILITY_X,cy,UTILITY_R,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(active?2f:1.1f);p.setColor(active?0xffffd77a:0x88c1a76e);c.drawCircle(UTILITY_X,cy,UTILITY_R,p);p.setStyle(Paint.Style.FILL);p.setTextSize(labels[i].length()>1?6.8f:11f);p.setColor(active?0xffffedc2:0xffe7deca);float tw=p.measureText(labels[i]);c.drawText(labels[i],UTILITY_X-tw/2,cy+3,p);}}
  private void drawChat(Canvas c){RuntimeMetrics metrics=state.metrics();p.setColor(0x6511161B);c.drawRoundRect(new RectF(16,326,248,371),9,9,p);mutedText(c,"[일반] 밀레스",27,342,7.5f);text(c,"격파 "+metrics.monsterDefeats()+"   피격 "+metrics.playerHits()+"   DMG "+metrics.damageDealt(),27,357,8f);mutedText(c,"NPC · 전투 · 보상",27,367,7f);}
  private void drawJoystick(Canvas c){p.setColor(joy?0x523D4652:0x2A2B3038);c.drawCircle(JOY_X,JOY_Y,JOY_R,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(joy?2.2f:1.2f);p.setColor(joy?0xC8DBC386:0x697F7562);c.drawCircle(JOY_X,JOY_Y,JOY_R,p);p.setStrokeWidth(1);p.setColor(0x447F7562);c.drawCircle(JOY_X,JOY_Y,36,p);p.setStyle(Paint.Style.FILL);p.setColor(joy?0xD7C7A86B:0x9A766951);c.drawCircle(knobX,knobY,joy?24:22,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xDCEAD9B0);c.drawCircle(knobX,knobY,joy?24:22,p);p.setStyle(Paint.Style.FILL);}
  private void drawPlayerStatus(Canvas c){panel(c,340,476,608,530);Integer level=state.rpg().normalLevel();text(c,"Lv "+(level==null?"?":level),352,493,10);mutedText(c,"평민",352,509,7.8f);bar(c,397,483,594,492,0xffd94050,state.player().hp/(float)state.player().maxHp);bar(c,397,499,594,508,0xff337fd6,state.player().mp/(float)state.player().maxMp);bar(c,397,516,594,522,0xff58aa62,.35f);mutedText(c,"HP",376,491,7f);mutedText(c,"MP",376,507,7f);mutedText(c,"EXP",372,522,7f);}
  private void drawCombatCluster(Canvas c){boolean target=combat.hasUsableTarget();boolean atkEnabled=target&&combat.attackReady()&&state.player().alive;boolean magEnabled=target&&combat.castReady()&&state.player().alive&&state.player().mp>=SkillDef.CAST_PROTO.mpCost;boolean skillEnabled=target&&combat.skillReady()&&state.player().alive&&state.player().mp>=SkillDef.SKILL_PROTO.mpCost;boolean kickEnabled=target&&combat.kickReady()&&state.player().alive;actionButton(c,ATK_X,ATK_Y,ATK_R,"ATK",atkEnabled,attacking(),"ATK".equals(pressedControl),combat.attackCooldown(),combat.attackDef().cooldown,true);actionButton(c,SKILL_X,SKILL_Y,SKILL_R,"SKILL",skillEnabled,action==Action.SKILL,"SKILL".equals(pressedControl),combat.skillCooldown(),SkillDef.SKILL_PROTO.cooldown,false);actionButton(c,MAG_X,MAG_Y,MAG_R,"MAG",magEnabled,action==Action.CAST,"MAG".equals(pressedControl),combat.castCooldown(),SkillDef.CAST_PROTO.cooldown,false);actionButton(c,KICK_X,KICK_Y,KICK_R,"KICK",kickEnabled,action==Action.KICK,"KICK".equals(pressedControl),combat.kickCooldown(),SkillDef.KICK_PROTO.cooldown,false);actionButton(c,MODE_X,MODE_Y,MODE_R,"MODE",true,false,"MODE".equals(pressedControl),0,0,false);actionButton(c,AUTO_X,AUTO_Y,AUTO_R,"AUTO",false,false,"AUTO".equals(pressedControl),0,0,false);mutedText(c,combat.attackDef().label,846,527,7f);}
  private void actionButton(Canvas c,float x,float y,float r,String label,boolean enabled,boolean selected,boolean pressed,float cooldown,float total,boolean primary){float rr=pressed?r-2:r;p.setColor(0x251F2227);c.drawCircle(x,y,r+4,p);p.setColor(enabled?(primary?0xE05D3B29:0xD0202429):0xAA111317);c.drawCircle(x,y,rr,p);if(selected){p.setColor(0x35FFE19A);c.drawCircle(x,y,rr+4,p);}p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(primary?2.4f:1.5f);p.setColor(enabled?(selected?0xffffe69d:0xffc8aa6c):0x605f5a50);c.drawCircle(x,y,rr,p);p.setStyle(Paint.Style.FILL);p.setTextSize(primary?10.5f:(label.length()>4?6.8f:8.2f));p.setColor(enabled?0xfffff0cf:0xff79756d);float tw=p.measureText(label);c.drawText(label,x-tw/2,y+3,p);cooldown(c,x,y,rr,cooldown,total);if(!enabled&&"AUTO".equals(label)){p.setTextSize(6.3f);p.setColor(0xff918c81);c.drawText("LOCK",x-8,y+12,p);}}
  private void cooldown(Canvas c,float x,float y,float r,float remaining,float total){if(remaining<=0||total<=0)return;float ratio=Math.min(1f,remaining/total);p.setColor(0xA9000000);c.drawArc(new RectF(x-r,y-r,x+r,y+r),-90,360*ratio,true,p);p.setTextSize(8);p.setColor(Color.WHITE);String s=String.format(java.util.Locale.US,"%.1f",remaining);float tw=p.measureText(s);c.drawText(s,x-tw/2,y+3,p);}

  private void drawFeedbackBanners(Canvas c){if(rewardClock>0){p.setColor(0xD02B2517);c.drawRoundRect(new RectF(346,74,614,106),16,16,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);p.setColor(0xBADBB768);c.drawRoundRect(new RectF(346,74,614,106),16,16,p);p.setStyle(Paint.Style.FILL);p.setTextSize(10);p.setColor(0xffffe5a3);float tw=p.measureText(rewardBanner);c.drawText(rewardBanner,480-tw/2,95,p);}if(feedbackClock>0){int fill=feedbackTone==FeedbackTone.WARN?0xD04A2226:feedbackTone==FeedbackTone.REWARD?0xD03C321E:0xCC161A20;p.setColor(fill);p.setTextSize(9.2f);float width=Math.min(340,Math.max(146,p.measureText(feedback)+38));c.drawRoundRect(new RectF(480-width/2,111,480+width/2,139),14,14,p);p.setColor(feedbackTone==FeedbackTone.WARN?0xffffb4ad:0xffeee5d3);float ft=p.measureText(feedback);c.drawText(feedback,480-ft/2,130,p);}}

  private void drawInventory(Canvas c){if(!inventoryOpen)return;p.setColor(0x62000000);c.drawRect(0,0,W,H,p);modalPanel(c,588,92,904,444);text(c,"INVENTORY",608,118,12.5f);Integer level=state.rpg().normalLevel();mutedText(c,"평민 · Lv "+(level==null?"?":level),608,135,8f);p.setColor(0xB024262A);c.drawCircle(879,113,15,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.1f);p.setColor(0x99C8A96A);c.drawCircle(879,113,15,p);p.setStyle(Paint.Style.FILL);text(c,"×",874,118,14);List<RpgInventoryPresentation.ItemRow> rows=rpgPresentation.inventoryRows(state.rpg());String selectedId=rpgInteraction.selectedInventoryItemId();if(rows.isEmpty())mutedText(c,"보유 아이템 없음",608,171,10);else{int limit=Math.min(7,rows.size());for(int i=0;i<limit;i++){RpgInventoryPresentation.ItemRow row=rows.get(i);float y=169+i*27;boolean selected=row.itemId.equals(selectedId);p.setColor(selected?0x665D4A28:0x5517191D);c.drawRoundRect(new RectF(602,y-16,890,y+6),7,7,p);if(selected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);p.setColor(0xBBD2AF67);c.drawRoundRect(new RectF(602,y-16,890,y+6),7,7,p);p.setStyle(Paint.Style.FILL);}text(c,row.name+"  x"+row.quantity+(row.equipped?"  [장착]":""),614,y-2,9);mutedText(c,row.itemId,790,y-2,7f);}if(rows.size()>limit)mutedText(c,"외 "+(rows.size()-limit)+"개",610,169+limit*27,8);}RpgInventoryPresentation.ItemRow selectedRow=null;if(selectedId!=null)for(RpgInventoryPresentation.ItemRow row:rows)if(selectedId.equals(row.itemId)){selectedRow=row;break;}p.setColor(0x5517191D);c.drawRoundRect(new RectF(602,356,890,426),8,8,p);if(selectedRow!=null){text(c,selectedRow.name,614,375,10);mutedText(c,rpgPresentation.requirementLabel(selectedRow),614,391,8);String element=rpgPresentation.elementLabel(selectedRow);if(!element.isEmpty())mutedText(c,element,614,406,7.5f);}else mutedText(c,"아이템을 선택하세요",614,386,9);p.setColor(0xD05B4727);c.drawRoundRect(new RectF(810,389,880,419),9,9,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.1f);p.setColor(0xCCD3B16D);c.drawRoundRect(new RectF(810,389,880,419),9,9,p);p.setStyle(Paint.Style.FILL);text(c,"장착",833,409,9.5f);RpgInventoryPresentation.RewardNotice notice=rpgPresentation.latestRewardNotice(state.rpg());if(notice!=null)mutedText(c,"최근 보상 · "+(notice.status==RpgProgressionState.RewardStatus.RESOLVED?"처리됨":"확인 필요"),608,438,7.3f);}
  private String equipResultLabel(RpgProgressionState.EquipResult result){switch(result){case EQUIPPED:return "장착 완료";case REQUIREMENT_NOT_MET:return "장착 불가 · 조건 미충족";case REQUIREMENT_PENDING:return "장착 보류 · 조건 확인 필요";case NOT_EQUIPPABLE:return "장착할 수 없는 아이템";case UNKNOWN_ITEM:return "알 수 없는 아이템";case ITEM_NOT_OWNED:default:return "보유하지 않은 아이템";}}
  private boolean handleInventoryTouch(float x,float y){if(!inventoryOpen)return false;if(dist(x,y,879,113)<=24){inventoryOpen=false;showFeedback("인벤토리 닫힘",FeedbackTone.INFO);return true;}List<RpgInventoryPresentation.ItemRow> rows=rpgPresentation.inventoryRows(state.rpg());int limit=Math.min(7,rows.size());if(x>=602&&x<=890){for(int i=0;i<limit;i++){float ry=169+i*27;if(y>=ry-18&&y<=ry+8){RpgInventoryPresentation.ItemRow row=rows.get(i);if(rpgInteraction.selectInventoryItem(state.rpg(),row.itemId))showFeedback(row.name+" 선택",FeedbackTone.INFO);return true;}}}if(x>=810&&x<=880&&y>=389&&y<=419){RpgProgressionState.EquipResult result=rpgInteraction.equipSelectedDetailed(state.rpg());showFeedback(equipResultLabel(result),result==RpgProgressionState.EquipResult.EQUIPPED?FeedbackTone.INFO:FeedbackTone.WARN);return true;}return inside(x,y,588,92,904,444);}

  private void drawDialogue(Canvas c){RuntimeState.Npc dialogNpc=interaction.dialogNpc();if(dialogNpc==null)return;p.setColor(0x74000000);c.drawRect(0,0,W,H,p);modalPanel(c,190,326,770,462);p.setColor(0xC95A472A);c.drawRoundRect(new RectF(210,342,338,371),8,8,p);text(c,dialogNpc.name,226,361,11.5f);drawWrappedText(c,dialogNpc.dialogue,218,394,510,11,18);mutedText(c,"화면을 터치하면 닫기",616,446,8);p.setColor(0xB024262A);c.drawCircle(744,347,14,p);text(c,"×",739,352,14);}
  private void drawDeath(Canvas c){if(state.player().alive)return;p.setColor(0xB6000000);c.drawRect(0,0,W,H,p);modalPanel(c,330,205,630,335);text(c,"행동 불능",428,242,18);mutedText(c,"프로토타입 부활",432,270,9.5f);p.setColor(0xD05B4727);c.drawRoundRect(new RectF(405,286,555,320),10,10,p);text(c,"화면 중앙 터치",433,308,10.5f);}
  private void drawWrappedText(Canvas c,String s,float x,float y,float maxWidth,float size,float lineHeight){p.setTextSize(size);p.setColor(0xffeee4cf);String[] words=s.split(" ");String line="";float yy=y;for(String word:words){String test=line.length()==0?word:line+" "+word;if(p.measureText(test)>maxWidth&&line.length()>0){c.drawText(line,x,yy,p);yy+=lineHeight;line=word;}else line=test;}if(line.length()>0)c.drawText(line,x,yy,p);}

  static boolean blocksWorldTapForHud(float x,float y,boolean targetVisible){
    if(inside(x,y,16,14,142,92)||inside(x,y,158,14,338,58)||inside(x,y,targetVisible?372:394,14,targetVisible?588:566,targetVisible?58:46)||inside(x,y,760,14,894,104)||inside(x,y,16,326,248,371)||inside(x,y,340,476,608,530))return true;
    if(circleHit(x,y,JOY_X,JOY_Y,JOY_R))return true;
    for(int i=0;i<6;i++)if(circleHit(x,y,UTILITY_X,UTILITY_Y0+i*UTILITY_STEP,UTILITY_R+2))return true;
    return circleHit(x,y,ATK_X,ATK_Y,ATK_R+4)||circleHit(x,y,SKILL_X,SKILL_Y,SKILL_R+4)||circleHit(x,y,MAG_X,MAG_Y,MAG_R+4)||circleHit(x,y,KICK_X,KICK_Y,KICK_R+4)||circleHit(x,y,MODE_X,MODE_Y,MODE_R+4)||circleHit(x,y,AUTO_X,AUTO_Y,AUTO_R+4);
  }
  private boolean isHudSurface(float x,float y){return blocksWorldTapForHud(x,y,combat.target()!=null);}
  private void requestGroundMove(float x,float y){WorldCameraTransform.Point wp=worldAdapter.screenToWorld(x,y);WorldMoveTargetController.Snapshot move=worldAdapter.requestGroundScreenTap(x,y);lastMoveRequestId=move.requestId;lastMoveStatus=WorldMoveTargetController.Status.IDLE;if(move.status==WorldMoveTargetController.Status.MOVING||move.status==WorldMoveTargetController.Status.REACHED){tapMarkerX=wp.x;tapMarkerY=wp.y;tapMarkerClock=.7f;showFeedback(move.replacedRequestId>0?"이동 목표 변경":"이동 시작",FeedbackTone.INFO);}consumeMoveOutcome(move);}

  public boolean onTouchEvent(MotionEvent e){float x=(e.getX()-ox)/scale,y=(e.getY()-oy)/scale;switch(e.getActionMasked()){
    case MotionEvent.ACTION_DOWN:
      if(!state.player().alive){if(dist(x,y,480,270)<=180){state.revivePlayer();combat.clearTarget();interaction.cancel();worldAdapter.cancel();worldAdapter.snapCameraToPlayer();action=Action.IDLE;}return true;}
      if(interaction.dialogOpen()){interaction.dismissDialog();worldAdapter.cancelForAction();showFeedback("대화 종료",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X,UTILITY_Y0+UTILITY_STEP,UTILITY_R+4)){inventoryOpen=!inventoryOpen;worldAdapter.cancelForAction();showFeedback(inventoryOpen?"인벤토리 열림":"인벤토리 닫힘",FeedbackTone.INFO);return true;}
      if(handleInventoryTouch(x,y)){worldAdapter.cancelForAction();return true;}
      if(inventoryOpen){inventoryOpen=false;worldAdapter.cancelForAction();showFeedback("인벤토리 닫힘",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,JOY_X,JOY_Y,JOY_R)){joy=true;pressedControl="JOY";worldAdapter.cancelForDirectInput();interaction.cancelApproach();combat.cancelApproach();stick(x,y);return true;}
      if(circleHit(x,y,MAG_X,MAG_Y,MAG_R+4)){pressedControl="MAG";cast();return true;}
      if(circleHit(x,y,MODE_X,MODE_Y,MODE_R+4)){pressedControl="MODE";worldAdapter.cancelForAction();combat.cycleAttackMode();showFeedback(combat.attackDef().label,FeedbackTone.INFO);return true;}
      if(circleHit(x,y,SKILL_X,SKILL_Y,SKILL_R+4)){pressedControl="SKILL";skill();return true;}
      if(circleHit(x,y,KICK_X,KICK_Y,KICK_R+4)){pressedControl="KICK";kick();return true;}
      if(circleHit(x,y,ATK_X,ATK_Y,ATK_R+4)){pressedControl="ATK";attack();return true;}
      if(circleHit(x,y,AUTO_X,AUTO_Y,AUTO_R+4)){pressedControl="AUTO";showFeedback("AUTO 준비 중",FeedbackTone.WARN);return true;}
      if(isHudSurface(x,y))return true;
      WorldCameraTransform.Point wp=worldAdapter.screenToWorld(x,y);
      RuntimeState.Npc npc=state.hitNpc(wp.x,wp.y,34f);if(npc!=null){worldAdapter.cancelForAction();combat.cancelApproach();interaction.request(state,npc);showFeedback("NPC 접근 · "+npc.name,FeedbackTone.INFO);return true;}
      RuntimeState.Monster monster=state.hitMonster(wp.x,wp.y,34f);if(monster!=null){worldAdapter.cancelForAction();combat.selectTarget(monster);interaction.cancelApproach();showFeedback("타깃 선택 · "+monster.name,FeedbackTone.INFO);return true;}
      requestGroundMove(x,y);return true;
    case MotionEvent.ACTION_MOVE:if(joy)stick(x,y);return true;
    case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:joy=false;pressedControl="";knobX=JOY_X;knobY=JOY_Y;vx=vy=0;return true;
  }return true;}
  private void stick(float x,float y){if(isActing()||!state.player().alive)return;float dx=x-JOY_X,dy=y-JOY_Y,len=(float)Math.sqrt(dx*dx+dy*dy);if(len>JOY_R){dx=dx/len*JOY_R;dy=dy/len*JOY_R;len=JOY_R;}knobX=JOY_X+dx;knobY=JOY_Y+dy;if(len<8){vx=vy=0;return;}float nx=dx/len,ny=dy/len;if(Math.abs(nx)>Math.abs(ny)){if(nx>0){vx=.707f;vy=.707f;dir=1;}else{vx=-.707f;vy=-.707f;dir=2;}}else{if(ny>0){vx=-.707f;vy=.707f;dir=0;}else{vx=.707f;vy=-.707f;dir=3;}}}
  private static boolean inside(float x,float y,float l,float t,float r,float b){return x>=l&&x<=r&&y>=t&&y<=b;}
  private static boolean circleHit(float x,float y,float cx,float cy,float r){float dx=x-cx,dy=y-cy;return dx*dx+dy*dy<=r*r;}
  private float dist(float a,float b,float c,float d){float x=a-c,y=b-d;return(float)Math.sqrt(x*x+y*y);}
}
