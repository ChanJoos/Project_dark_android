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
import com.projectdark.mobile.WorldEntityPresentationRenderer;
import com.projectdark.mobile.world.ReagentShopInteriorDef;
import com.projectdark.mobile.world.TownInteriorDef;
import com.projectdark.mobile.world.TownInteriorRenderer;
import com.projectdark.mobile.world.ReagentShopInteriorRenderer;
import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.PoteFieldRenderer;
import java.util.List;
import java.util.Collections;
import java.util.Map;

/** PROJECT DARK v0.74 - original-inspired mobile HUD adaptation + precise world tap routing. */
public final class GameView extends View {
  private static final float W=960f,H=540f;
  private static final float JOY_X=92f,JOY_Y=454f,JOY_R=58f;
  private static final float SLOT=42f,SLOT_GAP=8f,SLOT_X0=650f,SLOT_Y0=374f;
  private static final float ATK_X=914f,ATK_Y=498f,ATK_R=34f;
  private static final float CHAT_LEFT=288f,CHAT_RIGHT=624f;
  private static final float MODE_X=778f,MODE_Y=495f,MODE_R=22f;
  private static final float AUTO_X=842f,AUTO_Y=495f,AUTO_R=24f;
  static final float AUTO_TARGET_RADIUS=256f;
  private static final float UTILITY_X0=608f,UTILITY_Y0=28f,UTILITY_STEP=50f,UTILITY_R=16f;

