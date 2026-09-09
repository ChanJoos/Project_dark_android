package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.io.InputStream;
import java.net.URL;

/** PROJECT DARK v0.58 - integrated world/entity/action/combat runtime prototype. */
public final class GameView extends View {
  private static final float W=960f,H=540f;
  private static final String WORLD_URL="https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png";
  private enum Action { IDLE,WALK,CAST,SWING,THRUST,THROW,PUNCH,SKILL,KICK }

  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),pixel=new Paint();
  private final RuntimeState state=new RuntimeState();
  private Bitmap world;
  private float scale=1,ox,oy,vx,vy;
  private final float jx=92,jy=444,jr=60;
  private float knobX=jx,knobY=jy;
  private boolean joy,running,pendingAttack;
  private long last;
  private int dir=0,attackMode=0;
  private float walkClock=0,actionClock=0,approachBlockedClock=0;
  private float attackCooldown=0,spellCooldown=0,skillCooldown=0,kickCooldown=0;
  private Action action=Action.IDLE;
  private RuntimeState.Npc approachNpc,dialogNpc;
  private RuntimeState.Monster targetMonster,combatApproachMonster;
  private String feedback="";
  private float feedbackClock=0;

  private final Runnable loop=new Runnable(){@Override public void run(){if(!running)return;long n=SystemClock.uptimeMillis();float dt=Math.min(.05f,(n-last)/1000f);last=n;update(dt);invalidate();postDelayed(this,16);}};

  public GameView(Context c){super(c);pixel.setFilterBitmap(false);setKeepScreenOn(true);loadWorld();}
  private void loadWorld(){new Thread(()->{try(InputStream in=new URL(WORLD_URL).openStream()){world=BitmapFactory.decodeStream(in);}catch(Exception ignored){}postInvalidate();}).start();}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(loop);}
  public void pause(){running=false;removeCallbacks(loop);}

  private void update(float dt){
    attackCooldown=Math.max(0,attackCooldown-dt);spellCooldown=Math.max(0,spellCooldown-dt);skillCooldown=Math.max(0,skillCooldown-dt);kickCooldown=Math.max(0,kickCooldown-dt);feedbackClock=Math.max(0,feedbackClock-dt);
    state.tick(dt);updateMonsters(dt);if(targetMonster!=null&&!targetMonster.alive){targetMonster=null;combatApproachMonster=null;pendingAttack=false;}
    if(!state.player().alive){action=Action.IDLE;approachNpc=null;combatApproachMonster=null;pendingAttack=false;joy=false;vx=vy=0;knobX=jx;knobY=jy;return;}
    if(isActing()){actionClock+=dt;if(actionClock>=duration(action)){actionClock=0;action=(joy&&(vx!=0||vy!=0))?Action.WALK:Action.IDLE;}return;}
    if(joy&&(vx!=0||vy!=0)){action=Action.WALK;state.tryMove(vx*145*dt,vy*145*dt);walkClock+=dt;approachNpc=null;combatApproachMonster=null;pendingAttack=false;approachBlockedClock=0;return;}
    if(approachNpc!=null){updateNpcApproach(dt);return;}
    if(combatApproachMonster!=null&&pendingAttack){updateCombatApproach(dt);return;}
    action=Action.IDLE;
  }

  private void updateNpcApproach(float dt){
    float dx=approachNpc.x-state.player().x,dy=approachNpc.y-state.player().y,d=(float)Math.sqrt(dx*dx+dy*dy);
    if(d<=56f){dialogNpc=approachNpc;approachNpc=null;approachBlockedClock=0;action=Action.IDLE;return;}
    setAutoDirection(dx,dy);action=Action.WALK;boolean moved=state.tryMove(vx*92*dt,vy*92*dt);walkClock+=dt;
    if(moved){approachBlockedClock=0;return;}approachBlockedClock+=dt;float ovx=vx,ovy=vy;
    boolean detour=state.tryMove(ovx*92*dt,-ovy*92*dt);if(!detour)detour=state.tryMove(-ovx*92*dt,ovy*92*dt);
    if(detour){approachBlockedClock=Math.max(0,approachBlockedClock-.12f);return;}if(approachBlockedClock>2f){approachNpc=null;approachBlockedClock=0;action=Action.IDLE;showFeedback("접근 경로 없음 [B]");}
  }

  private void updateCombatApproach(float dt){
    if(combatApproachMonster==null||!combatApproachMonster.alive){combatApproachMonster=null;pendingAttack=false;return;}
    AttackDef def=AttackDef.at(attackMode);float d=state.distanceTo(combatApproachMonster);
    if(d<=def.range){combatApproachMonster=null;pendingAttack=false;attack();return;}
    float dx=combatApproachMonster.x-state.player().x,dy=combatApproachMonster.y-state.player().y;setAutoDirection(dx,dy);action=Action.WALK;
    boolean moved=state.tryMove(vx*105*dt,vy*105*dt);walkClock+=dt;if(!moved){combatApproachMonster=null;pendingAttack=false;action=Action.IDLE;showFeedback("사거리 접근 실패 [B]");}
  }

  private void updateMonsters(float dt){
    if(!state.player().alive)return;for(RuntimeState.Monster m:state.monsters()){
      if(!m.alive)continue;m.attackCooldown=Math.max(0,m.attackCooldown-dt);float dx=state.player().x-m.x,dy=state.player().y-m.y,d=(float)Math.sqrt(dx*dx+dy*dy);
      if(d<180f&&d>42f){float step=28f*dt;state.tryMoveMonster(m,Math.signum(dx)*step,Math.signum(dy)*step);}
      else if(d<=42f&&m.attackCooldown<=0){state.damagePlayer(4);m.attackCooldown=1.2f;showFeedback("-4 HP [B]");}
    }
  }

  private void setAutoDirection(float dx,float dy){float sx=dx>=0?1f:-1f,sy=dy>=0?1f:-1f;vx=.707f*sx;vy=.707f*sy;if(sx<0&&sy>0)dir=0;else if(sx>0&&sy>0)dir=1;else if(sx<0)dir=2;else dir=3;}
  private boolean isActing(){return action!=Action.IDLE&&action!=Action.WALK;}
  private float duration(Action a){switch(a){case CAST:return .65f;case SWING:return .45f;case THRUST:return .38f;case THROW:return .52f;case PUNCH:return .32f;case SKILL:return .50f;case KICK:return .45f;default:return 0;}}
  private void trigger(Action a){if(isActing()||!state.player().alive)return;approachNpc=null;dialogNpc=null;combatApproachMonster=null;pendingAttack=false;action=a;actionClock=0;}
  private void showFeedback(String s){feedback=s;feedbackClock=.8f;}
  private boolean consume(SkillDef def){if(state.player().mp<def.mpCost){showFeedback("MP 부족");return false;}state.player().mp-=def.mpCost;return true;}
  private boolean inRange(float range){return targetMonster!=null&&targetMonster.alive&&state.distanceTo(targetMonster)<=range;}
  private void applyToTarget(SkillDef def){if(inRange(def.range))state.damage(targetMonster,def.damage);else showFeedback("사거리 밖 [B]");}
  private void cast(){if(isActing()||spellCooldown>0||!consume(SkillDef.CAST_PROTO))return;spellCooldown=SkillDef.CAST_PROTO.cooldown;trigger(Action.CAST);applyToTarget(SkillDef.CAST_PROTO);}
  private void skill(){if(isActing()||skillCooldown>0||!consume(SkillDef.SKILL_PROTO))return;skillCooldown=SkillDef.SKILL_PROTO.cooldown;trigger(Action.SKILL);applyToTarget(SkillDef.SKILL_PROTO);}
  private void kick(){if(isActing()||kickCooldown>0)return;kickCooldown=SkillDef.KICK_PROTO.cooldown;trigger(Action.KICK);applyToTarget(SkillDef.KICK_PROTO);}
  private Action actionFor(AttackDef.Kind k){switch(k){case THRUST:return Action.THRUST;case THROW:return Action.THROW;case PUNCH:return Action.PUNCH;default:return Action.SWING;}}
  private void attack(){
    if(isActing()||attackCooldown>0||!state.player().alive)return;AttackDef def=AttackDef.at(attackMode);
    if(targetMonster!=null&&targetMonster.alive&&!inRange(def.range)){combatApproachMonster=targetMonster;pendingAttack=true;showFeedback("타깃 접근 [ADAPTED]");return;}
    attackCooldown=def.cooldown;trigger(actionFor(def.kind));if(targetMonster!=null&&targetMonster.alive)state.damage(targetMonster,def.damage);else showFeedback("타깃 없음");
  }

  protected void onSizeChanged(int w,int h,int ow,int oh){scale=Math.min(w/W,h/H);ox=(w-W*scale)/2;oy=(h-H*scale)/2;}
  protected void onDraw(Canvas c){c.drawColor(Color.BLACK);c.save();c.translate(ox,oy);c.scale(scale,scale);drawWorld(c);drawNpcs(c);drawMonsters(c);drawCharacter(c);drawActionFx(c);drawHud(c);drawDialogue(c);drawDeath(c);c.restore();}

  private void drawWorld(Canvas c){if(world==null){p.setColor(0xff090909);c.drawRect(0,0,W,H,p);return;}int sw=world.getWidth(),sh=world.getHeight();float da=W/H,sa=sw/(float)sh;Rect s;if(sa>da){int uw=Math.round(sh*da),l=(sw-uw)/2;s=new Rect(l,0,l+uw,sh);}else{int uh=Math.round(sw/da),t=(sh-uh)/2;s=new Rect(0,t,sw,t+uh);}c.drawBitmap(world,s,new RectF(0,0,W,H),pixel);p.setColor(0x26000000);c.drawRect(0,0,W,H,p);}

  private void drawNpcs(Canvas c){for(RuntimeState.Npc n:state.npcs()){p.setColor(0x66000000);c.drawOval(new RectF(n.x-11,n.y-3,n.x+11,n.y+4),p);p.setColor(0xffcab58b);c.drawCircle(n.x,n.y-39,7,p);p.setColor(0xff6c5946);c.drawRect(n.x-7,n.y-31,n.x+7,n.y-11,p);p.setColor(0xffb58f58);c.drawRect(n.x-5,n.y-12,n.x-1,n.y-3,p);c.drawRect(n.x+1,n.y-12,n.x+5,n.y-3,p);p.setTextSize(9);p.setColor(0xfff3e1ae);float tw=p.measureText(n.name);c.drawText(n.name,n.x-tw/2,n.y-52,p);if(approachNpc==n){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xfff0d17a);c.drawCircle(n.x,n.y-24,19,p);p.setStyle(Paint.Style.FILL);}}}

  private void drawMonsters(Canvas c){for(RuntimeState.Monster m:state.monsters()){if(!m.alive)continue;p.setColor(0x66000000);c.drawOval(new RectF(m.x-15,m.y-4,m.x+15,m.y+5),p);p.setColor(m.hitFlash>0?0xffd9e8d7:0xff6a7c69);c.drawOval(new RectF(m.x-13,m.y-31,m.x+13,m.y-5),p);p.setColor(0xffd8c27b);c.drawCircle(m.x-5,m.y-20,2,p);c.drawCircle(m.x+5,m.y-20,2,p);bar(c,m.x-18,m.y-42,m.x+18,m.y-37,0xffd63442,m.hp/(float)m.maxHp);if(m.damagePopupClock>0){p.setTextSize(12);p.setColor(0xffffdc72);String d="-"+m.lastDamage;float tw=p.measureText(d);c.drawText(d,m.x-tw/2,m.y-50-(.65f-m.damagePopupClock)*20,p);}if(targetMonster==m){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xffffd86b);c.drawOval(new RectF(m.x-21,m.y-9,m.x+21,m.y+9),p);p.setStyle(Paint.Style.FILL);}}}

  private void drawCharacter(Canvas c){float px=state.player().x,py=state.player().y,S=2f,x=px,y=py;int f=action==Action.WALK?((int)(walkClock*8f)%4):0;float bob=(f==1||f==3)?-1:0;p.setColor(0x66000000);c.drawOval(new RectF(x-13,y-4,x+13,y+4),p);c.save();c.translate(x,y-56+bob*S);c.scale(S,S);c.translate(-8,0);int skin=0xffffc68f,hair=0xff3b251b,outline=0xff171313,shirt=0xffeee5d3,blue=0xff3c6382,pants=0xff393a3a,shoe=0xff6a4526;int step=(f==1?1:f==3?-1:0);boolean left=(dir==0||dir==2),back=dir>=2;float phase=isActing()?Math.min(1f,actionClock/duration(action)):0;int armLift=action==Action.CAST?(phase<.25f?-5:-8):0;int kick=action==Action.KICK&&phase>.22f&&phase<.78f?5:0;rect(c,outline,4+step-kick,19,7+step,26);rect(c,outline,10-step,19,13-step+kick,26);rect(c,pants,5+step-kick,19,7+step,24);rect(c,pants,10-step,19,12-step+kick,24);rect(c,shoe,4+step-kick,24,7+step,27);rect(c,shoe,10-step,24,13-step+kick,27);rect(c,outline,3,10,14,21);rect(c,shirt,4,11,13,19);rect(c,blue,4,16,13,20);rect(c,0xffc9b070,7,11,9,20);int as=(f==1?1:f==3?-1:0);rect(c,outline,1,11+as+armLift,4,19+as);rect(c,skin,2,12+as+armLift,3,18+as);rect(c,outline,13,11-as+armLift,16,19-as);rect(c,skin,14,12-as+armLift,15,18-as);rect(c,outline,3,2,14,12);rect(c,skin,4,3,13,11);rect(c,hair,3,1,14,6);rect(c,hair,2,3,5,9);rect(c,hair,12,3,15,8);rect(c,hair,5,0,12,3);if(!back){int eye=0xff252020;if(left){rect(c,eye,5,7,6,8);rect(c,eye,9,7,10,8);}else{rect(c,eye,7,7,8,8);rect(c,eye,11,7,12,8);}}rect(c,0xff76503b,left?3:11,3,left?4:12,6);if(action==Action.SWING||action==Action.THRUST){p.setColor(0xffd7d2c5);p.setStrokeWidth(2);float ex=left?-7:23,ey=action==Action.THRUST?14:6;c.drawLine(left?2:15,14,ex,ey,p);}c.restore();}

  private void drawActionFx(Canvas c){if(!isActing())return;float px=state.player().x,py=state.player().y,q=Math.min(1f,actionClock/duration(action));if(action==Action.CAST){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(0xaa78b9ff);c.drawCircle(px,py-70,10+18*q,p);c.drawCircle(px,py-70,24-8*q,p);p.setStyle(Paint.Style.FILL);}else if(action==Action.THROW){p.setColor(0xffffd76b);float sx=(dir==0||dir==2)?-1:1,sy=(dir<2)?1:-1;c.drawCircle(px+sx*55*q,py-30+sy*35*q,4,p);}else if(action==Action.PUNCH||action==Action.KICK||action==Action.SKILL){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(action==Action.SKILL?5:3);p.setColor(action==Action.SKILL?0xaa7fffa8:0xaaffefb0);float sx=(dir==0||dir==2)?-1:1;c.drawArc(new RectF(px+sx*18-18,py-50,px+sx*18+18,py-14),20,130,false,p);p.setStyle(Paint.Style.FILL);}}

  private void rect(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}private void panel(Canvas c,float l,float t,float r,float b){p.setColor(0x9a0b0b0b);p.setStyle(Paint.Style.FILL);c.drawRoundRect(new RectF(l,t,r,b),10,10,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xaaad9364);c.drawRoundRect(new RectF(l,t,r,b),10,10,p);p.setStyle(Paint.Style.FILL);}private void text(Canvas c,String s,float x,float y,float size){p.setTextSize(size);p.setColor(0xffeee4cf);c.drawText(s,x,y,p);}private void bar(Canvas c,float l,float t,float r,float b,int color,float ratio){ratio=Math.max(0,Math.min(1,ratio));p.setColor(0xb51a1714);c.drawRoundRect(new RectF(l,t,r,b),5,5,p);p.setColor(color);c.drawRoundRect(new RectF(l,t,l+(r-l)*ratio,b),5,5,p);}

  private void drawHud(Canvas c){panel(c,12,12,165,116);text(c,"GROUP",24,31,12);String[] n={"조춘찬","수수료좀챙","이루","별빛영혼"};for(int i=0;i<4;i++){text(c,n[i],27,50+i*15,10);bar(c,91,42+i*15,154,48+i*15,0xffdf3342,1f);}panel(c,176,12,320,70);text(c,"퀘스트",190,32,13);text(c,"밀레스의 첫걸음 0/4",190,52,10);panel(c,372,12,604,55);if(targetMonster!=null){text(c,targetMonster.name,405,29,11);bar(c,392,36,584,44,0xffe21d2e,targetMonster.hp/(float)targetMonster.maxHp);}else{text(c,"타깃 없음",456,31,11);}panel(c,714,12,902,124);text(c,"밀레스",728,31,11);p.setColor(0xaa090909);c.drawRect(728,38,887,104,p);text(c,"X:"+(int)state.player().x+" Y:"+(int)state.player().y,800,117,9);String[] menu={"≡","▣","Q","G","W","⚙"};for(int i=0;i<6;i++){panel(c,912,20+i*50,948,58+i*50);text(c,menu[i],925,45+i*50,15);}panel(c,12,294,272,382);text(c,"[일반] PROJECT DARK runtime",23,316,9);text(c,"NPC 접근/대화 · 몬스터 타깃/추적",23,332,9);text(c,"MP 마법 · P 공격형태 · SK 기술 · K 발차기",23,348,9);if(feedbackClock>0)text(c,feedback,23,365,10);else text(c,"일반   파티   길드   귓속말   시스템",23,373,9);p.setColor(0x33101010);c.drawCircle(jx,jy,jr,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0x99d8c49b);c.drawCircle(jx,jy,jr,p);p.setStyle(Paint.Style.FILL);p.setColor(0x997e7057);c.drawCircle(knobX,knobY,24,p);panel(c,337,462,608,528);text(c,"Lv 1",351,486,12);bar(c,400,474,594,485,0xffe62c42,state.player().hp/(float)state.player().maxHp);bar(c,400,490,594,501,0xff2388df,state.player().mp/(float)state.player().maxMp);bar(c,400,507,594,518,0xff48b84d,.35f);String[] slots={"HP","MP","P","SK","K"};for(int i=0;i<5;i++)round(c,650+i*48,466,20,slots[i]);cooldown(c,698,466,20,spellCooldown,SkillDef.CAST_PROTO.cooldown);cooldown(c,794,466,20,skillCooldown,SkillDef.SKILL_PROTO.cooldown);cooldown(c,842,466,20,kickCooldown,SkillDef.KICK_PROTO.cooldown);round(c,895,478,34,"ATK");cooldown(c,895,478,34,attackCooldown,AttackDef.at(attackMode).cooldown);round(c,920,516,26,"AUTO");text(c,AttackDef.at(attackMode).label,840,530,8);}
  private void cooldown(Canvas c,float x,float y,float r,float remaining,float total){if(remaining<=0||total<=0)return;float ratio=Math.min(1f,remaining/total);p.setColor(0x99000000);c.drawArc(new RectF(x-r,y-r,x+r,y+r),-90,360*ratio,true,p);p.setTextSize(8);p.setColor(Color.WHITE);String s=String.format(java.util.Locale.US,"%.1f",remaining);float tw=p.measureText(s);c.drawText(s,x-tw/2,y+3,p);}
  private void drawDialogue(Canvas c){if(dialogNpc==null)return;panel(c,250,330,710,440);text(c,dialogNpc.name,270,355,13);drawWrappedText(c,dialogNpc.dialogue,270,380,420,12,18);text(c,"화면을 터치하면 닫기",555,425,9);}private void drawDeath(Canvas c){if(state.player().alive)return;p.setColor(0xaa000000);c.drawRect(0,0,W,H,p);panel(c,330,205,630,335);text(c,"행동 불능 [B]",421,240,18);text(c,"프로토타입 부활: 화면 중앙 터치",375,276,12);text(c,"원작 사망/패널티 규칙을 의미하지 않음",368,302,10);}private void drawWrappedText(Canvas c,String s,float x,float y,float maxWidth,float size,float lineHeight){p.setTextSize(size);p.setColor(0xffeee4cf);String[] words=s.split(" ");String line="";float yy=y;for(String word:words){String test=line.length()==0?word:line+" "+word;if(p.measureText(test)>maxWidth&&line.length()>0){c.drawText(line,x,yy,p);yy+=lineHeight;line=word;}else line=test;}if(line.length()>0)c.drawText(line,x,yy,p);}private void round(Canvas c,float x,float y,float r,String s){p.setColor(0xaa15120e);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xffc9a86c);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.FILL);p.setTextSize(s.length()>3?8:10);p.setColor(0xffffefd0);float tw=p.measureText(s);c.drawText(s,x-tw/2,y+3,p);}

  public boolean onTouchEvent(MotionEvent e){float x=(e.getX()-ox)/scale,y=(e.getY()-oy)/scale;switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:if(!state.player().alive){if(dist(x,y,480,270)<=180){state.revivePlayer();targetMonster=null;action=Action.IDLE;}return true;}if(dialogNpc!=null){dialogNpc=null;return true;}RuntimeState.Npc npc=state.hitNpc(x,y,34f);if(npc!=null){if(state.distanceTo(npc)<=56f)dialogNpc=npc;else{approachNpc=npc;approachBlockedClock=0;}return true;}RuntimeState.Monster monster=state.hitMonster(x,y,34f);if(monster!=null){targetMonster=monster;approachNpc=null;return true;}if(dist(x,y,jx,jy)<=jr*1.5f){joy=true;approachNpc=null;combatApproachMonster=null;pendingAttack=false;stick(x,y);return true;}if(dist(x,y,698,466)<=25){cast();return true;}if(dist(x,y,746,466)<=25){attackMode=(attackMode+1)%AttackDef.PROTOTYPES.length;showFeedback(AttackDef.at(attackMode).label+" [B]");return true;}if(dist(x,y,794,466)<=25){skill();return true;}if(dist(x,y,842,466)<=25){kick();return true;}if(dist(x,y,895,478)<=42){attack();return true;}return true;case MotionEvent.ACTION_MOVE:if(joy)stick(x,y);return true;case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:joy=false;knobX=jx;knobY=jy;vx=vy=0;return true;}return true;}
  private void stick(float x,float y){if(isActing()||!state.player().alive)return;float dx=x-jx,dy=y-jy,len=(float)Math.sqrt(dx*dx+dy*dy);if(len>jr){dx=dx/len*jr;dy=dy/len*jr;len=jr;}knobX=jx+dx;knobY=jy+dy;if(len<8){vx=vy=0;return;}float nx=dx/len,ny=dy/len;if(Math.abs(nx)>Math.abs(ny)){if(nx>0){vx=.707f;vy=.707f;dir=1;}else{vx=-.707f;vy=-.707f;dir=2;}}else{if(ny>0){vx=-.707f;vy=.707f;dir=0;}else{vx=.707f;vy=-.707f;dir=3;}}}
  private float dist(float a,float b,float c,float d){float x=a-c,y=b-d;return(float)Math.sqrt(x*x+y*y);}
}
