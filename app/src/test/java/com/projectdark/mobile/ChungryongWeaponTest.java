package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.*;
import android.graphics.*;
import java.io.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ChungryongWeaponTest {
  Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();c.getSharedPreferences("project_dark_visual_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  void save(Bitmap b,String n)throws Exception{File f=new File("build/reports/device-review/v96-"+n+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
  Bitmap render(GameView v)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));return b;}
  int cyan(Bitmap b){int count=0;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++){int z=b.getPixel(x,y);if(Color.alpha(z)>128&&Color.blue(z)-Color.red(z)>=20&&Color.green(z)-Color.red(z)>=5)count++;}return count;}
  @Test public void inventoryInputEquipsActualIconAndPaperDollThenRestarts()throws Exception{
    GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=TownInteriorTest.field(v,"state");ItemWindow w=TownInteriorTest.field(v,"itemWindow");
    assertEquals(Integer.valueOf(1),s.rpg().inventory().get(ChungryongWeaponRenderer.ITEM));TownInteriorTest.tap(v,608,28);int index=-1;
    List<RpgInventoryPresentation.ItemRow> rows=w.rows(s.rpg());for(int i=0;i<rows.size();i++)if(ChungryongWeaponRenderer.ITEM.equals(rows.get(i).itemId))index=i;
    assertTrue(index>=0);RectF cell=ItemWindow.cell(index);TownInteriorTest.tap(v,cell.centerX(),cell.centerY());save(render(v),"inventory-compare");TownInteriorTest.tap(v,805,490);
    assertEquals(ChungryongWeaponRenderer.ITEM,s.rpg().equipment().get("무기"));assertEquals(ChungryongWeaponRenderer.APPEARANCE,CharacterVisualBinding.from(s.rpg()).weaponVisualRef());
    TownInteriorTest.tap(v,914,60);TownInteriorTest.tap(v,716,28);save(render(v),"equipped-paper-doll");v.pause();F5mSaveStore.install(c);GameView restored=new GameView(c);RuntimeState r=TownInteriorTest.field(restored,"state");
    assertEquals(s.rpg().equipment(),r.rpg().equipment());assertEquals(Integer.valueOf(1),r.rpg().inventory().get(ChungryongWeaponRenderer.ITEM));
    r.rpg().equip(ChungryongWeaponRenderer.ITEM);assertNull(r.rpg().equipment().get("무기"));assertEquals(AnimationAction.PUNCH,new EquipmentActionResolver().resolveBasicAttack(r.rpg()).animationAction);
  }
  @Test public void previousSaveGetsOneWeaponWithoutLosingProgressOrRegranting()throws Exception{
    SharedPreferences prefs=c.getSharedPreferences("project_dark_f5m_v1",0);prefs.edit().putString("inventory_v2","{\"IT_TEST_WEAPON_MW002\":2}").putString("equipment_v2","{\"무기\":\"IT_TEST_WEAPON_MW002\"}").putLong("gold",456).putInt("normal_level",15).commit();
    F5mSaveStore.install(c);GameView v=new GameView(c);RuntimeState s=TownInteriorTest.field(v,"state");assertEquals(Integer.valueOf(1),s.rpg().inventory().get(ChungryongWeaponRenderer.ITEM));assertEquals(Integer.valueOf(2),s.rpg().inventory().get("IT_TEST_WEAPON_MW002"));assertEquals(Long.valueOf(456),s.rpg().gold());assertEquals("IT_TEST_WEAPON_MW002",s.rpg().equipment().get("무기"));assertTrue(prefs.getBoolean("chungryong_granted_v94",false));
    v.pause();F5mSaveStore.install(c);GameView restarted=new GameView(c);assertEquals(s.rpg().inventory(),((RuntimeState)TownInteriorTest.field(restarted,"state")).rpg().inventory());
    prefs.edit().putString("inventory_v2","{\"IT_TEST_WEAPON_MW002\":2}").commit();F5mSaveStore.install(c);GameView noRegrant=new GameView(c);assertFalse(((RuntimeState)TownInteriorTest.field(noRegrant,"state")).rpg().inventory().containsKey(ChungryongWeaponRenderer.ITEM));
  }
  @Test public void invalidPreviousSaveRemainsUntouched()throws Exception{
    SharedPreferences p=c.getSharedPreferences("project_dark_f5m_v1",0);String bad="{\"unknown_saved_identity\":1}";p.edit().putString("inventory_v2",bad).commit();F5mSaveStore.install(c);new GameView(c);assertFalse(F5mSaveStore.writable());assertEquals(bad,p.getString("inventory_v2",null));assertFalse(p.getBoolean("chungryong_granted_v94",false));
  }
  @Test public void sourceWeaponFramesAndBothBodiesHaveDistinctFourWayMotion()throws Exception{
    SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);SkillBodyRenderer body=new SkillBodyRenderer(c,catalog);ChungryongWeaponRenderer weapon=new ChungryongWeaponRenderer(c);assertTrue(cyan(weapon.icon)>50);
    for(String identity:new String[]{"mm001","wm001"})for(String motion:ChungryongWeaponRenderer.MOTIONS){
      Bitmap sheet=Bitmap.createBitmap(720,550,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(sheet);out.drawColor(0xff403b34);int col=0;
      for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
        for(int n=0;n<5;n++){float q=new float[]{0,.2f,.28f,1f/3f,.85f}[n];Bitmap b=Bitmap.createBitmap(180,110,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(b);
          CharacterRenderer.Pose p=new CharacterRenderer.Pose(90,92,d,CharacterRenderer.State.ATTACK,0,q,1,false,"mu0000001,mh172,ml228",ChungryongWeaponRenderer.APPEARANCE,null,CharacterRenderer.EffectFamily.NONE,AnimationAction.SWING);
          assertTrue(body.drawChungryong(canvas,p,identity,motion,q));assertTrue("visible blade "+identity+motion+d+n,cyan(b)>20);out.drawBitmap(b,col*180,n*110,null);b.recycle();}
        col++;
      }save(sheet,identity+"-"+motion);sheet.recycle();
    }
    for(String m:ChungryongWeaponRenderer.MOTIONS){assertEquals(0,ChungryongWeaponRenderer.frame(m,0));assertTrue(ChungryongWeaponRenderer.frame(m,1f/3f)>0);assertEquals(0,ChungryongWeaponRenderer.frame(m,.9f));}
  }
  @Test public void realWarriorInputsKeepFacingContactAndAllThreeWeaponMotions()throws Exception{
    for(String id:new String[]{"SK_전사_001","SK_전사_006","SK_전사_015"})for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
      setup();GameView v=new MartialHudAutoRegressionTest(){ {c=ChungryongWeaponTest.this.c;} }.directed(direction);RuntimeState s=TownInteriorTest.field(v,"state");assertEquals(RpgProgressionState.EquipResult.EQUIPPED,s.rpg().equip(ChungryongWeaponRenderer.ITEM));
      SkillBook book=TownInteriorTest.field(v,"skillBook");assertTrue(book.assign(0,id));TownInteriorTest.tap(v,671,395);assertEquals(id,TownInteriorTest.field(v,"activeSkillVisualId"));
      SkillActionContract.Rule rule=SkillActionContract.get(id);RuntimeState.Monster target=s.monsters().get(0);int hp=target.hp;advance(v,rule.contact-.01f);assertEquals(hp,target.hp);advance(v,.011f);
      assertEquals(direction,((CanonicalActorFacing)TownInteriorTest.field(v,"playerFacing")).presentation());if(rule.damage())assertTrue("existing resolver contact",target.hp<hp);
      save(render(v),"live-"+id+"-"+direction);advance(v,2f);assertNull(TownInteriorTest.field(v,"activeSkillVisualId"));
    }
  }
  @Test public void basicAttackAndIndoorSkillUseTheSameEquippedLayer()throws Exception{
    GameView v=new MartialHudAutoRegressionTest(){ {c=ChungryongWeaponTest.this.c;} }.directed(CharacterRenderer.Direction.SE);RuntimeState s=TownInteriorTest.field(v,"state");s.rpg().equip(ChungryongWeaponRenderer.ITEM);
    int before=s.monsters().get(0).hp;TownInteriorTest.tap(v,914,498);assertEquals("SWING",TownInteriorTest.field(v,"action").toString());advance(v,.17f);assertEquals("basic has no early hit before shared resolver contact",before,s.monsters().get(0).hp);advance(v,.011f);assertTrue("basic resolves at actual .18s contact",s.monsters().get(0).hp<before);assertEquals(ChungryongWeaponRenderer.APPEARANCE,CharacterVisualBinding.from(s.rpg()).weaponVisualRef());save(render(v),"basic-world");
    setup();InnDetailQuestFxTest inn=new InnDetailQuestFxTest();inn.c=c;v=inn.start();inn.place(v);s=TownInteriorTest.field(v,"state");s.rpg().equip(ChungryongWeaponRenderer.ITEM);SkillBook b=TownInteriorTest.field(v,"skillBook");b.assign(0,"SK_전사_001");TownInteriorTest.tap(v,671,395);assertEquals("SK_전사_001",TownInteriorTest.field(v,"activeSkillVisualId"));advance(v,.16f);save(render(v),"inn-horizontal");inn.assertVisibleFx(v,s.monsters().get(0).id);
  }
  @Test public void carryUsesBodyRatioAndEveryWalkFrameKeepsFourWayBlade()throws Exception{
    CharacterRenderer renderer=new CharacterRenderer(c);ChungryongWeaponRenderer weapon=new ChungryongWeaponRenderer(c);
    assertEquals(.5,weapon.manifest.getDouble("sourceScale"),.0001);
    assertEquals(43.0/51,weapon.manifest.getDouble("carryBodyRatio"),.0001);
    Bitmap sheet=Bitmap.createBitmap(640,600,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(sheet);out.drawColor(0xff403b34);int col=0;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      for(int n=0;n<5;n++){
        Bitmap b=Bitmap.createBitmap(160,120,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(b);
        CharacterRenderer.Pose p=new CharacterRenderer.Pose(80,100,d,n==0?CharacterRenderer.State.IDLE:CharacterRenderer.State.WALK,n*.14f,0,1,false,"mu0000001,mh172,ml228,ms001",ChungryongWeaponRenderer.APPEARANCE,null,CharacterRenderer.EffectFamily.NONE);
        CharacterRenderer.setPresentationWalkClock((n-1)*CharacterRenderer.WALK_FRAME_SECONDS);
        assertEquals(n,CharacterRenderer.paperDollAtlasColumn(p.state,CharacterRenderer.presentationWalkClock()));
        renderer.draw(canvas,p);if(d==CharacterRenderer.Direction.NE&&n==4)save(b,"carry-ne4-diagnostic");assertTrue("walk blade "+d+n,cyan(b)>20);out.drawBitmap(b,col*160,n*120,null);b.recycle();
      }col++;
    }CharacterRenderer.setPresentationWalkClock(0);save(sheet,"carry-all-frames");
  }
  @Test public void completeDefenseJumpsAndReturnsOnTheRealInputClock()throws Exception{
    SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);assertEquals("JUMP",catalog.get("SK_전사_014").motion);
    assertEquals(0,SkillBodyRenderer.jumpLift(0),.001);assertEquals(12,SkillBodyRenderer.jumpLift(1f/3f),.001);assertEquals(0,SkillBodyRenderer.jumpLift(1),.001);
    for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
      setup();c.getSharedPreferences("project_dark_visual_v1",0).edit().putString("body_identity",body).commit();
      GameView v=new MartialHudAutoRegressionTest(){ {c=ChungryongWeaponTest.this.c;} }.directed(direction);
      RuntimeState s=TownInteriorTest.field(v,"state");float x=s.player().x,y=s.player().y;s.rpg().equip(ChungryongWeaponRenderer.ITEM);
      SkillBook book=TownInteriorTest.field(v,"skillBook");assertTrue(book.assign(0,"SK_전사_014"));TownInteriorTest.tap(v,671,395);
      assertEquals("SK_전사_014",TownInteriorTest.field(v,"activeSkillVisualId"));
      save(render(v),"jump-start-"+body+direction);advance(v,.14f);save(render(v),"jump-contact-"+body+direction);
      assertTrue(s.skillEffects().has("player","PHYSICAL_GUARD"));assertEquals(x,s.player().x,.001);assertEquals(y,s.player().y,.001);
      advance(v,.12f);save(render(v),"jump-sphere-"+body+direction);
      Bitmap actor=Bitmap.createBitmap(180,140,Bitmap.Config.ARGB_8888);
      CharacterRenderer.Pose jumping=new CharacterRenderer.Pose(90,110,direction,CharacterRenderer.State.SKILL,0,.14f,.5f,false,null,ChungryongWeaponRenderer.APPEARANCE,null,CharacterRenderer.EffectFamily.NONE);
      assertTrue(new SkillBodyRenderer(c,catalog).draw(new Canvas(actor),jumping,body,"JUMP",1f/3f));assertTrue("equipped weapon follows jump",cyan(actor)>20);actor.recycle();
      advance(v,1);assertNull(TownInteriorTest.field(v,"activeSkillVisualId"));save(render(v),"jump-land-"+body+direction);
    }
  }
  void advance(GameView v,float seconds)throws Exception{for(float t=0;t<seconds;t+=.01f)MartialHudAutoRegressionTest.call(v,"update",float.class,Math.min(.01f,seconds-t));}
}
