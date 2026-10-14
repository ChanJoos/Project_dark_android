package com.projectdark.mobile;
import android.graphics.*;import com.projectdark.mobile.world.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import java.util.*;import java.nio.file.Path;import static org.junit.Assert.*;
/** User-requested comparison is same direction AND same pose, on actual production renderer. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CoastSourceV159Test extends CoastRefinementV158Test {
 @Before public void output159(){out=Path.of("build/reports/coast-v159");}
 Bitmap pose(PoteFieldRenderer r,String id,String p,CharacterRenderer.Direction d){Bitmap b=Bitmap.createBitmap(300,220,Bitmap.Config.ARGB_8888);r.drawMonsterTestPose(new Canvas(b),id,p,d,"attack".equals(p)?.65f:0,"walk".equals(p)?.21f:0,150,190);return b;}
 @Test public void colorPairsHaveIdenticalPoseAlphaAndKoatlOnlyChangesRibs()throws Exception{
  PoteFieldRenderer r=new PoteFieldRenderer();String[][] pairs={{"LYK_BLUE_HACON","LYK_RED_HACON"},{"LYK_KAKAPO","LYK_POISON_KAKAPO"},{"LYK_ELECT_KOATL","LYK_KOATL"}};
  for(String[] pair:pairs)for(String p:new String[]{"idle","walk","attack"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   Bitmap a=pose(r,pair[0],p,d),b=pose(r,pair[1],p,d);int changed=0,preserved=0;
   for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++){int aa=a.getPixel(x,y),bb=b.getPixel(x,y);assertEquals(pair[0]+"/"+p+"/"+d+" identical geometry",Color.alpha(aa),Color.alpha(bb));if(Color.alpha(aa)>200){if(aa!=bb)changed++;else preserved++;}}
   assertTrue("color difference must exist",changed>30);assertTrue("original material details preserved",preserved>100);
   if(pair[0].equals("LYK_ELECT_KOATL")){Bitmap base=BitmapFactory.decodeStream(c.getAssets().open(PoteForestMonsterShowcase.assetPath(pair[0],p,d)));Bitmap variant=CoastSpritePalette.apply(PoteForestMonsterShowcase.assetPath(pair[1],p,d),base.copy(Bitmap.Config.ARGB_8888,true));for(int y=0;y<base.getHeight();y++)for(int x=0;x<base.getWidth();x++){int q=base.getPixel(x,y);if(Color.blue(q)>Color.red(q)*1.2f&&Color.alpha(q)>200)assertEquals("navy fins/body unchanged",q,variant.getPixel(x,y));}}
  }
  assertEquals(PoteForestMonsterShowcase.bodyHeight("LYK_PLANT_ARKPUS"),PoteForestMonsterShowcase.bodyHeight("LYK_ARKPUS"),0);
  Bitmap a=pose(r,"LYK_ARKPUS","idle",CharacterRenderer.Direction.SW),b=pose(r,"LYK_PLANT_ARKPUS","idle",CharacterRenderer.Direction.SW);assertEquals("same visible size to plant",bounds(a).height(),bounds(b).height(),3);
 }
 @Test public void bothCoastsHaveUniqueCoastalContoursAndSourceLocalAssemblages(){
  Set<String> signatures=new HashSet<>();for(boolean r:new boolean[]{false,true})for(int z=0;z<5;z++){
   StringBuilder signature=new StringBuilder(Arrays.deepToString(CoastalHuntLayout.shoreLine(z,r)));for(CoastalHuntLayout.Prop p:CoastalHuntLayout.props(z,r))signature.append(p.asset).append(p.x).append(p.y);
   assertTrue("ten independently authored coast plans",signatures.add(signature.toString()));
   if(z>0){float min=Float.MAX_VALUE,max=0;for(float[] p:CoastalHuntLayout.shoreLine(z,r)){min=Math.min(min,p[1]);max=Math.max(max,p[1]);}assertTrue("beach bay instead of straight bottom ocean stripe",max-min>180);}
  }
 }
 @Test public void samePoseFourDirectionPairsAndAbelThirteenSpeciesAreCaptured()throws Exception{
  PoteFieldRenderer r=new PoteFieldRenderer();CharacterRenderer player=new CharacterRenderer(c);CharacterVisualBinding v=CharacterVisualBinding.from(new RuntimeState().rpg());Paint text=new Paint();text.setColor(Color.BLACK);text.setTextSize(14);
  List<List<String>> rosters=Arrays.asList(RucesionCoastMonsters.ALL,AbelCoastMonsters.ALL);
  for(List<String> roster:rosters)for(String p:new String[]{"idle","walk","attack"}){
   Bitmap grid=Bitmap.createBitmap(1400,roster.size()*210,Bitmap.Config.ARGB_8888);Canvas g=new Canvas(grid);g.drawColor(0xffcdb58d);
   for(int row=0;row<roster.size();row++){String id=roster.get(row);float foot=row*210+178;g.drawText(roster==RucesionCoastMonsters.ALL?RucesionCoastMonsters.name(id):AbelCoastMonsters.name(id),12,foot-130,text);
    for(int col=0;col<4;col++){CharacterRenderer.Direction d=new CharacterRenderer.Direction[]{CharacterRenderer.Direction.NW,CharacterRenderer.Direction.NE,CharacterRenderer.Direction.SW,CharacterRenderer.Direction.SE}[col];float x=110+col*340;player.draw(g,new CharacterRenderer.Pose(x,foot,d,CharacterRenderer.State.IDLE,0,0,1,false,v.equipmentVisualRef(),v.weaponVisualRef(),CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));r.drawMonsterTestPose(g,id,p,d,"attack".equals(p)?.65f:0,"walk".equals(p)?.21f:0,x+145,foot);g.drawText(p+" "+d,x,foot+25,text);}}
   save(grid,(roster==RucesionCoastMonsters.ALL?"rucesion10":"abel13")+"-same-pose-"+p);grid.recycle();
  }
 }
 @Test public void actualCameraScenesShowBothCoastsAtWaterVegetationAndReefEdges()throws Exception{
  PoteFieldRenderer r=new PoteFieldRenderer();for(String[] route:routes())for(int z=1;z<route.length;z++){
   String map=route[z];RuntimeState s=new RuntimeState();s.enterCampaignMap(map,false);WorldRuntimeAdapter w=CampaignNavigationTest.adapter(s);r.prepareMonsters(s);
   for(int spot=0;spot<3;spot++){float x=spot==0?820:spot==1?1400:1900;float shore=AbelCoastGeometry.contains(map)?AbelCoastGeometry.shoreY(map,x):RucesionCoastGeometry.shoreY(map,x);w.camera().snapTo(x,spot==0?850:spot==1?1400:shore-140);Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(b);List<PoteFieldRenderer.ActorDraw> actors=new ArrayList<>();for(RuntimeState.Monster m:s.monsters()){WorldCameraTransform.Point at=w.worldToScreen(m.x,m.y);actors.add(new PoteFieldRenderer.ActorDraw(m.x,m.y,PoteForestMonsterShowcase.bodyHeight(m.id),52,()->r.drawMonster(canvas,m,at.x,at.y)));}r.drawScene(canvas,w,actors);save(b,map+"-local"+spot);b.recycle();}
  }
 }
}
