package com.projectdark.mobile;
import java.util.*;
/** Video-tracked source-style impact fan; rendering only, never HP/direction authority. */
final class HitNumbersV260 {
 static final float STEP=1f/15f,LIFE=13f/15f;
 static final int LIMIT=12;
 static final class Hit {
  final int amount;final String label;final float x,y;final long group;float age;int index,count=1;
  Hit(int n,float x,float y,long group){amount=n;label=""+n;this.x=x;this.y=y;this.group=group;}
  int frame(){return Math.min(12,(int)Math.floor((age+1e-6f)/STEP));}
  float offsetX(){return Math.round(9.3f*frame());}
  float offsetY(){float k=frame(),lane=1f+index-(count-1)*.5f;return Math.round(1.18f*k*k-(22.2f-4.65f*lane)*k);}
  float alpha(){int k=frame();return k<10?1f:(13f-k)/4f;}
  String text(){return label;}
 }
 private long sequence;
 private final List<Hit> hits=new ArrayList<>();private final List<Hit> view=Collections.unmodifiableList(hits);
 void add(int n,float x,float y){
  if(n<=0)return;
  Hit last=hits.isEmpty()?null:hits.get(hits.size()-1);
  boolean joined=last!=null&&last.age==0f&&last.x==x&&last.y==y&&last.count<3;
  long group=joined?last.group:++sequence;
  Hit h=new Hit(n,x,y,group);
  if(hits.size()>=LIMIT)hits.remove(0);
  hits.add(h);
  int count=0;for(Hit a:hits)if(a.group==group)count++;
  int i=0;for(Hit a:hits)if(a.group==group){a.index=i++;a.count=count;}
 }
 void tick(float dt){if(dt<=0||hits.isEmpty())return;for(Iterator<Hit> it=hits.iterator();it.hasNext();){Hit h=it.next();h.age+=dt;if(h.age+1e-6f>=LIFE)it.remove();}}
 List<Hit> hits(){return view;}
 void clear(){hits.clear();}
}
