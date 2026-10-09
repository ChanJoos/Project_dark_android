package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ForestPixelCacheTest {
 static Object field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
 static float number(Object object,String name)throws Exception{return ((Number)field(object,name)).floatValue();}
 @Test public void maskKeepsEveryAlphaValueAndThresholdBoundary(){
  Bitmap bitmap=Bitmap.createBitmap(16,16,Bitmap.Config.ARGB_8888);int[] colors=new int[256];for(int i=0;i<256;i++)colors[i]=(i<<24)|0x102030;bitmap.setPixels(colors,0,16,0,0,16,16);
  SpriteAlphaMask mask=new SpriteAlphaMask(bitmap);for(int y=0;y<16;y++)for(int x=0;x<16;x++)assertEquals(bitmap.getPixel(x,y)>>>24,mask.at(x,y));assertEquals(0,mask.at(-1,0));assertEquals(0,mask.at(0,16));
 }
 private long legacyReads;
 private boolean legacy(Object p,Bitmap bitmap,List<PoteFieldRenderer.ActorDraw> actors)throws Exception{
  String role=(String)field(p,"role");if(!role.equals("canopy")&&!role.equals("secondary_canopy"))return false;
  float x=number(p,"x"),y=number(p,"y"),scale=number(p,"scale"),ww=bitmap.getWidth()*scale,hh=bitmap.getHeight()*scale;
  for(PoteFieldRenderer.ActorDraw a:actors){
   if(a.y>=y||Math.abs(a.x-x)>ww*.5f+a.width*.5f||a.y<y-hh||a.y-a.height>y)continue;
   for(float yy=a.y-a.height;yy<a.y;yy+=5)for(float xx=a.x-a.width*.4f;xx<=a.x+a.width*.4f;xx+=5){
    int bx=(int)((xx-(x-ww*.5f))/scale),by=(int)((yy-(y-hh))/scale);
    if(bx>=0&&bx<bitmap.getWidth()&&by>=0&&by<bitmap.getHeight()){legacyReads++;if((bitmap.getPixel(bx,by)>>>24)>64)return true;}
   }
  }return false;
 }
 @Test public void movingPlayerMonsterAndNpcKeepExactLegacyCanopyDecisionWithTwoReadbacks()throws Exception{
  PoteFieldRenderer renderer=new PoteFieldRenderer();Method placements=PoteFieldRenderer.class.getDeclaredMethod("placementsForMap",String.class),load=PoteFieldRenderer.class.getDeclaredMethod("bitmap",String.class);placements.setAccessible(true);load.setAccessible(true);
  Field actorsField=PoteFieldRenderer.class.getDeclaredField("visibleActors");actorsField.setAccessible(true);Set<String> checked=new HashSet<>();Random random=new Random(116);int matches=0,clear=0,queries=0;
  for(Object p:(List<?>)placements.invoke(null,"MAP_POTE_01")){
   if(!"canopy".equals(field(p,"role")))continue;String asset=(String)field(p,"asset");if(!checked.add(asset))continue;
   Bitmap bitmap=(Bitmap)load.invoke(renderer,asset);float x=number(p,"x"),y=number(p,"y"),scale=number(p,"scale"),ww=bitmap.getWidth()*scale,hh=bitmap.getHeight()*scale;
   Method overlap=PoteFieldRenderer.class.getDeclaredMethod("occludesActor",p.getClass(),Bitmap.class);overlap.setAccessible(true);
   for(int i=0;i<2500;i++){
    List<PoteFieldRenderer.ActorDraw> actors=new ArrayList<>();for(int a=0;a<3;a++)actors.add(new PoteFieldRenderer.ActorDraw(x+(random.nextFloat()-.5f)*ww*1.5f,y-hh*random.nextFloat()+12,a==0?52:a==1?38:58,a==0?30:24,()->{}));
    actorsField.set(renderer,actors);boolean expected=legacy(p,bitmap,actors);assertEquals(expected,overlap.invoke(renderer,p,bitmap));if(expected)matches++;else clear++;queries++;
   }
  }
  int masks=((Map<?,?>)field(renderer,"alphaMasks")).size();assertEquals(2,masks);assertTrue(matches>0&&clear>0);assertTrue(legacyReads>10000);
  System.out.println("V116_CANOPY_ORACLE_QUERIES="+queries+"; LEGACY_NATIVE_PIXEL_READS="+legacyReads+"; NEW_BULK_READBACKS="+masks+"; FRAME_PIXEL_READS=0");
 }
 @Test public void cachedBodyBoundsAndHandRigMatchIndependentSourceScansAndStopGrowing()throws Exception{
  CharacterRenderer renderer=new CharacterRenderer(RuntimeEnvironment.getApplication());Bitmap atlas=(Bitmap)field(renderer,"idleWalkAtlas");Method bounds=CharacterRenderer.class.getDeclaredMethod("alphaBoundsCell",Bitmap.class,int.class,int.class,int.class,int.class);bounds.setAccessible(true);
  Method rig=CharacterRenderer.class.getDeclaredMethod("cachedRig",Bitmap.class,Rect.class,CharacterRenderer.Direction.class);rig.setAccessible(true);
  for(int row=0;row<4;row++)for(int col=0;col<5;col++){
   int l=36,t=48,r=-1,b=-1;for(int y=0;y<48;y++)for(int x=0;x<36;x++)if((atlas.getPixel(col*36+x,row*48+y)>>>24)!=0){l=Math.min(l,x);r=Math.max(r,x);t=Math.min(t,y);b=Math.max(b,y);}
   Object cached=bounds.invoke(renderer,atlas,row,col,36,48);assertEquals(l,(int)number(cached,"left"));assertEquals(r,(int)number(cached,"right"));assertEquals(t,(int)number(cached,"top"));assertEquals(b,(int)number(cached,"bottom"));
   Rect cell=new Rect(col*36,row*48,col*36+36,row*48+48);for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
    CharacterSemanticRig.Anchors expected=CharacterSemanticRig.derive(atlas,cell,d),actual=(CharacterSemanticRig.Anchors)rig.invoke(renderer,atlas,cell,d);
    assertEquals(expected.dominantHand.x,actual.dominantHand.x,0);assertEquals(expected.dominantHand.y,actual.dominantHand.y,0);assertEquals(expected.foot.x,actual.foot.x,0);assertEquals(expected.foot.y,actual.foot.y,0);
   }
  }
  Bitmap output=Bitmap.createBitmap(160,160,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(output);
  for(int pass=0;pass<2;pass++){
   for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(CharacterRenderer.State state:new CharacterRenderer.State[]{CharacterRenderer.State.IDLE,CharacterRenderer.State.WALK,CharacterRenderer.State.ATTACK})for(int frame=0;frame<20;frame++)renderer.draw(canvas,new CharacterRenderer.Pose(80,140,d,state,frame*.12f,.5f,1,false,"mu0000001,ml228,mh172,ms001","mw001",CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));
   if(pass==0){int count=((Map<?,?>)field(renderer,"alphaMasks")).size();assertTrue(count<=5);}
  }
  int builds=((Map<?,?>)field(renderer,"alphaMasks")).size();assertTrue(builds<=5);assertEquals(20,((Map<?,?>)((Map<?,?>)field(renderer,"boundsCache")).get(atlas)).size());
  System.out.println("V116_PLAYER_ALPHA_BULK_READBACKS="+builds+"; BODY_CELL_SCANS_CACHED=20");
 }
}
