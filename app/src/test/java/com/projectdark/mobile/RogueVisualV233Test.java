package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import org.junit.*;import org.junit.runner.RunWith;
import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RogueVisualV233Test {
 final ScreenMagicV201Test h=new ScreenMagicV201Test();
 @Before public void setup(){h.setup();}
 @Test public void stabUsesSameBodyAndOriginalRecipientFxWithoutFallbackCasterSlash()throws Exception{
  SkillPresentationCatalog cat=new SkillPresentationCatalog(h.c);ClassicSkillReference ref=new ClassicSkillReference(h.c,"rogue");
  for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(float phase:new float[]{0,.17f,1f/3f,.75f})assertEquals(cat.frameKey(body,cat.get("SK_도적_007").motion,d,phase),cat.frameKey(body,cat.get("SK_도적_001").motion,d,phase));
  assertTrue(ref.channel("SK_도적_001",false).sequence.atlas.sameAs(ref.channel("SK_도적_007",false).sequence.atlas));
  SkillFeedbackV231Test fixture=new SkillFeedbackV231Test();fixture.h.setup();GameView v=fixture.actor("SK_도적_001",false);RuntimeState s=h.field(v,"state");RuntimeCombatSession session=h.field(v,"combatSession");SkillVfxRenderer fx=h.field(v,"skillVfx");h.call(v,"tickSkillCombat",new Class<?>[]{float.class},0f);assertTrue(fx.pulses.isEmpty());assertEquals("SK_도적_001",h.field(v,"activeSkillVisualId"));h.call(v,"tickSkillCombat",new Class<?>[]{float.class},session.effectiveCastSeconds("SK_도적_001")+.001f);assertTrue(s.monsters().get(0).hp<s.monsters().get(0).maxHp);assertTrue(fx.pulses.stream().anyMatch(p->p.id.equals("SK_도적_001")&&p.sheet.equals("rogue")&&!p.caster));assertFalse(fx.pulses.stream().anyMatch(p->p.caster));h.capture(v,"v233-stab-original-contact");
 }
 @Test public void restoredFlareLeftRaysAndSphereCoreSurviveNativeDrawAtEveryFrame()throws Exception{
  ClassicSkillReference ref=new ClassicSkillReference(h.c,"rogue");Bitmap grid=Bitmap.createBitmap(960,960,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(grid);out.drawColor(0xff303930);int row=0;
  for(String n:new String[]{"007","014","021","022","023","028","029","013","016"}){
   ClassicSkillReference.Channel ch=ref.channel("SK_도적_"+n,false);CapturedSkillFx.Sequence s=ch.sequence;int restored=0;int oldLeft=n.equals("007")?77:n.equals("014")?78:79;
   for(int frame=0;frame<s.durations.length;frame++)for(int y=0;y<s.height;y++)for(int x=0;x<oldLeft;x++)if(Color.alpha(s.atlas.getPixel(frame%s.columns*s.width+x,frame/s.columns*s.height+y))>24)restored++;
   if(n.equals("007")||n.equals("014")||n.equals("021")||n.equals("022"))assertTrue(n+" restored rays left of obsolete crop",restored>150);
   float age=0;for(int f=0;f<s.durations.length;f++){if(f<5)ref.draw(out,ch,age+.001f,90+f*180,row*100+90);age+=s.durations[f]/1000f;}row++;
  }RogueRepairV232Test.save(grid,"v233-full-source-fx-all-frames");
  CapturedSkillFx cap=new CapturedSkillFx(h.c);for(String id:new String[]{"SK_도적_020","SK_도적_027"})assertEquals(.52f,cap.get(id).scale,.001f);
  CapturedSkillFx.Sequence sphere=cap.get("SK_도적_020");int core=0;for(int y=60;y<175;y++)for(int x=65;x<110;x++)if(Color.alpha(sphere.atlas.getPixel(10%sphere.columns*sphere.width+x,10/sphere.columns*sphere.height+y))>100)core++;assertTrue("old actor polygon must not remove actual white sphere",core>3500);
  Bitmap b=Bitmap.createBitmap(800,260,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);c.drawColor(0xff303930);Paint p=new Paint();for(int i=0;i<5;i++)cap.draw(c,p,"SK_도적_020",new float[]{.15f,.42f,.57f,.9f,1.2f}[i],i*155+55,190);RogueRepairV232Test.save(b,"v233-assassin-sphere-size-and-core");
 }
 @Test public void hideHasBrownBackgroundWithoutWhiteCaptureStripOnBothSurfaces()throws Exception{
  SkillIconCatalog icons=new SkillIconCatalog(h.c);Bitmap grid=Bitmap.createBitmap(240,120,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(grid);out.drawColor(0xff303930);
  for(boolean quick:new boolean[]{false,true}){Bitmap b=Bitmap.createBitmap(40,40,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);if(quick)assertTrue(icons.drawQuickslot(c,"SK_도적_011",new RectF(0,0,40,40)));else assertTrue(icons.drawSquare(c,"SK_도적_011",new RectF(0,0,40,40)));int warm=0;for(int y=0;y<40;y++)for(int x=0;x<40;x++){int color=b.getPixel(x,y);assertFalse("white capture strip",y>=32&&Color.red(color)>220&&Color.green(color)>220&&Color.blue(color)>220);if(Color.red(color)>Color.blue(color)+15)warm++;}assertTrue(warm>700);out.drawBitmap(b,null,new Rect(quick?120:0,0,quick?240:120,120),null);}RogueRepairV232Test.save(grid,"v233-hide-book-and-quickslot");
 }
}
