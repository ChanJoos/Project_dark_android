package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import com.projectdark.mobile.world.WorldRuntimeAdapter;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MartialMechanicsV173Test {
 Context context;SkillAbilityRuntimeTest fixtures=new SkillAbilityRuntimeTest();
 @Before public void setup(){context=RuntimeEnvironment.getApplication();fixtures.before();context.getSharedPreferences("project_dark_skill_test_v1",0).edit().clear().commit();UiTheme.install(context);}
 static <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return (T)f.get(o);}
 static Object call(Object o,String n,Class<?>[] types,Object... a)throws Exception{Method m=o.getClass().getDeclaredMethod(n,types);m.setAccessible(true);return m.invoke(o,a);}
 RuntimeState state()throws Exception{return fixtures.state();}
 List<RuntimeState.Monster> monsters(RuntimeState s)throws Exception{return field(s,"monsters");}
 RuntimeState.Monster add(RuntimeState s,String id,float x,float y)throws Exception{RuntimeState.Monster m=new RuntimeState.Monster(id,"V173 fixture",x,y,100000,"ADAPTED_TEST");monsters(s).add(m);return m;}
 void movement(RuntimeCombatSession c,RuntimeState s){c.setSkillMovement(new SkillAbilityExecutor.Movement(){public boolean movePlayer(float x,float y){s.player().x=x;s.player().y=y;return true;}public boolean moveMonster(RuntimeState.Monster m,float x,float y){return false;}public boolean canLeapPlayer(float x,float y){return s.canPlayerOccupyActors(x,y);}public boolean leapPlayer(float x,float y){return canLeapPlayer(x,y)&&movePlayer(x,y);}});}
 RuntimeCombatSession session(RuntimeState s){RuntimeCombatSession c=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);movement(c,s);return c;}
 @Test public void all34MartialSkillsHaveLiveEffectsInNormalAndTestModes()throws Exception{
  List<String> audit=new ArrayList<>();for(boolean test:new boolean[]{false,true})for(SkillAbilityCatalog.Ability a:SkillAbilityCatalog.all())if(a.job.equals("무도가")){
   RuntimeState s=state();RuntimeCombatSession c=session(s);c.setSkillTestMode(test);SkillActionContract.Rule r=SkillActionContract.get(a.id);assertTrue(a.id,a.supported());
   if(a.kind.equals("PASSIVE")){SkillBook b=SkillBook.load(context);b.bindJob(()->"MARTIAL_ARTIST");b.setTestAccess(test);b.learn(a.id,0);assertTrue(a.id,b.basicHits()>=r.hits);c.setBasicHits(b::basicHits);assertTrue(c.submitPlayerBasicAttack("m0").combat.accepted());assertEquals(b.basicHits(),c.tick(.7f).events.stream().filter(e->e.type==CombatResolver.EventType.EFFECT_APPLIED).count());audit.add(a.id+" linked "+r.hits);continue;}
   if(a.status.startsWith("CLEAR_"))s.skillEffects().put("player","SK_마법사_025",a.status.substring(6),10,8);
   if(a.status.equals("TRAVEL")){assertTrue(c.useUtility(a.id));assertEquals("TRAVEL_MARTIAL",c.takeSkillNotice());audit.add(a.id+" return-service test="+test);continue;}
   if(a.status.equals("DRAIN")){s.monsters().get(0).hp=10;s.player().hp=1000;}
   if(a.heal())s.player().hp=1000;
   int hp=s.player().hp,mp=s.player().mp,mhp=s.monsters().get(0).hp;
   assertTrue(a.id,c.submitPlayer(r.selfAnchored()?"player":"m0",a.id).accepted());List<CombatResolver.Event> events=c.tick(c.effectiveCastSeconds(a.id)+.001f).events;
   assertTrue(a.id+" dispatched",events.stream().anyMatch(e->e.type==CombatResolver.EventType.EFFECT_APPLIED));
   if(a.damage())assertTrue(a.id+" damages",s.monsters().get(0).hp<mhp);
   if(a.status.equals("DRAIN"))assertEquals(hp+mhp-s.monsters().get(0).hp,s.player().hp);
   if(a.heal()){assertTrue(s.player().hp>hp);assertTrue(a.id+" heals",events.stream().anyMatch(e->e.hitSemantic==CombatResolver.HitSemantic.HEAL));}
   if(a.kind.equals("STATUS"))switch(a.status){
    case "LEAP":assertEquals(64,s.player().x,0);assertEquals(32,s.player().y,0);break;
    case "CLEAR_BLIND":assertFalse(s.skillEffects().has("player","BLIND"));break;
    case "CLEAR_POISON":assertFalse(s.skillEffects().has("player","POISON"));break;
    case "ROOT":assertTrue(s.skillEffects().rooted("m0"));assertFalse(s.skillEffects().disabled("m0"));break;
    default:String kind=a.status.startsWith("ARMOR")?"ARMOR":a.status;assertTrue(a.id,s.skillEffects().has("player",kind));
   }
   if(a.formula.equals("DARA")&&!test){assertEquals(1,s.player().hp);assertEquals(1,s.player().mp);}
   if(test&&!a.formula.equals("DARA")){if(!a.heal()&&!a.status.equals("DRAIN"))assertEquals(a.id+" preview HP",hp,s.player().hp);assertEquals(a.id+" preview MP",mp,s.player().mp);}
   audit.add(a.id+" "+a.name+" "+a.kind+" "+a.status+" test="+test);
  }assertEquals(68,audit.size());File file=new File("build/reports/martial-v173/all-34-effects.txt");file.getParentFile().mkdirs();try(java.io.PrintWriter out=new java.io.PrintWriter(file)){audit.forEach(out::println);}
 }
 @Test public void facingLeapCrossesMonsterOrNpcAndRejectsAbsentWrongSideAndBlockedLandings()throws Exception{
  for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(boolean npc:new boolean[]{false,true}){
   RuntimeState s=state();monsters(s).clear();List<RuntimeState.Npc> ns=field(s,"npcs");ns.clear();float[] v=MartialSkillPolicy.step(d);
   if(npc)ns.add(new RuntimeState.Npc("fixture_npc","NPC",v[0],v[1],"","B"));else add(s,"front",v[0],v[1]);
   RuntimeCombatSession c=session(s);c.setPlayerFacing(()->d);assertTrue(c.submitFacingMartial(MartialSkillPolicy.LEAP).accepted());c.tick(.15f);assertEquals(v[0]*2,s.player().x,0);assertEquals(v[1]*2,s.player().y,0);
   s=state();monsters(s).clear();c=session(s);c.setPlayerFacing(()->d);add(s,"wrong",-v[0],-v[1]);int mp=s.player().mp;assertFalse(c.submitFacingMartial(MartialSkillPolicy.LEAP).accepted());assertEquals(mp,s.player().mp);assertEquals(0,c.cooldownRemaining("player",MartialSkillPolicy.LEAP),0);
  }
  Set<String> endpoints=new HashSet<>();for(double random:new double[]{0,.99}){
   RuntimeState s=state();monsters(s).clear();add(s,"front",32,16);add(s,"blocked",64,32);RuntimeCombatSession c=session(s);c.setPlayerFacing(()->CharacterRenderer.Direction.SE);((RuntimeCombatPortAdapter)field(c,"port")).abilities.random=()->random;
   assertTrue(c.submitFacingMartial(MartialSkillPolicy.LEAP).accepted());c.tick(.15f);endpoints.add(s.player().x+","+s.player().y);assertFalse(MartialSkillPolicy.same(s.player().x,s.player().y,64,32));
  }assertEquals(new HashSet<>(Arrays.asList("0.0,32.0","64.0,0.0")),endpoints);
  RuntimeState s=state();monsters(s).clear();add(s,"front",32,16);add(s,"back",64,32);add(s,"left",64,0);add(s,"right",0,32);RuntimeCombatSession c=session(s);assertFalse(c.submitFacingMartial(MartialSkillPolicy.LEAP).accepted());assertEquals(0,s.player().x,0);
 }
 @Test public void freezeStopsMovementAndPendingAttacksButRootStillAllowsAdjacentAttackForExactlyEightSeconds()throws Exception{
  for(String id:new String[]{"SK_무도가_018","SK_무도가_010"}){
   RuntimeState s=state();RuntimeState.Monster m=s.monsters().get(0);RuntimeCombatSession c=session(s);m.isMoving=true;m.moveStartX=m.x;m.moveStartY=m.y;m.moveTargetX=m.x+32;m.moveTargetY=m.y+16;m.moveDuration=.6f;float beforeX=m.x,beforeY=m.y;assertTrue(c.submitPlayer(m.id,id).accepted());c.tick(.15f);assertFalse(m.isMoving);boolean freeze=id.endsWith("018");assertEquals(freeze,s.skillEffects().disabled(m.id));assertTrue(s.skillEffects().rooted(m.id));assertEquals(8,s.skillEffects().get(m.id,freeze?"FREEZE":"ROOT").remaining,0);
   assertFalse(s.tryMoveMonster(m,32,16,36));assertEquals(!freeze,c.monsterAutoBridge().submit(m.id,"player").outcome==MonsterAutoCombatBridge.Outcome.ACCEPTED);
   s.tick(7.99f);assertEquals(beforeX,m.x,0);assertEquals(beforeY,m.y,0);assertTrue(s.skillEffects().rooted(m.id));s.tick(.011f);assertFalse(s.skillEffects().rooted(m.id));
  }
  RuntimeState s=state();RuntimeState.Monster m=s.monsters().get(0);RuntimeCombatSession c=session(s);assertTrue(c.monsterAutoBridge().submit(m.id,"player").outcome==MonsterAutoCombatBridge.Outcome.ACCEPTED);assertTrue(c.submitPlayer(m.id,"SK_무도가_018").accepted());int hp=s.player().hp;c.tick(.15f);c.tick(.3f);assertEquals(hp,s.player().hp);
 }
 @Test public void lineHitsAllThreeCellsTogetherOnlyFacingAndEmitsOneCleanContactPerVictim()throws Exception{
  for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   RuntimeState s=state();monsters(s).clear();float[] v=MartialSkillPolicy.step(d);for(int n=1;n<=4;n++)add(s,"line"+n,v[0]*n,v[1]*n);add(s,"off",-v[0],-v[1]);RuntimeCombatSession c=session(s);c.setPlayerFacing(()->d);assertTrue(c.submitFacingMartial(MartialSkillPolicy.LINE).accepted());assertTrue(c.tick(.139f).events.stream().noneMatch(e->e.type==CombatResolver.EventType.HIT_FEEDBACK));List<CombatResolver.Event> events=c.tick(.002f).events;
   Set<String> hits=new HashSet<>();Set<Long> seq=new HashSet<>();for(CombatResolver.Event e:events)if(e.type==CombatResolver.EventType.HIT_FEEDBACK){hits.add(e.targetId);seq.add(e.actionSequence);}assertEquals(new HashSet<>(Arrays.asList("line1","line2","line3")),hits);assertEquals(1,seq.size());
   SkillVfxRenderer fx=new SkillVfxRenderer(context,new SkillPresentationCatalog(context));SkillVfxRenderer.Anchors anchors=new SkillVfxRenderer.Anchors(){public float x(String id){float[] p=MartialSkillPolicy.position(s,id);return p==null?0:300+p[0];}public float y(String id){float[] p=MartialSkillPolicy.position(s,id);return p==null?0:240+p[1];}};fx.consume(events,anchors);assertEquals(3,fx.pulses.size());for(SkillVfxRenderer.Pulse p:fx.pulses){assertEquals("martial-line",p.sheet);assertEquals(0,p.age,0);}
   Bitmap b=Bitmap.createBitmap(600,480,Bitmap.Config.ARGB_8888);fx.draw(new Canvas(b),anchors);int visible=0;for(int y=0;y<480;y++)for(int x=0;x<600;x++){int z=b.getPixel(x,y);if(Color.alpha(z)>0){visible++;assertTrue(Color.green(z)>=100&&Color.blue(z)>=100&&Color.red(z)<Math.min(Color.green(z),Color.blue(z))*.7f);}}assertTrue(visible>100);save(b,"line-"+d);b.recycle();
   monsters(s).clear();RuntimeCombatSession empty=session(s);empty.setPlayerFacing(()->d);assertFalse(empty.submitFacingMartial(MartialSkillPolicy.LINE).accepted());assertEquals(0,empty.cooldownRemaining("player",MartialSkillPolicy.LINE),0);
  }
 }
 @Test public void reflectionRedirectsEqualPhysicalAndMagicDamageAndInvincibilityBlocksDamageAndPoison()throws Exception{
  for(String attack:new String[]{RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,"cast_proto"}){
   RuntimeState s=state();RuntimeCombatSession c=session(s);RuntimeCombatPortAdapter p=field(c,"port");int hp=s.player().hp;int expected=p.applyDamage("m0","player",attack,100).appliedAmount;assertTrue(expected>0);s.player().hp=hp;
   assertTrue(c.submitPlayer("player","SK_무도가_022").accepted());c.tick(c.effectiveCastSeconds("SK_무도가_022")+.01f);int monsterHp=s.monsters().get(0).hp;p.applyDamage("m0","player",attack,100);assertEquals(hp,s.player().hp);assertEquals(monsterHp-expected,s.monsters().get(0).hp);
   SkillEffectState restored=new SkillEffectState();assertTrue(restored.restore(s.skillEffects().snapshot()));assertTrue(restored.has("player","REFLECT_ALL"));
   s.skillEffects().put("player","SK_무도가_016","INVINCIBLE",1,9);monsterHp=s.monsters().get(0).hp;p.applyDamage("m0","player",attack,100);assertEquals(monsterHp-expected,s.monsters().get(0).hp);assertEquals(hp,s.player().hp);
   s.skillEffects().remove("player","REFLECT_ALL");p.applyDamage("m0","player",attack,100);assertEquals(hp,s.player().hp);s.skillEffects().put("player","MONSTER_VENOM","POISON",20,2);s.tick(1);assertEquals(hp,s.player().hp);
   s.tick(8.01f);p.applyDamage("m0","player",attack,100);assertTrue(s.player().hp<hp);
  }
 }
 @Test public void martialBuffsChangeDefenseEvasionBasicDamageRegenerationAndSpeed()throws Exception{
  for(String id:new String[]{"SK_무도가_004","SK_무도가_008","SK_무도가_012","SK_무도가_013","SK_무도가_019","SK_무도가_029","SK_무도가_033"}){
   RuntimeState s=state();RuntimeCombatSession c=session(s);RuntimeCombatPortAdapter p=field(c,"port");p.abilities.random=()->0;
   int hp=s.player().hp,ac=s.rpg().finalStats().withEffects(s.skillEffects(),"player").ac;
   int baseline=p.applyDamage("m0","player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,1000).appliedAmount;s.player().hp=hp;
   int monsterHp=s.monsters().get(0).hp;p.applyDamage("player","m0",RuntimeCombatSession.playerAttackActionId(0),100);int baseHit=monsterHp-s.monsters().get(0).hp;
   assertTrue(id,c.submitPlayer("player",id).accepted());c.tick(c.effectiveCastSeconds(id)+.01f);
   if(id.endsWith("004"))assertEquals(0,p.applyDamage("m0","player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,1000).appliedAmount);
   if(id.endsWith("013")){assertEquals(ac-10,s.rpg().finalStats().withEffects(s.skillEffects(),"player").ac);assertTrue(p.applyDamage("m0","player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,1000).appliedAmount<baseline);}
   if(id.endsWith("029"))assertTrue(p.applyDamage("m0","player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,1000).appliedAmount<baseline);
   if(id.endsWith("019")){monsterHp=s.monsters().get(0).hp;p.applyDamage("player","m0",RuntimeCombatSession.playerAttackActionId(0),100);assertEquals(baseHit*2,monsterHp-s.monsters().get(0).hp);}
   if(id.endsWith("012")){s.player().hp=1000;s.tick(1);assertTrue(s.player().hp>1000);}
   if(id.endsWith("008")||id.endsWith("033"))assertEquals(1.2f,s.skillEffects().speed(),0);
  }
 }
 @Test public void leapValidatesActorLandingsEvenInActorOverlapRoomsAndNeverCrossesTerrain()throws Exception{
  RuntimeState s=state();monsters(s).clear();((List<?>)field(s,"npcs")).clear();add(s,"front",32,16);
  java.util.List<com.projectdark.mobile.world.WorldMoveTargetController.TileCenter> tiles=java.util.Arrays.asList(new com.projectdark.mobile.world.WorldMoveTargetController.TileCenter(0,0));
  WorldRuntimeAdapter w=new WorldRuntimeAdapter(s,960,540,-1000,1000,-1000,1000,tiles,java.util.Collections.emptyList(),false);
  assertTrue(w.canPlayerLeapTo(64,32));RuntimeState.Monster behind=add(s,"behind",64,32);assertFalse(w.canPlayerLeapTo(64,32));monsters(s).remove(behind);List<RuntimeState.Npc> ns=field(s,"npcs");ns.add(new RuntimeState.Npc("landing_npc","NPC",64,32,"","B"));assertFalse(w.canPlayerLeapTo(64,32));ns.clear();assertTrue(w.canPlayerLeapTo(64,32));
  WorldRuntimeAdapter blocked=new WorldRuntimeAdapter(s,960,540,-1000,1000,-1000,1000,tiles,java.util.Arrays.asList(new RectF(25,10,38,22)),false);assertFalse(blocked.canPlayerLeapTo(64,32));
  SkillEffectState old=new SkillEffectState();old.put("player","SK_무도가_022","REFLECT_TECH",1,10);SkillEffectState migrated=new SkillEffectState();assertTrue(migrated.restore(old.snapshot()));assertTrue(migrated.has("player","REFLECT_ALL"));
  s.skillEffects().put("player","SK_무도가_022","REFLECT_ALL",1,16);s.monsters().get(0).hp=1;RuntimeCombatSession c=session(s);RuntimeCombatPortAdapter port=field(c,"port");int hp=s.player().hp;port.applyDamage("front","player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,100);assertEquals(hp,s.player().hp);assertFalse(s.monsters().get(0).alive);assertEquals(1,s.ledger().snapshot().stream().filter(e->e.type==CombatLedger.Type.MONSTER_DEFEATED).count());
 }
 @Test public void actualQuickslotsLeapOverNpcAndApplyInvincibilityWithoutSelectionOrApproach()throws Exception{
  GameView v=new GameView(context);v.layout(0,0,960,540);v.setSkillTestMode(true);RuntimeState s=field(v,"state");monsters(s).clear();List<RuntimeState.Npc> npcs=field(s,"npcs");npcs.clear();WorldRuntimeAdapter world=(WorldRuntimeAdapter)call(v,"activeWorld",new Class<?>[]{});boolean found=false;
  for(com.projectdark.mobile.world.WorldMoveTargetController.TileCenter tile:world.navigationTiles()){s.player().x=tile.x;s.player().y=tile.y;if(world.canPlayerLeapTo(tile.x+64,tile.y+32)&&world.canPlayerOccupy(tile.x+32,tile.y+16)){found=true;break;}}assertTrue("actual legal Milles leap",found);
  float x=s.player().x,y=s.player().y;npcs.add(new RuntimeState.Npc("npc_jump","NPC",x+32,y+16,"","B"));((CanonicalActorFacing)field(v,"playerFacing")).setLocomotion(CharacterRenderer.Direction.SE);SkillBook book=field(v,"skillBook");book.assign(0,MartialSkillPolicy.LEAP);slot(v);call(v,"tickSkillCombat",new Class<?>[]{float.class},.15f);assertEquals(x+64,s.player().x,0);assertEquals(y+32,s.player().y,0);assertNull(((SkillApproachController)field(v,"skillApproach")).skillId);
  call(v,"update",new Class<?>[]{float.class},.6f);book.assign(0,"SK_무도가_016");slot(v);call(v,"tickSkillCombat",new Class<?>[]{float.class},.01f);assertTrue(s.skillEffects().has("player","INVINCIBLE"));int hp=s.player().hp;s.damagePlayer(500);assertEquals(hp,s.player().hp);Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));save(b,"actual-milles-quickslot-gold");b.recycle();
  call(v,"update",new Class<?>[]{float.class},.7f);book.assign(0,"SK_무도가_034");slot(v);assertTrue((Boolean)field(v,"inReagentShop"));assertEquals(com.projectdark.mobile.world.TownInteriorDef.Kind.INN,((com.projectdark.mobile.world.TownInteriorDef)field(v,"townInterior")).kind);call(v,"leaveReagentShop",new Class<?>[]{});assertEquals(WorldDef.ID,s.currentMapId());
 }
 void slot(GameView v)throws Exception{RectF rect=(RectF)call(v,"slotRect",new Class<?>[]{int.class},0);TownInteriorTest.tap(v,rect.centerX(),rect.centerY());}
 void save(Bitmap b,String name)throws Exception{File f=new File("build/reports/martial-v173/"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
}
