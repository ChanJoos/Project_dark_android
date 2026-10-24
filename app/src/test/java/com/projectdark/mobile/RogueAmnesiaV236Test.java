package com.projectdark.mobile;
import static org.junit.Assert.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
import org.json.*;import java.util.*;import java.io.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RogueAmnesiaV236Test {
 final RogueCombatV234Test h=new RogueCombatV234Test();
 final String id=RogueSkillPolicy.AMNESIA;
 @Before public void setup(){h.setup();}
 @Test public void onlyFacingCellsTwoThroughFourNearestOnlyAndNoHpDamage()throws Exception{
  for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(int n=1;n<=5;n++){
   RuntimeState s=h.state();float[] step=MartialSkillPolicy.step(d);RuntimeState.Monster m=h.add(s,"front",step[0]*n,step[1]*n);RuntimeCombatSession c=h.session(s,d);
   assertEquals(d+" "+n,n>=2&&n<=4,c.submitFacingRogueRay(id).accepted());c.tick(.2f);assertEquals(m.maxHp,m.hp);assertEquals(n>=2&&n<=4,s.skillEffects().has(m.id,"AMNESIA"));
  }
  RuntimeState s=h.state();RuntimeState.Monster far=h.add(s,"far",128,64),near=h.add(s,"near",64,32),side=h.add(s,"side",0,32);RuntimeCombatSession c=h.session(s,CharacterRenderer.Direction.SE);
  assertFalse(c.submitPlayer(far.id,id).accepted());assertEquals(near.id,c.submitFacingRogueRay(id).targetId);c.tick(.2f);assertTrue(s.skillEffects().has(near.id,"AMNESIA"));assertFalse(s.skillEffects().has(far.id,"AMNESIA"));assertFalse(s.skillEffects().has(side.id,"AMNESIA"));
 }
 @Test public void adjacentAmnesiaIsRejectedButShurikenStillAcceptsIt()throws Exception{
  RuntimeState s=h.state();RuntimeState.Monster m=h.add(s,"adjacent",32,16);RuntimeCombatSession c=h.session(s,CharacterRenderer.Direction.SE);
  assertFalse(c.submitFacingRogueRay(id).accepted());assertFalse(s.skillEffects().has(m.id,"AMNESIA"));
  h.add(s,"farther",96,48);assertFalse("do not silently target through adjacent monster",c.submitFacingRogueRay(id).accepted());
  assertTrue(c.submitFacingShuriken().accepted());
 }
 @Test public void normalQuickslotRejectsAdjacentWithoutWalkingOrTurning()throws Exception{
  GameView v=h.actor(id);RuntimeState s=h.h.field(v,"state");RuntimeState.Monster near=s.monsters().get(0);float x=s.player().x,y=s.player().y;int mp=s.player().mp;
  h.tap(v);h.h.call(v,"tickSkillCombat",new Class<?>[]{float.class},.2f);
  assertFalse(s.skillEffects().has(near.id,"AMNESIA"));assertEquals(mp,s.player().mp);assertEquals(x,s.player().x,0);assertEquals(y,s.player().y,0);assertNull(((SkillApproachController)h.h.field(v,"skillApproach")).skillId);assertEquals(CharacterRenderer.Direction.SE,((CanonicalActorFacing)h.h.field(v,"playerFacing")).presentation());
 }
 @Test public void rayRechecksNearestVisibilityDirectionAndWallsAtContact()throws Exception{
  for(int mode=0;mode<6;mode++){
   RuntimeState s=h.state();RuntimeState.Monster m=h.add(s,"front",128,64);final boolean[] visible={true},wall={false};final CharacterRenderer.Direction[] facing={CharacterRenderer.Direction.SE};
   RuntimeCombatSession c=new RuntimeCombatSession(s,(a,t)->!wall[0],(a,i)->true,a->true);c.setPlayerFacing(()->facing[0]);c.setSkillVisibility(t->visible[0]);assertTrue(c.submitFacingRogueRay(id).accepted());
   if(mode==0)h.add(s,"new-near",64,32);if(mode==1)visible[0]=false;if(mode==2)wall[0]=true;if(mode==3)facing[0]=CharacterRenderer.Direction.NW;if(mode==4){m.x=32;m.y=16;}if(mode==5)h.add(s,"adjacent-blocker",32,16);
   c.tick(.2f);assertFalse(s.skillEffects().has(m.id,"AMNESIA"));assertEquals(m.maxHp,m.hp);
  }
 }
 @Test public void normalQuickslotIgnoresSelectedFarTargetWithoutWalkingOrTurning()throws Exception{
  GameView v=h.actor(id);RuntimeState s=h.h.field(v,"state");RuntimeState.Monster near=s.monsters().get(0);near.x=s.player().x+64;near.y=s.player().y+32;RuntimeState.Monster far=h.add(s,"far",s.player().x+128,s.player().y+64);((CombatController)h.h.field(v,"combat")).selectTarget(far);float x=s.player().x,y=s.player().y;int mp=s.player().mp;
  h.tap(v);RuntimeCombatSession c=h.h.field(v,"combatSession");assertTrue(c.playerActionActive());h.h.call(v,"tickSkillCombat",new Class<?>[]{float.class},.2f);
  assertTrue(s.skillEffects().has(near.id,"AMNESIA"));assertFalse(s.skillEffects().has(far.id,"AMNESIA"));assertEquals(near.maxHp,near.hp);assertEquals(mp,s.player().mp);assertEquals(x,s.player().x,0);assertEquals(y,s.player().y,0);assertNull(((SkillApproachController)h.h.field(v,"skillApproach")).skillId);assertEquals(CharacterRenderer.Direction.SE,((CanonicalActorFacing)h.h.field(v,"playerFacing")).presentation());h.h.capture(v,"v236-amnesia-normal-quickslot");
 }
 @Test public void everyHitDuringTenSecondsDoublesActualHpPopupAndLedgerThenAggressionReturns()throws Exception{
  RuntimeState s=h.state();com.projectdark.mobile.world.WorldMoveTargetController.TileCenter at=s.monsterNavigationTiles().stream().filter(t->s.isMonsterTileCenter(t.x+32,t.y+16)).findFirst().orElseThrow(AssertionError::new);s.player().x=at.x;s.player().y=at.y;RuntimeState.Monster m=h.add(s,"combat_dummy_01",at.x+64,at.y+32);RuntimeCombatSession c=h.session(s,CharacterRenderer.Direction.SE);assertTrue(c.submitFacingRogueRay(id).accepted());c.tick(.2f);assertEquals(10,s.skillEffects().get(m.id,"AMNESIA").remaining,0);m.x=at.x+32;m.y=at.y+16;
  MonsterAIController ai=new MonsterAIController();int playerHp=s.player().hp;
  for(int n=0;n<3;n++){int hp=m.hp;s.damage(m,123);assertEquals(246,hp-m.hp);assertEquals(246,m.lastDamage);assertTrue(s.skillEffects().has(m.id,"AGGRO_RESET"));ai.tick(s,.8f);s.tick(.8f);assertEquals(playerHp,s.player().hp);assertFalse(m.attackPrimed);}
  assertTrue(s.ledger().snapshot().stream().filter(e->e.type==CombatLedger.Type.MONSTER_HIT).allMatch(e->e.amount==246));
  s.tick(7.61f);assertFalse(s.skillEffects().has(m.id,"AMNESIA"));assertFalse(s.skillEffects().has(m.id,"AGGRO_RESET"));int hp=m.hp;s.damage(m,123);assertEquals(123,hp-m.hp);assertEquals(123,m.lastDamage);
  ai.tick(s,.01f);ai.tick(s,.3f);assertTrue("normal attack resumes",m.attackPrimed||s.player().hp<playerHp);
 }
 @Test public void resolverFeedbackDoublesAfterDefenseAndOnlyAffectedMonsterThenDefeatExactlyOnce()throws Exception{
  RuntimeState s=h.state();RuntimeState.Monster m=h.add(s,"front",64,32),other=h.add(s,"other",-32,-16);RuntimeCombatSession c=h.session(s,CharacterRenderer.Direction.SE);assertTrue(c.submitFacingRogueRay(id).accepted());c.tick(.2f);
  RuntimeCombatPortAdapter p=h.h.field(c,"port");p.abilities.random=()->.99;String attack=RuntimeCombatSession.playerAttackActionId(AnimationAction.THRUST);int raw=s.rpg().finalStats().prototypePhysicalAttack();
  // Current combat resolves defense before the Amnesia multiplier; compare identical untreated stats.
  int normal=p.applyDamage("player",other.id,attack,raw).appliedAmount;assertTrue(normal>0);assertTrue("fixture exercises defense",normal<raw);assertEquals(normal,other.maxHp-other.hp);
  CombatResolver.EffectResult r=p.applyDamage("player",m.id,attack,raw);assertEquals(normal*2,r.appliedAmount);assertEquals(normal*2,m.maxHp-m.hp);assertEquals(normal*2,m.lastDamage);
  assertTrue(s.skillEffects().has(m.id,"AMNESIA"));assertFalse(s.skillEffects().has(other.id,"AMNESIA"));
  m.hp=1;p.applyDamage("player",m.id,attack,raw);p.applyDamage("player",m.id,attack,raw);assertEquals(1,s.ledger().snapshot().stream().filter(e->e.type==CombatLedger.Type.MONSTER_DEFEATED&&e.targetId.equals(m.id)).count());s.tick(.01f);assertFalse(s.skillEffects().has(m.id,"AMNESIA"));
 }
 @Test public void mageConfusionUsesSamePassiveDoubleDamageWindow()throws Exception{
  RuntimeState s=h.state();RuntimeState.Monster m=h.add(s,"front",32,16);RuntimeCombatSession c=h.session(s,CharacterRenderer.Direction.SE);assertTrue(c.submitPlayer(m.id,"SK_마법사_006").accepted());c.tick(c.effectiveCastSeconds("SK_마법사_006")+.01f);assertTrue(s.skillEffects().has(m.id,"AGGRO_RESET"));s.damage(m,123);assertEquals(246,m.maxHp-m.hp);assertTrue(s.skillEffects().has(m.id,"AGGRO_RESET"));assertTrue(s.skillEffects().has(m.id,"AMNESIA"));
 }
 @Test public void lowStatDamageHasDistinctRoleStrengthAndCurrentHpFinisherRemainsHonest()throws Exception{
  RuntimeState s=h.state();s.rpg().restoreStats(12,6,4,54,17,0);s.rpg().restoreProgression(26,0);FinalStats stats=s.rpg().finalStats();double base=stats.weaponBaseAttack()+1.8*stats.str+2.2*stats.dex+stats.dam;
  String[] ns={"001","007","014","021","022","030","015","029","028","023"};double[] coefficients={2,4,8,10,16,18,20,21,22,50};int last=0;JSONArray trace=new JSONArray();
  for(int n=0;n<ns.length;n++){String skill="SK_도적_"+ns[n];int expected=(int)Math.round(base*coefficients[n]);SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(skill);int amount=SkillAbilityCatalog.amount(a,new SkillAbilityCatalog.Snapshot(stats,8333,2606,26,100,false));assertEquals(expected,amount);assertTrue(amount>last);last=amount;assertEquals((int)Math.round(amount*.8),SkillAbilityCatalog.amount(a,new SkillAbilityCatalog.Snapshot(stats,8333,2606,26,0,false)),1);trace.put(new JSONObject().put("id",skill).put("level",26).put("str",stats.str).put("dex",stats.dex).put("raw",amount).put("coefficient",coefficients[n]).put("evidence","PROJECT_ADAPTED_NOT_ORIGINAL"));}
  assertEquals(609,SkillAbilityCatalog.amount(SkillAbilityCatalog.get("SK_도적_020"),new SkillAbilityCatalog.Snapshot(stats,961,2606,26,100,false)));
  assertEquals(6090,SkillAbilityCatalog.amount(SkillAbilityCatalog.get("SK_도적_020"),new SkillAbilityCatalog.Snapshot(stats,9612,2606,26,100,false)));
  File f=new File("build/reports/device-review/v236-low-stat-damage.json");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){out.write(trace.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
 }
}
