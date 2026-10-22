package com.projectdark.mobile;
import static org.junit.Assert.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
import android.graphics.*;import org.json.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MageVisualV237Test {
 final ScreenMagicV201Test h=new ScreenMagicV201Test();
 @Before public void setup(){h.setup();}
 @Test public void helphuraRetiresAndOldSaveSlotsAreCleanedPraboLivesInFifthCircle()throws Exception{
  SkillBook b=SkillBook.load(h.c);assertEquals("5",b.get("SK_마법사_053").circle);assertEquals(5,SkillAbilityCatalog.get("SK_마법사_053").circle);assertTrue(SkillPolicyV227.retired("SK_마법사_057"));assertFalse(b.learn("SK_마법사_057",100));assertFalse(b.previewable("SK_마법사_057"));assertFalse(b.list(true,false).stream().anyMatch(e->e.id.equals("SK_마법사_057")));
  JSONObject save=b.snapshot();save.getJSONObject("learned").put("SK_마법사_057",100);save.getJSONArray("slots").put(0,"SK_마법사_057");assertTrue(b.restore(save));assertFalse(b.learned("SK_마법사_057"));assertFalse(b.snapshot().getJSONObject("learned").has("SK_마법사_057"));assertTrue(b.snapshot().getJSONArray("slots").isNull(0));
 }
 @Test public void eightPromotionsRemoveRecordedActorMasksWithoutRepaintingSourceColorsAndRenderNative()throws Exception{
  ClassicSkillReference r=new ClassicSkillReference(h.c);Bitmap grid=Bitmap.createBitmap(1200,600,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(grid);c.drawColor(0xff666b62);int i=0;
  for(String n:new String[]{"039","040","041","042","044","045","051","052"}){ClassicSkillReference.Channel ch=r.channel("SK_마법사_"+n,false);Bitmap src=ch.sequence.atlas,clean=r.mageDisplayAtlas(ch);assertNotNull(n,clean);int removed=0,retained=0;for(int y=0;y<src.getHeight();y++)for(int x=0;x<src.getWidth();x++){int a=src.getPixel(x,y),b=clean.getPixel(x,y);if(Color.alpha(a)>0&&Color.alpha(b)==0)removed++;if(Color.alpha(b)>0){assertEquals(n,a,b);retained++;}}assertTrue(n+" source pixels retained",retained>20);assertTrue(n+" actor/noise removed",removed>0);int xx=150+(i%4)*300,yy=145+(i/4)*290;for(int k=0;k<ch.sequence.durations.length;k++)r.draw(c,ch,k*.1f+.05f,xx,yy);i++;}
  RogueRepairV232Test.save(grid,"v237-promotion-clean-source-grid");assertNull(r.mageDisplayAtlas(r.channel("SK_마법사_001",false)));
 }
 @Test public void darkMarkIsActorFreeGeometryAcrossRevealHoldFadeAndRealContactsKeepCurse()throws Exception{
  Bitmap out=Bitmap.createBitmap(720,240,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(out);c.drawColor(0xffa8a995);MageDarkMarkV237 fx=new MageDarkMarkV237();for(int i=0;i<6;i++)fx.draw(c,.04f+i*.09f,.6f,60+i*120,125);RogueRepairV232Test.save(out,"v237-darkmark-rebuilt-phases");
  MageSourcesV235Test m=new MageSourcesV235Test();m.setup();GameView v=m.actor("SK_마법사_046");RuntimeState s=m.h.field(v,"state");m.tap(v);m.contact(v);assertEquals(70,s.skillEffects().power(s.monsters().get(0).id,"CURSE"));m.h.capture(v,"v237-darkmark-normal-contact");
 }
}
