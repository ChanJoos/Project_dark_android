package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MonsterCurseV162Test {
 private final Path out=Path.of("build/reports/curse-v162");
 private RuntimeState coast(){RuntimeState s=new RuntimeState();s.enterCampaignMap("MAP_ABEL_COAST_1A",false);return s;}
 private RuntimeState.Monster pong(RuntimeState s){return s.monsters().stream().filter(m->PoteForestMonsterShowcase.species(m.id).equals("ABEL_PONG")).findFirst().get();}
 private Bitmap render(PoteFieldRenderer r,RuntimeState.Monster m){Bitmap b=Bitmap.createBitmap(240,220,Bitmap.Config.ARGB_8888);r.drawMonster(new Canvas(b),m,120,180);return b;}
 private int[] pixels(Bitmap b){int[] p=new int[b.getWidth()*b.getHeight()];b.getPixels(p,0,b.getWidth(),0,0,b.getWidth(),b.getHeight());return p;}
 private void sameAlphaChangedRgb(Bitmap a,Bitmap b){int[] p=pixels(a),q=pixels(b);int changes=0;for(int i=0;i<p.length;i++){assertEquals("alpha/outline/anchor at "+i,p[i]>>>24,q[i]>>>24);if((p[i]>>>24)>128&&p[i]!=q[i])changes++;}assertTrue("visible RGB must change: changed="+changes,changes>30);}
 private void save(Bitmap b,String name)throws Exception{Files.createDirectories(out);try(java.io.OutputStream f=Files.newOutputStream(out.resolve(name+".png"))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,f));}}
 @Test public void everyCatalogCurseChangesLiveActorAndExpiryRestoresExactly()throws Exception{
  String[] ids={"SK_마법사_002","SK_마법사_011","SK_마법사_024","SK_마법사_053","SK_마법사_046","SK_마법사_047","SK_마법사_048","SK_마법사_049","SK_마법사_050","SK_성직자_012"};
  RuntimeState s=coast();RuntimeState.Monster m=pong(s);PoteFieldRenderer r=new PoteFieldRenderer();r.prepareMonsters(s);Bitmap normal=render(r,m);save(normal,"pong-normal");SkillAbilityExecutor executor=new SkillAbilityExecutor(s);executor.testMode=true;
  for(String id:ids){assertFalse(MonsterCursePalette.active(s.skillEffects(),m.id));executor.apply(m.id,id);assertTrue(id,MonsterCursePalette.active(s.skillEffects(),m.id));Bitmap faded=render(r,m);sameAlphaChangedRgb(normal,faded);save(faded,id.replace("SK_마법사_","wizard-").replace("SK_성직자_","priest-") );s.skillEffects().tick(s,SkillAbilityCatalog.get(id).duration+.01f);assertFalse(id,MonsterCursePalette.active(s.skillEffects(),m.id));assertArrayEquals(pixels(normal),pixels(render(r,m)));faded.recycle();}
  // Unrelated statuses do not wash the normal palette out.
  for(String kind:new String[]{"POISON","FREEZE","ARMOR_BREAK"}){s.skillEffects().put(m.id,"fixture",kind,1,10);assertArrayEquals(pixels(normal),pixels(render(r,m)));s.skillEffects().clear(m.id);}
 }
 @Test public void strongCurseSurvivesWeakRecastAndWeakDispelThenCorrectDispelRestores()throws Exception{
  RuntimeState s=coast();RuntimeState.Monster m=pong(s);PoteFieldRenderer r=new PoteFieldRenderer();r.prepareMonsters(s);Bitmap normal=render(r,m);SkillAbilityExecutor ex=new SkillAbilityExecutor(s);ex.testMode=true;
  ex.apply(m.id,"SK_마법사_053");ex.apply(m.id,"SK_마법사_002");assertEquals(65,s.skillEffects().power(m.id,"CURSE"));ex.apply(m.id,"SK_성직자_003");assertTrue(MonsterCursePalette.active(s.skillEffects(),m.id));sameAlphaChangedRgb(normal,render(r,m));ex.apply(m.id,"SK_성직자_023");assertArrayEquals(pixels(normal),pixels(render(r,m)));save(render(r,m),"pong-dispelled");
  s.skillEffects().put(m.id,"fixture","CURSE",20,0);assertFalse(MonsterCursePalette.active(s.skillEffects(),m.id));
 }
 @Test public void allCoastSpeciesPosesKeepAlphaAndNoFilterLeaksToNextActorOrHit()throws Exception{
  PoteFieldRenderer r=new PoteFieldRenderer();Set<String> seen=new HashSet<>();Bitmap grid=Bitmap.createBitmap(960,23*150,Bitmap.Config.ARGB_8888);Canvas g=new Canvas(grid);g.drawColor(0xffc4ad83);Paint label=new Paint();label.setColor(Color.BLACK);label.setTextSize(13);int row=0;
  for(String[] route:new String[][]{AbelCoastGeometry.ROUTE,RucesionCoastGeometry.ROUTE})for(String map:route){RuntimeState s=new RuntimeState();s.enterCampaignMap(map,false);r.prepareMonsters(s);
   for(RuntimeState.Monster m:s.monsters()){String species=PoteForestMonsterShowcase.species(m.id);if(!seen.add(species))continue;
    for(String pose:new String[]{"idle","walk","attack"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
     m.visualFacing.endAttack();m.visualFacing.setLocomotion(d);m.moveStartX=0;m.moveStartY=0;m.moveTargetX=(d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW)?-32:32;m.moveTargetY=(d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE)?-16:16;m.isMoving=pose.equals("walk");m.attackVisualRemaining=pose.equals("attack")?.2f:0;m.hitFlash=0;
     Bitmap normal=render(r,m);s.skillEffects().put(m.id,"SK_마법사_002","CURSE",20,10);Bitmap faded=render(r,m);sameAlphaChangedRgb(normal,faded);
     if(pose.equals("idle")&&d==CharacterRenderer.Direction.SW){g.drawText(species,8,row*150+18,label);g.drawBitmap(normal,40,row*150-65,null);g.drawBitmap(faded,300,row*150-65,null);save(normal,species+"-normal");save(faded,species+"-curse");}
     m.hitFlash=.1f;Bitmap fadedHit=render(r,m);s.skillEffects().clear(m.id);Bitmap normalHit=render(r,m);sameAlphaChangedRgb(normalHit,fadedHit);assertFalse(Arrays.equals(pixels(faded),pixels(fadedHit)));m.hitFlash=0;assertArrayEquals(pixels(normal),pixels(render(r,m)));
     normal.recycle();faded.recycle();fadedHit.recycle();normalHit.recycle();
    }
    row++;
   }
   for(String cache:new String[]{"hitCache","curseCache","curseHitCache"})assertTrue(cache,((Map<?,?>)ForestPixelCacheTest.field(r,cache)).size()<=60);
   long bytes=0;for(String cache:new String[]{"hitCache","curseCache","curseHitCache"})for(Object b:((Map<?,?>)ForestPixelCacheTest.field(r,cache)).values())bytes+=((Bitmap)b).getAllocationByteCount();
   assertTrue("three variant caches at most26,542,080bytes",bytes<=26542080L);
  }
  assertEquals(23,seen.size());save(grid,"all23-normal-curse-native");
 }
 @Test public void palettePrewarmKeepsPongAlphaAndAcceptedRucesionColors()throws Exception{
  android.content.Context c=RuntimeEnvironment.getApplication();
  for(String pose:new String[]{"idle","walk","attack"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   String path=PoteForestMonsterShowcase.assetPath("ABEL_PONG",pose,d);Bitmap raw=BitmapFactory.decodeStream(c.getAssets().open(path));int[] before=pixels(raw);Bitmap normal=CoastSpritePalette.apply(path,raw);int[] after=pixels(normal);int changed=0;for(int i=0;i<before.length;i++){assertEquals(before[i]>>>24,after[i]>>>24);if(before[i]!=after[i])changed++;}assertTrue(changed>500);normal.recycle();
   String accepted=PoteForestMonsterShowcase.assetPath("LYK_TYRANT",pose,d);Bitmap keep=BitmapFactory.decodeStream(c.getAssets().open(accepted));assertSame(keep,CoastSpritePalette.apply(accepted,keep));keep.recycle();
  }
 }
 @Test public void mouseUsesLiveCurseWithoutLosingHitAndRestores()throws Exception{
  RuntimeState.Monster mouse=new RuntimeState.Monster("mouse-fixture","mouse",0,0,100,"ADAPTED");SkillEffectState effects=new SkillEffectState();InnMouseRenderer r=new InnMouseRenderer(RuntimeEnvironment.getApplication());
  Bitmap normal=Bitmap.createBitmap(120,120,Bitmap.Config.ARGB_8888);r.draw(new Canvas(normal),mouse,60,80,effects);
  effects.put(mouse.id,"SK_마법사_002","CURSE",20,10);Bitmap faded=Bitmap.createBitmap(120,120,Bitmap.Config.ARGB_8888);r.draw(new Canvas(faded),mouse,60,80,effects);sameAlphaChangedRgb(normal,faded);
  mouse.hitFlash=.1f;Bitmap hit=Bitmap.createBitmap(120,120,Bitmap.Config.ARGB_8888);r.draw(new Canvas(hit),mouse,60,80,effects);sameAlphaChangedRgb(faded,hit);mouse.hitFlash=0;effects.clear(mouse.id);Bitmap restored=Bitmap.createBitmap(120,120,Bitmap.Config.ARGB_8888);r.draw(new Canvas(restored),mouse,60,80,effects);assertArrayEquals(pixels(normal),pixels(restored));
 }
 @Test public void normalPongMatchesIndependentVideoMaterialSamples(){
  RuntimeState s=coast();PoteFieldRenderer r=new PoteFieldRenderer();r.prepareMonsters(s);RuntimeState.Monster m=pong(s);m.visualFacing.setLocomotion(CharacterRenderer.Direction.SW);Bitmap b=render(r,m);
  List<Float> goldS=new ArrayList<>(),goldV=new ArrayList<>(),violetS=new ArrayList<>(),violetV=new ArrayList<>();float[] hsv=new float[3];
  for(int p:pixels(b)){if(Color.alpha(p)<200)continue;Color.colorToHSV(p,hsv);if(hsv[1]<.2f)continue;
   if(hsv[0]>10&&hsv[0]<65){goldS.add(hsv[1]);goldV.add(hsv[2]);}
   if(hsv[0]>240&&hsv[0]<310){violetS.add(hsv[1]);violetV.add(hsv[2]);}
  }
  assertTrue(goldS.size()>100);assertTrue(violetS.size()>100);
  // Raw9.5s source patches: gold193/139/91, violet91/37/121; independent of adapter constants.
  for(List<Float> v:Arrays.asList(goldS,goldV,violetS,violetV))Collections.sort(v);
  assertEquals(.537f,goldS.get(goldS.size()/2),.10f);assertEquals(.757f,goldV.get(goldV.size()/2),.10f);
  assertEquals(.700f,violetS.get(violetS.size()/2),.12f);assertEquals(.475f,violetV.get(violetV.size()/2),.10f);
 }
 @Test public void respawnAndNewMapCannotRetainOldCurse()throws Exception{
  RuntimeState s=coast();RuntimeState.Monster m=pong(s);s.skillEffects().put(m.id,"SK_마법사_002","CURSE",20,100);s.damage(m,m.hp);s.player().x=-10000;s.player().y=-10000;s.tick(m.respawnSeconds+1);assertTrue(m.alive);assertFalse(MonsterCursePalette.active(s.skillEffects(),m.id));
  s.skillEffects().put(m.id,"SK_마법사_002","CURSE",20,100);s.skillEffects().put("player","fixture","CURSE",20,100);s.enterCampaignMap("MAP_ABEL_COAST_2A",false);s.enterCampaignMap("MAP_ABEL_COAST_1A",false);assertFalse(MonsterCursePalette.active(s.skillEffects(),pong(s).id));assertTrue("player lifetime preserved",MonsterCursePalette.active(s.skillEffects(),"player"));
  RuntimeState fresh=coast();PoteFieldRenderer r=new PoteFieldRenderer();r.prepareMonsters(s);r.prepareMonsters(fresh);assertSame(fresh.skillEffects(),ForestPixelCacheTest.field(r,"monsterEffects"));
 }
}
