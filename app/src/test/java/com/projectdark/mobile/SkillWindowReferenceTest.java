package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.*;
import android.graphics.*;
import android.view.MotionEvent;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SkillWindowReferenceTest {
  Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  @Test public void originalPanelsAndAllCatalogItemsRemainReachableThroughProductionInput()throws Exception{
    GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);SkillWindow w=field(v,"skillWindow");SkillBook book=field(v,"skillBook");tap(v,770,28);assertTrue(w.open);
    String[] jobs={"공통","마법사","도적","전사","무도가","성직자","전체"};Set<String> seen=new HashSet<>();
    for(int target=0;target<jobs.length;target++){
      while(w.tabFirst>target)tap(v,47,90);while(w.tabFirst+2<target)tap(v,615,90);tap(v,133+(target-w.tabFirst)*191,90);assertEquals(jobs[target],w.job);
      for(SkillWindow.Item item:w.layout()){
        w.reveal(item.entry.id);RectF r=w.rectFor(item.entry.id);assertTrue(r.centerY()>=SkillWindow.VIEWPORT.top&&r.centerY()<=SkillWindow.VIEWPORT.bottom);tap(v,r.centerX(),r.centerY());assertEquals(item.entry.id,w.selectedId);seen.add(w.selectedId);
      }
      render(v,"skill-window-v72-"+target+".png");
    }
    assertEquals(book.catalogSize(),seen.size());
    w.showJob("도적");w.reveal("SK_도적_014");w.selectedId="SK_도적_025";render(v,"skill-window-v72-reference-rogue.png");
    tap(v,850,400);tap(v,677,460);assertEquals("SK_도적_025",book.slot(0));tap(v,611,59);assertFalse(w.open);
  }
  @Test public void realScrollClipsHitsAndNeverMovesTheWorld()throws Exception{
    GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);SkillWindow w=field(v,"skillWindow");RuntimeState state=field(v,"state");w.open=true;w.showJob("전체");float x=state.player().x,y=state.player().y;
    event(v,MotionEvent.ACTION_DOWN,450,450);event(v,MotionEvent.ACTION_MOVE,450,150);event(v,MotionEvent.ACTION_UP,450,150);assertEquals(300,w.scroll,.01);assertEquals(x,state.player().x,0);assertEquals(y,state.player().y,0);
    assertTrue(w.maxScroll()>w.scroll);String selected=w.selectedId;tap(v,100,530);assertEquals(selected,w.selectedId);render(v,"skill-window-v72-scrolled.png");
    w.showJob("도적");assertEquals(0,w.scroll,0);for(SkillWindow.Item item:w.layout()){assertEquals(49,item.rect.width(),0);assertEquals(57,item.rect.height(),0);}assertTrue(w.rows().stream().anyMatch(e->e.magic()));assertTrue(w.rows().stream().anyMatch(e->!e.magic()));
  }
  @Test public void latestSquareIconUsesExactReferenceCropPixels()throws Exception{
    SkillIconCatalog icons=new SkillIconCatalog(c);Bitmap actual=Bitmap.createBitmap(65,65,Bitmap.Config.ARGB_8888);assertTrue(icons.drawSquare(new Canvas(actual),"SK_도적_025",new RectF(0,0,65,65)));
    Bitmap source=BitmapFactory.decodeStream(c.getAssets().open("skill-window/rogue_025.png"));for(int y=0;y<65;y++)for(int x=0;x<65;x++)assertEquals(source.getPixel(x,y),actual.getPixel(x,y));
    actual.eraseColor(0);assertFalse(icons.drawSquare(new Canvas(actual),"missing-id",new RectF(0,0,65,65)));assertEquals(0,actual.getPixel(32,32));
  }
  private static void event(GameView v,int action,float x,float y){MotionEvent e=MotionEvent.obtain(0,0,action,x,y,0);v.onTouchEvent(e);e.recycle();}
  private static void tap(GameView v,float x,float y){event(v,MotionEvent.ACTION_DOWN,x,y);}
  @SuppressWarnings("unchecked") private static <T>T field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return(T)f.get(o);}
  private static void render(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File d=new File("build/reports/device-review");d.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(d,name))){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
}
