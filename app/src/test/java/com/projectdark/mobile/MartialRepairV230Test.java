package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MartialRepairV230Test {
 final MartialSevenV228Test h=new MartialSevenV228Test();
 @Before public void setup(){h.setup();}
 SkillAbilityExecutor.Movement movement(RuntimeState s,java.util.function.BiPredicate<Float,Float> ok){return new SkillAbilityExecutor.Movement(){public boolean canLeapPlayer(float x,float y){return ok.test(x,y);}public boolean leapPlayer(float x,float y){if(!canLeapPlayer(x,y))return false;s.player().x=x;s.player().y=y;return true;}public boolean movePlayer(float x,float y){return false;}public boolean moveMonster(RuntimeState.Monster m,float x,float y){return false;}};}
 @Test public void forwardMonsterAndNpcAtEveryDistanceUseTargetRelativeLandingAndFaceBackEvenAfterKill()throws Exception{
  for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(int distance=1;distance<=3;distance++)for(boolean npc:new boolean[]{false,true}){
   final int range=distance;RuntimeState s=h.state();List<RuntimeState.Npc> ns=h.h.field(s,"npcs");ns.clear();float[] v=MartialSkillPolicy.step(d);String id="target";
   if(npc)ns.add(new RuntimeState.Npc(id,"NPC",v[0]*distance,v[1]*distance,"","B"));else h.add(s,id,v[0]*distance,v[1]*distance).hp=1;
   RuntimeCombatSession c=h.session(s,d);c.setSkillMovement(movement(s,(x,y)->x==v[0]*(range+1)&&y==v[1]*(range+1)));
   assertTrue(d+" "+distance+" npc="+npc,c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());c.tick(.15f);assertEquals(v[0]*(distance+1),s.player().x,0);assertEquals(v[1]*(distance+1),s.player().y,0);float[] facing=c.takeAdvanceFacing();assertNotNull(facing);assertEquals(-v[0],facing[0],0);assertEquals(-v[1],facing[1],0);if(!npc)assertFalse(s.monsters().get(0).alive);
  }
 }
 @Test public void missingForwardTargetBlockedLandingAndSideTargetDoNotSpendOrMove()throws Exception{
  RuntimeState s=h.state();RuntimeCombatSession c=h.session(s,CharacterRenderer.Direction.SE);c.setSkillMovement(movement(s,(x,y)->true));h.add(s,"side",-32,16);assertFalse(c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());h.add(s,"far",128,64);assertFalse(c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());h.add(s,"front",96,48);c.setSkillMovement(movement(s,(x,y)->false));assertFalse(c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());assertEquals(50000,s.player().mp);assertEquals(0,s.player().x,0);assertEquals(0,c.cooldownRemaining("player",MartialSourceV228.ADVANCE),0);
  c.setSkillMovement(movement(s,(x,y)->x==64&&y==64));assertTrue(c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());c.tick(.15f);assertEquals(64,s.player().x,0);assertEquals(64,s.player().y,0);
 }
 @Test public void pacheonHitsOnlyFourAdjacentDirectionsAndBungsinUsesBunggakProfile()throws Exception{
  RuntimeState s=h.state();for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){float[] v=MartialSkillPolicy.step(d);h.add(s,d.name(),v[0],v[1]);h.add(s,"far"+d.name(),v[0]*2,v[1]*2);}h.add(s,"diagonal",64,0);
  RuntimeCombatSession c=h.session(s,CharacterRenderer.Direction.SE);assertTrue(c.submitPlayer("player",MartialSourceV228.PACHEON).accepted());List<CombatResolver.Event> es=c.tick(.15f).events;for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){assertEquals(1,h.hits(es,d.name()));assertEquals(0,h.hits(es,"far"+d.name()));}assertEquals(0,h.hits(es,"diagonal"));
  SkillPresentationCatalog cat=new SkillPresentationCatalog(h.h.c);assertEquals(cat.get("SK_무도가_007").motion,cat.get("SK_무도가_025").motion);
 }
 @Test public void exactSourceIconsAndCompleteSourceFxRenderAndFade()throws Exception{
  SkillIconCatalog icons=new SkillIconCatalog(h.h.c);ClassicSkillReference ref=new ClassicSkillReference(h.h.c);for(String n:new String[]{"035","027","024","025","026","028","029"}){
   String id="SK_무도가_"+n;Bitmap expected=BitmapFactory.decodeStream(h.h.c.getAssets().open("skill-presentation/user-v228/icon-"+n+".png"));assertEquals(31,expected.getWidth());Bitmap actual=Bitmap.createBitmap(31,31,Bitmap.Config.ARGB_8888);assertTrue(icons.drawSquare(new Canvas(actual),id,new RectF(0,0,31,31)));assertTrue(id,actual.sameAs(expected));
  }
  java.io.File dir=new java.io.File("build/reports/horror-v179/v230");dir.mkdirs();
  for(String n:new String[]{"026","028","029"}){ClassicSkillReference.Channel ch=ref.channel("SK_무도가_"+n,false);CapturedSkillFx.Sequence seq=ch.sequence;Bitmap sheet=Bitmap.createBitmap(240*seq.durations.length,180,Bitmap.Config.ARGB_8888);Canvas cv=new Canvas(sheet);cv.drawColor(Color.rgb(40,55,48));float time=0;for(int i=0;i<seq.durations.length;i++){cv.save();cv.translate(240*i,0);ref.draw(cv,ch,time+.001f,120,135);cv.restore();time+=seq.durations[i]/1000f;}try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(dir,n+"-sequence.png"))){sheet.compress(Bitmap.CompressFormat.PNG,100,out);}int last=seq.durations.length-1;for(int y=0;y<seq.height;y++)for(int x=0;x<seq.width;x++)assertEquals(0,Color.alpha(seq.atlas.getPixel(last%seq.columns*seq.width+x,last/seq.columns*seq.height+y)));}
 }
}
