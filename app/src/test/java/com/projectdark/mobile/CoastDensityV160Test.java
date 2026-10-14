package com.projectdark.mobile;
import android.graphics.*;import com.projectdark.mobile.world.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import java.nio.file.Path;import java.util.*;import static org.junit.Assert.*;
/** Source silhouette envelopes, small density adjustment, accepted Rucesion preservation. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CoastDensityV160Test extends CoastSourceV159Test {
 @Before public void output160(){out=Path.of("build/reports/coast-v160");}
 @Test public void modestPopulationsKeepBossIdentitiesAndExactlyFiveActiveRooms(){
  int[][] expected={{0,65,86,97,97},{0,65,86,86,8}},previous={{0,72,96,108,108},{0,72,96,96,9}};
  for(int coast=0;coast<2;coast++){String[] route=routes()[coast];assertEquals(5,route.length);Set<String> seen=new HashSet<>();
   for(int z=0;z<5;z++){String map=route[z];RuntimeState s=new RuntimeState();s.enterCampaignMap(map,false);assertTrue(seen.add(map));assertEquals(expected[coast][z],s.monsters().size());assertEquals(z==4?null:route[z+1],CampaignWorld.next(map));if(z>0){assertEquals(route[z-1],CampaignWorld.previous(map));double ratio=(double)s.monsters().size()/previous[coast][z];assertTrue("small reduction only",ratio>=.88&&ratio<=.92);}else assertTrue(CampaignWorld.title(map).endsWith("대기방"));
    if(z==4){Map<String,Integer> ids=new HashMap<>();for(RuntimeState.Monster m:s.monsters())ids.merge(m.campaignRewardProfileId,1,Integer::sum);if(coast==0){assertEquals(Integer.valueOf(1),ids.get("ABEL_KINGARK"));assertEquals(Integer.valueOf(1),ids.get("ABEL_KINGARK_FAKE"));assertEquals(Integer.valueOf(3),ids.get("ABEL_BERSERKER"));}else{assertEquals(Integer.valueOf(1),ids.get("LYK_ARIEL"));assertEquals(Integer.valueOf(7),ids.get("LYK_BLUE_HACON"));}}
   }
  }
  assertEquals("MAP_ABEL_COAST_1A",AbelCoastGeometry.canonical("MAP_ABEL_COAST_1B"));assertFalse(Arrays.asList(AbelCoastGeometry.ROUTE).contains("MAP_ABEL_COAST_1B"));
 }
 @Test public void nativeAbelSilhouettesMatchSourcePlayerEnvelopesAndLarvaIsWide()throws Exception{
  PoteFieldRenderer r=new PoteFieldRenderer();CharacterRenderer player=new CharacterRenderer(c);CharacterVisualBinding v=CharacterVisualBinding.from(new RuntimeState().rpg());Bitmap p=Bitmap.createBitmap(300,220,Bitmap.Config.ARGB_8888);player.draw(new Canvas(p),new CharacterRenderer.Pose(150,190,CharacterRenderer.Direction.SW,CharacterRenderer.State.IDLE,0,0,1,false,v.equipmentVisualRef(),v.weaponVisualRef(),CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));float ph=bounds(p).height();assertTrue(ph>=45&&ph<=60);
  String[] ids={"ABEL_TURTLE","ABEL_PONG","ABEL_CATERPILLAR","ABEL_GRAB"};float[][] envelopes={{.80f,1.04f,.46f,.65f},{.72f,.94f,.52f,.74f},{.84f,1.10f,.56f,.80f},{.75f,1.00f,.67f,.91f}};
  for(int i=0;i<ids.length;i++){Rect b=bounds(pose(r,ids[i],"idle",CharacterRenderer.Direction.SW));float w=b.width()/ph,h=b.height()/ph;assertTrue(ids[i]+" width/player "+w,w>=envelopes[i][0]&&w<=envelopes[i][1]);assertTrue(ids[i]+" height/player "+h,h>=envelopes[i][2]&&h<=envelopes[i][3]);System.out.println("V160_SOURCE_RATIO "+ids[i]+" width="+w+" height="+h);}
  for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(String state:new String[]{"idle","walk","attack"}){Rect b=bounds(pose(r,"ABEL_CATERPILLAR",state,d));assertTrue("low wide larva "+state+"/"+d,b.width()>=b.height()*1.15f);assertTrue("registration near ground",b.bottom>=186&&b.bottom<=196);}
  assertEquals(60f,PoteForestMonsterShowcase.bodyHeight("ABEL_ARKPUS"),0);assertEquals(60f,PoteForestMonsterShowcase.bodyHeight("ABEL_OCTOPUS"),0);assertEquals(60f,PoteForestMonsterShowcase.bodyHeight("ABEL_GAS"),0);assertEquals(60f,PoteForestMonsterShowcase.bodyHeight("ABEL_BERSERKER"),0);assertEquals(210f,PoteForestMonsterShowcase.bodyHeight("ABEL_KINGARK"),0);
 }
 @Test public void userAcceptedRucesionSizesAndPresentationAreRetained(){float[] heights={72,72,84,84,64,64,84,84,95,110};for(int i=0;i<heights.length;i++){String id=RucesionCoastMonsters.ALL.get(i);assertEquals(heights[i],PoteForestMonsterShowcase.bodyHeight(id),0);assertEquals(id.endsWith("KOATL")?104:heights[i],PoteForestMonsterShowcase.bodyWidth(id),0);}}
}
