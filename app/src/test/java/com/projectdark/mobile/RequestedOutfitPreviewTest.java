package com.projectdark.mobile;

import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

/** Native visual fixtures, not combat acceptance or phone/user visual approval.
 * Uses real GameView, genuine job identity, source gear and the approved equipment sandbox.
 * Action clocks are sampled directly so previews require no synthetic HP/MP or enemy changes.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RequestedOutfitPreviewTest {
  final MineTownV203Test f=new MineTownV203Test();
  @Before public void setup(){f.reset();}
  void set(Object o,String name,Object value)throws Exception{
    Field field=o.getClass().getDeclaredField(name);field.setAccessible(true);field.set(o,value);
  }
  GameView outfit(boolean mage)throws Exception{
    GameView v=f.town();RuntimeState s=f.field(v,"state");RpgProgressionState r=s.rpg();
    assertEquals(1,r.normalLevel().intValue());
    // Reproducible test-only XP awards execute the normal level-growth code, never a base HP override.
    r.restoreGrowthRandomState(20261024L);
    while(r.normalLevel()<3)r.grantAdaptedReward(LevelExpCurve.requiredForNext(r.normalLevel()),0);
    assertTrue(r.chooseInitialJob(mage?"MAGE":"ROGUE"));
    while(r.normalLevel()<99)r.grantAdaptedReward(LevelExpCurve.requiredForNext(r.normalLevel()),0);
    assertEquals(196,r.statPoints()); // Unallocated conservative test character, not an optimized build.
    assertTrue(r.baseMaxHp()>=4510&&r.baseMaxHp()<=5490);
    assertTrue(r.baseMaxMp()>=4510&&r.baseMaxMp()<=5490);
    r.enableEquipmentSandbox(true);
    for(String slot:new ArrayList<>(r.equipment().keySet()))r.unequip(slot);
    String[] ids=mage?new String[]{"USER_ITEM_20261018_R01714","USER_ITEM_20261018_R01712",RequestedWeaponSourceFacts.ROO_GRACE}:
      new String[]{"USER_ITEM_20261018_R01486","USER_ITEM_20261018_R01485",RequestedWeaponSourceFacts.SHINWOLDO,"IT_SHIELD_KNIGHT"};
    String[] art=mage?new String[]{"mu0000372","mh391","mw078"}:new String[]{"mu0000037","mh128","mw103","ms008"};
    for(int i=0;i<ids.length;i++){
      assertEquals(ids[i],art[i],r.itemDefinitions().get(ids[i]).appearanceId);
      assertEquals(ids[i],RpgProgressionState.EquipResult.EQUIPPED,r.equip(ids[i]));
      assertEquals(ids[i],r.equippedDefinition(r.itemDefinitions().get(ids[i]).equipSlot).itemId);
    }
    s.applyDerivedGrowth();
    assertEquals(r.finalStats().maxHp,s.player().maxHp);
    assertEquals(r.finalStats().maxMp,s.player().maxMp);
    // Preview starts rested at derived maxima; no save file or global balance is changed.
    s.player().hp=s.player().maxHp;s.player().mp=s.player().maxMp;
    // Use the actual mine manager travel card, including the candidate's arrival policy.
    r.restoreGold(100); // Fixture funds the existing travel fee, not combat stats.
    f.approach(v,"mine_town_travel");RectF card=TownTravelWindow.cardFor(MineTownGeometry.ID,MineHuntGeometry.ID);
    assertNotNull(card);f.tap(v,card.centerX(),card.centerY());assertEquals(MineHuntGeometry.ID,s.currentMapId());
    assertEquals(mage?"MAGE":"ROGUE",r.currentJobCode());
    return v;
  }
  void save(Bitmap bitmap,String name)throws Exception{
    File target=new File("build/reports/horror-v179/requested-outfits/"+name+".png");
    assertTrue(target.getParentFile().isDirectory()||target.getParentFile().mkdirs());
    try(FileOutputStream out=new FileOutputStream(target)){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));}
    bitmap.recycle();
  }
  void screen(GameView v,String name)throws Exception{
    Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));save(b,name);
  }
  @SuppressWarnings({"unchecked","rawtypes"})
  void pose(GameView v,String action,float clock,String skill)throws Exception{
    Field field=GameView.class.getDeclaredField("action");field.setAccessible(true);
    field.set(v,Enum.valueOf((Class)field.getType(),action));set(v,"actionClock",clock);
    set(v,"activeSkillVisualId",skill);set(v,"activeSkillCastSeconds",.24f);
  }
  @Test public void genuineJobsRenderRequestedOutfitsInventoryAndActionSamples()throws Exception{
    for(boolean mage:new boolean[]{false,true}){
      f.reset();GameView v=outfit(mage);String prefix=mage?"mage":"rogue";
      RuntimeState state=f.field(v,"state");
      writeResources(state,prefix);
      screen(v,prefix+"-mine-arrival");
      ItemWindowReferenceTest.tap(v,742,28);
      assertTrue((Boolean)f.field(v,"equipmentOpen"));assertTrue((Boolean)f.field(v,"inventoryOpen"));
      screen(v,prefix+"-equipment-inventory");
      set(v,"equipmentOpen",false);set(v,"inventoryOpen",false);
      Bitmap sheet=Bitmap.createBitmap(1200,900,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);
      canvas.drawColor(0xff252a30);Paint label=new Paint();label.setColor(Color.WHITE);label.setTextSize(14);
      int col=0;
      for(CharacterRenderer.Direction dir:CharacterRenderer.Direction.values()){
        CanonicalActorFacing facing=f.field(v,"playerFacing");facing.endAttack();facing.setLocomotion(dir);
        for(int row=0;row<3;row++){
          // Actual renderer route, sampled preparation/contact/recovery poses; no cast acceptance claim.
          pose(v,mage?"CAST":"SWING",new float[]{.01f,.24f,.40f}[row],mage?"SK_마법사_001":null);
          Bitmap actor=Bitmap.createBitmap(300,280,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(actor);
          c.translate(150-v.renderedPlayerWorldX(),230-v.renderedPlayerWorldY());
          Method draw=GameView.class.getDeclaredMethod("drawVisibleCharacter",Canvas.class);draw.setAccessible(true);draw.invoke(v,c);
          int visible=0;for(int y=0;y<280;y++)for(int x=0;x<300;x++)if(Color.alpha(actor.getPixel(x,y))>0)visible++;
          assertTrue(prefix+" "+dir+" phase "+row+" visible actor",visible>100);
          canvas.drawBitmap(actor,col*300,row*300,null);actor.recycle();
          canvas.drawText(dir+" / sampled phase "+row,col*300+10,row*300+292,label);
        }col++;
      }
      save(sheet,prefix+"-native-action-samples");
      assertEquals(mage?"MAGE":"ROGUE",((RuntimeState)f.field(v,"state")).rpg().currentJobCode());
    }
  }
  void writeResources(RuntimeState state,String prefix)throws Exception{
    RpgProgressionState r=state.rpg();
    org.json.JSONObject data=new org.json.JSONObject().put("job",r.currentJobCode()).put("level",r.normalLevel())
      .put("baseHp",r.baseMaxHp()).put("baseMp",r.baseMaxMp()).put("hp",state.player().hp).put("mp",state.player().mp)
      .put("maxHp",state.player().maxHp).put("maxMp",state.player().maxMp).put("unspentPoints",r.statPoints())
      .put("equipment",new org.json.JSONObject(r.equipment())).put("setup","Test-only normal XP growth, seed20261024; unallocated stats; exact equipment sandbox; rested. Not a player save or recorded gameplay.");
    File target=new File("build/reports/horror-v179/requested-outfits/"+prefix+"-resources.json");target.getParentFile().mkdirs();
    try(FileWriter writer=new FileWriter(target)){writer.write(data.toString(2));}
  }
  @Test public void grownCharactersRetainResourcesAndSurviveTenStrongestPhysicalBudgetHits()throws Exception{
    for(boolean mage:new boolean[]{false,true}){
      f.reset();GameView v=outfit(mage);RuntimeState s=f.field(v,"state");
      int worst=0;for(AbelCombatProfile p:MineHuntMonsters.all()){
        int hit=CombatStatPipeline.resolve(p.damage,CombatStatPipeline.Channel.PHYSICAL,p.stats(),s.rpg().finalStats()).applied;
        worst=Math.max(worst,hit);
      }
      assertTrue(worst>0);int start=s.player().hp;
      // Budget check through real defense and HP debit, not an AI/combat-timing acceptance test.
      for(int n=0;n<10;n++)s.damagePlayer(worst);
      assertTrue("ten strongest physical hits remain survivable",s.player().alive);
      assertEquals(start-worst*10,s.player().hp);
      assertEquals(s.player().maxMp,s.player().mp);
    }
  }
  @Test public void sparseShinwoldoSourceRemainsSparseWithoutFabricatedFrame(){
    EquipmentVisualRegistry.Visual v=new EquipmentVisualRegistry(f.c).get("mw103");assertNotNull(v);assertNotNull(v.registration);
    assertNull("original absent idle frame must not be fabricated",v.registration.idleByIndex(9));
    assertNull("original absent contact frame must not be fabricated",v.registration.action(2));
    assertNotNull(v.registration.action(1));assertNotNull(v.registration.action(3));
  }
}
