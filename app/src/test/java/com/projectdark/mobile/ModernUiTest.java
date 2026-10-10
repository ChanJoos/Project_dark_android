package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

/** Real GameView rendering and modal input at phone and wide aspect ratios. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ModernUiTest {
  Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String n:new String[]{"project_dark_f5m_v1","project_dark_journal_v1","project_dark_skill_test_v1"})c.getSharedPreferences(n,0).edit().clear().commit();F5mSaveStore.install(c);}
  GameView start(int width,int height){GameView v=new GameView(c);v.layout(0,0,width,height);return v;}
  void tap(GameView v,float x,float y){ItemWindowReferenceTest.tap(v,x,y);}
  void capture(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File f=new File("build/reports/device-review/v99-"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();}
  @Test public void everyWindowAndQuickbarRenderWithoutChangingGameState()throws Exception{
    GameView v=start(960,540);RuntimeState state=ItemWindowReferenceTest.field(v,"state");long gold=state.rpg().gold();float x=state.player().x,y=state.player().y;
    SkillBook book=ItemWindowReferenceTest.field(v,"skillBook");book.setTestAccess(true);String[] ids={"SK_전사_012","SK_도적_014","SK_무도가_007","SK_성직자_011"};for(int i=0;i<4;i++)assertTrue(book.assign(i,ids[i]));capture(v,"hud");
    tap(v,742,28);assertTrue((Boolean)ItemWindowReferenceTest.field(v,"inventoryOpen"));capture(v,"inventory");ItemWindow w=ItemWindowReferenceTest.field(v,"itemWindow");int selected=0;for(RpgInventoryPresentation.ItemRow row:w.rows(state.rpg())){if(row.itemId.equals(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID))break;selected++;}RectF cell=ItemWindow.cell(selected);tap(v,cell.centerX(),cell.centerY());assertTrue(w.details);capture(v,"inventory-compare");tap(v,914,250);tap(v,914,60);
    tap(v,830,28);assertTrue((Boolean)ItemWindowReferenceTest.field(v,"equipmentOpen"));capture(v,"equipment");java.util.Map<String,String> before=new java.util.LinkedHashMap<>(state.rpg().equipment());java.util.List<String> equipped=new java.util.ArrayList<>(before.values());for(String id:equipped)state.rpg().equip(id);capture(v,"equipment-empty");for(int pass=0;pass<3;pass++)for(String id:equipped)if(!state.rpg().equipment().containsValue(id))state.rpg().equip(id);assertEquals(before,state.rpg().equipment());tap(v,640,92);assertTrue((Boolean)ItemWindowReferenceTest.field(v,"statsOpen"));capture(v,"stats");tap(v,911,91);
    tap(v,874,28);SkillWindow sw=ItemWindowReferenceTest.field(v,"skillWindow");assertTrue(sw.open);sw.showJob("전사");sw.selectedId="SK_전사_012";capture(v,"skills");sw.detailPage=1;capture(v,"skills-conditions");tap(v,610,61);
    QuestJournalTest.gesture(v,918,28);assertTrue((Boolean)ItemWindowReferenceTest.field(v,"questJournalOpen"));capture(v,"quests");QuestJournalTest.gesture(v,818,78);
    assertEquals(gold,state.rpg().gold().longValue());assertEquals(x,state.player().x,0);assertEquals(y,state.player().y,0);
  }
  @Test public void wideWindowsAndTheirCloseTargetsStayAligned()throws Exception{
    GameView v=start(2340,1080);float right=GameView.rightHudOffsetForView(2340,1080),center=right/2;
    tap(v,(742+right)*2,56);capture(v,"wide-inventory");tap(v,(914+center)*2,120);assertFalse((Boolean)ItemWindowReferenceTest.field(v,"inventoryOpen"));
    tap(v,(830+right)*2,56);capture(v,"wide-equipment");tap(v,(492+center)*2,122);assertFalse((Boolean)ItemWindowReferenceTest.field(v,"equipmentOpen"));
    tap(v,(786+right)*2,56);capture(v,"wide-stats");tap(v,(911+center)*2,182);assertFalse((Boolean)ItemWindowReferenceTest.field(v,"statsOpen"));
    tap(v,(874+right)*2,56);capture(v,"wide-skills");tap(v,(610+center)*2,122);assertFalse(((SkillWindow)ItemWindowReferenceTest.field(v,"skillWindow")).open);
    QuestJournalTest.gesture(v,(918+right)*2,56);capture(v,"wide-quests");QuestJournalTest.gesture(v,(818+center)*2,156);assertFalse((Boolean)ItemWindowReferenceTest.field(v,"questJournalOpen"));
  }
  @Test public void packagedKoreanFontsAndBodyTextContrastAreReadable()throws Exception{
    UiTheme.install(c);assertNotSame(UiTheme.font(false),UiTheme.font(true));Paint p=new Paint();p.setTypeface(UiTheme.font(false));p.setTextSize(14);assertTrue(p.hasGlyph("퀘"));assertTrue(p.hasGlyph("장"));assertTrue(contrast(UiTheme.TEXT,UiTheme.SURFACE)>7);assertTrue(contrast(UiTheme.MUTED,UiTheme.SURFACE)>4.5);assertTrue(contrast(UiTheme.GOLD,UiTheme.SURFACE)>4.5);
  }
  double luminance(int color){double n=0;double[] coeff={.2126,.7152,.0722};int[] rgb={Color.red(color),Color.green(color),Color.blue(color)};for(int i=0;i<3;i++){double x=rgb[i]/255.;n+=coeff[i]*(x<=.04045?x/12.92:Math.pow((x+.055)/1.055,2.4));}return n;}
  double contrast(int fg,int bg){return (luminance(fg)+.05)/(luminance(bg)+.05);}
}
