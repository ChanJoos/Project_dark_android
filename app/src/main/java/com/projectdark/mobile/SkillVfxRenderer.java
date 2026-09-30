package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import java.util.*;

/** Event-only visual projection: accepted start => caster, resolved non-miss => recipient. */
final class SkillVfxRenderer {
  interface Anchors {float x(String id);float y(String id);}
  static final class Pulse {
    final long actionSequence;final String id,sheet,anchor;final int row;final boolean caster;final float duration,x,y;float age;
    Pulse(long seq,String id,String sheet,int row,String anchor,boolean caster,float duration,float x,float y){actionSequence=seq;this.id=id;this.sheet=sheet;this.row=row;this.anchor=anchor;this.caster=caster;this.duration=duration;this.x=x;this.y=y;}
  }
  private final SkillPresentationCatalog catalog;private final Map<String,Bitmap> sheets=new HashMap<>();
  final List<Pulse> pulses=new ArrayList<>();private final Set<String> seen=new HashSet<>();private final Deque<String> history=new ArrayDeque<>();private final Paint p=new Paint();
  SkillVfxRenderer(Context c,SkillPresentationCatalog catalog){this.catalog=catalog;p.setFilterBitmap(false);for(String name:new String[]{"caster","target","status"})try{BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=2;sheets.put(name,BitmapFactory.decodeStream(c.getAssets().open("skill-presentation/"+name+".png"),null,options));}catch(Exception ignored){}}
  void tick(float dt){float safe=Math.max(0,dt);for(Pulse f:pulses)f.age+=safe;pulses.removeIf(f->f.age>=f.duration);}
  void clear(){pulses.clear();}
  void consume(List<CombatResolver.Event> events,Anchors a){for(CombatResolver.Event e:events){
    if(!RuntimeCombatSession.PLAYER_ID.equals(e.actorId))continue;
    String id=e.actionId.startsWith("attack_proto_")?"SK_공통_001":e.actionId;SkillPresentationCatalog.Entry v=catalog.get(id);if(v==null)continue;
    if(e.type==CombatResolver.EventType.ACTION_CANCELLED){pulses.removeIf(f->f.actionSequence==e.actionSequence);continue;}
    boolean caster=e.type==CombatResolver.EventType.ACTION_STARTED,target=e.type==CombatResolver.EventType.HIT_FEEDBACK&&e.hitSemantic!=CombatResolver.HitSemantic.MISS;
    if(!caster&&!target)continue;String key=e.actionSequence+":"+(caster?"caster":"target");if(!seen.add(key))continue;history.add(key);while(history.size()>256)seen.remove(history.removeFirst());
    int row=caster?casterRow(v.caster):v.targetRow;if(row<0)continue;String anchor=caster?e.actorId:e.targetId;
    String sheet=caster?"caster":v.targetSheet;float x=a.x(anchor),y=a.y(anchor);if(!Float.isFinite(x)||!Float.isFinite(y))continue;
    pulses.add(new Pulse(e.actionSequence,id,sheet,row,anchor,caster,caster?.30f:.48f,x,y));if(pulses.size()>64)pulses.remove(0);
  }}
  static int casterRow(String name){switch(name){case "SLASH":return 0;case "MARTIAL":return 1;case "ARCANE":return 2;case "HEAL":return 3;default:return -1;}}
  void draw(Canvas c,Anchors a){for(Pulse f:pulses){Bitmap b=sheets.get(f.sheet);if(b==null)continue;int rows=f.sheet.equals("caster")?4:8,cw=b.getWidth()/6,ch=b.getHeight()/rows;int col=Math.min(5,(int)(f.age/f.duration*6));
    float x=a.x(f.anchor),y=a.y(f.anchor);if(!Float.isFinite(x)||!Float.isFinite(y)){x=f.x;y=f.y;}
    float size=f.caster?58:68,height=size*ch/(float)cw;float centerY=y-(f.caster?23:25);p.setAlpha(Math.round(255*Math.min(1,(f.duration-f.age)/.10f)));
    c.drawBitmap(b,new Rect(col*cw,f.row*ch,(col+1)*cw,(f.row+1)*ch),new RectF(x-size/2,centerY-height/2,x+size/2,centerY+height/2),p);
  }p.setAlpha(255);}
}
