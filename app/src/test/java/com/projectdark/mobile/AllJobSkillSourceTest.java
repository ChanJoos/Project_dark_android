package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AllJobSkillSourceTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 @Test public void warriorAndSharedAssetsRetainExactIdsBytesAndHonestTiming()throws Exception{
  SkillBook book=SkillBook.load(c);assertEquals("마법",book.get("SK_전사_018").kind);assertEquals("CAST",new SkillPresentationCatalog(c).get("SK_전사_018").motion);
  ClassicSkillReference warrior=new ClassicSkillReference(c,"warrior");assertEquals(23,warrior.references.size());assertEquals(13,warrior.effects.size());
  SkillIconCatalog icons=new SkillIconCatalog(c);for(JSONObject row:warrior.references.values()){
   assertTrue(row.getString("id"),icons.has(row.getString("id")));assertEquals(row.getString("sourceSha256"),sha(java.nio.file.Files.readAllBytes(new File("../"+row.getString("sourcePath")).toPath())));
   Bitmap original=BitmapFactory.decodeFile("../"+row.getString("sourcePath"));Bitmap imported=BitmapFactory.decodeStream(c.getAssets().open("skill-presentation/warrior/"+row.getString("iconAssetPath")));assertEquals(original.getWidth(),imported.getWidth());assertEquals(original.getHeight(),imported.getHeight());
   for(int y=0;y<original.getHeight();y++)for(int x=0;x<original.getWidth();x++)assertEquals(original.getPixel(x,y),imported.getPixel(x,y));
  }
  for(String directory:new String[]{"warrior","shared"}){
   JSONObject document=new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/"+directory+"/manifest.json"),StandardCharsets.UTF_8));assertFalse(document.getBoolean("nativeArchivePixels"));
   ClassicSkillReference reference=new ClassicSkillReference(c,directory);
   for(Map<String,ClassicSkillReference.Channel> map:reference.effects.values())for(ClassicSkillReference.Channel channel:map.values())assertTrue(channel.sequence.duration>0);
  }
 }
 @Test public void everyCatalogPoseRendersForBothBodiesAndEveryDirection()throws Exception{
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);SkillBodyRenderer renderer=new SkillBodyRenderer(c,catalog);
  SkillBook book=SkillBook.load(c);Map<String,List<SkillPresentationCatalog.Entry>> jobs=new LinkedHashMap<>();for(SkillPresentationCatalog.Entry entry:catalog.entries.values())jobs.computeIfAbsent(book.get(entry.id).job,k->new ArrayList<>()).add(entry);
  for(Map.Entry<String,List<SkillPresentationCatalog.Entry>> job:jobs.entrySet())for(String body:new String[]{"mm001","wm001"}){
   Bitmap image=Bitmap.createBitmap(960,Math.max(480,((job.getValue().size()+7)/8)*180),Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);canvas.drawColor(0xff253129);Paint paint=new Paint();paint.setColor(Color.WHITE);paint.setTextSize(9);
   for(int i=0;i<job.getValue().size();i++){
    SkillPresentationCatalog.Entry entry=job.getValue().get(i);float x=i%8*120+16,y=i/8*180+100;
    for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
     String key=catalog.frameKey(body,entry.motion,direction,.4f);assertTrue(entry.id+" "+key,catalog.frames.has(key));
     CharacterRenderer.Pose pose=new CharacterRenderer.Pose(x+direction.ordinal()*27,y,direction,CharacterRenderer.State.SKILL,0,.2f,.5f,false,"","",null,CharacterRenderer.EffectFamily.SKILL);
     assertTrue(entry.id+" "+direction,renderer.draw(canvas,pose,body,entry.motion,.4f));
    }canvas.drawText(entry.id.substring(3),x-10,y+35,paint);canvas.drawText(entry.motion,x-10,y+48,paint);
   }save(image,"alljob-body-"+job.getValue().get(0).id.split("_")[1]+"-"+body+".png");
  }
 }
 @Test public void realDamageImpactIsIndependentRecipientOnlyAndNeverAppearsForMissHealOrPreview(){
  SkillVfxRenderer renderer=new SkillVfxRenderer(c,new SkillPresentationCatalog(c));SkillVfxRenderer.Anchors anchors=new SkillVfxRenderer.Anchors(){public float x(String id){return "player".equals(id)?100:300;}public float y(String id){return 200;}};
  String[] ids={"SK_전사_001","SK_도적_007","SK_마법사_005","SK_무도가_002","SK_성직자_013","SK_공통_001"};long sequence=500;
  for(String id:ids){
   CombatResolver.Definition def=new CombatResolver.Definition(id,CombatResolver.ActionKind.MAGIC,CombatResolver.ActionState.MAGIC,CombatResolver.EffectType.MAGIC_HIT,true,0,1,150,.24f,12);
   for(CombatResolver.HitSemantic semantic:CombatResolver.HitSemantic.values()){
    renderer.clear();CombatResolver.Event event=new CombatResolver.Event(++sequence,sequence,CombatResolver.EventType.HIT_FEEDBACK,"player","monster",def,CombatResolver.InputMode.MANUAL,null,semantic,semantic==CombatResolver.HitSemantic.MISS?0:12);
    renderer.consume(Arrays.asList(event,event),anchors);long impacts=renderer.pulses.stream().filter(p->p.sheet.equals("impact")).count();assertEquals(id+" "+semantic,semantic==CombatResolver.HitSemantic.DAMAGE||semantic==CombatResolver.HitSemantic.CRIT?1:0,impacts);
    for(SkillVfxRenderer.Pulse pulse:renderer.pulses)if(pulse.sheet.equals("impact")){assertEquals("monster",pulse.anchor);assertFalse(pulse.caster);assertEquals(300,pulse.x,0);}
   }
   renderer.clear();renderer.consume(Collections.singletonList(new CombatResolver.Event(++sequence,sequence,CombatResolver.EventType.HIT_FEEDBACK,"player","monster",def,CombatResolver.InputMode.MANUAL,null,CombatResolver.HitSemantic.DAMAGE,0)),anchors);assertFalse(renderer.pulses.stream().anyMatch(p->p.sheet.equals("impact")));
  }
 }
 @Test public void allJobsUseProductionInputAndRenderSkillAndActualRecipientTogether()throws Exception{
  String[] ids={"SK_전사_001","SK_전사_014","SK_전사_018","SK_전사_023","SK_도적_007","SK_마법사_001","SK_마법사_005","SK_마법사_056","SK_무도가_007","SK_무도가_020","SK_성직자_013","SK_성직자_005","SK_공통_001"};
  for(String id:ids){
   F5mSaveStore.install(c);GameView view=new GameView(c);view.layout(0,0,960,540);view.setSkillTestMode(true);RuntimeState state=field(view,"state");SkillActionContract.Rule rule=SkillActionContract.get(id);RuntimeState.Monster monster=state.monsters().get(0);state.player().x=monster.x-32*Math.max(1,rule.minReach);state.player().y=monster.y-16*Math.max(1,rule.minReach);state.player().hp=5;state.player().mp=0;
   ((CombatController)field(view,"combat")).selectTarget(monster);((com.projectdark.mobile.world.WorldRuntimeAdapter)field(view,"worldAdapter")).snapCameraToPlayer();
   Method use=GameView.class.getDeclaredMethod("useBookSkill",SkillBook.Entry.class);use.setAccessible(true);use.invoke(view,((SkillBook)field(view,"skillBook")).get(id));Method tick=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);tick.setAccessible(true);tick.invoke(view,rule.contact+.001f);
   SkillVfxRenderer fx=field(view,"skillVfx");if(rule.heal())assertFalse(id,fx.pulses.stream().anyMatch(p->p.sheet.equals("impact")));for(SkillVfxRenderer.Pulse pulse:fx.pulses)pulse.age=Math.min(.10f,pulse.duration/2);
   Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(image));save(image,"alljob-live-"+id+".png");fx.tick(30);assertTrue(fx.pulses.isEmpty());
  }
 }
 private static void save(Bitmap image,String name)throws Exception{File file=new File("build/reports/device-review/"+name);file.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(file)){image.compress(Bitmap.CompressFormat.PNG,100,out);}}
 private static String sha(byte[] bytes)throws Exception{StringBuilder s=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}
 @SuppressWarnings("unchecked")private static <T>T field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return(T)f.get(o);}
}
