package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class InnDetailQuestFxTest {
  Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  GameView start()throws Exception{
    GameView v=new GameView(c);v.layout(0,0,960,540);
    ((F5mAdaptedPrologueQuest)TownInteriorTest.field(v,"f5mQuest")).accept();
    TownInteriorTest.enter(v,TownInteriorDef.forMap("milles_interior_inn"));return v;
  }
  Bitmap render(GameView v)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));return b;}
  void capture(GameView v,String name)throws Exception{Bitmap b=render(v);File f=new File("build/reports/device-review/v93-"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();}
  void advance(GameView v,float seconds)throws Exception{for(float t=0;t<seconds;t+=.01f)MartialHudAutoRegressionTest.call(v,"update",float.class,Math.min(.01f,seconds-t));}
  void place(GameView v)throws Exception{
    RuntimeState s=TownInteriorTest.field(v,"state");RuntimeState.Monster m=s.monsters().get(0);m.x=TownInteriorDef.x(7,10);m.y=TownInteriorDef.y(7,10);m.hp=20000;m.isMoving=false;m.attackCooldown=100;
    s.skillEffects().put(m.id,"qa","ROOT",1,60);s.rpg().restoreBaseResources(20000,20000);s.applyDerivedGrowth();s.player().hp=10000;s.player().mp=20000;s.player().x=TownInteriorDef.x(6,10);s.player().y=TownInteriorDef.y(6,10);
    WorldRuntimeAdapter w=TownInteriorTest.field(v,"reagentShopAdapter");w.snapCameraToPlayer();((CombatController)TownInteriorTest.field(v,"combat")).selectTarget(m);v.setSkillTestMode(true);
  }
  void assertVisibleFx(GameView v,String anchor)throws Exception{
    SkillVfxRenderer fx=TownInteriorTest.field(v,"skillVfx");assertTrue("actual resolver emits a pulse on "+anchor,fx.pulses.stream().anyMatch(f->anchor.equals(f.anchor)));
    Bitmap with=render(v);List<SkillVfxRenderer.Pulse> saved=new ArrayList<>(fx.pulses);fx.pulses.clear();Bitmap without=render(v);fx.pulses.addAll(saved);
    int changed=0;for(int y=100;y<380;y++)for(int x=280;x<660;x++)if(with.getPixel(x,y)!=without.getPixel(x,y))changed++;
    assertTrue("indoor onDraw actually projects effect pixels, count="+changed,changed>5);with.recycle();without.recycle();
  }
  @Test public void realIndoorQuickslotsDrawMartialMagicAndSelfEffects()throws Exception{
    for(String id:new String[]{"SK_무도가_007","SK_마법사_005","SK_무도가_017"}){
      setup();GameView v=start();place(v);SkillBook b=TownInteriorTest.field(v,"skillBook");assertTrue(b.assign(0,id));TownInteriorTest.tap(v,671,395);
      assertEquals("actual quickslot is accepted indoors",id,TownInteriorTest.field(v,"activeSkillVisualId"));
      advance(v,SkillActionContract.get(id).contact+.06f);
      RuntimeState s=TownInteriorTest.field(v,"state");assertVisibleFx(v,SkillActionContract.get(id).selfAnchored()?"player":s.monsters().get(0).id);capture(v,"inn-"+id.replace("SK_", ""));
    }
  }
  @Test public void mouseResolvedContactDrawsImpactOnTheActualPlayer()throws Exception{
    GameView v=start();place(v);RuntimeState s=TownInteriorTest.field(v,"state");RuntimeState.Monster m=s.monsters().get(0);s.skillEffects().clear(m.id);m.attackCooldown=0;m.attackPrimed=false;
    int hp=s.player().hp;advance(v,.52f);assertTrue("real shared mouse attack damages player",s.player().hp<hp);assertVisibleFx(v,"player");capture(v,"inn-mouse-hit");
  }
  @Test public void rewardUnlocksTheNextQuestAndJournalWalksOutToItsNpc()throws Exception{
    GameView v=start();RuntimeState s=TownInteriorTest.field(v,"state");RuntimeState.Monster rat=s.monsters().get(0);
    s.damage(rat,rat.hp);advance(v,.01f);F5mAdaptedPrologueQuest q=TownInteriorTest.field(v,"f5mQuest");assertEquals(F5mAdaptedPrologueQuest.State.RETURN_READY,q.state());
    TownInteriorDef d=TownInteriorDef.forMap(s.currentMapId());WorldRuntimeAdapter w=TownInteriorTest.field(v,"reagentShopAdapter");WorldCameraTransform.Point npc=w.worldToScreen(d.npcX(),d.npcY());TownInteriorTest.tap(v,npc.x,npc.y-24);TownInteriorTest.tick(v,600);TownInteriorTest.tap(v,570,426);
    GrowthQuest2 next=TownInteriorTest.field(v,"quest2");assertEquals(F5mAdaptedPrologueQuest.State.COMPLETED,q.state());assertEquals("unlocked in the same reward transaction",GrowthQuest2.State.AVAILABLE,next.state());capture(v,"inn-next-quest");
    // Production journal button, then production objective navigation.
    TownInteriorTest.tap(v,294,28);capture(v,"growth-journal");TownInteriorTest.tap(v,560,404);TownInteriorTest.tick(v,2400);
    assertEquals(WorldDef.ID,s.currentMapId());InteractionController interaction=TownInteriorTest.field(v,"interaction");assertTrue("continued route opens next dialogue; alive="+s.player().alive+", hp="+s.player().hp+", x="+s.player().x+", y="+s.player().y,interaction.dialogOpen());assertEquals("milles_guide_proto",interaction.dialogNpc().id);capture(v,"growth-npc");TownInteriorTest.tap(v,550,426);assertEquals(GrowthQuest2.State.ACTIVE,next.state());
    assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(c);GameView restored=new GameView(c);assertEquals(GrowthQuest2.State.ACTIVE,((GrowthQuest2)TownInteriorTest.field(restored,"quest2")).state());assertEquals(s.rpg().gold(),((RuntimeState)TownInteriorTest.field(restored,"state")).rpg().gold());
  }
  @Test public void completedGrowthJournalReachesHansAndHisForestTravel()throws Exception{
    GameView v=start();F5mAdaptedPrologueQuest q=TownInteriorTest.field(v,"f5mQuest");q.restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);GrowthQuest2 growth=TownInteriorTest.field(v,"quest2");growth.restore(GrowthQuest2.State.COMPLETED,3);
    TownInteriorTest.call(v,"leaveReagentShop");RuntimeState s=TownInteriorTest.field(v,"state");s.rpg().restoreBaseResources(20000,20000);s.applyDerivedGrowth();s.player().hp=s.player().maxHp;
    TownInteriorTest.tap(v,294,28);capture(v,"forest-journal");TownInteriorTest.tap(v,560,404);TownInteriorTest.tick(v,1600);
    InteractionController i=TownInteriorTest.field(v,"interaction");assertTrue("Hans route opens dialogue",i.dialogOpen());assertEquals("milles_gate_proto",i.dialogNpc().id);capture(v,"hans-travel");TownInteriorTest.tap(v,550,426);assertEquals(PoteFieldDef.MAP_ID,s.currentMapId());capture(v,"forest-arrival");
  }
  @Test public void individualAntiqueSpritesHaveTransparentMarginsAndNativeScaleDetail()throws Exception{
    for(String n:new String[]{"table","chair_ne","chair_nw","chair_se","chair_sw","hearth"})try(InputStream in=c.getAssets().open("interiors/v93/"+n+".png")){
      Bitmap b=BitmapFactory.decodeStream(in);Set<Integer> colors=new HashSet<>();int visible=0,clear=0;
      for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++){int p=b.getPixel(x,y);if(Color.alpha(p)>128){visible++;colors.add(p);}if(Color.alpha(p)==0)clear++;}
      assertTrue(n+" retains material detail",colors.size()>100);assertTrue(n+" is an isolated object",clear>500);assertTrue(visible>500);b.recycle();
    }
    TownInteriorDef inn=TownInteriorDef.forMap("milles_interior_inn");
    for(TownInteriorDef.Prop chair:inn.props)if(chair.asset.startsWith("inn_world_chair_")){
      float best=Float.MAX_VALUE,dx=0,dy=0;
      for(TownInteriorDef.Prop table:inn.props)if(table.asset.equals("inn_world_table"))for(int u=table.u;u<table.u+table.cellsU;u++){
        if(Math.abs(u-chair.u)+Math.abs(table.v-chair.v)!=1)continue;
        float x=TownInteriorDef.x(u,table.v)-chair.x(),y=TownInteriorDef.y(u,table.v)-chair.y(),dist=x*x+y*y;if(dist<best){best=dist;dx=x;dy=y;}
      }
      assertEquals("chair faces its own table", "inn_world_chair_"+CanonicalActorFacing.quantize(dx,dy,CharacterRenderer.Direction.SE).name().toLowerCase(java.util.Locale.ROOT),chair.asset);
    }
    GameView v=start();capture(v,"antique-entry");RuntimeState s=TownInteriorTest.field(v,"state");WorldRuntimeAdapter w=TownInteriorTest.field(v,"reagentShopAdapter");
    for(int[] tile:new int[][]{{5,6},{6,8},{8,7},{11,9}}){s.player().x=TownInteriorDef.x(tile[0],tile[1]);s.player().y=TownInteriorDef.y(tile[0],tile[1]);w.snapCameraToPlayer();capture(v,"antique-depth-"+tile[0]+"-"+tile[1]);}
  }
  @Test public void skillWindowAndInventoryCanBeOpenedAndClosedInsideInn()throws Exception{
    GameView v=start();TownInteriorTest.tap(v,773,28);assertTrue(((SkillWindow)TownInteriorTest.field(v,"skillWindow")).open);capture(v,"inn-skills");TownInteriorTest.tap(v,773,28);assertFalse(((SkillWindow)TownInteriorTest.field(v,"skillWindow")).open);
    TownInteriorTest.tap(v,608,28);assertTrue((Boolean)TownInteriorTest.field(v,"inventoryOpen"));capture(v,"inn-inventory");
  }
}
