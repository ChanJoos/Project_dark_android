package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.BitmapFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.util.zip.*;
import java.util.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RapidSourceV198Test {
 @Test public void packagedSourceBytesMatchEveryRetainedOriginal()throws Exception{
  Context c=RuntimeEnvironment.getApplication();int checked=0;
  try(ZipFile z=new ZipFile("../master/source/items/rapid_v198/original_sources.zip")){
   Enumeration<? extends ZipEntry> all=z.entries();
   while(all.hasMoreElements()){ZipEntry e=all.nextElement();String n=e.getName();if(n.equals("receipt.json"))continue;String id=n.substring(n.lastIndexOf('/')+1);String path=(n.endsWith(".json")?"source-registration/":"wearable-atlases/")+id;assertArrayEquals(n,z.getInputStream(e).readAllBytes(),PresentationAssetBytes.read(c,path));checked++;}
  }assertEquals(32,checked);
 }
 @Test public void bothBodiesHaveAllTenIdleAndFourContactFramesInBounds()throws Exception{
  Context c=RuntimeEnvironment.getApplication();EquipmentVisualRegistry registry=new EquipmentVisualRegistry(c);
  for(String prefix:new String[]{"mg","mc","wg","wc"})for(int n=146;n<=149;n++){
   String id=prefix+n;EquipmentVisualRegistry.Visual v=registry.get(id);assertNotNull(id,v);assertNotNull(id,v.registration);
   for(int f=0;f<10;f++)bounds(id,v,v.registration.idleByIndex(f));
   for(int f=0;f<4;f++)bounds(id,v,v.registration.action(f));
  }
 }
 void bounds(String id,EquipmentVisualRegistry.Visual v,SourceEquipmentRegistration.Frame f){assertNotNull(id,f);assertTrue(id+" rect="+f.src+" atlas="+v.atlas.getWidth()+"x"+v.atlas.getHeight(),f.src.left>=0&&f.src.top>=0&&f.src.right<=v.atlas.getWidth()&&f.src.bottom<=v.atlas.getHeight());}
}
