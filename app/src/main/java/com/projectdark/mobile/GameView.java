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

/** PROJECT DARK v0.73 - device-playtest UX correction on canonical World runtime. */
public final class GameView extends View {
  private static final float W=960f,H=540f;
  private enum Action { IDLE,WALK,CAST,SWING,THRUST,THROW,PUNCH,SKILL,KICK }
  private enum FeedbackTone { INFO,WARN,REWARD }

  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RuntimeState state=new RuntimeState();
  private final WorldRuntimeAdapter worldRuntime=new WorldRuntimeAdapter(state,W,H);
  private final WorldCameraTransform camera=worldRuntime.camera();
  private final WorldMoveTargetController moveTarget=worldRuntime.movement();
  private final AdaptedMillesMapRenderer mapRenderer=new AdaptedMillesMapRenderer();
  private final CombatController combat=new CombatController();
  private final MonsterAIController monsterAi=new MonsterAIController();
  private final InteractionController interaction=new InteractionController();
  private final CharacterRenderer characterRenderer=new CharacterRenderer();
  private final RpgInventoryPresentation rpgPresentation=new RpgInventoryPresentation();
  private final RpgInteractionController rpgInteraction=new RpgInteractionController();

  private float scale=1,ox,oy,vx,vy;
  private final float jx=92,jy=444,jr=62;
  private float knobX=jx,knobY=jy;
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

