package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
import java.nio.file.Files;import java.nio.file.Path;import java.util.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class TownPolishV165Test {
 Context c;
 @Before public void reset(){c=RuntimeEnvironment.getApplication();for(String p:new String[]{"project_dark_f5m_v1","project_dark_skill_book_v1","project_dark_skill_test_v1"})c.getSharedPreferences(p,0).edit().clear().commit();F5mSaveStore.install(c);}
 void save(Bitmap b,String name)throws Exception{Path f=Path.of("build/reports/town-v165/"+name+".png");Files.createDirectories(f.getParent());try(java.io.OutputStream out=Files.newOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
 @Test public void exactThreeDestinationsAndShortHuntingCopyWithoutPietOrGuideDescription(){
  assertEquals(Arrays.asList(AbelTownGeometry.ID,RucesionTownGeometry.ID,TownTravelWindow.FOREST),TownTravelWindow.destinations(WorldDef.ID));
  assertEquals("아벨 마을",TownTravelWindow.title(AbelTownGeometry.ID));assertEquals("뤼케시온 마을",TownTravelWindow.title(RucesionTownGeometry.ID));assertEquals("포테의 숲",TownTravelWindow.title(TownTravelWindow.FOREST));assertEquals("2써클 사냥터 (Lv 11~Lv 40)",TownTravelWindow.description(TownTravelWindow.FOREST));
  for(TownTravel.Destination d:TownTravel.TOWNS){assertFalse(TownTravelWindow.destinations(d.map).contains(CampaignWorld.PIET));assertFalse(TownTravelWindow.destinations(d.map).contains(d.map));for(String target:TownTravelWindow.destinations(d.map)){RectF card=TownTravelWindow.cardFor(d.map,target);assertEquals(target,TownTravelWindow.hit(d.map,card.centerX(),card.centerY()));assertTrue(TownTravelWindow.PANEL.contains(card));}}
  assertTrue(TownTravel.canDepart(CampaignWorld.PIET,"piet_town_travel"));assertTrue(TownTravelWindow.destinations(CampaignWorld.PIET).contains(WorldDef.ID));
 }
 @Test public void continuousLampPixelsHaveNoMissingPoleRowsAndKeepOriginalNativeCanvas()throws Exception{
  for(String folder:new String[]{"abel-video-v147","rucesion-v154"})for(String name:folder.startsWith("abel")?new String[]{"lamp","lamp_main","lamp_pool","lamp_banner_complete"}:new String[]{"lamp_main"}){
   Bitmap b=BitmapFactory.decodeStream(c.getAssets().open(folder+"/"+name+".png"));assertNotNull(b);int top=b.getHeight(),bottom=-1;
   for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(Color.alpha(b.getPixel(x,y))>128){top=Math.min(top,y);bottom=Math.max(bottom,y);}
   assertTrue(bottom-top>90);for(int y=top;y<=bottom;y++){boolean ink=false;for(int x=0;x<b.getWidth();x++)if(Color.alpha(b.getPixel(x,y))>16)ink=true;assertTrue(folder+"/"+name+" disconnected row "+y,ink);}
   b.recycle();
  }
 }
 @Test public void allThreeTownsShareServiceSignsAndNpcPlayerPixelScale()throws Exception{
  assertEquals(5,TownServiceLabels.signs(WorldDef.ID).size());assertEquals(5,TownServiceLabels.signs(AbelTownGeometry.ID).size());assertEquals(7,TownServiceLabels.signs(RucesionTownGeometry.ID).size());
  assertTrue(TownServiceLabels.signs(WorldDef.ID).stream().anyMatch(s->s.name.equals("장비상점")));assertTrue(TownServiceLabels.signs(AbelTownGeometry.ID).stream().anyMatch(s->s.name.equals("무기상점")));assertTrue(TownServiceLabels.signs(RucesionTownGeometry.ID).stream().anyMatch(s->s.name.equals("방어구상점")));
  List<String> receipt=new ArrayList<>();for(TownTravel.Destination town:TownTravel.TOWNS){NpcIdentity.Profile profile=NpcIdentity.forId(town.npc);Bitmap a=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888),b=a.copy(Bitmap.Config.ARGB_8888,true);new NpcActorRenderer(c).draw(new Canvas(a),town.npc,64,112,CharacterRenderer.Direction.SW,CharacterRenderer.State.IDLE,0);new CharacterRenderer(c).draw(new Canvas(b),new CharacterRenderer.Pose(64,112,CharacterRenderer.Direction.SW,CharacterRenderer.State.IDLE,0,0,1,false,profile.outfit,profile.weapon,CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));int[] pa=new int[16384],pb=new int[16384];a.getPixels(pa,0,128,0,0,128,128);b.getPixels(pb,0,128,0,0,128,128);assertArrayEquals("NPC/player identical outfit and foot "+town.map,pa,pb);int top=128,bottom=-1;for(int y=0;y<128;y++)for(int x=0;x<128;x++)if(Color.alpha(pa[y*128+x])>128){top=Math.min(top,y);bottom=Math.max(bottom,y);}receipt.add(town.map+" sameOutfitHeight="+(bottom-top+1)+" playerRenderScale="+CharacterRenderer.PLAYER_RENDER_SCALE);Bitmap pair=Bitmap.createBitmap(320,180,Bitmap.Config.ARGB_8888);Canvas pc=new Canvas(pair);pc.drawColor(0xff51554b);pc.drawBitmap(a,10,30,null);pc.drawBitmap(b,170,30,null);save(pair,"npc-player-"+town.map);
   GameView v=new GameView(c);v.layout(0,0,960,540);TownCampaignV163Test h=new TownCampaignV163Test();h.c=c;h.call(v,"travelTown",town.map);RuntimeState state=TownInteriorTest.field(v,"state");RuntimeState.Npc npc=state.npcs().stream().filter(n->n.id.equals(town.npc)).findFirst().get();state.player().x=npc.x+64;state.player().y=npc.y;WorldRuntimeAdapter world=TownInteriorTest.field(v,WorldDef.ID.equals(town.map)?"worldAdapter":"poteFieldAdapter");world.snapCameraToPlayer();Bitmap shot=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(shot));save(shot,"town-"+town.map);h.approach(v,town.npc);v.draw(new Canvas(shot));save(shot,"travel-"+town.map);
  }
  Path p=Path.of("build/reports/town-v165/npc-scale.txt");Files.createDirectories(p.getParent());Files.write(p,receipt,java.nio.charset.StandardCharsets.UTF_8);
 }
}
