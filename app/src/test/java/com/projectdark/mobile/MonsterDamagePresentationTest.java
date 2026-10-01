package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MonsterDamagePresentationTest {
  Context context;
  @Before public void setup(){context=RuntimeEnvironment.getApplication();resetSave();}
  private void resetSave(){context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}

  @Test public void allSixtyPosesKeepTheirAlphaAndFootprintDuringAndAfterAHit()throws Exception{
    PoteFieldRenderer renderer=new PoteFieldRenderer();
    for(String pose:new String[]{"idle","walk","attack"}){
      Bitmap grid=Bitmap.createBitmap(800,600,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(grid);canvas.drawColor(0xff253129);
      Paint text=new Paint();text.setColor(Color.WHITE);text.setTextSize(10);int row=0;
      for(String id:PoteForestMonsterShowcase.monsterIds()){
        for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
          Bitmap normal=pose(renderer,id,pose,direction,false),hit=pose(renderer,id,pose,direction,true),restored=pose(renderer,id,pose,direction,false);
          int colored=0,changed=0;
          for(int y=0;y<normal.getHeight();y++)for(int x=0;x<normal.getWidth();x++){
            int a=normal.getPixel(x,y),b=hit.getPixel(x,y);
            assertEquals(id+" "+pose+" "+direction+" alpha",Color.alpha(a),Color.alpha(b));
            assertEquals("hit paint cannot leak to the next actor",a,restored.getPixel(x,y));
            if(Color.alpha(a)>0)colored++;if(a!=b)changed++;
          }
          assertTrue(id+" source sprite must load",colored>30);assertTrue(id+" visible hit tint",changed>10);
          int x=direction.ordinal()*200;canvas.drawBitmap(normal,x,row*120,null);canvas.drawBitmap(hit,x+100,row*120,null);
          canvas.drawText(id.replace("POTE_","")+" "+direction+" normal / hit",x+2,row*120+112,text);
        }row++;
      }save(grid,"pote-damage-poses-"+pose+".png");
    }
  }

  @Test public void everyJobDamagesEverySpeciesThroughGameInputAndDrivesTheLiveRenderer()throws Exception{
    String[] skills={"SK_전사_001","SK_도적_007","SK_무도가_002","SK_마법사_005","SK_성직자_013","SK_공통_001"};
    for(String skill:skills){
      GameView view=scene();RuntimeState state=field(view,"state");SkillBook book=field(view,"skillBook");WorldRuntimeAdapter world=field(view,"poteFieldAdapter");
      for(RuntimeState.Monster monster:state.monsters()){
        invoke(view,"tickSkillCombat",new Class<?>[]{float.class},120f);idle(view);state.tick(.2f);
        boolean placed=false;
        for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values()){
          float x=monster.x+d.dx,y=monster.y+d.dy;
          if(state.isMonsterTileCenter(x,y)&&world.canPlayerOccupy(x,y)){state.player().x=x;state.player().y=y;placed=true;break;}
        }
        assertTrue(monster.id+" adjacent legal fixture tile",placed);world.snapCameraToPlayer();((CombatController)field(view,"combat")).selectTarget(monster);
        int hp=monster.hp;assertEquals(0,monster.hitFlash,0);
        invoke(view,"useBookSkill",new Class<?>[]{SkillBook.Entry.class},book.get(skill));
        assertEquals("contact has not happened",hp,monster.hp);assertEquals(0,monster.hitFlash,0);
        float contact="SK_공통_001".equals(skill)?.2f:SkillActionContract.get(skill).contact+.001f;
        invoke(view,"tickSkillCombat",new Class<?>[]{float.class},contact);
        assertTrue(skill+" "+monster.id+" real damage",monster.hp<hp);assertTrue("keep this fixture alive to inspect the hit frame",monster.alive);assertTrue(monster.hitFlash>0);
        SkillVfxRenderer fx=field(view,"skillVfx");assertEquals("one recipient impact",1,fx.pulses.stream().filter(p->p.sheet.equals("impact")&&p.anchor.equals(monster.id)).count());
        Bitmap hit=liveMonster(view,monster);float flash=monster.hitFlash;monster.hitFlash=0;Bitmap normal=liveMonster(view,monster);monster.hitFlash=flash;
        assertFalse(skill+" "+monster.id+" live renderer must consume hitFlash",hit.sameAs(normal));
        if(skill.equals(skills[0])||monster.id.equals("POTE_PURPLE")){
          Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(image));save(image,"pote-live-hit-"+skill+"-"+monster.id+".png");
        }
        float phase=monster.animationClock,popup=monster.damagePopupClock;state.tick(.15f);assertEquals("existing hit timer expires",0,monster.hitFlash,0);monster.animationClock=phase;monster.damagePopupClock=popup;assertTrue("normal color returns at the same pose",normal.sameAs(liveMonster(view,monster)));
      }
    }
  }

  @Test public void healingAndZeroDamagePresentationNeverTintMonsters()throws Exception{
    GameView view=scene();RuntimeState state=field(view,"state");SkillBook book=field(view,"skillBook");state.player().hp=5;
    invoke(view,"useBookSkill",new Class<?>[]{SkillBook.Entry.class},book.get("SK_성직자_005"));invoke(view,"tickSkillCombat",new Class<?>[]{float.class},.3f);assertTrue(state.player().hp>5);
    for(RuntimeState.Monster m:state.monsters())assertEquals(0,m.hitFlash,0);
    assertFalse(((SkillVfxRenderer)field(view,"skillVfx")).pulses.stream().anyMatch(p->p.sheet.equals("impact")));
    invoke(view,"tickSkillCombat",new Class<?>[]{float.class},120f);idle(view);RuntimeState.Monster m=state.monsters().get(0);state.player().x=m.x-32;state.player().y=m.y-16;((WorldRuntimeAdapter)field(view,"poteFieldAdapter")).snapCameraToPlayer();((CombatController)field(view,"combat")).selectTarget(m);
    int hp=m.hp;invoke(view,"useBookSkill",new Class<?>[]{SkillBook.Entry.class},book.get("SK_도적_002"));assertEquals("SK_도적_002",field(view,"activeSkillVisualId"));invoke(view,"tickSkillCombat",new Class<?>[]{float.class},.3f);assertEquals(hp,m.hp);assertEquals(0,m.hitFlash,0);
    assertFalse(((SkillVfxRenderer)field(view,"skillVfx")).pulses.stream().anyMatch(p->p.sheet.equals("impact")));
  }

  private GameView scene()throws Exception{
    resetSave();GameView view=new GameView(context);view.layout(0,0,960,540);invoke(view,"enterPoteField",new Class<?>[0]);view.setSkillTestMode(true);RuntimeState s=field(view,"state");
    // Nonlethal test HP isolates hit rendering from the independently tested death/reward path.
    List<RuntimeState.Monster> actors=field(s,"monsters");for(int i=0;i<actors.size();i++){RuntimeState.Monster m=actors.get(i);actors.set(i,new RuntimeState.Monster(m.id,m.name,m.x,m.y,200,"B/NATIVE_HIT_FIXTURE"));}return view;
  }
  private static Bitmap pose(PoteFieldRenderer renderer,String id,String pose,CharacterRenderer.Direction direction,boolean hit){Bitmap image=Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);renderer.drawMonsterTestPose(new Canvas(image),id,pose,direction,.4f,.2f,50,85,hit);return image;}
  private static Bitmap liveMonster(GameView view,RuntimeState.Monster monster)throws Exception{Bitmap image=Bitmap.createBitmap(120,110,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);canvas.translate(60-monster.x,90-monster.y);invoke(view,"drawMonsters",new Class<?>[]{Canvas.class},canvas);return image;}
  private static void invoke(Object target,String name,Class<?>[] types,Object... args)throws Exception{Method method=target.getClass().getDeclaredMethod(name,types);method.setAccessible(true);method.invoke(target,args);}
  @SuppressWarnings({"unchecked","rawtypes"}) private static void idle(GameView view)throws Exception{Field f=GameView.class.getDeclaredField("action");f.setAccessible(true);f.set(view,Enum.valueOf((Class)f.getType(),"IDLE"));}
  @SuppressWarnings("unchecked") private static <T>T field(Object target,String name)throws Exception{Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return(T)f.get(target);}
  private static void save(Bitmap image,String name)throws Exception{File f=new File("build/reports/device-review/"+name);f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){image.compress(Bitmap.CompressFormat.PNG,100,out);}}
}
