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
/** Covers actual runtime definitions, rather than a manually curated accessory subset. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FullItemArtV127Test {
 Context context;
 @Before public void before(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);UiTheme.install(context);}
 @Test public void everyRuntimeItemHasAnExplicitOriginalArtBindingAndSharedShopReagentPath()throws Exception{
  RpgProgressionState state=new RpgProgressionState();SourceItemIconRegistry icons=new SourceItemIconRegistry(context);
  JSONObject manifest=new JSONObject(new String(PresentationAssetBytes.read(context,"item-icons/manifest.json"),"UTF-8"));assertEquals(state.itemDefinitions().size(),manifest.getJSONObject("items").length());assertEquals(422,state.itemDefinitions().size());
  Bitmap sheet=Bitmap.createBitmap(1000,((state.itemDefinitions().size()+11)/12)*84,Bitmap.Config.ARGB_8888);sheet.eraseColor(UiTheme.BG);Canvas canvas=new Canvas(sheet);Paint pixel=new Paint();pixel.setFilterBitmap(false);int i=0;
  for(RpgProgressionState.ItemDefinition def:state.itemDefinitions().values()){
   JSONObject receipt=icons.receipt(def.itemId);assertNotNull(def.itemId,receipt);Bitmap b=icons.get(def);assertNotNull(def.itemId,b);assertSame(def.itemId,b,icons.get(def.itemId));assertTrue(def.itemId,b.getWidth()>1&&b.getHeight()>1);
   if(receipt.getString("identityMatch").contains("EQUIVALENT"))assertTrue(receipt.has("limitation"));
   float x=i%12*83,y=i/12*84;UiTheme.slot(canvas,new RectF(x+3,y+3,x+77,y+70),false,false);SourceItemIconRegistry.draw(canvas,b,new RectF(x+14,y+9,x+66,y+61),pixel);UiTheme.fit(canvas,RpgInventoryPresentation.displayName(def.name),x+3,y+81,76,9,UiTheme.TEXT,false);i++;
  }
  for(String id:new String[]{"IT_REAGENT_KOMADIUM","IT_REAGENT_DIBENOMUM","IT_REAGENT_CURANUM","IT_REAGENT_EXCURANUM","IT_REAGENT_CURUM","IT_REAGENT_HOLYWATER","IT_RECALL_MILLES"}){
   Bitmap a=icons.get(id),b=new ReagentItemVisualRegistry(context).get(id);assertNotNull(id,b);assertEquals(a.getWidth(),b.getWidth());assertEquals(a.getHeight(),b.getHeight());assertTrue(id,a.sameAs(b));
  }
  save(sheet,"all-registered-items");
 }
 @Test public void allInventoryPagesAndCommonWindowsRenderWithoutMutatingInventory()throws Exception{
  GameView view=new GameView(context);view.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(view,"state");Map<String,Integer> owned=new LinkedHashMap<>(state.rpg().inventory());ItemWindow window=ItemWindowReferenceTest.field(view,"itemWindow");
  ItemWindowReferenceTest.tap(view,608,28);for(int page=0;page<(state.rpg().inventory().size()+49)/50;page++){assertEquals(page,window.page);capture(view,"inventory-"+(page+1));if(page<(state.rpg().inventory().size()+49)/50-1)ItemWindowReferenceTest.tap(view,894,480);}
  ItemWindowReferenceTest.tap(view,916,66);ItemWindowReferenceTest.tap(view,716,28);capture(view,"equipment");ItemWindowReferenceTest.tap(view,492,66);ItemWindowReferenceTest.tap(view,662,28);capture(view,"stats");ItemWindowReferenceTest.tap(view,911,91);ItemWindowReferenceTest.tap(view,770,28);capture(view,"skills");assertEquals(owned,state.rpg().inventory());
 }
 void capture(GameView view,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(b));save(b,name);b.recycle();}
 void save(Bitmap b,String name)throws Exception{File directory=new File("build/reports/full-item-v127");directory.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(directory,name+".png"))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
}
