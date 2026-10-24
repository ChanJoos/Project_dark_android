package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import java.io.*;
import java.lang.reflect.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CompanionNativeV269Test {
 final MobileHudV134Test h=new MobileHudV134Test();
 @Before public void setup(){h.setup();}
 GameView view(int width,int height)throws Exception{GameView v=h.view();v.layout(0,0,width,height);RuntimeState s=h.field(v,"state");s.rpg().restoreProgression(71,0);assertTrue(s.rpg().restoreJobCode("CLERIC"));s.applyDerivedGrowth();return v;}
 void tap(GameView v,float x,float y,float scale){EquipmentHudV262Test.tap(v,x*scale,y*scale);}
 void tick(GameView v,float dt)throws Exception{Method m=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);m.setAccessible(true);m.invoke(v,dt);}
 void render(GameView v,String file,int w,int height)throws Exception{Bitmap b=Bitmap.createBitmap(w,height,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File f=new File("build/reports/companion-v269/"+file+".png");f.getParentFile().mkdirs();try(FileOutputStream o=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,o);}assertNotEquals("render has content",b.getPixel(w/2,height/2),b.getPixel(0,0));b.recycle();}
 @Test public void recruitSelectSupportAndPersistThroughRealHudInBothAspectRatios()throws Exception{
  for(int[] size:new int[][]{{960,540},{2340,1080}}){h.reset();GameView v=view(size[0],size[1]);float scale=size[1]/540f,offset=(size[0]/scale-960)/2;RuntimeState s=h.field(v,"state");
   tap(v,60,213,scale);assertTrue(((CompanionWindow)h.field(v,"companionWindow")).open);tap(v,320+offset,205,scale);assertNotNull(s.party().active());assertEquals(CompanionParty.Job.WARRIOR,s.party().active().job);tap(v,320+offset,342,scale);assertEquals(s.party().active().id,s.party().allyTarget());assertFalse(((CompanionWindow)h.field(v,"companionWindow")).open);
   tick(v,.1f);render(v,"warrior-hud-"+size[0],size[0],size[1]);
   CompanionParty.Member m=s.party().active();m.body.hp=23;m.body.mp=17;v.pause();F5mSaveStore.install(h.c);GameView next=h.view();RuntimeState restored=h.field(next,"state");assertNotNull(restored.party().active());assertEquals(23,restored.party().active().body.hp);assertEquals(17,restored.party().active().body.mp);
  }
 }
 @Test public void selectedAllyReceivesActualQuickslotHealAndProtection()throws Exception{GameView v=view(960,540);RuntimeState s=h.field(v,"state");assertTrue(s.party().recruit(s,CompanionParty.Job.WARRIOR));tick(v,.1f);CompanionParty.Member m=s.party().active();m.body.x=s.player().x;m.body.y=s.player().y;m.body.hp=1;s.party().selectAlly();v.setSkillTestMode(true);SkillBook book=h.field(v,"skillBook");assertTrue(book.assign(0,"SK_성직자_001"));RectF slot=h.slot(v,0);tap(v,slot.centerX(),slot.centerY(),1);tick(v,.3f);assertTrue("real HUD heal reaches ally",m.body.hp>1);tick(v,1);assertTrue(book.assign(0,"SK_성직자_028"));tap(v,slot.centerX(),slot.centerY(),1);tick(v,.3f);assertTrue(s.skillEffects().has(m.id,"PROTECT"));render(v,"ally-heal-protect",960,540);}
 @Test public void managementOverlayConsumesTouchesAndRecoveryDoesNotChangePlayerResources()throws Exception{GameView v=view(960,540);RuntimeState s=h.field(v,"state");tap(v,60,213,1);tap(v,470,205,1);assertEquals(CompanionParty.Job.PRIEST,s.party().active().job);s.party().active().body.hp=0;s.party().active().body.alive=false;int hp=s.player().hp,mp=s.player().mp;tap(v,620,342,1);assertTrue(s.party().active().body.alive);assertEquals(hp,s.player().hp);assertEquals(mp,s.player().mp);render(v,"priest-management",960,540);}
}
