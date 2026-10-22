package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.junit.*;import org.junit.runner.RunWith;
import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ForestMageV239Test {
 @Before public void setup(){new MageSourcesV235Test().setup();}
 @Test public void newPromotionChannelsKeepCasterRegistrationAndContinuousContacts()throws Exception{
  MageSourcesV235Test m=new MageSourcesV235Test();ClassicSkillReference r=new ClassicSkillReference(RuntimeEnvironment.getApplication());
  Bitmap grid=Bitmap.createBitmap(1000,650,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(grid);c.drawColor(0xff544b3e);
  long videoBytes=0;int row=0;for(int n:new int[]{39,41,42,51,52}){
   ClassicSkillReference.Channel q=r.channel(m.mage(n),false);assertNotNull(q);videoBytes+=q.sequence.atlas.getAllocationByteCount();assertTrue(q.sequence.durations.length>=10);
   java.util.Set<Integer> hashes=new java.util.HashSet<>();int[] pixels=new int[q.sequence.width*q.sequence.height];
   for(int f=0;f<q.sequence.durations.length;f++){q.sequence.atlas.getPixels(pixels,0,q.sequence.width,f%q.sequence.columns*q.sequence.width,f/q.sequence.columns*q.sequence.height,q.sequence.width,q.sequence.height);hashes.add(java.util.Arrays.hashCode(pixels));}
   assertTrue("Changing recorded frames "+n,hashes.size()>=4);
   if(n==51||n==52){
    Bitmap envelope=Bitmap.createBitmap(200,180,Bitmap.Config.ARGB_8888);r.draw(new Canvas(envelope),q,q.sequence.duration*.5f,100,130);
    int top=180,bottom=-1;for(int y=0;y<180;y++)for(int x=0;x<200;x++)if(Color.alpha(envelope.getPixel(x,y))>24){top=Math.min(top,y);bottom=Math.max(bottom,y);}
    assertTrue("Shield surrounds the torso above the ground "+n+":"+top,top<85);
    assertTrue("Shield stays by the source foot instead of dropping under it "+n+":"+bottom,bottom<146);
   }
   for(int f=0;f<8;f++)r.draw(c,q,q.sequence.duration*f/8f,65+f*125,70+row*125);row++;
  }assertTrue("Five source atlases stay under11MB decoded "+videoBytes,videoBytes<=11000000L);RogueRepairV232Test.save(grid,"v239-promotion-sequences");
  // Existing costs/recipient/status rules must still be reached by ordinary quickslots.
  m.allFourteenPromotionSpellsReachNormalQuickslotContactWithSourceEffectsAndCorrectAnchors();
 }
 @Test public void originalForestPoseRegistrationPreservesNativeFootAndShowsEachDirection()throws Exception{
  String[] ids={"POTE_RED","POTE_GREEN","POTE_PURPLE","POTE_SILVER","POTE_TREANT","POTE_LYCAN","POTE_GNOLL","POTE_WOLFRIDER","POTE_ANTLION","POTE_ANTGIANT","POTE_SILVERWOLF"};
  assertEquals(41f,PoteForestMonsterShowcase.nameOffset("POTE_PURPLE"),0f);assertEquals(59f,PoteForestMonsterShowcase.nameOffset("POTE_WOLFRIDER"),0f);
  PoteFieldRenderer renderer=new PoteFieldRenderer();Bitmap grid=Bitmap.createBitmap(1152,1100,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(grid);c.drawColor(0xff514d3b);
  for(int j=0;j<ids.length;j++){
   String id=ids[j];assertTrue(PoteForestMonsterShowcase.originalPote(id));assertEquals(96f,PoteForestMonsterShowcase.bodyHeight(id),0f);
   int col=0;for(String state:new String[]{"idle","walk","attack"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
    String key=PoteForestMonsterShowcase.assetPath(id,state,d);assertTrue(key.contains("/original_v239/"));
    Bitmap b=BitmapFactory.decodeStream(RuntimeEnvironment.getApplication().getAssets().open(key));assertEquals(192,b.getWidth());assertEquals(192,b.getHeight());
    renderer.drawMonsterTestPose(c,id,state,d,.55f,.2f,48+col*96,85+j*96);col++;
   }
  }RogueRepairV232Test.save(grid,"v239-original-monster-directions");
 }
 @Test public void actualForestScenesRetainPopulationAndBoundFloorCacheAfterTwelveTrips()throws Exception{
  GameView v=new GameView(RuntimeEnvironment.getApplication());v.layout(0,0,1536,864);PoteReferenceV113Test.invoke(v,"enterPoteField");
  RuntimeState s=TownInteriorTest.field(v,"state");java.lang.reflect.Method change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class);change.setAccessible(true);
  for(int i=0;i<12;i++){
   int z=i%3+1;change.invoke(v,"MAP_POTE_0"+z,false);assertEquals(new int[]{72,96,108}[z-1],s.monsters().size());
   WorldRuntimeAdapter world=TownInteriorTest.field(v,"poteFieldAdapter");PoteCampaignMapDef d=PoteCampaignMapDef.forId(s.currentMapId());world.camera().snapTo(d.entryX,d.entryY);
   Bitmap b=Bitmap.createBitmap(1536,864,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));
   PoteFieldRenderer renderer=TownInteriorTest.field(v,"poteFieldRenderer");
   long retained=0;for(String field:new String[]{"cache","hitCache","curseCache","curseHitCache"}){
    java.util.Map<?,?> entries=(java.util.Map<?,?>)ForestPixelCacheTest.field(renderer,field);int monsters=0;
    for(java.util.Map.Entry<?,?> e:entries.entrySet())if(String.valueOf(e.getKey()).startsWith(PoteForestMonsterShowcase.ASSET_ROOT)&&e.getValue() instanceof Bitmap){monsters++;retained+=((Bitmap)e.getValue()).getAllocationByteCount();}
    assertTrue(field+" current-map textures "+monsters,monsters<=60);
   }assertTrue("Four-state monster byte budget "+retained,retained<=35389440L);
   assertTrue(((java.util.Map<?,?>)ForestPixelCacheTest.field(renderer,"floorChunks")).size()<=32);
   if(i<3)RogueRepairV232Test.save(b,"v239-forest-"+z);b.recycle();
  }
 }
}
