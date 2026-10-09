package com.projectdark.mobile;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class InstalledSpriteResourcesTest {
 private static final String INSTALLED="com.projectdark.mobile.v115test";
 private Context base(){return RuntimeEnvironment.getApplication();}
 // Simulate Android's suffixed resource table; Robolectric otherwise keeps R's Java namespace.
 private Context installed(){
  Resources original=base().getResources();
  Resources relocated=new Resources(original.getAssets(),original.getDisplayMetrics(),original.getConfiguration()) {
   @Override public String getResourcePackageName(int id){original.getResourcePackageName(id);return INSTALLED;}
   @Override public int getIdentifier(String name,String type,String pkg){
    if(!INSTALLED.equals(pkg))return 0;
    return original.getIdentifier(name,type,original.getResourcePackageName(R.drawable.hud_attack_button));
   }
  };
  return new ContextWrapper(base()){
   @Override public Resources getResources(){return relocated;}
   @Override public String getPackageName(){return INSTALLED;}
  };
 }
 private CharacterRenderer.Pose pose(CharacterRenderer.Direction d,CharacterRenderer.State state,float clock){
  return new CharacterRenderer.Pose(80,140,d,state,clock,clock,1,false,"mu0000001,ml228,mh172,ms001","mw001",CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE);
 }
 private Bitmap player(Context context,CharacterRenderer.Pose pose){
  Bitmap b=Bitmap.createBitmap(160,160,Bitmap.Config.ARGB_8888);new CharacterRenderer(context).draw(new Canvas(b),pose);return b;
 }
 private Bitmap npc(Context context,NpcIdentity.Profile profile){
  Bitmap b=Bitmap.createBitmap(160,160,Bitmap.Config.ARGB_8888);new NpcActorRenderer(context).draw(new Canvas(b),profile.id,80,140,CharacterRenderer.Direction.SW,CharacterRenderer.State.IDLE,0);return b;
 }
 private int[] pixels(Bitmap b){int[] result=new int[b.getWidth()*b.getHeight()];b.getPixels(result,0,b.getWidth(),0,0,b.getWidth(),b.getHeight());return result;}
 @Test public void oldNamespaceFailsButInstalledTableLoadsBodyActionsAndEveryNpcOutfit(){
  Context context=installed();
  assertEquals(0,context.getResources().getIdentifier(CharacterRenderer.IDLE_WALK_RESOURCE,"drawable","com.projectdark.mobile"));
  CharacterRenderer renderer=new CharacterRenderer(context);
  assertTrue("installed source body",renderer.resourceAtlasActive());
  assertTrue("installed directional actions",renderer.sourceActionActive());
  assertTrue("installed clothes",renderer.equipmentAtlasActive());
  assertTrue("installed weapon",renderer.weaponSourceActive());
  EquipmentVisualRegistry registry=new EquipmentVisualRegistry(context);
  for(NpcIdentity.Profile profile:NpcIdentity.ALL)for(String appearance:profile.outfit.split(",")){
   EquipmentVisualRegistry.Visual visual=registry.get(appearance);
   assertNotNull(profile.id+":"+appearance,visual);assertNotNull(visual.registration);
  }
 }
 @Test public void fourWayIdleWalkAndAttackPixelsMatchAcceptedBodyUnderRelocatedPackage()throws Exception{
  Bitmap sheet=Bitmap.createBitmap(1280,960,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);canvas.drawColor(0xff314b30);
  int row=0;
  for(CharacterRenderer.State state:new CharacterRenderer.State[]{CharacterRenderer.State.IDLE,CharacterRenderer.State.WALK,CharacterRenderer.State.ATTACK}){
   int col=0;for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
    for(float clock:new float[]{0.24f,0.5f}){
     CharacterRenderer.Pose p=pose(d,state,clock);Bitmap expected=player(base(),p),actual=player(installed(),p);
     assertArrayEquals(state+" "+d+" "+clock,pixels(expected),pixels(actual));
     canvas.drawBitmap(actual,col++*160,row*320,null);
    }
   }row++;
  }
  write(sheet,"v115-installed-player");
 }
 @Test public void everyNpcMatchesAcceptedSpriteUnderRelocatedPackage()throws Exception{
  Bitmap sheet=Bitmap.createBitmap(640,NpcIdentity.ALL.size()*160,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);canvas.drawColor(0xff314b30);
  Paint label=new Paint();label.setColor(Color.WHITE);label.setTextSize(16);int row=0;
  for(NpcIdentity.Profile profile:NpcIdentity.ALL){
   Bitmap expected=npc(base(),profile),actual=npc(installed(),profile);assertArrayEquals(profile.id,pixels(expected),pixels(actual));
   canvas.drawBitmap(expected,0,row*160,null);canvas.drawBitmap(actual,180,row*160,null);canvas.drawText(profile.id,350,row*160+80,label);row++;
  }
  write(sheet,"v115-installed-npc");
 }
 private void write(Bitmap bitmap,String name)throws Exception{
  File path=new File("build/reports/device-review/"+name+".png");path.getParentFile().mkdirs();
  try(OutputStream out=new FileOutputStream(path)){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));}
 }
}
