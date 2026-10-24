package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;import org.json.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MagePromotionV241Test {
 final MageSourcesV235Test m=new MageSourcesV235Test();
 @Before public void setup(){m.setup();}
 @Test public void allOriginalPixelsRenderOutsideOneTileWithoutLossAtEveryFrame()throws Exception{
  ClassicSkillReference r=new ClassicSkillReference(m.h.c);JSONObject entries=m.doc("skill-presentation/mage-v241/manifest.json").getJSONObject("skills");long bytes=0;int crossing=0;
  for(int n:new int[]{39,40,41,42,45,46,51,52}){
   String id=m.mage(n);ClassicSkillReference.Channel ch=r.channel(id,false);CapturedSkillFx.Sequence q=ch.sequence;JSONObject raw=entries.getJSONObject(id).getJSONObject("channels").getJSONObject("RECIPIENT_CONTACT");assertEquals(raw.getInt("width"),q.width);assertTrue(q.normalBlend);assertEquals(1f,q.scale,0f);assertNull(r.mageDisplayAtlas(ch));assertEquals(0f,q.footOffsetY,0f);bytes+=q.atlas.getAllocationByteCount();
   Bitmap rendered=Bitmap.createBitmap(700,700,Bitmap.Config.ARGB_8888);int elapsed=0;boolean crosses=false;
   for(int f=0;f<q.durations.length;f++){
    rendered.eraseColor(Color.TRANSPARENT);r.draw(new Canvas(rendered),ch,(elapsed+1)/1000f,350,350);int sx=f%q.columns*q.width,sy=f/q.columns*q.height,dx=350-(int)q.pivotX,dy=350-(int)q.pivotY;
    for(int y=0;y<q.height;y++)for(int x=0;x<q.width;x++){
     int source=q.atlas.getPixel(sx+x,sy+y);assertEquals(id+" frame"+f+" pixel "+x+","+y,source,rendered.getPixel(dx+x,dy+y));
     if(Color.alpha(source)>0&&(Math.abs(dx+x-350)>32||Math.abs(dy+y-350)>16))crosses=true;
     if(x<2||y<2||x>=q.width-2||y>=q.height-2)assertEquals(id+" sampling guard",0,Color.alpha(source));
    }elapsed+=q.durations[f];
   }assertTrue(id+" complete effect spans neighbouring world tiles",crosses);crossing++;
  }assertEquals(8,crossing);assertTrue("Original eight atlas budget "+bytes,bytes<11000000L);
 }
 @Test public void originalDarkSealRetainsCurse70AndAllPromotionQuickslotsReachExpectedRecipients()throws Exception{
  m.allFourteenPromotionSpellsReachNormalQuickslotContactWithSourceEffectsAndCorrectAnchors();
  m.setup();String id=m.mage(46);GameView v=m.actor(id);RuntimeState s=m.h.field(v,"state");int mp=s.player().mp;m.tap(v);RuntimeCombatSession session=m.h.field(v,"combatSession");m.contact(v);assertEquals(mp-session.effectiveMpCost(id),s.player().mp);assertEquals(70,s.skillEffects().power(s.monsters().get(0).id,"CURSE"));
  SkillVfxRenderer fx=m.h.field(v,"skillVfx");for(SkillVfxRenderer.Pulse p:fx.pulses)if(!p.caster&&p.id.equals(id))p.age=.6f;m.h.capture(v,"v241-dark-seal-live-contact");
 }
 @Test public void nativeCompleteSequenceGridAtWorldScale()throws Exception{
  ClassicSkillReference r=new ClassicSkillReference(m.h.c);Bitmap b=Bitmap.createBitmap(1280,1600,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);c.drawColor(0xff4c514a);int row=0;
  for(int n:new int[]{39,40,41,42,45,46,51,52}){ClassicSkillReference.Channel ch=r.channel(m.mage(n),false);for(int f=0;f<8;f++)r.draw(c,ch,ch.sequence.duration*f/8,80+f*160,180+row*195);row++;}RogueRepairV232Test.save(b,"v241-eight-complete-original-sequences");
 }
}