  public GameView(Context c){super(c);setKeepScreenOn(true);worldRuntime.snapCameraToPlayer();}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(loop);}
  public void pause(){running=false;removeCallbacks(loop);}

  private void update(float dt){
    feedbackClock=Math.max(0,feedbackClock-dt);rewardClock=Math.max(0,rewardClock-dt);tapMarkerClock=Math.max(0,tapMarkerClock-dt);
    combat.tick(dt);state.tick(dt);consumeLedger();consumeRewardNotice();monsterAi.tick(state,dt);
    if(!state.player().alive){action=Action.IDLE;interaction.cancel();combat.cancelApproach();worldRuntime.cancel();joy=false;vx=vy=0;knobX=jx;knobY=jy;return;}
    if(isActing()){actionClock+=dt;if(actionClock>=duration(action)){actionClock=0;action=(joy&&(vx!=0||vy!=0))?Action.WALK:Action.IDLE;}return;}
    if(joy&&(vx!=0||vy!=0)){action=Action.WALK;state.tryMove(vx*145*dt,vy*145*dt);walkClock+=dt;interaction.cancelApproach();combat.cancelApproach();return;}
    if(interaction.approaching()){updateNpcInteraction(dt);return;}
    if(combat.approaching()){updateCombatApproach(dt);return;}
    WorldMoveTargetController.Snapshot move=moveTarget.snapshot();
    if(move.status==WorldMoveTargetController.Status.MOVING){float bx=state.player().x,by=state.player().y;move=worldRuntime.tickNavigation(dt).movement;float dx=state.player().x-bx,dy=state.player().y-by;if(dx!=0||dy!=0){setFacingDirection(dx,dy);action=Action.WALK;walkClock+=dt;}else action=Action.IDLE;consumeMoveOutcome(move);return;}
    consumeMoveOutcome(move);action=Action.IDLE;
  }

  private void consumeMoveOutcome(WorldMoveTargetController.Snapshot move){if(move==null)return;if(move.requestId!=lastMoveRequestId){lastMoveRequestId=move.requestId;lastMoveStatus=WorldMoveTargetController.Status.IDLE;}if(move.status==lastMoveStatus)return;lastMoveStatus=move.status;if(move.status==WorldMoveTargetController.Status.REACHED)showFeedback("이동 완료",FeedbackTone.INFO);else if(move.status==WorldMoveTargetController.Status.BLOCKED)showFeedback("이동할 수 없는 위치입니다",FeedbackTone.WARN);}
  private void consumeLedger(){List<CombatLedger.Event> events=state.ledger().snapshot();for(CombatLedger.Event e:events){if(e.sequence<=lastLedgerSequence)continue;lastLedgerSequence=e.sequence;switch(e.type){case MONSTER_DEFEATED:showFeedback("몬스터 격파",FeedbackTone.INFO);break;case PLAYER_HIT:showFeedback("-"+e.amount+" HP",FeedbackTone.WARN);break;case PLAYER_DEFEATED:showFeedback("행동 불능",FeedbackTone.WARN);break;case PLAYER_REVIVED:showFeedback("부활",FeedbackTone.INFO);break;default:break;}}}
  private void consumeRewardNotice(){RpgInventoryPresentation.RewardNotice notice=rpgPresentation.latestRewardNotice(state.rpg());if(notice==null||notice.combatSequence<=lastRewardSequence)return;lastRewardSequence=notice.combatSequence;if(notice.status!=RpgProgressionState.RewardStatus.RESOLVED){showReward("보상 확인 필요");return;}if(!notice.autoLootedItems.isEmpty()){Map.Entry<String,Integer> first=notice.autoLootedItems.entrySet().iterator().next();int extra=Math.max(0,notice.autoLootedItems.size()-1);showReward("자동루팅 · "+itemDisplayName(first.getKey())+" x"+first.getValue()+(extra>0?" 외 "+extra+"종":""));return;}if(notice.exp!=null){showReward("보상 처리 · EXP +"+notice.exp);return;}showReward("보상 처리 완료");}
  private String itemDisplayName(String id){for(RpgInventoryPresentation.ItemRow row:rpgPresentation.inventoryRows(state.rpg()))if(row.itemId.equals(id))return row.name;return id;}

  private void updateNpcInteraction(float dt){InteractionController.TickResult result=interaction.tick(state,dt);vx=interaction.moveX();vy=interaction.moveY();if(vx!=0||vy!=0)setFacingDirection(vx,vy);if(result==InteractionController.TickResult.WALKING){action=Action.WALK;walkClock+=dt;return;}action=Action.IDLE;String m=interaction.consumeFeedback();if(m!=null)showFeedback(m,FeedbackTone.INFO);}
  private void updateCombatApproach(float dt){RuntimeState.Monster t=combat.approachTarget();if(t==null||!t.alive){combat.cancelApproach();return;}float d=state.distanceTo(t),range=combat.intentRange();if(d<=range){CombatController.Intent ready=combat.consumeReadyIntent();action=Action.IDLE;if(ready==CombatController.Intent.ATTACK)attack();else if(ready==CombatController.Intent.CAST)cast();else if(ready==CombatController.Intent.SKILL)skill();else if(ready==CombatController.Intent.KICK)kick();return;}float dx=t.x-state.player().x,dy=t.y-state.player().y;setAutoDirection(dx,dy);action=Action.WALK;boolean moved=state.tryMove(vx*105*dt,vy*105*dt);walkClock+=dt;if(!moved){combat.cancelApproach();action=Action.IDLE;showFeedback("사거리 접근 실패",FeedbackTone.WARN);}}
  private void beginCombatApproach(CombatController.Intent intent){worldRuntime.cancelForAction();if(combat.beginApproach(intent))showFeedback("타깃으로 이동",FeedbackTone.INFO);}

  private void setFacingDirection(float dx,float dy){float sx=dx>=0?1f:-1f,sy=dy>=0?1f:-1f;if(sx<0&&sy>0)dir=0;else if(sx>0&&sy>0)dir=1;else if(sx<0)dir=2;else dir=3;}
  private void setAutoDirection(float dx,float dy){float sx=dx>=0?1f:-1f,sy=dy>=0?1f:-1f;vx=.707f*sx;vy=.707f*sy;setFacingDirection(dx,dy);}
  private void faceTarget(){RuntimeState.Monster t=combat.target();if(t!=null&&t.alive)setFacingDirection(t.x-state.player().x,t.y-state.player().y);}
  private boolean isActing(){return action!=Action.IDLE&&action!=Action.WALK;}
  private boolean attacking(){return action==Action.SWING||action==Action.THRUST||action==Action.THROW||action==Action.PUNCH;}
  private float duration(Action a){switch(a){case CAST:return .65f;case SWING:return .45f;case THRUST:return .38f;case THROW:return .52f;case PUNCH:return .32f;case SKILL:return .50f;case KICK:return .45f;default:return 0;}}
  private void trigger(Action a){if(isActing()||!state.player().alive)return;worldRuntime.cancelForAction();interaction.cancel();combat.cancelApproach();action=a;actionClock=0;}
  private void showFeedback(String s,FeedbackTone tone){feedback=s;feedbackTone=tone;feedbackClock=1.15f;}private void showReward(String s){rewardBanner=s;rewardClock=2.4f;}
  private boolean consume(SkillDef def){if(state.player().mp<def.mpCost){showFeedback("MP가 부족합니다",FeedbackTone.WARN);return false;}state.player().mp-=def.mpCost;return true;}
  private boolean requireTarget(){if(combat.hasUsableTarget())return true;showFeedback("먼저 몬스터를 선택하세요",FeedbackTone.WARN);return false;}
  private boolean inRange(float r){return combat.inRange(state,r);}
  private void applyToTarget(SkillDef def){RuntimeState.Monster t=combat.target();if(t!=null&&t.alive&&inRange(def.range))state.damage(t,def.damage);}
  private void cast(){worldRuntime.cancelForAction();if(isActing()||!combat.castReady()||!requireTarget())return;if(!inRange(SkillDef.CAST_PROTO.range)){beginCombatApproach(CombatController.Intent.CAST);return;}if(!consume(SkillDef.CAST_PROTO))return;faceTarget();combat.commitCast();trigger(Action.CAST);applyToTarget(SkillDef.CAST_PROTO);}
  private void skill(){worldRuntime.cancelForAction();if(isActing()||!combat.skillReady()||!requireTarget())return;if(!inRange(SkillDef.SKILL_PROTO.range)){beginCombatApproach(CombatController.Intent.SKILL);return;}if(!consume(SkillDef.SKILL_PROTO))return;faceTarget();combat.commitSkill();trigger(Action.SKILL);applyToTarget(SkillDef.SKILL_PROTO);}
  private void kick(){worldRuntime.cancelForAction();if(isActing()||!combat.kickReady()||!requireTarget())return;if(!inRange(SkillDef.KICK_PROTO.range)){beginCombatApproach(CombatController.Intent.KICK);return;}faceTarget();combat.commitKick();trigger(Action.KICK);applyToTarget(SkillDef.KICK_PROTO);}
  private Action actionFor(AttackDef.Kind k){switch(k){case THRUST:return Action.THRUST;case THROW:return Action.THROW;case PUNCH:return Action.PUNCH;default:return Action.SWING;}}
  private void attack(){worldRuntime.cancelForAction();if(isActing()||!combat.attackReady()||!state.player().alive||!requireTarget())return;AttackDef def=combat.attackDef();if(!inRange(def.range)){beginCombatApproach(CombatController.Intent.ATTACK);return;}faceTarget();combat.commitAttack();trigger(actionFor(def.kind));RuntimeState.Monster t=combat.target();if(t!=null&&t.alive)state.damage(t,def.damage);}

  protected void onSizeChanged(int w,int h,int ow,int oh){scale=Math.min(w/W,h/H);ox=(w-W*scale)/2;oy=(h-H*scale)/2;}
  protected void onDraw(Canvas c){c.drawColor(0xff111319);c.save();c.translate(ox,oy);c.scale(scale,scale);mapRenderer.draw(c,worldRuntime);c.save();c.translate(-camera.cameraX(),-camera.cameraY());drawTapMarker(c);drawNpcs(c);drawMonsters(c);drawCharacter(c);c.restore();drawHud(c);drawFeedbackBanners(c);drawInventory(c);drawDialogue(c);drawDeath(c);c.restore();}

  private void drawTapMarker(Canvas c){if(tapMarkerClock<=0)return;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(0xD8FFD86B);c.drawCircle(tapMarkerX,tapMarkerY,12,p);p.setStyle(Paint.Style.FILL);p.setColor(0x66FFD86B);c.drawCircle(tapMarkerX,tapMarkerY,3.5f,p);}
  private void drawNpcs(Canvas c){for(RuntimeState.Npc n:state.npcs()){p.setColor(0x66000000);c.drawOval(new RectF(n.x-11,n.y-3,n.x+11,n.y+4),p);p.setColor(0xffcab58b);c.drawCircle(n.x,n.y-39,7,p);p.setColor(0xff6c5946);c.drawRect(n.x-7,n.y-31,n.x+7,n.y-11,p);p.setColor(0xffb58f58);c.drawRect(n.x-5,n.y-12,n.x-1,n.y-3,p);c.drawRect(n.x+1,n.y-12,n.x+5,n.y-3,p);p.setTextSize(9);p.setColor(0xfff3e1ae);float tw=p.measureText(n.name);c.drawText(n.name,n.x-tw/2,n.y-52,p);}}
  private void drawMonsters(Canvas c){RuntimeState.Monster selected=combat.target();for(RuntimeState.Monster m:state.monsters()){if(!m.alive)continue;p.setColor(0xff6a7c69);c.drawOval(new RectF(m.x-13,m.y-31,m.x+13,m.y-5),p);bar(c,m.x-18,m.y-42,m.x+18,m.y-37,0xffd63442,m.hp/(float)m.maxHp);if(selected==m){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.3f);p.setColor(0xffffd86b);c.drawOval(new RectF(m.x-21,m.y-9,m.x+21,m.y+9),p);p.setStyle(Paint.Style.FILL);}}}
  private CharacterRenderer.Direction characterDirection(){switch(dir){case 1:return CharacterRenderer.Direction.SE;case 2:return CharacterRenderer.Direction.NW;case 3:return CharacterRenderer.Direction.NE;default:return CharacterRenderer.Direction.SW;}}
  private CharacterRenderer.State characterState(){if(!state.player().alive)return CharacterRenderer.State.DEAD;if(state.player().hitFlash>0)return CharacterRenderer.State.HIT;switch(action){case WALK:return CharacterRenderer.State.WALK;case CAST:return CharacterRenderer.State.CAST;case SKILL:case KICK:return CharacterRenderer.State.SKILL;case SWING:case THRUST:case THROW:case PUNCH:return CharacterRenderer.State.ATTACK;default:return CharacterRenderer.State.IDLE;}}
  private CharacterRenderer.EffectFamily characterEffectFamily(){switch(action){case CAST:return CharacterRenderer.EffectFamily.CAST;case THROW:return CharacterRenderer.EffectFamily.THROW;case PUNCH:return CharacterRenderer.EffectFamily.PUNCH;case KICK:return CharacterRenderer.EffectFamily.KICK;case SKILL:return CharacterRenderer.EffectFamily.SKILL;default:return CharacterRenderer.EffectFamily.NONE;}}
  private void drawCharacter(Canvas c){CharacterVisualBinding v=CharacterVisualBinding.from(state.rpg());characterRenderer.draw(c,new CharacterRenderer.Pose(state.player().x,state.player().y,characterDirection(),characterState(),walkClock,actionClock,isActing()?duration(action):1f,state.player().hitFlash>0,v.equipmentVisualRef(),v.weaponVisualRef(),CharacterRenderer.ASSET_STATUS,characterEffectFamily()));}

  private void panel(Canvas c,float l,float t,float r,float b){p.setStyle(Paint.Style.FILL);p.setColor(0x9C111419);c.drawRoundRect(new RectF(l,t,r,b),11,11,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.1f);p.setColor(0x77C7A769);c.drawRoundRect(new RectF(l+.5f,t+.5f,r-.5f,b-.5f),11,11,p);p.setStyle(Paint.Style.FILL);}
  private void text(Canvas c,String s,float x,float y,float size){p.setTextSize(size);p.setColor(0xfff2ead8);c.drawText(s,x,y,p);}private void muted(Canvas c,String s,float x,float y,float size){p.setTextSize(size);p.setColor(0xffaaa395);c.drawText(s,x,y,p);}private void bar(Canvas c,float l,float t,float r,float b,int color,float ratio){ratio=Math.max(0,Math.min(1,ratio));p.setColor(0xA8181A1D);c.drawRoundRect(new RectF(l,t,r,b),5,5,p);p.setColor(color);c.drawRoundRect(new RectF(l,t,l+(r-l)*ratio,b),5,5,p);}
  private void drawHud(Canvas c){drawParty(c);drawQuest(c);drawTarget(c);drawMinimap(c);drawUtilityRail(c);drawChat(c);drawJoystick(c);drawPlayerStatus(c);drawCombatCluster(c);}
  private void drawParty(Canvas c){panel(c,16,16,148,92);text(c,"PARTY",28,33,8.5f);String[] n={"조춘찬","수수료좀챙","이루","별빛영혼"};for(int i=0;i<4;i++){float y=47+i*11;text(c,n[i],28,y,8);bar(c,90,y-6,137,y-3,0xffd84555,1f);}}
  private void drawQuest(Canvas c){panel(c,160,16,338,60);text(c,"밀레스의 첫걸음",174,36,10);muted(c,"0 / 4",293,36,8);}
  private void drawTarget(Canvas c){RuntimeState.Monster t=combat.target();if(t==null)return;panel(c,372,14,588,57);text(c,t.name,389,32,10.5f);bar(c,389,40,570,49,0xffd93b4d,t.hp/(float)t.maxHp);}
  private void drawMinimap(Canvas c){panel(c,754,16,890,105);text(c,"밀레스",766,33,9.5f);p.setColor(0xA40A0C0F);c.drawRoundRect(new RectF(766,40,878,91),7,7,p);float nx=(state.player().x-WorldDef.MIN_X)/Math.max(1f,WorldDef.MAX_X-WorldDef.MIN_X),ny=(state.player().y-WorldDef.MIN_Y)/Math.max(1f,WorldDef.MAX_Y-WorldDef.MIN_Y);p.setColor(0xffffdf74);c.drawCircle(768+Math.max(0,Math.min(1,nx))*108,42+Math.max(0,Math.min(1,ny))*47,3,p);}
  private void drawUtilityRail(Canvas c){String[] labels={"≡","BAG","Q","G","W","⚙"};for(int i=0;i<labels.length;i++){float cy=34+i*43;p.setColor(i==1&&inventoryOpen?0xD05F4D2E:0xA814171B);c.drawCircle(926,cy,16,p);p.setStyle(Paint.Style.STROKE);p.setColor(0x99bfa46b);c.drawCircle(926,cy,16,p);p.setStyle(Paint.Style.FILL);p.setTextSize(labels[i].length()>1?7:11);p.setColor(0xffe7ddc8);float tw=p.measureText(labels[i]);c.drawText(labels[i],926-tw/2,cy+3,p);}}
  private void drawChat(Canvas c){RuntimeMetrics m=state.metrics();panel(c,16,318,254,374);muted(c,"밀레스",27,336,8);text(c,"격파 "+m.monsterDefeats()+"  ·  DMG "+m.damageDealt(),27,354,8.5f);}
  private void drawJoystick(Canvas c){p.setColor(joy?0x553D4652:0x302B3038);c.drawCircle(jx,jy,jr,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(joy?2.4f:1.4f);p.setColor(joy?0xCCDBC386:0x777F7562);c.drawCircle(jx,jy,jr,p);p.setStyle(Paint.Style.FILL);p.setColor(joy?0xD5C7A86B:0xA47A6C54);c.drawCircle(knobX,knobY,joy?25:23,p);}
  private void drawPlayerStatus(Canvas c){panel(c,345,477,607,528);Integer level=state.rpg().normalLevel();text(c,"Lv "+(level==null?"?":level),358,496,10.5f);muted(c,"평민",358,512,8);bar(c,405,486,591,495,0xffd83c4d,state.player().hp/(float)state.player().maxHp);bar(c,405,501,591,510,0xff337fd6,state.player().mp/(float)state.player().maxMp);bar(c,405,516,591,521,0xff55a95f,.35f);}
  private void drawCombatCluster(Canvas c){boolean target=combat.hasUsableTarget();actionButton(c,888,466,40,"ATK",target&&combat.attackReady(),attacking(),"ATK".equals(pressedControl),combat.attackCooldown(),combat.attackDef().cooldown,true);actionButton(c,826,413,27,"SKILL",target&&combat.skillReady(),action==Action.SKILL,"SKILL".equals(pressedControl),combat.skillCooldown(),SkillDef.SKILL_PROTO.cooldown,false);actionButton(c,808,478,27,"MAG",target&&combat.castReady(),action==Action.CAST,"MAG".equals(pressedControl),combat.castCooldown(),SkillDef.CAST_PROTO.cooldown,false);actionButton(c,762,430,24,"KICK",target&&combat.kickReady(),action==Action.KICK,"KICK".equals(pressedControl),combat.kickCooldown(),SkillDef.KICK_PROTO.cooldown,false);actionButton(c,748,492,22,"MODE",true,false,"MODE".equals(pressedControl),0,0,false);actionButton(c,927,515,20,"AUTO",false,false,"AUTO".equals(pressedControl),0,0,false);}
  private void actionButton(Canvas c,float x,float y,float r,String label,boolean enabled,boolean selected,boolean pressed,float cd,float total,boolean primary){float rr=pressed?r-2:r;p.setColor(enabled?(primary?0xE05B3A28:0xD022252A):0xA9101114);c.drawCircle(x,y,rr,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(primary?2.6f:1.7f);p.setColor(enabled?(selected?0xffffe69d:0xffc8aa6c):0x665f5a50);c.drawCircle(x,y,rr,p);p.setStyle(Paint.Style.FILL);p.setTextSize(primary?11:(label.length()>4?7:8.5f));p.setColor(enabled?0xfffff0cf:0xff74716a);float tw=p.measureText(label);c.drawText(label,x-tw/2,y+3,p);cooldown(c,x,y,rr,cd,total);}
  private void cooldown(Canvas c,float x,float y,float r,float rem,float total){if(rem<=0||total<=0)return;p.setColor(0xA9000000);c.drawArc(new RectF(x-r,y-r,x+r,y+r),-90,360*Math.min(1f,rem/total),true,p);}
  private void drawFeedbackBanners(Canvas c){if(rewardClock>0){panel(c,345,76,615,108);p.setTextSize(10);p.setColor(0xffffe5a3);float tw=p.measureText(rewardBanner);c.drawText(rewardBanner,480-tw/2,97,p);}if(feedbackClock>0){float w=Math.min(350,Math.max(150,p.measureText(feedback)+42));p.setColor(feedbackTone==FeedbackTone.WARN?0xD04A2226:0xCE161A20);c.drawRoundRect(new RectF(480-w/2,116,480+w/2,145),14,14,p);p.setTextSize(9.5f);p.setColor(0xffeee5d3);float tw=p.measureText(feedback);c.drawText(feedback,480-tw/2,135,p);}}

  private void drawInventory(Canvas c){if(!inventoryOpen)return;p.setColor(0x55000000);c.drawRect(0,0,W,H,p);panel(c,588,92,904,444);text(c,"INVENTORY",608,118,13);p.setColor(0xB024262A);c.drawCircle(879,113,15,p);text(c,"×",874,118,14);List<RpgInventoryPresentation.ItemRow> rows=rpgPresentation.inventoryRows(state.rpg());String selected=rpgInteraction.selectedInventoryItemId();int limit=Math.min(7,rows.size());for(int i=0;i<limit;i++){RpgInventoryPresentation.ItemRow row=rows.get(i);float y=169+i*27;p.setColor(row.itemId.equals(selected)?0x665D4A28:0x5517191D);c.drawRoundRect(new RectF(602,y-16,890,y+6),7,7,p);text(c,row.name+"  x"+row.quantity+(row.equipped?"  [장착]":""),614,y-2,9.5f);}p.setColor(0xD05B4727);c.drawRoundRect(new RectF(810,389,880,419),9,9,p);text(c,"장착",833,409,10);}
  private boolean handleInventoryTouch(float x,float y){if(!inventoryOpen)return false;if(dist(x,y,879,113)<=24){inventoryOpen=false;return true;}List<RpgInventoryPresentation.ItemRow> rows=rpgPresentation.inventoryRows(state.rpg());int limit=Math.min(7,rows.size());if(x>=602&&x<=890)for(int i=0;i<limit;i++){float ry=169+i*27;if(y>=ry-18&&y<=ry+8){rpgInteraction.selectInventoryItem(state.rpg(),rows.get(i).itemId);return true;}}if(x>=810&&x<=880&&y>=389&&y<=419){RpgProgressionState.EquipResult result=rpgInteraction.equipSelectedDetailed(state.rpg());showFeedback(result==RpgProgressionState.EquipResult.EQUIPPED?"장착 완료":"장착 불가",result==RpgProgressionState.EquipResult.EQUIPPED?FeedbackTone.INFO:FeedbackTone.WARN);return true;}return x>=588&&x<=904&&y>=92&&y<=444;}
  private void drawDialogue(Canvas c){RuntimeState.Npc n=interaction.dialogNpc();if(n==null)return;p.setColor(0x72000000);c.drawRect(0,0,W,H,p);panel(c,190,326,770,462);text(c,n.name,218,360,12);drawWrappedText(c,n.dialogue,218,394,510,11.5f,18);muted(c,"화면을 터치하면 닫기",616,446,8.5f);}
  private void drawDeath(Canvas c){if(state.player().alive)return;p.setColor(0xB6000000);c.drawRect(0,0,W,H,p);panel(c,330,205,630,335);text(c,"행동 불능",428,242,19);muted(c,"화면 중앙 터치로 부활",405,298,10);}
  private void drawWrappedText(Canvas c,String s,float x,float y,float max,float size,float line){p.setTextSize(size);p.setColor(0xffeee4cf);String[] words=s.split(" ");String current="";float yy=y;for(String word:words){String test=current.length()==0?word:current+" "+word;if(p.measureText(test)>max&&current.length()>0){c.drawText(current,x,yy,p);yy+=line;current=word;}else current=test;}if(current.length()>0)c.drawText(current,x,yy,p);}

  private boolean isHudSurface(float x,float y){
    if(x>=16&&x<=148&&y>=16&&y<=92)return true;
    if(x>=160&&x<=338&&y>=16&&y<=60)return true;
    if(combat.target()!=null&&x>=372&&x<=588&&y>=14&&y<=57)return true;
    if(x>=754&&x<=890&&y>=16&&y<=105)return true;
    for(int i=0;i<6;i++)if(dist(x,y,926,34+i*43)<=20)return true;
    if(x>=16&&x<=254&&y>=318&&y<=374)return true;
    if(x>=345&&x<=607&&y>=477&&y<=528)return true;
    if(dist(x,y,jx,jy)<=jr*1.5f)return true;
    if(dist(x,y,808,478)<=34||dist(x,y,748,492)<=29||dist(x,y,826,413)<=34||dist(x,y,762,430)<=31||dist(x,y,888,466)<=48||dist(x,y,927,515)<=27)return true;
    return false;
  }
  private void requestGroundMove(float x,float y){WorldMoveTargetController.Snapshot move=worldRuntime.requestGroundScreenTap(x,y);WorldCameraTransform.Point wp=worldRuntime.screenToWorld(x,y);lastMoveRequestId=move.requestId;lastMoveStatus=WorldMoveTargetController.Status.IDLE;if(move.status==WorldMoveTargetController.Status.MOVING||move.status==WorldMoveTargetController.Status.REACHED){tapMarkerX=wp.x;tapMarkerY=wp.y;tapMarkerClock=.7f;showFeedback(move.replacedRequestId>0?"이동 목표 변경":"이동 시작",FeedbackTone.INFO);}consumeMoveOutcome(move);}

  public boolean onTouchEvent(MotionEvent e){float x=(e.getX()-ox)/scale,y=(e.getY()-oy)/scale;switch(e.getActionMasked()){
    case MotionEvent.ACTION_DOWN:
      if(!state.player().alive){if(dist(x,y,480,270)<=180){state.revivePlayer();combat.clearTarget();interaction.cancel();worldRuntime.cancel();worldRuntime.snapCameraToPlayer();action=Action.IDLE;}return true;}
      if(interaction.dialogOpen()){interaction.dismissDialog();worldRuntime.cancelForAction();return true;}
      if(dist(x,y,926,77)<=20){inventoryOpen=!inventoryOpen;worldRuntime.cancelForAction();return true;}
      if(handleInventoryTouch(x,y)){worldRuntime.cancelForAction();return true;}
      if(inventoryOpen){inventoryOpen=false;worldRuntime.cancelForAction();return true;}
      if(dist(x,y,jx,jy)<=jr*1.5f){joy=true;pressedControl="JOY";worldRuntime.cancelForDirectInput();interaction.cancelApproach();combat.cancelApproach();stick(x,y);return true;}
      if(dist(x,y,808,478)<=34){pressedControl="MAG";cast();return true;}if(dist(x,y,748,492)<=29){pressedControl="MODE";worldRuntime.cancelForAction();combat.cycleAttackMode();return true;}if(dist(x,y,826,413)<=34){pressedControl="SKILL";skill();return true;}if(dist(x,y,762,430)<=31){pressedControl="KICK";kick();return true;}if(dist(x,y,888,466)<=48){pressedControl="ATK";attack();return true;}if(dist(x,y,927,515)<=27){pressedControl="AUTO";showFeedback("AUTO 준비 중",FeedbackTone.WARN);return true;}
      if(isHudSurface(x,y))return true;
      WorldCameraTransform.Point wp=worldRuntime.screenToWorld(x,y);RuntimeState.Npc npc=state.hitNpc(wp.x,wp.y,34f);if(npc!=null){worldRuntime.cancelForAction();combat.cancelApproach();interaction.request(state,npc);showFeedback("NPC 접근 · "+npc.name,FeedbackTone.INFO);return true;}RuntimeState.Monster monster=state.hitMonster(wp.x,wp.y,34f);if(monster!=null){worldRuntime.cancelForAction();combat.selectTarget(monster);interaction.cancelApproach();showFeedback("타깃 선택 · "+monster.name,FeedbackTone.INFO);return true;}requestGroundMove(x,y);return true;
    case MotionEvent.ACTION_MOVE:if(joy)stick(x,y);return true;
    case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:joy=false;pressedControl="";knobX=jx;knobY=jy;vx=vy=0;return true;
  }return true;}
  private void stick(float x,float y){if(isActing()||!state.player().alive)return;float dx=x-jx,dy=y-jy,len=(float)Math.sqrt(dx*dx+dy*dy);if(len>jr){dx=dx/len*jr;dy=dy/len*jr;len=jr;}knobX=jx+dx;knobY=jy+dy;if(len<8){vx=vy=0;return;}float nx=dx/len,ny=dy/len;if(Math.abs(nx)>Math.abs(ny)){if(nx>0){vx=.707f;vy=.707f;dir=1;}else{vx=-.707f;vy=-.707f;dir=2;}}else{if(ny>0){vx=-.707f;vy=.707f;dir=0;}else{vx=.707f;vy=-.707f;dir=3;}}}
  private float dist(float a,float b,float c,float d){float x=a-c,y=b-d;return(float)Math.sqrt(x*x+y*y);}
}
