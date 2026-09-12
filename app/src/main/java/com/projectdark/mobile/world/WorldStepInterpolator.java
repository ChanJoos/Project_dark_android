package com.projectdark.mobile.world;

/** Presentation-only interpolation between authoritative tile centers. */
public final class WorldStepInterpolator {
  private final float duration;
  private float x,y,fromX,fromY,toX,toY,elapsed;
  private boolean active;

  public WorldStepInterpolator(float duration,float initialX,float initialY){
    if(duration<=0f)throw new IllegalArgumentException("duration must be positive");
    this.duration=duration;snap(initialX,initialY);
  }

  public void snap(float worldX,float worldY){x=fromX=toX=worldX;y=fromY=toY=worldY;elapsed=0f;active=false;}

  public void begin(float destinationX,float destinationY){
    fromX=x;fromY=y;toX=destinationX;toY=destinationY;elapsed=0f;
    active=Math.abs(toX-fromX)>.0001f||Math.abs(toY-fromY)>.0001f;
    if(!active){x=toX;y=toY;}
  }

  /** Advances the current step and returns any unconsumed time. */
  public float advance(float deltaSeconds){
    float remaining=Math.max(0f,deltaSeconds);
    if(!active)return remaining;
    float consumed=Math.min(remaining,duration-elapsed);elapsed+=consumed;remaining-=consumed;
    float t=Math.max(0f,Math.min(1f,elapsed/duration));
    // Linear world motion keeps velocity continuous across chained adjacent tiles.
    x=fromX+(toX-fromX)*t;y=fromY+(toY-fromY)*t;
    if(elapsed+.00001f>=duration){x=toX;y=toY;elapsed=duration;active=false;}
    return remaining;
  }

  public float x(){return x;}public float y(){return y;}
  public float progress(){return active?Math.max(0f,Math.min(1f,elapsed/duration)):1f;}
  public boolean active(){return active;}
}
