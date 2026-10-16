package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import com.projectdark.mobile.world.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MotionHudFrameV169Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String p:new String[]{"project_dark_f5m_v1","project_dark_skill_test_v1","project_dark_skill_book_v1"})c.getSharedPreferences(p,0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 static Object call(Object o,String name,Class<?>[] types,Object... args)throws Exception{Method m=o.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(o,args);}
 static void set(Object o,String name,Object value)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 List<String> profileNames(SkillPresentationCatalog catalog){List<String> names=new ArrayList<>();for(Iterator<String> it=catalog.profiles.keys();it.hasNext();)names.add(it.next());return names;}
 void save(Bitmap b,String n)throws Exception{File f=new File("build/reports/motion-hud-v169/"+n+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
 @Test public void twoHandSourceFramesHaveActualMatchingBladeAndClothCells()throws Exception{
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);JSONObject reg=new JSONObject(new String(PresentationAssetBytes.read(c,"source-registration/mw016.json"),java.nio.charset.StandardCharsets.UTF_8));
  // Source twohand weapon has c, not e; every used contact must bind the same source cell.
  assertNull(reg.getJSONObject("sprites").optJSONArray("e"));
  for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   Set<String> keys=new HashSet<>();for(float q:new float[]{0,.17f,.34f}){String key=catalog.frameKey(body,"TWO_HAND",d,q);keys.add(key);assertTrue(key,key.contains("/c/"));int i=Integer.parseInt(key.substring(key.lastIndexOf('/')+1));assertNotNull(reg.getJSONObject("sprites").getJSONArray("c").optJSONObject(i));}assertEquals(3,keys.size());
  }
 }
 @Test public void basicAndSkillFourFacingSourcePixelsAreNotDoubleMirrored()throws Exception{
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);SkillBodyRenderer renderer=new SkillBodyRenderer(c,catalog);
  // Independent pixel oracle: original four-facing group02 is already oriented, with its own pivot.
  for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   int i=d==CharacterRenderer.Direction.NW?0:d==CharacterRenderer.Direction.NE?1:d==CharacterRenderer.Direction.SW?2:3;
   JSONObject f=catalog.frames.getJSONObject(body+"/02/"+i);Bitmap src=BitmapFactory.decodeStream(c.getAssets().open("skill-presentation/"+f.getString("path")));
   Bitmap actual=Bitmap.createBitmap(200,220,Bitmap.Config.ARGB_8888),expected=Bitmap.createBitmap(200,220,Bitmap.Config.ARGB_8888);float x=100,y=190,scale=SkillBodyRenderer.SCALE,root=y+8*scale;
   Paint p=new Paint();p.setFilterBitmap(false);float l=x-(float)f.getDouble("pivotX")*scale,t=root-(src.getHeight()+(float)f.getDouble("offsetY"))*scale;
   new Canvas(expected).drawBitmap(src,null,new RectF(l,t,l+src.getWidth()*scale,t+src.getHeight()*scale),p);
   CharacterRenderer.Pose pose=new CharacterRenderer.Pose(x,y,d,CharacterRenderer.State.ATTACK,0,.18f,.5f,false,"",null,null,CharacterRenderer.EffectFamily.NONE,AnimationAction.SWING);
   assertTrue(renderer.draw(new Canvas(actual),pose,body,"SWING",.4f));assertTrue(body+" "+d,actual.sameAs(expected));src.recycle();actual.recycle();expected.recycle();
  }
 }
 @Test public void allBasicAndSkillFamiliesRenderBothBodiesWithBoundedWarmWork()throws Exception{
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);SkillBodyRenderer renderer=new SkillBodyRenderer(c,catalog);JSONArray stats=new JSONArray();
  for(String body:new String[]{"mm001","wm001"})for(String item:new String[]{"IT_ADAPTED_PLAYTEST_MOKDO","IT_WARDROBE_MW016",ChungryongWeaponRenderer.ITEM}){
   RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);r.unequip("무기");if(!r.inventory().containsKey(item))r.autoLootResolvedItem(item,1);assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(item));CharacterVisualBinding v=CharacterVisualBinding.from(r);renderer.prepare(v.equipmentVisualRef(),v.weaponVisualRef());
   String motion=WeaponCombatProfile.motion(r,"EQUIPPED_BASIC");assertNotEquals("EQUIPPED_BASIC",motion);Bitmap sheet=Bitmap.createBitmap(720,450,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);canvas.drawColor(0xffa8a18f);Paint label=new Paint();label.setTextSize(10);label.setColor(Color.BLACK);
   int row=0;for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){int col=0;for(float q:new float[]{0,.17f,.34f,.55f,.85f}){float x=65+col++*140,y=100+row*108;CharacterRenderer.Pose p=new CharacterRenderer.Pose(x,y,d,CharacterRenderer.State.ATTACK,0,q*.5f,.5f,false,v.equipmentVisualRef(),v.weaponVisualRef(),null,CharacterRenderer.EffectFamily.NONE,AnimationAction.SWING);assertTrue(ChungryongWeaponRenderer.equipped(v.weaponVisualRef())?renderer.drawChungryong(canvas,p,body,"twohand",q):renderer.draw(canvas,p,body,motion,q));canvas.drawText(d+" "+q,x-40,y+12,label);}row++;}save(sheet,body+"-"+v.weaponVisualRef()+"-"+motion);sheet.recycle();
  }
  Bitmap target=Bitmap.createBitmap(220,220,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(target);
  for(String body:new String[]{"mm001","wm001"})for(String motion:profileNames(catalog))for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(float q:new float[]{0,.17f,.34f,.6f,.85f}){CharacterRenderer.Pose p=new CharacterRenderer.Pose(110,190,d,CharacterRenderer.State.ATTACK,0,q,.5f,false,"mu0000001","mw001",null,CharacterRenderer.EffectFamily.NONE);assertTrue(body+motion+d,renderer.draw(canvas,p,body,motion,q));}
  int rigs=renderer.rigBuildCount(),loads=renderer.bitmapLoadCount();long[] times=new long[240];
  for(int n=0;n<times.length;n++){CharacterRenderer.Pose p=new CharacterRenderer.Pose(110,190,CharacterRenderer.Direction.NW,CharacterRenderer.State.ATTACK,0,.18f,.5f,false,"mu0000001","mw001",null,CharacterRenderer.EffectFamily.NONE);long start=System.nanoTime();renderer.draw(canvas,p,"mm001","THRUST",.4f);times[n]=System.nanoTime()-start;}
  assertEquals("no warm BODY decode",loads,renderer.bitmapLoadCount());assertEquals("no warm per-pixel rig scan",rigs,renderer.rigBuildCount());Arrays.sort(times);System.out.println("V169_WARM_ATTACK_NS p95="+times[227]+" p99="+times[237]+" max="+times[239]+" rigs="+rigs+" bitmaps="+loads);target.recycle();
 }
 @Test public void actualMapsAlwaysShowAndAcceptTheJoystick()throws Exception{
  GameView view=new GameView(c);view.layout(0,0,960,540);view.setSkillTestMode(true);Set<String> maps=new LinkedHashSet<>();maps.add(WorldDef.ID);Collections.addAll(maps,CampaignWorld.ROUTE);Collections.addAll(maps,RucesionCoastGeometry.ROUTE);maps.add(RucesionTownGeometry.ID);maps.add("MAP_POTE_04");
  Bitmap sheet=Bitmap.createBitmap(960,((maps.size()+3)/4)*135,Bitmap.Config.ARGB_8888);Canvas grid=new Canvas(sheet);int index=0;long[] times=new long[maps.size()*8];int count=0;
  for(String map:maps){
   if(WorldDef.ID.equals(map)){}else if(TownTravel.forMap(map)!=null)call(view,"travelTown",new Class<?>[]{String.class},map);else {set(view,"inPoteField",true);call(view,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},map,false);}
   RuntimeState state=TownInteriorTest.field(view,"state");state.player().hp=state.player().maxHp;state.player().alive=true;
   Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);view.draw(canvas);
   // Strong idle thumb and rim survive pale sand as well as dark terrain, no input required.
   int center=image.getPixel(110,374);assertTrue(map+" thumb alpha",Color.alpha(center)==255);assertTrue(map+" thumb contrast",Color.red(center)<150&&Color.green(center)<155&&Color.blue(center)<150);
   TownInteriorTest.tap(view,130,374); // DOWN/UP does reset state; also exercise an active held pointer.
   MotionEvent down=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,130,374,0);view.onTouchEvent(down);down.recycle();assertTrue(map,(Boolean)TownInteriorTest.field(view,"joy"));assertTrue(Math.abs((Float)TownInteriorTest.field(view,"vx"))>0);MotionEvent up=MotionEvent.obtain(0,1,MotionEvent.ACTION_UP,130,374,0);view.onTouchEvent(up);up.recycle();assertFalse((Boolean)TownInteriorTest.field(view,"joy"));
   grid.drawBitmap(image,null,new RectF((index%4)*240,(index/4)*135,(index%4+1)*240,(index/4+1)*135),new Paint());index++;
   for(int n=0;n<8;n++){long a=System.nanoTime();call(view,"update",new Class<?>[]{float.class},.016f);view.draw(canvas);times[count++]=System.nanoTime()-a;}
   image.recycle();
  }
  save(sheet,"all-map-hud");sheet.recycle();Arrays.sort(times);System.out.println("V169_ALL_MAPS="+maps.size()+" frame_ns p95="+times[(int)(count*.95)]+" p99="+times[(int)(count*.99)]+" max="+times[count-1]);
 }
 @Test public void everyInteriorKeepsVisibleStickAndDirectInput()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);List<TownInteriorDef> rooms=new ArrayList<>();rooms.addAll(TownInteriorDef.ALL);rooms.addAll(TownInteriorDef.ABEL);rooms.addAll(TownInteriorDef.RUCESION);
  Bitmap sheet=Bitmap.createBitmap(960,((rooms.size()+3)/4)*135,Bitmap.Config.ARGB_8888);Canvas grid=new Canvas(sheet);int i=0;
  for(TownInteriorDef room:rooms){call(v,"enterTownInterior",new Class<?>[]{TownInteriorDef.class},room);Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(image));int z=image.getPixel(110,374);assertTrue(room.mapId,Color.red(z)<150&&Color.green(z)<155);
   MotionEvent e=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,130,374,0);v.onTouchEvent(e);e.recycle();assertTrue(room.mapId,(Boolean)TownInteriorTest.field(v,"joy"));e=MotionEvent.obtain(0,1,MotionEvent.ACTION_UP,130,374,0);v.onTouchEvent(e);e.recycle();assertFalse((Boolean)TownInteriorTest.field(v,"joy"));grid.drawBitmap(image,null,new RectF(i%4*240,i/4*135,(i%4+1)*240,(i/4+1)*135),new Paint());i++;image.recycle();}
  save(sheet,"all-interior-hud");sheet.recycle();System.out.println("V169_INTERIORS="+rooms.size());
 }
 @Test public void journalFramesReuseRouteWithoutStaleJobOrMigration()throws Exception{
  RpgProgressionState r=new RpgProgressionState();CampaignProgress campaign=r.campaign();QuestJournalModel model=new QuestJournalModel(c);F5mAdaptedPrologueQuest q=F5mAdaptedPrologueQuest.openingFixture();GrowthQuest2 g=new GrowthQuest2();
  List<CampaignProgress.Def> route=campaign.route(r.currentJobCode());assertTrue(route.size()>60);int builds=campaign.routeBuildCount();long start=System.nanoTime();for(int n=0;n<100;n++){model.rows(q,g,true,r);assertSame(route,campaign.route(r.currentJobCode()));}long warm=System.nanoTime()-start;assertEquals(builds,campaign.routeBuildCount());
  List<CampaignProgress.Def> warrior=campaign.route("WARRIOR"),mage=campaign.route("MAGE");assertNotSame(warrior,mage);assertTrue(warrior.stream().anyMatch(d->"J01_WARRIOR".equals(d.id)));assertFalse(mage.stream().anyMatch(d->"J01_WARRIOR".equals(d.id)));
  JSONObject snapshot=campaign.snapshot();assertTrue(campaign.restore(snapshot));List<CampaignProgress.Def> restored=campaign.route(r.currentJobCode());assertEquals(route.size(),restored.size());for(int i=0;i<route.size();i++)assertEquals(route.get(i).id,restored.get(i).id);
  // Same historical builder is measured independently; verify byte-identical IDs, no benchmark timing assertion.
  long old=System.nanoTime();for(int n=0;n<1000;n++)call(campaign,"buildRoute",new Class<?>[]{String.class},r.currentJobCode());old=System.nanoTime()-old;start=System.nanoTime();for(int n=0;n<1000;n++)campaign.route(r.currentJobCode());long now=System.nanoTime()-start;
  System.out.println("V169_ROUTE old_build_1000_ns="+old+" cached_1000_ns="+now+" journal100_ns="+warm+" definitions="+route.size());
 }
 @Test public void leanGrowthProjectionPreservesFullStatsAcrossEquipmentAndRestore(){
  RuntimeState s=new RuntimeState();RpgProgressionState r=s.rpg();r.enableEquipmentSandbox(true);
  for(String id:new String[]{"IT_WARDROBE_MW016","IT_ADAPTED_STARTER_SHIELD",ChungryongWeaponRenderer.ITEM}){r.autoLootResolvedItem(id,1);r.equip(id);s.applyDerivedGrowth();FinalStats expected=r.finalStats();assertEquals(expected.maxHp,s.player().maxHp);assertEquals(expected.maxMp,s.player().maxMp);}
  r.restoreBaseResources(12000,9000);s.applyDerivedGrowth();assertEquals(r.finalStats().maxHp,s.player().maxHp);assertEquals(r.finalStats().maxMp,s.player().maxMp);
 }
}
