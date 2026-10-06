package com.projectdark.mobile;
import android.content.Context;
import android.graphics.Canvas;
/** Every human NPC uses the same source body, equipment registration and scale as the player. */
public final class NpcActorRenderer {
 private final CharacterRenderer character;
 public NpcActorRenderer(){character=new CharacterRenderer();}
 public NpcActorRenderer(Context context){character=new CharacterRenderer(context);}
 public void draw(Canvas canvas,String id,float x,float y,CharacterRenderer.Direction direction,CharacterRenderer.State state,float clock){
  NpcIdentity.Profile p=NpcIdentity.forId(id);
  character.draw(canvas,new CharacterRenderer.Pose(x,y,CharacterRenderer.Direction.SW,CharacterRenderer.State.IDLE,0,0,1,false,p.outfit,p.weapon,CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));
 }
}
