package com.projectdark.mobile;
import android.graphics.Canvas;
import com.projectdark.mobile.world.TownInteriorDef;
/** Shared player paper doll for all town service NPCs; no role recoloring. */
final class TownNpcRenderer {
 private final NpcActorRenderer actors;
 TownNpcRenderer(){actors=new NpcActorRenderer();}
 TownNpcRenderer(android.content.Context c){actors=new NpcActorRenderer(c);}
 void draw(Canvas c,TownInteriorDef d,float x,float y){draw(c,d,x,y,CharacterRenderer.Direction.SW);}
 void draw(Canvas c,TownInteriorDef d,float x,float y,CharacterRenderer.Direction direction){actors.draw(c,NpcIdentity.interiorKey(d.kind.name()),x,y,direction,CharacterRenderer.State.IDLE,0);}
}
