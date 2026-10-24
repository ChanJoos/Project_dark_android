package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import java.io.*;
import java.lang.reflect.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ContactPoseV264Test {
 final MartialHudAutoRegressionTest h=new MartialHudAutoRegressionTest();
 @Before public void setup(){h.setup();}
 Bitmap body(SkillBodyRenderer r,String identity,CharacterRenderer.Direction direction,String gear,String weapon){Bitmap b=Bitmap.createBitmap(301,301,Bitmap.Config.ARGB_8888);assertTrue(r.draw(new Canvas(b),new CharacterRenderer.Pose(150.5f,230,direction,CharacterRenderer.State.ATTACK,0,.19f,.45f,false,gear,weapon,null,CharacterRenderer.EffectFamily.NONE,AnimationAction.SWING),identity,"SWING",.5f));return b;}
 void save(Bitmap b,String name)throws Exception{File f=new File("build/reports/horror-v179/v264/"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
 int[] pixels(Bitmap b){int[] p=new int[b.getWidth()*b.getHeight()];b.getPixels(p,0,b.getWidth(),0,0,b.getWidth(),b.getHeight());return p;}
 @Test public void westSwingUsesTheOriginalContactPixelsMirroredTowardItsTarget()throws Exception{
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(h.c);SkillBodyRenderer r=new SkillBodyRenderer(h.c,catalog);Bitmap actual=body(r,"mm001",CharacterRenderer.Direction.SW,null,null),expected=Bitmap.createBitmap(301,301,Bitmap.Config.ARGB_8888);JSONObject f=catalog.frames.getJSONObject("mm001/02/3");Bitmap source=BitmapFactory.decodeStream(h.c.getAssets().open("skill-presentation/"+f.getString("path")));Canvas c=new Canvas(expected);float scale=SkillBodyRenderer.SCALE,x=150.5f,y=230+8*scale;float left=x-(float)f.getDouble("pivotX")*scale,top=y-(source.getHeight()+(float)f.getDouble("offsetY"))*scale;c.scale(-1,1,x,y);Paint ink=new Paint();ink.setFilterBitmap(false);c.drawBitmap(source,null,new RectF(left,top,left+source.getWidth()*scale,top+source.getHeight()*scale),ink);save(actual,"actual-west");save(expected,"source-contact-west");assertArrayEquals("visible west contact must use mirrored original contact pixels",pixels(expected),pixels(actual));
 }
 @Test public void bothBodiesAndAllFourDirectionsShareContactPhaseAndMirrorAllWornLayers()throws Exception{
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(h.c);SkillBodyRenderer r=new SkillBodyRenderer(h.c,catalog);
  for(String body:new String[]{"mm001","wm001"})for(boolean back:new boolean[]{false,true}){CharacterRenderer.Direction east=back?CharacterRenderer.Direction.NE:CharacterRenderer.Direction.SE,west=back?CharacterRenderer.Direction.NW:CharacterRenderer.Direction.SW;
   for(String gear:new String[]{"","mu0000001,mh172,ml228,ms001"}){Bitmap a=body(r,body,east,gear,"mw001"),b=body(r,body,west,gear,"mw001");int[] p=pixels(a),q=pixels(b);int different=0,visible=0;for(int y=0;y<301;y++)for(int x=0;x<301;x++){int i=y*301+x,j=y*301+300-x;if((p[i]>>>24)>0||(q[j]>>>24)>0){visible++;if(p[i]!=q[j])different++;}}assertTrue("real visible body/layers",visible>100);assertTrue(body+" "+west+" mirror mismatch "+different+"/"+visible,different<visible*.025f);save(a,body+"-"+east+"-"+gear.length());save(b,body+"-"+west+"-"+gear.length());}
  }
 }
 Bitmap actor(GameView v)throws Exception{RuntimeState s=h.field(v,"state");Bitmap b=Bitmap.createBitmap(301,301,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);c.translate(150.5f-v.renderedPlayerWorldX(),230-v.renderedPlayerWorldY());Method draw=GameView.class.getDeclaredMethod("drawVisibleCharacter",Canvas.class);draw.setAccessible(true);draw.invoke(v,c);return b;}
 @Test public void realManualAndAutoContactsKeepDamageAndFacingWhileRenderingBothSides()throws Exception{
  for(boolean auto:new boolean[]{false,true})for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){GameView v=h.directed(direction);RuntimeState s=h.field(v,"state");RuntimeState.Monster target=((CombatController)h.field(v,"combat")).target();int hp=target.hp;
   if(auto){SkillBook book=h.field(v,"skillBook");for(int i=0;i<12;i++)book.clearSlot(i);assertTrue(book.assign(0,"SK_공통_001"));HudTestControlsV267.auto(v);h.advance(v,.001f);}else{Method attack=GameView.class.getDeclaredMethod("attack");attack.setAccessible(true);attack.invoke(v);}
   h.set(v,"autoAttackEnabled",false);h.advance(v,.19f);assertEquals(direction,((CanonicalActorFacing)h.field(v,"playerFacing")).presentation());assertTrue("actual accepted contact",target.hp<hp);Bitmap b=actor(v);save(b,(auto?"auto-":"manual-")+direction);SkillPresentationCatalog catalog=h.field(v,"skillPresentation");boolean back=direction==CharacterRenderer.Direction.NW||direction==CharacterRenderer.Direction.NE;assertEquals("same contact pose regardless of left/right","mm001/02/"+(back?1:3),catalog.frameKey("mm001","SWING",direction,.5f));
  }
 }
}
