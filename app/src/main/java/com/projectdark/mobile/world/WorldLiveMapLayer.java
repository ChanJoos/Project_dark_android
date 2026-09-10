package com.projectdark.mobile.world;

import android.graphics.Canvas;
import com.projectdark.mobile.RuntimeState;

public final class WorldLiveMapLayer {
  private final WorldRuntimeAdapter world;
  private final AdaptedMillesMapRenderer renderer=new AdaptedMillesMapRenderer();
  public WorldLiveMapLayer(RuntimeState runtime,float width,float height){world=new WorldRuntimeAdapter(runtime,width,height);}
  public WorldRuntimeAdapter world(){return world;}
  public void draw(Canvas canvas){renderer.draw(canvas,world);}
  public void followPlayer(){world.camera().follow(world.runtime().player().x,world.runtime().player().y);}
  public void snapToPlayer(){world.camera().snapTo(world.runtime().player().x,world.runtime().player().y);}
  public WorldCameraTransform.Point screenToWorld(float x,float y){return world.screenToWorld(x,y);}
}
