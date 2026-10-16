package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;import android.graphics.*;import java.util.*;import java.io.*;import org.json.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import com.projectdark.mobile.world.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SkillTimingV174Test {
 Context context;
 @Before public void setup(){context=RuntimeEnvironment.getApplication();new SkillAbilityRuntimeTest().before();context.getSharedPreferences("project_dark_skill_test_v1",0).edit().clear().commit();UiTheme.install(context);}
 @Test public void wholeCatalogUsesExplicitReuseEvidenceAndRetainsUnknownOriginalValues()throws Exception{
  JSONObject policy=new JSONObject(new String(java.nio.file.Files.readAllBytes(new File("../master/changes/V174_SKILL_TIMING.json").toPath()),java.nio.charset.StandardCharsets.UTF_8));JSONArray rows=policy.getJSONArray("rows");assertEquals(221,rows.length());Set<String> ids=new HashSet<>();int historical=0,pending=0;
  for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);String id=row.getString("id");assertTrue(ids.add(id));SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);assertNotNull(id,a);assertEquals(id,row.getDouble("runtimeSeconds"),a.cooldown,.00001);assertEquals(row.getString("status"),a.cooldownEvidence);
   if(a.cooldownEvidence.equals("FAN_HISTORICAL_EXPLICIT")){historical++;assertTrue(row.getString("source").startsWith("https://"));assertEquals(64,row.getString("sourceSnapshotSha256").length());}
   if(a.cooldownEvidence.equals("RETAINED_ADAPTED_PENDING_ORIGINAL")){pending++;assertTrue(row.isNull("originalSeconds"));assertEquals(row.getDouble("before"),a.cooldown,0);}
  }assertEquals(25,historical);assertEquals(52,pending);
 }
 @Test public void realFinisherReuseBoundariesAreIndependentAndEqualInNormalAndTestModes()throws Exception{
  for(boolean test:new boolean[]{false,true})for(String id:new String[]{"SK_전사_013","SK_전사_022","SK_도적_020","SK_도적_027","SK_무도가_021","SK_무도가_023"}){
   SkillAbilityRuntimeTest fixtures=new SkillAbilityRuntimeTest();RuntimeState s=fixtures.state();RuntimeCombatSession c=fixtures.session(s);c.setSkillTestMode(test);SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);assertTrue(id,c.submitPlayer("m0",id).accepted());assertEquals(id,a.cooldown,c.cooldownRemaining("player",id),.0001);assertEquals(0,c.cooldownRemaining("player","SK_전사_001"),0);
   c.tick(a.cooldown-.01f);assertEquals(id,.01f,c.cooldownRemaining("player",id),.001);assertEquals(id,CombatResolver.RejectReason.COOLDOWN,c.submitPlayer("m0",id).rejectReason);c.tick(.02f);assertEquals(id,0,c.cooldownRemaining("player",id),0);
  }
 }
 @Test public void actualLeapHasNoActionOrParticlesAndFacesCrossedNpcFromAllLandings()throws Exception{
  for(CharacterRenderer.Direction dir:CharacterRenderer.Direction.values())for(int landing:new int[]{0,-1,1}){
   context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);GameView v=new GameView(context);v.layout(0,0,960,540);v.setSkillTestMode(true);RuntimeState s=MartialMechanicsV173Test.field(v,"state");List<RuntimeState.Monster> monsters=MartialMechanicsV173Test.field(s,"monsters");monsters.clear();List<RuntimeState.Npc> npcs=MartialMechanicsV173Test.field(s,"npcs");npcs.clear();float[] d=MartialSkillPolicy.step(dir);float x=1000,y=1000,tx=x+d[0],ty=y+d[1],lx=d[1]*2,ly=-d[0]/2;s.player().x=x;s.player().y=y;npcs.add(new RuntimeState.Npc("crossed","NPC",tx,ty,"","B"));
   CanonicalActorFacing facing=MartialMechanicsV173Test.field(v,"playerFacing");facing.setLocomotion(dir);RuntimeCombatSession session=MartialMechanicsV173Test.field(v,"combatSession");session.setSkillMovement(new SkillAbilityExecutor.Movement(){public boolean movePlayer(float a,float b){return false;}public boolean moveMonster(RuntimeState.Monster m,float a,float b){return false;}public boolean canLeapPlayer(float a,float b){return MartialSkillPolicy.same(a,b,landing==0?tx+d[0]:tx+landing*lx,landing==0?ty+d[1]:ty+landing*ly);}public boolean leapPlayer(float a,float b){if(!canLeapPlayer(a,b))return false;s.player().x=a;s.player().y=b;return true;}});
   SkillBook book=MartialMechanicsV173Test.field(v,"skillBook");MartialMechanicsV173Test.call(v,"useBookSkill",new Class<?>[]{SkillBook.Entry.class},book.get(MartialSkillPolicy.LEAP));assertNull(MartialMechanicsV173Test.field(v,"activeSkillVisualId"));assertFalse(facing.attackLocked());
   MartialMechanicsV173Test.call(v,"tickSkillCombat",new Class<?>[]{float.class},.15f);assertEquals(landing==0?tx+d[0]:tx+landing*lx,s.player().x,0);assertEquals(landing==0?ty+d[1]:ty+landing*ly,s.player().y,0);assertEquals(CanonicalActorFacing.quantize(tx-s.player().x,ty-s.player().y,dir),facing.presentation());assertFalse(facing.attackLocked());SkillVfxRenderer fx=MartialMechanicsV173Test.field(v,"skillVfx");assertTrue(fx.pulses.isEmpty());assertEquals("IDLE",new SkillPresentationCatalog(context).get(MartialSkillPolicy.LEAP).motion);
  }
 }
}
