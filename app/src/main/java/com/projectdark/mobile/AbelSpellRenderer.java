package com.projectdark.mobile;
import android.graphics.*;import java.util.*;
/** Small project-authored particles tied to accepted monster actions. No original FX claim. */
final class AbelSpellRenderer {
 private static final class Shot {long seq;String id,actor;float x,y,tx,ty,age;Shot(CombatResolver.Event e,RuntimeState.Monster m,RuntimeState s){seq=e.actionSequence;id=e.actionId;actor=e.actorId;x=m.x;y=m.y-24;tx=s.player().x;ty=s.player().y-24;}}
 private final List<Shot> shots=new ArrayList<>();private final Paint p=new Paint();
 void consume(List<CombatResolver.Event> events,RuntimeState s){for(CombatResolver.Event e:events)if(AbelCombatProfile.spell(e.actionId)&&!"MONSTER_NARCOLI".equals(e.actionId)&&!"MONSTER_DISPEL".equals(e.actionId)){if(e.type==CombatResolver.EventType.ACTION_CANCELLED){shots.removeIf(x->x.seq==e.actionSequence);continue;}if(e.type==CombatResolver.EventType.ACTION_STARTED){for(RuntimeState.Monster m:s.monsters())if(m.id.equals(e.actorId)){shots.add(new Shot(e,m,s));break;}if(shots.size()>24)shots.remove(0);}}}
 void tick(float dt){for(Shot s:shots)s.age+=Math.max(0,dt);shots.removeIf(s->s.age>1.25f);}
 void clear(){shots.clear();}
 void draw(Canvas c){p.setAntiAlias(true);for(Shot s:shots){int color=s.id.equals("MONSTER_FIRE")?0xffff9a36:s.id.startsWith("MONSTER_VENOM")?0xff82d36a:s.id.equals("MONSTER_WATER")?0xff76cfff:s.id.equals("MONSTER_RECOVER")?0xffa5f5be:0xffcfa2ef;p.setColor(color);p.setAlpha(Math.max(0,(int)(255*(1-s.age/1.25f))));float t=Math.min(1,s.age/.65f),x=s.x+(s.tx-s.x)*t,y=s.y+(s.ty-s.y)*t;boolean heal=s.id.equals("MONSTER_RECOVER");if(heal){x=s.x;y=s.y;}p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawOval(x-12,y-5,x+12,y+5,p);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,heal?5:4,p);}p.setAlpha(255);p.setAntiAlias(false);}
}
