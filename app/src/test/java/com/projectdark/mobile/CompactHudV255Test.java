package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import android.view.MotionEvent;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CompactHudV255Test {
 final MobileHudV134Test h=new MobileHudV134Test();
 @Before public void setup(){h.setup();h.c.getSharedPreferences(QuickItemBarV254.PREFS,0).edit().clear().commit();}
 @Test public void compactGridStaysTenPixelsBesideCenteredResourcesAtBothWidthsAndEveryCornerUsesItsOwnSlot()throws Exception{
  for(int[] size:new int[][]{{960,540},{2340,1080}}){
   GameView v=new GameView(h.c);v.layout(0,0,size[0],size[1]);float scale=size[1]/540f,d=size[0]/scale-960;
   RectF status=MobileHudLayout.status(d);assertEquals(status.right+10,h.slot(v,0).left,0);assertEquals(513,h.slot(v,11).bottom,0);assertEquals(MobileHudLayout.potion(0,d).top,h.slot(v,0).top,0);
   for(int i=0;i<12;i++){RectF r=h.slot(v,i);assertEquals(36,r.width(),0);assertEquals(36,r.height(),0);assertTrue(r.left>status.right);assertTrue(r.right<869+d);assertTrue(r.bottom<540);
    assertEquals(h.slot(v,i%6).left,r.left,0);assertEquals(i<6?438:477,r.top,0);
    for(int p=0;p<6;p++)assertFalse(RectF.intersects(r,MobileHudLayout.itemBar(p,d)));
    if(i%6>0)assertEquals(3,r.left-h.slot(v,i-1).right,0);
    for(float[] point:new float[][]{{r.left+1,r.top+1},{r.right-1,r.bottom-1}}){
     MotionEvent e=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,point[0]*scale,point[1]*scale,0);v.onTouchEvent(e);e.recycle();
     assertEquals("SLOT"+i,h.field(v,"pressedControl"));assertTrue(GameView.blocksWorldTapForHud(point[0],point[1],false,d));
    }
   }
   assertFalse("old upper cluster returns to world",GameView.blocksWorldTapForHud(550+d,350,false,d));
   assertEquals(36,MobileHudLayout.potion(0,d).width(),0);assertEquals(MobileHudLayout.potion(1,d).right,MobileHudLayout.quickItem(0,d).left,0);
  }
 }
 @Test public void actualGameViewShowsAllTwelveCompactSkillsBesideBarsInBothViewports()throws Exception{
  String[] ids={"SK_마법사_001","SK_마법사_003","SK_마법사_004","SK_마법사_005","SK_무도가_006","SK_도적_007","SK_전사_001","SK_성직자_001","SK_마법사_017","SK_마법사_043","SK_마법사_044","SK_성직자_040"};
  for(int[] size:new int[][]{{960,540},{2340,1080}}){h.reset();GameView v=new GameView(h.c);v.layout(0,0,size[0],size[1]);v.setSkillTestMode(true);SkillBook b=h.field(v,"skillBook");for(int i=0;i<12;i++)assertTrue(b.assign(i,ids[i]));
   RuntimeState state=h.field(v,"state");state.rpg().grantB5SmallHpPotion(3);state.rpg().autoLootResolvedItem("IT_B_MP_POTION",3);
   QuickItemBarV254 items=h.field(v,"quickItems");assertTrue(items.assign(0,state.rpg().quickHpPotionId(),state.rpg()));assertTrue(items.assign(1,"IT_B_MP_POTION",state.rpg()));
   Bitmap bitmap=Bitmap.createBitmap(size[0],size[1],Bitmap.Config.ARGB_8888);v.draw(new Canvas(bitmap));File f=new File("build/reports/horror-v179/v255/hud-"+size[0]+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));}bitmap.recycle();
  }
 }
}
