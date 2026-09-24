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
import com.projectdark.mobile.world.ReagentShopInteriorDef;
import com.projectdark.mobile.world.ReagentShopInteriorRenderer;
import java.util.List;
import java.util.Collections;
import java.util.Map;

/** PROJECT DARK v0.74 - original-inspired mobile HUD adaptation + precise world tap routing. */
public final class GameView extends View {
  private static final float W=960f,H=540f;
  private static final float JOY_X=92f,JOY_Y=454f,JOY_R=58f;
  private static final float SLOT=42f,SLOT_GAP=4f,SLOT_X0=696f,SLOT_Y0=374f;
  private static final float ATK_X=916f,ATK_Y=498f,ATK_R=38f;
  private static final float MODE_X=794f,MODE_Y=495f,MODE_R=22f;
  private static final float AUTO_X=850f,AUTO_Y=495f,AUTO_R=24f;
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
  private final EquipmentActionResolver equipmentActions=new EquipmentActionResolver();
  private final RuntimeCombatSession combatSession=new RuntimeCombatSession(state,
      worldAdapter::hasCombatLineOfSight,RuntimeCombatSession.startingCommonerLearnedActions(),actor->true);
  private final MonsterAIController monsterAi=new MonsterAIController();
  private final InteractionController interaction=new InteractionController();
  private final CharacterRenderer characterRenderer=new CharacterRenderer();
  private final RpgInventoryPresentation rpgPresentation=new RpgInventoryPresentation();
  private final RpgInteractionController rpgInteraction=new RpgInteractionController();
  private final ClassicHudIconAtlas hudIcons=new ClassicHudIconAtlas();
  private final EquipmentVisualRegistry inventoryVisuals;
  private final ReagentItemVisualRegistry reagentVisuals;
  private final ReagentShopInteriorRenderer reagentShopRenderer;
  private WorldRuntimeAdapter reagentShopAdapter;
  private boolean inReagentShop=false,reagentShopOpen=false;
  private float millesReturnX,millesReturnY;
  private final F5mAdaptedPrologueQuest f5mQuest=F5mAdaptedPrologueQuest.openingFixture();
  private final GrowthQuest2 quest2=new GrowthQuest2();

  private float scale=1,ox,oy,vx,vy;
  private float knobX=JOY_X,knobY=JOY_Y;
  private boolean joy,running,inventoryOpen,statsOpen,equipmentOpen;
  private int inventoryPage=0;
  private float checkpointClock;
  private boolean saveWarningShown;
  private long savedLedgerSequence;
  private long last,lastLedgerSequence=0,lastMoveRequestId=0,lastRewardSequence=0;
  private final CanonicalActorFacing playerFacing=new CanonicalActorFacing(CharacterRenderer.Direction.SW);
  private float walkClock=0,actionClock=0,tapMarkerClock=0,tapMarkerX=0,tapMarkerY=0,directStepClock=0;
  private Action action=Action.IDLE;
  private WorldMoveTargetController.Status lastMoveStatus=WorldMoveTargetController.Status.IDLE;
  private String feedback="",pressedControl="",rewardBanner="";
  private float feedbackClock=0,rewardClock=0;
  private FeedbackTone feedbackTone=FeedbackTone.INFO;

  private final Runnable loop=new Runnable(){@Override public void run(){if(!running)return;long n=SystemClock.uptimeMillis();float dt=Math.min(.05f,(n-last)/1000f);last=n;F5mSaveStore.beginFrame();try{update(dt);}finally{F5mSaveStore.endFrame();}checkpointClock+=dt;if(checkpointClock>=2f||savedLedgerSequence!=state.ledger().sequence()){checkpoint();}if(!inReagentShop)camera.follow(worldAdapter.presentationPlayerX(),worldAdapter.presentationPlayerY());invalidate();postDelayed(this,16);}};

