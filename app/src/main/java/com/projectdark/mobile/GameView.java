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
  private static final float UTILITY_X0=608f,UTILITY_Y0=28f,UTILITY_STEP=54f,UTILITY_R=16f;

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
  private final SkillVfxRenderer skillVfx;
  private String activeSkillVisualId,characterBodyIdentity="mm001";
  private final SkillApproachController skillApproach=new SkillApproachController();
  private final SkillVfxRenderer.Anchors skillAnchors=new SkillVfxRenderer.Anchors(){
    public float x(String id){if(RuntimeCombatSession.PLAYER_ID.equals(id))return renderedPlayerWorldX();for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id))return m.x;return Float.NaN;}
    public float y(String id){if(RuntimeCombatSession.PLAYER_ID.equals(id))return renderedPlayerWorldY();for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id))return m.y;return Float.NaN;}
    public float centerY(String id){if(RuntimeCombatSession.PLAYER_ID.equals(id))return y(id)-23f;for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id)){if(inPoteField&&PoteForestMonsterShowcase.containsMonster(m.id))return m.y-("POTE_LYCAN".equals(m.id)?33f:21f);return m.y-10f*WorldEntityPresentationRenderer.MONSTER_RENDER_SCALE;}return Float.NaN;}
  };
  private final WorldEntityPresentationRenderer worldEntityRenderer=new WorldEntityPresentationRenderer();
  private final RpgInventoryPresentation rpgPresentation=new RpgInventoryPresentation();
  private final RpgInteractionController rpgInteraction=new RpgInteractionController();
  private final ClassicHudIconAtlas hudIcons=new ClassicHudIconAtlas();
  private final HudSpriteCatalog hudSprites;
  private final EquipmentVisualRegistry inventoryVisuals;
  private final ReagentItemVisualRegistry reagentVisuals;
  private final ReagentShopInteriorRenderer reagentShopRenderer;
  private WorldRuntimeAdapter reagentShopAdapter;
  private boolean inReagentShop=false,reagentShopOpen=false;
  private float millesReturnX,millesReturnY;
  private TownInteriorDef townInterior;
  private final TownInteriorRenderer townRenderer;
  private final TownNpcRenderer townNpcRenderer=new TownNpcRenderer();
  private final TownShopWindow townWindow;
  private boolean merchantApproach;
  private final F5mAdaptedPrologueQuest f5mQuest=F5mAdaptedPrologueQuest.openingFixture();
  private final GrowthQuest2 quest2=new GrowthQuest2();

  private float scale=1,ox,oy,vx,vy,hudRightOffset,hudCenterOffset,logicalViewportWidth=W;
  private float knobX=JOY_X,knobY=JOY_Y;
  private int autoSkillCursor;
  private boolean joy,running,inventoryOpen,statsOpen,equipmentOpen,autoAttackEnabled,questCollapsed=true,questJournalOpen,chatExpanded,innDialogueOpen,innMaryApproach;
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

  public GameView(Context c){super(c);itemWindow=new ItemWindow(c);combatSession.setSkillVisibility(this::skillTargetVisible);skillPresentation=new SkillPresentationCatalog(c);skillBodyRenderer=new SkillBodyRenderer(c,skillPresentation);skillVfx=new SkillVfxRenderer(c,skillPresentation);characterBodyIdentity=c.getSharedPreferences("project_dark_visual_v1",0).getString("body_identity","mm001");skillBook=SkillBook.load(c);combatSession.setBasicHits(skillBook::basicHits);combatSession.setSkillProficiency(id->skillBook.testAccess()?100:skillBook.proficiency(id));
    combatSession.setSkillMovement(new SkillAbilityExecutor.Movement(){
      public boolean movePlayer(float x,float y){int steps=SkillActionContract.distance(state.player().x,state.player().y,x,y);if(steps==Integer.MAX_VALUE||steps>6||state.skillEffects().rooted("player"))return false;for(int i=1;i<=steps;i++){float q=i/(float)steps;if(!activeWorld().canPlayerOccupy(state.player().x+(x-state.player().x)*q,state.player().y+(y-state.player().y)*q))return false;}if(!activeWorld().canPlayerOccupy(x,y))return false;activeWorld().cancelForAction();state.player().x=x;state.player().y=y;activeWorld().snapCameraToPlayer();return true;}
      public boolean moveMonster(RuntimeState.Monster m,float x,float y){return state.tryMoveMonster(m,x-m.x,y-m.y,(float)Math.hypot(x-m.x,y-m.y));}
    });skillBook.bindJob(()->state.rpg().currentJobCode());skillWindow=new SkillWindow(skillBook,new SkillIconCatalog(c));F5mSaveStore.restoreAndBindSkillsActive(skillBook);hudSprites=new HudSpriteCatalog(c);inventoryVisuals=new EquipmentVisualRegistry(c);reagentVisuals=new ReagentItemVisualRegistry(c);reagentShopRenderer=new ReagentShopInteriorRenderer(c);townRenderer=new TownInteriorRenderer(c);townWindow=new TownShopWindow(c);setKeepScreenOn(true);F5mSaveStore.restoreQuestActive(f5mQuest);F5mSaveStore.restoreRewardsActive(state.rpg());F5mSaveStore.restoreQuest2Active(quest2);quest2.unlockIfPrologueCompleted(f5mQuest);float[] returnPoint=F5mSaveStore.savedFieldReturnPointActive();fieldReturnX=returnPoint[0];fieldReturnY=returnPoint[1];if(com.projectdark.mobile.world.PoteFieldDef.MAP_ID.equals(F5mSaveStore.savedMapIdActive())){state.enterPoteField();inPoteField=true;}F5mSaveStore.restoreRuntimeActive(state);worldAdapter.snapCameraToPlayer();if(inPoteField){poteFieldAdapter=new WorldRuntimeAdapter(state,logicalViewportWidth,H,PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.navigationTiles(),PoteFieldDef.obstacles(),true);poteFieldAdapter.snapCameraToPlayer();camera=poteFieldAdapter.camera();}F5mSaveStore.bindRuntime(state,f5mQuest,quest2);restoreTownInterior();savedLedgerSequence=state.ledger().sequence();if(!F5mSaveStore.writable())showFeedback("저장 데이터를 읽지 못했습니다 · 원본 보존 중",FeedbackTone.WARN);}
  void setSkillTestMode(boolean enabled){
    cancelSkillApproach();skillVfx.clear();activeSkillVisualId=null;skillBook.setTestAccess(enabled);combatSession.setSkillTestMode(enabled);
    getContext().getSharedPreferences("project_dark_skill_test_v1",0).edit().putBoolean("enabled",enabled).apply();
    if(enabled){String raw=getContext().getSharedPreferences("project_dark_skill_test_v1",0).getString("slots",null);String[] ids={"SK_마법사_001","SK_마법사_003","SK_마법사_004","SK_마법사_005","SK_성직자_005","SK_무도가_002","SK_무도가_007","SK_마법사_043"};if(raw!=null)try{org.json.JSONArray a=new org.json.JSONArray(raw);if(a.length()==8)for(int i=0;i<8;i++)ids[i]=a.isNull(i)?null:a.getString(i);}catch(Exception ignored){}skillBook.restoreTestSlots(ids);}
    showFeedback(enabled?"스킬 테스트 ON · "+skillBook.catalogSize()+"개 시험 목록 · MP 소모 없음":"스킬 테스트 OFF · 직업/습득 조건 적용",FeedbackTone.INFO);
  }
  private boolean saveSkillSlots(){if(!skillBook.testAccess())return F5mSaveStore.writable()&&F5mSaveStore.checkpointActive();org.json.JSONArray a=new org.json.JSONArray();for(String id:skillBook.testSlots())a.put(id==null?org.json.JSONObject.NULL:id);return getContext().getSharedPreferences("project_dark_skill_test_v1",0).edit().putString("slots",a.toString()).commit();}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(loop);}
  public void pause(){cancelSkillApproach();running=false;removeCallbacks(loop);F5mSaveStore.beginFrame();try{state.tick(0f);state.applyDerivedGrowth();consumeLedger();}finally{F5mSaveStore.endFrame();}checkpoint();}
  private void checkpoint(){
    savedLedgerSequence=state.ledger().sequence();
    if(F5mSaveStore.checkpointActive()){saveWarningShown=false;}
    else if(!saveWarningShown){showFeedback("저장하지 못했습니다 · 이전 저장 확인 필요",FeedbackTone.WARN);saveWarningShown=true;}
    checkpointClock=0f;
  }

  private void update(float dt){
    if(inReagentShop){updateReagentShop(dt);return;}
    if(inPoteField){updatePoteField(dt);return;}
    feedbackClock=Math.max(0,feedbackClock-dt);rewardClock=Math.max(0,rewardClock-dt);tapMarkerClock=Math.max(0,tapMarkerClock-dt);autoTargetHintClock=Math.max(0,autoTargetHintClock-dt);
    combat.tick(dt);if(autoAttackEnabled&&combat.target()==null&&isMonsterApproach(moveTarget.snapshot()))worldAdapter.cancel();combat.setBasicAttack(equipmentActions.resolveBasicAttack(state.rpg()).animationAction);tickSkillCombat(dt);state.tick(dt);state.applyDerivedGrowth();quest2.unlockIfPrologueCompleted(f5mQuest);consumeLedger();consumeRewardNotice();monsterAi.tick(state,dt);
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
  private void autoNavigateQuest(){
    F5mAdaptedPrologueQuest.State qs=f5mQuest.state();
    boolean growth=ActiveQuestTracker.current(f5mQuest,quest2)==ActiveQuestTracker.Quest.GROWTH_2;
    if(inReagentShop&&townInterior!=null&&townInterior.kind==TownInteriorDef.Kind.INN){
      if(!growth&&qs==F5mAdaptedPrologueQuest.State.RETURN_READY){
        reagentShopAdapter.requestGroundWorld(townInterior.customerX(),townInterior.customerY());innMaryApproach=true;innDialogueOpen=false;
      }else{leaveReagentShop();autoNavigateQuest();}
      return;
    }
    if(growth){autoNavigateQuest2();return;}
    worldAdapter.cancelForAction();interaction.cancelApproach();combat.cancelApproach();
    if(qs==F5mAdaptedPrologueQuest.State.AVAILABLE){RuntimeState.Npc npc=findNpc("milles_guide_proto");if(npc!=null){worldAdapter.requestNpcApproach(npc.id);showFeedback("제임스에게 이동",FeedbackTone.INFO);}return;}
    if(qs==F5mAdaptedPrologueQuest.State.RETURN_READY){worldAdapter.requestGroundWorld(2016f,736f);showFeedback("밀레스 여관의 메리에게 보고",FeedbackTone.INFO);return;}
    if(qs==F5mAdaptedPrologueQuest.State.ACTIVE){RuntimeState.Monster m=findMonster(f5mQuest.objectiveMonsterId());if(m!=null){combat.selectTarget(m);worldAdapter.requestMonsterApproach(m.id,48f);showFeedback("퀘스트 목표 · "+m.name,FeedbackTone.INFO);}else showFeedback("퀘스트 목표를 찾을 수 없습니다",FeedbackTone.WARN);return;}
    if(qs==F5mAdaptedPrologueQuest.State.COMPLETED){RuntimeState.Npc npc=findNpc("milles_gate_proto");if(npc!=null)worldAdapter.requestNpcApproach(npc.id);}
  }
  private void requestGroundMove(float x,float y){cancelSkillApproach();WorldMoveTargetController.Snapshot move=activeWorld().requestGroundScreenTap(x,y);lastMoveRequestId=move.requestId;lastMoveStatus=WorldMoveTargetController.Status.IDLE;if(move.status==WorldMoveTargetController.Status.MOVING||move.status==WorldMoveTargetController.Status.REACHED){tapMarkerX=move.targetX;tapMarkerY=move.targetY;tapMarkerClock=.7f;showFeedback(move.replacedRequestId>0?"이동 목표 변경":"이동 시작",FeedbackTone.INFO);}consumeMoveOutcome(move);}

  public boolean onTouchEvent(MotionEvent e){float x=(e.getX()-ox)/scale,y=(e.getY()-oy)/scale;if(questJournalOpen){if(e.getActionMasked()==MotionEvent.ACTION_DOWN){if(circleHit(x,y,686,112,20)){questJournalOpen=false;return true;}if(inside(x,y,492,386,666,419)){questJournalOpen=false;autoNavigateQuest();return true;}}return true;}if(e.getActionMasked()==MotionEvent.ACTION_DOWN&&circleHit(x,y,294,28,18)){questJournalOpen=true;innDialogueOpen=false;return true;}if(inReagentShop)return handleReagentShopTouch(e,x,y);if(skillWindow.open&&skillWindow.motion(e.getActionMasked(),x,y))return true;switch(e.getActionMasked()){
    case MotionEvent.ACTION_DOWN:
      if(!state.player().alive&&skillWindow.open)skillWindow.close();
      if(questJournalOpen){if(circleHit(x,y,686,112,20)){questJournalOpen=false;return true;}if(inside(x,y,492,386,666,419)){questJournalOpen=false;autoNavigateQuest();return true;}return true;}
      if(skillWindow.open){if(circleHit(x,y,UTILITY_X0+UTILITY_STEP*3+hudRightOffset,UTILITY_Y0,UTILITY_R+4)){skillWindow.close();return true;}return skillWindow.touch(x,y,skillActions);}
      if(statsOpen){
        if(circleHit(x,y,UTILITY_X0+UTILITY_STEP+hudRightOffset,UTILITY_Y0,UTILITY_R+4)||circleHit(x,y,911,91,18)){statsOpen=false;showFeedback("스탯창 닫힘",FeedbackTone.INFO);return true;}
        String st=inside(x,y,700,132,754,158)?"STR":inside(x,y,700,162,754,188)?"INT":inside(x,y,700,192,754,218)?"WIS":inside(x,y,700,222,754,248)?"CON":inside(x,y,700,252,754,278)?"DEX":null;
        if(st!=null){if(state.rpg().spendStat(st)){state.applyDerivedGrowth();F5mSaveStore.saveRewardsActive(state.rpg());showFeedback(st+" +1 · 성장 반영",FeedbackTone.REWARD);}else showFeedback("STAT POINT가 부족합니다",FeedbackTone.WARN);return true;}
        if(!inside(x,y,570,72,936,390)){statsOpen=false;showFeedback("스탯창 닫힘",FeedbackTone.INFO);}return true;
      }
      if(!state.player().alive){if(dist(x,y,480,270)<=180){state.revivePlayer();autoAttackEnabled=false;combat.clearTarget();interaction.cancel();activeWorld().cancelForAction();activeWorld().snapCameraToPlayer();camera=activeWorld().camera();action=Action.IDLE;actionClock=0f;playerFacing.endAttack();joy=false;vx=vy=0;knobX=JOY_X;knobY=JOY_Y;directStepClock=0f;checkpoint();}return true;}
      if(interaction.dialogOpen()){if(poteTravelDialogue()&&inside(x,y,500,408,606,442)){interaction.dismissDialog();worldAdapter.cancelForAction();enterPoteField();showFeedback("포테의 숲으로 이동",FeedbackTone.INFO);return true;}if(supplyDialogue()&&y>=408&&y<=442){String item=null,name=null;long price=0;if(x>=218&&x<=330){item=RpgProgressionState.B_SMALL_POTION_ITEM_ID;name="소형 회복물약";price=20;}else if(x>=346&&x<=458){item=RpgProgressionState.SHOP_MOKDO_ITEM_ID;name="목도";price=120;}else if(x>=474&&x<=586){item=RpgProgressionState.SHOP_LEATHER_GLOVE_ITEM_ID;name="가죽장갑";price=80;}else if(x>=602&&x<=714){item=RpgProgressionState.SHOP_SHOES_ITEM_ID;name="신발";price=60;}if(item!=null){if(state.rpg().buyItem(item,price)){checkpoint();showReward(name+" 구매 · Gold -"+price);}else showFeedback("구매 불가 · Gold/아이템 상태 확인",FeedbackTone.WARN);return true;}}if(growthQuestDialogue()&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.COMPLETED&&quest2.state()==GrowthQuest2.State.AVAILABLE&&inside(x,y,500,408,606,442)){if(quest2.accept()){showFeedback("퀘스트 수락 · 성장 훈련",FeedbackTone.INFO);interaction.dismissDialog();}return true;}if(growthQuestDialogue()&&quest2.state()==GrowthQuest2.State.RETURN_READY&&inside(x,y,500,408,606,442)){if(quest2.turnIn(state.rpg())){state.applyDerivedGrowth();F5mSaveStore.saveRewardsActive(state.rpg());showReward("성장 훈련 완료 · EXP +15000 · Gold +250");interaction.dismissDialog();}return true;}if(f5mGuideDialogue()&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.AVAILABLE&&inside(x,y,500,408,606,442)){F5mAdaptedPrologueQuest.AcceptResult r=f5mQuest.accept();if(r==F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED){interaction.dismissDialog();checkpoint();}showFeedback(r==F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED?"퀘스트 수락 · 첫 훈련":"퀘스트 상태 유지",FeedbackTone.INFO);return true;}if(f5mGuideDialogue()&&f5mQuest.state()==F5mAdaptedPrologueQuest.State.AVAILABLE&&inside(x,y,620,408,726,442)){f5mQuest.decline();interaction.dismissDialog();worldAdapter.cancelForAction();showFeedback("퀘스트 거절 · 다시 대화할 수 있습니다",FeedbackTone.INFO);return true;}interaction.dismissDialog();worldAdapter.cancelForAction();showFeedback("대화 종료",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0+hudRightOffset,UTILITY_Y0,UTILITY_R+4)){inventoryOpen=!inventoryOpen;if(inventoryOpen){itemWindow.details=false;statsOpen=false;equipmentOpen=false;skillWindow.close();}showFeedback(inventoryOpen?"인벤토리 열림":"인벤토리 닫힘",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0+UTILITY_STEP+hudRightOffset,UTILITY_Y0,UTILITY_R+4)){statsOpen=!statsOpen;if(statsOpen){inventoryOpen=false;equipmentOpen=false;skillWindow.close();}showFeedback(statsOpen?"스탯창 열림":"스탯창 닫힘",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0+UTILITY_STEP*2+hudRightOffset,UTILITY_Y0,UTILITY_R+4)){equipmentOpen=!equipmentOpen;if(equipmentOpen){inventoryOpen=false;statsOpen=false;skillWindow.close();}showFeedback(equipmentOpen?"장비창 열림":"장비창 닫힘",FeedbackTone.INFO);return true;}
      if(circleHit(x,y,UTILITY_X0+UTILITY_STEP*3+hudRightOffset,UTILITY_Y0,UTILITY_R+4)){skillWindow.open=true;inventoryOpen=statsOpen=equipmentOpen=false;questJournalOpen=false;return true;}
      if(circleHit(x,y,UTILITY_X0+UTILITY_STEP*4+hudRightOffset,UTILITY_Y0,UTILITY_R+4)){questJournalOpen=!questJournalOpen;inventoryOpen=statsOpen=equipmentOpen=false;skillWindow.close();return true;}
      if(equipmentOpen)return handleEquipmentTouch(x,y);if(handleInventoryTouch(x,y))return true;
      if(inside(x,y,CHAT_LEFT+hudCenterOffset,418,CHAT_RIGHT+hudCenterOffset,522)){if(!chatExpanded||inside(x,y,CHAT_RIGHT-42+hudCenterOffset,418,CHAT_RIGHT+hudCenterOffset,448)){chatExpanded=!chatExpanded;return true;}return true;}
      if(inside(x,y,14,132,264,questCollapsed?172:236)){questJournalOpen=true;return true;}
      if(circleHit(x,y,JOY_X,JOY_Y,JOY_R)){cancelSkillApproach();autoAttackEnabled=false;joy=true;directStepClock=0f;pressedControl="JOY";activeWorld().cancelForDirectInput();interaction.cancelApproach();combat.cancelApproach();stick(x,y);return true;}
      for(int i=0;i<10;i++)if(slotRect(i).contains(x,y)){pressedControl="SLOT"+i;if(i<8){SkillBook.Entry entry=skillBook.get(skillBook.slot(i));if(entry!=null)useBookSkill(entry);else showFeedback("스킬창에서 퀵슬롯을 등록하세요",FeedbackTone.INFO);}else showFeedback("포션 슬롯 · 내용 확정 후 연결됩니다",FeedbackTone.INFO);return true;}
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
