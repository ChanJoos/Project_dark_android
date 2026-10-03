package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.lang.reflect.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DaraRecipientFxTest {
  Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  GameView start(CharacterRenderer.Direction d)throws Exception{
    setup();GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);
    RuntimeState s=TownInteriorTest.field(v,"state");RuntimeState.Monster m=s.monsters().get(0);
    for(RuntimeState.Monster other:s.monsters())other.attackCooldown=100;
    s.player().hp=100;s.player().mp=90;m.hp=1000;s.skillEffects().put(m.id,"qa","ROOT",1,60);
    float dx=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW?-32:32;
    float dy=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE?-16:16;
    s.player().x=m.x-dx;s.player().y=m.y-dy;
    ((CombatController)TownInteriorTest.field(v,"combat")).selectTarget(m);
    ((com.projectdark.mobile.world.WorldRuntimeAdapter)TownInteriorTest.field(v,"worldAdapter")).snapCameraToPlayer();
    ((SkillBook)TownInteriorTest.field(v,"skillBook")).assign(0,"SK_무도가_020");return v;
  }
  void use(GameView v)throws Exception{TownInteriorTest.tap(v,671,395);assertEquals("SK_무도가_020",TownInteriorTest.field(v,"activeSkillVisualId"));}
  void tick(GameView v,float seconds)throws Exception{for(float t=0;t<seconds;t+=.01f)MartialHudAutoRegressionTest.call(v,"update",float.class,Math.min(.01f,seconds-t));}
  boolean source(SkillVfxRenderer.Pulse p){return p.id.equals("SK_무도가_020")&&p.sheet.equals("classic")&&!p.caster;}
  void visible(GameView v,RuntimeState.Monster m,String name)throws Exception{
    SkillVfxRenderer fx=TownInteriorTest.field(v,"skillVfx");assertEquals(1,fx.pulses.stream().filter(this::source).count());
    assertTrue(fx.pulses.stream().anyMatch(p->source(p)&&p.anchor.equals(m.id)));
    assertFalse("zero damage must not create damage impact",fx.pulses.stream().anyMatch(p->p.sheet.equals("impact")));
    tick(v,.15f);
    SkillVfxRenderer.Anchors anchors=TownInteriorTest.field(v,"skillAnchors");
    Bitmap effect=Bitmap.createBitmap(320,240,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(effect);
    canvas.translate(160-anchors.centerX(m.id),120-anchors.centerY(m.id));fx.draw(canvas,anchors);
    int pixels=0;for(int y=0;y<240;y++)for(int x=0;x<320;x++)if(Color.alpha(effect.getPixel(x,y))>12)pixels++;
    assertTrue("source burst has rendered pixels at actual recipient",pixels>1000);
    Bitmap scene=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(scene));
    File f=new File("build/reports/device-review/v95-dara-"+name+".png");f.getParentFile().mkdirs();
    try(FileOutputStream out=new FileOutputStream(f)){assertTrue(scene.compress(Bitmap.CompressFormat.PNG,100,out));}
    scene.recycle();effect.recycle();tick(v,.6f);assertFalse(fx.pulses.stream().anyMatch(this::source));
  }
  @Test public void lowResourceQuickslotReleaseIsVisibleOnRecipientInFourDirections()throws Exception{
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      GameView v=start(d);RuntimeState s=TownInteriorTest.field(v,"state");RuntimeState.Monster m=s.monsters().get(0);use(v);
      tick(v,2.99f);assertEquals(1000,m.hp);assertFalse(((SkillVfxRenderer)TownInteriorTest.field(v,"skillVfx")).pulses.stream().anyMatch(this::source));
      tick(v,.02f);assertEquals("formula remains zero",1000,m.hp);assertEquals(100,s.player().hp);assertEquals(90,s.player().mp);
      visible(v,m,d.toString());
    }
  }
  @Test public void lowResourceInnReleaseUsesActualMouseAnchor()throws Exception{
    setup();InnDetailQuestFxTest inn=new InnDetailQuestFxTest();inn.c=c;GameView v=inn.start();inn.place(v);
    v.setSkillTestMode(true);RuntimeState s=TownInteriorTest.field(v,"state");s.player().hp=100;s.player().mp=90;
    for(RuntimeState.Monster other:s.monsters())other.attackCooldown=100;
    RuntimeState.Monster m=s.monsters().get(0);int hp=m.hp;
    ((SkillBook)TownInteriorTest.field(v,"skillBook")).assign(0,"SK_무도가_020");use(v);tick(v,3.01f);
    assertEquals(hp,m.hp);visible(v,m,"inn");
  }
  @Test public void cancelledReleaseAndNormalMissDoNotEmitTestBurst()throws Exception{
    GameView v=start(CharacterRenderer.Direction.SE);RuntimeState s=TownInteriorTest.field(v,"state");use(v);s.monsters().get(0).alive=false;tick(v,3.01f);
    assertFalse(((SkillVfxRenderer)TownInteriorTest.field(v,"skillVfx")).pulses.stream().anyMatch(this::source));
    v=start(CharacterRenderer.Direction.SE);((SkillVfxRenderer)TownInteriorTest.field(v,"skillVfx")).testAccess=false;use(v);tick(v,3.01f);
    assertFalse(((SkillVfxRenderer)TownInteriorTest.field(v,"skillVfx")).pulses.stream().anyMatch(this::source));
  }
}
