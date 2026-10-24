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
    r.restoreProgression(99,0);assertTrue(r.restoreJobCode(mage?"MAGE":"ROGUE"));
    r.enableEquipmentSandbox(true);
    for(String slot:new ArrayList<>(r.equipment().keySet()))r.unequip(slot);
    String[] ids=mage?new String[]{"USER_ITEM_20261018_R01714","USER_ITEM_20261018_R01712",RequestedWeaponSourceFacts.ROO_GRACE}:
      new String[]{"USER_ITEM_20261018_R01486",RequestedWeaponSourceFacts.SHINWOLDO,"IT_SHIELD_KNIGHT"};
    String[] art=mage?new String[]{"mu0000372","mh391","mw078"}:new String[]{"mu0000037","mw103","ms008"};
    for(int i=0;i<ids.length;i++){
      assertEquals(ids[i],art[i],r.itemDefinitions().get(ids[i]).appearanceId);
      assertEquals(ids[i],RpgProgressionState.EquipResult.EQUIPPED,r.equip(ids[i]));
      assertEquals(ids[i],r.equippedDefinition(r.itemDefinitions().get(ids[i]).equipSlot).itemId);
    }
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
  @Test public void sparseShinwoldoSourceRemainsSparseWithoutFabricatedFrame(){
    EquipmentVisualRegistry.Visual v=new EquipmentVisualRegistry(f.c).get("mw103");assertNotNull(v);assertNotNull(v.registration);
    assertNull("original absent idle frame must not be fabricated",v.registration.idleByIndex(9));
    assertNull("original absent contact frame must not be fabricated",v.registration.action(2));
    assertNotNull(v.registration.action(1));assertNotNull(v.registration.action(3));
  }
}