  private enum Action { IDLE,WALK,CAST,SWING,THRUST,THROW,PUNCH,SKILL,KICK }
  private enum FeedbackTone { INFO,WARN,REWARD }

  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RuntimeState state=new RuntimeState();
  private final WorldRuntimeAdapter worldAdapter=new WorldRuntimeAdapter(state,W,H);
  private WorldCameraTransform camera=worldAdapter.camera();
  private final WorldMoveTargetController moveTarget=worldAdapter.movement();
  private final AdaptedMillesMapRenderer mapRenderer=new AdaptedMillesMapRenderer();
  private final PoteFieldRenderer poteFieldRenderer=new PoteFieldRenderer();
  private WorldRuntimeAdapter poteFieldAdapter;
  private boolean inPoteField=false;
  private float fieldReturnX=WorldDef.PLAYER_SPAWN_X,fieldReturnY=WorldDef.PLAYER_SPAWN_Y;
  private final CombatController combat=new CombatController();
  private final SkillBook skillBook;
  private final SkillWindow skillWindow;
  private final EquipmentActionResolver equipmentActions=new EquipmentActionResolver();
  private final RuntimeCombatSession combatSession=new RuntimeCombatSession(state,
      (actor,target)->activeWorld().hasCombatLineOfSight(actor,target),(actor,id)->RuntimeCombatSession.PLAYER_ID.equals(actor)&&isSkillLearned(id),actor->true);
  private final MonsterAIController monsterAi=new MonsterAIController(
      new MonsterAIController.SharedResolverAttackRouter(combatSession.monsterAutoBridge()));
  private final InteractionController interaction=new InteractionController();
  private final CharacterRenderer characterRenderer=new CharacterRenderer();
  private final SkillPresentationCatalog skillPresentation;
  private final SkillBodyRenderer skillBodyRenderer;
  private final ChungryongWeaponRenderer chungryongWeapon;
  private final SkillVfxRenderer skillVfx;
  private String activeSkillVisualId,characterBodyIdentity="mm001";
  private final SkillApproachController skillApproach=new SkillApproachController();
  private final SkillVfxRenderer.Anchors skillAnchors=new SkillVfxRenderer.Anchors(){
    public float x(String id){if(RuntimeCombatSession.PLAYER_ID.equals(id))return renderedPlayerWorldX();for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id))return m.x;return Float.NaN;}
    public float y(String id){if(RuntimeCombatSession.PLAYER_ID.equals(id))return renderedPlayerWorldY();for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id))return m.y;return Float.NaN;}
    public float centerY(String id){if(RuntimeCombatSession.PLAYER_ID.equals(id))return y(id)-23f;for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id)){if(inPoteField&&PoteForestMonsterShowcase.containsMonster(m.id))return m.y-("POTE_LYCAN".equals(m.id)?33f:21f);if(inReagentShop&&townInterior.kind==TownInteriorDef.Kind.INN)return m.y-5f;return m.y-10f*WorldEntityPresentationRenderer.MONSTER_RENDER_SCALE;}return Float.NaN;}
  };
  private final WorldEntityPresentationRenderer worldEntityRenderer=new WorldEntityPresentationRenderer();
  private final RpgInventoryPresentation rpgPresentation=new RpgInventoryPresentation();
  private final RpgInteractionController rpgInteraction=new RpgInteractionController();
  private final ClassicHudIconAtlas hudIcons=new ClassicHudIconAtlas();
  private final HudSpriteCatalog hudSprites;
  private final EquipmentVisualRegistry inventoryVisuals;
  private final ReagentItemVisualRegistry reagentVisuals;
  private final ReagentShopInteriorRenderer reagentShopRenderer;
  private WorldRuntimeAdapter reagentShopAdapter,lastOutcomeWorld;
  private boolean inReagentShop=false,reagentShopOpen=false;
  private float millesReturnX,millesReturnY;
  private TownInteriorDef townInterior;
  private final TownInteriorRenderer townRenderer;
  private final TownNpcRenderer townNpcRenderer;
  private final InnMouseRenderer innMouseRenderer;
  private final TownShopWindow townWindow;
  private boolean merchantApproach,questExitPending,questEntryPending;
  private final QuestJournalModel questJournalModel;
  private final QuestJournalWindow questJournalWindow=new QuestJournalWindow();
  private final F5mAdaptedPrologueQuest f5mQuest=F5mAdaptedPrologueQuest.openingFixture();
  private final GrowthQuest2 quest2=new GrowthQuest2();

  private float scale=1,ox,oy,vx,vy,hudRightOffset,hudCenterOffset,logicalViewportWidth=W;
  private float knobX=JOY_X,knobY=JOY_Y;
  private int autoSkillCursor;
  private boolean joy,running,inventoryOpen,statsOpen,equipmentOpen,autoAttackEnabled,questCollapsed=true,questJournalOpen,innDialogueOpen,chatExpanded;
  private float autoTargetHintClock;
  private final ItemWindow itemWindow;
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

  private final Runnable loop=new Runnable(){@Override public void run(){if(!running)return;long n=SystemClock.uptimeMillis();float dt=Math.min(.05f,(n-last)/1000f);last=n;F5mSaveStore.beginFrame();try{update(dt);}finally{F5mSaveStore.endFrame();}checkpointClock+=dt;if(checkpointClock>=2f||savedLedgerSequence!=state.ledger().sequence()){checkpoint();}{WorldRuntimeAdapter active=activeWorld();camera=active.camera();camera.follow(active.presentationPlayerX(),active.presentationPlayerY());}invalidate();postDelayed(this,16);}};

  public GameView(Context c){super(c);UiTheme.install(c);questJournalModel=new QuestJournalModel(c);townNpcRenderer=new TownNpcRenderer(c);innMouseRenderer=new InnMouseRenderer(c);itemWindow=new ItemWindow(c);combatSession.setSkillVisibility(this::skillTargetVisible);skillPresentation=new SkillPresentationCatalog(c);skillBodyRenderer=new SkillBodyRenderer(c,skillPresentation);chungryongWeapon=new ChungryongWeaponRenderer(c);skillVfx=new SkillVfxRenderer(c,skillPresentation);characterBodyIdentity=c.getSharedPreferences("project_dark_visual_v1",0).getString("body_identity","mm001");skillBook=SkillBook.load(c);combatSession.setBasicHits(skillBook::basicHits);combatSession.setSkillProficiency(id->skillBook.testAccess()?100:skillBook.proficiency(id));
    combatSession.setSkillMovement(new SkillAbilityExecutor.Movement(){
      public boolean movePlayer(float x,float y){int steps=SkillActionContract.distance(state.player().x,state.player().y,x,y);if(steps==Integer.MAX_VALUE||steps>6||state.skillEffects().rooted("player"))return false;for(int i=1;i<=steps;i++){float q=i/(float)steps;if(!activeWorld().canPlayerOccupy(state.player().x+(x-state.player().x)*q,state.player().y+(y-state.player().y)*q))return false;}if(!activeWorld().canPlayerOccupy(x,y))return false;activeWorld().cancelForAction();state.player().x=x;state.player().y=y;activeWorld().snapCameraToPlayer();return true;}
      public boolean moveMonster(RuntimeState.Monster m,float x,float y){return state.tryMoveMonster(m,x-m.x,y-m.y,(float)Math.hypot(x-m.x,y-m.y));}
    });skillBook.bindJob(()->state.rpg().currentJobCode());skillWindow=new SkillWindow(skillBook,new SkillIconCatalog(c));F5mSaveStore.restoreAndBindSkillsActive(skillBook);hudSprites=new HudSpriteCatalog(c);inventoryVisuals=new EquipmentVisualRegistry(c);reagentVisuals=new ReagentItemVisualRegistry(c);reagentShopRenderer=new ReagentShopInteriorRenderer(c);townRenderer=new TownInteriorRenderer(c);townWindow=new TownShopWindow(c);setKeepScreenOn(true);F5mSaveStore.restoreQuestActive(f5mQuest);F5mSaveStore.restoreRewardsActive(state.rpg());F5mSaveStore.restoreQuest2Active(quest2);quest2.unlockIfPrologueCompleted(f5mQuest);state.rpg().campaign().syncOpening(f5mQuest.state()==F5mAdaptedPrologueQuest.State.COMPLETED,quest2.state()==GrowthQuest2.State.COMPLETED);float[] returnPoint=F5mSaveStore.savedFieldReturnPointActive();fieldReturnX=returnPoint[0];fieldReturnY=returnPoint[1];if(com.projectdark.mobile.world.CampaignWorld.contains(F5mSaveStore.savedMapIdActive())){state.enterCampaignMap(F5mSaveStore.savedMapIdActive(),false);inPoteField=true;}F5mSaveStore.restoreRuntimeActive(state);worldAdapter.snapCameraToPlayer();if(inPoteField){poteFieldAdapter=new WorldRuntimeAdapter(state,logicalViewportWidth,H,PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.navigationTiles(),PoteFieldDef.obstacles(),true);poteFieldAdapter.snapCameraToPlayer();camera=poteFieldAdapter.camera();}F5mSaveStore.bindRuntime(state,f5mQuest,quest2);restoreTownInterior();if(inPoteField)questJournalModel.visitForest(f5mQuest,quest2);savedLedgerSequence=state.ledger().sequence();if(!F5mSaveStore.writable())showFeedback("저장 데이터를 읽지 못했습니다 · 원본 보존 중",FeedbackTone.WARN);}
  void setSkillTestMode(boolean enabled){
    cancelSkillApproach();skillVfx.clear();skillVfx.testAccess=enabled;activeSkillVisualId=null;skillBook.setTestAccess(enabled);combatSession.setSkillTestMode(enabled);
    getContext().getSharedPreferences("project_dark_skill_test_v1",0).edit().putBoolean("enabled",enabled).apply();
    if(enabled){String raw=getContext().getSharedPreferences("project_dark_skill_test_v1",0).getString("slots",null);String[] ids={"SK_마법사_001","SK_마법사_003","SK_마법사_004","SK_마법사_005","SK_성직자_005","SK_무도가_002","SK_무도가_007","SK_마법사_043"};if(raw!=null)try{org.json.JSONArray a=new org.json.JSONArray(raw);if(a.length()==8)for(int i=0;i<8;i++)ids[i]=a.isNull(i)?null:a.getString(i);}catch(Exception ignored){}skillBook.restoreTestSlots(ids);}
    showFeedback(enabled?"스킬 테스트 ON · "+skillBook.catalogSize()+"개 시험 목록 · MP 소모 없음":"스킬 테스트 OFF · 직업/습득 조건 적용",FeedbackTone.INFO);
  }
  private boolean saveSkillSlots(){if(!skillBook.testAccess())return F5mSaveStore.writable()&&F5mSaveStore.checkpointActive();org.json.JSONArray a=new org.json.JSONArray();for(String id:skillBook.testSlots())a.put(id==null?org.json.JSONObject.NULL:id);return getContext().getSharedPreferences("project_dark_skill_test_v1",0).edit().putString("slots",a.toString()).commit();}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(loop);}
  public void pause(){cancelSkillApproach();running=false;removeCallbacks(loop);F5mSaveStore.beginFrame();try{state.tick(0f);state.applyDerivedGrowth();consumeLedger();}finally{F5mSaveStore.endFrame();}checkpoint();}
  private boolean checkpoint(){
    savedLedgerSequence=state.ledger().sequence();
    state.rpg().campaign().visit(state.currentMapId());boolean saved=F5mSaveStore.checkpointActive();
    if(saved){saveWarningShown=false;}
    else if(!saveWarningShown){showFeedback("저장하지 못했습니다 · 이전 저장 확인 필요",FeedbackTone.WARN);saveWarningShown=true;}
    checkpointClock=0f;
    return saved;
  }

  private void update(float dt){
    quest2.unlockIfPrologueCompleted(f5mQuest);state.rpg().campaign().syncOpening(f5mQuest.state()==F5mAdaptedPrologueQuest.State.COMPLETED,quest2.state()==GrowthQuest2.State.COMPLETED);
    if(inReagentShop){updateReagentShop(dt);return;}
    if(inPoteField){updatePoteField(dt);return;}
    feedbackClock=Math.max(0,feedbackClock-dt);rewardClock=Math.max(0,rewardClock-dt);tapMarkerClock=Math.max(0,tapMarkerClock-dt);autoTargetHintClock=Math.max(0,autoTargetHintClock-dt);
    combat.tick(dt);if(autoAttackEnabled&&combat.target()==null&&isMonsterApproach(moveTarget.snapshot()))worldAdapter.cancel();combat.setBasicAttack(equipmentActions.resolveBasicAttack(state.rpg()).animationAction);tickSkillCombat(dt);state.tick(dt);state.applyDerivedGrowth();quest2.unlockIfPrologueCompleted(f5mQuest);state.rpg().campaign().syncOpening(f5mQuest.state()==F5mAdaptedPrologueQuest.State.COMPLETED,quest2.state()==GrowthQuest2.State.COMPLETED);consumeLedger();consumeRewardNotice();monsterAi.tick(state,dt);
    if(!state.player().alive){cancelSkillApproach();autoAttackEnabled=false;action=Action.IDLE;playerFacing.endAttack();interaction.cancel();combat.clearTarget();combat.cancelApproach();activeWorld().cancelForAction();joy=false;vx=vy=0;knobX=JOY_X;knobY=JOY_Y;directStepClock=0f;return;}
    if(isActing()){actionClock+=dt;if(actionClock>=duration(action)){actionClock=0;activeSkillVisualId=null;playerFacing.endAttack();action=(joy&&(vx!=0||vy!=0))?Action.WALK:Action.IDLE;}return;}
    if(state.skillEffects().rooted("player")){activeWorld().cancelForAction();joy=false;return;}
    tickSkillApproach(dt);if(isActing())return;
    WorldRuntimeAdapter.FrameSnapshot navigation=worldAdapter.tickNavigation(dt*state.skillEffects().speed());
    if(navigation.overlappingPortal!=null&&TownInteriorDef.forDoor(navigation.overlappingPortal.id)!=null){enterTownInterior(TownInteriorDef.forDoor(navigation.overlappingPortal.id));return;}
    if(navigation.overlappingPortal!=null&&"milles_south_exit_proto".equals(navigation.overlappingPortal.id)){enterPoteField();return;}
    if(joy&&(vx!=0||vy!=0)){
      directStepClock=Math.max(0f,directStepClock-dt*state.skillEffects().speed());
      if(!worldAdapter.presentationMoving()&&directStepClock<=0f){WorldMoveTargetController.Snapshot step=worldAdapter.step(joystickDirection());directStepClock=WorldMoveTargetController.TILE_STEP_SECONDS;applyWorldFacing(step.lastStepDirection);consumeMoveOutcome(step);}
      if(worldAdapter.presentationMoving()){action=Action.WALK;walkClock+=dt;}else action=Action.IDLE;
      interaction.cancelApproach();combat.cancelApproach();return;
    }
    WorldMoveTargetController.Snapshot move=navigation.movement;
    if(move.status==WorldMoveTargetController.Status.MOVING||worldAdapter.presentationMoving()){
      if(move.lastStepDirection!=null)applyWorldFacing(move.lastStepDirection);
      action=Action.WALK;walkClock+=dt;return;
    }
    consumeMoveOutcome(move);if(!isActing())action=Action.IDLE;tickSkillApproach(0);if(skillApproach.skillId==null&&!isActing())tickAutoAttack(move);
  }

  private void consumeMoveOutcome(WorldMoveTargetController.Snapshot move){
    if(move==null)return;
    // Request sequence numbers are local to each map adapter.
    if(lastOutcomeWorld!=activeWorld()){lastOutcomeWorld=activeWorld();lastMoveRequestId=-1;lastMoveStatus=WorldMoveTargetController.Status.IDLE;}
    if(move.requestId!=lastMoveRequestId){lastMoveRequestId=move.requestId;lastMoveStatus=WorldMoveTargetController.Status.IDLE;}
    if(move.status==lastMoveStatus)return;
    lastMoveStatus=move.status;
    if(move.status==WorldMoveTargetController.Status.REACHED){
      if(move.kind==WorldMoveTargetController.RequestKind.NPC_APPROACH){if(campaignAutoRoute&&"pote_travel_guide".equals(move.targetEntityId)){enterPoteField();autoNavigateCampaign();return;}RuntimeState.Npc npc=findNpc(move.targetEntityId);if(npc!=null)interaction.request(state,npc);}
      else if(move.kind==WorldMoveTargetController.RequestKind.MONSTER_APPROACH)executeReadyCombatIntent();
      else if(move.kind==WorldMoveTargetController.RequestKind.GROUND)showFeedback("이동 완료",FeedbackTone.INFO);
    }else if(move.status==WorldMoveTargetController.Status.BLOCKED){
      if(move.kind==WorldMoveTargetController.RequestKind.MONSTER_APPROACH)combat.cancelApproach();
      showFeedback("이동할 수 없는 위치입니다",FeedbackTone.WARN);
    }
  }
  private void consumeLedger(){List<CombatLedger.Event> events=state.ledger().snapshot();for(CombatLedger.Event e:events){if(e.sequence<=lastLedgerSequence)continue;lastLedgerSequence=e.sequence;F5mAdaptedPrologueQuest.DefeatResult qr=f5mQuest.consume(e);if(qr==F5mAdaptedPrologueQuest.DefeatResult.RETURN_READY)showReward("퀘스트 목표 완료 · 여관의 벤저민에게 보고하세요");if(quest2.consume(e)&&quest2.state()==GrowthQuest2.State.RETURN_READY)showReward("성장 훈련 완료 · 안내인에게 돌아가세요");switch(e.type){case MONSTER_DEFEATED:showFeedback("몬스터 격파",FeedbackTone.INFO);break;case PLAYER_HIT:showFeedback("-"+e.amount+" HP",FeedbackTone.WARN);break;case PLAYER_DEFEATED:showFeedback("행동 불능",FeedbackTone.WARN);break;case PLAYER_REVIVED:showFeedback("부활",FeedbackTone.INFO);break;default:break;}}}
  private void consumeRewardNotice(){
    long exp=0,gold=0,last=lastRewardSequence;java.util.Map<String,Integer> items=new java.util.LinkedHashMap<>();boolean unresolved=false,full=false;
    for(RpgProgressionState.RewardResolution r:state.rpg().rewardHistory())if(r.combatSequence>lastRewardSequence){last=Math.max(last,r.combatSequence);unresolved|=r.status!=RpgProgressionState.RewardStatus.RESOLVED;exp+=r.exp==null?0:r.exp;gold+=r.gold;for(Map.Entry<String,Integer> e:r.autoLootedItems.entrySet())items.put(e.getKey(),items.getOrDefault(e.getKey(),0)+e.getValue());full|=r.itemOutcomes.containsValue(RpgProgressionState.AutoLootResult.INVENTORY_FULL);}
    if(last==lastRewardSequence)return;lastRewardSequence=last;F5mSaveStore.saveRewardsActive(state.rpg());String message="EXP +"+exp+" · Gold +"+gold;
    for(Map.Entry<String,Integer> e:items.entrySet())message+=" · "+itemDisplayName(e.getKey())+" x"+e.getValue();
    if(unresolved)message+=" · 미확인 보상";if(full)message+=" · 가방 수량 한도";showReward(message);
  }
  private int inventoryQuantity(String itemId){for(RpgInventoryPresentation.ItemRow row:rpgPresentation.inventoryRows(state.rpg()))if(row.itemId.equals(itemId))return row.quantity;return 0;}
  private String itemDisplayName(String itemId){for(RpgInventoryPresentation.ItemRow row:rpgPresentation.inventoryRows(state.rpg()))if(row.itemId.equals(itemId))return row.name;return itemId;}
  private RuntimeState.Npc findNpc(String id){if(id==null)return null;for(RuntimeState.Npc npc:state.npcs())if(id.equals(npc.id))return npc;return null;}
  private RuntimeState.Monster findMonster(String id){if(id==null)return null;for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id)&&m.alive)return m;return null;}
  private void autoNavigateQuest(){
    CampaignProgress.Def campaignNext=state.rpg().campaign().next(state.rpg());if(campaignNext!=null&&!campaignNext.kind.equals("EXTERNAL")&&!"COMMONER".equals(state.rpg().currentJobCode())){autoNavigateCampaign();return;}
    if(questJournalModel.current(f5mQuest,quest2,inPoteField,state.rpg())==null)return;
    cancelSkillApproach();autoAttackEnabled=false;joy=false;vx=vy=0;interaction.cancel();activeWorld().cancelForAction();combat.cancelApproach();
    if(inPoteField)leavePoteField();
    F5mAdaptedPrologueQuest.State qs=f5mQuest.state();
    if(inReagentShop&&!(townInterior.kind==TownInteriorDef.Kind.INN&&(qs==F5mAdaptedPrologueQuest.State.ACTIVE||qs==F5mAdaptedPrologueQuest.State.RETURN_READY))){merchantApproach=false;innDialogueOpen=false;townWindow.close();questExitPending=true;reagentShopAdapter.requestGroundWorld(townInterior.exitX(),townInterior.exitY());showFeedback("건물을 나가 퀘스트 목표로 이동합니다",FeedbackTone.INFO);return;}
    QuestJournalModel.Row route=questJournalModel.current(f5mQuest,quest2,inPoteField,state.rpg());
    if(route!=null&&QuestJournalModel.JOB_CHOICE.equals(route.id)){RuntimeState.Npc counselor=findNpc("milles_job_counselor");if(counselor!=null){worldAdapter.requestNpcApproach(counselor.id);showFeedback("마이클에게 직업 상담 받기",FeedbackTone.INFO);}return;}
    if(qs==F5mAdaptedPrologueQuest.State.COMPLETED){
      if(inReagentShop){merchantApproach=false;innDialogueOpen=false;questExitPending=true;reagentShopAdapter.requestGroundWorld(townInterior.exitX(),townInterior.exitY());showFeedback("여관을 나가 다음 의뢰로 이동합니다",FeedbackTone.INFO);return;}
      if(ActiveQuestTracker.current(f5mQuest,quest2)==ActiveQuestTracker.Quest.GROWTH_2){autoNavigateQuest2();return;}
      RuntimeState.Npc next=findNpc("milles_gate_proto");if(next!=null){worldAdapter.requestNpcApproach(next.id);showFeedback("윌리엄에게 숲길 안내 받기",FeedbackTone.INFO);}return;
    }
    if(inReagentShop&&townInterior.kind==TownInteriorDef.Kind.INN){if(qs==F5mAdaptedPrologueQuest.State.ACTIVE){RuntimeState.Monster m=findMonster(f5mQuest.objectiveMonsterId());if(m!=null){combat.selectTarget(m);reagentShopAdapter.requestMonsterApproach(m.id,CanonicalMeleeTileContract.REACH_DISTANCE);}}else{merchantApproach=true;reagentShopAdapter.requestGroundWorld(townInterior.customerX(),townInterior.customerY());}return;}
    if(qs==F5mAdaptedPrologueQuest.State.AVAILABLE){RuntimeState.Npc npc=findNpc("milles_guide_proto");if(npc!=null){worldAdapter.requestNpcApproach(npc.id);showFeedback("제임스에게 이동",FeedbackTone.INFO);}return;}
    if(qs==F5mAdaptedPrologueQuest.State.ACTIVE||qs==F5mAdaptedPrologueQuest.State.RETURN_READY){questEntryPending=true;worldAdapter.requestGroundWorld(2016f,736f);showFeedback("밀레스 여관으로 이동",FeedbackTone.INFO);}
  }

  private void executeReadyCombatIntent(){CombatController.Intent ready=combat.consumeReadyIntent();action=Action.IDLE;if(ready==CombatController.Intent.ATTACK)attack();else if(ready==CombatController.Intent.CAST)cast();else if(ready==CombatController.Intent.SKILL)skill();else if(ready==CombatController.Intent.KICK)kick();}
  private void beginCombatApproach(CombatController.Intent intent){WorldRuntimeAdapter active=activeWorld();active.cancelForAction();if(combat.beginApproach(intent)){RuntimeState.Monster target=combat.approachTarget();if(target!=null)active.requestMonsterApproach(target.id,combat.intentRange());showFeedback("타깃으로 이동",FeedbackTone.INFO);}}

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
  private float duration(Action a){SkillActionContract.Rule r=SkillActionContract.get(activeSkillVisualId);if(r!=null&&r.contact>.3f)return r.contact+.35f;switch(a){case CAST:return .65f;case SWING:return .45f;case THRUST:return .38f;case THROW:return .52f;case PUNCH:return .32f;case SKILL:return .50f;case KICK:return .45f;default:return 0;}}
  private void trigger(Action a){if(isActing()||!state.player().alive)return;state.skillEffects().stopRest();activeWorld().cancelForAction();interaction.cancel();combat.cancelApproach();action=a;actionClock=0;}
  private void showFeedback(String s){showFeedback(s,FeedbackTone.INFO);}private void showFeedback(String s,FeedbackTone tone){feedback=s;feedbackTone=tone;feedbackClock=1.15f;}private void showReward(String s){rewardBanner=s;rewardClock=2.4f;}
  private boolean requireTarget(){if(combat.hasUsableTarget())return true;showFeedback("먼저 몬스터를 선택하세요",FeedbackTone.WARN);return false;}
  private boolean inRange(float range){return combat.inRange(state,range);}
  private void tickSkillCombat(float dt){
    skillVfx.tick(dt);
    RuntimeCombatSession.FrameResult result=combatSession.tick(dt);
    for(CombatResolver.Event e:result.events){SkillActionContract.Rule r=SkillActionContract.get(e.actionId);
      if(e.type==CombatResolver.EventType.HIT_FEEDBACK&&RuntimeCombatSession.PLAYER_ID.equals(e.actorId)&&r!=null&&r.pattern==SkillActionContract.Pattern.SINGLE&&r.damage()){
        for(RuntimeState.Monster target:state.monsters())if(target.id.equals(e.targetId)){playerFacing.beginAttack(target.x-state.player().x,target.y-state.player().y);break;}
      }
    }
    skillVfx.consume(result.events,skillAnchors);consumeSkillNotice();
    for(CombatResolver.Event e:result.events)if(RuntimeCombatSession.PLAYER_ID.equals(e.actorId)&&e.type==CombatResolver.EventType.ACTION_CANCELLED){activeSkillVisualId=null;action=Action.IDLE;actionClock=0;playerFacing.endAttack();}
    java.util.Set<Long> practiced=new java.util.HashSet<>();
    for(CombatResolver.Event e:result.events)if(e.type==CombatResolver.EventType.EFFECT_APPLIED&&RuntimeCombatSession.PLAYER_ID.equals(e.actorId)&&practiced.add(e.actionSequence)){skillBook.practiced(e.actionId.startsWith("attack_proto_")?"SK_공통_001":e.actionId);state.rpg().campaign().validSkill(e.actionId,e.actionSequence,e.amount,skillBook.testAccess(),state.rpg());}
  }
  private void consumeSkillNotice(){String notice=combatSession.takeSkillNotice();if(notice.isEmpty())return;
    if(notice.equals("TRAVEL")){if(inPoteField){leavePoteField();}state.player().x=WorldDef.PLAYER_SPAWN_X;state.player().y=WorldDef.PLAYER_SPAWN_Y;activeWorld().snapCameraToPlayer();checkpoint();showFeedback("밀레스 귀환",FeedbackTone.INFO);}
    else if(notice.equals("DOOR")){RuntimeState.Npc closest=null;for(RuntimeState.Npc n:state.npcs())if(closest==null||state.distanceTo(n)<state.distanceTo(closest))closest=n;if(closest!=null&&state.distanceTo(closest)<80){interaction.request(state,closest);showFeedback("출입 대상 선택",FeedbackTone.INFO);}else showFeedback("열 수 있는 문이 가까이 없습니다",FeedbackTone.WARN);}
    else showFeedback(notice,FeedbackTone.INFO);
  }
  private boolean isSkillLearned(String id){return skillBook!=null&&skillBook.usable(id);}
  private final SkillWindow.Actions skillActions=new SkillWindow.Actions(){
    public void use(SkillBook.Entry e){useBookSkill(e);}
    public boolean canLearn(SkillBook.Entry e){return F5mSaveStore.writable()&&quote(e).canLearn;}
    public SkillAcquisition.Quote quote(SkillBook.Entry e){return new SkillAcquisition(skillBook).quote(e,state.rpg());}
    public long gold(){return state.rpg().gold();}
    public void learn(SkillBook.Entry e){
      if(!F5mSaveStore.writable()){showFeedback("저장 데이터를 확인할 수 없어 습득할 수 없습니다",FeedbackTone.WARN);return;}
      SkillAcquisition service=new SkillAcquisition(skillBook);
      if(service.learn(e.id,state.rpg(),()->F5mSaveStore.checkpointActive())){performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);showFeedback(e.name+" 습득 완료",FeedbackTone.INFO);}
      else{SkillAcquisition.Quote q=service.quote(e,state.rpg());showFeedback(q.learned?"이미 습득한 스킬입니다":q.blockers.isEmpty()?"저장 실패 · 습득 비용을 돌려드렸습니다":String.join(" · ",q.blockers),FeedbackTone.WARN);}
    }
    public boolean save(){return saveSkillSlots();}
    public void testMode(boolean enabled){setSkillTestMode(enabled);}
    public float cooldown(String id){return combatSession.cooldownRemaining(RuntimeCombatSession.PLAYER_ID,"SK_공통_001".equals(id)?RuntimeCombatSession.playerAttackActionId(equipmentActions.resolveBasicAttack(state.rpg()).animationAction):id);}
    public String requirements(SkillBook.Entry e){return new SkillAcquisition(skillBook).description(e,state.rpg());}
    public void notice(String text){showFeedback(text,FeedbackTone.INFO);}
  };
  private void drawSkills(Canvas c){if(!skillWindow.open)return;c.save();c.translate(hudCenterOffset,0);skillWindow.draw(c,skillActions);c.restore();}
  private void useBookSkill(SkillBook.Entry entry){
    if(entry==null||isActing()||!state.player().alive)return;
    if(!skillBook.jobAllowed(entry.id)||!skillBook.usable(entry.id)){useBookSkillNow(entry);return;}
    cancelSkillApproach();autoAttackEnabled=false;
    if("SK_공통_001".equals(entry.id)){useBookSkillNow(entry);return;}
    SkillActionContract.Rule rule=SkillActionContract.get(entry.id);
    if(rule!=null&&rule.selfAnchored()){useBookSkillNow(entry);return;}
    boolean offensive=rule!=null&&rule.presentationAllowed()&&(!rule.selfAnchored()||rule.damage());
    if(!offensive){useBookSkillNow(entry);return;}
    float cd=skillActions.cooldown(entry.id);if(cd>0){showFeedback(String.format(java.util.Locale.ROOT,"쿨타임 %.1f초 남음",cd),FeedbackTone.WARN);return;}
    if(!skillBook.testAccess()&&entry.runtime!=null&&state.player().mp<entry.runtime.mpCost){showFeedback("MP가 부족합니다",FeedbackTone.WARN);return;}
    RuntimeState.Monster target=combat.target();List<WorldMoveTargetController.TileCenter> route=null;
    if(target!=null&&target.alive)route=SkillApproachController.path(activeWorld(),rule,target,logicalViewportWidth,H);
    else{
      int best=Integer.MAX_VALUE;
      for(RuntimeState.Monster candidate:state.monsters())if(candidate.alive&&skillTargetVisible(candidate.id)){
        List<WorldMoveTargetController.TileCenter> path=SkillApproachController.path(activeWorld(),rule,candidate,logicalViewportWidth,H);
        if(path!=null&&path.size()<best){best=path.size();target=candidate;route=path;}
      }
      if(target!=null)combat.selectTarget(target);
    }
    if(target==null||route==null){showFeedback("기술을 사용할 수 있는 경로가 없습니다",FeedbackTone.WARN);return;}
    skillApproach.begin(entry.id,target.id);interaction.cancel();combat.cancelApproach();joy=false;vx=vy=0;
    tickSkillApproach(0);
    if(skillApproach.skillId!=null)showFeedback(entry.name+" · 사용 가능한 위치로 이동",FeedbackTone.INFO);
  }
  private void cancelSkillApproach(){if(skillApproach.skillId!=null)activeWorld().cancel();skillApproach.cancel();}
  private void tickSkillApproach(float dt){
    if(skillApproach.skillId==null)return;skillApproach.age+=Math.max(0,dt);
    RuntimeState.Monster target=findMonster(skillApproach.targetId);SkillBook.Entry entry=skillBook.get(skillApproach.skillId);
    if(target==null||!target.alive||entry==null||!state.player().alive||!skillBook.usable(entry.id)||!skillBook.jobAllowed(entry.id)||skillApproach.age>30f){cancelSkillApproach();return;}
    if(activeWorld().presentationMoving()||isActing())return;
    SkillActionContract.Rule rule=SkillActionContract.get(entry.id);
    if(!target.isMoving&&skillTargetVisible(target.id)&&SkillApproachController.legal(rule,state.player().x,state.player().y,target.x,target.y)&&activeWorld().hasCombatLineOfSight("player",target.id)){
      combat.selectTarget(target);cancelSkillApproach();useBookSkillNow(entry);return;
    }
    float tx=target.isMoving?target.moveTargetX:target.x,ty=target.isMoving?target.moveTargetY:target.y;
    WorldMoveTargetController.Snapshot move=activeWorld().movement().snapshot();
    if(move.status==WorldMoveTargetController.Status.MOVING&&tx==skillApproach.plannedX&&ty==skillApproach.plannedY)return;
    List<WorldMoveTargetController.TileCenter> path=SkillApproachController.path(activeWorld(),rule,target,logicalViewportWidth,H);
    if(path==null){cancelSkillApproach();showFeedback("기술 접근 경로가 막혔습니다",FeedbackTone.WARN);return;}
    skillApproach.plannedX=tx;skillApproach.plannedY=ty;activeWorld().movement().requestSkillApproach(target.id,path);
  }
  private void useBookSkillNow(SkillBook.Entry entry){useBookSkillNow(entry,false);}
  private void useBookSkillNow(SkillBook.Entry entry,boolean automatic){
    if(entry==null)return;
    if(!skillBook.jobAllowed(entry.id)){showFeedback(entry.job+" 전용 기술입니다",FeedbackTone.WARN);return;}
    if(!skillBook.usable(entry.id)){showFeedback(skillBook.learned(entry.id)?"이 기술의 전투 효과는 준비 중입니다":"먼저 기술을 습득하세요",FeedbackTone.WARN);return;}
    if(!automatic)autoAttackEnabled=false;
    if("SK_공통_001".equals(entry.id)){float remaining=skillActions.cooldown(entry.id);if(remaining>0)showFeedback(String.format(java.util.Locale.ROOT,"쿨타임 %.1f초 남음",remaining),FeedbackTone.WARN);else attack();return;}
    SkillActionContract.Rule rule=SkillActionContract.get(entry.id);
    if(rule!=null&&!rule.presentationAllowed()){
      SkillAbilityCatalog.Ability ability=SkillAbilityCatalog.get(entry.id);
      if(rule.mode.equals("LINKED")||rule.pattern==SkillActionContract.Pattern.PASSIVE){showFeedback(entry.name+" · 기본공격/장착에 적용",FeedbackTone.INFO);return;}
      if(ability==null||!ability.supported()){showFeedback(entry.name+" · 필요한 대상 서비스가 아직 없습니다",FeedbackTone.WARN);return;}
      if(combatSession.useUtility(entry.id))consumeSkillNotice();else showFeedback(entry.name+" · 자원/쿨타임 조건을 확인하세요",FeedbackTone.WARN);return;
    }
    if(entry.runtime==null){showFeedback(entry.name+" · 효과 미확정",FeedbackTone.WARN);return;}
    submitLearnedAction(entry.runtime,entry.magic()?Action.CAST:entry.runtime.effectType==SkillDef.EffectType.KICK_ARC?Action.KICK:Action.SKILL);
  }
  private boolean skillTargetVisible(String id){
    if(RuntimeCombatSession.PLAYER_ID.equals(id))return true;
    for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id)){
      WorldCameraTransform.Point q=activeWorld().worldToScreen(m.x,m.y);
      return q.x>=0&&q.x<logicalViewportWidth&&q.y>=0&&q.y<H;
    }return false;
  }
  private void cast(){submitLearnedAction(SkillDef.CAST_PROTO,Action.CAST);}
  private void skill(){submitLearnedAction(SkillDef.SKILL_PROTO,Action.SKILL);}
  private void kick(){submitLearnedAction(SkillDef.KICK_PROTO,Action.KICK);}
  private void submitLearnedAction(SkillDef def,Action visual){
    float cd=combatSession.cooldownRemaining(RuntimeCombatSession.PLAYER_ID,def.id);
    if(cd>0){showFeedback(String.format(java.util.Locale.ROOT,"쿨타임 %.1f초 남음",cd),FeedbackTone.WARN);return;}
    if(isActing()||!state.player().alive)return;
    SkillActionContract.Rule rule=SkillActionContract.get(def.id);boolean self=rule!=null?rule.selfAnchored():def.targetPolicy==SkillDef.TargetPolicy.SELF;if(!self&&!requireTarget())return;
    CombatActionOrchestrator.Submission result=combatSession.submitPlayer(self?RuntimeCombatSession.PLAYER_ID:combat.target().id,def.id);
    if(result.accepted()){if(!self)snapshotAttackFacingTarget();else playerFacing.beginAttack();activeSkillVisualId=def.id;trigger(visual);}
    else showFeedback(result.rejectReason==CombatResolver.RejectReason.NOT_LEARNED?"아직 배우지 않은 기술입니다":result.rejectReason==CombatResolver.RejectReason.RESOURCE?"MP가 부족합니다":result.rejectReason==CombatResolver.RejectReason.COOLDOWN?"쿨타임 중입니다":result.rejectReason==CombatResolver.RejectReason.RANGE?"대상이 사거리 밖에 있습니다":"지금 사용할 수 없습니다",FeedbackTone.WARN);
  }
  private Action actionFor(AttackDef.Kind k){switch(k){case THRUST:return Action.THRUST;case THROW:return Action.THROW;case PUNCH:return Action.PUNCH;default:return Action.SWING;}}
  private void tickAutoAttack(WorldMoveTargetController.Snapshot navigation){
    if(!autoAttackEnabled||isActing()||joy||!state.player().alive||activeWorld().presentationMoving())return;
    if(navigation!=null&&navigation.status==WorldMoveTargetController.Status.MOVING)return;
    CampaignProgress.Def hunt=activeCampaignHunt();
    RuntimeState.Monster current=combat.target();
    if(!combat.hasUsableTarget()||(current!=null
        &&(!campaignHuntTarget(hunt,current)||activeWorld().monsterApproachPathSteps(current.id,CanonicalMeleeTileContract.REACH_DISTANCE)<0))){
      RuntimeState.Monster best=bestReachableAutoTarget(hunt);
      if(best==null){combat.clearTarget();if(autoTargetHintClock<=0f){showFeedback("접근 가능한 몬스터가 없습니다",FeedbackTone.INFO);autoTargetHintClock=3f;}return;}
      combat.selectTarget(best);
      autoTargetHintClock=1f;
    }
    // Use equipped slots in a fair cycle; skip unavailable actions without disabling AUTO.
    current=combat.target();
    for(int n=0;n<SkillBook.SLOT_COUNT;n++){
      int index=(autoSkillCursor+n)%SkillBook.SLOT_COUNT;SkillBook.Entry entry=skillBook.get(skillBook.slot(index));
      if(entry==null||!skillBook.usable(entry.id)||"SK_공통_001".equals(entry.id))continue;
      SkillActionContract.Rule rule=SkillActionContract.get(entry.id);SkillAbilityCatalog.Ability ability=SkillAbilityCatalog.get(entry.id);
      if(rule==null||!rule.presentationAllowed()||ability==null||!ability.supported()||skillActions.cooldown(entry.id)>0)continue;
      if(!skillBook.testAccess()&&state.player().mp<ability.mpCost)continue;
      if(ability.heal()&&state.player().hp>=state.player().maxHp)continue;
      if(!ability.damage()&&!ability.heal()&&state.skillEffects().playerEffects().stream().anyMatch(e->entry.id.equals(e.source)))continue;
      boolean self=rule.selfAnchored();
      if(!self&&(current==null||!SkillActionContract.canStart(rule,state.player().x,state.player().y,current.x,current.y,skillTargetVisible(current.id))||!activeWorld().hasCombatLineOfSight("player",current.id)))continue;
      useBookSkillNow(entry,true);
      if(isActing()){autoSkillCursor=(index+1)%SkillBook.SLOT_COUNT;return;}
    }
    if(combat.attackReady())attack();
  }

  private static boolean isMonsterApproach(WorldMoveTargetController.Snapshot move){return move!=null&&move.kind==WorldMoveTargetController.RequestKind.MONSTER_APPROACH&&move.status==WorldMoveTargetController.Status.MOVING;}

  private CampaignProgress.Def activeCampaignHunt(){
    CampaignProgress.Def d=campaignNext();
    if(d==null||state.rpg().campaign().status(d,state.rpg())!=CampaignProgress.Status.ACTIVE
        ||!(d.kind.equals("KILL")||d.kind.equals("PAIR")))return null;
    return d;
  }
  private boolean campaignHuntTarget(CampaignProgress.Def d,RuntimeState.Monster monster){
    if(d==null)return true;
    String species=monster.campaignRewardProfileId==null?monster.id:monster.campaignRewardProfileId;
    return state.rpg().campaign().wanted(d,species);
  }
  private RuntimeState.Monster bestReachableAutoTarget(){return bestReachableAutoTarget(null);}
  private RuntimeState.Monster bestReachableAutoTarget(CampaignProgress.Def hunt){
    return AutoAttackTargetSelector.select(state.monsters(),state.player().x,state.player().y,
        AUTO_TARGET_RADIUS,monster->activeWorld().monsterApproachPathSteps(
            monster.id,CanonicalMeleeTileContract.REACH_DISTANCE),monster->campaignHuntTarget(hunt,monster));
  }

  private void attack(){
    cancelSkillApproach();
    if(isActing()||!state.player().alive||!requireTarget())return;
    combat.setBasicAttack(equipmentActions.resolveBasicAttack(state.rpg()).animationAction);
    AttackDef def=combat.attackDef();
    if(!combat.attackInRange(state)){beginCombatApproach(CombatController.Intent.ATTACK);return;}
    RuntimeCombatSession.PlayerActionSubmission result=combatSession.submitPlayerBasicAttack(combat.target().id);
    if(!result.combat.accepted())return;
    activeWorld().cancelForAction();snapshotAttackFacingTarget();combat.commitAttack();trigger(actionFor(def.kind));
  }

  static float logicalWidthForView(int width,int height){float fittedScale=Math.min(width/W,height/H);return fittedScale<=0?W:width/fittedScale;}
  static float rightHudOffsetForView(int width,int height){return Math.max(0f,logicalWidthForView(width,height)-W);}
  protected void onSizeChanged(int w,int h,int ow,int oh){scale=Math.min(w/W,h/H);logicalViewportWidth=logicalWidthForView(w,h);boolean wide=logicalViewportWidth>W+.01f;ox=wide?0:(w-W*scale)/2;oy=(h-H*scale)/2;hudRightOffset=wide?logicalViewportWidth-W:0;hudCenterOffset=hudRightOffset*.5f;worldAdapter.resizeViewport(logicalViewportWidth,H);if(poteFieldAdapter!=null)poteFieldAdapter.resizeViewport(logicalViewportWidth,H);camera=activeWorld().camera();if(reagentShopAdapter!=null)reagentShopAdapter.resizeViewport(logicalViewportWidth,H);}
  protected void onDraw(Canvas c){c.drawColor(Color.BLACK);c.save();c.translate(ox,oy);c.scale(scale,scale);if(inReagentShop){drawReagentShopWorld(c);drawHud(c);drawFeedbackBanners(c);drawReagentShop(c);drawInventory(c);drawEquipment(c);drawStats(c);drawSkills(c);drawDeath(c);if(innDialogueOpen)drawInnDialogue(c);}else{if(inPoteField){poteFieldRenderer.drawBelow(c,poteFieldAdapter,activeWorld().presentationPlayerY());c.save();c.translate(-camera.cameraX(),-camera.cameraY());drawTapMarker(c);drawCampaignGates(c);drawNpcs(c);drawMonsters(c);drawCharacter(c);skillVfx.draw(c,skillAnchors);c.restore();poteFieldRenderer.drawAbove(c,poteFieldAdapter,activeWorld().presentationPlayerY());}else{drawWorld(c);c.save();c.translate(-camera.cameraX(),-camera.cameraY());skillVfx.draw(c,skillAnchors);c.restore();}drawHud(c);drawFeedbackBanners(c);drawInventory(c);drawEquipment(c);drawStats(c);drawSkills(c);drawDialogue(c);if(inPoteField)drawReagentShop(c);drawDeath(c);}if(questJournalOpen)drawQuestJournal(c);c.restore();}

  private WorldRuntimeAdapter activeWorld(){if(inReagentShop&&reagentShopAdapter!=null)return reagentShopAdapter;return inPoteField&&poteFieldAdapter!=null?poteFieldAdapter:worldAdapter;}

  private boolean campaignAutoRoute;
  private CampaignProgress.Def campaignNext(){return state.rpg().campaign().next(state.rpg());}
  private boolean campaignDialogue(){RuntimeState.Npc n=interaction.dialogNpc();if(n==null||"COMMONER".equals(state.rpg().currentJobCode()))return false;CampaignProgress.Def d=campaignNext();return n.id.startsWith("mentor_")||n.id.startsWith("piet_")||n.id.startsWith("campaign_altar_")||n.id.equals("pote_trail_guide")||(d!=null&&!d.kind.equals("EXTERNAL")&&d.npc.equals(n.id));}
  private void drawCampaignDialogue(Canvas c,RuntimeState.Npc npc){
    CampaignProgress.Def d=campaignNext();boolean relevant=d!=null&&d.npc.equals(npc.id);String body;
    if(npc.id.startsWith("campaign_altar_"))body="숲의 정수를 모아 제단을 정화하세요. 정화 의뢰를 수락한 뒤 세 곳을 각각 방문합니다.";
    else if(relevant){CampaignProgress.Status status=state.rpg().campaign().status(d,state.rpg());body=d.title+" · "+(status==CampaignProgress.Status.LOCKED?"Lv"+d.level+"에 다시 찾아오세요":status==CampaignProgress.Status.AVAILABLE?"수락한 뒤 목표를 수행하고 돌아오세요":status==CampaignProgress.Status.REPORT?"조사를 마쳤군요. 완료를 눌러 보상을 받으세요":state.rpg().campaign().objective(d)+" · "+state.rpg().campaign().count(d,state.rpg())+" / "+d.goal);}
    else body="조사를 준비하세요. 이곳에서 무료로 쉬거나 회복약과 마나약을 살 수 있습니다.";
    drawWrappedText(c,body,218,370,510,11,17);
    String[] labels={"무료 휴식","회복약 20G",npc.id.equals("piet_supplier")?"장비 상점":"마나약 25G",""};
    if(npc.id.startsWith("campaign_altar_"))labels[3]="정화";
    else if(relevant){CampaignProgress.Status st=state.rpg().campaign().status(d,state.rpg());labels[3]=st==CampaignProgress.Status.AVAILABLE?"수락":st==CampaignProgress.Status.REPORT?"완료":"목표 확인";}
    for(int i=0;i<4;i++){float x=218+i*130;p.setColor(0xD05B4727);c.drawRoundRect(new RectF(x,412,x+118,446),7,7,p);text(c,labels[i],x+14,434,9);}
  }
  private void handleCampaignDialogue(float x,float y){
    if(circleHit(x,y,744,321,18)||!inside(x,y,210,315,750,456)){interaction.dismissDialog();activeWorld().cancelForAction();return;}
    if(y<412||y>446)return;int button=(int)((x-218)/130);if(x<218||button<0||button>3)return;RuntimeState.Npc npc=interaction.dialogNpc();if(npc==null)return;
    if(button==0){int hp=state.player().hp,mp=state.player().mp;CampaignResources.freeRest(state);if(!checkpoint()){state.player().hp=hp;state.player().mp=mp;}else showReward("휴식 완료 · HP/MP 회복");return;}
    if(button==2&&npc.id.equals("piet_supplier")){interaction.dismissDialog();townWindow.openCampaign(state);return;}
    if(button==1||button==2){String id=button==1?RpgProgressionState.B_SMALL_POTION_ITEM_ID:"IT_B_MP_POTION";long price=button==1?20:25;if(F5mSaveStore.transactActive(state.rpg(),skillBook,()->state.rpg().buyItem(id,price)))showReward("보급품 구매 · Gold -"+price);else showFeedback("구매할 금전 또는 저장 상태를 확인하세요",FeedbackTone.WARN);return;}
    if(npc.id.startsWith("campaign_altar_")){if(F5mSaveStore.transactActive(state.rpg(),skillBook,()->state.rpg().campaign().purify(npc.id,state.rpg())))showReward("제단 정화 완료");else showFeedback("정화 의뢰 또는 이미 정화한 제단을 확인하세요",FeedbackTone.INFO);return;}
    CampaignProgress.Def d=campaignNext();if(d==null||!d.npc.equals(npc.id))return;CampaignProgress.Status st=state.rpg().campaign().status(d,state.rpg());boolean ok=false;
    if(st==CampaignProgress.Status.AVAILABLE)ok=F5mSaveStore.transactActive(state.rpg(),skillBook,()->state.rpg().campaign().accept(d.id,state.rpg(),skillBook));
    else if(st==CampaignProgress.Status.REPORT)ok=F5mSaveStore.transactActive(state.rpg(),skillBook,()->state.rpg().campaign().claim(d.id,state.rpg()));
    if(ok){state.applyDerivedGrowth();interaction.dismissDialog();showReward(st==CampaignProgress.Status.REPORT?d.title+" 완료 · EXP +"+d.exp+" · Gold +"+d.gold:"의뢰 수락 · "+d.title);}
    else if(st==CampaignProgress.Status.ACTIVE){interaction.dismissDialog();autoNavigateCampaign();}
    else showFeedback("필요 레벨과 목표, 저장 상태를 확인하세요",FeedbackTone.INFO);
  }
  private String campaignHuntMap(){int lv=state.rpg().normalLevel();return lv<10?WorldDef.ID:lv<15?"MAP_POTE_01":lv<20?"MAP_POTE_02":lv<30?"MAP_POTE_03":"MAP_POTE_04";}
  private String campaignObjectiveMap(CampaignProgress.Def d){CampaignProgress.Status status=state.rpg().campaign().status(d,state.rpg());if(status==CampaignProgress.Status.LOCKED)return campaignHuntMap();if(status!=CampaignProgress.Status.ACTIVE)return d.map;if(d.kind.equals("SKILL"))return d.level<11?WorldDef.ID:d.level<26?"MAP_POTE_01":"MAP_POTE_03";if(d.kind.equals("KILL")||d.kind.equals("PAIR")){return d.id.equals("M04")?WorldDef.ID:d.id.equals("M07")?"MAP_POTE_01":d.id.equals("M08")?"MAP_POTE_02":d.level<30?"MAP_POTE_03":"MAP_POTE_04";}if(d.kind.equals("ALTAR"))return "MAP_POTE_04";if(d.kind.equals("VISIT"))return d.targets;return d.map;}
  private void autoNavigateCampaign(){
    CampaignProgress.Def d=campaignNext();if(d==null)return;campaignAutoRoute=true;cancelSkillApproach();autoAttackEnabled=false;joy=false;interaction.cancel();activeWorld().cancelForAction();combat.cancelApproach();String destination=campaignObjectiveMap(d);
    if(inReagentShop){questExitPending=true;reagentShopAdapter.requestGroundWorld(townInterior.exitX(),townInterior.exitY());return;}
    if(!state.currentMapId().equals(destination)){
      if(!inPoteField){RuntimeState.Npc travel=findNpc("pote_travel_guide");if(travel!=null)worldAdapter.requestNpcApproach(travel.id);return;}
      java.util.List<String> route=java.util.Arrays.asList(com.projectdark.mobile.world.CampaignWorld.ROUTE);boolean forward=route.indexOf(destination)>route.indexOf(state.currentMapId());if(WorldDef.ID.equals(destination))forward=false;WorldMoveTargetController.TileCenter gate=forward?com.projectdark.mobile.world.CampaignWorld.forward():com.projectdark.mobile.world.CampaignWorld.backward();poteFieldAdapter.requestGroundWorld(gate.x,gate.y);showFeedback("숲길을 따라 다음 지역으로 이동",FeedbackTone.INFO);return;
    }
    campaignAutoRoute=false;CampaignProgress.Status st=state.rpg().campaign().status(d,state.rpg());
    if(st==CampaignProgress.Status.AVAILABLE||st==CampaignProgress.Status.REPORT||(st==CampaignProgress.Status.ACTIVE&&(d.kind.equals("SUPPLY")||d.kind.equals("VISIT")||d.kind.equals("JOB")||d.kind.equals("LEVEL")))){RuntimeState.Npc npc=findNpc(d.npc);if(npc!=null)activeWorld().requestNpcApproach(npc.id);return;}
    if(st==CampaignProgress.Status.ACTIVE&&d.kind.equals("ALTAR")){for(RuntimeState.Npc n:state.npcs())if(n.id.startsWith("campaign_altar_")&&!state.rpg().campaign().altarDone(n.id)){activeWorld().requestNpcApproach(n.id);return;}}
    if(st==CampaignProgress.Status.ACTIVE&&(d.kind.equals("KILL")||d.kind.equals("PAIR")))autoAttackEnabled=true;
    RuntimeState.Monster target=null;for(RuntimeState.Monster m:state.monsters()){String species=m.campaignRewardProfileId==null?m.id:m.campaignRewardProfileId;boolean wanted=st==CampaignProgress.Status.LOCKED||d.kind.equals("SKILL")||state.rpg().campaign().wanted(d,species);if(m.alive&&wanted&&(target==null||Math.hypot(m.x-state.player().x,m.y-state.player().y)<Math.hypot(target.x-state.player().x,target.y-state.player().y)))target=m;}
    if(target!=null){
      combat.selectTarget(target);
      activeWorld().requestMonsterApproach(target.id,CanonicalMeleeTileContract.REACH_DISTANCE);
      showFeedback("사냥 목표로 이동 · "+target.name+(autoAttackEnabled?" · 자동 공격":""),FeedbackTone.INFO);
    }else showFeedback("몬스터가 다시 나타날 때까지 쉬거나 보급하세요",FeedbackTone.INFO);
  }
  private void changeCampaignMap(String id,boolean fromNext){cancelSkillApproach();skillVfx.clear();combat.clearTarget();combat.cancelApproach();interaction.cancel();if(poteFieldAdapter!=null)poteFieldAdapter.cancel();state.enterCampaignMap(id,fromNext);poteFieldAdapter=new WorldRuntimeAdapter(state,logicalViewportWidth,H,PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.navigationTiles(),PoteFieldDef.obstacles(),true);poteFieldAdapter.snapCameraToPlayer();camera=poteFieldAdapter.camera();action=Action.IDLE;joy=false;vx=vy=0;directStepClock=0;checkpoint();showFeedback(com.projectdark.mobile.world.CampaignWorld.title(id),FeedbackTone.INFO);if(campaignAutoRoute)autoNavigateCampaign();}
  private boolean checkCampaignGate(){WorldMoveTargetController.TileCenter back=com.projectdark.mobile.world.CampaignWorld.backward(),forward=com.projectdark.mobile.world.CampaignWorld.forward();float x=state.player().x,y=state.player().y;if(Math.hypot(x-back.x,y-back.y)<22){String previous=com.projectdark.mobile.world.CampaignWorld.previous(state.currentMapId());if(previous==null){boolean route=campaignAutoRoute;leavePoteField();if(route)autoNavigateCampaign();}else changeCampaignMap(previous,true);return true;}String next=com.projectdark.mobile.world.CampaignWorld.next(state.currentMapId());if(next!=null&&Math.hypot(x-forward.x,y-forward.y)<22){changeCampaignMap(next,false);return true;}return false;}
  private void drawCampaignGates(Canvas c){if(!inPoteField)return;WorldMoveTargetController.TileCenter[] gates={com.projectdark.mobile.world.CampaignWorld.backward(),com.projectdark.mobile.world.CampaignWorld.forward()};for(int i=0;i<2;i++){String id=i==0?com.projectdark.mobile.world.CampaignWorld.previous(state.currentMapId()):com.projectdark.mobile.world.CampaignWorld.next(state.currentMapId());if(i==1&&id==null)continue;WorldMoveTargetController.TileCenter t=gates[i];p.setColor(0x8891b6bb);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawOval(t.x-22,t.y-11,t.x+22,t.y+11,p);p.setStyle(Paint.Style.FILL);text(c,id==null?"밀레스":com.projectdark.mobile.world.CampaignWorld.title(id),t.x-30,t.y-18,9);}}

  private void enterPoteField(){
    cancelSkillApproach();skillVfx.clear();activeSkillVisualId=null;
    fieldReturnX=state.player().x;fieldReturnY=state.player().y;F5mSaveStore.saveFieldReturnPointActive(fieldReturnX,fieldReturnY);autoAttackEnabled=false;combat.clearTarget();interaction.cancel();worldAdapter.cancelForAction();
    state.enterCampaignMap("MAP_POTE_01",false);inPoteField=true;questJournalModel.visitForest(f5mQuest,quest2);inventoryOpen=statsOpen=equipmentOpen=false;skillWindow.close();
    poteFieldAdapter=new WorldRuntimeAdapter(state,logicalViewportWidth,H,PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.navigationTiles(),PoteFieldDef.obstacles(),true);
    poteFieldAdapter.snapCameraToPlayer();camera=poteFieldAdapter.camera();action=Action.IDLE;joy=false;vx=vy=0;directStepClock=0f;checkpoint();showFeedback("포테의 숲 · 1구역 [B]",FeedbackTone.INFO);
  }
  private void leavePoteField(){
    cancelSkillApproach();skillVfx.clear();activeSkillVisualId=null;
    if(poteFieldAdapter!=null)poteFieldAdapter.cancel();combat.clearTarget();inPoteField=false;poteFieldAdapter=null;
    state.enterMillesFromField(fieldReturnX,Math.max(1510f,fieldReturnY-32f));worldAdapter.snapCameraToPlayer();camera=worldAdapter.camera();action=Action.IDLE;joy=false;vx=vy=0;checkpoint();showFeedback("밀레스",FeedbackTone.INFO);
  }
  private void updatePoteField(float dt){
    if(interaction.dialogOpen()||townWindow.isOpen()){action=Action.IDLE;return;}
    feedbackClock=Math.max(0,feedbackClock-dt);rewardClock=Math.max(0,rewardClock-dt);tapMarkerClock=Math.max(0,tapMarkerClock-dt);autoTargetHintClock=Math.max(0,autoTargetHintClock-dt);
    combat.tick(dt);combat.setBasicAttack(equipmentActions.resolveBasicAttack(state.rpg()).animationAction);tickSkillCombat(dt);state.tick(dt);state.applyDerivedGrowth();consumeLedger();consumeRewardNotice();monsterAi.tick(state,dt);
    if(!state.player().alive){cancelSkillApproach();autoAttackEnabled=false;action=Action.IDLE;playerFacing.endAttack();combat.clearTarget();combat.cancelApproach();poteFieldAdapter.cancelForAction();joy=false;vx=vy=0;knobX=JOY_X;knobY=JOY_Y;directStepClock=0f;return;}
    if(isActing()){actionClock+=dt;if(actionClock>=duration(action)){actionClock=0;activeSkillVisualId=null;playerFacing.endAttack();action=(joy&&(vx!=0||vy!=0))?Action.WALK:Action.IDLE;}return;}
    if(state.skillEffects().rooted("player")){activeWorld().cancelForAction();joy=false;return;}
    tickSkillApproach(dt);if(isActing())return;
    WorldRuntimeAdapter.FrameSnapshot navigation=poteFieldAdapter.tickNavigation(dt*state.skillEffects().speed());
    if(checkCampaignGate())return;
    if(joy&&(vx!=0||vy!=0)){directStepClock=Math.max(0f,directStepClock-dt*state.skillEffects().speed());if(!poteFieldAdapter.presentationMoving()&&directStepClock<=0f){WorldMoveTargetController.Snapshot step=poteFieldAdapter.step(joystickDirection());directStepClock=WorldMoveTargetController.TILE_STEP_SECONDS;applyWorldFacing(step.lastStepDirection);consumeMoveOutcome(step);}action=poteFieldAdapter.presentationMoving()?Action.WALK:Action.IDLE;if(action==Action.WALK)walkClock+=dt;combat.cancelApproach();return;}
    WorldMoveTargetController.Snapshot move=navigation.movement;if(move.status==WorldMoveTargetController.Status.MOVING||poteFieldAdapter.presentationMoving()){if(move.lastStepDirection!=null)applyWorldFacing(move.lastStepDirection);action=Action.WALK;walkClock+=dt;return;}consumeMoveOutcome(move);if(!isActing())action=Action.IDLE;tickSkillApproach(0);if(skillApproach.skillId==null&&!isActing())tickAutoAttack(move);
  }
  private void drawPoteField(Canvas c){p.setColor(0xff315f2c);c.drawRect(0,0,logicalViewportWidth,H,p);poteFieldRenderer.draw(c,poteFieldAdapter);}

  private void enterReagentShop(){enterTownInterior(TownInteriorDef.ALL.get(0));}
  private void restoreTownInterior(){TownInteriorDef d=TownInteriorDef.forMap(F5mSaveStore.savedMapIdActive());if(d==null)return;float[] r=F5mSaveStore.savedFieldReturnPointActive();millesReturnX=r[0];millesReturnY=r[1];townInterior=d;inReagentShop=true;state.enterTownInterior(d);if(d.kind==TownInteriorDef.Kind.INN&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.ACTIVE)state.ensureAdaptedMillesMouse();F5mSaveStore.restoreRuntimeActive(state);createTownAdapter();}
  private void createTownAdapter(){reagentShopAdapter=new WorldRuntimeAdapter(state,logicalViewportWidth,H,TownInteriorDef.MIN_X,TownInteriorDef.MAX_X,(townInterior.kind==TownInteriorDef.Kind.INN?64:TownInteriorDef.MIN_Y),TownInteriorDef.MAX_Y,townInterior.navigationTiles(),townInterior.obstacles(),true);reagentShopAdapter.snapCameraToPlayer();camera=reagentShopAdapter.camera();}
  private void enterTownInterior(TownInteriorDef d){cancelSkillApproach();skillVfx.clear();activeSkillVisualId=null;autoAttackEnabled=false;
    millesReturnX=state.player().x;millesReturnY=state.player().y;F5mSaveStore.saveFieldReturnPointActive(millesReturnX,millesReturnY);
    townInterior=d;inReagentShop=true;merchantApproach=false;reagentShopOpen=false;innDialogueOpen=false;townWindow.close();inventoryOpen=statsOpen=equipmentOpen=false;skillWindow.close();
    interaction.cancel();combat.cancelApproach();combat.clearTarget();worldAdapter.cancelForAction();state.enterTownInterior(d);if(d.kind==TownInteriorDef.Kind.INN&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.ACTIVE)state.ensureAdaptedMillesMouse();createTownAdapter();if(d.kind==TownInteriorDef.Kind.INN&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.RETURN_READY){reagentShopAdapter.requestGroundWorld(d.customerX(),d.customerY());}action=Action.IDLE;joy=false;vx=vy=0;directStepClock=0;checkpoint();showFeedback(d.title,FeedbackTone.INFO);if(questEntryPending){questEntryPending=false;autoNavigateQuest();}
  }
  private void leaveReagentShop(){combat.clearTarget();combat.cancelApproach();autoAttackEnabled=false;innDialogueOpen=false;cancelSkillApproach();skillVfx.clear();activeSkillVisualId=null;reagentShopAdapter.cancel();townWindow.close();merchantApproach=false;inReagentShop=false;reagentShopOpen=false;reagentShopAdapter=null;
    // Choose a legal canonical tile outside every door radius, preventing immediate re-entry.
    state.leaveTownInterior(millesReturnX,millesReturnY);
    float rx=millesReturnX,ry=millesReturnY;float best=Float.MAX_VALUE;for(WorldMoveTargetController.TileCenter t:worldAdapter.navigationTiles()){float dx=t.x-millesReturnX,dy=t.y-millesReturnY,dist=dx*dx+dy*dy;if(dist>1&&dist<best&&worldAdapter.canPlayerOccupy(t.x,t.y)&&worldAdapter.portalAt(t.x,t.y)==null){rx=t.x;ry=t.y;best=dist;}}
    state.player().x=rx;state.player().y=ry;worldAdapter.snapCameraToPlayer();camera=worldAdapter.camera();action=Action.IDLE;joy=false;vx=vy=0;checkpoint();showFeedback("밀레스",FeedbackTone.INFO);if(questExitPending){questExitPending=false;autoNavigateQuest();}
  }
  private void updateReagentShop(float dt){feedbackClock=Math.max(0,feedbackClock-dt);rewardClock=Math.max(0,rewardClock-dt);if(reagentShopAdapter==null)return;reagentShopOpen=townWindow.isOpen();if(reagentShopOpen||innDialogueOpen){action=Action.IDLE;return;}
    boolean inn=townInterior.kind==TownInteriorDef.Kind.INN;
    {combat.tick(dt);combat.setBasicAttack(equipmentActions.resolveBasicAttack(state.rpg()).animationAction);tickSkillCombat(dt);state.tick(dt);state.applyDerivedGrowth();consumeLedger();consumeRewardNotice();monsterAi.tick(state,dt);
      if(f5mQuest.state()!=F5mAdaptedPrologueQuest.State.ACTIVE)for(RuntimeState.Monster m:state.monsters())if(!m.alive)m.respawnClock=Float.POSITIVE_INFINITY;
      if(!state.player().alive){autoAttackEnabled=false;action=Action.IDLE;combat.clearTarget();reagentShopAdapter.cancelForAction();return;}
      if(isActing()){actionClock+=dt;if(actionClock>=duration(action)){actionClock=0;activeSkillVisualId=null;playerFacing.endAttack();action=Action.IDLE;}return;}
      if(state.skillEffects().rooted("player")){reagentShopAdapter.cancelForAction();joy=false;return;}
      tickSkillApproach(dt);if(isActing())return;
    }
    WorldRuntimeAdapter.FrameSnapshot nav=reagentShopAdapter.tickNavigation(dt*state.skillEffects().speed());
    if(inn&&!reagentShopAdapter.presentationMoving()){consumeMoveOutcome(nav.movement);tickSkillApproach(0);if(!isActing())tickAutoAttack(nav.movement);}

    if(joy&&(vx!=0||vy!=0)){directStepClock=Math.max(0,directStepClock-dt);if(!reagentShopAdapter.presentationMoving()&&directStepClock<=0){WorldMoveTargetController.Snapshot step=reagentShopAdapter.step(joystickDirection());directStepClock=WorldMoveTargetController.TILE_STEP_SECONDS;applyWorldFacing(step.lastStepDirection);}}
    if(nav.movement.lastStepDirection!=null)applyWorldFacing(nav.movement.lastStepDirection);if(reagentShopAdapter.presentationMoving()){action=Action.WALK;walkClock+=dt;}else if(!isActing())action=Action.IDLE;
    if(merchantApproach&&!reagentShopAdapter.presentationMoving()&&Math.abs(state.player().x-townInterior.customerX())<.01&&Math.abs(state.player().y-townInterior.customerY())<.01){merchantApproach=false;reagentShopAdapter.cancelForAction();if(townInterior.kind==TownInteriorDef.Kind.INN)innDialogueOpen=true;else{townWindow.open(townInterior,state);reagentShopOpen=true;}}
    if(townInterior.kind==TownInteriorDef.Kind.INN&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.RETURN_READY&&!reagentShopAdapter.presentationMoving()&&Math.abs(state.player().x-townInterior.customerX())<.01&&Math.abs(state.player().y-townInterior.customerY())<.01){innDialogueOpen=true;reagentShopAdapter.cancelForAction();}
    if(Math.abs(state.player().x-townInterior.exitX())<.01&&Math.abs(state.player().y-townInterior.exitY())<.01)leaveReagentShop();
  }
  private void drawReagentShopWorld(Canvas c){if(reagentShopAdapter==null)return;townRenderer.ground(c,reagentShopAdapter,townInterior);java.util.List<Runnable> draws=new java.util.ArrayList<>();java.util.List<Float> depths=new java.util.ArrayList<>();
    for(TownInteriorDef.Prop o:townInterior.props){draws.add(()->townRenderer.prop(c,reagentShopAdapter,o));depths.add(o.depthY());}
    draws.add(()->{WorldCameraTransform.Point m=reagentShopAdapter.worldToScreen(townInterior.npcX(),townInterior.npcY());townNpcRenderer.draw(c,townInterior,m.x,m.y,CharacterRenderer.Direction.SW);drawTownNpcName(c,townInterior.npcName,m.x,m.y);});depths.add(townInterior.npcY());
    draws.add(()->{c.save();c.translate(-reagentShopAdapter.camera().cameraX(),-reagentShopAdapter.camera().cameraY());drawCharacter(c);c.restore();});depths.add(reagentShopAdapter.presentationPlayerY());
    for(RuntimeState.Monster rat:state.monsters())if(rat.alive){draws.add(()->{WorldCameraTransform.Point q=reagentShopAdapter.worldToScreen(rat.x,rat.y);innMouseRenderer.draw(c,rat,q.x,q.y);if(combat.target()==rat)bar(c,q.x-12,q.y-18,q.x+12,q.y-15,0xffd63442,rat.hp/(float)rat.maxHp);if(rat.damagePopupClock>0)text(c,"-"+rat.lastDamage,q.x-8,q.y-23,10);});depths.add(rat.y);}
    java.util.List<Integer> order=new java.util.ArrayList<>();for(int i=0;i<draws.size();i++)order.add(i);java.util.Collections.sort(order,(a,b)->Float.compare(depths.get(a),depths.get(b)));for(int i:order)draws.get(i).run();
    c.save();c.translate(-reagentShopAdapter.camera().cameraX(),-reagentShopAdapter.camera().cameraY());skillVfx.draw(c,skillAnchors);c.restore();
  }
  private void drawReagentShop(Canvas c){townWindow.draw(c,hudCenterOffset);}
  private void drawInnDialogue(Canvas c){panel(c,190,330,770,468);text(c,"벤저민 · 밀레스 여관",218,360,13);String line=f5mQuest.state()==F5mAdaptedPrologueQuest.State.RETURN_READY?"여관 안의 생쥐를 정리했군요. 약속한 보급품을 받아요.":"어서 와요. 따뜻한 식사와 방이 준비되어 있어요.";drawWrappedText(c,line,218,388,500,11,18);if(f5mQuest.state()==F5mAdaptedPrologueQuest.State.RETURN_READY){p.setColor(0xD05B4727);c.drawRoundRect(new RectF(500,408,650,442),9,9,p);text(c,"보급품 받기",525,430,10);}else text(c,"화면을 터치해 닫기",218,444,9);}
  private boolean handleReagentShopTouch(MotionEvent e,float x,float y){if(e.getActionMasked()==MotionEvent.ACTION_DOWN){if(innDialogueOpen){if(f5mQuest.state()==F5mAdaptedPrologueQuest.State.RETURN_READY&&inside(x,y,500,408,650,442)){F5mTurnInCoordinator.Result tr=F5mQuestUiFlow.confirmTurnIn(f5mQuest,state.rpg());if(tr==F5mTurnInCoordinator.Result.COMPLETED){state.applyDerivedGrowth();quest2.unlockIfPrologueCompleted(f5mQuest);state.rpg().campaign().syncOpening(f5mQuest.state()==F5mAdaptedPrologueQuest.State.COMPLETED,quest2.state()==GrowthQuest2.State.COMPLETED);checkpoint();showReward(F5mQuestUiFlow.completionMessage(tr)+" · 다음: 제임스의 성장 훈련");}else showFeedback(F5mQuestUiFlow.completionMessage(tr),FeedbackTone.WARN);innDialogueOpen=false;return true;}innDialogueOpen=false;return true;}if(townWindow.isOpen()){townWindow.touch(x,y,hudCenterOffset,()->F5mSaveStore.writable()&&F5mSaveStore.checkpointActive());reagentShopOpen=townWindow.isOpen();return true;}if(reagentShopAdapter==null)return true;
    WorldCameraTransform.Point m=reagentShopAdapter.worldToScreen(townInterior.npcX(),townInterior.npcY());if(dist(x,y,m.x,m.y-24)<=36){joy=false;vx=vy=0;reagentShopAdapter.cancelForAction();if(townInterior.kind==TownInteriorDef.Kind.INN){combat.cancelApproach();autoAttackEnabled=false;merchantApproach=true;reagentShopAdapter.requestGroundWorld(townInterior.customerX(),townInterior.customerY());}else{townWindow.open(townInterior,state);reagentShopOpen=true;}action=Action.IDLE;return true;}
    if(townInterior.kind==TownInteriorDef.Kind.INN){
      if(!state.player().alive){if(dist(x,y,480,270)<=180){state.revivePlayer();reagentShopAdapter.snapCameraToPlayer();checkpoint();}return true;}
      if(inside(x,y,14,132,264,172)){autoNavigateQuest();return true;}
      if(circleHit(x,y,ATK_X+hudRightOffset,ATK_Y,ATK_R+4)){merchantApproach=false;attack();return true;}
      if(circleHit(x,y,AUTO_X+hudRightOffset,AUTO_Y,AUTO_R+3)){merchantApproach=false;cancelSkillApproach();autoAttackEnabled=!autoAttackEnabled;if(autoAttackEnabled){RuntimeState.Monster best=bestReachableAutoTarget();if(best!=null)combat.selectTarget(best);}return true;}
      for(int i=0;i<8;i++)if(slotRect(i).contains(x,y)){SkillBook.Entry entry=skillBook.get(skillBook.slot(i));if(entry!=null)useBookSkill(entry);return true;}
      WorldCameraTransform.Point target=reagentShopAdapter.screenToWorld(x,y);RuntimeState.Monster rat=state.hitMonster(target.x,target.y,18);if(rat!=null&&!isHudSurface(x,y)){merchantApproach=false;reagentShopAdapter.cancelForAction();combat.selectTarget(rat);return true;}
    }
    if(circleHit(x,y,JOY_X,JOY_Y,JOY_R)){merchantApproach=false;reagentShopAdapter.cancelForDirectInput();joy=true;directStepClock=0;stick(x,y);return true;}
    if(!isHudSurface(x,y)){merchantApproach=false;WorldCameraTransform.Point w=reagentShopAdapter.screenToWorld(x,y);reagentShopAdapter.requestGroundWorld(w.x,w.y);}return true;}
    if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&joy){stick(x,y);return true;}if(e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL){joy=false;vx=vy=0;knobX=JOY_X;knobY=JOY_Y;}return true;
  }

  private void drawWorld(Canvas c){
    java.util.List<AdaptedMillesMapRenderer.DepthDraw> actors=new java.util.ArrayList<>();
    actors.add(millesDepth(c,Float.NEGATIVE_INFINITY,()->drawTapMarker(c)));
    for(RuntimeState.Npc n:state.npcs())actors.add(millesDepth(c,n.y,()->drawNpc(c,n)));
    for(RuntimeState.Monster m:state.monsters())if(m.alive)actors.add(millesDepth(c,m.y,()->drawMonster(c,m)));
    actors.add(millesDepth(c,worldAdapter.presentationPlayerY(),()->drawCharacter(c)));
    mapRenderer.draw(c,worldAdapter,actors);
  }
  private AdaptedMillesMapRenderer.DepthDraw millesDepth(Canvas c,float y,Runnable draw){
    return new AdaptedMillesMapRenderer.DepthDraw(y,()->{c.save();c.translate(-camera.cameraX(),-camera.cameraY());draw.run();c.restore();});
  }
  private void drawTapMarker(Canvas c){if(tapMarkerClock<=0)return;float q=Math.max(0f,Math.min(1f,tapMarkerClock/.7f));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(0xD8FFD86B);c.drawCircle(tapMarkerX,tapMarkerY,8f+8f*(1f-q),p);p.setStyle(Paint.Style.FILL);p.setColor(0x66FFD86B);c.drawCircle(tapMarkerX,tapMarkerY,3.5f,p);}
  private void drawTownNpcName(Canvas c,String name,float x,float y){p.setTextSize(10);p.setTypeface(Typeface.DEFAULT_BOLD);float w=p.measureText(name);text(c,name,x-w/2,y-62,10);}
  private void drawNpcs(Canvas c){for(RuntimeState.Npc n:state.npcs())drawNpc(c,n);}
  private void drawNpc(Canvas c,RuntimeState.Npc n){if(n.id.startsWith("campaign_altar_")){boolean done=state.rpg().campaign().altarDone(n.id);p.setColor(0xff575854);c.drawOval(new RectF(n.x-22,n.y-12,n.x+22,n.y+10),p);p.setColor(0xffa5a393);c.drawRect(n.x-12,n.y-30,n.x+12,n.y-4,p);p.setColor(done?0xff83e5ce:0xffbd914d);c.drawOval(new RectF(n.x-11,n.y-36,n.x+11,n.y-24),p);text(c,done?"정화 완료":n.name,n.x-26,n.y-43,9);return;}CharacterRenderer.Direction facing=CharacterRenderer.Direction.SW;boolean selected=interaction.approachNpc()==n;worldEntityRenderer.draw(c,new WorldEntityPresentationRenderer.Pose(WorldEntityPresentationRenderer.Kind.NPC,n.x,n.y,facing,CharacterRenderer.State.IDLE,0f,0f,0f,CharacterRenderer.EffectFamily.NONE,false,selected,"npc/"+n.id,null));p.setTextSize(9);p.setTypeface(UiTheme.font(true));p.setColor(0xfff6e8c8);String label=n.id.startsWith("mentor_")||n.id.startsWith("piet_")?NpcIdentity.forId(n.id).name:n.name;float tw=p.measureText(label);p.setColor(0xB51C1710);c.drawRoundRect(new RectF(n.x-tw/2-4,n.y-64,n.x+tw/2+4,n.y-51),4,4,p);p.setColor(0xfff6e8c8);c.drawText(label,n.x-tw/2,n.y-54,p);if(selected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);p.setColor(0xfff0d17a);c.drawCircle(n.x,n.y-23,17,p);p.setStyle(Paint.Style.FILL);}}
  private void drawMonsters(Canvas c){for(RuntimeState.Monster m:state.monsters())drawMonster(c,m);}
  private void drawMonster(Canvas c,RuntimeState.Monster m){RuntimeState.Monster selected=combat.target();if(!m.alive)return;CharacterRenderer.Direction facing=inPoteField&&PoteForestMonsterShowcase.containsMonster(m.id)?PoteForestMonsterShowcase.presentationFacing(m):m.visualFacing.presentation();boolean isSelected=selected==m;CharacterRenderer.State poseState=WorldEntityPresentationRenderer.presentationState(m);float poseClock=m.attackPrimed?.24f-m.attackWindup:m.detourClock;float poseDuration=m.attackPrimed?.24f:1f;if(inPoteField&&PoteForestMonsterShowcase.containsMonster(m.id)){String pose=PoteForestMonsterShowcase.poseFor(m);poteFieldRenderer.drawMonsterTestPose(c,m.id,pose,facing,PoteForestMonsterShowcase.attackProgress(m),m.animationClock,m.x,m.y,m.hitFlash>0f);p.setTypeface(UiTheme.font(true));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(8);p.setColor(isSelected?0xffffe38b:0xfff3e8cd);c.drawText(m.name.replace(" [테스트 배치]",""),m.x,m.y-(PoteForestMonsterShowcase.bodyHeight(m.id)-11f),p);p.setTextAlign(Paint.Align.LEFT);}else if(MillesMousePresentation.isMouse(m.id)){innMouseRenderer.draw(c,m,m.x,m.y);}else worldEntityRenderer.draw(c,new WorldEntityPresentationRenderer.Pose(WorldEntityPresentationRenderer.Kind.MONSTER,m.x,m.y,facing,poseState,m.detourClock,poseClock,poseDuration,m.attackPrimed?CharacterRenderer.EffectFamily.PUNCH:CharacterRenderer.EffectFamily.NONE,m.hitFlash>0f,isSelected,"PENDING_CROP/milles/monster/"+m.id,null));if(m.attackPrimed&&!inPoteField){float q=1f-Math.min(1f,m.attackWindup/.24f);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.6f+1.8f*q);p.setColor(0xaaff7755);c.drawCircle(m.x,m.y-18,18+6*q,p);p.setStyle(Paint.Style.FILL);}float barTop=m.y-((inPoteField&&PoteForestMonsterShowcase.containsMonster(m.id))?(PoteForestMonsterShowcase.bodyHeight(m.id)-19f):MillesMousePresentation.isMouse(m.id)?20f:40f);bar(c,m.x-18,barTop,m.x+18,barTop+5f,0xffd63442,m.hp/(float)m.maxHp);if(m.damagePopupClock>0){p.setTextSize(12);p.setTypeface(UiTheme.font(true));p.setColor(0xffffdc72);String d="-"+m.lastDamage;float tw=p.measureText(d);float damageTop=inPoteField&&PoteForestMonsterShowcase.containsMonster(m.id)?(PoteForestMonsterShowcase.bodyHeight(m.id)+6f):MillesMousePresentation.isMouse(m.id)?24f:47f;c.drawText(d,m.x-tw/2,m.y-damageTop-(.65f-m.damagePopupClock)*20,p);}}
  static float monsterFacingX(CharacterRenderer.Direction d){return d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW?-3f:3f;}
  static float monsterFacingY(CharacterRenderer.Direction d){return d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE?-2f:2f;}
  private CharacterRenderer.Direction characterDirection(){return playerFacing.presentation();}
  private CharacterRenderer.State characterState(){if(!state.player().alive)return CharacterRenderer.State.DEAD;switch(action){case WALK:return CharacterRenderer.State.WALK;case CAST:return CharacterRenderer.State.CAST;case SKILL:case KICK:return CharacterRenderer.State.SKILL;case SWING:case THRUST:case THROW:case PUNCH:return CharacterRenderer.State.ATTACK;default:return CharacterRenderer.State.IDLE;}}
  private CharacterRenderer.EffectFamily characterEffectFamily(){switch(action){case CAST:return CharacterRenderer.EffectFamily.CAST;case THROW:return CharacterRenderer.EffectFamily.THROW;case PUNCH:return CharacterRenderer.EffectFamily.PUNCH;case KICK:return CharacterRenderer.EffectFamily.KICK;case SKILL:return CharacterRenderer.EffectFamily.SKILL;default:return CharacterRenderer.EffectFamily.NONE;}}
  static float skillPosePhase(float clock,float duration,float hitTime){return clock<hitTime?clock/Math.max(.001f,hitTime)/3f:1f/3f+(2f/3f)*(clock-hitTime)/Math.max(.001f,duration-hitTime);}
  float renderedPlayerWorldX(){return activeWorld().presentationPlayerX();}
  float renderedPlayerWorldY(){return activeWorld().presentationPlayerY();}
  private void drawCharacter(Canvas c){CharacterRenderer.State presentation=characterState();CharacterVisualBinding visuals=CharacterVisualBinding.from(state.rpg());float stateDuration=isActing()?duration(action):1f;AnimationAction visualAction=presentation==CharacterRenderer.State.ATTACK?equipmentActions.resolveBasicAttack(state.rpg()).animationAction:null;CharacterRenderer.Pose pose=new CharacterRenderer.Pose(renderedPlayerWorldX(),renderedPlayerWorldY(),characterDirection(),presentation,walkClock,actionClock,stateDuration,false,visuals.equipmentVisualRef(),visuals.weaponVisualRef(),CharacterRenderer.ASSET_STATUS,characterEffectFamily(),visualAction);
    SkillPresentationCatalog.Entry selected=skillPresentation.get(activeSkillVisualId);
    if(isActing()&&ChungryongWeaponRenderer.equipped(pose.weaponVisualRef)&&((selected==null&&presentation==CharacterRenderer.State.ATTACK)||(selected!=null&&selected.id.startsWith("SK_전사_")&&"SWING".equals(selected.motion)))){
      SkillActionContract.Rule rule=selected==null?null:SkillActionContract.get(selected.id);float contact=rule==null?CombatResolver.attackPrototype(combat.attackDef(),combat.attackMode()).hitTime:rule.contact;
      if(skillBodyRenderer.drawChungryong(c,pose,characterBodyIdentity,ChungryongWeaponRenderer.motion(activeSkillVisualId),skillPosePhase(actionClock,stateDuration,contact)))return;
    }
    if(selected!=null&&isActing()){
      SkillActionContract.Rule poseRule=SkillActionContract.get(activeSkillVisualId);
      float contact=poseRule==null?(action==Action.CAST?.24f:.14f):poseRule.contact;
      // Dara's preparation is standing. Raised arms belong only to the accepted release,
      // on the same contact clock as damage/recipient FX; never to button-down.
      if("CHARGE_CAST".equals(selected.motion)&&actionClock<contact){
        if("mm001".equals(characterBodyIdentity))characterRenderer.draw(c,new CharacterRenderer.Pose(pose.x,pose.y,pose.direction,CharacterRenderer.State.IDLE,walkClock,0,1,false,pose.equipmentVisualRef,pose.weaponVisualRef,pose.effectVisualRef,CharacterRenderer.EffectFamily.NONE));
        else skillBodyRenderer.draw(c,pose,characterBodyIdentity,"IDLE",0);
        return;
      }
      if("SWING".equals(selected.motion)&&"mm001".equals(characterBodyIdentity)){characterRenderer.draw(c,new CharacterRenderer.Pose(pose.x,pose.y,pose.direction,CharacterRenderer.State.ATTACK,walkClock,actionClock,stateDuration,false,pose.equipmentVisualRef,pose.weaponVisualRef,pose.effectVisualRef,pose.effectFamily,AnimationAction.SWING));return;}
      if(skillBodyRenderer.draw(c,pose,characterBodyIdentity,selected.motion,skillPosePhase(actionClock,stateDuration,contact)))return;
    }
    characterRenderer.draw(c,pose);}

  private void panel(Canvas c,float l,float t,float r,float b){UiTheme.surface(c,new RectF(l,t,r,b),0xe5101722,0x88607485,9);}
  private void modalPanel(Canvas c,float l,float t,float r,float b){UiTheme.surface(c,new RectF(l,t,r,b),UiTheme.BG,UiTheme.LINE,12);}
  private void text(Canvas c,String s,float x,float y,float size){p.setTypeface(UiTheme.font(true));p.setTextSize(size);p.setColor(UiTheme.TEXT);c.drawText(NpcIdentity.text(s),x,y,p);p.setTypeface(UiTheme.font(false));}
  private void mutedText(Canvas c,String s,float x,float y,float size){p.setTypeface(UiTheme.font(false));p.setTextSize(size);p.setColor(UiTheme.MUTED);c.drawText(NpcIdentity.text(s),x,y,p);}
  private void bar(Canvas c,float l,float t,float r,float b,int color,float ratio){ratio=Math.max(0,Math.min(1,ratio));p.setColor(0xCC171416);c.drawRoundRect(new RectF(l,t,r,b),(b-t)*.3f,(b-t)*.3f,p);p.setColor(color);c.drawRoundRect(new RectF(l,t,l+(r-l)*ratio,b),(b-t)*.3f,(b-t)*.3f,p);}

  private void drawHud(Canvas c){drawTopLeftStatus(c);int statusIndex=0;for(SkillEffectState.Effect e:state.skillEffects().playerEffects()){if(statusIndex>=3)break;SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(e.source);mutedText(c,(a==null?e.kind:a.name)+" "+Math.max(1,(int)Math.ceil(e.remaining))+"초",18,190+statusIndex++*12,8);}drawQuickQuest(c);drawTarget(c);drawMinimap(c);drawUtilityRail(c);drawChat(c);drawJoystick(c);drawCombatCluster(c);}
  private void drawTopLeftStatus(Canvas c){
    panel(c,14,12,264,124);Integer level=state.rpg().normalLevel();
    UiTheme.surface(c,new RectF(24,23,69,81),UiTheme.RAISED,UiTheme.LINE,9);
    UiTheme.center(c,"LV",46,39,8,UiTheme.GOLD,true);UiTheme.center(c,level==null?"?":String.valueOf(level),46,68,23,UiTheme.TEXT,true);
    drawReferenceResourceBar(c,82,39,249,46,"HP",state.player().hp,state.player().maxHp);
    drawReferenceResourceBar(c,82,70,249,77,"MP",state.player().mp,state.player().maxMp);
    Float ratio=PostF5mHudPresentation.expRatio(state.rpg());float q=ratio==null?0f:Math.max(0f,Math.min(1f,ratio));
    UiTheme.text(c,"경험치",25,104,9,UiTheme.MUTED,false);UiTheme.right(c,Math.round(q*100)+"%",249,104,9,UiTheme.GOLD,true);
    UiTheme.surface(c,new RectF(74,99,205,104),UiTheme.RAISED,0,3);if(q>0)UiTheme.surface(c,new RectF(74,99,74+131*q,104),UiTheme.GOLD,0,3);
  }
  private void centeredText(Canvas c,String value,float cx,float baseline,float size,boolean bold){p.setTypeface(UiTheme.font(bold));p.setTextSize(size);p.setColor(bold?0xfff4ead5:0xffb8aa91);c.drawText(value,cx-p.measureText(value)/2f,baseline,p);p.setTypeface(UiTheme.font(false));}
  private void drawReferenceResourceBar(Canvas c,float l,float t,float r,float b,String label,int value,int max){
    float q=Math.max(0,Math.min(1,value/(float)Math.max(1,max)));
    UiTheme.text(c,label,l,t-6,9,UiTheme.MUTED,true);UiTheme.right(c,value+" / "+max,r,t-6,10,UiTheme.TEXT,true);
    UiTheme.surface(c,new RectF(l,t,r,b),UiTheme.RAISED,0,4);if(q>0)UiTheme.surface(c,new RectF(l,t,l+(r-l)*q,b),label.equals("HP")?0xffe28d94:0xff81b4e8,0,4);
  }
  private void rightAlignedText(Canvas c,String value,float right,float baseline,float size){p.setTextSize(size);p.setTypeface(UiTheme.font(true));float width=p.measureText(value);text(c,value,right-width,baseline,size);}
  private void drawQuickQuest(Canvas c){QuestJournalModel.Row r=questJournalModel.current(f5mQuest,quest2,inPoteField,state.rpg());if(r==null)return;panel(c,14,132,264,172);UiTheme.glyph(c,"quest",28,150,7,UiTheme.GOLD);UiTheme.fit(c,r.title,43,148,142,11,UiTheme.TEXT,true);UiTheme.right(c,r.label(),249,148,8.5f,UiTheme.ACCENT,true);UiTheme.fit(c,r.objective,43,164,167,9.5f,UiTheme.MUTED,false);UiTheme.right(c,r.status==QuestJournalModel.Status.ACTIVE&&r.goal>0?r.count+"/"+r.goal+" ›":"›",250,164,10,UiTheme.GOLD,true);}
  private void drawQuestJournal(Canvas c){questJournalWindow.draw(c,questJournalModel.rows(f5mQuest,quest2,inPoteField,state.rpg()),hudCenterOffset);}
  private void openQuestJournal(){inventoryOpen=statsOpen=equipmentOpen=false;skillWindow.close();townWindow.close();reagentShopOpen=false;innDialogueOpen=false;interaction.cancel();joy=false;vx=vy=0;autoAttackEnabled=false;activeWorld().cancelForAction();combat.cancelApproach();questJournalWindow.open(questJournalModel.rows(f5mQuest,quest2,inPoteField,state.rpg()),questJournalModel.current(f5mQuest,quest2,inPoteField,state.rpg()));questJournalOpen=true;}
  static float questIconX(float rightOffset){return UTILITY_X0+4*UTILITY_STEP+rightOffset;}
