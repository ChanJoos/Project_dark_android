package com.projectdark.mobile;
import android.content.Context;import android.graphics.*;
import com.projectdark.mobile.world.*;import java.util.*;import java.io.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import org.json.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MineHuntV209Test {
 MineTownV203Test f;Context c;
 @Before public void setup(){f=new MineTownV203Test();f.reset();c=RuntimeEnvironment.getApplication();}
 GameView enter()throws Exception{GameView v=f.town();RuntimeState s=f.field(v,"state");s.rpg().restoreProgression(99,0);f.approach(v,"mine_town_travel");RectF card=TownTravelWindow.cardFor(MineTownGeometry.ID,MineHuntGeometry.ID);f.tap(v,card.centerX(),card.centerY());assertEquals(MineHuntGeometry.ID,s.currentMapId());return v;}
 @Test public void namedMissingSorcererUsesOriginal68AndIsVisibleFromRealArrival()throws Exception{
  GameView v=enter();RuntimeState s=f.field(v,"state");WorldRuntimeAdapter w=f.field(v,"poteFieldAdapter");RuntimeState.Monster mage=s.monsters().get(9);assertEquals("헬소서러",mage.name);assertEquals(9,MineHuntMonsters.type(mage.id));WorldCameraTransform.Point at=w.worldToScreen(mage.x,mage.y);assertTrue("mage present in initial viewport "+at.x+","+at.y,at.x>30&&at.x<930&&at.y>30&&at.y<510);assertEquals(12,MineHuntGeometry.sourceU(s.player().x,s.player().y));assertEquals(34,MineHuntGeometry.sourceV(s.player().x,s.player().y));
  JSONObject manifest=new JSONObject(new String(PresentationAssetBytes.read(c,"mine-hunt-v208/manifest.json"),java.nio.charset.StandardCharsets.UTF_8));assertEquals(68,manifest.getJSONArray("monsters").getJSONObject(9).getInt("mpf"));Set<Integer> mpfs=new HashSet<>();for(int i=0;i<10;i++)mpfs.add(manifest.getJSONArray("monsters").getJSONObject(i).getInt("mpf"));assertFalse("unobserved skeletal placeholder removed",mpfs.contains(153));f.shot(v,"v209-entry-sorcerer-original-gate");
  assertTrue(MineHuntMonsters.armors(mage.id).contains("USER_ITEM_20261018_R01489"));int before=s.rpg().inventory().getOrDefault("USER_ITEM_20261018_R01489",0);for(int k=0;k<500&&s.rpg().inventory().getOrDefault("USER_ITEM_20261018_R01489",0)==before;k++){mage.alive=true;mage.hp=mage.maxHp;s.damage(mage,mage.maxHp);s.rpg().consumeCombat(s.ledger().snapshot(),s);}assertEquals(before+1,(int)s.rpg().inventory().get("USER_ITEM_20261018_R01489"));assertTrue(F5mSaveStore.checkpointActive());
 }
 @Test public void wornThiefAndMonkFamiliesFollowActualArmorSilhouettes(){
  assertEquals("헬블씨프",MineHuntMonsters.NAMES[5]);assertEquals("USER_ITEM_20261018_R01486",MineHuntMonsters.armor("MINE_05#0"));assertEquals("USER_ITEM_20261018_R05502",MineHuntMonsters.weapon("MINE_05#0"));assertEquals("헬몽크",MineHuntMonsters.NAMES[8]);assertEquals("USER_ITEM_20261018_R01490",MineHuntMonsters.armor("MINE_08#0"));assertNull(MineHuntMonsters.weapon("MINE_09#0"));
 }
 @Test public void actualTourNativeViewsAndMinimapUseOriginalGeometryAcrossWholeRetainedRecording()throws Exception{
  GameView v=enter();RuntimeState s=f.field(v,"state");WorldRuntimeAdapter w=f.field(v,"poteFieldAdapter");for(RuntimeState.Monster m:s.monsters())m.alive=false;
  for(int[] uv:new int[][]{{95,42},{103,35},{71,15},{67,4},{44,17},{10,30},{13,35},{23,53},{32,66},{34,79},{33,83},{25,87},{14,84},{12,85},{47,92},{56,80},{67,81},{70,87},{65,65},{76,72},{78,82},{92,75},{99,78},{103,77},{102,76},{109,72}}){WorldMoveTargetController.TileCenter t=CampaignWorld.nearestConnectedTile(MineHuntGeometry.ID,MineHuntGeometry.x(uv[0],uv[1]),MineHuntGeometry.y(uv[0],uv[1]));s.player().x=t.x;s.player().y=t.y;w.snapCameraToPlayer();f.shot(v,"v209-tour-"+uv[0]+"-"+uv[1]);}
  Bitmap terrain=Bitmap.createBitmap(96,96,Bitmap.Config.ARGB_8888);PoteCampaignMapDef d=PoteCampaignMapDef.forId(MineHuntGeometry.ID);MineHuntMinimapV209.drawTerrain(new Canvas(terrain),d.minX,d.minY,d.maxX,d.maxY);Set<Integer> colors=new HashSet<>();int empty=0,floor=0;for(int y=0;y<96;y++)for(int x=0;x<96;x++){int color=terrain.getPixel(x,y);colors.add(color);if(color==0xff171411)empty++;if(color==0xff887556)floor++;}assertTrue("real diamond-shaped bounds, not rectangle",empty>3000);assertTrue("walkable source component",floor>500);assertTrue(colors.contains(0xff41392c));File file=new File("build/reports/horror-v179/v209-minimap.png");try(FileOutputStream out=new FileOutputStream(file)){assertTrue(terrain.compress(Bitmap.CompressFormat.PNG,100,out));}
 }
}
