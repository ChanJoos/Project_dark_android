package com.projectdark.mobile;

import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class PoteMonsterMotionV121Test {
  static final String[] IDS={"POTE_RED","POTE_GREEN","POTE_PURPLE","POTE_SILVER","POTE_LYCAN",
      "POTE_TREANT","POTE_ANTLION","POTE_GNOLL","POTE_WOLFRIDER","POTE_ANTGIANT",
      "POTE_SILVERWOLF","POTE_MANTIS","POTE_SPIRIT#0","POTE_SPIRIT#1"};
  static void save(Bitmap image,String name)throws Exception{
    File f=new File("build/reports/monster-motion-v121/"+name+".png");f.getParentFile().mkdirs();
    try(FileOutputStream out=new FileOutputStream(f)){assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));}
  }
  @Test public void allFamiliesHaveContinuousFourFacingGaitAndContactRecovery()throws Exception{
    PoteFieldRenderer renderer=new PoteFieldRenderer();
    for(String id:IDS){
      Bitmap sheet=Bitmap.createBitmap(8*128,4*128,Bitmap.Config.ARGB_8888);Canvas atlas=new Canvas(sheet);
      for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
        Set<Integer> hashes=new HashSet<>();
        for(int i=0;i<16;i++){
          Bitmap cell=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
          renderer.drawMonsterTestPose(new Canvas(cell),id,"walk",d,0,i*CharacterRenderer.WALK_CYCLE_SECONDS/16f,64,112);
          int[] pixels=new int[128*128];cell.getPixels(pixels,0,128,0,0,128,128);hashes.add(Arrays.hashCode(pixels));
          if(i%2==0)atlas.drawBitmap(cell,(i/2)*128,d.ordinal()*128,null);cell.recycle();
        }
        assertTrue(id+" "+d+" articulated changing frames",hashes.size()>=8);
      }
      save(sheet,id.replace('#','-')+"-walk");sheet.recycle();
    }
    Bitmap attack=Bitmap.createBitmap(8*128,4*128,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(attack);
    for(int row=0;row<4;row++)for(int i=0;i<8;i++){
      canvas.save();canvas.translate(i*128,row*128);
      renderer.drawMonsterTestPose(canvas,IDS[4+row*2],"attack",CharacterRenderer.Direction.SE,i/7f,0,64,112);
      canvas.restore();
    }
    save(attack,"attack-windup-contact-recovery");
  }
  @Test public void legacyHitStartsRecoveryAtActualDamageAndKeepsLockedFacing(){
    RuntimeState state=new RuntimeState();RuntimeState.Monster m=state.monsters().get(0);
    state.player().x=m.x+32;state.player().y=m.y+16;state.beginMonsterAttack(m);
    state.tick(.12f);assertTrue(m.attackPrimed);assertEquals(.36f,m.attackVisualRemaining,.001f);
    state.tick(.12f);int hp=state.player().hp;state.resolveMonsterAttack(m,4,1.8f);
    assertEquals(hp-4,state.player().hp);assertEquals(.5f,PoteForestMonsterShowcase.attackProgress(m),.001f);
    assertTrue(m.visualFacing.attackLocked());state.resolveMonsterAttack(m,4,1.8f);assertEquals(hp-4,state.player().hp);
    state.tick(.18f);assertEquals(.75f,PoteForestMonsterShowcase.attackProgress(m),.001f);
    state.tick(.19f);assertFalse(m.visualFacing.attackLocked());assertEquals(RuntimeState.Monster.State.IDLE,m.state);
  }
  @Test public void corpseDoesNotMoveOrRemainSelectableAndDefeatStillOccursOnce(){
    RuntimeState state=new RuntimeState();state.enterCampaignMap("MAP_POTE_03",false);
    RuntimeState.Monster m=state.monsters().get(0);m.isMoving=true;m.moveStartX=m.x;m.moveStartY=m.y;
    m.moveTargetX=m.x+32;m.moveTargetY=m.y+16;m.moveElapsed=0;m.moveDuration=.6f;
    float x=m.x,y=m.y;state.damage(m,m.hp);m.respawnClock=Float.POSITIVE_INFINITY;
    assertFalse(m.isMoving);assertTrue(PoteMonsterMotion.visible(m));assertNull(state.hitMonster(x,y,1));
    state.damage(m,99);state.tick(.3f);assertEquals(x,m.x,0);assertEquals(y,m.y,0);
    assertTrue(PoteMonsterMotion.visible(m));state.tick(.31f);assertFalse(PoteMonsterMotion.visible(m));
    assertFalse(m.alive);assertEquals(Float.POSITIVE_INFINITY,m.respawnClock,0);
    long defeats=state.ledger().snapshot().stream().filter(e->e.type==CombatLedger.Type.MONSTER_DEFEATED&&e.targetId.equals(m.id)).count();
    assertEquals(1,defeats);
  }
  @Test public void deathAndHitRenderAndKeepTheCurrentMapTextureBudget()throws Exception{
    RuntimeState state=new RuntimeState();state.enterCampaignMap("MAP_POTE_03",false);
    PoteFieldRenderer renderer=new PoteFieldRenderer();renderer.prepareMonsters(state);
    Field source=PoteFieldRenderer.class.getDeclaredField("cache"),hits=PoteFieldRenderer.class.getDeclaredField("hitCache");source.setAccessible(true);hits.setAccessible(true);
    int before=((Map<?,?>)source.get(renderer)).size(),hitBefore=((Map<?,?>)hits.get(renderer)).size();
    RuntimeState.Monster m=state.monsters().stream().filter(a->a.id.startsWith("POTE_WOLFRIDER")).findFirst().get();
    Bitmap sheet=Bitmap.createBitmap(8*128,2*128,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);
    state.damage(m,1);
    for(int i=0;i<8;i++){m.hitFlash=.14f*(1f-i/8f);canvas.save();canvas.translate(i*128,0);renderer.drawMonster(canvas,m,64,112);canvas.restore();}
    state.damage(m,m.hp);
    for(int i=0;i<8;i++){m.deathVisualRemaining=PoteMonsterMotion.DEATH_SECONDS*(1f-i/7f);canvas.save();canvas.translate(i*128,128);renderer.drawMonster(canvas,m,64,112);canvas.restore();}
    assertEquals(before,((Map<?,?>)source.get(renderer)).size());assertEquals(hitBefore,((Map<?,?>)hits.get(renderer)).size());
    save(sheet,"hit-and-death");
  }
  @Test public void liveForestGameViewActuallyDrawsCorpseBeforeItDisappears()throws Exception{
    GameView view=new GameView(RuntimeEnvironment.getApplication());view.layout(0,0,1536,864);
    Method enter=GameView.class.getDeclaredMethod("enterPoteField");enter.setAccessible(true);enter.invoke(view);
    RuntimeState state=TownInteriorTest.field(view,"state");RuntimeState.Monster m=state.monsters().get(0);
    WorldRuntimeAdapter world=TownInteriorTest.field(view,"poteFieldAdapter");state.player().x=m.x-32;state.player().y=m.y-16;world.snapCameraToPlayer();
    state.damage(m,m.hp);Bitmap corpse=Bitmap.createBitmap(1536,864,Bitmap.Config.ARGB_8888);view.draw(new Canvas(corpse));save(corpse,"live-corpse");
    state.tick(.61f);Bitmap gone=Bitmap.createBitmap(1536,864,Bitmap.Config.ARGB_8888);view.draw(new Canvas(gone));save(gone,"live-corpse-finished");
    assertFalse(corpse.sameAs(gone));assertFalse(m.alive);
  }
}
