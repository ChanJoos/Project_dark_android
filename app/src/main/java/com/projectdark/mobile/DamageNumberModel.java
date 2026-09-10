package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** Combat-owned presentation model. Renderer/UI consumes snapshots; this class draws nothing. */
public final class DamageNumberModel {
  public enum SemanticType { DAMAGE, CRIT, MISS, HEAL }

  public static final class Snapshot {
    public final long id;
    public final String targetId;
    public final SemanticType type;
    public final int amount;
    public final float anchorX,anchorY,offsetY,alpha,progress;
    Snapshot(long id,String targetId,SemanticType type,int amount,float anchorX,float anchorY,float offsetY,float alpha,float progress){
      this.id=id;this.targetId=targetId;this.type=type;this.amount=amount;this.anchorX=anchorX;this.anchorY=anchorY;
      this.offsetY=offsetY;this.alpha=alpha;this.progress=progress;
    }
    public String displayText(){
      switch(type){case MISS:return "MISS";case HEAL:return "+"+amount;default:return "-"+amount;}
    }
  }

  private static final class Entry {
    final long id;final String targetId;final SemanticType type;final int amount;final float x,y,duration,rise;
    float age;
    Entry(long id,String targetId,SemanticType type,int amount,float x,float y,float duration,float rise){
      this.id=id;this.targetId=targetId;this.type=type;this.amount=amount;this.x=x;this.y=y;this.duration=duration;this.rise=rise;
    }
  }

  public static final float DEFAULT_DURATION=.72f;
  public static final float DEFAULT_RISE=22f;
  private final List<Entry> active=new ArrayList<>();
  private long sequence=0;

  public long spawn(String targetId,SemanticType type,int amount,float anchorX,float anchorY){
    return spawn(targetId,type,amount,anchorX,anchorY,DEFAULT_DURATION,DEFAULT_RISE);
  }

  public long spawn(String targetId,SemanticType type,int amount,float anchorX,float anchorY,float duration,float rise){
    if(targetId==null||type==null||duration<=0f||rise<0f)throw new IllegalArgumentException("damage number");
    if(type==SemanticType.MISS)amount=0;else if(amount<0)throw new IllegalArgumentException("amount");
    long id=++sequence;active.add(new Entry(id,targetId,type,amount,anchorX,anchorY,duration,rise));return id;
  }

  /** spawn -> upward move/fade -> despawn. */
  public void tick(float dt){
    if(dt<=0f)return;
    for(Iterator<Entry> it=active.iterator();it.hasNext();){Entry e=it.next();e.age+=dt;if(e.age>=e.duration)it.remove();}
  }

  public List<Snapshot> snapshot(){
    List<Snapshot> out=new ArrayList<>();
    for(Entry e:active){
      float q=Math.max(0f,Math.min(1f,e.age/e.duration));
      float fade=q<.45f?1f:Math.max(0f,1f-(q-.45f)/.55f);
      out.add(new Snapshot(e.id,e.targetId,e.type,e.amount,e.x,e.y,-e.rise*q,fade,q));
    }
    return Collections.unmodifiableList(out);
  }

  public int size(){return active.size();}
  public void clear(){active.clear();}
}
