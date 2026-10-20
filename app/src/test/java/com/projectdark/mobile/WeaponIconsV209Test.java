package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import java.io.*;import java.util.*;
import org.junit.*;import org.junit.runner.RunWith;
import org.robolectric.*;import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class WeaponIconsV209Test {
 Context c;MineTownV203Test fixture;@Before public void setup(){c=RuntimeEnvironment.getApplication();fixture=new MineTownV203Test();fixture.reset();}
 @Test public void everyRegisteredWeaponUsesDiagonalSharedProjectionIncludingStaves()throws Exception{
  RpgProgressionState r=new RpgProgressionState();SourceItemIconRegistry icons=new SourceItemIconRegistry(c);int count=0,elongated=0;List<String> audit=new ArrayList<>();Bitmap gallery=Bitmap.createBitmap(960,1800,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(gallery);canvas.drawColor(0xff302820);Paint p=new Paint();
  for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values())if("무기".equals(d.equipSlot)){
   Bitmap source=icons.get(d.itemId),icon=icons.get(d);if(source==null){assertTrue("only explicit source pending",icons.pending(d.itemId)||icons.receipt(d.itemId)==null);continue;}
   assertEquals(d.itemId,32,icon.getWidth());assertEquals(32,icon.getHeight());assertSame(icon,icons.get(d));float axis=WeaponInventoryArtworkV209.axisDegrees(icon),length=WeaponInventoryArtworkV209.elongation(icon);
   if(length>3){assertTrue(d.name+" diagonal="+axis,Math.abs(axis+45)<9);elongated++;}audit.add(d.itemId+","+d.name+","+axis+","+length);SourceItemIconRegistry.draw(canvas,icon,new RectF(count%20*48,count/20*48,count%20*48+44,count/20*48+44),p);count++;
  }assertTrue(count>100);assertTrue("swords and staff long axes",elongated>30);save(gallery,"all-weapons");File f=new File("build/reports/horror-v179/v209-weapon-axis.csv");try(PrintWriter out=new PrintWriter(f)){out.println("itemId,name,axisDegrees,elongation");for(String row:audit)out.println(row);}
 }
 @Test public void actualGameViewInventoryAndEquipmentCallbacksDrawIdenticalWeaponPixels()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=fixture.field(v,"state");s.rpg().enableEquipmentSandbox(true);Map<String,Integer> owned=new LinkedHashMap<>();for(String id:new String[]{"IT_ADAPTED_PLAYTEST_MOKDO","IT_B_JOB_CLERIC_WAND","USER_ITEM_20261018_R05502","USER_ITEM_20261018_R01533"})owned.put(id,1);assertTrue(s.rpg().restoreOwnedItems(owned,Collections.emptyMap()));
  ItemWindow.Visuals visuals=fixture.field(v,"itemWindowVisuals");int n=0;Bitmap comparison=Bitmap.createBitmap(320,160,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(comparison);
  for(RpgInventoryPresentation.ItemRow row:new RpgInventoryPresentation().inventoryRows(s.rpg())){
   Bitmap bag=Bitmap.createBitmap(40,40,Bitmap.Config.ARGB_8888),gear=Bitmap.createBitmap(40,40,Bitmap.Config.ARGB_8888);visuals.icon(new Canvas(bag),row,0,0,40);visuals.equipmentIcon(new Canvas(gear),row,"무기",0,0,40);assertTrue(row.name,bag.sameAs(gear));out.drawBitmap(bag,n*80,0,null);out.drawBitmap(gear,n*80,80,null);n++;
  }assertEquals(4,n);save(comparison,"bag-equipment-same-icons");
 }
 private void save(Bitmap b,String name)throws Exception{File f=new File("build/reports/horror-v179/v209-"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
}
