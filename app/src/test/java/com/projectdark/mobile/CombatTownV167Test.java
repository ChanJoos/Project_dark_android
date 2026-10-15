package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import com.projectdark.mobile.world.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CombatTownV167Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String p:new String[]{"project_dark_f5m_v1","project_dark_skill_test_v1","project_dark_skill_book_v1"})c.getSharedPreferences(p,0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 RuntimeState state(){RuntimeState s=new RuntimeState();s.rpg().enableEquipmentSandbox(true);return s;}
 int hit(RuntimeState s){RuntimeState.Monster m=s.monsters().get(0);m.hp=100000;m.alive=true;new RuntimeCombatPortAdapter(s,(a,b)->true,(a,b)->true).applyDamage("player",m.id,"attack_proto_swing",8);return 100000-m.hp;}
 @Test public void sourceEquipmentAndStrengthReachActualHpAndRestore()throws Exception{
  RuntimeState s=state();RpgProgressionState r=s.rpg();r.unequip("무기");r.equip("IT_TEST_WEAPON_MW002");int weak=hit(s);r.unequip("무기");r.equip("IT_WARDROBE_MW023");int strong=hit(s);assertTrue(strong>weak+100);
  r.restoreStats(50,3,3,3,3,0);assertTrue(hit(s)>strong);int before=hit(s);F5mSaveStore.saveRewardsActive(r);F5mSaveStore.install(c);RuntimeState restored=state();F5mSaveStore.restoreRewardsActive(restored.rpg());assertEquals(before,hit(restored));
 }
 @Test public void conWisAllocationChangesOnlyFutureLevelGainsAndSurvivesSave(){
  RuntimeState low=state(),high=state();for(RuntimeState s:Arrays.asList(low,high)){s.rpg().restoreProgression(30,0);s.rpg().restoreBaseResources(1000,1000);s.applyDerivedGrowth();}
  high.rpg().restoreStats(3,3,30,30,3,0);assertEquals(1000,high.rpg().baseMaxHp());assertEquals(1000,high.rpg().baseMaxMp());
  int hp=high.rpg().nextHpGain(),mp=high.rpg().nextMpGain();long exp=LevelExpCurve.requiredForNext(30);
  for(RuntimeState s:Arrays.asList(low,high)){assertEquals(1,s.rpg().grantAdaptedReward(exp,0));s.applyDerivedGrowth();}
  assertEquals(1000+hp,high.rpg().baseMaxHp());assertEquals(1000+mp,high.rpg().baseMaxMp());assertTrue(high.player().maxHp>low.player().maxHp);assertTrue(high.player().maxMp>low.player().maxMp);
  F5mSaveStore.saveRewardsActive(high.rpg());F5mSaveStore.install(c);RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);assertEquals(high.rpg().baseMaxHp(),restored.baseMaxHp());assertEquals(high.rpg().baseMaxMp(),restored.baseMaxMp());
 }
 @Test public void intAndDexScaleTheirActualSkillFamilies(){
  RuntimeState s=state();FinalStats low=s.rpg().finalStats();s.rpg().restoreStats(3,80,3,3,80,0);FinalStats high=s.rpg().finalStats();
  for(String id:new String[]{"SK_마법사_001","SK_도적_001"}){SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);assertTrue(id,SkillAbilityCatalog.amount(a,new SkillAbilityCatalog.Snapshot(high,100,100,30,100,false))>SkillAbilityCatalog.amount(a,new SkillAbilityCatalog.Snapshot(low,100,100,30,100,false)));}
 }
 @Test public void twoHandedOccupancyAndAllFourSourceFacingsAreExplicit()throws Exception{
  RuntimeState s=state();RpgProgressionState r=s.rpg();r.equip("IT_ADAPTED_STARTER_SHIELD");r.equip("IT_WARDROBE_MW016");assertNull(r.equippedDefinition("방패"));assertEquals(WeaponCombatProfile.Grip.TWO_HAND,WeaponCombatProfile.grip(r.equippedDefinition("무기")));assertEquals("TWO_HAND",WeaponCombatProfile.motion(r,"SWING"));
  r.equip("IT_ADAPTED_STARTER_SHIELD");assertNull(r.equippedDefinition("무기"));
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(String body:new String[]{"mm001","wm001"})assertEquals(body+"/02/"+CharacterRenderer.actionSourceIndex(d),catalog.frameKey(body,"SWING",d,.5f));
 }
 @Test public void daraCannotSilentlyPreviewZeroAndRealResourcesDamageExactlyOnce()throws Exception{
  SkillAbilityRuntimeTest h=new SkillAbilityRuntimeTest();h.context=c;RuntimeState s=h.state();RuntimeCombatSession session=h.session(s);session.setSkillTestMode(true);s.player().mp=0;
  CombatActionOrchestrator.Submission rejected=session.submitPlayer("m0","SK_무도가_020");assertFalse(rejected.accepted());assertEquals(CombatResolver.RejectReason.RESOURCE,rejected.rejectReason);assertEquals(1000000,s.monsters().get(0).hp);
  s.player().mp=7000;assertTrue(session.submitPlayer("m0","SK_무도가_020").accepted());List<CombatResolver.Event> e=session.tick(session.effectiveCastSeconds("SK_무도가_020")+.01f).events;assertTrue(e.stream().anyMatch(x->x.type==CombatResolver.EventType.HIT_FEEDBACK&&x.amount>0));int hp=s.monsters().get(0).hp;session.tick(3);assertEquals(hp,s.monsters().get(0).hp);
 }
 @Test public void eachTownShowsOnlyItsLocalHuntAndArrivalIsNearManager()throws Exception{
  for(TownTravel.Destination town:TownTravel.TOWNS){String hunt=TownTravelWindow.hunting(town.map);assertTrue(TownTravelWindow.destinations(town.map).contains(hunt));if(!WorldDef.ID.equals(town.map))assertFalse(TownTravelWindow.destinations(town.map).contains(TownTravelWindow.FOREST));if(WorldDef.ID.equals(town.map))continue;
   WorldMoveTargetController.TileCenter a=CampaignWorld.arrival(town.map,false),b=TownTravel.center(town.map);assertTrue(town.map,Math.hypot(a.x-b.x,a.y-b.y)<120);
   GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);TownQualityV164Test h=new TownQualityV164Test();h.c=c;h.travel(v,town.map);h.capture(v,"v167-arrival-"+town.map);
  }
 }
 @Test public void coastEffectCentresUseThePrewarmedActualSpriteBody()throws Exception{
  RuntimeState s=state();s.enterCampaignMap(RucesionCoastGeometry.ROUTE[1],false);PoteFieldRenderer renderer=new PoteFieldRenderer();renderer.prepareMonsters(s);float[] center=new float[2];int n=0;
  for(RuntimeState.Monster m:s.monsters()){assertTrue(m.id,renderer.monsterCenter(m,center));assertTrue(Float.isFinite(center[0]));assertTrue(center[1]<m.y+10);assertTrue(center[1]>m.y-PoteForestMonsterShowcase.bodyHeight(m.id));n++;}assertTrue(n>0);
 }
 @Test public void sourceWeaponMotionsHaveNativeFourFacingMaleFemaleEvidence()throws Exception{
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);SkillBodyRenderer renderer=new SkillBodyRenderer(c,catalog);Bitmap sheet=Bitmap.createBitmap(960,600,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);canvas.drawColor(0xffd2c8b5);Paint text=new Paint();text.setColor(Color.BLACK);text.setTextSize(13);
  int row=0;for(String body:new String[]{"mm001","wm001"})for(String item:new String[]{"IT_TEST_WEAPON_MW002","IT_WARDROBE_MW016"}){
   RuntimeState state=state();state.rpg().unequip("무기");state.rpg().equip(item);CharacterVisualBinding gear=CharacterVisualBinding.from(state.rpg());String motion=WeaponCombatProfile.motion(state.rpg(),"SWING");int col=0;
   for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
    float x=90+col++*235,y=120+row*145;canvas.drawText(body+" "+motion+" "+d,x-65,y+20,text);
    CharacterRenderer.Pose pose=new CharacterRenderer.Pose(x,y,d,CharacterRenderer.State.ATTACK,0,.18f,.5f,false,gear.equipmentVisualRef(),gear.weaponVisualRef(),CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE,AnimationAction.SWING);
    assertTrue(body+" "+motion+" "+d,renderer.draw(canvas,pose,body,motion,.4f));
   }row++;
  }
  File dir=new File("build/reports/combat-town-v167");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,"weapon-motions-four-facing.png"))){assertTrue(sheet.compress(Bitmap.CompressFormat.PNG,100,out));}sheet.recycle();
 }
 @Test public void exactAndPaletteArmourRequirementsReachNormalEquipWithoutDestroyingSourceOptions(){
  RpgProgressionState r=new RpgProgressionState();String exact="IT_WARDROBE_MU0000013",variant="IT_WARDROBE_MU0000060";
  assertEquals(RpgProgressionState.RequirementResult.JOB_NOT_MET,r.evaluateRequirements(exact,"WARRIOR",99));
  assertEquals(RpgProgressionState.RequirementResult.MET,r.evaluateRequirements(exact,"MARTIAL_ARTIST",99));
  assertEquals(RpgProgressionState.RequirementResult.MET,r.evaluateRequirements(variant,"MARTIAL_ARTIST",99));
  assertEquals(Integer.valueOf(-19),r.itemDefinitions().get(variant).statModifiers.get("AC"));assertEquals(RpgProgressionState.Evidence.ADAPTED,r.itemDefinitions().get(variant).evidence);
 }
 @Test public void capturedFinisherSourceUnionIsCentredOnRecipientPixels(){
  CapturedSkillFx fx=new CapturedSkillFx(c);Paint paint=new Paint();
  for(String id:new String[]{"SK_전사_013","SK_도적_020","SK_무도가_020"}){
   CapturedSkillFx.Sequence sequence=fx.get(id);assertNotNull(id,sequence);Bitmap image=Bitmap.createBitmap(1200,1200,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);float age=0;
   for(int duration:sequence.durations){fx.drawCentered(canvas,paint,id,age+.001f,600,600);age+=duration/1000f;}
   int l=1200,t=1200,r=0,b=0;int[] pixels=new int[1200*1200];image.getPixels(pixels,0,1200,0,0,1200,1200);
   for(int y=0;y<1200;y++)for(int x=0;x<1200;x++)if(Color.alpha(pixels[y*1200+x])>24){l=Math.min(l,x);r=Math.max(r,x+1);t=Math.min(t,y);b=Math.max(b,y+1);}
   assertTrue(id,r>l);assertEquals(id,600,(l+r)*.5f,5f);assertEquals(id,600,(t+b)*.5f,5f);image.recycle();
  }
 }
 @Test public void identicalSourceWeaponsShareCombatAndBodyMotionAcrossItemIds(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);EquipmentActionResolver resolver=new EquipmentActionResolver();
  for(String id:new String[]{"IT_TEST_WEAPON_MW002","IT_B_JOB_ROGUE_WEAPON","IT_B_CAMPAIGN_TOOL_ROGUE_41","IT_B_JOB_MAGE_WAND","IT_B_JOB_CLERIC_WAND"}){
   r.unequip("무기");assertEquals(id,RpgProgressionState.EquipResult.EQUIPPED,r.equip(id));assertEquals(id,AnimationAction.THRUST,resolver.resolveBasicAttack(r).animationAction);assertEquals(id,"THRUST",WeaponCombatProfile.motion(r,"SWING"));
  }
 }
 @Test public void inventoryAuditRecordsPendingPerformanceRatherThanPassingEmptyOptions()throws Exception{
  RpgProgressionState r=new RpgProgressionState();JSONArray rows=new JSONArray();for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values())if(d.equippable())rows.put(new JSONObject().put("id",d.itemId).put("name",d.name).put("slot",d.equipSlot).put("stats",new JSONObject(d.statModifiers)).put("jobRestrictionResolved",d.jobRestrictionResolved).put("grip",WeaponCombatProfile.grip(d).name()).put("evidence",d.evidence.name()).put("performancePending",d.statModifiers.isEmpty()));
  File dir=new File("build/reports/combat-town-v167");dir.mkdirs();java.nio.file.Files.write(new File(dir,"all-equipment-audit.json").toPath(),rows.toString(2).getBytes("UTF-8"));assertTrue(rows.length()>100);
 }
}

