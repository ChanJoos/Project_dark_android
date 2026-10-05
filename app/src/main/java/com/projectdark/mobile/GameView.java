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
    });skillBook.bindJob(()->state.rpg().currentJobCode());skillWindow=new SkillWindow(skillBook,new SkillIconCatalog(c));F5mSaveStore.restoreAndBindSkillsActive(skillBook);hudSprites=new HudSpriteCatalog(c);inventoryVisuals=new EquipmentVisualRegistry(c);reagentVisuals=new ReagentItemVisualRegistry(c);reagentShopRenderer=new ReagentShopInteriorRenderer(c);townRenderer=new TownInteriorRenderer(c);townWindow=new TownShopWindow(c);setKeepScreenOn(true);F5mSaveStore.restoreQuestActive(f5mQuest);F5mSaveStore.restoreRewardsActive(state.rpg());F5mSaveStore.restoreQuest2Active(quest2);quest2.unlockIfPrologueCompleted(f5mQuest);state.rpg().campaign().syncOpening(f5mQuest.state()==F5mAdaptedPrologueQuest.State.COMPLETED,quest2.state()==GrowthQuest2.State.COMPLETED);float[] returnPoint=F5mSaveStore.savedFieldReturnPointActive();fieldReturnX=returnPoint[0];fieldReturnY=returnPoint[1];if(com.projectdark.mobile.world.CampaignWorld.contains(F5mSaveStore.savedMapIdActive())){state.enterCampaignMap(F5mSaveStore.savedMapIdActive(),false);inPoteField=true;}F5mSaveStore.restoreRuntimeActive(state);worldAdapter.snapCameraToPlayer();if(inPoteField){poteFieldAdapter=new WorldRuntimeAdapter(state,logicalViewportWidth,H,PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.navigationTiles(),PoteFieldDef.obstacles(),true);poteFieldAdapter.snapCameraToPlayer();camera=poteFieldAdapter.camera();}F5mSaveStore.bindRuntime(state,f5mQuest,quest2);restoreTownInterior();if(inPoteField)questJournalModel.visitForest(f5mQuest,quest2);savedLedgerSequence=state.ledger().sequence();if(!F5mSaveStore.writable())showFeedback("ì €ì¥ ë°ì´í„°ë¥¼ ì½ì§€ ëª»í–ˆìŠµë‹ˆë‹¤ Â· ì›ë³¸ ë³´ì¡´ ì¤‘",FeedbackTone.WARN);}
  void setSkillTestMode(boolean enabled){
    cancelSkillApproach();skillVfx.clear();skillVfx.testAccess=enabled;activeSkillVisualId=null;skillBook.setTestAccess(enabled);combatSession.setSkillTestMode(enabled);
    getContext().getSharedPreferences("project_dark_skill_test_v1",0).edit().putBoolean("enabled",enabled).apply();
    if(enabled){String raw=getContext().getSharedPreferences("project_dark_skill_test_v1",0).getString("slots",null);String[] ids={"SK_ë§ˆë²•ì‚¬_001","SK_ë§ˆë²•ì‚¬_003","SK_ë§ˆë²•ì‚¬_004","SK_ë§ˆë²•ì‚¬_005","SK_ì„±ì§ì_005","SK_ë¬´ë„ê°€_002","SK_ë¬´ë„ê°€_007","SK_ë§ˆë²•ì‚¬_043"};if(raw!=null)try{org.json.JSONArray a=new org.json.JSONArray(raw);if(a.length()==8)for(int i=0;i<8;i++)ids[i]=a.isNull(i)?null:a.getString(i);}catch(Exception ignored){}skillBook.restoreTestSlots(ids);}
    showFeedback(enabled?"ìŠ¤í‚¬ í…ŒìŠ¤íŠ¸ ON Â· "+skillBook.catalogSize()+"ê°œ ì‹œí—˜ ëª©ë¡ Â· MP ì†Œëª¨ ì—†ìŒ":"ìŠ¤í‚¬ í…ŒìŠ¤íŠ¸ OFF Â· ì§ì—…/ìŠµë“ ì¡°ê±´ ì ìš©",FeedbackTone.INFO);
  }
  private boolean saveSkillSlots(){if(!skillBook.testAccess())return F5mSaveStore.writable()&&F5mSaveStore.checkpointActive();org.json.JSONArray a=new org.json.JSONArray();for(String id:skillBook.testSlots())a.put(id==null?org.json.JSONObject.NULL:id);return getContext().getSharedPreferences("project_dark_skill_test_v1",0).edit().putString("slots",a.toString()).commit();}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(loop);}
  public void pause(){cancelSkillApproach();running=false;removeCallbacks(loop);F5mSaveStore.beginFrame();try{state.tick(0f);state.applyDerivedGrowth();consumeLedger();}finally{F5mSaveStore.endFrame();}checkpoint();}
  private boolean checkpoint(){
    savedLedgerSequence=state.ledger().sequence();
    state.rpg().campaign().visit(state.currentMapId());boolean saved=F5mSaveStore.checkpointActive();
    if(saved){saveWarningShown=false;}
    else if(!saveWarningShown){showFeedback("ì €ì¥í•˜ì§€ ëª»í–ˆìŠµë‹ˆë‹¤ Â· ì´ì „ ì €ì¥ í™•ì¸ í•„ìš”",FeedbackTone.WARN);saveWarningShown=true;}
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
      else if(move.kind==WorldMoveTargetController.RequestKind.GROUND)showFeedback("ì´ë™ ì™„ë£Œ",FeedbackTone.INFO);
    }else if(move.status==WorldMoveTargetController.Status.BLOCKED){
      if(move.kind==WorldMoveTargetController.RequestKind.MONSTER_APPROACH)combat.cancelApproach();
      showFeedback("ì´ë™í•  ìˆ˜ ì—†ëŠ” ìœ„ì¹˜ì…ë‹ˆë‹¤",FeedbackTone.WARN);
    }
  }
  private void consumeLedger(){List<CombatLedger.Event> events=state.ledger().snapshot();for(CombatLedger.Event e:events){if(e.sequence<=lastLedgerSequence)continue;lastLedgerSequence=e.sequence;F5mAdaptedPrologueQuest.DefeatResult qr=f5mQuest.consume(e);if(qr==F5mAdaptedPrologueQuest.DefeatResult.RETURN_READY)showReward("í€˜ìŠ¤íŠ¸ ëª©í‘œ ì™„ë£Œ Â· ì—¬ê´€ì˜ ë²¤ì €ë¯¼ì—ê²Œ ë³´ê³ í•˜ì„¸ìš”");if(quest2.consume(e)&&quest2.state()==GrowthQuest2.State.RETURN_READY)showReward("ì„±ì¥ í›ˆë ¨ ì™„ë£Œ Â· ì•ˆë‚´ì¸ì—ê²Œ ëŒì•„ê°€ì„¸ìš”");switch(e.type){case MONSTER_DEFEATED:showFeedback("ëª¬ìŠ¤í„° ê²©íŒŒ",FeedbackTone.INFO);break;case PLAYER_HIT:showFeedback("-"+e.amount+" HP",FeedbackTone.WARN);break;case PLAYER_DEFEATED:showFeedback("í–‰ë™ ë¶ˆëŠ¥",FeedbackTone.WARN);break;case PLAYER_REVIVED:showFeedback("ë¶€í™œ",FeedbackTone.INFO);break;default:break;}}}
  private void consumeRewardNotice(){
    long exp=0,gold=0,last=lastRewardSequence;java.util.Map<String,Integer> items=new java.util.LinkedHashMap<>();boolean unresolved=false,full=false;
    for(RpgProgressionState.RewardResolution r:state.rpg().rewardHistory())if(r.combatSequence>lastRewardSequence){last=Math.max(last,r.combatSequence);unresolved|=r.status!=RpgProgressionState.RewardStatus.RESOLVED;exp+=r.exp==null?0:r.exp;gold+=r.gold;for(Map.Entry<String,Integer> e:r.autoLootedItems.entrySet())items.put(e.getKey(),items.getOrDefault(e.getKey(),0)+e.getValue());full|=r.itemOutcomes.containsValue(RpgProgressionState.AutoLootResult.INVENTORY_FULL);}
    if(last==lastRewardSequence)return;lastRewardSequence=last;F5mSaveStore.saveRewardsActive(state.rpg());String message="EXP +"+exp+" Â· Gold +"+gold;
    for(Map.Entry<String,Integer> e:items.entrySet())message+=" Â· "+itemDisplayName(e.getKey())+" x"+e.getValue();
    if(unresolved)message+=" Â· ë¯¸í™•ì¸ ë³´ìƒ";if(full)message+=" Â· ê°€ë°© ìˆ˜ëŸ‰ í•œë„";showReward(message);
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
    if(inReagentShop&&!(townInterior.kind==TownInteriorDef.Kind.INN&&(qs==F5mAdaptedPrologueQuest.State.ACTIVE||qs==F5mAdaptedPrologueQuest.State.RETURN_READY))){merchantApproach=false;innDialogueOpen=false;townWindow.close();questExitPending=true;reagentShopAdapter.requestGroundWorld(townInterior.exitX(),townInterior.exitY());showFeedback("ê±´ë¬¼ì„ ë‚˜ê°€ í€˜ìŠ¤íŠ¸ ëª©í‘œë¡œ ì´ë™í•©ë‹ˆë‹¤",FeedbackTone.INFO);return;}
    QuestJournalModel.Row route=questJournalModel.current(f5mQuest,quest2,inPoteField,state.rpg());
    if(route!=null&&QuestJournalModel.JOB_CHOICE.equals(route.id)){RuntimeState.Npc counselor=findNpc("milles_job_counselor");if(counselor!=null){worldAdapter.requestNpcApproach(counselor.id);showFeedback("ë§ˆì´í´ì—ê²Œ ì§ì—… ìƒë‹´ ë°›ê¸°",FeedbackTone.INFO);}return;}
    if(qs==F5mAdaptedPrologueQuest.State.COMPLETED){
      if(inReagentShop){merchantApproach=false;innDialogueOpen=false;questExitPending=true;reagentShopAdapter.requestGroundWorld(townInterior.exitX(),townInterior.exitY());showFeedback("ì—¬ê´€ì„ ë‚˜ê°€ ë‹¤ìŒ ì˜ë¢°ë¡œ ì´ë™í•©ë‹ˆë‹¤",FeedbackTone.INFO);return;}
      if(ActiveQuestTracker.current(f5mQuest,quest2)==ActiveQuestTracker.Quest.GROWTH_2){autoNavigateQuest2();return;}
      RuntimeState.Npc next=findNpc("milles_gate_proto");if(next!=null){worldAdapter.requestNpcApproach(next.id);showFeedback("ìœŒë¦¬ì—„ì—ê²Œ ìˆ²ê¸¸ ì•ˆë‚´ ë°›ê¸°",FeedbackTone.INFO);}return;
    }
    if(inReagentShop&&townInterior.kind==TownInteriorDef.Kind.INN){if(qs==F5mAdaptedPrologueQuest.State.ACTIVE){RuntimeState.Monster m=findMonster(f5mQuest.objectiveMonsterId());if(m!=null){combat.selectTarget(m);reagentShopAdapter.requestMonsterApproach(m.id,CanonicalMeleeTileContract.REACH_DISTANCE);}}else{merchantApproach=true;reagentShopAdapter.requestGroundWorld(townInterior.customerX(),townInterior.customerY());}return;}
    if(qs==F5mAdaptedPrologueQuest.State.AVAILABLE){RuntimeState.Npc npc=findNpc("milles_guide_proto");if(npc!=null){worldAdapter.requestNpcApproach(npc.id);showFeedback("ì œì„ìŠ¤ì—ê²Œ ì´ë™",FeedbackTone.INFO);}return;}
    if(qs==F5mAdaptedPrologueQuest.State.ACTIVE||qs==F5mAdaptedPrologueQuest.State.RETURN_READY){questEntryPending=true;worldAdapter.requestGroundWorld(2016f,736f);showFeedback("ë°€ë ˆìŠ¤ ì—¬ê´€ìœ¼ë¡œ ì´ë™",FeedbackTone.INFO);}
  }

  private void executeReadyCombatIntent(){CombatController.Intent ready=combat.consumeReadyIntent();action=Action.IDLE;if(ready==CombatController.Intent.ATTACK)attack();else if(ready==CombatController.Intent.CAST)cast();else if(ready==CombatController.Intent.SKILL)skill();else if(ready==CombatController.Intent.KICK)kick();}
  private void beginCombatApproach(CombatController.Intent intent){WorldRuntimeAdapter active=activeWorld();active.cancelForAction();if(combat.beginApproach(intent)){RuntimeState.Monster target=combat.approachTarget();if(target!=null)active.requestMonsterApproach(target.id,combat.intentRange());showFeedback("íƒ€ê¹ƒìœ¼ë¡œ ì´ë™",FeedbackTone.INFO);}}

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
  private boolean requireTarget(){if(combat.hasUsableTarget())return true;showFeedback("ë¨¼ì € ëª¬ìŠ¤í„°ë¥¼ ì„ íƒí•˜ì„¸ìš”",FeedbackTone.WARN);return false;}
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
    for(CombatResolver.Event e:result.events)if(e.type==CombatResolver.EventType.EFFECT_APPLIED&&RuntimeCombatSession.PLAYER_ID.equals(e.actorId)&&practiced.add(e.actionSequence)){skillBook.practiced(e.actionId.startsWith("attack_proto_")?"SK_ê³µí†µ_001":e.actionId);state.rpg().campaign().validSkill(e.actionId,e.actionSequence,e.amount,skillBook.testAccess(),state.rpg());}
  }
  private void consumeSkillNotice(){String notice=combatSession.takeSkillNotice();if(notice.isEmpty())return;
    if(notice.equals("TRAVEL")){if(inPoteField){leavePoteField();}state.player().x=WorldDef.PLAYER_SPAWN_X;state.player().y=WorldDef.PLAYER_SPAWN_Y;activeWorld().snapCameraToPlayer();checkpoint();showFeedback("ë°€ë ˆìŠ¤ ê·€í™˜",FeedbackTone.INFO);}
    else if(notice.equals("DOOR")){RuntimeState.Npc closest=null;for(RuntimeState.Npc n:state.npcs())if(closest==null||state.distanceTo(n)<state.distanceTo(closest))closest=n;if(closest!=null&&state.distanceTo(closest)<80){interaction.request(state,closest);showFeedback("ì¶œì… ëŒ€ìƒ ì„ íƒ",FeedbackTone.INFO);}else showFeedback("ì—´ ìˆ˜ ìˆëŠ” ë¬¸ì´ ê°€ê¹Œì´ ì—†ìŠµë‹ˆë‹¤",FeedbackTone.WARN);}
    else showFeedback(notice,FeedbackTone.INFO);
  }
  private boolean isSkillLearned(String id){return skillBook!=null&&skillBook.usable(id);}
  private final SkillWindow.Actions skillActions=new SkillWindow.Actions(){
    public void use(SkillBook.Entry e){useBookSkill(e);}
    public boolean canLearn(SkillBook.Entry e){return F5mSaveStore.writable()&&quote(e).canLearn;}
    public SkillAcquisition.Quote quote(SkillBook.Entry e){return new SkillAcquisition(skillBook).quote(e,state.rpg());}
    public long gold(){return state.rpg().gold();}
    public void learn(SkillBook.Entry e){
      if(!F5mSaveStore.writable()){showFeedback("ì €ì¥ ë°ì´í„°ë¥¼ í™•ì¸í•  ìˆ˜ ì—†ì–´ ìŠµë“í•  ìˆ˜ ì—†ìŠµë‹ˆë‹¤",FeedbackTone.WARN);return;}
      SkillAcquisition service=new SkillAcquisition(skillBook);
      if(service.learn(e.id,state.rpg(),()->F5mSaveStore.checkpointActive())){performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);showFeedback(e.name+" ìŠµë“ ì™„ë£Œ",FeedbackTone.INFO);}
      else{SkillAcquisition.Quote q=service.quote(e,state.rpg());showFeedback(q.learned?"ì´ë¯¸ ìŠµë“í•œ ìŠ¤í‚¬ì…ë‹ˆë‹¤":q.blockers.isEmpty()?"ì €ì¥ ì‹¤íŒ¨ Â· ìŠµë“ ë¹„ìš©ì„ ëŒë ¤ë“œë ¸ìŠµë‹ˆë‹¤":String.join(" Â· ",q.blockers),FeedbackTone.WARN);}
    }
    public boolean save(){return saveSkillSlots();}
    public void testMode(boolean enabled){setSkillTestMode(enabled);}
    public float cooldown(String id){return combatSession.cooldownRemaining(RuntimeCombatSession.PLAYER_ID,"SK_ê³µí†µ_001".equals(id)?RuntimeCombatSession.playerAttackActionId(equipmentActions.resolveBasicAttack(state.rpg()).animationAction):id);}
    public String requirements(SkillBook.Entry e){return new SkillAcquisition(skillBook).description(e,state.rpg());}
    public void notice(String text){showFeedback(text,FeedbackTone.INFO);}
  };
  private void drawSkills(Canvas c){if(!skillWindow.open)return;c.save();c.translate(hudCenterOffset,0);skillWindow.draw(c,skillActions);c.restore();}
  private void useBookSkill(SkillBook.Entry entry){
    if(entry==null||isActing()||!state.player().alive)return;
    if(!skillBook.jobAllowed(entry.id)||!skillBook.usable(entry.id)){useBookSkillNow(entry);return;}
    cancelSkillApproach();autoAttackEnabled=false;
    if("SK_ê³µí†µ_001".equals(entry.id)){useBookSkillNow(entry);return;}
    SkillActionContract.Rule rule=SkillActionContract.get(entry.id);
    if(rule!=null&&rule.selfAnchored()){useBookSkillNow(entry);return;}
    boolean offensive=rule!=null&&rule.presentationAllowed()&&(!rule.selfAnchored()||rule.damage());
    if(!offensive){useBookSkillNow(entry);return;}
    float cd=skillActions.cooldown(entry.id);if(cd>0){showFeedback(String.format(java.util.Locale.ROOT,"ì¿¨íƒ€ì„ %.1fì´ˆ ë‚¨ìŒ",cd),FeedbackTone.WARN);return;}
    if(!skillBook.testAccess()&&entry.runtime!=null&&state.player().mp<entry.runtime.mpCost){showFeedback("MPê°€ ë¶€ì¡±í•©ë‹ˆë‹¤",FeedbackTone.WARN);return;}
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
    if(target==null||route==null){showFeedback("ê¸°ìˆ ì„ ì‚¬ìš©í•  ìˆ˜ ìˆëŠ” ê²½ë¡œê°€ ì—†ìŠµë‹ˆë‹¤",FeedbackTone.WARN);return;}
    skillApproach.begin(entry.id,target.id);interaction.cancel();combat.cancelApproach();joy=false;vx=vy=0;
    tickSkillApproach(0);
    if(skillApproach.skillId!=null)showFeedback(entry.name+" Â· ì‚¬ìš© ê°€ëŠ¥í•œ ìœ„ì¹˜ë¡œ ì´ë™",FeedbackTone.INFO);
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
    if(path==null){cancelSkillApproach();showFeedback("ê¸°ìˆ  ì ‘ê·¼ ê²½ë¡œê°€ ë§‰í˜”ìŠµë‹ˆë‹¤",FeedbackTone.WARN);return;}
    skillApproach.plannedX=tx;skillApproach.plannedY=ty;activeWorld().movement().requestSkillApproach(target.id,path);
  }
  private void useBookSkillNow(SkillBook.Entry entry){useBookSkillNow(entry,false);}
  private void useBookSkillNow(SkillBook.Entry entry,boolean automatic){
    if(entry==null)return;
    if(!skillBook.jobAllowed(entry.id)){showFeedback(entry.job+" ì „ìš© ê¸°ìˆ ì…ë‹ˆë‹¤",FeedbackTone.WARN);return;}
    if(!skillBook.usable(entry.id)){showFeedback(skillBook.learned(entry.id)?"ì´ ê¸°ìˆ ì˜ ì „íˆ¬ íš¨ê³¼ëŠ” ì¤€ë¹„ ì¤‘ì…ë‹ˆë‹¤":"ë¨¼ì € ê¸°ìˆ ì„ ìŠµë“í•˜ì„¸ìš”",FeedbackTone.WARN);return;}
    if(!automatic)autoAttackEnabled=false;
    if("SK_ê³µí†µ_001".equals(entry.id)){float remaining=skillActions.cooldown(entry.id);if(remaining>0)showFeedback(String.format(java.util.Locale.ROOT,"ì¿¨íƒ€ì„ %.1fì´ˆ ë‚¨ìŒ",remaining),FeedbackTone.WARN);else attack();return;}
    SkillActionContract.Rule rule=SkillActionContract.get(entry.id);
    if(rule!=null&&!rule.presentationAllowed()){
      SkillAbilityCatalog.Ability ability=SkillAbilityCatalog.get(entry.id);
      if(rule.mode.equals("LINKED")||rule.pattern==SkillActionContract.Pattern.PASSIVE){showFeedback(entry.name+" Â· ê¸°ë³¸ê³µê²©/ì¥ì°©ì— ì ìš©",FeedbackTone.INFO);return;}
      if(ability==null||!ability.supported()){showFeedback(entry.name+" Â· í•„ìš”í•œ ëŒ€ìƒ ì„œë¹„ìŠ¤ê°€ ì•„ì§ ì—†ìŠµë‹ˆë‹¤",FeedbackTone.WARN);return;}
      if(combatSession.useUtility(entry.id))consumeSkillNotice();else showFeedback(entry.name+" Â· ìì›/ì¿¨íƒ€ì„ ì¡°ê±´ì„ í™•ì¸í•˜ì„¸ìš”",FeedbackTone.WARN);return;
    }
    if(entry.runtime==null){showFeedback(entry.name+" Â· íš¨ê³¼ ë¯¸í™•ì •",FeedbackTone.WARN);return;}
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
    if(cd>0){showFeedback(String.format(java.util.Locale.ROOT,"ì¿¨íƒ€ì„ %.1fì´ˆ ë‚¨ìŒ",cd),FeedbackTone.WARN);return;}
    if(isActing()||!state.player().alive)return;
    SkillActionContract.Rule rule=SkillActionContract.get(def.id);boolean self=rule!=null?rule.selfAnchored():def.targetPolicy==SkillDef.TargetPolicy.SELF;if(!self&&!requireTarget())return;
    CombatActionOrchestrator.Submission result=combatSession.submitPlayer(self?RuntimeCombatSession.PLAYER_ID:combat.target().id,def.id);
    if(result.accepted()){if(!self)snapshotAttackFacingTarget();else playerFacing.beginAttack();activeSkillVisualId=def.id;trigger(visual);}
    else showFeedback(result.rejectReason==CombatResolver.RejectReason.NOT_LEARNED?"ì•„ì§ ë°°ìš°ì§€ ì•Šì€ ê¸°ìˆ ì…ë‹ˆë‹¤":result.rejectReason==CombatResolver.RejectReason.RESOURCE?"MPê°€ ë¶€ì¡±í•©ë‹ˆë‹¤":result.rejectReason==CombatResolver.RejectReason.COOLDOWN?"ì¿¨íƒ€ì„ ì¤‘ì…ë‹ˆë‹¤":result.rejectReason==CombatResolver.RejectReason.RANGE?"ëŒ€ìƒì´ ì‚¬ê±°ë¦¬ ë°–ì— ìˆìŠµë‹ˆë‹¤":"ì§€ê¸ˆ ì‚¬ìš©í•  ìˆ˜ ì—†ìŠµë‹ˆë‹¤",FeedbackTone.WARN);
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
      if(best==null){combat.clearTarget();if(autoTargetHintClock<=0f){showFeedback("ì ‘ê·¼ ê°€ëŠ¥í•œ ëª¬ìŠ¤í„°ê°€ ì—†ìŠµë‹ˆë‹¤",FeedbackTone.INFO);autoTargetHintClock=3f;}return;}
      combat.selectTarget(best);
      autoTargetHintClock=1f;
    }
    // Use equipped slots in a fair cycle; skip unavailable actions without disabling AUTO.
    current=combat.target();
    for(int n=0;n<SkillBook.SLOT_COUNT;n++){
      int index=(autoSkillCursor+n)%SkillBook.SLOT_COUNT;SkillBook.Entry entry=skillBook.get(skillBook.slot(index));
      if(entry==null||!skillBook.usable(entry.id)||"SK_ê³µí†µ_001".equals(entry.id))continue;
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
    if(npc.id.startsWith("campaign_altar_"))body="ìˆ²ì˜ ì •ìˆ˜ë¥¼ ëª¨ì•„ ì œë‹¨ì„ ì •í™”í•˜ì„¸ìš”. ì •í™” ì˜ë¢°ë¥¼ ìˆ˜ë½í•œ ë’¤ ì„¸ ê³³ì„ ê°ê° ë°©ë¬¸í•©ë‹ˆë‹¤.";
7ç8¶‰Ëkºwµçy•¹ÑÉåM•Ğ ¤¥¥˜ …¥Ñ•µÌ¹½¹Ñ…¥¹Í-•ä¡”¹•Ñ-•ä ¤¥ññ”¹•ÑY…±Õ” ¤ôõ¹Õ±±ññ”¹•ÑY…±Õ” ¤ğôÁññ”¹•ÑY…±Õ” ¤ù%9Y9Q=Ie}MQ-}1%5%P¥É•ÑÕÉ¸™…±Í”ì(€€€‰…¹­½±õ‰…±…¹”í‰…¹­%¹Ù•¹Ñ½Éä¹±•…È ¤í‰…¹­%¹Ù•¹Ñ½Éä¹ÁÕÑ±°¡‘•Á½Í¥Ñ•¤íÉ•ÑÕÉ¸ÑÉÕ”ì(€ô(€ÁÕ‰±¥ŒÙ½¥É•ÍÑ½É•½±¡±½¹œÙ…±Õ”¥í½±õ5…Ñ ¹µ…à Á0±Ù…±Õ”¤íô(€€¼¨¨¥ÉÍĞµ©½ˆ¡½¥”¥Ì„½¹”µÑ¥µ”°½ÍĞµ™É•”µ½‰¥±”…µÁ…¥¸ÑÉ…¹Í…Ñ¥½¸¸€¨¼(€ÁÕ‰±¥Œ‰½½±•…¸¡½½Í•%¹¥Ñ¥…±)½ˆ¡MÑÉ¥¹œ©½‰½‘”¥ì(€€€¥˜¡¹½Éµ…±1•Ù•°ôõ¹Õ±±ññ¹½Éµ…±1•Ù•°ğÍñğ„‰=55=9Hˆ¹•ÅÕ…±Ì¡ÕÉÉ•¹Ñ)½‰½‘”¤¥É•ÑÕÉ¸™…±Í”ì(€€€¥˜ „ ‰]II%=Hˆ¹•ÅÕ…±Ì¡©½‰½‘”¥ñğ‰I=Uˆ¹•ÅÕ…±Ì¡©½‰½‘”¥ñğ‰5ˆ¹•ÅÕ…±Ì¡©½‰½‘”¥ñğ‰1I%ˆ¹•ÅÕ…±Ì¡©½‰½‘”¥ñğ‰5IQ%1}IQ%MPˆ¹•ÅÕ…±Ì¡©½‰½‘”¤¤¥É•ÑÕÉ¸™…±Í”ì(€€€ÕÉÉ•¹Ñ)½‰½‘”õ©½‰½‘”ì(€€€ÁÉ½É•ÍÍ¥½¹9½‘”õAÉ½É•ÍÍ¥½¹9½‘”¹	M%})=ì(€€€MÑÉ¥¹œİ•…Á½¸ô‰]II%=Hˆ¹•ÅÕ…±Ì¡©½‰½‘”¤ü‰%Q}	})=	}]II%=I}]A=8ˆè‰I=Uˆ¹•ÅÕ…±Ì¡©½‰½‘”¤ü‰%Q}	})=	}I=U}]A=8ˆè‰5ˆ¹•ÅÕ…±Ì¡©½‰½‘”¤ü‰%Q}	})=	}5}]9ˆè‰1I%ˆ¹•ÅÕ…±Ì¡©½‰½‘”¤ü‰%Q}	})=	}1I%}]9ˆé¹Õ±°ì(€€€MÑÉ¥¹œ…Éµ½Èô‰]II%=Hˆ¹•ÅÕ…±Ì¡©½‰½‘”¤ü‰%Q}	})=	}]II%=I}QU9%ˆè‰I=Uˆ¹•ÅÕ…±Ì¡©½‰½‘”¤ü‰%Q}	})=	}I=U}I59Pˆè‰5ˆ¹•ÅÕ…±Ì¡©½‰½‘”¤ü‰%Q}	})=	}5}I=	ˆè‰1I%ˆ¹•ÅÕ…±Ì¡©½‰½‘”¤ü‰%Q}	})=	}1I%}I=	ˆè‰%Q}	})=	}5=9-}$ˆì(€€€¥˜¡İ•…Á½¸ôõ¹Õ±°¥•ÅÕ¥Áµ•¹Ñ	åM±½Ğ¹É•µ½Ù”¡]A=9}M1=P¤í•±Í•í¥¹Ù•¹Ñ½Éä¹ÁÕĞ¡İ•…Á½¸±5…Ñ ¹µ…à Ä±Ù…±Õ”¡¥¹Ù•¹Ñ½Éä±İ•…Á½¸¤¤¤í•ÅÕ¥Áµ•¹Ñ	åM±½Ğ¹ÁÕĞ¡]A=9}M1=P±İ•…Á½¸¤íô(€€€¥¹Ù•¹Ñ½Éä¹ÁÕĞ¡…Éµ½È±5…Ñ ¹µ…à Ä±Ù…±Õ”¡¥¹Ù•¹Ñ½Éä±…Éµ½È¤¤¤í•ÅÕ¥Áµ•¹Ñ	åM±½Ğ¹ÁÕĞ¡I5=I}M1=P±…Éµ½È¤ì(€€€É•ÑÕÉ¸ÑÉÕ”ì(€ô(€ÁÕ‰±¥Œ‰½½±•…¸‰ÕåMµ…±±A½Ñ¥½¸ ¥íÉ•ÑÕÉ¸‰Õå%Ñ•´¡	}M511}A=Q%=9}%Q5}%±	}M511}A=Q%=9}AI%¤íô(€ÁÕ‰±¥Œ‰½½±•…¸‰Õå%Ñ•´¡MÑÉ¥¹œ¥Ñ•µ%±±½¹œÁÉ¥”¥ì(€€€¥˜¡ÁÉ¥”ğÁññ½±ñÁÉ¥•ñğ…¥Ñ•µÌ¹½¹Ñ…¥¹Í-•ä¡¥Ñ•µ%¤¥É•ÑÕÉ¸™…±Í”ì(€€€ÕÑ½1½½ÑI•ÍÕ±Ğ…‘‘•õ…ÕÑ½1½½ÑI•Í½±Ù•‘%Ñ•´¡¥Ñ•µ%°Ä¤ì(€€€¥˜¡…‘‘•„õÕÑ½1½½ÑI•ÍÕ±Ğ¹1==Q¥É•ÑÕÉ¸™…±Í”ì(€€€½±´õÁÉ¥”í…µÁ…¥¸¹‰½Õ¡Ğ ¤íÉ•ÑÕÉ¸ÑÉÕ”ì(€ô(€€¼¨¨Ñ½µ¥ŒÁÉ½©•ĞÍ­¥±°µ…ÅÕ¥Í¥Ñ¥½¸Á…åµ•¹ĞèÙ…±¥‘…Ñ”•Ù•Éä½ÍĞ‰•™½É”µÕÑ…Ñ¥¹œ…¹ä‰…±…¹”¸€¨¼(€ÁÕ‰±¥Œ‰½½±•…¸Á…åM­¥±±1•…É¹¥¹½ÍĞ¡±½¹œÁÉ¥”±5…ÀñMÑÉ¥¹œ±%¹Ñ••Èøµ…Ñ•É¥…±Ì¥ì(€€€¥˜¡ÁÉ¥”ğÁññ½±ñÁÉ¥•ññµ…Ñ•É¥…±Ìôõ¹Õ±°¥É•ÑÕÉ¸™…±Í”ì(€€€™½È¡5…À¹¹ÑÉäñMÑÉ¥¹œ±%¹Ñ••Èø”éµ…Ñ•É¥…±Ì¹•¹ÑÉåM•Ğ ¤¥ì(€€€€€%Ñ•µ•™¥¹¥Ñ¥½¸õ¥Ñ•µÌ¹•Ğ¡”¹•Ñ-•ä ¤¤í%¹Ñ••È¸õ”¹•ÑY…±Õ” ¤ì(€€€€€¥˜¡ôõ¹Õ±±ññ¹•ÅÕ¥ÁÁ…‰±” ¥ññ¸ôõ¹Õ±±ññ¸ğôÁññÙ…±Õ”¡¥¹Ù•¹Ñ½Éä±”¹•Ñ-•ä ¤¤ñ¸¥É•ÑÕÉ¸™…±Í”ì(€€€ô(€€€½±´õÁÉ¥”ì(€€€™½È¡5…À¹¹ÑÉäñMÑÉ¥¹œ±%¹Ñ••Èø”éµ…Ñ•É¥…±Ì¹•¹ÑÉåM•Ğ ¤¥í¥¹Ğ±•™ĞõÙ…±Õ”¡¥¹Ù•¹Ñ½Éä±”¹•Ñ-•ä ¤¤µ”¹•ÑY…±Õ” ¤í¥˜¡±•™ĞôôÀ¥¥¹Ù•¹Ñ½Éä¹É•µ½Ù”¡”¹•Ñ-•ä ¤¤í•±Í”¥¹Ù•¹Ñ½Éä¹ÁÕĞ¡”¹•Ñ-•ä ¤±±•™Ğ¤íô(€€€É•ÑÕÉ¸ÑÉÕ”ì(€ô(€ÁÕ‰±¥Œ¥¹ĞÉ…¹Ñ‘…ÁÑ•‘I•İ…É¡±½¹œ•áÀ±±½¹œ½±‘µ½Õ¹Ğ¥í¥˜¡•áÀøÀ¥¹½Éµ…±áÀ¬õ•áÀí¥˜¡½±‘µ½Õ¹ĞøÀ¥½±¬õ½±‘µ½Õ¹ĞíÉ•ÑÕÉ¸¹½Éµ…±¥é•…¹½¹¥…±1•Ù•° ¤íô((€€¼¨¨I•ÍÑ½É•Ì‘ÕÉ…‰±”ÁÉ½É•ÍÍ¥½¸İ¥Ñ¡½ÕĞÉ•™±•Ñ¥½¸°Ñ¡•¸¹½Éµ…±¥é•Ì……¥¹ÍĞ…¹½¹¥…°1•Ù•±}aA}ÕÉÙ”¸€¨¼(€ÁÕ‰±¥ŒÙ½¥É•ÍÑ½É•AÉ½É•ÍÍ¥½¸¡¥¹Ğ±•Ù•°±±½¹œ•áÀ¥ì(€€€¹½Éµ…±1•Ù•°õ5…Ñ ¹µ…à Ä±5…Ñ ¹µ¥¸ ää±±•Ù•°¤¤ì(€€€¹½Éµ…±áÀõ5…Ñ ¹µ…à Á0±•áÀ¤ì(€€€¹½Éµ…±¥é•…¹½¹¥…±1•Ù•° ¤ì(€ô((€€¼¨¨ÁÁ±¥•Ì½¹±ä…¹½¹¥…°±•Ù•°Ñ¡É•Í¡½±‘Ì…¹…ÉÉ¥•Ì½Ù•É™±½Üa@Ñ¼Ñ¡”¹•áĞ±•Ù•°¸€¨¼(€ÁÕ‰±¥Œ¥¹Ğ¹½Éµ…±¥é•…¹½¹¥…±1•Ù•° ¥ì(€€€¥¹Ğ‰•™½É”õ¹½Éµ…±1•Ù•°ôõ¹Õ±°üÄé¹½Éµ…±1•Ù•°ì(€€€¥¹Ğ±•Ù•°õ‰•™½É”í±½¹œ•áÀõ¹½Éµ…±áÀôõ¹Õ±°üÁ0é¹½Éµ…±áÀì(€€€İ¡¥±”¡±•Ù•°ğää¥í1½¹œÉ•ÅÕ¥É•õ1•Ù•±áÁÕÉÙ”¹É•ÅÕ¥É•‘½É9•áĞ¡±•Ù•°¤í¥˜¡É•ÅÕ¥É•ôõ¹Õ±±ññ•áÀñÉ•ÅÕ¥É•¥‰É•…¬í•áÀ´õÉ•ÅÕ¥É•í±•Ù•°¬¬íô(€€€¹½Éµ…±1•Ù•°õ±•Ù•°í¹½Éµ…±áÀõ•áÀí¥¹Ğ…¥¹•õ±•Ù•°µ‰•™½É”í¥˜¡…¥¹•øÀ¥í™½È¡¥¹Ğ±Øõ‰•™½É”¬Äí±Øğõ±•Ù•°í±Ø¬¬¥í‰…Í•5…á!À¬õÉ½İÑ¡A½±¥ä¹¡Á…¥¸¡±Ø±ÕÉÉ•¹Ñ)½‰½‘”±½¸¤í‰…Í•5…á5À¬õÉ½İÑ¡A½±¥ä¹µÁ…¥¸¡±Ø±ÕÉÉ•¹Ñ)½‰½‘”±İ¥Ì¤íõÍÑ…ÑA½¥¹ÑÌ¬õ…¥¹•¨ÈíõÉ•ÑÕÉ¸…¥¹•ì(€ô((€€¼¨¨AÕÉ”É•ÅÕ¥É•µ•¹ĞÁÉ½©•Ñ¥½¸™½ÈU$½…Õ‘¥ÑÌì¥Ğ‘½•Ì¹½ĞµÕÑ…Ñ”Á±…å•È½È¥Ñ•´ÍÑ…Ñ”¸€¨¼(€ÁÕ‰±¥ŒI•ÅÕ¥É•µ•¹ÑI•ÍÕ±Ğ•Ù…±Õ…Ñ•I•ÅÕ¥É•µ•¹ÑÌ¡MÑÉ¥¹œ¥Ñ•µ%±MÑÉ¥¹œ©½‰½‘”±%¹Ñ••È±•Ù•°¥ì(€€€%Ñ•µ•™¥¹¥Ñ¥½¸‘•˜õ¥Ñ•µÌ¹•Ğ¡¥Ñ•µ%¤ì(€€€¥˜¡‘•˜ôõ¹Õ±°¥É•ÑÕÉ¸I•ÅÕ¥É•µ•¹ÑI•ÍÕ±Ğ¹U9-9=]9}%Q4ì(€€€¥˜¡‘•˜¹É•ÅÕ¥É•‘1•Ù•°„õ¹Õ±°˜™±•Ù•°ôõ¹Õ±°¥É•ÑÕÉ¸I•ÅÕ¥É•µ•¹ÑI•ÍÕ±Ğ¹A9%9ì(€€€¥˜¡‘•˜¹É•ÅÕ¥É•‘1•Ù•°„õ¹Õ±°˜™±•Ù•°ñ‘•˜¹É•ÅÕ¥É•‘1•Ù•°¥É•ÑÕÉ¸I•ÅÕ¥É•µ•¹ÑI•ÍÕ±Ğ¹1Y1}9=Q}5Pì(€€€¥˜ …‘•˜¹©½‰I•ÍÑÉ¥Ñ¥½¹I•Í½±Ù•¥É•ÑÕÉ¸I•ÅÕ¥É•µ•¹ÑI•ÍÕ±Ğ¹A9%9ì(€€€¥˜ …‘•˜¹…±±½İ•‘)½‰½‘•Ì¹¥ÍµÁÑä ¤˜˜¡©½‰½‘”ôõ¹Õ±±ñğ…‘•˜¹…±±½İ•‘)½‰½‘•Ì¹½¹Ñ…¥¹Ì¡©½‰½‘”¤¤¥É•ÑÕÉ¸I•ÅÕ¥É•µ•¹ÑI•ÍÕ±Ğ¹)=	}9=Q}5Pì(€€€É•ÑÕÉ¸I•ÅÕ¥É•µ•¹ÑI•ÍÕ±Ğ¹5Pì(€ô((€ÁÕ‰±¥ŒI•ÅÕ¥É•µ•¹ÑI•ÍÕ±ĞÕÉÉ•¹ÑI•ÅÕ¥É•µ•¹ÑÌ¡MÑÉ¥¹œ¥Ñ•µ%¥íÉ•ÑÕÉ¸•Ù…±Õ…Ñ•I•ÅÕ¥É•µ•¹ÑÌ¡¥Ñ•µ%±ÕÉÉ•¹Ñ)½‰½‘”±¹½Éµ…±1•Ù•°¤íô((€€¼¨¨(€€€¨½¹ÍÕµ•Ì½µ‰…Ğ•Ù•¹ÑÌ•á…Ñ±ä½¹”¸U¹­¹½İ¸µ½¹ÍÑ•ÈÉ•İ…Éµ…ÁÁ¥¹œÁÉ½‘Õ•Ì…¸•áÁ±¥¥ĞA9%9É•ÍÕ±Ğ¸(€€€¨I•Í½±Ù•¥Ñ•´É•İ…É‘Ì…É”¥¹Í•ÉÑ•‘¥É•Ñ±ä¥¹Ñ¼¥¹Ù•¹Ñ½Éäì¹¼İ½É±µ‘É½À•¹Ñ¥Ñä½ÈÁ¥­ÕÀÁ¡…Í”•á¥ÍÑÌ¸(€€€¨¼(€ÁÕ‰±¥ŒÙ½¥½¹ÍÕµ•½µ‰…Ğ¡1¥ÍĞñ½µ‰…Ñ1•‘•È¹Ù•¹Ğø•Ù•¹ÑÌ±IÕ¹Ñ¥µ•MÑ…Ñ”ÉÕ¹Ñ¥µ”¥ì(€€€™½È¡½µ‰…Ñ1•‘•È¹Ù•¹Ğ”é•Ù•¹ÑÌ¥ì(€€€€€¥˜¡”¹Í•ÅÕ•¹”ğõ±…ÍÑ½µ‰…ÑM•ÅÕ•¹”¥½¹Ñ¥¹Õ”ì(€€€€€±…ÍÑ½µ‰…ÑM•ÅÕ•¹”õ”¹Í•ÅÕ•¹”ì(€€€€€…µÁ…¥¸¹½¹ÍÕµ”¡”±ÉÕ¹Ñ¥µ”¤ì(€€€€€¥˜¡”¹ÑåÁ”„õ½µ‰…Ñ1•‘•È¹QåÁ”¹5=9MQI}Q¥½¹Ñ¥¹Õ”ì(€€€€€MÑÉ¥¹œ…µÁ…¥¹%õÉÕ¹Ñ¥µ”ôõ¹Õ±°ı¹Õ±°éÉÕ¹Ñ¥µ”¹…µÁ…¥¹I•İ…É‘AÉ½™¥±•½È¡”¹Ñ…É•Ñ%¤ì(€€€€€É•Í½±Ù•…µÁ…¥¹•™•…Ğ¡”±…µÁ…¥¹%¤ì(€€€ô(€ô((€ÁÉ¥Ù…Ñ”Ù½¥É•Í½±Ù•5½¹ÍÑ•É•™•…Ğ¡½µ‰…Ñ1•‘•È¹Ù•¹Ğ”¥ì(€€€…¹½¹¥…±5½¹ÍÑ•ÉI•İ…É‘…Ñ…±½œ¹I•İ…É‘¹ÑÉäÉ•İ…Éõµ½¹ÍÑ•ÉI•İ…É‘Ì¹™¥¹¡”¹Ñ…É•Ñ%¤ì(€€€¥˜¡É•İ…Éôõ¹Õ±°¥ì(€€€€€€¼¼¸•áÁ±¥¥Ğ…Ñ½Èµ±•Ù•°…‘…ÁÑ•È¥ÌÉ•ÅÕ¥É•¸Q¡”…¹½¹¥…°A=Q}AUIA1™¥áÑÕÉ”…¹(€€€€€€¼¼•Ù•ÉäÕ¹ÁÉ½™¥±•‘•™•…ĞÉ•µ…¥¸Õ¹É•Í½±Ù•°ÁÉ•Í•ÉÙ¥¹œÑ¡”™…¥°µ±½Í•‰½Õ¹‘…Éä…Õ‘¥Ğ¸(€€€€€€¼¼€¡Q¡”ÉÕ¹Ñ¥µ”ÍÕÁÁ±¥•Ì¥ÑÌÁÉ½™¥±”Ñ¡É½Õ Ñ¡”½Ù•É±½…‰•±½Ü¸¤(€€€€€‘…ÁÑ•‘AÉ½Ñ½ÑåÁ•I•İ…É‘…Ñ…±½œ¹¹ÑÉäÁÉ½Ñ½ÑåÁ”õÁÉ½Ñ½ÑåÁ•I•İ…É‘Ì¹™¥¹¡”¹Ñ…É•Ñ%¤ì(€€€€€¥˜¡ÁÉ½Ñ½ÑåÁ”„õ¹Õ±°¥ì(€€€€€€€5…ÀñMÑÉ¥¹œ±%¹Ñ••ÈøÉ…¹Ñ•õ¹•Ü1¥¹­•‘!…Í¡5…Àğø ¤ì(€€€€€€€5…ÀñMÑÉ¥¹œ±ÕÑ½1½½ÑI•ÍÕ±Ğø½ÕÑ½µ•Ìõ¹•Ü1¥¹­•‘!…Í¡5…Àğø ¤ì(€€€€€€€ÕÑ½1½½ÑI•ÍÕ±ĞÉ•ÍÕ±Ğõ…ÕÑ½1½½ÑI•Í½±Ù•‘%Ñ•´¡ÁÉ½Ñ½ÑåÁ”¹¥Ñ•µ%±ÁÉ½Ñ½ÑåÁ”¹ÅÕ…¹Ñ¥Ñä¤ì(€€€€€€€½ÕÑ½µ•Ì¹ÁÕĞ¡ÁÉ½Ñ½ÑåÁ”¹¥Ñ•µ%±É•ÍÕ±Ğ¤ì(€€€€€€€¥˜¡É•ÍÕ±ĞôõÕÑ½1½½ÑI•ÍÕ±Ğ¹1==Q¥É…¹Ñ•¹ÁÕĞ¡ÁÉ½Ñ½ÑåÁ”¹¥Ñ•µ%±ÁÉ½Ñ½ÑåÁ”¹ÅÕ…¹Ñ¥Ñä¤ì(€€€€€€€É…¹Ñ‘…ÁÑ•‘I•İ…É¡‘…ÁÑ•‘AÉ½Ñ½ÑåÁ•I•İ…É‘…Ñ…±½œ¹QI%9%9}5=9MQI}a@±‘…ÁÑ•‘AÉ½Ñ½ÑåÁ•I•İ…É‘…Ñ…±½œ¹QI%9%9}5=9MQI}=1¤ì(€€€€€€€É•İ…É‘!¥ÍÑ½Éä¹…‘¡¹•ÜI•İ…É‘I•Í½±ÕÑ¥½¸¡”¹Í•ÅÕ•¹”±”¹Ñ…É•Ñ%±I•İ…É‘MÑ…ÑÕÌ¹IM=1Y±‘…ÁÑ•‘AÉ½Ñ½ÑåÁ•I•İ…É‘…Ñ…±½œ¹QI%9%9}5=9MQI}a@±É…¹Ñ•±½ÕÑ½µ•Ì°(€€€€€€€€€€€I•İ…É‘M½ÕÉ”¹AQ}QMP±ÁÉ½Ñ½ÑåÁ”¹Á½±¥å%±ÁÉ½Ñ½ÑåÁ”¹•Ù¥‘•¹”±‘…ÁÑ•‘AÉ½Ñ½ÑåÁ•I•İ…É‘…Ñ…±½œ¹QI%9%9}5=9MQI}=1¤¤ì(€€€€€€€ÑÉ¥µI•İ…É‘!¥ÍÑ½Éä ¤ì(€€€€€€€É•ÑÕÉ¸ì(€€€€€ô(€€€€€É•İ…É‘!¥ÍÑ½Éä¹…‘¡¹•ÜI•İ…É‘I•Í½±ÕÑ¥½¸¡”¹Í•ÅÕ•¹”±”¹Ñ…É•Ñ%±I•İ…É‘MÑ…ÑÕÌ¹A9%9}9=}9=9%1}5=9MQI}I]I±¹Õ±°±½±±•Ñ¥½¹Ì¸ñMÑÉ¥¹œ±%¹Ñ••Èù•µÁÑå5…À ¤¤¤ì(€€€€€ÑÉ¥µI•İ…É‘!¥ÍÑ½Éä ¤ì(€€€€€É•ÑÕÉ¸ì(€€€ô((€€€5…ÀñMÑÉ¥¹œ±%¹Ñ••Èø±½½Ñ•õ¹•Ü1¥¹­•‘!…Í¡5…Àğø ¤ì(€€€5…ÀñMÑÉ¥¹œ±ÕÑ½1½½ÑI•ÍÕ±Ğø½ÕÑ½µ•Ìõ¹•Ü1¥¹­•‘!…Í¡5…Àğø ¤ì(€€€™½È¡…¹½¹¥…±5½¹ÍÑ•ÉI•İ…É‘…Ñ…±½œ¹É½Á!¥¹Ğ¡¥¹ĞéÉ•İ…É¹‘É½Á!¥¹ÑÌ¥ì(€€€€€¥˜ …¡¥¹Ğ¹•µ¥ÍÍ¥½¹I•Í½±Ù• ¤¥½¹Ñ¥¹Õ”ì(€€€€€ÕÑ½1½½ÑI•ÍÕ±ĞÉ•ÍÕ±Ğõ…ÕÑ½1½½ÑI•Í½±Ù•‘%Ñ•´¡¡¥¹Ğ¹¥Ñ•µ%±¡¥¹Ğ¹ÅÕ…¹Ñ¥Ñä¤ì(€€€€€½ÕÑ½µ•Ì¹ÁÕĞ¡¡¥¹Ğ¹¥Ñ•µ%±É•ÍÕ±Ğ¤ì(€€€€€¥˜¡É•ÍÕ±ĞôõÕÑ½1½½ÑI•ÍÕ±Ğ¹1==Q¥±½½Ñ•¹ÁÕĞ¡¡¥¹Ğ¹¥Ñ•µ%±Ù…±Õ”¡±½½Ñ•±¡¥¹Ğ¹¥Ñ•µ%¤­¡¥¹Ğ¹ÅÕ…¹Ñ¥Ñä¤ì(€€€ô(€€€¥˜¡É•İ…É¹•áÀ„õ¹Õ±°¥¹½Éµ…±áÀ¬õÉ•İ…É¹•áÀ¹±½¹Y…±Õ” ¤ì(€€€É•İ…É‘!¥ÍÑ½Éä¹…‘¡¹•ÜI•İ…É‘I•Í½±ÕÑ¥½¸¡”¹Í•ÅÕ•¹”±”¹Ñ…É•Ñ%±I•İ…É‘MÑ…ÑÕÌ¹IM=1Y±É•İ…É¹•áÀ±±½½Ñ•±½ÕÑ½µ•Ì°(€€€€€€€I•İ…É‘M½ÕÉ”¹9=9%0°‰9=9%1}5=9MQI}I]Iˆ±É•İ…É¹•áÁÙ¥‘•¹”ôõ¹Õ±°ı¹Õ±°éÉ•İ…É¹•áÁÙ¥‘•¹”¹¹…µ” ¤¤¤ì(€€€ÑÉ¥µI•İ…É‘!¥ÍÑ½Éä ¤ì(€ô((€€¼¨¨…µÁ…¥¸µ…İ…É”‘•™•…Ğ•¹‘Á½¥¹Ğì½¹±äµ…À…Ñ½ÉÌ…‘µ¥ÑÑ•‰äÑ¡”µ½‰¥±”™¥•±…‘…ÁÑ•È…¸É•Í½±Ù”¸€¨¼(€ÁÉ¥Ù…Ñ”Ù½¥É•Í½±Ù•…µÁ…¥¹•™•…Ğ¡½µ‰…Ñ1•‘•È¹Ù•¹Ğ”±MÑÉ¥¹œÁÉ½™¥±•%¥ì(€€€…¹½¹¥…±5½¹ÍÑ•ÉI•İ…É‘…Ñ…±½œ¹I•İ…É‘¹ÑÉä…¹½¹¥…°õµ½¹ÍÑ•ÉI•İ…É‘Ì¹™¥¹¡”¹Ñ…É•Ñ%¤ì(€€€¥˜¡…¹½¹¥…°„õ¹Õ±°¥íÉ•Í½±Ù•5½¹ÍÑ•É•™•…Ğ¡”¤íÉ•ÑÕÉ¸íô(€€€‘…ÁÑ•‘…µÁ…¥¹I•İ…É‘…Ñ…±½œ¹I•İ…ÉÉ•İ…Éõ…µÁ…¥¹I•İ…É‘Ì¹™¥¹¡ÁÉ½™¥±•%¤ì(€€€¥˜¡É•İ…Éôõ¹Õ±°¥íÉ•Í½±Ù•5½¹ÍÑ•É•™•…Ğ¡”¤íÉ•ÑÕÉ¸íô(€€€É…¹Ñ‘…ÁÑ•‘I•İ…É¡É•İ…É¹•áÀ±É•İ…É¹½±¤ì(€€€É•İ…É‘!¥ÍÑ½Éä¹…‘¡¹•ÜI•İ…É‘I•Í½±ÕÑ¥½¸¡”¹Í•ÅÕ•¹”±”¹Ñ…É•Ñ%±I•İ…É‘MÑ…ÑÕÌ¹IM=1Y±É•İ…É¹•áÀ°(€€€€€€€½±±•Ñ¥½¹Ì¸ñMÑÉ¥¹œ±%¹Ñ••Èù•µÁÑå5…À ¤±½±±•Ñ¥½¹Ì¸ñMÑÉ¥¹œ±ÕÑ½1½½ÑI•ÍÕ±Ğù•µÁÑå5…À ¤°(€€€€€€€I•İ…É‘M½ÕÉ”¹AQ}5A%8±‘…ÁÑ•‘…µÁ…¥¹I•İ…É‘…Ñ…±½œ¹A=1%e}%°‰­AQìé½¹”ôˆ­É•İ…É¹é½¹”±É•İ…É¹½±¤¤ì(€€€ÑÉ¥µI•İ…É‘!¥ÍÑ½Éä ¤ì(€ô((€€¼¨¨(€€€¨¥É•Ğµ¥¹Ù•¹Ñ½Éä•¹‘Á½¥¹Ğ™½È„É•İ…Éİ¡½Í”¥Ñ•´¥‘•¹Ñ¥Ñä…¹ÅÕ…¹Ñ¥Ñäİ•É”…±É•…‘äÉ•Í½±Ù•ÕÁÍÑÉ•…´¸(€€€¨%Ğ‘•±¥‰•É…Ñ•±äÉ•™ÕÍ•ÌÕ¹­¹½İ¸¥Ñ•µÌ½ÈÅÕ…¹Ñ¥Ñ¥•Ì¥¹ÍÑ•…½˜É•…Ñ¥¹œ„É½Õ¹™…±±‰…¬¸(€€€¨¼(€ÁÕ‰±¥Œ‰½½±•…¸¥Í½¹ÍÕµ…‰±”¡MÑÉ¥¹œ¥Ñ•µ%¥íÉ•ÑÕÉ¸Õ}M511}!A}A=Q%=9}%Q5}%¹•ÅÕ…±Ì¡¥Ñ•µ%¥ñğ‰%Q}I9Q}UIU4ˆ¹•ÅÕ…±Ì¡¥Ñ•µ%¥ñğ‰%Q}I9Q}aUI9U4ˆ¹•ÅÕ…±Ì¡¥Ñ•µ%¥ñğ‰%Q}	}5A}A=Q%=8ˆ¹•ÅÕ…±Ì¡¥Ñ•µ%¤íô(€ÁÕ‰±¥ŒUÍ•I•ÍÕ±ĞÕÍ•½¹ÍÕµ…‰±”¡MÑÉ¥¹œ¥Ñ•µ%±IÕ¹Ñ¥µ•MÑ…Ñ”ÉÕ¹Ñ¥µ”¥ì(€€€%¹Ñ••È½İ¹•õ¥¹Ù•¹Ñ½Éä¹•Ğ¡¥Ñ•µ%¤í¥˜¡½İ¹•ôõ¹Õ±±ññ½İ¹•ğôÀ¥É•ÑÕÉ¸UÍ•I•ÍÕ±Ğ¹%Q5}9=Q}=]9ì(€€€¥˜ …¥Í½¹ÍÕµ…‰±”¡¥Ñ•µ%¥ññÉÕ¹Ñ¥µ”ôõ¹Õ±°¥É•ÑÕÉ¸UÍ•I•ÍÕ±Ğ¹9=Q}=9MU5	1ì(€€€¥˜ ‰%Q}	}5A}A=Q%=8ˆ¹•ÅÕ…±Ì¡¥Ñ•µ%¤¥í¥˜¡ÉÕ¹Ñ¥µ”¹Á±…å•È ¤¹µÀøõÉÕ¹Ñ¥µ”¹Á±…å•È ¤¹µ…á5À¥É•ÑÕÉ¸UÍ•I•ÍÕ±Ğ¹9=}PíÉÕ¹Ñ¥µ”¹Á±…å•È ¤¹µÀõ5…Ñ ¹µ¥¸¡ÉÕ¹Ñ¥µ”¹Á±…å•È ¤¹µ…á5À±ÉÕ¹Ñ¥µ”¹Á±…å•È ¤¹µÀ¬ÄÀÀ¤í¥˜¡½İ¹•ôôÄ¥¥¹Ù•¹Ñ½Éä¹É•µ½Ù”¡¥Ñ•µ%¤í•±Í”¥¹Ù•¹Ñ½Éä¹ÁÕĞ¡¥Ñ•µ%±½İ¹•´Ä¤í…µÁ…¥¸¹¡•…±• ¤íÉ•ÑÕÉ¸UÍ•I•ÍÕ±Ğ¹UMíô(€€€¥˜¡ÉÕ¹Ñ¥µ”¹Á±…å•È ¤¹¡ÀøõÉÕ¹Ñ¥µ”¹Á±…å•È ¤¹µ…á!À¥É•ÑÕÉ¸UÍ•I•ÍÕ±Ğ¹9=}Pì(€€€¥¹Ğ…µ½Õ¹Ğô‰%Q}I9Q}aUI9U4ˆ¹•ÅÕ…±Ì¡¥Ñ•µ%¤üÄÀÀÀÀè‰%Q}I9Q}UIU4ˆ¹•ÅÕ…±Ì¡¥Ñ•µ%¤üÄÀÀéÕ}M511}!A}A=Q%=9}!0ì(€€€ÉÕ¹Ñ¥µ”¹Á±…å•È ¤¹¡Àõ5…Ñ ¹µ¥¸¡ÉÕ¹Ñ¥µ”¹Á±…å•È ¤¹µ…á!À±ÉÕ¹Ñ¥µ”¹Á±…å•È ¤¹¡À­…µ½Õ¹Ğ¤ì(€€€¥˜¡½İ¹•ôôÄ¥¥¹Ù•¹Ñ½Éä¹É•µ½Ù”¡¥Ñ•µ%¤í•±Í”¥¹Ù•¹Ñ½Éä¹ÁÕĞ¡¥Ñ•µ%±½İ¹•´Ä¤ì(€€€…µÁ…¥¸¹¡•…±• ¤íÉ•ÑÕÉ¸UÍ•I•ÍÕ±Ğ¹UMì(€ô(€ÁÕ‰±¥Œ‰½½±•…¸¥ÍI•…±°¡MÑÉ¥¹œ¥¥íÉ•ÑÕÉ¸I11}5%11M}%Q5}%¹•ÅÕ…±Ì¡¥¤íô(€ÁÕ‰±¥ŒUÍ•I•ÍÕ±ĞÕÍ•5¥±±•ÍI•…±°¡IÕ¹Ñ¥µ•MÑ…Ñ”ÉÕ¹Ñ¥µ”¥í¥¹Ğ½İ¹•õ¥¹Ù•¹Ñ½Éä¹•Ñ=É•™…Õ±Ğ¡I11}5%11M}%Q5}%°À¤í¥˜¡½İ¹•ğôÀ¥É•ÑÕÉ¸UÍ•I•ÍÕ±Ğ¹%Q5}9=Q}=]9í¥˜¡ÉÕ¹Ñ¥µ”ôõ¹Õ±±ñğ…ÉÕ¹Ñ¥µ”¹Á±…å•È ¤¹…±¥Ù”¥É•ÑÕÉ¸UÍ•I•ÍÕ±Ğ¹9=}Pí¥˜¡½İ¹•ôôÄ¥¥¹Ù•¹Ñ½Éä¹É•µ½Ù”¡I11}5%11M}%Q5}%¤í•±Í”¥¹Ù•¹Ñ½Éä¹ÁÕĞ¡I11}5%11M}%Q5}%±½İ¹•´Ä¤íÉ•ÑÕÉ¸UÍ•I•ÍÕ±Ğ¹UMíô(€ÁÕ‰±¥Œ‰½½±•…¸É…¹ÑÕMµ…±±!ÁA½Ñ¥½¸¡¥¹ĞÅÕ…¹Ñ¥Ñä¥íÉ•ÑÕÉ¸…ÕÑ½1½½ÑI•Í½±Ù•‘%Ñ•´¡Õ}M511}!A}A=Q%=9}%Q5}%±ÅÕ…¹Ñ¥Ñä¤ôõÕÑ½1½½ÑI•ÍÕ±Ğ¹1==Qíô(€ÁÕ‰±¥ŒÕÑ½1½½ÑI•ÍÕ±Ğ…ÕÑ½1½½ÑI•Í½±Ù•‘%Ñ•´¡MÑÉ¥¹œ¥Ñ•µ%±¥¹ĞÅÕ…¹Ñ¥Ñä¥ì(€€€¥˜¡ÅÕ…¹Ñ¥ÑäğôÀ¥É•ÑÕÉ¸ÕÑ½1½½ÑI•ÍÕ±Ğ¹%9Y1%}EU9Q%Qdì(€€€¥˜ …¥Ñ•µÌ¹½¹Ñ…¥¹Í-•ä¡¥Ñ•µ%¤¥É•ÑÕÉ¸ÕÑ½1½½ÑI•ÍÕ±Ğ¹%9Y1%}%Q4ì(€€€¥¹ĞÕÉÉ•¹Ğõ¥¹Ù•¹Ñ½Éä¹½¹Ñ…¥¹Í-•ä¡¥Ñ•µ%¤ı¥¹Ù•¹Ñ½Éä¹•Ğ¡¥Ñ•µ%¤èÀì(€€€¥˜¡ÕÉÉ•¹Ğù%9Y9Q=Ie}MQ-}1%5%PµÅÕ…¹Ñ¥Ñä¥É•ÑÕÉ¸ÕÑ½1½½ÑI•ÍÕ±Ğ¹%9Y9Q=Ie}U10ì(€€€¥¹Ù•¹Ñ½Éä¹ÁÕĞ¡¥Ñ•µ%±ÕÉÉ•¹Ğ­ÅÕ…¹Ñ¥Ñä¤ì(€€€É•ÑÕÉ¸ÕÑ½1½½ÑI•ÍÕ±Ğ¹1==Qì(€ô((€€¼¨¨I•Á±…”Í…Ù•½İ¹•ÉÍ¡¥À•á…Ñ±ä°¥¹±Õ‘¥¹œ…¸•áÁ±¥¥Ñ±ä•µÁÑä•ÅÕ¥Áµ•¹ĞÍ•Ğ¸€¨¼(€ÁÕ‰±¥Œ‰½½±•…¸É•ÍÑ½É•=İ¹•‘%Ñ•µÌ¡5…ÀñMÑÉ¥¹œ±%¹Ñ••Èø½İ¹•±5…ÀñMÑÉ¥¹œ±MÑÉ¥¹œø•ÅÕ¥ÁÁ•¥ì(€€€™½È¡5…À¹¹ÑÉäñMÑÉ¥¹œ±%¹Ñ••Èø”é½İ¹•¹•¹ÑÉåM•Ğ ¤¤(€€€€€¥˜ …¥Ñ•µÌ¹½¹Ñ…¥¹Í-•ä¡”¹•Ñ-•ä ¤¥ññ”¹•ÑY…±Õ” ¤ôõ¹Õ±±ññ”¹•ÑY…±Õ” ¤ğôÁññ”¹•ÑY…±Õ” ¤ù%9Y9Q=Ie}MQ-}1%5%P¥É•ÑÕÉ¸™…±Í”ì(€€€™½È¡5…À¹¹ÑÉäñMÑÉ¥¹œ±MÑÉ¥¹œø”é•ÅÕ¥ÁÁ•¹•¹ÑÉåM•Ğ ¤¥ì(€€€€€%Ñ•µ•™¥¹¥Ñ¥½¸õ¥Ñ•µÌ¹•Ğ¡”¹•ÑY…±Õ” ¤¤ì(€€€€€¥˜¡ôõ¹Õ±±ñğ…¹•ÅÕ¥ÁÁ…‰±” ¥ñğ…”¹•Ñ-•ä ¤¹•ÅÕ…±Ì¡¹•ÅÕ¥ÁM±½Ğ¥ñğ…½İ¹•¹½¹Ñ…¥¹Í-•ä¡¹¥Ñ•µ%¤¥É•ÑÕÉ¸™…±Í”ì(€€€ô(€€€¥¹Ù•¹Ñ½Éä¹±•…È ¤í¥¹Ù•¹Ñ½Éä¹ÁÕÑ±°¡½İ¹•¤ì(€€€•ÅÕ¥Áµ•¹Ñ	åM±½Ğ¹±•…È ¤í•ÅÕ¥Áµ•¹Ñ	åM±½Ğ¹ÁÕÑ±°¡•ÅÕ¥ÁÁ•¤ì(€€€É•ÑÕÉ¸ÑÉÕ”ì(€ô(€ÁÕ‰±¥Œ±½¹œ½¹ÍÕµ•‘½µ‰…ÑM•ÅÕ•¹” ¥íÉ•ÑÕÉ¸±…ÍÑ½µ‰…ÑM•ÅÕ•¹”íô(€ÁÕ‰±¥ŒÙ½¥É•ÍÑ½É•½µ‰…ÑM•ÅÕ•¹”¡±½¹œÍ•ÅÕ•¹”¥í±…ÍÑ½µ‰…ÑM•ÅÕ•¹”õ5…Ñ ¹µ…à Á0±Í•ÅÕ•¹”¤íÉ•İ…É‘!¥ÍÑ½Éä¹±•…È ¤íô((€ÁÕ‰±¥ŒÅÕ¥ÁI•ÍÕ±Ğ•ÅÕ¥À¡MÑÉ¥¹œ¥Ñ•µ%¥ì(€€€%¹Ñ••È½İ¹•õ¥¹Ù•¹Ñ½Éä¹•Ğ¡¥Ñ•µ%¤ì(€€€¥˜¡½İ¹•ôõ¹Õ±±ññ½İ¹•ğôÀ¥É•ÑÕÉ¸ÅÕ¥ÁI•ÍÕ±Ğ¹%Q5}9=Q}=]9ì(€€€%Ñ•µ•™¥¹¥Ñ¥½¸‘•˜õ¥Ñ•µÌ¹•Ğ¡¥Ñ•µ%¤ì(€€€¥˜¡‘•˜ôõ¹Õ±°¥É•ÑÕÉ¸ÅÕ¥ÁI•ÍÕ±Ğ¹U9-9=]9}%Q4ì(€€€¥˜ …‘•˜¹•ÅÕ¥ÁÁ…‰±” ¤¥É•ÑÕÉ¸ÅÕ¥ÁI•ÍÕ±Ğ¹9=Q}EU%AA	1ì(€€€¥˜¡¥Ñ•µ%¹•ÅÕ…±Ì¡•ÅÕ¥Áµ•¹Ñ	åM±½Ğ¹•Ğ¡‘•˜¹•ÅÕ¥ÁM±½Ğ¤¤¥ì(€€€€€•ÅÕ¥Áµ•¹Ñ	åM±½Ğ¹É•µ½Ù”¡‘•˜¹•ÅÕ¥ÁM±½Ğ¤ì(€€€€€É•ÑÕÉ¸ÅÕ¥ÁI•ÍÕ±Ğ¹U9EU%AAì(€€€ô(€€€I•ÅÕ¥É•µ•¹ÑI•ÍÕ±ĞÉ•ÅÕ¥É•µ•¹ÑÌõÕÉÉ•¹ÑI•ÅÕ¥É•µ•¹ÑÌ¡¥Ñ•µ%¤ì(€€€¥˜¡É•ÅÕ¥É•µ•¹ÑÌôõI•ÅÕ¥É•µ•¹ÑI•ÍÕ±Ğ¹A9%9¥É•ÑÕÉ¸ÅÕ¥ÁI•ÍÕ±Ğ¹IEU%I59Q}A9%9ì(€€€¥˜¡É•ÅÕ¥É•µ•¹ÑÌ„õI•ÅÕ¥É•µ•¹ÑI•ÍÕ±Ğ¹5P¥É•ÑÕÉ¸ÅÕ¥ÁI•ÍÕ±Ğ¹IEU%I59Q}9=Q}5Pì(€€€€¼¼ÅÕ¥Áµ•¹ĞÍ±½ÑÌÍÑ…äÍ½ÕÉ”µ™…¥¹œ€£ªÂG²bÜ¿ªÂ®Â`¤¸Y¥ÍÕ…°½Ù•É…”¥Ì„Í•Á…É…Ñ”Á…Á•Èµ‘½±°½¹•É¸è(€€€€¼¼U11}	=d½ÕÁ¥•Ì‰½Ñ UAAH…¹1=]H½Ù•É…”ìUAAH€¬1=]Hµ…ä½•á¥ÍĞ¸(€€€¡…É…Ñ•ÉY¥ÍÕ…±	¥¹‘¥¹œ¹…Éµ•¹Ñ½Ù•É…”¥¹½µ¥¹œô(€€€€€€€¡…É…Ñ•ÉY¥ÍÕ…±	¥¹‘¥¹œ¹…Éµ•¹Ñ½Ù•É…•½ÉÁÁ•…É…¹”¡‘•˜¹…ÁÁ•…É…¹•%¤ì(€€€¥˜¡¥¹½µ¥¹œôõ¡…É…Ñ•ÉY¥ÍÕ…±	¥¹‘¥¹œ¹…Éµ•¹Ñ½Ù•É…”¹U11}	=d¥ì(€€€€€É•µ½Ù•ÅÕ¥ÁÁ•‘½Ù•É…”¡¡…É…Ñ•ÉY¥ÍÕ…±	¥¹‘¥¹œ¹…Éµ•¹Ñ½Ù•É…”¹UAAH¤ì(€€€€€É•µ½Ù•ÅÕ¥ÁÁ•‘½Ù•É…”¡¡…É…Ñ•ÉY¥ÍÕ…±	¥¹‘¥¹œ¹…Éµ•¹Ñ½Ù•É…”¹1=]H¤ì(€€€õ•±Í”¥˜¡¥¹½µ¥¹œôõ¡…É…Ñ•ÉY¥ÍÕ…±	¥¹‘¥¹œ¹…Éµ•¹Ñ½Ù•É…”¹UAAH(€€€€€€€ññ¥¹½µ¥¹œôõ¡…É…Ñ•ÉY¥ÍÕ…±	¥¹‘¥¹œ¹…Éµ•¹Ñ½Ù•É…”¹1=]H¥ì(€€€€€É•µ½Ù•ÅÕ¥ÁÁ•‘½Ù•É…”¡¡…É…Ñ•ÉY¥ÍÕ…±	¥¹‘¥¹œ¹…Éµ•¹Ñ½Ù•É…”¹U11}	=d¤ì(€€€ô(€€€•ÅÕ¥Áµ•¹Ñ	åM±½Ğ¹ÁÕĞ¡‘•˜¹•ÅÕ¥ÁM±½Ğ±¥Ñ•µ%¤ì(€€€É•ÑÕÉ¸ÅÕ¥ÁI•ÍÕ±Ğ¹EU%AAì(€ô((€ÁÉ¥Ù…Ñ”Ù½¥É•µ½Ù•ÅÕ¥ÁÁ•‘½Ù•É…”¡¡…É…Ñ•ÉY¥ÍÕ…±	¥¹‘¥¹œ¹…Éµ•¹Ñ½Ù•É…”½Ù•É…”¥ì(€€€¥˜¡½Ù•É…”ôõ¹Õ±°¥É•ÑÕÉ¸ì(€€€1¥ÍĞñMÑÉ¥¹œøÍ±½ÑÌõ¹•ÜÉÉ…å1¥ÍĞğø ¤ì(€€€™½È¡5…À¹¹ÑÉäñMÑÉ¥¹œ±MÑÉ¥¹œø”é•ÅÕ¥Áµ•¹Ñ	åM±½Ğ¹•¹ÑÉåM•Ğ ¤¥ì(€€€€€%Ñ•µ•™¥¹¥Ñ¥½¸•ÅÕ¥ÁÁ•õ¥Ñ•µÌ¹•Ğ¡”¹•ÑY…±Õ” ¤¤ì(€€€€€¥˜¡•ÅÕ¥ÁÁ•ôõ¹Õ±°¥½¹Ñ¥¹Õ”ì(€€€€€¥˜¡¡…É…Ñ•ÉY¥ÍÕ…±	¥¹‘¥¹œ¹…Éµ•¹Ñ½Ù•É…•½ÉÁÁ•…É…¹”¡•ÅÕ¥ÁÁ•¹…ÁÁ•…É…¹•%¤ôõ½Ù•É…”¥Í±½ÑÌ¹…‘¡”¹•Ñ-•ä ¤¤ì(€€€ô(€€€™½È¡MÑÉ¥¹œÍ±½ĞéÍ±½ÑÌ¥•ÅÕ¥Áµ•¹Ñ	åM±½Ğ¹É•µ½Ù”¡Í±½Ğ¤ì(€ô((€ÁÕ‰±¥ŒMÑ…ÑM¹…ÁÍ¡½ĞÉ•½µÁÕÑ•MÑ…ÑÌ ¥ì(€€€5…ÀñMÑÉ¥¹œ±%¹Ñ••Èø•ÅÕ¥Àõ¹•Ü1¥¹­•‘!…Í¡5…Àğø ¤ì(€€€™½È¡MÑÉ¥¹œ¥Ñ•µ%é•ÅÕ¥Áµ•¹Ñ	åM±½Ğ¹Ù…±Õ•Ì ¤¥ì(€€€€€%Ñ•µ•™¥¹¥Ñ¥½¸‘•˜õ¥Ñ•µÌ¹•Ğ¡¥Ñ•µ%¤í¥˜¡‘•˜ôõ¹Õ±°¥½¹Ñ¥¹Õ”ì(€€€€€™½È¡5…À¹¹ÑÉäñMÑÉ¥¹œ±%¹Ñ••Èø´é‘•˜¹ÍÑ…Ñ5½‘¥™¥•ÉÌ¹•¹ÑÉåM•Ğ ¤¥•ÅÕ¥À¹ÁÕĞ¡´¹•Ñ-•ä ¤±Ù…±Õ”¡•ÅÕ¥À±´¹•Ñ-•ä ¤¤­´¹•ÑY…±Õ” ¤¤ì(€€€ô(€€€5…ÀñMÑÉ¥¹œ±%¹Ñ••ÈøÑ½Ñ…°õ¹•Ü1¥¹­•‘!…Í¡5…Àğø¡‰…Í•MÑ…ÑÌ¤ì(€€€™½È¡5…À¹¹ÑÉäñMÑÉ¥¹œ±%¹Ñ••Èø´é•ÅÕ¥À¹•¹ÑÉåM•Ğ ¤¥Ñ½Ñ…°¹ÁÕĞ¡´¹•Ñ-•ä ¤±Ù…±Õ”¡Ñ½Ñ…°±´¹•Ñ-•ä ¤¤­´¹•ÑY…±Õ” ¤¤ì(€€€É•ÑÕÉ¸¹•ÜMÑ…ÑM¹…ÁÍ¡½Ğ¡‰…Í•MÑ…ÑÌ±•ÅÕ¥À±Ñ½Ñ…°¤ì(€ô((€ÁÉ¥Ù…Ñ”Ù½¥ÑÉ¥µI•İ…É‘!¥ÍÑ½Éä ¥íİ¡¥±”¡É•İ…É‘!¥ÍÑ½Éä¹Í¥é” ¤øĞà¥É•İ…É‘!¥ÍÑ½Éä¹É•µ½Ù” À¤íô(€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥Œ¥¹ĞÙ…±Õ”¡5…ÀñMÑÉ¥¹œ±%¹Ñ••Èøµ…À±MÑÉ¥¹œ­•ä¥í%¹Ñ••ÈØõµ…À¹•Ğ¡­•ä¤íÉ•ÑÕÉ¸Øôõ¹Õ±°üÀéØíô)ô(