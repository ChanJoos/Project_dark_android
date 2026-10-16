package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.util.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class EquipmentWindowV176Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String n:new String[]{"project_dark_f5m_v1","project_dark_skill_test_v1"})c.getSharedPreferences(n,0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 @Test public void shieldIsDirectlyBelowEarringAndOrnamentsWithEvenRails(){
  RectF e=ItemWindow.slot(1),o=ItemWindow.slot(13),s=ItemWindow.slot(12);assertEquals(e.left,o.left,0);assertEquals(o.left,s.left,0);assertEquals(o.top-e.top,s.top-o.top,.001f);assertTrue(s.top>o.bottom);
  for(int[] pair:new int[][]{{5,7},{8,6}}){RectF a=ItemWindow.slot(pair[0]),b=ItemWindow.slot(pair[1]);assertEquals(11*.85f,b.left-a.right,.001f);}
  assertEquals(261,ItemWindow.POS[0][0]+35,0);
 }
 @Test public void equipmentProjectionFillsVisibleBoundsPreservesSourceDirectionAndMirrorsOnlyThePairedHand(){
  Bitmap src=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888);Canvas sc=new Canvas(src);Paint p=new Paint();p.setColor(Color.RED);sc.drawRect(8,8,16,24,p);p.setColor(Color.BLUE);sc.drawRect(16,8,24,24,p);
  RectF target=new RectF(10,10,64,64);Bitmap normal=Bitmap.createBitmap(80,80,Bitmap.Config.ARGB_8888),right=Bitmap.createBitmap(80,80,Bitmap.Config.ARGB_8888);
  SourceItemIconRegistry.drawEquipment(new Canvas(normal),src,target,p,false);SourceItemIconRegistry.drawEquipment(new Canvas(right),src,target,p,true);
  assertEquals(Color.RED,normal.getPixel(15,36));assertEquals(Color.BLUE,normal.getPixel(58,36));assertEquals(Color.BLUE,right.getPixel(15,36));assertEquals(Color.RED,right.getPixel(58,36));
  assertEquals(0,Color.alpha(normal.getPixel(9,36)));assertEquals(0,Color.alpha(normal.getPixel(64,36)));assertEquals(16,SourceItemIconRegistry.visibleEquipmentBounds(src).width());
 }
 @Test public void emptySocketsArePureSourceTextureWithNoSyntheticGlyphAndLowerRightHingeIsGone(){
  ItemWindow w=new ItemWindow(c);RpgProgressionState r=new RpgProgressionState();assertTrue(r.restoreOwnedItems(r.inventory(),Collections.emptyMap()));final int[] calls={0};
  Bitmap actual=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);w.equipment(new Canvas(actual),r,null,new ItemWindow.Visuals(){public void icon(Canvas c,RpgInventoryPresentation.ItemRow row,float x,float y,float size){calls[0]++;}public void actor(Canvas c,float x,float y){}});assertEquals(0,calls[0]);
  Bitmap expected=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);Canvas ec=new Canvas(expected);ec.translate(ItemWindow.EQUIP_X,ItemWindow.EQUIP_Y);ec.scale(ItemWindow.EQUIP_SCALE,ItemWindow.EQUIP_SCALE);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);System.out.println("EXPECTED_FILTER_BEFORE="+p.isFilterBitmap()+" ACTUAL_FILTER="+w.p.isFilterBitmap());p.setFilterBitmap(false);ec.drawBitmap(w.equipmentImages.get("panel"),0,0,p);for(float[] pos:ItemWindow.POS)ec.drawBitmap(w.equipmentImages.get("slot"),null,new RectF(pos[0],pos[1],pos[0]+70,pos[1]+70),p);
  int maxDelta=0;String worst="";
  for(int i=0;i<ItemWindow.SLOTS.length;i++){RectF b=ItemWindow.slot(i);for(int y=(int)b.top+15;y<(int)b.bottom-15;y++)for(int x=(int)b.left+15;x<(int)b.right-15;x++){int a=expected.getPixel(x,y),bpx=actual.getPixel(x,y);assertEquals(255,Color.alpha(bpx));for(int shift:new int[]{0,8,16}){int delta=Math.abs(((a>>shift)&255)-((bpx>>shift)&255));if(delta>maxDelta){maxDelta=delta;worst=ItemWindow.SLOTS[i]+" "+x+","+y+" expected="+a+" actual="+bpx;}}}}
  File debug=new File("build/reports/equipment-v176");debug.mkdirs();try(FileOutputStream a=new FileOutputStream(new File(debug,"empty-fixture-actual.png"));FileOutputStream e=new FileOutputStream(new File(debug,"empty-fixture-expected.png"))){actual.compress(Bitmap.CompressFormat.PNG,100,a);expected.compress(Bitmap.CompressFormat.PNG,100,e);}catch(IOException e){throw new RuntimeException(e);}
  System.out.println("EMPTY_SLOT_MAX_CHANNEL_DELTA="+maxDelta+" "+worst);assertTrue(worst,maxDelta<=2);
  Bitmap panel=w.equipmentImages.get("panel");int gold=0;for(int y=510;y<556;y++)for(int x=480;x<520;x++){int v=panel.getPixel(x,y);if(Color.red(v)>170&&Color.green(v)>100&&Color.blue(v)<95)gold++;}assertEquals(0,gold);
 }
 @Test public void actualGameViewRendersStarterFilledEmptyAndWideEquipmentAndClosesAtTheDrawnCross()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(v,"state");RpgProgressionState r=state.rpg();ItemWindowReferenceTest.tap(v,830,28);render(v,"starter.png");
  r.enableEquipmentSandbox(true);r.equipToSlot("IT_EARRING_DOUBLE_SILVER","귀걸이");r.equipToSlot("IT_GLOVE_LEATHER","장갑");r.equipToSlot("IT_GLOVE_LEATHER",RpgProgressionState.RIGHT_GLOVE_SLOT);r.equipToSlot("IT_RING_REDJADE","반지");r.equipToSlot("IT_RING_REDJADE",RpgProgressionState.RIGHT_RING_SLOT);
  for(String slot:new String[]{"목걸이","벨트","각반"})for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values())if(slot.equals(d.equipSlot)){r.equipToSlot(d.itemId,slot);break;}
  render(v,"filled.png");r.restoreOwnedItems(r.inventory(),Collections.emptyMap());render(v,"empty.png");RectF close=ItemWindow.equipmentClose();ItemWindowReferenceTest.tap(v,close.centerX(),close.centerY());assertFalse((Boolean)ItemWindowReferenceTest.field(v,"equipmentOpen"));
  GameView wide=new GameView(c);wide.layout(0,0,2340,1080);float right=GameView.rightHudOffsetForView(2340,1080),center=right/2;ItemWindowReferenceTest.tap(wide,(830+right)*2,56);render(wide,"wide.png");ItemWindowReferenceTest.tap(wide,(close.centerX()+center)*2,close.centerY()*2);assertFalse((Boolean)ItemWindowReferenceTest.field(wide,"equipmentOpen"));
 }
 void render(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File d=new File("build/reports/equipment-v176");d.mkdirs();try(FileOutputStream o=new FileOutputStream(new File(d,name))){b.compress(Bitmap.CompressFormat.PNG,100,o);}b.recycle();}
}
