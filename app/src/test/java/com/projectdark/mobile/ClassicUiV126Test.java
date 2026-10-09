package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ClassicUiV126Test {
 Context c;
 @Before public void before(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 @Test public void everySeparatedIconCompositesWithoutItsOldRectangularSocket()throws Exception{
  JSONObject manifest=new JSONObject(new String(PresentationAssetBytes.read(c,"equipment-icons/manifest.json"),"UTF-8"));JSONObject items=manifest.getJSONObject("items");Set<String> seen=new HashSet<>();int tested=0;
  for(Iterator<String> it=items.keys();it.hasNext();){JSONObject e=items.getJSONObject(it.next());if(!e.has("alphaMask")||!seen.add(e.getString("asset")))continue;
   Bitmap b=BitmapFactory.decodeStream(c.getAssets().open("equipment-icons/"+e.getString("asset")));assertNotNull(b);
   assertEquals(0,Color.alpha(b.getPixel(0,0)));assertEquals(0,Color.alpha(b.getPixel(b.getWidth()-1,b.getHeight()-1)));
   int foreground=0;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(Color.alpha(b.getPixel(x,y))>0)foreground++;
   assertTrue(e.getString("asset"),foreground>5&&foreground<b.getWidth()*b.getHeight()*.85);
   for(int background:new int[]{0xff603f25,0xff234567}){Bitmap canvas=Bitmap.createBitmap(b.getWidth(),b.getHeight(),Bitmap.Config.ARGB_8888);canvas.eraseColor(background);new Canvas(canvas).drawBitmap(b,0,0,null);assertEquals(background,canvas.getPixel(0,0));assertEquals(background,canvas.getPixel(b.getWidth()-1,b.getHeight()-1));}
   tested++;
  }
  assertEquals(157,tested);
 }
 @Test public void actualAllPagesAndFourWindowsRenderWithSourceBrownSkin()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(v,"state");Map<String,Integer> owned=new LinkedHashMap<>(state.rpg().inventory());ItemWindow w=ItemWindowReferenceTest.field(v,"itemWindow");
  ItemWindowReferenceTest.tap(v,608,28);
  for(int page=0;page<5;page++){assertEquals(page,w.page);save(v,"inventory-page-"+(page+1),new Rect(330,50,938,518));if(page<4)ItemWindowReferenceTest.tap(v,894,480);}
  ItemWindowReferenceTest.tap(v,916,66);ItemWindowReferenceTest.tap(v,716,28);save(v,"equipment",new Rect(226,50,510,517));
  ItemWindowReferenceTest.tap(v,492,66);ItemWindowReferenceTest.tap(v,662,28);save(v,"stats",new Rect(572,74,934,388));
  ItemWindowReferenceTest.tap(v,911,91);ItemWindowReferenceTest.tap(v,770,28);save(v,"skills",new Rect(37,47,626,530));
  assertEquals(owned,state.rpg().inventory());
 }
 void save(GameView v,String name,Rect panel)throws Exception{
  Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));int brown=0,total=0;
  for(int y=panel.top;y<panel.bottom;y+=3)for(int x=panel.left;x<panel.right;x+=3){int rgb=b.getPixel(x,y);total++;if(Color.red(rgb)>Color.green(rgb)&&Color.green(rgb)>Color.blue(rgb))brown++;}
  assertTrue(name+" brown coverage "+brown+"/"+total,brown>total*.65);
  File dir=new File("build/reports/classic-ui-v126");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}
 }
}
