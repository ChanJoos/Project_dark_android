package com.projectdark.mobile;
import com.projectdark.mobile.world.WorldRuntimeAdapter;import android.content.*;import android.graphics.*;import java.io.*;import java.util.*;import org.junit.*;import static org.junit.Assert.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class VisualFeedbackV250Test {
 Context c;@Before public void setup(){new MineTownV203Test().reset();c=RuntimeEnvironment.getApplication();}
 static void save(Bitmap b,String name)throws Exception{File f=new File("build/reports/horror-v179/v250/"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream o=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,o));}}
 @Test public void bothTravelStatesHaveCleanSymmetricCapsAndActualMenuUsesThem()throws Exception{
  SourceTravelSkin skin=new SourceTravelSkin(c);Bitmap sheet=Bitmap.createBitmap(600,180,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);
  for(String name:new String[]{"choice-selected","choice-idle"}){Bitmap b;try(InputStream in=c.getAssets().open("town-v248/"+name+".png")){b=BitmapFactory.decodeStream(in);}for(int y=0;y<b.getHeight();y++)for(int x=0;x<9;x++)assertEquals(name+" cap",b.getPixel(x,y),b.getPixel(b.getWidth()-1-x,y));skin.frame(canvas,name,new RectF(8,name.endsWith("selected")?8:70,264,name.endsWith("selected")?45:107));}
  save(sheet,"travel-caps");GameView v=new GameView(c);v.layout(0,0,960,540);TownTravelWindow.install(c);Bitmap menu=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(menu));TownTravelWindow.draw(new Canvas(menu),WorldDef.ID,"pote_travel_guide");save(menu,"actual-travel-menu");
 }
 @Test public void rapidAndWingLayersAreDisjointForBothSlotsBodiesAndDirections(){
  for(String body:new String[]{"mm001","wm001"})for(String slot:new String[]{"장식",RpgProgressionState.SECOND_ORNAMENT_SLOT})for(String item:new String[]{"USER_ITEM_20261018_R05141","USER_ITEM_20261018_R05145","USER_ITEM_20261018_R05143","USER_ITEM_20261018_R05142"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   String ref=RapidHairclipV199.appearance(item,slot);List<String> ids=new ArrayList<>();ids.addAll(RapidHairclipV199.layers(ref,body,d,true));ids.addAll(RapidHairclipV199.layers(ref,body,d,false));assertEquals(1,ids.size());assertFalse("Rapid is drawn only by its own depth route",WingShieldV215.layer(ids.get(0)));
  }assertTrue(WingShieldV215.layer("mg261"));assertTrue(WingShieldV215.layer("mc261"));
 }
 @Test public void templeRetainsWhiteLightAndStillDrawsSharedPortal()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=TownInteriorTest.field(v,"state");WorldRuntimeAdapter world=TownInteriorTest.field(v,"worldAdapter");state.player().x=TempleEntranceRenderer.X-128;state.player().y=TempleEntranceRenderer.Y+96;world.snapCameraToPlayer();Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);int draws=PortalAnimation.mapDraws;v.draw(new Canvas(b));assertEquals(draws+1,PortalAnimation.mapDraws);com.projectdark.mobile.world.WorldCameraTransform.Point point=world.worldToScreen(TempleEntranceRenderer.X+32,TempleEntranceRenderer.Y-32);assertTrue("white beam survives alongside animated portal",Color.blue(b.getPixel(Math.round(point.x),Math.round(point.y)))>=90);save(b,"temple-preserved-white-light");
 }
 @Test public void reducingOnePixelStripesPreservesTheirCoverageInsteadOfDroppingAll(){
  Bitmap source=Bitmap.createBitmap(86,86,Bitmap.Config.ARGB_8888);for(int y=0;y<86;y++)for(int x=1;x<86;x+=2)source.setPixel(x,y,Color.WHITE);Bitmap out=Bitmap.createBitmap(43,43,Bitmap.Config.ARGB_8888);SourceItemIconRegistry.draw(new Canvas(out),source,new RectF(0,0,43,43),new Paint());int alpha=Color.alpha(out.getPixel(20,20));assertTrue("half coverage="+alpha,alpha>=110&&alpha<=145);
 }
}