private void drawQuestJournalIcon(Canvas c){float cx=questIconX(hudRightOffset);p.setColor(questJournalOpen?0xff5e3f24:0xD01C1815);c.drawCircle(cx,UTILITY_Y0,UTILITY_R,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(questJournalOpen?2:1);p.setColor(questJournalOpen?0xffffd477:0xB6BB8753);c.drawCircle(cx,UTILITY_Y0,UTILITY_R,p);p.setStrokeWidth(1.4f);p.setColor(0xffc7a46b);c.drawRoundRect(new RectF(cx-7,18,cx+7,38),2,2,p);c.drawLine(cx-3,23,cx+4,23,p);c.drawLine(cx-3,28,cx+4,28,p);c.drawLine(cx-3,33,cx+2,33,p);p.setStyle(Paint.Style.FILL);centeredText(c,"퀘스트",cx,53,6.3f,true);int n=questJournalModel.attention(questJournalModel.rows(f5mQuest,quest2,inPoteField,state.rpg()));if(n>0){p.setColor(0xffbe8839);c.drawCircle(cx+10,15,6,p);p.setTextSize(7);String badge=String.valueOf(n);text(c,badge,cx+10-p.measureText(badge)/2,17.5f,7);}}
  private void drawTarget(Canvas c){RuntimeState.Monster selected=combat.target();if(selected==null)return;c.save();c.translate(hudCenterOffset,0);panel(c,380,12,580,55);text(c,selected.name,397,31,9.5f);bar(c,397,39,563,48,0xffd63e49,selected.hp/(float)selected.maxHp);c.restore();}
  private void drawMinimap(Canvas c){float d=hudRightOffset;panel(c,824+d,12,948+d,112);text(c,"CH. 5-1",856+d,30,9);p.setColor(0xB7232B1F);c.drawRect(834+d,38,938+d,92,p);WorldRuntimeAdapter aw=activeWorld();float minX=inReagentShop?TownInteriorDef.MIN_X:inPoteField?PoteFieldDef.MIN_X:WorldDef.MIN_X,maxX=inReagentShop?TownInteriorDef.MAX_X:inPoteField?PoteFieldDef.MAX_X:WorldDef.MAX_X,minY=inReagentShop?TownInteriorDef.MIN_Y:inPoteField?PoteFieldDef.MIN_Y:WorldDef.MIN_Y,maxY=inReagentShop?TownInteriorDef.MAX_Y:inPoteField?PoteFieldDef.MAX_Y:WorldDef.MAX_Y;float nx=(aw.presentationPlayerX()-minX)/Math.max(1f,maxX-minX),ny=(aw.presentationPlayerY()-minY)/Math.max(1f,maxY-minY);float px=837+d+Math.max(0,Math.min(1,nx))*98,py=41+Math.max(0,Math.min(1,ny))*48;p.setColor(0xffffd44f);c.drawCircle(px,py,3.5f,p);mutedText(c,inReagentShop?townInterior.title:inPoteField?com.projectdark.mobile.world.CampaignWorld.title(state.currentMapId()):"밀레스",inPoteField?854+d:869+d,105,8);}
private void drawUtilityRail(Canvas c){String[] labels={"가방","능력치","장비","스킬"};HudSpriteCatalog.Sprite[] icons={HudSpriteCatalog.Sprite.INVENTORY,HudSpriteCatalog.Sprite.STATUS,HudSpriteCatalog.Sprite.EQUIPMENT,HudSpriteCatalog.Sprite.QUEST};for(int i=0;i<labels.length;i++){float cx=UTILITY_X0+i*UTILITY_STEP+hudRightOffset,cy=UTILITY_Y0;boolean active=i==0&&inventoryOpen||i==1&&statsOpen||i==2&&equipmentOpen||i==3&&skillWindow.open;p.setColor(active?0xE35E3F24:0xD01C1815);c.drawCircle(cx,cy,UTILITY_R,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(active?2f:1f);p.setColor(active?0xffffd477:0xB6BB8753);c.drawCircle(cx,cy,UTILITY_R,p);p.setStyle(Paint.Style.FILL);hudSprites.draw(c,icons[i],new RectF(cx-13,cy-13,cx+13,cy+13),255);p.setTextSize(6.3f);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setColor(0xffeee0c6);float tw=p.measureText(labels[i]);c.drawText(labels[i],cx-tw/2,cy+25,p);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));}drawQuestJournalIcon(c);}
  private void drawStats(Canvas c){if(!statsOpen)return;c.save();c.translate(hudCenterOffset,0);UiTheme.scrim(c);UiTheme.panel(c,new RectF(570,72,936,390),"능력치");UiTheme.close(c,911,91);RpgProgressionState r=state.rpg();FinalStats fs=r.finalStats().withEffects(state.skillEffects(),"player");RpgProgressionState.StatSnapshot ss=r.recomputeStats();UiTheme.text(c,"Lv. "+r.normalLevel(),590,126,11,UiTheme.MUTED,false);UiTheme.right(c,"잔여 포인트  "+r.statPoints(),909,126,11,UiTheme.ACCENT,true);
    String[] keys={"STR","INT","WIS","CON","DEX"},labels=keys;int[] base={r.str(),r.intel(),r.wis(),r.con(),r.dex()};
    for(int n=0;n<5;n++){float yy=150+n*30;UiTheme.surface(c,new RectF(584,yy-17,752,yy+11),UiTheme.SURFACE,0,5);UiTheme.text(c,labels[n],594,yy,11,UiTheme.TEXT,true);Integer bonus=ss.equipment.get(keys[n]);UiTheme.right(c,base[n]+(bonus!=null&&bonus!=0?" +"+bonus:""),693,yy,12,UiTheme.GOLD,true);UiTheme.button(c,new RectF(705,yy-15,743,yy+9),"+",r.statPoints()>0,false);}
    String[] labels2={"생명력","마나","방어 / 마법방어","명중 / 피해","물리 공격","공격 속성","방어 속성","피해 감소","방어 무시"};
    String[] vals={state.player().hp+" / "+fs.maxHp,state.player().mp+" / "+fs.maxMp,fs.ac+" / "+fs.magicDefense,fs.hit+" / "+fs.dam,""+fs.prototypePhysicalAttack(),"NONE".equals(String.valueOf(fs.attackElement))?"없음":""+fs.attackElement,"NONE".equals(String.valueOf(fs.defenseElement))?"없음":""+fs.defenseElement,fs.damageReductionPct+"% · "+fs.flatMitigation,""+fs.acIgnore};
    for(int n=0;n<9;n++){float yy=150+n*23;UiTheme.text(c,labels2[n],771,yy,9.5f,UiTheme.MUTED,false);UiTheme.right(c,vals[n],915,yy,10.5f,UiTheme.TEXT,true);if(n<8)UiTheme.line(c,771,yy+7,915,yy+7,0xff283747);}
    UiTheme.line(c,590,357,915,357,UiTheme.LINE);UiTheme.text(c,"CON · WIS는 다음 레벨의 HP · MP 성장에 반영됩니다.",590,378,10,UiTheme.MUTED,false);c.restore();
  }
  private void drawChat(Canvas c){c.save();c.translate(hudCenterOffset,0);RuntimeMetrics metrics=state.metrics();float top=chatExpanded?418:454;p.setColor(0xd9101722);c.drawRoundRect(new RectF(CHAT_LEFT,top,CHAT_RIGHT,522),8,8,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xff354656);c.drawRoundRect(new RectF(CHAT_LEFT+.5f,top+.5f,CHAT_RIGHT-.5f,521.5f),8,8,p);p.setStyle(Paint.Style.FILL);text(c,"전체    지역    파티    길드",CHAT_LEFT+16,top+20,8.5f);mutedText(c,chatExpanded?"▾":"⋯",CHAT_RIGHT-23,top+20,10);p.setColor(0xff354656);c.drawRect(CHAT_LEFT+14,top+26,CHAT_RIGHT-14,top+27,p);if(chatExpanded){mutedText(c,"[지역] 밀레스에 오신 것을 환영합니다.",CHAT_LEFT+16,462,8);mutedText(c,"[전투] 격파 "+metrics.monsterDefeats()+" · 피격 "+metrics.playerHits()+" · DMG "+metrics.damageDealt(),CHAT_LEFT+16,482,8);mutedText(c,"[시스템] NPC · 전투 · 보상 알림",CHAT_LEFT+16,501,8);p.setColor(0xff243242);c.drawRoundRect(new RectF(CHAT_LEFT+12,505,CHAT_RIGHT-12,518),5,5,p);mutedText(c,"메시지를 입력하세요.",CHAT_LEFT+22,516,7.5f);}else{mutedText(c,"[전투] 격파 "+metrics.monsterDefeats()+" · 피격 "+metrics.playerHits()+" · DMG "+metrics.damageDealt(),CHAT_LEFT+16,499,8);}c.restore();}
  private void drawJoystick(Canvas c){p.setColor(joy?0x7A3D4652:0x70101722);c.drawCircle(JOY_X,JOY_Y,JOY_R,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(joy?2.5f:1.6f);p.setColor(joy?0xE8E0C487:0xaaa6b6c7);c.drawCircle(JOY_X,JOY_Y,JOY_R,p);p.setStrokeWidth(1);p.setColor(0x657F7562);c.drawCircle(JOY_X,JOY_Y,36,p);p.setStyle(Paint.Style.FILL);p.setColor(joy?0xE4C7A86B:0xaa344e57);c.drawCircle(knobX,knobY,joy?24:22,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xff83d5dc);c.drawCircle(knobX,knobY,joy?24:22,p);p.setStyle(Paint.Style.FILL);}
  private RectF slotRect(int index){int col=index%5,row=index/5;float l=SLOT_X0+hudRightOffset+col*(SLOT+SLOT_GAP),t=SLOT_Y0+row*(SLOT+SLOT_GAP);return new RectF(l,t,l+SLOT,t+SLOT);}
  private void drawCombatCluster(Canvas c){
    float d=hudRightOffset;UiTheme.text(c,"퀵 스킬",SLOT_X0+d,369,8.5f,UiTheme.TEXT,true);UiTheme.right(c,"물약",892+d,369,8.5f,UiTheme.MUTED,false);
    for(int i=0;i<10;i++){RectF r=slotRect(i);UiTheme.slot(c,r,false,false);if(i<8){skillWindow.drawSlot(c,r,i,skillActions);UiTheme.text(c,String.valueOf(i+1),r.left+4,r.top+10,7.5f,UiTheme.MUTED,true);}else {UiTheme.glyph(c,"potion",r.centerX(),r.centerY()-3,8,i==8?0xffe18d8d:0xff88b7df);UiTheme.text(c,(i==8?"HP ":"MP ")+inventoryQuantity(i==8?RpgProgressionState.B_SMALL_POTION_ITEM_ID:"IT_B_MP_POTION"),r.left+5,r.bottom-5,7,UiTheme.TEXT,true);}}
    boolean ready=combat.hasUsableTarget()&&combat.attackReady()&&state.player().alive;
    UiTheme.utility(c,ATK_X+d,ATK_Y,"attack",ready,ATK_R);UiTheme.utility(c,AUTO_X+d,AUTO_Y,"auto",autoAttackEnabled,AUTO_R);drawModeControl(c);
    UiTheme.center(c,"AUTO",AUTO_X+d,529,7.5f,autoAttackEnabled?UiTheme.ACCENT:UiTheme.MUTED,true);UiTheme.center(c,"모드",MODE_X+d,529,7.5f,UiTheme.MUTED,true);
  }
  private void drawModeControl(Canvas c){UiTheme.utility(c,MODE_X+hudRightOffset,MODE_Y,"mode","MODE".equals(pressedControl),MODE_R);}
  private void circleIcon(Canvas c,float cx,float cy,float r,ClassicHudIconAtlas.Icon icon,boolean enabled,boolean pressed){RectF b=new RectF(cx-r,cy-r,cx+r,cy+r);hudIcons.draw(c,p,icon,b,enabled,false,pressed);}
  private void cooldown(Canvas c,float x,float y,float r,float remaining,float total){if(remaining<=0||total<=0)return;float ratio=Math.min(1f,remaining/total);p.setColor(0xA9000000);c.drawArc(new RectF(x-r,y-r,x+r,y+r),-90,360*ratio,true,p);p.setTextSize(8);p.setColor(Color.WHITE);String s=String.format(java.util.Locale.US,"%.1f",remaining);float tw=p.measureText(s);c.drawText(s,x-tw/2,y+3,p);}

  private void drawFeedbackBanners(Canvas c){c.save();c.translate(hudCenterOffset,0);if(rewardClock>0){p.setColor(0xD02B2517);c.drawRoundRect(new RectF(346,74,614,106),16,16,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);p.setColor(0xBADBB768);c.drawRoundRect(new RectF(346,74,614,106),16,16,p);p.setStyle(Paint.Style.FILL);p.setTextSize(10);p.setTypeface(UiTheme.font(true));p.setColor(0xffffe5a3);float tw=p.measureText(rewardBanner);c.drawText(rewardBanner,480-tw/2,95,p);}else if(feedbackClock>0){int fill=feedbackTone==FeedbackTone.WARN?0xD04A2226:feedbackTone==FeedbackTone.REWARD?0xD03C321E:0xCC161A20;p.setColor(fill);p.setTextSize(9.2f);float width=Math.min(340,Math.max(146,p.measureText(feedback)+38));c.drawRoundRect(new RectF(480-width/2,111,480+width/2,139),14,14,p);p.setColor(feedbackTone==FeedbackTone.WARN?0xffffb4ad:0xffeee5d3);float ft=p.measureText(feedback);c.drawText(feedback,480-ft/2,130,p);}c.restore();}

  private final ItemWindow.Visuals itemWindowVisuals=new ItemWindow.Visuals(){
    public void icon(Canvas c,RpgInventoryPresentation.ItemRow row,float x,float y,float size){drawInventoryItemIcon(c,row,x,y,size);}
    public void actor(Canvas c,float x,float y){CharacterVisualBinding visuals=CharacterVisualBinding.from(state.rpg());c.save();c.translate(x,y);c.scale(2f,2f);characterRenderer.draw(c,new CharacterRenderer.Pose(0,0,CharacterRenderer.Direction.SW,CharacterRenderer.State.IDLE,0,0,1,false,visuals.equipmentVisualRef(),visuals.weaponVisualRef(),CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));c.restore();}
  };
  private void drawInventory(Canvas c){if(inventoryOpen){c.save();c.translate(hudCenterOffset,0);itemWindow.inventory(c,state.rpg(),rpgInteraction.selectedInventoryItemId(),itemWindowVisuals);c.restore();}}

  private void classicWindow(Canvas c,float l,float t,float r,float b,String title){
    p.setColor(0xF02A1D16);c.drawRect(l,t,r,b,p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(0xFF4B2D1D);c.drawRect(l+2,t+2,r-2,b-2,p);
    p.setStrokeWidth(1.4f);p.setColor(0xFFD0A066);c.drawRect(l+7,t+7,r-7,b-7,p);p.setStyle(Paint.Style.FILL);
    p.setColor(0xFF3B271B);c.drawRect(l+10,t+10,r-10,t+42,p);text(c,title,l+25,t+33,14);
  }
  private void drawInventoryItemIcon(Canvas c,RpgInventoryPresentation.ItemRow row,float l,float t,float size){
    RpgProgressionState.ItemDefinition d=state.rpg().itemDefinitions().get(row.itemId);
    if(ChungryongWeaponRenderer.ITEM.equals(row.itemId)){Bitmap b=chungryongWeapon.icon;float sc=Math.min(size/b.getWidth(),size/b.getHeight());float w=b.getWidth()*sc,h=b.getHeight()*sc;p.setFilterBitmap(false);c.drawBitmap(b,null,new RectF(l+(size-w)/2,t+(size-h)/2,l+(size+w)/2,t+(size+h)/2),p);return;}
    Bitmap reagent=reagentVisuals.get(row.itemId);if(reagent!=null){float sc=Math.min(size/reagent.getWidth(),size/reagent.getHeight()),w=reagent.getWidth()*sc,h=reagent.getHeight()*sc;p.setFilterBitmap(false);c.drawBitmap(reagent,null,new RectF(l+(size-w)/2,t+(size-h)/2,l+(size+w)/2,t+(size+h)/2),p);return;}
    Bitmap catalog=ItemIconCatalog.reagent(row.itemId);if(catalog==null)catalog=ItemIconCatalog.get(row.itemId,d);if(catalog!=null){float sc=Math.min(size/catalog.getWidth(),size/catalog.getHeight()),w=catalog.getWidth()*sc,h=catalog.getHeight()*sc;p.setFilterBitmap(false);c.drawBitmap(catalog,null,new RectF(l+(size-w)/2,t+(size-h)/2,l+(size+w)/2,t+(size+h)/2),p);return;}
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
    String[] keys={"AC","MAGIC_DEFENSE","HP","MP","DAM","HIT","STR","INT","WIS","CON","DEX"};int shown=0;
    for(String key:keys){int a=mod(current,key),b=mod(next,key);if(a==0&&b==0)continue;int delta=b-a;float yy=y+shown*16;
      mutedText(c,key.equals("MAGIC_DEFENSE")?"MDEF":key,x,yy+10,6.5f);mutedText(c,a+" → "+b,x+38,yy+10,7.2f);if(delta!=0)text(c,(delta>0?"+":"")+delta,x+112,yy+10,7.5f);shown++;if(shown>=3)break;}
    if(shown==0)mutedText(c,"장비 능력치 변화 없음",x,y+11,7.2f);
  }
  private void drawItemStatPanel(Canvas c,RpgProgressionState.ItemDefinition d,float x,float y,float width){
    if(d==null){mutedText(c,"능력치 정보 없음",x,y+12,8);return;}
    String[] keys={"AC","MAGIC_DEFENSE","HP","MP","DAM","HIT","STR","INT","WIS","CON","DEX"};
    int shown=0;
    for(String key:keys){
      Integer v=d.statModifiers.get(key);if(v==null||v==0)continue;
      float col=shown%2,row=shown/2, bx=x+col*(width/2),by=y+row*27;
      p.setColor(0xCC2B2119);c.drawRoundRect(new RectF(bx,by,bx+width/2-5,by+22),4,4,p);
      mutedText(c,key.equals("MAGIC_DEFENSE")?"MDEF":key,bx+6,by+14,7);text(c,(v>0?"+":"")+v,bx+42,by+15,9);shown++;
      if(shown>=6)break;
    }
    if(shown==0)mutedText(c,d.equippable()?"추가 능력치 없음":"소비/기타 아이템",x,y+14,8);
  }
  private String itemModifierLabel(RpgProgressionState.ItemDefinition d){if(d==null||d.statModifiers.isEmpty())return "";StringBuilder s=new StringBuilder();for(Map.Entry<String,Integer> e:d.statModifiers.entrySet()){if(s.length()>0)s.append(" · ");s.append("MAGIC_DEFENSE".equals(e.getKey())?"MDEF":e.getKey()).append(" ").append(e.getValue()>0?"+":"").append(e.getValue());}return s.toString();}
  private String equipmentCompareLabel(RpgProgressionState.ItemDefinition d){if(d==null||!d.equippable())return "";FinalStats now=state.rpg().finalStats();Map<String,String> eq=state.rpg().equipment();RpgProgressionState.ItemDefinition old=state.rpg().itemDefinitions().get(eq.get(d.equipSlot));int dam=now.dam-oldMod(old,"DAM")+mod(d,"DAM"),hit=now.hit-oldMod(old,"HIT")+mod(d,"HIT"),ac=now.ac-oldMod(old,"AC")+mod(d,"AC"),dex=now.dex-oldMod(old,"DEX")+mod(d,"DEX");if("무기".equals(d.equipSlot))return "장착 시 DAM "+now.dam+"→"+dam+" · HIT "+now.hit+"→"+hit;if("방패".equals(d.equipSlot)||"갑옷".equals(d.equipSlot)||"모자".equals(d.equipSlot)||"장갑".equals(d.equipSlot))return "장착 시 AC "+now.ac+"→"+ac;if("신발".equals(d.equipSlot))return "장착 시 DEX "+now.dex+"→"+dex;return "장착 시 최종 능력치에 즉시 반영";}
  private static int mod(RpgProgressionState.ItemDefinition d,String k){if(d==null)return 0;Integer v=d.statModifiers.get(k);return v==null?0:v;} private static int oldMod(RpgProgressionState.ItemDefinition d,String k){return mod(d,k);}
  private void drawEquipment(Canvas c){if(equipmentOpen){c.save();c.translate(hudCenterOffset,0);itemWindow.equipment(c,state.rpg(),rpgInteraction.selectedInventoryItemId(),itemWindowVisuals);c.restore();}}
  private boolean handleEquipmentTouch(float x,float y){
    ItemWindow.Hit hit=itemWindow.equipmentTouch(x,y,state.rpg());
    if(hit==ItemWindow.Hit.CLOSE){equipmentOpen=false;return true;}
    if(hit==ItemWindow.Hit.DETAILS){equipmentOpen=false;statsOpen=true;return true;}
    if(hit==ItemWindow.Hit.SELECT||hit==ItemWindow.Hit.ACTION)rpgInteraction.selectInventoryItem(state.rpg(),itemWindow.hitItem);
    if(hit==ItemWindow.Hit.ACTION)activateInventoryItem();
    return true;
  }
  private String equipResultLabel(RpgProgressionState.EquipResult result){switch(result){case EQUIPPED:return "장착 완료";case UNEQUIPPED:return "해제 완료";case REQUIREMENT_NOT_MET:return "장착 불가 · 조건 미충족";case REQUIREMENT_PENDING:return "장착 보류 · 조건 확인 필요";case NOT_EQUIPPABLE:return "장착할 수 없는 아이템";case UNKNOWN_ITEM:return "알 수 없는 아이템";case ITEM_NOT_OWNED:default:return "보유하지 않은 아이템";}}
  private boolean handleInventoryTouch(float x,float y){
    if(!inventoryOpen)return false;
    ItemWindow.Hit hit=itemWindow.inventoryTouch(x,y,state.rpg());
    if(hit==ItemWindow.Hit.CLOSE){inventoryOpen=false;itemWindow.details=false;return true;}
    if(hit==ItemWindow.Hit.SELECT)rpgInteraction.selectInventoryItem(state.rpg(),itemWindow.hitItem);
    if(hit==ItemWindow.Hit.ACTION)activateInventoryItem();
    return true;
  }
  private void activateInventoryItem(){
    String selected=rpgInteraction.selectedInventoryItemId();
    if(state.rpg().isRecall(selected)){if(state.rpg().useMillesRecall(state)==RpgProgressionState.UseResult.USED){if(inReagentShop)leaveReagentShop();if(inPoteField)leavePoteField();state.player().x=WorldDef.PLAYER_SPAWN_X;state.player().y=WorldDef.PLAYER_SPAWN_Y;worldAdapter.cancelForAction();worldAdapter.snapCameraToPlayer();inventoryOpen=false;checkpoint();showFeedback("밀레스마을로 귀환했습니다",FeedbackTone.INFO);}return;}
    if(selected!=null&&state.rpg().isConsumable(selected)){RpgProgressionState.UseResult used=state.rpg().useConsumable(selected,state);checkpoint();showFeedback(used==RpgProgressionState.UseResult.USED?"회복 시약을 사용했습니다":used==RpgProgressionState.UseResult.NO_EFFECT?"HP가 이미 가득 찼습니다":"사용할 수 없습니다",used==RpgProgressionState.UseResult.USED?FeedbackTone.REWARD:FeedbackTone.WARN);return;}
    RpgProgressionState.EquipResult result=rpgInteraction.equipSelectedDetailed(state.rpg());state.applyDerivedGrowth();checkpoint();showFeedback(equipResultLabel(result),(result==RpgProgressionState.EquipResult.EQUIPPED||result==RpgProgressionState.EquipResult.UNEQUIPPED)?FeedbackTone.INFO:FeedbackTone.WARN);
  }

  private boolean poteTravelDialogue(){RuntimeState.Npc n=interaction.dialogNpc();return n!=null&&("pote_travel_guide".equals(n.id)||("milles_gate_proto".equals(n.id)&&quest2.state()==GrowthQuest2.State.COMPLETED));}
  private boolean f5mGuideDialogue(){RuntimeState.Npc n=interaction.dialogNpc();return n!=null&&"milles_guide_proto".equals(n.id);}
  private boolean supplyDialogue(){RuntimeState.Npc n=interaction.dialogNpc();return n!=null&&"milles_service_proto".equals(n.id);}
  private boolean jobCounselorDialogue(){RuntimeState.Npc n=interaction.dialogNpc();return n!=null&&"milles_job_counselor".equals(n.id);}
  private String[] initialJobs(){return new String[]{"WARRIOR","ROGUE","MAGE","CLERIC","MARTIAL_ARTIST"};}
  private String[] initialJobLabels(){return new String[]{"전사","도적","마법사","성직자","무도가"};}
  private String initialSkill(String job){switch(job){case "WARRIOR":return "SK_전사_001";case "ROGUE":return "SK_도적_001";case "MAGE":return "SK_마법사_001";case "CLERIC":return "SK_성직자_013";case "MARTIAL_ARTIST":return "SK_무도가_001";default:return null;}}
  private void drawDialogue(Canvas c){RuntimeState.Npc dialogNpc=interaction.dialogNpc();if(dialogNpc==null)return;p.setColor(0x74000000);c.drawRect(0,0,W,H,p);modalPanel(c,190,300,770,462);p.setColor(0xC95A472A);c.drawRoundRect(new RectF(210,316,338,345),8,8,p);text(c,dialogNpc.name,226,335,11.5f);if(campaignDialogue()){drawCampaignDialogue(c,dialogNpc);}else if(jobCounselorDialogue()){boolean ready=f5mQuest.state()==F5mAdaptedPrologueQuest.State.COMPLETED&&quest2.state()==GrowthQuest2.State.COMPLETED&&state.rpg().normalLevel()!=null&&state.rpg().normalLevel()>=3;drawWrappedText(c,ready?"원하는 기본 직업을 한 번 선택하세요. 입문 장비는 무료로 지급합니다.":"직업 선택은 성장 훈련을 마치고 Lv3에 도달하면 열립니다.",218,372,510,11,18);if(ready&&"COMMONER".equals(state.rpg().currentJobCode())){String[] labels=initialJobLabels();for(int i=0;i<labels.length;i++){float x=i<3?218+i*173:304+(i-3)*173;float y=i<3?398:430;p.setColor(0xD05B4727);c.drawRoundRect(new RectF(x,y,x+154,y+27),7,7,p);text(c,labels[i]+" 선택",x+38,y+18,9);}}else mutedText(c,"현재 직업 · "+state.rpg().currentJobCode(),218,430,9);}else if(f5mGuideDialogue()){F5mAdaptedPrologueQuest.State qs=f5mQuest.state();if(qs==F5mAdaptedPrologueQuest.State.COMPLETED&&quest2.state()!=GrowthQuest2.State.LOCKED&&quest2.state()!=GrowthQuest2.State.COMPLETED){GrowthQuest2.State q2=quest2.state();String body=q2==GrowthQuest2.State.AVAILABLE?"좋아. 이번에는 훈련용 몬스터 세 마리를 상대하며 성장해 보게.":q2==GrowthQuest2.State.ACTIVE?"성장 훈련 중이군. 세 마리를 모두 처치하고 돌아오게.":"충분히 성장했군. 완료를 눌러 보상을 받게.";drawWrappedText(c,body,218,372,510,11,18);if(q2==GrowthQuest2.State.AVAILABLE||q2==GrowthQuest2.State.RETURN_READY){p.setColor(0xD05B4727);c.drawRoundRect(new RectF(500,408,606,442),9,9,p);text(c,q2==GrowthQuest2.State.AVAILABLE?"수락":"완료",538,430,10);}}else{String body=qs==F5mAdaptedPrologueQuest.State.AVAILABLE?"밀레스 여관 안의 생쥐 한 마리를 정리해 주겠나?":qs==F5mAdaptedPrologueQuest.State.ACTIVE?"여관 안의 생쥐를 처치하고 벤저민에게 보고하게.":qs==F5mAdaptedPrologueQuest.State.RETURN_READY?"잘 해냈군. 완료를 눌러 보상을 받게.":"첫 훈련은 완료되었네.";drawWrappedText(c,body,218,372,510,11,18);if(qs==F5mAdaptedPrologueQuest.State.AVAILABLE){p.setColor(0xD05B4727);c.drawRoundRect(new RectF(500,408,606,442),9,9,p);text(c,"수락",538,430,10);p.setColor(0xA0343030);c.drawRoundRect(new RectF(620,408,726,442),9,9,p);text(c,"거절",658,430,10);}else if(qs==F5mAdaptedPrologueQuest.State.RETURN_READY){p.setColor(0xD05B4727);c.drawRoundRect(new RectF(500,408,606,442),9,9,p);text(c,"완료",538,430,10);}}}else if(poteTravelDialogue()){drawWrappedText(c,"포테의 숲으로 이동하시겠습니까?",218,372,510,11,18);p.setColor(0xD05B4727);c.drawRoundRect(new RectF(500,408,606,442),9,9,p);text(c,"이동",538,430,10);}else if(supplyDialogue()){drawWrappedText(c,"아이템 상점 [B]  물약 20G · 목도 120G · 가죽장갑 80G · 신발 60G",218,366,510,10,16);mutedText(c,"보유 Gold "+state.rpg().gold(),218,389,9);String[] sn={"물약","목도","장갑","신발"};for(int i=0;i<4;i++){float bx=218+i*128;p.setColor(0xD05B4727);c.drawRoundRect(new RectF(bx,408,bx+112,442),9,9,p);text(c,sn[i],bx+36,430,9);}}else{drawWrappedText(c,dialogNpc.dialogue,218,372,510,11,18);mutedText(c,"화면을 터치하면 닫기",616,446,8);}p.setColor(0xB024262A);c.drawCircle(744,321,14,p);text(c,"×",739,326,14);}
  private void drawDeath(Canvas c){if(state.player().alive)return;p.setColor(0xB6000000);c.drawRect(0,0,W,H,p);modalPanel(c,330,205,630,335);text(c,"행동 불능",428,242,18);mutedText(c,"프로토타입 부활",432,270,9.5f);p.setColor(0xD05B4727);c.drawRoundRect(new RectF(405,286,555,320),10,10,p);text(c,"화면 중앙 터치",433,308,10.5f);}
  private void fittedText(Canvas c,String value,float x,float y,float width,float size){
    p.setTypeface(UiTheme.font(true));p.setTextSize(size);String shown=value;
    if(p.measureText(shown)>width){while(shown.length()>0&&p.measureText(shown+"…")>width)shown=shown.substring(0,shown.length()-1);shown+="…";}
    text(c,shown,x,y,size);
  }
  private void drawWrappedText(Canvas c,String s,float x,float y,float maxWidth,float size,float lineHeight){p.setTextSize(size);p.setColor(0xffeee4cf);String[] words=s.split(" ");String line="";float yy=y;for(String word:words){String test=line.length()==0?word:line+" "+word;if(p.measureText(test)>maxWidth&&line.length()>0){c.drawText(line,x,yy,p);yy+=lineHeight;line=word;}else line=test;}if(line.length()>0)c.drawText(line,x,yy,p);}

  static boolean blocksWorldTapForHud(float x,float y,boolean targetVisible){
    return blocksWorldTapForHud(x,y,targetVisible,0f);
  }
  static boolean blocksWorldTapForHud(float x,float y,boolean targetVisible,float d){
    float center=d*.5f;
    if(circleHit(x,y,questIconX(d),UTILITY_Y0,UTILITY_R+2)||inside(x,y,questIconX(d)-18,47,questIconX(d)+18,58))return true;
    if(inside(x,y,14,12,264,124)||inside(x,y,14,132,264,172)||inside(x,y,824+d,12,948+d,112)||inside(x,y,CHAT_LEFT+center,418,CHAT_RIGHT+center,522))return true;
    if(targetVisible&&inside(x,y,380+center,12,580+center,55))return true;
    if(circleHit(x,y,JOY_X,JOY_Y,JOY_R))return true;
    for(int i=0;i<4;i++){float cx=UTILITY_X0+i*UTILITY_STEP+d;if(circleHit(x,y,cx,UTILITY_Y0,UTILITY_R+2)||inside(x,y,cx-22,UTILITY_Y0+19,cx+22,UTILITY_Y0+30))return true;}
    for(int i=0;i<10;i++)if(slotRectStatic(i,d).contains(x,y))return true;
    return circleHit(x,y,ATK_X+d,ATK_Y,ATK_R+3)||circleHit(x,y,MODE_X+d,MODE_Y,MODE_R+3)||circleHit(x,y,AUTO_X+d,AUTO_Y,AUTO_R+3);
  }
  private static RectF slotRectStatic(int index,float d){int col=index%5,row=index/5;float l=SLOT_X0+d+col*(SLOT+SLOT_GAP),t=SLOT_Y0+row*(SLOT+SLOT_GAP);return new RectF(l,t,l+SLOT,t+SLOT);}
  private boolean isHudSurface(float x,float y){return blocksWorldTapForHud(x,y,combat.target()!=null,hudRightOffset);}
  private void autoNavigateQuest2(){worldAdapter.cancelForAction();interaction.cancelApproach();combat.cancelApproach();if(quest2.state()==GrowthQuest2.State.AVAILABLE||quest2.state()==GrowthQuest2.State.RETURN_READY){RuntimeState.Npc npc=findNpc("milles_guide_proto");if(npc!=null)worldAdapter.requestNpcApproach(npc.id);return;}if(quest2.state()==GrowthQuest2.State.ACTIVE){RuntimeState.Monster m=findMonster("combat_dummy_01");if(m==null)m=findMonster("combat_dummy_02");if(m==null)m=findMonster("combat_dummy_03");if(m!=null){combat.selectTarget(m);worldAdapter.requestMonsterApproach(m.id,48f);}else showFeedback("훈련 목표를 모두 처치했습니다 · 안내인에게 돌아가세요",FeedbackTone.INFO);}}
  private void requestGroundMove(float x,float y){cancelSkillApproach();WorldMoveTargetController.Snapshot move=activeWorld().requestGroundScreenTap(x,y);lastMoveRequestId=move.requestId;lastMoveStatus=WorldMoveTargetController.Status.IDLE;if(move.status==WorldMoveTargetController.Status.MOVING||move.status==WorldMoveTargetController.Status.REACHED){tapMarkerX=move.targetX;tapMarkerY=move.targetY;tapMarkerClock=.7f;showFeedback(move.replacedRequestId>0?"이동 목표 변경":"이동 시작",FeedbackTone.INFO);}consumeMoveOutcome(move);}

  public boolean onTouchEvent(MotionEvent e){float x=(e.getX()-ox)/scale,y=(e.getY()-oy)/scale;if(e.getActionMasked()==MotionEvent.ACTION_DOWN&&!questJournalOpen&&!inside(x,y,14,132,264,172)){questEntryPending=false;questExitPending=false;campaignAutoRoute=false;}if(questJournalOpen){QuestJournalWindow.Result r=questJournalWindow.motion(e.getActionMasked(),x,y,questJournalModel.rows(f5mQuest,quest2,inPoteField,state.rpg()),hudCenterOffset);if(r==QuestJournalWindow.Result.CLOSE)questJournalOpen=false;else if(r==QuestJournalWindow.Result.NAVIGATE){questJournalOpen=false;autoNavigateQuest();}return true;}if(e.getActionMasked()==MotionEvent.ACTION_DOWN&&circleHit(x,y,questIconX(hudRightOffset),UTILITY_Y0,UTILITY_R+2)){openQuestJournal();return true;}if(inPoteField&&townWindow.isOpen()){if(e.getActionMasked()==MotionEvent.ACTION_DOWN)townWindow.touch(x,y,hudCenterOffset,()->F5mSaveStore.writable()&&F5mSaveStore.checkpointActive());return true;}if(inReagentShop&&(innDialogueOpen||townWindow.isOpen()))return handleReagentShopTouch(e,x,y);if(skillWindow.open&&skillWindow.motion(e.getActionMasked(),x-hudCenterOffset,y))return true;if(inReagentShop&&e.getActionMasked()!=MotionEvent.ACTION_DOWN)return handleReagentShopTouch(e,x,y);switch(e.getActionMasked()){
    case MotionEvent.ACTION_DOWN:
      if(!state.player().alive&&skillWindow.open)skillWindow.close();
      if(skillWindow.open){if(circleHit(x,y,UTILITY_X0+UTILITY_STEP*3+hudRightOffset,UTILITY_Y0,UTILITY_R+4)){skillWindow.close();return true;}return skillWindow.touch(x-hudCenterOffset,y,skillActions);}
      if(statsOpen){float sx=x-hudCenterOffset;
        if(circleHit(x,y,UTILITY_X0+UTILITY_STEP+hudRightOffset,UTILITY_Y0,UTILITY_R+4)||circleHit(sx,y,911,91,18)){statsOpen=false;showFeedback("스탯창 닫힘",FeedbackTone.INFO);return true;}
        String st=inside(sx,y,700,132,754,158)?"STR":inside(sx,y,700,162,754,188)?"INT":inside(sx,y,700,192,754,218)?"WIS":inside(sx,y,700,222,754,248)?"CON":inside(sx,y,700,252,754,278)?"DEX":null;
        if(st!=null){if(state.rpg().spendStat(st)){state.applyDerivedGrowth();F5mSaveStore.saveRewardsActive(state.rpg());showFeedback(st+" +1 · 성장 반영",FeedbackTone.REWARD);}else showFeedback("STAT POINT가 부족합니다",FeedbackTone.WARN);return true;}
        if(!inside(sx,y,570,72,936,390)){statsOpen=false;showFeedback("스탯창 닫힘",FeedbackTone.INFO);}return true;
      }
      if(!state.player().alive){if(dist(x,y,480,270)<=180){state.revivePlayer();autoAttackEnabled=false;combat.clearTarget();interaction.cancel();activeWorld().cancelForAction();activeWorld().snapCameraToPlayer();camera=activeWorld().camera();action=Action.IDLE;actionClock=0f;playerFacing.endAttack();joy=false;vx=vy=0;knobX=JOY_X;knobY=JOY_Y;directStepClock=0f;checkpoint();}return true;}
      if(interaction.dialogOpen()){if(campaignDialogue()){handleCampaignDialogue(x,y);return true;}if(jobCounselorDialogue()){boolean ready=f5mQuest.state()==F5mAdaptedPrologueQuest.State.COMPLETED&&quest2.state()==GrowthQuest2.State.COMPLETED&&state.rpg().normalLevel()!=null&&state.rpg().normalLevel()>=3;if(ready&&"COMMONER".equals(state.rpg().currentJobCode())){int pick=-1;if(y>=395&&y<=425&&x>=218&&x<=737)pick=Math.min(2,(int)((x-218)/173));else if(y>=427&&y<=458&&x>=304&&x<=631)pick=3+Math.min(1,(int)((x-304)/173));if(pick>=0){String job=initialJobs()[pick];if(F5mSaveStore.transactActive(state.rpg(),skillBook,()->{if(!state.rpg().chooseInitialJob(job))return false;String starter=initialSkill(job);if(!skillBook.testAccess()&&starter!=null){skillBook.learn(starter,0);skillBook.assign(0,starter);if("CLERIC".equals(job)){skillBook.learn("SK_성직자_001",0);skillBook.assign(1,"SK_성직자_001");}}if(state.rpg().autoLootResolvedItem("IT_B_MP_POTION",5)!=RpgProgressionState.AutoLootResult.LOOTED||!state.rpg().grantB5SmallHpPotion(5))return false;return state.rpg().campaign().accept("M03",state.rpg(),skillBook);})){state.applyDerivedGrowth();interaction.dismissDialog();showReward(initialJobLabels()[pick]+" 전직 · 입문 장비·기술·보급 지급");}else showFeedback("직업 선택 또는 저장 조건을 확인하세요",FeedbackTone.WARN);return true;}}if(circleHit(x,y,744,321,18)||!inside(x,y,210,315,750,456)){interaction.dismissDialog();worldAdapter.cancelForAction();}return true;}if(poteTravelDialogue()&&inside(x,y,500,408,606,442)){interaction.dismissDialog();worldAdapter.cancelForAction();enterPoteField();showFeedback("포테의 숲으로 이동",FeedbackTone.INFO);return true;}if(supplyDialogue()&&y>=408&&y<=442){String item=null,name=null;long price=0;if(x>=218&&x<=330){item=RpgProgressionState.B_SMALL_POTION_ITEM_ID;name="소형 회복물약";price=20;}else if(x>=346&&x<=458){item=RpgProgressionState.SHOP_MOKDO_ITEM_ID;name="목도";price=120;}else if(x>=474&&x<=586){item=RpgProgressionState.SHOP_LEATHER_GLOVE_ITEM_ID;name="가죽장갑";price=80;}else if(x>=602&&x<=714){item=RpgProgressionState.SHOP_SHOES_ITEM_ID;name="신발";price=60;}if(item!=null){if(state.rpg().buyItem(item,price)){checkpoint();showReward(name+" 구매 · Gold -"+price);}else showFeedback("구매 불가 · Gold/아이템 상태 확인",FeedbackTone.WARN);return true;}}if(f5mGuideDialogue()&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.COMPLETED&&quest2.state()==GrowthQuest2.State.AVAILABLE&&inside(x,y,500,408,606,442)){if(quest2.accept()){showFeedback("퀘스트 수락 · 성장 훈련",FeedbackTone.INFO);interaction.dismissDialog();}return true;}if(f5mGuideDialogue()&&quest2.state()==GrowthQuest2.State.RETURN_READY&&inside(x,y,500,408,606,442)){if(quest2.turnIn(state.rpg())){state.applyDerivedGrowth();F5mSaveStore.saveRewardsActive(state.rpg());showReward("성장 훈련 완료 · EXP +15000 · Gold +250");interaction.dismissDialog();}return true;}if(f5mGuideDialogue()&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.RETURN_READY&&inside(x,y,500,408,606,442)){F5mTurnInCoordinator.Result tr=F5mQuestUiFlow.confirmTurnIn(f5mQuest,state.rpg());String msg=F5mQuestUiFlow.completionMessage(tr);if(tr==F5mTurnInCoordinator.Result.COMPLETED){state.applyDerivedGrowth();checkpoint();showReward(msg);interaction.dismissDialog();worldAdapter.cancelForAction();}else showFeedback(msg,tr==F5mTurnInCoordinator.Result.ALREADY_COMPLETED?FeedbackTone.INFO:FeedbackTone.WARN);return true;}if(f5mGuideDialogue()&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.AVAILABLE&&inside(x,y,500,408,606,442)){F5mAdaptedPrologueQuest.AcceptResult r=f5mQuest.accept();if(r==F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED){state.ensureAdaptedMillesMouse();interaction.dismissDialog();checkpoint();}showFeedback(r==F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED?"퀘스트 수락 · 첫 훈련":"퀘스트 상태 유지",FeedbackTone.INFO);return true;}if(f5mGuideDialogue()&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.AVAILABLE&&inside(x,y,620,408,726,442)){f5mQuest.decline();interaction.dismissDialog();worldAdapter.cancelForAction();showFeedback("퀘스트 거절 · 다시 대화할 수 있습니다",FeedbackTone.INFO);return true;}interaction.dismissDialog();worldAdapter.cancelForAction();showFeedback("대화 종료",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0+hudRightOffset,UTILITY_Y0,UTILITY_R+4)){inventoryOpen=!inventoryOpen;if(inventoryOpen){itemWindow.details=false;statsOpen=false;equipmentOpen=false;skillWindow.close();}showFeedback(inventoryOpen?"인벤토리 열림":"인벤토리 닫힘",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0+UTILITY_STEP+hudRightOffset,UTILITY_Y0,UTILITY_R+4)){statsOpen=!statsOpen;if(statsOpen){inventoryOpen=false;equipmentOpen=false;skillWindow.close();}showFeedback(statsOpen?"스탯창 열림":"스탯창 닫힘",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0+UTILITY_STEP*2+hudRightOffset,UTILITY_Y0,UTILITY_R+4)){equipmentOpen=!equipmentOpen;if(equipmentOpen){inventoryOpen=false;statsOpen=false;skillWindow.close();}showFeedback(equipmentOpen?"장비창 열림":"장비창 닫힘",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0+UTILITY_STEP*3+hudRightOffset,UTILITY_Y0,UTILITY_R+4)){skillWindow.open=true;inventoryOpen=statsOpen=equipmentOpen=false;return true;}
      if(equipmentOpen)return handleEquipmentTouch(x-hudCenterOffset,y);if(handleInventoryTouch(x-hudCenterOffset,y))return true;
      if(inside(x,y,CHAT_LEFT+hudCenterOffset,418,CHAT_RIGHT+hudCenterOffset,522)){if(!chatExpanded||inside(x,y,CHAT_RIGHT-42+hudCenterOffset,418,CHAT_RIGHT+hudCenterOffset,448)){chatExpanded=!chatExpanded;return true;}return true;}
      if(inside(x,y,14,132,264,172)){autoNavigateQuest();return true;}
      if(inReagentShop)return handleReagentShopTouch(e,x,y);
      if(circleHit(x,y,JOY_X,JOY_Y,JOY_R)){cancelSkillApproach();autoAttackEnabled=false;joy=true;directStepClock=0f;pressedControl="JOY";activeWorld().cancelForDirectInput();interaction.cancelApproach();combat.cancelApproach();stick(x,y);return true;}
      for(int i=0;i<10;i++)if(slotRect(i).contains(x,y)){pressedControl="SLOT"+i;if(i<8){SkillBook.Entry entry=skillBook.get(skillBook.slot(i));if(entry!=null)useBookSkill(entry);else showFeedback("스킬창에서 퀵슬롯을 등록하세요",FeedbackTone.INFO);}else {String potion=i==8?RpgProgressionState.B_SMALL_POTION_ITEM_ID:"IT_B_MP_POTION";if(F5mSaveStore.transactActive(state.rpg(),skillBook,()->state.rpg().useConsumable(potion,state)==RpgProgressionState.UseResult.USED))showFeedback(i==8?"회복약 사용":"마나약 사용",FeedbackTone.INFO);else showFeedback("물약이 없거나 회복할 필요가 없습니다",FeedbackTone.INFO);}return true;}
      if(circleHit(x,y,MODE_X+hudRightOffset,MODE_Y,MODE_R+3)){pressedControl="MODE";activeWorld().cancelForAction();showFeedback("기본 공격은 장착한 무기에 따라 결정됩니다",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,ATK_X+hudRightOffset,ATK_Y,ATK_R+4)){pressedControl="ATK";attack();return true;}
      if(circleHit(x,y,AUTO_X+hudRightOffset,AUTO_Y,AUTO_R+3)){cancelSkillApproach();autoAttackEnabled=!autoAttackEnabled;pressedControl="AUTO";autoTargetHintClock=0;if(autoAttackEnabled){RuntimeState.Monster best=bestReachableAutoTarget();if(best!=null){combat.selectTarget(best);showFeedback("자동 공격 시작 · "+best.name,FeedbackTone.INFO);}else showFeedback("자동 공격 대기 · 접근 가능한 몬스터가 없습니다",FeedbackTone.INFO);}else showFeedback("자동 공격 정지",FeedbackTone.INFO);return true;}
      if(isHudSurface(x,y))return true;
      WorldRuntimeAdapter active=activeWorld();WorldCameraTransform.Point wp=active.screenToWorld(x,y);RuntimeState.Npc npc=state.hitNpc(wp.x,wp.y,34f);if(npc!=null){cancelSkillApproach();combat.cancelApproach();interaction.cancelApproach();active.requestNpcApproach(npc.id);showFeedback("NPC 접근 · "+npc.name,FeedbackTone.INFO);return true;}RuntimeState.Monster monster=state.hitMonster(wp.x,wp.y,34f);if(monster!=null){cancelSkillApproach();active.cancelForAction();combat.selectTarget(monster);interaction.cancelApproach();showFeedback("타깃 선택 · "+monster.name,FeedbackTone.INFO);return true;}requestGroundMove(x,y);return true;
    case MotionEvent.ACTION_MOVE:if(joy)stick(x,y);return true;
    case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:joy=false;directStepClock=0f;pressedControl="";knobX=JOY_X;knobY=JOY_Y;vx=vy=0;return true;
  }return true;}
  private void stick(float x,float y){if(isActing()||!state.player().alive)return;float dx=x-JOY_X,dy=y-JOY_Y,len=(float)Math.sqrt(dx*dx+dy*dy);if(len>JOY_R){dx=dx/len*JOY_R;dy=dy/len*JOY_R;len=JOY_R;}knobX=JOY_X+dx;knobY=JOY_Y+dy;if(len<8){vx=vy=0;return;}float nx=dx/len,ny=dy/len;if(Math.abs(nx)>Math.abs(ny)){if(nx>0){vx=.707f;vy=.707f;}else{vx=-.707f;vy=-.707f;}}else{if(ny>0){vx=-.707f;vy=.707f;}else{vx=.707f;vy=-.707f;}}playerFacing.updateLocomotion(vx,vy);}
  private static boolean inside(float x,float y,float l,float t,float r,float b){return x>=l&&x<=r&&y>=t&&y<=b;}
  private static boolean circleHit(float x,float y,float cx,float cy,float r){float dx=x-cx,dy=y-cy;return dx*dx+dy*dy<=r*r;}
  private float dist(float a,float b,float c,float d){float x=a-c,y=b-d;return(float)Math.sqrt(x*x+y*y);}
}
