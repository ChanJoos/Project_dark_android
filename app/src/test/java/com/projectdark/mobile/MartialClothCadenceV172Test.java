package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MartialClothCadenceV172Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();UiTheme.install(c);}
 @Test public void jeonggwonUsesExactlyTongbaePixelsOnEveryIconSurface(){
  SkillIconCatalog icons=new SkillIconCatalog(c);assertTrue(icons.has("SK_무도가_001"));
  for(int surface=0;surface<5;surface++){
   Bitmap a=Bitmap.createBitmap(64,64,Bitmap.Config.ARGB_8888),b=Bitmap.createBitmap(64,64,Bitmap.Config.ARGB_8888);RectF rect=new RectF(3,3,61,61);
   for(int n=0;n<2;n++){Canvas canvas=new Canvas(n==0?a:b);String id=n==0?"SK_무도가_001":"SK_무도가_006";boolean ok=surface==0?icons.drawSquare(canvas,id,rect):surface==1?icons.drawQuickslot(canvas,id,rect):surface==2?icons.draw(canvas,id,rect):surface==3?icons.windowSkin().draw(canvas,id,rect):icons.windowSkin().drawContent(canvas,id,rect);if(surface<3)assertTrue(ok);}
   assertTrue("surface "+surface,a.sameAs(b));if(surface<3)assertTrue(opaque(a)>100);a.recycle();b.recycle();
  }
 }
 @Test public void everyBasicModeSharesOneSecondEvenWhenChangingWeaponMode(){
  for(int mode=0;mode<4;mode++){
   RuntimeState s=new RuntimeState();s.player().x=100;s.player().y=100;RuntimeState.Monster m=s.monsters().get(0);m.x=132;m.y=116;m.hp=100000;m.alive=true;
   RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);
   assertTrue(session.submitPlayerAttack(m.id,mode).accepted());session.tick(.6f);
   for(int next=0;next<4;next++)assertFalse("mode switch bypass",session.submitPlayerAttack(m.id,next).accepted());
   session.tick(.399f);assertFalse(session.submitPlayerAttack(m.id,mode).accepted());session.tick(.002f);assertTrue(session.submitPlayerAttack(m.id,(mode+1)%4).accepted());
   CombatController controller=new CombatController();controller.setBasicAttack(AttackDef.at(mode).kind==AttackDef.Kind.PUNCH?AnimationAction.PUNCH:AnimationAction.valueOf(AttackDef.at(mode).kind.name()));controller.commitAttack();controller.tick(.999f);assertFalse(controller.attackReady());controller.tick(.002f);assertTrue(controller.attackReady());
  }
 }
 int opaque(Bitmap b){int n=0;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(Color.alpha(b.getPixel(x,y))>0)n++;return n;}
 Bitmap frame(SkillBodyRenderer r,String body,String motion,CharacterRenderer.Direction d,float q,String gear){Bitmap b=Bitmap.createBitmap(150,160,Bitmap.Config.ARGB_8888);CharacterRenderer.Pose p=new CharacterRenderer.Pose(75,145,d,CharacterRenderer.State.ATTACK,0,q,.5f,false,gear,null,null,CharacterRenderer.EffectFamily.PUNCH,AnimationAction.PUNCH);assertTrue(r.draw(new Canvas(b),p,body,motion,q));return b;}
 float clothY(Bitmap dressed,Bitmap bare){float sum=0;int count=0;for(int y=0;y<160;y++)for(int x=0;x<150;x++)if(dressed.getPixel(x,y)!=bare.getPixel(x,y)&&Color.alpha(dressed.getPixel(x,y))>0){sum+=y;count++;}assertTrue("visible clothing",count>15);return sum/count;}
 @Test public void punchClothesFollowCrouchingTorsoForBothBodiesAndFourDirections()throws Exception{
  SkillPresentationCatalog cat=new SkillPresentationCatalog(c);SkillBodyRenderer r=new SkillBodyRenderer(c,cat);
  Bitmap sheet=Bitmap.createBitmap(900,1280,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(sheet);out.drawColor(0xff999080);Paint text=new Paint();text.setColor(Color.BLACK);text.setTextSize(10);int row=0;
  for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   float[] center=new float[2];int i=0;
   for(float q:new float[]{0,.5f}){Bitmap a=frame(r,body,"PUNCH",d,q,"mu0000001"),b=frame(r,body,"PUNCH",d,q,"");center[i]=clothY(a,b);out.drawBitmap(a,i*150,row*160,null);out.drawBitmap(b,(i+2)*150,row*160,null);i++;a.recycle();b.recycle();}
   assertTrue(body+d+" crouch garment follows body",center[1]>center[0]+2);
   Bitmap equipped=frame(r,body,"PUNCH",d,.5f,"mu0000001,ml228,mh172");out.drawBitmap(equipped,600,row*160,null);equipped.recycle();out.drawText(body+" "+d,750,row*160+30,text);row++;
  }
  save(sheet,"punch-body-cloth-four-directions");sheet.recycle();
  Bitmap target=Bitmap.createBitmap(150,160,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(target);
  for(String body:new String[]{"mm001","wm001"})for(String motion:new String[]{"PUNCH","TRIPLE_PUNCH","FRONT_KICK","SIDE_KICK","SPIN_KICK","JUMP_KICK","MARTIAL_CAST","RAISE","JUMP"}){
   Bitmap strip=Bitmap.createBitmap(900,640,Bitmap.Config.ARGB_8888);Canvas sc=new Canvas(strip);sc.drawColor(0xff999080);int row2=0;
   for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){int col=0;for(float q:new float[]{0,.17f,.34f,.55f,.7f,.85f}){Bitmap b=frame(r,body,motion,d,q,"mu0000001,ml228,mh172");sc.drawBitmap(b,col++*150,row2*160,null);b.recycle();}row2++;}save(strip,body+"-"+motion);strip.recycle();
  }
  int builds=r.attachmentBuildCount(),rigs=r.rigBuildCount();for(int n=0;n<100;n++){CharacterRenderer.Pose p=new CharacterRenderer.Pose(75,145,CharacterRenderer.Direction.SE,CharacterRenderer.State.ATTACK,0,.5f,.5f,false,"mu0000001,ml228,mh172",null,null,CharacterRenderer.EffectFamily.PUNCH);r.draw(canvas,p,"mm001","PUNCH",.5f);}assertEquals(builds,r.attachmentBuildCount());assertEquals(rigs,r.rigBuildCount());target.recycle();
 }
 void save(Bitmap b,String name)throws Exception{File f=new File("build/reports/martial-cloth-v172/"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
}
