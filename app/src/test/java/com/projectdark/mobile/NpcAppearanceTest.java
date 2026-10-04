package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.*;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.util.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class NpcAppearanceTest {
 private Context context(){return RuntimeEnvironment.getApplication();}
 private Bitmap actor(NpcIdentity.Profile p,CharacterRenderer.Direction d,boolean npc){
  Bitmap b=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);
  if(npc)new WorldEntityPresentationRenderer().draw(c,new WorldEntityPresentationRenderer.Pose(WorldEntityPresentationRenderer.Kind.NPC,64,112,d,CharacterRenderer.State.IDLE,0,0,1,CharacterRenderer.EffectFamily.NONE,false,false,"npc/"+p.id,null));
  else new CharacterRenderer(context()).draw(c,new CharacterRenderer.Pose(64,112,d,CharacterRenderer.State.IDLE,0,0,1,false,p.outfit,p.weapon,CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));
  return b;
 }
 private int[] pixels(Bitmap b){int[] a=new int[b.getWidth()*b.getHeight()];b.getPixels(a,0,b.getWidth(),0,0,b.getWidth(),b.getHeight());return a;}
 @Test public void allHumanNpcRoutesHaveDistinctRegisteredAmericanProfilesAndFutureDefaults(){
  Set<String> names=new HashSet<>(),outfits=new HashSet<>();EquipmentVisualRegistry registry=new EquipmentVisualRegistry(context());
  assertEquals(11,NpcIdentity.ALL.size());
  for(NpcIdentity.Profile p:NpcIdentity.ALL){
   assertTrue(p.rank>=1&&p.rank<=100);assertTrue(names.add(p.name));assertTrue(outfits.add(p.outfit));
   for(String id:p.outfit.split(",")){EquipmentVisualRegistry.Visual v=registry.get(id);assertNotNull("real atlas "+id,v);assertNotNull("real source registration "+id,v.registration);for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())assertNotNull(v.registration.idle(d,0));}
  }
  RuntimeState s=new RuntimeState();for(RuntimeState.Npc n:s.npcs())assertEquals(NpcIdentity.forId(n.id).label(),n.name);
  for(TownInteriorDef d:TownInteriorDef.ALL)assertEquals(NpcIdentity.forId(NpcIdentity.interiorKey(d.kind.name())).label(),d.npcName);
  NpcIdentity.Profile future=NpcIdentity.forId("future_npc_merchant_42");assertTrue(future.rank>=1&&future.rank<=100);assertEquals(future.outfit,NpcIdentity.forId(future.id).outfit);assertNotEquals(NpcIdentity.forId("future_npc_merchant_43").outfit,future.outfit);
 }
 @Test public void allFourDirectionsUseExactlyThePlayerPaperDollAndOutfitsAreVisiblyDifferent(){
  Set<Integer> appearances=new HashSet<>();assertTrue(new CharacterRenderer(context()).resourceAtlasActive());
  for(NpcIdentity.Profile p:NpcIdentity.ALL)for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   Bitmap npc=actor(p,d,true),player=actor(p,d,false);assertArrayEquals("same source scale, body, equipment and foot "+p.id+" "+d,pixels(player),pixels(npc));
   if(d==CharacterRenderer.Direction.SE)assertTrue("different actual outfit pixels "+p.id,appearances.add(Arrays.hashCode(pixels(npc))));
   int count=0;for(int color:pixels(npc))if(Color.alpha(color)>0)count++;assertTrue("real dressed sprite, not missing layers",count>350);
  }
 }
 @Test public void nativeRosterAndEveryTownServiceAreCapturedWithThePlayer()throws Exception{
  Bitmap sheet=Bitmap.createBitmap(1540,1760,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(sheet);c.drawColor(0xff314b30);Paint label=new Paint();label.setColor(Color.WHITE);label.setTextSize(18);
  int row=0;for(NpcIdentity.Profile p:NpcIdentity.ALL){c.drawText(p.label(),15,row*160+25,label);int col=0;for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   c.drawText(d.name(),270+col*300,row*160+25,label);c.save();c.translate(270+col*300,row*160+35);c.scale(2,2);new NpcActorRenderer(context()).draw(c,p.id,40,55,d,CharacterRenderer.State.IDLE,0);c.restore();col++;
  }row++;}write(sheet,"roster");
  GameView v=new GameView(context());v.layout(0,0,1920,1080);RuntimeState state=TownInteriorTest.field(v,"state");WorldRuntimeAdapter world=TownInteriorTest.field(v,"worldAdapter");
  state.player().x=656;state.player().y=640;world.snapCameraToPlayer();Bitmap outdoor=Bitmap.createBitmap(1920,1080,Bitmap.Config.ARGB_8888);v.draw(new Canvas(outdoor));write(outdoor,"milles");
  for(TownInteriorDef d:TownInteriorDef.ALL){TownInteriorTest.enter(v,d);state.player().x=d.customerX();state.player().y=d.customerY();WorldRuntimeAdapter inside=TownInteriorTest.field(v,"reagentShopAdapter");inside.snapCameraToPlayer();Bitmap b=Bitmap.createBitmap(1920,1080,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));write(b,d.kind.name().toLowerCase());TownInteriorTest.call(v,"leaveReagentShop");}
 }
 private void write(Bitmap b,String name)throws Exception{File f=new File("build/reports/device-review/v104-npc-"+name+".png");f.getParentFile().mkdirs();try(OutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
}
