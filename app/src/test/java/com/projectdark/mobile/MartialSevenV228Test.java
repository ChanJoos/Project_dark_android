package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MartialSevenV228Test {
 final ScreenMagicV201Test h=new ScreenMagicV201Test();
 @Before public void setup(){h.setup();}
 RuntimeState state()throws Exception{RuntimeState s=h.fixture();List<RuntimeState.Monster> ms=h.field(s,"monsters");ms.clear();return s;}
 RuntimeState.Monster add(RuntimeState s,String id,float x,float y)throws Exception{RuntimeState.Monster m=new RuntimeState.Monster(id,"V228 fixture",x,y,1000000,"B");List<RuntimeState.Monster> ms=h.field(s,"monsters");ms.add(m);return m;}
 RuntimeCombatSession session(RuntimeState s,CharacterRenderer.Direction d){RuntimeCombatSession c=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);c.setPlayerFacing(()->d);return c;}
 long hits(List<CombatResolver.Event> e,String id){return e.stream().filter(x->x.type==CombatResolver.EventType.HIT_FEEDBACK&&x.targetId.equals(id)).count();}
 @Test public void muyeongHitsEveryRecipientNActualTimesInAllFourDirections()throws Exception{
  for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(int n=1;n<=3;n++){
   RuntimeState s=state();float[] step=MartialSkillPolicy.step(d);for(int k=1;k<=n;k++)add(s,"front"+k,step[0]*k,step[1]*k);RuntimeState.Monster back=add(s,"back",-step[0],-step[1]),far=add(s,"far",step[0]*4,step[1]*4),side=add(s,"side",-step[0],step[1]);
   RuntimeCombatSession c=session(s,d);assertTrue(d+" "+n,c.submitFacingMartial(MartialSourceV228.MUYEONG).accepted());List<CombatResolver.Event> events=c.tick(2).events;
   for(int k=1;k<=n;k++)assertEquals(d+" "+n,n,hits(events,"front"+k));assertEquals(back.maxHp,back.hp);assertEquals(far.maxHp,far.hp);assertEquals(side.maxHp,side.hp);
   assertEquals("retained resource policy; no invented cost per recipient",50000,s.player().mp);assertFalse(c.playerActionActive());
   SkillVfxRenderer fx=new SkillVfxRenderer(h.c,new SkillPresentationCatalog(h.c));fx.consume(events,new SkillCorrectionsV227Test().anchors(s));assertEquals(n*n,fx.pulses.stream().filter(p->p.id.equals(MartialSourceV228.MUYEONG)&&p.sheet.equals("classic")).count());int pulseCount=fx.pulses.size();fx.consume(events,new SkillCorrectionsV227Test().anchors(s));assertEquals(pulseCount,fx.pulses.size());
  }
 }
 @Test public void muyeongSamplesCardinalityAtFirstContactAndKeepsOtherRecipientsAfterFirstDies()throws Exception{
  RuntimeState s=state();for(int k=1;k<=3;k++)add(s,"m"+k,32*k,16*k);RuntimeCombatSession c=session(s,CharacterRenderer.Direction.SE);Set<String> hidden=new HashSet<>();c.setSkillVisibility(id->!hidden.contains(id));assertTrue(c.submitFacingMartial(MartialSourceV228.MUYEONG).accepted());hidden.add("m3");s.monsters().get(0).hp=1;
  List<CombatResolver.Event> events=c.tick(.25f).events;List<CombatResolver.Event> later=c.tick(1).events;assertEquals(1,hits(events,"m1"));assertEquals(1,hits(events,"m2"));assertEquals(1,hits(later,"m2"));assertEquals(0,hits(events,"m3")+hits(later,"m3"));assertEquals(1000000,s.monsters().get(2).hp);assertFalse(c.playerActionActive());
  RuntimeState empty=state();add(empty,"behind",-32,-16);assertFalse(session(empty,CharacterRenderer.Direction.SE).submitFacingMartial(MartialSourceV228.MUYEONG).accepted());
 }
 @Test public void advanceAttacksThenLeapsOverTargetAndRejectsBlockedOrCancelledLanding()throws Exception{
  for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   RuntimeState s=state();float[] step=MartialSkillPolicy.step(d);RuntimeState.Monster m=add(s,"m",step[0],step[1]);RuntimeCombatSession c=session(s,d);final boolean[] legal={true};c.setSkillMovement(new SkillAbilityExecutor.Movement(){public boolean canLeapPlayer(float x,float y){return legal[0]&&x==step[0]*2&&y==step[1]*2;}public boolean leapPlayer(float x,float y){if(!canLeapPlayer(x,y))return false;s.player().x=x;s.player().y=y;return true;}public boolean movePlayer(float x,float y){return false;}public boolean moveMonster(RuntimeState.Monster m,float x,float y){return false;}});
   assertTrue(c.submitPlayer(m.id,MartialSourceV228.ADVANCE).accepted());assertEquals(0,s.player().x,0);c.tick(.15f);assertTrue(m.hp<m.maxHp);assertEquals(step[0]*2,s.player().x,0);assertEquals(step[1]*2,s.player().y,0);
   s.player().x=s.player().y=0;c.tick(4);legal[0]=false;int hp=m.hp;assertFalse(c.submitPlayer(m.id,MartialSourceV228.ADVANCE).accepted());assertEquals(0,c.cooldownRemaining("player",MartialSourceV228.ADVANCE),0);assertEquals(hp,m.hp);
   legal[0]=true;assertTrue(c.submitPlayer(m.id,MartialSourceV228.ADVANCE).accepted());legal[0]=false;c.tick(.15f);assertEquals(0,s.player().x,0);assertEquals(hp,m.hp);
  }
 }
 @Test public void tripleAttackIsLinkedToBasicAndSelfProtectionReducesActualIncomingDamage()throws Exception{
  RuntimeState s=state();RuntimeState.Monster m=add(s,"m",32,16);SkillBook b=SkillBook.load(h.c);assertEquals("트리플어택",b.get(MartialSourceV228.TRIPLE).name);assertTrue(b.learn(MartialSourceV228.TRIPLE,50));RuntimeCombatSession c=session(s,CharacterRenderer.Direction.SE);c.setBasicHits(b::basicHits);assertFalse(c.submitPlayer(m.id,MartialSourceV228.TRIPLE).accepted());assertTrue(c.submitPlayerBasicAttack(m.id).combat.accepted());List<CombatResolver.Event> basicEvents=c.tick(1).events;assertEquals(3,hits(basicEvents,m.id));SkillVfxRenderer basicFx=new SkillVfxRenderer(h.c,new SkillPresentationCatalog(h.c));basicFx.martialTriple=()->b.learned(MartialSourceV228.TRIPLE);basicFx.consume(basicEvents,new SkillCorrectionsV227Test().anchors(s));assertEquals(3,basicFx.pulses.stream().filter(pulse->pulse.id.equals(MartialSourceV228.TRIPLE)).count());
  RuntimeState plain=state();add(plain,"m",32,16);RuntimeCombatPortAdapter p=new RuntimeCombatPortAdapter(plain,(a,t)->true,(a,id)->true,a->true);int hp=plain.player().hp;p.applyDamage("m","player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,1000);int loss=hp-plain.player().hp;assertTrue(loss>0);
  RuntimeState guarded=state();add(guarded,"m",32,16);RuntimeCombatSession guard=session(guarded,CharacterRenderer.Direction.SE);assertTrue(guard.submitPlayer("player","SK_무도가_029").accepted());guard.tick(.25f);assertTrue(guarded.skillEffects().has("player","PROTECT"));RuntimeCombatPortAdapter gp=new RuntimeCombatPortAdapter(guarded,(a,t)->true,(a,id)->true,a->true);hp=guarded.player().hp;gp.applyDamage("m","player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,1000);assertEquals(Math.round(loss*.7f),hp-guarded.player().hp);
 }
 @Test public void newIdAndRenamedTripleSurviveDurableRestartWithoutChangingOldLeap()throws Exception{
  SkillBook b=SkillBook.load(h.c);assertEquals("이형환위",b.get(MartialSkillPolicy.LEAP).name);for(String id:new String[]{MartialSourceV228.ADVANCE,MartialSourceV228.TRIPLE})assertTrue(b.learn(id,42));assertTrue(b.assign(0,MartialSourceV228.ADVANCE));F5mSaveStore.bindRuntime(new RuntimeState(),F5mAdaptedPrologueQuest.openingFixture(),new GrowthQuest2());F5mSaveStore.restoreAndBindSkillsActive(b);assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(h.c);SkillBook restored=SkillBook.load(h.c);F5mSaveStore.restoreAndBindSkillsActive(restored);assertEquals(MartialSourceV228.ADVANCE,restored.slot(0));assertEquals(42,restored.proficiency(MartialSourceV228.TRIPLE));assertEquals("트리플어택",restored.get(MartialSourceV228.TRIPLE).name);
 }
 @Test public void allSevenIconsAndCaptureChannelsRenderWithoutFloorOrSourcePeople()throws Exception{
  SkillIconCatalog icons=new SkillIconCatalog(h.c);ClassicSkillReference ref=new ClassicSkillReference(h.c);for(String n:new String[]{"035","027","024","025","026","028","029"}){
   String id="SK_무도가_"+n;Bitmap frame=Bitmap.createBitmap(240,220,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(frame);assertTrue(icons.drawSquare(c,id,new RectF(0,0,40,40)));assertTrue(icons.drawQuickslot(c,id,new RectF(40,0,80,40)));ClassicSkillReference.Channel ch=ref.channel(id,false);assertNotNull(id,ch);assertEquals(0,Color.alpha(ch.sequence.atlas.getPixel(0,0)));ref.draw(c,ch,ch.sequence.duration*.5f,120,170);assertTrue(ch.sequence.atlas.getAllocationByteCount()<4000000);assertTrue(ch.sequence.duration<2);
  }
  assertTrue(ItemEffects.magic("SK_무도가_028"));assertTrue(ItemEffects.magic("SK_무도가_029"));
 }
 @Test public void allSixActiveSkillsUseRealNormalQuickslotsAndNativeRecipientOrCasterEffects()throws Exception{
  for(String n:new String[]{"035","024","025","026","028","029"}){
   h.setup();String id="SK_무도가_"+n;GameView v=new GameView(h.c);v.layout(0,0,960,540);RuntimeState s=h.field(v,"state");WorldRuntimeAdapter w=h.field(v,"worldAdapter");List<RuntimeState.Monster> ms=h.field(s,"monsters");ms.clear();s.rpg().restoreProgression(99,0);assertTrue("job restored",s.rpg().restoreJobCode("MARTIAL_ARTIST"));s.rpg().restoreStats(50,50,50,50,50,0);s.rpg().restoreBaseResources(100000,100000);s.applyDerivedGrowth();s.player().hp=100000;s.player().mp=50000;
   boolean placed=false;for(WorldMoveTargetController.TileCenter t:w.navigationTiles()){s.player().x=t.x;s.player().y=t.y;if(w.canPlayerOccupy(t.x,t.y)&&w.canPlayerLeapTo(t.x+96,t.y+48)){placed=true;break;}}assertTrue(id+" legal terrain fixture",placed);add(s,"v228-target",s.player().x+32,s.player().y+16);w.snapCameraToPlayer();((CanonicalActorFacing)h.field(v,"playerFacing")).updateLocomotion(32,16);SkillBook b=h.field(v,"skillBook");assertTrue(b.learn(id,50));assertTrue(b.assign(0,id));((CombatController)h.field(v,"combat")).selectTarget(s.monsters().get(0));RectF slot=(RectF)h.call(v,"slotRect",new Class<?>[]{int.class},0);h.tap(v,slot.centerX(),slot.centerY());RuntimeCombatSession session=h.field(v,"combatSession");assertTrue(id,session.playerActionActive());h.call(v,"tickSkillCombat",new Class<?>[]{float.class},session.effectiveCastSeconds(id)+.001f);SkillVfxRenderer fx=h.field(v,"skillVfx");assertTrue(id,fx.pulses.stream().anyMatch(p->p.id.equals(id)&&p.sheet.equals("classic")));if(n.equals("026")||n.equals("029"))assertTrue(id+" source effect belongs to caster",fx.pulses.stream().filter(p->p.id.equals(id)&&p.sheet.equals("classic")).allMatch(p->p.anchor.equals("player")));if(n.equals("026"))assertTrue(id+" damage feedback belongs to recipient",fx.pulses.stream().anyMatch(p->p.id.equals(id)&&p.sheet.equals("impact")&&p.anchor.equals("v228-target")));h.capture(v,"v228-"+n);
  }
 }
}
