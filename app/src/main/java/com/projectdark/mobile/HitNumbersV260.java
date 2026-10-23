package com.projectdark.mobile;
import java.util.*;
/** One bounded, independent popup per actual hit. No HP or combat logic lives here. */
final class HitNumbersV260 {
 static final float LIFE=.80f,STACK=12f;
 static final int LIMIT=12;
 static final class Hit {
  final int amount;final String label;final float x,y;float age,lift;
  Hit(int n,float x,float y){amount=n;label=""+n;this.x=x;this.y=y;}
  float offsetY(){float t=Math.min(1f,age/LIFE);return -lift-26f*(1f-(1f-t)*(1f-t));}
  float alpha(){return age<.58f?1f:Math.max(0f,(LIFE-age)/(LIFE-.58f));}
  String text(){return label;}
 }
 private final List<Hit> hits=new ArrayList<>();private final List<Hit> view=Collections.unmodifiableList(hits);
 void add(int n,float x,float y){if(n<=0)return;for(Hit h:hits)h.lift+=STACK;if(hits.size()>=LIMIT)hits.remove(0);hits.add(new Hit(n,x,y));}
 void tick(float dt){if(dt<=0||hits.isEmpty())return;for(Iterator<Hit> it=hits.iterator();it.hasNext();){Hit h=it.next();h.age+=dt;if(h.age>=LIFE)it.remove();}}
 List<Hit> hits(){return view;}
 void clear(){hits.clear();}
}