  public GameView(Context c){super(c);inventoryVisuals=new EquipmentVisualRegistry(c);reagentVisuals=new ReagentItemVisualRegistry(c);reagentShopRenderer=new ReagentShopInteriorRenderer(c);setKeepScreenOn(true);F5mSaveStore.restoreQuestActive(f5mQuest);F5mSaveStore.restoreRewardsActive(state.rpg());F5mSaveStore.restoreQuest2Active(quest2);quest2.unlockIfPrologueCompleted(f5mQuest);F5mSaveStore.restoreRuntimeActive(state);worldAdapter.snapCameraToPlayer();F5mSaveStore.bindRuntime(state,f5mQuest,quest2);savedLedgerSequence=state.ledger().sequence();if(!F5mSaveStore.writable())showFeedback("저장 데이터를 읽지 못했습니다 · 원본 보존 중",FeedbackTone.WARN);}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(loop);}
  public void pause(){running=false;removeCallbacks(loop);F5mSaveStore.beginFrame();try{state.tick(0f);state.applyDerivedGrowth();consumeLedger();}finally{F5mSaveStore.endFrame();}checkpoint();}
  private void checkpoint(){
    savedLedgerSequence=state.ledger().sequence();
    if(F5mSaveStore.checkpointActive()){saveWarningShown=false;}
    else if(!saveWarningShown){showFeedback("저장하지 못했습니다 · 이전 저장 확인 필요",FeedbackTone.WARN);saveWarningShown=true;}
    checkpointClock=0f;
  }

  private void update(float dt){
    if(inReagentShop){updateReagentShop(dt);return;}
    feedbackClock=Math.max(0,feedbackClock-dt);rewardClock=Math.max(0,rewardClock-dt);tapMarkerClock=Math.max(0,tapMarkerClock-dt);
    combat.tick(dt);combat.setBasicAttack(equipmentActions.resolveBasicAttack(state.rpg()).animationAction);combatSession.tick(dt);state.tick(dt);state.applyDerivedGrowth();quest2.unlockIfPrologueCompleted(f5mQuest);consumeLedger();consumeRewardNotice();monsterAi.tick(state,dt);
    if(!state.player().alive){action=Action.IDLE;playerFacing.endAttack();interaction.cancel();combat.cancelApproach();worldAdapter.cancel();joy=false;vx=vy=0;knobX=JOY_X;knobY=JOY_Y;return;}
    if(isActing()){actionClock+=dt;if(actionClock>=duration(action)){actionClock=0;playerFacing.endAttack();action=(joy&&(vx!=0||vy!=0))?Action.WALK:Action.IDLE;}return;}
    WorldRuntimeAdapter.FrameSnapshot navigation=worldAdapter.tickNavigation(dt);
    if(navigation.overlappingPortal!=null&&"potion_shop_door".equals(navigation.overlappingPortal.id)){enterReagentShop();return;}
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
  private void consumeLedger(){List<CombatLedger.Event> events=state.ledger().snapshot();for(CombatLedger.Event e:events){if(e.sequence<=lastLedgerSequence)continue;lastLedgerSequence=e.sequence;F5mAdaptedPrologueQuest.DefeatResult qr=f5mQuest.consume(e);if(qr==F5mAdaptedPrologueQuest.DefeatResult.RETURN_READY)showReward("퀘스트 목표 완료 · 안내인에게 돌아가세요");if(quest2.consume(e)&&quest2.state()==GrowthQuest2.State.RETURN_READY)showReward("성장 훈련 완료 · 안내인에게 돌아가세요");switch(e.type){case MONSTER_DEFEATED:showFeedback("몬스터 격파",FeedbackTone.INFO);break;case PLAYER_HIT:showFeedback("-"+e.amount+" HP",FeedbackTone.WARN);break;case PLAYER_DEFEATED:showFeedback("행동 불능",FeedbackTone.WARN);break;case PLAYER_REVIVED:showFeedback("부활",FeedbackTone.INFO);break;default:break;}}}
  private void consumeRewardNotice(){RpgInventoryPresentation.RewardNotice notice=rpgPresentation.latestRewardNotice(state.rpg());if(notice==null||notice.combatSequence<=lastRewardSequence)return;lastRewardSequence=notice.combatSequence;F5mSaveStore.saveRewardsActive(state.rpg());if(notice.status!=RpgProgressionState.RewardStatus.RESOLVED){showFeedback("보상 확인 필요",FeedbackTone.WARN);return;}if(!notice.autoLootedItems.isEmpty()){Map.Entry<String,Integer> first=null;for(Map.Entry<String,Integer> e:notice.autoLootedItems.entrySet()){first=e;break;}if(first!=null){int visibleQty=inventoryQuantity(first.getKey());if(visibleQty<first.getValue()){showFeedback("보상 지급 상태를 확인할 수 없습니다",FeedbackTone.WARN);return;}String name=itemDisplayName(first.getKey());int extra=Math.max(0,notice.autoLootedItems.size()-1);showReward("자동루팅 · "+name+" x"+first.getValue()+(extra>0?" 외 "+extra+"종":""));return;}}if(notice.exp!=null){showReward("보상 처리 · EXP +"+notice.exp);return;}showReward("보상 처리 완료");}
  private int inventoryQuantity(String itemId){for(RpgInventoryPresentation.ItemRow row:rpgPresentation.inventoryRows(state.rpg()))if(row.itemId.equals(itemId))return row.quantity;return 0;}
  private String itemDisplayName(String itemId){for(RpgInventoryPresentation.ItemRow row:rpgPresentation.inventoryRows(state.rpg()))if(row.itemId.equals(itemId))return row.name;return itemId;}
  private RuntimeState.Npc findNpc(String id){if(id==null)return null;for(RuntimeState.Npc npc:state.npcs())if(id.equals(npc.id))return npc;return null;}
  private RuntimeState.Monster findMonster(String id){if(id==null)return null;for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id)&&m.alive)return m;return null;}
  private void autoNavigateQuest(){F5mAdaptedPrologueQuest.State qs=f5mQuest.state();worldAdapter.cancelForAction();interaction.cancelApproach();combat.cancelApproach();if(qs==F5mAdaptedPrologueQuest.State.AVAILABLE||qs==F5mAdaptedPrologueQuest.State.RETURN_READY){RuntimeState.Npc npc=findNpc("milles_guide_proto");if(npc!=null){worldAdapter.requestNpcApproach(npc.id);showFeedback("퀘스트 자동이동 · "+npc.name,FeedbackTone.INFO);}return;}if(qs==F5mAdaptedPrologueQuest.State.ACTIVE){RuntimeState.Monster m=findMonster(f5mQuest.objectiveMonsterId());if(m!=null){combat.selectTarget(m);worldAdapter.requestMonsterApproach(m.id,48f);showFeedback("퀘스트 자동이동 · "+m.name,FeedbackTone.INFO);}else showFeedback("퀘스트 목표를 찾을 수 없습니다",FeedbackTone.WARN);}}

  private void executeReadyCombatIntent(){CombatController.Intent ready=combat.consumeReadyIntent();action=Action.IDLE;if(ready==CombatController.Intent.ATTACK)attack();else if(ready==CombatController.Intent.CAST)cast();else if(ready==CombatController.Intent.SKILL)skill();else if(ready==CombatController.Intent.KICK)kick();}
  private void beginCombatApproach(CombatController.Intent intent){worldAdapter.cancelForAction();if(combat.beginApproach(intent)){RuntimeState.Monster target=combat.approachTarget();if(target!=null)worldAdapter.requestMonsterApproach(target.id,combat.intentRange());showFeedback("타깃으로 이동",FeedbackTone.INFO);}}

  private void setFacingDirection(float dx,float dy){playerFacing.updateLocomotion(dx,dy);}
  private void applyWorldFacing(WorldMoveTargetController.Direction direction){if(direction==null)return;switch(direction){case SW:playerFacing.setLocomotion(CharacterRenderer.Direction.SW);break;case SE:playerFacing.setLocomotion(CharacterRenderer.Direction.SE);break;case NW:playerFacing.setLocomotion(CharacterRenderer.Direction.NW);break;case NE:playerFacing.setLocomotion(CharacterRenderer.Direction.NE);break;}}
  private WorldMoveTargetController.Direction joystickDirection(){if(vx>0f&&vy>0f)return WorldMoveTargetController.Direction.SE;if(vx<0f&&vy<0f)return WorldMoveTargetController.Direction.NW;if(vx>0f)return WorldMoveTargetController.Direction.NE;return WorldMoveTargetController.Direction.SW;}
  private void setAutoDirection(float dx,float dy){float sx=dx>=0?1f:-1f,sy=dy>=0?1f:-1f;vx=.707f*sx;vy=.707f*sy;}
  private void faceTarget(){RuntimeState.Monster t=combat.target();if(t!=null&&t.alive)setFacingDirection(t.x-state.player().x,t.y-state.player().y);}
  private void snapshotAttackFacingTarget(){RuntimeState.Monster t=combat.target();if(t==null||!t.alive)return; CharacterRenderer.Direction canonical=combat.attackFacing(state); if(canonical!=null){playerFacing.beginAttack(canonical);return;} playerFacing.beginAttack(t.x-state.player().x,t.y-state.player().y);}
  static CharacterRenderer.Direction canonicalAttackFacing(float dx,float dy,CharacterRenderer.Direction fallback){return CanonicalActorFacing.quantize(dx,dy,fallback);}
  static boolean inventoryOverlayCancelsMovement(){return false;}
  static boolean hitCharacterPoseEnabled(){return false;}
  private boolean isActing(){return action!=Action.IDLE&&action!=Action.WALK;}
  private boolean attacking(){return action==Action.SWING||action==Action.THRUST||action==Action.THROW||action==Action.PUNCH;}
  private float duration(Action a){switch(a){case CAST:return .65f;case SWING:return .45f;case THRUST:return .38f;case THROW:return .52f;case PUNCH:return .32f;case SKILL:return .50f;case KICK:return .45f;default:return 0;}}
  private void trigger(Action a){if(isActing()||!state.player().alive)return;worldAdapter.cancelForAction();interaction.cancel();combat.cancelApproach();action=a;actionClock=0;}
  private void showFeedback(String s){showFeedback(s,FeedbackTone.INFO);}private void showFeedback(String s,FeedbackTone tone){feedback=s;feedbackTone=tone;feedbackClock=1.15f;}private void showReward(String s){rewardBanner=s;rewardClock=2.4f;}
  private boolean requireTarget(){if(combat.hasUsableTarget())return true;showFeedback("먼저 몬스터를 선택하세요",FeedbackTone.WARN);return false;}
  private boolean inRange(float range){return combat.inRange(state,range);}
  private void cast(){submitLearnedAction(SkillDef.CAST_PROTO,Action.CAST);}
  private void skill(){submitLearnedAction(SkillDef.SKILL_PROTO,Action.SKILL);}
  private void kick(){submitLearnedAction(SkillDef.KICK_PROTO,Action.KICK);}
  private void submitLearnedAction(SkillDef def,Action visual){
    if(isActing()||!state.player().alive||!requireTarget())return;
    CombatActionOrchestrator.Submission result=combatSession.submitPlayer(combat.target().id,def.id);
    if(result.accepted()){snapshotAttackFacingTarget();trigger(visual);}
    else showFeedback(result.rejectReason==CombatResolver.RejectReason.NOT_LEARNED?"아직 배우지 않은 기술입니다":result.rejectReason==CombatResolver.RejectReason.RESOURCE?"MP가 부족합니다":"지금 사용할 수 없습니다",FeedbackTone.WARN);
  }
  private Action actionFor(AttackDef.Kind k){switch(k){case THRUST:return Action.THRUST;case THROW:return Action.THROW;case PUNCH:return Action.PUNCH;default:return Action.SWING;}}
  private void attack(){
    if(isActing()||!state.player().alive||!requireTarget())return;
    combat.setBasicAttack(equipmentActions.resolveBasicAttack(state.rpg()).animationAction);
    AttackDef def=combat.attackDef();
    if(!combat.attackInRange(state)){beginCombatApproach(CombatController.Intent.ATTACK);return;}
    RuntimeCombatSession.PlayerActionSubmission result=combatSession.submitPlayerBasicAttack(combat.target().id);
    if(!result.combat.accepted())return;
    worldAdapter.cancelForAction();snapshotAttackFacingTarget();combat.commitAttack();trigger(actionFor(def.kind));
  }

  protected void onSizeChanged(int w,int h,int ow,int oh){scale=Math.min(w/W,h/H);ox=(w-W*scale)/2;oy=(h-H*scale)/2;}
  protected void onDraw(Canvas c){c.drawColor(Color.BLACK);c.save();c.translate(ox,oy);c.scale(scale,scale);if(inReagentShop){drawReagentShopWorld(c);drawHud(c);drawFeedbackBanners(c);drawReagentShop(c);}else{drawWorld(c);c.save();c.translate(-camera.cameraX(),-camera.cameraY());drawTapMarker(c);drawNpcs(c);drawMonsters(c);drawCharacter(c);c.restore();drawHud(c);drawFeedbackBanners(c);drawInventory(c);drawEquipment(c);drawStats(c);drawDialogue(c);drawDeath(c);}c.restore();}

  private void enterReagentShop(){
    millesReturnX=state.player().x;millesReturnY=state.player().y;
    inReagentShop=true;reagentShopOpen=false;inventoryOpen=statsOpen=equipmentOpen=false;
    interaction.cancel();combat.cancelApproach();worldAdapter.cancelForAction();
    state.player().x=ReagentShopInteriorDef.SPAWN_X;state.player().y=ReagentShopInteriorDef.SPAWN_Y;
    reagentShopAdapter=new WorldRuntimeAdapter(state,W,H,ReagentShopInteriorDef.MIN_X,ReagentShopInteriorDef.MAX_X,
        ReagentShopInteriorDef.MIN_Y,ReagentShopInteriorDef.MAX_Y,ReagentShopInteriorDef.navigationTiles(),Collections.emptyList());
    reagentShopAdapter.snapCameraToPlayer();action=Action.IDLE;joy=false;vx=vy=0;directStepClock=0f;
    showFeedback("밀레스 시약상점",FeedbackTone.INFO);
  }
  private void leaveReagentShop(){
    if(reagentShopAdapter!=null)reagentShopAdapter.cancel();
    inReagentShop=false;reagentShopOpen=false;reagentShopAdapter=null;
    // Return one canonical tile south of the door so re-entry requires deliberately stepping back onto the portal.
    state.player().x=millesReturnX;state.player().y=millesReturnY+32f;
    worldAdapter.snapCameraToPlayer();action=Action.IDLE;joy=false;vx=vy=0;
    showFeedback("밀레스",FeedbackTone.INFO);
  }
  private void updateReagentShop(float dt){
    feedbackClock=Math.max(0,feedbackClock-dt);rewardClock=Math.max(0,rewardClock-dt);
    if(reagentShopAdapter==null)return;
    if(reagentShopOpen){action=Action.IDLE;return;}
    WorldRuntimeAdapter.FrameSnapshot nav=reagentShopAdapter.tickNavigation(dt);
    if(joy&&(vx!=0||vy!=0)){
      directStepClock=Math.max(0f,directStepClock-dt);
      if(!reagentShopAdapter.presentationMoving()&&directStepClock<=0f){
        WorldMoveTargetController.Snapshot step=reagentShopAdapter.step(joystickDirection());
        directStepClock=WorldMoveTargetController.TILE_STEP_SECONDS;applyWorldFacing(step.lastStepDirection);
      }
    }
    if(nav.movement.lastStepDirection!=null)applyWorldFacing(nav.movement.lastStepDirection);
    if(reagentShopAdapter.presentationMoving()){action=Action.WALK;walkClock+=dt;}else action=Action.IDLE;
    if(Math.abs(state.player().x-ReagentShopInteriorDef.EXIT_X)<.01f&&Math.abs(state.player().y-ReagentShopInteriorDef.EXIT_Y)<.01f)leaveReagentShop();
  }
  private void drawReagentShopWorld(Canvas c){
    p.setColor(0xff120d09);c.drawRect(0,0,W,H,p);if(reagentShopAdapter==null)return;
    reagentShopRenderer.draw(c,reagentShopAdapter);
    WorldCameraTransform.Point m=reagentShopAdapter.worldToScreen(ReagentShopInteriorDef.MERLIN_X,ReagentShopInteriorDef.MERLIN_Y);
    // Merlin must read at the same paper-doll scale as the player, not as an oversized prop.
    c.save();c.translate(m.x,m.y);c.scale(.78f,.78f);characterRenderer.draw(c,new CharacterRenderer.Pose(0,0,CharacterRenderer.Direction.SE,CharacterRenderer.State.IDLE,0,0,1,false,
        "mu0000118,mh259",null,CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));c.restore();
    text(c,"멀린",m.x-13,m.y-42,9);
    WorldCameraTransform.Point q=reagentShopAdapter.worldToScreen(reagentShopAdapter.presentationPlayerX(),reagentShopAdapter.presentationPlayerY());
    characterRenderer.draw(c,new CharacterRenderer.Pose(q.x,q.y,characterDirection(),
        reagentShopAdapter.presentationMoving()?CharacterRenderer.State.WALK:CharacterRenderer.State.IDLE,walkClock,0,1,false,
        CharacterVisualBinding.from(state.rpg()).equipmentVisualRef(),CharacterVisualBinding.from(state.rpg()).weaponVisualRef(),
        CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));
  }
  private void drawReagentShop(Canvas c){
    if(!reagentShopOpen)return;p.setColor(0x5c000000);c.drawRect(0,0,W,H,p);classicWindow(c,430,74,918,454,"멀린의 시약상점");text(c,"×",887,102,14);
    int i=0;for(ReagentShopCatalog.Offer o:ReagentShopCatalog.offers()){float t=128+i*70;p.setColor(0xD0191411);c.drawRect(456,t,892,t+58,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);p.setColor(0xFF765335);c.drawRect(456.5f,t+.5f,891.5f,t+57.5f,p);p.setStyle(Paint.Style.FILL);Bitmap b=reagentVisuals.get(o.itemId);if(b!=null){p.setFilterBitmap(false);float sc=Math.min(28f/b.getWidth(),28f/b.getHeight()),dw=b.getWidth()*sc,dh=b.getHeight()*sc;c.drawBitmap(b,null,new RectF(473+(28-dw)/2,t+15+(28-dh)/2,473+(28+dw)/2,t+15+(28+dh)/2),p);}text(c,o.name,526,t+25,10);mutedText(c,o.price==null?"가격 원전 확인 중":o.price+" Gold",526,t+44,8);if(o.purchasable()){p.setColor(0xD05B4727);c.drawRoundRect(new RectF(792,t+12,874,t+47),7,7,p);text(c,"구매",816,t+34,9);}i++;}
  }
  private boolean handleReagentShopTouch(MotionEvent e,float x,float y){
    if(e.getActionMasked()==MotionEvent.ACTION_DOWN){
      if(reagentShopOpen){if(dist(x,y,891,97)<=25){reagentShopOpen=false;return true;}return true;}
      if(reagentShopAdapter==null)return true;
      WorldCameraTransform.Point m=reagentShopAdapter.worldToScreen(ReagentShopInteriorDef.MERLIN_X,ReagentShopInteriorDef.MERLIN_Y);
      if(dist(x,y,m.x,m.y-22)<=42){reagentShopOpen=true;reagentShopAdapter.cancelForAction();joy=false;vx=vy=0;showFeedback("멀린 · 시약 판매",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,JOY_X,JOY_Y,JOY_R)){joy=true;directStepClock=0f;stick(x,y);return true;}
      if(!isHudSurface(x,y)){WorldCameraTransform.Point w=reagentShopAdapter.screenToWorld(x,y);reagentShopAdapter.requestGroundWorld(w.x,w.y);}return true;
    }
    if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&joy){stick(x,y);return true;}
    if(e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL){joy=false;vx=vy=0;knobX=JOY_X;knobY=JOY_Y;return true;}return true;
  }

  private void drawWorld(Canvas c){p.setColor(0xff101511);c.drawRect(0,0,W,H,p);mapRenderer.draw(c,worldAdapter);}
  private void drawTapMarker(Canvas c){if(tapMarkerClock<=0)return;float q=Math.max(0f,Math.min(1f,tapMarkerClock/.7f));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(0xD8FFD86B);c.drawCircle(tapMarkerX,tapMarkerY,8f+8f*(1f-q),p);p.setStyle(Paint.Style.FILL);p.setColor(0x66FFD86B);c.drawCircle(tapMarkerX,tapMarkerY,3.5f,p);}
  private void drawNpcs(Canvas c){for(RuntimeState.Npc n:state.npcs()){p.setColor(0x66000000);c.drawOval(new RectF(n.x-11,n.y-3,n.x+11,n.y+4),p);p.setColor(0xffcab58b);c.drawCircle(n.x,n.y-39,7,p);p.setColor(0xff6c5946);c.drawRect(n.x-7,n.y-31,n.x+7,n.y-11,p);p.setColor(0xffb58f58);c.drawRect(n.x-5,n.y-12,n.x-1,n.y-3,p);c.drawRect(n.x+1,n.y-12,n.x+5,n.y-3,p);p.setTextSize(9);p.setColor(0xfff3e1ae);float tw=p.measureText(n.name);c.drawText(n.name,n.x-tw/2,n.y-52,p);if(interaction.approachNpc()==n){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xfff0d17a);c.drawCircle(n.x,n.y-24,19,p);p.setStyle(Paint.Style.FILL);}}}
  private void drawMonsters(Canvas c){RuntimeState.Monster selected=combat.target();for(RuntimeState.Monster m:state.monsters()){if(!m.alive)continue;CharacterRenderer.Direction facing=m.visualFacing.presentation();float eyeX=monsterFacingX(facing),eyeY=monsterFacingY(facing);p.setColor(0x66000000);c.drawOval(new RectF(m.x-15,m.y-4,m.x+15,m.y+5),p);p.setColor(m.hitFlash>0?0xffd9e8d7:0xff6a7c69);c.drawOval(new RectF(m.x-13,m.y-31,m.x+13,m.y-5),p);p.setColor(0xffd8c27b);c.drawCircle(m.x+eyeX-3,m.y-20+eyeY,2,p);c.drawCircle(m.x+eyeX+3,m.y-20+eyeY,2,p);if(m.attackPrimed){float q=1f-Math.min(1f,m.attackWindup/.24f);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2+2*q);p.setColor(0xaaff7755);c.drawCircle(m.x,m.y-19,18+8*q,p);p.setStyle(Paint.Style.FILL);}bar(c,m.x-18,m.y-42,m.x+18,m.y-37,0xffd63442,m.hp/(float)m.maxHp);if(m.damagePopupClock>0){p.setTextSize(12);p.setColor(0xffffdc72);String d="-"+m.lastDamage;float tw=p.measureText(d);c.drawText(d,m.x-tw/2,m.y-50-(.65f-m.damagePopupClock)*20,p);}if(selected==m){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.3f);p.setColor(0xffffd86b);c.drawOval(new RectF(m.x-21,m.y-9,m.x+21,m.y+9),p);p.setStyle(Paint.Style.FILL);}}}
  static float monsterFacingX(CharacterRenderer.Direction d){return d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW?-3f:3f;}
  static float monsterFacingY(CharacterRenderer.Direction d){return d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE?-2f:2f;}
  private CharacterRenderer.Direction characterDirection(){return playerFacing.presentation();}
  private CharacterRenderer.State characterState(){if(!state.player().alive)return CharacterRenderer.State.DEAD;switch(action){case WALK:return CharacterRenderer.State.WALK;case CAST:return CharacterRenderer.State.CAST;case SKILL:case KICK:return CharacterRenderer.State.SKILL;case SWING:case THRUST:case THROW:case PUNCH:return CharacterRenderer.State.ATTACK;default:return CharacterRenderer.State.IDLE;}}
  private CharacterRenderer.EffectFamily characterEffectFamily(){switch(action){case CAST:return CharacterRenderer.EffectFamily.CAST;case THROW:return CharacterRenderer.EffectFamily.THROW;case PUNCH:return CharacterRenderer.EffectFamily.PUNCH;case KICK:return CharacterRenderer.EffectFamily.KICK;case SKILL:return CharacterRenderer.EffectFamily.SKILL;default:return CharacterRenderer.EffectFamily.NONE;}}
  private void drawCharacter(Canvas c){CharacterRenderer.State presentation=characterState();CharacterVisualBinding visuals=CharacterVisualBinding.from(state.rpg());float stateDuration=isActing()?duration(action):1f;AnimationAction visualAction=presentation==CharacterRenderer.State.ATTACK?equipmentActions.resolveBasicAttack(state.rpg()).animationAction:null;characterRenderer.draw(c,new CharacterRenderer.Pose(worldAdapter.presentationPlayerX(),worldAdapter.presentationPlayerY(),characterDirection(),presentation,walkClock,actionClock,stateDuration,false,visuals.equipmentVisualRef(),visuals.weaponVisualRef(),CharacterRenderer.ASSET_STATUS,characterEffectFamily(),visualAction));}

  private void panel(Canvas c,float l,float t,float r,float b){p.setStyle(Paint.Style.FILL);p.setColor(0x8A17120E);c.drawRoundRect(new RectF(l,t,r,b),7,7,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xA0A86F3B);c.drawRoundRect(new RectF(l+.5f,t+.5f,r-.5f,b-.5f),7,7,p);p.setStyle(Paint.Style.FILL);}
  private void modalPanel(Canvas c,float l,float t,float r,float b){p.setStyle(Paint.Style.FILL);p.setColor(0xED11151A);c.drawRoundRect(new RectF(l,t,r,b),12,12,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.4f);p.setColor(0xAACFB171);c.drawRoundRect(new RectF(l+.5f,t+.5f,r-.5f,b-.5f),12,12,p);p.setStyle(Paint.Style.FILL);}
  private void text(Canvas c,String s,float x,float y,float size){p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));p.setTextSize(size);p.setColor(0xfff4ead5);c.drawText(s,x,y,p);p.setTypeface(Typeface.DEFAULT);}
  private void mutedText(Canvas c,String s,float x,float y,float size){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);p.setColor(0xffb8aa91);c.drawText(s,x,y,p);}
  private void bar(Canvas c,float l,float t,float r,float b,int color,float ratio){ratio=Math.max(0,Math.min(1,ratio));p.setColor(0xCC171416);c.drawRoundRect(new RectF(l,t,r,b),(b-t)*.3f,(b-t)*.3f,p);p.setColor(color);c.drawRoundRect(new RectF(l,t,l+(r-l)*ratio,b),(b-t)*.3f,(b-t)*.3f,p);}

  private void drawHud(Canvas c){drawTopLeftStatus(c);ActiveQuestTracker.Quest tracked=ActiveQuestTracker.current(f5mQuest,quest2);if(tracked==ActiveQuestTracker.Quest.PROLOGUE)drawQuest(c);else if(tracked==ActiveQuestTracker.Quest.GROWTH_2)drawQuest2(c);else if(PostF5mHudPresentation.showNextPurpose(f5mQuest.state()))drawNextPurpose(c);drawTarget(c);drawMinimap(c);drawUtilityRail(c);drawChat(c);drawJoystick(c);drawExpStrip(c);drawCombatCluster(c);}
  private void drawTopLeftStatus(Canvas c){panel(c,14,12,202,108);Integer level=state.rpg().normalLevel();text(c,"Lv. "+(level==null?"?":level),25,36,14);mutedText(c,"평민",26,55,8);bar(c,70,22,190,37,0xffd73d31,state.player().hp/(float)state.player().maxHp);bar(c,70,43,190,58,0xff347fcf,state.player().mp/(float)state.player().maxMp);mutedText(c,state.player().hp+" / "+state.player().maxHp,106,34,7);mutedText(c,state.player().mp+" / "+state.player().maxMp,106,55,7);for(int i=0;i<8;i++){float x=24+i*20;p.setColor(0xCC25211D);c.drawRect(x,70,x+15,85,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xff8f6a43);c.drawRect(x,70,x+15,85,p);p.setStyle(Paint.Style.FILL);p.setColor(i%2==0?0xff6d9a58:0xff755dc0);c.drawCircle(x+7.5f,77.5f,4,p);}mutedText(c,"BUFF",24,100,7);Long gold=state.rpg().gold();text(c,"Gold "+(gold==null?0:gold),116,100,8);}
  private void drawQuest(Canvas c){panel(c,14,124,214,224);text(c,"Quick Quest",28,146,11);F5mAdaptedPrologueQuest.State qs=f5mQuest.state();if(qs==F5mAdaptedPrologueQuest.State.AVAILABLE){mutedText(c,"밀레스 안내인의 부탁",28,171,9);mutedText(c,"안내인과 대화해 퀘스트 받기",38,192,8);}else if(qs==F5mAdaptedPrologueQuest.State.ACTIVE){mutedText(c,"첫 훈련",28,171,9);mutedText(c,"훈련용 몬스터  "+f5mQuest.currentCount()+"/"+f5mQuest.requiredCount(),38,192,8);}else if(qs==F5mAdaptedPrologueQuest.State.RETURN_READY){mutedText(c,"첫 훈련  1/1",28,171,9);mutedText(c,"밀레스 안내인에게 돌아가기",38,192,8);}else{mutedText(c,"첫 훈련 · 완료",28,171,9);}mutedText(c,"눌러서 퀘스트 목표로 이동",28,214,6.5f);}
  private void drawQuest2(Canvas c){panel(c,14,124,214,224);text(c,"Quick Quest 2",28,146,11);GrowthQuest2.State q=quest2.state();if(q==GrowthQuest2.State.AVAILABLE){mutedText(c,"성장 훈련",28,171,9);mutedText(c,"안내인에게 퀘스트 받기",38,192,8);}else if(q==GrowthQuest2.State.ACTIVE){mutedText(c,"성장 훈련",28,171,9);mutedText(c,"훈련용 몬스터  "+quest2.currentCount()+"/"+quest2.requiredCount(),38,192,8);}else if(q==GrowthQuest2.State.RETURN_READY){mutedText(c,"성장 훈련  3/3",28,171,9);mutedText(c,"안내인에게 돌아가기",38,192,8);}mutedText(c,"보상 EXP 15000 · Gold 250",28,214,6.5f);}
  private void drawNextPurpose(Canvas c){panel(c,14,124,214,224);text(c,PostF5mHudPresentation.purposeTitle(f5mQuest.state()),28,146,11);mutedText(c,"첫 훈련 완료",28,171,9);drawWrappedText(c,PostF5mHudPresentation.purposeBody(f5mQuest.state()),38,192,160,8,13);}
  private void drawTarget(Canvas c){RuntimeState.Monster selected=combat.target();if(selected==null)return;panel(c,380,12,580,55);text(c,selected.name,397,31,9.5f);bar(c,397,39,563,48,0xffd63e49,selected.hp/(float)selected.maxHp);}
  private void drawMinimap(Canvas c){panel(c,810,12,946,112);text(c,"CH. 5-1",846,30,9);p.setColor(0xB7232B1F);c.drawRect(820,38,936,92,p);float nx=(worldAdapter.presentationPlayerX()-WorldDef.MIN_X)/Math.max(1f,WorldDef.MAX_X-WorldDef.MIN_X),ny=(worldAdapter.presentationPlayerY()-WorldDef.MIN_Y)/Math.max(1f,WorldDef.MAX_Y-WorldDef.MIN_Y);float px=823+Math.max(0,Math.min(1,nx))*110,py=41+Math.max(0,Math.min(1,ny))*48;p.setColor(0xffffd44f);c.drawCircle(px,py,3.5f,p);mutedText(c,"밀레스",861,105,8);}
  private void drawUtilityRail(Canvas c){String[] labels={"BAG","STAT","EQUIP","MAIL","≡","⚙"};for(int i=0;i<labels.length;i++){float cx=UTILITY_X0+(i%3)*UTILITY_STEP,cy=UTILITY_Y0+(i/3)*UTILITY_STEP;boolean active=i==0&&inventoryOpen||i==1&&statsOpen||i==2&&equipmentOpen;p.setColor(active?0xD75E3F24:0xA81C1815);c.drawCircle(cx,cy,UTILITY_R,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(active?2f:1f);p.setColor(active?0xffffd477:0x99bb8753);c.drawCircle(cx,cy,UTILITY_R,p);p.setStyle(Paint.Style.FILL);p.setTextSize(labels[i].length()>3?5.8f:7f);p.setColor(active?0xffffefc7:0xffe4d5bc);float tw=p.measureText(labels[i]);c.drawText(labels[i],cx-tw/2,cy+2.5f,p);}}
  private void drawStats(Canvas c){if(!statsOpen)return;modalPanel(c,570,72,936,390);text(c,"CHARACTER STATUS",590,99,13);p.setColor(0xB024262A);c.drawCircle(911,91,15,p);text(c,"×",906,96,14);RpgProgressionState r=state.rpg();FinalStats fs=r.finalStats();RpgProgressionState.StatSnapshot ss=r.recomputeStats();mutedText(c,"Lv. "+r.normalLevel()+"  Gold "+r.gold()+"  POINT "+r.statPoints(),590,121,9);
  String[] names={"STR","INT","WIS","CON","DEX"};int[] base={r.str(),r.intel(),r.wis(),r.con(),r.dex()};for(int n=0;n<5;n++){int y=150+n*30;Integer bonus=ss.equipment.get(names[n]);text(c,names[n]+"  "+base[n]+((bonus!=null&&bonus!=0)?" ("+(bonus>0?"+":"")+bonus+")":""),594,y,10);text(c,"+",720,y,12);}
  mutedText(c,"HP "+state.player().hp+" / "+fs.maxHp,770,150,9);mutedText(c,"MP "+state.player().mp+" / "+fs.maxMp,770,172,9);mutedText(c,"AC "+fs.ac+"   MDEF "+fs.magicDefense,770,204,9);mutedText(c,"HIT "+fs.hit+"   DAM "+fs.dam,770,226,9);mutedText(c,"ATK "+fs.prototypePhysicalAttack(),770,248,9);mutedText(c,"ATK ELEM "+fs.attackElement,770,278,8);mutedText(c,"DEF ELEM "+fs.defenseElement,770,298,8);mutedText(c,"DMG RED "+fs.damageReductionPct+"%  FLAT "+fs.flatMitigation,770,328,8);mutedText(c,"AC IGNORE "+fs.acIgnore,770,348,8);mutedText(c,"Lv1-99: STAT POINT +2 · CON/WIS는 다음 레벨 HP/MP 성장에 반영",590,374,7.5f);}
  private void drawChat(Canvas c){RuntimeMetrics metrics=state.metrics();p.setColor(0x76120f0e);c.drawRoundRect(new RectF(270,418,642,522),6,6,p);text(c,"전체    지역    파티    길드",286,438,8.5f);mutedText(c,"[지역] 밀레스에 오신 것을 환영합니다.",286,460,8);mutedText(c,"[전투] 격파 "+metrics.monsterDefeats()+" · 피격 "+metrics.playerHits()+" · DMG "+metrics.damageDealt(),286,479,8);mutedText(c,"[시스템] NPC · 전투 · 보상 알림",286,498,8);p.setColor(0x7F211B17);c.drawRoundRect(new RectF(282,505,630,518),5,5,p);mutedText(c,"메시지를 입력하세요.",292,516,7);}
  private void drawJoystick(Canvas c){p.setColor(joy?0x523D4652:0x2A2B3038);c.drawCircle(JOY_X,JOY_Y,JOY_R,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(joy?2.2f:1.2f);p.setColor(joy?0xC8DBC386:0x697F7562);c.drawCircle(JOY_X,JOY_Y,JOY_R,p);p.setStrokeWidth(1);p.setColor(0x447F7562);c.drawCircle(JOY_X,JOY_Y,36,p);p.setStyle(Paint.Style.FILL);p.setColor(joy?0xD7C7A86B:0x9A766951);c.drawCircle(knobX,knobY,joy?24:22,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xDCEAD9B0);c.drawCircle(knobX,knobY,joy?24:22,p);p.setStyle(Paint.Style.FILL);}
  private void drawExpStrip(Canvas c){p.setColor(0xB4101112);c.drawRect(0,531,W,540,p);Float ratio=PostF5mHudPresentation.expRatio(state.rpg());if(ratio!=null){p.setColor(0xff46ad45);c.drawRect(0,535,W*Math.max(0f,Math.min(1f,ratio)),540,p);}mutedText(c,PostF5mHudPresentation.expLabel(state.rpg()),216,538,6.5f);}

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

  private void drawInventory(Canvas c){
    if(!inventoryOpen)return;
    p.setColor(0x52000000);c.drawRect(0,0,W,H,p);
    classicWindow(c,420,54,938,476,"인벤토리");
    p.setColor(0xB02A1710);c.drawCircle(913,76,15,p);text(c,"×",908,81,14);
    List<RpgInventoryPresentation.ItemRow> rows=rpgPresentation.inventoryRows(state.rpg());
    String selectedId=rpgInteraction.selectedInventoryItemId();
    int pageSize=20,pages=Math.max(1,(rows.size()+pageSize-1)/pageSize);
    inventoryPage=Math.max(0,Math.min(inventoryPage,pages-1));
    int start=inventoryPage*pageSize,limit=Math.min(pageSize,Math.max(0,rows.size()-start));
    // Item-only grid: names stay hidden until selection, matching the mobile reference.
    float gx=440,gy=105,cell=62,gap=5;
    for(int i=0;i<20;i++){
      int col=i%4,row=i/4;float l=gx+col*(cell+gap),t=gy+row*(cell+gap);
      p.setColor(0xD0191411);c.drawRect(l,t,l+cell,t+cell,p);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xFF765335);c.drawRect(l+.5f,t+.5f,l+cell-.5f,t+cell-.5f,p);p.setStyle(Paint.Style.FILL);
      if(i<limit){
        RpgInventoryPresentation.ItemRow item=rows.get(start+i);
        boolean selected=item.itemId.equals(selectedId);
        if(selected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(0xFFE2A84E);c.drawRect(l+2,t+2,l+cell-2,t+cell-2,p);p.setStyle(Paint.Style.FILL);}
        drawInventoryItemIcon(c,item,l+7,t+7,cell-14);
        if(item.quantity>1){p.setColor(0xCC080706);c.drawRoundRect(new RectF(l+37,t+43,l+59,t+59),5,5,p);text(c,String.valueOf(item.quantity),l+41,t+56,8);}
        if(item.equipped){p.setColor(0xFF6DBD62);c.drawRect(l+3,t+3,l+14,t+14,p);text(c,"E",l+5,t+12,7);}
      }
    }
    // Right-side detail card only appears after tapping an item.
    p.setColor(0xE0161210);c.drawRect(716,105,918,401,p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.4f);p.setColor(0xFF8B6039);c.drawRect(716.5f,105.5f,917.5f,400.5f,p);p.setStyle(Paint.Style.FILL);
    RpgInventoryPresentation.ItemRow selectedRow=null;
    if(selectedId!=null)for(RpgInventoryPresentation.ItemRow row:rows)if(selectedId.equals(row.itemId)){selectedRow=row;break;}
    if(selectedRow==null){mutedText(c,"아이템을 선택하세요",752,250,10);}
    else{
      RpgProgressionState.ItemDefinition d=state.rpg().itemDefinitions().get(selectedRow.itemId);
      text(c,selectedRow.name,734,127,10.5f);
      mutedText(c,rpgPresentation.requirementLabel(selectedRow),734,143,7.2f);
      if(d!=null&&d.equippable()){
        RpgProgressionState.ItemDefinition current=state.rpg().equippedDefinition(d.equipSlot);
        drawCompareCard(c,"현재 장착",current,734,154,78,128);
        drawCompareCard(c,"선택 장비",d,822,154,78,128);
        drawStatDeltaPanel(c,current,d,734,289,166);
      }else{
        drawInventoryItemIcon(c,selectedRow,752,164,72);
        drawItemStatPanel(c,d,734,252,166);
      }
      String elem=rpgPresentation.elementLabel(selectedRow);if(!elem.isEmpty())mutedText(c,elem,734,337,7.2f);
      p.setColor(0xD06A431E);c.drawRoundRect(new RectF(748,354,886,392),5,5,p);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xFFE0AD62);c.drawRoundRect(new RectF(748,354,886,392),5,5,p);p.setStyle(Paint.Style.FILL);
      text(c,state.rpg().isConsumable(selectedRow.itemId)?"사용":selectedRow.equipped?"해제":"장착",799,378,11);
    }
    if(inventoryPage>0)text(c,"‹",555,457,18);
    mutedText(c,(inventoryPage+1)+" / "+pages,616,456,9);
    if(inventoryPage+1<pages)text(c,"›",682,457,18);
  }

  private void classicWindow(Canvas c,float l,float t,float r,float b,String title){
    p.setColor(0xF02A1D16);c.drawRect(l,t,r,b,p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(0xFF4B2D1D);c.drawRect(l+2,t+2,r-2,b-2,p);
    p.setStrokeWidth(1.4f);p.setColor(0xFFD0A066);c.drawRect(l+7,t+7,r-7,b-7,p);p.setStyle(Paint.Style.FILL);
    p.setColor(0xFF3B271B);c.drawRect(l+10,t+10,r-10,t+42,p);text(c,title,l+25,t+33,14);
  }
  private void drawInventoryItemIcon(Canvas c,RpgInventoryPresentation.ItemRow row,float l,float t,float size){
    RpgProgressionState.ItemDefinition d=state.rpg().itemDefinitions().get(row.itemId);
    if(d!=null&&drawSourceItemIcon(c,d,l,t,size))return;
    drawFallbackItemIcon(c,d,l,t,size);
  }
  private boolean drawSourceItemIcon(Canvas c,RpgProgressionState.ItemDefinition d,float l,float t,float size){
    if(d==null||d.appearanceId==null||d.appearanceId.isEmpty())return false;
    EquipmentVisualRegistry.Visual v=inventoryVisuals.get(d.appearanceId);
    if(v==null||v.atlas==null||v.registration==null)return false;
    SourceEquipmentRegistration.Frame frame=v.registration.idle(CharacterRenderer.Direction.SW,0);
    if(frame==null||frame.src==null||frame.src.width()<=0||frame.src.height()<=0)return false;
    Rect src=new Rect(frame.src);src.left=Math.max(0,src.left);src.top=Math.max(0,src.top);src.right=Math.min(v.atlas.getWidth(),src.right);src.bottom=Math.min(v.atlas.getHeight(),src.bottom);
    if(src.width()<=0||src.height()<=0)return false;
    float pad=Math.max(2f,size*.08f),avail=size-pad*2f,sc=Math.min(avail/src.width(),avail/src.height());
    float dw=Math.max(1,src.width()*sc),dh=Math.max(1,src.height()*sc),dx=l+(size-dw)/2f,dy=t+(size-dh)/2f;
    Paint old=p;p.setFilterBitmap(false);c.drawBitmap(v.atlas,src,new RectF(dx,dy,dx+dw,dy+dh),p);return true;
  }
  private void drawFallbackItemIcon(Canvas c,RpgProgressionState.ItemDefinition d,float l,float t,float size){
    String slot=d==null?"":d.equipSlot;float cx=l+size/2,cy=t+size/2;
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,size*.07f));p.setColor(0xFFE5D2A5);
    if("무기".equals(slot)){c.drawLine(l+size*.25f,t+size*.78f,l+size*.73f,t+size*.22f,p);c.drawLine(l+size*.19f,t+size*.62f,l+size*.39f,t+size*.82f,p);}
    else if("방패".equals(slot)){Path q=new Path();q.moveTo(cx,t+size*.15f);q.lineTo(l+size*.82f,t+size*.28f);q.lineTo(l+size*.72f,t+size*.72f);q.lineTo(cx,t+size*.88f);q.lineTo(l+size*.28f,t+size*.72f);q.lineTo(l+size*.18f,t+size*.28f);q.close();c.drawPath(q,p);}
    else if("모자".equals(slot)){c.drawArc(new RectF(l+size*.2f,t+size*.28f,l+size*.8f,t+size*.78f),190,160,false,p);c.drawLine(l+size*.18f,t+size*.66f,l+size*.82f,t+size*.66f,p);}
    else if("신발".equals(slot)||"각반".equals(slot)){c.drawLine(l+size*.38f,t+size*.2f,l+size*.38f,t+size*.7f,p);c.drawLine(l+size*.38f,t+size*.7f,l+size*.75f,t+size*.78f,p);}
    else if("장갑".equals(slot)){c.drawCircle(cx,cy,size*.24f,p);for(int i=-2;i<=2;i++)c.drawLine(cx+i*size*.08f,cy-size*.18f,cx+i*size*.08f,t+size*.18f,p);}
    else if("귀걸이".equals(slot)||"목걸이".equals(slot)){c.drawCircle(cx,cy,size*.25f,p);c.drawCircle(cx,cy+size*.25f,size*.07f,p);}
    else if("벨트".equals(slot)){c.drawRect(l+size*.15f,cy-size*.1f,l+size*.85f,cy+size*.1f,p);c.drawRect(cx-size*.12f,cy-size*.16f,cx+size*.12f,cy+size*.16f,p);}
    else {c.drawCircle(cx,cy,size*.24f,p);c.drawLine(cx,cy-size*.38f,cx,cy+size*.38f,p);}p.setStyle(Paint.Style.FILL);
  }
  private void drawCompareCard(Canvas c,String title,RpgProgressionState.ItemDefinition d,float x,float y,float w,float h){
    p.setColor(0xCC241A14);c.drawRoundRect(new RectF(x,y,x+w,y+h),5,5,p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xFF6E4C31);c.drawRoundRect(new RectF(x+.5f,y+.5f,x+w-.5f,y+h-.5f),5,5,p);p.setStyle(Paint.Style.FILL);
    mutedText(c,title,x+6,y+14,6.5f);
    if(d==null){mutedText(c,"없음",x+25,y+70,8);return;}
    RpgInventoryPresentation.ItemRow row=null;for(RpgInventoryPresentation.ItemRow r:rpgPresentation.inventoryRows(state.rpg()))if(r.itemId.equals(d.itemId)){row=r;break;}
    if(row!=null)drawInventoryItemIcon(c,row,x+18,y+22,42);
    fittedText(c,RpgInventoryPresentation.displayName(d.name),x+5,y+78,w-10,7.5f);
    String mods=itemModifierLabel(d);drawWrappedText(c,mods.isEmpty()?"추가 능력치 없음":mods,x+5,y+94,w-10,6.5f,10);
  }
  private void drawStatDeltaPanel(Canvas c,RpgProgressionState.ItemDefinition current,RpgProgressionState.ItemDefinition next,float x,float y,float width){
    String[] keys={"DAM","HIT","AC","DEX","STR","INT","WIS","CON","MDEF"};int shown=0;
    for(String key:keys){int a=mod(current,key),b=mod(next,key);if(a==0&&b==0)continue;int delta=b-a;float yy=y+shown*16;
      mutedText(c,key,x,yy+10,6.5f);mutedText(c,a+" → "+b,x+38,yy+10,7.2f);if(delta!=0)text(c,(delta>0?"+":"")+delta,x+112,yy+10,7.5f);shown++;if(shown>=3)break;}
    if(shown==0)mutedText(c,"장비 능력치 변화 없음",x,y+11,7.2f);
  }
  private void drawItemStatPanel(Canvas c,RpgProgressionState.ItemDefinition d,float x,float y,float width){
    if(d==null){mutedText(c,"능력치 정보 없음",x,y+12,8);return;}
    String[] keys={"DAM","HIT","AC","DEX","STR","INT","WIS","CON","MDEF"};
    int shown=0;
    for(String key:keys){
      Integer v=d.statModifiers.get(key);if(v==null||v==0)continue;
      float col=shown%2,row=shown/2, bx=x+col*(width/2),by=y+row*27;
      p.setColor(0xCC2B2119);c.drawRoundRect(new RectF(bx,by,bx+width/2-5,by+22),4,4,p);
      mutedText(c,key,bx+6,by+14,7);text(c,(v>0?"+":"")+v,bx+42,by+15,9);shown++;
      if(shown>=6)break;
    }
    if(shown==0)mutedText(c,d.equippable()?"추가 능력치 없음":"소비/기타 아이템",x,y+14,8);
  }
  private String itemModifierLabel(RpgProgressionState.ItemDefinition d){if(d==null||d.statModifiers.isEmpty())return "";StringBuilder s=new StringBuilder();for(Map.Entry<String,Integer> e:d.statModifiers.entrySet()){if(s.length()>0)s.append(" · ");s.append(e.getKey()).append(" ").append(e.getValue()>0?"+":"").append(e.getValue());}return s.toString();}
  private String equipmentCompareLabel(RpgProgressionState.ItemDefinition d){if(d==null||!d.equippable())return "";FinalStats now=state.rpg().finalStats();Map<String,String> eq=state.rpg().equipment();RpgProgressionState.ItemDefinition old=state.rpg().itemDefinitions().get(eq.get(d.equipSlot));int dam=now.dam-oldMod(old,"DAM")+mod(d,"DAM"),hit=now.hit-oldMod(old,"HIT")+mod(d,"HIT"),ac=now.ac-oldMod(old,"AC")+mod(d,"AC"),dex=now.dex-oldMod(old,"DEX")+mod(d,"DEX");if("무기".equals(d.equipSlot))return "장착 시 DAM "+now.dam+"→"+dam+" · HIT "+now.hit+"→"+hit;if("방패".equals(d.equipSlot)||"갑옷".equals(d.equipSlot)||"모자".equals(d.equipSlot)||"장갑".equals(d.equipSlot))return "장착 시 AC "+now.ac+"→"+ac;if("신발".equals(d.equipSlot))return "장착 시 DEX "+now.dex+"→"+dex;return "장착 시 최종 능력치에 즉시 반영";}
  private static int mod(RpgProgressionState.ItemDefinition d,String k){if(d==null)return 0;Integer v=d.statModifiers.get(k);return v==null?0:v;} private static int oldMod(RpgProgressionState.ItemDefinition d,String k){return mod(d,k);}
  private void drawEquipment(Canvas c){
    if(!equipmentOpen)return;
    p.setColor(0x52000000);c.drawRect(0,0,W,H,p);
    classicWindow(c,110,48,850,478,"Equip");
    p.setColor(0xB02A1710);c.drawCircle(825,70,15,p);text(c,"×",820,75,14);
    Map<String,String> eq=state.rpg().equipment();
    // Presentation-only slot map. No equipment domain/state is changed.
    String[] slots={"귀걸이","모자","목걸이","날개","방패","무기","장갑","장갑","벨트","각반","신발"};
    float[][] pos={{155,105},{286,88},{417,105},{155,188},{155,270},{417,188},{125,350},{447,350},{417,270},{245,374},{326,374}};
    for(int i=0;i<slots.length;i++)drawEquipSlot(c,eq,slots[i],pos[i][0],pos[i][1],74,66,i==6?"장갑(좌)":i==7?"장갑(우)":slots[i]);
    // Current paper-doll uses the exact runtime visual binding already used in world rendering.
    CharacterVisualBinding visuals=CharacterVisualBinding.from(state.rpg());
    characterRenderer.draw(c,new CharacterRenderer.Pose(324,310,CharacterRenderer.Direction.SW,CharacterRenderer.State.IDLE,0,0,1,false,visuals.equipmentVisualRef(),visuals.weaponVisualRef(),CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));
    RpgProgressionState r=state.rpg();FinalStats fs=r.finalStats();
    p.setColor(0xFF1B120E);c.drawRect(530,104,820,438,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);p.setColor(0xFF8A603B);c.drawRect(530.5f,104.5f,819.5f,437.5f,p);p.setStyle(Paint.Style.FILL);
    text(c,"Presentation",548,130,12);mutedText(c,"Name",548,158,8);text(c,"주찬",620,158,9);mutedText(c,"Class",548,181,8);text(c,"평민",620,181,9);
    p.setColor(0xFF5A3C28);c.drawRect(548,200,802,201,p);
    text(c,"STR  "+r.str(),550,230,9);text(c,"WIS  "+r.wis(),650,230,9);text(c,"DEX  "+r.dex(),750,230,9);
    text(c,"INT  "+r.intel(),550,255,9);text(c,"CON  "+r.con(),650,255,9);text(c,"AC  "+fs.ac,750,255,9);
    mutedText(c,"HP  "+state.player().hp+" / "+fs.maxHp,550,294,9);mutedText(c,"MP  "+state.player().mp+" / "+fs.maxMp,550,317,9);
    mutedText(c,"HIT  "+fs.hit+"   DAM  "+fs.dam,550,350,9);
    mutedText(c,"장비창은 표시 전용 · 장착/해제는 인벤토리에서",550,416,7.5f);
  }
  private void drawEquipSlot(Canvas c,Map<String,String> eq,String slot,float l,float t,float w,float h,String label){
    p.setColor(0xE018120F);c.drawRect(l,t,l+w,t+h,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xFF765335);c.drawRect(l+.5f,t+.5f,l+w-.5f,t+h-.5f,p);p.setStyle(Paint.Style.FILL);
    String id=eq.get(slot);RpgProgressionState.ItemDefinition d=id==null?null:state.rpg().itemDefinitions().get(id);
    mutedText(c,label,l+4,t+11,6.5f);
    if(d!=null){RpgInventoryPresentation.ItemRow found=null;for(RpgInventoryPresentation.ItemRow row:rpgPresentation.inventoryRows(state.rpg()))if(row.itemId.equals(id)){found=row;break;}if(found!=null)drawInventoryItemIcon(c,found,l+16,t+16,Math.min(w-24,h-20));}
  }
  private String equipResultLabel(RpgProgressionState.EquipResult result){switch(result){case EQUIPPED:return "장착 완료";case UNEQUIPPED:return "해제 완료";case REQUIREMENT_NOT_MET:return "장착 불가 · 조건 미충족";case REQUIREMENT_PENDING:return "장착 보류 · 조건 확인 필요";case NOT_EQUIPPABLE:return "장착할 수 없는 아이템";case UNKNOWN_ITEM:return "알 수 없는 아이템";case ITEM_NOT_OWNED:default:return "보유하지 않은 아이템";}}
  private boolean handleInventoryTouch(float x,float y){
    if(!inventoryOpen)return false;
    if(dist(x,y,913,76)<=24){inventoryOpen=false;showFeedback("인벤토리 닫힘",FeedbackTone.INFO);return true;}
    List<RpgInventoryPresentation.ItemRow> rows=rpgPresentation.inventoryRows(state.rpg());
    int pageSize=20,pages=Math.max(1,(rows.size()+pageSize-1)/pageSize);inventoryPage=Math.max(0,Math.min(inventoryPage,pages-1));
    if(y>=430&&y<=472){if(x>=530&&x<590&&inventoryPage>0){inventoryPage--;return true;}if(x>=660&&x<=710&&inventoryPage+1<pages){inventoryPage++;return true;}}
    float gx=440,gy=105,cell=62,gap=5;
    if(x>=gx&&x<=gx+4*cell+3*gap&&y>=gy&&y<=gy+5*cell+4*gap){
      int col=(int)((x-gx)/(cell+gap)),row=(int)((y-gy)/(cell+gap));float lx=gx+col*(cell+gap),ty=gy+row*(cell+gap);
      if(x<=lx+cell&&y<=ty+cell){int index=inventoryPage*pageSize+row*4+col;if(index<rows.size()){RpgInventoryPresentation.ItemRow item=rows.get(index);if(rpgInteraction.selectInventoryItem(state.rpg(),item.itemId))showFeedback(item.name+" 선택",FeedbackTone.INFO);}return true;}
    }
    if(x>=748&&x<=886&&y>=354&&y<=392){
      String selected=rpgInteraction.selectedInventoryItemId();
      if(selected!=null&&state.rpg().isConsumable(selected)){RpgProgressionState.UseResult used=state.rpg().useConsumable(selected,state);checkpoint();showFeedback(used==RpgProgressionState.UseResult.USED?"회복물약 사용 · HP +"+RpgProgressionState.B5_SMALL_HP_POTION_HEAL:used==RpgProgressionState.UseResult.NO_EFFECT?"HP가 이미 가득 찼습니다":"사용할 수 없습니다",used==RpgProgressionState.UseResult.USED?FeedbackTone.REWARD:FeedbackTone.WARN);return true;}
      RpgProgressionState.EquipResult result=rpgInteraction.equipSelectedDetailed(state.rpg());state.applyDerivedGrowth();checkpoint();showFeedback(equipResultLabel(result),(result==RpgProgressionState.EquipResult.EQUIPPED||result==RpgProgressionState.EquipResult.UNEQUIPPED)?FeedbackTone.INFO:FeedbackTone.WARN);return true;
    }
    return inside(x,y,420,54,938,476);
  }

  private boolean f5mGuideDialogue(){RuntimeState.Npc n=interaction.dialogNpc();return n!=null&&"milles_guide_proto".equals(n.id);}
  private boolean supplyDialogue(){RuntimeState.Npc n=interaction.dialogNpc();return n!=null&&"milles_service_proto".equals(n.id);}
  private void drawDialogue(Canvas c){RuntimeState.Npc dialogNpc=interaction.dialogNpc();if(dialogNpc==null)return;p.setColor(0x74000000);c.drawRect(0,0,W,H,p);modalPanel(c,190,300,770,462);p.setColor(0xC95A472A);c.drawRoundRect(new RectF(210,316,338,345),8,8,p);text(c,dialogNpc.name,226,335,11.5f);if(f5mGuideDialogue()){F5mAdaptedPrologueQuest.State qs=f5mQuest.state();if(qs==F5mAdaptedPrologueQuest.State.COMPLETED&&quest2.state()!=GrowthQuest2.State.LOCKED&&quest2.state()!=GrowthQuest2.State.COMPLETED){GrowthQuest2.State q2=quest2.state();String body=q2==GrowthQuest2.State.AVAILABLE?"좋아. 이번에는 훈련용 몬스터 세 마리를 상대하며 성장해 보게.":q2==GrowthQuest2.State.ACTIVE?"성장 훈련 중이군. 세 마리를 모두 처치하고 돌아오게.":"충분히 성장했군. 완료를 눌러 보상을 받게.";drawWrappedText(c,body,218,372,510,11,18);if(q2==GrowthQuest2.State.AVAILABLE||q2==GrowthQuest2.State.RETURN_READY){p.setColor(0xD05B4727);c.drawRoundRect(new RectF(500,408,606,442),9,9,p);text(c,q2==GrowthQuest2.State.AVAILABLE?"수락":"완료",538,430,10);}}else{String body=qs==F5mAdaptedPrologueQuest.State.AVAILABLE?"마을 동쪽의 훈련용 몬스터 한 마리를 상대해 보겠나?":qs==F5mAdaptedPrologueQuest.State.ACTIVE?"훈련용 몬스터를 처치하고 다시 찾아오게.":qs==F5mAdaptedPrologueQuest.State.RETURN_READY?"잘 해냈군. 완료를 눌러 보상을 받게.":"첫 훈련은 완료되었네.";drawWrappedText(c,body,218,372,510,11,18);if(qs==F5mAdaptedPrologueQuest.State.AVAILABLE){p.setColor(0xD05B4727);c.drawRoundRect(new RectF(500,408,606,442),9,9,p);text(c,"수락",538,430,10);p.setColor(0xA0343030);c.drawRoundRect(new RectF(620,408,726,442),9,9,p);text(c,"거절",658,430,10);}else if(qs==F5mAdaptedPrologueQuest.State.RETURN_READY){p.setColor(0xD05B4727);c.drawRoundRect(new RectF(500,408,606,442),9,9,p);text(c,"완료",538,430,10);}}}else if(supplyDialogue()){drawWrappedText(c,"아이템 상점 [B]  물약 20G · 목도 120G · 가죽장갑 80G · 신발 60G",218,366,510,10,16);mutedText(c,"보유 Gold "+state.rpg().gold(),218,389,9);String[] sn={"물약","목도","장갑","신발"};for(int i=0;i<4;i++){float bx=218+i*128;p.setColor(0xD05B4727);c.drawRoundRect(new RectF(bx,408,bx+112,442),9,9,p);text(c,sn[i],bx+36,430,9);}}else{drawWrappedText(c,dialogNpc.dialogue,218,372,510,11,18);mutedText(c,"화면을 터치하면 닫기",616,446,8);}p.setColor(0xB024262A);c.drawCircle(744,321,14,p);text(c,"×",739,326,14);}
  private void drawDeath(Canvas c){if(state.player().alive)return;p.setColor(0xB6000000);c.drawRect(0,0,W,H,p);modalPanel(c,330,205,630,335);text(c,"행동 불능",428,242,18);mutedText(c,"프로토타입 부활",432,270,9.5f);p.setColor(0xD05B4727);c.drawRoundRect(new RectF(405,286,555,320),10,10,p);text(c,"화면 중앙 터치",433,308,10.5f);}
  private void fittedText(Canvas c,String value,float x,float y,float width,float size){
    p.setTextSize(size);String shown=value;
    if(p.measureText(shown)>width){while(shown.length()>0&&p.measureText(shown+"…")>width)shown=shown.substring(0,shown.length()-1);shown+="…";}
    text(c,shown,x,y,size);
  }
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
  private void autoNavigateQuest2(){worldAdapter.cancelForAction();interaction.cancelApproach();combat.cancelApproach();if(quest2.state()==GrowthQuest2.State.AVAILABLE||quest2.state()==GrowthQuest2.State.RETURN_READY){RuntimeState.Npc npc=findNpc("milles_guide_proto");if(npc!=null)worldAdapter.requestNpcApproach(npc.id);return;}if(quest2.state()==GrowthQuest2.State.ACTIVE){RuntimeState.Monster m=findMonster("combat_dummy_01");if(m==null)m=findMonster("combat_dummy_02");if(m==null)m=findMonster("combat_dummy_03");if(m!=null){combat.selectTarget(m);worldAdapter.requestMonsterApproach(m.id,48f);}else showFeedback("훈련 목표를 모두 처치했습니다 · 안내인에게 돌아가세요",FeedbackTone.INFO);}}
  private void requestGroundMove(float x,float y){WorldMoveTargetController.Snapshot move=worldAdapter.requestGroundScreenTap(x,y);lastMoveRequestId=move.requestId;lastMoveStatus=WorldMoveTargetController.Status.IDLE;if(move.status==WorldMoveTargetController.Status.MOVING||move.status==WorldMoveTargetController.Status.REACHED){tapMarkerX=move.targetX;tapMarkerY=move.targetY;tapMarkerClock=.7f;showFeedback(move.replacedRequestId>0?"이동 목표 변경":"이동 시작",FeedbackTone.INFO);}consumeMoveOutcome(move);}

  public boolean onTouchEvent(MotionEvent e){float x=(e.getX()-ox)/scale,y=(e.getY()-oy)/scale;if(inReagentShop)return handleReagentShopTouch(e,x,y);switch(e.getActionMasked()){
    case MotionEvent.ACTION_DOWN:
      if(statsOpen){
        if(circleHit(x,y,UTILITY_X0+UTILITY_STEP,UTILITY_Y0,UTILITY_R+4)||circleHit(x,y,911,91,18)){statsOpen=false;showFeedback("스탯창 닫힘",FeedbackTone.INFO);return true;}
        String st=inside(x,y,700,132,754,158)?"STR":inside(x,y,700,162,754,188)?"INT":inside(x,y,700,192,754,218)?"WIS":inside(x,y,700,222,754,248)?"CON":inside(x,y,700,252,754,278)?"DEX":null;
        if(st!=null){if(state.rpg().spendStat(st)){state.applyDerivedGrowth();F5mSaveStore.saveRewardsActive(state.rpg());showFeedback(st+" +1 · 성장 반영",FeedbackTone.REWARD);}else showFeedback("STAT POINT가 부족합니다",FeedbackTone.WARN);return true;}
        if(!inside(x,y,570,72,936,390)){statsOpen=false;showFeedback("스탯창 닫힘",FeedbackTone.INFO);}return true;
      }
      if(!state.player().alive){if(dist(x,y,480,270)<=180){state.revivePlayer();combat.clearTarget();interaction.cancel();worldAdapter.cancel();worldAdapter.snapCameraToPlayer();action=Action.IDLE;checkpoint();}return true;}
      if(interaction.dialogOpen()){if(supplyDialogue()&&y>=408&&y<=442){String item=null,name=null;long price=0;if(x>=218&&x<=330){item=RpgProgressionState.B_SMALL_POTION_ITEM_ID;name="소형 회복물약";price=20;}else if(x>=346&&x<=458){item=RpgProgressionState.SHOP_MOKDO_ITEM_ID;name="목도";price=120;}else if(x>=474&&x<=586){item=RpgProgressionState.SHOP_LEATHER_GLOVE_ITEM_ID;name="가죽장갑";price=80;}else if(x>=602&&x<=714){item=RpgProgressionState.SHOP_SHOES_ITEM_ID;name="신발";price=60;}if(item!=null){if(state.rpg().buyItem(item,price)){checkpoint();showReward(name+" 구매 · Gold -"+price);}else showFeedback("구매 불가 · Gold/아이템 상태 확인",FeedbackTone.WARN);return true;}}if(f5mGuideDialogue()&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.COMPLETED&&quest2.state()==GrowthQuest2.State.AVAILABLE&&inside(x,y,500,408,606,442)){if(quest2.accept()){showFeedback("퀘스트 수락 · 성장 훈련",FeedbackTone.INFO);interaction.dismissDialog();}return true;}if(f5mGuideDialogue()&&quest2.state()==GrowthQuest2.State.RETURN_READY&&inside(x,y,500,408,606,442)){if(quest2.turnIn(state.rpg())){state.applyDerivedGrowth();F5mSaveStore.saveRewardsActive(state.rpg());showReward("성장 훈련 완료 · EXP +15000 · Gold +250");interaction.dismissDialog();}return true;}if(f5mGuideDialogue()&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.RETURN_READY&&inside(x,y,500,408,606,442)){F5mTurnInCoordinator.Result tr=F5mQuestUiFlow.confirmTurnIn(f5mQuest,state.rpg());String msg=F5mQuestUiFlow.completionMessage(tr);if(tr==F5mTurnInCoordinator.Result.COMPLETED){state.applyDerivedGrowth();checkpoint();showReward(msg);interaction.dismissDialog();worldAdapter.cancelForAction();}else showFeedback(msg,tr==F5mTurnInCoordinator.Result.ALREADY_COMPLETED?FeedbackTone.INFO:FeedbackTone.WARN);return true;}if(f5mGuideDialogue()&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.AVAILABLE&&inside(x,y,500,408,606,442)){F5mAdaptedPrologueQuest.AcceptResult r=f5mQuest.accept();if(r==F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED){interaction.dismissDialog();checkpoint();}showFeedback(r==F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED?"퀘스트 수락 · 첫 훈련":"퀘스트 상태 유지",FeedbackTone.INFO);return true;}if(f5mGuideDialogue()&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.AVAILABLE&&inside(x,y,620,408,726,442)){f5mQuest.decline();interaction.dismissDialog();worldAdapter.cancelForAction();showFeedback("퀘스트 거절 · 다시 대화할 수 있습니다",FeedbackTone.INFO);return true;}interaction.dismissDialog();worldAdapter.cancelForAction();showFeedback("대화 종료",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0,UTILITY_Y0,UTILITY_R+4)){inventoryOpen=!inventoryOpen;if(inventoryOpen){statsOpen=false;equipmentOpen=false;}showFeedback(inventoryOpen?"인벤토리 열림":"인벤토리 닫힘",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0+UTILITY_STEP,UTILITY_Y0,UTILITY_R+4)){statsOpen=!statsOpen;if(statsOpen){inventoryOpen=false;equipmentOpen=false;}showFeedback(statsOpen?"스탯창 열림":"스탯창 닫힘",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0+UTILITY_STEP*2,UTILITY_Y0,UTILITY_R+4)){equipmentOpen=!equipmentOpen;if(equipmentOpen){inventoryOpen=false;statsOpen=false;}showFeedback(equipmentOpen?"장비창 열림":"장비창 닫힘",FeedbackTone.INFO);return true;}if(equipmentOpen){if(dist(x,y,911,91)<=24){equipmentOpen=false;return true;}return inside(x,y,548,72,936,444);}if(handleInventoryTouch(x,y))return true;
      if(inside(x,y,14,124,214,224)){ActiveQuestTracker.Quest tracked=ActiveQuestTracker.current(f5mQuest,quest2);if(tracked==ActiveQuestTracker.Quest.PROLOGUE){autoNavigateQuest();return true;}if(tracked==ActiveQuestTracker.Quest.GROWTH_2){autoNavigateQuest2();return true;}}
      if(circleHit(x,y,JOY_X,JOY_Y,JOY_R)){joy=true;directStepClock=0f;pressedControl="JOY";worldAdapter.cancelForDirectInput();interaction.cancelApproach();combat.cancelApproach();stick(x,y);return true;}
      if(slotRect(0).contains(x,y)){pressedControl="SKILL";skill();return true;}
      if(slotRect(1).contains(x,y)){pressedControl="MAG";cast();return true;}
      if(slotRect(2).contains(x,y)){pressedControl="KICK";kick();return true;}
      for(int i=3;i<10;i++)if(slotRect(i).contains(x,y)){pressedControl="SLOT"+i;showFeedback("퀵슬롯 준비 중",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,MODE_X,MODE_Y,MODE_R+3)){pressedControl="MODE";worldAdapter.cancelForAction();showFeedback("기본 공격은 장착한 무기에 따라 결정됩니다",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,ATK_X,ATK_Y,ATK_R+4)){pressedControl="ATK";attack();return true;}
      if(circleHit(x,y,AUTO_X,AUTO_Y,AUTO_R+3)){pressedControl="AUTO";showFeedback("AUTO 준비 중",FeedbackTone.WARN);return true;}
      if(isHudSurface(x,y))return true;
      WorldCameraTransform.Point wp=worldAdapter.screenToWorld(x,y);RuntimeState.Npc npc=state.hitNpc(wp.x,wp.y,34f);if(npc!=null){combat.cancelApproach();interaction.cancelApproach();worldAdapter.requestNpcApproach(npc.id);showFeedback("NPC 접근 · "+npc.name,FeedbackTone.INFO);return true;}RuntimeState.Monster monster=state.hitMonster(wp.x,wp.y,34f);if(monster!=null){worldAdapter.cancelForAction();combat.selectTarget(monster);interaction.cancelApproach();showFeedback("타깃 선택 · "+monster.name,FeedbackTone.INFO);return true;}requestGroundMove(x,y);return true;
    case MotionEvent.ACTION_MOVE:if(joy)stick(x,y);return true;
    case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:joy=false;directStepClock=0f;pressedControl="";knobX=JOY_X;knobY=JOY_Y;vx=vy=0;return true;
  }return true;}
  private void stick(float x,float y){if(isActing()||!state.player().alive)return;float dx=x-JOY_X,dy=y-JOY_Y,len=(float)Math.sqrt(dx*dx+dy*dy);if(len>JOY_R){dx=dx/len*JOY_R;dy=dy/len*JOY_R;len=JOY_R;}knobX=JOY_X+dx;knobY=JOY_Y+dy;if(len<8){vx=vy=0;return;}float nx=dx/len,ny=dy/len;if(Math.abs(nx)>Math.abs(ny)){if(nx>0){vx=.707f;vy=.707f;}else{vx=-.707f;vy=-.707f;}}else{if(ny>0){vx=-.707f;vy=.707f;}else{vx=.707f;vy=-.707f;}}playerFacing.updateLocomotion(vx,vy);}
  private static boolean inside(float x,float y,float l,float t,float r,float b){return x>=l&&x<=r&&y>=t&&y<=b;}
  private static boolean circleHit(float x,float y,float cx,float cy,float r){float dx=x-cx,dy=y-cy;return dx*dx+dy*dy<=r*r;}
  private float dist(float a,float b,float c,float d){float x=a-c,y=b-d;return(float)Math.sqrt(x*x+y*y);}
}
