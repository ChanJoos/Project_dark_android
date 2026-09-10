package com.projectdark.mobile;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.projectdark.mobile.ui.GameUiController;
import com.projectdark.mobile.ui.TouchOwnership;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/** Runs inside a real Android process. Synthetic inputs/loot are QA fixtures, not canonical rewards. */
public final class RuntimeSmokeInstrumentation extends Instrumentation {
  private GameView game;
  private RuntimeState state;
  private int checks;
  private File evidenceDir;

  @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); start(); }
  private static Object field(Object object,String name) throws Exception {
    Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);
  }
  private void check(boolean condition,String message) {
    if(!condition)throw new AssertionError(message);checks++;
  }
  private void main(CheckedRunnable action) throws Exception {
    final Throwable[] error={null};
    runOnMainSync(()->{try{action.run();}catch(Throwable t){error[0]=t;}});
    if(error[0]!=null)throw new Exception(error[0]);
  }
  private interface CheckedRunnable { void run() throws Exception; }
  private void step(int frames) throws Exception {
    Method m=GameView.class.getDeclaredMethod("update",float.class);m.setAccessible(true);
    for(int i=0;i<frames;i++)m.invoke(game,.05f);
  }
  private void event(int action,int[] ids,float[] xs,float[] ys) throws Exception {
    float scale=(Float)field(game,"scale"),ox=(Float)field(game,"ox"),oy=(Float)field(game,"oy");
    MotionEvent.PointerProperties[] properties=new MotionEvent.PointerProperties[ids.length];
    MotionEvent.PointerCoords[] coords=new MotionEvent.PointerCoords[ids.length];
    for(int i=0;i<ids.length;i++){
      properties[i]=new MotionEvent.PointerProperties();properties[i].id=ids[i];properties[i].toolType=MotionEvent.TOOL_TYPE_FINGER;
      coords[i]=new MotionEvent.PointerCoords();coords[i].x=ox+xs[i]*scale;coords[i].y=oy+ys[i]*scale;coords[i].pressure=1;coords[i].size=1;
    }
    long now=SystemClock.uptimeMillis();
    MotionEvent e=MotionEvent.obtain(now,now,action,ids.length,properties,coords,0,0,1,1,0,0,android.view.InputDevice.SOURCE_TOUCHSCREEN,0);
    game.onTouchEvent(e);e.recycle();
  }
  private void tap(float x,float y) throws Exception {
    event(MotionEvent.ACTION_DOWN,new int[]{7},new float[]{x},new float[]{y});
    event(MotionEvent.ACTION_UP,new int[]{7},new float[]{x},new float[]{y});
  }
  private void screenshot(String name) throws Exception {
    main(()->game.invalidate());waitForIdleSync();SystemClock.sleep(200);
    Bitmap bitmap=getUiAutomation().takeScreenshot();check(bitmap!=null,"Android compositor screenshot");
    try(FileOutputStream out=new FileOutputStream(new File(evidenceDir,name))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}
    bitmap.recycle();
  }
  @Override public void onStart(){
    Bundle result=new Bundle();
    try{
      Intent intent=new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      Activity activity=startActivitySync(intent);waitForIdleSync();SystemClock.sleep(300);
      evidenceDir=new File(getTargetContext().getExternalFilesDir(null),"runtime-qa");
      if(!evidenceDir.exists()&&!evidenceDir.mkdirs())throw new IllegalStateException("Evidence directory");
      main(()->{
        game=(GameView)((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0);game.pause();
        state=(RuntimeState)field(game,"state");
        check(game.getWidth()>game.getHeight(),"landscape runtime");
        float before=state.player().x;
        event(MotionEvent.ACTION_DOWN,new int[]{17},new float[]{130},new float[]{444});step(3);
        check(state.player().x>before,"actual View touch advances player");
        event(MotionEvent.ACTION_POINTER_DOWN|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),new int[]{17,4},new float[]{130,746},new float[]{444,466});
        event(MotionEvent.ACTION_POINTER_UP|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),new int[]{17,4},new float[]{130,746},new float[]{444,466});
        check(((TouchOwnership)field(game,"touchOwnership")).ownsMovement(17),"action finger release preserves joystick");
        event(MotionEvent.ACTION_CANCEL,new int[]{17},new float[]{130},new float[]{444});
        before=state.player().x;step(3);check(state.player().x==before,"CANCEL stops movement");
        event(MotionEvent.ACTION_DOWN,new int[]{2},new float[]{728},new float[]{60});
        event(MotionEvent.ACTION_MOVE,new int[]{2},new float[]{130},new float[]{444});step(3);
        check(state.player().x==before,"HUD finger cannot acquire world movement");
        event(MotionEvent.ACTION_UP,new int[]{2},new float[]{130},new float[]{444});
        tap(928,90);check(((GameUiController)field(game,"ui")).modal(),"bag opens");
        before=state.player().x;tap(130,444);step(2);check(state.player().x==before,"modal captures world controls");
        check(game.handleBack(),"back closes top modal");check(!((GameUiController)field(game,"ui")).modal(),"bag closed");
        RuntimeState.Npc npc=state.npcs().get(0);state.player().x=npc.x-40;state.player().y=npc.y;
        tap(npc.x,npc.y-24);check(((InteractionController)field(game,"interaction")).dialogOpen(),"NPC opens real dialogue");
      });
      screenshot("01-npc-dialogue.png");
      main(()->{
        game.handleBack();RuntimeState.Monster monster=state.monsters().get(0);
        state.player().x=monster.x+55;state.player().y=monster.y;
        tap(monster.x,monster.y-18);check(((CombatController)field(game,"combat")).target()==monster,"target from touch");
        for(int i=0;i<10&&monster.alive;i++){tap(895,478);step(12);}
        check(!monster.alive,"attack kills prototype monster");
        check(state.metrics().monsterDefeats()==1,"defeat event once");
        check(state.rpg().worldDrops().isEmpty(),"dummy gets no fabricated canonical reward");
        long id=state.rpg().createWorldDrop("IT_GLOVE_LEATHER","QA_FIXTURE",1,state.player().x,state.player().y,RpgProgressionState.Evidence.B);
        check(id>0,"test-only ground entity uses existing RPG API");
        tap(650,466);check(state.rpg().inventory().get("IT_GLOVE_LEATHER")==1,"touch pickup reaches inventory");
        tap(650,466);check(state.rpg().inventory().get("IT_GLOVE_LEATHER")==1,"second pickup cannot duplicate");
        check(state.rpg().worldDrops().isEmpty(),"ground entity consumed");
        tap(928,90);
      });
      screenshot("02-inventory-qa-fixture.png");
      main(()->{
        game.handleBack();RuntimeState.Monster monster=state.monsters().get(0);step(100);
        CombatController combat=(CombatController)field(game,"combat");combat.selectTarget(monster);
        state.damagePlayer(100000);int mp=state.player().mp,hp=monster.hp;
        for(String name:new String[]{"cast","skill","kick"}){Method m=GameView.class.getDeclaredMethod(name);m.setAccessible(true);m.invoke(game);}
        check(state.player().mp==mp&&monster.hp==hp,"dead player cannot cast or deal skill damage");
        check(combat.castCooldown()==0&&combat.skillCooldown()==0&&combat.kickCooldown()==0,"dead request consumes no cooldown");
        state.revivePlayer();game.invalidate();
      });
      screenshot("03-runtime-geometry-prototype.png");
      String report="{\"status\":\"PASS\",\"checks\":"+checks+",\"scope\":\"Android View input, NPC, prototype combat, QA fixture pickup/inventory; no canonical reward, creation, quest, EXP, persistence or final visual verification\"}";
      try(FileOutputStream out=new FileOutputStream(new File(evidenceDir,"report.json"))){out.write(report.getBytes(StandardCharsets.UTF_8));}
      result.putString("stream","PROJECT_DARK_RUNTIME_PASS "+report);finish(Activity.RESULT_OK,result);
    }catch(Throwable error){
      result.putString("stream","PROJECT_DARK_RUNTIME_FAIL "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);
    }
  }
}
